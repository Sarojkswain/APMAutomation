package com.ca.apm.testbed;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ca.tas.testbed.ITestbed;

import static org.apache.commons.lang3.StringUtils.isBlank;

/**
 * Created by haiva01 on 28.4.2017.
 */
public final class BrowserPropertyHelper {
    private static final Logger log = LoggerFactory.getLogger(BrowserPropertyHelper.class);

    public static void handleBrowserProperty(ITestbed testbed) {
        handleBrowserProperty(testbed, ClassLoader.getSystemClassLoader());
    }

    /**
     * This method is used to load "browser" property from "tas_run.properties" or provides a
     * default value in a testbed's <code>create()</code> method.
     *
     * @param testbed testbed to handle
     */
    public static void handleBrowserProperty(ITestbed testbed, ClassLoader classLoader) {
        Properties props;
        String browser = null;
        try (InputStream propsStream = classLoader.getResourceAsStream("tas_run.properties")) {
            if (propsStream != null) {
                log.info("Found tas_run.properties!");
                props = new Properties();
                props.load(propsStream);
                browser = props.getProperty("browser");
                if (isBlank(browser)) {
                    log.info("browser property in tas_run.properties is blank or unset");
                }
            } else {
                log.info("tas_run.properties not found.");
            }
        } catch (IOException e) {
            log.error("Exception occurred while reading tas_run.properties: ", e);
        }

        if (isBlank(browser) || browser.startsWith("${")) {
            log.info("browser unset");
            browser = "CHROME";
        }
        testbed.addProperty("browser", browser);
        log.info("Chosen browser: {}", browser);
    }
}
