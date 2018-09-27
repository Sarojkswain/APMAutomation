package com.ca.apm.systemtest.fld.testbed.loads;

import static com.ca.apm.systemtest.fld.testbed.util.FLDTestbedUtil.getAgentDynamicInstrumentationDir;
import static com.ca.apm.systemtest.fld.testbed.util.FLDTestbedUtil.getCLWJar;
import static com.ca.apm.systemtest.fld.testbed.util.FLDTestbedUtil.getCLWLibDir;
import static com.ca.apm.systemtest.fld.testbed.util.FLDTestbedUtil.getJavaDir;

import java.util.Arrays;
import java.util.Collection;

import org.eclipse.aether.artifact.Artifact;

import com.ca.apm.systemtest.fld.artifact.CLWorkstationArtifact;
import com.ca.apm.systemtest.fld.role.DynamicInstrumentationLoadRole;
import com.ca.apm.systemtest.fld.testbed.FLDConstants;
import com.ca.apm.systemtest.fld.testbed.FLDLoadConstants;
import com.ca.apm.systemtest.fld.testbed.FldTestbedProvider;
import com.ca.tas.artifact.thirdParty.JavaBinary;
import com.ca.tas.resolver.ITasResolver;
import com.ca.tas.role.utility.UniversalRole;
import com.ca.tas.role.webapp.JavaRole;
import com.ca.tas.testbed.ITestbed;
import com.ca.tas.testbed.ITestbedMachine;
import com.ca.tas.testbed.TestbedMachine;


public class FLDAgentDynamicInstrumentationProvider implements FLDConstants, FLDLoadConstants, FldTestbedProvider {

    public static final JavaBinary JAVA_VERSION = JavaBinary.WINDOWS_64BIT_JDK_17;
    private ITestbedMachine testMachine;

    
    @Override
    public Collection<ITestbedMachine> initMachines() {
        testMachine = new TestbedMachine.Builder(DYNAMIC_INSTR_MACHINE_ID).templateId(
                ITestbedMachine.TEMPLATE_W64).build();
        return Arrays.asList(testMachine);
    }
    
    @Override
    public void initTestbed(ITestbed testbed, ITasResolver tasResolver) {
        // JDK
        JavaRole javaRole =
            new JavaRole.Builder(DYNAMIC_INSTR_ROLE_ID + "-java", tasResolver)
                .dir(getJavaDir(JAVA_VERSION)).version(JAVA_VERSION).build();
        testMachine.addRole(javaRole);

        // Command Line Workstation
        String clwDir = getCLWLibDir();
        String clwJar = getCLWJar();
        Artifact artifact = new CLWorkstationArtifact(tasResolver).createArtifact().getArtifact();
        UniversalRole clwRole =
            new UniversalRole.Builder(DYNAMIC_INSTR_ROLE_ID + "-clw", tasResolver).download(
                artifact, clwDir, clwJar).build();
        testMachine.addRole(clwRole);

        String momHost = tasResolver.getHostnameById(EM_MOM_ROLE_ID);
        String tomcatHost = tasResolver.getHostnameById(TOMCAT_7_ROLE_ID);

        // StringBuilder("http://")).append(host).append(':').append(port).append("/HelloWorld10k/HelloWorldServlet").append(servlets)
        DynamicInstrumentationLoadRole diRole =
            new DynamicInstrumentationLoadRole.Builder(DYNAMIC_INSTR_ROLE_ID, tasResolver)
                .em(momHost).agent(".*Tomcat7", tomcatHost, 9080).clwJar(clwDir + "\\" + clwJar)
                .dynamicInstrumentationHome(getAgentDynamicInstrumentationDir()).servlets(1000)
                .urlFormat("http://{0}:{1,number,#}/HelloWorld10k/HelloWorldServlet{2,number,#}")
                .javaHome(getJavaDir(JAVA_VERSION) + "\\bin\\java.exe").build();
        testMachine.addRole(diRole);

    }

}
