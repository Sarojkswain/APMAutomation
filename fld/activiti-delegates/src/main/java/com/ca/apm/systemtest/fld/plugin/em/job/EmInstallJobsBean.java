/**
 * 
 */
package com.ca.apm.systemtest.fld.plugin.em.job;

import java.util.ArrayList;

import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.JavaDelegate;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.ca.apm.systemtest.fld.plugin.AbstractJavaDelegate;
import com.ca.apm.systemtest.fld.plugin.NodeManager;
import com.ca.apm.systemtest.fld.plugin.em.EmPlugin;
import com.ca.apm.systemtest.fld.plugin.em.EmPlugin.InstallationParameters;
import com.ca.apm.systemtest.fld.proxy.AgentProxy;
import com.ca.apm.systemtest.fld.proxy.AgentProxyFactory;

/**
 * @author keyja01
 *
 */
@Component("emInstallJobs")
public class EmInstallJobsBean implements InitializingBean {
    public static final String INSTALL_ID = "installId";

    private MomInstallJobDelegate momInstallDelegate;
    private WebViewInstallJobDelegate webviewInstallDelegate;
    private EmCheckNodeJobDelegate emCheckNodeDelegate;
    private AgcInstallJobDelegate agcInstallDelegate;
    
    
    @Autowired
    private AgentProxyFactory agentProxyFactory;
    
    @Autowired
    private NodeManager nodeManager;
    
    @Override
    public void afterPropertiesSet() throws Exception {
        momInstallDelegate = new MomInstallJobDelegate(nodeManager, agentProxyFactory);
        webviewInstallDelegate = new WebViewInstallJobDelegate(nodeManager, agentProxyFactory);
        emCheckNodeDelegate = new EmCheckNodeJobDelegate(nodeManager, agentProxyFactory);
        agcInstallDelegate = new AgcInstallJobDelegate(nodeManager, agentProxyFactory);
    }
    
    public EmInstallJobsBean() {
    }
    
    
    public MomInstallJobDelegate getMomInstallDelegate() {
        return momInstallDelegate;
    }
    
    public WebViewInstallJobDelegate getWebviewInstallDelegate() {
        return webviewInstallDelegate;
    }
    
    public EmCheckNodeJobDelegate getEmCheckNodeDelegate() {
        return emCheckNodeDelegate;
    }
    
    public AgcInstallJobDelegate getAgcInstallDelegate() {
        return agcInstallDelegate;
    }

    /**
     * Handles installation of the MOM using an installer archive
     * @author keyja01
     *
     */
    public class MomInstallJobDelegate extends AbstractJavaDelegate {

        public MomInstallJobDelegate(NodeManager nodeManager, AgentProxyFactory agentProxyFactory) {
            super(nodeManager, agentProxyFactory);
        }

        @Override
        protected void handleExecution(DelegateExecution execution) throws Throwable {
            // create a proxy for the EM plugin
            String nodeName = getExecutionVariable(execution, NODE_NAME);
            AgentProxy proxy = agentProxyFactory.createProxy(nodeName);
            EmPlugin em = proxy.getPlugin("emPlugin", EmPlugin.class);
            
            InstallationParameters cfg = new EmPlugin.InstallationParameters();
            populateBeanFromExecution(execution, cfg);
            cfg.db = EmPlugin.Database.postgre;
            
            ArrayList<String> collectors = new ArrayList<>();
            for (int i = 1; i <= 10; i++) {
                String name = "collector" + i;
                String value = getExecutionVariable(execution, name);
                collectors.add(value);
            }
            cfg.collectors = collectors.toArray(new String[10]);
            
            String installationId = em.install(cfg);
            execution.setVariable(INSTALL_ID, installationId);

            /*
def proxy = agentProxyFactory.createProxy(nodeName);
def em = proxy.plugins.emPlugin;

def cfg = new com.ca.apm.systemtest.fld.plugin.em.EmPlugin.InstallationParameters();


cfg.installDir = emInstallDir;
cfg.trussServer = trussServer;
cfg.codeName = codeName;
cfg.buildNumber = buildNumber;
cfg.buildId = buildId;
cfg.osgiBuildId = osgiBuildId;
cfg.heapSize = heapSize;

cfg.logs = logs;
cfg.jvmExtraArgs = jvmExtraArgs;

cfg.db = com.ca.apm.systemtest.fld.plugin.em.EmPlugin.Database.postgre;
cfg.dbHost = dbHost;
cfg.dbPort = dbPort;
cfg.dbSid = dbSid;
cfg.dbUserName = dbUserName;
cfg.dbUserPass = dbUserPass;
cfg.dbAdminName = dbAdminName;
cfg.dbAdminPass = dbAdminPass;

cfg.collectors = [collector1, collector2, collector3, collector4, collector5, collector6, collector7, collector8, collector9, collector10];

def installationId = em.install(cfg);
execution.setVariable("installId", installationId);
*/
        }
    }
    
    
    public class WebViewInstallJobDelegate extends AbstractJavaDelegate implements JavaDelegate {

        public WebViewInstallJobDelegate(NodeManager nodeManager, AgentProxyFactory agentProxyFactory) {
            super(nodeManager, agentProxyFactory);
        }

        @Override
        protected void handleExecution(DelegateExecution execution) throws Throwable {
            String nodeName = getExecutionVariable(execution, NODE_NAME);
            AgentProxy proxy = agentProxyFactory.createProxy(nodeName);
            EmPlugin wv = proxy.getPlugin("wvPlugin", EmPlugin.class);
            
            InstallationParameters cfg = new EmPlugin.InstallationParameters();
            populateBeanFromExecution(execution, cfg);
            
            String installationId = wv.install(cfg);
            execution.setVariable(INSTALL_ID, installationId);
        }
    }

    
    
    public class EmCheckNodeJobDelegate extends AbstractJavaDelegate {
        public EmCheckNodeJobDelegate(NodeManager nodeManager, AgentProxyFactory agentProxyFactory) {
            super(nodeManager, agentProxyFactory);
        }

        @Override
        protected void handleExecution(DelegateExecution execution) throws Throwable {
            String nodeName = getExecutionVariable(execution, NODE_NAME);
            boolean available = nodeManager.checkNodeAvailable(nodeName);
            execution.setVariable("nodeAvailable", available);
        }
    }
    
    /**
     * Handles installation of the AGC Master using an installer archive
     *
     */
    public class AgcInstallJobDelegate extends AbstractJavaDelegate {

        public AgcInstallJobDelegate(NodeManager nodeManager, AgentProxyFactory agentProxyFactory) {
            super(nodeManager, agentProxyFactory);
        }

        @Override
        protected void handleExecution(DelegateExecution execution) throws Throwable {
            // create a proxy for the EM plugin
            String nodeName = getExecutionVariable(execution, NODE_NAME);
            AgentProxy proxy = agentProxyFactory.createProxy(nodeName);
            EmPlugin agc = proxy.getPlugin("agcPlugin", EmPlugin.class);
            
            InstallationParameters cfg = new EmPlugin.InstallationParameters();
            populateBeanFromExecution(execution, cfg);
            cfg.db = EmPlugin.Database.postgre;
            
            ArrayList<String> collectors = new ArrayList<>();
            for (int i = 1; i <= 10; i++) {
                String name = "collector" + i;
                String value = getExecutionVariable(execution, name);
                collectors.add(value);
            }
            cfg.collectors = collectors.toArray(new String[10]);
            
            String installationId = agc.install(cfg);
            execution.setVariable(INSTALL_ID, installationId);
        }
    }
}
