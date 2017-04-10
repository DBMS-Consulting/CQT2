package com.dbms.controller;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.ViewScoped;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;

import org.primefaces.context.RequestContext;
import org.primefaces.model.DualListModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dbms.entity.cqt.CmqBase190;
import com.dbms.entity.cqt.CmqBaseTarget;
import com.dbms.service.ICmqBase190Service;
import com.dbms.service.ICmqBaseTargetService;
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
	
	@ManagedProperty("#{CmqBaseTargetService}")
	private ICmqBaseTargetService cmqBaseTargetService;

	private List<CmqBase190> sourceList;
	private List<CmqBase190> targetList;
	
	private List<CmqBaseTarget> sourceIAList;
	private List<CmqBaseTarget> targetIAList;

	private DualListModel<CmqBase190> demoteToDraftDualListModel;
	private DualListModel<CmqBaseTarget> demoteTargetDualListModel;

	private CmqBaseDualListConverter cmqBaseDualListConverter;
	private CmqBaseTargetDualListConverter cmqBaseTargetDualListConverter;

	
	@PostConstruct
	public void init() {
		sourceList = this.cmqBaseService.findPublishedCmqs();
		targetList = new ArrayList<CmqBase190>();
		sourceIAList = this.cmqBaseTargetService.findPublishedCmqs();
		targetIAList = new ArrayList<CmqBaseTarget>();
		demoteToDraftDualListModel = new DualListModel<CmqBase190>(sourceList, targetList);
		demoteTargetDualListModel = new DualListModel<CmqBaseTarget>(sourceIAList, targetIAList);
		this.cmqBaseDualListConverter = new CmqBaseDualListConverter();
		this.cmqBaseTargetDualListConverter = new CmqBaseTargetDualListConverter();
	}
	
	/**
	 * Event when we pick on the source list
	 * @param event
	 */
	public void pickList() {
		if(demoteTargetDualListModel.getTarget().isEmpty()) {
			FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR,
					"Please select at least 1 list to demote.", "");
			FacesContext.getCurrentInstance().addMessage(null, msg);
		}
		else
			RequestContext.getCurrentInstance().execute("PF('confirmDemote').show();");
	}

	public void demote() {
		List<Long> targetCmqCodes = new ArrayList<>();
		List<Long> targetCmqParentCodes = new ArrayList<>();
		List<CmqBase190> targetCmqsSelected = new ArrayList<CmqBase190>(this.demoteToDraftDualListModel.getTarget());
		if((targetCmqsSelected == null) || (targetCmqsSelected.size() == 0)) {
			FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR,
					"Please select atleats 1 list to demote.", "");
			FacesContext.getCurrentInstance().addMessage(null, msg);
		} else {
			for (CmqBase190 cmqBase : targetCmqsSelected) {
				targetCmqCodes.add(cmqBase.getCmqCode());
				if(null != cmqBase.getCmqParentCode()) {
					targetCmqParentCodes.add(cmqBase.getCmqParentCode());
				}
			}
			
			List<CmqBase190> faultyCmqs = new ArrayList<>();
			
			//now get the children
			//If a parent is demoted, then child must be demoted 
			//(If the child is status in active then show a error 
			//"The list being demoted has an associated active child list hence cannot be demoted.”
			boolean isChildDemotedError = false;
			List<CmqBase190> childCmqsOftargets = this.cmqBaseService.findChildCmqsByCodes(targetCmqCodes);
			if((null != childCmqsOftargets) && (childCmqsOftargets.size() > 0)) {
				//add them to the selected cmqs list
				for (CmqBase190 childCmq : childCmqsOftargets) {
					if(!childCmq.getCmqState().equalsIgnoreCase(CmqBase190.CMQ_STATE_VALUE_PUBLISHED)
							&& childCmq.getCmqStatus().equalsIgnoreCase(CmqBase190.CMQ_STATUS_VALUE_PENDING)) {
						isChildDemotedError = true;
						faultyCmqs.add(childCmq);
					} else {
						targetCmqsSelected.add(childCmq);//we need to demote this as well
					}
				}
			}
			
			if(isChildDemotedError) {
				//we show a error on screen that we have one or more parents in the list which has a child which is published and pending.
				FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR,
						"The lists being demoted have an associated active child list hence cannot be demoted.", "");
				FacesContext.getCurrentInstance().addMessage(null, msg);
			} else {
				//If there is a child is being demoted, and the parent is NOT demoted, 
				//it should give only a WARNING that the parent is not demoted but it should NOT stop the child from being demoted.
				//and continue forward
				if(targetCmqParentCodes.size() > 0) {
					//we need to show message that parent is not demoted yet but we carry on with the children.
					FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_WARN,
							"The lists being demoted have parent lists which are not demoted yet.", "");
					FacesContext.getCurrentInstance().addMessage(null, msg);
				} 
				
				//continue
				boolean hasErrorOccured = false;
				for (CmqBase190 cmqBase190 : targetCmqsSelected) {
					cmqBase190.setCmqState(CmqBase190.CMQ_STATE_VALUE_DRAFT);
				}
				
				try {
					this.cmqBaseService.update(targetCmqsSelected);
				} catch (CqtServiceException e) {
					LOG.error(e.getMessage(), e);
					hasErrorOccured = true;
				}
				
				if(hasErrorOccured) {
					//show error message popup for partial success.
					String codes = "";
					if (targetCmqsSelected != null) {
						for (CmqBase190 cmq : targetCmqsSelected) {
							codes += cmq.getCmqCode() + ";";
						}
					}
					//show error dialog with names of faulty cmqs
					FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR,
							"The system could not demote the following cmqs :" + codes, "");
					FacesContext.getCurrentInstance().addMessage(null, msg);
				} else {
					//update the dualListModel source and target
					init();
					
					//show messages on screen
					FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_INFO,
							"The List(s) are successfully demoted to Draft", "");
					FacesContext.getCurrentInstance().addMessage(null, msg);
				}
			}
		}
	}
	
	/**
	 * Demote IA list.
	 * @return
	 */
	public String demoteIATargetList() {
		List<Long> targetCmqCodes = new ArrayList<>();
		List<Long> targetCmqParentCodes = new ArrayList<>();
		List<CmqBaseTarget> targetCmqsSelected = new ArrayList<CmqBaseTarget>(this.demoteTargetDualListModel.getTarget());
		if((targetCmqsSelected == null) || (targetCmqsSelected.size() == 0)) {
			FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR,
					"Please select atleats 1 list to demote.", "");
			FacesContext.getCurrentInstance().addMessage(null, msg);
		} else {
			for (CmqBaseTarget cmqBase : targetCmqsSelected) {
				targetCmqCodes.add(cmqBase.getCmqCode());
				if(null != cmqBase.getCmqParentCode()) {
					targetCmqParentCodes.add(cmqBase.getCmqParentCode());
				}
			}
			
			List<CmqBaseTarget> faultyCmqs = new ArrayList<>();
			
			//now get the children
			//If a parent is demoted, then child must be demoted 
			//(If the child is status in active then show a error 
			//"The list being demoted has an associated active child list hence cannot be demoted.”
			boolean isChildDemotedError = false;
			List<CmqBaseTarget> childCmqsOftargets = this.cmqBaseTargetService.findChildCmqsByCodes(targetCmqCodes);
			if((null != childCmqsOftargets) && (childCmqsOftargets.size() > 0)) {
				//add them to the selected cmqs list
				for (CmqBaseTarget childCmq : childCmqsOftargets) {
					if(!childCmq.getCmqState().equalsIgnoreCase(CmqBaseTarget.CMQ_STATE_PUBLISHED_IA)
							&& childCmq.getCmqStatus().equalsIgnoreCase(CmqBaseTarget.CMQ_STATUS_PENDING_IA)) {
						isChildDemotedError = true;
						faultyCmqs.add(childCmq);
					} else {
						targetCmqsSelected.add(childCmq);//we need to demote this as well
					}
				}
			}
			
			if(isChildDemotedError) {
				//we show a error on screen that we have one or more parents in the list which has a child which is published and pending.
				FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR,
						"The lists being demoted have an associated active child list hence cannot be demoted.", "");
				FacesContext.getCurrentInstance().addMessage(null, msg);
			} else {
				//If there is a child is being demoted, and the parent is NOT demoted, 
				//it should give only a WARNING that the parent is not demoted but it should NOT stop the child from being demoted.
				//and continue forward
				if(targetCmqParentCodes.size() > 0) {
					//we need to show message that parent is not demoted yet but we carry on with the children.
					FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_WARN,
							"The lists being demoted have parent lists which are not demoted yet.", "");
					FacesContext.getCurrentInstance().addMessage(null, msg);
				} 
				
				//continue
				boolean hasErrorOccured = false;
				for (CmqBaseTarget target : targetCmqsSelected) {
					target.setCmqStatus("P");
					target.setCmqState(CmqBaseTarget.CMQ_STATUS_PENDING_IA);
				}
				
				try {
					this.cmqBaseTargetService.update(targetCmqsSelected);
				} catch (CqtServiceException e) {
					LOG.error(e.getMessage(), e);
					hasErrorOccured = true;
				}
				
				if(hasErrorOccured) {
					//show error message popup for partial success.
					String codes = "";
					if (targetCmqsSelected != null) {
						for (CmqBaseTarget cmq : targetCmqsSelected) {
							codes += cmq.getCmqCode() + ";";
						}
					}
					//show error dialog with names of faulty cmqs
					FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR,
							"The system could not demote the following cmqs :" + codes, "");
					FacesContext.getCurrentInstance().addMessage(null, msg);
				} else {
					//update the dualListModel source and target
					init();
					
					//show messages on screen
					FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_INFO,
							"The List(s) are successfully demoted to Draft", "");
					FacesContext.getCurrentInstance().addMessage(null, msg);
				}
			}
		}
		
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

	public DualListModel<CmqBase190> getDemoteToDraftDualListModel() {
		return demoteToDraftDualListModel;
	}

	public void setDemoteToDraftDualListModel(DualListModel<CmqBase190> demoteToDraftDualListModel) {
		this.demoteToDraftDualListModel = demoteToDraftDualListModel;
	}
	
	public CmqBaseDualListConverter getCmqBaseDualListConverter() {
		return cmqBaseDualListConverter;
	}

	public void setCmqBaseDualListConverter(CmqBaseDualListConverter cmqBaseDualListConverter) {
		this.cmqBaseDualListConverter = cmqBaseDualListConverter;
	}

	public DualListModel<CmqBaseTarget> getDemoteTargetDualListModel() {
		return demoteTargetDualListModel;
	}

	public void setDemoteTargetDualListModel(
			DualListModel<CmqBaseTarget> demoteTargetDualListModel) {
		this.demoteTargetDualListModel = demoteTargetDualListModel;
	}

	private class CmqBaseDualListConverter implements Converter {

		@Override
		public Object getAsObject(FacesContext context, UIComponent component, String value) {
			long inputValue = 0;
			try{
				inputValue = Long.valueOf(value);
			} catch (Exception e) {
				LOG.error(e.getMessage(), e);
			}
			/*PickList p = (PickList) component;
		    DualListModel<CmqBase190> dl = (DualListModel) p.getValue();
			List<CmqBase190> sourceList = dl.getSource();*/
			for (CmqBase190 cmqBase190 : sourceList) {
				if(cmqBase190.getCmqCode().longValue() == inputValue) {
					return cmqBase190;
				}
			}
			return null;
		}

		@Override
		public String getAsString(FacesContext context, UIComponent component, Object value) {
			return value.toString();
		}
	}
	
	private class CmqBaseTargetDualListConverter implements Converter {

		@Override
		public Object getAsObject(FacesContext context, UIComponent component, String value) {
			long inputValue = 0;
			try{
				inputValue = Long.valueOf(value);
			} catch (Exception e) {
				LOG.error(e.getMessage(), e);
			}
			 
			for (CmqBaseTarget target : sourceIAList) {
				if(target.getCmqCode().longValue() == inputValue) {
					return target;
				}
			}
			return null;
		}

		@Override
		public String getAsString(FacesContext context, UIComponent component, Object value) {
			return value.toString();
		}
	}

	public ICmqBaseTargetService getCmqBaseTargetService() {
		return cmqBaseTargetService;
	}

	public void setCmqBaseTargetService(ICmqBaseTargetService cmqBaseTargetService) {
		this.cmqBaseTargetService = cmqBaseTargetService;
	}

	public List<CmqBaseTarget> getSourceIAList() {
		return sourceIAList;
	}

	public void setSourceIAList(List<CmqBaseTarget> sourceIAList) {
		this.sourceIAList = sourceIAList;
	}

	public List<CmqBaseTarget> getTargetIAList() {
		return targetIAList;
	}

	public void setTargetIAList(List<CmqBaseTarget> targetIAList) {
		this.targetIAList = targetIAList;
	}

	public CmqBaseTargetDualListConverter getCmqBaseTargetDualListConverter() {
		return cmqBaseTargetDualListConverter;
	}

	public void setCmqBaseTargetDualListConverter(
			CmqBaseTargetDualListConverter cmqBaseTargetDualListConverter) {
		this.cmqBaseTargetDualListConverter = cmqBaseTargetDualListConverter;
	}

}
