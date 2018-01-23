package com.dbms.web.dto;

public class FilterDTO {
	
	private boolean all;
	private boolean newSuccessorPT;
	private boolean nonCurrentLLT;
	private boolean lltPromotedToPT;
	private boolean medDraTermNameChanged;
	private boolean ptDemotedToLLT;
	private boolean mergedHLGT;
	private boolean primarySOCChange;
	private boolean newTermAdded;
	private boolean termMoved;
	private boolean scopechanged;
	private boolean termDeleted;
	private boolean statusChanged;
	private boolean impactedSMQ;
	
	
	public FilterDTO() {
		this.all = true;
		this.newSuccessorPT = false;
		this.nonCurrentLLT = false;
		this.lltPromotedToPT = false;
		this.medDraTermNameChanged = false;
		this.ptDemotedToLLT = false;
		this.mergedHLGT = false;
		this.primarySOCChange = false;
		this.newTermAdded = false;
		this.termMoved = false;
		this.scopechanged = false;
		this.termDeleted = false;
		this.statusChanged = false;
		this.impactedSMQ = false;
	}
	
	public void resetAll() {
		this.newSuccessorPT = false;
		this.nonCurrentLLT = false;
		this.lltPromotedToPT = false;
		this.medDraTermNameChanged = false;
		this.ptDemotedToLLT = false;
		this.mergedHLGT = false;
		this.primarySOCChange = false;
		this.newTermAdded = false;
		this.termMoved = false;
		this.scopechanged = false;
		this.termDeleted = false;
		this.statusChanged = false;
		this.impactedSMQ = false;
	}

	public boolean isAll() {
		return all;
	}
	public void setAll(boolean all) {
		this.all = all;
	}
	public boolean isNewSuccessorPT() {
		return newSuccessorPT;
	}
	public void setNewSuccessorPT(boolean newSuccessorPT) {
		this.newSuccessorPT = newSuccessorPT;
	}
	public boolean isNonCurrentLLT() {
		return nonCurrentLLT;
	}
	public void setNonCurrentLLT(boolean nonCurrentLLT) {
		this.nonCurrentLLT = nonCurrentLLT;
	}
	public boolean isLltPromotedToPT() {
		return lltPromotedToPT;
	}
	public void setLltPromotedToPT(boolean lltPromotedToPT) {
		this.lltPromotedToPT = lltPromotedToPT;
	}

	public boolean isMedDraTermNameChanged() {
		return medDraTermNameChanged;
	}

	public void setMedDraTermNameChanged(boolean medDraTermNameChanged) {
		this.medDraTermNameChanged = medDraTermNameChanged;
	}

	public boolean isPtDemotedToLLT() {
		return ptDemotedToLLT;
	}

	public void setPtDemotedToLLT(boolean ptDemotedToLLT) {
		this.ptDemotedToLLT = ptDemotedToLLT;
	}

	public boolean isMergedHLGT() {
		return mergedHLGT;
	}

	public void setMergedHLGT(boolean mergedHLGT) {
		this.mergedHLGT = mergedHLGT;
	}

	public boolean isPrimarySOCChange() {
		return primarySOCChange;
	}

	public void setPrimarySOCChange(boolean primarySOCChange) {
		this.primarySOCChange = primarySOCChange;
	}

	public boolean isNewTermAdded() {
		return newTermAdded;
	}

	public void setNewTermAdded(boolean newTermAdded) {
		this.newTermAdded = newTermAdded;
	}

	public boolean isTermMoved() {
		return termMoved;
	}

	public void setTermMoved(boolean termMoved) {
		this.termMoved = termMoved;
	}

	public boolean isScopechanged() {
		return scopechanged;
	}

	public void setScopechanged(boolean scopechanged) {
		this.scopechanged = scopechanged;
	}

	public boolean isTermDeleted() {
		return termDeleted;
	}

	public void setTermDeleted(boolean termDeleted) {
		this.termDeleted = termDeleted;
	}

	public boolean isStatusChanged() {
		return statusChanged;
	}

	public void setStatusChanged(boolean statusChanged) {
		this.statusChanged = statusChanged;
	}

	public boolean isImpactedSMQ() {
		return impactedSMQ;
	}

	public void setImpactedSMQ(boolean impactedSMQ) {
		this.impactedSMQ = impactedSMQ;
	}
	
	
	
	
	
}
