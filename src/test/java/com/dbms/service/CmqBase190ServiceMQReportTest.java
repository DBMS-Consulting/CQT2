package com.dbms.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;

import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.Test;

import com.dbms.entity.cqt.dtos.ReportLineDataDto;
import com.dbms.view.SystemConfigProperties;

/**
 * Tests for the row writer behind the Relations MQ Detailed Report.
 */
public class CmqBase190ServiceMQReportTest {

	/** An .xlsx workbook can hold at most 64000 cell styles. */
	private static final int XLSX_MAX_CELL_STYLES = 64000;

	@Test
	public void fillReportWritesLargeNestedReportWithoutExhaustingCellStyles() throws Exception {
		XSSFWorkbook workbook = new XSSFWorkbook();
		XSSFSheet worksheet = workbook.createSheet("MQ Report");
		int stylesBefore = workbook.getNumCellStyles();

		// More top-level terms with children than the .xlsx cell style limit.
		int parentCount = XLSX_MAX_CELL_STYLES + 1;
		List<ReportLineDataDto> report = new ArrayList<>(parentCount);
		for (int i = 0; i < parentCount; i++) {
			ReportLineDataDto parent = new ReportLineDataDto(9L, "PT", "P" + i, "Parent " + i, "");
			parent.getChildren().add(new ReportLineDataDto(10L, "LLT", "L" + i, "Child " + i, "....."));
			report.add(parent);
		}

		int lastRow = invokeFillReport(report, worksheet, 0);

		assertEquals(parentCount * 2, lastRow);
		assertEquals("one shared highlight style for the whole report", stylesBefore + 1, workbook.getNumCellStyles());
		workbook.close();
	}

	@Test
	public void fillReportHighlightsTopLevelRowsOnlyAndNestsChildren() throws Exception {
		XSSFWorkbook workbook = new XSSFWorkbook();
		XSSFSheet worksheet = workbook.createSheet("MQ Report");

		List<ReportLineDataDto> report = new ArrayList<>();
		ReportLineDataDto first = new ReportLineDataDto(8L, "HLT", "100", "First HLT", "");
		ReportLineDataDto pt = new ReportLineDataDto(9L, "PT", "200", "Some PT", "....");
		pt.getChildren().add(new ReportLineDataDto(10L, "LLT", "300", "Some LLT", "....."));
		first.getChildren().add(pt);
		report.add(first);
		ReportLineDataDto second = new ReportLineDataDto(9L, "PT", "400", "Second PT", "");
		second.getChildren().add(new ReportLineDataDto(10L, "LLT", "500", "Other LLT", "....."));
		report.add(second);
		report.add(new ReportLineDataDto(6L, "SOC", "600", "Childless SOC", ""));

		int lastRow = invokeFillReport(report, worksheet, 3);

		assertEquals(3 + 6, lastRow);
		String[][] expected = {
				{ "First HLT", "100", "HLT" },
				{ "....Some PT", "200", "PT" },
				{ ".....Some LLT", "300", "LLT" },
				{ "Second PT", "400", "PT" },
				{ ".....Other LLT", "500", "LLT" },
				{ "Childless SOC", "600", "SOC" } };
		boolean[] highlighted = { true, false, false, true, false, true };

		CellStyle highlight = null;
		for (int i = 0; i < expected.length; i++) {
			XSSFRow row = worksheet.getRow(3 + i);
			assertNotNull("row " + i, row);
			for (int c = 0; c < 3; c++) {
				assertEquals("row " + i + " cell " + c, expected[i][c], row.getCell(c).getStringCellValue());
			}
			CellStyle style = row.getCell(0).getCellStyle();
			if (highlighted[i]) {
				assertEquals(IndexedColors.PALE_BLUE.index, style.getFillForegroundColor());
				assertEquals(FillPatternType.SOLID_FOREGROUND, style.getFillPatternEnum());
				if (highlight == null) {
					highlight = style;
				} else {
					assertSame("top-level rows share one style", highlight.getIndex(), style.getIndex());
				}
			} else {
				assertEquals(FillPatternType.NO_FILL, style.getFillPatternEnum());
			}
		}
		assertNull(worksheet.getRow(3 + expected.length));
		workbook.close();
	}

	@Test
	public void fillReportWithEmptyReportWritesNothing() throws Exception {
		XSSFWorkbook workbook = new XSSFWorkbook();
		XSSFSheet worksheet = workbook.createSheet("MQ Report");

		int lastRow = invokeFillReport(new ArrayList<ReportLineDataDto>(), worksheet, 7);

		assertEquals(7, lastRow);
		assertEquals(0, worksheet.getPhysicalNumberOfRows());
		assertTrue(workbook.getNumCellStyles() <= 2);
		workbook.close();
	}

	private static int invokeFillReport(List<ReportLineDataDto> report, XSSFSheet worksheet, int rowCount) throws Exception {
		Method fillReport = CmqBase190Service.class.getDeclaredMethod("fillReport", List.class, XSSFCell.class,
				XSSFRow.class, int.class, XSSFSheet.class, SystemConfigProperties.class);
		fillReport.setAccessible(true);
		try {
			return (Integer) fillReport.invoke(new CmqBase190Service(), report, null, null, rowCount, worksheet,
					allColumnsHidden());
		} catch (InvocationTargetException e) {
			if (e.getCause() instanceof Exception) {
				throw (Exception) e.getCause();
			}
			throw e;
		}
	}

	/** System configuration with the optional Scope/Category/Category2/Weight columns switched off. */
	private static SystemConfigProperties allColumnsHidden() {
		IRefCodeListService refCodeListService = (IRefCodeListService) Proxy.newProxyInstance(
				IRefCodeListService.class.getClassLoader(), new Class<?>[] { IRefCodeListService.class },
				(proxy, method, args) -> method.getReturnType() == boolean.class ? Boolean.FALSE : null);
		return new SystemConfigProperties(refCodeListService);
	}
}
