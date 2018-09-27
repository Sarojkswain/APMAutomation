/**
 * 
 */
package com.ca.apm.systemtest.fld.plugin.em.job;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.JavaDelegate;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.FileCopyUtils;

import com.ca.apm.systemtest.fld.plugin.AbstractJavaDelegate;
import com.ca.apm.systemtest.fld.plugin.NodeManager;
import com.ca.apm.systemtest.fld.plugin.em.EmPlugin;
import com.ca.apm.systemtest.fld.plugin.file.transformation.FileTransformationPlugin;
import com.ca.apm.systemtest.fld.plugin.file.transformation.FileTransformationPlugin.ConfigurationFormat;
import com.ca.apm.systemtest.fld.proxy.AgentProxy;
import com.ca.apm.systemtest.fld.proxy.AgentProxyFactory;

/**
 * @author keyja01
 *
 */
@Component("configureJobsBean")
public class ConfigureJobsBean implements InitializingBean {
    
    @Autowired
    protected NodeManager nodeManager;
    
    @Autowired
    protected AgentProxyFactory agentProxyFactory;
    
    private InstallManagementModulesDelegate installManagementModules;
    private ConfigureCollector2Delegate configureCollector2;
    private ConfigureMomAnalyticsDelegate configureMomAnalytics;
    private ConfigureClamps configureClamps;
    private ConfigureMomJvmArgs configureMomJvmArgs;
    private ConfigureCollector1JvmArgs configureCollector1JvmArgs;
    private ConfigureCollectorXJvmArgs configureCollectorXJvmArgs;
    private ConfigureBaselines configureBaselines;
    private ConfigureWebViewJvmArgs configureWebViewJvmArgs;
    private ConfigureAgcMasterArgs configureAgcMasterArgs;
    private ConfigureSecondMOMArgs configureSecondMOMArgs;
    private ConfigureVirtCollectorXJvmArgs configureVirtCollectorXJvmArgs;
    
    @Override
    public void afterPropertiesSet() throws Exception {
        installManagementModules = new InstallManagementModulesDelegate(nodeManager, agentProxyFactory);
        configureCollector2 = new ConfigureCollector2Delegate(nodeManager, agentProxyFactory);
        configureMomAnalytics = new ConfigureMomAnalyticsDelegate(nodeManager, agentProxyFactory);
        configureClamps = new ConfigureClamps(nodeManager, agentProxyFactory);
        configureMomJvmArgs = new ConfigureMomJvmArgs(nodeManager, agentProxyFactory);
        configureCollector1JvmArgs = new ConfigureCollector1JvmArgs(nodeManager, agentProxyFactory);
        configureCollectorXJvmArgs = new ConfigureCollectorXJvmArgs(nodeManager, agentProxyFactory);
        configureBaselines = new ConfigureBaselines(nodeManager, agentProxyFactory);
        configureWebViewJvmArgs = new ConfigureWebViewJvmArgs(nodeManager, agentProxyFactory);
        configureAgcMasterArgs = new ConfigureAgcMasterArgs(nodeManager, agentProxyFactory);
        configureSecondMOMArgs = new ConfigureSecondMOMArgs(nodeManager, agentProxyFactory);
        configureVirtCollectorXJvmArgs = new ConfigureVirtCollectorXJvmArgs(nodeManager, agentProxyFactory);
    }
    
    
    
    public InstallManagementModulesDelegate getInstallManagementModules() {
        return installManagementModules;
    }
    
    public ConfigureCollector2Delegate getConfigureCollector2() {
        return configureCollector2;
    }

    public ConfigureMomAnalyticsDelegate getConfigureMomAnalytics() {
        return configureMomAnalytics;
    }
    
    public ConfigureClamps getConfigureClamps() {
        return configureClamps;
    }
    
    public ConfigureMomJvmArgs getConfigureMomJvmArgs() {
        return configureMomJvmArgs;
    }

    public ConfigureCollector1JvmArgs getConfigureCollector1JvmArgs() {
        return configureCollector1JvmArgs;
    }
    
    public ConfigureCollectorXJvmArgs getConfigureCollectorXJvmArgs() {
        return configureCollectorXJvmArgs;
    }
    
    public ConfigureBaselines getConfigureBaselines() {
        return configureBaselines;
    }
    
    public ConfigureWebViewJvmArgs getConfigureWebViewJvmArgs() {
        return configureWebViewJvmArgs;
    }
    
    public ConfigureAgcMasterArgs getConfigureAgcMasterArgs() {
        return configureAgcMasterArgs;
    }
    
    public ConfigureSecondMOMArgs getConfigureSecondMOMArgs() {
        return configureSecondMOMArgs;
    }
    
    public ConfigureVirtCollectorXJvmArgs getConfigureVirtCollectorXJvmArgs() {
        return configureVirtCollectorXJvmArgs;
    }
    
    public class InstallManagementModulesDelegate extends AbstractJavaDelegate implements JavaDelegate {
        public InstallManagementModulesDelegate(NodeManager nodeManager, AgentProxyFactory agentProxyFactory) {
            super(nodeManager, agentProxyFactory);
        }

        @Override
        protected void handleExecution(DelegateExecution execution) throws Throwable {
            String momNode = getExecutionVariable(execution, "fld.mom.node");
            if (!nodeManager.checkNodeAvailable(momNode)) {
                return;
            }
            
            AgentProxy proxy = agentProxyFactory.createProxy(momNode);
            EmPlugin mom = proxy.getPlugin(EmPlugin.PLUGIN, EmPlugin.class);
            
            // TODO get artifact spec from workflow instead of URL
            mom.installManagementModules(getExecutionVariable(execution, "fld.mm.url"));
        }
    }
    
    
    public class ConfigureCollector2Delegate extends AbstractJavaDelegate implements JavaDelegate {
        public ConfigureCollector2Delegate(NodeManager nodeManager, AgentProxyFactory agentProxyFactory) {
            super(nodeManager, agentProxyFactory);
        }

        @Override
        protected void handleExecution(DelegateExecution execution) throws Throwable {
            FileTransformationPlugin ftx = loadPlugin(execution, "fld.em02.node", "fileTransformation", FileTransformationPlugin.class);
            
            Map<String, Object> vars = new HashMap<>();
            String transformConfig = readResourceAsString("collector02.xml");
            ftx.transform(transformConfig, ConfigurationFormat.XML, vars);
        }
    }
    
    
    public class ConfigureMomAnalyticsDelegate extends AbstractJavaDelegate {

        public ConfigureMomAnalyticsDelegate(NodeManager nodeManager, AgentProxyFactory agentProxyFactory) {
            super(nodeManager, agentProxyFactory);
        }

        @Override
        protected void handleExecution(DelegateExecution execution) throws Throwable {
            String installPrelert = getExecutionVariable(execution, "fld.install.prelert.server");
            if (installPrelert != null && installPrelert.equals("true")) {
                FileTransformationPlugin ftx = loadPlugin(execution, "fld.mom.node", "fileTransformation", FileTransformationPlugin.class);
                
                Map<String, Object> vars = new HashMap<>();
                vars.put("prelertHost", "fldprelert01c");
                vars.put("prelertPort", "8080");
                String transformConfig = readResourceAsString("mom-analytics.xml");
                ftx.transform(transformConfig, ConfigurationFormat.XML, vars);
            }
        }
        
    }
    
    
    /**
     * Configures the clamp settings (from clamps.xml) on all of the EMs and MOM
     * @author keyja01
     *
     */
    public class ConfigureClamps extends AbstractJavaDelegate {

        public ConfigureClamps(NodeManager nodeManager, AgentProxyFactory agentProxyFactory) {
            super(nodeManager, agentProxyFactory);
        }

        @Override
        protected void handleExecution(DelegateExecution execution) throws Throwable {
            String[] nodes = new String[] { "fld.mom.node", "fld.em01.node", "fld.em02.node", 
                                            "fld.em03.node", "fld.em04.node", "fld.em05.node", 
                                            "fld.em06.node", "fld.em07.node", "fld.em08.node", 
                                            "fld.em09.node", "fld.em10.node" };
            
            String transformConfig = readResourceAsString("clamps.xml");
            
            for (String nodeName: nodes) {
                FileTransformationPlugin ftx = loadPlugin(execution, nodeName, "fileTransformation", FileTransformationPlugin.class);
                if (ftx == null) {
                    continue;
                }
                
                Map<String, Object> vars = new HashMap<>();
                ftx.transform(transformConfig, ConfigurationFormat.XML, vars);
            }
        }
    }
    
    
    /**
     * Configures the MOMs JVM args
     * @author keyja01
     *
     */
    public class ConfigureMomJvmArgs extends AbstractJavaDelegate {

        public ConfigureMomJvmArgs(NodeManager nodeManager, AgentProxyFactory agentProxyFactory) {
            super(nodeManager, agentProxyFactory);
        }

        @Override
        protected void handleExecution(DelegateExecution execution) throws Throwable {
            String transformConfig = readResourceAsString("mom-jvmargs.xml");
            FileTransformationPlugin ftx = loadPlugin(execution, "fld.mom.node", "fileTransformation", FileTransformationPlugin.class);
            Map<String, Object> vars = new HashMap<>();
            ftx.transform(transformConfig, ConfigurationFormat.XML, vars);
        }
        
    }
    
    
    /**
     * Configures the MOMs JVM args
     * @author keyja01
     *
     */
    public class ConfigureCollector1JvmArgs extends AbstractJavaDelegate {

        public ConfigureCollector1JvmArgs(NodeManager nodeManager, AgentProxyFactory agentProxyFactory) {
            super(nodeManager, agentProxyFactory);
        }

        @Override
        protected void handleExecution(DelegateExecution execution) throws Throwable {
            String transformConfig = readResourceAsString("collector01-jvmargs.xml");
            FileTransformationPlugin ftx = loadPlugin(execution, "fld.em01.node", "fileTransformation", FileTransformationPlugin.class);
            Map<String, Object> vars = new HashMap<>();
            ftx.transform(transformConfig, ConfigurationFormat.XML, vars);
        }
        
    }
    
    
    /**
     * Configures the MOMs JVM args
     * @author keyja01
     *
     */
    public class ConfigureWebViewJvmArgs extends AbstractJavaDelegate {

        public ConfigureWebViewJvmArgs(NodeManager nodeManager, AgentProxyFactory agentProxyFactory) {
            super(nodeManager, agentProxyFactory);
        }

        @Override
        protected void handleExecution(DelegateExecution execution) throws Throwable {
            String transformConfig = readResourceAsString("webview-jvmargs.xml");
            FileTransformationPlugin ftx = loadPlugin(execution, "fld.webview.node", "fileTransformation", FileTransformationPlugin.class);
            if (ftx == null) {
                // log me and don't fail
                return;
            }
            String momNode = getExecutionVariable(execution, "fld.mom.node");
            Map<String, Object> vars = new HashMap<>();
            vars.put("momNode", momNode);
            ftx.transform(transformConfig, ConfigurationFormat.XML, vars);
        }
        
    }
    
    
    /**
     * Configures the AGC MASTER
     * @author filja01
     *
     */
    public class ConfigureAgcMasterArgs extends AbstractJavaDelegate {

        public ConfigureAgcMasterArgs(NodeManager nodeManager, AgentProxyFactory agentProxyFactory) {
            super(nodeManager, agentProxyFactory);
        }

        @Override
        protected void handleExecution(DelegateExecution execution) throws Throwable {
            String transformConfig = readResourceAsString("agc-master.xml");
            FileTransformationPlugin ftx = loadPlugin(execution, "fld.agc.node", "fileTransformation", FileTransformationPlugin.class);
            if (ftx == null) {
                // log me and don't fail
                return;
            }
            Map<String, Object> vars = new HashMap<>();
            ftx.transform(transformConfig, ConfigurationFormat.XML, vars);
        }
        
    }
    
    
    /**
     * Configures the second MOM
     * @author filja01
     *
     */
    public class ConfigureSecondMOMArgs extends AbstractJavaDelegate {

        public ConfigureSecondMOMArgs(NodeManager nodeManager, AgentProxyFactory agentProxyFactory) {
            super(nodeManager, agentProxyFactory);
        }

        @Override
        protected void handleExecution(DelegateExecution execution) throws Throwable {
            String transformConfig = readResourceAsString("mom02-conf.xml");
            FileTransformationPlugin ftx = loadPlugin(execution, "fld.mom02.node", "fileTransformation", FileTransformationPlugin.class);
            if (ftx == null) {
                // log me and don't fail
                return;
            }
            Map<String, Object> vars = new HashMap<>();
            ftx.transform(transformConfig, ConfigurationFormat.XML, vars);
        }
        
    }
    
    
    public class ConfigureBaselines extends AbstractJavaDelegate {

        public ConfigureBaselines(NodeManager nodeManager, AgentProxyFactory agentProxyFactory) {
            super(nodeManager, agentProxyFactory);
        }

        @Override
        protected void handleExecution(DelegateExecution execution) throws Throwable {
            // TODO implement me!
        }
        
    }
    
    
    
    /**
     * Configures the MOMs JVM args
     * @author keyja01
     *
     */
    public class ConfigureCollectorXJvmArgs extends AbstractJavaDelegate {

        public ConfigureCollectorXJvmArgs(NodeManager nodeManager, AgentProxyFactory agentProxyFactory) {
            super(nodeManager, agentProxyFactory);
        }

        @Override
        protected void handleExecution(DelegateExecution execution) throws Throwable {
            String[] nodes = new String[] { "fld.em02.node", "fld.em03.node", "fld.em04.node", "fld.em05.node",
                                            "fld.em06.node", "fld.em07.node", "fld.em08.node", "fld.em09.node", 
                                            "fld.em10.node"
            };
            
            String transformConfig = readResourceAsString("collectors-jvmargs.xml");
            for (String nodeName: nodes) {
                FileTransformationPlugin ftx = loadPlugin(execution, nodeName, "fileTransformation", FileTransformationPlugin.class);
                if (ftx == null) {
                    continue;
                }
                Map<String, Object> vars = new HashMap<>();
                ftx.transform(transformConfig, ConfigurationFormat.XML, vars);
            }
        }
        
    }
    
    
    /**
     * Configures the Virtual Collectors JVM args
     * @author filja01
     *
     */
    public class ConfigureVirtCollectorXJvmArgs extends AbstractJavaDelegate {

        public ConfigureVirtCollectorXJvmArgs(NodeManager nodeManager, AgentProxyFactory agentProxyFactory) {
            super(nodeManager, agentProxyFactory);
        }

        @Override
        protected void handleExecution(DelegateExecution execution) throws Throwable {
            String[] nodes = new String[] { "fld.mom02.em01.node", "fld.mom02.em02.node", "fld.agc.em.node"
            };
            
            String transformConfig = readResourceAsString("virt-collectors-jvmargs.xml");
            for (String nodeName: nodes) {
                FileTransformationPlugin ftx = loadPlugin(execution, nodeName, "fileTransformation", FileTransformationPlugin.class);
                if (ftx == null) {
                    continue;
                }
                Map<String, Object> vars = new HashMap<>();
                ftx.transform(transformConfig, ConfigurationFormat.XML, vars);
            }
        }   
    }
    

    protected String readResourceAsString(String path) throws Exception {
        InputStream in = getClass().getResourceAsStream(path);
        String configuration = FileCopyUtils.copyToString(new InputStreamReader(in, "UTF-8"));
        
        return configuration;
    }
    
}
