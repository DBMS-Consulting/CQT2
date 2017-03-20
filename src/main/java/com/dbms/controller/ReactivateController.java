package com.dbms.controller;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.ViewScoped;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.event.AjaxBehaviorEvent;

import org.primefaces.context.RequestContext;
import org.primefaces.event.TransferEvent;
import org.primefaces.model.DualListModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dbms.entity.cqt.CmqBase190;
import com.dbms.service.ICmqBase190Service;
import com.dbms.service.ICmqRelation190Service;
import com.dbms.service.IRefCodeListService;
import com.dbms.util.exceptions.CqtServiceException;

/**
 **/
@ManagedBean
@ViewScoped
public class ReactivateController implements Serializable {

	private static final long serialVersionUID = -443251941538546278L;

	private static final Logger LOG = LoggerFactory
			.getLogger(ReactivateController.class);

	@ManagedProperty("#{CmqBase190Service}")
	private ICmqBase190Service cmqBaseService;

	@ManagedProperty("#{CmqRelation190Service}")
	private ICmqRelation190Service cmqRelationService;

	@ManagedProperty("#{RefCodeListService}")
	private IRefCodeListService refCodeListService;

	private List<CmqBase190> sourceListToReactivate;

	private List<CmqBase190> targetList;

	private DualListModel<CmqBase190> reactivateDualListModel;

	private CmqBaseDualListConverter cmqBaseDualListConverter;
	
	private String confirmMessage;
	
	@PostConstruct
	public void init() {
		sourceListToReactivate = this.cmqBaseService.findCmqsToReactivate();
		targetList = new ArrayList<CmqBase190>();
		reactivateDualListModel = new DualListModel<CmqBase190>(
				sourceListToReactivate, targetList);
		this.cmqBaseDualListConverter = new CmqBaseDualListConverter();
	}
	
	/**
	 * Event when we pick on the source list
	 * @param event
	 */
	public void pickList() {
		boolean childNotSelected = false;
		List<CmqBase190> targetCmqsSelected = new ArrayList<CmqBase190>(this.reactivateDualListModel.getTarget());
		List<Long> targetCmqCodes = new ArrayList<>();
		LOG.info("\n\n **********************   targetCmqsSelected size " + targetCmqsSelected.size());
		
		for (CmqBase190 cmqBase : targetCmqsSelected) {
			targetCmqCodes.add(cmqBase.getCmqCode());
		}
		
		List<CmqBase190> childCmqsOftargets = this.cmqBaseService.findChildCmqsByCodes(targetCmqCodes);
		if((null != childCmqsOftargets) && (childCmqsOftargets.size() > 0)) {
			//add them to the selected cmqs list
			for (CmqBase190 childCmq : childCmqsOftargets) {
				for (CmqBase190 srcCmq : sourceListToReactivate) {
					if (childCmq.getCmqCode().equals(srcCmq.getCmqCode())) {
						childNotSelected = true;
						break;
					}
				}
			}
		}
		if (childCmqsOftargets != null && !childCmqsOftargets.isEmpty() && childNotSelected)
			this.confirmMessage = "Not all associate child lists are selected for reactivation. Do you want to continue?";

		else
			this.confirmMessage = "Are you sure you want to reactivate this list?";
		
		RequestContext.getCurrentInstance().execute("PF('confirmReactivation').show();");
	}
	 

	public String reactivateTargetList() {
		List<Long> targetCmqCodes = new ArrayList<>();
		List<CmqBase190> targetCmqParents = new ArrayList<CmqBase190>();
		List<CmqBase190> targetCmqsSelected = new ArrayList<CmqBase190>(this.reactivateDualListModel.getTarget());
		if((targetCmqsSelected == null) || (targetCmqsSelected.size() == 0)) {
			FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR,
					"Please select at least 1 list to reactivate.", "");
			FacesContext.getCurrentInstance().addMessage(null, msg);
		} else {
			for (CmqBase190 cmqBase : targetCmqsSelected) {
				targetCmqCodes.add(cmqBase.getCmqCode());
				if(null != cmqBase.getCmqParentCode()) {
					CmqBase190 parent = cmqBaseService.findByCode(cmqBase.getCmqParentCode());
					if (parent != null && parent.getCmqStatus().equals("I") && parent.getCmqState().equalsIgnoreCase("published"))
						targetCmqParents.add(parent);
				}
			}
			
			//List<CmqBase190> faultyCmqs = new ArrayList<>();
			
			//now get the children
			//If a parent is reactivated, then child must be reactivated 
			//(If the child is status in active then show a error 
			//"The list being reactivated has an associated active child list hence cannot be reactivated.”
		//	boolean isChildReactivatedError = false;
			List<CmqBase190> childCmqsOftargets = this.cmqBaseService.findChildCmqsByCodes(targetCmqCodes);
			if((null != childCmqsOftargets) && (childCmqsOftargets.size() > 0)) {
				//add them to the selected cmqs list
				for (CmqBase190 childCmq : childCmqsOftargets) {
					if(childCmq.getCmqState().equalsIgnoreCase("published") && childCmq.getCmqStatus().equalsIgnoreCase("I")) {
						targetCmqsSelected.add(childCmq);//we need to reactivate these
					}
				}
				
 			}
			
			//If a child is being reactivated, and the parent is NOT selected, it SHOULD reactivate parent as well
			//it should also prints messages to show the multiple reactivations
			//and continue forward
			if(targetCmqParents.size() > 0) {
				//we need to show message that parent is Reactivated
				for (CmqBase190 cmqBase190 : targetCmqParents) {
					cmqBase190.setCmqState("DRAFT");
					cmqBase190.setCmqStatus("P"); 
//					cmqBase190.setActivatedBy("NONE");
//					cmqBase190.setActivationDate(new Date());
					cmqBase190.setLastModifiedDate(new Date());
					cmqBase190.setLastModifiedBy("NONE");
				}
				try {
					this.cmqBaseService.update(targetCmqParents);
				} catch (CqtServiceException e) {
					LOG.error(e.getMessage(), e);
 				}
			} 

			//continue
 			for (CmqBase190 cmqBase190 : targetCmqsSelected) {
				cmqBase190.setCmqState("DRAFT");
				cmqBase190.setCmqStatus("P"); 
//				cmqBase190.setActivatedBy("NONE");
//				cmqBase190.setActivationDate(new Date());
				cmqBase190.setLastModifiedDate(new Date());
				cmqBase190.setLastModifiedBy("NONE");
			}

			try {
				
				this.cmqBaseService.update(targetCmqsSelected);
				
				//update the dualListModel source and target
				init();
				String formatMsg = "The List(s) are reactivated successfully";
				
				if (targetCmqParents != null && !targetCmqParents.isEmpty())
 					formatMsg = "The List and retired parent list is Reactivated successfully";
					
				//show messages on screen
				FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_INFO,
						formatMsg, "");
				FacesContext.getCurrentInstance().addMessage(null, msg);
				
			} catch (CqtServiceException e) {
				LOG.error(e.getMessage(), e);

				//show error message popup for partial success.
				String codes = "";
				if (targetCmqsSelected != null) {
					for (CmqBase190 cmq : targetCmqsSelected) {
						codes += cmq.getCmqCode() + ";";
					}
				}
				//show error dialog with names of faulty cmqs
				FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR,
						"The system could not reactivate the following cmqs :" + codes, "");
				FacesContext.getCurrentInstance().addMessage(null, msg);
			
			} 
		}
		
		return "";
	}

	private class CmqBaseDualListConverter implements Converter {

		@Override
		public Object getAsObject(FacesContext context, UIComponent component,
				String value) {
			long inputValue = 0;
			try {
				inputValue = Long.valueOf(value);
			} catch (Exception e) {
				LOG.error(e.getMessage(), e);
			}

			for (CmqBase190 cmqBase190 : sourceListToReactivate) {
				if (cmqBase190.getCmqCode().longValue() == inputValue) {
					return cmqBase190;
				}
			}
			return null;
		}

		@Override
		public String getAsString(FacesContext context, UIComponent component,
				Object value) {
			return value.toString();
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

	public List<CmqBase190> getTargetList() {
		return targetList;
	}

	public void setTargetList(List<CmqBase190> targetList) {
		this.targetList = targetList;
	}

	public DualListModel<CmqBase190> getReactivateDualListModel() {
		return reactivateDualListModel;
	}

	public void setReactivateDualListModel(
			DualListModel<CmqBase190> reactivateDualListModel) {
		this.reactivateDualListModel = reactivateDualListModel;
	}

	public CmqBaseDualListConverter getCmqBaseDualListConverter() {
		return cmqBaseDualListConverter;
	}

	public void setCmqBaseDualListConverter(
			CmqBaseDualListConverter cmqBaseDualListConverter) {
		this.cmqBaseDualListConverter = cmqBaseDualListConverter;
	}

	public List<CmqBase190> getSourceListToReactivate() {
		return sourceListToReactivate;
	}

	public void setSourceListToReactivate(
			List<CmqBase190> sourceListToReactivate) {
		this.sourceListToReactivate = sourceListToReactivate;
	}

	public String getConfirmMessage() {
		return confirmMessage;
	}

	public void setConfirmMessage(String confirmMessage) {
		this.confirmMessage = confirmMessage;
	} 
}