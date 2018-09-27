package com.ca.apm.systemtest.fld.plugin.logmonitor;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Session;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;

import com.ca.apm.systemtest.fld.common.ErrorUtils;
import com.ca.apm.systemtest.fld.common.PluginAnnotationComponent;
import com.ca.apm.systemtest.fld.common.spel.ConfigurationPlaceholderResolver;
import com.ca.apm.systemtest.fld.common.spel.StringEvaluator;
import com.ca.apm.systemtest.fld.logmonitor.config.LogStream;
import com.ca.apm.systemtest.fld.plugin.AbstractPluginImpl;
import com.ca.apm.systemtest.fld.plugin.Plugin;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * @author TAVPA01
 *
 */
public class LogMonitorPluginImpl extends AbstractPluginImpl implements MonitoringPlugin, InitializingBean, ApplicationContextAware {
    private Logger log = LoggerFactory.getLogger(LogMonitorPluginImpl.class);

    private HashMap<String, LogFileTailer> threadHash = new HashMap<>();
    ObjectMapper mapper = new ObjectMapper();
    long[] timestampsWindow;
    int timestampWindowPosition = 0;
    boolean wasStarvation = false;


    @Autowired
    @Qualifier(value="logMonitorProducer")
    private JmsTemplate jmsTemplate;

    @Value(value="${fld.agent.node.name:UNKNOWN}")
    private String nodeName;

    // In seconds
    @Value(value="${logmonitor.fileNotFound.interval:300}")
    private int fileNotFoundInterval;

    // In seconds
    @Value(value="${logmonitor.period:180}")
    private int timePeriod;

    @Value(value="${logmonitor.maxMatchesPerPeriod:10}")
    private int maxMatches;

    @Value(value="${logmonitor.numberOfPreviousLines:5}")
    private int numberOfPreviousLines;

    private ApplicationContext applicationContext;
    
    
    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }
    

    @Override
    public void afterPropertiesSet() throws Exception {
        timestampsWindow = new long[maxMatches];
        
        LogMonitorPluginConfiguration config = configurationManager.loadPluginConfiguration(MonitoringPlugin.PLUGIN, LogMonitorPluginConfiguration.class);
        if (config.getLogStreams() == null) {
            Map<String, LogStream> map = new HashMap<>();
            config.setLogStreams(map);
            configurationManager.savePluginConfiguration(PLUGIN, config);
        }
    }



    @Override
    public void enableMonitor(String streamId) {
        if (threadHash.containsKey(streamId)) {
            info("Log monitor for stream " + streamId + " already executing");
            return;
        }

        LogMonitorPluginConfiguration config = configurationManager.loadPluginConfiguration(MonitoringPlugin.PLUGIN, LogMonitorPluginConfiguration.class);
        LogStream stream = config.getLogStreams().get(streamId);
        
        Map<String, Object> vars = new HashMap<>();
        Map<String, Plugin> plugins = applicationContext.getBeansOfType(Plugin.class);
        for (Entry<String, Plugin> entry: plugins.entrySet()) {
            PluginAnnotationComponent pac = applicationContext.findAnnotationOnBean(entry.getKey(), PluginAnnotationComponent.class);
            if (pac != null) {
                vars.put(pac.pluginType(), entry.getValue());
            }
        }
        
        
        ConfigurationPlaceholderResolver placeholderResolver = new ConfigurationPlaceholderResolver(vars);
        StringEvaluator evaluator = new StringEvaluator(placeholderResolver);
        String filename = evaluator.evaluateString(stream.getFileName());
        
        File logFile = new File(filename);
        LogFileMonitor listener = new LogFileMonitor(this, nodeName, fileNotFoundInterval, numberOfPreviousLines, stream);
        LogFileTailer tailer = null;
        
        try {
            tailer = new LogFileTailerImpl(logFile, listener);
            threadHash.put(streamId, tailer);
        } catch (IOException e) {
            throw new LogMonitorException(e);
        }
    }


    @Override
    public void disableMonitor(String streamId) {
        LogFileTailer t = threadHash.remove(streamId);
        if (t != null) {
            try {
                t.stop();
            } catch (Exception e) {
                ErrorUtils.logExceptionFmt(log, e,
                    "Exception while disabling monitor. Exception: {0}");
            }
        }
    }


    @Override
    public LogStream getStreamConfig(String streamId) {
        LogMonitorPluginConfiguration config = configurationManager.loadPluginConfiguration(MonitoringPlugin.PLUGIN, LogMonitorPluginConfiguration.class);
        return config.getLogStreams().get(streamId);
    }



    @Override
    public void setStreamConfig(String streamId, LogStream streamConfig) {
        LogMonitorPluginConfiguration config = configurationManager.loadPluginConfiguration(MonitoringPlugin.PLUGIN, LogMonitorPluginConfiguration.class);
        config.getLogStreams().put(streamId, streamConfig);
        configurationManager.savePluginConfiguration(PLUGIN, config);
    }



    void sendMessage(final LoggingMonitorEvent message) {
        // Check maxMessageInPeriod
        long now = System.currentTimeMillis();
        long prevVal = timestampsWindow[timestampWindowPosition];
        if (prevVal > (now - timePeriod * 1000)) {
            if (!wasStarvation) {
                message.setLog("ERROR: Max messages per time period reached");
                internalSendMessage(message);
            }
            wasStarvation=true;
            return;
        }
        wasStarvation=false;
        timestampsWindow[timestampWindowPosition++] = now;
        timestampWindowPosition = timestampWindowPosition % timestampsWindow.length;

        internalSendMessage(message);
    }


    private void internalSendMessage(final LoggingMonitorEvent message) {
        jmsTemplate.send(new MessageCreator() {
            @Override
            public Message createMessage(Session session) throws JMSException {
                try {
                    return session.createTextMessage(mapper.writeValueAsString(message));
                } catch (Exception e) {
                    final String msg = "Message serialization error on "+message.getHostName();
                    log.error(msg, e);
                    return session.createTextMessage("{\"log\": \"" + msg + "\"}");
                }
            }
        });
    }


    /*
     * Setters added for unit testing
     */
    public void setMaxMatches(int maxMatches) {
        this.maxMatches = maxMatches;
    }


    public void setJmsTemplate(JmsTemplate jmsTemplate) {
        this.jmsTemplate = jmsTemplate;
    }
    
    public void setNumberOfPreviousLines(int numberOfPreviousLines) {
        this.numberOfPreviousLines = numberOfPreviousLines;
    }
}
