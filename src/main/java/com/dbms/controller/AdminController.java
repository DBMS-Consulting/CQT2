package com.dbms.controller;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.RequestScoped;
import javax.faces.context.FacesContext;

import org.primefaces.event.RowEditEvent;

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

	private List<CodelistDTO> products, protocols, extensions, programs;

	private String codelistType;
	List<CodelistDTO> list;
	private String codelist;
	


	public AdminController() {

	}

	@PostConstruct
	public void init() {
		// TODO To test UI of ADMIN MODULE - Will be removed
		initValuesForAdmin();
		codelist = "";
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
	
	/******
	 * 
	 * 
	 * 
	 * 
	 * Dummy test on the ADMIN MODULE - To REMOVE if we have a service TODO
	 * 
	 * 
	 */
	public void addValue() {
		list.add(new CodelistDTO());
	}

	public void cancel() {
		for (CodelistDTO codel : list) {
			if (codel.getName() == null)
				list.remove(codel);
		}
	}
	
	public void onRowCancel(RowEditEvent event) {
		FacesMessage msg = new FacesMessage("Canceled", "");
		FacesContext.getCurrentInstance().addMessage(null, msg);
	}
	
	public void onRowEdit(RowEditEvent event) {
		
	}

	public List<CodelistDTO> getProducts() {
		return products;
	}

	public void setProducts(List<CodelistDTO> products) {
		this.products = products;
	}

	public List<CodelistDTO> getProtocols() {
		return protocols;
	}

	public void setProtocols(List<CodelistDTO> protocols) {
		this.protocols = protocols;
	}

	public List<CodelistDTO> getExtensions() {
		return extensions;
	}

	public void setExtensions(List<CodelistDTO> extensions) {
		this.extensions = extensions;
	}

	public List<CodelistDTO> getPrograms() {
		return programs;
	}

	public void setPrograms(List<CodelistDTO> programs) {
		this.programs = programs;
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

}
