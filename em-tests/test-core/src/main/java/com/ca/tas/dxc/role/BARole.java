/*
 * Copyright (c) 2017 CA. All rights reserved.
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
package com.ca.tas.dxc.role;

import java.util.Arrays;
import java.util.Collection;
import com.ca.apm.automation.action.flow.utility.FileModifierFlow;
import com.ca.apm.automation.action.flow.utility.FileModifierFlowContext;
import com.ca.tas.builder.BuilderBase;
import com.ca.tas.client.IAutomationAgentClient;
import com.ca.tas.resolver.ITasResolver;
import com.ca.tas.role.AbstractRole;

/**
 *
 * @author banra06@ca.com
 */

public class BARole extends AbstractRole {

    private String snippetPath;
    private String dxcHost;
    private int dxcPort;

    protected BARole(Builder builder) {

        super(builder.roleId, builder.getEnvProperties());
        this.snippetPath = builder.snippetPath;
        this.dxcHost = builder.dxcHost;
        this.dxcPort = builder.dxcPort;
    }

    @Override
    public void deploy(IAutomationAgentClient aaClient) {
        createSnippet(aaClient);
    }

    private void createSnippet(IAutomationAgentClient aaClient) {
        Collection<String> data =
            Arrays
                .asList("<script id=\"BA_AXA\""
                    + " "
                    + "src=\"http://"
                    + dxcHost
                    + ":"
                    + Integer.toString(dxcPort)
                    + "/api/1/urn:ca:tenantId:default-tenant/urn:ca:appId:default-app/bajs?agent=browser\""
                    + " "
                    + "data-profileUrl=\"http://"
                    + dxcHost
                    + ":"
                    + Integer.toString(dxcPort)
                    + "/api/1/urn:ca:tenantId:default-tenant/urn:ca:appId:default-app/profile?agent=browser\""
                    + " "
                    + "data-tenantID=\"default-tenant\" data-appID=\"default-app\" data-appKey=\"b0361800-4736-11e6-b8e5-bddc75280658\"></script>");
        FileModifierFlowContext createFileFlow =
            new FileModifierFlowContext.Builder().create(snippetPath, data)
                .build();
        runFlow(aaClient, FileModifierFlow.class, createFileFlow);
    }


    public static class LinuxBuilder extends Builder {

        public LinuxBuilder(String roleId, ITasResolver tasResolver) {
            super(roleId, tasResolver);
        }
    }

    public static class Builder extends BuilderBase<Builder, BARole> {

        private final String roleId;
        private String snippetPath = "/opt/automation/deployed/snippet.basnippet";
        private String dxcHost = "localhost";
        private int dxcPort = 8080;

        public Builder(String roleId, ITasResolver tasResolver) {
            this.roleId = roleId;
        }

        @Override
        public BARole build() {
            return getInstance();
        }

        @Override
        protected BARole getInstance() {
            return new BARole(this);
        }

        @Override
        protected Builder builder() {
            return this;
        }

        public Builder snippetPath(String snippetPath) {
            this.snippetPath = snippetPath;
            return builder();
        }

        public Builder dxcHost(String dxcHost) {
            this.dxcHost = dxcHost;
            return builder();
        }

        public Builder dxcPort(int dxcPort) {
            this.dxcPort = dxcPort;
            return builder();
		}

	}
}
