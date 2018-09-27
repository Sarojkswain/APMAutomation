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

package com.ca.apm.classes.from.appmap.plugin;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * A internal representation of vital information
 * related to an Actor. This will be part of ActorEntity
 * database object. This exists so that we can easily read
 * and write the JSON actor data field in actor table. Also
 * helps if we want to create internal representation without
 * dealing with database in case of AnalystReport etc.,
 *
 */
public class Actor {

    private String vertexId;
    private String name;

    @JsonCreator
    public Actor(@JsonProperty("vertexId") String vertexId, @JsonProperty("name") String name) {
        this.vertexId = vertexId;
        this.name = name;
    }

    public String getVertexId() {

        return vertexId;
    }

    public String getName() {

        return name;
    }

    @Override
    public boolean equals(Object incoming) {

        if (incoming instanceof Actor) {

            Actor actor = (Actor) incoming;

            if ((vertexId == actor.vertexId || (vertexId != null && vertexId.equals(actor.vertexId)))) {

                return true;
            }

        }

        return false;
    }

    @Override
    public int hashCode() {
        return vertexId.hashCode();
    }
}
