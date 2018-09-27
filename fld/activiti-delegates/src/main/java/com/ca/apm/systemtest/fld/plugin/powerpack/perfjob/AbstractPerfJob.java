package com.ca.apm.systemtest.fld.plugin.powerpack.perfjob;

import java.util.Collection;
import java.util.Map;

import com.ca.apm.systemtest.fld.plugin.NodeManager;
import com.ca.apm.systemtest.fld.proxy.AgentProxyFactory;

import org.activiti.engine.delegate.VariableScope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author keyja01
 *
 */

@Component
public abstract class AbstractPerfJob implements PerfJob {
    private static final Logger log = LoggerFactory.getLogger(AbstractPerfJob.class);

    @Autowired
    protected NodeManager nodeManager;
    @Autowired
    protected AgentProxyFactory agentProxyFactory;

    /**
     * This functions dumps variables into log using DEBUG priority.
     * @param vs variable scope to dump
     */
    public static void dumpVariables(VariableScope vs) {
        if (! log.isDebugEnabled()) {
            return;
        }

        Collection<String> localVariablesNames = vs.getVariableNamesLocal();
        Map<String, Object> localVariables = vs.getVariablesLocal(localVariablesNames, true);
        log.debug("Local variables:\n{}", localVariables);

        Collection<String> variablesNames = vs.getVariableNames();
        Map<String, Object> variables = vs.getVariables(variablesNames, true);
        log.debug("Variables:\n{}", variables);
    }
}
