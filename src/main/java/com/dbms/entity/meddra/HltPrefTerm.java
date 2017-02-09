package com.dbms.entity.meddra;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.OneToMany;
import javax.persistence.Table;

/**
 * @author Jay G.(jayshanchn@hotmail.com)
 * @date Feb 9, 2017 3:13:37 AM
 **/
@Entity
@Table(name = "CQT_HLT_TERM", indexes = {
		@Index(name = "IX1_HLT01", columnList = "HLT_CODE"),
		@Index(name = "IX1_HLT02", columnList = "HLT_NAME") })
public class HltPrefTerm implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3027934687818534781L;

	@Id
	@Column(name = "HLT_CODE", nullable = false)
	private Integer code;

	@Column(name = "HLT_NAME", length = 100, nullable = false)
	private String name;
	@Column(name = "HLT_WHOART_CODE", length = 7)
	private String whoartCode;
	@Column(name = "HLT_HARTS_CODE")
	private Long hartsCode;
	@Column(name = "HLT_COSTART_SYM", length = 21)
	private String costartSym;
	@Column(name = "HLT_ICD9_CODE", length = 8)
	private String icd9Code;
	@Column(name = "HLT_ICD9CM_CODE", length = 8)
	private String icd9cmCode;
	@Column(name = "HLT_ICD10_CODE", length = 8)
	private String icd10Code;
	@Column(name = "HLT_JART_CODE", length = 6)
	private String jartCode;

	@ManyToMany(cascade = { CascadeType.REFRESH })
	@JoinTable(name = "CQT_HLT_PREF_COMP", joinColumns = { @JoinColumn(name = "HLT_CODE") }, inverseJoinColumns = { @JoinColumn(name = "PT_CODE") })
	private Set<PrefTerm> prefTerms = new HashSet<>();
	
	@OneToMany(mappedBy="hltPrefTerm")
	private Set<MdHierarchy> mdHierarchys=new HashSet<>();
	
	
	public Integer getCode() {
		return code;
	}

	public void setCode(Integer code) {
		this.code = code;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getWhoartCode() {
		return whoartCode;
	}

	public void setWhoartCode(String whoartCode) {
		this.whoartCode = whoartCode;
	}

	public Long getHartsCode() {
		return hartsCode;
	}

	public void setHartsCode(Long hartsCode) {
		this.hartsCode = hartsCode;
	}

	public String getCostartSym() {
		return costartSym;
	}

	public void setCostartSym(String costartSym) {
		this.costartSym = costartSym;
	}

	public String getIcd9Code() {
		return icd9Code;
	}

	public void setIcd9Code(String icd9Code) {
		this.icd9Code = icd9Code;
	}

	public String getIcd9cmCode() {
		return icd9cmCode;
	}

	public void setIcd9cmCode(String icd9cmCode) {
		this.icd9cmCode = icd9cmCode;
	}

	public String getIcd10Code() {
		return icd10Code;
	}

	public void setIcd10Code(String icd10Code) {
		this.icd10Code = icd10Code;
	}

	public String getJartCode() {
		return jartCode;
	}

	public void setJartCode(String jartCode) {
		this.jartCode = jartCode;
	}

	public Set<PrefTerm> getPrefTerms() {
		return prefTerms;
	}

	public void setPrefTerms(Set<PrefTerm> prefTerms) {
		this.prefTerms = prefTerms;
	}

	public Set<MdHierarchy> getMdHierarchys() {
		return mdHierarchys;
	}

	public void setMdHierarchys(Set<MdHierarchy> mdHierarchys) {
		this.mdHierarchys = mdHierarchys;
	}

}
