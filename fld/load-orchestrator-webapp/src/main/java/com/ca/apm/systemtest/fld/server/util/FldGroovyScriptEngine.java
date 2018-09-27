package com.ca.apm.systemtest.fld.server.util;

import groovy.lang.GroovyClassLoader;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.UUID;

import javax.script.ScriptContext;
import javax.script.ScriptException;

import org.activiti.engine.delegate.BpmnError;
import org.codehaus.groovy.jsr223.GroovyScriptEngineImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ca.apm.systemtest.fld.common.ErrorUtils;

/**
 * This class tweaks Groovy engine behaviour so that we can handle errors in Groovy scripts
 * that are executed by Activiti.
 * Created by haiva01 on 22.5.2015.
 */
public class FldGroovyScriptEngine extends GroovyScriptEngineImpl {
    Logger log = LoggerFactory.getLogger(FldGroovyScriptEngine.class);

    public FldGroovyScriptEngine() {
        super();
    }

    public FldGroovyScriptEngine(GroovyClassLoader classLoader) {
        super(classLoader);
    }

    @Override
    public Object eval(String script, ScriptContext ctx) throws ScriptException {
        try {
            return super.eval(script, ctx);
        } catch (ScriptException e) {
            final String uuid = UUID.randomUUID().toString();
            ErrorUtils.logExceptionFmt(log, e, "Exception while evaluating Groovy script: {0}."
                + " Exception correlation ID is {1}", uuid);
            if (log.isTraceEnabled()) {
                log.trace("Script:\n{}", script);
            }
            final Throwable cause = e.getCause();
            if (cause != null) {
                if (cause instanceof ScriptException
                    && cause.getCause() instanceof BpmnError) {
                    BpmnError bpmnError = (BpmnError) cause.getCause();

                    // BpmnError does not let us modify the errorCode field, so we hack around it.
                    try {
                        Method setErrorCode
                            = bpmnError.getClass().getDeclaredMethod("setErrorCode", String.class);
                        setErrorCode.setAccessible(true);
                        String newErrorCode = bpmnError.getErrorCode()
                            + ", exception correlation ID " + uuid;
                        setErrorCode.invoke(bpmnError, newErrorCode);
                    } catch (NoSuchMethodException | IllegalAccessException
                        | InvocationTargetException e1) {
                        ErrorUtils.logExceptionFmt(log, e1,
                            "Failed set BpmnError's error code through reflection: {0}");
                    }

                    if (bpmnError.getCause() != null) {
                        Throwable bpmnErrorCause = bpmnError.getCause();
                        bpmnError.addSuppressed(bpmnErrorCause);
                        bpmnError.initCause(null);
                    }
                    // Rethrow.
                    throw e;
                } else if (cause instanceof ScriptException) {
                    // Wrap this into BpmnError.
                    final Throwable realCause = cause.getCause();
                    final BpmnError bpmnError = new BpmnError(realCause.getClass().getName(),
                        realCause.getMessage() + ", exception correlation ID " + uuid);
                    bpmnError.addSuppressed(e);
                    // And return this burrito doubly wrapped so that higher catch can recognize it.
                    throw new ScriptException(new ScriptException(bpmnError));
                }
            }

            // This could be a script syntax error.
            throw e;
        }
    }
}
