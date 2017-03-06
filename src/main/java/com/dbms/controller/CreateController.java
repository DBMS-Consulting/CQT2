package com.dbms.controller;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
import javax.faces.event.AjaxBehaviorEvent;

import org.apache.commons.lang3.StringUtils;
import org.primefaces.component.wizard.Wizard;
import org.primefaces.context.RequestContext;
import org.primefaces.event.FlowEvent;
import org.primefaces.model.TreeNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dbms.csmq.HierarchyNode;
import com.dbms.entity.IEntity;
import com.dbms.entity.cqt.CmqBase190;
import com.dbms.entity.cqt.CmqRelation190;
import com.dbms.entity.cqt.RefConfigCodeList;
import com.dbms.entity.cqt.SmqBase190;
import com.dbms.entity.cqt.SmqRelation190;
import com.dbms.entity.cqt.dtos.MeddraDictHierarchySearchDto;
import com.dbms.service.ICmqBase190Service;
import com.dbms.service.ICmqRelation190Service;
import com.dbms.service.IRefCodeListService;
import com.dbms.util.exceptions.CqtServiceException;

/**
 * @author Jay G.(jayshanchn@hotmail.com)
 * @date Feb 12, 2017 7:34:05 AM
 **/
@ManagedBean
@ViewScoped
public class CreateController implements Serializable {

	private static final long serialVersionUID = -443251941538546278L;

	private static final Logger LOG = LoggerFactory.getLogger(CreateController.class);

	@ManagedProperty("#{CmqBase190Service}")
	private ICmqBase190Service cmqBaseService;

	@ManagedProperty("#{CmqRelation190Service}")
	private ICmqRelation190Service cmqRelationService;

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

	private Wizard updateWizard, copyWizard, browseWizard, createWizard;
	private Long codeSelected;

	private TreeNode relationsRoot;
	private String[] selectedDesignees;

	private boolean reactivate, retire, demote, delete, approve, reviewed;
	private Long codevalue;
	
	private CmqBase190 selectedData;

	@PostConstruct
	public void init() {
		initAll();

		setReactivate(true);
		setRetire(true);
		setDemote(true);
		setDelete(true);
		setApprove(true);
		setReviewed(true);
	}

	private void initAll() {
		this.state = "Draft";
		this.status = "Pending";
		this.description = "*** Description ****";
		this.notes = "";
		this.source = "";

		maintainDesigBtn = false;
		level = 1;
		critical = "No";
		group = "No Group";
		drugProgram = "";
		product = "";
		protocol = "";
		extension = "TME";
		algorithm = "N";
	}

	public void initCreateForm() {
		this.selectedData = new CmqBase190();
		selectedData.setCmqDescription("*** Description ****");
	}

	/**
	 * FlowListener of Browse Wizard Component
	 * @param event
	 * @return
	 */
	public String onBrowseWizardFlowProcess(FlowEvent event) {
		String nextStep = event.getOldStep();
		if (codeSelected != null) {
			nextStep = event.getNewStep();
		}
		RequestContext.getCurrentInstance().update("fBrowse:wizardNavbar");
		return nextStep;
	}

	public boolean isBrowseWizardNavbarShown() {
		return !browseWizard.getStep().equals("searchBrowse");
	}
	public boolean isBrowseWizardNavbarNextShown() {
		return isBrowseWizardNavbarShown() && !browseWizard.getStep().equals("relations");
	}
	public boolean isBrowseWizardNavbarBackShown() {
		return !browseWizard.getStep().equals("searchBrowse");
	}

	/**
	 * FlowListener of Update Wizard Component
	 * @param event
	 * @return
	 */
	public String onUpdateWizardFlowProcess(FlowEvent event) {
		String nextStep = event.getOldStep();
		if (codeSelected != null) {
			nextStep = event.getNewStep();
		}
		RequestContext.getCurrentInstance().update("fUpdate:wizardNavbar");
		return nextStep;
	}

	public boolean isUpdateWizardNavbarShown() {
		return !updateWizard.getStep().equals("searchUpdate");
	}
	public boolean isUpdateWizardNavbarNextShown() {
		return isUpdateWizardNavbarShown() && !updateWizard.getStep().equals("confirmPanel");
	}
	public boolean isUpdateWizardNavbarBackShown() {
		return !updateWizard.getStep().equals("searchUpdate");
	}

	/**
	 * FlowListener of Update Wizard Component
	 * @param event
	 * @return
	 */
	public String onCopyWizardFlowProcess(FlowEvent event) {
		String nextStep = event.getOldStep();
		if (codeSelected != null) {
			nextStep = event.getNewStep();
		}
		RequestContext.getCurrentInstance().update("fCopy:wizardNavbar");
		return nextStep;
	}

	public boolean isCopyWizardNavbarShown() {
		return !copyWizard.getStep().equals("searchCopy");
	}
	public boolean isCopyWizardNavbarNextShown() {
		return isCopyWizardNavbarShown() && !copyWizard.getStep().equals("confirmPanel");
	}
	public boolean isCopyWizardNavbarBackShown() {
		return !copyWizard.getStep().equals("searchCopy");
	}

	public CreateController() {
		this.selectedData = new CmqBase190();
	}

	/**
	 * Bool when State is 'Draft' or 'Reviewed'.
	 * @return boolean
	 */
	public boolean isReadOnlyState() {
		if(copyWizard != null) {
			return false;
		} else if (updateWizard != null) {
			if ((selectedData != null) && (selectedData.getCmqState() != null) 
					&& (selectedData.getCmqState().equals("Draft") || selectedData.getCmqState().equals("Reviewed"))){
				return false;
			} else if (selectedData != null && selectedData.getCmqState() == null) {
				return false;	
			} else {
				return true;
			}
		} 
		return false;
	}
	
	public String loadCmqBaseByCode(Long code) {
		codeSelected = null;
		CmqBase190 cmq = new CmqBase190();
		cmq = this.cmqBaseService.findByCode(code);

		if (cmq != null) {

			codeSelected = cmq.getCmqCode();

			selectedData = cmq;
			this.extension = selectedData.getCmqTypeCd();
			this.state = selectedData.getCmqState();
			
			if(selectedData.getCmqStatus().equalsIgnoreCase("p")) {
				this.status = "Pending";
			} else if (selectedData.getCmqStatus().equalsIgnoreCase("a")) {
				this.status = "Active";
			} else {
				this.status = "Inactive";
			}
			this.description = selectedData.getCmqDescription();
			this.notes = selectedData.getCmqNote();
			this.source = selectedData.getCmqSource();

			level = selectedData.getCmqLevel();
			critical = selectedData.getCmqCriticalEvent();
			group = selectedData.getCmqGroup();
			algorithm = selectedData.getCmqAlgorithm();

			protocol = selectedData.getCmqProtocolCd();
			drugProgram = selectedData.getCmqProgramCd();
			product = selectedData.getCmqProductCd();
		}

		if (browseWizard != null) {
			browseWizard.setStep("details");
			if(RequestContext.getCurrentInstance() != null) {
				// UI: force update the custom wizard navbar area
				RequestContext.getCurrentInstance().update("fBrowse:wizardNavbar");
			}
		}
		
		if (updateWizard != null) {
			updateWizard.setStep("details");
			if(RequestContext.getCurrentInstance() != null) {
				// UI: force update the custom wizard navbar area
				RequestContext.getCurrentInstance().update("fUpdate:wizardNavbar");
			}
			// selectedData = new CmqBase190();
			// setSelectedData(cmq);
		}
		
		if (copyWizard != null) {
			//reset the values which are not supposed to be copied.
			selectedData.setId(null);//need to set since we may need to create a new cmq
			selectedData.setCmqCode(null);//need to set since we may need to create a new cmq
			selectedData.setCmqStatus("P");
			selectedData.setCmqState("Draft");
			this.state = "Draft";
			this.status = "Pending";
			selectedData.setCmqGroup(null);
			selectedData.setCreationDate(null);
			selectedData.setCreatedBy(null);
			selectedData.setCmqDescription("");
			selectedData.setCmqNote("");
			selectedData.setCmqSource("");
			copyWizard.setStep("details");
			if(RequestContext.getCurrentInstance() != null) {
				// UI: force update the custom wizard navbar area
				RequestContext.getCurrentInstance().update("fCopy:wizardNavbar");
			}
		}

		return "";
	}

	public String save() {
		try {

			Long count = this.cmqBaseService.findCmqCountByCmqNameAndExtension(extension, selectedData.getCmqName());

			if (count > 0) {
				String errorMsg = "Duplicate CMQ name ('" + selectedData.getCmqName() + "')and extention ('" + extension
						+ "') found in db.";
				LOG.error(errorMsg);

				FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, errorMsg, "");
				FacesContext.getCurrentInstance().addMessage(null, msg);

				return null;
			} else {
				setDatas();

				cmqBaseService.create(selectedData);

				// retrieve the saved cmq base
				CmqBase190 savedEntity = cmqBaseService.findByCode(codevalue);

				// save the cmq code to session
				FacesContext.getCurrentInstance().getExternalContext().getSessionMap().put("NEW-CMQ_BASE-ID",
						savedEntity.getId());

				FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_INFO,
						"List '" + selectedData.getCmqName() + "' is successfully saved.", "");
				FacesContext.getCurrentInstance().addMessage(null, msg);

				if (selectedData.getCmqDescription().equals("*** Description ****"))
					selectedData.setCmqDescription("");
			}
		} catch (CqtServiceException e) {
			LOG.error("Exception occured while creating CmqBase190.", e);

			FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR,
					"An error occured while trying to save the details.", "");
			FacesContext.getCurrentInstance().addMessage(null, msg);

			return null;
		}
		return "";
	}

	public String copy() {
		try {
			setDatas();
			
			cmqBaseService.create(selectedData);

			// retrieve the saved cmq base
			CmqBase190 savedEntity = cmqBaseService.findByCode(selectedData.getCmqCode());

			// save the cmq code to session
			FacesContext.getCurrentInstance().getExternalContext().getSessionMap().put("NEW-CMQ_BASE-ID",
					savedEntity.getId());

			FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_INFO,
					"List '" + selectedData.getCmqName() + "' is successfully saved.", "");
			FacesContext.getCurrentInstance().addMessage(null, msg);

			if (selectedData.getCmqDescription().equals("*** Description ****"))
				selectedData.setCmqDescription("");
		} catch (CqtServiceException e) {
			LOG.error("Exception occured while creating CmqBase190.", e);

			FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR,
					"An error occured while trying to save the details.", "");
			FacesContext.getCurrentInstance().addMessage(null, msg);

			return null;
		}
		return "";
	}

	private void setDatas() throws CqtServiceException {
		// get the next value of code
		codevalue = this.cmqBaseService.getNextCodeValue();
		RefConfigCodeList currentMeddraVersionCodeList = this.refCodeListService.getCurrentMeddraVersion();

		// fill data
		selectedData.setCreationDate(new Date());
		selectedData.setCmqGroup(group);
		selectedData.setCmqTypeCd(extension);
		if ((status == null) || status.equals("Pending"))
			selectedData.setCmqStatus("P");
		if (status.equals("Active"))
			selectedData.setCmqStatus("A");
		if (status.equals("Inactive"))
			selectedData.setCmqStatus("I");
		selectedData.setCmqState(state);
		selectedData.setCmqAlgorithm(algorithm);
		selectedData.setCmqProductCd(product);
		selectedData.setCmqLevel(level);
		selectedData.setCmqProtocolCd(protocol);
		selectedData.setCmqProgramCd(drugProgram);
		selectedData.setCmqGroup(group);
		selectedData.setCmqCode(codevalue);
		selectedData.setCmqDescription(description);
		selectedData.setDictionaryVersion(currentMeddraVersionCodeList.getValue());

		// hard coded for now
		selectedData.setCreatedBy("Test user");
		selectedData.setDictionaryName("Test-Dict");
		selectedData.setCmqSubversion(new BigDecimal(0.23d));
	}

	public String cancel() {
		selectedData = new CmqBase190();
		initAll();

		FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_WARN, "Form canceled", "");
		FacesContext.getCurrentInstance().addMessage(null, msg);
		return "";
	}

	public String cancelNotes() {
		selectedData.setCmqDescription("");
		selectedData.setCmqNote("");
		selectedData.setCmqSource("");

		if (selectedData.getId() == null) {
			selectedData = new CmqBase190();
		}

		FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_WARN, "Form canceled", "");
		FacesContext.getCurrentInstance().addMessage(null, msg);
		return "";
	}

	public String cancelRelations() {
		if (selectedData.getId() == null) {
			selectedData = new CmqBase190();
		}
		FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_WARN, "Form canceled", "");
		FacesContext.getCurrentInstance().addMessage(null, msg);
		return "";
	}

	/**
	 * Update for details.
	 * 
	 * @return String
	 */
	public String update() {
		try {
			
			Long count = this.cmqBaseService.findCmqCountByCmqNameAndExtension(extension, selectedData.getCmqName());
			
			if(count < 2) {
				//we should have atmost 1
				CmqBase190 existingCmqBase = this.cmqBaseService.findByCode(selectedData.getCmqCode());
				
				existingCmqBase.setCmqTypeCd(extension);
				existingCmqBase.setCmqName(selectedData.getCmqName());
				existingCmqBase.setCmqProgramCd(drugProgram);
				existingCmqBase.setCmqProtocolCd(protocol);
				existingCmqBase.setCmqProductCd(product);
				if (selectedData.getCmqDesignee() == null){
					existingCmqBase.setCmqDesignee("NONE");
				}
				existingCmqBase.setCmqLevel(level);
				existingCmqBase.setCmqAlgorithm(algorithm);
				
				existingCmqBase.setLastModifiedDate(new Date());
				existingCmqBase.setLastModifiedBy("test-user");

				cmqBaseService.update(existingCmqBase);

				// retrieve the saved cmq base
				CmqBase190 savedEntity = cmqBaseService.findByCode(selectedData.getCmqCode());
				
				// update the selectedData with saved one
				// we do not simply assign selectedData = savedEntity,
				// because selectedData might contain other unsaved changes like description
				selectedData.setCmqTypeCd(savedEntity.getCmqTypeCd());
				selectedData.setCmqName(savedEntity.getCmqName());
				selectedData.setCmqProgramCd(savedEntity.getCmqProgramCd());
				selectedData.setCmqProtocolCd(savedEntity.getCmqProtocolCd());		
				selectedData.setCmqProductCd(savedEntity.getCmqProductCd());
				selectedData.setCmqDesignee(savedEntity.getCmqDesignee());
				selectedData.setCmqLevel(savedEntity.getCmqLevel());
				selectedData.setCmqAlgorithm(savedEntity.getCmqAlgorithm());
				selectedData.setLastModifiedDate(savedEntity.getLastModifiedDate());
				selectedData.setLastModifiedBy(savedEntity.getLastModifiedBy());			

				// // save the cmq code to session
				FacesContext.getCurrentInstance().getExternalContext().getSessionMap().put("NEW-CMQ_BASE-ID",
						savedEntity.getId());

				FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_INFO,
						"List '" + selectedData.getCmqName() + "' is successfully saved.", "");
				FacesContext.getCurrentInstance().addMessage(null, msg);
			} else {
				String errorMsg = "Duplicate CMQ name ('" + selectedData.getCmqName() + "')and exteion ('" + extension
						+ "') found in db.";
				LOG.error(errorMsg);

				FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, errorMsg, "");
				FacesContext.getCurrentInstance().addMessage(null, msg);

				return null;
			}
		} catch (CqtServiceException e) {
			LOG.error("Exception occured while updating CmqBase190.", e);

			FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR,
					"An error occured while trying to update the details.", "");
			FacesContext.getCurrentInstance().addMessage(null, msg);

			return null;
		}
		return "";
	}

	public String saveInformativeNotes() {
		Long cmqId = (Long) (FacesContext.getCurrentInstance().getExternalContext().getSessionMap()
				.get("NEW-CMQ_BASE-ID"));

		CmqBase190 savedEntity = cmqBaseService.findById(cmqId);
		savedEntity.setCmqDescription(selectedData.getCmqDescription());
		savedEntity.setCmqNote(selectedData.getCmqNote());
		savedEntity.setCmqSource(selectedData.getCmqSource());
		try {
			this.cmqBaseService.update(savedEntity);

			FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_INFO,
					"Informative Notes are successfully saved for '" + selectedData.getCmqName() + "'", "");
			FacesContext.getCurrentInstance().addMessage(null, msg);
		} catch (CqtServiceException e) {
			LOG.error("Exception occured while updating CmqBase190 for add informative notes.", e);

			FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR,
					"An error occured while trying to save Informative Notes.", "");
			FacesContext.getCurrentInstance().addMessage(null, msg);

			return null;
		}
		return "";
	}

	/**
	 * Save relations on a list.
	 * 
	 * @return
	 */
	public String saveRelations(TreeNode relationsRoot) {
		if ((relationsRoot != null) && (relationsRoot.getChildCount() > 0)) {
			List<TreeNode> childTreeNodes = relationsRoot.getChildren();
			Long cmqId = (Long) (FacesContext.getCurrentInstance().getExternalContext().getSessionMap()
					.get("NEW-CMQ_BASE-ID"));
			List<CmqRelation190> cmqRelationsList = new ArrayList<>();
			List<CmqBase190> cmqBaseChildrenList = new ArrayList<>();
			
			CmqBase190 cmqBase = cmqBaseService.findById(cmqId);
			for (TreeNode childTreeNode : childTreeNodes) {
				
				HierarchyNode hierarchyNode = (HierarchyNode) childTreeNode.getData();
				if (null != hierarchyNode) {
					IEntity entity = hierarchyNode.getEntity();
					if(entity instanceof CmqBase190) {
						CmqBase190 cmqEntity = (CmqBase190) entity;
						cmqEntity.setCmqParentCode(cmqBase.getCmqCode());
						cmqEntity.setCmqParentName(cmqBase.getCmqName());
						cmqBaseChildrenList.add(cmqEntity);
					} else {
						CmqRelation190 cmqRelation = new CmqRelation190();
						cmqRelation.setCmqCode(cmqBase.getCmqCode());
						cmqRelation.setCmqId(cmqId);		
						if (entity instanceof MeddraDictHierarchySearchDto) {
							MeddraDictHierarchySearchDto meddraDictHierarchySearchDto = (MeddraDictHierarchySearchDto) entity;
							String level = hierarchyNode.getLevel();
							// set the code first
							if (level.equalsIgnoreCase("SOC")) {
								cmqRelation.setSocCode(Long.parseLong(meddraDictHierarchySearchDto.getCode()));
							} else if (level.equalsIgnoreCase("HLGT")) {
								cmqRelation.setHlgtCode(Long.parseLong(meddraDictHierarchySearchDto.getCode()));
							} else if (level.equalsIgnoreCase("HLT")) {
								cmqRelation.setHltCode(Long.parseLong(meddraDictHierarchySearchDto.getCode()));
							} else if (level.equalsIgnoreCase("PT")) {
								cmqRelation.setPtCode(Long.parseLong(meddraDictHierarchySearchDto.getCode()));
							} else if (level.equalsIgnoreCase("LLT")) {
								cmqRelation.setLltCode(Long.parseLong(meddraDictHierarchySearchDto.getCode()));
							}
						} else if (entity instanceof SmqBase190) {
							SmqBase190 smqBase = (SmqBase190) entity;
							cmqRelation.setSmqCode(smqBase.getSmqCode());
						} else if (entity instanceof SmqRelation190) {
							SmqRelation190 smqRelation = (SmqRelation190) entity;
							cmqRelation.setSmqCode(smqRelation.getSmqCode());
						}
						cmqRelation
								.setTermWeight((hierarchyNode.getWeight() != null && !hierarchyNode.getWeight().equals(""))
										? Long.parseLong(hierarchyNode.getWeight()) : null);
						cmqRelation.setTermScope(hierarchyNode.getScope());
						cmqRelation.setTermCategory(hierarchyNode.getCategory());
						cmqRelation.setDictionaryName(cmqBase.getDictionaryName());
						cmqRelation.setDictionaryVersion(cmqBase.getDictionaryVersion());
						cmqRelation.setCreatedBy("test-user");
						cmqRelation.setCreationDate(new Date());
						cmqRelation.setCmqSubversion(cmqBase.getCmqSubversion());
						cmqRelationsList.add(cmqRelation);
					}
				}
			}
			if (!cmqRelationsList.isEmpty()) {
				try {
					this.cmqRelationService.create(cmqRelationsList);
					FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_INFO,
							"Relations are successfully saved for '" + cmqBase.getCmqName() + "'", "");
					FacesContext.getCurrentInstance().addMessage(null, msg);
				} catch (CqtServiceException e) {
					LOG.error("Exception occured while saving the list of CmqRelations for CMQ base code "
							+ cmqBase.getCmqCode(), e);

					FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR,
							"An error occured while saving the list of CmqRelations for CMQ base code "
									+ cmqBase.getCmqCode(),
							"");
					FacesContext.getCurrentInstance().addMessage(null, msg);
				}
			}
		}
		return "";
	}

	/**
	 * Update relations on a list.
	 * 
	 * @return
	 */
	public String updateRelations(TreeNode relationsRoot) {
		if ((relationsRoot != null) && (relationsRoot.getChildCount() > 0)) {
			List<CmqRelation190> cmqRelationsList = new ArrayList<>();
			List<CmqBase190> cmqBaseChildrenList = new ArrayList<>();
			List<TreeNode> childTreeNodes = relationsRoot.getChildren();
			//TEST laster if we really need this call.
			CmqBase190 cmqBase = this.cmqBaseService.findByCode(selectedData.getCmqCode());
			
			List<CmqRelation190> existingRelation = this.cmqRelationService.findByCmqCode(selectedData.getCmqCode());
			for (TreeNode childTreeNode : childTreeNodes) {
				boolean matchFound = false;
				boolean updateNeeded = false;
				Map<String, Object> matchingMap = null;
				HierarchyNode hierarchyNode = (HierarchyNode) childTreeNode.getData();
				if (null != hierarchyNode) {
					IEntity entity = hierarchyNode.getEntity();
					if (entity instanceof CmqBase190) {
						CmqBase190 cmqEntity = (CmqBase190) entity;
						cmqEntity.setCmqParentCode(cmqBase.getCmqCode());
						cmqEntity.setCmqParentName(cmqBase.getCmqName());
						cmqBaseChildrenList.add(cmqEntity);
					} else {
						CmqRelation190 cmqRelation = null;						
						if (entity instanceof MeddraDictHierarchySearchDto) {
							MeddraDictHierarchySearchDto meddraDictHierarchySearchDto = (MeddraDictHierarchySearchDto) entity;
							String level = hierarchyNode.getLevel();
							long meddraDictCode = Long.parseLong(meddraDictHierarchySearchDto.getCode());
							matchingMap = this.checkIfMeddraRelationExists(existingRelation, level, hierarchyNode);
							matchFound = (boolean) matchingMap.get("MATCH_FOUND");
							updateNeeded = (boolean) matchingMap.get("UPDATE_NEEDED");
							
							if(updateNeeded) {
								cmqRelation = (CmqRelation190) matchingMap.get("TARGET_CMQ_RELATION_FOR_UPDATE");
							} else {
								cmqRelation = new CmqRelation190();
								cmqRelation.setCmqCode(selectedData.getCmqCode());
								cmqRelation.setCmqId(cmqBase.getId());
								
								// set the code first if needed
								if (level.equalsIgnoreCase("SOC") && !matchFound) {
									cmqRelation.setSocCode(meddraDictCode);
								} else if (level.equalsIgnoreCase("HLGT") && !matchFound) {
									cmqRelation.setHlgtCode(meddraDictCode);
								} else if (level.equalsIgnoreCase("HLT") && !matchFound) {
									cmqRelation.setHltCode(meddraDictCode);
								} else if (level.equalsIgnoreCase("PT") && !matchFound) {
									cmqRelation.setPtCode(meddraDictCode);
								} else if (level.equalsIgnoreCase("LLT") && !matchFound) {
									cmqRelation.setLltCode(meddraDictCode);
								}
							}
						} else if (entity instanceof SmqBase190) {
							SmqBase190 smqBase = (SmqBase190) entity;
							matchingMap = this.checkIfSmqBaseOrSmqRelationExists(existingRelation, smqBase.getSmqCode(), hierarchyNode);
							matchFound = (boolean) matchingMap.get("MATCH_FOUND");
							updateNeeded = (boolean) matchingMap.get("UPDATE_NEEDED");
							if(updateNeeded) {
								cmqRelation = (CmqRelation190) matchingMap.get("TARGET_CMQ_RELATION_FOR_UPDATE");
							} else if(!matchFound) {
								cmqRelation = new CmqRelation190();
								cmqRelation.setCmqCode(selectedData.getCmqCode());
								cmqRelation.setCmqId(cmqBase.getId());
								cmqRelation.setSmqCode(smqBase.getSmqCode());
							}
						} else if (entity instanceof SmqRelation190) {
							SmqRelation190 smqRelation = (SmqRelation190) entity;
							matchingMap = this.checkIfSmqBaseOrSmqRelationExists(existingRelation, smqRelation.getSmqCode(), hierarchyNode);
							matchFound = (boolean) matchingMap.get("MATCH_FOUND");
							updateNeeded = (boolean) matchingMap.get("UPDATE_NEEDED");
							if(updateNeeded) {
								cmqRelation = (CmqRelation190) matchingMap.get("TARGET_CMQ_RELATION_FOR_UPDATE");
							} else if(!matchFound) {
								cmqRelation = new CmqRelation190();
								cmqRelation.setCmqCode(selectedData.getCmqCode());
								cmqRelation.setCmqId(cmqBase.getId());
								cmqRelation.setSmqCode(smqRelation.getSmqCode());
							}
						}
						
						if(!matchFound || updateNeeded) {
							cmqRelation.setTermWeight((!StringUtils.isBlank(hierarchyNode.getWeight()) 
															&& !hierarchyNode.getWeight().equalsIgnoreCase("null"))
														? Long.parseLong(hierarchyNode.getWeight()) : null);
							cmqRelation.setTermScope(hierarchyNode.getScope());
							cmqRelation.setTermCategory(hierarchyNode.getCategory());
							cmqRelation.setDictionaryName(cmqBase.getDictionaryName());
							cmqRelation.setDictionaryVersion(cmqBase.getDictionaryVersion());
							cmqRelation.setCmqSubversion(cmqBase.getCmqSubversion());
							if(updateNeeded) {
								cmqRelation.setLastModifiedDate(new Date());
								cmqRelation.setLastModifiedBy("test-user");
							} else {
								cmqRelation.setCreationDate(new Date());
								cmqRelation.setCreatedBy("test-user");
							}
							cmqRelationsList.add(cmqRelation);
						}
					}
				}//end of if (null != hierarchyNode)
			}
			
			if (!cmqRelationsList.isEmpty() || !cmqBaseChildrenList.isEmpty()) {
				try {
					if(!cmqRelationsList.isEmpty()) {
						this.cmqRelationService.update(cmqRelationsList);
					}
					if(!cmqBaseChildrenList.isEmpty()) {
						this.cmqBaseService.update(cmqBaseChildrenList);
					}
					FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_INFO,
							"Relations are successfully updated for '" + cmqBase.getCmqName() + "'", "");
					FacesContext.getCurrentInstance().addMessage(null, msg);
				} catch (CqtServiceException e) {
					LOG.error("Exception occured while updated the list of CmqRelations for CMQ base code "
							+ cmqBase.getCmqCode(), e);

					FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR,
							"An error occured while updated the list of CmqRelations for CMQ base code "
									+ cmqBase.getCmqCode(),
							"");
					FacesContext.getCurrentInstance().addMessage(null, msg);
				}
			}
		}
		return "";
	}
	
	private Map<String, Object> checkIfMeddraRelationExists(List<CmqRelation190> existingRelation, String matchKey, HierarchyNode hierarchyNode) {
		Map<String, Object> matchingMap = new HashMap<String, Object>(3);
		matchingMap.put("MATCH_FOUND", false);
		matchingMap.put("UPDATE_NEEDED", false);
		matchingMap.put("TARGET_CMQ_RELATION_FOR_UPDATE", null);
		
		MeddraDictHierarchySearchDto meddraDictHierarchySearchDto = (MeddraDictHierarchySearchDto) hierarchyNode.getEntity();
		long nodeSocCode = Long.parseLong(meddraDictHierarchySearchDto.getCode());
		for (CmqRelation190 cmqRelation190 : existingRelation) {
			if(null != cmqRelation190) {
				if(matchKey.equalsIgnoreCase("SOC")) {
					if((null != cmqRelation190.getSocCode()) && (cmqRelation190.getSocCode().longValue() == nodeSocCode)){
						matchingMap.put("MATCH_FOUND", true);
					}
				} else if(matchKey.equalsIgnoreCase("HLGT")) {
					if((null != cmqRelation190.getHlgtCode()) && (cmqRelation190.getHlgtCode().longValue() == nodeSocCode)){
						matchingMap.put("MATCH_FOUND", true);
					}
				} else if(matchKey.equalsIgnoreCase("HLT")) {
					if((null != cmqRelation190.getHltCode()) && (cmqRelation190.getHltCode().longValue() == nodeSocCode)){
						matchingMap.put("MATCH_FOUND", true);
					}
				} else if(matchKey.equalsIgnoreCase("PT")) {
					if((null != cmqRelation190.getPtCode()) && (cmqRelation190.getPtCode().longValue() == nodeSocCode)){
						matchingMap.put("MATCH_FOUND", true);
					}
				} else if(matchKey.equalsIgnoreCase("LLT")) {
					if((null != cmqRelation190.getLltCode()) && (cmqRelation190.getLltCode().longValue() == nodeSocCode)){
						matchingMap.put("MATCH_FOUND", true);
					}
				}
				Boolean matchFound = (Boolean) matchingMap.get("MATCH_FOUND");
				if(matchFound) {
					matchingMap.put("UPDATE_NEEDED", this.checkIfExistingRelationNeedsUpdate(cmqRelation190, hierarchyNode));
					matchingMap.put("TARGET_CMQ_RELATION_FOR_UPDATE", cmqRelation190);
					break;
				}
			}//end of if(null != cmqRelation190)
		}//end of for (CmqRelation190 cmqRelation190 : existingRelation) 
		return matchingMap;
	}
	
	private Map<String, Object> checkIfSmqBaseOrSmqRelationExists(List<CmqRelation190> existingRelation, Long smqCode, HierarchyNode hierarchyNode) {
		Map<String, Object> matchingMap = new HashMap<String, Object>(3);
		matchingMap.put("MATCH_FOUND", false);
		matchingMap.put("UPDATE_NEEDED", false);
		matchingMap.put("TARGET_CMQ_RELATION_FOR_UPDATE", null);
		
		for (CmqRelation190 cmqRelation190 : existingRelation) {
			if((null != cmqRelation190.getSmqCode()) && (cmqRelation190.getSmqCode().longValue() == smqCode.longValue())){
				matchingMap.put("MATCH_FOUND", true);
			}
			
			Boolean matchFound = (Boolean) matchingMap.get("MATCH_FOUND");
			if(matchFound) {
				matchingMap.put("UPDATE_NEEDED", this.checkIfExistingRelationNeedsUpdate(cmqRelation190, hierarchyNode));
				matchingMap.put("TARGET_CMQ_RELATION_FOR_UPDATE", cmqRelation190);
				break;
			}
		}
		return matchingMap;
	}
	
	private boolean checkIfExistingRelationNeedsUpdate(CmqRelation190 cmqRelation190, HierarchyNode hierarchyNode) {
		boolean needsUpdate = false;
		if(!StringUtils.isBlank(hierarchyNode.getScope()) && !StringUtils.isBlank(cmqRelation190.getTermScope())
				&& !hierarchyNode.getScope().equals(cmqRelation190.getTermScope())) {
			needsUpdate = true;
		} else if(!StringUtils.isBlank(hierarchyNode.getCategory()) && !StringUtils.isBlank(cmqRelation190.getTermCategory())
				&& !hierarchyNode.getCategory().equals(cmqRelation190.getTermCategory())) {
			needsUpdate = true;
		} else {
			long nodeWeight = 0;
			if((null != cmqRelation190.getTermWeight()) && !StringUtils.isBlank(hierarchyNode.getWeight()) 
					&& StringUtils.isNumeric(hierarchyNode.getWeight())) {
				nodeWeight = Long.parseLong(hierarchyNode.getWeight());
				if(nodeWeight != cmqRelation190.getTermWeight().longValue()) {
					needsUpdate = true;
				}
			} 
		}
		return needsUpdate;
	}
	
	//
	// set workflow state CMQ_BASE_CURRENT -> CMQ_STATE
	//
	public String workflowState(String state) {
		LOG.info("\n OLD STATE :" + selectedData.getCmqState());

		if (state.equals("Retire") && selectedData.getCmqState().equals("Active")) {
			setState("Inactive");
		}

		// Deletes record
		if (state.equals("Delete")) {
			try {
				// delete the relations
				List<CmqRelation190> existingRelation = this.cmqRelationService.findByCmqCode(selectedData.getCmqCode());
				Set<Long> ids = new HashSet<Long>();
				for(CmqRelation190 r: existingRelation) {
					ids.add(r.getId());
				}
				this.cmqRelationService.remove(ids);
				
				// delete the record
				cmqBaseService.remove(selectedData.getCmqId());

				FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "Record deleted!'" + state + "'", "");
				FacesContext ctx = FacesContext.getCurrentInstance();
				ctx.addMessage(null, msg);

				selectedData = new CmqBase190();
				initAll();

			} catch (CqtServiceException e) {
				e.printStackTrace();
				FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR,
						"An error occured while deleting the state", "");
				FacesContext ctx = FacesContext.getCurrentInstance();
				ctx.addMessage(null, msg);
			}

			return "";
		}

		setState(state);
		selectedData.setCmqState(state);

		// Update
		try {
			cmqBaseService.update(selectedData);
			LOG.info("\n NEW STATE :" + selectedData.getCmqState());

			FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "Workflow state set to '" + state + "'",
					"");
			FacesContext ctx = FacesContext.getCurrentInstance();
			ctx.addMessage(null, msg);

		} catch (CqtServiceException e) {
			e.printStackTrace();
			FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR,
					"An error occured while updating the state", "");
			FacesContext ctx = FacesContext.getCurrentInstance();
			ctx.addMessage(null, msg);
		}

		return state;
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
		
		if(copyWizard == null) {
			//we are not doing copy so change others.
			if (extension.equals("CPT") || extension.equals("DME"))
				setDrugProgram("No Program");
		
			if (extension.equals("CPT") || extension.equals("DME") || extension.equals("TME") || extension.equals("TR1"))
				setProtocol("No Protocol");
			
			if (extension.equals("CPT") || extension.equals("DME"))
				setProduct("No Product");
			
		}
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

	public Long getCodeSelected() {
		return codeSelected;
	}

	public void setCodeSelected(Long codeSelected) {
		this.codeSelected = codeSelected;
	}

	public TreeNode getRelationsRoot() {
		return relationsRoot;
	}

	public void setRelationsRoot(TreeNode relationsRoot) {
		this.relationsRoot = relationsRoot;
	}

	public ICmqRelation190Service getCmqRelationService() {
		return cmqRelationService;
	}

	public void setCmqRelationService(ICmqRelation190Service cmqRelationService) {
		this.cmqRelationService = cmqRelationService;
	}

	public String[] getSelectedDesignees() {
		return selectedDesignees;
	}

	public void setSelectedDesignees(String[] selectedDesignees) {
		this.selectedDesignees = selectedDesignees;
	}

	public boolean isReactivate() {
		if (selectedData != null && selectedData.getCmqStatus() != null && selectedData.getCmqStatus().equals("I"))
			return false;
		return true;
	}

	public void setReactivate(boolean reactivate) {
		this.reactivate = reactivate;
	}

	public boolean isRetire() {
		if (selectedData != null && selectedData.getCmqStatus() != null && selectedData.getCmqStatus().equals("A"))
			return false;
		return true;
	}

	public void setRetire(boolean retire) {
		this.retire = retire;
	}

	public boolean isDemote() {
		if (selectedData != null && selectedData.getCmqStatus() != null
				&& (selectedData.getCmqState().equals("Reviewed") || selectedData.getCmqState().equals("Approved")))
			return false;
		return true;
	}

	public void setDemote(boolean demote) {
		this.demote = demote;
	}

	public boolean isDelete() {
		if (selectedData != null && selectedData.getCmqStatus() != null && selectedData.getCmqState().equals("Draft"))
			return false;
		return true;
	}

	public void setDelete(boolean delete) {
		this.delete = delete;
	}

	public boolean isApprove() {
		if (selectedData != null && selectedData.getCmqStatus() != null
				&& selectedData.getCmqState().equals("Reviewed"))
			return false;
		return true;
	}

	public void setApprove(boolean approve) {
		this.approve = approve;
	}

	public boolean isReviewed() {
		if (selectedData != null && selectedData.getCmqStatus() != null && selectedData.getCmqState().equals("Draft"))
			return false;
		return true;
	}

	public void setReviewed(boolean reviewed) {
		this.reviewed = reviewed;
	}

	public Long getCodevalue() {
		return codevalue;
	}

	public void setCodevalue(Long codevalue) {
		this.codevalue = codevalue;
	}

	public Wizard getCreateWizard() {
		return createWizard;
	}

	public void setCreateWizard(Wizard createWizard) {
		this.createWizard = createWizard;
	}

}
