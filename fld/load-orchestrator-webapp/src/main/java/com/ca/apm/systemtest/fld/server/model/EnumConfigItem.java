package com.ca.apm.systemtest.fld.server.model;

import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

/**
 * Custom config item type for enum values which also puts possible enum values into DB. 
 * 
 * @author sinal04
 *
 */
@Entity
@DiscriminatorValue("enm")
public class EnumConfigItem extends CustomConfigItem {
    
    /**
     * Enum values in form of "{ val1, val2, ..., valN}".
     */
    @Column(name="enum_values", nullable = true, length = 10240)
    private String enumValues;
    
    /**
     * Default constructor.
     */
    public EnumConfigItem() {
        
    }
    
    /**
     * Constructor.
     * 
     * @param name
     * @param formId
     * @param value
     * @param enumValues
     */
    public EnumConfigItem(String name, String formId, String value, String enumValues) {
        super(name, formId, value, "enum");
        this.enumValues = enumValues;
    }

    /**
     * @return the enumValues
     */
    public String getEnumValues() {
        return enumValues;
    }

    /**
     * @param enumValues the enumValues to set
     */
    public void setEnumValues(String enumValues) {
        this.enumValues = enumValues;
    }


    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "EnumConfigItem [enumValues=" + enumValues + ", super=" + super.toString()
            + "]";
    }

    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!super.equals(obj)) {
            return false;
        }
        
        EnumConfigItem other = (EnumConfigItem) obj;
        return areEnumValuesEqual(other);
    }

    @Override
    public boolean equalsIgnoringValue(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!super.equalsIgnoringValue(obj)) {
            return false;
        }
        
        EnumConfigItem other = (EnumConfigItem) obj;
        return areEnumValuesEqual(other);
    }
    
    private boolean areEnumValuesEqual(EnumConfigItem other) {
        if (enumValues == null) {
            return other.enumValues == null;
        } 
        return enumValues.equals(other.enumValues);
    }
    
}
