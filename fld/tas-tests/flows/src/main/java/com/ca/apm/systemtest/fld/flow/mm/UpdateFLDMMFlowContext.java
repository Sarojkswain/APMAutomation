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
public class UpdateFLDMMFlowContext implements IFlowContext {
    protected String mmJarFile;
    protected String emailAddresses;
    
    public interface Builder extends IGenericBuilder<UpdateFLDMMFlowContext> {
        public Builder mmJarFile(String mmJarFile);
        public Builder emailAddresses(String emailAddresses);
    }
    
    public static Builder getInstance() {
        BuilderFactory<UpdateFLDMMFlowContext, Builder> fact = new BuilderFactory<>();
        return fact.newBuilder(UpdateFLDMMFlowContext.class, Builder.class);
    }
}
