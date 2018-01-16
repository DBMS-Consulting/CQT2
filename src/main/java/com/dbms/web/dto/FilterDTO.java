package com.dbms.web.dto;

public class FilterDTO {
	
	private boolean all;
	private boolean newSuccessorPT;
	private boolean nonCurrentLLT;
	private boolean lltPromotedToPT;
	
	
	public FilterDTO() {
		this.all = false;
		this.newSuccessorPT = false;
		this.nonCurrentLLT = false;
		this.lltPromotedToPT = false;
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
	
	
	
	
	
}
