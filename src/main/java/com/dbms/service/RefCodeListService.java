package com.dbms.service;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
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
	
	// members required for caching mechanism for performance of code-value interpretation for UI
	private static final int cacheValidTimeInMillis = 10000; // 10 sec
	private HashMap<String, RefConfigCodeListCache> codeListCache = new HashMap<String, RefConfigCodeListCache>();

	@Override
	@SuppressWarnings({ "unchecked" })
	public List<RefConfigCodeList> findByConfigType(String codelistConfigType, OrderBy orderBy) {
		List<RefConfigCodeList> retVal = null;
		EntityManager entityManager = this.cqtEntityManagerFactory.getEntityManager();

		StringBuilder queryString = new StringBuilder("from RefConfigCodeList a");
		queryString.append(" where a.codelistConfigType = :codelistConfigType and a.activeFlag = 'Y' order by a.serialNum ");
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
	
	@Override
	@SuppressWarnings({ "unchecked" })
	public List<RefConfigCodeList> findAllByConfigType(String codelistConfigType, OrderBy orderBy) {
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
	
	@Override
	public String findCodeByInternalCode(String codelistInternalValue) {
		RefConfigCodeList ref = null;
		String queryString = "from RefConfigCodeList c where c.codelistInternalValue = :codelistInternalValue";
		EntityManager entityManager = this.cqtEntityManagerFactory.getEntityManager();
		try {
			Query query = entityManager.createQuery(queryString);
			query.setParameter("codelistInternalValue", codelistInternalValue);
			ref = (RefConfigCodeList) query.getSingleResult();
		} catch (Exception e) {
			StringBuilder msg = new StringBuilder();
			msg
					.append("findByCode failed for CODELIST_INTERNAL_VALUE value'")
					.append(codelistInternalValue)
					.append("' ")
					.append("Query used was ->")
					.append(queryString);
			LOG.error(msg.toString(), e);
		} finally {
			this.cqtEntityManagerFactory.closeEntityManager(entityManager);
		}
		if (ref != null)
			return ref.getValue();
		return codelistInternalValue;
	}
	
	@Override
	public String findCodeByInternalCode(String configType, String internalCode) {
		RefConfigCodeList ref = null;
		String queryString = "from RefConfigCodeList a where a.codelistConfigType = :codelistConfigType and a.codelistInternalValue = :codelistInternalValue";
		
		EntityManager entityManager = this.cqtEntityManagerFactory.getEntityManager();
		try {
			Query query = entityManager.createQuery(queryString);
			query.setParameter("codelistConfigType", configType);
			query.setParameter("codelistInternalValue", internalCode);
			ref = (RefConfigCodeList) query.getSingleResult();
		} catch (Exception e) {
			StringBuilder msg = new StringBuilder();
			msg
					.append("findCodeByInternalCode failed for CODELIST_INTERNAL_VALUE value'")
					.append(internalCode)
					.append("' ")
					.append("Query used was ->")
					.append(queryString);
			LOG.error(msg.toString(), e);
		} finally {
			this.cqtEntityManagerFactory.closeEntityManager(entityManager);
		}
		if (ref != null)
			return ref.getValue();
		return internalCode;
	}
	
	/**
	 * @author Andrius Mielkus(andrius.mielkus@yandex.com)
	 * 
	 * Interprets internal code to value
	 */
	@SuppressWarnings("unchecked")
	@Override
	public String interpretInternalCodeToValue(String configType, String internalCode) {
		RefConfigCodeListCache codeList;
		codeList = codeListCache.get(configType);
		if(codeList == null || !codeList.isValid()) {
			// since the cache is empty or invalid, update it from DB
			List<RefConfigCodeList> listFromDB = null;
			EntityManager entityManager = this.cqtEntityManagerFactory.getEntityManager();

			StringBuilder queryString = new StringBuilder("from RefConfigCodeList a");
			queryString.append(" where a.codelistConfigType = :codelistConfigType order by a.codelistInternalValue ASC");
			try {
				Query query = entityManager.createQuery(queryString.toString());
				query.setParameter("codelistConfigType", configType);

				listFromDB = query.getResultList();
			} catch (Exception ex) {
				StringBuilder msg = new StringBuilder();
				msg
						.append("interpretInternalCodeToValue: failed to find the code list for '")
						.append(configType)
						.append("' type. Query used was->")
						.append(queryString);
				LOG.error(msg.toString(), ex);
			} finally {
				this.cqtEntityManagerFactory.closeEntityManager(entityManager);
				if (null == listFromDB) {
					listFromDB = new ArrayList<>();
				}
			}
			if(codeList == null) {
				codeList = new RefConfigCodeListCache(listFromDB, cacheValidTimeInMillis);
				codeListCache.put(configType, codeList);
			} else
				codeList.setValueList(listFromDB);
		}
		
		RefConfigCodeList foundEntity = codeList.findByInternalCode(internalCode);
		
		if(foundEntity != null)
			return foundEntity.getValue();
		return internalCode;
	}
	
	/**
	 * 
	 * @author Andrius Mielkus(andrius.mielkus@yandex.com)
	 * Caching mechanism implementation of RefConfigCodeList
	 *
	 */
	private class RefConfigCodeListCache {
		private List<RefConfigCodeList> valueList;
		private final int validTimeInMillis;
		private long updateTimestamp;
		
		RefConfigCodeListCache(List<RefConfigCodeList> valueList, int validTimeInMillis) {
			this.valueList = valueList;
			this.validTimeInMillis = validTimeInMillis;
			this.updateTimestamp = Calendar.getInstance().getTimeInMillis();
		}
		
		public List<RefConfigCodeList> getValueList() {
			return valueList;
		}
		
		public void setValueList(List<RefConfigCodeList> valueList) {
			this.valueList = valueList;
			this.updateTimestamp = Calendar.getInstance().getTimeInMillis();
		}
		
		public boolean isValid() {
			long now = Calendar.getInstance().getTimeInMillis();
			return (now - updateTimestamp < validTimeInMillis);
		}
		
		public RefConfigCodeList findByInternalCode(String internalCode) {
			RefConfigCodeList searchKey = new RefConfigCodeList();
			searchKey.setCodelistInternalValue(internalCode);
			int loc = Collections.binarySearch(valueList, searchKey, new Comparator<RefConfigCodeList>() {

				@Override
				public int compare(RefConfigCodeList o1, RefConfigCodeList o2) {
					return o1.getCodelistInternalValue().compareTo(o2.getCodelistInternalValue());
				}
				
			});
			if(loc >= 0) {
				return valueList.get(loc);
			}
			return null;
		}
	}

}
