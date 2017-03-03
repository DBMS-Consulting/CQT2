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
import javax.faces.context.FacesContext;

import org.primefaces.event.RowEditEvent;

import com.dbms.entity.cqt.RefConfigCodeList;
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

	private String codelistType;
	List<CodelistDTO> list;
	private String codelist;

	@ManagedProperty("#{RefCodeListService}")
	private IRefCodeListService refCodeListService;

	private List<RefConfigCodeList> extensions, programs, protocols, products;
	
	private RefConfigCodeList selectedRow, ref;

	public AdminController() {

	}

	@PostConstruct
	public void init() {
		codelist = "EXTENSION";
//		getExtensionList();
//		getProductList();
//		getProgramList();
//		getProtocolList();
	}
	
	public String initAddCodelist() {
		ref = new RefConfigCodeList();
		if (codelist.equals("EXTENSION"))
			ref.setCodelistConfigType(CqtConstants.CODE_LIST_TYPE_EXTENSION); 
		if (codelist.equals("EXTENSION"))
			ref.setCodelistConfigType(CqtConstants.CODE_LIST_TYPE_PRODUCT); 
		if (codelist.equals("EXTENSION"))
			ref.setCodelistConfigType(CqtConstants.CODE_LIST_TYPE_PROGRAM); 
		if (codelist.equals("EXTENSION"))
			ref.setCodelistConfigType(CqtConstants.CODE_LIST_TYPE_PROTOCOL); 
		if (codelist.equals("EXTENSION"))
			ref.setCodelistConfigType(CqtConstants.CODE_LIST_TYPE_MEDDRA_VERSIONS); 
		ref.setCreationDate(new Date());
		ref.setLastModificationDate(new Date()); 
		
		return "";
	}
	
	public String initGetRef() {
		ref = new RefConfigCodeList();
		if (selectedRow == null) {
			FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR,
					"No selection was performed. Try again", "");
			FacesContext.getCurrentInstance().addMessage(null, msg);
			return "";
		}
		
		ref = refCodeListService.findById(selectedRow.getId());
		
		if (ref == null) {
			FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR,
					"The codeList selected does not exist.", "");
			FacesContext.getCurrentInstance().addMessage(null, msg);
		}
		
		return "";
	}

	public List<RefConfigCodeList> getExtensionList() {
		extensions = refCodeListService.findByConfigType(
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
		programs = refCodeListService.findByConfigType(
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
		protocols = refCodeListService.findByConfigType(
				CqtConstants.CODE_LIST_TYPE_PROTOCOL, OrderBy.ASC);
		if (protocols == null) {
			protocols = new ArrayList<>();
		}
		return protocols;
	}
	
	/**
	 * Returns products list.
	 * 
	 * @return
	 */
	public List<RefConfigCodeList> getProductList() {
		products = refCodeListService.findByConfigType(
				CqtConstants.CODE_LIST_TYPE_PRODUCT, OrderBy.ASC);
		if (products == null) {
			products = new ArrayList<>();
		}
		return products;
	}
	
	public void addRefCodelist() {
		ref.setCreatedBy("test-user");
		ref.setLastModifiedBy("test-user"); 
		ref.setCodelistInternalValue(ref.getCodelistInternalValue().toUpperCase());
		try {
			if (ref.getId() != null)
				refCodeListService.update(ref);
			else
				refCodeListService.create(ref);
			FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_INFO,
					"Extension '" + ref.getCodelistInternalValue() + "' is successfully saved.", "");
			FacesContext.getCurrentInstance().addMessage(null, msg);
		} catch (CqtServiceException e) {
			e.printStackTrace();
			FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR,
					"An error occured while creating an extension code", "");
			FacesContext.getCurrentInstance().addMessage(null, msg);
		}
		
		ref = new RefConfigCodeList();
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
		return ref;
	}

	public void setRef(RefConfigCodeList ref) {
		this.ref = ref;
	}

}
