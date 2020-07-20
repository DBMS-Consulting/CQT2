package com.dbms.view;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.component.UIViewRoot;
import javax.faces.context.FacesContext;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.primefaces.context.RequestContext;
import org.primefaces.event.NodeCollapseEvent;
import org.primefaces.event.NodeExpandEvent;
import org.primefaces.model.DefaultTreeNode;
import org.primefaces.model.TreeNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dbms.controller.GlobalController;
import com.dbms.controller.beans.HierarchySearchResultBean;
import com.dbms.csmq.HierarchyNode;
import com.dbms.entity.IEntity;
import com.dbms.entity.cqt.CmqBase190;
import com.dbms.entity.cqt.CmqRelation190;
import com.dbms.entity.cqt.SmqBase190;
import com.dbms.entity.cqt.SmqRelation190;
import com.dbms.entity.cqt.dtos.MeddraDictHierarchySearchDto;
import com.dbms.entity.cqt.dtos.MeddraDictReverseHierarchySearchDto;
import com.dbms.entity.cqt.dtos.SMQReverseHierarchySearchDto;
import com.dbms.service.AuthenticationService;
import com.dbms.service.ICmqBase190Service;
import com.dbms.service.ICmqRelation190Service;
import com.dbms.service.IMeddraDictService;
import com.dbms.service.IRefCodeListService;
import com.dbms.service.ISmqBaseService;
import com.dbms.util.CmqUtils;
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
	private GlobalController globalController;
	
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
    
    private String scopeFromParent;

    
    public ListRelationsVM(AuthenticationService authService, SWJSFRequest appSWJSFRequest,
            IRefCodeListService refCodeListService, ICmqBase190Service cmqBaseService, ISmqBaseService smqBaseService,
            IMeddraDictService meddraDictService, ICmqRelation190Service cmqRelationService
            , GlobalController globalController) {
        this.authService = authService;
        this.refCodeListService = refCodeListService;
        this.cmqBaseService = cmqBaseService;
        this.smqBaseService = smqBaseService;
        this.meddraDictService = meddraDictService;
        this.cmqRelationService = cmqRelationService;
        this.globalController = globalController;
        
        myHierarchyDlgModel = new CmqBaseHierarchySearchVM(cmqBaseService, smqBaseService, meddraDictService, cmqRelationService, globalController);
		
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

	public void hanldeFilterLltFlagToggle(boolean filterLltFlag) {
		System.out.println("hanldeFilterLltFlagToggle called");
		if(null != this.relationsRoot) {
			List<TreeNode> childrenNodes = this.relationsRoot.getChildren();
			for (TreeNode childTreeNode : childrenNodes) {
				childTreeNode.setExpanded(false);
				childTreeNode.getChildren().clear();//remove all children
				HierarchyNode hNode = (HierarchyNode) childTreeNode.getData();
				hNode.setDataFetchCompleted(false);
				IEntity entity = (IEntity) hNode.getEntity();
				if((!hNode.getLevel().equalsIgnoreCase("PT") && !hNode.getLevel().equalsIgnoreCase("LLT"))
						|| ((!filterLltFlag && (hNode.getLevel().equalsIgnoreCase("PT") 
								|| hNode.getLevel().equalsIgnoreCase("SMQ4"))) && !(entity instanceof SmqRelation190))) {
					//add a dummy node if ther eis no child here
					if(childTreeNode.getChildCount() == 0) {
						HierarchyNode dummyNode = new HierarchyNode(null, null, null, null);
						dummyNode.setDummyNode(true);
						new DefaultTreeNode(dummyNode, childTreeNode);
					}
				}
			}
			UIViewRoot viewRoot = FacesContext.getCurrentInstance().getViewRoot();
			if(null != viewRoot) {
				//in crate, update copy
				UIComponent relationTreeTableComponent = CmqUtils.findComponent(viewRoot, "resultRelations");
				if(null != relationTreeTableComponent) {
					//update has to be on relationTreeTableComponent.getClientId() and not on the xhtml id
					RequestContext.getCurrentInstance().update(relationTreeTableComponent.getClientId());
				}
				//in b&s
				UIComponent relationTreeTableForBrowseComponent = CmqUtils.findComponent(viewRoot, "relations-tree-table");
				if(null != relationTreeTableForBrowseComponent) {
					//update has to be on relationTreeTableForBrowseComponent.getClientId() and not on the xhtml id
					RequestContext.getCurrentInstance().update(relationTreeTableForBrowseComponent.getClientId());
				}
				//in ia left side
				UIComponent currentListsAndSmqsComponent = CmqUtils.findComponent(viewRoot, "currentListsAndSmqs");
				if(null != currentListsAndSmqsComponent) {
					//update has to be on currentListsAndSmqsComponent.getClientId() and not on the xhtml id
					RequestContext.getCurrentInstance().update(currentListsAndSmqsComponent.getClientId());
				}
			}
		}
	}
	
	//uiEventSourceName is either relations or hierarchy
	public void onNodeExpand(NodeExpandEvent event) {
		////event source attriute from the ui
		String uiSourceOfEvent =  (String) event.getComponent().getAttributes().get("uiEventSourceName");
		TreeNode expandedTreeNode = event.getTreeNode();
		boolean isRelationView = "RELATIONS".equalsIgnoreCase(uiSourceOfEvent);
		boolean isParentListView = "PARENT-LIST".equalsIgnoreCase(uiSourceOfEvent);
		CmqBaseRelationsTreeHelper relationsSearchHelper = new CmqBaseRelationsTreeHelper(cmqBaseService, smqBaseService
																	, meddraDictService, cmqRelationService, globalController);	
		
		HierarchyNode hNode = (HierarchyNode) expandedTreeNode.getData();
		IEntity entity = hNode.getEntity();
		
		//scan upto 6 tree levels to find if we have an smq with a scope
		if(entity instanceof SmqBase190) {
			//level 1 up
			String parentLevel1Up = hNode.getScope();
			if(StringUtils.isNoneBlank(parentLevel1Up) 
					&& (parentLevel1Up.equals("1") || parentLevel1Up.equals("2") 
							|| parentLevel1Up.equals("3") || parentLevel1Up.equals("4"))) {
				relationsSearchHelper.setScopeFromParent(parentLevel1Up); 
			} else {
				//level 2 up
				TreeNode parentLevel2Up = expandedTreeNode.getParent();
				HierarchyNode parentLevel2UpHnode = (HierarchyNode) parentLevel2Up.getData();
				IEntity parentLevel2UpEntity = parentLevel2UpHnode.getEntity();
				if(parentLevel2UpEntity instanceof SmqBase190) {
					String parentLevel2UpScope = parentLevel2UpHnode.getScope();
					if(StringUtils.isNoneBlank(parentLevel2UpScope) 
							&& (parentLevel2UpScope.equals("1") || parentLevel2UpScope.equals("2") 
									|| parentLevel2UpScope.equals("3") || parentLevel2UpScope.equals("4"))) {
						relationsSearchHelper.setScopeFromParent(parentLevel2UpScope); 
					} else {
						//level 3 up
						TreeNode parentLevel3Up = expandedTreeNode.getParent().getParent();
						if(null != parentLevel3Up) {
							HierarchyNode parentLevel3UpHnode = (HierarchyNode) parentLevel3Up.getData();
							IEntity parentLevel3UpEntity = parentLevel3UpHnode.getEntity();
							if(parentLevel3UpEntity instanceof SmqBase190) {
								String parentLevel3UpScope = parentLevel3UpHnode.getScope();
								if(StringUtils.isNoneBlank(parentLevel3UpScope) 
										&& (parentLevel3UpScope.equals("1") || parentLevel3UpScope.equals("2") 
												|| parentLevel3UpScope.equals("3") || parentLevel3UpScope.equals("4"))) {
									relationsSearchHelper.setScopeFromParent(parentLevel3UpScope); 
								} else {
									//level 4 up
									TreeNode parentLevel4Up = expandedTreeNode.getParent().getParent().getParent();
									if(null != parentLevel4Up) {
										HierarchyNode parentLevel4UpHnode = (HierarchyNode) parentLevel4Up.getData();
										IEntity parentLevel4UpEntity = parentLevel4UpHnode.getEntity();
										if(parentLevel4UpEntity instanceof SmqBase190) {
											String parentLevel4UpScope = parentLevel4UpHnode.getScope();
											if(StringUtils.isNoneBlank(parentLevel4UpScope) 
													&& (parentLevel4UpScope.equals("1") || parentLevel4UpScope.equals("2") 
															|| parentLevel4UpScope.equals("3") || parentLevel4UpScope.equals("4"))) {
												relationsSearchHelper.setScopeFromParent(parentLevel4UpScope); 
											} else {
												//level 5 up
												TreeNode parentLevel5Up = expandedTreeNode.getParent().getParent().getParent().getParent();
												if(null != parentLevel5Up) {
													HierarchyNode parentLevel5UpHnode = (HierarchyNode) parentLevel5Up.getData();
													IEntity parentLevel5UpEntity = parentLevel5UpHnode.getEntity();
													if(parentLevel5UpEntity instanceof SmqBase190) {
														String parentLevel5UpScope = parentLevel5UpHnode.getScope();
														if(StringUtils.isNoneBlank(parentLevel5UpScope) 
																&& (parentLevel5UpScope.equals("1") || parentLevel5UpScope.equals("2") 
																		|| parentLevel5UpScope.equals("3") || parentLevel5UpScope.equals("4"))) {
															relationsSearchHelper.setScopeFromParent(parentLevel5UpScope); 
														} else {
															//level 5 up
															TreeNode parentLevel6Up = expandedTreeNode.getParent().getParent().getParent().getParent().getParent();
															if(null != parentLevel6Up) {
																HierarchyNode parentLevel6UpHnode = (HierarchyNode) parentLevel6Up.getData();
																IEntity parentLevel6UpEntity = parentLevel6UpHnode.getEntity();
																if(parentLevel6UpEntity instanceof SmqBase190) {
																	String parentLevel6UpScope = parentLevel6UpHnode.getScope();
																	if(StringUtils.isNoneBlank(parentLevel6UpScope) 
																			&& (parentLevel6UpScope.equals("1") || parentLevel6UpScope.equals("2") 
																					|| parentLevel6UpScope.equals("3") || parentLevel6UpScope.equals("4"))) {
																		relationsSearchHelper.setScopeFromParent(parentLevel6UpScope); 
																	}
																}
															}
														}
													}
												}
											}
										}
									}
								}
							}
						}
					}
				}
			}
		}
        
		relationsSearchHelper.setRelationView(isRelationView);
        relationsSearchHelper.setParentListView(isParentListView);
		relationsSearchHelper.getRelationsNodeHierarchy(null, expandedTreeNode);
		for(TreeNode child: expandedTreeNode.getChildren()) {
			HierarchyNode hierNode = (HierarchyNode) child.getData();
			HierarchyNode parentNode = (HierarchyNode) child.getParent().getData();
			if(parentNode.getLevel().equalsIgnoreCase("SMQ") || parentNode.getLevel().equalsIgnoreCase("'C' SMQ") 
					|| parentNode.getLevel().equalsIgnoreCase("PRO")) {
				hierNode.setHideScope(true);
			}
			if(parentNode.getLevel().equalsIgnoreCase("SMQ") || parentNode.getLevel().equalsIgnoreCase("'C' SMQ")
					|| parentNode.getLevel().equalsIgnoreCase("SMQ1") || parentNode.getLevel().equalsIgnoreCase("SMQ2")
					|| parentNode.getLevel().equalsIgnoreCase("SMQ3") || parentNode.getLevel().equalsIgnoreCase("SMQ4")
					|| parentNode.getLevel().equalsIgnoreCase("SMQ5")) {
				hierNode.setReadOnlyCategory(true);
			} else {
				hierNode.setReadOnlyCategory(false);
			}
			
		}
	}
	
	public void onNodeCollapse(NodeCollapseEvent event) {
		TreeNode expandedTreeNode = event.getTreeNode();
		expandedTreeNode.setExpanded(false);
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
								relationsHierarchyNode.setJustAdded(true);
								IEntity entity = relationsHierarchyNode.getEntity();
								if(entity instanceof MeddraDictReverseHierarchySearchDto) {
									MeddraDictReverseHierarchySearchDto reverseHierarchySearchDto = (MeddraDictReverseHierarchySearchDto)entity;
									if("SOC".equalsIgnoreCase(relationsHierarchyNode.getLevel())) {
										Long socCode = Long.valueOf(reverseHierarchySearchDto.getSocCode());
										MeddraDictHierarchySearchDto socDictHierarchySearchDto = this.meddraDictService.findByCode("SOC_", socCode);
										relationsHierarchyNode.setEntity(socDictHierarchySearchDto);
										relationsHierarchyNode.setDataFetchCompleted(false);
										relationsHierarchyNode.setHideDelete(true);
										HierarchyNode dummyNode = new HierarchyNode(null, null, null, null);
										dummyNode.setDummyNode(true);
										new DefaultTreeNode(dummyNode, relationsTreeNode);
									} else if("HLT".equalsIgnoreCase(relationsHierarchyNode.getLevel())) {
										Long hltCode = Long.valueOf(reverseHierarchySearchDto.getHltCode());
										MeddraDictHierarchySearchDto hltDictHierarchySearchDto = this.meddraDictService.findByCode("HLT_", hltCode);
										relationsHierarchyNode.setEntity(hltDictHierarchySearchDto);
										relationsHierarchyNode.setDataFetchCompleted(false);
										relationsHierarchyNode.setHideDelete(true);
										HierarchyNode dummyNode = new HierarchyNode(null, null, null, null);
										dummyNode.setDummyNode(true);
										new DefaultTreeNode(dummyNode, relationsTreeNode);
									} else if("HLGT".equalsIgnoreCase(relationsHierarchyNode.getLevel())) {
										Long hlgtCode = Long.valueOf(reverseHierarchySearchDto.getHlgtCode());
										MeddraDictHierarchySearchDto hlgtDictHierarchySearchDto = this.meddraDictService.findByCode("HLGT_", hlgtCode);
										relationsHierarchyNode.setEntity(hlgtDictHierarchySearchDto);
										relationsHierarchyNode.setDataFetchCompleted(false);
										relationsHierarchyNode.setHideDelete(true);
										HierarchyNode dummyNode = new HierarchyNode(null, null, null, null);
										dummyNode.setDummyNode(true);
										new DefaultTreeNode(dummyNode, relationsTreeNode);
									} else if("PT".equalsIgnoreCase(relationsHierarchyNode.getLevel())) {
										Long ptCode = Long.valueOf(reverseHierarchySearchDto.getPtCode());
										MeddraDictHierarchySearchDto ptDictHierarchySearchDto = this.meddraDictService.findByCode("PT_", ptCode);
										relationsHierarchyNode.setEntity(ptDictHierarchySearchDto);
										relationsHierarchyNode.setDataFetchCompleted(false);
										relationsHierarchyNode.setHideDelete(true);
										
										boolean filterLltFlag = this.globalController.isFilterLltsFlag();
										if(!filterLltFlag) {
											HierarchyNode dummyNode = new HierarchyNode(null, null, null, null);
											dummyNode.setDummyNode(true);
											new DefaultTreeNode(dummyNode, relationsTreeNode);
										}
									} else if("LLT".equalsIgnoreCase(relationsHierarchyNode.getLevel())){
										relationsHierarchyNode.setHideDelete(true);
									}
								} else if(entity instanceof SMQReverseHierarchySearchDto) {
									Long ptCode = ((SMQReverseHierarchySearchDto) entity).getSmqCode();
									SmqBase190 smqBase190 = this.smqBaseService.findByCode(ptCode);
									relationsHierarchyNode.setEntity(smqBase190);
									relationsHierarchyNode.setCode(smqBase190.getSmqCode().toString());
									relationsHierarchyNode.setScope("2");
									relationsHierarchyNode.setHideDelete(true);
									List<TreeNode> childTreeNodes = treeNode.getChildren();
									if(CollectionUtils.isNotEmpty(childTreeNodes)) {
										relationsHierarchyNode.setDataFetchCompleted(false);
										HierarchyNode dummyNode = new HierarchyNode(null, null, null, null);
										dummyNode.setDummyNode(true);
										new DefaultTreeNode(dummyNode, relationsTreeNode);
									}
								} else if(entity instanceof SmqRelation190) {
									relationsHierarchyNode.setCategory(null);
									relationsHierarchyNode.setScope(null);
									relationsHierarchyNode.setWeight(null);
									relationsHierarchyNode.setHideDelete(true);
									
									List<TreeNode> childTreeNodes = treeNode.getChildren();
									if(CollectionUtils.isNotEmpty(childTreeNodes)) {
										relationsHierarchyNode.setDataFetchCompleted(false);
										HierarchyNode dummyNode = new HierarchyNode(null, null, null, null);
										dummyNode.setDummyNode(true);
										new DefaultTreeNode(dummyNode, relationsTreeNode);
									}
								} else if(!"LLT".equalsIgnoreCase(relationsHierarchyNode.getLevel())) {
									if(entity instanceof SmqBase190) {
										relationsHierarchyNode.setScope("2");
									}
									relationsHierarchyNode.setHideDelete(true);
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
                                    "Selected relations added successfully.", ""));
				}
				myHierarchyDlgModel.setFilterLevel(""); 
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
		
		
		//Reset HS level
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
					this.deleteRelationFromDb(selectedNode, ownerCmqCode);
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
									if((cmqRelation190.getPtCode() != null && cmqRelation190.getPtCode().longValue() == smqRelation.getPtCode()) 
											|| (cmqRelation190.getLltCode() != null && cmqRelation190.getLltCode().longValue() == smqRelation.getPtCode())) {
										matchFound = true;
										cmqRelationIdToDelete = cmqRelation190.getId();
										break;
									}
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

	public void clearChildrenInTreNode(TreeNode rootNodeToSearchFrom, HierarchyNode selectedNode) {
		if (rootNodeToSearchFrom.getChildCount() > 0) {
			List<TreeNode> childTreeNodes = rootNodeToSearchFrom.getChildren();
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
					break;
				} else if (childTreeNode.getChildCount() > 0) {
					// drill down
					this.clearChildrenInTreNode(childTreeNode, selectedNode);
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
		CmqBaseRelationsTreeHelper treeHelper = new CmqBaseRelationsTreeHelper(cmqBaseService, smqBaseService, meddraDictService, cmqRelationService, globalController);
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
