package com.ca.apm.tests.testbed;

import com.ca.tas.artifact.IBuiltArtifact;
import com.ca.tas.artifact.thirdParty.WebLogicVersion;
import com.ca.tas.resolver.ITasResolver;
import com.ca.tas.role.linux.YumInstallGlibcI686Role;
import com.ca.tas.role.web.WebAppRole;
import com.ca.tas.role.webapp.WebLogicRole;
import com.ca.tas.testbed.ITestbed;
import com.ca.tas.testbed.ITestbedFactory;
import com.ca.tas.testbed.ITestbedMachine;
import com.ca.tas.testbed.TestBedUtils;
import com.ca.tas.testbed.Testbed;
import com.ca.tas.tests.annotations.TestBedDefinition;
import com.ca.apm.tests.role.WebLogicPortalRole;
import com.ca.apm.tests.role.WebLogicPortalVersion;
import com.ca.apm.tests.role.JavaRole;
import com.ca.tas.role.IRole;

@TestBedDefinition
public class WeblogicPortalTestBed implements ITestbedFactory {

    private static final String WEBLOGIC_TEMPLATE_ID = ITestbedMachine.TEMPLATE_W64;
    public static final String WEBLOGIC_ROLE_ID = "weblogicRole";
    public static final String WEBLOGIC_MACHINE_ID = "webLogicMachine";
    public static final String GLIBC_ROLE_ID = "glibc";
    private static final String QAAPP_ROLE_ID = "qaAppRole";
    public static final String JAVA7_ROLE_ID = "java7Role";

    @Override
    public ITestbed create(ITasResolver tasResolver) {
    	
    	
    	  IRole java7Role = new JavaRole.LinuxBuilder(JAVA7_ROLE_ID, tasResolver).build();
        
        WebLogicPortalRole webLogicRole = new WebLogicPortalRole.Builder(WEBLOGIC_ROLE_ID, tasResolver)
        .installLocation("C:\\Oracle\\Middleware")
        .installLogFile("C:\\Oracle\\install.log")
        .webLogicInstallerDir("C:\\Oracle\\sources")
        .responseFileDir("C:\\test")
        
            .version(WebLogicPortalVersion.v103x86win)
            .build();

        ITestbedMachine machine = TestBedUtils
            .createWindowsMachine(WEBLOGIC_MACHINE_ID, WEBLOGIC_TEMPLATE_ID, webLogicRole);
        machine.addRole(java7Role);
        machine.addRole(webLogicRole);
        return Testbed.create(this, machine);
    }
}
