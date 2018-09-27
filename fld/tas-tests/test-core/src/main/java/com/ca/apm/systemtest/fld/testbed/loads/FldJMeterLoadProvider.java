/**
 * 
 */
package com.ca.apm.systemtest.fld.testbed.loads;

import static com.ca.apm.systemtest.fld.testbed.util.FLDTestbedUtil.getJavaDir;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.aether.artifact.Artifact;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ca.apm.systemtest.fld.artifact.thirdparty.FLDJmeterScriptsVersion;
import com.ca.apm.systemtest.fld.artifact.thirdparty.JMeterVersion;
import com.ca.apm.systemtest.fld.artifact.thirdparty.LoadTestArtifact;
import com.ca.apm.systemtest.fld.role.JMeterRole;
import com.ca.apm.systemtest.fld.role.PortForwardingRole;
import com.ca.apm.systemtest.fld.role.loads.JMeterLoadRole;
import com.ca.apm.systemtest.fld.testbed.FLDConstants;
import com.ca.apm.systemtest.fld.testbed.FLDLoadConstants;
import com.ca.apm.systemtest.fld.testbed.FldTestbedProvider;
import com.ca.tas.artifact.thirdParty.JavaBinary;
import com.ca.tas.resolver.ITasResolver;
import com.ca.tas.role.utility.UniversalRole;
import com.ca.tas.role.webapp.JavaRole;
import com.ca.tas.testbed.Bitness;
import com.ca.tas.testbed.ITestbed;
import com.ca.tas.testbed.ITestbedMachine;
import com.ca.tas.testbed.Testbed;
import com.ca.tas.testbed.TestbedMachine;
import com.ca.tas.type.Platform;

/**
 * @author keyja01
 *
 */
public class FldJMeterLoadProvider implements FldTestbedProvider, FLDLoadConstants, FLDConstants {

    private class PortForwardConfig {
        int nextPort = 8000;
        Map<String, Integer> map = new HashMap<>();
    }
    private static final Logger log = LoggerFactory.getLogger(FldJMeterLoadProvider.class);
    
    private int nextRoleId = 0;
    private static final int NUM_MACHINES = 4;
    private ITestbedMachine[] machines = new ITestbedMachine[NUM_MACHINES];
    private JMeterRole[] jmeterRoles = new JMeterRole[NUM_MACHINES];
    private ITestbedMachine[] timMachines = new ITestbedMachine[TIM_MACHINES.length];
    private String[] timHostnames = new String[TIM_ROLES.length];
    
    private HashMap<Integer, PortForwardConfig> forwardPortMap = new HashMap<>();
    private int portForwardId = 0;
    
    private void setupJMeter(int idx, String roleId, ITasResolver tasResolver) {
        ITestbedMachine machine = machines[idx];
        
        JavaRole javaRole = javaRole(JavaBinary.WINDOWS_64BIT_JDK_17, tasResolver);
        JMeterRole jmeterRole = new JMeterRole.Builder(roleId, tasResolver)
            .jmeterVersion(JMeterVersion.v213)
            .jmeterScriptsArchive(FLDJmeterScriptsVersion.v10_3_1)
            .customJava(javaRole)
            .build();
        
        machine.addRole(jmeterRole, javaRole);
        jmeterRoles[idx] = jmeterRole;
    }
    
    
    private int nextForwardingPort(int timIdx, String targetHost, int targetPort) {
        PortForwardConfig cfg = forwardPortMap.get(timIdx);
        if (cfg == null) {
            cfg = new PortForwardConfig();
            forwardPortMap.put(timIdx, cfg);
        }
        
        String key = targetHost.toLowerCase() + ":" + targetPort;
        Integer port = cfg.map.get(key);
        if (port == null) {
            port = cfg.nextPort++;
            cfg.map.put(key, port);
        }
        
        return port;
    }
    
    
    private void configureLoad(ITasResolver tasResolver, int machineIdx, int timIdx, String roleId, 
                               String script, String targetHost, int targetPort, boolean forward) {
        log.info("Configuring JMeter load {} ({}) -> {}:{}, forwarding = {}", roleId, script, targetHost, targetPort, forward);
        String hostHeader = targetHost;
        if (forward) {
            int forwardPort = nextForwardingPort(timIdx, targetHost, targetPort);
            String name = "portforward-" + portForwardId++ + "-" + roleId;
            PortForwardingRole pfRole = new PortForwardingRole.Builder(name)
                .listenPort(forwardPort).targetIpAddress(targetHost).targetPort(targetPort)
                .workDir(name).build();
            timMachines[timIdx].addRole(pfRole);
            targetPort = forwardPort;
            targetHost = timHostnames[timIdx];
            log.info("Configuring port forwarding: tim={}:{}, listen={}, targetAddr={}, targetPort={}, workDir={}", timIdx, timMachines[timIdx], forwardPort, targetHost, targetPort, name);
        }
        if (targetHost == null) {
            // HACK - when using tas:deploy, hostnames are not yet properly worked out during the init phase
            targetHost = "localhost";
        }
        JMeterLoadRole jmeterLoadRole = new JMeterLoadRole.Builder(roleId, tasResolver)
            .host(targetHost)
            .hostHeader(hostHeader)
            .port(targetPort)
            .script(script)
            .resultFile(script.replace(".jmx", ".csv"))
            .isOutputToFile(true)
            .jmeter(jmeterRoles[machineIdx])
            .build();
        
        machines[machineIdx].addRole(jmeterLoadRole);
    }
    
    private void configureLoad(ITasResolver tasResolver, int machineIdx, int timIdx, String roleId, 
                               String script, String targetHost, int targetPort) {
        configureLoad(tasResolver, machineIdx, timIdx, roleId, script, targetHost, targetPort, true);
    }
    
    
    @Override
    public Collection<ITestbedMachine> initMachines() {
        String[] ids = new String[] {
            JMETER_MACHINE_01_ID, JMETER_MACHINE_02_ID, JMETER_MACHINE_03_ID, JMETER_MACHINE_04_ID
        };
        for (int i = 0; i < ids.length; i++) {
            machines[i] = new TestbedMachine.Builder(ids[i])
                .platform(Platform.WINDOWS)
                .templateId(ITestbedMachine.TEMPLATE_W64)
                .bitness(Bitness.b64)
                .build();
        }
        
        return Arrays.asList(machines);
    }
    
    
    /* (non-Javadoc)
     * @see com.ca.apm.systemtest.fld.testbed.FldTestbedProvider#initTestbed(com.ca.tas.testbed.Testbed, com.ca.tas.resolver.ITasResolver)
     */
    @Override
    public void initTestbed(ITestbed testbed, ITasResolver tasResolver) {
        setupJMeter(0, JMETER_ROLE_01_ID, tasResolver);
        setupJMeter(1, JMETER_ROLE_02_ID, tasResolver);
        setupJMeter(2, JMETER_ROLE_03_ID, tasResolver);
        setupJMeter(3, JMETER_ROLE_04_ID, tasResolver);
        
        
        for (int i = 0; i < FLDConstants.TIM_ROLES.length; i++) {
            
            String id = FLDConstants.TIM_MACHINES[i];
            timMachines[i] = testbed.getMachineById(id);
            timHostnames[i] = tasResolver.getHostnameById(TIM_ROLES[i]);
        }
        
    /*
     *  Tomcat related jmeter loads
     */
        String hostTomcat9080 = tasResolver.getHostnameById(TOMCAT_9080_ROLE_ID);
        configureLoad(tasResolver, 0, 1, JMETER_LOAD_ROLE_TOMCAT9080_01_ID, "Tomcat_fld_01_02_Tomcat_9080_Axis2.jmx", hostTomcat9080, 8080);
        configureLoad(tasResolver, 0, 1, JMETER_LOAD_ROLE_WURLITZER_TOMCAT9080_01_ID, "wurlitzer_fld_01_Tomcat_9080.jmx", hostTomcat9080, 8080);
    
        String hostTomcat9081 = tasResolver.getHostnameById(TOMCAT_9081_ROLE_ID);
        configureLoad(tasResolver, 0, 1, JMETER_LOAD_ROLE_TOMCAT9081_01_ID, "Tomcat_fld_01_01_Tomcat_9081_Axis2.jmx", hostTomcat9081, 9080);
        configureLoad(tasResolver, 0, 1, JMETER_LOAD_ROLE_WURLITZER_TOMCAT9081_01_ID, "wurlitzer_fld_01_Tomcat_9081.jmx", hostTomcat9081, 9080);
        
        
        String host6Tomcat9091 = tasResolver.getHostnameById(TOMCAT_6_ROLE_ID);
        configureLoad(tasResolver, 0, 2, JMETER_LOAD_ROLE_6TOMCAT9091_01_ID, "Tomcat_fld_01_03_Tomcat6_9091_Axis2.jmx", host6Tomcat9091, 8080);
        configureLoad(tasResolver, 0, 2, JMETER_LOAD_ROLE_6TOMCAT9091T_01_ID, "Tomcat_fld_01_04_Tomcat6_9091_Trade.jmx", host6Tomcat9091, 8080);
        
        //REPLACED by new load
        //Tess App load test for jmeter - add them to jboss loads were is more resources
        //configureLoad(tasResolver, 1, 2, JMETER_LOAD_ROLE_6TOMCAT_LOADTEST_01_ID, "Tomcat_fld_01_06_Tomcat6_LoadTest.jmx", host6Tomcat9091, 8080);
        //addLoadtestFileInJMeter(tasResolver, 1, ADD_LOADTEST_ROLE_JMETER_6TOMCAT_01_ID, LoadTestArtifact.Loadtest);
        
        String host7Tomcat9090 = tasResolver.getHostnameById(TOMCAT_7_ROLE_ID);
        configureLoad(tasResolver, 0, 2, JMETER_LOAD_ROLE_7TOMCAT9090_01_ID, "Tomcat_fld_01_05_Tomcat7_9090_Axis2.jmx", host7Tomcat9090, 9080);
        
    /*
     *  WebSphere related jmeter loads
     */
        
        String hostWebSphere = tasResolver.getHostnameById(WEBSPHERE_01_ROLE_ID);
        configureLoad(tasResolver, 0, 3, JMETER_LOAD_ROLE_WAS_01_ID, "was_fld.jmx", hostWebSphere, 9080);
        configureLoad(tasResolver, 0, 3, JMETER_LOAD_ROLE_WAS_BRT_01_ID, "WAS_fld_01_01_WAS_BRTTestApp.jmx", hostWebSphere, 9080);
        
    /*
     *  JBoss related jmeter loads
     */
        String hostJBoss6 = tasResolver.getHostnameById(JBOSS6_ROLE_ID);
        String hostJBoss7 = tasResolver.getHostnameById(JBOSS7_ROLE_ID);
        configureLoad(tasResolver, 1, 0, JMETER_LOAD_ROLE_JBOSS6_01_ID, "jboss_fld_01_JBoss6_fldjboss01c_8383.jmx", hostJBoss6, 8180);
        configureLoad(tasResolver, 1, 0, JMETER_LOAD_ROLE_JBOSS7_01_ID, "jboss_fld_02_JBoss7_fldjboss01c_8080.jmx", hostJBoss7, 8080);
        
    /*
     *  WebLogic related jmeter loads
     */
        String hostWeblogic01 = tasResolver.getHostnameById(WLS_01_SERVER_01_ROLE_ID);
        String hostWeblogic02 = tasResolver.getHostnameById(WLS_02_SERVER_01_ROLE_ID);
        configureLoad(tasResolver, 2, 2, JMETER_LOAD_ROLE_SOA_WLS7001_01_ID, "soa_fld_01_fldwls01c_7001.jmx", hostWeblogic01, 7001);
        configureLoad(tasResolver, 2, 2, JMETER_LOAD_ROLE_SOA_WLS7002_01_ID, "soa_fld_02_fldwls01c_7002.jmx", hostWeblogic01, 7002);
        configureLoad(tasResolver, 2, 2, JMETER_LOAD_ROLE_SOA_WLS7001_02_ID, "soa_fld_03_fldwls02c_7003.jmx", hostWeblogic02, 7001);
        configureLoad(tasResolver, 2, 2, JMETER_LOAD_ROLE_SOA_WLS7002_02_ID, "soa_fld_04_fldwls02c_7002.jmx", hostWeblogic02, 7002);
        
    /*
     *  AppMap related jmeter loads
     */ 
        String hostAppMap = tasResolver.getHostnameById(AGC_ROLE_ID);
        configureLoad(tasResolver, 1, 0, JMETER_LOAD_ROLE_APPMAP_ID, "appmap-load.jmx", hostAppMap, 8081, false);
        configureLoad(tasResolver, 1, 0, JMETER_LOAD_ROLE_APPMAP_TEAMCENTER_ID, "TeamCenterWithCode.jmx", hostAppMap, 8081, false);
        
    /*
     *  Dotnet related jmeter loads
     */
        String hostDotNet01 = tasResolver.getHostnameById(DOTNET_MACHINE1+"_"+DOTNET_AGENT_ROLE_ID);
        configureLoad(tasResolver, 3, 4, JMETER_LOAD_ROLE_FLDNET01_01_ID, "fldnet01_02_01_fldnet01c_8086.jmx", hostDotNet01, 8086);
        configureLoad(tasResolver, 3, 4, JMETER_LOAD_ROLE_FLDNET01_02_ID, "fldnet01_02_02_fldnet01c_8085.jmx", hostDotNet01, 8085);
        configureLoad(tasResolver, 3, 4, JMETER_LOAD_ROLE_FLDNET01_03_ID, "fldnet01_02_03_fldnet01c_8082.jmx", hostDotNet01, 8082);
        configureLoad(tasResolver, 3, 4, JMETER_LOAD_ROLE_FLDNET01_04_ID, "fldnet01_02_04_fldnet01c_8081.jmx", hostDotNet01, 8081);
    
        String hostDotNet02 = tasResolver.getHostnameById(DOTNET_MACHINE2+"_"+DOTNET_AGENT_ROLE_ID);
        configureLoad(tasResolver, 3, 4, JMETER_LOAD_ROLE_FLDNET02_01_ID, "fldnet01_02_05_fldnet02c_8081.jmx", hostDotNet02, 8081);
        configureLoad(tasResolver, 3, 4, JMETER_LOAD_ROLE_FLDNET02_02_ID, "fldnet01_02_06_fldnet02c_8082.jmx", hostDotNet02, 8082);
        configureLoad(tasResolver, 3, 4, JMETER_LOAD_ROLE_FLDNET02_03_ID, "fldnet01_02_07_fldnet02c_8084.jmx", hostDotNet02, 8084);
        configureLoad(tasResolver, 3, 4, JMETER_LOAD_ROLE_FLDNET02_04_ID, "fldnet01_02_08_fldnet02c_8086.jmx", hostDotNet02, 8086);
    }
    

    private void addLoadtestFileInJMeter(ITasResolver tasResolver, int machineIdx,
        String roleId, LoadTestArtifact loadtest) {
        
        Artifact artifact = loadtest.getArtifact();
        UniversalRole addLoadTestRole =
            new UniversalRole.Builder(roleId, tasResolver)
                .download(artifact, jmeterRoles[machineIdx].getInstallDir() +"/scripts/loadtest.csv").build();
        machines[machineIdx].addRole(addLoadTestRole);
        
    }


    private JavaRole javaRole(JavaBinary version, ITasResolver tasResolver) {
        JavaRole javaRole = new JavaRole.Builder("jmeterJavaRole" + nextRoleId++, tasResolver)
            .dir(getJavaDir(version))
            .version(version)
            .build();
        
        return javaRole;
    }
}
