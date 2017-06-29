package com.dbms.entity.cqt.dtos;

import javax.persistence.Column;

import com.dbms.entity.IEntity;

public class SMQReverseHierarchySearchDto implements IEntity {

	private static final long serialVersionUID = -490379655871719191L;

	private Long smqId;
	private Long smqCode;
	private String smqName;
	private String smqLevel;
	private String smqDescription;
	private String smqSource;
	private String smqNote;
	private String smqStatus;
	private String smqAlgorithm;
	private String dictionaryVersion;
	private String impactType;
	private Integer ptTermScope;
	private Integer ptTermWeight;
	private String ptTermCategory;
	private String ptTermStatus;

	public Long getSmqId() {
		return smqId;
	}

	public void setSmqId(Long smqId) {
		this.smqId = smqId;
	}

	public Long getSmqCode() {
		return smqCode;
	}

	public void setSmqCode(Long smqCode) {
		this.smqCode = smqCode;
	}

	public String getSmqName() {
		return smqName;
	}

	public void setSmqName(String smqName) {
		this.smqName = smqName;
	}

	public String getSmqLevel() {
		return smqLevel;
	}

	public void setSmqLevel(String smqLevel) {
		this.smqLevel = smqLevel;
	}

	public String getSmqDescription() {
		return smqDescription;
	}

	public void setSmqDescription(String smqDescription) {
		this.smqDescription = smqDescription;
	}

	public String getSmqSource() {
		return smqSource;
	}

	public void setSmqSource(String smqSource) {
		this.smqSource = smqSource;
	}

	public String getSmqNote() {
		return smqNote;
	}

	public void setSmqNote(String smqNote) {
		this.smqNote = smqNote;
	}

	public String getSmqStatus() {
		return smqStatus;
	}

	public void setSmqStatus(String smqStatus) {
		this.smqStatus = smqStatus;
	}

	public String getSmqAlgorithm() {
		return smqAlgorithm;
	}

	public void setSmqAlgorithm(String smqAlgorithm) {
		this.smqAlgorithm = smqAlgorithm;
	}

	public String getDictionaryVersion() {
		return dictionaryVersion;
	}

	public void setDictionaryVersion(String dictionaryVersion) {
		this.dictionaryVersion = dictionaryVersion;
	}

	public String getImpactType() {
		return impactType;
	}

	public void setImpactType(String impactType) {
		this.impactType = impactType;
	}

	@Override
	public Long getId() {
		return this.smqId;
	}

	public Integer getPtTermScope() {
		return ptTermScope;
	}

	public void setPtTermScope(Integer ptTermScope) {
		this.ptTermScope = ptTermScope;
	}

	public Integer getPtTermWeight() {
		return ptTermWeight;
	}

	public void setPtTermWeight(Integer ptTermWeight) {
		this.ptTermWeight = ptTermWeight;
	}

	public String getPtTermCategory() {
		return ptTermCategory;
	}

	public void setPtTermCategory(String ptTermCategory) {
		this.ptTermCategory = ptTermCategory;
	}

	public String getPtTermStatus() {
		return ptTermStatus;
	}

	public void setPtTermStatus(String ptTermStatus) {
		this.ptTermStatus = ptTermStatus;
	}
}
