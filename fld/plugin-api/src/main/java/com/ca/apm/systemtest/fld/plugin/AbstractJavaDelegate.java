package com.ca.apm.systemtest.fld.plugin;

import java.text.MessageFormat;

import org.activiti.engine.delegate.BpmnError;
import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.JavaDelegate;
import org.activiti.engine.delegate.VariableScope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ca.apm.systemtest.fld.common.ErrorUtils;
import com.ca.apm.systemtest.fld.common.logmonitor.FldLevel;
import com.ca.apm.systemtest.fld.common.logmonitor.FldLogger;
import com.ca.apm.systemtest.fld.proxy.AgentProxyFactory;

/**
 * Abstract Java delegate class to provide commonly used methods to all delegate classes.
 * 
 * @author KEYJA01
 */
public abstract class AbstractJavaDelegate implements JavaDelegate {
    public static final String ERROR_MESSAGE = "errorMessage";
    public static final String NODE_NAME = "nodeName";
    public static final String NODE = "node";
    public static final String SERVER_ID = "serverId";

    public static final JavaDelegate DO_NOTHING_DELEGATE = new JavaDelegate() {
        @Override
        public void execute(DelegateExecution delegateExecution) throws Exception {
            /* do nothing */
        }
    };
    
    protected static final Logger LOGGER = LoggerFactory.getLogger(AbstractJavaDelegate.class);
    
    protected AgentProxyFactory agentProxyFactory;
    protected NodeManager nodeManager;

    protected FldLogger fldLogger;

    public AbstractJavaDelegate(NodeManager nodeManager, AgentProxyFactory agentProxyFactory) {
        this(nodeManager, agentProxyFactory, null);
    }

    public AbstractJavaDelegate(NodeManager nodeManager, AgentProxyFactory agentProxyFactory, FldLogger fldLogger) {
        this.nodeManager = nodeManager;
        this.agentProxyFactory = agentProxyFactory;
        this.fldLogger = fldLogger;
    }

    public void setFldLogger(FldLogger fldLogger) {
        this.fldLogger = fldLogger;
    }
    
    @Override
    public void execute(DelegateExecution execution) throws Exception {
        if (execution.hasVariable(ERROR_MESSAGE)) {
            execution.removeVariable(ERROR_MESSAGE);
        }
        try {
            handleExecution(execution);
        } catch (BpmnError error) {
            logError(error);
            execution.setVariable(ERROR_MESSAGE, error.getErrorCode());
            throw error;
        } catch (Throwable t) {
            logError(t);
            execution.setVariable(ERROR_MESSAGE, t.getMessage());
            throw new BpmnError("ExceptionFromAgent", t.getMessage());
        }
    }

    //----------- FLD specific logging
    
    protected void fldLog(FldLevel level, String category, String tag, 
                          Throwable exception, String message) {
        if (fldLogger == null) {
            if (LOGGER.isWarnEnabled()) {
                LOGGER.warn("Delegate's FLD logger is null!");
            }
            return;
        }
        fldLogger.log(level, category, tag, message, exception);
    }

    protected String fldLog(FldLevel level, String category, String tag, 
                            Throwable exception, String pattern, 
                            Object...arguments) {
        if (fldLogger == null) {
            return null;
        }
        String msg = MessageFormat.format(pattern, arguments);
        fldLogger.log(level, category, tag, msg, exception);
        return msg;
    }

    //----------- INFO level logging
    
    protected void logInfo(Throwable ex) {
        logInfo(getClass().getName(), "", ex.getMessage(), ex);
    }
    
    protected void logInfo(String message) {
        logInfo(getClass().getName(), "", message);
    }

    protected void logInfo(String tag, String message) {
        logInfo(getClass().getName(), tag, message);
    }
    
    protected void logInfo(String category, String tag, String message) {
        logInfo(category, tag, message, (Throwable) null);
    }

    protected void logInfo(String category, String tag, String pattern, Object... arguments) {
        logInfo(category, tag, null, pattern, arguments);
    }
    
    protected void logInfo(String category, String tag, Throwable ex, String pattern, Object... arguments) {
        String msg = null;
        if (getLogger().isInfoEnabled()) {
            msg = MessageFormat.format(pattern, arguments);
            getLogger().info(msg, ex);
        }
        
        if (msg == null) {
            fldLog(FldLevel.INFO, category, tag, ex, pattern, arguments);
        } else {
            fldLog(FldLevel.INFO, category, tag, ex, msg);
        }
    }

    protected void logInfo(String category, String tag, String message, Throwable ex) {
        if (getLogger().isInfoEnabled()) {
            getLogger().info(message, ex);
        }
        fldLog(FldLevel.INFO,  category,  tag, ex, message);
    }

    //----------- ERROR level logging
    
    protected void logError(Throwable ex) {
        logError(getClass().getName(), "", ex.getMessage(), ex);
    }
    
    protected void logError(String message) {
        logError(getClass().getName(), "", message);
    }

    protected void logError(String tag, String message) {
        logError(getClass().getName(), tag, message);
    }
    
    protected void logError(String category, String tag, String message) {
        logError(category, tag, message, (Throwable) null);
    }

    protected String logError(String category, String tag, String pattern, Object... arguments) {
        return logError(category, tag, null, pattern, arguments);
    }

    protected String logError(String category, String tag, Throwable ex, String pattern, Object... arguments) {
        String msg = null;
        if (getLogger().isErrorEnabled()) {
            msg = MessageFormat.format(pattern, arguments);
            getLogger().error(msg, ex);
        }
        
        if (msg == null) {
            msg = fldLog(FldLevel.ERROR, category, tag, ex, pattern, arguments);
        } else {
            fldLog(FldLevel.ERROR, category, tag, ex, msg);
        }
        return msg;
    }

    protected void logError(String category, String tag, String message, Throwable ex) {
        if (getLogger().isErrorEnabled()) {
            getLogger().error(message, ex);
        }
        fldLog(FldLevel.ERROR, category,  tag, ex, message);
    }
    
    //----------- DEBUG level logging
    
    protected void logDebug(Throwable ex) {
        logDebug(getClass().getName(), "", ex.getMessage(), ex);
    }
    
    protected void logDebug(String message) {
        logDebug(getClass().getName(), "", message);
    }

    protected void logDebug(String tag, String message) {
        logDebug(getClass().getName(), tag, message);
    }
    
    protected void logDebug(String category, String tag, String message) {
        logDebug(category, tag, message, (Throwable) null);
    }

    protected void logDebug(String category, String tag, String pattern, Object... arguments) {
        logDebug(category, tag, null, pattern, arguments);
    }

    protected void logDebug(String category, String tag, Throwable ex, String pattern, Object... arguments) {
        String msg = null;
        if (getLogger().isDebugEnabled()) {
            msg = MessageFormat.format(pattern, arguments);
            getLogger().debug(msg, ex);
        }
        
        if (msg == null) {
            fldLog(FldLevel.DEBUG, category, tag, ex, pattern, arguments);
        } else {
            fldLog(FldLevel.DEBUG, category, tag, ex, msg);
        }
    }

    protected void logDebug(String category, String tag, String message, Throwable ex) {
        if (getLogger().isDebugEnabled()) {
            getLogger().debug(message, ex);
        }
        fldLog(FldLevel.DEBUG, category,  tag, ex, message);
    }

    //----------- WARN level logging
    
    protected void logWarn(Throwable ex) {
        logWarn(getClass().getName(), "", ex.getMessage(), ex);
    }
    
    protected void logWarn(String message) {
        logWarn(getClass().getName(), "", message);
    }

    protected void logWarn(String tag, String message) {
        logWarn(getClass().getName(), tag, message);
    }
    
    protected void logWarn(String category, String tag, String message) {
        logWarn(category, tag, message, (Throwable) null);
    }

    protected void logWarn(String category, String tag, String pattern, Object... arguments) {
        logWarn(category, tag, null, pattern, arguments);
    }
    
    protected void logWarn(String category, String tag, Throwable ex, String pattern, Object... arguments) {
        String msg = null;
        if (getLogger().isWarnEnabled()) {
            msg = MessageFormat.format(pattern, arguments);
            getLogger().warn(msg, ex);
        }
        
        if (msg == null) {
            fldLog(FldLevel.WARN, category, tag, ex, pattern, arguments);
        } else {
            fldLog(FldLevel.WARN, category, tag, ex, msg);
        }
    }

    protected void logWarn(String category, String tag, String message, Throwable ex) {
        if (getLogger().isWarnEnabled()) {
            getLogger().warn(message, ex);
        }
        fldLog(FldLevel.WARN, category,  tag, ex, message);
    }

    //-------------------------------
    

    protected abstract void handleExecution(DelegateExecution execution) throws Throwable;

    /**
     * Returns a {@link Plugin} object for the desired plugin class and specified node.
     * 
     * <p/>
     * Note that <code>nodeVariable</code> is not the name of the node but the name of the execution variable 
     * which should be used to fetch the node name.
     * 
     * @param execution         workflow execution context
     * @param nodeVariable      name of the node variable (not the node name itself!)
     * @param pluginId          FLD Load Orchestrator plugin id
     * @param pluginClass       FLD Load Orchestrator plugin class implementing {@link Plugin}
     * @return                  app server plugin
     */
    protected <T extends Plugin> T loadPlugin(VariableScope execution, 
                                              String nodeVariable,
                                              String pluginId, 
                                              Class<T> pluginClass) {
        String nodeName = getNodeExecutionVariable(execution, nodeVariable);
        checkNodeIsAvailableAndFailIfNot(nodeName, pluginId);
        return agentProxyFactory.createProxy(nodeName).getPlugin(pluginId, pluginClass);
    }

    /**
     * Returns a {@link Plugin} object for the desired plugin class and specified node.
     * 
     * @param nodeName         name of the node 
     * @param pluginId         FLD Load Orchestrator plugin id
     * @param pluginClass      FLD Load Orchestrator plugin class implementing {@link Plugin}
     * @return                 app server plugin
     */
    protected <T extends Plugin> T getPluginForNode(String nodeName,
                                                    String pluginId, 
                                                    Class<T> pluginClass) {
        checkNodeIsAvailableAndFailIfNot(nodeName, pluginId);
        return agentProxyFactory.createProxy(nodeName).getPlugin(pluginId, pluginClass);
    }
    
    /**
     * Returns an {@link AppServerPlugin} object for the desired app server plugin 
     * class and specified node. 
     * 
     * @param execution         workflow execution context
     * @param nodeVariable      name of the node variable
     * @param pluginId          FLD Load Orchestrator plugin id
     * @param pluginClass       FLD Load Orchestrator plugin class implementing {@link AppServerPlugin}
     * @return                  app server plugin
     */
    protected <T extends AppServerPlugin> T getAppServerPlugin(VariableScope execution, 
                                                               String nodeVariable, String pluginId, 
                                                               Class<T> pluginClass) {
        String nodeName = getNodeExecutionVariable(execution, nodeVariable);
        checkNodeIsAvailableAndFailIfNot(nodeName, pluginId);
        return agentProxyFactory.createProxy(nodeName).getPlugin(pluginId, pluginClass);
    }

    /**
     * Checks if the specified node is available and fails with exception if it is not. 
     * 
     * @param nodeName   node name
     * @param pluginId
     */
    protected void checkNodeIsAvailableAndFailIfNot(String nodeName, String pluginId) {
        if (!nodeManager.checkNodeAvailable(nodeName)) {
            RuntimeException exception = ErrorUtils.logErrorAndReturnException(LOGGER,
                "Failed to load plugin {0} on {1}. Node is not available.", 
                pluginId, nodeName);
            logError(exception);
            throw exception;
        }
    }
    
    //---------- Execution Variable Getters
    
    /**
     * Returns a String enum value for the specified execution variable.
     * Equivalent to {@link #getEnumExecutionVariable(VariableScope, String, String) getEnumExecutionVariable(VariableScope, String, null)}.
     * 
     * <p/>
     * This method is type safe.
     *
     * @param execution     process execution context
     * @param variable      variable name
     * @return              String enum value
     */
    protected String getEnumExecutionVariable(VariableScope execution, String variable) {
        return getEnumExecutionVariable(execution, variable, null);
    }
    
    /**
     * Returns a String enum value for the specified execution variable.
     * 
     * <p/>
     * This method is type safe.
     *
     * @param execution     process execution context
     * @param variable      variable name
     * @param defaultValue  default value 
     * @return              String enum value
     */
    protected String getEnumExecutionVariable(VariableScope execution, String variable, String defaultValue) {
        return JavaDelegateUtils.getEnumExecutionVariable(execution, variable, defaultValue);
    }
    
    protected String getStringByGetter(VariableScope execution, String variable,
                                       String getter) {
        return getStringByGetter(execution, variable, getter, null);
    }
    
    protected String getStringByGetter(VariableScope execution, String variable, 
                                       String getter, String defaultValue) {
        return JavaDelegateUtils.getStringByGetter(execution, variable, getter, defaultValue);
    }    
    
    /**
     * Returns a Boolean value for the specified execution variable.
     * 
     * Equivalent to {@link #getBooleanExecutionVariable(VariableScope, String, Boolean) getBooleanExecutionVariable(DelegateExecution, String, null)}.
     * <p/>
     * This method is type safe.
     * 
     * @param execution     process execution context      
     * @param variable      variable name
     * @return              boolean value
     */
    protected Boolean getBooleanExecutionVariable(VariableScope execution, String variable) {
        return getBooleanExecutionVariable(execution, variable, null);
    }
    
    /**
     * Returns a Boolean value for the specified execution variable.
     * 
     * <p/>
     * This method is type safe.
     * 
     * @param execution     process execution context      
     * @param variable      variable name
     * @param defaultValue  default value to return in case the provided property has no value or if 
     *                      a property of the same name but different type was found
     * @return              boolean value
     */
    protected Boolean getBooleanExecutionVariable(VariableScope execution, String variable, Boolean defaultValue) {
        return JavaDelegateUtils.getBooleanExecutionVariable(execution, variable, defaultValue);
    }
    
    /**
     * Returns an Integer value for the specified execution variable.
     * 
     * Equivalent to {@link #getIntegerExecutionVariable(VariableScope, String, Integer) getIntegerExecutionVariable(DelegateExecution, String, null)}.
     * <p/>
     * This method is type safe.
     * 
     * @param execution     process execution context      
     * @param variable      variable name
     * @return              integer value
     */
    protected Integer getIntegerExecutionVariable(VariableScope execution, String variable) {
        return getIntegerExecutionVariable(execution, variable, null);
    }
    
    /**
     * Returns an Integer value for the specified execution variable.
     * 
     * <p/>
     * This method is type safe.
     * 
     * @param execution     process execution context      
     * @param variable      variable name
     * @param defaultValue  default value to return in case the provided property has no value or if 
     *                      a property of the same name but different type was found
     * @return              integer value
     */
    protected Integer getIntegerExecutionVariable(VariableScope execution, String variable, Integer defaultValue) {
        return JavaDelegateUtils.getIntegerExecutionVariable(execution, variable, defaultValue);
    }
    
    /**
     * Returns a Number value for the specified execution variable.
     * Equivalent to {@link #getNumberExecutionVariable(VariableScope, String, Number) getNumberExecutionVariable(DelegateExecution, String, null)}.
     * <p/>
     * This method is type safe.
     * 
     * @param execution     process execution context      
     * @param variable      variable name
     * @return              number value
     */
    protected Number getNumberExecutionVariable(VariableScope execution, String variable) {
        return getNumberExecutionVariable(execution, variable, null);
    }
    
    /**
     * Returns a Number value for the specified execution variable.
     * 
     * <p/>
     * This method is type safe.
     * 
     * @param execution     process execution context      
     * @param variable      variable name
     * @param defaultValue  default value to return in case the provided property has no value or if 
     *                      a property of the same name but different type was found
     * @return              number value
     */
    protected Number getNumberExecutionVariable(VariableScope execution, String variable, Number defaultValue) {
        return JavaDelegateUtils.getNumberExecutionVariable(execution, variable, defaultValue);
    }
    
    /**
     * Retrieves node name from execution context variable.
     * 
     * This method can be used to get the node name in both cases: <br/>
     * 
     *  - when the execution variable is of String type <br/> 
     *  - when the execution variable is a value object with a readable property "name"  
     *  
     * @param  execution   execution context 
     * @param  variable    variable name
     * @return node name   string
     */
    protected String getNodeExecutionVariable(VariableScope execution, String variable) {
        return getNodeExecutionVariable(execution, variable, null);
    }

    /**
     * Retrieves node name from execution context variable.
     * 
     * This method can be used to get the node name in both cases: <br/>
     * 
     *  - when the execution variable is of String type <br/> 
     *  - when the execution variable is a value object with a readable property "name"  
     *  
     * @param  execution     execution context 
     * @param  variable      variable name
     * @param  defaultValue  default value 
     * @return               node name string
     */
    protected String getNodeExecutionVariable(VariableScope execution, String variable, String defaultValue) {
        String node = JavaDelegateUtils.getNodeExecutionVariable(execution, variable, defaultValue);
        logInfo("AbstractJavaDelegate", "Node Execution Variable", 
            "Value of node name execution variable named ''{0}'': {1}", variable, node);
        return node;
    }

    /**
     * Typesafe method to retrieve an execution variable as a string.
     * 
     * <p/>
     * This method does not do type checking of the specified parameter and just calls 
     * {@link Object#toString()} on the found object (if any).
     * 
     * @param execution   process execution context
     * @param variable    variable name
     * 
     * @return  execution variable value
     * @deprecated use {@link #getStringExecutionVariable(VariableScope, String)}
     */
    protected String getExecutionVariable(VariableScope execution, String variable) {
        //Backwords compatibility
        Object obj = execution.getVariable(variable);
        if (obj != null) {
            return obj.toString();
        }
        return null;
    }

    protected String getStringExecutionVariable(VariableScope execution, String variable) {
        return getStringExecutionVariable(execution, variable, null);
    }
    
    protected String getStringExecutionVariable(VariableScope execution, String variable, String defaultValue) {
        return JavaDelegateUtils.getStringExecutionVariable(execution, variable, defaultValue);
    }
    
    /**
     * Typesafe method to retrieve an execution variable value as a string returning default value if 
     * the variable has no value or its value is empty. 
     * 
     * <p/>
     * This method does not do type checking of the specified parameter and just calls 
     * {@link Object#toString()} on the found object (if any).
     * 
     * @param execution     process execution context
     * @param variable      variable name
     * @param defaultValue  default value to be returned if the variable has no value or its value is empty
     * 
     * @return
     * @deprecated use {@link #getStringExecutionVariable(VariableScope, String, String)}
     */
    protected String getExecutionVariable(VariableScope execution, String variable, String defaultValue) {
        return JavaDelegateUtils.getStringExecutionVariable(execution, variable, defaultValue);
    }

    /**
     * Return execution variable value as {@link Object}.
     * 
     * @param execution
     * @param variable
     * @return
     */
    protected Object getExecutionVariableObject(VariableScope execution, String variable) {
        return getExecutionVariableObject(execution, variable, null);
    }

    /**
     * Return execution variable value as {@link Object}.
     * 
     * @param execution
     * @param variable
     * @param defaultValue
     * @return
     */
    protected Object getExecutionVariableObject(VariableScope execution, String variable, Object defaultValue) {
        return JavaDelegateUtils.getObjectExecutionVariable(execution, variable, defaultValue);
    }

    protected void setExecutionVariable(VariableScope execution, String var, Object value) {
        LOGGER.info("Setting variable {} in scope {} to value {}", var, execution, value);
        try {
            execution.setVariable(var, value);
        } catch (Exception ex) {
            throw ErrorUtils.logExceptionAndWrapFmt(LOGGER, ex,
                "Failed to set variable {1} in scope {2} to value {3}. Exception: {0}",
                var, execution, value);
        }
    }


    /**
     * Attempts to populate all of the public fields of a bean with values from the execution.
     *
     * @param execution
     * @param bean
     * @throws Exception
     * @throws IllegalArgumentException
     */
    protected void populateBeanFromExecution(VariableScope execution,
        Object bean) throws IllegalArgumentException, Exception {
        
        JavaDelegateUtils.populateBeanFromExecution(execution, bean);
    }

    protected Logger getLogger() {
        return LOGGER;
    }
    
}
