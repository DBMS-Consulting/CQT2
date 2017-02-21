package com.dbms.service;

import java.util.List;

import com.dbms.entity.cqt.MeddraDict190;

public interface IMeddraDictService {

	List<MeddraDict190> findByLevelAndTerm(String searchColumnType, String searchTerm);

}