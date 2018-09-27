/**
 *
 */
package com.ca.apm.systemtest.fld.logmonitor.config;

import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

/**
 * Used to test for a negative match
 *
 * @author keyja01
 */
@JsonInclude(Include.ALWAYS)
@JsonIgnoreProperties(ignoreUnknown = true)
public class NotCondition extends Condition {
    private Condition target;

    public NotCondition() {
    }

    public NotCondition(Condition target) {
        super();
        this.target = target;
    }

    @Override
    public boolean match(String line) {
        return !target.match(line);
    }

    public Condition getTarget() {
        return target;
    }

    public void setTarget(Condition target) {
        this.target = target;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof NotCondition)) {
            return false;
        }
        NotCondition that = (NotCondition) o;
        return Objects.equals(target, that.target);
    }

    @Override
    public int hashCode() {
        return Objects.hash(target);
    }
}
