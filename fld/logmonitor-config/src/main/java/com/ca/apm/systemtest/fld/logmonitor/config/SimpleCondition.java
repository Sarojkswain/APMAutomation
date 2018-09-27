/**
 *
 */

package com.ca.apm.systemtest.fld.logmonitor.config;

import java.util.Objects;
import java.util.regex.Pattern;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

/**
 * Matches a single string.  All operators except {@link Operator#Regexp} honor the
 * caseInsensitive flag.
 *
 * @author keyja01
 */
@JsonInclude(Include.ALWAYS)
@JsonIgnoreProperties(ignoreUnknown = true)
public class SimpleCondition extends Condition {
    private Operator operator;
    private String target;
    private boolean caseInsensitive;
    private transient Pattern pattern;


    public SimpleCondition() {
        this(Operator.Contains, "", false);
    }

    public SimpleCondition(Operator operator, String target, boolean caseInsensitive) {
        this.operator = operator;
        this.target = target;
        this.caseInsensitive = caseInsensitive;
    }


    public static SimpleCondition contains(String target) {
        return new SimpleCondition(Operator.Contains, target, false);
    }


    public static SimpleCondition regexp(String target) {
        return new SimpleCondition(Operator.Regexp, target, false);
    }


    @Override
    public String toString() {
        return "Condition(" + operator + "," + target + "," + caseInsensitive + ")";
    }


    /* (non-Javadoc)
     * @see com.ca.apm.systemtest.fld.plugin.logmonitor.config.Condition#match(java.lang.String)
     */
    @Override
    public boolean match(String line) {
        String tgt = target;
        if (!operator.equals(Operator.Regexp) && caseInsensitive) {
            line = line.toLowerCase();
            tgt = tgt.toLowerCase();
        }
        switch (operator) {
            case Contains:
                return line.contains(tgt);
            case EndsWith:
                return line.endsWith(tgt);
            case NotContains:
                return !line.contains(tgt);
            case StartsWith:
                return line.startsWith(tgt);
            case Regexp:
                return getPattern().matcher(line).matches();
            default:
                return false;
        }
    }

    private Pattern getPattern() {
        if (pattern == null) {
            pattern = Pattern.compile(target);
        }
        return pattern;
    }

    public Operator getOperator() {
        return operator;
    }

    public void setOperator(Operator operator) {
        this.operator = operator;
    }

    public String getTarget() {
        return target;
    }

    public void setTarget(String target) {
        pattern = null;
        this.target = target;
    }

    public boolean isCaseInsensitive() {
        return caseInsensitive;
    }

    public void setCaseInsensitive(boolean caseInsensitive) {
        this.caseInsensitive = caseInsensitive;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        SimpleCondition that = (SimpleCondition) o;
        return caseInsensitive == that.caseInsensitive
            && operator == that.operator
            && Objects.equals(target, that.target);
    }

    @Override
    public int hashCode() {
        return Objects.hash(operator, target, caseInsensitive);
    }
}
