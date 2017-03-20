package com.dbms.controller;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
import javax.faces.event.AjaxBehaviorEvent;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.ListUtils;
import org.apache.commons.lang3.StringUtils;
import org.primefaces.component.wizard.Wizard;
import org.primefaces.event.CloseEvent;
import org.primefaces.event.NodeExpandEvent;
import org.primefaces.event.RowEditEvent;
import org.primefaces.model.DefaultTreeNode;
import org.primefaces.model.TreeNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dbms.controller.beans.HierarchySearchResultBean;
import com.dbms.csmq.HierarchyNode;
import com.dbms.entity.IEntity;
import com.dbms.entity.cqt.CmqBase190;
import com.dbms.entity.cqt.CmqRelation190;
import com.dbms.entity.cqt.CreateEntity;
import com.dbms.entity.cqt.RefConfigCodeList;
import com.dbms.entity.cqt.SmqBase190;
import com.dbms.entity.cqt.SmqRelation190;
import com.dbms.entity.cqt.dtos.MeddraDictHierarchySearchDto;
import com.dbms.entity.cqt.dtos.MeddraDictReverseHierarchySearchDto;
import com.dbms.service.ICmqBase190Service;
import com.dbms.service.ICmqRelation190Service;
import com.dbms.service.IMeddraDictService;
import com.dbms.service.IRefCodeListService;
import com.dbms.service.ISmqBaseService;
import com.dbms.util.exceptions.CqtServiceException;
import com.dbms.web.dto.CodelistDTO;

/**
 * @date Feb 7, 2017 7:39:34 AM
 **/
@ManagedBean
@ViewScoped
public class SearchController extends BaseController<CmqBase190> {

	private static final long serialVersionUID = 5299394344651669792L;

	private static final Logger log = LoggerFactory
			.getLogger(SearchController.class);

	@ManagedProperty("#{CmqBase190Service}")
	private ICmqBase190Service cmqBaseService;

	@ManagedProperty("#{SmqBaseService}")
	private ISmqBaseService smqBaseService;

	@ManagedProperty("#{MeddraDictService}")
	private IMeddraDictService meddraDictService;

	@ManagedProperty("#{CmqRelation190Service}")
	private ICmqRelation190Service cmqRelationService;

	@ManagedProperty("#{RefCodeListService}")
	private IRefCodeListService refCodeListService;

	private String releaseStatus;
	private String criticalEvent;
	private String termName;
	private String termNameOfHierarchySearch;

	private Long code;

	private String extension;
	private String drugProgram;
	private String protocol;
	private String state;
	private Integer level;
	private String status;
	private String critical;
	private String scope;
	private String product;
	private String group;
	private String history;
	private String codelist;
	private String levelH;

	private boolean maintainDesigBtn;
	private boolean dataModified = false;

	private List<CreateEntity> values, vals;

	private List<CodelistDTO> admins;

	private Wizard updateWizard, copyWizard, browseWizard;

	private String[] selectedSOCs;
	private TreeNode hierarchyRoot;
	private TreeNode hierarchyRootCopy;
	private CmqBase190 selctedData;

	private List<HierarchySearchResultBean> hierarchySearchResults;

	private TreeNode[] relationSelected;
	private TreeNode[] relationSelectedInRelationsTable;
	private TreeNode relationsRoot;

	private Long clickedCmqCode;

	public SearchController() {
		this.selectedData = new CmqBase190();
	}

	@PostConstruct
	public void init() {
		this.maintainDesigBtn = false;
		resetSearch();

		hierarchyRoot = new DefaultTreeNode("root", new HierarchyNode("LEVEL",
				"NAME", "CODE", null), null);
		relationsRoot = new DefaultTreeNode("root", new HierarchyNode("LEVEL",
				"NAME", "CODE", "SCOPE", "CATEGORY", "WEIGHT", null), null);
	}

	/**
	 * Used by wizard component.
	 * 
	 * @param event
	 *            FlowEvent
	 * @return String
	 */
	// public String onFlowProcess(FlowEvent event) {
	//
	//
	// }

	public void initSearch() {
		this.datas = new ArrayList<CmqBase190>();
		this.dataModified = false;
		if (selctedData == null) {
			selctedData = new CmqBase190();
			RefConfigCodeList currentMeddraVersionCodeList = this.refCodeListService
					.getCurrentMeddraVersion();
			if (currentMeddraVersionCodeList != null) {
				selctedData.setDictionaryVersion(currentMeddraVersionCodeList
						.getValue());
				// selectedData.setDictionaryName(dictionaryName);
			}
		}
	}

	public String reset() {
		this.datas = new ArrayList<CmqBase190>();

		resetSearch();
		changeLevel();
		return "";
	}

	public TreeNode getHierarchyRoot() {
		return hierarchyRoot;
	}

	public void changeLevel(AjaxBehaviorEvent event) {
		changeLevel();
	}

	public void changeLevel() {
		if (this.extension.equals("PRO")) {
			setLevel(2);
		} else {
			setLevel(1);
		}			
	}
	
	/**
	 * Reset search on every extension change.
	 * 
	 * @param event
	 */
	public void resetSearch(AjaxBehaviorEvent event) {
		resetSearch();
	}

	private void resetSearch() {
		this.extension = "";
		this.state = "PUBLISHED";
		this.status = "A";
		this.level = 1;
		// this.critical = "No";
		this.group = "No Group";

		this.product = "";
		this.protocol = "";
		this.drugProgram = "";
		this.termName = "";
		this.code = null;
		this.dataModified = false;
		
		resetHierarchySearch();
	}

	/**
	 * Method to change State value on status selection.
	 * 
	 * @param event
	 *            AjaxBehaviour
	 */
	public void changeState(AjaxBehaviorEvent event) {		
		if ("P".equalsIgnoreCase(status))
			setState("DRAFT");
		if ("A".equalsIgnoreCase(status))
			setState("PUBLISHED");
		else if("".equals(status))
			setState("");
	}

	public void changeTabUpdate() {
		updateWizard.setStep("details");
	}

	public void changeTabCopy() {
		copyWizard.setStep("details");
	}

	public void changeTabBrowse() {
		browseWizard.setStep("details");
	}

	public String getExtension() {
		return extension;
	}

	public void setExtension(String extension) {
		this.extension = extension;
	}

	public String getDrugProgram() {
		return drugProgram;
	}

	public void setDrugProgram(String drugProgram) {
		this.drugProgram = drugProgram;
	}

	public String getProtocol() {
		return protocol;
	}

	public void setProtocol(String protocol) {
		this.protocol = protocol;
	}

	public String getReleaseStatus() {
		return releaseStatus;
	}

	public void setReleaseStatus(String releaseStatus) {
		this.releaseStatus = releaseStatus;
	}

	public Integer getLevel() {
		return level;
	}

	public void setLevel(Integer level) {
		this.level = level;
	}

	public String getCriticalEvent() {
		return criticalEvent;
	}

	public void setCriticalEvent(String criticalEvent) {
		this.criticalEvent = criticalEvent;
	}

	public String getProduct() {
		return product;
	}

	public void setProduct(String product) {
		this.product = product;
	}

	public List<CmqBase190> getDatas() {
		if(this.dataModified)
			search();
		return datas;
	}

	public void setDatas(List<CmqBase190> datas) {
		this.datas = datas;
	}

	public void onRowCancel(RowEditEvent event) {
		FacesMessage msg = new FacesMessage("Canceled", "ZZ");
		FacesContext.getCurrentInstance().addMessage(null, msg);
	}

	public List<CreateEntity> getValues() {
		return values;
	}

	public void setValues(List<CreateEntity> values) {
		this.values = values;
	}

	/*
	 * public void setCreateEntityService(CreateEntityService
	 * createEntityService) { this.createEntityService = createEntityService; }
	 */

	public boolean isMaintainDesigBtn() {
		return maintainDesigBtn;
	}

	public void setMaintainDesigBtn(boolean maintainDesigBtn) {
		this.maintainDesigBtn = maintainDesigBtn;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getCritical() {
		return critical;
	}

	public void setCritical(String critical) {
		this.critical = critical;
	}

	public String getScope() {
		return scope;
	}

	public void setScope(String scope) {
		this.scope = scope;
	}

	public String getGroup() {
		return group;
	}

	public void setGroup(String group) {
		this.group = group;
	}

	public String getTermName() {
		return termName;
	}

	public void setTermName(String termName) {
		this.termName = termName;
	}
	
	public String getTermNameOfHierarchySearch() {
		return termNameOfHierarchySearch;
	}

	public void setTermNameOfHierarchySearch(String termNameOfHierarchySearch) {
		this.termNameOfHierarchySearch = termNameOfHierarchySearch;
	}

	public Long getCode() {
		return code;
	}

	public void setCode(Long code) {
		this.code = code;
	}

	public String getState() {
		return state;
	}

	public void setState(String state) {
		this.state = state;
	}

	public String getHistory() {
		return history;
	}

	public void setHistory(String history) {
		this.history = history;
	}

	public Wizard getUpdateWizard() {
		return updateWizard;
	}

	public void setUpdateWizard(Wizard updateWizard) {
		this.updateWizard = updateWizard;
	}

	public Wizard getCopyWizard() {
		return copyWizard;
	}

	public void setCopyWizard(Wizard copyWizard) {
		this.copyWizard = copyWizard;
	}

	public Wizard getBrowseWizard() {
		return browseWizard;
	}

	public void setBrowseWizard(Wizard browseWizard) {
		this.browseWizard = browseWizard;
	}

	public List<CodelistDTO> getAdmins() {
		return admins;
	}

	public void setAdmins(List<CodelistDTO> admins) {
		this.admins = admins;
	}

	public List<CreateEntity> getVals() {
		return vals;
	}

	public void setVals(List<CreateEntity> vals) {
		this.vals = vals;
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

	public String search() {
		log.debug("search by{}", extension);

		dataModified = false;
		// Item label is 'All' but value is empty string
		if (StringUtils.isBlank(status)) {
			status = null;
		}
		// Item label is 'All' but value is empty string
		if (StringUtils.isBlank(critical)) {
			critical = null;
		}
		// Item label is 'All' but value is empty string
		if ((null != level) && (level.intValue() <= 0)) {
			level = null;
		}
		// Item label is 'All' but value is empty string
		if ("".equalsIgnoreCase(group)) {
			group = null;
		}

		if (code != null && code == 0) {
			code = null;
		}

		datas = cmqBaseService.findByCriterias(extension, drugProgram,
				protocol, product, level, status, state, critical, group,
				termName, code);
		log.debug("found values {}", datas == null ? 0 : datas.size());

		// Relations retrieval
		//buildRelationsRoot();
		return "";
	}

	/**
	 * Maintain Designees on search.
	 */
	public void maintainDesignees() {

	}

	public String loadCmqBaseByCode() {
		this.selctedData = this.cmqBaseService.findByCode(code);

		return "";
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public String hierarchySearch() {
		int level = 0;
		String meddraSearchTermPrefix = null;
		boolean searchSmqBase = false;
		boolean searchMeddraBase = false;
		boolean searchMeddraBaseReverse = false;
		boolean searchCmqBase = false;
		if ("SMQ1".equalsIgnoreCase(levelH)) {
			level = 1;
			searchSmqBase = true;
		} else if ("SMQ2".equalsIgnoreCase(levelH)) {
			level = 2;
			searchSmqBase = true;
		} else if ("SMQ3".equalsIgnoreCase(levelH)) {
			level = 3;
			searchSmqBase = true;
		} else if ("SMQ4".equalsIgnoreCase(levelH)) {
			level = 4;
			searchSmqBase = true;
		} else if ("SMQ5".equalsIgnoreCase(levelH)) {
			level = 5;
			searchSmqBase = true;
		} else if ("SOC".equalsIgnoreCase(levelH)) {
			meddraSearchTermPrefix = "SOC_";
			searchMeddraBase = true;
		} else if ("HLGT".equalsIgnoreCase(levelH)) {
			meddraSearchTermPrefix = "HLGT_";
			searchMeddraBase = true;
		} else if ("HLT".equalsIgnoreCase(levelH)) {
			meddraSearchTermPrefix = "HLT_";
			searchMeddraBase = true;
		} else if ("PT".equalsIgnoreCase(levelH)) {
			meddraSearchTermPrefix = "PT_";
			searchMeddraBaseReverse = true;
		} else if ("LLT".equalsIgnoreCase(levelH)) {
			meddraSearchTermPrefix = "LLT_";
			searchMeddraBaseReverse = true;
		} else if ("PRO".equalsIgnoreCase(levelH)) {
			searchCmqBase = true;
		}

		if (searchSmqBase) {
			List<SmqBase190> smqBaseList = smqBaseService.findByLevelAndTerm(
					level, termNameOfHierarchySearch);
			log.info("smqBaseList values {}", smqBaseList == null ? 0
					: smqBaseList.size());

			this.hierarchyRoot = new DefaultTreeNode("root", new HierarchyNode(
					"LEVEL", "NAME", "CODE", null), null);

			for (SmqBase190 smqBase : smqBaseList) {
				HierarchyNode node = this.createSmqBaseNode(smqBase);
				TreeNode parentTreeNode = new DefaultTreeNode(node,
						this.hierarchyRoot);
				//add a dummy node for either of the cases, expansion will handle the actuals later
				Long smqBaseChildrenCount = this.smqBaseService.findChildSmqCountByParentSmqCode(smqBase.getSmqCode());
				if((null != smqBaseChildrenCount) && (smqBaseChildrenCount > 0)) {
					// add a dummmy node to show expand arrow
					HierarchyNode dummyNode = new HierarchyNode(null, null,
							null, null);
					dummyNode.setDummyNode(true);
					new DefaultTreeNode(dummyNode, parentTreeNode);
				} else {
					Long childSmqrelationsCount = this.smqBaseService
							.findSmqRelationsCountForSmqCode(smqBase
									.getSmqCode());
					if((null != childSmqrelationsCount) && (childSmqrelationsCount > 0)) {
						// add a dummmy node to show expand arrow
						HierarchyNode dummyNode = new HierarchyNode(null, null,
								null, null);
						dummyNode.setDummyNode(true);
						new DefaultTreeNode(dummyNode, parentTreeNode);
					}
				}
			} // end of for (SmqBase190 smqBase : smqBaseList)
		} else if (searchMeddraBase) {
			List<MeddraDictHierarchySearchDto> meddraDictDtoList = meddraDictService
					.findByLevelAndTerm(meddraSearchTermPrefix.toUpperCase(),
							termNameOfHierarchySearch);
			this.hierarchyRoot = new DefaultTreeNode("root", new HierarchyNode(
					"LEVEL", "NAME", "CODE", null), null);

			String childSearchColumnTypePrefix = null;
			String parentCodeColumnPrefix = levelH + "_";
			if ("SOC".equalsIgnoreCase(levelH)) {
				childSearchColumnTypePrefix = "HLGT_";
			} else if ("HLGT".equalsIgnoreCase(levelH)) {
				childSearchColumnTypePrefix = "HLT_";
			} else if ("HLT".equalsIgnoreCase(levelH)) {
				childSearchColumnTypePrefix = "PT_";
			} else if ("PT".equalsIgnoreCase(levelH)) {
				childSearchColumnTypePrefix = "LLT_";
			}
			
			for (MeddraDictHierarchySearchDto meddraDictDto : meddraDictDtoList) {
				HierarchyNode node = this.createMeddraNode(meddraDictDto, levelH);
				TreeNode parentTreeNode = new DefaultTreeNode(node, this.hierarchyRoot);
				
				Long countOfChildren = this.meddraDictService.findChldrenCountByParentCode(childSearchColumnTypePrefix,
						parentCodeColumnPrefix, Long.valueOf(meddraDictDto.getCode()));
				if((null != countOfChildren) && (countOfChildren > 0)) {
					// add a dummmy node to show expand arrow
					HierarchyNode dummyNode = new HierarchyNode(null, null,
							null, null);
					dummyNode.setDummyNode(true);
					new DefaultTreeNode(dummyNode, parentTreeNode);
				}
			}
		} else if (searchMeddraBaseReverse) {
			List<MeddraDictReverseHierarchySearchDto> meddraDictDtoList = meddraDictService
					.findFullReverseHierarchyByLevelAndTerm(levelH, levelH, termNameOfHierarchySearch);
			this.hierarchyRoot = new DefaultTreeNode("root", new HierarchyNode("LEVEL", "NAME", "CODE", null), null);
			
			for (MeddraDictReverseHierarchySearchDto meddraDictReverseDto : meddraDictDtoList) {
				HierarchyNode node = this.createMeddraReverseNode(meddraDictReverseDto, levelH, true);
				TreeNode parentTreeNode = new DefaultTreeNode(node, this.hierarchyRoot);
				
				// add a dummmy node to show expand arrow
				HierarchyNode dummyNode = new HierarchyNode(null, null,
						null, null);
				dummyNode.setDummyNode(true);
				new DefaultTreeNode(dummyNode, parentTreeNode);
			}
		} else if (searchCmqBase) {
			List<CmqBase190> cmqBaseList = cmqBaseService.findByLevelAndTerm(2,
					termNameOfHierarchySearch);
			this.hierarchyRoot = new DefaultTreeNode("root", new HierarchyNode(
					"LEVEL", "NAME", "CODE", null), null);
			
			List<Long> parentCmqCodeList = new ArrayList<>();
			Map<Long, TreeNode> parentTreeNodes = new HashMap<Long, TreeNode>();
			
			for (CmqBase190 cmqBase190 : cmqBaseList) {
				HierarchyNode node = new HierarchyNode();
				node.setLevel(levelH);
				node.setTerm(cmqBase190.getCmqName());
				node.setCode(cmqBase190.getCmqCode().toString());
				node.setEntity(cmqBase190);

				TreeNode cmqBaseTreeNode = new DefaultTreeNode(node,
						hierarchyRoot);
				parentTreeNodes.put(cmqBase190.getCmqCode(), cmqBaseTreeNode);
				parentCmqCodeList.add(cmqBase190.getCmqCode());
			}
			
			if(CollectionUtils.isNotEmpty(parentCmqCodeList)) {
				List<Map<String, Object>> childCountsList = this.cmqBaseService.findCmqChildCountForParentCmqCode(parentCmqCodeList);
				if((null != childCountsList) && (childCountsList.size() > 0)) {
					//first find and fix child nodes stuff
					for (Iterator<Long> it = parentCmqCodeList.iterator(); it.hasNext();) {
						ListIterator li = childCountsList.listIterator();
						Long parentCmqCode = it.next();
						while(li.hasNext()) {
							Map<String, Object> map = (Map<String, Object>) li.next();
							if(map.get("CMQ_CODE") != null) {
								Long cmqCode = (Long)map.get("CMQ_CODE");
								if(cmqCode.longValue() == parentCmqCode.longValue()) {
									Long count = (Long)map.get("COUNT");
									if(count > 0) {
										it.remove();//remove it from parentCmqCodeList
										
										//add a dummy node for this child in parent
										TreeNode parentTreeNode = parentTreeNodes.get(parentCmqCode);
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
				List<Map<String, Object>> relationsCountsList = this.cmqRelationService.findCountByCmqCodes(parentCmqCodeList);	
				if((null != relationsCountsList) && (relationsCountsList.size() > 0)) {
					ListIterator li = relationsCountsList.listIterator();
					while(li.hasNext()) {
						Map<String, Object> map = (Map<String, Object>) li.next();
						if(map.get("CMQ_CODE") != null) {
							Long cmqCode = (Long)map.get("CMQ_CODE");
							Long count = (Long)map.get("COUNT");
							if(count > 0) {
								//add a dummy node for this child in parent
								TreeNode parentTreeNode = parentTreeNodes.get(cmqCode);
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

		return "";
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	//uiEventSourceName is either relations or hierarchy
	public void onNodeExpand(NodeExpandEvent event) {
		//event source attriute from the ui
		String uiSourceOfEvent =  (String) event.getComponent().getAttributes().get("uiEventSourceName");
		TreeNode expandedTreeNode = event.getTreeNode();
		HierarchyNode hierarchyNode = (HierarchyNode) expandedTreeNode
				.getData();
		boolean isDataFetchCompleted = hierarchyNode.isDataFetchCompleted();
		if (!isDataFetchCompleted) {
			IEntity entity = hierarchyNode.getEntity();

			// remove the first dummy node placeholder
			HierarchyNode dummyChildData = (HierarchyNode) expandedTreeNode
					.getChildren().get(0).getData();
			if (dummyChildData.isDummyNode()) {
				expandedTreeNode.getChildren().remove(0);
			}

			if (entity instanceof SmqBase190) {
				SmqBase190 smqBase = (SmqBase190) entity;
				
				//first handle all child nodes of this node
				List<SmqBase190> childSmqBaseList = this.smqBaseService.findChildSmqByParentSmqCode(smqBase.getSmqCode());
				if(CollectionUtils.isNotEmpty(childSmqBaseList)) {
					Map<Long, TreeNode> smqTreeNodeMap = new HashMap<>();
					List<Long> smqChildCodeList = new ArrayList<>();
					for (SmqBase190 childSmqBase : childSmqBaseList) {
						HierarchyNode childNode = new HierarchyNode();
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
						if("RELATIONS".equalsIgnoreCase(uiSourceOfEvent)) {
							childNode.markNotEditableInRelationstable();
						}

						// add child to parent
						TreeNode childTreeNode = new DefaultTreeNode(childNode, expandedTreeNode);
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
						List<Map<String, Object>> childSmqRelationsCountList = this.smqBaseService.findSmqRelationsCountForSmqCodes(subList);
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
										HierarchyNode dummyNode = new HierarchyNode(null, null,
												null, null);
										dummyNode.setDummyNode(true);
										new DefaultTreeNode(dummyNode, childTreeNode);
									}
								}
							}
						}//end of if((null != childSmqRelationsCountList) &&.....
					}//end of for (List<Long> subList : choppedLists)
				}
				
				/*FacesMessage message = new FacesMessage(
						FacesMessage.SEVERITY_INFO, "Loading....",
						"Finding relations for SMQ_CODE "
								+ smqBase.getSmqCode());
				FacesContext.getCurrentInstance().addMessage(null, message);*/
				//now add relations of this node if any
				List<SmqRelation190> childRelations = this.smqBaseService.findSmqRelationsForSmqCode(smqBase.getSmqCode());
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
						childRelationNode.setCode(childRelation.getPtCode()
								.toString());
						childRelationNode.setEntity(childRelation);
						if("RELATIONS".equalsIgnoreCase(uiSourceOfEvent)) {
							childRelationNode.markNotEditableInRelationstable();
						}

						new DefaultTreeNode(childRelationNode, expandedTreeNode);
					}
				}
			} else if(entity instanceof MeddraDictHierarchySearchDto) {
				String parentLevel = hierarchyNode.getLevel();
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
				List<MeddraDictHierarchySearchDto> childDtos = this.meddraDictService.findChildrenByParentCode(
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
					
					if("RELATIONS".equalsIgnoreCase(uiSourceOfEvent)) {
						childNode.markNotEditableInRelationstable();
					}
					
					TreeNode childTreeNode = new DefaultTreeNode(childNode, expandedTreeNode);
					
					//fetch children count of this iterating child node by code of child
					//no need to do this is the childOfChild is LLT since LT is the leaf ode type
					if(!"LLT".equalsIgnoreCase(childLevel)) {
						Long countOfChildrenOfChild = this.meddraDictService.findChldrenCountByParentCode(childchildOfChildSearchColumnTypePrefix
											, childSearchColumnTypePrefix, Long.valueOf(childDto.getCode()));
						if(countOfChildrenOfChild > 0) {
							// add a dummmy node to show expand arrow
							HierarchyNode dummyNode = new HierarchyNode(null, null, null, null);
							dummyNode.setDummyNode(true);
							new DefaultTreeNode(dummyNode, childTreeNode);
						}
					}
				}
			}else if(entity instanceof MeddraDictReverseHierarchySearchDto) {
				MeddraDictReverseHierarchySearchDto reverseSearchDto = (MeddraDictReverseHierarchySearchDto)entity;
				String levelOfExpandedNode = hierarchyNode.getLevel();
				if("LLT".equalsIgnoreCase(levelOfExpandedNode)) {
					Long lltCode = Long.valueOf(reverseSearchDto.getLltCode());
					List<MeddraDictReverseHierarchySearchDto> childReverseSearchDtos = this.meddraDictService.findReverseByCode("LLT_", "PT_", lltCode);
					if(CollectionUtils.isNotEmpty(childReverseSearchDtos)) {
						for (MeddraDictReverseHierarchySearchDto childReverseSearchDto : childReverseSearchDtos) {
							HierarchyNode childNode = this.createMeddraReverseNode(childReverseSearchDto, "PT", hierarchyNode.isPrimaryPathFlag());
							if("RELATIONS".equalsIgnoreCase(uiSourceOfEvent)) {
								childNode.markNotEditableInRelationstable();
							}
							TreeNode childTreeNode = new DefaultTreeNode(childNode, expandedTreeNode);
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
					List<MeddraDictReverseHierarchySearchDto> childReverseSearchDtos = this.meddraDictService.findReverseByCode("PT_", "HLT_", ptCode);
					if(CollectionUtils.isNotEmpty(childReverseSearchDtos)) {
						for (MeddraDictReverseHierarchySearchDto childReverseSearchDto : childReverseSearchDtos) {
							boolean isPrimary = false;
							if("Y".equalsIgnoreCase(childReverseSearchDto.getPrimaryPathFlag())) {
								isPrimary = true;
							}
							HierarchyNode childNode = this.createMeddraReverseNode(childReverseSearchDto, "HLT", isPrimary);
							if("RELATIONS".equalsIgnoreCase(uiSourceOfEvent)) {
								childNode.markNotEditableInRelationstable();
							}
							TreeNode childTreeNode = new DefaultTreeNode(childNode, expandedTreeNode);
							if(StringUtils.isNotBlank(reverseSearchDto.getHltTerm())) {
								// add a dummmy node to show expand arrow
								HierarchyNode dummyNode = new HierarchyNode(null, null, null, null);
								dummyNode.setDummyNode(true);
								new DefaultTreeNode(dummyNode, childTreeNode);
							}
						}
					}
				} else if ("HLT".equalsIgnoreCase(levelOfExpandedNode)) {
					Long hltCode = Long.valueOf(reverseSearchDto.getHltCode());
					List<MeddraDictReverseHierarchySearchDto> childReverseSearchDtos = this.meddraDictService.findReverseByCode("HLT_", "HLGT_", hltCode);
					if(CollectionUtils.isNotEmpty(childReverseSearchDtos)) {
						for (MeddraDictReverseHierarchySearchDto childReverseSearchDto : childReverseSearchDtos) {
							HierarchyNode childNode = this.createMeddraReverseNode(childReverseSearchDto, "HLGT", hierarchyNode.isPrimaryPathFlag());
							if("RELATIONS".equalsIgnoreCase(uiSourceOfEvent)) {
								childNode.markNotEditableInRelationstable();
							}
							TreeNode childTreeNode = new DefaultTreeNode(childNode, expandedTreeNode);
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
					List<MeddraDictReverseHierarchySearchDto> childReverseSearchDtos = this.meddraDictService.findReverseByCode("HLGT_", "SOC_", hlgtCode);
					if(CollectionUtils.isNotEmpty(childReverseSearchDtos)) {
						for (MeddraDictReverseHierarchySearchDto childReverseSearchDto : childReverseSearchDtos) {
							HierarchyNode childNode = this.createMeddraReverseNode(childReverseSearchDto, "SOC", hierarchyNode.isPrimaryPathFlag());
							if("RELATIONS".equalsIgnoreCase(uiSourceOfEvent)) {
								childNode.markNotEditableInRelationstable();
							}
							new DefaultTreeNode(childNode, expandedTreeNode);
						}
					}
				}				
			} else if (entity instanceof CmqBase190) {
				CmqBase190 cmqBase = (CmqBase190) entity;
				Long cmqCode = cmqBase.getCmqCode();
				List<CmqBase190> childCmqBaseList = cmqBaseService.findChildCmqsByParentCode(cmqCode);
				
				List<Long> childCmqCodeList = new ArrayList<>();
				Map<Long, TreeNode> childTreeNodes = new HashMap<Long, TreeNode>();
				
				if ((null != childCmqBaseList) && (childCmqBaseList.size() > 0)) {
					for (CmqBase190 childCmqBase : childCmqBaseList) {
						HierarchyNode node = new HierarchyNode();
						node.setLevel(levelH);
						node.setTerm(childCmqBase.getCmqName());
						node.setCode(childCmqBase.getCmqCode().toString());
						node.setEntity(childCmqBase);
						if("RELATIONS".equalsIgnoreCase(uiSourceOfEvent)) {
							node.markNotEditableInRelationstable();
						}
						TreeNode cmqBaseChildNode = new DefaultTreeNode(node, expandedTreeNode);
						
						childTreeNodes.put(childCmqBase.getCmqCode(), cmqBaseChildNode);
						childCmqCodeList.add(childCmqBase.getCmqCode());
					}
					
					List<Map<String, Object>> childrenOfChildCountsList = this.cmqBaseService.findCmqChildCountForParentCmqCode(childCmqCodeList);
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
					List<Map<String, Object>> relationsCountsList = this.cmqRelationService.findCountByCmqCodes(childCmqCodeList);	
					if((null != relationsCountsList) && (relationsCountsList.size() > 0)) {
						ListIterator li = relationsCountsList.listIterator();
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
				
				List<CmqRelation190> existingRelations = this.cmqRelationService.findByCmqCode(cmqCode);
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
							SmqBase190 smqBase = this.smqBaseService.findByCode(cmqRelation.getSmqCode());
							HierarchyNode node = this.createSmqBaseNode(smqBase);
							if("RELATIONS".equalsIgnoreCase(uiSourceOfEvent)) {
								node.markNotEditableInRelationstable();
							}
							TreeNode treeNode = new DefaultTreeNode(node, expandedTreeNode);
							
							//add a dummy node for either of the cases, expansion will handle the actuals later
							Long smqBaseChildrenCount = this.smqBaseService.findChildSmqCountByParentSmqCode(smqBase.getSmqCode());
							if((null != smqBaseChildrenCount) && (smqBaseChildrenCount > 0)) {
								// add a dummmy node to show expand arrow
								HierarchyNode dummyNode = new HierarchyNode(null, null,
										null, null);
								dummyNode.setDummyNode(true);
								new DefaultTreeNode(dummyNode, treeNode);
							} else {
								Long childSmqrelationsCount = this.smqBaseService
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
						List<MeddraDictHierarchySearchDto> socDtos = this.meddraDictService.findByCodes("SOC_", socCodesList);
						for (MeddraDictHierarchySearchDto meddraDictHierarchySearchDto : socDtos) {
							HierarchyNode node = this.createMeddraNode(meddraDictHierarchySearchDto, "SOC");
							if("RELATIONS".equalsIgnoreCase(uiSourceOfEvent)) {
								node.markNotEditableInRelationstable();
							}
							TreeNode treeNode = new DefaultTreeNode(node, expandedTreeNode);
							
							Long countOfChildren = this.meddraDictService.findChldrenCountByParentCode("HLGT_",
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
						List<MeddraDictHierarchySearchDto> socDtos = this.meddraDictService.findByCodes("HLGT_", hlgtCodesList);
						for (MeddraDictHierarchySearchDto meddraDictHierarchySearchDto : socDtos) {
							HierarchyNode node = this.createMeddraNode(meddraDictHierarchySearchDto, "HLGT");
							if("RELATIONS".equalsIgnoreCase(uiSourceOfEvent)) {
								node.markNotEditableInRelationstable();
							}
							TreeNode treeNode = new DefaultTreeNode(node, expandedTreeNode);
							
							Long countOfChildren = this.meddraDictService.findChldrenCountByParentCode("HLT_",
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
						List<MeddraDictHierarchySearchDto> socDtos = this.meddraDictService.findByCodes("HLT_", hltCodesList);
						for (MeddraDictHierarchySearchDto meddraDictHierarchySearchDto : socDtos) {
							HierarchyNode node = this.createMeddraNode(meddraDictHierarchySearchDto, "HLT");
							if("RELATIONS".equalsIgnoreCase(uiSourceOfEvent)) {
								node.markNotEditableInRelationstable();
							}
							TreeNode treeNode = new DefaultTreeNode(node, expandedTreeNode);
							
							Long countOfChildren = this.meddraDictService.findChldrenCountByParentCode("PT_",
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
						List<MeddraDictHierarchySearchDto> socDtos = this.meddraDictService.findByCodes("PT_", ptCodesList);
						for (MeddraDictHierarchySearchDto meddraDictHierarchySearchDto : socDtos) {
							HierarchyNode node = this.createMeddraNode(meddraDictHierarchySearchDto, "PT");
							if("RELATIONS".equalsIgnoreCase(uiSourceOfEvent)) {
								node.markNotEditableInRelationstable();
							}
							TreeNode treeNode = new DefaultTreeNode(node, expandedTreeNode);
							
							Long countOfChildren = this.meddraDictService.findChldrenCountByParentCode("LLT_",
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
						List<MeddraDictHierarchySearchDto> socDtos = this.meddraDictService.findByCodes("LLT_", lltCodesList);
						for (MeddraDictHierarchySearchDto meddraDictHierarchySearchDto : socDtos) {
							HierarchyNode node = this.createMeddraNode(meddraDictHierarchySearchDto, "LLT");
							if("RELATIONS".equalsIgnoreCase(uiSourceOfEvent)) {
								node.markNotEditableInRelationstable();
							}
							TreeNode treeNode = new DefaultTreeNode(node, expandedTreeNode);
						}
					}
				}
			}
			hierarchyNode.setDataFetchCompleted(true);
		}
	}

	/**
	 * Add the selected hierarchy details to the relation list.
	 */
	public void addSelectedToRelation(TreeNode[] nodes) {
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
							
								//no drill down for MeddraDictReverseHierarchySearchDto node types
								if(!(relationsHierarchyNode.getEntity() instanceof MeddraDictReverseHierarchySearchDto)) {
									relationsHierarchyNode.setDataFetchCompleted(false);
									List<TreeNode> childTreeNodes = treeNode.getChildren();
									if(CollectionUtils.isNotEmpty(childTreeNodes)) {
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
					FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_INFO,
							existingNodeTerms + " skipped as they are already added to relations. Remaining relations added succesfully.", "");
					FacesContext.getCurrentInstance().addMessage(null, message);
				} else {
					FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_INFO,
							"Selected relations added sucessfully.", "");
					FacesContext.getCurrentInstance().addMessage(null, message);
				}
			}
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_FATAL,
					"An error occured while adding relations.", e.getMessage());
			FacesContext.getCurrentInstance().addMessage(null, message);
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
						this.cmqBaseService.update(cmqEntity);
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
								this.cmqRelationService.remove(cmqRelationIdToDelete);
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

	public void saveDetails() {
		log.debug("save cmq details ... ");
		try {
			cmqBaseService.update(selectedData);
			FacesMessage msg = new FacesMessage("Successful save a CMQ", null);
			FacesContext.getCurrentInstance().addMessage(null, msg);
		} catch (Exception e) {
			log.error("Error when sae cmq - {}", e.getMessage(), e);
			FacesMessage msg = new FacesMessage("Failed - " + e.getMessage(),
					null);
			FacesContext.getCurrentInstance().addMessage(null, msg);
		}
	}

	public void saveInfos() {
		log.debug("save cmq infos ... ");
		try {
			cmqBaseService.update(selectedData);
			FacesMessage msg = new FacesMessage("Successful save a CMQ", null);
			FacesContext.getCurrentInstance().addMessage(null, msg);
		} catch (Exception e) {
			log.error("Error when sae cmq - {}", e.getMessage(), e);
			FacesMessage msg = new FacesMessage("Failed - " + e.getMessage(),
					null);
			FacesContext.getCurrentInstance().addMessage(null, msg);
		}
	}
	
	public void resetHierarchySearch() {
		this.hierarchyRoot = new DefaultTreeNode("root", new HierarchyNode(
				"LEVEL", "NAME", "CODE", null), null);
		this.levelH = null;
		this.termNameOfHierarchySearch = "";
	}
	
	public void onCloseHierarchySearch(CloseEvent event) {
		this.hierarchyRootCopy = this.hierarchyRoot;
		resetHierarchySearch();
	}

	public String[] getSelectedSOCs() {
		return selectedSOCs;
	}

	public void setSelectedSOCs(String[] selectedSOCs) {
		this.selectedSOCs = selectedSOCs;
	}

	public String getCodelist() {
		return codelist;
	}

	public void setCodelist(String codelist) {
		this.codelist = codelist;
	}

	public String getLevelH() {
		return levelH;
	}

	public void setLevelH(String levelH) {
		this.levelH = levelH;
	}

	public void setHierarchyRoot(TreeNode hierarchyRoot) {
		this.hierarchyRoot = hierarchyRoot;
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
			/*this.selctedData = this.cmqBaseService.findByCode(clickedCmqCode);
			List<CmqRelation190> cmqRelationList = this.cmqRelationService
					.findByCmqCode(clickedCmqCode);
			relationsRoot = new DefaultTreeNode("root", new HierarchyNode(
					"LEVEL", "NAME", "CODE", "SCOPE", "CATEGORY", "WEIGHT",
					null), null);
			for (CmqRelation190 cmqRelation : cmqRelationList) {
				HierarchyNode node = null;
				if ((cmqRelation.getSmqCode() != null)
						&& (cmqRelation.getSmqCode() > 0)) {
					SmqBase190 smqBase190 = this.smqBaseService
							.findByCode(cmqRelation.getSmqCode());
					if (null != smqBase190) {
						node = this.createSmqBaseNode(smqBase190);
						node.setCategory(cmqRelation.getTermCategory());
						node.setScope(cmqRelation.getTermScope());
						node.setWeight(cmqRelation.getTermWeight() + "");
						TreeNode smqBaseTreeNode = new DefaultTreeNode(node,
								this.relationsRoot);
						long childSmqrelationsCount = this.smqBaseService
								.findSmqRelationsCountForSmqCode(smqBase190
										.getSmqCode());
						if (childSmqrelationsCount > 0) {
							// add a dummmy node to show expand arrow
							HierarchyNode dummyNode = new HierarchyNode(null,
									null, null, null);
							dummyNode.setDummyNode(true);
							new DefaultTreeNode(dummyNode, smqBaseTreeNode);
						}
					}
				} else if ((cmqRelation.getSocCode() != null)
						&& (cmqRelation.getSocCode() > 0)) {
					MeddraDictHierarchySearchDto searchDto = this.meddraDictService
							.findByCode("SOC_", cmqRelation.getSocCode());
					node = this.createMeddraNode(searchDto, "SOC");
					node.setCategory(cmqRelation.getTermCategory());
					node.setScope(cmqRelation.getTermScope());
					node.setWeight(cmqRelation.getTermWeight() + "");
					new DefaultTreeNode(node, this.relationsRoot);
				} else if ((cmqRelation.getHlgtCode() != null)
						&& (cmqRelation.getHlgtCode() > 0)) {
					MeddraDictHierarchySearchDto searchDto = this.meddraDictService
							.findByCode("HLGT_", cmqRelation.getHlgtCode());
					node = this.createMeddraNode(searchDto, "HLGT");
					node.setCategory(cmqRelation.getTermCategory());
					node.setScope(cmqRelation.getTermScope());
					node.setWeight(cmqRelation.getTermWeight() + "");
					new DefaultTreeNode(node, this.relationsRoot);
				} else if ((cmqRelation.getHltCode() != null)
						&& (cmqRelation.getHltCode() > 0)) {
					MeddraDictHierarchySearchDto searchDto = this.meddraDictService
							.findByCode("HLT_", cmqRelation.getHltCode());
					node = this.createMeddraNode(searchDto, "HLT");
					node.setCategory(cmqRelation.getTermCategory());
					node.setScope(cmqRelation.getTermScope());
					node.setWeight(cmqRelation.getTermWeight() + "");
					new DefaultTreeNode(node, this.relationsRoot);
				} else if ((cmqRelation.getPtCode() != null)
						&& (cmqRelation.getPtCode() > 0)) {
					MeddraDictHierarchySearchDto searchDto = this.meddraDictService
							.findByCode("PT_", cmqRelation.getPtCode());
					node = this.createMeddraNode(searchDto, "PT");
					node.setCategory(cmqRelation.getTermCategory());
					node.setScope(cmqRelation.getTermScope());
					node.setWeight(cmqRelation.getTermWeight() + "");
					new DefaultTreeNode(node, this.relationsRoot);
				} else if ((cmqRelation.getLltCode() != null)
						&& (cmqRelation.getLltCode() > 0)) {
					MeddraDictHierarchySearchDto searchDto = this.meddraDictService
							.findByCode("LLT_", cmqRelation.getLltCode());
					node = this.createMeddraNode(searchDto, "LLT");
					node.setCategory(cmqRelation.getTermCategory());
					node.setScope(cmqRelation.getTermScope());
					node.setWeight(cmqRelation.getTermWeight() + "");
					new DefaultTreeNode(node, this.relationsRoot);
				}
			}*/
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
		this.clickedCmqCode = clickedCmqCode;
		this.selctedData = this.cmqBaseService.findByCode(clickedCmqCode);
		List<CmqRelation190> cmqRelationList = this.cmqRelationService
				.findByCmqCode(clickedCmqCode);
		relationsRoot = new DefaultTreeNode("root", new HierarchyNode("LEVEL",
				"NAME", "CODE", "SCOPE", "CATEGORY", "WEIGHT", null), null);
		//first process the cmq relations
		log.info("Populating cmq relations for cmq code " + clickedCmqCode);
		for (CmqRelation190 cmqRelation : cmqRelationList) {
			HierarchyNode node = null;
			if ((cmqRelation.getSmqCode() != null)
					&& (cmqRelation.getSmqCode() > 0)) {
				if((cmqRelation.getPtCode() != null)
						&& (cmqRelation.getPtCode() > 0)) {
					//this is an smq relation
					SmqRelation190 childRelation = this.smqBaseService.findSmqRelationBySmqAndPtCode(cmqRelation.getSmqCode()
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
					new DefaultTreeNode(childRelationNode, this.relationsRoot);
				} else {
					//this is an smq base 
					SmqBase190 smqBase190 = this.smqBaseService
							.findByCode(cmqRelation.getSmqCode());
					if (null != smqBase190) {
						node = this.createSmqBaseNode(smqBase190);
						node.setCategory((cmqRelation.getTermCategory() == null) ? "" : cmqRelation.getTermCategory());
						node.setScope((cmqRelation.getTermScope() == null) ? "" : cmqRelation.getTermScope());
						node.setWeight((cmqRelation.getTermWeight() == null) ? "" : cmqRelation.getTermWeight() + "");
						TreeNode smqBaseTreeNode = new DefaultTreeNode(node,
								this.relationsRoot);
						long childSmqrelationsCount = this.smqBaseService
								.findSmqRelationsCountForSmqCode(smqBase190
										.getSmqCode());
						if (childSmqrelationsCount > 0) {
							// add a dummmy node to show expand arrow
							HierarchyNode dummyNode = new HierarchyNode(null, null,
									null, null);
							dummyNode.setDummyNode(true);
							new DefaultTreeNode(dummyNode, smqBaseTreeNode);
						}
					}
				}
			} else if ((cmqRelation.getSocCode() != null)
					&& (cmqRelation.getSocCode() > 0)) {
				MeddraDictHierarchySearchDto searchDto = this.meddraDictService
						.findByCode("SOC_", cmqRelation.getSocCode());
				node = this.createMeddraNode(searchDto, "SOC");
				node.setCategory((cmqRelation.getTermCategory() == null) ? "" : cmqRelation.getTermCategory());
				node.setScope((cmqRelation.getTermScope() == null) ? "" : cmqRelation.getTermScope());
				node.setWeight((cmqRelation.getTermWeight() == null) ? "" : cmqRelation.getTermWeight() + "");
				TreeNode treeNode = new DefaultTreeNode(node, this.relationsRoot);

				Long childCount = this.meddraDictService.findChldrenCountByParentCode("HLGT_", "SOC_", cmqRelation.getSocCode());
				if((null != childCount) && (childCount > 0)) {
					HierarchyNode dummyNode = new HierarchyNode(null, null, null, null);
					dummyNode.setDummyNode(true);
					new DefaultTreeNode(dummyNode, treeNode);
				}
			} else if ((cmqRelation.getHlgtCode() != null)
					&& (cmqRelation.getHlgtCode() > 0)) {
				MeddraDictHierarchySearchDto searchDto = this.meddraDictService
						.findByCode("HLGT_", cmqRelation.getHlgtCode());
				node = this.createMeddraNode(searchDto, "HLGT");
				node.setCategory((cmqRelation.getTermCategory() == null) ? "" : cmqRelation.getTermCategory());
				node.setScope((cmqRelation.getTermScope() == null) ? "" : cmqRelation.getTermScope());
				node.setWeight((cmqRelation.getTermWeight() == null) ? "" : cmqRelation.getTermWeight() + "");
				TreeNode treeNode = new DefaultTreeNode(node, this.relationsRoot);

				Long childCount = this.meddraDictService.findChldrenCountByParentCode("HLT_", "HLGT_", cmqRelation.getHlgtCode());
				if((null != childCount) && (childCount > 0)) {
					HierarchyNode dummyNode = new HierarchyNode(null, null, null, null);
					dummyNode.setDummyNode(true);
					new DefaultTreeNode(dummyNode, treeNode);
				}
			} else if ((cmqRelation.getHltCode() != null)
					&& (cmqRelation.getHltCode() > 0)) {
				MeddraDictHierarchySearchDto searchDto = this.meddraDictService
						.findByCode("HLT_", cmqRelation.getHltCode());
				node = this.createMeddraNode(searchDto, "HLT");
				node.setCategory((cmqRelation.getTermCategory() == null) ? "" : cmqRelation.getTermCategory());
				node.setScope((cmqRelation.getTermScope() == null) ? "" : cmqRelation.getTermScope());
				node.setWeight((cmqRelation.getTermWeight() == null) ? "" : cmqRelation.getTermWeight() + "");
				TreeNode treeNode = new DefaultTreeNode(node, this.relationsRoot);

				Long childCount = this.meddraDictService.findChldrenCountByParentCode("PT_", "HLT_", cmqRelation.getHltCode());
				if((null != childCount) && (childCount > 0)) {
					HierarchyNode dummyNode = new HierarchyNode(null, null, null, null);
					dummyNode.setDummyNode(true);
					new DefaultTreeNode(dummyNode, treeNode);
				}
			} else if ((cmqRelation.getPtCode() != null)
					&& (cmqRelation.getPtCode() > 0)) {
				MeddraDictReverseHierarchySearchDto searchDto = this.meddraDictService.findByPtOrLltCode("PT_", cmqRelation.getPtCode());
				node = this.createMeddraReverseNode(searchDto, "PT", false);
				node.setCategory((cmqRelation.getTermCategory() == null) ? "" : cmqRelation.getTermCategory());
				node.setScope((cmqRelation.getTermScope() == null) ? "" : cmqRelation.getTermScope());
				node.setWeight((cmqRelation.getTermWeight() == null) ? "" : cmqRelation.getTermWeight() + "");
				new DefaultTreeNode(node, this.relationsRoot);
			} else if ((cmqRelation.getLltCode() != null)
					&& (cmqRelation.getLltCode() > 0)) {
				MeddraDictReverseHierarchySearchDto searchDto = this.meddraDictService.findByPtOrLltCode("LLT_", cmqRelation.getLltCode());
				node = this.createMeddraReverseNode(searchDto, "LLT", false);
				node.setCategory((cmqRelation.getTermCategory() == null) ? "" : cmqRelation.getTermCategory());
				node.setScope((cmqRelation.getTermScope() == null) ? "" : cmqRelation.getTermScope());
				node.setWeight((cmqRelation.getTermWeight() == null) ? "" : cmqRelation.getTermWeight() + "");
				new DefaultTreeNode(node, this.relationsRoot);
			}
		}
		
		log.info("Populating cmq base children for cmq parent code " + clickedCmqCode);
		//now process the cmq parent child relations
		List<CmqBase190> childCmqs = this.cmqBaseService.findChildCmqsByParentCode(clickedCmqCode);
		if((null != childCmqs) && (childCmqs.size() > 0)) {
			for (CmqBase190 childCmq : childCmqs) {
				HierarchyNode node = this.createCmqBaseNode(childCmq);
				node.setEntity(childCmq);
				TreeNode treeNode = new DefaultTreeNode(node, this.relationsRoot);
			
				Long childCount = this.cmqRelationService.findCountByCmqCode(childCmq.getCmqCode());
				if((null != childCount) && (childCount > 0)) {
					HierarchyNode dummyNode = new HierarchyNode(null, null, null, null);
					dummyNode.setDummyNode(true);
					new DefaultTreeNode(dummyNode, treeNode);
				}
			}
		}
	}
	
	public boolean isDataModified() {
		return dataModified;
	}

	public void setDataModified(boolean dataModified) {
		this.dataModified = dataModified;
		if(this.dataModified == true) {
			search();
		}
	}

	/**
	 * Event handler for drag-and-drop from "Hierarchy Search" treetable to "Result Relations" treetable
	 */
	public void onRelationDrop() {
		if(relationSelected.length > 0) {
			// Multiple item Drag-n-Drop
			addSelectedToRelation(relationSelected);
		} else {
			//One by one Drag-n-Drop
//			Map<String,String> params = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap();
//	        String nodeLevel = params.get("level");
//	        String nodeName = params.get("name");
//	        String nodeCode = params.get("code");
//	        Long nodeEntityId = Long.parseLong(params.get("entityId"));
//	        
//			TreeNode treeNode = findTreenodeByEntityId(hierarchyRoot, nodeEntityId);
//			setRelationSelected(new TreeNode[] {treeNode});
//			
//			addSelectedToRelation(relationSelected);
			FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "No relation is selected to add", "");
			FacesContext.getCurrentInstance().addMessage(null, msg);
		}
	}
	
	/**
	 * Reset relations
	 */
	public String resetRelations() {
		if(isDataModified()) {
			buildRelationsRoot();
		}
		FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_WARN, "Form canceled", "");
		FacesContext.getCurrentInstance().addMessage(null, msg);
		return "";
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
}
