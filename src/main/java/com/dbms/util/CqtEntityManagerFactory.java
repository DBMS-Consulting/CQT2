package com.dbms.util;

import java.util.Map;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.faces.bean.ApplicationScoped;
import javax.faces.bean.ManagedBean;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Jay G.(jayshanchn@hotmail.com)
 * @date Feb 10, 2017 7:10:58 AM
 **/
@ManagedBean(name = "CqtEntityManagerFactory", eager = true)
@ApplicationScoped
public class CqtEntityManagerFactory implements ICqtEntityManagerFactory {
	private static final Logger LOG = LoggerFactory.getLogger(CqtEntityManagerFactory.class);

	private EntityManagerFactory entityManagerFactory;

	@PostConstruct
	private void initialize() {
		if (LOG.isInfoEnabled()) {
			LOG.info("Initializing CQT JPA2.0 EntityManagerFactory.");
		}
		this.entityManagerFactory = Persistence.createEntityManagerFactory("cqtJPA");
	}

	@PreDestroy
	private void destroy() {
		if (LOG.isInfoEnabled()) {
			LOG.info("Closing CQT JPA2.0 EntityManagerFactory.");
		}
		if (this.entityManagerFactory.isOpen()) {
			this.entityManagerFactory.close();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.dbms.util.ICqtEntityManagerFactory#getEntityManager()
	 */
	@Override
	public EntityManager getEntityManager() {
		EntityManager entityManager = this.entityManagerFactory.createEntityManager();
		return entityManager;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.dbms.util.ICqtEntityManagerFactory#getEntityManager(java.util.Map)
	 */
	@Override
	@SuppressWarnings("rawtypes")
	public EntityManager getEntityManager(Map map) {
		EntityManager entityManager = this.entityManagerFactory.createEntityManager(map);
		return entityManager;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.dbms.util.ICqtEntityManagerFactory#closeEntityManager(javax.
	 * persistence.EntityManager)
	 */
	@Override
	public void closeEntityManager(EntityManager entityManager) {
		if(entityManager.getTransaction().isActive()) {
			entityManager.flush();
		}
		entityManager.close();
	}
}
