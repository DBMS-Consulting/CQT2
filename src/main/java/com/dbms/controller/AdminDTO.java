package com.dbms.controller;

import java.io.Serializable;

public class AdminDTO implements Serializable {

	/**
	 * serialVersionUID.
	 */
	private static final long serialVersionUID = 8524545604298122597L;

	private String sequence;

	private String name;

	private boolean active;

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

	public boolean isActive() {
		return active;
	}

	public void setActive(boolean active) {
		this.active = active;
	}
}
