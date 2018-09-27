/**
 * 
 */
package com.ca.apm.systemtest.fld.testbed.loads;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

import org.apache.commons.io.FilenameUtils;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.jetbrains.annotations.NotNull;

import com.ca.apm.automation.action.flow.commandline.RunCommandFlowContext;
import com.ca.apm.systemtest.fld.role.ClientDeployRole;
import com.ca.apm.systemtest.fld.role.WLSAgentAppDeployRole;
import com.ca.apm.systemtest.fld.testbed.FLDConstants;
import com.ca.apm.systemtest.fld.testbed.FLDLoadConstants;
import com.ca.apm.systemtest.fld.testbed.FldTestbedProvider;
import com.ca.apm.systemtest.fld.testbed.regional.FLDConfiguration;
import com.ca.apm.systemtest.fld.testbed.regional.FLDConfigurationService;
import com.ca.tas.artifact.thirdParty.JavaBinary;
import com.ca.tas.builder.TasBuilder;
import com.ca.tas.resolver.ITasResolver;
import com.ca.tas.role.utility.GenericRole;
import com.ca.tas.role.webapp.JavaRole;
import com.ca.tas.testbed.ITestbed;
import com.ca.tas.testbed.ITestbedMachine;
import com.ca.tas.testbed.TestbedMachine;

/**
 * Deploys two WebLogicServer instances for the Cross Cluster load.  One WLS agent is connected to 
 * the AGC, and the other to the 2nd cluster's MOM.  The cross cluster load client is deployed to 
 * the first WLS instance to drive the load.
 * 
 * @author banra06
 * @author KEYJA01
 *
 */
public class FLDWebLogicCrossClusterProvider implements FldTestbedProvider, FLDLoadConstants, 
        FLDConstants {
    
    private JavaRole javaRole;
    protected static final String DEPLOY_BASE = TasBuilder.WIN_SOFTWARE_LOC;
    protected static final String WLS12C_INSTALL_HOME = DEPLOY_BASE
            + "Oracle/Middleware12.1.3";
    protected final String PIPEORGAN_LOGGING_LEVEL = "quiet"; // [debug, verbose, quiet, nologging]

    private ITestbedMachine wls03Machine;
    private ITestbedMachine wls04Machine;
    
    private FLDConfiguration fldConfig = FLDConfigurationService.getConfig();
    
    @Override
    public Collection<ITestbedMachine> initMachines() {
        wls03Machine = new TestbedMachine.Builder(WLS03_MACHINE_ID)
            .templateId(ITestbedMachine.TEMPLATE_W64).build();
        wls04Machine = new TestbedMachine.Builder(WLS04_MACHINE_ID)
            .templateId(ITestbedMachine.TEMPLATE_W64).build();
        return Arrays.asList(wls03Machine, wls04Machine);
    }

    /* (non-Javadoc)
     * @see com.ca.apm.systemtest.fld.testbed.FldTestbedProvider#initTestbed(com.ca.tas.testbed.Testbed, com.ca.tas.resolver.ITasResolver)
     */
    @Override
    public void initTestbed(ITestbed testbed, ITasResolver tasResolver) {
        initMachine01(tasResolver);
        initMachine02(tasResolver);
    }

    private void initMachine01(ITasResolver tasResolver) {
        ClientDeployRole role = new ClientDeployRole.Builder(WLS03_CLIENT_ROLE_ID,
            tasResolver).jvmVersion("8").shouldDeployConsoleApps(true)
            .shouldDeployJassApps(false)
            .fldXjvmhost(tasResolver.getHostnameById(WLS04_ROLE_ID))
            .pipeorganLoggingLevel(PIPEORGAN_LOGGING_LEVEL)
            .build();
        wls03Machine.addRole(role);

        String agcMom = tasResolver.getHostnameById(AGC_ROLE_ID);
        addWeblogicRoles(WLS03_ROLE_ID, wls03Machine, tasResolver, fldConfig.getEmVersion(), agcMom);
    }

    private void initMachine02(ITasResolver tasResolver) {
        String mom02 = tasResolver.getHostnameById(EM_MOM2_ROLE_ID);
        addWeblogicRoles(WLS04_ROLE_ID, wls04Machine, tasResolver, fldConfig.getEmVersion(), mom02);
    }

    private void addWeblogicRoles(String wlsRoleId, ITestbedMachine machine,
            ITasResolver tasResolver, String emHost) {
        addWeblogicRoles(wlsRoleId, machine, tasResolver,
                tasResolver.getDefaultVersion(), emHost);
    }

    private void addWeblogicRoles(String wlsRoleId, ITestbedMachine machine,
            ITasResolver tasResolver, String agentVersion, String emHost) {
        // install wls
        javaRole = new JavaRole.Builder(machine.getMachineId() + "_"
                + "java8Role", tasResolver).version(
                JavaBinary.WINDOWS_64BIT_JDK_18_0_51).build();

        GenericRole wlsRole = getwls12cRole(tasResolver, wlsRoleId);

        WLSAgentAppDeployRole wlsAgentPORole = new WLSAgentAppDeployRole.Builder(
                machine.getMachineId() + "_" + wlsRoleId, tasResolver)
                .agentVersion(agentVersion).classifier("jvm7-genericnodb")
                .isLegacyMode(false).isJassEnabled(false)
                .javaRole(javaRole).serverPort("7001").wlsRole(wlsRoleId)
                .emHost(emHost)
                .pipeorganLoggingLevel(PIPEORGAN_LOGGING_LEVEL)
                .redirectStdoutToServerLog()
                .build();

        javaRole.before(wlsRole);
        wlsRole.before(wlsAgentPORole);
        machine.addRole(javaRole, wlsRole, wlsAgentPORole);
    }

    @NotNull
    private GenericRole getwls12cRole(ITasResolver tasResolver, String wlsRole) {
        ArrayList<String> args = new ArrayList<String>();
        args.add("-silent");

        RunCommandFlowContext installWlc12cCommand = new RunCommandFlowContext.Builder(
                "configure.cmd").workDir(WLS12C_INSTALL_HOME).args(args)
                .build();

        GenericRole wls12cInstallRole = new GenericRole.Builder(wlsRole,
                tasResolver)
                .unpack(new DefaultArtifact("com.ca.apm.binaries", "weblogic",
                        "dev", "zip", "12.1.3"),
                        codifyPath(WLS12C_INSTALL_HOME))
                .runCommand(installWlc12cCommand).build();

        return wls12cInstallRole;
    }

    @NotNull
    protected String codifyPath(String path) {
        return FilenameUtils.separatorsToUnix(path);
    }

}
