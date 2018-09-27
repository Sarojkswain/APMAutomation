package com.ca.apm.tests.role;

import java.util.List;
import java.util.Map;

public interface ExecCmdRole {

    class ExecContext {

        private List<String> command;
        private Map<String, String> environment;

        public ExecContext(List<String> command, Map<String, String> environment) {
            this.command = command;
            this.environment = environment;
        }

        List<String> getCommand() {
            return command;
        }

        Map<String, String> getEnvironment() {
            return environment;
        }
    }

    List<ExecContext> getExecContext();

    int getRunDuration();

    String getWorkDirecotry();

    String getOkStatus();
}
