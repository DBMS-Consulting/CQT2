package com.dbms.service;

import java.util.List;

import com.dbms.entity.cqt.dtos.HistoricalViewDbDataDTO;

public interface IHistoricalViewService {

	List<HistoricalViewDbDataDTO> findByCriterias(String listCode, String dictionaryVersion,
			String auditTimeStamp);

}
