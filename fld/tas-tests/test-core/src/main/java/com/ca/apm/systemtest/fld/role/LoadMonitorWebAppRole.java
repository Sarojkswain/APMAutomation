package com.ca.apm.systemtest.fld.role;

import org.eclipse.aether.artifact.Artifact;

import com.ca.apm.systemtest.fld.artifact.LoadMonitorVersion;
import com.ca.tas.artifact.ITasArtifact;
import com.ca.tas.resolver.ITasResolver;
import com.ca.tas.role.web.WebAppRole;
import com.ca.tas.role.webapp.TomcatRole;

public class LoadMonitorWebAppRole extends WebAppRole<TomcatRole> {
    private Artifact webAppArtifact;
    private String webAppContext;


    private LoadMonitorWebAppRole(Builder builder) {
        super(builder);
        this.webAppArtifact = builder.artifact;
        this.webAppContext = builder.ctxName;
    }

    
    @Override
    public Artifact getArtifact() {
        return webAppArtifact;
    }
    
    @Override
    public String getContextName() {
        return webAppContext;
    }
    
    
    public static final class Builder extends WebAppRole.Builder<TomcatRole> {
        private ITasResolver resolver;
        private Artifact artifact;
        private String ctxName;

        public Builder(String roleId, ITasResolver resolver) {
            super(roleId);
            this.resolver = resolver;
            // required to use cargo deploy
            this.cargoDeploy();
        }
        
        
        @Override
        protected void initWebAppArtifact() {
            if (webAppArtifact == null) {
                ITasArtifact afact = new LoadMonitorVersion(resolver).createArtifact();
                webAppArtifact = afact.getArtifact();
            }
            artifact = webAppArtifact;
        }
        
        @Override
        protected void initContextName() {
            if (contextName == null) {
                contextName = "loadmon";
            }
            ctxName = contextName;
        }
    }
}
