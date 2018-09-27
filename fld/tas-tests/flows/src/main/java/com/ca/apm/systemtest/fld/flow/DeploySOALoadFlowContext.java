/**
 * 
 */
package com.ca.apm.systemtest.fld.flow;

import com.ca.apm.automation.action.flow.IFlowContext;

/**
 * @author keyja01
 *
 */
public class DeploySOALoadFlowContext implements IFlowContext {
    protected String wurlitzerBaseDir;
    protected String wurlitzer1HostName;
    protected int wurlitzer1Port = 7001;
    protected String wurlitzer2HostName;
    protected int wurlitzer2Port = 7002;
    protected String groovyHome;
    protected String javaHome;
    protected String batchFileTitle = "SOA Load";
    protected String killFile = "killme.txt";
    
    public static Builder getBuilder() {
        BuilderFactory<DeploySOALoadFlowContext, Builder> fact = new BuilderFactory<>();
        return fact.newBuilder(DeploySOALoadFlowContext.class, Builder.class);
    }
    
    public interface Builder extends IGenericBuilder<DeploySOALoadFlowContext> {
        public Builder wurlitzerBaseDir(String wurlitzerBaseDir);
        public Builder wurlitzer1HostName(String wurlitzer1HostName);
        public Builder wurlitzer2HostName(String wurlitzer2HostName);
        public Builder javaHome(String javaHome);
        public Builder groovyHome(String groovyHome);
        public Builder wurlitzer1Port(int wurlitzer1Port);
        public Builder wurlitzer2Port(int wurlitzer2Port);
        public Builder batchFileTitle(String batchFileTitle);
        public Builder killFile(String killFile);
    }
}
