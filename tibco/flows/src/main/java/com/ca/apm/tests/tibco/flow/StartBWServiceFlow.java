/*
 * Copyright (c) 2015 CA. All rights reserved.
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

package com.ca.apm.tests.tibco.flow;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Properties;

import org.apache.commons.lang.SystemUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ca.apm.automation.action.core.IAutomationFlow;
import com.ca.apm.automation.action.core.annotations.Flow;
import com.ca.apm.automation.action.core.annotations.FlowContext;
import com.ca.apm.automation.action.flow.FlowBase;
import com.ca.apm.automation.action.flow.commandline.RunCommandFlow;
import com.ca.apm.automation.action.flow.commandline.RunCommandFlowContext;


/**
 * Start the BW and Hawk Services
 * 
 * @author Vashistha Singh (sinva01@ca.com)
 *
 */
@Flow
public class StartBWServiceFlow extends FlowBase implements TibcoConstants {
    private static final Logger LOGGER = LoggerFactory.getLogger(StartBWServiceFlow.class);
    @FlowContext
    DeployTibcoFlowContext context;

    @Override
    public void run() throws Exception {
        // Starting of the Tibco BW service is only applicable if the BWAdmin is installed
        if (context.getRoleId().equals(TIBCO_ADMIN_ROLE_ID) && SystemUtils.IS_OS_WINDOWS) {
            // start the RV service
            startService("rvd");
            Thread.sleep(2000);
            String doaminDir =
                context.getInstallDir() + PATH_SEPARATOR + "administrator" + PATH_SEPARATOR
                    + "domain" + PATH_SEPARATOR + context.getDomainName();
            String adminTraFileName =
                doaminDir + PATH_SEPARATOR + "bin" + PATH_SEPARATOR + "tibcoadmin_"
                    + context.getDomainName() + ".tra";

            // Start Tibco Hawk Service

            String tradoaminDir =
                context.getInstallDir() + PATH_SEPARATOR + "tra" + PATH_SEPARATOR + "domain"
                    + PATH_SEPARATOR + context.getDomainName();
            String hawkTraFileName =
                tradoaminDir + PATH_SEPARATOR + "hawkagent_" + context.getDomainName() + ".tra";
            Properties hawkproperties = new Properties();
            hawkproperties.load(new BufferedReader(new FileReader(new File(hawkTraFileName))));

            String HawkServiceName = hawkproperties.getProperty("ntservice.name");
            startService(HawkServiceName);

            // Start Tibco BWAdminstrator Service
            Properties properties = new Properties();
            properties.load(new BufferedReader(new FileReader(new File(adminTraFileName))));

            String BWServiceName = properties.getProperty("ntservice.name");
            startService(BWServiceName);

        }
    }

    private void startService(String serviceName) {
        try {
            RunCommandFlow commandFlow = new RunCommandFlow();
            RunCommandFlowContext command =
                new RunCommandFlowContext.Builder("sc.exe").args(
                    Arrays.asList("start", serviceName)).build();
            Field commandContextField = getFlowContextField(commandFlow);
            commandContextField.setAccessible(true);
            commandContextField.set(commandFlow, command);
            commandFlow.run();
        } catch (Exception e) {
            LOGGER.error("Could not Start the service " + serviceName);
        }
    }

    private Field getFlowContextField(Object obj) {
        if (obj instanceof IAutomationFlow) {
            for (Field f : obj.getClass().getDeclaredFields()) {
                for (Annotation t : f.getDeclaredAnnotations()) {
                    if (t instanceof FlowContext) return f;
                }
            }
        }
        return null;
    }
}
