/**
 * 
 */
package com.ca.apm.systemtest.fld.workflow;

import java.io.InputStream;
import java.util.Collections;
import java.util.List;

import org.activiti.engine.ProcessEngine;
import org.activiti.engine.ProcessEngineConfiguration;
import org.activiti.engine.form.AbstractFormType;
import org.activiti.engine.form.FormProperty;
import org.activiti.engine.form.StartFormData;
import org.activiti.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.activiti.engine.impl.cfg.StandaloneInMemProcessEngineConfiguration;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.repository.ProcessDefinitionQuery;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;


/**
 * @author KEYJA01
 *
 */
public class CustomFormTypeTest {
	private ProcessEngine processEngine;
	
	@Before
	public void setup() throws Exception {
		StandaloneInMemProcessEngineConfiguration config = (StandaloneInMemProcessEngineConfiguration) ProcessEngineConfiguration.createStandaloneInMemProcessEngineConfiguration();
        String url = config.getJdbcUrl();
        url = url + this.getClass().getSimpleName().substring(0, 5).toUpperCase();
        config.setJdbcUrl(url);
		
		// register the custom form type
		List<AbstractFormType> customFormTypes = Collections.<AbstractFormType>singletonList(new CustomFormType());
		((ProcessEngineConfigurationImpl) config).setCustomFormTypes(customFormTypes);
		
		config.setDatabaseSchemaUpdate(ProcessEngineConfiguration.DB_SCHEMA_UPDATE_CREATE_DROP)
				.setJobExecutorActivate(true);
		
		processEngine = config.buildProcessEngine();
	}

	@After
	public void tearDown() throws Exception {
        System.out.println("Closing down processEngine");
		processEngine.close();
	}

	@Test
	public void testCustomFormType() {
		InputStream inputStream = CustomFormTypeTest.class.getResourceAsStream("/CustomFormTest.xml");
		Deployment deployment = processEngine.getRepositoryService().createDeployment()
			.addInputStream("CustomFormTest.bpmn20.xml", inputStream)
			.deploy();
		
		Assert.assertNotNull(deployment);
		
//		processEngine.getRepositoryService().activateProcessDefinitionById("customTestFormProcess:1:4");
		ProcessDefinitionQuery query = processEngine.getRepositoryService().createProcessDefinitionQuery();
		List<ProcessDefinition> plist = query.processDefinitionKey("customTestFormProcess").list();
		
		StartFormData formData = processEngine.getFormService().getStartFormData(plist.get(0).getId());
		List<FormProperty> list = formData.getFormProperties();
		for (FormProperty fp: list) {
			System.out.println(fp);
		}
		
	}
}
