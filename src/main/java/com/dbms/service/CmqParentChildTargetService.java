package com.dbms.service;

import java.util.List;
import java.util.Map;

import javax.faces.bean.ApplicationScoped;
import javax.faces.bean.ManagedBean;
import javax.persistence.EntityManager;
import javax.persistence.Query;

import org.apache.commons.collections.CollectionUtils;
import org.hibernate.SQLQuery;
import org.hibernate.Session;
import org.hibernate.transform.Transformers;
import org.hibernate.type.StandardBasicTypes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dbms.entity.cqt.CmqParentChildTarget;
import com.dbms.service.base.CqtPersistenceService;
import com.dbms.util.CmqUtils;


@ManagedBean(name = "CmqParentChildTargetService")
@ApplicationScoped
public class CmqParentChildTargetService extends CqtPersistenceService<CmqParentChildTarget> implements ICmqParentChildTargetService {

	private static final Logger LOG = LoggerFactory.getLogger(CmqParentChildTargetService.class);

	/* (non-Javadoc)
	 * @see com.dbms.service.ICmqParentChildTargetService#findCountByCmqCode(java.lang.Long)
	 */
	@Override
	public Long findCountByCmqCode(Long cmqCode) {
		Long retVal = null;
		StringBuilder sb = new StringBuilder();
		sb.append("select count(*) from CmqRelationTarget c where c.cmqCode = :cmqCode");
		
		EntityManager entityManager = this.cqtEntityManagerFactory.getEntityManager();
		try {
			Query query = entityManager.createQuery(sb.toString());
			query.setParameter("cmqCode", cmqCode);
			query.setHint("org.hibernate.cacheable", true);
			retVal = (Long)query.getSingleResult();
		} catch (Exception e) {
			StringBuilder msg = new StringBuilder();
			msg
					.append("An error occurred while findCountByCmqCode ")
					.append(cmqCode)
					.append(" Query used was ->")
					.append(sb.toString());
			LOG.error(msg.toString(), e);
		} finally {
			this.cqtEntityManagerFactory.closeEntityManager(entityManager);
		}
		return retVal;
	}

	/* (non-Javadoc)
	 * @see com.dbms.service.ICmqParentChildTargetService#findParentsByCmqCode(java.lang.Long)
	 */
	@Override
	public List<CmqParentChildTarget> findParentsByCmqCode(Long cmqCode) {
		List<CmqParentChildTarget> retVal = null;
		StringBuilder sb = new StringBuilder();
		sb.append("from CmqParentChildTarget c where c.cmqChildCode = :cmqCode ");
		EntityManager entityManager = this.cqtEntityManagerFactory.getEntityManager();
		try {
			Query query = entityManager.createQuery(sb.toString());
			query.setParameter("cmqCode", cmqCode);
			query.setHint("org.hibernate.cacheable", true);
			retVal = query.getResultList();
		} catch (Exception e) {
			StringBuilder msg = new StringBuilder();
			msg
					.append("An error occurred while fetching types from CmqParentChildTarget on cmqCode ")
					.append(cmqCode)
					.append(" Query used was ->")
					.append(sb.toString());
			LOG.error(msg.toString(), e);
		} finally {
			this.cqtEntityManagerFactory.closeEntityManager(entityManager);
		}
		return retVal;
	}

	/* (non-Javadoc)
	 * @see com.dbms.service.ICmqParentChildTargetService#findChildsByCmqCode(java.lang.Long)
	 */
	@Override
	public List<CmqParentChildTarget> findChildsByCmqCode(Long parentCmqCode) {
		List<CmqParentChildTarget> retVal = null;
		StringBuilder sb = new StringBuilder();
		sb.append("from CmqParentChildTarget c where c.cmqParentCode = :cmqCode ");
		EntityManager entityManager = this.cqtEntityManagerFactory.getEntityManager();
		try {
			Query query = entityManager.createQuery(sb.toString());
			query.setParameter("cmqCode", parentCmqCode);
			query.setHint("org.hibernate.cacheable", true);
			retVal = query.getResultList();
		} catch (Exception e) {
			StringBuilder msg = new StringBuilder();
			msg
					.append("An error occurred while fetching childs from CmqParentChildTarget on cmqParentCode ")
					.append(parentCmqCode)
					.append(" Query used was ->")
					.append(sb.toString());
			LOG.error(msg.toString(), e);
		} finally {
			this.cqtEntityManagerFactory.closeEntityManager(entityManager);
		}
		return retVal;
	}

	/* (non-Javadoc)
	 * @see com.dbms.service.ICmqParentChildTargetService#findCmqChildCountForParentCmqCode(java.lang.Long)
	 */
	@Override
	public Long findCmqChildCountForParentCmqCode(Long parentCmqCode) {
		Long retVal = null;
		StringBuilder sb = new StringBuilder();
		sb.append("select count(*) from CmqParentChildTarget c where c.cmqParentCode = :cmqCode");
		
		EntityManager entityManager = this.cqtEntityManagerFactory.getEntityManager();
		try {
			Query query = entityManager.createQuery(sb.toString());
			query.setParameter("cmqCode", parentCmqCode);
			query.setHint("org.hibernate.cacheable", true);
			retVal = (Long)query.getSingleResult();
		} catch (Exception e) {
			StringBuilder msg = new StringBuilder();
			msg
					.append("An error occurred while findCountByCmqCode ")
					.append(parentCmqCode)
					.append(" Query used was ->")
					.append(sb.toString());
			LOG.error(msg.toString(), e);
		} finally {
			this.cqtEntityManagerFactory.closeEntityManager(entityManager);
		}
		return retVal;
	}

	/* (non-Javadoc)
	 * @see com.dbms.service.ICmqParentChildTargetService#findCmqParentCountForChildCmqCode(java.lang.Long)
	 */
	/* (non-Javadoc)
	 * @see com.dbms.service.ICmqParentChildTargetService#findCmqParentCountForChildCmqCode(java.lang.Long)
	 */
	@Override
	public Long findCmqParentCountForChildCmqCode(Long childCmqCode) {
		Long retVal = null;
		StringBuilder sb = new StringBuilder();
		sb.append("select count(*) from CmqParentChildTarget c where c.cmqChildCode = :cmqCode");
		
		EntityManager entityManager = this.cqtEntityManagerFactory.getEntityManager();
		try {
			Query query = entityManager.createQuery(sb.toString());
			query.setParameter("cmqCode", childCmqCode);
			query.setHint("org.hibernate.cacheable", true);
			retVal = (Long)query.getSingleResult();
		} catch (Exception e) {
			StringBuilder msg = new StringBuilder();
			msg
					.append("An error occurred while findCountByCmqCode ")
					.append(childCmqCode)
					.append(" Query used was ->")
					.append(sb.toString());
			LOG.error(msg.toString(), e);
		} finally {
			this.cqtEntityManagerFactory.closeEntityManager(entityManager);
		}
		return retVal;
	}
	
	/* (non-Javadoc)
	 * @see com.dbms.service.ICmqParentChildTargetService#findCmqChildCountForParentCmqCodes(java.util.List)
	 */
	@SuppressWarnings("unchecked")
    @Override
	public List<Map<String, Object>> findCmqChildCountForParentCmqCodes(List<Long> cmqCodes) {
		List<Map<String, Object>> retVal = null;
        
        if(CollectionUtils.isEmpty(cmqCodes))
            return null;
        
		String queryString = CmqUtils.convertArrayToTableWith(cmqCodes, "tempCmqCodes", "code")
                + " select CMQ_CHILD_CODE as CMQ_CODE, count(*) as COUNT"
                + " from CMQ_PARENT_CHILD_TARGET cmqTbl"
                + " inner join tempCmqCodes on tempCmqCodes.code=cmqTbl.CMQ_PARENT_CODE"
                + " group by CMQ_CHILD_CODE";
		

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

	/* (non-Javadoc)
	 * @see com.dbms.service.ICmqParentChildTargetService#findChildCmqsByParentCodes(java.util.List)
	 */
	/* (non-Javadoc)
	 * @see com.dbms.service.ICmqParentChildTargetService#findChildCmqsByParentCodes(java.util.List)
	 */
	@Override
	public List<CmqParentChildTarget> findChildCmqsByParentCodes(List<Long> parentCmqCodes) {
		List<CmqParentChildTarget> retVal = null;
		String queryString = "from CmqParentChildTarget c where c.cmqParentCode in (:codeList) ";
		EntityManager entityManager = this.cqtEntityManagerFactory
				.getEntityManager();
		try {
			Query query = entityManager.createQuery(queryString);
			query.setParameter("codeList", parentCmqCodes);
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

	/* (non-Javadoc)
	 * @see com.dbms.service.ICmqParentChildTargetService#findParentCmqsByChildCodes(java.util.List)
	 */
	@Override
	public List<CmqParentChildTarget> findParentCmqsByChildCodes(List<Long> childCmqCodes) {
		List<CmqParentChildTarget> retVal = null;
		String queryString = "from CmqParentChildTarget c where c.cmqChildCode in (:codeList) ";
		EntityManager entityManager = this.cqtEntityManagerFactory
				.getEntityManager();
		try {
			Query query = entityManager.createQuery(queryString);
			query.setParameter("codeList", childCmqCodes);
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

	/* (non-Javadoc)
	 * @see com.dbms.service.ICmqParentChildTargetService#findByParentAndChildCode(java.lang.Long, java.lang.Long)
	 */
	@Override
	public CmqParentChildTarget findByParentAndChildCode(Long parentCmqCode, Long childCmqCode) {
		CmqParentChildTarget retVal = null;
		String queryString = "from CmqParentChildTarget c where c.cmqParentCode = :cmqParentCode and  c.cmqChildCode = :cmqChildCode";
		EntityManager entityManager = this.cqtEntityManagerFactory
				.getEntityManager();
		try {
			Query query = entityManager.createQuery(queryString);
			query.setParameter("cmqParentCode", parentCmqCode);
			query.setParameter("cmqChildCode", childCmqCode);
			query.setHint("org.hibernate.cacheable", true);
			retVal = (CmqParentChildTarget) query.getSingleResult();
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

	/* (non-Javadoc)
	 * @see com.dbms.service.ICmqParentChildTargetService#findByParentOrChildCmqCode(java.lang.Long)
	 */
	@Override
	public List<CmqParentChildTarget> findByParentOrChildCmqCode(Long cmqCode) {
		List<CmqParentChildTarget> retVal = null;
		StringBuilder sb = new StringBuilder();
		sb.append("from CmqParentChildTarget c where c.cmqParentCode = :cmqCode or c.cmqChildCode = :cmqCode");
		EntityManager entityManager = this.cqtEntityManagerFactory.getEntityManager();
		try {
			Query query = entityManager.createQuery(sb.toString());
			query.setParameter("cmqCode", cmqCode);
			query.setHint("org.hibernate.cacheable", true);
			retVal = query.getResultList();
		} catch (Exception e) {
			StringBuilder msg = new StringBuilder();
			msg
					.append("An error occurred while fetching entries from CmqParentChild200 on cmqParentCode or cmqChildCode")
					.append(cmqCode)
					.append(" Query used was ->")
					.append(sb.toString());
			LOG.error(msg.toString(), e);
		} finally {
			this.cqtEntityManagerFactory.closeEntityManager(entityManager);
		}
		return retVal;
	}
}
