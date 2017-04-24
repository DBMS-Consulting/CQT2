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
import java.util.LinkedList;
import org.apache.commons.collections4.ListUtils;

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
	public List<MeddraDictReverseHierarchySearchDto> findFullReverseHierarchyByLevelAndTerm(String searchColumnPrefix
															, String partitionColumnPrefix, String searchTerm) {
		List<MeddraDictReverseHierarchySearchDto> retVal = null;
		String termSearchColumnName = (searchColumnPrefix.endsWith("_") ? searchColumnPrefix : searchColumnPrefix +"_") + "TERM";
		String codePartitionColumnName = (partitionColumnPrefix.endsWith("_") ? partitionColumnPrefix : partitionColumnPrefix +"_") + "CODE";
		String queryString = "";
		if (StringUtils.isBlank(searchTerm)) {
			queryString = "select MEDDRA_DICT_ID as meddraDictId, LLT_TERM as lltTerm, LLT_CODE as lltCode"
								+ ", PT_TERM as ptTerm, PT_CODE as ptCode, HLT_TERM as hltTerm, HLT_CODE as hltCode"
								+ ", HLGT_TERM as hlgtTerm, HLGT_CODE as hlgtCode, SOC_TERM as socTerm"
								+ ", SOC_CODE as socCode, PRIMARY_PATH_FLAG as primaryPathFlag "
							+ "from (select MEDDRA_DICT_ID, LLT_TERM, LLT_CODE, PT_TERM, PT_CODE, HLT_TERM, HLT_CODE, "
										+ "HLGT_TERM, HLGT_CODE, SOC_TERM, SOC_CODE, PRIMARY_PATH_FLAG, row_number() "
									+ "over (partition by " + codePartitionColumnName + " order by MEDDRA_DICT_ID) rn "
								+ "from MEDDRA_DICT_CURRENT) where rn = 1";
		} else {
			queryString = "select MEDDRA_DICT_ID as meddraDictId, LLT_TERM as lltTerm, LLT_CODE as lltCode"
								+ ", PT_TERM as ptTerm, PT_CODE as ptCode, HLT_TERM as hltTerm, HLT_CODE as hltCode"
								+ ", HLGT_TERM as hlgtTerm, HLGT_CODE as hlgtCode, SOC_TERM as socTerm"
								+ ", SOC_CODE as socCode, PRIMARY_PATH_FLAG as primaryPathFlag "
							+ "from (select MEDDRA_DICT_ID, LLT_TERM, LLT_CODE, PT_TERM, PT_CODE, HLT_TERM, HLT_CODE, "
										+ "HLGT_TERM, HLGT_CODE, SOC_TERM, SOC_CODE, PRIMARY_PATH_FLAG, row_number() "
									+ "over (partition by " + codePartitionColumnName + " order by MEDDRA_DICT_ID) rn "
								+ "from MEDDRA_DICT_CURRENT	where upper(" + termSearchColumnName + ") like :searchTerm ) where rn = 1";
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
			query.setCacheable(true);
			retVal = query.list();
		} catch (Exception e) {
			StringBuilder msg = new StringBuilder();
			msg.append("An error occurred while executing findFullReverseHierarchyByLevelAndTerm.  Search Term was ")
					.append(searchTerm).append(" and partition column was ").append(codePartitionColumnName).append(" Query used was ->")
					.append(queryString);
			LOG.error(msg.toString(), e);
		} finally {
			this.cqtEntityManagerFactory.closeEntityManager(entityManager);
		}
		return retVal;
	}

	@SuppressWarnings("unchecked")
	public List<MeddraDictReverseHierarchySearchDto> findReverseByCode(String searchColumnTypePrefix, String partitionColumnPrefix, Long code) {
		List<MeddraDictReverseHierarchySearchDto> retVal = null;
		String codeSearchColumnName = searchColumnTypePrefix + "CODE";
		String codePartitionColumnName = partitionColumnPrefix + "CODE";
		String queryString = "select MEDDRA_DICT_ID as meddraDictId, LLT_TERM as lltTerm, LLT_CODE as lltCode"
				+ ", PT_TERM as ptTerm, PT_CODE as ptCode, HLT_TERM as hltTerm, HLT_CODE as hltCode"
				+ ", HLGT_TERM as hlgtTerm, HLGT_CODE as hlgtCode, SOC_TERM as socTerm"
				+ ", SOC_CODE as socCode, PRIMARY_PATH_FLAG as primaryPathFlag "
			+ "from (select MEDDRA_DICT_ID, LLT_TERM, LLT_CODE, PT_TERM, PT_CODE, HLT_TERM, HLT_CODE, "
						+ "HLGT_TERM, HLGT_CODE, SOC_TERM, SOC_CODE, PRIMARY_PATH_FLAG, row_number() "
					+ "over (partition by " + codePartitionColumnName + " order by MEDDRA_DICT_ID) rn "
				+ "from MEDDRA_DICT_CURRENT	where " + codeSearchColumnName + " = :code ) where rn = 1";

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
			query.setParameter("code", code);
			query.setResultTransformer(Transformers.aliasToBean(MeddraDictReverseHierarchySearchDto.class));
			query.setCacheable(true);
			retVal = query.list();
		} catch (Exception e) {
			StringBuilder msg = new StringBuilder();
			msg.append("An error occurred while fetching findReverseByCode on searchColumnType ")
					.append(searchColumnTypePrefix).append(" with code equal to ").append(code).append(" Query used was ->")
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
			query.setCacheable(true);
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
			query.setCacheable(true);
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
			query.setCacheable(true);
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
		
		String codeAlias = searchColumnTypePrefix.replace("_", "").toLowerCase().concat("Code");
		
		String queryString = "select MEDDRA_DICT_ID as meddraDictId, " + termColumnName + " as term, " + codeColumnName
				+ " as " + codeAlias + ", PRIMARY_PATH_FLAG as primaryPathFlag, MOVED_LLT as movedLlt, NEW_PT as newPt, PROMOTED_PT as promotedPt, NEW_LLT as newLlt, DEMOTED_LLT as demotedLlt, "
				+ " PROMOTED_LLT as promotedLlt, PRIMARY_SOC_CHANGE as primarySocChange, DEMOTED_PT as demotedPt, LLT_CURRENCY_CHANGE as lltCurrencyChange, PT_NAME_CHANGED as ptNameChanged,"
				+ " LLT_NAME_CHANGED as lltNameChanged, NEW_HLT as newHlt, NEW_HLGT as newHlgt, MOVED_PT as movedPt, MOVED_HLT as movedHlt, MOVED_HLGT as movedHlgt, "
				+ " HLGT_NAME_CHANGED as hlgtNameChanged, HLT_NAME_CHANGED as hltNameChanged, SOC_NAME_CHANGED as socNameChanged, MERGED_HLT as mergedHlt, MERGED_HLGT as mergedHlgt" 
				+ " from (select MEDDRA_DICT_ID, " + termColumnName + ", " + codeColumnName 
				+ " , PRIMARY_PATH_FLAG, MOVED_PT, MOVED_LLT, NEW_PT, PROMOTED_PT, NEW_LLT, DEMOTED_LLT, PROMOTED_LLT, PRIMARY_SOC_CHANGE, DEMOTED_PT, LLT_CURRENCY_CHANGE, PT_NAME_CHANGED, "
				+ " LLT_NAME_CHANGED, NEW_HLT, NEW_HLGT, MOVED_HLT, MOVED_HLGT, HLGT_NAME_CHANGED, HLT_NAME_CHANGED, SOC_NAME_CHANGED, MERGED_HLT, MERGED_HLGT,"
				+ " row_number() over (partition by " + codeColumnName
				+ " order by MEDDRA_DICT_ID) rn from MEDDRA_DICT_CURRENT where " + codeColumnName
				+ " in :codeList ) where rn = 1";
        
        // IN clause allows maximum 1000 elements in the list condition, so we will partition the list of codes
        List<List<Long>> partitionedCodes = ListUtils.partition(codes, 1000);
        retVal = new LinkedList<>();
		
		EntityManager entityManager = this.cqtEntityManagerFactory.getEntityManager();
		Session session = entityManager.unwrap(Session.class);
		try {
            for(List<Long> pcds : partitionedCodes ) {
                //setParameterList is not working here for somereason so have to do it manually
                String codesString = " ( ";
                int i = 0;
                for (Long code : pcds) {
                    codesString += code;
                    if(++i < pcds.size()) {
                        codesString += " , ";
                    }
                }
                codesString += " ) ";
                String qs = queryString.replace(":codeList", codesString);

                SQLQuery query = session.createSQLQuery(qs);
                query.addScalar("meddraDictId", StandardBasicTypes.LONG);
                query.addScalar("term", StandardBasicTypes.STRING);
                //query.addScalar("code", StandardBasicTypes.STRING);
                query.addScalar("primaryPathFlag", StandardBasicTypes.STRING);

                query.addScalar("newPt", StandardBasicTypes.STRING);
                query.addScalar("promotedPt", StandardBasicTypes.STRING);
                query.addScalar("newLlt", StandardBasicTypes.STRING);
                query.addScalar("demotedLlt", StandardBasicTypes.STRING);
                query.addScalar("promotedLlt", StandardBasicTypes.STRING);

                query.addScalar("primarySocChange", StandardBasicTypes.STRING);
                query.addScalar("demotedPt", StandardBasicTypes.STRING);
                query.addScalar("movedLlt", StandardBasicTypes.STRING);
                query.addScalar("demotedLlt", StandardBasicTypes.STRING);
                query.addScalar("lltCurrencyChange", StandardBasicTypes.STRING);

                query.addScalar("ptNameChanged", StandardBasicTypes.STRING);
                query.addScalar("lltNameChanged", StandardBasicTypes.STRING);
                query.addScalar("newHlt", StandardBasicTypes.STRING);
                query.addScalar("newHlgt", StandardBasicTypes.STRING);
                query.addScalar("movedPt", StandardBasicTypes.STRING);

                query.addScalar("movedHlt", StandardBasicTypes.STRING);
                query.addScalar("movedHlgt", StandardBasicTypes.STRING);
                query.addScalar("hlgtNameChanged", StandardBasicTypes.STRING);
                query.addScalar("hltNameChanged", StandardBasicTypes.STRING);
                query.addScalar("socNameChanged", StandardBasicTypes.STRING);
                query.addScalar("mergedHlt", StandardBasicTypes.STRING);
                query.addScalar("mergedHlgt", StandardBasicTypes.STRING);

                query.addScalar(codeAlias, StandardBasicTypes.STRING);

                query.setFetchSize(400);
                //query.setParameterList("codeList", codes);
                query.setResultTransformer(Transformers.aliasToBean(MeddraDictHierarchySearchDto.class));
                query.setCacheable(true);
                retVal.addAll(query.list());
            }
		} catch (Exception e) {
			e.printStackTrace();
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
			//query.setCacheable(true);
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
		
		//Using this alias to get code - Either PT, HLT, LLT, HLGT, or SOC
		String codeAlias = searchColumnTypePrefix.replace("_", "").toLowerCase().concat("Code");
		
		String queryString = "select MEDDRA_DICT_ID as meddraDictId, " + termColumnName + " as term, " + codeColumnName
				+ " as " + codeAlias + ", PRIMARY_PATH_FLAG as primaryPathFlag, MOVED_LLT as movedLlt, NEW_PT as newPt, PROMOTED_PT as promotedPt, NEW_LLT as newLlt, DEMOTED_LLT as demotedLlt, "
				+ " PROMOTED_LLT as promotedLlt, PRIMARY_SOC_CHANGE as primarySocChange, DEMOTED_PT as demotedPt, LLT_CURRENCY_CHANGE as lltCurrencyChange, PT_NAME_CHANGED as ptNameChanged,"
				+ " LLT_NAME_CHANGED as lltNameChanged, NEW_HLT as newHlt, NEW_HLGT as newHlgt, MOVED_PT as movedPt, MOVED_HLT as movedHlt, MOVED_HLGT as movedHlgt, "
				+ " HLGT_NAME_CHANGED as hlgtNameChanged, HLT_NAME_CHANGED as hltNameChanged, SOC_NAME_CHANGED as socNameChanged, MERGED_HLT as mergedHlt, MERGED_HLGT as mergedHlgt" 
				+ " from (select MEDDRA_DICT_ID, " + termColumnName + ", " + codeColumnName 
				+ " , PRIMARY_PATH_FLAG, MOVED_PT, MOVED_LLT, NEW_PT, PROMOTED_PT, NEW_LLT, DEMOTED_LLT, PROMOTED_LLT, PRIMARY_SOC_CHANGE, DEMOTED_PT, LLT_CURRENCY_CHANGE, PT_NAME_CHANGED, "
				+ " LLT_NAME_CHANGED, NEW_HLT, NEW_HLGT, MOVED_HLT, MOVED_HLGT, HLGT_NAME_CHANGED, HLT_NAME_CHANGED, SOC_NAME_CHANGED, MERGED_HLT, MERGED_HLGT,"
				+ " row_number() over (partition by " + codeColumnName
				+ " order by MEDDRA_DICT_ID) rn from MEDDRA_DICT_CURRENT where " + parentCodeColumnName
				+ " in :" + codeAlias + " ) where rn = 1";

		EntityManager entityManager = this.cqtEntityManagerFactory.getEntityManager();
		Session session = entityManager.unwrap(Session.class);
		try {
			SQLQuery query = session.createSQLQuery(queryString);
			query.addScalar("meddraDictId", StandardBasicTypes.LONG);
			query.addScalar("term", StandardBasicTypes.STRING);
			//query.addScalar("code", StandardBasicTypes.STRING);
			query.addScalar("primaryPathFlag", StandardBasicTypes.STRING);
			
			query.addScalar("newPt", StandardBasicTypes.STRING);
			query.addScalar("promotedPt", StandardBasicTypes.STRING);
			query.addScalar("newLlt", StandardBasicTypes.STRING);
			query.addScalar("demotedLlt", StandardBasicTypes.STRING);
			query.addScalar("promotedLlt", StandardBasicTypes.STRING);

			query.addScalar("primarySocChange", StandardBasicTypes.STRING);
			query.addScalar("demotedPt", StandardBasicTypes.STRING);
			query.addScalar("movedLlt", StandardBasicTypes.STRING);
			query.addScalar("demotedLlt", StandardBasicTypes.STRING);
			query.addScalar("lltCurrencyChange", StandardBasicTypes.STRING);

			query.addScalar("ptNameChanged", StandardBasicTypes.STRING);
			query.addScalar("lltNameChanged", StandardBasicTypes.STRING);
			query.addScalar("newHlt", StandardBasicTypes.STRING);
			query.addScalar("newHlgt", StandardBasicTypes.STRING);
			query.addScalar("movedPt", StandardBasicTypes.STRING);

			query.addScalar("movedHlt", StandardBasicTypes.STRING);
			query.addScalar("movedHlgt", StandardBasicTypes.STRING);
			query.addScalar("hlgtNameChanged", StandardBasicTypes.STRING);
			query.addScalar("hltNameChanged", StandardBasicTypes.STRING);
			query.addScalar("socNameChanged", StandardBasicTypes.STRING);
			query.addScalar("mergedHlt", StandardBasicTypes.STRING);
			query.addScalar("mergedHlgt", StandardBasicTypes.STRING);
			
			query.addScalar(codeAlias, StandardBasicTypes.STRING);
			
			query.setFetchSize(400);
//			query.setParameter("code", parentCode);
			query.setParameter(codeAlias, parentCode);
			query.setResultTransformer(Transformers.aliasToBean(MeddraDictHierarchySearchDto.class));
			query.setCacheable(true);
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
