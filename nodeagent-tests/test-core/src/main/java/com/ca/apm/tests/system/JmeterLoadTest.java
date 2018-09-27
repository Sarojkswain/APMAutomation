package com.ca.apm.tests.system;

import java.util.Arrays;

import com.ca.apm.tests.utils.CommonUtils;


/**
 * @author kurma05
 */
public class JmeterLoadTest extends SystemBaseTest {
    
    protected void testTixChangeLoad() {
	 
	    setupTest();
	    testTixChangeGeneric("|Frontends|Apps|server:Responses Per Interval", "tixChange_load.jmx");
	}
	
	protected void testTixChangeErrors() {
       
	    updateHeapSettings("10", "20");
	    umAgentConfig.updateProperty("introscope.agent.errorsnapshots.enable", "true");
	    umAgentConfig.updateProperty("introscope.agent.errorsnapshots.throttle", "5000");
	    
	    setupTest();
	    testTixChangeGeneric("|Frontends|Apps|server:Errors Per Interval", "tixChange_errors.jmx");
    }
	
	protected void testTixChangeTTSampling() {
       
	    updateHeapSettings("20", "30");
	    umAgentConfig.updateProperty("introscope.agent.transactiontracer.sampling.enabled", "true");
	    umAgentConfig.updateProperty("introscope.agent.transactiontracer.sampling.perinterval.count", "10000");
	    umAgentConfig.updateProperty("introscope.agent.transactiontracer.sampling.interval.seconds", "1");

	    setupTest();
        testTixChangeGeneric("|Frontends|Apps|server:Responses Per Interval", "tixChange_load.jmx");
    }
	
	protected void testTixChangeStalls() {
       
	    updateHeapSettings("8", "10");
        umAgentConfig.updateProperty("introscope.agent.stalls.thresholdseconds", "1");
        umAgentConfig.updateProperty("introscope.agent.stalls.resolutionseconds", "1");

        setupTest();
        testTixChangeGeneric("|Frontends|Apps|server:Stall Count", "1", "tixChange_stalls.jmx");
    }
	
	protected void testTixChangeManyUrlGroupsMetricClamp() {
     
	    updateHeapSettings("16", "60");
	    
	    //update agent profile
        umAgentConfig.updateProperty("introscope.agent.urlgroup.frontend.url.clamp", "-1");
        umAgentConfig.updateProperty("introscope.agent.urlgroup.backend.url.clamp", "-1");
	    umAgentConfig.updateProperty("introscope.agent.metricClamp", "10000");
	    umAgentConfig.updateProperty("introscope.agent.urlgroup.keys", "custom1,custom2");
	    umAgentConfig.updateProperty("introscope.agent.urlgroup.group.custom1.pathprefix", "/rest/cart");
	    umAgentConfig.updateProperty("introscope.agent.urlgroup.group.custom1.format", "{path_substring:0:12}");
	    umAgentConfig.updateProperty("introscope.agent.urlgroup.group.custom2.pathprefix", "/rest/account");
        umAgentConfig.updateProperty("introscope.agent.urlgroup.group.custom2.format", "{path_substring:0:16}");
	    
        setupTest();
	    testTixChangeGeneric("|Frontends|Apps|.*|URLs|/rest/account/8.*:Responses Per Interval", "1", "tixChange_load.jmx");
    }
	
	protected void testTixChangeManyUrlGroups() {
     
        updateHeapSettings("250", "350");
        
        //update agent profile   
        umAgentConfig.updateProperty("introscope.agent.urlgroup.frontend.url.clamp", "-1");
        umAgentConfig.updateProperty("introscope.agent.urlgroup.backend.url.clamp", "-1");
        umAgentConfig.updateProperty("introscope.agent.urlgroup.keys", "custom");
        umAgentConfig.updateProperty("introscope.agent.urlgroup.group.custom.pathprefix", "*");
        umAgentConfig.updateProperty("introscope.agent.urlgroup.group.custom.format", "{path_substring:0:100}");
        
        setupTest();
        testTixChangeGeneric("|Frontends|Apps|.*|URLs|/api/Accounts/user10.*:Responses Per Interval", "10", "tixChange_load.jmx");
    }

	protected void testTixChangeMongoDb() {
      
	    updateHeapSettings("7", "10");
	    setupTest();
        testTixChangeGeneric("|Backends|.*MongoDB.*|find:Responses Per Interval", "20", "tixChange_mongodb.jmx");
    }
	
	protected void testTixChangeHttpBackend() {
      
	    updateHeapSettings("25", "50");
	    setupTest();
        testTixChangeGeneric("|Backends|WebService at http_//localhost_3000:Responses Per Interval", "tixChange_http.jmx");
    }
	
	protected void testTixChangeHttpBackendNoFrontend() {
      
	    tixChangeConfig.updateProperty("useRequestQueue", Boolean.TRUE);  // to disassociate backends from frontends in tixchange
        updateHeapSettings("15", "20");
        setupTest();
        testTixChangeGeneric("|Backends|WebService at http_//localhost_3000:Responses Per Interval", "200", "tixChange_http.jmx");
    }
	
	protected void testTixChangeAsync() {
      
        updateHeapSettings("25", "50");
        setupTest();
        testTixChangeGeneric("|Backends|fs.*:Responses Per Interval", "1000", "tixChange_async.jmx");
    }
	
	protected void testTixChangeRestartCollector() {
       
        setupTest();
        //start "restart collector" thread
        startBounceIAThread(getTestDuration(), 120000);        
        //run test
        expectedErrorMessages = Arrays.asList(CommonUtils.EXPECTED_ERRORS);
        expectedErrorMessages.add(".*Failed to accept ARF connection.*");
        testTixChangeGeneric("|Frontends|Apps|server:Responses Per Interval", "tixChange_load.jmx");
    }
	
	//slc not supported yet in 10.1/10.2
	protected void testTixChangeClusterLoad() {
	   
	    setupClusterTest("4");
	    testTixChangeGeneric("|Frontends|Apps|server:Responses Per Interval", "tixChange_load.jmx");
	}
}
