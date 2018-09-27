package com.ca.apm.tests.util.selenium;

/*
 * code based on
 * selenium-plugin/src/main/java/com/ca/apm/systemtest/fld/plugin/selenium/SeleniumPlugin.java
 */
public interface SeleniumHelper {

    public enum SelectionBy {
        ID, XPATH, NAME, LINK, PARTIAL_LINK, TAG_NAME, CLASS
    }

    /**
     * @return session ID used in further calls
     */
    String startSession();

    /**
     * Opens a URL in a new window or tab, and returns the window ID.
     *
     * @param url URL to open
     * @return window ID
     */
    String openUrl(String sessionId, String string);

    boolean waitForElement(String sessionId, String windowId, SelectionBy id, String string, int i);

    boolean fillTextField(String sessionId, String windowId, SelectionBy id, String string,
                          String webViewUser);

    boolean click(String sessionId, String windowId, SelectionBy id, String string);

}
