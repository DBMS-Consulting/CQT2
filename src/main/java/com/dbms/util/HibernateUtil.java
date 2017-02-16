package com.dbms.util;

import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dbms.entity.cqt.CmqBase190;
import com.dbms.entity.cqt.CmqRelation190;
import com.dbms.entity.cqt.ExtentionConfigCodeList;
import com.dbms.entity.cqt.MeddraDict190;
import com.dbms.entity.cqt.MeddraDict191;
import com.dbms.entity.cqt.ProductConfigCodeList;
import com.dbms.entity.cqt.ProgramConfigCodeList;
import com.dbms.entity.cqt.ProtocolConfigCodeList;
import com.dbms.entity.cqt.SmqBase190;
import com.dbms.entity.cqt.SmqRelation190;

/**
 * @author Jay G.(jayshanchn@hotmail.com)
 * @date Feb 10, 2017 7:10:58 AM
 **/
public class HibernateUtil {
	private static final Logger log = LoggerFactory
			.getLogger(HibernateUtil.class);
	private static final ThreadLocal<Session> session = new ThreadLocal<Session>();

	private static final SessionFactory sessionFactory;
	static {
		try {
			final Configuration configuration = new Configuration();
			configuration.addAnnotatedClass(CmqBase190.class)
					.addAnnotatedClass(CmqRelation190.class)
					.addAnnotatedClass(ExtentionConfigCodeList.class)
					.addAnnotatedClass(MeddraDict190.class)
					.addAnnotatedClass(MeddraDict191.class)
					.addAnnotatedClass(ProductConfigCodeList.class)
					.addAnnotatedClass(ProgramConfigCodeList.class)
					.addAnnotatedClass(ProtocolConfigCodeList.class)
					.addAnnotatedClass(SmqBase190.class)
					.addAnnotatedClass(SmqRelation190.class);
			configuration.setProperty("hibernate.dialect", "org.hibernate.dialect.OracleDialect");
			configuration.setProperty("hibernate.connection.driver_class", "oracle.jdbc.driver.OracleDriver");
			configuration.setProperty("hibernate.connection.url", "jdbc:oracle:thin:@192.196.245.20:79/ort501");
			configuration.setProperty("hibernate.connection.username", "opencqt");
			configuration.setProperty("hibernate.connection.password", "opencqt" );
			configuration.setProperty("hibernate.max_fetch_depth", "3");
			configuration.setProperty("hibernate.hbm2ddl.auto", "update");
			configuration.setProperty("hibernate.cache.use_second_level_cache", "false");
			configuration.setProperty("hibernate.cache.use_query_cache", "false");
			configuration.setProperty("hibernate.show_sql", "true");
			configuration.setProperty("hibernate.format_sql", "true");
			sessionFactory = configuration
					.buildSessionFactory(new StandardServiceRegistryBuilder().applySettings(configuration.getProperties())
							.build());
		} catch (Throwable ex) {
			log.error("Initial SessionFactory creation failed {}", ex);
			throw new ExceptionInInitializerError(ex);
		}
	}

	public static Session currentSession() throws HibernateException {
		Session s = session.get();
		if (s == null) {
			s = sessionFactory.openSession();
			session.set(s);
		}
		return s;
	}

	public static void closeSession() throws HibernateException {
		Session s = session.get();
		if (s != null)
			s.close();
		session.set(null);
	}
}
