package com.dbms.controller;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.ViewScoped;

import org.apache.commons.lang3.StringUtils;
import org.primefaces.component.wizard.Wizard;
import org.primefaces.context.RequestContext;
import org.primefaces.event.FlowEvent;
import org.primefaces.model.DefaultTreeNode;
import org.primefaces.model.TreeNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dbms.csmq.HierarchyNode;
import com.dbms.entity.cqt.SmqRelation190;
import com.dbms.entity.cqt.dtos.HistoricalViewDTO;
import com.dbms.entity.cqt.dtos.HistoricalViewDbDataDTO;
import com.dbms.entity.cqt.dtos.MeddraDictHierarchySearchDto;
import com.dbms.service.AuthenticationService;
import com.dbms.service.ICmqBase190Service;
import com.dbms.service.ICmqParentChild200Service;
import com.dbms.service.ICmqRelation190Service;
import com.dbms.service.IHistoricalViewService;
import com.dbms.service.IMeddraDictService;
import com.dbms.service.IRefCodeListService;
import com.dbms.service.ISmqBaseService;
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
				new HierarchyNode("LEVEL", "NAME", "CODE", "SCOPE", "CATEGORY", "WEIGHT", null), null);;
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
					if(!addedHierarchyNodes.contains(hierarchyNode)) {
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
	
	private HierarchyNode createRelationNode(String termDictLevel, String term, Long termCode, String termScope) {
		HierarchyNode node = new HierarchyNode();
		boolean isMeddra = false;
		boolean isSmq = false;
		int smqLevel = -1;
		MeddraDictHierarchySearchDto meddraDictHierarchySearchDto = new MeddraDictHierarchySearchDto();
		if(termDictLevel.equals("SMQ1")) {
			node.setLevel("SMQ1");
			isSmq = true;
			smqLevel = 1;
		} else if(termDictLevel.equals("SMQ2")) {
			node.setLevel("SMQ2");
			isSmq = true;
			smqLevel = 2;
		} else if(termDictLevel.equals("SMQ3")) {
			node.setLevel("SMQ3");
			isSmq = true;
			smqLevel = 3;
		} else if(termDictLevel.equals("SMQ4")) {
			node.setLevel("SMQ4");
			isSmq = true;
			smqLevel = 4;
		} else if(termDictLevel.equals("SMQ5")) {
			node.setLevel("SMQ5");
			isSmq = true;
			smqLevel = 5;
		} else if(termDictLevel.equals("LLT")) {
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
		if(StringUtils.isBlank(termScope)) {
			node.setScope("");
		} else {
			node.setScope(termScope);
		}
		
		if(isSmq) {
			SmqRelation190 smqRelation190 = new SmqRelation190();
			smqRelation190.setSmqCode(termCode);
			smqRelation190.setSmqLevel(smqLevel);
			smqRelation190.setPtName(term);
			node.setEntity(smqRelation190);
			
		} else if(isMeddra) {
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
		RequestContext.getCurrentInstance().execute("PF('wizard').next()");
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

}
