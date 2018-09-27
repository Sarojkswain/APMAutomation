package com.ca.apm.systemtest.fld.common;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.Locale;
import java.util.Map;

import org.apache.commons.lang3.SystemUtils;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ca.apm.systemtest.fld.common.WindowsPerfMonitorUtils.PerfMonitorHandle;
import com.ca.apm.systemtest.fld.common.WindowsPerfMonitorUtils.Sample;

import static org.junit.Assert.assertTrue;

/**
 * Created by haiva01 on 3.9.2015.
 */
public class WindowsPerfMonitorUtilsTest {
    private static final Logger log = LoggerFactory.getLogger(WindowsPerfMonitorUtilsTest.class);

    private static final String MACHINE_PERCENT_PROCESSOR_TIME
        = "\\\\" + System.getenv("COMPUTERNAME") + "\\Processor(_Total)\\% Processor Time";

    //@Test
    public void test() throws Exception {
        if (!SystemUtils.IS_OS_WINDOWS) {
            log.info("Skipping test on non-Windows platform.");
            return;
        }


        String[] metrics = {
            MACHINE_PERCENT_PROCESSOR_TIME,
            "\\Process(csrss*)\\% Processor Time"
        };

        PerfMonitorHandle handle = WindowsPerfMonitorUtils
            .startMonitoring(Arrays.asList(metrics));
        Thread.sleep(3000);
        handle.endMonitoring();
        log.info("Recorded metric names: {}", handle.getRecordedMetricNames());
        Map<String, Collection<Sample>> samples = handle.getSamples();
        assertTrue(samples.containsKey(MACHINE_PERCENT_PROCESSOR_TIME));
        assertTrue(samples.get(MACHINE_PERCENT_PROCESSOR_TIME).size() >= 1);
    }


    @Test
    public void testDateParsing() throws ParseException {
        DateFormat fmt = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss.SSS", Locale.ENGLISH);
        Date date = fmt.parse("09/03/2015 12:50:25.249");
        Calendar cal = Calendar.getInstance(Locale.ENGLISH);
        cal.set(2015, Calendar.SEPTEMBER, 3, 12, 50, 25);
        cal.set(Calendar.MILLISECOND, 0);
        assertTrue(cal.getTime().before(date));
        cal.set(2015, Calendar.SEPTEMBER, 3, 12, 50, 26);
        cal.set(Calendar.MILLISECOND, 0);
        assertTrue(cal.getTime().after(date));
    }

}