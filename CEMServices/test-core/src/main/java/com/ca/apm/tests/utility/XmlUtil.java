package com.ca.apm.tests.utility;

import static org.testng.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import org.apache.xerces.util.DOMUtil;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

// import java.io.*;
// import com.sun.org.apache.xerces.internal.util.DOMUtil;

// import com.ca.wily.apm.qa.DefaultNodeNameComparator;


public class XmlUtil extends DOMUtil {

    public static boolean isEqual = true;

    public static void traverse(Node n) {

        if ((n instanceof Element) | true) {
            String nodename = n.getNodeName();
            String test = n.getNodeValue();
            // Print and continue traversing.
            System.out.println("Node: name=" + n.getNodeName() + " value=" + test);
        }

        // Now traverse the rest of the tree in depth-first order.
        if (n.hasChildNodes()) {
            // Get the children in a list.
            NodeList nl = n.getChildNodes();
            // How many of them?
            int size = nl.getLength();
            for (int i = 0; i < size; i++)
                // Recursively traverse each of the children.
                traverse(nl.item(i));
        }

    }

    public static void compare(Node n1, Node n2) {

        String name1 = n1.getNodeName();
        String value1 = n1.getNodeValue();

        String name2 = n2.getNodeName();
        String value2 = n2.getNodeValue();
        // Print and continue traversing.
        if (!name1.equals(name2)) {
            // System.out.println ("root-Node: name=" + n1.getParentNode().getNodeName());
            System.out.println("file 1-Node: name=" + name1 + " value=" + value1);
            System.out.println("file 2-Node: name=" + name2 + " value=" + value2);
        }
        assertTrue(name1.equals(name2));

        if (value1 != null) {
            if (!value1.equals(value2)) {
                // System.out.println ("root-Node: name=" + n1.getParentNode().getNodeName());
                System.out.println("file 1-Node: name=" + name1 + " value=\"" + value1 + "\"");
                System.out.println("file 2-Node: name=" + name2 + " value=\"" + value2 + "\"");
            }
            assertTrue(value1.equals(value2));
        }
        // checking attributes of a node
        NamedNodeMap nm1 = n1.getAttributes();
        NamedNodeMap nm2 = n2.getAttributes();
        if (nm1 != null) {
            assertTrue(nm1.getLength() == nm2.getLength());
            for (int a = 0; a < nm1.getLength(); a++) {
                String aname1 = nm1.item(a).getNodeName();
                String avalue1 = nm1.item(a).getNodeValue();
                String aname2 = nm2.item(a).getNodeName();
                String avalue2 = nm2.item(a).getNodeValue();
                if (!aname1.equals(aname2) | !avalue1.equals(avalue2)) {
                    System.out.println("file1 Node attribute name:" + aname1 + ", value:"
                        + nm1.item(a).getNodeValue());
                    System.out.println("file2 Node attribute name:" + aname2 + ", value:"
                        + nm2.item(a).getNodeValue());
                }
                assertTrue(aname1.equals(aname2));
                assertTrue(avalue1.equals(avalue2));
            }
        }

        // Now traverse the rest of the tree in depth-first order.
        if (n1.hasChildNodes()) {
            // Get the children in a list.
            NodeList nlist1 = n1.getChildNodes();
            NodeList nlist2 = n2.getChildNodes();
            // How many of them?
            int size1 = nlist1.getLength();
            int size2 = nlist2.getLength();
            if (size1 != size2) {
                System.out.println("The number of nodes different in both files. root node: "
                    + name1);
                System.out.println("Nodes found in file1:" + size1 + ", Nodes found in file2:"
                    + size2);
                assertTrue(size1 == size2);
            }
            for (int i = 0; i < size1; i++)
                // Recursively traverse each of the children.
                compare(nlist1.item(i), nlist2.item(i));
        }
    }

    public static void compareWithoutSpaces(Node n1, Node n2) {

        String name1 = n1.getNodeName().trim();
        String value1 = n1.getNodeValue();
        if (value1 != null) value1 = value1.trim();

        String name2 = n2.getNodeName().trim();
        String value2 = n2.getNodeValue();
        if (value2 != null) value2 = value2.trim();
        // Print and continue traversing.
        if (!name1.equals(name2)) {
            // System.out.println ("root-Node: name=" + n1.getParentNode().getNodeName());
            System.out.println("file 1-Node: name=" + name1 + " value=" + value1);
            System.out.println("file 2-Node: name=" + name2 + " value=" + value2);
        }
        assertTrue(name1.equals(name2));

        if (value1 != null) {
            if (!value1.equals(value2)) {
                // System.out.println ("root-Node: name=" + n1.getParentNode().getNodeName());
                System.out.println("file 1-Node: name=" + name1 + " value=\"" + value1 + "\"");
                System.out.println("file 2-Node: name=" + name2 + " value=\"" + value2 + "\"");
            }
            assertTrue(value1.equals(value2));
        }
        // checking attributes of a node
        NamedNodeMap nm1 = n1.getAttributes();
        NamedNodeMap nm2 = n2.getAttributes();
        if (nm1 != null) {
            assertTrue(nm1.getLength() == nm2.getLength());
            for (int a = 0; a < nm1.getLength(); a++) {
                String aname1 = nm1.item(a).getNodeName().trim();
                String avalue1 = nm1.item(a).getNodeValue();
                if (avalue1 != null) avalue1 = avalue1.trim();
                String aname2 = nm2.item(a).getNodeName().trim();
                String avalue2 = nm2.item(a).getNodeValue();
                if (avalue2 != null) avalue2 = avalue2.trim();
                if (!aname1.equals(aname2) | !avalue1.equals(avalue2)) {
                    System.out.println("file1 Node attribute name:" + aname1 + ", value:"
                        + nm1.item(a).getNodeValue());
                    System.out.println("file2 Node attribute name:" + aname2 + ", value:"
                        + nm2.item(a).getNodeValue());
                }
                assertTrue(aname1.equals(aname2));
                assertTrue(avalue1.equals(avalue2));
            }
        }

        // Now traverse the rest of the tree in depth-first order.
        if (n1.hasChildNodes()) {
            // Get the children in a list.
            NodeList nlist1 = n1.getChildNodes();
            NodeList nlist2 = n2.getChildNodes();
            // How many of them?
            int size1 = nlist1.getLength();
            int size2 = nlist2.getLength();
            if (size1 != size2) {
                System.out.println("The number of nodes different in both files. root node: "
                    + name1);
                System.out.println("Nodes found in file1:" + size1 + ", Nodes found in file2:"
                    + size2);
                assertTrue(size1 == size2);
            }
            for (int i = 0; i < size1; i++)
                // Recursively traverse each of the children.
                compareWithoutSpaces(nlist1.item(i), nlist2.item(i));
        }
    }


    /**
     * Sorts the children of the given node upto the specified depth if
     * available
     * 
     * @param node -
     *        node whose children will be sorted
     * @param descending -
     *        true for sorting in descending order
     * @param depth -
     *        depth upto which to sort in DOM
     * @param comparator -
     *        comparator used to sort, if null a default NodeName
     *        comparator is used.
     */
    public static void sortChildNodes(Node node, boolean descending, int depth,
        Comparator comparator) {

        List nodes = new ArrayList();
        NodeList childNodeList = node.getChildNodes();
        if (depth > 0 && childNodeList.getLength() > 0) {
            for (int i = 0; i < childNodeList.getLength(); i++) {
                Node tNode = childNodeList.item(i);
                sortChildNodes(tNode, descending, depth - 1, comparator);
                // Remove empty text nodes
                if ((!(tNode instanceof Text))
                    || (tNode instanceof Text && ((Text) tNode).getTextContent().trim().length() > 1)) {
                    nodes.add(tNode);
                }
            }
            Comparator comp = (comparator != null) ? comparator : new DefaultNodeNameComparator();
            if (descending) {
                // if descending is true, get the reverse ordered comparator
                Collections.sort(nodes, Collections.reverseOrder(comp));
            } else {
                Collections.sort(nodes, comp);
            }

            for (Iterator iter = nodes.iterator(); iter.hasNext();) {
                Node element = (Node) iter.next();
                node.appendChild(element);
            }
        }

    }

}


class DefaultNodeNameComparator implements Comparator {

    public int compare(Object arg0, Object arg1) {
        return ((Node) arg0).getNodeName().compareTo(((Node) arg1).getNodeName());
    }

}
