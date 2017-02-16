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
import javax.persistence.Table;

/**
 * @author Jay G.(jayshanchn@hotmail.com)
 * @date Feb 9, 2017 3:13:37 AM
 **/
@Entity
@Table(name = "CQT_HLGT_PREF_TERM" //, indexes = {
		//@Index(name = "IX1_HLGT01", columnList = "HLGT_CODE"),
		//@Index(name = "IX1_HLGT02", columnList = "HLGT_NAME") }
)
public class HlgtPrefTerm implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5000707891989164217L;

	@Id
	@Column(name = "HLGT_CODE", nullable = false)
	private Long id;
	@Column(name = "HLGT_NAME", length = 100, nullable = false)
	private String name;
	@Column(name = "HLGT_WHOART_CODE", length = 7)
	private String whoartCode;
	@Column(name = "HLGT_HARTS_CODE")
	private Long hartsCode;
	@Column(name = "HLGT_COSTART_SYM", length = 21)
	private String costartSym;
	@Column(name = "HLGT_ICD9_CODE", length = 8)
	private String icd9Code;
	@Column(name = "HLGT_ICD9CM_CODE", length = 8)
	private String icd9cmCode;
	@Column(name = "HLGT_ICD10_CODE", length = 8)
	private String icd10Code;
	@Column(name = "HLGT_JART_CODE", length = 6)
	private String jartCode;

	@ManyToMany(cascade = { CascadeType.REFRESH })
	@JoinTable(name = "CQT_HLGT_HLT_COMP", joinColumns = { @JoinColumn(name = "HLGT_CODE") }, inverseJoinColumns = { @JoinColumn(name = "HLT_CODE") })
	private Set<HltPrefTerm> hltPrefTerms = new HashSet<>();

	@OneToMany(mappedBy="hlgtPrefTerm")
	private Set<MdHierarchy> mdHierarchys=new HashSet<>();
	
	public Long getId() {
		return id;
	}

	public void setCode(Long id) {
		this.id = id;
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


	public Set<HltPrefTerm> getHltPrefTerms() {
		return hltPrefTerms;
	}

	public void setHltPrefTerms(Set<HltPrefTerm> hltPrefTerms) {
		this.hltPrefTerms = hltPrefTerms;
	}

	public Set<MdHierarchy> getMdHierarchys() {
		return mdHierarchys;
	}

	public void setMdHierarchys(Set<MdHierarchy> mdHierarchys) {
		this.mdHierarchys = mdHierarchys;
	}

}
