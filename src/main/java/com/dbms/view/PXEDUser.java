package com.dbms.view;

import java.io.Serializable;

public class PXEDUser implements Serializable {

	private static final long serialVersionUID = 7748925914287753306L;
	
	private String userName;	
	private String firstName;
	private String lastName;
	
	
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
		this.firstName = firstName;
	}
	public String getLastName() {
		return lastName;
	}
	public void setLastName(String lastName) {
		this.lastName = lastName;
	}
    
    public String getFullName() {
        return lastName + "," + firstName;
    }
	
	@Override
    public boolean equals(Object e) {
        if(e instanceof PXEDUser) {
            if(this.userName == null)
                return ((PXEDUser) e).userName == null;
            else
                return this.userName.equals(((PXEDUser) e).userName);
        }
        return false;
    }
    
    
}
