package com.ca.apm.webui.test.framework.interfaces;

/**
 * Each object in the test framework requires an object version. This interface
 * defines the methods related to the version attribute.
 * 
 * @since QATF2.0
 * @author whogu01
 * @copyright 2013 CA Technology, All rights reserved.
 */
public interface IHasVersion
{

    /**
     * Set the object version to a user-defined value.
     * 
     * @param objectVersion
     *            A user-defined version.
     */
    public void setObjectVersion(String version);

    /**
     * @return The object version assigned to the object.
     */
    public String getObjectVersion();

    /**
     * Check the objects's actual version against <code>version</code>.
     * 
     * @param version
     *            The value to use for the check.
     * @return True if version matches.
     */
    public boolean isObjectVersionEqual(String version);

} // end interface