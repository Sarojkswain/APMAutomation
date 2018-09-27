package com.ca.apm.systemtest.fld.testbed;

import static com.ca.apm.systemtest.fld.testbed.util.FLDTestbedUtil.hasRole;

import java.util.Arrays;
import java.util.Collection;
import java.util.TreeSet;

import org.apache.http.util.Args;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ca.apm.systemtest.fld.artifact.MemoryMonitorWebappArtifact;
import com.ca.apm.systemtest.fld.role.MemoryMonitorRole;
import com.ca.tas.artifact.ITasArtifact;
import com.ca.tas.artifact.thirdParty.TomcatVersion;
import com.ca.tas.resolver.ITasResolver;
import com.ca.tas.role.web.WebAppRole;
import com.ca.tas.role.webapp.TomcatRole;
import com.ca.tas.testbed.Bitness;
import com.ca.tas.testbed.ITestbed;
import com.ca.tas.testbed.ITestbedMachine;
import com.ca.tas.testbed.TestbedMachine;
import com.ca.tas.type.Platform;

public class MemoryMonitorTestbedProvider
    implements
        FldTestbedProvider,
        FLDLoadConstants,
        FLDConstants {

    private static final Logger LOGGER = LoggerFactory
        .getLogger(MemoryMonitorTestbedProvider.class);

    public static final String MEMORY_MONITOR_WEBAPP_CONTEXT_ROOT = "memory-monitor";
    public static final int MEMORY_MONITOR_WEBAPP_PORT = 8080;

    public static final TomcatVersion TOMCAT_VERSION = TomcatVersion.v80;

    private String[] memoryMonitorMachineIds;
    private ITestbedMachine machine;

    private String memoryMonitorWebappMachineId = MEMORY_MONITOR_WEBAPP_MACHINE_ID;
    private boolean isLinuxMachine = true;
    private String gcLogFile = FLDMainClusterTestbed.GC_LOG_FILE;

    public MemoryMonitorTestbedProvider(String[] memoryMonitorMachineIds) {
        Args.check(memoryMonitorMachineIds != null && memoryMonitorMachineIds.length > 0,
            "memoryMonitorMachineIds is null or empty");
        this.memoryMonitorMachineIds =
            (new TreeSet<>(Arrays.asList(memoryMonitorMachineIds))).toArray(new String[0]);
    }

    @Override
    public Collection<ITestbedMachine> initMachines() {
        machine =
            new TestbedMachine.Builder(memoryMonitorWebappMachineId).platform(Platform.WINDOWS)
                .templateId(ITestbedMachine.TEMPLATE_W64).bitness(Bitness.b64).build();

        return Arrays.asList(machine);
    }

    @Override
    public void initTestbed(ITestbed testbed, ITasResolver tasResolver) {
        ITasArtifact mmWebApp = new MemoryMonitorWebappArtifact(tasResolver).createArtifact();
        WebAppRole<TomcatRole> memoryMonitorWebappRole =
            new WebAppRole.Builder<TomcatRole>(MEMORY_MONITOR_WEBAPP_ROLE_ID).artifact(mmWebApp)
                .cargoDeploy().contextName(MEMORY_MONITOR_WEBAPP_CONTEXT_ROOT).build();

        TomcatRole tomcatRole =
            new TomcatRole.Builder(MEMORY_MONITOR_WEBAPP_TOMCAT_ROLE_ID, tasResolver)
                .additionalVMOptions(
                    Arrays.asList("-Xms256m", "-Xmx512m", "-XX:PermSize=256m",
                        "-XX:MaxPermSize=512m", "-server")).tomcatVersion(TOMCAT_VERSION)
                .webApp(memoryMonitorWebappRole).autoStart().build();
        tomcatRole.before(memoryMonitorWebappRole);

        machine.addRole(memoryMonitorWebappRole, tomcatRole);
        testbed.addMachine(machine);

        String webappHost = tasResolver.getHostnameById(MEMORY_MONITOR_WEBAPP_TOMCAT_ROLE_ID);
        LOGGER
            .info(
                "MemoryMonitorTestbedProvider.initTestbed():: MemoryMonitor summary page URL: http://{}:{}/{}/api/memorymonitor/summary/{}",
                webappHost, MEMORY_MONITOR_WEBAPP_PORT, MEMORY_MONITOR_WEBAPP_CONTEXT_ROOT,
                FLD_MEMORY_MONITOR_GROUP);

        // EM machines
        for (String machineId : memoryMonitorMachineIds) {
            ITestbedMachine memoryMonitorMachine = testbed.getMachineById(machineId);
            // memory monitoring (start/stop scripts)
            String memoryMonitorRoleId = "memoryMonitorRole_" + machineId;
            if (!hasRole(memoryMonitorMachine, memoryMonitorRoleId)) {
                MemoryMonitorRole memoryMonitorRole =
                    (new MemoryMonitorRole.Builder(memoryMonitorRoleId, tasResolver))
                        .isLinuxMachine(isLinuxMachine).gcLogFile(gcLogFile)
                        .memoryMonitorGroup(FLD_MEMORY_MONITOR_GROUP)
                        .memoryMonitorRoleName(machineId).memoryMonitorWebappHost(webappHost)
                        .memoryMonitorWebappPort(MEMORY_MONITOR_WEBAPP_PORT)
                        .memoryMonitorWebappContextRoot(MEMORY_MONITOR_WEBAPP_CONTEXT_ROOT).build();
                memoryMonitorMachine.addRole(memoryMonitorRole);
            }
        }
    }

    public void setMemoryMonitorWebappMachineId(String memoryMonitorWebappMachineId) {
        this.memoryMonitorWebappMachineId = memoryMonitorWebappMachineId;
    }

    public void setLinuxMachine(boolean isLinuxMachine) {
        this.isLinuxMachine = isLinuxMachine;
    }

    public void setGcLogFile(String gcLogFile) {
        this.gcLogFile = gcLogFile;
    }

}
