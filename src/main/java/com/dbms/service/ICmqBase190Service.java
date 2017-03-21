package com.dbms.service;

import java.util.Date;
import java.util.List;
import java.util.Map;

import org.primefaces.model.SortOrder;
import org.primefaces.model.StreamedContent;

import com.dbms.entity.cqt.CmqBase190;
import com.dbms.service.base.ICqtPersistenceService;
import com.dbms.util.exceptions.CqtServiceException;
import com.dbms.view.ListDetailsFormModel;
import com.dbms.view.ListNotesFormModel;

public interface ICmqBase190Service extends ICqtPersistenceService<CmqBase190> {

	List<CmqBase190> findImpactedWithPaginated(int first, int pageSize, String sortField, SortOrder sortOrder,
			Map<String, Object> filters);

	List<CmqBase190> findByCriterias(String extension, String drugProgramCd, String protocolCd, String productCd,
			Integer level, String status, String state, String criticalEvent, String group, String termName, Long code);

	List<String> findTypes();

	List<String> findReleaseStatus();

	Long getNextCodeValue() throws CqtServiceException;

	CmqBase190 findByCode(Long cmqCode);

	List<CmqBase190> findByLevelAndTerm(Integer level, String searchTerm);

	Long findCmqChildCountForParentCmqCode(Long cmqCode);

	List<CmqBase190> findApprovedCmqs();

	List<CmqBase190> findPublishedCmqs();

	List<CmqBase190> findChildCmqsByParentCode(Long code);

	List<CmqBase190> findChildCmqsByCodes(List<Long> codes);

	List<CmqBase190> findParentCmqsByCodes(List<Long> codes);

	Long findCmqCountByCmqNameAndExtension(String extension, String cmqName);

	List<Map<String, Object>> findCmqChildCountForParentCmqCode(List<Long> cmqCodes);

	List<CmqBase190> findCmqsToReactivate();

	List<CmqBase190> findCmqsToRetire();

	/**
	 * 
	 * @return
	 */
	List<CmqBase190> getPublishedListsReportData(Date filterPublishedBetweenFrom, Date filterPublishedBetweenTo);

	StreamedContent generateExcelReport(ListDetailsFormModel details, String dictionaryVersion);

	StreamedContent generateMQReport(ListDetailsFormModel details, ListNotesFormModel notes, String dictionaryVersion);

	Long findImpactedCount();

	List<CmqBase190> findNotImpactedWithPaginated(int first, int pageSize, String sortField, SortOrder sortOrder,
			Map<String, Object> filters);

	Long findNotImpactedCount();

}