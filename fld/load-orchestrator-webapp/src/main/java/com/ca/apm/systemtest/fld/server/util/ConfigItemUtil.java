package com.ca.apm.systemtest.fld.server.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.activiti.engine.form.FormProperty;
import org.activiti.engine.impl.form.BooleanFormType;
import org.activiti.engine.impl.form.DoubleFormType;
import org.activiti.engine.impl.form.EnumFormType;
import org.activiti.engine.impl.form.LongFormType;
import org.activiti.engine.impl.form.StringFormType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ca.apm.systemtest.fld.server.model.BooleanConfigItem;
import com.ca.apm.systemtest.fld.server.model.ConfigItem;
import com.ca.apm.systemtest.fld.server.model.ConfigItemType;
import com.ca.apm.systemtest.fld.server.model.CustomConfigItem;
import com.ca.apm.systemtest.fld.server.model.DoubleConfigItem;
import com.ca.apm.systemtest.fld.server.model.EnumConfigItem;
import com.ca.apm.systemtest.fld.server.model.LongConfigItem;
import com.ca.apm.systemtest.fld.server.model.StringConfigItem;

/**
 * Utility class for common operations on {@link ConfigItem}.
 *
 * @author sinal04
 */
public class ConfigItemUtil {
    private static final Logger LOG = LoggerFactory.getLogger(ConfigItemUtil.class);


    /**
     * Converts a {@link FormProperty} to a {@link ConfigItem}
     *
     * @param prop
     * @return
     * @throws Exception
     */
    public static ConfigItem convertFormProperty(FormProperty prop) throws Exception {
        String propValue = prop.getValue();
        if (propValue != null) {
            propValue = propValue.trim();
        }
        if (prop.getType() instanceof StringFormType) {
            StringConfigItem item =
                new StringConfigItem(prop.getName(), prop.getId(), prop.getValue());
            item.setRequired(prop.isRequired());
            return item;
        } else if (prop.getType() instanceof NodeFormType) {
            CustomConfigItem item =
                new CustomConfigItem(prop.getName(), prop.getId(), prop.getValue(), prop.getType()
                    .getName());
            item.setRequired(prop.isRequired());
            return item;
        } else if (prop.getType() instanceof EnumFormType) {
            Object valuesObj = prop.getType().getInformation("values");
            EnumConfigItem item =
                new EnumConfigItem(prop.getName(), prop.getId(), prop.getValue(), valuesObj != null
                    ? valuesObj.toString()
                    : null);
            item.setRequired(prop.isRequired());
            return item;
        } else if (prop.getType() instanceof LongFormType) {
            LongConfigItem item = new LongConfigItem(prop.getName(), prop.getId(), null);
            if (propValue != null) {
                try {
                    Long value = Long.parseLong(propValue);
                    item.setLongValue(value);
                } catch (Exception e) {
                    String msg =
                        "Unable to convert \"" + propValue
                            + "\" to Long, setting form default value to null";
                    if (LOG.isWarnEnabled()) {
                        LOG.warn(msg);
                    }
                    throw new Exception(msg, e);
                }
            }
            item.setRequired(prop.isRequired());
            return item;
        } else if (prop.getType() instanceof BooleanFormType) {
            BooleanConfigItem item = new BooleanConfigItem(prop.getName(), prop.getId(), null);
            if (propValue != null) {
                Boolean value = Boolean.parseBoolean(propValue);
                item.setBooleanValue(value);
            }
            item.setRequired(prop.isRequired());
            return item;
        } else if (prop.getType() instanceof DoubleFormType) {
            DoubleConfigItem item = new DoubleConfigItem(prop.getName(), prop.getId(), null);
            if (propValue != null) {
                try {
                    Double value = Double.parseDouble(propValue);
                    item.setDoubleValue(value);
                } catch (NumberFormatException e) {
                    String msg =
                        "Unable to convert \"" + propValue
                            + "\" to Double, setting form default value to null";
                    if (LOG.isWarnEnabled()) {
                        LOG.warn(msg);
                    }
                    throw new Exception(msg, e);
                }
            }
            item.setRequired(prop.isRequired());
            return item;
        }

        String msg = "Unknown type for " + propValue + ", cannot automatically convert";
        if (LOG.isWarnEnabled()) {
            LOG.warn(msg);
        }
        throw new Exception(msg);
    }

    /**
     * Converts a collection of {@link ConfigItem} to a Map using {@link ConfigItem#getFormId()}
     * as a key
     * and wrapping each {@link ConfigItem} into {@link ConfigItemWrapper} which considers two
     * {@link ConfigItem}s equal
     * when their {@link ConfigItem#getFormId()}s are equal.
     *
     * @param configItems
     * @return
     */
    public static Map<String, ConfigItemWrapper> convertToMap(Collection<ConfigItem> configItems) {
        if (configItems == null) {
            return new LinkedHashMap<>(0);
        }

        Map<String, ConfigItemWrapper> result = new LinkedHashMap<>(configItems.size());
        for (ConfigItem configItem : configItems) {
            result.put(configItem.getFormId(), new ConfigItemWrapper(configItem));
        }
        return result;
    }

    /**
     * Returns a list containing form ids of the provided <code>configItems</code>.
     *
     * @param configItems dashboard configuration parameters
     * @return mutable list of form id strings
     */
    public static List<String> toFormIdsList(Collection<ConfigItem> configItems) {
        List<String> result = new ArrayList<>(configItems != null ? configItems.size() : 0);
        if (configItems != null) {
            for (ConfigItem configItem : configItems) {
                result.add(configItem.getFormId());
            }
        }
        return result;
    }

    public static List<ConfigItem> convertFormProperties(
        Collection<FormProperty> formProps) throws Exception {
        List<ConfigItem> configItems = new ArrayList<>(formProps != null ? formProps.size() : 0);
        if (formProps != null) {
            for (FormProperty formProp : formProps) {
                configItems.add(convertFormProperty(formProp));
            }
        }
        return configItems;
    }

    public static Collection<ConfigItem> nullifyIds(Collection<ConfigItem> configItems) {
        if (configItems != null) {
            for (ConfigItem configItem : configItems) {
                configItem.setId(null);
            }
        }
        return configItems;
    }

    public static List<ConfigItem> updateConfigItems(Collection<FormProperty> formProps,
        List<ConfigItem> configItems) throws Exception {
        List<ConfigItem> newConfigItems = convertFormProperties(formProps);
        return updateConfigItems(convertToMap(newConfigItems), configItems);
    }

    public static List<ConfigItem> updateConfigItems(
        Map<String, ConfigItemWrapper> newConfigItemsMap,
        List<ConfigItem> configItems) throws Exception {
        List<ConfigItem> oldConfigItems = configItems;
        if (oldConfigItems == null) {
            oldConfigItems = Collections.<ConfigItem>emptyList();
        }

        Collection<String> oldConfigItemIds = toFormIdsList(oldConfigItems);
        Collection<String> newConfigItemIds = newConfigItemsMap.keySet();

        Set<String> addedConfigItemIds = new LinkedHashSet<>(newConfigItemIds);
        addedConfigItemIds.removeAll(oldConfigItemIds);

        List<ConfigItem> modifiedConfigItems = new ArrayList<>(oldConfigItemIds.size());
        List<ConfigItem> unModifiedConfigItems = new ArrayList<>(oldConfigItemIds.size());

        for (ConfigItem oldConfigItem : oldConfigItems) {
            ConfigItemWrapper newConfigItemWrapper = newConfigItemsMap
                .get(oldConfigItem.getFormId());
            //If the config item is not found among the new ones it's deleted,
            //so, we just don't copy it over.
            if (newConfigItemWrapper != null) {
                ConfigItem newConfigItem = newConfigItemWrapper.getConfigItem();
                //Do not compare values not to lose any config item changes made by the user 
                if (!oldConfigItem.equalsIgnoringValue(newConfigItem)) {
                    modifiedConfigItems.add(newConfigItem);
                } else {
                    unModifiedConfigItems.add(oldConfigItem);
                }
            }
        }

        List<ConfigItem> addedConfigItems = new ArrayList<>(addedConfigItemIds.size());
        for (String formId : addedConfigItemIds) {
            addedConfigItems.add(newConfigItemsMap.get(formId).getConfigItem());
        }

        if (!modifiedConfigItems.isEmpty() || !addedConfigItems.isEmpty()
            || unModifiedConfigItems.size() != oldConfigItems.size()) {
            //We have changes in configuration parameters
            List<ConfigItem> resultConfigItems = new ArrayList<>(modifiedConfigItems.size()
                + addedConfigItems.size()
                + unModifiedConfigItems.size());
            resultConfigItems.addAll(unModifiedConfigItems);
            resultConfigItems.addAll(modifiedConfigItems);
            resultConfigItems.addAll(addedConfigItems);
            return resultConfigItems;
        }
        return null;
    }


    public static class ConfigItemWrapper extends ConfigItem {

        private ConfigItem realConfigItem;

        public ConfigItemWrapper(ConfigItem confItem) {
            this.realConfigItem = confItem;
        }

        public ConfigItem getConfigItem() {
            return realConfigItem;
        }

        @Override
        public ConfigItemType getItemType() {
            return realConfigItem.getItemType();
        }

        @Override
        public Object getValue() {
            return realConfigItem.getValue();
        }

        /* (non-Javadoc)
         * @see java.lang.Object#hashCode()
         */
        @Override
        public int hashCode() {
            return realConfigItem.getFormId().hashCode();
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (!(obj instanceof ConfigItem)) {
                return false;
            }
            ConfigItem other = (ConfigItem) obj;
            return other.getFormId().equals(getFormId());
        }
    }
}
