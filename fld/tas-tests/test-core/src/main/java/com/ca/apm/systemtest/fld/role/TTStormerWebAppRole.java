package com.ca.apm.systemtest.fld.role;

import org.eclipse.aether.artifact.Artifact;

import com.ca.apm.systemtest.fld.artifact.TTStormerWebAppArtifact;
import com.ca.tas.artifact.ITasArtifact;
import com.ca.tas.resolver.ITasResolver;
import com.ca.tas.role.web.WebAppRole;
import com.ca.tas.role.webapp.IWebAppServerRole;

/**
 * 
 * Role to deploy a test web application used for generating transaction trace storm load.
 * 
 * @author Alexander Sinyushkin (sinal04@ca.com)
 *
 */
public class TTStormerWebAppRole<T extends IWebAppServerRole> extends WebAppRole<T> {
    private Artifact webAppArtifact;
    private String webAppContext;

    protected TTStormerWebAppRole(Builder<T> builder) {
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

    /**
     * Builder for {@link TTStormerWebAppRole}.
     *  
     * @author Alexander Sinyushkin (sinal04@ca.com)
     *
     */
    public static final class Builder<U extends IWebAppServerRole> extends WebAppRole.Builder<U> {
        private ITasResolver resolver;
        private Artifact artifact;
        private String ctxName;

        public Builder(String roleId, ITasResolver resolver) {
            super(roleId);
            this.resolver = resolver;
            this.cargoDeploy();
        }
        
        @Override
        protected void initWebAppArtifact() {
            if (webAppArtifact == null) {
                ITasArtifact afact = new TTStormerWebAppArtifact(resolver).createArtifact();
                webAppArtifact = afact.getArtifact();
            }
            artifact = webAppArtifact;
        }
        
        @Override
        protected void initContextName() {
            if (contextName == null) {
                contextName = "tt-stormer";
            }
            ctxName = contextName;
        }
        
        @Override
        public TTStormerWebAppRole<U> build() {
            TTStormerWebAppRole<U> webAppRole = (TTStormerWebAppRole<U>) super.build();
            return webAppRole;
        }

    }

}
