package com.dbms.service;

import java.util.List;

import javax.faces.bean.ApplicationScoped;
import javax.faces.bean.ManagedBean;

import org.hibernate.Session;

import com.dbms.entity.cqt.CmqRelation190;
import com.dbms.util.HibernateUtil;

/**
 * @author Jay G.(jayshanchn@hotmail.com)
 * @date Feb 11, 2017 8:00:48 AM
 **/
@ManagedBean
@ApplicationScoped
public class CmqRelation190Service extends HibernatePersistenceService<CmqRelation190> {
	@SuppressWarnings("unchecked")
	public List<CmqRelation190> findBaseWithRootRelations(){
		List<CmqRelation190> result=null;
		Session session = HibernateUtil.currentSession();
		try {
			result=session.createNamedQuery("CmqRelation190.rootRelations").getResultList();
		}catch(Exception ex){
			log.error("Error - {}",ex.getMessage(),ex);
		}
		finally{
			HibernateUtil.closeSession();
		}
		return result;
	}

	public CmqRelation190 findByTermName(String termName) {
		CmqRelation190 result=null;
		Session session=HibernateUtil.currentSession();
		try{
			result=(CmqRelation190) session.createNamedQuery("CmqRelation190.findByTermName").setParameter("termName", termName).getSingleResult();
		}catch(Exception ex){
			log.error("Error - {}",ex.getMessage(),ex);
		}finally{
			HibernateUtil.closeSession();
		}
		return result;
	}
}

