package com.dbms.service;

import java.util.List;

import javax.faces.bean.ApplicationScoped;
import javax.faces.bean.ManagedBean;
import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dbms.entity.cqt.CmqRelation190;
import com.dbms.service.base.CqtPersistenceService;
import com.dbms.util.exceptions.CqtServiceException;

/**
 * @author Jay G.(jayshanchn@hotmail.com)
 * @date Feb 11, 2017 8:00:48 AM
 **/
@ManagedBean(name = "CmqRelation190Service")
@ApplicationScoped
public class CmqRelation190Service extends CqtPersistenceService<CmqRelation190> implements ICmqRelation190Service {

	private static final Logger LOG = LoggerFactory.getLogger(CmqRelation190Service.class);

	public void create(List<CmqRelation190> cmqRelations) throws CqtServiceException {
		EntityManager entityManager = this.cqtEntityManagerFactory.getEntityManager();
		EntityTransaction tx = null;
		try {
			tx = entityManager.getTransaction();
			tx.begin();
			for (CmqRelation190 cmqRelation : cmqRelations) {
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
}
