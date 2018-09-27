/**
 *
 */
package com.ca.apm.systemtest.fld.plugin;

import com.ca.apm.systemtest.fld.common.ErrorUtils;
import com.ca.apm.systemtest.fld.common.logmonitor.FldLevel;
import com.ca.apm.systemtest.fld.common.logmonitor.FldLogger;
import com.ca.apm.systemtest.fld.plugin.annotations.ExposeAttribute;
import com.ca.apm.systemtest.fld.plugin.annotations.ExposeMethod;
import com.ca.apm.systemtest.fld.plugin.annotations.OperationParameter;
import com.ca.apm.systemtest.fld.plugin.cm.ConfigurationManager;
import com.ca.apm.systemtest.fld.plugin.vo.Attribute;
import com.ca.apm.systemtest.fld.plugin.vo.Operation;
import com.ca.apm.systemtest.fld.plugin.vo.Parameter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;

import java.io.File;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.MessageFormat;
import java.util.ArrayList;

/**
 * Abstract Load Orchestrator plugin implementation.
 * 
 * @author keyja01
 *
 */
public abstract class AbstractPluginImpl implements Plugin {
    protected static final Logger log = LoggerFactory.getLogger(AbstractPluginImpl.class);
    private final String pluginName = this.getClass().getSimpleName();
    public static ThreadLocal<String> currentOperation = new ThreadLocal<String>();

    protected ConfigurationManager configurationManager;
    @Autowired(required = false)
    protected FldLogger fldLogger;

    private ArrayList<Attribute> attributes;
    private ArrayList<Operation> operations;

    /**
     * Scan the class for eligible fields and operations to export via the plugin interface
     */
    protected AbstractPluginImpl() {
        Class<? extends AbstractPluginImpl> klass = getClass();
        Field[] fields = klass.getDeclaredFields();
        attributes = new ArrayList<>(fields.length / 2);
        for (Field f : fields) {
            if (f.isAnnotationPresent(ExposeAttribute.class)) {
                Attribute attr = new Attribute();
                attr.setJavaType(f.getClass().getName());
                attr.setName(f.getName());
                attr.setReadable(true);
                attr.setWritable(!f.getAnnotation(ExposeAttribute.class).readOnly());
                attributes.add(attr);
            }
        }

        Method[] methods = klass.getMethods();
        operations = new ArrayList<>(methods.length / 2);
        for (Method m : methods) {
            if (m.isAnnotationPresent(ExposeMethod.class)) {
                ExposeMethod em = m.getAnnotation(ExposeMethod.class);
                Operation op = new Operation();
                op.setDescription(em.description());
                op.setJavaReturnType(m.getReturnType().getName());
                op.setName(m.getName());

                Class<?>[] parameterTypes = m.getParameterTypes();
                Annotation[][] parameterAnnotations = m.getParameterAnnotations();
                ArrayList<Parameter> parameters = new ArrayList<>(parameterTypes.length);

                for (int i = 0; i < parameterTypes.length; i++) {
                    Parameter param = op.createParameter();
                    OperationParameter opParamAnnotation = null;

                    Class<?> ptClass = parameterTypes[i];
                    Annotation[] annotations = parameterAnnotations[i];
                    for (Annotation a : annotations) {
                        if (a instanceof OperationParameter) {
                            opParamAnnotation = (OperationParameter) a;
                            break;
                        }
                    }

                    if (ptClass.isEnum()) {
                        Object[] enumValues = ptClass.getEnumConstants();
                        String[] values = new String[enumValues.length];
                        for (int j = 0; j < enumValues.length; ++j) {
                            values[j] = enumValues[j].toString();
                        }
                        param.setValues(values);
                    } else if (!isPrimitiveType(ptClass)) {
                        Field[] classFields = ptClass.getFields();
                        String[] values = new String[classFields.length];
                        for (int j = 0; j < classFields.length; ++j) {
                            StringBuilder builder = new StringBuilder(128);
                            builder.append(classFields[j].getName());
                            builder.append(":");
                            Class<?> fieldClass = classFields[j].getType();
                            if (fieldClass.isEnum()) {
                                Object[] enumConstants = fieldClass.getEnumConstants();
                                for (Object enumConstant : enumConstants) {
                                    builder.append(enumConstant.toString());
                                    builder.append(",");
                                }
                                values[j] = builder.toString();
                            } else {
                                values[j] =
                                    classFields[j].getName() + ":"
                                        + classFields[j].getType().getSimpleName();
                            }
                        }
                        param.setValues(values);
                    }

                    if (opParamAnnotation != null) {
                        param.setDescription(opParamAnnotation.description());
                        param.setRequired(opParamAnnotation.required());
                        param.setName(opParamAnnotation.name());
                    } else {
                        param.setName("");
                        param.setDescription("");
                        param.setRequired(true);
                    }
                    param.setJavaType(ptClass.getName());
                    parameters.add(param);
                }
                op.setParameters(parameters.toArray(new Parameter[parameters.size()]));
                operations.add(op);
            }
        }
    }

    /*
     * This implementation just returns an empty configuration
     * (non-Javadoc)
     * 
     * @see com.ca.apm.systemtest.fld.plugin.Plugin#getPluginConfiguration()
     */
    @Override
    public PluginConfiguration getPluginConfiguration() {
        return new EmptyPluginConfiguration();
    }

    private boolean isPrimitiveType(Class<?> klass) {
        if (klass == String.class || klass == Boolean.class || Number.class.isAssignableFrom(klass)) {
            return (true);
        }
        return (klass.isPrimitive());
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.ca.apm.systemtest.fld.plugin.Plugin#listOperations()
     */
    @ExposeMethod(description = "Returns a list of operations available on this plugin")
    public Operation[] listOperations() {
        return operations.toArray(new Operation[operations.size()]);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.ca.apm.systemtest.fld.plugin.Plugin#listAttributes()
     */
    @ExposeMethod(description = "Returns a list of attributes available on this plugin")
    public Attribute[] listAttributes() {
        return attributes.toArray(new Attribute[attributes.size()]);
    }

    protected File getTempDirFile(String tempDirName) {
        AgentConfiguration cfg =
            configurationManager.loadPluginConfiguration("agent", AgentConfiguration.class);
        if (StringUtils.isEmpty(cfg.getDefaultWorkDir())) {
            Path tempPath = Paths.get(System.getProperty("user.dir"), "temp");
            cfg.setDefaultWorkDir(tempPath.toString());
            configurationManager.savePluginConfiguration("agent", cfg);
        }

        Path tempPath = Paths.get(cfg.getDefaultWorkDir(), tempDirName);
        File tempDir = tempPath.toFile();
        return (tempDir);
    }

    @ExposeMethod(description = "Create a temporary directory under [user.dir]/temp")
    public File createTempDirectory(String tempDirName) {
        File tempDir = getTempDirFile(tempDirName);
        tempDir.mkdirs();
        return tempDir;
    }

    @ExposeMethod(description = "Create a temporary directory under [user.dir]/temp")
    public void deleteTempDirectory(String tempDirName) {
        File tempDir = getTempDirFile(tempDirName);
        deleteRecursive(tempDir);
    }

    private void deleteRecursive(File root) {
        if (root != null) {
            if (root.isDirectory()) {
                for (File f : root.listFiles()) {
                    deleteRecursive(f);
                }
            } else {
                root.delete();
            }
        }
    }

    private void logRemote(FldLevel level, String msg, Throwable ex) {
        if (fldLogger == null) {
            return;
        }
        fldLogger.log(level, pluginName + " at ", currentOperation.get(), msg, ex);
    }

    private void logRemote(FldLevel level, Throwable ex, String pattern, Object... arguments) {
        if (fldLogger == null) {
            return;
        }
        fldLogger.log(level, pluginName, currentOperation.get(),
            MessageFormat.format(pattern, arguments), ex);
    }

    /**
     * 
     * @param msg
     */
    public void debug(String msg) {
        debug(msg, (Throwable) null);
    }

    /**
     * 
     * @param pattern pattern for {@link MessageFormat#format(String, Object...)}
     * @param arguments
     */
    public void debug(String pattern, Object... arguments) {
        debug(null, pattern, arguments);
    }

    /**
     * 
     * @param ex
     * @param pattern pattern for {@link MessageFormat#format(String, Object...)}
     * @param arguments
     */
    public void debug(Throwable ex, String pattern, Object... arguments) {
        String msg = null;
        if (getLogger().isDebugEnabled()) {
            msg = messageFormat(pattern, arguments);
            if (ex != null) {
                getLogger().debug(msg, ex);
            } else {
                getLogger().debug(msg);
            }
        }
        if (msg == null) {
            logRemote(FldLevel.DEBUG, ex, pattern, arguments);
        } else {
            logRemote(FldLevel.DEBUG, msg, ex);
        }
    }

    /**
     * 
     * @param msg
     * @param ex
     */
    public void debug(String msg, Throwable ex) {
        if (getLogger().isDebugEnabled()) {
            if (ex != null) {
                getLogger().debug(msg, ex);
            } else {
                getLogger().debug(msg);
            }
        }
        logRemote(FldLevel.DEBUG, msg, ex);
    }

    /**
     * 
     * @param msg
     */
    public void info(String msg) {
        info(msg, (Throwable) null);
    }

    /**
     * 
     * @param pattern pattern for {@link MessageFormat#format(String, Object...)}
     * @param arguments
     */
    public void info(String pattern, Object... arguments) {
        info(null, pattern, arguments);
    }

    /**
     * 
     * @param ex
     * @param pattern pattern for {@link MessageFormat#format(String, Object...)}
     * @param arguments
     */
    public void info(Throwable ex, String pattern, Object... arguments) {
        String msg = null;
        if (getLogger().isInfoEnabled()) {
            msg = messageFormat(pattern, arguments);
            if (ex != null) {
                getLogger().info(msg, ex);
            } else {
                getLogger().info(msg);
            }
        }
        if (msg == null) {
            logRemote(FldLevel.INFO, ex, pattern, arguments);
        } else {
            logRemote(FldLevel.INFO, msg, ex);
        }
    }

    /**
     * 
     * @param msg
     * @param ex
     */
    public void info(String msg, Throwable ex) {
        if (getLogger().isInfoEnabled()) {
            if (ex != null) {
                getLogger().info(msg, ex);
            } else {
                getLogger().info(msg);
            }
        }
        logRemote(FldLevel.INFO, msg, ex);
    }

    /**
     * 
     * @param msg
     */
    public void warn(String msg) {
        warn(msg, (Throwable) null);
    }

    /**
     * 
     * @param pattern pattern for {@link MessageFormat#format(String, Object...)}
     * @param arguments
     */
    public void warn(String pattern, Object... arguments) {
        warn(null, pattern, arguments);
    }

    /**
     * 
     * @param ex
     * @param pattern pattern for {@link MessageFormat#format(String, Object...)}
     * @param arguments
     */
    public void warn(Throwable ex, String pattern, Object... arguments) {
        String msg = null;
        if (getLogger().isWarnEnabled()) {
            msg = messageFormat(pattern, arguments);
            if (ex != null) {
                getLogger().warn(msg, ex);
            } else {
                getLogger().warn(msg);
            }
        }
        if (msg == null) {
            logRemote(FldLevel.WARN, ex, pattern, arguments);
        } else {
            logRemote(FldLevel.WARN, msg, ex);
        }
    }

    /**
     * 
     * @param msg
     * @param ex
     */
    public void warn(String msg, Throwable ex) {
        if (getLogger().isWarnEnabled()) {
            if (ex != null) {
                getLogger().warn(msg, ex);
            } else {
                getLogger().warn(msg);
            }

        }
        logRemote(FldLevel.WARN, msg, ex);
    }

    /**
     * 
     * @param msg
     */
    public void error(String msg) {
        error(msg, (Throwable) null);
    }

    /**
     * 
     * @param pattern pattern for {@link MessageFormat#format(String, Object...)}
     * @param arguments
     */
    public void error(String pattern, Object... arguments) {
        error(null, pattern, arguments);
    }

    /**
     * 
     * @param ex
     * @param pattern pattern for {@link MessageFormat#format(String, Object...)}
     * @param arguments
     */
    public void error(Throwable ex, String pattern, Object... arguments) {
        String msg = null;
        if (getLogger().isErrorEnabled()) {
            msg = messageFormat(pattern, arguments);
            if (ex != null) {
                getLogger().error(msg, ex);
            } else {
                getLogger().error(msg);
            }
        }
        if (msg == null) {
            logRemote(FldLevel.ERROR, ex, pattern, arguments);
        } else {
            logRemote(FldLevel.ERROR, msg, ex);
        }
    }

    /**
     * 
     * @param msg
     * @param ex
     */
    public void error(String msg, Throwable ex) {
        if (getLogger().isErrorEnabled()) {
            if (ex != null) {
                getLogger().error(msg, ex);
            } else {
                getLogger().error(msg);
            }
        }
        logRemote(FldLevel.ERROR, msg, ex);
    }

    @Autowired
    public void setConfigurationManager(ConfigurationManager cm) {
        this.configurationManager = cm;
    }

    /**
     * Returns logger for this class.
     * 
     * Child classes should use {@link AbstractPluginImpl}'s (like {@link #info(String)}, etc.)
     * logging methods which not only write logs locally but also send them to the Load Orchestrator
     * with the help of {@link FldLogger}. If the child classes want their local logger to be used
     * they should override this method.
     * 
     * @return logger
     */
    protected Logger getLogger() {
        return log;
    }

    public static class EmptyPluginConfiguration implements PluginConfiguration {
    }

    private String messageFormat(String pattern, Object... arguments) {
        String msg;
        try {
            msg = MessageFormat.format(pattern, arguments);
        } catch (Exception e) {
            throw ErrorUtils.logExceptionAndWrapFmt(getLogger(), e,
                "Failed to format message using pattern \"{1}\". Exception: {0}", pattern);
        }
        return msg;
    }
}
