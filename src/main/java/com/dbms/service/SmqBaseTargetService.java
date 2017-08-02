package com.dbms.service;

import com.dbms.csmq.CSMQBean;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
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
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.metamodel.EntityType;
import javax.persistence.metamodel.Metamodel;

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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dbms.entity.cqt.CmqBase190;
import com.dbms.entity.cqt.CmqBaseTarget;
import com.dbms.entity.cqt.CmqRelationTarget;
import com.dbms.entity.cqt.SmqBase190;
import com.dbms.entity.cqt.SmqBaseTarget;
import com.dbms.entity.cqt.SmqBaseTarget;
import com.dbms.entity.cqt.SmqRelation190;
import com.dbms.entity.cqt.SmqRelationTarget;
import com.dbms.entity.cqt.dtos.MeddraDictHierarchySearchDto;
import com.dbms.entity.cqt.dtos.ReportLineDataDto;
import com.dbms.entity.cqt.dtos.SMQReverseHierarchySearchDto;
import com.dbms.service.base.CqtPersistenceService;
import com.dbms.util.CmqUtils;
import com.dbms.util.CqtConstants;

import org.apache.commons.collections4.CollectionUtils;

/**
 * @author Jay G.(jayshanchn@hotmail.com)
 * @date Feb 11, 2017 8:00:48 AM
 **/
@ManagedBean(name = "SmqBaseTargetService")
@ApplicationScoped
public class SmqBaseTargetService extends CqtPersistenceService<SmqBaseTarget> implements ISmqBaseTargetService {

	private static final Logger LOG = LoggerFactory.getLogger(SmqBaseTargetService.class);
	
	@ManagedProperty("#{MeddraDictTargetService}")
	private IMeddraDictTargetService meddraDictService;

	@Override
	@SuppressWarnings("unchecked")
	public List<SmqBaseTarget> findByLevelAndTerm(Integer level, String searchTerm) {
		List<SmqBaseTarget> retVal = null;
		StringBuilder sb = new StringBuilder();
		sb.append("from SmqBaseTarget c where c.smqLevel = :smqLevel ");
		if(!StringUtils.isBlank(searchTerm)) {
			sb.append("and upper(c.smqName) like :smqName");
		}
		EntityManager entityManager = this.cqtEntityManagerFactory.getEntityManager();
		try {
			Query query = entityManager.createQuery(sb.toString());
			query.setParameter("smqLevel", level);
			
			if(!StringUtils.isBlank(searchTerm)) {
				query.setParameter("smqName", searchTerm.toUpperCase());
			}
			retVal = query.getResultList();
		} catch (Exception e) {
			StringBuilder msg = new StringBuilder();
			msg
					.append("An error occurred while fetching types from SmqBaseTarget on smqLevel ")
					.append(level)
					.append(" with smqName like ")
					.append(searchTerm.toUpperCase())
					.append(" Query used was ->")
					.append(sb.toString());
			LOG.error(msg.toString(), e);
		} finally {
			this.cqtEntityManagerFactory.closeEntityManager(entityManager);
		}
		return retVal;
	}

	@Override
	public Long findSmqRelationsCountForSmqCode(Long smqCode) {
		Long retVal = null;
		StringBuilder sb = new StringBuilder();
		sb.append("select count(*) from SmqRelationTarget c where c.smqCode = :smqCode");
		
		EntityManager entityManager = this.cqtEntityManagerFactory.getEntityManager();
		try {
			Query query = entityManager.createQuery(sb.toString());
			query.setParameter("smqCode", smqCode);
			retVal = (Long)query.getSingleResult();
		} catch (Exception e) {
			StringBuilder msg = new StringBuilder();
			msg
					.append("An error occurred while findSmqRelationsCountForSmqCode ")
					.append(smqCode)
					.append(" Query used was ->")
					.append(sb.toString());
			LOG.error(msg.toString(), e);
		} finally {
			this.cqtEntityManagerFactory.closeEntityManager(entityManager);
		}
		return retVal;
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public List<Map<String, Object>> findSmqRelationsCountForSmqCodes(List<Long> smqCodes) {
		List<Map<String, Object>> retVal = null;
        
        if(CollectionUtils.isEmpty(smqCodes))
            return null;
		
        String queryString = CmqUtils.convertArrayToTableWith(smqCodes, "tempSmqCodes", "code")
                + " select count(*) as COUNT, SMQ_CODE"
                + " from SMQ_RELATIONS_TARGET smqTbl"
                + " inner join tempSmqCodes on tempSmqCodes.code=smqTbl.SMQ_CODE"
                + " group by SMQ_CODE";
		
		EntityManager entityManager = this.cqtEntityManagerFactory.getEntityManager();
		Session session = entityManager.unwrap(Session.class);
		try {
			SQLQuery query = session.createSQLQuery(queryString);
			query.addScalar("SMQ_CODE", StandardBasicTypes.LONG);
			query.addScalar("COUNT", StandardBasicTypes.LONG);
			query.setParameterList("smqCodes", smqCodes);
			query.setResultTransformer(Transformers.ALIAS_TO_ENTITY_MAP);
			retVal = query.list();
		} catch (Exception e) {
			StringBuilder msg = new StringBuilder();
			msg
					.append("An error occurred while findSmqRelationsCountForSmqCodes ")
					.append(smqCodes)
					.append(" Query used was ->")
					.append(queryString);
			LOG.error(msg.toString(), e);
		} finally {
			this.cqtEntityManagerFactory.closeEntityManager(entityManager);
		}
		return retVal;
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public List<SMQReverseHierarchySearchDto> findReverseParentByChildCode(Long smqCode) {
		List<SMQReverseHierarchySearchDto> retVal = null;
		StringBuilder sb = new StringBuilder();
		sb.append("select a.SMQ_CODE as smqCode, a.SMQ_NAME as smqName, a.SMQ_LEVEL as smqLevel, a.SMQ_DESCRIPTION as smqDescription, " 
					+ " a.SMQ_SOURCE as smqSource, a.SMQ_NOTE as smqNote, a.SMQ_STATUS as smqStatus, a.SMQ_ALGORITHM as smqAlgorithm, " 
					+ " a.DICTIONARY_VERSION as dictionaryVersion, a.IMPACT_TYPE as impactType, a.SMQ_ID as smqId, b.PT_TERM_SCOPE as ptTermScope, "
					+ " b.PT_TERM_WEIGHT as ptTermWeight, b.PT_TERM_CATEGORY as ptTermCategory, b.PT_TERM_STATUS as ptTermStatus from SMQ_BASE_TARGET a, "
					+ " SMQ_RELATIONS_TARGET b where a.SMQ_CODE = b.SMQ_CODE and b. PT_CODE = :smqCode"); 
		EntityManager entityManager = this.cqtEntityManagerFactory.getEntityManager();
		Session session = entityManager.unwrap(Session.class);
		try {
			SQLQuery query = session.createSQLQuery(sb.toString());
			query.addScalar("smqCode", StandardBasicTypes.LONG);
			query.addScalar("smqName", StandardBasicTypes.STRING);
			query.addScalar("smqLevel", StandardBasicTypes.STRING);
			query.addScalar("smqDescription", StandardBasicTypes.STRING);
			query.addScalar("smqSource", StandardBasicTypes.STRING);
			query.addScalar("smqNote", StandardBasicTypes.STRING);
			query.addScalar("smqStatus", StandardBasicTypes.STRING);
			query.addScalar("smqAlgorithm", StandardBasicTypes.STRING);
			query.addScalar("dictionaryVersion", StandardBasicTypes.STRING);
			query.addScalar("impactType", StandardBasicTypes.STRING);
			query.addScalar("smqId", StandardBasicTypes.LONG);
			query.addScalar("ptTermScope", StandardBasicTypes.INTEGER);
			query.addScalar("ptTermWeight", StandardBasicTypes.INTEGER);
			query.addScalar("ptTermCategory", StandardBasicTypes.STRING);
			query.addScalar("ptTermStatus", StandardBasicTypes.STRING);
			
			query.setParameter("smqCode", smqCode);
			query.setFetchSize(400);
 			query.setResultTransformer(Transformers.aliasToBean(SMQReverseHierarchySearchDto.class));
			query.setCacheable(true);
			retVal = query.list();
		} catch (Exception e) {
			StringBuilder msg = new StringBuilder();
			msg
					.append("An error occurred while fetching findReverseParentByChildCode smqCode ")
					.append(smqCode)
					.append(" with smqName like ")
					.append(" Query used was ->")
					.append(sb.toString());
			LOG.error(msg.toString(), e);
		} finally {
			this.cqtEntityManagerFactory.closeEntityManager(entityManager);
		}
		return retVal;
	}
	
	@Override
	public List<Map<String, Object>> findParentCountSmqCountByChildSmqCodes(List<Long> smqCodes) {
		List<Map<String, Object>> retVal = null;
		
        if(CollectionUtils.isEmpty(smqCodes))
            return null;
        
        String queryString = CmqUtils.convertArrayToTableWith(smqCodes, "tempSmqCodes", "code")
                + " select count(*) as COUNT, PT_CODE"
                + " from SMQ_RELATIONS_TARGET smqTbl"
                + " inner join tempSmqCodes on tempSmqCodes.code=smqTbl.PT_CODE"
                + " group by PT_CODE";
        
		EntityManager entityManager = this.cqtEntityManagerFactory.getEntityManager();
        Session session = entityManager.unwrap(Session.class);
		try {
            SQLQuery query = session.createSQLQuery(queryString);
			query.addScalar("PT_CODE", StandardBasicTypes.LONG);
			query.addScalar("COUNT", StandardBasicTypes.LONG);
            
            query.setResultTransformer(Transformers.ALIAS_TO_ENTITY_MAP);
			query.setCacheable(true);
			retVal = query.list();
		} catch (Exception e) {
			StringBuilder msg = new StringBuilder();
			msg
					.append("An error occurred while findChildSmqCountByParentSmqCode ")
					.append(smqCodes)
					.append(" Query used was ->")
					.append(queryString);
			LOG.error(msg.toString(), e);
		} finally {
			this.cqtEntityManagerFactory.closeEntityManager(entityManager);
		}
		return retVal;
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public List<SmqRelationTarget> findSmqRelationsForSmqCode(Long smqCode) {
		List<SmqRelationTarget> retVal = null;
		StringBuilder sb = new StringBuilder();
		sb.append("from SmqRelationTarget c where c.smqCode = :smqCode order by c.smqLevel asc");
		
		EntityManager entityManager = this.cqtEntityManagerFactory.getEntityManager();
		try {
			Query query = entityManager.createQuery(sb.toString());
			query.setParameter("smqCode", smqCode);
			retVal = query.getResultList();
		} catch (Exception e) {
			StringBuilder msg = new StringBuilder();
			msg
					.append("An error occurred while findSmqRelationsForSmqCode ")
					.append(smqCode)
					.append(" Query used was ->")
					.append(sb.toString());
			LOG.error(msg.toString(), e);
		} finally {
			this.cqtEntityManagerFactory.closeEntityManager(entityManager);
		}
		return retVal;
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public List<SmqRelationTarget> findSmqRelationsForSmqCodeAndScope(Long smqCode, String scope) {
		List<SmqRelationTarget> retVal = null;
		StringBuilder sb = new StringBuilder();
		if (StringUtils.isNotBlank(scope) && (scope.equals(CSMQBean.SCOPE_NARROW) || scope.equals(CSMQBean.SCOPE_BROAD))) {
			sb.append("from SmqRelationTarget c where c.smqCode = :smqCode and (c.ptTermScope = 0 or c.ptTermScope = :ptTermScope) order by c.smqLevel asc, c.ptName asc");
		} else {
			sb.append("from SmqRelationTarget c where c.smqCode = :smqCode order by c.smqLevel asc, c.ptName asc");
		}
		
		EntityManager entityManager = this.cqtEntityManagerFactory.getEntityManager();
		try {
			Query query = entityManager.createQuery(sb.toString());
			query.setParameter("smqCode", smqCode);
			if (StringUtils.isNotBlank(scope) && (scope.equals(CSMQBean.SCOPE_NARROW) || scope.equals(CSMQBean.SCOPE_BROAD))) {
				query.setParameter("ptTermScope", Integer.parseInt(scope));
			}
			query.setHint("org.hibernate.cacheable", true);
			retVal = query.getResultList();
		} catch (Exception e) {
			StringBuilder msg = new StringBuilder();
			msg
					.append("An error occurred while findSmqRelationsForSmqCodeAndScope ")
					.append(smqCode)
					.append(" Query used was ->")
					.append(sb.toString());
			LOG.error(msg.toString(), e);
		} finally {
			this.cqtEntityManagerFactory.closeEntityManager(entityManager);
		}
		return retVal;
	}
	
	@Override
	public SmqRelationTarget findSmqRelationBySmqAndPtCode(Long smqCode, Integer ptCode) {
		SmqRelationTarget retVal = null;
		StringBuilder sb = new StringBuilder();
		sb.append("from SmqRelationTarget c where c.smqCode = :smqCode and c.ptCode = :ptCode");
		
		EntityManager entityManager = this.cqtEntityManagerFactory.getEntityManager();
		try {
			Query query = entityManager.createQuery(sb.toString());
			query.setParameter("smqCode", smqCode);
			query.setParameter("ptCode", ptCode);
			retVal = (SmqRelationTarget) query.getSingleResult();
		} catch (Exception e) {
			StringBuilder msg = new StringBuilder();
			msg
					.append("An error occurred while findSmqRelationsForSmqCode ")
					.append(smqCode)
					.append(" Query used was ->")
					.append(sb.toString());
			LOG.error(msg.toString(), e);
		} finally {
			this.cqtEntityManagerFactory.closeEntityManager(entityManager);
		}
		return retVal;
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public List<SmqRelationTarget> findSmqRelationsForSmqCodes(List<Long> smqCodes) {
		List<SmqRelationTarget> retVal = null;
		StringBuilder sb = new StringBuilder();
		sb.append("from SmqRelationTarget c where c.smqCode in (:smqCodes) order by c.smq_Code asc, c.ptName asc");
		
		EntityManager entityManager = this.cqtEntityManagerFactory.getEntityManager();
		try {
			Query query = entityManager.createQuery(sb.toString());
			query.setParameter("smqCode", smqCodes);
			retVal = query.getResultList();
		} catch (Exception e) {
			StringBuilder msg = new StringBuilder();
			msg
					.append("An error occurred while findSmqRelationsForSmqCodes ")
					.append(smqCodes)
					.append(" Query used was ->")
					.append(sb.toString());
			LOG.error(msg.toString(), e);
		} finally {
			this.cqtEntityManagerFactory.closeEntityManager(entityManager);
		}
		return retVal;
	}
	
	@Override
	public List<SmqBaseTarget> findChildSmqByParentSmqCodes(List<Long> smqCodes) {
		List<SmqBaseTarget> retVal = null;
		StringBuilder sb = new StringBuilder();
		sb.append("from SmqBaseTarget c where c.smqParentCode in (:smqParentCodes) order by c.smqParentCode");
		
		EntityManager entityManager = this.cqtEntityManagerFactory.getEntityManager();
		try {
			Query query = entityManager.createQuery(sb.toString());
			query.setParameter("smqParentCodes", smqCodes);
			retVal = query.getResultList();
		} catch (Exception e) {
			StringBuilder msg = new StringBuilder();
			msg
					.append("An error occurred while findChildSmqByParentSmqCodes ")
					.append(smqCodes)
					.append(" Query used was ->")
					.append(sb.toString());
			LOG.error(msg.toString(), e);
		} finally {
			this.cqtEntityManagerFactory.closeEntityManager(entityManager);
		}
		return retVal;
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public List<SmqBaseTarget> findChildSmqByParentSmqCode(Long smqCode) {
		List<SmqBaseTarget> retVal = null;
		StringBuilder sb = new StringBuilder();
		sb.append("from SmqBaseTarget c where c.smqParentCode = :smqParentCode");
		
		EntityManager entityManager = this.cqtEntityManagerFactory.getEntityManager();
		try {
			Query query = entityManager.createQuery(sb.toString());
			query.setParameter("smqParentCode", smqCode);
			retVal = query.getResultList();
		} catch (Exception e) {
			StringBuilder msg = new StringBuilder();
			msg
					.append("An error occurred while findChildSmqByParentSmqCode ")
					.append(smqCode)
					.append(" Query used was ->")
					.append(sb.toString());
			LOG.error(msg.toString(), e);
		} finally {
			this.cqtEntityManagerFactory.closeEntityManager(entityManager);
		}
		return retVal;
	}
	
	@Override
	public Long findChildSmqCountByParentSmqCode(Long smqCode) {
		Long retVal = null;
		StringBuilder sb = new StringBuilder();
		sb.append("select count(*) from SmqBaseTarget c where c.smqParentCode = :smqParentCode");
		
		EntityManager entityManager = this.cqtEntityManagerFactory.getEntityManager();
		try {
			Query query = entityManager.createQuery(sb.toString());
			query.setParameter("smqParentCode", smqCode);
			retVal = (Long)query.getSingleResult();
		} catch (Exception e) {
			StringBuilder msg = new StringBuilder();
			msg
					.append("An error occurred while findChildSmqCountByParentSmqCode ")
					.append(smqCode)
					.append(" Query used was ->")
					.append(sb.toString());
			LOG.error(msg.toString(), e);
		} finally {
			this.cqtEntityManagerFactory.closeEntityManager(entityManager);
		}
		return retVal;
	}
	
	@Override
	public SmqBaseTarget findByCode(Long smqCode) {
		SmqBaseTarget retVal = null;
		String queryString = "from SmqBaseTarget c where c.smqCode = :smqCode ";
		EntityManager entityManager = this.cqtEntityManagerFactory.getEntityManager();
		try {
			Query query = entityManager.createQuery("from SmqBaseTarget c where c.smqCode = :smqCode ");
			query.setParameter("smqCode", smqCode);
			retVal = (SmqBaseTarget) query.getSingleResult();
		} catch (Exception e) {
			StringBuilder msg = new StringBuilder();
			msg
					.append("An error occurred while findSmqRelationsForSmqCode ")
					.append(smqCode)
					.append(" Query used was ->")
					.append(queryString);
			LOG.error(msg.toString(), e);
		} finally {
			this.cqtEntityManagerFactory.closeEntityManager(entityManager);
		}
		return retVal;
	}
    
    @SuppressWarnings("unchecked")
	@Override
	public List<SmqBaseTarget> findByCodes(List<Long> smqCodes) {
		List<SmqBaseTarget> retVal = null;
		String queryString = "from SmqBaseTarget c where c.smqCode in (:smqCodes) ";
		EntityManager entityManager = this.cqtEntityManagerFactory.getEntityManager();
		try {
			Query query = entityManager.createQuery(queryString);
			query.setParameter("smqCodes", smqCodes);
			query.setHint("org.hibernate.cacheable", true);
			retVal = query.getResultList();
		} catch (Exception e) {
			StringBuilder msg = new StringBuilder();
			msg
					.append("An error occurred while findByCodes ")
					.append(smqCodes)
					.append(" Query used was ->")
					.append(queryString);
			LOG.error(msg.toString(), e);
		} finally {
			this.cqtEntityManagerFactory.closeEntityManager(entityManager);
		}
		return retVal;
	}
	
	@Override
	public List<SmqBaseTarget> findImpactedWithPaginated(int first, int pageSize, String sortField
														, SortOrder sortOrder, Map<String, Object> filters) {
		List<SmqBaseTarget> retVal = null;
		EntityManager entityManager = this.cqtEntityManagerFactory.getEntityManager();
		try {
			CriteriaBuilder cb = entityManager.getCriteriaBuilder();
			CriteriaQuery<SmqBaseTarget> cq = cb.createQuery(SmqBaseTarget.class);
			Root<SmqBaseTarget> smqRoot = cq.from(SmqBaseTarget.class);

			List<Predicate> pred = new ArrayList<Predicate>();
			
			pred.add(cb.equal(smqRoot.get("impactType"), CSMQBean.IMPACT_TYPE_IMPACTED));
			
			if(filters.containsKey("smqName") && filters.get("smqName") != null) {
                String f = ((String)filters.get("smqName")).toLowerCase();
                f = f.contains("%") ? f : ("%" + f + "%");
				pred.add(cb.like(cb.lower(smqRoot.<String>get("smqName")), f));
            }
			
			if(filters.containsKey("smqLevel") && filters.get("smqLevel") != null)
				pred.add(cb.equal(smqRoot.get("smqLevel"), filters.get("smqLevel")));
            
            if(filters.containsKey("smqCode") && filters.get("smqCode") != null) {
                String f = ((String)filters.get("smqCode"));
                f = f.contains("%") ? f : ("%" + f + "%");
				pred.add(cb.like(smqRoot.get("smqCode").as(String.class), f));
            }
			
			cq.where(cb.and(pred.toArray(new Predicate[0])));
			cq.orderBy(cb.asc(smqRoot.get("smqName")));
			
			TypedQuery<SmqBaseTarget> tq = entityManager.createQuery(cq);
			
			if (pageSize >= 0){
	            tq.setMaxResults(pageSize);
	        }
	        if (first >= 0){
	            tq.setFirstResult(first);
	        }
			
			retVal = tq.getResultList();
		} catch (Exception e) {
			StringBuilder msg = new StringBuilder();
			msg.append("An error occurred while fetching paginated impacted SmqBaseTarget.");
			LOG.error(msg.toString(), e);
		} finally {
			this.cqtEntityManagerFactory.closeEntityManager(entityManager);
		}
		return retVal;
	}
	
	/* (non-Javadoc)
	 * @see com.dbms.service.ISmqBaseTargetService#findImpactedCount()
	 */
	@Override
	public Long findImpactedCount() {
		Long retVal = null;
		StringBuilder sb = new StringBuilder();
		sb.append("select count(*) from SmqBaseTarget c where c.impactType = 'IMPACTED'");
		
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
	 * @see com.dbms.service.ISmqBaseTargetService#findNotImpactedWithPaginated(int, int, java.lang.String, org.primefaces.model.SortOrder, java.util.Map)
	 */
	@Override
	public List<SmqBaseTarget> findNotImpactedWithPaginated(int first, int pageSize, String sortField
			, SortOrder sortOrder, Map<String, Object> filters) {
		List<SmqBaseTarget> retVal = null;
		EntityManager entityManager = this.cqtEntityManagerFactory.getEntityManager();
		try {
			CriteriaBuilder cb = entityManager.getCriteriaBuilder();
			CriteriaQuery<SmqBaseTarget> cq = cb.createQuery(SmqBaseTarget.class);
			Root<SmqBaseTarget> smqRoot = cq.from(SmqBaseTarget.class);
			List<Predicate> pred = new ArrayList<Predicate>();
			
			pred.add(cb.equal(smqRoot.get("impactType"), CSMQBean.IMPACT_TYPE_NONIMPACTED));
			
            if(filters.containsKey("smqName") && filters.get("smqName") != null) {
                String f = ((String)filters.get("smqName")).toLowerCase();
                f = f.contains("%") ? f : ("%" + f + "%");
				pred.add(cb.like(cb.lower(smqRoot.<String>get("smqName")), f));
            }
			
			if(filters.containsKey("smqLevel") && filters.get("smqLevel") != null)
				pred.add(cb.equal(smqRoot.get("smqLevel"), filters.get("smqLevel")));
            
            if(filters.containsKey("smqCode") && filters.get("smqCode") != null) {
                String f = ((String)filters.get("smqCode"));
                f = f.contains("%") ? f : ("%" + f + "%");
				pred.add(cb.like(smqRoot.get("smqCode").as(String.class), f));
            }
			
			cq.where(cb.and(pred.toArray(new Predicate[0])));
			
			cq.orderBy(cb.asc(smqRoot.get("smqName")));
			TypedQuery<SmqBaseTarget> tq = entityManager.createQuery(cq);
			
			if (pageSize >= 0){
			tq.setMaxResults(pageSize);
			}
			if (first >= 0){
			tq.setFirstResult(first);
			}
			
			retVal = tq.getResultList();
		} catch (Exception e) {
			StringBuilder msg = new StringBuilder();
			msg.append("An error occurred while fetching paginated impacted SmqBaseTarget.");
			LOG.error(msg.toString(), e);
		} finally {
			this.cqtEntityManagerFactory.closeEntityManager(entityManager);
		}
		return retVal;
	}

	/* (non-Javadoc)
	 * @see com.dbms.service.ISmqBaseTargetService#findNotImpactedCount()
	 */
	@Override
	public Long findNotImpactedCount() {
		Long retVal = null;
		StringBuilder sb = new StringBuilder();
		sb.append("select count(*) from SmqBaseTarget c where c.impactType = 'NON-IMPACTED'");
	
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
	public StreamedContent generateSMQExcel(SmqBaseTarget selectedImpactedSmqList, String dictionaryVersion) {
		XSSFWorkbook workbook = new XSSFWorkbook();
		XSSFSheet worksheet = null;

		worksheet = workbook.createSheet("IA Report");
		XSSFRow row = null;
		int rowCount = 0;
		DateFormat dateFormat = DateFormat.getDateTimeInstance(
		        DateFormat.LONG,
		        DateFormat.LONG, new Locale("EN","en"));

		/**
		 * Première ligne - entêtes
		 */
		row = worksheet.createRow(rowCount);
		XSSFCell cell = row.createCell(0);

		row = worksheet.createRow(rowCount);
		cell = row.createCell(0);
		cell.setCellValue(selectedImpactedSmqList.getSmqName());
		setCellStyleTitre(workbook, cell);

		// Term name
		rowCount += 2;
		row = worksheet.createRow(rowCount);
		cell = row.createCell(0);
		cell.setCellValue("MedDRA Dictionary Version: " + dictionaryVersion);

		rowCount++;
		row = worksheet.createRow(rowCount);
		cell = row.createCell(0);
		cell.setCellValue("Status: " + returnStatus(selectedImpactedSmqList));
		rowCount++;
		row = worksheet.createRow(rowCount);
		cell = row.createCell(0);
		cell.setCellValue("Scope (Yes/No): " + "");
		
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
		rowCount++;
		
		XSSFCellStyle cellStyle = workbook.createCellStyle();
		cellStyle.setFillForegroundColor(HSSFColor.BLUE.index);
		cellStyle.setFillPattern(XSSFCellStyle.SOLID_FOREGROUND);
		cellStyle.setBorderBottom(BorderStyle.MEDIUM);
		

		// Retrieval of relations - Loop
		List<SmqRelationTarget> relations = findSmqRelationsForSmqCode(selectedImpactedSmqList.getSmqCode());
		
		String level = "", term = "", codeTerm = "";

		if (relations != null) {
			for (SmqRelationTarget relation : relations) {
				/**
				 * 
				 * SMQs
				 */
				if (relation.getSmqCode() != null) {
					List<Long> smqChildCodeList = new ArrayList<>();
					smqChildCodeList.add(relation.getSmqCode());

					SmqBaseTarget smqSearched = findByCode(relation.getSmqCode());
					if (smqSearched != null) {
						List<SmqBaseTarget> smqBaseList = findByLevelAndTerm(smqSearched.getSmqLevel(),	smqSearched.getSmqName());
						if (smqBaseList != null) {
							for (SmqBaseTarget smq : smqBaseList) {
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
								buildCells(level, smq.getSmqCode() + "", smq.getSmqName(), cell, row, smq.getImpactType(), cellStyle); 
								setCellStyleColumn(workbook, cell); 
								rowCount++;
								
								if (level.equals("SMQ1")) {
									smqSearched = findByCode(smq.getSmqCode());
									if (smqSearched != null) {
										List<SmqRelationTarget> list = findSmqRelationsForSmqCode(smqSearched.getSmqCode());
										if (list != null)
											for (SmqRelationTarget smq3 : list) {
												if (smq3.getSmqLevel() == 1) {
													level = "SMQ1";
												} else if (smq3.getSmqLevel() == 2) {
													level = "SMQ2";
												} else if (smq3.getSmqLevel() == 3) {
													level = "SMQ3";
												} else if (smq3.getSmqLevel() == 4) {
													level = "PT";
												} else if (smq3.getSmqLevel() == 5) {
													level = "LLT";
												} else if (smq3.getSmqLevel() == 0) {
													level = "Child SMQ";
													System.out.println("\n ************ A");
												}
												row = worksheet.createRow(rowCount);
												buildChildCellsSMQ(level, smq3.getSmqCode() + "", smq3.getPtName(), smq3.getPtTermCategory(), smq3.getPtTermWeight() + "", smq3.getPtTermScope() + "",  cell, row, "..........", smq3.getRelationImpactType(), cellStyle); 
												setCellStyleColumn(workbook, cell); 
												rowCount++;
												
												if (level.equals("Child SMQ") && smq3.getPtCode() != null) {
													List<SmqRelationTarget> smqChildren = findSmqRelationsForSmqCode(Long.parseLong(smq3.getPtCode() + ""));
													if (smqChildren != null)
														for (SmqRelationTarget smqChild : smqChildren) {
															if (smqChild.getSmqLevel() == 4)
																level = "PT";
															if (smqChild.getSmqLevel() == 5)
																level = "LLT";
															row = worksheet.createRow(rowCount);
															buildChildCellsSMQ(level, smq.getSmqCode() + "", smqChild.getPtName(), smqChild.getPtTermCategory(), smqChild.getPtTermWeight() + "", smqChild.getPtTermScope() + "", cell, row, "..................", smqChild.getRelationImpactType(), cellStyle); 
															setCellStyleColumn(workbook, cell); 
															rowCount++;
 
													}
												}
											}
									}
								}		
								
								if (level.equals("SMQ2")) {						
									smqSearched = findByCode(smq.getSmqCode());
									if (smqSearched != null) {
										List<SmqRelationTarget> list = findSmqRelationsForSmqCode(smqSearched.getSmqCode());
										if (list != null)
											for (SmqRelationTarget smq3 : list) {
												row = worksheet.createRow(rowCount);
												buildChildCellsSMQ("PT", smq3.getPtCode() + "", smq3.getPtName(), smq3.getPtTermCategory(), smq3.getPtTermWeight() + "", smq3.getPtTermScope() + "", cell, row, "............", smq3.getRelationImpactType(), cellStyle);
												setCellStyleColumn(workbook, cell); 
												rowCount++;
											}
									}
									List<Long> codes = new ArrayList<>();
									codes.add(smq.getSmqCode());
									
									
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
					 					System.out.println("\n ************ E");
									} 
									List<SmqBaseTarget> smqChildren = findChildSmqByParentSmqCodes(codes);
									if (smqChildren != null)
										for (SmqBaseTarget child : smqChildren) {
											row = worksheet.createRow(rowCount);
											buildChildCellsSMQ(levelS, child.getSmqCode() + "", child.getSmqName(), "", "", "",cell, row, "............", child.getImpactType(), cellStyle);
											setCellStyleColumn(workbook, cell); 
											rowCount++;
											
											smqSearched = findByCode(child.getSmqCode());
											if (smqSearched != null) {
												List<SmqRelationTarget> list = findSmqRelationsForSmqCode(smqSearched.getSmqCode());
												if (list != null)
													for (SmqRelationTarget smq3 : list) {
														row = worksheet.createRow(rowCount);
														buildChildCellsSMQ("PT", smq3.getPtCode() + "", smq3.getPtName(), smq3.getPtTermCategory(), smq3.getPtTermWeight() + "", smq3.getPtTermScope() + "",cell, row, ".....................", smq3.getRelationImpactType(), cellStyle);
														setCellStyleColumn(workbook, cell); 
														rowCount++;
													}
											}
										}
									
								}
								
								if (level.equals("SMQ3")) {
									smqSearched = findByCode(smq.getSmqCode());
									if (smqSearched != null) {
										List<SmqRelationTarget> list = findSmqRelationsForSmqCode(smqSearched.getSmqCode());
										if (list != null)
											for (SmqRelationTarget smq3 : list) {
												row = worksheet.createRow(rowCount);
												buildChildCellsSMQ("PT", smq3.getPtCode() + "", smq3.getPtName(), smq3.getPtTermCategory(), smq3.getPtTermWeight() + "", smq3.getPtTermScope() + "",cell, row, "...............", smq3.getRelationImpactType(), cellStyle);
												setCellStyleColumn(workbook, cell); 
												rowCount++;
											}
									}
								}
								if (level.equals("SMQ4")) {
									smqSearched = findByCode(smq.getSmqCode());
									if (smqSearched != null) {
										List<SmqRelationTarget> list = findSmqRelationsForSmqCode(smqSearched.getSmqCode());
										if (list != null)
											for (SmqRelationTarget smq3 : list) {
												row = worksheet.createRow(rowCount);
												buildChildCellsSMQ("PT", smq3.getPtCode() + "", smq3.getPtName(), smq3.getPtTermCategory(), smq3.getPtTermWeight() + "", smq3.getPtTermScope() + "",cell, row, "...............", smq3.getRelationImpactType(), cellStyle);
												setCellStyleColumn(workbook, cell); 
												rowCount++;
											}
									}
								}
									
								if (level.equals("SMQ1")) {
									smqSearched = findByCode(smq.getSmqCode());
									if (smqSearched != null) {
										List<SmqRelationTarget> list = findSmqRelationsForSmqCode(smqSearched.getSmqCode());
										if (list != null)
											for (SmqRelationTarget smq3 : list) {
												row = worksheet.createRow(rowCount);
												buildChildCellsSMQ("PT", smq3.getPtCode() + "", smq3.getPtName(), smq3.getPtTermCategory(), smq3.getPtTermWeight() + "", smq3.getPtTermScope() + "",cell, row, "...............", smq3.getRelationImpactType(), cellStyle);
												setCellStyleColumn(workbook, cell); 
												rowCount++;
											}
									}
								}	
 
							 
							}
						}
 
					}
					
					//Relations for SMQs
					Long smqBaseChildrenCount = findChildSmqCountByParentSmqCode(relation.getSmqCode());
					smqBaseChildrenCount = findSmqRelationsCountForSmqCode(relation.getSmqCode());

 					List<SmqRelationTarget> childSmqs =  findSmqRelationsForSmqCode(relation.getSmqCode());

					if((null != childSmqs) && (childSmqs.size() > 0)) {
						for (SmqRelationTarget childSmq : childSmqs) {
							if (childSmq.getSmqLevel() == 0) {
								level = "Child SMQ";
			 					System.out.println("\n ************ C");

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
							
							row = worksheet.createRow(rowCount);
							buildChildCellsSMQ(level, childSmq.getPtCode() + "", childSmq.getPtName(), childSmq.getPtTermCategory(), childSmq.getPtTermWeight() + "", childSmq.getPtTermScope() + "",cell, row, "...............", childSmq.getRelationImpactType(), cellStyle);
							setCellStyleColumn(workbook, cell); 
							rowCount++;

 							
							List<Long> codes = new ArrayList<>();
							codes.add(childSmq.getSmqCode());
							
							List<SmqBaseTarget> smqs = findChildSmqByParentSmqCodes(codes);
							
							smqSearched = findByCode(Long.parseLong(childSmq.getPtCode() + ""));
							if (smqSearched != null) {
								List<SmqRelationTarget> firstRelation = findSmqRelationsForSmqCode(smqSearched.getSmqCode());
								if (firstRelation != null) {
									for (SmqRelationTarget first : firstRelation) {
										if (first.getSmqLevel() == 4) {
											level = "PT";
										} else if (first.getSmqLevel() == 5) {
											level = "LLT";
										} else if (first.getSmqLevel() == 0) {
											level = "Child SMQ";
						 					System.out.println("\n ************ D");

										} 
										row = worksheet.createRow(rowCount);
										buildChildCellsSMQ(level, first.getPtCode() + "", first.getPtName(), first.getPtTermCategory(), first.getPtTermWeight() + "", first.getPtTermScope() + "",cell, row, "...............", first.getRelationImpactType(), cellStyle);
										setCellStyleColumn(workbook, cell); 
										rowCount++;
										
 									 
										if (level.equals("Child SMQ")) {			
 											smqSearched = findByCode(Long.parseLong(first.getPtCode() + ""));
											if (smqSearched != null) {
												List<SmqRelationTarget> secondRelation =  findSmqRelationsForSmqCode(smqSearched.getSmqCode());					 					
							 					
							 					if (secondRelation != null) {
													for (SmqRelationTarget second : secondRelation) {
														if (second.getSmqLevel() == 4) {
															level = "PT";
														} else if (second.getSmqLevel() == 5) {
															level = "LLT";
														} else if (second.getSmqLevel() == 0) {
															level = "Child SMQ";
										 					System.out.println("\n ************ F");
														} 
														row = worksheet.createRow(rowCount);
														buildChildCellsSMQ(level, second.getPtCode() + "", second.getPtName(), second.getPtTermCategory(), second.getPtTermWeight() + "", second.getPtTermScope() + "", cell, row, "...............", second.getRelationImpactType(), cellStyle);
														setCellStyleColumn(workbook, cell); 
														rowCount++;
									 				 
														smqSearched = findByCode(Long.parseLong(second.getPtCode() + ""));
														
														if (smqSearched != null) {
															List<SmqRelationTarget> thirdRelation =  findSmqRelationsForSmqCode(smqSearched.getSmqCode());
 										 					
										 					if (thirdRelation != null) {
																for (SmqRelationTarget third : thirdRelation) {
																	if (third.getSmqLevel() == 4) {
																		level = "PT";
																	} else if (third.getSmqLevel() == 5) {
																		level = "LLT";
																	} else if (third.getSmqLevel() == 0) {
																		level = "Child SMQ";
													 					System.out.println("\n ************ I");

																	} 
																	row = worksheet.createRow(rowCount);
																	buildChildCellsSMQ(level, second.getPtCode() + "", third.getPtName(), third.getPtTermCategory(), third.getPtTermWeight() + "", third.getPtTermScope() + "", cell, row, "...............", third.getRelationImpactType(), cellStyle);
																	setCellStyleColumn(workbook, cell); 
																	rowCount++;
																	 
																	if (level.equals("Child SMQ")) {										
																		smqSearched = findByCode(Long.parseLong(third.getPtCode() + ""));
																		if (smqSearched != null) {
																			List<SmqRelationTarget> fourthRelation =  findSmqRelationsForSmqCode(smqSearched.getSmqCode());
 														 					
														 					
														 					if (fourthRelation != null) {
																				for (SmqRelationTarget fourth : fourthRelation) {
																					if (fourth.getSmqLevel() == 4) {
																						level = "PT";
																					} else if (fourth.getSmqLevel() == 5) {
																						level = "LLT";
																					} else if (fourth.getSmqLevel() == 0) {
																						level = "Child SMQ";
																	 					System.out.println("\n ************ G");

																					} 
																					row = worksheet.createRow(rowCount);
																					buildChildCellsSMQ(level, fourth.getPtCode() + "", fourth.getPtName(), fourth.getPtTermCategory(), fourth.getPtTermWeight() + "", fourth.getPtTermScope() + "",cell, row, "...............", fourth.getRelationImpactType(), cellStyle);
																					setCellStyleColumn(workbook, cell); 
																					rowCount++;
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
				 * LLT.
				 */
				if (relation.getPtCode() != null && relation.getSmqCode() == null) {
					List<Long> lltCodesList = new ArrayList<>();
					lltCodesList.add(Long.valueOf(relation.getPtCode()));
					List<MeddraDictHierarchySearchDto> llts = this.meddraDictService.findByCodes("LLT_", lltCodesList);
					for (MeddraDictHierarchySearchDto llt : llts) {
						row = worksheet.createRow(rowCount);
						buildCells("LLT", llt.getCode() + "", llt.getTerm(), cell, row, "", cellStyle);
						setCellStyleColumn(workbook, cell); 
						rowCount++;
					}
				}
				
				/**
				 * 
				 * PT
				 */
				if (relation.getPtCode() != null && relation.getSmqCode() == null) {
					row = worksheet.createRow(rowCount);
					buildCells("PT", relation.getPtCode() + "", relation.getPtName(), cell, row, "", cellStyle);
					setCellStyleColumn(workbook, cell); 
					rowCount++;

					/**
					 * LLT.
					 */
					List<MeddraDictHierarchySearchDto> listPT =  meddraDictService.findChildrenByParentCode("LLT_", "PT_", Long.valueOf(relation.getPtCode()));
					List<Long> hlgtCodesList = new ArrayList<>();
					for (MeddraDictHierarchySearchDto meddra : listPT) {
						hlgtCodesList.add(Long.parseLong(meddra.getCode())); 
					}

					List<MeddraDictHierarchySearchDto> llts = this.meddraDictService.findByCodes("LLT_", hlgtCodesList);
					if (llts != null)
						for (MeddraDictHierarchySearchDto llt : llts) {
							row = worksheet.createRow(rowCount);
							buildChildCells("LLT", llt.getCode(), llt.getTerm(), cell, row, "......", "", cellStyle);
							rowCount++;
						}	
				}
			}
				
			List<SmqBaseTarget> childCmqs = findChildSmqByParentSmqCode(selectedImpactedSmqList.getSmqCode());
			if((null != childCmqs) && (childCmqs.size() > 0)) {
				for (SmqBaseTarget childCmq : childCmqs) {
					level = childCmq.getSmqLevel() + "";
					term = childCmq.getSmqName();
					codeTerm = childCmq.getSmqCode() != null ? childCmq.getSmqCode() + "" : "";

					row = worksheet.createRow(rowCount);
					buildCells(level, codeTerm, term, cell, row,  childCmq.getImpactType(), cellStyle);
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
					"Impact_Assessment_Report_" + selectedImpactedSmqList.getSmqName()
					+ ".xlsx");
		} catch (IOException e) {
			e.printStackTrace();
		}

		return content;
	}
	
	
	private String returnStatus(SmqBaseTarget smq) {
		String status = "";
		if(CmqBase190.CMQ_STATUS_VALUE_PENDING.equalsIgnoreCase(smq.getSmqStatus())) {
			status = CmqBase190.CMQ_STATUS_DISP_LABEL_PENDING;
		} else if (CmqBase190.CMQ_STATUS_VALUE_ACTIVE.equalsIgnoreCase(smq.getSmqStatus())) {
			status = CmqBase190.CMQ_STATUS_DISP_LABEL_ACTIVE;
		} else if (CmqBase190.CMQ_STATUS_VALUE_INACTIVE.equalsIgnoreCase(smq.getSmqStatus())){
			status = CmqBase190.CMQ_STATUS_DISP_LABEL_INACTIVE;
		} else {
			status = "UNKNOWN";
		}
		return status;
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
	
	private void buildCells(String level, String codeTerm, String term, XSSFCell cell, XSSFRow row, String impact, XSSFCellStyle cellStyle) {
		String impactStr = "";
		if (impact != null && !"".equals(impact))
			impactStr = impact;
		// Cell 0
		cell = row.createCell(0);
		cell.setCellValue(term + impactStr);
//		if (impact != null && !"".equals(impact))
//			cell.setCellStyle(cellStyle); 

		// Cell 1
		cell = row.createCell(1);
		cell.setCellValue(codeTerm);

		// Cell 2
		cell = row.createCell(2);
		cell.setCellValue(level);
	}
	
	private void buildChildCells(String level, String codeTerm, String term, XSSFCell cell, XSSFRow row, String dots, String impact, XSSFCellStyle cellStyle) {
		String impactStr = "";
		if (impact != null && !"".equals(impact))
			impactStr = impact;
		// Cell 0
		cell = row.createCell(0);
		cell.setCellValue(dots + term + impactStr);
//		if (impact != null && !"".equals(impact))
//			cell.setCellStyle(cellStyle); 

		// Cell 1
		cell = row.createCell(1);
		cell.setCellValue(codeTerm);

		// Cell 2
		cell = row.createCell(2);
		cell.setCellValue(level);
	}
	
	private void buildChildCellsSMQ(String level, String codeTerm, String term, String category, String weight, String scope, XSSFCell cell, XSSFRow row, String dots, String impact, XSSFCellStyle cellStyle) {
		String impactStr = "";
		if (impact != null && !"".equals(impact))
			impactStr = impact;
		// Cell 0
		cell = row.createCell(0);
		cell.setCellValue(dots + term + impactStr);
 
		// Cell 1
		cell = row.createCell(1);
		cell.setCellValue(codeTerm);

		// Cell 2
		cell = row.createCell(2);
		cell.setCellValue(level);
		
		// Cell 3
		cell = row.createCell(3);
		cell.setCellValue(category);
		
		// Cell 4
		cell = row.createCell(4);
		cell.setCellValue(weight);
		
		// Cell 5
		cell = row.createCell(5);
		cell.setCellValue(interpretCqtBaseScope(scope + ""));
	}
	
	 public String interpretCqtBaseScope(String scopeVal) {
	        if("2".equals(scopeVal))
	            return "Narrow";
	        else if("2".equals(scopeVal))
	            return "Broad";
	        else if("3".equals(scopeVal))
	            return "Child Narrow";
	        else if("4".equals(scopeVal))
	            return "Full";
	        return "";
	    }
	
	@Override
	@SuppressWarnings("unchecked")
	public List<SMQReverseHierarchySearchDto> findReverseByLevelAndTerm(Integer smqLevel, String smqName) {
		List<SMQReverseHierarchySearchDto> retVal = null;
		StringBuilder sb = new StringBuilder();
		sb.append("select SMQ_CODE as smqCode, SMQ_NAME as smqName, SMQ_LEVEL as smqLevel, SMQ_DESCRIPTION as smqDescription, "
				+ " SMQ_SOURCE as smqSource, SMQ_NOTE as smqNote, SMQ_STATUS as smqStatus, SMQ_ALGORITHM as smqAlgorithm, "
				+ " DICTIONARY_VERSION as dictionaryVersion, IMPACT_TYPE as impactType, SMQ_ID as smqId from SMQ_BASE_TARGET "
				+ " where SMQ_LEVEL = :smqLevel ");
		if(!StringUtils.isBlank(smqName)) {
			sb.append("  and upper(SMQ_NAME) like :smqName");
		}
		EntityManager entityManager = this.cqtEntityManagerFactory.getEntityManager();
		Session session = entityManager.unwrap(Session.class);
		try {
			SQLQuery query = session.createSQLQuery(sb.toString());
			query.addScalar("smqCode", StandardBasicTypes.LONG);
			query.addScalar("smqName", StandardBasicTypes.STRING);
			query.addScalar("smqLevel", StandardBasicTypes.STRING);
			query.addScalar("smqDescription", StandardBasicTypes.STRING);
			query.addScalar("smqSource", StandardBasicTypes.STRING);
			query.addScalar("smqNote", StandardBasicTypes.STRING);
			query.addScalar("smqStatus", StandardBasicTypes.STRING);
			query.addScalar("smqAlgorithm", StandardBasicTypes.STRING);
			query.addScalar("dictionaryVersion", StandardBasicTypes.STRING);
			query.addScalar("impactType", StandardBasicTypes.STRING);
			query.addScalar("smqId", StandardBasicTypes.LONG);
			query.addScalar("smqNote", StandardBasicTypes.STRING);
			
			if (!StringUtils.isBlank(smqName)) {
				query.setParameter("smqName", smqName.toUpperCase());
			}
			if (smqLevel != null) {
				query.setParameter("smqLevel", smqLevel);
			}
			 
			query.setFetchSize(400);
 			query.setResultTransformer(Transformers.aliasToBean(SMQReverseHierarchySearchDto.class));
			query.setCacheable(true);
			retVal = query.list();
		} catch (Exception e) {
			StringBuilder msg = new StringBuilder();
			msg
					.append("An error occurred while fetching findReverseByLevelAndTerm from SMQ_BASE_TARGET on smqLevel ")
					.append(smqLevel)
					.append(" with smqName like ")
					.append(smqName.toUpperCase())
					.append(" Query used was ->")
					.append(sb.toString());
			LOG.error(msg.toString(), e);
		} finally {
			this.cqtEntityManagerFactory.closeEntityManager(entityManager);
		}
		return retVal;
	}
	
	@Override
	public List<SMQReverseHierarchySearchDto> findFullReverseByLevelAndTerm(String level, String myFilterTermName) {
		String parentLevel = "";
		if (level != null) {
			if (level.equals("5"))
				parentLevel = "4";
			if (level.equals("4"))
				parentLevel = "3";
			if (level.equals("3"))
				parentLevel = "2";
			if (level.equals("2"))
				parentLevel = "1";
		}
		List<SMQReverseHierarchySearchDto> retVal = null;
 		String queryString = "select   p.SMQ_PARENT_CODE as smqParentCode, "
									+ "p.SMQ_PARENT_NAME as smqParentName, "
									+ "s.SMQ_CODE as smqChildCode, "
									+ "s.SMQ_LEVEL as smqChildLevel, "
									+ "(select SMQ_LEVEL from SMQ_BASE_TARGET WHERE SMQ_CODE = p.SMQ_PARENT_CODE and SMQ_LEVEL = :parentLevel) as smqParentLevel, "
									+ "p.SMQ_CHILD_NAME as  smqChildName "
									+ "from SMQ_PARENT_CHILD_TARGET p, SMQ_BASE_TARGET s "
									+ "where rownum = 1 "
									+ "and upper(s.SMQ_NAME) like :myFilterTermName "
									+ "and p.SMQ_CHILD_CODE = s.SMQ_CODE "
									+ "and s.SMQ_LEVEL = :level "
									+ "and (select SMQ_LEVEL from SMQ_BASE_TARGET WHERE SMQ_CODE = p.SMQ_PARENT_CODE and SMQ_LEVEL = :parentLevel) is not null "
									+ " order by smqParentLevel";

		EntityManager entityManager = this.cqtEntityManagerFactory.getEntityManager();
		Session session = entityManager.unwrap(Session.class);
		try {
			SQLQuery query = session.createSQLQuery(queryString);
			query.addScalar("smqParentCode", StandardBasicTypes.STRING);
			query.addScalar("smqParentName", StandardBasicTypes.STRING);
			query.addScalar("smqChildCode", StandardBasicTypes.STRING);
			query.addScalar("smqChildName", StandardBasicTypes.STRING);
			query.addScalar("smqChildLevel", StandardBasicTypes.STRING);
			query.addScalar("smqParentLevel", StandardBasicTypes.STRING);

			
			if (!StringUtils.isBlank(myFilterTermName)) {
				query.setParameter("myFilterTermName", myFilterTermName.toUpperCase());
			}
			if (!StringUtils.isBlank(level)) {
				query.setParameter("level", level);
			}
			query.setParameter("parentLevel", parentLevel);
			 
			query.setFetchSize(400);
 			query.setResultTransformer(Transformers.aliasToBean(SMQReverseHierarchySearchDto.class));
			query.setCacheable(true);
			retVal = query.list();
		} catch (Exception e) {
			StringBuilder msg = new StringBuilder();
			msg.append("An error occurred while fetching findFullReverseByLevelAndTerm on searchColumnType ")
					.append(" Query used was ->")
					.append(queryString);
			LOG.error(msg.toString(), e);
		} finally {
			this.cqtEntityManagerFactory.closeEntityManager(entityManager);
		}
		return retVal;
	}

	public IMeddraDictTargetService getMeddraDictService() {
		return meddraDictService;
	}

	public void setMeddraDictService(IMeddraDictTargetService meddraDictService) {
		this.meddraDictService = meddraDictService;
	}
}
