package com.dbms.entity.cqt;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import com.dbms.entity.AuditableEntity;

/**
 * @date Feb 6, 2017 5:54:27 PM
 **/
@Entity
@Table(name = "CQT_USER")
@SequenceGenerator(name = "ID_GENERATOR", sequenceName = "CQT_USER_SEQ")
public class User extends AuditableEntity {

	private static final long serialVersionUID = 1L;

	public User() {
		super();
	}

	@Column(name = "NAME")
	private String name;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

}
