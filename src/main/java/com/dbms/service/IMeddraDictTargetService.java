package com.dbms.service;

import java.util.List;

import com.dbms.entity.cqt.dtos.MeddraDictHierarchySearchDto;
import com.dbms.entity.cqt.dtos.MeddraDictReverseHierarchySearchDto;

public interface IMeddraDictTargetService {

	List<MeddraDictReverseHierarchySearchDto> findFullReverseHierarchyByLevelAndTerm(String searchColumnPrefix,
			String partitionColumnPrefix, String searchTerm);

	List<MeddraDictReverseHierarchySearchDto> findReverseByCode(String searchColumnTypePrefix,
			String partitionColumnPrefix, Long code);

	List<MeddraDictHierarchySearchDto> findByLevelAndTerm(String searchColumnTypePrefix, String searchTerm);

	MeddraDictHierarchySearchDto findByCode(String searchColumnTypePrefix, Long code);

	MeddraDictReverseHierarchySearchDto findByPtOrLltCode(String searchColumnTypePrefix, Long code);

	List<MeddraDictHierarchySearchDto> findByCodes(String searchColumnTypePrefix, List<Long> codes);

	Long findChldrenCountByParentCode(String searchColumnTypePrefix, String parentCodeColumnPrefix, Long parentCode);

	List<MeddraDictHierarchySearchDto> findChildrenByParentCode(String searchColumnTypePrefix,
			String parentCodeColumnPrefix, Long parentCode);

}