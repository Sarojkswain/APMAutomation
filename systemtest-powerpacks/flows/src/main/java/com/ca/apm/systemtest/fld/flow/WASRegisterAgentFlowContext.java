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
package com.ca.apm.systemtest.fld.flow;

import com.ca.apm.automation.action.flow.IFlowContext;
import com.ca.apm.automation.action.flow.agent.ApplicationServerType;
import com.ca.tas.builder.ExtendedBuilderBase;
import org.apache.http.util.Args;

import java.util.Map;

/**
 * @Author rsssa02
 */
public class WASRegisterAgentFlowContext implements IFlowContext{

        private final String agentPath;
        private String maxHeap;
        private String initialHeap;
        private final ApplicationServerType serverType;
        private final String serverXmlFilePath;


        protected WASRegisterAgentFlowContext(WASRegisterAgentFlowContext.Builder builder) {
            this.agentPath = builder.agentPath;
            this.serverType = builder.serverType;
            this.maxHeap = builder.maxHeap;
            this.initialHeap = builder.initialHeap;
            this.serverXmlFilePath = builder.serverXmlFilePath;
        }

    public String getMaxHeap() {
        return maxHeap;
    }

    public String getInitialHeap() {
        return initialHeap;
    }

    public String getAgentPath() {
            return agentPath;
        }

        public ApplicationServerType getServerType() {
            return serverType;
        }

        public String getServerXmlFilePath() {
            return serverXmlFilePath;
        }

        public static class Builder extends ExtendedBuilderBase<Builder, WASRegisterAgentFlowContext> {

            protected String agentPath;
            protected String maxHeap;
            protected String initialHeap;

            protected ApplicationServerType serverType;
            protected String serverXmlFilePath;


            public Builder() {

            }

            public WASRegisterAgentFlowContext build() {
                WASRegisterAgentFlowContext context = this.getInstance();
                Args.notNull(context.agentPath, "agentPath");
                Args.notNull(context.serverType, "serverType");
                Args.notNull(context.serverXmlFilePath, "serverXmlFilePath");
                Args.notNull(context.initialHeap, "initialHeap");
                Args.notNull(context.maxHeap, "maxHeap");

                return context;
            }

            protected WASRegisterAgentFlowContext getInstance() {
                return new WASRegisterAgentFlowContext(this);
            }

            public WASRegisterAgentFlowContext.Builder agentPath(String agentPath) {
                this.agentPath = agentPath;
                return this.builder();
            }
            public WASRegisterAgentFlowContext.Builder initialHeap(String initialHeap){
                this.initialHeap = initialHeap;
                return this.builder();
            }
            public WASRegisterAgentFlowContext.Builder maxHeap(String maxHeap){
                this.maxHeap = maxHeap;
                return this.builder();
            }
            public WASRegisterAgentFlowContext.Builder serverType(ApplicationServerType serverType) {
                this.serverType = serverType;
                return this.builder();
            }
            public WASRegisterAgentFlowContext.Builder serverXmlFilePath(String serverXmlFilePath) {
                this.serverXmlFilePath = serverXmlFilePath;
                return this.builder();
            }

            protected WASRegisterAgentFlowContext.Builder builder() {
                return this;
            }
        }
}
