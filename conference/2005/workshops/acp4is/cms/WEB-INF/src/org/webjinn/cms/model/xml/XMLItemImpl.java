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
import org.w3c.dom.NodeList;
import org.w3c.dom.Document;
import java.util.List;

/** XML-backed implementation of the Item element */
public class XMLItemImpl implements Item {

  /** Actual XML Element that stores this Menu object */
	private Element el;
	/** XML Document */
	private Document doc;
	/** Host CMS tree */
	private XMLCMSTree tree;

  //The following fields are auxiliary	
	/** Attributes child */
	private Node attributesNode;
	

	public XMLItemImpl(Element el,XMLCMSTree tree) {
		this.el=el;
		this.doc = el.getOwnerDocument();
		this.tree=tree;
		this.attributesNode = Utils.getChildNodeByName(el,XMLConstants.AttributesTagName);
	}

	/** Returns item name */
	public String getName() {
		return el.getAttribute("name");
	}

	/** Returns menu-uri of this Tree Element */
	public String getInterfaceURI() {
		return getParentMenu().getInterfaceURI()+":"+getName();
  }	

 /** Returns associated (parent) Menu element (if any) */
  public Menu getParentMenu() {
		Node parent = el.getParentNode();
		if (parent!=null && 
				parent.getNodeName().equals(XMLConstants.MenuTagName)) 
			return new XMLMenuImpl((Element)parent,tree);
		return null;		
	}

  /** Returns nested (child) Menu element */
  public Menu getChildMenu() {
		Node child = Utils.getChildNodeByName(el,XMLConstants.MenuTagName);
		if (child!=null)
			return new XMLMenuImpl((Element)child,tree);
		return null;				
	}

  /** Creates new Menu element and associates it with this Item element 
	 * (adds the Menu element as a child of the Item element) 
	 * Does nothing if the Item element is already 
	 * associated with a Menu element */
  public void associate(String menuName) {
		if (menuName==null || menuName.length()==0) return;
		Node child = Utils.getChildNodeByName(el,XMLConstants.MenuTagName);
		if (child!=null) return;
	  Node menu = XMLMenuImpl.createMenu(el.getOwnerDocument(),menuName);
		el.appendChild(menu);
		tree.updateFile(); //!!! Update !!!
	}

	/** Disconnects child Menu element from this Item.
	 * Child Menu is then removed from the tree 
	 * FIXME: more actions on removing are needed (fixing Pages, for example) */
  public void disassociate() {
		Node child = Utils.getChildNodeByName(el,XMLConstants.MenuTagName);		
		if (child==null) return;
    tree.updatePagesOnRemove(getChildMenu());		
		el.removeChild(child);
		tree.updateFile(); //!!! Update !!!		
	}

	/** Moves this Item element one position up or down 
	 * relatively to its siblings */
	public void moveItem(boolean up) {
		Node upNode = null;
		Node botNode = null;
		if (up) {
		  upNode = el.getPreviousSibling();
			botNode = el;
		} else {
			upNode = el;
			botNode = el.getNextSibling();
		}
		if (upNode==null || botNode==null) return;
		Node parent=el.getParentNode();
		parent.removeChild(botNode);
		parent.insertBefore(botNode, upNode);
		tree.updateFile();
	}

	/** Returns parent element for this element 
	 * or null if this element is a root element in the Interface */
	public InterfaceTreeElement getParent() {
	  return getParentMenu();	
	}

	/** Builds header code !!! TODO !!! */
	public String buildHeader() {
	  return "";	
	}

	/** Builds footer code !!! TODO !!! */
	public String buildFooter() {
		return "";
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
			Element attrEl = XMLAttributeImpl.createAttribute(doc,attrName, attrDescr, attrVal);
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

	/** Returns host CMS Tree */
	public CMSTree getCMSTree() {
		return tree;
	}
	

	/** Returns wrapped XML element */
	protected Element getXMLElement() {
		return el;
	}

	/** Creates new Item XML Element (without adding it into the tree) */
	protected static Element createItem(Document doc, String itemName) {
		Element item = Utils.createElementNode(doc,XMLConstants.ItemTagName);
		item.setAttribute("name", itemName);
		Element attributes = Utils.createElementNode(doc,XMLConstants.AttributesTagName);
		item.appendChild(attributes);
		return item;
	}
}
