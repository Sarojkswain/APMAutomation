/*
 * Copyright (c) 2015 CA.  All rights reserved.
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

package com.ca.apm.tests.system;

import static org.testng.Assert.assertTrue;

import java.io.File;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.time.DateFormatUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeSuite;

import com.ca.apm.automation.action.flow.em.DeployEMFlowContext;
import com.ca.apm.automation.action.test.LogUtils;
import com.ca.apm.automation.common.AutomationConstants;
import com.ca.apm.automation.common.EmailUtil;
import com.ca.apm.automation.common.JmeterUtil;
import com.ca.apm.automation.common.SystemProperties;
import com.ca.apm.automation.common.Util;
import com.ca.apm.automation.common.jass.JassConstants;
import com.ca.apm.tests.common.file.FileUtils;
import com.ca.apm.tests.common.introscope.util.CLWBean;
import com.ca.apm.tests.config.BaseAppConfig;
//import com.ca.apm.tests.config.CollectorAgentConfig;
//import com.ca.apm.tests.config.CollectorAgentConfig.AgentLoggingLevel;
import com.ca.apm.tests.config.LoggingLevel;
import com.ca.apm.tests.config.NodeJSProbeConfig;
import com.ca.apm.tests.config.TixChangeAppConfig;
import com.ca.apm.tests.config.UMAgentConfig;
import com.ca.apm.tests.config.UMAgentConfig.AgentLoggingLevel;
import com.ca.apm.tests.functional.BaseNodeAgentTest;
import com.ca.apm.tests.role.NodeJSProbeRole;
import com.ca.apm.tests.role.TixChangeRole;
import com.ca.apm.tests.role.UMAgentRole;
import com.ca.apm.tests.testbed.NodeJSAgentTestbed;
import com.ca.apm.tests.testbed.NodeJSLoadTestbed;
import com.ca.apm.tests.testbed.SimpleJmeterTestbed;
import com.ca.apm.tests.utils.CommonUtils;
import com.ca.apm.tests.utils.MetricsUtil;
/**
 * @author kurma05
 */
public class SystemBaseTest extends BaseNodeAgentTest {
    
	private static final Logger LOGGER                       = LoggerFactory.getLogger(SystemBaseTest.class);
	protected static final String DEFAULT_NODE_PROCESS_NAME  = "nodejs-probes";
	private static final String APM_REPORT_UNIX_PATH         = "/home/APMReport_NodeJs.pdf";
	protected String appPort                                 = null;
	protected String appHost                                 = null;
	private static final String EMAIL_RECIPIENTS             = "kurma05@ca.com,sinka08@ca.com";
    private Thread metricValidatorThread                     = null;
    private Thread bounceIAThread                            = null;
    protected String agentName                               = "{collector}({program})";
    protected Exception error                                = null;
    protected static final String DEFAULT_MIN_HEAP_VAL_IN_MB = "16";
    protected static final String DEFAULT_MAX_HEAP_VAL_IN_MB = "256";
    private Process clusterProcess                           = null;
    protected static final long DEFAULT_TEST_DURATION_MS     = 43200000;
    protected static final String OWNER                      = "kurma05";
    protected List<String> expectedErrorMessages             = null;
  
    @BeforeSuite(alwaysRun = true)
    @Override
    public void testSuiteSetup() {
        
    	umAgentConfig = new UMAgentConfig(iaHome);
    	
        probeConfig = new NodeJSProbeConfig(envProperties.getRolePropertyById(
                NodeJSAgentTestbed.TIXCHANGE_PROBE_ROLE_ID,
                NodeJSProbeRole.Builder.ENV_NODEJS_PROBE_HOME));
    
        String roleId = NodeJSAgentTestbed.TIXCHANGE_ROLE_ID;
		tixChangeConfig = new TixChangeAppConfig(envProperties.getRolePropertyById(roleId,
		        TixChangeRole.ENV_TIXCHANGE_SERVER_DIR), envProperties.getRolePropertyById(roleId,
		        TixChangeRole.ENV_TIXCHANGE_STARTUP_SCRIPT_PATH),
		        envProperties.getRolePropertyById(roleId,
		                TixChangeRole.ENV_TIXCHANGE_SERVER_LOG_FILE));      
        tixChangeConfig.setHost(envProperties.getMachineHostnameByRoleId(roleId));
        tixChangeConfig.setPort(envProperties.getRolePropertyById(roleId, TixChangeRole.ENV_TIXCHANGE_PORT));
        appUrlBase = tixChangeConfig.getAppUrlBase();
      
        BaseAppConfig[] appConfigs = { umAgentConfig };
        CommonUtils.createBackupOfAppConfig(appConfigs);
    }
    
	@BeforeClass(alwaysRun = true)
	@Override
	public void testClassSetup() {
	    
	    //create clw bean
		String emHost = envProperties.getMachineHostnameByRoleId(NodeJSLoadTestbed.EM_ROLE_ID);
	    int emPort = Integer.parseInt(envProperties.getRolePropertyById(NodeJSLoadTestbed.EM_ROLE_ID, DeployEMFlowContext.ENV_EM_PORT));	      
	    String emHome = envProperties.getRolePropertyById(NodeJSLoadTestbed.EM_ROLE_ID, DeployEMFlowContext.ENV_EM_INSTALL_DIR);
	    MetricsUtil.clw = new CLWBean (emHost,"Admin","",emPort,emHome + "/lib/CLWorkstation.jar");
	    
	    //init host/port 
        String roleId = NodeJSAgentTestbed.TIXCHANGE_ROLE_ID;
        appPort = envProperties.getRolePropertyById(roleId, TixChangeRole.ENV_TIXCHANGE_PORT);
        appHost = envProperties.getMachineHostnameByRoleId(roleId);
        
        //print all system props
        LOGGER.info("***** Environment variables *****");
        Map<String, String> env = System.getenv();
        for (String envName : env.keySet()) {
            LOGGER.info(envName + "=" + env.get(envName));
        }
	}
	
	@BeforeMethod
    public void executeBeforeMethod(Method method) {
        
        testMethodName = method.getName();
        agentName = testMethodName;
        
        //update probe config
        probeConfig.updateLogFileName("Probe." + testMethodName + LOG_FILE_EXT);
        probeConfig.updateLogLevel(LoggingLevel.INFO);
        
        //update uma agent config
        umAgentConfig.updateLogLevel(AgentLoggingLevel.VERBOSE);
        umAgentConfig.updateProbeCollectorLogLevel(AgentLoggingLevel.VERBOSE);
        umAgentConfig.updateLogFileName("Collector." + testMethodName + LOG_FILE_EXT);
        umAgentConfig.updateProperty(
            AutomationConstants.Agent.LOG_APPENDER_LOGFILE_MAX_FILESIZE_PROPERTY, "100MB");
        umAgentConfig.updateProperty("introscope.remoteagent.probe.agent.name", agentName);      
    }
	
	protected void setupTest() {
      
	    setJvmOptions();	    
        super.startCollectorAgent();
        super.startNodeApp();
    }
	
	protected void setupClusterTest(String numberWorkers) {
	    
	    setJvmOptions();
       
        try {
            installStrongLoop();
    	    installClusterModule();
    	    configureCluster();
    	    startCollectorAgent();
    	    startCluster(numberWorkers);
    	} catch (Exception e) {
            e.printStackTrace();
            Assert.fail("Error occurred during starting cluster: " + e.getMessage());
        } 
	}
	
	protected void startCluster(String numberWorkers) throws Exception {
	   
        String startupScript = envProperties
            .getRolePropertyById(NodeJSAgentTestbed.TIXCHANGE_ROLE_ID, 
                TixChangeRole.ENV_TIXCHANGE_STARTUP_SCRIPT_PATH);
        String homeDir = envProperties
            .getRolePropertyById(NodeJSAgentTestbed.TIXCHANGE_ROLE_ID, 
                TixChangeRole.ENV_TIXCHANGE_HOME_DIR);
        
        ArrayList<String> args = new ArrayList<String>();
        args.add(homeDir + "/node_modules/strongloop/bin/slc.js");
        args.add("run");
        args.add("--no-profile");
        args.add("--cluster");
        args.add(numberWorkers);
        args.add(startupScript);
        
        LOGGER.info("Command: " + args.toString().replace(",",""));
        clusterProcess = Util.invokeProcessBuilderNoWait(args);
    }
	
	protected void installStrongLoop() throws Exception {

        String homeDir = envProperties
                .getRolePropertyById(NodeJSAgentTestbed.TIXCHANGE_ROLE_ID, 
                    TixChangeRole.ENV_TIXCHANGE_HOME_DIR);
        
        ArrayList<String> args = new ArrayList<String>();
        args.add("npm");
        args.add("install");
        args.add("strongloop"); 
        args.add("--registry");
        args.add("https://registry.npmjs.org");
       
        ProcessBuilder pb = new ProcessBuilder(args);
        pb.redirectErrorStream(true);
        pb.directory(new File(homeDir));
       
        LOGGER.info("Command: " + args.toString().replace(",",""));
        StringBuffer output = Util.invokeProcessBuilder(pb);
        LOGGER.info(output.toString());
	}
	
	protected void installClusterModule() throws Exception {

        String homeDir = envProperties
                .getRolePropertyById(NodeJSAgentTestbed.TIXCHANGE_ROLE_ID, 
                    TixChangeRole.ENV_TIXCHANGE_HOME_DIR);
        
        ArrayList<String> args = new ArrayList<String>();       
        args.add("npm");
        args.add("install");
        args.add("strong-cluster-control");
        args.add("--registry");
        args.add("https://registry.npmjs.org");
        
        ProcessBuilder pb = new ProcessBuilder(args);
        pb.redirectErrorStream(true);
        pb.directory(new File(homeDir));
       
        LOGGER.info("Command: " + args.toString().replace(",",""));
        StringBuffer output = Util.invokeProcessBuilder(pb);
        LOGGER.info(output.toString());
    }

	protected void configureCluster() throws Exception {
	    
	    String filePath = envProperties
            .getRolePropertyById(NodeJSAgentTestbed.TIXCHANGE_ROLE_ID, 
                TixChangeRole.ENV_TIXCHANGE_HOME_DIR) + 
                   "/node_modules/strongloop/node_modules/strong-supervisor/node_modules/strong-cluster-control/index.js";
	    
	    String newPattern = "\nif \\(cluster.isMaster\\) require\\('ca-apm-probe'\\)\\({slc: cluster}\\);";
	    String orgPattern = "var cluster = require\\('cluster'\\);";
	    
	    FileUtils.replace(filePath, orgPattern, orgPattern + newPattern);
	}
	
    protected void setJvmOptions() {
        
        String args = "-XX:+UseSerialGC -XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=/home/" + testMethodName + "_dump.hprof"
                      + " -verbosegc -XX:+PrintGCDateStamps -XX:+PrintGCTimeStamps -XX:+PrintGCDetails -Xloggc:"
                      + "/home/" + testMethodName + "_gc.log";
            
        try {
            FileUtils.replace(umAgentConfig.getHome() + "/bin/" + UMAgentConfig.APM_IA_LINUX_SCRIPT_NAME, 
                "\\{JAVA_HOME\\}/bin/java", "\\{JAVA_HOME\\}/bin/java " + args);
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail(e.getMessage());
        } 
    }
    
    protected void updateHeapSettings(String minHeapMb,
                                      String maxHeapMb) {
           
        try {
            FileUtils.replace(umAgentConfig.getHome() + "/bin/" + UMAgentConfig.APM_IA_LINUX_SCRIPT_NAME, 
                "MIN_HEAP_VAL_IN_MB=.*", "MIN_HEAP_VAL_IN_MB=" + minHeapMb);
            FileUtils.replace(umAgentConfig.getHome() + "/bin/" + UMAgentConfig.APM_IA_LINUX_SCRIPT_NAME, 
                "MAX_HEAP_VAL_IN_MB=.*", "MAX_HEAP_VAL_IN_MB=" + maxHeapMb);
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail(e.getMessage());
        } 
    }
	
	protected void startMetricValidator (final HashMap<String, String> params, 
	                                     final String metric,
	                                     final long testDurationMs,
	                                     final String testName,
	                                     final int intervalMin) {
        
        metricValidatorThread = new Thread(new Runnable() {
            @Override
            public void run() {
                int countFailures = 0;
                int countCycles = 0;
                long verifyMetricsSleep = SystemProperties.getVerifyMetricsFrequency();  
                long startTime = System.currentTimeMillis();
                if (testDurationMs < verifyMetricsSleep) verifyMetricsSleep = testDurationMs; 
              
                try {
                    do {
                        try {   
                            LOGGER.info("[startMetricValidator] Sleeping for " + verifyMetricsSleep + " ms before next cycle ...");
                            Thread.sleep(verifyMetricsSleep);
                            LOGGER.info("[startMetricValidator] Metric Validation Cycle " + (++countCycles));                            
                            MetricsUtil.verifyNumberMetrics(params, metric, intervalMin, MetricsUtil.MetricValueType.AVG);                              
                        } catch (AssertionError ae) {
                            String message = "Metric validation failed for: " + metric + " - FAILURE #" + (++countFailures) +
                                    ". Max allowed failures is " + (JassConstants.VERIFY_METRICS_MAX_ALLOWED_FAILURES + " & test will be terminated!");
                            LOGGER.warn(message);
                            if (countFailures > JassConstants.VERIFY_METRICS_MAX_ALLOWED_FAILURES) {
                                String errorMessage = "[startMetricValidator] " + testName + " stopped as # of metric failures exceeded max allowed.";
                                error = new Exception(errorMessage);
                                LOGGER.error(errorMessage);
                                stopJmeterScript();                            
                                Thread.currentThread().interrupt(); break;
                            }
                            else {   
                                EmailUtil.sendEmail(
                                    SystemProperties.getEmailSmtpServer(), 
                                    SystemProperties.getEmailSender(), 
                                    EMAIL_RECIPIENTS, 
                                    "NodeJS System Tests for " + testName, 
                                    "[WARNING] " + testName + ": metric validation failure #" + countFailures,
                                    null);
                            }
                        }
                    } while ((System.currentTimeMillis() - startTime) < testDurationMs);
                } catch (InterruptedException ie) {
                    LOGGER.info("[startMetricValidator] thread has been interrupted: " + ie.getMessage());
                } catch (Exception ex) {
                    error = new Exception("Test '" + testName + "': Exception occurred ", ex);
                    stopJmeterScript();
                }
                LOGGER.info("[startMetricValidator] Metric Validator Stopped.");  
            }
        });        
        metricValidatorThread.start(); 
    }       
	
	protected void checkLog() {
	    
	    // verify collector agent successfully connected to EM
        String keyword = "Connected controllable Agent to the Introscope Enterprise Manager";
        LogUtils util = utilities.createLogUtils(umAgentConfig.getLogPath(), keyword);
        assertTrue(util.isKeywordInLog());
	}
	
	protected void stopJmeterScript() {         
       
	    try {
            JmeterUtil.stopJmeterScript(SimpleJmeterTestbed.JMETER_HOME, true);
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail(e.getMessage());
            error = new Exception(e);
        }
    }
	
	protected void startBounceIAThread(final long durationMs,
	                                          final long frequencyMs) {
        
	    bounceIAThread = new Thread(new Runnable() {
            @Override
            public void run() {
                long startTime = System.currentTimeMillis();
                long sleep = frequencyMs;
                if (durationMs < frequencyMs) sleep = durationMs; 
              
                try {
                    do {
                        LOGGER.info("[startBounceIAThread] Sleeping for " + frequencyMs + " ms before next cycle ...");
                        Thread.sleep(sleep);
                        Util.runOSCommand(umAgentConfig.getHome() + "/" + 
                            UMAgentRole.LinuxBuilder.UMA_AGENT_EXECUTABLE + " restart");                       
                    } while ((System.currentTimeMillis() - startTime) < durationMs);
                } catch (InterruptedException ie) {
                    LOGGER.info("[startBounceIAThread] thread has been interrupted: " + ie.getMessage());
                } catch (Exception ex) {
                    ex.printStackTrace();
                    error = new Exception("Test '" + testMethodName + "' exception occurred: " + ex.getMessage(), ex);
                }
                LOGGER.info("[startBounceIAThread] Bounce IA thread Stopped.");  
            }
        });        
	    bounceIAThread.start(); 
    }    
	
	private void emailAPMReport(long durationMs,
	                              String testName) throws Exception {

        long currentTime = System.currentTimeMillis();
        String endTime = DateFormatUtils.format(currentTime, "MM/dd/yy HH:mm:ss");
        String startTime = DateFormatUtils.format(currentTime - durationMs, "MM/dd/yy HH:mm:ss");

        //Generate report
        String command = "generate report named \"Node Report\" in management module named Node" + 
                         " in agents matching .* starting at "
                         + startTime + " ending at " + endTime + " to " + APM_REPORT_UNIX_PATH;
        MetricsUtil.clw.setMetricData(false);
        MetricsUtil.clw.runCLW(command);

        //Get recipients
        String emailRecipients = envProperties.getTestbedPropertyById(
                                     NodeJSLoadTestbed.EMAIL_RECIPIENTS_ENV_VAR);
        if(emailRecipients == null || emailRecipients.isEmpty()) {
            emailRecipients = EMAIL_RECIPIENTS;
        }
        
        //Get artifact version#
        String artifactName = envProperties.getRolePropertyById(NodeJSAgentTestbed.TIXCHANGE_PROBE_ROLE_ID, 
                                       NodeJSAgentTestbed.NODEJS_PROBE_ARTIFACT_NAME);
        String version = "";
        Matcher match = Pattern.compile("(.*nodejs-probe-)(.*)(\\.tgz)").matcher(artifactName);
        if (match.find()) {
            version = match.group(2);
        } 
        
        //Send report
        ArrayList<String> attachments = new ArrayList<String>();
        attachments.add(APM_REPORT_UNIX_PATH);
        
        EmailUtil.sendEmail(
            SystemProperties.getEmailSmtpServer(), 
            SystemProperties.getEmailSender(), 
            emailRecipients, 
            "[NodeJS Probe " + version + "] System Tests for " + testName, 
            "APM Node.js report attached",
            attachments);
    }
	
    private void stopLoad (Thread appThread) {

        if(appThread != null) {
            do {
                LOGGER.debug("Trying to interrupt application thread ...");
                appThread.interrupt();
                Util.sleep(10000);
            } while (appThread.isAlive());
        }
    }
	
	protected void testTixChangeGeneric(String metricExpr,                                     
                                      String jmeterScript) {
        
        testTixChangeGeneric(metricExpr, "500", jmeterScript);
    }
    
	protected void testTixChangeGeneric(String metricExpr,
                                        String minValue,
                                        String jmeterScript) {
        
        checkLog();
        long testDuration = getTestDuration();
        
        //start metric validator thread
        HashMap<String, String> params = new HashMap<String, String> ();
        params.put("minExpectedValue", minValue);
        params.put("average", "true");
        String metric = "*SuperDomain*|" + appHost + "|" + DEFAULT_NODE_PROCESS_NAME +
                        "|" + agentName + metricExpr;  
        long testStartTime = System.currentTimeMillis();
        
        startMetricValidator(params, metric, testDuration, testMethodName, 5);
        
        //run jmeter & send report
        try {
            Thread.sleep(30000); 
            Util.readInputStreamTimeoutEnabled = false;
            JmeterUtil.runJmeterScript(SimpleJmeterTestbed.JMETER_HOME, SimpleJmeterTestbed.JMETER_SCRIPTS_HOME + 
                                       "/" + jmeterScript, testDuration, true, appHost, appPort);
            emailAPMReport(testDuration, testMethodName);
            
            //assert here if error happened in another thread
            if (error != null) Assert.fail(error.getMessage());
            
            //assert if test stops before duration is over
            Assert.assertTrue((System.currentTimeMillis() - testStartTime) >= testDuration, 
                         "Test stopped before expected test duration. Check log for errors.");
            
            checkErrorInLogs();
        }
        catch (Exception e) {
            e.printStackTrace();
            Assert.fail("Exception occurred during test execution", e);
        }
        finally {
            expectedErrorMessages = null;
        }
    }
	    
	@Override
	protected void checkErrorInCollectorLogs() {
	    
	    if(expectedErrorMessages == null) {
	        expectedErrorMessages = Arrays.asList(CommonUtils.EXPECTED_ERRORS);
	    }
        CommonUtils.checkErrorInCollectorLogs(umAgentConfig, expectedErrorMessages);
    }
	
    protected long getTestDuration() {
        
        long testDuration = DEFAULT_TEST_DURATION_MS;
        
        try {
            testDuration = Long.parseLong(envProperties.getTestbedPropertyById(
                                NodeJSLoadTestbed.TEST_DURATION_ENV_VAR));
            LOGGER.info("Using custom system test duration: " + testDuration);
        }
        catch (Exception e) {
            LOGGER.warn("Exception occurred trying to read environment var " 
                   + NodeJSLoadTestbed.TEST_DURATION_ENV_VAR + ": " + e.getMessage());
            LOGGER.warn("Using default system test duration: " + testDuration);
        }
        
        return testDuration;
    }
    
    @AfterMethod(alwaysRun = true)
    public void executeAfterMethod() {
        
        try {
            if(clusterProcess != null) {
                clusterProcess.destroy();
            }
            else {
                stopNodeApp();  
            }
            
            stopCollectorAgent();
            stopJmeterScript();
            stopLoad(bounceIAThread);
            stopLoad(metricValidatorThread);
        }
        catch (Exception e) {
            e.printStackTrace();
            String message = "Exception occurred during test teardown: " + e.getMessage();
            LOGGER.error(message);
            Assert.fail(message);
        }
    }
    
	@AfterClass(alwaysRun = true)
	public void testClassTeardown() {
		super.testClassTeardown();
	}
	
	@Override
    public void testSuiteTeardown() {
        
    }
}
