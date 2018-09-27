/**
 * 
 */
package com.ca.apm.systemtest.fld.role.loads;

import java.util.Arrays;
import java.util.List;

import com.ca.apm.automation.action.flow.commandline.RunCommandFlowContext;
import com.ca.apm.systemtest.fld.flow.DeploySOALoadFlow;
import com.ca.apm.systemtest.fld.flow.DeploySOALoadFlowContext;
import com.ca.apm.systemtest.fld.role.GroovyRole;
import com.ca.tas.client.IAutomationAgentClient;
import com.ca.tas.resolver.ITasResolver;
import com.ca.tas.role.webapp.JavaRole;

/**
 * This role sets up the the jax_*.groovy scripts used for the SOA load and provides
 * command flows to start them.
 * @author keyja01
 *
 */
public class WebLogicSOALoadRole extends AbstractFldLoadRole {
    private DeploySOALoadFlowContext ctx;
    
    protected WebLogicSOALoadRole(Builder builder) {
        super(builder.roleId, builder.getEnvProperties());
        
        ctx = builder.ctx;

    }
    
    public static class Builder extends FldLoadBuilderBase<Builder, WebLogicSOALoadRole> {
        private WebLogicDomainRole wlsDomain1Role;
        private WebLogicDomainRole wlsDomain2Role;
        private WurlitzerBaseRole wurlitzerBaseRole;
        private DeploySOALoadFlowContext ctx;
        private GroovyRole groovyRole;
        private JavaRole javaRole;
        private String windowTitle = "SOA Load";

        public Builder(String roleId, ITasResolver tasResolver) {
            super(roleId, tasResolver);
        }
        
        
        public Builder webLogicDomain1Role(WebLogicDomainRole wlsDomainRole) {
            this.wlsDomain1Role = wlsDomainRole;
            return this;
        }
        
        public Builder webLogicDomain2Role(WebLogicDomainRole wlsDomainRole) {
            this.wlsDomain2Role = wlsDomainRole;
            return this;
        }
        
        
        public Builder wurlitzerBaseRole(WurlitzerBaseRole wurlitzerBaseRole) {
            this.wurlitzerBaseRole = wurlitzerBaseRole;
            return this;
        }
        
        
        public Builder windowTitle(String windowTitle) {
            this.windowTitle = windowTitle;
            return this;
        }
        
        public Builder groovyRole(GroovyRole groovyRole) {
            this.groovyRole = groovyRole;
            return this;
        }
        
        
        public Builder customJava(JavaRole javaRole) {
            this.javaRole = javaRole;
            return this;
        }

        @Override
        protected WebLogicSOALoadRole buildRole() {
            String killFile = concatPaths(wurlitzerBaseRole.getDeployDir(), "killme.txt");
            ctx = DeploySOALoadFlowContext.getBuilder()
                .wurlitzer1HostName(tasResolver.getHostnameById(wlsDomain1Role.getRoleId()))
                .wurlitzer1Port(7001)
                .wurlitzer2HostName(tasResolver.getHostnameById(wlsDomain2Role.getRoleId()))
                .wurlitzer2Port(7002)
                .wurlitzerBaseDir(wurlitzerBaseRole.getDeployDir())
                .groovyHome(groovyRole.getInstallDir())
                .javaHome(javaRole.getInstallDir())
                .killFile(killFile)
                .batchFileTitle(windowTitle)
                .build();
            WebLogicSOALoadRole role = getInstance();
            return role;
        }

        @Override
        protected RunCommandFlowContext.Builder createStartLoadFlowContextBuilder() {
            String base = concatPaths(wurlitzerBaseRole.getDeployDir(), "scripts", "groovy");
            List<String> args = Arrays.asList(new String[] {">", "soa-load.log"});
            RunCommandFlowContext.Builder builder = new RunCommandFlowContext.Builder("soa.bat")
                .workDir(base)
                .args(args)
                .doNotPrependWorkingDirectory();
            
            return builder;
        }

        @Override
        protected RunCommandFlowContext.Builder createStopLoadFlowContextBuilder() {
            String base = wurlitzerBaseRole.getDeployDir();
            if (!base.endsWith("\\")) {
                base = base + "\\";
            }
            base = base + "scripts\\groovy";
            RunCommandFlowContext.Builder builder = new RunCommandFlowContext.Builder("soa-stop.bat")
                .workDir(base)
                .doNotPrependWorkingDirectory();
        
        return builder;
        }

        @Override
        protected WebLogicSOALoadRole getInstance() {
            WebLogicSOALoadRole role = new WebLogicSOALoadRole(this);
            return role;
        }

        @Override
        protected Builder builder() {
            return this;
        }
        
    }

    /* (non-Javadoc)
     * @see com.ca.tas.role.Deployable#deploy(com.ca.tas.client.IAutomationAgentClient)
     */
    @Override
    public void deploy(IAutomationAgentClient aaClient) {
        runFlow(aaClient, DeploySOALoadFlow.class, ctx);
    }

}
