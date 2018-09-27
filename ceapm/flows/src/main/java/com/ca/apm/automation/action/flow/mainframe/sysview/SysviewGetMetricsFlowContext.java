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
package com.ca.apm.automation.action.flow.mainframe.sysview;

import java.util.Arrays;
import java.util.List;

import org.apache.http.util.Args;

import com.ca.apm.automation.action.flow.IFlowContext;
import com.ca.tas.builder.BuilderBase;

/**
 * Context for the {@link SysviewCommandFlow} flow.
 */
public class SysviewGetMetricsFlowContext implements IFlowContext {
    private final String loadlib;
    private final String command;
    private final String keyName;
    private final List<String> keyValues;
    private final List<String> columns;

    /**
     * Constructor.
     *
     * @param builder Builder.
     */
    private SysviewGetMetricsFlowContext(Builder builder) {
        assert !builder.command.trim().isEmpty();

        loadlib = builder.loadlib;
        command = builder.command;
        keyName = builder.keyName;
        keyValues = builder.keyValues;
        columns = builder.columns;
    }

    String getLoadlib() {
        return loadlib;
    }

    String getCommand() {
        return command;
    }

    String getkeyName() {
        return keyName;
    }

    List<String> getKeyValues() {
        return keyValues;
    }

    List<String> getColumns() {
        return columns;
    }

    /**
     * Builder for the {@link SysviewGetMetricsFlowContext} flow context.
     */
    public static class Builder extends BuilderBase<Builder, SysviewGetMetricsFlowContext> {
        private String loadlib;
        private String command;
        private String keyName;
        private List<String> keyValues;
        private List<String> columns;

        /**
         * Constructor.
         *
         * @param command Command to execute.
         */
        public Builder(String command) {
            Args.notBlank(command, "command");
            this.command = command;
        }

        /**
         * Sets an explicit SYSVIEW load library to be used.
         *
         * @param loadlib Load library for the SYSVIEW instance to use.
         * @return Builder instance the method was called on.
         */
        public Builder loadlib(String loadlib) {
            Args.notBlank(loadlib, "loadlib");

            this.loadlib = loadlib;
            return builder();
        }

        /**
         * Sets key name for output filtering.
         *
         * @param keyName key column name
         * @return Builder instance the method was called on.
         */
        public Builder keyName(String keyName) {
            Args.notBlank(keyName, "key name");

            this.keyName = keyName;
            return builder();
        }

        /**
         * Defines row key values for output filtering.
         *
         * @param keyValues Set of values for key column
         * @return Referenced instance.
         */
        public Builder keyValues(String... keyValues) {
            Args.check(keyValues.length > 0, "At least one key has to be specified");

            this.keyValues = Arrays.asList(keyValues);
            return builder();
        }

        /**
         * Defines Sysview columns to be read.
         *
         * @param columns Set of return code values.
         * @return Referenced instance.
         */
        public Builder columns(String... columns) {
            Args.check(columns.length > 0, "At least one column name has to be specified");

            this.columns = Arrays.asList(columns);
            return builder();
        }

        @Override
        public SysviewGetMetricsFlowContext build() {
            Args.notNull(loadlib, "loadlib");
            Args.notNull(command, "command");
            Args.notNull(keyName, "key name");
            Args.notNull(keyValues, "key values");
            Args.notNull(columns, "columns");

            return getInstance();
        }

        @Override
        protected Builder builder() {
            return this;
        }

        @Override
        protected SysviewGetMetricsFlowContext getInstance() {
            return new SysviewGetMetricsFlowContext(this);
        }
    }
}
