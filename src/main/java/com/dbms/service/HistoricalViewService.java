package com.dbms.service;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.util.List;

import javax.faces.bean.ApplicationScoped;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.persistence.EntityManager;

import org.apache.commons.lang3.StringUtils;
import org.hibernate.SQLQuery;
import org.hibernate.Session;
import org.hibernate.transform.Transformers;
import org.hibernate.type.StandardBasicTypes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dbms.entity.cqt.dtos.HistoricalViewDbDataDTO;
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
				query.addScalar("creationDate", StandardBasicTypes.DATE);
				query.addScalar("createdBy", StandardBasicTypes.STRING);
				query.addScalar("algorithm", StandardBasicTypes.STRING);
				query.addScalar("lastActivationDate", StandardBasicTypes.DATE);
				query.addScalar("lastActivationBy", StandardBasicTypes.STRING);
				query.addScalar("description", StandardBasicTypes.STRING);
				query.addScalar("term", StandardBasicTypes.STRING);
				query.addScalar("termDictLevel", StandardBasicTypes.STRING);
				query.addScalar("termCode", StandardBasicTypes.LONG);
				query.addScalar("termScope", StandardBasicTypes.STRING);
				query.addScalar("dictionaryVersion", StandardBasicTypes.STRING);
				query.addScalar("designee", StandardBasicTypes.STRING);
				query.addScalar("designee2", StandardBasicTypes.STRING);
				query.addScalar("designee3", StandardBasicTypes.STRING);
				query.addScalar("medicalConcept", StandardBasicTypes.STRING);
				
				// query.setFetchSize(1000);
				query.setResultTransformer(Transformers.aliasToBean(HistoricalViewDbDataDTO.class));
				query.setCacheable(false);
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

	public ICqtEntityManagerFactory getCqtEntityManagerFactory() {
		return cqtEntityManagerFactory;
	}

	public void setCqtEntityManagerFactory(ICqtEntityManagerFactory cqtEntityManagerFactory) {
		this.cqtEntityManagerFactory = cqtEntityManagerFactory;
	}

}
