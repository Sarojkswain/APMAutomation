/**
 * 
 */
package com.ca.apm.systemtest.fld.role.loads;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ca.apm.automation.action.flow.commandline.RunCommandFlowContext;
import com.ca.apm.automation.action.flow.commandline.RunCommandFlowContext.Builder;
import com.ca.tas.builder.BuilderBase;
import com.ca.tas.resolver.ITasResolver;

/**
 * Base class for FLD load role builder implementations.  Specifically, implementations will have to proved a {@link Builder} for
 * both starting and stopping the load.
 * @author keyja01
 *
 */
public abstract class FldLoadBuilderBase<T extends FldLoadBuilderBase<T,U>, U> extends BuilderBase<T, U> {
    private static final Logger LOGGER = LoggerFactory.getLogger(FldLoadBuilderBase.class);
    protected String roleId;
    protected ITasResolver tasResolver;
    protected RunCommandFlowContext.Builder startLoadContextBuilder;
    protected RunCommandFlowContext.Builder stopLoadContextBuilder;
    protected RunCommandFlowContext startLoadContext;
    protected RunCommandFlowContext stopLoadContext;
    public static final String DEFAULT_LOAD_START_KEY = "LOAD_START";
    public static final String DEFAULT_LOAD_STOP_KEY = "LOAD_STOP";
    
    protected String startLoadKey = DEFAULT_LOAD_START_KEY;
    protected String stopLoadKey = DEFAULT_LOAD_STOP_KEY;
    
    public FldLoadBuilderBase(String roleId, ITasResolver tasResolver) {
        this.roleId = roleId;
        this.tasResolver = tasResolver;
    }
    
    @Override
    public U build() {
        preBuildInit();
        
        startLoadContext = createStartLoadFlowContextBuilder().build();
        stopLoadContext = createStopLoadFlowContextBuilder().build();
        LOGGER.info("Adding start load flow using key " + startLoadKey + ": " + startLoadContext);
        getEnvProperties().add(startLoadKey, startLoadContext);
        LOGGER.info("Adding stop load flow using key " + stopLoadKey + ": " + stopLoadContext);
        getEnvProperties().add(stopLoadKey, stopLoadContext);
        
        U u = buildRole();
        
        return u;
    }
    
    protected void preBuildInit() {
    }
    
    public String getRoleId() {
        return roleId;
    }
    
    public ITasResolver getTasResolver() {
        return tasResolver;
    }
    
    protected abstract U buildRole();

    /**
     * Returns a {@link Builder} to create the startLoadFlow
     * @return
     */
    protected abstract RunCommandFlowContext.Builder createStartLoadFlowContextBuilder();
    
    /**
     * Returns a {@link Builder} to create the stopLoadFlow
     * @return
     */
    protected abstract RunCommandFlowContext.Builder createStopLoadFlowContextBuilder();
    
    @SuppressWarnings("unchecked")
    public T startLoadKey(String key) {
        this.startLoadKey = key;
        return (T) this;
    }

    @SuppressWarnings("unchecked")
    public T stopLoadKey(String key) {
        this.stopLoadKey = key;
        return (T) this;
    }
}
