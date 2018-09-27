package com.ca.apm.systemtest.fld.plugin.file.transformation.config.transformation;

import java.io.File;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;

import com.ca.apm.systemtest.fld.common.spel.ConfigurationPlaceholderResolver;
import com.ca.apm.systemtest.fld.common.spel.LocalEvaluationContext;
import com.ca.apm.systemtest.fld.common.spel.StringEvaluator;

/**
 * This transformation sets given property to given value.
 * Values are evaluated for SpEL expressions. In SpEL expressions <code>#_</code> refers to
 * original value.
 * Created by haiva01 on 15.6.2015.
 */
@XmlType(name = "set-property",
    namespace = "com.ca.apm.systemtest.fld.plugin.file.transformation")
public class SetProperty extends TransformationBase {

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
        return "SetProperty{"
            + "name='" + name + '\''
            + ", value='" + value + '\'' + '}';
    }

    @Override
    public void apply(String file, TransformationContext context) throws ConfigurationException {
        final PropertiesConfiguration propertiesConfiguration = new PropertiesConfiguration();
        // Set list delimiter to NUL to effectively turn off splitting of property values to lists.
        propertiesConfiguration.setListDelimiter('\000');
        propertiesConfiguration.setFile(new File(file));
        propertiesConfiguration.load(new File(file));

        StringEvaluator stringEvaluator = context.getStringEvaluator();
        ConfigurationPlaceholderResolver resolver = stringEvaluator.getResolver();
        LocalEvaluationContext localEvaluationContext = new LocalEvaluationContext(
            resolver.getContext());
        localEvaluationContext.setLocalVariable("_", propertiesConfiguration.getProperty(name));

        String evaluatedValue;
        try {
            resolver.pushContext(localEvaluationContext);
            evaluatedValue = stringEvaluator.evaluateString(value);
        } finally {
            resolver.popContext();
        }

        propertiesConfiguration.setProperty(name, evaluatedValue);
        propertiesConfiguration.setFooter(propertiesConfiguration.getFooter()
            + '\n' + toString());
        propertiesConfiguration.save();
    }
}
