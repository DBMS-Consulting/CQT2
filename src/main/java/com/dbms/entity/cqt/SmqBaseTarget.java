package com.dbms.entity.cqt;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import com.dbms.entity.BaseEntity;

/**
 * SmqBase190 generated by hbm2java
 */
@Entity
@Table(name = "SMQ_BASE_TARGET")
public class SmqBaseTarget extends BaseEntity {

	private static final long serialVersionUID = 5502417767884764241L;

	@Id
	@GeneratedValue(generator = "SMQ_ID_SEQ", strategy = GenerationType.SEQUENCE)
	@SequenceGenerator(name = "SMQ_ID_SEQ", sequenceName = "SMQ_ID_SEQ", allocationSize = 20)
	@Column(name = "SMQ_ID", unique = true, nullable = false)
	private Long smqId;

	@Column(name = "SMQ_CODE", nullable = false, precision = 38)
	private Long smqCode;

	@Column(name = "SMQ_NAME", nullable = false, length = 100)
	private String smqName;

	@Column(name = "SMQ_PARENT_CODE", nullable = false, precision = 38)
	private Long smqParentCode;

	@Column(name = "SMQ_PARENT_NAME", length = 100)
	private String smqParentName;

	@Column(name = "SMQ_LEVEL", nullable = false, precision = 1)
	private Integer smqLevel;

	@Column(name = "SMQ_DESCRIPTION", nullable = false, length = 4000)
	private String smqDescription;

	@Column(name = "SMQ_SOURCE", length = 4000)
	private String smqSource;

	@Column(name = "SMQ_NOTE", length = 4000)
	private String smqNote;

	@Column(name = "SMQ_STATUS", nullable = false, length = 1)
	private String smqStatus;

	@Column(name = "SMQ_ALGORITHM", length = 4000)
	private String smqAlgorithm;

	@Column(name = "DICTIONARY_VERSION", nullable = false, length = 5)
	private String dictionaryVersion;

	public Long getId() {
		return smqId;
	}

	public void setId(Long smqId) {
		this.smqId = smqId;
	}

	public Long getSmqId() {
		return smqId;
	}

	public void setSmqId(Long smqId) {
		this.smqId = smqId;
	}

	public Long getSmqCode() {
		return smqCode;
	}

	public void setSmqCode(Long smqCode) {
		this.smqCode = smqCode;
	}

	public String getSmqName() {
		return smqName;
	}

	public void setSmqName(String smqName) {
		this.smqName = smqName;
	}

	public Long getSmqParentCode() {
		return smqParentCode;
	}

	public void setSmqParentCode(Long smqParentCode) {
		this.smqParentCode = smqParentCode;
	}

	public String getSmqParentName() {
		return smqParentName;
	}

	public void setSmqParentName(String smqParentName) {
		this.smqParentName = smqParentName;
	}

	public Integer getSmqLevel() {
		return smqLevel;
	}

	public void setSmqLevel(Integer smqLevel) {
		this.smqLevel = smqLevel;
	}

	public String getSmqDescription() {
		return smqDescription;
	}

	public void setSmqDescription(String smqDescription) {
		this.smqDescription = smqDescription;
	}

	public String getSmqSource() {
		return smqSource;
	}

	public void setSmqSource(String smqSource) {
		this.smqSource = smqSource;
	}

	public String getSmqNote() {
		return smqNote;
	}

	public void setSmqNote(String smqNote) {
		this.smqNote = smqNote;
	}

	public String getSmqStatus() {
		return smqStatus;
	}

	public void setSmqStatus(String smqStatus) {
		this.smqStatus = smqStatus;
	}

	public String getSmqAlgorithm() {
		return smqAlgorithm;
	}

	public void setSmqAlgorithm(String smqAlgorithm) {
		this.smqAlgorithm = smqAlgorithm;
	}

	public String getDictionaryVersion() {
		return dictionaryVersion;
	}

	public void setDictionaryVersion(String dictionaryVersion) {
		this.dictionaryVersion = dictionaryVersion;
	}

}