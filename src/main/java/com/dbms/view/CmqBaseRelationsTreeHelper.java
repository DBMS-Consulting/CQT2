package com.dbms.view;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections4.CollectionUtils;
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
	
	final private ICmqBase190Service cmqBaseSvc;
	final private ISmqBaseService smqBaseSvc;
	final private IMeddraDictService meddraDictSvc;
	final private ICmqRelation190Service cmqRelationSvc;
    
    boolean requireDrillDown = true; //indicates whether it should add dummy nodes for node-expansion for hierarchy drill-down
    boolean relationView = true;
    boolean parentListView = false;
	
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
	 * @param requireDrillDown 
	 * @return
	 */
	public TreeNode getCmqBaseRelationsRootHierarchy(Long cmqCode) {
		TreeNode rootNode = new DefaultTreeNode("root"
				, new HierarchyNode("LEVEL", "NAME", "CODE", "SCOPE", "CATEGORY", "WEIGHT", null)
				, null);
        List<CmqRelation190> cmqRelationList = this.cmqRelationSvc.findByCmqCode(cmqCode);
        
        Map<Long, IEntity> socCodesMap = new HashMap<>();
		Map<Long, IEntity> hlgtCodesMap = new HashMap<>();
		Map<Long, IEntity> hltCodesMap = new HashMap<>();
		Map<Long, IEntity> ptCodesMap = new HashMap<>();
		Map<Long, IEntity> lltCodesMap = new HashMap<>();
        
		for (CmqRelation190 cmqRelation : cmqRelationList) {
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
                this.populateSmqTreeNode(cmqRelation, rootNode, cmqCode, false);
            }
        }

        //find socs now
        if(socCodesMap.size() > 0) {
            List<MeddraDictHierarchySearchDto> socDtos;
            List<Long> socCodesList = new ArrayList<>(socCodesMap.keySet());
            socDtos = this.meddraDictSvc.findByCodes("SOC_", socCodesList);
            this.populateCmqRelationTreeNodes(socDtos, rootNode, "SOC", "HLGT", cmqCode, socCodesMap);
        }

        if(hlgtCodesMap.size() > 0) {
            List<MeddraDictHierarchySearchDto> hlgtDtos;
            List<Long> hlgtCodesList = new ArrayList<>(hlgtCodesMap.keySet());
            hlgtDtos = this.meddraDictSvc.findByCodes("HLGT_", hlgtCodesList);
            this.populateCmqRelationTreeNodes(hlgtDtos, rootNode, "HLGT", "HLT", cmqCode, hlgtCodesMap);
        }

        if(hltCodesMap.size() > 0) {
            List<MeddraDictHierarchySearchDto> hltDtos;
            List<Long> hltCodesList = new ArrayList<>(hltCodesMap.keySet());
            hltDtos = this.meddraDictSvc.findByCodes("HLT_", hltCodesList);
            this.populateCmqRelationTreeNodes(hltDtos, rootNode, "HLT", "PT", cmqCode, hltCodesMap);
        }

        if(ptCodesMap.size() > 0) {
            List<MeddraDictReverseHierarchySearchDto> ptDtos;
            List<Long> ptCodesList = new ArrayList<>(ptCodesMap.keySet());
            ptDtos = this.meddraDictSvc.findByPtOrLltCodes("PT_", ptCodesList);
            this.populateCmqRelationTreeNodes2(ptDtos, rootNode, "PT", "LLT", cmqCode, ptCodesMap);
        }

        if(lltCodesMap.size() > 0) {
            List<MeddraDictReverseHierarchySearchDto> lltDtos;
            List<Long> lltCodesList = new ArrayList<>(lltCodesMap.keySet());
            lltDtos = this.meddraDictSvc.findByPtOrLltCodes("LLT_", lltCodesList);
            this.populateCmqRelationTreeNodes2(lltDtos, rootNode, "LLT", null, cmqCode, lltCodesMap);
        }
		
		if(requireDrillDown) {
			this.populateChildCmqsByParent(cmqCode, rootNode);
		}
        return rootNode;
    }
    
    public void populateCmqRelations(Long cmqCode, TreeNode expandedTreeNode, IEntity entityExpanded) {
        List<CmqRelation190> cmqRelationList = this.cmqRelationSvc.findByCmqCode(cmqCode);
        
        Map<Long, IEntity> socCodesMap = new HashMap<>();
		Map<Long, IEntity> hlgtCodesMap = new HashMap<>();
		Map<Long, IEntity> hltCodesMap = new HashMap<>();
		Map<Long, IEntity> ptCodesMap = new HashMap<>();
		Map<Long, IEntity> lltCodesMap = new HashMap<>();
        
		for (CmqRelation190 cmqRelation : cmqRelationList) {
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
                this.populateSmqTreeNode(cmqRelation, expandedTreeNode, cmqCode, true);
            }
        }

        //find socs now
        if(socCodesMap.size() > 0) {
            List<MeddraDictHierarchySearchDto> socDtos;
            List<Long> socCodesList = new ArrayList<>(socCodesMap.keySet());
            socDtos = this.meddraDictSvc.findByCodes("SOC_", socCodesList);
            this.populateCmqRelationTreeNodes(socDtos, expandedTreeNode, "SOC", "HLGT", cmqCode, socCodesMap);
        }

        if(hlgtCodesMap.size() > 0) {
            List<MeddraDictHierarchySearchDto> hlgtDtos;
            List<Long> hlgtCodesList = new ArrayList<>(hlgtCodesMap.keySet());
            hlgtDtos = this.meddraDictSvc.findByCodes("HLGT_", hlgtCodesList);
            this.populateCmqRelationTreeNodes(hlgtDtos, expandedTreeNode, "HLGT", "HLT", cmqCode, hlgtCodesMap);
        }

        if(hltCodesMap.size() > 0) {
            List<MeddraDictHierarchySearchDto> hltDtos;
            List<Long> hltCodesList = new ArrayList<>(hltCodesMap.keySet());
            hltDtos = this.meddraDictSvc.findByCodes("HLT_", hltCodesList);
            this.populateCmqRelationTreeNodes(hltDtos, expandedTreeNode, "HLT", "PT", cmqCode, hltCodesMap);
        }

        if(ptCodesMap.size() > 0) {
            List<MeddraDictHierarchySearchDto> ptDtos;
            List<Long> ptCodesList = new ArrayList<>(ptCodesMap.keySet());
            ptDtos = this.meddraDictSvc.findByCodes("PT_", ptCodesList);
            this.populateCmqRelationTreeNodes(ptDtos, expandedTreeNode, "PT", "LLT", cmqCode, ptCodesMap);
        }

        if(lltCodesMap.size() > 0) {
            List<MeddraDictHierarchySearchDto> lltDtos;
            List<Long> lltCodesList = new ArrayList<>(lltCodesMap.keySet());
            lltDtos = this.meddraDictSvc.findByCodes("LLT_", lltCodesList);
            this.populateCmqRelationTreeNodes(lltDtos, expandedTreeNode, "LLT", null, cmqCode, lltCodesMap);
        }
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
        
        if (entity instanceof CmqBase190) {
            CmqBase190 cmqBase = (CmqBase190) entity;
            Long cmqCode = cmqBase.getCmqCode();
            this.populateCmqBaseChildren(cmqCode, expandedNode);
            this.populateCmqRelations(cmqCode, expandedNode, entity);
        } else if (entity instanceof SmqBase190){
            SmqBase190 smqBase = (SmqBase190) entity;
            this.populateSmqBaseChildren(smqBase.getSmqCode(), expandedNode);
            this.populateSmqRelations(smqBase.getSmqCode(), expandedNode);
        } else if(entity instanceof MeddraDictHierarchySearchDto) {
            String parentLevel = hNode.getLevel();
            MeddraDictHierarchySearchDto meddraDictHierarchySearchDto = (MeddraDictHierarchySearchDto)entity;
            Long dtoCode = Long.valueOf(meddraDictHierarchySearchDto.getCode());
            this.populateMeddraDictHierarchySearchDtoChildren(parentLevel, dtoCode, expandedNode);
        }  else if(entity instanceof MeddraDictReverseHierarchySearchDto) {
            MeddraDictReverseHierarchySearchDto reverseSearchDto = (MeddraDictReverseHierarchySearchDto)entity;
            String levelOfExpandedNode = hNode.getLevel();
            if("LLT".equalsIgnoreCase(levelOfExpandedNode)) {
                Long lltCode = Long.valueOf(reverseSearchDto.getLltCode());
                this.populateMeddraDictReverseHierarchySearchDtoChildren("LLT_", "PT", lltCode, hNode, expandedNode, reverseSearchDto, false);	
            } else if ("PT".equalsIgnoreCase(levelOfExpandedNode)) {
                Long ptCode = Long.valueOf(reverseSearchDto.getPtCode());
                if(relationView) {
                    //fetch children of parent node by code of parent
					List<MeddraDictHierarchySearchDto> childDtos = this.meddraDictSvc.findChildrenByParentCode("LLT_", "PT_", ptCode);
					for (MeddraDictHierarchySearchDto childDto : childDtos) {
						HierarchyNode childNode = this.createMeddraNode(childDto, "LLT", null);
						childNode.markNotEditableInRelationstable();
						new DefaultTreeNode(childNode, expandedNode);
					}
                } else {
                    //if its main view tables then show downward hierarchy else its from hierarchySearch so show reverse hierarchy
                    this.populateMeddraDictReverseHierarchySearchDtoChildren("PT_", "HLT", ptCode, hNode, expandedNode, reverseSearchDto, false);	
                }
            } else if ("HLT".equalsIgnoreCase(levelOfExpandedNode)) {
                Long hltCode = Long.valueOf(reverseSearchDto.getHltCode());
                this.populateMeddraDictReverseHierarchySearchDtoChildren("HLT_", "HLGT", hltCode, hNode, expandedNode, reverseSearchDto, false);	
            } else if ("HLGT".equalsIgnoreCase(levelOfExpandedNode)) {
                Long hlgtCode = Long.valueOf(reverseSearchDto.getHlgtCode());
                this.populateMeddraDictReverseHierarchySearchDtoChildren("HLGT_", "SOC", hlgtCode, hNode, expandedNode, reverseSearchDto, false);	
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

    public TreeNode populateSmqTreeNode(CmqRelation190 cmqRelation, TreeNode expandedTreeNode, Long parentCode, boolean hideDeleteButton) {
        TreeNode treeNode = null;
		HierarchyNode node = null;
        
        //check if it is a PT relation of smq or not
        if((cmqRelation.getPtCode() != null) && (cmqRelation.getPtCode().longValue() > 0)) {
            SmqRelation190 entity2 = this.smqBaseSvc.findSmqRelationBySmqAndPtCode(cmqRelation.getSmqCode(), cmqRelation.getPtCode().intValue());
            node = this.createSmqRelationNode(entity2);
            if(hideDeleteButton) {
            	node.setHideDelete(true);
            }
            treeNode = new DefaultTreeNode(node, expandedTreeNode);
        } else {
            SmqBase190 entity2 = this.smqBaseSvc.findByCode(cmqRelation.getSmqCode());
            node = this.createSmqBaseNode(entity2, cmqRelation);
            if(hideDeleteButton) {
            	node.setHideDelete(true);
            }
            treeNode = new DefaultTreeNode(node, expandedTreeNode);
            
            if(requireDrillDown) {
                //add a dummy node for either of the cases, expansion will handle the actuals later
                Long smqBaseChildrenCount;
                smqBaseChildrenCount = this.smqBaseSvc.findChildSmqCountByParentSmqCode(((SmqBase190)entity2).getSmqCode());
                if((null != smqBaseChildrenCount) && (smqBaseChildrenCount > 0)) {
                    // add a dummmy node to show expand arrow
                    createNewDummyNode(treeNode);
                } else {
                    Long childSmqrelationsCount;
                    childSmqrelationsCount = this.smqBaseSvc.findSmqRelationsCountForSmqCode(((SmqBase190)entity2).getSmqCode());
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
			, String nodeType, String childNodeType, Long parentCode, Map<Long, IEntity> cmqRelationsMap
            ) {
        Map<Long, TreeNode> addedNodes = new HashMap<>();
        List<Long> dtoCodes = new ArrayList<>(dtos.size());
        boolean isRootNodeExpanded = isRootListNode(expandedTreeNode);

        for (MeddraDictHierarchySearchDto m : dtos) {
            final Long c = Long.valueOf(m.getCode());
            HierarchyNode node = this.createMeddraNode(m, nodeType, cmqRelationsMap.get(c));
            if(!isRootNodeExpanded && relationView) {
                node.markNotEditableInRelationstable();
            }

            TreeNode treeNode = new DefaultTreeNode(node, expandedTreeNode);

            addedNodes.put(c, treeNode);
            dtoCodes.add(c);
        }
        
        if(requireDrillDown) {
            List<Map<String, Object>> countsOfChildren = this.meddraDictSvc.findChildrenCountByParentCodes(childNodeType + "_"
                                            , nodeType + "_", dtoCodes);

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
	}
    
    public void populateCmqRelationTreeNodes2(List<MeddraDictReverseHierarchySearchDto> dtos, TreeNode expandedTreeNode
			, String nodeType, String childNodeType, Long parentCode, Map<Long, IEntity> cmqRelationsMap
            ) {
        Map<Long, TreeNode> addedNodes = new HashMap<>();
        List<Long> dtoCodes = new ArrayList<>(dtos.size());

        for (MeddraDictReverseHierarchySearchDto m : dtos) {
            final Long c = Long.valueOf(m.getPtCode());
            HierarchyNode node = this.createMeddraReverseNode(m, nodeType,false, cmqRelationsMap.get(c));
            TreeNode treeNode = new DefaultTreeNode(node, expandedTreeNode);

            addedNodes.put(c, treeNode);
            dtoCodes.add(c);
        }
        
        if(requireDrillDown && childNodeType!=null) {
            List<Map<String, Object>> countsOfChildren = this.meddraDictSvc.findChildrenCountByParentCodes(childNodeType + "_"
                                            , nodeType + "_", dtoCodes);

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
	}
    
    public void populateCmqBaseChildren(Long cmqCode, TreeNode expandedTreeNode) {
		List<? extends IEntity> childCmqBaseList;

        childCmqBaseList = cmqBaseSvc.findChildCmqsByParentCode(cmqCode);
		
		List<Long> childCmqCodeList = new ArrayList<>();
		Map<Long, TreeNode> childTreeNodes = new HashMap<>();
		
		if ((null != childCmqBaseList) && (childCmqBaseList.size() > 0)) {
			for (IEntity entity : childCmqBaseList) {
				HierarchyNode node = new HierarchyNode();
                CmqBase190 childCmqBase = (CmqBase190) entity;
                if(parentListView) {
                    node.setLevel(childCmqBase.getCmqTypeCd());
                } else {
                    node.setLevel(((HierarchyNode)expandedTreeNode.getData()).getLevel()); // levelH
                }
                node.setTerm(childCmqBase.getCmqName());
                node.setCode(childCmqBase.getCmqCode().toString());
                node.setEntity(childCmqBase);
                if(relationView) {
                    node.markNotEditableInRelationstable();
                }
                TreeNode cmqBaseChildNode = new DefaultTreeNode(node, expandedTreeNode);

                childTreeNodes.put(childCmqBase.getCmqCode(), cmqBaseChildNode);
                childCmqCodeList.add(childCmqBase.getCmqCode());
			}
			
			List<Map<String, Object>> childrenOfChildCountsList = null;
            childrenOfChildCountsList = this.cmqBaseSvc.findCmqChildCountForParentCmqCodes(childCmqCodeList);
			
			if((null != childrenOfChildCountsList) && (childrenOfChildCountsList.size() > 0)) {
				//first find and fix child nodes stuff
				for (Map<String, Object> map : childrenOfChildCountsList) {
                    if(map.get("CMQ_CODE") != null) {
                        Long childCmqCode = (Long)map.get("CMQ_CODE");
                        if((Long)map.get("COUNT") > 0) {
                            //add a dummy node for this child in parent
                            createNewDummyNode(childTreeNodes.get(childCmqCode));
                            childTreeNodes.remove(childCmqCode);
                            childCmqCodeList.remove(childCmqCode);
                        }
                    }
				}
			}
			
			//now find relations for those who don't have children
			List<Map<String, Object>> relationsCountsList = this.cmqRelationSvc.findCountByCmqCodes(childCmqCodeList);
				
			if((null != relationsCountsList) && (relationsCountsList.size() > 0)) {
				for(Map<String, Object> map: relationsCountsList) {
					if(map.get("CMQ_CODE") != null) {
						Long resultCmqCode = (Long)map.get("CMQ_CODE");
						if((Long)map.get("COUNT") > 0) {
							//add a dummy node for this child in parent
							createNewDummyNode(childTreeNodes.get(resultCmqCode));
						}
					}
				}
			}
		}
	}
    
    @SuppressWarnings({ "rawtypes", "unchecked" })
	public void populateSmqBaseChildren(Long smqCode, TreeNode expandedTreeNode) {
        
		List<SmqBase190> childSmqBaseList = this.smqBaseSvc.findChildSmqByParentSmqCode(smqCode);

        if(CollectionUtils.isNotEmpty(childSmqBaseList)) {
			Map<Long, TreeNode> smqTreeNodeMap = new HashMap<>();
			List<Long> smqChildCodeList = new ArrayList<>();
			for (SmqBase190 childSmqBase : childSmqBaseList) {
                final Long childSmqCode = childSmqBase.getSmqCode();
				HierarchyNode childNode = new HierarchyNode();
                if (null != childSmqBase.getSmqLevel())
                    childNode.setLevel(SMQLevelHelper.getLabel(childSmqBase.getSmqLevel()));
                childNode.setTerm(childSmqBase.getSmqName());
                childNode.setCode(childSmqBase.getSmqCode().toString());
                childNode.setEntity(childSmqBase);
                if(relationView) {
                    childNode.markNotEditableInRelationstable();
                }

				// add child to parent
				TreeNode childTreeNode = new DefaultTreeNode(childNode, expandedTreeNode);
                smqChildCodeList.add(childSmqBase.getSmqCode());
				smqTreeNodeMap.put(childSmqCode, childTreeNode);
			} // end of for
			
			//find smqrelations of all child smqs
            List<Map<String, Object>> childSmqRelationsCountList = this.smqBaseSvc.findSmqRelationsCountForSmqCodes(smqChildCodeList);

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
    
    public void populateSmqRelations(Long smqCode, TreeNode expandedTreeNode) {
        List<SmqRelation190> childRelations = this.smqBaseSvc.findSmqRelationsForSmqCode(smqCode);

		if (null != childRelations) {
			for (SmqRelation190 childRelation : childRelations) {
				boolean isChildSmqNode = false;
				HierarchyNode childRelationNode = new HierarchyNode();
				if (childRelation.getSmqLevel() == 0) {
					SmqBase190 childSmq = new SmqBase190();
					childSmq.setSmqCode(childRelation.getPtCode().longValue());
					childSmq.setSmqName(childRelation.getPtName());
					childRelationNode.setLevel("Child SMQ");
					childRelationNode.setEntity(childSmq);
					isChildSmqNode = true;
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
				
                if(relationView) {
                    //childRelationNode.markNotEditableInRelationstable();
                    childRelationNode.setHideDelete(true);;
                }

				TreeNode treeNode = new DefaultTreeNode(childRelationNode, expandedTreeNode);
				if(isChildSmqNode) {
					this.createNewDummyNode(treeNode);
				}
			}
		}
	}
    
    public void populateMeddraDictHierarchySearchDtoChildren(String parentLevel, Long dtoCode, TreeNode expandedTreeNode) {
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
        childDtos = this.meddraDictSvc.findChildrenByParentCode(childSearchColumnTypePrefix, parentCodeColumnPrefix, dtoCode);
        
        Map<Long, TreeNode> nodesMap = new HashMap<>();
        List<Long> nodesMapKeys = new LinkedList<>();
		for (MeddraDictHierarchySearchDto childDto : childDtos) {
			HierarchyNode childNode = this.createMeddraNode(childDto, childLevel, null);
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
			
			if(relationView) {
				childNode.markNotEditableInRelationstable();
			}
			
			TreeNode childTreeNode = new DefaultTreeNode(childNode, expandedTreeNode);
			
			//fetch children count of this iterating child node by code of child
			//no need to do this is the childOfChild is LLT since LT is the leaf ode type
			if(!"LLT".equalsIgnoreCase(childLevel)) {
                nodesMap.put(Long.valueOf(childDto.getCode()), childTreeNode);
                nodesMapKeys.add(Long.valueOf(childDto.getCode()));
            }
		}
    
        List<Map<String, Object>> countsOfChildren;
        countsOfChildren = this.meddraDictSvc.findChildrenCountByParentCodes(childchildOfChildSearchColumnTypePrefix,
                childSearchColumnTypePrefix, nodesMapKeys);

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
    
    public void populateMeddraDictReverseHierarchySearchDtoChildren(String searchColumnTypePrefix, String partitionColumn
            , Long code, HierarchyNode hierarchyNode, TreeNode expandedTreeNode
            , MeddraDictReverseHierarchySearchDto reverseSearchDto
            , boolean chekcForPrimaryPath) {
        boolean checkPrimaryPathTemp = false;
		String partitionColumnPrefix = partitionColumn +"_";
		List<MeddraDictReverseHierarchySearchDto> childReverseSearchDtos = this.meddraDictSvc.findReverseByCode(searchColumnTypePrefix
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
					childNode = this.createMeddraReverseNode(childReverseSearchDto, partitionColumn, isPrimary, null);
					childNode.setPrimarypathCheckDone(true);
				} else {
					childNode = this.createMeddraReverseNode(childReverseSearchDto, partitionColumn, hierarchyNode.isPrimaryPathFlag(), null);
				}
				
				if(relationView) {
					childNode.markNotEditableInRelationstable();
				}
				
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
    
    private HierarchyNode createCmqBaseNode(CmqBase190 childCmq) {
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
	
	public HierarchyNode createSmqBaseNode(SmqBase190 smqBase, CmqRelation190 cmqRelation) {
		HierarchyNode node = new HierarchyNode();
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
		return node;
	}

	public HierarchyNode createMeddraNode(MeddraDictHierarchySearchDto searchDto, String level, IEntity relationEntity) {
		HierarchyNode node = new HierarchyNode();
		node.setLevel(level);
		node.setTerm(searchDto.getTerm());
		node.setCode(searchDto.getCode());
		node.setEntity(searchDto);
        node.setRelationEntity(relationEntity);
        if(relationEntity != null && relationEntity instanceof CmqRelation190) {
            CmqRelation190 cmqRelation = (CmqRelation190)relationEntity;
            node.setCategory((cmqRelation.getTermCategory() == null) ? "" : cmqRelation.getTermCategory());
            node.setScope((cmqRelation.getTermScope() == null) ? "" : cmqRelation.getTermScope());
            node.setWeight((cmqRelation.getTermWeight() == null) ? "" : cmqRelation.getTermWeight() + "");
        }
		return node;
	}
	
	public HierarchyNode createMeddraReverseNode(MeddraDictReverseHierarchySearchDto searchDto, String level, boolean isPrimary, IEntity relationEntity) {
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
        node.setRelationEntity(relationEntity);
        if(relationEntity!=null && relationEntity instanceof CmqRelation190) {
            CmqRelation190 cmqRelation = (CmqRelation190) relationEntity;
            node.setCategory((cmqRelation.getTermCategory() == null) ? "" : cmqRelation.getTermCategory());
            node.setScope((cmqRelation.getTermScope() == null) ? "" : cmqRelation.getTermScope());
            node.setWeight((cmqRelation.getTermWeight() == null) ? "" : cmqRelation.getTermWeight() + "");
        }
		return node;
	}
    
	public HierarchyNode createSmqRelationNode(SmqRelation190 smqRelation) {
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
		node.setCode(smqRelation.getPtCode().toString());
		node.setEntity(smqRelation);
		return node;
	}
    
    public TreeNode createNewDummyNode(TreeNode parentNode) {
        HierarchyNode dummyNode = new HierarchyNode(null, null, null, null);
        dummyNode.setDummyNode(true);
        return new DefaultTreeNode(dummyNode, parentNode);
    }
    
    public boolean isRequireDrillDown() {
        return requireDrillDown;
    }

    public void setRequireDrillDown(boolean requireDrillDown) {
        this.requireDrillDown = requireDrillDown;
    }

    public boolean isRelationView() {
        return relationView;
    }

    public void setRelationView(boolean relationView) {
        this.relationView = relationView;
    }

    public boolean isParentListView() {
        return parentListView;
    }

    public void setParentListView(boolean parentView) {
        this.parentListView = parentView;
    }
    
    public boolean isRootListNode(TreeNode treeNode) {
		return (StringUtils.equalsIgnoreCase(treeNode.getType(), "root"));
	}

}
