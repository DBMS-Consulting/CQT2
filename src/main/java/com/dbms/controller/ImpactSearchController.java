package com.dbms.controller;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Locale;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.ViewScoped;
import javax.faces.component.UIComponent;
import javax.faces.component.UIViewRoot;
import javax.faces.component.html.HtmlInputHidden;
import javax.faces.context.FacesContext;
import javax.faces.event.AjaxBehaviorEvent;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.primefaces.component.datatable.DataTable;
import org.primefaces.component.wizard.Wizard;
import org.primefaces.context.RequestContext;
import org.primefaces.event.FlowEvent;
import org.primefaces.event.NodeCollapseEvent;
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

import com.dbms.csmq.CSMQBean;
import com.dbms.csmq.HierarchyNode;
import com.dbms.entity.IEntity;
import com.dbms.entity.cqt.CmqBase190;
import com.dbms.entity.cqt.CmqBaseTarget;
import com.dbms.entity.cqt.CmqRelationTarget;
import com.dbms.entity.cqt.RefConfigCodeList;
import com.dbms.entity.cqt.SmqBase190;
import com.dbms.entity.cqt.SmqBaseTarget;
import com.dbms.entity.cqt.SmqRelationTarget;
import com.dbms.entity.cqt.dtos.MeddraDictHierarchySearchDto;
import com.dbms.entity.cqt.dtos.MeddraDictReverseHierarchySearchDto;
import com.dbms.entity.cqt.dtos.SMQReverseHierarchySearchDto;
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
import com.dbms.util.CqtConstants;
import com.dbms.util.OrderBy;
import com.dbms.util.SWJSFRequest;
import com.dbms.util.exceptions.CqtServiceException;
import com.dbms.view.CmqBaseHierarchySearchVM;
import com.dbms.view.IARelationsTreeHelper;
import com.dbms.view.ListDetailsFormVM;
import com.dbms.view.ListNotesFormVM;
import com.dbms.view.TargetHierarchySearchVM;
import com.dbms.web.dto.FilterDTO;

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
	
	private static final String NO_SCOPE_FILTER = "-1";
	
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
	
	@ManagedProperty("#{globalController}")
    private GlobalController globalController;
	
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
    
    private Boolean versionUpgradingPending = null;
    private boolean displayScopeCatWeight;
    private boolean newPTButtonEnabled;
    private boolean readOnlyIA;
    
    private String		scopeFilter;
    
	private String dictionaryVersion;
	private boolean filterAll, filterReadOnly;
	
	private boolean dummySelected;
	private FilterDTO filterDTO;
	
	private String formToOpen;
	private HtmlInputHidden formClicked;



	public ImpactSearchController() {
		
	}
	
	@PreDestroy
	public void onDestroy() {
		System.out.println("\n ***********************  onDetroy ImpactSearchController");

		if (iaWizard != null) {
			System.out.println("\n ***********   show DIALOG: " + showConfirmDialog());
			RequestContext.getCurrentInstance().execute("PF('confirmIASaveDetails').show();");
		}
	}

	@PostConstruct
	public void init() {
		this.detailsFormModel  = new ListDetailsFormVM(this.authService, this.refCodeListService, this.appSWJSFRequest);
		this.impactedCmqBaseLazyDataModel = new CmqLazyDataModel(true);
		this.notImpactedCmqBaseLazyDataModel = new CmqLazyDataModel(false);
		this.impactedSmqBaseLazyDataModel = new SmqLazyDataModel(true);
		this.notImpactedSmqBaseLazyDataModel = new SmqLazyDataModel(false);
		currentTableRootTreeNode = new DefaultTreeNode("root", new HierarchyNode("CODE", "LEVEL", "SCOPE", null), null);
		targetTableRootTreeNode = new DefaultTreeNode("root",
				new HierarchyNode("CODE", "LEVEL", "SCOPE", "CATEGORY", "WEIGHT", null, null), null);
		currentHierarchySearchDlgModel = new CmqBaseHierarchySearchVM(cmqBaseCurrentService, smqBaseCurrentService,
				meddraDictCurrentService, cmqRelationCurrentService, globalController);
		targetHierarchySearchDlgModel = new TargetHierarchySearchVM(cmqBaseTargetService, smqBaseTargetService,
				meddraDictTargetService, cmqRelationTargetService, globalController);

		currentOrTarget = SELECTED_NO_LIST;
		
		newPtDistinctSocTermsList = this.meddraDictTargetService.findSocsWithNewPt();
		//changeOccur = false;
		displayScopeCatWeight = refCodeListService.getLevelScopeCategorySystemConfig();
		//scopeFilter = NO_SCOPE_FILTER;
		
		//Dictionary version
		RefConfigCodeList currentMeddraVersionCodeList = this.refCodeListService.getTargetMeddraVersion();
		
		setDictionaryVersion(currentMeddraVersionCodeList.getValue());
		this.filterReadOnly = false;
		filterDTO = new FilterDTO();
		
		detailsFormModel.setModelChanged(false);
	}
	
	public String openForm() {
		RequestContext.getCurrentInstance().execute("PF('confirmIASaveDetails').hide();");
		RequestContext.getCurrentInstance().execute("PF('confirmIASaveNotes').hide();");
		return formToOpen;
	}
	
	//AUTO SAVE
	public boolean showConfirmDialog() {
		boolean detailChanged = detailsFormModel.isModelChanged();
		boolean notesChanged = notesFormModel.isModelChanged();
				
		if (detailChanged || notesChanged || targetRelationsUpdated)		
			return true;
		
		return false;
	}
	
	public String initForm(String url) {
		String form = url + ".xhtml?faces-redirect=true";
		setFormToOpen(form);
		System.out.println("\n  ---- FORM IAController: " + formToOpen);
		
		if (!showConfirmDialog()) {
			return form;
		}
		else {
			if (targetRelationsUpdated) {
				RequestContext.getCurrentInstance().execute("PF('confirmIASaveRelations').show();");
				return "";
			}
			else if (notesFormModel.isModelChanged()) {
				RequestContext.getCurrentInstance().execute("PF('confirmIASaveNotes').show();");
				return "";
			}
			else if (detailsFormModel.isModelChanged()) {
				RequestContext.getCurrentInstance().execute("PF('confirmIASaveDetails').show();");
				return "";
			}  
			
		}
		return form;
	}
		
	public String saveDetailsAndClose() {
		RequestContext.getCurrentInstance().execute("PF('confirmIASaveDetails').hide();");
		saveDetails();
		return formToOpen;
	}
	
	public String saveNotesAndClose() {
		RequestContext.getCurrentInstance().execute("PF('confirmIASaveNotes').hide();");
		saveInformativeNotes();
		return formToOpen;
	}
	
	public String saveRelationsAndClose() {
		RequestContext.getCurrentInstance().execute("PF('confirmIASaveRelations').hide();");
		updateTargetRelations();
		return formToOpen;
	}
	
	
	public boolean filterByCode(Object value, Object filter, Locale locale) {
        String filterText = (filter == null) ? null : filter.toString().trim();
        if(filterText == null||filterText.equals("")) {
            return true;
        }
         
        if(value == null) {
            return false;
        }
         
        return ((Comparable) value).compareTo(Integer.valueOf(filterText)) > 0;
    }
	
	
	
	/**
	 * Generate Excel report on target datatable.
	 */
	public void generateExcel() {
		String dict = "", dictTarget = "";
		RefConfigCodeList currentMeddraVersionCodeList = this.refCodeListService.getCurrentMeddraVersion();
		RefConfigCodeList targetMeddraVersionCodeList = this.refCodeListService.getTargetMeddraVersion();
		if (currentMeddraVersionCodeList != null)
			dict = currentMeddraVersionCodeList.getValue();
		if (targetMeddraVersionCodeList != null)
			dictTarget = targetMeddraVersionCodeList.getValue();
		StreamedContent content = null;
		if (isImpactedCmqSelected)
			content = cmqBaseTargetService.generateCMQExcel(selectedImpactedCmqList, dictTarget, targetTableSelection, this.globalController.isFilterLltsFlag());
		
		if (isNonImpactedCmqSelected) 
			content = cmqBaseTargetService.generateCMQExcel(selectedNotImpactedCmqList, dictTarget, targetTableSelection, this.globalController.isFilterLltsFlag());
			
		if (isImpactedSmqSelected)
			content = smqBaseTargetService.generateSMQExcel(selectedImpactedSmqList, dictTarget, this.globalController.isFilterLltsFlag());
		
		if (isNonImpactedSmqSelected) 
			content = smqBaseTargetService.generateSMQExcel(selectedNotImpactedSmqList, dictTarget, this.globalController.isFilterLltsFlag());
			
		
		setExcelFile(content);
		
		//Clean content
		content = null;
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
			if(CSMQBean.IMPACT_TYPE_IMPACTED.equalsIgnoreCase(cmqBase.getImpactType())
                    || CSMQBean.IMPACT_TYPE_ICC.equalsIgnoreCase(cmqBase.getImpactType())
                    || CSMQBean.IMPACT_TYPE_IPC.equalsIgnoreCase(cmqBase.getImpactType())){
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
			if(CSMQBean.IMPACT_TYPE_IMPACTED.equalsIgnoreCase(smqBase.getImpactType())){
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
		RefConfigCodeList dict = this.refCodeListService.getCurrentMeddraVersion();
		
		setDictionaryVersion(dict.getValue());
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
		
		RefConfigCodeList dict = this.refCodeListService.getTargetMeddraVersion();
		setDictionaryVersion(dict.getValue());
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
        event.getTreeNode().setExpanded(true);
        IARelationsTreeHelper treeHelper = new IARelationsTreeHelper(
                cmqBaseCurrentService, smqBaseCurrentService, meddraDictCurrentService, cmqRelationCurrentService,
                cmqBaseTargetService, smqBaseTargetService, meddraDictTargetService, cmqRelationTargetService, globalController);
        
        treeHelper.setImpactTypes(getSelectedImpactFilters());
        treeHelper.onNodeExpandCurrentTable(currentTableRootTreeNode, event);
        
	}
    
    public void onNodeCollapseCurrentTable(NodeCollapseEvent event) {
        event.getTreeNode().setExpanded(false);
    }
	
	public void onNodeExpandTargetTable(NodeExpandEvent event) {
        IARelationsTreeHelper treeHelper = new IARelationsTreeHelper(
                cmqBaseCurrentService, smqBaseCurrentService, meddraDictCurrentService, cmqRelationCurrentService,
                cmqBaseTargetService, smqBaseTargetService, meddraDictTargetService, cmqRelationTargetService, globalController);
        treeHelper.setImpactTypes(getSelectedImpactFilters());
        treeHelper.onNodeExpandTargetTable(targetTableRootTreeNode, event, true);
	}
	

	public void collapsingORexpanding(TreeNode n, boolean option) {
		if (n.getChildren().size() == 0) {
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
	
	public void filterRelationsByScope(AjaxBehaviorEvent event) {
		updateTargetRelations();

		IARelationsTreeHelper treeHelper = new IARelationsTreeHelper(
				cmqBaseCurrentService, smqBaseCurrentService,
				meddraDictCurrentService, cmqRelationCurrentService,
				cmqBaseTargetService, smqBaseTargetService,
				meddraDictTargetService, cmqRelationTargetService, globalController);
		treeHelper.setImpactTypes(getSelectedImpactFilters());
		treeHelper.onNodeExpandTargetTableScope(targetTableRootTreeNode, null,
				scopeFilter);
 
		RequestContext.getCurrentInstance().update("impactAssessment:futureListsAndSmqs");

	}
    
	public void onNodeCollapseTargetTable(NodeCollapseEvent event) {
        event.getTreeNode().setExpanded(false);
    }
	
	public void updateTables() {
		this.updateCurrentTable();
		this.updateTargetTable();
	}
    
	public void updateCurrentTable() {
        IARelationsTreeHelper treeHelper = new IARelationsTreeHelper(
                cmqBaseCurrentService, smqBaseCurrentService, meddraDictCurrentService, cmqRelationCurrentService,
                cmqBaseTargetService, smqBaseTargetService, meddraDictTargetService, cmqRelationTargetService, globalController);
        treeHelper.setImpactTypes(getSelectedImpactFilters());
		//Init of the treenode to print only one selected list
		currentTableRootTreeNode = new DefaultTreeNode("root", new HierarchyNode("CODE",
				"LEVEL", "SCOPE", null), null);
		
		LOG.info("current called");	
		if (this.isImpactedCmqSelected) {
			treeHelper.updateCurrentTableForCmqList(currentTableRootTreeNode, this.selectedImpactedCmqList);
			//Current List name
			setListName(this.selectedImpactedCmqList.getCmqName());
			setNewPTButtonEnabled(false);
		} else if (this.isNonImpactedCmqSelected) {
			treeHelper.updateCurrentTableForCmqList(currentTableRootTreeNode, this.selectedNotImpactedCmqList);
			//Current List name
			setListName(this.selectedNotImpactedCmqList.getCmqName());
			setNewPTButtonEnabled(false);
		} else if (this.isImpactedSmqSelected) {
			treeHelper.updateCurrentTableForSmqList(currentTableRootTreeNode, this.selectedImpactedSmqList);
			//Current List name
			setListName(this.selectedImpactedSmqList.getSmqName());
			setNewPTButtonEnabled(true);
		} else if (this.isNonImpactedSmqSelected) {
			treeHelper.updateCurrentTableForSmqList(currentTableRootTreeNode, this.selectedNotImpactedSmqList);
			//Current List name
			setListName(this.selectedNotImpactedSmqList.getSmqName());
			setNewPTButtonEnabled(true);
		}
		
		//reset value of selected to ze
		currentOrTarget = SELECTED_NO_LIST;
	}

	public void updateTargetTable() {
        IARelationsTreeHelper treeHelper = new IARelationsTreeHelper(
                cmqBaseCurrentService, smqBaseCurrentService, meddraDictCurrentService, cmqRelationCurrentService,
                cmqBaseTargetService, smqBaseTargetService, meddraDictTargetService, cmqRelationTargetService, globalController);
        treeHelper.setImpactTypes(getSelectedImpactFilters());
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
										IEntity relationsHierarchyNodeEntity = relationsHierarchyNode.getEntity();
										if(relationsHierarchyNodeEntity instanceof SMQReverseHierarchySearchDto) {
											Long ptCode = ((SMQReverseHierarchySearchDto) relationsHierarchyNodeEntity).getSmqCode();
											SmqBaseTarget smqBaseTarget = this.smqBaseTargetService.findByCode(ptCode);
											relationsHierarchyNode.setEntity(smqBaseTarget);
											relationsHierarchyNode.setCode(smqBaseTarget.getSmqCode().toString());

											List<TreeNode> childTreeNodes = treeNode.getChildren();
											if(CollectionUtils.isNotEmpty(childTreeNodes)) {
												relationsHierarchyNode.setDataFetchCompleted(false);
												HierarchyNode dummyNode = new HierarchyNode(null, null, null, null);
												dummyNode.setDummyNode(true);
												new DefaultTreeNode(dummyNode, relationsTreeNode);
											}
										} else {
											List<TreeNode> childTreeNodes = treeNode.getChildren();
											if(CollectionUtils.isNotEmpty(childTreeNodes)) {
												HierarchyNode dummyNode = new HierarchyNode(null, null, null, null);
												dummyNode.setDummyNode(true);
												new DefaultTreeNode(dummyNode, relationsTreeNode);
											}
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
									if("NON-IMPACTED".equals(cmqEntity.getImpactType())) {
										cmqEntity.setImpactType("IPC");
										cmqEntity.setCmqState("PENDING IA");
										cmqEntity.setCmqStatus("P");
									}
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
										} else if ("HLGT".equalsIgnoreCase(hierarchyNode.getLevel())) {
											code = Long.parseLong(searchDto.getHlgtCode());
											matchingMap = this.checkIfReverseMeddraRelationExists(existingRelations, code, hierarchyNode);
										} else if ("HLT".equalsIgnoreCase(hierarchyNode.getLevel())) {
											code = Long.parseLong(searchDto.getHltCode());
											matchingMap = this.checkIfReverseMeddraRelationExists(existingRelations, code, hierarchyNode);
										} else if ("SOC".equalsIgnoreCase(hierarchyNode.getLevel())) {
											code = Long.parseLong(searchDto.getSocCode());
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
											} else if ("HLGT".equalsIgnoreCase(hierarchyNode.getLevel())) {
												cmqRelation.setHlgtCode(code);
											} else if ("HLT".equalsIgnoreCase(hierarchyNode.getLevel())) {
												cmqRelation.setHltCode(code);
											} else if ("SOC".equalsIgnoreCase(hierarchyNode.getLevel())) {
												cmqRelation.setSocCode(code);
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
									} else if (childEntity instanceof SMQReverseHierarchySearchDto) {
										SMQReverseHierarchySearchDto smqReverseHierarchySearchDto = (SMQReverseHierarchySearchDto) childEntity;
										matchingMap = this.checkIfSmqBaseOrSmqRelationExists(existingRelations, smqReverseHierarchySearchDto.getSmqCode()
																								, smqReverseHierarchySearchDto.getSmqCode().intValue(), hierarchyNode);
										matchFound = (boolean) matchingMap.get("MATCH_FOUND");
										updateNeeded = (boolean) matchingMap.get("UPDATE_NEEDED");
										if(updateNeeded) {
											cmqRelation = (CmqRelationTarget) matchingMap.get("TARGET_CMQ_RELATION_FOR_UPDATE");
										} else if(!matchFound) {
											cmqRelation = new CmqRelationTarget();
											cmqRelation.setCmqCode(cmqBaseTarget.getCmqCode());
											cmqRelation.setCmqId(cmqBaseTarget.getId());
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
						
						boolean relationsAndChildUpdated = false;
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
								
								relationsAndChildUpdated = true;
							} catch (CqtServiceException e) {
								LOG.error("Exception occurred while updated the list of CmqRelations for CMQ base target code "
										+ cmqBaseTarget.getCmqCode(), e);

								FacesContext.getCurrentInstance().addMessage(null, 
                                        new FacesMessage(FacesMessage.SEVERITY_ERROR,
                                            "An error occurred while updated the list of CmqRelations for CMQ base code " + cmqBaseTarget.getCmqCode(),
                                            "Error:" + e.getMessage()));
							}
						}
						
						List<CmqBaseTarget> impactedCmqsList = new ArrayList<>();
						
						//mark the cmqbase as Impacted if it is NON-IMPACTED
						String impactType = cmqBaseTarget.getImpactType();
						if("NON-IMPACTED".equalsIgnoreCase(impactType)) {
							cmqBaseTarget.setImpactType("IMPACTED");
							cmqBaseTarget.setCmqState("PENDING IA");
							cmqBaseTarget.setCmqStatus("P");
							impactedCmqsList.add(cmqBaseTarget);
						}
							
						//now check for parent of target cmq
						if(cmqBaseTarget.getCmqParentCode() != null) {
							CmqBaseTarget parentCmq = this.cmqBaseTargetService.findByCode(cmqBaseTarget.getCmqParentCode());
							if("NON-IMPACTED".equalsIgnoreCase(parentCmq.getImpactType())) {
								parentCmq.setImpactType("ICC");
								parentCmq.setCmqState("PENDING IA");
								parentCmq.setCmqStatus("P");
								impactedCmqsList.add(parentCmq);
							}
							List<CmqBaseTarget> childrenOfParentCmq = this.cmqBaseTargetService.findChildCmqsByParentCode(parentCmq.getCmqCode());
							for (ListIterator<CmqBaseTarget> li = childrenOfParentCmq.listIterator(); li.hasNext();) {
								CmqBaseTarget childOfParentCmq = li.next();
								if(childOfParentCmq.getCmqCode().longValue() == cmqBaseTarget.getCmqCode().longValue()) {
									li.remove();//Remove this cmq as we are already dealing with it.
								} else if("NON-IMPACTED".equalsIgnoreCase(childOfParentCmq.getImpactType())) {
									childOfParentCmq.setImpactType("ICC");
									childOfParentCmq.setCmqState("PENDING IA");
									childOfParentCmq.setCmqStatus("P");
									impactedCmqsList.add(childOfParentCmq);
								}
							}
						}
						
						try {
							if(impactedCmqsList.size() > 0) {
								this.cmqBaseTargetService.update(impactedCmqsList, this.authService.getUserCn()
										, this.authService.getUserGivenName(), this.authService.getUserSurName()
										, this.authService.getCombinedMappedGroupMembershipAsString());
							}
						} catch (CqtServiceException e) {
							LOG.error("Exception occurred while updated the parent and its children for CMQ code "
									+ cmqBaseTarget.getCmqCode(), e);

							FacesContext.getCurrentInstance().addMessage(null, 
                                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                                        "An error occurred while updated the list of CmqRelations for CMQ base code " + cmqBaseTarget.getCmqCode(),
                                        "Error:" + e.getMessage()));
						}
						
						
						//reset the flag to track target changes
						targetRelationsUpdated = false;
						FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_INFO,
								"Relations are successfully updated for target '" + cmqBaseTarget.getCmqName() + "'", "");
						FacesContext.getCurrentInstance().addMessage(null, msg);
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
			} else if ((null != cmqRelationTarget.getHlgtCode()) && (cmqRelationTarget.getHlgtCode().longValue() == code.longValue())) {
				matchingMap.put("MATCH_FOUND", true);
			} else if ((null != cmqRelationTarget.getHltCode()) && (cmqRelationTarget.getHltCode().longValue() == code.longValue())) {
				matchingMap.put("MATCH_FOUND", true);
			} else if ((null != cmqRelationTarget.getSocCode()) && (cmqRelationTarget.getSocCode().longValue() == code.longValue())) {
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
				SmqBase190 current = smqBaseCurrentService.findByCode(((SmqBase190) d).getSmqCode());
				notesFormModel.loadFromSmqBase190(current);
			} else if(d instanceof CmqBaseTarget) {
				notesFormModel.loadFromCmqBaseTarget((CmqBaseTarget)d);
				detailsFormModel.loadFromCmqBaseTarget((CmqBaseTarget)d);
			} else if(d instanceof SmqBaseTarget) {
				SmqBaseTarget target = smqBaseTargetService.findByCode(((SmqBaseTarget) d).getSmqCode());
				notesFormModel.loadFromSmqBaseTarget(target);
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
				this.setCmqBaseAsImpacted((CmqBaseTarget)d);
			} else if(d != null && d instanceof SmqBaseTarget) {
				notesFormModel.saveToSmqBaseTarget((SmqBaseTarget)d);
				//
			}
            notesFormModel.setModelChanged(false);
            
            
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
				((CmqBase190)d).setCmqDesignee2(detailsFormModel.getDesigneeTwo());
				((CmqBase190)d).setCmqDesignee3(detailsFormModel.getDesigneeThree());

				cmqBaseCurrentService.update((CmqBase190)d, this.authService.getUserCn()
						, this.authService.getUserGivenName(), this.authService.getUserSurName()
						, this.authService.getCombinedMappedGroupMembershipAsString());
			} else if(d != null && d instanceof CmqBaseTarget) {
				((CmqBaseTarget)d).setCmqDesignee(detailsFormModel.getDesignee());
				((CmqBaseTarget)d).setCmqDesignee2(detailsFormModel.getDesigneeTwo());
				((CmqBaseTarget)d).setCmqDesignee3(detailsFormModel.getDesigneeThree());
				cmqBaseTargetService.update((CmqBaseTarget)d, this.authService.getUserCn()
						, this.authService.getUserGivenName(), this.authService.getUserSurName()
						, this.authService.getCombinedMappedGroupMembershipAsString());
				this.setCmqBaseAsImpacted((CmqBaseTarget)d);
			}
			detailsFormModel.setModelChanged(false);
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

	public void reloadRelationsOnFilterLltFlagToggle() {
		boolean filterLltFlag = this.globalController.isFilterLltsFlag();
		if(null != this.currentTableRootTreeNode) {
			List<TreeNode> childrenNodes = this.currentTableRootTreeNode.getChildren();
			for (TreeNode childTreeNode : childrenNodes) {
				childTreeNode.setExpanded(false);
				childTreeNode.getChildren().clear();//remove all children
				HierarchyNode hNode = (HierarchyNode) childTreeNode.getData();
				hNode.setDataFetchCompleted(false);
				
				//since the child will always be a cmq or an smq so just add the dummy node
				if(childTreeNode.getChildCount() == 0) {
					HierarchyNode dummyNode = new HierarchyNode(null, null, null, null);
					dummyNode.setDummyNode(true);
					new DefaultTreeNode(dummyNode, childTreeNode);
				}
			}
			UIViewRoot viewRoot = FacesContext.getCurrentInstance().getViewRoot();
			if(null != viewRoot) {
				//in ia left side
				UIComponent currentListsAndSmqsComponent = CmqUtils.findComponent(viewRoot, "currentListsAndSmqs");
				if(null != currentListsAndSmqsComponent) {
					//update has to be on currentListsAndSmqsComponent.getClientId() and not on the xhtml id
					RequestContext.getCurrentInstance().update(currentListsAndSmqsComponent.getClientId());
				}
			}
		}
		if(null != this.targetTableRootTreeNode) {
			List<TreeNode> childrenNodes = this.targetTableRootTreeNode.getChildren();
			for (TreeNode childTreeNode : childrenNodes) {
				childTreeNode.setExpanded(false);
				childTreeNode.getChildren().clear();//remove all children
				HierarchyNode hNode = (HierarchyNode) childTreeNode.getData();
				hNode.setDataFetchCompleted(false);
				
				//since the child will always be a cmq or an smq so just add the dummy node
				if(childTreeNode.getChildCount() == 0) {
					HierarchyNode dummyNode = new HierarchyNode(null, null, null, null);
					dummyNode.setDummyNode(true);
					new DefaultTreeNode(dummyNode, childTreeNode);
				}
			}
			UIViewRoot viewRoot = FacesContext.getCurrentInstance().getViewRoot();
			if(null != viewRoot) {
				//in ia left side
				UIComponent targetListsAndSmqsComponent = CmqUtils.findComponent(viewRoot, "futureListsAndSmqs");
				if(null != targetListsAndSmqsComponent) {
					//update has to be on targetListsAndSmqsComponent.getClientId() and not on the xhtml id
					RequestContext.getCurrentInstance().update(targetListsAndSmqsComponent.getClientId());
				}
			}
		}
	}
	
	public void addSelectedNewPtsToTargetRelation() {
        IARelationsTreeHelper treeHelper = new IARelationsTreeHelper(cmqBaseTargetService, smqBaseTargetService, meddraDictTargetService, cmqRelationTargetService, globalController);
        treeHelper.setImpactTypes(getSelectedImpactFilters());
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
				
					Long countOfChildren = this.meddraDictTargetService.findChildrenCountByParentCode("LLT_", "PT_", dtoCode);
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
					this.targetRelationsUpdated = true;
					break;
				} else if (childTreeNode.getChildCount() > 0) {
					// drill down
					this.deleteRelation(childTreeNode, selectedNode);
				}
			}
		}
	}
    
    /**
     * Returns the CMQ/SMQ relation impact type message (Hover text on relations hierarchy row/code column):
     * @param node
     * @return 
     */
    public String getImpactDesc(HierarchyNode node, String currentOrTarget) {
        return IARelationsTreeHelper.getImpactDesc(refCodeListService, node, currentOrTarget);
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
	
	private void setCmqBaseAsImpacted(CmqBaseTarget cmqBaseTarget) {
		if(null != cmqBaseTarget) {
			List<CmqBaseTarget> impactedCmqsList = new ArrayList<>();
			
			//mark the cmqbase as Impacted if it is NON-IMPACTED
			String impactType = cmqBaseTarget.getImpactType();
			if("NON-IMPACTED".equalsIgnoreCase(impactType)) {
				cmqBaseTarget.setImpactType("IMPACTED");
				cmqBaseTarget.setCmqState("PENDING IA");
				cmqBaseTarget.setCmqStatus("P");
				impactedCmqsList.add(cmqBaseTarget);
			}
			
			//find any child relations and update them
			List<CmqBaseTarget> children = this.cmqBaseTargetService.findChildCmqsByParentCode(cmqBaseTarget.getCmqCode());
			if(null != children) {
				for (CmqBaseTarget child : children) {
					if("NON-IMPACTED".equals(child.getImpactType())) {
						child.setImpactType("IPC");
						child.setCmqState("PENDING IA");
						child.setCmqStatus("P");
						impactedCmqsList.add(child);
					}
				}
			}
			
			//find parent relation and the children of that parent and update them all
			if(cmqBaseTarget.getCmqParentCode() != null) {
				CmqBaseTarget parentCmq = this.cmqBaseTargetService.findByCode(cmqBaseTarget.getCmqParentCode());
				if("NON-IMPACTED".equalsIgnoreCase(parentCmq.getImpactType())) {
					parentCmq.setImpactType("ICC");
					parentCmq.setCmqState("PENDING IA");
					parentCmq.setCmqStatus("P");
					impactedCmqsList.add(parentCmq);
				}
				List<CmqBaseTarget> childrenOfParentCmq = this.cmqBaseTargetService.findChildCmqsByParentCode(parentCmq.getCmqCode());
				for (ListIterator<CmqBaseTarget> li = childrenOfParentCmq.listIterator(); li.hasNext();) {
					CmqBaseTarget childOfParentCmq = li.next();
					if(childOfParentCmq.getCmqCode().longValue() == cmqBaseTarget.getCmqCode().longValue()) {
						li.remove();//Remove this cmq as we are already dealing with it.
					} else if("NON-IMPACTED".equalsIgnoreCase(childOfParentCmq.getImpactType())) {
						childOfParentCmq.setImpactType("ICC");
						childOfParentCmq.setCmqState("PENDING IA");
						childOfParentCmq.setCmqStatus("P");
						impactedCmqsList.add(childOfParentCmq);
					}
				}
			}
			
			//now update them all in one query
			try {
				if(impactedCmqsList.size() > 0) {
					this.cmqBaseTargetService.update(impactedCmqsList, this.authService.getUserCn()
							, this.authService.getUserGivenName(), this.authService.getUserSurName()
							, this.authService.getCombinedMappedGroupMembershipAsString());
				}
			} catch (CqtServiceException e) {
				LOG.error("Exception occurred while updated the cmq, its parent and other children of the parent for base CMQ code "
						+ cmqBaseTarget.getCmqCode(), e);

				FacesContext.getCurrentInstance().addMessage(null, 
                        new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "An error occurred while updated the impac type for CMQ base code " + cmqBaseTarget.getCmqCode(),
                            "Error:" + e.getMessage()));
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
				fetchedCmqBaseList = cmqBaseTargetService.findImpactedWithPaginated(first, pageSize, null, null,
						filters);
				this.setRowCount(fetchedCmqBaseList.size());
			} else {
				LOG.info("Loading more not impacted list cmqs starting from " + first + " with page size of "
						+ pageSize);
				fetchedCmqBaseList = cmqBaseTargetService.findNotImpactedWithPaginated(first, pageSize, null, null,
						filters);
				this.setRowCount(fetchedCmqBaseList.size());
			}

			if (fetchedCmqBaseList != null)
				this.cmqBaseList.addAll(fetchedCmqBaseList);
			return fetchedCmqBaseList;
		}

		@Override
		public List<CmqBaseTarget> load(int first, int pageSize, String sortField, SortOrder sortOrder,
				Map<String, Object> filters) {
			List<CmqBaseTarget> fetchedCmqBaseList = null;
			if (this.manageImpactedList) {
				LOG.info("Loading more impacted list cmqs starting from " + first + " with page size of " + pageSize);
				fetchedCmqBaseList = cmqBaseTargetService.findImpactedWithPaginated(first, pageSize, null, null,
						filters);
				this.setRowCount(cmqBaseTargetService.findImpactedCount().intValue());
			} else {
				LOG.info("Loading more not impacted list cmqs starting from " + first + " with page size of "
						+ pageSize);
				fetchedCmqBaseList = cmqBaseTargetService.findNotImpactedWithPaginated(first, pageSize, null, null,
						filters);
				this.setRowCount(cmqBaseTargetService.findNotImpactedCount().intValue());
			}

			if (fetchedCmqBaseList != null)
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
			List<SmqBaseTarget> paginatedList = new ArrayList<>();
			List<Map<String, Object>> fetchedSmqBaseList = null;
			if (this.manageImpactedList) {
				LOG.info("Loading more impacted list smqs starting from " + first + " with page size of " + pageSize);
				fetchedSmqBaseList = smqBaseTargetService.findImpactedWithPaginated(first, pageSize, null, null,
						filters);
				this.setRowCount(smqBaseTargetService.findImpactedCount(filters).intValue());
			} else {
				LOG.info("Loading more not impacted list smqs starting from " + first + " with page size of "
						+ pageSize);
				fetchedSmqBaseList = smqBaseTargetService.findNotImpactedWithPaginated(first, pageSize, null, null,
						filters);
				this.setRowCount(smqBaseTargetService.findNotImpactedCount(filters).intValue());
			}

			if (fetchedSmqBaseList != null) {
				for (Map<String, Object> map : fetchedSmqBaseList) {
					SmqBaseTarget target = new SmqBaseTarget();
					if(null != map.get("dictionaryVersion")) {
						target.setDictionaryVersion(map.get("dictionaryVersion").toString());
					}
					if(null != map.get("smqId")) {
						target.setId(Long.valueOf(map.get("smqId").toString()));
					}
					if(null != map.get("impactType")) {
						target.setImpactType(map.get("impactType").toString());
					}
					if(null != map.get("smqAlgorithm")) {
						target.setSmqAlgorithm(map.get("smqAlgorithm").toString());
					}
					if(null != map.get("smqCode")) {
						target.setSmqCode(Long.valueOf(map.get("smqCode").toString()));
					}
					if(null != map.get("smqDescription")) {
						target.setSmqDescription(map.get("smqDescription").toString());
					}
					if(null != map.get("smqId")) {
						target.setSmqId(Long.valueOf(map.get("smqId").toString()));
					}
					if(null != map.get("smqLevel")) {
						target.setSmqLevel(Integer.valueOf(map.get("smqLevel").toString()));
					}
					if(null != map.get("smqName")) {
						target.setSmqName(map.get("smqName").toString());
					}
					if(null != map.get("smqNote")) {
						target.setSmqNote(map.get("smqNote").toString());
					}
					if(null != map.get("smqParentCode")) {
						target.setSmqParentCode(Long.valueOf(map.get("smqParentCode").toString()));
					}
					if(null != map.get("smqParentName")) {
						target.setSmqParentName(map.get("smqParentName").toString());
					}
					if(null != map.get("smqSource")) {
						target.setSmqSource(map.get("smqSource").toString());
					}
					if(null != map.get("smqStatus")) {
						target.setSmqStatus(map.get("smqStatus").toString());
					}
					paginatedList.add(target);
				}
				this.smqBaseList.addAll(paginatedList);
			}
			return paginatedList;
		}

		@Override
		public List<SmqBaseTarget> load(int first, int pageSize, String sortField, SortOrder sortOrder,
				Map<String, Object> filters) {
			List<SmqBaseTarget> paginatedList = new ArrayList<>();
			List<Map<String, Object>> fetchedSmqBaseList = null;
			if (this.manageImpactedList) {
				LOG.info("Loading more impacted list cmqs starting from " + first + " with page size of " + pageSize);
				fetchedSmqBaseList = smqBaseTargetService.findImpactedWithPaginated(first, pageSize, null, null,
						filters);
				this.setRowCount(smqBaseTargetService.findImpactedCount(filters).intValue());
			} else {
				LOG.info("Loading more not impacted list cmqs starting from " + first + " with page size of "
						+ pageSize);
				fetchedSmqBaseList = smqBaseTargetService.findNotImpactedWithPaginated(first, pageSize, null, null,
						filters);
				this.setRowCount(smqBaseTargetService.findNotImpactedCount(filters).intValue());
 			}
			if (fetchedSmqBaseList != null) {
				for (Map<String, Object> map : fetchedSmqBaseList) {
					SmqBaseTarget target = new SmqBaseTarget();
					if(null != map.get("dictionaryVersion")) {
						target.setDictionaryVersion(map.get("dictionaryVersion").toString());
					}
					if(null != map.get("smqId")) {
						target.setId(Long.valueOf(map.get("smqId").toString()));
					}
					if(null != map.get("impactType")) {
						target.setImpactType(map.get("impactType").toString());
					}
					if(null != map.get("smqAlgorithm")) {
						target.setSmqAlgorithm(map.get("smqAlgorithm").toString());
					}
					if(null != map.get("smqCode")) {
						target.setSmqCode(Long.valueOf(map.get("smqCode").toString()));
					}
					if(null != map.get("smqDescription")) {
						target.setSmqDescription(map.get("smqDescription").toString());
					}
					if(null != map.get("smqId")) {
						target.setSmqId(Long.valueOf(map.get("smqId").toString()));
					}
					if(null != map.get("smqLevel")) {
						target.setSmqLevel(Integer.valueOf(map.get("smqLevel").toString()));
					}
					if(null != map.get("smqName")) {
						target.setSmqName(map.get("smqName").toString());
					}
					if(null != map.get("smqNote")) {
						target.setSmqNote(map.get("smqNote").toString());
					}
					if(null != map.get("smqParentCode")) {
						target.setSmqParentCode(Long.valueOf(map.get("smqParentCode").toString()));
					}
					if(null != map.get("smqParentName")) {
						target.setSmqParentName(map.get("smqParentName").toString());
					}
					if(null != map.get("smqSource")) {
						target.setSmqSource(map.get("smqSource").toString());
					}
					if(null != map.get("smqStatus")) {
						target.setSmqStatus(map.get("smqStatus").toString());
					}
					paginatedList.add(target);
				}
				this.smqBaseList.addAll(paginatedList);
			}

			int dataSize = fetchedSmqBaseList.size();

			// paginate
			if (dataSize > pageSize) {
				try {
					return paginatedList.subList(first, first + pageSize);
				} catch (IndexOutOfBoundsException e) {
					return paginatedList.subList(first, first + (dataSize % pageSize));
				}
			} else {
				return paginatedList;
			}
		}

		@Override
		public SmqBaseTarget getRowData(String rowKey) {
			long rowKeyLong = Long.parseLong(rowKey);
			for (SmqBaseTarget smqBaseTarget : smqBaseList) {
				if (smqBaseTarget.getSmqCode().longValue() == rowKeyLong) {
					return smqBaseTarget;
				}
			}
			return null;
		}

		@Override
		public Object getRowKey(SmqBaseTarget object) {
			return object.getSmqCode();
		}

	}
	
	/*private class LazySorter implements Comparator<SmqBaseTarget> {
		 
	    private String sortField;
	     
	    private SortOrder sortOrder;
	     
	    public LazySorter(String sortField, SortOrder sortOrder) {
	        this.sortField = sortField;
	        this.sortOrder = sortOrder;
	    }
	 
	    public int compare(SmqBaseTarget one, SmqBaseTarget two) {
	        try {
	            Object value1 = SmqBaseTarget.class.getField(this.sortField).get(one);
	            Object value2 = SmqBaseTarget.class.getField(this.sortField).get(two);
	 
	            int value = ((Comparable)value1).compareTo(value2);
	             
	            return SortOrder.ASCENDING.equals(sortOrder) ? value : -1 * value;
	        }
	        catch(Exception e) {
	            throw new RuntimeException();
	        }
	    }
	}*/
	
	
	
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
	
	public List<RefConfigCodeList> getLevelListForIA() {
		List<RefConfigCodeList> levels = refCodeListService.findByConfigType(
				CqtConstants.CODE_LIST_TYPE_DICTIONARY_LEVELS, OrderBy.ASC);
		RefConfigCodeList levelToRemove = refCodeListService.findByConfigTypeAndInternalCode(CqtConstants.CODE_LIST_TYPE_DICTIONARY_LEVELS, "NC-LLT");
		
		if (levelToRemove != null)
			levels.remove(levelToRemove);
		
		return levels;
	}
	
	public String formatDesignee(String designee1, String designee2, String designee3) {
		StringBuilder result = new StringBuilder();
		//String result = "";
		
		if (designee1 != null && !designee1.equals(""))
			result.append(designee1);
		if (designee2 != null && !designee2.equals(""))
			result.append(", ").append(designee2);
		if (designee3 != null && !designee3.equals(""))
			result.append(", ").append(designee3);
		return result.toString();
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
		 /**
         * Restrictions on users from  REQUESTOR and ADMIN groups
         */
        if (authService.getGroupMembershipHeader() != null && (authService.getGroupMembershipHeader().contains(AuthenticationService.REQUESTER_GROUP)
                || authService.getGroupMembershipHeader().contains("MQM"))) {        	
        	if (detailsFormModel.getStatus().equals(CmqBaseTarget.CMQ_STATUS_VALUE_ACTIVE) 
        			|| (detailsFormModel.getDesignee() != null && detailsFormModel.getDesignee().equals(authService.getUserCn()))
        			|| (detailsFormModel.getDesigneeTwo() != null && detailsFormModel.getDesigneeTwo().equals(authService.getUserCn()))
        			|| (detailsFormModel.getDesigneeThree() != null && detailsFormModel.getDesigneeThree().equals(authService.getUserCn()))) {
        		System.out.println("\n ******************** authService.getUserCn() " + authService.getUserCn());
        		return  false;
        	}
        }
        
		if (this.currentOrTarget == SELECTED_CURRENT_LIST ||
                (detailsFormModel.getState().equals(CmqBaseTarget.CMQ_STATE_APPROVED_IA) ||
                detailsFormModel.getState().equals(CmqBaseTarget.CMQ_STATE_PUBLISHED_IA)))
			return true;
		if (this.currentOrTarget == SELECTED_TARGET_LIST)
			return false;
		return true;
	}
	
	public boolean isDetailsFormReadonly() {
		 /**
         * Restrictions on users from  REQUESTOR and ADMIN groups
         */
        if (authService.getGroupMembershipHeader()!= null && (authService.getGroupMembershipHeader().contains(AuthenticationService.REQUESTER_GROUP) || 
        		authService.getGroupMembershipHeader().contains(AuthenticationService.ADMIN_GROUP))) {        	
        	if (detailsFormModel.getStatus().equals(CmqBaseTarget.CMQ_STATUS_VALUE_ACTIVE) 
        			|| (detailsFormModel.getDesignee() != null && detailsFormModel.getDesignee().equals(authService.getUserCn()))
        			|| (detailsFormModel.getDesigneeTwo() != null && detailsFormModel.getDesigneeTwo().equals(authService.getUserCn()))
        			|| (detailsFormModel.getDesigneeThree() != null && detailsFormModel.getDesigneeThree().equals(authService.getUserCn()))) {
        		System.out.println("\n ******************** authService.getUserCn() " + authService.getUserCn());
        		return  false;
        	}
        }
        
		
		if (this.currentOrTarget == SELECTED_CURRENT_LIST ||
                (detailsFormModel.getState().equals(CmqBaseTarget.CMQ_STATE_APPROVED_IA) ||
                detailsFormModel.getState().equals(CmqBaseTarget.CMQ_STATE_PUBLISHED_IA)))
			return true;
		if (this.currentOrTarget == SELECTED_TARGET_LIST)
			return false;
		return true;
	}
	
	public boolean isFilterEnabled() {
		return (this.isImpactedSmqSelected || this.isNonImpactedSmqSelected || this.isImpactedCmqSelected
				|| this.isNonImpactedCmqSelected);
	}

	public void refreshTables(AjaxBehaviorEvent event) {
		LOG.info("calling refres tables");
		this.updateCurrentTable();
		this.updateTargetTable();
	}
	public void selectFilter(AjaxBehaviorEvent event) {
		
		filterDTO.setAll(false);
		this.filterReadOnly = false;
		LOG.info("  ---- filterDTO all value : " + filterDTO.isAll());
	}
	
	public void selectAllFilter(AjaxBehaviorEvent event) {
		if (filterDTO.isAll()) {
			this.filterReadOnly = true;
			this.filterDTO.resetAll();
		}
		else {
			this.filterReadOnly = false;
		}
		
		LOG.info("  ---- filterDTO all value : " + filterDTO.isAll());
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
		if(selectedImpactedCmqList != null) {
			return true;
		} else if(selectedNotImpactedCmqList != null) {
			return true;
		} else {
			return false;
		} 
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

    public String getSelectedListExtension() {
        if(this.selectedImpactedCmqList != null)
            return this.selectedImpactedCmqList.getCmqTypeCd();
        else if(this.selectedNotImpactedCmqList != null)
            return this.selectedNotImpactedCmqList.getCmqTypeCd();
        return "";
    }
    
    public boolean isVersionUpgradePending() {
        if(versionUpgradingPending == null) {
            versionUpgradingPending = cmqBaseTargetService.isVersionUpgradePending();
        }
        return versionUpgradingPending;
    }

	public boolean isDisplayScopeCatWeight() {
		return displayScopeCatWeight;
	}

	public void setDisplayScopeCatWeight(boolean displayScopeCatWeight) {
		this.displayScopeCatWeight = displayScopeCatWeight;
	}

	public boolean isNewPTButtonEnabled() {
		return newPTButtonEnabled;
	}

	public void setNewPTButtonEnabled(boolean newPTButtonEnabled) {
		this.newPTButtonEnabled = newPTButtonEnabled;
	}

	public boolean isReadOnlyIA() {
		return ((selectedImpactedCmqList != null && selectedImpactedCmqList.getCmqState() != null && 
				(selectedImpactedCmqList.getCmqState().equals("APPROVED IA") || selectedImpactedCmqList.getCmqState().equals("PUBLISHED IA"))) ||
				(selectedNotImpactedCmqList != null && selectedNotImpactedCmqList.getCmqState() != null 
				&& (selectedNotImpactedCmqList.getCmqState().equals("PUBLISHED IA") || selectedNotImpactedCmqList.getCmqState().equals("APPROVED IA")))); 
		//return readOnlyIA;
	}
	
	public void filterRelationsByScopeInTargetTable(HierarchyNode node) {
		IEntity entity = node.getEntity();
		if((entity instanceof SmqBaseTarget) || (entity instanceof SMQReverseHierarchySearchDto)) {
			node.setDataFetchCompleted(false);
			TreeNode treeNode = this.clearChildrenInTargetTableTreNode(targetTableRootTreeNode, node);
			if(null != treeNode) {
				collapseRelationsInTargetTable(treeNode);
			}
		}
		this.setTargetRelationsUpdated();
	}

	public TreeNode clearChildrenInTargetTableTreNode(TreeNode rootNodeToSearchFrom, HierarchyNode selectedNode) {
		TreeNode treeNode = null;
		if (rootNodeToSearchFrom.getChildCount() > 0) {
			List<TreeNode> childTreeNodes = null;
			if(rootNodeToSearchFrom.getParent() == null) {//root node
				TreeNode parentTreeNode = rootNodeToSearchFrom.getChildren().get(0);
				if(parentTreeNode != null) {
					childTreeNodes = parentTreeNode.getChildren();
				}
			} else {
				childTreeNodes = rootNodeToSearchFrom.getChildren();
			}
			if(CollectionUtils.isNotEmpty(childTreeNodes)) {
				for (Iterator<TreeNode> treeNodeIterator = childTreeNodes
						.listIterator(); treeNodeIterator.hasNext();) {
					TreeNode childTreeNode = treeNodeIterator.next();
					HierarchyNode childNode = (HierarchyNode) childTreeNode
							.getData();
					if (childNode.equals(selectedNode)) {
						childTreeNode.getChildren().clear(); // clear child list
						HierarchyNode dummyNode = new HierarchyNode(null, null, null, null);
				        dummyNode.setDummyNode(true);
				        new DefaultTreeNode(dummyNode, childTreeNode);
				        treeNode = childTreeNode;
						break;
					} else if (childTreeNode.getChildCount() > 0) {
						// drill down
						treeNode = this.clearChildrenInTargetTableTreNode(childTreeNode, selectedNode);
						if(treeNode != null) {
							break;
						}
					}
				}
			}
		}
		return treeNode;
	}
	
	public void collapseRelationsInTargetTable(TreeNode node) {
		collapsingORexpanding(node, false);
	}
	
	public void setReadOnlyIA(boolean readOnlyIA) {
		this.readOnlyIA = readOnlyIA;
	}
    
    public void setTargetRelationsUpdated() {
        targetRelationsUpdated = true;
    }

	public String getScopeFilter() {
		return scopeFilter;
	}

	public void setScopeFilter(String scopeFilter) {
		this.scopeFilter = scopeFilter;
	}

	public boolean isImpactedSmqSelected() {
		return isImpactedSmqSelected;
	}

	public void setImpactedSmqSelected(boolean isImpactedSmqSelected) {
		this.isImpactedSmqSelected = isImpactedSmqSelected;
	}

	public boolean isNonImpactedSmqSelected() {
		return isNonImpactedSmqSelected;
	}

	public void setNonImpactedSmqSelected(boolean isNonImpactedSmqSelected) {
		this.isNonImpactedSmqSelected = isNonImpactedSmqSelected;
	}
	
	public String getDictionaryVersion() {
		return dictionaryVersion;
	}

	public void setDictionaryVersion(String dictionaryVersion) {
		this.dictionaryVersion = dictionaryVersion;
	}

	public int getCurrentOrTarget() {
		return currentOrTarget;
	}

	public void setCurrentOrTarget(int currentOrTarget) {
		this.currentOrTarget = currentOrTarget;
	}

	public boolean isFilterAll() {
		return filterAll;
	}

	public void setFilterAll(boolean filterAll) {
		this.filterAll = filterAll;
	}

	public boolean isFilterReadOnly() {
		return filterReadOnly;
	}

	public void setFilterReadOnly(boolean filterReadOnly) {
		this.filterReadOnly = filterReadOnly;
	}

	public boolean isDummySelected() {
		return dummySelected;
	}

	public void setDummySelected(boolean dummySelected) {
		this.dummySelected = dummySelected;
	}

	public FilterDTO getFilterDTO() {
		return filterDTO;
	}

	public void setFilterDTO(FilterDTO filterDTO) {
		this.filterDTO = filterDTO;
	}
	
	public GlobalController getGlobalController() {
		return globalController;
	}

	public void setGlobalController(GlobalController globalController) {
		this.globalController = globalController;
	}
	
	private List<String> getSelectedImpactFilters() {
		List<String> impactFilterList = new ArrayList<>();
		if(filterDTO.isAll()) {
			impactFilterList.add("LPP");
			impactFilterList.add("NCH");
			impactFilterList.add("MRG");
			impactFilterList.add("SDP");
			impactFilterList.add("NTR");
			impactFilterList.add("LCN");
			impactFilterList.add("HPP");
			impactFilterList.add("HNP");
			impactFilterList.add("PDL");
			impactFilterList.add("SWC");
			impactFilterList.add("PSI");
			impactFilterList.add("PSA");
			impactFilterList.add("DTR");
			impactFilterList.add("LDP");
			impactFilterList.add("PDH");
			impactFilterList.add("HDH");
			impactFilterList.add("HDS");
			impactFilterList.add("SCH");
			impactFilterList.add("MQM");
			return impactFilterList;
		}
		if(filterDTO.isLltPromotedToPT()) {
			impactFilterList.add("LPP");
		}
		if(filterDTO.isMedDraTermNameChanged()) {
			impactFilterList.add("NCH");
		}
		if(filterDTO.isMergedHLGT()) {
			impactFilterList.add("MRG");
		}
		if(filterDTO.isNewSuccessorPT()) {
			impactFilterList.add("SDP");
		}
		if(filterDTO.isNewTermAdded()) {
			impactFilterList.add("NTR");
		}
		if(filterDTO.isNonCurrentLLT()) {
			impactFilterList.add("LCN");
		}
		if(filterDTO.isPrimarySOCChange()) {
			impactFilterList.add("HPP");
			impactFilterList.add("HNP");
		}
		if(filterDTO.isPtDemotedToLLT()) {
			impactFilterList.add("PDL");
		}
		if(filterDTO.isScopechanged()) {
			impactFilterList.add("SWC");
		}
		if(filterDTO.isStatusChanged()) {
			impactFilterList.add("PSI");
			impactFilterList.add("PSA");
		}
		if(filterDTO.isTermDeleted()) {
			impactFilterList.add("DTR");
		}
		if(filterDTO.isTermMoved()) {
			impactFilterList.add("LDP");
			impactFilterList.add("PDH");
			impactFilterList.add("HDH");
			impactFilterList.add("HDS");
		}
		if(filterDTO.isImpactedSMQ()) {
			impactFilterList.add("SCH");
		}
		return impactFilterList;
	}

	public String getFormToOpen() {
		return formToOpen;
	}

	public void setFormToOpen(String formToOpen) {
		this.formToOpen = formToOpen;
	}

	public HtmlInputHidden getFormClicked() {
		return formClicked;
	}

	public void setFormClicked(HtmlInputHidden formClicked) {
		this.formClicked = formClicked;
	}
}
