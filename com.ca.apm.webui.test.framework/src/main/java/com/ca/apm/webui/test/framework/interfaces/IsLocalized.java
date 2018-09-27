package com.ca.apm.webui.test.framework.interfaces;

/**
 * The <code>IsLocalized</code> interface defines methods for applications or
 * services that are language-specific.
 * 
 * @author whogu01
 * @since QATF2.0
 * @copyright 2013 CA Technology, All rights reserved.
 */
public interface IsLocalized
{

    /**
     * 
     * @return The language value for the test application.
     * @since QATF2.0
     */
    String getLanguage();

    /**
     * Switch the language to <code>value</code>. If there are any application-
     * specific attributes, properties, or files that switch based upon the
     * language value, the application or service should implement the actual
     * switching-logic in this method.
     * 
     * @param value
     *            The language value for the application.
     * @since QATF2.0
     */
    boolean switchLanguage(String value);

} // end interface