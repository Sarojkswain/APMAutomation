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

/**
 * Browser Agent automation - Stop Watch utility
 *
 * @author Legacy BRTM automation code
 * 
 */

package com.ca.apm.tests.utils;

/**
 * Calculates the difference between time between start and stop.
 * Works similar to a Stop Watch.
 * 
 * @author mulmu01
 *
 */
public class StopWatch {

    private long startTime = 0;
    private long stopTime = 0;
    private boolean running = false;


    public void start() {
        this.startTime = System.currentTimeMillis();
        this.running = true;
    }


    public void stop() {
        this.stopTime = System.currentTimeMillis();
        this.running = false;
    }


    /**
     * Calculates the difference between start and stop.
     * 
     * @return elapsed time in milleseconds
     */
    public double getElapsedTime() {
        long elapsed;
        if (running) {
            elapsed = (System.currentTimeMillis() - startTime);
        } else {
            elapsed = (stopTime - startTime);
        }
        return Double.parseDouble(String.valueOf(elapsed));
    }


    /**
     * Calculates the difference between start and stop.
     * 
     * @return elapsed time in seconds
     */
    public long getElapsedTimeSecs() {
        long elapsed;
        if (running) {
            elapsed = ((System.currentTimeMillis() - startTime) / 1000);
        } else {
            elapsed = ((stopTime - startTime) / 1000);
        }
        return elapsed;
    }

    /**
     * Calculates the difference between start and stop.
     * 
     * @return elapsed time in seconds
     */
    public long getElapsedTimeinMinutes() {
        long elapsed;
        if (running) {
            elapsed = ((System.currentTimeMillis() - startTime) / 60000);
        } else {
            elapsed = ((stopTime - startTime) / 60000);
        }
        if (elapsed == 0) elapsed = 1;// ROund of to 1 minute by default
        return elapsed;
    }

    /*
     * // This is for test.
     * // Usage Exmaple:
     * public static void main(String[] args) {
     * StopWatch s = new StopWatch();
     * s.start();
     * try {
     * System.out.println("Sleeping for 10 seconds ... do not disturb");
     * Thread.sleep(50000);
     * } catch (InterruptedException e) {
     * e.printStackTrace();
     * }
     * s.stop();
     * System.out.println("elapsed time in milliseconds: " + s.getElapsedTimeinMinutes());
     * }
     */

}
