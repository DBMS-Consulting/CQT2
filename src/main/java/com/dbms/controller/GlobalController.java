package com.dbms.controller;

import java.io.Serializable;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.event.AjaxBehaviorEvent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
 
@ManagedBean
@SessionScoped
public class GlobalController implements Serializable {

	/**
	 * serialVersionUID.
	 */
	private static final long serialVersionUID = 2630718574599045932L;
	private static final Logger LOG = LoggerFactory.getLogger(GlobalController.class);
	private boolean filterLltsFlag;
	
 	private boolean myFlag;
	
	/**
	 * Event listener when checking the flag.
	 * @param event
	 */
	public void checkFlag(AjaxBehaviorEvent event) {
 		System.out.println("------ Event flag : " + myFlag);
	}
	
	public void checkFilterLltsFlag(AjaxBehaviorEvent event) {
		LOG.info("filterLltsFlag status is " + filterLltsFlag);
	}

	public boolean isMyFlag() {
		return myFlag;
	}


	public void setMyFlag(boolean myFlag) {
		this.myFlag = myFlag;
	}


	public boolean isFilterLltsFlag() {
		return filterLltsFlag;
	}


	public void setFilterLltsFlag(boolean filterLltsFlag) {
		this.filterLltsFlag = filterLltsFlag;
	}
	
	 
}
