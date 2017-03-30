package com.dbms.entity.cqt.dtos;

import com.dbms.entity.IEntity;

public class MeddraDictHierarchySearchDto implements IEntity {

	private static final long serialVersionUID = -1547764018697656790L;

	private Long meddraDictId;

	private String term;

	private String code;

	private String primaryPathFlag;
	
	private String newPt;
	private String promotedPt;
	private String newLlt;
	private String demotedLlt;
	private String promotedLlt;
	
	private String primarySocChange;
	private String demotedPt;
	private String movedLlt;
	private String lltCurrencyChange;
	private String ptNameChanged;
	
	private String lltNameChanged;
	private String newHlt;
	private String newHlgt;
	private String movedPt;
	private String movedHlt;
	
	private String movedHlgt;
	private String hlgtNameChanged;
	private String hltNameChanged;
	private String socNameChanged;
	private String mergedHlt;
	private String mergedHlgt;

	public Long getId() {
		return meddraDictId;
	}

	public void setId(Long meddraDictId) {
		this.meddraDictId = meddraDictId;
	}

	public String getTerm() {
		return term;
	}

	public void setTerm(String term) {
		this.term = term;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getPrimaryPathFlag() {
		return primaryPathFlag;
	}

	public void setPrimaryPathFlag(String primaryPathFlag) {
		this.primaryPathFlag = primaryPathFlag;
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
