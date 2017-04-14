package com.dbms.entity.cqt;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import com.dbms.entity.BaseEntity;

@Entity
@Table(name = "SESSION_TRACK")
public class SessionTrack extends BaseEntity {

	private static final long serialVersionUID = -7329937227203589928L;

	@Id
	@GeneratedValue(generator = "SESSION_TRACK_ID_SEQ", strategy = GenerationType.SEQUENCE)
	@SequenceGenerator(name = "SESSION_TRACK_ID_SEQ", sequenceName = "SESSION_TRACK_ID_SEQ", allocationSize = 1)
	@Column(name = "SESSION_TRACK_ID", unique = true, nullable = false)
	private Long sessionTrackId;

	@Column(name = "ORACLE_SID", nullable = true, length = 256)
	private String oracleSid;

	@Column(name = "ORACLE_SESSION_ID", nullable = true, length = 256)
	private String oracleSessionId;

	@Column(name = "JAVA_SESSION_GUID", nullable = true, length = 100)
	private String javaSessionGuid;

	@Column(name = "AD_CN", nullable = true, length = 500)
	private String adCn;

	@Column(name = "AD_FIRSTNAME", nullable = true, length = 500)
	private String adFirstName;

	@Column(name = "AD_EMAIL", nullable = true, length = 500)
	private String adEmail;

	@Column(name = "AD_LASTNAME", nullable = true, length = 500)
	private String adLastName;

	@Column(name = "AD_GROUP_LIST", nullable = true, length = 4000)
	private String adGroupList;

	@Temporal(TemporalType.DATE)
	@Column(name = "LOGON_TIME", length = 7, nullable = true)
	private Date logonTime;

	@Column(name = "ACTIVE_SESSION", nullable = true, length = 100)
	private String activeSession;

	@Temporal(TemporalType.DATE)
	@Column(name = "LOGOFF_TIME", length = 7, nullable = true)
	private Date logoffTime;

	public Long getId() {
		return this.getSessionTrackId();
	}

	public Long getSessionTrackId() {
		return sessionTrackId;
	}

	public void setSessionTrackId(Long sessionTrackId) {
		this.sessionTrackId = sessionTrackId;
	}

	public String getOracleSid() {
		return oracleSid;
	}

	public void setOracleSid(String oracleSid) {
		this.oracleSid = oracleSid;
	}

	public String getOracleSessionId() {
		return oracleSessionId;
	}

	public void setOracleSessionId(String oracleSessionId) {
		this.oracleSessionId = oracleSessionId;
	}

	public String getJavaSessionGuid() {
		return javaSessionGuid;
	}

	public void setJavaSessionGuid(String javaSessionGuid) {
		this.javaSessionGuid = javaSessionGuid;
	}

	public String getAdCn() {
		return adCn;
	}

	public void setAdCn(String adCn) {
		this.adCn = adCn;
	}

	public String getAdFirstName() {
		return adFirstName;
	}

	public void setAdFirstName(String adFirstName) {
		this.adFirstName = adFirstName;
	}

	public String getAdEmail() {
		return adEmail;
	}

	public void setAdEmail(String adEmail) {
		this.adEmail = adEmail;
	}

	public String getAdLastName() {
		return adLastName;
	}

	public void setAdLastName(String adLastName) {
		this.adLastName = adLastName;
	}

	public String getAdGroupList() {
		return adGroupList;
	}

	public void setAdGroupList(String adGroupList) {
		this.adGroupList = adGroupList;
	}

	public Date getLogonTime() {
		return logonTime;
	}

	public void setLogonTime(Date logonTime) {
		this.logonTime = logonTime;
	}

	public String getActiveSession() {
		return activeSession;
	}

	public void setActiveSession(String activeSession) {
		this.activeSession = activeSession;
	}

	public Date getLogoffTime() {
		return logoffTime;
	}

	public void setLogoffTime(Date logoffTime) {
		this.logoffTime = logoffTime;
	}

	@Override
	public String toString() {
		return "SessionTrack [sessionTrackId=" + sessionTrackId + ", oracleSid=" + oracleSid + ", oracleSessionId="
				+ oracleSessionId + ", javaSessionGuid=" + javaSessionGuid + ", adCn=" + adCn + ", adFirstName="
				+ adFirstName + ", adEmail=" + adEmail + ", adLastName=" + adLastName + ", adGroupList=" + adGroupList
				+ ", logonTime=" + logonTime + ", activeSession=" + activeSession + ", logoffTime=" + logoffTime + "]";
	}
}