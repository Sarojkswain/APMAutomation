package com.ca.apm.systemtest.fld.plugin.weblogic.powerPack;

import org.activiti.engine.delegate.DelegateExecution;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ca.apm.systemtest.fld.common.logmonitor.FldLogger;
import com.ca.apm.systemtest.fld.plugin.AbstractJavaDelegate;
import com.ca.apm.systemtest.fld.plugin.NodeManager;
import com.ca.apm.systemtest.fld.plugin.powerpack.common.PowerPackCommonPlugin;
import com.ca.apm.systemtest.fld.plugin.powerpack.common.PowerPackConstants;
import com.ca.apm.systemtest.fld.proxy.AgentProxyFactory;

/**
 * Activiti Java delegate to recreate Trade Database used by the StockTrader sample application (WebLogic Server version).
 * 
 * @author Alexander Sinyushkin (sinal04@ca.com)
 *
 */
public class ReCreateTradeDbDelegate extends AbstractJavaDelegate {
    public static final String LOG_CATEGORY = "PowerPack Re-Create Trade DB Delegate";

    private static final Logger LOGGER = LoggerFactory.getLogger(ReCreateTradeDbDelegate.class);

    public ReCreateTradeDbDelegate(NodeManager nodeManager, AgentProxyFactory agentProxyFactory, FldLogger fldLogger) {
        super(nodeManager, agentProxyFactory, fldLogger);
    }

    @Override
    protected void handleExecution(DelegateExecution execution) throws Throwable {
        final String serverType = getEnumExecutionVariable(execution, PowerPackConstants.SERVER_TYPE_PARAM_NAME);
        final String sqlScriptUrl = getStringExecutionVariable(execution, PowerPackConstants.RECREATE_DB_SCRIPT_ARCHIVE_URL_PARAM_NAME);

        /**
         * Ragu: if you have DB on the same node as your actual server node
         * please delete this execution variable from your WF form properties
         */
        String nodeName = getNodeExecutionVariable(execution, PowerPackConstants.ORACLE_NODE_PARAM_NAME);  
        if (nodeName != null) {
            logInfo(LOG_CATEGORY, serverType, "Looks like the Oracle Node exists, DB creation will proceed on this node ");
        } else {
            nodeName = getNodeExecutionVariable(execution, NODE);
        }

        StringBuffer buf = new StringBuffer("Recreating Trade Database (used by StockTrader)").append('\n')
            .append("Node name: {0}").append('\n')
            .append("SQL Script Archive URL: {1}");
        
        logInfo(LOG_CATEGORY, serverType, buf.toString(), nodeName, sqlScriptUrl);
        
        PowerPackCommonPlugin ppCommonPlugin = getPluginForNode(nodeName, 
            PowerPackCommonPlugin.POWER_PACK_COMMON_PLUGIN_ID, 
            PowerPackCommonPlugin.class);
        
        ppCommonPlugin.reCreateTradeDb(sqlScriptUrl);
        
    }

    @Override
    protected Logger getLogger() {
        return LOGGER;
    }

}
