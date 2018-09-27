package com.ca.apm.saas.testbed;

import static com.ca.tas.testbed.ITestbedMachine.TEMPLATE_RH7;
import static com.ca.tas.testbed.ITestbedMachine.TEMPLATE_W64;

import java.util.ArrayList;
import java.util.Arrays;

import com.ca.apm.automation.action.flow.commandline.RunCommandFlow;
import com.ca.apm.automation.action.flow.commandline.RunCommandFlowContext;
import com.ca.apm.automation.action.flow.utility.FileModifierFlow;
import com.ca.apm.automation.action.flow.utility.FileModifierFlowContext;
import com.ca.tas.resolver.ITasResolver;
import com.ca.tas.role.IRole;
import com.ca.tas.role.utility.ExecutionRole;
import com.ca.tas.role.utility.UniversalRole;
import com.ca.tas.testbed.ITestbedMachine;
import com.ca.tas.testbed.TestbedMachine;
import com.ca.tas.tests.annotations.TestBedDefinition;

/**
 * SAAS testbed for Infrastructure agent (IA)
 * 
 * @author kurma05
 */
@TestBedDefinition
public class SaasIATestbed extends SaasBaseTestbed {

    public static final String MACHINE1_ID = "machine1";
    public static final String MACHINE2_ID = "machine2";
  
    @Override
    protected void initMachines(ITasResolver tasResolver) {
        
        // add machines
        ITestbedMachine machine1 =
            new TestbedMachine.Builder(MACHINE1_ID).templateId(TEMPLATE_W64).build();
        ITestbedMachine machine2 =
            new TestbedMachine.Builder(MACHINE2_ID).templateId(TEMPLATE_RH7).build();
        
        addClientRoles(tasResolver, machine1);

        //required for sysedge/host monitoring
        IRole installGlibcLibRole = installGlibcLib(tasResolver); //for RedHat 6/7
        IRole installPsmiscLibRole = installPsmiscLib(tasResolver); //for RedHat 7
        
        //required for docker monitoring
        IRole createRepoRole = createCityFanRepo(tasResolver);
        IRole installCurlRole = upgradeCurl(tasResolver);
          
        installCurlRole.after(createRepoRole);
        machine2.addRole(installGlibcLibRole, installPsmiscLibRole, 
            createRepoRole, installCurlRole);  
        testbed.addMachine(machine1, machine2);
    }

    private IRole installPsmiscLib(ITasResolver tasResolver) {
      
        RunCommandFlowContext installCurlContext = new RunCommandFlowContext.Builder("yum")
            .args(Arrays.asList("-y", "install", "psmisc"))     
            .build();    
        ExecutionRole installPsmiscLibRole = new ExecutionRole.Builder("installPsmiscLib")
            .flow(RunCommandFlow.class, installCurlContext)
            .build();
    
        return installPsmiscLibRole;
    }

    private IRole installGlibcLib(ITasResolver tasResolver) {

        RunCommandFlowContext installCurlContext = new RunCommandFlowContext.Builder("yum")
            .args(Arrays.asList("-y", "install", "glibc.i686"))     
            .build();        
        ExecutionRole installGlibcLibRole = new ExecutionRole.Builder("installGlibcLibRole")
            .flow(RunCommandFlow.class, installCurlContext)
            .build();
    
        return installGlibcLibRole;
    }

    private IRole upgradeCurl(ITasResolver tasResolver) {

        RunCommandFlowContext installCurlContext = new RunCommandFlowContext.Builder("yum")
            .args(Arrays.asList("-y", "install", "curl"))     
            .build();     
        ExecutionRole installCurlRole =
            new ExecutionRole.Builder("installCurlRole")
            .flow(RunCommandFlow.class, installCurlContext)
            .build();
        
        return installCurlRole;
    }

    private IRole createCityFanRepo(ITasResolver tasResolver) {
        
        ArrayList<String> lines = new ArrayList<String>(); 
        lines.add("[CityFan]");
        lines.add("name=City Fan Repo");
        lines.add("baseurl=http://www.city-fan.org/ftp/contrib/yum-repo/rhel$releasever/$basearch/");
        lines.add("enabled=1");
        lines.add("gpgcheck=0");
       
        FileModifierFlowContext createRepoContext = new FileModifierFlowContext.Builder()
            .create("/etc/yum.repos.d/city-fan.repo", lines)
            .build();        
        UniversalRole createRepoRole = new UniversalRole.Builder(
            "createRepoRole", tasResolver)
            .runFlow(FileModifierFlow.class, createRepoContext)
            .build();
        
        return createRepoRole;
    }
}
