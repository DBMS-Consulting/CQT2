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

import org.primefaces.context.RequestContext;
import org.primefaces.model.DualListModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dbms.entity.cqt.CmqBase190;
import com.dbms.entity.cqt.CmqBaseTarget;
import com.dbms.service.AuthenticationService;
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

	@ManagedProperty("#{AuthenticationService}")
	private AuthenticationService authService;
	
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
	
	
	public String reactivateTargetList() {
		int cptChildren = 0;
		List<Long> targetCmqCodes = new ArrayList<>();
		List<Long> targetCmqParentCodes = new ArrayList<>();
		List<CmqBase190> targetCmqsSelected = new ArrayList<>(reactivateDualListModel.getTarget());
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
				//if child is not in the target list then check if its reactivated or not
				if(!targetCmqCodes.contains(cmqBase190.getCmqCode())) {
					cptChildren++;
					if(!cmqBase190.getCmqState().equalsIgnoreCase(CmqBase190.CMQ_STATE_VALUE_PUBLISHED)
							&& cmqBase190.getCmqStatus().equalsIgnoreCase(CmqBase190.CMQ_STATUS_VALUE_INACTIVE)) {
						isListPublishable = false;
						faultyCmqs.add(cmqBase190);  
					}
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
			FacesContext.getCurrentInstance().addMessage(null, 
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "The list being reactivated has an associated list that must be reactivated", ""));
			
			return "";
		} else {
			//now check the parents of these cmqs
			if(targetCmqParentCodes.size() > 0) {
				List<CmqBase190> parentCmqsList = this.cmqBaseService.findParentCmqsByCodes(targetCmqParentCodes);
				if(null != parentCmqsList) {
					for (CmqBase190 cmqBase190 : parentCmqsList) {
						//if parent is not in the target list then check if its reactivated or not
						if(!targetCmqCodes.contains(cmqBase190.getCmqCode()) && (!cmqBase190.getCmqStatus().equalsIgnoreCase(CmqBase190.CMQ_STATUS_VALUE_ACTIVE))) {
							cptChildren++;
							//if() {
								isListPublishable = false;
								faultyCmqs.add(cmqBase190);
								
							//}
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
				FacesContext.getCurrentInstance().addMessage(null, 
                        new FacesMessage(FacesMessage.SEVERITY_ERROR,
                                "The list being reactivated has an associated list that must be reactivated. ", ""));
				
				return "";
			} else if (cptChildren > 0) {
				FacesContext.getCurrentInstance().addMessage(null, 
                        new FacesMessage(FacesMessage.SEVERITY_ERROR,
                                "The list being reactivated has an associated list that must be reactivated. ", ""));
				
				return "";
			} else {
			
				boolean hasErrorOccured = false;
				boolean hasParentError = false;
				String cmqError = "";
				//success
				for (CmqBase190 cmqBase190 : targetCmqsSelected) {
					if (cmqBase190.getCmqLevel() == 2 && cmqBase190.getCmqParentCode() == null && cmqBase190.getCmqParentName() == null)
						hasParentError = true;
					else {
						cmqBase190.setCmqState(CmqBase190.CMQ_STATE_VALUE_DRAFT);
	 					cmqBase190.setCmqStatus(CmqBase190.CMQ_STATUS_VALUE_PENDING); 
	 					cmqBase190.setLastModifiedDate(new Date());
	 					cmqBase190.setLastModifiedBy(authService.getUserCn());
	 					cmqBase190.setActivationDate(new Date());
	 					cmqBase190.setActivatedBy(authService.getUserCn());
	 					if (cmqBase190.getCmqApproveReason() == null)
	 						cmqBase190.setCmqApproveReason("");
	 					if (cmqBase190.getCmqDesignee2() == null)
	 						cmqBase190.setCmqDesignee2("");
	 					if (cmqBase190.getCmqDesignee3() == null)
	 						cmqBase190.setCmqDesignee3("");
					}

					if (hasParentError) {
						cmqError = cmqBase190.getCmqName();
						break;
					}
				}
				if (hasParentError) {
					FacesContext.getCurrentInstance().addMessage(null, 
                            new FacesMessage(FacesMessage.SEVERITY_ERROR,
                                    "The List '"+ cmqError + "' does not have an associated parent list, hence cannot be reactivated", ""));
					
					return "";
				} else {
					for(CmqBase190 target : targetCmqsSelected) {
						if (!cmqBaseService.checkIfInactiveFor10Mins(target.getCmqCode())) {
							FacesContext.getCurrentInstance().addMessage(null, 
		                            new FacesMessage(FacesMessage.SEVERITY_ERROR,
		                                    "List(s) can be reactivated only after 10 minutes of list inactivation. Please try again later.", ""));
							
							return "";
						}
					}
				
					
				}
				
				try {
					this.cmqBaseService.update(targetCmqsSelected, this.authService.getUserCn()
							, this.authService.getUserGivenName(), this.authService.getUserSurName()
							, this.authService.getCombinedMappedGroupMembershipAsString());
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
					FacesContext.getCurrentInstance().addMessage(null, 
                            new FacesMessage(FacesMessage.SEVERITY_ERROR,
                                    "The system could not reactivate the following cmqs :" + codes, ""));
				} else {
					//update the dualListModel source and target
					init();
					
					//show messages on screen
					FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_INFO,
							"The List(s) were successfully Reactivated", "");
					FacesContext.getCurrentInstance().addMessage(null, msg);
				}
			}
		}//end 
		
		targetCmqsSelected = new ArrayList<CmqBase190>();
		
		return "";
	}

	private boolean isSelected(Long cmqCode, List<CmqBase190> targetCmqsSelected) {
		int cpt = 0;
		for (CmqBase190 cmq : targetCmqsSelected)
			if (cmq.getCmqCode().equals(cmqCode)) 
				cpt++;
		if (cpt > 0)
			return true;
		return false;
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


	public AuthenticationService getAuthService() {
		return authService;
	}


	public void setAuthService(AuthenticationService authService) {
		this.authService = authService;
	} 
}
