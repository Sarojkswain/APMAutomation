package com.ca.apm.systemtest.fld.plugin.selenium;

import java.nio.file.Paths;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.phantomjs.PhantomJSDriver;
import org.openqa.selenium.phantomjs.PhantomJSDriverService;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ca.apm.systemtest.fld.common.ErrorUtils;
import com.ca.apm.systemtest.fld.common.PluginAnnotationComponent;
import com.ca.apm.systemtest.fld.plugin.annotations.ExposeMethod;

@PluginAnnotationComponent(pluginType = "seleniumPluginPhantomJs")
public class PhantomJsSeleniumPlugin extends SeleniumPluginAbs implements SeleniumPlugin {
    private static final Logger log = LoggerFactory.getLogger(PhantomJsSeleniumPlugin.class);

    public PhantomJsSeleniumPlugin() {
    }

    public static void main(String[] args) throws Exception {

    }

    @ExposeMethod(description = "Starts a PhantomJS pseudo-browser session.")
    public String startSession() throws SeleniumPluginException {
        try {
            PhantomJSDriverService service = new PhantomJSDriverService.Builder()
                .usingAnyFreePort()
                .usingPhantomJSExecutable(
                    Paths.get("agent-current", "bin", "phantomjs.exe").toFile())
                .usingCommandLineArguments(new String[]{
                    "--ignore-ssl-errors=true", "--ssl-protocol=tlsv1", "--web-security=false",
                    "--webdriver-loglevel=INFO"})
                .build();
            DesiredCapabilities desireCaps = new DesiredCapabilities();
            WebDriver driver = new PhantomJSDriver(service, desireCaps);
            return super.startSession(driver, "phantomjs-");
        } catch (Exception e) {
            throw new SeleniumPluginException(ErrorUtils.logExceptionFmt(log, e,
                "Failed to start session of PhantomJsSeleniumPlugin. Exception: {0}"), e);
        }
    }

}
