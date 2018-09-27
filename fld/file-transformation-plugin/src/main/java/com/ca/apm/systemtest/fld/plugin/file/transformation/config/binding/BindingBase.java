package com.ca.apm.systemtest.fld.plugin.file.transformation.config.binding;

import javax.xml.bind.annotation.XmlAttribute;

/**
 * Created by haiva01 on 16.6.2015.
 */
public class BindingBase {
    private String id;

    public BindingBase() {
    }

    public String getId() {
        return id;
    }

    @XmlAttribute(name = "id")
    public void setId(String id) {
        this.id = id;
    }
}
