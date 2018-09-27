package com.ca.apm.systemtest.fld.common.spel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.PropertyPlaceholderHelper;

import static org.springframework.util.PropertyPlaceholderHelper.PlaceholderResolver;

/**
 * This class holds together the necessary pieces to parse and resolve multiple SpEL expressions
 * inside a string.
 * Created by haiva01 on 19.6.2015.
 */
public class StringEvaluator {
    private static final Logger log = LoggerFactory.getLogger(StringEvaluator.class);

    private final PropertyPlaceholderHelper resolverHelper = new PropertyPlaceholderHelper("${",
        "}");
    private final ConfigurationPlaceholderResolver resolver;


    public StringEvaluator(ConfigurationPlaceholderResolver resolver) {
        this.resolver = resolver;
    }

    /**
     * Evaluate SpEL expressions in given string.
     *
     * @param str string to evaluate
     * @return evaluated string
     */
    public String evaluateString(String str) {
        return evaluateString(str, resolver);
    }

    /**
     * Evaluate SpEL expressions in given string.
     *
     * @param str      string to evaluate
     * @param resolver resolver to use for variables resolution
     * @return evaluated string
     */
    public String evaluateString(String str, PlaceholderResolver resolver) {
        if (log.isDebugEnabled()) {
            log.debug("Evaluating {}", str);
        }

        String result = resolverHelper.replacePlaceholders(str, this.resolver);
        if (log.isDebugEnabled()) {
            log.debug("Evaluated to {}", result);
        }

        return result;
    }


    public ConfigurationPlaceholderResolver getResolver() {
        return resolver;
    }
}
