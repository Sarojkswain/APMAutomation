package com.ca.apm.commons.flow;

import static org.apache.http.util.Args.notNull;

import com.ca.apm.automation.action.flow.IBuilder;
import com.ca.apm.automation.action.flow.IFlowContext;

/**
 * Created by nick on 8.10.14.
 */
public class RunCommandFlowContext implements IFlowContext {

	private String dir;
	private String command;

	public RunCommandFlowContext(Builder builder) {
		dir = builder.dir;
		command = builder.command;

	}

	public String getDir() {
		return dir;
	}

	public String getCommand() {
		return command;
	}

	public static class Builder implements IBuilder<RunCommandFlowContext> {

		private String dir;
		private String command;

		public Builder directory(String value) {
			this.dir = value;
			return this;
		}

		public Builder command(String value) {
			this.command = value;
			return this;
		}

		@Override
		public RunCommandFlowContext build() {
			RunCommandFlowContext runCommandFlowContext = new RunCommandFlowContext(
					this);
			notNull(runCommandFlowContext.dir, "fileToLoad");
			notNull(runCommandFlowContext.command, "action");
			return runCommandFlowContext;
		}
	}
}
