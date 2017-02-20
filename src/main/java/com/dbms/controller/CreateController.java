package com.dbms.controller;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.RequestScoped;
import javax.faces.context.FacesContext;
import javax.faces.event.AjaxBehaviorEvent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dbms.entity.cqt.CmqBase190;
import com.dbms.service.ICmqBase190Service;
import com.dbms.util.exceptions.CqtServiceException;

/**
 * @author Jay G.(jayshanchn@hotmail.com)
 * @date Feb 12, 2017 7:34:05 AM
 **/
@ManagedBean
@RequestScoped
public class CreateController implements Serializable {

	private static final long serialVersionUID = -443251941538546278L;

	private static final Logger LOG = LoggerFactory
			.getLogger(CreateController.class);

	@ManagedProperty("#{CmqBase190Service}")
	private ICmqBase190Service cmqBaseService;

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

	@PostConstruct
	public void init() {
		this.state = "Draft";
		this.status = "Pending";
		
	//	maintainDesigBtn = false;
		level = 1;
		critical = "No";
		group = "No Group";
		extension = "TME";
	}

	private CmqBase190 selectedData;

	public CreateController() {
		this.selectedData = new CmqBase190();
	}

	public String save() {
		selectedData.setCreationDate(new Date());
		selectedData.setCmqTypeCd(extension);				
		selectedData.setCmqState(state);
		selectedData.setCmqAlgorithm(algorithm);
		selectedData.setCmqProductCd(product);
		selectedData.setCmqLevel(level);
		selectedData.setCmqProtocolCd(protocol);
		selectedData.setCmqProgramCd(drugProgram);
		selectedData.setCmqGroup(group);
		selectedData.setCmqStatus("P");				//length is 1 only
		//selectedData.setCmqParentCode(90000000L);
		//selectedData.setCmqScope(scope);						//Missing value
		//selectedData.setCmqCriticalEvent(critical); 		//form precision is 5 chars but form sends like 10 chars.
		
		//hard coded for now
		selectedData.setCmqCriticalEvent("Broad");			//Workaround for now
		selectedData.setCmqCode(91234567L);		
		selectedData.setCmqNote("Test Note");	
		selectedData.setCmqName("Test Name");
		selectedData.setCmqDescription("Test Description");
		selectedData.setCmqState("P");
		selectedData.setCmqSource("Test Source");
		selectedData.setCreatedBy("Test user");
		selectedData.setActivatedBy("Test user");
		selectedData.setActivationDate(new Date());
		selectedData.setDictionaryName("Test-Dict");
		selectedData.setDictionaryVersion("1.0");
		selectedData.setCmqSubversion(new BigDecimal(0.23d));

		try {
			cmqBaseService.create(selectedData);

			FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_INFO,
					"Infos saved", "");
			FacesContext.getCurrentInstance().addMessage(null, msg);
		} catch (CqtServiceException e) {
			LOG.error("Exception occured while creating CmqBase190.", e);

			FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR,
					"Exception occured while saving", "");
			FacesContext.getCurrentInstance().addMessage(null, msg);

			return null;
		}
		//return "/index.xhtml";
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

		if (extension.equals("CPT") || extension.equals("DME")
				|| extension.equals("TME") || extension.equals("TR1"))
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
}
