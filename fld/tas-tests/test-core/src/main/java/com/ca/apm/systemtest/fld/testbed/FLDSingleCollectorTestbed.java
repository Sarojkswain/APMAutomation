/**
 * 
 */
package com.ca.apm.systemtest.fld.testbed;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import com.ca.apm.automation.action.flow.em.DeployEMFlowContext.EmRoleEnum;
import com.ca.tas.resolver.ITasResolver;
import com.ca.tas.role.EmRole;
import com.ca.tas.role.EmRole.Builder;
import com.ca.tas.role.TIMRole;
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
@TestBedDefinition()
public class FLDSingleCollectorTestbed implements ITestbedFactory {
    public static final int DB_PORT = 5432;
    public static final String DB_ADMIN_USER = "pgadmin";
    public static final String DB_ADMIN_PASSWORD = "password";
    public static final String DB_USER = "postgres";
    public static final String DB_PASSWORD = "password";
    public static final String DB_NAME = "cemdb";

    /* (non-Javadoc)
     * @see com.ca.tas.testbed.ITestbedFactory#create(com.ca.tas.resolver.ITasResolver)
     */
    @Override
    public ITestbed create(ITasResolver tasResolver) {
        Testbed testbed = new Testbed("FLDLoadTestbed");
        
        TestbedMachine tim = new TestbedMachine.LinuxBuilder("tim").templateId(ITestbedMachine.TEMPLATE_CO65).build();
        
        TIMRole timRole = new TIMRole.Builder("timRole", tasResolver)
            .timVersion("10.3.0.13")
            .build();
        tim.addRole(timRole);
        
        
        TestbedMachine collMachine = em(FLDMainClusterTestbed.COLL01_MACHINE_ID);
        List<EmRole> collectors = new ArrayList<>();
        collectors.add(emRole(FLDMainClusterTestbed.EM_COLL01_ROLE_ID, tasResolver));
        collMachine.addRoles(collectors);
        
        TestbedMachine mom = em(FLDMainClusterTestbed.MOM_MACHINE_ID);
        EmRole momRole = momRole(FLDMainClusterTestbed.EM_MOM_ROLE_ID, tasResolver, timRole, collectors);
        mom.addRole(momRole);
        
        
//        new FldControllerLoadProvider().initTestbed(testbed, tasResolver);
//        new FLDJbossLoadProvider().initTestbed(testbed, tasResolver);
        testbed.addMachine(mom, tim, collMachine);
        return testbed;
    }


    private TestbedMachine em(String machineId) {
        TestbedMachine em = new TestbedMachine.Builder(machineId)
            .platform(Platform.WINDOWS)
            .templateId("w64")
            .bitness(Bitness.b64)
            .build();
        
//        em = new TestbedMachine.LinuxBuilder(machineId)
//            .platform(Platform.CENTOS)
//            .templateId("co65")
//            .build();
        return em;
    }

    
    private EmRole momRole(String machineId, ITasResolver tasResolver, TIMRole timRole, List<EmRole> collectors) {
        Collection<String> features = new HashSet<>(Arrays.asList("Enterprise Manager", "WebView", "Database"));
        
        Builder builder = new EmRole.Builder(machineId, tasResolver)
            .version("10.3.0.13")
            .emClusterRole(EmRoleEnum.MANAGER);
        
        if (collectors != null) {
            for (EmRole em: collectors) {
                builder.emCollector(em);
            }
        }
        
//        Builder builder = new EmRole.LinuxBuilder(machineId, tasResolver)
//            .version("10.3.0.13")
//            .emClusterRole(EmRoleEnum.COLLECTOR);
   
        String momHostname = tasResolver.getHostnameById(FLDMainClusterTestbed.EM_MOM_ROLE_ID);
        if (momHostname != null) {
            builder.dbhost(momHostname)
                .silentInstallChosenFeatures(features)
//                .dbuser(DB_USER)
//                .dbpassword(DB_PASSWORD)
                .dbname(DB_NAME)
                .dbport(DB_PORT);
        }
        
        if (timRole != null) {
            builder.tim(timRole);
        }
        
        EmRole em = builder.build();
        return em;
    }

    
    private EmRole emRole(String machineId, ITasResolver tasResolver) {
        Collection<String> features = new HashSet<>(Arrays.asList("Enterprise Manager"));
        
        Builder builder = new EmRole.Builder(machineId, tasResolver)
            .version("10.3.0.13")
            .emClusterRole(EmRoleEnum.COLLECTOR);
        
        String momHostname = tasResolver.getHostnameById(FLDMainClusterTestbed.EM_MOM_ROLE_ID);
        if (momHostname != null) {
            builder.dbhost(momHostname)
                .silentInstallChosenFeatures(features)
//                .dbuser(DB_USER)
//                .dbpassword(DB_PASSWORD)
                .dbname(DB_NAME)
                .dbport(DB_PORT);
        }
        
        EmRole em = builder.build();
        return em;
    }
}
