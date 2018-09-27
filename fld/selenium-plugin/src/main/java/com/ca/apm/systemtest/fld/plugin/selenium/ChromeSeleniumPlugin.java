package com.ca.apm.systemtest.fld.plugin.selenium;

import io.github.bonigarcia.wdm.ChromeDriverManager;

import java.util.concurrent.Callable;
import java.util.concurrent.Future;

import javax.annotation.PostConstruct;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.core.task.AsyncTaskExecutor;

import com.ca.apm.systemtest.fld.common.PluginAnnotationComponent;
import com.ca.apm.systemtest.fld.plugin.annotations.ExposeMethod;

@PluginAnnotationComponent(pluginType = "seleniumPluginChrome")
public class ChromeSeleniumPlugin extends SeleniumPluginAbs implements SeleniumPlugin {
    Logger log = LoggerFactory.getLogger(ChromeSeleniumPlugin.class);

    @Autowired
    ApplicationContext applicationContext;
    Future<String> webDriverVersion;

    public ChromeSeleniumPlugin() {
    }

    @PostConstruct
    void initialize() {
        AsyncTaskExecutor asyncTaskExecutor = (AsyncTaskExecutor) applicationContext
            .getBean("agentTaskExecutor");
        webDriverVersion = asyncTaskExecutor.submit(new Callable<String>() {
            @Override
            public String call() throws Exception {
                return procureWebDriver(ChromeDriverManager.class);
            }
        });
    }

    @ExposeMethod(description = "Starts a Chrome browser session.")
    public String startSession() throws SeleniumPluginException {
        waitForWebDriverDownload(webDriverVersion, log, ChromeDriverManager.class);
        WebDriver driver = new ChromeDriver();
        return super.startSession(driver, "chrome-");
    }
}
