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
 * Author : KETSW01
 */
package com.ca.apm.tests.agentcontrollability;

import static com.ca.tas.testbed.ITestbedMachine.TEMPLATE_CO66;
import static com.ca.tas.testbed.ITestbedMachine.TEMPLATE_W64;

import com.ca.apm.commons.tests.BaseAgentTest;


public class AccConstants extends BaseAgentTest {

    public static final String MOM_MACHINE_ID = "momMachine";
    public static final String COLLECTOR1_MACHINE_ID = "collector1Machine";
    public static final String COLLECTOR2_MACHINE_ID = "collector2Machine";
    public static final String COLLECTOR3_MACHINE_ID = "collector3Machine";
    public static final String STANDALONE_MACHINE_ID = "standaloneMachine";

    public static final String MOM_ROLE_ID = "momRole";
    public static final String COLLECTOR1_ROLE_ID = "collector1Role";
    public static final String COLLECTOR2_ROLE_ID = "collector2Role";
    public static final String COLLECTOR3_ROLE_ID = "collector3Role";
    public static final String STANDALONE_ROLE_ID = "standaloneRole";

    public static String EM_TEMPLATE_ID_WIN = TEMPLATE_W64;
    public static String EM_TEMPLATE_ID_LINUX = TEMPLATE_CO66;

    public static final String AGENT_MACHINE_ID = "agentMachine";
    public static final String AGENT_MACHINE_TEMPLATE_ID_WIN = TEMPLATE_W64;    
    public static final String AGENT_MACHINE_TEMPLATE_ID_LINUX = TEMPLATE_CO66;
    
    public static final String TOMCAT_ROLE_ID = "tomcatRole";
    public static final String TOMCAT_AGENT_ROLE_ID = "tomcatAgentRole";
    public static final String QA_APP_TOMCAT_ROLE_ID = "qaAppTomcatRole";

    public static final String JBOSS_ROLE_ID = "JBossRole";
    public static final String JBOSS_AGENT_ROLE_ID = "JbossAgentRole";
    public static final String QA_APP_JBOSS_ROLE_ID = "qaAppJBossRole";
    
    public static final String TOMCAT1_ROLE_ID = "tomcat1Role";
    public static final String TOMCAT1_AGENT_ROLE_ID = "tomcat1AgentRole";
    public static final String QA_APP_TOMCAT1_ROLE_ID = "qaAppTomcat1Role";
    public static final String TOMCAT1_AGENT_NAME = "Tomcat1 Agent";

    public static final String JBOSS1_ROLE_ID = "JBoss1Role";
    public static final String JBOSS1_AGENT_ROLE_ID = "Jboss1AgentRole";
    public static final String QA_APP_JBOSS1_ROLE_ID = "qaAppJBoss1Role";
    public static final String JBOSS1_AGENT_NAME = "JBoss1 Agent";

}
