/**
 * 
 */
package com.ca.apm.systemtest.fld.server.dao;

import java.util.List;

import org.hibernate.Query;
import org.hibernate.Session;
import org.springframework.stereotype.Component;

import com.ca.apm.systemtest.fld.server.model.AgentDistribution;

/**
 * @author KEYJA01
 *
 */
@Component
public class AgentDistributionHibernate4Dao extends AbstractHibernate4GenericDao<AgentDistribution, Long> implements AgentDistributionDao {

	/**
	 */
	public AgentDistributionHibernate4Dao() {
		super(AgentDistribution.class);
	}
	
	
	/* (non-Javadoc)
	 * @see com.ca.apm.systemtest.fld.server.dao.AgentDistributionDao#findNewest()
	 */
	@Override
	public AgentDistribution findNewest() {
		Query query = getCurrentSession().createQuery("from AgentDistribution order by id desc");
		@SuppressWarnings("unchecked")
		List<AgentDistribution> list = query.list();
		if (list != null && list.size() > 0) {
			return list.get(0);
		}
		
		return null;
	}

	@Override
	public Long getLatestVersion() {
        AgentDistribution newestDistro =  findNewest();
        return newestDistro == null ? null : newestDistro.getTimestamp(); 
	}
	
	@Override
	public void deleteByIds(List<Long> ids) {
	    getCurrentSession().createQuery("delete AgentDistribution ad where id in (:list)")
	        .setParameterList("list", ids)
	        .executeUpdate();
	}
	
	@Override
	public int deleteOlder(Long timestamp) {
        Session session = getCurrentSession();
        @SuppressWarnings("unchecked")
        List<Long> dataIds = session.createQuery("select ad.data.id from AgentDistribution ad where ad.timestamp < :ts")
            .setLong("ts", timestamp)
            .list();
        
        if (dataIds.size() == 0) {
            return 0;
        }
	    
        @SuppressWarnings("unchecked")
        List<Long> ids = session.createQuery("select ad.id from AgentDistribution ad where ad.timestamp < :ts")
            .setLong("ts", timestamp)
            .list();
        
        if (ids.size() == 0) {
            return 0;
        }
        
        session.createQuery("delete AgentDistribution ad where id in (:list)")
            .setParameterList("list", ids)
            .executeUpdate();
        
        return session.createQuery("delete AgentDistributionData ad where id in (:list)")
            .setParameterList("list", dataIds)
            .executeUpdate();
	    
//	    String hql1 = "delete AgentDistributionData add where add.id in (select ad.data.id from AgentDistribution ad where ad.timestamp < :ts)";
//	    String hql2 = "delete AgentDistribution ad where ad.timestamp < :ts ";
//	    
//	    session.createQuery(hql1)
//	        .setLong("ts", timestamp)
//	        .executeUpdate();
//	    
//	    return session.createQuery(hql2)
//            .setLong("ts", timestamp)
//            .executeUpdate();
	}
}
