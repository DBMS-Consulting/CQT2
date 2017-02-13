package com.dbms.entity.meddra;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

/**
 * @author Jay G.(jayshanchn@hotmail.com)
 * @date Feb 9, 2017 1:30:48 AM
 **/
@Entity
@Table(name = "CQT_LOW_LEVEL_TERM" //, indexes = {
		//@Index(name = "IX1_PT_LLT01", columnList = "LLT_CODE"),
		//@Index(name = "IX1_PT_LLT02", columnList = "LLT_NAME"),
		//@Index(name = "IX1_PT_LLT03", columnList = "PT_CODE") }
)
public class LowLevelTerm implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 368490947220514599L;
	@Id
	@Column(name = "LLT_CODE", nullable = false)
	private Integer code;
	@Column(name = "LLT_NAME", length = 100, nullable = false)
	private String name;
	@Column(name = "LLT_WHOART_CODE", length = 7)
	private String whoartCode;
	@Column(name = "LLT_HARTS_CODE")
	private Long hartsCode;
	@Column(name = "LLT_COSTART_SYM", length = 21)
	private String costartSym;
	@Column(name = "LLT_ICD9_CODE", length = 8)
	private String icd9Code;
	@Column(name = "LLT_ICD9CM_CODE", length = 8)
	private String icd9cmCode;
	@Column(name = "LLT_ICD10_CODE", length = 8)
	private String icd10Code;
	@Column(name = "LLT_CURRENCY", length = 1)
	private String currency;
	@Column(name = "LLT_JART_CODE", length = 6)
	private String jartCode;
	
	@ManyToOne
	@JoinColumn(name="PT_CODE")
	private PrefTerm prefTerm;

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

	public String getCurrency() {
		return currency;
	}

	public void setCurrency(String currency) {
		this.currency = currency;
	}

	public String getJartCode() {
		return jartCode;
	}

	public void setJartCode(String jartCode) {
		this.jartCode = jartCode;
	}

	public PrefTerm getPrefTerm() {
		return prefTerm;
	}

	public void setPrefTerm(PrefTerm prefTerm) {
		this.prefTerm = prefTerm;
	}

}
