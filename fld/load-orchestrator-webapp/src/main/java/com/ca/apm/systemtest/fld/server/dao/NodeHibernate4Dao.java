package com.ca.apm.systemtest.fld.server.dao;

import org.hibernate.Query;
import org.springframework.stereotype.Component;

import com.ca.apm.systemtest.fld.server.model.Node;

/**
 * @author ZUNPA01
 *
 */
@Component
public class NodeHibernate4Dao
        extends AbstractHibernate4GenericDao<Node, Long> implements NodeDao {

    public NodeHibernate4Dao() {
        super(Node.class);
    }

    @Override
    public Node findByNodeName(String nodeName) {
        Query query = getCurrentSession().createQuery("from Node where name = :nodeName");
        query.setString("nodeName", nodeName);
        return ((Node) query.uniqueResult());
    }

}
