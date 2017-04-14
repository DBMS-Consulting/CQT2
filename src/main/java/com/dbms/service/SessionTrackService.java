package com.dbms.service;

import java.util.Date;

import javax.faces.bean.ApplicationScoped;
import javax.faces.bean.ManagedBean;
import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.persistence.Query;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dbms.entity.cqt.SessionTrack;
import com.dbms.service.base.CqtPersistenceService;

@ManagedBean(name = "SessionTrackService")
@ApplicationScoped
public class SessionTrackService extends CqtPersistenceService<SessionTrack> implements ISessionTrackService {

	private static final Logger LOG = LoggerFactory.getLogger(SessionTrackService.class);

	@Override
	public SessionTrack addLoginSessionTrack(String adCn, String adFirstName, String adLastName, String adEmail,
			String adGroupList, String javaSessionGuid) {
		SessionTrack sessionTrack = new SessionTrack();
		sessionTrack.setAdCn(adCn);
		sessionTrack.setAdFirstName(adFirstName);
		sessionTrack.setAdLastName(adLastName);
		sessionTrack.setAdEmail(adEmail);
		sessionTrack.setAdGroupList(adGroupList);
		sessionTrack.setJavaSessionGuid(javaSessionGuid);
		sessionTrack.setLogonTime(new Date());
		sessionTrack.setActiveSession("YES");

		String oracleSidQueryString = "SELECT SYS_CONTEXT('USERENV','SESSIONID') FROM DUAL";
		String oracleSessionIdQueryString = "SELECT SYS_CONTEXT('USERENV','SID') FROM DUAL";

		EntityManager entityManager = this.cqtEntityManagerFactory.getEntityManager();
		EntityTransaction transaction = entityManager.getTransaction();
		try {
			transaction.begin();
			Query oracleSidQuery = entityManager.createNativeQuery(oracleSidQueryString);
			String oracleSid = (String) oracleSidQuery.getSingleResult();

			Query oracleSessionIdQuery = entityManager.createNativeQuery(oracleSessionIdQueryString);
			String oracleSessionId = (String) oracleSessionIdQuery.getSingleResult();

			sessionTrack.setOracleSid(oracleSid);
			sessionTrack.setOracleSessionId(oracleSessionId);

			entityManager.persist(sessionTrack);
			transaction.commit();
		} catch (Exception e) {
			if ((transaction != null) && transaction.isActive()){
				transaction.rollback();
			}
			StringBuilder msg = new StringBuilder();
			msg.append("addLoginSessionTrack failed for SessionTrack ").append(sessionTrack);
			LOG.error(msg.toString(), e);
		} finally {
			this.cqtEntityManagerFactory.closeEntityManager(entityManager);
		}
		return sessionTrack;
	}
	
	@Override
	public SessionTrack updateLogoutInSessionTrack(Long sessionTrackId) {
		SessionTrack sessionTrack = null;

		EntityManager entityManager = this.cqtEntityManagerFactory.getEntityManager();
		EntityTransaction transaction = entityManager.getTransaction();
		try {
			transaction.begin();
			sessionTrack = entityManager.find(SessionTrack.class, sessionTrackId);
			if(null != sessionTrack) {
				sessionTrack.setLogoffTime(new Date());
				sessionTrack.setActiveSession("NO");
				entityManager.persist(sessionTrack);
			}
			transaction.commit();
		} catch (Exception e) {
			if ((transaction != null) && transaction.isActive()){
				transaction.rollback();
			}
			StringBuilder msg = new StringBuilder();
			msg.append("updateLogoutInSessionTrack failed for sessionTrackId:").append(sessionTrackId);
			LOG.error(msg.toString(), e);
		} finally {
			this.cqtEntityManagerFactory.closeEntityManager(entityManager);
		}
		return sessionTrack;
	}

}
