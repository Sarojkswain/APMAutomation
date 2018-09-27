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

package com.ca.apm.tests.role;

import com.ca.apm.automation.action.flow.FlowConfig.FlowConfigBuilder;
import com.ca.apm.automation.action.flow.java.DeployJavaFlow;
import com.ca.apm.automation.action.flow.java.DeployJavaFlowContext;
import com.ca.tas.artifact.IThirdPartyArtifact;
import com.ca.tas.artifact.thirdParty.JavaBinary;
import com.ca.tas.artifact.thirdParty.JavaBinary.JavaRuntime;
import com.ca.tas.artifact.thirdParty.JavaVersion;
import com.ca.tas.builder.BuilderBase;
import com.ca.tas.client.IAutomationAgentClient;
import com.ca.tas.resolver.ITasResolver;
import com.ca.tas.role.AbstractRole;

import org.apache.http.util.Args;
import org.eclipse.aether.artifact.Artifact;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;

/**
 * JavaRole class.
 *
 * Java role class
 *
 * @author pojja01@ca.com
 */
public class JavaRole extends AbstractRole {

    public static final String INSTALL_DIR_PROPERTY = "installDir";
    public static final String JAVA_BINARY_EXEC_PROPERTY = "execPath";

    private static final int UNPACK_TIMEOUT = 600;
    private static final String RUNTIME_ENV_JAVA_RUNTIME_JDK = "JAVA_HOME";
    private static final String RUNTIME_ENV_JAVA_RUNTIME_JRE = "JRE_HOME";

    private final DeployJavaFlowContext javaFlowContext;
    private final JavaRuntime javaRuntime;
    private final String javaBinaryExecPath;


    /**
     * @param builder Builder object containing all necessary data
     */
    protected JavaRole(Builder builder) {
        super(builder.roleId, builder.getEnvProperties());

        javaFlowContext = builder.javaFlowContext;
        javaRuntime = builder.javaRuntime;
        javaBinaryExecPath = builder.execPath;
    }

    @Override
    public void deploy(IAutomationAgentClient aaClient) {
        aaClient.runJavaFlow(
            new FlowConfigBuilder(DeployJavaFlow.class, javaFlowContext, getHostWithPort())
                .timeout(UNPACK_TIMEOUT)
        );
    }

    @NotNull
    public Map<String, String> getRuntimeEnvironmentVariables() {

        Map<String, String> properties = new HashMap<>(1);

        if (javaRuntime == JavaRuntime.JDK) {
            properties.put(RUNTIME_ENV_JAVA_RUNTIME_JDK, javaFlowContext.getInstallDir());
        } else {
            properties.put(RUNTIME_ENV_JAVA_RUNTIME_JRE, javaFlowContext.getInstallDir());
        }

        return properties;
    }

    @NotNull
    public String getInstallDir() {
        return javaFlowContext.getInstallDir();
    }

    @NotNull
    public String getExecPath() {
        return javaBinaryExecPath;
    }

    /**
     * Linux Builder responsible for holding all necessary properties to instantiate {@link com.ca.tas.role.webapp.JavaRole}
     */
    public static class LinuxBuilder extends Builder {

        private static final String JAVA_BINARY = "java";

        public LinuxBuilder(String roleId, ITasResolver tasResolver) {
            super(roleId, tasResolver);
            javaFlowContextBuilder = new DeployJavaFlowContext.LinuxBuilder();
        }

        @Override
        protected Builder builder() {
            return this;
        }

        @Override
        protected String getPathSeparator() {
            return LINUX_SEPARATOR;
        }

        @NotNull
        @Override
        protected String getJavaBinary() {
            return JAVA_BINARY;
        }
    }

    /**
     * Builder responsible for holding all necessary properties to instantiate {@link com.ca.tas.role.webapp.JavaRole}
     */
    public static class Builder extends BuilderBase<Builder, JavaRole> {

        private static final String JAVA_BINARY = "java.exe";
        private final String roleId;
        private final ITasResolver tasResolver;

        protected JavaBinary javaBinary;
        protected String execPath;
        protected DeployJavaFlowContext.Builder javaFlowContextBuilder;

        @Nullable
        protected DeployJavaFlowContext javaFlowContext;
        @Nullable
        protected Artifact javaArtifact;
        @Nullable
        protected JavaRuntime javaRuntime;


        public Builder(String roleId, ITasResolver tasResolver) {
            this.roleId = roleId;
            this.tasResolver = tasResolver;
            javaFlowContextBuilder = new DeployJavaFlowContext.Builder();
            javaBinary = getDefaultJavaBinary();
        }

        JavaBinary getDefaultJavaBinary() {
            return JavaBinary.WINDOWS_64BIT_JDK_17;
        }

        @Override
        public JavaRole build() {

            URL artifactUrl = tasResolver.getArtifactUrl(javaArtifact == null ? javaBinary.getArtifact() : javaArtifact);

            initJavaRuntime();
            initJavaFlow(artifactUrl);
            initExecPath();

            JavaRole javaRole = getInstance();
            Args.notNull(javaRole.javaFlowContext, "Java flow context");
            Args.notNull(javaRole.javaRuntime, "Java runtime");
            Args.notNull(javaRole.javaBinaryExecPath, "Java binary exec path");

            return javaRole;
        }

        protected void initJavaRuntime() {
            if (javaArtifact == null) {
                javaRuntime = javaBinary.getJavaRuntime();
            }
        }

        protected void initJavaFlow(URL artifactUrl) {
            javaFlowContextBuilder.artifactUrl(artifactUrl);
            javaFlowContext = javaFlowContextBuilder.build();
            getEnvProperties().add(INSTALL_DIR_PROPERTY, javaFlowContext.getInstallDir());
        }

        private void initExecPath() {
            if (javaRuntime == JavaRuntime.JDK) {
                initJdkExecPath();
            } else {
                initJreExecPath();
            }
            getEnvProperties().add(JAVA_BINARY_EXEC_PROPERTY, execPath);
        }

        protected void initJdkExecPath() {
            assert javaFlowContext != null;

            execPath = javaFlowContext.getInstallDir() + getPathSeparator() + "bin" + getPathSeparator() + getJavaBinary();
        }

        @NotNull
        protected String getJavaBinary() {
            return JAVA_BINARY;
        }

        protected void initJreExecPath() {
            assert javaFlowContext != null;

            execPath = javaFlowContext.getInstallDir() + getPathSeparator() + "jre" + getPathSeparator() + "bin" + getPathSeparator()
                       + getJavaBinary();
        }

        @Override
        protected JavaRole getInstance() {
            return new JavaRole(this);
        }

        @Deprecated
        public Builder version(@NotNull JavaVersion javaVersion) {
            javaBinary = javaVersion.getJavaBinary();
            return builder();
        }

        public Builder version(@NotNull JavaBinary javaBinary) {
            this.javaBinary = javaBinary;
            return builder();
        }

        public Builder version(@NotNull IThirdPartyArtifact javaArtifact, @NotNull JavaRuntime javaRuntime) {
            this.javaArtifact = javaArtifact.getArtifact();
            this.javaRuntime = javaRuntime;
            return builder();
        }

        public Builder version(@NotNull Artifact javaArtifact, @NotNull JavaRuntime javaRuntime) {
            this.javaArtifact = javaArtifact;
            this.javaRuntime = javaRuntime;
            return builder();
        }

        public Builder dir(@NotNull String dir) {
            javaFlowContextBuilder.dir(dir);
            return builder();
        }

        @Override
        protected Builder builder() {
            return this;
        }
    }

    @Override
    public String toString() {
        return "JavaRole{" +
               "javaRuntime=" + javaRuntime +
               ", javaFlowContext=" + javaFlowContext +
               '}';
    }
}