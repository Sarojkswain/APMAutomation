package com.ca.apm.systemtest.alertstateload.role;

import org.apache.http.util.Args;

import com.ca.tas.builder.BuilderBase;
import com.ca.tas.client.IAutomationAgentClient;
import com.ca.tas.resolver.ITasResolver;
import com.ca.tas.role.AbstractRole;

public class MetricSynthRole extends AbstractRole {

    public static final String ENV_COLLECTOR_HOST = "collectorHost";

    private String collectorHost;

    protected MetricSynthRole(Builder builder) {
        super(builder.roleId, builder.getEnvProperties());
        this.collectorHost = builder.collectorHost;
    }

    @Override
    public void deploy(IAutomationAgentClient client) {}

    public String getCollectorHost() {
        return collectorHost;
    }

    public static class Builder extends BuilderBase<Builder, MetricSynthRole> {
        protected String roleId;
        protected String collectorHost;

        public Builder(String roleId, ITasResolver tasResolver) {
            this.roleId = roleId;
        }

        @Override
        public MetricSynthRole build() {
            Args.notBlank(collectorHost, "collectorHost");
            getEnvProperties().add(ENV_COLLECTOR_HOST, collectorHost);
            return getInstance();
        }

        @Override
        protected MetricSynthRole getInstance() {
            return new MetricSynthRole(this);
        }

        @Override
        protected Builder builder() {
            return this;
        }

        public Builder collectorHost(String collectorHost) {
            Args.notBlank(collectorHost, "collectorHost");
            this.collectorHost = collectorHost;
            return builder();
        }
    }

}
