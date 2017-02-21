package com.dbms.service;

import java.util.List;

import javax.faces.bean.ApplicationScoped;
import javax.faces.bean.ManagedBean;
import javax.persistence.EntityManager;
import javax.persistence.Query;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dbms.entity.cqt.SmqBase190;
import com.dbms.service.base.CqtPersistenceService;

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
			retVal = query.getResultList();
		} catch (Exception e) {
			StringBuilder msg = new StringBuilder();
			msg
					.append("An error occured while fetching types from SmqBase190 on smqLevel ")
					.append(level)
					.append(" with smqName like ")
					.append("%" + searchTerm.toUpperCase() + "%")
					.append(" Query used was ->")
					.append(sb.toString());
			LOG.error(msg.toString(), e);
		} finally {
			this.cqtEntityManagerFactory.closeEntityManager(entityManager);
		}
		return retVal;
	}

}
