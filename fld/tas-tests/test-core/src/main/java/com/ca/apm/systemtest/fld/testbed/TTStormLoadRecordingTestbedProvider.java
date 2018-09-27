package com.ca.apm.systemtest.fld.testbed;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ca.apm.systemtest.fld.artifact.TTStormerWebAppArtifact;
import com.ca.apm.systemtest.fld.role.HammondInstallRole;
import com.ca.tas.annotation.resource.RemoteResource;
import com.ca.tas.artifact.thirdParty.JavaBinary;
import com.ca.tas.artifact.thirdParty.TomcatVersion;
import com.ca.tas.resolver.ITasResolver;
import com.ca.tas.role.AgentRole;
import com.ca.tas.role.EmRole;
import com.ca.tas.role.IRole;
import com.ca.tas.role.utility.ExecutionRole;
import com.ca.tas.role.web.WebAppRole;
import com.ca.tas.role.webapp.JavaRole;
import com.ca.tas.role.webapp.NginxRole;
import com.ca.tas.role.webapp.TomcatRole;
import com.ca.tas.testbed.Bitness;
import com.ca.tas.testbed.ITestbed;
import com.ca.tas.testbed.ITestbedMachine;
import com.ca.tas.testbed.TestBedUtils;
import com.ca.tas.testbed.TestbedMachine;
import com.ca.tas.type.Platform;

/**
 * Provider for transaction trace storm load generating & recording testbed.
 *  
 * @author Alexander Sinyushkin (sinal04@ca.com)
 *
 */
public class TTStormLoadRecordingTestbedProvider implements FLDConstants, FLDLoadConstants, FldTestbedProvider {
    public static final String EM_MACHINE_ID = "EM_MACHINE_ID";
    public static final String NGINX_MACHINE_ID = "NGINX_MACHINE_ID";
    
    public static final String TOMCAT_MACHINE_ID_TEMPLATE = "TOMCAT%02d_MACHINE_ID";
    
    public static final String AGENT_ROLE_ID_TEMPLATE = "agent%02d_role";
    public static final String TOMCAT_ROLE_ID_TEMPLATE = "tomcat%02d_role";
    public static final String WEB_APP_ROLE_ID_TEMPLATE = "webapp%02d_role";
    public static final String JAVA8_ROLE_ID_TEMPLATE = "java8_%02d_role"; 

    public static final String EM_ROLE = "em_role";
    public static final String NGINX_ROLE = "nginx_role";
    public static final String START_WV_ROLE = "start_wv_role";
    public static final String START_EM_ROLE = "start_em_role";
    public static final String HAMMOND_READER_ROLE = "hammond_reader_role";
    
    public static final int NUM_OF_AGENTS_PER_COLLECTOR = 8;
    
    private static final String DB_USER = "admin";
    private static final String DB_PASSWORD = "quality";
    private static final String DB_ADMIN_USER = "postgres";
    private static final String DB_ADMIN_PASSWORD = "Password1";

    private static final String JDK8_PATH = "C:/java/jdk_18";
    private static final String HAMMOND_INSTALL_PATH = "C:/hammond";
    private static final String TOMCAT_SERVER_CONFIG_FILE_NAME = "server.xml";
    private static final String TOMCAT_SERVER_CONFIG_RESOURCE = "/tt-storm-load/server.xml";
    private static final String TEST_WEB_APP_DEPLOY_CONTEXT_NAME = "tt-stormer";  
    
    private static final String HAMMOND_EXTRACT_ROOT_FOLDER_PATH = "C:\\hammond\\extract"; 
    
    private static final Logger LOGGER = LoggerFactory.getLogger(TTStormLoadRecordingTestbedProvider.class);
    
    @Override
    public Collection<ITestbedMachine> initMachines() {
        List<ITestbedMachine> machines = new ArrayList<>(NUM_OF_AGENTS_PER_COLLECTOR + 2);
        
        TestbedMachine emMachine = new TestbedMachine.Builder(EM_MACHINE_ID)
            .platform(Platform.WINDOWS)
            .bitness(Bitness.b64)
            .templateId("w64")
            .build();
        
        machines.add(emMachine);
        ITestbedMachine nginxMachine = TestBedUtils.createLinuxMachine(NGINX_MACHINE_ID, ITestbedMachine.TEMPLATE_CO65);
        machines.add(nginxMachine);
        
        for (int i = 1; i <= NUM_OF_AGENTS_PER_COLLECTOR; i++) {
            String webServerMachineId = String.format(TOMCAT_MACHINE_ID_TEMPLATE, i);
            ITestbedMachine webServerMachine = TestBedUtils.createWindowsMachine(webServerMachineId, ITestbedMachine.TEMPLATE_W64);
            machines.add(webServerMachine);
        }
        
        return machines;
    }

    @Override
    public void initTestbed(ITestbed testbed, ITasResolver tasResolver) {
        Collection<String> laxOptions =
            Arrays.asList("-Xms4096m", "-XX:+UseConcMarkSweepGC", "-showversion", " -verbosegc",
                "-Dcom.wily.assert=false", "-Xmx8192m", "-Dmail.mime.charset=UTF-8",
                "-Dorg.owasp.esapi.resources=./config/esapi", "-XX:+UseParNewGC",
                "-XX:CMSInitiatingOccupancyFraction=50", "-XX:+HeapDumpOnOutOfMemoryError",
                "-Xss256k");

        String emHost = tasResolver.getHostnameById(EM_ROLE);

        EmRole emRole =
            new EmRole.Builder(EM_ROLE, tasResolver)
                .silentInstallChosenFeatures(Arrays.asList("Database", "Enterprise Manager", "WebView"))
                .nostartEM()
                .nostartWV()
                .dbAdminUser(DB_ADMIN_USER)
                .dbAdminPassword(DB_ADMIN_PASSWORD)
                .dbuser(DB_USER)
                .dbpassword(DB_PASSWORD)
                .emLaxNlClearJavaOption(laxOptions)
                .wvEmHost(emHost)
                .wvEmPort(5001)
                .wvPort(8080)
                .build();

        HammondInstallRole hammondRole = new HammondInstallRole.Builder(HAMMOND_READER_ROLE, tasResolver)
            .installDir(HAMMOND_INSTALL_PATH)
            .build();
        
        ITestbedMachine emMachine = testbed.getMachineById(EM_MACHINE_ID);
        
        ExecutionRole startWvRole = new ExecutionRole.Builder(START_WV_ROLE)
            .asyncCommand(emRole.getWvRunCommandFlowContext())
            .build();
    
        ExecutionRole startEmRole = new ExecutionRole.Builder(START_EM_ROLE)
            .asyncCommand(emRole.getEmRunCommandFlowContext())
            .build();
    
        startEmRole.after(emRole);
        startWvRole.after(startEmRole);
        hammondRole.after(startWvRole);
        emMachine.addRole(emRole, startEmRole, startWvRole, hammondRole);

        //Attach the archive to the Resman job remoteResources folder
        RemoteResource hammondExtractResource =
            RemoteResource.createFromRegExp(".*zip$", HAMMOND_EXTRACT_ROOT_FOLDER_PATH);
        emMachine.addRemoteResource(hammondExtractResource);
        
        ITestbedMachine nginxMachine = testbed.getMachineById(NGINX_MACHINE_ID);

        NginxRole nginxRole = new NginxRole.LinuxBuilder(NGINX_ROLE, tasResolver)
            .build();
        
        nginxMachine.addRole(nginxRole);
        List<String> tomcatServerConfigRows = null;
        try {
            tomcatServerConfigRows = getTomcatServerConfig();
        } catch (IOException e) {
            String errMsg = "Failed to read Tomcat's server.xml config file from resources: ";
            LOGGER.error(errMsg, e);
            throw new RuntimeException(errMsg, e);
        }
        
        for (int i = 1; i <= NUM_OF_AGENTS_PER_COLLECTOR; i++) {
            String webServerMachineId = String.format(TOMCAT_MACHINE_ID_TEMPLATE, i);
            ITestbedMachine webServerMachine = testbed.getMachineById(webServerMachineId);
            String javaRoleId = String.format(JAVA8_ROLE_ID_TEMPLATE, i);
            JavaRole javaRole = new JavaRole.Builder(javaRoleId, tasResolver)
                .dir(JDK8_PATH)
                .version(JavaBinary.WINDOWS_64BIT_JDK_18_0_51)
                .build();

            String webAppRoleId = String.format(WEB_APP_ROLE_ID_TEMPLATE, i);
            WebAppRole<TomcatRole> ttStormerWebAppRole = 
                new WebAppRole.Builder<TomcatRole>(webAppRoleId)
                    .artifact(new TTStormerWebAppArtifact(tasResolver).createArtifact()).cargoDeploy()
                    .contextName(TEST_WEB_APP_DEPLOY_CONTEXT_NAME).build();

            String tomcatRoleId = String.format(TOMCAT_ROLE_ID_TEMPLATE, i);
            TomcatRole tomcatRole = new TomcatRole.Builder(tomcatRoleId, tasResolver)
                    .customJava(javaRole)
                    .addConfigFile(TOMCAT_SERVER_CONFIG_FILE_NAME, tomcatServerConfigRows)
                    .webApp(ttStormerWebAppRole)
                    .tomcatVersion(TomcatVersion.v80)
                    .autoStart()
                    .build();
            javaRole.before(tomcatRole);
            tomcatRole.before(ttStormerWebAppRole);
            String agentRoleId = String.format(AGENT_ROLE_ID_TEMPLATE, i);
            
            IRole tomcatAgentRole =
                new AgentRole.Builder(agentRoleId, tasResolver)
                    .customName(agentRoleId)
                    .webAppServer(tomcatRole)
                    .emRole(emRole)
                    .customName(agentRoleId)
                    .build();
            tomcatRole.before(tomcatAgentRole);
            
            webServerMachine.addRole(javaRole, tomcatRole, ttStormerWebAppRole, tomcatAgentRole);
        }
        
    }

    private List<String> getTomcatServerConfig() throws IOException {
        return IOUtils.readLines(getClass().getResourceAsStream(TOMCAT_SERVER_CONFIG_RESOURCE));
    }

}
