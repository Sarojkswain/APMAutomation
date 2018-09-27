package com.ca.apm.webui.test.framework.tools.qc;

import java.util.Properties;

import com.ca.apm.webui.test.framework.base.PropertyLoader;

/**
 * 
 * The <code>QCConfig</code> represents ...
 * 
 * @author whogu01
 * @since
 * @copyright 2013 CA Technology, All rights reserved.
 */
public class QCConfig
{

    private static Properties   fProp          = new Properties();          // null;

    private final static String kDefaultConfig = "qualitycenter.properties";

    public static synchronized Properties getProperties()
    {
        loadPropertiesFromFile(kDefaultConfig); // added by whogu01
        return fProp;
    } // end method

    /**
     * Load properties from fileName and add to the properties object associated
     * to this class. <br>
     * Note that the fileName must be specified as a resource and exist in the
     * classpath.
     * <p>
     * Example:
     * <p>
     * <i>loadPropertiesFromFile("/com/ca/apm/qatf/contributed/mymodule/
     * myprops.properties");</i>
     * 
     * @param fileName
     *            Properties file in classpath.
     * @since QATF2.0
     */
    public static final void loadPropertiesFromFile(final String fileName)
    {
        Properties tempProp = PropertyLoader.loadProperties(fileName);
        fProp.putAll(tempProp);
    } // end method

} // end class