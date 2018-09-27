package com.ca.apm.systemtest.fld.sample;

import static com.ca.apm.systemtest.fld.testbed.util.FLDTestbedUtil.getJBossDir;
import static com.ca.apm.systemtest.fld.testbed.util.FLDTestbedUtil.getJavaDir;
import static com.ca.apm.systemtest.fld.testbed.util.FLDTestbedUtil.getTomcatDir;

import java.util.Arrays;

import com.ca.tas.artifact.thirdParty.JBossVersion;
import com.ca.tas.artifact.thirdParty.JavaBinary;
import com.ca.tas.artifact.thirdParty.TomcatVersion;
import com.ca.tas.resolver.ITasResolver;
import com.ca.tas.role.webapp.JavaRole;
import com.ca.tas.role.webapp.JbossRole;
import com.ca.tas.role.webapp.TomcatRole;
import com.ca.tas.testbed.ITestbed;
import com.ca.tas.testbed.ITestbedFactory;
import com.ca.tas.testbed.ITestbedMachine;
import com.ca.tas.testbed.Testbed;
import com.ca.tas.testbed.TestbedMachine;
import com.ca.tas.tests.annotations.TestBedDefinition;

@TestBedDefinition
public class SampleFldLoadTestbed implements ITestbedFactory {

    public static final String MACHINE_ID = "sampleFldLoadTestMachine";

    public static final JavaBinary JAVA_VERSION = JavaBinary.WINDOWS_64BIT_JDK_16;
    public static final JBossVersion JBOSS_VERSION = JBossVersion.JBOSS610;

    public static final TomcatVersion TOMCAT_VERSION = TomcatVersion.v60;

    @Override
    public ITestbed create(ITasResolver tasResolver) {
        ITestbedMachine testMachine =
            (new TestbedMachine.Builder(MACHINE_ID)).templateId(ITestbedMachine.TEMPLATE_W64)
                .build();

        JavaRole javaRole =
            (new JavaRole.Builder("javaRole", tasResolver)).dir(getJavaDir(JAVA_VERSION))
                .version(JAVA_VERSION).build();
        testMachine.addRole(javaRole);

        TomcatRole tomcatRole =
            (new TomcatRole.Builder("tomcatRole", tasResolver))
                .additionalVMOptions(
                    Arrays.asList("-Xms256m", "-Xmx512m", "-XX:PermSize=256m",
                        "-XX:MaxPermSize=512m", "-server",
                        "-Dcom.wily.introscope.agent.agentName=Tomcat6")).customJava(javaRole)
                .installDir(getTomcatDir(TOMCAT_VERSION)).jdkHomeDir(getJavaDir(JAVA_VERSION))
                .tomcatVersion(TOMCAT_VERSION).build();
        testMachine.addRole(tomcatRole);

        JbossRole jbossRole =
            (new JbossRole.Builder("jbossRole", tasResolver)).autostart().customJava(javaRole)
                .jbossInstallDirectory(getJBossDir(JBOSS_VERSION)).version(JBOSS_VERSION).build();
        testMachine.addRole(jbossRole);

        ITestbed testbed = new Testbed(getClass().getSimpleName());
        testbed.addMachine(testMachine);

        return testbed;
    }

}
