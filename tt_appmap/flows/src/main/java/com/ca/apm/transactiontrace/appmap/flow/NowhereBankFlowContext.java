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

package com.ca.apm.transactiontrace.appmap.flow;

import com.ca.apm.automation.action.flow.IFlowContext;
import com.ca.tas.builder.BuilderBase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.MalformedURLException;
import java.net.URL;

/**
 * Flow context for nowhere bank application
 *
 * @author bhusu01
 */
public class NowhereBankFlowContext implements IFlowContext {

    private static final Logger LOGGER = LoggerFactory.getLogger(NowhereBankFlowContext.class);
    final String installDirectory;
    final URL artifactURL;
    private final String extractionDirectory;

    @SuppressWarnings("unused")
    protected NowhereBankFlowContext(Builder builder) {
        installDirectory = builder.installDirectory;
        artifactURL = builder.artifactURL;
        extractionDirectory = builder.extractionDirectory;
    }

    public String getInstallDirectory() {
        return installDirectory;
    }

    public URL getArtifactURL() {
        return artifactURL;
    }

    public String getExtractionDirectory() {
        return extractionDirectory;
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

        @Override
        protected String getDeployBase() {
            return getLinuxDeployBase();
        }
    }


    public static class Builder extends BuilderBase<Builder, NowhereBankFlowContext> {

        private String installDirectory = getDeployBase() + getPathSeparator() + "agents";
        private String extractionDirectory = installDirectory + getPathSeparator() +  "noWhereBank" + getPathSeparator() + "App";
        private URL artifactURL;

        @Override
        public NowhereBankFlowContext build() {
            return getInstance();
        }

        @Override
        protected NowhereBankFlowContext getInstance() {
            return new NowhereBankFlowContext(this);
        }

        public Builder installDirectory(String installDir) {
            this.installDirectory = installDir;
            return this;
        }

        @Override
        protected Builder builder() {
            return this;
        }

        public Builder artifactURL(URL url) {
            artifactURL = url;
            return this;
        }
    }
}
