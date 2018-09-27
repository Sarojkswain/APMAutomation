/*
 * Copyright (c) 2017 CA. All rights reserved.
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
 */

package com.ca.apm.saas.standalone;

import static com.ca.tas.testbed.ITestbedMachine.TEMPLATE_W64;

/**
 *
 * @author banda06
 * @author ahmal01
 */

public interface FLDStandAloneConstants {

	 public static final String EM_MACHINE_ID = "emMachine";
	 public static final String EM_ROLE_ID = "emRole";
	 
	 public static final String TEMPLATE_ID = TEMPLATE_W64;
     public static final String SA_MASTER = EM_MACHINE_ID;
     public static final String SA_MASTER_ROLE = EM_ROLE_ID; 
     public static final String SELENIUM_HUB_ROLE_ID = "seleniumHubRole";
     public static final String SELENIUM_GRID_ROLE_ID = "seleniumNodeRole";
     public static final String SELENIUM_MACHINE_ID = "seleniumMachineId";
     public static final String SELENIUM_HUB_MACHINE_ID = SA_MASTER;
     

     public static final String DRIVERS_PATH = "C:\\sw\\seleniumdrivers";
     public static final String CHROME_DRIVER_PATH = DRIVERS_PATH + "\\chrome";
     public static final String MSIE_DRIVER_PATH = DRIVERS_PATH + "\\msie32b";
     public static final String DEPLOY_DIR = "C:\\sw";
     public static final String H2_LOCATION = DEPLOY_DIR + "\\H2";
     public static final String H2_VERSION = "1.4.191";

	 
	 
	 public static final String TOMCAT_6_MACHINE_ID = "tomcat6Machine";
	 public static final String TOMCAT_7_MACHINE_ID = "tomcat7Machine";
	 public static final String TOMCAT_9080_MACHINE_ID = "tomcat9080Machine";
	 public static final String TOMCAT_9081_MACHINE_ID = "tomcat9081Machine";
	 public static final String[] TOMCAT_MACHINE_IDS = {TOMCAT_6_MACHINE_ID, TOMCAT_9080_MACHINE_ID};
	 
	 public static final String TOMCAT_6_ROLE_ID = "tomcat6Role";
	 public static final String TOMCAT_6_AGENT_ROLE_ID = "tomcat6AgentRole";
	 public static final String TOMCAT_7_ROLE_ID = "tomcat7Role";
	 public static final String TOMCAT_7_AGENT_ROLE_ID = "tomcat7AgentRole";
	 public static final String TOMCAT_9080_ROLE_ID = "tomcat9080Role";
	 public static final String TOMCAT_9080_AGENT_ROLE_ID = "tomcat9080AgentRole";
	 public static final String TOMCAT_9081_ROLE_ID = "tomcat9081Role";
	 public static final String TOMCAT_9081_AGENT_ROLE_ID = "tomcat9081AgentRole";
	 public static final String[] TOMCAT_ROLE_IDS = {TOMCAT_6_ROLE_ID, TOMCAT_7_ROLE_ID, TOMCAT_9080_ROLE_ID, TOMCAT_9081_ROLE_ID};

	 public static final String JBOSS01_HOST_NAME = "fldjboss01";
	 public static final String TOMCAT_HOST_NAME = "fldtomcat01";
	 public static final String WAS_HOST_NAME = "fldwas01";
	 public static final String TOMCAT6_AGENT = "Tomcat6";
	 public static final String TOMCAT7_AGENT = "Tomcat7";
	 public static final String TOMCAT_AGENT_9080 = "TomcatAgent_9080";
	 public static final String TOMCAT_AGENT_9081 = "TomcatAgent_9081";
	 public static final String JBOSS_AGENT = "JbossAgent";
	 public static final String WAS85_AGENT = "WebSphere85";
	    
	 public static final String JBOSS_MACHINE = "JbossMachine";
	 public static final String JBOSS6_ROLE_ID = JBOSS_MACHINE + "-JBoss6";
	 public static final String JBOSS7_ROLE_ID = JBOSS_MACHINE + "-JBoss7";
	   
	 public static final String WEBSPHERE_01_MACHINE_ID = "websphere01";
	 public static final String WEBSPHERE_02_MACHINE_ID = "websphere02";
	 public static final String WEBSPHERE_03_MACHINE_ID = "websphere03";
	 public static final String WEBSPHERE_01_ROLE_ID = "websphereRole01";
	 public static final String WEBSPHERE_02_ROLE_ID = "websphereRole02";
	 public static final String WEBSPHERE_03_ROLE_ID = "websphereRole03";
	    
	 public static final String TOMCAT_6_AXIS2_ROLE_ID = "tomcat6axis2Role";
	 public static final String TOMCAT_6_QATESTAPP_ROLE_ID = "tomcat6qatestappRole";
	 public static final String TOMCAT_6_TESTAPP_ROLE_ID = "tomcat6testappRole";
	 public static final String TOMCAT_7_AXIS2_ROLE_ID = "tomcat7axis2Role";
	 public static final String TOMCAT_7_QATESTAPP_ROLE_ID = "tomcat7qatestappRole";
	 public static final String TOMCAT_9080_AXIS2_ROLE_ID = "tomcat9080axis2Role";
	 public static final String TOMCAT_9080_WURLITZER_ROLE_ID = "tomcat9080wurlitzerRole";
	 public static final String TOMCAT_9081_AXIS2_ROLE_ID = "tomcat9081axis2Role";
	 public static final String TOMCAT_9081_WURLITZER_ROLE_ID = "tomcat9081wurlitzerRole";
	 
	 public static final String WAS_XCLUSTER_CLIENT_ROLE_ID = "wasXClusterRole";
	 public static final String LOAD1_ROLE_ID = "loadRole1";
	 public static final String LOAD2_ROLE_ID = "loadRole2";
	 public static final String LOAD3_ROLE_ID = "loadRole3";
	    
	 public static final String EM_VERSION = "99.99.ttvi_stable-SNAPSHOT";
	 public static final String WLS_ROLE_ID = "wls12c";
	 public static final String WLS_ROLE2_ID = "wls12c_role2";
	 public static final String WURLITZER_ROLE_ID = "wurlitzerRole";
	 public static final String CLW_ROLE_ID = "clwRole";
	 public static final String HVR_ROLE_ID = "hvrRole";
	 public static final String WLSMACHINE_1 = "wlsmachine1";
	 public static final String WLSMACHINE_2 = "wlsmachine2";
	 public static final String WLSCLIENT_1 = "wlsclient1";
	 public static final String WLSCLIENT_2 = "wlsclient2";
	 public static final String LOAD_MACHINE1_ID = "loadMachine1";
	 public static final String EM_TEMPLATE_ID = "co66";
	 public static final String WIN_TEMPLATE_ID = "w64";
	 public static final String LOADMACHINE_TEMPLATE_ID = "w64_16g";	
		
	 public static final String JMETER_LOAD1 = "jmeter1";
	 public static final String JMETER_LOAD2 = "jmeter2";
	 public static final String JMETER_LOAD3 = "jmeter3";
	 public static final String JMETER_LOAD4 = "jmeter4";
	 public static final String JMETER_LOAD5 = "jmeter5";
	 public static final String JMETER_LOAD6 = "jmeter6";
	 public static final String JMETER_LOAD7 = "jmeter7";
	 public static final String JMETER_LOAD8 = "jmeter8";
	 public static final String JMETER_LOAD9 = "jmeter9";
		
}

