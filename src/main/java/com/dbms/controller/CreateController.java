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
import javax.faces.component.html.HtmlInputText;
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
import com.dbms.entity.cqt.dtos.MeddraDictReverseHierarchySearchDto;
import com.dbms.service.ICmqBase190Service;
import com.dbms.service.ICmqRelation190Service;
import com.dbms.service.IRefCodeListService;
import com.dbms.util.exceptions.CqtServiceException;
import com.dbms.view.ListDetailsFormModel;
import com.dbms.view.ListDetailsFormModel.WizardType;
import com.dbms.view.ListNotesFormModel;
import com.dbms.view.ListWorkflowFormModel;

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
	
	private ListDetailsFormModel detailsFormModel = new ListDetailsFormModel();
	private ListNotesFormModel notesFormModel = new ListNotesFormModel();
	private ListWorkflowFormModel workflowFormModel = new ListWorkflowFormModel();
	private boolean relationsModified;

	private boolean maintainDesigBtn;

	private Wizard updateWizard, copyWizard, browseWizard, createWizard;
	private String createWizardNextStep, copyWizardNextStep, updateWizardNextStep;
	
	private Long copyingCmqCode = null;
	private Long codeSelected = null;

	private TreeNode relationsRoot;
	private String[] selectedDesignees;

	private Long codevalue;
	private CmqBase190 selectedData = new CmqBase190();
	
	private HtmlInputText dictionaryName;
	
	@PostConstruct
	public void init() {
		initAll();
	}

	private void initAll() {
		detailsFormModel.init();
		notesFormModel.init();
		workflowFormModel.init();
		relationsModified = false;
		maintainDesigBtn = false;
	}

	public void initCreateForm() {
		this.selectedData = new CmqBase190();
		selectedData.setCmqDescription("Please enter the description");
	}
	
	//----------------------- Browse Wizard ------------------------

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
		return !"searchBrowse".equals(browseWizard.getStep());
	}
	public boolean isBrowseWizardNavbarNextShown() {
		return isBrowseWizardNavbarShown() && !"relations".equals(browseWizard.getStep());
	}
	public boolean isBrowseWizardNavbarBackShown() {
		return !"searchBrowse".equals(browseWizard.getStep());
	}
	
	//----------------------- Common to Create/Copy/Update Wizard ------------------------
	
	private void goToWizardNextStep() {
		if(createWizard != null)
			createWizard.setStep(createWizardNextStep);
		else if(copyWizard != null)
			copyWizard.setStep(copyWizardNextStep);
		else if(updateWizard != null)
			updateWizard.setStep(updateWizardNextStep);
	}

	public void cancelDetailsAndNextStep() {
		if(createWizard != null && codeSelected == null) {
			createWizard.setStep("details");
		} else {
			cancel();
			goToWizardNextStep();
		}
	}
	
	/**
	 * Reset the "Details" form
	 * @return
	 */
	public String cancel() {
		detailsFormModel.loadFromCmqBase190(selectedData);

		FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_WARN, "Form canceled", "");
		FacesContext.getCurrentInstance().addMessage(null, msg);
		return "";
	}
	
	public void saveDetailsAndNextStep() {
		if(createWizard != null)
			save();
		else if(copyWizard != null)
			copy();
		else if(updateWizard != null)
			update();
		
		goToWizardNextStep();
	}


	/**
	 * Reset the "Informative Notes" form
	 * @return
	 */
	public String cancelNotes() {
		notesFormModel.loadFromCmqBase190(selectedData);
		
		FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_WARN, "Form canceled", "");
		FacesContext.getCurrentInstance().addMessage(null, msg);
		return "";
	}

	public void cancelNotesAndNextStep() {
		cancelNotes();
		goToWizardNextStep();
	}
	

	/**
	 * Save "Informative Notes" form
	 * @return
	 */
	public String saveInformativeNotes() {
		notesFormModel.saveToCmqBase190(selectedData);
		
		try {
			this.cmqBaseService.update(selectedData);
			this.notesFormModel.loadFromCmqBase190(selectedData);

			FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_INFO,
					"Informative Notes are successfully saved for '" + selectedData.getCmqName() + "'", "");
			FacesContext.getCurrentInstance().addMessage(null, msg);
		} catch (CqtServiceException e) {
			LOG.error("Exception occurred while updating CmqBase190 for add informative notes.", e);

			FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR,
					"An error occurred while trying to save Informative Notes.", "");
			FacesContext.getCurrentInstance().addMessage(null, msg);

			return null;
		}
		return "";
	}

	public void saveNotesAndNextStep() {
		saveInformativeNotes();
		goToWizardNextStep();
	}
	
	public void notifyRelationsUpdate() {
		relationsModified = true;
	}
	
	public void notifyRelationsReset() {
		relationsModified = false;
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
						} else if (entity instanceof MeddraDictReverseHierarchySearchDto) {
							MeddraDictReverseHierarchySearchDto searchDto = (MeddraDictReverseHierarchySearchDto)entity;
							Long code = null;
							if("PT".equalsIgnoreCase(hierarchyNode.getLevel())) {
								code = Long.parseLong(searchDto.getPtCode());
								matchingMap = this.checkIfReverseMeddraRelationExists(existingRelation, code, hierarchyNode);
							} else if ("LLT".equalsIgnoreCase(hierarchyNode.getLevel())) {
								code = Long.parseLong(searchDto.getLltCode());
								matchingMap = this.checkIfReverseMeddraRelationExists(existingRelation, code, hierarchyNode);
							}
							
							matchFound = (boolean) matchingMap.get("MATCH_FOUND");
							updateNeeded = (boolean) matchingMap.get("UPDATE_NEEDED");
							if(updateNeeded) {
								cmqRelation = (CmqRelation190) matchingMap.get("TARGET_CMQ_RELATION_FOR_UPDATE");
							} else if(!matchFound) {
								cmqRelation = new CmqRelation190();
								cmqRelation.setCmqCode(selectedData.getCmqCode());
								cmqRelation.setCmqId(cmqBase.getId());
								if("PT".equalsIgnoreCase(hierarchyNode.getLevel())) {
									cmqRelation.setPtCode(code);
								} else if ("LLT".equalsIgnoreCase(hierarchyNode.getLevel())) {
									cmqRelation.setLltCode(code);
								}
							}
						} else if (entity instanceof SmqBase190) {
							SmqBase190 smqBase = (SmqBase190) entity;
							matchingMap = this.checkIfSmqBaseOrSmqRelationExists(existingRelation, smqBase.getSmqCode(), null, hierarchyNode);
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
							matchingMap = this.checkIfSmqBaseOrSmqRelationExists(existingRelation, smqRelation.getSmqCode()
																					, smqRelation.getPtCode(), hierarchyNode);
							matchFound = (boolean) matchingMap.get("MATCH_FOUND");
							updateNeeded = (boolean) matchingMap.get("UPDATE_NEEDED");
							if(updateNeeded) {
								cmqRelation = (CmqRelation190) matchingMap.get("TARGET_CMQ_RELATION_FOR_UPDATE");
							} else if(!matchFound) {
								cmqRelation = new CmqRelation190();
								cmqRelation.setCmqCode(selectedData.getCmqCode());
								cmqRelation.setCmqId(cmqBase.getId());
								//we set both smqcode and pt code to show that this is an smq relation
								cmqRelation.setSmqCode(smqRelation.getSmqCode());
								cmqRelation.setPtCode(smqRelation.getPtCode().longValue());
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
					LOG.error("Exception occurred while updated the list of CmqRelations for CMQ base code "
							+ cmqBase.getCmqCode(), e);

					FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR,
							"An error occurred while updated the list of CmqRelations for CMQ base code "
									+ cmqBase.getCmqCode(),
							"");
					FacesContext.getCurrentInstance().addMessage(null, msg);
				}
			}
		}
		relationsModified = false;
		return "";
	}
	
	public void updateRelationsAndNextStep(TreeNode relationsRoot) {
		updateRelations(relationsRoot);
		goToWizardNextStep();
	}
	
	public void resetRelationsAndNextStep() {
		relationsModified = false;
		goToWizardNextStep();
	}
	
	//----------------------- Create Wizard ------------------------

	/**
	 * FlowListener of Create Wizard Component
	 * @param event
	 * @return
	 */
	public String onCreateWizardFlowProcess(FlowEvent event) {
		String oldStep, nextStep;
		oldStep = nextStep = event.getOldStep();
		if("details".equalsIgnoreCase(oldStep) && detailsFormModel.isModelChanged()) {
			// current step is "Details" and the form has some unsaved changes
			
			//----Confirmation on unsaved changes: see onUpdateWizardFlowProcess's "details" step
			if(codeSelected != null)
				createWizardNextStep = event.getNewStep();
			else
				createWizardNextStep = "details";
			RequestContext.getCurrentInstance().execute("PF('confirmSaveDetailsDlg').show();");
		} else if(codeSelected != null && "contact".equalsIgnoreCase(oldStep) && notesFormModel.isModelChanged()) {
			// current step is "Informative Notes" and the form has some unsaved changes			
			//----Confirmation on unsaved changes: see onUpdateWizardFlowProcess's "contact" step
			createWizardNextStep = event.getNewStep();
			RequestContext.getCurrentInstance().execute("PF('confirmSaveNotesDlg').show();");
		} else if(codeSelected != null && "relations".equalsIgnoreCase(oldStep) && relationsModified) {
			// current step is "Relations" and the form has some unsaved changes
			createWizardNextStep = event.getNewStep();
			RequestContext.getCurrentInstance().execute("PF('confirmSaveRelationsDlg').show();");
		} else if(codeSelected != null){
			nextStep = event.getNewStep();
		} else {
			nextStep = "details";
		}
		
		RequestContext.getCurrentInstance().update("fCreate:wizardNavbar");
		return nextStep;
	}
	public boolean isCreateWizardNavbarShown() {
		return true;
	}
	public boolean isCreateWizardNavbarNextShown() {
		return isCreateWizardNavbarShown() && !"confirmPanel".equals(createWizard.getStep());
	}
	public boolean isCreateWizardNavbarBackShown() {
		return !"details".equals(createWizard.getStep());
	}
	
	//----------------------- Update Wizard ------------------------

	/**
	 * FlowListener of Update Wizard Component
	 * @param event
	 * @return
	 */
	public String onUpdateWizardFlowProcess(FlowEvent event) {
		String oldStep, nextStep;
		oldStep = nextStep = event.getOldStep();
		if (codeSelected != null) {
			if("details".equalsIgnoreCase(oldStep) && detailsFormModel.isModelChanged()) {
				// current step is "Details" and the form has some unsaved changes
				
				//----Confirmation on unsaved changes
				// 1. here in CreateController.onUpdateWizardFlowProcess(), check if the Details form model has been modified
				
				// 2. if modified, execute the client side javascript that will show the confirmation dialog
				updateWizardNextStep = event.getNewStep();
				RequestContext.getCurrentInstance().execute("PF('confirmSaveDetailsDlg').show();");
				
				// 3. if client clicks on yes, it will call PF:RemoteCommand - updateDetailsAndGoToNextStep(), which will
				//	further call server side CreateController.updateDetailsAndNextStep()
				
				// 4. if client clicks on no, it will call PF:RemoteCommand - cancelDetailsAndGoToNextStep(), which will
				//	further call server side CreateController.cancelDetailsAndNextStep()
			} else if("contact".equalsIgnoreCase(oldStep) && notesFormModel.isModelChanged()) {
				// current step is "Informative Notes" and the form has some unsaved changes
				
				//----Confirmation on unsaved changes
				// 1. here in CreateController.onUpdateWizardFlowProcess(), check if the Notes form model has been modified
				
				// 2. if modified, execute the client side javascript that will show the confirmation dialog
				updateWizardNextStep = event.getNewStep();
				RequestContext.getCurrentInstance().execute("PF('confirmSaveNotesDlg').show();");
				
				// 3. if client clicks on yes, it will call PF:RemoteCommand - saveNotesAndGoToNextStep(), which will
				//	further call server side CreateController.saveNotesAndNextStep()
				
				// 4. if client clicks on no, it will call PF:RemoteCommand - cancelNotesAndGoToNextStep(), which will
				//	further call server side CreateController.cancelNotesAndNextStep()
			} else if("relations".equalsIgnoreCase(oldStep) && relationsModified) {
				// current step is "Relations" and the form has some unsaved changes
				updateWizardNextStep = event.getNewStep();
				RequestContext.getCurrentInstance().execute("PF('confirmSaveRelationsDlg').show();");
			} else {
				nextStep = event.getNewStep();
				if("details".equalsIgnoreCase(oldStep) && "searchUpdate".equalsIgnoreCase(nextStep) && !updateWizard.isBackRequest(FacesContext.getCurrentInstance())) {
					RequestContext.getCurrentInstance().execute("setTimeout(function(){PF('wizard').back();},100)");
					return oldStep;
				}
			}
		} else {
			nextStep = "searchUpdate";
		}
		RequestContext.getCurrentInstance().update("fUpdate:wizardNavbar");
		return nextStep;
	}

	public boolean isUpdateWizardNavbarShown() {
		return !"searchUpdate".equals(updateWizard.getStep());
	}
	public boolean isUpdateWizardNavbarNextShown() {
		return isUpdateWizardNavbarShown() && !"confirmPanel".equals(updateWizard.getStep());
	}
	public boolean isUpdateWizardNavbarBackShown() {
		return !"searchUpdate".equals(updateWizard.getStep());
	}
	
	/**
	 * "Update" module -> saving CMQ List record Details
	 * 
	 * @return
	 */
	public String update() {
		try {
			
			Long count = this.cmqBaseService.findCmqCountByCmqNameAndExtension(detailsFormModel.getExtension(), detailsFormModel.getName());
			
			if(count < 2) {
				//we should have atmost 1
				prepareDetailsFormSave();
				cmqBaseService.update(selectedData);
				
				// retrieve the saved cmq base
				CmqBase190 savedEntity = cmqBaseService.findByCode(selectedData.getCmqCode());		
				selectedData = savedEntity;
				detailsFormModel.loadFromCmqBase190(selectedData);

				// // save the cmq code to session
				FacesContext.getCurrentInstance().getExternalContext().getSessionMap().put("NEW-CMQ_BASE-ID",
						savedEntity.getId());

				FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_INFO,
						"List '" + selectedData.getCmqName() + "' is successfully saved.", "");
				FacesContext.getCurrentInstance().addMessage(null, msg);
			} else {
				String errorMsg = "Duplicate CMQ name ('"
						+ selectedData.getCmqName() + "') and extention ('"
						+ detailsFormModel.getExtension()
						+ "') found in db.";
				
				LOG.error(errorMsg);

				FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, errorMsg, "");
				FacesContext.getCurrentInstance().addMessage(null, msg);

				return null;
			}
		} catch (CqtServiceException e) {
			LOG.error("Exception occurred while updating CmqBase190.", e);

			// rollback the data changes to the db's state
			selectedData = cmqBaseService.findByCode(selectedData.getCmqCode());
			if(selectedData == null)
				selectedData = new CmqBase190();
			
			FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR,
					"An error occurred while trying to update the details.", "");
			FacesContext.getCurrentInstance().addMessage(null, msg);

			return null;
		}
		return "";
	}

	//----------------------- Copy Wizard ------------------------
	/**
	 * FlowListener of Update Wizard Component
	 * @param event
	 * @return
	 */
	public String onCopyWizardFlowProcess(FlowEvent event) {
		String oldStep, nextStep;
		oldStep = nextStep = event.getOldStep();

		if("details".equalsIgnoreCase(oldStep) && detailsFormModel.isModelChanged()) {
			// current step is "Details" and the form has some unsaved changes
			if("searchCopy".equals(event.getNewStep())) {
				nextStep = event.getNewStep();
			} else {
				//----Confirmation on unsaved changes: see onUpdateWizardFlowProcess's "details" step
				if (codeSelected != null)
					copyWizardNextStep = event.getNewStep();
				else
					copyWizardNextStep = "details";
				RequestContext.getCurrentInstance().execute("PF('confirmSaveDetailsDlg').show();");
			}
		} else if(codeSelected != null && "contact".equalsIgnoreCase(oldStep) && notesFormModel.isModelChanged()) {
			// current step is "Informative Notes" and the form has some unsaved changes
			//----Confirmation on unsaved changes: see onUpdateWizardFlowProcess's "contact" step
			copyWizardNextStep = event.getNewStep();
			RequestContext.getCurrentInstance().execute("PF('confirmSaveNotesDlg').show();");
		} else if(codeSelected != null && "relations".equalsIgnoreCase(oldStep) && relationsModified) {
			// current step is "Relations" and the form has some unsaved changes
			copyWizardNextStep = event.getNewStep();
			RequestContext.getCurrentInstance().execute("PF('confirmSaveRelationsDlg').show();");
		} else {
			if(codeSelected == null)
				nextStep = "searchCopy";
			else
				nextStep = event.getNewStep();
		}
		RequestContext.getCurrentInstance().update("fCopy:wizardNavbar");
		return nextStep;
	}

	public boolean isCopyWizardNavbarShown() {
		return !"searchCopy".equals(copyWizard.getStep());
	}
	public boolean isCopyWizardNavbarNextShown() {
		return isCopyWizardNavbarShown() && !"confirmPanel".equals(copyWizard.getStep());
	}
	public boolean isCopyWizardNavbarBackShown() {
		return !"searchCopy".equals(copyWizard.getStep());
	}
	
	/**
	 * "Copy" module -> saving CMQ List record Details
	 * 
	 * @return
	 */
	public String copy() {
		if(selectedData.getId() != null) { // if already saved
			return update();
		} 
		
		boolean cmqSaved = false;
		boolean savedRelations = false;
		Long savedCmqId = null;
		Long savedCmqCode = null;
		try {
			prepareDetailsFormSave();
			
			cmqBaseService.create(selectedData);
			
			// retrieve the saved cmq base
			CmqBase190 savedEntity = cmqBaseService.findByCode(selectedData.getCmqCode());
			cmqSaved = true;
			savedCmqId = selectedData.getId();
			savedCmqCode = selectedData.getCmqCode();
			
			LOG.info("Successfully saved new cmq with cmq id " + savedCmqId + " and cmq_code " + savedCmqCode);
			
			long copiedCode = copyingCmqCode;

			this.copyRelationsToNewCmq(copiedCode, savedEntity);
			savedRelations = true;
			
			this.copyChildCmqsToNewCmq(copiedCode, savedEntity);
			
			LOG.info("All updates completed.");		
			
			selectedData = savedEntity;
			this.detailsFormModel.loadFromCmqBase190(selectedData);
			this.detailsFormModel.setModelChanged(false);//model is saved now
			codeSelected = selectedData.getCmqCode();
			// save the cmq code to session
			FacesContext.getCurrentInstance().getExternalContext().getSessionMap().put("NEW-CMQ_BASE-ID",
					savedEntity.getId());

			LOG.info("populating relations table for new cmq with code " + savedCmqCode);
			
			FacesContext context = FacesContext.getCurrentInstance();
			SearchController searchController = context.getApplication()
															.evaluateExpressionGet(context, "#{searchController}", SearchController.class);
			if(null != searchController) {
				searchController.setClickedCmqCode(savedCmqCode);
				FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_INFO,
						"List '" + selectedData.getCmqName() + "', its relations and children are successfully saved.", "");
				FacesContext.getCurrentInstance().addMessage(null, msg);
			} else {
				LOG.error("Failed to get reference of SearchController to populate the realtions table.");
				FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_INFO,
						"List '" + selectedData.getCmqName() + "', its relations and children are successfully saved "
								+ "but Failed to get reference of SearchController to populate the realtions table.", "");
				FacesContext.getCurrentInstance().addMessage(null, msg);
			}
		} catch (CqtServiceException e) {
			// roll back the selectedData
			selectedData = cmqBaseService.findByCode(copyingCmqCode);
			if(selectedData == null)
				selectedData = new CmqBase190();
			setCopiedCmq(selectedData);
			//
			LOG.error("Exception occurred while creating CmqBase190.", e);
			//delete the saves if any
			if(savedRelations) {
				List<CmqRelation190> cmqRelationList = this.cmqRelationService.findByCmqCode(savedCmqCode);
				for (CmqRelation190 cmqRelation190 : cmqRelationList) {
					try {
						this.cmqRelationService.remove(cmqRelation190);
					} catch (CqtServiceException e1) {
						LOG.error("Exception occurred while deleting relation with relation id " + cmqRelation190.getId(), e);
					}
				}
			}
			
			if(cmqSaved) {
				try {
					this.cmqBaseService.remove(savedCmqId);
				} catch (CqtServiceException e1) {
					LOG.error("Exception occurred while deleting cmq with id " + savedCmqId, e);
				}
			}

			FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR,
					"An error occurred while trying to save the details. Exception is [" + e.getMessage() + "]", "");
			FacesContext.getCurrentInstance().addMessage(null, msg);

			return null;
		}
		return "";
	}
	
	//----------------------- Create Wizard ------------------------
	
	/**
	 * "Create" module -> saving CMQ List record Details
	 * 
	 * @return
	 */
	public String save() {
		if(selectedData.getId() != null) { // if already saved
			return update();
		}
		try {
			Long count = this.cmqBaseService.findCmqCountByCmqNameAndExtension(detailsFormModel.getExtension(), detailsFormModel.getName());

			if (count > 0) {
				String errorMsg = "Duplicate CMQ name ('"
						+ selectedData.getCmqName() + "') and extention ('"
						+ detailsFormModel.getExtension()
						+ "') found in db.";
				
				LOG.error(errorMsg);

				FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, errorMsg, "");
				FacesContext.getCurrentInstance().addMessage(null, msg);

				return null;
			} else {
				prepareDetailsFormSave();
				cmqBaseService.create(selectedData);

				// retrieve the saved cmq base
				CmqBase190 savedEntity = cmqBaseService.findByCode(codevalue);
				selectedData = savedEntity;
				this.detailsFormModel.loadFromCmqBase190(selectedData);
				codeSelected = selectedData.getCmqCode();

				// save the cmq code to session
				FacesContext.getCurrentInstance().getExternalContext().getSessionMap().put("NEW-CMQ_BASE-ID",
						savedEntity.getId());

				FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_INFO,
						"List '" + selectedData.getCmqName() + "' is successfully saved.", "");
				FacesContext.getCurrentInstance().addMessage(null, msg);
			}
		} catch (CqtServiceException e) {
			LOG.error("Exception occurred while creating CmqBase190.", e);
			if(selectedData.getId() == null) {
				// since it failed to save the record, clear the creation date/user info
				selectedData = new CmqBase190();
			}

			FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR,
					"An error occurred while trying to save the details.", "");
			FacesContext.getCurrentInstance().addMessage(null, msg);

			return null;
		}
		return "";
	}
	

	/**
	 * Bool when State is 'Draft' or 'Reviewed'.
	 * @return boolean
	 */
	public boolean isReadOnlyState() {
		if(copyWizard != null) {	
			return false;
		} else if (updateWizard != null) {
			if (selectedData != null && selectedData.getCmqState() != null 
					&& ("DRAFT".equalsIgnoreCase(selectedData.getCmqState())
							|| "REVIEWED".equalsIgnoreCase(selectedData.getCmqState()))){
				return false;
			} else if (selectedData != null && selectedData.getCmqState() == null) {
				return false;
			} else {
				return true;
			}
		} 
		return false;
	}
	
	/**
	 * Load a single CMQ List record for "Details/Informative Notes/Relations/Confirm" tabs
	 * 
	 * @param code
	 * @return
	 */
	public String loadCmqBaseByCode(Long code) {
		codeSelected = null;
		CmqBase190 cmq = new CmqBase190();
		cmq = this.cmqBaseService.findByCode(code);

		if (cmq != null) {
			codeSelected = cmq.getCmqCode();
			selectedData = cmq;
		}
		if(createWizard != null) {
			detailsFormModel.setWizardType(WizardType.CreateWizard);
		}

		if (browseWizard != null) {
			detailsFormModel.setWizardType(WizardType.BrowseWizard);
			browseWizard.setStep("details");
			if(RequestContext.getCurrentInstance() != null) {
				// UI: force update the custom wizard navbar area
				RequestContext.getCurrentInstance().update("fBrowse:wizardNavbar");
			}
		}
		
		if (updateWizard != null) {
			detailsFormModel.setWizardType(WizardType.UpdateWizard);
			updateWizard.setStep("details");
			if(RequestContext.getCurrentInstance() != null) {
				// UI: force update the custom wizard navbar area
				RequestContext.getCurrentInstance().update("fUpdate:wizardNavbar");
			}
			// selectedData = new CmqBase190();
			// setSelectedData(cmq);
		}
		
		if (copyWizard != null) {
			detailsFormModel.setWizardType(WizardType.CopyWizard);
			//reset the values which are not supposed to be copied.
			copyingCmqCode = codeSelected;
			codeSelected = null;
			setCopiedCmq(selectedData);
			
			String name = selectedData.getCmqName();
			if(name.contains("-Copy-")) {
				String num = name.substring(name.lastIndexOf("-") + 1);
				int i = Integer.valueOf(num);
				name = name.substring(0, name.lastIndexOf("-"));
				name += "-" + (++i);
			} else if(name.endsWith("-Copy")) {
				name += "-1";
			} else {
				name += "-Copy";
			}
			
			selectedData.setCmqName(name);
			// set the details form model changed status to true
			copyWizard.setStep("details");
			
			if(RequestContext.getCurrentInstance() != null) {
				// UI: force update the custom wizard navbar area
				RequestContext.getCurrentInstance().update("fCopy:wizardNavbar");
			}
		}
		
		detailsFormModel.loadFromCmqBase190(selectedData);
		notesFormModel.loadFromCmqBase190(selectedData);
		workflowFormModel.loadFromCmqBase190(selectedData);

		return "";
	}
	
	private void copyRelationsToNewCmq(Long copiedCode, CmqBase190 savedEntity) throws CqtServiceException {
		LOG.info("Saving relations for the new cmq.");
		//copy relations now
		List<CmqRelation190> cmqRelationList = this.cmqRelationService.findByCmqCode(copiedCode);
		Date creationDate = new Date();
		for (CmqRelation190 cmqRelation190 : cmqRelationList) {
			cmqRelation190.setCmqRelationId(null);
			cmqRelation190.setCmqCode(savedEntity.getCmqCode());
			cmqRelation190.setCmqId(savedEntity.getCmqId());
			cmqRelation190.setLastModifiedDate(null);
			cmqRelation190.setLastModifiedBy(null);
			cmqRelation190.setCreationDate(creationDate);
			cmqRelation190.setCreatedBy("Test-User");
		}
		//save relations
		this.cmqRelationService.update(cmqRelationList);
		LOG.info("Cmq relations saved.");
	}
	
	private void copyChildCmqsToNewCmq(Long copiedCode, CmqBase190 savedEntity) throws CqtServiceException{
		LOG.info("Saving child cmqs for the new cmq.");
		//save the children now
		List<CmqBase190> childCmqs = this.cmqBaseService.findChildCmqsByParentCode(copiedCode);
		Date creationDate = new Date();
		for (CmqBase190 childCmq : childCmqs) {
			childCmq.setId(null);
			//get and set new code value
			Long codeValue = this.cmqBaseService.getNextCodeValue();
			childCmq.setCmqCode(codeValue);
			
			//set new child name
			String newChildCmqName = childCmq.getCmqName();
			if(newChildCmqName.contains("-Copy-")) {
				String num = newChildCmqName.substring(newChildCmqName.lastIndexOf("-") + 1);
				int i = Integer.valueOf(num);
				newChildCmqName = newChildCmqName.substring(0, newChildCmqName.lastIndexOf("-"));
				newChildCmqName += "-" + (++i);
			} else if(newChildCmqName.endsWith("-Copy")) {
				newChildCmqName += "-1";
			} else {
				newChildCmqName += "-Copy";
			}
			childCmq.setCmqName(newChildCmqName);
			childCmq.setCmqParentCode(savedEntity.getCmqCode());
			childCmq.setCmqParentName(savedEntity.getCmqName());
			childCmq.setCreatedBy("Test-User");
			childCmq.setCreationDate(creationDate);
			childCmq.setLastModifiedBy(null);
			childCmq.setLastModifiedDate(null);
		}
		//save children
		this.cmqBaseService.update(childCmqs);
		LOG.info("Cmq children saved.");
	}
	
	/**
	 * Sets the selectedData from ListDetailsFormModel
	 * @throws CqtServiceException
	 */
	private void prepareDetailsFormSave() throws CqtServiceException {
		// fill data
		if((createWizard != null || copyWizard != null) && selectedData.getId() == null) {
			// get the next value of code
			codevalue = this.cmqBaseService.getNextCodeValue();
			RefConfigCodeList currentMeddraVersionCodeList = this.refCodeListService.getCurrentMeddraVersion();
			
			if(createWizard != null)
				detailsFormModel.setWizardType(WizardType.CreateWizard);
			else if(copyWizard != null)
				detailsFormModel.setWizardType(WizardType.CopyWizard);
			
			selectedData.setCmqCode(codevalue);
			selectedData.setDictionaryVersion(currentMeddraVersionCodeList.getValue());
			// hard coded for now
			selectedData.setCreatedBy("Test user");
			if (dictionaryName != null && dictionaryName.getValue() != null)
				selectedData.setDictionaryName((String) dictionaryName.getValue());
			else
				selectedData.setDictionaryName("");
			selectedData.setCmqSubversion(new BigDecimal(0.23d));
			notesFormModel.saveToCmqBase190(selectedData);
		} else if(updateWizard != null) {
			detailsFormModel.setWizardType(WizardType.UpdateWizard);
		}
		
		detailsFormModel.saveToCmqBase190(selectedData);
	}

	/**
	 * Reset the "Relations" tab
	 * @return
	 */
	public String cancelRelations() {
		if (selectedData.getId() == null) {
			selectedData = new CmqBase190();
		}
		FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_WARN, "Form canceled", "");
		FacesContext.getCurrentInstance().addMessage(null, msg);
		return "";
	}

	/**
	 * Save relations on a list.
	 * 
	 * @return
	 */
	public String saveRelations(TreeNode relationsRoot) {
		return this.updateRelations(relationsRoot);
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
	
	private Map<String, Object> checkIfSmqBaseOrSmqRelationExists(List<CmqRelation190> existingRelation, Long smqCode, Integer ptCode
																		, HierarchyNode hierarchyNode) {
		Map<String, Object> matchingMap = new HashMap<String, Object>(3);
		matchingMap.put("MATCH_FOUND", false);
		matchingMap.put("UPDATE_NEEDED", false);
		matchingMap.put("TARGET_CMQ_RELATION_FOR_UPDATE", null);
		
		for (CmqRelation190 cmqRelation190 : existingRelation) {
			if((null != ptCode) && (null != cmqRelation190.getSmqCode()) && (cmqRelation190.getSmqCode().longValue() == smqCode.longValue())
						&& (null != cmqRelation190.getPtCode()) && (cmqRelation190.getPtCode().longValue() == ptCode.longValue())){
				//its an smqrelation and not an smqbase
				matchingMap.put("MATCH_FOUND", true);
			} else if((null != cmqRelation190.getSmqCode()) && (cmqRelation190.getSmqCode().longValue() == smqCode.longValue())){
				//its an smqbase
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
	
	private Map<String, Object> checkIfReverseMeddraRelationExists(List<CmqRelation190> existingRelation, Long code, HierarchyNode hierarchyNode) {
		Map<String, Object> matchingMap = new HashMap<String, Object>(3);
		matchingMap.put("MATCH_FOUND", false);
		matchingMap.put("UPDATE_NEEDED", false);
		matchingMap.put("TARGET_CMQ_RELATION_FOR_UPDATE", null);
		
		for (CmqRelation190 cmqRelation190 : existingRelation) {
			//check for PT or LLT
			if((null != cmqRelation190.getPtCode()) && (cmqRelation190.getPtCode().longValue() == code.longValue())){
				matchingMap.put("MATCH_FOUND", true);
			} else if ((null != cmqRelation190.getLltCode()) && (cmqRelation190.getLltCode().longValue() == code.longValue())) {
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
		//first match scope
		if(StringUtils.isBlank(hierarchyNode.getScope()) && !StringUtils.isBlank(cmqRelation190.getTermScope())) {
			needsUpdate = true;
		} else if(!StringUtils.isBlank(hierarchyNode.getScope()) && StringUtils.isBlank(cmqRelation190.getTermScope())) {
			needsUpdate = true;
		} else if(!StringUtils.isBlank(hierarchyNode.getScope()) && !StringUtils.isBlank(cmqRelation190.getTermScope())
				&& !hierarchyNode.getScope().equals(cmqRelation190.getTermScope())){
			needsUpdate = true;
		}
		
		//now so category
		if(StringUtils.isBlank(hierarchyNode.getCategory()) && !StringUtils.isBlank(cmqRelation190.getTermCategory())) {
			needsUpdate = true;
		} else if(!StringUtils.isBlank(hierarchyNode.getCategory()) && StringUtils.isBlank(cmqRelation190.getTermCategory())) {
			needsUpdate = true;
		} else if(!StringUtils.isBlank(hierarchyNode.getCategory()) && !StringUtils.isBlank(cmqRelation190.getTermCategory())
				&& !hierarchyNode.getCategory().equals(cmqRelation190.getTermCategory())){
			needsUpdate = true;
		}
		
		//now weight
		
		if(StringUtils.isBlank(hierarchyNode.getWeight()) && (null != cmqRelation190.getTermWeight())) {
			needsUpdate = true;
		} else if(!StringUtils.isBlank(hierarchyNode.getWeight()) && (null == cmqRelation190.getTermWeight())) {
			needsUpdate = true;
		} else if(!StringUtils.isBlank(hierarchyNode.getWeight()) && (null != cmqRelation190.getTermWeight()) 
				&& StringUtils.isNumeric(hierarchyNode.getWeight())) {
			long nodeWeight = Long.parseLong(hierarchyNode.getWeight());
			if(nodeWeight != cmqRelation190.getTermWeight().longValue()) {
				needsUpdate = true;
			}
		}
		
		return needsUpdate;
	}
	
	//
	// set workflow state CMQ_BASE_CURRENT -> CMQ_STATE
	//
	public String workflowState(String state) {
		LOG.info("\n OLD STATE :" + selectedData.getCmqState());
		
		if (state.equals("Delete")) { // Deletes record
			try {
				// delete the relations
				List<CmqRelation190> existingRelation = this.cmqRelationService.findByCmqCode(selectedData.getCmqCode());
				Set<Long> ids = new HashSet<Long>();
				for(CmqRelation190 r: existingRelation) {
					ids.add(r.getId());
				}
				if(!ids.isEmpty())
					this.cmqRelationService.remove(ids);
				
				// delete the record
				cmqBaseService.remove(selectedData.getCmqId());

				FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "Record has been deleted!", "");
				FacesContext ctx = FacesContext.getCurrentInstance();
				ctx.addMessage(null, msg);

				selectedData = new CmqBase190();
				codeSelected = null;
				initAll();

			} catch (CqtServiceException e) {
				e.printStackTrace();
				FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR,
						"An error occurred while deleting the state", "");
				FacesContext ctx = FacesContext.getCurrentInstance();
				ctx.addMessage(null, msg);
			}

			return "";
		} else if (state.equals("Retire")) { // Retires a record
			if(selectedData.getCmqStatus().equals(CmqBase190.CMQ_STATUS_VALUE_ACTIVE)
					&& selectedData.getCmqState().equalsIgnoreCase(CmqBase190.CMQ_STATE_VALUE_PUBLISHED)) {
				selectedData.setCmqStatus(CmqBase190.CMQ_STATUS_VALUE_INACTIVE);
			}
		} else {
			detailsFormModel.setState(state);
			selectedData.setCmqState(state);
		}
		
		//Adding the due date to be updated
		workflowFormModel.saveToCmqBase190(selectedData);
		
		// Update
		try {
			cmqBaseService.update(selectedData);
			LOG.info("\n NEW STATE :" + selectedData.getCmqState());

			FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "Workflow state set to '" + state + "'",
					"");
			FacesContext ctx = FacesContext.getCurrentInstance();
			ctx.addMessage(null, msg);
			
			//Clearing workflow attributes : due date, reason for request, reason for approval
			this.workflowFormModel = new ListWorkflowFormModel();

		} catch (CqtServiceException e) {
			e.printStackTrace();
			FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR,
					"An error occurred while updating the state", "");
			FacesContext ctx = FacesContext.getCurrentInstance();
			ctx.addMessage(null, msg);
		}

		return state;
	}
	
	public void changeLevel(AjaxBehaviorEvent event) {
		if(createWizard != null) {
			detailsFormModel.setWizardType(WizardType.CreateWizard);
		} else if(copyWizard != null) {
			detailsFormModel.setWizardType(WizardType.CopyWizard);
		} else if(updateWizard != null) {
			detailsFormModel.setWizardType(WizardType.UpdateWizard);
		}
		detailsFormModel.changeLevel(event);
	}

	
	//--------------------- Getters and Setters -----------------------
	
	/**
	 * Details form Model
	 * @return
	 */
	public ListDetailsFormModel getDetailsFormModel() {
		return this.detailsFormModel;
	}
	public void setDetailsFormModel(ListDetailsFormModel model) {
		this.detailsFormModel = model;
	}
	
	/**
	 * "Informative Notes" form model
	 * @return
	 */
	public ListNotesFormModel getNotesFormModel() {
		return this.notesFormModel;
	}
	public void setNotesFormModel(ListNotesFormModel model) {
		this.notesFormModel = model;
	}
	
	/**
	 * "Confirm / Workflow" form model
	 * @return
	 */
	public ListWorkflowFormModel getWorkflowFormModel() {
		return this.workflowFormModel;
	}
	public void setWorkflowFormModel(ListWorkflowFormModel model) {
		this.workflowFormModel = model;
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
	
	/**
	 * returns if relations can be added/removed in "Relations" tab
	 * @return
	 */
	public boolean isRelationsReadonly() {
		return (!CmqBase190.CMQ_STATE_VALUE_DRAFT.equalsIgnoreCase(selectedData.getCmqState())
				&& !CmqBase190.CMQ_STATE_VALUE_REVIEWED.equalsIgnoreCase(selectedData.getCmqState()));
	}
	
	/**
	 * returns if the current list is a child list and has a viewable parent list.
	 * @return
	 */
	public boolean isParentViewable() {
        return (selectedData.getCmqLevel() > 1
                && selectedData.getCmqParentCode()!=null);
	}

	public boolean isReactivateDisabled() {
		if (selectedData != null
				&& CmqBase190.CMQ_STATUS_VALUE_INACTIVE.equals(selectedData.getCmqStatus()))
			return false;
		return true;
	}

	/**
	 * returns if the retire button should be disabled or not. (true for disabled)
	 * The retire button will be enabled only when lists Status is A (active).
	 * 
	 * @return true if it is UNAVAILABLE for retire
	 */
	public boolean isRetireDisabled() {
		if (selectedData != null
				&& CmqBase190.CMQ_STATUS_VALUE_ACTIVE.equals(selectedData.getCmqStatus()))
			return false;
		return true;
	}

	public boolean isDemoteDisabled() {
		if (selectedData != null
				&& (CmqBase190.CMQ_STATE_VALUE_REVIEWED.equalsIgnoreCase(selectedData.getCmqState())
						|| CmqBase190.CMQ_STATE_VALUE_APPROVED.equalsIgnoreCase(selectedData.getCmqState())))
			return false;
		return true;
	}

	public boolean isDeleteDisabled() {
		if (selectedData == null
				|| !CmqBase190.CMQ_STATE_VALUE_DRAFT.equalsIgnoreCase(selectedData.getCmqState())
				|| selectedData.getActivationDate() != null)
			return true;
		return false;
	}

	public boolean isApproveDisabled() {
		if (selectedData != null && selectedData.getCmqStatus() != null
				&& CmqBase190.CMQ_STATE_VALUE_REVIEWED.equalsIgnoreCase(selectedData.getCmqState()))
			return false;
		return true;
	}

	public boolean isReviewedDisabled() {
		if (selectedData != null
				&& CmqBase190.CMQ_STATE_VALUE_DRAFT.equalsIgnoreCase(selectedData.getCmqState()))
			return false;
		return true;
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

	public HtmlInputText getDictionaryName() {
		return dictionaryName;
	}

	public void setDictionaryName(HtmlInputText dictionaryName) {
		this.dictionaryName = dictionaryName;
	}
	
	private Wizard getActiveWizard() {
		if(createWizard != null)
			return createWizard;
		else if(copyWizard != null)
			return copyWizard;
		else if(updateWizard != null)
			return updateWizard;
		return null;
	}

	private void setCopiedCmq(CmqBase190 cmq) {
		cmq.setId(null);//need to set since we may need to create a new cmq
		cmq.setCmqCode(null);//need to set since we may need to create a new cmq
		cmq.setCmqStatus(CmqBase190.CMQ_STATUS_VALUE_PENDING);
		cmq.setCmqState(CmqBase190.CMQ_STATE_VALUE_DRAFT);
		cmq.setCmqGroup("No Group");
		cmq.setCreationDate(null);
		cmq.setCreatedBy(null);
		cmq.setLastModifiedBy(null);
		cmq.setLastModifiedDate(null);
	}

}
