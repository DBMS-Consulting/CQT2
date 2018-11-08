package com.dbms.service;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.util.List;

import javax.faces.bean.ApplicationScoped;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.persistence.EntityManager;
import javax.persistence.Query;

import org.apache.commons.lang3.StringUtils;
import org.hibernate.CacheMode;
import org.hibernate.SQLQuery;
import org.hibernate.Session;
import org.hibernate.transform.Transformers;
import org.hibernate.type.StandardBasicTypes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dbms.entity.cqt.CmqParentChild200;
import com.dbms.entity.cqt.dtos.HistoricalViewDbDataDTO;
import com.dbms.entity.cqt.dtos.ParentChildAuditDBDataDTO;
import com.dbms.util.ICqtEntityManagerFactory;

@ManagedBean(name = "HistoricalViewService")
@ApplicationScoped
public class HistoricalViewService implements IHistoricalViewService {
	private static final Logger LOG = LoggerFactory.getLogger(HistoricalViewService.class);

	@ManagedProperty(value = "#{CqtEntityManagerFactory}")
	private ICqtEntityManagerFactory cqtEntityManagerFactory;

	@SuppressWarnings("unchecked")
	@Override
	public List<HistoricalViewDbDataDTO> findByCriterias(String listCode, String dictionaryVersion,
			String auditTimeStampString) {
		List<HistoricalViewDbDataDTO> retVal = null;
		InputStream sqlInputStream = this.getClass().getClassLoader().getResourceAsStream("historical-view.sql");
		if (null != sqlInputStream) {
			EntityManager entityManager = this.cqtEntityManagerFactory.getEntityManager();
			Session session = entityManager.unwrap(Session.class);
			String queryString = null;
			try {
				BufferedReader buf = new BufferedReader(new InputStreamReader(sqlInputStream));
				String line = buf.readLine() + System.lineSeparator();
				StringBuilder sqlStringBuilder = new StringBuilder();
				while (line != null) {
					sqlStringBuilder.append(line).append(System.lineSeparator());
					line = buf.readLine();
				}

				queryString = sqlStringBuilder.toString();
				queryString = StringUtils.replaceAll(queryString, "&&MedDRAAuditVersion\\.", dictionaryVersion);
				queryString = StringUtils.replaceAll(queryString, "&&MedDRAAuditVersion", dictionaryVersion);
				queryString = StringUtils.replaceAll(queryString, "&&CMQCodeForAudit", listCode);
				if (!StringUtils.isBlank(auditTimeStampString)) {
					DateTimeFormatter formatter = new DateTimeFormatterBuilder().parseCaseInsensitive().appendPattern("dd-MMM-yyyy:hh:mm:ss a z").toFormatter();
					LocalDateTime date = LocalDateTime.parse(auditTimeStampString, formatter);
					DateTimeFormatter formatter2 = new DateTimeFormatterBuilder().parseCaseInsensitive().appendPattern("dd-MMM-yyyy:HH:mm:ss").toFormatter();
					queryString = StringUtils.replaceAll(queryString, "&&CMQAuditTimestamp", date.format(formatter2));
				}
				
				LOG.info("Historical view using query -> " + queryString);

				SQLQuery query = session.createSQLQuery(queryString);
				query.addScalar("cmqCode", StandardBasicTypes.LONG);
				query.addScalar("listName", StandardBasicTypes.STRING);
				query.addScalar("listType", StandardBasicTypes.STRING);
				query.addScalar("product", StandardBasicTypes.STRING);
				query.addScalar("drugProgram", StandardBasicTypes.STRING);
				query.addScalar("protocolNumber", StandardBasicTypes.STRING);
				query.addScalar("listLevel", StandardBasicTypes.STRING);
				query.addScalar("parentListName", StandardBasicTypes.STRING);
				query.addScalar("status", StandardBasicTypes.STRING);
				query.addScalar("state", StandardBasicTypes.STRING);
				query.addScalar("creationDate", StandardBasicTypes.STRING);
				query.addScalar("createdBy", StandardBasicTypes.STRING);
				query.addScalar("algorithm", StandardBasicTypes.STRING);
				query.addScalar("lastActivationDate", StandardBasicTypes.STRING);
				query.addScalar("lastActivationBy", StandardBasicTypes.STRING);
				query.addScalar("description", StandardBasicTypes.STRING);
				query.addScalar("notes", StandardBasicTypes.STRING);
				query.addScalar("source", StandardBasicTypes.STRING);
				query.addScalar("term", StandardBasicTypes.STRING);
				query.addScalar("termDictLevel", StandardBasicTypes.STRING);
				query.addScalar("termCode", StandardBasicTypes.LONG);
				query.addScalar("termScope", StandardBasicTypes.STRING);
				query.addScalar("dictionaryVersion", StandardBasicTypes.STRING);
				query.addScalar("designee", StandardBasicTypes.STRING);
				query.addScalar("designee2", StandardBasicTypes.STRING);
				query.addScalar("designee3", StandardBasicTypes.STRING);
				query.addScalar("medicalConcept", StandardBasicTypes.STRING);
				
				if (!StringUtils.isBlank(auditTimeStampString)) {
					DateTimeFormatter formatter = new DateTimeFormatterBuilder().parseCaseInsensitive().appendPattern("dd-MMM-yyyy:hh:mm:ss a z").toFormatter();
					 LocalDateTime date = LocalDateTime.parse(auditTimeStampString, formatter);
					 Timestamp timestamp = Timestamp.valueOf(date);
					 query.setParameter("CMQAuditTimestamp", timestamp);
					 
				}

				// query.setFetchSize(1000);
				query.setResultTransformer(Transformers.aliasToBean(HistoricalViewDbDataDTO.class));
				query.setCacheable(false);
				query.setCacheMode(CacheMode.IGNORE);
				retVal = query.list();
			} catch (Exception e) {
				StringBuilder msg = new StringBuilder();
				msg.append("An error occurred while fetching findByCriterias on HistoricalViewService")
						.append(" Query used was ->").append(queryString);
				LOG.error(msg.toString(), e);
			} finally {
				this.cqtEntityManagerFactory.closeEntityManager(entityManager);
			}
		} else {
			StringBuilder msg = new StringBuilder();
			msg.append("An error occurred while reading historical-view.sql from classpath.");
			LOG.error(msg.toString());
		}
		return retVal;
	}
	
	public List<ParentChildAuditDBDataDTO> findHistoricalParentsByCmqId(Long childCmqId, String auditTimeStampString) {
		List<ParentChildAuditDBDataDTO> retVal = null;
		StringBuilder sb = new StringBuilder();
		sb.append("select transaction_type as transactionType,cmq_parent_code_old as cmqParentCodeOld,cmq_parent_code_new as cmqParentCodeNew,cmq_child_code_old as cmqChildCodeOld,cmq_child_code_new as cmqChildCodeNew,parent_cmq_id as parentCmqId,child_cmq_id as childCmqId from cmq_parent_child_current_audit where child_cmq_id = :childCmqId and audit_timestamp <= :auditTimeStampString order by audit_timestamp asc");
		EntityManager entityManager = this.cqtEntityManagerFactory.getEntityManager();
		Session session = entityManager.unwrap(Session.class);
		try {
			SQLQuery query = session.createSQLQuery(sb.toString());
			query.addScalar("transactionType", StandardBasicTypes.STRING);
			query.addScalar("cmqParentCodeOld", StandardBasicTypes.LONG);
			query.addScalar("cmqParentCodeNew", StandardBasicTypes.LONG);
			query.addScalar("cmqChildCodeOld", StandardBasicTypes.LONG);
			query.addScalar("cmqChildCodeNew", StandardBasicTypes.LONG);
			query.addScalar("parentCmqId", StandardBasicTypes.LONG);
			query.addScalar("childCmqId", StandardBasicTypes.LONG);
			query.setParameter("childCmqId", childCmqId);
			if (!StringUtils.isBlank(auditTimeStampString)) {
				DateTimeFormatter formatter = new DateTimeFormatterBuilder().parseCaseInsensitive().appendPattern("dd-MMM-yyyy:hh:mm:ss a z").toFormatter();
				 LocalDateTime date = LocalDateTime.parse(auditTimeStampString, formatter);
				 Timestamp timestamp = Timestamp.valueOf(date);
				 query.setParameter("auditTimeStampString", timestamp);
			}
			query.setResultTransformer(Transformers.aliasToBean(ParentChildAuditDBDataDTO.class));
			retVal = query.list();
		} catch (Exception e) {
			StringBuilder msg = new StringBuilder();
			msg
					.append("An error occurred while fetching HistoricalParentsByCmqCode ")
					.append(" Query used was ->")
					.append(sb.toString());
			LOG.error(msg.toString(), e);
		} finally {
			this.cqtEntityManagerFactory.closeEntityManager(entityManager);
		}
		return retVal;
	}
	
	public List<ParentChildAuditDBDataDTO> findHistoricalChildsByCmqId(Long parentCmqId, String auditTimeStampString) {
		List<ParentChildAuditDBDataDTO> retVal = null;
		StringBuilder sb = new StringBuilder();
		sb.append("select transaction_type as transactionType,cmq_parent_code_old as cmqParentCodeOld,cmq_parent_code_new as cmqParentCodeNew,cmq_child_code_old as cmqChildCodeOld,cmq_child_code_new as cmqChildCodeNew,parent_cmq_id as parentCmqId,child_cmq_id as childCmqId from cmq_parent_child_current_audit where parent_cmq_id = :parentCmqId and audit_timestamp <= :auditTimeStampString order by audit_timestamp asc");
		EntityManager entityManager = this.cqtEntityManagerFactory.getEntityManager();
		Session session = entityManager.unwrap(Session.class);
		try {
			SQLQuery query = session.createSQLQuery(sb.toString());
			query.addScalar("transactionType", StandardBasicTypes.STRING);
			query.addScalar("cmqParentCodeOld", StandardBasicTypes.LONG);
			query.addScalar("cmqParentCodeNew", StandardBasicTypes.LONG);
			query.addScalar("cmqChildCodeOld", StandardBasicTypes.LONG);
			query.addScalar("cmqChildCodeNew", StandardBasicTypes.LONG);
			query.addScalar("parentCmqId", StandardBasicTypes.LONG);
			query.addScalar("childCmqId", StandardBasicTypes.LONG);
			query.setParameter("parentCmqId", parentCmqId);
			if (!StringUtils.isBlank(auditTimeStampString)) {
				DateTimeFormatter formatter = new DateTimeFormatterBuilder().parseCaseInsensitive().appendPattern("dd-MMM-yyyy:hh:mm:ss a z").toFormatter();
				 LocalDateTime date = LocalDateTime.parse(auditTimeStampString, formatter);
				 Timestamp timestamp = Timestamp.valueOf(date);
				 query.setParameter("auditTimeStampString", timestamp);
			}
			query.setResultTransformer(Transformers.aliasToBean(ParentChildAuditDBDataDTO.class));
			retVal = query.list();
		} catch (Exception e) {
			StringBuilder msg = new StringBuilder();
			msg
					.append("An error occurred while fetching HistoricalParentsByCmqCode ")
					.append(" Query used was ->")
					.append(sb.toString());
			LOG.error(msg.toString(), e);
		} finally {
			this.cqtEntityManagerFactory.closeEntityManager(entityManager);
		}
		return retVal;
	}

	public ICqtEntityManagerFactory getCqtEntityManagerFactory() {
		return cqtEntityManagerFactory;
	}

	public void setCqtEntityManagerFactory(ICqtEntityManagerFactory cqtEntityManagerFactory) {
		this.cqtEntityManagerFactory = cqtEntityManagerFactory;
	}

}
