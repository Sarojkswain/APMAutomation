package com.ca.apm.systemtest.fld.hammond.data;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class AppmapDataTest {

    AppmapData data;

    @Before
    public void setUp() throws Exception {
//        data = new AppmapData("");
    }

    @After
    public void tearDown() throws Exception {}

    @Test
    public void testJsonParsing() {
//        data.addVertex(
//            0,
//            "[{\"name\":\"agent\",\"value\":\"tas-cz-n31|Tomcat|Tomcat Agent\",\"type\":\"GATHERED\"},"
//                + "{\"name\":\"applicationName\",\"value\":\"AuthenticationService\",\"type\":\"GATHERED\"},"
//                + "{\"name\":\"domain\",\"value\":\"ca.com\",\"type\":\"GATHERED\"},"
//                + "{\"name\":\"hostname\",\"value\":\"tas-cz-n31\",\"type\":\"GATHERED\"},"
//                + "{\"name\":\"name\",\"value\":\"ServletA6|service\",\"type\":\"GATHERED\"},"
//                + "{\"name\":\"servletClassname\",\"value\":\"ServletA6\",\"type\":\"GATHERED\"},"
//                + "{\"name\":\"servletMethod\",\"value\":\"service\",\"type\":\"GATHERED\"},"
//                + "{\"name\":\"type\",\"value\":\"SERVLET\",\"type\":\"GATHERED\"}]");
//        Vertex vertex = data.getVertex(0);
//        Assert.assertEquals("agent property", "tas-cz-n31|Tomcat|Tomcat Agent", vertex.getAgent());
//        Assert.assertEquals("applicationName property", "AuthenticationService",
//            vertex.getApplicationName());
//        Attribute attribute =
//            vertex.getAttribute(Vertex.MainAttribute.ATTRIBUTE_NAME_HOST_DOMAIN.getName());
//        Assert.assertEquals("domain property", "ca.com", attribute.getValue());
//        Assert.assertEquals("domain property type", AttributeType.GATHERED, attribute.getType());
//        Assert.assertEquals("hostname property", "tas-cz-n31", vertex.getHostname());
//        Assert.assertEquals("type property", VertexType.Type.SERVLET, vertex.getType());
//        
    }
}
