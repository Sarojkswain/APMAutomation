package com.ca.apm.tests.testbed;

import static com.ca.tas.testbed.ITestbedMachine.TEMPLATE_RH7;
import static com.ca.tas.testbed.ITestbedMachine.TEMPLATE_W64;

import java.util.Collections;

import org.eclipse.aether.artifact.Artifact;
import org.eclipse.aether.artifact.DefaultArtifact;

import com.ca.apm.tests.artifact.NodeJSProbeArtifact;
import com.ca.apm.tests.artifact.NodeJSRuntimeVersionArtifact;
import com.ca.apm.tests.role.MongoDBRole;
import com.ca.apm.tests.role.NodeJSAppRole;
import com.ca.apm.tests.role.NodeJSProbeRole;
import com.ca.apm.tests.role.TixChangeRole;
import com.ca.tas.builder.TasBuilder;
import com.ca.tas.resolver.ITasResolver;
import com.ca.tas.role.MysqlRole;
import com.ca.tas.role.NodeJsRole;
import com.ca.tas.role.RedisRole;
import com.ca.tas.role.utility.GenericRole;
import com.ca.tas.role.utility.UniversalRole;
import com.ca.tas.testbed.ITestbedMachine;
import com.ca.tas.testbed.TestbedMachine;
import com.ca.tas.tests.annotations.TestBedDefinition;

/**
 * Testbed for NodeJs Agent
 * 
 * @author kurma05
 */
@TestBedDefinition
public class NodeJsRedHat7Testbed extends BaseTestbed {
   
    public static final String TIXCHANGE_NODE_CONTEXT     = "tixchangeNode";
    public static final String MYSQL_ROLE_ID              = "mysqlRole";
    public static final String NODEJS_ROLE_ID             = "nodejsRole";
    public static final String TIXCHANGE_ROLE_ID          = "tixchangeRole";
    public static final String REDIS_ROLE_ID              = "redisRole";
    public static final String TIXCHANGE_PROBE_ROLE_ID    = "tixChangeProbeRole";
    public static final String NODEJS_PROBE_ARTIFACT_NAME = "nodeJsProbeArtifact";
    public static final String MONGODB_ROLE_ID            = "mongodbRole";
    private static final String TIXCHANGE_VERSION         = "0.1-SNAPSHOT";
    private static final String PROBE_VERSION             = "1.0-SNAPSHOT";
    public static final String JMETER_SCRIPTS_ROLE_ID     = "jmeterScriptsRole"; 
    public static final String JMETER_HOME                = WIN_DEPLOY_BASE + "/jmeter/apache-jmeter-3.1";
    public static final String JMETER_SCRIPTS_HOME        = WIN_DEPLOY_BASE + "/jmeter/scripts";

    @Override
    protected void initMachines(ITasResolver tasResolver) {
        
	    // add machines
        TestbedMachine machine1 =
            new TestbedMachine.Builder(MACHINE1_ID).templateId(TEMPLATE_W64).build();
        TestbedMachine machine2 =
            new TestbedMachine.Builder(MACHINE2_ID).templateId(TEMPLATE_RH7).build();
        
        shouldDeployJmeter = true;
        setupJarvis = true;
        addClientRoles(tasResolver, machine1);
        addJmeterScriptsRole(tasResolver, machine1); 
        addEMLinuxRole(tasResolver, machine2);
        addAgentRole(tasResolver, machine2);
        
		// mysql, nodejs & tixchange (testapp) roles
		MysqlRole mysqlRole = createMySqlRole();
		MongoDBRole mongodbRole = new MongoDBRole.LinuxBuilder(MONGODB_ROLE_ID).autoStart().build();
		NodeJsRole nodeJsRole = createNodeJsRole(tasResolver);
	    TixChangeRole tixChangeRole = createBasicTixChangeRoleBuilder(
	        tasResolver, mysqlRole, nodeJsRole).build();
	    
		// redis & nodejs probe roles
		RedisRole redisRole = new RedisRole.LinuxBuilder(REDIS_ROLE_ID, tasResolver)
		    .autoStart().build();	
		NodeJSProbeRole probeRole = createNodeJsProbeRole(tasResolver, tixChangeRole
				, nodeJsRole, TIXCHANGE_PROBE_ROLE_ID);
		
		//add roles/machines
        tixChangeRole.before(nodeJsRole, mysqlRole, redisRole, mongodbRole);
        probeRole.after(tixChangeRole, nodeJsRole);		
		machine2.addRole(redisRole, mysqlRole, mongodbRole, nodeJsRole, tixChangeRole, probeRole);
		testbed.addMachine(machine1, machine2);
	}
	
    private void addAgentRole(ITasResolver tasResolver, TestbedMachine machine2) {
        
        //get IA package   
        DefaultArtifact iaArtifact = new DefaultArtifact(
            "com.ca.apm.delivery", "APM-Infrastructure-Agent", "unix", 
            "tar.gz", tasResolver.getDefaultVersion());    
        UniversalRole iaRole = new UniversalRole.Builder(
            "iaRole", tasResolver)
            .unpack(iaArtifact, "/opt/apmia")
            .build();
        
        machine2.addRole(iaRole);        
    }
    
	private void addJmeterScriptsRole(ITasResolver tasResolver, ITestbedMachine machine) {
	    
	    testbed.addProperty("jmeter.install.dir", JMETER_HOME);
        testbed.addProperty("jmeter.scripts.install.dir", JMETER_SCRIPTS_HOME);
        
	    DefaultArtifact loadJmx =
            new DefaultArtifact("com.ca.apm.saas", "saas-tests-core", "jmeterscripts", "zip",
                tasResolver.getDefaultVersion()); 
        GenericRole role = new GenericRole.Builder(JMETER_SCRIPTS_ROLE_ID, tasResolver)
            .unpack(loadJmx, JMETER_SCRIPTS_HOME)
            .build();
        
        machine.addRole(role);
    }

    protected NodeJsRole createNodeJsRole(ITasResolver tasResolver) {
	    
        return new NodeJsRole.LinuxBuilder(NODEJS_ROLE_ID, tasResolver)
                .versionNodeJs(NodeJSRuntimeVersionArtifact.LINUXx64v4_3_1.getArtifact())
                .install("forever", Collections.singletonList("-g"))
                .build();
    }
	
	protected MysqlRole createMySqlRole() {
	    
        String unpackDest = TasBuilder.LINUX_SOFTWARE_LOC + TIXCHANGE_NODE_CONTEXT;
        String sqlFile = unpackDest + TasBuilder.LINUX_SEPARATOR + "nodetix.sql";
        return new MysqlRole.LinuxBuilder(MYSQL_ROLE_ID).importSql(sqlFile).autoStart().build();
    }
  
    protected TixChangeRole.Builder createBasicTixChangeRoleBuilder(ITasResolver tasResolver,
            MysqlRole mysqlRole, NodeJsRole nodeJsRole) {

        return new TixChangeRole.LinuxBuilder(TIXCHANGE_ROLE_ID, tasResolver)
                .version(TIXCHANGE_VERSION)
                .destination(TasBuilder.LINUX_SOFTWARE_LOC + TIXCHANGE_NODE_CONTEXT)
                .mysqlCreds(mysqlRole.getEnvPropertyById(MysqlRole.ENV_MYSQL_USERNAME),
                        mysqlRole.getEnvPropertyById(MysqlRole.ENV_MYSQL_PASSWORD))
                .node(nodeJsRole).configDatasources("nodetix.host", "localhost")
                .configConfig("useClickstream", "true");
    }
    
    protected NodeJSProbeRole createNodeJsProbeRole(ITasResolver tasResolver,
             NodeJSAppRole nodejsAppRole, NodeJsRole nodeJsRole, String roleId) {
        
        Artifact artifact = new NodeJSProbeArtifact(tasResolver)
            .createArtifact(PROBE_VERSION).getArtifact();
        NodeJSProbeRole role = new NodeJSProbeRole.LinuxBuilder(roleId, tasResolver)
                .version(artifact)
                .nodeJSAppRole(nodejsAppRole)
                .nodeJSRole(nodeJsRole)
                .build();
    
        String artifactName = tasResolver.getArtifactUrl(artifact).getFile();
        if (artifactName != null) {
            role.addProperty(NODEJS_PROBE_ARTIFACT_NAME, artifactName);
        }
        return role;
    }
}