package com.ca.apm.tests.agentcontrollability;

import static com.ca.tas.testbed.ITestbedMachine.TEMPLATE_CO65;
import static com.ca.tas.testbed.ITestbedMachine.TEMPLATE_CO66;
import static com.ca.tas.testbed.ITestbedMachine.TEMPLATE_RH66;
import static com.ca.tas.testbed.ITestbedMachine.TEMPLATE_W64;

import com.ca.apm.commons.tests.AgentControllabilityCommons;

/**
 * Created by jamsa07 on 6/1/2016.
 */
public class AgentControllabilityConstants extends AgentControllabilityCommons {

    public static final String CO66_TEMPLATE_ID = TEMPLATE_CO66;
    public static final String CO65_TEMPLATE_ID = TEMPLATE_CO65;
    public static final String RH66_TEMPLATE_ID = TEMPLATE_RH66;
    public static final String WINDOWS_TEMPLATE_ID = TEMPLATE_W64;

    public static final String EM_MACHINE_ID = "emMachine";
    public static final String MOM_MACHINE_ID = "momMachine";
    public static final String COLLECTOR1_MACHINE_ID = "collector1Machine";
    public static final String COLLECTOR2_MACHINE_ID = "collector2Machine";
    public static final String COLLECTOR3_MACHINE_ID = "collector3Machine";

    public static final String EM_ROLE_ID = "emRole";
    public static final String MOM_ROLE_ID = "momRole";
    public static final String COLLECTOR1_ROLE_ID = "collector1Role";
    public static final String COLLECTOR2_ROLE_ID = "collector2Role";
    public static final String COLLECTOR3_ROLE_ID = "collector3Role";

    public static final String TOMCAT_MACHINE_ID = "tomcatMachine";
    public static final String TOMCAT_MACHINE_ID1 = "tomcat1Machine";
    public static final String TOMCAT_MACHINE_ID2 = "tomcat2Machine";
    public static final String TOMCAT_MACHINE_ID3 = "tomcat3Machine";

    public static final String TOMCAT_ROLE_ID = "tomcatRole";
    public static final String TOMCAT_ROLE1_ID = "tomcat1Role";
    public static final String TOMCAT_ROLE2_ID = "tomcat2Role";
    public static final String TOMCAT_ROLE3_ID = "tomcat3Role";
    public static final String TOMCAT_AGENT_ROLE_ID = "tomcatAgentRole";
    public static final String TOMCAT_AGENT1_ROLE_ID = "tomcatAgent1Role";
    public static final String TOMCAT_AGENT2_ROLE_ID = "tomcatAgent2Role";
    public static final String TOMCAT_AGENT3_ROLE_ID = "tomcatAgent3Role";
    public static final String QA_APP_TOMCAT_ROLE1_ID = "qaAppTomcatRole1";
    public static final String QA_APP_TOMCAT_ROLE2_ID = "qaAppTomcatRole2";
    public static final String QA_APP_TOMCAT_ROLE3_ID = "qaAppTomcatRole3";
    public static final String QA_APP_TOMCAT_ROLE_ID = "qaAppTomcatRole";
    public static final String APACHE_ROLE_ID = "apacheRole";
    public static final String APACHE_MACHINE_ID = "apacheMachine";
 
    public static final String JBOSS_ROLE_ID = "jbossRole";
    public static final String JBOSS_AGENT_ROLE_ID = "jbossAgentRole";
    public static final String QA_APP_JBOSS_ROLE_ID = "qaAppJbossRole";

   


    public static final String clusterEM1Host="introscope.enterprisemanager.clustering.login.em1.host=";
    public static final String clusterEM2Host="introscope.enterprisemanager.clustering.login.em2.host=";
    public static final String clusterEM3Host="introscope.enterprisemanager.clustering.login.em3.host=";

    public static final String clusterEM1Port= "introscope.enterprisemanager.clustering.login.em1.port=";
    public static final String clusterEM2Port= "introscope.enterprisemanager.clustering.login.em2.port=";
    public static final String clusterEM3Port= "introscope.enterprisemanager.clustering.login.em3.port=";

    public static final String clusterEM1PublicKey="introscope.enterprisemanager.clustering.login.em1.publickey=config/internal/server/EM.public";
    public static final String clusterEM2PublicKey="introscope.enterprisemanager.clustering.login.em2.publickey=config/internal/server/EM.public";
    public static final String clusterEM3PublicKey="introscope.enterprisemanager.clustering.login.em3.publickey=config/internal/server/EM.public";

    public static final String defaultEMAgentAllowedProp="introscope.apm.agentcontrol.agent.allowed=true";
    public static final String defaultEMlistLookup="introscope.apm.agentcontrol.agent.emlistlookup.enable=true";
    public static final String defaultEMdisallowedConnLimit="introscope.enterprisemanager.agent.disallowed.connection.limit=0";

    public static final String defaultEMAgentAllowedPropFalse="introscope.apm.agentcontrol.agent.allowed=false";


}
