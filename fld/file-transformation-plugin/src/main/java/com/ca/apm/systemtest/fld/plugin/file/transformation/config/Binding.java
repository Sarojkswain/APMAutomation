package com.ca.apm.systemtest.fld.plugin.file.transformation.config;

import java.util.List;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

import com.ca.apm.systemtest.fld.plugin.file.transformation.config.binding.FilesRef;
import com.ca.apm.systemtest.fld.plugin.file.transformation.config.binding.TransformationRef;

/**
 * Created by haiva01 on 16.6.2015.
 */
@XmlType(name = "binding", namespace = "com.ca.apm.systemtest.fld.plugin.file.transformation",
    propOrder = {"id", "transformationRef", "filesRef"})
public class Binding {
    private String id;
    private List<TransformationRef> transformations;
    private List<FilesRef> files;

    public Binding() {
    }

    public String getId() {
        return id;
    }

    @XmlAttribute(name = "id", required = false)
    public void setId(String id) {
        this.id = id;
    }

    public List<TransformationRef> getTransformationRef() {
        return transformations;
    }

    @XmlElement(name = "transformation-ref")
    public void setTransformationRef(
        List<TransformationRef> transformations) {
        this.transformations = transformations;
    }

    public List<FilesRef> getFilesRef() {
        return files;
    }

    @XmlElement(name = "files-ref")
    public void setFilesRef(
        List<FilesRef> files) {
        this.files = files;
    }

    @Override
    public String toString() {
        return "Binding{"
            + "id='" + id + '\''
            + ", transformations=" + transformations
            + ", files=" + files + '}';
    }
}
