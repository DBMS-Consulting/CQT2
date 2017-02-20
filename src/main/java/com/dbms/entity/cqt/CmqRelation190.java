package com.dbms.entity.cqt;

import java.math.BigDecimal;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import com.dbms.entity.BaseEntity;

/**
 * CmqRelations190 generated by hbm2java
 */
@Entity
@Table(name = "CMQ_RELATIONS_CURRENT")
public class CmqRelation190 extends BaseEntity {

	private static final long serialVersionUID = -3022729714656606289L;

	@Id
	@GeneratedValue(generator = "CMQ_RELATION_ID_SEQ", strategy = GenerationType.SEQUENCE)
	@SequenceGenerator(name = "CMQ_RELATION_ID_SEQ", sequenceName = "CMQ_RELATION_ID_SEQ", allocationSize = 20)
	@Column(name = "CMQ_RELATION_ID", unique = true, nullable = false)
	private Long cmqRelationId;

	@Column(name = "CMQ_ID", nullable = false)
	private Long cmqId;

	@Column(name = "CMQ_CODE", nullable = false, insertable = false, updatable = false)
	private Long cmqCode;

	@Column(name = "SOC_CODE")
	private Long socCode;

	@Column(name = "HLGT_CODE")
	private Long hlgtCode;

	@Column(name = "HLT_CODE")
	private Long hltCode;

	@Column(name = "PT_CODE")
	private Long ptCode;

	@Column(name = "LLT_CODE")
	private Long lltCode;

	@Column(name = "SMQ_CODE")
	private Long smqCode;

	@Column(name = "TERM_WEIGHT")
	private Long termWeight;

	@Column(name = "TERM_CATEGORY", length = 1)
	private String termCategory;

	@Column(name = "TERM_SCOPE", length = 15)
	private String termScope;

	@Column(name = "RELATION_TYPE", length = 15)
	private String relationType;

	@Column(name = "RELATION_IMPACT_TYPE", length = 15)
	private String relationImpactType;

	@Column(name = "CREATED_BY", nullable = false, length = 30)
	private String createdBy;

	@Temporal(TemporalType.DATE)
	@Column(name = "CREATION_DATE", nullable = false, length = 7)
	private Date creationDate;

	@Column(name = "LAST_MODIFIED_BY", length = 30)
	private String lastModifiedBy;

	@Temporal(TemporalType.DATE)
	@Column(name = "LAST_MODIFIED_DATE", length = 7)
	private Date lastModifiedDate;

	@Column(name = "DICTIONARY_NAME", nullable = false, length = 10)
	private String dictionaryName;

	@Column(name = "DICTIONARY_VERSION", nullable = false, length = 5)
	private String dictionaryVersion;

	@Column(name = "CMQ_SUBVERSION", nullable = false, precision = 10)
	private BigDecimal cmqSubversion;

	@ManyToOne
	@JoinColumn(name = "CMQ_CODE", nullable = false, updatable = false)
	private CmqBase190 cmqBase;

	public Long getId() {
		return cmqRelationId;
	}

	public void setCmqRelationId(Long cmqRelationId) {
		this.cmqRelationId = cmqRelationId;
	}

	public Long getCmqId() {
		return cmqId;
	}

	public void setCmqId(Long cmqId) {
		this.cmqId = cmqId;
	}

	public Long getCmqCode() {
		return cmqCode;
	}

	public void setCmqCode(Long cmqCode) {
		this.cmqCode = cmqCode;
	}

	public Long getSocCode() {
		return socCode;
	}

	public void setSocCode(Long socCode) {
		this.socCode = socCode;
	}

	public Long getHlgtCode() {
		return hlgtCode;
	}

	public void setHlgtCode(Long hlgtCode) {
		this.hlgtCode = hlgtCode;
	}

	public Long getHltCode() {
		return hltCode;
	}

	public void setHltCode(Long hltCode) {
		this.hltCode = hltCode;
	}

	public Long getPtCode() {
		return ptCode;
	}

	public void setPtCode(Long ptCode) {
		this.ptCode = ptCode;
	}

	public Long getLltCode() {
		return lltCode;
	}

	public void setLltCode(Long lltCode) {
		this.lltCode = lltCode;
	}

	public Long getSmqCode() {
		return smqCode;
	}

	public void setSmqCode(Long smqCode) {
		this.smqCode = smqCode;
	}

	public Long getTermWeight() {
		return termWeight;
	}

	public void setTermWeight(Long termWeight) {
		this.termWeight = termWeight;
	}

	public String getTermCategory() {
		return termCategory;
	}

	public void setTermCategory(String termCategory) {
		this.termCategory = termCategory;
	}

	public String getTermScope() {
		return termScope;
	}

	public void setTermScope(String termScope) {
		this.termScope = termScope;
	}

	public String getRelationType() {
		return relationType;
	}

	public void setRelationType(String relationType) {
		this.relationType = relationType;
	}

	public String getRelationImpactType() {
		return relationImpactType;
	}

	public void setRelationImpactType(String relationImpactType) {
		this.relationImpactType = relationImpactType;
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

	public String getDictionaryName() {
		return dictionaryName;
	}

	public void setDictionaryName(String dictionaryName) {
		this.dictionaryName = dictionaryName;
	}

	public String getDictionaryVersion() {
		return dictionaryVersion;
	}

	public void setDictionaryVersion(String dictionaryVersion) {
		this.dictionaryVersion = dictionaryVersion;
	}

	public BigDecimal getCmqSubversion() {
		return cmqSubversion;
	}

	public void setCmqSubversion(BigDecimal cmqSubversion) {
		this.cmqSubversion = cmqSubversion;
	}

	public CmqBase190 getCmqBase() {
		return cmqBase;
	}

	public void setCmqBase(CmqBase190 cmqBase) {
		this.cmqBase = cmqBase;
	}

}
