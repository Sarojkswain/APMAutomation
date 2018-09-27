package com.ca.apm.systemtest.fld.server.util;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.activiti.engine.delegate.VariableScope;
import org.activiti.engine.form.AbstractFormType;
import org.activiti.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.activiti.engine.impl.javax.el.CompositeELResolver;
import org.activiti.engine.impl.javax.el.ELResolver;
import org.activiti.spring.ProcessEngineFactoryBean;
import org.activiti.spring.SpringExpressionManager;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Autowired;

import com.ca.apm.systemtest.fld.server.dao.NodeDao;

/**
 * Custom ProcessEngineFacotryBean for injecting Activiti ProcessEngine with custom config
 *
 * @author ZUNPA01
 */
public class CustomProcessEngineFactoryBean extends ProcessEngineFactoryBean implements DisposableBean {

    @Autowired
    private NodeDao nodeDao;

    /* (non-Javadoc)
     * @see org.activiti.spring.ProcessEngineFactoryBean#setProcessEngineConfiguration(org.activiti.engine.impl.cfg.ProcessEngineConfigurationImpl)
     */
    @Override
    public void setProcessEngineConfiguration(
        ProcessEngineConfigurationImpl processEngineConfiguration) {
        if (processEngineConfiguration != null) {
            List<AbstractFormType> customFormTypes = Collections
                .<AbstractFormType>singletonList(new NodeFormType(nodeDao));
            processEngineConfiguration.setCustomFormTypes(customFormTypes);
        }
        super.setProcessEngineConfiguration(processEngineConfiguration);
    }
    
    
    /* (non-Javadoc)
     * @see org.activiti.spring.ProcessEngineFactoryBean#configureExpressionManager()
     */
    @Override
    protected void configureExpressionManager() {
        // We need to add our own ELResolver to the stack to be able to automagically 
        // load spring beans from external jars 
        Map<Object, Object> beans = processEngineConfiguration.getBeans();
        SpringExpressionManager em = new SpringExpressionManager(applicationContext, beans) {
            @Override
            protected ELResolver createElResolver(VariableScope variableScope) {
                CompositeELResolver el = (CompositeELResolver) super.createElResolver(variableScope);
                // TODO actually add a resolver that can handle our externally defined beans
                // by creating a new child application context with its own classloader
                // the context and classloader should be disposed when no longer used
                return el;
            }
        };
        processEngineConfiguration.setExpressionManager(em);
    }
    
    
    @Override
    public void destroy() throws Exception {
        super.destroy();
        // TODO release the application contexts created by the engine
    }

}
