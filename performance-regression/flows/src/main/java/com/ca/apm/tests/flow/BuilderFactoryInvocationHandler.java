/**
 * 
 */
package com.ca.apm.tests.flow;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.HashSet;

/**
 * @author keyja01
 *
 */
public class BuilderFactoryInvocationHandler<U, B extends IGenericBuilder<U>> implements InvocationHandler {
    private Class<B> builderInterface;
    private Class<U> targetClass;
    private U target;
    private HashSet<Method> methods;

    public BuilderFactoryInvocationHandler(Class<B> builderInterface, Class<U> targetClass) throws 
            InstantiationException, IllegalAccessException {
        this.builderInterface = builderInterface;
        this.targetClass = targetClass;
        this.target = createTargetInstance();
        
         methods = new HashSet<>();
        // find all methods that match the signature "public B fieldName(? fieldName)"
        for (Method m: builderInterface.getMethods()) {
            Class<?> returnType = m.getReturnType();
            if (!returnType.equals(builderInterface)) {
                continue;
            }
            if (m.getParameterTypes().length != 1) {
                continue;
            }
            methods.add(m);
        }
    }
    
    
    protected U createTargetInstance() throws InstantiationException, IllegalAccessException {
        return targetClass.newInstance();
    }
    

    /* (non-Javadoc)
     * @see java.lang.reflect.InvocationHandler#invoke(java.lang.Object, java.lang.reflect.Method, java.lang.Object[])
     */
    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        if (isBuild(method)) {
            return target;
        }
        
        if (methods.contains(method)) {
            // we need to copy the value of the 1st (and only argument) to
            // the target by reflection
            copyPropertiesToTarget(args[0], method.getName(), target);
            return proxy;
        }
        
        throw new IllegalArgumentException("Unsupported method signature " + method.getName() 
            + " in " + this.builderInterface);
    }

    
    private void copyPropertiesToTarget(Object value, String fieldName, U target) throws 
            NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
        Field field = targetClass.getDeclaredField(fieldName);
        boolean accessible = field.isAccessible();
        field.setAccessible(true);
        field.set(target, value);
        field.setAccessible(accessible);
    }

    private boolean isBuild(Method m) {
        return m != null 
            && m.getName().equals("build") 
            && m.getParameterTypes().length == 0  
            && m.getReturnType().isAssignableFrom(targetClass);
    }
}
