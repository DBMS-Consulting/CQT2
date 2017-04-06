package com.dbms.service;

import java.util.List;
import java.util.Map;

import org.primefaces.model.SortOrder;
import org.primefaces.model.StreamedContent;

import com.dbms.entity.cqt.CmqBaseTarget;
import com.dbms.service.base.ICqtPersistenceService;

public interface ICmqBaseTargetService  extends ICqtPersistenceService<CmqBaseTarget>{

	List<CmqBaseTarget> findImpactedWithPaginated(int first, int pageSize, String sortField, SortOrder sortOrder,
			Map<String, Object> filters);

	Long findImpactedCount();

	List<CmqBaseTarget> findNotImpactedWithPaginated(int first, int pageSize, String sortField, SortOrder sortOrder,
			Map<String, Object> filters);

	Long findNotImpactedCount();

	CmqBaseTarget findByCode(Long cmqCode);

	List<Map<String, Object>> findCmqChildCountForParentCmqCode(List<Long> cmqCodes);

	Long findCmqChildCountForParentCmqCode(Long cmqCode);

	List<CmqBaseTarget> findChildCmqsByParentCode(Long code);

	List<CmqBaseTarget> findByLevelAndTerm(Integer level, String searchTerm);

	StreamedContent generateCMQExcel(CmqBaseTarget selectedImpactedCmqList);

}