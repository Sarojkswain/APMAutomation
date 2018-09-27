package com.ca.apm.systemtest.fld.server.util;

import org.activiti.engine.delegate.VariableScope;
import org.activiti.engine.impl.scripting.Resolver;
import org.activiti.engine.impl.scripting.ResolverFactory;
import org.activiti.engine.impl.scripting.ScriptBindingsFactory;
import org.activiti.spring.SpringProcessEngineConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.ca.apm.systemtest.fld.proxy.AgentProxy;
import com.ca.apm.systemtest.fld.proxy.AgentProxyFactory;
import com.ca.apm.systemtest.fld.server.dao.NodeDao;
import com.ca.apm.systemtest.fld.server.model.Node;

public class CustomSpringProcessEngineConfiguration extends SpringProcessEngineConfiguration {
    private Logger log = LoggerFactory.getLogger(CustomSpringProcessEngineConfiguration.class);

    @Autowired
    private NodeDao nodeDao;

    @Autowired
    private AgentProxyFactory agentProxyFactory;
    
    @Override
    protected void initScriptingEngines() {
        super.initScriptingEngines();
        if (resolverFactories != null) {
            resolverFactories.add(new NodeResolverFactory(nodeDao, agentProxyFactory));
            scriptingEngines = new FldScriptingEngines(new ScriptBindingsFactory(resolverFactories));
            // Never set this to false otherwise our override of Groovy scripting engine will stop
            // working.
            //scriptingEngines.setCacheScriptingEngines(true);
            scriptingEngines.addScriptEngineFactory(new FldGroovyScriptEngineFactory());
            log.info("Added {} script engine.", FldGroovyScriptEngineFactory.class.getName());
        }
    }

    public static class NodeResolverFactory implements ResolverFactory, Resolver {
        private NodeDao nodeDao;
        private AgentProxyFactory agentProxyFactory;

        public NodeResolverFactory(NodeDao dao, AgentProxyFactory factory) {
            nodeDao = dao;
            agentProxyFactory = factory;
        }

        @Override
        public boolean containsKey(Object key) {
            if (key instanceof String) {
                Node node = nodeDao.findByNodeName((String) key);
                return (node != null);
            }
            return (false);
        }

        @Override
        public Object get(Object key) {
            AgentProxy proxy = null;
            if (key instanceof String) {
                proxy = agentProxyFactory.createProxy((String) key);
            }
            return (proxy);
        }

        @Override
        public Resolver createResolver(VariableScope variableScope) {
            return (this);
        }
    }

}
