package com.ca.apm.commons.coda.common;

/**
 * This class is used to hold the actual start time,end time and metric value
 * @author parra14
 *
 */
public class MetricData implements Comparable
{

    private String actualEndTime;
    private String actualStartTime;
    private int metricValue;
    /**
     * Method to get the actual start time
     * @return String-- actual start time
     */
    public String getActualStartTime()
    {
        return actualStartTime;
    }
    /**
     * Method to set the actual start time
     * @param actualStartTime actual start time of the metric
     */
    public void setActualStartTime(String actualStartTime)
    {
        this.actualStartTime = actualStartTime;
    }

   
    /**
     * Method to get the actual end time
     * @return String-- actual end time
     */
    public String getActualTime()
    {
        return actualEndTime;
    }
    /**
     * Method to set  the end time
     * @param actualTime end time of the metric
     */
    public void setActualTime(String actualTime)
    {
        this.actualEndTime = actualTime;
    }
    /**
     * Method to get the metric value
     * @return integer representing the metric value
     */
    public int getMetricValue()
    {
        return metricValue;
    }
    
    /**
     * Method to set the metric value
     * @param metricValue  Value of the Metric
     */
    public void setMetricValue(int metricValue)
    {
        this.metricValue = metricValue;
    }
    
    /**
     * Method to sort the object based on the Metric value
     * @param obj representing the metric data
      * @return integer value based on comparison
     */
    
    public int compareTo(Object obj) {
        
        if (obj instanceof MetricData) {
            
            MetricData metricData = (MetricData) obj;
            System.out.println("this: " + this.metricValue);
            System.out.println("get: " + metricData.getMetricValue());
            if (this.metricValue < metricData.getMetricValue())
                return 1;
            else if (this.metricValue > metricData.getMetricValue())
                return -1;
        }
        return 0;
    }

}
