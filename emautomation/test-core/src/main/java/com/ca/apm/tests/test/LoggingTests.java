/*
 * Copyright (c) 2014 CA. All rights reserved.
 * 
 * This software and all information contained therein is confidential and
 * proprietary and shall not be duplicated, used, disclosed or disseminated in
 * any way except as authorized by the applicable license agreement, without
 * the express written permission of CA. All authorized reproductions must be
 * marked with this language.
 * 
 * EXCEPT AS SET FORTH IN THE APPLICABLE LICENSE AGREEMENT, TO THE EXTENT
 * PERMITTED BY APPLICABLE LAW, CA PROVIDES THIS SOFTWARE WITHOUT WARRANTY OF
 * ANY KIND, INCLUDING WITHOUT LIMITATION, ANY IMPLIED WARRANTIES OF
 * MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE. IN NO EVENT WILL CA BE
 * LIABLE TO THE END USER OR ANY THIRD PARTY FOR ANY LOSS OR DAMAGE, DIRECT OR
 * INDIRECT, FROM THE USE OF THIS SOFTWARE, INCLUDING WITHOUT LIMITATION, LOST
 * PROFITS, BUSINESS INTERRUPTION, GOODWILL, OR LOST DATA, EVEN IF CA IS
 * EXPRESSLY ADVISED OF SUCH LOSS OR DAMAGE.
 */

package com.ca.apm.tests.test;

import static org.testng.Assert.assertTrue;

import java.io.IOException;
import java.util.HashMap;

import org.mortbay.log.Log;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import com.ca.apm.tests.testbed.OneEmAbstractTestbed;
import com.ca.apm.tests.testbed.OneEmLinuxTestbed;
import com.ca.apm.tests.utils.configutils.PropertiesUtility;
import com.ca.apm.tests.utils.emutils.EmBatLocalUtils;
import com.ca.apm.tests.utils.emutils.EmConfiguration;
import com.ca.apm.tests.utils.emutils.EnterpriseManagerHibernateTestConfig;
import com.ca.tas.envproperty.EnvironmentPropertyContext;
import com.ca.tas.envproperty.EnvironmentPropertyContextFactory;
import com.ca.tas.envproperty.EnvironmentPropertyException;
import com.ca.tas.tests.annotations.Tas;
import com.ca.tas.tests.annotations.TestBed;
import com.ca.tas.type.ExclusivityType;
import com.ca.tas.type.SizeType;

/**
 * EM BAT tests # 351866<br>
 * Author : Artur Sobieski<br>
 * 
 * Checking different logging levels. <h5>PRE-REQUISITES:</h5> <br>
 * <ol>
 * <li>Install EM with default options</li>
 * </ol>
 * <h5>TEST ACTIVITY</h5>
 * <p>
 * <ol>
 * <li>Install and start EM</li>
 * <li>Goto EM_Home/config and open the file IntroscopeEnterpriseManager.properties.Check if
 * following properties exist. log4j.logger.Manager=INFO, console,
 * logfilelog4j.logger.Manager.Support=INFO, supportlogfilelog4j.logger.Manager.Performance=DEBUG,
 * performancelog4j.logger.Manager.QueryLog=INFO, querylog</li>
 * <li>Change the logging level to ERROR for all these properties.In order to test this,Goto
 * EM_HOME/config.Open the tess-db-cfg.xml file and change the property <property
 * name="em.dbtype">Postgres</property> to<property name="em.dbtype">TestPostgres</property>Restart
 * the EM once this is done.Automation Note:Once this is tested change the property again to
 * <property name="em.dbtype">Postgres</property> and restart the EM.</li>
 * <li>Change the logging level to INFO for all these properties.</li>
 * <li>Change the logging level to VERBOSE#com.wily.util.feedback.Log4JSeverityLevel for all these
 * properties one by one.</li>
 * <li>Change the logging level to VERBOSE for all these properties one by one.</li>
 * <li>Change the logging level to DEBUG for all the properties one by one.</li>
 * <li>Change the logging level to TRACE for all these properties.this is not applicable to all
 * loggers.</li>
 * </ol>
 * </p>
 * 
 * <h5>EXPECTED RESULTS</h5>
 * <p>
 * <ol>
 * <li></li>
 * <li>These should be the default logging levels.</li>
 * <li>You should see the error related messages reported by the EM in these log files.You should
 * see log messages followed by [ERROR] text.This is a very lowest logging level</li>
 * <li>When the logging level is INFO:ERROR and INFO messages are written to the log files. You
 * should see log messages followed by [INFO] text.</li>
 * <li>ERROR,INFO,DEBUG and VERBOSE messages are written to the log files. You should see log
 * messages followed by [VERBOSE] text.This gives more detailed info that ERROR and INFO.</li>
 * <li>When the logging level is VERBOSE:ERROR,INFO, DEBUG and VERBOSE messages are written to the
 * log files. You should see log messages followed by [VERBOSE] text.This gives more detailed info
 * that ERROR, DEBUG and INFO.</li>
 * <li>When the logging level is DEBUG:ERROR,INFO and DEBUG and VERBOSE messages are written to the
 * log files. You should see log messages followed by [DEBUG] text.This gives a more detailed
 * messages than INFO, ERROR and VERBOSE.</li>
 * <li>When the logging level is TRACE:ERROR,INFO,DEBUG,VERBOSE and TRACE messages are written to
 * the log files. You should see log messages followed by [TRACE] text.This is the highest logging
 * level which gives the messages at a very depth.</li>
 * </ol>
 * </p>
 * <h5>RISKS MITIGATED</h5>
 * <p>
 * <ul>
 * <li>Possibility of logging levels not working correctly</li>
 * </ul>
 * </p>
 */
public class LoggingTests {
    public static Long LOG_TIMEOUT = 300 * 1000L;

    public static Long PORT_EMPTY_TIMEOUT = 3 * 1000L;

    public static long PORT_BUSY_TIMEOUT = 40 * 1000L;

    private EnvironmentPropertyContext envProps;

    private String emInstallDir;

    private EmConfiguration config;

    @BeforeTest
    public void setUp() throws EnvironmentPropertyException, IOException {
        envProps = new EnvironmentPropertyContextFactory().createFromSystemProperty();

        emInstallDir =
            (String) envProps.getMachineProperties().get(OneEmAbstractTestbed.EM_MACHINE_ID)
                .get("emInstallDir");

        config = new EmConfiguration(emInstallDir, OneEmAbstractTestbed.EM_PORT);
    }

    @Tas(testBeds = {@TestBed(name = OneEmLinuxTestbed.class, executeOn = OneEmAbstractTestbed.EM_MACHINE_ID)}, size = SizeType.MEDIUM, owner = "sobar03", exclusivity = ExclusivityType.NONEXCLUSIVE)
    @Test(groups = {"BAT", "Log"})
    public void initialPropertiesTest() throws Exception {

        HashMap<String, String> properties =
            PropertiesUtility.getPropertiesAsMap(config.getPropertiesPath());

        assertTrue(properties.get("log4j.logger.Manager").equals("INFO, console, logfile"));
        assertTrue(properties.get("log4j.logger.Manager.Support").equals("INFO, supportlogfile"));
        assertTrue(properties.get("log4j.logger.Manager.Performance").equals("DEBUG, performance"));
        assertTrue(properties.get("log4j.logger.Manager.QueryLog").equals("INFO, querylog"));
    }

    @Tas(testBeds = {@TestBed(name = OneEmLinuxTestbed.class, executeOn = OneEmAbstractTestbed.EM_MACHINE_ID)}, size = SizeType.MEDIUM, owner = "sobar03")
    @Test(groups = {"BAT", "Log", "Errorlog"})
    public void switchLoggingToErrorTest() throws Exception {

        EnterpriseManagerHibernateTestConfig dbConfig =
            new EnterpriseManagerHibernateTestConfig(config);

        EmBatLocalUtils.stopLocalEm(config);

        HashMap<String, String> errorLoggingProps = new HashMap<String, String>();

        errorLoggingProps.put("log4j.logger.Manager", "ERROR, console, logfile");
        errorLoggingProps.put("log4j.logger.Manager.Support", "ERROR, supportlogfile");
        errorLoggingProps.put("log4j.logger.Manager.Performance", "ERROR, performance");
        errorLoggingProps.put("log4j.logger.Manager.QueryLog", "ERROR, querylog");

        PropertiesUtility.saveProperties(config.getPropertiesPath(), errorLoggingProps, false);

        dbConfig.setDBPropertyValue("em.dbtype", "TestPostgres");

        try {
            EmBatLocalUtils.startLocalEm(config);
        } catch (Exception e) {
            Log.info("Expected exception caught.");
        }

        assertTrue(EmBatLocalUtils.isKeywordInLog(config.getLogPath(), "[ERROR]"));
    }

    @Tas(testBeds = {@TestBed(name = OneEmLinuxTestbed.class, executeOn = OneEmAbstractTestbed.EM_MACHINE_ID)}, size = SizeType.MEDIUM, owner = "sobar03")
    @Test(groups = {"BAT", "Log"})
    public void switchLoggingToInfoTest() throws Exception {

        EmBatLocalUtils.stopLocalEm(config);

        HashMap<String, String> infoLoggingProps = new HashMap<String, String>();
        infoLoggingProps.put("log4j.logger.Manager", "INFO, console, logfile");
        infoLoggingProps.put("log4j.logger.Manager.Support", "INFO, supportlogfile");
        infoLoggingProps.put("log4j.logger.Manager.Performance", "INFO, performance");
        infoLoggingProps.put("log4j.logger.Manager.QueryLog", "INFO, querylog");

        PropertiesUtility.saveProperties(config.getPropertiesPath(), infoLoggingProps, false);

        EmBatLocalUtils.startLocalEm(config);

        EmBatLocalUtils.waitForKeywordInLog(config.getLogPath(), "[INFO]", LOG_TIMEOUT);
    }

    @Tas(testBeds = {@TestBed(name = OneEmLinuxTestbed.class, executeOn = OneEmAbstractTestbed.EM_MACHINE_ID)}, size = SizeType.MEDIUM, owner = "sobar03")
    @Test(groups = {"BAT", "Log"})
    public void switchLoggingToVerboseTest() throws Exception {

        EmBatLocalUtils.stopLocalEm(config);

        HashMap<String, String> verboseLoggingProps = new HashMap<String, String>();

        verboseLoggingProps.put("log4j.logger.Manager", "VERBOSE, console, logfile");
        verboseLoggingProps.put("log4j.logger.Manager.Support", "VERBOSE, supportlogfile");
        verboseLoggingProps.put("log4j.logger.Manager.Performance", "VERBOSE, performance");
        verboseLoggingProps.put("log4j.logger.Manager.QueryLog", "VERBOSE, querylog");

        PropertiesUtility.saveProperties(config.getPropertiesPath(), verboseLoggingProps, false);

        EmBatLocalUtils.startLocalEm(config);

        if (!EmBatLocalUtils.isKeywordInLog(config.getLogPath(), "[VERBOSE]")) {
            EmBatLocalUtils.waitForKeywordInLog(config.getLogPath(), "[VERBOSE]", LOG_TIMEOUT);
        }


    }

    @Tas(testBeds = {@TestBed(name = OneEmLinuxTestbed.class, executeOn = OneEmAbstractTestbed.EM_MACHINE_ID)}, size = SizeType.MEDIUM, owner = "sobar03")
    @Test(groups = {"BAT", "Log"})
    public void switchLoggingToDebugTest() throws Exception {

        EmBatLocalUtils.stopLocalEm(config);

        HashMap<String, String> debugLoggingProps = new HashMap<String, String>();
        debugLoggingProps.put("log4j.logger.Manager", "DEBUG, console, logfile");
        debugLoggingProps.put("log4j.logger.Manager.Support", "DEBUG, supportlogfile");
        debugLoggingProps.put("log4j.logger.Manager.Performance", "DEBUG, performance");
        debugLoggingProps.put("log4j.logger.Manager.QueryLog", "DEBUG, querylog");

        PropertiesUtility.saveProperties(config.getPropertiesPath(), debugLoggingProps, false);

        EmBatLocalUtils.startLocalEm(config);

        if (!EmBatLocalUtils.isKeywordInLog(config.getLogPath(), "[DEBUG]")) {
            EmBatLocalUtils.waitForKeywordInLog(config.getLogPath(), "[DEBUG]", LOG_TIMEOUT);
        }

    }
}
