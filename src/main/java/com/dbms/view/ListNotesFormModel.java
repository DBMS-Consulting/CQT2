package com.dbms.view;

import com.dbms.entity.cqt.CmqBase190;

/**
 * "Create/Update/Browse&Search" module's "Informative Notes" tab form data 
 * 
 * @author Andrius Mielkus(andrius.mielkus@yandex.com)
 *
 */
public class ListNotesFormModel {
	private final String emptyDescription = "Please enter the description";
	
	private boolean modelChanged = false;
	
	private String description;
	private String notes;
	private String source;
	
	public ListNotesFormModel() {
		init();
	}
	
	public void init() {
		this.description = emptyDescription;
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
		this.modelChanged = false;
	}
	
	/**
	 * Save Form data to CmqBase190 Entity
	 * @param cmq
	 */
	public void saveToCmqBase190(CmqBase190 cmq) {
		if(this.description == null || "".equals(this.description))
			cmq.setCmqDescription(emptyDescription);
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
	public void setModelChanged(boolean notesFormChanged) {
		this.modelChanged = notesFormChanged;
	}
	
	public String getDescription() {
		return (emptyDescription.equals(this.description) || this.description == null) ? "" : this.description;
	}

	public void setDescription(String description) {
		if(!this.getDescription().equals(description))
			this.modelChanged = true;
		this.description = description;
	}

	public String getNotes() {
		return this.notes == null ? "" : this.notes;
	}

	public void setNotes(String notes) {
		if(!this.getNotes().equals(notes))
			this.modelChanged = true;
		this.notes = notes;
	}

	public String getSource() {
		return this.source == null ? "" : this.source;
	}

	public void setSource(String source) {
		if(!this.getSource().equals(source))
			this.modelChanged = true;
		this.source = source;
	}
}