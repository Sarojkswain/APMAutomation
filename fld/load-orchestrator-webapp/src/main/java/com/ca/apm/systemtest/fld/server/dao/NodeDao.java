package com.ca.apm.systemtest.fld.server.dao;

import com.ca.apm.systemtest.fld.server.model.Node;

/**
 * @author ZUNPA01
 *
 */
public interface NodeDao extends GenericDao<Node, Long> {
    public Node findByNodeName(String nodeName);
}
