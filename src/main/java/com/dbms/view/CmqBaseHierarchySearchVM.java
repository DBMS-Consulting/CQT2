package com.dbms.view;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.event.AjaxBehaviorEvent;

import org.primefaces.event.CloseEvent;
import org.primefaces.event.NodeExpandEvent;
import org.primefaces.event.RowEditEvent;
import org.primefaces.model.DefaultTreeNode;
import org.primefaces.model.TreeNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dbms.controller.GlobalController;
import com.dbms.controller.beans.HierarchySearchResultBean;
import com.dbms.csmq.HierarchyNode;
import com.dbms.entity.cqt.CmqBase190;
import com.dbms.entity.cqt.RefConfigCodeList;
import com.dbms.entity.cqt.SmqBase190;
import com.dbms.entity.cqt.dtos.MeddraDictHierarchySearchDto;
import com.dbms.entity.cqt.dtos.MeddraDictReverseHierarchySearchDto;
import com.dbms.entity.cqt.dtos.SMQReverseHierarchySearchDto;
import com.dbms.service.ICmqBase190Service;
import com.dbms.service.ICmqRelation190Service;
import com.dbms.service.IMeddraDictService;
import com.dbms.service.ISmqBaseService;
import com.dbms.util.MeddraDictLevelHelper;
import com.dbms.util.SMQLevelHelper;

/**
 * @date Feb 7, 2017 7:39:34 AM
 **/
public class CmqBaseHierarchySearchVM {
    public static int SEARCH_DIRECTION_UP = 1;
    public static int SEARCH_DIRECTION_DOWN = 2;

	private static final Logger log = LoggerFactory
			.getLogger(CmqBaseHierarchySearchVM.class);

	private ICmqBase190Service cmqBaseService;
	private ISmqBaseService smqBaseService;
	private IMeddraDictService meddraDictService;
	private ICmqRelation190Service cmqRelationService;
	final private GlobalController globalController;
	
	private String myFilterTermName;
	private String myFilterLevel;
	private String filterSMQ;


	private TreeNode myHierarchyRoot;
	private TreeNode[] mySelectedNodes;

	private List<HierarchySearchResultBean> hierarchySearchResults;
	
	private IRelationsChangeListener onDropRelationsListener;
	
	private boolean showPrimaryPath;
	
	private boolean showPrimaryPathOnly;
    
    private int searchDirection;
    private int appliedSearchDirection;
    private boolean searchUpDisabled;
    private boolean searchDownDisabled;
    
    private boolean enableRadioButtons;
    
    private List<RefConfigCodeList> levels;
    
 	
    public CmqBaseHierarchySearchVM(ICmqBase190Service cmqBaseSvc,
			ISmqBaseService smqBaseSvc,
			IMeddraDictService meddraDictSvc,
			ICmqRelation190Service cmqRelationSvc,
			GlobalController globalController) {
		this.cmqBaseService = cmqBaseSvc;
		this.smqBaseService = smqBaseSvc;
		this.meddraDictService = meddraDictSvc;
		this.cmqRelationService = cmqRelationSvc;
		this.globalController = globalController;
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
		
        //trim any spaces
        myFilterTermName = myFilterTermName.trim();
        
		CmqBaseRelationsTreeHelper relationsTreeHelper = new CmqBaseRelationsTreeHelper(cmqBaseService, smqBaseService, meddraDictService, cmqRelationService, globalController);
        relationsTreeHelper.setRequireDrillDown(true);
		
		SMQLevelHelper smqLevelH = SMQLevelHelper.getByLabel(myFilterLevel);
		MeddraDictLevelHelper meddraLevelH = MeddraDictLevelHelper.getByLabel(myFilterLevel);

		if (smqLevelH != null) {
			Map<Long, TreeNode> smqTreeNodeMap = new HashMap<>();
			List<Long> smqChildCodeList = new ArrayList<>();
			if (appliedSearchDirection == SEARCH_DIRECTION_DOWN) {
				List<SmqBase190> smqBaseList = smqBaseService
						.findByLevelAndTerm(smqLevelH.getLevel(),
								myFilterTermName);

				myHierarchyRoot = new DefaultTreeNode("root",
						new HierarchyNode("LEVEL", "NAME", "CODE", null), null);

				// Map<Long, TreeNode> smqTreeNodeMap = new HashMap<>();
				// List<Long> smqChildCodeList = new ArrayList<>();

				for (SmqBase190 smqBase : smqBaseList) {
					HierarchyNode node = relationsTreeHelper.createSmqBaseNode(
							smqBase, null);
					TreeNode parentTreeNode = new DefaultTreeNode(node,
							myHierarchyRoot);

					smqChildCodeList.add(smqBase.getSmqCode());
					smqTreeNodeMap.put(smqBase.getSmqCode(), parentTreeNode);
				}

				List<Map<String, Object>> smqBaseChildrenCount = this.smqBaseService
						.findChildSmqCountByParentSmqCodes(smqChildCodeList);
				if ((null != smqBaseChildrenCount)
						&& (smqBaseChildrenCount.size() > 0)) {
					for (Map<String, Object> map : smqBaseChildrenCount) {
						if (map.get("SMQ_PARENT_CODE") != null) {
							Long childSmqCode = (Long) map
									.get("SMQ_PARENT_CODE");
							if ((Long) map.get("COUNT") > 0) {
								relationsTreeHelper
										.createNewDummyNode(smqTreeNodeMap
												.get(childSmqCode));
								smqChildCodeList.remove(childSmqCode);
								smqTreeNodeMap.remove(childSmqCode);
							}
						}
					}
				}

				List<Map<String, Object>> smqRelationsCountList = this.smqBaseService
						.findSmqRelationsCountForSmqCodes(smqChildCodeList);
				if ((null != smqRelationsCountList)
						&& (smqRelationsCountList.size() > 0)) {
					for (Map<String, Object> map : smqRelationsCountList) {
						if (map.get("SMQ_CODE") != null) {
							Long childSmqCode = (Long) map.get("SMQ_CODE");
							if ((Long) map.get("COUNT") > 0) {
								relationsTreeHelper
										.createNewDummyNode(smqTreeNodeMap
												.get(childSmqCode));
							}
						}
					}
				}
				
				//find child smqs for this one and add a C in fornt of name if it has
				smqRelationsCountList = this.smqBaseService
						.findSmqChildRelationsCountForSmqCodes(smqChildCodeList);
				if ((null != smqRelationsCountList)
						&& (smqRelationsCountList.size() > 0)) {
					for (Map<String, Object> map : smqRelationsCountList) {
						if (map.get("SMQ_CODE") != null) {
							Long childSmqCode = (Long) map.get("SMQ_CODE");
							if ((Long) map.get("COUNT") > 0) {
								TreeNode treeNode = smqTreeNodeMap.get(childSmqCode);
								HierarchyNode hierarchyNode = (HierarchyNode) treeNode.getData();
								String level = hierarchyNode.getLevel();
								hierarchyNode.setLevel("'C' " + level);
								//add a dummy node only if its not present yet
								if(treeNode.getChildCount() == 0) {
									relationsTreeHelper.createNewDummyNode(treeNode);
								}
							}
						}
					}
				}
				
			} else if (appliedSearchDirection == SEARCH_DIRECTION_UP) {
				Integer smqLevel = Integer.parseInt(returnSmqLevel(myFilterLevel));
				List<SMQReverseHierarchySearchDto> smqBaseList = smqBaseService
						.findReverseByLevelAndTerm(smqLevel, myFilterTermName);

				myHierarchyRoot = new DefaultTreeNode("root",
						new HierarchyNode("LEVEL", "NAME", "CODE", null), null);

				for (SMQReverseHierarchySearchDto smqBase : smqBaseList) {
					HierarchyNode node = relationsTreeHelper
							.createSmqBaseReverseNode(smqBase);
					TreeNode parentTreeNode = new DefaultTreeNode(node,
							myHierarchyRoot);

					smqChildCodeList.add(smqBase.getSmqCode());
					smqTreeNodeMap.put(smqBase.getSmqCode(), parentTreeNode);
				}

				List<Map<String, Object>> smqBaseChildrenCount = this.smqBaseService
						.findParentCountSmqCountByChildSmqCodes(smqChildCodeList);

				if ((null != smqBaseChildrenCount)
						&& (smqBaseChildrenCount.size() > 0)) {
					for (Map<String, Object> map : smqBaseChildrenCount) {
						if (map.get("PT_CODE") != null) {
							Long childSmqCode = (Long) map
									.get("PT_CODE");
							if ((Long) map.get("COUNT") > 0) {
								relationsTreeHelper
										.createNewDummyNode(smqTreeNodeMap
												.get(childSmqCode));
								smqChildCodeList.remove(childSmqCode);
								smqTreeNodeMap.remove(childSmqCode);
							}
						}
					}
				}
			}

		} else if (meddraLevelH != null && appliedSearchDirection == SEARCH_DIRECTION_DOWN) {
			List<MeddraDictHierarchySearchDto> meddraDictDtoList = meddraDictService
					.findByLevelAndTerm(meddraLevelH.getTermPrefix(), myFilterTermName);
			this.myHierarchyRoot = new DefaultTreeNode("root", new HierarchyNode(
					"LEVEL", "NAME", "CODE", null), null);

			String childSearchColumnTypePrefix = meddraLevelH.getTermPrefix();
			String parentCodeColumnPrefix = myFilterLevel + "_";
			
            List<Long> meddraCodeList = new ArrayList<>();
			Map<Long, TreeNode> meddraTreeNodes = new HashMap<Long, TreeNode>();
            
			for (MeddraDictHierarchySearchDto m : meddraDictDtoList) {
				HierarchyNode node = relationsTreeHelper.createMeddraNode(m, myFilterLevel, null);
				TreeNode parentTreeNode = new DefaultTreeNode(node, this.myHierarchyRoot);
				
                meddraCodeList.add(Long.valueOf(m.getCode()));
                meddraTreeNodes.put(Long.valueOf(m.getCode()), parentTreeNode);
            }
            
            List<Map<String, Object>> countOfChildrenList = this.meddraDictService.findChildrenCountByParentCodes(childSearchColumnTypePrefix, parentCodeColumnPrefix, meddraCodeList);
            if((null != countOfChildrenList) && (countOfChildrenList.size() > 0)) {
                for(Map<String, Object> map : countOfChildrenList) {
                    if(map.get("PARENT_CODE") != null) {
                        Long parentMeddraCode = (Long)map.get("PARENT_CODE");
                        if((Long)map.get("COUNT") > 0) {
                            relationsTreeHelper.createNewDummyNode(meddraTreeNodes.get(parentMeddraCode));
                        }
                    }
                }
            }
		} else if (meddraLevelH != null && appliedSearchDirection == SEARCH_DIRECTION_UP) {
			if(this.showPrimaryPath && (myFilterLevel.equals("PT") || myFilterLevel.equals("LLT"))) {
				this.showPrimaryPathOnly = true;
				this.myHierarchyRoot = new DefaultTreeNode("root", new HierarchyNode("LEVEL", "NAME", "CODE", null), null);
				boolean isPtSearch = myFilterLevel.equals("PT");
				List<MeddraDictReverseHierarchySearchDto> meddraDictDtoList = meddraDictService
																				.findPtOrLltPrimaryPathsByTerm(myFilterTermName, isPtSearch);
				List<Long> processedCodes = new ArrayList<>();
				for (MeddraDictReverseHierarchySearchDto meddraDictReverseDto : meddraDictDtoList) {
					Long ptCode = Long.valueOf(meddraDictReverseDto.getPtCode());
					if(!processedCodes.contains(ptCode)) {
						processedCodes.add(ptCode);
						
						TreeNode ptTreeNode = null;
						if(isPtSearch) {
							HierarchyNode ptNode = relationsTreeHelper.createMeddraReverseNode(meddraDictReverseDto, "PT", false, null);
							ptTreeNode = new DefaultTreeNode(ptNode, this.myHierarchyRoot);
						} else {
							HierarchyNode lltNode = relationsTreeHelper.createMeddraReverseNode(meddraDictReverseDto, "LLT", false, null);
							TreeNode lltTreeNode = new DefaultTreeNode(lltNode, this.myHierarchyRoot);
							HierarchyNode ptNode = relationsTreeHelper.createMeddraReverseNode(meddraDictReverseDto, "PT", false, null);
							ptTreeNode = new DefaultTreeNode(ptNode, lltTreeNode);
						}
						
						HierarchyNode hltNode = relationsTreeHelper.createMeddraReverseNode(meddraDictReverseDto, "HLT", false, null);
						TreeNode hltTreeNode = new DefaultTreeNode(hltNode, ptTreeNode);
						
						HierarchyNode hlgtNode = relationsTreeHelper.createMeddraReverseNode(meddraDictReverseDto, "HLGT", false, null);
						TreeNode hlgtTreeNode = new DefaultTreeNode(hlgtNode, hltTreeNode);
						
						HierarchyNode socNode = relationsTreeHelper.createMeddraReverseNode(meddraDictReverseDto, "SOC", false, null);
						TreeNode socTreeNode = new DefaultTreeNode(socNode, hlgtTreeNode);
					}
				}
			} else {
				List<MeddraDictReverseHierarchySearchDto> meddraDictDtoList = meddraDictService
						.findFullReverseHierarchyByLevelAndTerm(myFilterLevel, myFilterLevel, myFilterTermName);
				this.myHierarchyRoot = new DefaultTreeNode("root", new HierarchyNode("LEVEL", "NAME", "CODE", null), null);
				boolean filterLltFlag = this.globalController.isFilterLltsFlag();
				for (MeddraDictReverseHierarchySearchDto meddraDictReverseDto : meddraDictDtoList) {
					HierarchyNode node = relationsTreeHelper.createMeddraReverseNode(meddraDictReverseDto, myFilterLevel, false, null);
					TreeNode parentTreeNode = new DefaultTreeNode(node, this.myHierarchyRoot);
					
					//if filter llt flag is on and we are seaching for PTs
					if(!node.getLevel().equals("PT") && filterLltFlag) {
						// add a dummmy node to show expand arrow
						relationsTreeHelper.createNewDummyNode(parentTreeNode);
					}
				}
			}
		} else if ("PRO".equalsIgnoreCase(myFilterLevel)) {
			List<CmqBase190> cmqBaseList = cmqBaseService.findByLevelAndTerm(2, myFilterTermName);
			this.myHierarchyRoot = new DefaultTreeNode("root", new HierarchyNode(
					"LEVEL", "NAME", "CODE", null), null);
			
			List<Long> parentCmqCodeList = new ArrayList<>();
			Map<Long, TreeNode> parentTreeNodes = new HashMap<Long, TreeNode>();
			
			for (CmqBase190 cmqBase190 : cmqBaseList) {
				HierarchyNode node = new HierarchyNode();
				node.setLevel(myFilterLevel);
				node.setTerm(cmqBase190.getCmqName());
				node.setCode(cmqBase190.getCmqCode().toString());
				node.setEntity(cmqBase190);

				TreeNode cmqBaseTreeNode = new DefaultTreeNode(node, myHierarchyRoot);
				parentTreeNodes.put(cmqBase190.getCmqCode(), cmqBaseTreeNode);
				parentCmqCodeList.add(cmqBase190.getCmqCode());
			}
			
            List<Map<String, Object>> childCountsList = this.cmqBaseService.findCmqChildCountForParentCmqCodes(parentCmqCodeList);
            if((null != childCountsList) && (childCountsList.size() > 0)) {
                //first find and fix child nodes stuff
                for (Map<String, Object> map : childCountsList) {
                    if(map.get("CMQ_CODE") != null) {
                        Long cmqCode = (Long)map.get("CMQ_CODE");
                        Long count = (Long)map.get("COUNT");
                        if(count > 0) {
                            //add a dummy node for this child in parent
                            TreeNode parentTreeNode = parentTreeNodes.get(cmqCode);
                            relationsTreeHelper.createNewDummyNode(parentTreeNode);

                            parentCmqCodeList.remove(cmqCode);
                            parentTreeNodes.remove(cmqCode);
                        }
                    }
                }
            }
            //now find relations for those who don't have children
            List<Map<String, Object>> relationsCountsList = this.cmqRelationService.findCountByCmqCodes(parentCmqCodeList);	
            if((null != relationsCountsList) && (relationsCountsList.size() > 0)) {
                for(Map<String, Object> map : relationsCountsList) {
                    Long cmqCode = (Long)map.get("CMQ_CODE");
                    Long count = (Long)map.get("COUNT");
                    if(count > 0) {
                        //add a dummy node for this child in parent
                        TreeNode parentTreeNode = parentTreeNodes.get(cmqCode);
                        relationsTreeHelper.createNewDummyNode(parentTreeNode);
                    }
                }
            }
		}
		setEnableRadioButtons(false); 

		return "";
	}
	
	private String returnSmqLevel(String level) { 
		if (level != null) {
			if (level.equals("SMQ ALL"))
				return "-1";
			if (level.equals("SMQ1"))
				return "1";
			if (level.equals("SMQ2"))
				return "2";
			if (level.equals("SMQ3"))
				return "3";
			if (level.equals("SMQ4"))
				return "4";
			if (level.equals("SMQ5"))
				return "5";
		}
		return "1";
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
		myHierarchyRoot = new DefaultTreeNode("root", new HierarchyNode("LEVEL", "NAME", "CODE", null), null);
		setFilterLevel("PT");
//		if (!extension.equals("PRO") && !extension.equals("CPT") && !extension.equals("DME"))
//			setFilterLevel("PRO");
//		
//		if (extension.equals("PRO") && extension.equals("CPT") && extension.equals("DME"))
//			setFilterLevel("PT");
		
		setFilterTermName("");
		 
		searchDirection = SEARCH_DIRECTION_UP;
		handleSearchDirection();
		setSearchDownDisabled(true);
		setSearchUpDisabled(true); 
   	}

	/**
	 * Refresh HS after radio button selection.
	 * @param event AjaxBehaviorEvent
	 */
	public void refreshHS(AjaxBehaviorEvent event) {
		hierarchySearch();
	}

	//uiEventSourceName is either relations or hierarchy
	public void onNodeExpand(NodeExpandEvent event) {
		//event source attriute from the ui
		String uiSourceOfEvent =  (String) event.getComponent().getAttributes().get("uiEventSourceName");
		TreeNode expandedTreeNode = event.getTreeNode();
		boolean isRelationView = "RELATIONS".equalsIgnoreCase(uiSourceOfEvent);
		boolean isParentListView = "PARENT-LIST".equalsIgnoreCase(uiSourceOfEvent);
		if(!showPrimaryPathOnly) {
			CmqBaseRelationsTreeHelper relationsSearchHelper = new CmqBaseRelationsTreeHelper(cmqBaseService, smqBaseService, meddraDictService, cmqRelationService, globalController);	
	        relationsSearchHelper.setRelationView(isRelationView);
	        relationsSearchHelper.setRelationView(isParentListView);
			this.myHierarchyRoot = relationsSearchHelper.getRelationsNodeHierarchy(this.myHierarchyRoot, expandedTreeNode);	
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
    
    public ICmqBase190Service getCmqBaseService() {
		return cmqBaseService;
	}

	public void setCmqBaseService(ICmqBase190Service cmqBaseService) {
		this.cmqBaseService = cmqBaseService;
	}

	public void setCmqRelationService(ICmqRelation190Service cmqRelationService) {
		this.cmqRelationService = cmqRelationService;
	}

	public List<HierarchySearchResultBean> getHierarchySearchResults() {
		return hierarchySearchResults;
	}

	public void setHierarchySearchResults(
			List<HierarchySearchResultBean> hierarchySearchResults) {
		this.hierarchySearchResults = hierarchySearchResults;
	}

	public void setSmqBaseService(ISmqBaseService smqBaseService) {
		this.smqBaseService = smqBaseService;
	}

	public void setMeddraDictService(IMeddraDictService meddraDictService) {
		this.meddraDictService = meddraDictService;
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
//        SMQLevelHelper smqLevelH = SMQLevelHelper.getByLabel(myFilterLevel);
//        if(smqLevelH != null || "PRO".equalsIgnoreCase(myFilterLevel) || "SOC".equalsIgnoreCase(myFilterLevel)) {
//            searchDirection = SEARCH_DIRECTION_DOWN;
//            return true;
//        }
//        return false;
    	
		if (myFilterLevel != null && (myFilterLevel.equals("LLT")
				|| myFilterLevel.equals("PT") || myFilterLevel.equals("SMQ5"))) {
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
//        if(meddraLevelH != null && meddraLevelH.getSearchFrom() == MeddraDictLevelHelper.SEARCH_MEDDRA_BASE_REVERSE){
//            searchDirection = SEARCH_DIRECTION_UP;
//            return true;
//        }
//        return false;
    	
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
    
    /**
     * Event when selecting SMQ
     * @param event
     */
    public void selectSMQEvent(AjaxBehaviorEvent event) {
    	
		/**
		 * Handle here code with SMQ value selected by user: ---> filterSMQ
		 */
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

	public List<RefConfigCodeList> getLevels() {
		return levels;
	}

	public void setLevels(List<RefConfigCodeList> levels) {
		this.levels = levels;
	}

	public String getFilterSMQ() {
		return filterSMQ;
	}

	public void setFilterSMQ(String myFilterSMQ) {
		this.filterSMQ = myFilterSMQ;
	}
 
}
