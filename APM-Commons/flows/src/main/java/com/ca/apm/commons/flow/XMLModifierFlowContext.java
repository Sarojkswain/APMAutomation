package com.ca.apm.commons.flow;

import static org.apache.http.util.Args.notNull;

import java.util.List;

import com.ca.apm.automation.action.flow.IBuilder;
import com.ca.apm.automation.action.flow.IFlowContext;

/**
 * Created by nick on 8.10.14.
 */
public class XMLModifierFlowContext implements IFlowContext {

	private String methodName;
	private List<String> arguments;

	public XMLModifierFlowContext(Builder builder) {
		methodName = builder.methodName;
		arguments = builder.arguments;

	}

	public String getMethodName() {
		return methodName;
	}

	public List<String> getArguments() {
		return arguments;
	}

	public static class Builder implements IBuilder<XMLModifierFlowContext> {

		private String methodName;
		private List<String> arguments;

		public Builder methodName(String value) {
			this.methodName = value;
			return this;
		}

		public Builder arguments(List<String> value) {
			this.arguments = value;
			return this;
		}

		@Override
		public XMLModifierFlowContext build() {
			XMLModifierFlowContext xmlModifierFlowContext = new XMLModifierFlowContext(
					this);
			notNull(xmlModifierFlowContext.methodName, "methodName");
			notNull(xmlModifierFlowContext.arguments, "arguments");
			return xmlModifierFlowContext;
		}
	}
}
