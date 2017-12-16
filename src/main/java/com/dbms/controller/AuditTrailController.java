package com.dbms.controller;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.ViewScoped;

import org.primefaces.model.StreamedContent;

import com.dbms.entity.AuditableEntity;
import com.dbms.entity.cqt.CmqBase190;
import com.dbms.entity.cqt.dtos.AuditTrailDto;
import com.dbms.service.IAuditTrailService;
import com.dbms.service.ISmqBaseService;

 


@ManagedBean
@ViewScoped
public class AuditTrailController implements Serializable {

 

	/**
	 * serialVersionUID.
	 */
	private static final long serialVersionUID = 2630718574599045932L;
	
	private String date, dictionary, state;
	private String listName, listCode;
	private StreamedContent excelFile;
	private List<AuditTrailDto> datas;
	
	@ManagedProperty("#{AuditTrailService}")
	private IAuditTrailService auditTrailService;
	
	@PostConstruct
	public void init() {
		 
	}
	
	public void generateExcel(List<AuditableEntity> list) {
		
	}
	
	public String reset() {
		this.datas = new ArrayList<AuditTrailDto>();
		return "";
	}
	
	public void findAudit() {
		datas = this.auditTrailService.findByCriterias(listName, Long.valueOf(listCode), Integer.valueOf(dictionary), date);
		
	}

	public String getDate() {
		return date;
	}

	public void setDate(String date) {
		this.date = date;
	}

	public String getListName() {
		return listName;
	}

	public void setListName(String listName) {
		this.listName = listName;
	}

	public String getListCode() {
		return listCode;
	}

	public void setListCode(String listCode) {
		this.listCode = listCode;
	}

	public String getDictionary() {
		return dictionary;
	}

	public void setDictionary(String dictionary) {
		this.dictionary = dictionary;
	}

	public String getState() {
		return state;
	}

	public void setState(String state) {
		this.state = state;
	}

	public List<AuditTrailDto> getDatas() {
		return datas;
	}

	public void setDatas(List<AuditTrailDto> datas) {
		this.datas = datas;
	}

	public StreamedContent getExcelFile() {
		return excelFile;
	}

	public void setExcelFile(StreamedContent excelFile) {
		this.excelFile = excelFile;
	}

	public IAuditTrailService getAuditTrailService() {
		return auditTrailService;
	}

	public void setAuditTrailService(IAuditTrailService auditTrailService) {
		this.auditTrailService = auditTrailService;
	}

	

}
