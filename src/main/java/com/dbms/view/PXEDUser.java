package com.dbms.view;

import java.io.Serializable;
import org.apache.commons.lang3.StringUtils;

public class PXEDUser implements Serializable {

	private static final long serialVersionUID = 7748925914287753306L;

	private String userName;
	private String firstName;
	private String lastName;

	public PXEDUser() {
	}

	public PXEDUser(String userName, String fname, String lname) {
		this.userName = userName;
		if (fname == null) {
			this.firstName = "";
		} else {
			this.firstName = fname;
		}
		if (lname == null) {
			this.lastName = "";
		} else {
			this.lastName = lname;
		}
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getFirstName() {
		return firstName;
	}

	public void setFirstName(String firstName) {
		if (firstName == null) {
			this.firstName = "";
		} else {
			this.firstName = firstName;
		}
	}

	public String getLastName() {
		return lastName;
	}

	public void setLastName(String lastName) {
		if (lastName == null) {
			this.lastName = "";
		} else {
			this.lastName = lastName;
		}
	}

	public String getFullName() {
		if (StringUtils.isBlank(lastName)) {
			if (StringUtils.isBlank(firstName))
				return userName;
			else
				return firstName;
		} else {
			if (StringUtils.isBlank(firstName))
				return lastName;
			else
				return firstName + " " + lastName;
		}
	}

	@Override
	public boolean equals(Object e) {
		if (e instanceof PXEDUser) {
			if (this.userName == null)
				return ((PXEDUser) e).userName == null;
			else
				return this.userName.equals(((PXEDUser) e).userName);
		}
		return false;
	}

}
