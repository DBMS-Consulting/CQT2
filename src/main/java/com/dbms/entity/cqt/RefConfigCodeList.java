package com.dbms.entity.cqt;

import java.math.BigDecimal;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import com.dbms.entity.BaseEntity;

@Entity
@Cache(usage=CacheConcurrencyStrategy.READ_WRITE)
@Table(name = "REF_CONFIG_CODELIST")
public class RefConfigCodeList extends BaseEntity {

	private static final long serialVersionUID = -6991254386386614648L;

	@Id
	@GeneratedValue(generator = "CODELIST_ID_SEQ", strategy = GenerationType.SEQUENCE)
	@SequenceGenerator(name = "CODELIST_ID_SEQ", sequenceName = "CMQ_ID_SEQ", allocationSize = 1)
	@Column(name = "CODELIST_ID", length = 30, unique = true, nullable = false)
	private Long id;

	@Column(name = "CODELIST_CONFIGURATION_TYPE", length = 30, nullable = false)
	private String codelistConfigType;

	@Column(name = "SERIALNUM", nullable = false, precision = 4, scale = 0)
	private BigDecimal serialNum;

	@Column(name = "CODELIST_INTERNAL_VALUE", length = 200, nullable = false)
	private String codelistInternalValue;

	@Column(name = "CODELIST_VALUE", length = 200, nullable = false)
	private String value;

	@Column(name = "DEFAULT_FLAG", length = 1)
	private String defaultFlag;

	@Column(name = "ACTIVE_FLAG", length = 1, nullable = false)
	private String activeFlag;

	@Column(name = "CREATED_BY", length = 30, nullable = false)
	private String createdBy;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "CREATION_DATE", nullable = false)
	private Date creationDate;

	@Column(name = "LAST_MODIFIED_BY", length = 30)
	private String lastModifiedBy;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "LAST_MODIFICATION_DATE")
	private Date lastModificationDate;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getCodelistConfigType() {
		return codelistConfigType;
	}

	public void setCodelistConfigType(String codelistConfigType) {
		this.codelistConfigType = codelistConfigType;
	}

	public BigDecimal getSerialNum() {
		return serialNum;
	}

	public void setSerialNum(BigDecimal serialNum) {
		this.serialNum = serialNum;
	}

	public String getCodelistInternalValue() {
		return codelistInternalValue;
	}

	public void setCodelistInternalValue(String codelistInternalValue) {
		this.codelistInternalValue = codelistInternalValue;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public String getDefaultFlag() {
		return defaultFlag;
	}

	public void setDefaultFlag(String defaultFlag) {
		this.defaultFlag = defaultFlag;
	}

	public String getActiveFlag() {
		return activeFlag;
	}

	public void setActiveFlag(String activeFlag) {
		this.activeFlag = activeFlag;
	}

	public String getCreatedBy() {
		return createdBy;
	}

	public void setCreatedBy(String createdBy) {
		this.createdBy = createdBy;
	}

	public Date getCreationDate() {
		return creationDate;
	}

	public void setCreationDate(Date creationDate) {
		this.creationDate = creationDate;
	}

	public String getLastModifiedBy() {
		return lastModifiedBy;
	}

	public void setLastModifiedBy(String lastModifiedBy) {
		this.lastModifiedBy = lastModifiedBy;
	}

	public Date getLastModificationDate() {
		return lastModificationDate;
	}

	public void setLastModificationDate(Date lastModificationDate) {
		this.lastModificationDate = lastModificationDate;
	}

}
