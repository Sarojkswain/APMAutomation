package com.ca.apm.systemtest.fld.plugin.dotnet.powerpack.delegate;

import org.activiti.engine.delegate.DelegateExecution;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ca.apm.systemtest.fld.common.ErrorUtils;
import com.ca.apm.systemtest.fld.plugin.NodeManager;
import com.ca.apm.systemtest.fld.plugin.dotnet.Configuration;
import com.ca.apm.systemtest.fld.plugin.dotnet.DotNetPlugin;
import com.ca.apm.systemtest.fld.proxy.AgentProxyFactory;


/**
 * 
 * @author haiva01
 */
public class InstallAgentDelegate extends AbstractSharePointPpDelegate {
    private static final Logger log = LoggerFactory.getLogger(InstallAgentDelegate.class);
    private static final String TEST_TYPE_NO_AGENT = "noAgent";
    private static final String TEST_TYPE_AGENT = "Agent";
    private static final String TEST_TYPE_AGENT_PLUS_PP = "AgentPlusPP";

    private static final String DOTNET_TRUSS_BASE_URL = "http://truss.ca.com/builds/InternalBuilds";

    boolean withPp;

    public InstallAgentDelegate(boolean withPp, NodeManager nodeManager,
        AgentProxyFactory agentProxyFactory) {
        super(nodeManager, agentProxyFactory);
        this.withPp = withPp;
    }

    @Override
    protected void handleExecution(DelegateExecution execution) throws Throwable {
        try {
            DotNetPlugin dotnet = getDotNetPlugin(execution);
            dotnet.makeInstallPrefix();
            dotnet.uninstallAgent();
            dotnet.deleteAgentDirectory();

            final String artifactSpecification = getStringExecutionVariable(execution,
                "artifactSpecification");
            if (StringUtils.isBlank(artifactSpecification)) {
                throw ErrorUtils
                    .logErrorAndReturnException(log, "artifactSpecification must be specified");
            }

            final String[] specParts = StringUtils.split(artifactSpecification, ':');
            boolean useTruss = false;
            if (specParts[0].equalsIgnoreCase("truss")) {
                useTruss = true;
            }

            String file;
            final String dotnetBitness = getStringExecutionVariable(execution, "dotnetBitness", "64");
            if (useTruss) {
                String dotnetTrussBaseUrl = getStringExecutionVariable(execution, "dotnetTrussBaseUrl", DOTNET_TRUSS_BASE_URL);
                //  [0]    [1]       [2]      [3]
                // truss:10.0.0-NET:990301:10.0.0.18
                String dotnetCodeName = specParts[1];
                String dotnetBuildNumber = specParts[2];
                String dotnetBuildId = specParts[3];
                file = dotnet.fetchInstallerArtifactFromTruss(dotnetTrussBaseUrl,
                    dotnetCodeName, dotnetBuildNumber, dotnetBuildId,
                    dotnetBitness);
                log.info(".NET agent installer file: {}", file);
            } else {
                String version = specParts[1];
                file = dotnet.fetchInstallerArtifactFromArtifactory(version, dotnetBitness, null);
                log.info(".NET agent installer file: {}", file);
                file = dotnet.unzipInstallerArtifact();
                log.info(".NET agent installer file: {}", file);
            }

            Configuration config = new Configuration();

            String testType = getEnumExecutionVariable(execution, "testType", "noAgent");

            switch (testType) {
                case TEST_TYPE_AGENT:
                    config.enableSoa = true;
                    break;
                case TEST_TYPE_AGENT_PLUS_PP:
                    config.enableSoa = true;
                    config.enableSpp = true;
                    config.installSpMonitor = true;
                    break;
                case TEST_TYPE_NO_AGENT:
                    return;
                default:
                    throw ErrorUtils
                        .logErrorAndReturnException(log, "Unknown test type: {0}", testType);
            }

            if (withPp) {
                config.enableSoa = true;
                config.enableSpp = true;
                // ???
                config.installSpMonitor = true;
            }

            String dotnetEmHost = getNodeExecutionVariable(execution, "momNode", "127.0.0.1");
            int dotnetEmPort = getIntegerExecutionVariable(execution, "momPort", 5001);
            dotnet.installAgent(dotnetEmHost, dotnetEmPort, config);
        } catch (Exception ex) {
            throw ErrorUtils
                .logExceptionAndWrapFmt(log, ex, "Failure during agent install. Exception: {0}");
        }
    }
}
