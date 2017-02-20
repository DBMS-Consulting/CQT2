package com.dbms.service;

import java.util.List;

import javax.faces.bean.ApplicationScoped;
import javax.faces.bean.ManagedBean;
import javax.persistence.EntityManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dbms.entity.cqt.CmqRelation190;
import com.dbms.service.base.CqtPersistenceService;

/**
 * @author Jay G.(jayshanchn@hotmail.com)
 * @date Feb 11, 2017 8:00:48 AM
 **/
@ManagedBean(name = "CmqRelation190Service")
@ApplicationScoped
public class CmqRelation190Service extends CqtPersistenceService<CmqRelation190> implements ICmqRelation190Service {

	private static final Logger LOG = LoggerFactory.getLogger(CmqRelation190Service.class);

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.dbms.service.ICmqRelation190Service#findBaseWithRootRelations()
	 */
	@Override
	@SuppressWarnings("unchecked")
	public List<CmqRelation190> findBaseWithRootRelations() {
		List<CmqRelation190> result = null;
		EntityManager entityManager = this.cqtEntityManagerFactory.getEntityManager();
		try {
			result = entityManager.createNamedQuery("CmqRelation190.rootRelations").getResultList();
		} catch (Exception ex) {
			StringBuilder msg = new StringBuilder();
			msg.append("An error occured while executing named query CmqRelation190.rootRelations");
			LOG.error(msg.toString(), ex);
		} finally {
			this.cqtEntityManagerFactory.closeEntityManager(entityManager);
		}
		return result;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.dbms.service.ICmqRelation190Service#findByTermName(java.lang.String)
	 */
	@Override
	public CmqRelation190 findByTermName(String termName) {
		CmqRelation190 result = null;
		EntityManager entityManager = this.cqtEntityManagerFactory.getEntityManager();
		try {
			result = entityManager
					.createNamedQuery("CmqRelation190.findByTermName", CmqRelation190.class)
					.setParameter("termName", termName)
					.getSingleResult();
		} catch (Exception ex) {
			StringBuilder msg = new StringBuilder();
			msg.append("An error occured while executing named query CmqRelation190.findByTermName");
			LOG.error(msg.toString(), ex);
		} finally {
			this.cqtEntityManagerFactory.closeEntityManager(entityManager);
		}
		return result;
	}
}
