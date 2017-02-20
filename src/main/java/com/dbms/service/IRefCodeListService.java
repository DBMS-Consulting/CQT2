package com.dbms.service;

import com.dbms.entity.cqt.RefConfigCodeList;
import com.dbms.service.base.ICqtPersistenceService;
import com.dbms.util.OrderBy;

public interface IRefCodeListService extends ICqtPersistenceService<RefConfigCodeList> {

	public RefConfigCodeList findByConfigType(String codelistConfigType, OrderBy orderBy);

}