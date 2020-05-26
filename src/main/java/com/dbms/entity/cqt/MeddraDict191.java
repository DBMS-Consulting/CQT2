package com.dbms.entity.cqt;

import java.util.Date;

import javax.persistence.Cacheable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import com.dbms.entity.BaseEntity;

/**
 * @author Jay G.(jayshanchn@hotmail.com)
 * @date Feb 10, 2017 8:46:36 AM
 **/
@Entity
@Cacheable
@Cache(usage=CacheConcurrencyStrategy.READ_WRITE)
@Table(name="MEDDRA_DICT_191")
@SequenceGenerator(name = "ID_GENERATOR", sequenceName = "SEQ_MEDDRA_DICT_191")
public class MeddraDict191 extends BaseEntity{
	private static final long serialVersionUID = 1L;
	@Id
	@Column(name="DICT_CODE",length=10)
	@GeneratedValue(generator = "ID_GENERATOR", strategy = GenerationType.AUTO)
	private Long id;
	@Column(name="DICTIONARY_NAME",length=30)
	private String dictionaryName;
	@Column(name="DICTIONARY_VERSION",length=5)
	private String dictionaryVersion;
	@Column(name="LLT_TERM",length=300)
	private String lltTerm;
	@Column(name="LLT_CODE",length=10)
	private Integer lltCode;
	@Column(name="PT_TERM",length=300)
	private String ptTerm;
	@Column(name="PT_CODE",length=10)
	private Integer ptCode;
	@Column(name="HLT_TERM",length=300)
	private String hltTerm;
	@Column(name="HLT_CODE",length=10)
	private Integer hltCode;
	@Column(name="HLGT_TERM",length=300)
	private String hlgtTerm;
	@Column(name="HLTG_CODE",length=10)
	private Integer hlgtCode;
	@Column(name="SOC_TERM",length=300)
	private String socTerm;
	@Column(name="SOC_CODE",length=10)
	private Integer socCode;
	@Column(name="PRIMARY_LINK_FLAG",length=1)
	private String primaryLinkFlag;
	@Column(name="LLT_CURRENCY",length=11)
	private String lltCurrency;
	@Column(name="CREATED_BY",length=100)
	private String createdBy;
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name="CREATION_DATE")
	private Date creationDate;
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
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
	public String getLltTerm() {
		return lltTerm;
	}
	public void setLltTerm(String lltTerm) {
		this.lltTerm = lltTerm;
	}
	public Integer getLltCode() {
		return lltCode;
	}
	public void setLltCode(Integer lltCode) {
		this.lltCode = lltCode;
	}
	public String getPtTerm() {
		return ptTerm;
	}
	public void setPtTerm(String ptTerm) {
		this.ptTerm = ptTerm;
	}
	public Integer getPtCode() {
		return ptCode;
	}
	public void setPtCode(Integer ptCode) {
		this.ptCode = ptCode;
	}
	public String getHltTerm() {
		return hltTerm;
	}
	public void setHltTerm(String hltTerm) {
		this.hltTerm = hltTerm;
	}
	public Integer getHltCode() {
		return hltCode;
	}
	public void setHltCode(Integer hltCode) {
		this.hltCode = hltCode;
	}
	public String getHlgtTerm() {
		return hlgtTerm;
	}
	public void setHlgtTerm(String hlgtTerm) {
		this.hlgtTerm = hlgtTerm;
	}
	public Integer getHlgtCode() {
		return hlgtCode;
	}
	public void setHlgtCode(Integer hlgtCode) {
		this.hlgtCode = hlgtCode;
	}
	public String getSocTerm() {
		return socTerm;
	}
	public void setSocTerm(String socTerm) {
		this.socTerm = socTerm;
	}
	public Integer getSocCode() {
		return socCode;
	}
	public void setSocCode(Integer socCode) {
		this.socCode = socCode;
	}
	public String getPrimaryLinkFlag() {
		return primaryLinkFlag;
	}
	public void setPrimaryLinkFlag(String primaryLinkFlag) {
		this.primaryLinkFlag = primaryLinkFlag;
	}
	public String getLltCurrency() {
		return lltCurrency;
	}
	public void setLltCurrency(String lltCurrency) {
		this.lltCurrency = lltCurrency;
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

}

