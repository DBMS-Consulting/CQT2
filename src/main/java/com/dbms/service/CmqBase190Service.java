package com.dbms.service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.faces.bean.ApplicationScoped;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.persistence.TemporalType;

import org.apache.commons.codec.Charsets;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ArrayUtils;
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
import org.primefaces.model.StreamedContent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dbms.csmq.CSMQBean;
import com.dbms.entity.cqt.CmqBase190;
import com.dbms.entity.cqt.CmqRelation190;
import com.dbms.entity.cqt.SmqBase190;
import com.dbms.entity.cqt.SmqRelation190;
import com.dbms.entity.cqt.dtos.MeddraDictHierarchySearchDto;
import com.dbms.entity.cqt.dtos.MeddraDictReverseHierarchySearchDto;
import com.dbms.entity.cqt.dtos.ReportLineDataDto;
import com.dbms.service.base.CqtPersistenceService;
import com.dbms.util.CmqUtils;
import com.dbms.util.CqtConstants;
import com.dbms.util.exceptions.CqtServiceException;
import com.dbms.view.ListDetailsFormVM;
import com.dbms.view.ListNotesFormVM;
import com.dbms.web.dto.MQReportRelationsWorkerDTO;

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
			String group, String termName, String code, String[] designees) {
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
			sb.append(" c.cmqCode in (SELECT p.cmqCode FROM CmqProductBaseCurrent p WHERE lower(p.cmqProductCd) in (:cmqProductCds))");
            ArrayList<String> productCdList = new ArrayList<>(productCds.length);
            for(int i=0;i<productCds.length;i++)
                productCdList.add(StringUtils.lowerCase(productCds[i]));
			queryParams.put("cmqProductCds", productCdList);
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
            queryParams.put("cmqGroup", group.contains("%") ? group : ("%" + group + "%"));
			
			first = false;
		}
		if (StringUtils.isNotEmpty(termName)) {
			sb = appendClause(sb, first);
			sb.append(" lower(c.cmqName) like lower(:cmqName)");
			queryParams.put("cmqName", termName.contains("%") ? termName : ("%" + termName + "%"));
			first = false;
		}
		if (code != null) {
			sb = appendClause(sb, first);
			sb.append(" CAST(c.cmqCode as string) like :cmqCode");
			queryParams.put("cmqCode", code);
			first = false;
		}
        if(ArrayUtils.isNotEmpty(designees)) {
            sb = appendClause(sb, first);
			sb.append(" (c.cmqDesignee in (:cmqDesignees) or c.cmqDesignee2 in (:cmqDesignees) or c.cmqDesignee3 in (:cmqDesignees))");
			queryParams.put("cmqDesignees", Arrays.asList(designees));
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
			
			query.setHint("org.hibernate.cacheable", true);
			
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
			
			query.setHint("org.hibernate.cacheable", true);
			
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
	
	@Override
	public Boolean checkIfCmqNamqExists(String cmqName) {
		Boolean retVal = false;
		StringBuilder sb = new StringBuilder();
		sb.append("select count(*) from CmqBase190 c where c.cmqName = :cmqName");
		EntityManager entityManager = this.cqtEntityManagerFactory
				.getEntityManager();
		try {
			Query query = entityManager.createQuery(sb.toString());
			query.setParameter("cmqName", cmqName);
			
			Long count = (Long) query.getSingleResult();
			if(count > 0) {
				retVal = true;
			}
		} catch (Exception e) {
			StringBuilder msg = new StringBuilder();
			msg.append(
					"An error occurred while executing checkIfCmqNamqExists for cmqName ")
					.append(cmqName)
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
			retVal = entityManager.createQuery(query).setHint("org.hibernate.cacheable", true).getResultList();
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
			retVal = entityManager.createQuery(query).setHint("org.hibernate.cacheable", true).getResultList();
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
			query.setHint("org.hibernate.cacheable", true);
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
    @Override
	public List<Map<String, Object>> findCmqChildCountForParentCmqCodes(List<Long> cmqCodes) {
		List<Map<String, Object>> retVal = null;
        
        if(CollectionUtils.isEmpty(cmqCodes))
            return null;
        
		String queryString = CmqUtils.convertArrayToTableWith(cmqCodes, "tempCmqCodes", "code")
                + " select CMQ_CODE, count(*) as COUNT"
                + " from CMQ_BASE_CURRENT cmqTbl"
                + " inner join tempCmqCodes on tempCmqCodes.code=cmqTbl.CMQ_PARENT_CODE"
                + " group by CMQ_CODE";

		EntityManager entityManager = this.cqtEntityManagerFactory
				.getEntityManager();
		Session session = entityManager.unwrap(Session.class);
		try {
			SQLQuery query = session.createSQLQuery(queryString);
			query.addScalar("CMQ_CODE", StandardBasicTypes.LONG);
			query.addScalar("COUNT", StandardBasicTypes.LONG);
            
			query.setResultTransformer(Transformers.ALIAS_TO_ENTITY_MAP);
			query.setCacheable(true);
			retVal = query.list();
		} catch (Exception e) {
			StringBuilder msg = new StringBuilder();
			msg.append(
					"An error occurred while findCmqChildCountForParentCmqCode ")
					.append(cmqCodes).append(" Query used was ->")
					.append(queryString);
			LOG.error(msg.toString(), e);
		} finally {
			this.cqtEntityManagerFactory.closeEntityManager(entityManager);
		}
		return retVal;
	}
    
    @Override
    public boolean checkIfApprovedOnce(Long cmqCode) {
        boolean retVal = false;
        String q = "SELECT CMQ_STATE_OLD FROM CMQ_BASE_CURRENT_AUDIT cmqTblAudit WHERE cmqTblAudit.CMQ_CODE_NEW=:cmqCode ORDER BY AUDIT_TIMESTAMP DESC";
        EntityManager entityManager = this.cqtEntityManagerFactory.getEntityManager();
        Session session = entityManager.unwrap(Session.class);
        
        
		try {
			SQLQuery query = session.createSQLQuery(q);
			query.setParameter("cmqCode", cmqCode);
            query.addScalar("CMQ_STATE_OLD", StandardBasicTypes.STRING);
			query.setCacheable(true);
            
            List<Object> rows = query.list();

            for(Object row : rows) {
                if(row != null && CmqBase190.CMQ_STATE_VALUE_APPROVED.equalsIgnoreCase(row.toString())) {
                    retVal = true;
                }
            }
		} catch (Exception e) {
			retVal = false;
            StringBuilder msg = new StringBuilder();
			msg.append("An error occurred while checkAuditForOldStatus ")
					.append(cmqCode).append(" Query used was ->")
					.append(q);
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
			query.setHint("org.hibernate.cacheable", true);
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
			query.setHint("org.hibernate.cacheable", true);
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
		String queryString = "from CmqBase190 c where upper(c.cmqState) = upper('Approved') and c.cmqStatus = 'P' order by upper(c.cmqName) asc ";
		EntityManager entityManager = this.cqtEntityManagerFactory
				.getEntityManager();
		try {
			Query query = entityManager.createQuery(queryString);
			query.setHint("org.hibernate.cacheable", true);
			retVal = query.getResultList();
		} catch (Exception e) {
			StringBuilder msg = new StringBuilder();
			msg.append("findApprovedCmqs failed ").append("Query used was ->")
					.append(queryString);
			LOG.error(msg.toString(), e);
		} finally {
			this.cqtEntityManagerFactory.closeEntityManager(entityManager);
		}
		for (CmqBase190 c : retVal)
			System.out.println("PUBLISH: " + c.getCmqName());
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
			query.setHint("org.hibernate.cacheable", true);
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
			query.setHint("org.hibernate.cacheable", true);
			retVal = query.getResultList();
		} catch (Exception e) {
			StringBuilder msg = new StringBuilder();
			msg.append("findChildCmqsByCode failed ")
					.append("Query used was ->").append(queryString);
			LOG.error(msg.toString(), e);
		} finally {
			this.cqtEntityManagerFactory.closeEntityManager(entityManager);
		}
		return retVal;
	}

	public List<CmqBase190> findChildCmqsByCodes(List<Long> codes) {
		List<CmqBase190> retVal = null;
		String queryString = "from CmqBase190 c where c.cmqParentCode in (:codeList) ";
		EntityManager entityManager = this.cqtEntityManagerFactory
				.getEntityManager();
		try {
			Query query = entityManager.createQuery(queryString);
			query.setParameter("codeList", codes);
			query.setHint("org.hibernate.cacheable", true);
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
		String queryString = "from CmqBase190 c where c.cmqCode in (:codeList) ";
		EntityManager entityManager = this.cqtEntityManagerFactory
				.getEntityManager();
		try {
			Query query = entityManager.createQuery(queryString);
			query.setParameter("codeList", codes);
			query.setHint("org.hibernate.cacheable", true);
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
		String queryString = "from CmqBase190 c where upper(c.cmqState) = upper('Published') and c.cmqStatus = 'I' order by upper(c.cmqName) asc ";
		EntityManager entityManager = this.cqtEntityManagerFactory
				.getEntityManager();
		try {
			Query query = entityManager.createQuery(queryString);
			query.setHint("org.hibernate.cacheable", true);
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

	/**
	 * Find Current CMQs not in Target CMQs
	 */
	public List<CmqBase190> findCmqsToRetire() {
		List<CmqBase190> retVal = null;
		String queryString = "from CmqBase190 c where upper(c.cmqState) = upper('Published') and c.cmqStatus = 'A' "
				+ "and c.cmqCode not in (select target.cmqCode from CmqBaseTarget target where upper(target.cmqState) = upper('Published IA')) order by upper(c.cmqName) asc ";
		EntityManager entityManager = this.cqtEntityManagerFactory
				.getEntityManager();
		try {
			Query query = entityManager.createQuery(queryString);
			query.setHint("org.hibernate.cacheable", true);
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
			query.setHint("org.hibernate.cacheable", true);
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
	public StreamedContent generateExcelReport(ListDetailsFormVM details,
			String dictionaryVersion) {
		XSSFWorkbook workbook = new XSSFWorkbook();
		XSSFSheet worksheet = null;

		worksheet = workbook.createSheet("List Report");
		XSSFRow row = null;
		int rowCount = 6;

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
							
						} else if (childRelation.getSmqLevel() == 4) {
							level = "PT";
							
						} else if (childRelation.getSmqLevel() == 5) {
							level = "LLT";
							
						} 
						else if (childRelation.getSmqLevel() == 0) {
							level = "Child SMQ";
						}
//						} else if ((childRelation.getSmqLevel() == 4)
//								|| (childRelation.getSmqLevel() == 0)
//								|| (childRelation.getSmqLevel() == 5)) {
//							level = "PT";
//						}
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
						codeTerm = relation.getPtCode() + "";			
					}
				} else if (relation.getHlgtCode() != null) {
					MeddraDictHierarchySearchDto searchDto = this.meddraDictService
							.findByCode("HLGT_", relation.getHlgtCode());
					if (searchDto != null) {
						term = searchDto.getTerm();
						codeTerm = relation.getHlgtCode() + "";		
					}
					level = "HLGT";
				} else if (relation.getHltCode() != null) {					
					MeddraDictHierarchySearchDto searchDto = this.meddraDictService
							.findByCode("HLT_", relation.getHltCode());
					if (searchDto != null) {
						term = searchDto.getTerm();
						codeTerm = relation.getHltCode() + "";			
					}
					level = "HLT";
				} else if (relation.getSocCode() != null) {
					MeddraDictHierarchySearchDto searchDto = this.meddraDictService
							.findByCode("SOC_", relation.getSocCode());
					if (searchDto != null) {
						term = searchDto.getTerm();
						codeTerm = relation.getSocCode() + "";
					}
					level = "SOC";
				} else if (relation.getLltCode() != null) {
					MeddraDictReverseHierarchySearchDto searchDto = this.meddraDictService
							.findByPtOrLltCode("LLT_", relation.getLltCode());
					if (searchDto != null) {
						term = searchDto.getLltTerm();
						codeTerm = relation.getLltCode() + "";				
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

//		worksheet.autoSizeColumn(0);
//		worksheet.autoSizeColumn(1);
//		worksheet.autoSizeColumn(2);
//		worksheet.autoSizeColumn(3);
//		worksheet.autoSizeColumn(4);
//		worksheet.autoSizeColumn(5);

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
		cell.setCellValue(relation.getTermScope() != null ? returnScopeValue(relation.getTermScope()) : "");
		
	}

	private String returnScopeValue(String scopeVal) {
		if(CSMQBean.SCOPE_NARROW.equals(scopeVal))
            return "Narrow";
        else if(CSMQBean.SCOPE_BROAD.equals(scopeVal))
            return "Broad";
        else if(CSMQBean.SCOPE_CHILD_NARROW.equals(scopeVal))
            return "Child Narrow";
        else if(CSMQBean.SCOPE_FULL.equals(scopeVal))
            return "Full";
        return "";
 	}

	/**
	 * MQ Report.
	 */
	@Override
	public StreamedContent generateMQReport(ListDetailsFormVM details, ListNotesFormVM notes, String dictionaryVersion) {
		XSSFWorkbook workbook = new XSSFWorkbook();
		
		XSSFSheet worksheet = null;

		worksheet = workbook.createSheet("MQ Report");
		XSSFRow row = null;
		int rowCount = 6;

		try {
			insertExporLogoImage(worksheet, workbook);
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		
		Map<Integer, ReportLineDataDto> mapReport = new HashMap<Integer, ReportLineDataDto>();
		int cpt = 0;

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
		cell.setCellValue("Drug Program: " + refCodeListService.findCodeByInternalCode(CqtConstants.CODE_LIST_TYPE_PROGRAM, details.getDrugProgram()));
		rowCount++;
		row = worksheet.createRow(rowCount);
		cell = row.createCell(0);
		cell.setCellValue("Protocol: " + refCodeListService.findCodeByInternalCode(CqtConstants.CODE_LIST_TYPE_PROTOCOL, details.getProtocol()));
		rowCount++;
		row = worksheet.createRow(rowCount);
		cell = row.createCell(0);
		cell.setCellValue("Product List: " + refCodeListService.interpretProductCodesToValuesLabel(details.getProducts()));
		rowCount++;
		row = worksheet.createRow(rowCount);
		cell = row.createCell(0);
		if (details.getDesignee() != null)
			cell.setCellValue("Designee: " + details.getDesignee());
		else
			cell.setCellValue("Designee: ");
		rowCount++;
		row = worksheet.createRow(rowCount);
		cell = row.createCell(0);
		if (details.getDesigneeTwo() != null)
			cell.setCellValue("Designee 2: " + details.getDesigneeTwo());
		else
			cell.setCellValue("Designee 2: ");
		rowCount++;
		row = worksheet.createRow(rowCount);
		cell = row.createCell(0);
		if (details.getDesigneeThree() != null)
			cell.setCellValue("Designee 3: " + details.getDesigneeThree());
		else
			cell.setCellValue("Designee 3: ");
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
		cell.setCellValue("Initial Creation By: " + details.getCreatedBy() != null ? details.getCreatedBy() : "");
		rowCount++;
		row = worksheet.createRow(rowCount);
		cell = row.createCell(0);
		Calendar cal = Calendar.getInstance();
		if (details.getCreationDate() != null) {
			
			cal.setTime(details.getCreationDate());
			String creationDate = cal.get(Calendar.YEAR) + "-" + getTwoDigits(cal.get(Calendar.MONTH) + 1) + "-" + getTwoDigits(cal.get(Calendar.DAY_OF_MONTH)) 
					+ " " + getTwoDigits(cal.get(Calendar.HOUR)) + ":" + getTwoDigits(cal.get(Calendar.MINUTE)) + ":" + getTwoDigits(cal.get(Calendar.SECOND));
			cell.setCellValue("Initial Creation Date: " + creationDate);
		}
		else
			cell.setCellValue("Initial Creation Date: ");
		
 		
		rowCount++;
		row = worksheet.createRow(rowCount);
		cell = row.createCell(0);
		cell.setCellValue("Last Modified By: " + details.getLastModifiedBy() != null ? details.getLastModifiedBy() : "");
		
		rowCount++;
		row = worksheet.createRow(rowCount);
		cell = row.createCell(0);
		cal = Calendar.getInstance();
		if (details.getLastModifiedDate() != null) {
			cal.setTime(details.getLastModifiedDate());
			System.out.println("******* DATE : " + details.getLastModifiedDate());
			String modificationDate = cal.get(Calendar.YEAR) + "-" + getTwoDigits(cal.get(Calendar.MONTH) + 1) + "-" + getTwoDigits(cal.get(Calendar.DAY_OF_MONTH)) 
					+ " " + getTwoDigits(cal.get(Calendar.HOUR)) + ":" + getTwoDigits(cal.get(Calendar.MINUTE)) + ":" + getTwoDigits(cal.get(Calendar.SECOND));
	 		cell.setCellValue("Last Modification Date: " + modificationDate);
		}
		else
			cell.setCellValue("Last Modification Date: ");
		
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
		cell = row.createCell(6);
		//cell.setCellValue("PT Status");
		//setCellStyleColumn(workbook, cell);
		rowCount++;

		// Retrieval of relations - Loop
		List<CmqRelation190> relations = cmqRelationService.findByCmqCode(details.getCode());

		List<Future<MQReportRelationsWorkerDTO>> futures = new ArrayList<>();
		int workerId = 1;
		ExecutorService executorService = Executors.newFixedThreadPool(3);
		if (relations != null) {
			for (CmqRelation190 relation : relations) {
				MQReportRelationsWorker task = new MQReportRelationsWorker(workerId++, relation);
				futures.add(executorService.submit(task));
			}
		}
		LOG.info("Submitted all MQReportRelationsWorker for relations.");
		//now get the futures and process them.
		for (Future<MQReportRelationsWorkerDTO> future : futures) {
			try {
				MQReportRelationsWorkerDTO relationsWorkerDTO = future.get();
				if(relationsWorkerDTO.isSuccess()) {
					Map<Integer, ReportLineDataDto> mapReportData = relationsWorkerDTO.getMapReport();
					rowCount = fillReport(mapReportData, cell, row, rowCount, worksheet);
					mapReportData.clear();
				} else {
					LOG.info("Got false status for success in worker {}", relationsWorkerDTO.getWorkerName());
				}
			} catch (InterruptedException | ExecutionException e) {
				LOG.error("Exception while reading MQReportRelationsWorkerDTO", e);
			}
						
		}
		LOG.info("Processing children now.");
		//now child relations
		String level = "", term = "", codeTerm = "";
		List<CmqBase190> childCmqs = findChildCmqsByParentCode(details.getCode());
		if((null != childCmqs) && (childCmqs.size() > 0)) {
			LOG.info("Found child cmqs of size " + childCmqs.size()); 
			for (CmqBase190 childCmq : childCmqs) {
				level = childCmq.getCmqTypeCd();
				term = childCmq.getCmqName();
				codeTerm = childCmq.getCmqCode() != null ? childCmq.getCmqCode() + "" : "";

				mapReport.put(cpt++, new ReportLineDataDto(level, codeTerm, term, ""));
				rowCount = fillReport(mapReport, cell, row, rowCount, worksheet);
				mapReport.clear();
				
				/**
				 * Other Relations
				 */
				List<CmqRelation190> relationsPro = cmqRelationService.findByCmqCode(childCmq.getCmqCode());
				futures.clear();
				if (relations != null) {
					for (CmqRelation190 relation : relationsPro) {
						MQReportRelationsWorker task = new MQReportRelationsWorker(workerId++, relation);
						futures.add(executorService.submit(task));
					}
					
					LOG.info("Submitted all MQReportRelationsWorker for relations of child {}.", childCmq.getCmqCode());
					//now get the futures and process them.
					for (Future<MQReportRelationsWorkerDTO> future : futures) {
						try {
							MQReportRelationsWorkerDTO relationsWorkerDTO = future.get();
							if(relationsWorkerDTO.isSuccess()) {
								Map<Integer, ReportLineDataDto> mapReportData = relationsWorkerDTO.getMapReport();
								rowCount = fillReport(mapReportData, cell, row, rowCount, worksheet);
								mapReportData.clear();
							} else {
								LOG.info("Got false status for success in worker {}", relationsWorkerDTO.getWorkerName());
							}
						} catch (InterruptedException | ExecutionException e) {
							LOG.error("Exception while reading MQReportRelationsWorkerDTO", e);
						}
					}
				}
			}
		}
		
	
		
		
		LOG.info("Finished processing all relations and children.");
		executorService.shutdownNow();
			
		rowCount = fillReport(mapReport, cell, row, rowCount, worksheet);

//		worksheet.autoSizeColumn(0);
//		worksheet.autoSizeColumn(1);
//		worksheet.autoSizeColumn(2);
//		worksheet.autoSizeColumn(3);
//		worksheet.autoSizeColumn(4);
//		worksheet.autoSizeColumn(5);

		StreamedContent content = null;
		try {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			workbook.write(baos);
			//byte[] xls = baos.toByteArray();
			
			ByteArrayOutputStream zipBaos = new ByteArrayOutputStream();
		    ZipOutputStream zos = new ZipOutputStream(zipBaos);
		    ZipEntry entry = new ZipEntry("Relations_MQ_Detailed_Report_" + details.getName() + ".xlsx");
		    zos.putNextEntry(entry);
		    baos.writeTo(zos);
		    //workbook.write(zipBaos);
		    //zos.write(xls);
		    zos.flush();
		    zos.close();
			ByteArrayInputStream bais = new ByteArrayInputStream(zipBaos.toByteArray());
			content = new DefaultStreamedContent(
					bais,
					"application/zip",
					"Relations_MQ_Detailed_Report_" + details.getName()
					+ ".zip", Charsets.UTF_8.name());
		} catch (IOException e) {
			e.printStackTrace();
		}
		LOG.info("Finished MQ report generation.");
		return content;
	}
	
	private class MQReportRelationsWorker implements Callable<MQReportRelationsWorkerDTO> {
		private CmqRelation190 relation;
		private String level = "", term = "", codeTerm = "", workerName = null;
		
		public MQReportRelationsWorker(int workerId, CmqRelation190 relation) {
			this.workerName = "MQReportRelationsWorker_" + workerId;
			this.relation = relation;
		}
		
		@Override
		public MQReportRelationsWorkerDTO call() throws Exception {
			int cpt = 0;
			MQReportRelationsWorkerDTO relationsWorkerDTO = new MQReportRelationsWorkerDTO();
			relationsWorkerDTO.setWorkerName(workerName);
			LOG.info("In {} Starting Callable.", this.workerName);
			try {
				if (relation.getSmqCode() != null) {
 					List<Long> smqChildCodeList = new ArrayList<>();
					smqChildCodeList.add(relation.getSmqCode());

					SmqBase190 smqSearched = smqBaseService.findByCode(relation.getSmqCode());
					if (smqSearched != null) {
						List<SmqBase190> smqBaseList = smqBaseService.findByLevelAndTerm(smqSearched.getSmqLevel(),	smqSearched.getSmqName());
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
								relationsWorkerDTO.addToMapReport(cpt++, new ReportLineDataDto(level, smq.getSmqCode() + "", smq.getSmqName(), "", relation.getTermScope(),
										(relation.getTermWeight() != null ? relation.getTermWeight() + "" : ""), relation.getTermCategory(), "", ""));
								
								/**
								 * Other SMQs
								 * 
								 */
								List<SmqBase190> smqs = smqBaseService.findChildSmqByParentSmqCodes(smqChildCodeList);
								
								if (smqs != null) {
									for (SmqBase190 smqC : smqs) {
										if (smqC.getSmqLevel() == 1) {
											level = "SMQ1";
										} else if (smqC.getSmqLevel() == 2) {
											level = "SMQ2";
										} else if (smqC.getSmqLevel() == 3) {
											level = "SMQ3";
										} else if ((smqC.getSmqLevel() == 4)
												|| (smqC.getSmqLevel() == 0)
												|| (smqC.getSmqLevel() == 5)) {
											level = "PT";
										}
										relationsWorkerDTO.addToMapReport(cpt++, new ReportLineDataDto(level, smqC.getSmqCode() + "", smqC.getSmqName(), "......"));  
										
										if (level.equals("SMQ1")) {						
											smqSearched = smqBaseService.findByCode(smqC.getSmqCode());
											if (smqSearched != null) {
												List<SmqRelation190> list = smqBaseService.findSmqRelationsForSmqCode(smqSearched.getSmqCode());
												if (list != null) {
													for (SmqRelation190 smq3 : list) {
														relationsWorkerDTO.addToMapReport(cpt++, new ReportLineDataDto("PT", smq3.getPtCode() + "", smq3.getPtName(), ".............", smq3.getPtTermScope() + "", smq3.getPtTermWeight() + "", smq3.getPtTermCategory(), "", smq3.getPtTermStatus())); 
													}
												}
											}
											List<Long> codes = new ArrayList<>();
											codes.add(smqC.getSmqCode());
											
											//Others relations
											String levelS = "";
											if (smqSearched.getSmqLevel() == 2) {
												levelS = "SMQ2";
											}
											if (smqSearched.getSmqLevel() == 3) {
												levelS = "SMQ3";
											} else if (smqSearched.getSmqLevel() == 4) {
												levelS = "PT";
											} else if (smqSearched.getSmqLevel() == 5) {
												levelS = "LLT";
											} else if (smqSearched.getSmqLevel() == 0) {
												levelS = "Child SMQ";
											} 
											List<SmqBase190> smqChildren = smqBaseService.findChildSmqByParentSmqCodes(codes);
											if (smqChildren != null) {
												for (SmqBase190 child : smqChildren) {
													relationsWorkerDTO.addToMapReport(cpt++, new ReportLineDataDto(levelS, child.getSmqCode() + "", child.getSmqName(), ".............")); 
													
													codes = new ArrayList<>();
													codes.add(child.getSmqCode());
													
													//smqChildren = smqBaseService.findChildSmqByParentSmqCodes(codes);
													
													
													smqSearched = smqBaseService.findByCode(child.getSmqCode());
													if (smqSearched != null) {
														List<SmqRelation190> list = smqBaseService.findSmqRelationsForSmqCode(smqSearched.getSmqCode());
														if (list != null) {
															for (SmqRelation190 smq3 : list) {
																relationsWorkerDTO.addToMapReport(cpt++, new ReportLineDataDto("PT", smq3.getPtCode() + "", smq3.getPtName(), "....................", smq3.getPtTermScope() + "", smq3.getPtTermWeight() + "", smq3.getPtTermCategory(), "", smq3.getPtTermStatus())); 
															}
														}
													}
												}
											}
										}
										
										if (level.equals("SMQ2")) {						
											smqSearched = smqBaseService.findByCode(smqC.getSmqCode());
											if (smqSearched != null) {
												List<SmqRelation190> list = smqBaseService.findSmqRelationsForSmqCode(smqSearched.getSmqCode());
												if (list != null) {
													for (SmqRelation190 smq3 : list) {
														relationsWorkerDTO.addToMapReport(cpt++, new ReportLineDataDto("PT", smq3.getPtCode() + "", smq3.getPtName(), ".............", smq3.getPtTermScope() + "", smq3.getPtTermWeight() + "", smq3.getPtTermCategory(), "", smq3.getPtTermStatus())); 
													}
												}
											}
											List<Long> codes = new ArrayList<>();
											codes.add(smqC.getSmqCode());
											
											//Others relations
											String levelS = "";
											if (smqSearched.getSmqLevel() == 3) {
												levelS = "SMQ3";
											} else if (smqSearched.getSmqLevel() == 4) {
												levelS = "PT";
											} else if (smqSearched.getSmqLevel() == 5) {
												levelS = "LLT";
											} else if (smqSearched.getSmqLevel() == 0) {
												levelS = "Child SMQ";
											} 
											List<SmqBase190> smqChildren = smqBaseService.findChildSmqByParentSmqCodes(codes);
											if (smqChildren != null) {
												for (SmqBase190 child : smqChildren) {
													relationsWorkerDTO.addToMapReport(cpt++, new ReportLineDataDto(levelS, child.getSmqCode() + "", child.getSmqName(), ".............")); 
													
													smqSearched = smqBaseService.findByCode(child.getSmqCode());
													if (smqSearched != null) {
														List<SmqRelation190> list = smqBaseService.findSmqRelationsForSmqCode(smqSearched.getSmqCode());
														if (list != null) {
															for (SmqRelation190 smq3 : list) {
																relationsWorkerDTO.addToMapReport(cpt++, new ReportLineDataDto("PT", smq3.getPtCode() + "", smq3.getPtName(), "....................", smq3.getPtTermScope() + "", smq3.getPtTermWeight() + "", smq3.getPtTermCategory(), "", smq3.getPtTermStatus())); 
															}
														}
													}
												}
											}
										}
										
										if (level.equals("SMQ3")) {
											smqSearched = smqBaseService.findByCode(smqC.getSmqCode());
											if (smqSearched != null) {
												List<SmqRelation190> list = smqBaseService.findSmqRelationsForSmqCode(smqSearched.getSmqCode());
												if (list != null) {
													for (SmqRelation190 smq3 : list) {
														relationsWorkerDTO.addToMapReport(cpt++, new ReportLineDataDto("PT", smq3.getPtCode() + "", smq3.getPtName(), ".............", smq3.getPtTermScope() + "", smq3.getPtTermWeight() + "", smq3.getPtTermCategory(), "", smq3.getPtTermStatus())); 
													}
												}
											}
										}					
									}
								}
							}
						}
					}
					
					//Relations for SMQs
					Long smqBaseChildrenCount = smqBaseService.findChildSmqCountByParentSmqCode(relation.getSmqCode());
					smqBaseChildrenCount = smqBaseService.findSmqRelationsCountForSmqCode(relation.getSmqCode());

 					List<SmqRelation190> childSmqs =  smqBaseService.findSmqRelationsForSmqCode(relation.getSmqCode());

					if((null != childSmqs) && (childSmqs.size() > 0)) {
						for (SmqRelation190 childSmq : childSmqs) {
							if (childSmq.getSmqLevel() == 0) {
								level = "Child SMQ";
							}if (childSmq.getSmqLevel() == 1) {
								level = "SMQ1";
							} else if (childSmq.getSmqLevel() == 2) {
								level = "SMQ2";
							} else if (childSmq.getSmqLevel() == 3) {
								level = "SMQ3";
							} else if (childSmq.getSmqLevel() == 4) {
								level = "PT";
							} else if (childSmq.getSmqLevel() == 5) {
								level = "LLT";
							} 

							relationsWorkerDTO.addToMapReport(cpt++, new ReportLineDataDto(level, childSmq.getPtCode() + "", childSmq.getPtName(), ".......", childSmq.getPtTermScope() + "", childSmq.getPtTermWeight() + "", childSmq.getPtTermCategory(), "", childSmq.getPtTermStatus())); 
							
							List<Long> codes = new ArrayList<>();
							codes.add(childSmq.getSmqCode());
							
							List<SmqBase190> smqs = smqBaseService.findChildSmqByParentSmqCodes(codes);
							
							smqSearched = smqBaseService.findByCode(Long.parseLong(childSmq.getPtCode() + ""));
							if (smqSearched != null) {
								List<SmqRelation190> list = smqBaseService.findSmqRelationsForSmqCode(smqSearched.getSmqCode());
								if (list != null) {
									for (SmqRelation190 smq3 : list) {
										if (smq3.getSmqLevel() == 4) {
											level = "PT";
										} else if (smq3.getSmqLevel() == 5) {
											level = "LLT";
										} else if (smq3.getSmqLevel() == 0) {
											level = "Child SMQ";
										} 
										relationsWorkerDTO.addToMapReport(cpt++, new ReportLineDataDto(level, smq3.getPtCode() + "", smq3.getPtName(), ".............", smq3.getPtTermScope() + "", smq3.getPtTermWeight() + "", smq3.getPtTermCategory(), "", smq3.getPtTermStatus())); 
									
										/**
										 * 
										 * 
										 */
										if (level.equals("Child SMQ")) {										
											smqSearched = smqBaseService.findByCode(Long.parseLong(smq3.getPtCode() + ""));
											if (smqSearched != null) {
												List<SmqRelation190> test =  smqBaseService.findSmqRelationsForSmqCode(smqSearched.getSmqCode());
							 					System.out.println("\n ************ test size for " + smqSearched.getSmqName() + " = " + test.size());
							 					
							 					
							 					if (test != null) {
													for (SmqRelation190 tt : test) {
														if (tt.getSmqLevel() == 4) {
															level = "PT";
														} else if (tt.getSmqLevel() == 5) {
															level = "LLT";
														} else if (tt.getSmqLevel() == 0) {
															level = "Child SMQ";
														} 
									 					
														relationsWorkerDTO.addToMapReport(cpt++, new ReportLineDataDto(level, tt.getPtCode() + "", tt.getPtName(), "...................", 
																tt.getPtTermScope() + "", tt.getPtTermWeight() + "", tt.getPtTermCategory(), "", tt.getPtTermStatus())); 
														
														
														smqSearched = smqBaseService.findByCode(Long.parseLong(tt.getPtCode() + ""));
														
														if (smqSearched != null) {
															List<SmqRelation190> test2 =  smqBaseService.findSmqRelationsForSmqCode(smqSearched.getSmqCode());
										 					System.out.println("\n ************ test2 size for " + smqSearched.getSmqName() + " = " + test2.size());
										 					
										 					if (test2 != null) {
																for (SmqRelation190 tt2 : test2) {
																	if (tt2.getSmqLevel() == 4) {
																		level = "PT";
																	} else if (tt2.getSmqLevel() == 5) {
																		level = "LLT";
																	} else if (tt2.getSmqLevel() == 0) {
																		level = "Child SMQ";
																	} 
																	
																	relationsWorkerDTO.addToMapReport(cpt++, new ReportLineDataDto(level, tt2.getPtCode() + "", tt2.getPtName(), ".......................", 
																			tt2.getPtTermScope() + "", tt2.getPtTermWeight() + "", tt2.getPtTermCategory(), "", tt2.getPtTermStatus())); 
																	
																	
																	
																	if (level.equals("Child SMQ")) {										
																		smqSearched = smqBaseService.findByCode(Long.parseLong(tt2.getPtCode() + ""));
																		if (smqSearched != null) {
																			List<SmqRelation190> test3 =  smqBaseService.findSmqRelationsForSmqCode(smqSearched.getSmqCode());
														 					System.out.println("\n ************ test size for " + smqSearched.getSmqName() + " = " + test3.size());
														 					
														 					
														 					if (test3 != null) {
																				for (SmqRelation190 tt3 : test3) {
																					if (tt3.getSmqLevel() == 4) {
																						level = "PT";
																					} else if (tt3.getSmqLevel() == 5) {
																						level = "LLT";
																					} else if (tt3.getSmqLevel() == 0) {
																						level = "Child SMQ";
																					} 
																 					
																					relationsWorkerDTO.addToMapReport(cpt++, new ReportLineDataDto(level, tt3.getPtCode() + "", tt3.getPtName(), ".............................", 
																							tt3.getPtTermScope() + "", tt3.getPtTermWeight() + "", tt3.getPtTermCategory(), "", tt3.getPtTermStatus())); 
																					
																					 
																 					
																				}
														 					}
 																		}
																	}
																}
										 					}
														}
									 					
													}
							 					}
  											}
										}
						 					 
										
										/**
										 * 
										 * 
										 */
										
									}
								}
							}
 
						}
					}
					
					
					//LOG.info("In {} Finished Loading SMQ code relations.", this.workerName);
				}
				
				/**
				 * 
				 * HLT.
				 */
				if (relation.getHltCode() != null) {
					//LOG.info("In {} Loading HLT code relations.", this.workerName);
					List<Long> hltCodesList = new ArrayList<>();
					hltCodesList.add(relation.getHltCode());
					List<MeddraDictHierarchySearchDto> hlts = meddraDictService.findByCodes("HLT_", hltCodesList);
					for (MeddraDictHierarchySearchDto hlt : hlts) {
						relationsWorkerDTO.addToMapReport(cpt++, new ReportLineDataDto("HLT", hlt.getCode(), hlt.getTerm(), ""));  

						/**
						 * PT.
						 */
						List<MeddraDictHierarchySearchDto> listPT =  meddraDictService.findChildrenByParentCode("PT_", "HLT_", Long.valueOf(hlt.getCode()));
						List<Long> ptCodesList = new ArrayList<>();
						for (MeddraDictHierarchySearchDto meddra : listPT) {
							ptCodesList.add(Long.parseLong(meddra.getCode())); 
						}

						List<MeddraDictHierarchySearchDto> llts = meddraDictService.findByCodes("PT_", ptCodesList);
						if (llts != null) {
							for (MeddraDictHierarchySearchDto llt : llts) {
								relationsWorkerDTO.addToMapReport(cpt++, new ReportLineDataDto("PT", llt.getCode() + "", llt.getTerm(), "......")); 
								
								/**
								 * LLT.
								 */
								List<MeddraDictHierarchySearchDto> lltCodesList =  meddraDictService.findChildrenByParentCode("LLT_", "PT_", Long.valueOf(llt.getCode()));
								List<Long> lltCodesList_0 = new ArrayList<>();
								for (MeddraDictHierarchySearchDto meddra : lltCodesList) {
									lltCodesList_0.add(Long.parseLong(meddra.getCode())); 
								}

								List<MeddraDictHierarchySearchDto> llts_0 = meddraDictService.findByCodes("LLT_", lltCodesList_0);
								if (llts_0 != null) {
									for (MeddraDictHierarchySearchDto llt_1 : llts_0) {
										relationsWorkerDTO.addToMapReport(cpt++, new ReportLineDataDto("LLT", llt_1.getCode() + "", llt_1.getTerm(), ".............")); 
									}
								}
							}
						}
					}
					//LOG.info("In {} Finished Loading HLT code relations.", this.workerName);
				}
				
				/**
				 * 
				 * LLT.
				 */
				if (relation.getLltCode() != null) {
					//LOG.info("In {} Loading LLT code relations.", this.workerName);
					List<Long> lltCodesList = new ArrayList<>();
					lltCodesList.add(relation.getLltCode());
					List<MeddraDictHierarchySearchDto> llts = meddraDictService.findByCodes("LLT_", lltCodesList);
					for (MeddraDictHierarchySearchDto llt : llts) {
						relationsWorkerDTO.addToMapReport(cpt++, new ReportLineDataDto("LLT", llt.getCode() + "", llt.getTerm(), "")); 
					}
					//LOG.info("In {} Finished Loading LLT code relations.", this.workerName);
				}
				
				/**
				 * 
				 * PT
				 */
				if (relation.getPtCode() != null) {
					//LOG.info("In {} Loading PT code relations.", this.workerName);
					List<Long> ptCodesList = new ArrayList<>();
					ptCodesList.add(relation.getPtCode());
					List<MeddraDictHierarchySearchDto> pts = meddraDictService.findByCodes("PT_", ptCodesList);
					for (MeddraDictHierarchySearchDto pt : pts) {
						relationsWorkerDTO.addToMapReport(cpt++, new ReportLineDataDto("PT", pt.getCode() + "", pt.getTerm(), "")); 
						
						/**
						 * LLT.
						 */
						List<MeddraDictHierarchySearchDto> listPT =  meddraDictService.findChildrenByParentCode("LLT_", "PT_", Long.valueOf(pt.getCode()));
						List<Long> hlgtCodesList = new ArrayList<>();
						for (MeddraDictHierarchySearchDto meddra : listPT) {
							hlgtCodesList.add(Long.parseLong(meddra.getCode())); 
						}

						List<MeddraDictHierarchySearchDto> llts = meddraDictService.findByCodes("LLT_", hlgtCodesList);
						if (llts != null) {
							for (MeddraDictHierarchySearchDto llt : llts) {
								relationsWorkerDTO.addToMapReport(cpt++, new ReportLineDataDto("LLT", llt.getCode() + "", llt.getTerm(), "......"));
							}
						}
					}
					//LOG.info("In {} Finished Loading PT code relations.", this.workerName);
				}
				
				
				/**
				 * 
				 * SOC
				 */
				if (relation.getSocCode() != null) {
					//LOG.info("In {} Loading SOC code relations.", this.workerName);
					List<Long> socCodesList = new ArrayList<>();
					socCodesList.add(relation.getSocCode());
					List<MeddraDictHierarchySearchDto> socss = meddraDictService.findByCodes("SOC_", socCodesList);
					for (MeddraDictHierarchySearchDto soc : socss) {
						relationsWorkerDTO.addToMapReport(cpt++, new ReportLineDataDto("SOC", soc.getCode() + "", soc.getTerm(), "")); 


						/**
						 * HLGT.
						 */
						List<MeddraDictHierarchySearchDto> listHLGT =  meddraDictService.findChildrenByParentCode("HLGT_", "SOC_", Long.valueOf(soc.getCode()));
						List<Long> hlgtCodesList = new ArrayList<>();
						for (MeddraDictHierarchySearchDto meddra : listHLGT) {
							hlgtCodesList.add(Long.parseLong(meddra.getCode())); 
						}

						List<MeddraDictHierarchySearchDto> hlgts = meddraDictService.findByCodes("HLGT_", hlgtCodesList);
						if (hlgts != null) {
							for (MeddraDictHierarchySearchDto hlgt : hlgts) {
								relationsWorkerDTO.addToMapReport(cpt++, new ReportLineDataDto("HLGT", hlgt.getCode() + "", hlgt.getTerm(), "......"));

								/**
								 * HLT.
								 */
								List<MeddraDictHierarchySearchDto> listHLT =  meddraDictService.findChildrenByParentCode("HLT_", "HLGT_", Long.valueOf(hlgt.getCode()));
								List<Long> hltCodesList = new ArrayList<>();
								for (MeddraDictHierarchySearchDto meddra : listHLT) {
									hltCodesList.add(Long.parseLong(meddra.getCode())); 
								}

								List<MeddraDictHierarchySearchDto> hlts = meddraDictService.findByCodes("HLT_", hltCodesList);
								if (hlts != null) {
									for (MeddraDictHierarchySearchDto hlt : hlts) {
										relationsWorkerDTO.addToMapReport(cpt++, new ReportLineDataDto("HLT", hlt.getCode() + "", hlt.getTerm(), "...............")); 

										/**
										 * PT.
										 */
										List<MeddraDictHierarchySearchDto> listHT =  meddraDictService.findChildrenByParentCode("PT_", "HLT_", Long.valueOf(hlt.getCode()));
										List<Long> ptCodesList = new ArrayList<>();
										for (MeddraDictHierarchySearchDto meddra : listHT) {
											ptCodesList.add(Long.parseLong(meddra.getCode())); 
										}

										List<MeddraDictHierarchySearchDto> pts = meddraDictService.findByCodes("PT_", ptCodesList);
										if (pts != null) {
											for (MeddraDictHierarchySearchDto pt : pts) {
												relationsWorkerDTO.addToMapReport(cpt++, new ReportLineDataDto("PT", pt.getCode() + "", pt.getTerm(), "....................")); 
												
												/**
												 * LLT.
												 */
												List<MeddraDictHierarchySearchDto> listPT =  meddraDictService.findChildrenByParentCode("LLT_", "PT_", Long.valueOf(pt.getCode()));
												List<Long> lltCodesList = new ArrayList<>();
												for (MeddraDictHierarchySearchDto meddra : listPT) {
													lltCodesList.add(Long.parseLong(meddra.getCode())); 
												}

												List<MeddraDictHierarchySearchDto> llts = meddraDictService.findByCodes("LLT_", lltCodesList);
												if (llts != null) {
													for (MeddraDictHierarchySearchDto llt : llts) {
														relationsWorkerDTO.addToMapReport(cpt++, new ReportLineDataDto("LLT", llt.getCode() + "", llt.getTerm(), "..........................")); 
													}
												}
											}
										}
									}
								}
							}
						}
					}
					
					//LOG.info("In {} Finished Loading SOC code relations.", this.workerName);
				}
				
				/**
				 * 
				 * HLGT.
				 */
				if (relation.getHlgtCode() != null) {
					//LOG.info("In {} Loading HLGT code relations.", this.workerName);
					List<Long> hlgtCodesList = new ArrayList<>();
					hlgtCodesList.add(relation.getHlgtCode());
					List<MeddraDictHierarchySearchDto> socDtos = meddraDictService.findByCodes("HLGT_", hlgtCodesList);
					for (MeddraDictHierarchySearchDto hlgt : socDtos) {
						relationsWorkerDTO.addToMapReport(cpt++, new ReportLineDataDto("HLGT", hlgt.getCode() + "", hlgt.getTerm(), ""));  

						/**
						 * HLT.
						 */
						List<MeddraDictHierarchySearchDto> listHLGT =  meddraDictService.findChildrenByParentCode("HLT_", "HLGT_", Long.valueOf(hlgt.getCode()));
						List<Long> hltCodesList = new ArrayList<>();
						for (MeddraDictHierarchySearchDto meddra : listHLGT) {
							hltCodesList.add(Long.parseLong(meddra.getCode())); 
						}

						List<MeddraDictHierarchySearchDto> hlts = meddraDictService.findByCodes("HLT_", hltCodesList);
						if (hlts != null) {
							for (MeddraDictHierarchySearchDto hlt : hlts) {
								relationsWorkerDTO.addToMapReport(cpt++, new ReportLineDataDto("HLT", hlt.getCode() + "", hlt.getTerm(), "......")); 
								
								/**
								 * PT.
								 */
								List<MeddraDictHierarchySearchDto> listHT =  meddraDictService.findChildrenByParentCode("PT_", "HLT_", Long.valueOf(hlt.getCode()));
								List<Long> ptCodesList = new ArrayList<>();
								for (MeddraDictHierarchySearchDto meddra : listHT) {
									ptCodesList.add(Long.parseLong(meddra.getCode())); 
								}

								List<MeddraDictHierarchySearchDto> pts = meddraDictService.findByCodes("PT_", ptCodesList);
								if (pts != null) {
									for (MeddraDictHierarchySearchDto pt : pts) {
										relationsWorkerDTO.addToMapReport(cpt++, new ReportLineDataDto("PT", pt.getCode() + "", pt.getTerm(), "...............")); 
										
										/**
										 * LLT.
										 */
										List<MeddraDictHierarchySearchDto> listLLT =  meddraDictService.findChildrenByParentCode("LLT_", "PT_", Long.valueOf(pt.getCode()));
										List<Long> lltCodesList = new ArrayList<>();
										for (MeddraDictHierarchySearchDto meddra : listLLT) {
											lltCodesList.add(Long.parseLong(meddra.getCode())); 
										}

										List<MeddraDictHierarchySearchDto> list = meddraDictService.findByCodes("LLT_", lltCodesList);
										if (list != null) {
											for (MeddraDictHierarchySearchDto llt : list) {
												relationsWorkerDTO.addToMapReport(cpt++, new ReportLineDataDto("LLT", llt.getCode() + "", llt.getTerm(), ".............")); 
											}
										}
									}
								}
							}
						}
					}
					
					//LOG.info("In {} Finished Loading HLGT code relations.", this.workerName);
				}
				relationsWorkerDTO.setSuccess(true);
				LOG.info("In {} Finished Callable.", this.workerName);
			} catch (Exception e) {
				relationsWorkerDTO.setSuccess(false);
				LOG.error("In {} Exception occured whiel processing", this.workerName, e);
			}
			return relationsWorkerDTO;
		}
	}
	
	private String getTwoDigits(int number) {
		if (number < 10)
			return "0"+number;
		else return number+"";
		
	}
	
	private int fillReport(Map<Integer, ReportLineDataDto> mapReport, XSSFCell cell, XSSFRow row, int rowCount, XSSFSheet worksheet) {
		int cpt = 0;
		LOG.info("Writing {} ReportLineDataDtos." , mapReport.size());
		for(Map.Entry<Integer, ReportLineDataDto> entry : mapReport.entrySet()) {
			ReportLineDataDto line = entry.getValue();
			if(null != line) {
				row = worksheet.createRow(rowCount);

				// Cell 0
				cell = row.createCell(0);
				cell.setCellValue(line.getDots() + line.getTerm());

				// Cell 1
				cell = row.createCell(1);
				cell.setCellValue(line.getCode());

				// Cell 2
				cell = row.createCell(2);
				cell.setCellValue(line.getLevel());
				
				// Cell 3
				cell = row.createCell(3);
				cell.setCellValue(line.getCategory());
				
				// Cell 4
				cell = row.createCell(4);
				cell.setCellValue(line.getWeight());
				
				// Cell 5
				cell = row.createCell(5);
				cell.setCellValue(returnScopeValue(line.getScope()));
				
				// Cell 6
				//cell = row.createCell(5);
				//cell.setCellValue(line.getStatus());
				
				rowCount++;		
			} else {
				LOG.info("Got null line in map in fillReport");
			}
			cpt++;
		}
		return rowCount;
		
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
				ec.getRealPath("/image/logo.jpg"));
		final CreationHelper helper = wb.getCreationHelper();
		final Drawing drawing = sheet.createDrawingPatriarch();

		final ClientAnchor anchor = helper.createClientAnchor();
		anchor.setAnchorType(ClientAnchor.DONT_MOVE_AND_RESIZE);

		final int pictureIndex = wb.addPicture(stream,
				Workbook.PICTURE_TYPE_PNG);

//		anchor.setCol1(0);
//		anchor.setRow1(0); // same row is okay
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
