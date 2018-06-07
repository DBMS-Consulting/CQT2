package com.dbms.service;

import java.util.List;
import java.util.Map;

import com.dbms.entity.cqt.CmqParentChildTarget;
import com.dbms.service.base.ICqtPersistenceService;

public interface ICmqParentChildTargetService extends ICqtPersistenceService<CmqParentChildTarget> {

	Long findCountByCmqCode(Long cmqCode);

	List<CmqParentChildTarget> findParentsByCmqCode(Long cmqCode);

	List<CmqParentChildTarget> findChildsByCmqCode(Long parentCmqCode);

	Long findCmqChildCountForParentCmqCode(Long parentCmqCode);

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.dbms.service.ICmqParentChildTargetService#
	 * findCmqParentCountForChildCmqCode(java.lang.Long)
	 */
	Long findCmqParentCountForChildCmqCode(Long childCmqCode);

	List<Map<String, Object>> findCmqChildCountForParentCmqCodes(List<Long> cmqCodes);

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.dbms.service.ICmqParentChildTargetService#findChildCmqsByParentCodes(
	 * java.util.List)
	 */
	List<CmqParentChildTarget> findChildCmqsByParentCodes(List<Long> parentCmqCodes);

	List<CmqParentChildTarget> findParentCmqsByChildCodes(List<Long> childCmqCodes);

	CmqParentChildTarget findByParentAndChildCode(Long parentCmqCode, Long childCmqCode);

	List<CmqParentChildTarget> findByParentOrChildCmqCode(Long cmqCode);

}