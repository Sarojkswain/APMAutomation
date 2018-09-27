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
 */


package com.ca.apm.tests.testbed.acc;

import java.util.HashMap;
import java.util.Map;

import com.ca.apm.automation.action.flow.utility.ConfigureFlow;
import com.ca.apm.automation.action.flow.utility.ConfigureFlowContext;
import com.ca.apm.tests.role.EnableBrowserAgentRole;
import com.ca.tas.builder.TasBuilder;
import com.ca.tas.resolver.ITasResolver;
import com.ca.tas.role.utility.ExecutionRole;
import com.ca.tas.role.utility.GenericRole;
import com.ca.tas.role.EmRole;
import com.ca.tas.role.IRole;
import com.ca.tas.role.acc.AccControllerRole;
import com.ca.tas.role.acc.AccServerRole;
import com.ca.tas.testbed.ITestbed;
import com.ca.tas.testbed.ITestbedFactory;
import com.ca.tas.testbed.ITestbedMachine;
import com.ca.tas.testbed.TestBedUtils;
import com.ca.tas.testbed.Testbed;
import com.ca.tas.tests.annotations.TestBedDefinition;

import org.eclipse.aether.artifact.DefaultArtifact;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.ca.tas.testbed.ITestbedMachine.TEMPLATE_W64;


/**
 * ACC Windows Test bed
 * This script can be used to deploy ACC, EM & default agent on a single machine for manual test
 * effort
 * Some configurations will have to be done manually
 * 
 * @author pojja01@ca.com, gupra04
 */

@TestBedDefinition
public class Acc10_2DeployWindowsTestBed implements ITestbedFactory {

    private static final Logger LOGGER = LoggerFactory.getLogger(EnableBrowserAgentRole.class);

    public static final String ACC_MACHINE_ID = "accMachine";
    public static final String ACC_SERVER_ROLE_ID = "accServerRole";
    public static final String ACC_CONTROLLER_ROLE_ID = "accControllerRole";
    private static final String ACC_MACHINE_TEMPLATE_ID = TEMPLATE_W64;
    private static final String ACC_VERSION = "10.2.0-SNAPSHOT";

    public static final String EM_ROLE_ID = "emRole";

    public static final String DEFAULT_AGENT_ROLE_ID = "default_agent";
    protected static final String DEPLOY_BASE = TasBuilder.WIN_SOFTWARE_LOC;

    public static final String CONFIGURE_ACC_ROLE_ID = "acc_configuration";
    
    @Override
    public ITestbed create(ITasResolver tasResolver) {

        ITestbedMachine accMachine = initWindowsMachine(tasResolver);
        // accMachine.addRemoteResource("C:\\automation\\deployed\\APMCommandCenterServer\\logs");
        // accMachine.addRemoteResource("C:\\automation\\deployed\\APMCommandCenterServer\\log\\apmccsrv.log");

        return new Testbed(Acc10_2DeployWindowsTestBed.class.getSimpleName())
            .addMachine(accMachine);
    }

    private ITestbedMachine initWindowsMachine(ITasResolver tasResolver) {

        AccServerRole accServer =
            new AccServerRole.Builder(ACC_SERVER_ROLE_ID, tasResolver).version(ACC_VERSION).build();
        IRole accClient =
            new AccControllerRole.Builder(ACC_CONTROLLER_ROLE_ID, tasResolver).server(accServer)
                .version(ACC_VERSION).build();
        accClient.after(accServer);

        EmRole emRole =
            new EmRole.Builder(EM_ROLE_ID, tasResolver)
                .configProperty("introscope.enterprisemanager.hotconfig.enable", "false")
                .configProperty("introscope.enterprisemanager.performance.compressed", "false")
                .configProperty("log4j.logger.Manager.Performance", "DEBUG, performance, logfile")
                .build();
        emRole.addProperty("emPassword", "");

        GenericRole agentRole = addDefaultAgentRole(tasResolver);

        ExecutionRole configureAccRole = configureAccServer(tasResolver);

        configureAccRole.after(accServer);

        return TestBedUtils.createWindowsMachine(ACC_MACHINE_ID, ACC_MACHINE_TEMPLATE_ID,
            accServer, accClient, emRole, agentRole, configureAccRole);
    }

    protected GenericRole addDefaultAgentRole(ITasResolver tasResolver) {
        String delivery = "com.ca.apm.delivery";
        String artifact = "agent-noinstaller-default-windows";

        LOGGER.info("Deploying Default Agent Role");
        GenericRole defaultAgentRole =
            new GenericRole.Builder(DEFAULT_AGENT_ROLE_ID, tasResolver).unpack(
                new DefaultArtifact(delivery, artifact, "zip", tasResolver.getDefaultVersion()),
                DEPLOY_BASE + "default").build();
        LOGGER.info("END ---- Deploying Default Agent Role");
        return defaultAgentRole;
    }

    protected ExecutionRole configureAccServer(ITasResolver tasResolver) {

        String accServerPropFilePath =
            DEPLOY_BASE + "APMCommandCenterServer" + TasBuilder.WIN_SEPARATOR + "config"
                + TasBuilder.WIN_SEPARATOR + "apmccsrv.properties";
        LOGGER.info("accServerPropFilePath value is: " + accServerPropFilePath);
        Map<String, String> accConfigurationProps = new HashMap<String, String>();
        accConfigurationProps.put("wrapper.java.additional.3", "-javaagent:" + DEPLOY_BASE
            + "default\\wily\\Agent.jar");
        accConfigurationProps.put("wrapper.java.additional.4",
            "-Dcom.wily.introscope.agentProfile=" + DEPLOY_BASE
                + "default\\wily\\core\\config\\IntroscopeAgent.profile");

        ConfigureFlowContext updateConfigFile =
            new ConfigureFlowContext.Builder().configurationMap(accServerPropFilePath,
                accConfigurationProps).build();

        ExecutionRole configAccServerRole =
            new ExecutionRole.Builder(CONFIGURE_ACC_ROLE_ID).flow(ConfigureFlow.class,
                updateConfigFile).build();

        return configAccServerRole;
    }

}
