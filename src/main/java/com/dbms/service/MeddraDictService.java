package com.dbms.service;

import java.util.List;

import javax.faces.bean.ApplicationScoped;
import javax.faces.bean.ManagedBean;
import javax.persistence.EntityManager;
import javax.persistence.Query;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dbms.entity.cqt.MeddraDict190;
import com.dbms.service.base.CqtPersistenceService;

/**
 * @author Jay G.(jayshanchn@hotmail.com)
 * @date Feb 11, 2017 8:00:48 AM
 **/
@ManagedBean(name = "MeddraDictService")
@ApplicationScoped
public class MeddraDictService extends CqtPersistenceService<MeddraDict190> implements IMeddraDictService {
	
	private static final Logger LOG = LoggerFactory.getLogger(MeddraDictService.class);
	
	/* (non-Javadoc)
	 * @see com.dbms.service.IMeddraDictService#findByLevelAndTerm(java.lang.String, java.lang.String)
	 */
	@Override
	@SuppressWarnings("unchecked")
	public List<MeddraDict190> findByLevelAndTerm(String searchColumnType, String searchTerm) {
		List<MeddraDict190> retVal = null;
		String queryString = "from MeddraDict190 c where upper(" + searchColumnType + ") like :searchColumnType";
		EntityManager entityManager = this.cqtEntityManagerFactory.getEntityManager();
		try {
			Query query = entityManager.createQuery(queryString);
			query.setParameter("searchColumnType", searchTerm.toUpperCase());
			retVal = query.getResultList();
		} catch (Exception e) {
			StringBuilder msg = new StringBuilder();
			msg
					.append("An error occured while fetching types from MeddraDict190 on searchColumnType ")
					.append(searchColumnType)
					.append(" with value like ")
					.append(searchTerm)
					.append(" Query used was ->")
					.append(queryString);
			LOG.error(msg.toString(), e);
		} finally {
			this.cqtEntityManagerFactory.closeEntityManager(entityManager);
		}
		return retVal;
	}
	
}

