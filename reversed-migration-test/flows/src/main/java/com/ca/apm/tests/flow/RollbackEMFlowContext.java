/*
 * Copyright (c) 2017 CA.  All rights reserved.
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
import com.ca.tas.annotation.TasResource;
import com.ca.tas.builder.BuilderBase;
import com.ca.tas.property.AbstractEnvPropertySerializer;
import com.ca.tas.property.EnvPropSerializable;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.http.util.Args;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.Map;

/**
 * Rollback EM Flow Context
 *
 * @author dugra04
 */
public class RollbackEMFlowContext implements IFlowContext, EnvPropSerializable<RollbackEMFlowContext> {
    public static final String DATA = "DATA";

//    private final String olderEmInstallDir;
//    private final String apmRollbackDirectory;
//    private final boolean allowFinishedRollback;
//    private final boolean doCleanupOnly;

    private Data data = new Data();
    private final transient Serializer envPropSerializer = new RollbackEMFlowContext.Serializer(this);

    private RollbackEMFlowContext(Data d) {
        this.data = d;
    }

    private RollbackEMFlowContext(final Builder b) {
        this.data.olderEmInstallDir = b.olderEmInstallDir;
        this.data.apmRollbackDirectory = b.apmRollbackDirectory;
        this.data.allowFinishedRollback = b.allowFinishedRollback;
        this.data.doCleanupOnly = b.doCleanupOnly;
        this.data.useReversedMigration = b.useReversedMigration;
        this.data.isLinux = b.isLinux;
    }

    @TasResource(value = "em", regExp = ".*log$")
    public String getOlderEmInstallDir() {
        return this.data.olderEmInstallDir;
    }

    public String getApmRollbackDirectory() {
        return this.data.apmRollbackDirectory;
    }

    public boolean isAllowFinishedRollback() {
        return this.data.allowFinishedRollback;
    }

    public boolean isDoCleanupOnly() {
        return this.data.doCleanupOnly;
    }

    public boolean isUseReversedMigration() {
        return this.data.useReversedMigration;
    }

    public boolean isLinux() {
        return this.data.isLinux;
    }

    @Override
    public Map<String, String> serialize(String key) {
        return envPropSerializer.serialize(key);
    }

    @Override
    public RollbackEMFlowContext deserialize(String key, Map<String, String> serializedData) {
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

    public static class Builder extends BuilderBase<Builder, RollbackEMFlowContext> {

        private static final String INSTALLER_DIR = "APMRollback";
        private static final String DEFAULT_RESPONSE_FILE = "SampleResponseFile.Rollback.txt";

        protected String apmRollbackDirectory;
        private String olderEmInstallDir;
        private String sampleResponseFile;
        private boolean allowFinishedRollback;
        private boolean doCleanupOnly;
        private boolean useReversedMigration = false;
        private boolean isLinux = true;

        @Override
        public RollbackEMFlowContext build() {
            this.sampleResponseFile = initWithDefault(this.sampleResponseFile, DEFAULT_RESPONSE_FILE);

            final RollbackEMFlowContext flowContext = getInstance();
            Args.notNull(flowContext.data.olderEmInstallDir, "Existing Introscope install dir");

            return flowContext;
        }

        public Builder olderEmInstallDir(final String olderEmInstallDir) {
            this.olderEmInstallDir = olderEmInstallDir;
            if (this.apmRollbackDirectory == null) {
                apmRollbackDirectory(FilenameUtils.getFullPathNoEndSeparator(FilenameUtils.normalizeNoEndSeparator(olderEmInstallDir,
                        getPathSeparator().equals(LINUX_SEPARATOR))) + getPathSeparator() + INSTALLER_DIR);
            }
            return builder();
        }

        public Builder apmRollbackDirectory(final String apmRollbackDirectory) {
            this.apmRollbackDirectory = apmRollbackDirectory;
            return builder();
        }

        public Builder allowFinishedRollback(final boolean allowFinishedRollback) {
            this.allowFinishedRollback = allowFinishedRollback;
            return builder();
        }

        public Builder doCleanupOnly(final boolean doCleanupOnly) {
            this.doCleanupOnly = doCleanupOnly;
            return builder();
        }

        public Builder useReversedMigration() {
            this.useReversedMigration = true;
            return builder();
        }

        public Builder linux(boolean isLinux) {
            this.isLinux= isLinux;
            return builder();
        }

        @Override
        protected RollbackEMFlowContext getInstance() {
            return new RollbackEMFlowContext(this);
        }

        @Override
        protected Builder builder() {
            return this;
        }
    }

    public static class Serializer extends AbstractEnvPropertySerializer<RollbackEMFlowContext> {
        private static final Logger log = LoggerFactory.getLogger(Serializer.class);
        private RollbackEMFlowContext flowContext;
        private Gson gson = new GsonBuilder().create();

        public Serializer(RollbackEMFlowContext flowContext) {
            super(Serializer.class);
            this.flowContext = flowContext;
        }

        @Override
        public RollbackEMFlowContext deserialize(String key,
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

            return new RollbackEMFlowContext(data);
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
        protected String apmRollbackDirectory;
        private String olderEmInstallDir;
        private String sampleResponseFile;
        private boolean allowFinishedRollback;
        private boolean doCleanupOnly;
        private boolean useReversedMigration;
        private boolean isLinux;
    }

}