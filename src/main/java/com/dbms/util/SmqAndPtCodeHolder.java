package com.dbms.util;

public class SmqAndPtCodeHolder {
	private Long smqCode;
	private long ptCode;

	public SmqAndPtCodeHolder(Long smqCode, long ptCode) {
		super();
		this.smqCode = smqCode;
		this.ptCode = ptCode;
	}

	public Long getSmqCode() {
		return smqCode;
	}

	public void setSmqCode(Long smqCode) {
		this.smqCode = smqCode;
	}

	public long getPtCode() {
		return ptCode;
	}

	public void setPtCode(long ptCode) {
		this.ptCode = ptCode;
	}

}
