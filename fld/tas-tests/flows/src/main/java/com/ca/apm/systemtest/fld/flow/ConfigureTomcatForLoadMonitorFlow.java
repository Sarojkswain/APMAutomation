/**
 * 
 */
package com.ca.apm.systemtest.fld.flow;

import java.io.File;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.ca.apm.automation.action.core.annotations.Flow;
import com.ca.apm.automation.action.core.annotations.FlowContext;
import com.ca.apm.automation.action.flow.FlowBase;

/**
 * Configures Tomcat for running the FLD LoadMonitor.
 * 
 * @author keyja01
 *
 */
@Flow
public class ConfigureTomcatForLoadMonitorFlow extends FlowBase {

    @FlowContext
    private ConfigureTomcatForLoadMonitorFlowContext ctx;
    
    /**
     * 
     */
    public ConfigureTomcatForLoadMonitorFlow() {
        // TODO Auto-generated constructor stub
    }

    /* (non-Javadoc)
     * @see com.ca.apm.automation.action.core.IAutomationFlow#run()
     */
    @Override
    public void run() throws Exception {
        // needs to copy "export LOADMON=<location> to setenv.bat
        updateEnvironmentFile();
        
        // needs to copy properties to ${LOADMON}/loadmon_config.properties
        configureProperties();
    }
    
    
    private void configureProperties() {
        Map<String, String> configFileProps = new HashMap<>();
        configFileProps.put("loadmon.marker.dir", ctx.markerDir);
        File dest = new File(ctx.loadmonDir, "loadmon_config.properties");
        configFileFactory.create(dest).properties(configFileProps);
    }

    /**
     * Update setenv.bat/.sh with the location of our damn file
     */
    private void updateEnvironmentFile() {
        String line = String.format(ctx.loadmonFormatString, ctx.loadmonDir);
        File dest = new File(ctx.tomcatDirectory, ctx.environmentFile);
        List<String> list = Collections.singletonList(line);
        fileOperationFactory.createNew(dest).perform(list);
    }

}
