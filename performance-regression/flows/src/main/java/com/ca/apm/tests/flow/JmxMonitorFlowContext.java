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
package com.ca.apm.tests.flow;

import com.ca.apm.automation.action.flow.IFlowContext;
import com.ca.tas.builder.ExtendedBuilderBase;
import com.ca.tas.property.EnvPropSerializable;
import org.apache.http.util.Args;

import java.util.Map;

/**
 * Flow Context for installing NET StockTrader WebApp into IIS
 *
 * @author Erik Melecky (meler02@ca.com)
 */
public class JmxMonitorFlowContext implements IFlowContext, EnvPropSerializable<JmxMonitorFlowContext>, INetShareUser {

    private final String host;
    private final Integer port;
    private final String javaHome;
    private final String jmxMonitorJarPath;
    private final transient JmxMonitorFlowContextSerializer envPropSerializer;
    private Long runTime;
    private String outputFileName;
    private String copyResultsDestinationDir;
    private String copyResultsDestinationFileName;
    private String copyResultsDestinationUser;
    private String copyResultsDestinationPassword;
    private String jmxCollectionString;

    protected JmxMonitorFlowContext(JmxMonitorFlowContext.Builder builder) {
        this.host = builder.host;
        this.port = builder.port;
        this.javaHome = builder.javaHome;
        this.jmxMonitorJarPath = builder.jmxMonitorJarPath;
        this.runTime = builder.runTime;
        this.outputFileName = builder.outputFileName;
        this.copyResultsDestinationDir = builder.copyResultsDestinationDir;
        this.copyResultsDestinationFileName = builder.copyResultsDestinationFileName;
        this.copyResultsDestinationUser = builder.copyResultsDestinationUser;
        this.copyResultsDestinationPassword = builder.copyResultsDestinationPassword;
        this.jmxCollectionString = builder.jmxCollectionString;

        this.envPropSerializer = new JmxMonitorFlowContextSerializer(this);
    }

    public String getJmxCollectionString() {
        return jmxCollectionString;
    }

    public void setJmxCollectionString(String jmxCollectionString) {
        this.jmxCollectionString = jmxCollectionString;
    }

    public String getHost() {
        return host;
    }

    public Integer getPort() {
        return port;
    }

    public String getJavaHome() {
        return javaHome;
    }

    public String getJmxMonitorJarPath() {
        return jmxMonitorJarPath;
    }

    public Long getRunTime() {
        return runTime;
    }

    public void setRunTime(Long runTime) {
        this.runTime = runTime;
    }

    public String getOutputFileName() {
        return outputFileName;
    }

    public void setOutputFileName(String outputFileName) {
        this.outputFileName = outputFileName;
    }

    public String getCopyResultsDestinationDir() {
        return copyResultsDestinationDir;
    }

    public void setCopyResultsDestinationDir(String copyResultsDestinationDir) {
        this.copyResultsDestinationDir = copyResultsDestinationDir;
    }

    public String getCopyResultsDestinationFileName() {
        return copyResultsDestinationFileName;
    }

    public void setCopyResultsDestinationFileName(String copyResultsDestinationFileName) {
        this.copyResultsDestinationFileName = copyResultsDestinationFileName;
    }

    public String getCopyResultsDestinationUser() {
        return copyResultsDestinationUser;
    }

    public void setCopyResultsDestinationUser(String copyResultsDestinationUser) {
        this.copyResultsDestinationUser = copyResultsDestinationUser;
    }

    public String getCopyResultsDestinationPassword() {
        return copyResultsDestinationPassword;
    }

    public void setCopyResultsDestinationPassword(String copyResultsDestinationPassword) {
        this.copyResultsDestinationPassword = copyResultsDestinationPassword;
    }

    @Override
    public JmxMonitorFlowContext deserialize(String key, Map<String, String> serializedData) {
        return this.envPropSerializer.deserialize(key, serializedData);
    }

    @Override
    public Map<String, String> serialize(String key) {
        return this.envPropSerializer.serialize(key);
    }

    public static class Builder extends ExtendedBuilderBase<JmxMonitorFlowContext.Builder, JmxMonitorFlowContext> {

        protected String host;
        protected Integer port;
        protected String javaHome;
        protected String jmxMonitorJarPath;
        protected Long runTime;
        protected String outputFileName;
        protected String copyResultsDestinationDir;
        protected String copyResultsDestinationFileName;
        protected String copyResultsDestinationUser;
        protected String copyResultsDestinationPassword;
        protected String jmxCollectionString;

        public Builder() {
            this.host = "localhost";
            this.port = 1099;
            this.copyResultsDestinationUser = DEFAULT_COPY_RESULTS_USER;
            this.copyResultsDestinationPassword = DEFAULT_COPY_RESULTS_PASSWORD;
        }

        public JmxMonitorFlowContext build() {
            JmxMonitorFlowContext context = this.getInstance();
            Args.notNull(context.host, "host");
            Args.notNull(context.port, "port");
            Args.notNull(context.jmxMonitorJarPath, "jmxMonitorJarPath");
            Args.notNull(context.runTime, "runTime");
            Args.notNull(context.outputFileName, "outputFileName");

            return context;
        }

        protected JmxMonitorFlowContext getInstance() {
            return new JmxMonitorFlowContext(this);
        }

        public Builder host(String host) {
            this.host = host;
            return this.builder();
        }

        public Builder port(Integer port) {
            this.port = port;
            return this.builder();
        }

        public Builder jmxCollectionString(String jmxCollectionString) {
            this.jmxCollectionString = jmxCollectionString;
            return this.builder();
        }

        public Builder javaHome(String javaHome) {
            this.javaHome = javaHome;
            return this.builder();
        }

        public Builder jmxMonitorJarPath(String jmxMonitorJarPath) {
            this.jmxMonitorJarPath = jmxMonitorJarPath;
            return this.builder();
        }

        public Builder runTime(Long runTime) {
            this.runTime = runTime;
            return this.builder();
        }

        public Builder outputFileName(String outputFileName) {
            this.outputFileName = outputFileName;
            return this.builder();
        }

        public Builder copyResultsDestinationDir(String copyResultsDestinationDir) {
            this.copyResultsDestinationDir = copyResultsDestinationDir;
            return this.builder();
        }

        public Builder copyResultsDestinationFileName(String copyResultsDestinationFileName) {
            this.copyResultsDestinationFileName = copyResultsDestinationFileName;
            return this.builder();
        }

        public Builder copyResultsDestinationUser(String copyResultsDestinationUser) {
            this.copyResultsDestinationUser = copyResultsDestinationUser;
            return this.builder();
        }

        public Builder copyResultsDestinationPassword(String copyResultsDestinationPassword) {
            this.copyResultsDestinationPassword = copyResultsDestinationPassword;
            return this.builder();
        }

        protected JmxMonitorFlowContext.Builder builder() {
            return this;
        }
    }

}