/**
 * 
 */
package com.ca.apm.systemtest.fld.testbed.smoke;

import java.util.Arrays;
import java.util.Collections;

import com.ca.apm.automation.action.core.IAutomationFlow;
import com.ca.apm.automation.action.flow.AutowireCapable;
import com.ca.apm.automation.action.flow.AutowireCapableContext;
import com.ca.apm.automation.action.flow.commandline.RunCommandFlowContext;
import com.ca.apm.automation.action.flow.utility.FileModifierFlowContext;
import com.ca.apm.systemtest.fld.artifact.MemoryMonitorWebappArtifact;
import com.ca.apm.systemtest.fld.role.MemoryMonitorRole;
import com.ca.apm.systemtest.fld.testbed.FLDConstants;
import com.ca.apm.systemtest.fld.testbed.FLDLoadConstants;
import com.ca.apm.systemtest.fld.testbed.MemoryMonitorTestbedProvider;
import com.ca.tas.artifact.ITasArtifact;
import com.ca.tas.artifact.thirdParty.TomcatVersion;
import com.ca.tas.resolver.ITasResolver;
import com.ca.tas.role.utility.UtilityRole;
import com.ca.tas.role.web.WebAppRole;
import com.ca.tas.role.webapp.TomcatRole;
import com.ca.tas.testbed.Bitness;
import com.ca.tas.testbed.ITestbed;
import com.ca.tas.testbed.ITestbedFactory;
import com.ca.tas.testbed.ITestbedMachine;
import com.ca.tas.testbed.Testbed;
import com.ca.tas.testbed.TestbedMachine;
import com.ca.tas.tests.annotations.TestBedDefinition;
import com.ca.tas.type.Platform;

/**
 * @author keyja01
 *
 */
@TestBedDefinition
public class MemoryMonitorSmokeTestbed implements ITestbedFactory, FLDLoadConstants, FLDConstants {

    public static final String MMSMOKEROLE = "mmsmokerole";

    @Override
    public ITestbed create(ITasResolver tasResolver) {
        Testbed testbed = new Testbed("mmSmokeTestbed");

        TestbedMachine mmMachine= new TestbedMachine.Builder(MEMORY_MONITOR_WEBAPP_MACHINE_ID)
            .platform(Platform.WINDOWS).templateId(ITestbedMachine.TEMPLATE_W64)
            .bitness(Bitness.b64).build();
        
        ITasArtifact mmWebApp = new MemoryMonitorWebappArtifact(tasResolver).createArtifact();
        WebAppRole<TomcatRole> memoryMonitorWebappRole = 
            new WebAppRole.Builder<TomcatRole>(MEMORY_MONITOR_WEBAPP_ROLE_ID)
                .artifact(mmWebApp).cargoDeploy()
                .contextName(MemoryMonitorTestbedProvider.MEMORY_MONITOR_WEBAPP_CONTEXT_ROOT).build();

        TomcatRole tomcatRole =
            new TomcatRole.Builder(MEMORY_MONITOR_WEBAPP_TOMCAT_ROLE_ID, tasResolver)
                .additionalVMOptions(
                    Arrays.asList("-Xms256m", "-Xmx512m", "-XX:PermSize=256m",
                        "-XX:MaxPermSize=512m", "-server", "-Xloggc:c:\\gclog.txt", "-verbose:gc"))
                .tomcatVersion(TomcatVersion.v80)
                .webApp(memoryMonitorWebappRole).autoStart().build();
        RunCommandFlowContext stop = tomcatRole.getStopCmdFlowContext();
        RunCommandFlowContext start = tomcatRole.getStartCmdFlowContext();
        tomcatRole.before(memoryMonitorWebappRole);
        
        UtilityRole<RunCommandFlowContext> stopTomcat = UtilityRole.commandFlow("stopTomcat", stop);
        UtilityRole<RunCommandFlowContext> startTomcat = UtilityRole.commandFlow("startTomcat", start);
        String confFile = tomcatRole.getInstallDir() + "\\bin\\setEnv.bat";
        FileModifierFlowContext configureTomcatFlowContext = new FileModifierFlowContext.Builder()
            .append(confFile, Collections.singletonList("set \"CATALINA_OPTS=-verbose:gc -Xloggc:c:\\gclog.txt"))
            .build();
        UtilityRole<AutowireCapableContext<IAutomationFlow>> confTomcat = UtilityRole.flow("confTomcatGcLog", configureTomcatFlowContext);
        
        stopTomcat.after(memoryMonitorWebappRole);
        confTomcat.after(stopTomcat);
        startTomcat.after(confTomcat);
        

        String host = tasResolver.getHostnameById(MEMORY_MONITOR_WEBAPP_ROLE_ID);
        MemoryMonitorRole role = new MemoryMonitorRole.Builder(MMSMOKEROLE, tasResolver)
            .gcLogFile("c:\\gclog.txt")
            .chartWidth(1622)
            .chartHeight(968)
            .memoryMonitorGroup("DEMO")
            .memoryMonitorRoleName("tomcat")
            .memoryMonitorWebappHost(host)
            .memoryMonitorWebappPort(MemoryMonitorTestbedProvider.MEMORY_MONITOR_WEBAPP_PORT)
            .memoryMonitorWebappContextRoot(MemoryMonitorTestbedProvider.MEMORY_MONITOR_WEBAPP_CONTEXT_ROOT)
            .build();
        
        mmMachine.addRole(role, tomcatRole, memoryMonitorWebappRole, stopTomcat, startTomcat, confTomcat);
        
        testbed.addMachine(mmMachine);
        
        return testbed;
    }

}
