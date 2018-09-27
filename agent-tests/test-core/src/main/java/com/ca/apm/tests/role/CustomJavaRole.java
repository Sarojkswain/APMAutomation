package com.ca.apm.tests.role;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.apache.http.util.Args;
import org.eclipse.aether.artifact.Artifact;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.ca.apm.automation.action.flow.FlowConfig.FlowConfigBuilder;
import com.ca.apm.automation.action.flow.java.DeployJavaFlow;
import com.ca.apm.automation.action.flow.java.DeployJavaFlowContext;
import com.ca.apm.automation.action.flow.utility.FileModifierFlow;
import com.ca.apm.automation.action.flow.utility.FileModifierFlowContext;
import com.ca.tas.annotation.TasDocRole;
import com.ca.tas.annotation.TasEnvironmentPropertyKey;
import com.ca.tas.artifact.IThirdPartyArtifact;
import com.ca.tas.artifact.thirdParty.JavaBinary.JavaRuntime;
import com.ca.tas.builder.BuilderBase;
import com.ca.tas.client.IAutomationAgentClient;
import com.ca.tas.resolver.ITasResolver;
import com.ca.tas.role.AbstractRole;

@TasDocRole(platform = {})
public class CustomJavaRole extends AbstractRole {

    @TasEnvironmentPropertyKey
    public static final String INSTALL_DIR_PROPERTY = "installDir";
    @TasEnvironmentPropertyKey
    public static final String JAVA_BINARY_EXEC_PROPERTY = "execPath";

    private static final long UNPACK_TIMEOUT = 600;
    private static final String RUNTIME_ENV_JAVA_RUNTIME_JDK = "JAVA_HOME";
    private static final String RUNTIME_ENV_JAVA_RUNTIME_JRE = "JRE_HOME";

    private final DeployJavaFlowContext javaFlowContext;
    private final JavaRuntime javaRuntime;
    private final String javaBinaryExecPath;
    private final boolean shouldUpdateJavaSecurity;

    /**
     * <p>Constructor for JavaRole.</p>
     *
     * @param builder Builder object containing all necessary data
     */
    protected CustomJavaRole(Builder builder) {
        super(builder.roleId, builder.getEnvProperties());

        javaFlowContext = builder.javaFlowContext;
        javaRuntime = builder.javaRuntime;
        javaBinaryExecPath = builder.execPath;
        shouldUpdateJavaSecurity = builder.shouldUpdateJavaSecurity;
    }

    /** {@inheritDoc} */
    @Override
    public void deploy(IAutomationAgentClient aaClient) {
        aaClient.runJavaFlow(new FlowConfigBuilder(DeployJavaFlow.class, javaFlowContext, getHostWithPort())
            .timeout(UNPACK_TIMEOUT)
        );
        
        if(shouldUpdateJavaSecurity) {
            updateJavaSecurity(aaClient); 
        }
    }
    
    private void updateJavaSecurity(IAutomationAgentClient aaClient) {
                
        String file = javaFlowContext.getInstallDir() + "/jre/lib/security/java.security";        
      
        Map<String,String> replacePairs = new HashMap<String,String>();
        replacePairs.put("jdk.certpath.disabledAlgorithms","#jdk.certpath.disabledAlgorithms");
        replacePairs.put("jdk.tls.disabledAlgorithms", "#jdk.tls.disabledAlgorithms");
        replacePairs.put("jdk.jar.disabledAlgorithms", "#jdk.jar.disabledAlgorithms");
        
        FileModifierFlowContext context = new FileModifierFlowContext.Builder()
            .replace(file, replacePairs)
            .build();
        
        runFlow(aaClient, FileModifierFlow.class, context);
    }

    /**
     * <p>getRuntimeEnvironmentVariables.</p>
     *
     * @return a {@link java.util.Map} object.
     */
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

    /**
     * <p>getInstallDir.</p>
     *
     * @return a {@link java.lang.String} object.
     */
    @NotNull
    public String getInstallDir() {
        return javaFlowContext.getInstallDir();
    }

    /**
     * <p>getExecPath.</p>
     *
     * @return a {@link java.lang.String} object.
     */
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
    public static class Builder extends BuilderBase<Builder, CustomJavaRole> {

        private static final String JAVA_BINARY = "java.exe";
        private final String roleId;
        private final ITasResolver tasResolver;

        protected CustomJavaBinary javaBinary;
        protected String execPath;
        protected DeployJavaFlowContext.Builder javaFlowContextBuilder;

        @Nullable
        protected DeployJavaFlowContext javaFlowContext;
        @Nullable
        protected Artifact javaArtifact;
        @Nullable
        protected JavaRuntime javaRuntime;
        protected boolean shouldUpdateJavaSecurity = false;

        public Builder(String roleId, ITasResolver tasResolver) {
            this.roleId = roleId;
            this.tasResolver = tasResolver;
            javaFlowContextBuilder = new DeployJavaFlowContext.Builder();
            javaBinary = getDefaultJavaBinary();
        }

        CustomJavaBinary getDefaultJavaBinary() {
            return CustomJavaBinary.WINDOWS_64BIT_JDK_18_0_112;
        }

        @Override
        public CustomJavaRole build() {

            URL artifactUrl = tasResolver.getArtifactUrl(javaArtifact == null ? javaBinary.getArtifact() : javaArtifact);

            initJavaRuntime();
            initJavaFlow(artifactUrl);
            initExecPath();

            CustomJavaRole javaRole = getInstance();
            Args.notNull(javaRole.javaFlowContext, "Java flow context");
            Args.notNull(javaRole.javaRuntime, "Java runtime");
            Args.notNull(javaRole.javaBinaryExecPath, "Java binary exec path");

            return javaRole;
        }

        protected void initJavaRuntime() {
            if (javaArtifact == null) {
                javaRuntime = JavaRuntime.JDK;
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
        protected CustomJavaRole getInstance() {
            return new CustomJavaRole(this);
        }

        public Builder version(@NotNull CustomJavaBinary javaBinary) {
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

        public Builder shouldUpdateJavaSecurity(boolean shouldUpdateJavaSecurity) {
            this.shouldUpdateJavaSecurity = shouldUpdateJavaSecurity;           
            return builder();
        }
        
        @Override
        protected Builder builder() {
            return this;
        }
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        return "JavaRole{" +
               "javaRuntime=" + javaRuntime +
               ", javaFlowContext=" + javaFlowContext +
               '}';
    }
}
