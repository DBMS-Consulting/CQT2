package com.dbms.service;

import java.util.List;
import java.util.Map;

import org.primefaces.model.SortOrder;

import com.dbms.entity.cqt.CmqBaseTarget;
import com.dbms.service.base.ICqtPersistenceService;

public interface ICmqBaseTargetService  extends ICqtPersistenceService<CmqBaseTarget>{

	List<CmqBaseTarget> findImpactedWithPaginated(int first, int pageSize, String sortField, SortOrder sortOrder,
			Map<String, Object> filters);

	Long findImpactedCount();

	List<CmqBaseTarget> findNotImpactedWithPaginated(int first, int pageSize, String sortField, SortOrder sortOrder,
			Map<String, Object> filters);

	Long findNotImpactedCount();

}