package com.ca.apm.systemtest.fld.plugin;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import org.activiti.engine.delegate.VariableScope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.ConfigurablePropertyAccessor;
import org.springframework.beans.InvalidPropertyException;
import org.springframework.beans.PropertyAccessException;
import org.springframework.beans.PropertyAccessorFactory;

/**
 * Utility class to provide common methods for working with Activiti's {@link VariableScope}.
 * 
 * @author Alexander Sinyushkin (sinal04@ca.com)
 *
 */
public class JavaDelegateUtils {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(JavaDelegateUtils.class);
    
    
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
     * @return             node name string
     */
    public static String getNodeExecutionVariable(VariableScope execution, String variable) {
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
    public static String getNodeExecutionVariable(VariableScope execution, String variable, String defaultValue) {
        String node = getStringByPropertyName(execution, variable, "name");
        if (node == null) {
            if (LOGGER.isInfoEnabled()) {
                LOGGER.info("Variable named {} has no value, returning default value {}",
                    variable, defaultValue);
            }
            return defaultValue;
        }
        return node;
    }

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
    public static String getEnumExecutionVariable(VariableScope execution, String variable) {
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
    public static String getEnumExecutionVariable(VariableScope execution, String variable, String defaultValue) {
        return getStringByGetter(execution, variable, "getValue", defaultValue);
    }
    
    /**
     * Retrieves a String property from a complex execution variable.
     * If the execution variable is a plain String object, the variable itself is returned then. 
     * Should be used to fetch values of enums, other complex objects. 
     * 
     * @param  execution     execution context 
     * @param  variable      variable name
     * @param  propertyName  name of a complex execution variable property
     * @return               property value
     */
    public static String getStringByPropertyName(VariableScope execution, String variable,
                                                 String propertyName) {
        return getStringByPropertyName(execution, variable, propertyName, null);
    }
    
    /**
     * Retrieves a String property from a complex execution variable.
     * If the execution variable is a plain String object, the variable itself is returned then. 
     * Should be used to fetch values of enums, other complex objects. 
     * 
     * @param  execution     execution context 
     * @param  variable      variable name
     * @param  propertyName  name of a complex execution variable property
     * @param  defaultValue  defaultValue
     * @return               property value
     */
    public static String getStringByPropertyName(VariableScope execution, String variable,
                                                 String propertyName, String defaultValue) {
        Object obj = getObjectExecutionVariable(execution, variable);
        if (obj != null) {
            if (obj instanceof String) {
                return (String) obj;
            } else {
                ConfigurablePropertyAccessor acc = PropertyAccessorFactory.forDirectFieldAccess(obj);
                try {
                    Object propertyVal = acc.getPropertyValue(propertyName);
                    if (propertyVal == null) {
                        if (LOGGER.isErrorEnabled()) {
                            LOGGER.error("Variable '{}' is an object of type: {}. "
                                + "Property searched in the object by name: {}. "
                                + "Property value is null.", 
                                variable, obj.getClass().getName(), propertyName);
                        }
                        if (LOGGER.isInfoEnabled()) {
                            LOGGER.info("Returning default value: {}", defaultValue);
                        }
                        return defaultValue;
                    }
                    if (!(propertyVal instanceof String)) {
                        if (LOGGER.isErrorEnabled()) {
                            LOGGER.error("Variable '{}' is an object of type: {}. "
                                + "Property searched in the object by name: {}. "
                                + "Found property type (expected String): {}. ", 
                                variable, obj.getClass().getName(), propertyName, 
                                propertyVal.getClass().getName());

                        }
                        if (LOGGER.isInfoEnabled()) {
                            LOGGER.info("Returning default value: {}", defaultValue);
                        }
                        return defaultValue;
                    }
                    return (String) propertyVal;
                } catch (InvalidPropertyException ipe) {
                    if (LOGGER.isErrorEnabled()) {
                        LOGGER.error("Invalid property read attempted", ipe);
                        
                    }
                    if (LOGGER.isInfoEnabled()) {
                        LOGGER.info("Returning default value: {}", defaultValue);
                    }
                    return defaultValue;
                } catch (PropertyAccessException pae) {
                    if (LOGGER.isErrorEnabled()) {
                        LOGGER.error("Failed to read property", pae);
                    }
                    if (LOGGER.isInfoEnabled()) {
                        LOGGER.info("Returning default value: {}", defaultValue);
                    }
                    return defaultValue;
                }
            }
        }

        if (LOGGER.isInfoEnabled()) {
            LOGGER.info("No variable named {} found. Returning default value: {}", variable, defaultValue);
        }

        return defaultValue;
    }

    /**
     * Retrieves a String value from a complex execution variable by a getter.
     * If the execution variable is a plain String object, the variable itself is returned then. 
     * Should be used to fetch values of enums, other complex objects. 
     * 
     * <p/>
     * Same as calling {@link #getStringByGetter(VariableScope, String, String, String) getStringByGetter(VariableScope, String, String, null)}.
     *  
     * @param   execution     execution context 
     * @param   variable      variable name
     * @param   getter        getter to call to fetch a String property from a complex object
     * @return                String 
     */
    public static String getStringByGetter(VariableScope execution, String variable,
                                           String getter) {
        return getStringByGetter(execution, variable, getter, null);
    }
    
    /**
     * Retrieves a String value from a complex execution variable by a getter.
     * If the execution variable is a plain String object, the variable itself is returned then. 
     * Should be used to fetch values of enums, other complex objects. 
     * 
     * @param  execution     execution context 
     * @param  variable      variable name
     * @param  getter        getter to call to fetch a String property from a complex object
     * @param  defaultValue  default value to return when the getter returned no value 
     * @return               String 
     */
    public static String getStringByGetter(VariableScope execution, String variable,
                                           String getter, String defaultValue) {
        Object obj = getObjectExecutionVariable(execution, variable);
        if (obj != null) {
            if (obj instanceof String) {
                return (String) obj;
            } else {
                try {
                    Method getterMethod = obj.getClass().getMethod(getter);
                    Object result = getterMethod.invoke(obj);
                    if (result == null) {
                        if (LOGGER.isErrorEnabled()) {
                            LOGGER.error("Variable '{}' is an object of type: {}. "
                                + "Getter method {} returned null", 
                                variable, obj.getClass().getName(), getter);
                        }
                        if (LOGGER.isInfoEnabled()) {
                            LOGGER.info("Returning default value: {}", defaultValue);
                        }
                        return defaultValue;
                    }
                    if (!(result instanceof String)) {
                        if (LOGGER.isErrorEnabled()) {
                            LOGGER.error("Variable '{}' is an object of type: {}. "
                                + "Getter method {} returned object of type {} though String was expected", 
                                variable, obj.getClass().getName(), getter, 
                                result.getClass().getName());

                        }
                        if (LOGGER.isInfoEnabled()) {
                            LOGGER.info("Returning default value: {}", defaultValue);
                        }
                        return defaultValue;
                    }
                    return (String) result;
                } catch (NoSuchMethodException e) {
                    if (LOGGER.isErrorEnabled()) {
                        LOGGER.error("Getter not found", e);
                    }
                    if (LOGGER.isInfoEnabled()) {
                        LOGGER.info("Returning default value: {}", defaultValue);
                    }
                    return defaultValue;
                } catch (SecurityException e) {
                    if (LOGGER.isErrorEnabled()) {
                        LOGGER.error("Failed to find getter", e);
                    }
                    if (LOGGER.isInfoEnabled()) {
                        LOGGER.info("Returning default value: {}", defaultValue);
                    }
                    return defaultValue;
                } catch (IllegalAccessException e) {
                    if (LOGGER.isErrorEnabled()) {
                        LOGGER.error("Failed to call getter", e);
                    }
                    if (LOGGER.isInfoEnabled()) {
                        LOGGER.info("Returning default value: {}", defaultValue);
                    }
                    return defaultValue;
                } catch (IllegalArgumentException e) {
                    if (LOGGER.isErrorEnabled()) {
                        LOGGER.error("Failed to call getter", e);
                    }
                    if (LOGGER.isInfoEnabled()) {
                        LOGGER.info("Returning default value: {}", defaultValue);
                    }
                    return defaultValue;
                } catch (InvocationTargetException e) {
                    if (LOGGER.isErrorEnabled()) {
                        LOGGER.error("Failed to call getter", e);
                    }
                    if (LOGGER.isInfoEnabled()) {
                        LOGGER.info("Returning default value: {}", defaultValue);
                    }
                    return defaultValue;
                }
            }
        }

        if (LOGGER.isInfoEnabled()) {
            LOGGER.info("No variable named {} found. Returning default value: {}", 
                variable, defaultValue);
        }

        return defaultValue;
    }

    /**
     * 
     * Returns a String value for the specified execution variable.
     * Equivalent to {@link #getStringExecutionVariable(VariableScope, String, String) getStringExecutionVariable(VariableScope, String, null)}.
     * 
     * <p/>
     * This method is type safe.
     *
     * @param execution   process execution context
     * @param variable    variable name
     * 
     * @return            string value
     */
    public static String getStringExecutionVariable(VariableScope execution, String variable) {
        return getStringExecutionVariable(execution, variable, null);
    }

    /**
     *
     * Returns a String value for the specified execution variable.
     * 
     * <p/>
     * This method is type safe.
     * 
     * @param execution     process execution context
     * @param variable      variable name
     * @param defaultValue  default value to be returned if the variable has no value or its value is empty
     * 
     * @return              string value
     */
    public static String getStringExecutionVariable(VariableScope execution, String variable, String defaultValue) {
        return fetchObjectExecutionVariable(execution, variable, String.class, defaultValue);
    }

    /**
     * Returns an Integer value for the specified execution variable.
     * 
     * Equivalent to {@link #getIntegerExecutionVariable(VariableScope, String, Integer) getIntegerExecutionVariable(VariableScope, String, null)}.
     * <p/>
     * This method is type safe.
     * 
     * @param execution     process execution context      
     * @param variable      variable name
     * @return              integer value
     */
    public static Integer getIntegerExecutionVariable(VariableScope execution, String variable) {
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
    public static Integer getIntegerExecutionVariable(VariableScope execution, String variable, Integer defaultValue) {
        Number number = getNumberExecutionVariable(execution, variable);
        if (number == null) {
            if (LOGGER.isInfoEnabled()) {
                LOGGER.info("Variable named {} has no value, returning default value {}",
                    variable, defaultValue);
            }
            return defaultValue;
        }
        return new Integer(number.intValue());
    }
    
    /**
     * Returns a Boolean value for the specified execution variable.
     * 
     * Equivalent to {@link #getBooleanExecutionVariable(VariableScope, String, Boolean) getBooleanExecutionVariable(VariableScope, String, null)}.
     * <p/>
     * This method is type safe.
     * 
     * @param execution     process execution context      
     * @param variable      variable name
     * @return              boolean value
     */
    public static Boolean getBooleanExecutionVariable(VariableScope execution, String variable) {
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
    public static Boolean getBooleanExecutionVariable(VariableScope execution, String variable, Boolean defaultValue) {
        return fetchObjectExecutionVariable(execution, variable, Boolean.class, defaultValue);
    }
    
    /**
     * Returns a Number value for the specified execution variable.
     * Equivalent to {@link #getNumberExecutionVariable(VariableScope, String, Number) getNumberExecutionVariable(VariableScope, String, null)}.
     * <p/>
     * This method is type safe.
     * 
     * @param execution     process execution context      
     * @param variable      variable name
     * @return              number value
     */
    public static Number getNumberExecutionVariable(VariableScope execution, String variable) {
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
    public static Number getNumberExecutionVariable(VariableScope execution, String variable, Number defaultValue) {
        return fetchObjectExecutionVariable(execution, variable, Number.class, defaultValue);
    }
    
    /**
     * Returns value as Java Object for the specified execution variable.
     * Equivalent to {@link #getObjectExecutionVariable(VariableScope, String, Object) getObjectExecutionVariable(VariableScope, String, null)}.
     * 
     * <p/>
     * This method is type safe.
     * 
     * @param execution     process execution context      
     * @param variable      variable name
     * @return              object value
     */
    public static Object getObjectExecutionVariable(VariableScope execution, String variable) {
        return getObjectExecutionVariable(execution, variable, null);
    }

    /**
     * Returns value as Java Object for the specified execution variable.
     * 
     * 
     * <p/>
     * This method is type safe.
     * 
     * @param execution      process execution context      
     * @param variable       variable name
     * @param defaultValue   default value to return if the specified property has no value
     *                       or does not exist  
     * @return               object value              
     */
    public static Object getObjectExecutionVariable(VariableScope execution, String variable, Object defaultValue) {
        return fetchObjectExecutionVariable(execution, variable, Object.class, defaultValue);
    }
    
    /**
     * Populates bean object's fields with execution variables having the same names where possible.
     * 
     * @param  execution                      process execution context
     * @param  bean                           bean to fill with execution properties 
     * @throws IllegalArgumentException       
     * @throws Exception
     */
    public static void populateBeanFromExecution(VariableScope execution, 
                                                 Object bean) throws IllegalArgumentException, Exception {
        Field[] fields = bean.getClass().getFields();
        
        for (Field f : fields) {
            int mods = f.getModifiers();
            
            if (!Modifier.isPublic(mods)) {
                // consider whether to try using getter/setter
                continue;
            }
            
            String name = f.getName();
            Object value = execution.getVariable(name);
            
            if (value == null) {
                f.set(bean, null);
            } else if (f.getType().isAssignableFrom(value.getClass())) {
                f.set(bean, value);
            } else if (f.getType().equals(String.class)) {
                f.set(bean, value.toString());
            } else {
                setBeanValue(name, bean, value);
            }
        }
    }

    private static void setBeanValue(String name, Object target, Object value) {
        ConfigurablePropertyAccessor acc = PropertyAccessorFactory.forDirectFieldAccess(target);
        acc.setPropertyValue(name, value);
    }

    private static <T extends Object> T fetchObjectExecutionVariable(VariableScope execution, String variable, 
                                                                     Class<T> clazz, T defaultValue) {
        try {
            T tVariableValue = execution.getVariable(variable, clazz);
            if (tVariableValue == null) {
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("Variable named {} of type {} has no value, returning default value: {}",
                        variable, clazz, defaultValue);
                }
                return defaultValue;
            }
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Requested Object variable named {} of type {} was found with value={}",
                    variable, clazz, tVariableValue);
            }
            return tVariableValue;
        } catch (Exception e) {
            if (LOGGER.isErrorEnabled()) {
                LOGGER.error("Exception while getting a variable named {} of type {}, returning default value: {}",
                    variable, clazz, defaultValue);
                LOGGER.error("Exception details:", e);
            }
            return defaultValue;
        }
    }

}
