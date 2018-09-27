package com.ca.apm.systemtest.fld.util.selenium;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class SeleniumHelperResolver {

    private static final Map<Browser, SeleniumHelperBase> SELENIUM_HELPER_MAPPING;

    static {
        Map<Browser, SeleniumHelperBase> map = new HashMap<>();

        map.put(Browser.CHROME, new ChromeSeleniumHelper());
        map.put(Browser.FIREFOX, new FirefoxSeleniumHelper());
        map.put(Browser.INTERNET_EXPLORER, new IESeleniumHelper());

        SELENIUM_HELPER_MAPPING = Collections.unmodifiableMap(map);
    }

    private SeleniumHelperResolver() {}

    public static SeleniumHelperBase getSeleniumHelper(String browserAlias) {
        return getSeleniumHelper(Browser.resolveBrowser(browserAlias));
    }

    public static SeleniumHelperBase  getSeleniumHelper(Browser browser) {
        return SELENIUM_HELPER_MAPPING.get(browser);
    }

}
