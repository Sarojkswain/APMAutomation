/*
 * Copyright (c) 2016 CA. All rights reserved.
 * 
 * This software and all information contained therein is confidential and
 * proprietary and shall not be duplicated, used, disclosed or disseminated in
 * any way except as authorized by the applicable license agreement, without
 * the express written permission of CA. All authorized reproductions must be
 * marked with this language.
 * 
 * EXCEPT AS SET FORTH IN THE APPLICABLE LICENSE AGREEMENT, TO THE EXTENT
 * PERMITTED BY APPLICABLE LAW, CA PROVIDES THIS SOFTWARE WITHOUT WARRANTY OF
 * ANY KIND, INCLUDING WITHOUT LIMITATION, ANY IMPLIED WARRANTIES OF
 * MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE. IN NO EVENT WILL CA BE
 * LIABLE TO THE END USER OR ANY THIRD PARTY FOR ANY LOSS OR DAMAGE, DIRECT OR
 * INDIRECT, FROM THE USE OF THIS SOFTWARE, INCLUDING WITHOUT LIMITATION, LOST
 * PROFITS, BUSINESS INTERRUPTION, GOODWILL, OR LOST DATA, EVEN IF CA IS
 * EXPRESSLY ADVISED OF SUCH LOSS OR DAMAGE.
 * 
 * Author : BALRA06
 * 
 */
package com.ca.apm.tests.bpem;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.junit.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.ca.apm.automation.action.flow.em.DeployEMFlowContext;
import com.ca.apm.commons.coda.common.ApmbaseConstants;
import com.ca.apm.commons.coda.common.ApmbaseUtil;
import com.ca.apm.commons.common.CLWCommons;
import com.ca.apm.commons.common.TestUtils;
import com.ca.apm.tests.agentcontrollability.AgentControllabilityConstants;


public class InMemoryDataCacheTests extends AgentControllabilityConstants {

    private static final Logger LOGGER = LoggerFactory.getLogger(InMemoryDataCacheTests.class);    	
	CLWCommons clw = new CLWCommons();
	TestUtils tutil = new TestUtils();
	ApmbaseUtil apmutil = new ApmbaseUtil();
	private final String emMachineId;
    private final String emRoleId; 
	private final String emHost;
	private final int emPort;     
    private final String emLibDir;
    private final String emconfigFile;
    private final String emconfigFile_bckup;
    private final String user;
    private final String password;       
    private final String emLogFile;
    private final String queryLogFile;
    private final String tomcatRoleId; 
    private final String tomcatAgentExp;
    private final String metricExp;    
    private boolean metricFound;
    private boolean keywordFound;
    private boolean verifyData;
    private String testcaseId;
    private String testCaseNameIDPath;
    private String msg;
    List<String> keyWords = new ArrayList<String>();
    ArrayList<String> rolesInvolved = new ArrayList<String>();   

    public InMemoryDataCacheTests() {
    	
    	emMachineId = AgentControllabilityConstants.EM_MACHINE_ID;
    	emRoleId = AgentControllabilityConstants.EM_ROLE_ID; 
    	emHost = envProperties.getMachineHostnameByRoleId(EM_ROLE_ID);
    	emPort = Integer.parseInt(envProperties.getRolePropertiesById(EM_ROLE_ID).getProperty("emPort"));
    	emLibDir = envProperties.getRolePropertyById(EM_ROLE_ID, DeployEMFlowContext.ENV_EM_LIB_DIR);
    	user = ApmbaseConstants.emUser;
		password = ApmbaseConstants.emPassw;
    	emconfigFile = envProperties.getRolePropertyById(EM_ROLE_ID, DeployEMFlowContext.ENV_EM_CONFIG_FILE);
    	emconfigFile_bckup = emconfigFile + "_backup";
    	emLogFile = envProperties.getRolePropertyById(EM_ROLE_ID, DeployEMFlowContext.ENV_EM_LOG_FILE);
    	queryLogFile = envProperties.getRolePropertyById(EM_ROLE_ID, DeployEMFlowContext.ENV_EM_LOG_DIR)+"querylog.txt";
    	tomcatRoleId = AgentControllabilityConstants.TOMCAT_ROLE_ID;        
        tomcatAgentExp = ".*Tomcat.*";     
        metricExp = "GC Heap.*";    
        metricFound = false;
		keywordFound = false;
		verifyData = false;
		testcaseId = "null";
		testCaseNameIDPath = "null";
    }

    @BeforeClass(alwaysRun = true)
    public void initialize() {
        
    	// backup em config file
	   	backupFile(emconfigFile, emconfigFile_bckup, emMachineId);
    	
    	// sync time on EM and Agent machine
    	List<String> machines = new ArrayList<String>();
        machines.add(EM_MACHINE_ID);
        machines.add(TOMCAT_MACHINE_ID);
        syncTimeOnMachines(machines);         
        
    }  
      
    @Test(groups = { "Smoke" }, enabled = true)
    public void verify_ALM_348651_verify_in_memory_cache_property_added_in_EM_properties_file() throws Exception {
    	
    	testcaseId="348651";
    	testCaseNameIDPath = "verify_ALM_348651_verify_in_memory_cache_property_added_in_EM_properties_file";
    	testCaseStart(testCaseNameIDPath);    	
    	
    	keyWords.add("# The number of (15-second) data elements to be retained in the high-speed memory cache.");
    	keyWords.add("# Range is 32 (8 minutes) to 11520 (48 hours). A larger cache is faster but uses more memory.");
    	keyWords.add("# A setting of 240 is recommended in a 64-bit JVM with 4GB of memory, but see Sizing Guide");
    	keyWords.add("# requirements before increasing this value.");
    	keyWords.add("# Note: NOT a hot property. Changing the cache requires an EM restart.");
    	keyWords.add("introscope.enterprisemanager.memoryCache.elements=32");
    	
    	isKeywordInFile(envProperties, EM_MACHINE_ID, emconfigFile, keyWords);    	    	
    
    }
    
    @Test(groups = { "Smoke" }, enabled = true)
    public void verify_ALM_348852_verify_the_default_behavior_of_In_memory_cache_property() throws Exception {
    
    	testcaseId="348852";
    	testCaseNameIDPath = "verify_ALM_348852_verify_the_default_behavior_of_In_memory_cache_property";
    	rolesInvolved.add(emRoleId);
        rolesInvolved.add(tomcatRoleId);
    	testCaseStart(testCaseNameIDPath);    	
    	
    	startEM(emRoleId);
    	startTomcatAgent(tomcatRoleId);
    	
    	LOGGER.info("Sleeping for 8 minutes for the EM to collect data");
    	harvestWait(480);    	
    	metricFound = testInMemoryCacheBehavior("cache",4);
    	Assert.assertTrue("Cache Queries per Interval metric is not found", metricFound);    	
    	
    }      
    
    @Test(groups = { "Smoke" }, enabled = true)
    public void verify_ALM_348802_Verify_the_minimum_value_that_can_be_set_to_In_Memory_cache_property() throws Exception {
    	
    	testcaseId="348802";
    	testCaseNameIDPath = "verify_ALM_348802_Verify_the_minimum_value_that_can_be_set_to_In_Memory_cache_property";
    	testCaseStart(testCaseNameIDPath); 
    	rolesInvolved.add(emRoleId);
        rolesInvolved.add(tomcatRoleId);
    	setInMemoryCachePropertyValue("10");
    	
    	startEM(emRoleId);
    	startTomcatAgent(tomcatRoleId);    	
    	
    	msg = "[WARN] [main] [Manager.Agent] Using default for introscope.enterprisemanager.memoryCache.elements as override property is invalid: value less than 32";
    	keywordFound = checkForKeyword(envProperties, emMachineId, emLogFile, msg);
    	Assert.assertTrue("Keyword" + msg +" is not found", keywordFound);
    	
    	LOGGER.info("Sleeping for 8 minutes for the EM to collect data");
    	harvestWait(480);
    	metricFound = testInMemoryCacheBehavior("cache",4);
    	Assert.assertTrue("Cache Queries per Interval metric is not found", metricFound);    	
    	
    } 
    
    @Test(groups = { "Smoke" }, enabled = true)
    public void verify_ALM_348803_Verify_the_maximum_value_that_can_be_set_to_In_Memory_cache_property() throws Exception {
    	
    	testcaseId="348803";
    	testCaseNameIDPath = "verify_ALM_348803_Verify_the_maximum_value_that_can_be_set_to_In_Memory_cache_property";
    	rolesInvolved.add(emRoleId);        
    	testCaseStart(testCaseNameIDPath);
    	setInMemoryCachePropertyValue("12000");
    	
    	startEM(emRoleId);
    	
    	msg = "[WARN] [main] [Manager.Agent] Using default for introscope.enterprisemanager.memoryCache.elements as override property is invalid: value more than 11520";
    	keywordFound = checkForKeyword(envProperties, emMachineId, emLogFile, msg);
    	Assert.assertTrue("Keyword" + msg +" is not found", keywordFound);
    	
    }
    
    @Test(groups = { "Deep" }, enabled = true)
    public void verify_ALM_348804_Verify_that_In_Memory_cache_property_value_truncates_to_next_minimum_value() throws Exception {
    	
    	testcaseId="348804";
    	testCaseNameIDPath = "verify_ALM_348804_Verify_that_In_Memory_cache_property_value_truncates_to_next_minimum_value";
    	rolesInvolved.add(emRoleId);       
    	testCaseStart(testCaseNameIDPath);
    	setInMemoryCachePropertyValue("243");
    	
    	startEM(emRoleId);
    	
    	msg = "[INFO] [main] [Manager.Agent] Setting cache size to hold 224 elements";
    	keywordFound = checkForKeyword(envProperties, emMachineId, emLogFile, msg);
    	Assert.assertTrue("Keyword" + msg +" is not found", keywordFound);
    	
    } 
    
    @Test(groups = { "Smoke" }, enabled = true)
    public void verify_ALM_348805_Verify_that_In_Memory_cache_property_value_with_negative_number() throws Exception {
    	
    	testcaseId="348805";
    	testCaseNameIDPath = "verify_ALM_348805_Verify_that_In_Memory_cache_property_value_with_negative_number";
    	rolesInvolved.add(emRoleId);
        rolesInvolved.add(tomcatRoleId);
    	testCaseStart(testCaseNameIDPath);    	
    	LOGGER.info("Setting a Negative value to the In Memory cache property");
    	setInMemoryCachePropertyValue("-2");    	
    	
    	startEM(emRoleId); 
    	startTomcatAgent(tomcatRoleId);
    	
    	msg = "[WARN] [main] [Manager.Agent] Using default for introscope.enterprisemanager.memoryCache.elements as override property is invalid: value less than 32";
    	keywordFound = checkForKeyword(envProperties, emMachineId, emLogFile, msg);
    	Assert.assertTrue("Keyword" + msg +" is not found", keywordFound);    	
    	
    	LOGGER.info("Sleeping for 8 minutes for the EM to collect data");
    	harvestWait(480);
    	metricFound = testInMemoryCacheBehavior("cache",4);
    	Assert.assertTrue("Cache Queries per Interval metric is not found", metricFound);    	
    	
    }
    
    @Test(groups = { "Smoke" }, enabled = true)
    public void verify_ALM_455013_Verify_that_In_Memory_cache_property_value_with_blank_value() throws Exception {
    	
    	testcaseId="455013";
    	testCaseNameIDPath = "verify_ALM_455013_Verify_that_In_Memory_cache_property_value_with_blank_value";
    	rolesInvolved.add(emRoleId);
        rolesInvolved.add(tomcatRoleId);
    	testCaseStart(testCaseNameIDPath);    	
    	LOGGER.info("Setting a Blank value to the In Memory cache property");
    	setInMemoryCachePropertyValue(" ");
    	
    	startEM(emRoleId);
    	startTomcatAgent(tomcatRoleId);  
    	
    	msg = "[INFO] [main] [Manager.Agent] Setting cache size to hold 32 elements";
    	keywordFound = checkForKeyword(envProperties, emMachineId, emLogFile, msg);
    	Assert.assertTrue("Keyword" + msg +" is not found", keywordFound);  	  	
    	
    	LOGGER.info("Sleeping for 8 minutes for the EM to collect data");
    	harvestWait(480);    	
    	try {
    		isKeywordInFile(envProperties, emMachineId, emLogFile, "[ERROR] [main] [Manager.Agent]");
    		Assert.assertTrue(false);
        } catch (Exception e) {
        	Assert.assertTrue(true);
        	LOGGER.info("Errors seen in EM logs when In Memory Cache property has a blank value");
        }   
    	
    	metricFound = testInMemoryCacheBehavior("cache",4);
    	Assert.assertTrue("Cache Queries per Interval metric is not found", metricFound);    	
    	
    }
    
    @Test(groups = { "Smoke" }, enabled = true)
    public void verify_ALM_348810_Verify_the_functionality_by_removing_In_memory_cache_property() throws Exception {
    	
    	testcaseId="348810";
    	testCaseNameIDPath = "verify_ALM_348810_Verify_the_functionality_by_removing_In_memory_cache_property";
    	rolesInvolved.add(emRoleId);
        rolesInvolved.add(tomcatRoleId);
    	testCaseStart(testCaseNameIDPath); 
    	LOGGER.info("Removing the In memory cache property from the em config file");
    	replaceProp("introscope.enterprisemanager.memoryCache.elements=32", "", emMachineId,emconfigFile);
    	
    	startEM(emRoleId);
    	startTomcatAgent(tomcatRoleId);
    	
    	LOGGER.info("Sleeping for 8 minutes for the EM to collect data");
    	harvestWait(480);
    	metricFound = testInMemoryCacheBehavior("cache",4);
    	Assert.assertTrue("Cache Queries per Interval metric is not found", metricFound);     	
    	
    }    
    
    @Test(groups = { "Smoke" }, enabled = true)
    public void verify_ALM_348806_Verify_whether_historical_data_is_getting_queried_from_cache_based_on_the_value_set_in_In_Memory_cache_property() throws Exception {
    	
    	testcaseId="348806";
    	testCaseNameIDPath = "verify_ALM_348806_Verify_whether_historical_data_is_getting_queried_from_cache_based_on_the_value_set_in_In_Memory_cache_property";
    	rolesInvolved.add(emRoleId);
        rolesInvolved.add(tomcatRoleId);
    	testCaseStart(testCaseNameIDPath); 
    	setInMemoryCachePropertyValue("64");
    	
    	startEM(emRoleId);
    	startTomcatAgent(tomcatRoleId);
    	
    	LOGGER.info("Sleeping for 20 minutes for the EM to collect data");
    	harvestWait(1200);
    	metricFound = testInMemoryCacheBehavior("cache",14);
    	Assert.assertTrue("Cache Queries per Interval metric is not found", metricFound);     	   	
    	
    }
    
    @Test(groups = { "Smoke" }, enabled = true)
    public void verify_ALM_348808_Verify_whether_historical_data_is_getting_queried_from_SmartStor_based_on_the_value_set_in_In_Memory_cache_property() throws Exception {
    	
    	testcaseId="348808";
    	testCaseNameIDPath = "verify_ALM_348808_Verify_whether_historical_data_is_getting_queried_from_SmartStor_based_on_the_value_set_in_In_Memory_cache_property";
    	rolesInvolved.add(emRoleId);
        rolesInvolved.add(tomcatRoleId);
    	testCaseStart(testCaseNameIDPath);
    	setInMemoryCachePropertyValue("64");
    	
    	startEM(emRoleId);
    	startTomcatAgent(tomcatRoleId);
    	
    	LOGGER.info("Sleeping for 20 minutes for the EM to collect data");
    	harvestWait(1200);
    	metricFound = testInMemoryCacheBehavior("smartstor",17);
    	Assert.assertTrue("Smartstor Queries per Interval metric is not found", metricFound);    	
    	
    } 
    
    @Test(groups = { "Smoke" }, enabled = true)
    public void verify_ALM_348807_Verify_whether_data_coming_from_cache_and_smartstor_are_same() throws Exception {
    	    			
    	testcaseId="348807";
    	testCaseNameIDPath = "verify_ALM_348807_Verify_whether_data_coming_from_cache_and_smartstor_are_same";
    	rolesInvolved.add(emRoleId);
        rolesInvolved.add(tomcatRoleId);
    	testCaseStart(testCaseNameIDPath);
    	
    	startEM(emRoleId);
    	startTomcatAgent(tomcatRoleId);	
    	LOGGER.info("Sleeping for 20 minutes for the EM to collect data");
    	harvestWait(1200);    			
    	
    	DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    	Calendar cal = Calendar.getInstance();
    	String currentTime = dateFormat.format(cal.getTime());
    	LOGGER.info("Current Date Time : " + currentTime);    			
    	cal.add(Calendar.MINUTE, -5);
    	String pastFiveMiuntesTime = dateFormat.format(cal.getTime());
    	LOGGER.info("Subtract five minutes from current date : " + pastFiveMiuntesTime);    	
    	cal.add(Calendar.MINUTE, -15);
    	String pastTwentyMinutesTime = dateFormat.format(cal.getTime());
    	LOGGER.info("Subtract twenty minutes from current date : " + pastTwentyMinutesTime);
    	
    	LOGGER.info("Querying for data in Cache");   	
    	List<String> cacheData = clw.getMetricValueInTimeRange(user, password, tomcatAgentExp, metricExp, emHost, emPort, emLibDir, pastFiveMiuntesTime, currentTime);
    	LOGGER.info("Querying for data in SmartStor");    	
    	List<String> smartstoreData = clw.getMetricValueInTimeRange(user, password, tomcatAgentExp, metricExp, emHost, emPort, emLibDir, pastTwentyMinutesTime, currentTime);
    	
    	LOGGER.info("Comparing the Data coming from Cache and Smartstor" );
    	verifyData = tutil.containsList(cacheData, smartstoreData);
    	Assert.assertTrue("Cache Data is different from SmartStor Data",verifyData);     	
    	
    } 
    
    @Test(groups = { "Smoke" }, enabled = true)
    public void verify_ALM_348811_Verify_whether_data_retrieval_from_cache_is_taking_less_time_compared_with_smartstor() throws Exception {
    	    			
    	testcaseId="348811";
    	testCaseNameIDPath = "verify_ALM_348811_Verify_whether_data_retrieval_from_cache_is_taking_less_time_compared_with_smartstor";
    	rolesInvolved.add(emRoleId);
        rolesInvolved.add(tomcatRoleId);
    	testCaseStart(testCaseNameIDPath);
    	setInMemoryCachePropertyValue("64");    	
    	replaceProp("log4j.logger.Manager.QueryLog=INFO, querylog",
    	               "log4j.logger.Manager.QueryLog=DEBUG, querylog", emMachineId,emconfigFile);
    	
    	startEM(emRoleId);
    	startTomcatAgent(tomcatRoleId);	
    	LOGGER.info("Sleeping for 20 minutes for the EM to collect data");
    	harvestWait(1200); 
    	
    	clw.getMetricValueForTimeInMinutes(user, password, tomcatAgentExp, metricExp, emHost, emPort, emLibDir, 14);
    	int cacheQueryTime = apmutil.queryrunTime(queryLogFile);
    	harvestWait(30);
    	clw.getMetricValueForTimeInMinutes(user, password, tomcatAgentExp, metricExp, emHost, emPort, emLibDir, 17);
    	int smstrQueryTime = apmutil.queryrunTime(queryLogFile);    	
		Assert.assertTrue("SmartStor Query took more time than Cache Query",cacheQueryTime < smstrQueryTime);
		copyFile(queryLogFile, queryLogFile + "_" + testcaseId, emMachineId);	
    	
    }    
        
    /**
	 * This method checks the default or cached behavior of InMemoryCache Property 
	 * based on the type of data and input time  
	 * @return Returns boolean value based on the cache metric value check
	 * @parameters  
	 * datatype - type of the date either cache or smartstor
	 * minutes - time for which you want to run the clw query
	 * @throws Exception 
	 */  
    public boolean testInMemoryCacheBehavior(String datatype, int minutes) throws Exception {    	
		
    	LOGGER.info("Inside testInMemoryCacheBehavior");
		List<String> datatypeMetricValue = new ArrayList<String>();
		List<String> clwlist = clw.getMetricValueForTimeInMinutes(user, password, tomcatAgentExp, metricExp, emHost, emPort, emLibDir, minutes);
    	LOGGER.info("List size after getting the historic metric values for GC Heap is " + clwlist.size() );
		harvestWait(120);
		if(datatype.equals("cache")) {
			datatypeMetricValue = getCacheMetricValue();
		} else if (datatype.equals("smartstor")) {
			datatypeMetricValue = getSmartstorMetricValue();
		} else {
			Assert.assertTrue("Not a valid datatype input",false);
		}
			
    	for (String valFound : datatypeMetricValue) {    		
    		if(Integer.parseInt(valFound) >= 1) {
				metricFound = true;
			}
    	}
    	return metricFound;
    }
        
    /**
	 * This method returns a list which has past 17 minutes 
	 * data of the metric Smartstor Queries Per Interval
	 * @throws Exception 
	 */  
    public List<String> getSmartstorMetricValue() throws Exception {
    	
    	LOGGER.info("Inside getSmartstorMetricValue");
    	String agentExpression =
				"(.*)\\|Custom Metric Process \\(Virtual\\)\\|Custom Metric Agent \\(Virtual\\)(.*)";
		String cachequeriesmetricExp =
            	"Enterprise Manager\\|Internal\\|Query:Smartstor Queries Per Interval";		
		harvestWait(60);		
		List<String> smartstorMetricValues = clw.getHistoricMetricValuesForTimeInMinutes(user, password, agentExpression, cachequeriesmetricExp, emHost, emPort, emLibDir, 17);
		return smartstorMetricValues;
		
    }
    
    /**
   	 * This method returns a list which has past 7 minutes 
   	 * data of the metric Cache Queries Per Interval
   	 * @throws Exception 
   	 */     
    public List<String> getCacheMetricValue() throws Exception {
    	   
       	LOGGER.info("Inside getCacheMetricValue");
       	String agentExpression =
   				"(.*)\\|Custom Metric Process \\(Virtual\\)\\|Custom Metric Agent \\(Virtual\\)(.*)";
   		String cachequeriesmetricExp =
               	"Enterprise Manager\\|Internal\\|Query:Cache Queries Per Interval";		
   		harvestWait(60);		
   		List<String> cacheMetricValues = clw.getHistoricMetricValuesForTimeInMinutes(user, password, agentExpression, cachequeriesmetricExp, emHost, emPort, emLibDir, 7);
   		return cacheMetricValues;
   		
    }
    
    /**
     * This methods sets the value of the memoryCache elements property
     * @params memoryCachevalue      
     */    
    public void setInMemoryCachePropertyValue(String memoryCacheValue) {
    	   
    	 replaceProp("introscope.enterprisemanager.memoryCache.elements=32",
                 "introscope.enterprisemanager.memoryCache.elements="+memoryCacheValue, emMachineId,emconfigFile);
    }  	
    
    @AfterMethod(alwaysRun = true)
	public void stopservicesandrevertchanges() {
		
    	stopEM(emRoleId);
    	stopEMServiceFlowExecutor(emMachineId);
		try {
			LOGGER.info("Stopping tomcat");
			stopTomcatAgent(tomcatRoleId);
		} catch (Exception e) {
			LOGGER.info("Tomcat stop failed because of exception "+e);
		} 	
		renameLogWithTestCaseId(rolesInvolved, testcaseId);
		rolesInvolved.clear();
		revertFile(emconfigFile, emconfigFile_bckup, emMachineId);
		testCaseEnd(testCaseNameIDPath);
		
	}
	
}
