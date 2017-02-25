package com.dbms.service;

import java.util.List;

import javax.faces.bean.ApplicationScoped;
import javax.faces.bean.ManagedBean;
import javax.persistence.EntityManager;
import javax.persistence.Query;

import org.hibernate.SQLQuery;
import org.hibernate.Session;
import org.hibernate.transform.Transformers;
import org.hibernate.type.LongType;
import org.hibernate.type.SingleColumnType;
import org.hibernate.type.StandardBasicTypes;
import org.hibernate.type.Type;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dbms.entity.cqt.MeddraDict190;
import com.dbms.entity.cqt.SmqBase190;
import com.dbms.entity.cqt.dtos.MeddraDictHierarchySearchDto;
import com.dbms.service.base.CqtPersistenceService;

/**
 * @author Jay G.(jayshanchn@hotmail.com)
 * @date Feb 11, 2017 8:00:48 AM
 **/
@ManagedBean(name = "MeddraDictService")
@ApplicationScoped
public class MeddraDictService extends CqtPersistenceService<MeddraDict190> implements IMeddraDictService {

	private static final Logger LOG = LoggerFactory.getLogger(MeddraDictService.class);

	@Override
	@SuppressWarnings("unchecked")
	public List<MeddraDictHierarchySearchDto> findByLevelAndTerm(String searchColumnTypePrefix, String searchTerm) {
		List<MeddraDictHierarchySearchDto> retVal = null;
		String termColumnName = searchColumnTypePrefix + "TERM";
		String codeColumnName = searchColumnTypePrefix + "CODE";
		String queryString = "select MEDDRA_DICT_ID as meddraDictId, " + termColumnName + " as term, " + codeColumnName
				+ " as code from (select MEDDRA_DICT_ID, " + termColumnName + ", " + codeColumnName
				+ ", row_number() over (partition by " + codeColumnName
				+ " order by MEDDRA_DICT_ID) rn from MEDDRA_DICT_CURRENT where upper(" + termColumnName
				+ ")  like :searchTerm ) where rn = 1";

		EntityManager entityManager = this.cqtEntityManagerFactory.getEntityManager();
		Session session = entityManager.unwrap(Session.class);
		try {
			SQLQuery query = session.createSQLQuery(queryString);
			query.addScalar("meddraDictId", StandardBasicTypes.LONG);
			query.addScalar("term", StandardBasicTypes.STRING);
			query.addScalar("code", StandardBasicTypes.STRING);
			query.setParameter("searchTerm", searchTerm.toUpperCase());
			query.setResultTransformer(Transformers.aliasToBean(MeddraDictHierarchySearchDto.class));
			
			retVal = query.list();
		} catch (Exception e) {
			StringBuilder msg = new StringBuilder();
			msg.append("An error occured while fetching types from MeddraDict190 on searchColumnType ")
					.append(termColumnName).append(" with value like ").append(searchTerm)
					.append(" Query used was ->").append(queryString);
			LOG.error(msg.toString(), e);
		} finally {
			this.cqtEntityManagerFactory.closeEntityManager(entityManager);
		}
		return retVal;
	}

	@SuppressWarnings("unchecked")
	public MeddraDictHierarchySearchDto findByCode(String searchColumnTypePrefix, Long code) {
		MeddraDictHierarchySearchDto retVal = null;
		String termColumnName = searchColumnTypePrefix + "TERM";
		String codeColumnName = searchColumnTypePrefix + "CODE";
		String queryString = "select MEDDRA_DICT_ID as meddraDictId, " + termColumnName + " as term, " + codeColumnName
				+ " as code from (select MEDDRA_DICT_ID, " + termColumnName + ", " + codeColumnName
				+ ", row_number() over (partition by " + codeColumnName
				+ " order by MEDDRA_DICT_ID) rn from MEDDRA_DICT_CURRENT where " + codeColumnName
				+ " = :code ) where rn = 1";
		
		EntityManager entityManager = this.cqtEntityManagerFactory.getEntityManager();
		Session session = entityManager.unwrap(Session.class);
		try {
			SQLQuery query = session.createSQLQuery(queryString);
			query.addScalar("meddraDictId", StandardBasicTypes.LONG);
			query.addScalar("term", StandardBasicTypes.STRING);
			query.addScalar("code", StandardBasicTypes.STRING);
			query.setParameter("code", code);
			query.setResultTransformer(Transformers.aliasToBean(MeddraDictHierarchySearchDto.class));
			
			List<MeddraDictHierarchySearchDto> dataList = query.list();
			if((null != dataList) && (dataList.size() > 0)) {
				retVal = dataList.get(0);
			}
		} catch (Exception e) {
			StringBuilder msg = new StringBuilder();
			msg.append("An error occured while fetching types from MeddraDict190 on searchColumnType ")
					.append(termColumnName).append(" with code equal to ").append(code)
					.append(" Query used was ->").append(queryString);
			LOG.error(msg.toString(), e);
		} finally {
			this.cqtEntityManagerFactory.closeEntityManager(entityManager);
		}
		return retVal;
	}
	
}
