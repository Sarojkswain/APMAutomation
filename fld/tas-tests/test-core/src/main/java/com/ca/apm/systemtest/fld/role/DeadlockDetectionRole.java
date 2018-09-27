package com.ca.apm.systemtest.fld.role;

import java.util.HashSet;
import java.util.Set;

import org.apache.http.util.Args;

import com.ca.apm.systemtest.fld.flow.DeadlockDetectionFlowContext;
import com.ca.tas.builder.BuilderBase;
import com.ca.tas.client.IAutomationAgentClient;
import com.ca.tas.role.AbstractRole;

public class DeadlockDetectionRole extends AbstractRole {

    public final static String ENV_RUN_EM_DEADLOCK_DETECTION = "RUN_EM_DEADLOCK_DETECTION";

    public static final String DEFAULT_FROM_EMAIL_ADDRESS = "FLD@ca.com";

    protected DeadlockDetectionRole(Builder builder) {
        super(builder.roleId, builder.getEnvProperties());
    }

    @Override
    public void deploy(IAutomationAgentClient aaClient) {}

    public static class Builder extends BuilderBase<Builder, DeadlockDetectionRole> {
        private String roleId;
        private String smtpHost;
        private int smtpPort = -1;
        private String fromEmailAddress = DEFAULT_FROM_EMAIL_ADDRESS;
        private Set<String> emailAddresses;

        public Builder(String roleId) {
            this.roleId = roleId;
        }

        @Override
        public DeadlockDetectionRole build() {
            Args.notBlank(smtpHost, "smtpHost");
            Args.notBlank(fromEmailAddress, "fromEmailAddress");
            Args.notEmpty(emailAddresses, "emailAddresses");
            addRunDeadlockDetectionFlow();
            return getInstance();
        }

        @Override
        protected Builder builder() {
            return this;
        }

        @Override
        protected DeadlockDetectionRole getInstance() {
            DeadlockDetectionRole role = new DeadlockDetectionRole(this);
            return role;
        }

        public Builder smtpHost(String smtpHost) {
            Args.notBlank(smtpHost, "smtpHost");
            this.smtpHost = smtpHost;
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

        private void addRunDeadlockDetectionFlow() {
            DeadlockDetectionFlowContext.Builder builder =
                (new DeadlockDetectionFlowContext.Builder()).locateRunningEm().smtpHost(smtpHost)
                    .fromEmailAddress(fromEmailAddress).addEmailAddresses(emailAddresses);
            if (smtpPort > 0) {
                builder.smtpPort(smtpPort);
            }
            DeadlockDetectionFlowContext context = builder.build();
            getEnvProperties().add(ENV_RUN_EM_DEADLOCK_DETECTION, context);
        }

        private Set<String> getEmailAddresses() {
            if (emailAddresses == null) {
                emailAddresses = new HashSet<>();
            }
            return emailAddresses;
        }
    }

}
