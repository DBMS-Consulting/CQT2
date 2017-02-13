package com.dbms.controller;

import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;

import org.primefaces.event.FlowEvent;

/**
 * @date Feb 7, 2017 7:41:29 AM
 **/
@ManagedBean
@ViewScoped
public class CommonController {

	private boolean detailSave, notesSave, relationSave, confirmSave;
	
	
	public String onFlowProcessCreate(FlowEvent event) {
		if (detailSave)
		return event.getNewStep();
		
		else {
			FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_WARN, "The form is not saved yet. Proceed?", "");
	        FacesContext.getCurrentInstance().addMessage(null, msg);
		}
		return "";
	}

	public void saveDetail() {
		detailSave = true;
	}
	
	public void saveNotes() {
		notesSave = true;
	}
	
	public void saveRelation() {
		relationSave = true;
	}
	
	public void saveConfirm() {
		confirmSave = true;
	}

	public boolean isDetailSave() {
		return detailSave;
	}

	public void setDetailSave(boolean detailSave) {
		this.detailSave = detailSave;
	}

	public boolean isNotesSave() {
		return notesSave;
	}

	public void setNotesSave(boolean notesSave) {
		this.notesSave = notesSave;
	}

	public boolean isRelationSave() {
		return relationSave;
	}

	public void setRelationSave(boolean relationSave) {
		this.relationSave = relationSave;
	}

	public boolean isConfirmSave() {
		return confirmSave;
	}

	public void setConfirmSave(boolean confirmSave) {
		this.confirmSave = confirmSave;
	}

}
