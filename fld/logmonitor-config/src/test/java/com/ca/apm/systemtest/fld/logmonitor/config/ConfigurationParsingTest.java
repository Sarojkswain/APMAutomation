package com.ca.apm.systemtest.fld.logmonitor.config;

import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.BasicConfigurator;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ca.apm.systemtest.fld.logmonitor.config.LogMonitorConfiguration;
import com.ca.apm.systemtest.fld.logmonitor.config.LogPeriodicity;
import com.fasterxml.jackson.databind.ObjectMapper;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * @author haiva01
 */
public class ConfigurationParsingTest {
    Logger log = LoggerFactory.getLogger(ConfigurationParsingTest.class);

    @BeforeClass
    public static void initialize() {
        BasicConfigurator.configure();
    }

    /**
     * This test is testing backwards compatibility with configuration files with misspelled
     * "periodicity" as "period*o*city".
     *
     * @throws IOException
     */
    @Test
    public void testMisspelledPeriodicityParsing() throws IOException {
        ObjectMapper om = new ObjectMapper();
        InputStream jsonSource = this.getClass().getClassLoader().getResourceAsStream(
            "com/ca/apm/systemtest/fld/logmonitor/config/misspelled-periodicity.json");
        LogMonitorConfiguration config = om.readValue(jsonSource, LogMonitorConfiguration.class);
        assertNotNull(config.getLogStreams());
        assertTrue(config.getLogStreams().containsKey("emLogStream"));
        assertNotNull(config.getLogStreams().get("emLogStream").getRules());
        final int NTH_RULE = 2;
        assertNotNull(config.getLogStreams().get("emLogStream").getRules().get(NTH_RULE));
        assertNotNull(
            config.getLogStreams().get("emLogStream").getRules().get(NTH_RULE)
                .getPeriodicityLevel());
        assertEquals(
            config.getLogStreams().get("emLogStream").getRules().get(NTH_RULE)
                .getPeriodicityLevel(),
            LogPeriodicity.OncePerPeriod);

        String ruleAsJson = om
            .writeValueAsString(config.getLogStreams().get("emLogStream").getRules().get(NTH_RULE));
        log.info("corrected serialized rule:\n{}", ruleAsJson);
        assertTrue(StringUtils.contains(ruleAsJson, "periodicity"));
        assertFalse(StringUtils.contains(ruleAsJson, "periodocity"));
    }
}
