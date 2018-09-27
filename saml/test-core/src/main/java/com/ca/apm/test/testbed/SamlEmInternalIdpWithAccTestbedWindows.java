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

package com.ca.apm.test.testbed;

import java.util.HashMap;
import java.util.Map;

import com.ca.apm.test.AccConfigurationRole;
import com.ca.tas.artifact.IBuiltArtifact.ArtifactPlatform;
import com.ca.tas.artifact.built.AccServerArtifact;
import com.ca.tas.resolver.ITasResolver;
import com.ca.tas.role.EmRole;
import com.ca.tas.role.IRole;
import com.ca.tas.role.acc.AccServerRole;
import com.ca.tas.testbed.ITestbed;
import com.ca.tas.testbed.ITestbedMachine;
import com.ca.tas.tests.annotations.TestBedDefinition;

@TestBedDefinition
public class SamlEmInternalIdpWithAccTestbedWindows extends SamlEmInternalIdpTestbedWindows {
    
    private static final String ACC_ROOT_DIR = "c:\\automation\\deployed\\acc";
    public static final String ACC_SERVER_ROLE_ID = "accServerRole";

    @Override
    public ITestbed create(ITasResolver tasResolver) {
        ITestbed testbed =  super.create(tasResolver);
        ITestbedMachine machine = testbed.getMachineById(MACHINE_ID);
        
        Map<String, String> customConfig = new HashMap<>();
        customConfig.put("authentication.central.enabled", "true");
        customConfig.put("webserver.https.enable", "false");
        
        AccServerArtifact accArtefact = new AccServerArtifact(ArtifactPlatform.WINDOWS_AMD_64, tasResolver);
        
        AccServerRole.Builder accServerRoleBuilder = new AccServerRole.Builder(ACC_SERVER_ROLE_ID, tasResolver)
                .artifact(accArtefact)
                .version("99.99.accDev-SNAPSHOT")
                .installDir(ACC_ROOT_DIR)
                .disableSsl()
                .customConfig(customConfig)
                .noStart();            

        AccServerRole accServerRole = accServerRoleBuilder.build();
        machine.addRole(accServerRole);
        
        EmRole emRole = (EmRole)machine.getRoleById(ROLE_EM);
        IRole samlConfigRole = machine.getRoleById(ROLE_SAML_CONFIG);
        AccConfigurationRole.Builder accConfigurationRoleBuilder = new AccConfigurationRole.Builder("accConfigureRole", tasResolver);
        accConfigurationRoleBuilder.accRootDir(ACC_ROOT_DIR).apmRootDir(emRole.getDeployEmFlowContext().getInstallDir());
        
        AccConfigurationRole accConfigurationRole = accConfigurationRoleBuilder.build();
        accConfigurationRole.after(accServerRole, emRole);
        samlConfigRole.after(accConfigurationRole);
        machine.addRole(accConfigurationRole);

        return testbed;
    }

}
