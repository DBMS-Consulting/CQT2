package com.dbms.service;

import java.util.List;
import java.util.Map;

import com.dbms.entity.cqt.CmqRelation190;
import com.dbms.entity.cqt.CmqRelationTarget;
import com.dbms.service.base.ICqtPersistenceService;
import com.dbms.util.exceptions.CqtServiceException;

public interface ICmqRelationTargetService extends ICqtPersistenceService<CmqRelationTarget>{

	void create(List<CmqRelationTarget> cmqRelations) throws CqtServiceException;

	List<CmqRelationTarget> findByCmqCode(Long cmqCode);
	
	List<CmqRelationTarget> findByCmqCode(Long cmqCode, int startPosition, int limit);

	List<Map<String, Object>> findCountByCmqCodes(List<Long> cmqCodes);

	Long findCountByCmqCode(Long cmqCode);
	
	List<CmqRelationTarget> findByCmqCodeAndImpactTypes(Long cmqCode,List<String> impactTypes);

}