/**
 * 
 */
package com.ca.apm.systemtest.fld.testbed.loads;

import java.util.Arrays;
import java.util.Collection;

import com.ca.apm.systemtest.fld.role.EmailDashboardRole;
import com.ca.apm.systemtest.fld.role.EmailDashboardRole.Builder;
import com.ca.apm.systemtest.fld.role.LoadMonitorConfigureRole;
import com.ca.apm.systemtest.fld.role.LoadMonitorWebAppRole;
import com.ca.apm.systemtest.fld.testbed.FLDLoadConstants;
import com.ca.apm.systemtest.fld.testbed.FldTestbedProvider;
import com.ca.apm.systemtest.fld.testbed.regional.FLDConfiguration;
import com.ca.tas.artifact.thirdParty.TomcatVersion;
import com.ca.tas.resolver.ITasResolver;
import com.ca.tas.role.IRole;
import com.ca.tas.role.utility.UtilityRole;
import com.ca.tas.role.web.WebAppRole;
import com.ca.tas.role.webapp.TomcatRole;
import com.ca.tas.testbed.Bitness;
import com.ca.tas.testbed.ITestbed;
import com.ca.tas.testbed.ITestbedMachine;
import com.ca.tas.testbed.TestbedMachine;

/**
 * Deploys a machine with a Tomcat and FLD Load Monitor
 * @author keyja01
 *
 */
public class FldControllerLoadProvider implements FldTestbedProvider, FLDLoadConstants {
    
    private FLDConfiguration cfg;
    private ITestbedMachine machine = null;
    
    
    
    public FldControllerLoadProvider(FLDConfiguration cfg) {
        this.cfg = cfg;
    }


    @Override
    public Collection<ITestbedMachine> initMachines() {
        machine = new TestbedMachine.Builder(FLD_CONTROLLER_MACHINE_ID)
            .templateId(ITestbedMachine.TEMPLATE_W64)
            .bitness(Bitness.b64)
            .build();
        return Arrays.asList(machine);
    }

    /* (non-Javadoc)
     * @see com.ca.apm.systemtest.fld.testbed.FldTestbedProvider#initTestbed(com.ca.tas.testbed.Testbed, com.ca.tas.resolver.ITasResolver)
     */
    @Override
    public void initTestbed(ITestbed testbed, ITasResolver tasResolver) {
        if (cfg != null && cfg.getLogMonitorEmail() != null && cfg.getLogMonitorEmail().length > 0) {
            Builder builder = new EmailDashboardRole.Builder("emailDashboardRole", tasResolver)
                .withMailHost(cfg.getFldConfigSmtpHost())
                .testbedAPM(testbed);
            for (String email: cfg.getLogMonitorEmail()) {
                builder.withEmailRecieptent(email);
            }
            EmailDashboardRole emailRole = builder.build();
            machine.addRole(emailRole);
        }
        
        WebAppRole<TomcatRole> loadmonAppRole = new LoadMonitorWebAppRole.Builder(FLD_CONTROLLER_ROLE_ID + "-app", tasResolver)
            .build();
        
        TomcatRole tomcatRole = new TomcatRole.Builder(FLD_CONTROLLER_MACHINE_ID + "-tomcat", tasResolver)
            .tomcatVersion(TomcatVersion.v80)
            .webApp(loadmonAppRole)
            .build();
        
        
        LoadMonitorConfigureRole configureRole = new LoadMonitorConfigureRole.Builder(FLD_CONTROLLER_ROLE_ID, tasResolver)
            .markerDir("markerFiles")
            .tomcatRole(tomcatRole)
            .build();
        
        configureRole.after(tomcatRole, loadmonAppRole);
        
        IRole start = UtilityRole.commandFlow("custom-tomcat-start", tomcatRole.getStartCmdFlowContext());
        start.after(configureRole);
        machine.addRole(tomcatRole, loadmonAppRole, configureRole, start);
    }

}
