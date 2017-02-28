package com.dbms.controller;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.RequestScoped;
import javax.faces.context.FacesContext;

import org.primefaces.event.RowEditEvent;

import com.dbms.entity.cqt.RefConfigCodeList;
import com.dbms.service.IRefCodeListService;
import com.dbms.util.CqtConstants;
import com.dbms.util.OrderBy;
import com.dbms.web.dto.CodelistDTO;

/**
 * @date Feb 7, 2017 7:39:34 AM
 **/
@ManagedBean
@RequestScoped
public class AdminController implements Serializable {

	/**
	 * serialVersionUID.
	 */
	private static final long serialVersionUID = 1085292862045772511L;

//	private List<CodelistDTO> products, protocols, extensions, programs;

	private String codelistType;
	List<CodelistDTO> list;
	private String codelist;

	@ManagedProperty("#{RefCodeListService}")
	private IRefCodeListService refCodeListService;

	private List<RefConfigCodeList> extensions, programs, protocols, products;

	public AdminController() {

	}

	@PostConstruct
	public void init() {
		//initValuesForAdmin();
		codelist = "PROGRAM";
		getExtensionList();
		getProductList();
		getProgramList();
		getProtocolList();
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

	private void initValuesForAdmin() {
		list = new ArrayList<CodelistDTO>();
		CodelistDTO c = new CodelistDTO();
		c.setSequence("1");
		if (codelist != null && codelist.equals("PRODUCT"))
			c.setName("Product 1");
		if (codelist != null && codelist.equals("PROGRAM"))
			c.setName("Program 1");
		if (codelist != null && codelist.equals("PROTOCOL"))
			c.setName("Protocol 1");
		if (codelist != null && codelist.equals("EXTENSION"))
			c.setName("Extension 1");
		c.setActiveValue(true);
		c.setDefaultValue(true);
		list.add(c);

		c = new CodelistDTO();
		c.setSequence("2");
		if (codelist != null && codelist.equals("PRODUCT"))
			c.setName("Product 2");
		if (codelist != null && codelist.equals("PROGRAM"))
			c.setName("Program 2");
		if (codelist != null && codelist.equals("PROTOCOL"))
			c.setName("Protocol 2");
		if (codelist != null && codelist.equals("EXTENSION"))
			c.setName("Extension 2");
		c.setActiveValue(true);
		c.setDefaultValue(false);
		list.add(c);

		c = new CodelistDTO();
		c.setSequence("3");
		if (codelist != null && codelist.equals("PRODUCT"))
			c.setName("Product 3");
		if (codelist != null && codelist != null && codelist.equals("PROGRAM"))
			c.setName("Program 3");
		if (codelist != null && codelist.equals("PROTOCOL"))
			c.setName("Protocol 3");
		if (codelist != null && codelist.equals("EXTENSION"))
			c.setName("Extension 3");
		c.setActiveValue(true);
		c.setDefaultValue(false);
		list.add(c);
	}

	public void switchTable() {
		initValuesForAdmin();
	}

	 
	public void addProgram() {
		programs.add(new RefConfigCodeList());
	}
	
	public void addExtension() {
		extensions.add(new RefConfigCodeList());
	}
	
	public void addProtocol() {
		protocols.add(new RefConfigCodeList());
	}
	
	public void addProduct() {
		products.add(new RefConfigCodeList());
	}

	public void cancelProgram() {
		for (RefConfigCodeList ref : programs) {
			if (ref.getCodelistConfigType() == null || ref.getValue() == null)
				programs.remove(ref);
		}
	}
	
	public void cancelProtocol() {
		for (RefConfigCodeList ref : protocols) {
			if (ref.getCodelistConfigType() == null || ref.getValue() == null)
				protocols.remove(ref);
		}
	}
	
	public void cancelProduct() {
		for (RefConfigCodeList ref : products) {
			if (ref.getCodelistConfigType() == null || ref.getValue() == null)
				products.remove(ref);
		}
	}
	
	public void cancelExtension() {
		for (RefConfigCodeList ref : extensions) {
			if (ref.getCodelistConfigType() == null || ref.getValue() == null)
				extensions.remove(ref);
		}
	}

	public void onRowCancel(RowEditEvent event) {
		FacesMessage msg = new FacesMessage("Canceled", "");
		FacesContext.getCurrentInstance().addMessage(null, msg);
	}

	public void onRowEdit(RowEditEvent event) {

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

}
