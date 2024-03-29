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

import com.dbms.entity.cqt.CmqBaseTarget;
import com.dbms.service.*;
import org.primefaces.context.RequestContext;
import org.primefaces.model.DualListModel;
import org.primefaces.model.TreeNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dbms.csmq.HierarchyNode;
import com.dbms.entity.cqt.CmqBase190;
import com.dbms.util.SWJSFRequest;
import com.dbms.util.exceptions.CqtServiceException;
import com.dbms.view.ListRelationsVM;

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

	@ManagedProperty("#{CmqBaseTargetService}")
	private ICmqBaseTargetService cmqBaseTargetService;

	@ManagedProperty("#{CmqRelation190Service}")
	private ICmqRelation190Service cmqRelationService;

	@ManagedProperty("#{RefCodeListService}")
	private IRefCodeListService refCodeListService;

	@ManagedProperty("#{AuthenticationService}")
	private AuthenticationService authService;
	
	@ManagedProperty("#{SmqBaseService}")
	private ISmqBaseService smqBaseService;
	
	@ManagedProperty("#{appSWJSFRequest}")
    private SWJSFRequest appSWJSFRequest;
	
	@ManagedProperty("#{MeddraDictService}")
	private IMeddraDictService meddraDictService;
	
	@ManagedProperty("#{globalController}")
    private GlobalController globalController;
	
	
	private List<CmqBase190> sourceListToReactivate;

	private List<CmqBase190> targetList;

	private DualListModel<CmqBase190> reactivateDualListModel;

	private CmqBaseDualListConverter cmqBaseDualListConverter;
	
	private ListRelationsVM relationsModel;
	
	private String confirmMessage;
	
	@PostConstruct
	public void init() {
		sourceListToReactivate = this.cmqBaseService.findCmqsToReactivate();
		targetList = new ArrayList<CmqBase190>();
		reactivateDualListModel = new DualListModel<CmqBase190>(
				sourceListToReactivate, targetList);
		this.cmqBaseDualListConverter = new CmqBaseDualListConverter();
		this.relationsModel = new ListRelationsVM(authService, appSWJSFRequest, refCodeListService, cmqBaseService, smqBaseService, meddraDictService, cmqRelationService, globalController);
	}
	
	//Method checks to see if any of the lists are not the current meddra version and redirects appropriately.
	//If one or more of the target lists is not the current meddra version, a different reactivation function is 
	//called through the UI
	public void checkForReactivateList() {
		Boolean listsCurrent = true;
		List<CmqBase190> targetCmqsSelected = new ArrayList<>(reactivateDualListModel.getTarget());
		for (CmqBase190 cmqBase : targetCmqsSelected) {
			this.relationsModel.setClickedCmqCode(cmqBase.getCmqCode());
			/*List<TreeNode> test = relationsModel.getRelationsRoot().getChildren();
			for(TreeNode child: test) {
				HierarchyNode c = (HierarchyNode) child.getData();
				if(c.isDTR()) {
					relationsModel.deleteRelation(child, c, cmqBase.getCmqCode());
				}
			} */

			if(Integer.parseInt(cmqBase.getDictionaryVersion()) < Integer.parseInt(refCodeListService.getCurrentMeddraVersion().getValue())) {
				listsCurrent = false;
			}
		}

		if(listsCurrent) {
			reactivateTargetList();
		} else {
			RequestContext context = RequestContext.getCurrentInstance();
		    context.execute("PF('meddraConfirmButton').jq.click();");
		}
	}
	
	public String reactivateNonCurrentMeddraTargetList() {
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
				
				for(CmqBase190 target : targetCmqsSelected) {
					if (!cmqBaseService.checkIfInactiveFor10Mins(target.getCmqCode())) {
						FacesContext.getCurrentInstance().addMessage(null, 
	                            new FacesMessage(FacesMessage.SEVERITY_ERROR,
	                                    "List(s) can be reactivated only after 10 minutes of list inactivation. Please try again later.", ""));
						
						return "";
					}
				}
				
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
	 					cmqBase190.setDictionaryVersion(refCodeListService.getCurrentMeddraVersion().getValue());
	 					if (cmqBase190.getCmqApproveReason() == null)
	 						cmqBase190.setCmqApproveReason("");
	 					if (cmqBase190.getCmqDesignee2() == null)
	 						cmqBase190.setCmqDesignee2("");
	 					if (cmqBase190.getCmqDesignee3() == null)
	 						cmqBase190.setCmqDesignee3("");
	 					
	 					//sets the relation model for each targetCmq
	 					//gets a list of its children
	 					//checks each child if DTR and deletes them if they are
 						this.relationsModel.setClickedCmqCode(cmqBase190.getCmqCode());
 						List<TreeNode> children = relationsModel.getRelationsRoot().getChildren();
 						for(int i = children.size() - 1; i > -1; i--) {
 							TreeNode child = children.get(i);
 							HierarchyNode c = (HierarchyNode) child.getData();
 							if(c.isDTR()) {
 								relationsModel.deleteRelation(relationsModel.getRelationsRoot(), c, cmqBase190.getCmqCode());
 							}
 						}	 					
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
				} /*else {
					for(CmqBase190 target : targetCmqsSelected) {
						if (!cmqBaseService.checkIfInactiveFor10Mins(target.getCmqCode())) {
							FacesContext.getCurrentInstance().addMessage(null, 
		                            new FacesMessage(FacesMessage.SEVERITY_ERROR,
		                                    "List(s) can be reactivated only after 10 minutes of list inactivation. Please try again later.", ""));
							
							return "";
						}
					}
				
					
				} */
				
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

						CmqBaseTarget targetList = cmqBaseTargetService.findByCode(target.getCmqCode());
						if (targetList != null) {
							//System.out.println(" Target code, name: " + target.getCmqCode() + ", " + target.getCmqName());
							FacesContext.getCurrentInstance().addMessage(null,
									new FacesMessage(FacesMessage.SEVERITY_WARN,
											"The List exists in the target view. Please check if Batch Impact job is running successfully.", ""));

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

	public IMeddraDictService getMeddraDictService() {
		return meddraDictService;
	}

	public void setMeddraDictService(IMeddraDictService meddraDictService) {
		this.meddraDictService = meddraDictService;
	}
	
	public GlobalController getGlobalController() {
		return globalController;
	}

	public void setGlobalController(GlobalController globalController) {
		this.globalController = globalController;
	}

	public IRefCodeListService getRefCodeListService() {
		return refCodeListService;
	}

	public void setRefCodeListService(IRefCodeListService refCodeListService) {
		this.refCodeListService = refCodeListService;
	}
	
	//ISmqBaseService smqBaseService;
	public ISmqBaseService getSmqBaseService() {
		return smqBaseService;
	}

	public void setSmqBaseService(ISmqBaseService smqBaseService) {
		this.smqBaseService = smqBaseService;
	}
	
	public SWJSFRequest getAppSWJSFRequest() {
		return appSWJSFRequest;
	}

	public void setAppSWJSFRequest(SWJSFRequest appSWJSFRequest) {
		this.appSWJSFRequest = appSWJSFRequest;
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

	public ICmqBaseTargetService getCmqBaseTargetService() {
		return cmqBaseTargetService;
	}

	public void setCmqBaseTargetService(ICmqBaseTargetService cmqBaseTargetService) {
		this.cmqBaseTargetService = cmqBaseTargetService;
	}
}
