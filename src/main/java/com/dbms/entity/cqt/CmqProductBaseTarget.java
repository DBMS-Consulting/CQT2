package com.dbms.entity.cqt;

import javax.persistence.Cacheable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import com.dbms.entity.BaseEntity;

@Entity
@Cacheable
@Cache(usage=CacheConcurrencyStrategy.READ_WRITE)
@Table(name = "CMQ_PRODUCT_BASE_TARGET")
public class CmqProductBaseTarget extends BaseEntity {

	private static final long serialVersionUID = 857067504356223656L;

	@Id
	@GeneratedValue(generator = "CMQ_PRODUCT_ID_SEQ", strategy = GenerationType.SEQUENCE)
	@SequenceGenerator(name = "CMQ_PRODUCT_ID_SEQ", sequenceName = "CMQ_PRODUCT_ID_SEQ", allocationSize = 1)
	@Column(name = "CMQ_PRODUCT_ID", unique = true, nullable = false)
	private Long cmqProductId;

	@Column(name = "CMQ_CODE", unique = true, nullable = false, precision = 38)
	private Long cmqCode;

	@Column(name = "CMQ_NAME", nullable = false, length = 200)
	private String cmqName;

	@Column(name = "CMQ_PRODUCT_CD", nullable = false, length = 200)
	private String cmqProductCd;

	@ManyToOne
	@JoinColumn(name = "cmq_id")
	private CmqBaseTarget cmqBaseTarget;

	@Override
	public Long getId() {
		return this.cmqProductId;
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

	public CmqBaseTarget getCmqBaseTarget() {
		return cmqBaseTarget;
	}

	public void setCmqBaseTarget(CmqBaseTarget cmqBaseTarget) {
		this.cmqBaseTarget = cmqBaseTarget;
	}

	public void setCmqProductId(Long cmqProductId) {
		this.cmqProductId = cmqProductId;
	}

	public String getCmqProductCd() {
		return cmqProductCd;
	}

	public void setCmqProductCd(String cmqProductCd) {
		this.cmqProductCd = cmqProductCd;
	}

}