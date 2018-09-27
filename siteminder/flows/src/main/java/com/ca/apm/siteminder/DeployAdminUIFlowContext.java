package com.ca.apm.siteminder;

import java.io.File;
import java.net.URL;
import java.util.LinkedHashSet;
import java.util.Set;

import org.apache.http.util.Args;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.ca.apm.automation.action.flow.IBuilder;
import com.ca.apm.automation.action.flow.IFlowContext;
import com.ca.apm.automation.action.responsefile.Triplet;

public class DeployAdminUIFlowContext implements IFlowContext {

    private final File installLocation;
    private final File responseFileDir;
    private final File auiPackedInstallSourcesLocation;
    private final File auiUnpackedInstallSourcesDir;
    private final URL auiprPackedInstallSourcesUrl;
    private final URL auiPackedInstallSourcesUrl;
    private final String auiExecutable;
    private final String auiPrereqExecutable;


    private DeployAdminUIFlowContext(Builder b) {
        installLocation = b.installLocation;
        responseFileDir = b.responseFileDir;
        auiPackedInstallSourcesLocation = b.auiPackedInstallSourcesLocation;
        auiUnpackedInstallSourcesDir = b.auiUnpackedInstallSourcesDir;
        auiPackedInstallSourcesUrl = b.auiPackedInstallSourcesUrl;
        auiprPackedInstallSourcesUrl = b.auiprPackedInstallSourcesUrl;
        auiExecutable = b.auiExecutable;
        auiPrereqExecutable = b.auiPrereqExecutable;
    }

    public Set<Triplet> getInstallResponseFileData() {
        Set<Triplet> set = new LinkedHashSet<>();
        String hostname = System.getenv("COMPUTERNAME");

        set.add(new Triplet("", "EPM_DEFAULT_INSTALL_DIR", this.installLocation.toString().replace("\\", "\\\\")));
        set.add(new Triplet("", "APP_SERVER_HOST", hostname == null ? "127.0.0.1" : hostname));
        set.add(new Triplet("", "PARAM_PORT", "8080"));
        set.add(new Triplet("", "ACCEPT_LGPL_EULA", "YES"));

        return set;
    }

    public File getAUIHomeDir() {
        return installLocation;
    }

    public File getResponseFileDir() {
        return responseFileDir;
    }

    public File getAUIPackedInstallSourcesDir() {
        return auiPackedInstallSourcesLocation;
    }

    public File getAUIUnpackedSourcesDir() {
        return auiUnpackedInstallSourcesDir;
    }

    public URL getAUIPackedInstallSourcesURL() {
        return auiPackedInstallSourcesUrl;
    }

    public URL getAUIPRPackedInstallSourcesURL() {
        return auiprPackedInstallSourcesUrl;
    }

    public String getAUIExecutable() {
        return auiExecutable;
    }

    public String getAUIPrereqExecutable() {
        return auiPrereqExecutable;
    }


    public static class Builder implements IBuilder<DeployAdminUIFlowContext> {

        private File installLocation = new File("C:\\CA\\install");
        private File responseFileDir = new File("C:\\CA\\sourcesUnpacked\\install");
        private File auiPackedInstallSourcesLocation = new File("C:\\CA\\sourcesPacked\\install");
        private File auiUnpackedInstallSourcesDir = new File("C:\\CA\\sourcesUnpacked\\install");
        @Nullable
        private URL auiPackedInstallSourcesUrl;
        private URL auiprPackedInstallSourcesUrl;
        private String auiExecutable;
        private String auiPrereqExecutable;


        @Override
        public DeployAdminUIFlowContext build() {
            DeployAdminUIFlowContext flowContext = new DeployAdminUIFlowContext(this);
            Args.notNull(flowContext.auiprPackedInstallSourcesUrl, "Admin UI's pre-requisities installer URL");
            Args.notNull(flowContext.auiPackedInstallSourcesUrl, "Admin UI's installer URL");

            return flowContext;
        }

        public Builder installLocation(@NotNull File installLocation) {
            this.installLocation = installLocation;
            return this;
        }

        public Builder responseFileDir(@NotNull File responseFileDir) {
            this.responseFileDir = responseFileDir;
            return this;
        }

        public Builder auiPackedInstallSourcesLocation(@NotNull File installSourcesLocation) {
            this.auiPackedInstallSourcesLocation = installSourcesLocation;
            return this;
        }

        public Builder auiUnpackedInstallSourcesDir(@NotNull File auiUnpackedInstallSourcesDir) {
            this.auiUnpackedInstallSourcesDir = auiUnpackedInstallSourcesDir;
            return this;
        }

        public Builder auiPackedInstallSourcesUrl(@NotNull URL auiPackedInstallSourcesUrl) {
            this.auiPackedInstallSourcesUrl = auiPackedInstallSourcesUrl;
            return this;
        }

        public Builder auiprPackedInstallSourcesUrl(@NotNull URL auiprPackedInstallSourcesUrl) {
            this.auiprPackedInstallSourcesUrl = auiprPackedInstallSourcesUrl;
            return this;
        }

        public Builder auiExecutable(@NotNull String auiExecutable) {
            this.auiExecutable = auiExecutable;
            return this;
        }

        public Builder auiPrereqExecutable(@NotNull String auiPrereqExecutable) {
            this.auiPrereqExecutable = auiPrereqExecutable;
            return this;
        }

    }

    @Override
    public String toString() {
        return "DeployAdminUIFlowContext{" +
            ", installLocation=" + installLocation +
            ", responseFileDir=" + responseFileDir +
            ", auiPackedInstallSourcesLocation=" + auiPackedInstallSourcesLocation +
            ", auiUnpackedInstallSourcesDir=" + auiUnpackedInstallSourcesDir +
            ", auiExecutable=" + auiExecutable +
            ", auiPrereqExecutable=" + auiPrereqExecutable +
            '}';
    }

}
