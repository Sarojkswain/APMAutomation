package com.ca.apm.tests.util.selenium;

import io.github.bonigarcia.wdm.BrowserManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;
import java.util.concurrent.*;

/**
 * @author haiva01
 */
public final class WebDriverProcurementUtils {
    private static final Logger LOGGER = LoggerFactory.getLogger(WebDriverProcurementUtils.class);

    private static final long WEBDRIVER_DOWNLOAD_CHECK_TIMEOUT_SECONDS = 5;

    public static String procureWebDriver(Class<? extends BrowserManager> driverManagerClass) {
        return procureWebDriver(driverManagerClass, null);
    }

    public static String procureWebDriver(Class<? extends BrowserManager> driverManagerClass,
        String forcedVersion) {
        final String driverName = driverManagerClass.getSimpleName();
        while (true) {
            try {
                LOGGER.info("Trying to procure {}.", driverName);
                final Method getInstanceMethod = driverManagerClass.getMethod("getInstance");
                final BrowserManager browserManager =
                    (BrowserManager) getInstanceMethod.invoke(null);
                if (forcedVersion != null) {
                    browserManager.setup(forcedVersion);
                } else {
                    browserManager.setup();
                }
                String version = browserManager.getDownloadedVersion();
                LOGGER.info("Downloaded {} version {}", driverName, version);
                return version;
            } catch (Throwable e) {
                LOGGER.error("Got exception while trying to download {1}. Exception: {0}", e, driverName);
                LOGGER.debug("Sleeping a bit before retrying download of {}.", driverName);
                try {
                    TimeUnit.SECONDS.sleep(30);
                } catch (InterruptedException e1) {
                    LOGGER.error("Got interrupted while waiting to download {1}. Exception: {0}",
                        e, driverManagerClass.getSimpleName());
                }
            }
        }
    }

    public static void downloadWebDrivers(Collection<String> browsers) {
        downloadWebDrivers(Browser.resolveBrowsers(browsers));
    }

    public static void downloadWebDrivers(Set<Browser> browsers) {
        ExecutorService executor = Executors.newCachedThreadPool();
        try {
            Collection<Future<String>> webDriverVersionFutures = new ArrayList<>(browsers.size());
            for (final Browser browser : browsers) {
                final Class<? extends BrowserManager> driverManagerClass = browser.getDriverManagerClass();
                if (driverManagerClass == null) {
                    // Firefox does not require a web driver, so we skip when null
                    continue;
                }
                try {
                    Future<String> webDriverVersionFuture = executor.submit(new Callable<String>() {
                        @Override
                        public String call() throws Exception {
                            String webDriverVersion = procureWebDriver(driverManagerClass, browser.getForcedVersion());
                            LOGGER.info(
                                "Operation procureWebDriver for {} finished, WebDriver version: {}",
                                driverManagerClass, webDriverVersion);
                            return webDriverVersion;
                        }
                    });
                    LOGGER.debug("browser: {}, webDriverVersionFuture: {}", browser,
                        webDriverVersionFuture);
                    webDriverVersionFutures.add(webDriverVersionFuture);
                } catch (RejectedExecutionException e) {
                    LOGGER.warn("Unable to execute operation procureWebDriver in another thread");
                    String webDriverVersion = procureWebDriver(driverManagerClass, browser.getForcedVersion());
                    LOGGER.info(
                        "Operation procureWebDriver for {} finished, WebDriver version: {}",
                        driverManagerClass, webDriverVersion);
                }
            }

            waitForWebDriverDownload(webDriverVersionFutures);
        } finally {
            executor.shutdownNow();
        }
    }

    protected static void waitForWebDriverDownload(
        Collection<Future<String>> webDriverVersionFutures) {
        Collection<Future<String>> todoList = new ArrayList<>(webDriverVersionFutures);
        while (!todoList.isEmpty()) {
            for (Future<String> webDriverVersionFuture : webDriverVersionFutures) {
                if (webDriverVersionFuture.isDone()) {
                    LOGGER.debug("Done: {}", webDriverVersionFuture);
                    todoList.remove(webDriverVersionFuture);
                } else {
                    try {
                        LOGGER.debug("Waiting for {}", webDriverVersionFuture);
                        webDriverVersionFuture.get(WEBDRIVER_DOWNLOAD_CHECK_TIMEOUT_SECONDS,
                            TimeUnit.SECONDS);
                    } catch (InterruptedException e) {
                        // just loop again
                    } catch (ExecutionException e) {
                        LOGGER.error("Failed to download WebDriver. Exception: {0}", e);
                        throw new RuntimeException(e);
                    } catch (TimeoutException e) {
                        LOGGER.warn("Still waiting for WebDriver to be downloaded.");
                    }
                }
            }
        }
    }
}
