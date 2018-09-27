package com.ca.apm.systemtest.fld.common.spel;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.expression.BeanResolver;
import org.springframework.expression.ConstructorResolver;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.MethodResolver;
import org.springframework.expression.OperatorOverloader;
import org.springframework.expression.PropertyAccessor;
import org.springframework.expression.TypeComparator;
import org.springframework.expression.TypeConverter;
import org.springframework.expression.TypeLocator;
import org.springframework.expression.TypedValue;

/**
 * Context that allows to define local variables and delegates to higher scope context.
 * Created by haiva01 on 25.6.2015.
 */
public class LocalEvaluationContext implements EvaluationContext {
    EvaluationContext higherScopeContext;
    Map<String, Object> localVariables = new HashMap<>(10);

    public LocalEvaluationContext(EvaluationContext higherScopeContext) {
        this.higherScopeContext = higherScopeContext;
    }

    /**
     * Return the default root context object against which unqualified
     * properties/methods/etc should be resolved. This can be overridden
     * when evaluating an expression.
     */
    @Override
    public TypedValue getRootObject() {
        return higherScopeContext.getRootObject();
    }

    /**
     * Return a list of resolvers that will be asked in turn to locate a constructor.
     */
    @Override
    public List<ConstructorResolver> getConstructorResolvers() {
        return higherScopeContext.getConstructorResolvers();
    }

    /**
     * Return a list of resolvers that will be asked in turn to locate a method.
     */
    @Override
    public List<MethodResolver> getMethodResolvers() {
        return higherScopeContext.getMethodResolvers();
    }

    /**
     * Return a list of accessors that will be asked in turn to read/write a property.
     */
    @Override
    public List<PropertyAccessor> getPropertyAccessors() {
        return higherScopeContext.getPropertyAccessors();
    }

    /**
     * Return a type locator that can be used to find types, either by short or
     * fully qualified name.
     */
    @Override
    public TypeLocator getTypeLocator() {
        return higherScopeContext.getTypeLocator();
    }

    /**
     * Return a type converter that can convert (or coerce) a value from one type to another.
     */
    @Override
    public TypeConverter getTypeConverter() {
        return higherScopeContext.getTypeConverter();
    }

    /**
     * Return a type comparator for comparing pairs of objects for equality.
     */
    @Override
    public TypeComparator getTypeComparator() {
        return higherScopeContext.getTypeComparator();
    }

    /**
     * Return an operator overloader that may support mathematical operations
     * between more than the standard set of types.
     */
    @Override
    public OperatorOverloader getOperatorOverloader() {
        return higherScopeContext.getOperatorOverloader();
    }

    /**
     * Return a bean resolver that can look up beans by name.
     */
    @Override
    public BeanResolver getBeanResolver() {
        return higherScopeContext.getBeanResolver();
    }

    /**
     * Set a named variable within this evaluation context to a specified value.
     *
     * @param name  variable to set
     * @param value value to be placed in the variable
     */
    @Override
    public void setVariable(String name, Object value) {
        if (localVariables.containsKey(name)) {
            localVariables.put(name, value);
        } else {
            higherScopeContext.setVariable(name, value);
        }
    }


    /**
     * Set local variable.
     *
     * @param name  variable name
     * @param value variable value
     */
    public void setLocalVariable(String name, Object value) {
        localVariables.put(name, value);
    }


    /**
     * Look up a named variable within this evaluation context.
     *
     * @param name variable to lookup
     * @return the value of the variable
     */
    @Override
    public Object lookupVariable(String name) {
        if (localVariables.containsKey(name)) {
            return localVariables.get(name);
        } else {
            return higherScopeContext.lookupVariable(name);
        }
    }
}
