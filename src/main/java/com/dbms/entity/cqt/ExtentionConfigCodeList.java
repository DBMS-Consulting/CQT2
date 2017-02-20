package com.dbms.entity.cqt;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.hibernate.annotations.GenericGenerator;

import com.dbms.entity.BaseEntity;

@Entity
@Table(name = "EXTENTION_CONFIG_CODELIST")
public class ExtentionConfigCodeList extends BaseEntity {

	private static final long serialVersionUID = -6991254386386614648L;

	@Id
	@GeneratedValue(generator = "idGenerator")
	@GenericGenerator(name = "idGenerator", strategy = "assigned")
	@Column(name = "CODELIST_ID", length = 10, unique = true, nullable = false)
	private Long id;

	@Column(name = "DISPLAY_SN", length = 4)
	private Integer displaySN;

	@Column(name = "CODELIST_CONFIGURATION_TYPE", length = 30, nullable = false)
	private String type;

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

	public Integer getDisplaySN() {
		return displaySN;
	}

	public void setDisplaySN(Integer displaySN) {
		this.displaySN = displaySN;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
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
