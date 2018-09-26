package com.dbms.controller;

import com.dbms.entity.cqt.CmqBase190;
import com.dbms.entity.cqt.CmqBaseTarget;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;

import com.dbms.entity.cqt.RefConfigCodeList;
import com.dbms.service.ICqtCacheManager;
import com.dbms.service.IRefCodeListService;
import com.dbms.util.CqtConstants;
import com.dbms.util.OrderBy;

import java.util.Arrays;

import javax.swing.event.ListSelectionEvent;

import org.apache.commons.lang3.StringUtils;

/**
 * Controller that will contains codelist methods.
 * 
 * @author
 *
 */
@ManagedBean(name = "configMB")
@ApplicationScoped
public class ConfigurationController implements Serializable {

	/**
	 * serialVersionUID.
	 */
	private static final long serialVersionUID = 5539162862901321913L;

	@ManagedProperty("#{RefCodeListService}")
	private IRefCodeListService refCodeListService;

	private List<RefConfigCodeList> extensions, programs, protocols, products, workflows, levels, timezones;

	private RefConfigCodeList currentMeddraVersionCodeList;
	private RefConfigCodeList timezone;
	
	private String dictionaryName;
    
    private List<String[]> cmqBaseListStates = null;
    private List<String[]> cmqTargetListStates = null;
    
    @ManagedProperty("#{CqtCacheManager}")
   	private ICqtCacheManager cqtCacheManager;
    
    @ManagedProperty("#{globalController}")
    private GlobalController globalController;
    
	private final String CACHE_NAME = "code-list-cache";

	
	@PostConstruct
	public void init() {
		dictionaryName = "MEDDRA";
		
		List<RefConfigCodeList> defaultTZ = getTimezoneList();
		for(RefConfigCodeList tz : defaultTZ) {
			if(tz.getDefaultFlag().equalsIgnoreCase("Y"))
				setTimezone(tz);
		}
//		getExtensionList();
//		getProgramList();
//		getProtocolList();
//		getProductList();
//		getCurrentMeddraVersion();
//		getWorkflowStateList();
	}

	/**
	 * Returns extensions list.
	 * 
	 * @return
	 */
	public List<RefConfigCodeList> getExtensionList() {
		extensions = refCodeListService.findByConfigType(
				CqtConstants.CODE_LIST_TYPE_EXTENSION, OrderBy.ASC);
		if (extensions == null) {
			extensions = new ArrayList<>();
		}
		return extensions;
	}

	/**
	 * Returns programs list.
	 * 
	 * @return
	 */
	public List<RefConfigCodeList> getProgramList() {
		programs = refCodeListService.findByConfigType(
				CqtConstants.CODE_LIST_TYPE_PROGRAM, OrderBy.ASC);
		if (programs == null) {
			programs = new ArrayList<>();
		}
		return programs;
	}
	
	/**
	 * Returns protocol list.
	 * 
	 * @return
	 */
	public List<RefConfigCodeList> getProtocolList() {
		protocols = refCodeListService.findByConfigType(
				CqtConstants.CODE_LIST_TYPE_PROTOCOL, OrderBy.ASC);
		if (protocols == null) {
			protocols = new ArrayList<>();
		}
		return protocols;
	}
	
	/**
	 * Returns products list.
	 * 
	 * @return
	 */
	public List<RefConfigCodeList> getProductList() {
		products = refCodeListService.findByConfigType(
				CqtConstants.CODE_LIST_TYPE_PRODUCT, OrderBy.ASC);
		if (products == null) {
			products = new ArrayList<>();
		}
		return products;
	}
	
	/**
	 * Returns worflow states.
	 * 
	 * @return
	 */
	public List<RefConfigCodeList> getWorkflowStateList() {
		workflows = refCodeListService.findByConfigType(
				CqtConstants.CODE_LIST_TYPE_WORKFLOW_STATES, OrderBy.ASC);
		if (workflows == null) {
			workflows = new ArrayList<>();
		}
		return workflows;
	}

	
	/**
	 * Returns levels list.
	 * 
	 * @return
	 */
	public List<RefConfigCodeList> getLevelList() {
		cqtCacheManager.removeAllFromCache(CACHE_NAME);

		products = refCodeListService.findByConfigType(
				CqtConstants.CODE_LIST_TYPE_DICTIONARY_LEVELS, OrderBy.ASC);
		
	 
		RefConfigCodeList levelToRemove = refCodeListService.findByConfigTypeAndInternalCode(CqtConstants.CODE_LIST_TYPE_DICTIONARY_LEVELS, "NC-LLT");
		
		if (levelToRemove != null)
			products.remove(levelToRemove);
		
		if (products == null) {
			products = new ArrayList<>();
		}
		return products;
	}
	
	/**
	 * Returns levels list.
	 * 
	 * @return
	 */
	public List<RefConfigCodeList> getAllLevelList() {
		cqtCacheManager.removeAllFromCache(CACHE_NAME);

		List<RefConfigCodeList> levels = refCodeListService.findByConfigType(
				CqtConstants.CODE_LIST_TYPE_DICTIONARY_LEVELS, OrderBy.ASC);
		
	  
		if (levels == null) {
			levels = new ArrayList<>();
		}
		
		//filter out llts if the flag is set
		boolean filterLltFlag = this.globalController.isFilterLltsFlag();
		if(filterLltFlag) {
			for(ListIterator<RefConfigCodeList> li = levels.listIterator(); li.hasNext();) {
				RefConfigCodeList refConfigCodeList = li.next();
				if(StringUtils.isNotBlank(refConfigCodeList.getCodelistInternalValue()) 
						&& (StringUtils.containsIgnoreCase(refConfigCodeList.getCodelistInternalValue(), "LLT")
								|| StringUtils.containsIgnoreCase(refConfigCodeList.getCodelistInternalValue(), "SMQ5"))) {
					li.remove();
				}
			}
		}
		return levels;
	}
	
	/**
	 * Returns extensions list.
	 * 
	 * @return
	 */
	public List<RefConfigCodeList> getTimezoneList() {
		timezones = refCodeListService.findByConfigType(
				"USER_TIMEZONE", OrderBy.ASC);
		if (timezones == null) {
			timezones = new ArrayList<>();
		}
		return timezones;
	}
	
	
	public RefConfigCodeList getCurrentMeddraVersion() {
		this.currentMeddraVersionCodeList = refCodeListService.getCurrentMeddraVersion();
		return this.currentMeddraVersionCodeList;
	}
	
	public RefConfigCodeList getTargetMeddraVersion() {
		this.currentMeddraVersionCodeList = refCodeListService.getTargetMeddraVersion();
		return this.currentMeddraVersionCodeList;
	}
	
	
	
	public List<RefConfigCodeList> getAllMeddraVersion() {
		
		List<RefConfigCodeList> list = new ArrayList<>();
		list = refCodeListService.getMeddraVersions();
		
		
		/*
		list.add(this.getCurrentMeddraVersion());
		list.add(this.getTargetMeddraVersion());
		*/
		
		return list;
		
	}
	
	public List<RefConfigCodeList> getCodelistTypes(){
		List<RefConfigCodeList> list = new ArrayList<>();
		list = refCodeListService.getAllCodelistTypes();
		return list;
	}
	
	public IRefCodeListService getRefCodeListService() {
		return refCodeListService;
	}

	public void setRefCodeListService(IRefCodeListService refCodeListService) {
		this.refCodeListService = refCodeListService;
	}

	public List<RefConfigCodeList> getPrograms() {
		return programs;
	}

	public void setPrograms(List<RefConfigCodeList> programs) {
		this.programs = programs;
	}

	public List<RefConfigCodeList> getProtocols() {
		return protocols;
	}

	public void setProtocols(List<RefConfigCodeList> protocols) {
		this.protocols = protocols;
	}

	public List<RefConfigCodeList> getProducts() {
		return products;
	}

	public void setProducts(List<RefConfigCodeList> products) {
		this.products = products;
	}

	public List<RefConfigCodeList> getExtensions() {
		return extensions;
	}

	public void setExtensions(List<RefConfigCodeList> extensions) {
		this.extensions = extensions;
	}

	public String getDictionaryName() {
		return dictionaryName;
	}

	public void setDictionaryName(String dictionaryName) {
		this.dictionaryName = dictionaryName;
	}

	public List<RefConfigCodeList> getWorkflows() {
		return workflows;
	}

	public void setWorkflows(List<RefConfigCodeList> workflows) {
		this.workflows = workflows;
	}
	
	public List<RefConfigCodeList> getTimezones(){
		return timezones;
	}
	
	public void setTimezones(List<RefConfigCodeList> timezones) {
		this.timezones = timezones;
	}
	
	public RefConfigCodeList getTimezone(){
		return timezone;
	}
	
	public void setTimezone(RefConfigCodeList timezone) {
		this.timezone = timezone;
	}
    
    public List<String[]> getCmqBaseListStates() {
        if(cmqBaseListStates == null) {
            cmqBaseListStates = Arrays.asList(
                new String[] { CmqBase190.CMQ_STATE_VALUE_DRAFT, CmqBase190.CMQ_STATE_VALUE_DRAFT },
                new String[] { CmqBase190.CMQ_STATE_VALUE_REVIEWED, CmqBase190.CMQ_STATE_VALUE_REVIEWED },
                new String[] { CmqBase190.CMQ_STATE_VALUE_APPROVED, CmqBase190.CMQ_STATE_VALUE_APPROVED },
                new String[] { CmqBase190.CMQ_STATE_VALUE_PUBLISHED, CmqBase190.CMQ_STATE_VALUE_PUBLISHED }
            );
        }
        return cmqBaseListStates;
    }
    
    public List<String[]> getCmqTargetListStates() {
        if(cmqTargetListStates == null) {
            cmqTargetListStates = Arrays.asList(
                new String[] { CmqBaseTarget.CMQ_STATE_PENDING_IA, CmqBaseTarget.CMQ_STATE_PENDING_IA },
                new String[] { CmqBaseTarget.CMQ_STATE_REVIEWED_IA, CmqBaseTarget.CMQ_STATE_REVIEWED_IA },
                new String[] { CmqBaseTarget.CMQ_STATE_APPROVED_IA, CmqBaseTarget.CMQ_STATE_APPROVED_IA },
                new String[] { CmqBaseTarget.CMQ_STATE_PUBLISHED_IA, CmqBaseTarget.CMQ_STATE_PUBLISHED_IA }
            );
        }
        return cmqTargetListStates;
    }

	public List<RefConfigCodeList> getLevels() {
		return levels;
	}

	public void setLevels(List<RefConfigCodeList> levels) {
		this.levels = levels;
	}
	
	public ICqtCacheManager getCqtCacheManager() {
		return cqtCacheManager;
	}

	public void setCqtCacheManager(ICqtCacheManager cqtCacheManager) {
		this.cqtCacheManager = cqtCacheManager;
	}

	public GlobalController getGlobalController() {
		return globalController;
	}

	public void setGlobalController(GlobalController globalController) {
		this.globalController = globalController;
	}
}
