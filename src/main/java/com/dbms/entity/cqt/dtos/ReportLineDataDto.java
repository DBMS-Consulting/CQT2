package com.dbms.entity.cqt.dtos;

/**
 * Dto for Excel purposes.
 * 
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
	
	private String impact;
	private MeddraDictHierarchySearchDto meddra;

	public ReportLineDataDto(String level, String code, String term, String dots) {
		super();
		this.term = term;
		this.code = code;
		this.level = level;
		this.dots = dots;
	}
	
	/**
	 * Constructor with scope, weight, category added.
	 * @param level
	 * @param code
	 * @param term
	 * @param dots
	 */
	public ReportLineDataDto(String level, String code, String term, String dots, String scope, String weight, String categ) {
		super();
		this.term = term;
		this.code = code;
		this.level = level;
		this.dots = dots;
		this.weight = weight;
		this.scope = scope;
		this.category = categ;
	}
	
	public ReportLineDataDto(String level, String code, String term, String dots, String impact) {
		super();
		this.term = term;
		this.code = code;
		this.level = level;
		this.dots = dots;
		this.impact = impact;
	}
	
	public ReportLineDataDto(String level, String code, String term, String dots, String impact, String scope) {
		super();
		this.term = term;
		this.code = code;
		this.level = level;
		this.dots = dots;
		this.impact = impact;
		this.scope = scope;
	}

	public ReportLineDataDto(String level, String code, String term, String dots, MeddraDictHierarchySearchDto meddra) {
		super();
		this.term = term;
		this.code = code;
		this.level = level;
		this.dots = dots;
		this.meddra = meddra;
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

	@Override
	public String toString() {
		return "ReportLineDataDto [term=" + term + ", code=" + code + ", level=" + level + ", category=" + category
				+ ", weight=" + weight + ", scope=" + scope + ", dots=" + dots + "]";
	}

	public String getImpact() {
		return impact;
	}

	public void setImpact(String impact) {
		this.impact = impact;
	}

	public MeddraDictHierarchySearchDto getMeddra() {
		return meddra;
	}

	public void setMeddra(MeddraDictHierarchySearchDto meddra) {
		this.meddra = meddra;
	}

}
