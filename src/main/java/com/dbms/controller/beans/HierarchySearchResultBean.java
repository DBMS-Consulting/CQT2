package com.dbms.controller.beans;

import com.dbms.entity.BaseEntity;

public class HierarchySearchResultBean {
	private BaseEntity entity;
	private String level;
	private String term;
	private String code;

	public BaseEntity getEntity() {
		return entity;
	}

	public void setEntity(BaseEntity entity) {
		this.entity = entity;
	}

	public String getLevel() {
		return level;
	}

	public void setLevel(String level) {
		this.level = level;
	}

	public String getTerm() {
		return term;
	}

	public void setTerm(String term) {
		this.term = term;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

}
