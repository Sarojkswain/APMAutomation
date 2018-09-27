package com.ca.tas.role;

import org.apache.http.util.Args;
import org.eclipse.aether.artifact.Artifact;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.ca.apm.flow.DownloadInstallerFlow;
import com.ca.apm.flow.DownloadInstallerFlowContext;
import com.ca.tas.artifact.IArtifactVersion;
import com.ca.tas.artifact.IBuiltArtifact;
import com.ca.tas.artifact.IBuiltArtifact.ArtifactPlatform;
import com.ca.tas.artifact.TasArtifact;
import com.ca.tas.artifact.built.IntroscopeInstaller;
import com.ca.tas.builder.BuilderBase;
import com.ca.tas.client.IAutomationAgentClient;
import com.ca.tas.resolver.ITasResolver;

import static com.ca.tas.artifact.IBuiltArtifact.ArtifactPlatform.WINDOWS_AMD_64;
import static com.ca.tas.artifact.IBuiltArtifact.ArtifactPlatform.LINUX_AMD_64;

public class EmInstallerRole extends AbstractRole
{
    private DownloadInstallerFlowContext installerFlowContext;
    
    /**
     * @param builder Builder
     */
    private EmInstallerRole(Builder builder) {
        super(builder.roleId);
        installerFlowContext=builder.installerFlowContext;
    }

    @Override
    public void deploy(IAutomationAgentClient client)
    {
        runFlow(client, DownloadInstallerFlow.class, installerFlowContext);
    }
    public static class LinuxBuilder extends Builder {

        public LinuxBuilder(String roleId, ITasResolver tasResolver) {
            super(roleId, tasResolver);
            introscopePlatform(LINUX_AMD_64);
            flowContextBuilder = new DownloadInstallerFlowContext.LinuxBuilder(getEnvProperties());
        }

        @Override
        protected String getPathSeparator() {
            return LINUX_SEPARATOR;
        }

        @Override
        protected Builder builder() {
            return this;
        }
    }
    public static class Builder extends BuilderBase<Builder,EmInstallerRole>{
        
        private final String roleId;
        protected final ITasResolver tasResolver;
        
        protected Artifact introscopeArtifact;
        protected Artifact eulaArtifact;
        protected ArtifactPlatform introscopePlatform = WINDOWS_AMD_64;
        @Nullable
        protected String instroscopeVersion;
        @Nullable
        protected String eulaVersion;
        protected DownloadInstallerFlowContext.Builder flowContextBuilder;
        protected DownloadInstallerFlowContext installerFlowContext;
        
        public Builder(String roleId, ITasResolver tasResolver)
        {
            this.roleId=roleId;
            this.tasResolver=tasResolver;
            flowContextBuilder = new DownloadInstallerFlowContext.Builder(getEnvProperties());
        }
        @Override
        public EmInstallerRole build() {

            initIntroscopeArtifact();
            initEulaArtifact();
            initFlowContext();
            
            EmInstallerRole emInstallerRole = getInstance();
            
            return emInstallerRole;
        }
        protected void initIntroscopeArtifact() {
           if (introscopeArtifact != null) {
               return;
           }
           introscopeArtifact = new IntroscopeInstaller(introscopePlatform, tasResolver).createArtifact(instroscopeVersion).getArtifact();
       }
       
       protected void initEulaArtifact() {
           if (eulaArtifact != null) {
               return;
           }
           eulaArtifact =
               new TasArtifact.Builder("eula").extension(IBuiltArtifact.TasExtension.ZIP)
                   .version((eulaVersion == null) ? tasResolver.getDefaultVersion() : eulaVersion)
                   .build().getArtifact();
       }

       protected void initFlowContext() {

           assert introscopeArtifact != null : "Missing introscope artifact";
           assert eulaArtifact != null : "Missing EULA artifact";
           
           flowContextBuilder.introscopeVersion(introscopeArtifact.getVersion());
           flowContextBuilder.introscopeArtifactUrl(tasResolver.getArtifactUrl(introscopeArtifact));

           flowContextBuilder.eulaVersion(eulaArtifact.getVersion());
           flowContextBuilder.eulaArtifactUrl(tasResolver.getArtifactUrl(eulaArtifact));

           installerFlowContext = flowContextBuilder.build();
       }

        protected Builder builder()
        {
            return this;
        }
        protected EmInstallerRole getInstance()
        {
            return new EmInstallerRole(this);
        }
        public Builder instroscopeVersion(IArtifactVersion instroscopeVersion) {
            Args.notNull(instroscopeVersion, "instroscopeVersion");
            this.instroscopeVersion = instroscopeVersion.toString();
            return this;
        }

        public Builder instroscopeVersion(String instroscopeVersion) {
            Args.notNull(instroscopeVersion, "Introscope version");
            this.instroscopeVersion = instroscopeVersion;
            return this;
        }

        public Builder introscopePlatform(ArtifactPlatform introscopePlatform) {
            this.introscopePlatform = introscopePlatform;
            return this;
        }

        public Builder introscopeArtifact(Artifact introscopeArtifact) {
            this.introscopeArtifact = introscopeArtifact;
            return this;
        }
        public Builder eulaArtifact(Artifact eulaArtifact) {
            this.eulaArtifact = eulaArtifact;
            return this;
        }

        public Builder eulaVersion(@NotNull String eulaVersion) {
            Args.notNull(eulaVersion, "EULA version");
            this.eulaVersion = eulaVersion;
            return this;
        }
        
    }

}
