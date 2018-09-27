package com.ca.apm.systemtest.fld.monitor.plugin;

import java.util.*;

/**
 * @Author rsssa02
 */
public class MyMBean {

    private String type;
    private String name;
    private String path;
    private Map<String, List<String>> attributes;

    MyMBean() {
    }

    public Map<String, List<String>> getAttributes() {
        return attributes;
    }

    private void setAttributes(Map<String, List<String>> attributes) {
        this.attributes = attributes;
    }

    public String getPath() {
        return path;
    }

    private void setPath(String path) {
        this.path = path;
        this.type = getPropFromPath(path, "type");
        this.name = getPropFromPath(path, "name");
    }

    public Collection<String> getAttributeKeys() {
        return attributes.keySet();
    }

    public List<String> getSubAttributes(String attrKey) {
        return attributes.get(attrKey);
    }

    /**
     * @return the type
     */
    public String getType() {
        return type;
    }


    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        StringBuffer stringBuf = new StringBuffer();
        stringBuf.append(path);
        if (attributes != null && !attributes.isEmpty()) {
            for (Map.Entry<String, List<String>> attr : attributes.entrySet()) {
                stringBuf.append('|');
                stringBuf.append(attr.getKey());
                if (attr.getValue() != null && !attr.getValue().isEmpty()) {
                    stringBuf.append('/');
                    boolean putSubAttrDelimeter = false;
                    for (String subAttr : attr.getValue()) {
                        if (putSubAttrDelimeter) {
                            stringBuf.append(',');
                        }
                        stringBuf.append(subAttr);
                        putSubAttrDelimeter = true;
                    }
                }
            }
        }
        return stringBuf.toString();
    }

    public static String getPropFromPath(String path, String propName) {
        String substr = path;
        String prop = null;
        String propEq = propName + '=';
        int ind = substr.indexOf(propEq);
        if (ind > -1) {
            substr = path.substring(ind + propEq.length());
            ind = substr.indexOf(',');
            prop = ind > -1 ? substr.substring(0, ind) : substr;
        }
        return prop;
    }

    public static List<MyMBean> fromString(String metrics) {
        String[] mbeans = metrics.split(";");
        List<MyMBean> mbeansList = new ArrayList<>();
        for (String mbean : mbeans) {
            String[] mbeanSplit = mbean.split("\\|");
            MyMBean myMbean = new MyMBean();
            myMbean.setPath(mbeanSplit[0]);
            Map<String, List<String>> attribMap = new HashMap<>();
            for (int i = 1; i < mbeanSplit.length; i++) {
                String[] mbeanAttribs = mbeanSplit[i].split("/");
                String mbeanAttrib = mbeanAttribs[0];
                if (mbeanAttribs.length == 1) {
                    attribMap.put(mbeanAttrib, null);
                } else if (mbeanAttribs.length > 1) {
                    List<String> subAttribs = Arrays.asList(mbeanAttribs[1]
                            .split(","));
                    attribMap.put(mbeanAttrib, subAttribs);
                }
            }
            myMbean.setAttributes(attribMap);
            mbeansList.add(myMbean);
        }

        return mbeansList;

    }
}
