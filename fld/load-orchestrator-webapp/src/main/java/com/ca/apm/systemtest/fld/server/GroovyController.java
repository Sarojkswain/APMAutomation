package com.ca.apm.systemtest.fld.server;

import groovy.lang.Binding;
import groovy.lang.Closure;
import groovy.lang.GroovyShell;
import groovy.ui.SystemOutputInterceptor;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.ca.apm.systemtest.fld.proxy.AgentProxyFactory;

/**
 * Remote Groovy execution controller.
 * @author ZUNPA01
 *
 */
@Controller
public class GroovyController {
    private Logger log = LoggerFactory.getLogger(GroovyController.class);

    @Autowired
    private ApplicationContext applicationContext;
    @Autowired
    private AgentProxyFactory agentProxyFactory;

    public GroovyController() {
    }

    @ResponseBody
    @RequestMapping(value="/runGroovyScript", method=RequestMethod.POST)
    @Transactional(propagation = Propagation.REQUIRED, readOnly = false)
    public Map<String, String> runGroovyScript(@RequestParam(required = true, value = "script") String script) {
        Map<String, String> resultMap;
        try {
            resultMap = eval(script);
        } catch (Throwable t) {
            System.out.println(t);
            resultMap = new HashMap<String, String>();
            resultMap.put("error", t.getMessage());
        }
        return (resultMap);
    }

    @Transactional(propagation = Propagation.REQUIRED, readOnly = false)
    protected Map<String, String> eval(String script) {
        Map<String, String> resultMap = new HashMap<String, String>();
        resultMap.put("script", script);
        resultMap.put("startTime", Calendar.getInstance().getTime().toString());

        SystemOutputInterceptorClosure outputCollector = new SystemOutputInterceptorClosure(null);
        SystemOutputInterceptor systemOutputInterceptor = new SystemOutputInterceptor(outputCollector);
        systemOutputInterceptor.start();

        Binding binding = new Binding();
        binding.setVariable("ctx", applicationContext);
        binding.setVariable("agentProxyFactory", agentProxyFactory);
        GroovyShell shell = new GroovyShell(this.getClass().getClassLoader(), binding);

        try {
            Object result = shell.evaluate(script);
            resultMap.put("result", result != null ? result.toString() : "null");
        } catch (Throwable t) {
            StringBuffer sb = new StringBuffer(1024);
            final String msg = MessageFormat.format("Error running script: {0}", t.getMessage());
            sb.append(t.getClass().getName());
            sb.append(": ");
            String causeMessage = t.getMessage();
            if (causeMessage != null) {
                sb.append(causeMessage);
            }
            log.error(msg, t);
            log.debug("Script:\n{}", script);
            for (Throwable cause = t.getCause(), prevCause = null;
                cause != null && cause != prevCause;
                prevCause = cause, cause = cause.getCause()) {
                sb.append('\n');
                sb.append(cause.getClass().getName());
                sb.append(": ");
                causeMessage = cause.getMessage();
                if (causeMessage != null) {
                    sb.append(causeMessage);
                }
                log.debug("Caused by...", cause);
            }
            resultMap.put("error", sb.toString());
        } finally {
            systemOutputInterceptor.stop();
            try {
                systemOutputInterceptor.close();
            } catch (IOException e) {
                log.error("IOException", e);
            }
        }

        resultMap.put("output", outputCollector.getStringBuffer().toString().trim());
        resultMap.put("endTime", Calendar.getInstance().getTime().toString());

        return (resultMap);
    }

    @SuppressWarnings({ "serial", "rawtypes" })
    private static class SystemOutputInterceptorClosure extends Closure {

        StringBuffer stringBuffer = new StringBuffer();

        public SystemOutputInterceptorClosure(Object owner) {
            super(owner) ;
        }

        @Override
        public Object call(Object params) {
            stringBuffer.append(params);
            return (false);
        }

        @Override
        public Object call(Object... args) {
            stringBuffer.append(args);
            return (false);
        }

        public StringBuffer getStringBuffer() {
            return (stringBuffer);
        }
    }

}
