package com.dbms.entity.cqt.dtos;

import com.dbms.entity.IEntity;

public class MeddraDictHierarchySearchDto implements IEntity {

	private static final long serialVersionUID = -1547764018697656790L;

	private Long meddraDictId;

	private String term;

	private String code;

	public Long getId() {
		return meddraDictId;
	}

	public void setId(Long meddraDictId) {
		this.meddraDictId = meddraDictId;
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
