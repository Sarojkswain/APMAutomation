/**
 * 
 */
package com.ca.apm.systemtest.fld.role.loads;

import java.net.URL;

import org.eclipse.aether.artifact.Artifact;

import com.ca.apm.automation.action.flow.utility.GenericFlow;
import com.ca.apm.automation.action.flow.utility.GenericFlowContext;
import com.ca.apm.automation.action.utils.TasFileUtils;
import com.ca.tas.artifact.IArtifactVersion;
import com.ca.tas.artifact.built.WurlitzerArtifact;
import com.ca.tas.builder.BuilderBase;
import com.ca.tas.client.IAutomationAgentClient;
import com.ca.tas.resolver.ITasResolver;
import com.ca.tas.role.AbstractRole;

/**
 * Role to download and and unpack the Wurlitzer stress application for use by multiple tests
 * @author keyja01
 *
 */
public class WurlitzerBaseRole extends AbstractRole {
    private GenericFlowContext wurlitzerFlowContext;
    private String deployDir;


    public WurlitzerBaseRole(Builder builder) {
        super(builder.roleId);
        this.wurlitzerFlowContext = builder.wurlitzerFlowContext;
        this.deployDir = builder.deployDir;
    }
    
    
    public String getDeployDir() {
        return deployDir;
    }

    /* (non-Javadoc)
     * @see com.ca.tas.role.Deployable#deploy(com.ca.tas.client.IAutomationAgentClient)
     */
    @Override
    public void deploy(IAutomationAgentClient aaClient) {
        // attempt to deploy the wurlitzer zip
        runFlow(aaClient, GenericFlow.class, wurlitzerFlowContext, 300);
    }
    
    
    public static class Builder extends BuilderBase<Builder, WurlitzerBaseRole> {
        protected String roleId;
        protected String deployDir;
        private ITasResolver tasResolver;
        private String wurlitzerVersion;
        private GenericFlowContext wurlitzerFlowContext;
        
        public Builder(String roleId, ITasResolver tasResolver) {
            this.roleId = roleId;
            this.tasResolver = tasResolver;
        }
        
        public Builder deployDir(String deployDir) {
            this.deployDir = deployDir;
            return this;
        }
        
        public Builder version(IArtifactVersion version) {
            this.wurlitzerVersion = version.toString();
            return this;
        }
        
        public Builder version(String version) {
            this.wurlitzerVersion = version;
            return this;
        }
        
        @Override
        public WurlitzerBaseRole build() {
            initWurlitzerFlowContext();
            
            WurlitzerBaseRole role = getInstance();
            return role;
        }
        
        
        private void initWurlitzerFlowContext() {
            Artifact wurlitzerArtifact = new WurlitzerArtifact(tasResolver).createArtifact(wurlitzerVersion).getArtifact();
            URL artifactURL = tasResolver.getArtifactUrl(wurlitzerArtifact);
            GenericFlowContext.Builder wurlitzerFlowContextBuilder = new GenericFlowContext.Builder();
            String deployBase = getDeployBase();
            String wurlitzerDestination = deployBase + TasFileUtils.getBasename(artifactURL);
            deployDir = wurlitzerDestination;
            
            wurlitzerFlowContext = wurlitzerFlowContextBuilder.artifactUrl(artifactURL).destination(wurlitzerDestination).build();
            
        }
        
        
        @Override
        protected Builder builder() {
            return this;
        }

        @Override
        protected WurlitzerBaseRole getInstance() {
            return new WurlitzerBaseRole(this);
        }
        
    }

}
