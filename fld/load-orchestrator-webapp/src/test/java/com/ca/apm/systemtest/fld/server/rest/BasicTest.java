package com.ca.apm.systemtest.fld.server.rest;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.activiti.engine.ProcessEngine;
import org.activiti.engine.ProcessEngineConfiguration;
import org.activiti.engine.impl.cfg.StandaloneInMemProcessEngineConfiguration;
import org.activiti.engine.parse.BpmnParseHandler;
import org.dozer.DozerBeanMapper;
import org.hibernate.SessionFactory;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.Configuration;
import org.springframework.orm.hibernate4.HibernateTransactionManager;

import com.ca.apm.systemtest.fld.server.dao.DashboardDao;
import com.ca.apm.systemtest.fld.server.dao.DashboardHibernate4Dao;
import com.ca.apm.systemtest.fld.server.manager.DashboardManager;
import com.ca.apm.systemtest.fld.server.manager.DashboardManagerImpl;
import com.ca.apm.systemtest.fld.server.model.ConfigItem;
import com.ca.apm.systemtest.fld.server.model.Dashboard;
import com.ca.apm.systemtest.fld.server.model.DashboardConfig;
import com.ca.apm.systemtest.fld.server.model.LoggerMonitorValue;
import com.ca.apm.systemtest.fld.server.model.MonitoredValue;
import com.ca.apm.systemtest.fld.server.model.WorkflowProcessInstance;
import com.ca.apm.systemtest.fld.server.util.activiti.PreBpmnParseHandler;

/**
 * Common class to provide in-memory environment configuration (including Hibernate, Activiti, Spring) 
 * for Unit tests. 
 * 
 * @author SINAL04
 *
 */
public abstract class BasicTest {

    protected ProcessEngine processEngine;
    protected DashboardRestController dashboardRestController;
    protected DashboardManager dashboardManager;
    protected HibernateTransactionManager txManager;
    protected DozerBeanMapper mapper;
    
    protected void init() throws Exception {
        ProcessEngineConfiguration config = createActivitiProcessEngineConfiguration();
        processEngine = config.buildProcessEngine();
        
        mapper = new DozerBeanMapper(Collections.singletonList("dozer-config.xml"));
        
        Configuration configuration = getHibernateConfiguration();
        
        StandardServiceRegistryBuilder builder = new StandardServiceRegistryBuilder()
                .applySettings(configuration.getProperties());
        SessionFactory sessionFactory = configuration.buildSessionFactory(builder.build());
        
        txManager = new HibernateTransactionManager(sessionFactory);
        txManager.getSessionFactory().openSession();
        
        DashboardDao dashboardDao = new DashboardHibernate4Dao();
        dashboardDao.setSessionFactory(sessionFactory);
        DashboardManagerImpl dashboardManagerImpl = new DashboardManagerImpl();
        
        dashboardManagerImpl.setDashboardDao(dashboardDao);
        dashboardManagerImpl.setFormService(processEngine.getFormService());
        dashboardManagerImpl.setRepositoryService(processEngine.getRepositoryService());
        dashboardManagerImpl.setRuntimeService(processEngine.getRuntimeService());
        dashboardManagerImpl.setMapper(mapper);
        
        dashboardManager = dashboardManagerImpl;
        dashboardRestController = new DashboardRestController();
        dashboardRestController.setProcessEngine(processEngine);
        dashboardRestController.setRuntimeService(processEngine.getRuntimeService());
        dashboardRestController.setFormService(processEngine.getFormService());
        dashboardRestController.setRepositoryService(processEngine.getRepositoryService());
        dashboardRestController.setTaskService(processEngine.getTaskService());
        dashboardRestController.setDashboardManager(dashboardManagerImpl);
        dashboardRestController.setMapper(mapper);
        
        childInit();
    }

    protected ProcessEngineConfiguration createActivitiProcessEngineConfiguration() {
        StandaloneInMemProcessEngineConfiguration config = (StandaloneInMemProcessEngineConfiguration) ProcessEngineConfiguration.createStandaloneInMemProcessEngineConfiguration();
        config.setDatabaseSchemaUpdate(ProcessEngineConfiguration.DB_SCHEMA_UPDATE_CREATE_DROP)
                .setJobExecutorActivate(true);
        List<BpmnParseHandler> parseHandlers = config.getPreBpmnParseHandlers();
        if (parseHandlers == null) {
            parseHandlers = new ArrayList<>();
            config.setPreBpmnParseHandlers(parseHandlers);
        }
        parseHandlers.add(new PreBpmnParseHandler());
        return config;
    }
    
    protected abstract void childInit() throws Exception;
    
    protected void finish() throws Exception {
        txManager.getSessionFactory().getCurrentSession().flush();
        txManager.getSessionFactory().getCurrentSession().close();
        processEngine.close();
        
        childFinish();
    }
    
    protected abstract void childFinish() throws Exception;

    protected Configuration getHibernateConfiguration() {
        Configuration configuration = new Configuration();
        
        configuration.setProperty("hibernate.connection.driver_class", "org.h2.Driver");
        configuration.setProperty("hibernate.connection.url", "jdbc:h2:mem:test;DB_CLOSE_ON_EXIT=FALSE");
        configuration.setProperty("hibernate.connection.username", "sa");
        configuration.setProperty("hibernate.connection.password", "");
        configuration.setProperty("hibernate.defaultAutoCommit", "false");
        configuration.setProperty("hibernate.maxActive", "10");
        configuration.setProperty("hibernate.maxIdle", "2");
        configuration.setProperty("hibernate.initialSize", "2");
        
        configuration.setProperty("hibernate.current_session_context_class", "org.hibernate.context.internal.ThreadLocalSessionContext");
        
        configuration.setProperty("hibernate.dialect", "org.hibernate.dialect.H2Dialect");
        configuration.setProperty("hibernate.show_sql", "false");
        configuration.setProperty("hibernate.format_sql", "false");
        configuration.setProperty("hibernate.hbm2ddl.auto", "update");
        
        //required settings for HibernateTransactionManager
        configuration.setProperty("hibernate.connection.provider_class", "org.hibernate.service.jdbc.connections.internal.C3P0ConnectionProvider");
        configuration.setProperty("hibernate.c3p0.min_size", "5");
        configuration.setProperty("hibernate.c3p0.max_size", "20");
        configuration.setProperty("hibernate.c3p0.timeout", "300");
        configuration.setProperty("hibernate.c3p0.max_statements", "50");
        configuration.setProperty("hibernate.c3p0.idle_test_period", "3000");
        
        configuration.addPackage("com.ca.apm.systemtest.fld.server.model")
                     .addAnnotatedClass(Dashboard.class)
                     .addAnnotatedClass(DashboardConfig.class)
                     .addAnnotatedClass(MonitoredValue.class)
                     .addAnnotatedClass(ConfigItem.class)
                     .addAnnotatedClass(WorkflowProcessInstance.class)
                     .addAnnotatedClass(LoggerMonitorValue.class);
        
        return configuration;
    }

}
