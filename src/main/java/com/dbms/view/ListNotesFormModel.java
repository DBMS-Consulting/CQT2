package com.dbms.view;

import com.dbms.entity.cqt.CmqBase190;

/**
 * "Create/Update/Browse&Search" module's "Informative Notes" tab form data 
 * 
 * @author Andrius Mielkus(andrius.mielkus@yandex.com)
 *
 */
public class ListNotesFormModel {
	private boolean modelChanged = false;
	
	private String description;
	private String notes;
	private String source;
	
	public ListNotesFormModel() {
		init();
	}
	
	public void init() {
		this.description = "*** Description ****";
		this.notes = "";
		this.source = "";
	}
	
	/**
	 * Load Form data from CmqBase190 Entity
	 * @param cmq
	 */
	public void loadFromCmqBase190(CmqBase190 cmq) {
		this.description = cmq.getCmqDescription();
		this.notes = cmq.getCmqNote();
		this.source = cmq.getCmqSource();
	}
	
	/**
	 * Save Form data to CmqBase190 Entity
	 * @param cmq
	 */
	public void saveToCmqBase190(CmqBase190 cmq) {
		cmq.setCmqDescription(this.description);
		cmq.setCmqNote(this.notes);
		cmq.setCmqSource(this.source);
	}

	//---------------- Getters & Setters -------------------------
	
	/**
	 * Change/Submission status of Details form
	 * @return true if the model has been modified by user input
	 */
	public boolean isModelChanged() {
		return this.modelChanged;
	}
	public void setModelChanged(boolean detailsFormChanged) {
		this.modelChanged = detailsFormChanged;
	}
	
	public String getDescription() {
		return this.description;
	}

	public void setDescription(String description) {
		this.description = description;
		this.modelChanged = true;
	}

	public String getNotes() {
		return this.notes;
	}

	public void setNotes(String notes) {
		this.notes = notes;
		this.modelChanged = true;
	}

	public String getSource() {
		return this.source;
	}

	public void setSource(String source) {
		this.source = source;
		this.modelChanged = true;
	}
}
