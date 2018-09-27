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

package com.ca.apm.powerpack.sysview.tests.testbed;

import org.jetbrains.annotations.NotNull;

import com.ca.tas.testbed.Bitness;
import com.ca.tas.testbed.TestbedMachine;
import com.ca.tas.type.Platform;

public class MainframeTestbedMachine {
    public static final String TEMPLATE_CA11 = "zos_ca11";
    public static final String TEMPLATE_CA31 = "zos_ca31";

    public static class Builder extends TestbedMachine.LinuxBuilder {
        private static final String SSH_USER_NAME = "none";
        private static final String AUTOMATION_BASE_DIR = "/u/users/wily/jetty/default/tmp";
        private static final String DEFAULT_JAVA_HOME = "/sys/java31bt/v8r0m0/usr/lpp/java/J8.0";

        public Builder(@NotNull String machineId) {
            super(machineId);
            this.sshUserName(SSH_USER_NAME);
            this.automationBaseDir(AUTOMATION_BASE_DIR);
            this.defaultJavaHome(DEFAULT_JAVA_HOME);
            // Right now the platform/bitness information has no impact on our use of Mainframe
            // machines so we're fine with keeping it 'linux @ x86' for the time being.
            this.platform(Platform.LINUX);
            this.bitness(Bitness.b32);
        }
    }
}
