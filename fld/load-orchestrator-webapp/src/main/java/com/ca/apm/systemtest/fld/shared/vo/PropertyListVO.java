package com.ca.apm.systemtest.fld.shared.vo;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

/**
 * Transfer object to get property localizations by keys. 
 * 
 * @author sinal04
 *
 */
@JsonInclude(Include.NON_NULL)
public class PropertyListVO {
    
    private List<String> propertyNames;

    /**
     * Gets the property keys.
     * 
     * @return the propertyNames
     */
    public List<String> getPropertyNames() {
        return propertyNames;
    }

    /**
     * Sets the property keys.
     * 
     * @param propertyNames the propertyNames to set
     */
    public void setPropertyNames(List<String> propertyNames) {
        this.propertyNames = propertyNames;
    }
    
    
}
