package com.dbms.controller;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.ViewScoped;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.event.AjaxBehaviorEvent;
import javax.faces.validator.ValidatorException;

import org.apache.commons.lang3.StringUtils;
import org.primefaces.context.RequestContext;
import org.primefaces.model.StreamedContent;

import com.dbms.entity.cqt.RefConfigCodeList;
import com.dbms.service.AuthenticationService;
import com.dbms.service.ICqtCacheManager;
import com.dbms.service.IRefCodeListService;
import com.dbms.util.CqtConstants;
import com.dbms.util.OrderBy;
import com.dbms.util.exceptions.CqtServiceException;
import com.dbms.web.dto.CodelistDTO;

/**
 * @date Feb 7, 2017 7:39:34 AM
 **/
@ManagedBean
@ViewScoped
public class AdminController implements Serializable {

	/**
	 * serialVersionUID.
	 */
	private static final long serialVersionUID = 1085292862045772511L;
	//
	// private static final String CODELIST_EXTENSION = "EXTENSION";
	// private static final String CODELIST_PRODUCT = "PRODUCT";
	// private static final String CODELIST_PROGRAM = "PROGRAM";
	// private static final String CODELIST_PROTOCOL = "PROTOCOL";
	// private static final String CODELIST_MEDDRA = "MEDDRA";
	// private static final String CODELIST_WORKFLOW = "WORKFLOW";
	// private static final String CODELIST_USERGROUP = "USER_GROUPS";
	// private static final String CODELIST_SYSCONFIG = "SYSTEM_CONFIG";
	// private static final String CODELIST_CMQ_RELATION_IMPACT_TYPE =
	// "CMQ_RELATION_IMPACT_TYPE";
	// private static final String CODELIST_SMQ_RELATION_IMPACT_TYPE =
	// "SMQ_RELATION_IMPACT_TYPE";
	// private static final String CODELIST_MEDDRA_DICT_IMPACT_TYPE =
	// "MEDDRA_DICT_IMPACT_TYPE";
	// private static final String CODELIST_DICTIONARY_LEVELS_TYPE =
	// "DICTIONARY_CMQ_LEVELS";
	// private static final String CODELIST_SMQ_LEVELS_TYPE =
	// "SMQ_FILTER_LEVELS";

	@ManagedProperty("#{CqtCacheManager}")
	private ICqtCacheManager cqtCacheManager;
	private final String CACHE_NAME = "code-list-cache";

	List<CodelistDTO> list;
	private String codelist;

	@ManagedProperty("#{RefCodeListService}")
	private IRefCodeListService refCodeListService;

	@ManagedProperty("#{AuthenticationService}")
	private AuthenticationService authService;
	

	private List<RefConfigCodeList> extensions, programs, protocols, products,
			meddras, workflows, usergroups, sysconfigs, cmqImpactTypes,
			smqImpactTypes, meddraImpactTypes, levels, smqfilters;
	
	private List<RefConfigCodeList> codelistType;

	private RefConfigCodeList selectedRow, myFocusRef, myCodelist;
	private StreamedContent excelFile;

	public AdminController() {
		codelist = CqtConstants.CODE_LIST_TYPE_CMQ_RELATION_IMPACT_TYPE;
	}

	@PostConstruct
	public void init() {
		codelist = CqtConstants.CODE_LIST_TYPE_CMQ_RELATION_IMPACT_TYPE;
		getCodelistList();

		// Init add codelist
		this.initNewCodelist();
		
		FacesContext context = FacesContext.getCurrentInstance();
		ConfigurationController configMB = (ConfigurationController) context.getApplication().evaluateExpressionGet(context, "#{configMB}", ConfigurationController.class);
	}

	public String initNewCodelist() {
		myCodelist = new RefConfigCodeList();
		myCodelist.setCreationDate(new Date());
		myCodelist.setLastModificationDate(new Date());
		myCodelist.setSerialNum(new BigDecimal(1));
		myCodelist.setValue("");
		myCodelist.setCodelistInternalValue("");

		return "";
	}

	public String cancelCodelist() {
		this.initNewCodelist();
		FacesMessage msg = new FacesMessage(
				FacesMessage.SEVERITY_WARN,
				"Form canceled",
				"");
		FacesContext.getCurrentInstance().addMessage(null, msg);
		//RequestContext.getCurrentInstance().update("f1:codelistForm");
		return "";
	}


	public String addCodelist() {
		boolean codelistName = refCodeListService.existingCodelistByConfigType(myCodelist.getCodelistConfigType());
		if (codelistName) {
			FacesMessage msg = new FacesMessage(
					FacesMessage.SEVERITY_WARN,
					"This Codelist Name already exists",
					"");
			FacesContext.getCurrentInstance().addMessage(null, msg);
			return "";
		}
		boolean codelistValue = refCodeListService.existingCodelistByValue(myCodelist.getValue());
		if (codelistValue) {
			FacesMessage msg = new FacesMessage(
					FacesMessage.SEVERITY_WARN,
					"This Codelist Value already exists",
					"");
			FacesContext.getCurrentInstance().addMessage(null, msg);
			return "";
		}
		boolean codelistInternalValue = refCodeListService.existingCodelistByInternalValue(myCodelist.getCodelistInternalValue());
		if (codelistInternalValue) {
			FacesMessage msg = new FacesMessage(
					FacesMessage.SEVERITY_WARN,
					"This Codelist Internal Value already exists",
					"");
			FacesContext.getCurrentInstance().addMessage(null, msg);
			return "";
		}
		String lastModifiedByString = this.authService
				.getLastModifiedByUserAsString();
		myCodelist.setCreatedBy(lastModifiedByString);

		try {
			refCodeListService.create(myCodelist, this.authService
                            .getUserCn(), this.authService.getUserGivenName(),
                    this.authService.getUserSurName(), this.authService
                            .getCombinedMappedGroupMembershipAsString());
			this.initNewCodelist();
			getCodelistList();
			FacesMessage msg = new FacesMessage(
					FacesMessage.SEVERITY_INFO,
					"Codelist created successfully.",
					"");
			FacesContext.getCurrentInstance().addMessage(null, msg);
			//RequestContext.getCurrentInstance().update("f1:codelistForm");


		} catch (CqtServiceException e) {
			e.printStackTrace();
			FacesMessage msg = new FacesMessage(
					FacesMessage.SEVERITY_ERROR,
					"An error occured while creating a new codelist.",
					"");
			FacesContext.getCurrentInstance().addMessage(null, msg);
		}

		return "";
	}

	public void switchCodelist(AjaxBehaviorEvent event) {
		getCodelistList();

		myFocusRef = new RefConfigCodeList();
	}

	public List<RefConfigCodeList> getSMQFilters() {
		smqfilters = refCodeListService.findAllByConfigType(
				CqtConstants.CODE_LIST_TYPE_SMQ_FILTER_LEVELS, OrderBy.ASC);
		if (smqfilters == null) {
			smqfilters = new ArrayList<>();
		}
		return smqfilters;
	}

	public String initAddCodelist() {
		selectedRow = null;
		BigDecimal lastSerial = new BigDecimal(0);
		myFocusRef = new RefConfigCodeList();
		myFocusRef.setCodelistInternalValue("");
		myFocusRef.setValue("");
		
		myFocusRef.setCodelistConfigType(codelist);
		if(codelistType != null && !codelistType.isEmpty()) {
			lastSerial = codelistType.get(codelistType.size() - 1).getSerialNum();
		}
		System.out.println("\n #### lastSerial : " + lastSerial + "\n");

		myFocusRef.setCreationDate(new Date());
		myFocusRef.setLastModificationDate(new Date());
		myFocusRef.setSerialNum(lastSerial.add(new BigDecimal(1)));

		System.out.println("\n #### myFocusRef.getSerialNum() : " + myFocusRef.getSerialNum() + "\n");
		return "";
	}

	public String initGetRef() {
		myFocusRef = new RefConfigCodeList();
		if (selectedRow == null) {
			FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR,
					"No selection was performed. Try again", "");
			FacesContext.getCurrentInstance().addMessage(null, msg);
			return "";
		}

		myFocusRef = refCodeListService.findById(selectedRow.getId());
		if (myFocusRef == null) {
			FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR,
					"The codeList selected does not exist.", "");
			FacesContext.getCurrentInstance().addMessage(null, msg);
		}
		else
			myFocusRef = selectedRow;

		return "";
	}
	
	public List<RefConfigCodeList> getCodelistList(){
		codelistType = refCodeListService.findAllByConfigType(codelist, OrderBy.ASC);
		if(codelistType == null) {
			codelistType = new ArrayList<RefConfigCodeList>();
		}
		return codelistType;
	}

	public List<RefConfigCodeList> getExtensionList() {
		extensions = refCodeListService.findAllByConfigType(
				CqtConstants.CODE_LIST_TYPE_EXTENSION, OrderBy.ASC);
		if (extensions == null) {
			extensions = new ArrayList<RefConfigCodeList>();
		}
		return extensions;
	}

	/**
	 * Returns programs list.
	 * 
	 * @return
	 */
	public List<RefConfigCodeList> getProgramList() {
		programs = refCodeListService.findAllByConfigType(
				CqtConstants.CODE_LIST_TYPE_PROGRAM, OrderBy.ASC);
		if (programs == null) {
			programs = new ArrayList<RefConfigCodeList>();
		}
		return programs;
	}

	/**
	 * Returns protocol list.
	 * 
	 * @return
	 */
	public List<RefConfigCodeList> getProtocolList() {
		protocols = refCodeListService.findAllByConfigType(
				CqtConstants.CODE_LIST_TYPE_PROTOCOL, OrderBy.ASC);
		if (protocols == null) {
			protocols = new ArrayList<RefConfigCodeList>();
		}
		return protocols;
	}

	/**
	 * Returns Meddra codelist.
	 * 
	 * @return
	 */
	public List<RefConfigCodeList> getMeddraList() {
		meddras = refCodeListService.findAllByConfigType(
				CqtConstants.CODE_LIST_TYPE_MEDDRA_VERSIONS, OrderBy.ASC);
		if (meddras == null) {
			meddras = new ArrayList<RefConfigCodeList>();
		}
		return meddras;
	}

	/**
	 * Returns products list.
	 * 
	 * @return
	 */
	public List<RefConfigCodeList> getProductList() {
		products = refCodeListService.findAllByConfigType(
				CqtConstants.CODE_LIST_TYPE_PRODUCT, OrderBy.ASC);
		if (products == null) {
			products = new ArrayList<RefConfigCodeList>();
		}
		return products;
	}

	/**
	 * Returns Workflow State codelist.
	 * 
	 * @return
	 */
	public List<RefConfigCodeList> getWorkflowList() {
		workflows = refCodeListService.findAllByConfigType(
				CqtConstants.CODE_LIST_TYPE_WORKFLOW_STATES, OrderBy.ASC);
		if (workflows == null) {
			workflows = new ArrayList<RefConfigCodeList>();
		}
		return workflows;
	}

	/**
	 * Returns User Group codelist.
	 * 
	 * @return
	 */
	public List<RefConfigCodeList> getUsergroupList() {
		usergroups = refCodeListService.findAllByConfigType(
				CqtConstants.CODE_LIST_TYPE_USER_GROUPS, OrderBy.ASC);
		if (usergroups == null) {
			usergroups = new ArrayList<RefConfigCodeList>();
		}
		return usergroups;
	}

	/**
	 * Returns System Config codelist.
	 * 
	 * @return
	 */
	public List<RefConfigCodeList> getSysconfigList() {
		sysconfigs = refCodeListService.findAllByConfigType(
				CqtConstants.CODE_LIST_TYPE_SYSTEM_CONFIG, OrderBy.ASC);
		if (sysconfigs == null) {
			sysconfigs = new ArrayList<RefConfigCodeList>();
		}
		return sysconfigs;
	}

	/**
	 * Returns Dictionary level codelist.
	 * 
	 * @return
	 */
	public List<RefConfigCodeList> getLevelList() {
		levels = refCodeListService.findAllByConfigType(
				CqtConstants.CODE_LIST_TYPE_DICTIONARY_LEVELS, OrderBy.ASC);
		if (levels == null) {
			levels = new ArrayList<RefConfigCodeList>();
		}
		return levels;
	}

	/**
	 * Returns CMQ List Impact Type Config codelist.
	 * 
	 * @return
	 */
	public List<RefConfigCodeList> getCmqImpactTypeList() {
		cmqImpactTypes = refCodeListService.findAllByConfigType(
				CqtConstants.CODE_LIST_TYPE_CMQ_RELATION_IMPACT_TYPE,
				OrderBy.ASC);
		if (cmqImpactTypes == null) {
			cmqImpactTypes = new ArrayList<RefConfigCodeList>();
		}
		return cmqImpactTypes;
	}

	/**
	 * Returns SMQ List Impact Type Config codelist.
	 * 
	 * @return
	 */
	public List<RefConfigCodeList> getSmqImpactTypeList() {
		smqImpactTypes = refCodeListService.findAllByConfigType(
				CqtConstants.CODE_LIST_TYPE_SMQ_RELATION_IMPACT_TYPE,
				OrderBy.ASC);
		if (smqImpactTypes == null) {
			smqImpactTypes = new ArrayList<RefConfigCodeList>();
		}
		return smqImpactTypes;
	}

	/**
	 * Returns Meddra List Impact Type Config codelist.
	 * 
	 * @return
	 */
	public List<RefConfigCodeList> getMeddraImpactTypeList() {
		meddraImpactTypes = refCodeListService.findAllByConfigType(
				CqtConstants.CODE_LIST_TYPE_MEDDRA_DICT_IMPACT_TYPE,
				OrderBy.ASC);
		if (meddraImpactTypes == null) {
			meddraImpactTypes = new ArrayList<RefConfigCodeList>();
		}
		return meddraImpactTypes;
	}

	public String addRefCodelist() {
		if (myFocusRef.getCodelistConfigType().equals(CqtConstants.CODE_LIST_TYPE_EXTENSION)) {
			//validate the ref code. cannot be more than 3 characters long if codelist is extension
			if((myFocusRef.getCodelistInternalValue() != null) && (myFocusRef.getCodelistInternalValue().length() > 3)) {
				FacesMessage msg = new FacesMessage(
						FacesMessage.SEVERITY_ERROR,
						"Length of EXTENSION Code cannot exceed 3 characters.",
						"");
				FacesContext.getCurrentInstance().addMessage(null, msg);
				return "";
			}
		} else if(myFocusRef.getCodelistConfigType().equals(CqtConstants.CODE_LIST_TYPE_MEDDRA_VERSIONS)) {
			if(myFocusRef.getValue() != null) {
				try { 
			        Integer.parseInt(myFocusRef.getValue()); 
			    } catch(NumberFormatException e) { 
			    	RequestContext context = RequestContext.getCurrentInstance();
			    	FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Decimal is not allowed in the version number.", "");
					FacesContext.getCurrentInstance().addMessage(null, msg);
					context.addCallbackParam("validationFailed", true);
					return "";
			    } catch(NullPointerException e) {
			    	RequestContext context = RequestContext.getCurrentInstance();
			    	FacesMessage msg = new FacesMessage(
							FacesMessage.SEVERITY_ERROR,
							"MedDRA Value is required.",
							"");
					FacesContext.getCurrentInstance().addMessage(null, msg);
					context.addCallbackParam("validationFailed", true);
					return "";
			    }
			}
		}
		
		
		// Getting old serial number before save
		RefConfigCodeList oldCodelist = refCodeListService.findById(myFocusRef.getId());
		
		// check if changed
		if (myFocusRef.getId() != null) {
			RefConfigCodeList oldValue = refCodeListService.findById(myFocusRef
					.getId());
			if (oldValue != null
					&& myFocusRef.getActiveFlag().equals(
							oldValue.getActiveFlag())
					&& myFocusRef.getCodelistInternalValue().equals(
							oldValue.getCodelistInternalValue())
					&& myFocusRef.getSerialNum()
							.equals(oldValue.getSerialNum())
					&& myFocusRef.getDefaultFlag().equals(
							oldValue.getDefaultFlag())
					&& myFocusRef.getValue().equals(oldValue.getValue())) {
				// Nothing has been changed
				return "";
			}
		}

		boolean saved = false;
		Date lastModifiedDate = new Date();
		String lastModifiedByString = this.authService
				.getLastModifiedByUserAsString();

		RefConfigCodeList searchRefByCode = refCodeListService.findByCriterias(
				myFocusRef.getCodelistConfigType(),
				myFocusRef.getCodelistInternalValue(), "Y");

		if (myFocusRef.getCodelistInternalValue() != null
				&& !myFocusRef.getCodelistInternalValue().equals("")) {
			if (searchRefByCode != null
					&& myFocusRef.getActiveFlag().equals("Y")) {
				if ((myFocusRef.getId() != null && !searchRefByCode.getId()
						.equals(myFocusRef.getId()))
						|| myFocusRef.getId() == null) {
					FacesMessage msg = new FacesMessage(
							FacesMessage.SEVERITY_WARN,
							"The active codelist value exists for the same code",
							"");
					FacesContext.getCurrentInstance().addMessage(null, msg);
					return "";
				}
			}
		}

		// Default flag for another codelist set to Y
		RefConfigCodeList searchForDefault = refCodeListService
				.findByDefaultFlag(myFocusRef.getCodelistConfigType(), "Y");
		if (myFocusRef.getCodelistInternalValue() != null
				&& !myFocusRef.getCodelistInternalValue().equals("")) {
			if (searchForDefault != null
					&& myFocusRef.getDefaultFlag().equals("Y")) {
				if ((myFocusRef.getId() != null && !searchForDefault.getId()
						.equals(myFocusRef.getId()))
						|| myFocusRef.getId() == null) {

					// Reset default for existing codelist to Y and saving new
					// one
					try {
						searchForDefault.setDefaultFlag("N");
						refCodeListService
								.update(searchForDefault,
										this.authService.getUserCn(),
										this.authService.getUserGivenName(),
										this.authService.getUserSurName(),
										this.authService
												.getCombinedMappedGroupMembershipAsString());

					} catch (CqtServiceException e) {
						e.printStackTrace();
					}
				}
			}
		}

		try {
			if ("Y".equalsIgnoreCase(myFocusRef.getDefaultFlag())) {
				// if editing config value is set as default, remove the default
				// flag from the old default config
				RefConfigCodeList oldDefaultValue = refCodeListService
						.getDefaultForConfigType(myFocusRef
								.getCodelistConfigType());
				if (oldDefaultValue != null
						&& Long.compare(oldDefaultValue.getId(),
								myFocusRef.getId()) != 0) {
					oldDefaultValue.setDefaultFlag("N");
					refCodeListService
							.update(oldDefaultValue, this.authService
									.getUserCn(), this.authService
									.getUserGivenName(), this.authService
									.getUserSurName(), this.authService
									.getCombinedMappedGroupMembershipAsString());
				} else if (oldDefaultValue != null) {
					// this is the case when the updated refcode already had
					// defaultFlag 'Y'
					if (myFocusRef.getSerialNum().compareTo(
							BigDecimal.valueOf(1L)) != 0) {
						// if newly set serial number is not 1
						if (myFocusRef.getActiveFlag().equals(
								oldDefaultValue.getActiveFlag())
								&& myFocusRef.getCodelistInternalValue()
										.equals(oldDefaultValue
												.getCodelistInternalValue())
								&& myFocusRef.getValue().equals(
										oldDefaultValue.getValue())) {
							// if the Serial Number is the only field that has
							// been changed
							throw new Exception(
									"Serial# for default codelist value can not be greater than 1");
						}
					}
				}

				// when active flag of the default codelist is set to "N", it
				// should show the error message
				if ("N".equalsIgnoreCase(myFocusRef.getActiveFlag())) {
					throw new Exception(
							"Active flag for default codelist cannot be set to 'N'");
				}
			}
			if ("N".equalsIgnoreCase(myFocusRef.getDefaultFlag())
					&& myFocusRef.getSerialNum().compareTo(
							BigDecimal.valueOf(1L)) == 0) {
				throw new Exception(
						"The serial# can be set to 1 only for default codelist value.");
			}
			 
			// Increasing #serial
			/*if (oldCodelist != null && myFocusRef.getSerialNum().compareTo(oldCodelist.getSerialNum()) == 1) {
				System.out.println("\n #### Increasing");
				FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_WARN,	"This re-ordering of Serial Number is not currently allowed.",	"");
				FacesContext.getCurrentInstance().addMessage(null, msg);
				return;
			}*/
			
			RefConfigCodeList savedRefConfigCodeList = myFocusRef;
			boolean wasSerialNumAdjusted = false;
			double actualSerialNumUsed = 0.0d;
			boolean newAddedInMiddle = false;
			if (myFocusRef.getId() != null) {
				myFocusRef.setLastModificationDate(lastModifiedDate);
				myFocusRef.setLastModifiedBy(lastModifiedByString);
				if (myFocusRef.getDefaultFlag().equals("Y"))
					myFocusRef.setSerialNum(new BigDecimal(1.0));
				refCodeListService.update(myFocusRef, this.authService
						.getUserCn(), this.authService.getUserGivenName(),
						this.authService.getUserSurName(), this.authService
								.getCombinedMappedGroupMembershipAsString());
				saved = true;
				RequestContext.getCurrentInstance().execute("PF('extensionD').hide();");
			} else {
				//TODO: reset the serial num to 1 + max here if it not so and say that in info msg.
				
				myFocusRef.setLastModificationDate(lastModifiedDate);
				myFocusRef.setLastModifiedBy(lastModifiedByString);
				myFocusRef.setCreationDate(lastModifiedDate);
				myFocusRef.setCreatedBy(lastModifiedByString);
				if (myFocusRef.getDefaultFlag() != null && myFocusRef.getDefaultFlag().equals("Y")) {
					myFocusRef.setSerialNum(new BigDecimal(1.0));
				} else {
					List<RefConfigCodeList> refList = refCodeListService.findAllByConfigType(myFocusRef
							.getCodelistConfigType(), OrderBy.ASC);
					double nextSerialNum = refList.size() + 1;
					if(myFocusRef.getSerialNum().doubleValue() > nextSerialNum) {
						myFocusRef.setSerialNum(new BigDecimal(nextSerialNum));
						actualSerialNumUsed = nextSerialNum;
						wasSerialNumAdjusted=true;
					} else if(myFocusRef.getSerialNum().doubleValue() < nextSerialNum) {
						//make a copy of the saved one and
						newAddedInMiddle = true;
					}
				}
				refCodeListService.create(myFocusRef, this.authService
						.getUserCn(), this.authService.getUserGivenName(),
						this.authService.getUserSurName(), this.authService
								.getCombinedMappedGroupMembershipAsString());
				//since its a fresh insert get it back so that we know its id
				/*savedRefConfigCodeList = refCodeListService.findByConfigTypeAndInternalCode(myFocusRef
								.getCodelistConfigType(), myFocusRef
								.getCodelistInternalValue());*/
				savedRefConfigCodeList = refCodeListService.findById(myFocusRef.getId());
				saved = true;
				RequestContext.getCurrentInstance().execute("PF('extensionD').hide();");
			}
			if(newAddedInMiddle) {
				updateSerialNumberOfNewAdd(savedRefConfigCodeList.getCodelistConfigType(), savedRefConfigCodeList);
			} else {
				updateSerialNumbers(savedRefConfigCodeList.getCodelistConfigType(), savedRefConfigCodeList, oldCodelist);
			}
			String type = "";

			type = myFocusRef.getCodelistConfigType();
			getCodelistList();
			/*
			if (myFocusRef.getCodelistConfigType().equals(
					CqtConstants.CODE_LIST_TYPE_EXTENSION)) {
				type = "Extension";
				getCodelistList();
			} else if (myFocusRef.getCodelistConfigType().equals(
					CqtConstants.CODE_LIST_TYPE_MEDDRA_VERSIONS)) {
				type = "MedDRA Dictionary";
				getCodelistList();
			} else if (myFocusRef.getCodelistConfigType().equals(
					CqtConstants.CODE_LIST_TYPE_PRODUCT)) {
				type = "Product";
				getCodelistList();
			} else if (myFocusRef.getCodelistConfigType().equals(
					CqtConstants.CODE_LIST_TYPE_PROGRAM)) {
				type = "Program";
				getCodelistList();
			} else if (myFocusRef.getCodelistConfigType().equals(
					CqtConstants.CODE_LIST_TYPE_PROTOCOL)) {
				type = "Protocol";
				getCodelistList();
			} else if (myFocusRef.getCodelistConfigType().equals(
					CqtConstants.CODE_LIST_TYPE_WORKFLOW_STATES)) {
				type = "Workflow State";
				getCodelistList();
			} else if (myFocusRef.getCodelistConfigType().equals(
					CqtConstants.CODE_LIST_TYPE_USER_GROUPS)) {
				type = "User Group";
				getCodelistList();
			} else if (myFocusRef.getCodelistConfigType().equals(
					CqtConstants.CODE_LIST_TYPE_SYSTEM_CONFIG)) {
				type = "System Config";
				getCodelistList();
			} else if (myFocusRef.getCodelistConfigType().equals(
					CqtConstants.CODE_LIST_TYPE_CMQ_RELATION_IMPACT_TYPE)) {
				type = "CMQ Relation Impact Type";
				getCodelistList();
			} else if (myFocusRef.getCodelistConfigType().equals(
					CqtConstants.CODE_LIST_TYPE_SMQ_RELATION_IMPACT_TYPE)) {
				type = "SMQ Relation Impact Type";
				getCodelistList();
			} else if (myFocusRef.getCodelistConfigType().equals(
					CqtConstants.CODE_LIST_TYPE_MEDDRA_DICT_IMPACT_TYPE)) {
				type = "Meddra Dictionary Impact Type";
				getCodelistList();
			} else if (myFocusRef.getCodelistConfigType().equals(
					CqtConstants.CODE_LIST_TYPE_DICTIONARY_LEVELS)) {
				type = "Dictionary CMQ Level Type";
				getCodelistList();
			} else if (myFocusRef.getCodelistConfigType().equals(
					CqtConstants.CODE_LIST_TYPE_SMQ_FILTER_LEVELS)) {
				type = "SMQ Filter Level Type";
				getCodelistList();
			}
			*/
			//clear cache
			this.cqtCacheManager.removeAllFromCache("code-list-cache");
			FacesMessage msg = null;
			if(wasSerialNumAdjusted) {
				msg = new FacesMessage(FacesMessage.SEVERITY_INFO,
						type + " '" + myFocusRef.getCodelistInternalValue()
								+ "' is successfully saved. Serial Num updated to " + (int)actualSerialNumUsed, "");
			} else {
				msg = new FacesMessage(FacesMessage.SEVERITY_INFO,
						type + " '" + myFocusRef.getCodelistInternalValue()
								+ "' is successfully saved.", "");
			}
			FacesContext.getCurrentInstance().addMessage(null, msg);
			
		} catch (CqtServiceException e) {
			e.printStackTrace();
			if (saved) {
				FacesMessage msg = new FacesMessage(
						FacesMessage.SEVERITY_ERROR,
						"An error occurred while creating an codelist type",
						"Error:" + e.getMessage());
				FacesContext.getCurrentInstance().addMessage(null, msg);
			}
		} catch (Exception e) {
			e.printStackTrace();
			FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR,
					e.getMessage(), "");
			FacesContext.getCurrentInstance().addMessage(
					"An error occured when saving this codelist", msg);
		}
		

		//myFocusRef = new RefConfigCodeList();
		return "";
	}

	private void updateSerialNumberOfNewAdd(String codelistConfigType, RefConfigCodeList savedRef) {
 		double newVal = savedRef.getSerialNum().doubleValue();
 		double val = newVal;

		System.out.println("updateSerialNumberOfNewAdd new value " + savedRef.getSerialNum().doubleValue() + " for " + savedRef.getCodelistInternalValue());
		
 		List<RefConfigCodeList> refList = refCodeListService.findAllByConfigType(codelistConfigType, OrderBy.ASC);
		//List<RefConfigCodeList> refListToSave = new ArrayList<RefConfigCodeList>();
        
 		if (refList != null && !refList.isEmpty()) {
            refList.sort(new Comparator<RefConfigCodeList> () {
                @Override
                public int compare(RefConfigCodeList o1, RefConfigCodeList o2) {
                	// make sure Default values always comes first in the list
                    if("Y".equalsIgnoreCase(o1.getDefaultFlag()) && "Y".equalsIgnoreCase(o1.getActiveFlag()))
                        return -1;
                    else if("Y".equalsIgnoreCase(o2.getDefaultFlag()) && "Y".equalsIgnoreCase(o2.getActiveFlag()))
                        return 1;
                    
                    return Double.compare(o1.getSerialNum().doubleValue(), o2.getSerialNum().doubleValue());
                }
            });
		}
 		
        final Long lastSavedId = savedRef.getId();
        boolean matchFound = false;
        if(refList != null && !refList.isEmpty()) {
    		for (RefConfigCodeList refConfigCodeList : refList) {
				if(!matchFound && (refConfigCodeList.getId().longValue() == lastSavedId.longValue())) {
					matchFound = true;
					System.out.println("updateSerialNumberOfNewAdd match found decrease..." + refConfigCodeList.getCodelistInternalValue());
					continue;
				} else if (refConfigCodeList.getSerialNum().doubleValue() == newVal){
					BigDecimal newSerialNum = new BigDecimal(++val);
					System.out.println("updateSerialNumberOfNewAdd incrementing " + refConfigCodeList.getCodelistInternalValue() + " from " 
											+ refConfigCodeList.getSerialNum().doubleValue() + " to " + newSerialNum.doubleValue());
					refConfigCodeList.setSerialNum(newSerialNum);
				} else if(matchFound) {
					BigDecimal newSerialNum = new BigDecimal(++val);
					System.out.println("updateSerialNumberOfNewAdd incrementing " + refConfigCodeList.getCodelistInternalValue() + " from " 
											+ refConfigCodeList.getSerialNum().doubleValue() + " to " + newSerialNum.doubleValue());
					refConfigCodeList.setSerialNum(newSerialNum);
				}
			}
        }
        
		if (matchFound) {
			try {
				refCodeListService.update(refList, this.authService
						.getUserCn(), this.authService.getUserGivenName(),
						this.authService.getUserSurName(), this.authService
								.getCombinedMappedGroupMembershipAsString());
			} catch (CqtServiceException e) {
				e.printStackTrace();
				FacesMessage msg = new FacesMessage(
						FacesMessage.SEVERITY_ERROR,
						"An error occurred while saving the codelist",
						"Error: " + e.getMessage());
				FacesContext.getCurrentInstance().addMessage(null, msg);
			}
		}
		
  	}

	private void updateSerialNumbers(String codelistConfigType, RefConfigCodeList savedRef, RefConfigCodeList oldRef) {
 		double newVal = savedRef.getSerialNum().doubleValue();
 		double val = newVal;
		if (oldRef != null)
			System.out.println("old value " + oldRef.getSerialNum().doubleValue() + " for " + oldRef.getCodelistInternalValue());

		System.out.println("new value " + savedRef.getSerialNum().doubleValue() + " for " + savedRef.getCodelistInternalValue());
		
		boolean increase = false, decrease = false;		
		if (oldRef != null) {
			if (newVal > oldRef.getSerialNum().doubleValue())
				increase = true;
			
			if (newVal < oldRef.getSerialNum().doubleValue())
				decrease = true;
		} else if(savedRef.getSerialNum().doubleValue() == 1.0d) {
			//new add at 1
			decrease = true;
		}
		
 		List<RefConfigCodeList> refList = refCodeListService.findAllByConfigType(codelistConfigType, OrderBy.ASC);
		//List<RefConfigCodeList> refListToSave = new ArrayList<RefConfigCodeList>();
 		double modifiedSrNo = -1d;
        if(newVal > refList.size()) {
        	modifiedSrNo = refList.size();
        	System.out.println("Defaulting new value to " + modifiedSrNo + " as it is more than max in list.");
        }
 		if (refList != null && !refList.isEmpty()) {
            refList.sort(new Comparator<RefConfigCodeList> () {
                @Override
                public int compare(RefConfigCodeList o1, RefConfigCodeList o2) {
                	// make sure Default values always comes first in the list
                    if("Y".equalsIgnoreCase(o1.getDefaultFlag()) && "Y".equalsIgnoreCase(o1.getActiveFlag()))
                        return -1;
                    else if("Y".equalsIgnoreCase(o2.getDefaultFlag()) && "Y".equalsIgnoreCase(o2.getActiveFlag()))
                        return 1;
                    
                    return Double.compare(o1.getSerialNum().doubleValue(), o2.getSerialNum().doubleValue());
                }
            });
		}
 		
        final Long lastSavedId = savedRef.getId();
        boolean matchFound = false;
        if(refList != null && !refList.isEmpty()) {
        	if(decrease) {
        		for (RefConfigCodeList refConfigCodeList : refList) {
    				if(!matchFound && (refConfigCodeList.getId().longValue() == lastSavedId.longValue())) {
    					matchFound = true;
    					System.out.println("match found decrease..." + refConfigCodeList.getCodelistInternalValue());
    					continue;
    				} else if (refConfigCodeList.getSerialNum().doubleValue() == newVal){
    					BigDecimal newSerialNum = new BigDecimal(++val);
    					System.out.println("incrementing " + refConfigCodeList.getCodelistInternalValue() + " from " 
    											+ refConfigCodeList.getSerialNum().doubleValue() + " to " + newSerialNum.doubleValue());
    					refConfigCodeList.setSerialNum(newSerialNum);
    				} else if(matchFound) {
    					BigDecimal newSerialNum = new BigDecimal(++val);
    					System.out.println("incrementing " + refConfigCodeList.getCodelistInternalValue() + " from " 
    											+ refConfigCodeList.getSerialNum().doubleValue() + " to " + newSerialNum.doubleValue());
    					refConfigCodeList.setSerialNum(newSerialNum);
    				}
    			}
        	} else if(increase) {
        		double oldVal = oldRef.getSerialNum().doubleValue();
        		double start = newVal;
        		double end = oldVal;
        		if(modifiedSrNo != -1) {
        			start = modifiedSrNo;
        		}
        		for(int i=(int)(start - 1); i>=(end -1); i--) {
        			System.out.println("i = " + i);
        			RefConfigCodeList refConfigCodeList = refList.get(i);
        			if(!matchFound && (refConfigCodeList.getId().longValue() == lastSavedId.longValue())) {
    					matchFound = true;
    					System.out.println("match found increase..." + refConfigCodeList.getCodelistInternalValue());
    					if(modifiedSrNo != -1) {
    						BigDecimal newSerialNum = new BigDecimal(modifiedSrNo);
    						System.out.println("modifiedSrNo is not -1....changing " + refConfigCodeList.getCodelistInternalValue() + " from " 
													+ refConfigCodeList.getSerialNum().doubleValue() + " to " + modifiedSrNo);
    						refConfigCodeList.setSerialNum(newSerialNum);
    					}
    					continue;
    				} else if (refConfigCodeList.getSerialNum().doubleValue() == newVal){
    					double existingval = refConfigCodeList.getSerialNum().doubleValue();
    					BigDecimal newSerialNum = new BigDecimal(--existingval);
    					System.out.println("incrementing " + refConfigCodeList.getCodelistInternalValue() + " from " 
    											+ refConfigCodeList.getSerialNum().doubleValue() + " to " + newSerialNum.doubleValue());
    					refConfigCodeList.setSerialNum(newSerialNum);
    				} else if(matchFound) {
    					double existingval = refConfigCodeList.getSerialNum().doubleValue();
    					BigDecimal newSerialNum = new BigDecimal(--existingval);
    					System.out.println("incrementing " + refConfigCodeList.getCodelistInternalValue() + " from " 
    											+ refConfigCodeList.getSerialNum().doubleValue() + " to " + newSerialNum.doubleValue());
    					refConfigCodeList.setSerialNum(newSerialNum);
    				}
        		}
        	}
        }
        
		if (matchFound) {
			try {
				refCodeListService.update(refList, this.authService
						.getUserCn(), this.authService.getUserGivenName(),
						this.authService.getUserSurName(), this.authService
								.getCombinedMappedGroupMembershipAsString());
			} catch (CqtServiceException e) {
				e.printStackTrace();
				FacesMessage msg = new FacesMessage(
						FacesMessage.SEVERITY_ERROR,
						"An error occurred while saving the codelist",
						"Error: " + e.getMessage());
				FacesContext.getCurrentInstance().addMessage(null, msg);
			}
		}
		
  	}

	public void generateConfigReport() {
		StreamedContent content = refCodeListService
				.generateReport(this.codelist);
		setExcelFile(content);
	}

	public void validateMeddraValue(FacesContext context, UIComponent component,
            Object value) {
		if(null != value) {
			String valueString = value.toString();
			if(StringUtils.isNotBlank(valueString)) {
				if(valueString.matches("\\d*\\.?\\d+")) {
					FacesMessage msg = new FacesMessage("Decimal is not allowed in the version number.");
			        msg.setSeverity(FacesMessage.SEVERITY_ERROR);
			        throw new ValidatorException(msg);
				}
			} else {
				FacesMessage msg = new FacesMessage("Value cannot be empty.");
		        msg.setSeverity(FacesMessage.SEVERITY_ERROR);
		        throw new ValidatorException(msg);
			}
		} else {
			FacesMessage msg = new FacesMessage("Value cannot be empty.");
	        msg.setSeverity(FacesMessage.SEVERITY_ERROR);
	        throw new ValidatorException(msg);
		}
	}
	
	public String getSaveValue() {
		String save = "Add " + getCodelist();
		return save;
	}
	
	public String getName() {
		String name = getCodelist() + " Name";
		return name;
	}
	
	public String getHeader() {
		String header = getCodelist() + " CodeList";
		return header;
	}
	
	public List<CodelistDTO> getList() {
		return list;
	}

	public void setList(List<CodelistDTO> list) {
		this.list = list;
	}

	public String getCodelist() {
		return codelist;
	}

	public void setCodelist(String codelist) {
		this.codelist = codelist;
	}

	public IRefCodeListService getRefCodeListService() {
		return refCodeListService;
	}

	public void setRefCodeListService(IRefCodeListService refCodeListService) {
		this.refCodeListService = refCodeListService;
	}

	public void setExtensions(List<RefConfigCodeList> extensions) {
		this.extensions = extensions;
	}

	public void setPrograms(List<RefConfigCodeList> programs) {
		this.programs = programs;
	}

	public void setProtocols(List<RefConfigCodeList> protocols) {
		this.protocols = protocols;
	}

	public void setProducts(List<RefConfigCodeList> products) {
		this.products = products;
	}

	public List<RefConfigCodeList> getExtensions() {
		return extensions;
	}

	public List<RefConfigCodeList> getPrograms() {
		return programs;
	}

	public List<RefConfigCodeList> getProtocols() {
		return protocols;
	}

	public List<RefConfigCodeList> getProducts() {
		return products;
	}

	public RefConfigCodeList getSelectedRow() {
		return selectedRow;
	}

	public void setSelectedRow(RefConfigCodeList selectedRow) {
		this.selectedRow = selectedRow;
	}

	public RefConfigCodeList getRef() {
		return myFocusRef;
	}

	public void setRef(RefConfigCodeList ref) {
		this.myFocusRef = ref;
	}

	public RefConfigCodeList getNewCodelist() {
		return myCodelist;
	}

	public void setNewCodelist(RefConfigCodeList ref) {
		this.myCodelist = ref;
	}

	public List<RefConfigCodeList> getMeddras() {
		return meddras;
	}

	public void setMeddras(List<RefConfigCodeList> meddras) {
		this.meddras = meddras;
	}

	public List<RefConfigCodeList> getWorkflows() {
		return workflows;
	}

	public void setWorkflows(List<RefConfigCodeList> workflows) {
		this.workflows = workflows;
	}

	public StreamedContent getExcelFile() {
		return excelFile;
	}

	public void setExcelFile(StreamedContent excelFile) {
		this.excelFile = excelFile;
	}

	/**
	 * @return the usergroups
	 */
	public List<RefConfigCodeList> getUsergroups() {
		return usergroups;
	}

	/**
	 * @param usergroups
	 *            the usergroups to set
	 */
	public void setUsergroups(List<RefConfigCodeList> usergroups) {
		this.usergroups = usergroups;
	}

	/**
	 * @return the sysconfigs
	 */
	public List<RefConfigCodeList> getSysconfigs() {
		return sysconfigs;
	}

	/**
	 * @param
	 *
	 */
	public void setSysconfigs(List<RefConfigCodeList> sysconfigs) {
		this.sysconfigs = sysconfigs;
	}

	public AuthenticationService getAuthService() {
		return authService;
	}

	public void setAuthService(AuthenticationService authService) {
		this.authService = authService;
	}

	/**
	 * @return the cmqImpactTypes
	 */
	public List<RefConfigCodeList> getCmqImpactTypes() {
		return cmqImpactTypes;
	}

	/**
	 * @param cmqImpactTypes
	 *            the cmqImpactTypes to set
	 */
	public void setCmqImpactTypes(List<RefConfigCodeList> cmqImpactTypes) {
		this.cmqImpactTypes = cmqImpactTypes;
	}

	/**
	 * @return the smqImpactTypes
	 */
	public List<RefConfigCodeList> getSmqImpactTypes() {
		return smqImpactTypes;
	}

	/**
	 * @param smqImpactTypes
	 *            the smqImpactTypes to set
	 */
	public void setSmqImpactTypes(List<RefConfigCodeList> smqImpactTypes) {
		this.smqImpactTypes = smqImpactTypes;
	}

	/**
	 * @return the meddraImpactTypes
	 */
	public List<RefConfigCodeList> getMeddraImpactTypes() {
		return meddraImpactTypes;
	}

	/**
	 * @param meddraImpactTypes
	 *            the meddraImpactTypes to set
	 */
	public void setMeddraImpactTypes(List<RefConfigCodeList> meddraImpactTypes) {
		this.meddraImpactTypes = meddraImpactTypes;
	}

	public List<RefConfigCodeList> getLevels() {
		return levels;
	}

	public void setLevels(List<RefConfigCodeList> levels) {
		this.levels = levels;
	}

	public List<RefConfigCodeList> getSmqfilters() {
		return smqfilters;
	}

	public void setSmqfilters(List<RefConfigCodeList> smqfilters) {
		this.smqfilters = smqfilters;
	}

	public ICqtCacheManager getCqtCacheManager() {
		return cqtCacheManager;
	}

	public void setCqtCacheManager(ICqtCacheManager cqtCacheManager) {
		this.cqtCacheManager = cqtCacheManager;
	}
}
