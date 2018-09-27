package com.ca.apm.systemtest.fld.plugin.file.transformation.config;

import java.util.List;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlType;

/**
 * Created by haiva01 on 16.6.2015.
 */
@XmlType(name = "files",
    namespace = "com.ca.apm.systemtest.fld.plugin.file.transformation")
public class Files {

    private String id;
    private List<String> files;

    public Files() {
    }

    public String getId() {
        return id;
    }

    @XmlAttribute(name = "id", required = true)
    public void setId(String id) {
        this.id = id;
    }

    public List<String> getFiles() {
        return files;
    }

    @XmlElements(value = {@XmlElement(name = "file", type = String.class)})
    public void setFiles(List<String> files) {
        this.files = files;
    }

    @Override
    public String toString() {
        return "Files{"
            + "id='" + id + '\''
            + ", files=" + files + '}';
    }
}
