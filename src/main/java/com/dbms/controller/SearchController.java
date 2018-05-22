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

import org.apache.commons.lang3.StringUtils;
import org.primefaces.event.RowEditEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dbms.controller.beans.HierarchySearchResultBean;
import com.dbms.entity.cqt.CmqBase190;
import com.dbms.entity.cqt.CreateEntity;
import com.dbms.entity.cqt.RefConfigCodeList;
import com.dbms.service.AuthenticationService;
import com.dbms.service.ICmqBase190Service;
import com.dbms.service.ICmqRelation190Service;
import com.dbms.service.IMeddraDictService;
import com.dbms.service.IRefCodeListService;
import com.dbms.service.ISmqBaseService;
import com.dbms.view.CmqBaseHierarchySearchVM;
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

	@ManagedProperty("#{AuthenticationService}")
	private AuthenticationService authService;
	
	@ManagedProperty("#{globalController}")
    private GlobalController globalController;
	
	private String releaseStatus;
	private String criticalEvent;
	private String termName;

	private String code;

	private String myFltExtension;
	private String myFltDrugProgram;
	private String myFltProtocol;
	private String myFltState;
	private Integer myFltLevel;
	private String myFltStatus;
	private String critical;
	private String scope;
	private String product;
	private String[] myFltProducts;
	private String myFltGroup;
	private String history;
	private String codelist;
    
    private String[] myFltDesignees;

	private boolean maintainDesigBtn;
	private boolean dataModified = false;
	
	private List<CreateEntity> values, vals;

	private List<CodelistDTO> admins;

	private String[] selectedSOCs;
	private CmqBase190 selctedData;

	private List<HierarchySearchResultBean> hierarchySearchResults;

	private CmqBaseHierarchySearchVM myHierarchyDlgModel;
	private boolean disabledField;
	
	private String dictionaryVersion;


	public CmqBaseHierarchySearchVM getHierarchyDlgModel() {
		return myHierarchyDlgModel;
	}

	public void setHierarchyDlgModel(CmqBaseHierarchySearchVM hierSearchFormModel) {
		this.myHierarchyDlgModel = hierSearchFormModel;
	}

	public SearchController() {
		this.selectedData = new CmqBase190();
	}

	@PostConstruct
	public void init() {
		myHierarchyDlgModel = new CmqBaseHierarchySearchVM(cmqBaseService, smqBaseService, meddraDictService, cmqRelationService, globalController);
		this.maintainDesigBtn = false;
		resetSearch();
		
		//Dictionary version
		RefConfigCodeList currentMeddraVersionCodeList = this.refCodeListService.getCurrentMeddraVersion();
		setDictionaryVersion(currentMeddraVersionCodeList.getValue());
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

	public void changeLevel(AjaxBehaviorEvent event) {
		changeLevel();
	}
	
	public int listNameSort(String s1, String s2){
		return s1.toUpperCase().compareTo(s2.toUpperCase());	
	}
	

	public void changeLevel() {
		if(StringUtils.isBlank(this.myFltExtension)) {
			setLevel(null);
		} else if (this.myFltExtension.equals("PRO")) {
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
		this.myFltExtension = "";
		this.myFltState = "";
		this.myFltStatus = "";
		this.myFltLevel = null;
		// this.critical = "No";
		this.myFltGroup = "No Group";
		this.product = "";
		this.myFltProtocol = "";
		this.myFltDrugProgram = "";
		this.termName = "";
		this.code = null;
        myFltDesignees = new String[0];
        myFltProducts = new String[0];
        
		this.dataModified = false;
		
		myHierarchyDlgModel.resetForm();
		
		
		 /**
         * Restrictions on users from  REQUESTOR and ADMIN groups
         */
		List<String> userGroupList = authService.getCmqMappedGroupMemberships();
		if (userGroupList != null &&
                (!userGroupList.contains(AuthenticationService.REQUESTER_GROUP)
                    && !userGroupList.contains(AuthenticationService.ADMIN_GROUP)
                    && !userGroupList.contains(AuthenticationService.MQM_GROUP)
                    && !userGroupList.contains(AuthenticationService.MANAGER_GROUP))) {
			this.myFltState = "PUBLISHED";
    		this.myFltStatus = "A";
        	
        	disabledField = true;
		}
	}

	/**
	 * Method to change State value on status selection.
	 * 
	 * @param event
	 *            AjaxBehaviour
	 */
	public void changeState(AjaxBehaviorEvent event) {		
		if ("P".equalsIgnoreCase(myFltStatus))
			setState("DRAFT");
		else if ("A".equalsIgnoreCase(myFltStatus))
			setState("PUBLISHED");
		else if ("I".equalsIgnoreCase(myFltStatus))
			setState("PUBLISHED");
		else if("".equals(myFltStatus))
			setState("");
	}

	public String getExtension() {
		return myFltExtension;
	}

	public void setExtension(String extension) {
		this.myFltExtension = extension;
	}

	public String getDrugProgram() {
		return myFltDrugProgram;
	}

	public void setDrugProgram(String drugProgram) {
		this.myFltDrugProgram = drugProgram;
	}

	public String getProtocol() {
		return myFltProtocol;
	}

	public void setProtocol(String protocol) {
		this.myFltProtocol = protocol;
	}

	public String getReleaseStatus() {
		return releaseStatus;
	}

	public void setReleaseStatus(String releaseStatus) {
		this.releaseStatus = releaseStatus;
	}

	public Integer getLevel() {
		return myFltLevel;
	}

	public void setLevel(Integer level) {
		this.myFltLevel = level;
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
		FacesContext.getCurrentInstance().addMessage(null, new FacesMessage("Canceled", "ZZ"));
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
		return myFltStatus;
	}

	public void setStatus(String status) {
		this.myFltStatus = status;
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
		return myFltGroup;
	}

	public void setGroup(String group) {
		this.myFltGroup = group;
	}

	public String getTermName() {
		return termName;
	}

	public void setTermName(String termName) {
		this.termName = termName;
	}
	
	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getState() {
		return myFltState;
	}

	public void setState(String state) {
		this.myFltState = state;
	}

	public String getHistory() {
		return history;
	}

	public void setHistory(String history) {
		this.history = history;
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
		log.debug("search by{}", myFltExtension);

		dataModified = false;
		// Item label is 'All' but value is empty string
		if (StringUtils.isBlank(myFltStatus)) {
			myFltStatus = null;
		}
		
		// Item label is 'All' but value is empty string
		if (StringUtils.isBlank(critical)) {
			critical = null;
		}
		
		// Item label is 'All' but value is empty string
		if ((null != myFltLevel) && (myFltLevel.intValue() <= 0)) {
			myFltLevel = null;
		}
		
		// Item label is 'All' but value is empty string
		if ("".equalsIgnoreCase(myFltGroup)) {
			myFltGroup = null;
		}

		if (StringUtils.isBlank(code)) {
			code = null;
		}

		datas = cmqBaseService.findByCriterias(myFltExtension, myFltDrugProgram,
				myFltProtocol, myFltProducts, myFltLevel, myFltStatus, myFltState, critical, myFltGroup,
				termName, code, myFltDesignees);
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
        try {
            Long codeVal = Long.valueOf(code);
            this.selctedData = this.cmqBaseService.findByCode(codeVal);
        } catch (NumberFormatException e) {
            return "";
        }
		return "";
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

	public void setRefCodeListService(IRefCodeListService refCodeListService) {
		this.refCodeListService = refCodeListService;
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

	public String[] getProducts() {
		return myFltProducts;
	}

	public void setProducts(String[] products) {
		this.myFltProducts = products;
	}
	
	public String getProductsLabel() {
		if(this.myFltProducts != null && this.myFltProducts.length != 0 && this.refCodeListService != null) {
			return refCodeListService.interpretProductCodesToValuesLabel(this.myFltProducts);
		}
		return "Choose products";
	}

	public AuthenticationService getAuthService() {
		return authService;
	}

	public void setAuthService(AuthenticationService authService) {
		this.authService = authService;
	}
    
    public String[] getFltDesignees() {
        return myFltDesignees;
    }

    public void setFltDesignees(String[] myFilterDesignees) {
        this.myFltDesignees = myFilterDesignees;
    }

	public boolean isDisabledField() {
		return disabledField;
	}

	public void setDisabledField(boolean disabledField) {
		this.disabledField = disabledField;
	}

	public String getDictionaryVersion() {
		return dictionaryVersion;
	}

	public void setDictionaryVersion(String dictionaryVersion) {
		this.dictionaryVersion = dictionaryVersion;
	}

	public GlobalController getGlobalController() {
		return globalController;
	}

	public void setGlobalController(GlobalController globalController) {
		this.globalController = globalController;
	}
}
