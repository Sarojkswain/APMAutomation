package com.ca.apm.webui.test.framework.interfaces;

/**
 * Each object in the test framework requires an object type. This interface
 * defines the methods related to the type attribute.
 * 
 * @since QATF2.0
 * @author whogu01
 * @copyright 2013 CA Technology, All rights reserved.
 */
public interface IHasType
{

    /**
     * Set the object type to the user-defined objectType.
     * 
     * @param objectType
     *            an arbitrary String.
     */
    public void setObjectType(String objectType);

    /**
     * @return The object type assigned to the object.
     */
    public String getObjectType();

    /**
     * Check the objects's actual type against <code>objectType</code>.
     * 
     * @param objectType
     *            The value to use for the check.
     * @return True if name matches.
     */
    public boolean isObjectTypeEqual(String objectType);

} // end interface