/**
 * 
 */
package com.ca.apm.systemtest.fld.workflow;

import org.activiti.engine.ProcessEngine;
import org.activiti.engine.ProcessEngineConfiguration;
import org.activiti.engine.impl.cfg.StandaloneInMemProcessEngineConfiguration;

/**
 * @author KEYJA01
 *
 */
public class BaseActivitiTest {
	protected ProcessEngine processEngine;

	/**
	 * Creates a basic {@link ProcessEngine} from the configuration
	 * @return
	 */
	protected ProcessEngine createProcessEngine() {
		ProcessEngineConfiguration config = createProcessEngineConfig();
		return config.buildProcessEngine();
	}
	
	/**
	 * Creates a basic configuration for the process engine. Subclasses should override if they need more flexibility
	 * @return
	 */
	protected ProcessEngineConfiguration createProcessEngineConfig() {
		StandaloneInMemProcessEngineConfiguration config = (StandaloneInMemProcessEngineConfiguration) ProcessEngineConfiguration.createStandaloneInMemProcessEngineConfiguration();
		String url = config.getJdbcUrl();
		url = url + this.getClass().getSimpleName().substring(0, 5).toUpperCase();
		config.setJdbcUrl(url);
		
		config.setDatabaseSchemaUpdate(ProcessEngineConfiguration.DB_SCHEMA_UPDATE_CREATE_DROP)
				.setJobExecutorActivate(true);
		
		return config;
	}
}
