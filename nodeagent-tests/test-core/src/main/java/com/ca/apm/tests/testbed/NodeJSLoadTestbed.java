/*
 * Copyright (c) 2014 CA.  All rights reserved.
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
 * MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE.  IN NO EVENT WILL CA BE
 * LIABLE TO THE END USER OR ANY THIRD PARTY FOR ANY LOSS OR DAMAGE, DIRECT OR
 * INDIRECT, FROM THE USE OF THIS SOFTWARE, INCLUDING WITHOUT LIMITATION, LOST
 * PROFITS, BUSINESS INTERRUPTION, GOODWILL, OR LOST DATA, EVEN IF CA IS
 * EXPRESSLY ADVISED OF SUCH LOSS OR DAMAGE.
 */

package com.ca.apm.tests.testbed;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;

import org.eclipse.aether.artifact.DefaultArtifact;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ca.apm.tests.artifact.NodeJSRuntimeVersionArtifact;
import com.ca.apm.tests.role.MongoDBRole;
import com.ca.apm.tests.role.NodeJSProbeRole;
import com.ca.apm.tests.role.TixChangeRole;
import com.ca.apm.tests.role.UMAgentRole;
import com.ca.tas.resolver.ITasResolver;
import com.ca.tas.role.EmRole;
import com.ca.tas.role.IRole;
import com.ca.tas.role.MysqlRole;
import com.ca.tas.role.NodeJsRole;
import com.ca.tas.role.RedisRole;
import com.ca.tas.role.utility.ExecutionRole;
import com.ca.tas.role.utility.GenericRole;
import com.ca.tas.role.webapp.TomcatRole;
import com.ca.tas.testbed.ITestbed;
import com.ca.tas.testbed.ITestbedMachine;
import com.ca.tas.testbed.TestBedUtils;
import com.ca.tas.testbed.Testbed;
import com.ca.tas.tests.annotations.TestBedDefinition;

@TestBedDefinition
public class NodeJSLoadTestbed extends NodeJSAgentTestbed {

    public static final String TEST_DURATION_ENV_VAR    = "SYSTEM_TEST_DURATION";
    public static final String EMAIL_RECIPIENTS_ENV_VAR = "EMAIL_RECIPIENTS";
	public static final String EM_MACHINE_ID            = "emMachine";
    public static final String EM_ROLE_ID               = "emRole";     
    private static final String EM_MM_ROLE_ID           = "emMMRole";    
    private static final Logger LOGGER                  = LoggerFactory.getLogger(NodeJSLoadTestbed.class);

	@Override
	public ITestbed create(ITasResolver tasResolver) {

		// mysql role
		MysqlRole mysqlRole = createMySqlRole();

		// nodejs & tixchange roles
		NodeJsRole nodeJsRole = createNodeJsRole(tasResolver);
	    TixChangeRole tixChangeRole = createTixChangeRole(tasResolver, mysqlRole, nodeJsRole);

		// redis & tomcat roles
		RedisRole redisRole = creatRedisRole(tasResolver);
		
		// mongodb role
        MongoDBRole mongodbRole = createMongoDBRole();
		
		TomcatRole tomcatRole = createTomcatRole(tasResolver);

		// role for configuring tixchangeNode app on tomcat
		ExecutionRole configRole = createIndexFileModificationRole(tasResolver,
				TIXCHANGE_NODE_CONTEXT, tomcatRole);

		//create EM role
        EmRole emRole = new EmRole.LinuxBuilder(EM_ROLE_ID, tasResolver).build();
        IRole mmRole = createMMRole(tasResolver, emRole);
        emRole.before(mmRole);
        mmRole.after(emRole);
        
        String emHost = tasResolver.getHostnameById(emRole.getRoleId());
        UMAgentRole umAgentRole = createUMAgentRole(tasResolver, emHost, emRole.getEmPort());
		
		NodeJSProbeRole probeRole = createNodeJsProbeRole(tasResolver, umAgentRole, tixChangeRole,
		    nodeJsRole, TIXCHANGE_PROBE_ROLE_ID);
		
		//create jmeter roles
		ArrayList<IRole> jmeterRoles = SimpleJmeterTestbed.createJmeterRoles(tasResolver);
        
		//add roles/machines
        tixChangeRole.before(nodeJsRole, mysqlRole, redisRole, mongodbRole);
        probeRole.after(tixChangeRole, nodeJsRole);
        configRole.after(tomcatRole);
		ITestbedMachine nodeMachine = TestBedUtils.createLinuxMachine(NODEJS_MACHINE, TEMPLATE_ID,
		               redisRole, mongodbRole, mysqlRole, nodeJsRole, tixChangeRole, 
		               probeRole, tomcatRole, configRole, umAgentRole, emRole, mmRole);
		
		for (IRole role: jmeterRoles) {
		    nodeMachine.addRole(role);
	    }  
		
		ITestbed testbed = new Testbed(getClass().getSimpleName());		
		testbed.addMachine(nodeMachine);
		
		//update env properties 
        Map<String, String> env = System.getenv();
        LOGGER.debug("*** Environment variables ***");
        for (String envName : env.keySet()) {
            LOGGER.debug("{}={}", envName, env.get(envName));
        }
        String duration = env.get(TEST_DURATION_ENV_VAR);
        String emailRecipients = env.get(EMAIL_RECIPIENTS_ENV_VAR);

        if (duration != null) {
            testbed.addProperty(TEST_DURATION_ENV_VAR, duration);
        }        
        if(emailRecipients != null) {
            testbed.addProperty(EMAIL_RECIPIENTS_ENV_VAR, emailRecipients);
        }
        
        return testbed;
	}
	
	public static GenericRole createMMRole(ITasResolver tasResolver,
	                                        EmRole emRole) {
        
        DefaultArtifact zip = new DefaultArtifact("com.ca.apm.tests",
            "nodeagent-tests-core",
            "em",
            "zip",
            tasResolver.getDefaultVersion());
        
        String emInstall = emRole.getEnvPropertyById("USER_INSTALL_DIR");
        GenericRole mmRole = new GenericRole.Builder(EM_MM_ROLE_ID, tasResolver)
        .unpack(zip, emInstall + "/deploy") 
        .build();
        
        return mmRole;        
    }
	
	protected NodeJsRole createNodeJsRole(ITasResolver tasResolver) {
        return new NodeJsRole.LinuxBuilder(NODEJS_ROLE_ID, tasResolver)
                .versionNodeJs(NodeJSRuntimeVersionArtifact.LINUXx64v4_3_1.getArtifact())
                .install("forever", Collections.singletonList("-g")).build();
    }
}
