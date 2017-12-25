package com.dbms.controller;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.ViewScoped;
import javax.faces.event.AjaxBehaviorEvent;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.primefaces.component.wizard.Wizard;
import org.primefaces.context.RequestContext;
import org.primefaces.event.FlowEvent;
import org.primefaces.event.NodeCollapseEvent;
import org.primefaces.event.NodeExpandEvent;
import org.primefaces.model.DefaultTreeNode;
import org.primefaces.model.TreeNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dbms.csmq.CSMQBean;
import com.dbms.csmq.HierarchyNode;
import com.dbms.entity.IEntity;
import com.dbms.entity.cqt.CmqRelation190;
import com.dbms.entity.cqt.RefConfigCodeList;
import com.dbms.entity.cqt.SmqBase190;
import com.dbms.entity.cqt.SmqRelation190;
import com.dbms.entity.cqt.dtos.CmqBaseDTO;
import com.dbms.entity.cqt.dtos.HistoricalViewDTO;
import com.dbms.entity.cqt.dtos.HistoricalViewDbDataDTO;
import com.dbms.entity.cqt.dtos.MeddraDictHierarchySearchDto;
import com.dbms.service.AuthenticationService;
import com.dbms.service.IAuditTrailService;
import com.dbms.service.ICmqBase190Service;
import com.dbms.service.ICmqParentChild200Service;
import com.dbms.service.ICmqRelation190Service;
import com.dbms.service.IHistoricalViewService;
import com.dbms.service.IMeddraDictService;
import com.dbms.service.IRefCodeListService;
import com.dbms.service.ISmqBaseService;
import com.dbms.util.SMQLevelHelper;
import com.dbms.util.SWJSFRequest;
import com.dbms.view.ListRelationsVM;

@ManagedBean
@ViewScoped
public class HistoricalViewController implements Serializable {

	/**
	 * serialVersionUID.
	 */
	private static final long serialVersionUID = 2630718574599045932L;

	private static final Logger LOG = LoggerFactory.getLogger(HistoricalViewController.class);

	private String listName, listCode, dictionaryVersion, auditTimestamp;

	private Wizard historicalViewWizard;
	private String historicalViewWizardNextStep;

	private List<HistoricalViewDbDataDTO> searchResults;
	private List<HistoricalViewDTO> datas;
	private Long selectedCmqCode;
	private HistoricalViewDbDataDTO selectedHistoricalViewDbDataDTO;
	private HistoricalViewDTO selectedHistoricalViewDTO;
	private ListRelationsVM relationsModel;
	private TreeNode relationsRoot;
	private boolean displayScopeCatWeight;

	@ManagedProperty("#{HistoricalViewService}")
	private IHistoricalViewService historicalViewService;

	@ManagedProperty("#{AuthenticationService}")
	private AuthenticationService authService;

	@ManagedProperty("#{appSWJSFRequest}")
	private SWJSFRequest appSWJSFRequest;

	@ManagedProperty("#{RefCodeListService}")
	private IRefCodeListService refCodeListService;

	@ManagedProperty("#{CmqBase190Service}")
	private ICmqBase190Service cmqBaseService;

	@ManagedProperty("#{SmqBaseService}")
	private ISmqBaseService smqBaseService;

	@ManagedProperty("#{MeddraDictService}")
	private IMeddraDictService meddraDictService;

	@ManagedProperty("#{CmqRelation190Service}")
	private ICmqRelation190Service cmqRelationService;

	@ManagedProperty("#{CmqParentChild200Service}")
	private ICmqParentChild200Service cmqParentChildService;
	
	@ManagedProperty("#{AuditTrailService}")
	private IAuditTrailService auditTrailService;

	@PostConstruct
	public void init() {
		this.displayScopeCatWeight = refCodeListService.getLevelScopeCategorySystemConfig();
	}

	public void search() {
		this.searchResults = historicalViewService.findByCriterias(listName, listCode, dictionaryVersion,
				auditTimestamp);
		Map<Long, HistoricalViewDTO> historicalViewDTOMap = new HashMap<Long, HistoricalViewDTO>();
		List<HierarchyNode> addedHierarchyNodes = new ArrayList<>();
		TreeNode rootNode = new DefaultTreeNode("root",
				new HierarchyNode("LEVEL", "NAME", "CODE", "SCOPE", "CATEGORY", "WEIGHT", null), null);
		;
		for (HistoricalViewDbDataDTO historicalViewDbDataDTO : searchResults) {
			Long cmqCode = historicalViewDbDataDTO.getCmqCode();
			if (!historicalViewDTOMap.containsKey(cmqCode)) {
				HistoricalViewDTO historicalViewDTO = new HistoricalViewDTO();
				historicalViewDTO.setCmqCode(cmqCode);
				historicalViewDTO.setListName(historicalViewDbDataDTO.getListName());
				historicalViewDTO.setListType(historicalViewDbDataDTO.getListType());
				historicalViewDTO.setProduct(historicalViewDbDataDTO.getProduct());
				historicalViewDTO.setDrugProgram(historicalViewDbDataDTO.getDrugProgram());
				historicalViewDTO.setProtocolNumber(historicalViewDbDataDTO.getProtocolNumber());
				historicalViewDTO.setListLevel(historicalViewDbDataDTO.getListLevel());
				historicalViewDTO.setParentListName(historicalViewDbDataDTO.getParentListName());
				historicalViewDTO.setStatus(historicalViewDbDataDTO.getStatus());
				historicalViewDTO.setState(historicalViewDbDataDTO.getState());
				historicalViewDTO.setCreationDate(historicalViewDbDataDTO.getCreationDate());
				historicalViewDTO.setCreatedBy(historicalViewDbDataDTO.getCreatedBy());
				historicalViewDTO.setLastActivationDate(historicalViewDbDataDTO.getLastActivationDate());
				historicalViewDTO.setDescription(historicalViewDbDataDTO.getDescription());
				historicalViewDTO.setDictionaryVersion(historicalViewDbDataDTO.getDictionaryVersion());
				historicalViewDTO.setDesignee(historicalViewDbDataDTO.getDesignee());
				historicalViewDTO.setDesignee2(historicalViewDbDataDTO.getDesignee2());
				historicalViewDTO.setDesignee3(historicalViewDbDataDTO.getDesignee3());
				historicalViewDTO.setMedicalConcept(historicalViewDbDataDTO.getMedicalConcept());
				historicalViewDTOMap.put(cmqCode, historicalViewDTO);
			}

			HistoricalViewDTO historicalViewDTO = historicalViewDTOMap.get(cmqCode);

			if (null != historicalViewDTO) {
				// catch relations now.
				String termDictLevel = historicalViewDbDataDTO.getTermDictLevel();
				String term = historicalViewDbDataDTO.getTerm();
				String termScope = historicalViewDbDataDTO.getTermScope();
				Long termCode = historicalViewDbDataDTO.getTermCode();
				if ((null != termCode) && StringUtils.isNotBlank(term)) {
					HierarchyNode hierarchyNode = this.createRelationNode(termDictLevel, term, termCode, termScope);
					if (!addedHierarchyNodes.contains(hierarchyNode)) {
						new DefaultTreeNode(hierarchyNode, rootNode);
						addedHierarchyNodes.add(hierarchyNode);
					}
				} else {
					if (null == termCode) {
						LOG.warn("Got empty term code for term name {} for list code {} in historical view result.",
								term, cmqCode);
					} else {
						LOG.warn("Got empty term name for term code {} for list code {} in historical view result.",
								termCode, cmqCode);
					}
				}
			} else {
				LOG.warn("No HistoricalViewDTO found in map for cmqCode {} but shoudl have been there.", cmqCode);
			}
		}

		this.relationsRoot = rootNode;

		if (historicalViewDTOMap.size() > 0) {
			this.datas = new ArrayList<HistoricalViewDTO>(historicalViewDTOMap.values());
		} else {
			this.datas = new ArrayList<HistoricalViewDTO>();
		}
	}

	public void onNodeExpand(NodeExpandEvent event) {
		//// event source attriute from the ui
		TreeNode expandedTreeNode = event.getTreeNode();
		HierarchyNode hNode = (HierarchyNode) expandedTreeNode.getData();

		boolean isDataFetchCompleted = hNode.isDataFetchCompleted();

		if (isDataFetchCompleted) {
			return;
		}

		IEntity entity = hNode.getEntity();

		// remove the first dummy node placeholder
		HierarchyNode dummyChildData = (HierarchyNode) expandedTreeNode.getChildren().get(0).getData();

		if (dummyChildData.isDummyNode()) {
			expandedTreeNode.getChildren().remove(0);
		}

		if (entity instanceof SmqBase190) {
			String scopeNameFromParent = this.getScopeFromParentSmq(hNode, expandedTreeNode);

			// now find the children
			SmqBase190 smqBase = (SmqBase190) entity;
			this.populateSmqBaseChildren(smqBase.getSmqCode(), expandedTreeNode);
			this.populateSmqRelations(smqBase.getSmqCode(), expandedTreeNode,
					CSMQBean.getCqtBaseScopeByName(scopeNameFromParent));
		} else if (entity instanceof MeddraDictHierarchySearchDto) {
			String parentLevel = hNode.getLevel();
			MeddraDictHierarchySearchDto meddraDictHierarchySearchDto = (MeddraDictHierarchySearchDto) entity;
			Long dtoCode = null;
			String childLevel = null;
			String childSearchColumnTypePrefix = null;

			// child of the above child
			String childOfChildLevel = null;
			String childchildOfChildSearchColumnTypePrefix = null;

			String parentCodeColumnPrefix = parentLevel + "_";
			if ("SOC".equalsIgnoreCase(parentLevel)) {
				childLevel = "HLGT";
				childSearchColumnTypePrefix = childLevel + "_";
				childOfChildLevel = "HLT";
				childchildOfChildSearchColumnTypePrefix = childOfChildLevel + "_";
				dtoCode = Long.valueOf(meddraDictHierarchySearchDto.getSocCode());
			} else if ("HLGT".equalsIgnoreCase(parentLevel)) {
				childLevel = "HLT";
				childSearchColumnTypePrefix = childLevel + "_";
				childOfChildLevel = "PT";
				childchildOfChildSearchColumnTypePrefix = childOfChildLevel + "_";
				dtoCode = Long.valueOf(meddraDictHierarchySearchDto.getHlgtCode());
			} else if ("HLT".equalsIgnoreCase(parentLevel)) {
				childLevel = "PT";
				childSearchColumnTypePrefix = childLevel + "_";
				childOfChildLevel = "LLT";
				childchildOfChildSearchColumnTypePrefix = childOfChildLevel + "_";
				dtoCode = Long.valueOf(meddraDictHierarchySearchDto.getHltCode());
			} else if ("PT".equalsIgnoreCase(parentLevel)) {
				childLevel = "LLT";
				childSearchColumnTypePrefix = childLevel + "_";
				dtoCode = Long.valueOf(meddraDictHierarchySearchDto.getPtCode());
			}

			// fetch children of parent node by code of parent
			List<MeddraDictHierarchySearchDto> childDtos;
			childDtos = this.meddraDictService.findChildrenByParentCode(childSearchColumnTypePrefix,
					parentCodeColumnPrefix, dtoCode, dictionaryVersion);
			
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
				
				childNode.markNotEditableInRelationstable();
				
				TreeNode childTreeNode = new DefaultTreeNode(childNode, expandedTreeNode);
				
				//fetch children count of this iterating child node by code of child
				//no need to do this is the childOfChild is LLT since LT is the leaf ode type
				if(!"LLT".equalsIgnoreCase(childLevel)) {
	                nodesMap.put(Long.valueOf(childDto.getCode()), childTreeNode);
	                nodesMapKeys.add(Long.valueOf(childDto.getCode()));
	            }
			}
			
			List<Map<String, Object>> countsOfChildren;
	        countsOfChildren = this.meddraDictService.findChildrenCountByParentCodes(childchildOfChildSearchColumnTypePrefix,
	                childSearchColumnTypePrefix, nodesMapKeys, dictionaryVersion);
	        this.addCountDummyNodeToMeddranodes(countsOfChildren, nodesMap);
		}
		
		hNode.setDataFetchCompleted(true);
	}

	public void populateSmqBaseChildren(Long smqCode, TreeNode expandedTreeNode) {

		List<SmqBase190> childSmqBaseList = this.smqBaseService.findChildSmqByParentSmqCode(smqCode, dictionaryVersion);

		if (CollectionUtils.isNotEmpty(childSmqBaseList)) {
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
				childNode.markReadOnlyInRelationstable();

				// add child to parent
				TreeNode childTreeNode = new DefaultTreeNode(childNode, expandedTreeNode);
				smqChildCodeList.add(childSmqBase.getSmqCode());
				smqTreeNodeMap.put(childSmqCode, childTreeNode);
			} // end of for

			// find smqrelations of all child smqs
			List<Map<String, Object>> childSmqRelationsCountList = this.smqBaseService
					.findSmqRelationsCountForSmqCodes(smqChildCodeList);

			if ((null != childSmqRelationsCountList) && (childSmqRelationsCountList.size() > 0)) {
				for (Map<String, Object> map : childSmqRelationsCountList) {
					if (map.get("SMQ_CODE") != null) {
						Long childSmqCode = (Long) map.get("SMQ_CODE");
						Long count = (Long) map.get("COUNT");
						if (count > 0) {
							createNewDummyNode(smqTreeNodeMap.get(childSmqCode));
						}
					}
				}
			}
		}
	}

	public void populateSmqRelations(Long smqCode, TreeNode expandedTreeNode, String scopeFilter) {
		List<SmqRelation190> childRelations = null;
		if (StringUtils.isNotBlank(scopeFilter) && (scopeFilter.equals(CSMQBean.SCOPE_NARROW)
				|| scopeFilter.equals(CSMQBean.SCOPE_BROAD) || scopeFilter.equals(CSMQBean.SCOPE_FULL))) {
			childRelations = this.smqBaseService.findSmqRelationsForSmqCodeAndScope(smqCode, scopeFilter,
					dictionaryVersion);
		} else {
			childRelations = this.smqBaseService.findSmqRelationsForSmqCode(smqCode, dictionaryVersion);
		}

		if (null != childRelations) {
			List<Long> smqChildCodeList = new ArrayList<>();
			Map<Long, TreeNode> smqChildTreeNodeMap = new HashMap<>();
			for (SmqRelation190 childRelation : childRelations) {
				boolean isChildSmqNode = false;
				HierarchyNode childRelationNode = new HierarchyNode();
				if (childRelation.getSmqLevel() == 0) {
					SmqBase190 childSmq = new SmqBase190();
					childSmq.setSmqCode(childRelation.getPtCode().longValue());
					childSmq.setSmqName(childRelation.getPtName());
					childRelationNode.setLevel("Child SMQ");
					childRelationNode.setEntity(childSmq);
					childRelationNode.setScope(
							null != childRelation.getPtTermScope() ? childRelation.getPtTermScope().toString() : "");
					childRelationNode.setCategory(
							null != childRelation.getPtTermCategory() ? childRelation.getPtTermCategory() : "");
					childRelationNode.setWeight(
							null != childRelation.getPtTermWeight() ? childRelation.getPtTermWeight().toString() : "");
					isChildSmqNode = true;
					// for finding the child smqs of this one.
					smqChildCodeList.add(childRelation.getPtCode().longValue());
				} else if (childRelation.getSmqLevel() == 1) {
					childRelationNode.setLevel("SMQ1");
					childRelationNode.setEntity(childRelation);
					childRelationNode.setScope(
							null != childRelation.getPtTermScope() ? childRelation.getPtTermScope().toString() : "");
					childRelationNode.setCategory(
							null != childRelation.getPtTermCategory() ? childRelation.getPtTermCategory() : "");
					childRelationNode.setWeight(
							null != childRelation.getPtTermWeight() ? childRelation.getPtTermWeight().toString() : "");
				} else if (childRelation.getSmqLevel() == 2) {
					childRelationNode.setLevel("SMQ2");
					childRelationNode.setEntity(childRelation);
					childRelationNode.setScope(
							null != childRelation.getPtTermScope() ? childRelation.getPtTermScope().toString() : "");
					childRelationNode.setCategory(
							null != childRelation.getPtTermCategory() ? childRelation.getPtTermCategory() : "");
					childRelationNode.setWeight(
							null != childRelation.getPtTermWeight() ? childRelation.getPtTermWeight().toString() : "");
				} else if (childRelation.getSmqLevel() == 3) {
					childRelationNode.setLevel("SMQ3");
					childRelationNode.setEntity(childRelation);
					childRelationNode.setScope(
							null != childRelation.getPtTermScope() ? childRelation.getPtTermScope().toString() : "");
					childRelationNode.setCategory(
							null != childRelation.getPtTermCategory() ? childRelation.getPtTermCategory() : "");
					childRelationNode.setWeight(
							null != childRelation.getPtTermWeight() ? childRelation.getPtTermWeight().toString() : "");
				} else if (childRelation.getSmqLevel() == 4) {
					childRelationNode.setLevel("PT");
					childRelationNode.setScope(
							null != childRelation.getPtTermScope() ? childRelation.getPtTermScope().toString() : "");
					childRelationNode.setCategory(
							null != childRelation.getPtTermCategory() ? childRelation.getPtTermCategory() : "");
					childRelationNode.setWeight(
							null != childRelation.getPtTermWeight() ? childRelation.getPtTermWeight().toString() : "");
					childRelationNode.setEntity(childRelation);
				} else if (childRelation.getSmqLevel() == 5) {
					childRelationNode.setLevel("LLT");
					childRelationNode.setScope(
							null != childRelation.getPtTermScope() ? childRelation.getPtTermScope().toString() : "");
					childRelationNode.setCategory(
							null != childRelation.getPtTermCategory() ? childRelation.getPtTermCategory() : "");
					childRelationNode.setWeight(
							null != childRelation.getPtTermWeight() ? childRelation.getPtTermWeight().toString() : "");
					childRelationNode.setEntity(childRelation);
				}
				childRelationNode.setTerm(childRelation.getPtName());
				childRelationNode.setCode(childRelation.getPtCode().toString());
				childRelationNode.markReadOnlyInRelationstable();

				TreeNode treeNode = new DefaultTreeNode(childRelationNode, expandedTreeNode);
				if (isChildSmqNode) {
					this.createNewDummyNode(treeNode);
					// add the child smq to map to fit it in later.
					smqChildTreeNodeMap.put(childRelation.getPtCode().longValue(), treeNode);
				}
			}

			if (smqChildCodeList.size() > 0) {
				// find child smqs for this one and add a C in fornt of name if
				// it has
				List<Map<String, Object>> smqRelationsCountList = this.smqBaseService
						.findSmqChildRelationsCountForSmqCodes(smqChildCodeList, dictionaryVersion);
				if ((null != smqRelationsCountList) && (smqRelationsCountList.size() > 0)) {
					for (Map<String, Object> map : smqRelationsCountList) {
						if (map.get("SMQ_CODE") != null) {
							Long childSmqCode = (Long) map.get("SMQ_CODE");
							if ((Long) map.get("COUNT") > 0) {
								TreeNode treeNode = smqChildTreeNodeMap.get(childSmqCode);
								HierarchyNode hierarchyNode = (HierarchyNode) treeNode.getData();
								String level = hierarchyNode.getLevel();
								hierarchyNode.setLevel("'C' " + level);
							}
						}
					}
				}
			} // end of if(smqChildCodeList.size() > 0)

		}
	}

	private String getScopeFromParentSmq(HierarchyNode hNode, TreeNode expandedTreeNode) {
		// scan upto 6 tree levels to find if we have an smq with a scope
		// level 1 up
		String scopeNameFromParent = null;
		String parentLevel1Up = hNode.getScope();
		if (StringUtils.isNoneBlank(parentLevel1Up)
				&& (parentLevel1Up.equals("Broad") || parentLevel1Up.equals("Narrow")
						|| parentLevel1Up.equals("Child Narrow") || parentLevel1Up.equals("Full"))) {
			scopeNameFromParent = parentLevel1Up;
		} else {
			// level 2 up
			TreeNode parentLevel2Up = expandedTreeNode.getParent();
			HierarchyNode parentLevel2UpHnode = (HierarchyNode) parentLevel2Up.getData();
			IEntity parentLevel2UpEntity = parentLevel2UpHnode.getEntity();
			if (parentLevel2UpEntity instanceof SmqBase190) {
				String parentLevel2UpScope = parentLevel2UpHnode.getScope();
				if (StringUtils.isNoneBlank(parentLevel2UpScope)
						&& (parentLevel2UpScope.equals("1") || parentLevel2UpScope.equals("2")
								|| parentLevel2UpScope.equals("3") || parentLevel2UpScope.equals("4"))) {
					scopeNameFromParent = parentLevel2UpScope;
				} else {
					// level 3 up
					TreeNode parentLevel3Up = expandedTreeNode.getParent().getParent();
					if (null != parentLevel3Up) {
						HierarchyNode parentLevel3UpHnode = (HierarchyNode) parentLevel3Up.getData();
						IEntity parentLevel3UpEntity = parentLevel3UpHnode.getEntity();
						if (parentLevel3UpEntity instanceof SmqBase190) {
							String parentLevel3UpScope = parentLevel3UpHnode.getScope();
							if (StringUtils.isNoneBlank(parentLevel3UpScope)
									&& (parentLevel3UpScope.equals("1") || parentLevel3UpScope.equals("2")
											|| parentLevel3UpScope.equals("3") || parentLevel3UpScope.equals("4"))) {
								scopeNameFromParent = parentLevel3UpScope;
							} else {
								// level 4 up
								TreeNode parentLevel4Up = expandedTreeNode.getParent().getParent().getParent();
								if (null != parentLevel4Up) {
									HierarchyNode parentLevel4UpHnode = (HierarchyNode) parentLevel4Up.getData();
									IEntity parentLevel4UpEntity = parentLevel4UpHnode.getEntity();
									if (parentLevel4UpEntity instanceof SmqBase190) {
										String parentLevel4UpScope = parentLevel4UpHnode.getScope();
										if (StringUtils.isNoneBlank(parentLevel4UpScope)
												&& (parentLevel4UpScope.equals("1") || parentLevel4UpScope.equals("2")
														|| parentLevel4UpScope.equals("3")
														|| parentLevel4UpScope.equals("4"))) {
											scopeNameFromParent = parentLevel4UpScope;
										} else {
											// level 5 up
											TreeNode parentLevel5Up = expandedTreeNode.getParent().getParent()
													.getParent().getParent();
											if (null != parentLevel5Up) {
												HierarchyNode parentLevel5UpHnode = (HierarchyNode) parentLevel5Up
														.getData();
												IEntity parentLevel5UpEntity = parentLevel5UpHnode.getEntity();
												if (parentLevel5UpEntity instanceof SmqBase190) {
													String parentLevel5UpScope = parentLevel5UpHnode.getScope();
													if (StringUtils.isNoneBlank(parentLevel5UpScope)
															&& (parentLevel5UpScope.equals("1")
																	|| parentLevel5UpScope.equals("2")
																	|| parentLevel5UpScope.equals("3")
																	|| parentLevel5UpScope.equals("4"))) {
														scopeNameFromParent = parentLevel5UpScope;
													} else {
														// level 6 up
														TreeNode parentLevel6Up = expandedTreeNode.getParent()
																.getParent().getParent().getParent().getParent();
														if (null != parentLevel6Up) {
															HierarchyNode parentLevel6UpHnode = (HierarchyNode) parentLevel6Up
																	.getData();
															IEntity parentLevel6UpEntity = parentLevel6UpHnode
																	.getEntity();
															if (parentLevel6UpEntity instanceof SmqBase190) {
																String parentLevel6UpScope = parentLevel6UpHnode
																		.getScope();
																if (StringUtils.isNoneBlank(parentLevel6UpScope)
																		&& (parentLevel6UpScope.equals("1")
																				|| parentLevel6UpScope.equals("2")
																				|| parentLevel6UpScope.equals("3")
																				|| parentLevel6UpScope.equals("4"))) {
																	scopeNameFromParent = parentLevel6UpScope;
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

		return scopeNameFromParent;
	}

	public void onNodeCollapse(NodeCollapseEvent event) {
		TreeNode expandedTreeNode = event.getTreeNode();
		expandedTreeNode.setExpanded(false);
	}

	private HierarchyNode createMeddraNode(MeddraDictHierarchySearchDto searchDto, String level, IEntity relationEntity) {
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
	
	private HierarchyNode createRelationNode(String termDictLevel, String term, Long termCode, String termScope) {
		HierarchyNode node = new HierarchyNode();
		boolean isMeddra = false;
		boolean isSmq = false;
		int smqLevel = -1;
		MeddraDictHierarchySearchDto meddraDictHierarchySearchDto = new MeddraDictHierarchySearchDto();
		if (termDictLevel.equals("SMQ1")) {
			node.setLevel("SMQ1");
			isSmq = true;
			smqLevel = 1;
		} else if (termDictLevel.equals("SMQ2")) {
			node.setLevel("SMQ2");
			isSmq = true;
			smqLevel = 2;
		} else if (termDictLevel.equals("SMQ3")) {
			node.setLevel("SMQ3");
			isSmq = true;
			smqLevel = 3;
		} else if (termDictLevel.equals("SMQ4")) {
			node.setLevel("SMQ4");
			isSmq = true;
			smqLevel = 4;
		} else if (termDictLevel.equals("SMQ5")) {
			node.setLevel("SMQ5");
			isSmq = true;
			smqLevel = 5;
		} else if (termDictLevel.equals("LLT")) {
			node.setLevel("LLT");
			isMeddra = true;
			meddraDictHierarchySearchDto.setLltCode(termCode + "");
		} else if (termDictLevel.equals("PT")) {
			node.setLevel("PT");
			isMeddra = true;
			meddraDictHierarchySearchDto.setPtCode(termCode + "");
		} else if (termDictLevel.equals("HLT")) {
			node.setLevel("HLT");
			isMeddra = true;
			meddraDictHierarchySearchDto.setHltCode(termCode + "");
		} else if (termDictLevel.equals("HLGT")) {
			node.setLevel("HLGT");
			isMeddra = true;
			meddraDictHierarchySearchDto.setHlgtCode(termCode + "");
		} else if (termDictLevel.equals("SOC")) {
			node.setLevel("SOC");
			isMeddra = true;
			meddraDictHierarchySearchDto.setSocCode(termCode + "");
		}

		node.setTerm(term);
		node.setCode(termCode + "");
		if (StringUtils.isBlank(termScope)) {
			node.setScope("");
		} else {
			node.setScope(termScope);
		}

		if (isSmq) {
			SmqBase190 smqBase190 = new SmqBase190();
			smqBase190.setSmqCode(termCode);
			smqBase190.setSmqLevel(smqLevel);
			smqBase190.setSmqName(term);
			node.setEntity(smqBase190);

		} else if (isMeddra) {
			meddraDictHierarchySearchDto.setTerm(term);
			node.setEntity(meddraDictHierarchySearchDto);
		}

		return node;
	}

	public void pickSelected(Long cmqCode) {
		this.selectedCmqCode = cmqCode;
		for (HistoricalViewDTO historicalViewDTO : datas) {
			if (historicalViewDTO.getCmqCode().longValue() == cmqCode.longValue()) {
				this.selectedHistoricalViewDTO = historicalViewDTO;
				break;
			}
		}
		Map<Long, TreeNode> socCodesMap = new HashMap<>();
		Map<Long, TreeNode> hlgtCodesMap = new HashMap<>();
		Map<Long, TreeNode> hltCodesMap = new HashMap<>();
		Map<Long, TreeNode> ptCodesMap = new HashMap<>();
		Map<Long, TreeNode> lltCodesMap = new HashMap<>();
		this.selectedHistoricalViewDTO.setRelationsRootTreeNode(this.relationsRoot);
		List<TreeNode> childNodes = this.relationsRoot.getChildren();
		for (TreeNode childNode : childNodes) {
			HierarchyNode hierarchyNode = (HierarchyNode) childNode.getData();
			IEntity entity = hierarchyNode.getEntity();
			String level = hierarchyNode.getLevel();
			if (entity instanceof SmqBase190) {
				Long smqBaseChildrenCount = this.smqBaseService
						.findChildSmqCountByParentSmqCode(((SmqBase190) entity).getSmqCode(), dictionaryVersion);
				if ((null != smqBaseChildrenCount) && (smqBaseChildrenCount > 0)) {
					// add a dummmy node to show expand arrow
					createNewDummyNode(childNode);
				} else {
					Long childSmqrelationsCount;
					childSmqrelationsCount = this.smqBaseService
							.findSmqRelationsCountForSmqCode(((SmqBase190) entity).getSmqCode());
					if ((null != childSmqrelationsCount) && (childSmqrelationsCount > 0)) {
						// add a dummmy node to show expand arrow
						createNewDummyNode(childNode);
					}
				}
			} else if(entity instanceof MeddraDictHierarchySearchDto) {
				MeddraDictHierarchySearchDto meddraDictHierarchySearchDto = (MeddraDictHierarchySearchDto)entity;
				if(level.equals("LLT") && !StringUtils.isBlank(meddraDictHierarchySearchDto.getLltCode())) {
					Long code = Long.valueOf(meddraDictHierarchySearchDto.getLltCode());
					lltCodesMap.put(code, childNode);
				} else if(level.equals("PT") && !StringUtils.isBlank(meddraDictHierarchySearchDto.getPtCode())) {
					Long code = Long.valueOf(meddraDictHierarchySearchDto.getPtCode());
					ptCodesMap.put(code, childNode);
				} else if(level.equals("HLGT") && !StringUtils.isBlank(meddraDictHierarchySearchDto.getHlgtCode())) {
					Long code = Long.valueOf(meddraDictHierarchySearchDto.getHlgtCode());
					hlgtCodesMap.put(code, childNode);
				} else if(level.equals("HLT") && !StringUtils.isBlank(meddraDictHierarchySearchDto.getHltCode())) {
					Long code = Long.valueOf(meddraDictHierarchySearchDto.getHltCode());
					hltCodesMap.put(code, childNode);
				} else if(level.equals("SOC") && !StringUtils.isBlank(meddraDictHierarchySearchDto.getSocCode())) {
					Long code = Long.valueOf(meddraDictHierarchySearchDto.getSocCode());
					socCodesMap.put(code, childNode);
				}
			}
		}
		
		if(socCodesMap.size() > 0) {
            List<Long> socCodesList = new ArrayList<>(socCodesMap.keySet());
            List<Map<String, Object>> countsOfChildren = this.meddraDictService.findChildrenCountByParentCodes("HLGT_"
                    , "SOC_", socCodesList, dictionaryVersion);
            this.addCountDummyNodeToMeddranodes(countsOfChildren, socCodesMap);
        }
		
		if(hlgtCodesMap.size() > 0) {
            List<Long> hlgtCodesList = new ArrayList<>(hlgtCodesMap.keySet());
            List<Map<String, Object>> countsOfChildren = this.meddraDictService.findChildrenCountByParentCodes("HLT_"
                    , "HLGT_", hlgtCodesList, dictionaryVersion);
            this.addCountDummyNodeToMeddranodes(countsOfChildren, hlgtCodesMap);
        }

        if(hltCodesMap.size() > 0) {
            List<Long> hltCodesList = new ArrayList<>(hltCodesMap.keySet());
            List<Map<String, Object>> countsOfChildren = this.meddraDictService.findChildrenCountByParentCodes("PT_"
                    , "HLT_", hltCodesList, dictionaryVersion);
            this.addCountDummyNodeToMeddranodes(countsOfChildren, hltCodesMap);
        }

        if(ptCodesMap.size() > 0) {
            List<Long> ptCodesList = new ArrayList<>(ptCodesMap.keySet());
            List<Map<String, Object>> countsOfChildren = this.meddraDictService.findChildrenCountByParentCodes("LLT_"
                    , "PT_", ptCodesList, dictionaryVersion);
            this.addCountDummyNodeToMeddranodes(countsOfChildren, ptCodesMap);
        }
		
		RequestContext.getCurrentInstance().execute("PF('wizard').next()");
	}

	private void addCountDummyNodeToMeddranodes( List<Map<String, Object>> countsOfChildren
													, Map<Long, TreeNode> addedNodes) {
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
	
	public TreeNode createNewDummyNode(TreeNode parentNode) {
		HierarchyNode dummyNode = new HierarchyNode(null, null, null, null);
		dummyNode.setDummyNode(true);
		return new DefaultTreeNode(dummyNode, parentNode);
	}

	public void reset() {
		this.listName = null;
		this.listCode = null;
		this.dictionaryVersion = null;
		this.auditTimestamp = null;
	}

	/**
	 * FlowListener of Browse Wizard Component
	 * 
	 * @param event
	 * @return
	 */
	public String onWizardFlowProcess(FlowEvent event) {
		String historicalViewWizardNextStep = event.getOldStep();
		if (this.selectedCmqCode != null) {
			historicalViewWizardNextStep = event.getNewStep();
		}
		RequestContext.getCurrentInstance().update("HistoricalView:wizardNavbar");
		return historicalViewWizardNextStep;
	}

	public boolean isWizardNavbarShown() {
		return !"searchBrowse".equals(historicalViewWizard.getStep());
	}

	public boolean isWizardNavbarNextShown() {
		return isWizardNavbarShown() && "details".equals(historicalViewWizard.getStep());
	}

	public boolean isWizardNavbarBackShown() {
		return !"searchBrowse".equals(historicalViewWizard.getStep());
	}
	
	public List<String> findAuditTimestamps(int dictionaryVersion) {
		return this.auditTrailService.findAuditTimestamps(dictionaryVersion);
	}
	
	public List<CmqBaseDTO> selectList() {
		List<RefConfigCodeList> dict = new ArrayList<RefConfigCodeList>();
		dict.add(refCodeListService.getCurrentMeddraVersion());
		dict.add(refCodeListService.getTargetMeddraVersion());
		
 		return this.auditTrailService.findLists(dict);
	}

	public void resetCode(AjaxBehaviorEvent event) {
		this.listCode = null;
	}

	public void resetName(AjaxBehaviorEvent event) {
		this.listName = null;
	}

	public String getListName() {
		return listName;
	}

	public void setListName(String listName) {
		this.listName = listName;
	}

	public String getListCode() {
		return listCode;
	}

	public void setListCode(String listCode) {
		this.listCode = listCode;
	}

	public String getDictionaryVersion() {
		return dictionaryVersion;
	}

	public void setDictionaryVersion(String dictionaryVersion) {
		this.dictionaryVersion = dictionaryVersion;
	}

	public String getAuditTimestamp() {
		return auditTimestamp;
	}

	public void setAuditTimestamp(String auditTimestamp) {
		this.auditTimestamp = auditTimestamp;
	}

	public IHistoricalViewService getHistoricalViewService() {
		return historicalViewService;
	}

	public void setHistoricalViewService(IHistoricalViewService historicalViewService) {
		this.historicalViewService = historicalViewService;
	}

	public Wizard getHistoricalViewWizard() {
		return historicalViewWizard;
	}

	public void setHistoricalViewWizard(Wizard historicalViewWizard) {
		this.historicalViewWizard = historicalViewWizard;
	}

	public String getHistoricalViewWizardNextStep() {
		return historicalViewWizardNextStep;
	}

	public void setHistoricalViewWizardNextStep(String historicalViewWizardNextStep) {
		this.historicalViewWizardNextStep = historicalViewWizardNextStep;
	}

	public Long getSelectedCmqCode() {
		return selectedCmqCode;
	}

	public void setSelectedCmqCode(Long selectedCmqCode) {
		this.selectedCmqCode = selectedCmqCode;
	}

	public List<HistoricalViewDbDataDTO> getSearchResults() {
		return searchResults;
	}

	public void setSearchResults(List<HistoricalViewDbDataDTO> searchResults) {
		this.searchResults = searchResults;
	}

	public List<HistoricalViewDTO> getDatas() {
		return datas;
	}

	public void setDatas(List<HistoricalViewDTO> datas) {
		this.datas = datas;
	}

	public HistoricalViewDbDataDTO getSelectedHistoricalViewDbDataDTO() {
		return selectedHistoricalViewDbDataDTO;
	}

	public void setSelectedHistoricalViewDbDataDTO(HistoricalViewDbDataDTO selectedHistoricalViewDbDataDTO) {
		this.selectedHistoricalViewDbDataDTO = selectedHistoricalViewDbDataDTO;
	}

	public HistoricalViewDTO getSelectedHistoricalViewDTO() {
		return selectedHistoricalViewDTO;
	}

	public void setSelectedHistoricalViewDTO(HistoricalViewDTO selectedHistoricalViewDTO) {
		this.selectedHistoricalViewDTO = selectedHistoricalViewDTO;
	}

	public ListRelationsVM getRelationsModel() {
		return relationsModel;
	}

	public void setRelationsModel(ListRelationsVM relationsModel) {
		this.relationsModel = relationsModel;
	}

	public AuthenticationService getAuthService() {
		return authService;
	}

	public void setAuthService(AuthenticationService authService) {
		this.authService = authService;
	}

	public SWJSFRequest getAppSWJSFRequest() {
		return appSWJSFRequest;
	}

	public void setAppSWJSFRequest(SWJSFRequest appSWJSFRequest) {
		this.appSWJSFRequest = appSWJSFRequest;
	}

	public IRefCodeListService getRefCodeListService() {
		return refCodeListService;
	}

	public void setRefCodeListService(IRefCodeListService refCodeListService) {
		this.refCodeListService = refCodeListService;
	}

	public ICmqBase190Service getCmqBaseService() {
		return cmqBaseService;
	}

	public void setCmqBaseService(ICmqBase190Service cmqBaseService) {
		this.cmqBaseService = cmqBaseService;
	}

	public ISmqBaseService getSmqBaseService() {
		return smqBaseService;
	}

	public void setSmqBaseService(ISmqBaseService smqBaseService) {
		this.smqBaseService = smqBaseService;
	}

	public IMeddraDictService getMeddraDictService() {
		return meddraDictService;
	}

	public void setMeddraDictService(IMeddraDictService meddraDictService) {
		this.meddraDictService = meddraDictService;
	}

	public ICmqRelation190Service getCmqRelationService() {
		return cmqRelationService;
	}

	public void setCmqRelationService(ICmqRelation190Service cmqRelationService) {
		this.cmqRelationService = cmqRelationService;
	}

	public ICmqParentChild200Service getCmqParentChildService() {
		return cmqParentChildService;
	}

	public void setCmqParentChildService(ICmqParentChild200Service cmqParentChildService) {
		this.cmqParentChildService = cmqParentChildService;
	}

	public TreeNode getRelationsRoot() {
		return relationsRoot;
	}

	public void setRelationsRoot(TreeNode relationsRoot) {
		this.relationsRoot = relationsRoot;
	}

	public boolean isDisplayScopeCatWeight() {
		return displayScopeCatWeight;
	}

	public void setDisplayScopeCatWeight(boolean displayScopeCatWeight) {
		this.displayScopeCatWeight = displayScopeCatWeight;
	}

	public IAuditTrailService getAuditTrailService() {
		return auditTrailService;
	}

	public void setAuditTrailService(IAuditTrailService auditTrailService) {
		this.auditTrailService = auditTrailService;
	}

}
