/**
 *
 */

package com.ca.apm.systemtest.fld.logmonitor.config;

import com.ca.apm.systemtest.fld.common.ErrorUtils;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

/**
 * @author keyja01
 */
@JsonInclude(Include.ALWAYS)
public class AndCondition extends CompoundCondition {
    @Override
    public boolean match(String line) {
        boolean matches = true;
        for (Condition c : conditionList) {
            try {
                matches = c.match(line);
            } catch (Exception e) {
                ErrorUtils.logExceptionFmt(logger, e,
                    "Exception testing AND condition {1}. Exception: {0}", c);
                return false;
            }
            if (!matches) {
                break;
            }
        }
        return matches;
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof AndCondition && super.equals(obj);
    }
}
