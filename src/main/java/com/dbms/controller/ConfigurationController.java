package com.dbms.controller;

import java.io.Serializable;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.faces.bean.ApplicationScoped;
import javax.faces.bean.ManagedBean;

import com.dbms.entity.cqt.ExtentionConfigCodeList;

/**
 * Controller that will contains codelist methods.
 * 
 * @author
 *
 */
@ManagedBean(name = "configMB")
@ApplicationScoped
public class ConfigurationController implements Serializable {

	private List<ExtentionConfigCodeList> extensions;

	/**
	 * serialVersionUID.
	 */
	private static final long serialVersionUID = 5539162862901321913L;

	@PostConstruct
	public void init() {
		getExtensionList();
	}

	/**
	 * Returns extensions list.
	 * 
	 * @return
	 */
	public List<ExtentionConfigCodeList> getExtensionList() {

		// TODO
		// Call the service that returns extensions list
		//extensions = ?; 

		return extensions;
	}

	public List<ExtentionConfigCodeList> getExtensions() {
		return extensions;
	}

	public void setExtensions(List<ExtentionConfigCodeList> extensions) {
		this.extensions = extensions;
	}

}
