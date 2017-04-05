package com.dbms.service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
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
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.ListUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.usermodel.ClientAnchor;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.Drawing;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Picture;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.hibernate.SQLQuery;
import org.hibernate.Session;
import org.hibernate.transform.Transformers;
import org.hibernate.type.StandardBasicTypes;
import org.primefaces.model.DefaultStreamedContent;
import org.primefaces.model.DefaultTreeNode;
import org.primefaces.model.StreamedContent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dbms.csmq.HierarchyNode;
import com.dbms.entity.cqt.CmqBase190;
import com.dbms.entity.cqt.CmqRelation190;
import com.dbms.entity.cqt.SmqBase190;
import com.dbms.entity.cqt.SmqRelation190;
import com.dbms.entity.cqt.dtos.MeddraDictHierarchySearchDto;
import com.dbms.entity.cqt.dtos.MeddraDictReverseHierarchySearchDto;
import com.dbms.service.base.CqtPersistenceService;
import com.dbms.util.exceptions.CqtServiceException;
import com.dbms.view.ListDetailsFormModel;
import com.dbms.view.ListNotesFormModel;

/**
 * @author Jay G.(jayshanchn@hotmail.com)
 * @date Feb 11, 2017 8:00:48 AM
 **/
@ManagedBean(name = "CmqBase190Service")
@ApplicationScoped
public class CmqBase190Service extends CqtPersistenceService<CmqBase190>
		implements ICmqBase190Service {

	private static final Logger LOG = LoggerFactory
			.getLogger(CmqBase190Service.class);

	@ManagedProperty("#{CmqRelation190Service}")
	private ICmqRelation190Service cmqRelationService;

	@ManagedProperty("#{SmqBaseService}")
	private ISmqBaseService smqBaseService;

	@ManagedProperty("#{MeddraDictService}")
	private IMeddraDictService meddraDictService;
	
	@ManagedProperty("#{RefCodeListService}")
	private IRefCodeListService refCodeListService;
	
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
	public List<CmqBase190> findByCriterias(String extension,
			String drugProgramCd, String protocolCd, String[] productCds,
			Integer level, String status, String state, String criticalEvent,
			String group, String termName, Long code) {
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
		if (productCds != null && productCds.length > 0) {
			sb = appendClause(sb, first);
			sb.append(" c.cmqId in (SELECT p.cmqId FROM CmqProduct p WHERE p.cmqProductCd in :cmqProductCds)");
			queryParams.put("cmqProductCds", Arrays.asList(productCds));
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
		EntityManager entityManager = this.cqtEntityManagerFactory
				.getEntityManager();
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
			msg.append("An error occurred while fetching data from CmqBase190.")
					.append("Query used was ->").append(sb);
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
		EntityManager entityManager = this.cqtEntityManagerFactory
				.getEntityManager();
		try {
			Query query = entityManager.createQuery(sb.toString());
			query.setParameter("cmqLevel", level);

			if (!StringUtils.isBlank(searchTerm)) {
				query.setParameter("cmqName", searchTerm.toUpperCase());
			}
			retVal = query.getResultList();
		} catch (Exception e) {
			StringBuilder msg = new StringBuilder();
			msg.append(
					"An error occurred while fetching types from CmqBase190 on cmqLevel ")
					.append(level).append(" with cmqName like ")
					.append("%" + searchTerm.toUpperCase() + "%")
					.append(" Query used was ->").append(sb.toString());
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
		EntityManager entityManager = this.cqtEntityManagerFactory
				.getEntityManager();
		try {
			retVal = entityManager.createQuery(query).getResultList();
		} catch (Exception e) {
			StringBuilder msg = new StringBuilder();
			msg.append(
					"An error occurred while fetching types from CmqBase190.")
					.append("Query used was ->").append(query);
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
		EntityManager entityManager = this.cqtEntityManagerFactory
				.getEntityManager();
		try {
			retVal = entityManager.createQuery(query).getResultList();
		} catch (Exception e) {
			StringBuilder msg = new StringBuilder();
			msg.append(
					"An error occurred while fetching ReleaseStatus from CmqBase190.")
					.append("Query used was ->").append(query);
			LOG.error(msg.toString(), e);
		} finally {
			this.cqtEntityManagerFactory.closeEntityManager(entityManager);
		}
		return retVal;
	}

	public Long getNextCodeValue() throws CqtServiceException {
		Long codeValue = null;
		String query = "SELECT CMQ_CODE_SEQ.nextval as code from dual";
		EntityManager entityManager = this.cqtEntityManagerFactory
				.getEntityManager();
		try {
			Query nativeQuery = entityManager.createNativeQuery(query);
			BigDecimal retVal = (BigDecimal) nativeQuery.getSingleResult();
			codeValue = retVal.longValue();
		} catch (Exception e) {
			StringBuilder msg = new StringBuilder();
			msg.append(
					"An error occurred while fetching next code value form sequence CMQ_CODE_SEQ.")
					.append("Query used was ->").append(query);
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
		EntityManager entityManager = this.cqtEntityManagerFactory
				.getEntityManager();
		try {
			Query query = entityManager.createQuery(queryString);
			query.setParameter("cmqCode", cmqCode);
			retVal = (CmqBase190) query.getSingleResult();
		} catch (Exception e) {
			StringBuilder msg = new StringBuilder();
			msg.append("findByCode failed for CMQ_CODE value'").append(cmqCode)
					.append("' ").append("Query used was ->")
					.append(queryString);
			LOG.error(msg.toString(), e);
		} finally {
			this.cqtEntityManagerFactory.closeEntityManager(entityManager);
		}
		return retVal;
	}

	@SuppressWarnings("unchecked")
	public List<Map<String, Object>> findCmqChildCountForParentCmqCode(
			List<Long> cmqCodes) {
		List<Map<String, Object>> retVal = null;
		StringBuilder sb = new StringBuilder();
		sb.append("select CMQ_CODE, count(*) as COUNT from CMQ_BASE_CURRENT where CMQ_PARENT_CODE in :cmqCodes group by CMQ_CODE");

		EntityManager entityManager = this.cqtEntityManagerFactory
				.getEntityManager();
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
			msg.append(
					"An error occurred while findCmqChildCountForParentCmqCode ")
					.append(cmqCodes).append(" Query used was ->")
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

		EntityManager entityManager = this.cqtEntityManagerFactory
				.getEntityManager();
		try {
			Query query = entityManager.createQuery(sb.toString());
			query.setParameter("cmqCode", cmqCode);
			retVal = (Long) query.getSingleResult();
		} catch (Exception e) {
			StringBuilder msg = new StringBuilder();
			msg.append("An error occurred while findCmqChildCountForCmqCode ")
					.append(cmqCode).append(" Query used was ->")
					.append(sb.toString());
			LOG.error(msg.toString(), e);
		} finally {
			this.cqtEntityManagerFactory.closeEntityManager(entityManager);
		}
		return retVal;
	}

	public Long findCmqCountByCmqNameAndExtension(String extension,
			String cmqName) {
		Long retVal = null;
		StringBuilder sb = new StringBuilder();
		sb.append("select count(*) from CmqBase190 c where upper(c.cmqName) = :cmqName and upper(cmqTypeCd) = :cmqTypeCd");

		EntityManager entityManager = this.cqtEntityManagerFactory
				.getEntityManager();
		try {
			Query query = entityManager.createQuery(sb.toString());
			query.setParameter("cmqName", cmqName.toUpperCase());
			query.setParameter("cmqTypeCd", extension.toUpperCase());
			retVal = (Long) query.getSingleResult();
		} catch (Exception e) {
			StringBuilder msg = new StringBuilder();
			msg.append(
					"An error occurred while findCmqCountByCmqNameAndExtension ")
					.append(" Query used was ->").append(sb.toString());
			LOG.error(msg.toString(), e);
		} finally {
			this.cqtEntityManagerFactory.closeEntityManager(entityManager);
		}
		return retVal;
	}

	public List<CmqBase190> findApprovedCmqs() {
		List<CmqBase190> retVal = null;
		String queryString = "from CmqBase190 c where upper(c.cmqState) = upper('Approved') and c.cmqStatus = 'P' ";
		EntityManager entityManager = this.cqtEntityManagerFactory
				.getEntityManager();
		try {
			Query query = entityManager.createQuery(queryString);
			retVal = query.getResultList();
		} catch (Exception e) {
			StringBuilder msg = new StringBuilder();
			msg.append("findApprovedCmqs failed ").append("Query used was ->")
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
		EntityManager entityManager = this.cqtEntityManagerFactory
				.getEntityManager();
		try {
			Query query = entityManager.createQuery(queryString);
			retVal = query.getResultList();
		} catch (Exception e) {
			StringBuilder msg = new StringBuilder();
			msg.append("findApprovedCmqs failed ").append("Query used was ->")
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
		EntityManager entityManager = this.cqtEntityManagerFactory
				.getEntityManager();
		try {
			Query query = entityManager.createQuery(queryString);
			query.setParameter("codeList", code);
			retVal = query.getResultList();
		} catch (Exception e) {
			StringBuilder msg = new StringBuilder();
			msg.append("findChildCmqsByCodes failed ")
					.append("Query used was ->").append(queryString);
			LOG.error(msg.toString(), e);
		} finally {
			this.cqtEntityManagerFactory.closeEntityManager(entityManager);
		}
		return retVal;
	}

	public List<CmqBase190> findChildCmqsByCodes(List<Long> codes) {
		List<CmqBase190> retVal = null;
		String queryString = "from CmqBase190 c where c.cmqParentCode in :codeList ";
		EntityManager entityManager = this.cqtEntityManagerFactory
				.getEntityManager();
		try {
			Query query = entityManager.createQuery(queryString);
			query.setParameter("codeList", codes);
			retVal = query.getResultList();
		} catch (Exception e) {
			StringBuilder msg = new StringBuilder();
			msg.append("findChildCmqsByCodes failed ")
					.append("Query used was ->").append(queryString);
			LOG.error(msg.toString(), e);
		} finally {
			this.cqtEntityManagerFactory.closeEntityManager(entityManager);
		}
		return retVal;
	}

	public List<CmqBase190> findParentCmqsByCodes(List<Long> codes) {
		List<CmqBase190> retVal = null;
		String queryString = "from CmqBase190 c where c.cmqCode in :codeList ";
		EntityManager entityManager = this.cqtEntityManagerFactory
				.getEntityManager();
		try {
			Query query = entityManager.createQuery(queryString);
			query.setParameter("codeList", codes);
			retVal = query.getResultList();
		} catch (Exception e) {
			StringBuilder msg = new StringBuilder();
			msg.append("findParentCmqsByCodes failed ")
					.append("Query used was ->").append(queryString);
			LOG.error(msg.toString(), e);
		} finally {
			this.cqtEntityManagerFactory.closeEntityManager(entityManager);
		}
		return retVal;
	}

	public List<CmqBase190> findCmqsToReactivate() {
		List<CmqBase190> retVal = null;
		String queryString = "from CmqBase190 c where upper(c.cmqState) = upper('Published') and c.cmqStatus = 'I' ";
		EntityManager entityManager = this.cqtEntityManagerFactory
				.getEntityManager();
		try {
			Query query = entityManager.createQuery(queryString);
			retVal = query.getResultList();
		} catch (Exception e) {
			StringBuilder msg = new StringBuilder();
			msg.append("findApprovedCmqs failed ").append("Query used was ->")
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
		EntityManager entityManager = this.cqtEntityManagerFactory
				.getEntityManager();
		try {
			Query query = entityManager.createQuery(queryString);
			retVal = query.getResultList();
		} catch (Exception e) {
			StringBuilder msg = new StringBuilder();
			msg.append("findApprovedCmqs failed ").append("Query used was ->")
					.append(queryString);
			LOG.error(msg.toString(), e);
		} finally {
			this.cqtEntityManagerFactory.closeEntityManager(entityManager);
		}
		return retVal;
	}

	@Override
	public List<CmqBase190> getPublishedListsReportData(
			Date filterPublishedBetweenFrom, Date filterPublishedBetweenTo) {

		StringBuilder queryStrB = new StringBuilder(
				"from CmqBase190 c where c.cmqStatus=:cmqStatus and lower(c.cmqState)=lower(:cmqState)");

		// TODO Auto-generated method stub
		if (filterPublishedBetweenFrom != null)
			queryStrB.append(" and activationDate >= :pubBtwFrom");
		if (filterPublishedBetweenTo != null)
			queryStrB.append(" and activationDate <= :pubBtwTo");

		List<CmqBase190> retVal = null;

		EntityManager entityManager = this.cqtEntityManagerFactory
				.getEntityManager();
		try {
			Query query = entityManager.createQuery(queryStrB.toString());
			query.setParameter("cmqStatus", "A");
			query.setParameter("cmqState", "Published");
			if (filterPublishedBetweenFrom != null)
				query.setParameter("pubBtwFrom", filterPublishedBetweenFrom,
						TemporalType.TIMESTAMP);
			if (filterPublishedBetweenTo != null)
				query.setParameter("pubBtwTo", filterPublishedBetweenTo,
						TemporalType.TIMESTAMP);
			retVal = query.getResultList();
		} catch (Exception e) {
			LOG.error(
					"An error occurred while fetching data from CmqBase190. Query used was->"
							+ queryStrB.toString(), e);
		} finally {
			this.cqtEntityManagerFactory.closeEntityManager(entityManager);
		}

		return retVal;
	}

	/**
	 * Excel Report.
	 */
	@Override
	public StreamedContent generateExcelReport(ListDetailsFormModel details,
			String dictionaryVersion) {
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

		row = worksheet.createRow(rowCount);
		cell = row.createCell(0);
		cell.setCellValue(details.getName());
		setCellStyleTitre(workbook, cell);

		// Term name
		rowCount += 2;
		row = worksheet.createRow(rowCount);
		cell = row.createCell(0);
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
		setCellStyleColumn(workbook, cell);
		cell = row.createCell(1);
		cell.setCellValue("Code");
		setCellStyleColumn(workbook, cell);
		cell = row.createCell(2);
		cell.setCellValue("Level");
		setCellStyleColumn(workbook, cell);
		cell = row.createCell(3);
		cell.setCellValue("Category");
		setCellStyleColumn(workbook, cell);
		cell = row.createCell(4);
		cell.setCellValue("Weight");
		setCellStyleColumn(workbook, cell);
		cell = row.createCell(5);
		cell.setCellValue("Scope");
		setCellStyleColumn(workbook, cell);
		rowCount++;

		// Retrieval of relations - Loop
		List<CmqRelation190> relations = cmqRelationService
				.findByCmqCode(details.getCode());
		
		//Long code = null;
 		// MeddraDictReverseHierarchySearchDto search = null;
		String level = "", term = "", codeTerm = "";

		if (relations != null) {
			for (CmqRelation190 relation : relations) {
				if (relation.getSmqCode() != null) {
					if (relation.getPtCode() != null) {
						SmqRelation190 childRelation = this.smqBaseService
								.findSmqRelationBySmqAndPtCode(relation
										.getSmqCode(), relation.getPtCode()
										.intValue());
						if (childRelation.getSmqLevel() == 1) {
							level = "SMQ1";
						} else if (childRelation.getSmqLevel() == 2) {
							level = "SMQ2";
						} else if (childRelation.getSmqLevel() == 3) {
							level = "SMQ3";
						} else if ((childRelation.getSmqLevel() == 4)
								|| (childRelation.getSmqLevel() == 0)
								|| (childRelation.getSmqLevel() == 5)) {
							level = "PT";
						}
						codeTerm = childRelation.getPtCode() != null ? childRelation.getPtCode() + "" : "";
						term = childRelation.getPtName();
					} else {
						SmqBase190 smqBase = this.smqBaseService
								.findByCode(relation.getSmqCode());
						if (null != smqBase) {
							term = smqBase.getSmqName();
							codeTerm = smqBase.getSmqCode() != null ? smqBase.getSmqCode() + "" : "";	
							if (smqBase.getSmqLevel() == 1) {
								level = "SMQ1";
							} else if (smqBase.getSmqLevel() == 2) {
								level = "SMQ2";
							} else if (smqBase.getSmqLevel() == 3) {
								level = "SMQ3";
							} else if (smqBase.getSmqLevel() == 4) {
								level = "SMQ4";
							} else if (smqBase.getSmqLevel() == 5) {
								level = "SMQ5";
							}
						}
					}
				}

				else if (relation.getPtCode() != null) {
					level = "PT";
					MeddraDictReverseHierarchySearchDto search = this.meddraDictService
							.findByPtOrLltCode("PT_", relation.getPtCode());
					if (search != null) {
						term = search.getPtTerm();
						codeTerm = search.getLltCode();			
					}
				} else if (relation.getHlgtCode() != null) {
					MeddraDictHierarchySearchDto searchDto = this.meddraDictService
							.findByCode("HLGT_", relation.getHlgtCode());
					if (searchDto != null) {
						term = searchDto.getTerm();
						codeTerm = searchDto.getCode();				
					}
					level = "HLGT";
				} else if (relation.getHltCode() != null) {					
					MeddraDictHierarchySearchDto searchDto = this.meddraDictService
							.findByCode("HLT_", relation.getHltCode());
					if (searchDto != null) {
						term = searchDto.getTerm();
						codeTerm = searchDto.getCode();				
					}
					level = "HLT";
				} else if (relation.getSocCode() != null) {
					MeddraDictHierarchySearchDto searchDto = this.meddraDictService
							.findByCode("SOC_", relation.getSocCode());
					if (searchDto != null) {
						term = searchDto.getTerm();
						codeTerm = searchDto.getCode();				
					}
					level = "SOC";
				} else if (relation.getLltCode() != null) {
					MeddraDictReverseHierarchySearchDto searchDto = this.meddraDictService
							.findByPtOrLltCode("LLT_", relation.getLltCode());
					if (searchDto != null) {
						term = searchDto.getLltTerm();
						codeTerm = searchDto.getLltCode();				
					}
					level = "LLT";
				}
				row = worksheet.createRow(rowCount);
				
				buildCells(level, codeTerm, term, relation, cell, row); 

				rowCount++;
			}
		}
		List<CmqBase190> childCmqs = findChildCmqsByParentCode(details.getCode());
		if((null != childCmqs) && (childCmqs.size() > 0)) {
			for (CmqBase190 childCmq : childCmqs) {
				level = "PRO";
				term = childCmq.getCmqName();
				codeTerm = childCmq.getCmqCode() != null ? childCmq.getCmqCode() + "" : "";
				
				row = worksheet.createRow(rowCount);
				buildCells(level, codeTerm, term, cell, row); 

				rowCount++;
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

	private void buildCells(String level, String codeTerm, String term, XSSFCell cell, XSSFRow row) {
		// Cell 0
		cell = row.createCell(0);
		cell.setCellValue(term);

		// Cell 1
		cell = row.createCell(1);
		cell.setCellValue(codeTerm);

		// Cell 2
		cell = row.createCell(2);
		cell.setCellValue(level);
	}
	
	private void buildChildCells(String level, String codeTerm, String term, XSSFCell cell, XSSFRow row, String dots) {
		// Cell 0
		cell = row.createCell(0);
		cell.setCellValue(dots + term);

		// Cell 1
		cell = row.createCell(1);
		cell.setCellValue(codeTerm);

		// Cell 2
		cell = row.createCell(2);
		cell.setCellValue(level);
	}


	private void buildCells(String level, String codeTerm, String term, CmqRelation190 relation, XSSFCell cell, XSSFRow row) {
		// Cell 0
		cell = row.createCell(0);
		cell.setCellValue(term);
	
		// Cell 1
		cell = row.createCell(1);
		cell.setCellValue(codeTerm);

		// Cell 2
		cell = row.createCell(2);
		cell.setCellValue(level);

		// Cell 3
		cell = row.createCell(3);
		cell.setCellValue(relation.getTermCategory() != null ? relation
				.getTermCategory() : "");

		// Cell 4
		cell = row.createCell(4);
		cell.setCellValue(relation.getTermWeight() != null ? relation
				.getTermWeight().toString() : "");

		// Cell 5
		cell = row.createCell(5);
		cell.setCellValue(relation.getTermScope() != null ? relation
				.getTermScope() : "");
		
	}

	/**
	 * MQ Report.
	 */
	@Override
	public StreamedContent generateMQReport(ListDetailsFormModel details, ListNotesFormModel notes, String dictionaryVersion) {
		XSSFWorkbook workbook = new XSSFWorkbook();
		XSSFSheet worksheet = null;

		worksheet = workbook.createSheet("MQ Report");
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

		row = worksheet.createRow(rowCount);
		cell = row.createCell(0);
		cell.setCellValue(details.getName());
		setCellStyleTitre(workbook, cell);

		// Term name
		rowCount += 2;
		row = worksheet.createRow(rowCount);
		cell = row.createCell(0);
		cell.setCellValue("MedDRA Dictionary Version: " + dictionaryVersion);

		rowCount++;
		row = worksheet.createRow(rowCount);
		cell = row.createCell(0);
		cell.setCellValue("Term: " + details.getName());
		rowCount++;
		row = worksheet.createRow(rowCount);
		cell = row.createCell(0);
		cell.setCellValue("Drug Program: " + refCodeListService.findCodeByInternalCode("PROGRAM", details.getDrugProgram()));
		rowCount++;
		row = worksheet.createRow(rowCount);
		cell = row.createCell(0);
		cell.setCellValue("Protocol: " + refCodeListService.findCodeByInternalCode("PROTOCOL", details.getProtocol()));
		rowCount++;
		row = worksheet.createRow(rowCount);
		cell = row.createCell(0);
		cell.setCellValue("Product List: " + refCodeListService.findCodeByInternalCode("PRODUCT", details.getProduct()));
		rowCount++;
		row = worksheet.createRow(rowCount);
		cell = row.createCell(0);
		cell.setCellValue("Designee: " + details.getDesignee());
		rowCount++;
		row = worksheet.createRow(rowCount);
		cell = row.createCell(0);
		cell.setCellValue("Level: " + details.getLevel());
		rowCount++;
		row = worksheet.createRow(rowCount);
		cell = row.createCell(0);
		cell.setCellValue("Code: " + details.getCode());
		rowCount++;
		row = worksheet.createRow(rowCount);
		cell = row.createCell(0);
		cell.setCellValue("Algorithm: " + details.getAlgorithm());
		rowCount++;
		row = worksheet.createRow(rowCount);
		cell = row.createCell(0);
		cell.setCellValue("Level Extension: " + details.getExtension());
		rowCount++;
		row = worksheet.createRow(rowCount);
		cell = row.createCell(0);
		cell.setCellValue("Group: " + details.getGroup());
		rowCount++;
		row = worksheet.createRow(rowCount);
		cell = row.createCell(0);
		cell.setCellValue("Status: " + details.getStatus());
		rowCount++;
		row = worksheet.createRow(rowCount);
		cell = row.createCell(0);
		cell.setCellValue("State: " + details.getState());
		rowCount++;
		row = worksheet.createRow(rowCount);
		cell = row.createCell(0);
		cell.setCellValue("Initial Creation By: " + details.getCreatedBy());
		rowCount++;
		row = worksheet.createRow(rowCount);
		cell = row.createCell(0);
		cell.setCellValue("Initial Creation Date: " + details.getCreationDate());
		rowCount++;
		row = worksheet.createRow(rowCount);
		cell = row.createCell(0);
		cell.setCellValue("Last Modified By: " + details.getLastModifiedBy());
		rowCount++;
		row = worksheet.createRow(rowCount);
		cell = row.createCell(0);
		cell.setCellValue("Last Modification Date: "
				+ details.getLastModifiedDate());

		rowCount++;
		row = worksheet.createRow(rowCount);
		cell = row.createCell(0);
		cell.setCellValue("Report Date/Time: " + new Date().toString());

		rowCount += 2;
		row = worksheet.createRow(rowCount);
		cell = row.createCell(0);
		cell.setCellValue("Description: ");
		setCellStyleTitre(workbook, cell);
		rowCount++;
		row = worksheet.createRow(rowCount);
		cell = row.createCell(0);
		cell.setCellValue(notes.getDescription());

		rowCount += 2;
		row = worksheet.createRow(rowCount);
		cell = row.createCell(0);
		cell.setCellValue("Source:");
		setCellStyleTitre(workbook, cell);
		rowCount++;
		row = worksheet.createRow(rowCount);
		cell = row.createCell(0);
		cell.setCellValue(notes.getSource());

		rowCount += 2;
		row = worksheet.createRow(rowCount);
		cell = row.createCell(0);
		cell.setCellValue("Note: ");
		setCellStyleTitre(workbook, cell);
		rowCount++;
		row = worksheet.createRow(rowCount);
		cell = row.createCell(0);
		cell.setCellValue(notes.getNotes());

		rowCount += 2;
		row = worksheet.createRow(rowCount);
		cell = row.createCell(0);
		cell.setCellValue("Term");
		setCellStyleColumn(workbook, cell);
		cell = row.createCell(1);
		cell.setCellValue("Code");
		setCellStyleColumn(workbook, cell);
		cell = row.createCell(2);
		cell.setCellValue("Level");
		setCellStyleColumn(workbook, cell);
		cell = row.createCell(3);
		cell.setCellValue("Category");
		setCellStyleColumn(workbook, cell);
		cell = row.createCell(4);
		cell.setCellValue("Weight");
		setCellStyleColumn(workbook, cell);
		cell = row.createCell(5);
		cell.setCellValue("Scope");
		setCellStyleColumn(workbook, cell);
		rowCount++;

		// Retrieval of relations - Loop
		List<CmqRelation190> relations = cmqRelationService
				.findByCmqCode(details.getCode());

		String level = "", term = "", codeTerm = "";

		if (relations != null) {
			for (CmqRelation190 relation : relations) {
				/**
				 * 
				 * SMQs
				 */
				if (relation.getSmqCode() != null) {
					List<Long> smqChildCodeList = new ArrayList<>();
					smqChildCodeList.add(relation.getSmqCode());

					SmqBase190 smqSearched = smqBaseService.findByCode(relation.getSmqCode());
					if (smqSearched != null) {
						List<SmqBase190> smqBaseList = smqBaseService
								.findByLevelAndTerm(smqSearched.getSmqLevel(),
										smqSearched.getSmqName());
						if (smqBaseList != null) {
							for (SmqBase190 smq : smqBaseList) {
								if (smq.getSmqLevel() == 1) {
									level = "SMQ1";
								} else if (smq.getSmqLevel() == 2) {
									level = "SMQ2";
								} else if (smq.getSmqLevel() == 3) {
									level = "SMQ3";
								} else if (smq.getSmqLevel() == 4) {
									level = "SMQ4";
								} else if (smq.getSmqLevel() == 5) {
									level = "SMQ5";
								}
								row = worksheet.createRow(rowCount);
								buildCells(level, smq.getSmqCode() + "",
										smq.getSmqName(), cell, row);
								setCellStyleColumn(workbook, cell);
								rowCount++;

								/**
								 * Other SMQs
								 * 
								 */
								List<SmqBase190> smqs = smqBaseService
										.findChildSmqByParentSmqCodes(smqChildCodeList);
								if (smqs != null)
									for (SmqBase190 smqC : smqs) {
										if (smqC.getSmqLevel() == 1) {
											level = "SMQ1";
										} else if (smqC.getSmqLevel() == 2) {
											level = "SMQ2";
										} else if (smqC.getSmqLevel() == 3) {
											level = "SMQ3";
										} else if (smqC.getSmqLevel() == 4) {
											level = "SMQ4";
										} else if (smqC.getSmqLevel() == 5) {
											level = "SMQ5";
										}
										row = worksheet.createRow(rowCount);
										buildChildCells(level,
												smqC.getSmqCode() + "",
												smqC.getSmqName(), cell, row,
												"......");
										rowCount++;
									}
							}
						}

						List<SmqRelation190> childRelations = this.smqBaseService
								.findSmqRelationsForSmqCode(smqSearched
										.getSmqCode());

						if (null != childRelations) {
							System.out
							.println("\n ******************** childRelations " + childRelations.size()	+ ", for " + smqSearched.getSmqCode());
							for (SmqRelation190 childRelation : childRelations) {
								if (childRelation.getSmqLevel() == 1) {
									level = "SMQ1";
								} else if (childRelation.getSmqLevel() == 2) {
									level = "SMQ2";
								} else if (childRelation.getSmqLevel() == 3) {
									level = "SMQ3";
								} else if ((childRelation.getSmqLevel() == 4)
										|| (childRelation.getSmqLevel() == 0)
										|| (childRelation.getSmqLevel() == 5)) {
									level = "PT";
								}

								row = worksheet.createRow(rowCount);
								buildChildCells(level, childRelation.getPtCode() + "", childRelation.getPtName(), cell, row, "......");
								rowCount++;
							}


						}
					}

				}

				/**
				 * 
				 * HLT.
				 */
				if (relation.getHltCode() != null) {
					List<Long> hltCodesList = new ArrayList<>();
					hltCodesList.add(relation.getHltCode());
					List<MeddraDictHierarchySearchDto> hlts = this.meddraDictService.findByCodes("HLT_", hltCodesList);
					for (MeddraDictHierarchySearchDto hlt : hlts) {
						row = worksheet.createRow(rowCount);
						buildCells("HLT", hlt.getCode(), hlt.getTerm(), cell, row);
						setCellStyleColumn(workbook, cell); 
						rowCount++;

						/**
						 * PT.
						 */
						List<MeddraDictHierarchySearchDto> listPT =  meddraDictService.findChildrenByParentCode("PT_", "HLT_", Long.valueOf(hlt.getCode()));
						List<Long> ptCodesList = new ArrayList<>();
						for (MeddraDictHierarchySearchDto meddra : listPT) {
							ptCodesList.add(Long.parseLong(meddra.getCode())); 
						}

						List<MeddraDictHierarchySearchDto> llts = this.meddraDictService.findByCodes("PT_", ptCodesList);
						if (llts != null)
							for (MeddraDictHierarchySearchDto llt : llts) {
								row = worksheet.createRow(rowCount);
								buildChildCells("PT", llt.getCode(), llt.getTerm(), cell, row, "......");
								rowCount++;
							}
					}
				}

				/**
				 * 
				 * PT
				 */
				if (relation.getPtCode() != null) {
					List<Long> ptCodesList = new ArrayList<>();
					ptCodesList.add(relation.getPtCode());
					List<MeddraDictHierarchySearchDto> pts = this.meddraDictService.findByCodes("PT_", ptCodesList);
					for (MeddraDictHierarchySearchDto pt : pts) {
						row = worksheet.createRow(rowCount);
						buildCells("PT", pt.getCode(), pt.getTerm(), cell, row);
						setCellStyleColumn(workbook, cell); 
						rowCount++;

						/**
						 * LLT.
						 */
						List<MeddraDictHierarchySearchDto> listPT =  meddraDictService.findChildrenByParentCode("LLT_", "PT_", Long.valueOf(pt.getCode()));
						List<Long> hlgtCodesList = new ArrayList<>();
						for (MeddraDictHierarchySearchDto meddra : listPT) {
							hlgtCodesList.add(Long.parseLong(meddra.getCode())); 
						}

						List<MeddraDictHierarchySearchDto> llts = this.meddraDictService.findByCodes("LLT_", hlgtCodesList);
						if (llts != null)
							for (MeddraDictHierarchySearchDto llt : llts) {
								row = worksheet.createRow(rowCount);
								buildChildCells("LLT", llt.getCode(), llt.getTerm(), cell, row, "......");
								rowCount++;
							}
					}
				}

				/**
				 * 
				 * SOC
				 */
				if (relation.getSocCode() != null) {
					List<Long> socCodesList = new ArrayList<>();
					socCodesList.add(relation.getSocCode());
					List<MeddraDictHierarchySearchDto> socss = this.meddraDictService.findByCodes("SOC_", socCodesList);
					for (MeddraDictHierarchySearchDto soc : socss) {
						row = worksheet.createRow(rowCount);
						buildCells("SOC", soc.getCode(), soc.getTerm(), cell, row);
						setCellStyleColumn(workbook, cell); 
						rowCount++;

						/**
						 * HLGT.
						 */
						List<MeddraDictHierarchySearchDto> listHLGT =  meddraDictService.findChildrenByParentCode("HLGT_", "SOC_", Long.valueOf(soc.getCode()));
						List<Long> hlgtCodesList = new ArrayList<>();
						for (MeddraDictHierarchySearchDto meddra : listHLGT) {
							hlgtCodesList.add(Long.parseLong(meddra.getCode())); 
						}

						List<MeddraDictHierarchySearchDto> hlgts = this.meddraDictService.findByCodes("HLGT_", hlgtCodesList);
						if (hlgts != null)
							for (MeddraDictHierarchySearchDto hlgt : hlgts) {
								row = worksheet.createRow(rowCount);
								buildChildCells("HLGT", hlgt.getCode(), hlgt.getTerm(), cell, row, "......");
								rowCount++;

								/**
								 * HLT.
								 */
								List<MeddraDictHierarchySearchDto> listHLT =  meddraDictService.findChildrenByParentCode("HLT_", "HLGT_", Long.valueOf(hlgt.getCode()));
								List<Long> hltCodesList = new ArrayList<>();
								for (MeddraDictHierarchySearchDto meddra : listHLT) {
									hltCodesList.add(Long.parseLong(meddra.getCode())); 
								}

								List<MeddraDictHierarchySearchDto> hlts = this.meddraDictService.findByCodes("HLT_", hltCodesList);
								if (hlts != null)
									for (MeddraDictHierarchySearchDto hlt : hlts) {
										row = worksheet.createRow(rowCount);
										buildChildCells("HLT", hlt.getCode(), hlt.getTerm(), cell, row, "...............");
										rowCount++;

										/**
										 * PT.
										 */
										List<MeddraDictHierarchySearchDto> listHT =  meddraDictService.findChildrenByParentCode("PT_", "HLT_", Long.valueOf(hlt.getCode()));
										List<Long> ptCodesList = new ArrayList<>();
										for (MeddraDictHierarchySearchDto meddra : listHT) {
											ptCodesList.add(Long.parseLong(meddra.getCode())); 
										}

										List<MeddraDictHierarchySearchDto> pts = this.meddraDictService.findByCodes("PT_", ptCodesList);
										if (pts != null)
											for (MeddraDictHierarchySearchDto pt : pts) {
												row = worksheet.createRow(rowCount);
												buildChildCells("PT", pt.getCode(), pt.getTerm(), cell, row, "..............");
												rowCount++;
											}
									}
							}
					}
				}

				/**
				 * 
				 * HLGT.
				 */
				if (relation.getHlgtCode() != null) {
					List<Long> hlgtCodesList = new ArrayList<>();
					hlgtCodesList.add(relation.getHlgtCode());
					List<MeddraDictHierarchySearchDto> socDtos = this.meddraDictService.findByCodes("HLGT_", hlgtCodesList);
					for (MeddraDictHierarchySearchDto hlgt : socDtos) {
						row = worksheet.createRow(rowCount);
						buildCells("HLGT", hlgt.getCode(), hlgt.getTerm(), cell, row);
						setCellStyleColumn(workbook, cell); 
						rowCount++;

						/**
						 * HLT.
						 */
						List<MeddraDictHierarchySearchDto> listHLGT =  meddraDictService.findChildrenByParentCode("HLT_", "HLGT_", Long.valueOf(hlgt.getCode()));
						List<Long> hltCodesList = new ArrayList<>();
						for (MeddraDictHierarchySearchDto meddra : listHLGT) {
							hltCodesList.add(Long.parseLong(meddra.getCode())); 
						}

						List<MeddraDictHierarchySearchDto> hlts = this.meddraDictService.findByCodes("HLT_", hltCodesList);
						if (hlts != null)
							for (MeddraDictHierarchySearchDto hlt : hlts) {
								row = worksheet.createRow(rowCount);
								buildChildCells("HLT", hlt.getCode(), hlt.getTerm(), cell, row, "......");
								rowCount++;

								/**
								 * PT.
								 */
								List<MeddraDictHierarchySearchDto> listHT =  meddraDictService.findChildrenByParentCode("PT_", "HLT_", Long.valueOf(hlt.getCode()));
								List<Long> ptCodesList = new ArrayList<>();
								for (MeddraDictHierarchySearchDto meddra : listHT) {
									ptCodesList.add(Long.parseLong(meddra.getCode())); 
								}

								List<MeddraDictHierarchySearchDto> pts = this.meddraDictService.findByCodes("PT_", ptCodesList);
								if (pts != null)
									for (MeddraDictHierarchySearchDto pt : pts) {
										row = worksheet.createRow(rowCount);
										buildChildCells("PT", pt.getCode(), pt.getTerm(), cell, row, "..............");
										rowCount++;
									}
							}
					}
				}
			}
			List<CmqBase190> childCmqs = findChildCmqsByParentCode(details.getCode());
			if((null != childCmqs) && (childCmqs.size() > 0)) {
				for (CmqBase190 childCmq : childCmqs) {
					level = childCmq.getCmqTypeCd();
					term = childCmq.getCmqName();
					codeTerm = childCmq.getCmqCode() != null ? childCmq.getCmqCode() + "" : "";

					row = worksheet.createRow(rowCount);
					buildCells(level, codeTerm, term, cell, row);
					rowCount++;
				}
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
					"Relations_MQ_Detailed_Report_" + details.getName()
					+ ".xlsx");
		} catch (IOException e) {
			e.printStackTrace();
		}

		return content;
	}
	
	private void printCodes(CmqRelation190 cmq) {
		if (cmq.getSmqCode() != null)
			System.out.println("********************** SMQ " + cmq.getSmqCode());
		System.out.println();
		if (cmq.getSocCode() != null)
			System.out.println("********************** SOC " + cmq.getSocCode());System.out.println();
		if (cmq.getHlgtCode() != null)
			System.out.println("********************** HLGT " + cmq.getHlgtCode());System.out.println();
		if (cmq.getHltCode() != null)
			System.out.println("********************** HLT " + cmq.getHltCode());System.out.println();
		if (cmq.getLltCode() != null)
			System.out.println("********************** LLT " + cmq.getLltCode());System.out.println();
		if (cmq.getPtCode() != null)
			System.out.println("********************** PT " + cmq.getPtCode());System.out.println();
		
	}

	private void setCellStyleTitre(XSSFWorkbook wb, XSSFCell cell) {
		XSSFCellStyle cellStyle = wb.createCellStyle();

		XSSFFont defaultFont = wb.createFont();
		defaultFont.setFontHeightInPoints((short) 14);
		defaultFont.setFontName("Arial");
		defaultFont.setColor(IndexedColors.BLACK.getIndex());
		defaultFont.setBold(true);
		defaultFont.setItalic(false);

		cellStyle.setFont(defaultFont);
		cell.setCellStyle(cellStyle);
	}

	private void setCellStyleColumn(XSSFWorkbook wb, XSSFCell cell) {
		XSSFCellStyle cellStyle = wb.createCellStyle();
		cellStyle.setFillBackgroundColor(IndexedColors.AQUA.getIndex());
		cellStyle.setFillPattern(XSSFCellStyle.SOLID_FOREGROUND);
		cellStyle.setFillForegroundColor(HSSFColor.GREY_40_PERCENT.index);

		XSSFFont defaultFont = wb.createFont();
		defaultFont.setFontHeightInPoints((short) 12);
		defaultFont.setFontName("Arial");
		defaultFont.setColor(IndexedColors.BLACK.getIndex());
		defaultFont.setBold(true);
		defaultFont.setItalic(false);

		cellStyle.setFont(defaultFont);
		cell.setCellStyle(cellStyle);
	}

	private void insertExporLogoImage(XSSFSheet sheet, XSSFWorkbook wb)
			throws IOException {
		FacesContext fc = FacesContext.getCurrentInstance();
		ExternalContext ec = fc.getExternalContext();
		final FileInputStream stream = new FileInputStream(
				ec.getRealPath("/image/logo.png"));
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

	public IRefCodeListService getRefCodeListService() {
		return refCodeListService;
	}

	public void setRefCodeListService(IRefCodeListService refCodeListService) {
		this.refCodeListService = refCodeListService;
	}
}
