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

package com.ca.apm.automation.utils.mainframe.sysvdb2;

/**
 * Represents a supported SYSVDB2 instance system.
 * A system identifies the host an instance can run in, and the system name where its tasks run.
 */
public enum MfSystem {
    // TODO: Currently we are limiting ourselves to CA31 because we have no way of starting
    // Jobs on other systems.
    //CA11("ca11.ca.com", "CA11", "1"), CA31("ca31.ca.com", "CA31", "3"), CA32("ca32.ca.com", "CA32", "2");
    CA31("ca31.ca.com", "CA31", "3");

    public final String host;
    public final String name;
    public final String taskSuffix;

    private MfSystem(String host, String name, String taskSuffix) {
        this.host = host;
        this.name = name;
        this.taskSuffix = taskSuffix;
    }
}
