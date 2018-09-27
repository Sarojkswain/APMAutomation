/**
 * 
 */
package com.ca.apm.systemtest.fld.testbed.loads;

import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

import com.ca.apm.automation.action.flow.utility.FileCreatorFlow;
import com.ca.apm.automation.action.flow.utility.FileCreatorFlowContext;
import com.ca.apm.automation.action.utils.TasFileUtils;
import com.ca.apm.systemtest.fld.artifact.thirdparty.JMeterVersion;
import com.ca.apm.systemtest.fld.artifact.thirdparty.WeblogicWurlitzer1DomainTemplateVersion;
import com.ca.apm.systemtest.fld.role.JMeterRole;
import com.ca.apm.systemtest.fld.role.loads.WebLogicDomainRole;
import com.ca.apm.systemtest.fld.role.loads.WurlitzerBaseRole;
import com.ca.apm.systemtest.fld.testbed.FLDConstants;
import com.ca.apm.systemtest.fld.testbed.FLDLoadConstants;
import com.ca.apm.systemtest.fld.testbed.FldTestbedProvider;
import com.ca.apm.systemtest.fld.testbed.regional.FLDConfiguration;
import com.ca.apm.systemtest.fld.testbed.regional.FLDConfigurationService;
import com.ca.tas.artifact.IThirdPartyArtifact;
import com.ca.tas.artifact.thirdParty.JavaBinary;
import com.ca.tas.artifact.thirdParty.WebLogicVersion;
import com.ca.tas.resolver.ITasResolver;
import com.ca.tas.role.IRole;
import com.ca.tas.role.utility.UniversalRole;
import com.ca.tas.role.webapp.JavaRole;
import com.ca.tas.role.webapp.WebLogicRole;
import com.ca.tas.testbed.Bitness;
import com.ca.tas.testbed.ITestbed;
import com.ca.tas.testbed.ITestbedMachine;
import com.ca.tas.testbed.TestbedMachine;
import com.ca.tas.type.Platform;
import com.google.common.net.HostAndPort;

import static com.ca.apm.systemtest.fld.testbed.FLDMainClusterTestbed.EM_PORT;

/**
 * @author keyja01
 *
 */
public class FldLoadWeblogicProvider implements FldTestbedProvider, FLDLoadConstants, FLDConstants {
    private ITestbedMachine wls01;
    private ITestbedMachine wls02;
    
    private FLDConfiguration fldConfig = FLDConfigurationService.getConfig();
    
    private static final String JAVA_HOME = "%ProgramFiles%\\Java\\jdk1.7.0_51";
    
    @Override
    public Collection<ITestbedMachine> initMachines() {
        wls01 = new TestbedMachine.Builder(WLS_01_MACHINE_ID)
            .platform(Platform.WINDOWS)
            .bitness(Bitness.b64)
            .templateId(ITestbedMachine.TEMPLATE_W64)
            .build();
        wls02 = new TestbedMachine.Builder(WLS_02_MACHINE_ID)
            .platform(Platform.WINDOWS)
            .bitness(Bitness.b64)
            .templateId(ITestbedMachine.TEMPLATE_W64)
            .build();
        
        return Arrays.asList(wls01, wls02);
    }
    
    int i = 1;
    /* (non-Javadoc)
     * @see com.ca.apm.systemtest.fld.testbed.FldTestbedProvider#initTestbed(com.ca.tas.testbed.Testbed, com.ca.tas.resolver.ITasResolver)
     */
    @Override
    public void initTestbed(ITestbed testbed, ITasResolver tasResolver) {
        initRoles(testbed, tasResolver, wls01, WLS_01_INSTALLATION_ROLE_ID, 
            WLS_01_BASE_AGENT_ROLE_ID, new String[] {WLS_01_SERVER_01_ROLE_ID, WLS_01_SERVER_02_ROLE_ID},
            WLS01_HOST_NAME);
        i++;
        initRoles(testbed, tasResolver, wls02, WLS_02_INSTALLATION_ROLE_ID, 
            WLS_02_BASE_AGENT_ROLE_ID, new String[] {WLS_02_SERVER_01_ROLE_ID, WLS_02_SERVER_02_ROLE_ID},
            WLS02_HOST_NAME);
    }
    
    
    private void initRoles(ITestbed testbed, ITasResolver tasResolver, ITestbedMachine machine, String installId,
            String baseAgentRoleId,String[] domainId, String agentHostName) {
        
        String machineId = machine.getMachineId();
        WurlitzerBaseRole wurlitzerRole = new WurlitzerBaseRole.Builder(machineId + "-wurlitzerage", tasResolver)
            .deployDir("wurlitzer")
            .build();
        
        JavaRole javaRole = new JavaRole.Builder(machineId + "-javage", tasResolver)
            .version(JavaBinary.WINDOWS_64BIT_JDK_18)
            .dir("c:\\java\\" + TasFileUtils.getBasename(tasResolver.getArtifactUrl(JavaBinary.WINDOWS_64BIT_JDK_18.getArtifact())))
            .build();
        
        // create the base installation of WLS
        WebLogicRole installRole = new WebLogicRole.Builder(installId, tasResolver)
            .version(WebLogicVersion.v103x86w)
            .installLocation("c:\\Oracle\\Middleware")
            .installDir("c:\\Oracle\\Middleware\\wls01")
            .responseFileDir("c:\\Oracle\\responseFiles")
            .build();

        IThirdPartyArtifact afact = WeblogicWurlitzer1DomainTemplateVersion.domain1v1_0_2;
        WebLogicDomainRole domain1 = new WebLogicDomainRole.Builder(domainId[0], tasResolver)
            .addDomain(afact, "wurlitzer1")
            .webLogicRole(installRole)
            .overrideEM(HostAndPort.fromParts(tasResolver.getHostnameById(EM_MOM_ROLE_ID), EM_PORT))
            .version(fldConfig.getEmVersion())
            .agentName(WEBLOGIC_WURLITZER_1_AGENT)
            .agentHostName(agentHostName)
            .javaHomeDir(JAVA_HOME)
            .agentDecorator("true")
            .directivesFile("weblogic-typical.pbl,hotdeploy,bizrecording.pbd,ServletHeaderDecorator.pbd,browseragent.pbd")
            .build();
        domain1.after(installRole);

        afact = WeblogicWurlitzer1DomainTemplateVersion.domain2v1_0_4;
        WebLogicDomainRole domain2 = new WebLogicDomainRole.Builder(domainId[1], tasResolver)
            .addDomain(afact, "wurlitzer2")
            .webLogicRole(installRole)
            .overrideEM(HostAndPort.fromParts(tasResolver.getHostnameById(EM_MOM_ROLE_ID), EM_PORT))
            .version(fldConfig.getEmVersion())
            .agentName(WEBLOGIC_WURLITZER_2_AGENT)
            .agentHostName(agentHostName)
            .javaHomeDir(JAVA_HOME)
            .agentDecorator("true")
            .directivesFile("weblogic-typical.pbl,hotdeploy,bizrecording.pbd,ServletHeaderDecorator.pbd,browseragent.pbd")
            .build();
        domain2.after(domain1);
        
        String soaDirectory = "\\testplan\\";
        //String installDir = "c:\\automation\\deployed";
        String fileName = "SOA_load_SoapWurlitzer.jmx";
        
        Map<String, String> jmeterProperties = new LinkedHashMap<>(2);
        jmeterProperties.put("host1", tasResolver.getHostnameById(domain1.getRoleId()) + ":7001");
        jmeterProperties.put("host2", tasResolver.getHostnameById(domain2.getRoleId()) + ":7002");

        JMeterRole jmeterRole = new JMeterRole.Builder(machineId + "-SOALoadRole_JM", tasResolver)
            .jmeterVersion(JMeterVersion.v213)
            .installDir("jmeter")
            .testPlan(fileName)
            .jmeterProperties(jmeterProperties)
            .customJava(javaRole)
            //.autoStart()
            .build();
        
        Collection<IRole> otherRoles = Arrays.asList(new IRole[] {domain1, domain2, wurlitzerRole});
        jmeterRole.after(otherRoles);
        
        FileCreatorFlowContext context = new FileCreatorFlowContext.Builder()
            .fromResource("/soa-load/" + fileName)
            .destinationPath(jmeterRole.getInstallDir() + soaDirectory + fileName)
            .build();
        
        IRole createScriptRole = new UniversalRole.Builder(machineId + "-SOA_script", tasResolver)
                .runFlow(FileCreatorFlow.class, context).build();
        
        createScriptRole.after(jmeterRole);
        
        machine.addRole(wurlitzerRole, javaRole, installRole, domain1, domain2, createScriptRole, jmeterRole);
    }

}
