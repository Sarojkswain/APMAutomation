package com.ca.apm.systemtest.fld.test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.TimeUnit;

import org.apache.http.util.Args;
import org.codehaus.plexus.archiver.AbstractArchiver;
import org.codehaus.plexus.archiver.ArchiverException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;

import com.ca.apm.automation.action.flow.commandline.RunCommandFlowContext;
import com.ca.apm.automation.action.flow.em.DeployEMFlowContext;
import com.ca.apm.automation.action.flow.utility.FileModifierFlow;
import com.ca.apm.automation.action.flow.utility.FileModifierFlowContext;
import com.ca.apm.automation.action.test.ClwRunner2;
import com.ca.apm.automation.action.test.ClwUtils2;
import com.ca.apm.systemtest.fld.flow.HammondReadCommandFlowContextBuilder;
import com.ca.apm.systemtest.fld.role.HammondInstallRole;
import com.ca.apm.systemtest.fld.role.loads.ParametrizedJMeterLoadRole;
import com.ca.apm.systemtest.fld.testbed.FLDConstants;
import com.ca.apm.systemtest.fld.testbed.FLDLoadConstants;
import com.ca.apm.systemtest.fld.testbed.TTStormLoadRecordingTestbedProvider;
import com.ca.apm.systemtest.fld.testbed.TransactionTraceStormLoadRecordingTestbed;
import com.ca.apm.systemtest.fld.testbed.loads.TTStormLoadProvider;
import com.ca.apm.systemtest.fld.util.ArchiveUtils;
import com.ca.apm.systemtest.fld.util.ArchiveUtils.ArchiveCompression;
import com.ca.apm.systemtest.fld.util.ArchiveUtils.ArchiveEntry;
import com.ca.apm.systemtest.fld.util.ArchiveUtils.ArchiveType;
import com.ca.tas.role.EmRole;
import com.ca.tas.test.TasTestNgTest;
import com.ca.tas.tests.annotations.Tas;
import com.ca.tas.tests.annotations.TestBed;
import com.ca.tas.type.ExclusivityType;
import com.ca.tas.type.SizeType;

/**
 * TAS test which performs the following steps: 
 * <ul>
 *     <li>finishes setup of transaction trace storm load recording testbed: configures and starts Nginx</li>
 *     <li>starts Jmeter load</li>
 *     <li>Once in 30 mins starts transaction tracing using CLWorkstation.jar and runs it for 10 minutes</li>
 *     <li>Runs like this for 5 hours continuing recording transaction trace sessions the way described above</li>
 *     <li>Stops EM, Jmeter load</li>
 *     <li>Extracts transaction traces and any metrics from EM using Hammond reader feature</li>
 *     <li>Archives the extract into a ZIP archive</li>
 * </ul>
 * 
 * <p/>
 * The obtained resultant Hammond extract can be uploaded to Artifactory afterwords for the next step - performance testing when 
 * this extract can be scaled by Hammond to play it against a single EM for simulating transaction trace storm load. 
 * 
 * @author Alexander Sinyushkin (sinal04@ca.com)
 */
@Test
public class TTStormLoadRecordingTest extends TasTestNgTest implements FLDLoadConstants, FLDConstants {
    public static final String TOMCAT_SERVER_TEMPLATE = "        server %s:8080;";
    public static final String NGINX_CONF_FILE = "/usr/local/nginx/conf/nginx.conf";
    public static final String NGINX_EXECUTABLE_FILE = "/usr/local/nginx/sbin/nginx";
    

    private static final long RUN_PERIOD = 180000l;//5 hours
    private static final String TRACES_FOLDER_PATH = "C:/storm-load-txn-traces"; 
    private static final String HAMMOND_OUTPUT_PATH = "C:/hammond-data";
    private static final String HAMMOND_EXTRACT_NAME = "tt-load-hammond-extract.zip";
    
    private static final Logger LOG = LoggerFactory.getLogger(TTStormLoadRecordingTest.class);
    
    @Tas(testBeds = @TestBed(name = TransactionTraceStormLoadRecordingTestbed.class, executeOn = TTStormLoadRecordingTestbedProvider.EM_MACHINE_ID), 
        owner = "sinal04", 
        size = SizeType.DEBUG, 
        exclusivity = ExclusivityType.EXCLUSIVE)
    @Test
    public void testTTStormLoadRecording() throws Exception {
        //Stop Nginx
        RunCommandFlowContext stopNginxCtx = new RunCommandFlowContext.Builder(NGINX_EXECUTABLE_FILE).args(Arrays.asList("-s", "stop")).build();
        runCommandFlowByMachineId(TTStormLoadRecordingTestbedProvider.NGINX_MACHINE_ID, stopNginxCtx);
        
        //Recreate nginx.conf with upstream config for our Tomcat servers
        Collection<String> rows = getNginxConfRows();
        
        FileModifierFlowContext createNginxConfFlowContext = new FileModifierFlowContext.Builder().create(NGINX_CONF_FILE, rows).build();
        runFlowByMachineId(TTStormLoadRecordingTestbedProvider.NGINX_MACHINE_ID, FileModifierFlow.class, createNginxConfFlowContext);
    
        //Start Nginx
        RunCommandFlowContext startNginxCtx = new RunCommandFlowContext.Builder(NGINX_EXECUTABLE_FILE).build();
        runCommandFlowByMachineId(TTStormLoadRecordingTestbedProvider.NGINX_MACHINE_ID, startNginxCtx);
        
        long startTimeMillis = System.currentTimeMillis();
        
        runSerializedCommandFlowFromRoleAsync(TTStormLoadProvider.JMETER1_LOAD_ROLE_ID, ParametrizedJMeterLoadRole.START_LOAD_FLOW_KEY, TimeUnit.HOURS, 2);
        
        //When using two separate Jmeter load machines, uncomment the below row 
        //runSerializedCommandFlowFromRoleAsync(TTStormLoadProvider.JMETER2_LOAD_ROLE_ID, ParametrizedJMeterLoadRole.START_LOAD_FLOW_KEY, TimeUnit.HOURS, 5);

        ClwUtils2 emClwUtils = createClwUtils(TTStormLoadRecordingTestbedProvider.EM_ROLE);
        long startTime = System.currentTimeMillis();
        while (true) {
            runTxnSessionRecording(emClwUtils);
            
            if (System.currentTimeMillis() - startTime > RUN_PERIOD) {
                LOG.info("Stopping transaction trace load recording...");
                break;
            }
        }
        
        LOG.info("Stopping WebView...");
        //Stop WebView, EM
        runSerializedCommandFlowFromRole(TTStormLoadRecordingTestbedProvider.EM_ROLE, EmRole.ENV_STOP_WEBVIEW);
        LOG.info("Stopping EM...");
        runSerializedCommandFlowFromRole(TTStormLoadRecordingTestbedProvider.EM_ROLE, EmRole.ENV_STOP_EM);
        
        long stopTimeMillis = System.currentTimeMillis();
        
        LOG.info("Stopping Jmeter load...");
        //Stop Jmeter
        runSerializedCommandFlowFromRole(TTStormLoadProvider.JMETER1_LOAD_ROLE_ID, ParametrizedJMeterLoadRole.STOP_LOAD_FLOW_KEY, TimeUnit.MINUTES, 5);
        
        LOG.info("Extracting EM traces & metrics with Hammond");
        extractLoadWithHammond(startTimeMillis, stopTimeMillis);
        
        LOG.info("Archiving the Hammond extract into a ZIP file");
        String archivePath = archiveExtract();
        LOG.info("ZIP file created at '{}'", archivePath);
    }

    /**
     * Archives extracted load into a ZIP archive. 
     * 
     * @return archive absolute path
     * @throws ArchiverException
     * @throws IOException
     */
    private String archiveExtract() throws ArchiverException, IOException {
        //Archive the extract 
        Collection<ArchiveEntry> archiveEntries = new ArrayList<>(1);
        ArchiveEntry archiveEntry = ArchiveEntry.directory(HAMMOND_OUTPUT_PATH);
        archiveEntries.add(archiveEntry);

        Path archiveParentFolderPath = Paths.get("C:", "hammond", "extract");
        archiveParentFolderPath.toFile().mkdirs();
        
        String archivePath = Paths.get("C:", "hammond", "extract", HAMMOND_EXTRACT_NAME).toAbsolutePath().toString();
        AbstractArchiver zipArchiver = ArchiveUtils.prepareArchiver(archivePath.toString(), 
            ArchiveType.ZIP, ArchiveCompression.DEFAULT, archiveEntries);
        zipArchiver.createArchive();
        return archivePath;
    }
    
    /**
     * Extracts the recorded load from EM using Hammond reader.
     * 
     * @param startTimeMillis  value for Hammond's option <code>--from=</code> 
     * @param stopTimeMillis   value for Hammond's option <code>--to=</code>
     */
    private void extractLoadWithHammond(long startTimeMillis, long stopTimeMillis) {
        
        File hammondOutputFolder = new File(HAMMOND_OUTPUT_PATH);
        hammondOutputFolder.mkdirs();
        
        RunCommandFlowContext hammondReadCommandCtx = new HammondReadCommandFlowContextBuilder()
                                .installDir(envProperties.getRolePropertyById(TTStormLoadRecordingTestbedProvider.HAMMOND_READER_ROLE, HammondInstallRole.ENV_HAMMOND_HOME))
                                .fromMillis(startTimeMillis)
                                .toMillis(stopTimeMillis)
                                .inputFolderPath(envProperties.getRolePropertyById(TTStormLoadRecordingTestbedProvider.EM_ROLE, 
                                    EmRole.ENV_PROPERTY_INSTALL_DIR))
                                .outputFolderPath(HAMMOND_OUTPUT_PATH)
                                .build(); 

        runCommandFlowByMachineId(TTStormLoadRecordingTestbedProvider.EM_MACHINE_ID, hammondReadCommandCtx);
    }
    
    private void runTxnSessionRecording(ClwUtils2 emClwUtils) throws InterruptedException {
        LOG.info("Sleeping for 20 minutes");
        Thread.sleep(2*60*1000);

        //Trace transactions exceeding 10 milliseconds for 600 seconds (=10 minutes)
        emClwUtils.traceTransactions(".*", 10, 60);
    }
    
    public ClwUtils2 createClwUtils(String emRoleId) {
        Args.notNull(emRoleId, "EM role ID cannot be null");
        return new ClwUtils2(createLocalClwRunner2(emRoleId));
    }

    private ClwRunner2 createLocalClwRunner2(String emRoleId) {
        Args.notNull(emRoleId, "EM role ID cannot be null");
        String host = envProperties.getMachineHostnameByRoleId(emRoleId);
        int port = Integer.parseInt(envProperties.getRolePropertyById(emRoleId, DeployEMFlowContext.ENV_EM_PORT));
        String emLibDir = envProperties.getRolePropertyById(emRoleId, DeployEMFlowContext.ENV_EM_LIB_DIR);
        ClwRunner2.Builder clwBuilder = new ClwRunner2.Builder();
        File tracesFolder = new File(TRACES_FOLDER_PATH);
        tracesFolder.mkdirs();
        clwBuilder.host(host)
                  .port(port)
                  .addTransactionTraceProperty("introscope.clw.tt.dirname", tracesFolder.getAbsolutePath())
                  .clwWorkStationDir(emLibDir)
                  .maxHeapSizeInMb(4096);

        return clwBuilder.build();
        
    }
    
    /**
     * Returns rows to write into <code>nginx.conf</code> to setup it as a balance 
     * loader for Tomcat instances deployed in the testbed.
     * 
     * @return  <code>nginx.conf</code> config file content rows as a collection
     */
    private Collection<String> getNginxConfRows() {
        Collection<String> rows = new ArrayList<String>(17 + TTStormLoadRecordingTestbedProvider.NUM_OF_AGENTS_PER_COLLECTOR);
        rows.add("events {");
        rows.add("    worker_connections 1024;");
        rows.add("}");
        rows.add("http {");
        rows.add("    upstream tomcat {");
        for (int i = 1; i <= TTStormLoadRecordingTestbedProvider.NUM_OF_AGENTS_PER_COLLECTOR; i++) {
            String tomcatRoleId = String.format(TTStormLoadRecordingTestbedProvider.TOMCAT_ROLE_ID_TEMPLATE, i);
            String tomcatHostName = envProperties.getMachineHostnameByRoleId(tomcatRoleId);
            rows.add(String.format(TOMCAT_SERVER_TEMPLATE, tomcatHostName));
        }
        rows.add("    }");
        rows.add("    server {");
        rows.add("        listen 80;");
        rows.add("        server_name tt-stormer-server;");
        rows.add("        location /tt-stormer/ {");
        rows.add("            proxy_pass http://tomcat;");
        rows.add("        }");
        rows.add("        location = / {");
        rows.add("            return 302 /tt-stormer/;");
        rows.add("        }");
        rows.add("    }");
        rows.add("}");
        return rows;
    }
}
