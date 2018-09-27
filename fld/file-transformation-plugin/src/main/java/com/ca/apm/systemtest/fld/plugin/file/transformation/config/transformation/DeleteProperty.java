package com.ca.apm.systemtest.fld.plugin.file.transformation.config.transformation;

import java.io.File;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;

/**
 * This transformation deletes a property.
 * Created by haiva01 on 15.6.2015.
 */
@XmlType(name = "delete-property",
    namespace = "com.ca.apm.systemtest.fld.plugin.file.transformation")
public class DeleteProperty extends TransformationBase {
    String name;

    public DeleteProperty() {
    }

    public String getName() {
        return name;
    }

    @XmlAttribute
    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return "DeleteProperty{"
            + "name='" + name + '\'' + '}';
    }

    @Override
    public void apply(String file, TransformationContext context) throws ConfigurationException {
        PropertiesConfiguration propertiesConfiguration = new PropertiesConfiguration();
        propertiesConfiguration.setFile(new File(file));
        propertiesConfiguration.load();
        propertiesConfiguration.clearProperty(name);
        propertiesConfiguration.setFooter(propertiesConfiguration.getFooter()
            + '\n' + toString());
        propertiesConfiguration.save();
    }
}
