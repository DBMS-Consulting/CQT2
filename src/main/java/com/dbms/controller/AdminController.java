package com.dbms.controller;

import java.io.Serializable;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.RequestScoped;
import javax.faces.event.AjaxBehaviorEvent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dbms.web.dto.AdminDTO;

/**
 * @date Feb 7, 2017 7:39:34 AM
 **/
@ManagedBean(name = "adminMB")
@RequestScoped
public class AdminController implements Serializable {

	/**
	 * serialVersionUID.
	 */
	private static final long serialVersionUID = 1085292862045772511L;

	private static final Logger log = LoggerFactory
			.getLogger(AdminController.class);

	private List<AdminDTO> products, protocols, extensions, programs;
	
	private String codelistType;

	public AdminController() {

	}

	@PostConstruct
	public void init() {

	}

	/**
	 * 
	 * @param event
	 *            AjaxBehaviour
	 */
	public void changeLevel(AjaxBehaviorEvent event) {

	}

	public List<AdminDTO> getProducts() {
		return products;
	}

	public void setProducts(List<AdminDTO> products) {
		this.products = products;
	}

	public List<AdminDTO> getProtocols() {
		return protocols;
	}

	public void setProtocols(List<AdminDTO> protocols) {
		this.protocols = protocols;
	}

	public List<AdminDTO> getExtensions() {
		return extensions;
	}

	public void setExtensions(List<AdminDTO> extensions) {
		this.extensions = extensions;
	}

	public List<AdminDTO> getPrograms() {
		return programs;
	}

	public void setPrograms(List<AdminDTO> programs) {
		this.programs = programs;
	}

	public String getCodelistType() {
		return codelistType;
	}

	public void setCodelistType(String codelistType) {
		this.codelistType = codelistType;
	}

}
