package com.dbms.view;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;

import org.apache.commons.collections4.CollectionUtils;
import org.primefaces.event.NodeExpandEvent;
import org.primefaces.model.DefaultTreeNode;
import org.primefaces.model.TreeNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dbms.controller.beans.HierarchySearchResultBean;
import com.dbms.csmq.HierarchyNode;
import com.dbms.entity.IEntity;
import com.dbms.entity.cqt.CmqBase190;
import com.dbms.entity.cqt.CmqRelation190;
import com.dbms.entity.cqt.SmqBase190;
import com.dbms.entity.cqt.SmqRelation190;
import com.dbms.entity.cqt.dtos.MeddraDictHierarchySearchDto;
import com.dbms.entity.cqt.dtos.MeddraDictReverseHierarchySearchDto;
import com.dbms.service.AuthenticationService;
import com.dbms.service.ICmqBase190Service;
import com.dbms.service.ICmqRelation190Service;
import com.dbms.service.IMeddraDictService;
import com.dbms.service.IRefCodeListService;
import com.dbms.service.ISmqBaseService;
import com.dbms.util.SWJSFRequest;
import com.dbms.util.exceptions.CqtServiceException;
import com.dbms.view.CmqBaseHierarchySearchVM.IRelationsChangeListener;

/**
 * @date Feb 7, 2017 7:39:34 AM
 **/

public class ListRelationsVM implements IRelationsChangeListener {

	private static final long serialVersionUID = 5299394344651669792L;

	private static final Logger log = LoggerFactory
			.getLogger(ListRelationsVM.class);

    private ICmqBase190Service cmqBaseService;
	private ISmqBaseService smqBaseService;
	private IMeddraDictService meddraDictService;
	private ICmqRelation190Service cmqRelationService;
	private IRefCodeListService refCodeListService;
	private AuthenticationService authService;
	
	private String[] selectedSOCs;
	private CmqBase190 selctedData;

	private List<HierarchySearchResultBean> hierarchySearchResults;

	private TreeNode[] relationSelected;
	private TreeNode[] relationSelectedInRelationsTable;
	private TreeNode relationsRoot;
	
	private TreeNode parentListRoot;
	private CmqBase190 parentCmqEntity;
	
	private Long clickedCmqCode;
	
	private CmqBaseHierarchySearchVM myHierarchyDlgModel;	
    
    private boolean displayScopeCatWeight;
    
    public ListRelationsVM(AuthenticationService authService, SWJSFRequest appSWJSFRequest,
            IRefCodeListService refCodeListService, ICmqBase190Service cmqBaseService, ISmqBaseService smqBaseService,
            IMeddraDictService meddraDictService, ICmqRelation190Service cmqRelationService) {
        this.authService = authService;
        this.refCodeListService = refCodeListService;
        this.cmqBaseService = cmqBaseService;
        this.smqBaseService = smqBaseService;
        this.meddraDictService = meddraDictService;
        this.cmqRelationService = cmqRelationService;
        
        myHierarchyDlgModel = new CmqBaseHierarchySearchVM(cmqBaseService, smqBaseService, meddraDictService, cmqRelationService);
		
		parentListRoot = new DefaultTreeNode("root"
				, new HierarchyNode("LEVEL", "NAME", "CODE", "SCOPE", "CATEGORY", "WEIGHT", null)
				, null);
		
		relationsRoot = new DefaultTreeNode("root", new HierarchyNode("LEVEL",
				"NAME", "CODE", "SCOPE", "CATEGORY", "WEIGHT", null), null);
        
        displayScopeCatWeight = refCodeListService.getLevelScopeCategorySystemConfig();
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

	//uiEventSourceName is either relations or hierarchy
	public void onNodeExpand(NodeExpandEvent event) {
		////event source attriute from the ui
		String uiSourceOfEvent =  (String) event.getComponent().getAttributes().get("uiEventSourceName");
		TreeNode expandedTreeNode = event.getTreeNode();
		boolean isRelationView = "RELATIONS".equalsIgnoreCase(uiSourceOfEvent);
		boolean isParentListView = "PARENT-LIST".equalsIgnoreCase(uiSourceOfEvent);
		CmqBaseRelationsTreeHelper relationsSearchHelper = new CmqBaseRelationsTreeHelper(cmqBaseService, smqBaseService, meddraDictService, cmqRelationService);	
        relationsSearchHelper.setRelationView(isRelationView);
        relationsSearchHelper.setParentListView(isParentListView);
		relationsSearchHelper.getRelationsNodeHierarchy(null, expandedTreeNode);
	}

	/**
	 * Add the selected hierarchy details to the relation list.
	 */
	@Override
	public void addSelectedRelations(TreeNode[] nodes) {
		try{
			if (nodes != null && nodes.length > 0) {
				List<TreeNode> nodesList = Arrays.asList(nodes);
				List<String> existingNodeTerms = new ArrayList<>();
				for (TreeNode treeNode : nodesList) {
					HierarchyNode hierarchyNode = (HierarchyNode) treeNode.getData();
					if ((null != hierarchyNode) && !hierarchyNode.isDummyNode()) {
						//first check if this node is already added ot relations tree
						boolean exists = false;
						List<TreeNode> existingRelationsTreeNodes = this.relationsRoot.getChildren();
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
								TreeNode relationsTreeNode = new DefaultTreeNode(relationsHierarchyNode, relationsRoot);
								relationsHierarchyNode. markEditableInRelationstable();
								if(!"LLT".equalsIgnoreCase(relationsHierarchyNode.getLevel())) {
									List<TreeNode> childTreeNodes = treeNode.getChildren();
									if(CollectionUtils.isNotEmpty(childTreeNodes)) {
										relationsHierarchyNode.setDataFetchCompleted(false);
										HierarchyNode dummyNode = new HierarchyNode(null, null, null, null);
										dummyNode.setDummyNode(true);
										new DefaultTreeNode(dummyNode, relationsTreeNode);
									}
								} else {
									relationsHierarchyNode.setDataFetchCompleted(true);
								}
							}
						}
					}
				}
				// setRelationSelected(nodes);
				if(CollectionUtils.isNotEmpty(existingNodeTerms)) {
					FacesContext.getCurrentInstance().addMessage(null, 
                            new FacesMessage(FacesMessage.SEVERITY_INFO, "Relation/Term is already associated to the List", ""));
				} else {
					FacesContext.getCurrentInstance().addMessage(null, 
                            new FacesMessage(FacesMessage.SEVERITY_INFO,
                                    "Selected relations added sucessfully.", ""));
				}
			} else {
                FacesContext.getCurrentInstance().addMessage(null, 
                        new FacesMessage(FacesMessage.SEVERITY_INFO, "Please select one or more relations from Hierarchy Search Dialog", ""));
            }
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			FacesContext.getCurrentInstance().addMessage(null, 
                    new FacesMessage(FacesMessage.SEVERITY_FATAL,
                            "An error occured while adding relations.", e.getMessage()));
		}
	}

	/**
	 * Recursively find and delete the selected treenode from root treenode.
	 * 
	 * @param rootNodeToSearchFrom
	 * @param selectedTreeNode
	 */
	public void deleteRelation(TreeNode rootNodeToSearchFrom,
			HierarchyNode selectedNode, Long ownerCmqCode) {
		if (rootNodeToSearchFrom.getChildCount() > 0) {
			List<TreeNode> childTreeNodes = rootNodeToSearchFrom.getChildren();
			for (Iterator<TreeNode> treeNodeIterator = childTreeNodes
					.listIterator(); treeNodeIterator.hasNext();) {
				TreeNode childTreeNode = treeNodeIterator.next();
				HierarchyNode childNode = (HierarchyNode) childTreeNode
						.getData();
				if (childNode.equals(selectedNode)) {
					treeNodeIterator.remove(); // remove it from the root node
					this.deleteRelationFromDb(childNode, ownerCmqCode);
					break;
				} else if (childTreeNode.getChildCount() > 0) {
					// drill down
					this.deleteRelation(childTreeNode, selectedNode, ownerCmqCode);
				}
			}
		}
	}
	
	private void deleteRelationFromDb(HierarchyNode hierarchyNode, Long ownerCmqCode) {
		if (null != hierarchyNode) {
			IEntity entity = hierarchyNode.getEntity();
			if(null != entity) {
				if (entity instanceof CmqBase190) {
					CmqBase190 cmqEntity = (CmqBase190) entity;
					cmqEntity.setCmqParentCode(null);
					cmqEntity.setCmqParentName(null);
					try {
						this.cmqBaseService.update(cmqEntity, this.authService.getUserCn()
								, this.authService.getUserGivenName(), this.authService.getUserSurName()
								, this.authService.getCombinedMappedGroupMembershipAsString());
					} catch (CqtServiceException e) {
						log.error("Error while removing cmq_parent_code value from cmq_id " + cmqEntity.getId(), e);
					}
				} else {
					List<CmqRelation190> existingRelation = this.cmqRelationService.findByCmqCode(ownerCmqCode);
					if((null != existingRelation) && (existingRelation.size() > 0)) {
						boolean matchFound = false;
						Long cmqRelationIdToDelete = null;
						if(entity instanceof MeddraDictHierarchySearchDto) {
							MeddraDictHierarchySearchDto meddraDictHierarchySearchDto = (MeddraDictHierarchySearchDto) hierarchyNode.getEntity();
							String level = hierarchyNode.getLevel();
							long nodeCode = Long.parseLong(meddraDictHierarchySearchDto.getCode());
							for (CmqRelation190 cmqRelation190 : existingRelation) {
								if(level.equalsIgnoreCase("SOC")) {
									if((null != cmqRelation190.getSocCode()) 
											&& (cmqRelation190.getSocCode().longValue() == nodeCode)){
										matchFound = true;
									}
								} else if(level.equalsIgnoreCase("HLGT")) {
									if((null != cmqRelation190.getHlgtCode()) 
											&& (cmqRelation190.getHlgtCode().longValue() == nodeCode)){
										matchFound = true;
									}
								} else if(level.equalsIgnoreCase("HLT")) {
									if((null != cmqRelation190.getHltCode()) 
											&& (cmqRelation190.getHltCode().longValue() == nodeCode)){
										matchFound = true;
									}
								} else if(level.equalsIgnoreCase("PT")) {
									if((null != cmqRelation190.getPtCode()) 
											&& (cmqRelation190.getPtCode().longValue() == nodeCode)){
										matchFound = true;
									}
								} else if(level.equalsIgnoreCase("LLT")) {
									if((null != cmqRelation190.getLltCode()) 
											&& (cmqRelation190.getLltCode().longValue() == nodeCode)){
										matchFound = true;
									}
								}
								if(matchFound) {
									cmqRelationIdToDelete = cmqRelation190.getId();
									break;
								}
							}//end of for (CmqRelation190 cmqRelation190 : existingRelation)
						} else if(entity instanceof MeddraDictReverseHierarchySearchDto) {
							MeddraDictReverseHierarchySearchDto reverseSearchDto = (MeddraDictReverseHierarchySearchDto) hierarchyNode.getEntity();
							String level = hierarchyNode.getLevel();
							for (CmqRelation190 cmqRelation190 : existingRelation) {
								if(level.equalsIgnoreCase("PT") && (cmqRelation190.getPtCode() != null)) {
									Long relationPtCode = cmqRelation190.getPtCode();
									Long reverseSearchDtoPtCode = null;
									if(null != reverseSearchDto.getPtCode()) {
										reverseSearchDtoPtCode = Long.valueOf(reverseSearchDto.getPtCode());
									}
									if((reverseSearchDtoPtCode != null) 
											&& (relationPtCode.longValue() == reverseSearchDtoPtCode.longValue())) {
										matchFound = true;
									}
								} else if(level.equalsIgnoreCase("LLT") && (null != cmqRelation190.getLltCode())) {
									Long relationLltCode = cmqRelation190.getLltCode();
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
									cmqRelationIdToDelete = cmqRelation190.getId();
									break;
								}
							}//end of for (CmqRelation190 cmqRelation190 : existingRelation)
						} else if (entity instanceof SmqBase190) {
							SmqBase190 smqBase = (SmqBase190) entity;
							for (CmqRelation190 cmqRelation190 : existingRelation) {
								if((null != cmqRelation190.getSmqCode()) 
										&& (cmqRelation190.getSmqCode().longValue() == smqBase.getSmqCode().longValue())){
									matchFound = true;
									cmqRelationIdToDelete = cmqRelation190.getId();
									break;
								}
							}//end of for (CmqRelation190 cmqRelation190 : existingRelation)
						} else if (entity instanceof SmqRelation190) {
							SmqRelation190 smqRelation = (SmqRelation190) entity;
							for (CmqRelation190 cmqRelation190 : existingRelation) {
								if((null != cmqRelation190.getSmqCode()) 
										&& (cmqRelation190.getSmqCode().longValue() == smqRelation.getSmqCode().longValue())){
									matchFound = true;
									cmqRelationIdToDelete = cmqRelation190.getId();
									break;
								}
							}//end of for (CmqRelation190 cmqRelation190 : existingRelation)
						}
						
						if(matchFound && (cmqRelationIdToDelete != null)) {
							try {
								this.cmqRelationService.remove(cmqRelationIdToDelete, this.authService.getUserCn()
																	, this.authService.getUserGivenName(), this.authService.getUserSurName()
																	, this.authService.getCombinedMappedGroupMembershipAsString());
							} catch (CqtServiceException e) {
								log.error("Error while removing cmqbase relation.", e);
								FacesMessage message = new FacesMessage(
										FacesMessage.SEVERITY_ERROR, "Loading....",
										"Failed to delete the relation for cmqRelationId "
												+ cmqRelationIdToDelete);
								FacesContext.getCurrentInstance().addMessage(null, message);
							}
						}
					}
				}//end of if(null != entity)
			}//end of if (null != hierarchyNode)
		}//end of if (null != hierarchyNode)
	}

	public void deleteRelations(TreeNode rootNodeToSearchFrom) {
		if ((this.relationSelectedInRelationsTable != null)
				&& (this.relationSelectedInRelationsTable.length > 0)) {
			if (null == rootNodeToSearchFrom) {
				rootNodeToSearchFrom = this.relationsRoot;
			}

			// clear the selections in the table
			clearTreeTableSelections(rootNodeToSearchFrom);
			
			this.deleteSelectedRelations(rootNodeToSearchFrom);


			this.relationSelectedInRelationsTable = null;
		}
	}

	private void deleteSelectedRelations(TreeNode rootNodeToSearchFrom) {

		List<TreeNode> relationsSelectedInRelationsTableList = Arrays
				.asList(this.relationSelectedInRelationsTable);
		List<TreeNode> childTreeNodes = rootNodeToSearchFrom.getChildren();
		for (Iterator<TreeNode> treeNodeIterator = childTreeNodes
				.listIterator(); treeNodeIterator.hasNext();) {
			TreeNode childTreeNode = treeNodeIterator.next();
			// HierarchyNode childNode = (HierarchyNode)
			// childTreeNode.getData();
			if (relationsSelectedInRelationsTableList.contains(childTreeNode)) {
				treeNodeIterator.remove(); // remove it from the root node
				// break;
			} else if (childTreeNode.getChildCount() > 0) {
				// drill down
				this.deleteSelectedRelations(childTreeNode);
			}
		}
	}

	public void clearTreeTableSelections(TreeNode rootTreeNode) {
		if (null == rootTreeNode) {
			rootTreeNode = this.relationsRoot;
		}
		rootTreeNode.setSelected(false);
		if (rootTreeNode.getChildCount() > 0) {
			List<TreeNode> childTreeNodes = rootTreeNode.getChildren();
			for (TreeNode childTreeNode : childTreeNodes) {
				if (null != childTreeNode) {
					childTreeNode.setSelected(false);
				}
				if (childTreeNode.getChildCount() > 0) {
					clearTreeTableSelections(childTreeNode);
				}
			}
		}
	}

	public String[] getSelectedSOCs() {
		return selectedSOCs;
	}

	public void setSelectedSOCs(String[] selectedSOCs) {
		this.selectedSOCs = selectedSOCs;
	}

	public CmqBase190 getSelctedData() {
		return selctedData;
	}

	public void setSelctedData(CmqBase190 selctedData) {
		this.selctedData = selctedData;
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

	public TreeNode[] getRelationSelected() {
		return relationSelected;
	}

	public void setRelationSelected(TreeNode[] relationSelected) {
		this.relationSelected = relationSelected;
	}

	public TreeNode getRelationsRoot() {
		return relationsRoot;
	}

	public void buildRelationsRoot() {
		if ((null != clickedCmqCode) && clickedCmqCode.longValue() != 0) {
			this.setClickedCmqCode(clickedCmqCode);
		}
	}

	public TreeNode getRelationsRootFromCode() {
		return relationsRoot;
	}

	public void setRelationsRoot(TreeNode relationsRoot) {
		this.relationsRoot = relationsRoot;
	}

	public void setRefCodeListService(IRefCodeListService refCodeListService) {
		this.refCodeListService = refCodeListService;
	}

	public TreeNode[] getRelationSelectedInRelationsTable() {
		return relationSelectedInRelationsTable;
	}

	public void setRelationSelectedInRelationsTable(
			TreeNode[] relationSelectedInRelationsTable) {
		this.relationSelectedInRelationsTable = relationSelectedInRelationsTable;
	}

	public Long getClickedCmqCode() {
		return clickedCmqCode;
	}

	public void setClickedCmqCode(Long clickedCmqCode) {
		myHierarchyDlgModel.resetForm();
		CmqBaseRelationsTreeHelper treeHelper = new CmqBaseRelationsTreeHelper(cmqBaseService, smqBaseService, meddraDictService, cmqRelationService);
        treeHelper.setRelationView(true);
        treeHelper.setRequireDrillDown(true);
		this.clickedCmqCode = clickedCmqCode;
		this.relationsRoot = treeHelper.getCmqBaseRelationsRootHierarchy(this.clickedCmqCode);
	}
	
	public void populateParentCmqByChild(CmqBase190 childCmq) {
		if(childCmq.getCmqParentCode() != null) {
			log.info("Populating cmq base parent for cmq child code " + childCmq.getCmqCode());
			this.parentCmqEntity = this.cmqBaseService.findByCode(childCmq.getCmqParentCode());
			if(null != parentCmqEntity) {
				this.parentListRoot = new DefaultTreeNode("root"
						, new HierarchyNode("LEVEL", "NAME", "CODE", "SCOPE", "CATEGORY", "WEIGHT", null)
						, null);
				HierarchyNode node = new HierarchyNode();
				node.setLevel(parentCmqEntity.getCmqTypeCd());
				node.setCode(parentCmqEntity.getCmqCode().toString());
				node.setTerm(parentCmqEntity.getCmqName());
				node.setCategory("");
				node.setWeight("");
				node.setScope("");
				node.setEntity(parentCmqEntity);
				
				TreeNode treeNode = new DefaultTreeNode(node, this.parentListRoot);
			
				Long childCount = this.cmqRelationService.findCountByCmqCode(childCmq.getCmqParentCode());
				if((null != childCount) && (childCount > 0)) {
					HierarchyNode dummyNode = new HierarchyNode(null, null, null, null);
					dummyNode.setDummyNode(true);
					new DefaultTreeNode(dummyNode, treeNode);
				}
			}
		} else {
			log.info("No parent exists for cmq child code " + childCmq.getCmqCode());
		}
	}
	
	/**
	 * Event handler for drag-and-drop from "Hierarchy Search" treetable to "Result Relations" treetable
	 */
	@Override
	public void onDropRelations() {
        addSelectedRelations(myHierarchyDlgModel.getSelectedNodes());
	}
	
	/**
	 * Reset relations
	 */
	public String resetRelations(CmqBase190 selectedData) {
		if(selectedData != null && selectedData.getCmqCode() != null)
			this.clickedCmqCode = selectedData.getCmqCode();
		buildRelationsRoot();
		FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_WARN, "Form canceled", ""));
		return "";
	}

	public TreeNode getParentListRoot() {
		return parentListRoot;
	}

	public void setParentListRoot(TreeNode parentListRoot) {
		this.parentListRoot = parentListRoot;
	}

	public CmqBase190 getParentCmqEntity() {
		return parentCmqEntity;
	}

	public void setParentCmqEntity(CmqBase190 parentCmqEntity) {
		this.parentCmqEntity = parentCmqEntity;
	}

	public AuthenticationService getAuthService() {
		return authService;
	}

	public void setAuthService(AuthenticationService authService) {
		this.authService = authService;
	}
    
    public CmqBaseHierarchySearchVM getHierarchyDlgModel() {
		return myHierarchyDlgModel;
	}

	public void setHierarchyDlgModel(CmqBaseHierarchySearchVM hierSearchFormModel) {
		this.myHierarchyDlgModel = hierSearchFormModel;
	}
    
    public boolean isDisplayScopeCatWeight() {
		return displayScopeCatWeight;
	}

	public void setDisplayScopeCatWeight(boolean displayScopeCatWeight) {
		this.displayScopeCatWeight = displayScopeCatWeight;
	}

}