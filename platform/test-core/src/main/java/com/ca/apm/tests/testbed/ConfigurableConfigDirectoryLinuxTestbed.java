/*Copyright (c) 2016 CA. All rights reserved.
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
 * Author : BALRA06/ RADHA BALASUBRAMANIAM
 * 
 */

package com.ca.apm.tests.testbed;

import java.util.Arrays;
import java.util.List;

import com.ca.apm.tests.em.properties.ConfigurableConfigDirectoryConstants;
import com.ca.tas.annotation.resource.RemoteResource;
import com.ca.tas.resolver.ITasResolver;
import com.ca.tas.role.EmRole;
import com.ca.tas.testbed.ITestbed;
import com.ca.tas.testbed.ITestbedFactory;
import com.ca.tas.testbed.ITestbedMachine;
import com.ca.tas.testbed.TestBedUtils;
import com.ca.tas.testbed.Testbed;
import com.ca.tas.tests.annotations.TestBedDefinition;

/**
 * SampleTestbed class.
 *
 * Testbed description.
 */
@TestBedDefinition
public class ConfigurableConfigDirectoryLinuxTestbed implements ITestbedFactory {


    @Override
    public ITestbed create(ITasResolver tasResolver) {
       
    	// Features to be installed for EM2
        List<String> em2chosenfeatures = Arrays.asList("Enterprise Manager", "WebView");
    	
    	// create EM1 role
        EmRole em1Role =
            new EmRole.LinuxBuilder(ConfigurableConfigDirectoryConstants.EM1_ROLE_ID, tasResolver)
        	.installDir(ConfigurableConfigDirectoryConstants.EM1_INSTALL_LINUX_DIR)
        	.nostartEM().nostartWV().build();   
        
        // create EM1 role
        EmRole em2Role =
            new EmRole.LinuxBuilder(ConfigurableConfigDirectoryConstants.EM2_ROLE_ID, tasResolver)
        	.installDir(ConfigurableConfigDirectoryConstants.EM2_INSTALL_LINUX_DIR)
        	.silentInstallChosenFeatures(em2chosenfeatures)
            .dbhost(tasResolver.getHostnameById(ConfigurableConfigDirectoryConstants.EM1_ROLE_ID))
        	.nostartEM().nostartWV().build(); 

        // map roles to machines
        ITestbedMachine emMachine =
            TestBedUtils.createLinuxMachine(ConfigurableConfigDirectoryConstants.EM_MACHINE_ID,
                ConfigurableConfigDirectoryConstants.CO66_TEMPLATE_ID);
        emMachine.addRole(em1Role,em2Role);       

        emMachine.addRemoteResource(RemoteResource.createFromRegExp(".*", em1Role.getDeployEmFlowContext().getInstallDir()+ "/logs"));
        emMachine.addRemoteResource(RemoteResource.createFromRegExp(".*", em2Role.getDeployEmFlowContext().getInstallDir()+ "/logs"));
                
        return new Testbed(getClass().getSimpleName()).addMachine(emMachine);
    }
}





















