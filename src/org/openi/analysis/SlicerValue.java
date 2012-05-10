package org.openi.analysis;

/**
 * 
 * @author SUJEN
 *
 */
public class SlicerValue {
	
	private String dimension;
	private String hierarchy;
	private String level;
	private String member;
	private String uniqueName;

	public SlicerValue(String dimension, String hierarchy, String level,
			String member, String uniqueName) {
		this.dimension = dimension;
		this.hierarchy = hierarchy;
		this.level = level;
		this.member = member;
		this.uniqueName = uniqueName;
	}
	
	public String getDimension() {
		return dimension;
	}

	public void setDimension(String dimension) {
		this.dimension = dimension;
	}

	public String getHierarchy() {
		return hierarchy;
	}

	public void setHierarchy(String hierarchy) {
		this.hierarchy = hierarchy;
	}

	public String getLevel() {
		return level;
	}

	public void setLevel(String level) {
		this.level = level;
	}

	public String getMember() {
		return member;
	}

	public void setMember(String member) {
		this.member = member;
	}

	public String getUniqueName() {
		return uniqueName;
	}

	public void setUniqueName(String uniqueName) {
		this.uniqueName = uniqueName;
	}
	
}
