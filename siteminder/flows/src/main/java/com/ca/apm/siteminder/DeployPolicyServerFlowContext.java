package com.ca.apm.siteminder;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.LinkedHashSet;
import java.util.Set;

import org.apache.http.util.Args;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.ca.apm.automation.action.flow.IBuilder;
import com.ca.apm.automation.action.flow.IFlowContext;
import com.ca.apm.automation.action.responsefile.Triplet;

public class DeployPolicyServerFlowContext implements IFlowContext {
    private final String installLocation;
    private final File responseFileDir;
    private final File psPackedInstallSourcesLocation;
    private final File psUnpackedInstallSourcesDir;
    private final URL psPackedInstallSourcesUrl;
    private final String javaLocation;
    private final String psInstallExecutable;

    private DeployPolicyServerFlowContext(Builder b) {
        installLocation = b.installLocation;
        responseFileDir = b.responseFileDir;
        psPackedInstallSourcesLocation = b.psPackedInstallSourcesLocation;
        psUnpackedInstallSourcesDir = b.psUnpackedInstallSourcesDir;
        psPackedInstallSourcesUrl = b.psPackedInstallSourcesUrl;
        javaLocation = b.javaLocation;
        psInstallExecutable = b.psInstallExecutable;
    }

    public Set<Triplet> getInstallResponseFileData() {
        Set<Triplet> set = new LinkedHashSet<>();

        set.add(new Triplet("", "DEFAULT_INSTALL_DIR", this.installLocation.toString().replace("\\", "\\\\")));
        set.add(new Triplet("", "DEFAULT_JRE_ROOT", this.javaLocation.replace("\\", "\\\\")));
        set.add(new Triplet("", "ENCRYPTED_ENCRYPTKEY", "ENC:GhElIkxEvsRfTYoBFSTaDg=="));
        set.add(new Triplet("", "CA_SM_PS_FIPS140", "COMPAT"));
        set.add(new Triplet("", "DEFAULT_OVMGUI_CHOICE", "false"));
        set.add(new Triplet("", "DEFAULT_WEBSERVERS_CHOICE", "false"));
        set.add(new Triplet("", "DEFAULT_SNMP_CHOICE", "false"));
        set.add(new Triplet("", "DEFAULT_POLICYSTORE_CHOICE", "false"));
        set.add(new Triplet("", "DEFAULT_INIT_POLICYSTORE_CHOICE", "false"));
        set.add(new Triplet("", "DEFAULT_SMKEYDB_IMPORT_CHOICE", "false"));
        set.add(new Triplet("", "PASSWORD_RESULT", "Basic Password Services"));

        return set;
    }

    public String getPSHomeDir() {
        return installLocation;
    }

    public File getResponseFileDir() {
        return responseFileDir;
    }

    public File getPSPackedInstallSourcesDir() {
        return psPackedInstallSourcesLocation;
    }

    public File getPSUnpackedSourcesDir() {
        return psUnpackedInstallSourcesDir;
    }

    public URL getPSPackedInstallSourcesURL() throws MalformedURLException {
        return psPackedInstallSourcesUrl;
    }

    public String getJavaHomeDir() {
        return javaLocation;
    }

    public String getPsInstallExecutable() {
        return psInstallExecutable;
    }

    public static class Builder implements IBuilder<DeployPolicyServerFlowContext> {

        private String installLocation = "C:\\CA\\install\\siteminder";
        private String javaLocation;
        private File responseFileDir = new File("C:\\CA\\sourcesUnpacked\\install");
        private File psPackedInstallSourcesLocation = new File("C:\\CA\\sourcesPacked\\install");
        private File psUnpackedInstallSourcesDir = new File("C:\\CA\\sourcesUnpacked\\install");
        @Nullable
        private URL psPackedInstallSourcesUrl;
        private String psInstallExecutable;

        public DeployPolicyServerFlowContext build() {
            DeployPolicyServerFlowContext flowContext = new DeployPolicyServerFlowContext(this);
            Args.notNull(flowContext.psPackedInstallSourcesUrl, "PolicyServer's installer URL");

            return flowContext;
        }

        public Builder installLocation(@NotNull String installLocation) {
            this.installLocation = installLocation;
            return this;
        }

        public Builder javaLocation(@NotNull String javaLocation) {
            this.javaLocation = javaLocation;
            return this;
        }

        public Builder responseFileDir(@NotNull File responseFileDir) {
            this.responseFileDir = responseFileDir;
            return this;
        }

        public Builder psPackedInstallSourcesLocation(@NotNull File installSourcesLocation) {
            this.psPackedInstallSourcesLocation = installSourcesLocation;
            return this;
        }


        public Builder psUnpackedInstallSourcesDir(@NotNull File psUnpackedInstallSourcesDir) {
            this.psUnpackedInstallSourcesDir = psUnpackedInstallSourcesDir;
            return this;
        }

        public Builder psPackedInstallSourcesUrl(@NotNull URL psPackedInstallSourcesUrl) {
            this.psPackedInstallSourcesUrl = psPackedInstallSourcesUrl;
            return this;
        }

        public Builder psInstallExecutable(@NotNull String psInstallExecutable) {
            this.psInstallExecutable = psInstallExecutable;
            return this;
        }
    }

    @Override
    public String toString() {
        return "DeployPolicyServerFlowContext{" +
            ", installLocation=" + installLocation +
            ", javaLocation=" + javaLocation +
            ", responseFileDir=" + responseFileDir +
            ", psPackedInstallSourcesLocation=" + psPackedInstallSourcesLocation +
            ", psUnpackedInstallSourcesDir=" + psUnpackedInstallSourcesDir +
            ", psInstallExecutable=" + psInstallExecutable +
            '}';
    }

}
