package com.dbms.web.dto;

import java.util.Arrays;
import java.util.Date;

import com.dbms.view.ListDetailsFormVM;
import com.dbms.view.ListNotesFormVM;

public class DetailDTO {

	private String extension;
	private String name;
	private String drugProgram;
	private String protocol;
	private String designee;
	private String designeeTwo;
	private String designeeThree;
	private Integer level;
	private String critical;
	private String scope;
	private String[] products;
	private String group;
	private String algorithm;
	private String state;
	private String status;
	private String history;
	private Long code;
	private String createdBy;
	private Date creationDate;
	private String lastModifiedBy;
	private Date lastModifiedDate;
	private String description;
	private String notes;
	private String source;
	
	public boolean detailChange(ListDetailsFormVM detail) {
		if (extension != detail.getExtension() 
				|| name != detail.getName()
				|| drugProgram != detail.getDrugProgram()
				|| protocol != detail.getProtocol()
				|| designee != detail.getDesignee()
				|| designeeTwo != detail.getDesigneeTwo()
				|| designeeThree != detail.getDesigneeThree()
				|| level != detail.getLevel()
				|| (!Arrays.equals(this.products, detail.getProducts())))
 			return true;
		
		return false;
	}
	
	public boolean notesChange(ListNotesFormVM note) {
		if (description != note.getDescription()
				|| notes != note.getNotes()
				|| source != note.getSource())
 			return true;
		
		return false;
	}
	
	public void copyDatas(ListDetailsFormVM detail) {
		setExtension(detail.getExtension());
		setName(detail.getName());
		setDrugProgram(detail.getDrugProgram());
		setProtocol(detail.getProtocol());
		setDesignee(detail.getDesignee());
		setDesigneeTwo(detail.getDesigneeTwo());
		setDesigneeThree(detail.getDesigneeThree());
		setLevel(detail.getLevel());
 		setProducts(detail.getProducts());
		setGroup(detail.getGroup());
		setAlgorithm(detail.getAlgorithm());
		setState(detail.getState());
		setStatus(detail.getStatus());
 		setCode(detail.getCode());
	}
	
	public void copyNotes(ListNotesFormVM notes) {
		setDescription(notes.getDescription());
		setNotes(notes.getNotes());
		setSource(notes.getSource()); 
	}
	
	public String getExtension() {
		return extension;
	}
	public void setExtension(String extension) {
		this.extension = extension;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getDrugProgram() {
		return drugProgram;
	}
	public void setDrugProgram(String drugProgram) {
		this.drugProgram = drugProgram;
	}
	public String getProtocol() {
		return protocol;
	}
	public void setProtocol(String protocol) {
		this.protocol = protocol;
	}
	public String getDesignee() {
		return designee;
	}
	public void setDesignee(String designee) {
		this.designee = designee;
	}
	public String getDesigneeTwo() {
		return designeeTwo;
	}
	public void setDesigneeTwo(String designeeTwo) {
		this.designeeTwo = designeeTwo;
	}
	public String getDesigneeThree() {
		return designeeThree;
	}
	public void setDesigneeThree(String designeeThree) {
		this.designeeThree = designeeThree;
	}
	public Integer getLevel() {
		return level;
	}
	public void setLevel(Integer level) {
		this.level = level;
	}
	public String getCritical() {
		return critical;
	}
	public void setCritical(String critical) {
		this.critical = critical;
	}
	public String getScope() {
		return scope;
	}
	public void setScope(String scope) {
		this.scope = scope;
	}
	public String[] getProducts() {
		return products;
	}
	public void setProducts(String[] products) {
		this.products = products;
	}
	public String getGroup() {
		return group;
	}
	public void setGroup(String group) {
		this.group = group;
	}
	public String getAlgorithm() {
		return algorithm;
	}
	public void setAlgorithm(String algorithm) {
		this.algorithm = algorithm;
	}
	public String getState() {
		return state;
	}
	public void setState(String state) {
		this.state = state;
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	public String getHistory() {
		return history;
	}
	public void setHistory(String history) {
		this.history = history;
	}
	public Long getCode() {
		return code;
	}
	public void setCode(Long code) {
		this.code = code;
	}
	public String getCreatedBy() {
		return createdBy;
	}
	public void setCreatedBy(String createdBy) {
		this.createdBy = createdBy;
	}
	public Date getCreationDate() {
		return creationDate;
	}
	public void setCreationDate(Date creationDate) {
		this.creationDate = creationDate;
	}
	public String getLastModifiedBy() {
		return lastModifiedBy;
	}
	public void setLastModifiedBy(String lastModifiedBy) {
		this.lastModifiedBy = lastModifiedBy;
	}
	public Date getLastModifiedDate() {
		return lastModifiedDate;
	}
	public void setLastModifiedDate(Date lastModifiedDate) {
		this.lastModifiedDate = lastModifiedDate;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getNotes() {
		return notes;
	}

	public void setNotes(String notes) {
		this.notes = notes;
	}

	public String getSource() {
		return source;
	}

	public void setSource(String source) {
		this.source = source;
	}
	
	
}
