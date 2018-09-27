package com.ca.apm.tests.smartstor.sstools;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;

import com.ca.apm.automation.action.flow.em.DeployEMFlowContext;
import com.ca.apm.commons.coda.common.ApmbaseConstants;
import com.ca.apm.commons.coda.common.ApmbaseUtil;
import com.ca.apm.commons.coda.common.AutomationConstants;
import com.ca.apm.commons.coda.common.SSToolsConstants;
import com.ca.apm.commons.coda.common.SSToolsUtil;
import com.ca.apm.commons.coda.common.Util;
import com.ca.apm.commons.common.CLWCommons;
import com.ca.apm.tests.base.StandAloneEMOneTomcatOneJBossTestsBase;
import com.ca.apm.tests.common.introscope.util.CLWBean;

public class SSToolsBase extends StandAloneEMOneTomcatOneJBossTestsBase {

    protected String emToolsLoc;
    protected String emPath;
    protected String validEMDataLoc;
    protected CLWBean clw, clwBean, clwAdmin = null;
    public CLWCommons clwCommon = new CLWCommons();
    String platform = System.getProperty("os.name");
    private static final Logger LOGGER = LoggerFactory.getLogger(SSToolsBase.class);

    public SSToolsBase() {
        emPath =
            envProperties.getRolePropertyById(EM_ROLE_ID, DeployEMFlowContext.ENV_EM_INSTALL_DIR);
        emToolsLoc = emPath + ApmbaseConstants.EM_TOOLS_LOC;
        validEMDataLoc = emPath + ApmbaseConstants.EM_DATA_LOC;
    }


    public void execSSTCmdHelpRemoveMetrics(String cmdSuccStatus) {
        try {

            List<String> remove_Metrics = new ArrayList<String>();
            remove_Metrics.add("-dest       Destination SmartStor directory");
            remove_Metrics.add("-help       Prints a help message");
            remove_Metrics.add("-metrics    Regular expression for metrics to remove");
            remove_Metrics.add("-src        Source SmartStor directory");

            int statusCmndExec = 0;
            statusCmndExec =
                SSToolsUtil.executeSmartCommand(SSToolsConstants.HELP_CMD_REMOVE_METRICS,
                    emToolsLoc, remove_Metrics);
            Assert.assertEquals(statusCmndExec, Integer.parseInt(cmdSuccStatus));
        } catch (Exception e) {
            Assert.fail("Failed while executing the command:: REMOVE HELP METRICS.");
        }
    }

    /**
     * Method to test Help command Test_Regex
     * 
     * @param cmdSuccStatus
     */
    public void execSSTCmdHelpTestRegex(String cmdSuccStatus) {
        try {
            List<String> test_Regex_help = new ArrayList<String>();
            test_Regex_help.add("-agents     Regular expression for agents to match");
            test_Regex_help.add("-help       Prints a help message");
            test_Regex_help.add("-metrics    Regular expression for metrics to match");
            test_Regex_help.add("-src        Source SmartStor directory");

            int statusCmndExec = 0;
            statusCmndExec =
                SSToolsUtil.executeSmartCommand(SSToolsConstants.HELP_CMD_TEST_REGEX, emToolsLoc,
                    test_Regex_help);
            Assert.assertEquals(statusCmndExec, Integer.parseInt(cmdSuccStatus));
        } catch (Exception e) {
            Assert.fail("Failed while executing the command:: HELP TEST REGEX.");
        }
    }

    /**
     * Method to test Help command Remove Agents
     * 
     * @param cmdSuccStatus
     */
    public void execSSTCmdHelpRemoveAgents(String cmdSuccStatus) {
        try {
            List<String> removeAgentsHelp = new ArrayList<String>();
            removeAgentsHelp.add("-agents");
            removeAgentsHelp.add("-dest");
            removeAgentsHelp.add("-help");
            removeAgentsHelp.add("-domains");
            removeAgentsHelp.add("-src");

            int statusCmndExec = 0;
            statusCmndExec =
                SSToolsUtil.executeSmartCommand(SSToolsConstants.HELP_CMD_TEST_REMOVE_AGENTS,
                    emToolsLoc, removeAgentsHelp);
            Assert.assertEquals(statusCmndExec, Integer.parseInt(cmdSuccStatus));
        } catch (Exception e) {
            Assert.fail("Failed while executing the command:: HELP TEST REMOVE AGENTS.");
        }
    }

    /**
     * Method to test Help Command Prune Agents
     * 
     * @param cmdSuccStatus
     */
    public void execSSTCmdHelpPrune(String cmdSuccStatus) {
        try {

            List<String> pruneHelpCmds = new ArrayList<String>();
            pruneHelpCmds.add("-dest     Backup directory for copies of modified files");
            pruneHelpCmds.add("-help     Prints a help message");
            pruneHelpCmds.add("-silent   Silent mode");
            pruneHelpCmds.add("-src      Source SmartStor directory");

            int statusCmndExec = 0;
            statusCmndExec =
                SSToolsUtil.executeSmartCommand(SSToolsConstants.HELP_CMD_PRUNE, emToolsLoc,
                    pruneHelpCmds);
            Assert.assertEquals(statusCmndExec, Integer.parseInt(cmdSuccStatus));
        } catch (Exception e) {
            Assert.fail("Failed while executing the command:: HELP TEST PRUNE AGENTS.");
        }
    }

    /**
     * Method to test Help Command Keep Agents
     * 
     * @param cmdSuccStatus
     */
    public void execSSTCmdHelpKeepAgents(String cmdSuccStatus) {
        try {
            int statusCmndExec = 0;
            System.out.println("Inside exec method....");
            List<String> comparMsgs = new ArrayList<String>();
            comparMsgs.add("-agents    Regular expression for agents to keep");
            comparMsgs.add("-dest      Destination SmartStor directory");
            comparMsgs.add("-help      Prints a help message");
            comparMsgs.add("-src       Source SmartStor directory");
            statusCmndExec =
                SSToolsUtil.executeSmartCommand(SSToolsConstants.HELP_CMD_KEEP_AGENTS, emToolsLoc,
                    null);
            System.out.println("The status is...." + statusCmndExec + "The expected Statuys is"
                + cmdSuccStatus);
            Assert.assertEquals(statusCmndExec, Integer.parseInt(cmdSuccStatus));
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail("Failed while executing the command:: HELP TEST KEEP AGENTS.");
        }
    }


    /**
     * Method to test Help Command Merge
     * 
     * @param cmdSuccStatus
     */
    public void execSSTCmdHelpMerge(String cmdSuccStatus) {
        try {
            int statusCmndExec = 0;

            List<String> comparMsgs = new ArrayList<String>();
            comparMsgs.add("Usage: sourceDir destDir");
            comparMsgs
                .add("This tool merges a specified SmartStor directory (sourceDir) into another (destDir).");

            statusCmndExec =
                SSToolsUtil.executeSmartCommand(SSToolsConstants.HELP_CMD_MERGE, emToolsLoc,
                    comparMsgs);
            Assert.assertEquals(statusCmndExec, Integer.parseInt(cmdSuccStatus));
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail("Failed while executing the command:: HELP MERGE.");
        }
    }


    /**
     * Method to test existence of SmartStor Tools at proper location
     */
    public void checkSSToolsExistance() {
        try {
            boolean ssToolsExists = false;
            StringBuilder ssToolLocation = new StringBuilder();
            ssToolLocation.append(emToolsLoc);

            String operSysName = System.getProperty("os.name").toLowerCase();
            if (operSysName.contains("windows")) {
                ssToolLocation.append("/");
                ssToolLocation.append("SmartStorTools.bat");
            } else if (operSysName.contains("linux")) {
                ssToolLocation.append("/");
                ssToolLocation.append("SmartStorTools.sh");
            }
            File file = new File(ssToolLocation.toString());
            ssToolsExists = file.exists();
            Assert.assertTrue(ssToolsExists);

        } catch (Exception e) {
            Assert.fail("Failed not found 'SmartStorTools.bat'.");
        }
    }

    /**
     * Method to test Command KeepAgent functionality with invalid values for agent value
     * 
     * @param cmdSuccStatus
     * @param validDestLoc
     */
    public void execInvalidAgent(String cmdSuccStatus, String validDestLoc) {
        List<String> parameterLst = new ArrayList<String>();
        parameterLst.add((emToolsLoc + ApmbaseConstants.EM_DATA_LOC));
        parameterLst.add((emToolsLoc + validDestLoc));

        String[] commands =
            getDynamicCommand(SSToolsConstants.INVALID_CMD_AGENTS, parameterLst, null);

        try {
            int statusCmndExec = 0;
            statusCmndExec = SSToolsUtil.executeSmartCommand(commands, emToolsLoc, null);
            Assert.assertEquals(statusCmndExec, Integer.parseInt(cmdSuccStatus));
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail("Failed while executing the command:: HELP TEST KEEP AGENTS.");
        }
    }

    /**
     * Method to test command test_regex functionality with invalid value for Agent and source
     * directory
     * 
     * @param invalidAgntTestRegexErrMsg
     * @param invalidSrcTestRegexErrMsg
     * @param invalidDestLoc
     */
    public void execInvalidRegexTest(String invalidAgntTestRegexErrMsg,
        String invalidSrcTestRegexErrMsg, String invalidDestLoc) {
        List<String> errorMsgLst = new ArrayList<String>();
        errorMsgLst.add(invalidAgntTestRegexErrMsg);

        List<String> parameterLst = new ArrayList<String>();
        parameterLst.add(validEMDataLoc);

        try {
            int statusCmndExec = 0;

            String[] agentCommand =
                getDynamicCommand(SSToolsConstants.INVALID_AGENT_CMD_TEST_REGEX, parameterLst, null);
            statusCmndExec = SSToolsUtil.executeSmartCommand(agentCommand, emToolsLoc, errorMsgLst);


            parameterLst = new ArrayList<String>();
            parameterLst.add(invalidDestLoc);

            errorMsgLst = new ArrayList<String>();
            errorMsgLst.add(invalidSrcTestRegexErrMsg);

            String[] srcCommand =
                getDynamicCommand(SSToolsConstants.INVALID_SRC_CMD_TEST_REGEX, parameterLst, null);
            int srcStatus = SSToolsUtil.executeSmartCommand(srcCommand, emToolsLoc, errorMsgLst);

            if (statusCmndExec == 1 && srcStatus == 1) {
                Assert.assertTrue(true);
            } else {
                Assert.assertTrue(false);
            }


        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail("Failed while executing the command:: INVALID REGEX TEST.");
        }
    }

    /**
     * Method to test command for Invalid Prune functionality with source and destination
     * directories which are not present
     *
     * @param invalidSrcLoc
     * @param invalidDestLoc
     * @param pruneSrcErrMsg
     * @param pruneDestErrMsg
     */
    public void execInvalidPrune(String invalidSrcLoc, String invalidDestLoc,
        String pruneSrcErrMsg, String pruneDestErrMsg) {
        String invldSrcLoc = emPath + invalidSrcLoc;
        String invldDestLoc = emPath + invalidDestLoc;
        List<String> parameterLst = new ArrayList<String>();
        parameterLst.add(invldSrcLoc);
        parameterLst.add(invldDestLoc);

        List<String> errorMsgLst = new ArrayList<String>();
        errorMsgLst.add(pruneSrcErrMsg);

        try {
            String[] srcCmnd =
                getDynamicCommand(SSToolsConstants.INVALID_CMD_PRUNE_SRC, parameterLst, null);
            int statusSrcCmnd = SSToolsUtil.executeSmartCommand(srcCmnd, emToolsLoc, errorMsgLst);

            parameterLst.clear();
            parameterLst.add(emHome + ApmbaseConstants.EM_DATA_LOC);
            parameterLst.add(invldDestLoc);

            errorMsgLst.clear();
            errorMsgLst.add(pruneDestErrMsg);

            String[] destCmnd =
                getDynamicCommand(SSToolsConstants.INVALID_CMD_PRUNE_DEST, parameterLst, null);
            int statusDestCmnd = SSToolsUtil.executeSmartCommand(destCmnd, emToolsLoc, errorMsgLst);

            if (statusSrcCmnd == 1 && statusDestCmnd == 1) {
                Assert.assertTrue(true);
            } else {
                Assert.assertTrue(false);
            }
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail("Failed while executing the command:: INVALID PRUNE.");
        }
    }

    /**
     * Method to test command for Keep Agents functionality with source and destination directories
     * which are not present
     * 
     * @param cmdSuccStatus
     * @param invalidSrcLoc
     * @param invalidDestLoc
     * @param invalidAgntDirsErrMsg
     */
    public void execInvalidAgentDirs(String cmdSuccStatus, String agentName, String invalidSrcLoc,
        String invalidDestLoc, String invalidAgntDirsErrMsg) {
        String invldSrcLoc = emPath + invalidSrcLoc;
        String invldDestLoc = emPath + invalidDestLoc;

        List<String> parameterLst = new ArrayList<String>();
        parameterLst.add(invldSrcLoc);
        parameterLst.add(invldDestLoc);

        List<String> errorMsgLst = new ArrayList<String>();
        errorMsgLst.add(invalidAgntDirsErrMsg);

        try {
            int statusCmndExec = 0;
            String[] command =
                getDynamicCommand(SSToolsConstants.INVALID_CMD_AGENT_DIRS, parameterLst, agentName);
            statusCmndExec = SSToolsUtil.executeSmartCommand(command, emToolsLoc, errorMsgLst);
            Assert.assertEquals(statusCmndExec, Integer.parseInt(cmdSuccStatus));
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail("Failed while executing the command:: INVALID PRUNE.");
        }
    }

    /**
     * Method to test command for Removes Agents functionality with source and destination
     * directories which are not present
     * 
     * @param cmdSuccStatus
     * @param invalidSrcLoc
     * @param invalidDestLoc
     * @param remInvalAgntDirsErrMsg
     */
    public void execRemoveAgntsInvalidDirs(String cmdSuccStatus, String agentName,
        String invalidSrcLoc, String invalidDestLoc, String remInvalAgntDirsErrMsg) {
        String invldSrcLoc = emPath + invalidSrcLoc;
        String invldDestLoc = emPath + invalidDestLoc;
        List<String> parameterLst = new ArrayList<String>();
        parameterLst.add(invldSrcLoc);
        parameterLst.add(invldDestLoc);
        List<String> errorMsgLst = new ArrayList<String>();
        errorMsgLst.clear();
        errorMsgLst.add(remInvalAgntDirsErrMsg);

        try {
            int statusCmndExec = 0;

            String[] command =
                getDynamicCommand(SSToolsConstants.INVALID_CMD_REMOVE_AGENT_DIRS, parameterLst,
                    agentName);
            statusCmndExec = SSToolsUtil.executeSmartCommand(command, emToolsLoc, errorMsgLst);
            Assert.assertEquals(statusCmndExec, Integer.parseInt(cmdSuccStatus));
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail("Failed while executing the command:: REMOVE AGENTS INVALID DIRS.");
        }
    }


    public void checkMetricsUsingTestRegex(String metricName) {

        List<String> parameterLst = new ArrayList<String>();
        parameterLst.add(emPath + "/data");
        int linesWithoutEMRunning = 0;
        boolean flag = false;
        try {
            String[] commands =
                getDynamicCommand(SSToolsConstants.TEST_REGEX_METRICS_FUN, parameterLst, metricName);
            BufferedReader reader = null;
            Process process = null;
            try {
                process = ApmbaseUtil.getProcess(commands, emToolsLoc);
                reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
                String line = reader.readLine();
                while ((line) != null) {
                    linesWithoutEMRunning++;
                    line = reader.readLine();
                }
            } catch (Exception e) {
                reader.close();
                process.destroy();
                e.printStackTrace();
            }
            if (linesWithoutEMRunning > 8) flag = true;
            Assert.assertTrue(flag);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void checkAgentsUsingTestRegex(String agentName) {

        List<String> parameterLst = new ArrayList<String>();
        parameterLst.add(emPath + "/data");;
        int linesWithoutEMRunning = 0;
        boolean flag = false;
        try {
            String[] commands =
                getDynamicCommand(SSToolsConstants.TEST_REGEX_AGENT_FUN, parameterLst, agentName);
            BufferedReader reader = null;
            Process process = null;
            try {
                process = ApmbaseUtil.getProcess(commands, emToolsLoc);
                reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
                String line = reader.readLine();
                while ((line) != null) {
                    linesWithoutEMRunning++;
                    line = reader.readLine();
                }
            } catch (Exception e) {
                reader.close();
                process.destroy();
                e.printStackTrace();
            }
            if (linesWithoutEMRunning > 8) flag = true;
            Assert.assertTrue(flag);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void removeAgentsWrongSrcAndWrongDest(String agentName) {
        List<String> wrongDirList = null;
        wrongDirList = new ArrayList<String>();
        wrongDirList.add("/wrongDir1");
        wrongDirList.add("/wrongDir2");
        boolean flag = false;
        try {
            String[] commands =
                getDynamicCommand(SSToolsConstants.REMOVE_AGENTS_VALID_PARAMS, wrongDirList,
                    agentName);
            BufferedReader reader = null;
            Process process = null;
            try {
                process = ApmbaseUtil.getProcess(commands, emToolsLoc);
                reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
                String line = reader.readLine();
                while ((line) != null) {
                    LOGGER.info("+++++++++++" + line + "+++++++++++");
                    if (line.toLowerCase().contains("(-src)")) {
                        if (line.contains("wrongDir1")) {
                            flag = true;
                        }
                    }
                    line = reader.readLine();
                }
            } catch (Exception e) {
                reader.close();
                process.destroy();
                e.printStackTrace();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        Assert.assertTrue(flag);
    }

    // .*Enterprise Manager.*
    // No Matching Metrics are found in Smartstor

    public void removemetricsVerify(String metricName) {
        List<String> parameterLst = new ArrayList<String>();
        parameterLst.add(emPath + "/data");
        parameterLst.add(emPath + "/data_1");
        boolean flag = false;
        try {
            String[] commands =
                getDynamicCommand(SSToolsConstants.REMOVE_METRICS_VALID_PARAMS, parameterLst,
                    metricName);
            BufferedReader reader = null;
            Process process = null;
            File file = new File(emPath + "/data_1");
            if (!file.exists()) file.mkdirs();
            try {
                process = ApmbaseUtil.getProcess(commands, emToolsLoc);
                reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
                String line = reader.readLine();
                while ((line) != null) {
                    LOGGER.info("+++++++++++" + line + "+++++++++++");
                    if (line.contains("Pruning metric named")) {
                        flag = true;
                    }
                    line = reader.readLine();
                }
            } catch (Exception e) {
                reader.close();
                process.destroy();
                e.printStackTrace();
            }
            parameterLst.clear();
            parameterLst.add(emPath + "/data_1");

            commands =
                getDynamicCommand(SSToolsConstants.REMOVE_METRICS_CHECK_DEST, parameterLst,
                    metricName);
            try {
                process = ApmbaseUtil.getProcess(commands, emToolsLoc);
                reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
                String line = reader.readLine();
                while ((line) != null) {
                    LOGGER.info("+++++++++++" + line + "+++++++++++");
                    if (line.contains("No Matching Metrics are found in Smartstor")) {
                        flag = true;
                    }
                    line = reader.readLine();
                }
            } catch (Exception e) {
                reader.close();
                process.destroy();
                e.printStackTrace();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        Assert.assertTrue(flag);
    }
    
public void keepAgentsVerify(String agentName) {
        List<String> parameterLst = new ArrayList<String>();
        parameterLst.add(emPath + "/data");
        parameterLst.add(emPath + "/data_2");
        boolean flag = false;
        try {
            String[] commands =
                getDynamicCommand(SSToolsConstants.KEEP_AGENTS_VALID_PARAMS, parameterLst,
                    agentName);
            BufferedReader reader = null;
            Process process = null;
            File file = new File(emPath + "/data_2");
            if (!file.exists()) file.mkdirs();
            try {
                process = ApmbaseUtil.getProcess(commands, emToolsLoc);
                reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
                String line = reader.readLine();
                while ((line) != null) {
                    LOGGER.info("+++++++++++" + line + "+++++++++++");
                    if (line.contains("Pruning agent named")) {
                        flag = true;
                    }
                    line = reader.readLine();
                }
            } catch (Exception e) {
                reader.close();
                process.destroy();
                e.printStackTrace();
            }
            parameterLst.clear();
            parameterLst.add(emPath + "/data_2");

            commands =
                getDynamicCommand(SSToolsConstants.TEST_REGEX_FUN_VALID_PARAMS_AGENTS, parameterLst,
                    "");
            try {
                process = ApmbaseUtil.getProcess(commands, emToolsLoc);
                reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
                String line = reader.readLine();
                while ((line) != null) {
                    LOGGER.info("+++++++++++" + line + "+++++++++++");
                    if (line.contains("Tomcat")) {
                        flag = true;
                    }
                    line = reader.readLine();
                }
            } catch (Exception e) {
                reader.close();
                process.destroy();
                e.printStackTrace();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        Assert.assertTrue(flag);
    }
    
    public void keepAgentsWrongSrcAndWrongDest(String agentName) {
        List<String> wrongDirList = null;
        wrongDirList = new ArrayList<String>();
        wrongDirList.add("/wrongDir1");
        wrongDirList.add("/wrongDir2");
        boolean flag = false;
        try {
            String[] commands =
                getDynamicCommand(SSToolsConstants.KEEP_AGENTS_VALID_PARAMS, wrongDirList,
                    agentName);
            BufferedReader reader = null;
            Process process = null;
            try {
                process = ApmbaseUtil.getProcess(commands, emToolsLoc);
                reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
                String line = reader.readLine();
                while ((line) != null) {
                    LOGGER.info("+++++++++++" + line + "+++++++++++");
                    if (line.toLowerCase().contains("(-src)")) {
                        if (line.contains("wrongDir1")) {
                            flag = true;
                        }
                    }
                    line = reader.readLine();
                }
            } catch (Exception e) {
                reader.close();
                process.destroy();
                e.printStackTrace();
            }
            wrongDirList.clear();
            wrongDirList.add(validEMDataLoc);
            wrongDirList.add("/wrongDir2");
            commands =
                getDynamicCommand(SSToolsConstants.KEEP_AGENTS_VALID_PARAMS, wrongDirList,
                    agentName);
            
            try {
                process = ApmbaseUtil.getProcess(commands, emToolsLoc);
                reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
                String line = reader.readLine();
                while ((line) != null) {
                    LOGGER.info("+++++++++++" + line + "+++++++++++");
                    if (line.toLowerCase().contains("(-backup)")) {
                        if (line.contains("wrongDir2")) {
                            flag = true;
                        }
                    }
                    line = reader.readLine();
                }
            } catch (Exception e) {
                reader.close();
                process.destroy();
                e.printStackTrace();
            }
            
        } catch (Exception e) {
            e.printStackTrace();
        }
        Assert.assertTrue(flag);
    }
    
    
    public void keepAgents_inValidParams(String agentName, String validDestLoc) {

        String valdDestLoc = emPath + validDestLoc;

        File file = new File(valdDestLoc);
        if (!file.exists()) file.mkdirs();

        List<String> parameterLst = new ArrayList<String>();
        parameterLst.add(validEMDataLoc);
        parameterLst.add(valdDestLoc);
        boolean flag=false;
        try {
        String[] commands =
                getDynamicCommand(SSToolsConstants.KEEP_AGENTS_VALID_PARAMS, parameterLst,
                    agentName);
            BufferedReader reader = null;
            Process process = null;
            try {
                process = ApmbaseUtil.getProcess(commands, emToolsLoc);
                reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
                String line = reader.readLine();
                while ((line) != null) {
                    LOGGER.info("+++++++++++" + line + "+++++++++++");
                    if (line.contains("Pruning agent named")) {
                            flag = true;
                        }
                    line = reader.readLine();
                }
            } catch (Exception e) {
                reader.close();
                process.destroy();
                e.printStackTrace();
            }
            if(flag)
            {
                parameterLst.clear();
                parameterLst.add(valdDestLoc);
                commands =
                getDynamicCommand(SSToolsConstants.TEST_REGEX_AGENT_FUN, parameterLst,
                    agentName);
                try {
                process = ApmbaseUtil.getProcess(commands, emToolsLoc);
                reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
                String line = reader.readLine();
                while ((line) != null) {
                    LOGGER.info("+++++++++++" + line + "+++++++++++");
                    if (line.contains("No Matching Agents are found in Smartstor")) {
                            flag = true;
                        }
                    line = reader.readLine();
                }
            } catch (Exception e) {
                reader.close();
                process.destroy();
                e.printStackTrace();
            }
            }
            
            /** removing the temporary directory at the end of test case */
            ApmbaseUtil.deleteDir(file);
        } catch (Exception e) {
            Assert
                .fail("Failed while executing the command:: REMOVE AGENTS WITH VALID PARAMETERS.");
        }
    }

    



    /**
     * It will generate a dynamic command based on this list
     * 
     * @param orgCommand
     * @param commands
     * @return
     */
    private String[] getDynamicCommand(String[] orgCommand, List<String> commands, String agent) {
        StringBuilder sb = new StringBuilder();
        String[] myCommand = new String[orgCommand.length];
        for (int i = 0; i < orgCommand.length; i++)
            myCommand[i] = orgCommand[i];
        int j = 0;
        for (int i = 0; i < myCommand.length; i++) {
            if (myCommand[i].equalsIgnoreCase("DYNAMIC")) {
                LOGGER.info(j + "-----" + commands.get(j));
                myCommand[i] = commands.get(j);
                j++;
            }
            if ("DYAGENT".equalsIgnoreCase(myCommand[i])) {
                myCommand[i] = "\".*" + agent + ".*\"";
            }
            sb.append(myCommand[i]);
        }
        LOGGER.info("complete command  ::::::::::::::::: " + sb.toString());
        return myCommand;
    }

    /*private CLWBean getClwBeanInstance(String emhost, String emport, String emuser, String empassw) {
        if (clwBean == null) {
            clwBean = new CLWBean(emhost, emuser, empassw, Integer.parseInt(emport), clwJarFileLoc);
        }
        return clwBean;
    }*/

    public void renameDataFolder() {
        try {
            File file = new File(validEMDataLoc);
            ApmbaseUtil.deleteDir(file);
            // Assert.assertEquals(statusCmndExec, Integer.parseInt(cmdSuccStatus));
        } catch (Exception e) {
            Assert.fail("Failed while deleting days directory.");
        }
    }

    /**
     * Method to test command test_regex functionality with invalid value for
     * Agent and source directory
     * 
     * @param cmdSuccStatus - command success status - checks for value "1"
     * @param invalidAgntTestRegexErrMsg
     */
    public void execInvalidRegexTest(String cmdSuccStatus, String invalidAgntTestRegexErrMsg) {
        List<String> errorMsgLst = new ArrayList<String>();
        errorMsgLst.add(invalidAgntTestRegexErrMsg);

        List<String> parameterLst = new ArrayList<String>();
        parameterLst.add(validEMDataLoc);

        try {
            int statusCmndExec = 0;
            String[] commands =
                getDynamicCommand(SSToolsConstants.INVALID_AGENT_CMD_TEST_REGEX, parameterLst, null);
            statusCmndExec = SSToolsUtil.executeSmartCommand(commands, emToolsLoc, errorMsgLst);
            Assert.assertEquals(statusCmndExec, Integer.parseInt(cmdSuccStatus));
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail("Failed while executing the command:: INVALID REGEX TEST.");
        }
    }


    /**
     * This test method refers to test case - APM 9.0/Minor Releases/APM 9.1/Regression/APM
     * Base/EM/Smart Stor tools/Merge functionality with source and destination directories which
     * are not present
     * Merging source and destination which are not present
     * 
     * @param cmdSuccStatus - command success status - checks for value "1"
     * @param readMsg - message - "Cannot read &amp; or it does not exist."
     * @param invalidSrcLoc - Invalid source directory - /dummy
     * @param validDestLoc - Valid destination directory- /dummy2
     */
    public void mergeFun_invalidSource(String cmdSuccStatus, String readMsg, String invalidSrcLoc,
        String validDestLoc) {

        String inSrcLoc = (emPath + invalidSrcLoc);
        String valDestLoc = (emPath + validDestLoc);

        File file = new File(valDestLoc);
        if (!file.exists()) file.mkdirs();

        List<String> parameterLst = new ArrayList<String>();
        parameterLst.add(inSrcLoc);
        parameterLst.add(valDestLoc);

        String[] msgLst = readMsg.split("&");

        List<String> readErrorMsgLst = new ArrayList<String>();
        readErrorMsgLst.add(msgLst[0]);
        try {
            int statusCmndExec = 0;

            String[] commands =
                getDynamicCommand(SSToolsConstants.MERGE_FUN_SRC_AND_DEST_NOT_EXISTS_SRC,
                    parameterLst, null);
            statusCmndExec = SSToolsUtil.executeSmartCommand(commands, emToolsLoc, readErrorMsgLst);
            ApmbaseUtil.deleteDir(file);

            Assert.assertEquals(statusCmndExec, Integer.parseInt(cmdSuccStatus));
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail("Failed while executing the command:: INVALID REGEX TEST.");
        }
    }

    /**
     * This test method refers to test case - APM 9.0/Minor Releases/APM 9.1/Regression/APM
     * Base/EM/Smart Stor tools/Merge functionality with source and destination directories which
     * are not present
     * Merging source and destination which are not present
     * 
     * @param cmdSuccStatus - command success status - checks for value "1"
     * @param writeMsg - message "Cannot write to the destination directory"
     * @param invalidDestLoc 0 Invalid Destination directory - /dummy1
     */
    public void mergeFun_invalidDestination(String cmdSuccStatus, String writeMsg,
        String invalidDestLoc) {

        String valDestLoc = (emPath + invalidDestLoc);
        List<String> parameterLst = new ArrayList<String>();
        parameterLst.add(validEMDataLoc);
        parameterLst.add(valDestLoc);

        List<String> readErrorMsgLst = new ArrayList<String>();
        readErrorMsgLst.add(writeMsg);

        try {
            int statusCmndExec = 0;

            String[] command =
                getDynamicCommand(SSToolsConstants.MERGE_FUN_SRC_AND_DEST_NOT_EXISTS_DEST,
                    parameterLst, null);
            statusCmndExec = SSToolsUtil.executeSmartCommand(command, emToolsLoc, readErrorMsgLst);
            Assert.assertEquals(statusCmndExec, Integer.parseInt(cmdSuccStatus));

        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail("Failed while executing the command:: INVALID REGEX TEST.");
        }
    }

    /**
     * Method to test test_regex functionality with Agents and metrics
     * expression
     * 
     * @param cmdSuccStatus - command success status - checks for value "1"
     * @param agentName
     */
    public void testRegexFunAgentMetricExp(String cmdSuccStatus, String agentName) {

        List<String> parameterLst = new ArrayList<String>();
        parameterLst.add(validEMDataLoc);

        try {
            List<String> successMsgLst = new ArrayList<String>();
            successMsgLst.add(agentName);
            String[] command =
                getDynamicCommand(SSToolsConstants.TEST_REGEX_AGENT_FUN, parameterLst, agentName);
            int statusAgent = SSToolsUtil.executeSmartCommand(command, emToolsLoc, successMsgLst);

            successMsgLst = new ArrayList<String>();
            successMsgLst.add(agentName);
            String[] commandExp =
                getDynamicCommand(SSToolsConstants.TEST_REGEX_METRICS_FUN, parameterLst, agentName);
            int statusMetric =
                SSToolsUtil.executeSmartCommand(commandExp, emToolsLoc, successMsgLst);

            int expStatus = Integer.parseInt(cmdSuccStatus);
            if ((statusAgent == expStatus) && (statusMetric == expStatus)) {
                Assert.assertTrue(true);
            } else {
                Assert.assertTrue(false);
            }

        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail("Failed while executing the command:: REMOVE AGENTS INVALID DIRS.");
        }
    }

    /**
     * Method to test prune functionality with valid parameters
     * 
     * @param cmdSuccStatus
     * @param backUpDataLoc
     * @param bckFileName
     */
    public void pruneFun_validParams(String cmdSuccStatus, String backUpDataLoc, String bckFileName) {

        int isBackupComp = 0;
        String valDestLoc = (emPath + backUpDataLoc);
        File file = new File(valDestLoc);
        if (!file.exists()) file.mkdirs();

        try {

            List<String> parameterLst = new ArrayList<String>();
            parameterLst.add(valDestLoc);
            parameterLst.add(validEMDataLoc);

            String[] commands =
                getDynamicCommand(SSToolsConstants.PRUNE_FUN_VALID_PARAM, parameterLst, null);
            int k = SSToolsUtil.executeSmartCommand(commands, emToolsLoc, null);
            LOGGER.info("Smart command returned : " + k);
            // It requires some 30 sec of delay to execute the command
            harvestWait(30);
            String[] lstOfFile = file.list();
            LOGGER.info("" + Arrays.asList(lstOfFile));
            for (int i = 0; i < lstOfFile.length; i++) {
                if (lstOfFile[i].equals(bckFileName)) {
                    isBackupComp = 1;
                }
            }
            ApmbaseUtil.deleteDir(file);
            // It requires 30 sec of time before asseritng after delete directory
            harvestWait(30);
            Assert.assertEquals(isBackupComp, Integer.parseInt(cmdSuccStatus));

        } catch (Exception e) {
            e.printStackTrace();
            Assert
                .fail("Failed while executing the command:: PRUNE FUNCTIONALITY WITH VALID PARAMETERS.");
        } finally {

        }
    }



    /**
     * Test method to Remove Agents functionality with valid parameters
     * 
     * @param agentName
     * @param validDestLoc
     */
    public void removeAgent_validParams(String agentName, String validDestLoc) {

        String valdDestLoc = emPath + validDestLoc;

        File file = new File(valdDestLoc);
        if (!file.exists()) file.mkdirs();

        List<String> parameterLst = new ArrayList<String>();
        parameterLst.add(validEMDataLoc);
        parameterLst.add(valdDestLoc);

        try {
            String[] removeCommand =
                getDynamicCommand(SSToolsConstants.REMOVE_AGENTS_VALID_PARAMS, parameterLst,
                    agentName);
            SSToolsUtil.executeSmartCommand(removeCommand, emToolsLoc, null);
            // It requires 15 sec of delay to execute above command
            harvestWait(15);

            List<String> successMsgLst = new ArrayList<String>();
            successMsgLst.add(agentName);
            parameterLst = new ArrayList<String>();
            parameterLst.add(validEMDataLoc);

            String[] sourceCmnd =
                getDynamicCommand(SSToolsConstants.REMOVE_AGENTS_CHECK_SRC, parameterLst, agentName);
            int statusSrc = SSToolsUtil.executeSmartCommand(sourceCmnd, emToolsLoc, successMsgLst);
            // It requires 15 sec of delay to execute above command
            harvestWait(15);

            parameterLst = new ArrayList<String>();
            parameterLst.add(valdDestLoc);
            successMsgLst = new ArrayList<String>();
            successMsgLst.add(agentName);
            String[] destCmnd =
                getDynamicCommand(SSToolsConstants.REMOVE_AGENTS_CHECK_DEST, parameterLst,
                    agentName);
            int statusDest = SSToolsUtil.executeSmartCommand(destCmnd, emToolsLoc, successMsgLst);
            // It requires 15 sec of delay to execute above command
            harvestWait(15);
            /** removing the temporary directory at the end of test case */
            ApmbaseUtil.deleteDir(file);

            if ((statusSrc == 1) && (statusDest == 0))
                Assert.assertTrue(true);
            else
                Assert.assertTrue(false);

        } catch (Exception e) {
            Assert
                .fail("Failed while executing the command:: REMOVE AGENTS WITH VALID PARAMETERS.");
        }
    }


    /**
     * Test method to Remove Agents functionality with Invalid parameters
     * 
     * @param agentName
     * @param validDestLoc
     */
    public void removeAgent_inValidParams(String agentName, String validDestLoc) {

        String valdDestLoc = emPath + validDestLoc;

        File file = new File(valdDestLoc);
        if (!file.exists()) file.mkdirs();

        List<String> parameterLst = new ArrayList<String>();
        parameterLst.add(validEMDataLoc);
        parameterLst.add(valdDestLoc);

        try {
            String[] removeCommand =
                getDynamicCommand(SSToolsConstants.REMOVE_AGENTS_VALID_PARAMS, parameterLst,
                    agentName);
            SSToolsUtil.executeSmartCommand(removeCommand, emToolsLoc, null);
            // It requires 15 sec of delay to execute above command
            harvestWait(15);

            List<String> successMsgLst = new ArrayList<String>();
            successMsgLst.add(agentName);
            parameterLst.clear();
            parameterLst.add(valdDestLoc);
            successMsgLst = new ArrayList<String>();
            successMsgLst.add(agentName);

            String[] destCmnd =
                getDynamicCommand(SSToolsConstants.REMOVE_AGENTS_CHECK_DEST, parameterLst,
                    agentName);
            int statusDest = SSToolsUtil.executeSmartCommand(destCmnd, emToolsLoc, successMsgLst);
            // It requires 15 sec of delay to execute above command
            harvestWait(15);
            /** removing the temporary directory at the end of test case */
            ApmbaseUtil.deleteDir(file);

            if ((statusDest == 1))
                Assert.assertTrue(false);
            else
                Assert.assertTrue(true);
            parameterLst.clear();
        } catch (Exception e) {
            Assert
                .fail("Failed while executing the command:: REMOVE AGENTS WITH VALID PARAMETERS.");
        }
    }

    /**
     * Method to test the remove metrics functionality with all valid parameters
     * 
     * @param validDestLoc
     * @param agentSocket
     */
    public void removeMetrics_validParams(String validDestLoc, String agentSocket) {
        boolean isMetricFound = false;
        String valdDestLoc = emPath + validDestLoc;
        File file = new File(valdDestLoc);
        if (!file.exists()) file.mkdirs();

        List<String> parameterLst = new ArrayList<String>();
        parameterLst.add(validEMDataLoc);
        parameterLst.add(valdDestLoc);

        List<String> succMsgLst = new ArrayList<String>();
        succMsgLst.add(agentSocket);

        try {

            /** checking source folder for metrics */
            String[] sourceCmnd =
                getDynamicCommand(SSToolsConstants.REMOVE_METRICS_CHECK_SRC, parameterLst,
                    agentSocket);
            int srcCmndStatus = SSToolsUtil.executeSmartCommand(sourceCmnd, emToolsLoc, succMsgLst);
            // It requires 30 sec of delay to execute above command
            harvestWait(30);

            /** Removing metrics and copying to destination folder */
            String[] removeCommand =
                getDynamicCommand(SSToolsConstants.REMOVE_METRICS_VALID_PARAMS, parameterLst,
                    agentSocket);
            SSToolsUtil.executeSmartCommand(removeCommand, emToolsLoc, null);
            // It requires 30 sec of delay to execute above command
            harvestWait(30);

            /** checking metrics in destination folder */
            parameterLst = new ArrayList<String>();
            parameterLst.add(valdDestLoc);
            succMsgLst = new ArrayList<String>();
            succMsgLst.add(agentSocket);

            String[] destCmnd =
                getDynamicCommand(SSToolsConstants.REMOVE_METRICS_CHECK_DEST, parameterLst,
                    agentSocket);
            int destCmndStatus = SSToolsUtil.executeSmartCommand(destCmnd, emToolsLoc, succMsgLst);
            // It requires 30 sec of delay to execute above command
            harvestWait(30);

            if ((srcCmndStatus == 1) && (destCmndStatus == 0)) {
                isMetricFound = false;
            } else {
                isMetricFound = true;
            }
            /** removing the temporary directory at the end of test case */
            ApmbaseUtil.deleteDir(file);
            Assert.assertFalse(isMetricFound);

        } catch (Exception e) {
            Assert
                .fail("Failed while executing the command:: REMOVE AGENTS WITH VALID PARAMETERS.");
        }
    }

    /**
     * Below method will check whether the Dot data files present in the data
     * folder of EM. If it doesn't find the Dot data file it will restart the
     * EM.
     * 
     * @param emhost
     * @param emport
     * @param emuser
     * @param empassw
     */
    public void checkDotDataFile(String emhost, String emport, String emuser, String empassw) {
        boolean isSpoolFileConvData = false;

        LOGGER.info(validEMDataLoc);

        File file = new File(validEMDataLoc);
        String[] lstOfFile = file.list();
        if (lstOfFile.length > 0) {
            for (int i = 0; i < lstOfFile.length; i++) {

                if (lstOfFile[i].toLowerCase().contains(".data")) {
                    isSpoolFileConvData = true;
                    break;
                }
            }
        }
        if (!isSpoolFileConvData) {
            try {
                startEM();
                isSpoolFileConvData = true;
            } catch (Exception e) {
                Assert.fail("EM has not ReStarted Properly.");
            }
        }
        Assert.assertTrue(isSpoolFileConvData);
    }

    /**
     * Test method to the test case - APM 9.0/Minor Releases/APM 9.1/Regression/APM Base/EM/Smart
     * Stor tools/test_regex functionality with all valid parameters
     * Used to execute the smart store command and verifies metric and agent status.
     * 
     * @param cmdSuccStatus - command success status - checks for value "1"
     * @param agentName - Agent name - weblogic
     */
    public void testRegexFun_validParams(String cmdSuccStatus, String agentName) {

        List<String> parameterLst = new ArrayList<String>();
        parameterLst.add(validEMDataLoc);

        List<String> succMsgLst = new ArrayList<String>();
        succMsgLst.add(agentName);

        try {

            LOGGER.info("sleeping for 30 secs...");
            harvestWait(30);

            String[] metricCmnd =
                getDynamicCommand(SSToolsConstants.TEST_REGEX_FUN_VALID_PARAMS_METRICS,
                    parameterLst, agentName);
            LOGGER.info("metricCmnd == " + metricCmnd);
            LOGGER.info("emToolsLoc == " + emToolsLoc);
            LOGGER.info("succMsgLst == " + succMsgLst);
            int metricStatus = SSToolsUtil.executeSmartCommand(metricCmnd, emToolsLoc, succMsgLst);
            LOGGER.info("metricStatus == " + metricStatus);
            // It requires 15 sec of delay to execute above command
            harvestWait(15);

            succMsgLst = new ArrayList<String>();
            succMsgLst.add(agentName);
            String[] agentCmnd =
                getDynamicCommand(SSToolsConstants.TEST_REGEX_FUN_VALID_PARAMS_AGENTS,
                    parameterLst, agentName);
            LOGGER.info("agentCmnd == " + agentCmnd);
            LOGGER.info("emToolsLoc == " + emToolsLoc);
            LOGGER.info("succMsgLst == " + succMsgLst);
            int agentStatus = SSToolsUtil.executeSmartCommand(agentCmnd, emToolsLoc, succMsgLst);
            LOGGER.info("agentStatus == " + agentStatus);
            // It requires 15 sec of delay to execute above command
            harvestWait(15);

            int expStatus = Integer.parseInt(cmdSuccStatus);
            if ((metricStatus == expStatus) && (agentStatus == expStatus)) {
                Assert.assertTrue(true);
            } else {
                Assert.assertTrue(false);
            }

        } catch (Exception e) {
            e.printStackTrace();
            Assert
                .fail("Failed while executing the command:: TEST_REGEX FUNCTIONALITY WITH VALID PARAMETERS.");
        }
    }



    /**
     * updating the Agent profile
     */
    public void updateAgentProfile(String autonaming, String agentName) {

        try {
            // update agent profile
            String agentProfilePath =
                System.getProperty("role_agent.install.dir") + "/core/config/"
                    + System.getProperty("role_agent.agent.profile");

            Properties properties = Util.loadPropertiesFile(agentProfilePath);
            properties.setProperty(AutomationConstants.AGENT_AUTONAMING_PROPERTY, autonaming);
            properties.setProperty(AutomationConstants.AGENT_NAME_PROPERTY, agentName);

            LOGGER.info("*** agentName: ** " + agentName);
            LOGGER.info("*** autonaming: ** " + autonaming);
            Util.writePropertiesToFile(agentProfilePath, properties);
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail("Test failed because of the following reason: ", e);
        }
    }
}
