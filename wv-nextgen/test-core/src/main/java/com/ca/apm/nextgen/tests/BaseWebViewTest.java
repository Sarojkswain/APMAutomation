package com.ca.apm.nextgen.tests;

import java.util.concurrent.TimeUnit;

import org.openqa.selenium.remote.DesiredCapabilities;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.BeforeMethod;

import com.ca.apm.nextgen.WvNextgenTestbedNoCoda;
import com.ca.apm.nextgen.tests.helpers.ErrorReport;
import com.ca.apm.nextgen.tests.helpers.WebViewUi;
import com.ca.tas.test.TasTestNgTest;

import static com.ca.apm.nextgen.WvNextgenTestbedNoCoda.EM_ROLE;
import static com.ca.apm.nextgen.tests.helpers.SeleniumHelpers.prepCapabilities;

public abstract class BaseWebViewTest extends TasTestNgTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(BaseWebViewTest.class);

    protected DesiredCapabilities prepareDesiredCapabilities() {
        final String browser = envProperties.getTestbedPropertyById("browser");
        LOGGER.info("testbed property browser: {}", browser);
        return prepCapabilities(browser);
    }

    @BeforeMethod
    public void before() throws InterruptedException {
        LOGGER
            .info("Waiting a bit before the test will be started to try to avoid issues with WebView "
                + "startup.");
        sleep(30);
    }

    protected WebViewUi getWvUi() {
        String hubHost =
            this.envProperties
                .getMachineHostnameByRoleId(WvNextgenTestbedNoCoda.SELENIUM_HUB_ROLE_ID);
        LOGGER.debug("Selenium Grid hub at {}", hubHost);
        return WebViewUi.create(hubHost, prepareDesiredCapabilities());
    }

    protected String getWvUrl() {
        String webViewHost = this.envProperties.getMachineHostnameByRoleId(EM_ROLE);
        return "http://" + webViewHost + ":8080/#home;tr=0";
    }

    protected String getWvLoginUrl() {
        String webViewHost = this.envProperties.getMachineHostnameByRoleId(EM_ROLE);
        return "http://" + webViewHost + ":8080/jsp/login.jsf";
    }

    protected void sleep(long millis) {
        try {
            TimeUnit.MILLISECONDS.sleep(millis);
        } catch (InterruptedException e) {
            LOGGER.warn("Cannot sleep for {} millis. Interrupted.", millis);
            throw ErrorReport.logExceptionAndWrapFmt(LOGGER, e, "Exception: {0}");
        }
    }

}
