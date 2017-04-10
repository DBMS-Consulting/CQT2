package com.dbms.view;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;

import org.apache.commons.collections4.CollectionUtils;
import org.primefaces.event.CloseEvent;
import org.primefaces.event.NodeExpandEvent;
import org.primefaces.event.RowEditEvent;
import org.primefaces.model.DefaultTreeNode;
import org.primefaces.model.TreeNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dbms.controller.beans.HierarchySearchResultBean;
import com.dbms.csmq.HierarchyNode;
import com.dbms.entity.cqt.CmqBase190;
import com.dbms.entity.cqt.SmqBase190;
import com.dbms.entity.cqt.dtos.MeddraDictHierarchySearchDto;
import com.dbms.entity.cqt.dtos.MeddraDictReverseHierarchySearchDto;
import com.dbms.service.ICmqBase190Service;
import com.dbms.service.ICmqRelation190Service;
import com.dbms.service.IMeddraDictService;
import com.dbms.service.ISmqBaseService;
import com.dbms.util.MeddraDictLevelHelper;
import com.dbms.util.SMQLevelHelper;

/**
 * @date Feb 7, 2017 7:39:34 AM
 **/
public class CmqBaseHierarchySearchVM {

	private static final Logger log = LoggerFactory
			.getLogger(CmqBaseHierarchySearchVM.class);

	private ICmqBase190Service cmqBaseService;
	private ISmqBaseService smqBaseService;
	private IMeddraDictService meddraDictService;
	private ICmqRelation190Service cmqRelationService;

	private String myFilterTermName;
	private String myFilterLevel;

	private TreeNode myHierarchyRoot;
	private TreeNode[] mySelectedNodes;

	private List<HierarchySearchResultBean> hierarchySearchResults;
	
	private IRelationsChangeListener onDropRelationsListener;

	public CmqBaseHierarchySearchVM(ICmqBase190Service cmqBaseSvc,
			ISmqBaseService smqBaseSvc,
			IMeddraDictService meddraDictSvc,
			ICmqRelation190Service cmqRelationSvc) {
		this.cmqBaseService = cmqBaseSvc;
		this.smqBaseService = smqBaseSvc;
		this.meddraDictService = meddraDictSvc;
		this.cmqRelationService = cmqRelationSvc;
		
		myHierarchyRoot = new DefaultTreeNode("root", new HierarchyNode("LEVEL",
				"NAME", "CODE", null), null);
	}

	public void onRowCancel(RowEditEvent event) {
		FacesMessage msg = new FacesMessage("Canceled", "ZZ");
		FacesContext.getCurrentInstance().addMessage(null, msg);
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public String hierarchySearch() {
		CmqBaseRelationsTreeHelper relationsTreeHelper = new CmqBaseRelationsTreeHelper(cmqBaseService, smqBaseService, meddraDictService, cmqRelationService);
		
		SMQLevelHelper smqLevelH = SMQLevelHelper.getByLabel(myFilterLevel);
		MeddraDictLevelHelper meddraLevelH = MeddraDictLevelHelper.getByLabel(myFilterLevel);

		if (smqLevelH != null) {
			List<SmqBase190> smqBaseList = smqBaseService.findByLevelAndTerm(
					smqLevelH.getLevel(), myFilterTermName);
			log.info("smqBaseList values {}", smqBaseList == null ? 0
					: smqBaseList.size());

			this.myHierarchyRoot = new DefaultTreeNode("root", new HierarchyNode(
					"LEVEL", "NAME", "CODE", null), null);

			for (SmqBase190 smqBase : smqBaseList) {
				HierarchyNode node = relationsTreeHelper.createSmqBaseNode(smqBase);
				TreeNode parentTreeNode = new DefaultTreeNode(node,
						this.myHierarchyRoot);
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
		} else if (meddraLevelH != null && meddraLevelH.getSearchFrom() == MeddraDictLevelHelper.SEARCH_MEDDRA_BASE) {
			List<MeddraDictHierarchySearchDto> meddraDictDtoList = meddraDictService
					.findByLevelAndTerm(meddraLevelH.getTermPrefix(), myFilterTermName);
			this.myHierarchyRoot = new DefaultTreeNode("root", new HierarchyNode(
					"LEVEL", "NAME", "CODE", null), null);

			String childSearchColumnTypePrefix = meddraLevelH.getTermPrefix();
			String parentCodeColumnPrefix = myFilterLevel + "_";
			
			for (MeddraDictHierarchySearchDto meddraDictDto : meddraDictDtoList) {
				HierarchyNode node = relationsTreeHelper.createMeddraNode(meddraDictDto, myFilterLevel);
				TreeNode parentTreeNode = new DefaultTreeNode(node, this.myHierarchyRoot);
				
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
		} else if (meddraLevelH != null && meddraLevelH.getSearchFrom() == MeddraDictLevelHelper.SEARCH_MEDDRA_BASE_REVERSE) {
			List<MeddraDictReverseHierarchySearchDto> meddraDictDtoList = meddraDictService
					.findFullReverseHierarchyByLevelAndTerm(myFilterLevel, myFilterLevel, myFilterTermName);
			this.myHierarchyRoot = new DefaultTreeNode("root", new HierarchyNode("LEVEL", "NAME", "CODE", null), null);
			
			for (MeddraDictReverseHierarchySearchDto meddraDictReverseDto : meddraDictDtoList) {
				HierarchyNode node = relationsTreeHelper.createMeddraReverseNode(meddraDictReverseDto, myFilterLevel, true);
				TreeNode parentTreeNode = new DefaultTreeNode(node, this.myHierarchyRoot);
				
				// add a dummmy node to show expand arrow
				HierarchyNode dummyNode = new HierarchyNode(null, null,
						null, null);
				dummyNode.setDummyNode(true);
				new DefaultTreeNode(dummyNode, parentTreeNode);
			}
		} else if ("PRO".equalsIgnoreCase(myFilterLevel)) {
			List<CmqBase190> cmqBaseList = cmqBaseService.findByLevelAndTerm(2,
					myFilterTermName);
			this.myHierarchyRoot = new DefaultTreeNode("root", new HierarchyNode(
					"LEVEL", "NAME", "CODE", null), null);
			
			List<Long> parentCmqCodeList = new ArrayList<>();
			Map<Long, TreeNode> parentTreeNodes = new HashMap<Long, TreeNode>();
			
			for (CmqBase190 cmqBase190 : cmqBaseList) {
				HierarchyNode node = new HierarchyNode();
				node.setLevel(myFilterLevel);
				node.setTerm(cmqBase190.getCmqName());
				node.setCode(cmqBase190.getCmqCode().toString());
				node.setEntity(cmqBase190);

				TreeNode cmqBaseTreeNode = new DefaultTreeNode(node,
						myHierarchyRoot);
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

	//uiEventSourceName is either relations or hierarchy
	public void onNodeExpand(NodeExpandEvent event) {
		//event source attriute from the ui
		String uiSourceOfEvent =  (String) event.getComponent().getAttributes().get("uiEventSourceName");
		TreeNode expandedTreeNode = event.getTreeNode();
		boolean isNodesEditable = !"RELATIONS".equals(uiSourceOfEvent);
		CmqBaseRelationsTreeHelper relationsSearchHelper = new CmqBaseRelationsTreeHelper(cmqBaseService, smqBaseService, meddraDictService, cmqRelationService);	
		this.myHierarchyRoot = relationsSearchHelper.getRelationsNodeHierarchy(this.myHierarchyRoot, expandedTreeNode, isNodesEditable);
	}

	/**
	 * Reset Search form/results of hierarchy select dialog
	 */
	public void resetForm() {
		this.myHierarchyRoot = new DefaultTreeNode("root", new HierarchyNode(
				"LEVEL", "NAME", "CODE", null), null);
		this.myFilterLevel = null;
		this.myFilterTermName = "";
	}
	
	public void onCloseHierarchySearch(CloseEvent event) {
		resetForm();
	}

	public String getFilterLevel() {
		return myFilterLevel;
	}

	public void setFilterLevel(String levelH) {
		this.myFilterLevel = levelH;
	}

	public String getFilterTermName() {
		return myFilterTermName;
	}

	public void setFilterTermName(String filterTermName) {
		this.myFilterTermName = filterTermName;
	}

	public TreeNode getHierarchyRoot() {
		return myHierarchyRoot;
	}
    
	public void setHierarchyRoot(TreeNode hierarchyRoot) {
		this.myHierarchyRoot = hierarchyRoot;
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
	
	public IRelationsChangeListener getRelationsChangeListener() {
		return onDropRelationsListener;
	}

	public void setRelationsChangeListener(IRelationsChangeListener onDropRelationsListener) {
		this.onDropRelationsListener = onDropRelationsListener;
	}


	public TreeNode[] getSelectedNodes() {
		return mySelectedNodes;
	}

	public void setSelectedNodes(TreeNode[] mySelectedNodes) {
		this.mySelectedNodes = mySelectedNodes;
	}


	public interface IRelationsChangeListener {
		public void onDropRelations();
		public void addSelectedRelations(TreeNode[] nodes);
	}
	
}
