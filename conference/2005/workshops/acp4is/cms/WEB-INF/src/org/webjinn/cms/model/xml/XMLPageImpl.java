package org.webjinn.cms.model.xml;
import org.w3c.dom.Node;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.w3c.dom.Element;
import org.webjinn.cms.model.InterfaceTreeElement;
import org.webjinn.cms.model.Page;
import org.webjinn.cms.model.CMSTree;
import org.webjinn.cms.model.Attribute;
import org.webjinn.cms.model.CSSFile;
import java.util.List;
import org.webjinn.cms.model.weaver.Constants;
import org.webjinn.cms.model.weaver.TextProcessingUtils;
import java.io.File;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.FileInputStream;
import java.io.BufferedWriter;
import java.io.OutputStreamWriter;
import java.io.FileOutputStream;

public class XMLPageImpl implements Page {

	/** Actual XML element that stores this Page */ 
	private Element el;
	/** Host CMS tree */
	private XMLCMSTree tree;

	  //The following fields are auxiliary	
	/** Attributes child */
	private Node attributesNode;
	/** CSSFiles child */
	private Node cssfilesNode;

	public XMLPageImpl(Element el,XMLCMSTree tree) {
		this.el=el;
		this.tree=tree;
		this.attributesNode = Utils.getChildNodeByName(el,XMLConstants.AttributesTagName);
		this.cssfilesNode = Utils.getChildNodeByName(el,XMLConstants.CSSFilesTagName);		
	}

	/** Returns file path URI to the page */
	public String getFileURI() {
		return el.getAttribute("file-uri");
	}

	/*
	private String getAbsoluteFilePath() {
		String rootDir = tree.getRootDir(); //must be absolute file name!
		//just in case
		rootDir = new File(rootDir).getAbsolutePath(); //To be removed?
		//
		if (rootDir.endsWith(File.separator)) 
			rootDir=rootDir.substring(0,rootDir.length()-1);		
		String fileURI = getFileURI();
		fileURI=fileURI.replace('/',File.separatorChar);
		return rootDir+File.separator+fileURI;		
	} */

	/** Retrieves content of the page file !!! FIXME !!! */
	/*public String getPageContent() {
		try{
			StringBuffer result=new StringBuffer();
			String fileURI = getAbsoluteFilePath();
			File pageFile = new File(fileURI);
			BufferedReader reader=new BufferedReader(new InputStreamReader(new FileInputStream(pageFile),"Cp1251"));
			try{
				String line;
				boolean isContent=true;
				while ((line=reader.readLine())!=null) {
					if (line.indexOf(Constants.generatedStart)>=0) {
						isContent=false;
						continue;
					}
					if (line.indexOf(Constants.generatedEnd)>=0) {
						isContent=true;
						continue;
					}
					if (isContent || 
							line.indexOf(Constants.headerLocation)>=0 || 
							line.indexOf(Constants.footerLocation)>=0) 
						result=result.append(line).append("\n");
				}
			} finally {reader.close();}
			int length = result.length();
			if (length>0) 
				return result.substring(0,length-1);
			else
			  return result.toString();
		} catch (Exception e) {return "";}		
	} */

	/** Sets new content into the page file !!! FIXME !!! */
/*	public void setPageContent(String content) {
			try{
			if (content==null) content="";
			String fileURI = getAbsoluteFilePath();
			File container = new File(fileURI);
			if (!container.exists()) {
				container.getParentFile().mkdirs();
				container.createNewFile();
			}
			BufferedWriter writer=new BufferedWriter(new OutputStreamWriter(new FileOutputStream(container),"Cp1251"));
			try{
				writer.write(content);
			} finally {writer.close();}
		} catch (Exception e) {
			e.printStackTrace();
		}
	} */

	/** Wraps page file with associated element data !!! FIXME? !!! */
	/*public void wrap() {
          setPageContent(TextProcessingUtils.wrap(this));   
	}*/

	/** Returns menu/item element associated with the page */
	public InterfaceTreeElement getAssociatedElement() {
		return tree.resolveInterfaceURI(el.getAttribute("interface-uri"));
	}

	/** Returns host CMS Tree */
	public CMSTree getCMSTree() {
		return tree;
	}

	/** Retrieves a list of attributes stored in the container */
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

	/** Returns wrapped XML element */
	protected Element getXMLElement() {
		return el;
	}
	
	/** Creates new Page XML Element (without adding it into the tree) */
	protected static Element createPage(Document doc, String fileURI, String menuURI) {
		Element page = Utils.createElementNode(doc,XMLConstants.PageTagName);
		page.setAttribute("file-uri", fileURI);
    page.setAttribute("interface-uri", menuURI);
		Element attributes = Utils.createElementNode(doc,XMLConstants.AttributesTagName);
		Element cssfiles = Utils.createElementNode(doc,XMLConstants.CSSFilesTagName);
		page.appendChild(attributes);
		page.appendChild(cssfiles);
		return page;
	}
}

