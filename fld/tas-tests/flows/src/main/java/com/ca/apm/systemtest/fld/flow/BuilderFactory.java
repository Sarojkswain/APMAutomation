/**
 * 
 */
package com.ca.apm.systemtest.fld.flow;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;

/**
 * Attempt to reduce boiler-plate code for simple builders
 * @author keyja01
 *
 */
public class BuilderFactory<U, B extends IGenericBuilder<U>> {
    /**
     * Creates a proxy that implements the methods of the builder interface, and which builds
     * the target instance, setting the fields using simple reflection.
     * @return
     */
    @SuppressWarnings("unchecked")
    public B newBuilder(Class<U> targetClass, Class<B> builderInterface) {
        B builder;
        
        InvocationHandler handler;
        try {
            handler = createInvocationHandler(targetClass, builderInterface);
        } catch (InstantiationException | IllegalAccessException e) {
            throw new RuntimeException("Unable to create builder factory", e);
        }
        builder = (B) Proxy.newProxyInstance(this.getClass().getClassLoader(), new Class[] {builderInterface}, handler);
        
        
        return builder;
    }
    
    
    protected InvocationHandler createInvocationHandler(Class<U> targetClass, Class<B> builderInterface) throws InstantiationException, IllegalAccessException {
        InvocationHandler h = new BuilderFactoryInvocationHandler<>(builderInterface, targetClass);
        return h;
    }
}
