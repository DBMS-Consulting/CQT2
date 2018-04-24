package com.dbms.service;

import com.dbms.controller.GlobalController;
import com.dbms.csmq.CSMQBean;
import com.dbms.csmq.HierarchyNode;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.faces.bean.ApplicationScoped;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.apache.commons.lang3.StringUtils;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.IndexedColors;
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
import org.primefaces.model.SortOrder;
import org.primefaces.model.StreamedContent;
import org.primefaces.model.TreeNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dbms.entity.cqt.CmqBase190;
import com.dbms.entity.cqt.CmqBaseTarget;
import com.dbms.entity.cqt.CmqProductBaseTarget;
import com.dbms.entity.cqt.CmqRelation190;
import com.dbms.entity.cqt.CmqRelationTarget;
import com.dbms.entity.cqt.SmqBase190;
import com.dbms.entity.cqt.SmqBaseTarget;
import com.dbms.entity.cqt.SmqRelation190;
import com.dbms.entity.cqt.SmqRelationTarget;
import com.dbms.entity.cqt.dtos.MeddraDictHierarchySearchDto;
import com.dbms.entity.cqt.dtos.ReportLineDataDto;
import com.dbms.service.base.CqtPersistenceService;
import com.dbms.util.CmqUtils;
import com.dbms.util.CqtConstants;

import org.apache.commons.collections4.CollectionUtils;

/**
 * @author Jay G.(jayshanchn@hotmail.com)
 * @date Feb 11, 2017 8:00:48 AM
 **/
@ManagedBean(name = "CmqBaseTargetService")
@ApplicationScoped
public class CmqBaseTargetService extends CqtPersistenceService<CmqBaseTarget> implements ICmqBaseTargetService {

	private static final Logger LOG = LoggerFactory
			.getLogger(CmqBaseTargetService.class);
	
	@ManagedProperty("#{CmqRelationTargetService}")
	private ICmqRelationTargetService cmqRelationTargetService;
 
	@ManagedProperty("#{SmqBaseService}")
	private ISmqBaseService smqBaseService;
	
	@ManagedProperty("#{SmqBaseTargetService}")
	private ISmqBaseTargetService smqBaseTargetService;

	@ManagedProperty("#{MeddraDictTargetService}")
	private IMeddraDictTargetService meddraDictService;
	
	@ManagedProperty("#{RefCodeListService}")
	private IRefCodeListService refCodeListService;
    
	
	@SuppressWarnings("unchecked")
	@Override
	public List<Map<String, Object>> findImpactedWithPaginated(int first, int pageSize, String sortField
														, SortOrder sortOrder, Map<String, Object> filters) {
		List<Map<String, Object>> retVal = null;
		EntityManager entityManager = this.cqtEntityManagerFactory.getEntityManager();
		
		Session session = entityManager.unwrap(Session.class);
		try {
			String query = "SELECT CMQ_CODE as cmqCode, CMQ_NAME as cmqName, CMQ_LEVEL as cmqLevel, CMQ_STATUS as cmqStatus, "
					+ " CMQ_TYPE_CD as cmqTypeCd, CMQ_STATE as cmqState, CMQ_DESIGNEE as cmqDesignee, CMQ_DESIGNEE2 as cmqDesignee2,  CMQ_DESIGNEE3 as cmqDesignee3, "
					+ " CMQ_ID as cmqId, CMQ_DESCRIPTION as cmqDescription, CMQ_SOURCE as cmqSource, CMQ_NOTE as cmqNote,  "
					+ " CMQ_ALGORITHM as cmqAlgorithm, CMQ_PARENT_CODE as cmqParentCode, CMQ_PARENT_NAME as cmqParentName,"
					+ " IMPACT_TYPE as impactType, DICTIONARY_NAME as dictionaryName, DICTIONARY_VERSION as dictionaryVersion,"
					+ " CMQ_SUBVERSION as cmqSubversion, CREATION_DATE as creationDate, CREATED_BY as createdBy, CMQ_GROUP as cmqGroup,"
					+ " CMQ_PROGRAM_CD as cmqProgramCd, CMQ_PROTOCOL_CD as cmqProtocolCd"
					+ " FROM CMQ_BASE_TARGET WHERE (IMPACT_TYPE = 'IMPACTED' or IMPACT_TYPE = 'ICC') "  + " #1# "
 					+ " order by cmqName";

			String whereClause = "";

			if (filters.containsKey("cmqName") && filters.get("cmqName") != null) {
				String f = ((String) filters.get("cmqName")).toLowerCase();
				String cmqNameFilter = " and LOWER(CMQ_NAME) like '%" + f + "%' ";
				whereClause = whereClause + cmqNameFilter;
			}

			if (filters.containsKey("cmqLevel") && filters.get("cmqLevel") != null) {
				String cmqLevelFilter = " and CMQ_LEVEL = " + filters.get("cmqLevel");
				whereClause = whereClause + cmqLevelFilter;
			}
			
			if (filters.containsKey("cmqState") && filters.get("cmqState") != null) {
				String f = ((String) filters.get("cmqState")).toLowerCase();
				String cmqStateFilter = " and LOWER(CMQ_STATE) like '%" + f + "%' ";
				whereClause = whereClause + cmqStateFilter;
			}
			
			if (filters.containsKey("cmqTypeCd") && filters.get("cmqTypeCd") != null) {
 				String f = ((String) filters.get("cmqTypeCd")).toLowerCase();
				String filter = " and LOWER(CMQ_TYPE_CD) like '%" + f + "%' ";
				whereClause = whereClause + filter;
			}

			if (filters.containsKey("cmqCode") && filters.get("cmqCode") != null) {
				String f = ((String) filters.get("cmqCode"));
				String cmqCodeFilter = " and CMQ_CODE = " + f;
				whereClause = whereClause + cmqCodeFilter;
			}
			
			if (filters.containsKey("cmqDesignee") && filters.get("cmqDesignee") != null) {
				String f = ((String) filters.get("cmqDesignee")).toLowerCase();
				
				String filter = " and (LOWER(CMQ_DESIGNEE) like '%" + f + "%' ";
				filter += " or LOWER(CMQ_DESIGNEE2) like '%" + f + "%' ";
				filter += " or LOWER(CMQ_DESIGNEE3) like '%" + f + "%'"  + ")";
				whereClause = whereClause + filter;
			}

			query = query.replaceAll("#1#", whereClause);

			SQLQuery sqlQuery = session.createSQLQuery(query);
			sqlQuery.addScalar("cmqCode", StandardBasicTypes.LONG);
			sqlQuery.addScalar("cmqName", StandardBasicTypes.STRING);
			sqlQuery.addScalar("cmqLevel", StandardBasicTypes.INTEGER);
			sqlQuery.addScalar("cmqStatus", StandardBasicTypes.STRING);
			sqlQuery.addScalar("impactType", StandardBasicTypes.STRING);
			sqlQuery.addScalar("cmqState", StandardBasicTypes.STRING);
			sqlQuery.addScalar("cmqTypeCd", StandardBasicTypes.STRING);
			sqlQuery.addScalar("cmqDesignee", StandardBasicTypes.STRING);
			sqlQuery.addScalar("cmqDesignee2", StandardBasicTypes.STRING);
			sqlQuery.addScalar("cmqDesignee3", StandardBasicTypes.STRING);

			sqlQuery.addScalar("cmqId", StandardBasicTypes.LONG);
			sqlQuery.addScalar("dictionaryName", StandardBasicTypes.STRING);
			sqlQuery.addScalar("dictionaryVersion", StandardBasicTypes.STRING);
			sqlQuery.addScalar("cmqSubversion", StandardBasicTypes.STRING);
			sqlQuery.addScalar("cmqAlgorithm", StandardBasicTypes.STRING);
			sqlQuery.addScalar("cmqDescription", StandardBasicTypes.STRING);
			sqlQuery.addScalar("cmqNote", StandardBasicTypes.STRING);
			sqlQuery.addScalar("cmqSource", StandardBasicTypes.STRING);
			sqlQuery.addScalar("cmqParentCode", StandardBasicTypes.LONG);
			sqlQuery.addScalar("cmqParentName", StandardBasicTypes.STRING);
			
			sqlQuery.addScalar("cmqProgramCd", StandardBasicTypes.STRING);
			sqlQuery.addScalar("cmqProtocolCd", StandardBasicTypes.STRING);
			sqlQuery.addScalar("cmqGroup", StandardBasicTypes.STRING);
			sqlQuery.addScalar("creationDate", StandardBasicTypes.STRING);
			sqlQuery.addScalar("createdBy", StandardBasicTypes.STRING);
			
			sqlQuery.setResultTransformer(Transformers.ALIAS_TO_ENTITY_MAP);
			
			LOG.error("*****query 1 :" + query);

			if (pageSize >= 0) {
				sqlQuery.setMaxResults(pageSize);
			}
			if (first >= 0) {
				sqlQuery.setFirstResult(first);
			}
			
			retVal = sqlQuery.list();
		} catch (Exception e) {
			StringBuilder msg = new StringBuilder();
			msg.append("An error occurred while fetching paginated impacted CmqBaseTarget.");
			LOG.error(msg.toString(), e);
		} finally {
			this.cqtEntityManagerFactory.closeEntityManager(entityManager);
		}
		return retVal;
	}
	
	/* (non-Javadoc)
	 * @see com.dbms.service.ICmqBaseTargetService#findImpactedCount()
	 */
	@Override
	public Long findImpactedCount() {
		Long retVal = null;
		StringBuilder sb = new StringBuilder();
		sb.append("select count(*) from CmqBaseTarget c where c.impactType = 'IMPACTED' or c.impactType = 'ICC'");
		
		EntityManager entityManager = this.cqtEntityManagerFactory.getEntityManager();
		try {
			Query query = entityManager.createQuery(sb.toString());
			retVal = (Long)query.getSingleResult();
		} catch (Exception e) {
			StringBuilder msg = new StringBuilder();
			msg
					.append("An error occurred in findImpactedCount ")
					.append(" Query used was ->")
					.append(sb.toString());
			LOG.error(msg.toString(), e);
		} finally {
			this.cqtEntityManagerFactory.closeEntityManager(entityManager);
		}
		return retVal;
	}
	
	/* (non-Javadoc)
	 * @see com.dbms.service.ICmqBaseTargetService#findImpactedCount()
	 */
	@Override
	public BigDecimal findImpactedCount(Map<String, Object> filters) {
		BigDecimal retVal = null;

		String queryString = "select count(*) from CMQ_BASE_TARGET c where (c.IMPACT_TYPE = 'IMPACTED' or c.IMPACT_TYPE = 'ICC') " + " #1# " ;
		 
		String whereClause = "";

		if (filters.containsKey("cmqName") && filters.get("cmqName") != null) {
			String f = ((String) filters.get("cmqName")).toLowerCase();
			String cmqNameFilter = " and LOWER(CMQ_NAME) like '%" + f + "%' ";
			whereClause = whereClause + cmqNameFilter;
		}

		if (filters.containsKey("cmqLevel") && filters.get("cmqLevel") != null) {
			String cmqLevelFilter = " and CMQ_LEVEL = " + filters.get("cmqLevel");
			whereClause = whereClause + cmqLevelFilter;
		}
		
		if (filters.containsKey("cmqState") && filters.get("cmqState") != null) {
			String f = ((String) filters.get("cmqState")).toLowerCase();
			String cmqStateFilter = " and LOWER(CMQ_STATE) like '%" + f + "%' ";
			whereClause = whereClause + cmqStateFilter;
		}
		
		if (filters.containsKey("cmqTypeCd") && filters.get("cmqTypeCd") != null) {
				String f = ((String) filters.get("cmqTypeCd")).toLowerCase();
			String filter = " and LOWER(CMQ_TYPE_CD) like '%" + f + "%' ";
			whereClause = whereClause + filter;
		}

		if (filters.containsKey("cmqCode") && filters.get("cmqCode") != null) {
			String f = ((String) filters.get("cmqCode"));
			String cmqCodeFilter = " and CMQ_CODE = " + f;
			whereClause = whereClause + cmqCodeFilter;
		}
		
		if (filters.containsKey("cmqDesignee") && filters.get("cmqDesignee") != null) {
			String f = ((String) filters.get("cmqDesignee")).toLowerCase();
		
			String filter = " and (LOWER(CMQ_DESIGNEE) like '%" + f + "%' ";
			filter += " or LOWER(CMQ_DESIGNEE2) like '%" + f + "%' ";
			filter += " or LOWER(CMQ_DESIGNEE3) like '%" + f + "%'"  + ")";
			whereClause = whereClause + filter;
		}
		queryString = queryString.replaceAll("#1#", whereClause);
		
		EntityManager entityManager = this.cqtEntityManagerFactory.getEntityManager();
		Session session = entityManager.unwrap(Session.class);
		try {
			SQLQuery query = session.createSQLQuery(queryString);
			retVal = (BigDecimal) query.uniqueResult();
		} catch (Exception e) {
			StringBuilder msg = new StringBuilder();
			msg.append("An error occurred in findImpactedCount ").append(" Query used was ->").append(queryString);
			
		} finally {
			this.cqtEntityManagerFactory.closeEntityManager(entityManager);
		}
 		return retVal;
	}
	
	@Override
	public BigDecimal findNotImpactedCount(Map<String, Object> filters) {
		BigDecimal retVal = null;
		String queryString = "select count(*) from CMQ_BASE_TARGET c where c.IMPACT_TYPE = 'NON-IMPACTED'" + " #1# " ;

		String whereClause = "";

		if (filters.containsKey("cmqName") && filters.get("cmqName") != null) {
			String f = ((String) filters.get("cmqName")).toLowerCase();
			String cmqNameFilter = " and LOWER(CMQ_NAME) like '%" + f + "%' ";
			whereClause = whereClause + cmqNameFilter;
		}

		if (filters.containsKey("cmqLevel") && filters.get("cmqLevel") != null) {
			String cmqLevelFilter = " and CMQ_LEVEL = " + filters.get("cmqLevel");
			whereClause = whereClause + cmqLevelFilter;
		}
		
		if (filters.containsKey("cmqState") && filters.get("cmqState") != null) {
			String f = ((String) filters.get("cmqState")).toLowerCase();
			String cmqStateFilter = " and LOWER(CMQ_STATE) like '%" + f + "%' ";
			whereClause = whereClause + cmqStateFilter;
		}
		
		if (filters.containsKey("cmqTypeCd") && filters.get("cmqTypeCd") != null) {
				String f = ((String) filters.get("cmqTypeCd")).toLowerCase();
			String filter = " and LOWER(CMQ_TYPE_CD) like '%" + f + "%' ";
			whereClause = whereClause + filter;
		}

		if (filters.containsKey("cmqCode") && filters.get("cmqCode") != null) {
			String f = ((String) filters.get("cmqCode"));
			String cmqCodeFilter = " and CMQ_CODE = " + f;
			whereClause = whereClause + cmqCodeFilter;
		}
		
		if (filters.containsKey("cmqDesignee") && filters.get("cmqDesignee") != null) {
			String f = ((String) filters.get("cmqDesignee")).toLowerCase();
			 
			String filter = " and (LOWER(CMQ_DESIGNEE) like '%" + f + "%' ";
			filter += " or LOWER(CMQ_DESIGNEE2) like '%" + f + "%' ";
			filter += " or LOWER(CMQ_DESIGNEE3) like '%" + f + "%'"  + ")";
			whereClause = whereClause + filter;
		}
		queryString = queryString.replaceAll("#1#", whereClause);
		
		EntityManager entityManager = this.cqtEntityManagerFactory.getEntityManager();
		Session session = entityManager.unwrap(Session.class);
		try {
			SQLQuery query = session.createSQLQuery(queryString);
			retVal = (BigDecimal) query.uniqueResult();
		} catch (Exception e) {
			StringBuilder msg = new StringBuilder();
			msg.append("An error occurred in findNotImpactedCount ").append(" Query used was ->").append(queryString);
			LOG.error(msg.toString(), e);
		} finally {
			this.cqtEntityManagerFactory.closeEntityManager(entityManager);
		}
 
		return retVal;
	}
	
	/* (non-Javadoc)
	 * @see com.dbms.service.ICmqBaseTargetService#findNotImpactedWithPaginated(int, int, java.lang.String, org.primefaces.model.SortOrder, java.util.Map)
	 */
	@SuppressWarnings("unchecked")
	@Override
	public List<Map<String, Object>> findNotImpactedWithPaginated(int first, int pageSize, String sortField
			, SortOrder sortOrder, Map<String, Object> filters) {
		List<Map<String, Object>> retVal = null;
		EntityManager entityManager = this.cqtEntityManagerFactory.getEntityManager();
		Session session = entityManager.unwrap(Session.class);
		try {
			String query = "SELECT CMQ_CODE as cmqCode, CMQ_NAME as cmqName, CMQ_LEVEL as cmqLevel, CMQ_STATUS as cmqStatus, "
					+ " CMQ_TYPE_CD as cmqTypeCd, CMQ_STATE as cmqState,  CMQ_DESIGNEE as cmqDesignee, CMQ_DESIGNEE2 as cmqDesignee2,  CMQ_DESIGNEE3 as cmqDesignee3, "
					+ " CMQ_ID as cmqId, CMQ_DESCRIPTION as cmqDescription, CMQ_SOURCE as cmqSource, CMQ_NOTE as cmqNote,  "
					+ " CMQ_ALGORITHM as cmqAlgorithm, CMQ_PARENT_CODE as cmqParentCode, CMQ_PARENT_NAME as cmqParentName,"
					+ " IMPACT_TYPE as impactType, DICTIONARY_NAME as dictionaryName, DICTIONARY_VERSION as dictionaryVersion,"
					+ " CREATION_DATE as creationDate, CREATED_BY as createdBy, CMQ_GROUP as cmqGroup,"
					+ " CMQ_SUBVERSION as cmqSubversion, CMQ_PROGRAM_CD as cmqProgramCd, CMQ_PROTOCOL_CD as cmqProtocolCd"
					+ " FROM CMQ_BASE_TARGET WHERE IMPACT_TYPE = 'NON-IMPACTED' "
					+ "#1#"
					+ " order by cmqName";

			String whereClause = "";
			if (filters.containsKey("cmqName") && filters.get("cmqName") != null) {
				String f = ((String) filters.get("cmqName")).toLowerCase();
				String cmqNameFilter = " and LOWER(CMQ_NAME) like '%" + f + "%' ";
				whereClause = whereClause + cmqNameFilter;
			}

			if (filters.containsKey("cmqLevel") && filters.get("cmqLevel") != null) {
				String cmqLevelFilter = " and CMQ_LEVEL = " + filters.get("cmqLevel");
				whereClause = whereClause + cmqLevelFilter;
			}
			
			if (filters.containsKey("cmqState") && filters.get("cmqState") != null) {
				String f = ((String) filters.get("cmqState")).toLowerCase();
				String cmqStateFilter = " and LOWER(CMQ_STATE) like '%" + f + "%' ";
				whereClause = whereClause + cmqStateFilter;
			}
			
			if (filters.containsKey("cmqTypeCd") && filters.get("cmqTypeCd") != null) {
 				String f = ((String) filters.get("cmqTypeCd")).toLowerCase();
				String filter = " and LOWER(CMQ_TYPE_CD) like '%" + f + "%' ";
				whereClause = whereClause + filter;
			}

			if (filters.containsKey("cmqCode") && filters.get("cmqCode") != null) {
				String f = ((String) filters.get("cmqCode"));
				String cmqCodeFilter = " and CMQ_CODE = " + f;
				whereClause = whereClause + cmqCodeFilter;
			}
			
			if (filters.containsKey("cmqDesignee") && filters.get("cmqDesignee") != null) {
				String f = ((String) filters.get("cmqDesignee")).toLowerCase();
				
				String filter = " and (LOWER(CMQ_DESIGNEE) like '%" + f + "%' ";
				filter += " or LOWER(CMQ_DESIGNEE2) like '%" + f + "%' ";
				filter += " or LOWER(CMQ_DESIGNEE3) like '%" + f + "%'"  + ")";
				
				whereClause = whereClause + filter;
			}
			query = query.replaceAll("#1#", whereClause);

			SQLQuery sqlQuery = session.createSQLQuery(query);
			sqlQuery.addScalar("cmqCode", StandardBasicTypes.LONG);
			sqlQuery.addScalar("cmqName", StandardBasicTypes.STRING);
			sqlQuery.addScalar("cmqLevel", StandardBasicTypes.INTEGER);
			sqlQuery.addScalar("cmqStatus", StandardBasicTypes.STRING);
			sqlQuery.addScalar("impactType", StandardBasicTypes.STRING);
			sqlQuery.addScalar("cmqState", StandardBasicTypes.STRING);
			sqlQuery.addScalar("cmqTypeCd", StandardBasicTypes.STRING);
			sqlQuery.addScalar("cmqDesignee", StandardBasicTypes.STRING);
			sqlQuery.addScalar("cmqDesignee2", StandardBasicTypes.STRING);
			sqlQuery.addScalar("cmqDesignee3", StandardBasicTypes.STRING);
			
			sqlQuery.addScalar("cmqId", StandardBasicTypes.LONG);
			
			sqlQuery.addScalar("dictionaryName", StandardBasicTypes.STRING);
			sqlQuery.addScalar("dictionaryVersion", StandardBasicTypes.STRING);
			sqlQuery.addScalar("cmqSubversion", StandardBasicTypes.STRING);
			
			sqlQuery.addScalar("cmqAlgorithm", StandardBasicTypes.STRING);
			sqlQuery.addScalar("cmqDescription", StandardBasicTypes.STRING);
			sqlQuery.addScalar("cmqNote", StandardBasicTypes.STRING);
			sqlQuery.addScalar("cmqSource", StandardBasicTypes.STRING);
			sqlQuery.addScalar("cmqParentCode", StandardBasicTypes.LONG);
			sqlQuery.addScalar("cmqParentName", StandardBasicTypes.STRING);
			
			sqlQuery.addScalar("cmqProgramCd", StandardBasicTypes.STRING);
			sqlQuery.addScalar("cmqProtocolCd", StandardBasicTypes.STRING);
			sqlQuery.addScalar("cmqGroup", StandardBasicTypes.STRING);
			sqlQuery.addScalar("creationDate", StandardBasicTypes.STRING);
			sqlQuery.addScalar("createdBy", StandardBasicTypes.STRING);

			
			sqlQuery.setResultTransformer(Transformers.ALIAS_TO_ENTITY_MAP);
			
			LOG.error("*****query 2 :" + query);


			if (pageSize >= 0) {
				sqlQuery.setMaxResults(pageSize);
			}
			if (first >= 0) {
				sqlQuery.setFirstResult(first);
			}
			
			retVal = sqlQuery.list();
		} catch (Exception e) {
			StringBuilder msg = new StringBuilder();
			msg.append("An error occurred while fetching paginated impacted CmqBaseTarget.");
			LOG.error(msg.toString(), e);
		} finally {
			this.cqtEntityManagerFactory.closeEntityManager(entityManager);
		}
		return retVal;
	}

	/* (non-Javadoc)
	 * @see com.dbms.service.ICmqBaseTargetService#findNotImpactedCount()
	 */
	@Override
	public Long findNotImpactedCount() {
		Long retVal = null;
		StringBuilder sb = new StringBuilder();
		sb.append("select count(*) from CmqBaseTarget c where c.impactType = 'NON-IMPACTED'");
	
		EntityManager entityManager = this.cqtEntityManagerFactory.getEntityManager();
		try {
			Query query = entityManager.createQuery(sb.toString());
			retVal = (Long)query.getSingleResult();
		} catch (Exception e) {
			StringBuilder msg = new StringBuilder();
			msg
			.append("An error occurred in findImpactedCount ")
			.append(" Query used was ->")
			.append(sb.toString());
			LOG.error(msg.toString(), e);
		} finally {
			this.cqtEntityManagerFactory.closeEntityManager(entityManager);
		}
		return retVal;
	}
	
	@Override
	public CmqBaseTarget findByCode(Long cmqCode) {
		CmqBaseTarget retVal = null;
		String queryString = "from CmqBaseTarget c where c.cmqCode = :cmqCode";
		EntityManager entityManager = this.cqtEntityManagerFactory
				.getEntityManager();
		try {
			Query query = entityManager.createQuery(queryString);
			query.setParameter("cmqCode", cmqCode);
			retVal = (CmqBaseTarget) query.getSingleResult();
		} catch (javax.persistence.NoResultException e) {
			StringBuilder msg = new StringBuilder();
			msg.append("findByCode found no result for CMQ_CODE value'").append(cmqCode);
			LOG.error(msg.toString());
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
                + " from CMQ_BASE_TARGET cmqTbl"
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
	public Long findCmqChildCountForParentCmqCode(Long cmqCode) {
		Long retVal = null;
		StringBuilder sb = new StringBuilder();
		sb.append("select count(*) from CmqBaseTarget c where c.cmqParentCode = :cmqCode");

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
	
	@SuppressWarnings("unchecked")
	@Override
	public List<CmqProductBaseTarget> findProductsByCmqCode(Long code) {
		List<CmqProductBaseTarget> retVal = null;
		String queryString = "from CmqProductBaseTarget c where c.cmqBaseTarget.cmqCode = :code ";
		EntityManager entityManager = this.cqtEntityManagerFactory
				.getEntityManager();
		try {
			Query query = entityManager.createQuery(queryString);
			query.setParameter("code", code);
			retVal = query.getResultList();
		} catch (Exception e) {
			StringBuilder msg = new StringBuilder();
			msg.append("findProductsByCmqCode failed ")
					.append("Query used was ->").append(queryString);
			LOG.error(msg.toString(), e);
		} finally {
			this.cqtEntityManagerFactory.closeEntityManager(entityManager);
		}
		return retVal;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public List<CmqBaseTarget> findChildCmqsByParentCode(Long code) {
		List<CmqBaseTarget> retVal = null;
		String queryString = "from CmqBaseTarget c where c.cmqParentCode = :codeList ";
		EntityManager entityManager = this.cqtEntityManagerFactory
				.getEntityManager();
		try {
			Query query = entityManager.createQuery(queryString);
			query.setParameter("codeList", code);
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
	@Override
	public List<CmqBaseTarget> findByLevelAndTerm(Integer level, String searchTerm) {
		List<CmqBaseTarget> retVal = null;
		StringBuilder sb = new StringBuilder();
		//sb.append("from CmqBaseTarget c where c.cmqLevel = :cmqLevel and c.cmqParentCode is null and c.cmqStatus = 'I' ");
		sb.append("from CmqBaseTarget c where c.cmqLevel = :cmqLevel and c.cmqParentCode is null ");
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
					"An error occurred while fetching types from CmqBaseTarget on cmqLevel ")
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
	public StreamedContent generateCMQExcel(CmqBaseTarget selectedImpactedCmqList, String dictionaryVersion, TreeNode selectedNode, boolean filterLltFlag) {
		
		List<TreeNode> childTreeNodes = selectedNode.getChildren();
		Map<String,String> relationScopeMap = new HashMap<>();
		
		for(TreeNode childTreeNode: childTreeNodes) {
			updateRelationScopeMap(relationScopeMap,childTreeNode);
		}
		
		
		XSSFWorkbook workbook = new XSSFWorkbook();
		XSSFSheet worksheet = null;

		worksheet = workbook.createSheet("IA Report");
		XSSFRow row = null;
		int rowCount = 0;
		DateFormat dateFormat = DateFormat.getDateTimeInstance(
		        DateFormat.LONG,
		        DateFormat.LONG, new Locale("EN","en"));
		
		Map<Integer, ReportLineDataDto> mapReport = new HashMap<Integer, ReportLineDataDto>();
		int cpt = 0;

		/**
		 * Première ligne - entêtes
		 */
		row = worksheet.createRow(rowCount);
		XSSFCell cell = row.createCell(0);

		row = worksheet.createRow(rowCount);
		cell = row.createCell(0);
		cell.setCellValue(selectedImpactedCmqList.getCmqName());
		setCellStyleTitre(workbook, cell);

		// Term name
		rowCount += 2;
		row = worksheet.createRow(rowCount);
		cell = row.createCell(0);
		cell.setCellValue("MedDRA Dictionary Version: " + dictionaryVersion);

		rowCount++;
		row = worksheet.createRow(rowCount);
		cell = row.createCell(0);
//		cell.setCellValue("State: " + returnState(selectedImpactedCmqList));
		cell.setCellValue("State: " + selectedImpactedCmqList.getCmqState());
		
		rowCount++;
		row = worksheet.createRow(rowCount);
		cell = row.createCell(0);
		cell.setCellValue("Report Date/Time: " + dateFormat.format(new Date()));
		

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
		cell.setCellValue("Impact Relation Type");
		setCellStyleColumn(workbook, cell);
		
		/*cell = row.createCell(7);
		cell.setCellValue("PT Status");
		setCellStyleColumn(workbook, cell); */
		
		rowCount++;

		// Retrieval of relations - Loop
		List<CmqRelationTarget> relations = cmqRelationTargetService.findByCmqCode(selectedImpactedCmqList.getCmqCode());

		String level = "", term = "", codeTerm = "";

		if (relations != null) {
			for (CmqRelationTarget relation : relations) {
				//System.out.println("\n *******   relation.getRelationImpactType() : " + relation.getRelationImpactType());

				/**
				 * 
				 * SMQs
				 */
				if (relation.getSmqCode() != null) {
					String selectedScope = relationScopeMap.get(String.valueOf(relation.getSmqCode()));
					if(StringUtils.isEmpty(selectedScope)) {
						selectedScope = relation.getTermScope();
					}
 					List<Long> smqChildCodeList = new ArrayList<>();
					smqChildCodeList.add(relation.getSmqCode());

					SmqBaseTarget smqSearched = smqBaseTargetService.findByCode(relation.getSmqCode());
					if (smqSearched != null) {
						List<SmqBaseTarget> smqBaseList = smqBaseTargetService.findByLevelAndTerm(smqSearched.getSmqLevel(),	smqSearched.getSmqName());
						if (smqBaseList != null) {
							for (SmqBaseTarget smq : smqBaseList) {
								level = getLevelFromValue(smq.getSmqLevel());
								
								System.out.println("____________________________%%%%%%%%%%% STATUS SMQ " + smq.getSmqStatus() + ", smq= " + level);
								 
								mapReport.put(cpt++, new ReportLineDataDto(level, smq.getSmqCode() + "", smq.getSmqName(), "", smq.getImpactType(), "", smq.getSmqStatus())); 
 								
								/**
								 * Other SMQs
								 * 
								 */
								List<SmqBaseTarget> smqs = smqBaseTargetService.findChildSmqByParentSmqCodes(smqChildCodeList);
								
								if (smqs != null) {
									for (SmqBaseTarget smqC : smqs) {
										level = getLevelFromValue(smqC.getSmqLevel());
 										mapReport.put(cpt++, new ReportLineDataDto(level, smqC.getSmqCode() + "", smqC.getSmqName(), "......", smqC.getImpactType(), "", smqC.getSmqStatus())); 

										
										if (level.equals("SMQ1")) {			
											smqSearched = smqBaseTargetService.findByCode(smqC.getSmqCode());
											buildLinesFromLevel(smqSearched, mapReport, "PT", cpt, ".............");
											
											List<Long> codes = new ArrayList<>();
											codes.add(smqC.getSmqCode());
											
											//Others relations
											String levelS = "";
											level = getLevelFromValue(smqSearched.getSmqLevel());
											 
											List<SmqBaseTarget> smqChildren = smqBaseTargetService.findChildSmqByParentSmqCodes(codes);
											if (smqChildren != null) {
												for (SmqBaseTarget child : smqChildren) {
													mapReport.put(cpt++, new ReportLineDataDto(levelS, child.getSmqCode() + "", child.getSmqName(), ".............", "", "", smq.getSmqStatus())); 
													
													codes = new ArrayList<>();
													codes.add(child.getSmqCode());
													smqSearched = smqBaseTargetService.findByCode(child.getSmqCode());
													buildLinesFromLevel(smqSearched, mapReport, "PT", cpt, "....................");																										
												}
											}
										}
										
										if (level.equals("SMQ2")) {						
											smqSearched = smqBaseTargetService.findByCode(smqC.getSmqCode());
											buildLinesFromLevel(smqSearched, mapReport, "PT", cpt, "...........");													

											List<Long> codes = new ArrayList<>();
											codes.add(smqC.getSmqCode());
											
											//Others relations
											String levelS = "";
											levelS = getLevelFromValue(smqSearched.getSmqLevel());
										  
											List<SmqBaseTarget> smqChildren = smqBaseTargetService.findChildSmqByParentSmqCodes(codes);
											if (smqChildren != null) {
												for (SmqBaseTarget child : smqChildren) {
													mapReport.put(cpt++, new ReportLineDataDto(levelS, child.getSmqCode() + "", child.getSmqName(), ".............", "", "", child.getSmqStatus())); 
													
													smqSearched = smqBaseTargetService.findByCode(child.getSmqCode());
													buildLinesFromLevel(smqSearched, mapReport, "PT", cpt, "....................");																										
												}
											}
										}
										
										if (level.equals("SMQ3")) {	
											smqSearched = smqBaseTargetService.findByCode(smqC.getSmqCode());
											buildLinesFromLevel(smqSearched, mapReport, "PT", cpt, "...........");
										}					
									}
								}
							}
						}
					}
					
					//Relations for SMQs
 					//List<SmqRelationTarget> childSmqs =  smqBaseTargetService.findSmqRelationsForSmqCode(relation.getSmqCode());
					List<SmqRelationTarget> childSmqs =  smqBaseTargetService.findSmqRelationsForSmqCodeAndScope(relation.getSmqCode(), selectedScope);

					if((null != childSmqs) && (childSmqs.size() > 0)) {
						for (SmqRelationTarget childSmq : childSmqs) {
							level = getLevelFromValue(childSmq.getSmqLevel());
							if(filterLltFlag && (childSmq.getSmqLevel() == 5)) {
								continue;
							}
							mapReport.put(cpt++, new ReportLineDataDto(level, childSmq.getPtCode() + "", childSmq.getPtName(), ".......", childSmq.getPtTermScope() + "", childSmq.getPtTermWeight() + "", childSmq.getPtTermCategory(), childSmq.getRelationImpactType(), childSmq.getPtTermStatus())); 
							
							List<Long> codes = new ArrayList<>();
							codes.add(childSmq.getSmqCode());
							
 							
							smqSearched = smqBaseTargetService.findByCode(Long.parseLong(childSmq.getPtCode() + ""));
							if (smqSearched != null) {
								//List<SmqRelationTarget> list = smqBaseTargetService.findSmqRelationsForSmqCode(smqSearched.getSmqCode());
								List<SmqRelationTarget> list = smqBaseTargetService.findSmqRelationsForSmqCodeAndScope(smqSearched.getSmqCode(),selectedScope);
								if (list != null) {
									for (SmqRelationTarget smq3 : list) {
										level = getLevelFromValue(smq3.getSmqLevel());
										mapReport.put(cpt++, new ReportLineDataDto(level, smq3.getPtCode() + "", smq3.getPtName(), ".............", smq3.getPtTermScope() + "", smq3.getPtTermWeight() + "", smq3.getPtTermCategory(), smq3.getRelationImpactType(), smq3.getPtTermStatus())); 
									
										 
										if (level.equals("Child SMQ")) {										
											smqSearched = smqBaseTargetService.findByCode(Long.parseLong(smq3.getPtCode() + ""));
											if (smqSearched != null) {
												//List<SmqRelationTarget> test =  smqBaseTargetService.findSmqRelationsForSmqCode(smqSearched.getSmqCode());
												List<SmqRelationTarget> test =  smqBaseTargetService.findSmqRelationsForSmqCodeAndScope(smqSearched.getSmqCode(),selectedScope);
 							 					
							 					if (test != null) {
													for (SmqRelationTarget tt : test) {
														level = getLevelFromValue(tt.getSmqLevel());
									 					
														mapReport.put(cpt++, new ReportLineDataDto(level, tt.getPtCode() + "", tt.getPtName(), "...................", 
																tt.getPtTermScope() + "", tt.getPtTermWeight() + "", tt.getPtTermCategory(), tt.getRelationImpactType(), tt.getPtTermStatus())); 
														
														smqSearched = smqBaseTargetService.findByCode(Long.parseLong(tt.getPtCode() + ""));
														
														if (smqSearched != null) {
															List<SmqRelationTarget> test2 =  smqBaseTargetService.findSmqRelationsForSmqCodeAndScope(smqSearched.getSmqCode(),selectedScope);
 										 					
										 					if (test2 != null) {
																for (SmqRelationTarget tt2 : test2) {
																	level = getLevelFromValue(tt2.getSmqLevel());
																	
																	mapReport.put(cpt++, new ReportLineDataDto(level, tt2.getPtCode() + "", tt2.getPtName(), ".......................", 
																			tt2.getPtTermScope() + "", tt2.getPtTermWeight() + "", tt2.getPtTermCategory(), tt2.getRelationImpactType(), tt.getPtTermStatus())); 
  																	
																	if (level.equals("Child SMQ")) {										
																		smqSearched = smqBaseTargetService.findByCode(Long.parseLong(tt2.getPtCode() + ""));
																		if (smqSearched != null) {
																			List<SmqRelationTarget> test3 =  smqBaseTargetService.findSmqRelationsForSmqCodeAndScope(smqSearched.getSmqCode(),selectedScope);
 														 					 
														 					if (test3 != null) {
																				for (SmqRelationTarget tt3 : test3) {
																					level = getLevelFromValue(tt3.getSmqLevel());
																 					
																					mapReport.put(cpt++, new ReportLineDataDto(level, tt3.getPtCode() + "", tt3.getPtName(), ".............................", 
																							tt3.getPtTermScope() + "", tt3.getPtTermWeight() + "", tt3.getPtTermCategory(), tt3.getRelationImpactType(), tt3.getPtTermStatus())); 
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
  
				/**
				 * 
				 * HLT.
				 */
				if (relation.getHltCode() != null) {
					List<Long> hltCodesList = new ArrayList<>();
					hltCodesList.add(relation.getHltCode());
					List<MeddraDictHierarchySearchDto> hlts = this.meddraDictService.findByCodes("HLT_", hltCodesList);
					for (MeddraDictHierarchySearchDto hlt : hlts) {
						mapReport.put(cpt++, new ReportLineDataDto("HLT", hlt.getCode(), hlt.getTerm(), "", hlt, getCmqRelationImpactDesc(relation.getRelationImpactType())));  

						/**
						 * PT.
						 */
						List<MeddraDictHierarchySearchDto> listPT =  meddraDictService.findChildrenByParentCode("PT_", "HLT_", Long.valueOf(hlt.getCode()));
						List<Long> ptCodesList = new ArrayList<>();
						for (MeddraDictHierarchySearchDto meddra : listPT) {
							ptCodesList.add(Long.parseLong(meddra.getCode())); 
						}

						List<MeddraDictHierarchySearchDto> llts = this.meddraDictService.findByCodes("PT_", ptCodesList);
						if (llts != null) {
							for (MeddraDictHierarchySearchDto pt : llts) {
								mapReport.put(cpt++, new ReportLineDataDto("PT", pt.getCode() + "", pt.getTerm(), "......", pt, getImpact(pt, "PT"))); 
							
								if(!filterLltFlag) {
									/**
									 * LLT.
									 */
									List<MeddraDictHierarchySearchDto> listLLT =  meddraDictService.findChildrenByParentCode("LLT_", "PT_", Long.valueOf(pt.getCode()));
									List<Long> lltCodesList = new ArrayList<>();
									for (MeddraDictHierarchySearchDto meddra : listLLT) {
										lltCodesList.add(Long.parseLong(meddra.getCode())); 
									}

									List<MeddraDictHierarchySearchDto> list = this.meddraDictService.findByCodes("LLT_", lltCodesList);
									if (list != null)
										for (MeddraDictHierarchySearchDto llt : list) {
											mapReport.put(cpt++, new ReportLineDataDto("LLT", llt.getCode() + "", llt.getTerm(), ".............", llt, getImpact(llt, "LLT"))); 
										}
								}
 							}
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
						mapReport.put(cpt++, new ReportLineDataDto("PT", pt.getCode() + "", pt.getTerm(), "", pt, getCmqRelationImpactDesc(relation.getRelationImpactType())));
  						
						if(!filterLltFlag) {
							/**
							 * LLT.
							 */
							List<MeddraDictHierarchySearchDto> listPT =  meddraDictService.findChildrenByParentCode("LLT_", "PT_", Long.valueOf(pt.getCode()));
							List<Long> hlgtCodesList = new ArrayList<>();
							for (MeddraDictHierarchySearchDto meddra : listPT) {
								hlgtCodesList.add(Long.parseLong(meddra.getCode())); 
							}

							List<MeddraDictHierarchySearchDto> llts = this.meddraDictService.findByCodes("LLT_", hlgtCodesList);
	 						for (MeddraDictHierarchySearchDto llt : llts) {
	 							mapReport.put(cpt++, new ReportLineDataDto("LLT", llt.getCode() + "", llt.getTerm(), "......", llt, getImpact(llt, "LLT")));
							}
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
						mapReport.put(cpt++, new ReportLineDataDto("SOC", soc.getCode() + "", soc.getTerm(), "", soc, getCmqRelationImpactDesc(relation.getRelationImpactType()))); 

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
								mapReport.put(cpt++, new ReportLineDataDto("HLGT", hlgt.getCode() + "", hlgt.getTerm(), "......", hlgt, getImpact(hlgt, "HLGT")));
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
										mapReport.put(cpt++, new ReportLineDataDto("HLT", hlt.getCode() + "", hlt.getTerm(), "...............", hlt, getImpact(hlt, "HLT"))); 
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
												mapReport.put(cpt++, new ReportLineDataDto("PT", pt.getCode() + "", pt.getTerm(), "....................", pt, getImpact(pt, "PT"))); 
												
												if(!filterLltFlag) {
													/**
													 * LLT.
													 */
													List<MeddraDictHierarchySearchDto> listPT =  meddraDictService.findChildrenByParentCode("LLT_", "PT_", Long.valueOf(pt.getCode()));
													List<Long> lltCodes = new ArrayList<>();
													for (MeddraDictHierarchySearchDto meddra : listPT) {
														lltCodes.add(Long.parseLong(meddra.getCode())); 
													}
	 												List<MeddraDictHierarchySearchDto> llts = this.meddraDictService.findByCodes("LLT_", lltCodes);
													if (llts != null)
														for (MeddraDictHierarchySearchDto llt : llts) {
															mapReport.put(cpt++, new ReportLineDataDto("LLT", llt.getCode() + "", llt.getTerm(), "..........................", llt, getImpact(llt, "LLT"))); 
														}
													
												}
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
						mapReport.put(cpt++, new ReportLineDataDto("HLGT", hlgt.getCode() + "", hlgt.getTerm(), "", hlgt, getCmqRelationImpactDesc(relation.getRelationImpactType())));  
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
								mapReport.put(cpt++, new ReportLineDataDto("HLT", hlt.getCode() + "", hlt.getTerm(), "......", hlt, getImpact(hlt, "HLT"))); 
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
										mapReport.put(cpt++, new ReportLineDataDto("PT", pt.getCode() + "", pt.getTerm(), "...............", pt, getImpact(pt, "PT"))); 
										
										if(!filterLltFlag) {
											/**
											 * LLT.
											 */
											List<MeddraDictHierarchySearchDto> listPT =  meddraDictService.findChildrenByParentCode("LLT_", "PT_", Long.valueOf(pt.getCode()));
											List<Long> lltCodes = new ArrayList<>();
											for (MeddraDictHierarchySearchDto meddra : listPT) {
												lltCodes.add(Long.parseLong(meddra.getCode())); 
											}

											List<MeddraDictHierarchySearchDto> llts = this.meddraDictService.findByCodes("LLT_", lltCodes);
					 						for (MeddraDictHierarchySearchDto llt : llts) {
					 							mapReport.put(cpt++, new ReportLineDataDto("LLT", llt.getCode() + "", llt.getTerm(), ".......................", llt, getImpact(llt, "LLT")));
											}
										}
										
									}
							}
					}
				}
				

				/**
				 * 
				 * LLT.
				 */
				if (relation.getLltCode() != null) {
 					List<Long> lltCodesList = new ArrayList<>();
					lltCodesList.add(relation.getLltCode());
					List<MeddraDictHierarchySearchDto> llts = meddraDictService.findByCodes("LLT_", lltCodesList);
					for (MeddraDictHierarchySearchDto llt : llts) {
 						mapReport.put(cpt++, new ReportLineDataDto("LLT", llt.getCode() + "", llt.getTerm(), "", llt, getCmqRelationImpactDesc(relation.getRelationImpactType()))); 
					}
 				}
			}
			 
		}
		
		
		
		/**
		 * PRO, TME.
		 */
		List<CmqBaseTarget> childCmqs = findChildCmqsByParentCode(selectedImpactedCmqList.getCmqCode());
		
		if((null != childCmqs) && (childCmqs.size() > 0)) {
			for (CmqBaseTarget childCmq : childCmqs) {
				
				level = childCmq.getCmqTypeCd();
				term = childCmq.getCmqName();
				codeTerm = childCmq.getCmqCode() != null ? childCmq.getCmqCode() + "" : "";

				mapReport.put(cpt++, new ReportLineDataDto(level, codeTerm, term, "", childCmq.getImpactType()));
				 

				/**
				 * Other Relations
				 */
				List<CmqRelationTarget> relationsPro = cmqRelationTargetService.findByCmqCode(childCmq.getCmqCode());

				if (relations != null) {
					for (CmqRelationTarget relation : relationsPro) {
 						if (relation.getSmqCode() != null) {
							List<Long> smqChildCodeList = new ArrayList<>();
							smqChildCodeList.add(relation.getSmqCode());

							SmqBaseTarget smqSearched = smqBaseTargetService.findByCode(relation.getSmqCode());
							if (smqSearched != null) {
								List<SmqBaseTarget> smqBaseList = smqBaseTargetService.findByLevelAndTerm(smqSearched.getSmqLevel(), smqSearched.getSmqName());
								if (smqBaseList != null) {
									for (SmqBaseTarget smq : smqBaseList) {
										level = getLevelFromValue(smq.getSmqLevel());
										mapReport.put(cpt++, new ReportLineDataDto(level, smq.getSmqCode() + "", smq.getSmqName(), "........", smq.getImpactType(), "", smq.getSmqStatus())); 
										
										/**
										 * OTHERS.
										 */										
										List<SmqBaseTarget> smqs2 = smqBaseTargetService.findChildSmqByParentSmqCodes(smqChildCodeList);
										SmqBaseTarget smqSearched2 = null;
										
										if (smqs2 != null) {
											for (SmqBaseTarget smqC : smqs2) {
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
												
												if((smqC.getSmqLevel() != 5) || (!filterLltFlag && (smqC.getSmqLevel() == 5))) {
													mapReport.put(cpt++, new ReportLineDataDto(level, smqC.getSmqCode() + "", smqC.getSmqName(), "......", "", "", smqC.getSmqStatus()));  
													
													if (level.equals("SMQ1")) {						
														smqSearched2 = smqBaseTargetService.findByCode(smqC.getSmqCode());
														buildLinesFromLevel(smqSearched, mapReport, "PT", cpt, ".............");

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
														if((smqSearched.getSmqLevel() != 5) || (!filterLltFlag && (smqSearched.getSmqLevel() == 5))) {
															List<SmqBaseTarget> smqChildren = smqBaseTargetService.findChildSmqByParentSmqCodes(codes);
															if (smqChildren != null) {
																for (SmqBaseTarget child : smqChildren) {
																	mapReport.put(cpt++, new ReportLineDataDto(levelS, child.getSmqCode() + "", child.getSmqName(), ".............", "", "", child.getSmqStatus())); 
																	
																	codes = new ArrayList<>();
																	codes.add(child.getSmqCode());
																	smqSearched2 = smqBaseTargetService.findByCode(child.getSmqCode());
																	buildLinesFromLevel(smqSearched, mapReport, "PT", cpt, ".....................");															
																}
															}
														}
													}
												}
											}
										}
										
										/**
										 * Other SMQs
										 * 
										 */
 										List<SmqRelationTarget> smqs = null;
										 
										smqSearched = smqBaseTargetService.findByCode(smq.getSmqCode());
										if (smqSearched != null) {
											smqs = smqBaseTargetService.findSmqRelationsForSmqCode(smqSearched.getSmqCode());
											 
										if (smqs != null) {
											for (SmqRelationTarget smqC : smqs) {
												level = getLevelFromValue(smqC.getSmqLevel());
												
												mapReport.put(cpt++, new ReportLineDataDto(level, smqC.getSmqCode() + "", smqC.getPtName(), "..............", smqC.getRelationImpactType(), "", smqC.getPtTermStatus()));
												
												System.out.println("************************ STATUS :: " + smqC.getPtTermStatus()); //TODO
												
												if (level.equals("SMQ1")) {						
													smqSearched = smqBaseTargetService.findByCode(smqC.getSmqCode());
													if (smqSearched != null) {
														List<SmqRelationTarget> list = smqBaseTargetService.findSmqRelationsForSmqCode(smqSearched.getSmqCode());
														if (list != null) {
															for (SmqRelationTarget smq3 : list) {
																level = getLevelFromValue(smq3.getSmqLevel());

																if((smq3.getSmqLevel() != 5) || (!filterLltFlag && (smq3.getSmqLevel() == 5))) {
																	mapReport.put(cpt++, new ReportLineDataDto(level, smq3.getPtCode() + "", smq3.getPtName(), "....................", 
							 												smq3.getPtTermScope() + "", smq3.getPtTermWeight() + "", smq3.getPtTermCategory(), smq3.getRelationImpactType(), smq3.getPtTermStatus())); 
																
							 										if (level.equals("Child SMQ")) {
																		smqSearched = smqBaseTargetService.findByCode(Long.parseLong(smq3.getPtCode() + ""));
																		if (smqSearched != null) {
																			List<SmqRelationTarget> childSMQs =  smqBaseTargetService.findSmqRelationsForSmqCode(smqSearched.getSmqCode());
	 													 					
														 					for (SmqRelationTarget smqTest : childSMQs) {
	 																			level = getLevelFromValue(smqTest.getSmqLevel());
	 																			if((smqTest.getSmqLevel() != 5) || (!filterLltFlag && (smqTest.getSmqLevel() == 5))) {
	 																				mapReport.put(cpt++, new ReportLineDataDto(level, smqTest.getPtCode() + "", smqTest.getPtName(), "..........................", 
										 													smqTest.getPtTermScope() + "", smqTest.getPtTermWeight() + "", smqTest.getPtTermCategory(), smqTest.getRelationImpactType(), smqTest.getPtTermStatus())); 
															 					  

																 					List<Long> codes = new ArrayList<>();
																					codes.add(smqTest.getSmqCode());
																					
			 																		//Others relations
																					List<SmqBaseTarget> smqChildren = smqBaseTargetService.findChildSmqByParentSmqCodes(codes);
																					if (smqChildren != null) {
																						for (SmqBaseTarget child : smqChildren) {
		 																					mapReport.put(cpt++, new ReportLineDataDto(getLevelFromValue(child.getSmqLevel()), child.getSmqCode() + "", child.getSmqName(), 
		 																							".............................", child.getImpactType(), child.getSmqStatus())); 
				  																		}
																					}
										 											
										 											 
										 											smqSearched = smqBaseTargetService.findByCode(Long.parseLong(smqTest.getPtCode() + ""));

																					if (smqSearched != null) {
																						List<SmqRelationTarget> childSMQs2 =  smqBaseTargetService.findSmqRelationsForSmqCode(smqSearched.getSmqCode());
				 													 					
																	 					for (SmqRelationTarget smqTest2 : childSMQs2) {
																							level = getLevelFromValue(smqTest2.getSmqLevel());
																							if((smqTest2.getSmqLevel() != 5) || (!filterLltFlag && (smqTest2.getSmqLevel() == 5))) {
																								mapReport.put(cpt++, new ReportLineDataDto(level, smqTest2.getPtCode() + "", smqTest2.getPtName(), ".......................", 
													 													smqTest2.getPtTermScope() + "", smqTest2.getPtTermWeight() + "", smqTest2.getPtTermCategory(), smqTest2.getRelationImpactType(), smqTest2.getPtTermStatus()));	
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
												
												if (level.equals("SMQ2")) {						
													smqSearched = smqBaseTargetService.findByCode(smqC.getSmqCode());
													if (smqSearched != null) {
														List<SmqRelationTarget> list = smqBaseTargetService.findSmqRelationsForSmqCode(smqSearched.getSmqCode());
														if (list != null)
															for (SmqRelationTarget smq3 : list) {
																if (smqSearched.getSmqLevel() == 0) {										
																	smqSearched = smqBaseTargetService.findByCode(Long.parseLong(smq3.getPtCode() + ""));
																	if (smqSearched != null) {
																		List<SmqRelationTarget> test3 =  smqBaseTargetService.findSmqRelationsForSmqCode(smqSearched.getSmqCode());
 														 					
													 					if (test3 != null) {
																			for (SmqRelationTarget tt3 : test3) {
																				level = getLevelFromValue(tt3.getSmqLevel());
																				if((tt3.getSmqLevel() != 5) || (!filterLltFlag && (tt3.getSmqLevel() == 5))) {
																					mapReport.put(cpt++, new ReportLineDataDto(level, tt3.getPtCode() + "", tt3.getPtName(), ".........................", tt3.getRelationImpactType(), "", tt3.getPtTermStatus())); 
																					
																					if (level.equals("Child SMQ")) {										
																						smqSearched = smqBaseTargetService.findByCode(Long.parseLong(tt3.getPtCode() + ""));
																						if (smqSearched != null) {
																							List<SmqRelationTarget> otherChilds = smqBaseTargetService.findSmqRelationsForSmqCode(smqSearched.getSmqCode());
	 																	 					
																		 					
																		 					if (test3 != null) {
																								for (SmqRelationTarget other : otherChilds) {
																									level = getLevelFromValue(other.getSmqLevel());
																									if((other.getSmqLevel() != 5) || (!filterLltFlag && (other.getSmqLevel() == 5))) {
																										mapReport.put(cpt++, new ReportLineDataDto(level, other.getPtCode() + "", other.getPtName(), ".........................", other.getRelationImpactType(), "", other.getPtTermStatus())); 

																										if (level.equals("Child SMQ")) {		
		 																									smqSearched = smqBaseTargetService.findByCode(Long.parseLong(other.getPtCode() + ""));
																											if (smqSearched != null) {
																												List<SmqRelationTarget> fourthCH = smqBaseTargetService.findSmqRelationsForSmqCode(smqSearched.getSmqCode());
																							 					if (test3 != null) {
																													for (SmqRelationTarget fourth : fourthCH) {
																														level = getLevelFromValue(fourth.getSmqLevel());
																														if((fourth.getSmqLevel() != 5) || (!filterLltFlag && (fourth.getSmqLevel() == 5))) {
																															mapReport.put(cpt++, new ReportLineDataDto(level, fourth.getPtCode() + "", fourth.getPtName(), "...............................", fourth.getRelationImpactType(), "", fourth.getPtTermStatus())); 
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
													List<Long> codes = new ArrayList<>();
													codes.add(smqC.getSmqCode());
													
													
													//Others relations
													String levelS = "";
													level = getLevelFromValue(smqSearched.getSmqLevel());
													if((smqSearched.getSmqLevel() != 5) || (!filterLltFlag && (smqSearched.getSmqLevel() == 5))) {
														List<SmqBaseTarget> smqChildren = smqBaseTargetService.findChildSmqByParentSmqCodes(codes);
														if (smqChildren != null)
															for (SmqBaseTarget child : smqChildren) {
																mapReport.put(cpt++, new ReportLineDataDto(levelS, child.getSmqCode() + "", child.getSmqName(), ".............", child.getImpactType(), "", child.getSmqStatus())); 
																
																smqSearched = smqBaseTargetService.findByCode(child.getSmqCode());
																if (smqSearched != null) {
																	List<SmqRelationTarget> list = smqBaseTargetService.findSmqRelationsForSmqCode(smqSearched.getSmqCode());
																	if (list != null)
																		for (SmqRelationTarget smq3 : list) {
																			mapReport.put(cpt++, new ReportLineDataDto("PT", smq3.getPtCode() + "", smq3.getPtName(), "....................", smq3.getRelationImpactType(), "", smq3.getPtTermStatus())); 
	 																	}
																}
															}
													}
 												}
												
												if (level.equals("SMQ3")) {
													smqSearched = smqBaseTargetService.findByCode(smqC.getSmqCode());
													buildLinesFromLevelRelation(smqSearched, mapReport, "PT", cpt, ".............");													
												}	
												if (level.equals("SMQ4")) {
													smqSearched = smqBaseTargetService.findByCode(smqC.getSmqCode());
													if (smqSearched != null) {
														List<SmqRelationTarget> list = smqBaseTargetService.findSmqRelationsForSmqCode(smqSearched.getSmqCode());
														if (list != null)
															for (SmqRelationTarget smq3 : list) {
																level = getLevelFromValue(smq3.getSmqLevel());
																if((smq3.getSmqLevel() != 5) || (!filterLltFlag && (smq3.getSmqLevel() == 5))) {
																	mapReport.put(cpt++, new ReportLineDataDto(level, smq3.getPtCode() + "", smq3.getPtName(), ".............", smq3.getRelationImpactType(), "", smq3.getPtTermStatus()));
																}
															}
													}
												}
												
												if (level.equals("Child SMQ")) {
													SmqBaseTarget smqSearched22 = smqBaseTargetService.findByCode(Long.parseLong(smqC.getPtCode() + ""));
													if (smqSearched22 != null) {
														List<SmqRelationTarget> test =  smqBaseTargetService.findSmqRelationsForSmqCode(smqSearched22.getSmqCode());
									 					if (test != null) {
															for (SmqRelationTarget tt : test) {
																if (tt.getSmqLevel() == 4) {
																	level = "PT";
																} else if (tt.getSmqLevel() == 5) {
																	level = "LLT";
																} else if (tt.getSmqLevel() == 0) {
																	level = "Child SMQ";
																} 
																if((tt.getSmqLevel() != 5) || (!filterLltFlag && (tt.getSmqLevel() == 5))) {
																	mapReport.put(cpt++, new ReportLineDataDto(level, tt.getPtCode() + "", tt.getPtName(), "...................", 
																			tt.getPtTermScope() + "", tt.getPtTermWeight() + "", tt.getPtTermCategory(), tt.getRelationImpactType(), tt.getPtTermStatus())); 
																	
																	if (level.equals("Child SMQ")) {
																		smqSearched22 = smqBaseTargetService.findByCode(Long.parseLong(tt.getPtCode() + ""));
																		if (smqSearched22 != null) {
																			List<SmqRelationTarget> test3 =  smqBaseTargetService.findSmqRelationsForSmqCode(smqSearched22.getSmqCode());
	 													 					
																			if (test3 != null) {
																				for (SmqRelationTarget tt2 : test3) {
																					if (tt2.getSmqLevel() == 4) {
																						level = "PT";
																					} else if (tt2.getSmqLevel() == 5) {
																						level = "LLT";
																					} else if (tt2.getSmqLevel() == 0) {
																						level = "Child SMQ";
																					} 
																 					
																					if((tt2.getSmqLevel() != 5) || (!filterLltFlag && (tt2.getSmqLevel() == 5))) {
																						mapReport.put(cpt++, new ReportLineDataDto(level, tt2.getPtCode() + "", tt2.getPtName(), ".........................", 
																								tt2.getPtTermScope() + "", tt2.getPtTermWeight() + "", tt2.getPtTermCategory(), tt2.getRelationImpactType(), tt2.getPtTermStatus())); 
		  
																						if (level.equals("Child SMQ")) {
																							smqSearched22 = smqBaseTargetService.findByCode(Long.parseLong(tt2.getPtCode() + ""));
																							if (smqSearched22 != null) {
																								List<SmqRelationTarget> test4 =  smqBaseTargetService.findSmqRelationsForSmqCode(smqSearched22.getSmqCode());
		 																	 					
																								if (test4 != null) {
																									for (SmqRelationTarget tt4 : test4) {
																										if (tt4.getSmqLevel() == 4) {
																											level = "PT";
																										} else if (tt4.getSmqLevel() == 5) {
																											level = "LLT";
																										} else if (tt4.getSmqLevel() == 0) {
																											level = "Child SMQ";
																										} 
																										if((tt4.getSmqLevel() != 5) || (!filterLltFlag && (tt4.getSmqLevel() == 5))) {
																											mapReport.put(cpt++, new ReportLineDataDto(level, tt4.getPtCode() + "", tt4.getPtName(), "...............................", 
																													tt4.getPtTermScope() + "", tt4.getPtTermWeight() + "", tt4.getPtTermCategory(), tt4.getRelationImpactType(), tt4.getPtTermStatus())); 
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
												
												/**
												 * LLT.
												 */
												if (!filterLltFlag && level.equals("LLT")) {
													smqSearched = smqBaseTargetService.findByCode(smqC.getSmqCode());
													if (smqSearched != null) {
														List<Long> codes = new ArrayList<>();
														codes.add(smqSearched.getSmqCode());
 														
 														
														List<SmqBaseTarget> llts = smqBaseTargetService.findChildSmqByParentSmqCodes(codes);
														
 														if (llts != null) {
															for (SmqBaseTarget llt : llts) {
		 														level = getLevelFromValue(llt.getSmqLevel());

																mapReport.put(cpt++, new ReportLineDataDto(level, llt.getSmqCode() + "", llt.getSmqName(), ".......", llt.getImpactType(), "", llt.getSmqStatus())); 
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
						 * HLT.
						 */
						if (relation.getHltCode() != null) {
							List<Long> hltCodesList = new ArrayList<>();
							hltCodesList.add(relation.getHltCode());
							List<MeddraDictHierarchySearchDto> hlts = this.meddraDictService.findByCodes("HLT_", hltCodesList);
							for (MeddraDictHierarchySearchDto hlt : hlts) {
								mapReport.put(cpt++, new ReportLineDataDto("HLT", hlt.getCode() + "", hlt.getTerm(), "......", hlt, getCmqRelationImpactDesc(relation.getRelationImpactType())));
								

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
										mapReport.put(cpt++, new ReportLineDataDto("PT", llt.getCode() + "", llt.getTerm(), "..............", llt, getImpact(llt, "PLL")));
										
										if(!filterLltFlag) {
											/**
											 * LLT.
											 */
											List<MeddraDictHierarchySearchDto> listPT_soc =  meddraDictService.findChildrenByParentCode("LLT_", "PT_", Long.valueOf(llt.getCode()));
											List<Long> llttCodesList = new ArrayList<>();
											for (MeddraDictHierarchySearchDto meddra : listPT_soc) {
												llttCodesList.add(Long.parseLong(meddra.getCode())); 
											}

											List<MeddraDictHierarchySearchDto> llts_soc = this.meddraDictService.findByCodes("LLT_", llttCodesList);
											if (llts_soc != null)
												for (MeddraDictHierarchySearchDto llt_soc : llts_soc) {
													mapReport.put(cpt++, new ReportLineDataDto("LLT", llt_soc.getCode() + "", llt_soc.getTerm(), "....................", llt_soc, getImpact(llt_soc, "LLT")));
													
												}
										}
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
								mapReport.put(cpt++, new ReportLineDataDto("SOC", soc.getCode() + "", soc.getTerm(), "........", soc, getCmqRelationImpactDesc(relation.getRelationImpactType())));
								

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
										mapReport.put(cpt++, new ReportLineDataDto("HLGT", hlgt.getCode() + "", hlgt.getTerm(), ".............", hlgt, getImpact(hlgt, "HLGT")));
										
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
												mapReport.put(cpt++, new ReportLineDataDto("HLT", hlt.getCode() + "", hlt.getTerm(), "..................", hlt, getImpact(hlt, "HLT")));
												 

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
														mapReport.put(cpt++, new ReportLineDataDto("PT", pt.getCode() + "", pt.getTerm(), ".........................", pt, getImpact(pt, "PT")));

														if(!filterLltFlag) {
															/**
															 * LLT.
															 */
															List<MeddraDictHierarchySearchDto> listPT_soc =  meddraDictService.findChildrenByParentCode("LLT_", "PT_", Long.valueOf(pt.getCode()));
															List<Long> llttCodesList = new ArrayList<>();
															for (MeddraDictHierarchySearchDto meddra : listPT_soc) {
																llttCodesList.add(Long.parseLong(meddra.getCode())); 
															}

															List<MeddraDictHierarchySearchDto> llts_soc = this.meddraDictService.findByCodes("LLT_", llttCodesList);
															if (llts_soc != null)
																for (MeddraDictHierarchySearchDto llt_soc : llts_soc) {
																	mapReport.put(cpt++, new ReportLineDataDto("LLT", llt_soc.getCode() + "", llt_soc.getTerm(), "..................................", llt_soc, getImpact(llt_soc, "LLT")));
																
																}
														}
														
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
								mapReport.put(cpt++, new ReportLineDataDto("HLGT", hlgt.getCode() + "", hlgt.getTerm(), "......", hlgt, getCmqRelationImpactDesc(relation.getRelationImpactType())));
								 

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
										mapReport.put(cpt++, new ReportLineDataDto("HLT", hlt.getCode() + "", hlt.getTerm(), "...........", hlt, getImpact(hlt, "HLT")));
										

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
												mapReport.put(cpt++, new ReportLineDataDto("PT", pt.getCode() + "", pt.getTerm(), "....................", pt, getImpact(pt, "PT")));
												
												if(!filterLltFlag) {
													/**
													 * LLT.
													 */
													List<MeddraDictHierarchySearchDto> listPT =  meddraDictService.findChildrenByParentCode("LLT_", "PT_", Long.valueOf(pt.getCode()));
													List<Long> lltCodesList = new ArrayList<>();
													for (MeddraDictHierarchySearchDto meddra : listPT) {
														lltCodesList.add(Long.parseLong(meddra.getCode())); 
													}

													List<MeddraDictHierarchySearchDto> llts = this.meddraDictService.findByCodes("LLT_", lltCodesList);
													if (llts != null)
														for (MeddraDictHierarchySearchDto llt : llts) {
															mapReport.put(cpt++, new ReportLineDataDto("LLT", llt.getCode() + "", llt.getTerm(), ".........................", llt, getImpact(llt, "LLT")));
														}
												}
											}
									}
							}
						}
						

						/**
						 * 
						 * LLT.
						 */
						if (relation.getLltCode() != null) {
		 					List<Long> lltCodesList = new ArrayList<>();
							lltCodesList.add(relation.getLltCode());
							List<MeddraDictHierarchySearchDto> llts = meddraDictService.findByCodes("LLT_", lltCodesList);
							for (MeddraDictHierarchySearchDto llt : llts) {
		 						mapReport.put(cpt++, new ReportLineDataDto("LLT", llt.getCode() + ".....", llt.getTerm(), "", llt, getCmqRelationImpactDesc(relation.getRelationImpactType()))); 
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
 								mapReport.put(cpt++, new ReportLineDataDto("PT", pt.getCode() + "", pt.getTerm(), "", pt, getImpact(pt, "PT")));  

 								if(!filterLltFlag) {
 									/**
 	 								 * LLT.
 	 								 */
 	 								List<MeddraDictHierarchySearchDto> listPT =  meddraDictService.findChildrenByParentCode("LLT_", "PT_", Long.valueOf(pt.getCode()));
 	 								List<Long> hlgtCodesList = new ArrayList<>();
 	 								for (MeddraDictHierarchySearchDto meddra : listPT) {
 	 									hlgtCodesList.add(Long.parseLong(meddra.getCode())); 
 	 								}

 	 								List<MeddraDictHierarchySearchDto> llts = this.meddraDictService.findByCodes("LLT_", hlgtCodesList);
 	 		 						for (MeddraDictHierarchySearchDto llt : llts) {
 	 		 							mapReport.put(cpt++, new ReportLineDataDto("LLT", llt.getCode() + "", llt.getTerm(), "......", llt, getImpact(llt, "LLT")));
 	 								}
 								}
 							}
 						}
					}
 						
 						/**
 						 * 
 						 * LLT.
 						 */
 						if (!filterLltFlag && (relation.getLltCode() != null)) {
 							List<Long> lltCodesList = new ArrayList<>();
 							lltCodesList.add(relation.getLltCode());
 							List<MeddraDictHierarchySearchDto> llts = meddraDictService
 									.findByCodes("LLT_", lltCodesList);
 							for (MeddraDictHierarchySearchDto llt : llts) {
 								mapReport.put(
 										cpt++,
 										new ReportLineDataDto("LLT", llt
 												.getCode() + "", llt.getTerm(),
 												"", llt, getCmqRelationImpactDesc(relation.getRelationImpactType())));
 							}
 						}
 						
 						
 						
 						/**
 						 * TME or PRO.
 						 */

 						if (relation.getCmqCode() != null) {
 							/**
								 * 
								 * SOC
								 */
								if (relation.getSocCode() != null) {
									List<Long> socCodesList = new ArrayList<>();
									socCodesList.add(relation.getSocCode());
									List<MeddraDictHierarchySearchDto> socss = this.meddraDictService.findByCodes("SOC_", socCodesList);
									for (MeddraDictHierarchySearchDto soc : socss) {
										mapReport.put(cpt++, new ReportLineDataDto("SOC", soc.getCode() + "", soc.getTerm(), "........", soc, getCmqRelationImpactDesc(relation.getRelationImpactType())));
										

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
												mapReport.put(cpt++, new ReportLineDataDto("HLGT", hlgt.getCode() + "", hlgt.getTerm(), ".............", hlgt, getImpact(hlgt, "HLGT")));
												
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
														mapReport.put(cpt++, new ReportLineDataDto("HLT", hlt.getCode() + "", hlt.getTerm(), "..................", hlt, getImpact(hlt, "HLT")));
														 

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
																mapReport.put(cpt++, new ReportLineDataDto("PT", pt.getCode() + "", pt.getTerm(), ".........................", pt, getImpact(pt, "PT")));
																if(!filterLltFlag) {
																	/**
																	 * LLT.
																	 */
																	List<MeddraDictHierarchySearchDto> listPT_soc =  meddraDictService.findChildrenByParentCode("LLT_", "PT_", Long.valueOf(pt.getCode()));
																	List<Long> llttCodesList = new ArrayList<>();
																	if (listPT_soc != null) {
																		for (MeddraDictHierarchySearchDto meddra : listPT_soc) {
																			llttCodesList.add(Long.parseLong(meddra.getCode())); 
																		}

																		List<MeddraDictHierarchySearchDto> llts_soc = this.meddraDictService.findByCodes("LLT_", llttCodesList);
																		if (llts_soc != null)
																			for (MeddraDictHierarchySearchDto llt_soc : llts_soc) {
																				mapReport.put(cpt++, new ReportLineDataDto("LLT", llt_soc.getCode() + "", llt_soc.getTerm(), "..................................", llt_soc, getImpact(llt_soc, "LLT")));
																			
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
								 * HLGT.
								 */
								if (relation.getHlgtCode() != null) {
									List<Long> hlgtCodesList = new ArrayList<>();
									hlgtCodesList.add(relation.getHlgtCode());
									List<MeddraDictHierarchySearchDto> socDtos = this.meddraDictService.findByCodes("HLGT_", hlgtCodesList);
									for (MeddraDictHierarchySearchDto hlgt : socDtos) {
										mapReport.put(cpt++, new ReportLineDataDto("HLGT", hlgt.getCode() + "", hlgt.getTerm(), "......", hlgt, relation.getRelationImpactType()));
										 

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
												mapReport.put(cpt++, new ReportLineDataDto("HLT", hlt.getCode() + "", hlt.getTerm(), "...........", hlt, getImpact(hlt, "HLT")));
												

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
														mapReport.put(cpt++, new ReportLineDataDto("PT", pt.getCode() + "", pt.getTerm(), "....................", pt, getImpact(pt, "PT")));
														
														if(!filterLltFlag) {
															/**
															 * LLT.
															 */
															List<MeddraDictHierarchySearchDto> listPT =  meddraDictService.findChildrenByParentCode("LLT_", "PT_", Long.valueOf(pt.getCode()));
															List<Long> lltCodesList = new ArrayList<>();
															for (MeddraDictHierarchySearchDto meddra : listPT) {
																lltCodesList.add(Long.parseLong(meddra.getCode())); 
															}

															List<MeddraDictHierarchySearchDto> llts = this.meddraDictService.findByCodes("LLT_", lltCodesList);
															if (llts != null)
																for (MeddraDictHierarchySearchDto llt : llts) {
																	mapReport.put(cpt++, new ReportLineDataDto("LLT", llt.getCode() + "", llt.getTerm(), ".........................", llt, getImpact(llt, "LLT")));
																}
														}
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
										mapReport.put(cpt++, new ReportLineDataDto("HLT", hlt.getCode() + "", hlt.getTerm(), "......", hlt, relation.getRelationImpactType()));
										

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
												mapReport.put(cpt++, new ReportLineDataDto("PT", llt.getCode() + "", llt.getTerm(), "..............", llt, getImpact(llt, "LLT")));
												
												if(!filterLltFlag) {
													/**
													 * LLT.
													 */
													List<MeddraDictHierarchySearchDto> listPT_soc =  meddraDictService.findChildrenByParentCode("LLT_", "PT_", Long.valueOf(llt.getCode()));
													List<Long> llttCodesList = new ArrayList<>();
													for (MeddraDictHierarchySearchDto meddra : listPT_soc) {
														llttCodesList.add(Long.parseLong(meddra.getCode())); 
													}

													List<MeddraDictHierarchySearchDto> llts_soc = this.meddraDictService.findByCodes("LLT_", llttCodesList);
													if (llts_soc != null)
														for (MeddraDictHierarchySearchDto llt_soc : llts_soc) {
															mapReport.put(cpt++, new ReportLineDataDto("LLT", llt_soc.getCode() + "", llt_soc.getTerm(), "....................", llt_soc, getImpact(llt_soc, "LLT")));
															
														}
												}
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
										mapReport.put(cpt++, new ReportLineDataDto("PT", pt.getCode() + "", pt.getTerm(), "......", pt, relation.getRelationImpactType()));

										if(!filterLltFlag) {
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
													mapReport.put(cpt++, new ReportLineDataDto("LLT", llt.getCode() + "", llt.getTerm(), ".............", llt, getImpact(llt, "LLT")));
												}
										}
									}
								}
 						}
					}
				}
			}
		}
		
 		
		XSSFCellStyle cellStyle = workbook.createCellStyle();
		cellStyle.setFillForegroundColor(HSSFColor.BLUE.index);
		cellStyle.setFillPattern(XSSFCellStyle.SOLID_FOREGROUND);
		cellStyle.setBorderBottom(BorderStyle.MEDIUM);
		
		fillReport(mapReport, cell, row, rowCount, worksheet, cellStyle);
		
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
			content = new DefaultStreamedContent(bais, "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", "Impact_Assessment_Report_" + selectedImpactedCmqList.getCmqName() + ".xlsx");
		} catch (IOException e) {
			e.printStackTrace();
		}

		return content;
	}
	
	private void buildLinesFromLevelRelation(SmqBaseTarget smqSearched, Map<Integer, ReportLineDataDto> mapReport, String level, int cpt, String dots) {
		if (smqSearched != null) {
			List<SmqRelationTarget> list = smqBaseTargetService.findSmqRelationsForSmqCode(smqSearched.getSmqCode());
			if (list != null)
				for (SmqRelationTarget smq3 : list) {
					mapReport.put(cpt++, new ReportLineDataDto(level, smq3.getPtCode() + "", smq3.getPtName(), dots, smq3.getRelationImpactType())); 
				}
		}
	}

	private void buildLinesFromLevel(SmqBaseTarget smqSearched, Map<Integer, ReportLineDataDto> mapReport, String level, int cpt, String dots) {
		if (smqSearched != null) {
			List<SmqRelationTarget> list = smqBaseTargetService.findSmqRelationsForSmqCode(smqSearched.getSmqCode());
			if (list != null) {
				for (SmqRelationTarget smq3 : list) {					
					mapReport.put(cpt++, new ReportLineDataDto(level, smq3.getPtCode() + "", smq3.getPtName(), dots, smq3.getPtTermScope() + "", 
							smq3.getPtTermWeight() + "", smq3.getPtTermCategory(), smq3.getRelationImpactType(), smq3.getPtTermStatus())); 

				}
			}
		}
		
	}

	public String getCmqRelationImpactDesc(String impactType) {
        return refCodeListService.interpretInternalCodeToValueOrDefault(CqtConstants.CODE_LIST_TYPE_CMQ_RELATION_IMPACT_TYPE, impactType, impactType);
    }
	
	public String getImpact(MeddraDictHierarchySearchDto ent, String lvl) {

        if("LLT".equals(lvl)) {
            if(ent.getLltCurrencyChange() != null)
                return refCodeListService.interpretMeddraImpactTypeDesc("llt_currency_change", ent.getLltCurrencyChange());
            else if(ent.getNewLlt() != null)
                return refCodeListService.interpretMeddraImpactTypeDesc("new_llt", ent.getNewLlt());
            else if(ent.getLltNameChanged() != null)
                return refCodeListService.interpretMeddraImpactTypeDesc("llt_name_changed", ent.getLltNameChanged());
            else if(ent.getMovedLlt() != null)
                return refCodeListService.interpretMeddraImpactTypeDesc("moved_llt", ent.getMovedLlt());
            else if(ent.getPromotedLlt() != null)
                return refCodeListService.interpretMeddraImpactTypeDesc("promoted_llt", ent.getPromotedLlt());
            else if(ent.getPrimarySocChange() != null)
                return refCodeListService.interpretMeddraImpactTypeDesc("primary_soc_change", ent.getPrimarySocChange());
            else if(ent.getMovedPt() != null)
                return refCodeListService.interpretMeddraImpactTypeDesc("moved_pt", ent.getMovedPt());
            else if(ent.getDemotedPt() != null)
                return refCodeListService.interpretMeddraImpactTypeDesc("demoted_pt", ent.getDemotedPt());
            else if(ent.getPromotedPt() != null)
                return refCodeListService.interpretMeddraImpactTypeDesc("promoted_pt", ent.getPromotedPt());
            else if(ent.getNewSuccessorPt() != null)
                return refCodeListService.interpretMeddraImpactTypeDesc("new_successor_pt", ent.getNewSuccessorPt());
            else if(ent.getMovedHlt() != null)
                return refCodeListService.interpretMeddraImpactTypeDesc("moved_hlt", ent.getMovedHlt());
            else if(ent.getMergedHlt() != null)
                return refCodeListService.interpretMeddraImpactTypeDesc("merged_hlt", ent.getMergedHlt());
            else if(ent.getMovedHlgt() != null)
                return refCodeListService.interpretMeddraImpactTypeDesc("moved_hlgt", ent.getMovedHlgt());
            else if(ent.getMergedHlt() != null)
                return refCodeListService.interpretMeddraImpactTypeDesc("merged_hlgt", ent.getMovedHlt());
        } else if("PT".equals(lvl)) {
            if(ent.getNewPt() != null)
                return refCodeListService.interpretMeddraImpactTypeDesc("new_pt", ent.getNewPt());
            else if(ent.getPtNameChanged() != null)
                return refCodeListService.interpretMeddraImpactTypeDesc("pt_name_changed", ent.getPtNameChanged());
            else if(ent.getMovedPt() != null)
                return refCodeListService.interpretMeddraImpactTypeDesc("moved_pt", ent.getMovedPt());
            else if(ent.getDemotedPt() != null)
                return refCodeListService.interpretMeddraImpactTypeDesc("demoted_pt", ent.getDemotedPt());
            else if(ent.getPromotedPt() != null)
                return refCodeListService.interpretMeddraImpactTypeDesc("promoted_pt", ent.getPromotedPt());
            else if(ent.getNewSuccessorPt() != null)
                return refCodeListService.interpretMeddraImpactTypeDesc("new_successor_pt", ent.getNewSuccessorPt());
            else if(ent.getPrimarySocChange() != null)
                return refCodeListService.interpretMeddraImpactTypeDesc("primary_soc_change", ent.getPrimarySocChange());
            else if(ent.getMovedHlt() != null)
                return refCodeListService.interpretMeddraImpactTypeDesc("moved_hlt", ent.getMovedHlt());
            else if(ent.getMergedHlt() != null)
                return refCodeListService.interpretMeddraImpactTypeDesc("merged_hlt", ent.getMergedHlt());
            else if(ent.getMovedHlgt() != null)
                return refCodeListService.interpretMeddraImpactTypeDesc("moved_hlgt", ent.getMovedHlgt());
            else if(ent.getMergedHlt() != null)
                return refCodeListService.interpretMeddraImpactTypeDesc("merged_hlgt", ent.getMovedHlt());
        } else if("HLT".equals(lvl)) {
            if(ent.getNewHlt() != null)
                return refCodeListService.interpretMeddraImpactTypeDesc("new_hlt", ent.getNewHlt());
            else if(ent.getHltNameChanged() != null)
                return refCodeListService.interpretMeddraImpactTypeDesc("hlt_name_changed", ent.getHltNameChanged());
            else if(ent.getMovedHlt() != null)
                return refCodeListService.interpretMeddraImpactTypeDesc("moved_hlt", ent.getMovedHlt());
            else if(ent.getMergedHlt() != null)
                return refCodeListService.interpretMeddraImpactTypeDesc("merged_hlt", ent.getMergedHlt());
            else if(ent.getMovedHlgt() != null)
                return refCodeListService.interpretMeddraImpactTypeDesc("moved_hlgt", ent.getMovedHlgt());
            else if(ent.getMergedHlgt() != null)
                return refCodeListService.interpretMeddraImpactTypeDesc("merged_hlgt", ent.getMergedHlgt());
        } else if("HLGT".equals(lvl)) {
            if(ent.getNewHlgt() != null)
                return refCodeListService.interpretMeddraImpactTypeDesc("new_hlgt", ent.getNewHlgt());
            else if(ent.getHlgtNameChanged() != null)
                return refCodeListService.interpretMeddraImpactTypeDesc("hlgt_name_changed", ent.getHlgtNameChanged());
            else if(ent.getMovedHlgt() != null)
                return refCodeListService.interpretMeddraImpactTypeDesc("moved_hlgt", ent.getMovedHlgt());
            else if(ent.getMergedHlgt() != null)
                return refCodeListService.interpretMeddraImpactTypeDesc("merged_hlgt", ent.getMergedHlgt());
        } else if("SOC".equals(lvl)) {
            if(ent.getNewSoc() != null)
                return refCodeListService.interpretMeddraImpactTypeDesc("new_soc", ent.getNewSoc());
            else if(ent.getSocNameChanged() != null)
                return refCodeListService.interpretMeddraImpactTypeDesc("soc_name_changed", ent.getSocNameChanged());
        }
        return "";
    
		
		 
	}
	
	/*public String getImpact(MeddraDictHierarchySearchDto meddra) {
		if (meddra.getPtCode() != null)   {
			if ( meddra.getNewPt() != null) return  meddra.getNewPt();
			if (meddra.getPromotedPt() != null) return meddra.getPromotedPt();
			if (meddra.getDemotedPt() != null) return meddra.getDemotedPt();
			if (meddra.getMovedPt() != null) return meddra.getMovedPt();
			if (meddra.getNewSuccessorPt() != null) return meddra.getNewSuccessorPt();
			if (meddra.getPtNameChanged() != null) return meddra.getPtNameChanged();
		}
		if (meddra.getHlgtCode() != null) {
			if (meddra.getNewHlgt() != null) return meddra.getNewHlgt();
			if (meddra.getMergedHlgt() != null) return meddra.getMergedHlgt();
			if (meddra.getMovedHlgt() != null) return meddra.getMovedHlgt();
			if (meddra.getHlgtNameChanged()!= null) return meddra.getHlgtNameChanged();

		}
		if (meddra.getLltCode() != null)   {
			if (meddra.getNewLlt() != null) return meddra.getNewLlt();
			if (meddra.getDemotedLlt() != null) return meddra.getDemotedLlt();
			if (meddra.getPromotedLlt() != null) return meddra.getPromotedLlt();
			if (meddra.getMovedLlt() != null) return meddra.getMovedLlt();
			if (meddra.getLltCurrencyChange() != null) return meddra.getLltCurrencyChange();
			if (meddra.getLltNameChanged() != null) return meddra.getLltNameChanged();
		}
		if (meddra.getHltCode() != null) {
			if (meddra.getNewHlt() != null) return meddra.getNewHlt();
			if (meddra.getMovedHlt() != null) return meddra.getMovedHlt();
			if (meddra.getHltNameChanged() != null) return meddra.getHltNameChanged();
			if (meddra.getMergedHlt() != null) return meddra.getMergedHlt();
		}
		if (meddra.getSocCode() != null) {
			if (meddra.getNewSoc() != null) return meddra.getNewSoc();
			if (meddra.getSocNameChanged() != null) return meddra.getSocNameChanged();
			if (meddra.getPrimarySocChange() != null) return meddra.getPrimarySocChange();
		}
		return "";
	}*/
	
	

	
	private String getLevelFromValue(Integer smqLevel) {
		String level = "";
		if (smqLevel == 0) {
			level = "Child SMQ";
		} else if (smqLevel == 1) {
			level = "SMQ1";
		} else if (smqLevel == 2) {
			level = "SMQ2";
		} else if (smqLevel == 3) {
			level = "SMQ3";
		} else if (smqLevel == 4) {
			level = "PT";
		} else if (smqLevel == 5) {
			level = "LLT";
		}
		return level;
	}

	private void fillReport(Map<Integer, ReportLineDataDto> mapReport, XSSFCell cell, XSSFRow row, int rowCount, XSSFSheet worksheet, XSSFCellStyle cellStyle) {
		int cpt = 0;
		int rowCountIn= rowCount;
		while (cpt < mapReport.size()) {
			ReportLineDataDto line = mapReport.get(cpt);
			
			row = worksheet.createRow(rowCountIn);
			
			//Printing Relation impact type
			String impact = "";
			if (line.getImpact() != null && !"".equals(line.getImpact()))
				// = line.getImpact();
				impact = refCodeListService.interpretInternalCodeToValueOrDefault(CqtConstants.CODE_LIST_TYPE_CMQ_RELATION_IMPACT_TYPE, line.getImpact(), line.getImpact());
			if (line.getMeddra() != null)
				impact = getMeddraDictTargetImpactDesc(line.getMeddra(), line.getLevel());
			
			if (impact != null)
				impact = impact.toUpperCase();

			// Cell 0
			cell = row.createCell(0);
			cell.setCellValue(line.getDots() + line.getTerm());
//			if (line.getImpact() != null && !"".equals(line.getImpact()))
//				cell.setCellStyle(cellStyle);   TODO to enabled when color is needed

			// Cell 1
			cell = row.createCell(1);
			cell.setCellValue(line.getCode());

			// Cell 2
			cell = row.createCell(2);
			cell.setCellValue(line.getLevel());	
			
			cell = row.createCell(3);
			cell.setCellValue(line.getCategory());
			cell = row.createCell(4);
			cell.setCellValue(line.getWeight());
			
			// Cell 5
			cell = row.createCell(5);
			cell.setCellValue(line.getScope() != null ? interpretCqtBaseScope(line.getScope()) : "");	
			
			// Cell 6
			cell = row.createCell(6);
			cell.setCellValue(impact);
			
			// Cell 7
		//	cell = row.createCell(7);
			//cell.setCellValue(line.getStatus());
		//	System.out.println("_____________________ PT STATUS: " + line.getStatus());
			
			rowCountIn++;
			
			cpt++;
		}
		
		
	}
	 public String interpretCqtBaseScope(String scopeVal) {
	        if("2".equals(scopeVal))
	            return "Narrow";
	        else if("1".equals(scopeVal))
	            return "Broad";
	        else if("3".equals(scopeVal))
	            return "Child Narrow";
	        else if("4".equals(scopeVal))
	            return "Full";
	        return "";
	    }
	
	
	 private String getMeddraDictTargetImpactDesc(MeddraDictHierarchySearchDto ent, String lvl) {
	        if("LLT".equals(lvl)) {
	            if(ent.getLltCurrencyChange() != null)
	                return refCodeListService.interpretMeddraImpactTypeDesc("llt_currency_change", ent.getLltCurrencyChange());
	            else if(ent.getNewLlt() != null)
	                return refCodeListService.interpretMeddraImpactTypeDesc("new_llt", ent.getNewLlt());
	            else if(ent.getLltNameChanged() != null)
	                return refCodeListService.interpretMeddraImpactTypeDesc("llt_name_changed", ent.getLltNameChanged());
	            else if(ent.getMovedLlt() != null)
	                return refCodeListService.interpretMeddraImpactTypeDesc("moved_llt", ent.getMovedLlt());
	            else if(ent.getPromotedLlt() != null)
	                return refCodeListService.interpretMeddraImpactTypeDesc("promoted_llt", ent.getPromotedLlt());
	            else if(ent.getPrimarySocChange() != null)
	                return refCodeListService.interpretMeddraImpactTypeDesc("primary_soc_change", ent.getPrimarySocChange());
	            else if(ent.getMovedPt() != null)
	                return refCodeListService.interpretMeddraImpactTypeDesc("moved_pt", ent.getMovedPt());
	            else if(ent.getDemotedPt() != null)
	                return refCodeListService.interpretMeddraImpactTypeDesc("demoted_pt", ent.getDemotedPt());
	            else if(ent.getPromotedPt() != null)
	                return refCodeListService.interpretMeddraImpactTypeDesc("promoted_pt", ent.getPromotedPt());
	            else if(ent.getNewSuccessorPt() != null)
	                return refCodeListService.interpretMeddraImpactTypeDesc("new_successor_pt", ent.getNewSuccessorPt());
	            else if(ent.getMovedHlt() != null)
	                return refCodeListService.interpretMeddraImpactTypeDesc("moved_hlt", ent.getMovedHlt());
	            else if(ent.getMergedHlt() != null)
	                return refCodeListService.interpretMeddraImpactTypeDesc("merged_hlt", ent.getMergedHlt());
	            else if(ent.getMovedHlgt() != null)
	                return refCodeListService.interpretMeddraImpactTypeDesc("moved_hlgt", ent.getMovedHlgt());
	            else if(ent.getMergedHlt() != null)
	                return refCodeListService.interpretMeddraImpactTypeDesc("merged_hlgt", ent.getMovedHlt());
	        } else if("PT".equals(lvl)) {
	            if(ent.getNewPt() != null)
	                return refCodeListService.interpretMeddraImpactTypeDesc("new_pt", ent.getNewPt());
	            else if(ent.getPtNameChanged() != null)
	                return refCodeListService.interpretMeddraImpactTypeDesc("pt_name_changed", ent.getPtNameChanged());
	            else if(ent.getMovedPt() != null)
	                return refCodeListService.interpretMeddraImpactTypeDesc("moved_pt", ent.getMovedPt());
	            else if(ent.getDemotedPt() != null)
	                return refCodeListService.interpretMeddraImpactTypeDesc("demoted_pt", ent.getDemotedPt());
	            else if(ent.getPromotedPt() != null)
	                return refCodeListService.interpretMeddraImpactTypeDesc("promoted_pt", ent.getPromotedPt());
	            else if(ent.getNewSuccessorPt() != null)
	                return refCodeListService.interpretMeddraImpactTypeDesc("new_successor_pt", ent.getNewSuccessorPt());
	            else if(ent.getPrimarySocChange() != null)
	                return refCodeListService.interpretMeddraImpactTypeDesc("primary_soc_change", ent.getPrimarySocChange());
	            else if(ent.getMovedHlt() != null)
	                return refCodeListService.interpretMeddraImpactTypeDesc("moved_hlt", ent.getMovedHlt());
	            else if(ent.getMergedHlt() != null)
	                return refCodeListService.interpretMeddraImpactTypeDesc("merged_hlt", ent.getMergedHlt());
	            else if(ent.getMovedHlgt() != null)
	                return refCodeListService.interpretMeddraImpactTypeDesc("moved_hlgt", ent.getMovedHlgt());
	            else if(ent.getMergedHlt() != null)
	                return refCodeListService.interpretMeddraImpactTypeDesc("merged_hlgt", ent.getMovedHlt());
	        } else if("HLT".equals(lvl)) {
	            if(ent.getNewHlt() != null)
	                return refCodeListService.interpretMeddraImpactTypeDesc("new_hlt", ent.getNewHlt());
	            else if(ent.getHltNameChanged() != null)
	                return refCodeListService.interpretMeddraImpactTypeDesc("hlt_name_changed", ent.getHltNameChanged());
	            else if(ent.getMovedHlt() != null)
	                return refCodeListService.interpretMeddraImpactTypeDesc("moved_hlt", ent.getMovedHlt());
	            else if(ent.getMergedHlt() != null)
	                return refCodeListService.interpretMeddraImpactTypeDesc("merged_hlt", ent.getMergedHlt());
	            else if(ent.getMovedHlgt() != null)
	                return refCodeListService.interpretMeddraImpactTypeDesc("moved_hlgt", ent.getMovedHlgt());
	            else if(ent.getMergedHlgt() != null)
	                return refCodeListService.interpretMeddraImpactTypeDesc("merged_hlgt", ent.getMergedHlgt());
	        } else if("HLGT".equals(lvl)) {
	            if(ent.getNewHlgt() != null)
	                return refCodeListService.interpretMeddraImpactTypeDesc("new_hlgt", ent.getNewHlgt());
	            else if(ent.getHlgtNameChanged() != null)
	                return refCodeListService.interpretMeddraImpactTypeDesc("hlgt_name_changed", ent.getHlgtNameChanged());
	            else if(ent.getMovedHlgt() != null)
	                return refCodeListService.interpretMeddraImpactTypeDesc("moved_hlgt", ent.getMovedHlgt());
	            else if(ent.getMergedHlgt() != null)
	                return refCodeListService.interpretMeddraImpactTypeDesc("merged_hlgt", ent.getMergedHlgt());
	        } else if("SOC".equals(lvl)) {
	            if(ent.getNewSoc() != null)
	                return refCodeListService.interpretMeddraImpactTypeDesc("new_soc", ent.getNewSoc());
	            else if(ent.getSocNameChanged() != null)
	                return refCodeListService.interpretMeddraImpactTypeDesc("soc_name_changed", ent.getSocNameChanged());
	        }
	        return "";
	    }

	/*private void buildCells(String level, String codeTerm, String term, XSSFCell cell, XSSFRow row) {
		// Cell 0
		cell = row.createCell(0);
		cell.setCellValue(term);

		// Cell 1
		cell = row.createCell(1);
		cell.setCellValue(codeTerm);

		// Cell 2
		cell = row.createCell(2);
		cell.setCellValue(level);
	}*/
	
	/*private void buildChildCells(String level, String codeTerm, String term, XSSFCell cell, XSSFRow row, String dots) {
		// Cell 0
		cell = row.createCell(0);
		cell.setCellValue(dots + term);

		// Cell 1
		cell = row.createCell(1);
		cell.setCellValue(codeTerm);

		// Cell 2
		cell = row.createCell(2);
		cell.setCellValue(level);
	}*/


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
	
	private String returnState(CmqBaseTarget cmq) {
		String state = "";
		if(CmqBaseTarget.CMQ_STATE_PENDING_IA.equalsIgnoreCase(cmq.getCmqState()) || "PENDING".equalsIgnoreCase(cmq.getCmqState())) {
			state = CmqBaseTarget.CMQ_STATE_PENDING_IA;
		} else if (CmqBaseTarget.CMQ_STATE_PUBLISHED.equalsIgnoreCase(cmq.getCmqState()) || CmqBaseTarget.CMQ_STATE_PUBLISHED_IA.equalsIgnoreCase(cmq.getCmqState())) {
			state = CmqBaseTarget.CMQ_STATE_PUBLISHED_IA;
		} else if (CmqBaseTarget.CMQ_STATE_APPROVED_IA.equalsIgnoreCase(cmq.getCmqState()) || "APPROVED".equalsIgnoreCase(cmq.getCmqState())){
			state = CmqBaseTarget.CMQ_STATE_APPROVED_IA;
		} else if (CmqBaseTarget.CMQ_STATE_REVIEWED_IA.equalsIgnoreCase(cmq.getCmqState()) || "REVIEWED".equalsIgnoreCase(cmq.getCmqState())){
			state = CmqBaseTarget.CMQ_STATE_REVIEWED_IA;
		} else if ("PUBLISHED".equalsIgnoreCase(cmq.getCmqState())){
			state = "PUBLISHED";
		} else {
			state = "UNKNOWN";
		}
		return state;
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
	
	/**
	 * Find Approved IA CmqTarget.
	 * @return List<CmqBaseTarget>
	 */
	@Override
	public List<CmqBaseTarget> findApprovedCmqs() {
		List<CmqBaseTarget> retVal = null;
		String queryString = "from CmqBaseTarget c where upper(c.cmqState) = upper('Approved IA') and upper(c.cmqStatus) = upper('P') order by upper(c.cmqName) asc ";
		EntityManager entityManager = this.cqtEntityManagerFactory.getEntityManager();
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
	public List<CmqBaseTarget> findChildCmqsByCodes(List<Long> codes) {
		List<CmqBaseTarget> retVal = null;
		String queryString = "from CmqBaseTarget c where c.cmqParentCode in (:codeList) ";
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
	
	@Override
	public List<CmqBaseTarget> findParentCmqsByCodes(List<Long> codes) {
		List<CmqBaseTarget> retVal = null;
		String queryString = "from CmqBaseTarget c where c.cmqCode in (:codeList) ";
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
	
	@SuppressWarnings("unchecked")
    @Override
	public List<CmqBaseTarget> findPublishedCmqs() {
		List<CmqBaseTarget> retVal = null;
		String queryString = "from CmqBaseTarget c where upper(c.cmqState) = upper('PUBLISHED IA') and upper(c.cmqStatus) = upper('P') ";
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
    public boolean isVersionUpgradePending() {
        EntityManager entityManager = this.cqtEntityManagerFactory.getEntityManager();
        try {
            Query query = entityManager.createQuery("select count(*) from CmqBaseTarget c");
            Long retVal = (Long)query.getSingleResult();
            if(retVal!=null && retVal>0)
                return true;
        } catch (Exception e) {
            return false;
        } finally {
            this.cqtEntityManagerFactory.closeEntityManager(entityManager);
        }
		return false;
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

	public ICmqRelationTargetService getCmqRelationTargetService() {
		return cmqRelationTargetService;
	}

	public void setCmqRelationTargetService(
			ICmqRelationTargetService cmqRelationTargetService) {
		this.cmqRelationTargetService = cmqRelationTargetService;
	}

	public ISmqBaseTargetService getSmqBaseTargetService() {
		return smqBaseTargetService;
	}

	public void setSmqBaseTargetService(ISmqBaseTargetService smqBaseTargetService) {
		this.smqBaseTargetService = smqBaseTargetService;
	}

	public IMeddraDictTargetService getMeddraDictService() {
		return meddraDictService;
	}

	public void setMeddraDictService(IMeddraDictTargetService meddraDictService) {
		this.meddraDictService = meddraDictService;
	}

	public ISmqBaseService getSmqBaseService() {
		return smqBaseService;
	}

	public void setSmqBaseService(ISmqBaseService smqBaseService) {
		this.smqBaseService = smqBaseService;
	}

	public IRefCodeListService getRefCodeListService() {
		return refCodeListService;
	}
 

	public void setRefCodeListService(IRefCodeListService refCodeListService) {
		this.refCodeListService = refCodeListService;
	}
}
