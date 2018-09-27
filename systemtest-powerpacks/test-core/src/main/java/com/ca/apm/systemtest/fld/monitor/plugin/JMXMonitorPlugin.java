package com.ca.apm.systemtest.fld.monitor.plugin;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.management.MBeanServerConnection;
import javax.management.ObjectName;
import javax.management.openmbean.CompositeDataSupport;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * @Author rsssa02
 */
public class JMXMonitorPlugin extends MyMBean{
    private static final Logger LOGGER = LoggerFactory.getLogger(JMXMonitorPlugin.class);
    final static SimpleDateFormat dFormat = new SimpleDateFormat("HH:mm:ss.SSS");

    public JMXMonitorPlugin() {
    }

    public void monitorJmx(String host, Integer port, Integer metricCollectionIntervalMillis,
                              Integer samplesCount, String jmxLogFilePath, String metrics) {
        if(host == null){
            host = "localhost";
            LOGGER.info("host parameter is null, using localhost as fallback...");
        }
        LOGGER.info("Collecting JVM info through JMX");
        LOGGER.info("JMX interval: milliseconds" + metricCollectionIntervalMillis);
        LOGGER.info("JMX samples count: " + samplesCount);
        LOGGER.info("Running on Host: " + host);

        File outputLogFile = new File(jmxLogFilePath);

        LOGGER.info("Output JMX log file: " + outputLogFile);

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
                            LOGGER.warn("Exception occurred while getting attribute '" + attribName + "' for mbean: " + myMBean + e);
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
            LOGGER.info("Closing the connection");
            out.close(); // don't swallow close Exception if copy completes normally
        } catch (InterruptedException ex) {
            String msg = "Jmx meter thread was force terminated!";
            LOGGER.warn(msg, ex);
        } catch (Exception ex) {
            String msg = "Exception occurred during JMX monitoring";
            LOGGER.error(msg, ex);
        } finally {
            IOUtils.closeQuietly(out);

            LOGGER.info("Closing connection for JMX");
            if (jmxc != null) {
                try {
                    jmxc.close();
                } catch (IOException ex) {
                    LOGGER.error("Error closing connection" + ex);
                }
            }
        }

        //return outputLogFile.getAbsolutePath();
    }
    private List<MyMBean> getAllMBeans(String metrics) {
        List<MyMBean> mbeansList = MyMBean.fromString(metrics);
        LOGGER.info("JMX mbeans: " + mbeansList);
        return mbeansList;
    }

}
