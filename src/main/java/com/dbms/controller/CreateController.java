package com.dbms.controller;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.SessionScoped;
import javax.faces.context.FacesContext;
import javax.faces.event.AjaxBehaviorEvent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dbms.entity.cqt.CmqBase190;
import com.dbms.entity.cqt.RefConfigCodeList;
import com.dbms.service.ICmqBase190Service;
import com.dbms.service.IRefCodeListService;
import com.dbms.util.exceptions.CqtServiceException;

/**
 * @author Jay G.(jayshanchn@hotmail.com)
 * @date Feb 12, 2017 7:34:05 AM
 **/
@ManagedBean
@SessionScoped
public class CreateController implements Serializable {

	private static final long serialVersionUID = -443251941538546278L;

	private static final Logger LOG = LoggerFactory.getLogger(CreateController.class);

	@ManagedProperty("#{CmqBase190Service}")
	private ICmqBase190Service cmqBaseService;

	@ManagedProperty("#{RefCodeListService}")
	private IRefCodeListService refCodeListService;

	private String extension;
	private String drugProgram;
	private String protocol;
	private String state;
	private Integer level;
	private String status;
	private String critical;
	private String scope;
	private String product;
	private String group;
	private String history;
	private String algorithm;
	private boolean maintainDesigBtn;

	private String description;
	private String notes;
	private String source;

	@PostConstruct
	public void init() {
		this.state = "Draft";
		this.status = "Pending";
		this.description = "****** Description *******";
		this.notes = "****** Notes *******";
		this.source = "****** Source *******";

		maintainDesigBtn = false;
		level = 1;
		critical = "No";
		group = "No Group";
		extension = "TME";
		algorithm = "N";
	}

	private CmqBase190 selectedData;

	public CreateController() {
		this.selectedData = new CmqBase190();
	}

	public String save() {
		try {
			// get the next value of code
			Long codevalue = this.cmqBaseService.getNextCodeValue();
			RefConfigCodeList currentMeddraVersionCodeList = this.refCodeListService.getCurrentMeddraVersion();

			// fill data
			selectedData.setCreationDate(new Date());
			//selectedData.setCmqName("MEDDRA");
			selectedData.setCmqTypeCd(extension);
			selectedData.setCmqState(state);
			selectedData.setCmqAlgorithm(algorithm);
			selectedData.setCmqProductCd(product);
			selectedData.setCmqLevel(level);
			selectedData.setCmqProtocolCd(protocol);
			selectedData.setCmqProgramCd(drugProgram);
			selectedData.setCmqGroup(group);
			selectedData.setCmqStatus("P"); // length is 1 only
			selectedData.setCmqCode(codevalue);
			selectedData.setDictionaryVersion(currentMeddraVersionCodeList.getValue());

			// hard coded for now
			selectedData.setCmqDescription(description);
			selectedData.setCreatedBy("Test user");
			selectedData.setDictionaryName("Test-Dict");
			selectedData.setCmqSubversion(new BigDecimal(0.23d));

			cmqBaseService.create(selectedData);

			// retrieve the saved cmq base
			CmqBase190 savedEntity = cmqBaseService.findByCode(codevalue);

			// save the cmq code to session
			FacesContext.getCurrentInstance().getExternalContext().getSessionMap().put("NEW-CMQ_BASE-ID",
					savedEntity.getId());

			FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "Infos saved", "");
			FacesContext.getCurrentInstance().addMessage(null, msg);
		} catch (CqtServiceException e) {
			LOG.error("Exception occured while creating CmqBase190.", e);

			FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Exception occured while saving", "");
			FacesContext.getCurrentInstance().addMessage(null, msg);

			return null;
		}
		// return "/index.xhtml";
		return "";
	}

	public String saveInformativeNotes() {
		Long cmqId = (Long) (FacesContext
				.getCurrentInstance()
				.getExternalContext()
				.getSessionMap()
				.get("NEW-CMQ_BASE-ID"));

		CmqBase190 savedEntity = cmqBaseService.findById(cmqId);
		savedEntity.setCmqDescription(selectedData.getCmqDescription());
		savedEntity.setCmqNote(selectedData.getCmqNote());
		savedEntity.setCmqSource(selectedData.getCmqSource());
		try {
			this.cmqBaseService.update(savedEntity);
		} catch (CqtServiceException e) {
			LOG.error("Exception occured while updating CmqBase190 for add informative notes.", e);

			FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Exception occured while saving", "");
			FacesContext.getCurrentInstance().addMessage(null, msg);

			return null;
		}
		return "";
	}

	/**
	 * Method to change Level value on extention selection.
	 * 
	 * @param event
	 *            AjaxBehaviour
	 */
	public void changeLevel(AjaxBehaviorEvent event) {
		if (extension.equals("PRO")) {
			setLevel(1);
		} else
			setLevel(2);

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

	public String getState() {
		return state;
	}

	public void setState(String state) {
		this.state = state;
	}

	public Integer getLevel() {
		return level;
	}

	public void setLevel(Integer level) {
		this.level = level;
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

	public String getHistory() {
		return history;
	}

	public void setHistory(String history) {
		this.history = history;
	}

	public CmqBase190 getSelectedData() {
		return selectedData;
	}

	public void setSelectedData(CmqBase190 selectedData) {
		this.selectedData = selectedData;
	}

	public void setCmqBaseService(ICmqBase190Service cmqBaseService) {
		this.cmqBaseService = cmqBaseService;
	}

	public String getAlgorithm() {
		return algorithm;
	}

	public void setAlgorithm(String algorithm) {
		this.algorithm = algorithm;
	}

	public ICmqBase190Service getCmqBaseService() {
		return cmqBaseService;
	}

	public IRefCodeListService getRefCodeListService() {
		return refCodeListService;
	}

	public void setRefCodeListService(IRefCodeListService refCodeListService) {
		this.refCodeListService = refCodeListService;
	}

	public boolean isMaintainDesigBtn() {
		return maintainDesigBtn;
	}

	public void setMaintainDesigBtn(boolean maintainDesigBtn) {
		this.maintainDesigBtn = maintainDesigBtn;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getNotes() {
		return notes;
	}

	public void setNotes(String notes) {
		this.notes = notes;
	}

	public String getSource() {
		return source;
	}

	public void setSource(String source) {
		this.source = source;
	}
}
