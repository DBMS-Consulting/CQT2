package com.dbms.controller;

import java.io.Serializable;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dbms.entity.IEntity;

/**
 * @date Feb 7, 2017 7:41:29 AM
 **/
public abstract class BaseController<T extends IEntity> implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	protected final Logger log=LoggerFactory.getLogger(this.getClass());
	
	protected List<T> datas;
	protected T selectedData;
	
	abstract String search();

	public List<T> getDatas() {
		return datas;
	}

	public void setDatas(List<T> datas) {
		this.datas = datas;
	}

	public T getSelectedData() {
		return selectedData;
	}

	public void setSelectedData(T selectedData) {
		this.selectedData = selectedData;
	}

	

}

