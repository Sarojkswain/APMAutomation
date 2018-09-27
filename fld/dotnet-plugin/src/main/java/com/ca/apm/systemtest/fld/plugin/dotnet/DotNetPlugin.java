package com.ca.apm.systemtest.fld.plugin.dotnet;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import com.ca.apm.systemtest.fld.common.PluginAnnotationComponent;
import com.ca.apm.systemtest.fld.plugin.Plugin;

/**
 * FLD .NET plugin interface.
 *
 * @author haiva01
 */
@PluginAnnotationComponent(pluginType = DotNetPlugin.PLUGIN)
public interface DotNetPlugin extends Plugin {
    public static final String PLUGIN = "dotnetPlugin";
    
    void stopIis();

    void startIis();

    boolean uninstallAgent();

    String makeInstallPrefix();

    String getInstallPrefix();

    void deleteAgentDirectory();

    String fetchInstallerArtifactFromTruss(String repoBase, String codeName,
                                           String buildNumber, String buildId,
                                           String fileName);

    String fetchInstallerArtifactFromArtifactory(String version, String classifier,
                                                 String artifactoryBaseUrl);

    String unzipInstallerArtifact();

    void installAgent(String emHost, int emPort, Configuration config);

    enum AgentCheckResult {
        NOT_INSTALLED,
        NOT_RUNNING,
        RUNNING
    }

    AgentCheckResult checkAgent();

    Map<String, String> getIisWorkers() throws ExecutionException, InterruptedException,
        IOException;
}
