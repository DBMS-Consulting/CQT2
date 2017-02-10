package com.dbms.controller;

import java.io.Serializable;
import java.util.List;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.ViewScoped;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dbms.entity.cqt.CreateEntity;
import com.dbms.service.CreateEntityService;

/**
 * @date Feb 7, 2017 7:39:34 AM
 **/
@ManagedBean
@ViewScoped
public class SearchController implements Serializable{

	private static final long serialVersionUID = 5299394344651669792L;
	
	private static final Logger log=LoggerFactory.getLogger(SearchController.class);
	
	
	@ManagedProperty("#{createEntityService}")
	private CreateEntityService createEntityService;

	private String extension;
	private String drugProgram;
	private String protocol;
	private String state;
	
	private List<CreateEntity> values;

	public String getExtension() {
		return extension;
	}

	public void setExtension(String extension) {
		this.extension = extension;
	}
	
	public String getDrugProgram() {
		return drugProgram;
	}

	public void setDrugProgram(String drugProgram) {
		this.drugProgram = drugProgram;
	}

	public String getProtocol() {
		return protocol;
	}

	public void setProtocol(String protocol) {
		this.protocol = protocol;
	}

	public void search(){
		String extensionStr=null;
		if(!StringUtils.equals(extension, "-1")){
			extensionStr=extension;
		}
		values=createEntityService.findByCriterias(extensionStr,drugProgram,protocol);
		log.debug("found values {}",values==null?0:values.size());
	}

	public List<CreateEntity> getValues() {
		return values;
	}

	public void setValues(List<CreateEntity> values) {
		this.values = values;
	}

	public void setCreateEntityService(CreateEntityService createEntityService) {
		this.createEntityService = createEntityService;
	}

	public String getState() {
		return state;
	}

	public void setState(String state) {
		this.state = state;
	}
	
}

