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
import com.dbms.entity.cqt.CmqBaseTarget;
import com.dbms.entity.cqt.CmqProductBaseCurrent;
import com.dbms.entity.cqt.CmqRelation190;
import com.dbms.entity.cqt.RefConfigCodeList;
import com.dbms.entity.cqt.SmqBase190;
import com.dbms.entity.cqt.SmqRelation190;
import com.dbms.entity.cqt.dtos.MeddraDictHierarchySearchDto;
import com.dbms.entity.cqt.dtos.MeddraDictReverseHierarchySearchDto;
import com.dbms.entity.cqt.dtos.SMQReverseHierarchySearchDto;
import com.dbms.service.AuthenticationService;
import com.dbms.service.ICmqBase190Service;
import com.dbms.service.ICmqBaseTargetService;
import com.dbms.service.ICmqRelation190Service;
import com.dbms.service.ICqtCacheManager;
import com.dbms.service.IMeddraDictService;
import com.dbms.service.IRefCodeListService;
import com.dbms.service.ISmqBaseService;
import com.dbms.util.CmqUtils;
import com.dbms.util.CqtConstants;
import com.dbms.util.OrderBy;
import com.dbms.util.SWJSFRequest;
import com.dbms.util.exceptions.CqtServiceException;
import com.dbms.view.ListDetailsFormVM;
import com.dbms.view.ListDetailsFormVM.WizardType;
import com.dbms.view.ListNotesFormVM;
import com.dbms.view.ListRelationsVM;
import com.dbms.view.ListWorkflowFormVM;

/**
 * @author Jay G.(jayshanchn@hotmail.com)
 * @date Feb 12, 2017 7:34:05 AM
 **/
@ManagedBean
@ViewScoped
public class CreateController implements Serializable {

	private static final long serialVersionUID = -443251941538546278L;

	private static final Logger LOG = LoggerFactory.getLogger(CreateController.class);
    
    private static final String UPDATE_WIZARD_STEP_SEARCH = "searchUpdate";
    private static final String BROWSE_WIZARD_STEP_SEARCH = "searchBrowse";
    private static final String COPY_WIZARD_STEP_SEARCH = "searchCopy";
    private static final String WIZARD_STEP_DETAILS = "details";
    private static final String WIZARD_STEP_INFONOTES = "infonotes";
    private static final String WIZARD_STEP_RELATIONS = "relations";
    private static final String WIZARD_STEP_CONFIRM = "confirmPanel";
    

	@ManagedProperty("#{CmqBase190Service}")
	private ICmqBase190Service cmqBaseService;

	@ManagedProperty("#{CmqRelation190Service}")
	private ICmqRelation190Service cmqRelationService;
    
    @ManagedProperty("#{SmqBaseService}")
	private ISmqBaseService smqBaseService;

	@ManagedProperty("#{RefCodeListService}")
	private IRefCodeListService refCodeListService;
    
    @ManagedProperty("#{MeddraDictService}")
	private IMeddraDictService meddraDictService;
    
    @ManagedProperty("#{AuthenticationService}")
	private AuthenticationService authService;
	
    @ManagedProperty("#{appSWJSFRequest}")
    private SWJSFRequest appSWJSFRequest;
    
    @ManagedProperty("#{CmqBaseTargetService}")
    private ICmqBaseTargetService myCmqTargetService;
    
    @ManagedProperty("#{CqtCacheManager}")
	private ICqtCacheManager cqtCacheManager;
	
    
	private ListDetailsFormVM detailsFormModel;
	private ListNotesFormVM notesFormModel = new ListNotesFormVM();
    private ListRelationsVM relationsModel;
	private ListWorkflowFormVM workflowFormModel;
	private boolean relationsModified;

	private boolean maintainDesigBtn;

	private Wizard updateWizard, copyWizard, browseWizard, createWizard;
	private String createWizardNextStep, copyWizardNextStep, updateWizardNextStep;
	
	private Long copyingCmqCode = null;
	private Long codeSelected = null;

	private TreeNode relationsRoot;
	private String[] selectedDesignees;

	private Long codevalue;
	private CmqBase190 selectedData;
    private boolean isSelectedDataApprovedOnce;
    private CmqBaseTarget mySelectedCmqTarget = new CmqBaseTarget();
	
	private HtmlInputText dictionaryName;
	private boolean	formSaved;
	private String listCreator;
	
	private List<RefConfigCodeList> products;
	
	private final String CACHE_NAME = "code-list-cache";
	
	/**
	 * Filter for scope on relations BROWSE AND CREATE/UPDATE/COPY
	 */
	private String scopeFilter;
	private static final String NO_SCOPE_FILTER = "-1";

	
	
	public CreateController() {
		setSelectedData(null);
	}
	
	@PostConstruct
	public void init() {
  		this.detailsFormModel  = new ListDetailsFormVM(this.authService, this.refCodeListService, this.appSWJSFRequest);
        this.relationsModel = new ListRelationsVM(authService, appSWJSFRequest, refCodeListService, cmqBaseService, smqBaseService, meddraDictService, cmqRelationService);
        this.workflowFormModel = new ListWorkflowFormVM(this.authService);
		initAll();
	}

	private void initAll() {
		detailsFormModel.init();
		notesFormModel.init();
		workflowFormModel.init();
		relationsModified = false;
		maintainDesigBtn = false;
		formSaved = false;
		
		//Init of products when navigationg from UPDATE Details
		products = new ArrayList<>();
	}
	
	public void resetHS() {
//		myHierarchyRoot = new DefaultTreeNode("root", new HierarchyNode("LEVEL",
//				"NAME", "CODE", null), null);
//		setFilterLevel("PT");
//		setFilterTermName(""); 
//		RequestContext.getCurrentInstance().update("impactAssessment:levelH_label");
	}

	public void initCreateForm() {
		setSelectedData(null);
		selectedData.setCmqDescription("Please enter the description");	
		//RequestContext.getCurrentInstance().execute("PF('confirmSaveDetailsDlg').show();");

	}
	
	public void expandRelations(AjaxBehaviorEvent event) {
		collapsingORexpanding(relationsModel.getRelationsRoot(), true);
	}

	public void collapseRelations(HierarchyNode node) {
		collapsingORexpanding(relationsModel.getRelationsRoot(), false);
	}

	public void collapsingORexpanding(TreeNode n, boolean option) {
		if (n != null && n.getChildren() != null && n.getChildren().size() == 0) {
			n.setSelected(false);
		}
		else {
			for (TreeNode s : n.getChildren()) {
				collapsingORexpanding(s, option);
			}
			n.setExpanded(option);
			n.setSelected(false);
		}
	}
	
	public void filterRelationsByScope(HierarchyNode node) {
		
		//For relations.xhtml
		if (updateWizard != null || copyWizard != null || createWizard != null) {
			IEntity entity = node.getEntity();
			if((entity instanceof SmqBase190) || (entity instanceof SMQReverseHierarchySearchDto)) {
				node.setDataFetchCompleted(false);
				this.relationsModel.clearChildrenInTreNode(relationsModel.getRelationsRoot(), node);
				collapseRelations(node);
			}
		}
		
		//For relationsForBrowse.xhtml
		if (browseWizard != null) {
			
		}
		 
 
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
		return !BROWSE_WIZARD_STEP_SEARCH.equals(browseWizard.getStep());
	}
	public boolean isBrowseWizardNavbarNextShown() {
		return isBrowseWizardNavbarShown() && !WIZARD_STEP_RELATIONS.equals(browseWizard.getStep());
	}
	public boolean isBrowseWizardNavbarBackShown() {
		return !BROWSE_WIZARD_STEP_SEARCH.equals(browseWizard.getStep());
	}
	
	//----------------------- Common to Create/Copy/Update Wizard ------------------------
	
	private void goToWizardNextStep() {
		if(createWizard != null) {
			createWizard.setStep(createWizardNextStep);
            RequestContext.getCurrentInstance().update("fCreate:wizardNavbar");
        } else if(copyWizard != null) {
			copyWizard.setStep(copyWizardNextStep);
            RequestContext.getCurrentInstance().update("fCopy:wizardNavbar");
        } else if(updateWizard != null) {
			updateWizard.setStep(updateWizardNextStep);
            RequestContext.getCurrentInstance().update("fUpdate:wizardNavbar");
        }
	}

	public void cancelDetailsAndNextStep() {
		if(createWizard != null && codeSelected == null) {
			createWizard.setStep(WIZARD_STEP_DETAILS);
		} else {
			cancel();
            if(copyWizard != null
                    && COPY_WIZARD_STEP_SEARCH.equals(copyWizardNextStep))
                copyingCmqCode = null;
			goToWizardNextStep();
		}
	}
	
	/**
	 * Reset the "Details" form
	 * @return
	 */
	public String cancel() {
		if(selectedData.getId() != null)
			detailsFormModel.loadFromCmqBase190(selectedData);
		else
			detailsFormModel.init();
//		if (copyWizard != null)
//			detailsFormModel.setProducts(new String[0]);

		FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_WARN, "Form canceled", "");
		FacesContext.getCurrentInstance().addMessage(null, msg);
		return "";
	}
	
	public void saveDetailsAndNextStep() {
        if(detailsFormModel.validateForm()) {
            if(createWizard != null)
                save();
            else if(copyWizard != null)
                copy();
            else if(updateWizard != null)
                update();

            goToWizardNextStep();
        }
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
		
		Date d = new Date();
		String lastModifiedByString = this.authService.getLastModifiedByUserAsString();
		selectedData.setLastModifiedDate(d);
		selectedData.setLastModifiedBy(lastModifiedByString);
		
		try {
			this.cmqBaseService.update(selectedData, this.authService.getUserCn()
					, this.authService.getUserGivenName(), this.authService.getUserSurName()
					, this.authService.getCombinedMappedGroupMembershipAsString());
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
			Date lastModifiedDate = new Date();
			String lastModifiedByString = this.authService.getLastModifiedByUserAsString();
			
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
						Long existingParentCode = cmqEntity.getCmqParentCode();
						if((null == existingParentCode) || (existingParentCode.longValue() != cmqBase.getCmqCode().longValue())) {
							//update only if needed
							cmqEntity.setCmqParentCode(cmqBase.getCmqCode());
							cmqEntity.setCmqParentName(cmqBase.getCmqName());
							cmqBaseChildrenList.add(cmqEntity);
						}
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
							} else if ("HLGT".equalsIgnoreCase(hierarchyNode.getLevel())) {
								code = Long.parseLong(searchDto.getHlgtCode());
								matchingMap = this.checkIfReverseMeddraRelationExists(existingRelation, code, hierarchyNode);
							} else if ("HLT".equalsIgnoreCase(hierarchyNode.getLevel())) {
								code = Long.parseLong(searchDto.getHltCode());
								matchingMap = this.checkIfReverseMeddraRelationExists(existingRelation, code, hierarchyNode);
							} else if ("SOC".equalsIgnoreCase(hierarchyNode.getLevel())) {
								code = Long.parseLong(searchDto.getSocCode());
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
						} else if (entity instanceof SMQReverseHierarchySearchDto) {
							SMQReverseHierarchySearchDto smqReverseHierarchySearchDto = (SMQReverseHierarchySearchDto) entity;
							matchingMap = this.checkIfSmqBaseOrSmqRelationExists(existingRelation, smqReverseHierarchySearchDto.getSmqCode()
																					, smqReverseHierarchySearchDto.getSmqCode().intValue(), hierarchyNode);
							matchFound = (boolean) matchingMap.get("MATCH_FOUND");
							updateNeeded = (boolean) matchingMap.get("UPDATE_NEEDED");
							if(updateNeeded) {
								cmqRelation = (CmqRelation190) matchingMap.get("TARGET_CMQ_RELATION_FOR_UPDATE");
							} else if(!matchFound) {
								cmqRelation = new CmqRelation190();
								cmqRelation.setCmqCode(selectedData.getCmqCode());
								cmqRelation.setCmqId(cmqBase.getId());
								//we set both smqcode and pt code to show that this is an smq relation
								cmqRelation.setSmqCode(smqReverseHierarchySearchDto.getSmqCode());
								//cmqRelation.setPtCode(smqReverseHierarchySearchDto.getSmqCode());
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
							if(!updateNeeded) {
								cmqRelation.setCreationDate(lastModifiedDate);
								cmqRelation.setCreatedBy(lastModifiedByString);
							}
							cmqRelation.setLastModifiedDate(lastModifiedDate);
							cmqRelation.setLastModifiedBy(lastModifiedByString);
							cmqRelationsList.add(cmqRelation);
						}
					}
				}//end of if (null != hierarchyNode)
			}
			
			if (!cmqRelationsList.isEmpty() || !cmqBaseChildrenList.isEmpty()) {
				try {
					if(!cmqRelationsList.isEmpty()) {
						for (CmqRelation190 cmqRelation190 : cmqRelationsList) {
							if(StringUtils.isBlank(cmqRelation190.getLastModifiedBy()) || cmqRelation190.getLastModifiedDate() == null) {
								cmqRelation190.setLastModifiedBy(lastModifiedByString);
								cmqRelation190.setLastModifiedDate(lastModifiedDate);
							}
							if(StringUtils.isBlank(cmqRelation190.getCreatedBy()) || cmqRelation190.getCreationDate() == null) {
								cmqRelation190.setCreatedBy(lastModifiedByString);
								cmqRelation190.setCreationDate(lastModifiedDate);
							}
						}
						this.cmqRelationService.update(cmqRelationsList, this.authService.getUserCn()
								, this.authService.getUserGivenName(), this.authService.getUserSurName()
								, this.authService.getCombinedMappedGroupMembershipAsString());
					}
					if(!cmqBaseChildrenList.isEmpty()) {
						
						for (CmqBase190 cmqBase190 : cmqBaseChildrenList) {
							cmqBase190.setLastModifiedBy(lastModifiedByString);
							cmqBase190.setLastModifiedDate(lastModifiedDate);
							if(StringUtils.isBlank(cmqBase190.getCreatedBy()) || cmqBase190.getCreationDate() == null) {
								cmqBase190.setCreatedBy(lastModifiedByString);
								cmqBase190.setCreationDate(lastModifiedDate);
							}
						}
						this.cmqBaseService.update(cmqBaseChildrenList, this.authService.getUserCn()
								, this.authService.getUserGivenName(), this.authService.getUserSurName()
								, this.authService.getCombinedMappedGroupMembershipAsString());
					}
					FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_INFO,
							"Relations are successfully updated for '" + cmqBase.getCmqName() + "'", "");
					FacesContext.getCurrentInstance().addMessage(null, msg);
				} catch (CqtServiceException e) {
					LOG.error("Exception occurred while updated the list of CmqRelations for CMQ base code "
							+ cmqBase.getCmqCode(), e);

					FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR,
							"An error occurred while updated the list of CmqRelations for CMQ base code " + cmqBase.getCmqCode(),
							"Error Details:" + e.getMessage());
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
		if(WIZARD_STEP_DETAILS.equalsIgnoreCase(oldStep) && detailsFormModel.isModelChanged()) {
			// current step is "Details" and the form has some unsaved changes
			
			//----Confirmation on unsaved changes: see onUpdateWizardFlowProcess's "details" step
			if(codeSelected != null)
				createWizardNextStep = event.getNewStep();
			else
				createWizardNextStep = WIZARD_STEP_DETAILS;
			RequestContext.getCurrentInstance().execute("PF('confirmSaveDetailsDlg').show();");
		} else if(codeSelected != null && WIZARD_STEP_INFONOTES.equalsIgnoreCase(oldStep) && notesFormModel.isModelChanged()) {
			// current step is "Informative Notes" and the form has some unsaved changes			
			//----Confirmation on unsaved changes: see onUpdateWizardFlowProcess's "notes" step
			createWizardNextStep = event.getNewStep();
			RequestContext.getCurrentInstance().execute("PF('confirmSaveNotesDlg').show();");
		} else if(codeSelected != null && WIZARD_STEP_RELATIONS.equalsIgnoreCase(oldStep) && relationsModified) {
			// current step is "Relations" and the form has some unsaved changes
			createWizardNextStep = event.getNewStep();
			RequestContext.getCurrentInstance().execute("PF('confirmSaveRelationsDlg').show();");
		} else if(codeSelected != null){
			nextStep = event.getNewStep();
		} else {
			nextStep = WIZARD_STEP_DETAILS;
		}
		RequestContext.getCurrentInstance().update("fCreate:wizardNavbar");
		return nextStep;
	}
	public boolean isCreateWizardNavbarShown() {
		return true;
	}
	public boolean isCreateWizardNavbarNextShown() {
		return isCreateWizardNavbarShown() && !WIZARD_STEP_CONFIRM.equals(createWizard.getStep());
	}
	public boolean isCreateWizardNavbarBackShown() {
		return !WIZARD_STEP_DETAILS.equals(createWizard.getStep());
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
			if(WIZARD_STEP_DETAILS.equalsIgnoreCase(oldStep) && detailsFormModel.isModelChanged()) {
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
			} else if(WIZARD_STEP_INFONOTES.equalsIgnoreCase(oldStep) && notesFormModel.isModelChanged()) {
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
			} else if(WIZARD_STEP_RELATIONS.equalsIgnoreCase(oldStep) && relationsModified) {
				// current step is "Relations" and the form has some unsaved changes
				updateWizardNextStep = event.getNewStep();
				RequestContext.getCurrentInstance().execute("PF('confirmSaveRelationsDlg').show();");
			} else {
				nextStep = event.getNewStep();
//				if(WIZARD_STEP_DETAILS.equalsIgnoreCase(oldStep) && UPDATE_WIZARD_STEP_SEARCH.equalsIgnoreCase(nextStep) && !updateWizard.isBackRequest(FacesContext.getCurrentInstance())) {
//					RequestContext.getCurrentInstance().execute("setTimeout(function(){PF('wizard').back();},100)");
//					return oldStep;
//				}
			}
		} else {
			nextStep = UPDATE_WIZARD_STEP_SEARCH;
		}
		RequestContext.getCurrentInstance().update("fUpdate:wizardNavbar");
		return nextStep;
	}

	public boolean isUpdateWizardNavbarShown() {
		return !UPDATE_WIZARD_STEP_SEARCH.equals(updateWizard.getStep());
	}
	public boolean isUpdateWizardNavbarNextShown() {
		return isUpdateWizardNavbarShown() && !WIZARD_STEP_CONFIRM.equals(updateWizard.getStep());
	}
	public boolean isUpdateWizardNavbarBackShown() {
		return !UPDATE_WIZARD_STEP_SEARCH.equals(updateWizard.getStep());
	}
	
	/**
	 * "Update" module -> saving CMQ List record Details
	 * 
	 * @return
	 */
	public String update() {
        if(!detailsFormModel.validateForm())
            return "";
        
		try {
			
			Long count = this.cmqBaseService.findCmqCountByCmqNameAndExtension(detailsFormModel.getExtension(), detailsFormModel.getName());
			
			if(count < 2) {
				//we should have atmost 1
				prepareDetailsFormSave();
				cmqBaseService.update(selectedData, this.authService.getUserCn()
						, this.authService.getUserGivenName(), this.authService.getUserSurName()
						, this.authService.getCombinedMappedGroupMembershipAsString());
				
				// retrieve the saved cmq base
				CmqBase190 savedEntity = cmqBaseService.findByCode(selectedData.getCmqCode());		
                setSelectedData(savedEntity);
				detailsFormModel.loadFromCmqBase190(selectedData);

				// // save the cmq code to session
				FacesContext.getCurrentInstance().getExternalContext().getSessionMap().put("NEW-CMQ_BASE-ID",
						savedEntity.getId());

				FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_INFO,
						"List '" + selectedData.getCmqName() + "' is successfully saved.", "");
				FacesContext.getCurrentInstance().addMessage(null, msg);
			} else {
				String errorMsg = "Duplicate CMQ name ('"
						+ detailsFormModel.getName() + "') and extention ('"
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
            setSelectedData(cmqBaseService.findByCode(selectedData.getCmqCode()));
			
			String error = CmqUtils.getExceptionMessageChain(e);
			
			FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR,
					"An error occurred while trying to update the details.", "Error: " + error);
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

		if(WIZARD_STEP_DETAILS.equalsIgnoreCase(oldStep) && detailsFormModel.isModelChanged()) {
			// current step is "Details" and the form has some unsaved changes
			if(COPY_WIZARD_STEP_SEARCH.equals(event.getNewStep())) {
                copyWizardNextStep = COPY_WIZARD_STEP_SEARCH;
			} else {
				//----Confirmation on unsaved changes: see onUpdateWizardFlowProcess's "details" step
				if (codeSelected != null)
					copyWizardNextStep = event.getNewStep();
				else
					copyWizardNextStep = WIZARD_STEP_DETAILS;
            }
            RequestContext.getCurrentInstance().execute("PF('confirmSaveDetailsDlg').show();");
		} else if(codeSelected != null && WIZARD_STEP_INFONOTES.equalsIgnoreCase(oldStep) && notesFormModel.isModelChanged()) {
			// current step is "Informative Notes" and the form has some unsaved changes
			//----Confirmation on unsaved changes: see onUpdateWizardFlowProcess's "notes" step
			copyWizardNextStep = event.getNewStep();
			RequestContext.getCurrentInstance().execute("PF('confirmSaveNotesDlg').show();");
		} else if(codeSelected != null && WIZARD_STEP_RELATIONS.equalsIgnoreCase(oldStep) && relationsModified) {
			// current step is "Relations" and the form has some unsaved changes
			copyWizardNextStep = event.getNewStep();
			RequestContext.getCurrentInstance().execute("PF('confirmSaveRelationsDlg').show();");
		} else {
			if(codeSelected == null)
				nextStep = COPY_WIZARD_STEP_SEARCH;
			else
				nextStep = event.getNewStep();
		}
        
		RequestContext.getCurrentInstance().update("fCopy:wizardNavbar");
		return nextStep;
	}

	public boolean isCopyWizardNavbarShown() {
		return !COPY_WIZARD_STEP_SEARCH.equals(copyWizard.getStep());
	}
	public boolean isCopyWizardNavbarNextShown() {
		return isCopyWizardNavbarShown() && !WIZARD_STEP_CONFIRM.equals(copyWizard.getStep());
	}
	public boolean isCopyWizardNavbarBackShown() {
		return !COPY_WIZARD_STEP_SEARCH.equals(copyWizard.getStep());
	}
	
	/**
	 * "Copy" module -> saving CMQ List record Details
	 * 
	 * @return
	 */
	public String copy() {
        if(!detailsFormModel.validateForm())
            return "";
        
		if(selectedData.getId() != null) { // if already saved
			return update();
		} 
        
		boolean cmqSaved = false;
		boolean savedRelations = false;
		Long savedCmqId = null;
		Long savedCmqCode = null;
		try {
			prepareDetailsFormSave();
			
			//remoe the parent cmq from this cmq.
			selectedData.setCmqParentCode(null);
			selectedData.setCmqParentName(null);
			
			cmqBaseService.create(selectedData, this.authService.getUserCn()
					, this.authService.getUserGivenName(), this.authService.getUserSurName()
					, this.authService.getCombinedMappedGroupMembershipAsString());
			
			// retrieve the saved cmq base
			CmqBase190 savedEntity = cmqBaseService.findByCode(selectedData.getCmqCode());
			cmqSaved = true;
			savedCmqId = selectedData.getId();
			savedCmqCode = selectedData.getCmqCode();
			
			LOG.info("Successfully saved new cmq with cmq id " + savedCmqId + " and cmq_code " + savedCmqCode);
			
			long copiedCode = copyingCmqCode;

			this.copyRelationsToNewCmq(copiedCode, savedEntity);
			savedRelations = true;
			
			LOG.info("All updates completed.");		
			
            setSelectedData(savedEntity);
			this.detailsFormModel.loadFromCmqBase190(selectedData);
			this.detailsFormModel.setModelChanged(false);//model is saved now
			codeSelected = selectedData.getCmqCode();
			// save the cmq code to session
			FacesContext.getCurrentInstance().getExternalContext().getSessionMap().put("NEW-CMQ_BASE-ID",
					savedEntity.getId());

			LOG.info("populating relations table for new cmq with code " + savedCmqCode);
			
            relationsModel.setClickedCmqCode(savedCmqCode);
            FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_INFO,
                    "List , Informative Notes and Relations are copied/updated successfully.", "");
            FacesContext.getCurrentInstance().addMessage(null, msg);
            
            setFormSaved(true);
		} catch (CqtServiceException e) {
			// roll back the selectedData
			setSelectedData(cmqBaseService.findByCode(copyingCmqCode));

            setCopiedCmq(selectedData);
			//
			LOG.error("Exception occurred while creating CmqBase190.", e);
			//delete the saves if any
			if(savedRelations) {
				List<CmqRelation190> cmqRelationList = this.cmqRelationService.findByCmqCode(savedCmqCode);
				for (CmqRelation190 cmqRelation190 : cmqRelationList) {
					try {
						this.cmqRelationService.remove(cmqRelation190, this.authService.getUserCn()
								, this.authService.getUserGivenName(), this.authService.getUserSurName()
								, this.authService.getCombinedMappedGroupMembershipAsString());
					} catch (CqtServiceException e1) {
						LOG.error("Exception occurred while deleting relation with relation id " + cmqRelation190.getId(), e);
					}
				}
			}
			
			if(cmqSaved) {
				try {
					this.cmqBaseService.remove(savedCmqId, this.authService.getUserCn()
												, this.authService.getUserGivenName(), this.authService.getUserSurName()
												, this.authService.getCombinedMappedGroupMembershipAsString());
				} catch (CqtServiceException e1) {
					LOG.error("Exception occurred while deleting cmq with id " + savedCmqId, e);
				}
			}
			String exceptionMessageChain = CmqUtils.getExceptionMessageChain(e);
			
			FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR,
					"An error occurred while trying to save the details.",
                    "Exception is [" + exceptionMessageChain + "]");
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
        if(!detailsFormModel.validateForm())
            return "";
        
		if(selectedData != null && selectedData.getId() != null) { // if already saved
			return update();
		}
        
		try {
			Long count = this.cmqBaseService.findCmqCountByCmqNameAndExtension(detailsFormModel.getExtension(), detailsFormModel.getName());

			if (count > 0) {
				String errorMsg = "Duplicate CMQ name ('"
						+ detailsFormModel.getName() + "') and extention ('"
						+ detailsFormModel.getExtension()
						+ "') found in db.";
				
				LOG.error(errorMsg);

				FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, errorMsg, "");
				FacesContext.getCurrentInstance().addMessage(null, msg);

				return null;
			} else {
				prepareDetailsFormSave();
				cmqBaseService.create(selectedData, this.authService.getUserCn()
						, this.authService.getUserGivenName(), this.authService.getUserSurName()
						, this.authService.getCombinedMappedGroupMembershipAsString());

				// retrieve the saved cmq base
				CmqBase190 savedEntity = cmqBaseService.findByCode(codevalue);
                setSelectedData(savedEntity);
				this.detailsFormModel.loadFromCmqBase190(selectedData);
				codeSelected = selectedData.getCmqCode();
                //set relations tab
                this.relationsModel.setClickedCmqCode(codeSelected);

				// save the cmq code to session
				FacesContext.getCurrentInstance().getExternalContext().getSessionMap().put("NEW-CMQ_BASE-ID",
						savedEntity.getId());

				FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_INFO,
						"List '" + selectedData.getCmqName() + "' is successfully saved.", "");
				FacesContext.getCurrentInstance().addMessage(null, msg);
				
				setFormSaved(true); 
			}
		} catch (CqtServiceException e) {
			LOG.error("Exception occurred while creating CmqBase190.", e);
			if(selectedData.getId() != null) {
				// since it failed to save the record, clear the creation date/user info
				try {
					this.cmqBaseService.remove(selectedData.getCmqId(), this.authService.getUserCn()
							, this.authService.getUserGivenName(), this.authService.getUserSurName()
							, this.authService.getCombinedMappedGroupMembershipAsString());
				} catch (CqtServiceException e1) {
					//eat it. No problem here.
				}
			}
			
			//reset the on screen details.
			selectedData.setCmqId(null);
			selectedData.setCmqCode(null);
			selectedData.setCreationDate(null);
			selectedData.setCreatedBy(null);
			this.detailsFormModel.loadFromCmqBase190(selectedData);

			String error = CmqUtils.getExceptionMessageChain(e);
			
			FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR,
					"An error occurred while trying to save the details.", "Error: " + error);
			FacesContext.getCurrentInstance().addMessage(null, msg);

			return null;
		}
		return "";
	}
	

	/**
	 * List Create/Update/Copy Wizard Forms Readonly state
	 * @return boolean
	 */
	public boolean isReadOnlyState() {
        boolean d;
        List<String> userGroupList = authService.getCmqMappedGroupMemberships();
		/**
         * Restrictions on users from  REQUESTOR and ADMIN groups
         */
    	if (userGroupList != null &&
                (userGroupList.contains(AuthenticationService.REQUESTER_GROUP)
                    || userGroupList.contains(AuthenticationService.ADMIN_GROUP)))
			d = restrictionsByUserAuthentified();
        else
            d = false;

        // If CMQ_BASE_TARGET IN('PENDING IA', 'REVIEWED IA', 'APPROVED IA', 'PUBLISHED IA') then list should be read-only in Update Module.
        // User should NOT be able to update details, informative notes, relations and workflow from confirm.
        if(updateWizard != null && selectedData != null && isTargetMovedToHigherIAStatus(selectedData))
            return true;

        // List is editable when CMQ_BASE_CURRNET.State is 'Draft' or 'Reviewed'.
        if (selectedData != null && selectedData.getCmqState() != null 
                && (CmqBase190.CMQ_STATE_VALUE_DRAFT.equalsIgnoreCase(selectedData.getCmqState())
                        || CmqBase190.CMQ_STATE_VALUE_REVIEWED.equalsIgnoreCase(selectedData.getCmqState()))){
            return d || false;
        } else {
            return true;
        }
	}
	
	/**
	 * Load a single CMQ List record for "Details/Informative Notes/Relations/Confirm" tabs
	 * 
	 * @param code
	 * @return
	 */
	public String loadCmqBaseByCode(Long code) {
		codeSelected = null;
		CmqBase190 cmq = this.cmqBaseService.findByCode(code);

		if (cmq != null) {
			codeSelected = cmq.getCmqCode();
            setSelectedData(cmq);
            CmqBaseTarget t = myCmqTargetService.findByCode(code);
            mySelectedCmqTarget = (t == null ? new CmqBaseTarget() : t);
		}
		if(createWizard != null) {
			detailsFormModel.setWizardType(WizardType.CreateWizard);
		} else if (browseWizard != null) {
			detailsFormModel.setWizardType(WizardType.BrowseWizard);
		} else if (updateWizard != null) {
			detailsFormModel.setWizardType(WizardType.UpdateWizard);
			setListCreator(selectedData.getCreatedBy());
		} else if (copyWizard != null) {
			detailsFormModel.setWizardType(WizardType.CopyWizard);
			//reset the values which are not supposed to be copied.
			copyingCmqCode = codeSelected;
			codeSelected = null;
			setListCreator(selectedData.getCreatedBy());
			setCopiedCmq(selectedData);
			
			String newCopiedCmqName = this.getCopyCmqName(selectedData.getCmqName());
			selectedData.setCmqName(newCopiedCmqName);
		}
		
		detailsFormModel.loadFromCmqBase190(selectedData);
		notesFormModel.loadFromCmqBase190(selectedData);
        if(codeSelected != null) {
            // set relations model's cmq code
            relationsModel.setClickedCmqCode(codeSelected);
        }
        
        getActiveWizard().setStep(WIZARD_STEP_DETAILS);
        setFormSaved(false);
        if(updateWizard != null)
            RequestContext.getCurrentInstance().update("fUpdate:detailsPanel");
        else if(copyWizard != null)
            RequestContext.getCurrentInstance().update("fCopy:detailsPanel");
        
        // if CMQ_BASE_TARGET.Status != 'PENDING IA' on update wizard
        if(updateWizard!=null && isTargetMovedToHigherIAStatus(selectedData)) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "The List moved to higher IA state can not be updated", ""));            
        } else if(updateWizard!=null && isImpactedByMeddraVersioning(selectedData)) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "The List is impacted by MedDRA Versioning", ""));
        }

      	return null;
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
			cmqRelation190.setRelationImpactType(null);
			cmqRelation190.setLastModifiedDate(creationDate);
			cmqRelation190.setLastModifiedBy(this.authService.getLastModifiedByUserAsString());
			cmqRelation190.setCreationDate(creationDate);
			cmqRelation190.setCreatedBy(this.authService.getLastModifiedByUserAsString());
		}
		//save relations
		this.cmqRelationService.update(cmqRelationList, this.authService.getUserCn()
				, this.authService.getUserGivenName(), this.authService.getUserSurName()
				, this.authService.getCombinedMappedGroupMembershipAsString());
		LOG.info("Cmq relations saved.");
	}
	
	private String getCopyCmqName(String newChildCmqName) {
		if(newChildCmqName.endsWith("-Copy-")) {
			newChildCmqName = newChildCmqName + "1";
		} else if(newChildCmqName.contains("-Copy-")) {
			String num = newChildCmqName.substring(newChildCmqName.lastIndexOf("-Copy") + 6);
			try{
				int i = Integer.valueOf(num);
				newChildCmqName = newChildCmqName.substring(0, newChildCmqName.lastIndexOf("-Copy") + 5);
				newChildCmqName += "-" + (++i);
			} catch (Exception e) {
				LOG.info("The cmq name had -Copy- but the string after that was not a number, going to treat the full name as base. original Cmq name was {}"
							, newChildCmqName);
				newChildCmqName = newChildCmqName + "-Copy-1";
			}
		} else if(newChildCmqName.endsWith("-Copy")) {
			newChildCmqName = newChildCmqName + "-1";
		} else {
			newChildCmqName = newChildCmqName + "-Copy-1";
		}
		LOG.info("New name for Copied cmq namq {} in inferred as {}", selectedData.getCmqName(), newChildCmqName);

		//now check in db
		Boolean exists = this.cmqBaseService.checkIfCmqNamqExists(newChildCmqName);
		if(exists) {
			LOG.warn("New name for Copied cmq was inferred as {} but it is already used. Trying a new one.", selectedData.getCmqName(), newChildCmqName);
			return this.getCopyCmqName(newChildCmqName);
		} else {
			return newChildCmqName;
		}
	}
	
	/**
	 * Sets the selectedData from ListDetailsFormModel
	 * @throws CqtServiceException
	 */
	private void prepareDetailsFormSave() throws CqtServiceException {
		// fill data
		if((createWizard != null || copyWizard != null) && (selectedData != null && selectedData.getId() == null)) {
			// get the next value of code
			codevalue = this.cmqBaseService.getNextCodeValue();
			RefConfigCodeList currentMeddraVersionCodeList = this.refCodeListService.getCurrentMeddraVersion();
			
			if(createWizard != null)
				detailsFormModel.setWizardType(WizardType.CreateWizard);
			else if(copyWizard != null)
				detailsFormModel.setWizardType(WizardType.CopyWizard);
			
			selectedData.setCmqCode(codevalue);
			selectedData.setDictionaryVersion(currentMeddraVersionCodeList.getValue());
			
			if (dictionaryName != null && dictionaryName.getValue() != null)
				selectedData.setDictionaryName((String) dictionaryName.getValue());
			else
				selectedData.setDictionaryName("");
			selectedData.setCmqSubversion(new BigDecimal(0.23d));
			notesFormModel.saveToCmqBase190(selectedData);
		} else if(updateWizard != null) {
			detailsFormModel.setWizardType(WizardType.UpdateWizard);
		}
		
		String lastModifiedByString = this.authService.getLastModifiedByUserAsString();
		selectedData.setLastModifiedBy(lastModifiedByString);
		selectedData.setLastModifiedDate(new Date()); 
		
		detailsFormModel.saveToCmqBase190(selectedData);
	}

	/**
	 * Reset the "Relations" tab
	 * @return
	 */
	public String cancelRelations() {
		if (selectedData.getId() == null) {
            setSelectedData(null);
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
					this.cmqRelationService.remove(ids, this.authService.getUserCn()
							, this.authService.getUserGivenName(), this.authService.getUserSurName()
							, this.authService.getCombinedMappedGroupMembershipAsString());
				
				// delete the record
				cmqBaseService.remove(selectedData.getCmqId(), this.authService.getUserCn()
						, this.authService.getUserGivenName(), this.authService.getUserSurName()
						, this.authService.getCombinedMappedGroupMembershipAsString());

                setSelectedData(null);
				codeSelected = null;
				initAll();
                
				FacesContext.getCurrentInstance()
                        .addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Record has been deleted!", ""));

			} catch (CqtServiceException e) {
				e.printStackTrace();
				FacesContext.getCurrentInstance()
                        .addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "An error occurred while deleting the list", e.getMessage()));
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
		
		String lastModifiedByString = this.authService.getLastModifiedByUserAsString();
		selectedData.setLastModifiedBy(lastModifiedByString);
		selectedData.setLastModifiedDate(new Date());
		
		//Adding the due date to be updated
		workflowFormModel.saveToCmqBase190(selectedData);
		
		// Update
		try {
			cmqBaseService.update(selectedData, this.authService.getUserCn()
					, this.authService.getUserGivenName(), this.authService.getUserSurName()
					, this.authService.getCombinedMappedGroupMembershipAsString());
			LOG.info("\n NEW STATE :" + selectedData.getCmqState());

			FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_INFO,
                    "Workflow state set to '" + state + "'", "");
			FacesContext ctx = FacesContext.getCurrentInstance();
			ctx.addMessage(null, msg);
			
			//Clearing workflow attributes : due date, reason for request, reason for approval
			this.workflowFormModel.init();

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
		detailsFormModel.afterChangeExtension(event);
	}
	
	/**
	 * Returns products list with Inactive Products for CMQs selected.
	 * 
	 * @return
	 */
	public List<RefConfigCodeList> getProductList() {
		cqtCacheManager.removeAllFromCache(CACHE_NAME);
		products = new ArrayList<>();
		products = refCodeListService.findByConfigType(
				CqtConstants.CODE_LIST_TYPE_PRODUCT, OrderBy.ASC);
		if (products == null) {
			products = new ArrayList<>();
		}
		else {
			if (selectedData != null && selectedData.getProductsList() != null && (updateWizard != null || copyWizard != null))
				for (CmqProductBaseCurrent prod : selectedData.getProductsList()) {
					RefConfigCodeList config = refCodeListService.findByConfigTypeAndInternalCode(CqtConstants.CODE_LIST_TYPE_PRODUCT, prod.getCmqProductCd());
					if (config != null && config.getActiveFlag().equals("N") && !products.contains(config)) {
						config.setValue(config.getCodelistInternalValue());
						products.add(config);
					}
						
				}
					
		}
			
		return products;
	}
	
	public List<RefConfigCodeList> getLevelListWithoutProtocol() {
		cqtCacheManager.removeAllFromCache(CACHE_NAME);

		List<RefConfigCodeList> levels = refCodeListService.findByConfigType(
				CqtConstants.CODE_LIST_TYPE_DICTIONARY_LEVELS, OrderBy.ASC);
		RefConfigCodeList levelToRemove = refCodeListService.findByConfigTypeAndInternalCode(CqtConstants.CODE_LIST_TYPE_DICTIONARY_LEVELS, "PRO");
		
		if (levelToRemove != null)
			levels.remove(levelToRemove);
		
		levelToRemove = refCodeListService.findByConfigTypeAndInternalCode(CqtConstants.CODE_LIST_TYPE_DICTIONARY_LEVELS, "NC-LLT");
		
		if (levelToRemove != null)
			levels.remove(levelToRemove);
		
		return levels;
	}
	

	public List<RefConfigCodeList> getLevelListProtocol() {
		cqtCacheManager.removeAllFromCache(CACHE_NAME);

		List<RefConfigCodeList> levels = refCodeListService.findByConfigType(
				CqtConstants.CODE_LIST_TYPE_DICTIONARY_LEVELS, OrderBy.ASC);
		
		
		RefConfigCodeList levelToRemove = refCodeListService.findByConfigTypeAndInternalCode(CqtConstants.CODE_LIST_TYPE_DICTIONARY_LEVELS, "NC-LLT");
		if (levelToRemove != null)
			levels.remove(levelToRemove);
		 
		return levels;
	}
	
	
	 public List<RefConfigCodeList> getLevelSMQ() {
		cqtCacheManager.removeAllFromCache(CACHE_NAME);

		List<RefConfigCodeList> levels = refCodeListService.findByConfigType(
					CqtConstants.CODE_LIST_TYPE_DICTIONARY_LEVELS, OrderBy.ASC);
			
			return levels;
		}
	
	

	
	//--------------------- Getters and Setters -----------------------
	
	/**
	 * Details form Model
	 * @return
	 */
	public ListDetailsFormVM getDetailsFormModel() {
		return this.detailsFormModel;
	}
	public void setDetailsFormModel(ListDetailsFormVM model) {
		this.detailsFormModel = model;
	}
	
	/**
	 * "Informative Notes" form model
	 * @return
	 */
	public ListNotesFormVM getNotesFormModel() {
		return this.notesFormModel;
	}
	public void setNotesFormModel(ListNotesFormVM model) {
		this.notesFormModel = model;
	}
	
	/**
	 * "Confirm / Workflow" form model
	 * @return
	 */
	public ListWorkflowFormVM getWorkflowFormModel() {
		return this.workflowFormModel;
	}
	public void setWorkflowFormModel(ListWorkflowFormVM model) {
		this.workflowFormModel = model;
	}
	
	public CmqBase190 getSelectedData() {
		return selectedData;
	}

	public void setSelectedData(CmqBase190 selectedData) {
        if(selectedData == null) {
            this.selectedData = new CmqBase190();
            isSelectedDataApprovedOnce = false;
        } else {
            this.selectedData = selectedData;
            isSelectedDataApprovedOnce = cmqBaseService.checkIfApprovedOnce(selectedData.getCmqCode());
        }
	}

	public void setCmqBaseService(ICmqBase190Service cmqBaseService) {
		this.cmqBaseService = cmqBaseService;
	}

	public ICmqBase190Service getCmqBaseService() {
		return cmqBaseService;
	}
    
    public ICmqBaseTargetService getMyCmqTargetService() {
        return myCmqTargetService;
    }

    public void setMyCmqTargetService(ICmqBaseTargetService cmqTargetService) {
        this.myCmqTargetService = cmqTargetService;
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
		//return (!CmqBase190.CMQ_STATE_VALUE_DRAFT.equalsIgnoreCase(selectedData.getCmqState())
		//		&& !CmqBase190.CMQ_STATE_VALUE_REVIEWED.equalsIgnoreCase(selectedData.getCmqState()));
		return this.isReadOnlyState();
	}
	
	/**
	 * returns if the current list is a child list and has a viewable parent list.
	 * @return
	 */
	public boolean isParentViewable() {
        /*return ((selectedData.getCmqLevel() > 1)
                && (selectedData.getCmqParentCode()!=null)
                && !this.isReadOnlyState());*/
        return ((selectedData.getCmqLevel() > 1)
                && (selectedData.getCmqParentCode()!=null));
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
		String userGroup = authService.getGroupMembershipHeader();
		/**
         * Restrictions on users from  REQUESTOR and ADMIN groups
         */
		if(userGroup == null) { 
			return true;
		} else if(!userGroup.contains(AuthenticationService.REQUESTER_GROUP) 
				&& ((selectedData != null) && (CmqBase190.CMQ_STATE_VALUE_REVIEWED.equalsIgnoreCase(selectedData.getCmqState())
						|| CmqBase190.CMQ_STATE_VALUE_APPROVED.equalsIgnoreCase(selectedData.getCmqState())))) {
			return false;
		} else {
			return true;
		}
	}

	public boolean isDeleteDisabled() {
		if (selectedData == null
				|| !CmqBase190.CMQ_STATE_VALUE_DRAFT.equalsIgnoreCase(selectedData.getCmqState())
				|| selectedData.getActivationDate() != null)
			return true;
        
        // Disabled delete button if List has been approved once
        if(isSelectedDataApprovedOnce)
            return true;
		return false;
	}

	public boolean isApproveDisabled() {
        String userGroup = authService.getGroupMembershipHeader();
        // when List is not INACTIVE status, and is Reviewed state
		if (selectedData != null
                && !CmqBase190.CMQ_STATUS_VALUE_INACTIVE.equalsIgnoreCase(selectedData.getCmqStatus())
                && CmqBase190.CMQ_STATE_VALUE_REVIEWED.equalsIgnoreCase(selectedData.getCmqState())) {
			if (userGroup != null && (userGroup.contains("MQM")))
				return false;
			// if AD Group is Requester, disable it
			if (userGroup != null && (userGroup.contains(AuthenticationService.REQUESTER_GROUP)))
				return true;
		}
        
        // otherwise, disable
		return true;
	}

	public boolean isReviewedDisabled() {
        String userGroup = authService.getGroupMembershipHeader();
		// if AD Group is MANAGER, enable it
		if (userGroup != null &&
                (userGroup.contains("MQM")
                		&& userGroup.contains(AuthenticationService.REQUESTER_GROUP))) {
			//Disable Review Button when List is Approved
            if(selectedData != null) {
                // Disable Review button when List is Reviewed or Approved state
               /* if (CmqBase190.CMQ_STATE_VALUE_REVIEWED.equalsIgnoreCase(selectedData.getCmqState())
                        || CmqBase190.CMQ_STATE_VALUE_APPROVED.equalsIgnoreCase(selectedData.getCmqState())) {
                    return true;
                }*/
                if (this.isReadOnlyState()) {
                    return true;
                }
                // Disable Review button when List is INACTIVE
                if(CmqBase190.CMQ_STATUS_VALUE_INACTIVE.equalsIgnoreCase(selectedData.getCmqStatus())) {
                    return true;
                }
                if (CmqBase190.CMQ_STATE_VALUE_DRAFT
							.equalsIgnoreCase(selectedData.getCmqState()))
                    return false;
            } else {
                return true;
            }
		}

		// if AD Group is Requester, disable it
		if (userGroup != null &&
                (userGroup.contains(AuthenticationService.REQUESTER_GROUP)
                		&& !(userGroup.contains("MQM"))))
			return true;
        
        if(selectedData != null) {
        	if (this.isReadOnlyState()) {
                return true;
            }
            //Disable Review Button when List is Approved
            if (CmqBase190.CMQ_STATE_VALUE_APPROVED.equalsIgnoreCase(selectedData.getCmqState()))
                return true;
            // Disable Review button when List is INACTIVE
            if (CmqBase190.CMQ_STATUS_VALUE_INACTIVE.equalsIgnoreCase(selectedData.getCmqStatus()))
                return true;
            // Enable Review button when List is Draft
            if (CmqBase190.CMQ_STATE_VALUE_DRAFT
                            .equalsIgnoreCase(selectedData.getCmqState()))
                return false;
        }
        // otherwise disable
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
	
	public Wizard getActiveWizard() {
		if(createWizard != null)
			return createWizard;
		else if(copyWizard != null)
			return copyWizard;
		else if(updateWizard != null)
			return updateWizard;
        else if(browseWizard != null)
            return browseWizard;
		return null;
	}

	private void setCopiedCmq(CmqBase190 cmq) {
		cmq.setId(null);//need to set since we may need to create a new cmq
		cmq.setCmqCode(null);//need to set since we may need to create a new cmq
		cmq.setCmqStatus(CmqBase190.CMQ_STATUS_VALUE_PENDING);
		cmq.setCmqState(CmqBase190.CMQ_STATE_VALUE_DRAFT);
		cmq.setCmqGroup("No Group");
		cmq.setActivationDate(null);
		cmq.setActivatedBy(null);
		cmq.setCreationDate(null);
		cmq.setCreatedBy(null);
		cmq.setLastModifiedBy(null);
		cmq.setLastModifiedDate(null);
	}

	public boolean isFormSaved() {
		return formSaved;
	}

	public void setFormSaved(boolean formSaved) {
		this.formSaved = formSaved;
	}

    /**
     * @return the authService
     */
    public AuthenticationService getAuthService() {
        return authService;
    }

    /**
     * @param authService the authService to set
     */
    public void setAuthService(AuthenticationService authService) {
        this.authService = authService;
    }
    
    public boolean isDetailsFormDisabled() {
        boolean d;
        String userGroup = authService.getGroupMembershipHeader();
      //Create list abilities for MQM and REQUESTOR
      	/**
          * Restrictions on users from  REQUESTOR and ADMIN groups
        */
         if ((createWizard != null || updateWizard != null || copyWizard != null) && userGroup != null &&
                (userGroup.contains(AuthenticationService.REQUESTER_GROUP)
                  || userGroup.contains("MQM")))
      			d = false;
    	/**
         * Restrictions on users from  REQUESTOR and ADMIN groups
         */
    	if (userGroup != null &&
    			 (userGroup.contains(AuthenticationService.REQUESTER_GROUP)
    	                    || userGroup.contains(AuthenticationService.ADMIN_GROUP)))
            d = restrictionsByUserAuthentified();
    	
        else
            d = false;
        
        if(updateWizard != null)
            return d || (!WIZARD_STEP_DETAILS.equals(getActiveWizard().getStep()) || this.isReadOnlyState() || this.isFormSaved());
        if(copyWizard != null)
            return d || (copyingCmqCode==null || !WIZARD_STEP_DETAILS.equals(getActiveWizard().getStep()) || this.isReadOnlyState() || this.isFormSaved());
        else
            return d || (this.isReadOnlyState() || this.isFormSaved());
    }
    
    /**
     * Restrictions on users from  REQUESTOR and ADMIN groups
     */
    public boolean restrictionsByUserAuthentified() {
    	List<String> userGroupList = authService.getCmqMappedGroupMemberships();
    	
        if (updateWizard != null || copyWizard != null) {
        	//conditions are:
        	/*	
        	 	when user is a REQUESTER
        	 	1) list's state is DRAFT or REVIEWED
				2) the list's status is P
				3) they are any designee or they have created the list
        	 */
        	if (userGroupList != null && (userGroupList.contains("MQM") || userGroupList.contains("MANAGER")))
        		return false;
        	else if (userGroupList != null && (userGroupList.contains(AuthenticationService.REQUESTER_GROUP)) 
        			&& selectedData.getCmqStatus().equals("P") 
        			&& (selectedData.getCmqState().equals("DRAFT") || selectedData.getCmqState().equals("REVIEWED"))
        			&& (((listCreator != null) && (listCreator.startsWith(authService.getUserCn())))
        					|| ((selectedData.getCmqDesignee() != null && selectedData.getCmqDesignee().equals(authService.getUserCn()))
        		        			|| (selectedData.getCmqDesignee2() != null && selectedData.getCmqDesignee2().equals(authService.getUserCn()))
        		        			|| (selectedData.getCmqDesignee3() != null && selectedData.getCmqDesignee3().equals(authService.getUserCn()))))) {
        		return  false;
        	} else if (userGroupList != null && (userGroupList.contains(AuthenticationService.ADMIN_GROUP)) 
        			&& selectedData.getCmqStatus().equals("P") 
        			&& (selectedData.getCmqState().equals("DRAFT") || (selectedData.getCmqState().equals("PENDING IA") || selectedData.getCmqState().equals("REVIEWED IA")))
        			&& (((listCreator != null) && (listCreator.startsWith(authService.getUserCn())))
        					|| ((selectedData.getCmqDesignee() != null && selectedData.getCmqDesignee().equals(authService.getUserCn()))
        		        			|| (selectedData.getCmqDesignee2() != null && selectedData.getCmqDesignee2().equals(authService.getUserCn()))
        		        			|| (selectedData.getCmqDesignee3() != null && selectedData.getCmqDesignee3().equals(authService.getUserCn()))))) {
        		return  false;
        	}
        }
        
 
        if (createWizard != null)
        	return false;
        return true;
    }

	public SWJSFRequest getAppSWJSFRequest() {
		return appSWJSFRequest;
	}

	public void setAppSWJSFRequest(SWJSFRequest appSWJSFRequest) {
		this.appSWJSFRequest = appSWJSFRequest;
	}
    
    private void loadCmqBaseTarget(CmqBase190 cmq) {
        if(!cmq.isCmqBaseTargetSet()) {
            try{
                CmqBaseTarget cmqTarget = myCmqTargetService.findByCode(cmq.getCmqCode());
                cmq.setCmqBaseTarget(cmqTarget);
            } catch(Exception e) {
                cmq.setCmqBaseTarget(null);
            } finally {
                cmq.setCmqBaseTargetSet(true);
            }
        }
    }
    
    /**
     * Checks if given CMQ_BASE's CMQ_BASE_TARGET's state is "Pending IA"
     * @param cmq
     * @return 
     */
    public boolean isTargetMovedToHigherIAStatus(CmqBase190 cmq) {
        if(cmq == null || cmq.getCmqCode() == null)
            return false;
        
        loadCmqBaseTarget(cmq);
        
        return (cmq.getCmqBaseTarget()!=null
                && ( CmqBaseTarget.CMQ_STATE_PENDING_IA.equalsIgnoreCase(cmq.getCmqBaseTarget().getCmqState())
                    || CmqBaseTarget.CMQ_STATE_REVIEWED_IA.equalsIgnoreCase(cmq.getCmqBaseTarget().getCmqState())
                    || CmqBaseTarget.CMQ_STATE_APPROVED_IA.equalsIgnoreCase(cmq.getCmqBaseTarget().getCmqState())
                    || CmqBaseTarget.CMQ_STATE_PUBLISHED_IA.equalsIgnoreCase(cmq.getCmqBaseTarget().getCmqState())));
    }
    
    /**
     * Checks if given CMQ_BASE's CMQ_BASE_TARGET's state is "Pending IA"
     * @param cmq
     * @return 
     */
    public boolean isTargetStatusPendingIA(CmqBase190 cmq) {
        if(cmq == null || cmq.getCmqCode() == null)
            return false;
        
        loadCmqBaseTarget(cmq);
        
        return (cmq.getCmqBaseTarget()==null || CmqBaseTarget.CMQ_STATE_PENDING_IA.equalsIgnoreCase(cmq.getCmqBaseTarget().getCmqState()));
    }
    
    /**
     * Checks if given CMQ_BASE or CMQ_BASE_TARGET is impacted by Meddra versioning
     * @param cmq
     * @return 
     */
    public boolean isImpactedByMeddraVersioning(CmqBase190 cmq) {
        if(cmq != null || cmq.getCmqCode() == null){
            if(cmq.isImpactedByMeddraVersioning())
                return true;
            loadCmqBaseTarget(cmq);
            return (cmq.getCmqBaseTarget()!=null && cmq.getCmqBaseTarget().isImpactedByMeddraVersioning());
        }
        return false;
    }
    
    public boolean isAuthorized() {
        if(getAuthService().hasGroup(new String[] {AuthenticationService.REQUESTER_GROUP})) {
            return false;
        }
        return true;
    }
    
    
    public void clearCmqSelection() {
        codeSelected = null;
        copyingCmqCode = null;
        setSelectedData(null);
        initAll();        
    }

	public String getListCreator() {
		return listCreator;
	}

	public void setListCreator(String listCreator) {
		this.listCreator = listCreator;
	}
    
    public ListRelationsVM getRelationsModel() {
        return relationsModel;
    }

    public void setRelationsModel(ListRelationsVM relationsModel) {
        this.relationsModel = relationsModel;
    }
    
    public ISmqBaseService getSmqBaseService() {
        return smqBaseService;
    }

    public void setSmqBaseService(ISmqBaseService smqBaseService) {
        this.smqBaseService = smqBaseService;
    }

    public IMeddraDictService getMeddraDictService() {
        return meddraDictService;
    }

    public void setMeddraDictService(IMeddraDictService meddraDictService) {
        this.meddraDictService = meddraDictService;
    }

	public List<RefConfigCodeList> getProducts() {
		return products;
	}

	public void setProducts(List<RefConfigCodeList> products) {
		this.products = products;
	}

	public ICqtCacheManager getCqtCacheManager() {
		return cqtCacheManager;
	}

	public void setCqtCacheManager(ICqtCacheManager cqtCacheManager) {
		this.cqtCacheManager = cqtCacheManager;
	}

	public String getScopeFilter() {
		return scopeFilter;
	}

	public void setScopeFilter(String scopeFilter) {
		this.scopeFilter = scopeFilter;
	}
     
}
