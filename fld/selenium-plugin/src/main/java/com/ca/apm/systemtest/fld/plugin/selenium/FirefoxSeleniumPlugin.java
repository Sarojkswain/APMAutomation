package com.ca.apm.systemtest.fld.plugin.selenium;

import java.util.List;
import java.util.Random;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxProfile;

import com.ca.apm.systemtest.fld.common.PluginAnnotationComponent;
import com.ca.apm.systemtest.fld.plugin.annotations.ExposeMethod;

@PluginAnnotationComponent(pluginType = "seleniumPluginFirefox")
public class FirefoxSeleniumPlugin extends SeleniumPluginAbs implements SeleniumPlugin {

    public FirefoxSeleniumPlugin() {}

    @ExposeMethod(description = "Starts a Firefox browser session.")
    public String startSession() throws SeleniumPluginException {
        FirefoxProfile fp = new FirefoxProfile();
        fp.setPreference("webdriver.load.strategy", "unstable");
        WebDriver driver = new FirefoxDriver(fp);
        return super.startSession(driver, "firefox-");
    }

    public static void main(String[] args) throws Exception {
        SeleniumPlugin plugin = new FirefoxSeleniumPlugin();
        //
        String session = plugin.startSession();
        System.out.println("Session " + session);

        try {
            String handle = plugin.openUrl(session, "http://ca.com");
            System.out.println("Handle " + handle);

            synchronized (plugin) {
                plugin.wait(20000);
            }

            List<String> links = plugin.findLinks(session, handle, "articles/mobility");
            System.out.println("Found " + links.size() + " links");

            //
            Random rand = new Random();
            String link;
            if (!links.isEmpty()) {
                link = links.get(rand.nextInt(links.size()));
                boolean isOpenedLink = plugin.openLink(session, handle, link);
                System.out.println(isOpenedLink + ", " + link);
            }
            //
            synchronized (plugin) {
                plugin.wait(20000);
            }
        } finally {
            plugin.closeSession(session);
        }
        System.out.println("Leaving now");
    }

}
