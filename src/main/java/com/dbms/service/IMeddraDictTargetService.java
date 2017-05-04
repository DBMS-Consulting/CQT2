package com.dbms.service;

import java.math.BigDecimal;
import java.util.List;

import com.dbms.entity.cqt.dtos.MeddraDictHierarchySearchDto;
import com.dbms.entity.cqt.dtos.MeddraDictReverseHierarchySearchDto;
import java.util.Map;

public interface IMeddraDictTargetService {

	List<MeddraDictReverseHierarchySearchDto> findFullReverseHierarchyByLevelAndTerm(String searchColumnPrefix,
			String partitionColumnPrefix, String searchTerm);

	List<MeddraDictReverseHierarchySearchDto> findReverseByCode(String searchColumnTypePrefix,
			String partitionColumnPrefix, Long code);

	List<MeddraDictHierarchySearchDto> findByLevelAndTerm(String searchColumnTypePrefix, String searchTerm);

	MeddraDictHierarchySearchDto findByCode(String searchColumnTypePrefix, Long code);

	MeddraDictReverseHierarchySearchDto findByPtOrLltCode(String searchColumnTypePrefix, Long code);
    List<MeddraDictReverseHierarchySearchDto> findByPtOrLltCodes(String searchColumnTypePrefix, List<Long> codes);

	List<MeddraDictHierarchySearchDto> findByCodes(String searchColumnTypePrefix, List<Long> codes);

	Long findChildrenCountByParentCode(String searchColumnTypePrefix, String parentCodeColumnPrefix, Long parentCode);
    List<Map<String, Object>> findChildrenCountByParentCodes(String searchColumnTypePrefix, String parentCodeColumnPrefix, List<Long> parentCodes);

	List<MeddraDictHierarchySearchDto> findChildrenByParentCode(String searchColumnTypePrefix,
			String parentCodeColumnPrefix, Long parentCode);
    List<MeddraDictHierarchySearchDto> findChildrenByParentCodes(String searchColumnTypePrefix,
			String parentCodeColumnPrefix, List<Long> parentCodes);

	List<MeddraDictHierarchySearchDto> findNewPtTerm(String socSearchTerm, int firstResult, int fetchSize);

	BigDecimal findNewPtTermRowCount(String socSearchTerm);

	List<String> findSocsWithNewPt();

	List<MeddraDictReverseHierarchySearchDto> findFullReverseHierarchyByLevelAndTerm(String searchColumnPrefix,
			String partitionColumnPrefix, String searchTerm, boolean searchNonCurrentLlt);

}