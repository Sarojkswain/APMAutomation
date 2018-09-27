/**
 * 
 */
package com.ca.apm.systemtest.fld.testbed.loads;

import static com.ca.apm.systemtest.fld.testbed.FLDMainClusterTestbed.EM_PORT;
import static com.ca.apm.systemtest.fld.testbed.util.FLDTestbedUtil.getJBossDir;
import static com.ca.apm.systemtest.fld.testbed.util.FLDTestbedUtil.getJavaDir;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import com.ca.apm.automation.action.flow.agent.AgentInstrumentationLevel;
import com.ca.apm.systemtest.fld.artifact.WurlitzerWebAppArtifact;
import com.ca.apm.systemtest.fld.artifact.thirdparty.Axis2WebappVersion;
import com.ca.apm.systemtest.fld.testbed.FLDConstants;
import com.ca.apm.systemtest.fld.testbed.FLDLoadConstants;
import com.ca.apm.systemtest.fld.testbed.FldTestbedProvider;
import com.ca.apm.systemtest.fld.testbed.regional.FLDConfiguration;
import com.ca.apm.systemtest.fld.testbed.regional.FLDConfigurationService;
import com.ca.tas.artifact.thirdParty.JBossVersion;
import com.ca.tas.artifact.thirdParty.JavaBinary;
import com.ca.tas.resolver.ITasResolver;
import com.ca.tas.role.AgentRole;
import com.ca.tas.role.web.WebAppRole;
import com.ca.tas.role.webapp.JavaRole;
import com.ca.tas.role.webapp.JbossRole;
import com.ca.tas.testbed.ITestbed;
import com.ca.tas.testbed.ITestbedMachine;
import com.ca.tas.testbed.TestbedMachine;

/**
 * @author keyja01
 *
 */
public class FLDJbossLoadProvider implements FLDLoadConstants, FLDConstants, FldTestbedProvider {
    private ITestbedMachine machine;

    private FLDConfiguration fldConfig = FLDConfigurationService.getConfig();
    
    @Override
    public Collection<ITestbedMachine> initMachines() {
        machine = new TestbedMachine.Builder(JBOSS_MACHINE).templateId(ITestbedMachine.TEMPLATE_W64)
            .build();
        return Arrays.asList(machine);
    }
    
    /* (non-Javadoc)
     * @see com.ca.apm.systemtest.fld.testbed.FldTestbedProvider#initTestbed(com.ca.tas.testbed.Testbed, com.ca.tas.resolver.ITasResolver)
     */
    @Override
    public void initTestbed(ITestbed testbed, ITasResolver tasResolver) {

        addJboss6(testbed, tasResolver);
        addJboss7(testbed, tasResolver);
    }

    private void addJboss7(ITestbed testbed, ITasResolver tasResolver) {
        JavaBinary javaBin = JavaBinary.WINDOWS_64BIT_JDK_17;
        JavaRole javaRole = new JavaRole.Builder(JBOSS_MACHINE + "-jdk7Role", tasResolver)
            .version(javaBin)
            .dir(getJavaDir(javaBin))
            .build();
        
        JBossVersion jbossVersion = JBossVersion.JBOSS711;
        WebAppRole<JbossRole> webAppRole = new WebAppRole.Builder<JbossRole>(JBOSS_MACHINE + "-" + JBOSS7_ROLE_ID + "-axis2WebApp")
            .artifact(Axis2WebappVersion.v154)
            .cargoDeploy()
            .contextName("axis2")
            .build();
        
        
        JbossRole jbossRole = new JbossRole.Builder(JBOSS7_ROLE_ID, tasResolver)
            .customJava(javaRole)
            .jbossInstallDirectory(getJBossDir(jbossVersion))
            .version(jbossVersion)
            .addWebAppRole(webAppRole)
            .autostart()
            .build();
        
        Map<String, String> additionalProperties = new HashMap<>();
        additionalProperties.put("introscope.agent.customProcessName", "Jboss7");
        additionalProperties.put("introscope.agent.agentName", JBOSS_AGENT);
        additionalProperties.put("introscope.agent.hostName", JBOSS01_HOST_NAME);
        AgentRole agentRole = new AgentRole.Builder(JBOSS7_ROLE_ID + "-agent", tasResolver)
            .intrumentationLevel(AgentInstrumentationLevel.TYPICAL)
            .webAppServer(jbossRole)
            .overrideEM(tasResolver.getHostnameById(EM_MOM_ROLE_ID), EM_PORT)
            .version(fldConfig.getEmVersion())
            .additionalProperties(additionalProperties)
            .build();
        
        machine.addRole(javaRole, webAppRole, jbossRole, agentRole);
    }

    private void addJboss6(ITestbed testbed, ITasResolver tasResolver) {
        JavaBinary javaBin = JavaBinary.WINDOWS_64BIT_JDK_16;
        JavaRole javaRole = new JavaRole.Builder(JBOSS_MACHINE + "-jdk6Role", tasResolver)
            .version(javaBin)
            .dir(getJavaDir(javaBin))
            .build();
        
        JBossVersion jbossVersion = JBossVersion.JBOSS610;
        WebAppRole<JbossRole> webAppRole = new WebAppRole.Builder<JbossRole>("roleId")
            .artifact(new WurlitzerWebAppArtifact(tasResolver).createArtifact())
            .cargoDeploy()
            .contextName("wurlitzer")
            .build();
        
        
        Collection<String> attrs = Arrays.asList("-Djboss.service.binding.set=ports-01");
        JbossRole jbossRole = new JbossRole.Builder(JBOSS6_ROLE_ID, tasResolver)
            .customJava(javaRole)
            .jbossInstallDirectory(getJBossDir(jbossVersion))
            .version(jbossVersion)
            .addWebAppRole(webAppRole)
            .agentCmdLineAttributes(attrs)
            .autostart()
            .build();
        
        Map<String, String> additionalProperties = new HashMap<>();
        additionalProperties.put("introscope.agent.customProcessName", "Jboss6");
        additionalProperties.put("introscope.agent.agentName", JBOSS_AGENT);
        additionalProperties.put("introscope.agent.hostName", JBOSS01_HOST_NAME);
        AgentRole agentRole = new AgentRole.Builder(JBOSS6_ROLE_ID + "-agent", tasResolver)
            .intrumentationLevel(AgentInstrumentationLevel.TYPICAL)
            .webAppServer(jbossRole)
            .overrideEM(tasResolver.getHostnameById(EM_MOM_ROLE_ID), EM_PORT)
            .version(fldConfig.getEmVersion())
            .additionalProperties(additionalProperties)
            .build();
        
        machine.addRole(javaRole, webAppRole, jbossRole, agentRole);
    }

}
