package com.ca.apm.systemtest.fld.plugin.file.transformation.config.transformation;

import com.ca.apm.systemtest.fld.common.spel.StringEvaluator;

/**
 * Instance of this class is passed to {#link TransformationBase#apply(String,
 * TransformationContext)}.
 * Created by haiva01 on 18.6.2015.
 */
public class TransformationContext {
    private final StringEvaluator stringEvaluator;

    public TransformationContext(StringEvaluator stringEvaluator) {
        this.stringEvaluator = stringEvaluator;
    }

    public StringEvaluator getStringEvaluator() {
        return stringEvaluator;
    }
}
