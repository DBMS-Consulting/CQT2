package com.dbms.service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

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
			queryString.append(" order by a.serialNum ");
			queryString.append(orderBy.name());
			try {
				Query query = entityManager.createQuery(queryString.toString());
				query.setParameter("codelistConfigType", codelistConfigType);

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
	public RefConfigCodeList findByConfigTypeAndInternalCode(String configType, String internalCode) {
		RefConfigCodeList ref = null;
		String queryString = "from RefConfigCodeList a where a.codelistConfigType = :codelistConfigType and a.codelistInternalValue = :codelistInternalValue";

		EntityManager entityManager = this.cqtEntityManagerFactory
				.getEntityManager();
		try {
			Query query = entityManager.createQuery(queryString);
			query.setParameter("codelistConfigType", configType);
			query.setParameter("codelistInternalValue", internalCode);
			ref = (RefConfigCodeList) query.getSingleResult();
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
		if (ref != null)
			return ref;
		return null;
	}

	@Override
	public String findCodeByInternalCode(String codelistInternalValue) {
		RefConfigCodeList ref = null;
		String queryString = "from RefConfigCodeList c where c.codelistInternalValue = :codelistInternalValue";
		EntityManager entityManager = this.cqtEntityManagerFactory
				.getEntityManager();
		try {
			Query query = entityManager.createQuery(queryString);
			query.setParameter("codelistInternalValue", codelistInternalValue);
			ref = (RefConfigCodeList) query.getSingleResult();
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
	public String interpretInternalCodeToValue(String configType,
			String internalCode) {
		List<RefConfigCodeList> codeList = findByConfigType(configType, OrderBy.ASC);
		
		if (codeList != null) {
			for(RefConfigCodeList c : codeList) {
				if(c.getCodelistInternalValue().equals(internalCode)) {
					return c.getValue();
				}
			}	
		}
		return internalCode;
	}
	
	@Override
	public String[] interpretProductCodesToValues(List<CmqProductBaseCurrent> products)
	{
		if(products != null && products.size() > 0) {
			String[] pv = new String[products.size()];
			for(int i=0; i<products.size(); i++) {
				pv[i] = interpretInternalCodeToValue(CqtConstants.CODE_LIST_TYPE_PRODUCT, products.get(i).getCmqProductCd());
			}
			return pv;
		} else {
			return new String[0];
		}
	}
	
	@Override
	public String interpretProductCodesToValuesLabel(List<CmqProductBaseCurrent> products) {
		return StringUtils.join(this.interpretProductCodesToValues(products), ", ");
	}
	
	@Override
	public String convertProductCodesToValuesLabel(List<CmqProductBaseCurrent> products) {
		return StringUtils.join(this.interpretProductCodesToValues(products), ", ");
	}
	
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
	public void create(RefConfigCodeList e) throws CqtServiceException {
		super.create(e);
		cqtCacheManager.removeAllFromCache(CACHE_NAME);
	}
	
	@Override
	public RefConfigCodeList update(RefConfigCodeList e) throws CqtServiceException {
		RefConfigCodeList u = super.update(e);
		cqtCacheManager.removeAllFromCache(CACHE_NAME);
		return u;
	}
	
	@Override
	public void update(List<RefConfigCodeList> e) throws CqtServiceException {
		super.update(e);
		cqtCacheManager.removeAllFromCache(CACHE_NAME);
	}

	public ICqtCacheManager getCqtCacheManager() {
		return cqtCacheManager;
	}

	public void setCqtCacheManager(ICqtCacheManager cqtCacheManager) {
		this.cqtCacheManager = cqtCacheManager;
	}

	@Override
	public StreamedContent generateReport(String codelistType) {
		XSSFWorkbook workbook = new XSSFWorkbook();
		XSSFSheet worksheet = null;

		Calendar cal = Calendar.getInstance();

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
		cell.setCellValue(cal.get(Calendar.DATE) + "-"
				+ cal.get(Calendar.MONTH) + "-" + cal.get(Calendar.YEAR));
		rowCount += 2;

		row = worksheet.createRow(rowCount);
		cell = row.createCell(0);
		cell.setCellValue("Internal Value");
		cell = row.createCell(1);
		cell.setCellValue("Value");
		cell = row.createCell(2);
		cell.setCellValue("Active");
		rowCount += 2;

		// Retrieval of ConfigList - Loop
		List<RefConfigCodeList> list = findAllByConfigType(codelistType,
				OrderBy.ASC);
		for (RefConfigCodeList ref : list) {
			row = worksheet.createRow(rowCount);
			// Cell 0
			cell = row.createCell(0);
			cell.setCellValue(ref.getCodelistInternalValue());

			// Cell 1
			cell = row.createCell(1);
			cell.setCellValue(ref.getValue());

			// Cell 2
			cell = row.createCell(2);
			cell.setCellValue(ref.getActiveFlag());

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
		final FileInputStream stream = new FileInputStream(ec.getRealPath("/image/logo.png"));
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
}
