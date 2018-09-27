package com.ca.apm.tests.testbed;

import com.ca.apm.automation.action.flow.commandline.RunCommandFlowContext;
import com.ca.apm.automation.action.flow.em.DeployEMFlowContext;
import com.ca.apm.test.em.util.RoleUtility;
import com.ca.apm.tests.role.AGCRegisterRole;
import com.ca.tas.resolver.ITasResolver;
import com.ca.tas.role.EmRole;
import com.ca.tas.role.IRole;
import com.ca.tas.role.utility.ExecutionRole;
import com.ca.tas.testbed.Bitness;
import com.ca.tas.testbed.ITestbedMachine;
import com.ca.tas.testbed.Testbed;
import com.ca.tas.testbed.TestbedMachine;
import com.ca.tas.type.Platform;

import java.util.Arrays;
import java.util.HashSet;

/**
 * Created by jirji01 on 5/22/2017.
 */
public abstract class UpgradeAgcTestbed extends UpgradeTestbed {

    public static final String AGC_MACHINE_ID = "agcMachine";
    public static final String AGC_ROLE_ID = "agcRole";
    public static final String AGC_MOM_REGISTER_ROLE_ID = "agcRegisterRole";

    public ITestbedMachine agcMachine(String id) {
        TestbedMachine.Builder builder;
        switch (platform()) {
            case LINUX:
                builder = new TestbedMachine.LinuxBuilder(id);
                break;
            case WINDOWS:
                builder = new TestbedMachine.Builder(id);
                break;
            default:
                throw new IllegalStateException("unsupported platform");
        }

        ITestbedMachine machine = builder
                .platform(platform())
                .templateId(template().equals("w64") ? "w64b" : template())
                .bitness(Bitness.b64)
                .build();

        if (platform() == Platform.WINDOWS) {
            addTimeSyncRole(machine);
        }

        return machine;
    }

    @Override
    protected void registerAGC(Testbed testbed, ITasResolver resolver) {
        // AGC
        final ITestbedMachine agcMachine = agcMachine(AGC_MACHINE_ID);
        testbed.addMachine(agcMachine);

        EmRole agcRole = emBuilder(AGC_ROLE_ID, resolver)
                .silentInstallChosenFeatures(Arrays.asList("Enterprise Manager", "ProbeBuilder", "EPA", "Database", "WebView"))
                .emClusterRole(DeployEMFlowContext.EmRoleEnum.MANAGER)
                .nostartEM()
                .nostartWV()
                .version(version())
                .ignoreStopCommandErrors()
                .ignoreUninstallCommandErrors()
                .configProperty("introscope.apmserver.teamcenter.master", "true")
                .emLaxNlClearJavaOption(AGC_LAXNL_JAVA_OPTION)
                .build();
        agcMachine.addRole(agcRole);
        IRole startAgcRole = RoleUtility.addStartEmRole(agcMachine, agcRole, true, agcRole);

        //register MOM to AGC
        String agcHost = resolver.getHostnameById(AGC_ROLE_ID);

        AGCRegisterRole agcRegister = new AGCRegisterRole.Builder(AGC_MOM_REGISTER_ROLE_ID, resolver)
                .agcHostName(agcHost)
                .hostName(resolver.getHostnameById(MOM_ROLE_ID))
                .wvHostName(resolver.getHostnameById(DB_ROLE_ID))
                .startCommandContext(((EmRole)testbed.getRoleById(MOM_ROLE_ID)).getEmRunCommandFlowContext())
                .stopCommandContext(((EmRole)testbed.getRoleById(MOM_ROLE_ID)).getEmStopCommandFlowContext())
                .build();

        agcRegister.after(testbed.getRoleById(MOM_ROLE_ID + "_start"), testbed.getRoleById(DB_ROLE_ID + "_start"), startAgcRole);
        agcMachine.addRole(agcRegister);

        addUpgradeRole(resolver, agcMachine, agcRole);
    }
}
