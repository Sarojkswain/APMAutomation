package com.ca.apm.systemtest.fld.shared.vo;

/**
 * Transfer object to specify property names.
 *  
 * @author sinal04
 *
 */
public class PropertyValueVO {
    private Long id;

    private String name;

    private String value;

    private String propertiesFile;

    public PropertyValueVO() {
    }

    public Long getId() {
        return (id);
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return (name);
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getValue() {
        return (value);
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getPropertiesFile() {
        return (propertiesFile);
    }

    public void setPropertiesFile(String propertiesFile) {
        this.propertiesFile = propertiesFile;
    }

}
