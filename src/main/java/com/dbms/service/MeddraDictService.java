package com.dbms.service;

import java.util.List;

import javax.faces.bean.ApplicationScoped;
import javax.faces.bean.ManagedBean;
import javax.persistence.EntityManager;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.SQLQuery;
import org.hibernate.Session;
import org.hibernate.transform.Transformers;
import org.hibernate.type.StandardBasicTypes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dbms.entity.cqt.MeddraDict190;
import com.dbms.entity.cqt.dtos.MeddraDictHierarchySearchDto;
import com.dbms.entity.cqt.dtos.MeddraDictReverseHierarchySearchDto;
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
	public List<MeddraDictReverseHierarchySearchDto> findFullReverseHierarchyByLevelAndTerm(String searchType, String searchTerm) {
		List<MeddraDictReverseHierarchySearchDto> retVal = null;
		String termColumnName = null;
		if("PT".equalsIgnoreCase(searchType)) {
			termColumnName = "PT_TERM";
		} else {
			termColumnName = "LLT_TERM";
		}
		String queryString = "";
		if (StringUtils.isBlank(searchTerm)) {
			queryString = "select MEDDRA_DICT_ID as meddraDictId, LLT_TERM as lltTerm, LLT_CODE as lltCode"
								+ ", PT_TERM as ptTerm, PT_CODE as ptCode, HLT_TERM as hltTerm, HLT_CODE as hltCode"
								+ ", HLGT_TERM as hlgtTerm, HLGT_CODE as hlgtCode, SOC_TERM as socTerm"
								+ ", SOC_CODE as socCode, PRIMARY_PATH_FLAG as primaryPathFlag "
							+ "from (select MEDDRA_DICT_ID, LLT_TERM, LLT_CODE, PT_TERM, PT_CODE, HLT_TERM, HLT_CODE, "
										+ "HLGT_TERM, HLGT_CODE, SOC_TERM, SOC_CODE, PRIMARY_PATH_FLAG, row_number() "
									+ "over (partition by PT_CODE order by MEDDRA_DICT_ID) rn "
								+ "from MEDDRA_DICT_CURRENT) where rn = 1";
		} else {
			queryString = "select MEDDRA_DICT_ID as meddraDictId, LLT_TERM as lltTerm, LLT_CODE as lltCode"
								+ ", PT_TERM as ptTerm, PT_CODE as ptCode, HLT_TERM as hltTerm, HLT_CODE as hltCode"
								+ ", HLGT_TERM as hlgtTerm, HLGT_CODE as hlgtCode, SOC_TERM as socTerm"
								+ ", SOC_CODE as socCode, PRIMARY_PATH_FLAG as primaryPathFlag "
							+ "from (select MEDDRA_DICT_ID, LLT_TERM, LLT_CODE, PT_TERM, PT_CODE, HLT_TERM, HLT_CODE, "
										+ "HLGT_TERM, HLGT_CODE, SOC_TERM, SOC_CODE, PRIMARY_PATH_FLAG, row_number() "
									+ "over (partition by PT_CODE order by MEDDRA_DICT_ID) rn "
								+ "from MEDDRA_DICT_CURRENT	where upper(" + termColumnName + ") like :searchTerm ) where rn = 1";
		}

		EntityManager entityManager = this.cqtEntityManagerFactory.getEntityManager();
		Session session = entityManager.unwrap(Session.class);
		try {
			SQLQuery query = session.createSQLQuery(queryString);
			query.addScalar("meddraDictId", StandardBasicTypes.LONG);
			query.addScalar("lltTerm", StandardBasicTypes.STRING);
			query.addScalar("lltCode", StandardBasicTypes.STRING);
			query.addScalar("ptTerm", StandardBasicTypes.STRING);
			query.addScalar("ptCode", StandardBasicTypes.STRING);
			query.addScalar("hltTerm", StandardBasicTypes.STRING);
			query.addScalar("hltCode", StandardBasicTypes.STRING);
			query.addScalar("hlgtTerm", StandardBasicTypes.STRING);
			query.addScalar("hlgtCode", StandardBasicTypes.STRING);
			query.addScalar("socTerm", StandardBasicTypes.STRING);
			query.addScalar("socCode", StandardBasicTypes.STRING);
			query.addScalar("primaryPathFlag", StandardBasicTypes.STRING);
			query.setFetchSize(400);
			if (!StringUtils.isBlank(searchTerm)) {
				query.setParameter("searchTerm", searchTerm.toUpperCase());
			}
			query.setResultTransformer(Transformers.aliasToBean(MeddraDictReverseHierarchySearchDto.class));

			retVal = query.list();
		} catch (Exception e) {
			StringBuilder msg = new StringBuilder();
			msg.append("An error occurred while fetching types from MeddraDict190 on searchColumnType ")
					.append(termColumnName).append(" with value like ").append(searchTerm).append(" Query used was ->")
					.append(queryString);
			LOG.error(msg.toString(), e);
		} finally {
			this.cqtEntityManagerFactory.closeEntityManager(entityManager);
		}
		return retVal;
	}

	@Override
	@SuppressWarnings("unchecked")
	public List<MeddraDictHierarchySearchDto> findByLevelAndTerm(String searchColumnTypePrefix, String searchTerm) {
		List<MeddraDictHierarchySearchDto> retVal = null;
		String termColumnName = searchColumnTypePrefix + "TERM";
		String codeColumnName = searchColumnTypePrefix + "CODE";
		String queryString = "";
		if (StringUtils.isBlank(searchTerm)) {
			queryString = "select MEDDRA_DICT_ID as meddraDictId, " + termColumnName + " as term, " + codeColumnName
					+ " as code, PRIMARY_PATH_FLAG as primaryPathFlag from (select MEDDRA_DICT_ID, " + termColumnName + ", " + codeColumnName
					+ ", PRIMARY_PATH_FLAG, row_number() over (partition by " + codeColumnName
					+ " order by MEDDRA_DICT_ID) rn from MEDDRA_DICT_CURRENT ) where rn = 1";
		} else {
			queryString = "select MEDDRA_DICT_ID as meddraDictId, " + termColumnName + " as term, " + codeColumnName
					+ " as code, PRIMARY_PATH_FLAG as primaryPathFlag from (select MEDDRA_DICT_ID, " + termColumnName + ", " + codeColumnName
					+ ", PRIMARY_PATH_FLAG, row_number() over (partition by " + codeColumnName
					+ " order by MEDDRA_DICT_ID) rn from MEDDRA_DICT_CURRENT where upper(" + termColumnName
					+ ")  like :searchTerm ) where rn = 1";
		}

		EntityManager entityManager = this.cqtEntityManagerFactory.getEntityManager();
		Session session = entityManager.unwrap(Session.class);
		try {
			SQLQuery query = session.createSQLQuery(queryString);
			query.addScalar("meddraDictId", StandardBasicTypes.LONG);
			query.addScalar("term", StandardBasicTypes.STRING);
			query.addScalar("code", StandardBasicTypes.STRING);
			query.addScalar("primaryPathFlag", StandardBasicTypes.STRING);
			query.setFetchSize(400);
			if (!StringUtils.isBlank(searchTerm)) {
				query.setParameter("searchTerm", searchTerm.toUpperCase());
			}
			query.setResultTransformer(Transformers.aliasToBean(MeddraDictHierarchySearchDto.class));

			retVal = query.list();
		} catch (Exception e) {
			StringBuilder msg = new StringBuilder();
			msg.append("An error occurred while fetching types from MeddraDict190 on searchColumnType ")
					.append(termColumnName).append(" with value like ").append(searchTerm).append(" Query used was ->")
					.append(queryString);
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
				+ " as code, PRIMARY_PATH_FLAG as primaryPathFlag from (select MEDDRA_DICT_ID, " + termColumnName + ", " + codeColumnName
				+ ", PRIMARY_PATH_FLAG, row_number() over (partition by " + codeColumnName
				+ " order by MEDDRA_DICT_ID) rn from MEDDRA_DICT_CURRENT where " + codeColumnName
				+ " = :code ) where rn = 1";

		EntityManager entityManager = this.cqtEntityManagerFactory.getEntityManager();
		Session session = entityManager.unwrap(Session.class);
		try {
			SQLQuery query = session.createSQLQuery(queryString);
			query.addScalar("meddraDictId", StandardBasicTypes.LONG);
			query.addScalar("term", StandardBasicTypes.STRING);
			query.addScalar("code", StandardBasicTypes.STRING);
			query.addScalar("primaryPathFlag", StandardBasicTypes.STRING);
			query.setFetchSize(400);
			query.setParameter("code", code);
			query.setResultTransformer(Transformers.aliasToBean(MeddraDictHierarchySearchDto.class));

			List<MeddraDictHierarchySearchDto> dataList = query.list();
			if ((null != dataList) && (dataList.size() > 0)) {
				retVal = dataList.get(0);
			}
		} catch (Exception e) {
			StringBuilder msg = new StringBuilder();
			msg.append("An error occurred while fetching types from MeddraDict190 on searchColumnType ")
					.append(termColumnName).append(" with code equal to ").append(code).append(" Query used was ->")
					.append(queryString);
			LOG.error(msg.toString(), e);
		} finally {
			this.cqtEntityManagerFactory.closeEntityManager(entityManager);
		}
		return retVal;
	}
	
	@SuppressWarnings("unchecked")
	public MeddraDictReverseHierarchySearchDto findByPtOrLltCode(String searchColumnTypePrefix, Long code) {
		MeddraDictReverseHierarchySearchDto retVal = null;
		String codeColumnName = searchColumnTypePrefix + "CODE";
		String queryString = "select MEDDRA_DICT_ID as meddraDictId, LLT_TERM as lltTerm, LLT_CODE as lltCode"
									+ ", PT_TERM as ptTerm, PT_CODE as ptCode, HLT_TERM as hltTerm, HLT_CODE as hltCode"
									+ ", HLGT_TERM as hlgtTerm, HLGT_CODE as hlgtCode, SOC_TERM as socTerm"
									+ ", SOC_CODE as socCode, PRIMARY_PATH_FLAG as primaryPathFlag "
								+ "from (select MEDDRA_DICT_ID, LLT_TERM, LLT_CODE, PT_TERM, PT_CODE, HLT_TERM, HLT_CODE, "
											+ "HLGT_TERM, HLGT_CODE, SOC_TERM, SOC_CODE, PRIMARY_PATH_FLAG, row_number() "
										+ "over (partition by PT_CODE order by MEDDRA_DICT_ID) rn "
									+ "from MEDDRA_DICT_CURRENT	where " + codeColumnName + " = :searchCode ) where rn = 1";

		EntityManager entityManager = this.cqtEntityManagerFactory.getEntityManager();
		Session session = entityManager.unwrap(Session.class);
		try {
			SQLQuery query = session.createSQLQuery(queryString);
			query.addScalar("meddraDictId", StandardBasicTypes.LONG);
			query.addScalar("lltTerm", StandardBasicTypes.STRING);
			query.addScalar("lltCode", StandardBasicTypes.STRING);
			query.addScalar("ptTerm", StandardBasicTypes.STRING);
			query.addScalar("ptCode", StandardBasicTypes.STRING);
			query.addScalar("hltTerm", StandardBasicTypes.STRING);
			query.addScalar("hltCode", StandardBasicTypes.STRING);
			query.addScalar("hlgtTerm", StandardBasicTypes.STRING);
			query.addScalar("hlgtCode", StandardBasicTypes.STRING);
			query.addScalar("socTerm", StandardBasicTypes.STRING);
			query.addScalar("socCode", StandardBasicTypes.STRING);
			query.addScalar("primaryPathFlag", StandardBasicTypes.STRING);
			query.setFetchSize(400);
			query.setParameter("searchCode", code);
			query.setResultTransformer(Transformers.aliasToBean(MeddraDictReverseHierarchySearchDto.class));

			List<MeddraDictReverseHierarchySearchDto> dataList = query.list();
			if ((null != dataList) && (dataList.size() > 0)) {
				retVal = dataList.get(0);
			}
		} catch (Exception e) {
			StringBuilder msg = new StringBuilder();
			msg.append("An error occurred while fetching types from MeddraDict190 on searchColumnType ")
					.append(code).append(" Query used was ->")
					.append(queryString);
			LOG.error(msg.toString(), e);
		} finally {
			this.cqtEntityManagerFactory.closeEntityManager(entityManager);
		}
		return retVal;
	}
	
	@SuppressWarnings("unchecked")
	public List<MeddraDictHierarchySearchDto> findByCodes(String searchColumnTypePrefix, List<Long> codes) {
		List<MeddraDictHierarchySearchDto> retVal = null;
		String termColumnName = searchColumnTypePrefix + "TERM";
		String codeColumnName = searchColumnTypePrefix + "CODE";
		String queryString = "select MEDDRA_DICT_ID as meddraDictId, " + termColumnName + " as term, " + codeColumnName
				+ " as code, PRIMARY_PATH_FLAG as primaryPathFlag from (select MEDDRA_DICT_ID, " + termColumnName + ", " + codeColumnName
				+ ", PRIMARY_PATH_FLAG, row_number() over (partition by " + codeColumnName
				+ " order by MEDDRA_DICT_ID) rn from MEDDRA_DICT_CURRENT where " + codeColumnName
				+ " in :codeList ) where rn = 1";
		
		//setParameterList is not working here for somereason so have to do it manually
		String codesString = " ( ";
		int i = 0;
		for (Long code : codes) {
			codesString += code;
			if(++i < codes.size()) {
				codesString += " , ";
			}
		}
		codesString += " ) ";
		queryString = queryString.replace(":codeList", codesString);
		
		EntityManager entityManager = this.cqtEntityManagerFactory.getEntityManager();
		Session session = entityManager.unwrap(Session.class);
		try {
			SQLQuery query = session.createSQLQuery(queryString);
			query.addScalar("meddraDictId", StandardBasicTypes.LONG);
			query.addScalar("term", StandardBasicTypes.STRING);
			query.addScalar("code", StandardBasicTypes.STRING);
			query.addScalar("primaryPathFlag", StandardBasicTypes.STRING);
			query.setFetchSize(400);
			//query.setParameterList("codeList", codes);
			query.setResultTransformer(Transformers.aliasToBean(MeddraDictHierarchySearchDto.class));

			retVal = query.list();
		} catch (Exception e) {
			StringBuilder msg = new StringBuilder();
			msg.append("An error occurred while fetching types from MeddraDict190 on searchColumnType ")
					.append(termColumnName).append(" with code equal to ").append(codes).append(" Query used was ->")
					.append(queryString);
			LOG.error(msg.toString(), e);
		} finally {
			this.cqtEntityManagerFactory.closeEntityManager(entityManager);
		}
		return retVal;
	}

	public Long findChldrenCountByParentCode(String searchColumnTypePrefix,
			String parentCodeColumnPrefix, Long parentCode) {
		Long retVal = null;
		String termColumnName = searchColumnTypePrefix + "TERM";
		String codeColumnName = searchColumnTypePrefix + "CODE";
		String parentCodeColumnName = parentCodeColumnPrefix + "CODE";
		String queryString = "select count(*) "
				+ " from (select MEDDRA_DICT_ID, " + termColumnName + ", " + codeColumnName
				+ ", row_number() over (partition by " + codeColumnName
				+ " order by MEDDRA_DICT_ID) rn from MEDDRA_DICT_CURRENT where " + parentCodeColumnName
				+ " = :code ) where rn = 1";

		EntityManager entityManager = this.cqtEntityManagerFactory.getEntityManager();
		Session session = entityManager.unwrap(Session.class);
		try {
			SQLQuery query = session.createSQLQuery(queryString);
			query.setParameter("code", parentCode);
			query.setFetchSize(400);
			retVal = ((Number)query.uniqueResult()).longValue();
		} catch (Exception e) {
			StringBuilder msg = new StringBuilder();
			msg.append("An error occurred in findChldrenCountByParentCode termColumnName was ").append(termColumnName)
					.append(" codeColumnName was ").append(codeColumnName).append(" parentCodeColumnName was ")
					.append(parentCodeColumnName).append(" parentCode was ").append(parentCode)
					.append(" Query used was ->").append(queryString);
			LOG.error(msg.toString(), e);
		} finally {
			this.cqtEntityManagerFactory.closeEntityManager(entityManager);
		}
		return retVal;
	}

	@SuppressWarnings("unchecked")
	public List<MeddraDictHierarchySearchDto> findChildrenByParentCode(String searchColumnTypePrefix,
			String parentCodeColumnPrefix, Long parentCode) {
		List<MeddraDictHierarchySearchDto> retVal = null;
		String termColumnName = searchColumnTypePrefix + "TERM";
		String codeColumnName = searchColumnTypePrefix + "CODE";
		String parentCodeColumnName = parentCodeColumnPrefix + "CODE";
		String queryString = "select MEDDRA_DICT_ID as meddraDictId, " + termColumnName + " as term, " + codeColumnName
				+ " as code, PRIMARY_PATH_FLAG as primaryPathFlag from (select MEDDRA_DICT_ID, " + termColumnName + ", " + codeColumnName
				+ ", PRIMARY_PATH_FLAG, row_number() over (partition by " + codeColumnName
				+ " order by MEDDRA_DICT_ID) rn from MEDDRA_DICT_CURRENT where " + parentCodeColumnName
				+ " = :code ) where rn = 1";

		EntityManager entityManager = this.cqtEntityManagerFactory.getEntityManager();
		Session session = entityManager.unwrap(Session.class);
		try {
			SQLQuery query = session.createSQLQuery(queryString);
			query.addScalar("meddraDictId", StandardBasicTypes.LONG);
			query.addScalar("term", StandardBasicTypes.STRING);
			query.addScalar("code", StandardBasicTypes.STRING);
			query.addScalar("primaryPathFlag", StandardBasicTypes.STRING);
			query.setFetchSize(400);
			query.setParameter("code", parentCode);
			query.setResultTransformer(Transformers.aliasToBean(MeddraDictHierarchySearchDto.class));

			retVal = query.list();
		} catch (Exception e) {
			StringBuilder msg = new StringBuilder();
			msg.append("An error occurred in findChldrenByParentCode termColumnName was ").append(termColumnName)
					.append(" codeColumnName was ").append(codeColumnName).append(" parentCodeColumnName was ")
					.append(parentCodeColumnName).append(" parentCode was ").append(parentCode)
					.append(" Query used was ->").append(queryString);
			LOG.error(msg.toString(), e);
		} finally {
			this.cqtEntityManagerFactory.closeEntityManager(entityManager);
		}
		return retVal;
	}

}
