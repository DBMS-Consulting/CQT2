package com.dbms.service;

import java.util.List;
import java.util.Map;

import org.primefaces.model.SortOrder;

import com.dbms.entity.cqt.SmqBaseTarget;
import com.dbms.entity.cqt.SmqRelationTarget;

public interface ISmqBaseTargetService {

	List<SmqBaseTarget> findByLevelAndTerm(Integer level, String searchTerm);

	Long findSmqRelationsCountForSmqCode(Long smqCode);

	List<Map<String, Object>> findSmqRelationsCountForSmqCodes(List<Long> smqCodes);

	List<SmqRelationTarget> findSmqRelationsForSmqCode(Long smqCode);

	SmqRelationTarget findSmqRelationBySmqAndPtCode(Long smqCode, Integer ptCode);

	List<SmqRelationTarget> findSmqRelationsForSmqCodes(List<Long> smqCodes);

	List<SmqBaseTarget> findChildSmqByParentSmqCodes(List<Long> smqCodes);

	List<SmqBaseTarget> findChildSmqByParentSmqCode(Long smqCode);

	Long findChildSmqCountByParentSmqCode(Long smqCode);

	SmqBaseTarget findByCode(Long smqCode);

	List<SmqBaseTarget> findImpactedWithPaginated(int first, int pageSize, String sortField, SortOrder sortOrder,
			Map<String, Object> filters);

	Long findImpactedCount();

	List<SmqBaseTarget> findNotImpactedWithPaginated(int first, int pageSize, String sortField, SortOrder sortOrder,
			Map<String, Object> filters);

	Long findNotImpactedCount();

}