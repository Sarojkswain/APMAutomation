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

package com.ca.apm.automation.action.flow.mainframe.sysview;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ca.apm.automation.action.core.IAutomationFlow;
import com.ca.apm.automation.action.core.annotations.Flow;
import com.ca.apm.automation.action.core.annotations.FlowContext;
import com.ca.apm.automation.utils.mainframe.sysview.Sysview;
import com.ca.apm.automation.utils.mainframe.sysview.Sysview.ExecResult;

/**
 * Flow queries SYSVIEW metrics.
 *
 * <p>
 * Through the context ({@link SysviewGetMetricsFlowContext}) you can specify
 * SYSVIEW, query command, and output filtering criteria (task names, Sysview columns)
 * </p>
 */
@Flow
public class SysviewGetMetricsFlow implements IAutomationFlow {
    private static final Logger logger = LoggerFactory.getLogger(SysviewGetMetricsFlow.class);

    @FlowContext
    private SysviewGetMetricsFlowContext context;

    /**
     * Runs SYSVIEW command, returning selected row/column values to TAS log.
     */
    @Override
    public void run() throws Exception {
        assert context.getCommand() != null && !context.getCommand().isEmpty();
        assert context.getKeyValues().size() > 0;
        assert context.getColumns().size() > 0;

        try (Sysview sysview = new Sysview(context.getLoadlib())) {
            logger.info("Running the '" + context.getCommand() + "' SYSVIEW command");
            ExecResult result = sysview.execute(context.getCommand());

            StringBuffer outputLine = new StringBuffer();
            for (String key : context.getKeyValues()) {
                Map<String, String> row =
                    result.getTabularData().getFirstRowMatching(context.getkeyName(), key);
                if (row == null) {
                    logger.debug("{}", result);
                    throw new IllegalStateException("Unable to query SYSVIEW metrics for : " + key);
                }

                for (String column : context.getColumns()) {
                    outputLine.append(row.get(column) + ", ");
                }
            }

            if (outputLine.length() > 2) {
                outputLine.delete(outputLine.length() - 2, outputLine.length());
            }
            String currDateTime = new SimpleDateFormat("[yyyy-MM-dd HH:mm:ss]").format(new Date());
            logger.info(currDateTime + " SYSVIEW METRICS: " + outputLine);
        }

    }
}
