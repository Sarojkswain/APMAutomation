package com.ca.apm.systemtest.fld.plugin.selenium;

import io.github.bonigarcia.wdm.InternetExplorerDriverManager;

import java.util.concurrent.Callable;
import java.util.concurrent.Future;

import javax.annotation.PostConstruct;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.ie.InternetExplorerDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.core.task.AsyncTaskExecutor;

import com.ca.apm.systemtest.fld.common.PluginAnnotationComponent;
import com.ca.apm.systemtest.fld.plugin.annotations.ExposeMethod;
import com.ca.apm.systemtest.fld.plugin.util.SystemUtil;
import com.ca.apm.systemtest.fld.plugin.util.SystemUtil.OperatingSystemFamily;

@PluginAnnotationComponent(pluginType = "seleniumPluginIE")
public class IESeleniumPlugin extends SeleniumPluginAbs implements SeleniumPlugin {
    private static final Logger log = LoggerFactory.getLogger(IESeleniumPlugin.class);

    @Autowired
    ApplicationContext applicationContext;
    Future<String> webDriverVersion;

    public IESeleniumPlugin() {
    }

    @PostConstruct
    void initialize() {
        if (SystemUtil.getOsFamily() == OperatingSystemFamily.Windows) {
            AsyncTaskExecutor asyncTaskExecutor = (AsyncTaskExecutor) applicationContext
                .getBean("agentTaskExecutor");
            webDriverVersion = asyncTaskExecutor.submit(new Callable<String>() {
                @Override
                public String call() throws Exception {
                    return procureWebDriver(InternetExplorerDriverManager.class);
                }
            });
        }
    }

    @ExposeMethod(description = "Starts a IE browser session.")
    public String startSession() throws SeleniumPluginException {
        waitForWebDriverDownload(webDriverVersion, log, InternetExplorerDriverManager.class);
        WebDriver driver = new InternetExplorerDriver();
        return super.startSession(driver, "ie-");
    }
}
