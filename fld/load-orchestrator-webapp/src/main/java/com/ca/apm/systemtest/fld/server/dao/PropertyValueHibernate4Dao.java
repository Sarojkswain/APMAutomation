package com.ca.apm.systemtest.fld.server.dao;

import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.criterion.Restrictions;
import org.springframework.stereotype.Component;

import com.ca.apm.systemtest.fld.server.model.PropertyValue;

/**
 * @author ZUNPA01
 *
 */
@Component
public class PropertyValueHibernate4Dao
        extends AbstractHibernate4GenericDao<PropertyValue, Long> implements PropertyValueDao {

    public PropertyValueHibernate4Dao() {
        super(PropertyValue.class);
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<PropertyValue> findByName(String name) {
        Query query = getCurrentSession().createQuery("from PropertyValue where name = :valueName");
        query.setString("valueName", name);
        return ((List<PropertyValue>) query.list());
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<PropertyValue> findByPropertiesFile(String propertiesFile) {
        Query query = getCurrentSession().createQuery("from PropertyValue where properties_file = :fileName");
        query.setString("fileName", propertiesFile);
        return ((List<PropertyValue>) query.list());
    }

    @Override
    public PropertyValue findByNameAndFile(String name, String propertiesFile) {
        Query query = getCurrentSession().createQuery("from PropertyValue where name = :valueName and properties_file = :fileName");
        query.setString("valueName", name);
        query.setString("fileName", propertiesFile);
        return ((PropertyValue) query.uniqueResult());
    }

    @Override
    public void deleteByPropertiesFile(String propertiesFile) {
        Query query = getCurrentSession().createQuery("delete from PropertyValue where properties_file = :fileName");
        query.setString("fileName", propertiesFile);
        query.executeUpdate();
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<PropertyValue> findByNames(List<String> names) {
        Criteria criteria = getCurrentSession().createCriteria(PropertyValue.class);
        criteria.add(Restrictions.in("name", names));
        return criteria.list();
    }

}
