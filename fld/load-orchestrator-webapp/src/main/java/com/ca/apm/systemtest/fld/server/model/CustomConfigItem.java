package com.ca.apm.systemtest.fld.server.model;

import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

/**
 * Encapsulates a custom form type
 * @author KEYJA01
 *
 */
@Entity
@DiscriminatorValue("cst")
public class CustomConfigItem extends StringConfigItem {

    @Column(name="custom_item_type", nullable=true, length=128)
    private String customItemType;

    public CustomConfigItem() {
    }

    /**
     * @param name the name of the item
     * @param formId the id of the item in the form
     * @param stringValue the value currently assigned to the value
     * @param customItemType the name of the custom item type
     */
    public CustomConfigItem(String name, String formId, String stringValue, String customItemType) {
        super(name, formId, stringValue);
        this.customItemType = customItemType;
    }

    public String getCustomItemType() {
        return customItemType;
    }

    public void setCustomItemType(String customItemType) {
        this.customItemType = customItemType;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "CustomConfigItem [customItemType=" + customItemType + ", super="
            + super.toString() + "]";
    }
    
    
}
