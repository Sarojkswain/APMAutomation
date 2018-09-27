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
package com.ca.apm.tests.utils;

import org.jetbrains.annotations.NotNull;

import com.ca.tas.resolver.ITasResolver;
import com.ca.tas.role.AgentRole;
import com.ca.tas.role.EmRole;
import com.ca.tas.role.webapp.TomcatRole;
import com.ca.tas.testbed.TestbedMachine;
import com.ca.tas.type.Platform;

/**
 * Factories of builders of TAS entities for various platforms.
 * 
 * @author TURLU01
 *
 */
public class BuilderFactories {

    /**
     * 
     * @param platform Platform of the machine
     * @param roleId
     * @param tasResolver
     * @return
     */
    public static EmRole.Builder getEmBuilder(Platform platform, String roleId,
        ITasResolver tasResolver) {
        switch (platform) {
            case WINDOWS:
                return new EmRole.Builder(roleId, tasResolver);
            case LINUX:
                return new EmRole.LinuxBuilder(roleId, tasResolver);
            default:
                throw new RuntimeException("Unknown platform.");
        }
    }

    /**
     * 
     * @param platform Platform of the machine
     * @param roleId
     * @param tasResolver
     * @return
     */
    public static TomcatRole.Builder getTomcatBuilder(Platform platform, String roleId,
        ITasResolver tasResolver) {
        switch (platform) {
            case WINDOWS:
                return new TomcatRole.Builder(roleId, tasResolver);
            case LINUX:
                return new TomcatRole.LinuxBuilder(roleId, tasResolver);
            default:
                throw new RuntimeException("Unknown platform.");
        }
    }


    /**
     * 
     * @param platform Platform of the machine
     * @param roleId
     * @param tasResolver
     * @return
     */
    public static AgentRole.Builder getAgentBuilder(Platform platform, String roleId,
        ITasResolver tasResolver) {
        switch (platform) {
            case WINDOWS:
                return new AgentRole.Builder(roleId, tasResolver);
            case LINUX:
                return new AgentRole.LinuxBuilder(roleId, tasResolver);
            default:
                throw new RuntimeException("Unknown platform.");
        }
    }

    /**
     * 
     * @param platform Platform of the machine
     * @param machineId The id that will be used for addressing the machine in a testbed.
     * @return
     */
    public static TestbedMachine.Builder getTestbedMachineBuilder(Platform platform,
        @NotNull String machineId) {
        switch (platform) {
            case WINDOWS:
                return new TestbedMachine.Builder(machineId);
            case LINUX:
                return new TestbedMachine.LinuxBuilder(machineId);
            default:
                throw new RuntimeException("Unknown platform.");
        }
    }

}
