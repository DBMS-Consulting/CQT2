package com.dbms.web.dto;

import java.util.HashMap;
import java.util.Map;

import com.dbms.entity.cqt.dtos.ReportLineDataDto;

public class MQReportRelationsWorkerDTO {
	private boolean success;
	private String workerName;
	private Map<Integer, ReportLineDataDto> mapReport;

	public MQReportRelationsWorkerDTO() {
		this.mapReport = new HashMap<Integer, ReportLineDataDto>();
	}

	public Map<Integer, ReportLineDataDto> getMapReport() {
		return mapReport;
	}

	public void addToMapReport(Integer key, ReportLineDataDto value) {
		this.mapReport.put(key, value);
	}

	public boolean isSuccess() {
		return success;
	}

	public void setSuccess(boolean success) {
		this.success = success;
	}

	public String getWorkerName() {
		return workerName;
	}

	public void setWorkerName(String workerName) {
		this.workerName = workerName;
	}

}
