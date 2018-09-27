/*
 * Copyright (c) 2014 CA. All rights reserved.
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
 * 
 * Author : JAMSA07/ SANTOSH JAMMI
 * Date : 20/11/2015
 */

package com.ca.apm.tests.common;

import org.junit.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AssertTests {

    private static final Logger LOGGER = LoggerFactory.getLogger(AssertTests.class);

    /**
     * Checks for the metric value
     * 
     * @param metricReturnValue
     */
    public void assertMetricValue(String metricReturnValue) {
        try {
            LOGGER.info("String passed is : " + metricReturnValue);

            if (metricReturnValue.equalsIgnoreCase("-1")) {
                    LOGGER.info("No value obtained from CLW command ... FAILED!!!!!!!!");
                    Assert.assertTrue(false);
            }

            else if (!metricReturnValue.equalsIgnoreCase("?")) {
                String[] values = metricReturnValue.split(":::");
                if (values.length > 1) {
                    if (values[0].equalsIgnoreCase("Integer")) {
                        LOGGER.info("This is an integer value" + values[1]);
                        assertIntegerMetricValue(values[1]);
                    } else if (values[0].equalsIgnoreCase("Float")) {
                        LOGGER.info("This is an Float value" + values[1]);
                        assertFloatMetricValue(values[1]);

                    } else if (values[0].equalsIgnoreCase("String")) {
                        LOGGER.info("This is an String value" + values[1]);
                        assertStringMetricValue(values[1]);
                    }

                    else if (values[0].equalsIgnoreCase("Date")) {
                        LOGGER.info("This is an Date value" + values[1]);
                        assertDateMetricValue(values[1]);
                    } else if (values[0].equalsIgnoreCase("long")) {
                        LOGGER.info("This is an Long value" + values[1]);
                        assertLongMetricValue(values[1]);
                    } else if (values[0].equalsIgnoreCase("Double")) {
                        LOGGER.info("This is an Double value" + values[1]);
                        assertDoubleMetricValue(values[1]);
                    }
                }
            }


        } catch (Exception e) {
            LOGGER.info("Failed !!! There is no return value");
        }
    }

    /**
     * Checks for the metric value
     * 
     * @param metricReturnValue
     */

    public void assertIntegerMetricValue(String metricReturnValue) {
        try {
            LOGGER.info("Value passed is : " + metricReturnValue);

            int metricValue = Integer.parseInt(metricReturnValue);
            if (metricValue >= 0)
                Assert.assertTrue(true);
            else {
                try {
                    Assert.assertTrue(false);
                } catch (Exception e) {
                    LOGGER.info("Invalid metric value");
                } catch (Error er) {
                    er.printStackTrace();
                }
            }
        } catch (Exception e) {
            LOGGER.info("Failed !!! There is no return value");
        }
    }

    /**
     * Checks for the metric value
     * 
     * @param metricReturnValue
     */

    public void assertFloatMetricValue(String metricReturnValue) {
        try {
            LOGGER.info("Value passed is : " + metricReturnValue);

            float metricValue = Float.parseFloat(metricReturnValue);
            if (metricValue >= 0.0F)
                Assert.assertTrue(true);
            else {
                try {
                    Assert.assertTrue(false);
                } catch (Exception e) {
                    LOGGER.info("Invalid metric value");
                } catch (Error er) {
                    er.printStackTrace();
                }
            }
        } catch (Exception e) {
            LOGGER.info("Failed !!! There is no return value");
        }
    }

    /**
     * Checks for the metric value
     * 
     * @param metricReturnValue
     */

    public void assertLongMetricValue(String metricReturnValue) {
        try {
            LOGGER.info("Value passed is : " + metricReturnValue);

            long metricValue = Long.parseLong(metricReturnValue);
            if (metricValue >= 0)
                Assert.assertTrue(true);
            else {
                try {
                    Assert.assertTrue(false);
                } catch (Exception e) {
                    LOGGER.info("Invalid metric value");
                } catch (Error er) {
                    er.printStackTrace();
                }
            }
        } catch (Exception e) {
            LOGGER.info("Failed !!! There is no return value");
        }
    }

    /**
     * Checks for the metric value
     * 
     * @param metricReturnValue
     */

    public void assertDateMetricValue(String metricReturnValue) {
        // TODO: Will be implemented when required
    }

    /**
     * Checks for the metric value
     * 
     * @param metricReturnValue
     */

    public void assertDoubleMetricValue(String metricReturnValue) {
        try {
            LOGGER.info("Value passed is : " + metricReturnValue);

            double metricValue = Double.parseDouble(metricReturnValue);
            if (metricValue >= 0)
                Assert.assertTrue(true);
            else {
                try {
                    Assert.assertTrue(false);
                } catch (Exception e) {
                    LOGGER.info("Invalid metric value");
                } catch (Error er) {
                    er.printStackTrace();
                }
            }
        } catch (Exception e) {
            LOGGER.info("Failed !!! There is no return value");
        }
    }

    /**
     * Checks for the metric value
     * 
     * @param metricReturnValue
     */

    public void assertStringMetricValue(String metricReturnValue) {
        LOGGER.info("Value passed is : " + metricReturnValue);
        Assert.assertTrue(true);
    }
}
