package com.dbms.view;

import com.dbms.csmq.CSMQBean;
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
import com.dbms.entity.cqt.CmqBaseTarget;
import com.dbms.entity.cqt.CmqRelation190;
import com.dbms.entity.cqt.CmqRelationTarget;
import com.dbms.entity.cqt.SmqBase190;
import com.dbms.entity.cqt.SmqBaseTarget;
import com.dbms.entity.cqt.SmqRelation190;
import com.dbms.entity.cqt.SmqRelationTarget;
import com.dbms.entity.cqt.dtos.MeddraDictHierarchySearchDto;
import com.dbms.entity.cqt.dtos.MeddraDictReverseHierarchySearchDto;
import com.dbms.service.ICmqBase190Service;
import com.dbms.service.ICmqBaseTargetService;
import com.dbms.service.ICmqRelation190Service;
import com.dbms.service.ICmqRelationTargetService;
import com.dbms.service.IMeddraDictService;
import com.dbms.service.IMeddraDictTargetService;
import com.dbms.service.ISmqBaseService;
import com.dbms.service.ISmqBaseTargetService;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import org.primefaces.event.NodeExpandEvent;

public class IARelationsTreeHelper {
	public enum SearchTarget { SMQ_BASE, MEDDRA_DICT, CMQ_BASE }
	
    private ICmqBase190Service cmqBaseCurrentService;
	private ISmqBaseService smqBaseCurrentService;
	private IMeddraDictService meddraDictCurrentService;
	private ICmqRelation190Service cmqRelationCurrentService;
    
	private ICmqBaseTargetService cmqBaseTargetService;
	private ISmqBaseTargetService smqBaseTargetService;
	private IMeddraDictTargetService meddraDictTargetService;
	private ICmqRelationTargetService cmqRelationTargetService;
    
    public IARelationsTreeHelper(
            ICmqBase190Service cmqBaseService,
            ISmqBaseService smqBaseService,
            IMeddraDictService meddraDictService,
            ICmqRelation190Service cmqRelationService) {
        
        this.cmqBaseCurrentService = cmqBaseService;
		this.smqBaseCurrentService = smqBaseService;
		this.meddraDictCurrentService = meddraDictService;
		this.cmqRelationCurrentService = cmqRelationService;
	}
    
    public IARelationsTreeHelper(
            ICmqBaseTargetService cmqBaseSvc,
			ISmqBaseTargetService smqBaseSvc,
			IMeddraDictTargetService meddraDictSvc,
			ICmqRelationTargetService cmqRelationSvc) {      
		this.cmqBaseTargetService = cmqBaseSvc;
		this.smqBaseTargetService = smqBaseSvc;
		this.meddraDictTargetService = meddraDictSvc;
		this.cmqRelationTargetService = cmqRelationSvc;
	}
    
    public IARelationsTreeHelper(
            ICmqBase190Service cmqBaseService,
            ISmqBaseService smqBaseService,
            IMeddraDictService meddraDictService,
            ICmqRelation190Service cmqRelationService,
            ICmqBaseTargetService cmqBaseSvc,
			ISmqBaseTargetService smqBaseSvc,
			IMeddraDictTargetService meddraDictSvc,
			ICmqRelationTargetService cmqRelationSvc) {
        
        this.cmqBaseCurrentService = cmqBaseService;
		this.smqBaseCurrentService = smqBaseService;
		this.meddraDictCurrentService = meddraDictService;
		this.cmqRelationCurrentService = cmqRelationService;
        
        this.cmqBaseTargetService = cmqBaseSvc;
		this.smqBaseTargetService = smqBaseSvc;
		this.meddraDictTargetService = meddraDictSvc;
		this.cmqRelationTargetService = cmqRelationSvc;
	}
		
	
	
	public void onNodeExpandCurrentTable(TreeNode rootNode, NodeExpandEvent event) {
		TreeNode expandedTreeNode = event.getTreeNode();
		HierarchyNode hierarchyNode = (HierarchyNode) expandedTreeNode.getData();
		boolean isDataFetchCompleted = hierarchyNode.isDataFetchCompleted();
		if (!isDataFetchCompleted) {
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
				this.populateCmqRelations(cmqCode, expandedTreeNode, "current", null);
				
				//Color
				if (CSMQBean.IMPACT_TYPE_ICC.equals(cmqBase.getImpactType()) ||
                        CSMQBean.IMPACT_TYPE_IMPACTED.equalsIgnoreCase(cmqBase.getImpactType()))
					hierarchyNode.setRowStyleClass("blue-colored");
			} else if (entity instanceof SmqBase190){
				SmqBase190 smqBase = (SmqBase190) entity;
				this.populateSmqBaseChildren(smqBase.getSmqCode(), expandedTreeNode, "current", null);
				this.populateSmqRelations(smqBase.getSmqCode(), expandedTreeNode, "current", null);
				
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
	
	public void onNodeExpandTargetTable(TreeNode rootNode, NodeExpandEvent event) {
		String uiSourceOfEvent =  (String) event.getComponent().getAttributes().get("uiEventSourceName");
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
				this.populateCmqBaseChildren(cmqCode, expandedTreeNode, "target", uiSourceOfEvent);
				this.populateCmqRelations(cmqCode, expandedTreeNode, "target", uiSourceOfEvent);
				
				//Color
				if (CSMQBean.IMPACT_TYPE_ICC.equals(cmqBase.getImpactType()) ||
                        CSMQBean.IMPACT_TYPE_IMPACTED.equalsIgnoreCase(cmqBase.getImpactType()))
					hierarchyNode.setRowStyleClass("blue-colored");
			} else if (entity instanceof SmqBaseTarget){
				SmqBaseTarget smqBase = (SmqBaseTarget) entity;
				this.populateSmqBaseChildren(smqBase.getSmqCode(), expandedTreeNode, "target", uiSourceOfEvent);
				this.populateSmqRelations(smqBase.getSmqCode(), expandedTreeNode, "target", uiSourceOfEvent);
				
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
								, expandedTreeNode, reverseSearchDto, true, uiSourceOfEvent);	
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
	
	public HierarchyNode createSmqBaseCurrrentNode(SmqBase190 smqBase) {
		HierarchyNode node = null;
		if(null != smqBase) {
			node = new HierarchyNode();
            switch(smqBase.getSmqLevel()) {
                case 1:
                    node.setLevel("SMQ1");
                    break;
                case 2:
                    node.setLevel("SMQ2");
                    break;
                case 3:
                    node.setLevel("SMQ3");
                    break;
                case 4:
                    node.setLevel("SMQ4");
                    break;
                case 5:
                    node.setLevel("SMQ5");
                    break;
            }
			node.setTerm(smqBase.getSmqName());
			node.setCode(smqBase.getSmqCode().toString());
			node.setEntity(smqBase);
		}
		return node;
	}
	
	public HierarchyNode createSmqBaseTargetNode(SmqBaseTarget smqBase) {
		HierarchyNode node = null;
		if(null != smqBase) {
			node = new HierarchyNode();
            switch(smqBase.getSmqLevel()) {
                case 1:
                    node.setLevel("SMQ1");
                    break;
                case 2:
                    node.setLevel("SMQ2");
                    break;
                case 3:
                    node.setLevel("SMQ3");
                    break;
                case 4:
                    node.setLevel("SMQ4");
                    break;
                case 5:
                    node.setLevel("SMQ5");
                    break;
            }
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
    
   
    
    public void setCMQTargetNodeStyle(HierarchyNode node, CmqRelationTarget cmqRelationTarget) {
		if (cmqRelationTarget.getRelationImpactType() != null) {
			if("MQM".equalsIgnoreCase(cmqRelationTarget.getRelationImpactType())) {
				node.setRowStyleClass("green-colored");
			}
			if("NCH".equals(cmqRelationTarget.getRelationImpactType())) {
				node.setRowStyleClass("italic");
			}
			if("PDL".equals(cmqRelationTarget.getRelationImpactType())
					|| "PDH".equals(cmqRelationTarget.getRelationImpactType())
					|| "HDH".equals(cmqRelationTarget.getRelationImpactType())
					|| "HDS".equals(cmqRelationTarget.getRelationImpactType())
					|| "LDP".equals(cmqRelationTarget.getRelationImpactType())
					|| "LPP".equals(cmqRelationTarget.getRelationImpactType())
					|| "HPP".equals(cmqRelationTarget.getRelationImpactType())
					|| "NTR".equals(cmqRelationTarget.getRelationImpactType())) {
				node.setRowStyleClass("orange-colored");
			}
			
			if("LCN".equals(cmqRelationTarget.getRelationImpactType()))
				node.setRowStyleClass("mauve-colored");
			if ("SCH".equals(cmqRelationTarget.getRelationImpactType()))
				node.setRowStyleClass("blue-colored");
		}
		else
			node.setRowStyleClass("none");
	}
    
	public void setCMQCurrentNodeStyle(HierarchyNode node,	CmqRelation190 cmqRelation) {
		if (cmqRelation.getRelationImpactType() != null) {
			if("NCH".equals(cmqRelation.getRelationImpactType())) {
				node.setRowStyleClass("italic");
			}
			if("PDL".equals(cmqRelation.getRelationImpactType())
					|| "PDH".equals(cmqRelation.getRelationImpactType())
					|| "HDH".equals(cmqRelation.getRelationImpactType())
					|| "HDS".equals(cmqRelation.getRelationImpactType())
					|| "DTR".equals(cmqRelation.getRelationImpactType())
					|| "MRG".equals(cmqRelation.getRelationImpactType())
					|| "LCN".equals(cmqRelation.getRelationImpactType())
					|| "HPP".equals(cmqRelation.getRelationImpactType())
					|| "LPP".equals(cmqRelation.getRelationImpactType())
					|| "HNP".equals(cmqRelation.getRelationImpactType())
					|| "LDP".equals(cmqRelation.getRelationImpactType())) {
				node.setRowStyleClass("red-colored");
			}
			if ("SCH".equals(cmqRelation.getRelationImpactType()) || "ICC".equals(cmqRelation.getRelationImpactType()))
				node.setRowStyleClass("blue-colored");
//			if ("SWC".equals(cmqRelation.getRelationImpactType()))
//				node.setRowStyleClass("pink-colored");
		}
		
		else
			node.setRowStyleClass("none");
		
	}

	public void setCurrentMeddraColor(MeddraDictHierarchySearchDto meddra, HierarchyNode node) {
		if((meddra.getMovedPt() != null && "LDH".equalsIgnoreCase(meddra.getMovedPt()) && (meddra.getPtCode() != null || meddra.getLltCode() != null))
				|| (meddra.getMovedLlt() != null && "LDP".equalsIgnoreCase(meddra.getMovedLlt()) && meddra.getLltCode() != null)
				|| (meddra.getMovedHlt() != null && "HDH".equalsIgnoreCase(meddra.getMovedHlt()) && (meddra.getLltCode() != null || meddra.getHltCode() != null || meddra.getPtCode() != null))
				|| (meddra.getMovedHlgt() != null && "HDS".equalsIgnoreCase(meddra.getMovedHlgt()) && (meddra.getHlgtCode() != null || meddra.getLltCode() != null || meddra.getHltCode() != null || meddra.getPtCode() != null))
				|| (meddra.getDemotedPt() != null && "PDL".equalsIgnoreCase(meddra.getDemotedPt()) && (meddra.getPtCode() != null || meddra.getLltCode() != null))
				|| (meddra.getPromotedLlt() != null && "LPP".equalsIgnoreCase(meddra.getPromotedLlt()) && meddra.getLltCode() != null)
				|| (meddra.getMergedHlgt() != null && "MRG".equalsIgnoreCase(meddra.getMergedHlgt()) && (meddra.getHlgtCode() != null || meddra.getLltCode() != null || meddra.getHltCode() != null || meddra.getPtCode() != null))
				|| (meddra.getPrimarySocChange() != null && "HPN".equalsIgnoreCase(meddra.getPrimarySocChange()) 
						&& (meddra.getHlgtCode() != null || meddra.getLltCode() != null || meddra.getHltCode() != null || meddra.getPtCode() != null))
				|| (meddra.getMergedHlt() != null && "MRG".equalsIgnoreCase(meddra.getMergedHlt()) && (meddra.getLltCode() != null || meddra.getHltCode() != null || meddra.getPtCode() != null))) {
			node.setRowStyleClass("red-colored");
		}
		
		if((meddra.getHlgtNameChanged() != null && "NCH".equalsIgnoreCase(meddra.getHlgtNameChanged()) && meddra.getHlgtCode() != null)
				|| (meddra.getHltNameChanged() != null && "NCH".equalsIgnoreCase(meddra.getHltNameChanged()) && meddra.getHltCode() != null)
				|| (meddra.getPtNameChanged()!= null && "NCH".equalsIgnoreCase(meddra.getPtNameChanged()) && meddra.getPtCode() != null)
				|| (meddra.getSocNameChanged()!= null && "NCH".equalsIgnoreCase(meddra.getSocNameChanged()) && meddra.getSocCode() != null)
				|| (meddra.getLltNameChanged() != null && "NCH".equalsIgnoreCase(meddra.getLltNameChanged()) && meddra.getLltCode() != null)) {
			node.setRowStyleClass("italic");
		}
		
		if (meddra.getPrimarySocChange() != null && "HPP".equalsIgnoreCase(meddra.getPrimarySocChange()))
				node.setRowStyleClass("none");
	}
	
	 public void setTargetMeddraColor(MeddraDictHierarchySearchDto meddra, HierarchyNode node) {
	    	if ((meddra.getPrimarySocChange() != null && "HPP".equalsIgnoreCase(meddra.getPrimarySocChange()) && meddra.getSocCode() != null)
	    			|| (meddra.getPrimarySocChange() != null && "HNP".equalsIgnoreCase(meddra.getPrimarySocChange()) && meddra.getSocCode() != null)) {
	    		node.setRowStyleClass("none");
	        }
	    	if (meddra.getLltCurrencyChange() != null && "LCN".equalsIgnoreCase(meddra.getLltCurrencyChange()) && meddra.getLltCode() != null)
	    		node.setRowStyleClass("mauve-colored");	
	    	
			if((meddra.getNewLlt() != null && "NTR".equalsIgnoreCase(meddra.getNewLlt()) && meddra.getLltCode() != null)
					|| (meddra.getNewPt() != null && "NTR".equalsIgnoreCase(meddra.getNewPt()) && meddra.getPtCode() != null)
					|| (meddra.getNewHlt() != null && "NTR".equalsIgnoreCase(meddra.getNewHlt()) && meddra.getHltCode() != null)
					|| (meddra.getNewHlgt() != null && "NTR".equalsIgnoreCase(meddra.getNewHlgt()) && meddra.getHlgtCode() != null)
					|| (meddra.getNewSoc() != null && "NTR".equalsIgnoreCase(meddra.getNewSoc()) && meddra.getSocCode() != null)
					|| (meddra.getMovedHlt() != null && "NTR".equalsIgnoreCase(meddra.getMovedHlt()) && (meddra.getHltCode() != null || meddra.getLltCode() != null || meddra.getPtCode() != null))
					|| (meddra.getMovedHlgt()!= null && "NTR".equalsIgnoreCase(meddra.getMovedHlgt()) && (meddra.getHlgtCode() != null ||meddra.getHltCode() != null || meddra.getLltCode() != null || meddra.getPtCode() != null))
					|| (meddra.getMovedPt() != null && "LDH".equalsIgnoreCase(meddra.getMovedPt()) && (meddra.getLltCode() != null || meddra.getPtCode() != null))
					|| (meddra.getDemotedPt() != null && "PDL".equalsIgnoreCase(meddra.getDemotedPt()) && (meddra.getPtCode() != null || meddra.getLltCode() != null))
					|| (meddra.getPromotedLlt() != null && "LPP".equalsIgnoreCase(meddra.getPromotedLlt()) && meddra.getLltCode() != null)
					|| (meddra.getPrimarySocChange() != null && "HNP".equalsIgnoreCase(meddra.getPrimarySocChange()) && meddra.getSocCode() != null)
					|| (meddra.getMovedLlt() != null && "LDP".equalsIgnoreCase(meddra.getMovedLlt()))
					|| (meddra.getNewSuccessorPt() != null && "SDP".equalsIgnoreCase(meddra.getNewSuccessorPt()) && meddra.getPtCode() != null)
					|| (meddra.getMovedHlt() != null && "HDH".equalsIgnoreCase(meddra.getMovedHlt()) && (meddra.getHltCode() != null || meddra.getLltCode() != null || meddra.getPtCode() != null))
					|| (meddra.getMovedHlgt() != null && "HDS".equalsIgnoreCase(meddra.getMovedHlgt())) && (meddra.getHlgtCode() != null || meddra.getHltCode() != null || meddra.getLltCode() != null || meddra.getPtCode() != null)) {
				node.setRowStyleClass("orange-colored");
			}
			
			if((meddra.getHlgtNameChanged() != null && "NCH".equalsIgnoreCase(meddra.getHlgtNameChanged()) && meddra.getHlgtCode() != null)
					|| (meddra.getHltNameChanged() != null && "NCH".equalsIgnoreCase(meddra.getHltNameChanged()) && meddra.getHltCode() != null)
					|| (meddra.getPtNameChanged()!= null && "NCH".equalsIgnoreCase(meddra.getPtNameChanged()) && meddra.getPtCode() != null)
					|| (meddra.getSocNameChanged()!= null && "NCH".equalsIgnoreCase(meddra.getSocNameChanged()) && meddra.getSocCode() != null)
					|| (meddra.getLltNameChanged() != null && "NCH".equalsIgnoreCase(meddra.getLltNameChanged()) && meddra.getLltCode() != null)) {
				node.setRowStyleClass("italic");
			}
		}
    
	public void setSMQCurrentNodeStyle(HierarchyNode childRelationNode, SmqRelation190 childRelation) {
		if (childRelation.getRelationImpactType() != null) {
			if("NCH".equals(childRelation.getRelationImpactType())) {
				childRelationNode.setRowStyleClass("italic");
			}
			if("DTR".equals(childRelation.getRelationImpactType())) {
				childRelationNode.setRowStyleClass("red-colored");
			}
			if ("SCH".equals(childRelation.getRelationImpactType()) || "ICC".equals(childRelation.getRelationImpactType()))
				childRelationNode.setRowStyleClass("blue-colored");	
			
			if ("SWC".equals(childRelation.getRelationImpactType()))
				childRelationNode.setRowStyleClass("pink-colored");
			
			if("PTS".equals(childRelation.getRelationImpactType())) {
				childRelationNode.setRowStyleClass("text-bold");
			}
		}
		else
			childRelationNode.setRowStyleClass("none");
	}

	public void setSMQTargetNodeStyle(HierarchyNode childRelationNode,	SmqRelationTarget childRelation) {
		if (childRelation.getRelationImpactType() != null) {
			if("LPP".equalsIgnoreCase(childRelation.getRelationImpactType())
					|| "PDL".equalsIgnoreCase(childRelation.getRelationImpactType())
					|| "NTR".equalsIgnoreCase(childRelation.getRelationImpactType())
					|| "PSA".equalsIgnoreCase(childRelation.getRelationImpactType())) {
				childRelationNode.setRowStyleClass("orange-colored");
			}

			if ("SCH".equals(childRelation.getRelationImpactType()))
				childRelationNode.setRowStyleClass("blue-colored");
			
			if ("SWC".equals(childRelation.getRelationImpactType()))
				childRelationNode.setRowStyleClass("pink-colored");
		}
		else
			childRelationNode.setRowStyleClass("none");
	}
	
	public void populateSmqRelations(Long smqCode, TreeNode expandedTreeNode, String smqType, String uiSourceOfEvent) {
		boolean isRootListNode = isRootListNode(expandedTreeNode);
		List<? extends IEntity> childRelations = null;
 		if("current".equalsIgnoreCase(smqType)) {
			childRelations = this.smqBaseCurrentService.findSmqRelationsForSmqCode(smqCode);
		} else {
			childRelations = this.smqBaseTargetService.findSmqRelationsForSmqCode(smqCode);
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
					childRelationNode.setCode(childRelation.getPtCode().toString());
					childRelationNode.setEntity(childRelation);
					
//					if (childRelation.getRelationImpactType() != null && childRelation.getRelationImpactType().equals("LPP"))
//						System.out.println("***************************** LPP : " + childRelation.getPtName());
					
					//Set Color
					setSMQCurrentNodeStyle(childRelationNode, childRelation);
				} else {
					//for target here
					SmqRelationTarget childRelation = (SmqRelationTarget) entity;
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
					if(!isRootListNode && "target-table".equalsIgnoreCase(uiSourceOfEvent)) {
						childRelationNode.markNotEditableInRelationstable();
					}
					//Set Color
					setSMQTargetNodeStyle(childRelationNode, childRelation);
					
				}
				new DefaultTreeNode(childRelationNode, expandedTreeNode);
			}
		}
	}
	
	
	public void populateSmqTreeNode(IEntity entity, TreeNode expandedTreeNode, String cmqType, Long parentCode, String uiSourceOfEvent) {
		boolean isRootListNode = isRootListNode(expandedTreeNode);
		IEntity entity2 = null;
		HierarchyNode node = null;
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
				node = this.createSmqBaseCurrrentNode((SmqBase190) entity2);
			}
			//Color for node
			setCMQCurrentNodeStyle(node, cmqRelation);
		} else {
			CmqRelationTarget cmqRelation = (CmqRelationTarget) entity;
			if((cmqRelation.getPtCode() != null) && (cmqRelation.getPtCode().longValue() > 0)) {
				entity2 = this.smqBaseTargetService.findSmqRelationBySmqAndPtCode(cmqRelation.getSmqCode(), cmqRelation.getPtCode().intValue());
				node = this.createSmqRelationTargetNode((SmqRelationTarget) entity2);
				isSmqRelation = true;
			} else {
				entity2 = this.smqBaseTargetService.findByCode(cmqRelation.getSmqCode());
				node = this.createSmqBaseTargetNode((SmqBaseTarget) entity2);
			}
			if(!isRootListNode && "target-table".equalsIgnoreCase(uiSourceOfEvent)) {
				node.markNotEditableInRelationstable();
			}
			//color for node
			setCMQTargetNodeStyle(node, cmqRelation); 
		}
		if(null != node) {	
			TreeNode treeNode = new DefaultTreeNode(node, expandedTreeNode);
			
			//if thsi is not an SQM relation node then its an SMQ node so check for rleations.
			if(!isSmqRelation) {
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
	}
	
	public void populateCmqRelationTreeNodes(List<MeddraDictHierarchySearchDto> dtos, TreeNode expandedTreeNode
			, String nodeType, String childNodeType, String cmqType, Long parentCode, Map<Long, IEntity> cmqRelationsMap, String uiSourceOfEvent) {
		boolean isRootListNode = isRootListNode(expandedTreeNode);
		for (MeddraDictHierarchySearchDto meddraDictHierarchySearchDto : dtos) {
			HierarchyNode node = this.createMeddraNode(meddraDictHierarchySearchDto, nodeType);
			//System.out.println("\n *************** node :: " + node.getTerm());
			
			//Meddra Color
			//Meddra Color
			if ("current".equalsIgnoreCase(cmqType))
				setCurrentMeddraColor(meddraDictHierarchySearchDto, node);
			else {
				if(!isRootListNode && "target-table".equalsIgnoreCase(uiSourceOfEvent)) {
					node.markNotEditableInRelationstable();
				}
				setTargetMeddraColor(meddraDictHierarchySearchDto, node);
			}
			//convert string to long and match in map
			IEntity entity = cmqRelationsMap.get(Long.valueOf(meddraDictHierarchySearchDto.getCode()));
			if(entity instanceof CmqRelationTarget) {
				CmqRelationTarget cmqRelationTarget = (CmqRelationTarget) entity;
				node.setRelationEntity(entity);
				//Color node
				setCMQTargetNodeStyle(node, cmqRelationTarget); 
			}
			if(entity instanceof CmqRelation190) {
				CmqRelation190 cmqRelation = (CmqRelation190) entity;
				node.setRelationEntity(entity);
				//Color node
				setCMQCurrentNodeStyle(node, cmqRelation);
			}	
			TreeNode treeNode = new DefaultTreeNode(node, expandedTreeNode);

			Long countOfChildren = null;
			if ("current".equalsIgnoreCase(cmqType))
				countOfChildren = this.meddraDictCurrentService.findChldrenCountByParentCode(childNodeType + "_"
													, nodeType + "_", Long.valueOf(meddraDictHierarchySearchDto.getCode()));
			else {
				countOfChildren = this.meddraDictTargetService.findChldrenCountByParentCode(childNodeType + "_"
						, nodeType + "_", Long.valueOf(meddraDictHierarchySearchDto.getCode()));
			}
			
			if((null != countOfChildren) && (countOfChildren > 0)) {
				// add a dummmy node to show expand arrow
				HierarchyNode dummyNode = new HierarchyNode(null, null, null, null);
				dummyNode.setDummyNode(true);
				new DefaultTreeNode(dummyNode, treeNode);
			}
		}
	}
	
    
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void populateCmqBaseChildren(Long cmqCode, TreeNode expandedTreeNode, String cmqType, String uiSourceOfEvent) {
		List<? extends IEntity> childCmqBaseList = null;
		boolean isRootListNode = isRootListNode(expandedTreeNode);
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
					if(!isRootListNode && "target-table".equalsIgnoreCase(uiSourceOfEvent)) {
						node.markNotEditableInRelationstable();
					}
					TreeNode cmqBaseChildNode = new DefaultTreeNode(node, expandedTreeNode);
					
					childTreeNodes.put(childCmqBase.getCmqCode(), cmqBaseChildNode);
					childCmqCodeList.add(childCmqBase.getCmqCode());
					
					//Color
					if ("ICC".equalsIgnoreCase(childCmqBase.getImpactType()) || "IMPACTED".equalsIgnoreCase(childCmqBase.getImpactType()))
						node.setRowStyleClass("blue-colored");
					//setCMQCurrentNodeStyle(node, childCmqBase);
				} else {
					CmqBaseTarget childCmqBase = (CmqBaseTarget) entity;
					node.setLevel(childCmqBase.getCmqTypeCd());
					node.setTerm(childCmqBase.getCmqName());
					node.setCode(childCmqBase.getCmqCode().toString());
					node.setEntity(childCmqBase);
					if(!isRootListNode && "target-table".equalsIgnoreCase(uiSourceOfEvent)) {
						node.markNotEditableInRelationstable();
					}
					TreeNode cmqBaseChildNode = new DefaultTreeNode(node, expandedTreeNode);
					
					childTreeNodes.put(childCmqBase.getCmqCode(), cmqBaseChildNode);
					childCmqCodeList.add(childCmqBase.getCmqCode());
					
					//Color
					if ("ICC".equalsIgnoreCase(childCmqBase.getImpactType()) || "IMPACTED".equalsIgnoreCase(childCmqBase.getImpactType()))
						node.setRowStyleClass("blue-colored");
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
	
	public void populateCmqRelations(Long cmqCode, TreeNode expandedTreeNode, String cmqType, String uiSourceOfEvent) {
		//add cmq relations now
		Map<Long, IEntity> socCodesMap = new HashMap<>();
		Map<Long, IEntity> hlgtCodesMap = new HashMap<>();
		Map<Long, IEntity> hltCodesMap = new HashMap<>();
		Map<Long, IEntity> ptCodesMap = new HashMap<>();
		Map<Long, IEntity> lltCodesMap = new HashMap<>();
		
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
						socCodesMap.put(cmqRelation.getSocCode(), cmqRelation);
					} else if((cmqRelation.getHlgtCode() != null) && (cmqRelation.getHlgtCode().longValue() > 0)) {
						hlgtCodesMap.put(cmqRelation.getHlgtCode(), cmqRelation);
					} else if((cmqRelation.getHltCode() != null) && (cmqRelation.getHltCode().longValue() > 0)) {
						hltCodesMap.put(cmqRelation.getHltCode(), cmqRelation);
					} else if((cmqRelation.getPtCode() != null) && (cmqRelation.getPtCode().longValue() > 0)
								&& (cmqRelation.getSmqCode() == null)) {
						ptCodesMap.put(cmqRelation.getPtCode(), cmqRelation);
					} else if((cmqRelation.getLltCode() != null) && (cmqRelation.getLltCode().longValue() > 0)) {
						lltCodesMap.put(cmqRelation.getLltCode().longValue(), cmqRelation);
					} else if((cmqRelation.getSmqCode() != null) && (cmqRelation.getSmqCode().longValue() > 0)) {
						this.populateSmqTreeNode(cmqRelation, expandedTreeNode, cmqType, cmqCode, uiSourceOfEvent);
					}
				} else {
					CmqRelationTarget cmqRelation = (CmqRelationTarget) entity;
					if((cmqRelation.getSocCode() != null) && (cmqRelation.getSocCode().longValue() > 0)) {
						socCodesMap.put(cmqRelation.getSocCode(), cmqRelation);
					} else if((cmqRelation.getHlgtCode() != null) && (cmqRelation.getHlgtCode().longValue() > 0)) {
						hlgtCodesMap.put(cmqRelation.getHlgtCode(), cmqRelation);
					} else if((cmqRelation.getHltCode() != null) && (cmqRelation.getHltCode().longValue() > 0)) {
						hltCodesMap.put(cmqRelation.getHltCode(), cmqRelation);
					} else if((cmqRelation.getPtCode() != null) && (cmqRelation.getPtCode().longValue() > 0)
								&& (cmqRelation.getSmqCode() == null)) {
						ptCodesMap.put(cmqRelation.getPtCode(), cmqRelation);
					} else if((cmqRelation.getLltCode() != null) && (cmqRelation.getLltCode().longValue() > 0)) {
						lltCodesMap.put(cmqRelation.getLltCode(), cmqRelation);
					} else if((cmqRelation.getSmqCode() != null) && (cmqRelation.getSmqCode().longValue() > 0)) {
						this.populateSmqTreeNode(cmqRelation, expandedTreeNode, cmqType, cmqCode, uiSourceOfEvent);
					}
				}
			}
			
			//find socs now
			if(socCodesMap.size() > 0) {
				List<MeddraDictHierarchySearchDto> socDtos = null;
				List<Long> socCodesList = new ArrayList<>(socCodesMap.keySet());
				if("current".equalsIgnoreCase(cmqType)) {
					socDtos = this.meddraDictCurrentService.findByCodes("SOC_", socCodesList);
				} else {
					socDtos = this.meddraDictTargetService.findByCodes("SOC_", socCodesList);
				}
				this.populateCmqRelationTreeNodes(socDtos, expandedTreeNode, "SOC", "HLGT", cmqType, cmqCode, socCodesMap, uiSourceOfEvent);
			}
			
			if(hlgtCodesMap.size() > 0) {
				List<MeddraDictHierarchySearchDto> hlgtDtos = null;
				List<Long> hlgtCodesList = new ArrayList<>(hlgtCodesMap.keySet());
				if("current".equalsIgnoreCase(cmqType)) {
					hlgtDtos = this.meddraDictCurrentService.findByCodes("HLGT_", hlgtCodesList);
				} else {
					hlgtDtos = this.meddraDictTargetService.findByCodes("HLGT_", hlgtCodesList);
				}
				this.populateCmqRelationTreeNodes(hlgtDtos, expandedTreeNode, "HLGT", "HLT", cmqType, cmqCode, hlgtCodesMap, uiSourceOfEvent);
			}
			
			if(hltCodesMap.size() > 0) {
				List<MeddraDictHierarchySearchDto> hltDtos = null;
				List<Long> hltCodesList = new ArrayList<>(hltCodesMap.keySet());
				if("current".equalsIgnoreCase(cmqType)) {
					hltDtos = this.meddraDictCurrentService.findByCodes("HLT_", hltCodesList);
				} else {
					hltDtos = this.meddraDictTargetService.findByCodes("HLT_", hltCodesList);
				}
				this.populateCmqRelationTreeNodes(hltDtos, expandedTreeNode, "HLT", "PT", cmqType, cmqCode, hltCodesMap, uiSourceOfEvent);
			}
			
			if(ptCodesMap.size() > 0) {
				List<MeddraDictHierarchySearchDto> ptDtos = null;
				List<Long> ptCodesList = new ArrayList<>(ptCodesMap.keySet());
				if("current".equalsIgnoreCase(cmqType)) {
					ptDtos = this.meddraDictCurrentService.findByCodes("PT_", ptCodesList);
				} else {
					ptDtos = this.meddraDictTargetService.findByCodes("PT_", ptCodesList);
				}
				this.populateCmqRelationTreeNodes(ptDtos, expandedTreeNode, "PT", "LLT", cmqType, cmqCode, ptCodesMap, uiSourceOfEvent);
			}
			
			if(lltCodesMap.size() > 0) {
				boolean isRootListNode = isRootListNode(expandedTreeNode);
				List<MeddraDictHierarchySearchDto> lltDtos = null;
				List<Long> lltCodesList = new ArrayList<>(lltCodesMap.keySet());
				if("current".equalsIgnoreCase(cmqType)) {
					lltDtos = this.meddraDictCurrentService.findByCodes("LLT_", lltCodesList);
				} else {
					lltDtos = this.meddraDictTargetService.findByCodes("LLT_", lltCodesList);
				}
				for (MeddraDictHierarchySearchDto meddraDictHierarchySearchDto : lltDtos) {
					HierarchyNode node = this.createMeddraNode(meddraDictHierarchySearchDto, "LLT");
					//Meddra - Current colors
					//Meddra Color
					if ("current".equalsIgnoreCase(cmqType))
						setCurrentMeddraColor(meddraDictHierarchySearchDto, node);
					else {
						if(!isRootListNode && "target-table".equalsIgnoreCase(uiSourceOfEvent)) {
							node.markNotEditableInRelationstable();
						}
						setTargetMeddraColor(meddraDictHierarchySearchDto, node);
					}
					IEntity entity = lltCodesMap.get(Long.parseLong(meddraDictHierarchySearchDto.getCode()));
					if(entity instanceof CmqRelationTarget) {
						CmqRelationTarget cmqRelationTarget = (CmqRelationTarget) entity;
						
						//Set Color
						setCMQTargetNodeStyle(node, cmqRelationTarget);
					}
					if(entity instanceof CmqRelation190) {
						CmqRelation190 cmqRelation = (CmqRelation190) entity;
						
						//Set Color
						setCMQCurrentNodeStyle(node, cmqRelation);
					}
					
					TreeNode treeNode = new DefaultTreeNode(node, expandedTreeNode);
				}
			}
		}
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void populateSmqBaseChildren(Long smqCode, TreeNode expandedTreeNode, String smqType, String uiSourceOfEvent) {
		boolean isRootListNode = isRootListNode(expandedTreeNode);
		List<? extends IEntity> childSmqBaseList = null;
		if("current".equalsIgnoreCase(smqType)) {
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
				if("current".equalsIgnoreCase(smqType)) {
					SmqBase190 childSmqBase = (SmqBase190) entity;
					childSmqCode = childSmqBase.getSmqCode();
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
					
					if ("ICS".equalsIgnoreCase(childSmqBase.getImpactType()) || "IMPACTED".equalsIgnoreCase(childSmqBase.getImpactType()))
						childNode.setRowStyleClass("blue-colored");
					//setSMQCurrentNodeStyle(childNode, childSmqBase);
				} else {
					//for target here
					SmqBaseTarget childSmqBase = (SmqBaseTarget) entity;
					childSmqCode = childSmqBase.getSmqCode();
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
					if(!isRootListNode && "target-table".equalsIgnoreCase(uiSourceOfEvent)) {
						childNode.markNotEditableInRelationstable();
					}
					if ("ICS".equalsIgnoreCase(childSmqBase.getImpactType())  || "IMPACTED".equalsIgnoreCase(childSmqBase.getImpactType()))
						childNode.setRowStyleClass("blue-colored");
				}
				// add child to parent
				TreeNode childTreeNode = new DefaultTreeNode(childNode, expandedTreeNode);
				smqTreeNodeMap.put(childSmqCode, childTreeNode);
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
					childSmqRelationsCountList = this.smqBaseTargetService.findSmqRelationsCountForSmqCodes(subList);
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

	public void populateMeddraDictReverseHierarchySearchDtoChildren(String searchColumnTypePrefix, String partitionColumn
																		, Long code, HierarchyNode hierarchyNode, TreeNode expandedTreeNode
																		, MeddraDictReverseHierarchySearchDto reverseSearchDto
																		, boolean chekcForPrimaryPath, String uiSourceOfEvent) {
		boolean isRootListNode = isRootListNode(expandedTreeNode);
		String partitionColumnPrefix = partitionColumn +"_";
		List<MeddraDictReverseHierarchySearchDto> childReverseSearchDtos = this.meddraDictTargetService.findReverseByCode(searchColumnTypePrefix
																															, partitionColumnPrefix, code);
		if(CollectionUtils.isNotEmpty(childReverseSearchDtos)) {
			for (MeddraDictReverseHierarchySearchDto childReverseSearchDto : childReverseSearchDtos) {
				HierarchyNode childNode = null;
				if(chekcForPrimaryPath) {
					boolean isPrimary = false;
					if("Y".equalsIgnoreCase(childReverseSearchDto.getPrimaryPathFlag())) {
						isPrimary = true;
					}
					childNode = this.createMeddraReverseNode(childReverseSearchDto, partitionColumn, isPrimary);
				} else {
					childNode = this.createMeddraReverseNode(childReverseSearchDto, partitionColumn, hierarchyNode.isPrimaryPathFlag());
				}
				
				if(!isRootListNode && "target-table".equalsIgnoreCase(uiSourceOfEvent)) {
					childNode.markNotEditableInRelationstable();
				}
				
				//Meddra Color
				//setMeddraColor(childReverseSearchDto, childNode); //TODO A REVOIR
				TreeNode childTreeNode = new DefaultTreeNode(childNode, expandedTreeNode);
				
				//dont add any child for last leaf node
				if(!"SOC".equalsIgnoreCase(partitionColumn)) {
					if(StringUtils.isNotBlank(reverseSearchDto.getHltTerm())) {
						// add a dummmy node to show expand arrow
						HierarchyNode dummyNode = new HierarchyNode(null, null, null, null);
						dummyNode.setDummyNode(true);
						new DefaultTreeNode(dummyNode, childTreeNode);
					}
				}
			}
		}
	}
	
	
	public void populateMeddraDictHierarchySearchDtoChildren(String parentLevel, Long dtoCode, TreeNode expandedTreeNode
																, String meddraType, String uiSourceOfEvent) {
		boolean isRootListNode = isRootListNode(expandedTreeNode);
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
			childDtos = this.meddraDictTargetService.findChildrenByParentCode(childSearchColumnTypePrefix, parentCodeColumnPrefix, dtoCode);
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
			
			if(!isRootListNode && "target-table".equalsIgnoreCase(uiSourceOfEvent)) {
				childNode.markNotEditableInRelationstable();
			}
			
			//Meddra Color
			if ("current".equalsIgnoreCase(meddraType))
				setCurrentMeddraColor(childDto, childNode);
			else
				setTargetMeddraColor(childDto, childNode);
			
			TreeNode childTreeNode = new DefaultTreeNode(childNode, expandedTreeNode);
			
			//fetch children count of this iterating child node by code of child
			//no need to do this is the childOfChild is LLT since LT is the leaf ode type
			if(!"LLT".equalsIgnoreCase(childLevel)) {
				Long countOfChildrenOfChild = null;
				if("current".equalsIgnoreCase(meddraType)) {
					countOfChildrenOfChild = this.meddraDictCurrentService.findChldrenCountByParentCode(childchildOfChildSearchColumnTypePrefix
																					, childSearchColumnTypePrefix, Long.valueOf(childDto.getCode()));
				} else {
					countOfChildrenOfChild = this.meddraDictTargetService.findChldrenCountByParentCode(childchildOfChildSearchColumnTypePrefix
																					, childSearchColumnTypePrefix, Long.valueOf(childDto.getCode()));
				}
				if((countOfChildrenOfChild != null) && (countOfChildrenOfChild > 0)) {
					// add a dummmy node to show expand arrow
					HierarchyNode dummyNode = new HierarchyNode(null, null, null, null);
					dummyNode.setDummyNode(true);
					new DefaultTreeNode(dummyNode, childTreeNode);
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
    
    
	public void updateCurrentTableForCmqList(TreeNode currentTableRootTreeNode,CmqBaseTarget selectedCmqList) {
        IARelationsTreeHelper treeHelper = new IARelationsTreeHelper(cmqBaseTargetService, smqBaseTargetService, meddraDictTargetService, cmqRelationTargetService);
        
		CmqBase190 cmqBaseCurrent = this.cmqBaseCurrentService.findByCode(selectedCmqList.getCmqCode());
		HierarchyNode node = treeHelper.createCmqBaseCurrentHierarchyNode(cmqBaseCurrent);
		TreeNode cmqBaseTreeNode = new DefaultTreeNode(node, currentTableRootTreeNode);
		if ("IMPACTED".equalsIgnoreCase(cmqBaseCurrent.getImpactType()) || "IMPACTED".equalsIgnoreCase(cmqBaseCurrent.getImpactType()))
			node.setRowStyleClass("blue-colored");
	
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
	
	public void updateTargetTableForCmqList(TreeNode targetTableRootTreeNode, CmqBaseTarget selectedCmqList) {
		HierarchyNode node = this.createCmqBaseTargetHierarchyNode(selectedCmqList);
		node.markNotEditableInRelationstable();
		TreeNode cmqBaseTreeNode = new DefaultTreeNode(node, targetTableRootTreeNode);

		if ("IMPACTED".equalsIgnoreCase(selectedCmqList.getImpactType()) || "ICC".equalsIgnoreCase(selectedCmqList.getImpactType()))
			node.setRowStyleClass("blue-colored");
		
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
    
    public void updateCurrentTableForSmqList(TreeNode currentTableRootTreeNode, SmqBaseTarget selectedSmqList) {
		SmqBase190 smqBaseCurrent = this.smqBaseCurrentService.findByCode(selectedSmqList.getSmqCode());
		if(null != smqBaseCurrent) {
			HierarchyNode node = this.createSmqBaseCurrrentNode(smqBaseCurrent);
			TreeNode cmqBaseTreeNode = new DefaultTreeNode(node, currentTableRootTreeNode);

			if (smqBaseCurrent.getImpactType().equals(CSMQBean.IMPACT_TYPE_IMPACTED))
				node.setRowStyleClass("blue-colored");
			
			boolean dummyNodeAdded = false;
			Long count = this.smqBaseCurrentService.findChildSmqCountByParentSmqCode(smqBaseCurrent.getSmqCode());
			if((count != null) && (count > 0)) {
				HierarchyNode dummyNode = new HierarchyNode(null, null, null, null);
				dummyNode.setDummyNode(true);
				new DefaultTreeNode(dummyNode, cmqBaseTreeNode);
				dummyNodeAdded = true;
			}
			
			//check for relations now
			if(!dummyNodeAdded) {
				count = this.smqBaseCurrentService.findSmqRelationsCountForSmqCode(smqBaseCurrent.getSmqCode());
				if((count != null) && (count > 0)) {
					HierarchyNode dummyNode = new HierarchyNode(null, null, null, null);
					dummyNode.setDummyNode(true);
					new DefaultTreeNode(dummyNode, cmqBaseTreeNode);
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
                cmqBaseTargetService, smqBaseTargetService, meddraDictTargetService, cmqRelationTargetService);
		HierarchyNode node = treeHelper.createSmqBaseTargetNode(selectedSmqList);
		node.markNotEditableInRelationstable();
		TreeNode cmqBaseTreeNode = new DefaultTreeNode(node, targetTableRootTreeNode);

		if ("IMPACTED".equalsIgnoreCase(selectedSmqList.getImpactType()))
			node.setRowStyleClass("blue-colored");
		
		boolean dummyNodeAdded = false;
		Long count = this.smqBaseTargetService.findChildSmqCountByParentSmqCode(selectedSmqList.getSmqCode());
		if((count != null) && (count > 0)) {
			HierarchyNode dummyNode = new HierarchyNode(null, null, null, null);
			dummyNode.setDummyNode(true);
			new DefaultTreeNode(dummyNode, cmqBaseTreeNode);
			dummyNodeAdded = true;
		}
		
		//check for relations now
		if(!dummyNodeAdded) {
			count = this.smqBaseTargetService.findSmqRelationsCountForSmqCode(selectedSmqList.getSmqCode());
			if((count != null) && (count > 0)) {
				HierarchyNode dummyNode = new HierarchyNode(null, null, null, null);
				dummyNode.setDummyNode(true);
				new DefaultTreeNode(dummyNode, cmqBaseTreeNode);
			}
		}
	}
}
