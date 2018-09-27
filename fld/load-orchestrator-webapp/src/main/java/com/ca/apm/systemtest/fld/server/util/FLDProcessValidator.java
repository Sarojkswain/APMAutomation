package com.ca.apm.systemtest.fld.server.util;

import java.util.List;

import org.activiti.bpmn.model.BpmnModel;
import org.activiti.bpmn.model.Process;
import org.activiti.bpmn.model.ScriptTask;
import org.activiti.validation.ValidationError;
import org.activiti.validation.validator.ValidatorImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This implements our custom validation of uploaded BPMN processes.
 * Created by haiva01 on 19.3.2015.
 */
public class FLDProcessValidator extends ValidatorImpl {
    Logger log = LoggerFactory.getLogger(FLDProcessValidator.class);


    @Override
    public void validate(BpmnModel bpmnModel, List<ValidationError> errors) {
        List<Process> processes = bpmnModel.getProcesses();
        // Iterate over all processes.
        for (Process proc : processes) {
            // Find all script tasks and verify them.
            List<ScriptTask> scriptTasks = proc.findFlowElementsOfType(ScriptTask.class);
            for (ScriptTask sc : scriptTasks) {
                validateScriptTask(proc, sc, errors);
            }
        }
    }

    protected void validateScriptTask(Process process, ScriptTask sc,
        List<ValidationError> errors) {
        // Make sure that scriptFormat attribute is set. When it is not set, the script blocks
        // get ignored without a warning or an error.
        String scriptFormat = sc.getScriptFormat();
        if (scriptFormat == null) {
            log.warn("null script format of script task {} of process {}",
                sc.getName(), process.getName());
            addError(errors, "null-script-format", process, sc,
                "null script format");
        } else if (!(scriptFormat.equals("groovy")
            || scriptFormat.equals("javascript"))) {
            log.warn("script format \"{}\" of script task {} of process {} is not recognized",
                scriptFormat, sc.getName(), process.getName());
            addError(errors, "unknown-script-format", process, sc,
                "unrecognized script format \"" + scriptFormat + "\"");
        }
    }
}
