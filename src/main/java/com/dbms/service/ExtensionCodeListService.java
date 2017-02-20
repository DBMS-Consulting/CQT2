package com.dbms.service;

import java.util.List;

import javax.faces.bean.ApplicationScoped;
import javax.faces.bean.ManagedBean;
import javax.persistence.EntityManager;
import javax.persistence.Query;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dbms.entity.cqt.ExtentionConfigCodeList;
import com.dbms.service.base.CqtPersistenceService;

@ManagedBean(name = "ExtensionCodeListService")
@ApplicationScoped
public class ExtensionCodeListService extends CqtPersistenceService<ExtentionConfigCodeList>
		implements IExtensionCodeListService {

	private static final Logger LOG = LoggerFactory.getLogger(ExtensionCodeListService.class);

	@Override
	@SuppressWarnings("rawtypes")
	public ExtentionConfigCodeList findByValue(String value) {
		ExtentionConfigCodeList retVal = null;
		EntityManager entityManager = this.cqtEntityManagerFactory.getEntityManager();

		StringBuilder queryString = new StringBuilder("from ExtentionConfigCodeList a");
		queryString.append(" where a.value = :value");
		try {
			Query query = entityManager.createQuery(queryString.toString());
			query.setParameter("value", value);

			List results = query.getResultList();
			if (!results.isEmpty()) {
				retVal = (ExtentionConfigCodeList) results.get(0);
			}
		} catch (Exception ex) {
			StringBuilder msg = new StringBuilder();
			msg
					.append("findById with fetchFields, failed for type '")
					.append("ExtentionConfigCodeList")
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
