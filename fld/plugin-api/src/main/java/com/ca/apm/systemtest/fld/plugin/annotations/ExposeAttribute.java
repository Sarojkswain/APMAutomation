/**
 * 
 */
package com.ca.apm.systemtest.fld.plugin.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a field as eligible for read/write access via the plugin interface.
 * @author keyja01
 *
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface ExposeAttribute {
	/**
	 * Description of the attribute.  Since JavaDocs are not present in the bytecode, the description
	 * should be provide in an annotation
	 * @return
	 */
	public String description();
	
	/**
	 * If true, the attribute is read-only.
	 * @return
	 */
	public boolean readOnly() default true;
}
