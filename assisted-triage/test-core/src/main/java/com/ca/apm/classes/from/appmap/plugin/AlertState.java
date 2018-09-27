/*
 * Copyright (c) 2014 CA. All rights reserved.
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

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;


@JsonIgnoreProperties(ignoreUnknown = true)
public class AlertState {
    
    public enum Status {
        UNKNOWN(0), OK(1), CAUTION(2), DANGER(3), INFO(4);
        
        private final int value;
        
        private Status(int value) {
            this.value = value;
        }
        
        public int getValue() {
            return value;
        }
        
        public static Status fromValue(int statusValue) {
            for (Status s : Status.values()) {
                if (s.value == statusValue) {
                    return s;
                }
            }
            return null;
        }
    }

    public enum Trend {
        DOWN(1), STALL(2), UP(3);
        private final int value;

        private Trend(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }

        public static Trend fromValue(int trendValue) {
            for (Trend t : Trend.values()) {
                if (t.value == trendValue) {
                    return t;
                }
            }
            return null;
        }
    }

    private static final int TREND_BIT_WIDTH = 2;
    private static final int DAMAGE_BIT_WIDTH = 8;
    private static final int STATUS_BIT_WIDTH = 8;
    private static final int UVB_BIT_WIDTH = 1;



    private static int computeMaskForWidth(int bitWidth) {
        return (1 << bitWidth) - 1;
    }


    public static final int TREND_SHIFT = 0;
    public static final int DAMAGE_SHIFT = TREND_SHIFT + TREND_BIT_WIDTH;
    public static final int STATUS_SHIFT = DAMAGE_SHIFT + DAMAGE_BIT_WIDTH;
    public static final int UVB_SHIFT = STATUS_SHIFT + STATUS_BIT_WIDTH;

    public static final int TREND_MASK = computeMaskForWidth(TREND_BIT_WIDTH);
    public static final int DAMAGE_MASK = computeMaskForWidth(DAMAGE_BIT_WIDTH);
    public static final int STATUS_MASK = computeMaskForWidth(STATUS_BIT_WIDTH);
    public static final int UVB_MASK = computeMaskForWidth(UVB_BIT_WIDTH);

    private int alertId;
    private int state;
    private int damageFactor;
    private boolean uvb;
    private Trend trend;
    private long startTime;
    private String alertName;
    private String metricName;

    public AlertState() {
    }
    
    public AlertState(int alertId, int encodedState, String alertName, long startTime,
                      String metricName) {
        this.alertId = alertId;
        this.state = decodeStatus(encodedState);
        this.damageFactor = decodeDamageFactor(encodedState);
        this.trend = decodeTrend(encodedState);
        this.uvb = decodeUvb(encodedState);
        this.alertName = alertName;
        this.startTime = startTime;
        this.metricName = metricName;
    }

    public AlertState(int alertId, int state, int damageFactor, boolean uvb, Trend trend,
            String alertName, long startTime) {
        this.alertId = alertId;
        this.state = state;
        this.damageFactor = damageFactor;
        this.uvb = uvb;
        this.trend = trend;
        this.alertName = alertName;
        this.startTime = startTime;
    }

    public int getAlertId() {
        return alertId;
    }

    public void setAlertId(int id) {
        alertId = id;
    }

    public int getState() {
        return state;
    }

    public void setState(int s) {
        state = s;
    }

    public int getDamageFactor() {
        return damageFactor;
    }

    public void setDamageFactor(int f) {
        damageFactor = f;
    }

    public boolean isUvb() {
        return uvb;
    }

    public void setUvb(boolean u) {
        uvb = u;
    }

    public Trend getTrend() {
        return trend;
    }

    public void setTrend(Trend t) {
        trend = t;
    }

    public long getStartTime() {
        return startTime;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public String getAlertName() {
        return alertName;
    }

    public void setAlertName(String name) {
        alertName = name;
    }

    public String getMetricName() {
        return metricName;
    }

    public void setMetricName(String metricName) {
        this.metricName = metricName;
    }

    private static int decode(int value, int shift, int mask) {
        return (value >> shift) & mask;
    }

    public static int decodeStatus(int dbStatus) {
        return decode(dbStatus, STATUS_SHIFT, STATUS_MASK);
    }

    public static int decodeDamageFactor(int dbStatus) {
        return decode(dbStatus, DAMAGE_SHIFT, DAMAGE_MASK);
    }

    public static boolean decodeUvb(int dbStatus) {
        return decode(dbStatus, UVB_SHIFT, UVB_MASK) != 0;
    }

    public static Trend decodeTrend(int dbStatus) {
        return Trend.fromValue(decode(dbStatus, TREND_SHIFT, TREND_MASK));
    }

    public static int encodeToDbStatus(int status, int damageFactor, boolean uvb, Trend trend) {
        int result = 0;
        result |= (status & STATUS_MASK) << STATUS_SHIFT;
        result |= (damageFactor & DAMAGE_MASK) << DAMAGE_SHIFT;
        if (trend != null) {
            result |= (trend.getValue() & TREND_MASK) << TREND_SHIFT;
        }
        if (uvb) {
            result |= 1 << UVB_SHIFT;
        }
        return result;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + alertId;
        result = prime * result + ((alertName == null) ? 0 : alertName.hashCode());
        result = prime * result + damageFactor;
        result = prime * result + (int) (startTime ^ (startTime >>> 32));
        result = prime * result + state;
        result = prime * result + ((trend == null) ? 0 : trend.hashCode());
        result = prime * result + (uvb ? 1231 : 1237);
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        AlertState other = (AlertState) obj;
        if (alertId != other.alertId) {
            return false;
        }
        if (alertName == null) {
            if (other.alertName != null) {
                return false;
            }
        } else if (!alertName.equals(other.alertName)) {
            return false;
        }
        if (damageFactor != other.damageFactor) {
            return false;
        }
        if (startTime != other.startTime) {
            return false;
        }
        if (state != other.state) {
            return false;
        }
        if (trend != other.trend) {
            return false;
        }
        if (uvb != other.uvb) {
            return false;
        }
        return true;
    }

    @JsonIgnore
    public boolean isNullStatus() {
        return state == VertexStatus.NULL_STATUS;
    }

    @Override
    public String toString() {
        return "AlertState [alertId=" + alertId + ", alertName=" + alertName + ", metricName="
            + metricName + "]";
    }


}
