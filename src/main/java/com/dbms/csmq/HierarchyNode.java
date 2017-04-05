package com.dbms.csmq;

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;

import org.apache.commons.beanutils.BeanUtils;

import com.dbms.entity.IEntity;
import com.dbms.entity.cqt.CmqBase190;
import com.dbms.entity.cqt.CmqBaseTarget;

public class HierarchyNode implements Serializable, Comparable<HierarchyNode> {

	private static final long serialVersionUID = 3824369219641775903L;

	private String level;

	private String term;

	private String code;

	private IEntity entity;

	private boolean isDataFetchCompleted;

	private boolean isDummyNode;

	private boolean hideCategory;

	private String category;

	private boolean hideWeight;

	private String weight;

	private boolean hideScope;

	private String scope;

	private boolean hideDelete;

	private boolean primaryPathFlag;

	// for green colored primary rows
	private String rowStyleClass;

	public HierarchyNode() {
	}

	public HierarchyNode(String level, String term, String code, IEntity entity) {
		this.level = level;
		this.term = term;
		this.code = code;
		this.entity = entity;
	}

	public HierarchyNode(String level, String term, String code, String category, String weight, String scope,
			IEntity entity) {
		this.level = level;
		this.term = term;
		this.code = code;
		this.scope = scope;
		this.weight = weight;
		this.category = category;
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

	public HierarchyNode copy() throws IllegalAccessException, InvocationTargetException {
		HierarchyNode target = new HierarchyNode();
		BeanUtils.copyProperties(target, this);
		return target;
	}

	public void markNotEditableInRelationstable() {
		this.hideCategory = true;
		this.hideDelete = true;
		this.hideScope = true;
		this.hideWeight = true;
	}

	@Override
	public String toString() {
		return term;
	}

	public String getCategory() {
		return category;
	}

	public void setCategory(String category) {
		this.category = category;
	}

	public String getWeight() {
		return weight == null ? "" : weight;
	}

	public void setWeight(String weight) {
		this.weight = weight;
	}

	public String getScope() {
		return scope;
	}

	public void setScope(String scope) {
		this.scope = scope;
	}

	public boolean isPrimaryPathFlag() {
		return primaryPathFlag;
	}

	public void setPrimaryPathFlag(boolean primaryPathFlag) {
		this.primaryPathFlag = primaryPathFlag;
	}

	public boolean isHideCategory() {
		return hideCategory;
	}

	public void setHideCategory(boolean hideCategory) {
		this.hideCategory = hideCategory;
	}

	public boolean isHideWeight() {
		return hideWeight;
	}

	public void setHideWeight(boolean hideWeight) {
		this.hideWeight = hideWeight;
	}

	public boolean isHideScope() {
		return hideScope;
	}

	public void setHideScope(boolean hideScope) {
		this.hideScope = hideScope;
	}

	public boolean isHideDelete() {
		return hideDelete;
	}

	public void setHideDelete(boolean hideDelete) {
		this.hideDelete = hideDelete;
	}

	public String getRowStyleClass() {
		return rowStyleClass;
	}

	public void setRowStyleClass(String rowStyleClass) {
		this.rowStyleClass = rowStyleClass;
	}
	
	public boolean isAlgorithmN() {
		if(this.entity != null) {
			if(this.entity instanceof CmqBase190) {
				return "N".equalsIgnoreCase(((CmqBase190)this.entity).getCmqAlgorithm());
			} else if(this.entity instanceof CmqBaseTarget) {
				return "N".equalsIgnoreCase(((CmqBaseTarget)this.entity).getCmqAlgorithm());
			}
		}
		return true;
	}
	
	public boolean isInactiveList() {
		if(this.entity != null) {
			if(this.entity instanceof CmqBase190) {
				return CmqBase190.CMQ_STATUS_VALUE_INACTIVE.equals(((CmqBase190)this.entity).getCmqStatus());
			} else if(this.entity instanceof CmqBaseTarget) {
				return CmqBase190.CMQ_STATUS_VALUE_INACTIVE.equals(((CmqBaseTarget)this.entity).getCmqStatus());
			}
		}
		return false;
	}
}
