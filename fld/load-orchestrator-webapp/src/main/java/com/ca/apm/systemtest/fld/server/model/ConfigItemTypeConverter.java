/**
 * 
 */
package com.ca.apm.systemtest.fld.server.model;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

/**
 * @author KEYJA01
 *
 */
@Converter
public class ConfigItemTypeConverter implements AttributeConverter<ConfigItemType, String> {

	/**
	 * 
	 */
	public ConfigItemTypeConverter() {
	}

	@Override
	public String convertToDatabaseColumn(ConfigItemType itemType) {
		return itemType.getVal();
	}

	@Override
	public ConfigItemType convertToEntityAttribute(String val) {
		return ConfigItemType.convert(val);
	}

}
