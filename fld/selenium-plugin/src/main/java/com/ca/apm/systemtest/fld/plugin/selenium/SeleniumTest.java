package com.ca.apm.systemtest.fld.plugin.selenium;

import java.util.Map;

public interface SeleniumTest {
    public void executeSeleniumScript(SeleniumPlugin plugin, Map<String, String> params)
        throws SeleniumPluginException;

    /**
     * Indicate to the running test that it should terminate voluntarily at its earliest convenience
     */
    public void shouldStop();
}
