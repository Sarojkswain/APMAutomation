/*
 * Copyright (c) 2014 CA.  All rights reserved.
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

package com.ca.apm.siteminder.testbed;

import com.ca.apm.automation.action.flow.commandline.RunCommandFlowContext;
import com.ca.apm.em.EMSamlConfigurationRole;
import com.ca.apm.siteminder.ConfigureSMFederationRole;
import com.ca.apm.siteminder.PolicyServerRestartRole;
import com.ca.apm.siteminder.SiteMinderRole;
import com.ca.tas.artifact.IBuiltArtifact.ArtifactPlatform;
import com.ca.tas.resolver.ITasResolver;
import com.ca.tas.role.EmRole;
import com.ca.tas.role.IRole;
import com.ca.tas.role.utility.ExecutionRole;
import com.ca.tas.testbed.ITestbed;
import com.ca.tas.testbed.ITestbedFactory;
import com.ca.tas.testbed.ITestbedMachine;
import com.ca.tas.testbed.Testbed;
import com.ca.tas.testbed.TestbedMachine;
import com.ca.tas.tests.annotations.TestBedDefinition;

/**
 * SiteMinderTestbed class.
 * <p/>
 * Testbed description.
 */

@TestBedDefinition
public class SiteMinderTestbed implements ITestbedFactory {

    public static final String PS_MACHINE_ID = "psMachine";
    public static final String SITEMINDER_ROLE = "siteMinderRole";
    public static final String EM_ROLE = "role_em";
    public static final String JAVA_REG_LOC = "HKEY_LOCAL_MACHINE\\SOFTWARE\\JavaSoft\\Java Development Kit";
    public static final String JAVA_INSTALL_DIR = "C:\\CA\\Java32\\";

    @Override
    public ITestbed create(ITasResolver tasResolver) {
        ITestbedMachine smMachine =
            new TestbedMachine.Builder(PS_MACHINE_ID).templateId("w64").build();
        
        // Update registry to use java 1.6 for servletExec
        ExecutionRole javaRegRole = new ExecutionRole.Builder("java_reg")
                .syncCommand(new RunCommandFlowContext.Builder(
                    "reg add \"" + JAVA_REG_LOC + "\" /v CurrentVersion /t REG_SZ /d 1.6").build())
                .syncCommand(new RunCommandFlowContext.Builder(
                    "reg add \"" + JAVA_REG_LOC + "\\1.6\" /v JavaHome /t REG_SZ /d \"" + JAVA_INSTALL_DIR + "\"").build())
                .build();

        IRole siteMinderRole =
            new SiteMinderRole.Builder(SITEMINDER_ROLE, tasResolver).javaInstallDir(JAVA_INSTALL_DIR).build();
        siteMinderRole.after(javaRegRole);

        EmRole emRole = new EmRole.Builder(EM_ROLE, tasResolver)
                .introscopePlatform(ArtifactPlatform.WINDOWS_AMD_64)
                .nostartEM()
                .nostartWV()
                .build();

        PolicyServerRestartRole policyServerRestartRole =
            new PolicyServerRestartRole("policy_server_restart_role");
        policyServerRestartRole.after(siteMinderRole);

        smMachine.addRole(javaRegRole);
        smMachine.addRole(siteMinderRole);
        smMachine.addRole(emRole);
        smMachine.addRole(policyServerRestartRole);

        EMSamlConfigurationRole samlConfigurationRole = getEmSamlConfigurationRole(tasResolver);
        if (samlConfigurationRole != null) {
            samlConfigurationRole.after(emRole);
            smMachine.addRole(samlConfigurationRole);
        }

        
        ConfigureSMFederationRole configureSMFederationRole = getFederationConfigurationRole(tasResolver);
        if (configureSMFederationRole != null) {
            configureSMFederationRole.after(siteMinderRole, policyServerRestartRole, emRole);
            smMachine.addRole(configureSMFederationRole);
        }

        ITestbed testbed = new Testbed("SiteMinderTestbed");
        testbed.addMachine(smMachine);
        return testbed;
    }
    
    /**
     * Can be overridden with ConfigureSMFederationRole instance to create 
     * federation partnership during deploy.
     * @param tasResolver
     * @return
     */
    protected ConfigureSMFederationRole getFederationConfigurationRole(ITasResolver tasResolver) {
        return null;
    }
    
    /**
     * Can be overridden with EMSamlConfigurationRole instance to configure 
     * EM for SAML.
     * @param tasResolver
     * @return
     */
    protected EMSamlConfigurationRole getEmSamlConfigurationRole(ITasResolver tasResolver) {
        return null;
    }
}
