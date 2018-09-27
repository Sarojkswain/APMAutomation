package com.ca.apm.systemtest.fld.flow;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ca.apm.automation.action.core.annotations.Flow;
import com.ca.apm.automation.action.core.annotations.FlowContext;
import com.ca.apm.automation.action.flow.FlowBase;
import com.ca.apm.automation.action.flow.commandline.Execution;

/**
 * @Author rsssa02
 */
@Flow
public class ConfigureOrclSrvBusAgentFlow extends FlowBase {
    public static final String PROFILE_LOCATION_REL = "/wily/core/config/IntroscopeAgent.profile";
    public static final String AGENT_LOCATION_REL = "/wily/Agent.jar";

    private static final Logger LOGGER = LoggerFactory.getLogger(ConfigureOrclSrvBusAgentFlow.class);
    @FlowContext
    private ConfigureOrclSrvBusAgentFlowContext context;

    public ConfigureOrclSrvBusAgentFlow() {
    }

    public void run() throws IOException {

        File configFile = FileUtils.getFile(this.context.getAgentJarPath() + PROFILE_LOCATION_REL);
        //File agentFile = FileUtils.getFile(this.context.getAgentPath() + AGENT_LOCATION_REL);

        /*if (this.context.getServerType() == ApplicationServerType.WEBSPHERE) {
            String path = "profiles/AppSrv01/config/cells/cell01/nodes/node01/servers/server1";
            File wasConfigFile = FileUtils.getFile(this.context.getServerInstallDir(), path, "server.xml");
            File wasTmpConfigFile = FileUtils.getFile(this.context.getServerInstallDir(), path, "server_template.xml");
            //
            if (!wasTmpConfigFile.exists()) {
                FileUtils.copyFile(wasConfigFile, wasTmpConfigFile);
            }
            else {
                FileUtils.copyFile(wasTmpConfigFile, wasConfigFile);
            }
            String javaAgentOptions = "-javaagent:" + agentFile.toString().replaceAll("\\\\", "/") +
                    " -Dcom.wily.introscope.agentProfile=" + configFile.toString().replaceAll("\\\\", "/");
            setProperty(wasConfigFile, "genericJvmArguments", "-Djavax.management.builder.initial= " +
                    "-Dcom.sun.management.jmxremote " +
                    "-Dcom.sun.management.jmxremote.authenticate=false " +
                    "-Dcom.sun.management.jmxremote.ssl=false " +
                    "-Dcom.sun.management.jmxremote.port=1099 " + javaAgentOptions);
        }*/
    }

    protected void setProperty(File configFile, String property, String value) throws IOException {

        Path path = Paths.get(configFile.toString());
        Charset charset = StandardCharsets.UTF_8;

        String content = new String(Files.readAllBytes(path), charset);
        content = content.replaceFirst(property + "=\".*\"", property + "=\"" + value + "\"");
        Files.write(path, content.getBytes(charset));
    }

    protected Execution.Builder getExecutionBuilder(Logger logger, String executable) {
        return new Execution.Builder(executable, logger);
    }
}
