/**
 * 
 */
package com.ca.apm.systemtest.fld.role;

import java.net.URL;

import com.ca.apm.automation.action.flow.utility.GenericFlow;
import com.ca.apm.automation.action.flow.utility.GenericFlowContext;
import com.ca.apm.automation.action.utils.TasFileUtils;
import com.ca.apm.systemtest.fld.artifact.thirdparty.GroovyBinaryArtifact;
import com.ca.tas.builder.BuilderBase;
import com.ca.tas.client.IAutomationAgentClient;
import com.ca.tas.resolver.ITasResolver;
import com.ca.tas.role.AbstractRole;

/**
 * Provides an installation of Groovy
 * @author keyja01
 *
 */
public class GroovyRole extends AbstractRole {
    private GenericFlowContext downloadGroovyContext;
    private String installDir;

    /**
     * @param roleId
     * @param envPropertyContainer
     */
    public GroovyRole(Builder builder) {
        super(builder.roleId, builder.getEnvProperties());
        this.downloadGroovyContext = builder.downloadGroovyContext;
        this.installDir = builder.installDir;
    }

    /* (non-Javadoc)
     * @see com.ca.tas.role.Deployable#deploy(com.ca.tas.client.IAutomationAgentClient)
     */
    @Override
    public void deploy(IAutomationAgentClient aaClient) {
        runFlow(aaClient, GenericFlow.class, downloadGroovyContext);
    }
    
    public static class Builder extends BuilderBase<Builder, GroovyRole> {
        private String roleId;
        private GroovyBinaryArtifact groovyArtifact;
        private GenericFlowContext downloadGroovyContext;
        private ITasResolver resolver;
        private String installDir;

        public Builder(String roleId, ITasResolver resolver) {
            this.roleId = roleId;
            this.resolver = resolver;
            groovyArtifact = GroovyBinaryArtifact.v2_4_6;
        }
        
        public Builder groovyArtifact(GroovyBinaryArtifact groovyArtifact) {
            this.groovyArtifact = groovyArtifact;
            return this;
        }
        
        
        public Builder installDir(String installDir) {
            this.installDir = installDir;
            return this;
        }

        @Override
        public GroovyRole build() {
            URL artifactUrl = resolver.getArtifactUrl(groovyArtifact);
            String basename = TasFileUtils.getBasename(artifactUrl);
            if (installDir == null) {
                String base = getDeployBase();
                if (!base.endsWith("\\")) {
                    base = base + "\\";
                }
                installDir = base + basename;
            }
            downloadGroovyContext = new GenericFlowContext.Builder(artifactUrl)
                .destination(installDir)
                .build();
            
            GroovyRole role = getInstance();
            return role;
        }

        @Override
        protected GroovyRole getInstance() {
            return new GroovyRole(this);
        }

        @Override
        protected Builder builder() {
            return this;
        }
        
    }

    public String getInstallDir() {
        return installDir;
    }

    public void setInstallDir(String installDir) {
        this.installDir = installDir;
    }
}
