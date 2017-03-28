package com.dbms.view;

import java.util.Date;

import javax.faces.event.AjaxBehaviorEvent;

import com.dbms.entity.cqt.CmqBase190;
import com.dbms.entity.cqt.CmqBaseTarget;
import com.dbms.entity.cqt.SmqBase190;
import com.dbms.entity.cqt.SmqBaseTarget;

/**
 * "Create/Update/Browse&Search" module's "Details" tab form data 
 * 
 * @author Andrius Mielkus(andrius.mielkus@yandex.com)
 *
 */
public class ListDetailsFormModel {
	public enum WizardType { BrowseWizard, CreateWizard, UpdateWizard, CopyWizard };
	
	private WizardType wizardType = WizardType.CreateWizard;
	private boolean modelChanged = false;
	
	private String extension;
	private String name;
	private String drugProgram;
	private String protocol;
	private String designee;
	private Integer level;
	private String critical;
	private String scope;
	private String product;
	private String group;
	private String algorithm;
	private String state;
	private String status;
	private String history;
	private Long code;
	private String createdBy;
	private Date creationDate;
	private String lastModifiedBy;
	private Date lastModifiedDate;
	
	
	public ListDetailsFormModel() {
		init();
	}
	
	public void init() {
		this.extension = "TME";
		this.name = "";
		this.drugProgram = "";
		this.protocol = "999999";
		this.product = "";
		this.level = 1;
		this.algorithm = "N";
		this.critical = "No";
		this.group = "No Group";
		this.state = CmqBase190.CMQ_STATE_VALUE_DRAFT;
		this.status = CmqBase190.CMQ_STATUS_DISP_LABEL_PENDING;
		this.designee = "NONE";
		this.modelChanged = false;
	}
	
	/**
	 * Load Form data from CmqBase190 Entity
	 * @param cmq
	 */
	public void loadFromCmqBase190(CmqBase190 cmq) {
		this.extension = cmq.getCmqTypeCd();
		this.name = cmq.getCmqName();
		this.protocol = cmq.getCmqProtocolCd();
		this.drugProgram = cmq.getCmqProgramCd();
		this.product = cmq.getCmqProductCd();
		this.designee = cmq.getCmqDesignee();
		this.level = cmq.getCmqLevel();
		this.critical = cmq.getCmqCriticalEvent();
		this.group = cmq.getCmqGroup();
		this.algorithm = cmq.getCmqAlgorithm();

		this.state = cmq.getCmqState();
		if(CmqBase190.CMQ_STATUS_VALUE_PENDING.equalsIgnoreCase(cmq.getCmqStatus())) {
			this.status = CmqBase190.CMQ_STATUS_DISP_LABEL_PENDING;
		} else if (CmqBase190.CMQ_STATUS_VALUE_ACTIVE.equalsIgnoreCase(cmq.getCmqStatus())) {
			this.status = CmqBase190.CMQ_STATUS_DISP_LABEL_ACTIVE;
		} else if (CmqBase190.CMQ_STATUS_VALUE_INACTIVE.equalsIgnoreCase(cmq.getCmqStatus())){
			this.status = CmqBase190.CMQ_STATUS_DISP_LABEL_INACTIVE;
		} else {
			this.status = "UNKNOWN";
		}
		
		if(this.wizardType == WizardType.CopyWizard)
			this.modelChanged = true;
		else 
			this.modelChanged = false;
		
		this.code = cmq.getCmqCode();
		this.createdBy = cmq.getCreatedBy();
		this.creationDate = cmq.getCreationDate();
		this.lastModifiedBy = cmq.getLastModifiedBy();
		this.lastModifiedDate = cmq.getLastModifiedDate();
	}
	
	/**
	 * Save Form data to CmqBase190 Entity
	 * @param cmq
	 */
	public void saveToCmqBase190(CmqBase190 cmq) {
		cmq.setCmqTypeCd(extension);
		cmq.setCmqName(name);
		cmq.setCmqProgramCd(drugProgram);
		cmq.setCmqProtocolCd(protocol);
		cmq.setCmqProductCd(product);
		cmq.setCmqDesignee(designee);
		if (cmq.getCmqDesignee() == null){
			cmq.setCmqDesignee("NONE");
		}
		cmq.setCmqLevel(level);
		cmq.setCmqAlgorithm(algorithm);
		
		cmq.setLastModifiedDate(new Date());
		cmq.setLastModifiedBy("test-user");
		
		if(wizardType == WizardType.CreateWizard || wizardType == WizardType.CopyWizard) {
			cmq.setCreationDate(new Date());
			cmq.setCmqStatus(CmqBase190.CMQ_STATUS_VALUE_PENDING);
			cmq.setCmqState(CmqBase190.CMQ_STATE_VALUE_DRAFT);
			cmq.setCmqGroup("No Group");
		}
	}
	
	/**
	 * Load Form data from CmqBase190 Entity
	 * @param cmq
	 */
	public void loadFromCmqBaseTarget(CmqBaseTarget cmq) {
		this.extension = cmq.getCmqTypeCd();
		this.name = cmq.getCmqName();
		this.protocol = cmq.getCmqProtocolCd();
		this.drugProgram = cmq.getCmqProgramCd();
		this.product = cmq.getCmqProductCd();
		this.designee = cmq.getCmqDesignee();
		this.level = cmq.getCmqLevel();
		this.critical = cmq.getCmqCriticalEvent();
		this.group = cmq.getCmqGroup();
		this.algorithm = cmq.getCmqAlgorithm();

		this.state = cmq.getCmqState();
		if(CmqBase190.CMQ_STATUS_VALUE_PENDING.equalsIgnoreCase(cmq.getCmqStatus())) {
			this.status = CmqBase190.CMQ_STATUS_DISP_LABEL_PENDING;
		} else if (CmqBase190.CMQ_STATUS_VALUE_ACTIVE.equalsIgnoreCase(cmq.getCmqStatus())) {
			this.status = CmqBase190.CMQ_STATUS_DISP_LABEL_ACTIVE;
		} else if (CmqBase190.CMQ_STATUS_VALUE_INACTIVE.equalsIgnoreCase(cmq.getCmqStatus())){
			this.status = CmqBase190.CMQ_STATUS_DISP_LABEL_INACTIVE;
		} else {
			this.status = "UNKNOWN";
		}
		
		if(this.wizardType == WizardType.CopyWizard)
			this.modelChanged = true;
		else 
			this.modelChanged = false;
		
		this.code = cmq.getCmqCode();
		this.createdBy = cmq.getCreatedBy();
		this.creationDate = cmq.getCreationDate();
		this.lastModifiedBy = cmq.getLastModifiedBy();
		this.lastModifiedDate = cmq.getLastModifiedDate();
	}
	
	/**
	 * Load Form data from SmqBase190 Entity
	 * @param cmq
	 */
	public void loadFromSmqBase190(SmqBase190 smq) {
		this.extension = smq.getImpactType();
		this.name = smq.getSmqName();
		this.protocol = "";
		this.drugProgram = "";
		this.product = "";
		this.designee = "";
		this.level = smq.getSmqLevel();
		this.critical = "";
		this.group = "";
		this.algorithm = smq.getSmqAlgorithm();
		this.status = smq.getSmqStatus();
		
		this.code = smq.getSmqCode();
		this.createdBy = "";
		this.creationDate = null;
		this.lastModifiedBy = "";
		this.lastModifiedDate = null;
	}
	
	/**
	 * Load Form data from CmqBase190 Entity
	 * @param cmq
	 */
	public void loadFromSmqBaseTarget(SmqBaseTarget smq) {
		this.extension = smq.getImpactType();
		this.name = smq.getSmqName();
		this.protocol = "";
		this.drugProgram = "";
		this.product = "";
		this.designee = "";
		this.level = smq.getSmqLevel();
		this.critical = "";
		this.group = "";
		this.algorithm = smq.getSmqAlgorithm();
		this.status = smq.getSmqStatus();

		this.code = smq.getSmqCode();
		this.createdBy = "";
		this.creationDate = null;
		this.lastModifiedBy = "";
		this.lastModifiedDate = null;
	}
	
	/**
	 * Method to change Level value on extention selection.
	 * 
	 * @param event
	 *            AjaxBehaviour
	 */
	public void changeLevel(AjaxBehaviorEvent event) {
		if (extension.equals("PRO")) {
			setLevel(2);
		} else {
			setLevel(1);
		}
		
		if(wizardType != WizardType.CopyWizard) {
			//we are not doing copy so change others.
			/**
			 * Getting code internal value from now on
			 */
			if (extension.equals("CPT") || extension.equals("DME")) {
				setDrugProgram("420001");
				setProduct("99999");
			}
			else {
				setDrugProgram("");
				setProduct(""); 
			}
		
			if (extension.equals("TME") || extension.equals("TR1") || extension.equals("CPT") || extension.equals("DME"))
				setProtocol("999999");
			else
				setProtocol("");
		}
	}
	//--------------------------- Getters & Setters ---------------------------
	
	/**
	 * Wizard Type which the current form is in
	 * @return
	 */
	public WizardType getWizardType() {
		return wizardType;
	}
	public void setWizardType(WizardType wizardType) {
		this.wizardType = wizardType;
	}
	
	/**
	 * Change/Submission status of Details form
	 * @return true if the model has been modified by user input
	 */
	public boolean isModelChanged() {
		return this.modelChanged;
	}
	public void setModelChanged(boolean detailsFormChanged) {
		this.modelChanged = detailsFormChanged;
	}

	/**
	 * Details Form / Extension
	 * Getter
	 * @return
	 */
	public String getExtension() {
		return extension;
	}
	public void setExtension(String extension) {
		if(this.extension == null || !this.extension.equals(extension))
			this.modelChanged = true;
		this.extension = extension;
	}
	
	/**
	 * Details Form / Name
	 * Getter
	 * @return
	 */
	public String getName() {
		return name;
	}
	public void setName(String name) {
		if(this.name == null || !this.name.equals(name))
			this.modelChanged = true;
		this.name = name;
	}

	/**
	 * Details Form / Drug Program
	 * Getter and Setter
	 * @return
	 */
	public String getDrugProgram() {
		return drugProgram;
	}
	public void setDrugProgram(String drugProgram) {
		if(this.drugProgram == null || !this.drugProgram.equals(drugProgram))
			this.modelChanged = true;
		this.drugProgram = drugProgram;
	}

	/**
	 * Details Form / Protocol
	 * Getter and Setter
	 * @return
	 */
	public String getProtocol() {
		return protocol;
	}

	public void setProtocol(String protocol) {
		if(this.protocol == null || !this.protocol.equals(protocol))
			this.modelChanged = true;
		this.protocol = protocol;
	}
	
	/**
	 * Details Form / Product
	 * Getter, Setter
	 * @return
	 */
	public String getProduct() {
		return product;
	}
	public void setProduct(String product) {
		if(this.product== null || !this.product.equals(product))
			this.modelChanged = true;
		this.product = product;
	}
	
	/**
	 * Details Form / Designee
	 * Getter, Setter
	 * @return
	 */
	public String getDesignee() {
		return designee;
	}
	public void setDesignee(String designee) {
		if(this.designee == null || !this.designee.equals(designee))
			this.modelChanged = true;
		this.designee = designee;
	}

	/**
	 * Details Form / Level
	 * Getter, Setter
	 * @return
	 */
	public Integer getLevel() {
		return level;
	}

	public void setLevel(Integer level) {
		if(this.level == null || !this.level.equals(level))
			this.modelChanged = true;
		this.level = level;
	}
	
	/**
	 * Details Form / Algorithm
	 * Getter, Setter
	 * @return
	 */
	public String getAlgorithm() {
		return algorithm;
	}

	public void setAlgorithm(String algorithm) {
		if(this.algorithm == null || !this.algorithm.equals(algorithm))
			this.modelChanged = true;
		this.algorithm = algorithm;
	}

	/**
	 * Details Form / State
	 * corresponds to Cmq_State
	 * Getter, Setter
	 * @return
	 */
	
	public String getState() {
		return state;
	}

	public void setState(String state) {
		this.state = state;
	}

	/**
	 * Details Form / Status
	 * corresponds to Cmq_Status
	 * Getter, Setter
	 * @return
	 */
	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	/**
	 * Details Form / Group
	 * Getter, Setter
	 * @return
	 */
	public String getGroup() {
		return group;
	}

	public void setGroup(String group) {
		this.group = group;
	}

	/**
	 * Details Form / Critical
	 * Getter, Setter
	 * @return
	 */
	public String getCritical() {
		return critical;
	}

	public void setCritical(String critical) {
		this.critical = critical;
	}

	/**
	 * Details Form / Scope
	 * Getter, Setter
	 * @return
	 */
	public String getScope() {
		return scope;
	}

	public void setScope(String scope) {
		this.scope = scope;
	}
	
	/**
	 * if drug program field is required or not
	 * 
	 * @return true for required
	 */
	public boolean isDrugProgramRequired() {
		return ("TME".equalsIgnoreCase(extension)
				|| "TR1".equalsIgnoreCase(extension)
				|| "PRO".equalsIgnoreCase(extension));
	}
	
	/**
	 * if drug program field is required or not
	 * 
	 * @return true for required
	 */
	public boolean isProtocolRequired() {
		return ("PRO".equalsIgnoreCase(extension));
	}
	
	/**
	 * if product field is required or not
	 * 
	 * @return true for required
	 */
	public boolean isProductRequired() {
		return ("TME".equalsIgnoreCase(extension)
				|| "TR1".equalsIgnoreCase(extension)
				|| "PRO".equalsIgnoreCase(extension));
	}

	public Long getCode() {
		return code;
	}

	public void setCode(Long code) {
		this.code = code;
	}

	public String getCreatedBy() {
		return createdBy;
	}

	public void setCreatedBy(String createdBy) {
		this.createdBy = createdBy;
	}

	public Date getCreationDate() {
		return creationDate;
	}

	public void setCreationDate(Date creationDate) {
		this.creationDate = creationDate;
	}

	public String getLastModifiedBy() {
		return lastModifiedBy;
	}

	public void setLastModifiedBy(String lastModifiedBy) {
		this.lastModifiedBy = lastModifiedBy;
	}

	public Date getLastModifiedDate() {
		return lastModifiedDate;
	}

	public void setLastModifiedDate(Date lastModifiedDate) {
		this.lastModifiedDate = lastModifiedDate;
	}

}
