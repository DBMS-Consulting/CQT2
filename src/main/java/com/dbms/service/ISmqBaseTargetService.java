package com.dbms.service;

import java.util.List;
import java.util.Map;

import org.primefaces.model.SortOrder;
import org.primefaces.model.StreamedContent;

import com.dbms.entity.cqt.SmqBaseTarget;
import com.dbms.entity.cqt.SmqRelationTarget;
import com.dbms.entity.cqt.dtos.SMQReverseHierarchySearchDto;

public interface ISmqBaseTargetService {

	List<SmqBaseTarget> findByLevelAndTerm(Integer level, String searchTerm);

	Long findSmqRelationsCountForSmqCode(Long smqCode);

	List<Map<String, Object>> findSmqRelationsCountForSmqCodes(List<Long> smqCodes);

	List<SmqRelationTarget> findSmqRelationsForSmqCode(Long smqCode);
	//public List<SmqRelationTarget> findSmqRelationsForSmqCodeOrderByName(Long smqCode);

	SmqRelationTarget findSmqRelationBySmqAndPtCode(Long smqCode, Integer ptCode);

	List<SmqRelationTarget> findSmqRelationsForSmqCodes(List<Long> smqCodes);

	List<SmqBaseTarget> findChildSmqByParentSmqCodes(List<Long> smqCodes);
	public List<SmqBaseTarget> findChildSmqByParentSmqCodesOrderByName(List<Long> smqCodes);

	List<SmqBaseTarget> findChildSmqByParentSmqCode(Long smqCode);

	Long findChildSmqCountByParentSmqCode(Long smqCode);

	SmqBaseTarget findByCode(Long smqCode);
    List<SmqBaseTarget> findByCodes(List<Long> smqCodes);

	List<SmqBaseTarget> findImpactedWithPaginated(int first, int pageSize, String sortField, SortOrder sortOrder,
			Map<String, Object> filters);

	Long findImpactedCount(Map<String, Object> filters);

	List<SmqBaseTarget> findNotImpactedWithPaginated(int first, int pageSize, String sortField, SortOrder sortOrder,
			Map<String, Object> filters);

	Long findNotImpactedCount(Map<String, Object> filters);

	StreamedContent generateSMQExcel(SmqBaseTarget selectedImpactedSmqList, String dictionaryVersion);

	List<SmqRelationTarget> findSmqRelationsForSmqCodeAndScope(Long smqCode, String scope);
	
	List<SMQReverseHierarchySearchDto> findFullReverseByLevelAndTerm(String level, String myFilterTermName);

	List<SMQReverseHierarchySearchDto> findReverseByLevelAndTerm(Integer smqLevel, String smqName);

	List<Map<String, Object>> findParentCountSmqCountByChildSmqCodes(List<Long> smqCodes);

	List<SMQReverseHierarchySearchDto> findReverseParentByChildCode(Long smqCode);

	List<Map<String, Object>> findSmqChildRelationsCountForSmqCodes(List<Long> smqCodes);

}