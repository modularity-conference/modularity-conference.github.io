package org.webjinn.cms.model.weaver;
import org.webjinn.utils.Parser;
import org.webjinn.utils.Parser.Element;
import org.webjinn.cms.model.*;
import java.util.Vector;
import java.util.HashSet;

/** Contains static methods for parsing and processing 
 *  header and footer elements of menus and pages */
public class TextProcessingUtils {


	/** Parses text argument and returns an array of all 
	 * attributes with specified (by attrTagName) tag name found
	 * in the text */ 
  public static AttributeTag[] getAttributes(String text,String attrTagName) {
		if (text==null || attrTagName==null) return new AttributeTag[0];
		
		Vector result = new Vector();
		String[] tags = {attrTagName};
		boolean[] hasBodies = {false};
	  Parser parser = new Parser(text,tags,hasBodies);
		parser.parse();
		Element[] elements = parser.getElements();
		
		HashSet tagNames = new HashSet();
		for (int i=0;i<elements.length;i++) { 
			if (attrTagName.equalsIgnoreCase(elements[i].getType())) {
				String name = elements[i].getAttribute("name");
				if (!tagNames.contains(name)) {
					AttributeTag attrTag = new AttributeTag(attrTagName);
					attrTag.setName(name);
					attrTag.setHTMLType(elements[i].getAttribute("htmltype"));
					attrTag.setDescription(elements[i].getAttribute("description"));
					result.add(attrTag);
					tagNames.add(name);
				}
			}
		}
		return (AttributeTag[])result.toArray(new AttributeTag[0]);
	}	


	/** Used for unwrapping menu header with a collection of items. 
	 * If selectedItemName is null, then no item is selected.
	 * Menu attributes and Item attributes are set in the unwrapped text */
	public static String buildMenuPart(String part,Page page,Menu menu,Item item) {
		try{
			StringBuffer result = new StringBuffer();
			String[] tags = {
				Constants.CSSFilesTag,
				Constants.MenuAttributeTag,
				Constants.PageAttributeTag,
				Constants.SelectedItemTag,
				Constants.NoSelectedItemTag,
				Constants.MenuItemsTag
			};

			boolean selected = item!=null;
			boolean[] hasBodies = {false,false,false,true,true,true};			
			Parser parser = new Parser(part,tags,hasBodies);
			parser.parse();
			Element[] els = parser.getElements();
			for (int i=0;i<els.length;i++)
				parseElement(els[i],page,menu,item,selected,result);
			return result.toString();
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public static String wrap(Page page) {
		String header = buildPagePart(page,true);
		String footer = buildPagePart(page,false);
		//String fileURI = FileUtils.getAbsolutePath(page);
		String content = FileUtils.getPageContent(page);
		int headerLocation = content.indexOf(Constants.headerLocation);
		StringBuffer result = new StringBuffer();
		
		if (headerLocation>=0) {
			result.append(content.substring(0,headerLocation)+"\n");
			result.append(Constants.generatedStart+"\n");
      result.append(Constants.headerLocation+"\n");
      result.append(header+"\n");
      result.append(Constants.generatedEnd+"\n");
			content = 
				content.substring(headerLocation+Constants.headerLocation.length());
		} else {
			result.append(Constants.generatedStart+"\n");
      result.append(header+"\n");
      result.append(Constants.generatedEnd+"\n");			
		}
		int footerLocation = content.indexOf(Constants.footerLocation);
		if (footerLocation>=0) {
			result.append(content.substring(0,footerLocation)+"\n");
			result.append(Constants.generatedStart+"\n");
      result.append(Constants.footerLocation+"\n");
      result.append(footer+"\n");
      result.append(Constants.generatedEnd+"\n");
			result.append(content.substring(footerLocation+Constants.footerLocation.length()));
		} else {
			result.append(content+"\n");
			result.append(Constants.generatedStart+"\n");
      result.append(footer+"\n");
      result.append(Constants.generatedEnd);			
		}	
		return result.toString();
	}

	public static String buildPagePart(Page page,boolean header) {
		CMSTree tree = page.getCMSTree();
		InterfaceTreeElement el = page.getAssociatedElement();
		Menu menu=null;
		Item item=null;
		if (el instanceof Menu) {
			menu = (Menu)el;
		}
		else {
			item = (Item)el;
			menu=item.getParentMenu();
		}
		String part = "";
		
		if (header) 
			part=buildHeader(page,menu,item);
		else 
			part=buildFooter(page,menu,item);
		return part;
	}

	public static CSSFile[] getCSSFiles(Page page) {
    Vector result = new Vector();
	  addCSSFiles(page,result);
	  InterfaceTreeElement el = page.getAssociatedElement();
		Menu menu=null;
		if (el instanceof Menu) {
			menu=(Menu)el;
		} else {
			menu=((Item)el).getParentMenu();
		}
		while(menu!=null) {
			addCSSFiles(menu,result);
			Item item = menu.getParentItem();
			if (item==null) menu=null; else menu=item.getParentMenu();
		}
		return (CSSFile[])result.toArray(new CSSFile[0]);
	}

	
	private static void addCSSFiles(CSSFilesContainer cont,Vector result) {
		CSSFile[] files = cont.getCSSFiles();
		if (files==null) return;
		for(int i=0;i<files.length;i++)
			result.add(files[i]);
	}


	private static String buildHeader(Page page, Menu menu, Item item) {
		Item parentItem = menu.getParentItem();
		if (parentItem==null) 
			return buildMenuPart(menu.getHeader(),page,menu,item);
		Menu parentMenu = parentItem.getParentMenu();
		String parentMenuText = buildHeader(page, parentMenu, parentItem);
		//!!!Experimental code - start!!!
		int childNum = getChildNum(parentMenu,parentItem);
		String[] tags = {Constants.SubMenuTag};
		boolean[] hasBodies = {false};
	  Parser parser = new Parser(parentMenuText,tags,hasBodies);
		StringBuffer parentMenuProcessed = new StringBuffer();
		parser.parse();
		Element[] els = parser.getElements();
		int subMenuTagNum = 0;
		boolean isProcessed=false;
		for(int i=0;i<els.length;i++) {
			if (els[i].getType().equalsIgnoreCase("text")) {
				parentMenuProcessed.append(els[i].getBody());
			}
			if (els[i].getType().equalsIgnoreCase(Constants.SubMenuTag)) {
				subMenuTagNum++;
				if (subMenuTagNum==childNum) {
					parentMenuProcessed.append(buildMenuPart(menu.getHeader(),page,menu,item));
					isProcessed=true;
				}
			}
		}
     if (isProcessed) return parentMenuProcessed.toString();
		//!!!Experimental code - end!!!
		return parentMenuText +
			     buildMenuPart(menu.getHeader(),page,menu,item);
	}	

	//!!!Experimental code - start!!!
	private static int getChildNum(Menu menu,Item childItem) {
		String childItemName = childItem.getName();
		int result=0;
		Item[] items = menu.getChildItems();
		for (int i=0;i<items.length;i++) {
			result++;
			if (items[i].getName().equals(childItemName))
				return result;
		}
		return -1;
	}
	//!!!Experimental code - end!!!
	
	private static String buildFooter(Page page, Menu menu, Item item) {
		Item parentItem = menu.getParentItem();
		if (parentItem==null) 
			return buildMenuPart(menu.getFooter(),page,menu,item);
		Menu parentMenu = parentItem.getParentMenu();
		return buildMenuPart(menu.getFooter(),page,menu,item) +
           buildFooter(page,parentMenu, parentItem);
	}	

	private static void parseElement(Element el,Page page,Menu menu,Item item, boolean selected, StringBuffer result) {

		if (el==null) return;
		String type = el.getType();

		if (type.equalsIgnoreCase("text")) {
			result.append(el.getBody());
			return;
		}

		if (type.equalsIgnoreCase(Constants.ItemAttributeTag)) {
			if (menu!=null)
				result.append(getAttributeValue(el,item));
			return;
		}

		if (type.equalsIgnoreCase(Constants.MenuAttributeTag)) {
			if (item!=null)
				result.append(getAttributeValue(el,menu));
			return;
		}

		if (type.equalsIgnoreCase(Constants.PageAttributeTag)) {
			if (page!=null)
				result.append(getAttributeValue(el,page));
			return;
		}

		if (type.equalsIgnoreCase(Constants.CSSFilesTag)) {
			if (page==null) return;
			 CSSFile[] files = getCSSFiles(page);
			 for(int i=0;i<files.length;i++) {
				 String link = "<LINK href='"+files[i].getURL()+"' rel=STYLESHEET type=text/css>\n";
				 result.append(link);	
			 }
			return;
		}


		if ((type.equalsIgnoreCase(Constants.InactiveItemTag) && !selected) ||
				(type.equalsIgnoreCase(Constants.SelectedItemTag) && selected) ||
				(type.equalsIgnoreCase(Constants.NoSelectedItemTag) && !selected)) 
		{
			String[] tags = {
				Constants.MenuAttributeTag,
				Constants.ItemAttributeTag,
				Constants.PageAttributeTag,
				Constants.CSSFilesTag
			};
			boolean[] hasBodies = {false,false,false,false};
			el.parseBody(tags,hasBodies);			
			Element[] els = el.getChildren();
			for (int i=0;i<els.length;i++) 
				parseElement(els[i],page,menu,item,selected,result);
		}

		if (type.equalsIgnoreCase(Constants.MenuItemsTag)) {
			if (menu==null) return;
			String[] tags = {
				Constants.MenuAttributeTag,
				Constants.PageAttributeTag,				
				Constants.ItemAttributeTag,
				Constants.SelectedItemTag,
				Constants.InactiveItemTag
			};
			boolean[] hasBodies = {false,false,false,true,true};			
			el.parseBody(tags,hasBodies);			
			Element[] els = el.getChildren();
			Item[] items = menu.getChildItems();
			for (int i=0;i<items.length;i++) {
				boolean isSelected = 
					(item!=null && items[i]!=null && 
					 item.getName().equals(items[i].getName()));
				for (int j=0;j<els.length;j++) {
					parseElement(els[j],page,menu,items[i],isSelected,result);
				}
			}
			return;
		}
	}	

		private static String getAttributeValue(Element attrEl, AttributeContainer container) {
			String name = attrEl.getAttribute("name");
			Attribute attr = container.getAttribute(name);
			if (attr==null) return ""; else return attr.getValue();
		}

    /*
			private static String setAttributes(String text,String attrTagName, AttributeContainer container) {
		StringBuffer result = new StringBuffer();
		String[] tags = {attrTagName};
		boolean[] hasBodies = {false};		
		Parser parser = new Parser(text,tags,hasBodies);
		parser.parse();
		Element[] els = parser.getElements();
		for (int i=0;i<els.length;i++) {
			Element el = els[i];
			if (el.getType().equalsIgnoreCase("text")) 
				result.append(el.getBody());
			if (el.getType().equalsIgnoreCase(attrTagName)) {
				result.append(getAttributeValue(el,container));	
			}
		}
		return result.toString();
	} */

		
	}
