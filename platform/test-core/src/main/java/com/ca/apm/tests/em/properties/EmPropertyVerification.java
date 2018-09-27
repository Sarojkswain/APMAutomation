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
 * 
 * Author : TUUJA01/ JAYARAM PRASAD
 * Date : 11/08/2016
 */
package com.ca.apm.tests.em.properties;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.codehaus.plexus.util.Os;
import org.junit.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import com.ca.apm.automation.action.flow.em.DeployEMFlowContext;
import com.ca.apm.automation.action.flow.webapp.DeployTomcatFlowContext;
import com.ca.apm.commons.coda.common.ApmbaseConstants;
import com.ca.apm.commons.coda.common.ApmbaseUtil;
import com.ca.apm.commons.common.CLWCommons;
import com.ca.apm.commons.common.TestUtils;
import com.ca.apm.tests.base.StandAloneEMOneTomcatTestsBase;


public class EmPropertyVerification extends StandAloneEMOneTomcatTestsBase {

    private static final Logger LOGGER = LoggerFactory.getLogger(EmPropertyVerification.class);
    protected final String host;
    protected final String emLibDir;
    protected final String configFileEm;
    protected final String EMlogFile;
    protected final String emPerfLogFile;
    protected final String emQueryLogFile;
    protected final String emLogDir;
    protected final String emSecurePort;
    protected final String emPort;
    protected final String tomcatagentProfileFile;
    protected final String configFileEm_backup;
    protected final String tessdefaultXmlFile_backup;
    protected final String tomcatAgentExp;
    protected final String tessdefaultXmlFile;
    protected String tomcatAgentLogFile;
    protected String testcaseId;
    protected String emErrorLog;
    protected String emInfoLog;
    protected String emDebugLog;
    protected String emVerboseLog;
    protected String emTraceLog;
    protected String emCLWLog;
    CLWCommons clw;
    TestUtils testUtils;

    public EmPropertyVerification() {

        clw = new CLWCommons();
        testUtils = new TestUtils();

        tomcatAgentExp = ".*Tomcat.*";
        emPort = envProperties.getRolePropertiesById(EM_ROLE_ID).getProperty("emPort");
        emSecurePort = ApmbaseConstants.emSSLPort;
        host = envProperties.getMachineHostnameByRoleId(EM_ROLE_ID);
        emLibDir =
            envProperties.getRolePropertyById(EM_ROLE_ID, DeployEMFlowContext.ENV_EM_LIB_DIR);
        EMlogFile =
            envProperties.getRolePropertyById(EM_ROLE_ID, DeployEMFlowContext.ENV_EM_LOG_FILE);
        configFileEm =
            envProperties.getRolePropertyById(EM_ROLE_ID, DeployEMFlowContext.ENV_EM_CONFIG_FILE);
        configFileEm_backup = configFileEm + "_backup";
        tomcatagentProfileFile =
            envProperties.getRolePropertyById(TOMCAT_ROLE_ID,
                DeployTomcatFlowContext.ENV_TOMCAT_INSTALL_DIR)
                + "/wily/core/config/IntroscopeAgent.profile";
        tomcatAgentLogFile =
            envProperties.getRolePropertyById(TOMCAT_ROLE_ID,
                DeployTomcatFlowContext.ENV_TOMCAT_INSTALL_DIR) + "/wily/logs/IntroscopeAgent.log";
        tessdefaultXmlFile =
            envProperties.getRolePropertyById(EM_ROLE_ID, DeployEMFlowContext.ENV_EM_CONFIG_DIR)
                + "tess-db-cfg.xml";
        tessdefaultXmlFile_backup = tessdefaultXmlFile + "_backup";

        emErrorLog =
            envProperties.getRolePropertyById(EM_ROLE_ID, DeployEMFlowContext.ENV_EM_INSTALL_DIR)
                + "/logs/IntroscopeEnterpriseManager_ERROR.log";
        emInfoLog =
            envProperties.getRolePropertyById(EM_ROLE_ID, DeployEMFlowContext.ENV_EM_INSTALL_DIR)
                + "/logs/IntroscopeEnterpriseManager_INFO.log";
        emDebugLog =
            envProperties.getRolePropertyById(EM_ROLE_ID, DeployEMFlowContext.ENV_EM_INSTALL_DIR)
                + "/logs/IntroscopeEnterpriseManager_DEBUG.log";
        emVerboseLog =
            envProperties.getRolePropertyById(EM_ROLE_ID, DeployEMFlowContext.ENV_EM_INSTALL_DIR)
                + "/logs/IntroscopeEnterpriseManager_VERBOSE.log";
        emTraceLog =
            envProperties.getRolePropertyById(EM_ROLE_ID, DeployEMFlowContext.ENV_EM_INSTALL_DIR)
                + "/logs/IntroscopeEnterpriseManager_TRACE.log";
        emCLWLog =
            envProperties.getRolePropertyById(EM_ROLE_ID, DeployEMFlowContext.ENV_EM_INSTALL_DIR)
                + "/logs/IntroscopeEnterpriseManager_CLW.log";

        emLogDir =
            envProperties.getRolePropertyById(EM_ROLE_ID, DeployEMFlowContext.ENV_EM_INSTALL_DIR)
                + "/logs/";
        emPerfLogFile =
            envProperties.getRolePropertyById(EM_ROLE_ID, DeployEMFlowContext.ENV_EM_INSTALL_DIR)
                + "/logs/perflog.txt";
        emQueryLogFile =
            envProperties.getRolePropertyById(EM_ROLE_ID, DeployEMFlowContext.ENV_EM_INSTALL_DIR)
                + "/logs/querylog.txt";

    }

    @BeforeTest(alwaysRun = true)
    public void initialize() {
        List<String> machines = new ArrayList<String>();
        machines.add(EM_MACHINE_ID);
        machines.add(TOMCAT_MACHINE_ID);
        syncTimeOnMachines(machines);


    }

   @Test(groups = {"BAT"}, enabled = true)
    public void verify_ALM_351866_TT60661_Logging_levels() throws IOException {
        LOGGER.info("This is to verify verify_ALM_351866_TT60661_Logging_levels");

        try {
            backupFile(configFileEm, configFileEm_backup, EM_MACHINE_ID);
            backupFile(tessdefaultXmlFile, tessdefaultXmlFile_backup, EM_MACHINE_ID);
            startEM();

            try {
                isKeywordInFile(envProperties, EM_MACHINE_ID, configFileEm,
                    EmPropertyConstants.defaultlog4jMangerProp);

            } catch (Exception e) {
                LOGGER.info("Default values are not found for LOGGing");
                Assert.assertTrue(false);
                e.printStackTrace();
            }
            LOGGER.info("Default Properties are present in EMconfig file.....");

            replaceProp("<property name=\"em.dbtype\">Postgres",
                "<property name=\"em.dbtype\">TestPostgres", EM_MACHINE_ID, tessdefaultXmlFile);
            LOGGER.info("Updated the tess-db-cfg file entry to get error in em startup....");
            restartEM();
            checkEMLogForMsg("[ERROR] [main] [Manager.com.timestock.tess.util.DbConfiguration] Unknown database type: default used (Postgres)");
            LOGGER.info("ERROR check is done now go to next step..");

            replaceEMProperty(EmPropertyConstants.defaultlog4jMangerProp,
                EmPropertyConstants.verboselog4jMangerProp);
            replaceEMProperty(EmPropertyConstants.emLogDefultPath,
                EmPropertyConstants.emLogVerbosePath);
            checkEMLogForMsg( "Detected hot config change to");
            startAgent();
            checkLogForMsg(envProperties, EM_MACHINE_ID, emVerboseLog, "[VERBOSE]");
            LOGGER.info("VERBOSE check is done now go to next step..");

            replaceEMProperty(EmPropertyConstants.verboselog4jMangerProp,
                EmPropertyConstants.debuglog4jMangerProp);
            replaceEMProperty(EmPropertyConstants.emLogVerbosePath,
                EmPropertyConstants.emLogDebugPath);
            checkLogForMsg(envProperties, EM_MACHINE_ID, emVerboseLog,
                "Detected hot config change to");
            checkLogForMsg(envProperties, EM_MACHINE_ID, emDebugLog, "[DEBUG]");
            LOGGER.info("DEBUG check is done now go to next step..");

            replaceEMProperty(EmPropertyConstants.debuglog4jMangerProp,
                EmPropertyConstants.tracelog4jMangerProp);
            replaceEMProperty(EmPropertyConstants.emLogDebugPath,
                EmPropertyConstants.emLogTracePath);
            checkLogForMsg(envProperties, EM_MACHINE_ID, emDebugLog,
                "Detected hot config change to");
            checkLogForMsg(envProperties, EM_MACHINE_ID, emTraceLog, "[TRACE]");
            LOGGER.info("TRACE check is done,Test Passed....");

            replaceEMProperty(EmPropertyConstants.emLogTracePath,
                EmPropertyConstants.emLogDefultPath);
            checkLogForMsg(envProperties, EM_MACHINE_ID, emTraceLog,
                "Detected hot config change to");

        } finally {
            stopEM();
            stopAgent();
            revertFile(tessdefaultXmlFile_backup, tessdefaultXmlFile, EM_MACHINE_ID);
            revertFile(configFileEm_backup, configFileEm, EM_MACHINE_ID);

        }

    }

   @Test(groups = {"Full"}, enabled = true)
    public void verify_ALM_280562_TT_60022_Additivity_property_for_CLW_query_doesnt_work() {
        LOGGER
            .info("This is to verify_ALM_280562_TT_60022_Additivity_property_for_CLW_query_doesnt_work");
        try {
            backupFile(configFileEm, configFileEm_backup, EM_MACHINE_ID);

            startEM();
            startAgent();

            replaceEMProperty("log4j.additivity.Manager.CLW=false",
                "log4j.additivity.Manager.CLW=true");
            replaceEMProperty("log4j.logger.Manager.CLW=INFO", "log4j.logger.Manager.CLW=DEBUG");
            checkEMLogForMsg("Detected hot config change to");

            clw.getNodeList("admin", "", tomcatAgentExp, host, Integer.parseInt(emPort), emLibDir);
            checkEMLogForMsg("Execute command: list agents matching");
            LOGGER.info("Enabled CLW logging and found corresponding message....");

            replaceEMProperty("log4j.additivity.Manager.CLW=true",
                "log4j.additivity.Manager.CLW=false");
            replaceEMProperty("log4j.logger.Manager.CLW=DEBUG", "log4j.logger.Manager.CLW=INFO");
            checkEMLogForMsg( "Detected hot config change to");

            try {
                isKeywordInFileOneTimeCounter(envProperties, EM_MACHINE_ID, EMlogFile,
                    "Execute command: list agents matching");
            } catch (Exception e) {
                LOGGER.info("Test Failed, given keyword is not found or found more than once....");
                Assert.assertTrue(false);
            }
        } finally {
            stopAgent();
            stopEM();
            revertFile(configFileEm_backup, configFileEm, EM_MACHINE_ID);
        }
    }

   @Test(groups = {"Full"}, enabled = true)
    public void verify_ALM_223403_TT41946_Testing_the_start_up_messages_of_EM_on_the_console() {
        LOGGER
            .info("This is to verify_ALM_223403_TT41946_Testing_the_start_up_messages_of_EM_on_the_console");
        String command = "";
        boolean isWindows = false;
        String consoleOutMessage = "";
        try {
            isWindows = Os.isFamily(Os.FAMILY_WINDOWS);
            command =
                isWindows ? "cd "
                    + envProperties.getRolePropertyById(EM_ROLE_ID,
                        DeployEMFlowContext.ENV_EM_INSTALL_DIR) + "/bin/" + " && "
                    + "EMCtrl64.bat register && EMCtrl64.bat start" : "cd "
                    + envProperties.getRolePropertyById(EM_ROLE_ID,
                        DeployEMFlowContext.ENV_EM_INSTALL_DIR) + "/bin/" + " ; "
                    + "sh EMCtrl.sh start";
            LOGGER.info("The command is ...." + command);

            String[] commands = {command};
            consoleOutMessage = isWindows ? "Introscope Enterprise Manager started" : "Please check log file for more details";
            try {

                if (ApmbaseUtil.checkConsoleOutput(ApmbaseUtil.getProcess(commands, envProperties
                    .getRolePropertyById(EM_ROLE_ID, DeployEMFlowContext.ENV_EM_INSTALL_DIR)),
                    consoleOutMessage) == 1) {
                    LOGGER
                        .info("The expected Message found when EM controlscript command is executed... Test Passed");
                    Assert.assertTrue(true);
                } else {
                    Assert.assertTrue(false);
                }
            } catch (IOException e) {
                LOGGER.info("Failed to execute the command.....");
                Assert.assertTrue(false);
                e.printStackTrace();
            }
        } finally {
            stopEM();
            stopEMServiceFlowExecutor(EM_MACHINE_ID);
        }
    }

   @Test(groups = {"Deep"}, enabled = true)
    public void verify_ALM_449064_250227_TT85945_perflog_data_collection_is_disabled_by_defaul_in_95() {
        LOGGER
            .info("This is to verify_ALM_449064_250227_TT85945_perflog_data_collection_is_disabled_by_defaul_in_95");

        try {
            isKeywordInFile(envProperties, EM_MACHINE_ID, configFileEm,
                EmPropertyConstants.defaultlog4jPerformanceProp);
        } catch (Exception e) {
            LOGGER
                .info("Default values mentioned in EM Prop are not proper which are supposed to be log4j.logger.Manager.Performance=DEBUG, performance");
            Assert.assertTrue(false);
        }

    }

   @Test(groups = {"Deep"}, enabled = true)
    public void verify_ALM_352586_76859_IREG91A_com_wily_util_exception_UnexpectedExceptionError_while_shutdown_EM() {
        LOGGER
            .info("This is to verify_ALM_352586_76859_IREG91A_com_wily_util_exception_UnexpectedExceptionError_while_shutdown_EM");
        try {
            startEM();

            try {
                isKeywordInFile(
                    envProperties,
                    EM_MACHINE_ID,
                    EMlogFile,
                    "Caught an exception while stopping the EM: com.wily.util.exception.UnexpectedExceptionError: Service com.wily.introscope.server.enterprise.entity.domain.IDomainEntity not found");
            } catch (Exception e) {
                Assert.assertTrue(true);
                LOGGER
                    .info("The above error message is not seen in EM log hence test is passed....");
            }
        } finally {
            stopEM();
        }


    }

   @Test(groups = {"Smoke"}, enabled = true)
    public void verify_ALM_351868_Change_the_MaxFileSize_and_MaxBackupIndex_for_EM_log_file() {
        LOGGER
            .info("This is to verify_ALM_351868_Change_the_MaxFileSize_and_MaxBackupIndex_for_EM_log_file");

        try {
            backupFile(configFileEm, configFileEm_backup, EM_MACHINE_ID);

            try {
                isKeywordInFile(envProperties, EM_MACHINE_ID, configFileEm,
                    EmPropertyConstants.defaultLogFileMaxSize);
                isKeywordInFile(envProperties, EM_MACHINE_ID, configFileEm,
                    EmPropertyConstants.defaultLogFileMaxBackupIndex);
            } catch (Exception e) {
                LOGGER
                    .info("Default values of MaxFile Size & Index are not available....,Test Failed");
                Assert.assertTrue(false);
            }

            startEM();
            replaceEMProperty(EmPropertyConstants.defaultlog4jMangerProp,
                EmPropertyConstants.debuglog4jMangerProp);
            checkEMLogForMsg("Detected hot config change to");

            replaceEMProperty(EmPropertyConstants.defaultLogFileMaxSize,
                EmPropertyConstants.customLogFileMaxSize);
            replaceEMProperty(EmPropertyConstants.defaultLogFileMaxBackupIndex,
                EmPropertyConstants.customLogFileMaxBackupIndex);

            startAgent();
            harvestWait(60);

            List<String> fileNames = new ArrayList<String>();
            fileNames.add("IntroscopeEnterpriseManager.log.1");
            fileNames.add("IntroscopeEnterpriseManager.log.2");
            fileNames.add("IntroscopeEnterpriseManager.log.3");
            LOGGER
                .info("LOG4J Index and Backup count properties are updated lets check the file existence and sizes....");
            stopAgent();
            startAgent();
            harvestWait(60);
            Assert.assertTrue(testUtils.isGivenListOfFilesFound(fileNames, emLogDir));
            if (testUtils.getFileSizeinKB(fileNames.get(0), emLogDir) >= 19) {
                LOGGER.info("The file size is correct..");
            } else
                Assert.assertTrue(false);
        }

        finally {
            stopEM();
            stopAgent();
            revertFile(configFileEm, configFileEm_backup, EM_MACHINE_ID);
        }
    }

   @Test(groups = {"Smoke"}, enabled = true)
    public void verify_ALM_351871_Disable_Performance_logging() {
        LOGGER.info("This is to verify_ALM_351871_Disable_Performance_logging");
        try {
            startEM();
            replaceEMProperty(EmPropertyConstants.defaultlog4jPerformanceProp,
                "log4j.logger.Manager.Performance=OFF,performance");
            checkEMLogForMsg( "Detected hot config change to");
            int currentLineCount = testUtils.returnLineCountOfGivenFile(emLogDir + "perflog.txt");
            harvestWait(60);
            Assert.assertEquals(testUtils.returnLineCountOfGivenFile(emLogDir + "perflog.txt"),
                currentLineCount);

        } catch (IOException e) {
            LOGGER.info("Log file not found.... ");
            Assert.assertTrue(false);
            e.printStackTrace();
        } finally {
            stopEM();
            replaceEMProperty("log4j.logger.Manager.Performance=OFF,performance",
                EmPropertyConstants.defaultlog4jPerformanceProp);
        }
    }

    @Test(groups = {"Smoke"}, enabled = true)
    public void verify_ALM_448870_78159_Query_log_is_broken() {
        LOGGER.info("This is to verify_ALM_448870_78159_Query_log_is_broken");

        try {
            startEM();
            startAgent();
            replaceEMProperty("log4j.additivity.Manager.QueryLog=false",
                "log4j.additivity.Manager.QueryLog=true");
            replaceEMProperty(EmPropertyConstants.defaultlog4jQueryLogProp,
                "log4j.logger.Manager.QueryLog=DEBUG, querylog");
            checkEMLogForMsg("Detected hot config change to");
            for (int i = 0; i < 3; i++) {
                try {
                    clw.getHistoricMetricValuesForTimeInMinutes("admin", "", tomcatAgentExp, ".*", host,
                        Integer.parseInt(emPort), emLibDir, 1);
                       harvestWait(30);
                       LOGGER.info("Hit the CLW command for "+i+" Time...");
                } catch (Exception e) {
                    LOGGER.info("Failed to execute the command");
                    Assert.assertTrue(false);
                    e.printStackTrace();
                }
            }
            checkLogForMsg(envProperties, EM_MACHINE_ID, emQueryLogFile, "<BeginQuery timestamp=");
            LOGGER.info("Test Passed, seen result in querylog file....");

        } finally {
            stopEM();
            stopAgent();
            replaceEMProperty("log4j.additivity.Manager.QueryLog=true",
                "log4j.additivity.Manager.QueryLog=false");
            replaceEMProperty("log4j.logger.Manager.QueryLog=DEBUG, querylog",
                EmPropertyConstants.defaultlog4jQueryLogProp);
        }

    }

   @Test(groups = {"Smoke"}, enabled = true)
    public void verify_ALM_351867_introscope_enterprisemanager_performance_compressed_false() {
        LOGGER
            .info("This is to verify_ALM_351867_introscope_enterprisemanager_performance_compressed_false");

        try {
            startEM();
            startAgent();

            try {
                isKeywordInFile(envProperties, EM_MACHINE_ID, configFileEm,
                    EmPropertyConstants.defaultEmPerformanceCompressed);
                isKeywordInFile(envProperties, EM_MACHINE_ID, configFileEm,
                    EmPropertyConstants.defaultEMPerflogDisabled);
            } catch (Exception e) {
                LOGGER
                    .info("The given property is not available in the EM prop file or File itself not available/accessible..");
                Assert.assertTrue(false);
                e.printStackTrace();
            }

            replaceEMProperty(EmPropertyConstants.defaultEmPerformanceCompressed,
                "introscope.enterprisemanager.performance.compressed=false");
            replaceEMProperty(EmPropertyConstants.defaultlog4jMangerProp,
                EmPropertyConstants.debuglog4jMangerProp);
            restartEM();

            try {
                isKeywordInFileOneTimeCounter(
                    envProperties,
                    EM_MACHINE_ID,
                    configFileEm,
                    "[DEBUG] [Harvest Engine Pooled Worker] [Manager.Performance] Performance.Transactions.Num.Inserts.Per.Interval");
                Assert.assertTrue(false);
            } catch (Exception e) {
                LOGGER
                    .info("The given LOG text is not available in the LOG which is expected, test passed here..");
                Assert.assertTrue(true);
            }

            checkLogForMsg(envProperties, EM_MACHINE_ID, emPerfLogFile,
                "Performance.Transactions.Num.Inserts.Per.Interval");
            replaceEMProperty(EmPropertyConstants.defaultEMPerflogDisabled,
                "log4j.additivity.Manager.Performance= true");
            checkEMLogForMsg(
                "[DEBUG] [Harvest Engine Pooled Worker] [Manager.Performance] Performance.Transactions.Num.Inserts.Per.Interval");

            LOGGER
                .info("Test Passed, by putting compression to false, writes the data in compressed mode in EM log as well as Perflog.txt.");

        } finally {
            stopEM();
            stopAgent();
            replaceEMProperty("introscope.enterprisemanager.performance.compressed=false",
                EmPropertyConstants.defaultEmPerformanceCompressed);
            replaceEMProperty(EmPropertyConstants.debuglog4jMangerProp,
                EmPropertyConstants.defaultlog4jMangerProp);
            replaceEMProperty("log4j.additivity.Manager.Performance= true",
                EmPropertyConstants.defaultEMPerflogDisabled);
        }
    }

   @Test(groups = {"Deep"}, enabled = true)
    public void verify_ALM_351878_log4j_additivity_Manager_Support_true() {
        LOGGER.info("This is to verify_ALM_351878_log4j_additivity_Manager_Support_true");
        try {
            startEM();
            try {
                isKeywordInFile(envProperties, EM_MACHINE_ID, configFileEm,
                    EmPropertyConstants.defaultEMSupportDisabled);
            } catch (Exception e) {
                LOGGER.info("The property is not avialble "
                    + EmPropertyConstants.defaultEMSupportDisabled);
                Assert.assertTrue(false);
                e.printStackTrace();
            }

            replaceEMProperty(EmPropertyConstants.defaultEMSupportDisabled,
                "log4j.additivity.Manager.Support=true");
            restartEM();
            checkEMLogForMsg("[Manager.Support.Properties]");
        } finally {
            stopEM();
            replaceEMProperty("log4j.additivity.Manager.Support=true",
                EmPropertyConstants.defaultEMSupportDisabled);

        }
    }
}
