package com.ca.apm.systemtest.fld.plugin.powerpack.delegates;

import java.io.IOException;

import com.ca.apm.systemtest.fld.common.logmonitor.FldLogger;
import org.activiti.engine.delegate.DelegateExecution;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.NotImplementedException;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ca.apm.systemtest.fld.plugin.AbstractJavaDelegate;
import com.ca.apm.systemtest.fld.plugin.NodeManager;
import com.ca.apm.systemtest.fld.proxy.AgentProxyFactory;

/**
 * @author shadm01
 */
public class DbCleanupDelegate extends AbstractJavaDelegate {
    private final Logger LOGGER = LoggerFactory.getLogger(DbCleanupDelegate.class);

    public DbCleanupDelegate(NodeManager nodeManager, AgentProxyFactory agentProxyFactory, FldLogger fldLogger) {
        super(nodeManager, agentProxyFactory, fldLogger);
    }

    protected void stopServerBeforeCleanup(DelegateExecution execution) {
        throw new NotImplementedException("Not implemented!");
    }

    @Override
    protected void handleExecution(DelegateExecution execution) throws Throwable {
        String cleanupApp = getEnumExecutionVariable(execution, "applicationType");

        String dbCleanupHost = getStringExecutionVariable(execution, "dbCleanupHost");
        Integer dbCleanupPort = getIntegerExecutionVariable(execution, "dbCleanupPort");

        switch (cleanupApp) {
            case "trade6": {
                logInfo("Trade6 DB selected");
                stopServerBeforeCleanup(execution);
                cleanTrade6Database(dbCleanupHost, dbCleanupPort);
                recreateTrade6Database(dbCleanupHost, dbCleanupPort);
                break;
            }
            default: {
                logInfo("Database is not trade6, skip cleaning phase");
                break;
            }
        }
    }

    @Override
    protected Logger getLogger() {
        return LOGGER;
    }

    private boolean cleanTrade6Database(String dbCleanupHost, Integer dbCleanupPort)
        throws IOException {
        logInfo("Cleaning Database Trade6 Started");

        CloseableHttpClient httpclient = HttpClients.createDefault();
        String url = String
            .format("http://%s:%s/trade/config?action=resetTrade", dbCleanupHost,
                dbCleanupPort);
        HttpGet httpGet = new HttpGet(url);
        CloseableHttpResponse response1 = httpclient.execute(httpGet);

        HttpEntity entity1 = response1.getEntity();
        String responseBody = IOUtils.toString(entity1.getContent());

        EntityUtils.consume(entity1);
        response1.close();

        boolean resultOk = responseBody.contains("Trade Reset completed successfully");

        if (!resultOk) {
            logError("Database cleanup was not sucesfull!");
            logInfo(responseBody);
        }

        logInfo("Cleaning Trade6 Database Completed");
        return resultOk;

    }

    private boolean recreateTrade6Database(String dbCleanupHost, Integer dbCleanupPort) throws
        IOException {
        logInfo("Repopulating Trade6 database Started");

        CloseableHttpClient httpclient = HttpClients.createDefault();
        String url = String
            .format("http://%s:%s/trade/config?action=buildDB", dbCleanupHost,
                dbCleanupPort);
        HttpGet httpGet = new HttpGet(url);
        CloseableHttpResponse response1 = httpclient.execute(httpGet);

        logInfo(response1.toString());

        HttpEntity entity1 = response1.getEntity();
        String responseBody = IOUtils.toString(entity1.getContent());

        EntityUtils.consume(entity1);
        response1.close();


        boolean resultOk = responseBody.contains("Trade Database Built - 500users created");
        if (!resultOk) {
            logError("Database re-creation was not sucesfull!");
            logInfo(responseBody);
        }
        logInfo("Repopulating Trade6 database Completed");
        return resultOk;
    }
    
}
