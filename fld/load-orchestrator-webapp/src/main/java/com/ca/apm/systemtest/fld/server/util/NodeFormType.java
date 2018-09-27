package com.ca.apm.systemtest.fld.server.util;

import org.activiti.engine.ActivitiIllegalArgumentException;
import org.activiti.engine.form.AbstractFormType;

import com.ca.apm.systemtest.fld.server.dao.NodeDao;
import com.ca.apm.systemtest.fld.server.model.Node;

/**
 * Custom Activiti BPM form type representing an agent node
 * @author ZUNPA01
 *
 */
@SuppressWarnings("serial")
public class NodeFormType extends AbstractFormType {
    public static final String TYPE_NAME = "node";

    private NodeDao nodeDao;

    public NodeFormType(NodeDao dao) {
        super();
        nodeDao = dao;
    }

    @Override
    public String getName() {
        return (TYPE_NAME);
    }

    @Override
    public Object convertFormValueToModelValue(String propertyValue) {
        Node node = nodeDao.findByNodeName(propertyValue);
//        if (node == null) {
//            if (propertyValue == null) {
//                propertyValue = "null";
//            }
//            throw new ActivitiObjectNotFoundException("Node with name " + propertyValue + " not found", Node.class);
//        }
        return node;
    }

    @Override
    public String convertModelValueToFormValue(Object modelValue) {
        if (modelValue == null) {
            return (null);
        }
        if (!(modelValue instanceof Node)) {
            throw new ActivitiIllegalArgumentException("Illegal type: " + modelValue.getClass().toString() + ", expected: " + Node.class.toString());
        }
        return (((Node) modelValue).getName());
    }

}
