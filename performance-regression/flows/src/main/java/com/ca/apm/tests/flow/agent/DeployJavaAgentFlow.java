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
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;

/**
 * DeployJavaAgentFlow
 *
 * @author Erik Melecky (meler02@ca.com)
 */
@Flow
public class DeployJavaAgentFlow extends DeployAgentFlowAbs {

    private static final Logger LOGGER = LoggerFactory.getLogger(DeployJavaAgentFlow.class);
    @FlowContext
    private DeployJavaAgentFlowContext context;

    public DeployJavaAgentFlow() {
    }

    public void configureAgent() throws IOException {
        File configFile = FileUtils.getFile(this.context.getDeploySourcesLocation() + "/wily/core/config/IntroscopeAgent.profile");
        if (this.context.isSiEnabled() != null) {
            if (this.context.isSiEnabled()) {
                enableSi(configFile);
            } else {
                disableSi(configFile);
            }
        }
        if (this.context.isAccEnabled() != null) {
            if (this.context.isAccEnabled()) {
                enableAcc(configFile);
            } else {
                disableAcc(configFile);
            }
        }
        if (this.context.isBrtmEnabled() != null) {
            if (this.context.isBrtmEnabled()) {
                enableBrtm(configFile);
            } else {
                disableBrtm(configFile);
            }
        }
        if (this.context.getEmLocation() != null) {
            setEmLocation(configFile, this.context.getEmLocation());
        }
        if (this.context.getAgentName() != null) {
            setAgentName(configFile, this.context.getAgentName());
        }

//        try {
//            this.runInstallationProcess(installResponseFile);
//        } catch (InterruptedException var3) {
//            throw new IllegalStateException(var3);
//        }
    }

    protected void enableSi(File configFile) throws IOException {
        uncommentProperty(configFile, "introscope.agent.deep.instrumentation.enabled");
        setProperty(configFile, "introscope.agent.deep.instrumentation.enabled", "true");
        //
        uncommentProperty(configFile, "introscope.agent.deep.instrumentation.level");
        setProperty(configFile, "introscope.agent.deep.instrumentation.level", "medium");
    }

    protected void disableSi(File configFile) throws IOException {
        uncommentProperty(configFile, "introscope.agent.deep.instrumentation.enabled");
        setProperty(configFile, "introscope.agent.deep.instrumentation.enabled", "false");
        //
        uncommentProperty(configFile, "introscope.agent.deep.instrumentation.level");
        setProperty(configFile, "introscope.agent.deep.instrumentation.level", "low");
    }

    protected void enableAcc(File configFile) throws IOException {
        uncommentProperty(configFile, "introscope.agent.acc.enable");
        setProperty(configFile, "introscope.agent.acc.enable", "true");
    }

    protected void disableAcc(File configFile) throws IOException {
        uncommentProperty(configFile, "introscope.agent.acc.enable");
        setProperty(configFile, "introscope.agent.acc.enable", "false");
    }

    protected void enableBrtm(File configFile) throws IOException {
        uncommentProperty(configFile, "introscope.agent.browseragent.enabled");
        setProperty(configFile, "introscope.agent.browseragent.enabled", "true");
    }

    protected void disableBrtm(File configFile) throws IOException {
        uncommentProperty(configFile, "introscope.agent.browseragent.enabled");
        setProperty(configFile, "introscope.agent.browseragent.enabled", "false");
    }
}
