package com.ca.apm.tests.util.selenium;

import io.github.bonigarcia.wdm.*;

import java.util.Collection;
import java.util.SortedSet;
import java.util.TreeSet;

public enum Browser {

    FIREFOX(new String[] {"ff", "firefox"}, null, null),
    CHROME(new String[] {"ch", "chrome"}, ChromeDriverManager.class, "2.22"),
    INTERNET_EXPLORER(new String[]{
        "ie", "iexplorer", "internetexplorer", "internet-explorer", "internet_explorer"},
        InternetExplorerDriverManager.class, null),
    OPERA(new String[] {"opera"}, OperaDriverManager.class, null),
    PHANTOMJS(new String[] {"pjs", "phantomjs"}, PhantomJsDriverManager.class, null);

    private String[] aliases;
    private Class<? extends BrowserManager> driverManagerClass;
    private String forcedVersion;

    Browser(String[] aliases, Class<? extends BrowserManager> driverManagerClass, String forcedVersion) {
        this.aliases = aliases;
        this.driverManagerClass = driverManagerClass;
        this.forcedVersion = forcedVersion;
    }

    public Class<? extends BrowserManager> getDriverManagerClass() {
        return driverManagerClass;
    }

    public static Browser resolveBrowser(String alias) {
        for (Browser browser : Browser.values()) {
            for (String a : browser.aliases) {
                if (a.equalsIgnoreCase(alias)) {
                    return browser;
                }
            }
        }
        return null;
    }

    public static SortedSet<Browser> resolveBrowsers(Collection<String> browserAliases) {
        SortedSet<Browser> browsers = new TreeSet<>();
        if (browserAliases != null) {
            for (String browserAlias : browserAliases) {
                browsers.add(Browser.resolveBrowser(browserAlias));
            }
        }
        return browsers;
    }

    public String getForcedVersion() {
        return forcedVersion;
    }
}
