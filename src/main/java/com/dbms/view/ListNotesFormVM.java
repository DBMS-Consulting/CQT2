package com.dbms.view;

import com.dbms.entity.cqt.CmqBase190;
import com.dbms.entity.cqt.CmqBaseTarget;
import com.dbms.entity.cqt.SmqBase190;
import com.dbms.entity.cqt.SmqBaseTarget;

/**
 * "Create/Update/Browse&Search" module's "Informative Notes" tab form data 
 * 
 * @author Andrius Mielkus(andrius.mielkus@yandex.com)
 *
 */
public class ListNotesFormVM {
	private final String emptyDescription = "Please enter the description";
	
	private boolean modelChanged = false;
	
	private String description;
	private String notes;
	private String source;
	
	public ListNotesFormVM() {
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
	
	/**
	 * Load Form data from CmqBaseTarget Entity
	 * @param cmq
	 */
	public void loadFromCmqBaseTarget(CmqBaseTarget cmq) {
		this.description = cmq.getCmqDescription();
		this.notes = cmq.getCmqNote();
		this.source = cmq.getCmqSource();
		this.modelChanged = false;
	}
	
	/**
	 * Save Form data to CmqBase190 Entity
	 * @param cmq
	 */
	public void saveToCmqBaseTarget(CmqBaseTarget cmq) {
		if(this.description == null || "".equals(this.description))
			cmq.setCmqDescription(emptyDescription);
		cmq.setCmqDescription(this.description);
		cmq.setCmqNote(this.notes);
		cmq.setCmqSource(this.source);
	}
	
	/**
	 * Load Form data from SmqBase190 Entity
	 * @param cmq
	 */
	public void loadFromSmqBase190(SmqBase190 cmq) {
		this.description = cmq.getSmqDescription();
		this.notes = cmq.getSmqNote();
		this.source = cmq.getSmqSource();
		this.modelChanged = false;
	}
	
	/**
	 * Save Form data to SmqBase190 Entity
	 * @param cmq
	 */
	public void saveToSmqBase190(SmqBase190 smq) {
		if(this.description == null || "".equals(this.description))
			smq.setSmqDescription(emptyDescription);
		smq.setSmqDescription(this.description);
		smq.setSmqNote(this.notes);
		smq.setSmqSource(this.source);
	}
	
	/**
	 * Load Form data from SmqBase190 Entity
	 * @param cmq
	 */
	public void loadFromSmqBaseTarget(SmqBaseTarget cmq) {
		this.description = cmq.getSmqDescription();
		this.notes = cmq.getSmqNote();
		this.source = cmq.getSmqSource();
		this.modelChanged = false;
	}
	
	/**
	 * Save Form data to SmqBaseTarget Entity
	 * @param cmq
	 */
	public void saveToSmqBaseTarget(SmqBaseTarget smq) {
		if(this.description == null || "".equals(this.description))
			smq.setSmqDescription(emptyDescription);
		smq.setSmqDescription(this.description);
		smq.setSmqNote(this.notes);
		smq.setSmqSource(this.source);
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
