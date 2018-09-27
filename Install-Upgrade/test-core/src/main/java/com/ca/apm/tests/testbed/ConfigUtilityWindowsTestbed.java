package com.ca.apm.tests.testbed;

import java.util.Arrays;
import java.util.List;

import static com.ca.tas.testbed.ITestbedMachine.TEMPLATE_W64;

import com.ca.apm.automation.action.flow.agent.AgentInstrumentationLevel;
import com.ca.tas.artifact.thirdParty.TomcatVersion;
import com.ca.tas.builder.TasBuilder;
import com.ca.tas.resolver.ITasResolver;
import com.ca.tas.role.AgentRole;
import com.ca.tas.role.EmRole;
import com.ca.tas.role.IRole;
import com.ca.tas.role.WorkstationRole;
import com.ca.tas.role.webapp.TomcatRole;
import com.ca.tas.testbed.ITestbed;
import com.ca.tas.testbed.ITestbedFactory;
import com.ca.tas.testbed.TestBedUtils;
import com.ca.tas.testbed.Testbed;
import com.ca.tas.testbed.TestbedMachine;
import com.ca.tas.tests.annotations.TestBedDefinition;

@TestBedDefinition
public class ConfigUtilityWindowsTestbed implements ITestbedFactory
{

    public static final String MACHINE_ID = "testMachine";
    protected static String TEMPLATE_ID = TEMPLATE_W64;

    public static final String EM_ROLE_ID1 = "emRole1"; 
    public static final String EM_ROLE_ID2 = "emRole2";
    public static final String EMONLY_ROLE_ID = "emOnlyRole";
    public static final String WV_ROLE_ID = "wvOnlyRole";
    public static final String ACC_ROLE_ID = "accOnlyRole";
    public static final String WS_ROLE_ID = "workstationRole";

    //Install locations for Various installation instances
    public static final String InstLoc_EMFULL = TasBuilder.WIN_SOFTWARE_LOC+"EMFULL";
    public static final String InstLoc_EMFULL_old = TasBuilder.WIN_SOFTWARE_LOC+"EMFULL_OLD";
    public static final String InstLoc_EM = TasBuilder.WIN_SOFTWARE_LOC+"EM_ONLY";
    public static final String InstLoc_WV = TasBuilder.WIN_SOFTWARE_LOC+"WV_ONLY";
    public static final String InstLoc_ACC = TasBuilder.WIN_SOFTWARE_LOC+"ACC";
    public static final String InstLoc_WS = TasBuilder.WIN_SOFTWARE_LOC+"WS_ONLY";
//    public static final String InstLoc_ACC = "C:\Program Files\CA APM\CommandCenterServer";
    public static final String OLD_EM_VERSION = "10.3.0.23";

    @Override
    public ITestbed create(ITasResolver tasResolver) {
        
        //Install all components
        EmRole emFull = new EmRole.Builder(EM_ROLE_ID1, tasResolver).installDir(InstLoc_EMFULL).installerProperty("CCS_INSTALL_DIR", InstLoc_ACC).silentInstallChosenFeatures(Arrays.asList("Enterprise Manager","Database","WebView","Command Center Server")).nostartEM().nostartWV().build();

        //Install another version of EM
        EmRole emFull_old = new EmRole.Builder(EM_ROLE_ID2, tasResolver).installDir(InstLoc_EMFULL_old).instroscopeVersion(OLD_EM_VERSION).silentInstallChosenFeatures(Arrays.asList("Enterprise Manager","WebView")).nostartEM().nostartWV().build();

        //Install Workstation
        WorkstationRole wsRole = new WorkstationRole.Builder(WS_ROLE_ID, tasResolver).deployPath(InstLoc_WS).build();
        
        //Install EM only
        EmRole emOnly = new EmRole.Builder(EMONLY_ROLE_ID, tasResolver).installDir(InstLoc_EM).silentInstallChosenFeatures(Arrays.asList("Enterprise Manager")).nostartEM().build();

        //Install Webview only
        EmRole webviewOnly = new EmRole.Builder(WV_ROLE_ID, tasResolver).installDir(InstLoc_WV).silentInstallChosenFeatures(Arrays.asList("WebView")).nostartWV().build();


        // Configuration of test machine
        TestbedMachine testMachine =
                TestBedUtils.createWindowsMachine(MACHINE_ID, TEMPLATE_ID, emFull_old, emFull, wsRole, emOnly, webviewOnly);

        return Testbed.create(this, testMachine);


    }
}
