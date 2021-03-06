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
package com.ca.apm.tests.flow.jMeter;

import com.ca.apm.automation.action.core.annotations.Flow;
import com.ca.apm.automation.action.core.annotations.FlowContext;
import com.ca.apm.automation.action.flow.FlowBase;
import com.ca.apm.automation.action.flow.commandline.Execution;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;

/**
 * JMeterDeployFlow
 *
 * @author Erik Melecky (meler02@ca.com)
 */
@Flow
public class JMeterDeployFlow extends FlowBase {

    private static final Logger LOGGER = LoggerFactory.getLogger(JMeterDeployFlow.class);
    @FlowContext
    private JMeterDeployFlowContext context;

    public JMeterDeployFlow() {
    }

    public void run() throws IOException {
        File installSourcesDir = FileUtils.getFile(this.context.getDeploySourcesLocation());
        if (!installSourcesDir.exists()) {
            this.archiveFactory.createArchive(this.context.getDeployPackageUrl()).unpack(new File(this.context.getDeploySourcesLocation()));
        } else {
            LOGGER.info("JMeter installation files already exist in '" + this.context.getDeploySourcesLocation() + "'. Skipping download.");
        }
        LOGGER.info("Flow has finished.");
    }

    protected Execution.Builder getExecutionBuilder(Logger logger, String executable) {
        return new Execution.Builder(executable, logger);
    }
}
