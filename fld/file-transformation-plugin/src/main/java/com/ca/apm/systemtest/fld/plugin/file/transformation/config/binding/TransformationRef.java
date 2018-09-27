package com.ca.apm.systemtest.fld.plugin.file.transformation.config.binding;

import javax.xml.bind.annotation.XmlType;

/**
 * Created by haiva01 on 16.6.2015.
 */
@XmlType(name = "transformation-ref",
    namespace = "com.ca.apm.systemtest.fld.plugin.file.transformation")
public class TransformationRef extends BindingBase {
    public TransformationRef() {
    }

    @Override
    public String toString() {
        return "TransformationRef{"
            + "id='" + getId() + '\'' + '}';
    }
}
