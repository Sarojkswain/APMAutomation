/**
 * 
 */
package com.ca.apm.systemtest.fld.server.dao;

import java.util.List;

import com.ca.apm.systemtest.fld.server.model.AgentDistribution;

/**
 * @author KEYJA01
 *
 */
public interface AgentDistributionDao extends GenericDao<AgentDistribution, Long> {
	/**
	 * Returns the newest (by timestamp) {@link AgentDistribution} in the persistent store, or null if there are none
	 * @return
	 */
	public AgentDistribution findNewest();
	
	/**
	 * Returns just the version of the newest agent distribution.
	 * 
	 * @return
	 */
	public Long getLatestVersion();
	
	public void deleteByIds(List<Long> ids);
	
	public int deleteOlder(Long timestamp);
}
