package org.webjinn.cms.model.xml;
import org.webjinn.cms.model.CSSFile;
import org.webjinn.cms.model.CMSTreeNode;
import org.w3c.dom.Node;
import org.w3c.dom.Document;
import org.w3c.dom.Element;


/** Implementation of the CSSFile interface */
public class XMLCSSFileImpl implements CSSFile {

	/** XML Attribute element */
	private Element el;
	/** CMS Tree host */
	private CMSTreeNode host;

	public XMLCSSFileImpl(Element el, CMSTreeNode host) {
		this.el=el;
		this.host=host;
	}

	/** Returns host node */
	public CMSTreeNode getHost() {
		return host;
	}

	/** Returns css file URL */
	public String getURL() {
		return el.getAttribute("url");
	}

	/** Returns wrapped XML element */
	protected Element getXMLElement() {
		return el;
	}	

	/** Creates new CSSFile XML Element (without adding it into the tree) */
	protected static Element createCSSFile(Document doc, String url) {
		Element cssfile = Utils.createElementNode(doc,XMLConstants.CSSFileTagName);
		cssfile.setAttribute("url", url);
		return cssfile;
	}	

}
