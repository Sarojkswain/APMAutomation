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

package com.ca.apm.test.em.agc;

import static org.testng.Assert.assertEquals;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.HashMap;
import java.util.Map;

import com.ca.tas.test.em.agc.ComplexNowhereBankMaintenanceTestBed;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.ca.apm.automation.action.flow.commandline.RunCommandFlowContext;
import com.ca.apm.automation.action.flow.em.DeployEMFlowContext;
import com.ca.apm.automation.action.flow.utility.ConfigureFlowContext;
import com.ca.apm.automation.action.test.ClwRunner;
import com.ca.apm.automation.action.test.EmUtils;
import com.ca.apm.test.atc.common.WebViewTestNgTest;
import com.ca.apm.test.em.util.RestUtility;
import com.ca.apm.test.em.util.RoleUtility;
import com.ca.tas.em.saas.SaasEmTestBed;
import com.ca.tas.role.HammondRole;
import com.ca.tas.role.webapp.TomcatRole;
import com.ca.tas.test.em.agc.ComplexNowhereBankTestBed;
import com.ca.tas.test.em.agc.PerformanceAgcTestBed;
import com.ca.tas.test.em.appmap.PhantomJSTest;
import com.ca.tas.test.utils.LocalStorage;
import com.ca.tas.tests.annotations.Tas;
import com.ca.tas.tests.annotations.TestBed;
import com.ca.tas.type.SizeType;

public class AgcTest extends WebViewTestNgTest  {
    
    private final String REGISTRATION_RESULT = "Registration successful. Restart EM.";
    private final int SHUTDOWN_DELAY = 140;
    private final Logger log = LoggerFactory.getLogger(getClass());
    private LocalStorage localStorage;
    private RestUtility utility = new RestUtility();
    
    @BeforeMethod
    public void initTestMethod(Method testMethod) {
        localStorage = new LocalStorage(testMethod);
    }

    @Tas(testBeds = @TestBed(name = ComplexNowhereBankTestBed.class, executeOn = "standalone"), owner = "korzd01", size = SizeType.MEDIUM)
    @Test(groups = {"agc", "smoke"})
    public void configureTestbed() throws Exception {
        registerMoms();
        runSerializedCommandFlowFromRole(ComplexNowhereBankTestBed.COLLECTOR_AGENT_ROLE_ID,
                                         RoleUtility.ENV_START_COLLECTOR_AGENT);
        runSerializedCommandFlowFromRole(ComplexNowhereBankTestBed.TOMCAT_ROLE_ID, TomcatRole.ENV_TOMCAT_START);
        runSerializedCommandFlowFromRole(ComplexNowhereBankTestBed.HAMMOND_ROLE_ID, HammondRole.ENV_HAMMOND_START + "0");
        configureDockerMonitor();
        importTradingService();
        
        runCommandFlowByMachineId(SaasEmTestBed.EM_MACHINE_ID, createSaasCommand("EMCtrl", "start"));
        runCommandFlowByMachineId(SaasEmTestBed.EM_MACHINE_ID, createSaasCommand("WVCtrl", "start"));
        
        startTTs();
        configureDecorationPolicy();
    }
    
    @Tas(testBeds = @TestBed(name = ComplexNowhereBankTestBed.class, executeOn = "standalone"), owner = "korzd01", size = SizeType.MEDIUM)
    @Test(groups = {"agc", "smoke"})
    public void configureAnalyticsTestbed() throws Exception {
        killWebview(ComplexNowhereBankTestBed.AGC_ROLE_ID);
        EmUtils emUtils = utilities.createEmUtils();
        ClwRunner masterClwRunner =
            utilities.createClwUtils(ComplexNowhereBankTestBed.AGC_ROLE_ID).getClwRunner();
        ClwRunner localClwRunner =
            utilities.createClwUtils(ComplexNowhereBankTestBed.STANDALONE_ROLE_ID).getClwRunner();
        emUtils.stopRemoteEmWithTimeoutSec(localClwRunner, masterClwRunner, SHUTDOWN_DELAY);
        Map<String, String> replacePairsConfig = new HashMap<String, String>();
        replacePairsConfig.put("introscope.apmserver.ui.configuration.name.0", "ANALYTICS_TRACKING_ENABLED");
        replacePairsConfig.put("introscope.apmserver.ui.configuration.value.0", "true");
    
        Map<String, Map<String, String>> data = new HashMap<String, Map<String, String>>();
        data.put(envProperties.getRolePropertiesById(ComplexNowhereBankTestBed.AGC_ROLE_ID)
            .getProperty(DeployEMFlowContext.ENV_EM_CONFIG_FILE), replacePairsConfig);
    
        runConfigureFlowByRoleId(ComplexNowhereBankTestBed.AGC_ROLE_ID, data);
        
        startEmAndWebview(ComplexNowhereBankTestBed.AGC_ROLE_ID);
    }

    @Tas(testBeds = @TestBed(name = ComplexNowhereBankMaintenanceTestBed.class, executeOn = "standalone"), owner = "korzd01", size = SizeType.MEDIUM)
    @Test(groups = {"agc", "smoke"})
    public void configureMaintenanceTestbed() throws Exception {
        registerMoms();
        runSerializedCommandFlowFromRole(ComplexNowhereBankTestBed.TOMCAT_ROLE_ID, TomcatRole.ENV_TOMCAT_START);
        runSerializedCommandFlowFromRole(ComplexNowhereBankTestBed.HAMMOND_ROLE_ID, HammondRole.ENV_HAMMOND_START + "0");
        importTradingService();

        startTTs();
        configureDecorationPolicy();
    }

    private void configureDecorationPolicy() throws Exception {
        String agcHost = envProperties.getMachineHostnameByRoleId(ComplexNowhereBankTestBed.AGC_ROLE_ID);
        utility.addGroup(agcHost, "company");
        List<List<String>> ids = utility.getBTVertices(agcHost,
                Arrays.asList("Options Trading", "Place Order", "Balances", "Login"));
        utility.assignAttributeToVertices(agcHost, ids.get(0), "company", "Pepsi");
        utility.assignAttributeToVertices(agcHost, ids.get(1), "company", "Pepsi");
        utility.assignAttributeToVertices(agcHost, ids.get(2), "company", "Pepsi");
        utility.assignAttributeToVertices(agcHost, ids.get(3), "company", "Lego");
    }

    private void configureDockerMonitor() throws Exception {
        Map<String, String> bundleMap = new HashMap<>();
        bundleMap.put("docker.hostname", envProperties.getRolePropertyById(
            ComplexNowhereBankTestBed.DOCKER_MONITOR_ROLE_ID, RoleUtility.ENV_DOCKER_HOSTNAME));
        bundleMap.put("docker.port", "2375");
        bundleMap.put("apm.accesstoken", RoleUtility.ADMIN_AUX_TOKEN);
        ConfigureFlowContext confDockerCtx =  new ConfigureFlowContext.Builder()
                .configurationMap(envProperties.getRolePropertyById(
                    ComplexNowhereBankTestBed.DOCKER_MONITOR_ROLE_ID, RoleUtility.ENV_DOCKER_CONF_PATH), bundleMap)
                .build();
        runConfigureFlowByMachineId(
            envProperties.getMachineIdByRoleId(ComplexNowhereBankTestBed.DOCKER_MONITOR_ROLE_ID),
            confDockerCtx);
    }

    @Tas(testBeds = @TestBed(name = PerformanceAgcTestBed.class, executeOn = "master"), owner = "korzd01", size = SizeType.MEDIUM)
    @Test(groups = {"agc", "smoke"})
    public void configurePerformanceTestbed() throws Exception {
        String masterHost = envProperties.getMachineHostnameByRoleId(PerformanceAgcTestBed.MASTER_ROLE_ID);

        checkWebview(PerformanceAgcTestBed.MASTER_ROLE_ID);
        registerRemoteFollower(PerformanceAgcTestBed.FOLLOWER1_ROLE_ID, masterHost);
        registerRemoteFollower(PerformanceAgcTestBed.FOLLOWER2_ROLE_ID, masterHost);
    }
    
    private void registerRemoteFollower(String roleId, String masterHost) throws Exception {
        String followerHost = envProperties.getMachineHostnameByRoleId(roleId);
        String agcToken = utility.generateAgcToken(masterHost);
        log.info("AGC token for " + followerHost + ": " + agcToken);
        
        String resultFollower = utility.registerMomtoAgc(followerHost, masterHost, agcToken);
        assertEquals(resultFollower, REGISTRATION_RESULT);
        log.info(followerHost + ": " + resultFollower);
        EmUtils emUtils = utilities.createEmUtils();
        ClwRunner masterClwRunner =
            utilities.createClwUtils(PerformanceAgcTestBed.MASTER_ROLE_ID).getClwRunner();
        ClwRunner followerClwRunner =
            utilities.createClwUtils(roleId).getClwRunner();
        emUtils.stopRemoteEmWithTimeoutSec(masterClwRunner, followerClwRunner, SHUTDOWN_DELAY);
        startEmAndWebview(roleId);
        log.info(followerHost + " restarted.");
    }

    private void importTradingService() throws Exception {
        localStorage.fetchResource(envProperties.getRolePropertiesById("trade-service").getProperty(
                "tbexports.default.url"));
        PhantomJSTest.execute("importBT_TradingService.js", envProperties,
                localStorage.getFileLocations("*bt-exports*.zip"));
        log.info("BT_TradingService was successfully imported.");
    }
    
    private void registerMoms() throws Exception {
        String agcHost = envProperties.getMachineHostnameByRoleId(ComplexNowhereBankTestBed.AGC_ROLE_ID);
        String standaloneHost = envProperties.getMachineHostnameByRoleId(ComplexNowhereBankTestBed.STANDALONE_ROLE_ID);
        String momHost = envProperties.getMachineHostnameByRoleId(ComplexNowhereBankTestBed.MOM_ROLE_ID);
        String saasHost = envProperties.getMachineHostnameByRoleId(SaasEmTestBed.APM_ROLE_ID);

        checkWebview(ComplexNowhereBankTestBed.AGC_ROLE_ID);
        // register STANDALONE
        String agcToken = utility.generateAgcToken(agcHost);
        log.info("AGC token for STANDALONE: " + agcToken);
        
        String resultStandalone = utility.registerMomtoAgc(standaloneHost, agcHost, agcToken);
        assertEquals(resultStandalone, REGISTRATION_RESULT);
        log.info(standaloneHost + ": " + resultStandalone);
        // restart STANDALONE
        EmUtils emUtils = utilities.createEmUtils();
        ClwRunner standaloneClwRunner =
            utilities.createClwUtils(ComplexNowhereBankTestBed.STANDALONE_ROLE_ID).getClwRunner();
        standaloneClwRunner.runClw("shutdown");
        try {
            emUtils.stopLocalEm(standaloneClwRunner, ComplexNowhereBankTestBed.STANDALONE_ROLE_ID);
        } catch (Exception e) {
            log.warn("EM was not stopped properly!");
        }
        startEmAndWebview(ComplexNowhereBankTestBed.STANDALONE_ROLE_ID);
        log.info(standaloneHost + " restarted.");
        
        // register MOM
        agcToken = utility.generateAgcToken(agcHost);
        log.info("AGC token for MOM: " + agcToken);
        
        String resultMom = utility.registerMomtoAgc(momHost, agcHost, agcToken);
        assertEquals(resultMom, REGISTRATION_RESULT);
        log.info(momHost + ": " + resultMom);
        // restart MOM
        ClwRunner momClwRunner =
            utilities.createClwUtils(ComplexNowhereBankTestBed.MOM_ROLE_ID).getClwRunner();
        emUtils.stopRemoteEmWithTimeoutSec(standaloneClwRunner, momClwRunner, SHUTDOWN_DELAY);
        startEmAndWebview(ComplexNowhereBankTestBed.MOM_ROLE_ID);
        log.info(momHost + " restarted.");

        // register SAAS
        agcToken = utility.generateAgcToken(agcHost);
        log.info("AGC token for SaaS: " + agcToken);
        
        String resultSaas = utility.registerMomtoAgc(saasHost, agcHost, agcToken);
        assertEquals(resultSaas, REGISTRATION_RESULT);
        log.info(saasHost + ": " + resultSaas);
        
        // stop SaaS
        ClwRunner saasClwRunner = createSaasClwRunner(saasHost);
        emUtils.stopRemoteEmWithTimeoutSec(standaloneClwRunner, saasClwRunner, SHUTDOWN_DELAY);
        log.info(saasHost + " stopped.");
    }
    
    private static RunCommandFlowContext createSaasCommand(String command, String param) {
        return new RunCommandFlowContext.Builder("docker")
            .args(Arrays.asList("exec", "-i", "tas-apm", "sh", "-c",
                "cd bin && ./" + command + ".sh " + param))
            .build();
    }

    private ClwRunner createSaasClwRunner(String saasHost) {
        String emLibDir = envProperties.getRolePropertyById(ComplexNowhereBankTestBed.STANDALONE_ROLE_ID,
                DeployEMFlowContext.ENV_EM_LIB_DIR);
        return new ClwRunner.Builder()
                        .host(saasHost)
                        .user(SaasEmTestBed.DEFAULT_AUTH_USER)
                        .password(SaasEmTestBed.DEFAULT_AUTH_PASS)
                        .clwWorkStationDir(emLibDir)
                        .build();        
    }

    private void startTTs() {
        String command = "trace transactions exceeding 1 ms in agents matching \".*\" for 120 s";
        ClwRunner standaloneClwRunner =
            utilities.createClwUtils(ComplexNowhereBankTestBed.STANDALONE_ROLE_ID).getClwRunner();
        
        standaloneClwRunner.runClw(command);
        runClwOnRemote(standaloneClwRunner, ComplexNowhereBankTestBed.AGC_COLLECTOR_ROLE_ID, command);
        runClwOnRemote(standaloneClwRunner, ComplexNowhereBankTestBed.MOM_ROLE_ID, command);
    }
    
    private void runClwOnRemote(ClwRunner localRunner, String roleId, String command) {
        ClwRunner runner =
            getRemoteClw(localRunner, utilities.createClwUtils(roleId).getClwRunner());
        runner.runClw(command);
    }

    private ClwRunner getRemoteClw(ClwRunner clwRunnerLocalEm, ClwRunner clwRunnerRemoteEm) {
         return new ClwRunner.Builder()
            .clwWorkStationDir(clwRunnerLocalEm.getClwWorkStationDir())
            .host(clwRunnerRemoteEm.getEmHost())
            .javaPath(clwRunnerLocalEm.getJavaPath())
            .password(clwRunnerRemoteEm.getPassword())
            .user(clwRunnerRemoteEm.getUser())
            .port(clwRunnerRemoteEm.getPort())
            .build();
    }
}
