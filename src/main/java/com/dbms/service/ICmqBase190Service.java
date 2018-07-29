package com.dbms.service;

import java.util.Date;
import java.util.List;
import java.util.Map;

import org.primefaces.model.SortOrder;
import org.primefaces.model.StreamedContent;
import org.primefaces.model.TreeNode;

import com.dbms.entity.IEntity;
import com.dbms.entity.cqt.CmqBase190;
import com.dbms.service.base.ICqtPersistenceService;
import com.dbms.util.exceptions.CqtServiceException;
import com.dbms.view.ListDetailsFormVM;
import com.dbms.view.ListNotesFormVM;

public interface ICmqBase190Service extends ICqtPersistenceService<CmqBase190> {

	List<CmqBase190> findByCriterias(String extension, String drugProgramCd, String protocolCd, String[] productCds,
			Integer level, String status, String state, String criticalEvent, String group, String termName, String code, String[] designees);

	List<String> findTypes();

	List<String> findReleaseStatus();

	Long getNextCodeValue() throws CqtServiceException;

	CmqBase190 findByCode(Long cmqCode);
	List<CmqBase190> findByCodes(List<Long> cmqCodes);

	List<CmqBase190> findByLevelAndTerm(Integer level, String searchTerm);

	Long findCmqChildCountForParentCmqCode(Long cmqCode);
    List<Map<String, Object>> findCmqChildCountForParentCmqCodes(List<Long> cmqCodes);

	List<CmqBase190> findApprovedCmqs();

	List<CmqBase190> findPublishedCmqs();

	List<CmqBase190> findChildCmqsByParentCode(Long code);
	List<CmqBase190> findChildCmqsByCodes(List<Long> codes);

	List<CmqBase190> findParentCmqsByCodes(List<Long> codes);

	Long findCmqCountByCmqNameAndExtension(String extension, String cmqName);

	List<CmqBase190> findCmqsToReactivate();

	List<CmqBase190> findCmqsToRetire();

	/**
	 * 
	 * @return
	 */
	List<CmqBase190> getPublishedListsReportData(Date filterPublishedBetweenFrom, Date filterPublishedBetweenTo);

	StreamedContent generateExcelReport(ListDetailsFormVM details, String dictionaryVersion);

	StreamedContent generateMQReport(ListDetailsFormVM details, ListNotesFormVM notes, String dictionaryVersion, TreeNode relationsRoot, boolean filterLlts);

	Boolean checkIfCmqNamqExists(String cmqName);

    /**
     * This function will check CMQ_BASE_CURRENT_AUDIT.CMQ_STATE_OLD for a given CMQ_CODE
     * @param cmqCode
     * @return 
     */
    boolean checkIfApprovedOnce(Long cmqCode);

	StreamedContent generateExcel(List<CmqBase190> datas, String module, String user);
	
	CmqBase190 findByName(String cmqName);
	
	Map<Long,String> findAllCmqsCodeAndName();

	List<? extends IEntity> findChildCmqsByParentCode(Long cmqCode, String dictionaryVersion);

	List<Map<String, Object>> findCmqChildCountForParentCmqCodes(List<Long> cmqCodes, String dictionaryVersion);
}