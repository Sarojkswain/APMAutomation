/*
 * Copyright (c) 2016 CA. All rights reserved.
 * 
 * This software and all information contained therein is confidential and
 * proprietary and shall not be duplicated, used, disclosed or disseminated in
 * any way except as authorized by the applicable license agreement, without
 * the express written permission of CA. All authorized reproductions must be
 * marked with this language.
 * 
 * EXCEPT AS SET FORTH IN THE APPLICABLE LICENSE AGREEMENT, TO THE EXTENT
 * PERMITTED BY APPLICABLE LAW, CA PROVIDES THIS SOFTWARE WITHOUT WARRANTY OF
 * ANY KIND, INCLUDING WITHOUT LIMITATION, ANY IMPLIED WARRANTIES OF
 * MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE. IN NO EVENT WILL CA BE
 * LIABLE TO THE END USER OR ANY THIRD PARTY FOR ANY LOSS OR DAMAGE, DIRECT OR
 * INDIRECT, FROM THE USE OF THIS SOFTWARE, INCLUDING WITHOUT LIMITATION, LOST
 * PROFITS, BUSINESS INTERRUPTION, GOODWILL, OR LOST DATA, EVEN IF CA IS
 * EXPRESSLY ADVISED OF SUCH LOSS OR DAMAGE.
 */

package com.ca.apm.browseragent.testsupport.collector.pojo;


import java.util.Arrays;

/**
 * Attrs POJO for a Configuration
 *
 * @author Doug Briere
 */


public class Attributes {

    private boolean browserAgentEnabled;
    private boolean pageLoadMetricsEnabled;
    private long pageLoadMetricsThreshold;
    private boolean ajaxMetricsEnabled;
    private long ajaxMetricsThreshold;
    private boolean jsFunctionMetricsEnabled;
    private long jsFunctionMetricsThreshold;
    private boolean geoEnabled;
    private String[] urlExcludeList;
    private String[] urlIncludeList;
    private long metricFrequency;
    private boolean jsErrorsEnabled;
    private boolean ajaxErrorsEnabled;
    private boolean browserLoggingEnabled;
    private String collectorUrl;
    private boolean softPageMetricsEnabled;
    private long domChangeTimeout;
    private long domChangePollingInterval;
    private long sessionTimeout;
    private boolean geoHighAccuracyEnabled;
    private long geoMaximumAge;
    private long geoTimeout;
    private boolean urlMetricOff;
    private boolean cookieCaptureEnabled;



    public boolean isBrowserAgentEnabled() {
        return browserAgentEnabled;
    }

    public void setBrowserAgentEnabled(boolean browserAgentEnabled) {
        this.browserAgentEnabled = browserAgentEnabled;
    }

    public boolean isPageLoadMetricsEnabled() {
        return pageLoadMetricsEnabled;
    }

    public void setPageLoadMetricsEnabled(boolean pageLoadMetricsEnabled) {
        this.pageLoadMetricsEnabled = pageLoadMetricsEnabled;
    }

    public long getPageLoadMetricsThreshold() {
        return pageLoadMetricsThreshold;
    }

    public void setPageLoadMetricsThreshold(long pageLoadMetricsThreshold) {
        this.pageLoadMetricsThreshold = pageLoadMetricsThreshold;
    }

    public boolean isAjaxMetricsEnabled() {
        return ajaxMetricsEnabled;
    }

    public void setAjaxMetricsEnabled(boolean ajaxMetricsEnabled) {
        this.ajaxMetricsEnabled = ajaxMetricsEnabled;
    }

    public long getAjaxMetricsThreshold() {
        return ajaxMetricsThreshold;
    }

    public void setAjaxMetricsThreshold(long ajaxMetricsThreshold) {
        this.ajaxMetricsThreshold = ajaxMetricsThreshold;
    }

    public boolean isJsFunctionMetricsEnabled() {
        return jsFunctionMetricsEnabled;
    }

    public void setJsFunctionMetricsEnabled(boolean jsFunctionMetricsEnabled) {
        this.jsFunctionMetricsEnabled = jsFunctionMetricsEnabled;
    }

    public long getJsFunctionMetricsThreshold() {
        return jsFunctionMetricsThreshold;
    }

    public void setJsFunctionMetricsThreshold(long jsFunctionMetricsThreshold) {
        this.jsFunctionMetricsThreshold = jsFunctionMetricsThreshold;
    }

    public boolean isGeoEnabled() {
        return geoEnabled;
    }

    public void setGeoEnabled(boolean geoEnabled) {
        this.geoEnabled = geoEnabled;
    }

    public String[] getUrlExcludeList() {
        return urlExcludeList;
    }

    public void setUrlExcludeList(String[] urlExcludeList) {
        this.urlExcludeList = urlExcludeList;
    }

    public String[] getUrlIncludeList() {
        return urlIncludeList;
    }

    public void setUrlIncludeList(String[] urlIncludeList) {
        this.urlIncludeList = urlIncludeList;
    }

    public long getMetricFrequency() {
        return metricFrequency;
    }

    public void setMetricFrequency(long metricFrequency) {
        this.metricFrequency = metricFrequency;
    }

    public boolean isJsErrorsEnabled() {
        return jsErrorsEnabled;
    }

    public void setJsErrorsEnabled(boolean jsErrorsEnabled) {
        this.jsErrorsEnabled = jsErrorsEnabled;
    }

    public boolean isAjaxErrorsEnabled() {
        return ajaxErrorsEnabled;
    }

    public void setAjaxErrorsEnabled(boolean ajaxErrorsEnabled) {
        this.ajaxErrorsEnabled = ajaxErrorsEnabled;
    }

    public boolean isBrowserLoggingEnabled() {
        return browserLoggingEnabled;
    }

    public void setBrowserLoggingEnabled(boolean browserLoggingEnabled) {
        this.browserLoggingEnabled = browserLoggingEnabled;
    }

    public String getCollectorUrl() {
        return collectorUrl;
    }

    public void setCollectorUrl(String collectorUrl) {
        this.collectorUrl = collectorUrl;
    }

    public boolean getSoftPageMetricsEnabled() {
        return this.softPageMetricsEnabled;
    }

    public void setSoftPageMetricsEnabled(boolean softPageMetricsEnabled) {
        this.softPageMetricsEnabled = softPageMetricsEnabled;
    }

    public long getDomChangeTimeout() {
        return this.domChangeTimeout;
    }

    public void setDomChangeTimeout(long domChangeTimeout) {
        this.domChangeTimeout = domChangeTimeout;
    }

    public long getDomChangePollingInterval() {
        return this.domChangePollingInterval;
    }

    public void setDomChangePollingInterval(long domChangePollingInterval) {
        this.domChangePollingInterval = domChangePollingInterval;
    }

    public long getSessionTimeout() {
        return this.sessionTimeout;
    }

    public void setSessionTimeout(long sessionTimeout) {
        this.sessionTimeout = sessionTimeout;
    }

    public boolean getGeoHighAccuracyEnabled() {
        return this.geoHighAccuracyEnabled;
    }

    public void setGeoHighAccuracyEnabled(boolean geoHighAccuracyEnabled) {
        this.geoHighAccuracyEnabled = geoHighAccuracyEnabled;
    }

    public long getGeoMaximumAge() {
        return this.geoMaximumAge;
    }

    public void setGeoMaximumAge(long geoMaximumAge) {
        this.geoMaximumAge = geoMaximumAge;
    }

    public long getGeoTimeout() {
        return this.geoTimeout;
    }

    public void setGeoTimeout(long geoTimeout) {
        this.geoTimeout = geoTimeout;
    }

    public boolean getUrlMetricOff() {
        return this.urlMetricOff;
    }

    public void setUrlMetricOff(boolean urlMetricOff) {
        this.urlMetricOff = urlMetricOff;
    }

    public boolean isCookieCaptureEnabled() {
        return cookieCaptureEnabled;
    }

    public void setCookieCaptureEnabled(boolean cookieCaptureEnabled) {
        this.cookieCaptureEnabled = cookieCaptureEnabled;
    }


    @Override
    public String toString() {
        return "Attributes [browserAgentEnabled=" + browserAgentEnabled
            + ", pageLoadMetricsEnabled=" + pageLoadMetricsEnabled + ", pageLoadMetricsThreshold="
            + pageLoadMetricsThreshold + ", ajaxMetricsEnabled=" + ajaxMetricsEnabled
            + ", ajaxMetricsThreshold=" + ajaxMetricsThreshold + ", jsFunctionMetricsEnabled="
            + jsFunctionMetricsEnabled + ", jsFunctionMetricsThreshold="
            + jsFunctionMetricsThreshold + ", geoEnabled=" + geoEnabled + ", urlExcludeList="
            + Arrays.toString(urlExcludeList) + ", urlIncludeList="
            + Arrays.toString(urlIncludeList) + ", metricFrequency=" + metricFrequency
            + ", jsErrorsEnabled=" + jsErrorsEnabled + ", ajaxErrorsEnabled=" + ajaxErrorsEnabled
            + ", browserLoggingEnabled=" + browserLoggingEnabled + ", collectorUrl=" + collectorUrl
            + ", softPageMetricsEnabled=" + softPageMetricsEnabled + ", domChangeTimeout="
            + domChangeTimeout + ", domChangePollingInterval=" + domChangePollingInterval
            + ", sessionTimeout=" + sessionTimeout + ", geoHighAccuracyEnabled="
            + geoHighAccuracyEnabled + ", geoMaximumAge=" + geoMaximumAge + ", geoTimeout="
            + geoTimeout + ", urlMetricOff=" + urlMetricOff + ", cookieCaptureEnabled="
            + cookieCaptureEnabled + "]";
    }

}
