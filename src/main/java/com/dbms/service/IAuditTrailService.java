package com.dbms.service;

import java.util.List;

import com.dbms.entity.cqt.dtos.AuditTrailDto;

public interface IAuditTrailService {
	
	List<AuditTrailDto> findByCriterias(String listName, Long listCode, int dictionaryVersion, String auditTimeStamp);

}
