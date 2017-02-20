package com.dbms.service;

import com.dbms.entity.cqt.ExtentionConfigCodeList;
import com.dbms.service.base.ICqtPersistenceService;

public interface IExtensionCodeListService extends ICqtPersistenceService<ExtentionConfigCodeList> {

	ExtentionConfigCodeList findByValue(String value);

}