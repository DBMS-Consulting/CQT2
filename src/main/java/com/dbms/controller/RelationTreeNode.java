package com.dbms.controller;

import java.io.Serializable;

/**
 * @author Jay G.(jayshanchn@hotmail.com)
 * @date Feb 12, 2017 5:14:12 AM
 **/
public class RelationTreeNode implements Serializable,Comparable<RelationTreeNode>{
	
	private Long code;
	private String name;
	private Integer level;
	private String scope;
	private String category;
	private Integer weight;
	private Boolean root=Boolean.FALSE;
	public RelationTreeNode(Long code,String name,Integer level,String scope,String category,Integer weight,Boolean root){
		this.code=code;
		this.name=name;
		this.level=level;
		this.scope=scope;
		this.category=category;
		this.weight=weight;
		this.root=root;
	}
	
	public Integer getLevel() {
		return level;
	}
	public void setLevel(Integer level) {
		this.level = level;
	}
	public String getScope() {
		return scope;
	}
	public void setScope(String scope) {
		this.scope = scope;
	}
	public String getCategory() {
		return category;
	}
	public void setCategory(String category) {
		this.category = category;
	}
	public Integer getWeight() {
		return weight;
	}
	public void setWeight(Integer weight) {
		this.weight = weight;
	}
	

	
	public Long getCode() {
		return code;
	}

	public void setCode(Long code) {
		this.code = code;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Boolean getRoot() {
		return root;
	}
	public void setRoot(Boolean root) {
		this.root = root;
	}
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((code == null) ? 0 : code.hashCode());
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		return result;
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		RelationTreeNode other = (RelationTreeNode) obj;
		if (code == null) {
			if (other.code != null)
				return false;
		} 
		
		else if (!code.equals(other.code))
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} 
		
		else if (!name.equals(other.name))
			return false;
		return true;
	}
	@Override
	public int compareTo(RelationTreeNode node) {
		return this.code.compareTo(node.getCode())==0?this.name.compareTo(node.getName()):0;
	}

}

