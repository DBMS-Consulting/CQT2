package com.dbms.service;

import java.util.List;
import java.util.Map;

import javax.faces.bean.ApplicationScoped;
import javax.faces.bean.ManagedBean;
import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.persistence.Query;

import org.hibernate.SQLQuery;
import org.hibernate.Session;
import org.hibernate.transform.Transformers;
import org.hibernate.type.StandardBasicTypes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dbms.entity.cqt.CmqRelationTarget;
import com.dbms.service.base.CqtPersistenceService;
import com.dbms.util.exceptions.CqtServiceException;

/**
 * @author Jay G.(jayshanchn@hotmail.com)
 * @date Feb 11, 2017 8:00:48 AM
 **/
@ManagedBean(name = "CmqRelationTargetService")
@ApplicationScoped
public class CmqRelationTargetService extends CqtPersistenceService<CmqRelationTarget>
		implements ICmqRelationTargetService {

	private static final Logger LOG = LoggerFactory.getLogger(CmqRelationTargetService.class);

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.dbms.service.ICmqRelationTargetService#create(java.util.List)
	 */
	@Override
	public void create(List<CmqRelationTarget> cmqRelations) throws CqtServiceException {
		EntityManager entityManager = this.cqtEntityManagerFactory.getEntityManager();
		EntityTransaction tx = null;
		try {
			tx = entityManager.getTransaction();
			tx.begin();
			for (CmqRelationTarget cmqRelation : cmqRelations) {
				entityManager.persist(cmqRelation);
			}
			tx.commit();
		} catch (Exception ex) {
			if ((tx != null) && tx.isActive()) {
				tx.rollback();
			}
			StringBuilder msg = new StringBuilder();
			msg.append("Failed to save List<CmqRelationTarget>");
			LOG.error(msg.toString(), ex);
			throw new CqtServiceException(ex);
		} finally {
			this.cqtEntityManagerFactory.closeEntityManager(entityManager);
		}
	}

	@Override
	@SuppressWarnings("unchecked")
	public List<CmqRelationTarget> findByCmqCode(Long cmqCode) {
		List<CmqRelationTarget> retVal = null;
		StringBuilder sb = new StringBuilder();
		sb.append("from CmqRelationTarget c where c.cmqCode = :cmqCode ");
		EntityManager entityManager = this.cqtEntityManagerFactory.getEntityManager();
		try {
			Query query = entityManager.createQuery(sb.toString());
			query.setParameter("cmqCode", cmqCode);
			retVal = query.getResultList();
		} catch (Exception e) {
			StringBuilder msg = new StringBuilder();
			msg.append("An error occurred while fetching types from CmqRelationTarget on cmqCode ").append(cmqCode)
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
	 * @see
	 * com.dbms.service.ICmqRelationTargetService#findByCmqCode(java.lang.Long)
	 */
	@Override
	@SuppressWarnings("unchecked")
	public List<CmqRelationTarget> findByCmqCode(Long cmqCode, int startPosition, int limit) {
		List<CmqRelationTarget> retVal = null;
		StringBuilder sb = new StringBuilder();
		sb.append("from CmqRelationTarget c where c.cmqCode = :cmqCode ");
		EntityManager entityManager = this.cqtEntityManagerFactory.getEntityManager();
		try {
			Query query = entityManager.createQuery(sb.toString());
			query.setParameter("cmqCode", cmqCode);
			query.setFirstResult(startPosition);
			query.setMaxResults(limit);
			retVal = query.getResultList();
		} catch (Exception e) {
			StringBuilder msg = new StringBuilder();
			msg.append("An error occurred while fetching types from CmqRelationTarget on cmqCode ").append(cmqCode)
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
	 * @see
	 * com.dbms.service.ICmqRelationTargetService#findCountByCmqCodes(java.util.
	 * List)
	 */
	@Override
	@SuppressWarnings("unchecked")
	public List<Map<String, Object>> findCountByCmqCodes(List<Long> cmqCodes) {
		List<Map<String, Object>> retVal = null;
		StringBuilder sb = new StringBuilder();
		sb.append(
				"select CMQ_CODE, count(*) as COUNT from CMQ_RELATIONS_TARGET where CMQ_CODE in (:cmqCodes) group by CMQ_CODE");

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
			msg.append("An error occurred while findCountByCmqCodes ").append(cmqCodes).append(" Query used was ->")
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
	 * @see
	 * com.dbms.service.ICmqRelationTargetService#findCountByCmqCode(java.lang.
	 * Long)
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
			retVal = (Long) query.getSingleResult();
		} catch (Exception e) {
			StringBuilder msg = new StringBuilder();
			msg.append("An error occurred while findCountByCmqCode ").append(cmqCode).append(" Query used was ->")
					.append(sb.toString());
			LOG.error(msg.toString(), e);
		} finally {
			this.cqtEntityManagerFactory.closeEntityManager(entityManager);
		}
		return retVal;
	}
}
