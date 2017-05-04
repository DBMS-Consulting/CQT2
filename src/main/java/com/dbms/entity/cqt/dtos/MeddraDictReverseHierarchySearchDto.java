package com.dbms.entity.cqt.dtos;

import com.dbms.entity.IEntity;

public class MeddraDictReverseHierarchySearchDto implements IEntity {

	private static final long serialVersionUID = -490379655871719191L;

	private Long meddraDictId;

	private String lltTerm;

	private String lltCode;

	private String ptTerm;

	private String ptCode;

	private String hltTerm;

	private String hltCode;

	private String hlgtTerm;

	private String hlgtCode;

	private String socTerm;

	private String socCode;

	private String primaryPathFlag;

	private String lltCurrency;

	public Long getId() {
		return meddraDictId;
	}

	public void setId(Long meddraDictId) {
		this.meddraDictId = meddraDictId;
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

}
