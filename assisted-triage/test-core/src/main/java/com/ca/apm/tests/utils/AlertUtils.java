package com.ca.apm.tests.utils;

import java.util.Arrays;

import com.ca.apm.automation.action.flow.commandline.RunCommandFlowContext;
import com.ca.apm.tests.testbed.AssistedTriageTestbed;

public class AlertUtils {

    // Creates a Command Line flow and enables/disables PipeOrgan Alerts in Management Module for
    // CLWorkstation
    public RunCommandFlowContext statusAlertCLW(String status, String hostMachine,
        String agentName, String alertName) {

        String command =
            "java -Xmx256M -Duser=admin -Dpassword= -Dhost=" + hostMachine + " -Dport=5001 -jar "
                + AssistedTriageTestbed.CLW_LOCATION + "\\CLWorkstation.jar" + " " + status
                + " alerts matching \".*" + agentName + ".*\" in management modules matching \".*"
                + alertName + ".*\" ";

        // String batFile = status + "Alert.bat";
        RunCommandFlowContext runCommandFlowContext =
            new RunCommandFlowContext.Builder("").args(Arrays.asList(command)).build();
        return runCommandFlowContext;

    }

    public RunCommandFlowContext statusDefaultAlertCLW(String status, String hostMachine) {

        String command =
            "java -Xmx256M -Duser=admin -Dpassword= -Dhost=" + hostMachine + " -Dport=5001 -jar "
                + AssistedTriageTestbed.CLW_LOCATION + "\\CLWorkstation.jar" + " " + status
                + " management modules matching \".*Default.*\" ";

        // String batFile = status + "Alert.bat";
        RunCommandFlowContext runCommandFlowContext =
            new RunCommandFlowContext.Builder("").args(Arrays.asList(command)).build();

        return runCommandFlowContext;


    }
}
