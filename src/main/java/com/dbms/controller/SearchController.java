package com.dbms.controller;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
import javax.faces.event.AjaxBehaviorEvent;

import org.apache.commons.lang3.StringUtils;
import org.primefaces.component.wizard.Wizard;
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
import com.dbms.service.ICmqBase190Service;
import com.dbms.service.ICmqRelation190Service;
import com.dbms.service.IMeddraDictService;
import com.dbms.service.IRefCodeListService;
import com.dbms.service.ISmqBaseService;
import com.dbms.util.CqtConstants;
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

	private List<CreateEntity> values, vals;

	private List<CodelistDTO> admins;

	private Wizard updateWizard, copyWizard, browseWizard;

	private String[] selectedSOCs;
	private TreeNode hierarchyRoot;

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
		this.state = ""; // default is "All"
		this.status = ""; // default is "All"
		this.level = 1;
		this.critical = null;
		this.group = ""; // default is "All"
		this.extension = ""; // default is "All"
		drugProgram = ""; // default is "All"
		product = ""; // default is "All"
		protocol = ""; // default is "All"

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

	public void reset() {
		this.datas = new ArrayList<CmqBase190>();

		resetSearch();
		changeLevel();
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
		this.state = "Published";
		this.status = "Active";
		this.level = 1;
		// this.critical = "No";
		this.group = "No Group";

		this.product = "";
		this.protocol = "";
		this.drugProgram = "";
		this.termName = "";
		this.code = null;
	}

	/**
	 * Method to change State value on status selection.
	 * 
	 * @param event
	 *            AjaxBehaviour
	 */
	public void changeState(AjaxBehaviorEvent event) {		
		if (status.equals("P"))
			setState("Draft");
		if (status.equals("A"))
			setState("Published");
		
		if (status.equals(""))
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

	public void search() {
		log.debug("search by{}", extension);

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
		buildRelationsRoot();
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

	public String hierarchySearch() {
		int level = 0;
		String meddraSearchTermPrefix = null;
		boolean searchSmqBase = false;
		boolean searchMeddraBase = false;
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
			searchMeddraBase = true;
		} else if ("LLT".equalsIgnoreCase(levelH)) {
			meddraSearchTermPrefix = "LLT_";
			searchMeddraBase = true;
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

				// process children now
				Set<SmqBase190> childSmqBaseList = smqBase.getSmqBaseChildres();
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

					// add child to parent
					TreeNode childTreeNode = new DefaultTreeNode(childNode,
							parentTreeNode);
					long childSmqrelationsCount = this.smqBaseService
							.findSmqRelationsCountForSmqCode(childSmqBase
									.getSmqCode());
					if (childSmqrelationsCount > 0) {
						// add a dummmy node to show expand arrow
						HierarchyNode dummyNode = new HierarchyNode(null, null,
								null, null);
						dummyNode.setDummyNode(true);
						new DefaultTreeNode(dummyNode, childTreeNode);
					}
				} // end of for (SmqBase190 childSmqBase : childSmqBaseList)
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
		} else if (searchCmqBase) {
			List<CmqBase190> cmqBaseList = cmqBaseService.findByLevelAndTerm(2,
					termNameOfHierarchySearch);
			this.hierarchyRoot = new DefaultTreeNode("root", new HierarchyNode(
					"LEVEL", "NAME", "CODE", null), null);
			boolean dummyNodeAdded = false;
			for (CmqBase190 cmqBase190 : cmqBaseList) {
				HierarchyNode node = new HierarchyNode();
				node.setLevel(levelH);
				node.setTerm(cmqBase190.getCmqName());
				node.setCode(cmqBase190.getCmqCode().toString());
				node.setEntity(cmqBase190);

				TreeNode cmqBaseTreeNode = new DefaultTreeNode(node,
						hierarchyRoot);

				Long childCount = this.cmqBaseService
						.findCmqChildCountForParentCmqCode(cmqBase190
								.getCmqCode());

				if ((null != childCount) && childCount.longValue() > 0) {
					// add a dummmy node to show expand arrow
					HierarchyNode dummyNode = new HierarchyNode(null, null,
							null, null);
					dummyNode.setDummyNode(true);
					new DefaultTreeNode(dummyNode, cmqBaseTreeNode);
					dummyNodeAdded = true;
				}
				
				if(!dummyNodeAdded) {
					//add cmq relations now
					List<CmqRelation190> existingRelations = this.cmqRelationService.findByCmqCode(cmqBase190.getCmqCode());
					if((null != existingRelations) && (existingRelations.size() > 0)) {
						// add a dummmy node to show expand arrow
						HierarchyNode dummyNode = new HierarchyNode(null, null, null, null);
						dummyNode.setDummyNode(true);
						new DefaultTreeNode(dummyNode, cmqBaseTreeNode);
						dummyNodeAdded = true;
					}
				}
			}
		}

		return "";
	}

	public void onNodeExpand(NodeExpandEvent event) {
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

				FacesMessage message = new FacesMessage(
						FacesMessage.SEVERITY_INFO, "Loading....",
						"Finding relations for SMQ_CODE "
								+ smqBase.getSmqCode());
				FacesContext.getCurrentInstance().addMessage(null, message);

				List<SmqRelation190> childRelations = this.smqBaseService
						.findSmqRelationsForSmqCode(smqBase.getSmqCode());
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
			} else if (entity instanceof CmqBase190) {
				CmqBase190 cmqBase = (CmqBase190) entity;
				Long cmqCode = cmqBase.getCmqCode();
				List<CmqBase190> childCmqBaseList = cmqBaseService.findChildCmqsByParentCode(cmqCode);
				if ((null != childCmqBaseList) && (childCmqBaseList.size() > 0)) {
					for (CmqBase190 childCmqBase : childCmqBaseList) {
						HierarchyNode node = new HierarchyNode();
						node.setLevel(levelH);
						node.setTerm(childCmqBase.getCmqName());
						node.setCode(childCmqBase.getCmqCode().toString());
						node.setEntity(childCmqBase);
						TreeNode cmqBaseChildNode = new DefaultTreeNode(node, expandedTreeNode);
						
						Long childCount = this.cmqBaseService
								.findCmqChildCountForParentCmqCode(childCmqBase
										.getCmqCode());
						boolean dummyNodeAdded = false;
						if ((null != childCount) && childCount.longValue() > 0) {
							// add a dummmy node to show expand arrow
							HierarchyNode dummyNode = new HierarchyNode(null, null,
									null, null);
							dummyNode.setDummyNode(true);
							new DefaultTreeNode(dummyNode, cmqBaseChildNode);
							dummyNodeAdded = true;
						}
						
						if(!dummyNodeAdded) {
							//add cmq relations now
							List<CmqRelation190> existingRelations = this.cmqRelationService.findByCmqCode(childCmqBase
									.getCmqCode());
							if((null != existingRelations) && (existingRelations.size() > 0)) {
								// add a dummmy node to show expand arrow
								HierarchyNode dummyNode = new HierarchyNode(null, null, null, null);
								dummyNode.setDummyNode(true);
								new DefaultTreeNode(dummyNode, cmqBaseChildNode);
								dummyNodeAdded = true;
							}
						}
					}
				}
				
				//add cmq relations now
				List<CmqRelation190> existingRelations = this.cmqRelationService.findByCmqCode(cmqCode);
				if((null != existingRelations) && (existingRelations.size() > 0)) {
					for (CmqRelation190 cmqRelation : existingRelations) {
						
						if((cmqRelation.getSocCode() != null) && (cmqRelation.getSocCode().longValue() > 0)) {
							MeddraDictHierarchySearchDto meddraDictHierarchySearchDto = this.meddraDictService.findByCode("SOC_", cmqRelation.getSocCode());
							HierarchyNode node = this.createMeddraNode(meddraDictHierarchySearchDto, "SOC");
							TreeNode treeNode = new DefaultTreeNode(node, expandedTreeNode);
							
							Long countOfChildren = this.meddraDictService.findChldrenCountByParentCode("HLGT_",
									"SOC_", Long.valueOf(cmqRelation.getSocCode()));
							if((null != countOfChildren) && (countOfChildren > 0)) {
								// add a dummmy node to show expand arrow
								HierarchyNode dummyNode = new HierarchyNode(null, null,
										null, null);
								dummyNode.setDummyNode(true);
								new DefaultTreeNode(dummyNode, treeNode);
							}
						} else if((cmqRelation.getHlgtCode() != null) && (cmqRelation.getHlgtCode().longValue() > 0)) {
							MeddraDictHierarchySearchDto meddraDictHierarchySearchDto = this.meddraDictService.findByCode("HLGT_", cmqRelation.getHlgtCode());
							HierarchyNode node = this.createMeddraNode(meddraDictHierarchySearchDto, "HLGT");
							TreeNode treeNode = new DefaultTreeNode(node, expandedTreeNode);
							
							Long countOfChildren = this.meddraDictService.findChldrenCountByParentCode("HLT_",
									"HLGT_", Long.valueOf(cmqRelation.getHlgtCode()));
							if((null != countOfChildren) && (countOfChildren > 0)) {
								// add a dummmy node to show expand arrow
								HierarchyNode dummyNode = new HierarchyNode(null, null,
										null, null);
								dummyNode.setDummyNode(true);
								new DefaultTreeNode(dummyNode, treeNode);
							}
						} else if((cmqRelation.getHltCode() != null) && (cmqRelation.getHltCode().longValue() > 0)) {
							MeddraDictHierarchySearchDto meddraDictHierarchySearchDto = this.meddraDictService.findByCode("HLT_", cmqRelation.getHltCode());
							HierarchyNode node = this.createMeddraNode(meddraDictHierarchySearchDto, "HLT");
							TreeNode treeNode = new DefaultTreeNode(node, expandedTreeNode);
							
							Long countOfChildren = this.meddraDictService.findChldrenCountByParentCode("PT_",
									"HLT_", Long.valueOf(cmqRelation.getHltCode()));
							if((null != countOfChildren) && (countOfChildren > 0)) {
								// add a dummmy node to show expand arrow
								HierarchyNode dummyNode = new HierarchyNode(null, null,
										null, null);
								dummyNode.setDummyNode(true);
								new DefaultTreeNode(dummyNode, treeNode);
							}
						} else if((cmqRelation.getPtCode() != null) && (cmqRelation.getPtCode().longValue() > 0)) {
							MeddraDictHierarchySearchDto meddraDictHierarchySearchDto = this.meddraDictService.findByCode("PT_", cmqRelation.getPtCode());
							HierarchyNode node = this.createMeddraNode(meddraDictHierarchySearchDto, "PT");
							TreeNode treeNode = new DefaultTreeNode(node, expandedTreeNode);
							
							Long countOfChildren = this.meddraDictService.findChldrenCountByParentCode("LLT_",
									"PT_", Long.valueOf(cmqRelation.getPtCode()));
							if((null != countOfChildren) && (countOfChildren > 0)) {
								// add a dummmy node to show expand arrow
								HierarchyNode dummyNode = new HierarchyNode(null, null,
										null, null);
								dummyNode.setDummyNode(true);
								new DefaultTreeNode(dummyNode, treeNode);
							}
						} else if((cmqRelation.getLltCode() != null) && (cmqRelation.getLltCode().longValue() > 0)) {
							MeddraDictHierarchySearchDto meddraDictHierarchySearchDto = this.meddraDictService.findByCode("LLT_", cmqRelation.getLltCode());
							HierarchyNode node = this.createMeddraNode(meddraDictHierarchySearchDto, "LLT");
							TreeNode treeNode = new DefaultTreeNode(node, expandedTreeNode);
						} else if((cmqRelation.getSmqCode() != null) && (cmqRelation.getSmqCode().longValue() > 0)) {
							SmqBase190 smqBase = this.smqBaseService.findByCode(cmqRelation.getSmqCode());
							HierarchyNode node = this.createSmqBaseNode(smqBase);
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
		if (nodes != null && nodes.length > 0) {
			List<TreeNode> nodesList = Arrays.asList(nodes);
			for (TreeNode treeNode : nodesList) {
				HierarchyNode hierarchyNode = (HierarchyNode) treeNode
						.getData();
				if ((null != hierarchyNode) && !hierarchyNode.isDummyNode()) {
					TreeNode parentNode = treeNode.getParent();
					if (!nodesList.contains(parentNode)) {
						// remove the first dummy node placeholder
						List<TreeNode> childTreeNodes = treeNode.getChildren();
						if ((null != childTreeNodes)
								&& (childTreeNodes.size() > 0)) {
							HierarchyNode dummyChildData = (HierarchyNode) childTreeNodes
									.get(0).getData();
							if (dummyChildData.isDummyNode()) {
								treeNode.getChildren().remove(0);
							}
						}
						// now add it to the parent
						treeNode.setParent(relationsRoot);
						relationsRoot.getChildren().add(treeNode);
					}
				}
			}
			// setRelationSelected(nodes);

			FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_INFO,
					"Relations selected", "");
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
			int i = relationsSelectedInRelationsTableList.indexOf(childTreeNode);
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

	private void addTreeNode(CmqRelation190 relation, TreeNode root) {
		/*
		 * if (relation.getChildren() != null &&
		 * !relation.getChildren().isEmpty()) { for (CmqRelation190 r :
		 * relation.getChildren()) { TreeNode node = new DefaultTreeNode(new
		 * RelationTreeNode( r.getId(), r.getTermName(), r.getCmqLevel(),
		 * r.getPtTermScope(), r.getPtTermCategory(), r.getPtTermWeight(),
		 * false), root); addTreeNode(r, node); } }
		 */
	}

	public void onRowEdit(RowEditEvent event) {
		/*
		 * TreeNode node = (DefaultTreeNode) event.getObject(); RelationTreeNode
		 * relationNode = (RelationTreeNode) node.getData();
		 * log.debug("update tree node scope#{},category#{},weigth#{}",
		 * relationNode.getScope(), relationNode.getCategory(),
		 * relationNode.getWeight()); try { if (relationNode.getRoot()) {
		 * CmqBase190 base = cmqBaseService.findById(relationNode .getCode());
		 * base.setScope(relationNode.getScope()); cmqBaseService.update(base);
		 * } else { CmqRelation190 relation = cmqRelationService
		 * .findById(relationNode.getCode());
		 * relation.setPtTermScope(relationNode.getScope());
		 * relation.setPtTermCategory(relationNode.getCategory());
		 * relation.setPtTermWeight(relationNode.getWeight());
		 * cmqRelationService.update(relation); } } catch (Exception e) {
		 * log.error("Error when update tree!", e); }
		 */
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
			this.selctedData = this.cmqBaseService.findByCode(clickedCmqCode);
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
			}
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
			} else if ((cmqRelation.getSocCode() != null)
					&& (cmqRelation.getSocCode() > 0)) {
				MeddraDictHierarchySearchDto searchDto = this.meddraDictService
						.findByCode("SOC_", cmqRelation.getSocCode());
				node = this.createMeddraNode(searchDto, "SOC");
				node.setCategory((cmqRelation.getTermCategory() == null) ? "" : cmqRelation.getTermCategory());
				node.setScope((cmqRelation.getTermScope() == null) ? "" : cmqRelation.getTermScope());
				node.setWeight((cmqRelation.getTermWeight() == null) ? "" : cmqRelation.getTermWeight() + "");
				new DefaultTreeNode(node, this.relationsRoot);
			} else if ((cmqRelation.getHlgtCode() != null)
					&& (cmqRelation.getHlgtCode() > 0)) {
				MeddraDictHierarchySearchDto searchDto = this.meddraDictService
						.findByCode("HLGT_", cmqRelation.getHlgtCode());
				node = this.createMeddraNode(searchDto, "HLGT");
				node.setCategory((cmqRelation.getTermCategory() == null) ? "" : cmqRelation.getTermCategory());
				node.setScope((cmqRelation.getTermScope() == null) ? "" : cmqRelation.getTermScope());
				node.setWeight((cmqRelation.getTermWeight() == null) ? "" : cmqRelation.getTermWeight() + "");
				new DefaultTreeNode(node, this.relationsRoot);
			} else if ((cmqRelation.getHltCode() != null)
					&& (cmqRelation.getHltCode() > 0)) {
				MeddraDictHierarchySearchDto searchDto = this.meddraDictService
						.findByCode("HLT_", cmqRelation.getHltCode());
				node = this.createMeddraNode(searchDto, "HLT");
				node.setCategory((cmqRelation.getTermCategory() == null) ? "" : cmqRelation.getTermCategory());
				node.setScope((cmqRelation.getTermScope() == null) ? "" : cmqRelation.getTermScope());
				node.setWeight((cmqRelation.getTermWeight() == null) ? "" : cmqRelation.getTermWeight() + "");
				new DefaultTreeNode(node, this.relationsRoot);
			} else if ((cmqRelation.getPtCode() != null)
					&& (cmqRelation.getPtCode() > 0)) {
				MeddraDictHierarchySearchDto searchDto = this.meddraDictService
						.findByCode("PT_", cmqRelation.getPtCode());
				node = this.createMeddraNode(searchDto, "PT");
				node.setCategory((cmqRelation.getTermCategory() == null) ? "" : cmqRelation.getTermCategory());
				node.setScope((cmqRelation.getTermScope() == null) ? "" : cmqRelation.getTermScope());
				node.setWeight((cmqRelation.getTermWeight() == null) ? "" : cmqRelation.getTermWeight() + "");
				new DefaultTreeNode(node, this.relationsRoot);
			} else if ((cmqRelation.getLltCode() != null)
					&& (cmqRelation.getLltCode() > 0)) {
				MeddraDictHierarchySearchDto searchDto = this.meddraDictService
						.findByCode("LLT_", cmqRelation.getLltCode());
				node = this.createMeddraNode(searchDto, "LLT");
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
				new DefaultTreeNode(node, this.relationsRoot);
			}
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
		}
	}
	
	/**
	 * Reset relations
	 */
	public String resetRelations() {
		buildRelationsRoot();
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
}
