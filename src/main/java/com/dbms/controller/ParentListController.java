package com.dbms.controller;


import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.ViewScoped;
import javax.faces.event.AjaxBehaviorEvent;

import org.primefaces.event.NodeExpandEvent;
import org.primefaces.model.DefaultTreeNode;
import org.primefaces.model.TreeNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dbms.csmq.HierarchyNode;
import com.dbms.entity.cqt.CmqBase190;
import com.dbms.service.ICmqBase190Service;
import com.dbms.service.ICmqRelation190Service;
import com.dbms.service.IMeddraDictService;
import com.dbms.service.IRefCodeListService;
import com.dbms.service.ISmqBaseService;
import com.dbms.view.CmqBaseRelationsTreeHelper;

/**
 * @date Feb 7, 2017 7:39:34 AM
 **/
@ManagedBean
@ViewScoped
public class ParentListController extends BaseController<CmqBase190> {

	private static final long serialVersionUID = 52993434344651662L;

	private static final Logger log = LoggerFactory.getLogger(ParentListController.class);

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
	
	@ManagedProperty("#{globalController}")
    private GlobalController globalController;
	
	// Search & Filters
	private String searchTermName;
	private String searchExtension;
	private String searchLevelH;
	
	// data
	private Long childCmqCode;
	private CmqBase190 childCmqEntity;
	private Long parentCmqCode;
	private CmqBase190 parentCmqEntity;

	private TreeNode parentRelationsRoot;

	public ParentListController() {
	}

	@PostConstruct
	public void init() {
		parentRelationsRoot = new DefaultTreeNode("root", new HierarchyNode("LEVEL", "NAME", "CODE", null), null);
	}
	
	/**
	 * Reset search on every extension change.
	 * 
	 * @param event
	 */
	public void resetSearch(AjaxBehaviorEvent event) {
		this.searchExtension = "";
		this.searchTermName = "";
	}
	
	@Override
	String search() {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * Node Expanding Event of Relation Tree
	 * @param event
	 */
	public void onNodeExpand(NodeExpandEvent event) {
		CmqBaseRelationsTreeHelper relationsSearchHelper = new CmqBaseRelationsTreeHelper(cmqBaseService, smqBaseService, meddraDictService, cmqRelationService, globalController);	
		this.parentRelationsRoot = relationsSearchHelper.getRelationsNodeHierarchy(this.parentRelationsRoot, event.getTreeNode()); 
	}
	
	//-------------------------- Getters and Setters -------------------------------
	
	public TreeNode getParentRelationsRoot() {
		return parentRelationsRoot;
	}

	public ICmqBase190Service getCmqBaseService() {
		return cmqBaseService;
	}

	public void setCmqBaseService(ICmqBase190Service cmqBaseService) {
		this.cmqBaseService = cmqBaseService;
	}

	public ICmqRelation190Service getCmqRelationService() {
		return cmqRelationService;
	}

	public void setCmqRelationService(ICmqRelation190Service cmqRelationService) {
		this.cmqRelationService = cmqRelationService;
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

	public IRefCodeListService getRefCodeListService() {
		return refCodeListService;
	}

	public void setMeddraDictService(IMeddraDictService meddraDictService) {
		this.meddraDictService = meddraDictService;
	}
	
	public void setRefCodeListService(IRefCodeListService refCodeListService) {
		this.refCodeListService = refCodeListService;
	}
	
	public CmqBase190 getChildCmqEntity() {
		return childCmqEntity;
	}
	
	public void setChildCmqEntity(CmqBase190 childCmqEntity) {
		this.childCmqEntity = childCmqEntity;
		this.childCmqCode = this.childCmqEntity.getCmqCode();
		this.parentCmqCode = this.childCmqEntity.getCmqParentCode();
		this.parentCmqEntity = this.cmqBaseService.findByCode(this.parentCmqCode);

		CmqBaseRelationsTreeHelper relationsTreeHelper = new CmqBaseRelationsTreeHelper(cmqBaseService, smqBaseService, meddraDictService, cmqRelationService, globalController);
        relationsTreeHelper.setRequireDrillDown(false);
		parentRelationsRoot = relationsTreeHelper.getCmqBaseRelationsRootHierarchy(parentCmqCode);
	}
	
	public Long getParentCmqCode() {
		return this.parentCmqCode;
	}
	
	public CmqBase190 getParentCmqEntity() {
		return this.parentCmqEntity;
	}

	public GlobalController getGlobalController() {
		return globalController;
	}

	public void setGlobalController(GlobalController globalController) {
		this.globalController = globalController;
	}
}
