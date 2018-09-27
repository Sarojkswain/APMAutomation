/*
 * Copyright (c) 2016 CA. All rights reserved.
 *
 * This software and all information contained therein is confidential and
 * proprietary and shall not be duplicated, used, disclosed or disseminated in
 * any way except as authorized by the applicable license agreement, without
 * the express written permission of CA. All authorized reproductions must be
 * marked with this language.
 *
 * EXCEPT AS SET FORTH IN THE APPLICABLE LICENSE AGREEMENT, TO THE EXTENT
 * PERMITTED BY APPLICABLE LAW, CA PROVIDES THIS SOFTWARE WITHOUT WARRANTY OF
 * ANY KIND, INCLUDING WITHOUT LIMITATION, ANY IMPLIED WARRANTIES OF
 * MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE. IN NO EVENT WILL CA BE
 * LIABLE TO THE END USER OR ANY THIRD PARTY FOR ANY LOSS OR DAMAGE, DIRECT OR
 * INDIRECT, FROM THE USE OF THIS SOFTWARE, INCLUDING WITHOUT LIMITATION, LOST
 * PROFITS, BUSINESS INTERRUPTION, GOODWILL, OR LOST DATA, EVEN IF CA IS
 * EXPRESSLY ADVISED OF SUCH LOSS OR DAMAGE.
 */

package com.ca.apm.powerpack.sysview.tests.role;

import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Arrays;

import org.apache.http.Header;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.Args;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ca.apm.automation.action.flow.FlowConfig;
import com.ca.apm.automation.action.flow.commandline.RunCommandFlow;
import com.ca.apm.automation.action.flow.commandline.RunCommandFlowContext;
import com.ca.apm.automation.action.flow.utility.FileModifierFlow;
import com.ca.apm.automation.action.flow.utility.FileModifierFlowContext;
import com.ca.tas.builder.BuilderBase;
import com.ca.tas.client.IAutomationAgentClient;
import com.ca.tas.role.AbstractRole;
import com.ca.tas.testbed.ITestbedMachine;

/**
 * This role sets up forwarding of the automation agent to a different host.
 * The purpose of this role is to support machines not managed by resman (e.g.
 * physical Mainframe machines) as targets for TAS-managed roles/tests.
 *
 * <p>
 * The following are requirements for the host that is to be a target of the forwarding role:
 * </p>
 * <ul>
 * <li>An automation agent has to be already deployed and running</li>
 * <li>The servlet server running the automation agent also needs to provide an additional context
 * identified by the {@link ForwarderRole#FW_DETECTION_CONTEXT} value, which when queried (GET) has
 * to include a special HTTP header in the response identified by the
 * {@link ForwarderRole#FW_DETECTION_HEADER} value.</li>
 * </ul>
 *
 * <p>
 * An easy way of achieving the above-mentioned requirements when using Jetty is to add the
 * following rewrite rules:
 * </p>
 *
 * <pre>
 * {@code
 * <Call name="addRule">
 *     <Arg>
 *         <New class="org.eclipse.jetty.rewrite.handler.HeaderPatternRule">
 *             <Set name="pattern">/forwarder</Set>
 *             <Set name="name">X-TAS-Forwarded</Set>
 *             <Set name="value">1</Set>
 *             <Set name="terminating">true</Set>
 *         </New>
 *     </Arg>
 * </Call>
 * <Call name="addRule">
 *     <Arg>
 *         <New class="org.eclipse.jetty.rewrite.handler.ResponsePatternRule">
 *             <Set name="pattern">/forwarder</Set>
 *             <Set name="code">200</Set>
 *         </New>
 *     </Arg>
 * </Call>
 * }
 * </pre>
 */
public class ForwarderRole extends AbstractRole {

    /** Forwarder for CA11. */
    public static final ForwarderRole FW_CA11 = new ForwarderRole.Builder("fwdCA11Role", "ca11")
        .targetPort(15030).build();
    /** Forwarder for CA31. */
    public static final ForwarderRole FW_CA31 = new ForwarderRole.Builder("fwdCA31Role", "ca31")
        .targetPort(15030).build();

    private static final Logger logger = LoggerFactory.getLogger(ForwarderRole.class);

    private static final String FW_SCRIPT_NAME = "fw.sh";
    private static final String FW_SCRIPT_PACKAGE = "/com/ca/apm/powerpack/sysview/tests/role/"
        + FW_SCRIPT_NAME;
    private static final String FW_SCRIPT_PATH = "/tmp/";
    private static final String FW_DETECTION_CONTEXT = "forwarder";
    private static final String FW_DETECTION_HEADER = "X-TAS-Forwarded";
    /**
     * Flow context object containing all info necessary for flow execution.
     */
    private final RunCommandFlowContext runFlowContext;
    private final FileModifierFlowContext confResourceFlowContext;
    private final int goSignalPort;

    /**
     * Constructor.
     *
     * @param build Builder object containing all necessary data.
     */
    private ForwarderRole(Builder build) {
        super(build.roleId);

        goSignalPort = build.goSignalPort;
        runFlowContext = build.runFlowContext;
        confResourceFlowContext = build.confResourceFlowContext;
    }

    /**
     * Checks whether forwarding is already in effect on the associated testbed
     * machine.
     * The detection is done based on a custom HTTP header returned by a
     * specific server context.
     *
     * @see #FW_DETECTION_CONTEXT
     * @see #FW_DETECTION_HEADER
     * @return true is forwarding is already in effect, false otherwise.
     */
    private boolean isForwarded() {
        HttpGet get = new HttpGet("http://" + getHostWithPort() + "/" + FW_DETECTION_CONTEXT);
        try (CloseableHttpClient client = HttpClients.createDefault();
            CloseableHttpResponse response = client.execute(get)) {

            // We don't care about the value nor the status code returned for
            // the queried context.
            Header header = response.getFirstHeader(FW_DETECTION_HEADER);
            return (header != null);
        } catch (IOException e) {
            // This is expected if the forwarding is not in effect since the
            // testbed machine doesn't necessarily have the server context we
            // use.
            return false;
        }
    }

    /**
     * Connects to the server provided by the forwarder script and send the go
     * signal that allows it to enable the forwarding.
     * The go signal is very simple and only consists of a tcp connection to a
     * specific port.
     *
     * @return true if the go signal was sent successfully, false otherwise.
     */
    private boolean sendGoSignal() {
        ITestbedMachine hostingMachine = getHostingMachine();
        Args.notNull(hostingMachine, "hosting machine");

        // We wait no longer than 20 seconds
        for (int retry = 0; retry < 20; ++retry) {
            logger.debug("Connecting to: " + hostingMachine.getHostname() + ":" + goSignalPort);
            try (Socket sock = new Socket(hostingMachine.getHostname(), goSignalPort)) {
                logger.debug("Sent go signal to forwarder server");
                return true;
            } catch (UnknownHostException e) {
                // This is unexpected and we can't recover.
                logger.error("Unable to locate target testbed machine: " + e.getMessage());
                return false;
            } catch (IOException e) {
                // This is expected, as the 'server' side provided by the
                // forwarder script might not be up yet. We give it some time.
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException ie) {
                    break; // Stop waiting if we get interrupted
                }
            }
        }

        logger.error("Timed out waiting for the forwarder script");
        return false;
    }

    /**
     * Deploys the role.
     *
     * @param aaClient Automation client used to trigger flows on automation agents
     */
    @Override
    public void deploy(IAutomationAgentClient aaClient) {
        Args.notNull(aaClient, "aaClient");

        if (!isForwarded()) {
            logger.debug("Deploying and starting forwarder script");
            runFlow(aaClient, FileModifierFlow.class, confResourceFlowContext);
            aaClient.runJavaFlow(new FlowConfig.FlowConfigBuilder(RunCommandFlow.class,
                runFlowContext, getHostWithPort()));

            // Send go signal to the script.
            if (!sendGoSignal()) {
                return;
            }

            logger.debug("Waiting for forwarding to go into effect");
            // We wait no longer than 20 seconds
            for (int retry = 0; retry < 20 && !isForwarded(); ++retry) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    // Stop waiting if we get interrupted
                    break;
                }
            }

            if (!isForwarded()) {
                logger.info("Forwarding failed (time-out)");
                return;
            }
        }

        logger.info("Forwarding is in effect");
    }

    /**
     * Builder responsible for holding all necessary properties to
     * instantiate {@link com.ca.apm.powerpack.sysview.tests.role.ForwarderRole}
     */
    public static class Builder extends BuilderBase<Builder, ForwarderRole> {
        private final String roleId;

        private RunCommandFlowContext runFlowContext;
        private FileModifierFlowContext confResourceFlowContext;

        private String targetHost;
        private int targetPort = 8888;
        private int goSignalPort = 1111;

        /**
         * @param roleId Role identification
         * @param targetHost Hostname of the target machine for the forwarding.
         */
        public Builder(String roleId, String targetHost) {
            Args.notBlank(targetHost, "targetHost");

            this.roleId = roleId;
            this.targetHost = targetHost;
        }

        /**
         * Sets a new value for the target port.
         *
         * @param port Port number.
         * @return Builder instance the method was called on.
         */
        public Builder targetPort(int port) {
            Args.check(port > 0 && port <= 65535, "Invalid port value: " + port);
            Args.check(port != goSignalPort,
                "Target port has to be different from the go-signal port (" + goSignalPort + ")");

            targetPort = port;
            return builder();
        }

        /**
         * Sets a new value for the go-signal port.
         *
         * @param port Port number.
         * @return Builder instance the method was called on.
         */
        public Builder goSignalPort(int port) {
            Args.check(port > 0 && port <= 65535, "Invalid port value: " + port);
            Args.check(port != targetPort,
                "Go-signal port has to be different from the target port (" + targetPort + ")");

            goSignalPort = port;
            return builder();
        }

        /**
         * Builds instance of the role based on the builder parameters.
         *
         * @return Role instance.
         */
        @Override
        public ForwarderRole build() {
            String[] args =
                {FW_SCRIPT_NAME, "&", targetHost, String.valueOf(targetPort),
                        String.valueOf(goSignalPort)};

            confResourceFlowContext =
                new FileModifierFlowContext.Builder().resource(FW_SCRIPT_PATH + FW_SCRIPT_NAME,
                    FW_SCRIPT_PACKAGE).build();

            runFlowContext =
                new RunCommandFlowContext.Builder("sh").args(Arrays.asList(args))
                    .workDir(FW_SCRIPT_PATH).doNotPrependWorkingDirectory().build();

            return new ForwarderRole(this);
        }

        @Override
        protected Builder builder() {
            return this;
        }

        @Override
        protected ForwarderRole getInstance() {
            return new ForwarderRole(this);
        }
    }
}
