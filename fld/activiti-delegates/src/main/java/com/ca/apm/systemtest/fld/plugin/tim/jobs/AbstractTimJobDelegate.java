package com.ca.apm.systemtest.fld.plugin.tim.jobs;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import org.activiti.engine.delegate.DelegateExecution;

public abstract class AbstractTimJobDelegate {

    protected List<String> prepareTimNodesList(DelegateExecution execution) {
        Object timNodes = execution.getVariable("timNodes");
        if (timNodes == null) {
            timNodes = "";
        }
        StringTokenizer st = new StringTokenizer(timNodes.toString(), ",");
        List<String> nodes = new ArrayList<>();
        while (st.hasMoreTokens()) {
            String node = st.nextToken().trim();
            nodes.add(node);
        }

        execution.setVariable("nodeList", nodes);
        return nodes;
    }

}
