/*
 * Copyright (c) 2017 CA.  All rights reserved.
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

package com.ca.tas.test.em.appmap;

import java.util.Arrays;

import com.ca.apm.automation.action.flow.FlowConfig.FlowConfigBuilder;
import com.ca.apm.automation.action.flow.commandline.RunCommandFlow;
import com.ca.apm.test.em.util.RoleUtility;
import com.ca.tas.artifact.IBuiltArtifact.Version;
import com.ca.tas.resolver.ITasResolver;
import com.ca.tas.role.DelayRole;
import com.ca.tas.role.EmRole;
import com.ca.tas.role.IRole;
import com.ca.tas.role.oracle.OracleApmDbRole;
import com.ca.tas.role.utility.ExecutionRole;
import com.ca.tas.role.utility.UniversalRole;
import com.ca.tas.testbed.Bitness;
import com.ca.tas.testbed.ITestbed;
import com.ca.tas.testbed.ITestbedFactory;
import com.ca.tas.testbed.ITestbedMachine;
import com.ca.tas.testbed.Testbed;
import com.ca.tas.testbed.TestbedMachine;
import com.ca.tas.tests.annotations.TestBedDefinition;
import com.ca.tas.type.Platform;

@TestBedDefinition
public class UpgradeEmTestBed_10_1 implements ITestbedFactory {

    public static final String MACHINE_ID = "standalone";
    public static final String EM_ROLE_ID = "introscope";

    public static final String ORACLE_MACHINE_ID = "orclMachine";
    public static final String ORACLE_ROLE_ID = "role_em_oracle";

    @Override
    public ITestbed create(ITasResolver tasResolver) {
        ITestbed testbed = new Testbed("Introscope/AppMap/UpgradeEm_10_X");

        ITestbedMachine emMachine =
            new TestbedMachine.Builder(MACHINE_ID).platform(Platform.WINDOWS)
                .templateId("w64").bitness(Bitness.b64).automationBaseDir("C:/sw").build();
        testbed.addMachine(emMachine);
        
        EmRole.Builder emBuilder = new EmRole.Builder(EM_ROLE_ID, tasResolver)
                .nostartEM()
                .version(getEmVersion());
        emBuilder.installerTgDir(emBuilder.getWinDeployBase() + "installers\\em_10_X");
        if (useOracle()) {
            emBuilder.useOracle(addOracleRole(testbed, tasResolver));
        }
        if (separateWebView()) {
            emBuilder.silentInstallChosenFeatures(
                Arrays.asList("Enterprise Manager", "ProbeBuilder", "Database"));
        }

        EmRole emRole = emBuilder.build();
        emMachine.addRole(emRole);
        if (separateWebView()) {
            EmRole.Builder wvBuilder = new EmRole.Builder(EM_ROLE_ID + "_wv", tasResolver)
                .nostartWV()
                .silentInstallChosenFeatures(Arrays.asList("WebView"))
                .version(getEmVersion())
                .installSubDir("webview");
            wvBuilder.installerTgDir(wvBuilder.getWinDeployBase() + "installers\\em_10_X");
            EmRole wvRole = wvBuilder.build();
            emMachine.addRole(wvRole);
            wvRole.before(emRole);
        }
        
        IRole mmRole = RoleUtility.addMmRole(emMachine, emRole.getRoleId() + "_mm", emRole, "NowhereBankMM");
        IRole nbRole = RoleUtility.addNowhereBankRole(emMachine, emRole, null, tasResolver);
        
        IRole startRole = RoleUtility.addStartEmRole(emMachine, emRole, !separateWebView(), nbRole);
        startRole.after(mmRole, nbRole);
        
        DelayRole delayRole = new DelayRole("delay", 120);
        emMachine.addRole(delayRole);
        delayRole.after(startRole);
        
        UniversalRole.Builder stopRoleBuilder = new UniversalRole.Builder("stop", tasResolver);
        if (!separateWebView()) {
            stopRoleBuilder.runFlow(new FlowConfigBuilder(RunCommandFlow.class, emRole.getWvStopCommandFlowContext()));
        }
        stopRoleBuilder.runFlow(new FlowConfigBuilder(RunCommandFlow.class, emRole.getEmStopCommandFlowContext()));
        stopRoleBuilder.runFlow(new FlowConfigBuilder(RunCommandFlow.class, emRole.getEmKillCommandFlowContext()));
        UniversalRole stopRole = stopRoleBuilder.build();
        emMachine.addRole(stopRole);
        stopRole.after(delayRole);

        EmRole.Builder emUpgBuilder =
            new EmRole.Builder(EM_ROLE_ID + "_upgrade", tasResolver)
                .installerProperty("shouldUpgrade", "true")
                .installerProperty("upgradeSchema", "true");
        if (useOracle()) {
            emUpgBuilder.useOracle()
                .oracleDbHost(tasResolver.getHostnameById(ORACLE_ROLE_ID))
                .oracleDbUsername(OracleApmDbRole.Builder.DEFAULT_APM_USER)
                .oracleDbPassword(OracleApmDbRole.Builder.DEFAULT_APM_PASSWORD);
        }
        if (separateWebView()) {
            emUpgBuilder.nostartWV();
            emUpgBuilder.silentInstallChosenFeatures(
                Arrays.asList("Enterprise Manager", "ProbeBuilder", "Database"));
        }
        EmRole emUpgRole = emUpgBuilder.build();
        emMachine.addRole(emUpgRole);
        emUpgRole.after(stopRole);
        if (separateWebView()) {
            EmRole.Builder wvUpgBuilder = new EmRole.Builder(EM_ROLE_ID + "_upgrade_wv", tasResolver)
                .nostartEM()
                .installerProperty("shouldUpgrade", "true")
                .silentInstallChosenFeatures(Arrays.asList("WebView"))
                .installSubDir("webview");
            EmRole wvUpgRole = wvUpgBuilder.build();
            emMachine.addRole(wvUpgRole);
            wvUpgRole.before(emUpgRole);

            ExecutionRole startWvRole = new ExecutionRole.Builder(EM_ROLE_ID + "_start_upg_wv")
                            .syncCommand(wvUpgRole.getWvRunCommandFlowContext())
                            .build();
            startWvRole.after(wvUpgRole);
        }
        
        // register remote Selenium Grid
        testbed.addProperty("selenium.webdriverURL", "http://cz-selenium1.ca.com:4444/wd/hub");

        testbed.addProperty(
            "test.applicationBaseURL",
            String.format("http://%s:8082", tasResolver.getHostnameById(EM_ROLE_ID)));

        return testbed;
    }
    
    String getEmVersion() {
        return Version.RELEASE_10_1.getValue();
    }
    
    boolean useOracle() {
        return false;
    }
    
    boolean separateWebView() {
        return false;
    }
    
    private OracleApmDbRole addOracleRole(ITestbed testbed, ITasResolver tasResolver) {
        ITestbedMachine dbMachine =
            new TestbedMachine.Builder(ORACLE_MACHINE_ID).platform(Platform.WINDOWS)
                .templateId("w64").bitness(Bitness.b64).automationBaseDir("C:/sw").build();
        OracleApmDbRole apmOracleRole =
            new OracleApmDbRole.Builder(ORACLE_ROLE_ID, tasResolver).build();
        dbMachine.addRole(apmOracleRole);
        testbed.addMachine(dbMachine);
        return apmOracleRole;
    }
}
