package com.dbms.service;

import java.util.List;
import java.util.Map;

import com.dbms.entity.cqt.CmqRelation190;
import com.dbms.service.base.ICqtPersistenceService;
import com.dbms.util.exceptions.CqtServiceException;

public interface ICmqRelation190Service extends ICqtPersistenceService<CmqRelation190> {

	void create(List<CmqRelation190> cmqRelations) throws CqtServiceException;
	
	List<CmqRelation190> findByCmqCode(Long cmqCode);
	
	List<Map<String, Object>> findCountByCmqCodes(List<Long> cmqCodes);
	
	Long findCountByCmqCode(Long cmqCode);
	Long findCountByCmqCode(Long cmqCode,String dictionaryVersion);

	List<CmqRelation190> findByCmqCode(Long cmqCode, int startPosition, int limit);

	List<Map<String, Object>> findCountByCmqCodes(List<Long> childCmqCodeList, String dictionaryVersion);

	List<CmqRelation190> findByCmqCode(Long cmqCode, String dictionaryVersion);
	List<CmqRelation190> findByCmqCodeAndImpactTypes(Long cmqCode,List<String> impactTypes);
}