package com.dbms.entity.cqt.dtos;

import com.dbms.entity.IEntity;

public class SMQReverseHierarchySearchDto implements IEntity {

	private static final long serialVersionUID = -490379655871719191L;

	private Long 	smqId;

	private String 	smqParentCode;
	private String 	smqParentName;
	private String 	smqParentLevel;
	private String 	smqChildCode;
	private String 	smqChildName;
	private String 	smqChildLevel;

	 
	public void setId(Long smqId) {
		this.smqId = smqId;
	}
	public String getSmqParentCode() {
		return smqParentCode;
	}
	public void setSmqParentCode(String smqParentCode) {
		this.smqParentCode = smqParentCode;
	}
	public String getSmqChildCode() {
		return smqChildCode;
	}
	public void setSmqChildCode(String smqChildCode) {
		this.smqChildCode = smqChildCode;
	}
	public String getSmqChildName() {
		return smqChildName;
	}
	public void setSmqChildName(String smqChildName) {
		this.smqChildName = smqChildName;
	}
	
	@Override
	public Long getId() {
		return smqId;
	}
	public String getSmqParentName() {
		return smqParentName;
	}
	public void setSmqParentName(String smqParentName) {
		this.smqParentName = smqParentName;
	}
	public String getSmqChildLevel() {
		return smqChildLevel;
	}
	public void setSmqChildLevel(String smqChildLevel) {
		this.smqChildLevel = smqChildLevel;
	}
	public String getSmqParentLevel() {
		return smqParentLevel;
	}
	public void setSmqParentLevel(String smqParentLevel) {
		this.smqParentLevel = smqParentLevel;
	}

	
}
