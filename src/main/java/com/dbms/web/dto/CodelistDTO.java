package com.dbms.web.dto;

import java.io.Serializable;

public class CodelistDTO implements Serializable {

	/**
	 * serialVersionUID.
	 */
	private static final long serialVersionUID = 8524545604298122597L;

	private String sequence;
	private String name;
	private boolean activeValue, defaultValue;
	private String codelistType;

	public String getSequence() {
		return sequence;
	}

	public void setSequence(String sequence) {
		this.sequence = sequence;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getCodelistType() {
		return codelistType;
	}

	public void setCodelistType(String codelistType) {
		this.codelistType = codelistType;
	}

	public boolean getActiveValue() {
		return activeValue;
	}

	public void setActiveValue(boolean activeValue) {
		this.activeValue = activeValue;
	}

	public boolean getDefaultValue() {
		return defaultValue;
	}

	public void setDefaultValue(boolean defaultValue) {
		this.defaultValue = defaultValue;
	}
}
