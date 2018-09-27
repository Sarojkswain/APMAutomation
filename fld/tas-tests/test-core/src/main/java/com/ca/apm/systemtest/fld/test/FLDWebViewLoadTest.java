package com.ca.apm.systemtest.fld.test;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;

import com.ca.apm.systemtest.fld.test.loads.BaseFldLoadTest;
import com.ca.apm.systemtest.fld.testbed.FLDConstants;
import com.ca.apm.systemtest.fld.testbed.FLDLoadConstants;

@Test
public class FLDWebViewLoadTest extends BaseFldLoadTest  implements FLDLoadConstants, FLDConstants {

    private static final Logger LOGGER = LoggerFactory.getLogger(FLDWebViewLoadTest.class);
    private String[] chromeUrls = {
       "http://WEBVIEW_HOST_NAME:8080/#agentAdm;tr=0",
       "http://WEBVIEW_HOST_NAME:8080/#console;db=S3_Perf_Test_25_Metrics_Per_Agent_Total_500_Metrics_20_Charts;dn=SuperDomain;mm=Clean_BofA_MM_12;tr=0",
       "http://WEBVIEW_HOST_NAME:8080/#console;db=S3_Perf_Test_25_Metrics_Per_Agent_Total_500_Metrics_20_Charts_No_Legend;dn=SuperDomain;mm=Clean_BofA_MM_20;tr=0",
       "http://WEBVIEW_HOST_NAME:8080/#console;db=S3_Perf_Test_25_Metrics_Per_Agent_Total_500_Metrics_20_Charts_No_Legend;dn=SuperDomain;mm=Clean_BofA_MM_21;tr=0",
       "http://WEBVIEW_HOST_NAME:8080/#console;db=S3_Perf_Test_25_Metrics_Per_Agent_Total_500_Metrics_20_Charts_No_Legend;dn=SuperDomain;mm=Clean_BofA_MM_22;tr=0",
       "http://WEBVIEW_HOST_NAME:8080/#console;db=S3_Perf_Test_25_Metrics_Per_Agent_Total_500_Metrics_20_Charts_No_Legend;dn=SuperDomain;mm=Clean_BofA_MM_24;tr=0",
       "http://WEBVIEW_HOST_NAME:8080/#console;db=S3_Perf_Test_25_Metrics_Per_Agent_Total_500_Metrics_20_Charts_No_Legend;dn=SuperDomain;mm=Clean_BofA_MM_25;tr=0",
       "http://WEBVIEW_HOST_NAME:8080/#console;db=S3_Perf_Test_25_Metrics_Per_Agent_Total_500_Metrics_20_Charts_No_Legend;dn=SuperDomain;mm=Clean_BofA_MM_26;tr=0",
       "http://WEBVIEW_HOST_NAME:8080/#console;db=S3_Perf_Test_25_Metrics_Per_Agent_Total_500_Metrics_20_Charts_No_Legend;dn=SuperDomain;mm=Clean_BofA_MM_28;tr=0",
       "http://WEBVIEW_HOST_NAME:8080/#console;db=S3_Perf_Test_25_Metrics_Per_Agent_Total_500_Metrics_20_Charts_No_Legend;dn=SuperDomain;mm=Clean_BofA_MM_29;tr=0",
       "http://WEBVIEW_HOST_NAME:8080/#console;db=S3_Perf_Test_25_Metrics_Per_Agent_Total_500_Metrics_20_Charts_No_Legend;dn=SuperDomain;mm=Clean_BofA_MM_30;tr=0",
       "http://WEBVIEW_HOST_NAME:8080/#home;tr=0",
       "http://WEBVIEW_HOST_NAME:8080/#investigator;tab-in=mb;tab-tv=pd;tr=0;uid=SuperDomain%257Cwurlitzer-stress-app%257CStressTestProcess%257CWurlitzerAgent_1%257CAgent+Stats%257CSustainability%257CResponse+Time+Stats%253AAgent+Time+(ns)",
       "http://WEBVIEW_HOST_NAME:8080/#management;cn=Fake_Metrics_5_5_metrics;ct=96;dn=SuperDomain;mm=Clean_BofA_MM_24;tr=0",
    };
    private String[] ffUrls = {
        "http://WEBVIEW_HOST_NAME:8080/#console;db=S3_Perf_Test_25_Metrics_Per_Agent_Total_500_Metrics_20_Charts;dn=SuperDomain;mm=Clean_BofA_MM_10;tr=0",
        "http://WEBVIEW_HOST_NAME:8080/#console;db=S3_Perf_Test_25_Metrics_Per_Agent_Total_500_Metrics_20_Charts;dn=SuperDomain;mm=Clean_BofA_MM_11;tr=0",
        "http://WEBVIEW_HOST_NAME:8080/#console;db=S3_Perf_Test_25_Metrics_Per_Agent_Total_500_Metrics_20_Charts;dn=SuperDomain;mm=Clean_BofA_MM_19;tr=0",
        "http://WEBVIEW_HOST_NAME:8080/#console;db=S3_Perf_Test_25_Metrics_Per_Agent_Total_500_Metrics_20_Charts;dn=SuperDomain;mm=Clean_BofA_MM_1;tr=0",
        "http://WEBVIEW_HOST_NAME:8080/#console;db=S3_Perf_Test_25_Metrics_Per_Agent_Total_500_Metrics_20_Charts;dn=SuperDomain;mm=Clean_BofA_MM_20;tr=0",
        "http://WEBVIEW_HOST_NAME:8080/#console;db=S3_Perf_Test_25_Metrics_Per_Agent_Total_500_Metrics_20_Charts;dn=SuperDomain;mm=Clean_BofA_MM_2;tr=0",
        "http://WEBVIEW_HOST_NAME:8080/#console;db=S3_Perf_Test_25_Metrics_Per_Agent_Total_500_Metrics_20_Charts;dn=SuperDomain;mm=Clean_BofA_MM_3;tr=0",
        "http://WEBVIEW_HOST_NAME:8080/#console;db=S3_Perf_Test_25_Metrics_Per_Agent_Total_500_Metrics_20_Charts;dn=SuperDomain;mm=Clean_BofA_MM_5;tr=0",
        "http://WEBVIEW_HOST_NAME:8080/#console;db=S3_Perf_Test_25_Metrics_Per_Agent_Total_500_Metrics_20_Charts;dn=SuperDomain;mm=Clean_BofA_MM_6;tr=0",
        "http://WEBVIEW_HOST_NAME:8080/#console;db=S3_Perf_Test_25_Metrics_Per_Agent_Total_500_Metrics_20_Charts;dn=SuperDomain;mm=Clean_BofA_MM_7;tr=0",
        "http://WEBVIEW_HOST_NAME:8080/#console;db=S3_Perf_Test_25_Metrics_Per_Agent_Total_500_Metrics_20_Charts;dn=SuperDomain;mm=Clean_BofA_MM_8;tr=0",
        "http://WEBVIEW_HOST_NAME:8080/#console;db=S3_Perf_Test_25_Metrics_Per_Agent_Total_500_Metrics_20_Charts;dn=SuperDomain;mm=Clean_BofA_MM_9;tr=0",
        "http://WEBVIEW_HOST_NAME:8080/#console;db=S3_Perf_Test_25_Metrics_Per_Agent_Total_500_Metrics_20_Charts_No_Legend;dn=SuperDomain;mm=Clean_BofA_MM_23;tr=0",
        "http://WEBVIEW_HOST_NAME:8080/#investigator;tab-in=mb;tab-tv=pd;tr=0;uid=SuperDomain%257Cwurlitzer-stress-app%257CStressTestProcess%257CWurlitzerAgent_11%257CFakeMetricSet3%257CFakeServlet1%253AAverage+Response+Time+(ms)",
        "http://WEBVIEW_HOST_NAME:8080/#investigator;tab-in=mb;tab-tv=pd;tr=0;uid=SuperDomain%257Cwurlitzer-stress-app%257CStressTestProcess%257CWurlitzerAgent_14%257CBackends%257CMySqlDatabase_1%253AResponses+Per+Interval",
        "http://WEBVIEW_HOST_NAME:8080/#investigator;tab-in=mb;tab-tv=pd;tr=0;uid=SuperDomain%257Cwurlitzer-stress-app%257CStressTestProcess%257CWurlitzerAgent_27%257CFrontends%257CApps%257CNew_Apps_1%257CURLs%257CServlet_1%253AAverage+Response+Time+(ms)",
        "http://WEBVIEW_HOST_NAME:8080/#investigator;tab-in=mb;tab-tv=pd;tr=0;uid=SuperDomain%257Cwurlitzer-stress-app%257CStressTestProcess%257CWurlitzerAgent_8%257CAgent+Stats%257CSustainability%257CCPU+Stats%253AAgent+Time+(ns)",
        "http://WEBVIEW_HOST_NAME:8080/#management;cn=Fake_Metrics_1_10_metrics;ct=96;dn=*SuperDomain*;mm=Clean_BofA_MM_24;tr=0",
        "http://WEBVIEW_HOST_NAME:8080/#management;cn=Sample_17_70_Dashboards;ct=8;dn=SuperDomain;mm=Clean_BofA_MM_17;tr=0",
    };
    private String[] ieUrls = {
        "http://WEBVIEW_HOST_NAME:8080/#console;db=Dashboard;mm=martins;tr=0",
        "http://WEBVIEW_HOST_NAME:8080/#console;db=S3_Perf_Test_25_Metrics_Per_Agent_Total_500_Metrics_20_Charts;dn=SuperDomain;mm=Clean_BofA_MM_13;tr=0",
        "http://WEBVIEW_HOST_NAME:8080/#console;db=S3_Perf_Test_25_Metrics_Per_Agent_Total_500_Metrics_20_Charts;dn=SuperDomain;mm=Clean_BofA_MM_14;tr=0",
        "http://WEBVIEW_HOST_NAME:8080/#console;db=S3_Perf_Test_25_Metrics_Per_Agent_Total_500_Metrics_20_Charts;dn=SuperDomain;mm=Clean_BofA_MM_15;tr=0",
        "http://WEBVIEW_HOST_NAME:8080/#console;db=S3_Perf_Test_25_Metrics_Per_Agent_Total_500_Metrics_20_Charts;dn=SuperDomain;mm=Clean_BofA_MM_17;tr=0",
        "http://WEBVIEW_HOST_NAME:8080/#console;db=S3_Perf_Test_25_Metrics_Per_Agent_Total_500_Metrics_20_Charts;dn=SuperDomain;mm=Clean_BofA_MM_18;tr=0",
        "http://WEBVIEW_HOST_NAME:8080/#console;db=S3_Perf_Test_25_Metrics_Per_Agent_Total_500_Metrics_20_Charts;dn=SuperDomain;mm=Clean_BofA_MM_4;tr=0",
        "http://WEBVIEW_HOST_NAME:8080/#console;db=S3_Perf_Test_25_Metrics_Per_Agent_Total_500_Metrics_20_Charts_No_Legend;dn=SuperDomain;mm=Clean_BofA_MM_16;tr=0",
        "http://WEBVIEW_HOST_NAME:8080/#console;db=S3_Perf_Test_25_Metrics_Per_Agent_Total_500_Metrics_20_Charts_No_Legend;dn=SuperDomain;mm=Clean_BofA_MM_17;tr=0",
        "http://WEBVIEW_HOST_NAME:8080/#console;db=S3_Perf_Test_25_Metrics_Per_Agent_Total_500_Metrics_20_Charts_No_Legend;dn=SuperDomain;mm=Clean_BofA_MM_18;tr=0",
        "http://WEBVIEW_HOST_NAME:8080/#console;db=S3_Perf_Test_25_Metrics_Per_Agent_Total_500_Metrics_20_Charts_No_Legend;dn=SuperDomain;mm=Clean_BofA_MM_19;tr=0",
        "http://WEBVIEW_HOST_NAME:8080/#investigator;tab-in=mb;tab-tv=pd;tr=0;uid=SuperDomain%257Cwurlitzer-stress-app%257CStressTestProcess%257CWurlitzerAgent_16%253AEM+Host",
        "http://WEBVIEW_HOST_NAME:8080/#investigator;tab-in=mb;tab-tv=pd;tr=0;uid=SuperDomain%257Cwurlitzer-stress-app%257CStressTestProcess%257CWurlitzerAgent_19%257CHeuristics%257CApps%257CNew_Apps_1%253AUser",
        "http://WEBVIEW_HOST_NAME:8080/#investigator;tab-in=mb;tab-tv=pd;tr=0;uid=SuperDomain%257Cwurlitzer-stress-app%257CStressTestProcess%257CWurlitzerAgent_28%257CFrontends%257CApps%257CNew_Apps_1%253AAverage+Response+Time+(ms)",
        "http://WEBVIEW_HOST_NAME:8080/#investigator;tab-in=mb;tab-tv=pd;tr=0;uid=SuperDomain%257Cwurlitzer-stress-app%257CStressTestProcess%257CWurlitzerAgent_7%257CAgent+Stats%257CSustainability%257CMemory+Stats%253ASocketMapCache",    
    };
    private String vwHost;
    private String hubHost;
    private ArrayList<RemoteWebDriver> drivers;
    private ArrayList<SeleniumInjectorClass> injectors;

    @Override
    protected String getLoadName() {
        return "webviewload";
    }

    @Override
    protected void startLoad() {
        LOGGER.info("WebViewLoadTest.runWebViewLoad():: entry");
        vwHost = envProperties.getMachineHostnameByRoleId(EM_WEBVIEW_ROLE_ID);
        hubHost = envProperties.getMachineHostnameByRoleId(SELENIUM_HUB_ROLE_ID);
        
        drivers = new ArrayList<>();
        injectors = new ArrayList<>();
        launchUrls("chrome", chromeUrls);
        launchUrls("ff", ffUrls);
        launchUrls("ie", ieUrls);
        
        
        
        LOGGER.info("WebViewLoadTest.runWebViewLoad():: exit");
    }

    @Override
    protected void stopLoad() {
        for (SeleniumInjectorClass injector: injectors) {
            try {
                injector.shutdown();
            } catch (Exception e) {
                LOGGER.warn("Unable to shutdown remote webdriver: " + e.getMessage());
            }
        }
        injectors.clear();
    }

    private void launchUrls(String browser, String[] urls) {
        String hubHostUrl = "http://" + hubHost + ":4444/wd/hub";
        for (String url: urls) {
            String link = url.replace("WEBVIEW_HOST_NAME", vwHost);
            SeleniumInjectorClass cc = new SeleniumInjectorClass(browser, hubHostUrl);
            injectors.add(cc);
            RemoteWebDriver driver = cc.executeRequest(link);
            drivers.add(driver);
        }
    }
}

class SeleniumInjectorClass {
    private final Logger LOGGER = LoggerFactory.getLogger(SeleniumInjectorClass.class);
    private boolean shutdown = false;

    RemoteWebDriver driver;
    
    public SeleniumInjectorClass(String driverType, String remoteAddress) {
        LOGGER.info("Using {} driver", driverType);

        boolean setTimeouts = true;
        DesiredCapabilities capabilities;

        switch (driverType) {
            case "ie": {
                capabilities = DesiredCapabilities.internetExplorer();
                break;
            }
            case "ff": {
                capabilities = DesiredCapabilities.firefox();
                capabilities.setCapability("marionette", false);
                break;
            }
            case "chrome": {
                capabilities = DesiredCapabilities.chrome();
                break;
            }
            default: {
                throw new IllegalArgumentException("Should be one of 'ie', 'ff', 'chrome'");
            }
        }

        try {
            driver = new RemoteWebDriver(new URL(remoteAddress), capabilities);
            driver.manage().timeouts().implicitlyWait(60, TimeUnit.SECONDS);
            if (setTimeouts) {
                // the IE driver crashes on this... grrr.
                driver.manage().timeouts().setScriptTimeout(-1, TimeUnit.SECONDS);
            }
        } catch (MalformedURLException ex) {
            LOGGER.error("Malformed HUB URL", ex);
        }
    }

    public RemoteWebDriver executeRequest(final String requestUrl) {
        LOGGER.info("Requesting URL start {}", requestUrl);
        try {
                driver.manage().window().maximize();
                driver.get(requestUrl);
                Thread.sleep(15000);

                WebElement loginField;

                try {
                    driver.switchTo().frame("LoginFrame");
                    loginField = driver.findElement(By.id("username"));
                } catch(NoSuchElementException ex)
                {
                    LOGGER.warn("LoginForm not found, using alternative search patterns");
                    driver.switchTo().frame("loginForm");
                    loginField = driver.findElement(By.name("j_username"));
                }

                loginField.sendKeys("Admin");
                loginField.submit();

                LOGGER.info("Requesting URL end {}", requestUrl);
        } catch (InterruptedException ex) {
            LOGGER.error("Interrupted", ex);
        }
        // We start a thread to make one call per minute to the remote webdriver,
        // which should keep it from closing after a five minute timeout
        Thread keepaliveThread = new Thread(new Runnable() {
            
            @Override
            public void run() {
                while (!shutdown) {
                    String url = driver.getCurrentUrl();
                    LOGGER.trace("The url is still " + url);
                    try {
                        Thread.sleep(60000L);
                    } catch (InterruptedException e) {
                    }
                }
                try {
                    driver.close();
                } catch (Exception e) {
                }
            }
        });
        keepaliveThread.setDaemon(true);
        keepaliveThread.start();

        return driver;
    }
    
    public void shutdown() {
        this.shutdown = true;
    }
}
