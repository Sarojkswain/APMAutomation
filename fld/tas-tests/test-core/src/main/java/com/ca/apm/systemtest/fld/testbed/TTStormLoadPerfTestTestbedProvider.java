package com.ca.apm.systemtest.fld.testbed;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import com.ca.apm.systemtest.fld.artifact.thirdparty.HammondDataVersion;
import com.ca.tas.resolver.ITasResolver;
import com.ca.tas.role.EmRole;
import com.ca.tas.role.HammondRole;
import com.ca.tas.testbed.Bitness;
import com.ca.tas.testbed.ITestbed;
import com.ca.tas.testbed.ITestbedMachine;
import com.ca.tas.testbed.TestbedMachine;
import com.ca.tas.type.Platform;

/**
 * Provider of the testbed used for performance testing under transaction trace storm load previously generated 
 * and replicated by Hammond.
 * 
 * @author Alexander Sinyushkin (sinal04@ca.com)
 *
 */
public class TTStormLoadPerfTestTestbedProvider implements FLDConstants, FLDLoadConstants, FldTestbedProvider {
    public static final String EM_MACHINE_ID = "EM_MACHINE_ID";
    public static final String EXEC_MACHINE_ID = "EXEC_MACHINE_ID";
    public static final String HAMMOND_MACHINE_ID_TEMPLATE = "HAMMOND%02d_MACHINE_ID";
    public static final String HAMMOND_ROLE_ID_TEMPLATE = "hammond%02d_role";
    public static final String EM_ROLE = "em_role";
    public static final String START_WV_ROLE = "start_wv_role";
    public static final String START_EM_ROLE = "start_em_role";

    public static final String HAMMOND_INSTALL_PATH = "C:/hammond";
    public static final String DB_USER = "admin";
    public static final String DB_PASSWORD = "quality";
    public static final String DB_ADMIN_USER = "postgres";
    public static final String DB_ADMIN_PASSWORD = "Password1";
    
    public static final int NUM_OF_HAMMONDS = 1;
    
    @Override
    public Collection<ITestbedMachine> initMachines() {
        List<ITestbedMachine> machines = new ArrayList<>(NUM_OF_HAMMONDS);
        
        TestbedMachine emMachine = new TestbedMachine.Builder(EM_MACHINE_ID)
            .platform(Platform.WINDOWS)
            .bitness(Bitness.b64)
            .templateId("w64_16gb")
            .build();
    
        machines.add(emMachine);

        for (int i = 1; i <= NUM_OF_HAMMONDS; i++) {
            String machineId = String.format(HAMMOND_MACHINE_ID_TEMPLATE, i);
            TestbedMachine hammondMachine = new TestbedMachine.Builder(machineId)
                .platform(Platform.WINDOWS)
                .bitness(Bitness.b64)
                .templateId("w64")
                .build();
            machines.add(hammondMachine);
        }

        TestbedMachine execMachine = new TestbedMachine.Builder(EXEC_MACHINE_ID)
            .platform(Platform.WINDOWS)
            .bitness(Bitness.b64)
            .templateId("w64")
            .build();
        
        machines.add(execMachine);
        
        return machines;
    }

    @Override
    public void initTestbed(ITestbed testbed, ITasResolver tasResolver) {
        Collection<String> laxOptions =
            Arrays.asList("-Xms4096m", "-XX:+UseConcMarkSweepGC", "-showversion", " -verbosegc",
                "-Dcom.wily.assert=false", "-Xmx8192m", "-Dmail.mime.charset=UTF-8",
                "-Dorg.owasp.esapi.resources=./config/esapi", "-XX:+UseParNewGC",
                "-XX:CMSInitiatingOccupancyFraction=50", "-XX:+HeapDumpOnOutOfMemoryError",
                "-Xss256k");

        String emHost = tasResolver.getHostnameById(EM_ROLE);

        EmRole emRole =
            new EmRole.Builder(EM_ROLE, tasResolver)
                .silentInstallChosenFeatures(Arrays.asList("Database", "Enterprise Manager", "WebView"))
                .dbAdminUser(DB_ADMIN_USER)
                .dbAdminPassword(DB_ADMIN_PASSWORD)
                .dbuser(DB_USER)
                .dbpassword(DB_PASSWORD)
                .emLaxNlClearJavaOption(laxOptions)
                .wvEmHost(emHost)
                .wvEmPort(5001)
                .wvPort(8080)
                .build();

        ITestbedMachine emMachine = testbed.getMachineById(EM_MACHINE_ID);
        
        emMachine.addRole(emRole);

        for (int i = 1; i <= NUM_OF_HAMMONDS; i++) {
            String machineId = String.format(HAMMOND_MACHINE_ID_TEMPLATE, i);
            ITestbedMachine machine = testbed.getMachineById(machineId);
            String hammondRoleId = String.format(HAMMOND_ROLE_ID_TEMPLATE, i);
            
            HammondRole hammondRole = new HammondRole.Builder(hammondRoleId, tasResolver)
                .installDir(HAMMOND_INSTALL_PATH)
                .collector(tasResolver.getHostnameById(TTStormLoadRecordingTestbedProvider.EM_ROLE))
                .heapMemory("1024m")
                .data(HammondDataVersion.TransactionTraceStormLoad)
                .scale(5)
                .build();
            
            machine.addRole(hammondRole);
        }
    }

}
