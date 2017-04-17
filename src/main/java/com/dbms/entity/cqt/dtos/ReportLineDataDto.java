package com.dbms.entity.cqt.dtos;

/**
 * Dto for Excel purposes.
 * @author 
 *
 */
public class ReportLineDataDto {
	
	private String term;
	private String code;
	private String level;
	private String category;
	private String weight;
	private String scope;
	private String dots;
	
	public ReportLineDataDto(String level, String code, String term, String dots) {
		super();
		this.term = term;
		this.code = code;
		this.level = level;
		this.dots = dots;
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
	public String getLevel() {
		return level;
	}
	public void setLevel(String level) {
		this.level = level;
	}
	public String getCategory() {
		return category;
	}
	public void setCategory(String category) {
		this.category = category;
	}
	public String getWeight() {
		return weight;
	}
	public void setWeight(String weight) {
		this.weight = weight;
	}
	public String getScope() {
		return scope;
	}
	public void setScope(String scope) {
		this.scope = scope;
	}
	public String getDots() {
		return dots;
	}
	public void setDots(String dots) {
		this.dots = dots;
	}
	
	
}
