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

package com.ca.apm.transactiontrace.appmap.testbed;

import java.util.Collection;
import java.util.Collections;

import org.jetbrains.annotations.NotNull;

import com.ca.apm.transactiontrace.appmap.artifact.MagentoSampleDataVersion;
import com.ca.apm.transactiontrace.appmap.artifact.MagentoVersion;
import com.ca.apm.transactiontrace.appmap.role.ApacheRole;
import com.ca.apm.transactiontrace.appmap.role.DeferredInitiateTransactionTraceSessionRole;
import com.ca.apm.transactiontrace.appmap.role.MagentoRole;
import com.ca.apm.transactiontrace.appmap.role.PhpAgentRole;
import com.ca.apm.transactiontrace.appmap.role.PhpAgentRole.PhpVersion;
import com.ca.apm.transactiontrace.appmap.role.PhpRole;
import com.ca.tas.builder.TasBuilder;
import com.ca.tas.resolver.ITasResolver;
import com.ca.tas.role.EmRole;
import com.ca.tas.role.MysqlRole;
import com.ca.tas.testbed.ITestbed;
import com.ca.tas.testbed.ITestbedFactory;
import com.ca.tas.testbed.ITestbedMachine;
import com.ca.tas.testbed.TestBedUtils;
import com.ca.tas.testbed.Testbed;
import com.ca.tas.tests.annotations.TestBedDefinition;

/**
 * Test bed that installs
 *
 * on a CentOS machine
 * 1. Stand Alone EM
 * 2. Magento test application (+ Apache, PHP, MySQL)
 * 3. PHP agent (= Collector Agent + PHP probe)
 * 
 * on a Windows machine
 * 1. Selenium web driver for Chrome
 */
@TestBedDefinition
public class PhpAgentStandAloneTestbed implements ITestbedFactory {

    private static final String PHP_TEMPLATE_ID = ITestbedMachine.TEMPLATE_CO66;
    public static final String PHP_MACHINE = "phpMachine";

    private static final String MYSQL_ROLE_ID = "mysqlRole";

    public static final String MAGENTO_ROLE_ID = "magentoRole";
    public static final String APACHE_ROLE_ID = "apacheRole";
    public static final String PHP_ROLE_ID = "phpRole";
    public static final String PHP_AGENT_ROLE_ID = "phpAgentRole";

    // ---

    public static final String EM_ROLE_ID = "emRole";
    public static final String INITIATE_TT_SESSION_ROLE_ID = "inititateTTSessionRole";

    private static final String EM_CONF_PROP_TT_ARRIVAL_BUFFER_INCUB_TIME_FAST =
        "introscope.enterprisemanager.transactiontrace.arrivalbuffer.incubationtime.fast";

    @Override
    public ITestbed create(ITasResolver tasResolver) {

        final String magentoUnpackDest = "/var/www/html/magento";
        final String magentoSampleDataUnpackDest =
            TasBuilder.LINUX_SOFTWARE_LOC + "magento-sample-data";

        // apache role
        ApacheRole apacheRole = new ApacheRole.LinuxBuilder(APACHE_ROLE_ID).build();

        // php role
        PhpRole phpRole =
            new PhpRole.LinuxBuilder(PHP_ROLE_ID).withMySql().withMCrypt().withXml().withGd()
                .build();

        // mysql role
        String sqlFile = getSqlScriptLocation(magentoSampleDataUnpackDest);
        MysqlRole mysqlRole =
            new MysqlRole.LinuxBuilder(MYSQL_ROLE_ID).autoStart().build();

        // magento role
        MagentoRole magentoRole =
            new MagentoRole.LinuxBuilder(MAGENTO_ROLE_ID, tasResolver)
                .magentoVersion(MagentoVersion.v1_9_2_0).magentoDestination(magentoUnpackDest)
                .magentoSampleDataVersion(MagentoSampleDataVersion.v1_9_1_0)
                .magentoSampleDataDestination(magentoSampleDataUnpackDest)
                .mysqlRole(mysqlRole).sqlImportScript(sqlFile).apacheRole(apacheRole).build();

        // php agent role
        PhpAgentRole phpAgentRole =
            new PhpAgentRole.LinuxBuilder(PHP_AGENT_ROLE_ID, tasResolver)
                .phpAgentVersion("10.2.0.28")
                .phpVersion(PhpVersion.PHP53)
                .phpExtDirPath("/usr/lib64/php/modules")
                .phpExtConfDirPath("/etc/php.d")
                .appendToPbdFile(
                    Collections
                        .singleton("TraceOneMethodOfClass: Varien_Db_Adapter_Pdo_Mysql query PhpBackendMarker \"!BRIDGE!{programname}|Backends|{classname}\""))
                .collectorAgentAutoStart().build();

        // EM role
        EmRole emRole =
            new EmRole.LinuxBuilder(EM_ROLE_ID, tasResolver).configProperty(
                EM_CONF_PROP_TT_ARRIVAL_BUFFER_INCUB_TIME_FAST, "30").build();

        // initiate TT session role
        DeferredInitiateTransactionTraceSessionRole traceSessionRole =
            new DeferredInitiateTransactionTraceSessionRole.LinuxBuilder(
                INITIATE_TT_SESSION_ROLE_ID).emRole(emRole).build();


        phpRole.after(apacheRole);

        magentoRole.after(phpRole, mysqlRole);

        phpAgentRole.after(magentoRole);

        emRole.before(phpAgentRole);

        traceSessionRole.after(phpAgentRole);

        ITestbedMachine phpMachine =
            TestBedUtils.createLinuxMachine(PHP_MACHINE, PHP_TEMPLATE_ID, apacheRole, phpRole,
                mysqlRole, magentoRole, emRole, phpAgentRole, traceSessionRole);

        SeleniumGridMachinesFactory seleniumGridMachinesFactory = new SeleniumGridMachinesFactory();
        Collection<ITestbedMachine> seleniumGridMachines = seleniumGridMachinesFactory.createMachines(tasResolver);

        return new Testbed(getClass().getSimpleName()).addMachine(phpMachine)
            .addMachines(seleniumGridMachines);
    }

    @NotNull
    private String getSqlScriptLocation(String unpackDest) {
        return unpackDest + TasBuilder.LINUX_SEPARATOR
            + MagentoSampleDataVersion.v1_9_1_0.getSqlFileWithinArchive();
    }
}
