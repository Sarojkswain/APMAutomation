package com.ca.apm.systemtest.fld.plugin.file.transformation.config;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by haiva01 on 15.6.2015.
 */
@XmlRootElement(name = "configuration")
@XmlType(propOrder = {"transformations", "files", "bindings"})
public class Configuration {
    public static final Logger log = LoggerFactory.getLogger(Configuration.class);

    private List<Transformation> transformations;
    private List<Files> files;
    private List<Binding> bindings;

    public Configuration() {
    }

    public List<Transformation> getTransformations() {
        return transformations;
    }

    @XmlElement(name = "transformation")
    public void setTransformations(
        List<Transformation> transformations) {
        this.transformations = transformations;
    }

    public List<Files> getFiles() {
        return files;
    }

    @XmlElement(name = "files")
    public void setFiles(List<Files> files) {
        this.files = files;
    }

    public List<Binding> getBindings() {
        return bindings;
    }

    @XmlElement(name = "binding")
    public void setBindings(
        List<Binding> bindings) {
        this.bindings = bindings;
    }

    @Override
    public String toString() {
        return "Configuration{"
            + "transformations=" + transformations
            + ", files=" + files
            + ", bindings=" + bindings + '}';
    }

    @XmlTransient
    public Map<String, Transformation> getTransformationsMap() {
        List<Transformation> transformations = getTransformations();
        Map<String, Transformation> transformationsMap = new TreeMap<>();
        for (Transformation trans : transformations) {
            transformationsMap.put(trans.getId(), trans);
        }
        return transformationsMap;
    }

    @XmlTransient
    public Map<String, Files> getFilesMap() {
        List<Files> files = getFiles();
        Map<String, Files> filesMap = new TreeMap<>();
        for (Files f : files) {
            filesMap.put(f.getId(), f);
        }
        return filesMap;
    }
}
