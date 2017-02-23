package com.dbms.service;

import java.util.List;

import com.dbms.entity.cqt.dtos.MeddraDictHierarchySearchDto;

public interface IMeddraDictService {

	List<MeddraDictHierarchySearchDto> findByLevelAndTerm(String searchColumnType, String searchTerm);

}