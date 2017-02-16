package com.dbms.entity.cqt;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.hibernate.annotations.GenericGenerator;

import com.dbms.entity.BaseEntity;

/**
 * @author Jay G.(jayshanchn@hotmail.com)
 * @date Feb 11, 2017 6:26:15 AM
 **/
@Entity
@Table(name="CMQ_BASE_190")
public class CmqBase190 extends BaseEntity{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	@Id
	@GeneratedValue(generator = "idGenerator")    
	@GenericGenerator(name = "idGenerator", strategy = "assigned") 
	@Column(name = "CMQ_CODE",length=10,unique=true,nullable=false)
	private Long id;
	@Column(name="CMQ_NAME",length=200)
	private String name;
	@Column(name="CMQ_TYPE",length=3)
	private String type;
	@Column(name="CMQ_LEVEL",length=1)
	private Integer level;
	@Lob
	@Column(name="CMQ_DESCRIPTION",columnDefinition="CLOB")
	private String description;
	@Lob
	@Column(name="CMQ_SOURCE",columnDefinition="CLOB")
	private String source;
	@Lob
	@Column(name="CMQ_NOTE",columnDefinition="CLOB")
	private String note;
	@Column(name="CMQ_STATUS",length=1)
	private String status;
	@Column(name="CMQ_STATE",length=15)
	private String state;
	@Column(name="CMQ_CRITICAL_EVENT",length=10)
	private String criticalEvent;
	@Column(name="CMQ_ALGORITHM",length=300)
	private String algorithm;
	@Column(name="CMQ_SCOPE",length=5)
	private String scope;
	@Column(name="CMQ_PROGRAM_NAME",length=200)
	private String programName;
	@Column(name="CMQ_PROTOCOL_NAME",length=100)
	private String protocolName;
	@Column(name="CMQ_PRODUCT_NAME",length=200)
	private String productName;
	@Column(name="DESIGNEE",length=100)
	private String designee;
	@Column(name="CMQ_GROUP",length=100)
	private String group;
	@Column(name="CMQ_DUE_DATE")
	private Date dueDate;
	@Column(name="CMQ_WF_DESC",length=200)
	private String wfDesc;
	@Column(name="CREATED_BY",length=30)
	private String createdBy;
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name="CREATION_DATE")
	private Date creationDate;
	@Column(name="LAST_MODIFIED_BY",length=30)
	private String lastModifiedBy;
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name="LAST_MODIFIED_DATE")
	private Date lastModifedDate;
	@Column(name="ACTIVATED_BY",length=30)
	private String activatedBy;
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name="ACTIVATION_DATE")
	private Date activationDate;
	@Column(name="RELEASE_GROUP",length=30)
	private String releaseGroup;
	@Column(name="RELEASE_STATUS",length=30)
	private String releaseStatus;
	@OneToMany(mappedBy="parent",cascade=CascadeType.ALL)
	private Set<CmqBase190> children=new HashSet<>();
	@ManyToOne
	@JoinColumn(name="CMQ_PARENT_CODE")
	private CmqBase190 parent;
	
	@OneToMany(mappedBy="base",cascade=CascadeType.ALL,orphanRemoval=true,fetch=FetchType.EAGER)
	private Set<CmqRelation190> relations=new HashSet<>();
	
	@Column(name="DRUG_PROGRAM",length=30)
	private String drugProgram;
	@Override
	public Long getId() {
		return this.id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public Integer getLevel() {
		return level;
	}
	public void setLevel(Integer level) {
		this.level = level;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public String getSource() {
		return source;
	}
	public void setSource(String source) {
		this.source = source;
	}
	public String getNote() {
		return note;
	}
	public void setNote(String note) {
		this.note = note;
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
	public String getAlgorithm() {
		return algorithm;
	}
	public void setAlgorithm(String algorithm) {
		this.algorithm = algorithm;
	}
	public String getScope() {
		return scope;
	}
	public void setScope(String scope) {
		this.scope = scope;
	}
	public String getProgramName() {
		return programName;
	}
	public void setProgramName(String programName) {
		this.programName = programName;
	}
	public String getProtocolName() {
		return protocolName;
	}
	public void setProtocolName(String protocolName) {
		this.protocolName = protocolName;
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
	public Date getDueDate() {
		return dueDate;
	}
	public void setDueDate(Date dueDate) {
		this.dueDate = dueDate;
	}
	public String getWfDesc() {
		return wfDesc;
	}
	public void setWfDesc(String wfDesc) {
		this.wfDesc = wfDesc;
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
	public Date getLastModifedDate() {
		return lastModifedDate;
	}
	public void setLastModifedDate(Date lastModifedDate) {
		this.lastModifedDate = lastModifedDate;
	}
	public String getActivatedBy() {
		return activatedBy;
	}
	public void setActivatedBy(String activatedBy) {
		this.activatedBy = activatedBy;
	}
	public Date getActivationDate() {
		return activationDate;
	}
	public void setActivationDate(Date activationDate) {
		this.activationDate = activationDate;
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
	public Set<CmqBase190> getChildren() {
		return children;
	}
	public void setChildren(Set<CmqBase190> children) {
		this.children = children;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public CmqBase190 getParent() {
		return parent;
	}
	public void setParent(CmqBase190 parent) {
		this.parent = parent;
	}
	public String getProductName() {
		return productName;
	}
	public void setProductName(String productName) {
		this.productName = productName;
	}
	public String getDrugProgram() {
		return drugProgram;
	}
	public void setDrugProgram(String drugProgram) {
		this.drugProgram = drugProgram;
	}
	public Set<CmqRelation190> getRelations() {
		return relations;
	}
	public void setRelations(Set<CmqRelation190> relations) {
		this.relations = relations;
	}
	public void addRelation(CmqRelation190 relation){
		this.relations.add(relation);
		relation.setBase(this);
	}
}

