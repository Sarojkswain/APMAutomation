package com.ca.apm.systemtest.fld.plugin.powerpack.delegates;

import java.util.HashMap;
import java.util.Map;

import org.activiti.engine.delegate.DelegateExecution;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ca.apm.systemtest.fld.common.logmonitor.FldLogger;
import com.ca.apm.systemtest.fld.plugin.AbstractJavaDelegate;
import com.ca.apm.systemtest.fld.plugin.NodeManager;
import com.ca.apm.systemtest.fld.plugin.jmeter.JMeterPlugin;
import com.ca.apm.systemtest.fld.plugin.powerpack.common.PowerPackConstants;
import com.ca.apm.systemtest.fld.proxy.AgentProxyFactory;

/**
 * Java delegate to run Jmeter load.
 * 
 * @author shadm01
 * @author Alexander Sinyushkin (sinal04@ca.com)
 *
 */
public class RunJmeterLoadDelegate extends AbstractJavaDelegate {
    public static final String LOG_CATEGORY = RunJmeterLoadDelegate.class.getSimpleName();

    public static final String JMETER_SAVE_SERVICE_TIMESTAMP_FORMAT_OPTION_NAME = "jmeter.save.saveservice.timestamp_format";
    public static final String JMETER_TIMESTAMP_FORMAT = "yyyy/MM/dd HH:mm:ss";

    public static final String JMETER_SAVE_SERVICE_PRINT_FIELD_NAMES_OPTION_NAME = "jmeter.save.saveservice.print_field_names";

    protected static final Logger LOGGER = LoggerFactory.getLogger(RunJmeterLoadDelegate.class);

    public RunJmeterLoadDelegate(NodeManager nodeManager, AgentProxyFactory agentProxyFactory, FldLogger fldLogger) {
        super(nodeManager, agentProxyFactory, fldLogger);
    }

    @Override
    protected void handleExecution(DelegateExecution execution) throws Throwable {
        String nodeName = getNodeExecutionVariable(execution, PowerPackConstants.JMETER_NODE_PARAM_NAME);
        final String serverType = getEnumExecutionVariable(execution, PowerPackConstants.SERVER_TYPE_PARAM_NAME);

        JMeterPlugin jmeterPlugin = getPluginForNode(nodeName, JMeterPlugin.PLUGIN, JMeterPlugin.class);

        String testScenario = getStringExecutionVariable(execution, PowerPackConstants.JMETER_SCENARIO_URL_PARAM_NAME);
        Integer testWarmupInSeconds = getIntegerExecutionVariable(execution, PowerPackConstants.TEST_WARMUP_PERIOD_IN_SECONDS_PARAM_NAME);
        Integer testDurationInSeconds = getIntegerExecutionVariable(execution, PowerPackConstants.TEST_DURATION_PERIOD_IN_SECONDS_PARAM_NAME);
        Integer numberOfConcurrentUsers = getIntegerExecutionVariable(execution, PowerPackConstants.JMETER_NUMBER_OF_THREADS_PARAM_NAME);
        String logDir = getStringExecutionVariable(execution, PowerPackConstants.LOG_DIR_PARAM_NAME);
        Integer appServerPort = getIntegerExecutionVariable(execution, PowerPackConstants.APP_SERVER_PORT_PARAM_NAME);
        String appServerHost = getStringExecutionVariable(execution, PowerPackConstants.APP_SERVER_HOST_PARAM_NAME);
        Integer cycleDelay = getIntegerExecutionVariable(execution, PowerPackConstants.JMETER_TEST_CYCLE_DELAY_PARAM_NAME, 
            PowerPackConstants.DEFAULT_JMETER_TEST_CYCLE_DELAY_IN_MILLISECONDS);
        Integer loops = getIntegerExecutionVariable(execution, PowerPackConstants.JMETER_LOOP_COUNT_PARAM_NAME, 
            PowerPackConstants.DEFAULT_JMETER_LOOP_COUNT);
        
        StringBuffer strBuf = new StringBuffer().append("Starting Jmeter load on node: {0}").
            append('\n').
            append("Jmeter test scenario URL: {1}").
            append('\n').
            append("Jmeter test warmup period in seconds (load runs without monitoring): {2,number,#}").
            append('\n').
            append("Jmeter test duration period in seconds (load runs with monitoring): {3,number,#}").
            append('\n').
            append("Jmeter test number of threads (concurrent users): {4,number,#}").
            append('\n').
            append("Jmeter test cycle delay: {5,number,#}").
            append('\n').
            append("Jmeter test loops count: {6,number,#}").            
            append('\n').
            append("Log dir: {7}").
            append('\n').
            append("Application server host: {8}").
            append('\n').
            append("Application server port: {9,number,#}");
            
        logInfo(LOG_CATEGORY, serverType, strBuf.toString(), nodeName, testScenario, 
            testWarmupInSeconds, testDurationInSeconds, numberOfConcurrentUsers, cycleDelay, 
            loops, logDir, appServerHost, appServerPort);

        jmeterPlugin.setScenarioUrl(testScenario);
        
        /*
         * Ramp-up is counted 10 seconds per each thread. Should be moved to workflow config as well.
         */
        int rampUpPeriod = numberOfConcurrentUsers != null ? numberOfConcurrentUsers * 10 : 10;
     	final Map<String, String> jmeterProps = new HashMap<>(8);
        jmeterProps.put(PowerPackConstants.TEST_RAMPUP_PERIOD_IN_SECONDS_PARAM_NAME, 
            String.valueOf(rampUpPeriod));
        
        int jmeterTestDuration = 0;
        if (testDurationInSeconds != null) {
        	jmeterTestDuration += testDurationInSeconds;
        }
        /*
         * Jmeter should run the load around testWarmupInSeconds longer,
         * otherwise all the graphs would have a drop in load exactly that same 
         * interval from the end of the measure.
         */ 
        if (testWarmupInSeconds != null) {
        	jmeterTestDuration += testWarmupInSeconds;
        }
        
        jmeterProps.put(PowerPackConstants.TEST_DURATION_PERIOD_IN_SECONDS_PARAM_NAME, 
            String.valueOf(jmeterTestDuration));
        jmeterProps.put(PowerPackConstants.JMETER_NUMBER_OF_THREADS_PARAM_NAME, 
            String.valueOf(numberOfConcurrentUsers));
        jmeterProps.put(PowerPackConstants.JMETER_TEST_CYCLE_DELAY_PARAM_NAME, 
            String.valueOf(cycleDelay));
        jmeterProps.put(PowerPackConstants.JMETER_LOOP_COUNT_PARAM_NAME, 
            String.valueOf(loops));
        jmeterProps.put(PowerPackConstants.LOG_DIR_PARAM_NAME, logDir);
        jmeterProps.put(PowerPackConstants.APP_SERVER_PORT_PARAM_NAME, 
            String.valueOf(appServerPort));
        jmeterProps.put(PowerPackConstants.APP_SERVER_HOST_PARAM_NAME, 
            appServerHost);
        jmeterProps.put(JMETER_SAVE_SERVICE_TIMESTAMP_FORMAT_OPTION_NAME, JMETER_TIMESTAMP_FORMAT);
        jmeterProps.put(JMETER_SAVE_SERVICE_PRINT_FIELD_NAMES_OPTION_NAME, Boolean.TRUE.toString());

        String task = jmeterPlugin.execute(jmeterProps);
        String jmeterStoppingPortStr = jmeterPlugin.getJmeterStoppingPort();
        String jmeterPath = jmeterPlugin.getJmeterPath();

        Integer jmeterStoppingPort = null;
        if (jmeterStoppingPortStr != null) {
        	try {
        		jmeterStoppingPort = Integer.valueOf(jmeterStoppingPortStr);
        	} catch (NumberFormatException nfe) {
        		logWarn(LOG_CATEGORY, serverType, 
        				"Failed to parse Jmeter stopping port returned by the Jmeter plugin!", nfe);
        	}
        }
        strBuf.setLength(0);
        strBuf.append("Jmeter launched").append('\n')
        .append("Jmeter Task Name: {0}").append('\n')
        .append("Jmeter Stopping Port: {1}").append('\n')
        .append("Jmeter Path: {2}");
        
        logInfo(LOG_CATEGORY, serverType, strBuf.toString(), task, jmeterStoppingPortStr, jmeterPath);
        
        execution.setVariable(PowerPackConstants.JMETER_TASK_NAME_PARAM_NAME, task);
        execution.setVariable(JMeterPlugin.JMETER_STOPPING_PORT_KEY, jmeterStoppingPort);
        execution.setVariable(JMeterPlugin.JMETER_PATH_KEY, jmeterPath);
    }

    @Override
    protected Logger getLogger() {
        return LOGGER;
    }

}
