package com.dbms.view;

import java.util.Date;

import org.primefaces.event.SelectEvent;

import com.dbms.entity.cqt.CmqBase190;
import com.dbms.service.AuthenticationService;

/**
 * "Create/Update/Browse&Search" module / "Confirm" tab / "Workflow" section's form data 
 * 
 * @author Andrius Mielkus(andrius.mielkus@yandex.com)
 *
 */
public class ListWorkflowFormVM {
    private AuthenticationService authService;
    
    private String myCurState;
	private String requestReason;
	private Date dueDate;
	private String approvalReason;
    
	public ListWorkflowFormVM(AuthenticationService authService) {
        this.authService = authService;
		init();
	}
	
	public void init() {
		this.requestReason = "";
		this.dueDate = null;
		this.approvalReason = "";
	}
	
	public void loadFromCmqBase190(CmqBase190 cmq) {
        myCurState = cmq.getCmqState();
		this.dueDate = cmq.getCmqDueDate();
        this.requestReason = cmq.getCmqWfDesc();
        this.approvalReason = cmq.getCmqApproveReason();
	}
	
	public void saveToCmqBase190(CmqBase190 cmq) {
		cmq.setCmqDueDate(this.dueDate);
		cmq.setCmqWfDesc(requestReason);
		cmq.setCmqApproveReason(approvalReason);
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
    
    public Date getMinDueDate() {
        return new Date();
    }

	public String getApprovalReason() {
		return approvalReason;
	}

	public void setApprovalReason(String approvalReason) {
		this.approvalReason = approvalReason;
	}
    
    public boolean isRequestReasonDisabled() {
        if(authService.hasGroup(new String[] {AuthenticationService.REQUESTER_GROUP}))
            return true;
        return false;
    }
    
    public boolean isDueDateDisabled() {
        if(authService.hasGroup(new String[] {AuthenticationService.REQUESTER_GROUP}))
            return true;
        return false;
    }
    
    public boolean isApprovalReasonDisabled() {
        if(authService.hasGroup(new String[] {AuthenticationService.REQUESTER_GROUP}))
            return true;
        return false;
    }
}
