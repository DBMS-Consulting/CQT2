package com.dbms.controller;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.ViewScoped;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;

import org.apache.commons.lang3.RandomStringUtils;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.primefaces.model.StreamedContent;
import org.primefaces.model.TreeNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dbms.entity.cqt.CmqBase190;
import com.dbms.entity.cqt.RefConfigCodeList;
import com.dbms.service.ICmqBase190Service;
import com.dbms.service.ICmqRelation190Service;
import com.dbms.service.IMeddraDictService;
import com.dbms.service.IRefCodeListService;
import com.dbms.service.ISmqBaseService;
import com.dbms.util.CqtConstants;
import com.dbms.view.ListDetailsFormModel;

/**
 * @date Feb 7, 2017 7:39:34 AM
 **/
@ManagedBean
@ViewScoped
public class ReportController extends BaseController<CmqBase190> {

	private static final long serialVersionUID = 52332344344651662L;

	private static final Logger log = LoggerFactory.getLogger(ReportController.class);

	@ManagedProperty("#{CmqBase190Service}")
	private ICmqBase190Service cmqBaseService;

	@ManagedProperty("#{SmqBaseService}")
	private ISmqBaseService smqBaseService;

	@ManagedProperty("#{MeddraDictService}")
	private IMeddraDictService meddraDictService;

	@ManagedProperty("#{CmqRelation190Service}")
	private ICmqRelation190Service cmqRelationService;

	@ManagedProperty("#{RefCodeListService}")
	private IRefCodeListService refCodeListService;
	
	// Search & Filters
	private Date reportStartDate = null;
	private Date reportEndDate = null;
	private ReportType genReportType = null;
	private ReportFormat genReportFormat = null;
	
	private StreamedContent excelFile;

	public ReportController() {
	}

	@PostConstruct
	public void init() {
	}
	
	@Override
	public String search() {
		// TODO Auto-generated method stub
		return null;
	}
	
	public HSSFRow getOrCreateHSSFRow(HSSFSheet sheet, int rowIdx) {
		HSSFRow row = sheet.getRow(rowIdx);
        if(row == null)
        	row = sheet.createRow(rowIdx);
        return row;
	}
	
	public HSSFCell getOrCreateHSSFCell(HSSFRow row, int cellNum) {
		HSSFCell cell = row.getCell(cellNum);
		if(cell == null)
			cell = row.createCell(cellNum);
		return cell;
	}
	
	public void generateReport() throws IOException {
		FacesContext fc = FacesContext.getCurrentInstance();
		ExternalContext ec = fc.getExternalContext();
	    
		File tempFile = null, templateFile = null;
	    String outputFileName = null;
	        
	    try {
	    
		    if(genReportType == ReportType.GEN_LIST_DETAILS) {

		    	// TODO: generate report data using filter
		    	List<CmqBase190> reportData = cmqBaseService.getPublishedListsReportData(reportStartDate, reportEndDate);
		    	if(!reportData.isEmpty()) {
		    		switch(genReportFormat) {
		    		case XLS: 
		    			outputFileName = "list-details-report.xls";
		    			templateFile = new File(ec.getRealPath("/WEB-INF/report_templates/CQT-Reports-Generate List Details-List Details.xls"));
		    			tempFile = File.createTempFile(RandomStringUtils.randomAlphabetic(5), ".xls");
		    			tempFile.deleteOnExit();

		    			// Read workbook from template file
			    		HSSFWorkbook wb = new HSSFWorkbook(new FileInputStream(templateFile));
			            HSSFSheet sheet = wb.getSheetAt(0);
			            int rowIdx = 5; // this 0-based index of the first data row
			            
			            // Create a row and put some cells in it. Rows are 0 based.
			            HSSFRow row;
			            String datetimeStr = new SimpleDateFormat("d-MMM-yyyy h:mm a z").format(new Date());
			            
			            // Write summary cells
			            getOrCreateHSSFCell(getOrCreateHSSFRow(sheet, 2), 0).setCellValue("Report Date/Time: " + datetimeStr);
			            getOrCreateHSSFCell(getOrCreateHSSFRow(sheet, 3), 0).setCellValue("Total: " + reportData.size());
			            
			            // Write data cells
				    	for(CmqBase190 dr: reportData) {
				    		row = getOrCreateHSSFRow(sheet, rowIdx);
				            getOrCreateHSSFCell(row, 0).setCellValue(dr.getCmqCode());
				            getOrCreateHSSFCell(row, 1).setCellValue(dr.getCmqName());
				            getOrCreateHSSFCell(row, 2).setCellValue(refCodeListService.interpretInternalCodeToValue(CqtConstants.CODE_LIST_TYPE_EXTENSION, dr.getCmqTypeCd()));
				            getOrCreateHSSFCell(row, 3).setCellValue(refCodeListService.interpretInternalCodeToValue(CqtConstants.CODE_LIST_TYPE_PROGRAM, dr.getCmqProgramCd()));
				            getOrCreateHSSFCell(row, 4).setCellValue(refCodeListService.interpretInternalCodeToValue(CqtConstants.CODE_LIST_TYPE_PROTOCOL, dr.getCmqProtocolCd()));
				            getOrCreateHSSFCell(row, 5).setCellValue(refCodeListService.interpretInternalCodeToValue(CqtConstants.CODE_LIST_TYPE_PRODUCT, dr.getCmqProductCd()));
				            getOrCreateHSSFCell(row, 6).setCellValue(dr.getCmqLevel());
				            getOrCreateHSSFCell(row, 7).setCellValue(dr.getDictionaryVersion());
				            getOrCreateHSSFCell(row, 8).setCellValue(dr.getCmqStatus());
				            getOrCreateHSSFCell(row, 9).setCellValue(dr.getCmqAlgorithm());
				            getOrCreateHSSFCell(row, 10).setCellValue(dr.getCmqGroup());
				            rowIdx ++;
				    	}
				    	
				    	// Write back to the temp file
				    	wb.write(new FileOutputStream(tempFile));
				    	break;
		    		case PDF:
		    			break;
		    		}
		    	}
		    }
		    
		    if(tempFile != null) {
			    int contentLength = (int) tempFile.length();
			    String contentType = ec.getMimeType(outputFileName);
			    
			    ec.responseReset(); // Some JSF component library or some Filter might have set some headers in the buffer beforehand. We want to get rid of them, else it may collide.
			    ec.setResponseContentType(contentType); // Check http://www.iana.org/assignments/media-types for all types. Use if necessary ExternalContext#getMimeType() for auto-detection based on filename.
			    ec.setResponseContentLength(contentLength); // Set it with the file size. This header is optional. It will work if it's omitted, but the download progress will be unknown.
			    ec.setResponseHeader("Content-Disposition", "attachment; filename=\"" + outputFileName + "\""); // The Save As popup magic is done here. You can give it any file name you want, this only won't work in MSIE, it will use current request URL as file name instead.
		
			    OutputStream output = ec.getResponseOutputStream();
			    Files.copy(tempFile.toPath(), output);
		
			    fc.responseComplete(); // Important! Otherwise JSF will attempt to render the response which obviously will fail since it's already written with a file and closed.
			    // erase temp file
			    tempFile.delete();
		    } else {
		    	fc.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_WARN, "The report type or format not supported yet!", ""));
		    }
	    } catch(Exception e) {
			fc.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "There was an error while generating the report!", ""));
	    }
	}
	
	/**
	 * Generate Excel report on relations tab.
	 */
	public void generateExcelReport(ListDetailsFormModel details, TreeNode relationsRoot) {
		RefConfigCodeList currentMeddraVersionCodeList = this.refCodeListService.getCurrentMeddraVersion();
		StreamedContent content = cmqBaseService.generateExcelReport(details, (currentMeddraVersionCodeList != null ? currentMeddraVersionCodeList.getValue() : ""), relationsRoot);
		setExcelFile(content); 
	}


	//-------------------------- Getters and Setters -------------------------------
	
	public ICmqBase190Service getCmqBaseService() {
		return cmqBaseService;
	}

	public void setCmqBaseService(ICmqBase190Service cmqBaseService) {
		this.cmqBaseService = cmqBaseService;
	}

	public ICmqRelation190Service getCmqRelationService() {
		return cmqRelationService;
	}

	public void setCmqRelationService(ICmqRelation190Service cmqRelationService) {
		this.cmqRelationService = cmqRelationService;
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

	public IRefCodeListService getRefCodeListService() {
		return refCodeListService;
	}

	public void setMeddraDictService(IMeddraDictService meddraDictService) {
		this.meddraDictService = meddraDictService;
	}
	
	public void setRefCodeListService(IRefCodeListService refCodeListService) {
		this.refCodeListService = refCodeListService;
	}
	
	public ReportType[] getReportTypes() {
		return ReportType.values();
	}
	public ReportFormat[] getReportFormats() {
		return ReportFormat.values();
	}

	public Date getReportStartDate() {
		return reportStartDate;
	}

	public void setReportStartDate(Date reportStartDate) {
		this.reportStartDate = reportStartDate;
	}

	public Date getReportEndDate() {
		return reportEndDate;
	}

	public void setReportEndDate(Date reportEndDate) {
		this.reportEndDate = reportEndDate;
	}

	public ReportType getGenReportType() {
		return genReportType;
	}

	public void setGenReportType(ReportType genReportType) {
		this.genReportType = genReportType;
	}
	
	public ReportFormat getGenReportFormat() {
		return genReportFormat;
	}

	public void setGenReportFormat(ReportFormat genReportFormat) {
		this.genReportFormat= genReportFormat;
	}


	//---------------------- child classes -----------------------------------
	public enum ReportType {

		GEN_LIST_DETAILS("Generate List Details");

	    private String label;

	    private ReportType(String label) {
	        this.label = label;
	    }

	    public String getLabel() {
	        return label;
	    }
	}
	
	public enum ReportFormat {

		XLS("Excel (xls)"),
		PDF("PDF");

	    private String label;

	    private ReportFormat(String label) {
	        this.label = label;
	    }

	    public String getLabel() {
	        return label;
	    }
	}

	public StreamedContent getExcelFile() {
		return excelFile;
	}

	public void setExcelFile(StreamedContent excelFile) {
		this.excelFile = excelFile;
	}
}
