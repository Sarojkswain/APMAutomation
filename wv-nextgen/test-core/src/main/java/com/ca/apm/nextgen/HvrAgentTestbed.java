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

package com.ca.apm.nextgen;

import com.ca.apm.nextgen.role.HVRAgentRole;
import com.ca.tas.artifact.IBuiltArtifact;
import com.ca.tas.artifact.ITasArtifact;
import com.ca.tas.artifact.ITasArtifactFactory;
import com.ca.tas.artifact.TasArtifact;
import com.ca.tas.artifact.thirdParty.JavaBinary;
import com.ca.tas.resolver.ITasResolver;
import com.ca.tas.role.IRole;
import com.ca.tas.role.webapp.JavaRole;
import com.ca.tas.testbed.ITestbed;
import com.ca.tas.testbed.ITestbedFactory;
import com.ca.tas.testbed.ITestbedMachine;
import com.ca.tas.testbed.Testbed;
import com.ca.tas.testbed.TestbedMachine;
import com.ca.tas.tests.annotations.TestBedDefinition;

/**
 * HvrAgentTestbed class.
 * <p/>
 * HVR Agent testbed.
 *
 * @author haiva01@ca.com
 */
@TestBedDefinition
public class HvrAgentTestbed implements ITestbedFactory {
    public static final String HVR_AGENT_TESTBED = "hvrAgentTestbed";
    public static final String HVR_ROLE = "hvr01";
    public static final String MACHINE_ID = "hvrAgentMachine";
    public static final String JAVA_ROLE = "java18Role";
    public static final String QATNG_DEPLOY_ROLE = "qatf01";

    static class FldHvrAgentMetrics implements ITasArtifactFactory {
        private static final String GROUP_ID = "com.ca.apm.fld";
        private static final String ARTIFACT_ID = "hvragent-extract";
        private static final IBuiltArtifact.TasExtension EXTENSION = IBuiltArtifact.TasExtension.JAR;
        private static final String VERSION = "9.8";

        public FldHvrAgentMetrics() {
        }

        @Override
        public ITasArtifact createArtifact(String version) {
            return new TasArtifact.Builder(ARTIFACT_ID).groupId(GROUP_ID).version(version)
                .extension(EXTENSION).build();
        }

        @Override
        public ITasArtifact createArtifact() {
            return createArtifact(VERSION);
        }
    }

    @Override
    public ITestbed create(ITasResolver tasResolver) {
        ITestbedMachine machine =
            new TestbedMachine.Builder(MACHINE_ID).templateId(ITestbedMachine.TEMPLATE_W64)
                .build();

        // add HVR agent role
        HVRAgentRole hvrRole =
            new HVRAgentRole.Builder(HVR_ROLE, tasResolver)
                .addMetricsArtifact(new FldHvrAgentMetrics())
                .emHost("fldcoll07c")
                .emPort("5001")
                .loadFile("extract")
                .cloneconnections(8)
                .cloneagents(25)
                .secondspertrace(1)
                .agentHost("HVRAgent")
                //.start()
                .build();
        machine.addRole(hvrRole);

        // add Java role
        IRole javaRole = new JavaRole.Builder(JAVA_ROLE, tasResolver)
            .version(JavaBinary.WINDOWS_64BIT_JDK_18).build();
        machine.addRole(javaRole);

        ITestbed testBed = new Testbed(HVR_AGENT_TESTBED);
        testBed.addMachine(machine);
        return testBed;
    }
}
