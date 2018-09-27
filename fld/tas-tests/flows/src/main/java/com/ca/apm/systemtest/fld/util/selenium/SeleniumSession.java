package com.ca.apm.systemtest.fld.util.selenium;

import java.io.Serializable;

public class SeleniumSession implements Serializable {

    private static final long serialVersionUID = -4839326396933627796L;

    public String browser;
    public String url;
    public String sessionId;

    public SeleniumSession() {}

    public SeleniumSession(String browser, String url) {
        this.browser = browser;
        this.url = url;
    }

    public int hashCode() {
        int hc = 0;
        if (browser != null) {
            hc = hc ^ browser.hashCode();
        }
        if (url != null) {
            hc = hc ^ url.hashCode();
        }
        if (sessionId != null) {
            hc = hc ^ sessionId.hashCode();
        }
        return hc;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof SeleniumSession)) {
            return false;
        }
        SeleniumSession s2 = (SeleniumSession) obj;
        return compare(browser, s2.browser) && compare(url, s2.url)
            && compare(sessionId, s2.sessionId);
    }

    private boolean compare(String s1, String s2) {
        if (s1 != null) {
            return s1.equals(s2);
        }
        return s2 == null;
    }

    @Override
    public String toString() {
        return (sessionId == null ? new StringBuilder() : (new StringBuilder(sessionId))
            .append(':')).append(browser).append(':').append(url).toString();
    }

}
