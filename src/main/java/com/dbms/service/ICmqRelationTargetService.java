package com.dbms.service;

import java.util.List;
import java.util.Map;

import com.dbms.entity.cqt.CmqRelationTarget;
import com.dbms.util.exceptions.CqtServiceException;

public interface ICmqRelationTargetService {

	void create(List<CmqRelationTarget> cmqRelations) throws CqtServiceException;

	List<CmqRelationTarget> findByCmqCode(Long cmqCode);

	List<Map<String, Object>> findCountByCmqCodes(List<Long> cmqCodes);

	Long findCountByCmqCode(Long cmqCode);

}