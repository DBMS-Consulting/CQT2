package com.dbms.entity.cqt;

import java.util.Date;

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
@Cache(usage=CacheConcurrencyStrategy.READ_WRITE)
@Table(name = "MEDDRA_DICT_CURRENT")
public class MeddraDict190 extends BaseEntity {

	private static final long serialVersionUID = 2129890222601213956L;

	@Id
	@Column(name = "MEDDRA_DICT_ID", unique = true, nullable = false, precision = 38, scale = 0)
	@GeneratedValue(generator = "MEDDRA_DICT_ID_SEQ", strategy = GenerationType.SEQUENCE)
	@SequenceGenerator(name = "MEDDRA_DICT_ID_SEQ", sequenceName = "MEDDRA_DICT_ID_SEQ", allocationSize = 20)
	private Long meddraDictId;

	@Column(name = "DICTIONARY_NAME", nullable = false, length = 10)
	private String dictionaryName;

	@Column(name = "DICTIONARY_VERSION", nullable = false, length = 5)
	private String dictionaryVersion;

	@Column(name = "LLT_TERM", nullable = false, length = 100)
	private String lltTerm;

	@Column(name = "LLT_CODE", nullable = false, length = 10)
	private String lltCode;

	@Column(name = "PT_TERM", nullable = false, length = 100)
	private String ptTerm;

	@Column(name = "PT_CODE", nullable = false, length = 10)
	private String ptCode;

	@Column(name = "HLT_TERM", nullable = false, length = 100)
	private String hltTerm;

	@Column(name = "HLT_CODE", nullable = false, length = 10)
	private String hltCode;

	@Column(name = "HLGT_TERM", nullable = false, length = 100)
	private String hlgtTerm;

	@Column(name = "HLGT_CODE", nullable = false, length = 10)
	private String hlgtCode;

	@Column(name = "SOC_TERM", nullable = false, length = 100)
	private String socTerm;

	@Column(name = "SOC_CODE", nullable = false, length = 10)
	private String socCode;

	@Column(name = "SOC_ABBREV", nullable = false, length = 100)
	private String socAbbrev;

	@Column(name = "PRIMARY_PATH_FLAG", nullable = false, length = 1)
	private String primaryPathFlag;

	@Column(name = "LLT_CURRENCY", nullable = false, length = 1)
	private String lltCurrency;

	@Column(name = "CREATED_BY", nullable = false, length = 100)
	private String createdBy;

	@Temporal(TemporalType.DATE)
	@Column(name = "CREATION_DATE", length = 7)
	private Date creationDate;

	@Column(name = "NEW_PT", nullable = true, length = 15)
	private String newPt;

	@Column(name = "PROMOTED_PT", nullable = true, length = 15)
	private String promotedPt;

	@Column(name = "NEW_LLT", nullable = true, length = 15)
	private String newLlt;

	@Column(name = "DEMOTED_LLT", nullable = true, length = 15)
	private String demotedLlt;

	@Column(name = "PROMOTED_LLT", nullable = true, length = 15)
	private String promotedLlt;

	@Column(name = "PRIMARY_SOC_CHANGE", nullable = true, length = 15)
	private String primarySocChange;

	@Column(name = "DEMOTED_PT", nullable = true, length = 15)
	private String demotedPt;

	@Column(name = "MOVED_LLT", nullable = true, length = 15)
	private String movedLlt;

	@Column(name = "LLT_CURRENCY_CHANGE", nullable = true, length = 15)
	private String lltCurrencyChange;

	@Column(name = "PT_NAME_CHANGED", nullable = true, length = 15)
	private String ptNameChanged;

	@Column(name = "LLT_NAME_CHANGED", nullable = true, length = 15)
	private String lltNameChanged;

	@Column(name = "NEW_HLT", nullable = true, length = 15)
	private String newHlt;

	@Column(name = "NEW_HLGT", nullable = true, length = 15)
	private String newHlgt;

	@Column(name = "MOVED_PT", nullable = true, length = 15)
	private String movedPt;

	@Column(name = "MOVED_HLT", nullable = true, length = 15)
	private String movedHlt;

	@Column(name = "MOVED_HLGT", nullable = true, length = 15)
	private String movedHlgt;

	@Column(name = "HLGT_NAME_CHANGED", nullable = true, length = 15)
	private String hlgtNameChanged;

	@Column(name = "HLT_NAME_CHANGED", nullable = true, length = 15)
	private String hltNameChanged;

	@Column(name = "SOC_NAME_CHANGED", nullable = true, length = 15)
	private String socNameChanged;

	@Column(name = "MERGED_HLT", nullable = true, length = 15)
	private String mergedHlt;

	@Column(name = "MERGED_HLGT", nullable = true, length = 15)
	private String mergedHlgt;

	public Long getId() {
		return meddraDictId;
	}

	public void setCmqId(Long meddraDictId) {
		this.meddraDictId = meddraDictId;
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

	public String getLltCode() {
		return lltCode;
	}

	public void setLltCode(String lltCode) {
		this.lltCode = lltCode;
	}

	public String getPtTerm() {
		return ptTerm;
	}

	public void setPtTerm(String ptTerm) {
		this.ptTerm = ptTerm;
	}

	public String getPtCode() {
		return ptCode;
	}

	public void setPtCode(String ptCode) {
		this.ptCode = ptCode;
	}

	public String getHltTerm() {
		return hltTerm;
	}

	public void setHltTerm(String hltTerm) {
		this.hltTerm = hltTerm;
	}

	public String getHltCode() {
		return hltCode;
	}

	public void setHltCode(String hltCode) {
		this.hltCode = hltCode;
	}

	public String getHlgtTerm() {
		return hlgtTerm;
	}

	public void setHlgtTerm(String hlgtTerm) {
		this.hlgtTerm = hlgtTerm;
	}

	public String getHlgtCode() {
		return hlgtCode;
	}

	public void setHlgtCode(String hlgtCode) {
		this.hlgtCode = hlgtCode;
	}

	public String getSocTerm() {
		return socTerm;
	}

	public void setSocTerm(String socTerm) {
		this.socTerm = socTerm;
	}

	public String getSocCode() {
		return socCode;
	}

	public void setSocCode(String socCode) {
		this.socCode = socCode;
	}

	public String getSocAbbrev() {
		return socAbbrev;
	}

	public void setSocAbbrev(String socAbbrev) {
		this.socAbbrev = socAbbrev;
	}

	public String getPrimaryPathFlag() {
		return primaryPathFlag;
	}

	public void setPrimaryPathFlag(String primaryPathFlag) {
		this.primaryPathFlag = primaryPathFlag;
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

	public Long getMeddraDictId() {
		return meddraDictId;
	}

	public void setMeddraDictId(Long meddraDictId) {
		this.meddraDictId = meddraDictId;
	}

	public String getNewPt() {
		return newPt;
	}

	public void setNewPt(String newPt) {
		this.newPt = newPt;
	}

	public String getPromotedPt() {
		return promotedPt;
	}

	public void setPromotedPt(String promotedPt) {
		this.promotedPt = promotedPt;
	}

	public String getNewLlt() {
		return newLlt;
	}

	public void setNewLlt(String newLlt) {
		this.newLlt = newLlt;
	}

	public String getDemotedLlt() {
		return demotedLlt;
	}

	public void setDemotedLlt(String demotedLlt) {
		this.demotedLlt = demotedLlt;
	}

	public String getPromotedLlt() {
		return promotedLlt;
	}

	public void setPromotedLlt(String promotedLlt) {
		this.promotedLlt = promotedLlt;
	}

	public String getPrimarySocChange() {
		return primarySocChange;
	}

	public void setPrimarySocChange(String primarySocChange) {
		this.primarySocChange = primarySocChange;
	}

	public String getDemotedPt() {
		return demotedPt;
	}

	public void setDemotedPt(String demotedPt) {
		this.demotedPt = demotedPt;
	}

	public String getMovedLlt() {
		return movedLlt;
	}

	public void setMovedLlt(String movedLlt) {
		this.movedLlt = movedLlt;
	}

	public String getLltCurrencyChange() {
		return lltCurrencyChange;
	}

	public void setLltCurrencyChange(String lltCurrencyChange) {
		this.lltCurrencyChange = lltCurrencyChange;
	}

	public String getPtNameChanged() {
		return ptNameChanged;
	}

	public void setPtNameChanged(String ptNameChanged) {
		this.ptNameChanged = ptNameChanged;
	}

	public String getLltNameChanged() {
		return lltNameChanged;
	}

	public void setLltNameChanged(String lltNameChanged) {
		this.lltNameChanged = lltNameChanged;
	}

	public String getNewHlt() {
		return newHlt;
	}

	public void setNewHlt(String newHlt) {
		this.newHlt = newHlt;
	}

	public String getNewHlgt() {
		return newHlgt;
	}

	public void setNewHlgt(String newHlgt) {
		this.newHlgt = newHlgt;
	}

	public String getMovedPt() {
		return movedPt;
	}

	public void setMovedPt(String movedPt) {
		this.movedPt = movedPt;
	}

	public String getMovedHlt() {
		return movedHlt;
	}

	public void setMovedHlt(String movedHlt) {
		this.movedHlt = movedHlt;
	}

	public String getMovedHlgt() {
		return movedHlgt;
	}

	public void setMovedHlgt(String movedHlgt) {
		this.movedHlgt = movedHlgt;
	}

	public String getHlgtNameChanged() {
		return hlgtNameChanged;
	}

	public void setHlgtNameChanged(String hlgtNameChanged) {
		this.hlgtNameChanged = hlgtNameChanged;
	}

	public String getHltNameChanged() {
		return hltNameChanged;
	}

	public void setHltNameChanged(String hltNameChanged) {
		this.hltNameChanged = hltNameChanged;
	}

	public String getSocNameChanged() {
		return socNameChanged;
	}

	public void setSocNameChanged(String socNameChanged) {
		this.socNameChanged = socNameChanged;
	}

	public String getMergedHlt() {
		return mergedHlt;
	}

	public void setMergedHlt(String mergedHlt) {
		this.mergedHlt = mergedHlt;
	}

	public String getMergedHlgt() {
		return mergedHlgt;
	}

	public void setMergedHlgt(String mergedHlgt) {
		this.mergedHlgt = mergedHlgt;
	}


}
