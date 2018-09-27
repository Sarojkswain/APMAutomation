package com.ca.apm.tests.em.properties;

import static com.ca.tas.testbed.ITestbedMachine.TEMPLATE_CO65;
import static com.ca.tas.testbed.ITestbedMachine.TEMPLATE_CO66;
import static com.ca.tas.testbed.ITestbedMachine.TEMPLATE_RH66;
import static com.ca.tas.testbed.ITestbedMachine.TEMPLATE_W64;

import com.ca.apm.commons.tests.AgentControllabilityCommons;
import com.ca.tas.builder.TasBuilder;

/**
 * Created by jamsa07 on 6/1/2016.
 */
public class ConfigurableConfigDirectoryConstants extends AgentControllabilityCommons {

    public static final String CO66_TEMPLATE_ID = TEMPLATE_CO66;
    public static final String CO65_TEMPLATE_ID = TEMPLATE_CO65;
    public static final String RH66_TEMPLATE_ID = TEMPLATE_RH66;
    public static final String WINDOWS_TEMPLATE_ID = TEMPLATE_W64;

    public static final String EM_MACHINE_ID = "emMachine";   

    public static final String EM1_ROLE_ID = "em1Role";
    public static final String EM2_ROLE_ID = "em2Role";
   
    public static final String TOMCAT_MACHINE_ID = "tomcatMachine";    

    public static final String TOMCAT_ROLE_ID = "tomcatRole";   
    public static final String TOMCAT_AGENT_ROLE_ID = "tomcatAgentRole";    
    public static final String QA_APP_TOMCAT_ROLE_ID = "qaAppTomcatRole";   
    
    public static final String EM1_INSTALL_WIN_DIR = TasBuilder.WIN_SOFTWARE_LOC + "em1";
    public static final String EM2_INSTALL_WIN_DIR = TasBuilder.WIN_SOFTWARE_LOC + "em2";
    
    public static final String EM1_INSTALL_LINUX_DIR = TasBuilder.LINUX_SOFTWARE_LOC + "em1";
    public static final String EM2_INSTALL_LINUX_DIR = TasBuilder.LINUX_SOFTWARE_LOC + "em2";

}
