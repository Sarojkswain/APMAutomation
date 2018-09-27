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
public class LogicalActor {

    private String name;
    private String type;
    private String applicationName;
    private boolean inferred;

    public boolean isInferred() {
        return inferred;
    }

    public String getType() {
        return type;
    }

    public String getApplicationName() {
        return applicationName;
    }

    public String getName() {

        return name;
    }

    public LogicalActor(String name, String applicationName, String type) {
        this(name, applicationName, type, false);
    }
    
    @JsonCreator
    public LogicalActor(@JsonProperty("name") String name,
        @JsonProperty("applicationName") String applicationName, @JsonProperty("type") String type,
        @JsonProperty("inferred") boolean isInferred) {

        this.name = name;
        this.applicationName = applicationName;
        this.type = type;
        this.inferred = isInferred;
    }

    @Override
    public boolean equals(Object incoming) {

        if (incoming instanceof LogicalActor) {

            LogicalActor actor = (LogicalActor) incoming;

            if ((name == actor.name || (name != null && name.equals(actor.name)))) {

                if ((applicationName == actor.applicationName || (applicationName != null && applicationName
                    .equals(actor.applicationName)))) {

                    if ((type == actor.type || (type != null && type.equals(actor.type)))) {

                        return true;
                    }
                }
            }
        }

        return false;
    }

    @Override
    public int hashCode() {

        final int prime = 31;
        int result = 1;
        result = prime * result + (name == null ? 0 : name.hashCode());
        result = prime * result + (applicationName == null ? 0 : applicationName.hashCode());
        result = prime * result + (type == null ? 0 : type.hashCode());
        result = prime * result + (inferred?1:0);

        return result;
    }
}
