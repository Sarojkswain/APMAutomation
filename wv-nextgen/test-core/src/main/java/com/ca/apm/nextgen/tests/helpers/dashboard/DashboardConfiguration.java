package com.ca.apm.nextgen.tests.helpers.dashboard;

import org.apache.xpath.XPathAPI;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.transform.TransformerException;

public class DashboardConfiguration {

    private Document fDocument;
    private List<WidgetConfiguration> fWidgets = new ArrayList<WidgetConfiguration>();

    public DashboardConfiguration(Document document) {
        fDocument = document;
        NodeList widgetList = fDocument.getElementsByTagName("Widget");
        for (int i = 0; i < widgetList.getLength(); i++) {
            addWidget(new WidgetConfiguration(widgetList.item(i)));
        }
    }

    public List<WidgetConfiguration> getWidgets() {
        return fWidgets;
    }

    /*
     * get font weight property from the data in the dashboard config XML
     */
    private static String getFontWeightProperty(int styleCode) {
        if (styleCode == 1) {
            return "bold";
        } else {
            return "normal";
        }
    }

    /*
     * get font style property from the data in the dashboard config XML
     */
    private static String getFontStyleProperty(int styleCode) {
        if (styleCode == 2) {
            return "italic";
        } else {
            return "normal";
        }
    }

    /*
     * get font align property from the data in the dashboard config XML
     */
    private static String getTextAlignProperty(Integer styleCode) {
        if (styleCode == 2) {
            return "right";
        } else if (styleCode == 1) {
            return "center";
        } else {
            return "left";
        }
    }

    private static String getAttributeValue(Node node, String attributeName) {
        NamedNodeMap widgetAttrs = node.getAttributes();
        Node attrNode = widgetAttrs.getNamedItem(attributeName);
        return attrNode.getNodeValue();
    }

    private void addWidget(WidgetConfiguration widget) {
        if (!fWidgets.contains(widget)) {
            fWidgets.add(widget);
        }
    }

    public static class WidgetConfiguration {
        private Node fWidget;
        private Boolean fHasDefaultLink;

        public WidgetConfiguration(Node widget) {
            this.fWidget = widget;
        }

        public Integer getWidth() {
            Double width = null;
            Point cornerPoint = getDisplayBoxPoint("CornerPoint");
            Point originPoint = getDisplayBoxPoint("OriginPoint");
            if (originPoint != null && cornerPoint != null) {
                width = cornerPoint.getXCoordinate() - originPoint.getXCoordinate();
            }
            return width.intValue();
        }

        public Integer getHeight() {
            Double height = null;
            Point cornerPoint = getDisplayBoxPoint("CornerPoint");
            Point originPoint = getDisplayBoxPoint("OriginPoint");
            if (originPoint != null && cornerPoint != null) {
                height = cornerPoint.getYCoordinate() - originPoint.getYCoordinate();
            }
            return height.intValue();
        }

        public Integer getXCoordinate() {
            Double x = null;
            DashboardConfiguration.Point originPoint = getDisplayBoxPoint("OriginPoint");
            if (originPoint != null) {
                x = originPoint.getXCoordinate();
            }
            return x.intValue();
        }

        public Integer getYCoordinate() {
            Double y = null;
            DashboardConfiguration.Point originPoint = getDisplayBoxPoint("OriginPoint");
            if (originPoint != null) {
                y = originPoint.getYCoordinate();
            }
            return y.intValue();
        }

        public String getType() {
            return getAttributeValue(fWidget, "Type");
        }

        public int getId() {
            return getIntegerPropertyValue("ID");
        }

        public String getText() {
            return getWidgetPropertyTextValue("Text");
        }

        public String getFontName() {
            return getStringPropertyValue("FontName");
        }


        public int getFontSize() {
            return getIntegerPropertyValue("FontSize");
        }


        public String getFontStyle() {
            return getFontStyleProperty(getIntegerPropertyValue("FontStyle"));
        }

        public String getFontWeight() {
            return getFontWeightProperty(getIntegerPropertyValue("FontStyle"));
        }


        public String getTextColor() {
            return getStringPropertyValue("TextColor");
        }


        public String getTextAlign() {
            return getTextAlignProperty(getIntegerPropertyValue("TextAlign"));
        }

        public String getFrameColor() {
            return getStringPropertyValue("FrameColor");
        }

        public String getFillColor() {
            return getStringPropertyValue("FillColor");
        }

        public String[] getLinkNames() {
            String[] linkNameArray = null;
            try {
                Node node =
                    XPathAPI.selectSingleNode(fWidget, "./WidgetProperty[@Name='LinkList']/Value");
                if (node != null) {
                    NodeList linkList = XPathAPI.selectNodeList(node, "./Link");
                    // we have some custom links
                    if (linkList != null && linkList.getLength() > 0) {
                        linkNameArray = new String[linkList.getLength()];
                        for (int i = 0; i < linkList.getLength(); i++) {
                            Node nameNode;
                            Node linkNode = linkList.item(i).getFirstChild();
                            if (linkNode.getNodeName().equals("WebURL")) {
                                nameNode = XPathAPI.selectSingleNode(linkNode, "./Name");
                            } else {
                                nameNode = XPathAPI.selectSingleNode(linkNode, "./LinkName");
                            }
                            linkNameArray[i] = nameNode.getTextContent();
                        }
                    }
                }
            } catch (TransformerException e) {
                // nothing to do
            }
            return linkNameArray;
        }

        public boolean hasDefaultLink() {
            if (fHasDefaultLink == null) {
                fHasDefaultLink = false;
                try {
                    Node node =
                        XPathAPI.selectSingleNode(fWidget,
                            "./WidgetProperty[@Name='LinkList']/Value");
                    if (node != null) {
                        NodeList linkList = XPathAPI.selectNodeList(node, "./Link");
                        // we have some custom links
                        if (linkList != null && linkList.getLength() > 0) {
                            for (int i = 0; i < linkList.getLength(); i++) {
                                String defaultValue =
                                    getAttributeValue(linkList.item(i), "Default");
                                if (defaultValue != null && defaultValue.equalsIgnoreCase("true")) {
                                    fHasDefaultLink = true;
                                    break;
                                }
                            }
                        }
                    }
                } catch (TransformerException e) {
                    // nothing to do
                }
            }
            return fHasDefaultLink;
        }

        public boolean isLabelsVisible() {
            return getBooleanPropertyValue("LabelsVisible");
        }

        public String getStringPropertyValue(String propertyName) {
            Node valueNode = null;
            String stringValue = null;
            StringBuilder property = new StringBuilder(100);
            property.append("./WidgetProperty[@Name='");
            property.append(propertyName);
            property.append("']/Value");
            try {
                valueNode = XPathAPI.selectSingleNode(fWidget, property.toString());
            } catch (TransformerException e) {
                // nothing to do
            }
            if (valueNode != null) {
                Element valueElement = (Element) valueNode;
                stringValue = valueElement.getAttribute("Value");
            }
            return stringValue;
        }

        public Integer getIntegerPropertyValue(String propertyName) {
            Integer propertyValue = null;
            String stringValue = getStringPropertyValue(propertyName);
            if (stringValue != null) {
                propertyValue = Integer.parseInt(stringValue);
            }
            return propertyValue;
        }

        public Boolean getBooleanPropertyValue(String propertyName) {
            Boolean propertyValue = null;
            String stringValue = getStringPropertyValue(propertyName);
            if (stringValue != null) {
                if (stringValue.equalsIgnoreCase("true")
                    || stringValue.equalsIgnoreCase("false")) {
                    propertyValue = Boolean.valueOf(stringValue);
                }
            }

            return propertyValue;
        }

        private String getWidgetPropertyTextValue(String propertyName) {
            Node textValueNode = null;
            String propertyValue = null;
            StringBuilder property = new StringBuilder(100);
            property.append("./WidgetProperty[@Name='");
            property.append(propertyName);
            property.append("']/TextValue");
            try {
                textValueNode = XPathAPI.selectSingleNode(fWidget, property.toString());
            } catch (TransformerException e) {
                // nothing to do
            }
            if (textValueNode != null) {
                propertyValue = textValueNode.getTextContent();
            }
            return propertyValue;
        }

        private Point getDisplayBoxPoint(String pointType) {
            String stringValue;
            Point point = null;
            StringBuilder pointBuffer = new StringBuilder(100);
            pointBuffer.append("./DisplayBox/");
            pointBuffer.append(pointType);
            try {
                Node pointNode = XPathAPI.selectSingleNode(fWidget, pointBuffer.toString());
                Element pointElement = (Element) pointNode;

                stringValue = pointElement.getAttribute("XCoordinate");
                int x = Integer.parseInt(stringValue);

                stringValue = pointElement.getAttribute("YCoordinate");
                int y = Integer.parseInt(stringValue);

                point = new Point(x, y);
            } catch (TransformerException e) {
                // nothing to do
            }
            return point;
        }

        public List<Point> getPointListProperties() {
            Map<Integer, Integer> map = new HashMap<>();
            NodeList widgetProperties = null;
            try {
                widgetProperties = XPathAPI.selectNodeList(fWidget, "./WidgetProperty");
            } catch (TransformerException e) {
                // nothing to do
            }
            if (widgetProperties != null && widgetProperties.getLength() > 0) {
                for (int j = 0; j < widgetProperties.getLength(); j++) {
                    String propertyName = getAttributeValue(widgetProperties.item(j), "Name");
                    if (propertyName != null && (propertyName.startsWith("PointList"))) {
                        Integer index = Integer.parseInt(propertyName.substring(9));
                        Integer value = getIntegerPropertyValue(propertyName);
                        map.put(index, value);
                    }
                }
            }
            List<Point> pointList = new ArrayList<Point>();
            int i = 0;
            while (i < map.size()) {
                pointList.add(new Point(map.get(i), map.get(i + 1)));
                i += 2;
            }
            return pointList;
        }

        public Point createPoint(double x, double y) {
            return new Point(x, y);
        }
    }

    public static class Point {
        private double fXCoordinate;
        private double fYCoordinate;

        public Point(double x, double y) {
            this.fXCoordinate = x;
            this.fYCoordinate = y;
        }

        public double getXCoordinate() {
            return fXCoordinate;
        }

        public double getYCoordinate() {
            return fYCoordinate;
        }

        @Override
        public boolean equals(Object p) {
            boolean status;
            status = compare(this.fXCoordinate, ((Point) p).getXCoordinate());
            status &= compare(this.fYCoordinate, ((Point) p).getYCoordinate());
            return status;
        }

        private boolean compare(double d1, double d2) {
            return Math.round(d1 * 1000.0) / 1000.0 == Math.round(d2 * 1000.0) / 1000.0;
        }
    }

}
