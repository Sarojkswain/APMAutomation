package com.ca.apm.webui.test.framework.base;

import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.WebDriverWait;

import com.ca.apm.webui.test.framework.tools.qc.QCUpdater;
import com.ca.apm.webui.test.framework.tools.qc.TestCaseData;

/**
 * <code> AbstractTestCase</code> is an abstract class which provides basic
 * testcase services such as testcase-specific properties, convenience methods
 * for testcase logging,testcase exit-management, and testNG before/after
 * methods.
 * 
 * <p>
 * TEST CASE MANAGEMENT
 * <p>
 * The method, <code>defaultTest()</code> must be given an concrete
 * implementation in the subclass. The intention of defaultTest() is to act as a
 * method template for further test methods in the extending class.
 * <code>defaultTest()</code> should not be used as the real test method but
 * rather as a code template.
 * 
 * <p>
 * AbstractTestCase includes a non-override, final method called myAfterTest()
 * which contains the call to the helper method, {@link #processTestCaseExit()},
 * which contains the testcase exit management code.
 * 
 * <p>
 * ADDITIONAL @TEST METHODS
 * <p>
 * 
 * <p>
 * Additional test methods should be created within the same testcase subclass.
 * Each new test method must include the @Test annotation and should represent a
 * stand-alone testcase, having it's own testcase name, id, and author values.
 * 
 * <p>
 * SETTING TESTCASE STATUS
 * <p>
 * Within each method marked by the @Test annotation, prior to exiting the
 * method,the test designer must make a call to either
 * {@link #setTestCaseStatusToPass()} or {@link #setTestCaseStatusToFail()} in
 * order for the test-case exit to be handled appropriately. Unless a call is
 * explicitly made to set the status to pass, the testcase will fail (default
 * behavior).
 * 
 * @since QATF2.0
 * @author whogu01
 * @copyright 2013 CA Technology, All rights reserved.
 */
public abstract class AbstractTestCase
    extends BaseTestObject
{
    /**
     * Selenium WebDriver object for <code>this</code>.
     * 
     * @since QATF2.0
     */
    private WebDriver          fWebDriver;

    /**
     * Selenium Actions object for <code>this</code>.
     * 
     * @since QATF2.0
     */
    private Actions            fActions;

    /**
     * Selenium JavascriptExecutors object for <code>this</code>.
     * 
     * @since QATF2.0
     */
    private JavascriptExecutor fJavaScriptExecutor;

    /**
     * Selenium WebDriverWait object for <code>this</code>.
     * 
     * @since QATF2.0
     */
    private WebDriverWait      fWait;

    // Default status indicates FAIL.
    private STATUS             status = STATUS.FAIL;

    protected AbstractTestCase()
    {
        loadPropertiesFromFile(BROWSER_FILE);
        loadPropertiesFromFile(LAUNCH_FILE);
        loadPropertiesFromFile(APPLICATION_FILE);

        // setting this.object name/type/version
        setProperty("this.object.name", "TestNGTestCase");
        setProperty("this.object.type", "TestCase");
        setProperty("this.object.version", "1.0");
        
        // setting test-case default values
        setProperty("testcase.name", "<not-defined>");
        setProperty("testcase.author", "<not-defined>");
        setProperty("testcase.id", "<not-defined>");
        setProperty("testcase.text.onfail", "<not-defined>");
        setProperty("testcase.description", "<not-defined>");
        setProperty("log.level", "debug");
        
        // TestLogger.setMaximumLogLevel(DEBUG);
        setTestCaseStatusToFail();
    }

    /*-------------------------------------------
    	 Getters and Setters for Selenium Objects
     --------------------------------------------*/

    /**
     * Get <code>this.fWebDriver</code>.
     * 
     * @return Selenium WebDriver object.
     * @since QATF2.0
     */
    public final WebDriver getWebDriver()
    {
        return this.fWebDriver;
    }

    /**
     * Set <code>this.fWebDriver</code> to <code>fWebDriver</code>.
     * 
     * @param wd
     *            - Selenium WebDriver object.
     * @since QATF2.0
     */
    public final void setWebDriver(WebDriver wd)
    {
        this.fWebDriver = wd;
    } 

    /**
     * Get <code>this.fActions</code>.
     * 
     * @return Selenium Actions object.
     * @since QATF2.0
     */
    public final Actions getActions()
    {
        return this.fActions;
    }

    /**
     * Set <code>this.fActions</code> to <code>act</code>.
     * 
     * @param act
     *            - Selenium Actions object.
     * @since QATF2.0
     */
    public final void setActions(Actions act)
    {
        this.fActions = act;
    } 

    /**
     * Get <code>this.fActions</code>.
     * 
     * @return Selenium WebDriverWait object.
     * @since QATF2.0
     */
    public final WebDriverWait getWait()
    {
        return this.fWait;
    }

    /**
     * Set <code>this.fWait</code> to <code>wait</code>.
     * 
     * @param wait
     *            - Selenium WebDriverWait object.
     * @since QATF2.0
     */
    public final void setWait(WebDriverWait wait)
    {
        this.fWait = wait;
    } 

    /**
     * Get <code>this.fJavaScriptExecutor</code>.
     * 
     * @return Selenium JavascriptExecutor object.
     * @since QATF2.0
     */
    public final JavascriptExecutor getJavascriptExecutor()
    {
        return this.fJavaScriptExecutor;
    }

    /**
     * Set <code>this.fJavaScriptExecutor</code> to <code>je</code>.
     * 
     * @param je
     *            - Selenium JavascriptExecutor object.
     * @since QATF2.0
     */
    public final void setJavascriptExecutor(JavascriptExecutor je)
    {
        this.fJavaScriptExecutor = je;
    } 
    
    // *** GETTERS AND SETTERS ***
    protected final String getTestCaseDescription()
    {
        return this.getProperty("testcase.description");
    } 

    protected final void setTestCaseDescription(String description)
    {
        this.setProperty("testcase.description", description);
    } 

    // test-caseID (comma separated string of QC ID)
    protected final String getTestCaseID()
    {
        return this.getProperty("testcase.id");
    }

    protected final void setTestCaseID(String testCaseID)
    {
        this.setProperty("testcase.id", testCaseID);
    }

    // test-case name (ex. = "<QC-ID LIST> - <NAME>")
    protected final String getTestCaseName()
    {
        return this.getProperty("testcase.name");
    }

    protected final void setTestCaseName(String testCaseName)
    {
        this.setProperty("testcase.name", testCaseName);
    }

    // test-case author (PMFKEY)
    protected final String getAuthorName()
    {
        return this.getProperty("testcase.author");
    }

    protected final void setAuthorName(String authorName)
    {
        this.setProperty("testcase.author", authorName);
    }

    // text to display on test-case logging when test-case fails and exits
    protected final String getTestCaseExitTextOnFail()
    {
        return this.getProperty("testcase.text.onfail");
    }

    protected final void setTestCaseExitTextOnFail(String testCaseExitTextOnFail)
    {
        this.setProperty("testcase.text.onfail", testCaseExitTextOnFail);
    }

    protected final void setTestCaseStatusToPass()
    {
        this.status = STATUS.PASS;
    } 

    protected final void setTestCaseStatusToFail()
    {
        this.status = STATUS.FAIL;
    } 

    // exit-code (used to determine pass/fail for testNG assertion)
    protected final STATUS getTestCaseStatus()
    {
        return this.status;
    }

    // get TC status as a string compatible with Quality Center Updating
    protected final String testCaseStatusToString()
    {
        if (this.status == STATUS.PASS)
        {
            return "Passed";
        } else
        {
            return "Failed";
        } 
    }

    /**
     * Log test-start message to testcase.log and testsuite.log.
     * 
     * @since QATF2.0
     * @author whogu01
     */
    protected final void logTestCaseStart()
    {
        String message = "S T A R T   T E S T   C A S E " + NEW_LINE
                         + "   Name:  \"" + getTestCaseName() + "\"" + NEW_LINE
                         + "   Class: \"" + getClass().getCanonicalName()
                         + "\"" + NEW_LINE + "   ID:    \"" + getTestCaseID()
                         + "\"" + NEW_LINE + "   Auth:  \"" + getAuthorName()
                         + "\"" + NEW_LINE + "   Desc:  \""
                         + getTestCaseDescription() + "\"";
        fLog.logBoth(INFO, message);
    }

    /**
     * Log test-end message to testcase.log and testsuite.log.
     * 
     * @since QATF2.0
     * @author whogu01
     */
    protected final void logTestCaseEnd()
    {
        String message = "E N D   T E S T   C A S E ";
        fLog.logBoth(INFO, message);
    } 

    /**
     * Set the log level for the test method based on property value. Only info,
     * trace, and debug are allowed at the test case level.
     * 
     * @since QATF2.0.
     */
    protected final void autosetLogLevel()
    {
        String logLvl = getProperty("log.level").toLowerCase().trim();
        if (logLvl.equalsIgnoreCase("info"))
        {
            TestLogger.setMaximumLogLevel(INFO);
        } else if (logLvl.equalsIgnoreCase("trace"))
        {
            TestLogger.setMaximumLogLevel(TRACE);
        } else
        { 
            TestLogger.setMaximumLogLevel(DEBUG);
        } 
    }

    /**
     * <code>processTestCaseExit</code> asserts that the testcase status is
     * equal to PASS. The testcase and testsuite log is updated with with an
     * exit-test message.
     * 
     * In the event of a testNG assertion failure, the assertion short-text is
     * written to the logs and an assertion error is raised. Finally, the
     * testresults.csv is updated with the test-case id and status.
     * <p>
     * 
     * By default, upon testcase instantiation, the testcase status is set to
     * FAILS. The testcase must be explicitly set to passes before calling
     * <code>processTestCaseExit</code> if the intent to to log that the test
     * passed.
     * 
     * @see #setTestCaseStatusToPass()
     * @see #setTestCaseStatusToFail()
     * @see AbstractTestCase
     * @since QATF2.0
     * @author whogu01
     */
    protected final void processTestCaseExit()
    {
        STATUS status = getTestCaseStatus();
        int logLevel = ERROR;
       
        if(status == STATUS.PASS)
        {           
           setObjectType("Passes");       
           
           logLevel = INFO;   
        } else
        {
            setObjectType("Fails");                         
        } 
          
        logTestCaseEnd(); 
        
        logBoth(logLevel, "tcName=\"" + getTestCaseName() + "\" tcID=\""
                + getTestCaseID() + "\"");
        
        updateStatusToQC();
    } 

    /*
     * update test run status to mapped test id in quality center
     */
    private void updateStatusToQC()
    {
        QCUpdater qcc = QCUpdater.getInstance();
        qcc.connect();
        qcc.uploadResult(new TestCaseData(getTestCaseID(), testCaseStatusToString()));
        // release locks
        qcc.disconnect();
    }
} // end base class