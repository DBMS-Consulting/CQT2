package com.dbms.controller;

 
import java.io.Serializable;

import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.component.html.HtmlInputHidden;
import javax.faces.context.FacesContext;
 

@ManagedBean
@ViewScoped
public class ApplicationController implements Serializable {
	
	//private boolean formOpened;
	private HtmlInputHidden form;
	
	@PostConstruct
	public void init() {
		// System.out.println("\n  ---- FORM ApplicationController: " + formOpened);
	}
	
	public String openForm() {
		if (form == null || (form != null && form.getValue() == null))
			return "";
		String formToOpen = (String) form.getValue();
		System.out.println("\n  ---- FORM ApplicationController: " + formToOpen);
		return formToOpen;
	}	
	
	public String redirectHome() {
		return "index.xhtml?faces-redirect=true";
	}	
	

	public HtmlInputHidden getForm() {
		return form;
	}

	public void setForm(HtmlInputHidden form) {
		this.form = form;
	}

	 
	 public void onIdle() {
	        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_WARN, 
	                                        "No activity.", "What are you doing over there?"));
	        
	        
	    }
	 
	    public void onActive() {
	        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_WARN,
	                                        "Welcome Back", "Well, that's a long coffee break!"));
	    }
	
}
