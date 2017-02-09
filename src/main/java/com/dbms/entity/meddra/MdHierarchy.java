package com.dbms.entity.meddra;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

/**
 * @author Jay G.(jayshanchn@hotmail.com)
 * @date Feb 9, 2017 3:13:37 AM
 **/
@Entity
@Table(name = "CQT_MD_HIERARCHY", indexes = {
		@Index(name = "IX1_MD_HIER01", columnList = "PT_CODE"),
		@Index(name = "IX1_MD_HIER02", columnList = "HLT_CODE"),
		@Index(name = "IX1_MD_HIER03", columnList = "HLGT_CODE"),
		@Index(name = "IX1_MD_HIER04", columnList = "SOC_CODE"),
		@Index(name = "IX1_MD_HIER05", columnList = "PT_SOC_CODE")})
@SequenceGenerator(name = "ID_GENERATOR", sequenceName = "SEQ_MD_HIERARCHY")
public class MdHierarchy implements Serializable {

	private static final long serialVersionUID = -520733295843058594L;
	
	@Id
	@GeneratedValue(generator = "ID_GENERATOR", strategy = GenerationType.AUTO)
	@Column(name="ID")
	private Long id;

	@Column(name="PT_NAME",length=100,nullable=false)
	private String ptName;
	@Column(name="HLT_NAME",length=100,nullable=false)
	private String hltName;
	@Column(name="HLGT_NAME",length=100,nullable=false)
	private String hlgtName;
	@Column(name="SOC_NAME",length=100,nullable=false)
	private String socName;
	@Column(name="SOC_ABBREV",length=5,nullable=false)
	private String socAbbrev;
	@Column(name="NULL_FIELD",length=1)
	private String nullField;
	@Column(name="PT_SOC_CODE")
	private Long ptSocCode;
	@Column(name="PRIMARY_SOC_FG",length=1)
	private String primarySocFg;
	@ManyToOne
	@JoinColumn(name="SOC_CODE")
	private PrefTerm prefTerm;
	@ManyToOne
	@JoinColumn(name="HLT_CODE")
	private HltPrefTerm hltPrefTerm;
	@ManyToOne
	@JoinColumn(name="HLGT_CODE")
	private HlgtPrefTerm hlgtPrefTerm;
	@ManyToOne
	@JoinColumn(name="PT_CODE")
	private SocTerm socTerm;
	
	public String getPtName() {
		return ptName;
	}
	public void setPtName(String ptName) {
		this.ptName = ptName;
	}
	public String getHltName() {
		return hltName;
	}
	public void setHltName(String hltName) {
		this.hltName = hltName;
	}
	public String getHlgtName() {
		return hlgtName;
	}
	public void setHlgtName(String hlgtName) {
		this.hlgtName = hlgtName;
	}
	public String getSocName() {
		return socName;
	}
	public void setSocName(String socName) {
		this.socName = socName;
	}
	public String getSocAbbrev() {
		return socAbbrev;
	}
	public void setSocAbbrev(String socAbbrev) {
		this.socAbbrev = socAbbrev;
	}
	public String getNullField() {
		return nullField;
	}
	public void setNullField(String nullField) {
		this.nullField = nullField;
	}
	public Long getPtSocCode() {
		return ptSocCode;
	}
	public void setPtSocCode(Long ptSocCode) {
		this.ptSocCode = ptSocCode;
	}
	public String getPrimarySocFg() {
		return primarySocFg;
	}
	public void setPrimarySocFg(String primarySocFg) {
		this.primarySocFg = primarySocFg;
	}
	public PrefTerm getPrefTerm() {
		return prefTerm;
	}
	public void setPrefTerm(PrefTerm prefTerm) {
		this.prefTerm = prefTerm;
	}
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public HltPrefTerm getHltPrefTerm() {
		return hltPrefTerm;
	}
	public void setHltPrefTerm(HltPrefTerm hltPrefTerm) {
		this.hltPrefTerm = hltPrefTerm;
	}
	public HlgtPrefTerm getHlgtPrefTerm() {
		return hlgtPrefTerm;
	}
	public void setHlgtPrefTerm(HlgtPrefTerm hlgtPrefTerm) {
		this.hlgtPrefTerm = hlgtPrefTerm;
	}
	public SocTerm getSocTerm() {
		return socTerm;
	}
	public void setSocTerm(SocTerm socTerm) {
		this.socTerm = socTerm;
	}

}

