/**
 * 
 */
package com.ca.apm.systemtest.fld.flow.controller.dao;

import java.io.Serializable;
import java.util.List;


/**
 * 
 * @author KEYJA01
 *
 */
public interface GenericDao<T, PK extends Serializable> {
	/**
	 * Finds a single instance with the specified id
	 * @param id
	 * @return
	 */
	public T find(PK id);
	
	/**
	 * Returns a list of ALL Ts in the database
	 * @return
	 */
	public List<T> findAll();
	
	
	/**
	 * Persists a newly created T
	 * @param t
	 */
	public void create(T t);
	
	
	/**
	 * Updates t in the database
	 * @param t
	 */
	public void update(T t);
	
	
	/**
	 * Removes t from the database
	 * @param t
	 */
	public void delete(T t);
	
	
	/**
	 * Removes the record from the database by private key.
	 *  
	 * @param id
	 */
	public void deleteById(PK id);
	
	/**
	 * Deletes all records of this type from the database. 
	 * @return number of records deleted
	 * 
	 */
	public int deleteAll();
	
}
