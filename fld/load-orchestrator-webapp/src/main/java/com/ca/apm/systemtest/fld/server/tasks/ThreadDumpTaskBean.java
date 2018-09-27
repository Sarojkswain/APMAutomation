package com.ca.apm.systemtest.fld.server.tasks;

import org.activiti.engine.delegate.BpmnError;
import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.JavaDelegate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.ca.apm.systemtest.fld.common.ErrorUtils;
import com.ca.apm.systemtest.fld.plugin.NodeManager;
import com.ca.apm.systemtest.fld.plugin.em.EmPlugin;
import com.ca.apm.systemtest.fld.proxy.AgentProxy;
import com.ca.apm.systemtest.fld.proxy.AgentProxyFactory;
/**
 * 
 * @author rsssa02
 *
 */
@Component("threaddumpTaskBean")
public class ThreadDumpTaskBean implements InitializingBean {
    private static Logger log = LoggerFactory.getLogger(ThreadDumpTaskBean.class);

    @Autowired
    private NodeManager nodeManager;
    @Autowired
    private AgentProxyFactory fact;
    private GetThreadDumpCLWDelegate getThreadDumpCLWDelegate;
    private VerifyThreadDumpDelegate verifyThreadDumpDelegate;
    
    /**
     * GetThreadDumpCLWDelegate class executes EMPlugin executeCLW method to collect the threaddumps from agent (agentname)
     *
     * @author rsssa02
     *
     */
    private class GetThreadDumpCLWDelegate implements JavaDelegate {

        @Override
        public void execute(DelegateExecution execution) throws Exception {
            try {
                String nodeName = (String) execution.getVariable("nodeName");
                System.out.println(nodeName);
                boolean available = nodeManager.checkNodeAvailable(nodeName);
                System.out.println(nodeName + " is available: " + available);
                
                if (!available) {
                    throw new BpmnError("NODENOTAVAILABLE", "Target node not available for threaddump test");
                }
                AgentProxy proxy = fact.createProxy(nodeName);
                EmPlugin emPlugin = (EmPlugin) proxy.getPlugins().get("emPlugin");
                
                emPlugin.executeCLW((String) execution.getVariable("agentName"), (String) execution.getVariable("outFileName"));
                
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
    
    /**
     * VerifyThreadDumpDelegate tries to verify that collect dumps are genuine  
     * since this will be a long run its good to check the collected thread periodically
     * @author rsssa02
     *
     */
    private class VerifyThreadDumpDelegate implements JavaDelegate {

        @Override
        public void execute(DelegateExecution execution) throws Exception {
            try {
                String nodeName = (String) execution.getVariable("nodeName");
                System.out.println(nodeName);
                boolean available = nodeManager.checkNodeAvailable(nodeName);
                System.out.println(nodeName + " is available: " + available);
                
                if (!available) {
                    throw new BpmnError("NODENOTAVAILABLE", "Target node not available threaddump test");
                }
                AgentProxy proxy = fact.createProxy(nodeName);
                EmPlugin emPlugin = (EmPlugin) proxy.getPlugins().get("emPlugin");
                
                emPlugin.verifyTDList((String) execution.getVariable("agentName"), (String) execution.getVariable("outFileName"));
                
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

    @Override
    public void afterPropertiesSet() throws Exception {
        getThreadDumpCLWDelegate = new GetThreadDumpCLWDelegate();
        verifyThreadDumpDelegate = new VerifyThreadDumpDelegate();
    }

    public GetThreadDumpCLWDelegate getGetThreadDumpCLWDelegate() {
        return getThreadDumpCLWDelegate;
    }


    public VerifyThreadDumpDelegate getVerifyThreadDumpDelegate() {
        return verifyThreadDumpDelegate;
    }
  
}