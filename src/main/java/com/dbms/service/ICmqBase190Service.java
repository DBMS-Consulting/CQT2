package com.dbms.service;

import java.util.List;

import com.dbms.entity.cqt.CmqBase190;
import com.dbms.service.base.ICqtPersistenceService;

public interface ICmqBase190Service extends ICqtPersistenceService<CmqBase190> {

	List<CmqBase190> findByCriterias(String extension, String drugProgram, String protocol, String product,
			Integer level, String status, String state, String criticalEvent, String group, String termName, Long code);

	List<String> findTypes();

	List<String> findReleaseStatus();

}