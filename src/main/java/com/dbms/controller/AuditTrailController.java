package com.dbms.controller;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
import javax.faces.event.AjaxBehaviorEvent;

import org.apache.commons.lang3.StringUtils;
import org.primefaces.model.StreamedContent;

import com.dbms.entity.AuditableEntity;
import com.dbms.entity.cqt.CmqBase190;
import com.dbms.entity.cqt.RefConfigCodeList;
import com.dbms.entity.cqt.dtos.AuditTrailDto;
import com.dbms.entity.cqt.dtos.CmqBaseDTO;
import com.dbms.service.AuthenticationService;
import com.dbms.service.IAuditTrailService;
import com.dbms.service.ICmqBase190Service;
import com.dbms.service.IRefCodeListService;

 


@ManagedBean
@ViewScoped
public class AuditTrailController implements Serializable {

 

	/**
	 * serialVersionUID.
	 */
	private static final long serialVersionUID = 2630718574599045932L;
	
	private String auditTimestamp, dictionary, state;
	private String listName, listCode;
	private StreamedContent excelFile;
	private List<AuditTrailDto> datas;
	
	@ManagedProperty("#{CmqBase190Service}")
	private ICmqBase190Service cmqBaseService;
	
	@ManagedProperty("#{AuditTrailService}")
	private IAuditTrailService auditTrailService;
	
	@ManagedProperty("#{RefCodeListService}")
	private IRefCodeListService refCodeListService;
	
	@ManagedProperty("#{AuthenticationService}")
	private AuthenticationService authService;
	
	@PostConstruct
	public void init() {
		 
	}

	
	public void reset() {
		this.datas = new ArrayList<AuditTrailDto>();
		this.listName = null;
		this.listCode = null;
		this.dictionary = null;
		this.auditTimestamp = null;
 	}
	
	public String findAudit() {
		
		if(StringUtils.isBlank(listName) && StringUtils.isBlank(listCode)) {
			FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_INFO,
	                "Please select List Name or List Code", "");
	        FacesContext.getCurrentInstance().addMessage(null, msg);
	        return null;
		}
		
		if(StringUtils.isBlank(dictionary)) {
			FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_INFO,
	                "Please select Dictionary version", "");
	        FacesContext.getCurrentInstance().addMessage(null, msg);
	        return null;
		}
		
		if(StringUtils.isBlank(auditTimestamp)) {
			FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_INFO,
	                "Please select Audit timestamp", "");
	        FacesContext.getCurrentInstance().addMessage(null, msg);
	        return null;
		}
		String listCodeSelected = listCode;
		if(!StringUtils.isBlank(listName) ) {
			CmqBase190 cmq  = this.cmqBaseService.findByName(listName);
			if(null!=cmq) {
				listCodeSelected = String.valueOf(cmq.getCmqCode());
			}
		}
		datas = this.auditTrailService.findByCriterias(Long.valueOf(listCodeSelected), Integer.valueOf(dictionary), auditTimestamp);
		return "";
	}
	
	public List<String> findAuditTimestamps(int dictionaryVersion) {
		if (this.listCode == null && this.listName == null)
			return null;
		return this.auditTrailService.findAuditTimestamps(dictionaryVersion);
	}
	
	public List<CmqBaseDTO> selectList() {
		List<RefConfigCodeList> dict = new ArrayList<RefConfigCodeList>();
		dict.add(refCodeListService.getCurrentMeddraVersion());
		dict.add(refCodeListService.getTargetMeddraVersion());
		
 		return this.auditTrailService.findLists(dict);
	}
	

	public void resetCode(AjaxBehaviorEvent event) {
		this.listCode = null;
	}

	public void resetName(AjaxBehaviorEvent event) {
		this.listName = null;
	}
	
	public void generateExcel(List<AuditTrailDto> list) {
		String user =  this.authService.getUserGivenName() + " " + this.authService.getUserSurName();
		StreamedContent content = auditTrailService.generateExcel(list, user);
		setExcelFile(content); 
	}

	public String getAuditTimestamp() {
		return auditTimestamp;
	}

	public void setAuditTimestamp(String auditTimestamp) {
		this.auditTimestamp = auditTimestamp;
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

	public ICmqBase190Service getCmqBaseService() {
		return cmqBaseService;
	}

	public void setCmqBaseService(ICmqBase190Service cmqBaseService) {
		this.cmqBaseService = cmqBaseService;
	}

	
	public IRefCodeListService getRefCodeListService() {
		return refCodeListService;
	}

	public void setRefCodeListService(IRefCodeListService refCodeListService) {
		this.refCodeListService = refCodeListService;
	}


	public AuthenticationService getAuthService() {
		return authService;
	}


	public void setAuthService(AuthenticationService authService) {
		this.authService = authService;
	}
}
