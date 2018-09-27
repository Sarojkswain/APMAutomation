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
 */
package com.ca.apm.tests.utils;

import java.util.HashMap;

import com.ca.apm.automation.action.flow.FlowConfig.FlowConfigBuilder;
import com.ca.apm.tests.flow.ChangePropertiesFlow;
import com.ca.apm.tests.flow.ChangePropertiesFlowContext;
import com.ca.apm.tests.flow.CheckLogKeywordFlow;
import com.ca.apm.tests.flow.CheckLogKeywordFlowContext;
import com.ca.apm.tests.flow.StartEmFlow;
import com.ca.apm.tests.flow.StartEmFlowContext;
import com.ca.apm.tests.flow.StartTomcatFlow;
import com.ca.apm.tests.flow.StartTomcatFlowContext;
import com.ca.apm.tests.flow.StopEmFlow;
import com.ca.apm.tests.flow.StopEmFlowContext;
import com.ca.apm.tests.flow.StopTomcatFlow;
import com.ca.apm.tests.flow.StopTomcatFlowContext;
import com.ca.apm.tests.flow.SynchronizeTimeFlow;
import com.ca.apm.tests.flow.SynchronizeTimeFlowContext;
import com.ca.tas.client.AutomationAgentClientFactory;
import com.ca.tas.client.IAutomationAgentClient;
import com.ca.tas.envproperty.EnvironmentPropertyContext;
import com.ca.tas.envproperty.MachineEnvironmentProperties;
import com.ca.tas.role.EmRole;

/**
 * Methods to invoke Java Flows on remote servers
 * 
 * @author turyu01
 *
 */
public class FlowUtils {

    /**
     * Starts EM on the remote machine in the testbed
     */
    public static void startEm(EnvironmentPropertyContext envProps, String machineId,
        String emRoleId) throws Exception {
        final IAutomationAgentClient aaClient = new AutomationAgentClientFactory(envProps).create();

        final String installDir =
            envProps.getRolePropertyById(emRoleId, EmRole.ENV_PROPERTY_INSTALL_DIR);
        final StartEmFlowContext ctx = new StartEmFlowContext.Builder(installDir).build();

        final String hostnameWithPort =
            envProps.getMachinePropertyById(machineId,
                MachineEnvironmentProperties.HOSTNAME_WITH_PORT);

        aaClient.runJavaFlow(new FlowConfigBuilder(StartEmFlow.class, ctx, hostnameWithPort));
    }

    /**
     * Stop EM on the remote machine in the testbed
     */
    public static void stopEm(EnvironmentPropertyContext envProps, String machineId, String emRoleId)
        throws Exception {
        final IAutomationAgentClient aaClient = new AutomationAgentClientFactory(envProps).create();

        final String installDir =
            envProps.getRolePropertyById(emRoleId, EmRole.ENV_PROPERTY_INSTALL_DIR);
        final StopEmFlowContext ctx = new StopEmFlowContext.Builder(installDir).build();

        final String hostnameWithPort =
            envProps.getMachinePropertyById(machineId,
                MachineEnvironmentProperties.HOSTNAME_WITH_PORT);

        aaClient.runJavaFlow(new FlowConfigBuilder(StopEmFlow.class, ctx, hostnameWithPort));
    }

    /**
     * Starts Tomcat on the remote machine in the testbed
     */
    public static void startTomcat(EnvironmentPropertyContext envProps, String machineId,
        String tomcatDir) throws Exception {
        final IAutomationAgentClient aaClient = new AutomationAgentClientFactory(envProps).create();

        final StartTomcatFlowContext cmdFlowContext =
            new StartTomcatFlowContext.Builder(tomcatDir).build();

        final String hostnameWithPort =
            envProps.getMachinePropertyById(machineId,
                MachineEnvironmentProperties.HOSTNAME_WITH_PORT);

        aaClient.runJavaFlow(new FlowConfigBuilder(StartTomcatFlow.class, cmdFlowContext,
            hostnameWithPort));
    }

    /**
     * Stops Tomcat on the remote machine in the testbed
     */
    public static void stopTomcat(EnvironmentPropertyContext envProps, String machineId,
        String tomcatDir) throws Exception {
        final IAutomationAgentClient aaClient = new AutomationAgentClientFactory(envProps).create();

        final String hostnameWithPort =
            envProps.getMachinePropertyById(machineId,
                MachineEnvironmentProperties.HOSTNAME_WITH_PORT);

        final StopTomcatFlowContext ctxStop = new StopTomcatFlowContext.Builder(tomcatDir).build();
        aaClient
            .runJavaFlow(new FlowConfigBuilder(StopTomcatFlow.class, ctxStop, hostnameWithPort));
    }


    /**
     * Sets EM's properties to use secure communication channel
     */
    public static void setUpEMHttpsProperties(EnvironmentPropertyContext envProps,
        String machineId, String propertyFilePath) throws Exception {
        insertProperty(envProps, machineId, propertyFilePath,
            "introscope.enterprisemanager.enabled.channels", "channel1,channel2");
        insertProperty(envProps, machineId, propertyFilePath,
            "introscope.enterprisemanager.webserver.jetty.configurationFile", "em-jetty-config.xml");
    }

    /**
     * Sets Agent's properties to use secure communication
     */
    public static void setUpAgentHttpsProperties(EnvironmentPropertyContext envProps,
        String machineId, String propertyFilePath) throws Exception {
        insertProperty(envProps, machineId, propertyFilePath,
            "introscope.agent.enterprisemanager.transport.tcp.socketfactory.DEFAULT",
            "com.wily.isengard.postofficehub.link.net.HttpsTunnelingSocketFactory");
        insertProperty(envProps, machineId, propertyFilePath,
            "introscope.agent.enterprisemanager.transport.tcp.port.DEFAULT", "8444");
    }

    /**
     * Updates property on the remote machine
     * 
     * @param propertyFilePath - file path to the .property file
     * @param propertyKey - existing key in the .property file
     * @param propertyValue - new value for the key
     */
    public static void updateProperty(EnvironmentPropertyContext envProps, String machineId,
        String propertyFilePath, String propertyKey, String propertyValue) throws Exception {
        insertProperty(envProps, machineId, propertyFilePath, propertyKey, propertyValue, false);
    }


    /**
     * Insert property on the remote machine
     * 
     * @param propertyFilePath - file path to the .property file
     * @param propertyKey - existing key in the .property file
     * @param propertyValue - new value for the key
     */
    public static void insertProperty(EnvironmentPropertyContext envProps, String machineId,
        String propertyFilePath, String propertyKey, String propertyValue) throws Exception {
        insertProperty(envProps, machineId, propertyFilePath, propertyKey, propertyValue, true);
    }

    private static void insertProperty(EnvironmentPropertyContext envProps, String machineId,
        String propertyFilePath, String propertyKey, String propertyValue, boolean addNotExist)
        throws Exception {
        final IAutomationAgentClient aaClient = new AutomationAgentClientFactory(envProps).create();

        final String hostnameWithPort =
            envProps.getMachinePropertyById(machineId,
                MachineEnvironmentProperties.HOSTNAME_WITH_PORT);

        final HashMap<String, String> props = new HashMap<>();
        props.put(propertyKey, propertyValue);

        final ChangePropertiesFlowContext changePropertiesContext =
            new ChangePropertiesFlowContext(props, propertyFilePath);

        changePropertiesContext.setAddNotExisting(addNotExist);

        aaClient.runJavaFlow(new FlowConfigBuilder(ChangePropertiesFlow.class,
            changePropertiesContext, hostnameWithPort));
    }



    /**
     * Check if is keyWord in log at logPath. Otherwise it throw exception
     */
    public static void isKeywordInLog(EnvironmentPropertyContext envProps, String machineId,
        String logPath, String keyWord) throws Exception {
        final IAutomationAgentClient aaClient = new AutomationAgentClientFactory(envProps).create();
        CheckLogKeywordFlowContext connectionLogCheckContext =
            new CheckLogKeywordFlowContext(logPath, keyWord);

        final String hostnameWithPort =
            envProps.getMachinePropertyById(machineId,
                MachineEnvironmentProperties.HOSTNAME_WITH_PORT);

        aaClient.runJavaFlow(new FlowConfigBuilder(CheckLogKeywordFlow.class,
            connectionLogCheckContext, hostnameWithPort));
    }

    /**
     * Synchronizes time on the remote machine with CA NTP
     */
    public static void synchronizeTime(EnvironmentPropertyContext envProps, String machineId)
        throws Exception {
        final IAutomationAgentClient aaClient = new AutomationAgentClientFactory(envProps).create();

        final String hostnameWithPort =
            envProps.getMachinePropertyById(machineId,
                MachineEnvironmentProperties.HOSTNAME_WITH_PORT);

        final SynchronizeTimeFlowContext ctx = new SynchronizeTimeFlowContext();

        aaClient
            .runJavaFlow(new FlowConfigBuilder(SynchronizeTimeFlow.class, ctx, hostnameWithPort));
    }


}
