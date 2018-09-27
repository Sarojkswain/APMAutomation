package com.ca.apm.systemtest.fld.test.devel;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

import com.ca.apm.systemtest.fld.flow.ATCUI;
import com.ca.apm.systemtest.fld.flow.ConfigureATCUILoadFlowContext;
import com.ca.apm.systemtest.fld.role.ATCUISetLoadRole;
import com.ca.apm.systemtest.fld.test.loads.BaseFldLoadTest;
import com.ca.apm.systemtest.fld.testbed.FLDLoadConstants;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author filja01
 */
public class ATCUILoadTest extends BaseFldLoadTest {
    private final Logger log = LoggerFactory.getLogger(ATCUILoadTest.class);
    private volatile boolean stop = false;
    
    public static final String WEBVIEW_URL = "fldcoll11c.ca.com";
    public static final int WEBVIEW_PORT = 8080;
    public static final String PASSWORD = "";
    public static final String USER = "admin";
    
    public static final long TEST_DURATION = 500000L;
    public static final int NUMBER_OF_BROWSERS = 3;
    

    @Override
    protected String getLoadName() {
        return "ATCUILoad";
    }


    @Override
    protected void startLoad() {
        final ConfigureATCUILoadFlowContext ctx = deserializeFlowContextFromRole(
            FLDLoadConstants.ATCUI_SET_LOAD_ROLE_ID,
            ATCUISetLoadRole.ATCUI_SET_LOAD_FLOW_CTX_KEY,
            ConfigureATCUILoadFlowContext.class);

        final String webviewHost;
        if (ctx.getWebviewHost() == null || ctx.getWebviewHost().isEmpty()) {
            webviewHost = WEBVIEW_URL;
        } else {
            webviewHost = ctx.getWebviewHost();
        }
        final Integer webviewPort;
        if (ctx.getWebviewPort() == null || ctx.getWebviewPort().isEmpty()) {
            webviewPort = WEBVIEW_PORT;
            log.error("WebviewPort is: {}", ctx.getWebviewPort());
        } else {
            webviewPort = Integer.valueOf(ctx.getWebviewPort());
            log.info("WebviewPort is: {}", ctx.getWebviewPort());
        }
        
        final String password;
        if (ctx.getPassword() == null) {
            password = PASSWORD;
        } else {
            password = ctx.getPassword();
        }
        final String user;
        if (ctx.getUser() == null || ctx.getUser().isEmpty()) {
            user = USER;
        } else {
            user = ctx.getUser();
        }
        Integer numberOfBrowsers;
        if (ctx.getNumberOfBrowsers() == null) {
            numberOfBrowsers = NUMBER_OF_BROWSERS;
        } else {
            numberOfBrowsers = ctx.getNumberOfBrowsers();
        }
        
        //wait 2 minutes for start
        //ATCUI.sleep(120000L);
        
        String hubHost = envProperties.getMachineHostnameByRoleId(SELENIUM_ATCUI_HUB_ROLE_ID);
        final String hubHostUrl = "http://" + hubHost + ":4444/wd/hub";
        
        ArrayList<Thread> threads = new ArrayList<>();
            
        for (int i = 0; i < numberOfBrowsers; i++) {
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
                        runTestHomepageAT(driver, webviewHost, webviewPort, user, password, threadName);
                        log.info("Close connection on {} thread", threadName);
                        driver.close();
                    }
                }
            });
            thFF.start();
            threads.add(thFF);
            
            ATCUI.sleep(10000L);//start threads every 10s
        }
        
        for (int i = 0; i < numberOfBrowsers; i++) {
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
                        runTestHomepageAT(driver, webviewHost, webviewPort, user, password, threadName);
                        log.info("Close connection on {} thread", threadName);
                        driver.close();
                    }
                }
            });
            thIE.start();
            threads.add(thIE);
            
            ATCUI.sleep(10000L);//start threads every 10s
        }
        
        for (int i = 0; i < numberOfBrowsers; i++) {
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
                        runTestHomepageAT(driver, webviewHost, webviewPort, user, password, threadName);
                        log.info("Close connection on {} thread", threadName);
                        driver.close();
                    }
                }
            });
            thCH.start();
            threads.add(thCH);
            
            ATCUI.sleep(10000L);//start threads every 10s
        }
    }

    @Override
    protected void stopLoad() {
        stop = true;
        log.info("Wait for test end");
        ATCUI.sleep(60000L); //wait for closing of threads
        log.info("Test end");
    }
    
    private void runTestHomepageAT(WebDriver driver, String host, int port, String user, String password, String threadName) {
        log.info("{}: Run AT test", threadName);
        driver.manage().window().maximize(); // Always maximize on windows
        ATCUI.sleep(2000);
        ATCUI atcUI = new ATCUI(host, port, user, password, driver);
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
