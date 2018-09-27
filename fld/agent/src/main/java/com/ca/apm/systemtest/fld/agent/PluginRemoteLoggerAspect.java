/**
 * 
 */
package com.ca.apm.systemtest.fld.agent;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

import com.ca.apm.systemtest.fld.plugin.AbstractPluginImpl;

/**
 * Aspect and advice to automatically set the operation in a thread local for remote logging purposes
 * @author KEYJA01
 *
 */
@Aspect
@Component
public class PluginRemoteLoggerAspect {
    
    public PluginRemoteLoggerAspect() {
    }
    
    @Pointcut("target(com.ca.apm.systemtest.fld.plugin.AbstractPluginImpl) && @target(com.ca.apm.systemtest.fld.common.PluginAnnotationComponent) && execution(public * *(..))")
    private void operationAsTag() {
    }
    
    
    @Around("operationAsTag()")
    public Object doSetOperationAsTag(ProceedingJoinPoint pjp) throws Throwable {
        Signature sig = pjp.getSignature();
        String name = sig.getName();
        if (name != null) {
            AbstractPluginImpl.currentOperation.set(name);
        }
        try {
            
            Object obj = pjp.proceed();
            
            return obj;
        } finally {
            AbstractPluginImpl.currentOperation.set(null);
        }
    }
}
