package com.dbms.entity.cqt;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import com.dbms.entity.BaseEntity;

/**
 * @author Jay G.(jayshanchn@hotmail.com)
 * @date Feb 11, 2017 6:21:09 AM
 **/
@Entity
@Table(name="SMQ_RELATION_190")
public class SmqRelation190 extends BaseEntity{

	@Id
	@Column(name = "SMQ_RELATION_CODE",unique=true,nullable=false)
	@GeneratedValue(generator = "idGenerator")
	@SequenceGenerator(name = "idGenerator", sequenceName="SEQ_SMQ_RELATION190")
	private Long id;
	@Column(name="PT_CODE",length=10)
	private Integer ptCode;
	@Column(name="PT_NAME",length=300)
	private String ptName;
	@Column(name="RELATION_TYPE",length=15)
	private String relationType;
	@Column(name="SMQ_LEVEL",length=1)
	private Integer smqLevel;
	@Column(name="PT_TERM_SCOPE",length=1)
	private Integer ptTermScope;
	@Column(name="PT_TERM_WEIGHT",length=8)
	private Integer ptTermWeight;
	@Column(name="PT_TERM_CATEGORY",length=1)
	private String ptTermCategory;
	@Column(name="PT_TERM_STATUS",length=1)
	private String ptTermStatus;
	@Column(name="PT_TERM_ADDITION_VERSION",length=5)
	private String ptTermAdditionVersion;
	@Column(name="PT_TERM_RAST_MODIFIED_VERSION",length=5)
	private String ptTermLastModifiedVersion;
	
	@ManyToOne
	@JoinColumn(name="SMQ_CODE")
	private SmqBase190 base;
	
	public Integer getPtCode() {
		return ptCode;
	}
	public void setPtCode(Integer ptCode) {
		this.ptCode = ptCode;
	}
	public String getPtName() {
		return ptName;
	}
	public void setPtName(String ptName) {
		this.ptName = ptName;
	}
	public String getRelationType() {
		return relationType;
	}
	public void setRelationType(String relationType) {
		this.relationType = relationType;
	}
	public Integer getSmqLevel() {
		return smqLevel;
	}
	public void setSmqLevel(Integer smqLevel) {
		this.smqLevel = smqLevel;
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
	public String getPtTermAdditionVersion() {
		return ptTermAdditionVersion;
	}
	public void setPtTermAdditionVersion(String ptTermAdditionVersion) {
		this.ptTermAdditionVersion = ptTermAdditionVersion;
	}
	public String getPtTermLastModifiedVersion() {
		return ptTermLastModifiedVersion;
	}
	public void setPtTermLastModifiedVersion(String ptTermLastModifiedVersion) {
		this.ptTermLastModifiedVersion = ptTermLastModifiedVersion;
	}
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public SmqBase190 getBase() {
		return base;
	}
	public void setBase(SmqBase190 base) {
		this.base = base;
	}
	
}

