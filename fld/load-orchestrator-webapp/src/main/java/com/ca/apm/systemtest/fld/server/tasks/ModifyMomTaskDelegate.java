/**
 * 
 */
package com.ca.apm.systemtest.fld.server.tasks;

import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import org.activiti.engine.delegate.BpmnError;
import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.JavaDelegate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.ca.apm.systemtest.fld.common.ErrorUtils;
import com.ca.apm.systemtest.fld.plugin.NodeManager;
import com.ca.apm.systemtest.fld.plugin.file.transformation.FileTransformationPlugin;
import com.ca.apm.systemtest.fld.proxy.AgentProxy;
import com.ca.apm.systemtest.fld.proxy.AgentProxyFactory;

/**
 * @author JIRJI01
 *
 */
@Component("modifyMomDelegate")
public class ModifyMomTaskDelegate implements JavaDelegate {
    private static Logger log = LoggerFactory.getLogger(ModifyMomTaskDelegate.class);

    @Autowired
    private NodeManager nodeManager;
    @Autowired
    private AgentProxyFactory fact;

    @Override
    public void execute(DelegateExecution execution) throws Exception {
        try {
            String nodeName = execution.getVariable("nodeName").toString();
            System.out.println(nodeName);
            boolean available = nodeManager.checkNodeAvailable(nodeName);
            System.out.println(nodeName + " is available: " + available);

            if (!available) {
                throw new BpmnError("NODENOTAVAILABLE",
                    "Target node not available for EM database install");
            }
            
            AgentProxy proxy = fact.createProxy(nodeName);
            FileTransformationPlugin ftPlugin = (FileTransformationPlugin) proxy.getPlugins().get("fileTransformation");

            URL url = ModifyMomTaskDelegate.class.getResource("realm_eem_transformation.xml");
            Path resPath = Paths.get(url.toURI());
            String configuration = new String(Files.readAllBytes(resPath), "UTF-8");
            configuration = configuration.replace("__INSTALL_DIR__", "${#emPlugin.pluginConfiguration.currentInstallDir}/config/realms.xml");
            
            Map<String, Object> vars = new HashMap<>();
            String username = (String) execution.getVariable("eemUsername");
            if (username == null || username.isEmpty()) {
                username = "EiamAdmin";
            }
            vars.put("eemUsername", username);
            vars.put("eemHost", execution.getVariable("eemHost"));
            vars.put("eemPassword", execution.getVariable("eemPassword"));
            
            ftPlugin.transform(configuration, FileTransformationPlugin.ConfigurationFormat.XML, vars );

        } catch (BpmnError be) {
            throw be;
        } catch (Exception e) {
            ErrorUtils.logExceptionFmt(log, e, "Exception: {0}");
            BpmnError bpmnError = new BpmnError("SOMETHINGBAD",
                "Something bad has happened, check the logs");
            bpmnError.initCause(e);
            throw bpmnError;
        }
    }
}
