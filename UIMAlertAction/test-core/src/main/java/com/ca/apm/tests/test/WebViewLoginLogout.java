package com.ca.apm.tests.test;

import java.util.ArrayList;
import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.ElementNotVisibleException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ca.apm.tests.testbed.WindowsStandaloneTestbed;

public class WebViewLoginLogout extends WebDriverWrapper {
    static final Logger LOGGER = LoggerFactory.getLogger(WebViewLoginLogout.class);

    ArrayList<String> handleCount = null;
    String oldtab = null;

    public void loginToTeamCenter() {
        initializeEMandAgents();
        wait = new WebDriverWait(fd, 30);
        LOGGER.info("http://" + emHost + ":" + webviewPort + "/ApmServer/");
        fd.get("http://" + emHost + ":" + webviewPort + "/ApmServer/");

        fd.manage().window().maximize();
        LOGGER.info("Maximized the APMServer Window");
        oldtab = fd.getWindowHandle();
        final List<WebElement> iframes = fd.findElements(By.tagName("iframe"));
        for (WebElement iframe : iframes) {
            if ((iframe.getAttribute("id").length() > 0)) {
                LOGGER.info("Current Frame :" + iframe.getAttribute("id").toString());
            }
        }

        fd.switchTo().frame("LoginFrame");
        we = waitExplicitPresenceOfElement(LOGIN_USERNAME);
        we.sendKeys("admin");
        we = waitExplicitPresenceOfElement(LOGIN_LOGINBUTTON);
        we.click();
        we = waitExplicitPresenceOfElement(POPUP_CLOSE);
        we.click();
        LOGGER.info("Closed the popup and entering in to Webview");

    }

    public void moveToWebView() {
        LOGGER.info("Entering WebView ");
        try {
            String hostName =
                envProperties.getMachineHostnameByRoleId(WindowsStandaloneTestbed.EM_ROLE_ID);
            String WebViewURL = "http://" + hostName + ":" + webviewPort + "/#home;tr=0";
            fd.navigate().to(WebViewURL);

        } catch (ElementNotVisibleException e) {

            e.printStackTrace();
        }
    }
}
