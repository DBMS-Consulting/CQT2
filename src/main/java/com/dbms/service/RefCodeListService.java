package com.dbms.service;

import java.util.ArrayList;
import java.util.List;

import javax.faces.bean.ApplicationScoped;
import javax.faces.bean.ManagedBean;
import javax.persistence.EntityManager;
import javax.persistence.Query;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dbms.entity.cqt.RefConfigCodeList;
import com.dbms.service.base.CqtPersistenceService;
import com.dbms.util.CqtConstants;
import com.dbms.util.OrderBy;

@ManagedBean(name = "RefCodeListService")
@ApplicationScoped
public class RefCodeListService extends CqtPersistenceService<RefConfigCodeList> implements IRefCodeListService {

	private static final Logger LOG = LoggerFactory.getLogger(RefCodeListService.class);

	@Override
	@SuppressWarnings({ "unchecked" })
	public List<RefConfigCodeList> findByConfigType(String codelistConfigType, OrderBy orderBy) {
		List<RefConfigCodeList> retVal = null;
		EntityManager entityManager = this.cqtEntityManagerFactory.getEntityManager();

		StringBuilder queryString = new StringBuilder("from RefConfigCodeList a");
		queryString.append(" where a.codelistConfigType = :codelistConfigType order by a.serialNum ");
		queryString.append(orderBy.name());
		try {
			Query query = entityManager.createQuery(queryString.toString());
			query.setParameter("codelistConfigType", codelistConfigType);

			retVal = query.getResultList();
			if (null == retVal) {
				retVal = new ArrayList<>();
			}
		} catch (Exception ex) {
			StringBuilder msg = new StringBuilder();
			msg
					.append("findByConfigType failed for type '")
					.append("RefConfigCodeList")
					.append("' and value of ")
					.append(codelistConfigType)
					.append(". Query used was->")
					.append(queryString);
			LOG.error(msg.toString(), ex);
		} finally {
			this.cqtEntityManagerFactory.closeEntityManager(entityManager);
		}
		return retVal;
	}

	@SuppressWarnings("unchecked")
	public RefConfigCodeList getCurrentMeddraVersion() {
		RefConfigCodeList retVal = null;
		EntityManager entityManager = this.cqtEntityManagerFactory.getEntityManager();

		StringBuilder queryString = new StringBuilder("from RefConfigCodeList a");
		queryString.append(" where a.codelistConfigType = :codelistConfigType ");
		queryString.append("and  a.codelistInternalValue = :codelistInternalValue");
		try {
			Query query = entityManager.createQuery(queryString.toString());
			query.setParameter("codelistConfigType", CqtConstants.CODE_LIST_TYPE_MEDDRA_VERSIONS);
			query.setParameter("codelistInternalValue", CqtConstants.CURRENT_MEDDRA_VERSION);

			List<RefConfigCodeList> result = query.getResultList();
			if ((null != result) && result.size() > 0) {
				retVal = result.get(0);
			}
		} catch (Exception ex) {
			StringBuilder msg = new StringBuilder();
			msg
					.append("getCurrentMeddraVersion failed for codelistConfigType ")
					.append(CqtConstants.CODE_LIST_TYPE_MEDDRA_VERSIONS)
					.append(" and codelistInternalValue ")
					.append(CqtConstants.CURRENT_MEDDRA_VERSION)
					.append("Query used was->")
					.append(queryString);
			LOG.error(msg.toString(), ex);
		} finally {
			this.cqtEntityManagerFactory.closeEntityManager(entityManager);
		}
		return retVal;
	}

}
