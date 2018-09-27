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

package com.ca.apm.automation.utils.appmap;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

/**
 *
 * {
 *  "vertexId":68,
 *  "status": {
 *      "alertStates" : [
 *          {"alertId":0, "state":0,"damageFactor":0,"uvb":true,"trend":"STALL","durationInSec":0,"alertName":"UVB://SuperDomain|CZPRCORVUS-WAS2|WebSphere|WebSphere Agent|Variance|Differential Analysis|Default Differential Control|Servlets|JCAPutServlet:Average Response Time (ms) Variance Intensity"},
 *          {"alertId":0,"state":0,"damageFactor":0,"uvb":true,"trend":"STALL","durationInSec":0,"alertName":"UVB://SuperDomain|Custom Metric Host (Virtual)|Custom Metric Process (Virtual)|Custom Business Application Agent (Virtual)|Variance|Differential Analysis|Default Differential Control|By Frontend|CPTJCAApp|Health:Average Response Time (ms) Variance Intensity"},
 *          {"alertId":0,"state":1,"damageFactor":0,"uvb":false,"trend":"STALL","durationInSec":8,"alertName":"SuperDomain:Sample:Servlet Individual Average Response Time (ms)"}
 *      ]
 *  },
 *  "attributes" : {
 *      "owner":null,
 *      "servletMethod":"service",
 *      "hostname":"czprcorvus-was2",
 *      "agent":"CZPRCORVUS-WAS2|WebSphere|WebSphere Agent"
 *   }
 * }
 */
public class Vertex {
    public static final Map<String, Pattern> CTG_BACKEND_FILTER;
    public static final Map<String, Pattern> MQ_BACKEND_FILTER;
    public static final Map<String, Pattern> WS_BACKEND_FILTER;
    public static final Map<String, Pattern> BT_BACKEND_FILTER;
    public static final Map<String, Pattern> CICS_FILTER;
    public static final Map<String, Pattern> IMS_FILTER;
    public static final Map<String, Pattern> DB2_FILTER;
    static {
        CTG_BACKEND_FILTER = new HashMap<>();
        CTG_BACKEND_FILTER.put("type", Pattern.compile("GENERICBACKEND"));
        CTG_BACKEND_FILTER.put("name", Pattern.compile("Backends\\|CTG.* server .* program .*"));

        MQ_BACKEND_FILTER = new HashMap<>();

        MQ_BACKEND_FILTER.put("type", Pattern.compile("GENERICBACKEND"));
        MQ_BACKEND_FILTER.put("name", Pattern.compile(
            "Backends\\|WebSphereMQ.*\\|.*\\|Connector\\|Queues\\|.*\\|Put\\|Queue Put"));

        WS_BACKEND_FILTER = new HashMap<>();
        WS_BACKEND_FILTER.put("type", Pattern.compile("WEBSERVICE"));

        BT_BACKEND_FILTER = new HashMap<>();
        BT_BACKEND_FILTER.put("type", Pattern.compile("BUSINESSTRANSACTION"));

        CICS_FILTER = new HashMap<>();
        CICS_FILTER.put("type", Pattern.compile("TRANSACTION_PROCESSOR"));
        CICS_FILTER.put("transactionProcessor", Pattern.compile("CICS"));
        CICS_FILTER.put("name", Pattern.compile("CICS Region .*"));

        IMS_FILTER = new HashMap<>();
        IMS_FILTER.put("type", Pattern.compile("TRANSACTION_PROCESSOR"));
        IMS_FILTER.put("transactionProcessor", Pattern.compile("IMS"));
        IMS_FILTER.put("name", Pattern.compile("IMS Subsystem .*"));

        DB2_FILTER = new HashMap<>();
        DB2_FILTER.put("type", Pattern.compile("DATABASE"));
        DB2_FILTER.put("databaseType", Pattern.compile("DB2"));
        DB2_FILTER.put("name", Pattern.compile("DB2 Subsystem .*"));
    }

    protected static final String M_ID = "vertexId";
    protected static final String M_STATUS = "status";
    protected static final String M_ALERT_STATES = "alertStates";
    protected static final String M_ATTRIBUTES = "attributes";

    String id;
    Map<String, List<String>> attributes = new HashMap<String, List<String>>();
    List<Alert> alerts = new ArrayList<Alert>();
    List<Vertex> callees = new ArrayList<Vertex>();
    List<Vertex> callers = new ArrayList<Vertex>();

    private Vertex(String id) {
        this.id = id;
    }

    static Vertex fromJsonObject(JsonObject oVertex) {
        String vertexId = null;

        // First just look for the vertex Id
        for (Map.Entry<String, JsonElement> member : oVertex.entrySet()) {
            switch (member.getKey()) {
                case M_ID:
                    final JsonElement element = member.getValue();
                    assert element.isJsonPrimitive();
                    assert vertexId == null;

                    vertexId = element.getAsString();
                    break;

                default:
                    break;
            }
        }
        if (vertexId == null) {
            return null;
        }

        // Initialize the instance and populate it with attributes and alerts
        Vertex vertex = new Vertex(vertexId);
        for (Map.Entry<String, JsonElement> member : oVertex.entrySet()) {
            final JsonElement element = member.getValue();
            switch (member.getKey()) {
                case M_STATUS:
                    assert element.isJsonObject();
                    JsonObject status = element.getAsJsonObject();
                    JsonArray alertStates = null;
                    for (Map.Entry<String, JsonElement> m : status.entrySet()) {
                        if (m.getKey().compareTo(M_ALERT_STATES) == 0) {
                            JsonElement e = m.getValue();
                            assert e.isJsonArray();
                            alertStates = e.getAsJsonArray();
                            break;
                        }
                    }
                    for (JsonElement alertState : alertStates) {
                        assert alertState.isJsonObject();
                        vertex.addAlert(Alert.fromJsonObject(alertState.getAsJsonObject()));
                    }
                    break;

                case M_ATTRIBUTES:
                    assert element.isJsonObject();
                    JsonObject attributes = element.getAsJsonObject();
                    for (Map.Entry<String, JsonElement> attribute : attributes.entrySet()) {
                        if (attribute.getValue().isJsonNull()) {
                            continue;
                        }
                        assert attribute.getValue().isJsonArray();

                        List<String> attributeValues = new ArrayList<String>();
                        for (JsonElement value : attribute.getValue().getAsJsonArray()) {
                            assert value.isJsonPrimitive();

                            attributeValues.add(value.getAsString());
                        }

                        vertex.addAttribute(attribute.getKey(), attributeValues);
                    }
                    break;

                default:
                    break;
            }
        }

        return vertex;
    }

    public boolean matches(Map<String, Pattern> attributeFilters) {
        for (Map.Entry<String, Pattern> filter : attributeFilters.entrySet()) {
            final String attribute = filter.getKey();
            final Pattern pattern = filter.getValue();

            if (!hasAttribute(attribute)) {
                return false;
            }

            boolean matches = false;
            for (String value : getAttributeValues(attribute)) {
                if (pattern.matcher(value).matches()) {
                    matches = true;
                    break;
                }
            }

            if (!matches) {
                return false;
            }
        }

        return true;
    }

    protected void addAttribute(String name, List<String> values) {
        attributes.put(name, values);
    }

    protected void addAlert(Alert alert) {
        alerts.add(alert);
    }

    public boolean hasAttribute(String name) {
        return attributes.containsKey(name);
    }

    public List<String> getAttributeValues(String name) {
        return attributes.get(name);
    }

    public String getFirstAttributeValue(String name) {
        List<String> values = getAttributeValues(name);

        return values.get(0);
    }

    public String getId() {
        return id;
    }

    public void addCallee(Vertex callee) {
        callees.add(callee);
    }

    public Collection<Vertex> getAllCallees() {
        return callees;
    }

    public Collection<Vertex> getCalleesMatching(Map<String, Pattern> attributeFilters) {
        Collection<Vertex> matched = new ArrayList<Vertex>();

        for (Vertex callee : callees) {
            if (callee.matches(attributeFilters)) {
                matched.add(callee);
            }
        }

        return matched;
    }

    public void addCaller(Vertex caller) {
        callers.add(caller);
    }

    public Collection<Vertex> getAllCallers() {
        return callers;
    }

    public Collection<Vertex> getCallersMatching(Map<String, Pattern> attributeFilters) {
        Collection<Vertex> matched = new ArrayList<Vertex>();

        for (Vertex caller : callers) {
            if (caller.matches(attributeFilters)) {
                matched.add(caller);
            }
        }

        return matched;
    }

    public Collection<Alert> getAlerts() {
        return alerts;
    }

    @Override
    public String toString() {
        return "[" + getFirstAttributeValue("type") + ":" + getFirstAttributeValue("name") + "]";
    }
}
