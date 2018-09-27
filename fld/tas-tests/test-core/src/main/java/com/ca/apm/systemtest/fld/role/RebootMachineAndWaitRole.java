package com.ca.apm.systemtest.fld.role;

import com.ca.tas.builder.BuilderBase;
import com.ca.tas.client.IAutomationAgentClient;
import com.ca.tas.resolver.ITasResolver;
import com.ca.tas.restClient.IRestRequest;
import com.ca.tas.restClient.IRestResponse;
import com.ca.tas.restClient.RestClient;
import com.ca.tas.role.AbstractRole;
import com.ca.tas.role.revert.ResmanRevertRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URL;

public class RebootMachineAndWaitRole extends AbstractRole {

    private static final Logger LOGGER = LoggerFactory.getLogger(RebootMachineAndWaitRole.class);
    private static final Object REVERT_API_SEGMENT = "/api/vm/reboot?waitForBoot=true&hostname=";
    private static final String REBOOT_ROLE = "RebootMachineAndWaitRole";

    private final String hostname;
    private final URL resmanApi;
    private final RestClient restClient;
    private final int timeout;

    protected RebootMachineAndWaitRole(Builder builder) {
        super(REBOOT_ROLE, builder.getEnvProperties());
        hostname = builder.hostname;
        resmanApi = builder.resmanApi;
        restClient = builder.restClient;
        timeout = builder.timeout;
    }

    @Override
    public void deploy(IAutomationAgentClient aaClient) {
        revert();
    }

    protected void revert() {

        String urlPart = resmanApi.toString() + REVERT_API_SEGMENT + hostname;
        if (timeout != 300) {
            urlPart = urlPart + "&timeout=" + timeout;
        }

        LOGGER.info("request {}", urlPart);

        IRestRequest<String> revertRq = new ResmanRevertRequest(urlPart);
        try {
            IRestResponse<String> response = restClient.process(revertRq);
            if (response.getResultStatus() != IRestResponse.Status.SUCCESS) {
                throw new IllegalStateException("Error in rest request: " + response.getHttpStatus().getStatusCode() + " "
                        + response.getHttpStatus().getReasonPhrase());
            }
            LOGGER.info("Machine {} were successfully rebooted.", hostname);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to issue reboot request.", e);
        }
    }

    public static class Builder extends BuilderBase<Builder, RebootMachineAndWaitRole> {
        private final ITasResolver tasResolver;
        private String hostname;
        private URL resmanApi;
        protected RestClient restClient;
        private int timeout = 300;

        public Builder(ITasResolver tasResolver, String hostname) {
            this.tasResolver = tasResolver;
            restClient = new RestClient();
            this.hostname = hostname;
        }

        @Override
        public RebootMachineAndWaitRole build() {
            resmanApi = tasResolver.getResmanApi();

            return getInstance();
        }

        @Override
        protected Builder builder() {
            return this;
        }

        @Override
        protected RebootMachineAndWaitRole getInstance() {
            return new RebootMachineAndWaitRole(this);
        }

        public Builder withTimeout(int seconds) {
            this.timeout = seconds;
            return this;
        }
    }
}
