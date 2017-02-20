package com.dbms.service;

import java.util.List;

import javax.faces.bean.ApplicationScoped;
import javax.faces.bean.ManagedBean;
import javax.persistence.EntityManager;
import javax.persistence.Query;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dbms.entity.cqt.ProgramConfigCodeList;
import com.dbms.service.base.CqtPersistenceService;

@ManagedBean(name = "ProgramCodeListService")
@ApplicationScoped
public class ProgramCodeListService extends CqtPersistenceService<ProgramConfigCodeList>
		implements IProgramCodeListService {

	private static final Logger LOG = LoggerFactory.getLogger(ExtensionCodeListService.class);

	@Override
	@SuppressWarnings("rawtypes")
	public ProgramConfigCodeList findByValue(String value) {
		ProgramConfigCodeList retVal = null;
		EntityManager entityManager = this.cqtEntityManagerFactory.getEntityManager();

		StringBuilder queryString = new StringBuilder("from ProgramConfigCodeList a");
		queryString.append(" where a.value = :value");
		try {
			Query query = entityManager.createQuery(queryString.toString());
			query.setParameter("value", value);

			List results = query.getResultList();
			if (!results.isEmpty()) {
				retVal = (ProgramConfigCodeList) results.get(0);
			}
		} catch (Exception ex) {
			StringBuilder msg = new StringBuilder();
			msg
					.append("findById with fetchFields, failed for type '")
					.append("ProgramConfigCodeList")
					.append("' and value of ")
					.append(value)
					.append(". Query used was->")
					.append(queryString);
			LOG.error(msg.toString(), ex);
		} finally {
			this.cqtEntityManagerFactory.closeEntityManager(entityManager);
		}
		return retVal;
	}
}
