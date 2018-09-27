package com.ca.apm.tests.flow;

import com.ca.apm.automation.action.flow.IFlowContext;
import com.ca.apm.automation.action.flow.utility.VarSubstitutionFilter;
import com.ca.tas.builder.BuilderBase;
import org.apache.http.util.Args;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * Context for FileCreatorFlowContext
 *
 * Designed to create a single file from either another file (copy), resource or chunk of data
 *
 * @author Nick Giles (gilni04@ca.com)
 * @author Jan Pojer (pojja01@ca.com)
 * @see FileCreatorFlow
 */
public class FileCreatorFlowContext implements IFlowContext {

    @Nullable
    private final String destinationPath;
    @Nullable
    private final String destinationDir;
    @Nullable
    private final String destinationFilename;
    @NotNull
    private final Map<String, String> substitutionVariables;
    @NotNull
    private final Map<String, String> replacePairs;
    @Nullable
    private final String sourceFilePath;
    @Nullable
    private final String sourceResourcePath;
    @NotNull
    private final Collection<String> sourceData;
    @NotNull
    private final String charset;
    @NotNull
    private final String placeholder;

    protected FileCreatorFlowContext(final Builder builder) {
        destinationPath = builder.destinationPath;
        destinationDir = builder.destinationDir;
        destinationFilename = builder.destinationFilename;
        substitutionVariables = builder.substitutionVariables;
        replacePairs = builder.replacePairs;
        sourceFilePath = builder.sourceFilePath;
        sourceResourcePath = builder.sourceResourcePath;
        sourceData = builder.sourceData;
        charset = builder.charset;
        placeholder = builder.placeholder;
    }

    @Nullable
    public String getDestinationPath() {
        return destinationPath;
    }

    @Nullable
    public String getDestinationFilename() {
        return destinationFilename;
    }

    @Nullable
    public String getDestinationDir() {
        return destinationDir;
    }

    @NotNull
    public Map<String, String> getSubstitutionVariables() {
        return substitutionVariables;
    }

    @NotNull
    public Map<String, String> getReplacePairs() {
        return replacePairs;
    }

    @NotNull
    public Collection<String> getSourceData() {
        return sourceData;
    }

    @Nullable
    public String getSourceFilePath() {
        return sourceFilePath;
    }

    @Nullable
    public String getSourceResourcePath() {
        return sourceResourcePath;
    }

    @NotNull
    public String getCharset() {
        return charset;
    }

    @NotNull
    public String getPlaceholder() {
        return placeholder;
    }

    public static class Builder extends BuilderBase<Builder, FileCreatorFlowContext> {

        private static final Charset DEFAULT_CHARSET = StandardCharsets.UTF_8;
        private final Map<String, String> substitutionVariables = new HashMap<>();
        private final Map<String, String> replacePairs = new HashMap<>();
        private final Collection<String> sourceData = new ArrayList<>();
        private final String charset;
        private final String placeholder;
        private String sourceFilePath;
        private String sourceResourcePath;
        private String destinationPath;
        private String destinationDir;
        private String destinationFilename;

        public Builder(Charset charset, String placeholder) {
            this.placeholder = placeholder;
            this.charset = charset.name();
        }

        public Builder(Charset charset) {
            this(charset, VarSubstitutionFilter.DEFAULT_PLACEHOLDER_FORMAT);
        }

        public Builder(String placeholder) {
            this(DEFAULT_CHARSET, placeholder);
        }

        public Builder() {
            this(DEFAULT_CHARSET, VarSubstitutionFilter.DEFAULT_PLACEHOLDER_FORMAT);
        }

        @Override
        public FileCreatorFlowContext build() {

            if (((destinationPath != null) && ((destinationDir != null) || (destinationFilename != null))) ||
                ((destinationPath == null) && ((destinationDir == null) || (destinationFilename == null)))) {
                throw new IllegalArgumentException("Either set destinationPath or both destinationDir and destinationFilename, not both");
            }
            if (sourceData.isEmpty() && (sourceFilePath == null) && (sourceResourcePath == null)) {
                throw new IllegalArgumentException("Must set one of the sources using: .fromResource(), .fromFile() or .fromData()");
            }

            final FileCreatorFlowContext context = getInstance();
            Args.check((context.destinationPath != null) || ((context.destinationDir != null) && (context.destinationFilename != null)),
                       "Destination is misconfigured");
            Args.check(!(context.sourceData.isEmpty() && (context.sourceFilePath == null) && (context.sourceResourcePath == null)),
                       "Source is misconfigured");
            Args.notNull(replacePairs, "Replace pairs");
            Args.notNull(substitutionVariables, "Substitution variables");

            return context;
        }

        @Override
        protected FileCreatorFlowContext getInstance() {
            return new FileCreatorFlowContext(this);
        }

        public Builder destinationPath(final String value) {
            destinationPath = value;
            return builder();
        }

        public Builder destinationDir(final String value) {
            destinationDir = value;
            return builder();
        }

        public Builder destinationFilename(final String value) {
            destinationFilename = value;
            return builder();
        }

        /**
         * Adds a substitution pair - key is considered as a placeholder
         *
         * @param variable Placeholder value
         * @param value    Value to be placed in
         */
        public Builder substitution(final String variable, final String value) {
            substitutionVariables.put(variable, value);
            return builder();
        }

        /**
         * Adds a replace pair - string based matching
         *
         * @param variable String to be looked up and replaced
         * @param value    Replace value
         */
        public Builder replace(final String variable, final String value) {
            replacePairs.put(variable, value);
            return builder();
        }

        public Builder fromResource(final String value) {
            sourceResourcePath = value;
            return builder();
        }

        public Builder fromFile(final String value) {
            sourceFilePath = value;
            return builder();
        }

        public Builder fromData(final List<String> value) {
            sourceData.addAll(value);
            return builder();
        }

        public Builder fromData(final String value) {
            sourceData.add(value);
            return builder();
        }

        @Override
        protected Builder builder() {
            return this;
        }
    }
}
