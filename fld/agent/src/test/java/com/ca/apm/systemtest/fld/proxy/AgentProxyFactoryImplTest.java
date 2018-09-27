package com.ca.apm.systemtest.fld.proxy;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.util.Collections;
import java.util.Map;

import javax.jms.ConnectionFactory;

import org.junit.Test;

import com.ca.apm.systemtest.fld.plugin.Plugin;
import com.ca.apm.systemtest.fld.proxy.test.*;

/**
 * Created by haiva01 on 1.7.2015.
 */
public class AgentProxyFactoryImplTest {

    @Test
    public void pluginSearchTest() {
        AgentProxyFactoryImpl proxyFactory = new AgentProxyFactoryImpl(Collections.singletonList("com.ca.apm.systemtest.fld.proxy.test"));
        ConnectionFactory connFactory = mock(ConnectionFactory.class);
        proxyFactory.setConnectionFactory(connFactory);
        
        AgentProxy proxy = proxyFactory.createProxy("foo");
        Map<String, Plugin> plugins = proxy.getPlugins();
        
        // test simple case with annotation on the interface
        Plugin p = plugins.get("testPlugin1");
        assertNotNull(p);
        assertTrue(p instanceof TestPlugin1);
        
        // test complex case with multiple implementations of the same plugin interface
        p = plugins.get("testPlugin2");
        assertNotNull(p);
        assertTrue(p instanceof TestPlugin2);
        
        p = plugins.get("testPlugin2.2");
        assertNotNull(p);
        assertTrue(p instanceof TestPlugin2);
    }
}