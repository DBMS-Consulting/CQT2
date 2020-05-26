package com.dbms.entity;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

@Embeddable
public class Auditable implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 7008289767926035355L;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "CREATION_DATE")
	private Date creationDate;

	@Column(name = "CREATED_BY", length = 100)
	private String createdBy;

	public Auditable() {
	}

	public Auditable(String createdBy) {
		this.createdBy = createdBy;
		this.creationDate = new Date();
	}

	public Date getCreationDate() {
		return creationDate;
	}

	public void setCreationDate(Date creationDate) {
		this.creationDate = creationDate;
	}

	public String getCreatedBy() {
		return createdBy;
	}

	public void setCreatedBy(String createdBy) {
		this.createdBy = createdBy;
	}

}