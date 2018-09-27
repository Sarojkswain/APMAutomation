/*
 * Copyright (c) 2015 CA. All rights reserved.
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

package com.ca.apm.systemtest.fld.role;

import java.net.Socket;

import com.ca.apm.automation.action.core.IAutomationFlow;
import com.ca.apm.automation.action.flow.FlowConfig;
import com.ca.apm.automation.action.flow.FlowConfig.FlowConfigBuilder;
import com.ca.apm.automation.action.flow.IFlowContext;
import com.ca.apm.systemtest.fld.flow.AGCRegisterFlow;
import com.ca.apm.systemtest.fld.flow.AGCRegisterFlowContext;
import com.ca.tas.builder.BuilderBase;
import com.ca.tas.client.IAutomationAgentClient;
import com.ca.tas.resolver.ITasResolver;
import com.ca.tas.role.AbstractRole;
import org.apache.http.util.Args;


/**
 * Register follower into AGC
 *
 * @author filja01
 */
public class AGCRegisterRole extends AbstractRole{

    private static final long START_TIMEOUT = 600000L;
    private final AGCRegisterFlowContext flowContext;

    private Class<? extends IAutomationFlow> startFlowClass;
    private IFlowContext startContext;
    private Class<? extends IAutomationFlow> stopFlowClass;
    private IFlowContext stopContext;


    /**
     * @param builder
     */
    public AGCRegisterRole(Builder builder) {
        super(builder.roleId);

        flowContext = builder.flowContext;

        startFlowClass = builder.startFlowClass;
        startContext = builder.startContext;
        stopFlowClass = builder.stopFlowClass;
        stopContext = builder.stopContext;
    }

    /**
     * Main method driving role deployment.
     *
     * @param aaClient AA client used for triggering flows
     */
    @Override
    public void deploy(IAutomationAgentClient aaClient) {
        boolean waitForStart = true;
        boolean agcRunning = false;
        boolean followerRunning = false;
        long startTime = System.currentTimeMillis();
        while (waitForStart) {
            // wait for AGC and MOM to both start
            if (!agcRunning) {
                agcRunning = checkPort(flowContext.getAGCHostName(), Integer.parseInt(flowContext.getAgcEmWvPort()));
            }
            if (!followerRunning) {
                followerRunning = checkPort(flowContext.getHostName(), Integer.parseInt(flowContext.getEmWvPort()));
            }
            
            waitForStart = !agcRunning || !followerRunning;
            if (!waitForStart) {
                long elapsed = System.currentTimeMillis() - startTime;
                if (elapsed > START_TIMEOUT) {
                    throw new RuntimeException("Timed out waiting for AGC and MOM to start");
                }
                shortWait(10000L);
            }
        }
        // give it two more minutes after the webview ports are listening to ensure that it is started completely
        shortWait(120000L);
        aaClient.runJavaFlow(new FlowConfig.FlowConfigBuilder(AGCRegisterFlow.class, flowContext,
            getHostingMachine().getHostnameWithPort()));

        //stop EM
        aaClient.runJavaFlow(new FlowConfigBuilder(stopFlowClass, stopContext, flowContext.getHostName() + ":8888"));

        //wait 60s for proper EM's shutdown
        try {
            Thread.sleep(60 * 1000);
        }
        catch (Exception e) {
            ;
        }
        //start EM
        aaClient.runJavaFlow(new FlowConfigBuilder(startFlowClass, startContext,flowContext.getHostName() + ":8888"));
    }
    
    
    private boolean checkPort(String hostname, int port) {
        boolean retval = false;
        try {
            Socket s = new Socket(hostname, port);
            retval = s.isConnected();
            s.close();
        } catch (Exception e) {
        }
        
        return retval;
    }
    
    
    private void shortWait(long ms) {
        try {
            Thread.sleep(ms);
        } catch (Exception e) {
            // do nothing
        }
    }
    

    public static class Builder extends BuilderBase<Builder, AGCRegisterRole> {

        private final String roleId;
        
        private AGCRegisterFlowContext flowContext;
        private AGCRegisterFlowContext.Builder flowContextBuilder;

        private Class<? extends IAutomationFlow> startFlowClass;
        private IFlowContext startContext;
        private Class<? extends IAutomationFlow> stopFlowClass;
        private IFlowContext stopContext;

        public Builder(String roleId, ITasResolver tasResolver) {
            this.roleId = roleId;
            flowContextBuilder = new AGCRegisterFlowContext.Builder();
        }

        @Override
        protected AGCRegisterRole getInstance() {
            return new AGCRegisterRole(this);
        }

        @Override
        protected Builder builder() {
            return this;
        }

        @Override
        public AGCRegisterRole build() {
            Args.notNull(startContext, "EM start command context");
            Args.notNull(stopContext, "EM stop command context");

            flowContext = flowContextBuilder.build();
            return new AGCRegisterRole(this);
        }
        
        public Builder hostName(String hostName) {
            flowContextBuilder.hostName(hostName);
            return this;
        }
        
        public Builder emWvPort(String emWvPort) {
            flowContextBuilder.emWvPort(emWvPort);
            return this;
        }

        public Builder wvHostName(String wvHostName) {
            flowContextBuilder.wvHostName(wvHostName);
            return this;
        }
        
        public Builder wvPort(String wvPort) {
            flowContextBuilder.wvPort(wvPort);
            return this;
        }

        public Builder agcHostName(String agcHostName) {
            flowContextBuilder.agcHostName(agcHostName);
            return this;
        }
        
        public Builder agcEmWvPort(String agcEmWvPort) {
            flowContextBuilder.agcEmWvPort(agcEmWvPort);
            return this;
        }
        
        public Builder agcWvPort(String agcWvPort) {
            flowContextBuilder.agcWvPort(agcWvPort);
            return this;
        }
        
        public Builder startCommand(Class<? extends IAutomationFlow> flowClass, IFlowContext flowContext) {
            this.startFlowClass = flowClass;
            this.startContext = flowContext;
            return this;
        }

        public Builder stopCommand(Class<? extends IAutomationFlow> flowClass, IFlowContext flowContext) {
            this.stopFlowClass = flowClass;
            this.stopContext = flowContext;
            return this;
        }
    }
}
