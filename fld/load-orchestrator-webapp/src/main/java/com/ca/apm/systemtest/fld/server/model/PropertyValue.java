package com.ca.apm.systemtest.fld.server.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * @author ZUNPA01
 *
 */
@Entity
@Table(name="properties")
public class PropertyValue {

    @Id
    @GeneratedValue(strategy=GenerationType.AUTO)
    @Column(name="property_id", nullable=false)
    private Long id;

    @Column(name="name", nullable=false)
    private String name;

    @Column(name="value", nullable=false)
    private String value;

    @Column(name="properties_file", nullable=false)
    private String propertiesFile;

    public PropertyValue() {
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
