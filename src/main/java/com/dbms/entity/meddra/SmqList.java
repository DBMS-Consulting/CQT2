package com.dbms.entity.meddra;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.Table;
import javax.persistence.Entity;

/**
 * @author Jay G.(jayshanchn@hotmail.com)
 * @date Feb 9, 2017 3:13:37 AM
 **/
@Entity
@Table(name = "CQT_SMQ_LIST" //, indexes = {
		//@Index(name = "IX1_SMQ_LIST01", columnList = "SMQ_CODE")}
)
public class SmqList implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 6515344905755098903L;
	@Id
	@Column(name="SMQ_CODE",nullable=false)
	private Integer code;
	@Column(name="SMQ_NAME",length=100,nullable=false)
	private String name;
	@Column(name="SMQ_LEVEL",nullable=false)
	private Integer level;
	@Lob
	@Column(name="SMQ_DESCRIPTION",nullable=false)
	private String description;
	@Lob
	@Column(name="SMQ_SOURCE")
	private String source;
	@Lob
	@Column(name="SMQ_NOTE")
	private String note;
	@Column(name="MEDDRA_VERSION",length=5,nullable=false)
	private String version;
	@Column(name="STATUS",length=1,nullable=false)
	private String status;
	@Column(name="SMQ_ALGORITHM",nullable=false)
	private String algorithm;
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
	public Integer getLevel() {
		return level;
	}
	public void setLevel(Integer level) {
		this.level = level;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public String getSource() {
		return source;
	}
	public void setSource(String source) {
		this.source = source;
	}
	public String getNote() {
		return note;
	}
	public void setNote(String note) {
		this.note = note;
	}
	public String getVersion() {
		return version;
	}
	public void setVersion(String version) {
		this.version = version;
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	public String getAlgorithm() {
		return algorithm;
	}
	public void setAlgorithm(String algorithm) {
		this.algorithm = algorithm;
	}

}

