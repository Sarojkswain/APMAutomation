package com.ca.apm.nextgen.tests.helpers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;

/**
 * @author haiva01
 */
public class ErrorReportTest {
    private static final Logger log = LoggerFactory.getLogger(ErrorReportTest.class);

    //@Test
    public void testLogExceptionFmt() throws Exception {
        log.info("Following logged exceptions are expected:");
        try {
            throw new RuntimeException("test exception");
        } catch (Exception ex) {
            ErrorReport.logExceptionFmt(log, ex, "From catch without parameters");
            ErrorReport.logExceptionFmt(log, ex, "From catch with exception message: {0}");
            ErrorReport.logExceptionFmt(log, ex, "From catch with just own parameter: {1}", "THIS");
            ErrorReport
                .logExceptionFmt(log, ex, "From catch with own parameter: {1} and exception: {0}",
                    "THAT");
            ErrorReport
                .logExceptionFmt(log, ex,
                    "From catch with two own parameters: {1} and {2} and exception: {0}",
                    "THIS", "THAT");

        }
        log.info("Previously logged exceptions were expected.");
    }

}