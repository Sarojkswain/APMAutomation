package com.ca.apm.systemtest.fld.testbed;

import java.util.Arrays;
import java.util.Collection;
import java.util.TreeSet;

import org.apache.http.util.Args;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ca.apm.systemtest.fld.artifact.NetworkTrafficMonitorWebappArtifact;
import com.ca.apm.systemtest.fld.role.NetworkTrafficMonitorRole;
import com.ca.tas.artifact.ITasArtifact;
import com.ca.tas.artifact.thirdParty.TomcatVersion;
import com.ca.tas.resolver.ITasResolver;
import com.ca.tas.role.IRole;
import com.ca.tas.role.linux.YumInstallPackageRole;
import com.ca.tas.role.web.WebAppRole;
import com.ca.tas.role.webapp.TomcatRole;
import com.ca.tas.testbed.Bitness;
import com.ca.tas.testbed.ITestbed;
import com.ca.tas.testbed.ITestbedMachine;
import com.ca.tas.testbed.TestbedMachine;
import com.ca.tas.type.Platform;

public class NetworkTrafficMonitorTestbedProvider
    implements
        FldTestbedProvider,
        FLDLoadConstants,
        FLDConstants {

    private static final Logger LOGGER = LoggerFactory
        .getLogger(NetworkTrafficMonitorTestbedProvider.class);

    public static final TomcatVersion TOMCAT_VERSION = TomcatVersion.v80;

    public static final String TCPDUMP_PACKAGE_NAME = "tcpdump";

    public static final String NETWORK_TRAFFIC_MONITOR_WEBAPP_CONTEXT_ROOT =
        "network-traffic-monitor";
    public static final int NETWORK_TRAFFIC_MONITOR_WEBAPP_PORT = 8080;

    private String[] networkTrafficMonitorMachineIds;
    private ITestbedMachine webappMachine;

    public NetworkTrafficMonitorTestbedProvider(String[] networkTrafficMonitorMachineIds) {
        Args.check(networkTrafficMonitorMachineIds != null
            && networkTrafficMonitorMachineIds.length > 0,
            "networkTrafficMonitorMachineIds is null or empty");
        this.networkTrafficMonitorMachineIds =
            (new TreeSet<>(Arrays.asList(networkTrafficMonitorMachineIds))).toArray(new String[0]);
    }

    @Override
    public Collection<ITestbedMachine> initMachines() {
        webappMachine =
            new TestbedMachine.Builder(NETWORK_TRAFFIC_MONITOR_WEBAPP_MACHINE_ID)
                .platform(Platform.WINDOWS).templateId(ITestbedMachine.TEMPLATE_W64)
                .bitness(Bitness.b64).build();
        return Arrays.asList(webappMachine);
    }

    @Override
    public void initTestbed(ITestbed testbed, ITasResolver tasResolver) {
        ITasArtifact ntmWebApp =
            new NetworkTrafficMonitorWebappArtifact(tasResolver).createArtifact();
        WebAppRole<TomcatRole> networkTrafficMonitorWebappRole =
            new WebAppRole.Builder<TomcatRole>(NETWORK_TRAFFIC_MONITOR_WEBAPP_ROLE_ID)
                .artifact(ntmWebApp).cargoDeploy()
                .contextName(NETWORK_TRAFFIC_MONITOR_WEBAPP_CONTEXT_ROOT).build();

        TomcatRole tomcatRole =
            new TomcatRole.Builder(NETWORK_TRAFFIC_MONITOR_WEBAPP_TOMCAT_ROLE_ID, tasResolver)
                .additionalVMOptions(
                    Arrays.asList("-Xms256m", "-Xmx512m", "-XX:PermSize=256m",
                        "-XX:MaxPermSize=512m", "-server")).tomcatVersion(TOMCAT_VERSION)
                .webApp(networkTrafficMonitorWebappRole).autoStart().build();
        tomcatRole.before(networkTrafficMonitorWebappRole);

        webappMachine.addRole(networkTrafficMonitorWebappRole, tomcatRole);

        String webappHost =
            tasResolver.getHostnameById(NETWORK_TRAFFIC_MONITOR_WEBAPP_TOMCAT_ROLE_ID); // webappMachine.getHostname();
        LOGGER
            .info(
                "NetworkTrafficMonitorTestbedProvider.initTestbed():: NetworkTrafficMonitor summary page URL: http://{}:{}/{}/api/networktrafficmonitor/summary",
                webappHost, NETWORK_TRAFFIC_MONITOR_WEBAPP_PORT,
                NETWORK_TRAFFIC_MONITOR_WEBAPP_CONTEXT_ROOT);

        // EM machines
        for (String machineId : networkTrafficMonitorMachineIds) {
            ITestbedMachine networkTrafficMonitorMachine = testbed.getMachineById(machineId);

            // tcpdump package installation role
            String tcpdumpYumInstallRoleId = "tcpdumpYumInstallRole_" + machineId;
            IRole tcpdumpYumInstallRole =
                new YumInstallPackageRole.Builder(tcpdumpYumInstallRoleId).addPackage(
                    TCPDUMP_PACKAGE_NAME).build();
            networkTrafficMonitorMachine.addRole(tcpdumpYumInstallRole);

            // network traffic monitoring
            String networkTrafficMonitorRoleId = "networkTrafficMonitorRole_" + machineId;
            NetworkTrafficMonitorRole networkTrafficMonitorRole =
                (new NetworkTrafficMonitorRole.Builder(networkTrafficMonitorRoleId, tasResolver))
                    .networkTrafficMonitorWebappHost(webappHost)
                    .networkTrafficMonitorWebappPort(NETWORK_TRAFFIC_MONITOR_WEBAPP_PORT)
                    .networkTrafficMonitorWebappContextRoot(
                        NETWORK_TRAFFIC_MONITOR_WEBAPP_CONTEXT_ROOT).chartWidth(1600)
                    .chartHeight(640).waitInterval(60000L) // 1 minute
                    .build();
            networkTrafficMonitorRole.after(tcpdumpYumInstallRole);
            networkTrafficMonitorMachine.addRole(networkTrafficMonitorRole);
        }
    }

}
