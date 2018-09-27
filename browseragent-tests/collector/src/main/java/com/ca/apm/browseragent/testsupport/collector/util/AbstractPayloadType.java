package com.ca.apm.browseragent.testsupport.collector.util;

/**
 * This abstract class defines a PayloadType for the waitForNextNotification method
 */

public abstract class AbstractPayloadType {

    /**
     * Allows any type of metric
     *
     */

    public static class AnyType extends AbstractPayloadType {
        public int calculateTypeCount(MetricPostRecord recordToTest) {
            return 1;
        }
    }

    /**
     * This type, is an AnyType and count will never reach zero thus timeout
     *
     */

    public static class TimeOutType extends AnyType {
        public void setCount(int count) {
            // No-op
        }

        public int getCount() {
            return Integer.MAX_VALUE;
        }
    }

    /**
     * At least one type is expected when using the type
     */

    private int expectedCount = 1;


    /**
     * Implementers should return a count for each type seen, zero if not seen.
     * 
     * @param recordToTest
     * @return
     */

    public abstract int calculateTypeCount(MetricPostRecord recordToTest);


    /**
     * The count of types expected. Default already set to 1, must call with
     * values greater than 0 otherwise IllegalArgumentException is thrown
     * 
     * @param expectedCount
     */

    public synchronized void setCount(int expectedCount) {
        if (expectedCount <= 0) {
            throw new IllegalArgumentException("expectedCount: " + expectedCount + " is invalid. "
                + " Must be greater than zero");
        }

        this.expectedCount = expectedCount;
    }

    /**
     * Returns the current count of types
     * 
     * @return int count
     */

    public synchronized int getCount() {
        return expectedCount;
    }


    /**
     * For use by the MetricCollectionContextHandler only!
     */

    public synchronized void decrementCount() {
        expectedCount -= 1;
    }
}
