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

package com.ca.apm.automation.action.flow.mainframe;

import org.apache.http.util.Args;

import com.ca.apm.automation.action.flow.IFlowContext;
import com.ca.apm.automation.utils.mainframe.sysvdb2.Version;
import com.ca.tas.builder.BuilderBase;

/**
 * Context for the {@link SysvDb2StartupFlow} flow.
 */
public class SysvDb2StartupFlowContext implements IFlowContext {
    private final Version version;
    private final String subsystem;
    private final String propertiesFilePath;
    private final String sysviewLoadlib;
    private final boolean onlyVerify;

    /**
     * Constructor.
     *
     * @param builder Builder.
     */
    private SysvDb2StartupFlowContext(Builder builder) {
        assert builder != null;

        version = builder.version;
        subsystem = builder.subsystem;
        propertiesFilePath = builder.propertiesFilePath;
        sysviewLoadlib = builder.sysviewLoadlib;
        onlyVerify = builder.onlyVerify;
    }

    Version getVersion() {
        return version;
    }

    String getSubsystem() {
        return subsystem;
    }

    String getPropertiesFilePath() {
        return propertiesFilePath;
    }

    String getSysviewLoadlib() {
        return sysviewLoadlib;
    }

    boolean isOnlyVerify() {
        return onlyVerify;
    }

    /**
     * Builder responsible for holding all necessary properties to
     * instantiate {@link SysvDb2StartupFlowContext}.
     */
    public static class Builder extends BuilderBase<Builder, SysvDb2StartupFlowContext> {
        private Version version;
        private String subsystem = null;
        private String propertiesFilePath;
        private String sysviewLoadlib = null;
        private boolean onlyVerify = false;

        /**
         * Constructor.
         *
         * @param version Desired SYSVDB2 version.
         * @param propertiesFilePath Path to where the a properties file of the instance should be
         *        saved.
         */
        public Builder(String version, String propertiesFilePath) {
            Version parsed = Version.fromString(version);
            if (parsed == null) {
                throw new IllegalArgumentException("Unrecognized version value: " + version);
            }

            this.version = parsed;
            this.propertiesFilePath = propertiesFilePath;
        }

        /**
         * Specifies a DB2 subsystem that needs to be monitored by the chosen SYSVDB2 instance.
         * Subsequent calls of this method will overwrite previous ones.
         *
         * @param subsystem DB2 subsystem to be monitored.
         * @return Builder instance the method was called on.
         */
        public Builder requireSubsystem(String subsystem) {
            Args.notBlank(subsystem, "subsystem");

            this.subsystem = subsystem;
            return builder();
        }

        /**
         * Sets an explicit SYSVIEW load library to be used.
         *
         * @param sysviewLoadlib Load library for the SYSVIEW instance to use.
         * @return Builder instance the method was called on.
         */
        public Builder sysviewLoadlib(String sysviewLoadlib) {
            Args.notBlank(sysviewLoadlib, "sysviewLoadlib");

            this.sysviewLoadlib = sysviewLoadlib;
            return builder();
        }

        /**
         * Only verify whether a SYSVDB2 instance matching the requirements is already running.
         *
         * @return Builder instance the method was called on.
         */
        public Builder onlyVerify() {
            onlyVerify = true;
            return builder();
        }

        @Override
        public SysvDb2StartupFlowContext build() {
            return getInstance();
        }

        @Override
        protected Builder builder() {
            return this;
        }

        @Override
        protected SysvDb2StartupFlowContext getInstance() {
            return new SysvDb2StartupFlowContext(this);
        }
    }
}
