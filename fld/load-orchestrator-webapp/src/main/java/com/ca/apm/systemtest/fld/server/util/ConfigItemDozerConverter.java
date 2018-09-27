/**
 * 
 */
package com.ca.apm.systemtest.fld.server.util;

import org.dozer.CustomConverter;
import org.hibernate.MappingException;

import com.ca.apm.systemtest.fld.server.model.BooleanConfigItem;
import com.ca.apm.systemtest.fld.server.model.ConfigItem;
import com.ca.apm.systemtest.fld.server.model.CustomConfigItem;
import com.ca.apm.systemtest.fld.server.model.DoubleConfigItem;
import com.ca.apm.systemtest.fld.server.model.EnumConfigItem;
import com.ca.apm.systemtest.fld.server.model.LongConfigItem;
import com.ca.apm.systemtest.fld.server.model.StringConfigItem;
import com.ca.apm.systemtest.fld.shared.vo.ConfigItemVO;

/**
 * Used to covert {@link ConfigItem} <==> {@link ConfigItemVO}
 * @author KEYJA01
 *
 */
public class ConfigItemDozerConverter implements CustomConverter {

	/**
	 * 
	 */
	public ConfigItemDozerConverter() {
	}

	/* (non-Javadoc)
	 * @see org.dozer.CustomConverter#convert(java.lang.Object, java.lang.Object, java.lang.Class, java.lang.Class)
	 */
	@Override
	public Object convert(Object dest, Object source, Class<?> destinationClass, Class<?> sourceClass) {
		if (source == null) {
			return null;
		}
		
		if (ConfigItem.class.isAssignableFrom(sourceClass)) {
			// convert to ConfigItemVO
			ConfigItemVO vo = (ConfigItemVO) dest;
			ConfigItem item = (ConfigItem) source;
			if (dest == null) {
				vo = new ConfigItemVO();
			}
			vo.setName(item.getName());
			vo.setFormId(item.getFormId());
			if (item instanceof EnumConfigItem) {
			    EnumConfigItem enumConfigItem = (EnumConfigItem) item;
			    vo.setType(enumConfigItem.getCustomItemType());
                vo.setValue(enumConfigItem.getStringValue());
			    vo.setTypeInformation(enumConfigItem.getEnumValues());
			} else if (item instanceof CustomConfigItem) {
			    CustomConfigItem cci = (CustomConfigItem) item;
			    vo.setType(cci.getCustomItemType());
			    vo.setValue(cci.getStringValue());
			} else if (item instanceof StringConfigItem) {
				vo.setType("string");
				vo.setValue(((StringConfigItem) item).getStringValue());
			} else if (item instanceof LongConfigItem) {
				vo.setType("long");
				vo.setValue(((LongConfigItem) item).getLongValue());
			} else if (item instanceof DoubleConfigItem) {
				vo.setType("double");
				vo.setValue(((DoubleConfigItem) item).getDoubleValue());
			} else if (item instanceof BooleanConfigItem) {
				vo.setType("boolean");
				vo.setValue(((BooleanConfigItem) item).getBooleanValue());
			}
			vo.setRequired(item.isRequired());
			return vo;
		} else if (sourceClass == ConfigItemVO.class) {
			if (dest != null) {
				throw new MappingException("Conversion into already existing ConfigItem is not supported");
			}
			// convert to ConfigItem
			ConfigItemVO vo = (ConfigItemVO) source;
			ConfigItem item = null;
			Object value = vo.getValue();
			if ("string".equals(vo.getType())) {
				StringConfigItem stringConfigItem = new StringConfigItem();
				item = stringConfigItem;
				if (value != null) {
					stringConfigItem.setStringValue(value.toString());
				}
			} else if ("long".equals(vo.getType())) {
				LongConfigItem longConfigItem = new LongConfigItem();
				item = longConfigItem;
				if (value != null && value instanceof Number) {
					Number n = (Number) value;
					longConfigItem.setLongValue(n.longValue());
				}
			} else if ("double".equals(vo.getType())) {
				DoubleConfigItem doubleConfigItem = new DoubleConfigItem();
				item = doubleConfigItem;
				if (value != null && value instanceof Number) {
					Number n = (Number) value;
					doubleConfigItem.setDoubleValue(n.doubleValue());
				}
			} else if ("boolean".equals(vo.getType())) {
				BooleanConfigItem booleanConfigItem = new BooleanConfigItem();
				item = booleanConfigItem;
				
				booleanConfigItem.setName(vo.getName());
				if (value != null) {
					Boolean bval = null;
					if (value instanceof Boolean) {
						bval = (Boolean) value;
					} else {
						bval = Boolean.valueOf(value.toString());
					}
					booleanConfigItem.setBooleanValue(bval);
				}
			} else {
			    CustomConfigItem customConfigItem = null;
			    if ("enum".equals(vo.getType())) {
	                EnumConfigItem enumConfigItem = new EnumConfigItem();
	                enumConfigItem.setEnumValues(vo.getTypeInformation());
                    customConfigItem = enumConfigItem;
			    } else {
			        customConfigItem = new CustomConfigItem();
			    }
			    item = customConfigItem; 
			    customConfigItem.setCustomItemType(vo.getType());
                if (value != null) {
                    customConfigItem.setStringValue(value.toString());
                }
			}
			item.setName(vo.getName());
			item.setFormId(vo.getFormId());
			item.setRequired(vo.getRequired());
			return item;
		}
		
		throw new MappingException("Unable to map " + source + " (" + sourceClass + " to " + dest + "(" + destinationClass + ")");
	}

}
