package com.dbms.util;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

/**
 * @date Feb 6, 2017 5:27:54 PM
 **/
public class HibernateUtil {
	private static EntityManagerFactory entityManagerFactory = null;
	public static EntityManager openEntityManager() {
		if (entityManagerFactory == null) {
			entityManagerFactory = Persistence.createEntityManagerFactory( "cqtJPA" );
		}
		return entityManagerFactory.createEntityManager();
	}
}
