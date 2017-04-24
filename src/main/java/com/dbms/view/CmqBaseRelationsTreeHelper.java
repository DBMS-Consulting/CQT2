package com.dbms.view;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.ListUtils;
import org.apache.commons.lang3.StringUtils;
import org.primefaces.model.DefaultTreeNode;
import org.primefaces.model.TreeNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dbms.csmq.HierarchyNode;
import com.dbms.entity.IEntity;
import com.dbms.entity.cqt.CmqBase190;
import com.dbms.entity.cqt.CmqRelation190;
import com.dbms.entity.cqt.SmqBase190;
import com.dbms.entity.cqt.SmqRelation190;
import com.dbms.entity.cqt.dtos.MeddraDictHierarchySearchDto;
import com.dbms.entity.cqt.dtos.MeddraDictReverseHierarchySearchDto;
import com.dbms.service.ICmqBase190Service;
import com.dbms.service.ICmqRelation190Service;
import com.dbms.service.IMeddraDictService;
import com.dbms.service.ISmqBaseService;
import com.dbms.util.SMQLevelHelper;

public class CmqBaseRelationsTreeHelper {
	public enum SearchTarget { SMQ_BASE, MEDDRA_DICT, CMQ_BASE }
	
	private static final Logger LOG = LoggerFactory
			.getLogger(CmqBaseRelationsTreeHelper.class);
	
	private ICmqBase190Service cmqBaseSvc;
	private ISmqBaseService smqBaseSvc;
	private IMeddraDictService meddraDictSvc;
	private ICmqRelation190Service cmqRelationSvc;
	
	public CmqBaseRelationsTreeHelper(ICmqBase190Service cmqBaseSvc,
			ISmqBaseService smqBaseSvc,
			IMeddraDictService meddraDictSvc,
			ICmqRelation190Service cmqRelationSvc) {
		this.cmqBaseSvc = cmqBaseSvc;
		this.smqBaseSvc = smqBaseSvc;
		this.meddraDictSvc = meddraDictSvc;
		this.cmqRelationSvc = cmqRelationSvc;
	}
	
	/**
	 * 
	 * @param cmqCode
	 * @param requireDrillDown indicates whether it should add dummy nodes for node-expansion for hierarchy drill-down
	 * @return
	 */
	public TreeNode getCmqBaseRelationsRootHierarchy(Long cmqCode, boolean requireDrillDown) {
		ExecutorService executorService = Executors.newFixedThreadPool(7);
		List<CmqRelation190> cmqRelationList = this.cmqRelationSvc.findByCmqCode(cmqCode);
		TreeNode rootNode = new DefaultTreeNode("root"
				, new HierarchyNode("LEVEL", "NAME", "CODE", "SCOPE", "CATEGORY", "WEIGHT", null)
				, null);
		List<Future<Boolean>> futures = new ArrayList<>();
		for (CmqRelation190 cmqRelation : cmqRelationList) {
			RelationsWorker worker  = new RelationsWorker(cmqRelation, rootNode, requireDrillDown);
			Future<Boolean> future = executorService.submit(worker);
			futures.add(future);
		}
		
		int i=0;
		
		for (Future<Boolean> future : futures) {
			try {
				future.get();//get it to make sure we wait till all finish
				i++;
				if((i % 100) == 0) {
					LOG.info("finishes {} futures fetches.", i);
				}
			} catch (InterruptedException | ExecutionException e) {
				LOG.error(e.getMessage());
			}
		}
		
		LOG.info("Finished futures fetch.");
		
		if(requireDrillDown) {
			this.populateChildCmqsByParent(cmqCode, rootNode);
		}
		return rootNode;
	}
	
	public TreeNode getRelationsNodeHierarchy(TreeNode rootNode, TreeNode expandedNode) {
		return getRelationsNodeHierarchy(rootNode, expandedNode, true, false);
	}
	
	public TreeNode getRelationsNodeHierarchy(TreeNode rootNode, TreeNode expandedNode, boolean isRelationView, boolean isParentListView) {
		HierarchyNode hNode = (HierarchyNode) expandedNode.getData();
		boolean isDataFetchCompleted = hNode.isDataFetchCompleted();
		
		if(isDataFetchCompleted) // if data has already been fetched, skip this step
			return rootNode;
		
		IEntity entity = hNode.getEntity();

		// remove the first dummy node placeholder
		HierarchyNode dummyChildData = (HierarchyNode) expandedNode.getChildren().get(0).getData();
		
		if (dummyChildData.isDummyNode()) {
			expandedNode.getChildren().remove(0);
		}

		if (entity instanceof SmqBase190) {
			SmqBase190 smqBase = (SmqBase190) entity;
			
			//first handle all child nodes of this node
			List<SmqBase190> childSmqBaseList = this.smqBaseSvc.findChildSmqByParentSmqCode(smqBase.getSmqCode());
			if(CollectionUtils.isNotEmpty(childSmqBaseList)) {
				Map<Long, TreeNode> smqTreeNodeMap = new HashMap<>();
				List<Long> smqChildCodeList = new ArrayList<>();
				for (SmqBase190 childSmqBase : childSmqBaseList) {
					HierarchyNode childNode = new HierarchyNode();
					childNode.setLevel(SMQLevelHelper.getLabel(childSmqBase.getSmqLevel()));
					childNode.setTerm(childSmqBase.getSmqName());
					childNode.setCode(childSmqBase.getSmqCode().toString());
					childNode.setEntity(childSmqBase);

					if(isRelationView) {
						childNode.markNotEditableInRelationstable();
					}
					// add child to parent
					TreeNode childTreeNode = new DefaultTreeNode(childNode, expandedNode);
					smqChildCodeList.add(childSmqBase.getSmqCode());
					smqTreeNodeMap.put(childSmqBase.getSmqCode(), childTreeNode);
				} // end of for (SmqBase190 childSmqBase : childSmqBaseList)
				
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
					// check if sub level records has further sub-level children
					List<Map<String, Object>> childSmqRelationsCountList = this.smqBaseSvc.findSmqRelationsCountForSmqCodes(subList);
					if((null != childSmqRelationsCountList) && (childSmqRelationsCountList.size() > 0)) {
						ListIterator<Map<String, Object>> li = childSmqRelationsCountList.listIterator();
						while(li.hasNext()) {
							Map<String, Object> map = (Map<String, Object>) li.next();
							if(map.get("SMQ_CODE") != null) {
								Long childSmqCode = (Long)map.get("SMQ_CODE");
								Long count = (Long)map.get("COUNT");
								if(count > 0) {
									// if it has children, create dummy node for node expansion for dynamic loading of subtree nodes
									TreeNode childTreeNode = smqTreeNodeMap.get(childSmqCode);

									// add a dummmy node to show expand arrow
									HierarchyNode dummyNode = new HierarchyNode(null, null, null, null);
									dummyNode.setDummyNode(true);
									new DefaultTreeNode(dummyNode, childTreeNode);
								}
							}
						}
					}//end of if((null != childSmqRelationsCountList) &&.....
				}//end of for (List<Long> subList : choppedLists)
			}
			
			//now add relations of this node if any
			List<SmqRelation190> childRelations = this.smqBaseSvc.findSmqRelationsForSmqCode(smqBase.getSmqCode());
			if (null != childRelations) {
				for (SmqRelation190 childRelation : childRelations) {
					HierarchyNode childRelationNode = new HierarchyNode();
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
					if(isRelationView) {
						childRelationNode.markNotEditableInRelationstable();
					}

					new DefaultTreeNode(childRelationNode, expandedNode);
				}
			}
		} else if(entity instanceof MeddraDictHierarchySearchDto) {
			String parentLevel = hNode.getLevel();
			MeddraDictHierarchySearchDto meddraDictHierarchySearchDto = (MeddraDictHierarchySearchDto)entity;
			
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
			List<MeddraDictHierarchySearchDto> childDtos = this.meddraDictSvc.findChildrenByParentCode(
					childSearchColumnTypePrefix, parentCodeColumnPrefix, Long.valueOf(meddraDictHierarchySearchDto.getCode()));
			
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
				
				if(isRelationView) {
					childNode.markNotEditableInRelationstable();
				}
				
				TreeNode childTreeNode = new DefaultTreeNode(childNode, expandedNode);
				
				//fetch children count of this iterating child node by code of child
				//no need to do this is the childOfChild is LLT since LT is the leaf ode type
				if(!"LLT".equalsIgnoreCase(childLevel)) {
					Long countOfChildrenOfChild = this.meddraDictSvc.findChldrenCountByParentCode(childchildOfChildSearchColumnTypePrefix
										, childSearchColumnTypePrefix, Long.valueOf(childDto.getCode()));
					if(countOfChildrenOfChild > 0) {
						// add a dummmy node to show expand arrow
						HierarchyNode dummyNode = new HierarchyNode(null, null, null, null);
						dummyNode.setDummyNode(true);
						new DefaultTreeNode(dummyNode, childTreeNode);
					}
				}
			}
		} else if(entity instanceof MeddraDictReverseHierarchySearchDto) {
			MeddraDictReverseHierarchySearchDto reverseSearchDto = (MeddraDictReverseHierarchySearchDto)entity;
			String levelOfExpandedNode = hNode.getLevel();
			if("LLT".equalsIgnoreCase(levelOfExpandedNode)) {
				Long lltCode = Long.valueOf(reverseSearchDto.getLltCode());
				List<MeddraDictReverseHierarchySearchDto> childReverseSearchDtos = this.meddraDictSvc.findReverseByCode("LLT_", "PT_", lltCode);
				if(CollectionUtils.isNotEmpty(childReverseSearchDtos)) {
					for (MeddraDictReverseHierarchySearchDto childReverseSearchDto : childReverseSearchDtos) {
						HierarchyNode childNode = this.createMeddraReverseNode(childReverseSearchDto, "PT", hNode.isPrimaryPathFlag());
						if(isRelationView) {
							childNode.markNotEditableInRelationstable();
						}
						TreeNode childTreeNode = new DefaultTreeNode(childNode, expandedNode);
						if(StringUtils.isNotBlank(reverseSearchDto.getHltTerm())) {
							// add a dummmy node to show expand arrow
							HierarchyNode dummyNode = new HierarchyNode(null, null, null, null);
							dummyNode.setDummyNode(true);
							new DefaultTreeNode(dummyNode, childTreeNode);
						}
					}
				}
			} else if ("PT".equalsIgnoreCase(levelOfExpandedNode)) {
				Long ptCode = Long.valueOf(reverseSearchDto.getPtCode());
				if(isRelationView) {
					//fetch children of parent node by code of parent
					List<MeddraDictHierarchySearchDto> childDtos = this.meddraDictSvc.findChildrenByParentCode("LLT_", "PT_", ptCode);
					for (MeddraDictHierarchySearchDto childDto : childDtos) {
						HierarchyNode childNode = this.createMeddraNode(childDto, "LLT");
						childNode.markNotEditableInRelationstable();
						new DefaultTreeNode(childNode, expandedNode);
					}
				} else {
					List<MeddraDictReverseHierarchySearchDto> childReverseSearchDtos = this.meddraDictSvc.findReverseByCode("PT_", "HLT_", ptCode);
					if(CollectionUtils.isNotEmpty(childReverseSearchDtos)) {
						for (MeddraDictReverseHierarchySearchDto childReverseSearchDto : childReverseSearchDtos) {
							boolean isPrimary = false;
							if("Y".equalsIgnoreCase(childReverseSearchDto.getPrimaryPathFlag())) {
								isPrimary = true;
							}
							HierarchyNode childNode = this.createMeddraReverseNode(childReverseSearchDto, "HLT", isPrimary);
							if(isRelationView) {
								childNode.markNotEditableInRelationstable();
							}
							TreeNode childTreeNode = new DefaultTreeNode(childNode, expandedNode);
							if(StringUtils.isNotBlank(reverseSearchDto.getHltTerm())) {
								// add a dummmy node to show expand arrow
								HierarchyNode dummyNode = new HierarchyNode(null, null, null, null);
								dummyNode.setDummyNode(true);
								new DefaultTreeNode(dummyNode, childTreeNode);
							}
						}
					}
				}
			} else if ("HLT".equalsIgnoreCase(levelOfExpandedNode)) {
				Long hltCode = Long.valueOf(reverseSearchDto.getHltCode());
				List<MeddraDictReverseHierarchySearchDto> childReverseSearchDtos = this.meddraDictSvc.findReverseByCode("HLT_", "HLGT_", hltCode);
				if(CollectionUtils.isNotEmpty(childReverseSearchDtos)) {
					for (MeddraDictReverseHierarchySearchDto childReverseSearchDto : childReverseSearchDtos) {
						HierarchyNode childNode = this.createMeddraReverseNode(childReverseSearchDto, "HLGT", hNode.isPrimaryPathFlag());
						if(isRelationView) {
							childNode.markNotEditableInRelationstable();
						}
						TreeNode childTreeNode = new DefaultTreeNode(childNode, expandedNode);
						if(StringUtils.isNotBlank(reverseSearchDto.getHltTerm())) {
							// add a dummmy node to show expand arrow
							HierarchyNode dummyNode = new HierarchyNode(null, null, null, null);
							dummyNode.setDummyNode(true);
							new DefaultTreeNode(dummyNode, childTreeNode);
						}
					}
				}
			} else if ("HLGT".equalsIgnoreCase(levelOfExpandedNode)) {
				Long hlgtCode = Long.valueOf(reverseSearchDto.getHlgtCode());
				List<MeddraDictReverseHierarchySearchDto> childReverseSearchDtos = this.meddraDictSvc.findReverseByCode("HLGT_", "SOC_", hlgtCode);
				if(CollectionUtils.isNotEmpty(childReverseSearchDtos)) {
					for (MeddraDictReverseHierarchySearchDto childReverseSearchDto : childReverseSearchDtos) {
						HierarchyNode childNode = this.createMeddraReverseNode(childReverseSearchDto, "SOC", hNode.isPrimaryPathFlag());
						if(isRelationView) {
							childNode.markNotEditableInRelationstable();
						}
						new DefaultTreeNode(childNode, expandedNode);
					}
				}
			}				
		} else if (entity instanceof CmqBase190) {
			CmqBase190 cmqBase = (CmqBase190) entity;
			Long cmqCode = cmqBase.getCmqCode();
			List<CmqBase190> childCmqBaseList = cmqBaseSvc.findChildCmqsByParentCode(cmqCode);
			
			List<Long> childCmqCodeList = new ArrayList<>();
			Map<Long, TreeNode> childTreeNodes = new HashMap<Long, TreeNode>();
			
			if ((null != childCmqBaseList) && (childCmqBaseList.size() > 0)) {
				for (CmqBase190 childCmqBase : childCmqBaseList) {
					HierarchyNode node = new HierarchyNode();
					if(isParentListView) {
						node.setLevel(childCmqBase.getCmqTypeCd());
					} else {
						node.setLevel(hNode.getLevel()); // levelH
					}
					node.setTerm(childCmqBase.getCmqName());
					node.setCode(childCmqBase.getCmqCode().toString());
					node.setEntity(childCmqBase);
					if(isRelationView) {
						node.markNotEditableInRelationstable();
					}
					TreeNode cmqBaseChildNode = new DefaultTreeNode(node, expandedNode);
					
					childTreeNodes.put(childCmqBase.getCmqCode(), cmqBaseChildNode);
					childCmqCodeList.add(childCmqBase.getCmqCode());
				}
				
				List<Map<String, Object>> childrenOfChildCountsList = this.cmqBaseSvc.findCmqChildCountForParentCmqCode(childCmqCodeList);
				if((null != childrenOfChildCountsList) && (childrenOfChildCountsList.size() > 0)) {
					//first find and fix child nodes stuff
					for (Iterator<Long> it = childCmqCodeList.iterator(); it.hasNext();) {
						ListIterator<Map<String, Object>> li = childrenOfChildCountsList.listIterator();
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
				List<Map<String, Object>> relationsCountsList = this.cmqRelationSvc.findCountByCmqCodes(childCmqCodeList);	
				if((null != relationsCountsList) && (relationsCountsList.size() > 0)) {
					ListIterator<Map<String, Object>> li = relationsCountsList.listIterator();
					while(li.hasNext()) {
						Map<String, Object> map = (Map<String, Object>) li.next();
						if(map.get("CMQ_CODE") != null) {
							Long resultCmqCode = (Long)map.get("CMQ_CODE");
							Long count = (Long)map.get("COUNT");
							if(count > 0) {
								//add a dummy node for this child in parent
								TreeNode parentTreeNode = childTreeNodes.get(cmqCode);
								HierarchyNode dummyNode = new HierarchyNode(null, null,
										null, null);
								dummyNode.setDummyNode(true);
								new DefaultTreeNode(dummyNode, parentTreeNode);
							}
						}
					}
				}
			}
			
			//add cmq relations now
			List<Long> socCodesList = new ArrayList<>();
			List<Long> hlgtCodesList = new ArrayList<>();
			List<Long> hltCodesList = new ArrayList<>();
			List<Long> ptCodesList = new ArrayList<>();
			List<Long> lltCodesList = new ArrayList<>();
			List<Long> smqCodesList = new ArrayList<>();
			
			List<CmqRelation190> existingRelations = this.cmqRelationSvc.findByCmqCode(cmqCode);
			if((null != existingRelations) && (existingRelations.size() > 0)) {
				for (CmqRelation190 cmqRelation : existingRelations) {						
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
						SmqBase190 smqBase = this.smqBaseSvc.findByCode(cmqRelation.getSmqCode());
						HierarchyNode node = this.createSmqBaseNode(smqBase);
						if(isRelationView) {
							node.markNotEditableInRelationstable();
						}
						TreeNode treeNode = new DefaultTreeNode(node, expandedNode);
						
						//add a dummy node for either of the cases, expansion will handle the actuals later
						Long smqBaseChildrenCount = this.smqBaseSvc.findChildSmqCountByParentSmqCode(smqBase.getSmqCode());
						if((null != smqBaseChildrenCount) && (smqBaseChildrenCount > 0)) {
							// add a dummmy node to show expand arrow
							HierarchyNode dummyNode = new HierarchyNode(null, null,
									null, null);
							dummyNode.setDummyNode(true);
							new DefaultTreeNode(dummyNode, treeNode);
						} else {
							Long childSmqrelationsCount = this.smqBaseSvc
									.findSmqRelationsCountForSmqCode(smqBase
											.getSmqCode());
							if((null != childSmqrelationsCount) && (childSmqrelationsCount > 0)) {
								// add a dummmy node to show expand arrow
								HierarchyNode dummyNode = new HierarchyNode(null, null,
										null, null);
								dummyNode.setDummyNode(true);
								new DefaultTreeNode(dummyNode, treeNode);
							}
						}
					}
				}
				
				//find socs now
				if(socCodesList.size() > 0) {
					List<MeddraDictHierarchySearchDto> socDtos = this.meddraDictSvc.findByCodes("SOC_", socCodesList);
					for (MeddraDictHierarchySearchDto meddraDictHierarchySearchDto : socDtos) {
						HierarchyNode node = this.createMeddraNode(meddraDictHierarchySearchDto, "SOC");
						if(isRelationView) {
							node.markNotEditableInRelationstable();
						}
						TreeNode treeNode = new DefaultTreeNode(node, expandedNode);
						
						Long countOfChildren = this.meddraDictSvc.findChldrenCountByParentCode("HLGT_",
								"SOC_", Long.valueOf(meddraDictHierarchySearchDto.getCode()));
						if((null != countOfChildren) && (countOfChildren > 0)) {
							// add a dummmy node to show expand arrow
							HierarchyNode dummyNode = new HierarchyNode(null, null,
									null, null);
							dummyNode.setDummyNode(true);
							new DefaultTreeNode(dummyNode, treeNode);
						}
					}
				}
				
				if(hlgtCodesList.size() > 0) {
					List<MeddraDictHierarchySearchDto> socDtos = this.meddraDictSvc.findByCodes("HLGT_", hlgtCodesList);
					for (MeddraDictHierarchySearchDto meddraDictHierarchySearchDto : socDtos) {
						HierarchyNode node = this.createMeddraNode(meddraDictHierarchySearchDto, "HLGT");
						if(isRelationView) {
							node.markNotEditableInRelationstable();
						}
						TreeNode treeNode = new DefaultTreeNode(node, expandedNode);
						
						Long countOfChildren = this.meddraDictSvc.findChldrenCountByParentCode("HLT_",
								"HLGT_", Long.valueOf(meddraDictHierarchySearchDto.getCode()));
						if((null != countOfChildren) && (countOfChildren > 0)) {
							// add a dummmy node to show expand arrow
							HierarchyNode dummyNode = new HierarchyNode(null, null,
									null, null);
							dummyNode.setDummyNode(true);
							new DefaultTreeNode(dummyNode, treeNode);
						}
					}
				}
				
				if(hltCodesList.size() > 0) {
					List<MeddraDictHierarchySearchDto> socDtos = this.meddraDictSvc.findByCodes("HLT_", hltCodesList);
					for (MeddraDictHierarchySearchDto meddraDictHierarchySearchDto : socDtos) {
						HierarchyNode node = this.createMeddraNode(meddraDictHierarchySearchDto, "HLT");
						if(isRelationView) {
							node.markNotEditableInRelationstable();
						}
						TreeNode treeNode = new DefaultTreeNode(node, expandedNode);
						
						Long countOfChildren = this.meddraDictSvc.findChldrenCountByParentCode("PT_",
								"HLT_", Long.valueOf(meddraDictHierarchySearchDto.getCode()));
						if((null != countOfChildren) && (countOfChildren > 0)) {
							// add a dummmy node to show expand arrow
							HierarchyNode dummyNode = new HierarchyNode(null, null,
									null, null);
							dummyNode.setDummyNode(true);
							new DefaultTreeNode(dummyNode, treeNode);
						}
					}
				}
				
				if(ptCodesList.size() > 0) {
					List<MeddraDictHierarchySearchDto> socDtos = this.meddraDictSvc.findByCodes("PT_", ptCodesList);
					for (MeddraDictHierarchySearchDto meddraDictHierarchySearchDto : socDtos) {
						HierarchyNode node = this.createMeddraNode(meddraDictHierarchySearchDto, "PT");
						if(isRelationView) {
							node.markNotEditableInRelationstable();
						}
						TreeNode treeNode = new DefaultTreeNode(node, expandedNode);
						
						Long countOfChildren = this.meddraDictSvc.findChldrenCountByParentCode("LLT_",
								"PT_", Long.valueOf(meddraDictHierarchySearchDto.getCode()));
						if((null != countOfChildren) && (countOfChildren > 0)) {
							// add a dummmy node to show expand arrow
							HierarchyNode dummyNode = new HierarchyNode(null, null,
									null, null);
							dummyNode.setDummyNode(true);
							new DefaultTreeNode(dummyNode, treeNode);
						}
					}
				}
				
				if(lltCodesList.size() > 0) {
					List<MeddraDictHierarchySearchDto> socDtos = this.meddraDictSvc.findByCodes("LLT_", lltCodesList);
					for (MeddraDictHierarchySearchDto meddraDictHierarchySearchDto : socDtos) {
						HierarchyNode node = this.createMeddraNode(meddraDictHierarchySearchDto, "LLT");
						if(isRelationView) {
							node.markNotEditableInRelationstable();
						}
						TreeNode treeNode = new DefaultTreeNode(node, expandedNode);
					}
				}
			}
		}
		hNode.setDataFetchCompleted(true);
		return rootNode;
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
	private TreeNode findTreenodeByEntityId(TreeNode rtNode, long entityId) {
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
	
	public void populateChildCmqsByParent(Long parentCmqCode, TreeNode rootTreeNode) {
		//now process the cmq parent child relations
		List<CmqBase190> childCmqs = this.cmqBaseSvc.findChildCmqsByParentCode(parentCmqCode);
		if((null != childCmqs) && (childCmqs.size() > 0)) {
			for (CmqBase190 childCmq : childCmqs) {
				HierarchyNode node = this.createCmqBaseNode(childCmq);
				node.setEntity(childCmq);
				TreeNode treeNode = new DefaultTreeNode(node, rootTreeNode);
			
				Long childCount = this.cmqRelationSvc.findCountByCmqCode(childCmq.getCmqCode());
				if((null != childCount) && (childCount > 0)) {
					HierarchyNode dummyNode = new HierarchyNode(null, null, null, null);
					dummyNode.setDummyNode(true);
					new DefaultTreeNode(dummyNode, treeNode);
				}
			}
		}
	}

	public HierarchyNode createCmqBaseNode(CmqBase190 childCmq) {
		HierarchyNode node = new HierarchyNode();
		node.setLevel(childCmq.getCmqTypeCd());
		node.setTerm(childCmq.getCmqName());
		node.setCode(childCmq.getCmqCode().toString());
		node.setCategory("");
		node.setWeight("");
		node.setScope("");
		node.setEntity(childCmq);
		return node;
	}
	
	public HierarchyNode createSmqBaseNode(SmqBase190 smqBase) {
		HierarchyNode node = new HierarchyNode();
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
		}
		node.setTerm(smqRelation.getPtName());
		node.setCode(smqRelation.getPtCode()
				.toString());
		node.setEntity(smqRelation);
		return node;
	}
	

	public HierarchyNode createMeddraNode(MeddraDictHierarchySearchDto searchDto, String level) {
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
	
	private class RelationsWorker implements Callable<Boolean> {
		private CmqRelation190 cmqRelation;
		private TreeNode rootNode;
		private boolean requireDrillDown;
		
		public RelationsWorker(CmqRelation190 cmqRelation, TreeNode rootNode, boolean requireDrillDown) {
			this.cmqRelation = cmqRelation;
			this.rootNode = rootNode;
			this.requireDrillDown = requireDrillDown;
		}
		
		@Override
		public Boolean call() throws Exception {
			try {
				HierarchyNode node = null;
				
				if ((cmqRelation.getSmqCode() != null)
						&& (cmqRelation.getSmqCode() > 0)) {
					if((cmqRelation.getPtCode() != null)
							&& (cmqRelation.getPtCode() > 0)) {
						//this is an smq relation
						SmqRelation190 childRelation = smqBaseSvc.findSmqRelationBySmqAndPtCode(cmqRelation.getSmqCode()
																											, cmqRelation.getPtCode().intValue());
						HierarchyNode childRelationNode = new HierarchyNode();
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
						synchronized (rootNode) {
							new DefaultTreeNode(childRelationNode, rootNode);
						}
					} else {
						//this is an smq base 
						SmqBase190 smqBase190 = smqBaseSvc.findByCode(cmqRelation.getSmqCode());
						if (null != smqBase190) {
							node = createSmqBaseNode(smqBase190);
							node.setCategory((cmqRelation.getTermCategory() == null) ? "" : cmqRelation.getTermCategory());
							node.setScope((cmqRelation.getTermScope() == null) ? "" : cmqRelation.getTermScope());
							node.setWeight((cmqRelation.getTermWeight() == null) ? "" : cmqRelation.getTermWeight() + "");
							TreeNode smqBaseTreeNode = null;
							synchronized (rootNode) {
								smqBaseTreeNode = new DefaultTreeNode(node,rootNode);
							}
							if(requireDrillDown) {
								long childSmqrelationsCount = smqBaseSvc
										.findSmqRelationsCountForSmqCode(smqBase190
												.getSmqCode());
								if (childSmqrelationsCount > 0) {
									// add a dummmy node to show expand arrow
									HierarchyNode dummyNode = new HierarchyNode(null, null,
											null, null);
									dummyNode.setDummyNode(true);
									synchronized (rootNode) {
										new DefaultTreeNode(dummyNode, smqBaseTreeNode);
									}
								}
							}
						}
					}
				} else if ((cmqRelation.getSocCode() != null) && (cmqRelation.getSocCode() > 0)) {
					MeddraDictHierarchySearchDto searchDto = meddraDictSvc
							.findByCode("SOC_", cmqRelation.getSocCode());
					node = createMeddraNode(searchDto, "SOC");
					node.setCategory((cmqRelation.getTermCategory() == null) ? "" : cmqRelation.getTermCategory());
					node.setScope((cmqRelation.getTermScope() == null) ? "" : cmqRelation.getTermScope());
					node.setWeight((cmqRelation.getTermWeight() == null) ? "" : cmqRelation.getTermWeight() + "");
					TreeNode treeNode = null;
					synchronized (rootNode) {
						treeNode = new DefaultTreeNode(node, rootNode);
					}

					if(requireDrillDown) {
						Long childCount = meddraDictSvc.findChldrenCountByParentCode("HLGT_", "SOC_", cmqRelation.getSocCode());
						if((null != childCount) && (childCount > 0)) {
							HierarchyNode dummyNode = new HierarchyNode(null, null, null, null);
							dummyNode.setDummyNode(true);
							synchronized (rootNode) {
								new DefaultTreeNode(dummyNode, treeNode);
							}
						}
					}
				} else if ((cmqRelation.getHlgtCode() != null) && (cmqRelation.getHlgtCode() > 0)) {
					MeddraDictHierarchySearchDto searchDto = meddraDictSvc
							.findByCode("HLGT_", cmqRelation.getHlgtCode());
					node = createMeddraNode(searchDto, "HLGT");
					node.setCategory((cmqRelation.getTermCategory() == null) ? "" : cmqRelation.getTermCategory());
					node.setScope((cmqRelation.getTermScope() == null) ? "" : cmqRelation.getTermScope());
					node.setWeight((cmqRelation.getTermWeight() == null) ? "" : cmqRelation.getTermWeight() + "");
					TreeNode treeNode = null;
					synchronized (rootNode) {
						treeNode = new DefaultTreeNode(node, rootNode);
					}


					if(requireDrillDown) {
						Long childCount = meddraDictSvc.findChldrenCountByParentCode("HLT_", "HLGT_", cmqRelation.getHlgtCode());
						if((null != childCount) && (childCount > 0)) {
							HierarchyNode dummyNode = new HierarchyNode(null, null, null, null);
							dummyNode.setDummyNode(true);
							synchronized (rootNode) {
								new DefaultTreeNode(dummyNode, treeNode);
							}
						}
					}
				} else if ((cmqRelation.getHltCode() != null) && (cmqRelation.getHltCode() > 0)) {
					MeddraDictHierarchySearchDto searchDto = meddraDictSvc
							.findByCode("HLT_", cmqRelation.getHltCode());
					node = createMeddraNode(searchDto, "HLT");
					node.setCategory((cmqRelation.getTermCategory() == null) ? "" : cmqRelation.getTermCategory());
					node.setScope((cmqRelation.getTermScope() == null) ? "" : cmqRelation.getTermScope());
					node.setWeight((cmqRelation.getTermWeight() == null) ? "" : cmqRelation.getTermWeight() + "");
					TreeNode treeNode = null;
					synchronized (rootNode) {
						treeNode = new DefaultTreeNode(node, rootNode);
					}

					if(requireDrillDown) {
						Long childCount = meddraDictSvc.findChldrenCountByParentCode("PT_", "HLT_", cmqRelation.getHltCode());
						if((null != childCount) && (childCount > 0)) {
							HierarchyNode dummyNode = new HierarchyNode(null, null, null, null);
							dummyNode.setDummyNode(true);
							synchronized (rootNode) {
								new DefaultTreeNode(dummyNode, treeNode);
							}
						}
					}
				} else if ((cmqRelation.getPtCode() != null) && (cmqRelation.getPtCode() > 0)) {
					MeddraDictReverseHierarchySearchDto searchDto = meddraDictSvc.findByPtOrLltCode("PT_", cmqRelation.getPtCode());
					node = createMeddraReverseNode(searchDto, "PT", false);
					node.setCategory((cmqRelation.getTermCategory() == null) ? "" : cmqRelation.getTermCategory());
					node.setScope((cmqRelation.getTermScope() == null) ? "" : cmqRelation.getTermScope());
					node.setWeight((cmqRelation.getTermWeight() == null) ? "" : cmqRelation.getTermWeight() + "");
					TreeNode treeNode = null;
					synchronized (rootNode) {
						treeNode = new DefaultTreeNode(node, rootNode);
					}
					
					if(requireDrillDown) {
						Long childCount = meddraDictSvc.findChldrenCountByParentCode("LLT_", "PT_", cmqRelation.getPtCode());
						if((null != childCount) && (childCount > 0)) {
							HierarchyNode dummyNode = new HierarchyNode(null, null, null, null);
							dummyNode.setDummyNode(true);
							synchronized (rootNode) {
								new DefaultTreeNode(dummyNode, treeNode);
							}
						}
					}
				} else if ((cmqRelation.getLltCode() != null)
						&& (cmqRelation.getLltCode() > 0)) {
					MeddraDictReverseHierarchySearchDto searchDto = meddraDictSvc.findByPtOrLltCode("LLT_", cmqRelation.getLltCode());
					node = createMeddraReverseNode(searchDto, "LLT", false);
					node.setCategory((cmqRelation.getTermCategory() == null) ? "" : cmqRelation.getTermCategory());
					node.setScope((cmqRelation.getTermScope() == null) ? "" : cmqRelation.getTermScope());
					node.setWeight((cmqRelation.getTermWeight() == null) ? "" : cmqRelation.getTermWeight() + "");
					synchronized (rootNode) {
						new DefaultTreeNode(node, rootNode);
					}
				}
			} catch (Throwable e) {
				LOG.error(e.getMessage(), e);
				return false;
			}
			return true;
		}
		
	}
}
