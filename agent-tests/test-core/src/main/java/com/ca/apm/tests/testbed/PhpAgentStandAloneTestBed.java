/*
 * Copyright (c) 2016 CA. All rights reserved.
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
 */

package com.ca.apm.tests.testbed;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import org.apache.commons.io.FilenameUtils;
import org.jetbrains.annotations.NotNull;

import com.ca.apm.automation.action.flow.testng.RunTestNgFlowContext;
import com.ca.apm.tests.artifact.MagentoSampleDataVersion;
import com.ca.apm.tests.artifact.MagentoVersion;
import com.ca.apm.tests.role.ApacheRole;
import com.ca.apm.tests.role.PhpClientDeployRole;
import com.ca.apm.tests.role.MagentoRole;
import com.ca.apm.tests.role.PhpAgentRole;
import com.ca.apm.tests.role.PhpAgentRole.PhpVersion;
import com.ca.apm.tests.role.PhpRole;
import com.ca.tas.artifact.thirdParty.JavaBinary;
import com.ca.tas.builder.TasBuilder;
import com.ca.tas.resolver.ITasResolver;
import com.ca.tas.role.EmRole;
import com.ca.tas.role.MysqlRole;
import com.ca.tas.role.webapp.JavaRole;
import com.ca.tas.testbed.ITestbed;
import com.ca.tas.testbed.ITestbedFactory;
import com.ca.tas.testbed.ITestbedMachine;
import com.ca.tas.testbed.TestBedUtils;
import com.ca.tas.testbed.Testbed;
import com.ca.tas.tests.annotations.TestBedDefinition;

/**
 * Test bed that installs
 *
 * on a CentOS machine
 * 1. Stand Alone EM
 * 2. Magento test application (+ Apache, PHP, MySQL)
 * 3. PHP agent (= Collector Agent + PHP probe)
 * 
  * @author Aleem Ahmad (ahmal01@ca.com)
 */
@TestBedDefinition
public class PhpAgentStandAloneTestBed implements ITestbedFactory {

    private static final String PHP_TEMPLATE_ID     = ITestbedMachine.TEMPLATE_CO66;
    public static final String PHP_MACHINE          = "phpMachine";
    private static final String MYSQL_ROLE_ID       = "mysqlRole";
    public static final String MAGENTO_ROLE_ID      = "magentoRole";
    public static final String APACHE_ROLE_ID       = "apacheRole";
    public static final String PHP_ROLE_ID          = "phpRole";
    public static final String PHP_AGENT_ROLE_ID    = "phpAgentRole";

    protected static final String DEPLOY_BASE       = TasBuilder.LINUX_SOFTWARE_LOC;
    public static final String RESULTS_DIR         = "testlogs";

    public static final String EM_ROLE_ID           = "emRole";
    //public static String agentVersion               = "";
    
    public final static String EMAIL_RECIPIENTS_DEFAULT         = "aleem.ahmad@ca.com Agent-Automation-Results@km.ca.com";
    protected String emailRecipients                            = EMAIL_RECIPIENTS_DEFAULT;
    protected final static String EMAIL_SENDER                  = "Team-APM-PhPAgent@ca.com";
    protected final static String SMTP_SERVER                   = "mail.ca.com";
    private JavaRole javaRole;
    private PhpVersion phpVersion = PhpVersion.PHP53;

    @Override
    public ITestbed create(ITasResolver tasResolver) {

        final String magentoUnpackDest = "/var/www/html/magento";
        final String magentoSampleDataUnpackDest = 
                TasBuilder.LINUX_SOFTWARE_LOC + "magento-sample-data";
        
        ITestbedMachine phpMachine = 
                TestBedUtils.createLinuxMachine(PHP_MACHINE, PHP_TEMPLATE_ID);
        
        ITestbed testBed = 
                new Testbed(getClass().getSimpleName()).addMachine(phpMachine);
        
        // java role
        javaRole = new JavaRole.Builder("java7Role", tasResolver).version(JavaBinary.LINUX_64BIT_JRE_17).build();
        
        // apache role
        ApacheRole apacheRole = 
                new ApacheRole.LinuxBuilder(APACHE_ROLE_ID).build();

        // php role
        PhpRole phpRole =
            new PhpRole.LinuxBuilder(PHP_ROLE_ID).withMySql().withMCrypt().withXml().withGd()
                .build();

        // mysql role
        String sqlFile = getSqlScriptLocation(magentoSampleDataUnpackDest);
        MysqlRole mysqlRole =
            new MysqlRole.LinuxBuilder(MYSQL_ROLE_ID).autoStart().build();

        // magento role
        MagentoRole magentoRole =
            new MagentoRole.LinuxBuilder(MAGENTO_ROLE_ID, tasResolver)
                .magentoVersion(MagentoVersion.v1_9_2_0).magentoDestination(magentoUnpackDest)
                .magentoSampleDataVersion(MagentoSampleDataVersion.v1_9_1_0)
                .magentoSampleDataDestination(magentoSampleDataUnpackDest)
                .mysqlRole(mysqlRole).sqlImportScript(sqlFile).apacheRole(apacheRole).build();

        // php agent role
        PhpAgentRole phpAgentRole =
            new PhpAgentRole.LinuxBuilder(PHP_AGENT_ROLE_ID, tasResolver)
                .phpVersion(phpVersion)
                .phpExtDirPath("/usr/lib64/php/modules")
                .phpExtConfDirPath("/etc/php.d")
                .deployBase(DEPLOY_BASE)
                .collectorAgentAutoStart().build();

        // EM role
        EmRole emRole =
            new EmRole.LinuxBuilder(EM_ROLE_ID, tasResolver).build();
        
        PhpClientDeployRole phpClientDeployRole = 
                new PhpClientDeployRole.Builder("php_client01", tasResolver).build();

        phpRole.after(javaRole,apacheRole);

        magentoRole.after(phpRole, mysqlRole);

        phpAgentRole.after(magentoRole, phpClientDeployRole);

        emRole.before(phpAgentRole);

        phpMachine.addRole(javaRole, apacheRole, phpRole, mysqlRole, magentoRole, emRole, phpAgentRole, phpClientDeployRole);
        
        initSystemProperties(tasResolver, testBed, new HashMap<String,String>());
        
        return testBed;
    }

    @NotNull
    private String getSqlScriptLocation(String unpackDest) {
        return unpackDest + TasBuilder.LINUX_SEPARATOR
            + MagentoSampleDataVersion.v1_9_1_0.getSqlFileWithinArchive();
    }
    
    protected void initSystemProperties(ITasResolver tasResolver, 
                                        ITestbed testBed, 
                                        HashMap<String,String> props) {
        
        String host = tasResolver.getHostnameById(PHP_ROLE_ID);
        initGenericSystemProperties(tasResolver, testBed, props, host);
        initPhpSystemProperties(host, props); 
        setTestngCustomJvmArgs(props, testBed);   

    }

    @NotNull
    protected void initGenericSystemProperties(ITasResolver tasResolver, 
                                               ITestbed testBed, 
                                               HashMap<String,String> props, String host) {
            
        String parentDir = codifyPath(DEPLOY_BASE);
        //testng   
        props.put("data.file", "testng_php.csv");
        props.put("test.priority.exclusive", "false"); 
       // props.put("role_client.test.priority", "1"); //enabling here will overwrite suite level
        props.put("client.dir", parentDir + "client/");
        props.put("results.dir", parentDir + RESULTS_DIR + "/");
        props.put("log4j.configuration", "file:" + parentDir + "client/lib/log4j-testng-agent.properties");
        props.put("config.location", "file");
        props.put("org.uncommons.reportng.title", "Agent_Test_Results");    
        props.put("default.cqapps.dir", parentDir + "client/resources/cqapps"); 
        props.put("db.servers.data.files", "sc_dbserver_data.csv");      
        //em & agent
        props.put("testbed_em.hostname", tasResolver.getHostnameById(EM_ROLE_ID));
        props.put("role_em.port", "5001");  
        props.put("mockem.port", "5002");   
        props.put("common.scp.user", testBed.getMachineById(PHP_MACHINE).getLocalSSHUserName());
        props.put("common.scp.password", testBed.getMachineById(PHP_MACHINE).getLocalSSHPassword());
        //props.put("agent.build.number", agentVersion); 
        props.put("browseragent.enabled", "true"); 
        
        //misc
        props.put("role_client.jmeter.install.dir", parentDir + "jmeter/apache-jmeter-2.11");
        props.put("java_v2.email.feature.enabled", "true"); 
        props.put("java_v2.email.recipients", emailRecipients.replace(" ", ","));
        props.put("java_v2.email.smtp.server", SMTP_SERVER); 
        props.put("java_v2.email.sender", EMAIL_SENDER); 
    }

    protected void initPhpSystemProperties(String host,
                                           HashMap<String,String> props) {
        //php system properties
        String home = codifyPath(DEPLOY_BASE + "phpAgent/collector");
        
        props.put("testbed_client.hostname", host);       
        props.put("role_agent.install.dir", DEPLOY_BASE + "phpAgent");
        props.put("role_agent.agent.profile", "IntroscopeCollectorAgent.profile");
        props.put("role_agent.config.dir", "collector/core/config/");
        
        props.put("role_webapp.container.type", "php");
        props.put("role_webapp.home.dir", home);
        props.put("role_webapp.port", "80");
        props.put("role_testbed.port", "80");
        props.put("testbed_webapp.hostname", host);
        props.put("role_webapp.appserver.dir", DEPLOY_BASE + "phpAgent/collector");
        props.put("php.version", phpVersion.toString());
        props.put("php.probe.config.dir", "/etc/php.d");
        props.put("role_webapp.server.name", "server");
        props.put("org.uncommons.reportng.title", "PHP Agent Test Results");
        props.put("role_webapp.appserver.jvm.home", javaRole.getInstallDir());
 
        
    }
    
    protected void setTestngCustomJvmArgs(HashMap<String,String> map, ITestbed testBed) {
        updateTestBedProps(map, testBed);
    }
    
    protected void updateTestBedProps(HashMap<String,String> map, ITestbed testBed) {
        
        HashSet<String> props = new HashSet<String>();
        
        for (Map.Entry<String, String> entry : map.entrySet()) {
            props.add("-D" + entry.getKey() + "=" + entry.getValue());
        }        
        testBed.addProperty(RunTestNgFlowContext.CUSTOM_JAVA_ARGS, props);  
    }
   
    @NotNull
    protected String codifyPath(String path) {
        return FilenameUtils.separatorsToUnix(path);
    }
    
}
