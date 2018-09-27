package com.ca.apm.systemtest.fld.server.rest;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.activiti.engine.impl.util.json.JSONArray;
import org.activiti.engine.impl.util.json.JSONException;
import org.activiti.engine.impl.util.json.JSONObject;
import org.dozer.Mapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.ca.apm.systemtest.fld.plugin.NodeManager;
import com.ca.apm.systemtest.fld.plugin.Plugin;
import com.ca.apm.systemtest.fld.proxy.AgentProxy;
import com.ca.apm.systemtest.fld.proxy.AgentProxyFactory;
import com.ca.apm.systemtest.fld.server.dao.AgentDistributionDao;
import com.ca.apm.systemtest.fld.server.dao.NodeDao;
import com.ca.apm.systemtest.fld.server.model.Node;
import com.ca.apm.systemtest.fld.shared.vo.NodeVO;
import com.ca.apm.systemtest.fld.shared.vo.PluginVO;
import com.ca.apm.systemtest.fld.shared.vo.Response;

/**
 * To use this REST API, explicitly set the Accept header to "Accept:
 * GET /nodeList - retrieve a list of available agent nodes
 * 
 * @author zunpa01
 *
 */
@RestController("nodeManagerRestController")
public class NodeManagerRestController {
    public static Logger log = LoggerFactory.getLogger(NodeManagerRestController.class);

    @Autowired
    private AgentDistributionDao agentDistroDao;
    
    @Autowired
    private NodeManager nodeManager;

    @Autowired
    private AgentProxyFactory agentProxyFactory;

    @Autowired
    private NodeDao nodeDao;

    @Autowired
    private Mapper mapper;

    @RequestMapping(value = "/invokePluginMethod", method = RequestMethod.POST,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Transactional(propagation = Propagation.REQUIRED)
    public ResponseEntity<Response> invokePluginMethod(
        @RequestParam(required = true, value = "nodeName") String nodeName,
        @RequestParam(required = true, value = "pluginName") String pluginName,
        @RequestParam(required = true, value = "methodName") String methodName,
        @RequestParam(required = false, value = "methodParams") List<String> methodParams) {
        Response response = new Response();

        Node node = nodeDao.findByNodeName(nodeName);
        if (node == null) {
            response.setStatus(HttpStatus.NOT_FOUND);
            return (new ResponseEntity<Response>(response, HttpStatus.NOT_FOUND));
        }

        AgentProxy proxy = agentProxyFactory.createProxy(nodeName);
        Plugin plugin = proxy.getPlugin(pluginName, Plugin.class);
        Method[] methods = plugin.getClass().getMethods();
        for (Method m : methods) {
            if (m.getName().equals(methodName)) {
                Type[] parameterTypes = m.getGenericParameterTypes();
                Object[] args = new Object[parameterTypes.length];
                if (methodParams != null && parameterTypes.length == methodParams.size()) {
                    for (int i = 0; i < parameterTypes.length; ++i) {
                        try {
                            args[i] = toObject(parameterTypes[i], methodParams.get(i));
                        } catch (JSONException | NoSuchFieldException | SecurityException
                            | InstantiationException | IllegalAccessException e) {
                            log.error("Exception.", e);
                            response.setStatus(HttpStatus.BAD_REQUEST);
                            return (new ResponseEntity<Response>(response, HttpStatus.BAD_REQUEST));
                        }
                    }
                }
                try {
                    response.setReturnValue(m.invoke(plugin, args).toString());
                } catch (IllegalAccessException | IllegalArgumentException
                    | InvocationTargetException e) {
                    log.error("Exception.", e);
                    response.setStatus(HttpStatus.BAD_REQUEST);
                    return (new ResponseEntity<Response>(response, HttpStatus.BAD_REQUEST));
                }
            }
        }

        response.setStatus(HttpStatus.OK);
        return (new ResponseEntity<Response>(response, HttpStatus.OK));
    }

    @RequestMapping(value = "/nodePluginList", method = RequestMethod.GET,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Transactional(propagation = Propagation.REQUIRED)
    public ResponseEntity<Response> getAvailablePlugins(
        @RequestParam(required = true, value = "nodeName") String nodeName) {
        Node node = nodeDao.findByNodeName(nodeName);
        if (node == null) {
            Response notFound = new Response();
            notFound.setStatus(HttpStatus.NOT_FOUND);
            return (new ResponseEntity<Response>(notFound, HttpStatus.NOT_FOUND));
        }

        AgentProxy proxy = agentProxyFactory.createProxy(nodeName);
        Map<String, Plugin> pluginMap = proxy.getPlugins();
        List<PluginVO> plugins = new ArrayList<>(pluginMap.size());
        for (Entry<String, Plugin> entry : pluginMap.entrySet()) {
            if (!entry.getKey().contains("weblogic") && !entry.getKey().contains("jmeter")) {
                Plugin plugin = entry.getValue();
                plugins.add(new PluginVO(entry.getKey(), plugin.listOperations()));
            }
        }

        Response response = new Response();
        response.setPlugins(plugins);
        response.setStatus(HttpStatus.OK);

        return (new ResponseEntity<Response>(response, HttpStatus.OK));
    }

    @RequestMapping(value = "/deleteNode/{nodeId}", method = RequestMethod.DELETE,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Transactional(propagation = Propagation.REQUIRED)
    public ResponseEntity<Response> deleteNode(@PathVariable("nodeId") Long nodeId) {
        Node node = nodeDao.find(nodeId);
        if (node == null) {
            Response notFound = new Response();
            notFound.setStatus(HttpStatus.NOT_FOUND);
            return (new ResponseEntity<Response>(notFound, HttpStatus.NOT_FOUND));
        }
        
        nodeDao.delete(node);

        List<NodeVO> vos = listAllNodesAsVOs();

        Response response = new Response();
        response.setNodes(vos);
        
        Long agentLatestVersion = agentDistroDao.getLatestVersion();
        if (agentLatestVersion != null) {
            response.setHasAgentDistribution(true);
            response.setLatestAgentDistributionVersion(agentLatestVersion.toString());
        }
        response.setStatus(HttpStatus.OK);

        return (new ResponseEntity<Response>(response, HttpStatus.OK));
    }

    @RequestMapping(value = "/nodeList", method = RequestMethod.GET,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Transactional(propagation = Propagation.REQUIRED)
    public ResponseEntity<Response> getNodeList() {
        List<NodeVO> vos = listAllNodesAsVOs();

        Response response = new Response();
        response.setNodes(vos);
        Long agentLatestVersion = agentDistroDao.getLatestVersion();
        if (agentLatestVersion != null) {
            response.setHasAgentDistribution(true);
            response.setLatestAgentDistributionVersion(agentLatestVersion.toString());
        }
        response.setStatus(HttpStatus.OK);

        return (new ResponseEntity<Response>(response, HttpStatus.OK));
    }

    private List<NodeVO> listAllNodesAsVOs() {
        List<Node> nodes = nodeDao.findAll();
        List<NodeVO> vos = new ArrayList<>(nodes.size());
        for (Node n : nodes) {
            NodeVO nodeVO = convertNode(n);
            nodeVO.setIsAvailable(nodeManager.checkNodeAvailable(n.getName()));
            vos.add(nodeVO);
        }
        Collections.sort(vos, new Comparator<NodeVO>() {
            public int compare(NodeVO n1, NodeVO n2) {
                if (n1.getName() == null && n2.getName() == null) {
                    return (0);
                } else if (n1.getName() == null) {
                    return (1);
                } else if (n2.getName() == null) {
                    return (-1);
                }
                return (n1.getName().compareTo(n2.getName()));
            }
           
        });
        return vos;
    }
    
    /**
     * Converts a {@link Node} model instance to a {@link NodeVO} used by the REST service
     * @param node
     * @return
     */
    private NodeVO convertNode(Node node) {
        NodeVO vo = null;
        synchronized (mapper) {
            vo = mapper.map(node, NodeVO.class);
        }
        return (vo);
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    private Object toObject(Type type, String value) throws JSONException, NoSuchFieldException,
        SecurityException, InstantiationException, IllegalAccessException {
        Class<?> clazz = (Class<?>) type;
        if (String.class == clazz) return (value);
        if (Boolean.class == clazz || Boolean.TYPE == clazz) return (Boolean.parseBoolean(value));
        if (Byte.class == clazz || Byte.TYPE == clazz) return (Byte.parseByte(value));
        if (Short.class == clazz || Short.TYPE == clazz) return (Short.parseShort(value));
        if (Integer.class == clazz || Integer.TYPE == clazz) return (Integer.parseInt(value));
        if (Long.class == clazz || Long.TYPE == clazz) return (Long.parseLong(value));
        if (Float.class == clazz || Float.TYPE == clazz) return (Float.parseFloat(value));
        if (Double.class == clazz || Double.TYPE == clazz) return (Double.parseDouble(value));
        if (clazz.isEnum()) return (Enum.valueOf((Class<Enum>) clazz, value));

        if (List.class.isAssignableFrom(clazz)) {
            // Class generic = (Class) ((ParameterizedType) type).getActualTypeArguments()[0];
            JSONArray arr = new JSONArray(value);
            Collection<String> list = new ArrayList(arr.length());
            for (int i = 0; i < arr.length(); ++i) {
                // list.add(toObject(generic, arr.getString(i)));
                list.add(arr.getString(i));
            }
            return (list);
        }

        if (Map.class.isAssignableFrom(clazz)) {
            /*ParameterizedType ptype = (ParameterizedType) type;
            Class generic1 = (Class) ptype.getActualTypeArguments()[0]; 
            Class generic2 = (Class) ptype.getActualTypeArguments()[1];*/
            JSONObject json = new JSONObject(value);
            String[] names = JSONObject.getNames(json);
            Map<String, String> map = new HashMap<>(names != null ? names.length : 1);
            if (names != null) {
                for (String s : names) {
                    // map.put(toObject(generic1, s),  toObject(generic2, json.getString(s)));
                    map.put(s, json.getString(s));
                }
            }
            return (map);
        }

        JSONObject json = new JSONObject(value);
        Object obj = clazz.newInstance();
        String[] names = JSONObject.getNames(json);
        for (String s : names) {
            Field f = clazz.getField(s);
            f.set(obj, toObject(f.getType(), json.getString(s)));
        }
        return (obj);
    }

}
