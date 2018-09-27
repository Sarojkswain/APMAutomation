package com.ca.apm.systemtest.fld.testbed.loads;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ca.apm.systemtest.fld.artifact.thirdparty.JMeterVersion;
import com.ca.apm.systemtest.fld.role.JMeterRole;
import com.ca.apm.systemtest.fld.role.loads.ParametrizedJMeterLoadRole;
import com.ca.apm.systemtest.fld.testbed.FLDConstants;
import com.ca.apm.systemtest.fld.testbed.FLDLoadConstants;
import com.ca.apm.systemtest.fld.testbed.FldTestbedProvider;
import com.ca.apm.systemtest.fld.testbed.TTStormLoadRecordingTestbedProvider;
import com.ca.tas.artifact.thirdParty.JavaBinary;
import com.ca.tas.resolver.ITasResolver;
import com.ca.tas.role.webapp.JavaRole;
import com.ca.tas.testbed.Bitness;
import com.ca.tas.testbed.ITestbed;
import com.ca.tas.testbed.ITestbedMachine;
import com.ca.tas.testbed.TestbedMachine;
import com.ca.tas.type.Platform;

/**
 * Provider of Jmeter load for generating transaction trace storm load. 
 * 
 * @author Alexander Sinyushkin (sinal04@ca.com)
 *
 */
public class TTStormLoadProvider implements FldTestbedProvider, FLDLoadConstants, FLDConstants {
    public static final String JMETER1_MACHINE_ID = "JMETER01_MACHINE_ID";
    public static final String JMETER1_ROLE_ID = "jmeter01_role";

    public static final String JMETER2_MACHINE_ID = "JMETER02_MACHINE_ID";
    public static final String JMETER2_ROLE_ID = "jmeter02_role";

    public static final String JMETER1_JAVA8_ROLE = "jmeter01_java8_role"; 
    public static final String JMETER1_LOAD_ROLE_ID = "jmeter01_load_role";

    public static final String JMETER2_JAVA8_ROLE = "jmeter02_java8_role"; 
    public static final String JMETER2_LOAD_ROLE_ID = "jmeter02_load_role";


    public static final String JMETER_TEST_PLAN_TARGET_HOST_PARAM_TEMPLATE = "targetHost%d";
    private static final String JDK8_PATH = "C:\\java\\jdk_18";
    private static final String JMETER_SCRIPT_NAME = "transaction_trace_storm_load.jmx";
    private static final String JMETER_TEST_PLAN_RESOURCE = "/tt-storm-load/" + JMETER_SCRIPT_NAME;
    private static final String JMETER_TARGET_SCRIPTS_FOLDER = "testplan";
    
    private static final Logger LOGGER = LoggerFactory.getLogger(TTStormLoadProvider.class);
    
    @Override
    public Collection<ITestbedMachine> initMachines() {
        
        ITestbedMachine jmeter1Machine = new TestbedMachine.Builder(JMETER1_MACHINE_ID)
            .platform(Platform.WINDOWS)
            .bitness(Bitness.b64)
            .templateId("w64")
            .build();
        
        //When a second VM with Jmeter load is needed, uncomment below the second machine 
        //creation code and add the machine to the testbed
        
        //ITestbedMachine jmeter2Machine = new TestbedMachine.Builder(JMETER2_MACHINE_ID)
        //    .platform(Platform.WINDOWS)
        //    .bitness(Bitness.b64)
        //    .templateId("w64")
        //    .build();

        return Arrays.asList(jmeter1Machine);

    }

    @Override
    public void initTestbed(ITestbed testbed, ITasResolver tasResolver) {
        initForMachine(testbed.getMachineById(JMETER1_MACHINE_ID), tasResolver, JMETER1_JAVA8_ROLE, JMETER1_ROLE_ID, JMETER1_LOAD_ROLE_ID);
        
        //When a second VM with Jmeter load is needed, uncomment below the second machine initialization 
        //initForMachine(testbed.getMachineById(JMETER2_MACHINE_ID), tasResolver, JMETER2_JAVA8_ROLE, JMETER2_ROLE_ID, JMETER2_LOAD_ROLE_ID);
    }
    
    private void initForMachine(ITestbedMachine jmeterMachine, ITasResolver tasResolver, String javaRoleId, String jmeterRoleId, String jmeterLoadRoleId) {
        JavaRole javaRole = new JavaRole.Builder(javaRoleId, tasResolver)
        .dir(JDK8_PATH)
        .version(JavaBinary.WINDOWS_64BIT_JDK_18_0_51)
        .build();

        JMeterRole jmeterRole = new JMeterRole.Builder(jmeterRoleId, tasResolver)
            .jmeterVersion(JMeterVersion.v213)
            .installDir("jmeter")
            .testPlanResource("/" + JMETER_TARGET_SCRIPTS_FOLDER + "/" + JMETER_SCRIPT_NAME, JMETER_TEST_PLAN_RESOURCE)
            .customJava(javaRole)
            .build();
        jmeterRole.after(javaRole);
        jmeterMachine.addRole(javaRole, jmeterRole);

        Map<String, String> jmeterTestPlanParamsMap = new HashMap<>(TTStormLoadRecordingTestbedProvider.NUM_OF_AGENTS_PER_COLLECTOR);
        for (int i = 1; i <= TTStormLoadRecordingTestbedProvider.NUM_OF_AGENTS_PER_COLLECTOR; i++) {
            String tomcatRoleId = String.format(TTStormLoadRecordingTestbedProvider.TOMCAT_ROLE_ID_TEMPLATE, i);
            String host = tasResolver.getHostnameById(tomcatRoleId);
            if (host != null) {
                String jmeterTestPlanParam = String.format(JMETER_TEST_PLAN_TARGET_HOST_PARAM_TEMPLATE, i);
                jmeterTestPlanParamsMap.put(jmeterTestPlanParam, host + ":8080");
            }
        }
        
        jmeterTestPlanParamsMap.put("host", tasResolver.getHostnameById(TTStormLoadRecordingTestbedProvider.NGINX_ROLE));
        
        ParametrizedJMeterLoadRole.Builder jmeterLoadRoleBuilder = new ParametrizedJMeterLoadRole.Builder(jmeterLoadRoleId, tasResolver)
            .jmeterParametersMap(jmeterTestPlanParamsMap)
            .scriptsFolder(JMETER_TARGET_SCRIPTS_FOLDER)
            .script(JMETER_SCRIPT_NAME)
            .resultFile(JMETER_SCRIPT_NAME.replace(".jmx", ".csv"))
            .isOutputToFile(true)
            .jmeter(jmeterRole);

        ParametrizedJMeterLoadRole jmeterLoadRole = jmeterLoadRoleBuilder.build(); 
        jmeterLoadRole.after(jmeterRole);
        jmeterMachine.addRole(jmeterLoadRole);
        
    }
}
