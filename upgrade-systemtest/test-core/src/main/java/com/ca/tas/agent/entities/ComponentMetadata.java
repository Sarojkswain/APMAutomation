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

package com.ca.tas.agent.entities;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * Class ComponentMetadata DTO.
 * <p>
 * Description
 */
public class ComponentMetadata {
    private static final String EMPTY = "";

    private final String componentId;
    private final String installDir;
    private final String host;

    private ComponentMetadata(final String componentId, final String installDir) {

        this.componentId = componentId;
        this.installDir = installDir;
        String hostname;
        try {
            hostname = InetAddress.getLocalHost().getHostName();
        } catch (final UnknownHostException e) {
            hostname = EMPTY;
        }
        this.host = hostname;
    }

    public static ComponentMetadata empty(final String componentId) {
        return new ComponentMetadata(componentId, EMPTY);
    }

    public static ComponentMetadata withData(final String componentId, final String installDir) {
        return new ComponentMetadata(componentId, installDir);
    }

    public String getComponentId() {
        return this.componentId;
    }

    public String getInstallDir() {
        return this.installDir;
    }

    public String getHost() {
        return this.host;
    }
}
