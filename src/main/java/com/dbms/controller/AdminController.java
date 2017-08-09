package com.dbms.controller;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Objects;

import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
import javax.faces.event.AjaxBehaviorEvent;

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
//    private static final String CODELIST_EXTENSION = "EXTENSION";
//    private static final String CODELIST_PRODUCT = "PRODUCT";
//    private static final String CODELIST_PROGRAM = "PROGRAM";
//    private static final String CODELIST_PROTOCOL = "PROTOCOL";
//    private static final String CODELIST_MEDDRA = "MEDDRA";
//    private static final String CODELIST_WORKFLOW = "WORKFLOW";
//    private static final String CODELIST_USERGROUP = "USER_GROUPS";
//    private static final String CODELIST_SYSCONFIG = "SYSTEM_CONFIG";
//    private static final String CODELIST_CMQ_RELATION_IMPACT_TYPE = "CMQ_RELATION_IMPACT_TYPE";
//    private static final String CODELIST_SMQ_RELATION_IMPACT_TYPE = "SMQ_RELATION_IMPACT_TYPE";
//    private static final String CODELIST_MEDDRA_DICT_IMPACT_TYPE = "MEDDRA_DICT_IMPACT_TYPE";
//    private static final String CODELIST_DICTIONARY_LEVELS_TYPE = "DICTIONARY_CMQ_LEVELS";
//    private static final String CODELIST_SMQ_LEVELS_TYPE = "SMQ_FILTER_LEVELS";
    
    @ManagedProperty("#{CqtCacheManager}")
   	private ICqtCacheManager cqtCacheManager;
	private final String CACHE_NAME = "code-list-cache";



	List<CodelistDTO> list;
	private String codelist;

	@ManagedProperty("#{RefCodeListService}")
	private IRefCodeListService refCodeListService;

	@ManagedProperty("#{AuthenticationService}")
	private AuthenticationService authService;
	
	private List<RefConfigCodeList> extensions, programs, protocols,
            products, meddras, workflows, usergroups, sysconfigs,
            cmqImpactTypes, smqImpactTypes, meddraImpactTypes, levels, smqfilters;
	
	private RefConfigCodeList selectedRow, myFocusRef;
	private StreamedContent excelFile;

	public AdminController() {
		codelist = CqtConstants.CODE_LIST_TYPE_EXTENSION;
	}

	@PostConstruct
	public void init() {
		
		getExtensionList();
//		getProductList();
//		getProgramList();
//		getProtocolList();
//		getMeddraList();
//		getWorkflowList();
//        getUsergroupList();
//        getSysconfigList();
//        getCmqImpactTypeList();
//        getSmqImpactTypeList();
//        getMeddraImpactTypeList();
//        getLevelList();
//        getSMQFilters();
	}
	
	public void switchCodelist(AjaxBehaviorEvent event) {
		if (codelist.equals(CqtConstants.CODE_LIST_TYPE_EXTENSION) && extensions == null) {
 			getExtensionList();
		} else if (codelist.equals(CqtConstants.CODE_LIST_TYPE_MEDDRA_VERSIONS) && meddras == null) { 
 			getMeddraList();
		} else if (codelist.equals(CqtConstants.CODE_LIST_TYPE_PRODUCT) && products == null) {
 			getProductList();
		} else if (codelist.equals(CqtConstants.CODE_LIST_TYPE_PROGRAM) && programs == null) {
 			getProgramList();
		} else if (codelist.equals(CqtConstants.CODE_LIST_TYPE_PROTOCOL) && protocols == null) {
 			getProtocolList();
		} else if (codelist.equals(CqtConstants.CODE_LIST_TYPE_WORKFLOW_STATES) && workflows == null) {
 			getWorkflowList();
		} else if (codelist.equals(CqtConstants.CODE_LIST_TYPE_USER_GROUPS) && usergroups == null) {
 			getUsergroupList();
		} else if (codelist.equals(CqtConstants.CODE_LIST_TYPE_SYSTEM_CONFIG) && sysconfigs == null) {
 			getSysconfigList();
		} else if (codelist.equals(CqtConstants.CODE_LIST_TYPE_CMQ_RELATION_IMPACT_TYPE) && cmqImpactTypes == null) {
 			getCmqImpactTypeList();
		} else if (codelist.equals(CqtConstants.CODE_LIST_TYPE_SMQ_RELATION_IMPACT_TYPE) && smqImpactTypes == null) {
 			getSmqImpactTypeList();
		} else if (codelist.equals(CqtConstants.CODE_LIST_TYPE_MEDDRA_DICT_IMPACT_TYPE) && meddraImpactTypes == null) {
 			getMeddraImpactTypeList();
		} else if (codelist.equals(CqtConstants.CODE_LIST_TYPE_DICTIONARY_LEVELS) && levels == null) {
 			getLevelList();
		}
		else if (codelist.equals(CqtConstants.CODE_LIST_TYPE_SMQ_FILTER_LEVELS) && smqfilters == null) {
 			getSMQFilters();
		}
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
		BigDecimal lastSerial = new BigDecimal(0);
		myFocusRef = new RefConfigCodeList();
		if (codelist.equals(CqtConstants.CODE_LIST_TYPE_EXTENSION)) {
			myFocusRef.setCodelistConfigType(CqtConstants.CODE_LIST_TYPE_EXTENSION); 
			if (extensions != null && !extensions.isEmpty())
				lastSerial = extensions.get(extensions.size() - 1).getSerialNum();
		} else if (codelist.equals(CqtConstants.CODE_LIST_TYPE_PRODUCT)) {
			myFocusRef.setCodelistConfigType(CqtConstants.CODE_LIST_TYPE_PRODUCT); 
			if (products != null && !products.isEmpty())
				lastSerial = products.get(products.size() - 1).getSerialNum();
		} else if (codelist.equals(CqtConstants.CODE_LIST_TYPE_PROGRAM)) {
			myFocusRef.setCodelistConfigType(CqtConstants.CODE_LIST_TYPE_PROGRAM); 
			if (programs != null && !programs.isEmpty())
				lastSerial = programs.get(programs.size() - 1).getSerialNum();
		} else if (codelist.equals(CqtConstants.CODE_LIST_TYPE_PROTOCOL)) {
			myFocusRef.setCodelistConfigType(CqtConstants.CODE_LIST_TYPE_PROTOCOL); 
			if (protocols != null && !protocols.isEmpty())
				lastSerial = protocols.get(protocols.size() - 1).getSerialNum();
		} else if (codelist.equals(CqtConstants.CODE_LIST_TYPE_MEDDRA_VERSIONS)) {
			myFocusRef.setCodelistConfigType(CqtConstants.CODE_LIST_TYPE_MEDDRA_VERSIONS); 
			if (meddras != null && !meddras.isEmpty())
				lastSerial = meddras.get(meddras.size() - 1).getSerialNum();
		} else if (codelist.equals(CqtConstants.CODE_LIST_TYPE_WORKFLOW_STATES)) {
			myFocusRef.setCodelistConfigType(CqtConstants.CODE_LIST_TYPE_WORKFLOW_STATES); 
			if (workflows != null && !workflows.isEmpty())
				lastSerial = workflows.get(workflows.size() - 1).getSerialNum();
		} else if (codelist.equals(CqtConstants.CODE_LIST_TYPE_USER_GROUPS)) {
			myFocusRef.setCodelistConfigType(CqtConstants.CODE_LIST_TYPE_USER_GROUPS); 
			if (usergroups != null && !usergroups.isEmpty())
				lastSerial = usergroups.get(usergroups.size() - 1).getSerialNum();
		} else if (codelist.equals(CqtConstants.CODE_LIST_TYPE_SYSTEM_CONFIG)) {
			myFocusRef.setCodelistConfigType(CqtConstants.CODE_LIST_TYPE_SYSTEM_CONFIG); 
			if (sysconfigs != null && !sysconfigs.isEmpty())
				lastSerial = sysconfigs.get(sysconfigs.size() - 1).getSerialNum();
		} else if (codelist.equals(CqtConstants.CODE_LIST_TYPE_CMQ_RELATION_IMPACT_TYPE)) {
			myFocusRef.setCodelistConfigType(CqtConstants.CODE_LIST_TYPE_CMQ_RELATION_IMPACT_TYPE); 
			if (getCmqImpactTypes() != null && !cmqImpactTypes.isEmpty())
				lastSerial = getCmqImpactTypes().get(getCmqImpactTypes().size() - 1).getSerialNum();
		} else if (codelist.equals(CqtConstants.CODE_LIST_TYPE_SMQ_RELATION_IMPACT_TYPE)) {
			myFocusRef.setCodelistConfigType(CqtConstants.CODE_LIST_TYPE_SMQ_RELATION_IMPACT_TYPE); 
			if (getSmqImpactTypes() != null && !smqImpactTypes.isEmpty())
				lastSerial = getSmqImpactTypes().get(getSmqImpactTypes().size() - 1).getSerialNum();
		} else if (codelist.equals(CqtConstants.CODE_LIST_TYPE_MEDDRA_DICT_IMPACT_TYPE)) {
			myFocusRef.setCodelistConfigType(CqtConstants.CODE_LIST_TYPE_MEDDRA_DICT_IMPACT_TYPE); 
			if (getMeddraImpactTypes() != null && !meddraImpactTypes.isEmpty())
				lastSerial = getMeddraImpactTypes().get(getMeddraImpactTypes().size() - 1).getSerialNum();
		} else if (codelist.equals(CqtConstants.CODE_LIST_TYPE_DICTIONARY_LEVELS)) {
			myFocusRef.setCodelistConfigType(CqtConstants.CODE_LIST_TYPE_DICTIONARY_LEVELS); 
			if (getLevels()!= null && !levels.isEmpty())
				lastSerial = getLevels().get(getLevels().size() - 1).getSerialNum();
		} else if (codelist.equals(CqtConstants.CODE_LIST_TYPE_SMQ_FILTER_LEVELS)) {
			myFocusRef.setCodelistConfigType(CqtConstants.CODE_LIST_TYPE_SMQ_FILTER_LEVELS); 
			if (getSmqfilters() != null && !smqfilters.isEmpty())
				lastSerial = getSmqfilters().get(getSmqfilters().size() - 1).getSerialNum();
		}
		myFocusRef.setCreationDate(new Date());
		myFocusRef.setLastModificationDate(new Date()); 
		myFocusRef.setSerialNum(lastSerial.add(new BigDecimal(1)));
		
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
		
		return "";
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
	 * @return
	 */
	public List<RefConfigCodeList> getUsergroupList() {
		usergroups = refCodeListService.findAllByConfigType(CqtConstants.CODE_LIST_TYPE_USER_GROUPS, OrderBy.ASC);
		if (usergroups == null) {
			usergroups = new ArrayList<RefConfigCodeList>();
		}
		return usergroups;
	}
    
    /**
	 * Returns System Config codelist.
	 * @return
	 */
	public List<RefConfigCodeList> getSysconfigList() {
		sysconfigs = refCodeListService.findAllByConfigType(CqtConstants.CODE_LIST_TYPE_SYSTEM_CONFIG, OrderBy.ASC);
		if (sysconfigs == null) {
			sysconfigs = new ArrayList<RefConfigCodeList>();
		}
		return sysconfigs;
	}
	
	/**
	 * Returns Dictionary level codelist.
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
	 * @return
	 */
	public List<RefConfigCodeList> getCmqImpactTypeList() {
		cmqImpactTypes = refCodeListService.findAllByConfigType(CqtConstants.CODE_LIST_TYPE_CMQ_RELATION_IMPACT_TYPE, OrderBy.ASC);
		if (cmqImpactTypes == null) {
			cmqImpactTypes = new ArrayList<RefConfigCodeList>();
		}
		return cmqImpactTypes;
	}
    
    /**
	 * Returns SMQ List Impact Type Config codelist.
	 * @return
	 */
	public List<RefConfigCodeList> getSmqImpactTypeList() {
		smqImpactTypes = refCodeListService.findAllByConfigType(CqtConstants.CODE_LIST_TYPE_SMQ_RELATION_IMPACT_TYPE, OrderBy.ASC);
		if (smqImpactTypes == null) {
			smqImpactTypes = new ArrayList<RefConfigCodeList>();
		}
		return smqImpactTypes;
	}
    
    /**
	 * Returns Meddra List Impact Type Config codelist.
	 * @return
	 */
	public List<RefConfigCodeList> getMeddraImpactTypeList() {
		meddraImpactTypes = refCodeListService.findAllByConfigType(CqtConstants.CODE_LIST_TYPE_MEDDRA_DICT_IMPACT_TYPE, OrderBy.ASC);
		if (meddraImpactTypes == null) {
			meddraImpactTypes = new ArrayList<RefConfigCodeList>();
		}
		return meddraImpactTypes;
	}
	
	public void addRefCodelist() {
        // check if changed
        if(myFocusRef.getId() != null) {
            RefConfigCodeList oldValue = refCodeListService.findById(myFocusRef.getId());
            if(oldValue != null
                    && myFocusRef.getActiveFlag().equals(oldValue.getActiveFlag())
                    && myFocusRef.getCodelistInternalValue().equals(oldValue.getCodelistInternalValue())
                    && myFocusRef.getSerialNum().equals(oldValue.getSerialNum())
                    && myFocusRef.getDefaultFlag().equals(oldValue.getDefaultFlag())
                    && myFocusRef.getValue().equals(oldValue.getValue())) {
                // Nothing has been changed
                return;
            }
        }

        boolean saved = false;
		Date lastModifiedDate = new Date();
		String lastModifiedByString = this.authService.getLastModifiedByUserAsString();	 
			
		RefConfigCodeList searchRefByCode = refCodeListService.findByCriterias(myFocusRef.getCodelistConfigType(),  myFocusRef.getCodelistInternalValue(), "Y");
		 
		if (myFocusRef.getCodelistInternalValue() != null && !myFocusRef.getCodelistInternalValue().equals("")) {
			if (searchRefByCode != null && myFocusRef.getActiveFlag().equals("Y")) {
				if ((myFocusRef.getId() != null && !searchRefByCode.getId().equals(myFocusRef.getId())) || myFocusRef.getId() == null) {
					FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_WARN, "The active codelist value exists for the same code", "");
					FacesContext.getCurrentInstance().addMessage(null, msg);
					return;
				}
			}
		}
		
		//Default flag for another codelist set to Y
		RefConfigCodeList searchForDefault = refCodeListService.findByDefaultFlag(myFocusRef.getCodelistConfigType(), "Y");
		if (myFocusRef.getCodelistInternalValue() != null && !myFocusRef.getCodelistInternalValue().equals("")) {
			if (searchForDefault != null && myFocusRef.getDefaultFlag().equals("Y")) {
				if ((myFocusRef.getId() != null && !searchForDefault.getId().equals(myFocusRef.getId())) || myFocusRef.getId() == null) {
					
					//Reset default for existing codelist to Y and saving new one
					try {
						searchForDefault.setDefaultFlag("N");
						refCodeListService.update(searchForDefault, this.authService.getUserCn()
								, this.authService.getUserGivenName(), this.authService.getUserSurName()
								, this.authService.getCombinedMappedGroupMembershipAsString());
						
 					} catch (CqtServiceException e) {
						e.printStackTrace();
					}
				}
			}
		}
		
		try {
            if ("Y".equalsIgnoreCase(myFocusRef.getDefaultFlag())) {
                // if editing config value is set as default, remove the default flag from the old default config
                RefConfigCodeList oldDefaultValue = refCodeListService.getDefaultForConfigType(myFocusRef.getCodelistConfigType());
                if(oldDefaultValue != null && Long.compare(oldDefaultValue.getId(), myFocusRef.getId()) != 0) {
                    oldDefaultValue.setDefaultFlag("N");
                    refCodeListService.update(oldDefaultValue, this.authService.getUserCn()
						, this.authService.getUserGivenName(), this.authService.getUserSurName()
						, this.authService.getCombinedMappedGroupMembershipAsString());
                } else if(oldDefaultValue != null) {
                    // this is the case when the updated refcode already had defaultFlag 'Y'
                    if(myFocusRef.getSerialNum().compareTo(BigDecimal.valueOf(1L)) != 0) {
                        //if newly set serial number is not 1
                       if(myFocusRef.getActiveFlag().equals(oldDefaultValue.getActiveFlag())
                               && myFocusRef.getCodelistInternalValue().equals(oldDefaultValue.getCodelistInternalValue())
                               && myFocusRef.getValue().equals(oldDefaultValue.getValue()))  {
                           // if the Serial Number is the only field that has been changed
                           throw new Exception("Serial# for default codelist value can not be greater than 1");
                       }
                    }
                }
                
                // when active flag of the default codelist is set to "N", it should show the error message
                if("N".equalsIgnoreCase(myFocusRef.getActiveFlag())) {
                    throw new Exception("Active flag for default codelist cannot be set to 'N'");
                }
            }
            if ("N".equalsIgnoreCase(myFocusRef.getDefaultFlag()) && myFocusRef.getSerialNum().compareTo(BigDecimal.valueOf(1L)) == 0) {
            	throw new Exception("The serial# can be set to 1 only for default codelist value.");
            }
			if (myFocusRef.getId() != null){
				myFocusRef.setLastModificationDate(lastModifiedDate);
				myFocusRef.setLastModifiedBy(lastModifiedByString);
				if (myFocusRef.getDefaultFlag().equals("Y"))
					myFocusRef.setSerialNum(new BigDecimal(1.0));
				refCodeListService.update(myFocusRef, this.authService.getUserCn()
						, this.authService.getUserGivenName(), this.authService.getUserSurName()
						, this.authService.getCombinedMappedGroupMembershipAsString());
				saved = true;
			} else {
				myFocusRef.setLastModificationDate(lastModifiedDate);
				myFocusRef.setLastModifiedBy(lastModifiedByString);
				myFocusRef.setCreationDate(lastModifiedDate);
				myFocusRef.setCreatedBy(lastModifiedByString);
				if (myFocusRef.getDefaultFlag().equals("Y"))
					myFocusRef.setSerialNum(new BigDecimal(1.0));
 				refCodeListService.create(myFocusRef, this.authService.getUserCn()
						, this.authService.getUserGivenName(), this.authService.getUserSurName()
						, this.authService.getCombinedMappedGroupMembershipAsString());
 				saved = true;
			}
			updateSerialNumbers(myFocusRef.getCodelistConfigType(), myFocusRef);
			String type = "";
		

			if (myFocusRef.getCodelistConfigType().equals(CqtConstants.CODE_LIST_TYPE_EXTENSION)) {
				type = "Extension";
				getExtensionList();
			} else if (myFocusRef.getCodelistConfigType().equals(CqtConstants.CODE_LIST_TYPE_MEDDRA_VERSIONS)) { 
				type = "MedDRA Dictionary";
				getMeddraList();
			} else if (myFocusRef.getCodelistConfigType().equals(CqtConstants.CODE_LIST_TYPE_PRODUCT)) {
				type = "Product";
				getProductList();
			} else if (myFocusRef.getCodelistConfigType().equals(CqtConstants.CODE_LIST_TYPE_PROGRAM)) {
				type = "Program";
				getProgramList();
			} else if (myFocusRef.getCodelistConfigType().equals(CqtConstants.CODE_LIST_TYPE_PROTOCOL)) {
				type = "Protocol"; 
				getProtocolList();
			} else if (myFocusRef.getCodelistConfigType().equals(CqtConstants.CODE_LIST_TYPE_WORKFLOW_STATES)) {
				type = "Workflow State";
				getWorkflowList();
			} else if (myFocusRef.getCodelistConfigType().equals(CqtConstants.CODE_LIST_TYPE_USER_GROUPS)) {
				type = "User Group";
				getUsergroupList();
			} else if (myFocusRef.getCodelistConfigType().equals(CqtConstants.CODE_LIST_TYPE_SYSTEM_CONFIG)) {
				type = "System Config";
				getSysconfigList();
			} else if (myFocusRef.getCodelistConfigType().equals(CqtConstants.CODE_LIST_TYPE_CMQ_RELATION_IMPACT_TYPE)) {
				type = "CMQ Relation Impact Type";
				getCmqImpactTypeList();
			} else if (myFocusRef.getCodelistConfigType().equals(CqtConstants.CODE_LIST_TYPE_SMQ_RELATION_IMPACT_TYPE)) {
				type = "SMQ Relation Impact Type";
				getSmqImpactTypeList();
			} else if (myFocusRef.getCodelistConfigType().equals(CqtConstants.CODE_LIST_TYPE_MEDDRA_DICT_IMPACT_TYPE)) {
				type = "Meddra Dictionary Impact Type";
				getMeddraImpactTypeList();
			} else if (myFocusRef.getCodelistConfigType().equals(CqtConstants.CODE_LIST_TYPE_DICTIONARY_LEVELS)) {
				type = "Dictionary CMQ Level Type";
				getLevelList();
			}
			else if (myFocusRef.getCodelistConfigType().equals(CqtConstants.CODE_LIST_TYPE_SMQ_FILTER_LEVELS)) {
				type = "SMQ Filter Level Type";
				getSMQFilters();
			}
			FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_INFO,
					type + " '" + myFocusRef.getCodelistInternalValue() + "' is successfully saved.", "");
			FacesContext.getCurrentInstance().addMessage(null, msg);
		} catch (CqtServiceException e) {
			e.printStackTrace();
			if (saved) {
				FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR,
						"An error occurred while creating an codelist type", "Error:" + e.getMessage());
				FacesContext.getCurrentInstance().addMessage(null, msg);
			}
		} catch (Exception e) {
			e.printStackTrace();
            FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR,
						e.getMessage(), "");
            FacesContext.getCurrentInstance().addMessage("An error occured when saving this codelist", msg);
        }
		myFocusRef = new RefConfigCodeList();
	}

	private void updateSerialNumbers(String codelistConfigType, RefConfigCodeList savedRef) {
 		double val = savedRef.getSerialNum().doubleValue();
		System.out.println("\n\n ********************* serialSaved :  " + savedRef.getSerialNum().doubleValue());
 		List<RefConfigCodeList> refList = refCodeListService.findAllByConfigType(codelistConfigType, OrderBy.ASC);
		List<RefConfigCodeList> refListToSave = new ArrayList<RefConfigCodeList>();
        
        final Long lastSavedId = savedRef.getId();
        
        /**
         * Since default codeList is always #1, don't need to sort. Codelist already ordered by serial #
         */
		if (refList != null && !refList.isEmpty()) {
            refList.sort(new Comparator<RefConfigCodeList> () {
                @Override
                public int compare(RefConfigCodeList o1, RefConfigCodeList o2) {
                    // make sure Default values always comes first in the list
                    if("Y".equalsIgnoreCase(o1.getDefaultFlag()) && "Y".equalsIgnoreCase(o1.getActiveFlag()))
                        return -1;
                    else if("Y".equalsIgnoreCase(o2.getDefaultFlag()) && "Y".equalsIgnoreCase(o2.getActiveFlag()))
                        return 1;
                    
                    // then compare by serial number
                    int c = Double.compare(o1.getSerialNum().doubleValue(), o2.getSerialNum().doubleValue());
                    if(c == 0) {
                        if(Objects.equals(o1.getId(), lastSavedId))
                            return -1;
                        else if(Objects.equals(lastSavedId, o2.getId()))
                            return 1;
                    }
                    return c;
                }
            });
		}
 
		// Adding ref to save inside list
		double valS = val;
		refListToSave.add(savedRef);
		for (RefConfigCodeList ref : refList) {
			System.out.println("val -> " + val);
			System.out.println("ref -> " + ref.getSerialNum().doubleValue());
			
			if (valS > ref.getSerialNum().doubleValue()) {
				continue;
			}
			

//			if (savedRef.getId().equals(ref.getId()))
//				continue;

			ref.setSerialNum(new BigDecimal(val++));
			refListToSave.add(ref);

			System.out.println("REF     : #" + ref.getSerialNum().doubleValue()
					+ ", code : " + ref.getCodelistInternalValue());
		}

		if (!refListToSave.isEmpty()) {
			try {
				refCodeListService.update(refListToSave, this.authService
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
		StreamedContent content = refCodeListService.generateReport(this.codelist);
		setExcelFile(content); 
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
     * @param usergroups the usergroups to set
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
     * @param usergroups the usergroups to set
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
     * @param cmqImpactTypes the cmqImpactTypes to set
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
     * @param smqImpactTypes the smqImpactTypes to set
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
     * @param meddraImpactTypes the meddraImpactTypes to set
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
