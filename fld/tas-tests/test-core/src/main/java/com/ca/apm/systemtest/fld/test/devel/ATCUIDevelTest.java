package com.ca.apm.systemtest.fld.test.devel;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

import com.ca.apm.systemtest.fld.flow.ATCUI;
import com.ca.apm.systemtest.fld.testbed.devel.ATCUITestbed;
import com.ca.tas.test.TasTestNgTest;
import com.ca.tas.tests.annotations.Tas;
import com.ca.tas.tests.annotations.TestBed;
import com.ca.tas.type.SizeType;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;

import static com.ca.apm.systemtest.fld.testbed.FLDLoadConstants.SELENIUM_ATCUI_HUB_ROLE_ID;
import static com.ca.apm.systemtest.fld.testbed.FLDLoadConstants.SELENIUM_ATCUI_HUB_MACHINE_ID;

/**
 * @author filja01
 */
public class ATCUIDevelTest extends TasTestNgTest {
    private final Logger log = LoggerFactory.getLogger(ATCUIDevelTest.class);
    private volatile boolean stop = false;
    
    public static final String WEBVIEW_URL = "fldcoll11.ca.com";
    public static final Integer WEBVIEW_PORT = 8080;
    
    public static final long TEST_DURATION = 500000L;
    public static final int NUMBER_OF_BROWSERS = 3;
    
    @Test(groups = {"windows"})
    @Tas(testBeds = @TestBed(name = ATCUITestbed.class, executeOn = SELENIUM_ATCUI_HUB_MACHINE_ID),
        owner = "filja01", size = SizeType.MEDIUM)
    public void test() {
        
        String hubHost = envProperties.getMachineHostnameByRoleId(SELENIUM_ATCUI_HUB_ROLE_ID);
        final String hubHostUrl = "http://" + hubHost + ":4444/wd/hub";
        
        ArrayList<Thread> threads = new ArrayList<>();
            
        for (int i = 0; i < NUMBER_OF_BROWSERS; i++) {
            final int c = i;
            final String threadName = "FF_"+c;
            Thread thFF = new Thread(new Runnable() {
                @Override
                public void run() {
                    log.info("Run {} thread", threadName);
                    RemoteWebDriver driver = null;
                    try {
                        driver = new RemoteWebDriver(new URL(hubHostUrl), DesiredCapabilities.firefox());
                    } catch (MalformedURLException ex) {
                        log.error("Malformed HUB URL", ex);
                    }
                    if (driver != null) {
                        runTestHomepageAT(driver, WEBVIEW_URL, WEBVIEW_PORT, threadName);
                        driver.close();
                    }
                }
            });
            thFF.start();
            threads.add(thFF);
            
            ATCUI.sleep(10000L);//start threads every 10s
        }
        
        for (int i = 0; i < NUMBER_OF_BROWSERS; i++) {
            final int c = i;
            final String threadName = "IE_"+c;
            Thread thIE = new Thread(new Runnable() {
                @Override
                public void run() {
                    log.info("Run {} thread", threadName);
                    RemoteWebDriver driver = null;
                    try {
                        driver = new RemoteWebDriver(new URL(hubHostUrl), DesiredCapabilities.internetExplorer());
                    } catch (MalformedURLException ex) {
                        log.error("Malformed HUB URL", ex);
                    }
                    if (driver != null) {
                        runTestHomepageAT(driver, WEBVIEW_URL, WEBVIEW_PORT, threadName);
                        driver.close();
                    }
                }
            });
            thIE.start();
            threads.add(thIE);
            
            ATCUI.sleep(10000L);//start threads every 10s
        }
        
        for (int i = 0; i < NUMBER_OF_BROWSERS; i++) {
            final int c = i;
            final String threadName = "CH_"+c;
            Thread thCH = new Thread(new Runnable() {
                @Override
                public void run() {
                    log.info("Run {} thread", threadName);
                    RemoteWebDriver driver = null;
                    try {
                        driver = new RemoteWebDriver(new URL(hubHostUrl), DesiredCapabilities.chrome());
                    } catch (MalformedURLException ex) {
                        log.error("Malformed HUB URL", ex);
                    }
                    if (driver != null) {
                        runTestHomepageAT(driver, WEBVIEW_URL, WEBVIEW_PORT, threadName);
                        driver.close();
                    }
                }
            });
            thCH.start();
            threads.add(thCH);
            
            ATCUI.sleep(10000L);//start threads every 10s
        }
        
        ATCUI.sleep(TEST_DURATION);
        stop = true;
        log.info("Wait for test end");
        ATCUI.sleep(30000L); //wait for closing of threads
        log.info("Test end");
    }


    private void runTestHomepageAT(WebDriver driver, String host, int port, String threadName) {
        log.info("{}: Run AT test", threadName);
        driver.manage().window().maximize(); // Always maximize on windows
        ATCUI.sleep(2000);
        ATCUI atcUI = new ATCUI(host, port, "admin", "", driver);
        atcUI.login();
        ATCUI.sleep(3000); //wait after login
        atcUI.setSomeLiveRange(threadName);
        ATCUI.sleep(4000);
        // run clicking loop on Homepage with Assisted Triage  
        while (true) {
            // click on some Experience
            //atcUI.clickOnExperienceFromDropdown(threadName);
            atcUI.clickOnSomeExperienceTile(threadName);
            // expand AT Panel and select some problem
            atcUI.selectSomeProblem(threadName);
            ATCUI.sleep(5000);
            // open notebook in expanded problem
            atcUI.openNotebookInExpandedProblem(threadName);
            ATCUI.sleep(5000);
            // click on some evidence, eventually open problem (if no is opened) and click notebook
            atcUI.clickOnSomeEvidenceInAT(threadName);
            ATCUI.sleep(5000);
            // go to the homepage wit All Experiences 
            atcUI.clickOnHomeExperience(threadName);
            
            // small wait after each loop
            ATCUI.sleep(7000);
            
            if(stop) {
                break;
            }
        }
    }
}
