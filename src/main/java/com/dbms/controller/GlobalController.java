package com.dbms.controller;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import javax.enterprise.inject.spi.Bean;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.context.FacesContext;
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
 	
 	private String timezone;
	
	/**
	 * Event listener when checking the flag.
	 * @param event
	 */
	public void checkFlag(AjaxBehaviorEvent event) {
 		System.out.println("------ Event flag : " + myFlag);
	}
	
	public void checkFilterLltsFlag(AjaxBehaviorEvent event) {
		LOG.info("filterLltsFlag status is " + filterLltsFlag);
		FacesContext context = FacesContext.getCurrentInstance();
		CreateController createController = context.getApplication().evaluateExpressionGet(context, "#{createController}", CreateController.class);
		if(null != createController) {
			createController.reloadRelationsOnFilterLltFlagToggle();
		}
		
		ImpactSearchController impactSearchController = context.getApplication().evaluateExpressionGet(context, "#{impactSearchController}", ImpactSearchController.class);
		if(null != impactSearchController) {
			impactSearchController.reloadRelationsOnFilterLltFlagToggle();
		}
	}
	
	public void timezoneSwitch(AjaxBehaviorEvent event) {
 		System.out.println("*** " + timezone);		
		TimeZone.setDefault(TimeZone.getTimeZone(timezone));

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

	public String getTimezone() {
		return timezone;
	}

	public void setTimezone(String timezone) {
		this.timezone = timezone;
	}
	
	
	
	 
}
