package com.ca.apm.systemtest.fld.plugin.selenium.job;

import java.io.Serializable;

@SuppressWarnings("serial")
public class SeleniumSession implements Serializable {
    public String node;
    public String browser;
    public String url;
    public String sessionId;
    
    public int hashCode() {
        int hc = 0;
        if (node != null) {
            hc = hc ^ node.hashCode();
        }
        
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
        
        return compare(node, s2.node) && compare(browser, s2.browser) && compare(url, s2.url) && compare(sessionId, s2.sessionId); 
    }

    private boolean compare(String s1, String s2) {
        if (s1 != null) {
            return s1.equals(s2);
        }
        return s2 == null;
    }
}

