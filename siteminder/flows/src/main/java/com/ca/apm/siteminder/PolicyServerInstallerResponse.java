/*
 * Copyright (c) 2014 CA.  All rights reserved.
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
 * MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE.  IN NO EVENT WILL CA BE
 * LIABLE TO THE END USER OR ANY THIRD PARTY FOR ANY LOSS OR DAMAGE, DIRECT OR
 * INDIRECT, FROM THE USE OF THIS SOFTWARE, INCLUDING WITHOUT LIMITATION, LOST
 * PROFITS, BUSINESS INTERRUPTION, GOODWILL, OR LOST DATA, EVEN IF CA IS
 * EXPRESSLY ADVISED OF SUCH LOSS OR DAMAGE.
 */

package com.ca.apm.siteminder;

enum PolicyServerInstallerResponse {

    SUCCESS(0),
    JAVA_MISSING(1),
    PARTIAL_SUCCESS(2),
    ERROR_IN_RESPONSE_FILE(403),
    UNKNOWN(-1);

    private int exitStatus;

    PolicyServerInstallerResponse(int exitStatus) {
        this.exitStatus = exitStatus;
    }

    public int exitStatus() {
        return exitStatus;
    }

    public static PolicyServerInstallerResponse fromExitStatus(int exitStatus) {
        for (PolicyServerInstallerResponse response : values()) {
            if (response.exitStatus == exitStatus) {
                return response;
            }
        }

        return UNKNOWN;
    }
}
