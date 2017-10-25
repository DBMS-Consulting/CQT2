package com.dbms.view;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.event.AjaxBehaviorEvent;

import org.apache.commons.collections4.CollectionUtils;
import org.primefaces.event.CloseEvent;
import org.primefaces.event.NodeExpandEvent;
import org.primefaces.event.RowEditEvent;
import org.primefaces.model.DefaultTreeNode;
import org.primefaces.model.TreeNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dbms.controller.beans.HierarchySearchResultBean;
import com.dbms.csmq.HierarchyNode;
import com.dbms.entity.cqt.CmqBaseTarget;
import com.dbms.entity.cqt.SmqBaseTarget;
import com.dbms.entity.cqt.dtos.MeddraDictHierarchySearchDto;
import com.dbms.entity.cqt.dtos.MeddraDictReverseHierarchySearchDto;
import com.dbms.entity.cqt.dtos.SMQReverseHierarchySearchDto;
import com.dbms.service.ICmqBaseTargetService;
import com.dbms.service.ICmqRelationTargetService;
import com.dbms.service.IMeddraDictTargetService;
import com.dbms.service.ISmqBaseTargetService;
import com.dbms.util.MeddraDictLevelHelper;
import com.dbms.util.SMQLevelHelper;

/**
 * @date Feb 7, 2017 7:39:34 AM
 **/
public class TargetHierarchySearchVM {

	private static final Logger LOG = LoggerFactory
			.getLogger(TargetHierarchySearchVM.class);
	
	 public static int SEARCH_DIRECTION_UP = 1;
	   public static int SEARCH_DIRECTION_DOWN = 2;

	private ICmqBaseTargetService cmqBaseTargetService;
	private ISmqBaseTargetService smqBaseTargetService;
	private IMeddraDictTargetService meddraDictTargetService;
	private ICmqRelationTargetService cmqRelationTargetService;

	private String myFilterTermName;
	private String myFilterLevel;

	private TreeNode myHierarchyRoot;
	private TreeNode[] mySelectedNodes;
	
	private boolean nonCurrentLlt;
    private boolean showPrimaryPath;
    private boolean showPrimaryPathOnly;
    
    private int searchDirection;
    private int appliedSearchDirection;
    private boolean searchUpDisabled;
    private boolean searchDownDisabled;
    private boolean enableRadioButtons;
	
	private List<HierarchySearchResultBean> hierarchySearchResults;
	
	private IRelationsChangeListener onDropRelationsListener;

	public TargetHierarchySearchVM(ICmqBaseTargetService cmqBaseSvc,
			ISmqBaseTargetService smqBaseSvc,
			IMeddraDictTargetService meddraDictSvc,
			ICmqRelationTargetService cmqRelationSvc) {
		this.cmqBaseTargetService = cmqBaseSvc;
		this.smqBaseTargetService = smqBaseSvc;
		this.meddraDictTargetService = meddraDictSvc;
		this.cmqRelationTargetService = cmqRelationSvc;
		
		myHierarchyRoot = new DefaultTreeNode("root", new HierarchyNode("LEVEL",
				"NAME", "CODE", null), null);
		searchDirection = SEARCH_DIRECTION_UP; //UP
		enableRadioButtons = true;
	}

	public void onRowCancel(RowEditEvent event) {
		FacesContext.getCurrentInstance().addMessage(null, new FacesMessage("Canceled", "ZZ"));
	}

	public String hierarchySearch() {
		if (searchDirection == 0)
			handleSearchDirection();
		//need to set this as default. if its a pp search the correct branch will toggle it on its own.
		this.showPrimaryPathOnly = false;
		appliedSearchDirection = searchDirection;
		
		IARelationsTreeHelper relationsTreeHelper = new IARelationsTreeHelper(
                null, null, null, null,
                cmqBaseTargetService, smqBaseTargetService, meddraDictTargetService, cmqRelationTargetService);
		
		SMQLevelHelper smqLevelH = SMQLevelHelper.getByLabel(myFilterLevel);
		MeddraDictLevelHelper meddraLevelH = MeddraDictLevelHelper.getByLabel(myFilterLevel);

		if (smqLevelH != null) {
 			if (appliedSearchDirection == SEARCH_DIRECTION_DOWN) {
 				List<SmqBaseTarget> smqBaseList = this.smqBaseTargetService.findByLevelAndTerm(
 						smqLevelH.getLevel(), myFilterTermName);
 				LOG.info("smqBaseList values {}", smqBaseList == null ? 0 : smqBaseList.size());

 				this.myHierarchyRoot = new DefaultTreeNode("root", new HierarchyNode(
 						"LEVEL", "NAME", "CODE", null), null);
 				for (SmqBaseTarget smqBaseTarget : smqBaseList) {
 					this.updateHierarchySearchForSmqTaget(smqBaseTarget);
 				}
			}
 			else if (appliedSearchDirection == SEARCH_DIRECTION_UP) {
				List<SMQReverseHierarchySearchDto> smqBaseList = smqBaseTargetService
						.findReverseByLevelAndTerm(smqLevelH.getLevel(), myFilterTermName);
 				LOG.info("smqBaseList values {}", smqBaseList == null ? 0 : smqBaseList.size());

 				this.myHierarchyRoot = new DefaultTreeNode("root", new HierarchyNode(
 						"LEVEL", "NAME", "CODE", null), null);
 				Map<Long, TreeNode> smqTreeNodeMap = new HashMap<>();
 				List<Long> smqChildCodeList = new ArrayList<>();
 				for (SMQReverseHierarchySearchDto smqBaseTarget : smqBaseList) {
 					IARelationsTreeHelper iaTreeHelper = new IARelationsTreeHelper(
 			                null, null, null, null,
 			                cmqBaseTargetService, smqBaseTargetService, meddraDictTargetService, cmqRelationTargetService);
 					HierarchyNode node = iaTreeHelper.createSmqBaseTargetReverseNode(smqBaseTarget);
 					TreeNode smqBaseTreeNode = new DefaultTreeNode(node, this.myHierarchyRoot);
 					smqChildCodeList.add(smqBaseTarget.getSmqCode());
					smqTreeNodeMap.put(smqBaseTarget.getSmqCode(), smqBaseTreeNode);
					
 				}
 					
				boolean dummyNodeAdded = false;
				List<Map<String, Object>> smqBaseChildrenCount = this.smqBaseTargetService
						.findParentCountSmqCountByChildSmqCodes(smqChildCodeList);
				if ((null != smqBaseChildrenCount)
						&& (smqBaseChildrenCount.size() > 0)) {
					for (Map<String, Object> map : smqBaseChildrenCount) {
						if (map.get("PT_CODE") != null) {
							Long childSmqCode = (Long) map
									.get("PT_CODE");
							if ((Long) map.get("COUNT") > 0) {
								HierarchyNode dummyNode = new HierarchyNode(null, null, null, null);
								dummyNode.setDummyNode(true);
								new DefaultTreeNode(dummyNode, smqTreeNodeMap.get(childSmqCode));
								dummyNodeAdded = true;
							}
						}
					}
				}
 			}
		} else if (meddraLevelH != null && appliedSearchDirection == SEARCH_DIRECTION_DOWN) {
			List<MeddraDictHierarchySearchDto> meddraDictDtoList = this.meddraDictTargetService
					.findByLevelAndTerm(meddraLevelH.getTermPrefix(), myFilterTermName);
			this.myHierarchyRoot = new DefaultTreeNode("root", new HierarchyNode(
					"LEVEL", "NAME", "CODE", null), null);
			
			String childSearchColumnTypePrefix = null;
			String parentCodeColumnPrefix = myFilterLevel + "_";
			if ("SOC".equalsIgnoreCase(myFilterLevel)) {
				childSearchColumnTypePrefix = "HLGT_";
			} else if ("HLGT".equalsIgnoreCase(myFilterLevel)) {
				childSearchColumnTypePrefix = "HLT_";
			} else if ("HLT".equalsIgnoreCase(myFilterLevel)) {
				childSearchColumnTypePrefix = "PT_";
			} else if ("PT".equalsIgnoreCase(myFilterLevel)) {
				childSearchColumnTypePrefix = "LLT_";
			}
			
			for (MeddraDictHierarchySearchDto meddraDictDto : meddraDictDtoList) {
				this.updateHierarchySearchForMeddraDict(meddraDictDto, childSearchColumnTypePrefix, parentCodeColumnPrefix);
			}
		} else if (meddraLevelH != null && appliedSearchDirection == SEARCH_DIRECTION_UP) {
			if(this.showPrimaryPath && (myFilterLevel.equals("PT") || myFilterLevel.equals("LLT"))) {
				this.showPrimaryPathOnly = true;
				this.myHierarchyRoot = new DefaultTreeNode("root", new HierarchyNode("LEVEL", "NAME", "CODE", null), null);
				boolean isPtSearch = myFilterLevel.equals("PT");
				List<MeddraDictReverseHierarchySearchDto> meddraDictDtoList = this.meddraDictTargetService
																					.findPtOrLltPrimaryPathsByTerm(myFilterTermName, isPtSearch);
				List<Long> processedCodes = new ArrayList<>();
				for (MeddraDictReverseHierarchySearchDto meddraDictReverseDto : meddraDictDtoList) {
					Long ptCode = Long.valueOf(meddraDictReverseDto.getPtCode());
					if(!processedCodes.contains(ptCode)) {
						processedCodes.add(ptCode);
						
						TreeNode ptTreeNode = null;
						if(isPtSearch) {
							HierarchyNode ptNode = relationsTreeHelper.createMeddraReverseNode(meddraDictReverseDto, "PT", false);
							ptTreeNode = new DefaultTreeNode(ptNode, this.myHierarchyRoot);
						} else {
							HierarchyNode lltNode = relationsTreeHelper.createMeddraReverseNode(meddraDictReverseDto, "LLT", false);
							TreeNode lltTreeNode = new DefaultTreeNode(lltNode, this.myHierarchyRoot);
							HierarchyNode ptNode = relationsTreeHelper.createMeddraReverseNode(meddraDictReverseDto, "PT", false);
							ptTreeNode = new DefaultTreeNode(ptNode, lltTreeNode);
						}
						
						HierarchyNode hltNode = relationsTreeHelper.createMeddraReverseNode(meddraDictReverseDto, "HLT", false);
						TreeNode hltTreeNode = new DefaultTreeNode(hltNode, ptTreeNode);
						
						HierarchyNode hlgtNode = relationsTreeHelper.createMeddraReverseNode(meddraDictReverseDto, "HLGT", false);
						TreeNode hlgtTreeNode = new DefaultTreeNode(hlgtNode, hltTreeNode);
						
						HierarchyNode socNode = relationsTreeHelper.createMeddraReverseNode(meddraDictReverseDto, "SOC", false);
						TreeNode socTreeNode = new DefaultTreeNode(socNode, hlgtTreeNode);
					}
				}
			} else {
				List<MeddraDictReverseHierarchySearchDto> meddraDictDtoList = this.meddraDictTargetService
						.findFullReverseHierarchyByLevelAndTerm(myFilterLevel, myFilterLevel, myFilterTermName);
				this.myHierarchyRoot = new DefaultTreeNode("root", new HierarchyNode("LEVEL", "NAME", "CODE", null), null);
				
				for (MeddraDictReverseHierarchySearchDto meddraDictReverseDto : meddraDictDtoList) {
					HierarchyNode node = relationsTreeHelper.createMeddraReverseNode(meddraDictReverseDto, myFilterLevel, false);
					TreeNode parentTreeNode = new DefaultTreeNode(node, this.myHierarchyRoot);
					
					// add a dummmy node to show expand arrow
					HierarchyNode dummyNode = new HierarchyNode(null, null,
							null, null);
					dummyNode.setDummyNode(true);
					new DefaultTreeNode(dummyNode, parentTreeNode);
				}
			}
		} else if ("PRO".equalsIgnoreCase(myFilterLevel)) {
			List<CmqBaseTarget> cmqBaseList = this.cmqBaseTargetService.findByLevelAndTerm(2,
					myFilterTermName);
			this.myHierarchyRoot = new DefaultTreeNode("root", new HierarchyNode(
					"LEVEL", "NAME", "CODE", null), null);
			
			List<Long> parentCmqCodeList = new ArrayList<>();
			Map<Long, TreeNode> parentTreeNodes = new HashMap<Long, TreeNode>();
			
			this.updateParentCodesAndParentTreeNodesForCmqTaget(cmqBaseList, parentCmqCodeList, parentTreeNodes);
			
			if(CollectionUtils.isNotEmpty(parentCmqCodeList)) {
				this.updateHierarchySearchCmqChildNodes(parentCmqCodeList, parentTreeNodes);
				this.updateHierarchySearchCmqRelationChildNodes(parentCmqCodeList, parentTreeNodes);
			}
		} else if ("NC-LLT".equalsIgnoreCase(myFilterLevel)) {
			String filterLevel = myFilterLevel.substring(3);
			List<MeddraDictReverseHierarchySearchDto> meddraDictDtoList = this.meddraDictTargetService
					.findFullReverseHierarchyByLevelAndTerm(filterLevel, filterLevel, myFilterTermName, true);
			this.myHierarchyRoot = new DefaultTreeNode("root", new HierarchyNode("LEVEL", "NAME", "CODE", null), null);
			
			for (MeddraDictReverseHierarchySearchDto meddraDictReverseDto : meddraDictDtoList) {
				HierarchyNode node = relationsTreeHelper.createMeddraReverseNode(meddraDictReverseDto, filterLevel, false);
				TreeNode parentTreeNode = new DefaultTreeNode(node, this.myHierarchyRoot);
				
				// add a dummmy node to show expand arrow
				HierarchyNode dummyNode = new HierarchyNode(null, null, null, null);
				dummyNode.setDummyNode(true);
				new DefaultTreeNode(dummyNode, parentTreeNode);
			}
		}
		setEnableRadioButtons(false); 

		return "";
	}
	
	private void handleSearchDirection() {
		if (myFilterLevel != null
				&& (myFilterLevel.equals("SOC") || myFilterLevel.equals("SMQ1")
						|| myFilterLevel.equals("SMQ2")
						|| myFilterLevel.equals("SMQ3")
						|| myFilterLevel.equals("SMQ4")
						|| myFilterLevel.equals("SMQ ALL")
 						|| myFilterLevel.equals("PRO")
						|| myFilterLevel.equals("HLGT") || myFilterLevel
							.equals("HLT"))) {
			searchDirection = SEARCH_DIRECTION_DOWN;
		}
		if (myFilterLevel != null && (myFilterLevel.equals("LLT")
				|| myFilterLevel.equals("PT")) || myFilterLevel.equals("SMQ5")) {
			searchDirection = SEARCH_DIRECTION_UP;
		}
	}
	
	public void selectLevelEvent(AjaxBehaviorEvent event) {
		handleSearchDirection();
	}
	
	public void resetHS(AjaxBehaviorEvent event) {
		this.myHierarchyRoot = new DefaultTreeNode("root", new HierarchyNode("LEVEL", "NAME", "CODE", null), null);	 
		
		setFilterLevel("PT");
		myFilterTermName = "";
 
		searchDirection = SEARCH_DIRECTION_UP;
		handleSearchDirection();
		setSearchDownDisabled(true);
		setSearchUpDisabled(true); 
  	}
	
	/**
	 * Refresh HS after radio button selection.
	 * @param event AjaxBehaviorEvent
	 */
	public void refreshTHS(AjaxBehaviorEvent event) {
		 hierarchySearch();
	}
	
	/**
	 * Collapse tree.
	 */
	public void collapseForPrimaryPathShowing(AjaxBehaviorEvent event) {
		//collapsingORexpanding(myHierarchyRoot, false);
		//hierarchySearch();
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

	//uiEventSourceName is either relations or hierarchy
	public void onNodeExpand(NodeExpandEvent event) {
		if(!this.showPrimaryPathOnly) {
			IARelationsTreeHelper relationsSearchHelper = new IARelationsTreeHelper(
	                null, null, null, null,
	                cmqBaseTargetService, smqBaseTargetService, meddraDictTargetService, cmqRelationTargetService);	
			relationsSearchHelper.onNodeExpandTargetTable(this.myHierarchyRoot, event, false);
		}
	}

	/**
	 * Reset Search form/results of hierarchy select dialog
	 */
	public void resetForm() {
		this.myHierarchyRoot = new DefaultTreeNode("root", new HierarchyNode(
				"LEVEL", "NAME", "CODE", null), null);
		this.myFilterLevel = null;
		this.myFilterTermName = "";
	}
	
	public void onCloseHierarchySearch(CloseEvent event) {
		resetForm();
	}

	public String getFilterLevel() {
		return myFilterLevel;
	}

	public void setFilterLevel(String levelH) {
		this.myFilterLevel = levelH;
	}

	public String getFilterTermName() {
		return myFilterTermName;
	}

	public void setFilterTermName(String filterTermName) {
		this.myFilterTermName = filterTermName;
	}

	public TreeNode getHierarchyRoot() {
		return myHierarchyRoot;
	}
    
	public void setHierarchyRoot(TreeNode hierarchyRoot) {
		this.myHierarchyRoot = hierarchyRoot;
	}
    
    public ICmqBaseTargetService getCmqBaseTargetService() {
		return cmqBaseTargetService;
	}

	public void setCmqBaseTargetService(ICmqBaseTargetService cmqBaseTargetService) {
		this.cmqBaseTargetService = cmqBaseTargetService;
	}

	public void setCmqRelationTargetService(ICmqRelationTargetService cmqRelationTargetService) {
		this.cmqRelationTargetService = cmqRelationTargetService;
	}

	public List<HierarchySearchResultBean> getHierarchySearchResults() {
		return hierarchySearchResults;
	}

	public void setHierarchySearchResults(
			List<HierarchySearchResultBean> hierarchySearchResults) {
		this.hierarchySearchResults = hierarchySearchResults;
	}

	public void setSmqBaseTargetService(ISmqBaseTargetService smqBaseTargetService) {
		this.smqBaseTargetService = smqBaseTargetService;
	}

	public void setMeddraDictTargetService(IMeddraDictTargetService meddraDictTargetService) {
		this.meddraDictTargetService = meddraDictTargetService;
	}
	
	public IRelationsChangeListener getRelationsChangeListener() {
		return onDropRelationsListener;
	}

	public void setRelationsChangeListener(IRelationsChangeListener onDropRelationsListener) {
		this.onDropRelationsListener = onDropRelationsListener;
	}


	public TreeNode[] getSelectedNodes() {
		return mySelectedNodes;
	}

	public void setSelectedNodes(TreeNode[] mySelectedNodes) {
		this.mySelectedNodes = mySelectedNodes;
	}


	public interface IRelationsChangeListener {
		public void onDropRelations();
		public void addSelectedRelations(TreeNode[] nodes);
	}
    
	
    private void updateHierarchySearchForSmqTaget(SmqBaseTarget smqBaseTarget) {
        IARelationsTreeHelper iaTreeHelper = new IARelationsTreeHelper(
                null, null, null, null,
                cmqBaseTargetService, smqBaseTargetService, meddraDictTargetService, cmqRelationTargetService);
		HierarchyNode node = iaTreeHelper.createSmqBaseTargetNode(smqBaseTarget, null);
		TreeNode smqBaseTreeNode = new DefaultTreeNode(node, this.myHierarchyRoot);
		
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
    
    /*private void updateHierarchySearchForSmqTargetReverse(SMQReverseHierarchySearchDto smqBaseTargetReverse) {
        IARelationsTreeHelper iaTreeHelper = new IARelationsTreeHelper(
                null, null, null, null,
                cmqBaseTargetService, smqBaseTargetService, meddraDictTargetService, cmqRelationTargetService);
		HierarchyNode node = iaTreeHelper.createSmqBaseTargetReverseNode(smqBaseTargetReverse);
		TreeNode smqBaseTreeNode = new DefaultTreeNode(node, this.myHierarchyRoot);
		
		boolean dummyNodeAdded = false;
		Long count = this.smqBaseTargetService.findChildSmqCountByParentSmqCode(Long.parseLong(smqBaseTargetReverse.getSmqParentCode()));
		if((count != null) && (count > 0)) {
			HierarchyNode dummyNode = new HierarchyNode(null, null, null, null);
			dummyNode.setDummyNode(true);
			new DefaultTreeNode(dummyNode, smqBaseTreeNode);
			dummyNodeAdded = true;

		}

	}*/
	
    
	private void updateParentCodesAndParentTreeNodesForCmqTaget(List<CmqBaseTarget> cmqBaseList
																	, List<Long> parentCmqCodeList
																	, Map<Long, TreeNode> parentTreeNodes) {
		for (CmqBaseTarget cmqBase : cmqBaseList) {
			HierarchyNode node = new HierarchyNode();
			node.setLevel(myFilterLevel);
			node.setTerm(cmqBase.getCmqName());
			node.setCode(cmqBase.getCmqCode().toString());
			node.setEntity(cmqBase);

			TreeNode cmqBaseTreeNode = new DefaultTreeNode(node, myHierarchyRoot);
			parentTreeNodes.put(cmqBase.getCmqCode(), cmqBaseTreeNode);
			parentCmqCodeList.add(cmqBase.getCmqCode());
		}
	}
	
    @SuppressWarnings({ "unchecked", "rawtypes" })
	private void updateHierarchySearchCmqChildNodes(List<Long> parentCmqCodeList, Map<Long, TreeNode> parentTreeNodes) {
		List<Map<String, Object>> childCountsList = this.cmqBaseTargetService.findCmqChildCountForParentCmqCodes(parentCmqCodeList);
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

    
	private void updateHierarchySearchForMeddraDict(MeddraDictHierarchySearchDto meddraDictDto
														, String childSearchColumnTypePrefix
														, String parentCodeColumnPrefix) {
        IARelationsTreeHelper relationsTreeHelper = new IARelationsTreeHelper(cmqBaseTargetService, smqBaseTargetService, meddraDictTargetService, cmqRelationTargetService);
		HierarchyNode node = relationsTreeHelper.createMeddraNode(meddraDictDto, myFilterLevel);
		TreeNode parentTreeNode = new DefaultTreeNode(node, this.myHierarchyRoot);
		
		Long countOfChildren = this.meddraDictTargetService.findChildrenCountByParentCode(childSearchColumnTypePrefix,
				parentCodeColumnPrefix, Long.valueOf(meddraDictDto.getCode()));
		
		if((null != countOfChildren) && (countOfChildren > 0)) {
			// add a dummmy node to show expand arrow
			HierarchyNode dummyNode = new HierarchyNode(null, null,
					null, null);
			dummyNode.setDummyNode(true);
			new DefaultTreeNode(dummyNode, parentTreeNode);
		}

	}

	public boolean isNonCurrentLlt() {
		return nonCurrentLlt;
	}

	public void setNonCurrentLlt(boolean nonCurrentLlt) {
		this.nonCurrentLlt = nonCurrentLlt;
	}

	public boolean isShowPrimaryPath() {
		return showPrimaryPath;
	}

	public void setShowPrimaryPath(boolean showPrimaryPath) {
		this.showPrimaryPath = showPrimaryPath;
	}
	
	public int getSearchDirection() {
		return searchDirection;
	}

	public void setSearchDirection(int searchDirection) {
		this.searchDirection = searchDirection;
	}

	 public boolean isSearchUpDisabled() {
//       SMQLevelHelper smqLevelH = SMQLevelHelper.getByLabel(myFilterLevel);
//       if(smqLevelH != null || "PRO".equalsIgnoreCase(myFilterLevel) || "SOC".equalsIgnoreCase(myFilterLevel)) {
//           searchDirection = SEARCH_DIRECTION_DOWN;
//           return true;
//       }
//       return false;
   	
		if (myFilterLevel != null
				&& (myFilterLevel.equals("LLT") || myFilterLevel.equals("PT") || myFilterLevel
						.equals("SMQ5"))) {
			searchDirection = SEARCH_DIRECTION_UP;
			if (myFilterLevel.equals("LLT")) {
				return true;
			}
		}
		if (myFilterLevel != null
				&& (myFilterLevel.equals("SOC") || myFilterLevel.equals("SMQ1")
						|| myFilterLevel.equals("SMQ2")
						|| myFilterLevel.equals("SMQ3")
						|| myFilterLevel.equals("SMQ4")
						|| myFilterLevel.equals("PRO")
						|| myFilterLevel.equals("HLGT") || myFilterLevel
							.equals("HLT"))) {
			searchDirection = SEARCH_DIRECTION_DOWN;
			if (myFilterLevel.equals("HLGT") || myFilterLevel.equals("HLT")
					|| myFilterLevel.equals("SMQ1")
					|| myFilterLevel.equals("SMQ2")
					|| myFilterLevel.equals("SMQ3")
					|| myFilterLevel.equals("SMQ4"))
				return false;
			return true;

		}
		return false;
   }
   public boolean isSearchDownDisabled() {
//		MeddraDictLevelHelper meddraLevelH = MeddraDictLevelHelper.getByLabel(myFilterLevel);
//       
//       if(meddraLevelH != null && meddraLevelH.getSearchFrom() == MeddraDictLevelHelper.SEARCH_MEDDRA_BASE_REVERSE){
//           searchDirection = SEARCH_DIRECTION_UP;
//           return true;
//       }
//       return false;
   	
	   if (myFilterLevel != null
				&& (myFilterLevel.equals("SOC") 
						|| myFilterLevel.equals("SMQ1")
						|| myFilterLevel.equals("SMQ2")
						|| myFilterLevel.equals("SMQ3")
						|| myFilterLevel.equals("SMQ4")
						|| myFilterLevel.equals("PRO")
						|| myFilterLevel.equals("HLGT") || myFilterLevel
							.equals("HLT"))) {
			searchDirection = SEARCH_DIRECTION_DOWN;
			if (myFilterLevel.equals("HLGT") || myFilterLevel.equals("HLT")
					|| myFilterLevel.equals("SMQ1")
					|| myFilterLevel.equals("SMQ2")
					|| myFilterLevel.equals("SMQ3")
					|| myFilterLevel.equals("SMQ4"))
				return false;
			return true;

		}
		if (myFilterLevel != null
				&& (myFilterLevel.equals("LLT") || myFilterLevel.equals("PT") || myFilterLevel.equals("SMQ5"))) {
			searchDirection = SEARCH_DIRECTION_UP;
			if (myFilterLevel.equals("LLT")) {
				return true;
			}

		}
    	return false;
   }
   
   public void setSearchUpDisabled(boolean searchUpDisabled) {
		this.searchUpDisabled = searchUpDisabled;
	}

	public void setSearchDownDisabled(boolean searchDownDisabled) {
		this.searchDownDisabled = searchDownDisabled;
	}

	public boolean isEnableRadioButtons() {
		return enableRadioButtons;
	}

	public void setEnableRadioButtons(boolean enableRadioButtons) {
		this.enableRadioButtons = enableRadioButtons;
	}
}
