package com.ca.apm.flow;

import java.net.URL;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.ca.apm.automation.action.flow.IBuilder;
import com.ca.apm.automation.action.flow.IFlowContext;
import com.ca.tas.property.RolePropertyContainer;



public class DownloadInstallerFlowContext implements IFlowContext
{
    private final URL introscopeArtifactUrl;
    private final URL eulaArtifactUrl;
    private final String installerTargetDir;

    private DownloadInstallerFlowContext(Builder builder)
    {
        introscopeArtifactUrl = builder.introscopeArtifactUrl;
        eulaArtifactUrl = builder.eulaArtifactUrl;
        installerTargetDir = builder.installerTargetDir;
        
    }
    public URL getInstroscopeArtifactURL()
    {
        return introscopeArtifactUrl;
    }

    public URL getEulaArtifactURL()
    {
        return eulaArtifactUrl;
    }
    public String getinstallerTargetDir()
    {
        return installerTargetDir;
    }
    public static class LinuxBuilder extends Builder {
        
        public LinuxBuilder(RolePropertyContainer envProperties) {
            super(envProperties);
        }

        @Override
        protected Builder builder() {
            return this;
        }

    }
    public static class Builder implements IBuilder<DownloadInstallerFlowContext>
    {
        private URL introscopeArtifactUrl;
        private URL eulaArtifactUrl;
        private String installerTargetDir;
        
        @Nullable
        private String introscopeVersion;
        @Nullable
        private String eulaVersion;
        
        private final RolePropertyContainer envProperties;
        
        public Builder(RolePropertyContainer envProperties)
        {
            this.envProperties=envProperties;
            
        }
        public Builder introscopeArtifactUrl(URL value)
        {
            introscopeArtifactUrl = value;
            return this;
        }
        public Builder introscopeVersion(@NotNull String Version) {
            introscopeVersion = Version;
            return this;
        }
        public Builder eulaArtifactUrl(URL value)
        {
            eulaArtifactUrl = value;
            return this;
        }
        public Builder eulaVersion(@NotNull String Version) {
            eulaVersion = Version;
            return this;
        }
        public Builder installerTargetDir(String value)
        {
            installerTargetDir = value;
            return this;
        }
        @Override
        @NotNull
        public DownloadInstallerFlowContext build()
        {
            DownloadInstallerFlowContext InstallerFlowContext = new DownloadInstallerFlowContext(this);
            return InstallerFlowContext;
        }
        public DownloadInstallerFlowContext getInstance()
        {
            return new DownloadInstallerFlowContext(this);
        }
        protected Builder builder()
        {
            return this;
        }
    }
    
}