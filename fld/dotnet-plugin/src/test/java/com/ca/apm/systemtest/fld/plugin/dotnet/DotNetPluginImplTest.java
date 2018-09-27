package com.ca.apm.systemtest.fld.plugin.dotnet;

import java.io.IOException;
import java.io.StringReader;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Created by haiva01 on 15.9.2015.
 */
public class DotNetPluginImplTest {
    private static final Logger log = LoggerFactory.getLogger(DotNetPluginImplTest.class);

    String WORKER_PROCESS_LIST_TEXT = "WP \"8036\" (applicationPool:SharePoint - 80)\n"
        + "WP \"12060\" (applicationPool:5845da092bfc49fab82d214fade9dd1f)\n"
        + "WP \"9824\" (applicationPool:SecurityTokenServiceApplicationPool)";

    @Test
    public void testIisWorkerProcessListParsing() throws IOException {
        List<String> lines = IOUtils.readLines(new StringReader(WORKER_PROCESS_LIST_TEXT));
        log.info("Parsing lines:\n{}", WORKER_PROCESS_LIST_TEXT);

        Pair<String, String> pair = DotNetPluginImpl.parseIisWorkersListLine(lines.get(0));
        log.info("Parsed 1st line: {}", pair);
        assertNotNull(pair);
        assertNotNull(pair.getKey());
        assertNotNull(pair.getValue());
        assertEquals(pair.getKey(), String.valueOf(8036));
        assertEquals(pair.getValue(), "(applicationPool:SharePoint - 80)");

        pair = DotNetPluginImpl.parseIisWorkersListLine(lines.get(1));
        log.info("Parsed 2nd line: {}", pair);
        assertNotNull(pair);
        assertNotNull(pair.getKey());
        assertNotNull(pair.getValue());
        assertEquals(pair.getKey(), String.valueOf(12060));
        assertEquals(pair.getValue(), "(applicationPool:5845da092bfc49fab82d214fade9dd1f)");
    }
}