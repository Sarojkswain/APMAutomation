package com.ca.apm.systemtest.fld.plugin.powerpack.metrics;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import javax.management.MBeanServerConnection;
import javax.management.ObjectName;
import javax.management.openmbean.CompositeDataSupport;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import com.ca.apm.systemtest.fld.common.PluginAnnotationComponent;
import com.ca.apm.systemtest.fld.plugin.AbstractPluginImpl;
import com.ca.apm.systemtest.fld.plugin.annotations.ExposeMethod;
import com.ca.apm.systemtest.fld.plugin.powerpack.common.PowerPackConstants;
import com.ca.apm.systemtest.fld.plugin.vo.DashboardIdStore;
import com.ca.apm.systemtest.fld.plugin.vo.ProcessInstanceIdStore;

/**
 * Load Orchestrator plugin which collects JMX metrics.
 *
 * @author meler03
 * @author shadm01
 * @author Alexander Sinyushkin (sinal04@ca.com)
 *
 */
@PluginAnnotationComponent(pluginType = JmxMetricGatheringPlugin.PLUGIN)
public class JmxMetricGatheringPlugin extends AbstractPluginImpl implements MetricGatheringPlugin {
    public static final String PLUGIN = "jmxMetricGatheringPlugin";

    private static final Logger LOGGER = LoggerFactory.getLogger(JmxMetricGatheringPlugin.class);

    public final static String JMX_LOG_PATH = "jmxMetricLogPath";
    final static SimpleDateFormat dFormat = new SimpleDateFormat("HH:mm:ss.SSS");
    final String thread_suffix = "_WASD_JMX";

    @Override
    @ExposeMethod(description = "Starts monitoring of JMX metrics.")
    public Long runMonitoring(PerfMonitoringConfig config) {
        if (config == null) {
            String msg = "Performance monitoring configuration object must not be null!";
            error(msg);
            throw new MetricGatheringPluginException(msg,
                MetricGatheringPluginException.ERR_MONITORING_CONFIG_IS_INVALID);
        }

        if (!(config instanceof JmxMonitoringConfig)) {
            String msg = MessageFormat.format("Expected performance monitoring object of type {0}, got {1}",
                JmxMonitoringConfig.class.getName(), config.getClass().getName());
            error(msg);
            throw new MetricGatheringPluginException(msg,
                MetricGatheringPluginException.ERR_MONITORING_CONFIG_IS_INVALID);
        }

        JmxMonitoringConfig jmxMonConfig = (JmxMonitoringConfig) config;

        final String jmxConnectionHost = jmxMonConfig.getJmxConnectionHost();
        final Integer jmxConnectionPort = jmxMonConfig.getJmxConnectionPort();
        final Integer metricCollectionIntervalMillis = jmxMonConfig.getSampleIntervalMillis();
        final Integer samplesCount = jmxMonConfig.getSamplesCount();
        final String outputLogDir = jmxMonConfig.getOutputLogDirPath();
        final String metrics = StringUtils.hasText(jmxMonConfig.getJmxMetrics()) ? 
            jmxMonConfig.getJmxMetrics() : PowerPackConstants.DEFAULT_JMX_METRICS;
        final String jmxLogFileName = StringUtils.hasText(jmxMonConfig.getJmxOutputFileName()) ? 
            jmxMonConfig.getJmxOutputFileName() : PowerPackConstants.DEFAULT_JMX_LOG_FILE_NAME;
        
        info("Using metrics: {0}", metrics);

        final Long dashbId = DashboardIdStore.getDashboardId();
        final String processId = ProcessInstanceIdStore.getProcessInstanceId();

        Thread jmxThread = new Thread(new Runnable() {
            private final Long dshbId = dashbId;
            private final String prcInstId = processId;
        
            @Override
            public void run() {
                DashboardIdStore.setDashboardId(dshbId);
                ProcessInstanceIdStore.setProcessInstanceId(prcInstId);

                info("JMX Thread Entered");
                monitorJmx(jmxConnectionHost, jmxConnectionPort, metricCollectionIntervalMillis,
                    samplesCount, outputLogDir, jmxLogFileName, metrics);
                info("JMX Thread Exited");
            }
        });


        final String jmxThreadName = thread_suffix;
        jmxThread.setName(jmxThreadName);

        jmxThread.start();
        return null;
    }

    @Override
    @ExposeMethod(description = "Stops monitoring.")
    public void stopMonitoring() {
        info("Stopping JMX monitoring");
        String jmxThreadName = thread_suffix;
        Thread jmxThread = getThreadByName(jmxThreadName);

        info("JMX thread is null: {0}", new Boolean(jmxThread == null));

        if (jmxThread != null) {
            info("{0} thread state is: {1}", jmxThreadName, jmxThread.getState());

            if (jmxThread.isAlive()) {
                jmxThread.interrupt();
                info("JMX thread state after killing: {0}", jmxThread.getState());
            }
        }
    }

    @Override
    protected Logger getLogger() {
        return LOGGER;
    }

    /***
     * Collects JVM metrics through JMX
     *
     * @param host                               host for JMX monitoring (most likely localhost)
     * @param port                               port for JMX (default = 1099)
     * @param metricCollectionIntervalMillis     interval between collections in ms
     * @param samplesCount                       number of samples to collect
     * @param outputLogDir                       path to directory where metrics would be stored
     * @param jmxLogFileName                     JMX log file name
     * @throws Exception
     */
    private String monitorJmx(String host, Integer port, Integer metricCollectionIntervalMillis,
        Integer samplesCount, String outputLogDir, String jmxLogFileName, String metrics) {

        info("Collecting JVM info through JMX");
        info("JMX interval: {0} milliseconds", metricCollectionIntervalMillis);
        info("JMX samples count: {0}", samplesCount);

        File outputLogFile = new File(outputLogDir, jmxLogFileName);

        info("Output JMX log file: {0}", outputLogFile);

        //TODO - only works on localhost, tunneling / VM does not work
        //Need to configure server with params:
        //-Djavax.management.builder.initial= -Dcom.sun.management.jmxremote
        //-Dcom.sun.management.jmxremote.authenticate=false
        // -Dcom.sun.management.jmxremote.ssl=false -Dcom.sun.management.jmxremote.port=1099

        OutputStream out = null;

        JMXConnector jmxc = null;
        try {
            out = FileUtils.openOutputStream(outputLogFile, true);

            List<MyMBean> mbeansList = getAllMBeans(metrics);
            StringBuilder params = new StringBuilder();
            params.append("Time,");
            
            //write caption
            for (MyMBean mBean : mbeansList) {
                String mBeanType = mBean.getType();
                String mBeanName = mBean.getName();
                String colNameBase = "";
                if (mBeanType != null) {
                    colNameBase += mBeanType + ":";
                }
                if (mBeanName != null) {
                    colNameBase += mBeanName + ":";
                }
                for (String attrName : mBean.getAttributeKeys()) {
                    List<String> subAttrs = mBean.getSubAttributes(attrName);
                    if (subAttrs == null || subAttrs.isEmpty()) {
                        String colName = colNameBase + attrName; 
                        params.append(colName).append(',');
                    } else {
                        String colNameAttrBase = colNameBase + attrName + ":";
                        for (String subAttr : subAttrs) {
                            String colName = colNameAttrBase + subAttr;
                            params.append(colName).append(',');
                        }
                    }
                }
            }
            
            params.append("\r\n");
            IOUtils.write(params.toString(), out, Charset.defaultCharset());
            params.setLength(0);

            final JMXServiceURL url = new JMXServiceURL(
                String.format("service:jmx:rmi:///jndi/rmi://%s:%s/jmxrmi", host, port));
            jmxc = null;
            MBeanServerConnection mbsc = null;

            for (int i = 0; i < samplesCount; i++) {
                long tmpTime = System.currentTimeMillis();
                params.append(dFormat.format(new Date())).append(",");
                for (MyMBean myMBean : mbeansList) {
                    ObjectName dynMBeanName = new ObjectName(myMBean.getPath());
                    for (String attribName : myMBean.getAttributeKeys()) {
                        Object data = null;
                        try {
                            if (mbsc == null) {
                                if (jmxc == null) {
                                    jmxc = JMXConnectorFactory.connect(url, null);    
                                }
                                mbsc = jmxc.getMBeanServerConnection();
                            }
                            data = mbsc.getAttribute(dynMBeanName, attribName);    
                        } catch (Exception e) {
                            warn("Exception occurred while getting attribute '" + attribName + "' for mbean: " + myMBean, e);
                            break;
                        }
                        
                        List<String> subAttrs = myMBean.getSubAttributes(attribName);
                        if (subAttrs == null) {
                            params.append(data).append(",");
                        } else {
                            CompositeDataSupport compData = (CompositeDataSupport) data;
                            for (String subAttrib : myMBean.getSubAttributes(attribName)) {
                                params.append(compData.get(subAttrib)).append(",");
                            }
                        }
                    }
                }
                params.append("\r\n");
                IOUtils.write(params.toString(), out, Charset.defaultCharset());
                params.setLength(0);

                long offset = System.currentTimeMillis() - tmpTime;
                long sleepTime = metricCollectionIntervalMillis - offset;
                if (sleepTime > 0) {
                    Thread.sleep(sleepTime);
                }
            }
            info("Closing the connection");
            out.close(); // don't swallow close Exception if copy completes normally
        } catch (InterruptedException ex) {
            String msg = "Jmx meter thread was force terminated!";
            warn(msg, ex);
        } catch (Exception ex) {
            String msg = "Exception occurred during JMX monitoring";
            error(msg, ex);
            throw new MetricGatheringPluginException(msg, ex);
        } finally {
            IOUtils.closeQuietly(out);

            info("Closing connection for JMX");
            if (jmxc != null) {
                try {
                    jmxc.close();
                } catch (IOException ex) {
                    error("Error closing connection", ex);
                }
            }
        }

        return outputLogFile.getAbsolutePath();
    }

    private List<MyMBean> getAllMBeans(String metrics) {
        List<MyMBean> mbeansList = MyMBean.fromString(metrics);
        info("JMX mbeans: {0}", mbeansList);
        return mbeansList;
    }

    private Thread getThreadByName(String threadName) {
        for (Thread t : Thread.getAllStackTraces().keySet()) {
            if (t.getName().equals(threadName)) {
                return t;
            }
        }
        return null;
    }

}
