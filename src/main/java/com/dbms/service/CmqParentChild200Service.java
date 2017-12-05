package com.dbms.service;

import java.util.List;
import java.util.Map;

import javax.faces.bean.ApplicationScoped;
import javax.faces.bean.ManagedBean;
import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.persistence.Query;

import org.apache.commons.collections.CollectionUtils;
import org.hibernate.SQLQuery;
import org.hibernate.Session;
import org.hibernate.transform.Transformers;
import org.hibernate.type.StandardBasicTypes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dbms.entity.cqt.CmqBase190;
import com.dbms.entity.cqt.CmqParentChild200;
import com.dbms.service.base.CqtPersistenceService;
import com.dbms.util.CmqUtils;
import com.dbms.util.exceptions.CqtServiceException;


@ManagedBean(name = "CmqParentChild200Service")
@ApplicationScoped
public class CmqParentChild200Service extends CqtPersistenceService<CmqParentChild200> implements ICmqParentChild200Service {

	private static final Logger LOG = LoggerFactory.getLogger(CmqParentChild200Service.class);

	public void create(List<CmqParentChild200> cmqRelations) throws CqtServiceException {
		EntityManager entityManager = this.cqtEntityManagerFactory.getEntityManager();
		EntityTransaction tx = null;
		try {
			tx = entityManager.getTransaction();
			tx.begin();
			for (CmqParentChild200 cmqRelation : cmqRelations) {
				entityManager.persist(cmqRelation);
			}
			tx.commit();
		} catch (Exception ex) {
			if ((tx != null) && tx.isActive()) {
				tx.rollback();
			}
			StringBuilder msg = new StringBuilder();
			msg.append("Failed to save List<CmqRelation190>");
			LOG.error(msg.toString(), ex);
			throw new CqtServiceException(ex);
		} finally {
			this.cqtEntityManagerFactory.closeEntityManager(entityManager);
		}
	}
	
	public Long findCountByCmqCode(Long cmqCode) {
		Long retVal = null;
		StringBuilder sb = new StringBuilder();
		sb.append("select count(*) from CmqRelation190 c where c.cmqCode = :cmqCode");
		
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

	@Override
	public List<CmqParentChild200> findParentsByCmqCode(Long cmqCode) {
		List<CmqParentChild200> retVal = null;
		StringBuilder sb = new StringBuilder();
		sb.append("from CmqParentChild200 c where c.cmqChildCode = :cmqCode ");
		EntityManager entityManager = this.cqtEntityManagerFactory.getEntityManager();
		try {
			Query query = entityManager.createQuery(sb.toString());
			query.setParameter("cmqCode", cmqCode);
			query.setHint("org.hibernate.cacheable", true);
			retVal = query.getResultList();
		} catch (Exception e) {
			StringBuilder msg = new StringBuilder();
			msg
					.append("An error occurred while fetching types from CmqParenChild200 on cmqCode ")
					.append(cmqCode)
					.append(" Query used was ->")
					.append(sb.toString());
			LOG.error(msg.toString(), e);
		} finally {
			this.cqtEntityManagerFactory.closeEntityManager(entityManager);
		}
		return retVal;
	}

	@Override
	public List<CmqParentChild200> findChildsByCmqCode(Long parentCmqCode) {
		List<CmqParentChild200> retVal = null;
		StringBuilder sb = new StringBuilder();
		sb.append("from CmqParentChild200 c where c.cmqParentCode = :cmqCode ");
		EntityManager entityManager = this.cqtEntityManagerFactory.getEntityManager();
		try {
			Query query = entityManager.createQuery(sb.toString());
			query.setParameter("cmqCode", parentCmqCode);
			query.setHint("org.hibernate.cacheable", true);
			retVal = query.getResultList();
		} catch (Exception e) {
			StringBuilder msg = new StringBuilder();
			msg
					.append("An error occurred while fetching childs from CmqParentChild200 on cmqParentCode ")
					.append(parentCmqCode)
					.append(" Query used was ->")
					.append(sb.toString());
			LOG.error(msg.toString(), e);
		} finally {
			this.cqtEntityManagerFactory.closeEntityManager(entityManager);
		}
		return retVal;
	}

	@Override
	public Long findCmqChildCountForParentCmqCode(Long parentCmqCode) {
		Long retVal = null;
		StringBuilder sb = new StringBuilder();
		sb.append("select count(*) from CmqParentChild200 c where c.cmqParentCode = :cmqCode");
		
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

	@Override
	public Long findCmqParentCountForChildCmqCode(Long childCmqCode) {
		Long retVal = null;
		StringBuilder sb = new StringBuilder();
		sb.append("select count(*) from CmqParentChild200 c where c.cmqChildCode = :cmqCode");
		
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
	
	@SuppressWarnings("unchecked")
    @Override
	public List<Map<String, Object>> findCmqChildCountForParentCmqCodes(List<Long> cmqCodes) {
		List<Map<String, Object>> retVal = null;
        
        if(CollectionUtils.isEmpty(cmqCodes))
            return null;
        
		String queryString = CmqUtils.convertArrayToTableWith(cmqCodes, "tempCmqCodes", "code")
                + " select CMQ_CHILD_CODE as CMQ_CODE, count(*) as COUNT"
                + " from CMQ_PARENT_CHILD_CURRENT cmqTbl"
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

	@Override
	public List<CmqParentChild200> findChildCmqsByParentCodes(List<Long> parentCmqCodes) {

		List<CmqParentChild200> retVal = null;
		String queryString = "from CmqParentChild200 c where c.cmqParentCode in (:codeList) ";
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

	@Override
	public List<CmqParentChild200> findParentCmqsByChildCodes(List<Long> childCmqCodes) {
		List<CmqParentChild200> retVal = null;
		String queryString = "from CmqParentChild200 c where c.cmqChildCode in (:codeList) ";
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

	@Override
	public CmqParentChild200 findByParentAndChildCode(Long parentCmqCode, Long childCmqCode) {
		CmqParentChild200 retVal = null;
		String queryString = "from CmqParentChild200 c where c.cmqParentCode = :cmqParentCode and  c.cmqChildCode = :cmqChildCode";
		EntityManager entityManager = this.cqtEntityManagerFactory
				.getEntityManager();
		try {
			Query query = entityManager.createQuery(queryString);
			query.setParameter("cmqParentCode", parentCmqCode);
			query.setParameter("cmqChildCode", childCmqCode);
			query.setHint("org.hibernate.cacheable", true);
			retVal = (CmqParentChild200) query.getSingleResult();
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
	public List<CmqParentChild200> findByParentOrChildCmqCode(Long cmqCode) {
		List<CmqParentChild200> retVal = null;
		StringBuilder sb = new StringBuilder();
		sb.append("from CmqParentChild200 c where c.cmqParentCode = :cmqCode or c.cmqChildCode = :cmqCode");
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
