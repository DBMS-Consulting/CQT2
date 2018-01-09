package com.dbms.controller;

import java.io.Serializable;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.event.AjaxBehaviorEvent;
 
@ManagedBean
@SessionScoped
public class GlobalController implements Serializable {

 

	/**
	 * serialVersionUID.
	 */
	private static final long serialVersionUID = 2630718574599045932L;
	
	 
 	private boolean myFlag;
	
	
	/**
	 * Event listener when checking the flag.
	 * @param event
	 */
	public void checkFlag(AjaxBehaviorEvent event) {
 		System.out.println("------ Event flag : " + myFlag);
	}


	public boolean isMyFlag() {
		return myFlag;
	}


	public void setMyFlag(boolean myFlag) {
		this.myFlag = myFlag;
	}
	
	 
}
