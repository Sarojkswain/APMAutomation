/*
* Copyright (c) 2014 CA.  All rights reserved.
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
 * MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE.  IN NO EVENT WILL CA BE 
 * LIABLE TO THE END USER OR ANY THIRD PARTY FOR ANY LOSS OR DAMAGE, DIRECT OR 
 * INDIRECT, FROM THE USE OF THIS SOFTWARE, INCLUDING WITHOUT LIMITATION, LOST 
 * PROFITS, BUSINESS INTERRUPTION, GOODWILL, OR LOST DATA, EVEN IF CA IS 
 * EXPRESSLY ADVISED OF SUCH LOSS OR DAMAGE.  
 */

package com.ca.apm.ant;

import java.util.Arrays;

import org.eclipse.aether.artifact.Artifact;

import com.ca.apm.automation.action.flow.commandline.RunCommandFlowContext;
import com.ca.apm.automation.action.flow.utility.UniversalFlow;
import com.ca.apm.automation.action.flow.utility.UniversalFlowContext;
import com.ca.tas.builder.BuilderBase;
import com.ca.tas.client.IAutomationAgentClient;
import com.ca.tas.resolver.ITasResolver;
import com.ca.tas.role.AbstractRole;

/**
 * note: this role is supposed to run on windows machnine
 * also it expects that environment variable ANT_HOME exists and is set on the PATH
 * 
 * @author svazd01
 *
 */
public class AntRole extends AbstractRole {

    private RunCommandFlowContext setAntOnPathFlowCtx;

    private UniversalFlowContext deployArtifactFlowCtx;

    protected AntRole(Builder builder) {
        super(builder.roleId, builder.getEnvProperties());
        setAntOnPathFlowCtx = builder.setAntOnPathFlowCtx;
        deployArtifactFlowCtx = builder.deployArtifactFlowCtx;
    }

    @Override
    public void deploy(IAutomationAgentClient aaClient) {
        runFlow(aaClient, UniversalFlow.class, deployArtifactFlowCtx);
        runCommandFlow(aaClient, setAntOnPathFlowCtx);
    }

    public static class Builder extends BuilderBase<Builder, AntRole> {

        protected final String roleId;
        protected final ITasResolver tasResolver;

        protected RunCommandFlowContext setAntOnPathFlowCtx;

        protected Artifact antArtifact = AntVersion.v1_9_3_zip.getArtifact();

        protected UniversalFlowContext deployArtifactFlowCtx;



        protected String antHome;

        public Builder(String roleId, ITasResolver tasResolver) {
            this.roleId = roleId;
            this.tasResolver = tasResolver;

        }

        private void initSetAntOnPathFlowCtx() {
            String[] args = {"ANT_HOME", antHome, "/m"};
            setAntOnPathFlowCtx =
                new RunCommandFlowContext.Builder("setx").args(Arrays.asList(args)).build();
        }

        @Override
        public AntRole build() {
            initDeployFlow();
            initSetAntOnPathFlowCtx();
            return new AntRole(this);

        }

        private void initDeployFlow() {

            antHome = concatPaths(getDeployBase(), antArtifact.getArtifactId());
            deployArtifactFlowCtx =
                new UniversalFlowContext.Builder().archive(
                    tasResolver.getArtifactUrl(antArtifact), antHome).build();
        }

        public Builder antVersion(AntVersion antVersion) {
            antArtifact = antVersion.getArtifact();
            return this;
        }



        @Override
        protected Builder builder() {
            return this;
        }

        @Override
        protected AntRole getInstance() {
            // TODO Auto-generated method stub
            return new AntRole(this);
        }

    }

}
