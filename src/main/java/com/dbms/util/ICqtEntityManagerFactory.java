package com.dbms.util;

import java.util.Map;

import javax.persistence.EntityManager;

public interface ICqtEntityManagerFactory {

	/**
	 * Create a new application-managed EntityManager with the specified Map
	 * of properties. This method returns a new EntityManager instance each time
	 * it is invoked.
	 * 
	 * @return {@link EntityManager} a new entity manager
	 */
	EntityManager getEntityManager();

	/**
	 * Create a new application-managed EntityManager with the specified Map of
	 * properties. This method returns a new EntityManager instance each time it
	 * is invoked.
	 * 
	 * @param map
	 *            of properties to create the entity manager with
	 * @return {@link EntityManager} a new entity manager
	 */
	EntityManager getEntityManager(Map map);

	void closeEntityManager(EntityManager entityManager);

}