package com.dbms.service;

import java.util.List;

import org.primefaces.model.StreamedContent;

import com.dbms.entity.cqt.RefConfigCodeList;
import com.dbms.entity.cqt.dtos.AuditTrailDto;
import com.dbms.entity.cqt.dtos.CmqBaseDTO;

public interface IAuditTrailService {
	
	List<AuditTrailDto> findByCriterias(Long listCode, String listName, int dictionaryVersion, String auditTimeStamp);
	List<String> findAuditTimestamps(int dictionaryVersion, String code, String name,String timezone);
	List<CmqBaseDTO> findLists(List<RefConfigCodeList> dictionaryVersions);
	StreamedContent generateExcel(List<AuditTrailDto> list, String user, String timezone);
	List<String> findAuditTimestampsForHistoricalView(int dictionaryVersion, String code, String name,String timezone); 


}
