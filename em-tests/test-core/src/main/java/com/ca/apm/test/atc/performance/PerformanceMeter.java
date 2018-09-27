/*
 * Copyright (c) 2015 CA. All rights reserved.
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
package com.ca.apm.test.atc.performance;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.remote.RemoteWebDriver;

public class PerformanceMeter {

    RemoteWebDriver webDriver;
    
    /**
     * all loop's durations 
     */
    private List<Long> timeRecords = new ArrayList<Long>();
    
    /**
     * heap size records of all iterations,
     * i.e. total amount of memory being used by JS objects including V8 internal objects
     */
    private List<Long> heapRecords = new ArrayList<Long>();
    
    TimeMetrics timeMetrics = new TimeMetrics();
    
    HeapSizeMetrics heapMetrics = new HeapSizeMetrics();
    
    
    public PerformanceMeter(RemoteWebDriver webDriver) {
        this.webDriver = webDriver;
    }
    
    
    public void run(RepeatableTest test, int iterations) throws Exception {
        long start = new Date().getTime();
        timeMetrics.setStart(start);
        
        long end = start;        
        while(iterations-- > 0) {
            start = System.currentTimeMillis();
            test.oneLoop();
            end = System.currentTimeMillis();
            
            timeRecords.add(end - start);
            saveHeapSize();
        }
        
        timeMetrics.setEnd(end);
        calcMetrics();
    }
    
    private void saveHeapSize() {
        try {
            JavascriptExecutor jse = (JavascriptExecutor) webDriver;
            long usedHeapSize =
                    (Long) jse.executeScript(" return window.performance.memory.usedJSHeapSize");
            heapRecords.add(usedHeapSize);
        } catch (WebDriverException e) {
            // TODO: warn e
        }
    }
    
    public static long getAverageValue(List<Long> values) {
        long sum = 0; 
        for (long val : values) {
            sum += val;
        }
        return sum / values.size();
    }
    
    private void calcMetrics() {
        // time metrics
        timeMetrics.setTotal(timeMetrics.getEnd() - timeMetrics.getStart());
        if (!timeRecords.isEmpty()) {            
            timeMetrics.setMin(Collections.min(timeRecords));
            timeMetrics.setMax(Collections.max(timeRecords));
            timeMetrics.setAvg(getAverageValue(timeRecords));
        }
        
        // heap metrics
        if (!heapRecords.isEmpty()) {         
            heapMetrics.setMin(Collections.min(heapRecords));
            heapMetrics.setMax(Collections.max(heapRecords));
            heapMetrics.setAvg(getAverageValue(heapRecords));
        }
    }
    
    @Override
    public String toString() {
        long total = timeMetrics.getTotal();
        long totalMsc = total % 1000;
        long totalSec = (total / 1000) % 60;
        long totalMin = (total / 1000 * 60) % 60;
        long totalHrs = (total / 1000 * 3600) % 60;
        
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
        
        return "[TIME = "
                + "total: " + totalHrs + "h " + totalMin + "min " + totalSec + "." + totalMsc + "s"
                + " (" + timeRecords.size() + " loops); "
                + "min: " + timeMetrics.getMin() + "ms; "
                + "max: " + timeMetrics.getMax() + "ms; "
                + "avg: " + timeMetrics.getAvg() + "ms; "
                + "start: " + df.format(new Date(timeMetrics.getStart())) + "; "
                + "end: " + df.format(new Date(timeMetrics.getEnd()))

                + " | HEAP = "
                + "min: " + heapMetrics.getMin() / 1048576 + "MB; "
                + "max: " + heapMetrics.getMax() / 1048576 + "MB; "
                + "avg: " + heapMetrics.getAvg() / 1048576 + "MB ]";
    }

}
