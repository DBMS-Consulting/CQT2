package com.dbms.service;

import java.util.List;

import com.dbms.entity.cqt.RefConfigCodeList;
import com.dbms.service.base.ICqtPersistenceService;
import com.dbms.util.OrderBy;

public interface IRefCodeListService extends ICqtPersistenceService<RefConfigCodeList> {

	public List<RefConfigCodeList> findByConfigType(String codelistConfigType, OrderBy orderBy);
	
	public List<RefConfigCodeList> findAllByConfigType(String codelistConfigType, OrderBy orderBy);


	public RefConfigCodeList getCurrentMeddraVersion();
	
	public String findCodeByInternalCode(String codelistInternalValue);
}