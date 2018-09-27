/**
 * 
 */
package com.ca.apm.systemtest.fld.plugin.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author keyja01
 *
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.PARAMETER)
public @interface OperationParameter {
	/**
	 * Describes the service parameter. Should include information on validation, required 
	 * @return
	 */
	public String description();
	
	/**
	 * Returns true if the service parameter is required.  Otherwise, if it is not a primitive type,
	 * the service parameter's value may be null
	 * @return
	 */
	public boolean required() default false;
	
	/**
	 * Returns the name of the parameter - can be used to display a user-friendly name for the parameter in the FLD UI
	 * @return
	 */
	public String name();
}
