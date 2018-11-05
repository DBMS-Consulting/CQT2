package com.dbms.service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import javax.faces.bean.ApplicationScoped;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.persistence.EntityManager;
import javax.persistence.Query;

import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.ClientAnchor;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.Drawing;
import org.apache.poi.ss.usermodel.Picture;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.primefaces.model.DefaultStreamedContent;
import org.primefaces.model.StreamedContent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dbms.csmq.CSMQBean;
import com.dbms.entity.cqt.CmqProductBaseCurrent;
import com.dbms.entity.cqt.RefConfigCodeList;
import com.dbms.service.base.CqtPersistenceService;
import com.dbms.util.CqtConstants;
import com.dbms.util.OrderBy;
import com.dbms.util.exceptions.CqtServiceException;

import java.util.HashSet;

@ManagedBean(name = "RefCodeListService")
@ApplicationScoped
public class RefCodeListService extends
		CqtPersistenceService<RefConfigCodeList> implements IRefCodeListService {
	private final String CACHE_NAME = "code-list-cache";

	private static final Logger LOG = LoggerFactory
			.getLogger(RefCodeListService.class);

	@ManagedProperty("#{CqtCacheManager}")
	private ICqtCacheManager cqtCacheManager;
	
	@Override
	@SuppressWarnings({ "unchecked" })
	public List<RefConfigCodeList> findByConfigType(String codelistConfigType, boolean activeOnly, OrderBy orderBy) {
		String cacheKey = codelistConfigType + "-" + orderBy.name() + "-" + Boolean.toString(activeOnly);
		
		List<RefConfigCodeList> retVal = null;
		try {
			retVal = new ArrayList<RefConfigCodeList>((List<RefConfigCodeList>) this.cqtCacheManager
				.getFromCache(CACHE_NAME, cacheKey));
		} catch(Exception e) {
			retVal = null;
		}

		if (null == retVal) {
			EntityManager entityManager = this.cqtEntityManagerFactory
					.getEntityManager();

			StringBuilder queryString = new StringBuilder(
					"from RefConfigCodeList a");
			queryString
					.append(" where a.codelistConfigType = :codelistConfigType");
			if(activeOnly == true)
				queryString .append(" and a.activeFlag = 'Y'");
			queryString.append(" order by a.serialNum ");
			queryString.append(orderBy.name());
			try {
				Query query = entityManager.createQuery(queryString.toString());
				query.setParameter("codelistConfigType", codelistConfigType);
				query.setHint("org.hibernate.cacheable", true);
				retVal = query.getResultList();
				if (null == retVal) {
					retVal = new ArrayList<>();
				} else {
					// add them to cache
					cqtCacheManager.addToCache(CACHE_NAME, cacheKey, retVal);
				}
			} catch (Exception ex) {
				StringBuilder msg = new StringBuilder();
				msg.append("findByConfigType failed for type '")
						.append("RefConfigCodeList").append("' and value of ")
						.append(codelistConfigType)
						.append(". Query used was->").append(queryString);
				LOG.error(msg.toString(), ex);
			} finally {
				this.cqtEntityManagerFactory.closeEntityManager(entityManager);
			}
		}
		return retVal;
	}
	
	@Override
	@SuppressWarnings({ "unchecked" })
	public List<RefConfigCodeList> findByConfigTypeOrderByName(String codelistConfigType, boolean activeOnly, OrderBy orderBy) {
		String cacheKey = codelistConfigType + "-" + orderBy.name() + "-" + Boolean.toString(activeOnly);
		
		List<RefConfigCodeList> retVal = null;
		try {
			retVal = (List<RefConfigCodeList>) this.cqtCacheManager
				.getFromCache(CACHE_NAME, cacheKey);
		} catch(Exception e) {
			retVal = null;
		}

		if (null == retVal) {
			EntityManager entityManager = this.cqtEntityManagerFactory
					.getEntityManager();

			StringBuilder queryString = new StringBuilder(
					"from RefConfigCodeList a");
			queryString
					.append(" where a.codelistConfigType = :codelistConfigType");
			if(activeOnly == true)
				queryString .append(" and a.activeFlag = 'Y'");
			queryString.append(" order by a.value ");
			queryString.append(orderBy.name());
			try {
				Query query = entityManager.createQuery(queryString.toString());
				query.setParameter("codelistConfigType", codelistConfigType);
				query.setHint("org.hibernate.cacheable", true);
				retVal = query.getResultList();
				if (null == retVal) {
					retVal = new ArrayList<>();
				} else {
					// add them to cache
					cqtCacheManager.addToCache(CACHE_NAME, cacheKey, retVal);
				}
			} catch (Exception ex) {
				StringBuilder msg = new StringBuilder();
				msg.append("findByConfigType failed for type '")
						.append("RefConfigCodeList").append("' and value of ")
						.append(codelistConfigType)
						.append(". Query used was->").append(queryString);
				LOG.error(msg.toString(), ex);
			} finally {
				this.cqtEntityManagerFactory.closeEntityManager(entityManager);
			}
		}
		return retVal;
	}

	@Override
	@SuppressWarnings({ "unchecked" })
	public RefConfigCodeList findEnterpriseAdType() {
		RefConfigCodeList retVal = null;

		EntityManager entityManager = this.cqtEntityManagerFactory
				.getEntityManager();

		StringBuilder queryString = new StringBuilder(
				"from RefConfigCodeList a");
		queryString
				.append(" where a.codelistConfigType = 'SYSTEM_CONFIG' and a.activeFlag = 'Y' and a.codelistInternalValue = 'ENTERPRISE_AD' ");
		try {
			Query query = entityManager.createQuery(queryString.toString());
			query.setHint("org.hibernate.cacheable", true);
			List<RefConfigCodeList> data = query.getResultList();
			if((null != data) && (data.size() > 0)) {
				retVal = data.get(0);
			}
		} catch (Exception ex) {
			StringBuilder msg = new StringBuilder();
			msg.append("findEnterpriseAdType failed '")
					.append(". Query used was->").append(queryString);
			LOG.error(msg.toString(), ex);
		} finally {
			this.cqtEntityManagerFactory.closeEntityManager(entityManager);
		}
		return retVal;
	}
	
	@Override
	@SuppressWarnings({ "unchecked" })
	public List<RefConfigCodeList> findLdapConfig() {
		List<RefConfigCodeList> retVal = null;

		EntityManager entityManager = this.cqtEntityManagerFactory
				.getEntityManager();

		StringBuilder queryString = new StringBuilder(
				"from RefConfigCodeList a");
		queryString
				.append(" where a.codelistConfigType = 'SYSTEM_CONFIG' and a.activeFlag = 'Y' and a.codelistInternalValue like 'LDAP%' ");
		try {
			Query query = entityManager.createQuery(queryString.toString());
			query.setHint("org.hibernate.cacheable", true);
			retVal = query.getResultList();
		} catch (Exception ex) {
			StringBuilder msg = new StringBuilder();
			msg.append("findEnterpriseAdType failed '")
					.append(". Query used was->").append(queryString);
			LOG.error(msg.toString(), ex);
		} finally {
			this.cqtEntityManagerFactory.closeEntityManager(entityManager);
		}
		return retVal;
	}
	
	@Override
	@SuppressWarnings({ "unchecked" })
	public List<RefConfigCodeList> findUserGroups() {
		List<RefConfigCodeList> retVal = null;

		EntityManager entityManager = this.cqtEntityManagerFactory
				.getEntityManager();

		StringBuilder queryString = new StringBuilder(
				"from RefConfigCodeList a");
		queryString
				.append(" where a.codelistConfigType = 'USER_GROUPS' and a.activeFlag = 'Y' ");
		try {
			Query query = entityManager.createQuery(queryString.toString());
			query.setHint("org.hibernate.cacheable", true);
			retVal = query.getResultList();
		} catch (Exception ex) {
			StringBuilder msg = new StringBuilder();
			msg.append("findUserGroups failed '")
					.append(". Query used was->").append(queryString);
			LOG.error(msg.toString(), ex);
		} finally {
			this.cqtEntityManagerFactory.closeEntityManager(entityManager);
		}
		return retVal;
	}
	
	/**
	 * Find all active configs, order by serialNum
	 */
	@Override
	public List<RefConfigCodeList> findByConfigType(String codelistConfigType, OrderBy orderBy) {
		return findByConfigType(codelistConfigType, true, orderBy);
	}

	/**
	 * Find all configs(active/inactive)
	 */
	@Override
	public List<RefConfigCodeList> findAllByConfigType(
			String codelistConfigType, OrderBy orderBy) {
		return findByConfigType(codelistConfigType, false, orderBy);
	}
    
	@Override
	public RefConfigCodeList findDefaultByConfigType(String configType) {
		List<RefConfigCodeList> codeList = findByConfigType(configType, OrderBy.ASC);
		
		if (codeList != null) {
			for(RefConfigCodeList c : codeList) {
				if(CSMQBean.TRUE.equals(c.getDefaultFlag())) {
					return c;
				}
			}	
		}
		return null;
	}
    
    @Override
    public List<RefConfigCodeList> findDefaultsByConfigType(String configType) {
        List<RefConfigCodeList> codeList = findByConfigType(configType, OrderBy.ASC);
        List<RefConfigCodeList> retVal = new ArrayList<RefConfigCodeList>();
		
		if (codeList != null) {
			for(RefConfigCodeList c : codeList) {
				if(CSMQBean.TRUE.equals(c.getDefaultFlag())) {
					retVal.add(c);
				}
			}
		}
		return retVal;
    }

	@Override
	public RefConfigCodeList getCurrentMeddraVersion() {
		RefConfigCodeList retVal = null;
		retVal = findByConfigTypeAndInternalCode(CqtConstants.CODE_LIST_TYPE_MEDDRA_VERSIONS, CqtConstants.CURRENT_MEDDRA_VERSION);
		return retVal;
	}
	
	@Override
	public RefConfigCodeList getTargetMeddraVersion() {
		RefConfigCodeList retVal = null;
		retVal = findByConfigTypeAndInternalCode(CqtConstants.CODE_LIST_TYPE_MEDDRA_VERSIONS, CqtConstants.TARGET_MEDDRA_VERSION);
		return retVal;
	}
	
	@Override
	public List<RefConfigCodeList> getMeddraVersions(){
		List<RefConfigCodeList> list = new ArrayList<>();
		List<RefConfigCodeList> retVal = new ArrayList<>();
		list = findByConfigType(CqtConstants.CODE_LIST_TYPE_MEDDRA_VERSIONS);
		for(RefConfigCodeList item: list) {
			if(!item.getCodelistInternalValue().equalsIgnoreCase("DICTIONARY_NAME") 
					&& !item.getCodelistInternalValue().equalsIgnoreCase("LOAD_CMQ_CURRENT_OR_TARGET")
					&& item.getActiveFlag().equalsIgnoreCase("Y")) {
				retVal.add(item);
			}
		}
		return retVal;
	}
	
	public List<RefConfigCodeList> getAllCodelistTypes(){
		List<RefConfigCodeList> list = new ArrayList<>();
		list = findAllCodelistTypes();
		/*
		List<RefConfigCodeList> retVal = new ArrayList<>();
		for(RefConfigCodeList item: list) {
			retVal.add(item.getCodelistConfigType());
		}*/
		return list;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public List<RefConfigCodeList> findAllCodelistTypes(){
		String defaultFlag = "Y";
		List<RefConfigCodeList> ref = new ArrayList<>();
		String cacheKey = "[" + defaultFlag + "]";
		
		// try to get it from cache first
        Object cv = (List<RefConfigCodeList>)this.cqtCacheManager.getFromCache(CACHE_NAME, cacheKey);
        if(cv != null && cv instanceof List<?>) {
            return (List<RefConfigCodeList>) cv;
        }
		
		String queryString = "from RefConfigCodeList a where a.defaultFlag = :defaultFlag order by a.codelistConfigType asc";
		
		EntityManager entityManager = this.cqtEntityManagerFactory.getEntityManager();
		try {
			Query query = entityManager.createQuery(queryString);
			query.setParameter("defaultFlag", defaultFlag);
			query.setHint("org.hibernate.cacheable", true);
			ref = (List<RefConfigCodeList>) query.getResultList();
		} catch (javax.persistence.NoResultException e) {
            LOG.info("findAllCodelistTypes found no result for defaultFlag: " + defaultFlag);
        } catch (Exception e) {
			StringBuilder msg = new StringBuilder();
			msg.append(
					"findAllCodelistTypes failed for defaultFlag = '")
					.append(defaultFlag).append("' ")
					.append("Query used was ->").append(queryString);
			LOG.error(msg.toString(), e);
		} finally {
			this.cqtEntityManagerFactory.closeEntityManager(entityManager);
		}
		if (ref != null) {
            this.cqtCacheManager.addToCache(CACHE_NAME, cacheKey, ref);
			return ref;
        }
		return null;
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<RefConfigCodeList> findByConfigType(String configType){
		List<RefConfigCodeList> ref = new ArrayList<>();
		String cacheKey = "[" + configType + "]";
		
		// try to get it from cache first
        Object cv = (List<RefConfigCodeList>)this.cqtCacheManager.getFromCache(CACHE_NAME, cacheKey);
        if(cv != null && cv instanceof RefConfigCodeList) {
            return (List<RefConfigCodeList>) cv;
        }
		
		String queryString = "from RefConfigCodeList a where a.codelistConfigType = :codelistConfigType";
		
		EntityManager entityManager = this.cqtEntityManagerFactory.getEntityManager();
		try {
			Query query = entityManager.createQuery(queryString);
			query.setParameter("codelistConfigType", configType);
			query.setHint("org.hibernate.cacheable", true);
			ref = (List<RefConfigCodeList>) query.getResultList();
		} catch (javax.persistence.NoResultException e) {
            LOG.info("findByConfigTypeAndInternalCode found no result for ConfigType: " + configType);
        } catch (Exception e) {
			StringBuilder msg = new StringBuilder();
			msg.append(
					"findCodeByInternalCode failed for codelistConfigType = '")
					.append(configType).append("' ")
					.append("Query used was ->").append(queryString);
			LOG.error(msg.toString(), e);
		} finally {
			this.cqtEntityManagerFactory.closeEntityManager(entityManager);
		}
		if (ref != null) {
            this.cqtCacheManager.addToCache(CACHE_NAME, cacheKey, ref);
			return ref;
        }
		return null;
	}
	
	@Override
	public RefConfigCodeList findByConfigTypeAndInternalCode(String configType, String internalCode) {
		RefConfigCodeList ref = null;
        String cacheKey = "[" + configType + "-" + internalCode + "]";
        
        // try to get it from cache first
        Object cv = (RefConfigCodeList)this.cqtCacheManager.getFromCache(CACHE_NAME, cacheKey);
        if(cv != null && cv instanceof RefConfigCodeList) {
            return (RefConfigCodeList) cv;
        }
        
		String queryString = "from RefConfigCodeList a where a.codelistConfigType = :codelistConfigType and lower(a.codelistInternalValue) = lower(:codelistInternalValue)";

		EntityManager entityManager = this.cqtEntityManagerFactory
				.getEntityManager();
		try {
			Query query = entityManager.createQuery(queryString);
			query.setParameter("codelistConfigType", configType);
			query.setParameter("codelistInternalValue", internalCode);
			query.setHint("org.hibernate.cacheable", true);
			ref = (RefConfigCodeList) query.getSingleResult();
		} catch (javax.persistence.NoResultException e) {
            LOG.info("findByConfigTypeAndInternalCode found no result for ConfigType: " + configType + ", InternalCode: " + internalCode);
        } catch (Exception e) {
			StringBuilder msg = new StringBuilder();
			msg.append(
					"findCodeByInternalCode failed for CODELIST_INTERNAL_VALUE value'")
					.append(internalCode).append("' and codelistConfigType = '")
					.append(configType).append("' ")
					.append("Query used was ->").append(queryString);
			LOG.error(msg.toString(), e);
		} finally {
			this.cqtEntityManagerFactory.closeEntityManager(entityManager);
		}
		if (ref != null) {
            this.cqtCacheManager.addToCache(CACHE_NAME, cacheKey, ref);
			return ref;
        }
		return null;
	}
	
	@Override
	public RefConfigCodeList findByCriterias(String configType, String internalCode, String activeFlag) {
		RefConfigCodeList ref = null;
        String cacheKey = "[" + configType + "-" + internalCode + "]";
        
        // try to get it from cache first
        Object cv = (RefConfigCodeList)this.cqtCacheManager.getFromCache(CACHE_NAME, cacheKey);
        if(cv != null && cv instanceof RefConfigCodeList) {
            return (RefConfigCodeList) cv;
        }
        
		String queryString = "from RefConfigCodeList a where a.codelistConfigType = :codelistConfigType and lower(a.codelistInternalValue) = lower(:codelistInternalValue)"
				+ " and a.activeFlag=:activeFlag";

		EntityManager entityManager = this.cqtEntityManagerFactory
				.getEntityManager();
		try {
			Query query = entityManager.createQuery(queryString);
			query.setParameter("codelistConfigType", configType);
			query.setParameter("codelistInternalValue", internalCode);
			query.setParameter("activeFlag", activeFlag);
			query.setHint("org.hibernate.cacheable", true);
			ref = (RefConfigCodeList) query.getSingleResult();
		} catch (javax.persistence.NoResultException e) {
            LOG.info("findByConfigTypeAndInternalCode found no result for ConfigType: " + configType + ", InternalCode: " + internalCode);
        } catch (Exception e) {
			StringBuilder msg = new StringBuilder();
			msg.append(
					"findCodeByInternalCode failed for CODELIST_INTERNAL_VALUE value'")
					.append(internalCode).append("' and codelistConfigType = '")
					.append(configType).append("' ")
					.append("Query used was ->").append(queryString);
			LOG.error(msg.toString(), e);
		} finally {
			this.cqtEntityManagerFactory.closeEntityManager(entityManager);
		}
		if (ref != null) {
            this.cqtCacheManager.addToCache(CACHE_NAME, cacheKey, ref);
			return ref;
        }
		return null;
	}
	

	@Override
	public RefConfigCodeList findByDefaultFlag(String configType, String defaultFlag) {
		RefConfigCodeList ref = null;
        String cacheKey = "[" + configType  + "]";
        
        /* try to get it from cache first
        Object cv = (RefConfigCodeList)this.cqtCacheManager.getFromCache(CACHE_NAME, cacheKey);
        if(cv != null && cv instanceof RefConfigCodeList) {
            return (RefConfigCodeList) cv;
        }
        */
        
		String queryString = "from RefConfigCodeList a where a.codelistConfigType = :codelistConfigType "
				+ " and a.defaultFlag=:defaultFlag";

		EntityManager entityManager = this.cqtEntityManagerFactory
				.getEntityManager();
		try {
			Query query = entityManager.createQuery(queryString);
			query.setParameter("codelistConfigType", configType);
 			query.setParameter("defaultFlag", defaultFlag);
			query.setHint("org.hibernate.cacheable", true);
			if (query.getSingleResult() == null)
				return null;
			ref = (RefConfigCodeList) query.getSingleResult();
		} catch (javax.persistence.NoResultException e) {
            LOG.info("findByConfigTypeAndInternalCode found no result for ConfigType: " + configType);
        } catch (Exception e) {
			StringBuilder msg = new StringBuilder();
			msg.append(
					"findCodeByInternalCode failed for CODELIST_INTERNAL_VALUE value'")
					.append("' and codelistConfigType = '")
					.append(configType).append("' ")
					.append("Query used was ->").append(queryString);
			LOG.error(msg.toString(), e);
		} finally {
			this.cqtEntityManagerFactory.closeEntityManager(entityManager);
		}
		if (ref != null) {
            this.cqtCacheManager.addToCache(CACHE_NAME, cacheKey, ref);
			return ref;
        }
		return null;
	}


	@Override
	public String findCodeByInternalCode(String codelistInternalValue) {
		RefConfigCodeList ref = null;
		String queryString = "from RefConfigCodeList c where lower(c.codelistInternalValue) = lower(:codelistInternalValue)";
		EntityManager entityManager = this.cqtEntityManagerFactory
				.getEntityManager();
		try {
			Query query = entityManager.createQuery(queryString);
			query.setParameter("codelistInternalValue", codelistInternalValue);
			query.setHint("org.hibernate.cacheable", true);
			ref = (RefConfigCodeList) query.getSingleResult();
		} catch (javax.persistence.NoResultException e) {
            LOG.info("findByConfigTypeAndInternalCode found no result for InternalCode: " + codelistInternalValue);
        } catch (Exception e) {
			StringBuilder msg = new StringBuilder();
			msg.append("findByCode failed for CODELIST_INTERNAL_VALUE value'")
					.append(codelistInternalValue).append("' ")
					.append("Query used was ->").append(queryString);
			LOG.error(msg.toString(), e);
		} finally {
			this.cqtEntityManagerFactory.closeEntityManager(entityManager);
		}
		if (ref != null)
			return ref.getValue();
		return codelistInternalValue;
	}

	@Override
	public boolean existingCodelistByInternalValue(String codelistInternalValue) {
        Long retVal = null;
        StringBuilder sb = new StringBuilder();
        sb.append("select count(*) from RefConfigCodeList c where lower(c.codelistInternalValue) = lower(:codelistInternalValue)");

        EntityManager entityManager = this.cqtEntityManagerFactory.getEntityManager();
        try {
            Query query = entityManager.createQuery(sb.toString());
            query.setParameter("codelistInternalValue", codelistInternalValue);
            query.setHint("org.hibernate.cacheable", true);
            retVal = (Long)query.getSingleResult();
			if (retVal > 0)
                return true;
        } catch (Exception e) {
            StringBuilder msg = new StringBuilder();
            msg
                    .append("An error occurred while findSmqRelationsCountForSmqCode ")
                    .append(codelistInternalValue)
                    .append(" Query used was ->")
                    .append(sb.toString());
            LOG.error(msg.toString(), e);
        } finally {
            this.cqtEntityManagerFactory.closeEntityManager(entityManager);
        }
        return false;
	}

    @Override
    public boolean existingCodelistByValue(String value) {
        Long retVal = null;
        StringBuilder sb = new StringBuilder();
        sb.append("select count(*) from RefConfigCodeList c where lower(c.value) = lower(:value)");

        EntityManager entityManager = this.cqtEntityManagerFactory.getEntityManager();
        try {
            Query query = entityManager.createQuery(sb.toString());
            query.setParameter("value", value);
            query.setHint("org.hibernate.cacheable", true);
            retVal = (Long)query.getSingleResult();
			if (retVal > 0)
                return true;
        } catch (Exception e) {
            StringBuilder msg = new StringBuilder();
            msg
                    .append("An error occurred while existingCodelistByValue ")
                    .append(value)
                    .append(" Query used was ->")
                    .append(sb.toString());
            LOG.error(msg.toString(), e);
        } finally {
            this.cqtEntityManagerFactory.closeEntityManager(entityManager);
        }
        return false;
    }

    @Override
    public boolean existingCodelistByConfigType(String codelistConfigType) {
        Long retVal = null;
        StringBuilder sb = new StringBuilder();
        sb.append("select count(*) from RefConfigCodeList c where lower(c.codelistConfigType) = lower(:codelistConfigType)");

        EntityManager entityManager = this.cqtEntityManagerFactory.getEntityManager();
        try {
            Query query = entityManager.createQuery(sb.toString());
            query.setParameter("codelistConfigType", codelistConfigType);
            query.setHint("org.hibernate.cacheable", true);
            retVal = (Long)query.getSingleResult();
			if (retVal > 0)
                return true;
        } catch (Exception e) {
            StringBuilder msg = new StringBuilder();
            msg
                    .append("An error occurred while existingCodelistByConfigType ")
                    .append(codelistConfigType)
                    .append(" Query used was ->")
                    .append(sb.toString());
            LOG.error(msg.toString(), e);
        } finally {
            this.cqtEntityManagerFactory.closeEntityManager(entityManager);
        }
        return false;
    }


	@Override
	public String findCodeByInternalCode(String configType, String internalCode) {
		RefConfigCodeList ref = null;
		
		ref = findByConfigTypeAndInternalCode(configType, internalCode);
		
		if(ref == null)
			return "";
		else
			return ref.getValue();
	}

	/**
	 * @author Andrius Mielkus(andrius.mielkus@yandex.com)
	 * 
	 *         Interprets internal code to value
	 */
    @Override
    public String interpretInternalCodeToValueOrDefault(String configType,
			String internalCode, String defaultValue) {
        List<RefConfigCodeList> codeList = findByConfigType(configType, OrderBy.ASC);
		
		if (codeList != null) {
			for(RefConfigCodeList c : codeList) {
				if(c.getCodelistInternalValue().equalsIgnoreCase(internalCode)) {
					return c.getValue();
				}
			}	
		}
		return defaultValue;
    }
    
	@Override
	public String interpretInternalCodeToValue(String configType,
			String internalCode) {
        return interpretInternalCodeToValueOrDefault(configType, internalCode, internalCode);
	}
    
	/**
	 * @author Andrius Mielkus(andrius.mielkus@yandex.com)
	 * 
	 *         Interprets MedDRA Dictionary Impact Type code to value
     * 
     * @param internalCode 
     * @param impactType
	 */
	@Override
	public String interpretMeddraImpactTypeDesc(String internalCode, String impactType) {
		List<RefConfigCodeList> codeList = findByConfigType(CqtConstants.CODE_LIST_TYPE_MEDDRA_DICT_IMPACT_TYPE, OrderBy.ASC);
		
		if (codeList != null) {
			for(RefConfigCodeList c : codeList) {
				if(c.getCodelistInternalValue().equalsIgnoreCase(internalCode) && c.getValue().startsWith(impactType + ":")) {
                    return c.getValue();
				}
			}	
		}
		return impactType;
	}
    
	@Override
	public String[] interpretProductCodesToValues(List<CmqProductBaseCurrent> products)
	{
		if(products != null && products.size() > 0) {
            HashSet<String> upcds = new HashSet<String>();
			String[] pv;
			for(int i=0; i<products.size(); i++) {
                upcds.add(interpretInternalCodeToValue(CqtConstants.CODE_LIST_TYPE_PRODUCT, products.get(i).getCmqProductCd()));
			}
            pv = new String[upcds.size()];
            upcds.toArray(pv);
            if(upcds.size() != products.size()) {
                LOG.debug("There were some duplicates in products list given.");
            }
			return pv;
		} else {
			return new String[0];
		}
	}
	
	@Override
	public String convertProductCodesToValuesLabel(List<CmqProductBaseCurrent> products) {
		return StringUtils.join(this.interpretProductCodesToValues(products), ", ");
	}
	
    @Override
	public String interpretProductCodesToValuesLabel(String[] productCds) {
		String [] pv;
		if(productCds != null && productCds.length > 0) {
			pv = new String[productCds.length];
			for(int i=0; i<pv.length; i++) {
				pv[i] = interpretInternalCodeToValue(CqtConstants.CODE_LIST_TYPE_PRODUCT, productCds[i]);
			}
		} else {
			pv = new String[0];
		}
		return StringUtils.join(pv, ", ");
	}
	
	@Override
	public void create(RefConfigCodeList e, String userCn, String userFirstName, String userLastName, String userGroups) throws CqtServiceException {
		super.create(e, userCn, userFirstName, userLastName, userGroups);
		cqtCacheManager.removeAllFromCache(CACHE_NAME);
	}
	
	@Override
	public RefConfigCodeList update(RefConfigCodeList e, String userCn, String userFirstName, String userLastName, String userGroups) throws CqtServiceException {
		RefConfigCodeList u = super.update(e, userCn, userFirstName, userLastName, userGroups);
		cqtCacheManager.removeAllFromCache(CACHE_NAME);
		return u;
	}
	
	@Override
	public void update(List<RefConfigCodeList> e, String userCn, String userFirstName, String userLastName, String userGroups) throws CqtServiceException {
		super.update(e, userCn, userFirstName, userLastName, userGroups);
		cqtCacheManager.removeAllFromCache(CACHE_NAME);
	}

	public ICqtCacheManager getCqtCacheManager() {
		return cqtCacheManager;
	}

	public void setCqtCacheManager(ICqtCacheManager cqtCacheManager) {
		this.cqtCacheManager = cqtCacheManager;
	}
    
    @Override
    public String getMedDRAVersioningReportsPath() {
        RefConfigCodeList reportsPathConfig = findByConfigTypeAndInternalCode(CqtConstants.CODE_LIST_TYPE_SYSTEM_CONFIG, CqtConstants.SYSTEM_CONFIG_MEDDRA_IMPACT_DL_PATH);
        if(reportsPathConfig != null) {
            return reportsPathConfig.getValue();
        }
        return null;
    }

	@Override
	public StreamedContent generateReport(String codelistType, String timezone) {
		SimpleDateFormat dateTimeFormat = new SimpleDateFormat("dd-MMM-yyyy:hh:mm:ss a z");
		if (timezone != null)
			dateTimeFormat.setTimeZone(TimeZone.getTimeZone(timezone));
		XSSFWorkbook workbook = new XSSFWorkbook();
		XSSFSheet worksheet = null;


		worksheet = workbook.createSheet("Report " + codelistType);
		XSSFRow row = null;
		int rowCount = 4;
		
		try {
			insertExporLogoImage(worksheet, workbook);
		} catch (IOException e1) {
			e1.printStackTrace();
		}

		/**
		 * Première ligne - entêtes
		 */
		row = worksheet.createRow(rowCount);
		XSSFCell cell = row.createCell(0);
		cell.setCellValue("Administration Report : [" + codelistType + "]");
		rowCount++;
		row = worksheet.createRow(rowCount);
		cell = row.createCell(0);
		cell.setCellValue("Report Date:");
		cell = row.createCell(1);
		cell.setCellValue(dateTimeFormat.format(new Date()));
		rowCount += 2;

		row = worksheet.createRow(rowCount);
		cell = row.createCell(0);
		cell.setCellValue("Serial #");
		cell = row.createCell(1);
		cell.setCellValue("Internal Value");
		cell = row.createCell(2);
		cell.setCellValue("Value");
		cell = row.createCell(3);
		cell.setCellValue("Active flag");
		cell = row.createCell(4);
		cell.setCellValue("Default flag");
		rowCount += 2;

		// Retrieval of ConfigList - Loop
		List<RefConfigCodeList> list = findAllByConfigType(codelistType,
				OrderBy.ASC);
		for (RefConfigCodeList ref : list) {
			row = worksheet.createRow(rowCount);
			// Cell 0
			cell = row.createCell(0);
			cell.setCellValue(ref.getSerialNum() + "");
						
			// Cell 1
			cell = row.createCell(1);
			cell.setCellValue(ref.getCodelistInternalValue());

			// Cell 2
			cell = row.createCell(2);
			cell.setCellValue(ref.getValue());

			// Cell 3
			cell = row.createCell(3);
			cell.setCellValue(ref.getActiveFlag());
			
			// Cell 4
			cell = row.createCell(4);
			cell.setCellValue(ref.getDefaultFlag());

			rowCount++;
		}
		worksheet.autoSizeColumn(0);
		worksheet.autoSizeColumn(1);
		worksheet.autoSizeColumn(2);

		StreamedContent content = null;
		try {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			workbook.write(baos);
			byte[] xls = baos.toByteArray();
			ByteArrayInputStream bais = new ByteArrayInputStream(xls);
			content = new DefaultStreamedContent(
					bais,
					"application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
					codelistType + "_report" + ".xlsx");
		} catch (IOException e) {
			e.printStackTrace();
		}

		return content;
	}

	private void insertExporLogoImage(XSSFSheet sheet, XSSFWorkbook wb) throws IOException {
		FacesContext fc = FacesContext.getCurrentInstance();
		ExternalContext ec = fc.getExternalContext();
		final FileInputStream stream = new FileInputStream(ec.getRealPath("/image/logo.jpg"));
		final CreationHelper helper = wb.getCreationHelper();
		final Drawing drawing = sheet.createDrawingPatriarch();

		final ClientAnchor anchor = helper.createClientAnchor();
		anchor.setAnchorType(ClientAnchor.MOVE_AND_RESIZE);

		final int pictureIndex = wb.addPicture(stream,
				Workbook.PICTURE_TYPE_PNG);

		anchor.setCol1(0);
		anchor.setRow1(0); // same row is okay
		anchor.setRow2(1);
		anchor.setCol2(1);
		final Picture pict = drawing.createPicture(anchor, pictureIndex);
		pict.resize();
	}
	
	@Override
	public boolean getLevelScopeCategorySystemConfig() {
		RefConfigCodeList retVal = null;
		retVal = findByConfigTypeAndInternalCode(CqtConstants.CODE_LIST_TYPE_SYSTEM_CONFIG, CqtConstants.CODE_LIST_TYPE_DISPLAY_TERM_WEIGHT_SCOPE);
		if (retVal != null)
			if (retVal.getValue().equals("Y"))
				return true;
		
		return false;
	}
    
    @Override
    public RefConfigCodeList getDefaultForConfigType(String configType) {
        List<RefConfigCodeList> values = findByConfigType(configType, OrderBy.ASC);
        RefConfigCodeList defaultValue = null;
        for(RefConfigCodeList rcc : values) {
            if("Y".equalsIgnoreCase(rcc.getDefaultFlag())) {
                defaultValue = rcc;
                break;
            }
        }
        return defaultValue;
    }
    
    @Override
	public RefConfigCodeList findBySerialNumber(double serialNum) {
		RefConfigCodeList ref = null;
        String cacheKey = "[" + serialNum + "]";
        
        // try to get it from cache first
        Object cv = (RefConfigCodeList)this.cqtCacheManager.getFromCache(CACHE_NAME, cacheKey);
        if(cv != null && cv instanceof RefConfigCodeList) {
            return (RefConfigCodeList) cv;
        }
        
		String queryString = "from RefConfigCodeList a where a.serialNum = :serialNum";

		EntityManager entityManager = this.cqtEntityManagerFactory
				.getEntityManager();
		try {
			Query query = entityManager.createQuery(queryString);
			query.setParameter("serialNum", serialNum);
 			query.setHint("org.hibernate.cacheable", true);
 			if (query.getSingleResult() == null)
 				return null;
 			ref = (RefConfigCodeList) query.getSingleResult();
		} catch (javax.persistence.NoResultException e) {
            LOG.info("findByConfigTypeAndInternalCode found no result for serialNum: " + serialNum);
        } catch (Exception e) {
			StringBuilder msg = new StringBuilder();
			msg.append(" Search failed for serialNum")
					.append(serialNum).append("' ")
					.append("Query used was ->").append(queryString);
			LOG.error(msg.toString(), e);
		} finally {
			this.cqtEntityManagerFactory.closeEntityManager(entityManager);
		}
		if (ref != null) {
            this.cqtCacheManager.addToCache(CACHE_NAME, cacheKey, ref);
			return ref;
        }
		return null;
	}
}
