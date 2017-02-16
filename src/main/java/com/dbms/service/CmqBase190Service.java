package com.dbms.service;

import java.util.List;

import javax.faces.bean.ApplicationScoped;
import javax.faces.bean.ManagedBean;
import javax.persistence.Query;

import org.apache.commons.lang3.StringUtils;
import org.hibernate.Session;

import com.dbms.entity.cqt.CmqBase190;
import com.dbms.util.HibernateUtil;

/**
 * @author Jay G.(jayshanchn@hotmail.com)
 * @date Feb 11, 2017 8:00:48 AM
 **/
@ManagedBean()
@ApplicationScoped
public class CmqBase190Service extends HibernatePersistenceService<CmqBase190> {

	private StringBuilder appendClause(StringBuilder sb, boolean first) {
		if (first) {
			sb.append(" where");
		} else {
			sb.append(" and");
		}
		return sb;
	}
	
	@SuppressWarnings("unchecked")
	public List<CmqBase190> findByCriterias(String extension,
			String drugProgram, String protocol, String product, Integer level,
			String status, String state, String criticalEvent, String group,
			String termName, Long code) {
		StringBuilder sb = new StringBuilder("from CmqBase190 c");
		boolean first = true;
		if (StringUtils.isNotEmpty(extension)) {
			sb.append(" where c.type=:type");
			first = false;
		}
		if (StringUtils.isNoneEmpty(drugProgram)) {
			sb = appendClause(sb, first);
			sb.append(" lower(c.drugProgram) like lower(:drugProgram)");
			first = false;
		}
		if (StringUtils.isNotEmpty(protocol)) {
			sb = appendClause(sb, first);
			sb.append(" lower(c.protocolName) like lower(:protocolName)");
			first = false;
		}
		if(StringUtils.isNotEmpty(product)){
			sb = appendClause(sb, first);
			sb.append(" lower(c.productName) like lower(:product)");
			first = false;
		}
		if(level!=null){
			sb=appendClause(sb,first);
			sb.append(" c.level=:level");
		}
		if(StringUtils.isNotEmpty(status)){
			sb = appendClause(sb, first);
			sb.append(" c.status=:status");
			first = false;
		}
		if(StringUtils.isNotEmpty(state)){
			sb = appendClause(sb, first);
			sb.append(" lower(c.state)=lower(:state)");
			first = false;
		}
		if(StringUtils.isNotEmpty(criticalEvent)){
			sb = appendClause(sb, first);
			sb.append(" c.criticalEvent=:criticalEvent");
			first = false;
		}
		if(StringUtils.isNotEmpty(group)){
			sb = appendClause(sb, first);
			sb.append(" lower(c.group) like lower(:group)");
			first = false;
		}
		if(StringUtils.isNotEmpty(termName)){
			sb = appendClause(sb, first);
			sb.append(" lower(c.name) like lower(:termName)");
			first = false;
		}
		if(code!=null){
			sb = appendClause(sb, first);
			sb.append(" c.id=:code");
			first = false;
		}
		Session session = HibernateUtil.currentSession();
		try {
			Query query = session.createQuery(sb.toString());
			if (StringUtils.isNotEmpty(extension)) {
				query.setParameter("type", extension);
			}
			if (StringUtils.isNotEmpty(drugProgram)) {
				query.setParameter("drugProgram", "%" + drugProgram + "%");
			}
			if (StringUtils.isNotEmpty(protocol)) {
				query.setParameter("protocolName", "%" + protocol + "%");
			}
			if(StringUtils.isNotEmpty(product)){
				query.setParameter("product", "%"+product+"%");
			}
			if(level!=null){
				query.setParameter("level", level);
			}
			if(StringUtils.isNotEmpty(status)){
				query.setParameter("status", status);
			}

			if(StringUtils.isNotEmpty(state)){
				query.setParameter("state", state);
			}
			
			if(StringUtils.isNotEmpty(criticalEvent)){
				query.setParameter("criticalEvent", criticalEvent);
			}
			
			if(StringUtils.isNotEmpty(group)){
				query.setParameter("group", "%"+group+"%");
			}
			if(StringUtils.isNotEmpty(termName)){
				query.setParameter("termName", "%"+termName+"%");
			}
			if(code!=null){
				query.setParameter("code", code);
			}
			return query.getResultList();
		} catch (Exception e) {
			log.error("Error - {}",e.getMessage(),e);
		} finally {
			HibernateUtil.closeSession();
		}
		return null;
	}
	@SuppressWarnings("unchecked")
	public List<String> findTypes() {
		Session session = HibernateUtil.currentSession();
		try {
			return session.createQuery("select distinct c.type from CmqBase190 c").getResultList();
		}finally{
			HibernateUtil.closeSession();
		}
	}
	@SuppressWarnings("unchecked")
	public List<String> findReleaseStatus() {
		Session session = HibernateUtil.currentSession();
		try {
			return session.createQuery(
					"select distinct c.releaseStatus from CmqBase190 c").getResultList();
		} finally {
			HibernateUtil.closeSession();
		}
	}
}

