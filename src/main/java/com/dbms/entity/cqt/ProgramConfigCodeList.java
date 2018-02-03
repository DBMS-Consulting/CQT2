package com.dbms.entity.cqt;

import java.util.Date;

import javax.persistence.Cacheable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.GenericGenerator;

import com.dbms.entity.BaseEntity;

/**
 * @author Jay G.(jayshanchn@hotmail.com)
 * @date Feb 11, 2017 6:39:33 AM
 **/
@Entity
@Cacheable
@Cache(usage=CacheConcurrencyStrategy.READ_WRITE)
@Table(name="PROGRAM_CONFIG_CODELIST")
public class ProgramConfigCodeList extends BaseEntity {

	@Id
	@GeneratedValue(generator = "idGenerator")    
	@GenericGenerator(name = "idGenerator", strategy = "assigned") 
	@Column(name = "CODELIST_ID",length=10,unique=true,nullable=false)
	private Long id;
	@Column(name="DISPLAY_SN",length=4)
	private Integer displaySN;
	@Column(name="CODELIST_CONFIGURATION_TYPE",length=30,nullable=false)
	private String type;
	@Column(name="CODELIST_VALUE",length=200,nullable=false)
	private String value;
	@Column(name="DEFAULT_FLAG",length=1)
	private String defaultFlag;
	@Column(name="ACTIVE_FLAG",length=1,nullable=false)
	private String activeFlag;
	@Column(name="CREATED_BY",length=30,nullable=false)
	private String createdBy;
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name="CREATION_DATE",nullable=false)
	private Date creationDate;
	@Column(name="LAST_MODIFIED_BY",length=30)
	private String lastModifiedBy;
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name="LAST_MODIFICATION_DATE")
	private Date lastModificationDate;
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	

}

