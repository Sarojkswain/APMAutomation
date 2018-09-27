package com.ca.apm.systemtest.fld.plugin.dotnet.powerpack;

import java.util.regex.Matcher;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Created by haiva01 on 21.9.2015.
 */
public class SharePointPowerPackImplTest {
    String PROCESS_ID_METRIC_EXAMPLE = "\\\\AQPP-SP01\\Process(w3wp#1)\\ID Process";

    @Test
    public void testProcessIdParsing() {
        Matcher matcher = SharePointPowerPackImpl.PROCESS_ID_PATTERN
            .matcher(PROCESS_ID_METRIC_EXAMPLE);
        assertTrue(matcher.matches());
        String processInstanceName = matcher.group(1);
        assertEquals(processInstanceName, "w3wp#1");
    }

    @Test
    public void testMetricWithInstanceParsing() {
        Matcher matcher = SharePointPowerPackImpl.METRIC_WITH_INSTANCE_PATTERN
            .matcher(PROCESS_ID_METRIC_EXAMPLE);
        assertTrue(matcher.matches());
        assertEquals(matcher.group(1), "\\\\AQPP-SP01\\Process(");
        String processInstanceName = matcher.group(2);
        assertEquals(processInstanceName, "w3wp#1");
        assertEquals(matcher.group(3), ")\\ID Process");
    }
}