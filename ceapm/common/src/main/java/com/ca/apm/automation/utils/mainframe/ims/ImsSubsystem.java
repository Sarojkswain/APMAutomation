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

package com.ca.apm.automation.utils.mainframe.ims;

import org.apache.commons.lang3.text.WordUtils;

public enum ImsSubsystem {
    INTER_REGION_LOCK_MANAGER("RL1", null),
    STRUCTURED_CALL_INTERFACE("SCI", "ALREADY ACTIVE ON OS IMAGE"),
    OPERATIONS_MANAGER("OM", "IMSPLEX INITIALIZATION ERROR "),
    RESOURCE_MANAGER("RM", "IMSPLEX INITIALIZATION ERROR "),
    CONTROL_REGION("CR1", null),
    MESSAGE_PROCESSING_REGION("M11", null),
    IMS_CONNECT("HWS", null),
    TRIGGER_MONITOR("MQB", null),
    ;

    /**
     * Prefix used for all IMS task names. Currently common for all IMS taks.
     */
    private static final String TASK_PREFIX = "SVD";

    /**
     * Task name suffix specific to the subsystem.
     */
    private final String taskSuffix;

    /**
     * Message that, when found in the task output, identifies that the particular IMS subsystem is
     * already available on the LPAR.
     * <p>If {@code null} the subsystem is considered not shared.
     */
    private final String sharedDetectionMessage;

    ImsSubsystem(String taskSuffix, String sharedDetectionMessage) {
        this.taskSuffix = taskSuffix;
        this.sharedDetectionMessage = sharedDetectionMessage;
    }

    public String getTaskName(String version) {
        return TASK_PREFIX + version + taskSuffix;
    }

    public boolean isShared() {
        return sharedDetectionMessage != null;
    }

    public String getSharedDetectionMessage() {
        return sharedDetectionMessage;
    }

    public String getName() {
        return WordUtils.capitalizeFully(name(), new char[] {'_'}).replaceAll("_", " ");
    }
}
