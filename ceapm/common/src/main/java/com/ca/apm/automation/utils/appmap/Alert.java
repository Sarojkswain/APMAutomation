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

import java.util.Map;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

/**
 * {
 * "alertId":0,
 * "state":0,
 * "damageFactor":0,
 * "uvb":true,
 * "trend":"STALL",
 * "durationInSec":0,
 * "alertName":
 *   "UVB://SuperDomain|CZPRCORVUS-WAS2|WebSphere|WebSphere Agent|Variance|Differential Analysis
 *   |Default Differential Control|Servlets|JCAPutServlet:Average Response Time (ms) Variance Intensity"
 * }
 */
public class Alert {
    protected static final String M_STATE = "state";
    protected static final String M_UVB = "uvb";
    protected static final String M_NAME = "alertName";
    protected static final String M_DURATION = "durationInSec";
    protected static final String UVB_PREFIX = "UVB://";

    enum State {
        UNKNOWN, OK, CAUTION, DANGER, INFO;

        // See webview:apmserver.ui/app/scripts/directives/AppMapDirective.js
        public static State fromNumericValue(int value) {
            switch (value) {
                default:
                case 0:
                    return UNKNOWN;
                case 1:
                    return OK;
                case 2:
                    return CAUTION;
                case 3:
                    return DANGER;
                case 4:
                    return INFO;
            }
        }
    };

    String name;
    boolean differential;
    State state;
    int duration;

    static Alert fromJsonObject(JsonObject oVertex) {
        Alert alert = new Alert();
        for (Map.Entry<String, JsonElement> member : oVertex.entrySet()) {
            final JsonElement element = member.getValue();
            switch (member.getKey()) {
                case M_STATE:
                    assert element.isJsonPrimitive();
                    alert.setState(State.fromNumericValue(element.getAsInt()));
                    break;

                case M_UVB:
                    assert element.isJsonPrimitive();
                    alert.setDifferential(element.getAsBoolean());
                    break;

                case M_NAME:
                    assert element.isJsonPrimitive();
                    alert.setName(element.getAsString());
                    break;

                case M_DURATION:
                    assert element.isJsonPrimitive();
                    alert.setDuration(element.getAsInt());
                    break;

                default:
                    break;
            }
        }

        return alert;
    }

    void setState(State state) {
        this.state = state;
    }

    void setDifferential(boolean uvb) {
        this.differential = uvb;
    }

    void setName(String name) {
        this.name = name;
    }

    void setDuration(int duration) {
        this.duration = duration;
    }

    public String getName() {
        return name;
    }

    public boolean isDifferential() {
        return differential;
    }

    public State getState() {
        return state;
    }

    /**
     * Create regular expression pattern that matches name of differential alert with specified
     * characteristics.
     * All parameters are optional, supply null to use appropriate wildcard instead.
     *
     * @param host
     * @param process
     * @param agent
     * @param managementModule
     * @param differentialControl
     * @param metricPattern
     * @return compiled pattern
     */
    public static String getDiffAlertPattern(String host, String process, String agent,
        String managementModule, String differentialControl, String metricPattern) {
        String pattern = "UVB://SuperDomain\\|";
        if (host == null) {
            pattern += "[^|]+";
        } else {
            pattern += host;
        }
        pattern += "\\|";
        if (process == null) {
            pattern += "[^|]+";
        } else {
            pattern += process;
        }
        pattern += "\\|";
        if (agent == null) {
            pattern += "[^|]+";
        } else {
            pattern += agent;
        }
        pattern += "\\|Variance\\|";
        if (managementModule == null) {
            pattern += "[^|]+";
        } else {
            pattern += escape(managementModule);
        }
        pattern += "\\|";
        if (differentialControl == null) {
            pattern += "[^|]+";
        } else {
            pattern += escape(differentialControl);
        }
        pattern += "\\|";
        if (metricPattern == null) {
            pattern += "(.*)";
        } else {
            pattern += metricPattern;
        }
        pattern += " Variance Intensity";
        return pattern;
    }

    // DA escapes special characters in MM names
    private static String escape(String what) {
        return what.replace(':', '_').replace('|', '_');
    }

    @Override
    public String toString() {
        return "[" + (differential ? "D" : "A") + ":" + state + "|" + name + "]";
    }
}
