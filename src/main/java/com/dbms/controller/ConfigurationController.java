package com.dbms.controller;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.faces.bean.ApplicationScoped;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;

import com.dbms.entity.cqt.ExtentionConfigCodeList;
import com.dbms.entity.cqt.ProgramConfigCodeList;
import com.dbms.service.IExtensionCodeListService;
import com.dbms.service.IProgramCodeListService;
import com.dbms.util.OrderBy;

/**
 * Controller that will contains codelist methods.
 * 
 * @author
 *
 */
@ManagedBean(name = "configMB")
@ApplicationScoped
public class ConfigurationController implements Serializable {

	/**
	 * serialVersionUID.
	 */
	private static final long serialVersionUID = 5539162862901321913L;

	private List<ExtentionConfigCodeList> extentionConfigCodeList;

	private List<ProgramConfigCodeList> programConfigCodeList;

	@ManagedProperty("#{ExtensionCodeListService}")
	private IExtensionCodeListService extensionCodeListService;

	@ManagedProperty("#{ProgramCodeListService}")
	private IProgramCodeListService programCodeListService;

	@PostConstruct
	public void init() {
		getExtensionList();
	}

	/**
	 * Returns extensions list.
	 * 
	 * @return
	 */
	public List<ExtentionConfigCodeList> getExtensionList() {
		this.extentionConfigCodeList = this.extensionCodeListService.list("displaySN", OrderBy.ASC);
		if (this.extentionConfigCodeList == null) {
			this.extentionConfigCodeList = new ArrayList<>();
		}
		return this.extentionConfigCodeList;
	}

	/**
	 * Returns extensions list.
	 * 
	 * @return
	 */
	public List<ProgramConfigCodeList> getDrugProgramList() {
		this.programConfigCodeList = this.programCodeListService.list("displaySN", OrderBy.ASC);
		if (this.programConfigCodeList == null) {
			this.programConfigCodeList = new ArrayList<>();
		}
		return this.programConfigCodeList;
	}

	public List<ExtentionConfigCodeList> getExtentionConfigCodeList() {
		return extentionConfigCodeList;
	}

	public void setExtentionConfigCodeList(List<ExtentionConfigCodeList> extentionConfigCodeList) {
		this.extentionConfigCodeList = extentionConfigCodeList;
	}

	public List<ProgramConfigCodeList> getProgramConfigCodeList() {
		return programConfigCodeList;
	}

	public void setProgramConfigCodeList(List<ProgramConfigCodeList> programConfigCodeList) {
		this.programConfigCodeList = programConfigCodeList;
	}

	public IExtensionCodeListService getExtensionCodeListService() {
		return extensionCodeListService;
	}

	public void setExtensionCodeListService(IExtensionCodeListService extensionCodeListService) {
		this.extensionCodeListService = extensionCodeListService;
	}

	public IProgramCodeListService getProgramCodeListService() {
		return programCodeListService;
	}

	public void setProgramCodeListService(IProgramCodeListService programCodeListService) {
		this.programCodeListService = programCodeListService;
	}
}
