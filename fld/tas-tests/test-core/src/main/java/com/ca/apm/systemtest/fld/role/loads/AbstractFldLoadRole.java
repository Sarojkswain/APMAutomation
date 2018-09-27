/**
 * 
 */
package com.ca.apm.systemtest.fld.role.loads;

import com.ca.tas.property.RolePropertyContainer;
import com.ca.tas.role.AbstractRole;

/**
 * @author keyja01
 *
 */
public abstract class AbstractFldLoadRole extends AbstractRole {
    public AbstractFldLoadRole(String roleId) {
        super(roleId);
    }

    public AbstractFldLoadRole(String roleId, RolePropertyContainer envPropertyContainer) {
        super(roleId, envPropertyContainer);
    }
}
