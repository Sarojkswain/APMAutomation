/**
 * 
 */
package com.ca.apm.systemtest.fld.flow;

import com.ca.tas.builder.TasBuilder;

/**
 * @author keyja01
 *
 */
public interface IGenericBuilder<U> extends TasBuilder<U> {
    public U build();
}
