/*
 * Copyright (c) 2015 CA. All rights reserved.
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
package com.ca.apm.siteminder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ca.apm.automation.action.core.annotations.Flow;
import com.ca.apm.automation.action.core.annotations.FlowContext;
import com.ca.apm.automation.action.flow.FlowBase;

/**
 * @author Sundeep (bhusu01)
 */
@Flow
public class PolicyServerRestartFlow extends FlowBase {

    private static final Logger LOGGER = LoggerFactory.getLogger(PolicyServerRestartFlow.class);

    @FlowContext
    private PolicyServerRestartFlowContext context;

    @Override
    public void run() throws Exception {
        String defaultRootDir = context.getDefaultRootDir();
        String siteMinderServiceName = context.getSiteMinderServiceName();

        LOGGER.info("Attempting to stop SiteMinder Policy Server");

        final int stopResponseCode =
            Utils.exec(defaultRootDir, "cmd", new String[] {"/C", "net", "stop", siteMinderServiceName}, LOGGER);

        LOGGER
            .info("net stop {} returned response code {}", siteMinderServiceName, stopResponseCode);

        LOGGER.info("Attempting to start SiteMinder Policy Server");

        final int startResponseCode =
            Utils.exec(defaultRootDir, "cmd", new String[] {"/C", "net", "start", "SiteMinder Policy Server"}, LOGGER);

        LOGGER.info("net start {} returned response code {}", siteMinderServiceName,
            startResponseCode);
        
        // Give PS some time to start
        try {
            Thread.sleep(30000);
        } catch (InterruptedException e) {
            // do nothing
        }
    }
}
