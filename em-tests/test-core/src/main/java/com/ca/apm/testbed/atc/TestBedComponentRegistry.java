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
package com.ca.apm.testbed.atc;

import static java.lang.String.format;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.FilenameUtils;
import org.eclipse.aether.artifact.Artifact;
import org.eclipse.aether.artifact.DefaultArtifact;

import com.ca.apm.automation.action.flow.commandline.RunCommandFlow;
import com.ca.apm.automation.action.flow.commandline.RunCommandFlowContext;
import com.ca.tas.resolver.ITasResolver;
import com.ca.tas.role.NodeJsRole;
import com.ca.tas.role.utility.ExecutionRole;
import com.ca.tas.role.utility.GenericRole;
import com.ca.tas.testbed.ITestbed;
import com.ca.tas.testbed.ProtractorTestBed;
import com.ca.tas.testbed.TestbedMachine;
import com.ca.tas.type.Platform;

/**
 * Provides various standard components for testbed definition of ATC capability.
 * 
 * @author Pospichal, Pavel <pospa02@ca.com>
 *
 */
public class TestBedComponentRegistry {

    /**
     * Adds the NodeJS with additional webdriver-manager module to particular machine in tesbed.
     * <p>
     * The webdriver-manager is updated with the latest drivers and started.
     * <p/>
     * 
     * NOTE: The actions are specific for {@link Platform#WINDOWS}.
     * 
     * @param testbed Target testbed the new roles will be part of.
     * @param machine The machine the new roles will be deployed on.
     * @param tasResolver The resolver from TAS.
     */
    public void addNodeJsWithWebdriverManager(ITestbed testbed, TestbedMachine machine,
            ITasResolver tasResolver) {

        NodeJsRole nodeJsRole =
                new NodeJsRole.Builder(ProtractorTestBed.NODEJS_ROLE_ID, tasResolver).build();
        machine.addRole(nodeJsRole);

        String nodeJsHomeDir = nodeJsRole.getDeployContext().getDestination();
        Map<String, String> npmEnvironment = Collections.emptyMap();

        RunCommandFlowContext setNpmRegistryCxt =
                new RunCommandFlowContext.Builder("./bin/node").workDir(nodeJsHomeDir)
                        .args(Arrays
                                .asList("npm", "config", "set", "registry",
                                        "http://isl-dsdc.ca.com/artifactory/api/npm/npm-org"))
                        .environment(npmEnvironment).build();
        ExecutionRole setNpmRegistryExecRole =
                new ExecutionRole.Builder("setNpmRegistry").flow(RunCommandFlow.class,
                        setNpmRegistryCxt).build();
        setNpmRegistryExecRole.after(nodeJsRole);
        machine.addRole(setNpmRegistryExecRole);

        RunCommandFlowContext installSeleniumServerCxt =
                new RunCommandFlowContext.Builder("./bin/node")
                        .workDir(nodeJsHomeDir)
                        .args(Arrays
                                .asList("npm", "install", "selenium-standalone@4.4.2", "--save-dev"))
                        .environment(npmEnvironment).build();
        ExecutionRole installSeleniumServerExecRole =
                new ExecutionRole.Builder("installSeleniumServer").flow(RunCommandFlow.class,
                        installSeleniumServerCxt).build();
        installSeleniumServerExecRole.after(setNpmRegistryExecRole);
        machine.addRole(installSeleniumServerExecRole);

        // install drivers
        String s3LocalStorageDir =
                FilenameUtils.separatorsToWindows(machine.getAutomationBaseDir() + "/s3storage");

        Artifact commonDriver =
                new DefaultArtifact("com.googleapis.storage.selenium-release", "selenium-server",
                        "jar", "2.45");
        GenericRole installCommonDriverRole =
                new GenericRole.Builder("installCommonDriver", tasResolver).download(
                        commonDriver,
                        FilenameUtils.separatorsToWindows(s3LocalStorageDir
                                + "/2.45/selenium-server-standalone-2.45.0.jar"))
                        .build();
        installCommonDriverRole.after(installSeleniumServerExecRole);
        machine.addRole(installCommonDriverRole);

        Artifact chromeDriver =
                new DefaultArtifact("com.googleapis.storage.chromedriver", "chromedriver", "win32",
                        "zip", "2.15");
        GenericRole installChromeDriverRole =
                new GenericRole.Builder("installChromeDriver", tasResolver).download(
                        chromeDriver,
                        FilenameUtils.separatorsToWindows(s3LocalStorageDir
                                + "/2.15/chromedriver_win32.zip")).build();
        installChromeDriverRole.after(installSeleniumServerExecRole);
        machine.addRole(installChromeDriverRole);

        Artifact ieDriver =
                new DefaultArtifact("com.googleapis.storage.selenium-release", "IEDriverServer",
                        "x64", "zip", "2.45");
        GenericRole installIEDriverRole =
                new GenericRole.Builder("installIEDriver", tasResolver).download(
                        ieDriver,
                        FilenameUtils.separatorsToWindows(s3LocalStorageDir
                                + "/2.45/IEDriverServer_x64_2.45.0.zip")).build();
        installIEDriverRole.after(installSeleniumServerExecRole);
        machine.addRole(installIEDriverRole);

        RunCommandFlowContext installLocalMirrorCxt =
                new RunCommandFlowContext.Builder("./bin/node")
                        .workDir(nodeJsHomeDir)
                        .args(Arrays
                                .asList("npm", "install", "local-web-server@0.5.19", "--save-dev"))
                        .environment(npmEnvironment).build();
        ExecutionRole installLocalMirrorExecRole =
                new ExecutionRole.Builder("installLocalMirror").flow(RunCommandFlow.class,
                        installLocalMirrorCxt).build();
        installLocalMirrorExecRole.after(setNpmRegistryExecRole);
        machine.addRole(installLocalMirrorExecRole);

        Map<String, String> nodeModuleEnvironment = new HashMap<>();
        nodeModuleEnvironment.put("NODE_PATH", nodeJsRole.getDeployContext()
                .getNodeJsModulesDirectory());

        RunCommandFlowContext startLocalMirrorCxt =
                new RunCommandFlowContext.Builder("node_modules/.bin/ws")
                        .workDir(nodeJsHomeDir)
                        .args(Arrays
                                .asList("--port", "9000", "--directory", s3LocalStorageDir,
                                        "--log-format", "none"))
                        .environment(nodeModuleEnvironment)
                        .terminateOnMatch("serving (.*) at (.*)")
                        .build();
        ExecutionRole startLocalMirrorExecRole =
                new ExecutionRole.Builder("startLocalMirror").flow(RunCommandFlow.class,
                        startLocalMirrorCxt).build();
        startLocalMirrorExecRole.after(installLocalMirrorExecRole, installCommonDriverRole,
                installChromeDriverRole, installIEDriverRole);
        machine.addRole(startLocalMirrorExecRole);

        RunCommandFlowContext installSeleniumDriversCxt =
                new RunCommandFlowContext.Builder("node_modules/.bin/selenium-standalone")
                        .workDir(nodeJsHomeDir)
                        .args(Arrays
                                .asList("install", "--baseURL=http://localhost:9000",
                                        "--drivers.chrome.baseURL=http://localhost:9000",
                                        "--drivers.ie.baseURL=http://localhost:9000"))
                        .environment(nodeModuleEnvironment).build();
        ExecutionRole installSeleniumDriversExecRole =
                new ExecutionRole.Builder("installSeleniumDrivers").flow(RunCommandFlow.class,
                        installSeleniumDriversCxt).build();
        installSeleniumDriversExecRole.after(startLocalMirrorExecRole);
        machine.addRole(installSeleniumDriversExecRole);

        RunCommandFlowContext startSeleniumServerCxt =
                new RunCommandFlowContext.Builder("node_modules/.bin/selenium-standalone")
                        .workDir(nodeJsHomeDir)
                        .args(Arrays
                                .asList("start"))
                        .environment(nodeModuleEnvironment)
                        .terminateOnMatch("Selenium started").build();
        ExecutionRole startSeleniumServerExecRole =
                new ExecutionRole.Builder("startSeleniumServer").flow(RunCommandFlow.class,
                        startSeleniumServerCxt).build();
        startSeleniumServerExecRole.after(installSeleniumDriversExecRole);
        machine.addRole(startSeleniumServerExecRole);

        testbed.addProperty("selenium.webdriverURL",
                format("http://%s:4444/wd/hub", tasResolver.getHostnameById("startSeleniumServer")));
    }
}
