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

import org.primefaces.model.StreamedContent;

import com.dbms.entity.cqt.RefConfigCodeList;
import com.dbms.service.AuthenticationService;
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
    
    private static final String CODELIST_EXTENSION = "EXTENSION";
    private static final String CODELIST_PRODUCT = "PRODUCT";
    private static final String CODELIST_PROGRAM = "PROGRAM";
    private static final String CODELIST_PROTOCOL = "PROTOCOL";
    private static final String CODELIST_MEDDRA = "MEDDRA";
    private static final String CODELIST_WORKFLOW = "WORKFLOW";
    private static final String CODELIST_USERGROUP = "USER_GROUPS";
    private static final String CODELIST_SYSCONFIG = "SYSTEM_CONFIG";
    private static final String CODELIST_CMQ_RELATION_IMPACT_TYPE = "CMQ_RELATION_IMPACT_TYPE";
    private static final String CODELIST_SMQ_RELATION_IMPACT_TYPE = "SMQ_RELATION_IMPACT_TYPE";
    private static final String CODELIST_MEDDRA_DICT_IMPACT_TYPE = "MEDDRA_DICT_IMPACT_TYPE";

	List<CodelistDTO> list;
	private String codelist;

	@ManagedProperty("#{RefCodeListService}")
	private IRefCodeListService refCodeListService;

	@ManagedProperty("#{AuthenticationService}")
	private AuthenticationService authService;
	
	private List<RefConfigCodeList> extensions, programs, protocols,
            products, meddras, workflows, usergroups, sysconfigs,
            cmqImpactTypes, smqImpactTypes, meddraImpactTypes;
	
	private RefConfigCodeList selectedRow, myFocusRef;
	private StreamedContent excelFile;

	public AdminController() {

	}

	@PostConstruct
	public void init() {
		codelist = CODELIST_EXTENSION;
		getExtensionList();
		getProductList();
		getProgramList();
		getProtocolList();
		getMeddraList();
		getWorkflowList();
        getUsergroupList();
        getSysconfigList();
        getCmqImpactTypeList();
        getSmqImpactTypeList();
        getMeddraImpactTypeList();
	}
	
	public String initAddCodelist() {
		BigDecimal lastSerial = new BigDecimal(0);
		myFocusRef = new RefConfigCodeList();
		if (codelist.equals(CODELIST_EXTENSION)) {
			myFocusRef.setCodelistConfigType(CqtConstants.CODE_LIST_TYPE_EXTENSION); 
			if (extensions != null && !extensions.isEmpty())
				lastSerial = extensions.get(extensions.size() - 1).getSerialNum();
		} else if (codelist.equals(CODELIST_PRODUCT)) {
			myFocusRef.setCodelistConfigType(CqtConstants.CODE_LIST_TYPE_PRODUCT); 
			if (products != null && !products.isEmpty())
				lastSerial = products.get(products.size() - 1).getSerialNum();
		} else if (codelist.equals(CODELIST_PROGRAM)) {
			myFocusRef.setCodelistConfigType(CqtConstants.CODE_LIST_TYPE_PROGRAM); 
			if (programs != null && !programs.isEmpty())
				lastSerial = programs.get(programs.size() - 1).getSerialNum();
		} else if (codelist.equals(CODELIST_PROTOCOL)) {
			myFocusRef.setCodelistConfigType(CqtConstants.CODE_LIST_TYPE_PROTOCOL); 
			if (protocols != null && !protocols.isEmpty())
				lastSerial = protocols.get(protocols.size() - 1).getSerialNum();
		} else if (codelist.equals(CODELIST_MEDDRA)) {
			myFocusRef.setCodelistConfigType(CqtConstants.CODE_LIST_TYPE_MEDDRA_VERSIONS); 
			if (meddras != null && !meddras.isEmpty())
				lastSerial = meddras.get(meddras.size() - 1).getSerialNum();
		} else if (codelist.equals(CODELIST_WORKFLOW)) {
			myFocusRef.setCodelistConfigType(CqtConstants.CODE_LIST_TYPE_WORKFLOW_STATES); 
			if (workflows != null && !workflows.isEmpty())
				lastSerial = workflows.get(workflows.size() - 1).getSerialNum();
		} else if (codelist.equals(CODELIST_USERGROUP)) {
			myFocusRef.setCodelistConfigType(CqtConstants.CODE_LIST_TYPE_USER_GROUPS); 
			if (usergroups != null && !usergroups.isEmpty())
				lastSerial = usergroups.get(usergroups.size() - 1).getSerialNum();
		} else if (codelist.equals(CODELIST_SYSCONFIG)) {
			myFocusRef.setCodelistConfigType(CqtConstants.CODE_LIST_TYPE_SYSTEM_CONFIG); 
			if (sysconfigs != null && !sysconfigs.isEmpty())
				lastSerial = sysconfigs.get(sysconfigs.size() - 1).getSerialNum();
		} else if (codelist.equals(CODELIST_CMQ_RELATION_IMPACT_TYPE)) {
			myFocusRef.setCodelistConfigType(CqtConstants.CODE_LIST_TYPE_CMQ_RELATION_IMPACT_TYPE); 
			if (getCmqImpactTypes() != null && !cmqImpactTypes.isEmpty())
				lastSerial = getCmqImpactTypes().get(getCmqImpactTypes().size() - 1).getSerialNum();
		} else if (codelist.equals(CODELIST_SMQ_RELATION_IMPACT_TYPE)) {
			myFocusRef.setCodelistConfigType(CqtConstants.CODE_LIST_TYPE_SMQ_RELATION_IMPACT_TYPE); 
			if (getSmqImpactTypes() != null && !smqImpactTypes.isEmpty())
				lastSerial = getSmqImpactTypes().get(getSmqImpactTypes().size() - 1).getSerialNum();
		} else if (codelist.equals(CODELIST_MEDDRA_DICT_IMPACT_TYPE)) {
			myFocusRef.setCodelistConfigType(CqtConstants.CODE_LIST_TYPE_MEDDRA_DICT_IMPACT_TYPE); 
			if (getMeddraImpactTypes() != null && !meddraImpactTypes.isEmpty())
				lastSerial = getMeddraImpactTypes().get(getMeddraImpactTypes().size() - 1).getSerialNum();
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
			extensions = new ArrayList<>();
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
			programs = new ArrayList<>();
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
			protocols = new ArrayList<>();
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
			meddras = new ArrayList<>();
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
			products = new ArrayList<>();
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
			workflows = new ArrayList<>();
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
			usergroups = new ArrayList<>();
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
			sysconfigs = new ArrayList<>();
		}
		return sysconfigs;
	}
    
    /**
	 * Returns CMQ List Impact Type Config codelist.
	 * @return
	 */
	public List<RefConfigCodeList> getCmqImpactTypeList() {
		setCmqImpactTypes(refCodeListService.findAllByConfigType(CqtConstants.CODE_LIST_TYPE_CMQ_RELATION_IMPACT_TYPE, OrderBy.ASC));
		if (getCmqImpactTypes() == null) {
			setCmqImpactTypes(new ArrayList<RefConfigCodeList>());
		}
		return getCmqImpactTypes();
	}
    
    /**
	 * Returns SMQ List Impact Type Config codelist.
	 * @return
	 */
	public List<RefConfigCodeList> getSmqImpactTypeList() {
		setSmqImpactTypes(refCodeListService.findAllByConfigType(CqtConstants.CODE_LIST_TYPE_SMQ_RELATION_IMPACT_TYPE, OrderBy.ASC));
		if (getSmqImpactTypes() == null) {
			setSmqImpactTypes(new ArrayList<RefConfigCodeList>());
		}
		return getSmqImpactTypes();
	}
    
    /**
	 * Returns Meddra List Impact Type Config codelist.
	 * @return
	 */
	public List<RefConfigCodeList> getMeddraImpactTypeList() {
		setMeddraImpactTypes(refCodeListService.findAllByConfigType(CqtConstants.CODE_LIST_TYPE_MEDDRA_DICT_IMPACT_TYPE, OrderBy.ASC));
		if (getMeddraImpactTypes() == null) {
			setMeddraImpactTypes(new ArrayList<RefConfigCodeList>());
		}
		return getMeddraImpactTypes();
	}
	
	public void addRefCodelist() {
		Date lastModifiedDate = new Date();
		String lastModifiedByString = this.authService.getLastModifiedByString();
		//To uppercase for workflow State
		if (myFocusRef.getCodelistConfigType().equals(CqtConstants.CODE_LIST_TYPE_WORKFLOW_STATES))
			myFocusRef.setValue(myFocusRef.getValue().toUpperCase());
			
		if (myFocusRef.getCodelistInternalValue() != null)
			myFocusRef.setCodelistInternalValue(myFocusRef.getCodelistInternalValue().toUpperCase());
		try {
			if (myFocusRef.getId() != null){
				myFocusRef.setLastModificationDate(lastModifiedDate);
				myFocusRef.setLastModifiedBy(lastModifiedByString);
				refCodeListService.update(myFocusRef, this.authService.getUserCn()
						, this.authService.getUserGivenName(), this.authService.getUserSurName()
						, this.authService.getCombinedMappedGroupMembershipAsString());
			} else {
				myFocusRef.setLastModificationDate(lastModifiedDate);
				myFocusRef.setLastModifiedBy(lastModifiedByString);
				myFocusRef.setCreationDate(lastModifiedDate);
				myFocusRef.setCreatedBy(lastModifiedByString);
				refCodeListService.create(myFocusRef, this.authService.getUserCn()
						, this.authService.getUserGivenName(), this.authService.getUserSurName()
						, this.authService.getCombinedMappedGroupMembershipAsString());
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
			}
			FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_INFO,
					type + " '" + myFocusRef.getCodelistInternalValue() + "' is successfully saved.", "");
			FacesContext.getCurrentInstance().addMessage(null, msg);
		} catch (CqtServiceException e) {
			e.printStackTrace();
			FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR,
					"An error occurred while creating an extension code", "Error:" + e.getMessage());
			FacesContext.getCurrentInstance().addMessage(null, msg);
		}
		myFocusRef = new RefConfigCodeList();
	}

	private void updateSerialNumbers(String codelistConfigType, RefConfigCodeList savedRef) {
		double val = 1;
		System.out.println("\n\n ********************* serialSaved :  " + savedRef.getSerialNum());
 		List<RefConfigCodeList> refList = refCodeListService.findAllByConfigType(codelistConfigType, OrderBy.ASC);
		List<RefConfigCodeList> refListToSave = new ArrayList<RefConfigCodeList>();
        
        final Long lastSavedId = savedRef.getId();
        
		if (refList != null && !refList.isEmpty()) {
            refList.sort(new Comparator<RefConfigCodeList> () {
                @Override
                public int compare(RefConfigCodeList o1, RefConfigCodeList o2) {
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
            for (RefConfigCodeList ref : refList) {
                ref.setSerialNum(new BigDecimal(val));
                refListToSave.add(ref);
                ++ val;
            }
			
			if (!refListToSave.isEmpty()) {
				try {
					refCodeListService.update(refListToSave, this.authService.getUserCn()
							, this.authService.getUserGivenName(), this.authService.getUserSurName()
							, this.authService.getCombinedMappedGroupMembershipAsString());
				} catch (CqtServiceException e) {
					e.printStackTrace();
					FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR,
							"An error occurred while saving the codelist", "Error: " + e.getMessage());
					FacesContext.getCurrentInstance().addMessage(null, msg);
				}
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
}
