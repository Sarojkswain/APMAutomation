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

import org.apache.http.util.Args;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * Contains dependant component data gathered from a role discovery
 *
 * @author Jan Pojer (pojja01@ca.com)
 */
public class ComponentData implements Comparable<ComponentData> {

    /**
     * refers to ComponentType
     */
    private String type;
    /**
     * hostname or IP
     */
    private String host;
    /**
     * Component port
     */
    private Integer port;

    public ComponentData() {
    }

    public ComponentData(final String host, final Integer port, final String type) {
        Args.notNull(host, "Component host cannot be null");
        Args.notNull(type, "Component type cannot be null");
        Args.notNull(port, "Component port cannot be null");
        this.host = host;
        this.port = port;
        this.type = type;
    }

    public String getHost() {
        return this.host;
    }

    public void setHost(final String host) {
        this.host = host;
    }

    public Integer getPort() {
        return this.port;
    }

    public void setPort(final Integer port) {
        this.port = port;
    }

    public String getType() {
        return this.type;
    }

    public void setType(final String type) {
        this.type = type;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        final ComponentData that = (ComponentData) o;
        return Objects.equals(this.type, that.type) &&
                Objects.equals(this.host, that.host) &&
                Objects.equals(this.port, that.port);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.type, this.host, this.port);
    }

    @Override
    public int compareTo(@NotNull final ComponentData o) {
        return this.host.compareTo(o.getHost());
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("ComponentData{");
        sb.append("type='").append(type).append('\'');
        sb.append(", host='").append(host).append('\'');
        sb.append(", port=").append(port);
        sb.append('}');
        return sb.toString();
    }
}
