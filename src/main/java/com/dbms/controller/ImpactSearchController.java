package com.dbms.controller;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.ListUtils;
import org.apache.commons.lang3.StringUtils;
import org.primefaces.component.datatable.DataTable;
import org.primefaces.component.wizard.Wizard;
import org.primefaces.context.RequestContext;
import org.primefaces.event.FlowEvent;
import org.primefaces.event.NodeExpandEvent;
import org.primefaces.event.NodeSelectEvent;
import org.primefaces.event.NodeUnselectEvent;
import org.primefaces.event.SelectEvent;
import org.primefaces.model.DefaultTreeNode;
import org.primefaces.model.LazyDataModel;
import org.primefaces.model.SortMeta;
import org.primefaces.model.SortOrder;
import org.primefaces.model.StreamedContent;
import org.primefaces.model.TreeNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dbms.csmq.HierarchyNode;
import com.dbms.entity.IEntity;
import com.dbms.entity.cqt.CmqBase190;
import com.dbms.entity.cqt.CmqBaseTarget;
import com.dbms.entity.cqt.CmqRelation190;
import com.dbms.entity.cqt.CmqRelationTarget;
import com.dbms.entity.cqt.RefConfigCodeList;
import com.dbms.entity.cqt.SmqBase190;
import com.dbms.entity.cqt.SmqBaseTarget;
import com.dbms.entity.cqt.SmqRelation190;
import com.dbms.entity.cqt.SmqRelationTarget;
import com.dbms.entity.cqt.dtos.MeddraDictHierarchySearchDto;
import com.dbms.entity.cqt.dtos.MeddraDictReverseHierarchySearchDto;
import com.dbms.service.AuthenticationService;
import com.dbms.service.ICmqBase190Service;
import com.dbms.service.ICmqBaseTargetService;
import com.dbms.service.ICmqRelation190Service;
import com.dbms.service.ICmqRelationTargetService;
import com.dbms.service.IMeddraDictService;
import com.dbms.service.IMeddraDictTargetService;
import com.dbms.service.IRefCodeListService;
import com.dbms.service.ISmqBaseService;
import com.dbms.service.ISmqBaseTargetService;
import com.dbms.util.CmqUtils;
import com.dbms.util.SWJSFRequest;
import com.dbms.view.IARelationsTreeHelper;
import com.dbms.util.exceptions.CqtServiceException;
import com.dbms.view.CmqBaseHierarchySearchVM;
import com.dbms.view.ListDetailsFormVM;
import com.dbms.view.ListNotesFormVM;
import com.dbms.view.TargetHierarchySearchVM;

/**
 * @date Feb 7, 2017 7:39:34 AM
 **/
@ManagedBean
@ViewScoped
public class ImpactSearchController implements Serializable {

	private static final long serialVersionUID = 52993434344651662L;

	private static final Logger LOG = LoggerFactory.getLogger(ImpactSearchController.class);
	
	private static final int SELECTED_NO_LIST = 0;
	private static final int SELECTED_CURRENT_LIST = 1;
	private static final int SELECTED_TARGET_LIST = 2;
	
	@ManagedProperty("#{MeddraDictService}")
	private IMeddraDictService meddraDictCurrentService;

	@ManagedProperty("#{MeddraDictTargetService}")
	private IMeddraDictTargetService meddraDictTargetService;
	
	@ManagedProperty("#{CmqBase190Service}")
	private ICmqBase190Service cmqBaseCurrentService;

	@ManagedProperty("#{CmqRelation190Service}")
	private ICmqRelation190Service cmqRelationCurrentService;

	@ManagedProperty("#{CmqBaseTargetService}")
	private ICmqBaseTargetService cmqBaseTargetService;

	@ManagedProperty("#{CmqRelationTargetService}")
	private ICmqRelationTargetService cmqRelationTargetService;
	
	@ManagedProperty("#{SmqBaseService}")
	private ISmqBaseService smqBaseCurrentService;

	@ManagedProperty("#{SmqBaseTargetService}")
	private ISmqBaseTargetService smqBaseTargetService;
	
	@ManagedProperty("#{RefCodeListService}")
	private IRefCodeListService refCodeListService;
	
	@ManagedProperty("#{AuthenticationService}")
	private AuthenticationService authService;
	
	@ManagedProperty("#{appSWJSFRequest}")
    private SWJSFRequest appSWJSFRequest;
	
	Wizard iaWizard, iaVersionWizard;
	private String iaWizardNextStep;

	private LazyDataModel<CmqBaseTarget> impactedCmqBaseLazyDataModel;
	private CmqBaseTarget selectedImpactedCmqList;

	private LazyDataModel<CmqBaseTarget> notImpactedCmqBaseLazyDataModel;
	private CmqBaseTarget selectedNotImpactedCmqList;
	
	private LazyDataModel<SmqBaseTarget> impactedSmqBaseLazyDataModel;
	private SmqBaseTarget selectedImpactedSmqList;

	private LazyDataModel<SmqBaseTarget> notImpactedSmqBaseLazyDataModel;
	private SmqBaseTarget selectedNotImpactedSmqList;

	private TreeNode currentTableRootTreeNode; // root node of current table
	private TreeNode currentTableSelection; // selected row of current table
	
	private TreeNode targetTableRootTreeNode; // root node of target table
	private TreeNode targetTableSelection; // selected row of target table
	
	private int currentOrTarget = 0; // represents if viewing LIST is from current table or target table
	
	private ListNotesFormVM notesFormModel = new ListNotesFormVM(); // "Informative Notes" tab model
	private ListDetailsFormVM detailsFormModel; // "Details" tab model
	
	private boolean isImpactedCmqSelected, isNonImpactedCmqSelected, isImpactedSmqSelected, isNonImpactedSmqSelected;
	
	private TargetHierarchySearchVM targetHierarchySearchDlgModel;
    private CmqBaseHierarchySearchVM currentHierarchySearchDlgModel;
	
	private TreeNode[] relationSelected;
	private TreeNode[] relationSelectedInRelationsTable;
	//private boolean changeOccur;
	private boolean targetRelationsUpdated = false;
	
	private LazyDataModel<MeddraDictHierarchySearchDto> newPtSearchLazyDataModel;
	
	private String newPtSocSearchTerm;
	
	private MeddraDictHierarchySearchDto[] selectedNewPtLists;
	
	private List<String> newPtDistinctSocTermsList;
	
	private String listName;
	private StreamedContent excelFile;
	private String confirmMessage;

	
	public ImpactSearchController() {
		
	}

	@PostConstruct
	public void init() {
		this.detailsFormModel  = new ListDetailsFormVM(this.authService, this.refCodeListService, this.appSWJSFRequest);
		this.impactedCmqBaseLazyDataModel = new CmqLazyDataModel(true);
		this.notImpactedCmqBaseLazyDataModel = new CmqLazyDataModel(false);
		this.impactedSmqBaseLazyDataModel = new SmqLazyDataModel(true);
		this.notImpactedSmqBaseLazyDataModel = new SmqLazyDataModel(false);
		currentTableRootTreeNode = new DefaultTreeNode("root", new HierarchyNode("CODE",
				"LEVEL", "SCOPE", null), null);
		targetTableRootTreeNode = new DefaultTreeNode("root", new HierarchyNode("CODE",
				"LEVEL", "SCOPE", "CATEGORY", "WEIGHT", null, null), null);
        currentHierarchySearchDlgModel = new CmqBaseHierarchySearchVM(cmqBaseCurrentService, smqBaseCurrentService, meddraDictCurrentService, cmqRelationCurrentService);
        targetHierarchySearchDlgModel = new TargetHierarchySearchVM(cmqBaseTargetService, smqBaseTargetService, meddraDictTargetService, cmqRelationTargetService);
        
		currentOrTarget = SELECTED_NO_LIST;
		
		newPtDistinctSocTermsList = this.meddraDictTargetService.findSocsWithNewPt();
		//changeOccur = false;
	}
	
	/**
	 * Generate Excel report on target datatable.
	 */
	public void generateExcel() {
		String dict = "";
		RefConfigCodeList currentMeddraVersionCodeList = this.refCodeListService.getCurrentMeddraVersion();
		if (currentMeddraVersionCodeList != null)
			dict = currentMeddraVersionCodeList.getValue();
		StreamedContent content = null;
		if (selectedImpactedCmqList != null)
			content = cmqBaseTargetService.generateCMQExcel(selectedImpactedCmqList, dict);
		if (selectedNotImpactedCmqList != null)
			content = cmqBaseTargetService.generateCMQExcel(selectedNotImpactedCmqList, dict);
		if (selectedImpactedSmqList != null)
			content = smqBaseTargetService.generateSMQExcel(selectedImpactedSmqList, dict);
		if (selectedNotImpactedSmqList != null)
			content = smqBaseTargetService.generateSMQExcel(selectedNotImpactedSmqList, dict);
		setExcelFile(content); 
	}
	
	public void onRelationDrop() {
		/*
		 * if(selectedImpactedCmqList.length > 0) { // Multiple item Drag-n-Drop
		 * System.out.println(selectedImpactedCmqList); } else { FacesMessage
		 * msg = new FacesMessage(FacesMessage.SEVERITY_INFO,
		 * "No relation is selected to add", "");
		 * FacesContext.getCurrentInstance().addMessage(null, msg); }
		 */
	}
	
	public void onRowSelect(SelectEvent event) {
		Object obj = event.getObject();
		if(obj instanceof CmqBaseTarget) {
			CmqBaseTarget cmqBase = (CmqBaseTarget) obj;
			if("IMPACTED".equalsIgnoreCase(cmqBase.getImpactType()) || "ICC".equalsIgnoreCase(cmqBase.getImpactType())){
				this.isImpactedCmqSelected = true;
				this.isNonImpactedCmqSelected = false;
				this.isImpactedSmqSelected = false;
				this.isNonImpactedSmqSelected = false;
			} else {
				this.isImpactedCmqSelected = false;
				this.isNonImpactedCmqSelected = true;
				this.isImpactedSmqSelected = false;
				this.isNonImpactedSmqSelected = false;
			}
		} else if(obj instanceof SmqBaseTarget) {
			SmqBaseTarget smqBase = (SmqBaseTarget) obj;
			if("IMPACTED".equalsIgnoreCase(smqBase.getImpactType()) || "ICC".equalsIgnoreCase(smqBase.getImpactType())){
				this.isImpactedCmqSelected = false;
				this.isNonImpactedCmqSelected = false;
				this.isImpactedSmqSelected = true;
				this.isNonImpactedSmqSelected = false;
			} else {
				this.isImpactedCmqSelected = false;
				this.isNonImpactedCmqSelected = false;
				this.isImpactedSmqSelected = false;
				this.isNonImpactedSmqSelected = true;
			}
		}
	}
	
	public void onRowUnselect(SelectEvent event) {
		
	}
	
	/**
	 * Event fired on the selection of a row.
	 * @param event NodeSelectEvent
	 */
	public void onSelectCurrentRowTreeTable(NodeSelectEvent event) {
		//Updating the worflow buttons
		currentOrTarget = SELECTED_CURRENT_LIST;
	}
	/**
	 * Event fired on the unselection of a row from current list.
	 * @param event NodeSelectEvent
	 */
	public void onUnselectCurrentRowTreeTable(NodeUnselectEvent event) {
		//Updating the worflow buttons
		if(currentOrTarget == SELECTED_CURRENT_LIST)
			currentOrTarget = SELECTED_NO_LIST;
	}
	
	/**
	 * Event fired on the selection of a row.
	 * @param event NodeSelectEvent
	 */
	public void onSelectTargetRowTreeTable(NodeSelectEvent event) {
		currentOrTarget = SELECTED_TARGET_LIST;
		String code = "";
		if (isImpactedCmqSelected && selectedImpactedCmqList != null) code = selectedImpactedCmqList.getCmqCode() + "";
        else if (isNonImpactedCmqSelected && selectedNotImpactedCmqList != null) code = selectedNotImpactedCmqList.getCmqCode() + "";
        else if (isImpactedSmqSelected && selectedImpactedSmqList != null ) code = selectedImpactedSmqList.getSmqCode() + "";
        else if (isNonImpactedSmqSelected && selectedNotImpactedSmqList != null ) code = selectedNotImpactedSmqList.getSmqCode() + "";
		
		HierarchyNode node;
		if (event != null && event.getTreeNode() != null && event.getTreeNode().getData() != null) {
			 node = (HierarchyNode) event.getTreeNode().getData();
			if (node != null && !node.getCode().equals(code)) {	
				currentOrTarget = SELECTED_NO_LIST;
			}
		}		
	}
	/**
	 * Event fired on the unselection of a row from target list.
	 * @param event NodeSelectEvent
	 */
	public void onUnselectTargetRowTreeTable(NodeUnselectEvent event) {
		//Updating the worflow buttons
		if(currentOrTarget == SELECTED_TARGET_LIST)
			currentOrTarget = SELECTED_NO_LIST;
	}
	

	public void onNodeExpandCurrentTable(NodeExpandEvent event) {
        IARelationsTreeHelper treeHelper = new IARelationsTreeHelper(
                cmqBaseCurrentService, smqBaseCurrentService, meddraDictCurrentService, cmqRelationCurrentService,
                cmqBaseTargetService, smqBaseTargetService, meddraDictTargetService, cmqRelationTargetService);
        treeHelper.onNodeExpandCurrentTable(currentTableRootTreeNode, event);
	}
	
	public void onNodeExpandTargetTable(NodeExpandEvent event) {
        IARelationsTreeHelper treeHelper = new IARelationsTreeHelper(
                cmqBaseCurrentService, smqBaseCurrentService, meddraDictCurrentService, cmqRelationCurrentService,
                cmqBaseTargetService, smqBaseTargetService, meddraDictTargetService, cmqRelationTargetService);
        treeHelper.onNodeExpandTargetTable(targetTableRootTreeNode, event);
	}
	
	public void updateCurrentTable() {
        IARelationsTreeHelper treeHelper = new IARelationsTreeHelper(
                cmqBaseCurrentService, smqBaseCurrentService, meddraDictCurrentService, cmqRelationCurrentService,
                cmqBaseTargetService, smqBaseTargetService, meddraDictTargetService, cmqRelationTargetService);
		//Init of the treenode to print only one selected list
		currentTableRootTreeNode = new DefaultTreeNode("root", new HierarchyNode("CODE",
				"LEVEL", "SCOPE", null), null);
		
		LOG.info("current called");	
		if (this.isImpactedCmqSelected) {
			treeHelper.updateCurrentTableForCmqList(currentTableRootTreeNode, this.selectedImpactedCmqList);
			//Current List name
			setListName(this.selectedImpactedCmqList.getCmqName());
		} else if (this.isNonImpactedCmqSelected) {
			treeHelper.updateCurrentTableForCmqList(currentTableRootTreeNode, this.selectedNotImpactedCmqList);
			//Current List name
			setListName(this.selectedNotImpactedCmqList.getCmqName());
		} else if (this.isImpactedSmqSelected) {
			treeHelper.updateCurrentTableForSmqList(currentTableRootTreeNode, this.selectedImpactedSmqList);
			//Current List name
			setListName(this.selectedImpactedSmqList.getSmqName());
		} else if (this.isNonImpactedSmqSelected) {
			treeHelper.updateCurrentTableForSmqList(currentTableRootTreeNode, this.selectedNotImpactedSmqList);
			//Current List name
			setListName(this.selectedNotImpactedSmqList.getSmqName());
		}
		
		//reset value of selected to ze
		currentOrTarget = SELECTED_NO_LIST;
	}

	public void updateTargetTable() {
        IARelationsTreeHelper treeHelper = new IARelationsTreeHelper(
                cmqBaseCurrentService, smqBaseCurrentService, meddraDictCurrentService, cmqRelationCurrentService,
                cmqBaseTargetService, smqBaseTargetService, meddraDictTargetService, cmqRelationTargetService);
		//Init of the treenode to print only one selected list
		targetTableRootTreeNode = new DefaultTreeNode("root", new HierarchyNode("CODE",
				"LEVEL", "SCOPE", "CATEGORY", "WEIGHT", null, null), null);
				
		LOG.info("target called");
		if(this.isImpactedCmqSelected) {
			treeHelper.updateTargetTableForCmqList(targetTableRootTreeNode, this.selectedImpactedCmqList);
			//Target List name
			setListName(this.selectedImpactedCmqList.getCmqName());
		} else if(this.isNonImpactedCmqSelected) {
			treeHelper.updateTargetTableForCmqList(targetTableRootTreeNode, this.selectedNotImpactedCmqList);
			//Target List name
			setListName(this.selectedNotImpactedCmqList.getCmqName());
		} else if (this.isImpactedSmqSelected) {
			treeHelper.updateTargetTableForSmqList(targetTableRootTreeNode, this.selectedImpactedSmqList);
			//Target List name
			setListName(this.selectedImpactedSmqList.getSmqName());
		} else if (this.isNonImpactedSmqSelected) {
			treeHelper.updateTargetTableForSmqList(targetTableRootTreeNode, this.selectedNotImpactedSmqList);
			//Target List name
			setListName(this.selectedNotImpactedSmqList.getSmqName());
		}
		//reset value of selected to ze
		currentOrTarget = SELECTED_NO_LIST;
	}
	
	/**
	 * only called when the target table has a CMQ. not for smq at all
	 * @param nodes
	 */
	public void addSelectedToTargetRelation(TreeNode[] nodes) {		
		try{
			if (nodes != null && nodes.length > 0) {
				List<TreeNode> nodesList = Arrays.asList(nodes);
				List<String> existingNodeTerms = new ArrayList<>();
				if(this.currentTableRootTreeNode.getChildCount() == 1){
					//count will always be either 0 or 1.
					TreeNode parentTreeNode = this.targetTableRootTreeNode.getChildren().get(0);
					HierarchyNode parentHierarchyNode = (HierarchyNode) parentTreeNode.getData();
					IEntity entity = (IEntity) parentHierarchyNode.getEntity();
					if(entity instanceof CmqBaseTarget) {
						//allow additions 
						for (TreeNode treeNode : nodesList) {
							HierarchyNode hierarchyNode = (HierarchyNode) treeNode.getData();
							if ((null != hierarchyNode) && !hierarchyNode.isDummyNode()) {
								//first check if this node is already added ot relations tree
								boolean exists = false;
								List<TreeNode> existingRelationsTreeNodes = parentTreeNode.getChildren();
								if (CollectionUtils.isNotEmpty(existingRelationsTreeNodes)) {
									for (TreeNode existingRelationsTreeNode : existingRelationsTreeNodes) {
										HierarchyNode existingHierarchyNode = (HierarchyNode) existingRelationsTreeNode.getData();
										if(hierarchyNode.getCode().equalsIgnoreCase(existingHierarchyNode.getCode())
												&& hierarchyNode.getLevel().equalsIgnoreCase(existingHierarchyNode.getLevel())) {
											exists = true;
											existingNodeTerms.add(existingHierarchyNode.getTerm());
											break;
										}
									}
								}
							
								if(!exists) {
									TreeNode parentNode = treeNode.getParent();
									if (!nodesList.contains(parentNode)) {
										HierarchyNode relationsHierarchyNode = hierarchyNode.copy();
										relationsHierarchyNode.setRowStyleClass("green-colored");//mark this row as green
										TreeNode relationsTreeNode = new DefaultTreeNode(relationsHierarchyNode, parentTreeNode);
										relationsHierarchyNode.setDataFetchCompleted(false);
										List<TreeNode> childTreeNodes = treeNode.getChildren();
										if(CollectionUtils.isNotEmpty(childTreeNodes)) {
											HierarchyNode dummyNode = new HierarchyNode(null, null, null, null);
											dummyNode.setDummyNode(true);
											new DefaultTreeNode(dummyNode, relationsTreeNode);
										}
										targetRelationsUpdated = true;
									}
								}
							}
						}
						
						// setRelationSelected(nodes);
						if(CollectionUtils.isNotEmpty(existingNodeTerms)) {
							FacesContext.getCurrentInstance().addMessage(null,
                                    new FacesMessage(FacesMessage.SEVERITY_WARN, "The relation already exists", ""));
						} else {
							FacesContext.getCurrentInstance().addMessage(null,
                                    new FacesMessage(FacesMessage.SEVERITY_INFO,
                                        "Selected relations added sucessfully.", ""));
						}
					} else {
						FacesContext.getCurrentInstance().addMessage(null, 
                                new FacesMessage(FacesMessage.SEVERITY_FATAL,
                                    "Adding relations is allowed for CMQs only.", ""));
					}
				}
			} else {
                FacesContext.getCurrentInstance().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_WARN, "The relation already exists", ""));
            }
		} catch (Exception e) {
			LOG.error(e.getMessage(), e);
			FacesContext.getCurrentInstance().addMessage(null, 
                    new FacesMessage(FacesMessage.SEVERITY_FATAL,
                        "An error occured while adding relations.", e.getMessage()));
		}
	}

	/**
	 * Update relations on a list.
	 * 
	 * @return
	 */
	public void updateTargetRelations() {
		if ((this.targetTableRootTreeNode != null) && (targetTableRootTreeNode.getChildCount() > 0)) {
			List<CmqRelationTarget> cmqRelationsList = new ArrayList<>();
			List<CmqBaseTarget> cmqBaseChildrenList = new ArrayList<>();
			
			if((this.currentTableRootTreeNode.getChildCount() == 1) && targetRelationsUpdated){
				//count will always be either 0 or 1.
				TreeNode parentTreeNode = this.targetTableRootTreeNode.getChildren().get(0);
				HierarchyNode parentHierarchyNode = (HierarchyNode) parentTreeNode.getData();
				IEntity parentEntity = (IEntity) parentHierarchyNode.getEntity();
				if(parentEntity instanceof CmqBaseTarget) {
					//allow save
					CmqBaseTarget cmqBaseTarget = (CmqBaseTarget) parentEntity;
					List<CmqRelationTarget> existingRelations = this.cmqRelationTargetService.findByCmqCode(cmqBaseTarget.getCmqCode());
					List<TreeNode> childTreeNodes = parentTreeNode.getChildren();
					if (CollectionUtils.isNotEmpty(childTreeNodes)) {
						for (TreeNode childTreeNode : childTreeNodes) {
							boolean matchFound = false;
							boolean updateNeeded = false;
							Map<String, Object> matchingMap = null;
							HierarchyNode hierarchyNode = (HierarchyNode) childTreeNode.getData();
							if (null != hierarchyNode) {
								IEntity childEntity = hierarchyNode.getEntity();
								if (childEntity instanceof CmqBaseTarget) {
									CmqBaseTarget cmqEntity = (CmqBaseTarget) childEntity;
									cmqEntity.setCmqParentCode(cmqBaseTarget.getCmqCode());
									cmqEntity.setCmqParentName(cmqBaseTarget.getCmqName());
									cmqEntity.setImpactType("ICC");
									cmqBaseChildrenList.add(cmqEntity);
								} else {
									CmqRelationTarget cmqRelation = null;
									if (childEntity instanceof MeddraDictHierarchySearchDto) {
										MeddraDictHierarchySearchDto meddraDictHierarchySearchDto = (MeddraDictHierarchySearchDto) childEntity;
										String level = hierarchyNode.getLevel();
										long meddraDictCode = Long.parseLong(meddraDictHierarchySearchDto.getCode());
										matchingMap = this.checkIfMeddraRelationExists(existingRelations, level, hierarchyNode);
										matchFound = (boolean) matchingMap.get("MATCH_FOUND");
										updateNeeded = (boolean) matchingMap.get("UPDATE_NEEDED");
										if(updateNeeded) {
											cmqRelation = (CmqRelationTarget) matchingMap.get("TARGET_CMQ_RELATION_FOR_UPDATE");
										} else {
											cmqRelation = new CmqRelationTarget();
											cmqRelation.setCmqCode(cmqBaseTarget.getCmqCode());
											cmqRelation.setCmqId(cmqBaseTarget.getId());
											cmqRelation.setRelationImpactType("MQM");
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
									} else if (childEntity instanceof MeddraDictReverseHierarchySearchDto) {
										MeddraDictReverseHierarchySearchDto searchDto = (MeddraDictReverseHierarchySearchDto)childEntity;
										Long code = null;
										if("PT".equalsIgnoreCase(hierarchyNode.getLevel())) {
											code = Long.parseLong(searchDto.getPtCode());
											matchingMap = this.checkIfReverseMeddraRelationExists(existingRelations, code, hierarchyNode);
										} else if ("LLT".equalsIgnoreCase(hierarchyNode.getLevel())) {
											code = Long.parseLong(searchDto.getLltCode());
											matchingMap = this.checkIfReverseMeddraRelationExists(existingRelations, code, hierarchyNode);
										}
										
										matchFound = (boolean) matchingMap.get("MATCH_FOUND");
										updateNeeded = (boolean) matchingMap.get("UPDATE_NEEDED");
										if(updateNeeded) {
											cmqRelation = (CmqRelationTarget) matchingMap.get("TARGET_CMQ_RELATION_FOR_UPDATE");
										} else if(!matchFound) {
											cmqRelation = new CmqRelationTarget();
											cmqRelation.setCmqCode(cmqBaseTarget.getCmqCode());
											cmqRelation.setCmqId(cmqBaseTarget.getId());
											cmqRelation.setRelationImpactType("MQM");
											if("PT".equalsIgnoreCase(hierarchyNode.getLevel())) {
												cmqRelation.setPtCode(code);
											} else if ("LLT".equalsIgnoreCase(hierarchyNode.getLevel())) {
												cmqRelation.setLltCode(code);
											}
										}
									} else if (childEntity instanceof SmqBaseTarget) {
										SmqBaseTarget smqBase = (SmqBaseTarget) childEntity;
										matchingMap = this.checkIfSmqBaseOrSmqRelationExists(existingRelations, smqBase.getSmqCode(), null, hierarchyNode);
										matchFound = (boolean) matchingMap.get("MATCH_FOUND");
										updateNeeded = (boolean) matchingMap.get("UPDATE_NEEDED");
										if(updateNeeded) {
											cmqRelation = (CmqRelationTarget) matchingMap.get("TARGET_CMQ_RELATION_FOR_UPDATE");
										} else if(!matchFound) {
											cmqRelation = new CmqRelationTarget();
											cmqRelation.setCmqCode(cmqBaseTarget.getCmqCode());
											cmqRelation.setCmqId(cmqBaseTarget.getId());
											cmqRelation.setSmqCode(smqBase.getSmqCode());
											cmqRelation.setRelationImpactType("MQM");
										}
									} else if (childEntity instanceof SmqRelationTarget) {
										SmqRelationTarget smqRelation = (SmqRelationTarget) childEntity;
										matchingMap = this.checkIfSmqBaseOrSmqRelationExists(existingRelations, smqRelation.getSmqCode()
																								, smqRelation.getPtCode(), hierarchyNode);
										matchFound = (boolean) matchingMap.get("MATCH_FOUND");
										updateNeeded = (boolean) matchingMap.get("UPDATE_NEEDED");
										if(updateNeeded) {
											cmqRelation = (CmqRelationTarget) matchingMap.get("TARGET_CMQ_RELATION_FOR_UPDATE");
										} else if(!matchFound) {
											cmqRelation = new CmqRelationTarget();
											cmqRelation.setCmqCode(cmqBaseTarget.getCmqCode());
											cmqRelation.setCmqId(cmqBaseTarget.getId());
											//we set both smqcode and pt code to show that this is an smq relation
											cmqRelation.setSmqCode(smqRelation.getSmqCode());
											cmqRelation.setPtCode(smqRelation.getPtCode().longValue());
											cmqRelation.setRelationImpactType("MQM");
										}
									}
									
									if(!matchFound || updateNeeded) {
										cmqRelation.setTermWeight((!StringUtils.isBlank(hierarchyNode.getWeight()) 
																		&& !hierarchyNode.getWeight().equalsIgnoreCase("null"))
																	? Long.parseLong(hierarchyNode.getWeight()) : null);
										cmqRelation.setTermScope(hierarchyNode.getScope());
										cmqRelation.setTermCategory(hierarchyNode.getCategory());
										cmqRelation.setDictionaryName(cmqBaseTarget.getDictionaryName());
										cmqRelation.setDictionaryVersion(cmqBaseTarget.getDictionaryVersion());
										cmqRelation.setCmqSubversion(cmqBaseTarget.getCmqSubversion());
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
							}
						}
						
						if (!cmqRelationsList.isEmpty() || !cmqBaseChildrenList.isEmpty()) {
							try {
								if(!cmqRelationsList.isEmpty()) {
									this.cmqRelationTargetService.update(cmqRelationsList, this.authService.getUserCn()
											, this.authService.getUserGivenName(), this.authService.getUserSurName()
											, this.authService.getCombinedMappedGroupMembershipAsString());
								}
								if(!cmqBaseChildrenList.isEmpty()) {
									this.cmqBaseTargetService.update(cmqBaseChildrenList, this.authService.getUserCn()
											, this.authService.getUserGivenName(), this.authService.getUserSurName()
											, this.authService.getCombinedMappedGroupMembershipAsString());
								}
								
								//mark the cmqbase as Impacted if it is NON-IMPACTED
								String impactType = cmqBaseTarget.getImpactType();
								if("NON-IMPACTED".equalsIgnoreCase(impactType)) {
									cmqBaseTarget.setImpactType("IMPACTED");
									cmqBaseTarget.setCmqState("PENDING IA");
									cmqBaseTarget.setCmqStatus("P");
									this.cmqBaseTargetService.update(cmqBaseTarget, this.authService.getUserCn()
											, this.authService.getUserGivenName(), this.authService.getUserSurName()
											, this.authService.getCombinedMappedGroupMembershipAsString());
								}
								
								FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_INFO,
										"Relations are successfully updated for target '" + cmqBaseTarget.getCmqName() + "'", "");
								FacesContext.getCurrentInstance().addMessage(null, msg);
								
								//reset the flag to track target changes
								targetRelationsUpdated = false;
							} catch (CqtServiceException e) {
								LOG.error("Exception occurred while updated the list of CmqRelations for CMQ base target code "
										+ cmqBaseTarget.getCmqCode(), e);

								FacesContext.getCurrentInstance().addMessage(null, 
                                        new FacesMessage(FacesMessage.SEVERITY_ERROR,
                                            "An error occurred while updated the list of CmqRelations for CMQ base code " + cmqBaseTarget.getCmqCode(),
                                            "Error:" + e.getMessage()));
							}
						}
					}//end of if (CollectionUtils.isNotEmpty(childTreeNodes)) 
				}//end of if(parentEntity instanceof CmqBaseTarget)
			}			
		}
	}
	
	private Map<String, Object> checkIfMeddraRelationExists(List<CmqRelationTarget> existingRelation, String matchKey, HierarchyNode hierarchyNode) {
		Map<String, Object> matchingMap = new HashMap<String, Object>(3);
		matchingMap.put("MATCH_FOUND", false);
		matchingMap.put("UPDATE_NEEDED", false);
		matchingMap.put("TARGET_CMQ_RELATION_FOR_UPDATE", null);
		
		MeddraDictHierarchySearchDto meddraDictHierarchySearchDto = (MeddraDictHierarchySearchDto) hierarchyNode.getEntity();
		long nodeSocCode = Long.parseLong(meddraDictHierarchySearchDto.getCode());
		for (CmqRelationTarget cmqRelationTarget : existingRelation) {
			if(null != cmqRelationTarget) {
				if(matchKey.equalsIgnoreCase("SOC")) {
					if((null != cmqRelationTarget.getSocCode()) && (cmqRelationTarget.getSocCode().longValue() == nodeSocCode)){
						matchingMap.put("MATCH_FOUND", true);
					}
				} else if(matchKey.equalsIgnoreCase("HLGT")) {
					if((null != cmqRelationTarget.getHlgtCode()) && (cmqRelationTarget.getHlgtCode().longValue() == nodeSocCode)){
						matchingMap.put("MATCH_FOUND", true);
					}
				} else if(matchKey.equalsIgnoreCase("HLT")) {
					if((null != cmqRelationTarget.getHltCode()) && (cmqRelationTarget.getHltCode().longValue() == nodeSocCode)){
						matchingMap.put("MATCH_FOUND", true);
					}
				} else if(matchKey.equalsIgnoreCase("PT")) {
					if((null != cmqRelationTarget.getPtCode()) && (cmqRelationTarget.getPtCode().longValue() == nodeSocCode)){
						matchingMap.put("MATCH_FOUND", true);
					}
				} else if(matchKey.equalsIgnoreCase("LLT")) {
					if((null != cmqRelationTarget.getLltCode()) && (cmqRelationTarget.getLltCode().longValue() == nodeSocCode)){
						matchingMap.put("MATCH_FOUND", true);
					}
				}
				Boolean matchFound = (Boolean) matchingMap.get("MATCH_FOUND");
				if(matchFound) {
					matchingMap.put("UPDATE_NEEDED", this.checkIfExistingRelationNeedsUpdate(cmqRelationTarget, hierarchyNode));
					matchingMap.put("TARGET_CMQ_RELATION_FOR_UPDATE", cmqRelationTarget);
					break;
				}
			}//end of if(null != cmqRelation190)
		}//end of for (CmqRelation190 cmqRelation190 : existingRelation) 
		return matchingMap;
	}
	
	private Map<String, Object> checkIfReverseMeddraRelationExists(List<CmqRelationTarget> existingRelations, Long code, HierarchyNode hierarchyNode) {
		Map<String, Object> matchingMap = new HashMap<String, Object>(3);
		matchingMap.put("MATCH_FOUND", false);
		matchingMap.put("UPDATE_NEEDED", false);
		matchingMap.put("TARGET_CMQ_RELATION_FOR_UPDATE", null);
		
		for (CmqRelationTarget cmqRelationTarget : existingRelations) {
			//check for PT or LLT
			if((null != cmqRelationTarget.getPtCode()) && (cmqRelationTarget.getPtCode().longValue() == code.longValue())){
				matchingMap.put("MATCH_FOUND", true);
			} else if ((null != cmqRelationTarget.getLltCode()) && (cmqRelationTarget.getLltCode().longValue() == code.longValue())) {
				matchingMap.put("MATCH_FOUND", true);
			}
			
			Boolean matchFound = (Boolean) matchingMap.get("MATCH_FOUND");
			if(matchFound) {
				matchingMap.put("UPDATE_NEEDED", this.checkIfExistingRelationNeedsUpdate(cmqRelationTarget, hierarchyNode));
				matchingMap.put("TARGET_CMQ_RELATION_FOR_UPDATE", cmqRelationTarget);
				break;
			}
		}
		return matchingMap;
	}
	
	private Map<String, Object> checkIfSmqBaseOrSmqRelationExists(List<CmqRelationTarget> existingRelations, Long smqCode, Integer ptCode
			, HierarchyNode hierarchyNode) {
		Map<String, Object> matchingMap = new HashMap<String, Object>(3);
		matchingMap.put("MATCH_FOUND", false);
		matchingMap.put("UPDATE_NEEDED", false);
		matchingMap.put("TARGET_CMQ_RELATION_FOR_UPDATE", null);

		for (CmqRelationTarget cmqRelationTarget : existingRelations) {
			if((null != ptCode) && (null != cmqRelationTarget.getSmqCode()) && (cmqRelationTarget.getSmqCode().longValue() == smqCode.longValue())
					&& (null != cmqRelationTarget.getPtCode()) && (cmqRelationTarget.getPtCode().longValue() == ptCode.longValue())){
				//its an smqrelation and not an smqbase
				matchingMap.put("MATCH_FOUND", true);
			} else if((null != cmqRelationTarget.getSmqCode()) && (cmqRelationTarget.getSmqCode().longValue() == smqCode.longValue())){
				//its an smqbase
				matchingMap.put("MATCH_FOUND", true);
			}
		
			Boolean matchFound = (Boolean) matchingMap.get("MATCH_FOUND");
			if(matchFound) {
				matchingMap.put("UPDATE_NEEDED", this.checkIfExistingRelationNeedsUpdate(cmqRelationTarget, hierarchyNode));
				matchingMap.put("TARGET_CMQ_RELATION_FOR_UPDATE", cmqRelationTarget);
				break;
			}
		}
		return matchingMap;
	}
	
	private boolean checkIfExistingRelationNeedsUpdate(CmqRelationTarget cmqRelationTarget, HierarchyNode hierarchyNode) {
		boolean needsUpdate = false;
		//first match scope
		if(StringUtils.isBlank(hierarchyNode.getScope()) && !StringUtils.isBlank(cmqRelationTarget.getTermScope())) {
			needsUpdate = true;
		} else if(!StringUtils.isBlank(hierarchyNode.getScope()) && StringUtils.isBlank(cmqRelationTarget.getTermScope())) {
			needsUpdate = true;
		} else if(!StringUtils.isBlank(hierarchyNode.getScope()) && !StringUtils.isBlank(cmqRelationTarget.getTermScope())
				&& !hierarchyNode.getScope().equals(cmqRelationTarget.getTermScope())){
			needsUpdate = true;
		}
		
		//now so category
		if(StringUtils.isBlank(hierarchyNode.getCategory()) && !StringUtils.isBlank(cmqRelationTarget.getTermCategory())) {
			needsUpdate = true;
		} else if(!StringUtils.isBlank(hierarchyNode.getCategory()) && StringUtils.isBlank(cmqRelationTarget.getTermCategory())) {
			needsUpdate = true;
		} else if(!StringUtils.isBlank(hierarchyNode.getCategory()) && !StringUtils.isBlank(cmqRelationTarget.getTermCategory())
				&& !hierarchyNode.getCategory().equals(cmqRelationTarget.getTermCategory())){
			needsUpdate = true;
		}
		
		//now weight
		if(StringUtils.isBlank(hierarchyNode.getWeight()) && (null != cmqRelationTarget.getTermWeight())) {
			needsUpdate = true;
		} else if(!StringUtils.isBlank(hierarchyNode.getWeight()) && (null == cmqRelationTarget.getTermWeight())) {
			needsUpdate = true;
		} else if(!StringUtils.isBlank(hierarchyNode.getWeight()) && (null != cmqRelationTarget.getTermWeight()) 
				&& StringUtils.isNumeric(hierarchyNode.getWeight())) {
			long nodeWeight = Long.parseLong(hierarchyNode.getWeight());
			if(nodeWeight != cmqRelationTarget.getTermWeight().longValue()) {
				needsUpdate = true;
			}
		}
		
		return needsUpdate;
	}

	/**
	 * FlowListener of Browse Wizard Component
	 * @param event
	 * @return
	 */
	public String onIaWizardFlowProcess(FlowEvent event) {
		boolean selected = true;
//		if (selectedImpactedCmqList != null || selectedImpactedSmqList != null || selectedNotImpactedCmqList != null || selectedNotImpactedSmqList != null) {
//			selected = true;			
//		}
		if (currentOrTarget == SELECTED_NO_LIST)
			selected = false;	
			
		if (!selected) {
		//	this.confirmMessage = "Select a List/SMQ to proceed";
			FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_WARN, "Select a List/SMQ to proceed", "");
			FacesContext.getCurrentInstance().addMessage(null, msg);
			RequestContext.getCurrentInstance().update("impactAssessment:messages");
			return "impact";
		}
		
		String nextStep = event.getNewStep();
		boolean unsavedRedirect = true;
		
		if( "impact".equalsIgnoreCase(event.getOldStep()) && (this.currentTableRootTreeNode.getChildCount() == 1) && targetRelationsUpdated) {
			// currently opened tab is "Impact" and it contains unsaved changes;
			iaWizardNextStep = nextStep;
			nextStep = event.getOldStep();
			RequestContext.getCurrentInstance().execute("PF('confirmSaveImpactsDlg').show();");
		} else if("notes".equalsIgnoreCase(event.getOldStep()) && notesFormModel.isModelChanged()) {
			iaWizardNextStep = nextStep;
			nextStep = event.getOldStep();
			RequestContext.getCurrentInstance().execute("PF('confirmSaveNotesDlg').show();");
		}  else if("details".equalsIgnoreCase(event.getOldStep()) && detailsFormModel.isModelChanged()) {
			iaWizardNextStep = nextStep;
			nextStep = event.getOldStep();
			RequestContext.getCurrentInstance().execute("PF('confirmSaveDetailsDlg').show();");
		} else 
			unsavedRedirect = false;
		
		if("impact".equalsIgnoreCase(event.getNewStep())) {
			// if the target tab is "Impact Assessment", allow it always
			if(unsavedRedirect)
				iaWizardNextStep = event.getNewStep();
			else
				nextStep = event.getNewStep();
		} else if("notes".equalsIgnoreCase(event.getNewStep())) {
			// if the target tab is "notes", allow it only when there is a valid CmqBase* or SmqBase* object selected from current or target lists
			Object d = null;
			if(currentOrTarget == SELECTED_CURRENT_LIST && currentTableSelection != null) {
				HierarchyNode hn = (HierarchyNode)currentTableSelection.getData();
				d = (hn != null ? hn.getEntity() : null); 
			} else if(currentOrTarget == SELECTED_TARGET_LIST && targetTableSelection != null) {
				HierarchyNode hn = (HierarchyNode)targetTableSelection.getData();
				d = (hn != null ? hn.getEntity() : null); 
			}
			
			if(d instanceof CmqBase190) {
				notesFormModel.loadFromCmqBase190((CmqBase190)d);
				detailsFormModel.loadFromCmqBase190((CmqBase190)d);
			} else if(d instanceof SmqBase190) {
				notesFormModel.loadFromSmqBase190((SmqBase190)d);
			} else if(d instanceof CmqBaseTarget) {
				notesFormModel.loadFromCmqBaseTarget((CmqBaseTarget)d);
				detailsFormModel.loadFromCmqBaseTarget((CmqBaseTarget)d);
			} else if(d instanceof SmqBaseTarget) {
				notesFormModel.loadFromSmqBaseTarget((SmqBaseTarget)d);
			} else {
				if(unsavedRedirect)
					iaWizardNextStep = event.getOldStep();
				else
					nextStep = event.getOldStep();
			}
		} else if("details".equalsIgnoreCase(event.getNewStep())) {
			// if the target tab is "details", allow it only when there is a valid CmqBase* or SmqBase* object selected from current or target lists
			Object d = null;
			if(currentOrTarget == SELECTED_CURRENT_LIST && currentTableSelection != null) {
				HierarchyNode hn = (HierarchyNode)currentTableSelection.getData();
				d = (hn != null ? hn.getEntity() : null); 					
			} else if(currentOrTarget == SELECTED_TARGET_LIST && targetTableSelection != null) {
				HierarchyNode hn = (HierarchyNode)targetTableSelection.getData();
				d = (hn != null ? hn.getEntity() : null); 
			}
			if(d instanceof CmqBase190) {
				detailsFormModel.loadFromCmqBase190((CmqBase190)d);
			} else if(d instanceof CmqBaseTarget) {
				detailsFormModel.loadFromCmqBaseTarget((CmqBaseTarget)d);
			} else if(d instanceof SmqBase190) {
				FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_WARN, "Details tab is not accessible for SMQs", "");
				FacesContext.getCurrentInstance().addMessage(null, msg);
				RequestContext.getCurrentInstance().update("impactAssessment:messages");
				//if (event.)
				return "notes";
				
			} else if(d instanceof SmqBaseTarget) {
				FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_WARN, "Details tab is not accessible for SMQs", "");
				FacesContext.getCurrentInstance().addMessage(null, msg);
				RequestContext.getCurrentInstance().update("impactAssessment:messages");
				return "notes";
				
			} 
			else {
				if(unsavedRedirect)
					iaWizardNextStep = event.getOldStep();
				else
					nextStep = event.getOldStep();
			}
		}
		return nextStep;
	}
	
	/**
	 * FlowListener of Browse Wizard Component
	 * @param event
	 * @return
	 */
	public String onIaVersionWizardFlowProcess(FlowEvent event) {
		boolean selected = true;

		if (currentOrTarget == SELECTED_NO_LIST)
			selected = false;	
			
		if (!selected) {
			FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_WARN, "Select a List/SMQ to proceed", "");
			FacesContext.getCurrentInstance().addMessage(null, msg);
			RequestContext.getCurrentInstance().update("impactAssessment:messages");
			return "impact";
		}
		
		String nextStep = event.getNewStep();
		
		
		/*if("impact".equalsIgnoreCase(event.getNewStep())) {
			// if the target tab is "Impact Assessment", allow it always
			if(unsavedRedirect)
				iaWizardNextStep = event.getNewStep();
			else
				nextStep = event.getNewStep();
		} else*/ if("notes".equalsIgnoreCase(event.getNewStep())) {
			// if the target tab is "notes", allow it only when there is a valid CmqBase* or SmqBase* object selected from current or target lists
			Object d = null;
			if(currentOrTarget == SELECTED_CURRENT_LIST && currentTableSelection != null) {
				HierarchyNode hn = (HierarchyNode)currentTableSelection.getData();
				d = (hn != null ? hn.getEntity() : null); 
			} else if(currentOrTarget == SELECTED_TARGET_LIST && targetTableSelection != null) {
				HierarchyNode hn = (HierarchyNode)targetTableSelection.getData();
				d = (hn != null ? hn.getEntity() : null); 
			}
			
			if(d instanceof CmqBase190) {
				notesFormModel.loadFromCmqBase190((CmqBase190)d);
				detailsFormModel.loadFromCmqBase190((CmqBase190)d);
			} else if(d instanceof SmqBase190) {
				notesFormModel.loadFromSmqBase190((SmqBase190)d);
			} else if(d instanceof CmqBaseTarget) {
				notesFormModel.loadFromCmqBaseTarget((CmqBaseTarget)d);
				detailsFormModel.loadFromCmqBaseTarget((CmqBaseTarget)d);
			} else if(d instanceof SmqBaseTarget) {
				notesFormModel.loadFromSmqBaseTarget((SmqBaseTarget)d);
			}
			
		} else if("details".equalsIgnoreCase(event.getNewStep())) {
			// if the target tab is "details", allow it only when there is a valid CmqBase* or SmqBase* object selected from current or target lists
			Object d = null;
			if(currentOrTarget == SELECTED_CURRENT_LIST && currentTableSelection != null) {
				HierarchyNode hn = (HierarchyNode)currentTableSelection.getData();
				d = (hn != null ? hn.getEntity() : null); 					
			} else if(currentOrTarget == SELECTED_TARGET_LIST && targetTableSelection != null) {
				HierarchyNode hn = (HierarchyNode)targetTableSelection.getData();
				d = (hn != null ? hn.getEntity() : null); 
			}
			if(d instanceof CmqBase190) {
				detailsFormModel.loadFromCmqBase190((CmqBase190)d);
			} else if(d instanceof CmqBaseTarget) {
				detailsFormModel.loadFromCmqBaseTarget((CmqBaseTarget)d);
			} else if(d instanceof SmqBase190) {
				FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_WARN, "Details tab is not accessible for SMQs", "");
				FacesContext.getCurrentInstance().addMessage(null, msg);
				RequestContext.getCurrentInstance().update("impactAssessment:messages");
				//if (event.)
				return "notes";
				
			} else if(d instanceof SmqBaseTarget) {
				FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_WARN, "Details tab is not accessible for SMQs", "");
				FacesContext.getCurrentInstance().addMessage(null, msg);
				RequestContext.getCurrentInstance().update("impactAssessment:messages");
				return "notes";
				
			} 			
		}
		return nextStep;
	}
	
	public void saveNotesAndGoToNextStep() {
		saveInformativeNotes();
		iaWizard.setStep(iaWizardNextStep);
	}
	
	public void cancelNotesAndGoToNextStep() {
		cancelNotes();
		iaWizard.setStep(iaWizardNextStep);
	}
	
	public void saveInformativeNotes() {
		Object d = null;
		if(currentOrTarget == SELECTED_CURRENT_LIST && currentTableSelection != null) {
			HierarchyNode hn = (HierarchyNode)currentTableSelection.getData();
			d = (hn != null ? hn.getEntity() : null); 
		} else if(currentOrTarget == SELECTED_TARGET_LIST && targetTableSelection != null) {
			HierarchyNode hn = (HierarchyNode)targetTableSelection.getData();
			d = (hn != null ? hn.getEntity() : null); 
		}
		
		try {
			if(d != null && d instanceof CmqBase190) {
				notesFormModel.saveToCmqBase190((CmqBase190)d);
				cmqBaseCurrentService.update((CmqBase190)d, this.authService.getUserCn()
						, this.authService.getUserGivenName(), this.authService.getUserSurName()
						, this.authService.getCombinedMappedGroupMembershipAsString());
			} else if(d != null && d instanceof SmqBase190) {
				notesFormModel.saveToSmqBase190((SmqBase190)d);
				//
			} else if(d != null && d instanceof CmqBaseTarget) {
				notesFormModel.saveToCmqBaseTarget((CmqBaseTarget)d);
				cmqBaseTargetService.update((CmqBaseTarget)d, this.authService.getUserCn()
						, this.authService.getUserGivenName(), this.authService.getUserSurName()
						, this.authService.getCombinedMappedGroupMembershipAsString());
			} else if(d != null && d instanceof SmqBaseTarget) {
				notesFormModel.saveToSmqBaseTarget((SmqBaseTarget)d);
				//
			}
			FacesContext.getCurrentInstance()
				.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Successfully saved notes", ""));
		} catch(CqtServiceException e) {
			FacesContext.getCurrentInstance()
				.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Failed to save notes", "Error: " + e.getMessage()));
		}
	}
	
	public void cancelNotes() {
		Object d = null;
		if(currentOrTarget == SELECTED_CURRENT_LIST && currentTableSelection != null) {
			HierarchyNode hn = (HierarchyNode)currentTableSelection.getData();
			d = (hn != null ? hn.getEntity() : null); 
		} else if(currentOrTarget == SELECTED_TARGET_LIST && targetTableSelection != null) {
			HierarchyNode hn = (HierarchyNode)targetTableSelection.getData();
			d = (hn != null ? hn.getEntity() : null); 
		}
		if(d != null && d instanceof CmqBase190) {
			notesFormModel.loadFromCmqBase190((CmqBase190)d);
		} else if(d != null && d instanceof SmqBase190) {
			notesFormModel.loadFromSmqBase190((SmqBase190)d);
			//
		} else if(d != null && d instanceof CmqBaseTarget) {
			notesFormModel.loadFromCmqBaseTarget((CmqBaseTarget)d);
		} else if(d != null && d instanceof SmqBaseTarget) {
			notesFormModel.loadFromSmqBaseTarget((SmqBaseTarget)d);
			//
		}
	}
	
	public void saveDetails() {
		Object d = null;
		if(currentOrTarget == SELECTED_CURRENT_LIST && currentTableSelection != null) {
			HierarchyNode hn = (HierarchyNode)currentTableSelection.getData();
			d = (hn != null ? hn.getEntity() : null); 
		} else if(currentOrTarget == SELECTED_TARGET_LIST && targetTableSelection != null) {
			HierarchyNode hn = (HierarchyNode)targetTableSelection.getData();
			d = (hn != null ? hn.getEntity() : null); 
		}
		
		try {
			if(d != null && d instanceof CmqBase190) {
				((CmqBase190)d).setCmqDesignee(detailsFormModel.getDesignee());
				cmqBaseCurrentService.update((CmqBase190)d, this.authService.getUserCn()
						, this.authService.getUserGivenName(), this.authService.getUserSurName()
						, this.authService.getCombinedMappedGroupMembershipAsString());
			} else if(d != null && d instanceof CmqBaseTarget) {
				((CmqBaseTarget)d).setCmqDesignee(detailsFormModel.getDesignee());
				cmqBaseTargetService.update((CmqBaseTarget)d, this.authService.getUserCn()
						, this.authService.getUserGivenName(), this.authService.getUserSurName()
						, this.authService.getCombinedMappedGroupMembershipAsString());
			}
			
			FacesContext.getCurrentInstance()
				.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Successfully saved details", ""));
		} catch(CqtServiceException e) {
			FacesContext.getCurrentInstance()
				.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Failed to save details", "Error: " + e.getMessage()));
		}
	}
	
	public void cancelDetails() {
		Object d = null;
		if(currentOrTarget == SELECTED_CURRENT_LIST && currentTableSelection != null) {
			HierarchyNode hn = (HierarchyNode)currentTableSelection.getData();
			d = (hn != null ? hn.getEntity() : null); 
		} else if(currentOrTarget == SELECTED_TARGET_LIST && targetTableSelection != null) {
			HierarchyNode hn = (HierarchyNode)targetTableSelection.getData();
			d = (hn != null ? hn.getEntity() : null); 
		}
		
		if(d != null && d instanceof CmqBase190) {
			detailsFormModel.loadFromCmqBase190((CmqBase190) d);
		} else if(d != null && d instanceof CmqBaseTarget) {
			detailsFormModel.loadFromCmqBaseTarget((CmqBaseTarget) d);
		}
		
		FacesContext.getCurrentInstance()
			.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Canceled the details form", ""));
	}
	
	public void saveDetailsAndGoToNextStep() {
		saveDetails();
		iaWizard.setStep(iaWizardNextStep);
	}
	
	public void cancelDetailsAndGoToNextStep() {
		cancelDetails();
		iaWizard.setStep(iaWizardNextStep);
	}
	
	public void loadNewPts() {
		this.newPtSearchLazyDataModel = new NewPtSearchLazyDataModel();
		//reload the table
		DataTable dataTable = (DataTable)  FacesContext.getCurrentInstance().getViewRoot().findComponent("impactAssessment:newPtResultList");
		dataTable.reset();
		dataTable.loadLazyData();
	}

	public void addSelectedNewPtsToTargetRelation() {
        IARelationsTreeHelper treeHelper = new IARelationsTreeHelper(cmqBaseTargetService, smqBaseTargetService, meddraDictTargetService, cmqRelationTargetService);
		if((this.selectedNewPtLists != null) && (this.currentTableRootTreeNode.getChildCount() == 1)) {
			List<String> existingNodeTerms = new ArrayList<>();
			//count will always be either 0 or 1.
			TreeNode parentTreeNode = this.targetTableRootTreeNode.getChildren().get(0);
			for (MeddraDictHierarchySearchDto meddraDictHierarchySearchDto : selectedNewPtLists) {
				Long dtoCode = Long.valueOf(meddraDictHierarchySearchDto.getCode());
				
				//first check if this node is already added ot relations tree
				boolean exists = false;
				List<TreeNode> existingRelationsTreeNodes = parentTreeNode.getChildren();
				if (CollectionUtils.isNotEmpty(existingRelationsTreeNodes)) {
					for (TreeNode existingRelationsTreeNode : existingRelationsTreeNodes) {
						HierarchyNode existingHierarchyNode = (HierarchyNode) existingRelationsTreeNode.getData();
						IEntity existingEntity = existingHierarchyNode.getEntity();
						if(existingEntity instanceof MeddraDictHierarchySearchDto) {
							if(dtoCode.toString().equalsIgnoreCase(existingHierarchyNode.getCode())
									&& "PT".toString().equalsIgnoreCase(existingHierarchyNode.getLevel())) {
								exists = true;
								existingNodeTerms.add(existingHierarchyNode.getTerm());
								break;
							}
						}
					}
				}
				
				if(!exists) {
					HierarchyNode node = treeHelper.createMeddraNode(meddraDictHierarchySearchDto, "PT");
					node.setRowStyleClass("green-colored");
					TreeNode treeNode = new DefaultTreeNode(node, parentTreeNode);
				
					Long countOfChildren = this.meddraDictTargetService.findChldrenCountByParentCode("LLT_", "PT_", dtoCode);
					if((null != countOfChildren) && (countOfChildren > 0)) {
						// add a dummmy node to show expand arrow
						HierarchyNode dummyNode = new HierarchyNode(null, null,
								null, null);
						dummyNode.setDummyNode(true);
						new DefaultTreeNode(dummyNode, treeNode);
					}
				}
			}
			
			// setRelationSelected(nodes);
			if(CollectionUtils.isNotEmpty(existingNodeTerms)) {
				FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_INFO, "The relation already exists", "");
				FacesContext.getCurrentInstance().addMessage(null, message);
			} else {
				FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "Selected New PTs added successfully.", "");
				FacesContext ctx = FacesContext.getCurrentInstance();
				ctx.addMessage(null, msg);
			}
			targetRelationsUpdated = true;
		} else {
			FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "No New PTs selected for addition to target table.", "");
			FacesContext ctx = FacesContext.getCurrentInstance();
			ctx.addMessage(null, msg);
		}
	}
	
	public void deleteRelation(TreeNode rootNodeToSearchFrom,
			HierarchyNode selectedNode) {
		if (rootNodeToSearchFrom.getChildCount() > 0) {
			List<TreeNode> childTreeNodes = rootNodeToSearchFrom.getChildren();
			for (Iterator<TreeNode> treeNodeIterator = childTreeNodes
					.listIterator(); treeNodeIterator.hasNext();) {
				TreeNode childTreeNode = treeNodeIterator.next();
				HierarchyNode childNode = (HierarchyNode) childTreeNode
						.getData();
				if (childNode.equals(selectedNode)) {
					treeNodeIterator.remove(); // remove it from the root node
					this.deleteRelationFromDb(childNode);
					break;
				} else if (childTreeNode.getChildCount() > 0) {
					// drill down
					this.deleteRelation(childTreeNode, selectedNode);
				}
			}
		}
	}
	
	private void deleteRelationFromDb(HierarchyNode hierarchyNode) {
		if (null != hierarchyNode) {
			TreeNode parentTreeNode = this.targetTableRootTreeNode.getChildren().get(0);
			HierarchyNode parentHierarchyNode = (HierarchyNode) parentTreeNode.getData();
			IEntity parentEntity = (IEntity) parentHierarchyNode.getEntity();
			CmqBaseTarget cmqBaseTarget = (CmqBaseTarget) parentEntity;
			Long parentCmqCode = cmqBaseTarget.getCmqCode();
			IEntity entity = hierarchyNode.getEntity();
			if(null != entity) {
				boolean isDeletSuccessful = false;
				if (entity instanceof CmqBaseTarget) {
					CmqBaseTarget cmqEntity = (CmqBaseTarget) entity;
					cmqEntity.setCmqParentCode(null);
					cmqEntity.setCmqParentName(null);
					try {
						this.cmqBaseTargetService.update(cmqEntity, this.authService.getUserCn()
								, this.authService.getUserGivenName(), this.authService.getUserSurName()
								, this.authService.getCombinedMappedGroupMembershipAsString());
						isDeletSuccessful = true;
					} catch (CqtServiceException e) {
						String exception = CmqUtils.getExceptionMessageChain(e);
						LOG.error("Error while removing cmq_parent_code value from cmq_id " + cmqEntity.getId(), e);
						FacesMessage message = new FacesMessage(
								FacesMessage.SEVERITY_ERROR, "Loading....",
								"Error while removing cmq_parent_code value from cmq_id " + cmqEntity.getId() + " Error is:" + exception);
						FacesContext.getCurrentInstance().addMessage(null, message);
					}
				} else {
					List<CmqRelationTarget> existingRelation = this.cmqRelationTargetService.findByCmqCode(parentCmqCode);
					if((null != existingRelation) && (existingRelation.size() > 0)) {
						boolean matchFound = false;
						Long cmqRelationIdToDelete = null;
						if(entity instanceof MeddraDictHierarchySearchDto) {
							MeddraDictHierarchySearchDto meddraDictHierarchySearchDto = (MeddraDictHierarchySearchDto) hierarchyNode.getEntity();
							String level = hierarchyNode.getLevel();
							long nodeCode = Long.parseLong(meddraDictHierarchySearchDto.getCode());
							for (CmqRelationTarget cmqRelationTarget : existingRelation) {
								if(level.equalsIgnoreCase("SOC")) {
									if((null != cmqRelationTarget.getSocCode()) 
											&& (cmqRelationTarget.getSocCode().longValue() == nodeCode)){
										matchFound = true;
									}
								} else if(level.equalsIgnoreCase("HLGT")) {
									if((null != cmqRelationTarget.getHlgtCode()) 
											&& (cmqRelationTarget.getHlgtCode().longValue() == nodeCode)){
										matchFound = true;
									}
								} else if(level.equalsIgnoreCase("HLT")) {
									if((null != cmqRelationTarget.getHltCode()) 
											&& (cmqRelationTarget.getHltCode().longValue() == nodeCode)){
										matchFound = true;
									}
								} else if(level.equalsIgnoreCase("PT")) {
									if((null != cmqRelationTarget.getPtCode()) 
											&& (cmqRelationTarget.getPtCode().longValue() == nodeCode)){
										matchFound = true;
									}
								} else if(level.equalsIgnoreCase("LLT")) {
									if((null != cmqRelationTarget.getLltCode()) 
											&& (cmqRelationTarget.getLltCode().longValue() == nodeCode)){
										matchFound = true;
									}
								}
								if(matchFound) {
									cmqRelationIdToDelete = cmqRelationTarget.getId();
									break;
								}
							}
						} else if(entity instanceof MeddraDictReverseHierarchySearchDto) {
							MeddraDictReverseHierarchySearchDto reverseSearchDto = (MeddraDictReverseHierarchySearchDto) hierarchyNode.getEntity();
							String level = hierarchyNode.getLevel();
							for (CmqRelationTarget cmqRelationTarget : existingRelation) {
								if(level.equalsIgnoreCase("PT") && (cmqRelationTarget.getPtCode() != null)) {
									Long relationPtCode = cmqRelationTarget.getPtCode();
									Long reverseSearchDtoPtCode = null;
									if(null != reverseSearchDto.getPtCode()) {
										reverseSearchDtoPtCode = Long.valueOf(reverseSearchDto.getPtCode());
									}
									if((reverseSearchDtoPtCode != null) 
											&& (relationPtCode.longValue() == reverseSearchDtoPtCode.longValue())) {
										matchFound = true;
									}
								} else if(level.equalsIgnoreCase("LLT") && (null != cmqRelationTarget.getLltCode())) {
									Long relationLltCode = cmqRelationTarget.getLltCode();
									Long reverseSearchDtoLltCode = null;
									if(null != reverseSearchDto.getLltCode()) {
										reverseSearchDtoLltCode = Long.valueOf(reverseSearchDto.getLltCode());
									}
									if((reverseSearchDtoLltCode != null) 
											&& (relationLltCode.longValue() == reverseSearchDtoLltCode.longValue())) {
										matchFound = true;
									}
								}
								if(matchFound) {
									cmqRelationIdToDelete = cmqRelationTarget.getId();
									break;
								}
							}//end of for (CmqRelation190 cmqRelation190 : existingRelation)
						} else if (entity instanceof SmqBaseTarget) {
							SmqBaseTarget smqBase = (SmqBaseTarget) entity;
							for (CmqRelationTarget cmqRelationTarget : existingRelation) {
								if((null != cmqRelationTarget.getSmqCode()) 
										&& (cmqRelationTarget.getSmqCode().longValue() == smqBase.getSmqCode().longValue())){
									matchFound = true;
									cmqRelationIdToDelete = cmqRelationTarget.getId();
									break;
								}
							}//end of for (CmqRelation190 cmqRelation190 : existingRelation)
						} else if (entity instanceof SmqRelationTarget) {
							SmqRelationTarget smqRelation = (SmqRelationTarget) entity;
							for (CmqRelationTarget cmqRelationTarget : existingRelation) {
								if((null != cmqRelationTarget.getSmqCode()) 
										&& (cmqRelationTarget.getSmqCode().longValue() == smqRelation.getSmqCode().longValue())){
									matchFound = true;
									cmqRelationIdToDelete = cmqRelationTarget.getId();
									break;
								}
							}//end of for (CmqRelation190 cmqRelation190 : existingRelation)
						}
						
						if(matchFound && (cmqRelationIdToDelete != null)) {
							try {
								this.cmqRelationTargetService.remove(cmqRelationIdToDelete, this.authService.getUserCn()
										, this.authService.getUserGivenName(), this.authService.getUserSurName()
										, this.authService.getCombinedMappedGroupMembershipAsString());
								isDeletSuccessful = true;
							} catch (CqtServiceException e) {
								LOG.error("Error while removing cmqbase relation.", e);
								String exception = CmqUtils.getExceptionMessageChain(e);
								FacesMessage message = new FacesMessage(
										FacesMessage.SEVERITY_ERROR, "Loading....",
										"Failed to delete the relation for cmqRelationId. Error is: "
												+ exception);
								FacesContext.getCurrentInstance().addMessage(null, message);
							}
						}
					}
				}//end of if(null != entity)
				
				if(isDeletSuccessful) {
					//make the cmq target as impacted.
					cmqBaseTarget.setImpactType("IMPACTED");
					try {
						this.cmqBaseTargetService.update(cmqBaseTarget, this.authService.getUserCn()
								, this.authService.getUserGivenName(), this.authService.getUserSurName()
								, this.authService.getCombinedMappedGroupMembershipAsString());
						FacesContext.getCurrentInstance().addMessage(null, 
                                new FacesMessage(FacesMessage.SEVERITY_INFO,
                                        "Relation deleted successfully.", ""));
					} catch (CqtServiceException e) {
						LOG.error("Error while making the cmq target as IMPACTED.", e);
						String exception = CmqUtils.getExceptionMessageChain(e);
						FacesContext.getCurrentInstance().addMessage(null,
                                new FacesMessage(FacesMessage.SEVERITY_ERROR,
                                        "Loading....", 
                                        "Error while making the cmq target as IMPACTED. Error is: " + exception));
					}
				}
			}//end of if (null != hierarchyNode)
		}//end of if (null != hierarchyNode)
	}
	
	/**
	 * Workflow States update.
	 * @param state String
	 * @return String
	 */
	public String workflowIAState(String state) {
		if(this.isImpactedCmqSelected) {
			updateWorkflowStates(this.selectedImpactedCmqList, state);
		} else if(this.isNonImpactedCmqSelected){
			updateWorkflowStates(this.selectedNotImpactedCmqList, state);
		}
		
		//Update of target
		try {
			if (this.isImpactedCmqSelected)
				cmqBaseTargetService.update(selectedImpactedCmqList, this.authService.getUserCn()
						, this.authService.getUserGivenName(), this.authService.getUserSurName()
						, this.authService.getCombinedMappedGroupMembershipAsString());
			if (this.isNonImpactedCmqSelected)
				cmqBaseTargetService.update(selectedNotImpactedCmqList, this.authService.getUserCn()
						, this.authService.getUserGivenName(), this.authService.getUserSurName()
						, this.authService.getCombinedMappedGroupMembershipAsString());
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO,
                            "Workflow state has been updated", ""));
		} catch (CqtServiceException e) {
			e.printStackTrace();
			FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "An error occurred while updating the state of the List", "Error: " + e.getMessage()));
		}
		
//		setReviewEnabled(false);
//		setApproveEnabled(false);
//		setDemoteEnabled(false); 
		
		return "";
	}
	
	private void updateWorkflowStates(CmqBaseTarget target, String state) {
		if (target != null) {
			if (state.equals("review")) {
				// Pending IA to Reviewed IA
				if (target.getCmqState().equals("PENDING IA"))
					target.setCmqState("REVIEWED IA");
			} else if (state.equals("approve")) {
				// Review IA to Approved IA
				if (target.getCmqState().equals("REVIEWED IA"))
					target.setCmqState("APPROVED IA");
			} else if (state.equals("demote")) {
				// Review IA/APPROVED IA to Pending IA
				if (target.getCmqState().equals("REVIEWED IA")
						|| target.getCmqState().equals("APPROVED IA"))
					target.setCmqState("PENDING IA");
			}

		}
	}
	
	private class CmqLazyDataModel extends LazyDataModel<CmqBaseTarget> {

		private static final long serialVersionUID = -8027413902738365916L;

		private List<CmqBaseTarget> cmqBaseList = new ArrayList<>();

		private boolean manageImpactedList;

		public CmqLazyDataModel(boolean manageImpactedList) {
			this.manageImpactedList = manageImpactedList;
		}

		@Override
		public List<CmqBaseTarget> load(int first, int pageSize, List<SortMeta> multiSortMeta,
				Map<String, Object> filters) {
			List<CmqBaseTarget> fetchedCmqBaseList = null;
			if (this.manageImpactedList) {
				LOG.info("Loading more impacted list cmqs starting from " + first + " with page size of " + pageSize);
				fetchedCmqBaseList = cmqBaseTargetService.findImpactedWithPaginated(first, pageSize, null, null, filters);
				this.setRowCount(cmqBaseTargetService.findImpactedCount().intValue());
			} else {
				LOG.info("Loading more not impacted list cmqs starting from " + first + " with page size of "
						+ pageSize);
				fetchedCmqBaseList = cmqBaseTargetService.findNotImpactedWithPaginated(first, pageSize, null, null, filters);
				this.setRowCount(cmqBaseTargetService.findNotImpactedCount().intValue());
			}
            
            if(fetchedCmqBaseList != null)
                this.cmqBaseList.addAll(fetchedCmqBaseList);
			return fetchedCmqBaseList;
		}

		@Override
		public List<CmqBaseTarget> load(int first, int pageSize, String sortField, SortOrder sortOrder,
				Map<String, Object> filters) {
			List<CmqBaseTarget> fetchedCmqBaseList = null;
			if (this.manageImpactedList) {
				LOG.info("Loading more impacted list cmqs starting from " + first + " with page size of " + pageSize);
				fetchedCmqBaseList = cmqBaseTargetService.findImpactedWithPaginated(first, pageSize, null, null, filters);
				this.setRowCount(cmqBaseTargetService.findImpactedCount().intValue());
			} else {
				LOG.info("Loading more not impacted list cmqs starting from " + first + " with page size of "
						+ pageSize);
				fetchedCmqBaseList = cmqBaseTargetService.findNotImpactedWithPaginated(first, pageSize, null, null, filters);
				this.setRowCount(cmqBaseTargetService.findNotImpactedCount().intValue());
			}
            
            if(fetchedCmqBaseList != null)
                this.cmqBaseList.addAll(fetchedCmqBaseList);
			return fetchedCmqBaseList;
		}

		@Override
		public CmqBaseTarget getRowData(String rowKey) {
			long rowKeyLong = Long.parseLong(rowKey);
			for (CmqBaseTarget cmqBaseTarget : cmqBaseList) {
				if (cmqBaseTarget.getId().longValue() == rowKeyLong) {
					return cmqBaseTarget;
				}
			}
			return null;
		}

		@Override
		public Object getRowKey(CmqBaseTarget object) {
			return object.getId();
		}

	}
	
	private class SmqLazyDataModel extends LazyDataModel<SmqBaseTarget> {

		private static final long serialVersionUID = -8027413902738365916L;

		private List<SmqBaseTarget> smqBaseList = new ArrayList<>();

		private boolean manageImpactedList;

		public SmqLazyDataModel(boolean manageImpactedList) {
			this.manageImpactedList = manageImpactedList;
		}

		@Override
		public List<SmqBaseTarget> load(int first, int pageSize, List<SortMeta> multiSortMeta,
				Map<String, Object> filters) {
			List<SmqBaseTarget> fetchedSmqBaseList = null;
			if (this.manageImpactedList) {
				LOG.info("Loading more impacted list smqs starting from " + first + " with page size of " + pageSize);
				fetchedSmqBaseList = smqBaseTargetService.findImpactedWithPaginated(first, pageSize, null, null, filters);
				this.setRowCount(smqBaseTargetService.findImpactedCount().intValue());
			} else {
				LOG.info("Loading more not impacted list smqs starting from " + first + " with page size of "
						+ pageSize);
				fetchedSmqBaseList = smqBaseTargetService.findNotImpactedWithPaginated(first, pageSize, null, null, filters);
				this.setRowCount(smqBaseTargetService.findNotImpactedCount().intValue());
			}
            
            if(fetchedSmqBaseList != null)
                this.smqBaseList.addAll(fetchedSmqBaseList);
			return fetchedSmqBaseList;
		}

		@Override
		public List<SmqBaseTarget> load(int first, int pageSize, String sortField, SortOrder sortOrder,
				Map<String, Object> filters) {
			List<SmqBaseTarget> fetchedSmqBaseList = null;
			if (this.manageImpactedList) {
				LOG.info("Loading more impacted list cmqs starting from " + first + " with page size of " + pageSize);
				fetchedSmqBaseList = smqBaseTargetService.findImpactedWithPaginated(first, pageSize, null, null, filters);
				this.setRowCount(smqBaseTargetService.findImpactedCount().intValue());
			} else {
				LOG.info("Loading more not impacted list cmqs starting from " + first + " with page size of "
						+ pageSize);
				fetchedSmqBaseList = smqBaseTargetService.findNotImpactedWithPaginated(first, pageSize, null, null, filters);
				this.setRowCount(smqBaseTargetService.findNotImpactedCount().intValue());
			}
            
            if(fetchedSmqBaseList != null)
                this.smqBaseList.addAll(fetchedSmqBaseList);
			return fetchedSmqBaseList;
		}

		@Override
		public SmqBaseTarget getRowData(String rowKey) {
			long rowKeyLong = Long.parseLong(rowKey);
			for (SmqBaseTarget smqBaseTarget : smqBaseList) {
				if (smqBaseTarget.getId().longValue() == rowKeyLong) {
					return smqBaseTarget;
				}
			}
			return null;
		}

		@Override
		public Object getRowKey(SmqBaseTarget object) {
			return object.getId();
		}

	}
	
	private class NewPtSearchLazyDataModel extends LazyDataModel<MeddraDictHierarchySearchDto> {

		private static final long serialVersionUID = 7103755193083915253L;

		private List<MeddraDictHierarchySearchDto> meddraDictHierarchySearchDtos = new ArrayList<>();

		@Override
		public List<MeddraDictHierarchySearchDto> load(int first, int pageSize, List<SortMeta> multiSortMeta,
				Map<String, Object> filters) {
			List<MeddraDictHierarchySearchDto> fetchedMeddraDictHierarchySearchDtos = null;
			LOG.info("Loading more new pt list MeddraDictHierarchySearchDto starting from " + first + " with page size of " + pageSize);
			String searchTerm = null;
			if(newPtSocSearchTerm != null) {
				searchTerm = newPtSocSearchTerm;
			}
			fetchedMeddraDictHierarchySearchDtos = meddraDictTargetService.findNewPtTerm(searchTerm, first, pageSize);
			this.setRowCount(meddraDictTargetService.findNewPtTermRowCount(searchTerm).intValue());
			this.meddraDictHierarchySearchDtos.addAll(fetchedMeddraDictHierarchySearchDtos);
			
			return fetchedMeddraDictHierarchySearchDtos;
		}

		@Override
		public List<MeddraDictHierarchySearchDto> load(int first, int pageSize, String sortField, SortOrder sortOrder,
				Map<String, Object> filters) {
			List<MeddraDictHierarchySearchDto> fetchedMeddraDictHierarchySearchDtos = null;
			LOG.info("Loading more new pt list MeddraDictHierarchySearchDto starting from " + first + " with page size of " + pageSize);
			String searchTerm = null;
			if(newPtSocSearchTerm != null) {
				searchTerm = newPtSocSearchTerm;
			}
			fetchedMeddraDictHierarchySearchDtos = meddraDictTargetService.findNewPtTerm(searchTerm, first, pageSize);
			this.setRowCount(meddraDictTargetService.findNewPtTermRowCount(searchTerm).intValue());
			this.meddraDictHierarchySearchDtos.addAll(fetchedMeddraDictHierarchySearchDtos);
			
			return fetchedMeddraDictHierarchySearchDtos;
		}

		@Override
		public MeddraDictHierarchySearchDto getRowData(String rowKey) {
			long rowKeyLong = Long.parseLong(rowKey);
			for (MeddraDictHierarchySearchDto meddraDictHierarchySearchDto : meddraDictHierarchySearchDtos) {
				if (meddraDictHierarchySearchDto.getId().longValue() == rowKeyLong) {
					return meddraDictHierarchySearchDto;
				}
			}
			return null;
		}

		@Override
		public Object getRowKey(MeddraDictHierarchySearchDto object) {
			return object.getId();
		}

	}

	public IMeddraDictService getMeddraDictCurrentService() {
		return meddraDictCurrentService;
	}

	public void setMeddraDictCurrentService(IMeddraDictService meddraDictCurrentService) {
		this.meddraDictCurrentService = meddraDictCurrentService;
	}

	public ICmqBase190Service getCmqBaseCurrentService() {
		return cmqBaseCurrentService;
	}

	public void setCmqBaseCurrentService(ICmqBase190Service cmqBaseCurrentService) {
		this.cmqBaseCurrentService = cmqBaseCurrentService;
	}

	public ICmqRelation190Service getCmqRelationCurrentService() {
		return cmqRelationCurrentService;
	}

	public void setCmqRelationCurrentService(ICmqRelation190Service cmqRelationCurrentService) {
		this.cmqRelationCurrentService = cmqRelationCurrentService;
	}

	public ICmqBaseTargetService getCmqBaseTargetService() {
		return cmqBaseTargetService;
	}

	public void setCmqBaseTargetService(ICmqBaseTargetService cmqBaseTargetService) {
		this.cmqBaseTargetService = cmqBaseTargetService;
	}

	public ICmqRelationTargetService getCmqRelationTargetService() {
		return cmqRelationTargetService;
	}

	public void setCmqRelationTargetService(ICmqRelationTargetService cmqRelationTargetService) {
		this.cmqRelationTargetService = cmqRelationTargetService;
	}

	public ISmqBaseService getSmqBaseCurrentService() {
		return smqBaseCurrentService;
	}

	public void setSmqBaseCurrentService(ISmqBaseService smqBaseCurrentService) {
		this.smqBaseCurrentService = smqBaseCurrentService;
	}

	public IRefCodeListService getRefCodeListService() {
		return refCodeListService;
	}

	public void setRefCodeListService(IRefCodeListService refCodeListService) {
		this.refCodeListService = refCodeListService;
	}

	public LazyDataModel<CmqBaseTarget> getImpactedCmqBaseLazyDataModel() {
		return impactedCmqBaseLazyDataModel;
	}

	public void setImpactedCmqBaseLazyDataModel(LazyDataModel<CmqBaseTarget> impactedCmqBaseLazyDataModel) {
		this.impactedCmqBaseLazyDataModel = impactedCmqBaseLazyDataModel;
	}

	public CmqBaseTarget getSelectedImpactedCmqList() {
		return selectedImpactedCmqList;
	}

	public void setSelectedImpactedCmqList(CmqBaseTarget selectedImpactedCmqList) {
		this.selectedImpactedCmqList = selectedImpactedCmqList;
	}

	public LazyDataModel<CmqBaseTarget> getNotImpactedCmqBaseLazyDataModel() {
		return notImpactedCmqBaseLazyDataModel;
	}

	public void setNotImpactedCmqBaseLazyDataModel(LazyDataModel<CmqBaseTarget> notImpactedCmqBaseLazyDataModel) {
		this.notImpactedCmqBaseLazyDataModel = notImpactedCmqBaseLazyDataModel;
	}

	public CmqBaseTarget getSelectedNotImpactedCmqList() {
		return selectedNotImpactedCmqList;
	}

	public void setSelectedNotImpactedCmqList(CmqBaseTarget selectedNotImpactedCmqList) {
		this.selectedNotImpactedCmqList = selectedNotImpactedCmqList;
	}

	public TreeNode getCurrentTableRootTreeNode() {
		return currentTableRootTreeNode;
	}

	public void setCurrentTableRootTreeNode(TreeNode currentTableRootTreeNode) {
		this.currentTableRootTreeNode = currentTableRootTreeNode;
	}

	public TreeNode getTargetTableRootTreeNode() {
		return targetTableRootTreeNode;
	}

	public void setTargetTableRootTreeNode(TreeNode targetTableRootTreeNode) {
		this.targetTableRootTreeNode = targetTableRootTreeNode;
	}

	public IMeddraDictTargetService getMeddraDictTargetService() {
		return meddraDictTargetService;
	}

	public void setMeddraDictTargetService(IMeddraDictTargetService meddraDictTargetService) {
		this.meddraDictTargetService = meddraDictTargetService;
	}

	public ISmqBaseTargetService getSmqBaseTargetService() {
		return smqBaseTargetService;
	}

	public void setSmqBaseTargetService(ISmqBaseTargetService smqBaseTargetService) {
		this.smqBaseTargetService = smqBaseTargetService;
	}

	public boolean isReviewEnabled() {
        CmqBaseTarget t = (this.isImpactedCmqSelected ? this.selectedImpactedCmqList : this.selectedNotImpactedCmqList);
        return (currentOrTarget == SELECTED_TARGET_LIST && t != null && "PENDING IA".equals(t.getCmqState()));
	}

	public boolean isDemoteEnabled() {
		CmqBaseTarget t = (this.isImpactedCmqSelected ? this.selectedImpactedCmqList : this.selectedNotImpactedCmqList);
        return (currentOrTarget == SELECTED_TARGET_LIST && t != null && ("REVIEWED IA".equals(t.getCmqState()) || "APPROVED IA".equals(t.getCmqState())));
	}

	public boolean isApproveEnabled() {
		CmqBaseTarget t = (this.isImpactedCmqSelected ? this.selectedImpactedCmqList : this.selectedNotImpactedCmqList);
        return (currentOrTarget == SELECTED_TARGET_LIST && t != null && "REVIEWED IA".equals(t.getCmqState()));
	}

	public LazyDataModel<SmqBaseTarget> getImpactedSmqBaseLazyDataModel() {
		return impactedSmqBaseLazyDataModel;
	}

	public void setImpactedSmqBaseLazyDataModel(LazyDataModel<SmqBaseTarget> impactedSmqBaseLazyDataModel) {
		this.impactedSmqBaseLazyDataModel = impactedSmqBaseLazyDataModel;
	}

	public SmqBaseTarget getSelectedImpactedSmqList() {
		return selectedImpactedSmqList;
	}

	public void setSelectedImpactedSmqList(SmqBaseTarget selectedImpactedSmqList) {
		this.selectedImpactedSmqList = selectedImpactedSmqList;
	}

	public LazyDataModel<SmqBaseTarget> getNotImpactedSmqBaseLazyDataModel() {
		return notImpactedSmqBaseLazyDataModel;
	}

	public void setNotImpactedSmqBaseLazyDataModel(LazyDataModel<SmqBaseTarget> notImpactedSmqBaseLazyDataModel) {
		this.notImpactedSmqBaseLazyDataModel = notImpactedSmqBaseLazyDataModel;
	}

	public SmqBaseTarget getSelectedNotImpactedSmqList() {
		return selectedNotImpactedSmqList;
	}

	public void setSelectedNotImpactedSmqList(SmqBaseTarget selectedNotImpactedSmqList) {
		this.selectedNotImpactedSmqList = selectedNotImpactedSmqList;
	}
    
	public TreeNode[] getRelationSelected() {
		return relationSelected;
	}

	public void setRelationSelected(TreeNode[] relationSelected) {
		this.relationSelected = relationSelected;
	}

	public TreeNode[] getRelationSelectedInRelationsTable() {
		return relationSelectedInRelationsTable;
	}

	public void setRelationSelectedInRelationsTable(TreeNode[] relationSelectedInRelationsTable) {
		this.relationSelectedInRelationsTable = relationSelectedInRelationsTable;
	}

	public TreeNode getCurrentTableSelection() {
		return currentTableSelection;
	}

	public void setCurrentTableSelection(TreeNode currentTableSelection) {
		this.currentTableSelection = currentTableSelection;
	}

	public TreeNode getTargetTableSelection() {
		return targetTableSelection;
	}

	public void setTargetTableSelection(TreeNode targetTableSelection) {
		this.targetTableSelection = targetTableSelection;
	}

	public ListNotesFormVM getNotesFormModel() {
		return notesFormModel;
	}

	public void setNotesFormModel(ListNotesFormVM notesFormModel) {
		this.notesFormModel = notesFormModel;
	}

	public ListDetailsFormVM getDetailsFormModel() {
		return detailsFormModel;
	}

	public void setDetailsFormModel(ListDetailsFormVM detailsFormModel) {
		this.detailsFormModel = detailsFormModel;
	}
	
	public boolean isNotesFormReadonly() {
		if (this.currentOrTarget == SELECTED_CURRENT_LIST || (detailsFormModel.getState().equals(CmqBaseTarget.CMQ_STATE_APPROVED_IA) || detailsFormModel.getState().equals(CmqBaseTarget.CMQ_STATE_PUBLISHED_IA)))
			return true;
		if (this.currentOrTarget == SELECTED_TARGET_LIST)
			return false;
		return true;
	}
	
	public boolean isDetailsFormReadonly() {
		if (this.currentOrTarget == SELECTED_CURRENT_LIST || (detailsFormModel.getState().equals(CmqBaseTarget.CMQ_STATE_APPROVED_IA) || detailsFormModel.getState().equals(CmqBaseTarget.CMQ_STATE_PUBLISHED_IA)))
			return true;
		if (this.currentOrTarget == SELECTED_TARGET_LIST)
			return false;
		return true;
	}

/*	public boolean isChangeOccur() {
		return changeOccur;
	}

	public void setChangeOccur(boolean changeOccur) {
		this.changeOccur = changeOccur;
	}*/
	
	public Wizard getIaWizard() {
		return iaWizard;
	}

	public void setIaWizard(Wizard iaWizard) {
		this.iaWizard = iaWizard;
	}

	public LazyDataModel<MeddraDictHierarchySearchDto> getNewPtSearchLazyDataModel() {
		return newPtSearchLazyDataModel;
	}

	public void setNewPtSearchLazyDataModel(LazyDataModel<MeddraDictHierarchySearchDto> newPtSearchLazyDataModel) {
		this.newPtSearchLazyDataModel = newPtSearchLazyDataModel;
	}

	public MeddraDictHierarchySearchDto[] getSelectedNewPtLists() {
		return selectedNewPtLists;
	}

	public void setSelectedNewPtLists(MeddraDictHierarchySearchDto[] selectedNewPtLists) {
		this.selectedNewPtLists = selectedNewPtLists;
	}

	public String getNewPtSocSearchTerm() {
		return newPtSocSearchTerm;
	}

	public void setNewPtSocSearchTerm(String newPtSocSearchTerm) {
		this.newPtSocSearchTerm = newPtSocSearchTerm;
	}

	public List<String> getNewPtDistinctSocTermsList() {
		return newPtDistinctSocTermsList;
	}

	public void setNewPtDistinctSocTermsList(List<String> newPtDistinctSocTermsList) {
		this.newPtDistinctSocTermsList = newPtDistinctSocTermsList;
	}
	
	public boolean isSelectNewPTButtonEnabled() {
		return ((selectedImpactedCmqList != null) ||
				(selectedNotImpactedCmqList != null) ||
				(selectedImpactedSmqList != null) ||
				(selectedNotImpactedSmqList != null)); 
	}
	
	public boolean isHierachySearchButtonEnabled() {
		return ((selectedImpactedCmqList != null) ||
				(selectedNotImpactedCmqList != null) ||
				(selectedImpactedSmqList != null) ||
				(selectedNotImpactedSmqList != null)); 
	}

	public String getListName() {
		return listName;
	}

	public void setListName(String listName) {
		this.listName = listName;
	}

	public boolean isExportEnabled() {
		return (currentOrTarget == SELECTED_TARGET_LIST);
	}

	public StreamedContent getExcelFile() {
		return excelFile;
	}

	public void setExcelFile(StreamedContent excelFile) {
		this.excelFile = excelFile;
	}

	public String getConfirmMessage() {
		return confirmMessage;
	}

	public void setConfirmMessage(String confirmMessage) {
		this.confirmMessage = confirmMessage;
	}
    
    public TargetHierarchySearchVM getTargetHierarchySearchDlgModel() {
        return targetHierarchySearchDlgModel;
    }

    public void setTargetHierarchySearchDlgModel(TargetHierarchySearchVM targetHierarchySearchDlgModel) {
        this.targetHierarchySearchDlgModel = targetHierarchySearchDlgModel;
    }

    public CmqBaseHierarchySearchVM getCurrentHierarchySearchDlgModel() {
        return currentHierarchySearchDlgModel;
    }

    public void setCurrentHierarchySearchDlgModel(CmqBaseHierarchySearchVM currentHierarchySearchDlgModel) {
        this.currentHierarchySearchDlgModel = currentHierarchySearchDlgModel;
    }

	public AuthenticationService getAuthService() {
		return authService;
	}

	public void setAuthService(AuthenticationService authService) {
		this.authService = authService;
	}

	public Wizard getIaVersionWizard() {
		return iaVersionWizard;
	}

	public void setIaVersionWizard(Wizard iaVersionWizard) {
		this.iaVersionWizard = iaVersionWizard;
	}

	public SWJSFRequest getAppSWJSFRequest() {
		return appSWJSFRequest;
	}

	public void setAppSWJSFRequest(SWJSFRequest appSWJSFRequest) {
		this.appSWJSFRequest = appSWJSFRequest;
	}

}
