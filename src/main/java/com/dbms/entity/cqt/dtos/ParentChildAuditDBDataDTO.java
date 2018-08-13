package com.dbms.entity.cqt.dtos;

import java.io.Serializable;

public class ParentChildAuditDBDataDTO implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5063533502864514650L;
	
	String transactionType;
	Long cmqParentCodeOld;
	Long cmqParentCodeNew;
	Long cmqChildCodeOld;
	Long cmqChildCodeNew;
	Long parentCmqId;
	Long childCmqId;
	public String getTransactionType() {
		return transactionType;
	}
	public void setTransactionType(String transactionType) {
		this.transactionType = transactionType;
	}
	public Long getCmqParentCodeOld() {
		return cmqParentCodeOld;
	}
	public void setCmqParentCodeOld(Long cmqParentCodeOld) {
		this.cmqParentCodeOld = cmqParentCodeOld;
	}
	public Long getCmqParentCodeNew() {
		return cmqParentCodeNew;
	}
	public void setCmqParentCodeNew(Long cmqParentCodeNew) {
		this.cmqParentCodeNew = cmqParentCodeNew;
	}
	public Long getCmqChildCodeOld() {
		return cmqChildCodeOld;
	}
	public void setCmqChildCodeOld(Long cmqChildCodeOld) {
		this.cmqChildCodeOld = cmqChildCodeOld;
	}
	public Long getCmqChildCodeNew() {
		return cmqChildCodeNew;
	}
	public void setCmqChildCodeNew(Long cmqChildCodeNew) {
		this.cmqChildCodeNew = cmqChildCodeNew;
	}
	public Long getParentCmqId() {
		return parentCmqId;
	}
	public void setParentCmqId(Long parentCmqId) {
		this.parentCmqId = parentCmqId;
	}
	public Long getChildCmqId() {
		return childCmqId;
	}
	public void setChildCmqId(Long childCmqId) {
		this.childCmqId = childCmqId;
	}
	
}
