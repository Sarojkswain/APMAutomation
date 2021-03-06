/**
 * 
 */
package com.ca.apm.systemtest.fld.server.dao;

import java.io.Serializable;
import java.util.List;

import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author KEYJA01
 *
 */
public class AbstractHibernate4GenericDao<T, PK extends Serializable> implements GenericDao<T, PK> {
	private Class<T> klass;
	
	@Autowired
	private SessionFactory sessionFactory;

	/**
	 * 
	 */
	public AbstractHibernate4GenericDao(Class <T> klass) {
		this.klass = klass;
	}

	@SuppressWarnings("unchecked")
	@Override
	public T find(PK id) {
		return (T) getCurrentSession().get(klass, id);
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<T> findAll() {
		Session session = getCurrentSession();
		Query q = session.createQuery("from " + klass.getName());
		return q.list();
	}

	@Override
	public void create(T t) {
		Session session = getCurrentSession();
		session.persist(t);
	}

	@Override
	public void update(T t) {
		Session session = getCurrentSession();
		session.merge(t);
	}

	@Override
	public void delete(T t) {
		Session session = getCurrentSession();
		session.delete(t);
	}

	protected Session getCurrentSession() {
		return sessionFactory.getCurrentSession();
	}

	protected Class<T> getCurrentClass() {
		return klass;
	}
	
	public void setSessionFactory(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    @Override
    public void deleteById(PK id) {
        T t = find(id);
        delete(t);
    }

    @Override
    public int deleteAll() {
        List<T> all = findAll();
        int result = 0;
        if (all != null) {
            //Need Hibernate to cascade deletions
            for (T t : all) {
                if (t != null) {
                    delete(t);
                    result++;
                }
            }
        }
        return result;
    }
    
}
