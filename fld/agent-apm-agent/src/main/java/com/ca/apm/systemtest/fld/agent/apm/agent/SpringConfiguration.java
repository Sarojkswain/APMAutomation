package com.ca.apm.systemtest.fld.agent.apm.agent;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.management.JMSConnectionStatsImpl;
import org.apache.activemq.management.JMSStatsImpl;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.Scheduled;

import com.ca.apm.systemtest.fld.agent.AgentSpringConfiguration.TimeMonitorBean;
import com.ca.apm.systemtest.fld.common.ErrorUtils;
import com.ca.apm.systemtest.fld.plugin.agentdownload.AgentDownloadPlugin;
import com.wily.introscope.agent.api.DataRecorderFactory;
import com.wily.introscope.agent.api.LongCounterDataRecorder;
import com.wily.introscope.agent.api.StringEventDataRecorder;

/**
 * Created by haiva01 on 14.12.2015.
 */
@Configuration
@ComponentScan("com.ca.apm.systemtest.fld.agent.apm.agent")
public class SpringConfiguration {
    private static final Logger log = LoggerFactory.getLogger(
        "com.ca.apm.systemtest.fld.agent.apm.agent");

    @Bean
    B myB() {
        return new B();
    }

    public static class B implements InitializingBean {
        private static final String ACTIVEMQ_METRICS_PREFIX = "FLD Agent|ActiveMQ";
        private static final String AGENT_METRICS_PREFIX = "FLD Agent";

        @Autowired
        ApplicationContext ctx;
        @Autowired
        ActiveMQConnectionFactory activeMqConnectionFactory;
        @Autowired
        TimeMonitorBean timeMonitorBean;

        @Scheduled(initialDelay = 1000, fixedRate = 5000)
        private void getActiveMqInfo() {
            try {
                JMSStatsImpl factoryStats = (JMSStatsImpl) activeMqConnectionFactory.getStats();
                JMSConnectionStatsImpl[] connections = factoryStats.getConnections();
                int count = connections != null ? factoryStats.getConnections().length : 0;

                LongCounterDataRecorder piDataRecorder = DataRecorderFactory
                    .createLongCounterDataRecorder(ACTIVEMQ_METRICS_PREFIX + ":Connections Count");
                piDataRecorder.recordCurrentValue(count);
            } catch (Throwable ex) {
                log.error("Attempted to get Broker connection count, but failed due to: " + ex
                    .getMessage(), ex);
            }

            try {
                StringEventDataRecorder sedr = DataRecorderFactory
                    .createStringEventDataRecorder(ACTIVEMQ_METRICS_PREFIX + ":Broker URL");
                String brokerUrl = activeMqConnectionFactory.getBrokerURL();
                sedr.recordDataPoint(StringUtils.defaultString(brokerUrl));
            } catch (Throwable ex) {
                log.error("Attempted to get Broker URL, but failed due to: " + ex
                    .getMessage(), ex);
            }
        }

        @Scheduled(initialDelay = 1000, fixedRate = 15000)
        private void getAgentNodeName() {
            try {
                String agentNodeName = System.getProperty("fld.agent.node.name", "(null)");
                StringEventDataRecorder sedr = DataRecorderFactory
                    .createStringEventDataRecorder(AGENT_METRICS_PREFIX + ":Node Name");
                sedr.recordDataPoint(agentNodeName);
            } catch (Throwable ex) {
                log.error("Attempted to get FLD agent node name, but failed due to: " + ex
                    .getMessage(), ex);
            }
        }

        @Scheduled(initialDelay = 1000, fixedRate = 15000)
        private void getAgentVersion() {
            try {
                AgentDownloadPlugin downloadPlugin = ctx.getBean(AgentDownloadPlugin.class);
                if (downloadPlugin == null) {
                    log.warn("AgentDownloadPlugin not found.");
                    return;
                }

                long version = downloadPlugin.getCurrentVersion();
                StringEventDataRecorder sedr = DataRecorderFactory
                    .createStringEventDataRecorder(AGENT_METRICS_PREFIX + ":Version");
                sedr.recordDataPoint(Long.toString(version));
            } catch (Throwable ex) {
                log.error("Attempted to get FLD agent version, but failed due to: " + ex
                    .getMessage(), ex);
            }
        }

        @Scheduled(initialDelay = 10000, fixedDelay = 60 * 1000)
        public void timeSynchronizationCheck() {
            try {
                final long offset = timeMonitorBean.getOffset();
                final LongCounterDataRecorder offsetRec = DataRecorderFactory
                    .createLongCounterDataRecorder(AGENT_METRICS_PREFIX + ":NTP Clock Offset");
                offsetRec.recordCurrentValue(offset);

                final long delay = timeMonitorBean.getDelay();
                final LongCounterDataRecorder delayRec = DataRecorderFactory
                    .createLongCounterDataRecorder(AGENT_METRICS_PREFIX + ":NTP Roundtrip Delay");
                delayRec.recordCurrentValue(delay);

                final String ntpHost = timeMonitorBean.getNtpHost();
                final StringEventDataRecorder sedr = DataRecorderFactory
                    .createStringEventDataRecorder(AGENT_METRICS_PREFIX + ":NTP Host");
                sedr.recordDataPoint(ntpHost);
            } catch (Exception e) {
                ErrorUtils.logExceptionFmt(log, e,
                    "Exception while trying to get time offset. Exception: {0}");
            }
        }


        @Override
        public void afterPropertiesSet() throws Exception {
            getActiveMqInfo();
            getAgentNodeName();
            getAgentVersion();
        }
    }
}
