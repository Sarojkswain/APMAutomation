/**
 *
 */

package com.ca.apm.systemtest.fld.logmonitor.config;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonSetter;

/**
 * @author keyja01
 */
@JsonInclude(Include.NON_EMPTY)
@JsonIgnoreProperties(ignoreUnknown = true)
public class Rule {
    private Long blockUntil;
    private Condition condition;
    private LogPeriodicity periodicityLevel;
    /**
     * The number of periodic units
     */
    private Integer periodicity;
    private TimeUnit timeUnit;


    public Rule() {
    }

    public Rule(LogPeriodicity periodicityLevel, Condition condition) {
        this.periodicityLevel = periodicityLevel;
        this.condition = condition;
    }

    public Rule(LogPeriodicity periodicityLevel, Integer periodicity, TimeUnit timeUnit,
        Condition condition) {
        this.periodicityLevel = periodicityLevel;
        this.periodicity = periodicity;
        this.timeUnit = timeUnit;
        this.condition = condition;
    }

    public RuleMatchResult matches(String line) {
        if (condition.match(line)) {
            switch (periodicityLevel) {
                case Always:
                    return RuleMatchResult.MatchesInclude;
                case Never:
                    return RuleMatchResult.MatchesIgnore;
                case OncePerPeriod:
                    if (shouldDefer()) {
                        return RuleMatchResult.MatchesIgnore;
                    } else {
                        return RuleMatchResult.MatchesInclude;
                    }
                default:
                    break;
            }
            // fall back - should never get here
            return RuleMatchResult.MatchesInclude;
        }

        return RuleMatchResult.DoesNotMatch;
    }

    /**
     * Returns true if we should not trigger the log, false otherwise
     *
     * @return
     */
    private synchronized boolean shouldDefer() {
        if (periodicity == null || timeUnit == null) {
            return false;
        }
        if (blockUntil == null) {
            long delay = periodicity.longValue();
            switch (timeUnit) {
                case Days:
                    delay *= 86400000L;
                    break;
                case Hours:
                    delay *= 3600000L;
                    break;
                case Minutes:
                    delay *= 60000L;
                    break;
                case Seconds:
                    delay *= 1000L;
                    break;
            }

            blockUntil = System.currentTimeMillis() + delay;
            return false;
        } else if (System.currentTimeMillis() > blockUntil) {
            blockUntil = null;
            return false;
        }

        return true;

    }

    @Override
    public String toString() {
        return "Rule: [" + periodicityLevel + "] " + condition;
    }

    public Condition getCondition() {
        return condition;
    }

    public void setCondition(Condition condition) {
        this.condition = condition;
    }

    public LogPeriodicity getPeriodicityLevel() {
        return periodicityLevel;
    }

    public void setPeriodicityLevel(LogPeriodicity periodicityLevel) {
        this.periodicityLevel = periodicityLevel;
    }

    /**
     * @deprecated Misspelled variant of periodicity for backwards compatibility.
     * @param periodicityLevel
     */
    @Deprecated
    @JsonSetter
    public void setPeriodocityLevel(LogPeriodicity periodicityLevel) {
        setPeriodicityLevel(periodicityLevel);
    }


    public Integer getPeriodicity() {
        return periodicity;
    }

    public void setPeriodicity(Integer periodicity) {
        this.periodicity = periodicity;
    }

    /**
     * @deprecated Misspelled variant of periodicity for backwards compatibility.
     *
     * @param periodicity number of periodic units
     */
    @Deprecated
    @JsonSetter
    public void setPeriodocity(Integer periodicity) {
        setPeriodicity(periodicity);
    }

    public TimeUnit getTimeUnit() {
        return timeUnit;
    }

    public void setTimeUnit(TimeUnit timeUnit) {
        this.timeUnit = timeUnit;
    }

    public enum RuleMatchResult {
        MatchesInclude, MatchesIgnore, DoesNotMatch;
    }
}
