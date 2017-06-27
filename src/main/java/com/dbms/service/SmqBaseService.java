package com.dbms.service;

import java.util.List;
import java.util.Map;

import javax.faces.bean.ApplicationScoped;
import javax.faces.bean.ManagedBean;
import javax.persistence.EntityManager;
import javax.persistence.Query;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.SQLQuery;
import org.hibernate.Session;
import org.hibernate.transform.Transformers;
import org.hibernate.type.StandardBasicTypes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dbms.csmq.CSMQBean;
import com.dbms.entity.cqt.SmqBase190;
import com.dbms.entity.cqt.SmqRelation190;
import com.dbms.entity.cqt.dtos.MeddraDictReverseHierarchySearchDto;
import com.dbms.entity.cqt.dtos.SMQReverseHierarchySearchDto;
import com.dbms.service.base.CqtPersistenceService;
import com.dbms.util.CmqUtils;
import com.dbms.util.SmqAndPtCodeHolder;

/**
 * @author Jay G.(jayshanchn@hotmail.com)
 * @date Feb 11, 2017 8:00:48 AM
 **/
@ManagedBean(name = "SmqBaseService")
@ApplicationScoped
public class SmqBaseService extends CqtPersistenceService<SmqBase190> implements ISmqBaseService {

	private static final Logger LOG = LoggerFactory.getLogger(SmqBaseService.class);

	/* (non-Javadoc)
	 * @see com.dbms.service.ISmqBaseService#findByLevelAndTerm(java.lang.Integer, java.lang.String)
	 */
	/* (non-Javadoc)
	 * @see com.dbms.service.ISmqBaseService#findByLevelAndTerm(java.lang.Integer, java.lang.String)
	 */
	@Override
	@SuppressWarnings("unchecked")
	public List<SmqBase190> findByLevelAndTerm(Integer level, String searchTerm) {
		List<SmqBase190> retVal = null;
		StringBuilder sb = new StringBuilder();
		sb.append("from SmqBase190 c where c.smqLevel = :smqLevel ");
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
			query.setHint("org.hibernate.cacheable", true);
			retVal = query.getResultList();
		} catch (Exception e) {
			StringBuilder msg = new StringBuilder();
			msg
					.append("An error occurred while fetching types from SmqBase190 on smqLevel ")
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

	/* (non-Javadoc)
	 * @see com.dbms.service.ISmqBaseService#findSmqRelationsCountForSmqCode(java.lang.Long)
	 */
	@Override
	public Long findSmqRelationsCountForSmqCode(Long smqCode) {
		Long retVal = null;
		StringBuilder sb = new StringBuilder();
		sb.append("select count(*) from SmqRelation190 c where c.smqCode = :smqCode");
		
		EntityManager entityManager = this.cqtEntityManagerFactory.getEntityManager();
		try {
			Query query = entityManager.createQuery(sb.toString());
			query.setParameter("smqCode", smqCode);
			query.setHint("org.hibernate.cacheable", true);
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
	
	/* (non-Javadoc)
	 * @see com.dbms.service.ISmqBaseService#findSmqRelationsCountForSmqCodes(java.util.List)
	 */
	@Override
	@SuppressWarnings("unchecked")
	public List<Map<String, Object>> findSmqRelationsCountForSmqCodes(List<Long> smqCodes) {
		List<Map<String, Object>> retVal = null;
        
        if(CollectionUtils.isEmpty(smqCodes))
            return null;
		
		String queryString = CmqUtils.convertArrayToTableWith(smqCodes, "tempSmqCodes", "code")
                + " select count(*) as COUNT, SMQ_CODE"
                + " from SMQ_RELATIONS_CURRENT smqTbl"
                + " inner join tempSmqCodes on tempSmqCodes.code=smqTbl.SMQ_CODE"
                + " group by SMQ_CODE";
		
		EntityManager entityManager = this.cqtEntityManagerFactory.getEntityManager();
		Session session = entityManager.unwrap(Session.class);
		try {
			SQLQuery query = session.createSQLQuery(queryString);
			query.addScalar("SMQ_CODE", StandardBasicTypes.LONG);
			query.addScalar("COUNT", StandardBasicTypes.LONG);

            query.setResultTransformer(Transformers.ALIAS_TO_ENTITY_MAP);
			query.setCacheable(true);
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
	
	/* (non-Javadoc)
	 * @see com.dbms.service.ISmqBaseService#findSmqRelationsForSmqCode(java.lang.Long)
	 */
	@Override
	@SuppressWarnings("unchecked")
	public List<SmqRelation190> findSmqRelationsForSmqCode(Long smqCode) {
		List<SmqRelation190> retVal = null;
		StringBuilder sb = new StringBuilder();
		sb.append("from SmqRelation190 c where c.smqCode = :smqCode order by c.smqLevel asc, c.ptName asc");
		
		EntityManager entityManager = this.cqtEntityManagerFactory.getEntityManager();
		try {
			Query query = entityManager.createQuery(sb.toString());
			query.setParameter("smqCode", smqCode);
			query.setHint("org.hibernate.cacheable", true);
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
	public List<SmqRelation190> findSmqRelationsForSmqCodeAndScope(Long smqCode, String scope) {
		List<SmqRelation190> retVal = null;
		StringBuilder sb = new StringBuilder();
		if (StringUtils.isNotBlank(scope) && (scope.equals(CSMQBean.SCOPE_NARROW) || scope.equals(CSMQBean.SCOPE_BROAD))) {
			sb.append("from SmqRelation190 c where c.smqCode = :smqCode and (c.ptTermScope = 0 or c.ptTermScope = :ptTermScope) order by c.smqLevel asc, c.ptName asc");
		} else {
			sb.append("from SmqRelation190 c where c.smqCode = :smqCode order by c.smqLevel asc, c.ptName asc");
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
	public SmqRelation190 findSmqRelationBySmqAndPtCode(Long smqCode, Integer ptCode) {
		SmqRelation190 retVal = null;
		StringBuilder sb = new StringBuilder();
		sb.append("from SmqRelation190 c where c.smqCode = :smqCode and c.ptCode = :ptCode");
		
		EntityManager entityManager = this.cqtEntityManagerFactory.getEntityManager();
		try {
			Query query = entityManager.createQuery(sb.toString());
			query.setParameter("smqCode", smqCode);
			query.setParameter("ptCode", ptCode);
			query.setHint("org.hibernate.cacheable", true);
			retVal = (SmqRelation190) query.getSingleResult();
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
	public List<SmqRelation190> findSmqRelationBySmqAndPtCode(List<SmqAndPtCodeHolder> smqAndPtCodeHolders) {
		List<SmqRelation190> retVal = null;
		StringBuilder sb = new StringBuilder();
		sb.append("from SmqRelation190 c where ");
		int i = 0;
		for (SmqAndPtCodeHolder smqAndPtCodeHolder : smqAndPtCodeHolders) {
			if(i > 0) {
				sb.append(" or ");
			}
			sb.append(" (c.smqCode = ").append(smqAndPtCodeHolder.getSmqCode())
				.append(" and c.ptCode = ").append(smqAndPtCodeHolder.getPtCode())
				.append(") ");
			i++;
		}
		
		EntityManager entityManager = this.cqtEntityManagerFactory.getEntityManager();
		try {
			Query query = entityManager.createQuery(sb.toString());
			query.setHint("org.hibernate.cacheable", true);
			retVal = query.getResultList();
		} catch (Exception e) {
			StringBuilder msg = new StringBuilder();
			msg
					.append("An error occurred while findSmqRelationBySmqAndPtCode.")
					.append(" Query used was ->")
					.append(sb.toString());
			LOG.error(msg.toString(), e);
		} finally {
			this.cqtEntityManagerFactory.closeEntityManager(entityManager);
		}
		return retVal;
	}
	
	/* (non-Javadoc)
	 * @see com.dbms.service.ISmqBaseService#findSmqRelationsForSmqCodes(java.util.List)
	 */
	@Override
	@SuppressWarnings("unchecked")
	public List<SmqRelation190> findSmqRelationsForSmqCodes(List<Long> smqCodes) {
		List<SmqRelation190> retVal = null;
		StringBuilder sb = new StringBuilder();
		sb.append("from SmqRelation190 c where c.smqCode in (:smqCodes) order by c.smq_Code asc, c.ptName asc");
		
		EntityManager entityManager = this.cqtEntityManagerFactory.getEntityManager();
		try {
			Query query = entityManager.createQuery(sb.toString());
			query.setParameter("smqCode", smqCodes);
			query.setHint("org.hibernate.cacheable", true);
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
	
	/* (non-Javadoc)
	 * @see com.dbms.service.ISmqBaseService#findChildSmqByParentSmqCodes(java.util.List)
	 */
	@Override
	@SuppressWarnings("unchecked")
	public List<SmqBase190> findChildSmqByParentSmqCodes(List<Long> smqCodes) {
		List<SmqBase190> retVal = null;
		StringBuilder sb = new StringBuilder();
		sb.append("from SmqBase190 c where c.smqParentCode in (:smqParentCodes) order by c.smqParentCode");
		
		EntityManager entityManager = this.cqtEntityManagerFactory.getEntityManager();
		try {
			Query query = entityManager.createQuery(sb.toString());
			query.setParameter("smqParentCodes", smqCodes);
			query.setHint("org.hibernate.cacheable", true);
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
	
	/* (non-Javadoc)
	 * @see com.dbms.service.ISmqBaseService#findChildSmqByParentSmqCode(java.lang.Long)
	 */
	@SuppressWarnings("unchecked")
	@Override
	public List<SmqBase190> findChildSmqByParentSmqCode(Long smqCode) {
		List<SmqBase190> retVal = null;
		StringBuilder sb = new StringBuilder();
		sb.append("from SmqBase190 c where c.smqParentCode = :smqParentCode");
		
		EntityManager entityManager = this.cqtEntityManagerFactory.getEntityManager();
		try {
			Query query = entityManager.createQuery(sb.toString());
			query.setParameter("smqParentCode", smqCode);
			query.setHint("org.hibernate.cacheable", true);
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
		sb.append("select count(*) from SmqBase190 c where c.smqParentCode = :smqParentCode");
		
		EntityManager entityManager = this.cqtEntityManagerFactory.getEntityManager();
		try {
			Query query = entityManager.createQuery(sb.toString());
			query.setParameter("smqParentCode", smqCode);
			query.setHint("org.hibernate.cacheable", true);
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
	public List<Map<String, Object>> findChildSmqCountByParentSmqCodes(List<Long> smqCodes) {
		List<Map<String, Object>> retVal = null;
		
        if(CollectionUtils.isEmpty(smqCodes))
            return null;
        
        String queryString = CmqUtils.convertArrayToTableWith(smqCodes, "tempSmqCodes", "code")
                + " select count(*) as COUNT, SMQ_PARENT_CODE"
                + " from SMQ_BASE_CURRENT smqTbl"
                + " inner join tempSmqCodes on tempSmqCodes.code=smqTbl.SMQ_PARENT_CODE"
                + " group by SMQ_PARENT_CODE";
        
		EntityManager entityManager = this.cqtEntityManagerFactory.getEntityManager();
        Session session = entityManager.unwrap(Session.class);
		try {
            SQLQuery query = session.createSQLQuery(queryString);
			query.addScalar("SMQ_PARENT_CODE", StandardBasicTypes.LONG);
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
	
	/* (non-Javadoc)
	 * @see com.dbms.service.ISmqBaseService#findByCode(java.lang.Long)
	 */
	@Override
	public SmqBase190 findByCode(Long smqCode) {
		SmqBase190 retVal = null;
		String queryString = "from SmqBase190 c where c.smqCode = :smqCode ";
		EntityManager entityManager = this.cqtEntityManagerFactory.getEntityManager();
		try {
			Query query = entityManager.createQuery("from SmqBase190 c where c.smqCode = :smqCode ");
			query.setParameter("smqCode", smqCode);
			query.setHint("org.hibernate.cacheable", true);
			retVal = (SmqBase190) query.getSingleResult();
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
	public List<SmqBase190> findByCodes(List<Long> smqCodes) {
		List<SmqBase190> retVal = null;
		String queryString = "from SmqBase190 c where c.smqCode in (:smqCodes) ";
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
									+ "(select SMQ_LEVEL from SMQ_BASE_CURRENT WHERE SMQ_CODE = p.SMQ_PARENT_CODE and SMQ_LEVEL = :parentLevel) as smqParentLevel, "
									+ "p.SMQ_CHILD_NAME as  smqChildName "
									+ "from SMQ_PARENT_CHILD_CURRENT p, SMQ_BASE_CURRENT s "
									+ "where rownum = 1 "
									+ "and upper(s.SMQ_NAME) like :myFilterTermName "
									+ "and p.SMQ_CHILD_CODE = s.SMQ_CODE "
									+ "and s.SMQ_LEVEL = :level "
									+ "and (select SMQ_LEVEL from SMQ_BASE_CURRENT WHERE SMQ_CODE = p.SMQ_PARENT_CODE and SMQ_LEVEL = :parentLevel) is not null "
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
}
