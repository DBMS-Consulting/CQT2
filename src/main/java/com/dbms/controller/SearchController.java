package com.dbms.controller;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
import javax.faces.event.AjaxBehaviorEvent;

import org.primefaces.component.wizard.Wizard;
import org.primefaces.event.RowEditEvent;
import org.primefaces.model.DefaultTreeNode;
import org.primefaces.model.TreeNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dbms.entity.cqt.CmqBase190;
import com.dbms.entity.cqt.CmqRelation190;
import com.dbms.entity.cqt.CreateEntity;
import com.dbms.service.CmqBase190Service;
import com.dbms.service.CmqRelation190Service;
import com.dbms.web.dto.AdminDTO;

/**
 * @date Feb 7, 2017 7:39:34 AM
 **/
@ManagedBean
@ViewScoped
public class SearchController extends BaseController<CmqBase190> {

	private static final long serialVersionUID = 5299394344651669792L;

	private static final Logger log = LoggerFactory
			.getLogger(SearchController.class);

	@ManagedProperty("#{cmqBase190Service}")
	private CmqBase190Service cmqBaseService;
	@ManagedProperty("#{cmqRelation190Service}")
	private CmqRelation190Service cmqRelationService;

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

	private boolean maintainDesigBtn;

	private List<CreateEntity> values, vals;

	private List<AdminDTO> admins;

	private Wizard updateWizard, copyWizard, browseWizard;

	private String[] selectedSOCs;

	public SearchController() {
		this.selectedData = new CmqBase190();
	}

	@PostConstruct
	public void init() {
		maintainDesigBtn = false;
		status = "Active";
		state = "Published";
		level = 1;
		critical = "No";
		group = "No Group";
		extension = "TME";

		// TODO To test UI of ADMIN MODULE - Will be removed
		initValuesForAdmin();
	}

	private void initValuesForAdmin() {
		admins = new ArrayList<AdminDTO>();
		AdminDTO c = new AdminDTO();
		c.setSequence("SEQ 1");
		c.setName("AZERTY");
		c.setActive(true);
		admins.add(c);

		c = new AdminDTO();
		c.setSequence("SEQ 2");
		c.setName("QWERTY");
		c.setActive(true);
		admins.add(c);

		c = new AdminDTO();
		c.setSequence("SEQ 31");
		c.setName("ABCDEFG");
		c.setActive(false);
		admins.add(c);

		vals = new ArrayList<CreateEntity>();
		CreateEntity ce = new CreateEntity();
		ce.setCode(122);
		vals.add(ce);

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

		if (extension.equals("CPT") || extension.equals("DME"))
			setDrugProgram("No Program");
		else
			setDrugProgram("");

		if (extension.equals("CPT") || extension.equals("DME")
				|| extension.equals("TME") || extension.equals("TR1"))
			setProtocol("No Protocol");
		else
			setProtocol("");

		if (extension.equals("CPT") || extension.equals("DME"))
			setProduct("No Product");
		else
			setProduct("");
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

	public List<AdminDTO> getAdmins() {
		return admins;
	}

	public void setAdmins(List<AdminDTO> admins) {
		this.admins = admins;
	}

	public List<CreateEntity> getVals() {
		return vals;
	}

	public void setVals(List<CreateEntity> vals) {
		this.vals = vals;
	}

	public CmqBase190Service getCmqBaseService() {
		return cmqBaseService;
	}

	public void setCmqBaseService(CmqBase190Service cmqBaseService) {
		this.cmqBaseService = cmqBaseService;
	}

	public void setCmqRelationService(CmqRelation190Service cmqRelationService) {
		this.cmqRelationService = cmqRelationService;
	}

	public void search() {
		log.debug("search by{}", extension);
		datas = cmqBaseService.findByCriterias(extension, drugProgram,
				protocol, product, level, status, state, criticalEvent, group,
				termName, code);
		log.debug("found values {}", datas == null ? 0 : datas.size());
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
		if (relation.getChildren() != null && !relation.getChildren().isEmpty()) {
			for (CmqRelation190 r : relation.getChildren()) {
				TreeNode node = new DefaultTreeNode(new RelationTreeNode(
						r.getId(), r.getTermName(), r.getCmqLevel(),
						r.getPtTermScope(), r.getPtTermCategory(),
						r.getPtTermWeight(), false), root);
				addTreeNode(r, node);
			}
		}
	}

	public TreeNode getRoot() {
		TreeNode root = new DefaultTreeNode(new RelationTreeNode(null, "ROOT",
				null, null, null, null, true), null);
		TreeNode first = new DefaultTreeNode(new RelationTreeNode(
				selectedData.getId(), selectedData.getName(),
				selectedData.getLevel(), selectedData.getScope(), null, null,
				true), root);
		first.setExpanded(true);
		if (selectedData.getRelations() != null) {
			for (CmqRelation190 relation : selectedData.getRelations()) {
				addTreeNode(relation, first);
			}
		}
		return root;
	}

	public void onRowEdit(RowEditEvent event) {
		TreeNode node = (DefaultTreeNode) event.getObject();
		RelationTreeNode relationNode = (RelationTreeNode) node.getData();
		log.debug("update tree node scope#{},category#{},weigth#{}",
				relationNode.getScope(), relationNode.getCategory(),
				relationNode.getWeight());
		try {
			if (relationNode.getRoot()) {
				CmqBase190 base = cmqBaseService.findById(relationNode
						.getCode());
				base.setScope(relationNode.getScope());
				cmqBaseService.update(base);
			} else {
				CmqRelation190 relation = cmqRelationService
						.findById(relationNode.getCode());
				relation.setPtTermScope(relationNode.getScope());
				relation.setPtTermCategory(relationNode.getCategory());
				relation.setPtTermWeight(relationNode.getWeight());
				cmqRelationService.update(relation);
			}
		} catch (Exception e) {
			log.error("Error when update tree!", e);
		}
	}

	/******
	 * 
	 * 
	 * 
	 * 
	 * Dummy test on the ADMIN MODULE - To REMOVE if we have a service TODO
	 * 
	 * 
	 */
	public void addDrugProgram() {
		admins.add(new AdminDTO());
	}

	public void cancelDrugProgram() {
		for (AdminDTO adminDTO : admins) {
			if (adminDTO.getName() == null)
				admins.remove(adminDTO);
		}
	}

	public String[] getSelectedSOCs() {
		return selectedSOCs;
	}

	public void setSelectedSOCs(String[] selectedSOCs) {
		this.selectedSOCs = selectedSOCs;
	}
}
