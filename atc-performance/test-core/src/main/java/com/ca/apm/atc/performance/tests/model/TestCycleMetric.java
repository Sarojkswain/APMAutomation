package com.ca.apm.atc.performance.tests.model;

/**
 *  @author Alexander Sinyushkin (sinal04@ca.com)
 */
public class TestCycleMetric {

    private int id;
    private long duration = 0;
    private long jsHeapSize = 0;
    private String errorMessage;
    private String screenshotName;

    public TestCycleMetric() {
    }

    public TestCycleMetric(String errorMsg) {
        this.errorMessage = errorMsg;
    }

    public TestCycleMetric(String errorMsg, String screenshotName) {
        this.errorMessage = errorMsg;
        this.screenshotName = screenshotName;
    }

    public TestCycleMetric(long duration, long jsHeapSize) {
        this.duration = duration;
        this.jsHeapSize = jsHeapSize;
    }

    public long getDuration() {
        return duration;
    }

    public long getJsHeapSize() {
        return jsHeapSize;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }

    public void setJsHeapSize(long jsHeapSize) {
        this.jsHeapSize = jsHeapSize;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public String getScreenshotName() {
        return screenshotName;
    }

    public void setScreenshotName(String screenshotName) {
        this.screenshotName = screenshotName;
    }
}
