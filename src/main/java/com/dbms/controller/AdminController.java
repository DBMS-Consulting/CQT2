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
import javax.faces.context.FacesContext;

import org.primefaces.model.StreamedContent;

import com.dbms.entity.cqt.RefConfigCodeList;
import com.dbms.service.IRefCodeListService;
import com.dbms.util.CqtConstants;
import com.dbms.util.OrderBy;
import com.dbms.util.exceptions.CqtServiceException;
import com.dbms.web.dto.CodelistDTO;
import java.util.Comparator;
import java.util.Objects;

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

	private String codelistType;
	List<CodelistDTO> list;
	private String codelist;

	@ManagedProperty("#{RefCodeListService}")
	private IRefCodeListService refCodeListService;

	private List<RefConfigCodeList> extensions, programs, protocols, products, meddras, workflows;
	
	private RefConfigCodeList selectedRow, myFocusRef;
	private StreamedContent excelFile;

	public AdminController() {

	}

	@PostConstruct
	public void init() {
		codelist = "EXTENSION";
		getExtensionList();
		getProductList();
		getProgramList();
		getProtocolList();
		getMeddraList();
		getWorkflowList();
	}
	
	public String initAddCodelist() {
		BigDecimal lastSerial = new BigDecimal(0);
		myFocusRef = new RefConfigCodeList();
		if (codelist.equals("EXTENSION")) {
			myFocusRef.setCodelistConfigType(CqtConstants.CODE_LIST_TYPE_EXTENSION); 
			if (extensions != null && !extensions.isEmpty())
				lastSerial = extensions.get(extensions.size() - 1).getSerialNum();
		}
		if (codelist.equals("PRODUCT")) {
			myFocusRef.setCodelistConfigType(CqtConstants.CODE_LIST_TYPE_PRODUCT); 
			if (products != null && !products.isEmpty())
				lastSerial = products.get(products.size() - 1).getSerialNum();
		}
		if (codelist.equals("PROGRAM")) {
			myFocusRef.setCodelistConfigType(CqtConstants.CODE_LIST_TYPE_PROGRAM); 
			if (programs != null && !programs.isEmpty())
				lastSerial = programs.get(programs.size() - 1).getSerialNum();
		}
		if (codelist.equals("PROTOCOL")) {
			myFocusRef.setCodelistConfigType(CqtConstants.CODE_LIST_TYPE_PROTOCOL); 
			if (protocols != null && !protocols.isEmpty())
				lastSerial = protocols.get(protocols.size() - 1).getSerialNum();
		}
		if (codelist.equals("MEDDRA")) {
			myFocusRef.setCodelistConfigType(CqtConstants.CODE_LIST_TYPE_MEDDRA_VERSIONS); 
			if (meddras != null && !meddras.isEmpty())
				lastSerial = meddras.get(meddras.size() - 1).getSerialNum();
		}
		if (codelist.equals("WORKFLOW")) {
			myFocusRef.setCodelistConfigType(CqtConstants.CODE_LIST_TYPE_WORKFLOW_STATES); 
			if (workflows != null && !workflows.isEmpty())
				lastSerial = workflows.get(workflows.size() - 1).getSerialNum();
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
		System.out.println("\n\n ************** extensionLIST");
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
		System.out.println("\n\n ************** programLIST");
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
	
	public void addRefCodelist() {
		myFocusRef.setCreatedBy("test-user");
		myFocusRef.setLastModifiedBy("test-user"); 
		//To uppercase for workflow State
		if (myFocusRef.getCodelistConfigType().equals(CqtConstants.CODE_LIST_TYPE_WORKFLOW_STATES))
			myFocusRef.setValue(myFocusRef.getValue().toUpperCase());
			
		if (myFocusRef.getCodelistInternalValue() != null)
			myFocusRef.setCodelistInternalValue(myFocusRef.getCodelistInternalValue().toUpperCase());
		try {
			if (myFocusRef.getId() != null)
				refCodeListService.update(myFocusRef);
			else {
				refCodeListService.create(myFocusRef);
			}
			updateSerialNumbers(myFocusRef.getCodelistConfigType(), myFocusRef);
			String type = "";
			
			if (myFocusRef.getCodelistConfigType().equals(CqtConstants.CODE_LIST_TYPE_EXTENSION)) {
				type = "Extension";
				getExtensionList();
			}
			if (myFocusRef.getCodelistConfigType().equals(CqtConstants.CODE_LIST_TYPE_MEDDRA_VERSIONS)) {
				type = "MedDRA Dictionary";
				getMeddraList();
			}
			if (myFocusRef.getCodelistConfigType().equals(CqtConstants.CODE_LIST_TYPE_PRODUCT)) {
				type = "Product";
				getProductList();
			}
			if (myFocusRef.getCodelistConfigType().equals(CqtConstants.CODE_LIST_TYPE_PROGRAM)) {
				type = "Program";
				getProgramList();
			}
			if (myFocusRef.getCodelistConfigType().equals(CqtConstants.CODE_LIST_TYPE_PROTOCOL)) {
				type = "Protocol"; 
				getProtocolList();
			}
			if (myFocusRef.getCodelistConfigType().equals(CqtConstants.CODE_LIST_TYPE_WORKFLOW_STATES)) {
				type = "Workflow State";
				getWorkflowList();
			}
			FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_INFO,
					type + " '" + myFocusRef.getCodelistInternalValue() + "' is successfully saved.", "");
			FacesContext.getCurrentInstance().addMessage(null, msg);
		} catch (CqtServiceException e) {
			e.printStackTrace();
			FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR,
					"An error occurred while creating an extension code", "");
			FacesContext.getCurrentInstance().addMessage(null, msg);
		}
		myFocusRef = new RefConfigCodeList();
	}

	private void updateSerialNumbers(String codelistConfigType, RefConfigCodeList savedRef) {
		double val = 0;
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
					refCodeListService.update(refListToSave);
				} catch (CqtServiceException e) {
					e.printStackTrace();
					FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR,
							"An error occurred while saving the codelist", "");
					FacesContext.getCurrentInstance().addMessage(null, msg);
				}
            }
		}
	}
	
	public void generateConfigReport() {
		StreamedContent content = refCodeListService.generateReport(this.codelist);
		setExcelFile(content); 
	}

	public String getCodelistType() {
		return codelistType;
	}

	public void setCodelistType(String codelistType) {
		this.codelistType = codelistType;
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

}
