/**
 * 
 */
package com.ca.apm.systemtest.fld.flow.mm;

import com.ca.apm.automation.action.flow.IFlowContext;
import com.ca.apm.systemtest.fld.flow.BuilderFactory;
import com.ca.apm.systemtest.fld.flow.IGenericBuilder;

/**
 * @author keyja01
 *
 */
public class CreateMMFlowContext implements IFlowContext {
    protected ManagementModule managementModule;
    protected String filename;
    protected String deployDir;
    
    public interface Builder extends IGenericBuilder<CreateMMFlowContext> {
        public Builder managementModule(ManagementModule managementModule);
        public Builder filename(String filename);
        public Builder deployDir(String deployDir);
    }
    
    public static Builder getInstance() {
        BuilderFactory<CreateMMFlowContext, Builder> fact = new BuilderFactory<>();
        return fact.newBuilder(CreateMMFlowContext.class, Builder.class);
    }
}
