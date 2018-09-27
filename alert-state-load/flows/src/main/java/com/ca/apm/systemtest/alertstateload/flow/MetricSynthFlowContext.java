package com.ca.apm.systemtest.alertstateload.flow;

import org.apache.http.util.Args;

import com.ca.apm.automation.action.flow.IFlowContext;
import com.ca.tas.builder.BuilderBase;

public class MetricSynthFlowContext implements IFlowContext {

    private String emHost;
    private int numberOfConnectionGroups;
    private int numberOfHosts;
    private int numberOfAgents;
    private long duration;

    private String domain;
    private String processName;
    private int emPort;
    private String hostname;
    private String agentFormat;

    private String webappBaseName;
    private String ejbBaseName;

    private String agentHostnameFormat;

    private int minValueAverageResponseTime;
    private int maxValueAverageResponseTime;

    protected MetricSynthFlowContext(Builder builder) {
        this.emHost = builder.emHost;
        this.numberOfConnectionGroups = builder.numberOfConnectionGroups;
        this.numberOfHosts = builder.numberOfHosts;
        this.numberOfAgents = builder.numberOfAgents;
        this.duration = builder.duration;
        this.domain = builder.domain;
        this.processName = builder.processName;
        this.emPort = builder.emPort;
        this.hostname = builder.hostname;
        this.agentFormat = builder.agentFormat;
        this.webappBaseName = builder.webappBaseName;
        this.ejbBaseName = builder.ejbBaseName;
        this.agentHostnameFormat = builder.agentHostnameFormat;
        this.minValueAverageResponseTime = builder.minValueAverageResponseTime;
        this.maxValueAverageResponseTime = builder.maxValueAverageResponseTime;
    }

    public String getEmHost() {
        return emHost;
    }

    public int getNumberOfConnectionGroups() {
        return numberOfConnectionGroups;
    }

    public int getNumberOfHosts() {
        return numberOfHosts;
    }

    public int getNumberOfAgents() {
        return numberOfAgents;
    }

    public long getDuration() {
        return duration;
    }

    public String getDomain() {
        return domain;
    }

    public String getProcessName() {
        return processName;
    }

    public int getEmPort() {
        return emPort;
    }

    public String getHostname() {
        return hostname;
    }

    public String getAgentFormat() {
        return agentFormat;
    }

    public String getWebappBaseName() {
        return webappBaseName;
    }

    public String getEjbBaseName() {
        return ejbBaseName;
    }

    public String getAgentHostnameFormat() {
        return agentHostnameFormat;
    }

    public int getMinValueAverageResponseTime() {
        return minValueAverageResponseTime;
    }

    public int getMaxValueAverageResponseTime() {
        return maxValueAverageResponseTime;
    }

    public static class Builder extends BuilderBase<Builder, MetricSynthFlowContext> {
        private String emHost;
        private int numberOfConnectionGroups;
        private int numberOfHosts;
        private int numberOfAgents;
        private long duration;

        private String domain = "SuperDomain";
        private String processName = "FakeAgent";
        private int emPort = 5001;
        private String hostname = "superhost";
        private String agentFormat = "appsrv-%04d";

        private String webappBaseName = "webapp";
        private String ejbBaseName = "baseejb";

        private String agentHostnameFormat = "%s-%04d";

        private int minValueAverageResponseTime;
        private int maxValueAverageResponseTime;

        @Override
        public MetricSynthFlowContext build() {
            MetricSynthFlowContext ctx = getInstance();
            Args.notBlank(emHost, "emHost");
            Args.positive(numberOfConnectionGroups, "numberOfConnectionGroups");
            Args.positive(numberOfHosts, "numberOfHosts");
            Args.positive(numberOfAgents, "numberOfAgents");
            Args.positive(duration, "duration");
            Args.notBlank(domain, "domain");
            Args.notBlank(processName, "processName");
            Args.positive(emPort, "emPort");
            Args.notBlank(hostname, "hostname");
            Args.notBlank(agentFormat, "agentFormat");
            Args.notBlank(webappBaseName, "webappBaseName");
            Args.notBlank(ejbBaseName, "ejbBaseName");
            Args.notBlank(agentHostnameFormat, "agentHostnameFormat");
            Args.notNegative(minValueAverageResponseTime, "minValueAverageResponseTime");
            Args.notNegative(maxValueAverageResponseTime, "maxValueAverageResponseTime");
            return ctx;
        }

        @Override
        protected Builder builder() {
            return this;
        }

        @Override
        protected MetricSynthFlowContext getInstance() {
            return new MetricSynthFlowContext(this);
        }

        public Builder emHost(String emHost) {
            Args.notBlank(emHost, "emHost");
            this.emHost = emHost;
            return builder();
        }

        public Builder numberOfConnectionGroups(int numberOfConnectionGroups) {
            Args.positive(numberOfConnectionGroups, "numberOfConnectionGroups");
            this.numberOfConnectionGroups = numberOfConnectionGroups;
            return builder();
        }

        public Builder numberOfHosts(int numberOfHosts) {
            Args.positive(numberOfHosts, "numberOfHosts");
            this.numberOfHosts = numberOfHosts;
            return builder();
        }

        public Builder numberOfAgents(int numberOfAgents) {
            Args.positive(numberOfAgents, "numberOfAgents");
            this.numberOfAgents = numberOfAgents;
            return builder();
        }

        public Builder duration(long duration) {
            Args.positive(duration, "duration");
            this.duration = duration;
            return builder();
        }

        public Builder domain(String domain) {
            Args.notBlank(domain, "domain");
            this.domain = domain;
            return builder();
        }

        public Builder processName(String processName) {
            Args.notBlank(processName, "processName");
            this.processName = processName;
            return builder();
        }

        public Builder emPort(int emPort) {
            Args.positive(emPort, "emPort");
            this.emPort = emPort;
            return builder();
        }

        public Builder hostname(String hostname) {
            Args.notBlank(hostname, "hostname");
            this.hostname = hostname;
            return builder();
        }

        public Builder agentFormat(String agentFormat) {
            Args.notBlank(agentFormat, "agentFormat");
            this.agentFormat = agentFormat;
            return builder();
        }

        public Builder webappBaseName(String webappBaseName) {
            Args.notBlank(webappBaseName, "webappBaseName");
            this.webappBaseName = webappBaseName;
            return builder();
        }

        public Builder ejbBaseName(String ejbBaseName) {
            Args.notBlank(ejbBaseName, "ejbBaseName");
            this.ejbBaseName = ejbBaseName;
            return builder();
        }

        public Builder agentHostnameFormat(String agentHostnameFormat) {
            Args.notBlank(agentHostnameFormat, "agentHostnameFormat");
            this.agentHostnameFormat = agentHostnameFormat;
            return builder();
        }

        public Builder minValueAverageResponseTime(int minValueAverageResponseTime) {
            Args.notNegative(minValueAverageResponseTime, "minValueAverageResponseTime");
            this.minValueAverageResponseTime = minValueAverageResponseTime;
            return builder();
        }

        public Builder maxValueAverageResponseTime(int maxValueAverageResponseTime) {
            Args.notNegative(maxValueAverageResponseTime, "maxValueAverageResponseTime");
            this.maxValueAverageResponseTime = maxValueAverageResponseTime;
            return builder();
        }
    }

}
