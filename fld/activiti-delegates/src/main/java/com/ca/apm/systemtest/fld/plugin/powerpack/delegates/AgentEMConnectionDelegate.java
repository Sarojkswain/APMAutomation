package com.ca.apm.systemtest.fld.plugin.powerpack.delegates;

import java.io.BufferedReader;
import java.io.StringReader;
import java.text.MessageFormat;

import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.VariableScope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import com.ca.apm.systemtest.fld.common.logmonitor.FldLogger;
import com.ca.apm.systemtest.fld.plugin.AbstractJavaDelegate;
import com.ca.apm.systemtest.fld.plugin.NodeManager;
import com.ca.apm.systemtest.fld.plugin.em.EmPlugin;
import com.ca.apm.systemtest.fld.plugin.em.EmPlugin.CLWResult;
import com.ca.apm.systemtest.fld.plugin.powerpack.common.PowerPackConstants;
import com.ca.apm.systemtest.fld.proxy.AgentProxyFactory;

/**
 * Java delegate to check if an Introscope agent is connected to an Enterprise Manager.
 * 
 * @author bocto01
 *
 */
public class AgentEMConnectionDelegate extends AbstractJavaDelegate {

    public static final String LOG_CATEGORY = AgentEMConnectionDelegate.class.getSimpleName();

    private static final Logger LOGGER = LoggerFactory.getLogger(AgentEMConnectionDelegate.class);

    static final String CLW_COMMAND = "list agents matching \"{0}\\|{1}\\|{2}\"";

    
    public AgentEMConnectionDelegate(NodeManager nodeManager, AgentProxyFactory agentProxyFactory, FldLogger fldLogger) {
        super(nodeManager, agentProxyFactory, fldLogger);
    }

    @Override
    protected void handleExecution(DelegateExecution execution) throws Throwable {
        checkAgentConnectedEM(execution);
    }

    protected void checkAgentConnectedEM(VariableScope execution) {
        final String serverType = getEnumExecutionVariable(execution, PowerPackConstants.SERVER_TYPE_PARAM_NAME);
        final String testType = getEnumExecutionVariable(execution, PowerPackConstants.TEST_TYPE_PARAM_NAME);

        if (PowerPackConstants.NO_AGENT_TEST_TYPE.equals(testType)) {
            logInfo(LOG_CATEGORY, serverType, "No need to check agent connection to EM since it is a no-agent test run, skipping.");
            return;
        }
        
        
        Boolean agentConnectedEM = null;

        String emNode = getNodeExecutionVariable(execution, PowerPackConstants.MOM_NODE_PARAM_NAME);
        String agentNode = getNodeExecutionVariable(execution, NODE);
        String agentHostName = getStringByGetter(execution, NODE, "getHostName", agentNode);

        agentHostName = cutOffDomain(agentHostName);
        final String agentName = getStringExecutionVariable(execution, PowerPackConstants.AGENT_NAME_PARAM_NAME);
        final String agentVersion = getStringExecutionVariable(execution, PowerPackConstants.AGENT_VERSION_PARAM_NAME);
        final String agentEmInvestigatorGroupName = getStringExecutionVariable(execution, PowerPackConstants.AGENT_EM_INVESTIGATOR_GROUP_NAME_PARAM_NAME);
        
        String clwCommand =
            MessageFormat.format(CLW_COMMAND, agentHostName, agentEmInvestigatorGroupName, agentName);
        
        StringBuffer buf = new StringBuffer("Checking Introscope Agent is connected to the EM.").append('\n')
            .append("Introscope Agent Node: {0}").append('\n')
            .append("Introscope Agent Host: {1}").append('\n')
            .append("Introscope Agent Name: {2}").append('\n')
            .append("Introscope Agent Version: {3}").append('\n')
            .append("Name of the Group under which Introscope Agent appears in EM Investigator: {4}").append('\n')
            .append("EM Node: {5}").append('\n')
            .append("Test Type: {6}").append('\n')
            .append("CLW Command: {7}");
            
        logInfo(LOG_CATEGORY, serverType, buf.toString(), agentNode, agentHostName, agentName, agentVersion, 
            agentEmInvestigatorGroupName, emNode, testType, clwCommand);

        try {
            EmPlugin emPlugin = getPluginForNode(emNode, EmPlugin.PLUGIN, EmPlugin.class);
            CLWResult clwResult = emPlugin.executeCLWCommand(clwCommand);
            Integer clwExitCode = clwResult.exitCode;
            String clwOutput = clwResult.output;
            logDebug(LOG_CATEGORY, serverType, "clwExitCode = {0}", clwExitCode);
            logDebug(LOG_CATEGORY, serverType, "clwOutput = {0}", clwOutput);

            if (clwExitCode != null && clwExitCode == 0) { // = OK
                // e.g., "aqpp-wls01|WebLogic|weblogicportal103"
                String currentAgentLine = agentHostName + "|" + agentEmInvestigatorGroupName + "|" + agentName;
                logDebug(LOG_CATEGORY, serverType, "currentAgentLine = {0}", currentAgentLine);

                String line;
                boolean emptyResponse = true;
                try (BufferedReader br = new BufferedReader(new StringReader(clwOutput))) {
                    logDebug(LOG_CATEGORY, serverType, "Start reading CLW output");

                    while ((line = br.readLine()) != null) {
                        logDebug(LOG_CATEGORY, serverType, "> {0}", line);
                        emptyResponse = false;
                        if (currentAgentLine.equals(line)) {
                            logDebug(LOG_CATEGORY, serverType, "Current agent line found!");
                            agentConnectedEM = Boolean.TRUE;
                            break;
                        } else {
                            agentConnectedEM = Boolean.FALSE;
                        }
                    }
                    logDebug(LOG_CATEGORY, serverType, "Finish reading CLW output");
                }

                if (emptyResponse) {
                    agentConnectedEM = Boolean.FALSE;
                }
            }

            logDebug(LOG_CATEGORY, serverType, "agentConnectedEM = {0}", agentConnectedEM);
            if (agentConnectedEM == null) {
                logError(
                    LOG_CATEGORY,
                    serverType,
                    "Could not check whether agent is connected to EM. No output from CLW received!");
            } else {
                switch (testType) {
                    case "Agent"://legacy type
                    case "AgentPlusPP"://legacy type
                    case "oldAgent":
                    case "newAgent":
                    case "oldAgentPlusPP":
                    case "newAgentPlusPP": {
                        if (agentConnectedEM) {
                            logInfo(
                                LOG_CATEGORY,
                                serverType,
                                "Introscope Agent is connected to EM!");
                        } else {
                            logError(
                                LOG_CATEGORY,
                                serverType,
                                "Introscope Agent is not connected to EM though the test type is NOT NO_AGENT!");
                        }
                        break;
                    }
                    default: {
                        String msg =
                            logError(LOG_CATEGORY, serverType, "Unknown test type: {0}", testType);
                        throw new PowerPackDelegateException(msg,
                            PowerPackDelegateException.ERR_UNKNOWN_TEST_TYPE);
                    }
                }
            }
        } catch (Exception e) {
            String msg =
                "Exception occurred while determining if Introscope Agent is connected to EM!";
            logError(LOG_CATEGORY, serverType, msg, e);
            throw new PowerPackDelegateException(msg, e,
                PowerPackDelegateException.ERR_AGENT_EM_CONNECTION_CHECK_FAILED);
        }
    }

    @Override
    protected Logger getLogger() {
        return LOGGER;
    }

    private static String cutOffDomain(String host) {
        if (!StringUtils.hasText(host)) {
            return host;
        }
        int i = host.indexOf('.');
        return i > 0 ? host.substring(0, i) : host;
    }

}
