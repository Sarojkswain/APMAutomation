package com.ca.apm.systemtest.sizingguidetest.flow;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ca.apm.automation.action.core.IAutomationFlow;
import com.ca.apm.automation.action.core.annotations.Flow;
import com.ca.apm.automation.action.core.annotations.FlowContext;
import com.ca.apm.automation.action.flow.FlowBase;
import com.ca.apm.systemtest.sizingguidetest.util.GenerateExcelReport;

@Flow
public class GenerateExcelReportFlow extends FlowBase implements IAutomationFlow {

    private static final Logger LOGGER = LoggerFactory.getLogger(GenerateExcelReportFlow.class);

    @FlowContext
    private GenerateExcelReportFlowContext ctx;

    @Override
    public void run() throws Exception {
        GenerateExcelReport generateExcelReport = new GenerateExcelReport();
        LOGGER.info("GenerateExcelReportFlow.run():: generating excel report, ctx = {}", ctx);
        generateExcelReport.copyResults(ctx.getResultsFile(), ctx.getTemplateFile(),
            ctx.getReportFile(), ctx.getSheetName(), ctx.isExpectHeader());
    }

}
