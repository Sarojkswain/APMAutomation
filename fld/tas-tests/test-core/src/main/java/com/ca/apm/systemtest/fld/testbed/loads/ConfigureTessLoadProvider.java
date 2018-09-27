package com.ca.apm.systemtest.fld.testbed.loads;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;

import com.ca.apm.systemtest.fld.flow.ConfigureTessFlowContext.PreferredBrowser;
import com.ca.apm.systemtest.fld.flow.ConfigureTessFlowContext.TessService;
import com.ca.apm.systemtest.fld.role.ConfigureTimRole;
import com.ca.apm.systemtest.fld.testbed.FLDConstants;
import com.ca.apm.systemtest.fld.testbed.FLDLoadConstants;
import com.ca.apm.systemtest.fld.testbed.FldTestbedProvider;
import com.ca.apm.systemtest.fld.testbed.regional.FLDConfiguration;
import com.ca.apm.systemtest.fld.testbed.regional.FLDConfigurationService;
import com.ca.tas.resolver.ITasResolver;
import com.ca.tas.role.IRole;
import com.ca.tas.role.TIMRole;
import com.ca.tas.role.tess.ConfigureTessRole;
import com.ca.tas.testbed.ITestbed;
import com.ca.tas.testbed.ITestbedMachine;

/**
 * This load provider configures the TESS services and Web Server Filters with data from the deployed
 * roles in the testbed.  It should be included as the last provider in the chain to ensure
 * that all of its dependent roles are available.
 * @author keyja01
 *
 */
public class ConfigureTessLoadProvider implements FldTestbedProvider, FLDLoadConstants, FLDConstants {

    private static final String CONFIGURE_ETH02 = "ConfigureEth02";
    
    
    @Override
    public Collection<ITestbedMachine> initMachines() {
        return Collections.emptySet();
    }
    

    @Override
    public void initTestbed(ITestbed testbed, ITasResolver tasResolver) {
        ITestbedMachine machine = testbed.getMachineById(FLD_CONTROLLER_MACHINE_ID);
        
        FLDConfiguration fldConfig = FLDConfigurationService.getConfig();
        
        IRole mom = testbed.getRoleById(EM_MOM_ROLE_ID);
        IRole coll01 = testbed.getRoleById(EM_COLL01_ROLE_ID);
        IRole coll02 = testbed.getRoleById(EM_COLL02_ROLE_ID);
        TIMRole tim01 = (TIMRole) testbed.getRoleById(TIM01_ROLE_ID);
        TIMRole tim02 = (TIMRole) testbed.getRoleById(TIM02_ROLE_ID);
        TIMRole tim03 = (TIMRole) testbed.getRoleById(TIM03_ROLE_ID);
        TIMRole tim04 = (TIMRole) testbed.getRoleById(TIM04_ROLE_ID);
        TIMRole tim05 = (TIMRole) testbed.getRoleById(TIM05_ROLE_ID);
        
        ConfigureTessRole.Builder builder = new ConfigureTessRole.Builder("configureTess", tasResolver)
            .removeOldWebServerFilters()
            .removeOldTims()
            .mom(mom)
            .tim(tim01)
            .tim(tim02)
            .tim(tim03)
            .tim(tim04)
            .tim(tim05)
            .tessService(TessService.DbCleanup, coll01)
            .tessService(TessService.TimCollection, coll01)
            .tessService(TessService.StatsAggregation, coll02)
            .preferredBrowser(PreferredBrowser.Firefox)
            ;
        
        FLDConfiguration config = FLDConfigurationService.getConfig();
        builder.reportEmail(config.getReportEmail())
            .smtpHost(fldConfig.getTessSmtpHost());
        
        AppServerConfig[] appServers = new AppServerConfig[] {
            new AppServerConfig(WLS_01_SERVER_01_ROLE_ID, "WebLogic01-7001", 7001, tim03),
            new AppServerConfig(WLS_01_SERVER_02_ROLE_ID, "WebLogic01-7002", 7002, tim03),
            new AppServerConfig(WLS_02_SERVER_01_ROLE_ID, "WebLogic02-7001", 7001, tim03),
            new AppServerConfig(WLS_02_SERVER_02_ROLE_ID, "WebLogic02-7002", 7002, tim03),
            new AppServerConfig(TOMCAT_6_ROLE_ID, "Tomcat6", 8080, tim03),
            new AppServerConfig(TOMCAT_7_ROLE_ID, "Tomcat7", 9080, tim03),
            new AppServerConfig(TOMCAT_9080_ROLE_ID, "Tomcat9080", 8080, tim02),
            new AppServerConfig(TOMCAT_9081_ROLE_ID, "Tomcat9081", 9080, tim02),
            new AppServerConfig(JBOSS6_ROLE_ID, "JBoss6", 8180, tim01),
            new AppServerConfig(JBOSS7_ROLE_ID, "JBoss7", 8080, tim01),
            new AppServerConfig(TC_ROLE_ID, "Flex-load", 8080, tim05),
            new AppServerConfig(WEBSPHERE_01_ROLE_ID, "Websphere", 9080, tim04),
            new AppServerConfig(DOTNET_MACHINE1+"_"+DOTNET_AGENT_ROLE_ID, "DotNet1", 0, tim05),
            new AppServerConfig(DOTNET_MACHINE2+"_"+DOTNET_AGENT_ROLE_ID, "DotNet2", 0, tim05),
        };
        
        for (AppServerConfig cfg: appServers) {
            IRole role = testbed.getRoleById(cfg.roleId);
            builder.webServerFilter(cfg.name, cfg.tim, role, cfg.port);
        }
        
        // make sure it's after the MOM is registered on the AGC
        ConfigureTessRole configureTessRole = builder.build();
        ITestbedMachine momMachine = testbed.getMachineById(MOM_MACHINE_ID);
        HashSet<IRole> roles = new HashSet<IRole>(Arrays.asList(momMachine.getRoles()));
        roles.add(mom);
        roles.add(coll01);
        roles.add(coll02);
        
        configureTessRole.after(roles);
        machine.addRole(configureTessRole);

        IRole lastRole = configureTessRole;
        for (String timRoleId: TIM_ROLES) {
            String timHostname = tasResolver.getHostnameById(timRoleId);
            ConfigureTimRole configureTimRole = new ConfigureTimRole.Builder(timRoleId + CONFIGURE_ETH02)
                .timHostname(timHostname)
                .requiredInterface("eth2")
                .disallowedInterface("eth0")
                .additionalProperty("MaxFlexRequestBodySize", "100000")
                .additionalProperty("MaxFlexResponseBodySize", "100000")
                .build();
            //configureTessRole.after(configureTimRole);
            machine.addRole(configureTimRole);
            configureTimRole.after(lastRole);
            lastRole = configureTimRole;
        }

    }

    private static class AppServerConfig {
        private String roleId;
        private String name;
        private int port;
        private TIMRole tim;
        
        public AppServerConfig(String roleId, String name, int port, TIMRole tim) {
            this.roleId = roleId;
            this.name = name;
            this.port = port;
            this.tim = tim;
        }
        
        
    }
}
