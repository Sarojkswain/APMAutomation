/*
 * Copyright (c) 2014-2016 CA. All rights reserved.
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

package com.ca.apm.systemtest.fld.testbed.smoke;

import com.ca.apm.automation.action.flow.em.DeployEMFlowContext;
import com.ca.apm.systemtest.fld.testbed.FldTestbedProvider;
import com.ca.apm.systemtest.fld.testbed.SeleniumWebViewLoadFldTestbedProvider;
import com.ca.tas.resolver.ITasResolver;
import com.ca.tas.role.EmRole.LinuxBuilder;
import com.ca.tas.testbed.*;
import com.ca.tas.tests.annotations.TestBedDefinition;

import java.util.Arrays;
import java.util.Collection;

/**
 * Minimal testbed for DB, EM, WEBVIEW and Selenium GRID installation.
 * Used for selenium GRID testing.
 * Autostarts EM, WV, Selenium HUB & selenium nodes.
 * Extracted from FLD testbed.
 *
 * @author filja01 (original FLD one)
 * @author shadm01
 */
@TestBedDefinition
public class SeleniumWebViewLoadTestBed implements ITestbedFactory {

    public static final String INSTALL_DIR = "/home/sw/em/Introscope";
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

    private static final Collection<String> WV_LAXNL_JAVA_OPTION = Arrays.asList(
            "-Djava.awt.headless=true", "-Dorg.owasp.esapi.resources=./config/esapi", "-Dsun.java2d.noddraw=true",
            "-Dorg.osgi.framework.bootdelegation=org.apache.xpath", "-javaagent:./product/webview/agent/wily/Agent.jar",
            "-Dcom.wily.introscope.agentProfile=./product/webview/agent/wily/core/config/IntroscopeAgent.profile",
            "-Dcom.wily.introscope.wilyForWilyPrefix=com.wily", "-Xms1000m", "-Xmx1000m",
            "-XX:+PrintGCDateStamps", "-XX:+HeapDumpOnOutOfMemoryError",
            "-verbose:gc", "-Xloggc:"+GC_LOG_FILE);

    private final String EM_MOM_ROLE = "emMomRole";
    private final String MOM_MACHINE = "momMachine";

    @Override
    public ITestbed create(ITasResolver tasResolver) {

        Testbed testbed = new Testbed("FLDMainClusterTestbed");

        ITestbedMachine momMachine =
                new TestbedMachine.LinuxBuilder(MOM_MACHINE)
                        .templateId("co65")
                        .bitness(Bitness.b64)
                        .build();
        LinuxBuilder momBuilder = new LinuxBuilder(EM_MOM_ROLE, tasResolver);

        momBuilder
                .silentInstallChosenFeatures(Arrays.asList("Database","Enterprise Manager", "ProbeBuilder", "EPA", "WebView"))
                .emClusterRole(DeployEMFlowContext.EmRoleEnum.MANAGER)
                .dbAdminUser(DB_ADMIN_USERNAME)
                .dbpassword(DB_PASSWORD)
                .dbAdminPassword(DB_ADMIN_PASSWORD)
                .dbuser(DB_USERNAME)
                .databaseDir(DATABASE_DIR)
                .wvPort(8080)
                .wvLaxNlClearJavaOption(WV_LAXNL_JAVA_OPTION)
                .emWebPort(EMWEBPORT)
                .installDir(INSTALL_DIR)
                .installerTgDir(INSTALL_TG_DIR)
                .emLaxNlClearJavaOption(MOM_LAXNL_JAVA_OPTION);

        momMachine.addRole(momBuilder.build()); // MOM role

        FldTestbedProvider seleniumLoadProvider = new SeleniumWebViewLoadFldTestbedProvider();
        testbed.addMachines(seleniumLoadProvider.initMachines());
        seleniumLoadProvider.initTestbed(testbed, tasResolver);

        testbed.addMachine(momMachine);

        return testbed;
    }

}
