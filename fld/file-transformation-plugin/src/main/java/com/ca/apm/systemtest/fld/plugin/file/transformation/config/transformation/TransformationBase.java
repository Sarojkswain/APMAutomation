package com.ca.apm.systemtest.fld.plugin.file.transformation.config.transformation;

/**
 * Base class for all transformations.
 * Created by haiva01 on 15.6.2015.
 */
public abstract class TransformationBase {
    public abstract void apply(String file, TransformationContext context) throws Exception;
}
