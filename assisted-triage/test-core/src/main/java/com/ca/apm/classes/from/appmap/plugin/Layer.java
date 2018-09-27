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

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;


/**
 * @author julro02
 *
 */
public class Layer {
    private static final ConcurrentMap<String, Layer> instances =
        new ConcurrentHashMap<String, Layer>();

    public static final Layer ATC = new Layer("ATC");
    public static final Layer UIM = new Layer("UIM");
    public static final Layer APM_INFRASTRUCTURE = new Layer("APM_INFRASTRUCTURE");
    public static final Layer INFRASTRUCTURE = new Layer("INFRASTRUCTURE");

    static {
        instances.put(ATC.getValue(), ATC);
        instances.put(UIM.getValue(), UIM);
        instances.put(APM_INFRASTRUCTURE.getValue(), APM_INFRASTRUCTURE);
        instances.put(INFRASTRUCTURE.getValue(), INFRASTRUCTURE);
    }



    private String value;

    // only for JSON deserialization
    @Deprecated
    public Layer() {}


    private Layer(String value) {
        this.value = value;
    }

    // only for JSON deserialization
    @Deprecated
    public void setValue(String value) {
        if (this.value != null) {
            throw new IllegalStateException("Method is for JSON deserialization only.");
        }
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static Layer fromValue(String value) {
        if (value == null || value.indexOf(":") != -1) {
            throw new IllegalArgumentException("Layer cannot be null or contain a colon character: " + value);
        }
        
        Layer result = instances.get(value);
        if (result != null) {
            return result;
        }        

        result = new Layer(value);
        instances.put(result.getValue(), result);
        return result;
    }

    public static Layer getDefaultLayer() {
        return ATC;
    }


    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((value == null) ? 0 : value.hashCode());
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
        Layer other = (Layer) obj;
        if (value == null) {
            if (other.value != null) {
                return false;
            }
        } else if (!value.equals(other.value)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return value;
    }


    public int compareTo(Layer layer) {
        if (this.value == null) {
            return layer.value == null ? 0 : -1;
        }
        return this.value.compareTo(layer.getValue());
    }
}
