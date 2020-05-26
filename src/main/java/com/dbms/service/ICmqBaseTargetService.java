package com.dbms.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import org.primefaces.model.SortOrder;
import org.primefaces.model.StreamedContent;
import org.primefaces.model.TreeNode;

import com.dbms.entity.cqt.CmqBaseTarget;
import com.dbms.entity.cqt.CmqProductBaseTarget;
import com.dbms.service.base.ICqtPersistenceService;

public interface ICmqBaseTargetService  extends ICqtPersistenceService<CmqBaseTarget>{

	List<Map<String, Object>> findImpactedWithPaginated(int first, int pageSize, String sortField, SortOrder sortOrder,
			Map<String, Object> filters);

	Long findImpactedCount();

	List<Map<String, Object>> findNotImpactedWithPaginated(int first, int pageSize, String sortField, SortOrder sortOrder,
			Map<String, Object> filters);

	Long findNotImpactedCount();

	CmqBaseTarget findByCode(Long cmqCode);

	List<Map<String, Object>> findCmqChildCountForParentCmqCodes(List<Long> cmqCodes);

	Long findCmqChildCountForParentCmqCode(Long cmqCode);

	List<CmqBaseTarget> findChildCmqsByParentCode(Long code);

	List<CmqBaseTarget> findByLevelAndTerm(Integer level, String searchTerm);

	StreamedContent generateCMQExcel(CmqBaseTarget selectedImpactedCmqList, String dictionaryVersion,TreeNode selectedNode, boolean filterLltFlag);

	List<CmqBaseTarget> findApprovedCmqs();

	List<CmqBaseTarget> findChildCmqsByCodes(List<Long> targetCmqCodes);

	List<CmqBaseTarget> findParentCmqsByCodes(List<Long> targetCmqParentCodes);

	List<CmqBaseTarget> findPublishedCmqs();
    
    boolean isVersionUpgradePending();
    
	BigDecimal findImpactedCount(Map<String, Object> filters);
	BigDecimal findNotImpactedCount(Map<String, Object> filters);

	List<CmqProductBaseTarget> findProductsByCmqCode(Long code);
}