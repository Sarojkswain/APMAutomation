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

package com.ca.apm.test.em.metadata;


import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import com.ca.apm.automation.action.flow.utility.ConfigureFlowContext;
import com.ca.apm.test.em.metadata.hammond.HammondRole;
import com.ca.tas.resolver.ITasResolver;
import com.ca.tas.role.EmRole;
import com.ca.tas.role.utility.UtilityRole;
import com.ca.tas.testbed.Bitness;
import com.ca.tas.testbed.ITestbed;
import com.ca.tas.testbed.ITestbedFactory;
import com.ca.tas.testbed.ITestbedMachine;
import com.ca.tas.testbed.Testbed;
import com.ca.tas.testbed.TestbedMachine;



/**
 * This testbed still needs quite a lot manual work
 * 
 * 
 * @author svazd01
 *
 */
public abstract class MetadataPerformanceStandaloneTestbed implements ITestbedFactory {

    public static final String STADALONE_MACHINE = "em";
    public static final String STADALONE_MACHINE_OLD = "em_old";



    public static final String HAMMOND_MACHINE_1 = "h1";
    public static final String HAMMOND_MACHINE_2 = "h2";


    public static final String STADALONE_ROLE = "em_role";
    public static final String MODIFY_CONFIGS_ROLE = "modify_role";

    public static final String STADALONE_ROLE_OLD = "em_role_old";
    public static final String MODIFY_CONFIGS_ROLE_OLD = "modify_role_old";


    public static final String HAMMOND_ROLE_1 = "h1_role";
    public static final String HAMMOND_ROLE_2 = "h2_role";


    public static final String UMNOUNT_DELAY_KEY =
        "introscope.enterprisemanager.autoUnmountDelayInMinutes";
    public static final String UNMOUNT_DELAY_VALUE = "1";


    protected abstract int getMetricRotationScale();

    protected abstract int getMetricRotationInterval();

    protected abstract int getMetricCount();

    protected abstract int getAgentCount();

    protected abstract double getAgentScale();

    @Override
    public ITestbed create(ITasResolver resolver) {
        ITestbed testbed = new Testbed("MetadataPerformanceStandaloneTestbed");

        EmRole standa =
            new EmRole.Builder(STADALONE_ROLE, resolver).nostartWV().nostartEM()
                .configProperty(UMNOUNT_DELAY_KEY, UNMOUNT_DELAY_VALUE)
                .installDir("E:\\em").databaseDir("E:\\database")
                .emLaxNlClearJavaOption(getLaxProps()).build();

        String installDir = standa.getDeployEmFlowContext().getInstallDir();



        UtilityRole<ConfigureFlowContext> configRole =
            UtilityRole.configFlow(MODIFY_CONFIGS_ROLE, getConfiguratinFlowContext(installDir));
        configRole.after(standa);

        ITestbedMachine standaMachine =
            new TestbedMachine.Builder(STADALONE_MACHINE).templateId("w64").bitness(Bitness.b64)
                .build();
        standaMachine.addRole(standa, configRole);



        testbed.addMachine(standaMachine);

        HammondRole hammondRole1 =
            new HammondRole.Builder(HAMMOND_ROLE_1, resolver).heapMemory("4608m")
                .rotationInterval(getMetricRotationInterval())
                .rotationScale(getMetricRotationScale()).player(HammondRole.Player.SYNTHETIC)
                .metricCount(getMetricCount()).agentCount(getAgentCount())
                .collector(resolver.getHostnameById(STADALONE_ROLE)).prefix(HAMMOND_MACHINE_1)
                .build();
        TestbedMachine hammonMachine1 =
            new TestbedMachine.Builder(HAMMOND_MACHINE_1).templateId("w64").bitness(Bitness.b64)
                .build();
        hammonMachine1.addRole(hammondRole1);
        testbed.addMachine(hammonMachine1);


        EmRole standaOld =
            new EmRole.Builder(STADALONE_ROLE_OLD, resolver).version("10.2.0-SNAPSHOT")
                .installDir("E:\\em").databaseDir("E:\\database").nostartWV().nostartEM()
                .configProperty(UMNOUNT_DELAY_KEY, UNMOUNT_DELAY_VALUE)
                .emLaxNlClearJavaOption(getLaxProps()).build();


        UtilityRole<ConfigureFlowContext> configRoleOld =
            UtilityRole.configFlow(MODIFY_CONFIGS_ROLE_OLD, getConfiguratinFlowContext(installDir));
        configRoleOld.after(standaOld);

        ITestbedMachine standaOldMachine =
            new TestbedMachine.Builder(STADALONE_MACHINE_OLD).templateId("w64")
                .bitness(Bitness.b64).build();
        standaOldMachine.addRole(standaOld, configRoleOld);



        testbed.addMachine(standaOldMachine);

        HammondRole hammondRoleOld =
            new HammondRole.Builder(HAMMOND_ROLE_2, resolver).heapMemory("4608m")
                .rotationInterval(getMetricRotationInterval())
                .rotationScale(getMetricRotationScale()).player(HammondRole.Player.SYNTHETIC)
                .metricCount(getMetricCount()).agentCount(getAgentCount())
                .collector(resolver.getHostnameById(STADALONE_ROLE_OLD)).prefix(HAMMOND_MACHINE_2)
                .build();
        TestbedMachine hammonMachineOld =
            new TestbedMachine.Builder(HAMMOND_MACHINE_2).templateId("w64").bitness(Bitness.b64)
                .build();
        hammonMachineOld.addRole(hammondRoleOld);



        testbed.addMachine(hammonMachineOld);

        return testbed;
    }


    protected ConfigureFlowContext getConfiguratinFlowContext(String emDir) {
        ConfigureFlowContext.Builder builder = new ConfigureFlowContext.Builder();

        String tresholdFile = emDir.concat("\\config\\apm-events-thresholds-config.xml");

        builder.configurationMap(tresholdFile, getTresholdMap());


        return builder.build();
    }


    protected Collection<String> getLaxProps() {
        StringTokenizer st =
            new StringTokenizer(
                "-Xms7g -Xmx7g -Djava.awt.headless=false -Dmail.mime.charset=UTF-8 -Dorg.owasp.esapi.resources=./config/esapi -XX:+UseConcMarkSweepGC -XX:+UseParNewGC -Xss512k",
                " ");
        List<String> laxOptions = new ArrayList<String>();
        while (st.hasMoreTokens()) {
            laxOptions.add(st.nextToken());
        }
        return laxOptions;
    }


    protected Map<String, String> getTresholdMap() {
        Map<String, String> tresholds = new HashMap<String, String>();
        tresholds.put(
            "//clamp[@id='introscope.enterprisemanager.metrics.live.limit']/threshold/@value",
            "1000000000");
        tresholds
            .put(
                "//clamp[@id='introscope.enterprisemanager.metrics.historical.limit']/threshold/@value",
                "900000000");
        tresholds.put(
            "//clamp[@id='introscope.enterprisemanager.agent.connection.limit']/threshold/@value",
            "100000000");
        tresholds.put(
            "//clamp[@id='introscope.enterprisemanager.agent.metrics.limit']/threshold/@value",
            "10000000000");
        tresholds
            .put(
                "//clamp[@id='introscope.enterprisemanager.disconnected.historical.agent.limit']/threshold/@value",
                "100000000");


        return tresholds;
    }
}
