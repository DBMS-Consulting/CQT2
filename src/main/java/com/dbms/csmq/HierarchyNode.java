package com.dbms.csmq;

import java.io.Serializable;

import com.dbms.entity.IEntity;

public class HierarchyNode implements Serializable, Comparable<HierarchyNode> {

	private static final long serialVersionUID = 3824369219641775903L;

	private String level;

	private String term;

	private String code;

	private IEntity entity;

	private boolean isDataFetchCompleted;
	
	private boolean isDummyNode;

	public HierarchyNode() {
	}

	public HierarchyNode(String level, String term, String code, IEntity entity) {
		this.level = level;
		this.term = term;
		this.code = code;
		this.entity = entity;
	}

	public String getLevel() {
		return level;
	}

	public void setLevel(String level) {
		this.level = level;
	}

	public String getTerm() {
		return term;
	}

	public void setTerm(String term) {
		this.term = term;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public int compareTo(HierarchyNode document) {
		int retVal = this.getLevel().compareTo(document.getLevel());
		if (retVal == 0) {
			retVal = this.getTerm().compareTo(document.getTerm());
		}
		return retVal;
	}

	public IEntity getEntity() {
		return entity;
	}

	public void setEntity(IEntity entity) {
		this.entity = entity;
	}

	public boolean isDataFetchCompleted() {
		return isDataFetchCompleted;
	}

	public void setDataFetchCompleted(boolean isDataFetchCompleted) {
		this.isDataFetchCompleted = isDataFetchCompleted;
	}

	public boolean isDummyNode() {
		return isDummyNode;
	}

	public void setDummyNode(boolean isDummyNode) {
		this.isDummyNode = isDummyNode;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((code == null) ? 0 : code.hashCode());
		result = prime * result + ((level == null) ? 0 : level.hashCode());
		result = prime * result + ((term == null) ? 0 : term.hashCode());
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
		HierarchyNode other = (HierarchyNode) obj;
		if (code == null) {
			if (other.code != null)
				return false;
		} else if (!code.equals(other.code))
			return false;
		if (level == null) {
			if (other.level != null)
				return false;
		} else if (!level.equals(other.level))
			return false;
		if (term == null) {
			if (other.term != null)
				return false;
		} else if (!term.equals(other.term))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return term;
	}
}
