package org.webjinn.cms.model.xml;
import org.webjinn.cms.model.CMSTree;
import org.webjinn.cms.model.Interface;
import org.webjinn.cms.model.InterfaceTreeElement;
import org.webjinn.cms.model.Page;
import org.webjinn.cms.model.Menu;
import org.webjinn.cms.model.Item;
import org.webjinn.cms.model.FileUtils;

//XML
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Element;
//JDK
import java.io.File;
import java.io.FileOutputStream;
import java.util.List;
import java.util.Vector;
import java.util.StringTokenizer;
//From XERCES examples
import dom.Writer;
import org.apache.xerces.parsers.DOMParser;
//import dom.wrappers.Xerces;




/** XML-backed implementation of the CMS tree 
 * FIXME: change parser stuff to normal */
public class XMLCMSTree implements CMSTree {

	/** The url of an xml file that stores CMS tree */
	private String fileURI;

	/** DOM representation of the CMS tree */
	private Document doc;

  /** DOM Writer */
	private	Writer writer;

	//The following are auxiliary fields 
	/** XML Element that represents Tree root node */
	private Node rootNode;
	/** XML Element that represents interfaces node */
	private Node interfacesNode;
	/** XML Element that represents pages node */
	private Node pagesNode;

	

	public XMLCMSTree(String fileURI) {
		this.fileURI=fileURI;
		this.doc = getDOMTree();
		this.rootNode = doc.getDocumentElement();
		this.interfacesNode = Utils.getChildNodeByName(rootNode,XMLConstants.InterfacesTagName);
		this.pagesNode = Utils.getChildNodeByName(rootNode,XMLConstants.PagesTagName);
		writer = new Writer();
	}


	/** Retrieves array of interfaces */	
	public Interface[] getInterfaces() {
		List interfaces = getInterfaceNodes();
		int length = interfaces.size();
		Interface[] result = new Interface[length];
		for (int i=0;i<length;i++)
			result[i] = new XMLInterfaceImpl((Element)interfaces.get(i), this);
		return result;
	}

	/** Retrieves Interface by name or null if not found */
	public Interface getInterface(String name) {
		List interfaces = getInterfaceNodes();
		for (int i=0;i<interfaces.size();i++)
		  if (((Element)interfaces.get(i)).getAttribute("name").equals(name))	
				return new XMLInterfaceImpl((Element)interfaces.get(i), this);
		return null;		
	}


	/** Adds a new interface to the tree.
	 * Interface name provide must be not null, not empty and
	 * unique among interfaces already in the tree 
	 * If interface with the name provided already exists, 
	 * method does nothing */
	public void addInterface(String name, String descr) {
	  if (name==null || name.length()==0 || getInterface(name)!=null) return;	
		Element interfaceEl = 
			XMLInterfaceImpl.createInterface(doc,name,descr);
		interfacesNode.appendChild(interfaceEl);
		updateFile(); //!!! Update !!!
	}

	/** Removes an interface from the tree. 
	 * Do nothing if Interface object provided is not
	 * found in the tree */
	public void removeInterface(Interface in) {
		if (in instanceof XMLInterfaceImpl) {
			Element inEl = ((XMLInterfaceImpl)in).getXMLElement();
			updatePagesOnRemove(in.getRootMenu());
			interfacesNode.removeChild(inEl);
			updateFile(); //!!! Update !!!
		}
	}

	/** Given an interface-uri string returns corresponding tree element 
	 * or null if not found 
	 * interface-uri: 
	 *  item: "interface-name/menu-name/menu-name/.../menu-name:item-name"
	 *  menu: "interface-name/menu-name/menu-name/.../menu-name"
	 * FIXME: code is stupid. */
	public InterfaceTreeElement resolveInterfaceURI(String uri) {
		try{
			StringTokenizer st = new StringTokenizer(uri,"/");
			Interface in = getInterface(st.nextToken());
			Menu menu = in.getRootMenu();
			String section = st.nextToken();
			String menuName = getMenuName(section);
			if (menuName==null) return null;
			String itemName = getItemName(section);
			if (!menu.getName().equals(menuName)) return null;
			if (st.hasMoreTokens()) {
				if (itemName!=null) return null;
			} else 
				if (itemName==null) 
					return menu; 
				else 
					return menu.getChildItem(itemName);
			InterfaceTreeElement result = null; 
			while (st.hasMoreTokens()) {
				section = st.nextToken();
				result = getTreeElement(menu,section);
				if (result==null) return null;
				if (st.hasMoreTokens())
					if (result instanceof Menu) 
						menu = (Menu)result; 
					else 
						return null;
			}
			return result;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	

	/** Retrieves array of pages */
	public Page[] getPages() {
		List pages = getPageNodes();
		int length = pages.size();
		Page[] result = new Page[length];
		for (int i=0;i<length;i++)
			result[i] = new XMLPageImpl((Element)pages.get(i), this);
		return result;		
	}

	/** Returns Page object by file URI or null if not found */
	public Page getPage(String fileURI) {
		List pages = getPageNodes();
		for (int i=0;i<pages.size();i++)
		  if (((Element)pages.get(i)).getAttribute("file-uri").equals(fileURI))	
				return new XMLPageImpl((Element)pages.get(i), this);
		return null;				
	}

	/** Retrieves array of Pages 
	 * associated with element specified by the interfaceURI argument.
	 *  If there are no such Pages exists, returns array of size 0 
	 *  (Here uri may resolve not only menus and items but an interface as well. 
	 *  If interfaceURI is null or empty string, all pages in the tree 
	 *  are returned) */
	public Page[] getAssociatedPages(String interfaceURI) {
    if (interfaceURI==null || interfaceURI.length()==0) return getPages();		
		List pages = getPageNodes();
		Vector result = new Vector();
		for (int i=0;i<pages.size();i++)
		  if (((Element)pages.get(i)).getAttribute("interface-uri").equals(interfaceURI))
				result.add(new XMLPageImpl((Element)pages.get(i), this));
		return (Page[])result.toArray(new Page[0]);
	}

	/** Returns array of Pages associated with a tree 
	 * element specified by the interfaceURI argument or with any 
	 * of its descendants.
	 * (Here uri may resolve not only menus and items but an interface as well
	 * If interfaceURI is null or empty string, method  
	 * returns all the Page objects in the CMS tree) */
 	public Page[] getRelatedPages(String interfaceURI) {	
		if (interfaceURI==null || interfaceURI.length()==0) return getPages();
		String itemURI = null;
		String menuURI = null;
		
	        InterfaceTreeElement el = resolveInterfaceURI(interfaceURI);
		if (el==null) return new Page[0]; 
		if (el instanceof Item) {
	         itemURI = el.getInterfaceURI();
		 if (((Item)el).getChildMenu()!=null)
		 menuURI = ((Item)el).getChildMenu().getInterfaceURI();
		} 
		if (el instanceof Menu) {
		  menuURI = el.getInterfaceURI();	
		} 
	        //related are those that are either attached to the item
		//or attached to the elements under the menu (including the menu itself)	
		List pages = getPageNodes();
		Vector result = new Vector();
		for (int i=0;i<pages.size();i++) {
		  String pageIntURI = ((Element)pages.get(i)).getAttribute("interface-uri");
		  if ((itemURI!=null && pageIntURI.equals(itemURI)) ||
		      (menuURI!=null && pageIntURI.startsWith(menuURI))) {
				result.add(new XMLPageImpl((Element)pages.get(i), this));
		      }
		}
		return (Page[])result.toArray(new Page[0]);		
	}

	/** Retrieves array of Pages associated with files 
	 * located in the directory specified by argument fileURI. 
	 * if includeSubDirs argument is true, then method recurse
	 * on subdirs of the directory specified by fileURI. 
	 * Returns empty array if fileURI is null.*/
	public Page[] getDirectoryPages(String fileURI, boolean includeSubDirs) {
		if (fileURI==null) return new Page[0];
		fileURI.replace(File.separatorChar,'/'); //??? Remove?
		if (fileURI.endsWith("/")) fileURI=fileURI.substring(0,fileURI.length()-1);

		List pages = getPageNodes();
		Vector result = new Vector();
		for (int i=0;i<pages.size();i++) {
			Element page = (Element)pages.get(i);
			String uri = page.getAttribute("file-uri");
			if (includeSubDirs) {
				if (uri.startsWith(fileURI)) 
					result.add(new XMLPageImpl(page, this));
			} else {
				String dirURI;
				int ind = uri.lastIndexOf('/');
				if (ind<=0) dirURI = ""; else dirURI = uri.substring(0,ind);
				if (dirURI.equals(fileURI)) 
					result.add(new XMLPageImpl(page, this));
			}
		}
		return (Page[])result.toArray(new Page[0]);				
	}

	
	/** Adds new page association to the tree. */
	public void addPage(String fileURI, String interfaceURI) {
	  if (fileURI==null || interfaceURI==null || 
				getPage(fileURI)!=null || resolveInterfaceURI(interfaceURI)==null) return;	
		Element pageEl = XMLPageImpl.createPage(doc,fileURI,interfaceURI);
		pagesNode.appendChild(pageEl);
		updateFile(); //!!! Update !!!		
	}

	/** Removes a page from the tree. Do nothing if
	 * this page object is not found in the tree */
	public void removePage(Page page) {
    if (page==null) return;
		if (page instanceof XMLPageImpl) {
			removePage((XMLPageImpl)page);
			updateFile(); //!!! Update !!!
		}
	}

 /** Removes an array of pages from the tree.
	 * Do nothing if argument is null. 
	 * Do nothing for page objects that are not found in the tree
	 * Semantically identical to the operation, that applies removePage 
	 * method to each object from the argument array. */
	public void removePages(Page[] pages) {
		if (pages==null) return;
		for (int i=0;i<pages.length;i++)
			if (pages[i] instanceof XMLPageImpl)
				removePage((XMLPageImpl)pages[i]);
		updateFile(); //!!! Update !!!
	}
	

	/** Returns absolute file path of a web-site root dir on the server */ 
        public String getRootDir() {
          String rootDir=((Element)rootNode).getAttribute("rootdir");
          if (rootDir==null) rootDir="";
          try{
            if (!(new File(rootDir).isAbsolute())) {
              File XMLFile=new File(fileURI).getParentFile();
              rootDir = new File(XMLFile, rootDir).getAbsolutePath();
            }
          } catch (Exception e) { } 
          return rootDir;
        }

	

	/** Saves memory copy of XML CMS tree into the source xml file */ 
	protected void updateFile() {
		try{
			FileOutputStream fout = new FileOutputStream(new File(fileURI));
			try{
				writer.setOutput(fout,null);
				writer.write(doc);
			} finally {fout.close();}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/** Updates Page objects when removing InterfaceTreeElement */
	protected void updatePagesOnRemove(InterfaceTreeElement el) {
		Page[] pages = getRelatedPages(el.getInterfaceURI());
		for (int i=0;i<pages.length;i++)
			//For efficiency reasons (to avoid updateFile on every removing op.), 
			//removePage(pages[i]) code is inlined
			if (pages[i] instanceof XMLPageImpl) {
			      String fileURI = FileUtils.getAbsoluteFilePath(pages[i]);
			       FileUtils.setFileContent(fileURI,FileUtils.getPageContent(pages[i]));
				
				//pages[i].setPageContent(pages[i].getPageContent());
				Element pageEl = ((XMLPageImpl)pages[i]).getXMLElement();
				pagesNode.removeChild(pageEl);
			}				
		//updateFile(); Actual file update is performed by callers
	}

	private void removePage(XMLPageImpl page) {
		if (page==null) return;
		try{
			String fileURI = FileUtils.getAbsoluteFilePath(page);
			FileUtils.setFileContent(fileURI,FileUtils.getPageContent(page));
			Element pageEl = page.getXMLElement();
			pagesNode.removeChild(pageEl);			
		} catch (Exception e) {
		}
	}

	/** Returns List of Interface nodes */
	private List getInterfaceNodes() {
		return Utils.getChildNodesByName(
				interfacesNode,XMLConstants.InterfaceTagName); 
	}
	
	/** Returns List of Page nodes */
	private List getPageNodes() {
		return Utils.getChildNodesByName(
				pagesNode,XMLConstants.PageTagName); 
	}
	
	
	/** Parses file and returns DOM XML Document */
	private Document getDOMTree() {
		try{
			DOMParser parser = new DOMParser();
				parser.parse(fileURI);
				return parser.getDocument();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;	
	}

	/** Parses a section of the menu-uri and returns its menu name part.
	 * Returns null if an error occurs */
  private String getMenuName(String section) {
		if (section==null) return null;
		int ind = section.indexOf(":");
		if (ind == 0 || ind==section.length()-1) return null;
	  if (ind < 0) return section;
	  if (ind > 0) return section.substring(0,ind); 	
		return null;
	}

	/** Parses a section of the menu-uri and returns its item name part.
	 * Returns null if an error occurs or not found */
  private String getItemName(String section) {
		if (section==null) return null;
		int ind = section.indexOf(":");
		if (ind <= 0 || ind==section.length()-1) return null;
	  if (ind > 0) return section.substring(ind+1); 	
		return null;
	}

	/** Takes host Menu element and searches for its descender
	 * specified by menu-uri section (Menu or Item) */  
	private InterfaceTreeElement getTreeElement(Menu host,String uriSection) {
		String menuName = getMenuName(uriSection);
		String itemName = getItemName(uriSection);
		if (menuName==null) return null;
		Item[] items = host.getChildItems();
		for (int i=0;i<items.length; i++) {
			Menu menu = items[i].getChildMenu();
			if (menu!=null && menu.getName().equals(menuName)) {
				if (itemName==null) 
					return menu;
				else
					return menu.getChildItem(itemName);
			}
		}
		return null;
	}

    /** Namespaces feature id (http://xml.org/sax/features/namespaces). */	
    protected static final String NAMESPACES_FEATURE_ID = "http://xml.org/sax/features/namespaces";

    /** Validation feature id (http://xml.org/sax/features/validation). */
    protected static final String VALIDATION_FEATURE_ID = "http://xml.org/sax/features/validation";

    /** Schema validation feature id (http://apache.org/xml/features/validation/schema). */
    protected static final String SCHEMA_VALIDATION_FEATURE_ID = "http://apache.org/xml/features/validation/schema";

    /** Schema full checking feature id (http://apache.org/xml/features/validation/schema-full-checking). */
    protected static final String SCHEMA_FULL_CHECKING_FEATURE_ID = "http://apache.org/xml/features/validation/schema-full-checking";

}
