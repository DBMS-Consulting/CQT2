package com.dbms.entity;


/**
 * @author Jay G.(jayshanchn@hotmail.com)
 * @date Feb 9, 2017 8:03:20 PM
 **/
public interface IAuditable {
	public Auditable getAuditable();
	public void updateAuditable(String user);
}
