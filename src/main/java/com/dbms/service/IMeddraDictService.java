package com.dbms.service;

import java.util.List;

import com.dbms.entity.cqt.dtos.MeddraDictHierarchySearchDto;

public interface IMeddraDictService {

	List<MeddraDictHierarchySearchDto> findByLevelAndTerm(String searchColumnType, String searchTerm);
	
	List<MeddraDictHierarchySearchDto> findByCodes(String searchColumnTypePrefix, List<Long> codes);
	
	MeddraDictHierarchySearchDto findByCode(String searchColumnTypePrefix, Long code);
	
	Long findChldrenCountByParentCode(String searchColumnTypePrefix,
			String parentCodeColumnPrefix, Long parentCode);
	
	List<MeddraDictHierarchySearchDto> findChildrenByParentCode(String searchColumnTypePrefix,
			String parentCodeColumnPrefix, Long parentCode);
}