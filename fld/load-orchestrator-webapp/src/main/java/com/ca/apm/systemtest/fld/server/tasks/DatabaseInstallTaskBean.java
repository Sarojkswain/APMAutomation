/**
 *
 */
package com.ca.apm.systemtest.fld.server.tasks;

import org.activiti.engine.delegate.BpmnError;
import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.JavaDelegate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.ca.apm.systemtest.fld.plugin.AbstractJavaDelegate;
import com.ca.apm.systemtest.fld.plugin.NodeManager;
import com.ca.apm.systemtest.fld.plugin.em.DatabasePlugin;
import com.ca.apm.systemtest.fld.plugin.em.EmPlugin;
import com.ca.apm.systemtest.fld.plugin.em.EmPlugin.InstallStatus;
import com.ca.apm.systemtest.fld.plugin.em.EmPlugin.InstallationParameters;
import com.ca.apm.systemtest.fld.plugin.em.InstallerProperties;
import com.ca.apm.systemtest.fld.proxy.AgentProxy;
import com.ca.apm.systemtest.fld.proxy.AgentProxyFactory;

/**
 * @author KEYJA01
 */
@Component("databaseInstallTaskBean")
public class DatabaseInstallTaskBean implements InitializingBean {
    Logger log = LoggerFactory.getLogger(DatabaseInstallTaskBean.class);

    @Autowired
    private NodeManager nodeManager;
    @Autowired
    private AgentProxyFactory fact;
    private CheckEmDatabaseInstallDelegate checkEmDatabaseInstallDelegate;
    private StartEmDatabaseInstallDelegate startEmDatabaseInstallDelegate;
    private ImportDomainXmlDelegate importDomainXmlDelegate;

    private class CheckEmDatabaseInstallDelegate implements JavaDelegate {

        @Override
        public void execute(DelegateExecution execution) throws Exception {
            try {
                String nodeName = execution.getVariable("nodeName").toString();
                boolean available = nodeManager.checkNodeAvailable(nodeName);
                log.info("{} is available: {}", nodeName, available);

                if (!available) {
                    throw new BpmnError("NODENOTAVAILABLE",
                        "Target node not available for EM database install");
                }

                AgentProxy proxy = fact.createProxy(nodeName);
                EmPlugin dbPlugin = (EmPlugin) proxy.getPlugins().get("dbPlugin");

                InstallStatus status = dbPlugin
                    .checkInstallStatus((String) execution.getVariable("installId"));
                execution.setVariable("installStatus", status);
                log.debug("Install status is {}", status);
            } catch (BpmnError be) {
                throw be;
            } catch (Exception e) {
                BpmnError error = new BpmnError("SOMETHINGBAD",
                    "Something bad has happened, check the logs");
                error.initCause(e);
                log.error("Exception", e);
                throw error;
            }
        }

    }

    private class StartEmDatabaseInstallDelegate implements JavaDelegate {

        public static final long DATABASE_PORT = 5432L;

        @Override
        public void execute(DelegateExecution execution) throws Exception {
            try {
                String nodeName = (String) execution.getVariable("nodeName").toString();
                boolean available = nodeManager.checkNodeAvailable(nodeName);
                log.info("{} is available: {}", nodeName, available);

                if (!available) {
                    throw new BpmnError("NODENOTAVAILABLE",
                        "Target node not available for EM database install");
                }

                InstallationParameters cfg = new InstallationParameters();
                cfg.installerType = InstallerProperties.InstallerType.DATABASE;
                cfg.installDir = (String) execution.getVariable("emInstallDir");
                cfg.trussServer = (String) execution.getVariable("trussServer");
                cfg.noInstallerSpecification = (String) execution.getVariable("emNoInstallerSpecification");
                
                cfg.logs = (String) execution.getVariable("logs");

                cfg.db = com.ca.apm.systemtest.fld.plugin.em.EmPlugin.Database.postgre;
                cfg.dbHost = nodeName;
                Long port = (Long) execution.getVariable("dbPort");
                if (port == null) {
                    port = DATABASE_PORT;
                }
                cfg.dbPort = port.intValue();
                cfg.dbSid = (String) execution.getVariable("Sid");
                cfg.dbUserName = (String) execution.getVariable("dbUserName");
                cfg.dbUserPass = (String) execution.getVariable("dbUserPass");
                cfg.dbAdminName = (String) execution.getVariable("dbAdminName");
                cfg.dbAdminPass = (String) execution.getVariable("dbAdminPass");

                AgentProxy proxy = fact.createProxy(nodeName);
                EmPlugin dbPlugin = (EmPlugin) proxy.getPlugins().get("dbPlugin");
                String installationId = dbPlugin.install(cfg);
                execution.setVariable("installId", installationId);

            } catch (BpmnError be) {
                throw be;
            } catch (Exception e) {
                BpmnError error = new BpmnError("SOMETHINGBAD",
                    "Something bad has happened, check the logs");
                error.initCause(e);
                log.error("Exception", e);
                throw error;
            }
        }
    }
    
    
    /**
     * Handles importing a domain.xml export into a fresh postgres installation 
     * @author keyja01
     *
     */
    private class ImportDomainXmlDelegate extends AbstractJavaDelegate {

        public ImportDomainXmlDelegate(NodeManager nodeManager, AgentProxyFactory agentProxyFactory) {
            super(nodeManager, agentProxyFactory);
        }

        @Override
        protected void handleExecution(DelegateExecution execution) throws Throwable {
            String nodeName = getExecutionVariable(execution, NODE_NAME);
            boolean available = nodeManager.checkNodeAvailable(nodeName);
            if (!available) {
                throw new BpmnError("NODENOTAVAILABLE",
                    "Target node not available for EM database install");
            }
            
            AgentProxy proxy = fact.createProxy(nodeName);
            DatabasePlugin db = proxy.getPlugin("dbPlugin", DatabasePlugin.class);
            
//            InstallationParameters cfg = new EmPlugin.InstallationParameters();
//            populateBeanFromExecution(execution, cfg);
//            cfg.db = Database.postgre;
            String cemDbExportFile = getExecutionVariable(execution, "cemDbExportFile");
            if (cemDbExportFile == null) {
                cemDbExportFile = "/automation/domainconfig/current";
            }
            db.importDomainConfig(cemDbExportFile, getExecutionVariable(execution, "dbConfigImportTargetRelease"));
            
            /*
            def cfg = new com.ca.apm.systemtest.fld.plugin.em.EmPlugin.InstallationParameters();

            cfg.installDir = emInstallDir;

            cfg.trussServer = trussServer;
            cfg.codeName = codeName;
            cfg.buildNumber = buildNumber;
            cfg.buildId = buildId;
            //cfg.osgiBuildId = osgiBuildId;
            cfg.logs = logs;

            cfg.db = com.ca.apm.systemtest.fld.plugin.em.EmPlugin.Database.postgre;
            cfg.dbHost = dbHost;
            cfg.dbPort = dbPort;
            cfg.dbSid = dbSid;
            cfg.dbUserName = dbUserName;
            cfg.dbUserPass = dbUserPass;
            cfg.dbAdminName = dbAdminName;
            cfg.dbAdminPass = dbAdminPass;
            cfg.dbConfigImportTargetRelease = dbConfigImportTargetRelease;

            def  proxy = agentProxyFactory.createProxy(nodeName);
            def em = proxy.plugins.dbPlugin;
            em.importDomainConfig(cfg, "/automation/domainconfig/current");
            */
        }
        
    }
    

    @Override
    public void afterPropertiesSet() throws Exception {
        checkEmDatabaseInstallDelegate = new CheckEmDatabaseInstallDelegate();
        startEmDatabaseInstallDelegate = new StartEmDatabaseInstallDelegate();
        importDomainXmlDelegate = new ImportDomainXmlDelegate(nodeManager, fact);
    }

    public CheckEmDatabaseInstallDelegate getCheckEmDatabaseInstallDelegate() {
        return checkEmDatabaseInstallDelegate;
    }

    public StartEmDatabaseInstallDelegate getStartEmDatabaseInstallDelegate() {
        return startEmDatabaseInstallDelegate;
    }

    public ImportDomainXmlDelegate getImportDomainXmlDelegate() {
        return importDomainXmlDelegate;
    }
}
