package com.dbms.entity.cqt.dtos;

import java.io.Serializable;
import java.util.Date;

public class HistoricalViewDbDataDTO implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 6379905233332491974L;
	private Long cmqCode;
	private String listName;
	private String listType;
	private String product;
	private String drugProgram;
	private String protocolNumber;
	private String listLevel;
	private String parentListName;
	private String status;
	private String state;
	private Date creationDate;
	private String createdBy;
	private String algorithm;
	private Date lastActivationDate;
	private String lastActivationBy;
	private String description;
	private String term;
	private String termDictLevel;
	private Long termCode;
	private String termScope;
	private String dictionaryVersion;
	private String designee;
	private String designee2;
	private String designee3;
	private String medicalConcept;
	private String termCategory;
        private String termCategory2;

	public Long getCmqCode() {
		return cmqCode;
	}

	public void setCmqCode(Long cmqCode) {
		this.cmqCode = cmqCode;
	}

	public String getListName() {
		return listName;
	}

	public void setListName(String listName) {
		this.listName = listName;
	}
	
	public String getTermCategory() {
		return termCategory;
	}

	public void setTermCategory(String termCategory) {
		this.termCategory = termCategory;
	}

        public String getTermCategory2() {
            return termCategory2;
        }

        public void setTermCategory2(String termCategory2) {
            this.termCategory2 = termCategory2;
        }

	public String getListType() {
		return listType;
	}

	public void setListType(String listType) {
		this.listType = listType;
	}

	public String getProduct() {
		return product;
	}

	public void setProduct(String product) {
		this.product = product;
	}

	public String getDrugProgram() {
		return drugProgram;
	}

	public void setDrugProgram(String drugProgram) {
		this.drugProgram = drugProgram;
	}

	public String getProtocolNumber() {
		return protocolNumber;
	}

	public void setProtocolNumber(String protocolNumber) {
		this.protocolNumber = protocolNumber;
	}

	public String getListLevel() {
		return listLevel;
	}

	public void setListLevel(String listLevel) {
		this.listLevel = listLevel;
	}

	public String getParentListName() {
		return parentListName;
	}

	public void setParentListName(String parentListName) {
		this.parentListName = parentListName;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getState() {
		return state;
	}

	public void setState(String state) {
		this.state = state;
	}

	public Date getCreationDate() {
		return creationDate;
	}

	public void setCreationDate(Date creationDate) {
		this.creationDate = creationDate;
	}

	public String getCreatedBy() {
		return createdBy;
	}

	public void setCreatedBy(String createdBy) {
		this.createdBy = createdBy;
	}

	public Date getLastActivationDate() {
		return lastActivationDate;
	}

	public void setLastActivationDate(Date lastActivationDate) {
		this.lastActivationDate = lastActivationDate;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getTerm() {
		return term;
	}

	public void setTerm(String term) {
		this.term = term;
	}

	public String getTermDictLevel() {
		return termDictLevel;
	}

	public void setTermDictLevel(String termDictLevel) {
		this.termDictLevel = termDictLevel;
	}

	public Long getTermCode() {
		return termCode;
	}

	public void setTermCode(Long termCode) {
		this.termCode = termCode;
	}

	public String getTermScope() {
		return termScope;
	}

	public void setTermScope(String termScope) {
		this.termScope = termScope;
	}

	public String getDictionaryVersion() {
		return dictionaryVersion;
	}

	public void setDictionaryVersion(String dictionaryVersion) {
		this.dictionaryVersion = dictionaryVersion;
	}

	public String getDesignee() {
		return designee;
	}

	public void setDesignee(String designee) {
		this.designee = designee;
	}

	public String getDesignee2() {
		return designee2;
	}

	public void setDesignee2(String designee2) {
		this.designee2 = designee2;
	}

	public String getDesignee3() {
		return designee3;
	}

	public void setDesignee3(String designee3) {
		this.designee3 = designee3;
	}

	public String getMedicalConcept() {
		return medicalConcept;
	}

	public void setMedicalConcept(String medicalConcept) {
		this.medicalConcept = medicalConcept;
	}

	public String getAlgorithm() {
		return algorithm;
	}

	public void setAlgorithm(String algorithm) {
		this.algorithm = algorithm;
	}

	public String getLastActivationBy() {
		return lastActivationBy;
	}

	public void setLastActivationBy(String lastActivationBy) {
		this.lastActivationBy = lastActivationBy;
	}

}
