package com.dbms.service;

import java.util.List;
import java.util.Map;

import javax.faces.bean.ApplicationScoped;
import javax.faces.bean.ManagedBean;
import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import org.primefaces.model.SortOrder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dbms.entity.cqt.CmqBaseTarget;
import com.dbms.service.base.CqtPersistenceService;

/**
 * @author Jay G.(jayshanchn@hotmail.com)
 * @date Feb 11, 2017 8:00:48 AM
 **/
@ManagedBean(name = "CmqBaseTargetService")
@ApplicationScoped
public class CmqBaseTargetService extends CqtPersistenceService<CmqBaseTarget> implements ICmqBaseTargetService {

	private static final Logger LOG = LoggerFactory
			.getLogger(CmqBaseTargetService.class);

	/* (non-Javadoc)
	 * @see com.dbms.service.ICmqBaseTargetService#findImpactedWithPaginated(int, int, java.lang.String, org.primefaces.model.SortOrder, java.util.Map)
	 */
	@Override
	public List<CmqBaseTarget> findImpactedWithPaginated(int first, int pageSize, String sortField
														, SortOrder sortOrder, Map<String, Object> filters) {
		List<CmqBaseTarget> retVal = null;
		EntityManager entityManager = this.cqtEntityManagerFactory.getEntityManager();
		try {
			CriteriaBuilder cb = entityManager.getCriteriaBuilder();
			CriteriaQuery<CmqBaseTarget> cq = cb.createQuery(CmqBaseTarget.class);
			Root<CmqBaseTarget> cmqRoot = cq.from(CmqBaseTarget.class);
			cq.where(cmqRoot.get("cmqId").isNotNull());
			cq.orderBy(cb.asc(cmqRoot.get("cmqName")));
			TypedQuery<CmqBaseTarget> tq = entityManager.createQuery(cq);
			
			if (pageSize >= 0){
	            tq.setMaxResults(pageSize);
	        }
	        if (first >= 0){
	            tq.setFirstResult(first);
	        }
			
			retVal = tq.getResultList();
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
		sb.append("select count(*) from CmqBaseTarget c where c.cmqId is not null");
		
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
	 * @see com.dbms.service.ICmqBaseTargetService#findNotImpactedWithPaginated(int, int, java.lang.String, org.primefaces.model.SortOrder, java.util.Map)
	 */
	@Override
	public List<CmqBaseTarget> findNotImpactedWithPaginated(int first, int pageSize, String sortField
			, SortOrder sortOrder, Map<String, Object> filters) {
		List<CmqBaseTarget> retVal = null;
		EntityManager entityManager = this.cqtEntityManagerFactory.getEntityManager();
		try {
			CriteriaBuilder cb = entityManager.getCriteriaBuilder();
			CriteriaQuery<CmqBaseTarget> cq = cb.createQuery(CmqBaseTarget.class);
			Root<CmqBaseTarget> cmqRoot = cq.from(CmqBaseTarget.class);
			cq.where(cmqRoot.get("cmqId").isNotNull());
			cq.orderBy(cb.asc(cmqRoot.get("cmqName")));
			TypedQuery<CmqBaseTarget> tq = entityManager.createQuery(cq);
			
			if (pageSize >= 0){
			tq.setMaxResults(pageSize);
			}
			if (first >= 0){
			tq.setFirstResult(first);
			}
			
			retVal = tq.getResultList();
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
		sb.append("select count(*) from CmqBaseTarget c where c.cmqId is not null");
	
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
}
