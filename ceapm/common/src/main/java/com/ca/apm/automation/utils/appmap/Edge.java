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

package com.ca.apm.automation.utils.appmap;

import java.util.Map;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

/**
 * {
 *  "sourceId":68,
 *  "targetId":71,
 *  "businessTransactionId":null,
 *  "topologicalChange":"NOT_CHANGED"
 * }
 */
public class Edge {
    protected static final String M_SOURCE = "sourceId";
    protected static final String M_TARGET = "targetId";

    Vertex source;
    Vertex target;

    Edge(Vertex source, Vertex target) {
        this.source = source;
        this.target = target;
    }

    static Edge fromJsonObject(JsonObject oEdge, Map<String, Vertex> vertices) {
        Vertex source = null;
        Vertex target = null;
        for (Map.Entry<String, JsonElement> member : oEdge.entrySet()) {
            final JsonElement element = member.getValue();
            switch (member.getKey()) {
                case M_SOURCE:
                    assert element.isJsonPrimitive();
                    source = vertices.get(element.getAsString());
                    break;

                case M_TARGET:
                    assert element.isJsonPrimitive();
                    target = vertices.get(element.getAsString());
                    break;

                default:
                    break;
            }
        }

        if (source == null || target == null) {
            return null;
        }

        source.addCallee(target);
        target.addCaller(source);
        return new Edge(source, target);
    }

    Vertex getSource() {
        return source;
    }

    Vertex getTarget() {
        return target;
    }

    @Override
    public String toString() {
        return source.toString() + " => " + target.toString();
    }
}
