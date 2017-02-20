package com.dbms.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.faces.bean.ApplicationScoped;
import javax.faces.bean.ManagedBean;
import javax.persistence.EntityManager;
import javax.persistence.Query;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dbms.entity.cqt.CmqBase190;
import com.dbms.service.base.CqtPersistenceService;

/**
 * @author Jay G.(jayshanchn@hotmail.com)
 * @date Feb 11, 2017 8:00:48 AM
 **/
@ManagedBean(name = "CmqBase190Service")
@ApplicationScoped
public class CmqBase190Service extends CqtPersistenceService<CmqBase190> implements ICmqBase190Service {

	private static final Logger LOG = LoggerFactory.getLogger(CmqBase190Service.class);

	private StringBuilder appendClause(StringBuilder sb, boolean first) {
		if (first) {
			sb.append(" where");
		} else {
			sb.append(" and");
		}
		return sb;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.dbms.service.ICmqBase190Service#findByCriterias(java.lang.String,
	 * java.lang.String, java.lang.String, java.lang.String, java.lang.Integer,
	 * java.lang.String, java.lang.String, java.lang.String, java.lang.String,
	 * java.lang.String, java.lang.Long)
	 */
	@Override
	@SuppressWarnings("unchecked")
	public List<CmqBase190> findByCriterias(String extension, String drugProgram, String protocol, String product,
			Integer level, String status, String state, String criticalEvent, String group, String termName,
			Long code) {
		List<CmqBase190> retVal = null;
		StringBuilder sb = new StringBuilder("from CmqBase190 c");
		boolean first = true;
		Map<String, Object> queryParams = new HashMap<>();
		if (StringUtils.isNotEmpty(extension)) {
			sb = appendClause(sb, first);
			sb.append(" c.cmqTypeCd=:cmqTypeCd");
			queryParams.put("cmqTypeCd", extension);
			first = false;
		}
		if (StringUtils.isNoneEmpty(drugProgram)) {
			sb = appendClause(sb, first);
			sb.append(" lower(c.cmqProgramCd) like lower(:cmqProgramCd)");
			queryParams.put("cmqProgramCd", "%" + drugProgram + "%");
			first = false;
		}
		if (StringUtils.isNotEmpty(protocol)) {
			sb = appendClause(sb, first);
			sb.append(" lower(c.cmqProtocolCd) like lower(:cmqProtocolCd)");
			queryParams.put("cmqProtocolCd", "%" + protocol + "%");
			first = false;
		}
		if (StringUtils.isNotEmpty(product)) {
			sb = appendClause(sb, first);
			sb.append(" lower(c.cmqProductCd) like lower(:cmqProductCd)");
			queryParams.put("cmqProductCd", "%" + product + "%");
			first = false;
		}
		if (level != null) {
			sb = appendClause(sb, first);
			sb.append(" c.cmqLevel=:cmqLevel");
			queryParams.put("cmqLevel", level);
			first = false;
		}
		if (StringUtils.isNotEmpty(status)) {
			sb = appendClause(sb, first);
			sb.append(" c.cmqStatus=:cmqStatus");
			queryParams.put("cmqStatus", status);
			first = false;
		}
		if (StringUtils.isNotEmpty(state)) {
			sb = appendClause(sb, first);
			sb.append(" lower(c.cmqState)=lower(:cmqState)");
			queryParams.put("cmqState", state);
			first = false;
		}
		if (StringUtils.isNotEmpty(criticalEvent)) {
			sb = appendClause(sb, first);
			sb.append(" c.cmqCriticalEvent=:cmqCriticalEvent");
			queryParams.put("cmqCriticalEvent", criticalEvent);
			first = false;
		}
		if (StringUtils.isNotEmpty(group)) {
			sb = appendClause(sb, first);
			sb.append(" lower(c.cmqGroup) like lower(:cmqGroup)");
			queryParams.put("cmqGroup", "%" + group + "%");
			first = false;
		}
		if (StringUtils.isNotEmpty(termName)) {
			sb = appendClause(sb, first);
			sb.append(" lower(c.cmqName) like lower(:cmqName)");
			queryParams.put("cmqName", "%" + termName + "%");
			first = false;
		}
		if (code != null) {
			sb = appendClause(sb, first);
			sb.append(" c.cmqCode=:cmqCode");
			queryParams.put("cmqCode", code);
			first = false;
		}
		EntityManager entityManager = this.cqtEntityManagerFactory.getEntityManager();
		try {
			Query query = entityManager.createQuery(sb.toString());

			// now set the parameter values in the query
			Set<String> keySet = queryParams.keySet();
			for (String key : keySet) {
				query.setParameter(key, queryParams.get(key));
			}

			retVal = query.getResultList();
		} catch (Exception e) {
			StringBuilder msg = new StringBuilder();
			msg.append("An error occured while fetching data from CmqBase190.").append("Query used was ->").append(sb);
			LOG.error(msg.toString(), e);
		} finally {
			this.cqtEntityManagerFactory.closeEntityManager(entityManager);
		}
		return retVal;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.dbms.service.ICmqBase190Service#findTypes()
	 */
	@Override
	@SuppressWarnings("unchecked")
	public List<String> findTypes() {
		List<String> retVal = null;
		String query = "select distinct c.type from CmqBase190 c";
		EntityManager entityManager = this.cqtEntityManagerFactory.getEntityManager();
		try {
			retVal = entityManager.createQuery(query).getResultList();
		} catch (Exception e) {
			StringBuilder msg = new StringBuilder();
			msg.append("An error occured while fetching types from CmqBase190.").append("Query used was ->").append(
					query);
			LOG.error(msg.toString(), e);
		} finally {
			this.cqtEntityManagerFactory.closeEntityManager(entityManager);
		}
		return retVal;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.dbms.service.ICmqBase190Service#findReleaseStatus()
	 */
	@Override
	@SuppressWarnings("unchecked")
	public List<String> findReleaseStatus() {
		List<String> retVal = null;
		String query = "select distinct c.releaseStatus from CmqBase190 c";
		EntityManager entityManager = this.cqtEntityManagerFactory.getEntityManager();
		try {
			retVal = entityManager.createQuery(query).getResultList();
		} catch (Exception e) {
			StringBuilder msg = new StringBuilder();
			msg
					.append("An error occured while fetching ReleaseStatus from CmqBase190.")
					.append("Query used was ->")
					.append(query);
			LOG.error(msg.toString(), e);
		} finally {
			this.cqtEntityManagerFactory.closeEntityManager(entityManager);
		}
		return retVal;
	}
}
