package com.ca.apm.systemtest.fld.plugin.file.transformation.config.binding;

import javax.xml.bind.annotation.XmlType;

/**
 * Created by haiva01 on 16.6.2015.
 */
@XmlType(name = "files-ref",
    namespace = "com.ca.apm.systemtest.fld.plugin.file.transformation")
public class FilesRef extends BindingBase {
    public FilesRef() {
    }

    @Override
    public String toString() {
        return "FilesRef{"
            + "id='" + getId() + '\'' + '}';
    }
}
