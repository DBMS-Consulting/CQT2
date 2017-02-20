package com.dbms.controller;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.faces.bean.ApplicationScoped;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;

import com.dbms.entity.cqt.RefConfigCodeList;
import com.dbms.service.IRefCodeListService;
import com.dbms.util.CqtConstants;
import com.dbms.util.OrderBy;

/**
 * Controller that will contains codelist methods.
 * 
 * @author
 *
 */
@ManagedBean(name = "configMB")
@ApplicationScoped
public class ConfigurationController implements Serializable {

	/**
	 * serialVersionUID.
	 */
	private static final long serialVersionUID = 5539162862901321913L;

	@ManagedProperty("#{RefCodeListService}")
	private IRefCodeListService refCodeListService;

	private List<RefConfigCodeList> extensions, programs, protocols, products;

	@PostConstruct
	public void init() {
		getExtensionList();
		getProgramList();
		getProtocolList();
		getProductList();
	}

	/**
	 * Returns extensions list.
	 * 
	 * @return
	 */
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

	public IRefCodeListService getRefCodeListService() {
		return refCodeListService;
	}

	public void setRefCodeListService(IRefCodeListService refCodeListService) {
		this.refCodeListService = refCodeListService;
	}

	public List<RefConfigCodeList> getPrograms() {
		return programs;
	}

	public void setPrograms(List<RefConfigCodeList> programs) {
		this.programs = programs;
	}

	public List<RefConfigCodeList> getProtocols() {
		return protocols;
	}

	public void setProtocols(List<RefConfigCodeList> protocols) {
		this.protocols = protocols;
	}

	public List<RefConfigCodeList> getProducts() {
		return products;
	}

	public void setProducts(List<RefConfigCodeList> products) {
		this.products = products;
	}

	public List<RefConfigCodeList> getExtensions() {
		return extensions;
	}

	public void setExtensions(List<RefConfigCodeList> extensions) {
		this.extensions = extensions;
	}
}
