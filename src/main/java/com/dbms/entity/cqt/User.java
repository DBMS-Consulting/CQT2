package com.dbms.entity.cqt;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

/**
 * @date Feb 6, 2017 5:54:27 PM
 **/
@Entity
@Table(name = "CQT_USER")
@SequenceGenerator(name = "ID_GENERATOR", sequenceName = "CQT_USER_SEQ")
public class User /*extends AuditableEntity*/ {

	@Id
	@Column(name = "ID", unique = true, nullable = false)
	private Long userId;

	@Column(name = "NAME")
	private String name;

	public User() {
		super();
	}
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

}
