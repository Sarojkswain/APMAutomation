/*
 * Copyright (c) 2014 CA. All rights reserved.
 * 
 * This software and all information contained therein is confidential and
 * proprietary and shall not be duplicated, used, disclosed or disseminated in
 * any way except as authorized by the applicable license agreement, without
 * the express written permission of CA. All authorized reproductions must be
 * marked with this language.
 * 
 * EXCEPT AS SET FORTH IN THE APPLICABLE LICENSE AGREEMENT, TO THE EXTENT
 * PERMITTED BY APPLICABLE LAW, CA PROVIDES THIS SOFTWARE WITHOUT WARRANTY OF
 * ANY KIND, INCLUDING WITHOUT LIMITATION, ANY IMPLIED WARRANTIES OF
 * MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE. IN NO EVENT WILL CA BE
 * LIABLE TO THE END USER OR ANY THIRD PARTY FOR ANY LOSS OR DAMAGE, DIRECT OR
 * INDIRECT, FROM THE USE OF THIS SOFTWARE, INCLUDING WITHOUT LIMITATION, LOST
 * PROFITS, BUSINESS INTERRUPTION, GOODWILL, OR LOST DATA, EVEN IF CA IS
 * EXPRESSLY ADVISED OF SUCH LOSS OR DAMAGE.
 * Author : JAMSA07 / JAMMI SANTOSH
 * Date : 23/01/2016
 */
package com.ca.apm.tests.testbed;

import static com.ca.apm.tests.cem.common.CEMConstants.BTS_LOC;
import static com.ca.apm.tests.cem.common.CEMConstants.BTS_ZIP_ROLE_ID;
import static com.ca.apm.tests.cem.common.CEMConstants.BTS_ZIP_VERSION;
import static com.ca.apm.tests.cem.common.CEMConstants.CHROME_SELENIUM_DRIVER_ROLE_ID;
import static com.ca.apm.tests.cem.common.CEMConstants.EM_MACHINE_ID;
import static com.ca.apm.tests.cem.common.CEMConstants.EM_MACHINE_TEMPLATE_ID;
import static com.ca.apm.tests.cem.common.CEMConstants.EM_ROLE_ID;
import static com.ca.apm.tests.cem.common.CEMConstants.IE_SELENIUM_DRIVER_ROLE_ID;
import static com.ca.apm.tests.cem.common.CEMConstants.JAVA7_LINUX_HOME;
import static com.ca.apm.tests.cem.common.CEMConstants.JAVA7_LINUX_ROLE_ID;
import static com.ca.apm.tests.cem.common.CEMConstants.TIM_ATTENDEE_LIN_ROLE_ID;
import static com.ca.apm.tests.cem.common.CEMConstants.TIM_MACHINE_ID;
import static com.ca.apm.tests.cem.common.CEMConstants.TIM_MACHINE_TEMPLATE_ID;
import static com.ca.apm.tests.cem.common.CEMConstants.TIM_ROLE_ID;
import static com.ca.apm.tests.cem.common.CEMConstants.VERSION;
import static com.ca.apm.tests.cem.common.CEMConstants.WEBDRIVER_ROLE_ID;
import static com.ca.apm.tests.cem.common.CEMConstants.chromeSeleniumDriver;
import static com.ca.apm.tests.cem.common.CEMConstants.ieSeleniumDriver;
import static com.ca.apm.tests.cem.common.CEMConstants.seleniumDriverHome;

import org.eclipse.aether.artifact.DefaultArtifact;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ca.apm.tests.artifact.ChromeDriverArtifact;
import com.ca.apm.tests.artifact.IEDriverServerArtifact;
import com.ca.tas.annotation.resource.RemoteResource;
import com.ca.tas.resolver.ITasResolver;
import com.ca.tas.role.EmRole;
import com.ca.tas.role.IRole;
import com.ca.tas.role.TIMAttendeeRole;
import com.ca.tas.role.TIMRole;
import com.ca.tas.role.utility.GenericRole;
import com.ca.tas.role.utility.UniversalRole;
import com.ca.tas.role.webapp.JavaRole;
import com.ca.tas.testbed.ITestbed;
import com.ca.tas.testbed.ITestbedFactory;
import com.ca.tas.testbed.TestBedUtils;
import com.ca.tas.testbed.Testbed;
import com.ca.tas.testbed.TestbedMachine;
import com.ca.tas.tests.annotations.TestBedDefinition;
/**
 * CEMTestbed class.
 *
 * Testbed description.
 */
@TestBedDefinition
public abstract class CEMTestBed implements ITestbedFactory {

    protected static final Logger LOGGER = LoggerFactory.getLogger(CEMTestBed.class);
    public String browser="";

    @Override
    public ITestbed create(ITasResolver tasResolver) {

        // EM role
        EmRole emRole =
            new EmRole.Builder(EM_ROLE_ID, tasResolver)
                .nostartEM().nostartWV().build();
        
//        com.ca.apm.role.EnableCemApiRole enableCemApiRole = new com.ca.apm.role.EnableCemApiRole.Builder(ENABLE_CEM_API_ROLE_ID, tasResolver)
//        .emHomeDir(emRole.getDeployEmFlowContext().getInstallDir())
//        .emInstallVersion(tasResolver.getDefaultVersion()) 
//        .build();
//    
        GenericRole downloadBTsRole =
            new GenericRole.Builder(BTS_ZIP_ROLE_ID, tasResolver).download(
                new DefaultArtifact("com.ca.apm.coda.testdata.em.virtualagents", "CEM_BTS", "zip",
                    BTS_ZIP_VERSION), BTS_LOC + "CEM_BTS.zip").build();
        
        TIMRole timRole =
            new TIMRole.Builder(TIM_ROLE_ID, tasResolver).timVersion(VERSION).build();
    
        TIMAttendeeRole timAttendeeLinuxRole =
            new TIMAttendeeRole.LinuxBuilder(TIM_ATTENDEE_LIN_ROLE_ID, timRole, tasResolver)
                .build();
    
        IRole java7LinuxRole =
            new JavaRole.LinuxBuilder(JAVA7_LINUX_ROLE_ID, tasResolver).dir(JAVA7_LINUX_HOME)
                .build();
        
        this.setBrowser();
        System.out.println("BROWSER IS " + browser);
        UniversalRole webDriverRole =
            new UniversalRole.Builder(WEBDRIVER_ROLE_ID, tasResolver).download(
                IEDriverServerArtifact.ENUM_NAME.getArtifact(),
                seleniumDriverHome + ieSeleniumDriver).build();
        webDriverRole.addProperty("browser.type", browser);
    
    
        UniversalRole ieSeleniumDriverRole =
            createIESeleniumDriverRole(tasResolver, seleniumDriverHome);
        UniversalRole chromeSeleniumDriverRole =
            createChromeSeleniumDriverRole(tasResolver, seleniumDriverHome);
        
        TestbedMachine timMachine =
          TestBedUtils.createLinuxMachine(TIM_MACHINE_ID, TIM_MACHINE_TEMPLATE_ID, java7LinuxRole, timRole, timAttendeeLinuxRole);
        
        TestbedMachine emMachine =
            TestBedUtils.createWindowsMachine(EM_MACHINE_ID,
                EM_MACHINE_TEMPLATE_ID, emRole, ieSeleniumDriverRole, chromeSeleniumDriverRole, webDriverRole, downloadBTsRole);
        
        emMachine.addRemoteResource(RemoteResource.createFromRegExp(".*", emRole.getDeployEmFlowContext().getInstallDir()+ "\\logs"));
        
        return Testbed.create(this, timMachine, emMachine);
    }
    
    protected abstract void setBrowser();

    protected UniversalRole createIESeleniumDriverRole(ITasResolver tasResolver, String path) {

        UniversalRole ieSeleniumDriverRole =
            new UniversalRole.Builder(IE_SELENIUM_DRIVER_ROLE_ID, tasResolver).download(
                IEDriverServerArtifact.ENUM_NAME.getArtifact(),
                seleniumDriverHome + ieSeleniumDriver).build();

        ieSeleniumDriverRole.addProperty("seleniumDriverHome", seleniumDriverHome);
        ieSeleniumDriverRole.addProperty("ieSeleniumDriver", ieSeleniumDriver);

        return ieSeleniumDriverRole;
    }

    protected UniversalRole createChromeSeleniumDriverRole(ITasResolver tasResolver, String path) {

        UniversalRole chromeSeleniumDriverRole =
            new UniversalRole.Builder(CHROME_SELENIUM_DRIVER_ROLE_ID, tasResolver).download(
                ChromeDriverArtifact.ENUM_NAME.getArtifact(),
                seleniumDriverHome + chromeSeleniumDriver).build();

        chromeSeleniumDriverRole.addProperty("seleniumDriverHome", seleniumDriverHome);
        chromeSeleniumDriverRole.addProperty("chromeSeleniumDriver", chromeSeleniumDriver);

        return chromeSeleniumDriverRole;
    }
}
