/*
 * Copyright (c) 2014 CA. All rights reserved.
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
 * Author : GAMSA03/ SANTOSH JAMMI
 * Author : KETSW01/ KETHIREDDY SWETHA
 * Author : JAMSA07/ SANTOSH JAMMI
 * Date : 20/11/2015
 */
package com.ca.apm.commons.tests;

import java.util.HashMap;
import java.util.Map;

import junit.framework.Assert;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;

import com.ca.apm.automation.action.flow.em.DeployEMFlowContext;
import com.ca.apm.commons.common.TestUtils;
import com.ca.apm.commons.coda.common.ApmbaseUtil;
import com.ca.apm.commons.coda.common.XMLUtil;
import com.ca.apm.commons.testbed.CommonsLinuxTestbed;
import com.ca.apm.commons.testbed.CommonsWindowsTestbed;
import com.ca.tas.test.TasTestNgTest;
import com.ca.tas.tests.annotations.Tas;
import com.ca.tas.tests.annotations.TestBed;
import com.ca.tas.type.SizeType;



public class TestCommonsXMLUtilLinux extends TasTestNgTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(TestCommonsXMLUtilLinux.class);
    TestUtils utility = new TestUtils();
    XMLUtil x = new XMLUtil();

    private final String emRoleId;
    private final String emConfigDir;
    
    
    public TestCommonsXMLUtilLinux() {
        emRoleId = CommonsLinuxTestbed.EM_ROLE_ID;   
        emConfigDir = envProperties.getRolePropertyById(emRoleId, DeployEMFlowContext.ENV_EM_CONFIG_DIR);      
     }
    
    /**
	 * Checks if the element contains the attribute name and corresponding value	
	 */       
    @Tas(testBeds = @TestBed(name = CommonsLinuxTestbed.class, executeOn = CommonsLinuxTestbed.EM_MACHINE_ID), size = SizeType.COLOSSAL, owner = "balra06")
    @Test
    public void containsElementsTest() {        	
        	
            try {            	
            	Assert.assertTrue( XMLUtil.containsElements(emConfigDir+"/users.xml", "principals", "plainTextPasswords", "false")); 
            	Assert.assertTrue( XMLUtil.containsElements(emConfigDir+"/domains.xml","grant","user","Guest"));
            	LOGGER.info("containsElementsTest completed");
            } catch (Exception e) {
                e.printStackTrace();
            }
     }  
     
    /**
	 * Changes the attribute value of the corresponding element	
	 */
        
    @Tas(testBeds = @TestBed(name = CommonsLinuxTestbed.class, executeOn = CommonsLinuxTestbed.EM_MACHINE_ID), size = SizeType.COLOSSAL, owner = "balra06")    
    @Test
    public void changeAttributeValue() {
            	
            try {            	
            	String msg1= XMLUtil.changeAttributeValue(emConfigDir+"/users.xml", "principals", "plainTextPasswords", "false", "true");
            	Assert.assertTrue(XMLUtil.SUCCESS_MESSAGE.equals(msg1));
            	String msg2 = XMLUtil.changeAttributeValue(emConfigDir+"/users.xml", "principals", "plainTextPasswords", "true", "false");
            	Assert.assertTrue(XMLUtil.SUCCESS_MESSAGE.equals(msg2)); 
            	LOGGER.info("changeAttributeValue completed");
            } catch (Exception e) {
                e.printStackTrace();
            }
     }
       
    /**
	 * Changes the attribute value of the element under the corresponding parent node	
	 */    
    @Tas(testBeds = @TestBed(name = CommonsLinuxTestbed.class, executeOn = CommonsLinuxTestbed.EM_MACHINE_ID), size = SizeType.COLOSSAL, owner = "balra06")
    @Test
    public void changeAttributeValueWithparentNode() {
        	        	
            try {            	
            	String msg1 = XMLUtil.changeAttributeValueWithparentNode(emConfigDir+"/domains.xml", "agent", "SuperDomain", "mapping", "(.*)", "(.*)Tomcat(.*)");
            	Assert.assertTrue(XMLUtil.SUCCESS_MESSAGE.equals(msg1));
            	String msg2 = XMLUtil.changeAttributeValueWithparentNode(emConfigDir+"/domains.xml", "agent", "SuperDomain", "mapping", "(.*)Tomcat(.*)", "(.*)");
            	Assert.assertTrue(XMLUtil.SUCCESS_MESSAGE.equals(msg2));
            	LOGGER.info("changeAttributeValueWithparentNode completed");
            } catch (Exception e) {
                e.printStackTrace();
            }
    }
        
    /**
	 * Changes the attribute value of the user under the corresponding element	
	 */        
    @Tas(testBeds = @TestBed(name = CommonsLinuxTestbed.class, executeOn = CommonsLinuxTestbed.EM_MACHINE_ID), size = SizeType.COLOSSAL, owner = "balra06")
    @Test
    public void changeAttributeValueforUser() {        	
        	
            try {
            	String msg1 = XMLUtil.changeAttributeValueforUser(emConfigDir+"/users.xml", "user", "name", "Admin", "password", "", "Admin");
            	Assert.assertTrue(XMLUtil.SUCCESS_MESSAGE.equals(msg1));  
            	String msg2 = XMLUtil.changeAttributeValueforUser(emConfigDir+"/users.xml", "user", "name", "Admin", "password", "Admin", "");
            	Assert.assertTrue(XMLUtil.SUCCESS_MESSAGE.equals(msg2)); 
            	LOGGER.info("changeAttributeValueforUser completed");
            } catch (Exception e) {
                e.printStackTrace();
            }
    }
        
    /**
	 * This method creates a new element with the given name and attributes and
	 * adds it to the parentNode.
	 */
    @Tas(testBeds = @TestBed(name = CommonsLinuxTestbed.class, executeOn = CommonsLinuxTestbed.EM_MACHINE_ID), size = SizeType.COLOSSAL, owner = "balra06")
    @Test
    public void createElementTest() {
    	
    		try {    		
    			Map<String, String> attributeMap = new HashMap<String, String>();
    			attributeMap.put("name", "testFile");    	
    			String msg1 = XMLUtil.createElement(emConfigDir+"/realms.xml","property", "", "realm","id", "Local Users and Groups", attributeMap);
    			Assert.assertTrue(XMLUtil.SUCCESS_MESSAGE.equals(msg1));
    			attributeMap.remove("name");
    			attributeMap.put("","");
    			String msg2 = XMLUtil.createElement(emConfigDir+"/realms.xml","value", "users.xml", "property","name", "testFile", attributeMap);
    			Assert.assertTrue(XMLUtil.SUCCESS_MESSAGE.equals(msg2));
    			LOGGER.info("createElementTest completed");
    		} catch (Exception e) {
    			e.printStackTrace();
    		}
    }
      
    /**
	 * This method creates a new element with the given name and attributes and
	 * adds it to the parentNode only if it has no childNodes
	 */
    @Tas(testBeds = @TestBed(name = CommonsLinuxTestbed.class, executeOn = CommonsLinuxTestbed.EM_MACHINE_ID), size = SizeType.COLOSSAL, owner = "balra06")
    @Test
    public void createElementWhennoChildTest() {
    
    	   try {
    		
    		   Map<String, String> attributeMap = new HashMap<String, String>();
    		   attributeMap.put("name", "testuser");    	
    		   String msg1 = XMLUtil.createElementWhennoChild(emConfigDir+"/users.xml","user", "", "group","name", "CEM Configuration Administrator", attributeMap);
    		   Assert.assertTrue(XMLUtil.SUCCESS_MESSAGE.equals(msg1));
    		   String msg2 = XMLUtil.createElement(emConfigDir+"/users.xml","user", "", "group","name", "CEM System Administrator", attributeMap);
    		   Assert.assertTrue(XMLUtil.SUCCESS_MESSAGE.equals(msg2));
    		   LOGGER.info("createElementWhennoChildTest completed");
    		   deleteElementTest(emConfigDir+"/users.xml","user","name","testuser");
    		   deleteElementMultipleTest(emConfigDir+"/users.xml","user","name","testuser");
    		   
    	   } catch (Exception e) {
			e.printStackTrace();
    	   }
    }
    
    /**
	 * Deletes the element with the corresponding attribute name and value in the first occurrence
	 */
    public void deleteElementTest(String xmlFilePath, String element,
			String attrName, String attrValue) {
    
    	String msg = XMLUtil.deleteElement(xmlFilePath, element, attrName, attrValue);
    	Assert.assertTrue(XMLUtil.SUCCESS_MESSAGE.equals(msg));
    	LOGGER.info("deleteElementTest completed");
    }
    
    /**
	 * Deletes the element with the corresponding attribute name and value in all the occurrences
	 */    
   public void deleteElementMultipleTest(String xmlFilePath, String element,
			String attrName, String attrValue) {
    	    	
    	String msg = XMLUtil.deleteElementMultiple(xmlFilePath,element,attrName, attrValue);
    	Assert.assertTrue(XMLUtil.SUCCESS_MESSAGE.equals(msg));
    	LOGGER.info("deleteElementMultipleTest completed");
   }

    
   /**
	 * This method creates an agent-cluster element in agentClusters.xml
	 */
    @Tas(testBeds = @TestBed(name = CommonsLinuxTestbed.class, executeOn = CommonsLinuxTestbed.EM_MACHINE_ID), size = SizeType.COLOSSAL, owner = "balra06")
    @Test
    public void createAgentClusterTest() {
    	
    	try {
    		
    		ApmbaseUtil.fileBackUp(emConfigDir+"/agentclusters.xml");
    		String[] metrSpecifiers = { "CPU.*", "GC Heap.*" };
    	
    		String msg1 = XMLUtil.createAgentCluster(emConfigDir+"/agentclusters.xml","CPUAndHeapMetricsAgAgentDomain", "SuperDomain",
			".*\\|.*\\|Web[S|L].*", metrSpecifiers);

    		String msg2 = XMLUtil.createAgentCluster(emConfigDir+"/agentclusters.xml","CPUAndHeapMetricsAgAgentDomain", "AgAgentDomain",
			".*\\|.*\\|Web[S|L].*", metrSpecifiers);
    		Assert.assertTrue(XMLUtil.SUCCESS_MESSAGE.equals(msg1) && XMLUtil.SUCCESS_MESSAGE.equals(msg2));
    		ApmbaseUtil.revertFile(emConfigDir+"/agentclusters.xml");
    		
    		LOGGER.info("createAgentClusterTest completed");
    	
    	} catch (Exception e) {
			e.printStackTrace();
 	   }
    }

    /**
	 * Creates a new domain in domains.xml
	 */
    @Tas(testBeds = @TestBed(name = CommonsLinuxTestbed.class, executeOn = CommonsLinuxTestbed.EM_MACHINE_ID), size = SizeType.COLOSSAL, owner = "balra06")
    @Test
    public void createDomainTest() { 
    	try {
    		ApmbaseUtil.fileBackUp(emConfigDir+"/domains.xml");
    		Map<String, String> usersMap = new HashMap<String, String>();
    		Map<String, String> groupsMap = new HashMap<String, String>();
    		groupsMap.put("Admin", "full");
    		usersMap.put("Guest", "read");
    				XMLUtil.createDomain(emConfigDir+"/domains.xml","TestDomain",
				"custom domain to test createDomain", "(.*)", usersMap,groupsMap);
    		ApmbaseUtil.revertFile(emConfigDir+"/domains.xml");
    		LOGGER.info("createDomainTest completed");
    	
    		} catch (Exception e) {
    			e.printStackTrace();
 	   }
    }
    
    /**
	 * Creates a new domain in domains.xml and returns a success message
	 */
    @Tas(testBeds = @TestBed(name = CommonsLinuxTestbed.class, executeOn = CommonsLinuxTestbed.EM_MACHINE_ID), size = SizeType.COLOSSAL, owner = "balra06")
    @Test
    public void createDomainWithReturnMessageTest() { 
    	try {
    		ApmbaseUtil.fileBackUp(emConfigDir+"/domains.xml");
    		Map<String, String> usersMap = new HashMap<String, String>();
    		Map<String, String> groupsMap = new HashMap<String, String>();
    		groupsMap.put("Admin", "full");
    		usersMap.put("Guest", "read");
		String msg = XMLUtil.createDomainWithReturnMessage(emConfigDir+"/domains.xml","TestDomain",
				"custom domain to test createDomainWithReturnmessage", "(.*)", usersMap, groupsMap);
		Assert.assertTrue(XMLUtil.SUCCESS_MESSAGE.equals(msg));
		ApmbaseUtil.revertFile(emConfigDir+"/domains.xml");
		LOGGER.info("createDomainWithReturnMessageTest completed");
    	} catch (Exception e) {
			e.printStackTrace();
	   }
    }
    
    /**
	 * Creates the group grant element with the given group name and permission level
	 * in domains.xml for SuperDomain
	 */
    @Tas(testBeds = @TestBed(name = CommonsLinuxTestbed.class, executeOn = CommonsLinuxTestbed.EM_MACHINE_ID), size = SizeType.COLOSSAL, owner = "balra06")
    @Test
    public void createGroupGrantForSuperDomainTest() { 
    	try {
    		ApmbaseUtil.fileBackUp(emConfigDir+"/domains.xml");
    		String msg = XMLUtil.createGroupGrantForSuperDomain(emConfigDir+"/domains.xml","grantgroup1", "full");    	
    		Assert.assertTrue(XMLUtil.SUCCESS_MESSAGE.equals(msg));
    		ApmbaseUtil.revertFile(emConfigDir+"/domains.xml");
    		LOGGER.info("createGroupGrantForSuperDomainTest completed");
    	} catch (Exception e) {
			e.printStackTrace();
	   }
    }
    
    /**
	 * Creates the group grant element with the given group name and permission level
	 * for the corresponding element 
	 */
    @Tas(testBeds = @TestBed(name = CommonsLinuxTestbed.class, executeOn = CommonsLinuxTestbed.EM_MACHINE_ID), size = SizeType.COLOSSAL, owner = "balra06")
    @Test
    public void createGroupGrantForElementTest() { 
    	try {
    		ApmbaseUtil.fileBackUp(emConfigDir+"/domains.xml");
    		ApmbaseUtil.fileBackUp(emConfigDir+"/server.xml");
    		Map<String, String> usersMap = new HashMap<String, String>();
    		Map<String, String> groupsMap = new HashMap<String, String>();
    		groupsMap.put("Admin", "full");
    		usersMap.put("Guest", "read");
    				XMLUtil.createDomain(emConfigDir+"/domains.xml","TestDomain",
				"custom domain to test createDomain", "(.*)", usersMap,groupsMap);
    		String msg1 = XMLUtil.createGroupGrantForElement(emConfigDir+"/domains.xml","domain", "grantgroup1", "full");
    		String msg2 = XMLUtil.createGroupGrantForElement(emConfigDir+"/server.xml","server", "grantgroup2", "full");    	
		    Assert.assertTrue(XMLUtil.SUCCESS_MESSAGE.equals(msg1)&& XMLUtil.SUCCESS_MESSAGE.equals(msg2));
		    ApmbaseUtil.revertFile(emConfigDir+"/domains.xml");
		    ApmbaseUtil.revertFile(emConfigDir+"/server.xml");
		    LOGGER.info("createGroupGrantForElementTest completed");
    	} catch (Exception e) {
			e.printStackTrace();
	   }
    	 
    }
    
    /**
	 * Creates the group grant element with the given group name and permission level
	 * for the SuperDomain 
	 */   
    @Tas(testBeds = @TestBed(name = CommonsLinuxTestbed.class, executeOn = CommonsLinuxTestbed.EM_MACHINE_ID), size = SizeType.COLOSSAL, owner = "balra06")
    @Test
    public void createUserGrantElementTest() { 
    	try {
    		ApmbaseUtil.fileBackUp(emConfigDir+"/domains.xml");
    		String msg = XMLUtil.createUserGrantElement(emConfigDir+"/domains.xml","grantuser1", "full");   
    		Assert.assertTrue(XMLUtil.SUCCESS_MESSAGE.equals(msg));
    		ApmbaseUtil.revertFile(emConfigDir+"/domains.xml");
    		LOGGER.info("createUserGrantElementTest completed");
    	} catch (Exception e) {
			e.printStackTrace();
	   }
    }
    
    /**
	 * Creates the group grant element with the given group name and permission level
	 * for the CustomDomain 
	 */ 
    @Tas(testBeds = @TestBed(name = CommonsLinuxTestbed.class, executeOn = CommonsLinuxTestbed.EM_MACHINE_ID), size = SizeType.COLOSSAL, owner = "balra06")
    @Test
    public void createUserGrantElementForCustomDomainTest() { 
    	try {
    		ApmbaseUtil.fileBackUp(emConfigDir+"/domains.xml");	
    		String msg = XMLUtil.createUserGrantElementForCustomDomain(emConfigDir+"/domains.xml","domain", "grantuser2", "full");
    		Assert.assertTrue(msg.equals(XMLUtil.SUCCESS_MESSAGE));
    		ApmbaseUtil.revertFile(emConfigDir+"/domains.xml");
    		LOGGER.info("createUserGrantElementForCustomDomainTest completed");
    	} catch (Exception e) {
			e.printStackTrace();
	   }
    }
    
    /**
	 * Creates a user with the given username and password under <users> tag in users.xml
	 *  
	 */ 
    @Tas(testBeds = @TestBed(name = CommonsLinuxTestbed.class, executeOn = CommonsLinuxTestbed.EM_MACHINE_ID), size = SizeType.COLOSSAL, owner = "balra06")
    @Test
    public void createUserInUsersXMLTest() { 
    	try {
    		ApmbaseUtil.fileBackUp(emConfigDir+"/users.xml");		
    		String msg = XMLUtil.createUserInUsersXML(emConfigDir+"/users.xml","user1", "user1");
    		Assert.assertTrue(XMLUtil.SUCCESS_MESSAGE.equals(msg));
    		ApmbaseUtil.revertFile(emConfigDir+"/users.xml");
    		LOGGER.info("createUserInUsersXMLTest completed");
    	} catch (Exception e) {
			e.printStackTrace();
	   }
    }
    
    /**
	 * Creates a user with the given username for the Admin group in users.xml
	 *  
	 */ 
    @Tas(testBeds = @TestBed(name = CommonsLinuxTestbed.class, executeOn = CommonsLinuxTestbed.EM_MACHINE_ID), size = SizeType.COLOSSAL, owner = "balra06")
    @Test
    public void addUserToAdminGroupTest() { 
    	try {
    		ApmbaseUtil.fileBackUp(emConfigDir+"/users.xml");	
    		String msg = XMLUtil.addUserToAdminGroup(emConfigDir+"/users.xml","adminUser1");
    		Assert.assertTrue(msg.equals(XMLUtil.SUCCESS_MESSAGE));
    		ApmbaseUtil.revertFile(emConfigDir+"/users.xml");
    		LOGGER.info("addUserToAdminGroupTest completed");
    	} catch (Exception e) {
			e.printStackTrace();
	   }
    }
    
    /**
	 * Creates a user with the given username for the CEM System Administrator group in users.xml
	 *  
	 */ 
    @Tas(testBeds = @TestBed(name = CommonsLinuxTestbed.class, executeOn = CommonsLinuxTestbed.EM_MACHINE_ID), size = SizeType.COLOSSAL, owner = "balra06")
    @Test
    public void addUserToCEMAdminGroupTest() { 
       	try {
    		ApmbaseUtil.fileBackUp(emConfigDir+"/users.xml");	
    		String msg = XMLUtil.addUserToCEMAdminGroup(emConfigDir+"/users.xml","cemadminUser1");
    		Assert.assertTrue(msg.equals(XMLUtil.SUCCESS_MESSAGE));
    		ApmbaseUtil.revertFile(emConfigDir+"/users.xml");
    		LOGGER.info("addUserToCEMAdminGroupTest completed");
    	} catch (Exception e) {
			e.printStackTrace();
	   }
    }
    
    /**
	 * Creates a group with the given group name and description 
	 * and adds single user to that group in users.xml
	 *  
	 */ 
    @Tas(testBeds = @TestBed(name = CommonsLinuxTestbed.class, executeOn = CommonsLinuxTestbed.EM_MACHINE_ID), size = SizeType.COLOSSAL, owner = "balra06")
    @Test
    public void createGroupAddSingleUserInUsersXMLTest() { 
       	try {
    		ApmbaseUtil.fileBackUp(emConfigDir+"/users.xml");
    		String msg = XMLUtil.createGroupAddSingleUserInUsersXML(emConfigDir+"/users.xml","SingleUser Group","SingleUser", "user1");    
    		Assert.assertTrue(msg.equals(XMLUtil.SUCCESS_MESSAGE));
    		ApmbaseUtil.revertFile(emConfigDir+"/users.xml");
    		LOGGER.info("createGroupAddSingleUserInUsersXMLTest completed");
    	} catch (Exception e) {
			e.printStackTrace();
	   }
    }
    
    /**
	 * Creates a group with the given group name and description 
	 * and adds two users to that group in users.xml
	 *  
	 */ 
    @Tas(testBeds = @TestBed(name = CommonsLinuxTestbed.class, executeOn = CommonsLinuxTestbed.EM_MACHINE_ID), size = SizeType.COLOSSAL, owner = "balra06")
    @Test   
    public void createGroupAddTwoUsersInUsersXMLTest() { 
    	try {
    		ApmbaseUtil.fileBackUp(emConfigDir+"/users.xml");
    		String msg = XMLUtil.createGroupAddTwoUsersInUsersXML(emConfigDir+"/users.xml","TwoUsers group", "TwoUsers","user1", "user2");
    		Assert.assertTrue(msg.equals(XMLUtil.SUCCESS_MESSAGE));
    		ApmbaseUtil.revertFile(emConfigDir+"/users.xml");
    		LOGGER.info("createGroupAddTwoUsersInUsersXMLTest completed");
    	} catch (Exception e) {
			e.printStackTrace();
	   }
    }
    
    
    /**
	 * Creates a group with the given group name and description 
	 * and adds multiple users to that group in users.xml
	 *  
	 */ 
    @Tas(testBeds = @TestBed(name = CommonsLinuxTestbed.class, executeOn = CommonsLinuxTestbed.EM_MACHINE_ID), size = SizeType.COLOSSAL, owner = "balra06")
    @Test   
    public void createGroupAddMultipleUsersInUsersXMLTest() { 
    	try {
    		ApmbaseUtil.fileBackUp(emConfigDir+"/users.xml");    
    		String msg = XMLUtil.createGroupAddMultipleUsersInUsersXML(emConfigDir+"/users.xml","MultipleUsers group", "Multiple","user1,user2,user3,user4");
    		Assert.assertTrue(msg.equals(XMLUtil.SUCCESS_MESSAGE));
    		ApmbaseUtil.revertFile(emConfigDir+"/users.xml");
    		LOGGER.info("createGroupAddMultipleUsersInUsersXMLTest completed");
    	} catch (Exception e) {
			e.printStackTrace();
	   }
    }
    
    @Tas(testBeds = @TestBed(name = CommonsLinuxTestbed.class, executeOn = CommonsLinuxTestbed.EM_MACHINE_ID), size = SizeType.COLOSSAL, owner = "balra06")
    @Test
    public void deleteUserFromAdminGroupTest() { 
    String msg = XMLUtil.deleteUserFromAdminGroup(emConfigDir+"/users.xml","adminUser1");
    Assert.assertTrue(msg.equals(XMLUtil.SUCCESS_MESSAGE));
    }
    

    /* Adds an entry  in the loadbalancing.xml file for the agent colletctor 
     * with latched property set and also changes the affinity 
     */    
    @Tas(testBeds = @TestBed(name = CommonsWindowsTestbed.class, executeOn = CommonsWindowsTestbed.EM_MACHINE_ID), size = SizeType.COLOSSAL, owner = "balra06")
    @Test
    public void ModifyloadbalancingXMLTest() throws Exception { 
    	String col1host = "C1";
    	String col2host = "C2";
    	String c2Port = "5002";
    	String c1Port = "5001";  
    	ApmbaseUtil.fileBackUp(emConfigDir+"/loadbalancing.xml");
    	int success = x.addlatchedEntryInLoadBalXML(emConfigDir+"/loadbalancing.xml","Test-affinity",".*\\|.*\\|.*91.*",col1host+":"+c1Port,col2host+":"+c2Port,col1host+":"+"true"); 
		Assert.assertEquals(success, 1);		
    	Boolean done = x.changelatchedEntryInLoadBalXML(emConfigDir+"/loadbalancing.xml","Test-affinity",col2host+":"+c2Port,col1host+":"+c1Port);    	
    	Assert.assertEquals(done, Boolean.TRUE);    	    
		ApmbaseUtil.revertFile(emConfigDir+"/loadbalancing.xml");
    }
}
