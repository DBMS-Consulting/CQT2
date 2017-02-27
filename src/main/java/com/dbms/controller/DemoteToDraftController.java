package com.dbms.controller;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.ViewScoped;

import org.primefaces.model.DualListModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dbms.entity.cqt.CmqBase190;
import com.dbms.service.ICmqBase190Service;
import com.dbms.service.ICmqRelation190Service;
import com.dbms.service.IRefCodeListService;
import com.dbms.util.exceptions.CqtServiceException;

/**
 * @author Jay G.(jayshanchn@hotmail.com)
 * @date Feb 12, 2017 7:34:05 AM
 **/
@ManagedBean
@ViewScoped
public class DemoteToDraftController implements Serializable {

	private static final long serialVersionUID = -443251941538546278L;

	private static final Logger LOG = LoggerFactory.getLogger(DemoteToDraftController.class);

	@ManagedProperty("#{CmqBase190Service}")
	private ICmqBase190Service cmqBaseService;

	@ManagedProperty("#{CmqRelation190Service}")
	private ICmqRelation190Service cmqRelationService;

	@ManagedProperty("#{RefCodeListService}")
	private IRefCodeListService refCodeListService;

	private List<CmqBase190> sourceList;

	private List<CmqBase190> targetList;

	private DualListModel<CmqBase190> demoteToDraftDualListModel;

	@PostConstruct
	public void init() {
		sourceList = this.cmqBaseService.findPublishedCmqs();
		targetList = new ArrayList<CmqBase190>();
		demoteToDraftDualListModel = new DualListModel<CmqBase190>(sourceList, targetList);
	}

	public void demote() {
		List<Long> targetCmqCodes = new ArrayList<>();
		List<Long> targetCmqParentCodes = new ArrayList<>();
		List<CmqBase190> targetCmqsSelected = new ArrayList<CmqBase190>(this.demoteToDraftDualListModel.getTarget());
		for (CmqBase190 cmqBase : targetCmqsSelected) {
			targetCmqCodes.add(cmqBase.getCmqCode());
			if(null != cmqBase.getCmqParentCode()) {
				targetCmqParentCodes.add(cmqBase.getCmqParentCode());
			}
		}
		
		
		//If the is a child is being demoted, and the parent is NOT demoted, 
		//it should give only a WARNING that the parent is not demoted but it should NOT stop the child from being demoted.
		boolean isListDemotable = true;
		List<CmqBase190> faultyCmqs = new ArrayList<>();
		if(targetCmqParentCodes.size() > 0) {
			List<CmqBase190> parentCmqsList = this.cmqBaseService.findParentCmqsByCodes(targetCmqParentCodes);
			if(null != parentCmqsList) {
				for (CmqBase190 parentcmqBase : parentCmqsList) {
					if(!parentcmqBase.getCmqState().equalsIgnoreCase("draft") && parentcmqBase.getCmqStatus().equalsIgnoreCase("P")) {
						isListDemotable = false;
						faultyCmqs.add(parentcmqBase);
					}
				}
			}
		}
		
		if(!isListDemotable) {
			//we need to show message that parent is not demoted yet but we carry on with the children.
			
		} 
		
		//now get the children
		List<CmqBase190> childCmqsOftargets = this.cmqBaseService.findChildCmqsByCodes(targetCmqCodes);
		if((null != childCmqsOftargets) && (childCmqsOftargets.size() > 0)) {
			//add them to the selected cmqs list
			for (CmqBase190 childCmq : childCmqsOftargets) {
				if(!childCmq.getCmqState().equalsIgnoreCase("draft") && childCmq.getCmqStatus().equalsIgnoreCase("P")) {
					isListDemotable = false;
					faultyCmqs.add(childCmq);
					targetCmqsSelected.add(childCmq);//we need to demote this as well
				}
			}
		}
		
		
		if(!isListDemotable) {
			//we show a warning on screen that we have one or more parents in the list which has a child which is not demoted yet.
			//do you want to demote both?
			//when user clicks on yes we demote both by using the following code.
			
		} else {
			boolean hasErrorOccured = false;
			List<CmqBase190> cmqsFailedToSave = new ArrayList<>();
			//success
			for (CmqBase190 cmqBase190 : targetCmqsSelected) {
				cmqBase190.setCmqState("Draft");
				cmqBase190.setCmqStatus("P");
				try {
					this.cmqBaseService.update(cmqBase190);
				} catch (CqtServiceException e) {
					LOG.error(e.getMessage(), e);
					hasErrorOccured = true;
					cmqsFailedToSave.add(cmqBase190);
				}
			}
			
			if(hasErrorOccured) {
				//show error message popup for partial success.
			}
		}
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

	public IRefCodeListService getRefCodeListService() {
		return refCodeListService;
	}

	public void setRefCodeListService(IRefCodeListService refCodeListService) {
		this.refCodeListService = refCodeListService;
	}

	public List<CmqBase190> getSourceList() {
		return sourceList;
	}

	public void setSourceList(List<CmqBase190> sourceList) {
		this.sourceList = sourceList;
	}

	public List<CmqBase190> getTargetList() {
		return targetList;
	}

	public void setTargetList(List<CmqBase190> targetList) {
		this.targetList = targetList;
	}

	public DualListModel<CmqBase190> getDemoteToDraftDualListModel() {
		return demoteToDraftDualListModel;
	}

	public void setDemoteToDraftDualListModel(DualListModel<CmqBase190> demoteToDraftDualListModel) {
		this.demoteToDraftDualListModel = demoteToDraftDualListModel;
	}
}
