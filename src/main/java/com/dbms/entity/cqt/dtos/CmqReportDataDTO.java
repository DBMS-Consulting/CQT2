package com.dbms.entity.cqt.dtos;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;

public class CmqReportDataDTO implements Serializable {

	private Long levelNum;
	private String level;
	private String term;
	private String codeTerm;
	private String termWeight;
	private String termCategory;
	private String termScope;
	
	public Long getLevelNum() {
		return levelNum;
	}
	public void setLevelNum(Long levelNum) {
		this.levelNum = levelNum;
	}
	public String getLevel() {
		return level;
	}
	public void setLevel(String level) {
		this.level = level;
	}
	public String getTerm() {
		return term;
	}
	public void setTerm(String term) {
		this.term = term;
	}
	public String getCodeTerm() {
		return codeTerm;
	}
	public void setCodeTerm(String codeTerm) {
		this.codeTerm = codeTerm;
	}
	public String getTermWeight() {
		return termWeight;
	}
	public void setTermWeight(String termWeight) {
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
	
}
