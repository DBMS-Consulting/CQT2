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
 * @author 
 **/
@ManagedBean
@ViewScoped
public class RetireController implements Serializable {

	private static final long serialVersionUID = -443251941538546278L;

	private static final Logger LOG = LoggerFactory
			.getLogger(RetireController.class);

	@ManagedProperty("#{CmqBase190Service}")
	private ICmqBase190Service cmqBaseService;

	@ManagedProperty("#{CmqRelation190Service}")
	private ICmqRelation190Service cmqRelationService;

	@ManagedProperty("#{RefCodeListService}")
	private IRefCodeListService refCodeListService;

	private List<CmqBase190> sourceListToRetire;

	private List<CmqBase190> targetList;

	private DualListModel<CmqBase190> retireDualListModel;

	private CmqBaseDualListConverter cmqBaseDualListConverter;
	
	private String confirmMessage;
	
	@PostConstruct
	public void init() {
		sourceListToRetire = this.cmqBaseService.findCmqsToRetire();
		targetList = new ArrayList<CmqBase190>();
		retireDualListModel = new DualListModel<CmqBase190>(
				sourceListToRetire, targetList);
		this.cmqBaseDualListConverter = new CmqBaseDualListConverter();
	}
	
	/**
	 * Event when we pick on the source list
	 * @param event
	 */
	public void pickList() {
		boolean childNotSelected = false;
		List<CmqBase190> targetCmqsSelected = new ArrayList<CmqBase190>(this.retireDualListModel.getTarget());
		List<Long> targetCmqCodes = new ArrayList<>();
		LOG.info("\n\n **********************   targetCmqsSelected size " + targetCmqsSelected.size());
		
		for (CmqBase190 cmqBase : targetCmqsSelected) {
			targetCmqCodes.add(cmqBase.getCmqCode());
		}
		
		List<CmqBase190> childCmqsOftargets = this.cmqBaseService.findChildCmqsByCodes(targetCmqCodes);
		if((null != childCmqsOftargets) && (childCmqsOftargets.size() > 0)) {
			//add them to the selected cmqs list
			for (CmqBase190 childCmq : childCmqsOftargets) {
				for (CmqBase190 srcCmq : sourceListToRetire) {
					if (childCmq.getCmqCode().equals(srcCmq.getCmqCode())) {
						childNotSelected = true;
						break;
					}
				}
			}
		}
		if (childCmqsOftargets != null && !childCmqsOftargets.isEmpty() && !childNotSelected)
			this.confirmMessage = "Not all associate child lists are selected for inactivation. Do you want to continue?";

		else
			this.confirmMessage = "Are you sure you want to retire this list?";
		
		RequestContext.getCurrentInstance().execute("PF('confirmRetire').show();");
	}
	 

	public String retireTargetList() {
		List<Long> targetCmqCodes = new ArrayList<>();
		List<CmqBase190> targetCmqParents = new ArrayList<CmqBase190>();
		List<CmqBase190> targetCmqsSelected = new ArrayList<CmqBase190>(this.retireDualListModel.getTarget());
		if((targetCmqsSelected == null) || (targetCmqsSelected.size() == 0)) {
			FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR,
					"Please select at least 1 list to retire.", "");
			FacesContext.getCurrentInstance().addMessage(null, msg);
		} else {
			
			//now get the children
			//If a parent is retire, then child must be retire 
			//(If the child is status in active then show a error 
			//"The list being retire has an associated active child list hence cannot be retire.”
			List<CmqBase190> childCmqsOftargets = this.cmqBaseService.findChildCmqsByCodes(targetCmqCodes);
			if((null != childCmqsOftargets) && (childCmqsOftargets.size() > 0)) {
				//add them to the selected cmqs list
				for (CmqBase190 childCmq : childCmqsOftargets) {
					if(childCmq.getCmqState().equalsIgnoreCase("published") && childCmq.getCmqStatus().equalsIgnoreCase("A")) {
						targetCmqsSelected.add(childCmq);//we need to retire these
					}
				}
 			}
			
			//continue
 			for (CmqBase190 cmqBase190 : targetCmqsSelected) {
 				cmqBase190.setCmqStatus("I"); 
			}
			try {
				this.cmqBaseService.update(targetCmqsSelected);
				
				//update the dualListModel source and target
				init();
				String formatMsg = "The List(s) are retired successfully";
				
				if (targetCmqParents != null && !targetCmqParents.isEmpty())
 					formatMsg = "The List and retired parent list is retire successfully";
					
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
						"The system could not retire the following cmqs :" + codes, "");
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

			for (CmqBase190 cmqBase190 : sourceListToRetire) {
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

	 

	public CmqBaseDualListConverter getCmqBaseDualListConverter() {
		return cmqBaseDualListConverter;
	}

	public void setCmqBaseDualListConverter(
			CmqBaseDualListConverter cmqBaseDualListConverter) {
		this.cmqBaseDualListConverter = cmqBaseDualListConverter;
	}

	 

	public String getConfirmMessage() {
		return confirmMessage;
	}

	public void setConfirmMessage(String confirmMessage) {
		this.confirmMessage = confirmMessage;
	}

	public List<CmqBase190> getSourceListToRetire() {
		return sourceListToRetire;
	}

	public void setSourceListToRetire(List<CmqBase190> sourceListToRetire) {
		this.sourceListToRetire = sourceListToRetire;
	}

	public DualListModel<CmqBase190> getRetireDualListModel() {
		return retireDualListModel;
	}

	public void setRetireDualListModel(DualListModel<CmqBase190> retireDualListModel) {
		this.retireDualListModel = retireDualListModel;
	} 
}
