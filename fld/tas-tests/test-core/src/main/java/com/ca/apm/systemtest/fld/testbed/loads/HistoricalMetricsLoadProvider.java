/**
 * 
 */
package com.ca.apm.systemtest.fld.testbed.loads;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.codehaus.cargo.util.log.Loggable;
import org.jfree.util.Log;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ca.apm.automation.action.flow.agent.AgentInstrumentationLevel;
import com.ca.apm.systemtest.fld.artifact.TessTestArtifact;
import com.ca.apm.systemtest.fld.testbed.FLDConstants;
import com.ca.apm.systemtest.fld.testbed.FLDLoadConstants;
import com.ca.apm.systemtest.fld.testbed.FldTestbedProvider;
import com.ca.apm.systemtest.fld.testbed.regional.FLDConfiguration;
import com.ca.apm.systemtest.fld.testbed.regional.FLDConfigurationService;
import com.ca.tas.artifact.thirdParty.JavaBinary;
import com.ca.tas.artifact.thirdParty.TomcatVersion;
import com.ca.tas.resolver.ITasResolver;
import com.ca.tas.role.AgentRole;
import com.ca.tas.role.IRole;
import com.ca.tas.role.web.WebAppRole;
import com.ca.tas.role.webapp.JavaRole;
import com.ca.tas.role.webapp.TomcatRole;
import com.ca.tas.testbed.ITestbed;
import com.ca.tas.testbed.ITestbedMachine;

/**
 * @author keyja01
 *
 */
public class HistoricalMetricsLoadProvider implements FldTestbedProvider, FLDLoadConstants, FLDConstants {
    private static final String BASE_ROLE_ID = HistoricalMetricsLoadProvider.class.getSimpleName();
    private static final Logger logger = LoggerFactory.getLogger(HistoricalMetricsLoadProvider.class);
    
    private FLDConfiguration fldConfig = FLDConfigurationService.getConfig();

    /* (non-Javadoc)
     * @see com.ca.apm.systemtest.fld.testbed.FldTestbedProvider#initMachines()
     */
    @Override
    public Collection<ITestbedMachine> initMachines() {
        return Collections.emptyList();
    }

    /* (non-Javadoc)
     * @see com.ca.apm.systemtest.fld.testbed.FldTestbedProvider#initTestbed(com.ca.tas.testbed.ITestbed, com.ca.tas.resolver.ITasResolver)
     */
    @Override
    public void initTestbed(ITestbed testbed, ITasResolver tasResolver) {
        ITestbedMachine accMachine = testbed.getMachineById(ACC_MACHINE_ID);
        
        JavaBinary javaVersion = JavaBinary.WINDOWS_64BIT_JDK_17;
        JavaRole javaRole = new JavaRole.Builder(BASE_ROLE_ID + "_JavaRole", tasResolver)
            .dir("c:\\hmjava\\jdk1.7")
            .version(javaVersion)
            .build();
        WebAppRole<TomcatRole> tessTestRole = new WebAppRole.Builder<TomcatRole>(BASE_ROLE_ID + "_TessTestRole")
            .artifact(new TessTestArtifact(tasResolver).createArtifact())
            .cargoDeploy()
            .contextName("tesstest")
            .build();
        TomcatRole tomcatRole = new TomcatRole.Builder(HISTORICAL_METRICS_TOMCAT_ROLE_ID, tasResolver)
            .additionalVMOptions(Arrays.asList("-Xms256m", "-Xmx512m", "-XX:PermSize=256m", "-XX:MaxPermSize=512m", "-server"))
            .tomcatVersion(TomcatVersion.v70)
            .customJava(javaRole)
            .webApp(tessTestRole)
            .build();
        
        Map<String, String> additionalProperties = new HashMap<String, String>();
        additionalProperties.put("introscope.agent.hostName", "HistoricalMetrics");
        additionalProperties.put("introscope.agent.agentName", "TomcatDM-00001");
        additionalProperties.put("introscope.agent.customProcessName", "Tomcat");
        additionalProperties.put("introscope.agent.urlgroup.keys", "byusers,default");
        additionalProperties.put("introscope.agent.urlgroup.group.byusers.pathprefix", "/tesstest/webapp/foo");
        additionalProperties.put("introscope.agent.urlgroup.group.byusers.format", "Foo user {query_param:user} option {query_param:option}");
        
        String coll7RoleId = tasResolver.getHostnameById(EM_COLL07_ROLE_ID);
        logger.info("AGENTAGENTAGENT: " + coll7RoleId);
        
        IRole tomcatAgentRole = new AgentRole.Builder(BASE_ROLE_ID + "_TomcatAgentRole", tasResolver)
            .webAppServer(tomcatRole)
            .intrumentationLevel(AgentInstrumentationLevel.FULL)
            .overrideEM(coll7RoleId, 5001)
            .version(fldConfig.getEmVersion())
            .additionalProperties(additionalProperties)
            .build();
        
        accMachine.addRole(javaRole, tomcatRole, tessTestRole, tomcatAgentRole);
    }

}
