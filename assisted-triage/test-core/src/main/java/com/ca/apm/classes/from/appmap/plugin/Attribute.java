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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@JsonIgnoreProperties(ignoreUnknown=true)
public class Attribute {
    private static final String JSON_SUFFIX = "}";
    private static final String JSON_PREFIX = "$JSON{";
    private static final ObjectMapper MAPPER = new ObjectMapper();
    private String name;
    private String value;
    private AttributeType type;
    private String owner;

    public enum AttributeType {
        GATHERED("G"),
        DECORATED("D"),
        CUSTOM("C"),
        HIDDEN("H");

        private final String dbValue;

        private AttributeType(String dbValue) {
            this.dbValue = dbValue;
        }

        public String getDbValue() {
            return this.dbValue;
        }

        public static AttributeType fromDbValue(String dbValue) {
            for (AttributeType type : values()) {
                if (type.getDbValue().equals(dbValue)) {
                    return type;
                }
            }
            return CUSTOM;
        }
    }

    public Attribute() {}

    public Attribute(String name, String value, AttributeType type) {
        super();
        this.name = name;
        this.value = value;
        this.type = type;
    }
    
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public AttributeType getType() {
        return type;
    }

    public void setType(AttributeType type) {
        this.type = type;
    }

    @Override
    public String toString() {
        return String.format("%s{%s} = %s", getName(), getType().getDbValue(),
                getValue());
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        result = prime * result + ((type == null) ? 0 : type.hashCode());
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
        Attribute other = (Attribute) obj;
        if (name == null) {
            if (other.name != null) {
                return false;
            }
        } else if (!name.equals(other.name)) {
            return false;
        }
        if (type != other.type) {
            return false;
        }
        if (value == null) {
            if (other.value != null) {
                return false;
            }
        } else if (!value.equals(other.value)) {
            return false;
        }
        return true;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public static boolean isStringArray(String value) {
        if (!isJsonObject(value)) {
            return false;
        }
        try {
            JsonNode tree = MAPPER.readTree(value.substring(JSON_PREFIX.length(), value.length() - JSON_SUFFIX.length()));
            if (tree.isArray()) {
                Iterator<JsonNode> it = tree.elements();
                while (it.hasNext()) {
                    JsonNode v = it.next();
                    if (!v.isTextual()) {
                        return false;
                    }
                }
                return true;
            }
        } catch (Exception e) {
        }
        return false;
    }
    
    @JsonIgnore
    public boolean isStringArray() {
        return isStringArray(value);
    }

    @JsonIgnore
    public boolean isJsonObject() {
        return isJsonObject(value);
    }

    public static boolean isJsonObject(String value) {
        if (value == null) {
            return false;
        }
        if (value.startsWith(JSON_PREFIX) && value.endsWith(JSON_SUFFIX)) {
            return true;
        }
        return false;
    }
    
    @JsonIgnore
    public List<String> getStringArray() {
        return getStringArray(value);
    }
    
    public static List<String> getStringArray(String value) {
        try {
            JsonNode tree = MAPPER.readTree(value.substring(JSON_PREFIX.length(), value.length() - JSON_SUFFIX.length()));
            if (tree.isArray()) {
                List<String> result = new ArrayList<>();
                Iterator<JsonNode> it = tree.elements();
                while (it.hasNext()) {
                    JsonNode v = it.next();
                    result.add(v.asText());
                }
                return result;
            }
        } catch (Exception e) {
        }
        return null;
    }

    public static String encodeArray(Collection<String> value) {
        return encodeObject(value);
    }
    
    public static String encodeObject(Object value) {
        if (value == null) {
            return null;
        }
        try {
            return JSON_PREFIX + MAPPER.writeValueAsString(value) + JSON_SUFFIX;
        } catch (JsonProcessingException e) {
        }
        return null;
    }

}
 