package com.ca.apm.systemtest.fld.plugin.file.transformation.config.transformation;

import java.io.File;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.lang3.StringUtils;

/**
 * This transformation appends to property's value.
 * Created by haiva01 on 24.6.2015.
 */
@XmlType(name = "append-property",
    namespace = "com.ca.apm.systemtest.fld.plugin.file.transformation")
public class AppendProperty extends TransformationBase {

    private String name;
    private String value;

    public String getName() {
        return name;
    }

    @XmlAttribute
    public void setName(String name) {
        this.name = name;
    }

    public String getValue() {
        return value;
    }

    @XmlAttribute
    public void setValue(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return "AppendProperty{"
            + "name='" + name + '\''
            + ", value='" + value + '\'' + '}';
    }

    @Override
    public void apply(String file, TransformationContext context) throws ConfigurationException {
        final PropertiesConfiguration propertiesConfiguration = new PropertiesConfiguration();
        propertiesConfiguration.setFile(new File(file));
        propertiesConfiguration.load();
        final String evaluatedValue = context.getStringEvaluator().evaluateString(value);
        final String newValue = StringUtils.defaultString(
            propertiesConfiguration.getProperty(name) + evaluatedValue);
        propertiesConfiguration.setProperty(name, newValue);
        propertiesConfiguration.setFooter(propertiesConfiguration.getFooter()
            + '\n' + toString());
        propertiesConfiguration.save();
    }
}
