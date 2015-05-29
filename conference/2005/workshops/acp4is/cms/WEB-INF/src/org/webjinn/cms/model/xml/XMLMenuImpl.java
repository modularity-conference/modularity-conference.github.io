package org.webjinn.cms.model.xml;
import org.webjinn.cms.model.Menu;
import org.webjinn.cms.model.CMSTree;
import org.webjinn.cms.model.Item;
import org.webjinn.cms.model.Interface;
import org.webjinn.cms.model.InterfaceTreeElement;
import org.webjinn.cms.model.Attribute;
import org.webjinn.cms.model.CSSFile;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.Document;
import java.util.List;

/** XML-backed implementation of the Menu element */
public class XMLMenuImpl implements Menu {

  /** Actual XML Element that stores this Menu object */
	private Element el;
	/** Host CMS tree */
	private XMLCMSTree tree;

  //The following fields are auxiliary	
	/** Attributes child */
	private Node attributesNode;
	/** CSSFiles child */
	private Node cssfilesNode;
	

	public XMLMenuImpl(Element el,XMLCMSTree tree) {
		this.el=el;
		this.tree=tree;
		this.attributesNode = Utils.getChildNodeByName(el,XMLConstants.AttributesTagName);
		this.cssfilesNode = Utils.getChildNodeByName(el,XMLConstants.CSSFilesTagName);		
	}

	/** Returns menu name */
	public String getName() {
		return el.getAttribute("name");
	}

	/** Returns menu-uri of this Tree Element */
	public String getInterfaceURI() {
		StringBuffer result = new StringBuffer();
		Node node = el;
		Node parent = node.getParentNode();
		result.append(el.getAttribute("name"));		
		while (!parent.getNodeName().equals(XMLConstants.InterfaceTagName)) {
		 node = node.getParentNode().getParentNode(); 
		 result.insert(0,((Element)node).getAttribute("name")+"/");
		 parent = node.getParentNode();
		}
		result.insert(0,((Element)parent).getAttribute("name")+"/");
		return result.toString();
	}
	
	
	
	/** Returns header string associated with the menu */
	public String getHeader() {
		Node CDATA = getChildCDATA(XMLConstants.HeaderTagName);
		if (CDATA!=null) 
			return CDATA.getNodeValue();
		else 
			return "";
	}

	/** Returns footer string associated with the menu */
	public String getFooter() {
		Node CDATA = getChildCDATA(XMLConstants.FooterTagName);
		if (CDATA!=null) 
			return CDATA.getNodeValue();
		else 
			return "";
	}

	/** Sets new header into the Menu */
	public void setHeader(String header) {
		Node CDATA = getChildCDATA(XMLConstants.HeaderTagName);
		CDATA.setNodeValue(header);
		tree.updateFile(); //!!! Update !!!
	}
	
	/** Sets new footer into the Menu */
	public void setFooter(String footer) {
		Node CDATA = getChildCDATA(XMLConstants.FooterTagName);
		CDATA.setNodeValue(footer);
		tree.updateFile(); //!!! Update !!!		
	}

  /** Returns associated (parent) Item element 
	 * (or null if it is the root menu) */
  public Item getParentItem() {
		Node parent = el.getParentNode();
		if (parent!=null && 
				parent.getNodeName().equals(XMLConstants.ItemTagName)) 
			return new XMLItemImpl((Element)parent,tree);
		return null;
	}

  /** Returns nested Item elements */
  public Item[] getChildItems() {
		List items = Utils.getChildNodesByName(el,XMLConstants.ItemTagName);
		int length = items.size();
		Item[] result = new Item[length];
		for (int i=0;i<length;i++)
			result[i] = new XMLItemImpl((Element)items.get(i),tree);
		return result;		
	}

  /** Returns child Item with the name specified or null if not found */
  public Item getChildItem(String itemName) {
		List items = Utils.getChildNodesByName(el,XMLConstants.ItemTagName);
		for (int i=0;i<items.size();i++)
		  if (((Element)items.get(i)).getAttribute("name").equals(itemName))	
				return new XMLItemImpl((Element)items.get(i), tree);
		return null;						
	}

	/** Adds new Item element as a child of this Menu element.
	 * itemName must not be empty or null and must be unique among
	 * names of all children Items of this Menu */
	public void addItem(String itemName) {
	  if (itemName==null || itemName.length()==0 || 
				getChildItem(itemName)!=null) return;	
		Element itemEl = XMLItemImpl.createItem(el.getOwnerDocument(),itemName);
		el.appendChild(itemEl);
		tree.updateFile(); //!!! Update !!!				
	}

	/** Removes Item from the list of children of this Menu
	 * as well as from the tree. Do nothing if Item is not 
	 * a child of this Menu */
	public void removeItem(Item item) {
		if (item instanceof XMLItemImpl) {
			tree.updatePagesOnRemove(item);
			Element itemEl = ((XMLItemImpl)item).getXMLElement();
			el.removeChild(itemEl);
			tree.updateFile(); //!!! Update !!!
		}				
	}

	/** Returns parent element for this element 
	 * or null if this element is a root element in the Interface */
	public InterfaceTreeElement getParent() {
	  return getParentItem();	
	}

	/** Builds header code !!! TODO !!! */
	public String buildHeader() {
	  return "";	
	}

	/** Builds footer code !!! TODO !!! */
	public String buildFooter() {
		return "";
	}

	/** Retrieves a list of CSSFile objects stored in the container */
	public CSSFile[] getCSSFiles() {
		List cssfiles = Utils.getChildNodesByName(
				cssfilesNode,XMLConstants.CSSFileTagName);
		int length = cssfiles.size();
		CSSFile[] result = new CSSFile[length];
		for (int i=0;i<length;i++)
			result[i] = new XMLCSSFileImpl((Element)cssfiles.get(i),this);
		return result;				
	}
	
	/** Retrieves a CSSFile object by its url from the container or
	 * null if not found */
	public CSSFile getCSSFile(String url) {
		if (url==null) return null;
		List cssfiles = Utils.getChildNodesByName(
				cssfilesNode,XMLConstants.CSSFileTagName);
		int length = cssfiles.size();
		for (int i=0;i<length;i++) {
			Element cssfile = (Element)cssfiles.get(i);
			String urlAttr = cssfile.getAttribute("url");
			if (urlAttr!=null && urlAttr.equals(url)) 
				return new XMLCSSFileImpl(cssfile,this);
		}
		return null;				
	}

	/** Creates CSS file using url provided and 
	 * places it in the container */
	public void addCSSFile(String url) {
	  if (url==null || url.length()==0) return;	
		Element cssfileEl = XMLCSSFileImpl.createCSSFile(el.getOwnerDocument(),url);
		cssfilesNode.appendChild(cssfileEl);
		tree.updateFile(); //!!! Update !!!						
	}

	/** Removes a CSSFile from the container.
	 * Does nothing if argument CSSFile is not found
	 * in the container */
	public void removeCSSFile(CSSFile cssfile) {
		if (cssfile instanceof XMLCSSFileImpl) {
			Element cssfileEl = ((XMLCSSFileImpl)cssfile).getXMLElement();
			cssfilesNode.removeChild(cssfileEl);
			tree.updateFile(); //!!! Update !!!
		}						
	}


	/** Retrieves a list of attributes stored in the container */
	public Attribute[] getAttributes() {
		List attributes = Utils.getChildNodesByName(
				attributesNode,XMLConstants.AttributeTagName);
		int length = attributes.size();
		Attribute[] result = new Attribute[length];
		for (int i=0;i<length;i++)
			result[i] = new XMLAttributeImpl((Element)attributes.get(i),this);
		return result;						
	}

	/** Retrieves attribute from the container by its name */
	public Attribute getAttribute(String attrName) {
		List attributes = Utils.getChildNodesByName(attributesNode,XMLConstants.AttributeTagName);
		for (int i=0;i<attributes.size();i++)
		  if (((Element)attributes.get(i)).getAttribute("name").equals(attrName))	
				return new XMLAttributeImpl((Element)attributes.get(i), this);
		return null;								
	}

	/** Adds an attribute to the container. 
	 * if attribute with attrName already exists, method
	 * ovverides its value and description with new values */
	public void addAttribute(String attrName, String attrDescr, String attrVal) {
		if (attrName==null || attrName.length()==0) return;	
		Attribute attr = getAttribute(attrName);
		if (attr!=null) {
			if (attrDescr==null) attrDescr="";
			if (attrVal==null) attrVal="";
			attr.setDescription(attrDescr);
			attr.setValue(attrVal);
		} else {
			Element attrEl = XMLAttributeImpl.createAttribute(el.getOwnerDocument(),attrName, attrDescr, attrVal);
			attributesNode.appendChild(attrEl);
		}
		tree.updateFile(); //!!! Update !!!								
	}

	/** Removes an attribute from the container.
	 * Does nothing if argument attribute is not an attribute
	 * of this container */
	public void removeAttribute(Attribute attr) {
		if (attr instanceof XMLAttributeImpl) {
			Element attrEl = ((XMLAttributeImpl)attr).getXMLElement();
			attributesNode.removeChild(attrEl);
			tree.updateFile(); //!!! Update !!!
		}								
	}

	/** Obtains the content of CDATA section of the menu 
	 * child tag (used for header and footer access) */
	private Node getChildCDATA(String childTagName) {
		try{
		  Node child = Utils.getChildNodeByName(el,childTagName);
		  return Utils.getCDATAChild(child);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}		
	}

	/** Returns host CMS Tree */
	public CMSTree getCMSTree() {
		return tree;
	}
	

	/** Returns wrapped XML element */
	protected Element getXMLElement() {
		return el;
	}

	/** Creates new Menu XML Element (without adding it into the tree) */
	protected static Element createMenu(Document doc, String menuName) {
		Element menu = Utils.createElementNode(doc,XMLConstants.MenuTagName);
		menu.setAttribute("name", menuName);
		Element header = Utils.createElementNode(doc,XMLConstants.HeaderTagName);
		Element footer = Utils.createElementNode(doc,XMLConstants.FooterTagName);
		Element attributes = Utils.createElementNode(doc,XMLConstants.AttributesTagName);
		Element cssfiles = Utils.createElementNode(doc,XMLConstants.CSSFilesTagName);
		menu.appendChild(header);
		menu.appendChild(footer);
		menu.appendChild(attributes);
		menu.appendChild(cssfiles);
		return menu;
	}
}
