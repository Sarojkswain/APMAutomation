package com.ca.apm.test.atc.common;

import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;

public class UrlUtils {
    
    private static class MyNameValuePair implements NameValuePair {
        private String name;
        private String value;

        public MyNameValuePair(String name, String value) {
            super();
            this.name = name;
            this.value = value;
        }
        
        @Override
        public String getName() {
            return name;
        }

        @Override
        public String getValue() {
            return value;
        }

        @Override
        public String toString() {
            return name + "=" + value;
        }
    }

    private UrlUtils() {}

    /**
     * Sets the given parameter within the ATC UI URL to the specified value and returns the URL.
     * If the URL contained the parameter, its original value is replaced with the passed one.
     */
    public static String getAtcMapUrlWithParamValue(String url, String paramName, String value) throws Exception {
        int divPos = url.indexOf("?");
        String firstPart = url.substring(0, divPos);
        String secondPart = url.substring(divPos + 1);
        
        List<NameValuePair> params = URLEncodedUtils.parse(secondPart, Charset.forName("UTF-8"));
        List<NameValuePair> modParams = new ArrayList<NameValuePair>(params.size());
        
        for (NameValuePair p : params) {
            if (!p.getName().equals(paramName)) {
                modParams.add(p);
            }
        }
        
        modParams.add(new MyNameValuePair(paramName, value));
        
        String resultUrl = firstPart + "?" + URLEncodedUtils.format(modParams, Charset.forName("UTF-8"));
        return resultUrl;
    }
    
    /**
     * Sets the given parameter within the WebView URL to the specified value and returns the URL.
     * If the URL contained the parameter, its original value is replaced with the passed one.
     */
    public static String getWebViewUrlWithParamValue(String url, String paramName, Long value) throws Exception {
        int divPos = url.indexOf(";"); 
        String firstPart = url.substring(0, divPos);
        String secondPart = url.substring(divPos + 1);
        
        Map<String, Long> params = getMapOfParameterValuesFromWebViewUrl(secondPart, false);
        params.put(paramName, value);
        String newSecondPart = getWebViewUrlFromMapOfParameterValues(params);
                
        String resultUrl = firstPart + ";" + newSecondPart;
        return resultUrl;
    }
    
    /**
     * Removes the given parameter from the URL.
     */
    public static String getAtcMapUrlWithParamRemoved(String url, String paramName) throws Exception {
        int divPos = url.indexOf("?");
        String firstPart = url.substring(0, divPos);
        String secondPart = url.substring(divPos + 1);
        
        List<NameValuePair> params = URLEncodedUtils.parse(secondPart, Charset.forName("UTF-8"));
        List<NameValuePair> modParams = new ArrayList<NameValuePair>(params.size());
        
        for (NameValuePair p : params) {
            if (!p.getName().equals(paramName)) {
                modParams.add(p);
            }
        }
        
        String resultUrl = firstPart + "?" + URLEncodedUtils.format(modParams, Charset.forName("UTF-8"));
        return resultUrl;
    }
    
    public static String getQueryStringParam(String url, String paramName) {
        if (url.contains("/#/")) {
            url = url.replace("/#/", "/");
        }
        try {
            List<NameValuePair> params = URLEncodedUtils.parse(new URI(url), Charset.forName("UTF-8"));
            for (NameValuePair p : params) {
                if (p.getName().equals(paramName)) {
                    return p.getValue();
                }
            }
        } catch (URISyntaxException use) {
            throw new IllegalArgumentException("Incorrect url syntax: " + url);
        }
        
        return null;
    }
    
    /**
     * Parse the WebView URL and return the map of parameter names to parameter values.
     *  
     * <pre>
     * The URL scheme is <firstPart>#home;<par1Name>=<par1Value>;....;<parXName>=<parXValue>.
     * </pre>
     * 
     * @param webViewUrl
     * @param skipFirstPart 
     * @return
     */
    public static Map<String, Long> getMapOfParameterValuesFromWebViewUrl(String webViewUrl, boolean skipFirstPart) {
        Map<String, Long> res = new HashMap<String, Long>();
        String[] parts = webViewUrl.split(";");
        int startIndex = skipFirstPart ? 1 : 0;
        for (int i = startIndex; i < parts.length; i++) {
            String[] nv = parts[i].split("=");
            res.put(nv[0], Long.valueOf(nv[1]));
        }
        return res;
    }
    
    public static String getWebViewUrlFromMapOfParameterValues(Map<String, Long> map) {
        StringBuilder s = new StringBuilder();
        boolean first = true;
        for (Entry<String, Long> e : map.entrySet()) {
            if (first) {
                first = false;
            } else {
                s.append(";");
            }
            
            s.append(e.getKey()).append("=").append(e.getValue());
        }
        
        return s.toString();
    }
}
