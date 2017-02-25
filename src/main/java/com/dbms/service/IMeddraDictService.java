package com.dbms.service;

import java.util.List;

import com.dbms.entity.cqt.MeddraDict190;
import com.dbms.entity.cqt.dtos.MeddraDictHierarchySearchDto;

public interface IMeddraDictService {

	List<MeddraDictHierarchySearchDto> findByLevelAndTerm(String searchColumnType, String searchTerm);
	
	MeddraDictHierarchySearchDto findByCode(String searchColumnTypePrefix, Long code);
}