package com.ca.apm.tests.utils;

import java.util.HashMap;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;

import com.ca.apm.tests.common.introscope.util.CLWBean;
import com.ca.apm.tests.common.introscope.util.CLWResult;
import com.ca.apm.tests.common.introscope.util.MetricUtil;

//methods were cloned from BaseAgentTest class v2 project
//can be moved to common project later if needed
public class MetricsUtil {

    public static CLWBean clw = null;
    public enum MetricValueType {MIN, MAX, AVG};
    private static final Logger LOGGER = LoggerFactory.getLogger(MetricsUtil.class);
   
	/**
     * Retrieves clw value (supports numbers only)
     *
     * @param metric metric expression
     * @param minutes number of minutes to get historical data for
     * @param average either to get average value (true) or sum of values (false)
     * @return avg value or sum of values (if aggregate is false). In case no data returned or all values are null this method will return -1
     * @throws Exception
     */
    public static long getClwData(String metric, int minutes, boolean average) throws Exception {
         return getClwData(metric, minutes, average, MetricValueType.AVG);
    }

    /**
     * Retrieves clw value (supports numbers only)
     *
     * @param metric metric expression
     * @param minutes number of minutes to get historical data for
     * @param average either to get average value (true) or sum of values (false)
     * @param getMinValue retrieve min value from the time slices
     * @param getMaxValue retrieve max value from the time slices
     * @return avg value or sum of values (if aggregate is false). In case no data returned or all values are null this method will return -1
     * @throws Exception
     */
    public static long getClwData(String metric, int minutes, boolean average, MetricValueType valueType) throws Exception {

        //TODO clw null check
        if (minutes == 0) return getClwData(metric); //return last time slice value only

        int initValue = -1;
        long actualValue = 0;
        int countValues = 0;
        long totalTimeSliceValues = 0;
        List<CLWResult> data = (new MetricUtil (metric, clw)).getLastNMinutesMetricResults(minutes);

        try {
            if (data != null && data.size() != 0) {
                for (CLWResult clwResult: data) {
                    String value = null;

                    switch (valueType) {
                        case MIN: value = clwResult.getMINValue(); break;
                        case MAX: value = clwResult.getMAXValue(); break;
                        case AVG: value = clwResult.getValue(); break;
                        default : value = clwResult.getValue(); break;
                    }

                    if (value != null) {
                        initValue = 0;
                        if (Long.parseLong(value) != 0) {
                            actualValue += Long.parseLong(value);
                            long count = Long.parseLong(clwResult.getValueCount());
                            totalTimeSliceValues += Long.parseLong(value) * count;  //computed as value * count for each time slice
                            countValues += count;                                   //sum of all value counts to be used for average calculation
                        }
                    }
                }
                if (average && actualValue != 0) {
                    if (countValues == 0) {
                        Assert.fail("Unable to compute average for metric " + metric + " as total 'Value Count' value is 0!");
                    }
                    actualValue = Math.round((double)totalTimeSliceValues/(double)countValues);
                }
            }

        } catch (NumberFormatException e) {
            throw new Exception ("\ngetClwData method is used to retrieve numeric data only!", e);
        }

        return initValue + actualValue;
    }

    /**
     * Retrieves last data point clw value (supports numbers only)
     *
     * @param metric
     * @return
     * @throws Exception
     */
    public static long getClwData (String metric) throws Exception {

        //TODO clw null check
        String data = (new MetricUtil (metric, clw)).getMetricValue();

        try {
            if (data != null) {
                return Long.parseLong(data);
            }
        } catch (NumberFormatException e) {
            throw new Exception ("\ngetClwData method is used to retrieve numeric data only!", e);
        }

        return -1;
    }

    /**
     * Verifies string metrics by comparing expected vs actual values
     *
     * @param params key/value pairs
     * @param metric metric name to verify
     * @param minutes number of minutes to get historical data for
     * @throws Exception
     */
    @SuppressWarnings("null")
    public static void verifyStringMetrics (HashMap<String, String> params, String metric, int minutes) throws Exception {

        List<CLWResult> data = (new MetricUtil (metric, clw)).getLastNMinutesMetricResults(minutes);
        Assert.assertFalse(data == null, "CLW Data is null. It doesn't contain any results");

        if (params.get("metricExists") != null) {
            LOGGER.info ("[" + params.get("testname") + "] checking metric existence.");

            if (Boolean.parseBoolean(params.get("metricExists"))) {
                Assert.assertTrue (data.size() > 0, "Metric " + metric + " doesn't exist");
            }
            else {
                Assert.assertTrue (data.size() == 0, "Metric " + metric + " exists" );
            }
        }
        else  if(params.get("expectedValue") != null) {
            Assert.assertFalse(data.size() == 0, "CLW Data size is 0. It doesn't contain any results");

            boolean foundMatch = false;
            resultset_loop: for (CLWResult clwResult: data) {

                Assert.assertNotNull(clwResult, "CLW result set is null for metric " + metric);
                Assert.assertNotNull(clwResult.getValue(), "CLW returned value is null for metric " + metric);

                for(String expectedValue: params.get("expectedValue").split(";")) {
                    if(clwResult.getValue().contains(expectedValue)) {
                        foundMatch = true; break resultset_loop;
                    }
                }
            }
            Assert.assertTrue(foundMatch, "Expected and actual values don't match for string metric: " + metric);
        }
    }
    
    /**
     * Verifies number metrics by comparing expected vs actual values.
     * If 'metricExists' key presents, it will just check metric existence.
     *
     * @param params key/value pairs. Required keys: testname, metricExpr,
     *               agentExpr, agentProcess, average (true|false), expectedValue|minExpectedValue|maxExpectedValue,
     *               delta (if expectedValue provided)
     * @param metric metric name to verify
     * @param minutes number of minutes to get historical data for
     * @param valueType metric value type
     * @throws Exception
     */
    public static void verifyNumberMetrics (HashMap<String, String> params, 
                                            String metric, 
                                            int minutes, 
                                            MetricValueType valueType) throws Exception {

        // checks for negative values (mostly used in deviation metric checks)
        if (Boolean.parseBoolean(params.get("metricSupportedNegativeExists"))){
            boolean negativeMetric = (new MetricUtil(metric, clw)).metricExists();
            LOGGER.info("Checking for Negative Metric existance, Method returned : " + negativeMetric);
            Assert.assertTrue (negativeMetric, "Metric " + metric + " doesn't exist");
            return;
        }
        long actualValue = getClwData(metric, minutes, Boolean.parseBoolean(params.get("average")), valueType);

        if (params.get("metricExists") != null) {
            LOGGER.info ("[" + params.get("testname") + "] checking metric existence.");
            if (Boolean.parseBoolean(params.get("metricExists"))) {
                Assert.assertTrue (actualValue >=0, "Metric " + metric + " doesn't exist, actualValue: " + actualValue);
            }
            else {
                Assert.assertTrue (actualValue == -1, "Metric " + metric + " exists, actualValue: " + actualValue);
            }
        }
        else if (params.get("expectedValue") != null) {

            long expValue = Long.parseLong(params.get("expectedValue"));
            double delta;

            if (params.get("deltaPercentage") != null) {
                delta = Long.parseLong(params.get("expectedValue")) * Double.parseDouble(params.get("deltaPercentage"))/100;
            }
            else {
                Assert.assertNotNull(params.get("delta"), "Unable to verify metrics as no \"delta\" value provided!");
                delta = Long.parseLong(params.get("delta"));
            }

            String result = "expected " + expValue + ", actual " + actualValue + ", delta " + delta;
            LOGGER.info ("[" + params.get("testname") + "] metrics: " + result);
            Assert.assertEquals (actualValue, expValue, delta, "Metric expected and actual values didn't match: " + result);
        }
        else if (params.get("minExpectedValue") != null) {
            long minExpValue = Long.parseLong(params.get("minExpectedValue"));
            String result = "min expected " + minExpValue + ", actual " + actualValue;
            LOGGER.info ("[" + params.get("testname") + "] metrics: " + result);
            Assert.assertTrue (actualValue >= minExpValue, "Actual value is less than min expected: " + result);
        }
        else if (params.get("maxExpectedValue") != null) {
            long maxExpValue = Long.parseLong(params.get("maxExpectedValue"));
            String result = "max expected " + maxExpValue + ", actual " + actualValue;
            LOGGER.info ("[" + params.get("testname") + "] metrics: " + result);
            Assert.assertTrue (actualValue <= maxExpValue, "Actual value is bigger than max expected: " + result);
        }
        else {
            Assert.fail("Unable to verify metrics as no expected value provided!");
        }
    }
}
