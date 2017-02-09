package com.dbms.entity.cqt;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

/**
 * @date Feb 6, 2017 9:37:10 AM
 **/
@Entity
@Table(name = "CQT_CREATE_ENTITY")
public class CreateEntity implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	@Id
	@Column(name = "CODE")
	protected Integer code;

	@Column(name = "EXTENSION", length = 20)
	private String extension;

	@Column(name = "NAME")
	private String name;

	@Column(name = "FILTER_DICTIONARY")
	private String filterDictionary = "NMATSMQ";

	@Column(name = "RELEASE_GROUP")
	private String releaseGroup = "Draft AG";

	@Column(name = "RELEASE_STATUS")
	private String releaseStatus = "PENDING";

	@Column(name="PRODUCT")
	private String product;

	@Column(name="DESIGNEE")
	private String designee;

	@Column(name = "ALGORITHM")
	private String algorithm;

	@Column(name = "STATUS")
	private String status = "Pending";

	@Column(name = "CE_STATE")
	private String state = "Draft";

	@Column(name = "CRITICAL_EVENT")
	private String criticalEvent;

	@Column(name = "LEVEL1", length = 20)
	private String level = "1";

	@Column(name = "SCOPE")
	private String scope;

	@Column(name="GROUP1")
	private String group;

	@Column(name = "DRUG_PROGRAM")
	private String drugProgram;

	@Column(name = "PROTOCOL")
	private String protocol;

	@Column(name = "DESCRIPTION")
	private String description;
	
	@Column(name="BRAND")
	private String brand;
	
	@Column(name="TERM")
	private String term;
	
	@Column(name="TERM_LEVEL")
	private String termLevel;
	@Column(name="PARENT_NAME")
	private String parentName;
	
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "CREATED", nullable = false, updatable = false)
	private Date created;
	
	@ManyToOne
	@JoinColumn(name="CREATOR_ID")
	private User creator;

	@ManyToOne
	@JoinColumn(name="ACTIVATOR_ID")
	private User activator;
	
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "ACTIVATED")
	private Date activated;

	public CreateEntity() {
		super();
	}
	public boolean isTransient() {
		return code == null;
	}

	public Integer getCode() {
		return code;
	}

	public void setCode(Integer code) {
		this.code = code;
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

	public String getFilterDictionary() {
		return filterDictionary;
	}

	public void setFilterDictionary(String filterDictionary) {
		this.filterDictionary = filterDictionary;
	}

	public String getReleaseGroup() {
		return releaseGroup;
	}

	public void setReleaseGroup(String releaseGroup) {
		this.releaseGroup = releaseGroup;
	}

	public String getReleaseStatus() {
		return releaseStatus;
	}

	public void setReleaseStatus(String releaseStatus) {
		this.releaseStatus = releaseStatus;
	}

	public String getAlgorithm() {
		return algorithm;
	}

	public void setAlgorithm(String algorithm) {
		this.algorithm = algorithm;
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

	public String getCriticalEvent() {
		return criticalEvent;
	}

	public void setCriticalEvent(String criticalEvent) {
		this.criticalEvent = criticalEvent;
	}

	public String getLevel() {
		return level;
	}

	public void setLevel(String level) {
		this.level = level;
	}

	public String getScope() {
		return scope;
	}

	public void setScope(String scope) {
		this.scope = scope;
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

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getBrand() {
		return brand;
	}

	public void setBrand(String brand) {
		this.brand = brand;
	}

	public String getTerm() {
		return term;
	}

	public void setTerm(String term) {
		this.term = term;
	}

	public String getTermLevel() {
		return termLevel;
	}

	public void setTermLevel(String termLevel) {
		this.termLevel = termLevel;
	}

	public String getParentName() {
		return parentName;
	}

	public void setParentName(String parentName) {
		this.parentName = parentName;
	}

	public String getProduct() {
		return product;
	}

	public void setProduct(String product) {
		this.product = product;
	}

	public String getDesignee() {
		return designee;
	}

	public void setDesignee(String designee) {
		this.designee = designee;
	}

	public String getGroup() {
		return group;
	}

	public void setGroup(String group) {
		this.group = group;
	}

	public Date getCreated() {
		return created;
	}
	public void setCreated(Date created) {
		this.created = created;
	}
	public User getCreator() {
		return creator;
	}
	public void setCreator(User creator) {
		this.creator = creator;
	}
	public User getActivator() {
		return activator;
	}
	public void setActivator(User activator) {
		this.activator = activator;
	}
	public Date getActivated() {
		return activated;
	}
	public void setActivated(Date activated) {
		this.activated = activated;
	}
	@Override
	public String toString() {
		return "CreateEntity [ code="+code+", extension=" + extension + ", name=" + name + ", filterDictionary=" + filterDictionary + ", releaseGroup=" + releaseGroup + ", releaseStatus="
				+ releaseStatus + ", product=" + product + ", designee=" + designee + ", algorithm=" + algorithm + ", status=" + status + ", state=" + state + ", criticalEvent="
				+ criticalEvent + ", level=" + level + ", scope=" + scope + ", group=" + group + ", drugProgram=" + drugProgram + ", protocol=" + protocol + ", description="
				+ description +  ", brand=" + brand + ", term=" + term + ", termLevel=" + termLevel + ", parentName=" + parentName + "]";
	}


}
