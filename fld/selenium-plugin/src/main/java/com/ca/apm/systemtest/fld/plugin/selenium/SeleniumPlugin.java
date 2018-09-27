/**
 *
 */

package com.ca.apm.systemtest.fld.plugin.selenium;

import java.util.List;
import java.util.Map;

import org.openqa.selenium.WebDriver;

import com.ca.apm.systemtest.fld.plugin.Plugin;

/**
 * @author keyja01
 */

public interface SeleniumPlugin extends Plugin {
    enum SelectionBy {
        ID, XPATH, NAME, LINK, PARTIAL_LINK, TAG_NAME, CLASS
    }

    /**
     * @return session ID used in further calls
     * @throws SeleniumPluginException
     */
    String startSession() throws SeleniumPluginException;

    /**
     * Opens a URL in a new window or tab, and returns the window ID.
     *
     * @param url URL to open
     * @return window ID
     * @throws SeleniumPluginException
     */
    String openUrl(String sessionId, String url) throws SeleniumPluginException;

    void visitUrl(String sessionId, String windowId, String url) throws SeleniumPluginException;

    /**
     * Searches for links containing the keyword in the currently open web page.
     *
     * @throws SeleniumPluginException
     */
    List<String> findLinks(String sessionId, String windowId, String keyword)
        throws SeleniumPluginException;

    boolean openLink(String sessionId, String windowId, String link)
        throws SeleniumPluginException;

    List<String> listSessions();

    boolean fillTextField(String sessionId, String windowId, SelectionBy selectionBy, String id,
        String newText) throws SeleniumPluginException;

    boolean submitForm(String sessionId, String windowId, SelectionBy selectionBy, String id)
        throws SeleniumPluginException;

    boolean click(String sessionId, String windowId, SelectionBy selectionBy, String id)
        throws SeleniumPluginException;

    List<String> tableContent(String sessionId, String windowId, SelectionBy selectionBy, String id)
        throws SeleniumPluginException;

    List<String> getText(String sessionId, String windowId, SelectionBy selectionBy,
        String id) throws SeleniumPluginException;

    boolean selectOption(String sessionId, String windowId, SelectionBy selectionBy, String id,
        String option) throws SeleniumPluginException;

    boolean waitForElement(String sessionId, String windowId, SelectionBy selectionBy, String id,
        int timeOutInSeconds) throws SeleniumPluginException;

    boolean acceptAlert(String sessionId, String windowId) throws SeleniumPluginException;

    void closeSession(String sessionId);

    /**
     * Executes a pre-compiled selenium test.
     * @param tempDirName The directory where the artifact has been downloaded and unpacked
     * @param className The fully qualified class name of the test to run
     * @param params Paramters that may be used by the script
     * @param async If true, the script is run asynchronously in the background.
     * @return The execution ID if run asynchronously in the background.  Otherwise null.
     * @throws SeleniumPluginException
     */
    String executeSeleniumTest(String tempDirName, String className, Map<String, String> params,
        boolean async)
        throws SeleniumPluginException;

    /**
     * Stop precompiled test.
     * @param executionId ID of executed process which should be stoped
     */
    void shouldStop(String executionId);

    /**
     * Returns the current status of the async test.
     * @param testExecutionId
     * @return
     */
    Execution checkAsyncSeleniumtest(String testExecutionId) throws SeleniumPluginException;

    /**
     * Returns the selenium {@link WebDriver} for the given session.
     * 
     * @return
     */
    WebDriver webDriver(String sessionId);
}
