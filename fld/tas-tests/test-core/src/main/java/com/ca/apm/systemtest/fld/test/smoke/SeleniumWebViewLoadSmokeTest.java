package com.ca.apm.systemtest.fld.test.smoke;

import static com.ca.apm.systemtest.fld.testbed.FLDLoadConstants.SELENIUM_HUB_MACHINE_ID;
import static com.ca.apm.systemtest.fld.testbed.FLDLoadConstants.SELENIUM_HUB_ROLE_ID;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.TimeUnit;

import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;

import com.ca.apm.systemtest.fld.testbed.smoke.SeleniumWebViewLoadTestBed;
import com.ca.tas.test.TasTestNgTest;
import com.ca.tas.tests.annotations.Tas;
import com.ca.tas.tests.annotations.TestBed;
import com.ca.tas.type.SizeType;

/**
 * Unit test, that opens browsers with WV on machines, using Selenium Grid.
 * @author shadm01
 */

@Tas(testBeds = {@TestBed(name = SeleniumWebViewLoadTestBed.class, executeOn = SELENIUM_HUB_MACHINE_ID)}, size = SizeType.BIG)
@Test
public class SeleniumWebViewLoadSmokeTest extends TasTestNgTest {
/*
 */
    private final Logger LOGGER = LoggerFactory.getLogger(SeleniumWebViewLoadSmokeTest.class);
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
    private Queue<Duplet> queue = new LinkedList<>(); 
    private boolean testDone = false;
    
    private static class Duplet {
        private String browser;
        private String url;
    }
    
    private class LoadRunner implements Runnable {
        @Override
        public void run() {
            Duplet duplet = null;
            
            while (!testDone){
                synchronized (queue) {
                    duplet = queue.poll();
                    if (duplet == null) {
                        try {
                            queue.wait(10000L);
                        } catch (InterruptedException e) {
                        }
                        continue;
                    }
                }
                String hubHostUrl = "http://" + hubHost + ":4444/wd/hub";
                String link = duplet.url.replace("WEBVIEW_HOST_NAME", vwHost);
                SeleniumInjectorClass cc = new SeleniumInjectorClass(duplet.browser, hubHostUrl);
                RemoteWebDriver driver = cc.executeRequest(link);
                drivers.add(driver);
            }
        }
        
    }

    public void runTest() throws Exception {
        vwHost = envProperties.getMachineHostnameByRoleId("emMomRole"); //TODO - replace with VW
        hubHost = envProperties.getMachineHostnameByRoleId(SELENIUM_HUB_ROLE_ID);
//        hubHost = envProperties.getMachineHostnameByRoleId(FLDLoadConstants.WEBVIEW_LOAD_01_MACHINE_ID + "_seleniumNodeRole");

        LOGGER.info("VW host is : {}", vwHost);
        LOGGER.info("HUB host is : {}", hubHost);

        drivers = new ArrayList<>();
        launchUrls("chrome", chromeUrls);
        launchUrls("ff", ffUrls);
        launchUrls("ie", ieUrls);
        
        for (int i = 0; i < 3; i++) {
            Thread th = new Thread(new LoadRunner());
            th.start();
        }

        LOGGER.info("All webview sessions started - waiting 15 min to kill");

        Thread.sleep(900000L);
        
        LOGGER.info("Done waiting - will kill now");
        
        testDone = true;

        for (RemoteWebDriver driver : drivers) {
            try {
                LOGGER.info("Killing " + driver);
                driver.close();
            } catch (Exception e) {
                LOGGER.warn("Unable to close driver: " + driver + ", message: " + e.getMessage());
            }
        }
        
    }


    private void launchUrls(String browser, String[] urls) {
        boolean useThreads = true;
        String hubHostUrl = "http://" + hubHost + ":4444/wd/hub";
        for (String url: urls) {
            String link = url.replace("WEBVIEW_HOST_NAME", vwHost);
            if (useThreads) {
                Duplet d = new Duplet();
                d.browser = browser;
                d.url = link;
                queue.add(d);
            } else {
                SeleniumInjectorClass cc = new SeleniumInjectorClass(browser, hubHostUrl);
                RemoteWebDriver driver = cc.executeRequest(link);
                drivers.add(driver);
            }
        }
    }
}


class SeleniumInjectorClass {
    private final Logger LOGGER = LoggerFactory.getLogger(SeleniumInjectorClass.class);

    RemoteWebDriver driver;

    public SeleniumInjectorClass(String driverType, String remoteAddress) {
        LOGGER.info("Using {} driver", driverType);

        boolean setTimeouts = true;
        DesiredCapabilities capabilities;

        switch (driverType) {
            case "ie": {
                setTimeouts = false;
                capabilities = DesiredCapabilities.internetExplorer();
                break;
            }
            case "ff": {
                capabilities = DesiredCapabilities.firefox();
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
                driver.manage().timeouts().setScriptTimeout(28, TimeUnit.DAYS);
            }
        } catch (MalformedURLException ex) {
            LOGGER.error("Malformed HUB URL", ex);
        }
    }

    public RemoteWebDriver executeRequest(final String requestUrl) {
        LOGGER.info("Requesting URL start {}", requestUrl);
        try {
                driver.manage().window().maximize(); // Always maximize firefox on windows
                driver.get(requestUrl);
                Thread.sleep(5000); //TODO - investigate if we need to wait for page to load

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

                //TODO - investigate if sessions are auto-closed

                LOGGER.info("Requesting URL end {}", requestUrl);
        } catch (InterruptedException ex) {
            LOGGER.error("Interrupted", ex);
        }
        Thread keepaliveThread = new Thread(new Runnable() {
            
            @Override
            public void run() {
                boolean done = false;
                while (!done) {
                    String url = driver.getCurrentUrl();
                    LOGGER.trace("The url is still " + url);
                    try {
                        Thread.sleep(60000L);
                    } catch (InterruptedException e) {
                    }
                }
            }
        });
        keepaliveThread.setDaemon(true);
        keepaliveThread.start();

        return driver;
    }
}



