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

import org.apache.http.util.Args;

/**
 * Represents a SYSVDB2 version.
 * The set of versions is limited to those supported by all instance groups/hosts.
 */
public enum Version {
    R17("17.0", "17"), R18("18.0", "18"), R19("19.0", "19");

    public final String version;
    public final String taskSuffix;

    private Version(String version, String taskSuffix) {
        this.version = version;
        this.taskSuffix = taskSuffix;
    }

    /**
     * Returns an enum member matching a version string.
     * 
     * @param versionString Version string to match.
     * @return Matching enum member or null if not matched.
     */
    public static Version fromString(String versionString) {
        Args.notBlank(versionString, "versionString");

        for (Version version : Version.values()) {
            if (version.version.equalsIgnoreCase(versionString)) {
                return version;
            }
        }
        return null;
    }
}
