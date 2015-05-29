package org.webjinn.cms.model.xml;
import org.webjinn.cms.model.Attribute;
import org.webjinn.cms.model.CMSTreeNode;
import org.w3c.dom.Node;
import org.w3c.dom.Element;
import org.w3c.dom.Document;

/** An implementation of the Attribute interface */
public class XMLAttributeImpl implements Attribute {

	/** XML Attribute element */
	private Element el;
	/** CMS Tree host */
	private CMSTreeNode host;

	public XMLAttributeImpl(Element el, CMSTreeNode host) {
		this.el=el;
		this.host=host;
	}

	/** Returns host node */
	public CMSTreeNode getHost() {
		return host;
	}

	/** Returns attribute name */
	public String getName() {
		return el.getAttribute("name");
	}

	/** Returns attribute value (empty string if none) */
	public String getValue() {
		Node CDATA = Utils.getCDATAChild(el);
		return CDATA.getNodeValue();
	}

	/** Returns attribute description */
	public String getDescription() {
		return el.getAttribute("description");
	}

	/** Sets description */
	public void setDescription(String descr) {
		el.setAttribute("description", descr);
    Utils.update(host.getCMSTree()); //!!! Update !!!
	}

	/** Sets value */
	public void setValue(String val) {
		try{
  		Node CDATA = Utils.getCDATAChild(el);
			CDATA.setNodeValue(val);
			Utils.update(host.getCMSTree()); //!!! Update !!!
		} catch (Exception e) {
			e.printStackTrace();
		}		
	}

	/** Returns wrapped XML element */
	protected Element getXMLElement() {
		return el;
	}	


	/** Creates new Attribute XML Element (without adding it into the tree) */
	protected static Element createAttribute(Document doc, String name, String descr, String value) {
		Element attribute = Utils.createElementNode(doc,XMLConstants.AttributeTagName);
		attribute.setAttribute("name", name);
		if (descr!=null) attribute.setAttribute("description", descr);
		if (value!=null) {
			Node CDATA = Utils.getCDATAChild(attribute);
			CDATA.setNodeValue(value);
		}
		return attribute;
	}		

}
