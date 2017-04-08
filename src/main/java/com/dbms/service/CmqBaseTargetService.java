package com.dbms.service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
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
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

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
import com.dbms.entity.cqt.CmqRelation190;
import com.dbms.entity.cqt.CmqRelationTarget;
import com.dbms.entity.cqt.SmqBase190;
import com.dbms.entity.cqt.SmqRelation190;
import com.dbms.entity.cqt.dtos.MeddraDictHierarchySearchDto;
import com.dbms.service.base.CqtPersistenceService;

/**
 * @author Jay G.(jayshanchn@hotmail.com)
 * @date Feb 11, 2017 8:00:48 AM
 **/
@ManagedBean(name = "CmqBaseTargetService")
@ApplicationScoped
public class CmqBaseTargetService extends CqtPersistenceService<CmqBaseTarget> implements ICmqBaseTargetService {

	private static final Logger LOG = LoggerFactory
			.getLogger(CmqBaseTargetService.class);
	
	@ManagedProperty("#{CmqRelationTargetService}")
	private ICmqRelationTargetService cmqRelationTargetService;

	@ManagedProperty("#{SmqBaseService}")
	private ISmqBaseService smqBaseService;

	@ManagedProperty("#{MeddraDictService}")
	private IMeddraDictService meddraDictService;

	/* (non-Javadoc)
	 * @see com.dbms.service.ICmqBaseTargetService#findImpactedWithPaginated(int, int, java.lang.String, org.primefaces.model.SortOrder, java.util.Map)
	 */
	@Override
	public List<CmqBaseTarget> findImpactedWithPaginated(int first, int pageSize, String sortField
														, SortOrder sortOrder, Map<String, Object> filters) {
		List<CmqBaseTarget> retVal = null;
		EntityManager entityManager = this.cqtEntityManagerFactory.getEntityManager();
		try {
			CriteriaBuilder cb = entityManager.getCriteriaBuilder();
			CriteriaQuery<CmqBaseTarget> cq = cb.createQuery(CmqBaseTarget.class);
			Root<CmqBaseTarget> cmqRoot = cq.from(CmqBaseTarget.class);
			
			List<Predicate> pred = new ArrayList<Predicate>();
			
			pred.add(cb.or(cb.equal(cmqRoot.get("impactType"), "IMPACTED"), 
					cb.equal(cmqRoot.get("impactType"), "ICC")
			));
			
			if(filters.containsKey("cmqName") && filters.get("cmqName") != null)
				pred.add(cb.like(cb.lower(cmqRoot.<String>get("cmqName")), "%" + ((String)filters.get("cmqName")).toLowerCase() + "%"));
			
			if(filters.containsKey("cmqTypeCd") && filters.get("cmqTypeCd") != null)
				pred.add(cb.equal(cmqRoot.get("cmqTypeCd"), filters.get("cmqTypeCd")));
			
			if(filters.containsKey("smqLevel") && filters.get("smqLevel") != null)
				pred.add(cb.equal(cmqRoot.get("cmqLevel"), filters.get("smqLevel")));
			
			cq.where(cb.and(pred.toArray(new Predicate[0])));
			cq.orderBy(cb.asc(cmqRoot.get("cmqName")));
			
			TypedQuery<CmqBaseTarget> tq = entityManager.createQuery(cq);
			
			if (pageSize >= 0){
	            tq.setMaxResults(pageSize);
	        }
	        if (first >= 0){
	            tq.setFirstResult(first);
	        }
			
			retVal = tq.getResultList();
		} catch (Exception e) {
			StringBuilder msg = new StringBuilder();
			msg.append("An error occurred while fetching paginated impacted CmqBaseTarget.");
			LOG.error(msg.toString(), e);
		} finally {
			this.cqtEntityManagerFactory.closeEntityManager(entityManager);
		}
		return retVal;
	}
	
	/* (non-Javadoc)
	 * @see com.dbms.service.ICmqBaseTargetService#findImpactedCount()
	 */
	@Override
	public Long findImpactedCount() {
		Long retVal = null;
		StringBuilder sb = new StringBuilder();
		sb.append("select count(*) from CmqBaseTarget c where c.impactType = 'IMPACTED' or c.impactType = 'ICC'");
		
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
	 * @see com.dbms.service.ICmqBaseTargetService#findNotImpactedWithPaginated(int, int, java.lang.String, org.primefaces.model.SortOrder, java.util.Map)
	 */
	@Override
	public List<CmqBaseTarget> findNotImpactedWithPaginated(int first, int pageSize, String sortField
			, SortOrder sortOrder, Map<String, Object> filters) {
		List<CmqBaseTarget> retVal = null;
		EntityManager entityManager = this.cqtEntityManagerFactory.getEntityManager();
		try {
			CriteriaBuilder cb = entityManager.getCriteriaBuilder();
			CriteriaQuery<CmqBaseTarget> cq = cb.createQuery(CmqBaseTarget.class);
			Root<CmqBaseTarget> cmqRoot = cq.from(CmqBaseTarget.class);
			List<Predicate> pred = new ArrayList<Predicate>();
			
			pred.add(cb.equal(cmqRoot.get("impactType"), "NON-IMPACTED"));
			
			if(filters.containsKey("cmqName") && filters.get("cmqName") != null)
				pred.add(cb.like(cb.lower(cmqRoot.<String>get("cmqName")), "%" + ((String)filters.get("cmqName")).toLowerCase() + "%"));
			
			if(filters.containsKey("cmqTypeCd") && filters.get("cmqTypeCd") != null)
				pred.add(cb.equal(cmqRoot.get("cmqTypeCd"), filters.get("cmqTypeCd")));
			
			if(filters.containsKey("smqLevel") && filters.get("smqLevel") != null)
				pred.add(cb.equal(cmqRoot.get("cmqLevel"), filters.get("smqLevel")));
			
			cq.where(cb.and(pred.toArray(new Predicate[0])));
			
			cq.orderBy(cb.asc(cmqRoot.get("cmqName")));
			TypedQuery<CmqBaseTarget> tq = entityManager.createQuery(cq);
			
			if (pageSize >= 0){
			tq.setMaxResults(pageSize);
			}
			if (first >= 0){
			tq.setFirstResult(first);
			}
			
			retVal = tq.getResultList();
		} catch (Exception e) {
			StringBuilder msg = new StringBuilder();
			msg.append("An error occurred while fetching paginated impacted CmqBaseTarget.");
			LOG.error(msg.toString(), e);
		} finally {
			this.cqtEntityManagerFactory.closeEntityManager(entityManager);
		}
		return retVal;
	}

	/* (non-Javadoc)
	 * @see com.dbms.service.ICmqBaseTargetService#findNotImpactedCount()
	 */
	@Override
	public Long findNotImpactedCount() {
		Long retVal = null;
		StringBuilder sb = new StringBuilder();
		sb.append("select count(*) from CmqBaseTarget c where c.impactType = 'NON-IMPACTED'");
	
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
	public CmqBaseTarget findByCode(Long cmqCode) {
		CmqBaseTarget retVal = null;
		String queryString = "from CmqBaseTarget c where c.cmqCode = :cmqCode";
		EntityManager entityManager = this.cqtEntityManagerFactory
				.getEntityManager();
		try {
			Query query = entityManager.createQuery(queryString);
			query.setParameter("cmqCode", cmqCode);
			retVal = (CmqBaseTarget) query.getSingleResult();
		} catch (Exception e) {
			StringBuilder msg = new StringBuilder();
			msg.append("findByCode failed for CMQ_CODE value'").append(cmqCode)
					.append("' ").append("Query used was ->")
					.append(queryString);
			LOG.error(msg.toString(), e);
		} finally {
			this.cqtEntityManagerFactory.closeEntityManager(entityManager);
		}
		return retVal;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public List<Map<String, Object>> findCmqChildCountForParentCmqCode(
			List<Long> cmqCodes) {
		List<Map<String, Object>> retVal = null;
		StringBuilder sb = new StringBuilder();
		sb.append("select CMQ_CODE, count(*) as COUNT from CMQ_BASE_TARGET where CMQ_PARENT_CODE in :cmqCodes group by CMQ_CODE");

		EntityManager entityManager = this.cqtEntityManagerFactory
				.getEntityManager();
		Session session = entityManager.unwrap(Session.class);
		try {
			SQLQuery query = session.createSQLQuery(sb.toString());
			query.addScalar("CMQ_CODE", StandardBasicTypes.LONG);
			query.addScalar("COUNT", StandardBasicTypes.LONG);
			query.setParameterList("cmqCodes", cmqCodes);
			query.setResultTransformer(Transformers.ALIAS_TO_ENTITY_MAP);
			retVal = query.list();
		} catch (Exception e) {
			StringBuilder msg = new StringBuilder();
			msg.append(
					"An error occurred while findCmqChildCountForParentCmqCode ")
					.append(cmqCodes).append(" Query used was ->")
					.append(sb.toString());
			LOG.error(msg.toString(), e);
		} finally {
			this.cqtEntityManagerFactory.closeEntityManager(entityManager);
		}
		return retVal;
	}

	@Override
	public Long findCmqChildCountForParentCmqCode(Long cmqCode) {
		Long retVal = null;
		StringBuilder sb = new StringBuilder();
		sb.append("select count(*) from CmqBaseTarget c where c.cmqParentCode = :cmqCode");

		EntityManager entityManager = this.cqtEntityManagerFactory
				.getEntityManager();
		try {
			Query query = entityManager.createQuery(sb.toString());
			query.setParameter("cmqCode", cmqCode);
			retVal = (Long) query.getSingleResult();
		} catch (Exception e) {
			StringBuilder msg = new StringBuilder();
			msg.append("An error occurred while findCmqChildCountForCmqCode ")
					.append(cmqCode).append(" Query used was ->")
					.append(sb.toString());
			LOG.error(msg.toString(), e);
		} finally {
			this.cqtEntityManagerFactory.closeEntityManager(entityManager);
		}
		return retVal;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public List<CmqBaseTarget> findChildCmqsByParentCode(Long code) {
		List<CmqBaseTarget> retVal = null;
		String queryString = "from CmqBaseTarget c where c.cmqParentCode = :codeList ";
		EntityManager entityManager = this.cqtEntityManagerFactory
				.getEntityManager();
		try {
			Query query = entityManager.createQuery(queryString);
			query.setParameter("codeList", code);
			retVal = query.getResultList();
		} catch (Exception e) {
			StringBuilder msg = new StringBuilder();
			msg.append("findChildCmqsByCodes failed ")
					.append("Query used was ->").append(queryString);
			LOG.error(msg.toString(), e);
		} finally {
			this.cqtEntityManagerFactory.closeEntityManager(entityManager);
		}
		return retVal;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public List<CmqBaseTarget> findByLevelAndTerm(Integer level, String searchTerm) {
		List<CmqBaseTarget> retVal = null;
		StringBuilder sb = new StringBuilder();
		sb.append("from CmqBaseTarget c where c.cmqLevel = :cmqLevel and c.cmqParentCode is null and c.cmqStatus = 'I' ");
		if (!StringUtils.isBlank(searchTerm)) {
			sb.append("and upper(c.cmqName) like :cmqName");
		}
		EntityManager entityManager = this.cqtEntityManagerFactory
				.getEntityManager();
		try {
			Query query = entityManager.createQuery(sb.toString());
			query.setParameter("cmqLevel", level);

			if (!StringUtils.isBlank(searchTerm)) {
				query.setParameter("cmqName", searchTerm.toUpperCase());
			}
			retVal = query.getResultList();
		} catch (Exception e) {
			StringBuilder msg = new StringBuilder();
			msg.append(
					"An error occurred while fetching types from CmqBaseTarget on cmqLevel ")
					.append(level).append(" with cmqName like ")
					.append("%" + searchTerm.toUpperCase() + "%")
					.append(" Query used was ->").append(sb.toString());
			LOG.error(msg.toString(), e);
		} finally {
			this.cqtEntityManagerFactory.closeEntityManager(entityManager);
		}
		return retVal;
	}

	@Override
	public StreamedContent generateCMQExcel(CmqBaseTarget selectedImpactedCmqList, String dictionaryVersion) {
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
		cell.setCellValue(selectedImpactedCmqList.getCmqName());
		setCellStyleTitre(workbook, cell);

		// Term name
		rowCount += 2;
		row = worksheet.createRow(rowCount);
		cell = row.createCell(0);
		cell.setCellValue("MedDRA Dictionary Version: " + dictionaryVersion);

		rowCount++;
		row = worksheet.createRow(rowCount);
		cell = row.createCell(0);
		cell.setCellValue("Status: " + returnStatus(selectedImpactedCmqList));
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
		List<CmqRelationTarget> relations = cmqRelationTargetService.findByCmqCode(selectedImpactedCmqList.getCmqCode());

		String level = "", term = "", codeTerm = "";

		if (relations != null) {
			for (CmqRelationTarget relation : relations) {
				/**
				 * 
				 * SMQs
				 */
				if (relation.getSmqCode() != null) {
					List<Long> smqChildCodeList = new ArrayList<>();
					smqChildCodeList.add(relation.getSmqCode());

					SmqBase190 smqSearched = smqBaseService.findByCode(relation.getSmqCode());
					if (smqSearched != null) {
						List<SmqBase190> smqBaseList = smqBaseService
								.findByLevelAndTerm(smqSearched.getSmqLevel(),
										smqSearched.getSmqName());
						if (smqBaseList != null) {
							for (SmqBase190 smq : smqBaseList) {
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
								buildCells(level, smq.getSmqCode() + "",
										smq.getSmqName(), cell, row);
								setCellStyleColumn(workbook, cell);
								rowCount++;

								/**
								 * Other SMQs
								 * 
								 */
								List<SmqBase190> smqs = smqBaseService
										.findChildSmqByParentSmqCodes(smqChildCodeList);
								if (smqs != null)
									for (SmqBase190 smqC : smqs) {
										if (smqC.getSmqLevel() == 1) {
											level = "SMQ1";
										} else if (smqC.getSmqLevel() == 2) {
											level = "SMQ2";
										} else if (smqC.getSmqLevel() == 3) {
											level = "SMQ3";
										} else if (smqC.getSmqLevel() == 4) {
											level = "SMQ4";
										} else if (smqC.getSmqLevel() == 5) {
											level = "SMQ5";
										}
										row = worksheet.createRow(rowCount);
										buildChildCells(level,
												smqC.getSmqCode() + "",
												smqC.getSmqName(), cell, row,
												"......");
										rowCount++;
									}
							}
						}

						List<SmqRelation190> childRelations = this.smqBaseService
								.findSmqRelationsForSmqCode(smqSearched
										.getSmqCode());

						if (null != childRelations) {
							System.out
							.println("\n ******************** childRelations " + childRelations.size()	+ ", for " + smqSearched.getSmqCode());
							for (SmqRelation190 childRelation : childRelations) {
								if (childRelation.getSmqLevel() == 1) {
									level = "SMQ1";
								} else if (childRelation.getSmqLevel() == 2) {
									level = "SMQ2";
								} else if (childRelation.getSmqLevel() == 3) {
									level = "SMQ3";
								} else if ((childRelation.getSmqLevel() == 4)
										|| (childRelation.getSmqLevel() == 0)
										|| (childRelation.getSmqLevel() == 5)) {
									level = "PT";
								}

								row = worksheet.createRow(rowCount);
								buildChildCells(level, childRelation.getPtCode() + "", childRelation.getPtName(), cell, row, "......");
								rowCount++;
							}


						}
					}

				}

				/**
				 * 
				 * HLT.
				 */
				if (relation.getHltCode() != null) {
					List<Long> hltCodesList = new ArrayList<>();
					hltCodesList.add(relation.getHltCode());
					List<MeddraDictHierarchySearchDto> hlts = this.meddraDictService.findByCodes("HLT_", hltCodesList);
					for (MeddraDictHierarchySearchDto hlt : hlts) {
						row = worksheet.createRow(rowCount);
						buildCells("HLT", hlt.getCode(), hlt.getTerm(), cell, row);
						setCellStyleColumn(workbook, cell); 
						rowCount++;

						/**
						 * PT.
						 */
						List<MeddraDictHierarchySearchDto> listPT =  meddraDictService.findChildrenByParentCode("PT_", "HLT_", Long.valueOf(hlt.getCode()));
						List<Long> ptCodesList = new ArrayList<>();
						for (MeddraDictHierarchySearchDto meddra : listPT) {
							ptCodesList.add(Long.parseLong(meddra.getCode())); 
						}

						List<MeddraDictHierarchySearchDto> llts = this.meddraDictService.findByCodes("PT_", ptCodesList);
						if (llts != null)
							for (MeddraDictHierarchySearchDto llt : llts) {
								row = worksheet.createRow(rowCount);
								buildChildCells("PT", llt.getCode(), llt.getTerm(), cell, row, "......");
								rowCount++;
							}
					}
				}

				/**
				 * 
				 * PT
				 */
				if (relation.getPtCode() != null) {
					List<Long> ptCodesList = new ArrayList<>();
					ptCodesList.add(relation.getPtCode());
					List<MeddraDictHierarchySearchDto> pts = this.meddraDictService.findByCodes("PT_", ptCodesList);
					for (MeddraDictHierarchySearchDto pt : pts) {
						row = worksheet.createRow(rowCount);
						buildCells("PT", pt.getCode(), pt.getTerm(), cell, row);
						setCellStyleColumn(workbook, cell); 
						rowCount++;

						/**
						 * LLT.
						 */
						List<MeddraDictHierarchySearchDto> listPT =  meddraDictService.findChildrenByParentCode("LLT_", "PT_", Long.valueOf(pt.getCode()));
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

				/**
				 * 
				 * SOC
				 */
				if (relation.getSocCode() != null) {
					List<Long> socCodesList = new ArrayList<>();
					socCodesList.add(relation.getSocCode());
					List<MeddraDictHierarchySearchDto> socss = this.meddraDictService.findByCodes("SOC_", socCodesList);
					for (MeddraDictHierarchySearchDto soc : socss) {
						row = worksheet.createRow(rowCount);
						buildCells("SOC", soc.getCode(), soc.getTerm(), cell, row);
						setCellStyleColumn(workbook, cell); 
						rowCount++;

						/**
						 * HLGT.
						 */
						List<MeddraDictHierarchySearchDto> listHLGT =  meddraDictService.findChildrenByParentCode("HLGT_", "SOC_", Long.valueOf(soc.getCode()));
						List<Long> hlgtCodesList = new ArrayList<>();
						for (MeddraDictHierarchySearchDto meddra : listHLGT) {
							hlgtCodesList.add(Long.parseLong(meddra.getCode())); 
						}

						List<MeddraDictHierarchySearchDto> hlgts = this.meddraDictService.findByCodes("HLGT_", hlgtCodesList);
						if (hlgts != null)
							for (MeddraDictHierarchySearchDto hlgt : hlgts) {
								row = worksheet.createRow(rowCount);
								buildChildCells("HLGT", hlgt.getCode(), hlgt.getTerm(), cell, row, "......");
								rowCount++;

								/**
								 * HLT.
								 */
								List<MeddraDictHierarchySearchDto> listHLT =  meddraDictService.findChildrenByParentCode("HLT_", "HLGT_", Long.valueOf(hlgt.getCode()));
								List<Long> hltCodesList = new ArrayList<>();
								for (MeddraDictHierarchySearchDto meddra : listHLT) {
									hltCodesList.add(Long.parseLong(meddra.getCode())); 
								}

								List<MeddraDictHierarchySearchDto> hlts = this.meddraDictService.findByCodes("HLT_", hltCodesList);
								if (hlts != null)
									for (MeddraDictHierarchySearchDto hlt : hlts) {
										row = worksheet.createRow(rowCount);
										buildChildCells("HLT", hlt.getCode(), hlt.getTerm(), cell, row, "...............");
										rowCount++;

										/**
										 * PT.
										 */
										List<MeddraDictHierarchySearchDto> listHT =  meddraDictService.findChildrenByParentCode("PT_", "HLT_", Long.valueOf(hlt.getCode()));
										List<Long> ptCodesList = new ArrayList<>();
										for (MeddraDictHierarchySearchDto meddra : listHT) {
											ptCodesList.add(Long.parseLong(meddra.getCode())); 
										}

										List<MeddraDictHierarchySearchDto> pts = this.meddraDictService.findByCodes("PT_", ptCodesList);
										if (pts != null)
											for (MeddraDictHierarchySearchDto pt : pts) {
												row = worksheet.createRow(rowCount);
												buildChildCells("PT", pt.getCode(), pt.getTerm(), cell, row, "..............");
												rowCount++;
											}
									}
							}
					}
				}

				/**
				 * 
				 * HLGT.
				 */
				if (relation.getHlgtCode() != null) {
					List<Long> hlgtCodesList = new ArrayList<>();
					hlgtCodesList.add(relation.getHlgtCode());
					List<MeddraDictHierarchySearchDto> socDtos = this.meddraDictService.findByCodes("HLGT_", hlgtCodesList);
					for (MeddraDictHierarchySearchDto hlgt : socDtos) {
						row = worksheet.createRow(rowCount);
						buildCells("HLGT", hlgt.getCode(), hlgt.getTerm(), cell, row);
						setCellStyleColumn(workbook, cell); 
						rowCount++;

						/**
						 * HLT.
						 */
						List<MeddraDictHierarchySearchDto> listHLGT =  meddraDictService.findChildrenByParentCode("HLT_", "HLGT_", Long.valueOf(hlgt.getCode()));
						List<Long> hltCodesList = new ArrayList<>();
						for (MeddraDictHierarchySearchDto meddra : listHLGT) {
							hltCodesList.add(Long.parseLong(meddra.getCode())); 
						}

						List<MeddraDictHierarchySearchDto> hlts = this.meddraDictService.findByCodes("HLT_", hltCodesList);
						if (hlts != null)
							for (MeddraDictHierarchySearchDto hlt : hlts) {
								row = worksheet.createRow(rowCount);
								buildChildCells("HLT", hlt.getCode(), hlt.getTerm(), cell, row, "......");
								rowCount++;

								/**
								 * PT.
								 */
								List<MeddraDictHierarchySearchDto> listHT =  meddraDictService.findChildrenByParentCode("PT_", "HLT_", Long.valueOf(hlt.getCode()));
								List<Long> ptCodesList = new ArrayList<>();
								for (MeddraDictHierarchySearchDto meddra : listHT) {
									ptCodesList.add(Long.parseLong(meddra.getCode())); 
								}

								List<MeddraDictHierarchySearchDto> pts = this.meddraDictService.findByCodes("PT_", ptCodesList);
								if (pts != null)
									for (MeddraDictHierarchySearchDto pt : pts) {
										row = worksheet.createRow(rowCount);
										buildChildCells("PT", pt.getCode(), pt.getTerm(), cell, row, "..............");
										rowCount++;
									}
							}
					}
				}
			}
			List<CmqBaseTarget> childCmqs = findChildCmqsByParentCode(selectedImpactedCmqList.getCmqCode());
			if((null != childCmqs) && (childCmqs.size() > 0)) {
				for (CmqBaseTarget childCmq : childCmqs) {
					level = childCmq.getCmqTypeCd();
					term = childCmq.getCmqName();
					codeTerm = childCmq.getCmqCode() != null ? childCmq.getCmqCode() + "" : "";

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
					"Impact_Assessment_Report_" + selectedImpactedCmqList.getCmqName()
					+ ".xlsx");
		} catch (IOException e) {
			e.printStackTrace();
		}

		return content;
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


	private void buildCells(String level, String codeTerm, String term, CmqRelation190 relation, XSSFCell cell, XSSFRow row) {
		// Cell 0
		cell = row.createCell(0);
		cell.setCellValue(term);
	
		// Cell 1
		cell = row.createCell(1);
		cell.setCellValue(codeTerm);

		// Cell 2
		cell = row.createCell(2);
		cell.setCellValue(level);

		// Cell 3
		cell = row.createCell(3);
		cell.setCellValue(relation.getTermCategory() != null ? relation
				.getTermCategory() : "");

		// Cell 4
		cell = row.createCell(4);
		cell.setCellValue(relation.getTermWeight() != null ? relation
				.getTermWeight().toString() : "");

		// Cell 5
		cell = row.createCell(5);
		cell.setCellValue(relation.getTermScope() != null ? relation
				.getTermScope() : "");
		
	}
	
	private String returnStatus(CmqBaseTarget cmq) {
		String status = "";
		if(CmqBase190.CMQ_STATUS_VALUE_PENDING.equalsIgnoreCase(cmq.getCmqStatus())) {
			status = CmqBase190.CMQ_STATUS_DISP_LABEL_PENDING;
		} else if (CmqBase190.CMQ_STATUS_VALUE_ACTIVE.equalsIgnoreCase(cmq.getCmqStatus())) {
			status = CmqBase190.CMQ_STATUS_DISP_LABEL_ACTIVE;
		} else if (CmqBase190.CMQ_STATUS_VALUE_INACTIVE.equalsIgnoreCase(cmq.getCmqStatus())){
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
	
	/**
	 * Find Approved IA CmqTarget.
	 * @return List<CmqBaseTarget>
	 */
	@Override
	public List<CmqBaseTarget> findApprovedCmqs() {
		List<CmqBaseTarget> retVal = null;
		String queryString = "from CmqBaseTarget c where upper(c.cmqState) = upper('Approved IA') and c.cmqStatus = 'P' ";
		EntityManager entityManager = this.cqtEntityManagerFactory.getEntityManager();
		try {
			Query query = entityManager.createQuery(queryString);
			retVal = query.getResultList();
		} catch (Exception e) {
			StringBuilder msg = new StringBuilder();
			msg.append("findApprovedCmqs failed ").append("Query used was ->")
					.append(queryString);
			LOG.error(msg.toString(), e);
		} finally {
			this.cqtEntityManagerFactory.closeEntityManager(entityManager);
		}
		return retVal;

	}
	
	@Override
	public List<CmqBaseTarget> findChildCmqsByCodes(List<Long> codes) {
		List<CmqBaseTarget> retVal = null;
		String queryString = "from CmqBaseTarget c where c.cmqParentCode in :codeList ";
		EntityManager entityManager = this.cqtEntityManagerFactory
				.getEntityManager();
		try {
			Query query = entityManager.createQuery(queryString);
			query.setParameter("codeList", codes);
			retVal = query.getResultList();
		} catch (Exception e) {
			StringBuilder msg = new StringBuilder();
			msg.append("findChildCmqsByCodes failed ")
					.append("Query used was ->").append(queryString);
			LOG.error(msg.toString(), e);
		} finally {
			this.cqtEntityManagerFactory.closeEntityManager(entityManager);
		}
		return retVal;
	}
	
	@Override
	public List<CmqBaseTarget> findParentCmqsByCodes(List<Long> codes) {
		List<CmqBaseTarget> retVal = null;
		String queryString = "from CmqBaseTarget c where c.cmqCode in :codeList ";
		EntityManager entityManager = this.cqtEntityManagerFactory
				.getEntityManager();
		try {
			Query query = entityManager.createQuery(queryString);
			query.setParameter("codeList", codes);
			retVal = query.getResultList();
		} catch (Exception e) {
			StringBuilder msg = new StringBuilder();
			msg.append("findParentCmqsByCodes failed ")
					.append("Query used was ->").append(queryString);
			LOG.error(msg.toString(), e);
		} finally {
			this.cqtEntityManagerFactory.closeEntityManager(entityManager);
		}
		return retVal;
	}
	
	@SuppressWarnings("unchecked")
	public List<CmqBaseTarget> findPublishedCmqs() {
		List<CmqBaseTarget> retVal = null;
		String queryString = "from CmqBaseTarget c where upper(c.cmqState) = upper('PUBLISHED IA') and c.cmqStatus = 'P' ";
		EntityManager entityManager = this.cqtEntityManagerFactory
				.getEntityManager();
		try {
			Query query = entityManager.createQuery(queryString);
			retVal = query.getResultList();
		} catch (Exception e) {
			StringBuilder msg = new StringBuilder();
			msg.append("findApprovedCmqs failed ").append("Query used was ->")
					.append(queryString);
			LOG.error(msg.toString(), e);
		} finally {
			this.cqtEntityManagerFactory.closeEntityManager(entityManager);
		}
		return retVal;
	}

	public ICmqRelationTargetService getCmqRelationTargetService() {
		return cmqRelationTargetService;
	}

	public void setCmqRelationTargetService(
			ICmqRelationTargetService cmqRelationTargetService) {
		this.cmqRelationTargetService = cmqRelationTargetService;
	}

	public ISmqBaseService getSmqBaseService() {
		return smqBaseService;
	}

	public void setSmqBaseService(ISmqBaseService smqBaseService) {
		this.smqBaseService = smqBaseService;
	}

	public IMeddraDictService getMeddraDictService() {
		return meddraDictService;
	}

	public void setMeddraDictService(IMeddraDictService meddraDictService) {
		this.meddraDictService = meddraDictService;
	}
}
