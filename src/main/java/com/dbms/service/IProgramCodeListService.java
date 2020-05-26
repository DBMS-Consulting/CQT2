package com.dbms.service;

import com.dbms.entity.cqt.ProgramConfigCodeList;
import com.dbms.service.base.ICqtPersistenceService;

public interface IProgramCodeListService extends ICqtPersistenceService<ProgramConfigCodeList> {

	ProgramConfigCodeList findByValue(String value);

}