package com.dbms.view;

import java.util.Date;

import org.primefaces.event.SelectEvent;

import com.dbms.entity.cqt.CmqBase190;

/**
 * "Create/Update/Browse&Search" module / "Confirm" tab / "Workflow" section's form data 
 * 
 * @author Andrius Mielkus(andrius.mielkus@yandex.com)
 *
 */
public class ListWorkflowFormModel {
	private String requestReason;
	private Date dueDate;
	private String approvalReason;
	
	public ListWorkflowFormModel() {
		init();
	}
	
	public void init() {
		this.requestReason = "";
		this.dueDate = null;
		this.approvalReason = "";
	}
	
	public void loadFromCmqBase190(CmqBase190 cmq) {
		this.dueDate = cmq.getCmqDueDate();
	}
	
	public void saveToCmqBase190(CmqBase190 cmq) {
		cmq.setCmqDueDate(this.dueDate);
	}
	
	public void onDueDateSelect(SelectEvent event) {
		if(event.getObject() instanceof Date)
			setDueDate((Date)event.getObject());
	}
	
	//---------------- Getters & Setters -------------------------
	public String getRequestReason() {
		return requestReason;
	}

	public void setRequestReason(String requestReason) {
		this.requestReason = requestReason;
	}

	public Date getDueDate() {
		return dueDate;
	}

	public void setDueDate(Date dueDate) {
		this.dueDate = dueDate;
	}

	public String getApprovalReason() {
		return approvalReason;
	}

	public void setApprovalReason(String approvalReason) {
		this.approvalReason = approvalReason;
	}
}
