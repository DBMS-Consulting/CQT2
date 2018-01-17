package com.dbms.view;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.primefaces.event.NodeExpandEvent;
import org.primefaces.model.DefaultTreeNode;
import org.primefaces.model.TreeNode;

import com.dbms.controller.GlobalController;
import com.dbms.csmq.CSMQBean;
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
import com.dbms.entity.cqt.dtos.SMQReverseHierarchySearchDto;
import com.dbms.service.ICmqBase190Service;
import com.dbms.service.ICmqBaseTargetService;
import com.dbms.service.ICmqRelation190Service;
import com.dbms.service.ICmqRelationTargetService;
import com.dbms.service.IMeddraDictService;
import com.dbms.service.IMeddraDictTargetService;
import com.dbms.service.IRefCodeListService;
import com.dbms.service.ISmqBaseService;
import com.dbms.service.ISmqBaseTargetService;
import com.dbms.util.CqtConstants;
import com.dbms.util.SMQLevelHelper;

public class IARelationsTreeHelper {
	public enum SearchTarget { SMQ_BASE, MEDDRA_DICT, CMQ_BASE }
	
	private static final String NO_SCOPE_FILTER = "-1";

	
    private ICmqBase190Service cmqBaseCurrentService;
	private ISmqBaseService smqBaseCurrentService;
	private IMeddraDictService meddraDictCurrentService;
	private ICmqRelation190Service cmqRelationCurrentService;
    
	private ICmqBaseTargetService cmqBaseTargetService;
	private ISmqBaseTargetService smqBaseTargetService;
	private IMeddraDictTargetService meddraDictTargetService;
	private ICmqRelationTargetService cmqRelationTargetService;
    private GlobalController globalController;

    public IARelationsTreeHelper(
            ICmqBase190Service cmqBaseService,
            ISmqBaseService smqBaseService,
            IMeddraDictService meddraDictService,
            ICmqRelation190Service cmqRelationService,
            GlobalController globalController) {
        
        this.cmqBaseCurrentService = cmqBaseService;
		this.smqBaseCurrentService = smqBaseService;
		this.meddraDictCurrentService = meddraDictService;
		this.cmqRelationCurrentService = cmqRelationService;
		this.globalController = globalController;
	}
    
    public IARelationsTreeHelper(
            ICmqBaseTargetService cmqBaseSvc,
			ISmqBaseTargetService smqBaseSvc,
			IMeddraDictTargetService meddraDictSvc,
			ICmqRelationTargetService cmqRelationSvc,
			GlobalController globalController) {      
		this.cmqBaseTargetService = cmqBaseSvc;
		this.smqBaseTargetService = smqBaseSvc;
		this.meddraDictTargetService = meddraDictSvc;
		this.cmqRelationTargetService = cmqRelationSvc;
		this.globalController = globalController;
	}
    
    public IARelationsTreeHelper(
            ICmqBase190Service cmqBaseService,
            ISmqBaseService smqBaseService,
            IMeddraDictService meddraDictService,
            ICmqRelation190Service cmqRelationService,
            ICmqBaseTargetService cmqBaseSvc,
			ISmqBaseTargetService smqBaseSvc,
			IMeddraDictTargetService meddraDictSvc,
			ICmqRelationTargetService cmqRelationSvc,
			GlobalController globalController) {
        
        this.cmqBaseCurrentService = cmqBaseService;
		this.smqBaseCurrentService = smqBaseService;
		this.meddraDictCurrentService = meddraDictService;
		this.cmqRelationCurrentService = cmqRelationService;
        
        this.cmqBaseTargetService = cmqBaseSvc;
		this.smqBaseTargetService = smqBaseSvc;
		this.meddraDictTargetService = meddraDictSvc;
		this.cmqRelationTargetService = cmqRelationSvc;
		this.globalController = globalController;
	}
	
	public void onNodeExpandCurrentTable(TreeNode rootNode, NodeExpandEvent event) {
		TreeNode expandedTreeNode = event.getTreeNode();
		HierarchyNode hierarchyNode = (HierarchyNode) expandedTreeNode.getData();
		boolean isDataFetchCompleted = hierarchyNode.isDataFetchCompleted();
		if (!isDataFetchCompleted) {
			String scopeFilter = this.getScopeFromExpandedTreeNodeInCurrentTable(expandedTreeNode);
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
				this.populateCmqBaseChildren(cmqCode, expandedTreeNode, "current", null);
				this.populateCmqRelations(cmqCode, expandedTreeNode, "current", null, entity);
				
                setCurrentCmqBaseNodeStyle(hierarchyNode, cmqBase);
			} else if (entity instanceof SmqBase190){
				SmqBase190 smqBase = (SmqBase190) entity;
				this.populateSmqBaseChildren(smqBase.getSmqCode(), expandedTreeNode, "current", null);
				this.populateSmqRelations(smqBase.getSmqCode(), expandedTreeNode, "current", null, scopeFilter);
				
				//Color
				if (CSMQBean.IMPACT_TYPE_ICS.equals(smqBase.getImpactType()) ||
                        CSMQBean.IMPACT_TYPE_IMPACTED.equalsIgnoreCase(smqBase.getImpactType()))
					hierarchyNode.setRowStyleClass("blue-colored");
			} else if(entity instanceof MeddraDictHierarchySearchDto) {
				String parentLevel = hierarchyNode.getLevel();
				MeddraDictHierarchySearchDto meddraDictHierarchySearchDto = (MeddraDictHierarchySearchDto)entity;
				Long dtoCode = Long.valueOf(meddraDictHierarchySearchDto.getCode());
				this.populateMeddraDictHierarchySearchDtoChildren(parentLevel, dtoCode, expandedTreeNode, "current", null);
			}
            
			hierarchyNode.setDataFetchCompleted(true);
		}
		//hierarchyNode.setRowStyleClass("blue-colored");
	}
	
	public void onNodeExpandTargetTable(TreeNode rootNode, NodeExpandEvent event, boolean applyScope) {
		String uiSourceOfEvent =  (String) event.getComponent().getAttributes().get("uiEventSourceName");
		TreeNode expandedTreeNode = event.getTreeNode();
		HierarchyNode hierarchyNode = (HierarchyNode) expandedTreeNode.getData();
		
		boolean isDataFetchCompleted = hierarchyNode.isDataFetchCompleted();
		if (!isDataFetchCompleted) {
			String scopeFilter = null;
			if(applyScope) {
				scopeFilter = this.getScopeFromExpandedTreeNodeInTargetTable(expandedTreeNode);
			}
			IEntity entity = hierarchyNode.getEntity();
			
			// remove the first dummy node placeholder
			HierarchyNode dummyChildData = (HierarchyNode) expandedTreeNode.getChildren().get(0).getData();
			if (dummyChildData.isDummyNode()) {
				expandedTreeNode.getChildren().remove(0);
			}
			
			if (entity instanceof CmqBaseTarget) {
				CmqBaseTarget cmqBase = (CmqBaseTarget) entity;
				Long cmqCode = cmqBase.getCmqCode();
				this.populateCmqBaseChildren(cmqCode, expandedTreeNode, "target", uiSourceOfEvent);
				this.populateCmqRelations(cmqCode, expandedTreeNode, "target", uiSourceOfEvent, entity);
				
				//Color           
                setTargetCmqBaseNodeStyle(hierarchyNode, cmqBase);
			} else if (entity instanceof SMQReverseHierarchySearchDto){
	        	SMQReverseHierarchySearchDto smqBase = (SMQReverseHierarchySearchDto) entity;
	            this.populateSmqReverseDtoParent(smqBase.getSmqCode(), expandedTreeNode, "target", uiSourceOfEvent);
	        } else if (entity instanceof SmqBaseTarget){
				SmqBaseTarget smqBase = (SmqBaseTarget) entity;
				this.populateSmqBaseChildren(smqBase.getSmqCode(), expandedTreeNode, "target", uiSourceOfEvent);
				this.populateSmqRelations(smqBase.getSmqCode(), expandedTreeNode, "target", uiSourceOfEvent, scopeFilter);
				
				 
				//Color
				if (CSMQBean.IMPACT_TYPE_ICS.equals(smqBase.getImpactType()) ||
                        CSMQBean.IMPACT_TYPE_IMPACTED.equalsIgnoreCase(smqBase.getImpactType()))
					hierarchyNode.setRowStyleClass("blue-colored");
			} else if(entity instanceof MeddraDictHierarchySearchDto) {
				String parentLevel = hierarchyNode.getLevel();
				MeddraDictHierarchySearchDto meddraDictHierarchySearchDto = (MeddraDictHierarchySearchDto)entity;
				Long dtoCode = Long.valueOf(meddraDictHierarchySearchDto.getCode());
				this.populateMeddraDictHierarchySearchDtoChildren(parentLevel, dtoCode, expandedTreeNode, "target", uiSourceOfEvent);
			} else if(entity instanceof MeddraDictReverseHierarchySearchDto) {
				MeddraDictReverseHierarchySearchDto reverseSearchDto = (MeddraDictReverseHierarchySearchDto)entity;
				String levelOfExpandedNode = hierarchyNode.getLevel();
				if("LLT".equalsIgnoreCase(levelOfExpandedNode)) {
					Long lltCode = Long.valueOf(reverseSearchDto.getLltCode());
					this.populateMeddraDictReverseHierarchySearchDtoChildren("LLT_", "PT", lltCode, hierarchyNode
																				, expandedTreeNode, reverseSearchDto, false, uiSourceOfEvent);	
				} else if ("PT".equalsIgnoreCase(levelOfExpandedNode)) {
					Long ptCode = Long.valueOf(reverseSearchDto.getPtCode());
					//if its main view tables then show downward hierarchy else its from hierarchySearch so show reverse hierarchy
					if("target-table".equalsIgnoreCase(uiSourceOfEvent) || "current-table".equalsIgnoreCase(uiSourceOfEvent)) {
						this.populateMeddraDictHierarchySearchDtoChildren(levelOfExpandedNode, ptCode, expandedTreeNode, "target", uiSourceOfEvent);
					} else {
						this.populateMeddraDictReverseHierarchySearchDtoChildren("PT_", "HLT", ptCode, hierarchyNode
								, expandedTreeNode, reverseSearchDto, false, uiSourceOfEvent);	
					}
				} else if ("HLT".equalsIgnoreCase(levelOfExpandedNode)) {
					Long hltCode = Long.valueOf(reverseSearchDto.getHltCode());
					this.populateMeddraDictReverseHierarchySearchDtoChildren("HLT_", "HLGT", hltCode, hierarchyNode
																				, expandedTreeNode, reverseSearchDto, false, uiSourceOfEvent);	
				} else if ("HLGT".equalsIgnoreCase(levelOfExpandedNode)) {
					Long hlgtCode = Long.valueOf(reverseSearchDto.getHlgtCode());
					this.populateMeddraDictReverseHierarchySearchDtoChildren("HLGT_", "SOC", hlgtCode, hierarchyNode
																				, expandedTreeNode, reverseSearchDto, false, uiSourceOfEvent);	
				}
			}
			hierarchyNode.setDataFetchCompleted(true);
			// 
		}
	}
	
	public void onNodeExpandTargetTableScope(TreeNode rootNode, NodeExpandEvent event, String scopeFilter) {
		String uiSourceOfEvent =  "target-table";
		TreeNode expandedTreeNode = rootNode;
		HierarchyNode hierarchyNode = (HierarchyNode) expandedTreeNode.getData();
		boolean isDataFetchCompleted = hierarchyNode.isDataFetchCompleted();
		if (!isDataFetchCompleted) {
			IEntity entity = hierarchyNode.getEntity();
			
			// remove the first dummy node placeholder
			HierarchyNode dummyChildData = (HierarchyNode) expandedTreeNode.getChildren().get(0).getData();
			if (dummyChildData.isDummyNode()) {
				expandedTreeNode.getChildren().remove(0);
			}
			
			if (entity instanceof SmqBaseTarget){
				SmqBaseTarget smqBase = (SmqBaseTarget) entity;
				this.populateSmqBaseChildren(smqBase.getSmqCode(), expandedTreeNode, "target", uiSourceOfEvent);
				this.populateSmqRelations(smqBase.getSmqCode(), expandedTreeNode, "target", uiSourceOfEvent, scopeFilter);
				
				 
				//Color
				if (CSMQBean.IMPACT_TYPE_ICS.equals(smqBase.getImpactType()) ||
                        CSMQBean.IMPACT_TYPE_IMPACTED.equalsIgnoreCase(smqBase.getImpactType()))
					hierarchyNode.setRowStyleClass("blue-colored");
			} 
			hierarchyNode.setDataFetchCompleted(true);
			// 
		}
	}
	
	private String getScopeFromExpandedTreeNodeInTargetTable(TreeNode expandedTreeNode) {
		HierarchyNode hierarchyNode = (HierarchyNode) expandedTreeNode.getData();
		IEntity entity = hierarchyNode.getEntity();
		
		//scan upto 4 tree levels to find if we have an smq with a scope
		String scopeFilter = null;
		if(entity instanceof SmqBaseTarget) {
			//level 1 up
			String parentLevel1Up = hierarchyNode.getScope();
			if(StringUtils.isNoneBlank(parentLevel1Up) 
					&& (parentLevel1Up.equals("1") || parentLevel1Up.equals("2") 
							|| parentLevel1Up.equals("3") || parentLevel1Up.equals("4"))) {
				scopeFilter = parentLevel1Up; 
			} else {
				//level 2 up
				TreeNode parentLevel2Up = expandedTreeNode.getParent();
				HierarchyNode parentLevel2UpHnode = (HierarchyNode) parentLevel2Up.getData();
				IEntity parentLevel2UpEntity = parentLevel2UpHnode.getEntity();
				if(parentLevel2UpEntity instanceof SmqBaseTarget) {
					String parentLevel2UpScope = parentLevel2UpHnode.getScope();
					if(StringUtils.isNoneBlank(parentLevel2UpScope) 
							&& (parentLevel2UpScope.equals("1") || parentLevel2UpScope.equals("2") 
									|| parentLevel2UpScope.equals("3") || parentLevel2UpScope.equals("4"))) {
						scopeFilter = parentLevel2UpScope; 
					} else {
						//level 3 up
						TreeNode parentLevel3Up = expandedTreeNode.getParent().getParent();
						if(null != parentLevel3Up) {
							HierarchyNode parentLevel3UpHnode = (HierarchyNode) parentLevel3Up.getData();
							IEntity parentLevel3UpEntity = parentLevel3UpHnode.getEntity();
							if(parentLevel3UpEntity instanceof SmqBaseTarget) {
								String parentLevel3UpScope = parentLevel3UpHnode.getScope();
								if(StringUtils.isNoneBlank(parentLevel3UpScope) 
										&& (parentLevel3UpScope.equals("1") || parentLevel3UpScope.equals("2") 
												|| parentLevel3UpScope.equals("3") || parentLevel3UpScope.equals("4"))) {
									scopeFilter = parentLevel3UpScope; 
								} else {
									//level 4 up
									TreeNode parentLevel4Up = expandedTreeNode.getParent().getParent().getParent();
									if(null != parentLevel4Up) {
										HierarchyNode parentLevel4UpHnode = (HierarchyNode) parentLevel4Up.getData();
										IEntity parentLevel4UpEntity = parentLevel4UpHnode.getEntity();
										if(parentLevel4UpEntity instanceof SmqBaseTarget) {
											String parentLevel4UpScope = parentLevel4UpHnode.getScope();
											if(StringUtils.isNoneBlank(parentLevel4UpScope) 
													&& (parentLevel4UpScope.equals("1") || parentLevel4UpScope.equals("2") 
															|| parentLevel4UpScope.equals("3") || parentLevel4UpScope.equals("4"))) {
												scopeFilter = parentLevel4UpScope; 
											} else {
												//level 5 up
												TreeNode parentLevel5Up = expandedTreeNode.getParent().getParent().getParent().getParent();
												if(null != parentLevel5Up) {
													HierarchyNode parentLevel5UpHnode = (HierarchyNode) parentLevel5Up.getData();
													IEntity parentLevel5UpEntity = parentLevel5UpHnode.getEntity();
													if(parentLevel5UpEntity instanceof SmqBaseTarget) {
														String parentLevel5UpScope = parentLevel5UpHnode.getScope();
														if(StringUtils.isNoneBlank(parentLevel5UpScope) 
																&& (parentLevel5UpScope.equals("1") || parentLevel5UpScope.equals("2") 
																		|| parentLevel5UpScope.equals("3") || parentLevel5UpScope.equals("4"))) {
															scopeFilter = parentLevel5UpScope; 
														} else {
															//level 6 up
															TreeNode parentLevel6Up = expandedTreeNode.getParent().getParent().getParent().getParent().getParent();
															if(null != parentLevel6Up) {
																HierarchyNode parentLevel6UpHnode = (HierarchyNode) parentLevel6Up.getData();
																IEntity parentLevel6UpEntity = parentLevel6UpHnode.getEntity();
																if(parentLevel6UpEntity instanceof SmqBaseTarget) {
																	String parentLevel6UpScope = parentLevel6UpHnode.getScope();
																	if(StringUtils.isNoneBlank(parentLevel6UpScope) 
																			&& (parentLevel6UpScope.equals("1") || parentLevel6UpScope.equals("2") 
																					|| parentLevel6UpScope.equals("3") || parentLevel6UpScope.equals("4"))) {
																		scopeFilter = parentLevel6UpScope; 
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
		
		return scopeFilter;
	}
	
	private String getScopeFromExpandedTreeNodeInCurrentTable(TreeNode expandedTreeNode) {
		HierarchyNode hierarchyNode = (HierarchyNode) expandedTreeNode.getData();
		IEntity entity = hierarchyNode.getEntity();
		
		//scan upto 6 tree levels to find if we have an smq with a scope
		String scopeFilter = null;
		if(entity instanceof SmqBase190) {
			//level 1 up
			String parentLevel1Up = hierarchyNode.getScope();
			if(StringUtils.isNoneBlank(parentLevel1Up) 
					&& (parentLevel1Up.equals("1") || parentLevel1Up.equals("2") 
							|| parentLevel1Up.equals("3") || parentLevel1Up.equals("4"))) {
				scopeFilter = parentLevel1Up; 
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
						scopeFilter = parentLevel2UpScope; 
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
									scopeFilter = parentLevel3UpScope; 
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
												scopeFilter = parentLevel4UpScope; 
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
															scopeFilter = parentLevel5UpScope; 
														} else {
															//level 6 up
															TreeNode parentLevel6Up = expandedTreeNode.getParent().getParent().getParent().getParent().getParent();
															if(null != parentLevel6Up) {
																HierarchyNode parentLevel6UpHnode = (HierarchyNode) parentLevel6Up.getData();
																IEntity parentLevel6UpEntity = parentLevel6UpHnode.getEntity();
																if(parentLevel6UpEntity instanceof SmqBase190) {
																	String parentLevel6UpScope = parentLevel6UpHnode.getScope();
																	if(StringUtils.isNoneBlank(parentLevel6UpScope) 
																			&& (parentLevel6UpScope.equals("1") || parentLevel6UpScope.equals("2") 
																					|| parentLevel6UpScope.equals("3") || parentLevel6UpScope.equals("4"))) {
																		scopeFilter = parentLevel6UpScope; 
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
		
		return scopeFilter;
	}
	
	public static String[] getAllLevelH() {
		return new String[] {
			"PRO",
			"SMQ1", "SMQ2", "SMQ3", "SMQ4", "SMQ5",
			"SOC", "HLGT", "HLT", "PT", "LLT"
		};
	}
	
	/**
	 * find a TreeNode by a given Entity ID
	 * Recursive function
	 * @author andmiel81@yandex.com
	 * @param rtNode root node of the subtree to be searched in.
	 * @param entityId Entity ID to be searched for
	 */
	public TreeNode findTreenodeByEntityId(TreeNode rtNode, long entityId) {
		if(rtNode!=null) {
			for(TreeNode chNode: rtNode.getChildren()) {
				if(chNode!=null && chNode.getData()!=null
						&& chNode.getData() instanceof HierarchyNode
						&& ((HierarchyNode)chNode.getData()).getEntity()!=null
						&& ((HierarchyNode)chNode.getData()).getEntity().getId() == entityId) {
					return chNode;
				} else {
					TreeNode f = findTreenodeByEntityId(chNode, entityId);
					if(f!=null)
						return f;
				}
			}
		}
		return null;
	}
	
	
	public HierarchyNode createCmqBaseCurrentHierarchyNode(CmqBase190 cmqBaseCurrent) {
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
	
	public HierarchyNode createCmqBaseTargetHierarchyNode(CmqBaseTarget cmqBaseTarget) {
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
	
	public HierarchyNode createSmqBaseCurrrentNode(CmqRelation190 cmqRelation, SmqBase190 smqBase) {
		HierarchyNode node = null;
		if(null != smqBase) {
			node = new HierarchyNode();
            if (null != smqBase.getSmqLevel()) 
                node.setLevel(SMQLevelHelper.getLabel(smqBase.getSmqLevel()));
			node.setTerm(smqBase.getSmqName());
			node.setCode(smqBase.getSmqCode().toString());
			node.setEntity(smqBase);
            if(cmqRelation != null) {
                node.setCategory((cmqRelation.getTermCategory() == null) ? "" : cmqRelation.getTermCategory());
                node.setScope((cmqRelation.getTermScope() == null) ? "" : cmqRelation.getTermScope());
                node.setWeight((cmqRelation.getTermWeight() == null) ? "" : cmqRelation.getTermWeight() + "");
            }
		}
		return node;
	}
	
	public HierarchyNode createSmqBaseTargetNode(SmqBaseTarget smqBase, CmqRelationTarget cmqRelation) {
		HierarchyNode node = null;
		if(null != smqBase) {
			node = new HierarchyNode();
            if (null != smqBase.getSmqLevel()) 
                node.setLevel(SMQLevelHelper.getLabel(smqBase.getSmqLevel()));
			node.setTerm(smqBase.getSmqName());
			node.setCode(smqBase.getSmqCode().toString());
			node.setEntity(smqBase);
            node.setRelationEntity(cmqRelation);
            if(cmqRelation != null) {
                node.setCategory((cmqRelation.getTermCategory() == null) ? "" : cmqRelation.getTermCategory());
                node.setScope((cmqRelation.getTermScope() == null) ? "" : cmqRelation.getTermScope());
                node.setWeight((cmqRelation.getTermWeight() == null) ? "" : cmqRelation.getTermWeight() + "");
            }
		}
		return node;
	}
	
	public HierarchyNode createSmqBaseTargetReverseNode(SMQReverseHierarchySearchDto smqBase) {
		HierarchyNode node = null;
		if(null != smqBase) {
			node = new HierarchyNode();
            if (null != smqBase.getSmqLevel()) 
                node.setLevel(SMQLevelHelper.getLabel(Integer.parseInt(smqBase.getSmqLevel())));
			node.setTerm(smqBase.getSmqName());
			node.setCode(smqBase.getSmqCode().toString());
			node.setEntity(smqBase);
		}
		return node;
	}
	
	public HierarchyNode createSmqRelationCurrentNode(SmqRelation190 smqRelation) {
		HierarchyNode node = new HierarchyNode();
		if (smqRelation.getSmqLevel() == 1) {
			node.setLevel("SMQ1");
		} else if (smqRelation.getSmqLevel() == 2) {
			node.setLevel("SMQ2");
		} else if (smqRelation.getSmqLevel() == 3) {
			node.setLevel("SMQ3");
		} else if ((smqRelation.getSmqLevel() == 4)
				|| (smqRelation.getSmqLevel() == 0)
				|| (smqRelation.getSmqLevel() == 5)) {
			node.setLevel("PT");
            node.setScope(null != smqRelation.getPtTermScope() ? smqRelation.getPtTermScope().toString() : "");
            node.setCategory(null != smqRelation.getPtTermCategory() ? smqRelation.getPtTermCategory() : "");
            node.setWeight(null != smqRelation.getPtTermWeight()? smqRelation.getPtTermWeight().toString() : "");
		}
		node.setTerm(smqRelation.getPtName());
		node.setCode(smqRelation.getPtCode()
				.toString());
		node.setEntity(smqRelation);
		return node;
	}
	
	public HierarchyNode createSmqRelationTargetNode(SmqRelationTarget smqRelationTarget) {
		HierarchyNode node = new HierarchyNode();
		if (smqRelationTarget.getSmqLevel() == 1) {
			node.setLevel("SMQ1");
		} else if (smqRelationTarget.getSmqLevel() == 2) {
			node.setLevel("SMQ2");
		} else if (smqRelationTarget.getSmqLevel() == 3) {
			node.setLevel("SMQ3");
		} else if ((smqRelationTarget.getSmqLevel() == 4)
				|| (smqRelationTarget.getSmqLevel() == 0)
				|| (smqRelationTarget.getSmqLevel() == 5)) {
			node.setLevel("PT");
            node.setScope(null != smqRelationTarget.getPtTermScope() ? smqRelationTarget.getPtTermScope().toString() : "");
            node.setCategory(null != smqRelationTarget.getPtTermCategory() ? smqRelationTarget.getPtTermCategory() : "");
            node.setWeight(null != smqRelationTarget.getPtTermWeight()? smqRelationTarget.getPtTermWeight().toString() : "");
		}
		node.setTerm(smqRelationTarget.getPtName());
		node.setCode(smqRelationTarget.getPtCode()
				.toString());
		node.setEntity(smqRelationTarget);
		return node;
	}
	
	public HierarchyNode createMeddraNode(
			MeddraDictHierarchySearchDto searchDto, String level) {
		HierarchyNode node = new HierarchyNode();
		node.setLevel(level);
		node.setTerm(searchDto.getTerm());
		node.setCode(searchDto.getCode());
		node.setEntity(searchDto);
		return node;
	}
	
	public HierarchyNode createMeddraReverseNode(
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
    
    public void setCurrentCmqBaseNodeStyle(HierarchyNode node, CmqBase190 cmq) {
        if (CSMQBean.IMPACT_TYPE_IMPACTED.equalsIgnoreCase(cmq.getImpactType()))
            node.setRowStyleClass("blue-colored");
        if (CmqBase190.CMQ_STATUS_VALUE_INACTIVE.equals(cmq.getCmqStatus()))
            node.setRowStyleClass("brown-colored");
    }
    
    public void setTargetCmqBaseNodeStyle(HierarchyNode node, CmqBaseTarget cmq) {
        if (CSMQBean.IMPACT_TYPE_IMPACTED.equalsIgnoreCase(cmq.getImpactType()))
            node.setRowStyleClass("blue-colored");
        else if(CSMQBean.IMPACT_TYPE_ICC.equalsIgnoreCase(cmq.getImpactType())
                || CSMQBean.IMPACT_TYPE_IPC.equalsIgnoreCase(cmq.getImpactType()))
            node.setRowStyleClass("darkgrey-colored");
    }
    
    public void setTargetCmqRelationNodeStyle(HierarchyNode node, CmqRelationTarget cmqRelationTarget) {
		if ((null != cmqRelationTarget) && (cmqRelationTarget.getRelationImpactType() != null)) {
			if("MQM".equalsIgnoreCase(cmqRelationTarget.getRelationImpactType())) {
				node.setRowStyleClass("green-colored");
			} else if("NCH".equals(cmqRelationTarget.getRelationImpactType())) {
				node.setRowStyleClass("italic");
			} else if(StringUtils.equalsAny(cmqRelationTarget.getRelationImpactType(),
                    "PDL","PDH","HDH","HDS","LDP","LPP","HPP","NTR")) {
				node.setRowStyleClass("orange-colored");
			} else if("LCN".equals(cmqRelationTarget.getRelationImpactType())) {
				node.setRowStyleClass("mauve-colored");
            } else if ("SCH".equals(cmqRelationTarget.getRelationImpactType())) {
				node.setRowStyleClass("blue-colored");
            }
		}
	}
    
	public void setCurrentCmqRelationNodeStyle(HierarchyNode node,	CmqRelation190 cmqRelation) {
		if (cmqRelation!= null && cmqRelation.getRelationImpactType() != null) {
			if("NCH".equals(cmqRelation.getRelationImpactType())) {
				node.setRowStyleClass("italic");
			} else if(StringUtils.equalsAny(cmqRelation.getRelationImpactType(),
                    "PDL","PDH","HDH","HDS","DTR","MRG","LCN","HPP","LPP","HNP","LDP")) {
				node.setRowStyleClass("red-colored");
			} else if (StringUtils.equalsAny(cmqRelation.getRelationImpactType(), "SCH","ICC"))
				node.setRowStyleClass("blue-colored");
		}
	}
    
    public void setCurrentMeddraColors(List<MeddraDictHierarchySearchDto> meddras, Map<Long, TreeNode> nodes, Map<Long, IEntity> cmqRelationsMap) {
        List<Long> socCodes = new LinkedList<>();
        List<Long> hlgtCodes = new LinkedList<>();
        List<Long> hltCodes = new LinkedList<>();
        List<Long> ptCodes = new LinkedList<>();
        for (MeddraDictHierarchySearchDto m : meddras) {
            IEntity relationEntity = cmqRelationsMap!=null? cmqRelationsMap.get(Long.valueOf(m.getCode())) : null;
            if(relationEntity != null && relationEntity instanceof CmqRelation190
                    && ((CmqRelation190)relationEntity).getRelationImpactType() == null) {
                if (m.getSocCode() != null)
                    socCodes.add(Long.parseLong(m.getSocCode()));
                if (m.getHlgtCode() != null)
                    hlgtCodes.add(Long.parseLong(m.getHlgtCode()));
                if (m.getHltCode() != null)
                    hltCodes.add(Long.parseLong(m.getHltCode()));
                if (m.getPtCode() != null)
                    ptCodes.add(Long.parseLong(m.getPtCode()));
            }
        }
        
        List<MeddraDictHierarchySearchDto> socMeddras = meddraDictCurrentService.findChildrenByParentCodes("HLGT_", "SOC_",  socCodes);
        List<MeddraDictHierarchySearchDto> hlgtMeddras = meddraDictCurrentService.findChildrenByParentCodes("HLT_", "HLGT_", hlgtCodes);
        List<MeddraDictHierarchySearchDto> hltMeddras = meddraDictCurrentService.findChildrenByParentCodes("PT_", "HLT_", hltCodes);
        List<MeddraDictHierarchySearchDto> ptMeddras = meddraDictCurrentService.findChildrenByParentCodes("LLT_", "PT_", ptCodes);
        
        // convert lists to map
        Map<Long, List<MeddraDictHierarchySearchDto>> socMeddraMap = new HashMap<>();
        Map<Long, List<MeddraDictHierarchySearchDto>> hlgtMeddraMap = new HashMap<>();
        Map<Long, List<MeddraDictHierarchySearchDto>> hltMeddraMap = new HashMap<>();
        Map<Long, List<MeddraDictHierarchySearchDto>> ptMeddraMap = new HashMap<>();
        
        for(MeddraDictHierarchySearchDto m: socMeddras) {
            final Long c = Long.valueOf(m.getSocCode());
            if(socMeddraMap.get(c) == null)
                socMeddraMap.put(c, new ArrayList<MeddraDictHierarchySearchDto>());
            m.setSocCode(null);
            socMeddraMap.get(c).add(m);
        }
        for(MeddraDictHierarchySearchDto m: hlgtMeddras) {
            final Long c = Long.valueOf(m.getHlgtCode());
            if(hlgtMeddraMap.get(c) == null)
                hlgtMeddraMap.put(c,  new ArrayList<MeddraDictHierarchySearchDto>());
            m.setHlgtCode(null);
            hlgtMeddraMap.get(c).add(m);
        }
        for(MeddraDictHierarchySearchDto m: hltMeddras) {
            final Long c = Long.valueOf(m.getHltCode());
            if(hltMeddraMap.get(c) == null)
                hltMeddraMap.put(c,  new ArrayList<MeddraDictHierarchySearchDto>());
            m.setHltCode(null);
            hltMeddraMap.get(c).add(m);
        }
        for(MeddraDictHierarchySearchDto m: ptMeddras) {
            final Long c = Long.valueOf(m.getPtCode());
            if(ptMeddraMap.get(c) == null)
                ptMeddraMap.put(c,  new ArrayList<MeddraDictHierarchySearchDto>());
            m.setPtCode(null);
            ptMeddraMap.get(c).add(m);
        }
        
        socCodes = hlgtCodes = hltCodes = ptCodes= null;
        socMeddras = hlgtMeddras = hltMeddras = ptMeddras = null;
        
        for (MeddraDictHierarchySearchDto m : meddras) {
            Map<String, List<MeddraDictHierarchySearchDto>> chMeddras = new HashMap<>();
            chMeddras.put("SOC/HLGT", null);
            chMeddras.put("HLGT/HLT", null);
            chMeddras.put("HLT/PT", null);
            chMeddras.put("PT/LLT", null);
            
            IEntity relationEntity = cmqRelationsMap!=null? cmqRelationsMap.get(Long.valueOf(m.getCode())) : null;
            if(relationEntity instanceof CmqRelation190 && ((CmqRelation190)relationEntity).getRelationImpactType() == null) {
                if (m.getSocCode() != null) {
                    chMeddras.put("SOC/HLGT", socMeddraMap.get(Long.valueOf(m.getSocCode())));
                }
                if (m.getHlgtCode() != null) {
                    chMeddras.put("HLGT/HLT", hlgtMeddraMap.get(Long.valueOf(m.getHlgtCode())));
                }
                if (m.getHltCode() != null) {
                    chMeddras.put("HLT/PT", hltMeddraMap.get(Long.valueOf(m.getHltCode())));
                }
                if (m.getPtCode() != null){
                    chMeddras.put("PT/LLT", ptMeddraMap.get(Long.valueOf(m.getPtCode())));
                }
            }
            
            Long code = Long.valueOf(m.getCode());
            HierarchyNode hnode = (HierarchyNode)nodes.get(code).getData();
            
            setCurrentMeddraColor(m, hnode, cmqRelationsMap.get(code), chMeddras);
        }
    }

	public void setCurrentMeddraColor(MeddraDictHierarchySearchDto meddra, HierarchyNode node, IEntity relationEntity, Map<String, List<MeddraDictHierarchySearchDto>> chMeddras) {	
        if (relationEntity != null && relationEntity instanceof CmqRelation190
                && ((CmqRelation190) relationEntity).getRelationImpactType() == null) {
            // RelationImpactType is null, so check for any indirect impact
            
            //Blue color on relation SOC level
            if (meddra.getSocCode() != null && chMeddras.get("SOC/HLGT") != null) {
                List<MeddraDictHierarchySearchDto> list = chMeddras.get("SOC/HLGT");
                for (MeddraDictHierarchySearchDto child : list)
                if (child.getMergedHlgt() != null ||child.getMovedHlgt() != null || child.getHlgtNameChanged() != null) {
                    node.setRowStyleClass("blue-colored");
                    break;
                }
            }
            //Blue color on relation HLGT level
            if (meddra.getHlgtCode() != null && chMeddras.get("HLGT/HLT") != null) {
                List<MeddraDictHierarchySearchDto> list = chMeddras.get("HLGT/HLT");
                for (MeddraDictHierarchySearchDto child : list)
                if (child.getMergedHlt() != null || child.getMovedHlt() != null || child.getHltNameChanged() != null) {
                    node.setRowStyleClass("blue-colored");
                    break;
                }
            }
            //Blue color on relation HLT level
            if (meddra.getHltCode() != null && chMeddras.get("HLT/PT") != null) { 
                List<MeddraDictHierarchySearchDto> list = chMeddras.get("HLT/PT");
                for (MeddraDictHierarchySearchDto child : list)
                if (child.getMovedPt() != null || child.getPromotedPt() != null || child.getDemotedPt() != null || child.getPtNameChanged() != null) {
                    node.setRowStyleClass("blue-colored");
                    break;
                }
            }			
            //Blue color on relation PT level
            if (meddra.getPtCode() != null && chMeddras.get("PT/LLT") != null) {
                List<MeddraDictHierarchySearchDto> list = chMeddras.get("PT/LLT");
                for (MeddraDictHierarchySearchDto child : list)
                if (child.getMovedLlt() != null || child.getPromotedLlt() != null || child.getDemotedLlt() != null || child.getLltNameChanged() != null) {
                    node.setRowStyleClass("blue-colored");
                    break;
                }
            }
        } else {
            // RelationImpactType is NOT null, meaning has some direct impact on others
            if(("LDH".equalsIgnoreCase(meddra.getMovedPt()) && (meddra.getPtCode() != null))
                    || ("LDP".equalsIgnoreCase(meddra.getMovedLlt()) && meddra.getLltCode() != null)
                    || ("HDH".equalsIgnoreCase(meddra.getMovedHlt()) && meddra.getHltCode() != null)
                    || ("HDS".equalsIgnoreCase(meddra.getMovedHlgt()) && meddra.getHlgtCode() != null)
                    || ("PDL".equalsIgnoreCase(meddra.getDemotedPt()) && meddra.getPtCode() != null)
                    || ("LPP".equalsIgnoreCase(meddra.getPromotedLlt()) && meddra.getLltCode() != null)
                    || ("HPN".equalsIgnoreCase(meddra.getPrimarySocChange()) && (meddra.getPtCode() != null || meddra.getLltCode() != null))
                    || ("HPP".equalsIgnoreCase(meddra.getPrimarySocChange()) && (meddra.getPtCode() != null || meddra.getLltCode() != null))
                    || ("MRG".equalsIgnoreCase(meddra.getMergedHlgt()) && (meddra.getHlgtCode() != null))
                    || ("MRG".equalsIgnoreCase(meddra.getMergedHlt()) && (meddra.getHltCode() != null))) {
                node.setRowStyleClass("red-colored");
            }  if(("NCH".equalsIgnoreCase(meddra.getHlgtNameChanged()) && meddra.getHlgtCode() != null)
                    || ("NCH".equalsIgnoreCase(meddra.getHltNameChanged()) && meddra.getHltCode() != null)
                    || ("NCH".equalsIgnoreCase(meddra.getPtNameChanged()) && meddra.getPtCode() != null)
                    || ("NCH".equalsIgnoreCase(meddra.getSocNameChanged()) && meddra.getSocCode() != null)
                    || ("NCH".equalsIgnoreCase(meddra.getLltNameChanged()) && meddra.getLltCode() != null)) {
                node.setRowStyleClass("italic");
            }
        }
	}
    
    public void setTargetMeddraColors(List<MeddraDictHierarchySearchDto> meddras, Map<Long, TreeNode> nodes, Map<Long, IEntity> cmqRelationsMap) {
        List<Long> socCodes = new LinkedList<>();
        List<Long> hlgtCodes = new LinkedList<>();
        List<Long> hltCodes = new LinkedList<>();
        List<Long> ptCodes = new LinkedList<>();
        for (MeddraDictHierarchySearchDto m : meddras) {
            IEntity relationEntity = cmqRelationsMap!=null? cmqRelationsMap.get(Long.valueOf(m.getCode())) : null;
            if (relationEntity != null && relationEntity instanceof CmqRelationTarget
                    && ((CmqRelationTarget) relationEntity).getRelationImpactType() == null) {
                if (m.getSocCode() != null)
                    socCodes.add(Long.parseLong(m.getSocCode()));
                if (m.getHlgtCode() != null)
                    hlgtCodes.add(Long.parseLong(m.getHlgtCode()));
                if (m.getHltCode() != null)
                    hltCodes.add(Long.parseLong(m.getHltCode()));
                if (m.getPtCode() != null)
                    ptCodes.add(Long.parseLong(m.getPtCode()));
            }
        }
        
        List<MeddraDictHierarchySearchDto> socMeddras = meddraDictTargetService.findChildrenByParentCodes("HLGT_", "SOC_",  socCodes);
        List<MeddraDictHierarchySearchDto> hlgtMeddras = meddraDictTargetService.findChildrenByParentCodes("HLT_", "HLGT_", hlgtCodes);
        List<MeddraDictHierarchySearchDto> hltMeddras = meddraDictTargetService.findChildrenByParentCodes("PT_", "HLT_", hltCodes);
        List<MeddraDictHierarchySearchDto> ptMeddras = meddraDictTargetService.findChildrenByParentCodes("LLT_", "PT_", ptCodes);
        
        // convert lists to map
        Map<Long, List<MeddraDictHierarchySearchDto>> socMeddraMap = new HashMap<>();
        Map<Long, List<MeddraDictHierarchySearchDto>> hlgtMeddraMap = new HashMap<>();
        Map<Long, List<MeddraDictHierarchySearchDto>> hltMeddraMap = new HashMap<>();
        Map<Long, List<MeddraDictHierarchySearchDto>> ptMeddraMap = new HashMap<>();
        
        for(MeddraDictHierarchySearchDto m: socMeddras) {
            final Long c = Long.valueOf(m.getSocCode());
            if(socMeddraMap.get(c) == null)
                socMeddraMap.put(c,  new ArrayList<MeddraDictHierarchySearchDto>());
            m.setSocCode(null);
            socMeddraMap.get(c).add(m);
        }
        for(MeddraDictHierarchySearchDto m: hlgtMeddras) {
            final Long c = Long.valueOf(m.getHlgtCode());
            if(hlgtMeddraMap.get(c) == null)
                hlgtMeddraMap.put(c,  new ArrayList<MeddraDictHierarchySearchDto>());
            m.setHlgtCode(null);
            hlgtMeddraMap.get(c).add(m);
        }
        for(MeddraDictHierarchySearchDto m: hltMeddras) {
            final Long c = Long.valueOf(m.getHltCode());
            if(hltMeddraMap.get(c) == null)
                hltMeddraMap.put(c,  new ArrayList<MeddraDictHierarchySearchDto>());
            m.setHltCode(null);
            hltMeddraMap.get(c).add(m);
        }
        for(MeddraDictHierarchySearchDto m: ptMeddras) {
            final Long c = Long.valueOf(m.getPtCode());
            if(ptMeddraMap.get(c) == null)
                ptMeddraMap.put(c,  new ArrayList<MeddraDictHierarchySearchDto>());
            m.setPtCode(null);
            ptMeddraMap.get(c).add(m);
        }
        
        socCodes = hlgtCodes = hltCodes = ptCodes= null;
        socMeddras = hlgtMeddras = hltMeddras = ptMeddras = null;
        
        for (MeddraDictHierarchySearchDto m : meddras) {
            Map<String, List<MeddraDictHierarchySearchDto>> chMeddras = new HashMap<>();
            chMeddras.put("SOC/HLGT", null);
            chMeddras.put("HLGT/HLT", null);
            chMeddras.put("HLT/PT", null);
            chMeddras.put("PT/LLT", null);
            
            IEntity relationEntity = cmqRelationsMap!=null? cmqRelationsMap.get(Long.valueOf(m.getCode())) : null;
            if(relationEntity instanceof CmqRelationTarget && ((CmqRelationTarget)relationEntity).getRelationImpactType() == null) {
                if (m.getSocCode() != null) {
                    chMeddras.put("SOC/HLGT", socMeddraMap.get(Long.valueOf(m.getSocCode())));
                }
                if (m.getHlgtCode() != null) {
                    chMeddras.put("HLGT/HLT", hlgtMeddraMap.get(Long.valueOf(m.getHlgtCode())));
                }
                if (m.getHltCode() != null) {
                    chMeddras.put("HLT/PT", hltMeddraMap.get(Long.valueOf(m.getHltCode())));
                }
                if (m.getPtCode() != null){
                    chMeddras.put("PT/LLT", ptMeddraMap.get(Long.valueOf(m.getPtCode())));
                }
            }
            
            Long code = Long.valueOf(m.getCode());
            HierarchyNode hnode = (HierarchyNode)nodes.get(code).getData();
            
            setTargetMeddraColor(m, hnode, cmqRelationsMap.get(code), chMeddras);
        }
    }
	
	 public void setTargetMeddraColor(MeddraDictHierarchySearchDto meddra, HierarchyNode node, IEntity relationEntity, Map<String, List<MeddraDictHierarchySearchDto>> chMeddras) {
            if (relationEntity != null && relationEntity instanceof CmqRelationTarget
                    && ((CmqRelationTarget) relationEntity).getRelationImpactType() == null) {
                // RelationImpactType is NULL, meaning has no direct impact. So check for any indirect impact
                
                //Blue color on relation SOC level
                if (meddra.getSocCode() != null && chMeddras.get("SOC/HLGT") != null) {
                    List<MeddraDictHierarchySearchDto> list = chMeddras.get("SOC/HLGT");
                    for (MeddraDictHierarchySearchDto child : list)
                    if (child.getNewHlgt() != null || child.getMovedHlgt() != null || child.getHlgtNameChanged() != null) {
                        node.setRowStyleClass("blue-colored");
                        break;
                    }
                }
                //Blue color on relation HLGT level
                if (meddra.getHlgtCode() != null && chMeddras.get("HLGT/HLT") != null) {
                    List<MeddraDictHierarchySearchDto> list = chMeddras.get("HLGT/HLT");
                    for (MeddraDictHierarchySearchDto child : list)
                    if (child.getNewHlt() != null || child.getMovedHlt() != null || child.getHltNameChanged() != null) {
                        node.setRowStyleClass("blue-colored");
                        break;
                    }
                }
                //Blue color on relation HLT level
                if (meddra.getHltCode() != null && chMeddras.get("HLT/PT") != null) { 
                    List<MeddraDictHierarchySearchDto> list = chMeddras.get("HLT/PT");
                    for (MeddraDictHierarchySearchDto child : list)
                    if (child.getNewSuccessorPt() != null || child.getNewPt() != null || child.getMovedPt() != null || child.getPromotedPt() != null || child.getDemotedLlt() != null || child.getPtNameChanged() != null) {
                        node.setRowStyleClass("blue-colored");
                        break;
                    }
                }			
                //Blue color on relation PT level
                if (meddra.getPtCode() != null && chMeddras.get("PT/LLT") != null) { 
                    List<MeddraDictHierarchySearchDto> list = chMeddras.get("PT/LLT");
                    for (MeddraDictHierarchySearchDto child : list)
                    if (child.getNewLlt() != null || child.getMovedLlt() != null || child.getPromotedLlt() != null || child.getDemotedLlt() != null || child.getLltNameChanged() != null || child.getLltCurrencyChange() != null) {
                        node.setRowStyleClass("blue-colored");
                        break;
                    }
                }
            } else {
                // RelationImpactType is NOT NULL, meaning it has direct impact.
                
                if (("HPP".equalsIgnoreCase(meddra.getPrimarySocChange()) && meddra.getSocCode() != null)
                        || ("HNP".equalsIgnoreCase(meddra.getPrimarySocChange()) && meddra.getSocCode() != null)) {
                    node.setRowStyleClass("none");
                } else if ("LCN".equalsIgnoreCase(meddra.getLltCurrencyChange()) && meddra.getLltCode() != null) {
                    node.setRowStyleClass("mauve-colored");	
                } else if(("NTR".equalsIgnoreCase(meddra.getNewLlt()) && meddra.getLltCode() != null)
                        || ("NTR".equalsIgnoreCase(meddra.getNewPt()) && meddra.getPtCode() != null)
                        || ("NTR".equalsIgnoreCase(meddra.getNewHlt()) && meddra.getHltCode() != null)
                        || ("NTR".equalsIgnoreCase(meddra.getNewHlgt()) && meddra.getHlgtCode() != null)
                        || ("NTR".equalsIgnoreCase(meddra.getNewSoc()) && meddra.getSocCode() != null)
                        || (("NTR".equalsIgnoreCase(meddra.getMovedHlt()) || "HDH".equalsIgnoreCase(meddra.getMovedHlt())) && meddra.getHltCode() != null)
                        || (("NTR".equalsIgnoreCase(meddra.getMovedHlgt()) || "HDS".equalsIgnoreCase(meddra.getMovedHlgt())) && meddra.getHlgtCode() != null)
                        || ("LDH".equalsIgnoreCase(meddra.getMovedPt()) && meddra.getPtCode() != null)
                        || ("LDP".equalsIgnoreCase(meddra.getMovedLlt()))
                        || ("PDL".equalsIgnoreCase(meddra.getDemotedPt()) && meddra.getPtCode() != null)
                        || ("LPP".equalsIgnoreCase(meddra.getPromotedLlt()) && meddra.getLltCode() != null)
                        || ("SDP".equalsIgnoreCase(meddra.getNewSuccessorPt()) && meddra.getPtCode() != null)){
                    node.setRowStyleClass("orange-colored");
                }  if(("NCH".equalsIgnoreCase(meddra.getHlgtNameChanged()) && meddra.getHlgtCode() != null)
                        || ("NCH".equalsIgnoreCase(meddra.getHltNameChanged()) && meddra.getHltCode() != null)
                        || ("NCH".equalsIgnoreCase(meddra.getPtNameChanged()) && meddra.getPtCode() != null)
                        || ("NCH".equalsIgnoreCase(meddra.getSocNameChanged()) && meddra.getSocCode() != null)
                        || ("NCH".equalsIgnoreCase(meddra.getLltNameChanged()) && meddra.getLltCode() != null)) {
                    node.setRowStyleClass("italic");
                }
            }
		}
    
	public void setSMQCurrentNodeStyle(HierarchyNode childRelationNode, SmqRelation190 childRelation) {
		if (childRelation.getRelationImpactType() != null) {
            if("NCH".equals(childRelation.getRelationImpactType())) {
				childRelationNode.setRowStyleClass("italic");
			} else if("DTR".equals(childRelation.getRelationImpactType())) {
				childRelationNode.setRowStyleClass("red-colored");
			} else if (StringUtils.equalsAny(childRelation.getRelationImpactType(), "SCH", "ICC")) {
				childRelationNode.setRowStyleClass("blue-colored");	
            } else if ("SWC".equals(childRelation.getRelationImpactType())) {
				childRelationNode.setRowStyleClass("pink-colored");
            } else if("PTS".equals(childRelation.getRelationImpactType())) {
				childRelationNode.setRowStyleClass("text-bold");
			} else if("PSI".equals(childRelation.getRelationImpactType())) {
                childRelationNode.setRowStyleClass("orange-colored");
            }
		}
		else
			childRelationNode.setRowStyleClass("none");
	}

	public void setSMQTargetNodeStyle(HierarchyNode childRelationNode,	SmqRelationTarget childRelation) {
		if (childRelation.getRelationImpactType() != null) {
            if(StringUtils.equalsAny(childRelation.getRelationImpactType(), "LPP","PDL","NTR","PSA")) {
				childRelationNode.setRowStyleClass("orange-colored");
			} else if ("SCH".equals(childRelation.getRelationImpactType())) {
				childRelationNode.setRowStyleClass("blue-colored");
            } else if ("SWC".equals(childRelation.getRelationImpactType())) {
				childRelationNode.setRowStyleClass("pink-colored");
            } else if ("PSI".equals(childRelation.getRelationImpactType())) {
				childRelationNode.setRowStyleClass("red-colored");
            }
		}
		else
			childRelationNode.setRowStyleClass("none");
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private void populateSmqReverseDtoParent(Long smqCode, TreeNode expandedTreeNode, String smqType, String uiSourceOfEvent) {
		boolean isRootListNode = isRootListNode(expandedTreeNode);
		boolean isRootNodeOfSmqType = this.isRootNodeOfSmqType(expandedTreeNode);
        boolean currentList = ("current".equalsIgnoreCase(smqType));
		List<SMQReverseHierarchySearchDto> parentSmqBaseList = this.smqBaseTargetService.findReverseParentByChildCode(smqCode);

        if(CollectionUtils.isNotEmpty(parentSmqBaseList)) {
			Map<Long, TreeNode> smqTreeNodeMap = new HashMap<>();
			List<Long> smqChildCodeList = new ArrayList<>();
			for (SMQReverseHierarchySearchDto parentSmqBase : parentSmqBaseList) {
                final Long parentSmqCode = parentSmqBase.getSmqCode();
				HierarchyNode childNode = new HierarchyNode();
                if (null != parentSmqBase.getSmqLevel())
                    childNode.setLevel(SMQLevelHelper.getLabel(Integer.parseInt(parentSmqBase.getSmqLevel())));
                childNode.setTerm(parentSmqBase.getSmqName());
                childNode.setCode(parentSmqBase.getSmqCode().toString());
                childNode.setScope(null != parentSmqBase.getPtTermScope() ? parentSmqBase.getPtTermScope().toString() : "");
                childNode.setCategory(null != parentSmqBase.getPtTermCategory() ? parentSmqBase.getPtTermCategory() : "");
                childNode.setWeight(null != parentSmqBase.getPtTermWeight()? parentSmqBase.getPtTermWeight().toString() : "");
                childNode.setEntity(parentSmqBase);
                if(!isRootListNode && "target-table".equalsIgnoreCase(uiSourceOfEvent)) {
                    //childNode.markNotEditableInRelationstable();
                	childNode.markReadOnlyInRelationstable();
                }

				// add child to parent
				TreeNode childTreeNode = new DefaultTreeNode(childNode, expandedTreeNode);
                smqChildCodeList.add(parentSmqBase.getSmqCode());
				smqTreeNodeMap.put(parentSmqCode, childTreeNode);
			} // end of for
			
			//find smqrelations of all child smqs
            List<Map<String, Object>> childSmqRelationsCountList = this.smqBaseTargetService.findParentCountSmqCountByChildSmqCodes(smqChildCodeList);

            if((null != childSmqRelationsCountList) && (childSmqRelationsCountList.size() > 0)) {
                for(Map<String, Object> map : childSmqRelationsCountList) {
                    if(map.get("PT_CODE") != null) {
                        Long childSmqCode = (Long)map.get("PT_CODE");
                        Long count = (Long)map.get("COUNT");
                        if(count > 0) {
                            createNewDummyNode(smqTreeNodeMap.get(childSmqCode));
                        }
                    }
                }
            }
		}
	}
	
	public void populateSmqRelations(Long smqCode, TreeNode expandedTreeNode, String smqType, String uiSourceOfEvent, String scopeFilter) {
		boolean isRootListNode = isRootListNode(expandedTreeNode);
		boolean isRootNodeOfSmqType = this.isRootNodeOfSmqType(expandedTreeNode);
        boolean currentList = ("current".equalsIgnoreCase(smqType));
		List<? extends IEntity> childRelations = null;
 		if(currentList) {
 			if (StringUtils.isNotBlank(scopeFilter) && (scopeFilter.equals(CSMQBean.SCOPE_NARROW) || scopeFilter.equals(CSMQBean.SCOPE_BROAD)
		   			 || scopeFilter.equals(CSMQBean.SCOPE_FULL))) {
	    		childRelations = this.smqBaseCurrentService.findSmqRelationsForSmqCodeAndScope(smqCode, scopeFilter);
	    	} else {
	    		childRelations = this.smqBaseCurrentService.findSmqRelationsForSmqCode(smqCode);
	    	}
		} else {
			if (StringUtils.isNotBlank(scopeFilter) && (scopeFilter.equals(CSMQBean.SCOPE_NARROW) || scopeFilter.equals(CSMQBean.SCOPE_BROAD)
		   			 || scopeFilter.equals(CSMQBean.SCOPE_FULL))) {
	    		childRelations = this.smqBaseTargetService.findSmqRelationsForSmqCodeAndScope(smqCode, scopeFilter);
	    	} else {
	    		childRelations = this.smqBaseTargetService.findSmqRelationsForSmqCode(smqCode);
	    	}
  		}

		if (null != childRelations) {
            Map<Long, HierarchyNode> childSmqNodes = new HashMap<>();
			for (IEntity entity : childRelations) {
				boolean isChildSmqNode = false;
				HierarchyNode childRelationNode = new HierarchyNode();
				if(currentList) {
					SmqRelation190 childRelation = (SmqRelation190) entity;
					if (childRelation.getSmqLevel() == 0) {
						SmqBase190 childSmq = new SmqBase190();
						childSmq.setSmqCode(childRelation.getPtCode().longValue());
						childSmq.setSmqName(childRelation.getPtName());
						childRelationNode.setLevel("Child SMQ");
						childRelationNode.setEntity(childSmq);
						isChildSmqNode = true;
                        childSmqNodes.put(childSmq.getSmqCode(), childRelationNode);
					} else if (childRelation.getSmqLevel() == 1) {
						childRelationNode.setLevel("SMQ1");
						childRelationNode.setEntity(childRelation);
					} else if (childRelation.getSmqLevel() == 2) {
						childRelationNode.setLevel("SMQ2");
						childRelationNode.setEntity(childRelation);
					} else if (childRelation.getSmqLevel() == 3) {
						childRelationNode.setLevel("SMQ3");
						childRelationNode.setEntity(childRelation);
					} else if (childRelation.getSmqLevel() == 4) {
						childRelationNode.setLevel("PT");
                        childRelationNode.setScope(null != childRelation.getPtTermScope() ? childRelation.getPtTermScope().toString() : "");
                        childRelationNode.setCategory(null != childRelation.getPtTermCategory() ? childRelation.getPtTermCategory() : "");
                        childRelationNode.setWeight(null != childRelation.getPtTermWeight()? childRelation.getPtTermWeight().toString() : "");
                        childRelationNode.setEntity(childRelation);
					} else if (childRelation.getSmqLevel() == 5) {
						childRelationNode.setLevel("LLT");
                        childRelationNode.setScope(null != childRelation.getPtTermScope() ? childRelation.getPtTermScope().toString() : "");
                        childRelationNode.setCategory(null != childRelation.getPtTermCategory() ? childRelation.getPtTermCategory() : "");
                        childRelationNode.setWeight(null != childRelation.getPtTermWeight()? childRelation.getPtTermWeight().toString() : "");
                        childRelationNode.setEntity(childRelation);
					}
					childRelationNode.setTerm(childRelation.getPtName());
					childRelationNode.setCode(childRelation.getPtCode().toString());
					
					//Set Color
					setSMQCurrentNodeStyle(childRelationNode, childRelation);
				} else {
					//for target here
					SmqRelationTarget childRelation = (SmqRelationTarget) entity;
					if (childRelation.getSmqLevel() == 0) {
						SmqBaseTarget childSmq = new SmqBaseTarget();
						childSmq.setSmqCode(childRelation.getPtCode().longValue());
						childSmq.setSmqName(childRelation.getPtName());
						childRelationNode.setScope(null != childRelation.getPtTermScope() ? childRelation.getPtTermScope().toString() : "");
                        childRelationNode.setCategory(null != childRelation.getPtTermCategory() ? childRelation.getPtTermCategory() : "");
                        childRelationNode.setWeight(null != childRelation.getPtTermWeight()? childRelation.getPtTermWeight().toString() : "");
						childRelationNode.setLevel("Child SMQ");
						childRelationNode.setEntity(childSmq);
						isChildSmqNode = true;
                        childSmqNodes.put(childSmq.getSmqCode(), childRelationNode);
					} else if (childRelation.getSmqLevel() == 1) {
						childRelationNode.setLevel("SMQ1");
						childRelationNode.setEntity(childRelation);
					} else if (childRelation.getSmqLevel() == 2) {
						childRelationNode.setLevel("SMQ2");
						childRelationNode.setEntity(childRelation);
					} else if (childRelation.getSmqLevel() == 3) {
						childRelationNode.setLevel("SMQ3");
						childRelationNode.setEntity(childRelation);
					} else if (childRelation.getSmqLevel() == 4) {
						childRelationNode.setLevel("PT");
                        childRelationNode.setScope(null != childRelation.getPtTermScope() ? childRelation.getPtTermScope().toString() : "");
                        childRelationNode.setCategory(null != childRelation.getPtTermCategory() ? childRelation.getPtTermCategory() : "");
                        childRelationNode.setWeight(null != childRelation.getPtTermWeight()? childRelation.getPtTermWeight().toString() : "");
                        childRelationNode.setEntity(childRelation);
					}else if (childRelation.getSmqLevel() == 5) {
						childRelationNode.setLevel("LLT");
                        childRelationNode.setScope(null != childRelation.getPtTermScope() ? childRelation.getPtTermScope().toString() : "");
                        childRelationNode.setCategory(null != childRelation.getPtTermCategory() ? childRelation.getPtTermCategory() : "");
                        childRelationNode.setWeight(null != childRelation.getPtTermWeight()? childRelation.getPtTermWeight().toString() : "");
                        childRelationNode.setEntity(childRelation);
					}
					childRelationNode.setTerm(childRelation.getPtName());
					childRelationNode.setCode(childRelation.getPtCode().toString());
					
					if(!isRootListNode && "target-table".equalsIgnoreCase(uiSourceOfEvent)) {
						//childRelationNode.markNotEditableInRelationstable();
						childRelationNode.markReadOnlyInRelationstable();
					}
					if(isRootNodeOfSmqType) {
						childRelationNode.setHideDelete(true);
						childRelationNode.markReadOnlyInRelationstable();
					}
					//Set Color
					setSMQTargetNodeStyle(childRelationNode, childRelation);
					
				}
				
				TreeNode treeNode = new DefaultTreeNode(childRelationNode, expandedTreeNode);
				if(isChildSmqNode) {
					
					this.createNewDummyNode(treeNode);
				}
			}
            if(!childSmqNodes.isEmpty()) {
            	if(!childSmqNodes.keySet().isEmpty()) {
            		if(currentList) {
            			List<Long> smqChildCodeList = new ArrayList<>(childSmqNodes.keySet());
                        List<SmqBase190> childSmqs = smqBaseCurrentService.findByCodes(new ArrayList<>(smqChildCodeList));
                        //find child smqs for this one and add a C in fornt of name if it has
        				List<Map<String, Object>> smqChildRelationsCountList = smqBaseCurrentService.findSmqChildRelationsCountForSmqCodes(smqChildCodeList);
                        if(childSmqs != null) {
                            for(SmqBase190 cs : childSmqs) {
                                if(CSMQBean.IMPACT_TYPE_IMPACTED.equals(cs.getImpactType())) {
                                    HierarchyNode hn = childSmqNodes.get(cs.getSmqCode());
                                    if(hn != null) {
                                        hn.setEntity(cs);
                                        hn.setRowStyleClass("blue-colored");
                                    }
                                }
                            }
                        }
                        if ((null != smqChildRelationsCountList)
        						&& (smqChildRelationsCountList.size() > 0)) {
        					for (Map<String, Object> map : smqChildRelationsCountList) {
        						if (map.get("SMQ_CODE") != null) {
        							Long childSmqCode = (Long) map.get("SMQ_CODE");
        							if ((Long) map.get("COUNT") > 0) {
        								HierarchyNode hierarchyNode = childSmqNodes.get(childSmqCode);
        								String level = hierarchyNode.getLevel();
        								hierarchyNode.setLevel("'C' " + level);
        							}
        						}
        					}
        				}
                    } else {
                    	List<Long> smqChildCodeList = new ArrayList<>(childSmqNodes.keySet());
                        List<SmqBaseTarget> childSmqs = smqBaseTargetService.findByCodes(new ArrayList<>(smqChildCodeList));
                        //find child smqs for this one and add a C in fornt of name if it has
        				List<Map<String, Object>> smqChildRelationsCountList = smqBaseTargetService.findSmqChildRelationsCountForSmqCodes(smqChildCodeList);
                        if(childSmqs != null) {
                            for(SmqBaseTarget cs : childSmqs) {
                                if(CSMQBean.IMPACT_TYPE_IMPACTED.equals(cs.getImpactType())) {
                                    HierarchyNode hn = childSmqNodes.get(cs.getSmqCode());
                                    if(hn != null) {
                                        hn.setEntity(cs);
                                        hn.setRowStyleClass("blue-colored");
                                    }
                                }
                            }
                        }
                        if ((null != smqChildRelationsCountList)
        						&& (smqChildRelationsCountList.size() > 0)) {
        					for (Map<String, Object> map : smqChildRelationsCountList) {
        						if (map.get("SMQ_CODE") != null) {
        							Long childSmqCode = (Long) map.get("SMQ_CODE");
        							if ((Long) map.get("COUNT") > 0) {
        								HierarchyNode hierarchyNode = childSmqNodes.get(childSmqCode);
        								String level = hierarchyNode.getLevel();
        								hierarchyNode.setLevel("'C' " + level);
        							}
        						}
        					}
        				}
                    }
            	}
            }//end of if(!childSmqNodes.isEmpty())
		}
	}
	
	
	public TreeNode populateSmqTreeNode(IEntity entity, TreeNode expandedTreeNode, String cmqType, Long parentCode, String uiSourceOfEvent) {
		boolean isRootListNode = isRootListNode(expandedTreeNode);
		IEntity entity2 = null;
		HierarchyNode node = null;
		TreeNode treeNode = null;
		boolean isSmqRelation = false;
		if("current".equalsIgnoreCase(cmqType)) {
			CmqRelation190 cmqRelation = (CmqRelation190) entity;
			//check if it is a PT relation of smq or not
			if((cmqRelation.getPtCode() != null) && (cmqRelation.getPtCode().longValue() > 0)) {
				entity2 = this.smqBaseCurrentService.findSmqRelationBySmqAndPtCode(cmqRelation.getSmqCode(), cmqRelation.getPtCode().intValue());
				node = this.createSmqRelationCurrentNode((SmqRelation190) entity2);
				isSmqRelation = true;
			} else {
				entity2 = this.smqBaseCurrentService.findByCode(cmqRelation.getSmqCode());
				node = this.createSmqBaseCurrrentNode(cmqRelation, (SmqBase190) entity2);
			}
			//Color for node
			setCurrentCmqRelationNodeStyle(node, cmqRelation);
		} else {
			CmqRelationTarget cmqRelation = (CmqRelationTarget) entity;
			if((cmqRelation.getPtCode() != null) && (cmqRelation.getPtCode().longValue() > 0)) {
				entity2 = this.smqBaseTargetService.findSmqRelationBySmqAndPtCode(cmqRelation.getSmqCode(), cmqRelation.getPtCode().intValue());
				node = this.createSmqRelationTargetNode((SmqRelationTarget) entity2);
				isSmqRelation = true;
			} else {
				entity2 = this.smqBaseTargetService.findByCode(cmqRelation.getSmqCode());
				node = this.createSmqBaseTargetNode((SmqBaseTarget) entity2, cmqRelation);
			}
			if(!isRootListNode && "target-table".equalsIgnoreCase(uiSourceOfEvent)) {
				node.markNotEditableInRelationstable();
			}
			//color for node
			setTargetCmqRelationNodeStyle(node, cmqRelation); 
		}
        
		if(null != node) {	
			treeNode = new DefaultTreeNode(node, expandedTreeNode);
			
			//if thsi is not an SQM relation node then its an SMQ node so check for rleations.
			if(!isSmqRelation) {
				//add a dummy node for either of the cases, expansion will handle the actuals later
				Long smqBaseChildrenCount;
				if("current".equalsIgnoreCase(cmqType)) {
					smqBaseChildrenCount = this.smqBaseCurrentService.findChildSmqCountByParentSmqCode(((SmqBase190)entity2).getSmqCode());
				} else {
					smqBaseChildrenCount = this.smqBaseTargetService.findChildSmqCountByParentSmqCode(((SmqBaseTarget)entity2).getSmqCode());
				}
				if((null != smqBaseChildrenCount) && (smqBaseChildrenCount > 0)) {
					// add a dummmy node to show expand arrow
					createNewDummyNode(treeNode);
				} else {
					Long childSmqrelationsCount;
					if("current".equalsIgnoreCase(cmqType)) {
						childSmqrelationsCount = this.smqBaseCurrentService.findSmqRelationsCountForSmqCode(((SmqBase190)entity2).getSmqCode());
					} else {
						childSmqrelationsCount = this.smqBaseTargetService.findSmqRelationsCountForSmqCode(((SmqBaseTarget)entity2).getSmqCode());
					}
					if((null != childSmqrelationsCount) && (childSmqrelationsCount > 0)) {
						// add a dummmy node to show expand arrow
						createNewDummyNode(treeNode);
					}
				}
			}
		}
		
		return treeNode;
	}
	
	public void populateCmqRelationTreeNodes(List<MeddraDictHierarchySearchDto> dtos, TreeNode expandedTreeNode
			, String nodeType, String childNodeType, String cmqType, Long parentCode, Map<Long, IEntity> cmqRelationsMap, String uiSourceOfEvent, IEntity entityExpanded) {
		boolean isRootListNode = isRootListNode(expandedTreeNode);
        Map<Long, TreeNode> addedNodes = new HashMap<>();
        List<Long> dtoCodes = new ArrayList<>(dtos.size());
        boolean bCurrentList = "current".equalsIgnoreCase(cmqType);
        boolean bEventFromTargetTable = "target-table".equalsIgnoreCase(uiSourceOfEvent);
		for (MeddraDictHierarchySearchDto m : dtos) {
            final Long c = Long.valueOf(m.getCode());
			HierarchyNode node = this.createMeddraNode(m, nodeType);
            node.setRelationEntity(cmqRelationsMap.get(c));
			
            if(!bCurrentList && !isRootListNode && bEventFromTargetTable) {
                node.markNotEditableInRelationstable();
            }

            TreeNode treeNode = new DefaultTreeNode(node, expandedTreeNode);

            addedNodes.put(c, treeNode);
            dtoCodes.add(c);
		}
        
        List<Map<String, Object>> countsOfChildren = null;
        if (bCurrentList) {
            // styling and coloring
            setCurrentMeddraColors(dtos, addedNodes, cmqRelationsMap);
            for (TreeNode n : addedNodes.values()) {
                HierarchyNode hn = (HierarchyNode)n.getData();
                setCurrentCmqRelationNodeStyle(hn, (CmqRelation190)hn.getRelationEntity());
            }
                            
            if(childNodeType != null) {
            	if(childNodeType.equalsIgnoreCase("LLT")) {
            		boolean filterLltFlag = this.globalController.isFilterLltsFlag();
            		if(!filterLltFlag) {
            			countsOfChildren = this.meddraDictCurrentService.findChildrenCountByParentCodes(childNodeType + "_"
                                , nodeType + "_", dtoCodes);
            		}
            	} else {
            		countsOfChildren = this.meddraDictCurrentService.findChildrenCountByParentCodes(childNodeType + "_"
                            , nodeType + "_", dtoCodes);
            	}
            }
        } else {
            // styling and coloring
            setTargetMeddraColors(dtos, addedNodes, cmqRelationsMap);
            for (TreeNode n : addedNodes.values()) {
                HierarchyNode hn = (HierarchyNode)n.getData();
                setTargetCmqRelationNodeStyle(hn, (CmqRelationTarget)hn.getRelationEntity());
            }
            if(childNodeType != null) {
            	if(childNodeType.equalsIgnoreCase("LLT")) {
            		boolean filterLltFlag = this.globalController.isFilterLltsFlag();
            		if(!filterLltFlag) {
            			countsOfChildren = this.meddraDictTargetService.findChildrenCountByParentCodes(childNodeType + "_"
                                , nodeType + "_", dtoCodes);
            		}
            	} else {
            		countsOfChildren = this.meddraDictTargetService.findChildrenCountByParentCodes(childNodeType + "_"
                            , nodeType + "_", dtoCodes);
            	}
            }
        }

        // dummy node generation for expanding children
        if((null != countsOfChildren) && (countsOfChildren.size() > 0)) {
            //first find and fix child nodes stuff
            for (Map<String, Object> cc: countsOfChildren) {
                if(cc.get("PARENT_CODE") != null && cc.get("COUNT") != null) {
                    Long pCode = (Long)cc.get("PARENT_CODE");
                    Long c = (Long)cc.get("COUNT");
                    TreeNode t = c > 0 ? addedNodes.get(pCode) : null;
                    if(t!=null) {
                        // add a dummmy node to show expand arrow
                        createNewDummyNode(t);
                    }
                }
            }
        }
	}
	
    
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void populateCmqBaseChildren(Long cmqCode, TreeNode expandedTreeNode, String cmqType, String uiSourceOfEvent) {
		List<? extends IEntity> childCmqBaseList;
		boolean isRootListNode = isRootListNode(expandedTreeNode);
        boolean bCurrentList = "current".equalsIgnoreCase(cmqType);
        boolean bEventFromTargetTable = "target-table".equalsIgnoreCase(uiSourceOfEvent);
        
		if(bCurrentList) {
			childCmqBaseList = cmqBaseCurrentService.findChildCmqsByParentCode(cmqCode);
		} else {
			childCmqBaseList = cmqBaseTargetService.findChildCmqsByParentCode(cmqCode);
		}
		
		List<Long> childCmqCodeList = new ArrayList<>();
		Map<Long, TreeNode> childTreeNodes = new HashMap<>();
		
		if ((null != childCmqBaseList) && (childCmqBaseList.size() > 0)) {
			for (IEntity entity : childCmqBaseList) {
				HierarchyNode node = new HierarchyNode();
				if(bCurrentList) {
					CmqBase190 childCmqBase = (CmqBase190) entity;
					node.setLevel(childCmqBase.getCmqTypeCd());
					node.setTerm(childCmqBase.getCmqName());
					node.setCode(childCmqBase.getCmqCode().toString());
					node.setEntity(childCmqBase);
					if(!isRootListNode && bEventFromTargetTable) {
						node.markNotEditableInRelationstable();
					}
					TreeNode cmqBaseChildNode = new DefaultTreeNode(node, expandedTreeNode);
					
					childTreeNodes.put(childCmqBase.getCmqCode(), cmqBaseChildNode);
					childCmqCodeList.add(childCmqBase.getCmqCode());
					
                    //Coloring
                    setCurrentCmqBaseNodeStyle(node, childCmqBase);
					
				} else {
					CmqBaseTarget childCmqBase = (CmqBaseTarget) entity;
					node.setLevel(childCmqBase.getCmqTypeCd());
					node.setTerm(childCmqBase.getCmqName());
					node.setCode(childCmqBase.getCmqCode().toString());
					node.setEntity(childCmqBase);
					if(!isRootListNode && bEventFromTargetTable) {
						node.markNotEditableInRelationstable();
					}
					if("PRO".equalsIgnoreCase(childCmqBase.getCmqTypeCd())) {
						node.setHideDelete(true);
					}
					TreeNode cmqBaseChildNode = new DefaultTreeNode(node, expandedTreeNode);
					
					childTreeNodes.put(childCmqBase.getCmqCode(), cmqBaseChildNode);
					childCmqCodeList.add(childCmqBase.getCmqCode());
					
                    // Coloring
                    setTargetCmqBaseNodeStyle(node, childCmqBase);
				}
			}
			
			List<Map<String, Object>> childrenOfChildCountsList = null;
			if(bCurrentList) {
				childrenOfChildCountsList = this.cmqBaseCurrentService.findCmqChildCountForParentCmqCodes(childCmqCodeList);
			} else {
				childrenOfChildCountsList = this.cmqBaseTargetService.findCmqChildCountForParentCmqCodes(childCmqCodeList);
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
									createNewDummyNode(childTreeNodes.get(childCmqCode));
								}
								break;
							}//end of if(cmqCode.longValue() == parentCmqCode.longValue())
						}
					}
				}
			}
			
			//now find relations for those who don't have children
			List<Map<String, Object>> relationsCountsList = null;
			if(bCurrentList) {
				relationsCountsList = this.cmqRelationCurrentService.findCountByCmqCodes(childCmqCodeList);
			} else {
				relationsCountsList = this.cmqRelationTargetService.findCountByCmqCodes(childCmqCodeList);
			}
				
			if((null != relationsCountsList) && (relationsCountsList.size() > 0)) {
				for(Map<String, Object> map: relationsCountsList) {
					if(map.get("CMQ_CODE") != null) {
						Long resultCmqCode = (Long)map.get("CMQ_CODE");
						Long count = (Long)map.get("COUNT");
						if(count > 0) {
							//add a dummy node for this child in parent
							createNewDummyNode(childTreeNodes.get(resultCmqCode));
						}
					}
				}
			}
		}
	}
	
	public void populateCmqRelations(Long cmqCode, TreeNode expandedTreeNode, String cmqType, String uiSourceOfEvent, IEntity entityExpanded) {
        boolean bCurrentList = "current".equalsIgnoreCase(cmqType);
        boolean bEventFromTargetTable = "target-table".equalsIgnoreCase(uiSourceOfEvent);
        
		Map<Long, IEntity> socCodesMap = new HashMap<>();
		Map<Long, IEntity> hlgtCodesMap = new HashMap<>();
		Map<Long, IEntity> hltCodesMap = new HashMap<>();
		Map<Long, IEntity> ptCodesMap = new HashMap<>();
		Map<Long, IEntity> lltCodesMap = new HashMap<>();
		
		List<? extends IEntity> existingRelations = null;
		if(bCurrentList) {
			existingRelations = this.cmqRelationCurrentService.findByCmqCode(cmqCode);
		} else {
			existingRelations = this.cmqRelationTargetService.findByCmqCode(cmqCode);
		}
		
		if((null != existingRelations) && (existingRelations.size() > 0)) {
			List<Long> smqChildCodeList = new ArrayList<>();
			Map<Long, TreeNode> smqChildTreeNodeMap = new HashMap<>();
            if(bCurrentList) {
                for (IEntity entity : existingRelations) {
					CmqRelation190 cmqRelation = (CmqRelation190) entity;
					if((cmqRelation.getSocCode() != null) && (cmqRelation.getSocCode() > 0)) {
						socCodesMap.put(cmqRelation.getSocCode(), cmqRelation);
					} else if((cmqRelation.getHlgtCode() != null) && (cmqRelation.getHlgtCode() > 0)) {
						hlgtCodesMap.put(cmqRelation.getHlgtCode(), cmqRelation);
					} else if((cmqRelation.getHltCode() != null) && (cmqRelation.getHltCode() > 0)) {
						hltCodesMap.put(cmqRelation.getHltCode(), cmqRelation);
					} else if((cmqRelation.getPtCode() != null) && (cmqRelation.getPtCode() > 0) && (cmqRelation.getSmqCode() == null)) {
						ptCodesMap.put(cmqRelation.getPtCode(), cmqRelation);
					} else if((cmqRelation.getLltCode() != null) && (cmqRelation.getLltCode() > 0)) {
						lltCodesMap.put(cmqRelation.getLltCode(), cmqRelation);
					} else if((cmqRelation.getSmqCode() != null) && (cmqRelation.getSmqCode() > 0)) {
						smqChildCodeList.add(cmqRelation.getSmqCode());
						TreeNode treeNode = this.populateSmqTreeNode(cmqRelation, expandedTreeNode, cmqType, cmqCode, uiSourceOfEvent);
						smqChildTreeNodeMap.put(cmqRelation.getSmqCode(), treeNode);
					}
                }
                
                if(smqChildCodeList.size() > 0) {
    				//find child smqs for this one and add a C in fornt of name if it has
    				List<Map<String, Object>> smqRelationsCountList = smqBaseCurrentService
    						.findSmqChildRelationsCountForSmqCodes(smqChildCodeList);
    				if ((null != smqRelationsCountList)
    						&& (smqRelationsCountList.size() > 0)) {
    					for (Map<String, Object> map : smqRelationsCountList) {
    						if (map.get("SMQ_CODE") != null) {
    							Long childSmqCode = (Long) map.get("SMQ_CODE");
    							if ((Long) map.get("COUNT") > 0) {
    								TreeNode treeNode = smqChildTreeNodeMap.get(childSmqCode);
    								if(null != treeNode) {
	    								HierarchyNode hierarchyNode = (HierarchyNode) treeNode.getData();
	    								String level = hierarchyNode.getLevel();
	    								hierarchyNode.setLevel("'C' " + level);
    								}
    							}
    						}
    					}
    				}
    			}//end of if(smqChildCodeList.size() > 0)
            } else {
                for (IEntity entity : existingRelations) {
					CmqRelationTarget cmqRelation = (CmqRelationTarget) entity;
					if((cmqRelation.getSocCode() != null) && (cmqRelation.getSocCode() > 0)) {
						socCodesMap.put(cmqRelation.getSocCode(), cmqRelation);
					} else if((cmqRelation.getHlgtCode() != null) && (cmqRelation.getHlgtCode() > 0)) {
						hlgtCodesMap.put(cmqRelation.getHlgtCode(), cmqRelation);
					} else if((cmqRelation.getHltCode() != null) && (cmqRelation.getHltCode() > 0)) {
						hltCodesMap.put(cmqRelation.getHltCode(), cmqRelation);
					} else if((cmqRelation.getPtCode() != null) && (cmqRelation.getPtCode() > 0) && (cmqRelation.getSmqCode() == null)) {
						ptCodesMap.put(cmqRelation.getPtCode(), cmqRelation);
					} else if((cmqRelation.getLltCode() != null) && (cmqRelation.getLltCode() > 0)) {
						lltCodesMap.put(cmqRelation.getLltCode(), cmqRelation);
					} else if((cmqRelation.getSmqCode() != null) && (cmqRelation.getSmqCode() > 0)) {
						smqChildCodeList.add(cmqRelation.getSmqCode());
						TreeNode treeNode = this.populateSmqTreeNode(cmqRelation, expandedTreeNode, cmqType, cmqCode, uiSourceOfEvent);
						smqChildTreeNodeMap.put(cmqRelation.getSmqCode(), treeNode);
					}
				}
                
                if(smqChildCodeList.size() > 0) {
    				//find child smqs for this one and add a C in fornt of name if it has
    				List<Map<String, Object>> smqRelationsCountList = smqBaseTargetService
    						.findSmqChildRelationsCountForSmqCodes(smqChildCodeList);
    				if ((null != smqRelationsCountList)
    						&& (smqRelationsCountList.size() > 0)) {
    					for (Map<String, Object> map : smqRelationsCountList) {
    						if (map.get("SMQ_CODE") != null) {
    							Long childSmqCode = (Long) map.get("SMQ_CODE");
    							if ((Long) map.get("COUNT") > 0) {
    								TreeNode treeNode = smqChildTreeNodeMap.get(childSmqCode);
    								if(null != treeNode) {
	    								HierarchyNode hierarchyNode = (HierarchyNode) treeNode.getData();
	    								String level = hierarchyNode.getLevel();
	    								hierarchyNode.setLevel("'C' " + level);
    								}
    							}
    						}
    					}
    				}
    			}//end of if(smqChildCodeList.size() > 0)
			}
			
			//find socs now
			if(socCodesMap.size() > 0) {
				List<MeddraDictHierarchySearchDto> socDtos;
				List<Long> socCodesList = new ArrayList<>(socCodesMap.keySet());
				if(bCurrentList) {
					socDtos = this.meddraDictCurrentService.findByCodes("SOC_", socCodesList);
				} else {
					socDtos = this.meddraDictTargetService.findByCodes("SOC_", socCodesList);
				}
				this.populateCmqRelationTreeNodes(socDtos, expandedTreeNode, "SOC", "HLGT", cmqType, cmqCode, socCodesMap, uiSourceOfEvent, entityExpanded);
			}
			
			if(hlgtCodesMap.size() > 0) {
				List<MeddraDictHierarchySearchDto> hlgtDtos;
				List<Long> hlgtCodesList = new ArrayList<>(hlgtCodesMap.keySet());
				if(bCurrentList) {
					hlgtDtos = this.meddraDictCurrentService.findByCodes("HLGT_", hlgtCodesList);
				} else {
					hlgtDtos = this.meddraDictTargetService.findByCodes("HLGT_", hlgtCodesList);
				}
				this.populateCmqRelationTreeNodes(hlgtDtos, expandedTreeNode, "HLGT", "HLT", cmqType, cmqCode, hlgtCodesMap, uiSourceOfEvent, entityExpanded);
			}
			
			if(hltCodesMap.size() > 0) {
				List<MeddraDictHierarchySearchDto> hltDtos;
				List<Long> hltCodesList = new ArrayList<>(hltCodesMap.keySet());
				if(bCurrentList) {
					hltDtos = this.meddraDictCurrentService.findByCodes("HLT_", hltCodesList);
				} else {
					hltDtos = this.meddraDictTargetService.findByCodes("HLT_", hltCodesList);
				}
				this.populateCmqRelationTreeNodes(hltDtos, expandedTreeNode, "HLT", "PT", cmqType, cmqCode, hltCodesMap, uiSourceOfEvent, entityExpanded);
			}
			
			if(ptCodesMap.size() > 0) {
				List<MeddraDictHierarchySearchDto> ptDtos;
				List<Long> ptCodesList = new ArrayList<>(ptCodesMap.keySet());
				if(bCurrentList) {
					ptDtos = this.meddraDictCurrentService.findByCodes("PT_", ptCodesList);
				} else {
					ptDtos = this.meddraDictTargetService.findByCodes("PT_", ptCodesList);
				}
				this.populateCmqRelationTreeNodes(ptDtos, expandedTreeNode, "PT", "LLT", cmqType, cmqCode, ptCodesMap, uiSourceOfEvent, entityExpanded);
			}
			
			if(lltCodesMap.size() > 0) {
				boolean isRootListNode = isRootListNode(expandedTreeNode);
				List<MeddraDictHierarchySearchDto> lltDtos;
				List<Long> lltCodesList = new ArrayList<>(lltCodesMap.keySet());
				if(bCurrentList) {
					lltDtos = this.meddraDictCurrentService.findByCodes("LLT_", lltCodesList);
				} else {
					lltDtos = this.meddraDictTargetService.findByCodes("LLT_", lltCodesList);
				}
                this.populateCmqRelationTreeNodes(lltDtos, expandedTreeNode, "LLT", null, cmqType, cmqCode, ptCodesMap, uiSourceOfEvent, entityExpanded);
			}
		}
	}

	public void populateSmqBaseChildren(Long smqCode, TreeNode expandedTreeNode, String smqType, String uiSourceOfEvent) {
		boolean isRootListNode = isRootListNode(expandedTreeNode);
        boolean bCurrentList = "current".equalsIgnoreCase(smqType);
        boolean bEventFromTargetTable = "target-table".equalsIgnoreCase(uiSourceOfEvent);
        
		List<? extends IEntity> childSmqBaseList = null;
		if(bCurrentList) {
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
				if(bCurrentList) {
					SmqBase190 childSmqBase = (SmqBase190) entity;
					childSmqCode = childSmqBase.getSmqCode();
                    if (null != childSmqBase.getSmqLevel())
                        childNode.setLevel(SMQLevelHelper.getLabel(childSmqBase.getSmqLevel()));
					childNode.setTerm(childSmqBase.getSmqName());
					childNode.setCode(childSmqBase.getSmqCode().toString());
					childNode.setEntity(childSmqBase);
					smqChildCodeList.add(childSmqBase.getSmqCode());
					
                    if (CSMQBean.IMPACT_TYPE_ICS.equals(childSmqBase.getImpactType()) ||
                            CSMQBean.IMPACT_TYPE_IMPACTED.equalsIgnoreCase(childSmqBase.getImpactType()))
						childNode.setRowStyleClass("blue-colored");
					//setSMQCurrentNodeStyle(childNode, childSmqBase);
				} else {
					//for target here
					SmqBaseTarget childSmqBase = (SmqBaseTarget) entity;
					childSmqCode = childSmqBase.getSmqCode();
                    if (null != childSmqBase.getSmqLevel())
                        childNode.setLevel(SMQLevelHelper.getLabel(childSmqBase.getSmqLevel()));
					childNode.setTerm(childSmqBase.getSmqName());
					childNode.setCode(childSmqBase.getSmqCode().toString());
					childNode.setEntity(childSmqBase);
					smqChildCodeList.add(childSmqBase.getSmqCode());
					if(!isRootListNode && bEventFromTargetTable) {
						childNode.markNotEditableInRelationstable();
					}
                    if (CSMQBean.IMPACT_TYPE_ICS.equals(childSmqBase.getImpactType()) ||
                            CSMQBean.IMPACT_TYPE_IMPACTED.equalsIgnoreCase(childSmqBase.getImpactType()))
                        childNode.setRowStyleClass("blue-colored");
				}
				// add child to parent
				TreeNode childTreeNode = new DefaultTreeNode(childNode, expandedTreeNode);
				smqTreeNodeMap.put(childSmqCode, childTreeNode);
			} // end of for
			
			//find smqrelations of all child smqs
            List<Map<String, Object>> childSmqRelationsCountList;
            if(bCurrentList) {
                childSmqRelationsCountList = this.smqBaseCurrentService.findSmqRelationsCountForSmqCodes(smqChildCodeList);
            } else {
                childSmqRelationsCountList = this.smqBaseTargetService.findSmqRelationsCountForSmqCodes(smqChildCodeList);
            }
            if((null != childSmqRelationsCountList) && (childSmqRelationsCountList.size() > 0)) {
                for(Map<String, Object> map : childSmqRelationsCountList) {
                    if(map.get("SMQ_CODE") != null) {
                        Long childSmqCode = (Long)map.get("SMQ_CODE");
                        Long count = (Long)map.get("COUNT");
                        if(count > 0) {
                            createNewDummyNode(smqTreeNodeMap.get(childSmqCode));
                        }
                    }
                }
            }
		}
	}

	public void populateMeddraDictReverseHierarchySearchDtoChildren(String searchColumnTypePrefix, String partitionColumn
																		, Long code, HierarchyNode hierarchyNode, TreeNode expandedTreeNode
																		, MeddraDictReverseHierarchySearchDto reverseSearchDto
																		, boolean chekcForPrimaryPath, String uiSourceOfEvent) {
		boolean isRootListNode = isRootListNode(expandedTreeNode);
        boolean bEventFromTargetTable = "target-table".equalsIgnoreCase(uiSourceOfEvent);
        boolean checkPrimaryPathTemp = false;
		String partitionColumnPrefix = partitionColumn +"_";
		List<MeddraDictReverseHierarchySearchDto> childReverseSearchDtos = this.meddraDictTargetService.findReverseByCode(searchColumnTypePrefix
																															, partitionColumnPrefix, code);
		if(CollectionUtils.isNotEmpty(childReverseSearchDtos)) {
			if(chekcForPrimaryPath && !hierarchyNode.isPrimarypathCheckDone() && (childReverseSearchDtos.size() > 1)) {
				checkPrimaryPathTemp = true;
			}
			for (MeddraDictReverseHierarchySearchDto childReverseSearchDto : childReverseSearchDtos) {
				HierarchyNode childNode;
				if(checkPrimaryPathTemp) {
					boolean isPrimary = false;
					if("Y".equalsIgnoreCase(childReverseSearchDto.getPrimaryPathFlag())) {
						isPrimary = true;
					}
					childNode = this.createMeddraReverseNode(childReverseSearchDto, partitionColumn, isPrimary);
					childNode.setPrimarypathCheckDone(true);
				} else {
					childNode = this.createMeddraReverseNode(childReverseSearchDto, partitionColumn, hierarchyNode.isPrimaryPathFlag());
				}
				
				if(!isRootListNode && bEventFromTargetTable) {
					childNode.markNotEditableInRelationstable();
				}
				
				//Meddra Color
				//setMeddraColor(childReverseSearchDto, childNode); //TODO A REVOIR
				TreeNode childTreeNode = new DefaultTreeNode(childNode, expandedTreeNode);
				
				//dont add any child for last leaf node
				if(!"SOC".equalsIgnoreCase(partitionColumn)) {
					if(StringUtils.isNotBlank(reverseSearchDto.getHltTerm())) {
						// add a dummmy node to show expand arrow
						createNewDummyNode(childTreeNode);
					}
				}
			}
		}
	}
	
	
	public void populateMeddraDictHierarchySearchDtoChildren(String parentLevel, Long dtoCode, TreeNode expandedTreeNode
																, String meddraType, String uiSourceOfEvent) {
		boolean isRootListNode = isRootListNode(expandedTreeNode);
        boolean bCurrentList = "current".equalsIgnoreCase(meddraType);
        boolean bEventFromTargetTable = "target-table".equalsIgnoreCase(uiSourceOfEvent);
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
		List<MeddraDictHierarchySearchDto> childDtos;
		if(bCurrentList) {
			childDtos = this.meddraDictCurrentService.findChildrenByParentCode(childSearchColumnTypePrefix, parentCodeColumnPrefix, dtoCode);
		} else {
			childDtos = this.meddraDictTargetService.findChildrenByParentCode(childSearchColumnTypePrefix, parentCodeColumnPrefix, dtoCode);
		}
        
        Map<Long, TreeNode> nodesMap = new HashMap<>();
        List<Long> nodesMapKeys = new LinkedList<>();
		for (MeddraDictHierarchySearchDto childDto : childDtos) {
			HierarchyNode childNode = this.createMeddraNode(childDto, childLevel);
			if("PT".equalsIgnoreCase(childLevel)){//add in only PT children
				if(!StringUtils.isBlank(childDto.getPrimaryPathFlag()) 
						&& (childDto.getPrimaryPathFlag().equalsIgnoreCase("Y"))){
					childNode.setPrimaryPathFlag(true);
				} else {
					childNode.setPrimaryPathFlag(false);
				}
			} else {
				childNode.setPrimaryPathFlag(false);
			}
			
			if(!isRootListNode && bEventFromTargetTable) {
				childNode.markNotEditableInRelationstable();
			}
			
			//Meddra Color
			if (bCurrentList)
				setCurrentMeddraColor(childDto, childNode, null, null);
			else
				setTargetMeddraColor(childDto, childNode, null, null);
			
			TreeNode childTreeNode = new DefaultTreeNode(childNode, expandedTreeNode);
			
			//fetch children count of this iterating child node by code of child
			//no need to do this is the childOfChild is LLT since LT is the leaf ode type
			if(!"LLT".equalsIgnoreCase(childLevel)) {
                nodesMap.put(Long.valueOf(childDto.getCode()), childTreeNode);
                nodesMapKeys.add(Long.valueOf(childDto.getCode()));
            }
		}
    
		boolean filterLltFlag = this.globalController.isFilterLltsFlag();
		if(!"PT".equalsIgnoreCase(childLevel) || ("PT".equalsIgnoreCase(childLevel) && !filterLltFlag)) {
			List<Map<String, Object>> countsOfChildren;
	        if (bCurrentList) {
	            countsOfChildren = this.meddraDictCurrentService.findChildrenCountByParentCodes(childchildOfChildSearchColumnTypePrefix,
	                    childSearchColumnTypePrefix, nodesMapKeys);
	        } else {
	            countsOfChildren = this.meddraDictTargetService.findChildrenCountByParentCodes(childchildOfChildSearchColumnTypePrefix,
	                    childSearchColumnTypePrefix, nodesMapKeys);
	        }

	        if((null != countsOfChildren) && (countsOfChildren.size() > 0)) {
	            //first find and fix child nodes stuff
	            for (Map<String, Object> cc: countsOfChildren) {
	                if(cc.get("PARENT_CODE") != null && cc.get("COUNT") != null) {
	                    Long pCode = (Long)cc.get("PARENT_CODE");
	                    Long c = (Long)cc.get("COUNT");
	                    TreeNode t = c > 0 ? nodesMap.get(pCode) : null;
	                    if(t!=null) {
	                        // add a dummmy node to show expand arrow
	                        createNewDummyNode(t);
	                    }
	                }
	            }
	        }
		}
	}
			
	public boolean isRootListNode(TreeNode treeNode) {
		if((StringUtils.isNotEmpty(treeNode.getType())) && !(treeNode.getType().equalsIgnoreCase("root"))) {
			return treeNode.getParent().getType().equalsIgnoreCase("root");
		}
		return false;
	}
    
	private boolean isRootNodeOfSmqType(TreeNode treeNode) {
		if((StringUtils.isNotEmpty(treeNode.getType())) && !(treeNode.getType().equalsIgnoreCase("root"))) {
			TreeNode parentTreeNode = treeNode.getParent();
			if(parentTreeNode.getType().equalsIgnoreCase("root")) {
				HierarchyNode hierarchyNode = (HierarchyNode) treeNode.getData();
				IEntity entity = hierarchyNode.getEntity();
				return entity instanceof SmqBaseTarget;
			} else if(parentTreeNode.getParent().getType().equalsIgnoreCase("root")) {
				HierarchyNode hierarchyNode = (HierarchyNode) parentTreeNode.getData();
				IEntity entity = hierarchyNode.getEntity();
				return entity instanceof SmqBaseTarget;
			} else {
				return this.isRootNodeOfSmqType(parentTreeNode);
			}
		}
		return false;
	}
	
	public void updateCurrentTableForCmqList(TreeNode currentTableRootTreeNode,CmqBaseTarget selectedCmqList) {
        IARelationsTreeHelper treeHelper = new IARelationsTreeHelper(cmqBaseTargetService, smqBaseTargetService, meddraDictTargetService, cmqRelationTargetService, globalController);
        
		CmqBase190 cmqBaseCurrent = this.cmqBaseCurrentService.findByCode(selectedCmqList.getCmqCode());
		HierarchyNode node = treeHelper.createCmqBaseCurrentHierarchyNode(cmqBaseCurrent);
		TreeNode cmqBaseTreeNode = new DefaultTreeNode(node, currentTableRootTreeNode);
        
        setCurrentCmqBaseNodeStyle(node, cmqBaseCurrent);
	
		boolean dummyNodeAdded = false;
		Long count = this.cmqBaseCurrentService.findCmqChildCountForParentCmqCode(cmqBaseCurrent.getCmqCode());
		if((count != null) && (count > 0)) {
			createNewDummyNode(cmqBaseTreeNode);
			dummyNodeAdded = true;
		}
		
		//check for relations now
		if(!dummyNodeAdded) {
			count = this.cmqRelationCurrentService.findCountByCmqCode(cmqBaseCurrent.getCmqCode());
			if((count != null) && (count > 0)) {
				createNewDummyNode(cmqBaseTreeNode);
			}
		}
	}
	
	public void updateTargetTableForCmqList(TreeNode targetTableRootTreeNode, CmqBaseTarget selectedCmqList) {
		HierarchyNode node = this.createCmqBaseTargetHierarchyNode(selectedCmqList);
		node.markReadOnlyInRelationstable();
		TreeNode cmqBaseTreeNode = new DefaultTreeNode(node, targetTableRootTreeNode);

        setTargetCmqBaseNodeStyle(node, selectedCmqList);
		
		boolean dummyNodeAdded = false;
		Long count = this.cmqBaseTargetService.findCmqChildCountForParentCmqCode(selectedCmqList.getCmqCode());
		if((count != null) && (count > 0)) {
			createNewDummyNode(cmqBaseTreeNode);
			dummyNodeAdded = true;
		}
		
		//check for relations now
		if(!dummyNodeAdded) {
			count = this.cmqRelationTargetService.findCountByCmqCode(selectedCmqList.getCmqCode());
			if((count != null) && (count > 0)) {
				createNewDummyNode(cmqBaseTreeNode);
			}
		}
	}
    
    public void updateCurrentTableForSmqList(TreeNode currentTableRootTreeNode, SmqBaseTarget selectedSmqList) {
		SmqBase190 smqBaseCurrent = this.smqBaseCurrentService.findByCode(selectedSmqList.getSmqCode());
		if(null != smqBaseCurrent) {
			HierarchyNode node = this.createSmqBaseCurrrentNode(null, smqBaseCurrent);
			TreeNode cmqBaseTreeNode = new DefaultTreeNode(node, currentTableRootTreeNode);

			if (CSMQBean.IMPACT_TYPE_ICS.equals(smqBaseCurrent.getImpactType())
                    || CSMQBean.IMPACT_TYPE_IMPACTED.equals(smqBaseCurrent.getImpactType()))
				node.setRowStyleClass("blue-colored");
			
			boolean dummyNodeAdded = false;
			Long count = this.smqBaseCurrentService.findChildSmqCountByParentSmqCode(smqBaseCurrent.getSmqCode());
			if((count != null) && (count > 0)) {
				createNewDummyNode(cmqBaseTreeNode);
				dummyNodeAdded = true;
			}
			
			//check for child smq in relations table and add a C to level if found
			count = this.smqBaseCurrentService.findSmqChildRelationsCountForSmqCode(smqBaseCurrent.getSmqCode());
			if(count > 0) {
				String level = node.getLevel();
				node.setLevel("'C' " + level);
			}
			
			//check for relations now
			if(!dummyNodeAdded) {
				count = this.smqBaseCurrentService.findSmqRelationsCountForSmqCode(smqBaseCurrent.getSmqCode());
				if((count != null) && (count > 0)) {
					createNewDummyNode(cmqBaseTreeNode);
				}
			}
		} else {
			FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO,
                            "No Current SQM found with code '"  + selectedSmqList.getSmqCode() + "'", ""));
		}
	}
	
	public void updateTargetTableForSmqList(TreeNode targetTableRootTreeNode, SmqBaseTarget selectedSmqList) {
        IARelationsTreeHelper treeHelper = new IARelationsTreeHelper(
                cmqBaseCurrentService, smqBaseCurrentService, meddraDictCurrentService, cmqRelationCurrentService,
                cmqBaseTargetService, smqBaseTargetService, meddraDictTargetService, cmqRelationTargetService, globalController);
		HierarchyNode node = treeHelper.createSmqBaseTargetNode(selectedSmqList, null);
		node.markReadOnlyInRelationstable();
		TreeNode cmqBaseTreeNode = new DefaultTreeNode(node, targetTableRootTreeNode);

		if (CSMQBean.IMPACT_TYPE_ICS.equals(selectedSmqList.getImpactType())
                || CSMQBean.IMPACT_TYPE_IMPACTED.equalsIgnoreCase(selectedSmqList.getImpactType()))
			node.setRowStyleClass("blue-colored");
		
		boolean dummyNodeAdded = false;
		Long count = this.smqBaseTargetService.findChildSmqCountByParentSmqCode(selectedSmqList.getSmqCode());
		if((count != null) && (count > 0)) {
			createNewDummyNode(cmqBaseTreeNode);
			dummyNodeAdded = true;
		}
		
		//check for child smq in relations table and add a C to level if found
		List<Long> smqCodes = new ArrayList<>();
		smqCodes.add(selectedSmqList.getSmqCode());
		List<Map<String, Object>> smqChildRelationsCountList = smqBaseCurrentService.findSmqChildRelationsCountForSmqCodes(smqCodes);
        if ((null != smqChildRelationsCountList)
				&& (smqChildRelationsCountList.size() > 0)) {
			for (Map<String, Object> map : smqChildRelationsCountList) {
				if (map.get("SMQ_CODE") != null) {
					if ((Long) map.get("COUNT") > 0) {
						String level = node.getLevel();
						node.setLevel("'C' " + level);
					}
				}
			}
		}
		
		//check for relations now
		if(!dummyNodeAdded) {
			count = this.smqBaseTargetService.findSmqRelationsCountForSmqCode(selectedSmqList.getSmqCode());
			if((count != null) && (count > 0)) {
				createNewDummyNode(cmqBaseTreeNode);
			}
		}
	}
    
    /**
     * Returns CMQ Impact Type Description
     * @param refCodeListService
     * @param impactType
     * @return 
     */
    protected static String getCmqRelationImpactDesc(IRefCodeListService refCodeListService, String impactType) {
        return refCodeListService.interpretInternalCodeToValueOrDefault(CqtConstants.CODE_LIST_TYPE_CMQ_RELATION_IMPACT_TYPE, impactType, impactType);
    }
    /**
     * Returns SMQ Impact Type Description
     * @param refCodeListService
     * @param impactType
     * @return 
     */
    protected static String getSmqRelationImpactDesc(IRefCodeListService refCodeListService, String impactType) {
        return refCodeListService.interpretInternalCodeToValueOrDefault(CqtConstants.CODE_LIST_TYPE_SMQ_RELATION_IMPACT_TYPE, impactType, impactType);
    }
    
    /**
     * Returns MedDRA Current Impact Type Description
     * @param refCodeListService
     * @param ent
     * @param lvl
     * @return 
     */
    protected static String getMeddraDictCurrentImpactDesc(IRefCodeListService refCodeListService, MeddraDictHierarchySearchDto ent, String lvl) {
        if("LLT".equals(lvl)) {
            if(ent.getLltNameChanged() != null)
                return refCodeListService.interpretMeddraImpactTypeDesc("llt_name_changed", ent.getLltNameChanged());
            else if(ent.getMovedLlt() != null)
                return refCodeListService.interpretMeddraImpactTypeDesc("moved_llt", ent.getMovedLlt());
            else if(ent.getPromotedLlt() != null)
                return refCodeListService.interpretMeddraImpactTypeDesc("promoted_llt", ent.getPromotedLlt());
            else if(ent.getMovedPt() != null)
                return refCodeListService.interpretMeddraImpactTypeDesc("moved_pt", ent.getMovedPt());
            else if(ent.getPrimarySocChange() != null)
                return refCodeListService.interpretMeddraImpactTypeDesc("primary_soc_change", ent.getPrimarySocChange());
            else if(ent.getDemotedPt() != null)
                return refCodeListService.interpretMeddraImpactTypeDesc("demoted_pt", ent.getDemotedPt());
            else if(ent.getPromotedPt() != null)
                return refCodeListService.interpretMeddraImpactTypeDesc("promoted_pt", ent.getPromotedPt());
            else if(ent.getNewSuccessorPt() != null)
                return refCodeListService.interpretMeddraImpactTypeDesc("new_successor_pt", ent.getNewSuccessorPt());
            else if(ent.getMovedHlt() != null)
                return refCodeListService.interpretMeddraImpactTypeDesc("moved_hlt", ent.getMovedHlt());
            else if(ent.getMergedHlt() != null)
                return refCodeListService.interpretMeddraImpactTypeDesc("merged_hlt", ent.getMergedHlt());
            else if(ent.getMovedHlgt() != null)
                return refCodeListService.interpretMeddraImpactTypeDesc("moved_hlgt", ent.getMovedHlgt());
            else if(ent.getMergedHlt() != null)
                return refCodeListService.interpretMeddraImpactTypeDesc("merged_hlgt", ent.getMovedHlt());
            else if(ent.getLltCurrencyChange() != null)
                return refCodeListService.interpretMeddraImpactTypeDesc("llt_currency_change", ent.getLltCurrencyChange());
            else if(ent.getNewLlt() != null)
                return refCodeListService.interpretMeddraImpactTypeDesc("new_llt", ent.getNewLlt());
        } else if("PT".equals(lvl)) {
            if(ent.getNewPt() != null)
                return refCodeListService.interpretMeddraImpactTypeDesc("new_pt", ent.getNewPt());
            else if(ent.getPtNameChanged() != null)
                return refCodeListService.interpretMeddraImpactTypeDesc("pt_name_changed", ent.getPtNameChanged());
            else if(ent.getMovedPt() != null)
                return refCodeListService.interpretMeddraImpactTypeDesc("moved_pt", ent.getMovedPt());
            else if(ent.getDemotedPt() != null)
                return refCodeListService.interpretMeddraImpactTypeDesc("demoted_pt", ent.getDemotedPt());
            else if(ent.getPromotedPt() != null)
                return refCodeListService.interpretMeddraImpactTypeDesc("promoted_pt", ent.getPromotedPt());
            else if(ent.getNewSuccessorPt() != null)
                return refCodeListService.interpretMeddraImpactTypeDesc("new_successor_pt", ent.getNewSuccessorPt());
            else if(ent.getPrimarySocChange() != null)
                return refCodeListService.interpretMeddraImpactTypeDesc("primary_soc_change", ent.getPrimarySocChange());
            else if(ent.getMovedHlt() != null)
                return refCodeListService.interpretMeddraImpactTypeDesc("moved_hlt", ent.getMovedHlt());
            else if(ent.getMergedHlt() != null)
                return refCodeListService.interpretMeddraImpactTypeDesc("merged_hlt", ent.getMergedHlt());
            else if(ent.getMovedHlgt() != null)
                return refCodeListService.interpretMeddraImpactTypeDesc("moved_hlgt", ent.getMovedHlgt());
            else if(ent.getMergedHlt() != null)
                return refCodeListService.interpretMeddraImpactTypeDesc("merged_hlgt", ent.getMovedHlt());
        } else if("HLT".equals(lvl)) {
            if(ent.getNewHlt() != null)
                return refCodeListService.interpretMeddraImpactTypeDesc("new_hlt", ent.getNewHlt());
            else if(ent.getHltNameChanged() != null)
                return refCodeListService.interpretMeddraImpactTypeDesc("hlt_name_changed", ent.getHltNameChanged());
            else if(ent.getMovedHlt() != null)
                return refCodeListService.interpretMeddraImpactTypeDesc("moved_hlt", ent.getMovedHlt());
            else if(ent.getMergedHlt() != null)
                return refCodeListService.interpretMeddraImpactTypeDesc("merged_hlt", ent.getMergedHlt());
            else if(ent.getMovedHlgt() != null)
                return refCodeListService.interpretMeddraImpactTypeDesc("moved_hlgt", ent.getMovedHlgt());
            else if(ent.getMergedHlgt() != null)
                return refCodeListService.interpretMeddraImpactTypeDesc("merged_hlgt", ent.getMergedHlgt());
        } else if("HLGT".equals(lvl)) {
            if(ent.getNewHlgt() != null)
                return refCodeListService.interpretMeddraImpactTypeDesc("new_hlgt", ent.getNewHlgt());
            else if(ent.getHlgtNameChanged() != null)
                return refCodeListService.interpretMeddraImpactTypeDesc("hlgt_name_changed", ent.getHlgtNameChanged());
            else if(ent.getMovedHlgt() != null)
                return refCodeListService.interpretMeddraImpactTypeDesc("moved_hlgt", ent.getMovedHlgt());
            else if(ent.getMergedHlgt() != null)
                return refCodeListService.interpretMeddraImpactTypeDesc("merged_hlgt", ent.getMergedHlgt());
        } else if("SOC".equals(lvl)) {
            if(ent.getNewSoc() != null)
                return refCodeListService.interpretMeddraImpactTypeDesc("new_soc", ent.getNewSoc());
            else if(ent.getSocNameChanged() != null)
                return refCodeListService.interpretMeddraImpactTypeDesc("soc_name_changed", ent.getSocNameChanged());
        }
        return "";
    }
    /**
     * Returns MedDRA Target Impact Type Description
     * @param refCodeListService
     * @param ent
     * @param lvl
     * @return 
     */
    protected static String getMeddraDictTargetImpactDesc(IRefCodeListService refCodeListService, MeddraDictHierarchySearchDto ent, String lvl) {
        if("LLT".equals(lvl)) {
            if(ent.getLltCurrencyChange() != null)
                return refCodeListService.interpretMeddraImpactTypeDesc("llt_currency_change", ent.getLltCurrencyChange());
            else if(ent.getNewLlt() != null)
                return refCodeListService.interpretMeddraImpactTypeDesc("new_llt", ent.getNewLlt());
            else if(ent.getLltNameChanged() != null)
                return refCodeListService.interpretMeddraImpactTypeDesc("llt_name_changed", ent.getLltNameChanged());
            else if(ent.getMovedLlt() != null)
                return refCodeListService.interpretMeddraImpactTypeDesc("moved_llt", ent.getMovedLlt());
            else if(ent.getPromotedLlt() != null)
                return refCodeListService.interpretMeddraImpactTypeDesc("promoted_llt", ent.getPromotedLlt());
            else if(ent.getPrimarySocChange() != null)
                return refCodeListService.interpretMeddraImpactTypeDesc("primary_soc_change", ent.getPrimarySocChange());
            else if(ent.getMovedPt() != null)
                return refCodeListService.interpretMeddraImpactTypeDesc("moved_pt", ent.getMovedPt());
            else if(ent.getDemotedPt() != null)
                return refCodeListService.interpretMeddraImpactTypeDesc("demoted_pt", ent.getDemotedPt());
            else if(ent.getPromotedPt() != null)
                return refCodeListService.interpretMeddraImpactTypeDesc("promoted_pt", ent.getPromotedPt());
            else if(ent.getNewSuccessorPt() != null)
                return refCodeListService.interpretMeddraImpactTypeDesc("new_successor_pt", ent.getNewSuccessorPt());
            else if(ent.getMovedHlt() != null)
                return refCodeListService.interpretMeddraImpactTypeDesc("moved_hlt", ent.getMovedHlt());
            else if(ent.getMergedHlt() != null)
                return refCodeListService.interpretMeddraImpactTypeDesc("merged_hlt", ent.getMergedHlt());
            else if(ent.getMovedHlgt() != null)
                return refCodeListService.interpretMeddraImpactTypeDesc("moved_hlgt", ent.getMovedHlgt());
            else if(ent.getMergedHlt() != null)
                return refCodeListService.interpretMeddraImpactTypeDesc("merged_hlgt", ent.getMovedHlt());
        } else if("PT".equals(lvl)) {
            if(ent.getNewPt() != null)
                return refCodeListService.interpretMeddraImpactTypeDesc("new_pt", ent.getNewPt());
            else if(ent.getPtNameChanged() != null)
                return refCodeListService.interpretMeddraImpactTypeDesc("pt_name_changed", ent.getPtNameChanged());
            else if(ent.getMovedPt() != null)
                return refCodeListService.interpretMeddraImpactTypeDesc("moved_pt", ent.getMovedPt());
            else if(ent.getDemotedPt() != null)
                return refCodeListService.interpretMeddraImpactTypeDesc("demoted_pt", ent.getDemotedPt());
            else if(ent.getPromotedPt() != null)
                return refCodeListService.interpretMeddraImpactTypeDesc("promoted_pt", ent.getPromotedPt());
            else if(ent.getNewSuccessorPt() != null)
                return refCodeListService.interpretMeddraImpactTypeDesc("new_successor_pt", ent.getNewSuccessorPt());
            else if(ent.getPrimarySocChange() != null)
                return refCodeListService.interpretMeddraImpactTypeDesc("primary_soc_change", ent.getPrimarySocChange());
            else if(ent.getMovedHlt() != null)
                return refCodeListService.interpretMeddraImpactTypeDesc("moved_hlt", ent.getMovedHlt());
            else if(ent.getMergedHlt() != null)
                return refCodeListService.interpretMeddraImpactTypeDesc("merged_hlt", ent.getMergedHlt());
            else if(ent.getMovedHlgt() != null)
                return refCodeListService.interpretMeddraImpactTypeDesc("moved_hlgt", ent.getMovedHlgt());
            else if(ent.getMergedHlt() != null)
                return refCodeListService.interpretMeddraImpactTypeDesc("merged_hlgt", ent.getMovedHlt());
        } else if("HLT".equals(lvl)) {
            if(ent.getNewHlt() != null)
                return refCodeListService.interpretMeddraImpactTypeDesc("new_hlt", ent.getNewHlt());
            else if(ent.getHltNameChanged() != null)
                return refCodeListService.interpretMeddraImpactTypeDesc("hlt_name_changed", ent.getHltNameChanged());
            else if(ent.getMovedHlt() != null)
                return refCodeListService.interpretMeddraImpactTypeDesc("moved_hlt", ent.getMovedHlt());
            else if(ent.getMergedHlt() != null)
                return refCodeListService.interpretMeddraImpactTypeDesc("merged_hlt", ent.getMergedHlt());
            else if(ent.getMovedHlgt() != null)
                return refCodeListService.interpretMeddraImpactTypeDesc("moved_hlgt", ent.getMovedHlgt());
            else if(ent.getMergedHlgt() != null)
                return refCodeListService.interpretMeddraImpactTypeDesc("merged_hlgt", ent.getMergedHlgt());
        } else if("HLGT".equals(lvl)) {
            if(ent.getNewHlgt() != null)
                return refCodeListService.interpretMeddraImpactTypeDesc("new_hlgt", ent.getNewHlgt());
            else if(ent.getHlgtNameChanged() != null)
                return refCodeListService.interpretMeddraImpactTypeDesc("hlgt_name_changed", ent.getHlgtNameChanged());
            else if(ent.getMovedHlgt() != null)
                return refCodeListService.interpretMeddraImpactTypeDesc("moved_hlgt", ent.getMovedHlgt());
            else if(ent.getMergedHlgt() != null)
                return refCodeListService.interpretMeddraImpactTypeDesc("merged_hlgt", ent.getMergedHlgt());
        } else if("SOC".equals(lvl)) {
            if(ent.getNewSoc() != null)
                return refCodeListService.interpretMeddraImpactTypeDesc("new_soc", ent.getNewSoc());
            else if(ent.getSocNameChanged() != null)
                return refCodeListService.interpretMeddraImpactTypeDesc("soc_name_changed", ent.getSocNameChanged());
        }
        return "";
    }
    
    /**
     * Returns the CMQ/SMQ relation impact type message (Hover text on relations hierarchy row/code column):
     * @param refCodeListService
     * @param node
     * @param currentOrTarget "current" or "target"
     * @return 
     */
    public static String getImpactDesc(IRefCodeListService refCodeListService, HierarchyNode node, String currentOrTarget) {
        boolean bCurrentList = "current".equalsIgnoreCase(currentOrTarget);
        
        if(bCurrentList) {
            if(node.getEntity() != null && node.getEntity() instanceof SmqBase190) {
                SmqBase190 ent = (SmqBase190)node.getEntity();
                return getSmqRelationImpactDesc(refCodeListService, ent.getImpactType());
            } else if(node.getEntity() != null && node.getEntity() instanceof SmqRelation190) {
                SmqRelation190 ent = (SmqRelation190)node.getEntity();
                return getSmqRelationImpactDesc(refCodeListService, ent.getRelationImpactType());
            } else if(node.getEntity() != null && node.getEntity() instanceof MeddraDictHierarchySearchDto) {
                MeddraDictHierarchySearchDto ent = (MeddraDictHierarchySearchDto)node.getEntity();
                if(node.getRelationEntity() != null && node.getRelationEntity() instanceof CmqRelation190) {
                    CmqRelation190 ent1 = (CmqRelation190)node.getRelationEntity();
                    if(ent1.getRelationImpactType() != null)
                        return getCmqRelationImpactDesc(refCodeListService, ent1.getRelationImpactType());
                }
                return getMeddraDictCurrentImpactDesc(refCodeListService, ent, node.getLevel());
            }
        } else {
            if(node.getEntity() != null && node.getEntity() instanceof SmqBaseTarget) {
                SmqBaseTarget ent = (SmqBaseTarget)node.getEntity();
                return getSmqRelationImpactDesc(refCodeListService, ent.getImpactType());
            } else if(node.getEntity() != null && node.getEntity() instanceof SmqRelationTarget) {
                SmqRelationTarget ent = (SmqRelationTarget)node.getEntity();
                return getSmqRelationImpactDesc(refCodeListService, ent.getRelationImpactType());
            } else if(node.getEntity() != null && node.getEntity() instanceof MeddraDictHierarchySearchDto) {
                MeddraDictHierarchySearchDto ent = (MeddraDictHierarchySearchDto)node.getEntity();
                if(node.getRelationEntity() != null && node.getRelationEntity() instanceof CmqRelationTarget) {
                    CmqRelationTarget ent1 = (CmqRelationTarget)node.getRelationEntity();
                    if(ent1.getRelationImpactType() != null)
                        return getCmqRelationImpactDesc(refCodeListService, ent1.getRelationImpactType());
                }
                return getMeddraDictTargetImpactDesc(refCodeListService, ent, node.getLevel());
            }
        }
        return "";
    }
    
    private TreeNode createNewDummyNode(TreeNode parentNode) {
        HierarchyNode dummyNode = new HierarchyNode(null, null, null, null);
        dummyNode.setDummyNode(true);
        return new DefaultTreeNode(dummyNode, parentNode);
    }
}
