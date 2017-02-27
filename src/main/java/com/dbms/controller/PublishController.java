package com.dbms.controller;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;

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
public class PublishController implements Serializable {

	private static final long serialVersionUID = -443251941538546278L;

	private static final Logger LOG = LoggerFactory.getLogger(PublishController.class);

	@ManagedProperty("#{CmqBase190Service}")
	private ICmqBase190Service cmqBaseService;

	@ManagedProperty("#{CmqRelation190Service}")
	private ICmqRelation190Service cmqRelationService;

	@ManagedProperty("#{RefCodeListService}")
	private IRefCodeListService refCodeListService;

	private List<CmqBase190> sourceList;

	private List<CmqBase190> targetList;

	private DualListModel<CmqBase190> publishCurrentVersionDualListModel;
	
	@PostConstruct
	public void init() {
		sourceList = this.cmqBaseService.findApprovedCmqs();
		targetList = new ArrayList<CmqBase190>();
		publishCurrentVersionDualListModel = new DualListModel<CmqBase190>(sourceList, targetList);
	}

	public String promoteTargetList() {
		List<Long> targetCmqCodes = new ArrayList<>();
		List<Long> targetCmqParentCodes = new ArrayList<>();
		List<CmqBase190> targetCmqsSelected = this.publishCurrentVersionDualListModel.getTarget();
		for (CmqBase190 cmqBase : targetCmqsSelected) {
			targetCmqCodes.add(cmqBase.getCmqCode());
			if(null != cmqBase.getCmqParentCode()) {
				targetCmqParentCodes.add(cmqBase.getCmqParentCode());
			}
		}
		
		boolean isListPublishable = true;
		List<CmqBase190> faultyCmqs = new ArrayList<>();
		List<CmqBase190> childCmqsOftargets = this.cmqBaseService.findChildCmqsByCodes(targetCmqCodes);
		if(null != childCmqsOftargets) {
			for (CmqBase190 cmqBase190 : childCmqsOftargets) {
				if(!cmqBase190.getCmqState().equalsIgnoreCase("published") && cmqBase190.getCmqStatus().equalsIgnoreCase("P")) {
					isListPublishable = false;
					faultyCmqs.add(cmqBase190);
				}
			}
		}
		
		if(!isListPublishable) {
			String codes = "";
			if (faultyCmqs != null) {
				for (CmqBase190 cmq : faultyCmqs) {
					codes += cmq.getCmqCode() + ";";
				}
			}
			//show error dialog with names of faulty cmqs
			LOG.info("\n\n ******  " + codes); 
			FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR,
					"The list being promoted has an associated list that must be Promoted", "");
			FacesContext.getCurrentInstance().addMessage(null, msg);
			
			return "";
		} else {
			//now check the parents of these cmqs
			if(targetCmqParentCodes.size() > 0) {
				List<CmqBase190> parentCmqsList = this.cmqBaseService.findParentCmqsByCodes(targetCmqParentCodes);
				if(null != parentCmqsList) {
					for (CmqBase190 cmqBase190 : parentCmqsList) {
						if(!cmqBase190.getCmqState().equalsIgnoreCase("published") && cmqBase190.getCmqStatus().equalsIgnoreCase("P")) {
							isListPublishable = false;
							faultyCmqs.add(cmqBase190);
						}
					}
				}
			}
			
			if(!isListPublishable) {
				//show error dialog with names of faulty cmqs
				String codes = "";
				if (faultyCmqs != null) {
					for (CmqBase190 cmq : faultyCmqs) {
						codes += cmq.getCmqCode() + ";";
					}
				}
				LOG.info("\n\n ******  " + codes); 
				//show error dialog with names of faulty cmqs
				FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR,
						"The list being promoted has an associated list that must be Promoted. ", "");
				FacesContext.getCurrentInstance().addMessage(null, msg);
				
				return "";
			} else {
				boolean hasErrorOccured = false;
				List<CmqBase190> cmqsFailedToSave = new ArrayList<>();
				//success
				for (CmqBase190 cmqBase190 : targetCmqsSelected) {
					cmqBase190.setCmqState("Published");
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
					String codes = "";
					if (cmqsFailedToSave != null) {
						for (CmqBase190 cmq : cmqsFailedToSave) {
							codes += cmq.getCmqCode() + ";";
						}
					}
					//show error dialog with names of faulty cmqs
					FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR,
							"The system could not publish the following cmqs :" + codes, "");
					FacesContext.getCurrentInstance().addMessage(null, msg);
				}
				else {
					FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_INFO,
							"The CMQs were successfully published", "");
					FacesContext.getCurrentInstance().addMessage(null, msg);
				}
			}
		}//end 
		
		return "";
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

	public DualListModel<CmqBase190> getPublishCurrentVersionDualListModel() {
		return publishCurrentVersionDualListModel;
	}

	public void setPublishCurrentVersionDualListModel(DualListModel<CmqBase190> publishCurrentVersionDualListModel) {
		this.publishCurrentVersionDualListModel = publishCurrentVersionDualListModel;
	}
}
