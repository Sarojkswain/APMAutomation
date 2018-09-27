package com.ca.apm.webui.test.framework.base;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Properties;

import org.apache.commons.lang.StringUtils;
import org.testng.Assert;
import org.testng.annotations.AfterSuite;

import com.ca.apm.webui.test.framework.interfaces.IConstants;
import com.ca.apm.webui.test.framework.interfaces.IHasName;
import com.ca.apm.webui.test.framework.interfaces.IHasType;
import com.ca.apm.webui.test.framework.interfaces.IHasVersion;
import com.ca.apm.webui.test.framework.tools.qc.QCUpdater;

/**
 * The <code><a name="TOP">BaseTestObject</a></code> is the base class for all
 * QATF classes. This class provides basic object-related properties, a logger,
 * and a properties object.
 * 
 * <p>
 * Object-level getters and setters are defined for ObjectName, ObjectType, and
 * ObjectVersion. These name,type, and version attributes are arbitrary string
 * values meant to help facilitate run-time test management.
 * 
 * The attribute values should be set at the sub-class level in the constructor
 * for all objects that extend <code>BaseTestObject</code> or are derived from
 * such.
 * 
 * <pre>
 * Example of Setting Values
 * {@code
 * public MyClass extends BaseTestObject {
 *   public myClass() {
 *     setObjectName("MyObjectIsBob"); //any ole' name I want
 *     setObjectType("Service");//UIComponent,AbstractClass,Service,Browser
 *     setObjectVersion("1.0");
 *    } //end constructor
 * } //end class
 * </pre>
 * 
 * @since QATF2.0
 * @author whogu01
 * @copyright 2013 CA Technology, All rights reserved.
 */
public abstract class BaseTestObject 
    implements IHasName, IHasVersion, IHasType, IConstants 
{

	// hold connection object at base level
	protected static QCUpdater fInstance;
	
    /** Log wrapper for class. (log4j) */
    protected TestLogger fLog;

    /** Properties for class. */
    private Properties   fProp;

    /**
     * Default logging level set to DEBUG.
     */
    protected BaseTestObject()
    {

        fProp = new Properties(); // empty properties object
        fLog = TestLogger.getInstance(); // default level is DEBUG

    } // end empty constructor

    /*
     * Properties Object Methods
     */

    /** @return the Properties object associated with this class. */
    public final Properties getPropertiesObject()
    {
        return fProp;
    } // end getter

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
    public final void loadPropertiesFromFile(final String fileName)
    {

        Properties tempProp = PropertyLoader.loadProperties(fileName);
        fProp.putAll(tempProp);

    } // end method

    /**
     * Merge all of the properties from <code>prop</code> into the properties
     * for this object.
     * 
     * @param prop
     *            A Properties object.
     * @since QATF2.0
     */
    public final void mergeProperties(final Properties prop)
    {
        this.fProp.putAll(prop);
    } // end method

    /**
     * Read properties from fileName and return the properties object. <br>
     * Note that the fileName must be specified as a resource and exist in the
     * classpath.
     * <p>
     * Example:
     * <p>
     * <i>readPropertiesFromFile("/com/ca/apm/qatf/contributed/mymodule/
     * myprops.properties");</i>
     * 
     * @param fileName
     *            Properties file in classpath.
     * @since QATF2.0
     */
    public final Properties readPropertiesFromFile(final String fileName)
    {

        if (!StringUtils.isEmpty(fileName))
        {
            return PropertyLoader.loadProperties(fileName);
        } else
        {
            return null;
        } // end if..else

    } // end method

    /**
     * Store the specified key/value pair.
     * <p>
     * Normalization
     * <p>
     * The key is normalized by converting to lower case, and by removing
     * leading, trailing, and inner whitespaces.
     * <p>
     * The value is normalized by removing leading and trailing whitespaces.The
     * case and inner whitespaces are preserved.
     * 
     * @param key
     *            A case-insensitive, non-Empty, non-Null String.
     * @param value
     *            An empty String is acceptable.
     * @since QATF2.0
     */
    public final void setProperty(final String key, final String value)
    {

        if (!StringUtils.isBlank(key))
        {

            if ((StringUtils.isBlank(value)) || (StringUtils.isEmpty(value)))
            {
                this.fProp.setProperty(StringUtils.deleteWhitespace(key)
                        .toLowerCase().trim(), " ");
            } else
            {
                this.fProp.setProperty(StringUtils.deleteWhitespace(key)
                        .toLowerCase().trim(), value.trim());
            } // end if..else
        } // end outer if.else

    } // end method

    /**
     * Get the value for the specified key. see
     * {@link #setProperty(String, String)} re: key normalization.
     * 
     * @param key
     *            A case-insensitive, non-Empty, non-Null String.
     * @return If key exists, return the value (empty string supported)<br>
     *         If key is empty, blank, or absent, return NULL
     * @since QATF2.0
     */
    public final String getProperty(final String key)
    {

        if (StringUtils.isBlank(key))
        {
            return null;
        }
        return this.fProp.getProperty(StringUtils.deleteWhitespace(key)
                .toLowerCase().trim(), null);

    } // end method

    /**
     * Check for existence of the specified key.
     * <p>
     * see {@link #setProperty(String, String)} re: key normalization.
     * 
     * @param key
     *            A case-insensitive, non-Empty, non-Null String.
     * @return <b>true</b> - key exists.<br>
     *         <b>false</b> - key is absent.
     * @since QATF2.0
     */
    public final boolean propertyExists(final String key)
    {

        if (StringUtils.isBlank(key))
        {
            return false;
        } else
        {
            try
            {
                if (this.fProp.getProperty(StringUtils.deleteWhitespace(key)
                        .toLowerCase().trim()) == null)
                {
                    return false;
                } else
                {
                    return true;
                } // end if..else
            } catch (Exception e)
            {
                return false;
            }
        } // end if..else

    } // end method

    /**
     * Checks whether or not the passed-in text matches the key's value.
     * <p>
     * see {@link #setProperty(String, String)} re: key and value (text)
     * normalization.
     * 
     * @param key
     *            A case-insensitive, non-Empty, non-Null String.
     * @param text
     *            A <i>case-sensitive</i> value. Empty string is acceptable.
     * @return <b>true</b> - 'text' matches the key's value<br>
     *         <b>false</b> - either 'text' does not match key value or key is
     *         missing.
     * @since QATF2.0
     */
    public final boolean isPropertyValueEqual(final String key,
                                              final String text)
    {

        if (propertyExists(key))
        {
            return (this.fProp.getProperty(StringUtils.deleteWhitespace(key)
                    .toLowerCase().trim()).equals(text.trim()));
        } else
        { // property is missing
            return false;
        } // end if..else

    } // end method

    /**
     * Get an ascending-sorted list of all properties for this object.
     * 
     * @return List&ltString&gt where each item is 'key=value'
     * @since QATF2.0
     */
    public final List<String> getPropertiesList()
    {

        List<String> allProps = new ArrayList<String>();
        Enumeration<?> e = this.fProp.propertyNames();

        while (e.hasMoreElements())
        {
            String key = (String) e.nextElement();
            String value = this.fProp.getProperty(key);
            allProps.add(key + "=" + value);
        } // end while

        Collections.sort(allProps);
        return allProps;

    } // end method

    /**
     * Print the properties associated with this object into the testcase.log.
     * 
     * @since QATF2.0
     */
    public final void logAllProperties()
    {

        String message = "Listing All Properties For Object:" + NEW_LINE
                         + toString() + ":" + NEW_LINE;
        List<String> allProps = getPropertiesList();
        for (String temp : allProps)
        {
            message += temp + NEW_LINE;
        } // end for
        fLog.logTestCase(INFO, message.trim());

    } // end method

    /*--------------------------------------------
        Test framework convenience methods for
        this object's name, type, version
     ---------------------------------------------*/

    /**
     * Set this object's objectType value to 'typeValue'.
     * <p>
     * see {@link #setProperty(String, String)} re:value (typeValue)
     * normalization.
     * <p>
     * See {@link BaseTestObject} for notes on the ObjectType attribute.
     * 
     * @param typeValue
     *            User-defined. Empty String is okay.
     * @since QATF2.0
     */
    public final void setObjectType(final String typeValue)
    {
        setProperty("this.object.type", typeValue.trim());
    } // end method

    /**
     * @return the objectType value for this object.
     * @since 1.0
     */
    public final String getObjectType()
    {
        return fProp.getProperty("this.object.type");
    } // end method

    /**
     * Check whether or not the 'text' parameter matches the objectType value.
     * Case is considered.
     * <p>
     * see {@link #setProperty(String, String)} re: value (text) normalization.
     * 
     * @param text
     *            A <i>case sensitive</i> value. Empty string is acceptable.
     * @return <b>true</b> - 'text' matches the objectType value.<br>
     *         <b>false</b> - 'text' does not match the objectType value.
     * @since QATF2.0
     */
    public final boolean isObjectTypeEqual(final String text)
    {
        return (getObjectType().equals(text.trim()));
    } // end method

    /**
     * Set this object's ObjectName.
     * <p>
     * See {@link BaseTestObject} for notes on ObjectName.
     * <p>
     * see {@link #setProperty(String, String)} re: value (objectNameValue)
     * normalization.
     * 
     * @param objectNameValue
     *            user-defined value, empty String is acceptable.
     * @since QATF2.0
     */
    public final void setObjectName(final String objectNameValue)
    {
        setProperty("this.object.name", objectNameValue.trim());
    } // end method

    /**
     * @return the ObjectName value for this object.
     * @since 1.0
     */
    public final String getObjectName()
    {
        return this.fProp.getProperty("this.object.name");
    } // end method

    /**
     * Checks whether or not the passed-in objectName matches this object's
     * objectName.
     * 
     * @param objectName
     *            A <i>case insensitive</i> value. Empty string is acceptable.
     * @return <b>true</b> - This object's name matches objectName.<br>
     *         <b>false</b> -This object's name does not match objectName.
     * @since QATF2.0
     */
    public final boolean isObjectNameEqual(final String objectName)
    {
        return (getObjectName().equals(objectName.trim()));
    } // end method

    /**
     * Set this object's ObjectVersion attribute.
     * <p>
     * <p>
     * See {@link BaseTestObject} for notes on ObjectVersion.
     * <p>
     * see {@link #setProperty(String, String)} re:value (objectVersion)
     * normalization.
     * 
     * @param objectVersion
     *            User-defined value, empty String is acceptable.
     * @since QATF2.0
     */
    public final void setObjectVersion(String objectVersion)
    {
        setProperty("this.object.version", objectVersion.trim());
    } // end method

    /** @since QATF2.0 */
    public final String getObjectVersion()
    {
        return this.fProp.getProperty("this.object.version");
    } // end method

    /**
     * Checks whether or not the passed-in objectVersion matches this object's
     * objectVersion value.
     * <p>
     * see {@link #setProperty(String, String)} re: value (objectVersion)
     * normalization.
     * 
     * @param objectVersion
     *            A <i>case sensitive</i> value. Empty string is acceptable.
     * @return <b>true</b> - This object's version matches objectVersion.<br>
     *         <b>false</b> -This object's version does not match objectVersion.
     * @since QATF2.0
     */
    public final boolean isObjectVersionEqual(final String objectVersion)
    {
        return (getObjectVersion().equals(objectVersion.trim()));
    } // end method

    @Override
    /**
     * Get the object's canonical name, ObjectName, ObjectType, and
     * ObjectVersion.
     * @return A custom String representation of this object.
     * @since QATF2.0
     */
    public String toString()
    { // Keep non-final so subclasses can override

        return "[@Class=" + this.getClass().getCanonicalName().toString()
               + " @name=" + this.getObjectName() + " @type="
               + this.getObjectType() + " @version=" + this.getObjectVersion()
               + "]";

    } // end method

    /**
     * @param numOfMillis
     *            Number of milliseconds to sleep. A value less than 0 is set to
     *            0. Any value greater than 120000 is reduced to 120000.
     * @since QATF2.0
     */
    public final void sleep(long numOfMillis)
    {
        if (numOfMillis > 120000)
        {
            numOfMillis = 120000;
        } else if (numOfMillis < 0)
        {
            numOfMillis = 0;
        }
        try
        {
            Thread.sleep(numOfMillis);
        } catch (InterruptedException e)
        {
            e.printStackTrace();
        }
    } // end method

    /**
     * Sleep for 'wait.between.user.actions.in.millis'. Specified in
     * launch.properties. Place this method between user input and click
     * actions.
     * 
     * @since QATF2.0
     */
    public final void sleepBetweenActions()
    {
        long numOfMillis = Long
                .parseLong(getProperty("wait.between.user.actions.in.millis"));
        if (numOfMillis > 10000)
        {
            numOfMillis = 10000;
        } else if (numOfMillis < 0)
        {
            numOfMillis = 0;
        }
        try
        {
            Thread.sleep(numOfMillis);
        } catch (InterruptedException e)
        {
            e.printStackTrace();
        }
    } // end method

    // Delegating and decorating log message to BaseTestLogger

    /**
     * Decorate the message with objectName before writing to the testcase log.
     * 
     * @param level
     *            Log level for <code>message</code>.
     * @param message
     *            - user defined text.
     * @since QATF2.0
     */
    public final void logTestCase(final int level, final String message)
    {
        String tempM = "<" + getObjectName() + "::" + getObjectType() + "> "
                       + message;
        fLog.logTestCase(level, tempM);

    } // end method
    
    /**
     * Decorate the message with objectName before writing to the testcase log.
     * 
     * @param level
     *            Log level for <code>message</code>.
     * @param message
     *            - user defined text.
     * @since QATF2.0
     */
    public final void logTestCaseAndFail(final int level, final String message)
    {
        String tempM = "<" + getObjectName() + "::" + getObjectType() + "> "
                       + message;
        fLog.logTestCase(level, tempM);

        Assert.fail(message);
    } // end method

    /**
     * Decorate the message with objectName before writing to the testsuite log.
     * 
     * @param level
     *            Log level for <code>message</code>.
     * @param message
     *            - user defined text.
     * @since QATF2.0
     */
    public final void logTestSuite(final int level, final String message)
    {
        String tempM = "<" + getObjectName() + "::" + getObjectType() + "> "
                       + message;
        fLog.logTestSuite(level, tempM);

    } // end method

    /**
     * Decorate the message with objectName before writing to the test-case and
     * test-suite log.
     * 
     * @param level
     *            Log level for <code>message</code>.
     * @param message
     *            - user defined text.
     * @since QATF2.0
     */
    public final void logBoth(final int level, final String message)
    {
        String tempM = "<" + getObjectName() + "::" + getObjectType() + "> "
                       + message;
        fLog.logBoth(level, tempM);

    } // end method
    
    @AfterSuite
    public void afterSuite()
    {
    	// release qc connecton
    	if(fInstance != null)
    	{
    	   fInstance.disconnect();
    	   fInstance = null;
    	}
    }
  

} // end class