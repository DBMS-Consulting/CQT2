package com.dbms.entity.meddra;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * @author Jay G.(jayshanchn@hotmail.com)
 * @date Feb 9, 2017 3:13:37 AM
 **/
@Table(name = "CQT_SMQ_CONTENT" //, indexes = {
		//@Index(name = "IX1_SMQ_CONTENT01", columnList = "SMQ_CODE"),
		//@Index(name = "IX1_SMQ_CONTENT02", columnList = "TERM_CODE")}
)
public class SmqContent implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 451576507968531636L;
	@Id
	@Column(name="SMQ_CODE",nullable=false)
	private Integer smqCode;
	@Id
	@Column(name="TERM_CODE",nullable=false)
	private Integer termCode;
	@Column(name="TERM_LEVEL",nullable=false)
	private Integer level;
	@Column(name="TERM_SCOPE",nullable=false)
	private Integer scope;
	@Column(name="TERM_CATEGORY",length=1,nullable=false)
	private String category;
	@Column(name="TERM_WEIGHT",nullable=false)
	private Integer weight;
	@Column(name="TERM_STATUS",length=1,nullable=false)
	private String status;

	@Column(name="TERM_ADDITION_VERSION",length=5,nullable=false)
	private String additionVersion;
	@Column(name="TERM_LAST_MODIFIED_VERSION",length=5,nullable=false)
	private String lastModifiedVersion;
	public Integer getSmqCode() {
		return smqCode;
	}
	public void setSmqCode(Integer smqCode) {
		this.smqCode = smqCode;
	}
	public Integer getTermCode() {
		return termCode;
	}
	public void setTermCode(Integer termCode) {
		this.termCode = termCode;
	}
	public Integer getLevel() {
		return level;
	}
	public void setLevel(Integer level) {
		this.level = level;
	}
	public Integer getScope() {
		return scope;
	}
	public void setScope(Integer scope) {
		this.scope = scope;
	}
	public String getCategory() {
		return category;
	}
	public void setCategory(String category) {
		this.category = category;
	}
	public Integer getWeight() {
		return weight;
	}
	public void setWeight(Integer weight) {
		this.weight = weight;
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	public String getAdditionVersion() {
		return additionVersion;
	}
	public void setAdditionVersion(String additionVersion) {
		this.additionVersion = additionVersion;
	}
	public String getLastModifiedVersion() {
		return lastModifiedVersion;
	}
	public void setLastModifiedVersion(String lastModifiedVersion) {
		this.lastModifiedVersion = lastModifiedVersion;
	}

}

