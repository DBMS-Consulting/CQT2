package com.dbms.service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.faces.bean.ApplicationScoped;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.persistence.TemporalType;

import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.ClientAnchor;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.Drawing;
import org.apache.poi.ss.usermodel.Picture;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.hibernate.SQLQuery;
import org.hibernate.Session;
import org.hibernate.transform.Transformers;
import org.hibernate.type.StandardBasicTypes;
import org.primefaces.model.DefaultStreamedContent;
import org.primefaces.model.StreamedContent;
import org.primefaces.model.TreeNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dbms.csmq.HierarchyNode;
import com.dbms.entity.cqt.CmqBase190;
import com.dbms.entity.cqt.CmqRelation190;
import com.dbms.entity.cqt.SmqBase190;
import com.dbms.entity.cqt.SmqRelation190;
import com.dbms.entity.cqt.dtos.MeddraDictHierarchySearchDto;
import com.dbms.service.base.CqtPersistenceService;
import com.dbms.util.exceptions.CqtServiceException;
import com.dbms.view.ListDetailsFormModel;

/**
 * @author Jay G.(jayshanchn@hotmail.com)
 * @date Feb 11, 2017 8:00:48 AM
 **/
@ManagedBean(name = "CmqBase190Service")
@ApplicationScoped
public class CmqBase190Service extends CqtPersistenceService<CmqBase190> implements ICmqBase190Service {

	private static final Logger LOG = LoggerFactory.getLogger(CmqBase190Service.class);
	
	@ManagedProperty("#{CmqRelation190Service}")
	private ICmqRelation190Service cmqRelationService;
	
	@ManagedProperty("#{SmqBaseService}")
	private ISmqBaseService smqBaseService;
	
	@ManagedProperty("#{MeddraDictService}")
	private IMeddraDictService meddraDictService;

	private StringBuilder appendClause(StringBuilder sb, boolean first) {
		if (first) {
			sb.append(" where");
		} else {
			sb.append(" and");
		}
		return sb;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.dbms.service.ICmqBase190Service#findByCriterias(java.lang.String,
	 * java.lang.String, java.lang.String, java.lang.String, java.lang.Integer,
	 * java.lang.String, java.lang.String, java.lang.String, java.lang.String,
	 * java.lang.String, java.lang.Long)
	 */
	@Override
	@SuppressWarnings("unchecked")
	public List<CmqBase190> findByCriterias(String extension, String drugProgramCd, String protocolCd, String productCd,
			Integer level, String status, String state, String criticalEvent, String group, String termName,
			Long code) {
		List<CmqBase190> retVal = null;
		StringBuilder sb = new StringBuilder("from CmqBase190 c");
		boolean first = true;
		Map<String, Object> queryParams = new HashMap<>();
		if (StringUtils.isNotEmpty(extension)) {
			sb = appendClause(sb, first);
			sb.append(" c.cmqTypeCd=:cmqTypeCd");
			queryParams.put("cmqTypeCd", extension);
			first = false;
		}
		if (StringUtils.isNoneEmpty(drugProgramCd)) {
			sb = appendClause(sb, first);
			sb.append(" lower(c.cmqProgramCd) like lower(:cmqProgramCd)");
			queryParams.put("cmqProgramCd", drugProgramCd);
			first = false;
		}
		if (StringUtils.isNotEmpty(protocolCd)) {
			sb = appendClause(sb, first);
			sb.append(" lower(c.cmqProtocolCd) like lower(:cmqProtocolCd)");
			queryParams.put("cmqProtocolCd", protocolCd);
			first = false;
		}
		if (StringUtils.isNotEmpty(productCd)) {
			sb = appendClause(sb, first);
			sb.append(" lower(c.cmqProductCd) like lower(:cmqProductCd)");
			queryParams.put("cmqProductCd", productCd);
			first = false;
		}
		if (level != null) {
			sb = appendClause(sb, first);
			sb.append(" c.cmqLevel=:cmqLevel");
			queryParams.put("cmqLevel", level);
			first = false;
		}
		if (StringUtils.isNotEmpty(status)) {
			sb = appendClause(sb, first);
			sb.append(" c.cmqStatus=:cmqStatus");
			queryParams.put("cmqStatus", status);
			first = false;
		}
		if (StringUtils.isNotEmpty(state)) {
			sb = appendClause(sb, first);
			sb.append(" lower(c.cmqState)=lower(:cmqState)");
			queryParams.put("cmqState", state);
			first = false;
		}
		if (StringUtils.isNotEmpty(criticalEvent)) {
			sb = appendClause(sb, first);
			sb.append(" c.cmqCriticalEvent=:cmqCriticalEvent");
			queryParams.put("cmqCriticalEvent", criticalEvent);
			first = false;
		}
		if (StringUtils.isNotEmpty(group)) {
			sb = appendClause(sb, first);
			sb.append(" lower(c.cmqGroup) like lower(:cmqGroup)");
			queryParams.put("cmqGroup", "%" + group + "%");
			first = false;
		}
		if (StringUtils.isNotEmpty(termName)) {
			sb = appendClause(sb, first);
			sb.append(" lower(c.cmqName) like lower(:cmqName)");
			queryParams.put("cmqName", "%" + termName + "%");
			first = false;
		}
		if (code != null) {
			sb = appendClause(sb, first);
			sb.append(" c.cmqCode=:cmqCode");
			queryParams.put("cmqCode", code);
			first = false;
		}
		EntityManager entityManager = this.cqtEntityManagerFactory.getEntityManager();
		try {
			Query query = entityManager.createQuery(sb.toString());

			// now set the parameter values in the query
			Set<String> keySet = queryParams.keySet();
			for (String key : keySet) {
				query.setParameter(key, queryParams.get(key));
			}

			retVal = query.getResultList();
		} catch (Exception e) {
			StringBuilder msg = new StringBuilder();
			msg.append("An error occurred while fetching data from CmqBase190.").append("Query used was ->").append(sb);
			LOG.error(msg.toString(), e);
		} finally {
			this.cqtEntityManagerFactory.closeEntityManager(entityManager);
		}
		return retVal;
	}

	@SuppressWarnings("unchecked")
	public List<CmqBase190> findByLevelAndTerm(Integer level, String searchTerm) {
		List<CmqBase190> retVal = null;
		StringBuilder sb = new StringBuilder();
		sb.append("from CmqBase190 c where c.cmqLevel = :cmqLevel and c.cmqParentCode is null ");
		if (!StringUtils.isBlank(searchTerm)) {
			sb.append("and upper(c.cmqName) like :cmqName");
		}
		EntityManager entityManager = this.cqtEntityManagerFactory.getEntityManager();
		try {
			Query query = entityManager.createQuery(sb.toString());
			query.setParameter("cmqLevel", level);

			if (!StringUtils.isBlank(searchTerm)) {
				query.setParameter("cmqName", searchTerm.toUpperCase());
			}
			retVal = query.getResultList();
		} catch (Exception e) {
			StringBuilder msg = new StringBuilder();
			msg
					.append("An error occurred while fetching types from CmqBase190 on cmqLevel ")
					.append(level)
					.append(" with cmqName like ")
					.append("%" + searchTerm.toUpperCase() + "%")
					.append(" Query used was ->")
					.append(sb.toString());
			LOG.error(msg.toString(), e);
		} finally {
			this.cqtEntityManagerFactory.closeEntityManager(entityManager);
		}
		return retVal;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.dbms.service.ICmqBase190Service#findTypes()
	 */
	@Override
	@SuppressWarnings("unchecked")
	public List<String> findTypes() {
		List<String> retVal = null;
		String query = "select distinct c.type from CmqBase190 c";
		EntityManager entityManager = this.cqtEntityManagerFactory.getEntityManager();
		try {
			retVal = entityManager.createQuery(query).getResultList();
		} catch (Exception e) {
			StringBuilder msg = new StringBuilder();
			msg.append("An error occurred while fetching types from CmqBase190.").append("Query used was ->").append(
					query);
			LOG.error(msg.toString(), e);
		} finally {
			this.cqtEntityManagerFactory.closeEntityManager(entityManager);
		}
		return retVal;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.dbms.service.ICmqBase190Service#findReleaseStatus()
	 */
	@Override
	@SuppressWarnings("unchecked")
	public List<String> findReleaseStatus() {
		List<String> retVal = null;
		String query = "select distinct c.releaseStatus from CmqBase190 c";
		EntityManager entityManager = this.cqtEntityManagerFactory.getEntityManager();
		try {
			retVal = entityManager.createQuery(query).getResultList();
		} catch (Exception e) {
			StringBuilder msg = new StringBuilder();
			msg
					.append("An error occurred while fetching ReleaseStatus from CmqBase190.")
					.append("Query used was ->")
					.append(query);
			LOG.error(msg.toString(), e);
		} finally {
			this.cqtEntityManagerFactory.closeEntityManager(entityManager);
		}
		return retVal;
	}

	public Long getNextCodeValue() throws CqtServiceException {
		Long codeValue = null;
		String query = "SELECT CMQ_CODE_SEQ.nextval as code from dual";
		EntityManager entityManager = this.cqtEntityManagerFactory.getEntityManager();
		try {
			Query nativeQuery = entityManager.createNativeQuery(query);
			BigDecimal retVal = (BigDecimal) nativeQuery.getSingleResult();
			codeValue = retVal.longValue();
		} catch (Exception e) {
			StringBuilder msg = new StringBuilder();
			msg
					.append("An error occurred while fetching next code value form sequence CMQ_CODE_SEQ.")
					.append("Query used was ->")
					.append(query);
			LOG.error(msg.toString(), e);
			throw new CqtServiceException(msg.toString(), e);
		} finally {
			this.cqtEntityManagerFactory.closeEntityManager(entityManager);
		}
		return codeValue;
	}

	public CmqBase190 findByCode(Long cmqCode) {
		CmqBase190 retVal = null;
		String queryString = "from CmqBase190 c where c.cmqCode = :cmqCode";
		EntityManager entityManager = this.cqtEntityManagerFactory.getEntityManager();
		try {
			Query query = entityManager.createQuery(queryString);
			query.setParameter("cmqCode", cmqCode);
			retVal = (CmqBase190) query.getSingleResult();
		} catch (Exception e) {
			StringBuilder msg = new StringBuilder();
			msg
					.append("findByCode failed for CMQ_CODE value'")
					.append(cmqCode)
					.append("' ")
					.append("Query used was ->")
					.append(queryString);
			LOG.error(msg.toString(), e);
		} finally {
			this.cqtEntityManagerFactory.closeEntityManager(entityManager);
		}
		return retVal;
	}
	
	@SuppressWarnings("unchecked")
	public List<Map<String, Object>> findCmqChildCountForParentCmqCode(List<Long> cmqCodes) {
		List<Map<String, Object>>  retVal = null;
		StringBuilder sb = new StringBuilder();
		sb.append("select CMQ_CODE, count(*) as COUNT from CMQ_BASE_CURRENT where CMQ_PARENT_CODE in :cmqCodes group by CMQ_CODE");
		
		EntityManager entityManager = this.cqtEntityManagerFactory.getEntityManager();
		Session session = entityManager.unwrap(Session.class);
		try {
			SQLQuery query = session.createSQLQuery(sb.toString());
			query.addScalar("CMQ_CODE", StandardBasicTypes.LONG);
			query.addScalar("COUNT", StandardBasicTypes.LONG);
			query.setParameterList("cmqCodes", cmqCodes);
			query.setResultTransformer(Transformers.ALIAS_TO_ENTITY_MAP);
			retVal = query.list();
		} catch (Exception e) {
			StringBuilder msg = new StringBuilder();
			msg
					.append("An error occurred while findCmqChildCountForParentCmqCode ")
					.append(cmqCodes)
					.append(" Query used was ->")
					.append(sb.toString());
			LOG.error(msg.toString(), e);
		} finally {
			this.cqtEntityManagerFactory.closeEntityManager(entityManager);
		}
		return retVal;
	}
	
	public Long findCmqChildCountForParentCmqCode(Long cmqCode) {
		Long retVal = null;
		StringBuilder sb = new StringBuilder();
		sb.append("select count(*) from CmqBase190 c where c.cmqParentCode = :cmqCode");
		
		EntityManager entityManager = this.cqtEntityManagerFactory.getEntityManager();
		try {
			Query query = entityManager.createQuery(sb.toString());
			query.setParameter("cmqCode", cmqCode);
			retVal = (Long)query.getSingleResult();
		} catch (Exception e) {
			StringBuilder msg = new StringBuilder();
			msg
					.append("An error occurred while findCmqChildCountForCmqCode ")
					.append(cmqCode)
					.append(" Query used was ->")
					.append(sb.toString());
			LOG.error(msg.toString(), e);
		} finally {
			this.cqtEntityManagerFactory.closeEntityManager(entityManager);
		}
		return retVal;
	}
	
	public Long findCmqCountByCmqNameAndExtension(String extension, String cmqName) {
		Long retVal = null;
		StringBuilder sb = new StringBuilder();
		sb.append("select count(*) from CmqBase190 c where upper(c.cmqName) = :cmqName and upper(cmqTypeCd) = :cmqTypeCd");
		
		EntityManager entityManager = this.cqtEntityManagerFactory.getEntityManager();
		try {
			Query query = entityManager.createQuery(sb.toString());
			query.setParameter("cmqName", cmqName.toUpperCase());
			query.setParameter("cmqTypeCd", extension.toUpperCase());
			retVal = (Long)query.getSingleResult();
		} catch (Exception e) {
			StringBuilder msg = new StringBuilder();
			msg
					.append("An error occurred while findCmqCountByCmqNameAndExtension ")
					.append(" Query used was ->")
					.append(sb.toString());
			LOG.error(msg.toString(), e);
		} finally {
			this.cqtEntityManagerFactory.closeEntityManager(entityManager);
		}
		return retVal;
	}
	
	public List<CmqBase190> findApprovedCmqs() {
		List<CmqBase190> retVal = null;
		String queryString = "from CmqBase190 c where upper(c.cmqState) = upper('Approved') and c.cmqStatus = 'P' ";
		EntityManager entityManager = this.cqtEntityManagerFactory.getEntityManager();
		try {
			Query query = entityManager.createQuery(queryString);
			retVal = query.getResultList();
		} catch (Exception e) {
			StringBuilder msg = new StringBuilder();
			msg
					.append("findApprovedCmqs failed ")
					.append("Query used was ->")
					.append(queryString);
			LOG.error(msg.toString(), e);
		} finally {
			this.cqtEntityManagerFactory.closeEntityManager(entityManager);
		}
		return retVal;
	}
	
	@SuppressWarnings("unchecked")
	public List<CmqBase190> findPublishedCmqs() {
		List<CmqBase190> retVal = null;
		String queryString = "from CmqBase190 c where upper(c.cmqState) = upper('Published') and c.cmqStatus = 'P' ";
		EntityManager entityManager = this.cqtEntityManagerFactory.getEntityManager();
		try {
			Query query = entityManager.createQuery(queryString);
			retVal = query.getResultList();
		} catch (Exception e) {
			StringBuilder msg = new StringBuilder();
			msg
					.append("findApprovedCmqs failed ")
					.append("Query used was ->")
					.append(queryString);
			LOG.error(msg.toString(), e);
		} finally {
			this.cqtEntityManagerFactory.closeEntityManager(entityManager);
		}
		return retVal;
	}
	
	@SuppressWarnings("unchecked")
	public List<CmqBase190> findChildCmqsByParentCode(Long code) {
		List<CmqBase190> retVal = null;
		String queryString = "from CmqBase190 c where c.cmqParentCode = :codeList ";
		EntityManager entityManager = this.cqtEntityManagerFactory.getEntityManager();
		try {
			Query query = entityManager.createQuery(queryString);
			query.setParameter("codeList", code);
			retVal = query.getResultList();
		} catch (Exception e) {
			StringBuilder msg = new StringBuilder();
			msg
					.append("findChildCmqsByCodes failed ")
					.append("Query used was ->")
					.append(queryString);
			LOG.error(msg.toString(), e);
		} finally {
			this.cqtEntityManagerFactory.closeEntityManager(entityManager);
		}
		return retVal;
	}
	
	public List<CmqBase190> findChildCmqsByCodes(List<Long> codes) {
		List<CmqBase190> retVal = null;
		String queryString = "from CmqBase190 c where c.cmqParentCode in :codeList ";
		EntityManager entityManager = this.cqtEntityManagerFactory.getEntityManager();
		try {
			Query query = entityManager.createQuery(queryString);
			query.setParameter("codeList", codes);
			retVal = query.getResultList();
		} catch (Exception e) {
			StringBuilder msg = new StringBuilder();
			msg
					.append("findChildCmqsByCodes failed ")
					.append("Query used was ->")
					.append(queryString);
			LOG.error(msg.toString(), e);
		} finally {
			this.cqtEntityManagerFactory.closeEntityManager(entityManager);
		}
		return retVal;
	}
	
	public List<CmqBase190> findParentCmqsByCodes(List<Long> codes) {
		List<CmqBase190> retVal = null;
		String queryString = "from CmqBase190 c where c.cmqCode in :codeList ";
		EntityManager entityManager = this.cqtEntityManagerFactory.getEntityManager();
		try {
			Query query = entityManager.createQuery(queryString);
			query.setParameter("codeList", codes);
			retVal = query.getResultList();
		} catch (Exception e) {
			StringBuilder msg = new StringBuilder();
			msg
					.append("findParentCmqsByCodes failed ")
					.append("Query used was ->")
					.append(queryString);
			LOG.error(msg.toString(), e);
		} finally {
			this.cqtEntityManagerFactory.closeEntityManager(entityManager);
		}
		return retVal;
	}
	
	public List<CmqBase190> findCmqsToReactivate() {
		List<CmqBase190> retVal = null;
		String queryString = "from CmqBase190 c where upper(c.cmqState) = upper('Published') and c.cmqStatus = 'I' ";
		EntityManager entityManager = this.cqtEntityManagerFactory.getEntityManager();
		try {
			Query query = entityManager.createQuery(queryString);
			retVal = query.getResultList();
		} catch (Exception e) {
			StringBuilder msg = new StringBuilder();
			msg
					.append("findApprovedCmqs failed ")
					.append("Query used was ->")
					.append(queryString);
			LOG.error(msg.toString(), e);
		} finally {
			this.cqtEntityManagerFactory.closeEntityManager(entityManager);
		}
		return retVal;
	}
	
	public List<CmqBase190> findCmqsToRetire() {
		List<CmqBase190> retVal = null;
		String queryString = "from CmqBase190 c where upper(c.cmqState) = upper('Published') and c.cmqStatus = 'A' ";
		EntityManager entityManager = this.cqtEntityManagerFactory.getEntityManager();
		try {
			Query query = entityManager.createQuery(queryString);
			retVal = query.getResultList();
		} catch (Exception e) {
			StringBuilder msg = new StringBuilder();
			msg
					.append("findApprovedCmqs failed ")
					.append("Query used was ->")
					.append(queryString);
			LOG.error(msg.toString(), e);
		} finally {
			this.cqtEntityManagerFactory.closeEntityManager(entityManager);
		}
		return retVal;
	}

	@Override
	public List<CmqBase190> getPublishedListsReportData(Date filterPublishedBetweenFrom,
			Date filterPublishedBetweenTo) {

		StringBuilder queryStrB = new StringBuilder("from CmqBase190 c where c.cmqStatus=:cmqStatus and lower(c.cmqState)=lower(:cmqState)");

		// TODO Auto-generated method stub
		if(filterPublishedBetweenFrom != null)
			queryStrB.append(" and activationDate > :pubBtwFrom");
		if(filterPublishedBetweenTo != null)
			queryStrB.append(" and activationDate < :pubBtwTo");
		
		List<CmqBase190> retVal = null;
		
		EntityManager entityManager = this.cqtEntityManagerFactory.getEntityManager();
		try {
			Query query = entityManager.createQuery(queryStrB.toString());
			query.setParameter("cmqStatus", "A");
			query.setParameter("cmqState", "Published");
			if(filterPublishedBetweenFrom != null)
				query.setParameter("pubBtwFrom", filterPublishedBetweenFrom, TemporalType.TIMESTAMP);
			if(filterPublishedBetweenTo != null)
				query.setParameter("pubBtwTo", filterPublishedBetweenFrom, TemporalType.TIMESTAMP);
			retVal = query.getResultList();
		} catch (Exception e) {
			LOG.error("An error occurred while fetching data from CmqBase190. Query used was->" + queryStrB.toString(), e);
		} finally {
			this.cqtEntityManagerFactory.closeEntityManager(entityManager);
		}
		
		return retVal;
	}
	
	
	/**
	 * Excel Report.
	 */
	@Override
	public StreamedContent generateExcelReport(ListDetailsFormModel details, String dictionaryVersion, TreeNode relationsRoot) {
		XSSFWorkbook workbook = new XSSFWorkbook();
		XSSFSheet worksheet = null;
 
		worksheet = workbook.createSheet("List Report");
		XSSFRow row = null;
		int rowCount = 4;
		
		try {
			insertExporLogoImage(worksheet, workbook);
		} catch (IOException e1) {
			e1.printStackTrace();
		}

		/**
		 * Première ligne - entêtes
		 */
		row = worksheet.createRow(rowCount);
		XSSFCell cell = row.createCell(0);
		
		//Term name 
		cell.setCellValue("MedDRA Dictionary Version: " + dictionaryVersion);
		
		rowCount++;
		row = worksheet.createRow(rowCount);
		cell = row.createCell(0);
		cell.setCellValue("Status: " + details.getStatus());
		
		rowCount++;
		row = worksheet.createRow(rowCount);
		cell = row.createCell(0);
		cell.setCellValue("Term Name: " + details.getName());
		
		rowCount++;
		row = worksheet.createRow(rowCount);
		cell = row.createCell(0);
		cell.setCellValue("Code: " + details.getCode());
		
		rowCount++;
		row = worksheet.createRow(rowCount);
		cell = row.createCell(0);
		cell.setCellValue("Extension: " + details.getExtension());
		
		rowCount++;
		row = worksheet.createRow(rowCount);
		cell = row.createCell(0);
		cell.setCellValue("Report Date/Time: " + new Date().toString());
		cell = row.createCell(1);
	 
		
		rowCount += 2;
		row = worksheet.createRow(rowCount);
		cell = row.createCell(0);
		cell.setCellValue("Term");
		cell = row.createCell(1);
		cell.setCellValue("Code");
		cell = row.createCell(2);
		cell.setCellValue("Level");
		cell = row.createCell(3);
		cell.setCellValue("Category");
		cell = row.createCell(4);
		cell.setCellValue("Weight");
		cell = row.createCell(5);
		cell.setCellValue("Scope");
		rowCount++;

		// Retrieval of relations - Loop
		List<CmqRelation190> relations = cmqRelationService.findByCmqCode(details.getCode());
		System.out.println("\n\n ****************** relations SIZE for export " + relations.size()); 
		Long code = null;
		MeddraDictHierarchySearchDto searchDto = null;
		String level = "";
 		
		if (relations != null) {
			for (CmqRelation190 relation : relations) {
				//System.out.println("\n\n ****************** relation PT CODE : " + relation.getPtCode());
				
				if (relation.getPtCode() != null)
					code = relation.getPtCode();
				
				else if (relation.getHlgtCode() != null) {
					code = relation.getHlgtCode();
					searchDto = this.meddraDictService.findByCode("HLGT_", code);
					level = "HLGT";
				}
				else if (relation.getHltCode() != null) {
					code = relation.getHltCode();
					searchDto = this.meddraDictService.findByCode("HLT_", code);
					level = "HLT";
				}
				else if (relation.getSocCode() != null) {
					code = relation.getSocCode();
					searchDto = this.meddraDictService.findByCode("SOC_", code);
					level = "SOC";
				}
			
				//SmqRelation190 child = null;
				SmqRelation190 child = null;
				if(code != null) 
 					child = smqBaseService.findSmqRelationBySmqAndPtCode(relation.getSmqCode(), code.intValue());
				 
				//if (child != null) {
				row = worksheet.createRow(rowCount);
				// Cell 0
				cell = row.createCell(0);
				if (child != null)
					cell.setCellValue(child.getPtName());
				if (searchDto != null)
					cell.setCellValue(searchDto.getTerm());

				// Cell 1
				cell = row.createCell(1);
				if (child != null)
					cell.setCellValue(child.getPtCode());
				if (searchDto != null)
					cell.setCellValue(searchDto.getCode());

				// Cell 2
				cell = row.createCell(2);
				if (child != null)
					cell.setCellValue(child.getSmqLevel());
				if (searchDto != null)
					cell.setCellValue(level);

				// Cell 3
				cell = row.createCell(3);
				cell.setCellValue(relation.getTermCategory() != null ? relation
						.getTermCategory() : "");

				// Cell 4
				cell = row.createCell(4);
				cell.setCellValue(relation.getTermWeight() != null ? relation
						.getTermWeight() : 0);

				// Cell 5
				cell = row.createCell(5);
				cell.setCellValue(relation.getTermScope() != null ? relation
						.getTermScope() : "");

				rowCount++;
				//}
			}
		}

		worksheet.autoSizeColumn(0);
		worksheet.autoSizeColumn(1);
		worksheet.autoSizeColumn(2);
		worksheet.autoSizeColumn(3);
		worksheet.autoSizeColumn(4);
		worksheet.autoSizeColumn(5);
 
		StreamedContent content = null;
		try {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			workbook.write(baos);
			byte[] xls = baos.toByteArray();
			ByteArrayInputStream bais = new ByteArrayInputStream(xls);
			content = new DefaultStreamedContent(
					bais,
					"application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
					"Report_" + details.getName() + ".xlsx");
		} catch (IOException e) {
			e.printStackTrace();
		}

		return content;
	}
	
	private void insertExporLogoImage(XSSFSheet sheet, XSSFWorkbook wb) throws IOException {
		FacesContext fc = FacesContext.getCurrentInstance();
		ExternalContext ec = fc.getExternalContext();
		final FileInputStream stream = new FileInputStream(ec.getRealPath("/image/logo.png"));
		final CreationHelper helper = wb.getCreationHelper();
		final Drawing drawing = sheet.createDrawingPatriarch();

		final ClientAnchor anchor = helper.createClientAnchor();
		anchor.setAnchorType(ClientAnchor.MOVE_AND_RESIZE);

		final int pictureIndex = wb.addPicture(stream,
				Workbook.PICTURE_TYPE_PNG);

		anchor.setCol1(0);
		anchor.setRow1(0); // same row is okay
  		final Picture pict = drawing.createPicture(anchor, pictureIndex);
		pict.resize();
	}

	public ICmqRelation190Service getCmqRelationService() {
		return cmqRelationService;
	}

	public void setCmqRelationService(ICmqRelation190Service cmqRelationService) {
		this.cmqRelationService = cmqRelationService;
	}

	public ISmqBaseService getSmqBaseService() {
		return smqBaseService;
	}

	public void setSmqBaseService(ISmqBaseService smqBaseService) {
		this.smqBaseService = smqBaseService;
	}

	public IMeddraDictService getMeddraDictService() {
		return meddraDictService;
	}

	public void setMeddraDictService(IMeddraDictService meddraDictService) {
		this.meddraDictService = meddraDictService;
	}
}
