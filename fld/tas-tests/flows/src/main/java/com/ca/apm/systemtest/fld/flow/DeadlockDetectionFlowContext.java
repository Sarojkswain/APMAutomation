package com.ca.apm.systemtest.fld.flow;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.http.util.Args;

import com.ca.apm.automation.action.flow.IFlowContext;
import com.ca.tas.builder.BuilderBase;
import com.ca.tas.property.AbstractEnvPropertySerializer;
import com.ca.tas.property.EnvPropSerializable;
import com.google.gson.Gson;

public class DeadlockDetectionFlowContext
    implements
        IFlowContext,
        EnvPropSerializable<DeadlockDetectionFlowContext> {

    private final transient Serializer serializer = new Serializer(this);

    private final boolean locateRunningEmOrWebview;
    private final Set<Integer> pids;
    private final String smtpHost;
    private final int smtpPort;
    private final String fromEmailAddress;
    private final Set<String> emailAddresses;

    protected DeadlockDetectionFlowContext(Builder builder) {
        this.locateRunningEmOrWebview = builder.locateRunningEmOrWebview;
        this.pids = builder.pids;
        this.smtpHost = builder.smtpHost;
        this.smtpPort = builder.smtpPort;
        this.fromEmailAddress = builder.fromEmailAddress;
        this.emailAddresses = builder.emailAddresses;
    }

    @Override
    public DeadlockDetectionFlowContext deserialize(String key, Map<String, String> map) {
        return serializer.deserialize(key, map);
    }

    @Override
    public Map<String, String> serialize(String key) {
        return serializer.serialize(key);
    }

    public boolean locateRunningEm() {
        return locateRunningEmOrWebview;
    }

    @SuppressWarnings("unchecked")
    public Set<Integer> getPids() {
        return pids == null ? Collections.EMPTY_SET : Collections.unmodifiableSet(pids);
    }

    public String getSmtpHost() {
        return smtpHost;
    }

    public int getSmtpPort() {
        return smtpPort;
    }

    public String getFromEmailAddress() {
        return fromEmailAddress;
    }

    @SuppressWarnings("unchecked")
    public Set<String> getEmailAddresses() {
        return emailAddresses == null ? Collections.EMPTY_SET : Collections
            .unmodifiableSet(emailAddresses);
    }

    public static class Builder extends BuilderBase<Builder, DeadlockDetectionFlowContext> {
        private boolean locateRunningEmOrWebview = true;
        private Set<Integer> pids;
        private boolean sendEmail = true;
        private String smtpHost;
        private int smtpPort = -1;
        private String fromEmailAddress;
        private Set<String> emailAddresses;

        @Override
        public DeadlockDetectionFlowContext build() {
            DeadlockDetectionFlowContext ctx = getInstance();
            if (sendEmail) {
                Args.notBlank(smtpHost, "smtpHost");
                Args.notBlank(fromEmailAddress, "fromEmailAddress");
                Args.notEmpty(emailAddresses, "emailAddresses");
            }
            return ctx;
        }

        @Override
        protected Builder builder() {
            return this;
        }

        @Override
        protected DeadlockDetectionFlowContext getInstance() {
            return new DeadlockDetectionFlowContext(this);
        }

        public Builder locateRunningEm() {
            return locateRunningEm(true);
        }

        public Builder doNotLocateRunningEm() {
            return locateRunningEm(false);
        }

        public Builder locateRunningEm(boolean locateRunningEmOrWebview) {
            this.locateRunningEmOrWebview = locateRunningEmOrWebview;
            return builder();
        }

        public Builder addCheckPids(Set<Integer> pids) {
            Args.notEmpty(pids, "pids");
            getPids().addAll(pids);
            return builder();
        }

        public Builder addCheckPid(int pid) {
            Args.positive(pid, "pid");
            getPids().add(pid);
            return builder();
        }

        public Builder smtpHost(String smtpHost) {
            Args.notBlank(smtpHost, "smtpHost");
            this.smtpHost = smtpHost;
            return builder();
        }

        public Builder smtpPort(int smtpPort) {
            Args.positive(smtpPort, "smtpPort");
            this.smtpPort = smtpPort;
            return builder();
        }

        public Builder fromEmailAddress(String fromEmailAddress) {
            Args.notBlank(fromEmailAddress, "fromEmailAddress");
            this.fromEmailAddress = fromEmailAddress;
            return builder();
        }

        public Builder addEmailAddresses(Set<String> emailAddresses) {
            Args.notEmpty(emailAddresses, "emailAddresses");
            getEmailAddresses().addAll(emailAddresses);
            return builder();
        }

        public Builder addEmailAddress(String emailAddress) {
            Args.notBlank(emailAddress, "emailAddress");
            getEmailAddresses().add(emailAddress);
            return builder();
        }

        private Set<Integer> getPids() {
            if (pids == null) {
                pids = new HashSet<>();
            }
            return pids;
        }

        private Set<String> getEmailAddresses() {
            if (emailAddresses == null) {
                emailAddresses = new HashSet<>();
            }
            return emailAddresses;
        }
    }

    public static class Serializer
        extends AbstractEnvPropertySerializer<DeadlockDetectionFlowContext> {
        private static final String DATA = "data";
        private DeadlockDetectionFlowContext context;

        public Serializer(DeadlockDetectionFlowContext context) {
            super(Serializer.class);
            this.context = context;
        }

        @Override
        public DeadlockDetectionFlowContext deserialize(String key, Map<String, String> map) {
            Map<String, String> deserializedMap = deserializeMapWithKey(key, map);
            String data = deserializedMap.get(DATA);
            Gson gson = new Gson();
            DeadlockDetectionFlowContext context =
                gson.fromJson(data, DeadlockDetectionFlowContext.class);
            return context;
        }

        @Override
        public Map<String, String> serialize(String key) {
            Map<String, String> map = super.serialize(key);
            Map<String, String> customData = new HashMap<>(1);
            Gson gson = new Gson();
            customData.put(DATA, gson.toJson(context));
            map.putAll(serializeMapWithKey(key, customData));
            return map;
        }
    }

}
