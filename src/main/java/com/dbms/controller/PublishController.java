package com.dbms.controller;

import java.io.Serializable;
import java.math.BigDecimal;
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
import com.dbms.entity.cqt.RefConfigCodeList;
import com.dbms.service.AuthenticationService;
import com.dbms.service.ICmqBase190Service;
import com.dbms.service.ICmqBaseTargetService;
import com.dbms.service.ICmqRelation190Service;
import com.dbms.service.IRefCodeListService;
import com.dbms.util.exceptions.CqtServiceException;
import com.dbms.mail.EmailEntity;
import com.dbms.util.CqtConstants;
import com.dbms.util.SWJSFRequest;
import com.dbms.view.ListDetailsFormVM;

/**
 * @author Jay G.(jayshanchn@hotmail.com)
 * @date Feb 12, 2017 7:34:05 AM
 *
 */
@ManagedBean
@ViewScoped
public class PublishController implements Serializable {

    private static final long serialVersionUID = -443251941538546278L;

    private static final Logger LOG = LoggerFactory.getLogger(PublishController.class);

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

    @ManagedProperty("#{appSWJSFRequest}")
    private SWJSFRequest appSWJSFRequest;

    private ListDetailsFormVM detailsFormModel;

    private List<CmqBase190> sourceList;
    private List<CmqBase190> targetList;

    private List<CmqBaseTarget> sourceIAList;
    private List<CmqBaseTarget> targetIAList;

    private DualListModel<CmqBase190> publishCurrentVersionDualListModel;
    private DualListModel<CmqBaseTarget> publishFutureVersionDualListModel;

    private CmqBaseDualListConverter cmqBaseDualListConverter;
    private CmqBaseTargetDualListConverter cmqBaseTargetDualListConverter;

    @PostConstruct
    public void init() {
        this.detailsFormModel = new ListDetailsFormVM(this.authService, this.refCodeListService, this.appSWJSFRequest);
        sourceList = this.cmqBaseService.findApprovedCmqs();
        targetList = new ArrayList<CmqBase190>();
        sourceIAList = this.cmqBaseTargetService.findApprovedCmqs();
        targetIAList = new ArrayList<CmqBaseTarget>();
        publishCurrentVersionDualListModel = new DualListModel<CmqBase190>(sourceList, targetList);
        publishFutureVersionDualListModel = new DualListModel<CmqBaseTarget>(sourceIAList, targetIAList);
        this.cmqBaseDualListConverter = new CmqBaseDualListConverter();
        this.cmqBaseTargetDualListConverter = new CmqBaseTargetDualListConverter();
    }

    public String promoteTargetList() {
        List<Long> targetCmqCodes = new ArrayList<>();
        List<Long> targetCmqParentCodes = new ArrayList<>();
        List<CmqBase190> targetCmqsSelected = new ArrayList<>(publishCurrentVersionDualListModel.getTarget());
        for (CmqBase190 cmqBase : targetCmqsSelected) {
            targetCmqCodes.add(cmqBase.getCmqCode());
            if (null != cmqBase.getCmqParentCode()) {
                targetCmqParentCodes.add(cmqBase.getCmqParentCode());
            }
        }

        boolean isListPublishable = true;
        List<CmqBase190> faultyCmqs = new ArrayList<>();
        List<CmqBase190> childCmqsOftargets = this.cmqBaseService.findChildCmqsByCodes(targetCmqCodes);
        if (null != childCmqsOftargets) {
            for (CmqBase190 cmqBase190 : childCmqsOftargets) {
                //if child is not in the target list then check if its publisher or not
                if (!targetCmqCodes.contains(cmqBase190.getCmqCode())) {
                    if (!cmqBase190.getCmqState().equalsIgnoreCase(CmqBase190.CMQ_STATE_VALUE_PUBLISHED)
                            && cmqBase190.getCmqStatus().equalsIgnoreCase(CmqBase190.CMQ_STATUS_VALUE_PENDING)) {
                        isListPublishable = false;
                        faultyCmqs.add(cmqBase190);
                    }
                }
            }
        }

        if (!isListPublishable) {
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
                            "The List being promoted has an associated list that must be Promoted", ""));

            return "";
        } else {
            //now check the parents of these cmqs
            if (targetCmqParentCodes.size() > 0) {
                List<CmqBase190> parentCmqsList = this.cmqBaseService.findParentCmqsByCodes(targetCmqParentCodes);
                if (null != parentCmqsList) {
                    for (CmqBase190 cmqBase190 : parentCmqsList) {
                        //if parent is not in the target list then check if its publisher or not
                        if (!targetCmqCodes.contains(cmqBase190.getCmqCode())) {
                            if (!cmqBase190.getCmqState().equalsIgnoreCase(CmqBase190.CMQ_STATE_VALUE_PUBLISHED)
                                    && cmqBase190.getCmqStatus().equalsIgnoreCase(CmqBase190.CMQ_STATUS_VALUE_PENDING)) {
                                isListPublishable = false;
                                faultyCmqs.add(cmqBase190);
                            }
                        }
                    }
                }
            }

            if (!isListPublishable) {
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
                                "The list being promoted has an associated list that must be Promoted. ", ""));

                return "";
            } else {
                boolean hasErrorOccured = false;
                boolean hasParentError = false;
                String cmqError = "";
                //success
                for (CmqBase190 cmqBase190 : targetCmqsSelected) {
                    if (cmqBase190.getCmqLevel() == 2 && cmqBase190.getCmqParentCode() == null && cmqBase190.getCmqParentName() == null) {
                        hasParentError = true;
                    } else {
                        Date lastModifiedDate = new Date();
                        String lastModifiedByString = this.authService.getLastModifiedByUserAsString();
                        cmqBase190.setCmqState(CmqBase190.CMQ_STATE_VALUE_PUBLISHED);
                        //Pending to Active 'A'
                        cmqBase190.setCmqStatus(CmqBase190.CMQ_STATUS_VALUE_ACTIVE);
                        cmqBase190.setActivatedBy(lastModifiedByString);
                        cmqBase190.setActivationDate(lastModifiedDate);
                        cmqBase190.setLastModifiedDate(lastModifiedDate);
                        cmqBase190.setLastModifiedBy(lastModifiedByString);
                    }
                    if (hasParentError) {
                        cmqError = cmqBase190.getCmqName();
                        break;
                    }
                }
                if (hasParentError) {
                    FacesContext.getCurrentInstance().addMessage(null,
                            new FacesMessage(FacesMessage.SEVERITY_ERROR,
                                    "The List '" + cmqError + "' does not have an associated parent list, hence cannot be Published", ""));

                    return "";
                }

                try {
                    Date lastModifiedDate = new Date();
                    String lastModifiedByString = this.authService.getLastModifiedByUserAsString();
                    for (CmqBase190 cmqBase190 : targetCmqsSelected) {
                        cmqBase190.setLastModifiedBy(lastModifiedByString);
                        cmqBase190.setLastModifiedDate(lastModifiedDate);
                        if (StringUtils.isBlank(cmqBase190.getCreatedBy()) || cmqBase190.getCreationDate() == null) {
                            cmqBase190.setCreatedBy(lastModifiedByString);
                            cmqBase190.setCreationDate(lastModifiedDate);
                        }

                        RefConfigCodeList dictionary = refCodeListService.getCurrentMeddraVersion();

                        //Subversion increment - Dictionary version
                        if (cmqBase190.getCmqSubversion() != null && dictionary != null) {
                            System.out.println("\n *******************  subversion final " + cmqBase190.getCmqSubversion());
                            if (!cmqBase190.getDictionaryVersion().equals(dictionary.getValue())) {
                                cmqBase190.setCmqSubversion(new BigDecimal(1));
                                cmqBase190.setDictionaryVersion(dictionary.getValue());
                            } else {
                                cmqBase190.setCmqSubversion(cmqBase190.getCmqSubversion().add(new BigDecimal(0.01)));

                            }
                            System.out.println("\n *******************  subversion final " + cmqBase190.getCmqSubversion());
                        }
                    }
                    this.cmqBaseService.update(targetCmqsSelected, this.authService.getUserCn(),
                            this.authService.getUserGivenName(), this.authService.getUserSurName(),
                            this.authService.getCombinedMappedGroupMembershipAsString());
                } catch (CqtServiceException e) {
                    LOG.error(e.getMessage(), e);
                    hasErrorOccured = true;
                }

                if (hasErrorOccured) {
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
                                    "The system could not publish the following cmqs :" + codes, ""));
                } else {
                    emailer("Published");
                    //update the dualListModel source and target
                    init();

                    //show messages on screen
                    FacesContext.getCurrentInstance().addMessage(null,
                            new FacesMessage(FacesMessage.SEVERITY_INFO,
                                    "The List(s) were successfully Published", ""));

                }
            }
        }//end 

        targetCmqsSelected = new ArrayList<CmqBase190>();

        return "";
    }

    private void emailer(String state) {

        String smtp_host = "";
        String smtp_port = "";
        String username = "";
        String password = "";

        String adminEmailAddress = "";

        List<RefConfigCodeList> smtpConfigCodeList = refCodeListService.findSmtpServerConfig();
        for (RefConfigCodeList refConfigCodeList : smtpConfigCodeList) {
            String codeInternalValue = refConfigCodeList.getCodelistInternalValue();
            if ("SERVER_NM".equalsIgnoreCase(codeInternalValue)) {
                smtp_host = refConfigCodeList.getValue();
            } else if ("SERVER_PORT".equalsIgnoreCase(codeInternalValue)) {
                smtp_port = refConfigCodeList.getValue();
            }
        }

        List<RefConfigCodeList> senderConfigCodeList = refCodeListService.findSenderConfig();
        for (RefConfigCodeList refConfigCodeList : senderConfigCodeList) {
            String codeInternalValue = refConfigCodeList.getCodelistInternalValue();
            if ("SMTP_ADDRESS".equalsIgnoreCase(codeInternalValue)) {
                username = refConfigCodeList.getValue();
            } else if ("USER_PASSWORD".equalsIgnoreCase(codeInternalValue)) {
                password = refConfigCodeList.getValue();
            }
        }

        RefConfigCodeList adminEmailCodeList = refCodeListService.findByCriterias(CqtConstants.CODE_LIST_TYPE_USER_EMAIL_ADDRESS,
                "ADMIN_EMAIL", "Y");
        adminEmailAddress = adminEmailCodeList.getValue();

        List<CmqBase190> targetCmqsSelected = new ArrayList<>(publishCurrentVersionDualListModel.getTarget());
        for (CmqBase190 cmqBase190 : targetCmqsSelected) {

            if (cmqBase190.getCmqTypeCd().equalsIgnoreCase("TME")) {
                LOG.info("\n READY TO SEND EMAIL,CURRENT STATE :" + cmqBase190.getCmqState());
                List<String> recipients = new ArrayList<String>();
                if (!StringUtils.isEmpty(adminEmailAddress)) {
                    recipients.add(adminEmailAddress);
                }

                String designee = this.detailsFormModel.getEmailAddressFromUsername(cmqBase190.getCmqDesignee());
                String designee2 = this.detailsFormModel.getEmailAddressFromUsername(cmqBase190.getCmqDesignee2());
                String designee3 = this.detailsFormModel.getEmailAddressFromUsername(cmqBase190.getCmqDesignee3());

                if (!StringUtils.isEmpty(designee)) {
                    recipients.add(designee);
                }
                if (!StringUtils.isEmpty(designee2)) {
                    recipients.add(designee2);
                }
                if (!StringUtils.isEmpty(designee3)) {
                    recipients.add(designee3);
                }

                String subject = cmqBase190.getCmqName() + " " + cmqBase190.getCmqTypeCd()
                        + " " + refCodeListService.findByCriterias(CqtConstants.CODE_LIST_TYPE_EMAIL_SUBJECT,
                        "WORLKFLOW_ST3", "Y").getValue();
                
                String textMessage = refCodeListService.findByCriterias(CqtConstants.CODE_LIST_TYPE_EMAIL_NOTIFICATION_MSG,
                        "WORLKFLOW_ST3", "Y").getValue();

                EmailEntity email = new EmailEntity(smtp_host, smtp_port, username, password,
                        recipients, subject, textMessage);

                try {
                    email.sendEmail();
                } catch (Exception e) {
                    e.printStackTrace();
                    FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "An error occurred while sending the email", "");
                    FacesContext ctx = FacesContext.getCurrentInstance();
                    ctx.addMessage(null, msg);
                }
            }

        }

    }

    /**
     * Event when we pick on the source list
     *
     * @param event
     */
    public void pickList() {
        if (publishFutureVersionDualListModel.getTarget().isEmpty()) {
            FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR,
                    "Please select at least 1 list to promote.", "");
            FacesContext.getCurrentInstance().addMessage(null, msg);

        } else {
            RequestContext.getCurrentInstance().execute("PF('confirmPromote').show();");
        }
    }

    /**
     * Publish IA list. // OLD CODE
     *
     * @return
     */
    /*public String promoteIATargetList() {
		List<Long> targetCmqCodes = new ArrayList<>();
		List<Long> targetCmqParentCodes = new ArrayList<>();
		List<CmqBaseTarget> targetCmqsSelected = new ArrayList<>(publishFutureVersionDualListModel.getTarget());
		for (CmqBaseTarget cmqBase : targetCmqsSelected) {
			targetCmqCodes.add(cmqBase.getCmqCode());
			if(null != cmqBase.getCmqParentCode()) {
				targetCmqParentCodes.add(cmqBase.getCmqParentCode());
			}
		}
		
		boolean isListPublishable = true;
		List<CmqBaseTarget> faultyCmqs = new ArrayList<>();
		List<CmqBaseTarget> childCmqsOftargets = this.cmqBaseTargetService.findChildCmqsByCodes(targetCmqCodes);
		if(null != childCmqsOftargets) {
			for (CmqBaseTarget cmqBaseTarget : childCmqsOftargets) {
				//if child is not in the target list then check if its publisher or not
				if(!targetCmqCodes.contains(cmqBaseTarget.getCmqCode())) {
					if(!cmqBaseTarget.getCmqState().equalsIgnoreCase(CmqBaseTarget.CMQ_STATE_PUBLISHED_IA)
							&& cmqBaseTarget.getCmqStatus().equalsIgnoreCase(CmqBaseTarget.CMQ_STATE_PENDING_IA)) {
						isListPublishable = false;
						faultyCmqs.add(cmqBaseTarget);
					}
				}
			}
		}
		
		if(!isListPublishable) {
			String codes = "";
			if (faultyCmqs != null) {
				for (CmqBaseTarget cmq : faultyCmqs) {
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
				List<CmqBaseTarget> parentCmqsList = this.cmqBaseTargetService.findParentCmqsByCodes(targetCmqParentCodes);
				if(null != parentCmqsList) {
					for (CmqBaseTarget cmqBaseTarget : parentCmqsList) {
						//if parent is not in the target list then check if its publisher or not
						if(!targetCmqCodes.contains(cmqBaseTarget.getCmqCode())) {
							if(!cmqBaseTarget.getCmqState().equalsIgnoreCase(CmqBaseTarget.CMQ_STATE_PUBLISHED_IA)
									&& cmqBaseTarget.getCmqStatus().equalsIgnoreCase(CmqBaseTarget.CMQ_STATE_PENDING_IA)) {
								isListPublishable = false;
								faultyCmqs.add(cmqBaseTarget);
							}
						}
					}
				}
			}
			
			if(!isListPublishable) {
				//show error dialog with names of faulty cmqs
				String codes = "";
				if (faultyCmqs != null) {
					for (CmqBaseTarget cmq : faultyCmqs) {
						codes += cmq.getCmqCode() + ";";
					}
				}
				LOG.info("\n\n ******  " + codes); 
				//show error dialog with names of faulty cmqs
				FacesContext.getCurrentInstance().addMessage(null, 
                        new FacesMessage(FacesMessage.SEVERITY_ERROR,
                                "The list being promoted has an associated list that must be Promoted. ", ""));
				
				return "";
			} else {
				boolean hasErrorOccured = false;
				boolean hasParentError = false;
				String cmqError = "";
				//success
				for (CmqBaseTarget cmqBaseTarget : targetCmqsSelected) {
					if (cmqBaseTarget.getCmqLevel() == 2 && cmqBaseTarget.getCmqParentCode() == null && cmqBaseTarget.getCmqParentName() == null)
						hasParentError = true;
					else {
						Date lastModifiedDate = new Date();
						String lastModifiedByString = this.authService.getLastModifiedByUserAsString();
						cmqBaseTarget.setCmqState(CmqBaseTarget.CMQ_STATE_PUBLISHED_IA);
						cmqBaseTarget.setActivatedBy(lastModifiedByString);
						cmqBaseTarget.setActivationDate(lastModifiedDate);
						cmqBaseTarget.setLastModifiedDate(lastModifiedDate);
						cmqBaseTarget.setLastModifiedBy(lastModifiedByString);
					}

					if (hasParentError) {
						cmqError = cmqBaseTarget.getCmqName();
						break;
					}
				}
				if (hasParentError) {
					FacesContext.getCurrentInstance().addMessage(null, 
                            new FacesMessage(FacesMessage.SEVERITY_ERROR,
                                    "The List '"+ cmqError + "' does not have an associated parent list, hence cannot be Published", ""));
					
					return "";
				}
				
				try {
					this.cmqBaseTargetService.update(targetCmqsSelected, this.authService.getUserCn()
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
						for (CmqBaseTarget cmq : targetCmqsSelected) {
							codes += cmq.getCmqCode() + ";";
						}
					}
					//show error dialog with names of faulty cmqs
					FacesContext.getCurrentInstance().addMessage(null, 
                            new FacesMessage(FacesMessage.SEVERITY_ERROR,
                                    "The system could not publish the following cmqs :" + codes, ""));
				} else {
					//update the dualListModel source and target
					init();
					
					//show messages on screen
					FacesContext.getCurrentInstance().addMessage(null, 
                            new FacesMessage(FacesMessage.SEVERITY_INFO,
                                    "The List(s) were successfully Published", ""));
				}
			}
		}//end 
		targetCmqsSelected = new ArrayList<CmqBaseTarget>();	
		
		return "";
	}
     */
    /**
     * Publish target List.
     *
     * @return
     */
    public String promoteIATargetList() {
        List<Long> targetCmqCodes = new ArrayList<>();
        List<Long> targetCmqParentCodes = new ArrayList<>();
        List<CmqBaseTarget> targetCmqsSelected = new ArrayList<>(publishFutureVersionDualListModel.getTarget());
        for (CmqBaseTarget cmqBase : targetCmqsSelected) {
            targetCmqCodes.add(cmqBase.getCmqCode());
            if (null != cmqBase.getCmqParentCode()) {
                targetCmqParentCodes.add(cmqBase.getCmqParentCode());
            }
        }

        boolean isListPublishable = true;
        List<CmqBaseTarget> childCmqsOftargets = this.cmqBaseTargetService.findChildCmqsByCodes(targetCmqCodes);
        if (null != childCmqsOftargets) {
            for (CmqBaseTarget cmqBaseTarget : childCmqsOftargets) {
                //if child is not in the target list then check if its publisher or not
                if (!targetCmqCodes.contains(cmqBaseTarget.getCmqCode()) && (cmqBaseTarget.getCmqLevel() == 1 || cmqBaseTarget.getCmqLevel() == 2)) {
                    if (!cmqBaseTarget.getCmqState().equalsIgnoreCase(CmqBaseTarget.CMQ_STATE_PUBLISHED_IA)
                            && cmqBaseTarget.getCmqStatus().equalsIgnoreCase(CmqBase190.CMQ_STATUS_VALUE_PENDING)) {
                        isListPublishable = false;
                    }
                }
            }
        }

        if (!isListPublishable) {
            //show error dialog 
            FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR,
                    "The list being promoted has an associated list that must be Promoted", "");
            FacesContext.getCurrentInstance().addMessage(null, msg);

            return "";
        } else {
            //now check the parents of these cmqs
            if (targetCmqParentCodes.size() > 0) {
                List<CmqBaseTarget> parentCmqsList = this.cmqBaseTargetService.findParentCmqsByCodes(targetCmqParentCodes);
                if (null != parentCmqsList) {
                    for (CmqBaseTarget cmqBaseTarget : parentCmqsList) {
                        //if parent is not in the target list then check if its publisher or not
                        if (!targetCmqCodes.contains(cmqBaseTarget.getCmqCode())) {
                            if (!cmqBaseTarget.getCmqState().equalsIgnoreCase(CmqBaseTarget.CMQ_STATE_PUBLISHED_IA)
                                    && cmqBaseTarget.getCmqStatus().equalsIgnoreCase(CmqBase190.CMQ_STATUS_VALUE_PENDING)) {
                                isListPublishable = false;
                            }
                        }
                    }
                }
            }

            if (!isListPublishable) {
                //show error dialog with names
                FacesContext.getCurrentInstance().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_ERROR,
                                "The list being promoted has an associated list that must be Promoted. ", ""));

                return "";
            } else {
                boolean hasErrorOccured = false;
                boolean hasParentError = false;
                String cmqError = "";
                //success
                for (CmqBaseTarget cmqBaseTarget : targetCmqsSelected) {
//					if ((cmqBaseTarget.getCmqLevel() == 2 || cmqBaseTarget.getCmqLevel() == 1) 
//							&& cmqBaseTarget.getCmqParentCode() == null && cmqBaseTarget.getCmqParentName() == null)
//						hasParentError = true;
//					else {
                    Date lastModifiedDate = new Date();
                    String lastModifiedByString = this.authService.getLastModifiedByUserAsString();
                    cmqBaseTarget.setCmqState(CmqBaseTarget.CMQ_STATE_PUBLISHED_IA);
                    cmqBaseTarget.setActivatedBy(lastModifiedByString);
                    cmqBaseTarget.setActivationDate(lastModifiedDate);
                    cmqBaseTarget.setLastModifiedDate(lastModifiedDate);
                    cmqBaseTarget.setLastModifiedBy(lastModifiedByString);
                    //}

//					if (hasParentError) {
//						cmqError = cmqBaseTarget.getCmqName();
//						break;
//					}
                }
                if (hasParentError) {
                    FacesContext.getCurrentInstance().addMessage(null,
                            new FacesMessage(FacesMessage.SEVERITY_ERROR,
                                    "The list being promoted has an associated list that must be Promoted.", ""));

                    return "";
                }

                try {
                    this.cmqBaseTargetService.update(targetCmqsSelected, this.authService.getUserCn(),
                            this.authService.getUserGivenName(), this.authService.getUserSurName(),
                            this.authService.getCombinedMappedGroupMembershipAsString());
                } catch (CqtServiceException e) {
                    LOG.error(e.getMessage(), e);
                    hasErrorOccured = true;
                }

                if (hasErrorOccured) {
                    //show error message popup for partial success.
                    String codes = "";
                    if (targetCmqsSelected != null) {
                        for (CmqBaseTarget cmq : targetCmqsSelected) {
                            codes += cmq.getCmqCode() + ";";
                        }
                    }
                    //show error dialog with names of faulty cmqs
                    FacesContext.getCurrentInstance().addMessage(null,
                            new FacesMessage(FacesMessage.SEVERITY_ERROR,
                                    "The system could not publish the following cmqs :" + codes, ""));
                } else {
                    //update the dualListModel source and target
                    init();

                    //show messages on screen
                    FacesContext.getCurrentInstance().addMessage(null,
                            new FacesMessage(FacesMessage.SEVERITY_INFO,
                                    "The List(s) are Published successfully", ""));
                }
            }
        }//end 
        targetCmqsSelected = new ArrayList<CmqBaseTarget>();

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

    public CmqBaseDualListConverter getCmqBaseDualListConverter() {
        return cmqBaseDualListConverter;
    }

    public void setCmqBaseDualListConverter(CmqBaseDualListConverter cmqBaseDualListConverter) {
        this.cmqBaseDualListConverter = cmqBaseDualListConverter;
    }

    private class CmqBaseDualListConverter implements Converter {

        @Override
        public Object getAsObject(FacesContext context, UIComponent component, String value) {
            long inputValue = 0;
            try {
                inputValue = Long.valueOf(value);
            } catch (Exception e) {
                LOG.error(e.getMessage(), e);
            }
            /*PickList p = (PickList) component;
		    DualListModel<CmqBase190> dl = (DualListModel) p.getValue();
			List<CmqBase190> sourceList = dl.getSource();*/
            for (CmqBase190 cmqBase190 : sourceList) {
                if (cmqBase190.getCmqCode().longValue() == inputValue) {
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
            try {
                inputValue = Long.valueOf(value);
            } catch (Exception e) {
                LOG.error(e.getMessage(), e);
            }
            for (CmqBaseTarget cmqBaseTarget : sourceIAList) {
                if (cmqBaseTarget.getCmqCode().longValue() == inputValue) {
                    return cmqBaseTarget;
                }
            }
            return null;
        }

        @Override
        public String getAsString(FacesContext context, UIComponent component, Object value) {
            return value.toString();
        }
    }

    public DualListModel<CmqBaseTarget> getPublishFutureVersionDualListModel() {
        return publishFutureVersionDualListModel;
    }

    public void setPublishFutureVersionDualListModel(
            DualListModel<CmqBaseTarget> publishFutureVersionDualListModel) {
        this.publishFutureVersionDualListModel = publishFutureVersionDualListModel;
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

    public ICmqBaseTargetService getCmqBaseTargetService() {
        return cmqBaseTargetService;
    }

    public void setCmqBaseTargetService(ICmqBaseTargetService cmqBaseTargetService) {
        this.cmqBaseTargetService = cmqBaseTargetService;
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

}
