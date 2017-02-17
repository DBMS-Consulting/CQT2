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
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import com.dbms.entity.BaseEntity;

/**
 * @author Jay G.(jayshanchn@hotmail.com)
 * @date Feb 11, 2017 6:31:22 AM
 **/
@Entity
@Table(name="CMQ_RELATION_190")
@NamedQueries({
		@NamedQuery(name = "CmqRelation190.rootRelations", query = "from CmqRelation190 c where size(c.children)>0"),
		@NamedQuery(name="CmqRelation190.findByTermName",query="from CmqRelation190 c where c.termName=:termName")})
public class CmqRelation190 extends BaseEntity{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	@Id
	@GeneratedValue(generator = "idGenerator")
	@SequenceGenerator(name="idGenerator",sequenceName="SEQ_CMQ_RELATION190")
	@Column(name="ID",nullable=false,updatable=false)
	private Long id;
	
	@Column(name = "TERM_NAME",length=200,nullable=false)
	private String termName;
	
	@Column(name="CMQ_LEVEL",length=8)
	private Integer cmqLevel;
	private Integer socCode;
	private Integer hlgtCode;
	private Integer hltCode;
	private Integer ptCode;
	private Integer lltCode;
	@Column(name="PT_TERM_DICT_LEVEL",length=3)
	private String termDictLevel;
	@Column(name="PT_TERM_WEIGHT",length=8)
	private Integer ptTermWeight;
	@Column(name="PT_TERM_ALGORITHM",length=300)
	private String ptTermAlgorithm;
	@Column(name="PT_TERM_CATEGORY",length=1)
	private String ptTermCategory;
	@Column(name="PT_TERM_SCOPE",length=15)
	private String ptTermScope;
	@Column(name="RELATION_TYPE",length=15)
	private String relationType;
	@Column(name="CREATED_BY",length=30)
	private String createdBy;
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name="CREATION_DATE")
	private Date creationDate;
	@Column(name="LAST_MODIFIED_BY",length=30)
	private String lastModifiedBy;
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name="LAST_MODIFIED_DATE")
	private Date lastModifiedDate;
	
	@ManyToOne
	@JoinColumn(name="BASE_CODE",nullable=false,updatable=false)
	private CmqBase190 base;
	
	@OneToMany(mappedBy="parent",cascade={CascadeType.ALL},fetch=FetchType.EAGER)
	private Set<CmqRelation190> children=new HashSet<>();
	
	@ManyToOne()
	@JoinColumn(name="PARENT_ID")
	private CmqRelation190 parent;
	
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getTermName() {
		return termName;
	}

	public void setTermName(String termName) {
		this.termName = termName;
	}

	public Integer getCmqLevel() {
		return cmqLevel;
	}

	public void setCmqLevel(Integer cmqLevel) {
		this.cmqLevel = cmqLevel;
	}

	public Integer getSocCode() {
		return socCode;
	}

	public void setSocCode(Integer socCode) {
		this.socCode = socCode;
	}

	public Integer getHlgtCode() {
		return hlgtCode;
	}

	public void setHlgtCode(Integer hlgtCode) {
		this.hlgtCode = hlgtCode;
	}

	public Integer getHltCode() {
		return hltCode;
	}

	public void setHltCode(Integer hltCode) {
		this.hltCode = hltCode;
	}

	public Integer getPtCode() {
		return ptCode;
	}

	public void setPtCode(Integer ptCode) {
		this.ptCode = ptCode;
	}

	public Integer getLltCode() {
		return lltCode;
	}

	public void setLltCode(Integer lltCode) {
		this.lltCode = lltCode;
	}

	public String getTermDictLevel() {
		return termDictLevel;
	}

	public void setTermDictLevel(String termDictLevel) {
		this.termDictLevel = termDictLevel;
	}

	public Integer getPtTermWeight() {
		return ptTermWeight;
	}

	public void setPtTermWeight(Integer ptTermWeight) {
		this.ptTermWeight = ptTermWeight;
	}

	public String getPtTermAlgorithm() {
		return ptTermAlgorithm;
	}

	public void setPtTermAlgorithm(String ptTermAlgorithm) {
		this.ptTermAlgorithm = ptTermAlgorithm;
	}

	public String getPtTermCategory() {
		return ptTermCategory;
	}

	public void setPtTermCategory(String ptTermCategory) {
		this.ptTermCategory = ptTermCategory;
	}

	public String getPtTermScope() {
		return ptTermScope;
	}

	public void setPtTermScope(String ptTermScope) {
		this.ptTermScope = ptTermScope;
	}

	public String getRelationType() {
		return relationType;
	}

	public void setRelationType(String relationType) {
		this.relationType = relationType;
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

	public CmqBase190 getBase() {
		return base;
	}

	public void setBase(CmqBase190 base) {
		this.base = base;
	}

	public Set<CmqRelation190> getChildren() {
		return children;
	}

	public void setChildren(Set<CmqRelation190> children) {
		this.children = children;
	}
	public void addChild(CmqRelation190 child){
		this.children.add(child);
		child.setParent(this);
	}

	public CmqRelation190 getParent() {
		return parent;
	}

	public void setParent(CmqRelation190 parent) {
		this.parent = parent;
	}

	
}

