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

import com.dbms.entity.cqt.CmqBase190;
import com.dbms.entity.cqt.RefConfigCodeList;
import com.dbms.entity.cqt.dtos.AuditTrailDto;
import com.dbms.entity.cqt.dtos.CmqBaseDTO;
import com.dbms.service.AuthenticationService;
import com.dbms.service.IAuditTrailService;
import com.dbms.service.ICmqBase190Service;
import com.dbms.service.IRefCodeListService;
import com.dbms.util.CmqUtils;


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
	private List<AuditTrailDto> datas, filteredValues;
	
	@ManagedProperty("#{CmqBase190Service}")
	private ICmqBase190Service cmqBaseService;
	
	@ManagedProperty("#{AuditTrailService}")
	private IAuditTrailService auditTrailService;
	
	@ManagedProperty("#{RefCodeListService}")
	private IRefCodeListService refCodeListService;
	
	@ManagedProperty("#{AuthenticationService}")
	private AuthenticationService authService;
	
	private String timezone;
	
	@PostConstruct
	public void init() {
		this.datas = new ArrayList<AuditTrailDto>();
	}
	
	public String getTimezone() {
		FacesContext context = FacesContext.getCurrentInstance();
		GlobalController controller = (GlobalController) context.getApplication().evaluateExpressionGet(context, "#{globalController}", GlobalController.class);
 		return  controller.getTimezone(); 
	}
	
	public void reset() {
		this.filteredValues = new ArrayList<AuditTrailDto>();
		this.datas = new ArrayList<AuditTrailDto>();
		this.listName = null;
		this.listCode = null;
		this.dictionary = null;
		this.auditTimestamp = null;		
 	}
	
	public void findAudit() {
		this.datas = new ArrayList<AuditTrailDto>();
		if((listName == null || (listName != null && StringUtils.isBlank(listName))) 
				&& (listCode == null || (listCode != null && StringUtils.isBlank(listCode)))) {
			FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_WARN,
	                "Please select List Name or List Code", "");
	        FacesContext.getCurrentInstance().addMessage(null, msg);
	        return;
		}
		
		if(dictionary == null || (dictionary != null && StringUtils.isBlank(dictionary))) {
			FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_WARN,
	                "Please select Dictionary version", "");
	        FacesContext.getCurrentInstance().addMessage(null, msg);
	        return;
		}
		
		if(auditTimestamp == null || (auditTimestamp != null && StringUtils.isBlank(auditTimestamp))) {
			FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_WARN,
	                "Please select Audit timestamp", "");
	        FacesContext.getCurrentInstance().addMessage(null, msg);
	        return;
		}
		
		Long listCodeSelected = null;
		if(StringUtils.isNotBlank(listCode)) {
			listCodeSelected = Long.valueOf(listCode);
		}

		datas = this.auditTrailService.findByCriterias(listCodeSelected, listName, Integer.valueOf(dictionary), CmqUtils.convertimeZone("dd-MMM-yyyy:hh:mm:ss a z", auditTimestamp, getTimezone(), "dd-MMM-yyyy:hh:mm:ss a z", "EST"));
		if(null!=datas && !datas.isEmpty()){
			for(AuditTrailDto dto : datas) {
				dto.setAuditTimestamp(CmqUtils.convertimeZone("dd-MMM-yyyy:hh:mm:ss a", dto.getAuditTimestamp(), "EST", "dd-MMM-yyyy:hh:mm:ss a z",getTimezone()));
			}
		}
		this.filteredValues = datas;
		//RequestContext.getCurrentInstance().update("auditDT");

	}
	
	public List<String> findAuditTimestamps(int dictionaryVersion) {
		String listCodeSelected = listCode;
		if (this.listCode == null && this.listName == null)
			return null;
		if(!StringUtils.isBlank(listName) ) {
			CmqBase190 cmq  = this.cmqBaseService.findByName(listName);
			if(null!=cmq) {
				listCodeSelected = String.valueOf(cmq.getCmqCode());
			}
		}
		return this.auditTrailService.findAuditTimestamps(dictionaryVersion, listCodeSelected, null,getTimezone());
	}
	
	public List<CmqBaseDTO> selectList() {
		List<RefConfigCodeList> dict = new ArrayList<RefConfigCodeList>();
		dict.add(refCodeListService.getCurrentMeddraVersion());
		//dict.add(refCodeListService.getTargetMeddraVersion());
		
 		return this.auditTrailService.findLists(dict);
	}
	

	public void resetCode(AjaxBehaviorEvent event) {
		this.listCode = null;
	}

	public void resetName(AjaxBehaviorEvent event) {
		this.listName = null;
	}
	
	public void generateExcel(List<AuditTrailDto> list) {
 		System.out.println("*** audit " + timezone + "\n");	

		String user =  this.authService.getUserGivenName() + " " + this.authService.getUserSurName();
		StreamedContent content = auditTrailService.generateExcel(list, user, getTimezone());
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


	public List<AuditTrailDto> getFilteredValues() {
		return filteredValues;
	}


	public void setFilteredValues(List<AuditTrailDto> filteredValues) {
		this.filteredValues = filteredValues;
	}
 
	
}
