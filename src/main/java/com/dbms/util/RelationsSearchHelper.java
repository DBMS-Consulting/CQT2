package com.dbms.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.ListUtils;
import org.apache.commons.lang3.StringUtils;
import org.primefaces.model.DefaultTreeNode;
import org.primefaces.model.TreeNode;

import com.dbms.csmq.HierarchyNode;
import com.dbms.entity.IEntity;
import com.dbms.entity.cqt.CmqBase190;
import com.dbms.entity.cqt.CmqRelation190;
import com.dbms.entity.cqt.SmqBase190;
import com.dbms.entity.cqt.SmqRelation190;
import com.dbms.entity.cqt.dtos.MeddraDictHierarchySearchDto;
import com.dbms.service.ICmqBase190Service;
import com.dbms.service.ICmqRelation190Service;
import com.dbms.service.IMeddraDictService;
import com.dbms.service.ISmqBaseService;

public class RelationsSearchHelper {
	public enum SearchTarget { SMQ_BASE, MEDDRA_DICT, CMQ_BASE }
	
	private ICmqBase190Service cmqBaseSvc;
	private ISmqBaseService smqBaseSvc;
	private IMeddraDictService meddraDictSvc;
	private ICmqRelation190Service cmqRelationSvc;
		
	public RelationsSearchHelper(ICmqBase190Service cmqBaseSvc,
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
	 * @return
	 */
	public TreeNode getRelationsRootHierarchy(Long cmqCode) {
		List<CmqRelation190> cmqRelationList = this.cmqRelationSvc.findByCmqCode(cmqCode);
		TreeNode rootNode = new DefaultTreeNode("root"
				, new HierarchyNode("LEVEL", "NAME", "CODE", "SCOPE", "CATEGORY", "WEIGHT", null)
				, null);
		for (CmqRelation190 cmqRelation : cmqRelationList) {
			HierarchyNode node = null;
			if ((cmqRelation.getSmqCode() != null)
					&& (cmqRelation.getSmqCode() > 0)) {
				SmqBase190 smqBase190 = this.smqBaseSvc.findByCode(cmqRelation.getSmqCode());
				if (null != smqBase190) {
					node = this.createSmqBaseNode(smqBase190);
					node.setCategory(cmqRelation.getTermCategory());
					node.setScope(cmqRelation.getTermScope());
					node.setWeight(cmqRelation.getTermWeight() + "");
					TreeNode smqBaseTreeNode = new DefaultTreeNode(node, rootNode);
					long childSmqrelationsCount = this.smqBaseSvc.findSmqRelationsCountForSmqCode(smqBase190.getSmqCode());
					if (childSmqrelationsCount > 0) {
						// add a dummmy node to show expand arrow
						HierarchyNode dummyNode = new HierarchyNode(null, null, null, null);
						dummyNode.setDummyNode(true);
						new DefaultTreeNode(dummyNode, smqBaseTreeNode);
					}
				}
			} else if ((cmqRelation.getSocCode() != null) && (cmqRelation.getSocCode() > 0)) {
				MeddraDictHierarchySearchDto searchDto = this.meddraDictSvc.findByCode("SOC_", cmqRelation.getSocCode());
				node = this.createMeddraNode(searchDto, "SOC");
				node.setCategory(cmqRelation.getTermCategory());
				node.setScope(cmqRelation.getTermScope());
				node.setWeight(cmqRelation.getTermWeight() + "");
				new DefaultTreeNode(node, rootNode);
			} else if ((cmqRelation.getHlgtCode() != null) && (cmqRelation.getHlgtCode() > 0)) {
				MeddraDictHierarchySearchDto searchDto = this.meddraDictSvc.findByCode("HLGT_", cmqRelation.getHlgtCode());
				node = this.createMeddraNode(searchDto, "HLGT");
				node.setCategory(cmqRelation.getTermCategory());
				node.setScope(cmqRelation.getTermScope());
				node.setWeight(cmqRelation.getTermWeight() + "");
				new DefaultTreeNode(node, rootNode);
			} else if ((cmqRelation.getHltCode() != null) && (cmqRelation.getHltCode() > 0)) {
				MeddraDictHierarchySearchDto searchDto = this.meddraDictSvc.findByCode("HLT_", cmqRelation.getHltCode());
				node = this.createMeddraNode(searchDto, "HLT");
				node.setCategory(cmqRelation.getTermCategory());
				node.setScope(cmqRelation.getTermScope());
				node.setWeight(cmqRelation.getTermWeight() + "");
				new DefaultTreeNode(node, rootNode);
			} else if ((cmqRelation.getPtCode() != null) && (cmqRelation.getPtCode() > 0)) {
				MeddraDictHierarchySearchDto searchDto = this.meddraDictSvc.findByCode("PT_", cmqRelation.getPtCode());
				node = this.createMeddraNode(searchDto, "PT");
				node.setCategory(cmqRelation.getTermCategory());
				node.setScope(cmqRelation.getTermScope());
				node.setWeight(cmqRelation.getTermWeight() + "");
				new DefaultTreeNode(node, rootNode);
			} else if ((cmqRelation.getLltCode() != null)
					&& (cmqRelation.getLltCode() > 0)) {
				MeddraDictHierarchySearchDto searchDto = this.meddraDictSvc.findByCode("LLT_", cmqRelation.getLltCode());
				node = this.createMeddraNode(searchDto, "LLT");
				node.setCategory(cmqRelation.getTermCategory());
				node.setScope(cmqRelation.getTermScope());
				node.setWeight(cmqRelation.getTermWeight() + "");
				new DefaultTreeNode(node, rootNode);
			}
		}
		return rootNode;
	}
	
	public TreeNode getRelationsNodeHierarchy(TreeNode rootNode, TreeNode expandedNode) {
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
		} else if (entity instanceof CmqBase190) {
			CmqBase190 cmqBase = (CmqBase190) entity;
			Long cmqCode = cmqBase.getCmqCode();
			List<CmqBase190> childCmqBaseList = cmqBaseSvc.findChildCmqsByParentCode(cmqCode);
			
			List<Long> childCmqCodeList = new ArrayList<>();
			Map<Long, TreeNode> childTreeNodes = new HashMap<Long, TreeNode>();
			
			if ((null != childCmqBaseList) && (childCmqBaseList.size() > 0)) {
				for (CmqBase190 childCmqBase : childCmqBaseList) {
					HierarchyNode node = new HierarchyNode();
					node.setLevel(hNode.getLevel()); // levelH
					node.setTerm(childCmqBase.getCmqName());
					node.setCode(childCmqBase.getCmqCode().toString());
					node.setEntity(childCmqBase);
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
	

	private HierarchyNode createCmqBaseNode(CmqBase190 childCmq) {
		HierarchyNode node = new HierarchyNode();
		node.setLevel("PRO");
		node.setCode(childCmq.getCmqCode().toString());
		node.setTerm(childCmq.getCmqName());
		node.setCategory("");
		node.setWeight("");
		node.setScope("");
		return node;
	}
	
	private HierarchyNode createSmqBaseNode(SmqBase190 smqBase) {
		HierarchyNode node = new HierarchyNode();

		node.setLevel(SMQLevelHelper.getLabel(smqBase.getSmqLevel()));
		node.setTerm(smqBase.getSmqName());
		node.setCode(smqBase.getSmqCode().toString());
		node.setEntity(smqBase);
		return node;
	}

	private HierarchyNode createMeddraNode(MeddraDictHierarchySearchDto searchDto, String level) {
		HierarchyNode node = new HierarchyNode();
		node.setLevel(level);
		node.setTerm(searchDto.getTerm());
		node.setCode(searchDto.getCode());
		node.setEntity(searchDto);
		return node;
	}
}
