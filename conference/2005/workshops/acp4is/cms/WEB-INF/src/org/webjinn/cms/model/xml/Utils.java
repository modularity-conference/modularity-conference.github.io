package org.webjinn.cms.model.xml;
import org.w3c.dom.*;
import org.webjinn.cms.model.*;

import org.apache.xerces.dom.CoreDocumentImpl;
import org.apache.xerces.dom.CDATASectionImpl;
import org.apache.xerces.dom.ElementImpl;

import java.util.List;
import java.util.Vector;


/** Set of useful auxiliaritity functions */
public class Utils {

	/** Returns attribute value of a node. null if no attribute found */
	public static String getAttributeValue(Node node, String attrName) {
		if (node==null || attrName==null) return null;
		NamedNodeMap attrs = node.getAttributes();
		Node attr = attrs.getNamedItem(attrName);
		if (attr!=null) return attr.getNodeValue();
		return null;
	}
	
	/** Returns first element of the node children NodeList 
	 * with specified tag name or null if not found */
	public static Node getChildNodeByName(Node node, String childTagName) {
		NodeList list = node.getChildNodes();
		int length = list.getLength();
		for (int i=0;i<length;i++) {
			Node n = list.item(i);
			if (n.getNodeName().equals(childTagName))
				return n;
		}
		return null;		
	}

		/** Returns list of node child elements  
	 * with specified tag name or empty list if not found */
	public static List getChildNodesByName(Node node, String childTagName) {
		NodeList list = node.getChildNodes();
		Vector result = new Vector();
		try{
			int length = list.getLength();
			for (int i=0;i<length;i++) {
				Node n = list.item(i);
				if (n.getNodeName().equals(childTagName))
					result.add(n);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;		
	}

	
	/** Returns NodeList of attribute nodes located within
	 * node provided. The argument node must be Page, Menu or Item */
	public static NodeList getAttributeNodes(Node container) {
		Node attributesNode =  getChildNodeByName(container, "attributes");
		if (attributesNode!=null && attributesNode instanceof Element)
			return ((Element)attributesNode).getElementsByTagName("attribute");
	  return null;
	}

	/** Returns NodeList of cssfile nodes located within the
	 * node provided. The argument node must be Page or Menu */
	public static NodeList getCSSFileNodes(Node container) {
		Node cssfilesNode =  getChildNodeByName(container, "cssfiles");
		if (cssfilesNode!=null && cssfilesNode instanceof Element)
			return ((Element)cssfilesNode).getElementsByTagName("cssfile");
	  return null;
	}

	/** Auxiliary method: tries to obtain CDATA child of the Node provided. 
	 * If not found, creates one with empty content,
	 * appends it as a child of the node and returns the CDATA
	 * node created. Is used to ensure, that CDATA elements are created
	 * when accessed */
	public static Node getCDATAChild(Node host) {
		Node CDATA = getChildNodeByName(host,"#cdata-section");
		if (CDATA==null) {
			CDATA = createCDATANode(host.getOwnerDocument(),"");  
			host.appendChild(CDATA);
		}
		return CDATA;
	}

	/** Creates new CDATA Node, 
	 *  returns null if failed to create */
	public static Node createCDATANode(Document doc, String data) {
		if (doc instanceof CoreDocumentImpl) {
			CoreDocumentImpl xercesDoc = (CoreDocumentImpl)doc;
			CDATASectionImpl result = new CDATASectionImpl(xercesDoc,data);
			return result;
		}
		return null;
	}

	/** Creates fresh new ready-to-go plug-and-play Element Node, 
	 *  returns null if failed to create */
	public static Element createElementNode(Document doc, String tagName) {
		if (doc instanceof CoreDocumentImpl) {
			CoreDocumentImpl xercesDoc = (CoreDocumentImpl)doc;
			ElementImpl result = new ElementImpl(xercesDoc,tagName);
			return result;
		}
		return null;
	}
	
  	

	/** Updates changes made in CMS tree into the xml file */
	public static void update(CMSTree tree) {
		if (tree instanceof XMLCMSTree) {
			((XMLCMSTree)tree).updateFile();
		}
	}
	
	/*
	public static XMLInterfaceImpl toInterface(Node node) {
		if (node==null || 
				node.getNodeType()!=Node.ELEMENT_NODE ||
				!node.getNodeName().equals(XMLConstants.InterfaceTagName))
			return null;
		return new XMLInterfaceImpl(node);
	}

	public static XMLMenuImpl toMenu(Node node) {
		if (node==null || 
				node.getNodeType()!=Node.ELEMENT_NODE ||
				!node.getNodeName().equals(XMLConstants.MenuTagName))
			return null;
		return new XMLMenuImpl(node);		
	} */

	/** Returns the content of the first CDATA node nested within
	 * argument node or null if none */
	/*
	public static String getCDATAContent(Node host) {
		try{
			return getChildNodeByName(host,"#cdata-section").getNodeValue();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	} */


}

