package com.ca.apm.systemtest.fld.plugin.tim.jobs;

import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.JavaDelegate;

public class TimBTStatsJobDelegate extends AbstractTimJobDelegate implements JavaDelegate {

    @Override
    public void execute(DelegateExecution execution) throws Exception {
        prepareTimNodesList(execution);
    }

}
