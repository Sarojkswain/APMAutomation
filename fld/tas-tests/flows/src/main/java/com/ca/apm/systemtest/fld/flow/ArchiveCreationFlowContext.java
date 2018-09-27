/*
 * Copyright (c) 2016 CA. All rights reserved.
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
 * MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE. IN NO EVENT WILL CA BE
 * LIABLE TO THE END USER OR ANY THIRD PARTY FOR ANY LOSS OR DAMAGE, DIRECT OR
 * INDIRECT, FROM THE USE OF THIS SOFTWARE, INCLUDING WITHOUT LIMITATION, LOST
 * PROFITS, BUSINESS INTERRUPTION, GOODWILL, OR LOST DATA, EVEN IF CA IS
 * EXPRESSLY ADVISED OF SUCH LOSS OR DAMAGE.
 */

package com.ca.apm.systemtest.fld.flow;

import com.ca.apm.automation.action.core.IAutomationFlow;
import com.ca.apm.automation.action.flow.AutowireCapable;
import com.ca.apm.systemtest.fld.util.ArchiveUtils.ArchiveCompression;
import com.ca.apm.systemtest.fld.util.ArchiveUtils.ArchiveEntry;
import com.ca.apm.systemtest.fld.util.ArchiveUtils.ArchiveType;
import com.ca.tas.builder.BuilderBase;
import com.ca.tas.property.AbstractEnvPropertySerializer;
import com.ca.tas.property.EnvPropSerializable;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.apache.commons.lang.StringUtils;
import org.apache.http.util.Args;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;

/**
 * Archive creation flow context.
 *
 * @author haiva01
 */
public class ArchiveCreationFlowContext implements AutowireCapable,
    EnvPropSerializable<ArchiveCreationFlowContext> {

    public static final String DATA = "DATA";

    private final transient Serializer envPropSerializer;
    protected Data data = new Data();

    protected ArchiveCreationFlowContext() {
        envPropSerializer = new Serializer(this);
    }

    protected ArchiveCreationFlowContext(Builder b) {
        this();
        data.archiveType = b.archiveType;
        data.archiveCompression = b.archiveCompression;
        data.archivePath = b.archivePath;
        data.archiveEntries = b.archiveEntries.toArray(new ArchiveEntry[b.archiveEntries.size()]);
    }

    protected ArchiveCreationFlowContext(Data data) {
        this();
        this.data = data;
    }

    public ArchiveType getArchiveType() {
        return data.archiveType;
    }

    public ArchiveCompression getArchiveCompression() {
        return data.archiveCompression;
    }

    public String getArchivePath() {
        return data.archivePath;
    }

    public Collection<ArchiveEntry> getArchiveEntries() {
        return Arrays.asList(data.archiveEntries);
    }

    @NotNull
    @Override
    public Class<? extends IAutomationFlow> autowiredFlow() {
        return ArchiveCreationFlow.class;
    }

    @Override
    public Map<String, String> serialize(String key) {
        return envPropSerializer.serialize(key);
    }

    @Override
    public ArchiveCreationFlowContext deserialize(String key, Map<String, String> serializedData) {
        return envPropSerializer.deserialize(key, serializedData);
    }

    public static class Serializer extends
        AbstractEnvPropertySerializer<ArchiveCreationFlowContext> {
        private static final Logger log = LoggerFactory.getLogger(Serializer.class);
        private ArchiveCreationFlowContext flowContext;
        private Gson gson = new GsonBuilder().create();

        public Serializer(ArchiveCreationFlowContext flowContext) {
            super(Serializer.class);
            this.flowContext = flowContext;
        }

        @Override
        public ArchiveCreationFlowContext deserialize(String key,
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

            return new ArchiveCreationFlowContext(data);
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

    public static class Builder extends BuilderBase<Builder, ArchiveCreationFlowContext> {
        private ArchiveType archiveType = ArchiveType.TAR;
        private ArchiveCompression archiveCompression = ArchiveCompression.GZIP;
        private String archivePath;
        private Collection<ArchiveEntry> archiveEntries = new ArrayList<>(3);

        @Override
        protected ArchiveCreationFlowContext getInstance() {
            return new ArchiveCreationFlowContext(this);
        }

        @Override
        protected Builder builder() {
            return this;
        }

        @Override
        public ArchiveCreationFlowContext build() {
            Args.notBlank(archivePath, "Archive path must not be blank");
            return getInstance();
        }

        /**
         * Set archive type.
         *
         * @param type archive type
         */
        public Builder type(ArchiveType type) {
            archiveType = type;
            return builder();
        }

        /**
         * Set compression method.
         *
         * @param compression compression method
         */
        public Builder compression(ArchiveCompression compression) {
            archiveCompression = compression;
            return builder();
        }

        /**
         * Set destination archive path.
         *
         * @param path archive path
         */
        public Builder path(String path) {
            archivePath = path;
            return builder();
        }

        /**
         * Add one archive entry. This method can be called multiple times with different
         * {@link ArchiveEntry} instances.
         *
         * @param entry archive entry.
         */
        public Builder entry(ArchiveEntry entry) {
            archiveEntries.add(entry);
            return builder();
        }
    }

    protected static class Data {
        public ArchiveType archiveType = ArchiveType.TAR;
        public ArchiveCompression archiveCompression = ArchiveCompression.GZIP;
        public String archivePath;
        public ArchiveEntry[] archiveEntries;
    }
}
