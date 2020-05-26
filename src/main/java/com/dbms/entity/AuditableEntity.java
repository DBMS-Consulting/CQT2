package com.dbms.entity;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.MappedSuperclass;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import com.dbms.entity.cqt.User;

@MappedSuperclass
public abstract class AuditableEntity extends BaseEntity {

	private static final long serialVersionUID = 1L;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "CREATED", nullable = false, updatable = false)
	private Date created;
	
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="CREATOR_ID")
	private User creator;

	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="ACTIVATOR_ID")
	private User activator;
	
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "ACTIVATED")
	private Date activated;

	public AuditableEntity() {
		this.created = new Date();
	}

	//
	public void updateEntity() {
		this.activated = new Date();
		if (this.created == null) {
			this.created = this.activated;
		}
	}

	public Date getCreated() {
		return created;
	}

	public void setCreated(Date created) {
		this.created = created;
	}

	public User getCreator() {
		return creator;
	}

	public void setCreator(User creator) {
		this.creator = creator;
	}

	public User getActivator() {
		return activator;
	}

	public void setActivator(User activator) {
		this.activator = activator;
	}

	public Date getActivated() {
		return activated;
	}

	public void setActivated(Date activated) {
		this.activated = activated;
	}

}