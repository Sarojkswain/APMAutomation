package com.ca.apm.webui.test.framework.interfaces;

/**
 * The <code>SwitchableProperties</code> interface defines methods for test
 * applications and services requiring alternate versions of property files.
 * 
 * @author whogu01
 * @since QATF2.0
 * @copyright 2013 CA Technology, All rights reserved.
 */
public interface ISwitchableProperties
{

    /**
     * @return The value of the current property file set. The value can be used
     *         to discriminate between several different properties files or
     *         sets of properties files.
     * @since QATF2.0
     */
    String getPropertyFileSet();

    /**
     * Switch the property file set to <code>value</code>. The application or
     * service should implement the actual switching-logic in this method.
     * 
     * @param version
     *            A keyword differentiating one set of properties files from
     *            other sets.
     * @since QATF2.0
     */
    void switchPropertyFileSet(String value);

} // end interface