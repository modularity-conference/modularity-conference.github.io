package org.webjinn.cms.model.xml;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.Document;
import org.webjinn.cms.model.Interface;
import org.webjinn.cms.model.Menu;
import org.webjinn.cms.model.CMSTree;


public class XMLInterfaceImpl implements Interface {

  /** Actual XML Element that stores this Interface object */ 
  private Element el;

  /** Root Menu object */
  private XMLMenuImpl menu;

  /** Host XMLCMSTree object */
  private XMLCMSTree tree;

  public XMLInterfaceImpl(Element el,XMLCMSTree tree) {
    //Precondition: node represents Interface node
    this.el = el;
    this.tree = tree;
    Node menuNode = Utils.getChildNodeByName(el,XMLConstants.MenuTagName);
    this.menu = new XMLMenuImpl((Element)menuNode, tree);
  }

  /** Returns Interface name */
  public String getName() {
    return el.getAttribute("name");
  }

  /** Returns Interface description */
  public String getDescription() {
    return el.getAttribute("description");
  }

  /** Returns root Menu object */
  public Menu getRootMenu() {
    return menu;
  }

  /** Returns host CMS Tree */
  public CMSTree getCMSTree() {
    return tree;
  }

  /** Creates new Interface XML Element (without adding it into the tree) */
  protected static Element createInterface(Document doc, String name, String descr) {
    Element interfaceEl = Utils.createElementNode(doc,XMLConstants.InterfaceTagName);
    interfaceEl.setAttribute("name", name);
    if (descr!=null) interfaceEl.setAttribute("description", descr);
    Element menu = XMLMenuImpl.createMenu(doc,"Main Menu");
    interfaceEl.appendChild(menu);
    return interfaceEl;
  }

  /** Returns wrapped XML element */
  protected Element getXMLElement() {
    return el;
  }

}

