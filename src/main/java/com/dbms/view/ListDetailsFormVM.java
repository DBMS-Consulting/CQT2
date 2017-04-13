package com.dbms.view;

import java.util.Arrays;
import java.util.Date;

import javax.faces.event.AjaxBehaviorEvent;

import org.apache.commons.lang.StringUtils;

import com.dbms.entity.cqt.CmqBase190;
import com.dbms.entity.cqt.CmqBaseTarget;
import com.dbms.entity.cqt.RefConfigCodeList;
import com.dbms.service.IRefCodeListService;
import com.dbms.util.CqtConstants;
import java.util.List;

/**
 * "Create/Update/Browse&Search" module's "Details" tab form data 
 * 
 * @author Andrius Mielkus(andrius.mielkus@yandex.com)
 *
 */
public class ListDetailsFormVM {
	public enum WizardType { BrowseWizard, CreateWizard, UpdateWizard, CopyWizard };
    
	private WizardType wizardType = WizardType.CreateWizard;
	private boolean modelChanged = false;
	private IRefCodeListService refCodeListService;
	
	private String extension;
	private String name;
	private String drugProgram;
	private String protocol;
	private String designee;
	private Integer level;
	private String critical;
	private String scope;
	private String[] products;
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
	
	
	public ListDetailsFormVM() {
		init();
	}
	
	public void init() {
		this.extension = "TME";
		this.name = "";
		this.drugProgram = "";
		this.protocol = CqtConstants.CODE_LIST_NO_PROTOCOL_INTERNALCODE;
		this.products = new String[0];
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
		this.products = cmq.getCmqProductCds();
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
		cmq.setCmqProductCds(products);
		
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
        this.products = cmq.getCmqProductCds();
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
			setModelChanged(true);
		else 
			setModelChanged(false);
		
		this.code = cmq.getCmqCode();
		this.createdBy = cmq.getCreatedBy();
		this.creationDate = cmq.getCreationDate();
		this.lastModifiedBy = cmq.getLastModifiedBy();
		this.lastModifiedDate = cmq.getLastModifiedDate();
	}
	
	/**
	 * Method to change Level value on extention selection.
	 * 
	 * @param event
	 *            AjaxBehaviour
	 */
	public void afterChangeExtension(AjaxBehaviorEvent event) {
		if ("PRO".equals(extension)) {
			setLevel(2);
		} else {
			setLevel(1);
		}
		
		if(wizardType != WizardType.CopyWizard) {
			//we are not doing copy so change others.
			/**
			 * Getting code internal value from now on
			 */
			
			if("TME".equals(extension) ||
					"TR1".equals(extension)) {
				setLevel(1);
				setDrugProgram("");
				setProtocol(CqtConstants.CODE_LIST_NO_PROTOCOL_INTERNALCODE);
                setProducts(new String[0]);
			} else if("PRO".equals(extension)) {
				setLevel(2);
				setDrugProgram("");
				setProtocol("");
                setProducts(new String[0]);
			} else if("CPT".equals(extension) ||
					"DME".equals(extension)) {
				setDrugProgram(CqtConstants.CODE_LIST_NO_PROGRAM_INTERNALCODE);
				setProtocol(CqtConstants.CODE_LIST_NO_PROTOCOL_INTERNALCODE);
				setProducts(new String[] { CqtConstants.CODE_LIST_NO_PRODUCT_INTERNALCODE });
			} else {
				if(refCodeListService != null) {
					RefConfigCodeList d;
                    List<RefConfigCodeList> ds;
                    String[] dpIds;
										
					ds = refCodeListService.findDefaultsByConfigType(CqtConstants.CODE_LIST_TYPE_PRODUCT);
                    dpIds = new String[ds.size()];
                    for(int i=0;i<ds.size();i++) {
                        dpIds[i] = ds.get(i).getCodelistInternalValue();
                    }
					setProducts(dpIds);
					
					d = refCodeListService.findDefaultByConfigType(CqtConstants.CODE_LIST_TYPE_PROGRAM);
					setDrugProgram(d != null ? d.getCodelistInternalValue() : getDrugProgram());
					
					d = refCodeListService.findDefaultByConfigType(CqtConstants.CODE_LIST_TYPE_PROTOCOL);
					setProtocol(d != null ? d.getCodelistInternalValue() : getProtocol());
				} else {
					setDrugProgram("");
					setProtocol("");
				}
			}
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
			setModelChanged(true);
		this.extension = extension;
	}
	public String getExtensionLabel() {
		if("PRO".equals(this.extension))
			return "Protocol";
		else if("CPT".equals(this.extension))
			return "Critical Preferred Terms";
		else if("DME".equals(this.extension))
			return "Designated Medical Events";
		else if("TME".equals(this.extension))
			return "Targeted Medical Events";
		else if("TR1".equals(this.extension))
			return "Events of Special Clinical Interest";
		else if("TIER1".equals(this.extension))
			return "Events of Clinical Interest";
		else if(this.refCodeListService != null)
			return refCodeListService.interpretInternalCodeToValue(CqtConstants.CODE_LIST_TYPE_EXTENSION, this.extension);
			
		return "";
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
			setModelChanged(true);
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
			setModelChanged(true);
		this.drugProgram = drugProgram;
	}
	public String getDrugProgramLabel() {
		if(this.refCodeListService != null) {
			return refCodeListService.interpretInternalCodeToValue(CqtConstants.CODE_LIST_TYPE_PROGRAM, this.drugProgram);
		}
		return this.drugProgram;
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
			setModelChanged(true);
		this.protocol = protocol;
	}
	public String getProtocolLabel() {
		if(this.refCodeListService != null) {
			return refCodeListService.interpretInternalCodeToValue(CqtConstants.CODE_LIST_TYPE_PROTOCOL, this.protocol);
		}
		return this.protocol;
	}
	/**
	 * Details Form / Products
	 * Getter, Setter
	 * @return
	 */
	public String[] getProducts() {
		return products;
	}
	public void setProducts(String[] products) {
        this.products = (this.products == null ? new String[0] : this.products);
        products = (products == null ? new String[0] : products);
		Arrays.sort(this.products);
		Arrays.sort(products);
        if(!Arrays.equals(this.products, products))
			this.modelChanged = true;
		this.products = products;
	}
	
	public String getProductsLabel() {
		if(this.products != null && this.products.length != 0 && this.refCodeListService != null) {
			String[] prd = new String[this.products.length];
			for(int i=0;i<products.length;i++) {
				prd[i] = refCodeListService.interpretInternalCodeToValue(CqtConstants.CODE_LIST_TYPE_PRODUCT, this.products[i]);
			}
			return StringUtils.join(prd, ", ");
		}
		return "Choose products";
	}
	public String getProductsLabel1() {
        if(this.products != null && this.products.length != 0 && this.refCodeListService != null) {
            String[] prd = new String[this.products.length];
            for(int i=0;i<products.length;i++) {
                prd[i] = refCodeListService.interpretInternalCodeToValue(CqtConstants.CODE_LIST_TYPE_PRODUCT, this.products[i]);
            }
            return StringUtils.join(prd, ", ");
        }
        return "";
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
			setModelChanged(true);
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
			setModelChanged(true);
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
			setModelChanged(true);
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
	public String getStatusLabel() {
		if(CmqBase190.CMQ_STATUS_VALUE_ACTIVE.equals(this.status))
			return CmqBase190.CMQ_STATUS_DISP_LABEL_ACTIVE;
		else if(CmqBase190.CMQ_STATUS_VALUE_INACTIVE.equals(this.status))
			return CmqBase190.CMQ_STATUS_DISP_LABEL_INACTIVE;
		else if(CmqBase190.CMQ_STATUS_VALUE_PENDING.equals(this.status))
			return CmqBase190.CMQ_STATUS_DISP_LABEL_PENDING;
		return this.status;
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

	public IRefCodeListService getRefCodeListService() {
		return refCodeListService;
	}

	public void setRefCodeListService(IRefCodeListService refCodeListService) {
		this.refCodeListService = refCodeListService;
	}

}
