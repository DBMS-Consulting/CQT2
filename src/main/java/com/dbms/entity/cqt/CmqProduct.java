package com.dbms.entity.cqt;

import java.math.BigDecimal;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.apache.commons.lang.StringUtils;

import com.dbms.entity.BaseEntity;

@Entity
@Table(name = "CMQ_PRODUCT_BASE_CURRENT")
public class CmqProduct extends BaseEntity {

	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(generator = "CMQ_ID_SEQ", strategy = GenerationType.SEQUENCE)
	@SequenceGenerator(name = "CMQ_ID_SEQ", sequenceName = "CMQ_ID_SEQ", allocationSize = 1)
	@Column(name = "CMQ_PRODUCT_ID", unique = true, nullable = false)
	private Long cmqProductId;
	
	@Column(name = "CMQ_ID", nullable = false, insertable = false, updatable = false)
	private Long cmqId;
	
	@Column(name = "CMQ_CODE", unique = true, nullable = false, precision = 38)
	private Long cmqCode;

	@Column(name = "CMQ_NAME", nullable = false, length = 200)
	private String cmqName;

	@Column(name = "CMQ_PRODUCT_CD", nullable = false, length = 200)
	private String cmqProductCd;
	
	@ManyToOne
    @JoinColumn(name="CMQ_ID", nullable=false)
	private CmqBase190 cmqBase;
	

	public Long getId() {
		return cmqProductId;
	}

	public void setId(Long cmqProductId) {
		this.cmqProductId = cmqProductId;
	}
	
	public Long getCmqId() {
		return cmqId;
	}
	
	public void setCmqId(Long cmqId) {
		this.cmqId = cmqId;
	}

	public Long getCmqCode() {
		return cmqCode;
	}

	public void setCmqCode(Long cmqCode) {
		this.cmqCode = cmqCode;
	}

	public String getCmqName() {
		return cmqName;
	}

	public void setCmqName(String cmqName) {
		this.cmqName = cmqName;
	}

	public String getCmqProductCd() {
		return cmqProductCd;
	}

	public void setCmqProductCd(String cmqProductCd) {
		this.cmqProductCd = cmqProductCd;
	}

	public CmqBase190 getCmqBase() {
		return cmqBase;
	}

	public void setCmqBase(CmqBase190 cmqBase) {
		this.cmqBase = cmqBase;
		if(cmqBase != null) {
			this.setCmqName(cmqBase.getCmqName());
			this.setCmqCode(cmqBase.getCmqCode());
		}
	}
	
	
}