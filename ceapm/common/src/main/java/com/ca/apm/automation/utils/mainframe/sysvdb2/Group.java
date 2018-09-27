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
 * Represents a SYSVDB2 instance group.
 * A group in this context identifies the purpose of the instance and the related port/passticket
 * information.
 */
public enum Group {
    PRODUCTION(6600, "CATICKET", "66"), DEVELOPMENT(7700, "DB2TOOLS", "77"), QA(8800, "CATICKET",
        "88");

    public final int port;
    public final String passticket;
    public final String taskSuffix;

    private Group(int port, String passticket, String taskSuffix) {
        this.port = port;
        this.passticket = passticket;
        this.taskSuffix = taskSuffix;
    }
}
