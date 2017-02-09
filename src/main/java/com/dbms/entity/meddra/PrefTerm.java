package com.dbms.entity.meddra;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;

/**
 * @author Jay G.(jayshanchn@hotmail.com)
 * @date Feb 9, 2017 3:13:37 AM
 **/
@Entity
@Table(name = "CQT_PREF_TERM", indexes = {
		@Index(name = "IX1_PT01", columnList = "PT_CODE"),
		@Index(name = "IX1_PT02", columnList = "PT_NAME"),
		@Index(name = "IX1_PT03", columnList = "PT_SOC_CODE") })
public class PrefTerm implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 4835091140858948132L;

	@Id
	@Column(name="PT_CODE",nullable=false)
	private Integer code;
	
	@Column(name="PT_NAME",length=100,nullable=false)
	private String name;
	@Column(name="NULL_FIELD",length=1)
	private String nullField;
	@Column(name="PT_WHOART_CODE",length=7)
	private String whoartCode;
	@Column(name="PT_HARTS_CODE")
	private Long hartsCode;
	@Column(name="PT_COSTART_SYM",length=21)
	private String costartSym;
	@Column(name="PT_ICD9_CODE",length=8)
	private String icd9Code;
	@Column(name="PT_ICD9CM_CODE",length=8)
	private String icd9cmCode;
	@Column(name="PT_ICD10_CODE",length=8)
	private String icd10Code;
	@Column(name="PT_JART_CODE",length=6)
	private String jartCode;
	
	@OneToMany(mappedBy="prefTerm")
	private Set<LowLevelTerm> lowLevelTerms=new HashSet<>();
	
	@OneToMany(mappedBy="prefTerm")
	private Set<MdHierarchy> mdHierarchys=new HashSet<>();
	
	@ManyToOne
	@JoinColumn(name="PT_SOC_CODE")
	private SocTerm socTerm;
	
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
	public String getNullField() {
		return nullField;
	}
	public void setNullField(String nullField) {
		this.nullField = nullField;
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
	public Set<LowLevelTerm> getLowLevelTerms() {
		return lowLevelTerms;
	}
	public void setLowLevelTerms(Set<LowLevelTerm> lowLevelTerms) {
		this.lowLevelTerms = lowLevelTerms;
	}
	public Set<MdHierarchy> getMdHierarchys() {
		return mdHierarchys;
	}
	public void setMdHierarchys(Set<MdHierarchy> mdHierarchys) {
		this.mdHierarchys = mdHierarchys;
	}
	public SocTerm getSocTerm() {
		return socTerm;
	}
	public void setSocTerm(SocTerm socTerm) {
		this.socTerm = socTerm;
	}

}

