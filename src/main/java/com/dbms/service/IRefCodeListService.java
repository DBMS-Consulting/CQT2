package com.dbms.service;

import java.util.List;

import org.primefaces.model.StreamedContent;

import com.dbms.entity.cqt.CmqProductBaseCurrent;
import com.dbms.entity.cqt.RefConfigCodeList;
import com.dbms.service.base.ICqtPersistenceService;
import com.dbms.util.OrderBy;

public interface IRefCodeListService extends ICqtPersistenceService<RefConfigCodeList> {

	public List<RefConfigCodeList> findByConfigType(String codelistConfigType, boolean activeOnly, OrderBy orderBy);
	
	public List<RefConfigCodeList> findByConfigType(String codelistConfigType, OrderBy orderBy);
	
	public List<RefConfigCodeList> findAllByConfigType(String codelistConfigType, OrderBy orderBy);

	public RefConfigCodeList getCurrentMeddraVersion();
	public RefConfigCodeList getTargetMeddraVersion();

	public RefConfigCodeList findByConfigTypeAndInternalCode(String configType, String internalCode);
	public RefConfigCodeList findDefaultByConfigType(String configType);
    public List<RefConfigCodeList> findDefaultsByConfigType(String configType);
	
	public String findCodeByInternalCode(String codelistInternalValue);
	
	/**
	 * Use this function for single interpretation
	 * @param configType
	 * @param internalCode
	 * @return
	 */
	public String findCodeByInternalCode(String configType, String internalCode);
	
	/**
	 * Use this function for batch interpretation like listing in the table
	 * @param configType
	 * @param internalCode
	 * @return
	 */
	public String interpretInternalCodeToValue(String configType, String internalCode);
	public String[] interpretProductCodesToValues(List<CmqProductBaseCurrent> products);
	public String interpretProductCodesToValuesLabel(List<CmqProductBaseCurrent> products);
	public String interpretProductCodesToValuesLabel(String[] productCds);

	public StreamedContent generateReport(String codelistType);

	String convertProductCodesToValuesLabel(List<CmqProductBaseCurrent> products); 
}