package com.dbms.controller;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.ViewScoped;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.ListUtils;
import org.apache.commons.lang3.StringUtils;
import org.primefaces.event.NodeExpandEvent;
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

/**
 * @date Feb 7, 2017 7:39:34 AM
 **/
@ManagedBean
@ViewScoped
public class ImpactSearchController implements Serializable {

	private static final long serialVersionUID = 52993434344651662L;

	private static final Logger LOG = LoggerFactory.getLogger(ImpactSearchController.class);
	
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

	private LazyDataModel<CmqBaseTarget> impactedCmqBaseLazyDataModel;
	private CmqBaseTarget selectedImpactedCmqList;

	private LazyDataModel<CmqBaseTarget> notImpactedCmqBaseLazyDataModel;
	private CmqBaseTarget selectedNotImpactedCmqList;

	private TreeNode currentTableRootTreeNode;
	
	private TreeNode targetTableRootTreeNode;
	private boolean reviewEnabled, demoteEnabled, approveEnabled;
	
	public ImpactSearchController() {
		
	}

	@PostConstruct
	public void init() {
		this.impactedCmqBaseLazyDataModel = new CmqLazyDataModel(true);
		this.notImpactedCmqBaseLazyDataModel = new CmqLazyDataModel(false);
		currentTableRootTreeNode = new DefaultTreeNode("root", new HierarchyNode("CODE",
				"LEVEL", "SCOPE", null), null);
		targetTableRootTreeNode = new DefaultTreeNode("root", new HierarchyNode("CODE",
				"LEVEL", "SCOPE", "CATEGORY", "WEIGHT", null, null), null);
		
		setReviewEnabled(true);
		setApproveEnabled(true);
		setDemoteEnabled(true); 
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

	public void onNodeExpandCurrentTable(NodeExpandEvent event) {
		LOG.info("onNodeExpandCurrentTable");
		TreeNode expandedTreeNode = event.getTreeNode();
		HierarchyNode hierarchyNode = (HierarchyNode) expandedTreeNode.getData();
		boolean isDataFetchCompleted = hierarchyNode.isDataFetchCompleted();
		if (!isDataFetchCompleted) {
			IEntity entity = hierarchyNode.getEntity();

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
			}
			hierarchyNode.setDataFetchCompleted(true);
		}
	}
	
	public void updateCurrentTable() {
		LOG.info("current called");	
		if(this.selectedImpactedCmqList != null) {
			this.updateCurrentTableForCmqList(this.selectedImpactedCmqList);
		} else if(this.selectedNotImpactedCmqList != null) {
			this.updateCurrentTableForCmqList(this.selectedNotImpactedCmqList);
		}
	}

	public void updateTargetTable() {
		LOG.info("target called");
		if(this.selectedImpactedCmqList != null) {
			this.updateTargetTableForCmqList(this.selectedImpactedCmqList);
		} else if(this.selectedNotImpactedCmqList != null) {
			this.updateTargetTableForCmqList(this.selectedNotImpactedCmqList);
		}
	}

	private void updateCurrentTableForCmqList(CmqBaseTarget selectedCmqList) {
		//TODO: check if the currentTableRootTreeNode already has this tree node
		CmqBase190 cmqBaseCurrent = this.cmqBaseCurrentService.findByCode(selectedCmqList.getCmqCode());
		HierarchyNode node = this.createCmqBaseCurrentHierarchyNode(cmqBaseCurrent);
		TreeNode cmqBaseTreeNode = new DefaultTreeNode(node, currentTableRootTreeNode);
		
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
		//TODO: check if the targetTableRootTreeNode already has this tree node
		//CmqBaseTarget cmqBaseTarget = this.cmqBaseTargetService.findByCode(this.selectedImpactedCmqList.getCmqCode());
		HierarchyNode node = this.createCmqBaseTargetHierarchyNode(selectedCmqList);
		TreeNode cmqBaseTreeNode = new DefaultTreeNode(node, targetTableRootTreeNode);
		
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
			//childDtos = this.meddraDictService.findChildrenByParentCode(childSearchColumnTypePrefix, parentCodeColumnPrefix, dtoCode);
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
					//countOfChildrenOfChild = this.meddraDictCurrentService.findChldrenCountByParentCode(childchildOfChildSearchColumnTypePrefix
					//																, childSearchColumnTypePrefix, Long.valueOf(childDto.getCode()));
				}
				if(countOfChildrenOfChild > 0) {
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
		List<SmqBase190> childSmqBaseList = null;
		if("current".equalsIgnoreCase(smqType)) {
			childSmqBaseList = this.smqBaseCurrentService.findChildSmqByParentSmqCode(smqCode);
		} else {
			//childSmqBaseList = this.smqBaseTargetService.findChildSmqByParentSmqCode(smqCode);
		}
		if(CollectionUtils.isNotEmpty(childSmqBaseList)) {
			Map<Long, TreeNode> smqTreeNodeMap = new HashMap<>();
			List<Long> smqChildCodeList = new ArrayList<>();
			for (IEntity entity : childSmqBaseList) {
				HierarchyNode childNode = new HierarchyNode();
				if("current".equalsIgnoreCase(smqType)) {
					SmqBase190 childSmqBase = (SmqBase190) entity;
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
					// add child to parent
					TreeNode childTreeNode = new DefaultTreeNode(childNode, expandedTreeNode);
					smqTreeNodeMap.put(childSmqBase.getSmqCode(), childTreeNode);
				} else {
					//for target here
				}
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
					//childSmqRelationsCountList = this.smqBaseTargetService.findSmqRelationsCountForSmqCodes(subList);
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
		List<SmqRelation190> childRelations = null;
		if("current".equalsIgnoreCase(smqType)) {
			childRelations = this.smqBaseCurrentService.findSmqRelationsForSmqCode(smqCode);
		} else {
			//childRelations = this.smqBaseTargetService.findSmqRelationsForSmqCode(smqCode);
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
					childRelationNode.setCode(childRelation.getPtCode()
							.toString());
					childRelationNode.setEntity(childRelation);
				} else {
					//for target here
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
						this.populateSmqTreeNode(cmqRelation, expandedTreeNode, cmqType);
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
						this.populateSmqTreeNode(cmqRelation, expandedTreeNode, cmqType);
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
				this.populateCmqRelationTreeNodes(socDtos, expandedTreeNode, "SOC", "HLGT");
			}
			
			if(hlgtCodesList.size() > 0) {
				List<MeddraDictHierarchySearchDto> hlgtDtos = null;
				if("current".equalsIgnoreCase(cmqType)) {
					hlgtDtos = this.meddraDictCurrentService.findByCodes("HLGT_", hlgtCodesList);
				} else {
					hlgtDtos = this.meddraDictTargetService.findByCodes("HLGT_", hlgtCodesList);
				}
				this.populateCmqRelationTreeNodes(hlgtDtos, expandedTreeNode, "HLGT", "HLT");
			}
			
			if(hltCodesList.size() > 0) {
				List<MeddraDictHierarchySearchDto> hltDtos = null;
				if("current".equalsIgnoreCase(cmqType)) {
					hltDtos = this.meddraDictCurrentService.findByCodes("HLT_", hltCodesList);
				} else {
					hltDtos = this.meddraDictTargetService.findByCodes("HLT_", hltCodesList);
				}
				this.populateCmqRelationTreeNodes(hltDtos, expandedTreeNode, "HLT", "PT");
			}
			
			if(ptCodesList.size() > 0) {
				List<MeddraDictHierarchySearchDto> ptDtos = null;
				if("current".equalsIgnoreCase(cmqType)) {
					ptDtos = this.meddraDictCurrentService.findByCodes("PT_", ptCodesList);
				} else {
					ptDtos = this.meddraDictTargetService.findByCodes("PT_", ptCodesList);
				}
				this.populateCmqRelationTreeNodes(ptDtos, expandedTreeNode, "PT", "LLT");
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
	
	private void populateSmqTreeNode(IEntity entity, TreeNode expandedTreeNode, String cmqType) {
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
													, String nodeType, String childNodeType) {
		for (MeddraDictHierarchySearchDto meddraDictHierarchySearchDto : dtos) {
			HierarchyNode node = this.createMeddraNode(meddraDictHierarchySearchDto, nodeType);
			TreeNode treeNode = new DefaultTreeNode(node, expandedTreeNode);
			
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
}
