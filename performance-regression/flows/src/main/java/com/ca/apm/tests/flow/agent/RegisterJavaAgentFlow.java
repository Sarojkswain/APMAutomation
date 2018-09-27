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
package com.ca.apm.tests.flow.agent;

import com.ca.apm.automation.action.core.annotations.Flow;
import com.ca.apm.automation.action.core.annotations.FlowContext;
import com.ca.apm.automation.action.flow.FlowBase;
import com.ca.apm.automation.action.flow.agent.ApplicationServerType;
import com.ca.apm.automation.action.flow.commandline.Execution;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * RegisterJavaAgentFlow
 *
 * @author Erik Melecky (meler02@ca.com)
 */
@Flow
public class RegisterJavaAgentFlow extends FlowBase {

    public static final String PROFILE_LOCATION_REL = "/wily/core/config/IntroscopeAgent.profile";
    public static final String AGENT_LOCATION_REL = "/wily/Agent.jar";

    private static final Logger LOGGER = LoggerFactory.getLogger(RegisterJavaAgentFlow.class);
    @FlowContext
    private RegisterJavaAgentFlowContext context;

    public RegisterJavaAgentFlow() {
    }

    public void run() throws IOException {
        if (this.context.getServerType() == ApplicationServerType.WEBSPHERE) {
            File wasConfigFile = FileUtils.getFile(this.context.getServerXmlFilePath(), "server.xml");
            File wasTmpConfigFile = FileUtils.getFile(this.context.getServerXmlFilePath(), "server_template.xml");
            //
            if (!wasTmpConfigFile.exists()) {
                FileUtils.copyFile(wasConfigFile, wasTmpConfigFile);
            } else {
                FileUtils.copyFile(wasTmpConfigFile, wasConfigFile);
            }
            setProperty(wasConfigFile, "genericJvmArguments", "-Djavax.management.builder.initial= " +
                    "-Dcom.sun.management.jmxremote " +
                    "-Dcom.sun.management.jmxremote.authenticate=false " +
                    "-Dcom.sun.management.jmxremote.ssl=false " +
                    "-Dcom.sun.management.jmxremote.port=1099 " + getJavaAgentOptions());
        }
        LOGGER.info("Flow has finished.");
    }

    protected String getJavaAgentOptions() {
        File configFile = FileUtils.getFile(this.context.getAgentPath() + PROFILE_LOCATION_REL);
        File agentFile = FileUtils.getFile(this.context.getAgentPath() + AGENT_LOCATION_REL);
        return "-javaagent:" + agentFile.toString().replaceAll("\\\\", "/") +
                " -Dcom.wily.introscope.agentProfile=" + configFile.toString().replaceAll("\\\\", "/");
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
