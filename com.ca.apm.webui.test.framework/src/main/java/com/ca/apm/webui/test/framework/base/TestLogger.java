package com.ca.apm.webui.test.framework.base;

import java.util.Properties;

import org.apache.log4j.FileAppender;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import com.ca.apm.webui.test.framework.interfaces.IConstants;

/**
 * <code>TestLogger</code> provides basic logging services.
 * <code>TestLogger</code> is a singleton object and provides for synchronized
 * logging writes to the testcase and testsuite log files.
 * 
 * @since QATF2.0
 * @author whogu01
 * @copyright 2013 CA Technology, All rights reserved.
 */
public final class TestLogger
    implements IConstants
{

    /**
     * Singleton TestLogger Object.
     * 
     * @since QAFT2.0
     */
    private static TestLogger fQATFLogger;             // singleton instance

    /**
     * <code>maximumLogLevel</code> specifies the 'ceiling' for log messages.
     * 
     * @see com.ca.apm.IConstants.interfaces.Constants
     */
    private static int        fMaximumLogLevel = DEBUG;

    /**
     * The log level associated with each log message.
     * 
     * @since QATF2.0
     */
    private int               fMessageLogLevel;        // log level for a
// specific message

    /**
     * Log4J Logger dedicated to the testcase log file.
     * 
     * @since QATF2.0
     */
    private Logger            fTcLog;                  // log4j

    /**
     * Log4J Logger dedicated to the testsuite log file.
     * 
     * @since QATF2.0
     */
    private Logger            fTsLog;                  // log4j

    /**
     * An Enum defining which log file(s) will receive the log message.
     * 
     * <pre>
     * LOGTYPE { tcase, tsuite, both }
     * </pre>
     * 
     * tcase = Write only to the testcase.log file.<br>
     * tsuite = Write only to the testsuite.log file.<br>
     * both = Write to both the testcase and testsuite log files.
     * 
     * @since QATF2.0
     */
    public enum LOGTYPE {
        /** Testcase.log file. */
        tcase,
        /** Testsuite.log file. */
        tsuite,
        /** Testcase and Testsuite log files. */
        both;
    } // end enum

    /**
     * Simple, no-args constructor called from the getInstance methods.
     * <code>TestLogger</code> is a singleton wrapper around log4j logger.
     * 
     * @since QATF2.0
     */
    private TestLogger()
    {
        Properties logProp = PropertyLoader.loadProperties(LOG_PROPERTIES);
        PropertyConfigurator.configure(logProp);
        fTcLog = Logger.getLogger("tCase"); // test-case log
        fTsLog = Logger.getLogger("tSuite"); // test-suite log
        FileAppender appender = null;
        fTcLog.addAppender(appender);
    } // end constructor

    /**
     * @return An instance of <code>TestLogger</code> at DEBUG level.
     */
    /*
     * public static TestLogger getInstance() { return
     * TestLogger.getInstance(DEBUG); } //end static factory
     */

    /**
     * @param maxLevel
     *            - desired log level for <code>TestLogger</code>.
     * @return An instance of <code>TestLogger</code> with baseLogLevel set to
     *         <code>baseLogLevel</code>.
     * @see com.ca.apm.IConstants.interfaces.Constants
     */
    public static TestLogger getInstance()
    {
        if (fQATFLogger == null)
        {
            fQATFLogger = new TestLogger();
            return fQATFLogger;
        } else
        {
            return fQATFLogger;
        } // end if..else
    } // end static factory

    /**
     * @return the <code>fMaximumLogLevel</code> for TestLogger.
     */
    public static int getMaximumLogLevel()
    {
        return fMaximumLogLevel;
    } // end method

    /**
     * @param level
     *            - desired log level ceiling for <code>TestLogger</code>.
     * @since QATF2.0
     * @see com.ca.apm.IConstants.interfaces.Constants
     */
    public static synchronized void setMaximumLogLevel(final int level)
    {
        fMaximumLogLevel = level;
    } // end method

    /**
     * Write a message to the test case log file.
     * 
     * @param level
     *            - message log level.
     * @param message
     *            - user defined text.
     * @since QATF2.0
     * @see com.ca.apm.IConstants.interfaces.Constants
     */
    public void logTestCase(final int level, final String message)
    {
        fMessageLogLevel = level;
        this.logMessage(TEST_CASE_LOG, fMessageLogLevel, message);
    } // end method

    /**
     * Write a message to the test suite log file.
     * 
     * @param level
     *            - message log level.
     * @param message
     *            - user defined text.
     * @since QATF2.0
     */
    public void logTestSuite(final int level, final String message)
    {
        fMessageLogLevel = level;
        this.logMessage(TEST_SUITE_LOG, fMessageLogLevel, message);
    } // end method

    /**
     * Write a message to the testcase.log and testsuite.log.
     * 
     * @param level
     *            - message log level.
     * @param message
     *            - user defined text.
     * @since QATF2.0
     */
    public void logBoth(final int level, final String message)
    {
        fMessageLogLevel = level;
        this.logMessage(TEST_LOGS_ALL, fMessageLogLevel, message);
    } // end method

    /**
     * Write a message to the <code>logType</code> file having the message log
     * level set to <code>msgLogLevel</code>.
     * 
     * @param logType
     *            - integer value representing a log file type.
     * @param msgLogLevel
     *            - message log level.
     * @param message
     *            - user defined text.
     * @see com.ca.apm.IConstants.interfaces.Constants
     */
    private void logMessage(final int logType,
                            final int msgLogLevel,
                            final String message)
    {

        boolean tc = false;
        boolean ts = false;

        // determine which log file to write to: tcase, tsuite, or both
        switch (logType)
        {
        case TEST_CASE_LOG:
            tc = true;
            break;
        case TEST_SUITE_LOG:
            ts = true;
            break;
        case TEST_LOGS_ALL:
            tc = true;
            ts = true;
        default: // write only to tcase
            tc = true;
            break;
        } // end type switch

        /*
         * The global 'baseLogLevel' needs to match or be greater than level in
         * order for this particular message to be written to the log
         */
        if (fMaximumLogLevel >= msgLogLevel)
        {

            synchronized (this)
            { // synch singleton access to logs
                switch (msgLogLevel)
                {
                case ERROR:
                    if (tc)
                    {
                        fTcLog.error(message);
                    }
                    if (ts)
                    {
                        fTsLog.error(message);
                    }
                    break;
                case INFO:
                    if (tc)
                    {
                        fTcLog.info(message);
                    }
                    if (ts)
                    {
                        fTsLog.info(message);
                    }
                    break;
                case DEBUG:
                    if (tc)
                    {
                        fTcLog.debug(message);
                    }
                    if (ts)
                    {
                        fTsLog.debug(message);
                    }
                    break;
                case TRACE:
                    if (tc)
                    {
                        fTcLog.debug("++TRACE++ " + message);
                    }
                    if (ts)
                    {
                        fTsLog.debug("++TRACE++ " + message);
                    }
                    break;
                default: // DEFAULT is debug message to testcase.log
                    fTcLog.debug(message);
                    break;
                } // end write switch
            } // end sync
        } // end if..else
    } // end method

} // end class
