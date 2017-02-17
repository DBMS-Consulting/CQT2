package com.dbms.controller;

import java.io.Serializable;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.ViewScoped;

import com.dbms.entity.cqt.CmqBase190;
import com.dbms.service.ICmqBase190Service;

/**
 * @author Jay G.(jayshanchn@hotmail.com)
 * @date Feb 12, 2017 7:34:05 AM
 **/
@ManagedBean
@ViewScoped
public class CreateController implements Serializable{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -443251941538546278L;

	@ManagedProperty("#{CmqBase190Service}")
	private ICmqBase190Service cmqBaseService;
	
	private CmqBase190 selectedData;
	
	public CreateController(){
		this.selectedData=new CmqBase190();
	}

	public CmqBase190 getSelectedData() {
		return selectedData;
	}

	public void setSelectedData(CmqBase190 selectedData) {
		this.selectedData = selectedData;
	}

	public void setCmqBaseService(ICmqBase190Service cmqBaseService) {
		this.cmqBaseService = cmqBaseService;
	}

	public String save(){
		try {
			cmqBaseService.create(selectedData);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		return "/index.xhtml";
	}
}

