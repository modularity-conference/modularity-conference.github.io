package org.webjinn.cms.model.weaver;

/** Represents attribute tag (Item, Menu or Page attribute)
 *  located in the header or footer strings */
public class AttributeTag {
  /** The name of the tag */
	private String tagName;
	
	/** HTML type attribute */
	private String htmlType;

	/** Name attribute */
	private String name;
	
	/** Description attribute */
	private String descr;
	
	public AttributeTag(String tagName) {
		this.tagName=tagName;
	}	

	public void setName(String name) {
		this.name=name;
	}

	public void setDescription(String descr) {
		this.descr=descr;
	}

	public void setHTMLType(String htmlType) {
		this.htmlType=htmlType;
	}

	public String getName() {
		return name;
	}

	public String getDescription() {
		return descr;
	}
	
	public String getTagName() {
		return tagName;
	}
	
	public String getHTMLType() {
		return htmlType;
	}
}
