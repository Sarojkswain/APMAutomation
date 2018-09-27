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
package com.ca.apm.tests.flow;

import com.ca.apm.automation.action.core.annotations.Flow;
import com.ca.apm.automation.action.core.annotations.FlowContext;
import com.ca.apm.automation.action.flow.FlowBase;
import com.ca.apm.automation.action.flow.commandline.Execution;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * RegisterNetAgentFlow
 *
 * @author Erik Melecky (meler02@ca.com)
 */
@Flow
public class CsvToXlsFlow extends FlowBase {

    private static final Logger LOGGER = LoggerFactory.getLogger(CsvToXlsFlow.class);
    @FlowContext
    private CsvToXlsFlowContext context;

    public CsvToXlsFlow() {
    }

    public void run() throws IOException {
        List<String> args = new ArrayList<>();
        args.add("-Xmx" + context.getHeapMemory());
        args.add("-jar");
        args.add(this.context.getCsvToXlsJarPath());
        args.add(this.context.getTemplateFileName());
        args.add(this.context.getOutputFileName());
        for (Map.Entry<String, String> sheetEntry : this.context.getSheetsMapping().entrySet()) {
            args.add(this.context.getShareDir() + "/" + sheetEntry.getKey());
            args.add(sheetEntry.getValue());
        }
        String[] argsArr = new String[args.size()];
        argsArr = args.toArray(argsArr);
        try {
            runCsvToXls(argsArr);
        } catch (InterruptedException var3) {
            throw new IllegalStateException(var3);
        }
        LOGGER.info("Flow has finished.");
    }

    protected void runCsvToXls(String[] argsArr) throws InterruptedException {
        int responseCode = this.getExecutionBuilder(LOGGER, "java")
                .args(argsArr).build().go();
        switch (responseCode) {
            case 0:
                LOGGER.info("CsvToXls completed SUCCESSFULLY! Congratulations!");
                return;
            default:
                throw new IllegalStateException(String.format("CsvToXls failed (%d)", new Object[]{responseCode}));
        }
    }


    protected Execution.Builder getExecutionBuilder(Logger logger, String executable) {
        return new Execution.Builder(executable, logger);
    }
}
