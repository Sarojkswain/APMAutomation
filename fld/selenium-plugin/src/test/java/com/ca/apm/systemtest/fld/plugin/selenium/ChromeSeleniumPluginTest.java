package com.ca.apm.systemtest.fld.plugin.selenium;

import java.util.List;
import java.util.Random;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by haiva01 on 15.10.2015.
 */
public class ChromeSeleniumPluginTest {
    Logger log = LoggerFactory.getLogger(ChromeSeleniumPluginTest.class);

    //@Test
    public void testChrome() throws SeleniumPluginException, InterruptedException {
        SeleniumPlugin plugin = new ChromeSeleniumPlugin();
        //
        String session = plugin.startSession();
        log.info("Session {}", session);

        try {
            String handle = plugin.openUrl(session, "http://ca.com");
            log.info("Handle {}", handle);

            synchronized (plugin) {
                plugin.wait(10000);
            }

            List<String> links = plugin.findLinks(session, handle, "community/ca-mobility");
            log.info("Found {} links", links.size());

            //
            Random rand = new Random();
            String link;
            if (!links.isEmpty()) {
                link = links.get(rand.nextInt(links.size()));
                plugin
                    .click(session, handle, SeleniumPlugin.SelectionBy.XPATH,
                        "//span[starts-with(text(),'Communities')]");
                synchronized (plugin) {
                    plugin.wait(3000);
                }
                boolean isOpenedLink = plugin
                    .click(session, handle, SeleniumPlugin.SelectionBy.XPATH,
                        "//a[contains(@href,'" + link + "')]");
                log.info("{}, {}", isOpenedLink, link);
            } else {
                log.warn("Could not find any links!");
            }
            //
            synchronized (plugin) {
                plugin.wait(10000);
            }
        } finally {
            plugin.closeSession(session);
        }
        log.info("Leaving now");
    }
}