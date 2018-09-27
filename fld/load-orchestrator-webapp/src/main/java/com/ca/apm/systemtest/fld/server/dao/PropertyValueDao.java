package com.ca.apm.systemtest.fld.server.dao;

import java.util.List;

import com.ca.apm.systemtest.fld.server.model.PropertyValue;

/**
 * @author ZUNPA01
 *
 */
public interface PropertyValueDao extends GenericDao<PropertyValue, Long> {
    public List<PropertyValue> findByName(String name);
    public List<PropertyValue> findByNames(List<String> names);
    public List<PropertyValue> findByPropertiesFile(String propertiesFile);
    public PropertyValue findByNameAndFile(String name, String propertiesFile);
    public void deleteByPropertiesFile(String propertiesFile);

}
