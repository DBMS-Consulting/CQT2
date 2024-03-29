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

import org.apache.commons.lang3.StringUtils;
import org.primefaces.context.RequestContext;
import org.primefaces.model.DualListModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dbms.entity.cqt.CmqBase190;
import com.dbms.entity.cqt.CmqBaseTarget;
import com.dbms.service.AuthenticationService;
import com.dbms.service.ICmqBase190Service;
import com.dbms.service.ICmqBaseTargetService;
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

	@ManagedProperty("#{AuthenticationService}")
	private AuthenticationService authService;
	
	@ManagedProperty("#{CmqBaseTargetService}")
    private ICmqBaseTargetService cmqTargetService;
	
	private List<CmqBase190> sourceListToRetire;

	private List<CmqBase190> targetList;

	private DualListModel<CmqBase190> retireDualListModel;

	private CmqBaseDualListConverter cmqBaseDualListConverter;
	
	private String confirmMessage;
	
	private boolean childNotSelected;
	
	private boolean parentNotSelected;
	
	private boolean deleteRelation;
	
	public String retirementReason;
	
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
		childNotSelected = true;
		parentNotSelected = true;
		int cpt = 0;
		int cptChild = 0;
		int ppt =0;
		int pptParent = 0;
		List<CmqBase190> targetCmqsSelected = new ArrayList<CmqBase190>(this.retireDualListModel.getTarget());
		if(targetCmqsSelected.isEmpty()) {
			FacesContext.getCurrentInstance().addMessage(null, 
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Please select at least 1 list to retire.", ""));
			return;
		} 
		List<Long> targetCmqCodes = new ArrayList<>();
		LOG.info("\n\n **********************   targetCmqsSelected size " + targetCmqsSelected.size());
		
		boolean tmeOrTier1 = true;
		boolean isProtocol = true;
		boolean isDME = false;
		boolean isCPT = false;
		
		for (CmqBase190 cmqBase : targetCmqsSelected) {
			targetCmqCodes.add(cmqBase.getCmqCode());
			if (!cmqBase.getCmqTypeCd().equalsIgnoreCase("TME") && !cmqBase.getCmqTypeCd().equalsIgnoreCase("TR1")) {
				tmeOrTier1 = false;
			}
			if(!cmqBase.getCmqTypeCd().equalsIgnoreCase("PRO")) {
				isProtocol = false;
			}
			if(cmqBase.getCmqTypeCd().equalsIgnoreCase("DME")) {
				isDME = true;
			} else if(cmqBase.getCmqTypeCd().equalsIgnoreCase("CPT")) {
				isCPT = true;
			}
		}
		
		List<CmqBase190> childCmqsOftargets = this.cmqBaseService.findChildCmqsByCodes(targetCmqCodes);
		List<CmqBase190> parentCmqsOftargets = this.cmqBaseService.findParentCmqsByCodes(targetCmqCodes);
		
		if((null != childCmqsOftargets) && (childCmqsOftargets.size() > 0)) {
			//add them to the selected cmqs list
			for (CmqBase190 childCmq : childCmqsOftargets) {
				if (childCmq.getCmqStatus().equals(CmqBase190.CMQ_STATUS_VALUE_ACTIVE)
						&& childCmq.getCmqState().equalsIgnoreCase(CmqBase190.CMQ_STATE_VALUE_PUBLISHED))
					cptChild++;
				for (CmqBase190 srcCmq : targetCmqsSelected) {
					if (!srcCmq.getCmqCode().equals(childCmq.getCmqParentCode()))
						if (childCmq.getCmqCode().equals(srcCmq.getCmqCode())) 
							cpt++;
				}
			}
			for (CmqBase190 parentCmq : parentCmqsOftargets) {
				if (parentCmq.getCmqStatus().equals(CmqBase190.CMQ_STATUS_VALUE_ACTIVE)
						&& parentCmq.getCmqState().equalsIgnoreCase(CmqBase190.CMQ_STATE_VALUE_PUBLISHED))
					pptParent++;
				for (CmqBase190 srcCmq : targetCmqsSelected) {
					if (!srcCmq.getCmqCode().equals(parentCmq.getCmqParentCode()))
						if (parentCmq.getCmqCode().equals(srcCmq.getCmqCode())) 
							ppt++;
				}
			}
			if (cpt == cptChild) //if (cpt == childCmqsOftargets.size())
				childNotSelected = false;
			if (ppt == pptParent)
				parentNotSelected = false;
		}
		if(parentCmqsOftargets != null && !parentCmqsOftargets.isEmpty() && parentNotSelected && isProtocol) {
			this.confirmMessage = "Do you want to delete the association between Protocol and Program list?";
			RequestContext.getCurrentInstance().execute("PF('confirmRetirePro').show();");
		} else if (childCmqsOftargets != null && !childCmqsOftargets.isEmpty() && childNotSelected) {
			this.confirmMessage = "Not all associate child lists are selected for inactivation.";
			RequestContext.getCurrentInstance().execute("PF('confirmRetireOK').show();");
		} else if (childCmqsOftargets != null && !childCmqsOftargets.isEmpty() && !childNotSelected) {
			saveRetirementReason();
		} else if (childCmqsOftargets.isEmpty() && (tmeOrTier1 || isDME || isCPT || !isProtocol)) {
			saveRetirementReason();
		} else {
			this.confirmMessage = "Are you sure you want to retire this list?";
			RequestContext.getCurrentInstance().execute("PF('confirmRetire').show();");
		}
	}
	 

	public String retireTargetList() {
		String lastModifiedByString = this.authService.getLastModifiedByUserAsString();

		List<Long> targetCmqCodes = new ArrayList<>();
		List<CmqBase190> targetCmqParents = new ArrayList<CmqBase190>();
		List<CmqBase190> targetCmqsSelected = new ArrayList<CmqBase190>(this.retireDualListModel.getTarget());
		
		if((targetCmqsSelected == null) || (targetCmqsSelected.size() == 0)) {
			FacesContext.getCurrentInstance().addMessage(null, 
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Please select at least 1 list to retire.", ""));
		} else {
			for (CmqBase190 cmqBase : targetCmqsSelected) {
				targetCmqCodes.add(cmqBase.getCmqCode());
			}
			
			
			//now get the children
			//If a parent is retire, then child must be retire 
			//(If the child is status in active then show a error 
			//"The list being retire has an associated active child list hence cannot be retire.”
			List<CmqBase190> childCmqsOftargets = this.cmqBaseService.findChildCmqsByCodes(targetCmqCodes);
			if (childNotSelected && childCmqsOftargets != null && childCmqsOftargets.size() > 0)
				return "";
			if((null != childCmqsOftargets) && (childCmqsOftargets.size() > 0)) {
				//add them to the selected cmqs list
				for (CmqBase190 childCmq : childCmqsOftargets) {
					if(childCmq.getCmqState().equalsIgnoreCase(CmqBase190.CMQ_STATE_VALUE_PUBLISHED)
							&& childCmq.getCmqStatus().equalsIgnoreCase(CmqBase190.CMQ_STATUS_VALUE_ACTIVE)) {
						targetCmqsSelected.add(childCmq);//we need to retire these if selected
					}
				}
 			}
			
			//continue
 			for (CmqBase190 cmqBase190 : targetCmqsSelected) {
 				cmqBase190.setCmqStatus(CmqBase190.CMQ_STATUS_VALUE_INACTIVE); 
 				cmqBase190.setLastModifiedDate(new Date());
				//cmqBase190.setLastModifiedBy("NONE");
				cmqBase190.setLastModifiedBy(lastModifiedByString);
			}
			try {
				this.cmqBaseService.update(targetCmqsSelected, this.authService.getUserCn()
						, this.authService.getUserGivenName(), this.authService.getUserSurName()
						, this.authService.getCombinedMappedGroupMembershipAsString());
				
				//update the dualListModel source and target
				init();
				String formatMsg = "The List(s) is retired successfully";
				
				if (targetCmqParents != null && !targetCmqParents.isEmpty())
 					formatMsg = "The List and retired parent list are retired successfully";
					
				//show messages on screen
				FacesContext.getCurrentInstance().addMessage(null, 
                        new FacesMessage(FacesMessage.SEVERITY_INFO,
                                formatMsg, ""));
				
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
				FacesContext.getCurrentInstance().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_ERROR,
                                "The system could not retire the following cmqs :" + codes, ""));
			} 
		}
		
		return "";
	}
	
	public String saveRetirementReasonAndDeleteRelation() {
		this.confirmMessage = "Please enter the retirement reason.";
		this.retirementReason = "";
		RequestContext.getCurrentInstance().execute("PF('RetireDescriptionAndDelete').show();");
		setDeleteRelation(true);
		return"";
	}
	
	public String saveRetirementReason() {
		this.confirmMessage = "Please enter the retirement reason.";
		this.retirementReason = "";
		RequestContext.getCurrentInstance().execute("PF('RetireDescriptionAndDelete').show();");
		setDeleteRelation(false);
		return"";
	}
	
	public String retireTargetProtocolList() {
		String lastModifiedByString = this.authService.getLastModifiedByUserAsString();
		
		List<Long> targetCmqCodes = new ArrayList<>();
		List<CmqBase190> targetCmqParents = new ArrayList<CmqBase190>();
		List<Long> cmqBaseCode = new ArrayList<>();
		List<CmqBase190> parentCmqOftarget = new ArrayList<CmqBase190>();
		List<CmqBase190> targetCmqsSelected = new ArrayList<CmqBase190>(this.retireDualListModel.getTarget());
		
		
		
		if((targetCmqsSelected == null) || (targetCmqsSelected.size() == 0)) {
			FacesContext.getCurrentInstance().addMessage(null, 
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Please select at least 1 list to retire.", ""));
			
		} else {
			if(StringUtils.isBlank(getRetirementReason())) {
				FacesContext.getCurrentInstance().addMessage(null, 
	                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
	                            "Please enter the retirement reason", ""));
				RequestContext.getCurrentInstance().execute("PF('RetireDescriptionAndDelete').show();");
				return "";
			}
			for (CmqBase190 cmqBase : targetCmqsSelected) {
				targetCmqCodes.add(cmqBase.getCmqCode());
				if(cmqBaseCode != null) {
					cmqBaseCode.clear();
				}
				cmqBaseCode.add(cmqBase.getCmqCode());
				parentCmqOftarget = this.cmqBaseService.findParentCmqsByCodes(cmqBaseCode);
				if(parentCmqOftarget != null) {
					if(cmqBase.getCmqNote() == null) {
						cmqBase.setCmqNote("RETIREMENT REASON: " + getRetirementReason());
					} else {
						cmqBase.setCmqNote(cmqBase.getCmqNote() + "\n\n" + "RETIREMENT REASON: " + getRetirementReason());
					}
					if(getDeleteRelation()) {
						cmqBase.setCmqParentCode(null);
						cmqBase.setCmqParentName(null);
					}
					parentCmqOftarget.clear();
				}
				
			}
			
			
			
			//now get the children
			//If a parent is retire, then child must be retire 
			//(If the child is status in active then show a error 
			//"The list being retire has an associated active child list hence cannot be retire.”
			List<CmqBase190> childCmqsOftargets = this.cmqBaseService.findChildCmqsByCodes(targetCmqCodes);
			if (childNotSelected && childCmqsOftargets != null && childCmqsOftargets.size() > 0)
				return "";
			if((null != childCmqsOftargets) && (childCmqsOftargets.size() > 0)) {
				//add them to the selected cmqs list
				for (CmqBase190 childCmq : childCmqsOftargets) {
					if(childCmq.getCmqState().equalsIgnoreCase(CmqBase190.CMQ_STATE_VALUE_PUBLISHED)
							&& childCmq.getCmqStatus().equalsIgnoreCase(CmqBase190.CMQ_STATUS_VALUE_ACTIVE)) {
						if(childCmq.getCmqNote() == null) {
							childCmq.setCmqNote("RETIREMENT REASON: " + getRetirementReason());
						} else {
							childCmq.setCmqNote(childCmq.getCmqNote() + "\n\n" + "RETIREMENT REASON: " + getRetirementReason());
						}
						targetCmqsSelected.add(childCmq);//we need to retire these if selected
					}
				}
 			}
			
			//continue
 			for (CmqBase190 cmqBase190 : targetCmqsSelected) {
 				cmqBase190.setCmqStatus(CmqBase190.CMQ_STATUS_VALUE_INACTIVE); 
 				cmqBase190.setLastModifiedDate(new Date());
				//cmqBase190.setLastModifiedBy("NONE");
				cmqBase190.setLastModifiedBy(lastModifiedByString);
			}
			try {
				this.cmqBaseService.update(targetCmqsSelected, this.authService.getUserCn()
						, this.authService.getUserGivenName(), this.authService.getUserSurName()
						, this.authService.getCombinedMappedGroupMembershipAsString());
				
				//update the dualListModel source and target
				init();
				String formatMsg = "The List(s) is retired successfully";
				
				if (targetCmqParents != null && !targetCmqParents.isEmpty())
 					formatMsg = "The List and retired parent list are retired successfully";
					
				//show messages on screen
				FacesContext.getCurrentInstance().addMessage(null, 
                        new FacesMessage(FacesMessage.SEVERITY_INFO,
                                formatMsg, ""));
				
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
				FacesContext.getCurrentInstance().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_ERROR,
                                "The system could not retire the following cmqs :" + codes, ""));
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
	
	public boolean getDeleteRelation() {
		return deleteRelation;
	}
	
	public void setDeleteRelation(boolean deleteRelation) {
		this.deleteRelation = deleteRelation;
	}
	
	public String getRetirementReason() {
		return retirementReason;
	}
	
	public void setRetirementReason(String retirementReason) {
		this.retirementReason = retirementReason;
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

	public boolean isChildNotSelected() {
		return childNotSelected;
	}

	public void setChildNotSelected(boolean childNotSelected) {
		this.childNotSelected = childNotSelected;
	}

	public AuthenticationService getAuthService() {
		return authService;
	}

	public ICmqBaseTargetService getCmqTargetService() {
		return cmqTargetService;
	}



	public void setCmqTargetService(ICmqBaseTargetService cmqTargetService) {
		this.cmqTargetService = cmqTargetService;
	}



	public void setAuthService(AuthenticationService authService) {
		this.authService = authService;
	} 
}
