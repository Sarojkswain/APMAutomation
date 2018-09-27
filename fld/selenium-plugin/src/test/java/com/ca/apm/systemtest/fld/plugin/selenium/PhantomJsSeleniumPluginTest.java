package com.ca.apm.systemtest.fld.plugin.selenium;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Random;

import org.apache.commons.lang3.StringUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ca.apm.systemtest.fld.plugin.selenium.SeleniumPlugin.SelectionBy;

import static org.junit.Assert.assertTrue;

/**
 * Created by haiva01 on 15.10.2015.
 */
public class PhantomJsSeleniumPluginTest {
    Logger log = LoggerFactory.getLogger(PhantomJsSeleniumPluginTest.class);

    @Before
    public void setUp() throws Exception {

    }

    @After
    public void tearDown() throws Exception {

    }

    //@Test
    public void testPhantomJs() throws SeleniumPluginException, InterruptedException {
        SeleniumPlugin plugin = new PhantomJsSeleniumPlugin();
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
                handle = plugin.openUrl(session, link);
                log.info("Handle for {}: {}", link, handle);
                List<String> communityLinks = plugin.findLinks(session, handle, "/thread/");
                log.info("Found links: {}", communityLinks);
                List<String> textOfLinks = plugin.getText(session, handle,
                    SelectionBy.XPATH, "//a");
                log.info("Text of links: {}", textOfLinks);
            } else {
                log.warn("Could not find any links!");
            }
            //
            synchronized (plugin) {
                plugin.wait(20000);
            }
        } finally {
            plugin.closeSession(session);
        }
        log.info("Leaving now");
    }

    @Test
    public void cemWebRecordingTextParsing() throws ParseException {
        final String TEST_TEXT = "cemadmin@Oct 15, 2015 11:34 PM";
        final String[] parts = StringUtils.split(TEST_TEXT, '@');
        assertTrue(parts.length == 2);
        final String dateStr = parts[1];
        final SimpleDateFormat dateFormat = new SimpleDateFormat("MMM d, yyyy hh:mm a", Locale.US);
        dateFormat.setLenient(false);
        final Date date = dateFormat.parse(dateStr);
        log.info("parsed date {} out of string {}", date, dateStr);

        Calendar calendar = Calendar.getInstance();
        int hoursBack = 8;
        calendar.add(Calendar.HOUR, -hoursBack);
        Date threshold = calendar.getTime();
        date.after(threshold);
    }
}
