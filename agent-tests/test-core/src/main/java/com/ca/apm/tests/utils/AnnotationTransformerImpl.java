package com.ca.apm.tests.utils;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.HashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.IAnnotationTransformer;
import org.testng.annotations.ITestAnnotation;

/**
 * Setting priority to be able to run all methods in the same class in order.
 * Have to be done due to known Testng issue: https://github.com/cbeust/testng/issues/106
 *
 * @author kurma05
 */
public class AnnotationTransformerImpl implements IAnnotationTransformer {
    
    private int priorityCounter = 0;
    private HashMap<Class<?>, Integer> priorityMap = new HashMap<Class<?>, Integer>();
    private static final Logger LOGGER = LoggerFactory.getLogger(AnnotationTransformerImpl.class);
  
    public void transform(ITestAnnotation annotation, Class testClass, 
                          Constructor testConstructor, Method testMethod) {
      
      Integer priority = null;
      
      if(testMethod != null) {
          Class<?> declaringClass = testMethod.getDeclaringClass();
          priority = priorityMap.get(declaringClass);
          if (priority == null) {
            priority = ++priorityCounter;
            priorityMap.put(declaringClass, priority);
          }
          annotation.setPriority(priority);
      }
      LOGGER.debug("Setting priority {} for {}", priority, testMethod);      
    }    
}
