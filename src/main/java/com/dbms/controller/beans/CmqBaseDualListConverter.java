package com.dbms.controller.beans;

import java.util.List;

import javax.faces.bean.ManagedProperty;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.FacesConverter;

import org.primefaces.component.picklist.PickList;
import org.primefaces.model.DualListModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dbms.controller.PublishController;
import com.dbms.entity.cqt.CmqBase190;

@FacesConverter(value = "cmqBaseDualListConverter")
public class CmqBaseDualListConverter implements Converter {

	private static final Logger LOG = LoggerFactory.getLogger(CmqBaseDualListConverter.class);
	
	@ManagedProperty("#{publishController}")
	private PublishController publishController;
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public Object getAsObject(FacesContext context, UIComponent component, String value) {
		long inputValue = 0;
		try{
			inputValue = Long.valueOf(value);
		} catch (Exception e) {
			LOG.error(e.getMessage(), e);
		}
		PickList p = (PickList) component;
	    DualListModel<CmqBase190> dl = (DualListModel) p.getValue();
		List<CmqBase190> sourceList = dl.getSource();
		for (CmqBase190 cmqBase190 : sourceList) {
			if(cmqBase190.getCmqCode().longValue() == inputValue) {
				return cmqBase190;
			}
		}
		return null;
	}

	@Override
	public String getAsString(FacesContext context, UIComponent component, Object value) {
		return value.toString();
	}

}
