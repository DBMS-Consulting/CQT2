package com.dbms.controller;

import java.io.Serializable;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.ViewScoped;
import javax.faces.event.AjaxBehaviorEvent;

import org.apache.commons.lang3.StringUtils;
import org.primefaces.component.wizard.Wizard;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dbms.entity.cqt.CreateEntity;
import com.dbms.service.CreateEntityService;

/**
 * @date Feb 7, 2017 7:39:34 AM
 **/
@ManagedBean
@ViewScoped
public class SearchController implements Serializable {

	private static final long serialVersionUID = 5299394344651669792L;

	private static final Logger log = LoggerFactory
			.getLogger(SearchController.class);

	@ManagedProperty("#{createEntityService}")
	private CreateEntityService createEntityService;

	private String extension;
	private String drugProgram;
	private String protocol;
	private String state;
	private String level;
	private String status;
	private String critical;
	private String scope;
	private String product;
	private String group;

	private boolean maintainDesigBtn;

	private List<CreateEntity> values;
	
	private Wizard updateWizard, copyWizard, browseWizard;	
	

	@PostConstruct
	public void init() {
		maintainDesigBtn = false;
		status = "Active";
		state = "Published";
		level= "1";
		critical = "No";
		group = "No Group";
		extension = "TME";
	}
	
	public void changeTabUpdate() {
		updateWizard.setStep("details");
	}
	
	public void changeTabCopy() {
		copyWizard.setStep("details");
	}
	
	public void changeTabBrowse() {
		browseWizard.setStep("details");
	}

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

	public void search() {
		String extensionStr = null;
		if (!StringUtils.equals(extension, "-1")) {
			extensionStr = extension;
		}
		values = createEntityService.findByCriterias(extensionStr, drugProgram,
				protocol);
		log.debug("found values {}", values == null ? 0 : values.size());
	}

	/**
	 * Method to change Level value on extention selection.
	 * 
	 * @param event
	 *            AjaxBehaviour
	 */
	public void changeLevel(AjaxBehaviorEvent event) {
		if (extension.equals("PRO")) {
			setLevel("2");
		}
		else
			setLevel("1");
		
		if (extension.equals("CPT") || extension.equals("DME"))
			setDrugProgram("No Program");
		else
			setDrugProgram("");
		
		if (extension.equals("CPT") || extension.equals("DME") || extension.equals("TME") || extension.equals("TR1"))
			setProtocol("No Protocol");
		else
			setProtocol("");
		
		if (extension.equals("CPT") || extension.equals("DME"))
			setProduct("No Product");
		else
			setProduct("");
	}
	
	/**
	 * Method to change State value on status selection.
	 * 
	 * @param event
	 *            AjaxBehaviour
	 */
	public void changeState(AjaxBehaviorEvent event) {
		if (status.equals("Active"))
			setState("Published");
		if (status.equals("Inactive"))
			setState("Draft");
		if (status.equals("All"))
			setState("All");
		if (status.equals("Pending"))
			setState("Draft");
	}
	
	public String getLevel() {
		return level;
	}

	public void setLevel(String level) {
		this.level = level;
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

	public boolean isMaintainDesigBtn() {
		return maintainDesigBtn;
	}

	public void setMaintainDesigBtn(boolean maintainDesigBtn) {
		this.maintainDesigBtn = maintainDesigBtn;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getCritical() {
		return critical;
	}

	public void setCritical(String critical) {
		this.critical = critical;
	}

	public String getScope() {
		return scope;
	}

	public void setScope(String scope) {
		this.scope = scope;
	}

	public String getProduct() {
		return product;
	}

	public void setProduct(String product) {
		this.product = product;
	}

	public String getGroup() {
		return group;
	}

	public void setGroup(String group) {
		this.group = group;
	}

	public Wizard getUpdateWizard() {
		return updateWizard;
	}

	public void setUpdateWizard(Wizard updateWizard) {
		this.updateWizard = updateWizard;
	}

	public Wizard getCopyWizard() {
		return copyWizard;
	}

	public void setCopyWizard(Wizard copyWizard) {
		this.copyWizard = copyWizard;
	}

	public Wizard getBrowseWizard() {
		return browseWizard;
	}

	public void setBrowseWizard(Wizard browseWizard) {
		this.browseWizard = browseWizard;
	}
}
