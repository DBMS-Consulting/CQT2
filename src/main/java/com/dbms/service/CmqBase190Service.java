package com.dbms.service;

import com.dbms.controller.CreateController;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.faces.bean.ApplicationScoped;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.persistence.CacheStoreMode;
import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.persistence.TemporalType;

import com.dbms.util.workers.UniquePTReportRelationsWorker;
import org.apache.commons.codec.Charsets;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.ClientAnchor;
import org.apache.poi.ss.usermodel.ClientAnchor.AnchorType;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.Drawing;
import org.apache.poi.ss.usermodel.FillPatternType;
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
import org.primefaces.model.TreeNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dbms.controller.GlobalController;
import com.dbms.csmq.CSMQBean;
import com.dbms.csmq.HierarchyNode;
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
import com.dbms.view.SystemConfigProperties;
import com.dbms.web.dto.MQReportRelationsWorkerDTO;
import javax.annotation.PostConstruct;

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

	private static final String CMQ_BASE_TABLE_PREFIX = "CMQ_BASE_";
	private static final Comparator<ReportLineDataDto> LEVELNUM_TERM_REPORT_LINE_DTO_COMPARATOR = (o1, o2) -> {
		if(o1.getLevelNum() == null && o2.getLevelNum() != null) {
			return 1;
		} else if (o1.getLevelNum() != null && o2.getLevelNum() == null) {
			return -1;
		} else if ((o1.getLevelNum() == null && o2.getLevelNum() == null) || o1.getLevelNum().compareTo(o2.getLevelNum()) == 0) {
			return o1.getTerm().toLowerCase().compareTo(o2.getTerm().toLowerCase());
		} else {
			return o1.getLevelNum().compareTo(o2.getLevelNum());
		}
	};
	private static final Comparator<ReportLineDataDto> LEVELNUM_REPORT_LINE_DTO_COMPARATOR = (o1, o2) -> {
		if(o1.getLevelNum() == null && o2.getLevelNum() != null) {
			return 1;
		} else if (o1.getLevelNum() != null && o2.getLevelNum() == null) {
			return -1;
		} else if ((o1.getLevelNum() == null && o2.getLevelNum() == null) || o1.getLevelNum().compareTo(o2.getLevelNum()) == 0) {
			return 0;
		} else {
			return o1.getLevelNum().compareTo(o2.getLevelNum());
		}
	};

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
		sb.append(" order by c.cmqName asc");
        
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
			query.setHint("javax.persistence.cache.storeMode", CacheStoreMode.USE);
			
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
    
	@SuppressWarnings("unchecked")
    @Override
	public List<Map<String, Object>> findCmqChildCountForParentCmqCodes(List<Long> cmqCodes, String dictionaryVersion) {
		List<Map<String, Object>> retVal = null;
        
        if(CollectionUtils.isEmpty(cmqCodes))
            return null;
        
		String queryString = CmqUtils.convertArrayToTableWith(cmqCodes, "tempCmqCodes", "code")
                + " select CMQ_CODE, count(*) as COUNT"
                + " from " + CMQ_BASE_TABLE_PREFIX + dictionaryVersion +" cmqTbl"
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
			query.setCacheable(false);
            
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
    
    @Override
    public boolean checkIfInactiveFor10Mins(Long cmqCode) {
    		Date d = new Date();
        boolean retVal = false;
        String q = "SELECT CMQ_STATUS_NEW FROM CMQ_BASE_CURRENT_AUDIT cmqTblAudit WHERE cmqTblAudit.CMQ_CODE_NEW=:cmqCode ORDER BY AUDIT_TIMESTAMP DESC";
        EntityManager entityManager = this.cqtEntityManagerFactory.getEntityManager();
        Session session = entityManager.unwrap(Session.class);
        
        
		try {
			SQLQuery query = session.createSQLQuery(q);
			query.setParameter("cmqCode", cmqCode);
            //query.addScalar("AUDIT_TIMESTAMP", StandardBasicTypes.TIMESTAMP);
            query.addScalar("CMQ_STATUS_NEW", StandardBasicTypes.STRING);
			query.setCacheable(false);
            
            List<Object> rows = query.list();

            for(Object row : rows) {
                if(row != null && CmqBase190.CMQ_STATUS_VALUE_INACTIVE.equalsIgnoreCase(row.toString())) {
                		if(d.getTime() - findByCode(cmqCode).getLastModifiedDate().getTime() > 600000)
                		retVal = true;
                }
            }
		} catch (Exception e) {
			retVal = false;
            StringBuilder msg = new StringBuilder();
			msg.append("An error occurred while checkAuditForNewStatus ")
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
		String queryString = "from CmqBase190 c where upper(c.cmqState) = upper('Approved') "
				+"and c.cmqCode not in (select target.cmqCode from CmqBaseTarget target where upper(target.cmqState) = upper('REVIEWED IA') or upper(target.cmqState) = upper('Approved IA') or upper(target.cmqState) = upper('Published IA')) order by upper(c.cmqName) asc ";;
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
	
	@SuppressWarnings("unchecked")
	public List<CmqBase190> findChildCmqsByParentCode(Long code, String dictionaryVersion) {
		List<CmqBase190> retVal = null;
		String queryString = "Select * from " + CMQ_BASE_TABLE_PREFIX + dictionaryVersion + " where CMQ_PARENT_CODE = :codeList ";
		EntityManager entityManager = this.cqtEntityManagerFactory.getEntityManager();
		Session session = entityManager.unwrap(Session.class);
		try {
			SQLQuery query = session.createSQLQuery(queryString);
			query.setParameter("codeList", code);
			query.addEntity(CmqBase190.class);
			retVal = query.list();
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
		String queryString = "from CmqBase190 c where upper(c.cmqState) = upper('Published') and c.cmqStatus = 'I' "
				+"and c.cmqCode not in (select target.cmqCode from CmqBaseTarget target where upper(target.cmqState) = upper('REVIEWED IA') or upper(target.cmqState) = upper('Approved IA') or upper(target.cmqState) = upper('Published IA')) order by upper(c.cmqName) asc ";
		EntityManager entityManager = this.cqtEntityManagerFactory
				.getEntityManager();
		try {
			Query query = entityManager.createQuery(queryString);
			query.setHint("org.hibernate.cacheable", true);
			retVal = query.getResultList();
		} catch (Exception e) {
			StringBuilder msg = new StringBuilder();
			msg.append("findCmqsToReactivate failed ").append("Query used was ->")
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
				+ "and c.cmqCode not in (select target.cmqCode from CmqBaseTarget target where upper(target.cmqState) = upper('Published IA') or upper(target.cmqState) = upper('REVIEWED IA') or upper(target.cmqState) = upper('Approved IA')) order by upper(c.cmqName) asc ";
		EntityManager entityManager = this.cqtEntityManagerFactory
				.getEntityManager();
		try {
			Query query = entityManager.createQuery(queryString);
			query.setHint("org.hibernate.cacheable", true);
			retVal = query.getResultList();
		} catch (Exception e) {
			StringBuilder msg = new StringBuilder();
			msg.append("findCmqsToRetire failed ").append("Query used was ->")
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
			String dictionaryVersion, SystemConfigProperties systemConfigProperties) {
		XSSFWorkbook workbook = new XSSFWorkbook();
		XSSFSheet worksheet = null;
		DateFormat dateTimeFormat = new SimpleDateFormat("dd-MMM-yyyy:hh:mm:ss a z");

		worksheet = workbook.createSheet("List Report");
		XSSFRow row = null;
		int rowCount = 6;

		try {
			insertExporLogoImage(worksheet, workbook);
		} catch (IOException e1) {
			e1.printStackTrace();
		}

		/**
		 * 
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
		cell.setCellValue("MedDRA Dictionary Version: " + refCodeListService.interpretDictionaryVersion(dictionaryVersion));

		rowCount++;
		row = worksheet.createRow(rowCount);
		cell = row.createCell(0);
		cell.setCellValue("List Name: " + details.getName());

		rowCount++;
		row = worksheet.createRow(rowCount);
		cell = row.createCell(0);
		cell.setCellValue("Code: " + details.getCode());

		rowCount++;
		row = worksheet.createRow(rowCount);
		cell = row.createCell(0);
		cell.setCellValue("Level Extension: " + details.getExtension());

                rowCount++;
		row = worksheet.createRow(rowCount);
		cell = row.createCell(0);
		cell.setCellValue("Status: " + details.getStatus());

		rowCount++;
		row = worksheet.createRow(rowCount);
		cell = row.createCell(0);
		cell.setCellValue("Report Date/Time: " + dateTimeFormat.format(new Date()));
		cell = row.createCell(1);

		rowCount += 2;
		row = worksheet.createRow(rowCount);
                
                int cellCount = 0;

		cell = row.createCell(cellCount);
		cell.setCellValue("Term");
		setCellStyleColumn(workbook, cell);
                
                cellCount++;
		cell = row.createCell(cellCount);
		cell.setCellValue("Code");
		setCellStyleColumn(workbook, cell);
                
		cellCount++;
		cell = row.createCell(cellCount);
		cell.setCellValue("Level");
		setCellStyleColumn(workbook, cell);
                
                if(systemConfigProperties.isDisplayCategory()) {
                    cellCount++;
                    cell = row.createCell(cellCount);
                    cell.setCellValue("Category");
                    setCellStyleColumn(workbook, cell);
                }
                
		if(systemConfigProperties.isDisplayCategory2()) {
                    cellCount++;
                    cell = row.createCell(cellCount);
                    cell.setCellValue("Category2");
                    setCellStyleColumn(workbook, cell);
                }
                
		if(systemConfigProperties.isDisplayWeight()) {
                    cellCount++;
                    cell = row.createCell(cellCount);
                    cell.setCellValue("Weight");
                    setCellStyleColumn(workbook, cell);
                }
                
		if(systemConfigProperties.isDisplayScope()) {
                    cellCount++;
                    cell = row.createCell(cellCount);
                    cell.setCellValue("Scope");
                    setCellStyleColumn(workbook, cell);
                }
		rowCount++;

		// Retrieval of relations - Loop
		List<CmqRelation190> relations = cmqRelationService
				.findByCmqCode(details.getCode());
		CmqBase190 cmq = findByCode(details.getCode());
		String cmqDictionaryVersion = cmq.getDictionaryVersion();
		
		//Long code = null;
 		// MeddraDictReverseHierarchySearchDto search = null;
		String level = "", term = "", codeTerm = "";

		List<ReportLineDataDto> dtos = new ArrayList<>();
		if (relations != null) {
			for (CmqRelation190 relation : relations) {
				ReportLineDataDto dto = new ReportLineDataDto();
				if (relation.getSmqCode() != null) {
					if (relation.getPtCode() != null) {
						SmqRelation190 childRelation = this.smqBaseService
								.findSmqRelationBySmqAndPtCode(relation
										.getSmqCode(), relation.getPtCode()
										.intValue());
						dto.setLevelNum(childRelation.getSmqLevel() == null ? null : childRelation.getSmqLevel().longValue());
						if (childRelation.getSmqLevel() == 1) {
							level = "SMQ1";
						} else if (childRelation.getSmqLevel() == 2) {
							level = "SMQ2";
						} else if (childRelation.getSmqLevel() == 3) {
							level = "SMQ3";
							
						} else if (childRelation.getSmqLevel() == 4) {
							level = "PT";
							relation.setTermScope(relation.getTermScope());
							relation.setTermWeight(relation.getTermWeight());
							
						} else if (childRelation.getSmqLevel() == 5) {
							level = "LLT";
							relation.setTermScope(null);
							relation.setTermWeight(null);
							
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
					} else if(relation.getLltCode() != null) {
						SmqRelation190 childRelation = this.smqBaseService
								.findSmqRelationBySmqAndPtCode(relation
										.getSmqCode(), relation.getLltCode()
										.intValue());
						dto.setLevelNum(childRelation.getSmqLevel() == null ? null : childRelation.getSmqLevel().longValue());
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
						dto.setLevelNum(smqBase.getSmqLevel() == null ? null : smqBase.getSmqLevel().longValue());
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
					dto.setLevelNum(9L);
					level = "PT";
					MeddraDictReverseHierarchySearchDto search = this.meddraDictService
							.findByPtOrLltCode("PT_", relation.getPtCode(),cmqDictionaryVersion);
					if (search != null) {
						term = search.getPtTerm();
						codeTerm = relation.getPtCode() + "";			
					}
				} else if (relation.getHlgtCode() != null) {
					MeddraDictHierarchySearchDto searchDto = this.meddraDictService
							.findByCode("HLGT_", relation.getHlgtCode(),cmqDictionaryVersion);
					if (searchDto != null) {
						term = searchDto.getTerm();
						codeTerm = relation.getHlgtCode() + "";		
					}
					dto.setLevelNum(7L);
					level = "HLGT";
				} else if (relation.getHltCode() != null) {					
					MeddraDictHierarchySearchDto searchDto = this.meddraDictService
							.findByCode("HLT_", relation.getHltCode(),cmqDictionaryVersion);
					if (searchDto != null) {
						term = searchDto.getTerm();
						codeTerm = relation.getHltCode() + "";			
					}
					dto.setLevelNum(8L);
					level = "HLT";
				} else if (relation.getSocCode() != null) {
					MeddraDictHierarchySearchDto searchDto = this.meddraDictService
							.findByCode("SOC_", relation.getSocCode(),cmqDictionaryVersion);
					if (searchDto != null) {
						term = searchDto.getTerm();
						codeTerm = relation.getSocCode() + "";
					}
					dto.setLevelNum(6L);
					level = "SOC";
				} else if (relation.getLltCode() != null) {
					MeddraDictReverseHierarchySearchDto searchDto = this.meddraDictService
							.findByPtOrLltCode("LLT_", relation.getLltCode(),cmqDictionaryVersion);
					if (searchDto != null) {
						term = searchDto.getLltTerm();
						codeTerm = relation.getLltCode() + "";				
					}
					dto.setLevelNum(10L);
					level = "LLT";
				}
				//row = worksheet.createRow(rowCount);
				dto.setLevel(level);
				dto.setTerm(term);
				dto.setCode(codeTerm);
				dto.setCategory(relation.getTermCategory() != null ? relation.getTermCategory() : "");
                                dto.setCategory2(relation.getTermCategory2() != null ? relation.getTermCategory2() : "");
				dto.setScope(relation.getTermScope() != null ? returnScopeValue(relation.getTermScope()) : "");
				dto.setWeight(relation.getTermWeight() != null ? relation.getTermWeight().toString() : "");
				dtos.add(dto);
			}
		}
		List<CmqBase190> childCmqs = findChildCmqsByParentCode(details.getCode());
		if((null != childCmqs) && (childCmqs.size() > 0)) {
			for (CmqBase190 childCmq : childCmqs) {
				ReportLineDataDto dto = new ReportLineDataDto();
				level = "PRO";
				term = childCmq.getCmqName();
				codeTerm = childCmq.getCmqCode() != null ? childCmq.getCmqCode() + "" : "";
				
				dto.setLevel("PRO");
				dto.setTerm(term);
				dto.setCode(codeTerm);
				dtos.add(dto);
			}
		}

		dtos.sort(LEVELNUM_TERM_REPORT_LINE_DTO_COMPARATOR);
		
		for(ReportLineDataDto dto : dtos) {
			row = worksheet.createRow(rowCount);
			if(dto.getCategory() == null && dto.getCategory2() == null) {
				buildShortCells(dto, cell, row);
			} else {
				buildCells(dto, cell, row, systemConfigProperties);
			}
			rowCount++;
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

	private void buildShortCells(ReportLineDataDto dto, XSSFCell cell, XSSFRow row) {
		// Cell 0
		cell = row.createCell(0);
		cell.setCellValue(dto.getTerm());

		// Cell 1
		cell = row.createCell(1);
		cell.setCellValue(dto.getCode());

		// Cell 2
		cell = row.createCell(2);
		cell.setCellValue(dto.getLevel());
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


	private void buildCells(ReportLineDataDto dto, XSSFCell cell, XSSFRow row, SystemConfigProperties systemConfigProperties) {
                
            int cellCount = 0;
		// Cell 0
		cell = row.createCell(cellCount);
		cell.setCellValue(dto.getTerm());
	
		// Cell 1
                cellCount++;
		cell = row.createCell(cellCount);
		cell.setCellValue(dto.getCode());

		// Cell 2
                cellCount++;
		cell = row.createCell(cellCount);
		cell.setCellValue(dto.getLevel());

		// Cell 3
                if(systemConfigProperties.isDisplayCategory()) {
                    cellCount++;
                    cell = row.createCell(cellCount);
                    cell.setCellValue(dto.getCategory());
                }
                
                // Cell 4
                if(systemConfigProperties.isDisplayCategory2()) {
                    cellCount++;
                    cell = row.createCell(cellCount);
                    cell.setCellValue(dto.getCategory2());
                }

		// Cell 5
                if(systemConfigProperties.isDisplayWeight()) {
                    cellCount++;
                    cell = row.createCell(cellCount);
                    cell.setCellValue(dto.getWeight());
                }

		// Cell 6
                if(systemConfigProperties.isDisplayScope()) {
                    cellCount++;
                    cell = row.createCell(cellCount);
                    cell.setCellValue(dto.getScope());
                }
		
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
	public StreamedContent generateMQReport(ListDetailsFormVM details, ListNotesFormVM notes, String dictionaryVersion, TreeNode relationsRoot, boolean filterLlts, SystemConfigProperties systemConfigProperties) {
		XSSFWorkbook workbook = new XSSFWorkbook();
		
		DateFormat dateTimeFormat = new SimpleDateFormat("dd-MMM-yyyy:hh:mm:ss a z");
		
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
		/**
		 * 
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
		cell.setCellValue("MedDRA Dictionary Version: " + refCodeListService.interpretDictionaryVersion(dictionaryVersion));

		rowCount++;
		row = worksheet.createRow(rowCount);
		cell = row.createCell(0);
		cell.setCellValue("List Name: " + details.getName());
		rowCount++;
//		row = worksheet.createRow(rowCount);
//		cell = row.createCell(0);
//		cell.setCellValue("Drug Program: " + refCodeListService.findCodeByInternalCode(CqtConstants.CODE_LIST_TYPE_PROGRAM, details.getDrugProgram()));
//		rowCount++;
//		row = worksheet.createRow(rowCount);
//		cell = row.createCell(0);
//		cell.setCellValue("Protocol: " + refCodeListService.findCodeByInternalCode(CqtConstants.CODE_LIST_TYPE_PROTOCOL, details.getProtocol()));
//		rowCount++;
//		row = worksheet.createRow(rowCount);
//		cell = row.createCell(0);
//		cell.setCellValue("Product List: " + refCodeListService.interpretProductCodesToValuesLabel(details.getProducts()));
//		rowCount++;
//		row = worksheet.createRow(rowCount);
//		cell = row.createCell(0);
//		if (details.getDesignee() != null)
//			cell.setCellValue("Designee: " + details.getDesignee());
//		else
//			cell.setCellValue("Designee: ");
//		rowCount++;
//		row = worksheet.createRow(rowCount);
//		cell = row.createCell(0);
//		if (details.getDesigneeTwo() != null)
//			cell.setCellValue("Designee 2: " + details.getDesigneeTwo());
//		else
//			cell.setCellValue("Designee 2: ");
//		rowCount++;
//		row = worksheet.createRow(rowCount);
//		cell = row.createCell(0);
//		if (details.getDesigneeThree() != null)
//			cell.setCellValue("Designee 3: " + details.getDesigneeThree());
//		else
//			cell.setCellValue("Designee 3: ");
//		rowCount++;
//		row = worksheet.createRow(rowCount);
//		cell = row.createCell(0);
//		cell.setCellValue("Level: " + details.getLevel());
//		rowCount++;
		row = worksheet.createRow(rowCount);
		cell = row.createCell(0);
		cell.setCellValue("Code: " + details.getCode());
		rowCount++;
//		row = worksheet.createRow(rowCount);
//		cell = row.createCell(0);
//		cell.setCellValue("Algorithm: " + details.getAlgorithm());
//		rowCount++;
		row = worksheet.createRow(rowCount);
		cell = row.createCell(0);
		cell.setCellValue("Level Extension: " + details.getExtension());
		rowCount++;
//		row = worksheet.createRow(rowCount);
//		cell = row.createCell(0);
//		cell.setCellValue("Group: " + details.getGroup());
//		rowCount++;
		row = worksheet.createRow(rowCount);
		cell = row.createCell(0);
		cell.setCellValue("Status: " + details.getStatus());
		rowCount++;
//		row = worksheet.createRow(rowCount);
//		cell = row.createCell(0);
//		cell.setCellValue("State: " + details.getState());
//		rowCount++;
//		row = worksheet.createRow(rowCount);
//		cell = row.createCell(0);
//		String createdBy = "";
//		if (details.getCreatedBy() != null)
//			createdBy = details.getCreatedBy();
//		cell.setCellValue("Initial Creation By: " + createdBy);
//		rowCount++;
//		row = worksheet.createRow(rowCount);
//		cell = row.createCell(0);
//		if (details.getCreationDate() != null) {
//			
//			cell.setCellValue("Initial Creation Date: " + dateTimeFormat.format(details.getCreationDate()));
//		}
//		else
//			cell.setCellValue("Initial Creation Date: ");
//		
// 		
//		rowCount++;
//		row = worksheet.createRow(rowCount);
//		cell = row.createCell(0);
//		String modifiedBy = "";
//		if (details.getLastModifiedBy() != null)
//			modifiedBy = details.getLastModifiedBy();
//		cell.setCellValue("Last Modified By: " + modifiedBy);
//		
//		rowCount++;
//		row = worksheet.createRow(rowCount);
//		cell = row.createCell(0);
//		if (details.getLastModifiedDate() != null) {
//	 		cell.setCellValue("Last Modification Date: " + dateTimeFormat.format(details.getLastModifiedDate()));
//		}
//		else
//			cell.setCellValue("Last Modification Date: ");
//		
//		rowCount++;
		row = worksheet.createRow(rowCount);
		cell = row.createCell(0);
		
		cell.setCellValue("Report Date/Time: " + dateTimeFormat.format(new Date()));
		
		rowCount += 2;
		row = worksheet.createRow(rowCount);
                
                int cellCount=0;
		cell = row.createCell(cellCount);
		cell.setCellValue("Term");
		setCellStyleColumn(workbook, cell);
                
                cellCount++;
		cell = row.createCell(cellCount);
		cell.setCellValue("Code");
		setCellStyleColumn(workbook, cell);
                
                cellCount++;
		cell = row.createCell(cellCount);
		cell.setCellValue("Level");
		setCellStyleColumn(workbook, cell);
                
                if(systemConfigProperties.isDisplayCategory()) {
                    cellCount++;
                    cell = row.createCell(cellCount);
                    cell.setCellValue("Category");
                    setCellStyleColumn(workbook, cell);
                }
                
                if(systemConfigProperties.isDisplayCategory2()) {
                    cellCount++;
                    cell = row.createCell(cellCount);
                    cell.setCellValue("Category2");
                    setCellStyleColumn(workbook, cell);
                }
                
                if(systemConfigProperties.isDisplayWeight()) {
                    cellCount++;
                    cell = row.createCell(cellCount);
                    cell.setCellValue("Weight");
                    setCellStyleColumn(workbook, cell);
                }
                
                if(systemConfigProperties.isDisplayScope()) {
                    cellCount++;
                    cell = row.createCell(cellCount);
                    cell.setCellValue("Scope");
                    setCellStyleColumn(workbook, cell);
                }
                
		cell = row.createCell(cellCount + 1);
		//cell.setCellValue("PT Status");
		//setCellStyleColumn(workbook, cell);
		rowCount++;

		// Retrieval of relations - Loop
		List<CmqRelation190> relations = cmqRelationService.findByCmqCode(details.getCode());
		List<Future<MQReportRelationsWorkerDTO>> futures = new ArrayList<>();
		int workerId = 1;
		ExecutorService executorService = Executors.newFixedThreadPool(8);
		List<TreeNode> childTreeNodes = relationsRoot.getChildren();
		Map<String,String> relationScopeMap = new HashMap<>();
		
		CmqBase190 cmq = findByCode(details.getCode());
		for(TreeNode childTreeNode: childTreeNodes) {
			updateRelationScopeMap(relationScopeMap,childTreeNode);
		}
		
		if (relations != null) {
			for (CmqRelation190 relation : relations) {
				if(relation.getSmqCode() != null && (relation.getSocCode() != null || relation.getHlgtCode() != null || relation.getHltCode() != null 
						|| relation.getLltCode() != null || relation.getPtCode() != null)) {
					relation.setSmqCode(null);
				}
				MQReportRelationsWorker task = new MQReportRelationsWorker(workerId++, relation,relationScopeMap,filterLlts,cmq.getDictionaryVersion());
				futures.add(executorService.submit(task));
			}
		}
		
		LOG.info("Submitted all MQReportRelationsWorker for relations.");
		//now get the futures and process them.
		int iterator = 0;
		List<ReportLineDataDto> parents = new ArrayList<>();
		for (Future<MQReportRelationsWorkerDTO> future : futures) {
			try {
				MQReportRelationsWorkerDTO relationsWorkerDTO = future.get();
				if(relationsWorkerDTO.isSuccess()) {
					Map<Integer, ReportLineDataDto> mapReportData = relationsWorkerDTO.getMapReport();
					
					if(relations.get(iterator).getTermCategory() != null && mapReportData.get(0) != null) {
						mapReportData.get(0).setCategory(relations.get(iterator).getTermCategory());
					}
                                        
                                        if(relations.get(iterator).getTermCategory2() != null && mapReportData.get(0) != null) {
						mapReportData.get(0).setCategory2(relations.get(iterator).getTermCategory2());
					}
					
					if(relations.get(iterator).getTermWeight() != null && mapReportData.get(0) != null) {
						mapReportData.get(0).setWeight(relations.get(iterator).getTermWeight()+"");
					}
					
					if(relations.get(iterator).getTermScope() != null && mapReportData.get(0) != null) {
						mapReportData.get(0).setScope(relations.get(iterator).getTermScope());
					}
					
					parents.addAll(mapReportData.values());
					LOG.info("Adding parents from reportData {}", mapReportData.values());
					mapReportData.clear();
				} else {
					LOG.info("Got false status for success in worker {}", relationsWorkerDTO.getWorkerName());
				}
				iterator++;
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

				ReportLineDataDto parent = new ReportLineDataDto(getLevelNumFromLevel(level), level, codeTerm, term, "");
				parents.add(parent);
				LOG.info("Adding parent from children {}", parent);
				mapReport.clear();
				
				/**
				 * Other Relations
				 */
				List<CmqRelation190> relationsPro = cmqRelationService.findByCmqCode(childCmq.getCmqCode());
				futures.clear();
				if (relations != null) {
					ArrayList<Boolean> addedFromSmq = new ArrayList<Boolean>();
					for (CmqRelation190 relation : relationsPro) {
						if(relation.getSmqCode() != null && (relation.getSocCode() != null || relation.getHlgtCode() != null || relation.getHltCode() != null 
								|| relation.getLltCode() != null || relation.getPtCode() != null)) {
							relation.setSmqCode(null);
							addedFromSmq.add(true);
						} else {
							addedFromSmq.add(false);
						}
						MQReportRelationsWorker task = new MQReportRelationsWorker(workerId++, relation,relationScopeMap, filterLlts,childCmq.getDictionaryVersion());

						futures.add(executorService.submit(task));
					}
					
					LOG.info("Submitted all MQReportRelationsWorker for relations of child {}.", childCmq.getCmqCode());
					//now get the futures and process them.
					int addedFromSmqCounter = 0;
					for (Future<MQReportRelationsWorkerDTO> future : futures) {
						try {
							MQReportRelationsWorkerDTO relationsWorkerDTO = future.get();
							if(relationsWorkerDTO.isSuccess()) {
								Map<Integer, ReportLineDataDto> mapReportData = relationsWorkerDTO.getMapReport();
								if(addedFromSmq.get(addedFromSmqCounter)) {
									mapReportData.keySet().removeIf(key -> key != 0);
									if(relationsPro.get(addedFromSmqCounter).getTermScope() != null && mapReportData.get(0) != null) {
										mapReportData.get(0).setScope(relationsPro.get(addedFromSmqCounter).getTermScope());
									}
									if(relationsPro.get(addedFromSmqCounter).getTermWeight() != null && mapReportData.get(0) != null) {
										mapReportData.get(0).setWeight(relationsPro.get(addedFromSmqCounter).getTermWeight().toString());
									}
									if(relationsPro.get(addedFromSmqCounter).getTermCategory() != null && mapReportData.get(0) != null){
										mapReportData.get(0).setCategory(relationsPro.get(addedFromSmqCounter).getTermCategory());
									}
                                                                        if(relationsPro.get(addedFromSmqCounter).getTermCategory2() != null && mapReportData.get(0) != null){
										mapReportData.get(0).setCategory2(relationsPro.get(addedFromSmqCounter).getTermCategory2());
									}
								}
								parent.getChildren().addAll(mapReportData.values());
								//LOG.info("Adding children to parent {}", parent);
								mapReportData.clear();
								addedFromSmqCounter++;
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
		
		parents.sort(LEVELNUM_TERM_REPORT_LINE_DTO_COMPARATOR);
		parents.forEach(parent -> parent.getChildren().sort(LEVELNUM_REPORT_LINE_DTO_COMPARATOR));
		
		rowCount = fillReport(parents, cell, row, rowCount, worksheet, systemConfigProperties);

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

	private Long getLevelNumFromLevel(String level) {
		switch(level) {
			case "SMQ1": return 1L;
			case "SMQ2": return 2L;
			case "SMQ3": return 3L;
			case "SMQ4": return 4L;
			case "SMQ5": return 5L;
			case "SOC":  return 6L;
			case "HLGT": return 7L;
			case "HLT": return 8L;
			case "PT": return 9L;
			case "LLT": return 10L; 
			case "PRO": return 11L;
			default: return 12L;
		}
	}

	/**
	 * MQ Report.
	 */
	@Override
	public StreamedContent generateUniquePTReport(ListDetailsFormVM details, ListNotesFormVM notes, String dictionaryVersion, TreeNode relationsRoot, boolean filterLlts) {
		XSSFWorkbook workbook = new XSSFWorkbook();

		DateFormat dateTimeFormat = new SimpleDateFormat("dd-MMM-yyyy:hh:mm:ss a z");

		XSSFSheet worksheet = null;

		worksheet = workbook.createSheet("Unique PT Report");
		XSSFRow row = null;
		int rowCount = 6;

		try {
			insertExporLogoImage(worksheet, workbook);
		} catch (IOException e1) {
			e1.printStackTrace();
		}

		/**
		 * 
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
		cell.setCellValue("MedDRA Dictionary Version: " + refCodeListService.interpretDictionaryVersion(dictionaryVersion));

		rowCount++;
		row = worksheet.createRow(rowCount);
		cell = row.createCell(0);
		cell.setCellValue("List Name: " + details.getName());
		rowCount++;
		row = worksheet.createRow(rowCount);
		cell = row.createCell(0);
		cell.setCellValue("Code: " + details.getCode());
		rowCount++;
		row = worksheet.createRow(rowCount);
		cell = row.createCell(0);
		cell.setCellValue("Level Extension: " + details.getExtension());
		rowCount++;
                row = worksheet.createRow(rowCount);
		cell = row.createCell(0);
		cell.setCellValue("Status: " + details.getStatus());
		rowCount++;
		row = worksheet.createRow(rowCount);
		cell = row.createCell(0);

		cell.setCellValue("Report Date/Time: " + dateTimeFormat.format(new Date()));
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
		rowCount++;

		// Retrieval of relations - Loop
		List<CmqRelation190> relations = cmqRelationService.findByCmqCode(details.getCode());
		List<Future<MQReportRelationsWorkerDTO>> futures = new ArrayList<>();
		int workerId = 1;
		ExecutorService executorService = Executors.newFixedThreadPool(8);
		List<TreeNode> childTreeNodes = relationsRoot.getChildren();
		Map<String,String> relationScopeMap = new HashMap<>();

		CmqBase190 cmq = findByCode(details.getCode());
		for(TreeNode childTreeNode: childTreeNodes) {
			updateRelationScopeMap(relationScopeMap,childTreeNode);
		}

		if (relations != null) {
			for (CmqRelation190 relation : relations) {
				if(relation.getSmqCode() != null && (relation.getSocCode() != null || relation.getHlgtCode() != null || relation.getHltCode() != null
						|| relation.getLltCode() != null || relation.getPtCode() != null)) {
					relation.setSmqCode(null);
				}
				UniquePTReportRelationsWorker task = new UniquePTReportRelationsWorker(workerId++, relation,relationScopeMap,filterLlts,cmq.getDictionaryVersion(), smqBaseService, meddraDictService);
				futures.add(executorService.submit(task));
			}
		}

		LOG.info("Submitted all UniquePTReportRelationsWorker for relations.");
		//now get the futures and process them.
		int iterator = 0;
		Set<ReportLineDataDto> elements = new TreeSet<>(Comparator.comparing(o -> o.getTerm().toLowerCase()));
		for (Future<MQReportRelationsWorkerDTO> future : futures) {
			try {
				MQReportRelationsWorkerDTO relationsWorkerDTO = future.get();
				if(relationsWorkerDTO.isSuccess()) {
					Map<Integer, ReportLineDataDto> mapReportData = relationsWorkerDTO.getMapReport();

					if(relations.get(iterator).getTermCategory() != null && mapReportData.get(0) != null) {
						mapReportData.get(0).setCategory(relations.get(iterator).getTermCategory());
					}
                                        
                                        if(relations.get(iterator).getTermCategory2() != null && mapReportData.get(0) != null) {
						mapReportData.get(0).setCategory2(relations.get(iterator).getTermCategory2());
					}

					if(relations.get(iterator).getTermWeight() != null && mapReportData.get(0) != null) {
						mapReportData.get(0).setWeight(relations.get(iterator).getTermWeight()+"");
					}

					if(relations.get(iterator).getTermScope() != null && mapReportData.get(0) != null) {
						mapReportData.get(0).setScope(relations.get(iterator).getTermScope());
					}

					elements.addAll(mapReportData.values());
					mapReportData.clear();
				} else {
					LOG.info("Got false status for success in worker {}", relationsWorkerDTO.getWorkerName());
				}
				iterator++;
			} catch (InterruptedException | ExecutionException e) {
				LOG.error("Exception while reading MQReportRelationsWorkerDTO", e);
			}

		}
		LOG.info("Processing children now.");
		//now child relations
		List<CmqBase190> childCmqs = findChildCmqsByParentCode(details.getCode());
		if((null != childCmqs) && (childCmqs.size() > 0)) {
			LOG.info("Found child cmqs of size " + childCmqs.size());
			for (CmqBase190 childCmq : childCmqs) {
				List<CmqRelation190> relationsPro = cmqRelationService.findByCmqCode(childCmq.getCmqCode());
				futures.clear();
				if (relations != null) {
					ArrayList<Boolean> addedFromSmq = new ArrayList<Boolean>();
					for (CmqRelation190 relation : relationsPro) {
						if(relation.getSmqCode() != null && (relation.getSocCode() != null || relation.getHlgtCode() != null || relation.getHltCode() != null
								|| relation.getLltCode() != null || relation.getPtCode() != null)) {
							relation.setSmqCode(null);
							addedFromSmq.add(true);
						} else {
							addedFromSmq.add(false);
						}
						UniquePTReportRelationsWorker task = new UniquePTReportRelationsWorker(workerId++, relation,relationScopeMap, filterLlts,childCmq.getDictionaryVersion(), smqBaseService, meddraDictService);
						futures.add(executorService.submit(task));
					}

					LOG.info("Submitted all MQReportRelationsWorker for relations of child {}.", childCmq.getCmqCode());
					//now get the futures and process them.
					int addedFromSmqCounter = 0;
					for (Future<MQReportRelationsWorkerDTO> future : futures) {
						try {
							MQReportRelationsWorkerDTO relationsWorkerDTO = future.get();
							if(relationsWorkerDTO.isSuccess()) {
								Map<Integer, ReportLineDataDto> mapReportData = relationsWorkerDTO.getMapReport();
								if(addedFromSmq.get(addedFromSmqCounter)) {
									mapReportData.keySet().removeIf(key -> key != 0);
									if(relationsPro.get(addedFromSmqCounter).getTermScope() != null && mapReportData.get(0) != null) {
										mapReportData.get(0).setScope(relationsPro.get(addedFromSmqCounter).getTermScope());
									}
									if(relationsPro.get(addedFromSmqCounter).getTermWeight() != null && mapReportData.get(0) != null) {
										mapReportData.get(0).setWeight(relationsPro.get(addedFromSmqCounter).getTermWeight().toString());
									}
									if(relationsPro.get(addedFromSmqCounter).getTermCategory() != null && mapReportData.get(0) != null){
										mapReportData.get(0).setCategory(relationsPro.get(addedFromSmqCounter).getTermCategory());
									}
                                                                        if(relationsPro.get(addedFromSmqCounter).getTermCategory2() != null && mapReportData.get(0) != null){
										mapReportData.get(0).setCategory2(relationsPro.get(addedFromSmqCounter).getTermCategory2());
									}
								}
								elements.addAll(mapReportData.values());
								mapReportData.clear();
								addedFromSmqCounter++;
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

		fillUniquePTReport(elements, cell, row, rowCount, worksheet);

		StreamedContent content = null;
		try {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			workbook.write(baos);
			ByteArrayOutputStream zipBaos = new ByteArrayOutputStream();
			ZipOutputStream zos = new ZipOutputStream(zipBaos);
			ZipEntry entry = new ZipEntry("Relations_Unique_PT_Report_" + details.getName() + ".xlsx");
			zos.putNextEntry(entry);
			baos.writeTo(zos);
			zos.flush();
			zos.close();
			ByteArrayInputStream bais = new ByteArrayInputStream(zipBaos.toByteArray());
			content = new DefaultStreamedContent(
					bais,
					"application/zip",
					"Relations_Unique_PT_Report_" + details.getName()
							+ ".zip", Charsets.UTF_8.name());
		} catch (IOException e) {
			e.printStackTrace();
		}
		LOG.info("Finished unique PT report generation.");
		return content;
	}
	
	/**
	 * Excel Report.
	 */
	@Override
	public StreamedContent generateExcel(List<CmqBase190> datas, String module, String user) {
		XSSFWorkbook workbook = new XSSFWorkbook();
		XSSFSheet worksheet = null;
		DateFormat dateTimeFormat = new SimpleDateFormat("dd-MMM-yyyy:hh:mm:ss a z");

		worksheet = workbook.createSheet(module + "_ListSearch");
		XSSFRow row = null;
		int rowCount = 6;

		try {
			insertExporLogoImage(worksheet, workbook);
		} catch (IOException e1) {
			e1.printStackTrace();
		}

		/**
		 * 
		 */
		row = worksheet.createRow(rowCount);
		XSSFCell cell = row.createCell(0);

		// User name
		rowCount += 2;
		row = worksheet.createRow(rowCount);
		cell = row.createCell(0);
		cell.setCellValue("User name: " + user);

		rowCount++;
		//Calendar cal = Calendar.getInstance();
		//.setTime(new Date());
		/*String date = getWeekDay(cal.get(Calendar.DAY_OF_WEEK)) + ", " + 
				getTwoDigits(cal.get(Calendar.DAY_OF_MONTH) + 1) + "-" + 
				getMonth(cal.get(Calendar.MONTH)) + "-" + 
				cal.get(Calendar.YEAR) + " : " + 
				getTwoDigits(cal.get(Calendar.HOUR)) + ":" + 
				getTwoDigits(cal.get(Calendar.MINUTE)) + ":" + 
				getTwoDigits(cal.get(Calendar.SECOND)) + " EST";*/
		row = worksheet.createRow(rowCount);
		cell = row.createCell(0);
		cell.setCellValue("Report Date/Time: " + dateTimeFormat.format(new Date()));
		
		
		cell = row.createCell(1);

		//Columns
		rowCount += 2;
		row = worksheet.createRow(rowCount);
		
		cell = row.createCell(0);
		cell.setCellValue("List Name");
		setCellStyleColumn(workbook, cell);
		cell = row.createCell(1);
		cell.setCellValue("Extension");
		setCellStyleColumn(workbook, cell);
		cell = row.createCell(2);
		cell.setCellValue("Level");
		setCellStyleColumn(workbook, cell);
		cell = row.createCell(3);
		cell.setCellValue("Status");
		setCellStyleColumn(workbook, cell);
		cell = row.createCell(4);
		cell.setCellValue("State");
		setCellStyleColumn(workbook, cell);
		cell = row.createCell(5);
		cell.setCellValue("Code");
		setCellStyleColumn(workbook, cell);
		cell = row.createCell(6);
		cell.setCellValue("Drug Program");
		setCellStyleColumn(workbook, cell);
		cell = row.createCell(7);
		cell.setCellValue("Protocol");
		setCellStyleColumn(workbook, cell);
		cell = row.createCell(8);
		cell.setCellValue("Product");
		setCellStyleColumn(workbook, cell);
		cell = row.createCell(9);
		cell.setCellValue("Group");
		setCellStyleColumn(workbook, cell);
		cell = row.createCell(10);
		cell.setCellValue("Created By");
		setCellStyleColumn(workbook, cell);
		
		
		rowCount++;
 
		if (datas != null)
 		for (CmqBase190 cmq : datas) {
 			row = worksheet.createRow(rowCount);
 			// Cell 0
 			cell = row.createCell(0);
 			cell.setCellValue(cmq.getCmqName());

 			// Cell 1
 			cell = row.createCell(1);
 			cell.setCellValue(refCodeListService.interpretInternalCodeToValue("LIST_EXTENSION_TYPES", cmq.getCmqTypeCd()));

 			// Cell 2
 			cell = row.createCell(2);
 			cell.setCellValue(cmq.getCmqLevel());
 			
 			// Cell 3
 			cell = row.createCell(3);
 			String status = "";
 			if (cmq.getCmqStatus().equals("P"))
 				status = "PENDING";
 			if (cmq.getCmqStatus().equals("A"))
 				status = "ACTIVE";
 			if (cmq.getCmqStatus().equals("I"))
 				status = "INACTIVE";
 			cell.setCellValue(status);

 			// Cell 4
 			cell = row.createCell(4);
 			cell.setCellValue(cmq.getCmqState());

 			// Cell 5
 			cell = row.createCell(5);
 			cell.setCellValue(cmq.getCmqCode());
 			
 			// Cell 6
 			cell = row.createCell(6);
 			cell.setCellValue(refCodeListService.interpretInternalCodeToValue("PROGRAM", cmq.getCmqProgramCd()));

 			// Cell 7
 			cell = row.createCell(7);
 			cell.setCellValue(refCodeListService.interpretInternalCodeToValue("PROTOCOL", cmq.getCmqProtocolCd()));

 			// Cell 8
 			cell = row.createCell(8);
 			cell.setCellValue(refCodeListService.convertProductCodesToValuesLabel(cmq.getProductsList()));
 			
 			// Cell 9
 			cell = row.createCell(9);
 			cell.setCellValue(cmq.getCmqGroup());

 			// Cell 10
 			cell = row.createCell(10);
 			cell.setCellValue(cmq.getCreatedByLabel());
 			
 			rowCount++;
		}
  

		StreamedContent content = null;
		try {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			workbook.write(baos);
			byte[] xls = baos.toByteArray();
			ByteArrayInputStream bais = new ByteArrayInputStream(xls);
			content = new DefaultStreamedContent(
					bais,
					"application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
					module.toLowerCase() + "_ListSearch.xlsx");
		} catch (IOException e) {
			e.printStackTrace();
		}

		return content;
	}
	
	private void updateRelationScopeMap(Map<String, String> relationScopeMap, TreeNode relationsRoot) {
		if(null!=relationsRoot) {
			if(relationsRoot.getChildCount() > 0) {
				List<TreeNode> childTreeNodes  = relationsRoot.getChildren();
				for(TreeNode childTreeNode: childTreeNodes) {
					updateRelationScopeMap(relationScopeMap,childTreeNode);
				}
			}
			
			HierarchyNode hierarchyNode = (HierarchyNode) relationsRoot.getData();
			
			if (null != hierarchyNode && null != hierarchyNode.getCode()) {
				relationScopeMap.put(hierarchyNode.getCode(), hierarchyNode.getScope());
			}
			
		}
		
	}

	private class MQReportRelationsWorker implements Callable<MQReportRelationsWorkerDTO> {
		private CmqRelation190 relation;
		private String level = "", term = "", codeTerm = "", workerName = null,dictionaryVersion = "";
		Map<String,String> relationScopeMap;
		private boolean filterLltFlag;
		public MQReportRelationsWorker(int workerId, CmqRelation190 relation, Map<String,String> relationScopeMap, boolean filterLltFlag, String dictionaryVersion) {
			this.workerName = "MQReportRelationsWorker_" + workerId;
			this.relation = relation;
			this.relationScopeMap = relationScopeMap;
			this.filterLltFlag = filterLltFlag;
			this.dictionaryVersion = dictionaryVersion;
		}
		
		@Override
		public MQReportRelationsWorkerDTO call() throws Exception {
			int cpt = 0;
			MQReportRelationsWorkerDTO relationsWorkerDTO = new MQReportRelationsWorkerDTO();
			relationsWorkerDTO.setWorkerName(workerName);
			LOG.info("In {} Starting Callable.", this.workerName);
			try {
				if (relation.getSmqCode() != null) {
					String selectedScope = relationScopeMap.get(String.valueOf(relation.getSmqCode()));
					if(StringUtils.isEmpty(selectedScope)) {
						selectedScope = relation.getTermScope();
					}
 					List<Long> smqChildCodeList = new ArrayList<>();
					smqChildCodeList.add(relation.getSmqCode());

					SmqBase190 smqSearched = smqBaseService.findByCode(relation.getSmqCode());
					ReportLineDataDto parent = new ReportLineDataDto();
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
								
								//if ((smq.getSmqLevel() != 5) || (!filterLltFlag && smq.getSmqLevel() == 5)) {
									parent.setLevelNum(smq.getSmqLevel() == null ? null : smq.getSmqLevel().longValue());
									parent.setLevel(level);
									parent.setCode(smq.getSmqCode() + "");
									parent.setTerm(smq.getSmqName());
									parent.setDots("");
									parent.setScope(selectedScope);
									parent.setWeight((relation.getTermWeight() != null ? relation.getTermWeight() + "" : ""));
									parent.setCategory(relation.getTermCategory());
                                                                        parent.setCategory2(relation.getTermCategory2());
									parent.setImpact("");
									parent.setStatus("");
									relationsWorkerDTO.addToMapReport(cpt++, parent);
									
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
											
											if ((smqC.getSmqLevel() != 5) || (!filterLltFlag && smqC.getSmqLevel() == 5)) {
												parent.getChildren().add(new ReportLineDataDto(level, smqC.getSmqCode() + "", smqC.getSmqName(), "......"));
												
												if (level.equals("SMQ1")) {						
													smqSearched = smqBaseService.findByCode(smqC.getSmqCode());
													if (smqSearched != null) {
														//List<SmqRelation190> list = smqBaseService.findSmqRelationsForSmqCode(smqSearched.getSmqCode());
														List<SmqRelation190> list = smqBaseService.findSmqRelationsForSmqCodeAndScope(smqSearched.getSmqCode(), selectedScope);
														if (list != null) {
															for (SmqRelation190 smq3 : list) {
																parent.getChildren().add(new ReportLineDataDto("PT", smq3.getPtCode() + "", smq3.getPtName(), ".............", smq3.getPtTermScope() + "", smq3.getPtTermWeight() + "", smq3.getPtTermCategory(), "", smq3.getPtTermStatus()));
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
													
													if ((smqSearched.getSmqLevel() != 5) || (!filterLltFlag && smqSearched.getSmqLevel() == 5)) {
														List<SmqBase190> smqChildren = smqBaseService.findChildSmqByParentSmqCodes(codes);
														if (smqChildren != null) {
															for (SmqBase190 child : smqChildren) {
																parent.getChildren().add(new ReportLineDataDto(levelS, child.getSmqCode() + "", child.getSmqName(), "............."));
																
																codes = new ArrayList<>();
																codes.add(child.getSmqCode());
																
																//smqChildren = smqBaseService.findChildSmqByParentSmqCodes(codes);
																
																
																smqSearched = smqBaseService.findByCode(child.getSmqCode());
																if (smqSearched != null) {
																	//List<SmqRelation190> list = smqBaseService.findSmqRelationsForSmqCode(smqSearched.getSmqCode());
																	List<SmqRelation190> list = smqBaseService.findSmqRelationsForSmqCodeAndScope(smqSearched.getSmqCode(), selectedScope);
																	if (list != null) {
																		for (SmqRelation190 smq3 : list) {
																			parent.getChildren().add(new ReportLineDataDto("PT", smq3.getPtCode() + "", smq3.getPtName(), "....................", smq3.getPtTermScope() + "", smq3.getPtTermWeight() + "", smq3.getPtTermCategory(), "", smq3.getPtTermStatus()));
																		}
																	}
																}
															}
														}
													}
												}
												
												if (level.equals("SMQ2")) {						
													smqSearched = smqBaseService.findByCode(smqC.getSmqCode());
													if (smqSearched != null) {
														//List<SmqRelation190> list = smqBaseService.findSmqRelationsForSmqCode(smqSearched.getSmqCode());
														List<SmqRelation190> list = smqBaseService.findSmqRelationsForSmqCodeAndScope(smqSearched.getSmqCode(), selectedScope);
														if (list != null) {
															for (SmqRelation190 smq3 : list) {
																parent.getChildren().add(new ReportLineDataDto("PT", smq3.getPtCode() + "", smq3.getPtName(), ".............", smq3.getPtTermScope() + "", smq3.getPtTermWeight() + "", smq3.getPtTermCategory(), "", smq3.getPtTermStatus()));
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
													
													if ((smqSearched.getSmqLevel() != 5) || (!filterLltFlag && smqSearched.getSmqLevel() == 5)) {
														List<SmqBase190> smqChildren = smqBaseService.findChildSmqByParentSmqCodes(codes);
														if (smqChildren != null) {
															for (SmqBase190 child : smqChildren) {
																parent.getChildren().add(new ReportLineDataDto(levelS, child.getSmqCode() + "", child.getSmqName(), "............."));																
																smqSearched = smqBaseService.findByCode(child.getSmqCode());
																if (smqSearched != null) {
																	//List<SmqRelation190> list = smqBaseService.findSmqRelationsForSmqCode(smqSearched.getSmqCode());
																	List<SmqRelation190> list = smqBaseService.findSmqRelationsForSmqCodeAndScope(smqSearched.getSmqCode(), selectedScope);
																	if (list != null) {
																		for (SmqRelation190 smq3 : list) {
																			parent.getChildren().add(new ReportLineDataDto("PT", smq3.getPtCode() + "", smq3.getPtName(), "....................", smq3.getPtTermScope() + "", smq3.getPtTermWeight() + "", smq3.getPtTermCategory(), "", smq3.getPtTermStatus()));
																		}
																	}
																}
															}
														}
													}
												}
												
												if (level.equals("SMQ3")) {
													smqSearched = smqBaseService.findByCode(smqC.getSmqCode());
													if (smqSearched != null) {
														//List<SmqRelation190> list = smqBaseService.findSmqRelationsForSmqCode(smqSearched.getSmqCode());
														List<SmqRelation190> list = smqBaseService.findSmqRelationsForSmqCodeAndScope(smqSearched.getSmqCode(), selectedScope);
														if (list != null) {
															for (SmqRelation190 smq3 : list) {
																parent.getChildren().add(new ReportLineDataDto("PT", smq3.getPtCode() + "", smq3.getPtName(), ".............", smq3.getPtTermScope() + "", smq3.getPtTermWeight() + "", smq3.getPtTermCategory(), "", smq3.getPtTermStatus()));
															}
														}
													}
												}
											}
										}
									}
								//}
							}
						}
					}
					
					//Relations for SMQs
					//Long smqBaseChildrenCount = smqBaseService.findChildSmqCountByParentSmqCode(relation.getSmqCode());
					//smqBaseChildrenCount = smqBaseService.findSmqRelationsCountForSmqCode(relation.getSmqCode());

 					//List<SmqRelation190> childSmqs =  smqBaseService.findSmqRelationsForSmqCode(relation.getSmqCode());
					List<SmqRelation190> childSmqs =  smqBaseService.findSmqRelationsForSmqCodeAndScope(relation.getSmqCode(), selectedScope);

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

							if ((childSmq.getSmqLevel() != 5) || (!filterLltFlag && childSmq.getSmqLevel() == 5)) {
								parent.getChildren().add(new ReportLineDataDto(level, childSmq.getPtCode() + "", childSmq.getPtName(), ".......", childSmq.getPtTermScope() + "", childSmq.getPtTermWeight() + "", childSmq.getPtTermCategory(), "", childSmq.getPtTermStatus()));

								List<Long> codes = new ArrayList<>();
								codes.add(childSmq.getSmqCode());
								
								//List<SmqBase190> smqs = smqBaseService.findChildSmqByParentSmqCodes(codes);
								
								smqSearched = smqBaseService.findByCode(Long.parseLong(childSmq.getPtCode() + ""));
								if (smqSearched != null) {
									//List<SmqRelation190> list = smqBaseService.findSmqRelationsForSmqCode(smqSearched.getSmqCode());
									List<SmqRelation190> list = smqBaseService.findSmqRelationsForSmqCodeAndScope(smqSearched.getSmqCode(), selectedScope);
									if (list != null) {
										for (SmqRelation190 smq3 : list) {
											if (smq3.getSmqLevel() == 4) {
												level = "PT";
											} else if (smq3.getSmqLevel() == 5) {
												level = "LLT";
											} else if (smq3.getSmqLevel() == 0) {
												level = "Child SMQ";
											} 
											
											if ((smq3.getSmqLevel() != 5) || (!filterLltFlag && smq3.getSmqLevel() == 5)) {
												parent.getChildren().add(new ReportLineDataDto(level, smq3.getPtCode() + "", smq3.getPtName(), ".............", smq3.getPtTermScope() + "", smq3.getPtTermWeight() + "", smq3.getPtTermCategory(), "", smq3.getPtTermStatus()));

												/**
												 * 
												 * 
												 */
												if (level.equals("Child SMQ")) {										
													smqSearched = smqBaseService.findByCode(Long.parseLong(smq3.getPtCode() + ""));
													if (smqSearched != null) {
														//List<SmqRelation190> test =  smqBaseService.findSmqRelationsForSmqCode(smqSearched.getSmqCode());
														List<SmqRelation190> test =  smqBaseService.findSmqRelationsForSmqCodeAndScope(smqSearched.getSmqCode(), selectedScope);
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
											 					
																if ((tt.getSmqLevel() != 5) || (!filterLltFlag && tt.getSmqLevel() == 5)) {
																	parent.getChildren().add(new ReportLineDataDto(level, tt.getPtCode() + "", tt.getPtName(), "...................",
																			tt.getPtTermScope() + "", tt.getPtTermWeight() + "", tt.getPtTermCategory(), "", tt.getPtTermStatus()));

																	smqSearched = smqBaseService.findByCode(Long.parseLong(tt.getPtCode() + ""));
																	
																	if (smqSearched != null) {
																		//List<SmqRelation190> test2 =  smqBaseService.findSmqRelationsForSmqCode(smqSearched.getSmqCode());
																		List<SmqRelation190> test2 =  smqBaseService.findSmqRelationsForSmqCodeAndScope(smqSearched.getSmqCode(), selectedScope);
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
																				
																				if ((tt2.getSmqLevel() != 5) || (!filterLltFlag && tt2.getSmqLevel() == 5)) {
																					parent.getChildren().add(new ReportLineDataDto(level, tt2.getPtCode() + "", tt2.getPtName(), ".......................",
																							tt2.getPtTermScope() + "", tt2.getPtTermWeight() + "", tt2.getPtTermCategory(), "", tt2.getPtTermStatus()));

																					if (level.equals("Child SMQ")) {										
																						smqSearched = smqBaseService.findByCode(Long.parseLong(tt2.getPtCode() + ""));
																						if (smqSearched != null) {
																							//List<SmqRelation190> test3 =  smqBaseService.findSmqRelationsForSmqCode(smqSearched.getSmqCode());
																							List<SmqRelation190> test3 =  smqBaseService.findSmqRelationsForSmqCodeAndScope(smqSearched.getSmqCode(), selectedScope);
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
																				 					
																									if ((tt3.getSmqLevel() != 5) || (!filterLltFlag && tt3.getSmqLevel() == 5)) {
																										parent.getChildren().add(new ReportLineDataDto(level, tt3.getPtCode() + "", tt3.getPtName(), ".............................",
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
					List<MeddraDictHierarchySearchDto> hlts = meddraDictService.findByCodes("HLT_", hltCodesList,dictionaryVersion);
					for (MeddraDictHierarchySearchDto hlt : hlts) {
						ReportLineDataDto parent = new ReportLineDataDto(8L, "HLT", hlt.getCode(), hlt.getTerm(), "");
						relationsWorkerDTO.addToMapReport(cpt++, parent);

						/**
						 * PT.
						 */
						List<MeddraDictHierarchySearchDto> listPT =  meddraDictService.findChildrenByParentCode("PT_", "HLT_", Long.valueOf(hlt.getCode()),dictionaryVersion);
						List<Long> ptCodesList = new ArrayList<>();
						for (MeddraDictHierarchySearchDto meddra : listPT) {
							ptCodesList.add(Long.parseLong(meddra.getCode())); 
							//relationsWorkerDTO.addToMapReport(cpt++, new ReportLineDataDto("PT", meddra.getCode() + "", meddra.getTerm(), "......")); 
						}
						
						//if (!filterLltFlag) {
							List<MeddraDictHierarchySearchDto> llts = meddraDictService.findByCodes("PT_", ptCodesList,dictionaryVersion);
							if (llts != null) {
								for (MeddraDictHierarchySearchDto llt : llts) {
									parent.getChildren().add(new ReportLineDataDto("PT", llt.getCode() + "", llt.getTerm(), "......"));

									/**
									 * LLT.
									 */
									List<MeddraDictHierarchySearchDto> lltCodesList =  meddraDictService.findChildrenByParentCode("LLT_", "PT_", Long.valueOf(llt.getCode()),dictionaryVersion);
									List<Long> lltCodesList_0 = new ArrayList<>();
									for (MeddraDictHierarchySearchDto meddra : lltCodesList) {
										lltCodesList_0.add(Long.parseLong(meddra.getCode())); 
									}

									List<MeddraDictHierarchySearchDto> llts_0 = meddraDictService.findByCodes("LLT_", lltCodesList_0,dictionaryVersion);
									if (llts_0 != null) {
										for (MeddraDictHierarchySearchDto llt_1 : llts_0) {
											if (!filterLltFlag){
												parent.getChildren().add(new ReportLineDataDto("LLT", llt_1.getCode() + "", llt_1.getTerm(), "............."));
											}
										}
									}
								}
							}
						//}
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
					List<MeddraDictHierarchySearchDto> llts = meddraDictService.findByCodes("LLT_", lltCodesList,dictionaryVersion);
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
					List<MeddraDictHierarchySearchDto> pts = meddraDictService.findByCodes("PT_", ptCodesList,dictionaryVersion);
					for (MeddraDictHierarchySearchDto pt : pts) {
						ReportLineDataDto parent = new ReportLineDataDto(9L, "PT", pt.getCode() + "", pt.getTerm(), "");
						relationsWorkerDTO.addToMapReport(cpt++, parent);
						
						if (!filterLltFlag) {
							/**
							 * LLT.
							 */
							List<MeddraDictHierarchySearchDto> listPT =  meddraDictService.findChildrenByParentCode("LLT_", "PT_", Long.valueOf(pt.getCode()),dictionaryVersion);
							List<Long> hlgtCodesList = new ArrayList<>();
							for (MeddraDictHierarchySearchDto meddra : listPT) {
								hlgtCodesList.add(Long.parseLong(meddra.getCode())); 
							}

							List<MeddraDictHierarchySearchDto> llts = meddraDictService.findByCodes("LLT_", hlgtCodesList,dictionaryVersion);
							if (llts != null) {
								for (MeddraDictHierarchySearchDto llt : llts) {
									parent.getChildren().add(new ReportLineDataDto("LLT", llt.getCode() + "", llt.getTerm(), "......"));
								}
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
					List<MeddraDictHierarchySearchDto> socss = meddraDictService.findByCodes("SOC_", socCodesList,dictionaryVersion);
					for (MeddraDictHierarchySearchDto soc : socss) {
						ReportLineDataDto parent = new ReportLineDataDto(6L, "SOC", soc.getCode() + "", soc.getTerm(), "");
						relationsWorkerDTO.addToMapReport(cpt++, parent);


						/**
						 * HLGT.
						 */
						List<MeddraDictHierarchySearchDto> listHLGT =  meddraDictService.findChildrenByParentCode("HLGT_", "SOC_", Long.valueOf(soc.getCode()),dictionaryVersion);
						List<Long> hlgtCodesList = new ArrayList<>();
						for (MeddraDictHierarchySearchDto meddra : listHLGT) {
							hlgtCodesList.add(Long.parseLong(meddra.getCode())); 
						}

						List<MeddraDictHierarchySearchDto> hlgts = meddraDictService.findByCodes("HLGT_", hlgtCodesList,dictionaryVersion);
						if (hlgts != null) {
							for (MeddraDictHierarchySearchDto hlgt : hlgts) {
								parent.getChildren().add(new ReportLineDataDto("HLGT", hlgt.getCode() + "", hlgt.getTerm(), "......"));

								/**
								 * HLT.
								 */
								List<MeddraDictHierarchySearchDto> listHLT =  meddraDictService.findChildrenByParentCode("HLT_", "HLGT_", Long.valueOf(hlgt.getCode()),dictionaryVersion);
								List<Long> hltCodesList = new ArrayList<>();
								for (MeddraDictHierarchySearchDto meddra : listHLT) {
									hltCodesList.add(Long.parseLong(meddra.getCode())); 
								}

								List<MeddraDictHierarchySearchDto> hlts = meddraDictService.findByCodes("HLT_", hltCodesList,dictionaryVersion);
								if (hlts != null) {
									for (MeddraDictHierarchySearchDto hlt : hlts) {
										parent.getChildren().add(new ReportLineDataDto("HLT", hlt.getCode() + "", hlt.getTerm(), "..............."));

										/**
										 * PT.
										 */
										List<MeddraDictHierarchySearchDto> listHT =  meddraDictService.findChildrenByParentCode("PT_", "HLT_", Long.valueOf(hlt.getCode()),dictionaryVersion);
										List<Long> ptCodesList = new ArrayList<>();
										for (MeddraDictHierarchySearchDto meddra : listHT) {
											ptCodesList.add(Long.parseLong(meddra.getCode())); 
										}

										List<MeddraDictHierarchySearchDto> pts = meddraDictService.findByCodes("PT_", ptCodesList,dictionaryVersion);
										if (pts != null) {
											for (MeddraDictHierarchySearchDto pt : pts) {
												parent.getChildren().add(new ReportLineDataDto("PT", pt.getCode() + "", pt.getTerm(), "...................."));

												if (!filterLltFlag) {
													/**
													 * LLT.
													 */
													List<MeddraDictHierarchySearchDto> listPT =  meddraDictService.findChildrenByParentCode("LLT_", "PT_", Long.valueOf(pt.getCode()),dictionaryVersion);
													List<Long> lltCodesList = new ArrayList<>();
													for (MeddraDictHierarchySearchDto meddra : listPT) {
														lltCodesList.add(Long.parseLong(meddra.getCode())); 
													}

													List<MeddraDictHierarchySearchDto> llts = meddraDictService.findByCodes("LLT_", lltCodesList,dictionaryVersion);
													if (llts != null) {
														for (MeddraDictHierarchySearchDto llt : llts) {
															parent.getChildren().add(new ReportLineDataDto("LLT", llt.getCode() + "", llt.getTerm(), ".........................."));
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
					List<MeddraDictHierarchySearchDto> socDtos = meddraDictService.findByCodes("HLGT_", hlgtCodesList,dictionaryVersion);
					for (MeddraDictHierarchySearchDto hlgt : socDtos) {
						ReportLineDataDto parent = new ReportLineDataDto(7L, "HLGT", hlgt.getCode() + "", hlgt.getTerm(), "");
						relationsWorkerDTO.addToMapReport(cpt++, parent);

						/**
						 * HLT.
						 */
						List<MeddraDictHierarchySearchDto> listHLGT =  meddraDictService.findChildrenByParentCode("HLT_", "HLGT_", Long.valueOf(hlgt.getCode()),dictionaryVersion);
						List<Long> hltCodesList = new ArrayList<>();
						for (MeddraDictHierarchySearchDto meddra : listHLGT) {
							hltCodesList.add(Long.parseLong(meddra.getCode())); 
						}

						List<MeddraDictHierarchySearchDto> hlts = meddraDictService.findByCodes("HLT_", hltCodesList,dictionaryVersion);
						if (hlts != null) {
							for (MeddraDictHierarchySearchDto hlt : hlts) {
								parent.getChildren().add(new ReportLineDataDto("HLT", hlt.getCode() + "", hlt.getTerm(), "......"));

								/**
								 * PT.
								 */
								List<MeddraDictHierarchySearchDto> listHT =  meddraDictService.findChildrenByParentCode("PT_", "HLT_", Long.valueOf(hlt.getCode()),dictionaryVersion);
								List<Long> ptCodesList = new ArrayList<>();
								for (MeddraDictHierarchySearchDto meddra : listHT) {
									ptCodesList.add(Long.parseLong(meddra.getCode())); 
								}

								List<MeddraDictHierarchySearchDto> pts = meddraDictService.findByCodes("PT_", ptCodesList,dictionaryVersion);
								if (pts != null) {
									for (MeddraDictHierarchySearchDto pt : pts) {
										parent.getChildren().add(new ReportLineDataDto("PT", pt.getCode() + "", pt.getTerm(), "..............."));

										if (!filterLltFlag) {
											/**
											 * LLT.
											 */
											List<MeddraDictHierarchySearchDto> listLLT =  meddraDictService.findChildrenByParentCode("LLT_", "PT_", Long.valueOf(pt.getCode()),dictionaryVersion);
											List<Long> lltCodesList = new ArrayList<>();
											for (MeddraDictHierarchySearchDto meddra : listLLT) {
												lltCodesList.add(Long.parseLong(meddra.getCode())); 
											}

											List<MeddraDictHierarchySearchDto> list = meddraDictService.findByCodes("LLT_", lltCodesList,dictionaryVersion);
											if (list != null) {
												for (MeddraDictHierarchySearchDto llt : list) {
													parent.getChildren().add(new ReportLineDataDto("LLT", llt.getCode() + "", llt.getTerm(), "............."));
												}
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
	
	private String getWeekDay(int weekday) {
		switch (weekday) {
		case 0:
			return "Monday";
		case 1:
			return "Tuesday";
		case 2:
			return "Wednesday";
		case 3:
			return "Thursday";
		case 4:
			return "Friday";
		case 5:
			return "Saturday";
		case 6:
			return "Sunday";

		default:
			break;
		}
		return "";
	}
	
	private String getMonth(int month) {
		switch (month) {
		case 0:
			return "Jan";
		case 1:
			return "Feb";
		case 2:
			return "Mar";
		case 3:
			return "Apr";
		case 4:
			return "May";
		case 5:
			return "Jun";
		case 6:
			return "Jul";
		case 7:
			return "Aug";
		case 8:
			return "Sep";
		case 9:
			return "Oct";
		case 10:
			return "Nov";
		case 11:
			return "Dec";

		default:
			break;
		}
		return "";
	}
	
	private int fillReport(List<ReportLineDataDto> report, XSSFCell cell, XSSFRow row, int rowCount, XSSFSheet worksheet,SystemConfigProperties systemConfigProperties) {
		int cpt = 0;
		LOG.info("Writing {} ReportLineDataDtos." , report.size());
		for(ReportLineDataDto line : report) {
			if(null != line) {
				row = worksheet.createRow(rowCount);

				CellStyle headerCellStyle = worksheet.getWorkbook().createCellStyle();
				headerCellStyle.setFillForegroundColor(IndexedColors.PALE_BLUE.index);
				headerCellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

                                int cellCount = 0;
                                
				// Cell 0
				cell = row.createCell(cellCount);
				cell.setCellValue(line.getDots() + line.getTerm());
				if(line.getDots() == null || line.getDots().isEmpty()) {
					cell.setCellStyle(headerCellStyle);
				}

				// Cell 1
                                cellCount++;
				cell = row.createCell(cellCount);
				cell.setCellValue(line.getCode());
				if(line.getDots() == null || line.getDots().isEmpty()) {
					cell.setCellStyle(headerCellStyle);
				}

				// Cell 2
                                cellCount++;
				cell = row.createCell(cellCount);
				cell.setCellValue(line.getLevel());
				if(line.getDots() == null || line.getDots().isEmpty()) {
					cell.setCellStyle(headerCellStyle);
				}
				
				// Cell 3
                                if(systemConfigProperties.isDisplayCategory()) {
                                    cellCount++;
                                    cell = row.createCell(cellCount);
                                    cell.setCellValue(line.getCategory());
                                    if(line.getDots() == null || line.getDots().isEmpty()) {
                                            cell.setCellStyle(headerCellStyle);
                                    }
                                }
                                
                                // Cell 4
                                if(systemConfigProperties.isDisplayCategory2()) {
                                    cellCount++;
                                    cell = row.createCell(cellCount);
                                    cell.setCellValue(line.getCategory2());
                                    if(line.getDots() == null || line.getDots().isEmpty()) {
                                            cell.setCellStyle(headerCellStyle);
                                    }
                                }
				
				// Cell 5
                                if(systemConfigProperties.isDisplayWeight()) {
                                    cellCount++;
                                    cell = row.createCell(cellCount);
                                    cell.setCellValue(line.getWeight());
                                    if(line.getDots() == null || line.getDots().isEmpty()) {
                                            cell.setCellStyle(headerCellStyle);
                                    }
                                }
				
				// Cell 6
                                if(systemConfigProperties.isDisplayScope()) {
                                    cellCount++;
                                    cell = row.createCell(cellCount);
                                    cell.setCellValue(returnScopeValue(line.getScope()));
                                    if(line.getDots() == null || line.getDots().isEmpty()) {
                                            cell.setCellStyle(headerCellStyle);
                                    }
                                }
				
				// Cell 6
				//cell = row.createCell(5);
				//cell.setCellValue(line.getStatus());
				
				rowCount++;
				
				if(!line.getChildren().isEmpty()) {
					rowCount = fillReport(line.getChildren(), cell, row, rowCount, worksheet, systemConfigProperties);
				}
			} else {
				LOG.info("Got null line in map in fillReport");
			}
			cpt++;
		}
		return rowCount;
	}

	private int fillUniquePTReport(Collection<ReportLineDataDto> report, XSSFCell cell, XSSFRow row, int rowCount, XSSFSheet worksheet) {
		LOG.info("Writing {} ReportLineDataDtos." , report.size());
		for(ReportLineDataDto line : report) {
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

				rowCount++;

				if(!line.getChildren().isEmpty()) {
					rowCount = fillUniquePTReport(line.getChildren(), cell, row, rowCount, worksheet);
				}
			} else {
				LOG.info("Got null line in map in fillReport");
			}
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
		cellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
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
		anchor.setAnchorType(AnchorType.DONT_MOVE_AND_RESIZE);

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

@Override
	public CmqBase190 findByName(String cmqName) {
		CmqBase190 retVal = null;
		String queryString = "from CmqBase190 c where c.cmqName = :cmqName";
		EntityManager entityManager = this.cqtEntityManagerFactory
				.getEntityManager();
		try {
			Query query = entityManager.createQuery(queryString);
			query.setParameter("cmqName", cmqName);
			query.setHint("org.hibernate.cacheable", true);
			retVal = (CmqBase190) query.getSingleResult();
		} catch (Exception e) {
			StringBuilder msg = new StringBuilder();
			msg.append("findByName failed for CMQ_NAME value'").append(cmqName)
					.append("' ").append("Query used was ->")
					.append(queryString);
			LOG.error(msg.toString(), e);
		} finally {
			this.cqtEntityManagerFactory.closeEntityManager(entityManager);
		}
		return retVal;
	}

	@Override
	public Map<Long, String> findAllCmqsCodeAndName() {
		List<CmqBase190> retVal = null;
		Map<Long,String> cmqCodeNameMap = new HashMap<>();
		String queryString = "from CmqBase190 c order by upper(c.cmqName) asc ";
		EntityManager entityManager = this.cqtEntityManagerFactory
				.getEntityManager();
		try {
			Query query = entityManager.createQuery(queryString);
			query.setHint("org.hibernate.cacheable", true);
			retVal = query.getResultList();
			for(CmqBase190 cmq:retVal) {
				cmqCodeNameMap.put(cmq.getCmqCode(), cmq.getCmqName());
			}
		} catch (Exception e) {
			StringBuilder msg = new StringBuilder();
			msg.append("findAllCmqsCodeAndName failed ").append("Query used was ->")
					.append(queryString);
			LOG.error(msg.toString(), e);
		} finally {
			this.cqtEntityManagerFactory.closeEntityManager(entityManager);
		}
		return cmqCodeNameMap;
	
	}
}
