/**
 * 
 */
package com.ca.tas.role.tess;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.http.util.Args;

import com.ca.apm.systemtest.fld.flow.ConfigureTessFlow;
import com.ca.apm.systemtest.fld.flow.ConfigureTessFlowContext;
import com.ca.apm.systemtest.fld.flow.ConfigureTessFlowContext.PreferredBrowser;
import com.ca.apm.systemtest.fld.flow.ConfigureTessFlowContext.TessService;
import com.ca.apm.systemtest.fld.flow.TessWebServerFilterConfig;
import com.ca.apm.systemtest.fld.testbed.FLDConstants;
import com.ca.tas.builder.BuilderBase;
import com.ca.tas.client.IAutomationAgentClient;
import com.ca.tas.flow.tess.reports.TessReportSchedule;
import com.ca.tas.property.RolePropertyContainer;
import com.ca.tas.resolver.ITasResolver;
import com.ca.tas.role.AbstractRole;
import com.ca.tas.role.EmRole;
import com.ca.tas.role.IRole;
import com.ca.tas.role.TIMRole;

/**
 * Role used to configure the TESS UI.  It can be used to configure the list of monitors (TIMs), assign services
 * to collectors, and configure web server filters.
 * @author keyja01
 *
 */
public class ConfigureTessRole extends AbstractRole {
    public static String CONFIGURE_TESS_FLOW_KEY = "CFG_TESS_FLOW";
    private boolean autostart;
    private ConfigureTessFlowContext flow;
    
    private ConfigureTessRole(Builder builder) {
        super(builder.roleId, builder.getEnvProperties());
        this.autostart = builder.autostart;
        this.flow = builder.flow;
    }
    
    public static class Builder extends BuilderBase<Builder, ConfigureTessRole> {
        private boolean removeOldWebServerFilters;
        private String roleId;
        private ITasResolver resolver;
        private Set<String> tims = new HashSet<>();
        private Map<TessService, String> tessServiceMap = new HashMap<>();
        private Map<String, TessWebServerFilterConfig> tessWebServerFilters = new HashMap<>();
        private boolean removeOldTims;
        private String tessHost;
        private boolean autostart = false;
        private ConfigureTessFlowContext flow;
        private String reportEmail = TessReportSchedule.DEFAULT_CEM_REPORT_TO_EMAIL_ADDRESS;
        private String smtpHost = FLDConstants.DEFAULT_SMTP_HOST;
        private String seleniumGridHubHostAndPort;
        private PreferredBrowser preferredBrowser = PreferredBrowser.Firefox;
        
        public Builder(String roleId, ITasResolver resolver) {
            this.roleId = roleId;
            this.resolver = resolver;
        }
        
        public Builder tim(TIMRole timRole) {
            Args.notNull(timRole, "TIM role");
            String hostname = resolver.getHostnameById(timRole.getRoleId());
            tims.add(hostname);
            return this;
        }
        
        /**
         * Sets the Selenium Grid Hub URL. 
         * 
         * @param hostAndPort   full URL in form of protocol://host:port
         * @return              this builder object
         */
        public Builder seleniumGridHubHostAndPort(String hostAndPort) {
        	this.seleniumGridHubHostAndPort = hostAndPort;
        	return this;
        }
        
        /**
         * Sets the preferred browser to use.
         * 
         * @param    browser  Chrome, Fireforx, IE enum constant
         * @return   this builder object
         */
        public Builder preferredBrowser(PreferredBrowser browser) {
        	this.preferredBrowser = browser;
        	return this;
        }
        
        public Builder reportEmail(String reportEmail) {
            this.reportEmail = reportEmail;
            return this;
        }
        
        public Builder smtpHost(String host) {
        	this.smtpHost = host;
        	return this;
        }
        
        public Builder tessService(TessService service, IRole emRole) {
            String emHost = resolver.getHostnameById(emRole.getRoleId());
            tessServiceMap.put(service, emHost);
            return this;
        }
        
        public Builder webServerFilter(String name, TIMRole timRole, IRole appServerRole, int appServerPort) {
            String appServerHostname = resolver.getHostnameById(appServerRole.getRoleId());
            String timHost = resolver.getHostnameById(timRole.getRoleId());
            TessWebServerFilterConfig config = new TessWebServerFilterConfig(name, timHost, appServerHostname, appServerPort);
            tessWebServerFilters.put(name, config);
            
            return this;
        }
        
        /**
         * Delete any existing TIMs from the TESS UI prior to adding new TIMs.  Calling this will also set
         * removeOldWebServerFilters as well, since TIMs cannot be deleted with configured services.
         * @return
         */
        public Builder removeOldTims() {
            removeOldTims = true;
            return this;
        }
        
        /**
         * Remove any existing web server filters prior to adding new configurations.
         * @return
         */
        public Builder removeOldWebServerFilters() {
            removeOldWebServerFilters = true;
            return this;
        }
        
        /**
         * Automatically run the configuration flow during the deploy phase.
         * @return
         */
        public Builder autostart() {
            this.autostart = true;
            return this;
        }
        
        public Builder mom(IRole mom) {
            tessHost = resolver.getHostnameById(mom.getRoleId());
            return this;
        }
        
        
        @Override
        public ConfigureTessRole build() {
            ConfigureTessFlowContext.Builder builder = ConfigureTessFlowContext.getBuilder();
            builder.removeOldTims(removeOldTims)
                .removeOldWebServerFilters(removeOldWebServerFilters)
                .tims(tims)
                .tessServiceMap(tessServiceMap)
                .tessWebServerFilters(tessWebServerFilters)
                .tessUser("cemadmin")
                .tessPassword("quality")
                //.tessUser("Admin6")
                //.tessPassword("Admin6")
                .tessHostname(tessHost)
                .tessPort(8081)
                .reportEmail(reportEmail)
                .smtpHost(smtpHost)
                .seleniumGridHubHostAndPort(seleniumGridHubHostAndPort)
                .preferredBrowser(preferredBrowser);
            
            flow = builder.build();
            
            RolePropertyContainer props = getEnvProperties();
            props.add(CONFIGURE_TESS_FLOW_KEY, flow);
            return getInstance();
        }

        @Override
        protected Builder builder() {
            return this;
        }

        @Override
        protected ConfigureTessRole getInstance() {
            return new ConfigureTessRole(this);
        }
        
    }

    /* (non-Javadoc)
     * @see com.ca.tas.role.Deployable#deploy(com.ca.tas.client.IAutomationAgentClient)
     */
    @Override
    public void deploy(IAutomationAgentClient aaClient) {
        if (autostart) {
            runFlow(aaClient, ConfigureTessFlow.class, flow);
        }
    }

}
