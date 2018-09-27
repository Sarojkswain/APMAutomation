/*
 * Copyright (c) 2015 CA.  All rights reserved.
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
 * MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE.  IN NO EVENT WILL CA BE
 * LIABLE TO THE END USER OR ANY THIRD PARTY FOR ANY LOSS OR DAMAGE, DIRECT OR
 * INDIRECT, FROM THE USE OF THIS SOFTWARE, INCLUDING WITHOUT LIMITATION, LOST
 * PROFITS, BUSINESS INTERRUPTION, GOODWILL, OR LOST DATA, EVEN IF CA IS
 * EXPRESSLY ADVISED OF SUCH LOSS OR DAMAGE.
 */

package com.ca.apm.test.atc.common;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ca.apm.automation.action.flow.commandline.RunCommandFlowContext;
import com.ca.apm.automation.action.flow.em.DeployEMFlowContext;
import com.ca.apm.automation.action.test.EmUtils;
import com.ca.apm.automation.action.test.PortUtils;
import com.ca.tas.envproperty.MachineEnvironmentProperties;
import com.ca.tas.role.EmRole;
import com.ca.tas.test.TasTestNgTest;
import com.ca.tas.type.Platform;

public abstract class WebViewTestNgTest extends TasTestNgTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(WebViewTestNgTest.class);
    private final EmUtils emUtils;

    protected WebViewTestNgTest() {
        emUtils = utilities.createEmUtils();
    }

    protected void checkWebview(String roleId) {
        String wvHost = envProperties.getMachineHostnameByRoleId(roleId);
        String wvPort = envProperties.getRolePropertyById(roleId, "wvPort");
        
        while (!loadPage("http://" + wvHost + ":" + wvPort)) {
            killWebview(roleId);
            runSerializedCommandFlowFromRole(roleId, EmRole.ENV_START_WEBVIEW);
        }
    }
    
    private boolean loadPage(String pageUrl) {
        try {
            URL url = new URL(pageUrl);
            HttpURLConnection con = (HttpURLConnection)url.openConnection();
            int responseCode = con.getResponseCode();
            con.disconnect();
            LOGGER.info("Response code from Webview: " + Integer.toString(responseCode));
            if (responseCode < HttpURLConnection.HTTP_BAD_REQUEST) {
                return true;
            }
        } catch (Exception e) {
            // swallow all
        }
        return false;
    }
    
    protected void killWebview(String roleId) {
        try {
            String machineId = envProperties.getMachineIdByRoleId(roleId);
            Platform platform =
                Platform.fromString(envProperties.getMachinePropertyById(machineId,
                    MachineEnvironmentProperties.PLATFORM));
            if (platform == Platform.WINDOWS) {
                RunCommandFlowContext runCommandFlowContext =
                    new RunCommandFlowContext.Builder("taskkill").args(
                        Arrays.asList("/F", "/T", "/IM", EmRole.Builder.WEBVIEW_EXECUTABLE)).build();
                runCommandFlowByMachineId(machineId, runCommandFlowContext);
            } else {
                RunCommandFlowContext runCommandFlowContext =
                    new RunCommandFlowContext.Builder("pkill").args(
                        Arrays.asList("-f", EmRole.LinuxBuilder.WEBVIEW_EXECUTABLE)).build();
                runCommandFlowByMachineId(machineId, runCommandFlowContext);
            }
        } catch (Exception e) {
            // swallow all
        }
    }
    
    protected void killEM(String roleId) {
        try {
            String machineId = envProperties.getMachineIdByRoleId(roleId);
            Platform platform =
                Platform.fromString(envProperties.getMachinePropertyById(machineId,
                    MachineEnvironmentProperties.PLATFORM));
            if (platform == Platform.WINDOWS) {
                RunCommandFlowContext runCommandFlowContext =
                    new RunCommandFlowContext.Builder("taskkill").args(
                        Arrays.asList("/F", "/T", "/IM", EmRole.Builder.INTROSCOPE_EXECUTABLE)).build();
                runCommandFlowByMachineId(machineId, runCommandFlowContext);
            } else {
                RunCommandFlowContext runCommandFlowContext =
                    new RunCommandFlowContext.Builder("pkill").args(
                        Arrays.asList("-f", EmRole.LinuxBuilder.INTROSCOPE_EXECUTABLE)).build();
                runCommandFlowByMachineId(machineId, runCommandFlowContext);
                LOGGER.info("Waiting for EM port is available");
                new PortUtils().waitTillRemotePortIsAvailableInSec(
                    envProperties.getMachineHostnameByRoleId(roleId),
                    Integer.parseInt(envProperties.getRolePropertyById(roleId,
                            DeployEMFlowContext.ENV_EM_PORT)),
                    60);
            }
        } catch (Exception e) {
            // swallow all
        }
    }
    
    protected void startEmAndWebview(String roleId) {
        runSerializedCommandFlowFromRole(roleId, EmRole.ENV_START_EM);
        runSerializedCommandFlowFromRole(roleId, EmRole.ENV_START_WEBVIEW);
        checkWebview(roleId);
    }
    
    protected String getEmWebUrl(String roleId) {
        String wvHost = getFQDN(envProperties.getMachineHostnameByRoleId(roleId));
        String emWebPort = envProperties.getRolePropertyById(roleId, "emWebPort");
        
        return "http://" + wvHost + ":" + emWebPort;   
    }
    
    protected String getVWUrl(String roleId) {
        String wvHost = getFQDN(envProperties.getMachineHostnameByRoleId(roleId));
        String wvPort = envProperties.getRolePropertyById(roleId, "wvPort");
        
        return "http://" + wvHost + ":" + wvPort;   
    }
    
    protected String getFQDN(String hostname) {
        return emUtils.hostnameToFqdn(hostname);   
    }
}
