package com.dbms.controller;

import java.io.Serializable;
import java.util.ArrayList;
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
import org.primefaces.model.TreeNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dbms.csmq.HierarchyNode;
import com.dbms.entity.IEntity;
import com.dbms.entity.cqt.CmqBase190;
import com.dbms.entity.cqt.CmqBaseTarget;
import com.dbms.entity.cqt.CmqRelation190;
import com.dbms.entity.cqt.CmqRelationTarget;
import com.dbms.entity.cqt.SmqBase190;
import com.dbms.entity.cqt.SmqBaseTarget;
import com.dbms.entity.cqt.SmqRelation190;
import com.dbms.entity.cqt.SmqRelationTarget;
import com.dbms.entity.cqt.dtos.MeddraDictHierarchySearchDto;
import com.dbms.entity.cqt.dtos.MeddraDictReverseHierarchySearchDto;
import com.dbms.service.ICmqBase190Service;
import com.dbms.service.ICmqBaseTargetService;
import com.dbms.service.ICmqRelation190Service;
import com.dbms.service.ICmqRelationTargetService;
import com.dbms.service.IMeddraDictService;
import com.dbms.service.IMeddraDictTargetService;
import com.dbms.service.IRefCodeListService;
import com.dbms.service.ISmqBaseService;
import com.dbms.service.ISmqBaseTargetService;
import com.dbms.util.exceptions.CqtServiceException;
import com.dbms.view.ListDetailsFormModel;
import com.dbms.view.ListNotesFormModel;

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
	
	Wizard iaWizard;

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
	
	private ListNotesFormModel notesFormModel = new ListNotesFormModel(); // "Informative Notes" tab model
	private ListDetailsFormModel detailsFormModel = new ListDetailsFormModel(); // "Details" tab model
	
	private boolean reviewEnabled, demoteEnabled, approveEnabled;
	
	private boolean isImpactedCmqSelected, isNonImpactedCmqSelected, isImpactedSmqSelected, isNonImpactedSmqSelected;
	
	private String levelH;
	
	private TreeNode hierarchyRoot;
	
	private String termNameOfHierarchySearch;
	
	private TreeNode[] relationSelected;
	private TreeNode[] relationSelectedInRelationsTable;
	private TreeNode relationsRoot;
	private boolean changeOccur;
	
	
	
	public ImpactSearchController() {
		
	}

	@PostConstruct
	public void init() {
		this.impactedCmqBaseLazyDataModel = new CmqLazyDataModel(true);
		this.notImpactedCmqBaseLazyDataModel = new CmqLazyDataModel(false);
		this.impactedSmqBaseLazyDataModel = new SmqLazyDataModel(true);
		this.notImpactedSmqBaseLazyDataModel = new SmqLazyDataModel(false);
		currentTableRootTreeNode = new DefaultTreeNode("root", new HierarchyNode("CODE",
				"LEVEL", "SCOPE", null), null);
		targetTableRootTreeNode = new DefaultTreeNode("root", new HierarchyNode("CODE",
				"LEVEL", "SCOPE", "CATEGORY", "WEIGHT", null, null), null);
		this.hierarchyRoot = new DefaultTreeNode("root", new HierarchyNode(
				"LEVEL", "NAME", "CODE", null), null);
		relationsRoot = new DefaultTreeNode("root", new HierarchyNode("LEVEL",
				"NAME", "CODE", "SCOPE", "CATEGORY", "WEIGHT", null), null);
		setReviewEnabled(false);
		setApproveEnabled(false);
		setDemoteEnabled(false);
		currentOrTarget = SELECTED_NO_LIST;
		//changeOccur = false;
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
			if("IMPACTED".equalsIgnoreCase(cmqBase.getImpactType())){
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
			if("IMPACTED".equalsIgnoreCase(smqBase.getImpactType())){
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
		setReviewEnabled(false);
		setApproveEnabled(false);
		setDemoteEnabled(false);
		
		currentOrTarget = SELECTED_CURRENT_LIST;
	}
	/**
	 * Event fired on the unselection of a row from current list.
	 * @param event NodeSelectEvent
	 */
	public void onUnselectCurrentRowTreeTable(NodeUnselectEvent event) {
		//Updating the worflow buttons
		setReviewEnabled(false);
		setApproveEnabled(false);
		setDemoteEnabled(false);
		
		if(currentOrTarget == SELECTED_CURRENT_LIST)
			currentOrTarget = SELECTED_NO_LIST;
	}
	
	/**
	 * Event fired on the selection of a row.
	 * @param event NodeSelectEvent
	 */
	public void onSelectTargetRowTreeTable(NodeSelectEvent event) {
		//Updating the worflow buttons
		if(this.isImpactedCmqSelected) {
			updateWorkflowButtonStates(this.selectedImpactedCmqList);
		} else if(this.isNonImpactedCmqSelected) {
			updateWorkflowButtonStates(this.selectedNotImpactedCmqList);
		}
		
		currentOrTarget = SELECTED_TARGET_LIST;
	}
	/**
	 * Event fired on the unselection of a row from target list.
	 * @param event NodeSelectEvent
	 */
	public void onUnselectTargetRowTreeTable(NodeUnselectEvent event) {
		//Updating the worflow buttons
		setReviewEnabled(false);
		setApproveEnabled(false);
		setDemoteEnabled(false);
		if(currentOrTarget == SELECTED_TARGET_LIST)
			currentOrTarget = SELECTED_NO_LIST;
	}
	

	public void onNodeExpandCurrentTable(NodeExpandEvent event) {
		LOG.info("onNodeExpandCurrentTable");
		TreeNode expandedTreeNode = event.getTreeNode();
		HierarchyNode hierarchyNode = (HierarchyNode) expandedTreeNode.getData();
		boolean isDataFetchCompleted = hierarchyNode.isDataFetchCompleted();
		if (!isDataFetchCompleted) {
			IEntity entity = hierarchyNode.getEntity();
			
			//hierarchyNode.setRowStyleClass("blue-colored");

			// remove the first dummy node placeholder
			HierarchyNode dummyChildData = (HierarchyNode) expandedTreeNode
					.getChildren().get(0).getData();
			if (dummyChildData.isDummyNode()) {
				expandedTreeNode.getChildren().remove(0);
			}
			
			if (entity instanceof CmqBase190) {
				CmqBase190 cmqBase = (CmqBase190) entity;
				Long cmqCode = cmqBase.getCmqCode();
				this.populateCmqBaseChildren(cmqCode, expandedTreeNode, "current");
				this.populateCmqRelations(cmqCode, expandedTreeNode, "current");
			} else if (entity instanceof SmqBase190){
				SmqBase190 smqBase = (SmqBase190) entity;
				this.populateSmqBaseChildren(smqBase.getSmqCode(), expandedTreeNode, "current");
				this.populateSmqRelations(smqBase.getSmqCode(), expandedTreeNode, "current");
			} else if(entity instanceof MeddraDictHierarchySearchDto) {
				String parentLevel = hierarchyNode.getLevel();
				MeddraDictHierarchySearchDto meddraDictHierarchySearchDto = (MeddraDictHierarchySearchDto)entity;
				Long dtoCode = Long.valueOf(meddraDictHierarchySearchDto.getCode());
				this.populateMeddraDictHierarchySearchDtoChildren(parentLevel, dtoCode, expandedTreeNode, "current");
			}
			hierarchyNode.setDataFetchCompleted(true);
		}
		hierarchyNode.setRowStyleClass("blue-colored");
	}
	
	public void onNodeExpandTargetTable(NodeExpandEvent event) {
		LOG.info("onNodeExpandTargetTable");
		TreeNode expandedTreeNode = event.getTreeNode();
		HierarchyNode hierarchyNode = (HierarchyNode) expandedTreeNode.getData();
		boolean isDataFetchCompleted = hierarchyNode.isDataFetchCompleted();
		if (!isDataFetchCompleted) {
			IEntity entity = hierarchyNode.getEntity();
			

			// remove the first dummy node placeholder
			HierarchyNode dummyChildData = (HierarchyNode) expandedTreeNode.getChildren().get(0).getData();
			if (dummyChildData.isDummyNode()) {
				expandedTreeNode.getChildren().remove(0);
			}
			
			if (entity instanceof CmqBaseTarget) {
				CmqBaseTarget cmqBase = (CmqBaseTarget) entity;
				Long cmqCode = cmqBase.getCmqCode();
				this.populateCmqBaseChildren(cmqCode, expandedTreeNode, "target");
				this.populateCmqRelations(cmqCode, expandedTreeNode, "target");
			} else if (entity instanceof SmqBaseTarget){
				SmqBaseTarget smqBase = (SmqBaseTarget) entity;
				this.populateSmqBaseChildren(smqBase.getSmqCode(), expandedTreeNode, "target");
				this.populateSmqRelations(smqBase.getSmqCode(), expandedTreeNode, "target");
			} else if(entity instanceof MeddraDictHierarchySearchDto) {
				String parentLevel = hierarchyNode.getLevel();
				MeddraDictHierarchySearchDto meddraDictHierarchySearchDto = (MeddraDictHierarchySearchDto)entity;
				Long dtoCode = Long.valueOf(meddraDictHierarchySearchDto.getCode());
				this.populateMeddraDictHierarchySearchDtoChildren(parentLevel, dtoCode, expandedTreeNode, "target");
			} else if(entity instanceof MeddraDictReverseHierarchySearchDto) {
				MeddraDictReverseHierarchySearchDto reverseSearchDto = (MeddraDictReverseHierarchySearchDto)entity;
				String levelOfExpandedNode = hierarchyNode.getLevel();
				if("LLT".equalsIgnoreCase(levelOfExpandedNode)) {
					Long lltCode = Long.valueOf(reverseSearchDto.getLltCode());
					this.populateMeddraDictReverseHierarchySearchDtoChildren("LLT_", "PT", lltCode, hierarchyNode
																				, expandedTreeNode, reverseSearchDto, false);	
				} else if ("PT".equalsIgnoreCase(levelOfExpandedNode)) {
					Long ptCode = Long.valueOf(reverseSearchDto.getPtCode());
					this.populateMeddraDictReverseHierarchySearchDtoChildren("PT_", "HLT", ptCode, hierarchyNode
																				, expandedTreeNode, reverseSearchDto, true);		
				} else if ("HLT".equalsIgnoreCase(levelOfExpandedNode)) {
					Long hltCode = Long.valueOf(reverseSearchDto.getHltCode());
					this.populateMeddraDictReverseHierarchySearchDtoChildren("HLT_", "HLGT", hltCode, hierarchyNode
																				, expandedTreeNode, reverseSearchDto, false);	
				} else if ("HLGT".equalsIgnoreCase(levelOfExpandedNode)) {
					Long hlgtCode = Long.valueOf(reverseSearchDto.getHlgtCode());
					this.populateMeddraDictReverseHierarchySearchDtoChildren("HLGT_", "SOC", hlgtCode, hierarchyNode
																				, expandedTreeNode, reverseSearchDto, false);	
				}
			}
			hierarchyNode.setDataFetchCompleted(true);
			// 
		}
	}
	
	public void updateCurrentTable() {
		//Init of the treenode to print only one selected list
		currentTableRootTreeNode = new DefaultTreeNode("root", new HierarchyNode("CODE",
				"LEVEL", "SCOPE", null), null);
		
		LOG.info("current called");	
		if (this.isImpactedCmqSelected) {
			this.updateCurrentTableForCmqList(this.selectedImpactedCmqList);
		} else if (this.isNonImpactedCmqSelected) {
			this.updateCurrentTableForCmqList(this.selectedNotImpactedCmqList);
		} else if (this.isImpactedSmqSelected) {
			this.updateCurrentTableForSmqList(this.selectedImpactedSmqList);
		} else if (this.isNonImpactedSmqSelected) {
			this.updateCurrentTableForSmqList(this.selectedNotImpactedSmqList);
		}
	}

	public void updateTargetTable() {
		//Init of the treenode to print only one selected list
		targetTableRootTreeNode = new DefaultTreeNode("root", new HierarchyNode("CODE",
				"LEVEL", "SCOPE", "CATEGORY", "WEIGHT", null, null), null);
				
		LOG.info("target called");
		if(this.isImpactedCmqSelected) {
			this.updateTargetTableForCmqList(this.selectedImpactedCmqList);
		} else if(this.isNonImpactedCmqSelected) {
			this.updateTargetTableForCmqList(this.selectedNotImpactedCmqList);
		} else if (this.isImpactedSmqSelected) {
			this.updateTargetTableForSmqList(this.selectedImpactedSmqList);
		} else if (this.isNonImpactedSmqSelected) {
			this.updateTargetTableForSmqList(this.selectedNotImpactedSmqList);
		}
	}

	public void hierarchySearch() {
		int level = 0;
		String meddraSearchTermPrefix = null;
		boolean searchSmqBase = false;
		boolean searchMeddraBase = false;
		boolean searchMeddraBaseReverse = false;
		boolean searchCmqBase = false;
		
		if ("SMQ1".equalsIgnoreCase(levelH)) {
			level = 1;
			searchSmqBase = true;
		} else if ("SMQ2".equalsIgnoreCase(levelH)) {
			level = 2;
			searchSmqBase = true;
		} else if ("SMQ3".equalsIgnoreCase(levelH)) {
			level = 3;
			searchSmqBase = true;
		} else if ("SMQ4".equalsIgnoreCase(levelH)) {
			level = 4;
			searchSmqBase = true;
		} else if ("SMQ5".equalsIgnoreCase(levelH)) {
			level = 5;
			searchSmqBase = true;
		} else if ("SOC".equalsIgnoreCase(levelH)) {
			meddraSearchTermPrefix = "SOC_";
			searchMeddraBase = true;
		} else if ("HLGT".equalsIgnoreCase(levelH)) {
			meddraSearchTermPrefix = "HLGT_";
			searchMeddraBase = true;
		} else if ("HLT".equalsIgnoreCase(levelH)) {
			meddraSearchTermPrefix = "HLT_";
			searchMeddraBase = true;
		} else if ("PT".equalsIgnoreCase(levelH)) {
			meddraSearchTermPrefix = "PT_";
			searchMeddraBaseReverse = true;
		} else if ("LLT".equalsIgnoreCase(levelH)) {
			meddraSearchTermPrefix = "LLT_";
			searchMeddraBaseReverse = true;
		} else if ("PRO".equalsIgnoreCase(levelH)) {
			searchCmqBase = true;
		}
	
		if (searchSmqBase) {
			List<SmqBaseTarget> smqBaseList = this.smqBaseTargetService.findByLevelAndTerm(
					level, termNameOfHierarchySearch);
			LOG.info("smqBaseList values {}", smqBaseList == null ? 0 : smqBaseList.size());

			this.hierarchyRoot = new DefaultTreeNode("root", new HierarchyNode(
					"LEVEL", "NAME", "CODE", null), null);
			for (SmqBaseTarget smqBaseTarget : smqBaseList) {
				this.updateHierarchySearchForSmqTaget(smqBaseTarget);
			}
		} else if (searchMeddraBase) {
			List<MeddraDictHierarchySearchDto> meddraDictDtoList = this.meddraDictTargetService
					.findByLevelAndTerm(meddraSearchTermPrefix.toUpperCase(),
							termNameOfHierarchySearch);
			this.hierarchyRoot = new DefaultTreeNode("root", new HierarchyNode(
					"LEVEL", "NAME", "CODE", null), null);
			
			String childSearchColumnTypePrefix = null;
			String parentCodeColumnPrefix = levelH + "_";
			if ("SOC".equalsIgnoreCase(levelH)) {
				childSearchColumnTypePrefix = "HLGT_";
			} else if ("HLGT".equalsIgnoreCase(levelH)) {
				childSearchColumnTypePrefix = "HLT_";
			} else if ("HLT".equalsIgnoreCase(levelH)) {
				childSearchColumnTypePrefix = "PT_";
			} else if ("PT".equalsIgnoreCase(levelH)) {
				childSearchColumnTypePrefix = "LLT_";
			}
			
			for (MeddraDictHierarchySearchDto meddraDictDto : meddraDictDtoList) {
				this.updateHierarchySearchForMeddraDict(meddraDictDto, childSearchColumnTypePrefix, parentCodeColumnPrefix);
			}
			
		} else if (searchMeddraBaseReverse) {
			List<MeddraDictReverseHierarchySearchDto> meddraDictDtoList = this.meddraDictTargetService
					.findFullReverseHierarchyByLevelAndTerm(levelH, levelH, termNameOfHierarchySearch);
			this.hierarchyRoot = new DefaultTreeNode("root", new HierarchyNode("LEVEL", "NAME", "CODE", null), null);
			
			for (MeddraDictReverseHierarchySearchDto meddraDictReverseDto : meddraDictDtoList) {
				HierarchyNode node = this.createMeddraReverseNode(meddraDictReverseDto, levelH, true);
				TreeNode parentTreeNode = new DefaultTreeNode(node, this.hierarchyRoot);
				
				// add a dummmy node to show expand arrow
				HierarchyNode dummyNode = new HierarchyNode(null, null,
						null, null);
				dummyNode.setDummyNode(true);
				new DefaultTreeNode(dummyNode, parentTreeNode);
			}
		} else if (searchCmqBase) {
			List<CmqBaseTarget> cmqBaseList = this.cmqBaseTargetService.findByLevelAndTerm(2,
					termNameOfHierarchySearch);
			this.hierarchyRoot = new DefaultTreeNode("root", new HierarchyNode(
					"LEVEL", "NAME", "CODE", null), null);
			
			List<Long> parentCmqCodeList = new ArrayList<>();
			Map<Long, TreeNode> parentTreeNodes = new HashMap<Long, TreeNode>();
			
			this.updateParentCodesAndParentTreeNodesForCmqTaget(cmqBaseList, parentCmqCodeList, parentTreeNodes);
			
			if(CollectionUtils.isNotEmpty(parentCmqCodeList)) {
				this.updateHierarchySearchCmqChildNodes(parentCmqCodeList, parentTreeNodes);
				this.updateHierarchySearchCmqRelationChildNodes(parentCmqCodeList, parentTreeNodes);
			}
		}
		
	}
	

	/**
	 * FlowListener of Browse Wizard Component
	 * @param event
	 * @return
	 */
	public String onIaWizardFlowProcess(FlowEvent event) {
		String nextStep = event.getOldStep();
		if("notes".equalsIgnoreCase(event.getOldStep()) && notesFormModel.isModelChanged()) {
			RequestContext.getCurrentInstance().execute("PF('confirmSaveNotesDlg').show();");
		} else if("impact".equalsIgnoreCase(event.getNewStep())) {
			// if the target tab is "Impact Assessment", allow it always
			nextStep = event.getNewStep();
		} else {
			// if the target tab is NOT "Impact Assessment", allow it only when selected a list from left(current) or right(target) table.
			if(currentOrTarget == SELECTED_CURRENT_LIST && currentTableSelection != null) {
				nextStep = event.getNewStep();
				if("notes".equalsIgnoreCase(nextStep)) {
					HierarchyNode hn = (HierarchyNode)currentTableSelection.getData();
					Object d = (hn != null ? hn.getEntity() : null); 
					if(d instanceof CmqBase190) {
						LOG.info("CmqBase190");
						notesFormModel.loadFromCmqBase190((CmqBase190)d);
					} else if(d instanceof SmqBase190) {
						LOG.info("SmqBase190");
						notesFormModel.loadFromSmqBase190((SmqBase190)d);
					} else if(d instanceof CmqBaseTarget) {
						notesFormModel.loadFromCmqBaseTarget((CmqBaseTarget)d);
						LOG.info("CmqBaseTarget");
					} else if(d instanceof SmqBaseTarget) {
						notesFormModel.loadFromSmqBaseTarget((SmqBaseTarget)d);
						LOG.info("SmqBaseTarget");
					}
				} else if("details".equalsIgnoreCase(nextStep)) {
					HierarchyNode hn = (HierarchyNode)currentTableSelection.getData();
					Object d = (hn != null ? hn.getEntity() : null); 
					if(d instanceof CmqBase190) {
						detailsFormModel.loadFromCmqBase190((CmqBase190)d);
						LOG.info("CmqBase190");
					} else if(d instanceof SmqBase190) {
						LOG.info("SmqBase190");
						nextStep = event.getOldStep();
					} else if(d instanceof CmqBaseTarget) {
						LOG.info("CmqBaseTarget");
						detailsFormModel.loadFromCmqBaseTarget((CmqBaseTarget)d);
					} else if(d instanceof SmqBaseTarget) {
						LOG.info("SmqBaseTarget");
						nextStep = event.getOldStep();
					}
				}
			} else if(currentOrTarget == SELECTED_TARGET_LIST && targetTableSelection != null) {
				nextStep = event.getNewStep();
				if("notes".equalsIgnoreCase(nextStep)) {
					HierarchyNode hn = (HierarchyNode)targetTableSelection.getData();
					Object d = (hn != null ? hn.getEntity() : null); 
					if(d instanceof CmqBase190) {
						LOG.info("CmqBase190");
						notesFormModel.loadFromCmqBase190((CmqBase190)d);
					} else if(d instanceof SmqBase190) {
						notesFormModel.loadFromSmqBase190((SmqBase190)d);
						LOG.info("SmqBase190");
					} else if(d instanceof CmqBaseTarget) {
						notesFormModel.loadFromCmqBaseTarget((CmqBaseTarget)d);
						LOG.info("CmqBaseTarget");
					} else if(d instanceof SmqBaseTarget) {
						notesFormModel.loadFromSmqBaseTarget((SmqBaseTarget)d);
						LOG.info("SmqBaseTarget");
					}
				} else if("details".equalsIgnoreCase(nextStep)) {
					HierarchyNode hn = (HierarchyNode)targetTableSelection.getData();
					Object d = (hn != null ? hn.getEntity() : null); 
					if(d instanceof CmqBase190) {
						LOG.info("CmqBase190");
						detailsFormModel.loadFromCmqBase190((CmqBase190)d);
					} else if(d instanceof SmqBase190) {
						nextStep = event.getOldStep();
						LOG.info("SmqBase190");
					} else if(d instanceof CmqBaseTarget) {
						detailsFormModel.loadFromCmqBaseTarget((CmqBaseTarget)d);
						LOG.info("CmqBaseTarget");
					} else if(d instanceof SmqBaseTarget) {
						nextStep = event.getOldStep();
						LOG.info("SmqBaseTarget");
					}
				}
			} else {
				nextStep = event.getOldStep(); 
			}
		}
		return nextStep;
	}
	
	public void saveNotesAndGoToNextStep() {
		saveInformativeNotes();
	}
	public void cancelNotesAndGoToNextStep() {
		cancelNotes();
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
				cmqBaseCurrentService.update((CmqBase190)d);
			} else if(d != null && d instanceof SmqBase190) {
				notesFormModel.saveToSmqBase190((SmqBase190)d);
				//
			} else if(d != null && d instanceof CmqBaseTarget) {
				notesFormModel.saveToCmqBaseTarget((CmqBaseTarget)d);
				cmqBaseTargetService.update((CmqBaseTarget)d);
			} else if(d != null && d instanceof SmqBaseTarget) {
				notesFormModel.saveToSmqBaseTarget((SmqBaseTarget)d);
				//
			}
			FacesContext.getCurrentInstance()
				.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Successfully saved notes", ""));
		} catch(CqtServiceException e) {
			FacesContext.getCurrentInstance()
				.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Failed to save notes", ""));
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
				cmqBaseCurrentService.update((CmqBase190)d);
			} else if(d != null && d instanceof SmqBase190) {
				//
			} else if(d != null && d instanceof CmqBaseTarget) {
				((CmqBaseTarget)d).setCmqDesignee(detailsFormModel.getDesignee());
				cmqBaseTargetService.update((CmqBaseTarget)d);
			} else if(d != null && d instanceof SmqBaseTarget) {
				//
			}
			
			FacesContext.getCurrentInstance()
				.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Successfully saved details", ""));
		} catch(CqtServiceException e) {
			FacesContext.getCurrentInstance()
				.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Failed to save details", ""));
		}
	}
	
	private void updateParentCodesAndParentTreeNodesForCmqTaget(List<CmqBaseTarget> cmqBaseList
																	, List<Long> parentCmqCodeList
																	, Map<Long, TreeNode> parentTreeNodes) {
		for (CmqBaseTarget cmqBase : cmqBaseList) {
			HierarchyNode node = new HierarchyNode();
			node.setLevel(levelH);
			node.setTerm(cmqBase.getCmqName());
			node.setCode(cmqBase.getCmqCode().toString());
			node.setEntity(cmqBase);

			TreeNode cmqBaseTreeNode = new DefaultTreeNode(node,
					hierarchyRoot);
			parentTreeNodes.put(cmqBase.getCmqCode(), cmqBaseTreeNode);
			parentCmqCodeList.add(cmqBase.getCmqCode());
		}
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private void updateHierarchySearchCmqChildNodes(List<Long> parentCmqCodeList, Map<Long, TreeNode> parentTreeNodes) {
		List<Map<String, Object>> childCountsList = this.cmqBaseTargetService.findCmqChildCountForParentCmqCode(parentCmqCodeList);
		if((null != childCountsList) && (childCountsList.size() > 0)) {
			//first find and fix child nodes stuff
			for (Iterator<Long> it = parentCmqCodeList.iterator(); it.hasNext();) {
				ListIterator li = childCountsList.listIterator();
				Long parentCmqCode = it.next();
				while(li.hasNext()) {
					Map<String, Object> map = (Map<String, Object>) li.next();
					if(map.get("CMQ_CODE") != null) {
						Long cmqCode = (Long)map.get("CMQ_CODE");
						if(cmqCode.longValue() == parentCmqCode.longValue()) {
							Long count = (Long)map.get("COUNT");
							if(count > 0) {
								it.remove();//remove it from parentCmqCodeList
								
								//add a dummy node for this child in parent
								TreeNode parentTreeNode = parentTreeNodes.get(parentCmqCode);
								HierarchyNode dummyNode = new HierarchyNode(null, null,
										null, null);
								dummyNode.setDummyNode(true);
								new DefaultTreeNode(dummyNode, parentTreeNode);
							}
							break;
						}//end of if(cmqCode.longValue() == parentCmqCode.longValue())
					}
				}
			}
		}
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private void updateHierarchySearchCmqRelationChildNodes(List<Long> parentCmqCodeList, Map<Long, TreeNode> parentTreeNodes) {
		//now find relations for those who don't have children
		List<Map<String, Object>> relationsCountsList = this.cmqRelationTargetService.findCountByCmqCodes(parentCmqCodeList);	
		if((null != relationsCountsList) && (relationsCountsList.size() > 0)) {
			ListIterator li = relationsCountsList.listIterator();
			while(li.hasNext()) {
				Map<String, Object> map = (Map<String, Object>) li.next();
				if(map.get("CMQ_CODE") != null) {
					Long cmqCode = (Long)map.get("CMQ_CODE");
					Long count = (Long)map.get("COUNT");
					if(count > 0) {
						//add a dummy node for this child in parent
						TreeNode parentTreeNode = parentTreeNodes.get(cmqCode);
						HierarchyNode dummyNode = new HierarchyNode(null, null,
								null, null);
						dummyNode.setDummyNode(true);
						new DefaultTreeNode(dummyNode, parentTreeNode);
					}
				}
			}
		}
	}
	
	private void updateHierarchySearchForSmqTaget(SmqBaseTarget smqBaseTarget) {
		HierarchyNode node = this.createSmqBaseTargetNode(smqBaseTarget);
		TreeNode smqBaseTreeNode = new DefaultTreeNode(node, this.hierarchyRoot);
		
		boolean dummyNodeAdded = false;
		Long count = this.smqBaseTargetService.findChildSmqCountByParentSmqCode(smqBaseTarget.getSmqCode());
		if((count != null) && (count > 0)) {
			HierarchyNode dummyNode = new HierarchyNode(null, null, null, null);
			dummyNode.setDummyNode(true);
			new DefaultTreeNode(dummyNode, smqBaseTreeNode);
			dummyNodeAdded = true;
		}
		
		//check for relations now
		if(!dummyNodeAdded) {
			count = this.smqBaseTargetService.findSmqRelationsCountForSmqCode(smqBaseTarget.getSmqCode());
			if((count != null) && (count > 0)) {
				HierarchyNode dummyNode = new HierarchyNode(null, null, null, null);
				dummyNode.setDummyNode(true);
				new DefaultTreeNode(dummyNode, smqBaseTreeNode);
			}
		}
	}
	
	private void updateHierarchySearchForMeddraDict(MeddraDictHierarchySearchDto meddraDictDto
														, String childSearchColumnTypePrefix
														, String parentCodeColumnPrefix) {
		HierarchyNode node = this.createMeddraNode(meddraDictDto, levelH);
		TreeNode parentTreeNode = new DefaultTreeNode(node, this.hierarchyRoot);
		
		Long countOfChildren = this.meddraDictTargetService.findChldrenCountByParentCode(childSearchColumnTypePrefix,
				parentCodeColumnPrefix, Long.valueOf(meddraDictDto.getCode()));
		if((null != countOfChildren) && (countOfChildren > 0)) {
			// add a dummmy node to show expand arrow
			HierarchyNode dummyNode = new HierarchyNode(null, null,
					null, null);
			dummyNode.setDummyNode(true);
			new DefaultTreeNode(dummyNode, parentTreeNode);
		}
	}
	
	private void updateCurrentTableForCmqList(CmqBaseTarget selectedCmqList) {
		CmqBase190 cmqBaseCurrent = this.cmqBaseCurrentService.findByCode(selectedCmqList.getCmqCode());
		HierarchyNode node = this.createCmqBaseCurrentHierarchyNode(cmqBaseCurrent);
		TreeNode cmqBaseTreeNode = new DefaultTreeNode(node, currentTableRootTreeNode);
		if (changeOccur)
			node.setRowStyleClass("blue-colored");
		
		boolean dummyNodeAdded = false;
		Long count = this.cmqBaseCurrentService.findCmqChildCountForParentCmqCode(cmqBaseCurrent.getCmqCode());
		if((count != null) && (count > 0)) {
			HierarchyNode dummyNode = new HierarchyNode(null, null, null, null);
			dummyNode.setDummyNode(true);
			new DefaultTreeNode(dummyNode, cmqBaseTreeNode);
			dummyNodeAdded = true;
		}
		
		//check for relations now
		if(!dummyNodeAdded) {
			count = this.cmqRelationCurrentService.findCountByCmqCode(cmqBaseCurrent.getCmqCode());
			if((count != null) && (count > 0)) {
				HierarchyNode dummyNode = new HierarchyNode(null, null, null, null);
				dummyNode.setDummyNode(true);
				new DefaultTreeNode(dummyNode, cmqBaseTreeNode);
			}
		}
	}
	
	private void updateTargetTableForCmqList(CmqBaseTarget selectedCmqList) {
		HierarchyNode node = this.createCmqBaseTargetHierarchyNode(selectedCmqList);
		TreeNode cmqBaseTreeNode = new DefaultTreeNode(node, targetTableRootTreeNode);
		if (changeOccur)
			node.setRowStyleClass("blue-colored");
		
		boolean dummyNodeAdded = false;
		Long count = this.cmqBaseTargetService.findCmqChildCountForParentCmqCode(selectedCmqList.getCmqCode());
		if((count != null) && (count > 0)) {
			HierarchyNode dummyNode = new HierarchyNode(null, null, null, null);
			dummyNode.setDummyNode(true);
			new DefaultTreeNode(dummyNode, cmqBaseTreeNode);
			dummyNodeAdded = true;
		}
		
		//check for relations now
		if(!dummyNodeAdded) {
			count = this.cmqRelationTargetService.findCountByCmqCode(selectedCmqList.getCmqCode());
			if((count != null) && (count > 0)) {
				HierarchyNode dummyNode = new HierarchyNode(null, null, null, null);
				dummyNode.setDummyNode(true);
				new DefaultTreeNode(dummyNode, cmqBaseTreeNode);
			}
		}
	}
	
	private void updateCurrentTableForSmqList(SmqBaseTarget selectedSmqList) {
		SmqBase190 smqBaseCurrent = this.smqBaseCurrentService.findByCode(selectedSmqList.getSmqCode());
		if(null != smqBaseCurrent) {
			HierarchyNode node = this.createSmqBaseCurrrentNode(smqBaseCurrent);
			TreeNode cmqBaseTreeNode = new DefaultTreeNode(node, currentTableRootTreeNode);
			if (changeOccur)
				node.setRowStyleClass("blue-colored");
			
			boolean dummyNodeAdded = false;
			Long count = this.smqBaseCurrentService.findChildSmqCountByParentSmqCode(smqBaseCurrent.getSmqCode());
			if((count != null) && (count > 0)) {
				HierarchyNode dummyNode = new HierarchyNode(null, null, null, null);
				dummyNode.setDummyNode(true);
				new DefaultTreeNode(dummyNode, cmqBaseTreeNode);
				dummyNodeAdded = true;
			}
			
			//check for relations now
			if(!dummyNodeAdded) {
				count = this.smqBaseCurrentService.findSmqRelationsCountForSmqCode(smqBaseCurrent.getSmqCode());
				if((count != null) && (count > 0)) {
					HierarchyNode dummyNode = new HierarchyNode(null, null, null, null);
					dummyNode.setDummyNode(true);
					new DefaultTreeNode(dummyNode, cmqBaseTreeNode);
				}
			}
		} else {
			FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "No Current SQM found with code '" 
																				+ selectedSmqList.getSmqCode() + "'", "");
			FacesContext ctx = FacesContext.getCurrentInstance();
			ctx.addMessage(null, msg);
		}
	}
	
	private void updateTargetTableForSmqList(SmqBaseTarget selectedSmqList) {
		HierarchyNode node = this.createSmqBaseTargetNode(selectedSmqList);
		TreeNode cmqBaseTreeNode = new DefaultTreeNode(node, targetTableRootTreeNode);
		if (changeOccur)
			node.setRowStyleClass("blue-colored");
		
		boolean dummyNodeAdded = false;
		Long count = this.smqBaseCurrentService.findChildSmqCountByParentSmqCode(selectedSmqList.getSmqCode());
		if((count != null) && (count > 0)) {
			HierarchyNode dummyNode = new HierarchyNode(null, null, null, null);
			dummyNode.setDummyNode(true);
			new DefaultTreeNode(dummyNode, cmqBaseTreeNode);
			dummyNodeAdded = true;
		}
		
		//check for relations now
		if(!dummyNodeAdded) {
			count = this.smqBaseCurrentService.findSmqRelationsCountForSmqCode(selectedSmqList.getSmqCode());
			if((count != null) && (count > 0)) {
				HierarchyNode dummyNode = new HierarchyNode(null, null, null, null);
				dummyNode.setDummyNode(true);
				new DefaultTreeNode(dummyNode, cmqBaseTreeNode);
			}
		}
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
				cmqBaseTargetService.update(selectedImpactedCmqList);
			if (this.isNonImpactedCmqSelected)
				cmqBaseTargetService.update(selectedNotImpactedCmqList);
			FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "Workflow state has been updated", "");
			FacesContext ctx = FacesContext.getCurrentInstance();
			ctx.addMessage(null, msg);
		} catch (CqtServiceException e) {
			e.printStackTrace();
			FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR,
					"An error occurred while updating the state of the List", "");
			FacesContext ctx = FacesContext.getCurrentInstance();
			ctx.addMessage(null, msg);
		}
		
		setReviewEnabled(false);
		setApproveEnabled(false);
		setDemoteEnabled(false); 
		
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

	private void updateWorkflowButtonStates(CmqBaseTarget target) {
		/**
		 * Update on workflow buttons
		 */
		if (target != null) {
			if (target.getCmqState().equals("PENDING IA"))
				setReviewEnabled(true);
			if (target.getCmqState().equals("REVIEWED IA"))
				setApproveEnabled(true);
			if (target.getCmqState().equals("REVIEWED IA") || target.getCmqState().equals("APPROVED IA"))
				setDemoteEnabled(true);
		}
	}

	private void populateMeddraDictReverseHierarchySearchDtoChildren(String searchColumnTypePrefix, String partitionColumn
																		, Long code, HierarchyNode hierarchyNode, TreeNode expandedTreeNode
																		, MeddraDictReverseHierarchySearchDto reverseSearchDto
																		, boolean chekcForPrimaryPath) {
		String partitionColumnPrefix = partitionColumn +"_";
		List<MeddraDictReverseHierarchySearchDto> childReverseSearchDtos = this.meddraDictTargetService.findReverseByCode(searchColumnTypePrefix
																															, partitionColumnPrefix, code);
		if(CollectionUtils.isNotEmpty(childReverseSearchDtos)) {
			for (MeddraDictReverseHierarchySearchDto childReverseSearchDto : childReverseSearchDtos) {
				HierarchyNode childNode = null;
				if(chekcForPrimaryPath) {
					boolean isPrimary = false;
					if("Y".equalsIgnoreCase(childReverseSearchDto.getPrimaryPathFlag())) {
						isPrimary = true;
					}
					childNode = this.createMeddraReverseNode(childReverseSearchDto, partitionColumn, isPrimary);
				} else {
					childNode = this.createMeddraReverseNode(childReverseSearchDto, partitionColumn, hierarchyNode.isPrimaryPathFlag());
				}
				TreeNode childTreeNode = new DefaultTreeNode(childNode, expandedTreeNode);
				
				//dont add any child for last leaf node
				if(!"SOC".equalsIgnoreCase(partitionColumn)) {
					if(StringUtils.isNotBlank(reverseSearchDto.getHltTerm())) {
						// add a dummmy node to show expand arrow
						HierarchyNode dummyNode = new HierarchyNode(null, null, null, null);
						dummyNode.setDummyNode(true);
						new DefaultTreeNode(dummyNode, childTreeNode);
					}
				}
			}
		}
	}
	
	
	private void populateMeddraDictHierarchySearchDtoChildren(String parentLevel, Long dtoCode, TreeNode expandedTreeNode, String meddraType) {
		//child code and term type prefix for the parent i.e: node that was expanded in ui
		String childLevel = null;
		String childSearchColumnTypePrefix = null;
		
		//child of the above child
		String childOfChildLevel = null;
		String childchildOfChildSearchColumnTypePrefix = null;
		
		String parentCodeColumnPrefix = parentLevel + "_";
		if ("SOC".equalsIgnoreCase(parentLevel)) {
			childLevel = "HLGT";
			childSearchColumnTypePrefix = childLevel + "_";
			childOfChildLevel = "HLT";
			childchildOfChildSearchColumnTypePrefix = childOfChildLevel + "_";
		} else if ("HLGT".equalsIgnoreCase(parentLevel)) {
			childLevel = "HLT";
			childSearchColumnTypePrefix = childLevel + "_";
			childOfChildLevel = "PT";
			childchildOfChildSearchColumnTypePrefix = childOfChildLevel + "_";
		} else if ("HLT".equalsIgnoreCase(parentLevel)) {
			childLevel = "PT";
			childSearchColumnTypePrefix = childLevel + "_";
			childOfChildLevel = "LLT";
			childchildOfChildSearchColumnTypePrefix = childOfChildLevel + "_";
		} else if ("PT".equalsIgnoreCase(parentLevel)) {
			childLevel = "LLT";
			childSearchColumnTypePrefix = childLevel + "_";
		}
		
		//fetch children of parent node by code of parent
		List<MeddraDictHierarchySearchDto> childDtos = null;
		if("current".equalsIgnoreCase(meddraType)) {
			childDtos = this.meddraDictCurrentService.findChildrenByParentCode(childSearchColumnTypePrefix
																					, parentCodeColumnPrefix, dtoCode);
		} else {
			childDtos = this.meddraDictTargetService.findChildrenByParentCode(childSearchColumnTypePrefix, parentCodeColumnPrefix, dtoCode);
		}
		for (MeddraDictHierarchySearchDto childDto : childDtos) {
			HierarchyNode childNode = this.createMeddraNode(childDto, childLevel);
			if(childLevel.equalsIgnoreCase("PT")){//add in only PT children
				if(!StringUtils.isBlank(childDto.getPrimaryPathFlag()) 
						&& (childDto.getPrimaryPathFlag().equalsIgnoreCase("Y"))){
					childNode.setPrimaryPathFlag(true);
				} else {
					childNode.setPrimaryPathFlag(false);
				}
			} else {
				childNode.setPrimaryPathFlag(false);
			}
			
			TreeNode childTreeNode = new DefaultTreeNode(childNode, expandedTreeNode);
			
			//fetch children count of this iterating child node by code of child
			//no need to do this is the childOfChild is LLT since LT is the leaf ode type
			if(!"LLT".equalsIgnoreCase(childLevel)) {
				Long countOfChildrenOfChild = null;
				if("current".equalsIgnoreCase(meddraType)) {
					countOfChildrenOfChild = this.meddraDictCurrentService.findChldrenCountByParentCode(childchildOfChildSearchColumnTypePrefix
																					, childSearchColumnTypePrefix, Long.valueOf(childDto.getCode()));
				} else {
					countOfChildrenOfChild = this.meddraDictTargetService.findChldrenCountByParentCode(childchildOfChildSearchColumnTypePrefix
																					, childSearchColumnTypePrefix, Long.valueOf(childDto.getCode()));
				}
				if((countOfChildrenOfChild != null) && (countOfChildrenOfChild > 0)) {
					// add a dummmy node to show expand arrow
					HierarchyNode dummyNode = new HierarchyNode(null, null, null, null);
					dummyNode.setDummyNode(true);
					new DefaultTreeNode(dummyNode, childTreeNode);
				}
			}
		}
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private void populateSmqBaseChildren(Long smqCode, TreeNode expandedTreeNode, String smqType) {
		List<? extends IEntity> childSmqBaseList = null;
		if("current".equalsIgnoreCase(smqType)) {
			childSmqBaseList = this.smqBaseCurrentService.findChildSmqByParentSmqCode(smqCode);
		} else {
			childSmqBaseList = this.smqBaseTargetService.findChildSmqByParentSmqCode(smqCode);
		}
		if(CollectionUtils.isNotEmpty(childSmqBaseList)) {
			Map<Long, TreeNode> smqTreeNodeMap = new HashMap<>();
			List<Long> smqChildCodeList = new ArrayList<>();
			for (IEntity entity : childSmqBaseList) {
				HierarchyNode childNode = new HierarchyNode();
				Long childSmqCode = null;
				if("current".equalsIgnoreCase(smqType)) {
					SmqBase190 childSmqBase = (SmqBase190) entity;
					childSmqCode = childSmqBase.getSmqCode();
					if (childSmqBase.getSmqLevel() == 1) {
						childNode.setLevel("SMQ1");
					} else if (childSmqBase.getSmqLevel() == 2) {
						childNode.setLevel("SMQ2");
					} else if (childSmqBase.getSmqLevel() == 3) {
						childNode.setLevel("SMQ3");
					} else if (childSmqBase.getSmqLevel() == 4) {
						childNode.setLevel("SMQ4");
					} else if (childSmqBase.getSmqLevel() == 5) {
						childNode.setLevel("SMQ5");
					}
					childNode.setTerm(childSmqBase.getSmqName());
					childNode.setCode(childSmqBase.getSmqCode().toString());
					childNode.setEntity(childSmqBase);
					smqChildCodeList.add(childSmqBase.getSmqCode());
				} else {
					//for target here
					SmqBaseTarget childSmqBase = (SmqBaseTarget) entity;
					childSmqCode = childSmqBase.getSmqCode();
					if (childSmqBase.getSmqLevel() == 1) {
						childNode.setLevel("SMQ1");
					} else if (childSmqBase.getSmqLevel() == 2) {
						childNode.setLevel("SMQ2");
					} else if (childSmqBase.getSmqLevel() == 3) {
						childNode.setLevel("SMQ3");
					} else if (childSmqBase.getSmqLevel() == 4) {
						childNode.setLevel("SMQ4");
					} else if (childSmqBase.getSmqLevel() == 5) {
						childNode.setLevel("SMQ5");
					}
					childNode.setTerm(childSmqBase.getSmqName());
					childNode.setCode(childSmqBase.getSmqCode().toString());
					childNode.setEntity(childSmqBase);
					smqChildCodeList.add(childSmqBase.getSmqCode());
				}
				// add child to parent
				TreeNode childTreeNode = new DefaultTreeNode(childNode, expandedTreeNode);
				smqTreeNodeMap.put(childSmqCode, childTreeNode);
			} // end of for
			
			//find smqrelations of all child smqs
			int smqChildCodesSize = smqChildCodeList.size();
			List<List<Long>> choppedLists = null;
			if(smqChildCodesSize > 400) {
				//split it into smaller lists
				choppedLists = ListUtils.partition(smqChildCodeList, 200);
			}else {
				choppedLists = new ArrayList<List<Long>>();
				choppedLists.add(smqChildCodeList);
			}
			//process the chopped lists now
			for (List<Long> subList : choppedLists) {
				List<Map<String, Object>> childSmqRelationsCountList = null;
				if("current".equalsIgnoreCase(smqType)) {
					childSmqRelationsCountList = this.smqBaseCurrentService.findSmqRelationsCountForSmqCodes(subList);
				} else {
					childSmqRelationsCountList = this.smqBaseTargetService.findSmqRelationsCountForSmqCodes(subList);
				}
				if((null != childSmqRelationsCountList) && (childSmqRelationsCountList.size() > 0)) {
					ListIterator li = childSmqRelationsCountList.listIterator();
					while(li.hasNext()) {
						Map<String, Object> map = (Map<String, Object>) li.next();
						if(map.get("SMQ_CODE") != null) {
							Long childSmqCode = (Long)map.get("SMQ_CODE");
							Long count = (Long)map.get("COUNT");
							if(count > 0) {
								TreeNode childTreeNode = smqTreeNodeMap.get(childSmqCode);
								
								// add a dummmy node to show expand arrow
								HierarchyNode dummyNode = new HierarchyNode(null, null,null, null);
								dummyNode.setDummyNode(true);
								new DefaultTreeNode(dummyNode, childTreeNode);
							}
						}
					}
				}//end of if((null != childSmqRelationsCountList) &&.....
			}//end of for (List<Long> subList : choppedLists)
		}
	}
	
	private void populateSmqRelations(Long smqCode, TreeNode expandedTreeNode, String smqType) {
		List<? extends IEntity> childRelations = null;
		if("current".equalsIgnoreCase(smqType)) {
			childRelations = this.smqBaseCurrentService.findSmqRelationsForSmqCode(smqCode);
		} else {
			childRelations = this.smqBaseTargetService.findSmqRelationsForSmqCode(smqCode);
		}
		if (null != childRelations) {
			for (IEntity entity : childRelations) {
				HierarchyNode childRelationNode = new HierarchyNode();
				if("current".equalsIgnoreCase(smqType)) {
					SmqRelation190 childRelation = (SmqRelation190) entity;
					if (childRelation.getSmqLevel() == 1) {
						childRelationNode.setLevel("SMQ1");
					} else if (childRelation.getSmqLevel() == 2) {
						childRelationNode.setLevel("SMQ2");
					} else if (childRelation.getSmqLevel() == 3) {
						childRelationNode.setLevel("SMQ3");
					} else if ((childRelation.getSmqLevel() == 4)
							|| (childRelation.getSmqLevel() == 0)
							|| (childRelation.getSmqLevel() == 5)) {
						childRelationNode.setLevel("PT");
					}
					childRelationNode.setTerm(childRelation.getPtName());
					childRelationNode.setCode(childRelation.getPtCode().toString());
					childRelationNode.setEntity(childRelation);
				} else {
					//for target here
					SmqRelationTarget childRelation = (SmqRelationTarget) entity;
					if (childRelation.getSmqLevel() == 1) {
						childRelationNode.setLevel("SMQ1");
					} else if (childRelation.getSmqLevel() == 2) {
						childRelationNode.setLevel("SMQ2");
					} else if (childRelation.getSmqLevel() == 3) {
						childRelationNode.setLevel("SMQ3");
					} else if ((childRelation.getSmqLevel() == 4)
							|| (childRelation.getSmqLevel() == 0)
							|| (childRelation.getSmqLevel() == 5)) {
						childRelationNode.setLevel("PT");
					}
					childRelationNode.setTerm(childRelation.getPtName());
					childRelationNode.setCode(childRelation.getPtCode().toString());
					childRelationNode.setEntity(childRelation);
				}
				new DefaultTreeNode(childRelationNode, expandedTreeNode);
			}
		}
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private void populateCmqBaseChildren(Long cmqCode, TreeNode expandedTreeNode, String cmqType) {
		List<? extends IEntity> childCmqBaseList = null;
		if("current".equalsIgnoreCase(cmqType)) {
			childCmqBaseList = cmqBaseCurrentService.findChildCmqsByParentCode(cmqCode);
		} else {
			childCmqBaseList = cmqBaseTargetService.findChildCmqsByParentCode(cmqCode);
		}
		
		List<Long> childCmqCodeList = new ArrayList<>();
		Map<Long, TreeNode> childTreeNodes = new HashMap<Long, TreeNode>();
		
		if ((null != childCmqBaseList) && (childCmqBaseList.size() > 0)) {
			for (IEntity entity : childCmqBaseList) {
				HierarchyNode node = new HierarchyNode();
				if("current".equalsIgnoreCase(cmqType)) {
					CmqBase190 childCmqBase = (CmqBase190) entity;
					node.setLevel(childCmqBase.getCmqTypeCd());
					node.setTerm(childCmqBase.getCmqName());
					node.setCode(childCmqBase.getCmqCode().toString());
					node.setEntity(childCmqBase);
					TreeNode cmqBaseChildNode = new DefaultTreeNode(node, expandedTreeNode);
					
					childTreeNodes.put(childCmqBase.getCmqCode(), cmqBaseChildNode);
					childCmqCodeList.add(childCmqBase.getCmqCode());
				} else {
					CmqBaseTarget childCmqBase = (CmqBaseTarget) entity;
					node.setLevel(childCmqBase.getCmqTypeCd());
					node.setTerm(childCmqBase.getCmqName());
					node.setCode(childCmqBase.getCmqCode().toString());
					node.setEntity(childCmqBase);
					TreeNode cmqBaseChildNode = new DefaultTreeNode(node, expandedTreeNode);
					
					childTreeNodes.put(childCmqBase.getCmqCode(), cmqBaseChildNode);
					childCmqCodeList.add(childCmqBase.getCmqCode());
				}
			}
			
			List<Map<String, Object>> childrenOfChildCountsList = null;
			if("current".equalsIgnoreCase(cmqType)) {
				childrenOfChildCountsList = this.cmqBaseCurrentService.findCmqChildCountForParentCmqCode(childCmqCodeList);
			} else {
				childrenOfChildCountsList = this.cmqBaseTargetService.findCmqChildCountForParentCmqCode(childCmqCodeList);
			}
			
			if((null != childrenOfChildCountsList) && (childrenOfChildCountsList.size() > 0)) {
				//first find and fix child nodes stuff
				for (Iterator<Long> it = childCmqCodeList.iterator(); it.hasNext();) {
					ListIterator li = childrenOfChildCountsList.listIterator();
					Long  childCmqCode = it.next();
					while(li.hasNext()) {
						Map<String, Object> map = (Map<String, Object>) li.next();
						if(map.get("CMQ_CODE") != null) {
							Long resultCmqCode = (Long)map.get("CMQ_CODE");
							if(resultCmqCode.longValue() ==  childCmqCode.longValue()) {
								it.remove();//remove it from parentCmqCodeList
								Long count = (Long)map.get("COUNT");
								if(count > 0) {
									
									//add a dummy node for this child in parent
									TreeNode parentTreeNode = childTreeNodes.get(childCmqCode);
									HierarchyNode dummyNode = new HierarchyNode(null, null,
											null, null);
									dummyNode.setDummyNode(true);
									new DefaultTreeNode(dummyNode, parentTreeNode);
								}
								break;
							}//end of if(cmqCode.longValue() == parentCmqCode.longValue())
						}
					}
				}
			}
			
			//now find relations for those who don't have children
			List<Map<String, Object>> relationsCountsList = null;
			if("current".equalsIgnoreCase(cmqType)) {
				relationsCountsList = this.cmqRelationCurrentService.findCountByCmqCodes(childCmqCodeList);
			} else {
				relationsCountsList = this.cmqRelationTargetService.findCountByCmqCodes(childCmqCodeList);
			}
				
			if((null != relationsCountsList) && (relationsCountsList.size() > 0)) {
				ListIterator li = relationsCountsList.listIterator();
				while(li.hasNext()) {
					Map<String, Object> map = (Map<String, Object>) li.next();
					if(map.get("CMQ_CODE") != null) {
						Long resultCmqCode = (Long)map.get("CMQ_CODE");
						Long count = (Long)map.get("COUNT");
						if(count > 0) {
							//add a dummy node for this child in parent
							TreeNode parentTreeNode = childTreeNodes.get(resultCmqCode);
							HierarchyNode dummyNode = new HierarchyNode(null, null,
									null, null);
							dummyNode.setDummyNode(true);
							new DefaultTreeNode(dummyNode, parentTreeNode);
						}
					}
				}
			}
		}
	}
	
	private void populateCmqRelations(Long cmqCode, TreeNode expandedTreeNode, String cmqType) {
		//add cmq relations now
		List<Long> socCodesList = new ArrayList<>();
		List<Long> hlgtCodesList = new ArrayList<>();
		List<Long> hltCodesList = new ArrayList<>();
		List<Long> ptCodesList = new ArrayList<>();
		List<Long> lltCodesList = new ArrayList<>();
		List<Long> smqCodesList = new ArrayList<>();
		
		List<? extends IEntity> existingRelations = null;
		if("current".equalsIgnoreCase(cmqType)) {
			existingRelations = this.cmqRelationCurrentService.findByCmqCode(cmqCode);
		} else {
			existingRelations = this.cmqRelationTargetService.findByCmqCode(cmqCode);
		}
		
		if((null != existingRelations) && (existingRelations.size() > 0)) {
			for (IEntity entity : existingRelations) {
				if("current".equalsIgnoreCase(cmqType)) {
					CmqRelation190 cmqRelation = (CmqRelation190) entity;
					if((cmqRelation.getSocCode() != null) && (cmqRelation.getSocCode().longValue() > 0)) {
						socCodesList.add(cmqRelation.getSocCode());
					} else if((cmqRelation.getHlgtCode() != null) && (cmqRelation.getHlgtCode().longValue() > 0)) {
						hlgtCodesList.add(cmqRelation.getHlgtCode());
					} else if((cmqRelation.getHltCode() != null) && (cmqRelation.getHltCode().longValue() > 0)) {
						hltCodesList.add(cmqRelation.getHltCode());
					} else if((cmqRelation.getPtCode() != null) && (cmqRelation.getPtCode().longValue() > 0)) {
						ptCodesList.add(cmqRelation.getPtCode());
					} else if((cmqRelation.getLltCode() != null) && (cmqRelation.getLltCode().longValue() > 0)) {
						lltCodesList.add(cmqRelation.getLltCode());
					} else if((cmqRelation.getSmqCode() != null) && (cmqRelation.getSmqCode().longValue() > 0)) {
						this.populateSmqTreeNode(cmqRelation, expandedTreeNode, cmqType, cmqCode);
					}
				} else {
					CmqRelationTarget cmqRelation = (CmqRelationTarget) entity;
					if((cmqRelation.getSocCode() != null) && (cmqRelation.getSocCode().longValue() > 0)) {
						socCodesList.add(cmqRelation.getSocCode());
					} else if((cmqRelation.getHlgtCode() != null) && (cmqRelation.getHlgtCode().longValue() > 0)) {
						hlgtCodesList.add(cmqRelation.getHlgtCode());
					} else if((cmqRelation.getHltCode() != null) && (cmqRelation.getHltCode().longValue() > 0)) {
						hltCodesList.add(cmqRelation.getHltCode());
					} else if((cmqRelation.getPtCode() != null) && (cmqRelation.getPtCode().longValue() > 0)) {
						ptCodesList.add(cmqRelation.getPtCode());
					} else if((cmqRelation.getLltCode() != null) && (cmqRelation.getLltCode().longValue() > 0)) {
						lltCodesList.add(cmqRelation.getLltCode());
					} else if((cmqRelation.getSmqCode() != null) && (cmqRelation.getSmqCode().longValue() > 0)) {
						this.populateSmqTreeNode(cmqRelation, expandedTreeNode, cmqType, cmqCode);
					}
				}
			}
			
			//find socs now
			if(socCodesList.size() > 0) {
				List<MeddraDictHierarchySearchDto> socDtos = null;
				if("current".equalsIgnoreCase(cmqType)) {
					socDtos = this.meddraDictCurrentService.findByCodes("SOC_", socCodesList);
				} else {
					socDtos = this.meddraDictTargetService.findByCodes("SOC_", socCodesList);
				}
				this.populateCmqRelationTreeNodes(socDtos, expandedTreeNode, "SOC", "HLGT", cmqType, cmqCode);
			}
			
			if(hlgtCodesList.size() > 0) {
				List<MeddraDictHierarchySearchDto> hlgtDtos = null;
				if("current".equalsIgnoreCase(cmqType)) {
					hlgtDtos = this.meddraDictCurrentService.findByCodes("HLGT_", hlgtCodesList);
				} else {
					hlgtDtos = this.meddraDictTargetService.findByCodes("HLGT_", hlgtCodesList);
				}
				this.populateCmqRelationTreeNodes(hlgtDtos, expandedTreeNode, "HLGT", "HLT", cmqType, cmqCode);
			}
			
			if(hltCodesList.size() > 0) {
				List<MeddraDictHierarchySearchDto> hltDtos = null;
				if("current".equalsIgnoreCase(cmqType)) {
					hltDtos = this.meddraDictCurrentService.findByCodes("HLT_", hltCodesList);
				} else {
					hltDtos = this.meddraDictTargetService.findByCodes("HLT_", hltCodesList);
				}
				this.populateCmqRelationTreeNodes(hltDtos, expandedTreeNode, "HLT", "PT", cmqType, cmqCode);
			}
			
			if(ptCodesList.size() > 0) {
				List<MeddraDictHierarchySearchDto> ptDtos = null;
				if("current".equalsIgnoreCase(cmqType)) {
					ptDtos = this.meddraDictCurrentService.findByCodes("PT_", ptCodesList);
				} else {
					ptDtos = this.meddraDictTargetService.findByCodes("PT_", ptCodesList);
				}
				this.populateCmqRelationTreeNodes(ptDtos, expandedTreeNode, "PT", "LLT", cmqType, cmqCode);
			}
			
			if(lltCodesList.size() > 0) {
				List<MeddraDictHierarchySearchDto> lltDtos = null;
				if("current".equalsIgnoreCase(cmqType)) {
					lltDtos = this.meddraDictCurrentService.findByCodes("LLT_", lltCodesList);
				} else {
					lltDtos = this.meddraDictTargetService.findByCodes("LLT_", lltCodesList);
				}
				for (MeddraDictHierarchySearchDto meddraDictHierarchySearchDto : lltDtos) {
					HierarchyNode node = this.createMeddraNode(meddraDictHierarchySearchDto, "LLT");
					TreeNode treeNode = new DefaultTreeNode(node, expandedTreeNode);
				}
			}
		}
	}
	
	private void populateSmqTreeNode(IEntity entity, TreeNode expandedTreeNode, String cmqType, Long parentCode) {
		IEntity entity2 = null;
		HierarchyNode node = null;
		if("current".equalsIgnoreCase(cmqType)) {
			CmqRelation190 cmqRelation = (CmqRelation190) entity;
			entity2 = this.smqBaseCurrentService.findByCode(cmqRelation.getSmqCode());
			node = this.createSmqBaseCurrrentNode((SmqBase190) entity2);
		} else {
			CmqRelationTarget cmqRelation = (CmqRelationTarget) entity;
			entity2 = this.smqBaseTargetService.findByCode(cmqRelation.getSmqCode());
			node = this.createSmqBaseTargetNode((SmqBaseTarget) entity2);
		}
		if(null != node) {
			if (cmqType.equals("current")) 
				setSQMCurrentNodeStyle(node, parentCode);
			else
				setSQMTargetNodeStyle(node, parentCode); 
			
			TreeNode treeNode = new DefaultTreeNode(node, expandedTreeNode);
			
			//add a dummy node for either of the cases, expansion will handle the actuals later
			Long smqBaseChildrenCount = null;
			if("current".equalsIgnoreCase(cmqType)) {
				smqBaseChildrenCount = this.smqBaseCurrentService.findChildSmqCountByParentSmqCode(((SmqBase190)entity2).getSmqCode());
			} else {
				smqBaseChildrenCount = this.smqBaseTargetService.findChildSmqCountByParentSmqCode(((SmqBaseTarget)entity2).getSmqCode());
			}
			if((null != smqBaseChildrenCount) && (smqBaseChildrenCount > 0)) {
				// add a dummmy node to show expand arrow
				HierarchyNode dummyNode = new HierarchyNode(null, null, null, null);
				dummyNode.setDummyNode(true);
				new DefaultTreeNode(dummyNode, treeNode);
			} else {
				Long childSmqrelationsCount = null;
				if("current".equalsIgnoreCase(cmqType)) {
					childSmqrelationsCount = this.smqBaseCurrentService.findSmqRelationsCountForSmqCode(((SmqBase190)entity2).getSmqCode());
				} else {
					childSmqrelationsCount = this.smqBaseTargetService.findSmqRelationsCountForSmqCode(((SmqBaseTarget)entity2).getSmqCode());
				}
				if((null != childSmqrelationsCount) && (childSmqrelationsCount > 0)) {
					// add a dummmy node to show expand arrow
					HierarchyNode dummyNode = new HierarchyNode(null, null, null, null);
					dummyNode.setDummyNode(true);
					new DefaultTreeNode(dummyNode, treeNode);
				}
			}
		}
	}
	
	private void populateCmqRelationTreeNodes(List<MeddraDictHierarchySearchDto> dtos, TreeNode expandedTreeNode
			, String nodeType, String childNodeType, String cmqType, Long parentCode) {
		for (MeddraDictHierarchySearchDto meddraDictHierarchySearchDto : dtos) {
			HierarchyNode node = this.createMeddraNode(meddraDictHierarchySearchDto, nodeType);
			TreeNode treeNode = new DefaultTreeNode(node, expandedTreeNode);

			if (cmqType.equals("current")) 
				setCMQCurrentNodeStyle(node, parentCode);
			else
				setCMQTargetNodeStyle(node, parentCode); 

			Long countOfChildren = this.meddraDictCurrentService.findChldrenCountByParentCode(childNodeType + "_"
					, nodeType + "_", Long.valueOf(meddraDictHierarchySearchDto.getCode()));
			if((null != countOfChildren) && (countOfChildren > 0)) {
				// add a dummmy node to show expand arrow
				HierarchyNode dummyNode = new HierarchyNode(null, null, null, null);
				dummyNode.setDummyNode(true);
				new DefaultTreeNode(dummyNode, treeNode);
			}
		}
	}
	
	/**
	 * Added term style for CMQ.
	 * 
	 * @param node
	 *            HierarchyNode
	 * @param cmqCodeListSelected
	 *            Long
	 */
	private void setCMQCurrentNodeStyle(HierarchyNode node,	Long cmqCodeListSelected) {
		//System.out.println("\n ================== start setCMQCurrentNodeStyle===================== \n");
		//boolean added = false;
		List<CmqRelationTarget> targetRelations = cmqRelationTargetService.findByCmqCode(cmqCodeListSelected);
		for (CmqRelationTarget rel : targetRelations) {
			Long code = getRelationCode(rel);
			//System.out.println("**************************** code relation : " + code);

			MeddraDictHierarchySearchDto med = meddraDictTargetService.findByCode(getColumnPrefix(rel), code);
			String termName = med != null ? med.getTerm() : null;
//			System.out.println("**************************** Current term name : " + node.getTerm());
//			System.out.println("**************************** Target term name : " + termName);
			//Merged / Deleted terms
 			if (termName == null || (code != null && !(code + "").equals(node.getCode()))) {
 				node.setRowStyleClass("red-colored");
 				changeOccur = true;
 			}
 			
 			//Renamed terms
 			if (code != null && (code + "").equals(node.getCode()) && (termName != null && !termName.equals(node.getTerm()))) {
 				node.setRowStyleClass("italic");
 				changeOccur = true;
 			}
 			else
 				break;
		}
  		//System.out.println("\n ================== end setCMQCurrentNodeStyle===================== ");
	}
	
	private void setCMQTargetNodeStyle(HierarchyNode node,	Long cmqCodeListSelected) {
		//System.out.println("\n ================== start setCMQTargetNodeStyle===================== \n");
 		List<CmqRelation190> relations = cmqRelationCurrentService.findByCmqCode(cmqCodeListSelected);
		for (CmqRelation190 rel : relations) {
			Long code = getRelationCode(rel);
			//System.out.println("**************************** code relation : " + code);

			MeddraDictHierarchySearchDto med = meddraDictCurrentService.findByCode(getColumnPrefix(rel), code);
			String termName = med != null ? med.getTerm() : null;
//			System.out.println("**************************** Target term name : " + node.getTerm());
//			System.out.println("**************************** Current term name : " + termName);
			//Added terms
 			if (code != null && !(code + "").equals(node.getCode())) {
 				node.setRowStyleClass("orange-colored");
 				changeOccur = true;
 			}
 			
 			//Renamed terms
 			if (code != null && (code + "").equals(node.getCode()) && (termName != null && !termName.equals(node.getTerm()))) {
 				node.setRowStyleClass("italic");
 				changeOccur = true;
 			}
 			else
 				break;
		}
		//System.out.println("\n ================== end setCMQTargetNodeStyle===================== ");
	}
	
	private String getColumnPrefix(CmqRelationTarget rel) {
		String prefix = "";
		if (rel.getHlgtCode() != null)
			prefix ="HLGT_";
		if (rel.getHltCode() != null)
			prefix ="HLT_";
		if (rel.getPtCode() != null)
			prefix ="PT_";
		if (rel.getSocCode() != null)
			prefix ="SOC_";
		if (rel.getLltCode() != null)
			prefix ="LLT_";
		return prefix;
	}
	
	private String getColumnPrefix(CmqRelation190 rel) {
		String prefix = "";
		if (rel.getHlgtCode() != null)
			prefix ="HLGT_";
		if (rel.getHltCode() != null)
			prefix ="HLT_";
		if (rel.getPtCode() != null)
			prefix ="PT_";
		if (rel.getSocCode() != null)
			prefix ="SOC_";
		if (rel.getLltCode() != null)
			prefix ="LLT_";	
		return prefix;
	}

	private Long getRelationCode(CmqRelation190 rel) {
		Long code = null;
		if (rel.getHlgtCode() != null)
			code = rel.getHlgtCode();
		if (rel.getHltCode() != null)
			code = rel.getHltCode();
		if (rel.getPtCode() != null)
			code = rel.getPtCode();
		if (rel.getSmqCode() != null)
			code = rel.getSmqCode();
		if (rel.getSocCode() != null)
			code = rel.getSocCode();
		if (rel.getLltCode() != null)
			code = rel.getLltCode();

		return code;
	}

	/**
	 * Added term style for SMQ.
	 * 
	 * @param node
	 *            HierarchyNode
	 * @param cmqCodeListSelected
	 *            Long
	 */
	private void setSQMCurrentNodeStyle(HierarchyNode node,	Long cmqCodeListSelected) {
		//System.out.println("\n ================== start setSMQCurrentNodeStyle===================== \n");
		List<SmqRelationTarget> targetRelations = smqBaseTargetService.findSmqRelationsForSmqCode(cmqCodeListSelected);
		for (SmqRelationTarget rel : targetRelations) {
			Long code = getRelationCode(rel);
			//System.out.println("**************************** code relation : " + code);
			SmqBaseTarget smq = smqBaseTargetService.findByCode(code);
			String termName = smq != null ? smq.getSmqName() : null;
			//System.out.println("**************************** Current term name : " + node.getTerm());
			//System.out.println("**************************** Target term name : " + termName);
			//Added terms
 			if (termName == null  || (code != null && !(code + "").equals(node.getCode()))) {
 				node.setRowStyleClass("red-colored");
 				changeOccur = true;
 			}
 			
 			//Renamed terms
 			if (code != null && (code + "").equals(node.getCode()) && (termName != null && !termName.equals(node.getTerm()))) {
 				node.setRowStyleClass("italic");
 				changeOccur = true;
			}
 			else
 				break;
 			//Merged terms
 			
		}
		//System.out.println("\n ================== end setSMQCurrentNodeStyle===================== ");
	}
	
	private void setSQMTargetNodeStyle(HierarchyNode node,	Long cmqCodeListSelected) {
		//System.out.println("\n ================== start setSQMTargetNodeStyle===================== \n");
 		List<SmqRelation190> targetRelations = smqBaseCurrentService.findSmqRelationsForSmqCode(cmqCodeListSelected);
		for (SmqRelation190 rel : targetRelations) {
			Long code = getRelationCode(rel);
			//System.out.println("**************************** code relation : " + code);
			SmqBase190 smq = smqBaseCurrentService.findByCode(code);
			String termName = smq != null ? smq.getSmqName() : null;
//			System.out.println("**************************** Target term name : " + node.getTerm());
//			System.out.println("**************************** Current term name : " + termName);
 			//Added terms
 			if (code != null && !(code + "").equals(node.getCode())) {
 				node.setRowStyleClass("orange-colored");
 				changeOccur = true;
  			}
 			
 			//Renamed terms
 			if (code != null && (code + "").equals(node.getCode()) && (termName != null && !termName.equals(node.getTerm()))) {
 				node.setRowStyleClass("italic");
 				changeOccur = true;
			}
 			else
 				break;
 			
		}
		//System.out.println("\n ================== end setSQMTargetNodeStyle===================== ");
	}
	
	
	private Long getRelationCode(SmqRelationTarget rel) {
		Long code = null;
		if (rel.getPtCode() != null)
			code = Long.parseLong(rel.getPtCode() + "");
		if (rel.getSmqCode() != null)
			code = rel.getSmqCode();

		return code;
	}

	

	private Long getRelationCode(CmqRelationTarget rel) {
		Long code = null;
		if (rel.getHlgtCode() != null)
			code = rel.getHlgtCode();
		if (rel.getHltCode() != null)
			code = rel.getHltCode();
		if (rel.getPtCode() != null)
			code = rel.getPtCode();
		if (rel.getSmqCode() != null)
			code = rel.getSmqCode();
		if (rel.getSocCode() != null)
			code = rel.getSocCode();
		if (rel.getLltCode() != null)
			code = rel.getLltCode();
		
		
		return code;
	}
	private Long getRelationCode(SmqRelation190 rel) {
		if (rel.getPtCode() != null)
			return Long.parseLong(rel.getPtCode() + "");
		return rel.getSmqCode();
	}
	
	private boolean selectedCmqNodeExistsInAssessment(TreeNode rootTreeNode, CmqBaseTarget selectedCmqList) {
		boolean retVal = false;
		List<TreeNode> children = rootTreeNode.getChildren();
		for (TreeNode child : children) {
			HierarchyNode hierarchyNode = (HierarchyNode) child.getData();
			
		}
		return retVal;
	}
	
	private HierarchyNode createCmqBaseCurrentHierarchyNode(CmqBase190 cmqBaseCurrent) {
		HierarchyNode node = new HierarchyNode();
		node.setLevel(cmqBaseCurrent.getCmqTypeCd());
		node.setTerm(cmqBaseCurrent.getCmqName());
		node.setCode(cmqBaseCurrent.getCmqCode().toString());
		node.setCategory("");
		node.setWeight("");
		node.setScope("");
		node.setEntity(cmqBaseCurrent);
		return node;
	}
	
	private HierarchyNode createCmqBaseTargetHierarchyNode(CmqBaseTarget cmqBaseTarget) {
		HierarchyNode node = new HierarchyNode();
		node.setLevel(cmqBaseTarget.getCmqTypeCd());
		node.setTerm(cmqBaseTarget.getCmqName());
		node.setCode(cmqBaseTarget.getCmqCode().toString());
		node.setCategory("");
		node.setWeight("");
		node.setScope("");
		node.setEntity(cmqBaseTarget);
		return node;
	}
	
	private HierarchyNode createSmqBaseCurrrentNode(SmqBase190 smqBase) {
		HierarchyNode node = null;
		if(null != smqBase) {
			node = new HierarchyNode();
			if (smqBase.getSmqLevel() == 1) {
				node.setLevel("SMQ1");
			} else if (smqBase.getSmqLevel() == 2) {
				node.setLevel("SMQ2");
			} else if (smqBase.getSmqLevel() == 3) {
				node.setLevel("SMQ3");
			} else if (smqBase.getSmqLevel() == 4) {
				node.setLevel("SMQ4");
			} else if (smqBase.getSmqLevel() == 5) {
				node.setLevel("SMQ5");
			}
			node.setTerm(smqBase.getSmqName());
			node.setCode(smqBase.getSmqCode().toString());
			node.setEntity(smqBase);
		}
		return node;
	}
	
	private HierarchyNode createSmqBaseTargetNode(SmqBaseTarget smqBase) {
		HierarchyNode node = null;
		if(null != smqBase) {
			node = new HierarchyNode();
			if (smqBase.getSmqLevel() == 1) {
				node.setLevel("SMQ1");
			} else if (smqBase.getSmqLevel() == 2) {
				node.setLevel("SMQ2");
			} else if (smqBase.getSmqLevel() == 3) {
				node.setLevel("SMQ3");
			} else if (smqBase.getSmqLevel() == 4) {
				node.setLevel("SMQ4");
			} else if (smqBase.getSmqLevel() == 5) {
				node.setLevel("SMQ5");
			}
			node.setTerm(smqBase.getSmqName());
			node.setCode(smqBase.getSmqCode().toString());
			node.setEntity(smqBase);
		}
		return node;
	}
	
	private HierarchyNode createMeddraNode(
			MeddraDictHierarchySearchDto searchDto, String level) {
		HierarchyNode node = new HierarchyNode();
		node.setLevel(level);
		node.setTerm(searchDto.getTerm());
		node.setCode(searchDto.getCode());
		node.setEntity(searchDto);
		return node;
	}
	
	private HierarchyNode createMeddraReverseNode(
			MeddraDictReverseHierarchySearchDto searchDto, String level, boolean isPrimary) {
		HierarchyNode node = new HierarchyNode();
		node.setLevel(level);
		if("LLT".equalsIgnoreCase(level)) {
			node.setTerm(searchDto.getLltTerm());
			node.setCode(searchDto.getLltCode());	
		} else if ("PT".equalsIgnoreCase(level)) {
			node.setTerm(searchDto.getPtTerm());
			node.setCode(searchDto.getPtCode());
		} else if ("HLT".equalsIgnoreCase(level)) {
			node.setTerm(searchDto.getHltTerm());
			node.setCode(searchDto.getHltCode());
		} else if ("HLGT".equalsIgnoreCase(level)) {
			node.setTerm(searchDto.getHlgtTerm());
			node.setCode(searchDto.getHlgtCode());
		} else if ("SOC".equalsIgnoreCase(level)) {
			node.setTerm(searchDto.getSocTerm());
			node.setCode(searchDto.getSocCode());
		}
		
		if(isPrimary) {
			node.setPrimaryPathFlag(true);
			node.setRowStyleClass("green-colored");
		} else {
			node.setPrimaryPathFlag(false);
		}
		node.setEntity(searchDto);
		return node;
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
		return reviewEnabled;
	}

	public void setReviewEnabled(boolean reviewEnabled) {
		this.reviewEnabled = reviewEnabled;
	}

	public boolean isDemoteEnabled() {
		return demoteEnabled;
	}

	public void setDemoteEnabled(boolean demoteEnabled) {
		this.demoteEnabled = demoteEnabled;
	}

	public boolean isApproveEnabled() {
		return approveEnabled;
	}

	public void setApproveEnabled(boolean approveEnabled) {
		this.approveEnabled = approveEnabled;
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

	public String getLevelH() {
		return levelH;
	}

	public void setLevelH(String levelH) {
		this.levelH = levelH;
	}

	public TreeNode getHierarchyRoot() {
		return hierarchyRoot;
	}

	public void setHierarchyRoot(TreeNode hierarchyRoot) {
		this.hierarchyRoot = hierarchyRoot;
	}

	public String getTermNameOfHierarchySearch() {
		return termNameOfHierarchySearch;
	}

	public void setTermNameOfHierarchySearch(String termNameOfHierarchySearch) {
		this.termNameOfHierarchySearch = termNameOfHierarchySearch;
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

	public TreeNode getRelationsRoot() {
		return relationsRoot;
	}

	public void setRelationsRoot(TreeNode relationsRoot) {
		this.relationsRoot = relationsRoot;
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

	public ListNotesFormModel getNotesFormModel() {
		return notesFormModel;
	}

	public void setNotesFormModel(ListNotesFormModel notesFormModel) {
		this.notesFormModel = notesFormModel;
	}

	public ListDetailsFormModel getDetailsFormModel() {
		return detailsFormModel;
	}

	public void setDetailsFormModel(ListDetailsFormModel detailsFormModel) {
		this.detailsFormModel = detailsFormModel;
	}
	
	public boolean isNotesFormReadonly() {
		if (this.currentOrTarget == SELECTED_CURRENT_LIST)
			return true;
		if (this.currentOrTarget == SELECTED_TARGET_LIST)
			return false;
		return true;
	}
	
	public boolean isDetailsFormReadonly() {
		if (this.currentOrTarget == SELECTED_CURRENT_LIST)
			return true;
		if (this.currentOrTarget == SELECTED_TARGET_LIST)
			return false;
		return true;
	}

	public boolean isChangeOccur() {
		return changeOccur;
	}

	public void setChangeOccur(boolean changeOccur) {
		this.changeOccur = changeOccur;
	}
}
