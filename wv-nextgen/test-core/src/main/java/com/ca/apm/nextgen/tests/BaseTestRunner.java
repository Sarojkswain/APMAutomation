package com.ca.apm.nextgen.tests;

import org.openqa.selenium.remote.DesiredCapabilities;

import com.ca.apm.nextgen.tests.helpers.WebViewUi;

/**
 * Basic class for test runner implementors.  
 * 
 * @author Alexander Sinyushkin (sinal04@ca.com)
 */
public class BaseTestRunner {
    protected String webViewUserName;
    protected String webViewUserPassword;
    protected String webViewHost;
    protected String hubHost;
    protected String hubPath;
    protected int hubPort;
    protected int webViewPort;
    protected DesiredCapabilities desiredCapabilities;
    
    public BaseTestRunner(String webViewUserName, String webViewUserPassword, String webViewHost,
        int webViewPort, String hubHost, String hubPath, int hubPort, DesiredCapabilities desiredCapabilities) {
        this.webViewUserName = webViewUserName;
        this.webViewUserPassword = webViewUserPassword;
        this.webViewHost = webViewHost;
        this.webViewPort = webViewPort;
        this.hubHost = hubHost;
        this.hubPath = hubPath;
        this.hubPort = hubPort;
        this.desiredCapabilities = desiredCapabilities;
    }
    
    /**
     * Creates a new helper instance simplifying work with WebView using Selenium WebDriver. 
     * 
     * @return
     */
    protected WebViewUi createWebViewUi() {
        return WebViewUi.create(WebViewUi.HUB_PROTOCOL, hubHost, hubPort, hubPath, desiredCapabilities);
    }

    /**
     * Logs into WebView using provided credentials. 
     * 
     * @param    ui
     * @return
     */
    protected WebViewUi login(WebViewUi ui) {
        ui.login(webViewHost, webViewPort, webViewUserName, webViewUserPassword);
        return ui;
    }
}
