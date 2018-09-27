package com.ca.apm.tests.testbed;

import static com.ca.tas.testbed.ITestbedMachine.TEMPLATE_W64;

import java.util.Arrays;
import java.util.Collection;

import com.ca.apm.automation.action.flow.commandline.RunCommandFlowContext;
import com.ca.apm.automation.action.flow.em.DeployEMFlowContext;
import com.ca.apm.automation.action.flow.utility.FileModifierFlow;
import com.ca.apm.automation.action.flow.utility.FileModifierFlowContext;
import com.ca.tas.resolver.ITasResolver;
import com.ca.tas.role.EmRole;
import com.ca.tas.role.IRole;
import com.ca.tas.role.utility.ExecutionRole;
import com.ca.tas.testbed.Bitness;
import com.ca.tas.testbed.ITestbed;
import com.ca.tas.testbed.ITestbedFactory;
import com.ca.tas.testbed.ITestbedMachine;
import com.ca.tas.testbed.TestBedUtils;
import com.ca.tas.testbed.Testbed;
import com.ca.tas.testbed.TestbedMachine;
import com.ca.tas.tests.annotations.TestBedDefinition;
import com.ca.tas.type.Platform;

@TestBedDefinition
public class CustomerDataReplayTestbed implements ITestbedFactory {

    // Standalone EM machine

    public static final String EM_MACHINE_ID = "emMachine";
    public static final String EM_ROLE_ID = "emRole";
    private static final String EM_MACHINE_TEMPLATE_ID = TEMPLATE_W64;
    public static String EM_INSTALL_DIR = "C:\\sw\\em";
    public static final int NUMBER_OF_CLUSTERS = 5;
    // AGC machine
    public static final String AGC_MACHINE_ID = "agcMachine";
    public static String AGC_ROLE_ID = "agc_em";

    public static final String ADMIN_AUX_TOKEN_HASHED = "8f400c257611ed5d30c0e6607ac61074307dfa24cf70a8e92c3e8147d67d2c70";
    public static final String ADMIN_AUX_TOKEN = "f47ac10b-58cc-4372-a567-0e02b2c3d479";

    private static final String SCRIPT_LOCATION = "scriptLocation";
    private static final String BAT_FILE = "C:\\temp\\add_token.bat";

    public ITestbed create(ITasResolver tasResolver) {

        // Customer Data Replay Testbed
        ITestbed atTestbed = new Testbed("CustomerDataReplayTestbed");

        for (int i = 1; i <= NUMBER_OF_CLUSTERS; i++) {
            ITestbedMachine agcMachine = new TestbedMachine.Builder(AGC_MACHINE_ID + i + "").platform(Platform.WINDOWS).templateId("w64").bitness(Bitness.b64)
                    .automationBaseDir("C:/sw").build();
            EmRole emRole = new EmRole.Builder(EM_ROLE_ID + "" + i, tasResolver).installDir(EM_INSTALL_DIR).dbpassword("quality").nostartWV().nostartEM().build();
            AGC_ROLE_ID = "agc_em" + i + "";
            ITestbedMachine standaloneMachine = TestBedUtils.createWindowsMachine(EM_MACHINE_ID + i + "", EM_MACHINE_TEMPLATE_ID, emRole);
            IRole followerTokenRole = addAuxTokenRole(standaloneMachine, emRole);
            addStartEmRole(standaloneMachine, emRole, false, followerTokenRole);

            atTestbed.addMachine(standaloneMachine);

            ITestbedMachine followers[] = { standaloneMachine };
            EmRole agcRole = addMomRoles(agcMachine, null, followers, tasResolver);
            IRole agcTokenRole = addAuxTokenRole(agcMachine, agcRole);
            addStartEmRole(agcMachine, agcRole, true, agcTokenRole);

            atTestbed.addMachine(agcMachine);
        }
        return atTestbed;
    }

    @SuppressWarnings("deprecation")
    public static void addStartEmRole(ITestbedMachine machine, EmRole emRole, boolean startWv, IRole beforeRole) {
        // starts EM and WebView
        ExecutionRole.Builder builder = new ExecutionRole.Builder(emRole.getRoleId() + "_start").command(emRole.getEmRunCommandFlowContext());
        if (startWv) {
            builder.command(emRole.getWvRunCommandFlowContext());
        }
        ExecutionRole startRole = builder.build();
        startRole.after(beforeRole);
        machine.addRole(startRole);
    }

    // Adding MOM role to AGC machine
    private EmRole addMomRoles(ITestbedMachine machine, EmRole remoteCollectorRole, ITestbedMachine followers[], ITasResolver tasResolver) {
        EmRole emMomRole; // used for installing MM
        EmRole.Builder emBuilder;

        emBuilder = new EmRole.Builder(AGC_ROLE_ID, tasResolver).dbpassword("quality").emPort(5003).wvEmPort(5003)
                .emClusterRole(DeployEMFlowContext.EmRoleEnum.MANAGER)
                .silentInstallChosenFeatures(Arrays.asList("Enterprise Manager", "WebView", "ProbeBuilder", "Database")).nostartEM().nostartWV();
        if (remoteCollectorRole != null) {
            emBuilder.emCollector(remoteCollectorRole);
        }
        if (followers != null) {
            emBuilder.configProperty("introscope.apmserver.agc", "true");
            emBuilder.configProperty("introscope.apmserver.teamcenter.master", "true");

        }

        emMomRole = emBuilder.build();
        machine.addRole(emMomRole);
        return emMomRole;
    }

    @SuppressWarnings("deprecation")
    public IRole addAuxTokenRole(ITestbedMachine machine, IRole beforeRole) {
        // creates AGC token in the DB
        Collection<String> data = Arrays.asList("set PGPASSWORD=Lister@123", "C:\\automation\\deployed\\database\\bin\\psql --username=postgres --dbname=cemdb"
                + " --command=\"INSERT INTO appmap_api_keys(id, username, date_created, hashed_token, description) "
                + "VALUES (NEXTVAL('seq_appmap_api_key_id'), 'Admin', NOW(), " + "'" + ADMIN_AUX_TOKEN_HASHED + "', "
                + "'{\\\"system\\\":false,\\\"description\\\":\\\"TAS aux token\\\"}')\"");

        FileModifierFlowContext createFileFlow = new FileModifierFlowContext.Builder().create(BAT_FILE, data).build();
        ExecutionRole execRole = new ExecutionRole.Builder(machine.getMachineId() + "_token").flow(FileModifierFlow.class, createFileFlow)
                .command(new RunCommandFlowContext.Builder(BAT_FILE).build()).build();
        execRole.addProperty(SCRIPT_LOCATION, BAT_FILE);
        execRole.after(beforeRole);
        machine.addRole(execRole);
        return execRole;
    }

}
