package com.dbms.service;

import com.dbms.csmq.CSMQBean;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.faces.bean.ApplicationScoped;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.metamodel.EntityType;
import javax.persistence.metamodel.Metamodel;

import org.apache.commons.lang3.StringUtils;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.hibernate.SQLQuery;
import org.hibernate.Session;
import org.hibernate.transform.Transformers;
import org.hibernate.type.StandardBasicTypes;
import org.primefaces.model.DefaultStreamedContent;
import org.primefaces.model.SortOrder;
import org.primefaces.model.StreamedContent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dbms.entity.cqt.CmqBase190;
import com.dbms.entity.cqt.CmqBaseTarget;
import com.dbms.entity.cqt.CmqRelationTarget;
import com.dbms.entity.cqt.SmqBase190;
import com.dbms.entity.cqt.SmqBaseTarget;
import com.dbms.entity.cqt.SmqBaseTarget;
import com.dbms.entity.cqt.SmqRelation190;
import com.dbms.entity.cqt.SmqRelationTarget;
import com.dbms.entity.cqt.dtos.MeddraDictHierarchySearchDto;
import com.dbms.entity.cqt.dtos.ReportLineDataDto;
import com.dbms.service.base.CqtPersistenceService;
import com.dbms.util.CmqUtils;
import org.apache.commons.collections4.CollectionUtils;

/**
 * @author Jay G.(jayshanchn@hotmail.com)
 * @date Feb 11, 2017 8:00:48 AM
 **/
@ManagedBean(name = "SmqBaseTargetService")
@ApplicationScoped
public class SmqBaseTargetService extends CqtPersistenceService<SmqBaseTarget> implements ISmqBaseTargetService {

	private static final Logger LOG = LoggerFactory.getLogger(SmqBaseTargetService.class);
	
	@ManagedProperty("#{MeddraDictTargetService}")
	private IMeddraDictTargetService meddraDictService;

	@Override
	@SuppressWarnings("unchecked")
	public List<SmqBaseTarget> findByLevelAndTerm(Integer level, String searchTerm) {
		List<SmqBaseTarget> retVal = null;
		StringBuilder sb = new StringBuilder();
		sb.append("from SmqBaseTarget c where c.smqLevel = :smqLevel ");
		if(!StringUtils.isBlank(searchTerm)) {
			sb.append("and upper(c.smqName) like :smqName");
		}
		EntityManager entityManager = this.cqtEntityManagerFactory.getEntityManager();
		try {
			Query query = entityManager.createQuery(sb.toString());
			query.setParameter("smqLevel", level);
			
			if(!StringUtils.isBlank(searchTerm)) {
				query.setParameter("smqName", searchTerm.toUpperCase());
			}
			retVal = query.getResultList();
		} catch (Exception e) {
			StringBuilder msg = new StringBuilder();
			msg
					.append("An error occurred while fetching types from SmqBaseTarget on smqLevel ")
					.append(level)
					.append(" with smqName like ")
					.append(searchTerm.toUpperCase())
					.append(" Query used was ->")
					.append(sb.toString());
			LOG.error(msg.toString(), e);
		} finally {
			this.cqtEntityManagerFactory.closeEntityManager(entityManager);
		}
		return retVal;
	}

	@Override
	public Long findSmqRelationsCountForSmqCode(Long smqCode) {
		Long retVal = null;
		StringBuilder sb = new StringBuilder();
		sb.append("select count(*) from SmqRelationTarget c where c.smqCode = :smqCode");
		
		EntityManager entityManager = this.cqtEntityManagerFactory.getEntityManager();
		try {
			Query query = entityManager.createQuery(sb.toString());
			query.setParameter("smqCode", smqCode);
			retVal = (Long)query.getSingleResult();
		} catch (Exception e) {
			StringBuilder msg = new StringBuilder();
			msg
					.append("An error occurred while findSmqRelationsCountForSmqCode ")
					.append(smqCode)
					.append(" Query used was ->")
					.append(sb.toString());
			LOG.error(msg.toString(), e);
		} finally {
			this.cqtEntityManagerFactory.closeEntityManager(entityManager);
		}
		return retVal;
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public List<Map<String, Object>> findSmqRelationsCountForSmqCodes(List<Long> smqCodes) {
		List<Map<String, Object>> retVal = null;
        
        if(CollectionUtils.isEmpty(smqCodes))
            return null;
		
        String queryString = CmqUtils.convertArrayToTableWith(smqCodes, "tempSmqCodes", "code")
                + " select count(*) as COUNT, SMQ_CODE"
                + " from SMQ_RELATIONS_TARGET smqTbl"
                + " inner join tempSmqCodes on tempSmqCodes.code=smqTbl.SMQ_CODE"
                + " group by SMQ_CODE";
		
		EntityManager entityManager = this.cqtEntityManagerFactory.getEntityManager();
		Session session = entityManager.unwrap(Session.class);
		try {
			SQLQuery query = session.createSQLQuery(queryString);
			query.addScalar("SMQ_CODE", StandardBasicTypes.LONG);
			query.addScalar("COUNT", StandardBasicTypes.LONG);
			query.setParameterList("smqCodes", smqCodes);
			query.setResultTransformer(Transformers.ALIAS_TO_ENTITY_MAP);
			retVal = query.list();
		} catch (Exception e) {
			StringBuilder msg = new StringBuilder();
			msg
					.append("An error occurred while findSmqRelationsCountForSmqCodes ")
					.append(smqCodes)
					.append(" Query used was ->")
					.append(queryString);
			LOG.error(msg.toString(), e);
		} finally {
			this.cqtEntityManagerFactory.closeEntityManager(entityManager);
		}
		return retVal;
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public List<SmqRelationTarget> findSmqRelationsForSmqCode(Long smqCode) {
		List<SmqRelationTarget> retVal = null;
		StringBuilder sb = new StringBuilder();
		sb.append("from SmqRelationTarget c where c.smqCode = :smqCode order by c.smqLevel asc");
		
		EntityManager entityManager = this.cqtEntityManagerFactory.getEntityManager();
		try {
			Query query = entityManager.createQuery(sb.toString());
			query.setParameter("smqCode", smqCode);
			retVal = query.getResultList();
		} catch (Exception e) {
			StringBuilder msg = new StringBuilder();
			msg
					.append("An error occurred while findSmqRelationsForSmqCode ")
					.append(smqCode)
					.append(" Query used was ->")
					.append(sb.toString());
			LOG.error(msg.toString(), e);
		} finally {
			this.cqtEntityManagerFactory.closeEntityManager(entityManager);
		}
		return retVal;
	}
	
	@Override
	public SmqRelationTarget findSmqRelationBySmqAndPtCode(Long smqCode, Integer ptCode) {
		SmqRelationTarget retVal = null;
		StringBuilder sb = new StringBuilder();
		sb.append("from SmqRelationTarget c where c.smqCode = :smqCode and c.ptCode = :ptCode");
		
		EntityManager entityManager = this.cqtEntityManagerFactory.getEntityManager();
		try {
			Query query = entityManager.createQuery(sb.toString());
			query.setParameter("smqCode", smqCode);
			query.setParameter("ptCode", ptCode);
			retVal = (SmqRelationTarget) query.getSingleResult();
		} catch (Exception e) {
			StringBuilder msg = new StringBuilder();
			msg
					.append("An error occurred while findSmqRelationsForSmqCode ")
					.append(smqCode)
					.append(" Query used was ->")
					.append(sb.toString());
			LOG.error(msg.toString(), e);
		} finally {
			this.cqtEntityManagerFactory.closeEntityManager(entityManager);
		}
		return retVal;
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public List<SmqRelationTarget> findSmqRelationsForSmqCodes(List<Long> smqCodes) {
		List<SmqRelationTarget> retVal = null;
		StringBuilder sb = new StringBuilder();
		sb.append("from SmqRelationTarget c where c.smqCode in (:smqCodes) order by c.smq_Code asc, c.ptName asc");
		
		EntityManager entityManager = this.cqtEntityManagerFactory.getEntityManager();
		try {
			Query query = entityManager.createQuery(sb.toString());
			query.setParameter("smqCode", smqCodes);
			retVal = query.getResultList();
		} catch (Exception e) {
			StringBuilder msg = new StringBuilder();
			msg
					.append("An error occurred while findSmqRelationsForSmqCodes ")
					.append(smqCodes)
					.append(" Query used was ->")
					.append(sb.toString());
			LOG.error(msg.toString(), e);
		} finally {
			this.cqtEntityManagerFactory.closeEntityManager(entityManager);
		}
		return retVal;
	}
	
	@Override
	public List<SmqBaseTarget> findChildSmqByParentSmqCodes(List<Long> smqCodes) {
		List<SmqBaseTarget> retVal = null;
		StringBuilder sb = new StringBuilder();
		sb.append("from SmqBaseTarget c where c.smqParentCode in (:smqParentCodes) order by c.smqParentCode");
		
		EntityManager entityManager = this.cqtEntityManagerFactory.getEntityManager();
		try {
			Query query = entityManager.createQuery(sb.toString());
			query.setParameter("smqParentCodes", smqCodes);
			retVal = query.getResultList();
		} catch (Exception e) {
			StringBuilder msg = new StringBuilder();
			msg
					.append("An error occurred while findChildSmqByParentSmqCodes ")
					.append(smqCodes)
					.append(" Query used was ->")
					.append(sb.toString());
			LOG.error(msg.toString(), e);
		} finally {
			this.cqtEntityManagerFactory.closeEntityManager(entityManager);
		}
		return retVal;
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public List<SmqBaseTarget> findChildSmqByParentSmqCode(Long smqCode) {
		List<SmqBaseTarget> retVal = null;
		StringBuilder sb = new StringBuilder();
		sb.append("from SmqBaseTarget c where c.smqParentCode = :smqParentCode");
		
		EntityManager entityManager = this.cqtEntityManagerFactory.getEntityManager();
		try {
			Query query = entityManager.createQuery(sb.toString());
			query.setParameter("smqParentCode", smqCode);
			retVal = query.getResultList();
		} catch (Exception e) {
			StringBuilder msg = new StringBuilder();
			msg
					.append("An error occurred while findChildSmqByParentSmqCode ")
					.append(smqCode)
					.append(" Query used was ->")
					.append(sb.toString());
			LOG.error(msg.toString(), e);
		} finally {
			this.cqtEntityManagerFactory.closeEntityManager(entityManager);
		}
		return retVal;
	}
	
	@Override
	public Long findChildSmqCountByParentSmqCode(Long smqCode) {
		Long retVal = null;
		StringBuilder sb = new StringBuilder();
		sb.append("select count(*) from SmqBaseTarget c where c.smqParentCode = :smqParentCode");
		
		EntityManager entityManager = this.cqtEntityManagerFactory.getEntityManager();
		try {
			Query query = entityManager.createQuery(sb.toString());
			query.setParameter("smqParentCode", smqCode);
			retVal = (Long)query.getSingleResult();
		} catch (Exception e) {
			StringBuilder msg = new StringBuilder();
			msg
					.append("An error occurred while findChildSmqCountByParentSmqCode ")
					.append(smqCode)
					.append(" Query used was ->")
					.append(sb.toString());
			LOG.error(msg.toString(), e);
		} finally {
			this.cqtEntityManagerFactory.closeEntityManager(entityManager);
		}
		return retVal;
	}
	
	@Override
	public SmqBaseTarget findByCode(Long smqCode) {
		SmqBaseTarget retVal = null;
		String queryString = "from SmqBaseTarget c where c.smqCode = :smqCode ";
		EntityManager entityManager = this.cqtEntityManagerFactory.getEntityManager();
		try {
			Query query = entityManager.createQuery("from SmqBaseTarget c where c.smqCode = :smqCode ");
			query.setParameter("smqCode", smqCode);
			retVal = (SmqBaseTarget) query.getSingleResult();
		} catch (Exception e) {
			StringBuilder msg = new StringBuilder();
			msg
					.append("An error occurred while findSmqRelationsForSmqCode ")
					.append(smqCode)
					.append(" Query used was ->")
					.append(queryString);
			LOG.error(msg.toString(), e);
		} finally {
			this.cqtEntityManagerFactory.closeEntityManager(entityManager);
		}
		return retVal;
	}
    
    @SuppressWarnings("unchecked")
	@Override
	public List<SmqBaseTarget> findByCodes(List<Long> smqCodes) {
		List<SmqBaseTarget> retVal = null;
		String queryString = "from SmqBaseTarget c where c.smqCode in (:smqCodes) ";
		EntityManager entityManager = this.cqtEntityManagerFactory.getEntityManager();
		try {
			Query query = entityManager.createQuery(queryString);
			query.setParameter("smqCodes", smqCodes);
			query.setHint("org.hibernate.cacheable", true);
			retVal = query.getResultList();
		} catch (Exception e) {
			StringBuilder msg = new StringBuilder();
			msg
					.append("An error occurred while findByCodes ")
					.append(smqCodes)
					.append(" Query used was ->")
					.append(queryString);
			LOG.error(msg.toString(), e);
		} finally {
			this.cqtEntityManagerFactory.closeEntityManager(entityManager);
		}
		return retVal;
	}
	
	@Override
	public List<SmqBaseTarget> findImpactedWithPaginated(int first, int pageSize, String sortField
														, SortOrder sortOrder, Map<String, Object> filters) {
		List<SmqBaseTarget> retVal = null;
		EntityManager entityManager = this.cqtEntityManagerFactory.getEntityManager();
		try {
			CriteriaBuilder cb = entityManager.getCriteriaBuilder();
			CriteriaQuery<SmqBaseTarget> cq = cb.createQuery(SmqBaseTarget.class);
			Root<SmqBaseTarget> smqRoot = cq.from(SmqBaseTarget.class);

			List<Predicate> pred = new ArrayList<Predicate>();
			
			pred.add(cb.equal(smqRoot.get("impactType"), CSMQBean.IMPACT_TYPE_IMPACTED));
			
			if(filters.containsKey("smqName") && filters.get("smqName") != null)
				pred.add(cb.like(cb.lower(smqRoot.<String>get("smqName")), "%" + ((String)filters.get("smqName")).toLowerCase() + "%"));
			
			if(filters.containsKey("smqLevel") && filters.get("smqLevel") != null)
				pred.add(cb.equal(smqRoot.get("smqLevel"), filters.get("smqLevel")));
			
			cq.where(cb.and(pred.toArray(new Predicate[0])));
			cq.orderBy(cb.asc(smqRoot.get("smqName")));
			
			TypedQuery<SmqBaseTarget> tq = entityManager.createQuery(cq);
			
			if (pageSize >= 0){
	            tq.setMaxResults(pageSize);
	        }
	        if (first >= 0){
	            tq.setFirstResult(first);
	        }
			
			retVal = tq.getResultList();
		} catch (Exception e) {
			StringBuilder msg = new StringBuilder();
			msg.append("An error occurred while fetching paginated impacted SmqBaseTarget.");
			LOG.error(msg.toString(), e);
		} finally {
			this.cqtEntityManagerFactory.closeEntityManager(entityManager);
		}
		return retVal;
	}
	
	/* (non-Javadoc)
	 * @see com.dbms.service.ISmqBaseTargetService#findImpactedCount()
	 */
	@Override
	public Long findImpactedCount() {
		Long retVal = null;
		StringBuilder sb = new StringBuilder();
		sb.append("select count(*) from SmqBaseTarget c where c.impactType = 'IMPACTED'");
		
		EntityManager entityManager = this.cqtEntityManagerFactory.getEntityManager();
		try {
			Query query = entityManager.createQuery(sb.toString());
			retVal = (Long)query.getSingleResult();
		} catch (Exception e) {
			StringBuilder msg = new StringBuilder();
			msg
					.append("An error occurred in findImpactedCount ")
					.append(" Query used was ->")
					.append(sb.toString());
			LOG.error(msg.toString(), e);
		} finally {
			this.cqtEntityManagerFactory.closeEntityManager(entityManager);
		}
		return retVal;
	}
	
	/* (non-Javadoc)
	 * @see com.dbms.service.ISmqBaseTargetService#findNotImpactedWithPaginated(int, int, java.lang.String, org.primefaces.model.SortOrder, java.util.Map)
	 */
	@Override
	public List<SmqBaseTarget> findNotImpactedWithPaginated(int first, int pageSize, String sortField
			, SortOrder sortOrder, Map<String, Object> filters) {
		List<SmqBaseTarget> retVal = null;
		EntityManager entityManager = this.cqtEntityManagerFactory.getEntityManager();
		try {
			CriteriaBuilder cb = entityManager.getCriteriaBuilder();
			CriteriaQuery<SmqBaseTarget> cq = cb.createQuery(SmqBaseTarget.class);
			Root<SmqBaseTarget> smqRoot = cq.from(SmqBaseTarget.class);
			List<Predicate> pred = new ArrayList<Predicate>();
			
			pred.add(cb.equal(smqRoot.get("impactType"), CSMQBean.IMPACT_TYPE_NONIMPACTED));
			
			if(filters.containsKey("smqName") && filters.get("smqName") != null)
				pred.add(cb.like(cb.lower(smqRoot.<String>get("smqName")), "%" + ((String)filters.get("smqName")).toLowerCase() + "%"));			
			if(filters.containsKey("smqLevel") && filters.get("smqLevel") != null)
				pred.add(cb.equal(smqRoot.get("smqLevel"), filters.get("smqLevel")));
			
			cq.where(cb.and(pred.toArray(new Predicate[0])));
			
			cq.orderBy(cb.asc(smqRoot.get("smqName")));
			TypedQuery<SmqBaseTarget> tq = entityManager.createQuery(cq);
			
			if (pageSize >= 0){
			tq.setMaxResults(pageSize);
			}
			if (first >= 0){
			tq.setFirstResult(first);
			}
			
			retVal = tq.getResultList();
		} catch (Exception e) {
			StringBuilder msg = new StringBuilder();
			msg.append("An error occurred while fetching paginated impacted SmqBaseTarget.");
			LOG.error(msg.toString(), e);
		} finally {
			this.cqtEntityManagerFactory.closeEntityManager(entityManager);
		}
		return retVal;
	}

	/* (non-Javadoc)
	 * @see com.dbms.service.ISmqBaseTargetService#findNotImpactedCount()
	 */
	@Override
	public Long findNotImpactedCount() {
		Long retVal = null;
		StringBuilder sb = new StringBuilder();
		sb.append("select count(*) from SmqBaseTarget c where c.impactType = 'NON-IMPACTED'");
	
		EntityManager entityManager = this.cqtEntityManagerFactory.getEntityManager();
		try {
			Query query = entityManager.createQuery(sb.toString());
			retVal = (Long)query.getSingleResult();
		} catch (Exception e) {
			StringBuilder msg = new StringBuilder();
			msg
			.append("An error occurred in findImpactedCount ")
			.append(" Query used was ->")
			.append(sb.toString());
			LOG.error(msg.toString(), e);
		} finally {
			this.cqtEntityManagerFactory.closeEntityManager(entityManager);
		}
		return retVal;
	}

	@Override
	public StreamedContent generateSMQExcel(SmqBaseTarget selectedImpactedSmqList, String dictionaryVersion) {
		XSSFWorkbook workbook = new XSSFWorkbook();
		XSSFSheet worksheet = null;

		worksheet = workbook.createSheet("IA Report");
		XSSFRow row = null;
		int rowCount = 0;
		DateFormat dateFormat = DateFormat.getDateTimeInstance(
		        DateFormat.LONG,
		        DateFormat.LONG, new Locale("EN","en"));

		/**
		 * Première ligne - entêtes
		 */
		row = worksheet.createRow(rowCount);
		XSSFCell cell = row.createCell(0);

		row = worksheet.createRow(rowCount);
		cell = row.createCell(0);
		cell.setCellValue(selectedImpactedSmqList.getSmqName());
		setCellStyleTitre(workbook, cell);

		// Term name
		rowCount += 2;
		row = worksheet.createRow(rowCount);
		cell = row.createCell(0);
		cell.setCellValue("MedDRA Dictionary Version: " + dictionaryVersion);

		rowCount++;
		row = worksheet.createRow(rowCount);
		cell = row.createCell(0);
		cell.setCellValue("Status: " + returnStatus(selectedImpactedSmqList));
		rowCount++;
		row = worksheet.createRow(rowCount);
		cell = row.createCell(0);
		cell.setCellValue("Scope (Yes/No): " + "");
		
		rowCount++;
		row = worksheet.createRow(rowCount);
		cell = row.createCell(0);
		cell.setCellValue("Report Date/Time: " + dateFormat.format(new Date()));
		

		rowCount += 2;
		row = worksheet.createRow(rowCount);
		cell = row.createCell(0);
		cell.setCellValue("Term");
		setCellStyleColumn(workbook, cell);
		cell = row.createCell(1);
		cell.setCellValue("Code");
		setCellStyleColumn(workbook, cell);
		cell = row.createCell(2);
		cell.setCellValue("Level");
		setCellStyleColumn(workbook, cell);
		cell = row.createCell(3);
		cell.setCellValue("Category");
		setCellStyleColumn(workbook, cell);
		cell = row.createCell(4);
		cell.setCellValue("Weight");
		setCellStyleColumn(workbook, cell);
		cell = row.createCell(5);
		cell.setCellValue("Scope");
		setCellStyleColumn(workbook, cell);
		rowCount++;

		// Retrieval of relations - Loop
		List<SmqRelationTarget> relations = findSmqRelationsForSmqCode(selectedImpactedSmqList.getSmqCode());
		
		String level = "", term = "", codeTerm = "";

		if (relations != null) {
			for (SmqRelationTarget relation : relations) {
				/**
				 * 
				 * SMQs
				 */
				if (relation.getSmqCode() != null) {
					List<Long> smqChildCodeList = new ArrayList<>();
					smqChildCodeList.add(relation.getSmqCode());

					SmqBaseTarget smqSearched = findByCode(relation.getSmqCode());
					if (smqSearched != null) {
						List<SmqBaseTarget> smqBaseList = findByLevelAndTerm(smqSearched.getSmqLevel(),	smqSearched.getSmqName());
						if (smqBaseList != null) {
							for (SmqBaseTarget smq : smqBaseList) {
								if (smq.getSmqLevel() == 1) {
									level = "SMQ1";
								} else if (smq.getSmqLevel() == 2) {
									level = "SMQ2";
								} else if (smq.getSmqLevel() == 3) {
									level = "SMQ3";
								} else if (smq.getSmqLevel() == 4) {
									level = "SMQ4";
								} else if (smq.getSmqLevel() == 5) {
									level = "SMQ5";
								}
								row = worksheet.createRow(rowCount);
								buildCells(level, smq.getSmqCode() + "", smq.getSmqName(), cell, row);
								setCellStyleColumn(workbook, cell); 
								rowCount++;

								/**
								 * Other SMQs
								 * 
								 */
								List<SmqBaseTarget> smqs = findChildSmqByParentSmqCodes(smqChildCodeList);
								
								if (smqs != null)
									for (SmqBaseTarget smqC : smqs) {
										if (smqC.getSmqLevel() == 1) {
											level = "SMQ1";
										} else if (smqC.getSmqLevel() == 2) {
											level = "SMQ2";
										} else if (smqC.getSmqLevel() == 3) {
											level = "SMQ3";
										} else if ((smqC.getSmqLevel() == 4)
												|| (smqC.getSmqLevel() == 0)
												|| (smqC.getSmqLevel() == 5)) {
											level = "PT";
										}
										row = worksheet.createRow(rowCount);
										buildChildCells(level, smqC.getSmqCode() + "", smqC.getSmqName(), cell, row, "......");
										setCellStyleColumn(workbook, cell); 
										rowCount++;
										
										if (level.equals("SMQ2")) {						
											smqSearched = findByCode(smqC.getSmqCode());
											if (smqSearched != null) {
												List<SmqRelationTarget> list = findSmqRelationsForSmqCode(smqSearched.getSmqCode());
												if (list != null)
													for (SmqRelationTarget smq3 : list) {
														row = worksheet.createRow(rowCount);
														buildChildCells("PT", smq3.getPtCode() + "", smq3.getPtName(), cell, row, "............");
														setCellStyleColumn(workbook, cell); 
														rowCount++;
													}
											}
											List<Long> codes = new ArrayList<>();
											codes.add(smqC.getSmqCode());
											
											
											//Others relations
											String levelS = "";
											if (smqSearched.getSmqLevel() == 3) {
												levelS = "SMQ3";
											} else if ((smqSearched.getSmqLevel() == 4)
													|| (smqSearched.getSmqLevel() == 0)
													|| (smqSearched.getSmqLevel() == 5)) {
												levelS = "PT";
											}
											List<SmqBaseTarget> smqChildren = findChildSmqByParentSmqCodes(codes);
											if (smqChildren != null)
												for (SmqBaseTarget child : smqChildren) {
													row = worksheet.createRow(rowCount);
													buildChildCells("SMQ3", child.getSmqCode() + "", child.getSmqName(), cell, row, "............");
													setCellStyleColumn(workbook, cell); 
													rowCount++;
													
													smqSearched = findByCode(child.getSmqCode());
													if (smqSearched != null) {
														List<SmqRelationTarget> list = findSmqRelationsForSmqCode(smqSearched.getSmqCode());
														if (list != null)
															for (SmqRelationTarget smq3 : list) {
																row = worksheet.createRow(rowCount);
																buildChildCells("PT", smq3.getPtCode() + "", smq3.getPtName(), cell, row, ".....................");
																setCellStyleColumn(workbook, cell); 
																rowCount++;
															}
													}
												}
											
										}
										
										if (level.equals("SMQ3")) {
											smqSearched = findByCode(smqC.getSmqCode());
											if (smqSearched != null) {
												List<SmqRelationTarget> list = findSmqRelationsForSmqCode(smqSearched.getSmqCode());
												if (list != null)
													for (SmqRelationTarget smq3 : list) {
														row = worksheet.createRow(rowCount);
														buildChildCells("PT", smq3.getPtCode() + "", smq3.getPtName(), cell, row, "...............");
														setCellStyleColumn(workbook, cell); 
														rowCount++;
													}
											}
										}					
									}
							}
						}
 
					}
				}
				
				/**
				 * 
				 * LLT.
				 */
				if (relation.getPtCode() != null) {
					List<Long> lltCodesList = new ArrayList<>();
					lltCodesList.add(Long.valueOf(relation.getPtCode()));
					List<MeddraDictHierarchySearchDto> llts = this.meddraDictService.findByCodes("LLT_", lltCodesList);
					for (MeddraDictHierarchySearchDto llt : llts) {
						row = worksheet.createRow(rowCount);
						buildCells("LLT", llt.getCode() + "", llt.getTerm(), cell, row);
						setCellStyleColumn(workbook, cell); 
						rowCount++;
					}
				}
				
				/**
				 * 
				 * PT
				 */
				if (relation.getPtCode() != null) {
					row = worksheet.createRow(rowCount);
					buildCells("PT", relation.getPtCode() + "", relation.getPtName(), cell, row);
					setCellStyleColumn(workbook, cell); 
					rowCount++;

					/**
					 * LLT.
					 */
					List<MeddraDictHierarchySearchDto> listPT =  meddraDictService.findChildrenByParentCode("LLT_", "PT_", Long.valueOf(relation.getPtCode()));
					List<Long> hlgtCodesList = new ArrayList<>();
					for (MeddraDictHierarchySearchDto meddra : listPT) {
						hlgtCodesList.add(Long.parseLong(meddra.getCode())); 
					}

					List<MeddraDictHierarchySearchDto> llts = this.meddraDictService.findByCodes("LLT_", hlgtCodesList);
					if (llts != null)
						for (MeddraDictHierarchySearchDto llt : llts) {
							row = worksheet.createRow(rowCount);
							buildChildCells("LLT", llt.getCode(), llt.getTerm(), cell, row, "......");
							rowCount++;
						}	
				}
			}
				
			List<SmqBaseTarget> childCmqs = findChildSmqByParentSmqCode(selectedImpactedSmqList.getSmqCode());
			if((null != childCmqs) && (childCmqs.size() > 0)) {
				for (SmqBaseTarget childCmq : childCmqs) {
					level = childCmq.getSmqLevel() + "";
					term = childCmq.getSmqName();
					codeTerm = childCmq.getSmqCode() != null ? childCmq.getSmqCode() + "" : "";

					row = worksheet.createRow(rowCount);
					buildCells(level, codeTerm, term, cell, row);
					rowCount++;
				}
			}
		}
		worksheet.autoSizeColumn(0);
		worksheet.autoSizeColumn(1);
		worksheet.autoSizeColumn(2);
		worksheet.autoSizeColumn(3);
		worksheet.autoSizeColumn(4);
		worksheet.autoSizeColumn(5);

		StreamedContent content = null;
		try {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			workbook.write(baos);
			byte[] xls = baos.toByteArray();
			ByteArrayInputStream bais = new ByteArrayInputStream(xls);
			content = new DefaultStreamedContent(
					bais,
					"application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
					"Impact_Assessment_Report_" + selectedImpactedSmqList.getSmqName()
					+ ".xlsx");
		} catch (IOException e) {
			e.printStackTrace();
		}

		return content;
	}
	
	
	private String returnStatus(SmqBaseTarget smq) {
		String status = "";
		if(CmqBase190.CMQ_STATUS_VALUE_PENDING.equalsIgnoreCase(smq.getSmqStatus())) {
			status = CmqBase190.CMQ_STATUS_DISP_LABEL_PENDING;
		} else if (CmqBase190.CMQ_STATUS_VALUE_ACTIVE.equalsIgnoreCase(smq.getSmqStatus())) {
			status = CmqBase190.CMQ_STATUS_DISP_LABEL_ACTIVE;
		} else if (CmqBase190.CMQ_STATUS_VALUE_INACTIVE.equalsIgnoreCase(smq.getSmqStatus())){
			status = CmqBase190.CMQ_STATUS_DISP_LABEL_INACTIVE;
		} else {
			status = "UNKNOWN";
		}
		return status;
	}
	private void setCellStyleTitre(XSSFWorkbook wb, XSSFCell cell) {
		XSSFCellStyle cellStyle = wb.createCellStyle();

		XSSFFont defaultFont = wb.createFont();
		defaultFont.setFontHeightInPoints((short) 14);
		defaultFont.setFontName("Arial");
		defaultFont.setColor(IndexedColors.BLACK.getIndex());
		defaultFont.setBold(true);
		defaultFont.setItalic(false);

		cellStyle.setFont(defaultFont);
		cell.setCellStyle(cellStyle);
	}

	private void setCellStyleColumn(XSSFWorkbook wb, XSSFCell cell) {
		XSSFCellStyle cellStyle = wb.createCellStyle();
		cellStyle.setFillBackgroundColor(IndexedColors.AQUA.getIndex());
		cellStyle.setFillPattern(XSSFCellStyle.SOLID_FOREGROUND);
		cellStyle.setFillForegroundColor(HSSFColor.GREY_40_PERCENT.index);

		XSSFFont defaultFont = wb.createFont();
		defaultFont.setFontHeightInPoints((short) 12);
		defaultFont.setFontName("Arial");
		defaultFont.setColor(IndexedColors.BLACK.getIndex());
		defaultFont.setBold(true);
		defaultFont.setItalic(false);

		cellStyle.setFont(defaultFont);
		cell.setCellStyle(cellStyle);
	}
	
	private void buildCells(String level, String codeTerm, String term, XSSFCell cell, XSSFRow row) {
		// Cell 0
		cell = row.createCell(0);
		cell.setCellValue(term);

		// Cell 1
		cell = row.createCell(1);
		cell.setCellValue(codeTerm);

		// Cell 2
		cell = row.createCell(2);
		cell.setCellValue(level);
	}
	
	private void buildChildCells(String level, String codeTerm, String term, XSSFCell cell, XSSFRow row, String dots) {
		// Cell 0
		cell = row.createCell(0);
		cell.setCellValue(dots + term);

		// Cell 1
		cell = row.createCell(1);
		cell.setCellValue(codeTerm);

		// Cell 2
		cell = row.createCell(2);
		cell.setCellValue(level);
	}

	public IMeddraDictTargetService getMeddraDictService() {
		return meddraDictService;
	}

	public void setMeddraDictService(IMeddraDictTargetService meddraDictService) {
		this.meddraDictService = meddraDictService;
	}
}
