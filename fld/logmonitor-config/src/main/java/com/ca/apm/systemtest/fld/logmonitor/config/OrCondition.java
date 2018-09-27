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
public class OrCondition extends CompoundCondition {

    @Override
    public boolean match(String line) {
        boolean matched = false;
        for (Condition c : conditionList) {
            try {
                matched = c.match(line);
            } catch (Exception e) {
                ErrorUtils.logExceptionFmt(logger, e,
                    "Exception testing OR condition {1}. Exception: {0}", c);
            }
            if (matched) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof OrCondition && super.equals(obj);
    }
}
