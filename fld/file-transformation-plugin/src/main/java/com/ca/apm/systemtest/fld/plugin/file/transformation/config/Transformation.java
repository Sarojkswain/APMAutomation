package com.ca.apm.systemtest.fld.plugin.file.transformation.config;

import java.util.List;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ca.apm.systemtest.fld.plugin.file.transformation.config.transformation.AppendProperty;
import com.ca.apm.systemtest.fld.plugin.file.transformation.config.transformation.DeleteProperty;
import com.ca.apm.systemtest.fld.plugin.file.transformation.config.transformation.SetProperty;
import com.ca.apm.systemtest.fld.plugin.file.transformation.config.transformation
    .TransformationBase;
import com.ca.apm.systemtest.fld.plugin.file.transformation.config.transformation.XsltTransform;

/**
 * Created by haiva01 on 15.6.2015.
 */
@XmlType(name = "transformation",
    namespace = "com.ca.apm.systemtest.fld.plugin.file.transformation")
public class Transformation {
    public static final Logger log = LoggerFactory.getLogger(Transformation.class);

    private String id;

    private List<TransformationBase> transformations;

    public Transformation() {
    }

    public List<TransformationBase> getTransformations() {
        return transformations;
    }

    @XmlElements(
        value = {
            @XmlElement(name = "set-property", type = SetProperty.class),
            @XmlElement(name = "delete-property", type = DeleteProperty.class),
            @XmlElement(name = "append-property", type = AppendProperty.class),
            @XmlElement(name = "xslt-transform", type = XsltTransform.class)
        })
    public void setTransformations(
        List<TransformationBase> transformations) {
        this.transformations = transformations;
    }

    public String getId() {
        return id;
    }

    @XmlAttribute(name = "id", required = true)
    public void setId(String id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return "Transformation{"
            + "id='" + id + '\''
            + ", transformations=" + transformations + '}';
    }
}
