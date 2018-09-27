package com.ca.apm.systemtest.fld.plugin.selenium;

import io.github.bonigarcia.wdm.EdgeDriverManager;

import java.util.concurrent.Callable;
import java.util.concurrent.Future;

import javax.annotation.PostConstruct;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.edge.EdgeDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.core.task.AsyncTaskExecutor;

import com.ca.apm.systemtest.fld.common.ErrorUtils;
import com.ca.apm.systemtest.fld.common.PluginAnnotationComponent;
import com.ca.apm.systemtest.fld.plugin.annotations.ExposeMethod;
import com.ca.apm.systemtest.fld.plugin.util.SystemUtil;
import com.ca.apm.systemtest.fld.plugin.util.SystemUtil.OperatingSystemFamily;

@PluginAnnotationComponent(pluginType = "seleniumPluginEdge")
public class EdgeSeleniumPlugin extends SeleniumPluginAbs implements SeleniumPlugin {

    private static final Logger log = LoggerFactory.getLogger(EdgeSeleniumPlugin.class);
    @Autowired
    ApplicationContext applicationContext;
    Future<String> webDriverVersion;

    public EdgeSeleniumPlugin() {
    }

    @PostConstruct
    void initialize() {
        if (SystemUtil.getOsFamily() == OperatingSystemFamily.Windows) {
            AsyncTaskExecutor asyncTaskExecutor = (AsyncTaskExecutor) applicationContext
                .getBean("agentTaskExecutor");
            webDriverVersion = asyncTaskExecutor.submit(new Callable<String>() {
                @Override
                public String call() throws Exception {
                    // Use fixed version of EdgeWebDriver because the underlying
                    // code is not able to get newer version automatically anyway.
                    return procureWebDriver(EdgeDriverManager.class,
                        "8D0D08CF-790D-4586-B726-C6469A9ED49C");
                }
            });
        }
    }

    @ExposeMethod(description = "Starts a Edge browser session.")
    public String startSession() throws SeleniumPluginException {
        waitForWebDriverDownload(webDriverVersion, log, EdgeDriverManager.class);
        WebDriver driver;
        try {
            driver = new EdgeDriver();
        } catch (Exception e) {
            throw new SeleniumPluginException(
                ErrorUtils.logExceptionFmt(log, e,
                    "Failed to create Edge driver. Exception: {0}"));
        }

        try {
            return super.startSession(driver, "edge-");
        } catch (Exception e) {
            throw new SeleniumPluginException(
                ErrorUtils.logExceptionFmt(log, e,
                    "Failed to start Selenium plugin session. Exception: {0}"));
        }
    }
}
