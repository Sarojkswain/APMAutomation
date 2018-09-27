/**
 *
 */

package com.ca.apm.systemtest.fld.logmonitor.config;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;

/**
 * @author keyja01
 */
@JsonInclude(Include.ALWAYS)
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonSubTypes(value = {
    @Type(value = AndCondition.class, name = "and"), @Type(value = OrCondition.class, name = "or")})
public abstract class CompoundCondition extends Condition {
    protected List<Condition> conditionList;

    public CompoundCondition() {
        conditionList = new ArrayList<>();
    }

    public CompoundCondition(Condition... conditions) {
        this.conditionList = new ArrayList<>(conditions.length);
        Collections.addAll(this.conditionList, conditions);
    }

    public void addCondition(Condition condition) {
        this.conditionList.add(condition);
    }

    public boolean removeCondition(Condition condition) {
        return this.conditionList.remove(condition);
    }

    public void clearConditionList() {
        this.conditionList.clear();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof CompoundCondition)) {
            return false;
        }
        CompoundCondition that = (CompoundCondition) o;
        return Objects.equals(conditionList, that.conditionList);
    }

    @Override
    public int hashCode() {
        return Objects.hash(conditionList);
    }

    public List<Condition> getConditionList() {
        return conditionList;
    }

    public void setConditionList(List<Condition> conditionList) {
        this.conditionList = conditionList;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(getClass().getSimpleName())
            .append("[");
        for (Condition c : conditionList) {
            sb.append(c).append(",");
        }
        sb.append("]");

        return sb.toString();
    }
}
