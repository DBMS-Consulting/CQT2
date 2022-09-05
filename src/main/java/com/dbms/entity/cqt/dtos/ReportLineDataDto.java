package com.dbms.entity.cqt.dtos;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Dto for Excel purposes.
 * 
 * @author
 *
 */
public class ReportLineDataDto implements Comparable<ReportLineDataDto>{

	private String term;
	private String code;
	private Long levelNum;
	private String level;
	private String category;
	private String weight;
	private String scope;
	private String dots;
	private String status;
	
	private String impact;
	private MeddraDictHierarchySearchDto meddra;
	private List<ReportLineDataDto> children = new ArrayList<>();

	public ReportLineDataDto() {
		super();
	}

	public ReportLineDataDto(String level, String code, String term, String dots) {
		super();
		this.term = term;
		this.code = code;
		this.level = level;
		this.dots = dots;
	}

	public ReportLineDataDto(Long levelNum, String level, String code, String term, String dots) {
		super();
		this.term = term;
		this.code = code;
		this.levelNum = levelNum;
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
	public ReportLineDataDto(String level, String code, String term, String dots, String scope, String weight, String categ, String impact, String status) {
		super();
		this.term = term;
		this.code = code;
		this.level = level;
		this.dots = dots;
		this.weight = weight;
		this.scope = scope;
		this.category = categ;
		this.impact = impact;
	}
	
	public ReportLineDataDto(String level, String code, String term, String dots, String impact) {
		super();
		this.term = term;
		this.code = code;
		this.level = level;
		this.dots = dots;
		this.impact = impact;
	}
	
	public ReportLineDataDto(String level, String code, String term, String dots, String impact, String scope, String status) {
		super();
		this.term = term;
		this.code = code;
		this.level = level;
		this.dots = dots;
		this.impact = impact;
		this.scope = scope;
		this.status = status;
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
	
	public ReportLineDataDto(String level, String code, String term, String dots, MeddraDictHierarchySearchDto meddra, String impact) {
		super();
		this.term = term;
		this.code = code;
		this.level = level;
		this.dots = dots;
		this.meddra = meddra;
		this.impact = impact;
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
		return "ReportLineDataDto [term=" + term + ", code=" + code + ", levelNum=" + levelNum + ", level=" + level
				+ ", category=" + category + ", weight=" + weight + ", scope=" + scope + ", dots=" + dots + ", status="
				+ status + ", impact=" + impact + ", meddra=" + meddra + "]";
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

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public List<ReportLineDataDto> getChildren() {
		return children;
	}

	public void setChildren(List<ReportLineDataDto> children) {
		this.children = children;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		ReportLineDataDto that = (ReportLineDataDto) o;
		return Objects.equals(term, that.term) && Objects.equals(code, that.code) && Objects.equals(levelNum, that.levelNum) && Objects.equals(level, that.level) && Objects.equals(category, that.category) && Objects.equals(weight, that.weight) && Objects.equals(scope, that.scope) && Objects.equals(status, that.status) && Objects.equals(impact, that.impact);
	}

	@Override
	public int hashCode() {
		return Objects.hash(term, code, levelNum, level, category, weight, scope, status, impact);
	}

	@Override
	public int compareTo(ReportLineDataDto o) {
		return 0;
	}
}
