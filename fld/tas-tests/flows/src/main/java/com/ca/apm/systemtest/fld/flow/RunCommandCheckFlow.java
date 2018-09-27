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

package com.ca.apm.systemtest.fld.flow;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ca.apm.automation.action.core.annotations.Flow;
import com.ca.apm.automation.action.core.annotations.FlowContext;
import com.ca.apm.automation.action.flow.commandline.RunCommandFlow;
import com.ca.apm.automation.action.flow.commandline.RunCommandFlowContext;

/**
 * Runs specified command in OS shell with check
 * Skip run of command if workdir doesn't exist
 *
 * @author filja01
 */
@Flow
public class RunCommandCheckFlow extends RunCommandFlow {

    private static final Logger LOGGER = LoggerFactory.getLogger(RunCommandCheckFlow.class);

    @FlowContext
    private RunCommandFlowContext ctx;

    @Override
    public void run() throws Exception {
        String textToMatch = ctx.getTextToMatch();

        Logger logger = LoggerFactory.getLogger(getClass());
        String[] args = ctx.getArgs().toArray(new String[ctx.getArgs().size()]);
        
        try {
            int exitValue = !textToMatch.isEmpty() ?
                            getExecutionBuilder(logger)
                                .args(args)
                                .textToMatch(textToMatch)
                                .environment(ctx.getEnvironment())
                                .build()
                                .go() :
                            getExecutionBuilder(logger)
                                .args(args)
                                .environment(ctx.getEnvironment())
                                .build()
                                .go();
            if (exitValue != 0) {
                throw new IllegalStateException("RunCommand has failed with value: " + exitValue);
            }
        } 
        catch(Exception e) {
            ;
        }
        
        LOGGER.info("Flow has finished.");
    }
}
