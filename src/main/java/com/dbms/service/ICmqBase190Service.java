package com.dbms.service;

import java.util.List;

import com.dbms.entity.cqt.CmqBase190;
import com.dbms.service.base.ICqtPersistenceService;
import com.dbms.util.exceptions.CqtServiceException;

public interface ICmqBase190Service extends ICqtPersistenceService<CmqBase190> {

	List<CmqBase190> findByCriterias(String extension, String drugProgram, String protocol, String product,
			Integer level, String status, String state, String criticalEvent, String group, String termName, Long code);

	List<String> findTypes();

	List<String> findReleaseStatus();

	Long getNextCodeValue() throws CqtServiceException;

	CmqBase190 findByCode(Long cmqCode);

	List<CmqBase190> findByLevelAndTerm(Integer level, String searchTerm);
	
	Long findCmqChildCountForParentCmqCode(Long cmqCode);
	
	List<CmqBase190> findApprovedCmqs();
	
	List<CmqBase190> findChildCmqsByCodes(List<Long> codes);
	
	List<CmqBase190> findParentCmqsByCodes(List<Long> codes);
}