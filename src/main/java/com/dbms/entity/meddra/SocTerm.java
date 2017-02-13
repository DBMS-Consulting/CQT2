package com.dbms.entity.meddra;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.Table;

/**
 * @author Jay G.(jayshanchn@hotmail.com)
 * @date Feb 9, 2017 3:13:37 AM
 **/
@Entity
@Table(name = "CQT_SOC_TERM" //, indexes = {
		//@Index(name = "IX1_SOC01", columnList = "SOC_CODE"),
		//@Index(name = "IX1_SOC02", columnList = "SOC_NAME") }
)
public class SocTerm implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -6395390190275230384L;

	@Id
	@Column(name="SOC_CODE",nullable=false)
	private Integer code;
	@Column(name="SOC_NAME",length=100,nullable=false)
	private String name;
	@Column(name="SOC_ABBREV",length=5,nullable=false)
	private String socAbbrev;
	@Column(name="SOC_WHOART_CODE",length=7)
	private String whoartCode;
	@Column(name="SOC_HARTS_CODE")
	private Long hartsCode;
	@Column(name="SOC_COSTART_SYM",length=21)
	private String costartSym;
	@Column(name="SOC_ICD9_CODE",length=8)
	private String icd9Code;
	@Column(name="SOC_ICD9CM_CODE",length=8)
	private String icd9cmCode;
	@Column(name="SOC_ICD10_CODE",length=8)
	private String icd10Code;
	@Column(name="SOC_JART_CODE",length=6)
	private String jartCode;

	@ManyToMany(cascade = { CascadeType.REFRESH })
	@JoinTable(name = "CQT_SOC_HLGT_COMP", joinColumns = { @JoinColumn(name = "SOC_CODE") }, 
		inverseJoinColumns = { @JoinColumn(name = "HLGT_CODE") })
	private Set<HlgtPrefTerm> hlgtPrefTerms = new HashSet<>();
	
	@OneToMany(mappedBy="socTerm",cascade={CascadeType.PERSIST,CascadeType.MERGE})
	private Set<PrefTerm> prefTerms=new HashSet<>();
	
	@OneToMany(mappedBy="socTerm")
	private Set<MdHierarchy> mdHierarchys=new HashSet<>();

	@OneToOne(cascade=CascadeType.PERSIST)
	@PrimaryKeyJoinColumn(name="SOC_CODE")
	private SocIntlOrder socIntlOrder;
	
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

	public String getSocAbbrev() {
		return socAbbrev;
	}

	public void setSocAbbrev(String socAbbrev) {
		this.socAbbrev = socAbbrev;
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

	

	public Set<HlgtPrefTerm> getHlgtPrefTerms() {
		return hlgtPrefTerms;
	}

	public void setHlgtPrefTerms(Set<HlgtPrefTerm> hlgtPrefTerms) {
		this.hlgtPrefTerms = hlgtPrefTerms;
	}

	public Set<PrefTerm> getPrefTerms() {
		return prefTerms;
	}

	public void setPrefTerms(Set<PrefTerm> prefTerms) {
		this.prefTerms = prefTerms;
	}

	public void addPrefTerm(PrefTerm prefTerm){
		this.prefTerms.add(prefTerm);
		prefTerm.setSocTerm(this);
	}
	public SocIntlOrder getSocIntlOrder() {
		return socIntlOrder;
	}

	public void setSocIntlOrder(SocIntlOrder socIntlOrder) {
		this.socIntlOrder = socIntlOrder;
	}

	public Set<MdHierarchy> getMdHierarchys() {
		return mdHierarchys;
	}

	public void setMdHierarchys(Set<MdHierarchy> mdHierarchys) {
		this.mdHierarchys = mdHierarchys;
	}
	
}

