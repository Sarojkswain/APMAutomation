/**
 * 
 */
package com.ca.apm.systemtest.fld.flow;

import com.ca.apm.automation.action.flow.IFlowContext;

/**
 * @author keyja01
 *
 */
public class ConfigureTomcatForLoadMonitorFlowContext implements IFlowContext {
    public String tomcatDirectory;
    public String environmentFile;
    public String loadmonDir;
    public String loadmonFormatString = "set LOADMON=%s";
    public String markerDir;
    
    public static Builder getBuilder() {
        BuilderFactory<ConfigureTomcatForLoadMonitorFlowContext, Builder> builder = new BuilderFactory<>();
        return builder.newBuilder(ConfigureTomcatForLoadMonitorFlowContext.class, Builder.class);
    }

    public interface Builder extends IGenericBuilder<ConfigureTomcatForLoadMonitorFlowContext> {
        public Builder tomcatDirectory(String tomcatDirectory);
        public Builder environmentFile(String environmentFile);
        public Builder loadmonDir(String loadmonDir);
        public Builder loadmonFormatString(String loadmonFormatString);
        public Builder markerDir(String markerDir);
    }
}
