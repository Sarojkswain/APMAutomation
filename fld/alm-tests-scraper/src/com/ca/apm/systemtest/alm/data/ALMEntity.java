package com.ca.apm.systemtest.alm.data;

import java.util.Collections;
import java.util.Comparator;
import java.util.Map;
import java.util.TreeMap;

import com.ca.apm.systemtest.alm.data.utility.ALMUtilities;
import com.ca.testing.almclient.entities.alm.Entity;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.googlecode.cqengine.attribute.SimpleNullableAttribute;
import com.googlecode.cqengine.query.option.QueryOptions;

/**
 * Entity wrapper class.
 */
public class ALMEntity {
    public static class GetStringAttributeImpl extends SimpleNullableAttribute<ALMEntity, String> {
        private String field;

        public GetStringAttributeImpl(String fieldName) {
            super(fieldName);
            this.field = fieldName;
        }

        @Override
        public String getValue(ALMEntity obj, QueryOptions queryOptions) {
            if (obj == null) {
                return null;
            }
            return obj.getFieldValue(field);
        }
    }


    public static class GetIntegerAttributeImpl
        extends SimpleNullableAttribute<ALMEntity, Integer> {
        private String field;

        public GetIntegerAttributeImpl(String fieldName) {
            super(fieldName);
            this.field = fieldName;
        }

        @Override
        public Integer getValue(ALMEntity obj, QueryOptions queryOptions) {
            if (obj == null) {
                return null;
            }
            return obj.getFieldIntValue(field);
        }
    }

    public static class CompareByField implements Comparator<ALMEntity> {
        String attribute;

        public CompareByField(String attribute) {
            this.attribute = attribute;
        }

        @Override
        public int compare(ALMEntity o1, ALMEntity o2) {
            if (o1 == null && o2 == null) {
                return 0;
            } else if (o1 != null && o2 == null) {
                return 1;
            } else if (o1 == null && o2 != null) {
                return -1;
            } else {
                String a1 = o1.getFieldValue(attribute);
                String a2 = o2.getFieldValue(attribute);
                if (a1 == null && a2 == null) {
                    return 0;
                } else if (a1 != null && a2 == null) {
                    return 1;
                } else if (a1 == null && a2 != null) {
                    return -1;
                } else {
                    return a1.compareTo(a2);
                }
            }
        }
    }


    public static final SimpleNullableAttribute<ALMEntity, Integer> ID
        = new GetIntegerAttributeImpl("id");
    public static final SimpleNullableAttribute<ALMEntity, Integer> PARENT_ID
        = new GetIntegerAttributeImpl("parent-id");
    public static final SimpleNullableAttribute<ALMEntity, String> NAME
        = new GetStringAttributeImpl("name");


    private Map<String, String> fields = new TreeMap<>();
    private String type;

    public ALMEntity() {
    }

    public ALMEntity(Entity entity) {
        this.type = entity.getType();
        fields = ALMUtilities.extractFieldsAndValues(entity);
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getFieldValue(String field) {
        return fields.get(field);
    }

    public Integer getFieldIntValue(String field) {
        String value = fields.get(field);
        if (value == null) {
            return null;
        } else {
            return Integer.parseInt(value);
        }
    }

    public Map<String, String> getFields() {
        return Collections.unmodifiableMap(fields);
    }

    @JsonSetter("fields")
    public void setFields(Map<String, String> fields) {
        this.fields = fields;
    }
}
