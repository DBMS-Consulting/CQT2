package com.dbms.service;

import java.util.List;

import com.dbms.entity.cqt.dtos.HistoricalViewDbDataDTO;
import com.dbms.entity.cqt.dtos.ParentChildAuditDBDataDTO;

public interface IHistoricalViewService {

	List<HistoricalViewDbDataDTO> findByCriterias(String listCode, String dictionaryVersion,
			String auditTimeStamp);
	
	List<ParentChildAuditDBDataDTO> findHistoricalParentsByCmqId(Long childCmqId, String auditTimeStampString);
	List<ParentChildAuditDBDataDTO> findHistoricalChildsByCmqId(Long parentCmqId, String auditTimeStampString);

}
