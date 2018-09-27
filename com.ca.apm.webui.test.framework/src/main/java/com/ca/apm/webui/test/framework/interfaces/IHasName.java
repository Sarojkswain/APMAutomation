package com.ca.apm.webui.test.framework.interfaces;

/**
 * Each object that exists in the test framework requires an object name. This
 * interface defines the methods related to the name attribute.
 * 
 * @since QATF2.0
 * @author whogu01
 * @copyright 2013 CA Technology, All rights reserved.
 */
public interface IHasName
{

    /**
     * Set the object name to a user-defined value.
     * 
     * @param objectName
     *            A user-defined name.
     */
    public void setObjectName(String objectName);

    /**
     * @return The object name assigned to the object.
     */
    public String getObjectName();

    /**
     * Check the objects's actual name against <code>objectName</code>.
     * 
     * @param objectName
     *            The value to use for the check.
     * @return True if name matches.
     */
    public boolean isObjectNameEqual(String objectName);

} // end interface