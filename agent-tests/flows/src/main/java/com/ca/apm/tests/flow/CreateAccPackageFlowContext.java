package com.ca.apm.tests.flow;

import com.ca.apm.automation.action.flow.IFlowContext;
import com.ca.tas.builder.BuilderBase;

/**
 * @author sinka08
 */
public class CreateAccPackageFlowContext implements IFlowContext {

	private String packageName;
	private String osName;
	private String process;
	private String agentVersion;
	private String accServerUrl;
	private long sleep;

	protected CreateAccPackageFlowContext(Builder builder) {

		this.packageName = builder.packageName;
		this.osName = builder.osName;
		this.process = builder.process;
		this.agentVersion = builder.agentVersion;
		this.accServerUrl = builder.accServerUrl;
		this.sleep = builder.sleep;
	}

	public String getPackageName() {
		return packageName;
	}

	public String getOsName() {
		return osName;
	}

	public String getProcess() {
		return process;
	}

	public String getAgentVersion() {
		return agentVersion;
	}

	public String getAccServerUrl() {
		return this.accServerUrl;
	}

	public long getSleep() {
        return sleep;
    }
	
	public static class LinuxBuilder extends Builder {

		@Override
		protected Builder builder() {
			return this;
		}

		@Override
		protected String getPathSeparator() {
			return LINUX_SEPARATOR;
		}

		@Override
		protected String getDeployBase() {
			return getLinuxDeployBase();
		}
	}

	public static class Builder extends BuilderBase<Builder, CreateAccPackageFlowContext> {
		private String packageName;
		private String osName;
		private String process;
		private String agentVersion;
		private String accServerUrl = "http://localhost:8088";
		private long sleep = 0;

		@Override
		public CreateAccPackageFlowContext build() {
			return getInstance();
		}

		@Override
		protected CreateAccPackageFlowContext getInstance() {
			return new CreateAccPackageFlowContext(this);
		}

		@Override
		protected Builder builder() {
			return this;
		}

		public Builder packageName(String packageName) {
			this.packageName = packageName;
			return builder();
		}

		public Builder osName(OsName osName) {
			this.osName = osName.toString();
			return builder();
		}

		public Builder process(Process process) {
			this.process = process.toString();
			return builder();
		}

		public Builder agentVersion(String agentVersion) {
			this.agentVersion = agentVersion;
			return builder();
		}

		public Builder accServerUrl(String url) {
			this.accServerUrl = url;
			return builder();
		}
		
		public Builder sleep(long sleep) {
            this.sleep = sleep;
            return builder();
        }
	}
	
	public static enum Process {
	    TOMCAT("tomcat"), 
	    TOMCATNOREDEF("tomcat-noredef"),
	    WEBSPHERE("websphere"),
	    WEBSPHERENOREDEF("websphere-noredef"),
	    WEBLOGIC("weblogic"),
	    WEBLOGICNOREDEF("weblogic-noredef");

	    private final String processName;
	    
	    Process(String processName) {	    
	        this.processName = processName;
	    }
	    
	    public String toString() {
	        return processName;
        }
	};

	public static enum OsName {
		WINDOWS, UNIX;

		public String toString() {
			return this.name().toLowerCase();
		}
	};

}