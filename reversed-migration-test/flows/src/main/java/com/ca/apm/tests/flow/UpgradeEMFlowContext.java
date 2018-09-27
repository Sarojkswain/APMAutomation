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

package com.ca.apm.tests.flow;

import com.ca.apm.automation.action.flow.IFlowContext;
import com.ca.apm.automation.action.flow.em.EmFeature;
import com.ca.tas.annotation.TasEnvironmentProperty;
import com.ca.tas.annotation.TasEnvironmentPropertyKey;
import com.ca.tas.annotation.TasResource;
import com.ca.tas.builder.BuilderBase;
import com.ca.tas.property.AbstractEnvPropertySerializer;
import com.ca.tas.property.EnvPropSerializable;
import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.base.MoreObjects;
import com.google.common.collect.Collections2;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.http.util.Args;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

/**
 * @author Jan Pojer (pojja01@ca.com)
 * @see UpgradeEMFlow
 */
public class UpgradeEMFlowContext implements IFlowContext, EnvPropSerializable<UpgradeEMFlowContext> {

    public static final String DATA = "DATA";

    public static final String CAEULA_FILENAME = "ca-eula.txt";
    @TasEnvironmentPropertyKey
    public static final String ENV_EM_INSTALL_PROPERTIES = "installerProperties";
    @TasEnvironmentPropertyKey
    public static final String ENV_EM_UPGRADE_CA_EULA_PATH = "caEulaPath";

    private Data data = new Data();
    private final transient Serializer envPropSerializer = new Serializer(this);

    private UpgradeEMFlowContext(final Builder b) {
        this.data.introscopeArtifactUrl = b.introscopeArtifactUrl;
        this.data.eulaArtifactUrl = b.eulaArtifactUrl;
        this.data.olderEmInstallDir = b.olderEmInstallDir;
        this.data.emInstallerProperties = b.installerProperties;
        this.data.sampleResponseFile = b.sampleResponseFile;
        this.data.installerDir = b.installerDir;
        this.data.caEulaPath = b.caEulaPath;
        this.data.emFeatures = b.emFeatures;
    }

    protected UpgradeEMFlowContext(Data data) {
        this.data = data;
    }


    @TasResource(value = "emLogs", regExp = ".*log$")
    public String getOlderEmInstallDir() {
        return this.data.olderEmInstallDir;
    }

    public URL getInstallerUrl() {
        return this.data.introscopeArtifactUrl;
    }

    @NotNull
    @TasEnvironmentProperty(ENV_EM_INSTALL_PROPERTIES)
    public Map<String, String> getInstallerProperties() {
        return this.data.emInstallerProperties;
    }

    public String getSampleResponseFile() {
        return this.data.sampleResponseFile;
    }

    public String getInstallerDir() {
        return this.data.installerDir;
    }

    public EnumSet<EmFeature> getEmFeatures() {
        return this.data.emFeatures;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("emInstallerProperties", this.data.emInstallerProperties)
                .add("introscopeArtifactUrl", this.data.introscopeArtifactUrl)
                .add("eulaArtifactUrl", this.data.eulaArtifactUrl)
                .add("olderEmInstallDir", this.data.olderEmInstallDir)
                .add("sampleResponseFile", this.data.sampleResponseFile)
                .toString();
    }

    @TasEnvironmentProperty(ENV_EM_UPGRADE_CA_EULA_PATH)
    public String getCaEulaPath() {
        return this.data.caEulaPath;
    }

    @Override
    public Map<String, String> serialize(String key) {
        return envPropSerializer.serialize(key);
    }

    @Override
    public UpgradeEMFlowContext deserialize(String key, Map<String, String> serializedData) {
        return envPropSerializer.deserialize(key, serializedData);
    }

    public static class LinuxBuilder extends Builder {

        @Override
        protected Builder builder() {
            return this;
        }

        @Override
        protected String getPathSeparator() {
            return LINUX_SEPARATOR;
        }
    }

    public static class Builder extends BuilderBase<Builder, UpgradeEMFlowContext> {

        //default additional java options if none specified
        private static final Joiner JOINER = Joiner.on(",").skipNulls();
        private static final String DEFAULT_RESPONSE_FILE = "/SampleResponseFile.IntroscopeUpgrade.txt";
        private static final String INSTALLER_DIR = "em-upgrade-installer";

        private final Map<String, String> installerProperties = new HashMap<>();
        private final Map<String, String> customInstallerProperties = new HashMap<>();
        private final EnumSet<EmFeature> emFeatures = EnumSet.noneOf(EmFeature.class);
        protected String installerDir;
        @Nullable
        private URL introscopeArtifactUrl;
        @Nullable
        private String introscopeVersion;
        @Nullable
        private URL eulaArtifactUrl;
        private String olderEmInstallDir;
        @Nullable
        private boolean changeInstallDirDuringUpgrade = false;
        private Boolean useOracle;
        private String dbhost = StringUtils.EMPTY;
        private Integer dbport;
        private String dbname = StringUtils.EMPTY;
        private String dbuser = StringUtils.EMPTY;
        private String dbpassword = StringUtils.EMPTY;
        private String dbAdminUser = StringUtils.EMPTY;
        private String dbAdminPassword = StringUtils.EMPTY;
        private String databaseDir = StringUtils.EMPTY;
        private String sampleResponseFile;
        private String customUpgradeInstallDir;
        private String caEulaPath = StringUtils.EMPTY;

        @Override
        @NotNull
        public UpgradeEMFlowContext build() {

            Args.notNull(this.introscopeVersion, "Introscope version");

            if (StringUtils.isBlank(this.sampleResponseFile)) {
                verifyDbProperties();
            }
            initInstallerProperties();

            if (this.changeInstallDirDuringUpgrade) {
                Args.notNull(this.customUpgradeInstallDir, "Desired upgrade dir name change");
            }

            this.sampleResponseFile = initWithDefault(this.sampleResponseFile, DEFAULT_RESPONSE_FILE);

            final UpgradeEMFlowContext flowContext = getInstance();
            Args.notNull(flowContext.data.introscopeArtifactUrl, "Introscope artifact URL");
            Args.notNull(flowContext.data.emInstallerProperties, "EM installer properties");
            Args.notNull(flowContext.data.olderEmInstallDir, "Existing Introscope install dir");
            Args.notEmpty(flowContext.data.emFeatures, "EM features must be set");

            return flowContext;
        }

        private void verifyDbProperties() {
            Args.positive(this.dbport, "DB port must be positive");
            Args.notBlank(this.dbhost, "DB host must be set");
            Args.notBlank(this.dbname, "DB name must be set");
            Args.notBlank(this.dbuser, "DB user must be set");
            Args.notBlank(this.dbpassword, "DB password must be set");

            if (this.emFeatures.contains(EmFeature.DATABASE)) {
                Args.notBlank(this.databaseDir, "DB database dir must be set");
            }
        }

        /**
         * Initializes default response file properties
         */
        private void initInstallerProperties() {
            //points to valid introscope installation
            this.installerProperties.put("USER_INSTALL_DIR", this.olderEmInstallDir);
            this.installerProperties.put("shouldUpgrade", String.valueOf(true));

            this.installerProperties.put("silentInstallChosenFeatures", JOINER.join(toEmFeatureValues()));
            this.installerProperties.put("ca-eulaFile", CAEULA_FILENAME);

            putIfNotBlank("chosenDatabaseIsPostgres", this.useOracle != null ? String.valueOf(!this.useOracle) : null);
            putIfNotBlank("chosenDatabaseIsOracle", this.useOracle != null ? String.valueOf(this.useOracle) : null);
            putIfNotBlank("dbHost", this.dbhost);
            putIfNotBlank("dbPort", dbport != null ? this.dbport.toString() : null);
            putIfNotBlank("dbName", this.dbname);
            putIfNotBlank("dbUser", this.dbuser);
            putIfNotBlank("dbPassword", this.dbpassword);
            putIfNotBlank("dbAdminUser", this.dbAdminUser);
            putIfNotBlank("dbAdminPassword", this.dbAdminPassword);
            putIfNotBlank("databaseDir", this.databaseDir);
            putIfNotBlank("newPgInstallDir", this.databaseDir);
            boolean upgradeSchema =false;
            boolean validateDatabase = false;
            if (this.emFeatures.contains(EmFeature.DATABASE)) {
                upgradeSchema = true;
                validateDatabase = true;
            }
            this.installerProperties.put("validateDatabase", String.valueOf(validateDatabase));
            this.installerProperties.put("upgradeSchema", String.valueOf(upgradeSchema));
            if (this.changeInstallDirDuringUpgrade) {
                this.installerProperties.put("upgradedInstallDir", this.customUpgradeInstallDir);
            }

            this.installerProperties.putAll(this.customInstallerProperties);
        }

        private void putIfNotBlank(final String key, final String value) {
            if (StringUtils.isNotBlank(value)) {
                this.installerProperties.put(key, value);
            }
        }

        @NotNull
        private Collection<String> toEmFeatureValues() {
            return new HashSet<>(Collections2.transform(this.emFeatures, new Function<EmFeature, String>() {
                @Override
                public String apply(final EmFeature input) {
                    return input.getValue();
                }
            }));
        }

        public Builder olderEmInstallDir(final String olderEmInstallDir) {
            this.olderEmInstallDir = olderEmInstallDir;
            if (this.installerDir == null) {
                installerDir(FilenameUtils.getFullPathNoEndSeparator(FilenameUtils.normalizeNoEndSeparator(olderEmInstallDir)) + getPathSeparator() + INSTALLER_DIR);
            }
            return builder();
        }        @Override
        protected Builder builder() {
            return this;
        }

        public Builder installerDir(final String installerDir) {
            this.installerDir = FilenameUtils.normalizeNoEndSeparator(installerDir, LINUX_SEPARATOR.equals(getPathSeparator()));
            return builder();
        }

        public Builder introscopeUrl(final URL introscopeArtifactUrl) {
            this.introscopeArtifactUrl = introscopeArtifactUrl;
            return builder();
        }

        public Builder introscopeVersion(@NotNull final String introscopeVersion) {
            this.introscopeVersion = introscopeVersion;
            return builder();
        }        @Override
        protected UpgradeEMFlowContext getInstance() {
            return new UpgradeEMFlowContext(this);
        }

        public Builder eulaUrl(final URL eulaArtifactUrl) {
            this.eulaArtifactUrl = eulaArtifactUrl;
            return builder();
        }

        public Builder installerProp(final String key, final String value) {
            Args.notNull(key, "Property key");
            Args.notNull(value, "Property value");
            this.customInstallerProperties.put(key, value);

            return builder();
        }

        public Builder changeInstallDirDuringUpgrade(final String customUpgradeInstallDir) {
            this.changeInstallDirDuringUpgrade = true;
            this.customUpgradeInstallDir = customUpgradeInstallDir;

            return builder();
        }

        public Builder silentInstallChosenFeatures(final Collection<String> silentInstallChosenFeatures) {
            Args.notNull(silentInstallChosenFeatures, "Silent install chosen features");

            for (final String silentInstallChosenFeature : silentInstallChosenFeatures) {
                this.emFeatures.add(EmFeature.from(silentInstallChosenFeature));
            }

            return builder();
        }

        public Builder useOracle() {
            this.useOracle = true;

            return builder();
        }

        public Builder dbhost(final String dbhost) {
            if (StringUtils.isNotBlank(dbhost)) {
                this.dbhost = dbhost;
            }
            return builder();
        }

        public Builder dbport(final int dbport) {
            if (dbport > 0) {
                this.dbport = dbport;
            }
            return builder();
        }

        public Builder dbname(@NotNull final String dbname) {
            if (StringUtils.isNotBlank(dbname)) {
                this.dbname = dbname;
            }
            return builder();
        }

        public Builder dbuser(@NotNull final String dbuser) {
            if (StringUtils.isNotBlank(dbuser)) {
                this.dbuser = dbuser;
            }
            return builder();
        }

        public Builder dbpassword(@NotNull final String dbpassword) {
            if (StringUtils.isNotBlank(dbpassword)) {
                this.dbpassword = dbpassword;
            }
            return builder();
        }

        public Builder dbAdminUser(@NotNull final String dbAdminUser) {
            if (StringUtils.isNotBlank(dbAdminUser)) {
                this.dbAdminUser = dbAdminUser;
            }
            return builder();
        }

        public Builder dbAdminPassword(@NotNull final String dbAdminPassword) {
            if (StringUtils.isNotBlank(dbAdminPassword)) {
                this.dbAdminPassword = dbAdminPassword;
            }
            return builder();
        }

        public Builder databaseDir(@NotNull final String databaseDir) {
            if (StringUtils.isNotBlank(databaseDir)) {
                this.databaseDir = databaseDir;
            }
            return builder();
        }

        public Builder caEulaPath(final String caEulaPath) {
            if (StringUtils.isNotBlank(caEulaPath)) {
                this.caEulaPath = caEulaPath;
            }
            return builder();
        }

        public Builder sampleResponseFile(final String sampleResponseFile) {
            if (StringUtils.isNotBlank(sampleResponseFile)) {
                this.sampleResponseFile = sampleResponseFile;
            }
            return builder();
        }
    }
    public static class Serializer extends AbstractEnvPropertySerializer<UpgradeEMFlowContext> {
        private static final Logger log = LoggerFactory .getLogger(Serializer.class);
        private UpgradeEMFlowContext flowContext;
        private Gson gson = new GsonBuilder().create();

        public Serializer(UpgradeEMFlowContext flowContext) {
            super(Serializer.class);
            this.flowContext = flowContext;
        }

        @Override
        public UpgradeEMFlowContext deserialize(String key,
                                                              Map<String, String> serializedData) {
            log.debug("Serialized data: {}", serializedData);
            Map<String, String> deserializedMap = deserializeMapWithKey(key, serializedData);
            log.debug("Deserialized data: {}", deserializedMap);
            String jsonStr = deserializedMap.get(DATA);
            if (StringUtils.isBlank(jsonStr)) {
                throw new IllegalArgumentException("Empty deserialized data");
            }

            Data data = gson.fromJson(jsonStr, Data.class);
            if (data == null) {
                throw new IllegalArgumentException("JSON deserialization failure");
            }

            return new UpgradeEMFlowContext(data);
        }

        @Override
        public Map<String, String> serialize(String key) {
            Args.notNull(flowContext, "Flow context");

            Map<String, String> serializeMap = super.serialize(key);
            serializeMap.putAll(
                    serializeMapWithKey(key,
                            Collections.singletonMap(DATA, gson.toJson(flowContext.data))));

            return serializeMap;
        }
    }

    protected static class Data {
        private URL introscopeArtifactUrl;
        private URL eulaArtifactUrl;
        private String olderEmInstallDir;
        private String sampleResponseFile;
        private Map<String, String> emInstallerProperties;
        private String installerDir;
        private String caEulaPath;
        private EnumSet<EmFeature> emFeatures;
        private transient Serializer envPropSerializer;
    }
}
