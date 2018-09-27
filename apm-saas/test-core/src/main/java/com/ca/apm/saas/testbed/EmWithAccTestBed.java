/*
 * Copyright (c) 2014 CA. All rights reserved.
 *
 * This software and all information contained therein is confidential and proprietary and shall not
 * be duplicated, used, disclosed or disseminated in any way except as authorized by the applicable
 * license agreement, without the express written permission of CA. All authorized reproductions
 * must be marked with this language.
 *
 * EXCEPT AS SET FORTH IN THE APPLICABLE LICENSE AGREEMENT, TO THE EXTENT PERMITTED BY APPLICABLE
 * LAW, CA PROVIDES THIS SOFTWARE WITHOUT WARRANTY OF ANY KIND, INCLUDING WITHOUT LIMITATION, ANY
 * IMPLIED WARRANTIES OF MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE. IN NO EVENT WILL CA BE
 * LIABLE TO THE END USER OR ANY THIRD PARTY FOR ANY LOSS OR DAMAGE, DIRECT OR INDIRECT, FROM THE
 * USE OF THIS SOFTWARE, INCLUDING WITHOUT LIMITATION, LOST PROFITS, BUSINESS INTERRUPTION,
 * GOODWILL, OR LOST DATA, EVEN IF CA IS EXPRESSLY ADVISED OF SUCH LOSS OR DAMAGE.
 */

package com.ca.apm.saas.testbed;

import com.ca.apm.automation.action.flow.em.DeployEMFlowContext;
import com.ca.tas.resolver.ITasResolver;
import com.ca.tas.role.EmRole;
import com.ca.tas.role.EmRole.LinuxBuilder;
import com.ca.tas.role.acc.AccServerRole;
import com.ca.tas.testbed.*;
import com.ca.tas.tests.annotations.TestBedDefinition;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;

/**
 * Minimal testbed for EM, and ACC installation.
 * Used for TAS deployment of new builds.
 * Extracted from FLD LOGGER testbed.
 *
 * @author filja01 (original FLD one)
 * @author shadm01
 */
@TestBedDefinition
public class EmWithAccTestBed implements ITestbedFactory {

    public static final String DEFAULT_EM_VERSION = "99.99.saastrial-SNAPSHOT";
    public static final String DEFAULT_ACC_VERSION = "99.99.accFalcon-SNAPSHOT";

    public static final String INSTALL_DIR = "/home/sw/em/Introscope";

    public static final String INSTALL_ACC_DIR = "c:/sw/ApmCommandCenter";

    public static final String INSTALL_TG_DIR = "/home/sw/em/Installer";
    public static final String DATABASE_DIR = "/data/em/database";
    public static final String GC_LOG_FILE = INSTALL_DIR + "/logs/gclog.txt";

    public static final String DB_PASSWORD = "password";
    public static final String DB_ADMIN_PASSWORD = "password123";
    public static final String DB_USERNAME = "cemadmin";
    public static final String DB_ADMIN_USERNAME = "postgres";

    public static final int EMWEBPORT = 8081;

    public static final String ADMIN_AUX_TOKEN = "f47ac10b-58cc-4372-a567-0e02b2c3d479";

    private static final Collection<String> MOM_LAXNL_JAVA_OPTION = Arrays.asList(
            "-Djava.awt.headless=true", "-Dmail.mime.charset=UTF-8", "-Dorg.owasp.esapi.resources=./config/esapi",
            "-XX:+UseConcMarkSweepGC", "-XX:+UseParNewGC", "-Xss512k", "-Dcom.wily.assert=false", "-showversion",
            "-XX:CMSInitiatingOccupancyFraction=50", "-XX:+HeapDumpOnOutOfMemoryError", "-Xms1600m", "-Xmx1600m",
            "-verbose:gc", "-Xloggc:" + GC_LOG_FILE, "-Dappmap.user=admin",
            "-Dappmap.token=" + ADMIN_AUX_TOKEN);

    private final String EM_MOM_ROLE = "emMomRole";
    private final String ACC_ROLE = "accRole";

    private final String MOM_MACHINE = "momMachine"; //linux
    private final String ACC_MACHINE = "accMachine"; //windows

    @Override
    public ITestbed create(ITasResolver tasResolver) {

        Testbed testbed = new Testbed("FLDMainClusterTestbed");

        ITestbedMachine momMachine = addEmMachine(tasResolver);
        ITestbedMachine accMachine = addAccMachine(tasResolver);

        testbed.addMachine(momMachine);
        testbed.addMachine(accMachine);

        return testbed;
    }

    @NotNull private ITestbedMachine addEmMachine(ITasResolver tasResolver) {
        ITestbedMachine momMachine =
                new TestbedMachine.LinuxBuilder(MOM_MACHINE)
                        .templateId(ITestbedMachine.TEMPLATE_CO65)
                        .bitness(Bitness.b64)
                        .build();

        EmRole momRole = new LinuxBuilder(EM_MOM_ROLE, tasResolver)
                .silentInstallChosenFeatures(Arrays.asList("Database", "Enterprise Manager", "WebView"))
                .emClusterRole(DeployEMFlowContext.EmRoleEnum.MANAGER)
                .nostartEM()
                .nostartWV()
                .version(DEFAULT_EM_VERSION)
                .emWebPort(EMWEBPORT)
                .installDir(INSTALL_DIR)
                .installerTgDir(INSTALL_TG_DIR)
                .emLaxNlClearJavaOption(MOM_LAXNL_JAVA_OPTION)

//                .dbhost(tasResolver.getHostnameById(EM_DB_ROLE))
                .dbuser(DB_USERNAME)
                .dbpassword(DB_PASSWORD)
                .dbAdminUser(DB_ADMIN_USERNAME)
                .dbAdminPassword(DB_ADMIN_PASSWORD)
                .databaseDir(DATABASE_DIR)

                .build();

        momMachine.addRole(momRole);

        return momMachine;
    }

    @NotNull private ITestbedMachine addAccMachine(ITasResolver tasResolver) {

        ITestbedMachine accMachine =
                new TestbedMachine.Builder(ACC_MACHINE)
                        .templateId(ITestbedMachine.TEMPLATE_W64)
                        .bitness(Bitness.b64)
                        .build();

        HashMap<String, String> config = new HashMap<>();
        config.put("security.basic.enabled", "true");

        AccServerRole accRole = new AccServerRole.Builder(ACC_ROLE, tasResolver)
                .version(DEFAULT_ACC_VERSION)
                .installDir(INSTALL_ACC_DIR)
                .disableSsl()
                .customConfig(config)
                .build();

        accMachine.addRole(accRole);

        return accMachine;
    }
}
