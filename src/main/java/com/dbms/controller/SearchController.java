package com.dbms.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
import javax.faces.event.AjaxBehaviorEvent;

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
import com.dbms.entity.cqt.MeddraDict190;
import com.dbms.entity.cqt.SmqBase190;
import com.dbms.entity.cqt.SmqRelation190;
import com.dbms.service.ICmqBase190Service;
import com.dbms.service.ICmqRelation190Service;
import com.dbms.service.IMeddraDictService;
import com.dbms.service.ISmqBaseService;
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

	private String releaseStatus;
	private String criticalEvent;
	private String termName;
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
	private TreeNode relationsRoot;

	public SearchController() {
		this.selectedData = new CmqBase190();
	}

	@PostConstruct
	public void init() {
		this.maintainDesigBtn = false;
		this.state = "Published";
		this.status = "Active";
		this.level = 1;
		this.critical = "No";
		this.group = "No Group";
		this.extension = "";

		hierarchyRoot = new DefaultTreeNode("root", new HierarchyNode("LEVEL",
				"NAME", "CODE", null), null);
		relationsRoot = new DefaultTreeNode("root", new HierarchyNode("LEVEL",
				"NAME", "CODE", "SCOPE", "CATEGORY", "WEIGHT", null), null);
	}

	public void initSearch() {
		this.datas = new ArrayList<CmqBase190>();
	}

	public void reset() {
		this.datas = new ArrayList<CmqBase190>();
	}

	public TreeNode getHierarchyRoot() {
		return hierarchyRoot;
	}

	/**
	 * Method to change Level value on extention selection.
	 * 
	 * @param event
	 *            AjaxBehaviour
	 */
	public void changeLevel(AjaxBehaviorEvent event) {
		if (extension.equals("PRO")) {
			setLevel(1);
		} else
			setLevel(2);

		if (extension.equals("CPT") || extension.equals("DME")) {
			setDrugProgram("No Program");
			setProduct("No Product");
		} else {
			setDrugProgram("");
			setProduct("");
		}

		if (extension.equals("CPT") || extension.equals("DME")
				|| extension.equals("TME") || extension.equals("TR1"))
			setProtocol("No Protocol");
		else
			setProtocol("");

	}

	/**
	 * Reset search on every extension change.
	 * 
	 * @param event
	 */
	public void resetSearch(AjaxBehaviorEvent event) {
		this.state = "Published";
		this.status = "Active";
		this.level = 1;
		this.critical = "No";
		this.group = "No Group";

		this.product = "";
		this.protocol = "";
		this.drugProgram = "";
		this.extension = "";
	}

	/**
	 * Method to change State value on status selection.
	 * 
	 * @param event
	 *            AjaxBehaviour
	 */
	public void changeState(AjaxBehaviorEvent event) {
		if (status.equals("Active"))
			setState("Published");
		if (status.equals("Inactive"))
			setState("Draft");
		if (status.equals("All"))
			setState("All");
		if (status.equals("Pending"))
			setState("Draft");
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
		if ("".equalsIgnoreCase(status)) {
			status = null;
		}
		// Item label is 'All' but value is empty string
		if ("".equalsIgnoreCase(critical)) {
			critical = null;
		}
		// Item label is 'All' but value is empty string
		if ("".equals(level)) {
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
	}

	public String loadCmqBaseByCode() {
		this.selctedData = this.cmqBaseService.findByCode(code);

		return "";
	}

	public String hierarchySearch() {
		int level = 0;
		String meddraSearchTerm = null;
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
			meddraSearchTerm = "SOC_TERM";
			searchMeddraBase = true;
		} else if ("HLGT".equalsIgnoreCase(levelH)) {
			meddraSearchTerm = "HLGT_TERM";
			searchMeddraBase = true;
		} else if ("HLT".equalsIgnoreCase(levelH)) {
			meddraSearchTerm = "HLT_TERM";
			searchMeddraBase = true;
		} else if ("PT".equalsIgnoreCase(levelH)) {
			meddraSearchTerm = "PT_TERM";
			searchMeddraBase = true;
		} else if ("PRO".equalsIgnoreCase(levelH)) {
			searchCmqBase = true;
		}

		if (searchSmqBase) {
			List<SmqBase190> smqBaseList = smqBaseService.findByLevelAndTerm(
					level, termName);
			log.info("smqBaseList values {}", smqBaseList == null ? 0
					: smqBaseList.size());
			hierarchySearchResults = new ArrayList<>();

			this.hierarchyRoot = new DefaultTreeNode("root", new HierarchyNode(
					"LEVEL", "NAME", "CODE", null), null);

			for (SmqBase190 smqBase : smqBaseList) {
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
					/*
					 * List<SmqRelation190> childRelations =
					 * this.smqBaseService.findSmqRelationsForSmqCode(
					 * childSmqBase.getSmqCode()); if(null != childRelations) {
					 * for (SmqRelation190 childRelation : childRelations) {
					 * HierarchyNode childRelationNode = new HierarchyNode(); if
					 * (childRelation.getSmqLevel() == 1) {
					 * childRelationNode.setLevel("SMQ1"); } else if
					 * (childRelation.getSmqLevel() == 2) {
					 * childRelationNode.setLevel("SMQ2"); } else if
					 * (childRelation.getSmqLevel() == 3) {
					 * childRelationNode.setLevel("SMQ3"); } else if
					 * ((childRelation.getSmqLevel() == 4) ||
					 * (childRelation.getSmqLevel() == 0) ||
					 * (childRelation.getSmqLevel() == 5)) {
					 * childRelationNode.setLevel("PT"); }
					 * childRelationNode.setTerm(childRelation.getPtName());
					 * childRelationNode.setCode(childRelation.getPtCode().
					 * toString()); childRelationNode.setEntity(childRelation);
					 * 
					 * TreeNode childRelationTreeNode = new
					 * DefaultTreeNode(childNode, childTreeNode); } }
					 */
				} // end of for (SmqBase190 childSmqBase : childSmqBaseList)
			} // end of for (SmqBase190 smqBase : smqBaseList)
		} else if (searchMeddraBase) {
			List<MeddraDict190> meddraDictList = meddraDictService
					.findByLevelAndTerm(meddraSearchTerm.toUpperCase(),
							termName);
			hierarchySearchResults = new ArrayList<>();
			for (MeddraDict190 meddraDict190 : meddraDictList) {
				HierarchySearchResultBean bean = new HierarchySearchResultBean();
				bean.setLevel(levelH);
				if ("SOC".equalsIgnoreCase(levelH)) {
					bean.setTerm(meddraDict190.getSocTerm());
					bean.setCode(meddraDict190.getSocCode());
				} else if ("HLGT".equalsIgnoreCase(levelH)) {
					bean.setTerm(meddraDict190.getHlgtTerm());
					bean.setCode(meddraDict190.getHlgtCode());
				} else if ("HLT".equalsIgnoreCase(levelH)) {
					bean.setTerm(meddraDict190.getHltTerm());
					bean.setCode(meddraDict190.getHltCode());
				} else if ("PT".equalsIgnoreCase(levelH)) {
					bean.setTerm(meddraDict190.getPtTerm());
					bean.setCode(meddraDict190.getPtCode());
				}
				bean.setEntity(meddraDict190);
				hierarchySearchResults.add(bean);

			}
		} /*
		 * else if (searchCmqBase) { List<CmqBase190> cmqBaseList =
		 * cmqBaseService.findByLevelAndTerm(2, termName);
		 * hierarchySearchResults = new ArrayList<>(); for (CmqBase190
		 * cmqBase190 : cmqBaseList) { HierarchySearchResultBean bean = new
		 * HierarchySearchResultBean(); bean.setLevel(levelH);
		 * bean.setTerm(cmqBase190.getCmqName());
		 * bean.setCode(cmqBase190.getCmqCode().toString());
		 * bean.setEntity(cmqBase190); hierarchySearchResults.add(bean);
		 * 
		 * TreeNode level1 = new DefaultTreeNode( new HierarchyNode(levelH,
		 * cmqBase190.getCmqName(), cmqBase190.getCmqCode().toString()),
		 * hierarchyRoot);
		 * 
		 * // if (cmqBase190.getChildCmqs() != null // &&
		 * cmqBase190.getChildCmqs().size() > 0) { // for (CmqBase190 child :
		 * cmqBase190.getChildCmqs()) { // TreeNode level2 = new
		 * DefaultTreeNode( // new HierarchyNode(levelH, child.getCmqName(), //
		 * child.getCmqCode().toString()), level1); // } // } } }
		 */
		return "";
	}

	public void onNodeExpand(NodeExpandEvent event) {
		log.info("Expand called finally....");
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
			}
			hierarchyNode.setDataFetchCompleted(true);
		}
	}
	
	/**
	 * Add the selected hierarchy details to the relation list.
	 */
	public void addSelectedToRelation(TreeNode[] nodes) {
		if (nodes != null && nodes.length > 0) {
			//testing one node
			relationsRoot = nodes[0];
 
			setRelationSelected(nodes); 

			FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_INFO,
					"Relations selected","");
			FacesContext.getCurrentInstance().addMessage(null, message);
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

	public void setRelationsRoot(TreeNode relationsRoot) {
		this.relationsRoot = relationsRoot;
	}
}
