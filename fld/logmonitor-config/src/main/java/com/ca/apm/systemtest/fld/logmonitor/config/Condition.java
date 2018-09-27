/**
 *
 */

package com.ca.apm.systemtest.fld.logmonitor.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.As;
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;

/**
 * @author keyja01
 */
@JsonInclude(Include.ALWAYS)
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonTypeInfo(use = Id.NAME, include = As.PROPERTY, property = "@type")
@JsonSubTypes(value = {
    @Type(value = SimpleCondition.class, name = "simple"),
    @Type(value = CompoundCondition.class, name = "compound"),
    @Type(value = NotCondition.class, name = "not")})
public abstract class Condition {
    protected static final Logger logger = LoggerFactory.getLogger(Condition.class);

    /**
     * Tests the line and returns true if the condition is met
     *
     * @param line
     * @return
     */
    public abstract boolean match(String line);
}
