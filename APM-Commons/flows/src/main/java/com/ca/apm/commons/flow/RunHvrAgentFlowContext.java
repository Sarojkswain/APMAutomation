package com.ca.apm.commons.flow;

import com.ca.apm.automation.action.flow.IBuilder;
import com.ca.apm.automation.action.flow.IFlowContext;

import static org.apache.http.util.Args.notNull;
import static org.apache.http.util.Args.positive;

/**
 * Created by nick on 8.10.14.
 */
public class RunHvrAgentFlowContext implements IFlowContext {

    private final String emHost;
    private final int emPort;
    private final String installDir;
    private final String cloneconnections;
    private final String cloneagents;
    private final String secondspertrace;
    private String fileToLoad;
    private String action;

  public RunHvrAgentFlowContext(Builder builder) {
    emHost = builder.emHost;
    emPort = builder.emPort;
    installDir = builder.installDir;
    cloneconnections = builder.cloneconnections;
    cloneagents = builder.cloneagents;
    secondspertrace = builder.secondspertrace;
    fileToLoad = builder.fileToLoad;
    action = builder.action;

  }

  public String getEmHost() {
      return emHost;
    }

  public String getAction() {
      return action;
    }

  public int getEmPort() {
    return emPort;
  }

  public String getInstallDir() {
    return installDir;
  }

  public String getCloneconnections() {
      return cloneconnections;
    }

    public String getCloneagents() {
      return cloneagents;
    }

    public String getSecondsPerTrace() {
      return secondspertrace;
    }
    public String getFileToLoad() {
        return fileToLoad;
      }

    
  public static class Builder implements IBuilder<RunHvrAgentFlowContext> {

    private String emHost;
    private int emPort;
    private String installDir;
    private String cloneconnections;
    private String cloneagents;
    private String secondspertrace;
    private String fileToLoad;
    private String action;
    
    public Builder emHost(String value) {
        this.emHost = value;
        return this;
      }

    public Builder action(String value) {
        this.action = value;
        return this;
      }

      public Builder emPort(int value) {
        this.emPort = value;
        return this;
      }

      public Builder hvrAgentInstallationDirectory(String value) {
        installDir = value;
        return this;
      }

      public Builder cloneconnections(String value) {
          this.cloneconnections = value;
          return this;
        }
      
      public Builder fileToLoad(String value) {
          this.fileToLoad = value;
          return this;
        }

        public Builder cloneagents(String value) {
          this.cloneagents = value;
          return this;
        }

        public Builder secondspertrace(String value) {
            this.secondspertrace = value;
          return this;
        }

    @Override
    public RunHvrAgentFlowContext build() {
      RunHvrAgentFlowContext runHvrAgentFlowContext = new RunHvrAgentFlowContext(this);
      notNull(runHvrAgentFlowContext.emHost, "emHost");
      positive(runHvrAgentFlowContext.emPort, "emPort");
      notNull(runHvrAgentFlowContext.installDir, "installDir");
      notNull(runHvrAgentFlowContext.cloneconnections, "cloneconnections");
      notNull(runHvrAgentFlowContext.cloneagents, "cloneagents");
      notNull(runHvrAgentFlowContext.secondspertrace, "secondspertrace");
      notNull(runHvrAgentFlowContext.fileToLoad, "fileToLoad");
      notNull(runHvrAgentFlowContext.action, "action");
      return runHvrAgentFlowContext;
    }
  }
}
