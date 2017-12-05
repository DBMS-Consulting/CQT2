package com.dbms.service;

import java.util.List;
import java.util.Map;

import com.dbms.entity.cqt.CmqBase190;
import com.dbms.entity.cqt.CmqParentChild200;
import com.dbms.service.base.ICqtPersistenceService;
import com.dbms.util.exceptions.CqtServiceException;

public interface ICmqParentChild200Service extends ICqtPersistenceService<CmqParentChild200> {

	void create(List<CmqParentChild200> cmqParentChildRelations) throws CqtServiceException;
	
	List<CmqParentChild200> findParentsByCmqCode(Long childCmqCode);
	
	List<CmqParentChild200> findChildsByCmqCode(Long parentCmqCode);
	
	List<CmqParentChild200> findByParentOrChildCmqCode(Long cmqCode);
	
	Long findCmqChildCountForParentCmqCode(Long parentCmqCode);
	
	Long findCmqParentCountForChildCmqCode(Long childCmqCode);
	
	List<Map<String, Object>> findCmqChildCountForParentCmqCodes(List<Long> cmqCodes);
	
	List<CmqParentChild200> findChildCmqsByParentCodes(List<Long> parentCmqCodes);
	List<CmqParentChild200> findParentCmqsByChildCodes(List<Long> childCmqCodes);
		
	CmqParentChild200 findByParentAndChildCode (Long parentCmqCode,Long childCmqCode);
	
}