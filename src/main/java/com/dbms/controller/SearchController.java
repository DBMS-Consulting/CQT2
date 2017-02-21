package com.dbms.controller;

import java.util.List;

import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.SessionScoped;
import javax.faces.context.FacesContext;
import javax.faces.event.AjaxBehaviorEvent;

import org.primefaces.component.wizard.Wizard;
import org.primefaces.event.RowEditEvent;
import org.primefaces.model.DefaultTreeNode;
import org.primefaces.model.TreeNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dbms.csmq.Document;
import com.dbms.entity.cqt.CmqBase190;
import com.dbms.entity.cqt.CmqRelation190;
import com.dbms.entity.cqt.CreateEntity;
import com.dbms.service.ICmqBase190Service;
import com.dbms.service.ICmqRelation190Service;
import com.dbms.web.dto.CodelistDTO;

/**
 * @date Feb 7, 2017 7:39:34 AM
 **/
@ManagedBean
@SessionScoped
public class SearchController extends BaseController<CmqBase190> {

	private static final long serialVersionUID = 5299394344651669792L;

	private static final Logger log = LoggerFactory.getLogger(SearchController.class);

	@ManagedProperty("#{CmqBase190Service}")
	private ICmqBase190Service cmqBaseService;

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

	public SearchController() {
		this.selectedData = new CmqBase190();
	}

	@PostConstruct
	public void init() {
		this.maintainDesigBtn = false;
		this.state = "Draft";
		this.status = "Pending";
		this.level = 1;
		this.critical = "No";
		this.group = "No Group";
		this.extension = "TME";
	}

	public TreeNode getHierarchyRoot() {
		hierarchyRoot = new DefaultTreeNode(new Document("SOC", "-", "CODE_1"), null);

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

		if (extension.equals("CPT") || extension.equals("DME") || extension.equals("TME") || extension.equals("TR1"))
			setProtocol("No Protocol");
		else
			setProtocol("");

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
		if ("All".equalsIgnoreCase(status)) {
			status = null;
		}
		if ("All".equalsIgnoreCase(critical)) {
			critical = null;
		}
		if (-1 == level) {
			level = null;
		}
		if ("All".equalsIgnoreCase(group)) {
			group = null;
		}
		
		if(code != null && code == 0) {
			code = null;
		}
		
		datas = cmqBaseService.findByCriterias(extension, drugProgram, protocol, product, level, status, state,
				critical, group, termName, code);
		log.debug("found values {}", datas == null ? 0 : datas.size());
	}

	public String hierarchySearch() {
		int level = 0;
		if ("SMQ1".equalsIgnoreCase(levelH)) {
			level = 1;
		} else if ("SMQ2".equalsIgnoreCase(levelH)) {
			level = 2;
		} else if ("SMQ3".equalsIgnoreCase(levelH)) {
			level = 3;
		} else if ("SMQ4".equalsIgnoreCase(levelH)) {
			level = 4;
		} else if ("SMQ5".equalsIgnoreCase(levelH)) {
			level = 5;
		} else if ("SMQ2".equalsIgnoreCase(levelH)) {
			level = 1;
		} 
		datas = cmqBaseService.findByCriterias(null, null, null, null, level, null, null, null, null, termName, null);
		return "";
	}

	public void saveDetails() {
		log.debug("save cmq details ... ");
		try {
			cmqBaseService.update(selectedData);
			FacesMessage msg = new FacesMessage("Successful save a CMQ", null);
			FacesContext.getCurrentInstance().addMessage(null, msg);
		} catch (Exception e) {
			log.error("Error when sae cmq - {}", e.getMessage(), e);
			FacesMessage msg = new FacesMessage("Failed - " + e.getMessage(), null);
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
			FacesMessage msg = new FacesMessage("Failed - " + e.getMessage(), null);
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
}
