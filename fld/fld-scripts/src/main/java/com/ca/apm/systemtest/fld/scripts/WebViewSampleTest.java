/**
 * 
 */
package com.ca.apm.systemtest.fld.scripts;

import java.util.HashMap;
import java.util.Map;

import org.openqa.selenium.By;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import com.ca.apm.systemtest.fld.plugin.selenium.ChromeSeleniumPlugin;
import com.ca.apm.systemtest.fld.plugin.selenium.SeleniumPlugin;
import com.ca.apm.systemtest.fld.plugin.selenium.SeleniumPluginException;
import com.ca.apm.systemtest.fld.plugin.selenium.SeleniumTest;

/**
 * This sample selenium test logs into a webview server, and navigates to the "Management" tab.  After
 * one minute, it exits and closes the browser.  It can be executed as a standalone program by running the main
 * method with arguments, or used by the new FLD automation.
 *  
 * @author KEYJA01
 *
 */
public class WebViewSampleTest implements SeleniumTest {
    public static final String PASSWORD = "password";
    public static final String USERNAME = "username";
    public static final String WEBVIEW_SERVER_URL = "webview_server";
    private static final Object LOCK = new Object();;
    private ThreadLocal<Map<String, String>> paramMap = new ThreadLocal<Map<String, String>>() {
        @Override
        protected Map<String, String> initialValue() {
            return new HashMap<String, String>();
        }
    };
    private boolean stop = false;

    /* (non-Javadoc)
     * @see com.ca.apm.systemtest.fld.plugin.selenium.SeleniumTest#executeSeleniumScript(com.ca.apm.systemtest.fld.plugin.selenium.SeleniumPlugin, java.util.Map)
     */
    @Override
    public void executeSeleniumScript(SeleniumPlugin plugin, Map<String, String> params)
        throws SeleniumPluginException {
        paramMap.set(params);
        
        WebDriver driver = null;
        String sessionId = null;
        
        try {
            sessionId = plugin.startSession();
            driver = plugin.webDriver(sessionId);
            
            boolean loggedIn = login(driver);
            if (!loggedIn) {
                System.out.println("Not logged in, exiting test");
                return;
            }
            
            if (stop) {
                return;
            }
            
            System.out.println("Looking for the management span");
            
            // yes, this is a really hacky way to find the <a> for the management tab in the application
            // but the generated HTML elements don't have predictable CSS class names or ids
            // so we find the <a> link higher up in the DOM that contains the Management tab.
            WebElement managementTabLink = driver.findElement(By.xpath("//span[text()='Management']/ancestor::a"));
            
            shortWait(1500L);
            if (stop) {
                return;
            }
            
            
            managementTabLink.click();
            
            System.out.println("We just clicked on the management tab");
            
            while (!stop) {
                shortWait(1000L);
            }
            System.out.println("Test is now concluded");
            
        } finally {
            if (sessionId != null) {
                plugin.closeSession(sessionId);
            }
        }
    }
    
    
    
    @Override
    public void shouldStop() {
        this.stop = true;
    }



    /**
     * Attempts to log the user into a new session
     * @param driver
     * @param params
     * @return
     */
    private boolean login(WebDriver driver) {
        Map<String, String> params = paramMap.get();
        // open up a new browser window to the webview URL
        driver.get(params.get(WEBVIEW_SERVER_URL));
        WebElement loginFrame = null;
        
        long startTime = System.currentTimeMillis();
        long elapsed = System.currentTimeMillis() - startTime;
        while (loginFrame == null && elapsed < 30000L) {
            loginFrame = driver.findElement(By.id("LoginFrame"));
            if (loginFrame == null) {
                shortWait(1000L);
            }
            elapsed = System.currentTimeMillis() - startTime;
        }
        System.out.println(loginFrame);
        if (loginFrame == null) {
            // TODO log error here
            return false;
        }
        
        // let GWT finish loading
        shortWait(5000L);
        
        // switch to the iframe login page
        driver.switchTo().frame(0);
        
        // find the login form elements
        WebElement usernameInput = findElementById(driver, USERNAME);
        WebElement passwordInput = findElementById(driver, "j_passWord");
        WebElement button = findElementById(driver, "webview-loginPage-login-button");
        
        // send the username and password using the form
        usernameInput.sendKeys(params.get(USERNAME));
        passwordInput.sendKeys(params.get(PASSWORD));
        button.click();
        
        WebElement div = null;
        startTime = System.currentTimeMillis();
        elapsed = 0L;
        while (div == null && elapsed < 60000L) {
            div = findElementByClassName(driver, "webviewViewport");
            shortWait(1000L);
            elapsed = System.currentTimeMillis() - startTime;
        }
        
        System.out.println(div);
        
        // if the webviewViewport div is present, then we likely logged in successfully
        return (div != null);
    }
    
    
    
    private static void shortWait(long ms) {
        synchronized (LOCK) {
            try {
                LOCK.wait(ms);
            } catch (InterruptedException e) {
            }
        }
    }



    private WebElement findElementById(SearchContext driver, String id) {
        WebElement e = null;
        try {
            e = driver.findElement(By.id(id));
        } catch (Exception ex) {
            // ignore
        }
        
        return e;
    }


    private WebElement findElementByClassName(SearchContext driver, String className) {
        WebElement e = null;
        try {
            e = driver.findElement(By.className(className));
        } catch (Exception ex) {
            // ignore
        }
        
        return e;
    }

    
    public static void main(String[] args) throws Exception {
        
        if (args.length != 3) {
            System.out.println("Usage: java " + WebViewSampleTest.class.getCanonicalName() + " <webview url> <username> <password>");
            return;
        }
        
        final HashMap<String, String> params = new HashMap<String, String>();
        params.put(WEBVIEW_SERVER_URL, args[0]);
        params.put(USERNAME, args[1]);
        params.put(PASSWORD, args[2]);
        
        final ChromeSeleniumPlugin plugin = new ChromeSeleniumPlugin();
        final WebViewSampleTest test = new WebViewSampleTest();
        // start the test in a new thread
        Thread thread = new Thread(new Runnable() {
            
            @Override
            public void run() {
                try {
                    test.executeSeleniumScript(plugin, params);
                } catch (SeleniumPluginException e) {
                    e.printStackTrace();
                }
            }
        });
        thread.start();
        
        
        // wait for one minute
        for (int i = 60; i > 0; i--) {
            System.out.println(i);
            synchronized (plugin) {
                plugin.wait(1000L);
            }
        }
        
        // then signal the test to stop
        test.shouldStop();
    }
    
}
