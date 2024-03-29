package com.dbms.service.base;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.TypeVariable;
import java.util.List;
import java.util.Set;

import javax.faces.bean.ManagedProperty;
import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.persistence.Query;

import org.hibernate.SQLQuery;
import org.hibernate.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dbms.entity.IEntity;
import com.dbms.util.ICqtEntityManagerFactory;
import com.dbms.util.OrderBy;
import com.dbms.util.exceptions.CqtServiceException;

/**
 * @author Jay G.(jayshanchn@hotmail.com)
 * @date Feb 9, 2017 7:21:44 PM
 **/
public abstract class CqtPersistenceService<E extends IEntity> implements ICqtPersistenceService<E> {

	private static final Logger LOG = LoggerFactory.getLogger(CqtPersistenceService.class);

	private static final String DELETE_SP = "begin CPQ_DML_PKG.SET_MODIFIED_BY(:PV_USER_ID, :PV_FNAME, :PV_LNAME, :PV_GROUP_NAME); end; ";
	
	private Class<E> entityClass;

	@ManagedProperty(value = "#{CqtEntityManagerFactory}")
	protected ICqtEntityManagerFactory cqtEntityManagerFactory;

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public CqtPersistenceService() {
		Class clazz = getClass();
		while (!(clazz.getGenericSuperclass() instanceof ParameterizedType)) {
			clazz = clazz.getSuperclass();
		}
		Object o = ((ParameterizedType) clazz.getGenericSuperclass()).getActualTypeArguments()[0];

		if (o instanceof TypeVariable) {
			this.entityClass = (Class<E>) ((TypeVariable) o).getBounds()[0];
		} else {
			this.entityClass = (Class<E>) o;
		}
	}

	public void setCqtEntityManagerFactory(ICqtEntityManagerFactory cqtEntityManagerFactory) {
		this.cqtEntityManagerFactory = cqtEntityManagerFactory;
	}

	@Override
	public E findById(Long id) {
		E retVal = null;
		final Class<? extends E> productClass = getEntityClass();
		EntityManager entityManager = this.cqtEntityManagerFactory.getEntityManager();
		try {
			retVal = entityManager.find(productClass, id);
		} catch (Exception e) {
			StringBuilder msg = new StringBuilder();
			msg.append("findById failed for type '").append(productClass.getName()).append("' and id value of ").append(
					id);
			LOG.error(msg.toString(), e);
		} finally {
			this.cqtEntityManagerFactory.closeEntityManager(entityManager);
		}
		return retVal;
	}

	@SuppressWarnings({ "unchecked" })
	@Override
	public E findById(Long id, List<String> fetchFields) {
		E retVal = null;
		final Class<? extends E> productClass = getEntityClass();
		EntityManager entityManager = this.cqtEntityManagerFactory.getEntityManager();

		StringBuilder queryString = new StringBuilder("from " + productClass.getName() + " a");
		if ((null != fetchFields) && !fetchFields.isEmpty()) {
			for (String fetchField : fetchFields) {
				queryString.append(" left join fetch a." + fetchField);
			}
		}
		queryString.append(" where a.id = :id");
		try {
			Query query = entityManager.createQuery(queryString.toString());
			query.setParameter("id", id);

			List<E> results = query.getResultList();
			if (!results.isEmpty()) {
				retVal = (E) results.get(0);
			}
		} catch (Exception ex) {
			StringBuilder msg = new StringBuilder();
			msg
					.append("findById with fetchFields, failed for type '")
					.append(productClass.getName())
					.append("' and id value of ")
					.append(id)
					.append(". Query used was->")
					.append(queryString);
			LOG.error(msg.toString(), ex);
		} finally {
			this.cqtEntityManagerFactory.closeEntityManager(entityManager);
		}
		return retVal;
	}
	
	@Override	
	public void create(E e, String userCn, String userFirstName, String userLastName, String userGroups) throws CqtServiceException {
		EntityManager entityManager = this.cqtEntityManagerFactory.getEntityManager();
		EntityTransaction tx = null;
		try {
			tx = entityManager.getTransaction();
			tx.begin();
			
			Session session = entityManager.unwrap(Session.class);
			SQLQuery query = session.createSQLQuery(DELETE_SP);
			query.setParameter("PV_USER_ID", userCn);
			query.setParameter("PV_FNAME", userFirstName);
			query.setParameter("PV_LNAME", userLastName);
			query.setParameter("PV_GROUP_NAME", userGroups);
			query.executeUpdate();
			
			entityManager.persist(e);
			tx.commit();
		} catch (Exception ex) {
			if ((tx != null) && tx.isActive()) {
				tx.rollback();
			}
			StringBuilder msg = new StringBuilder();
			msg.append("Failed to save entity of type '").append(getEntityClass().getName()).append("'");
			LOG.error(msg.toString(), ex);
			throw new CqtServiceException(ex);
		} finally {
			this.cqtEntityManagerFactory.closeEntityManager(entityManager);
		}
	}
	
	@Override
	public E update(E e, String userCn, String userFirstName, String userLastName, String userGroups) throws CqtServiceException {
		EntityManager entityManager = this.cqtEntityManagerFactory.getEntityManager();
		EntityTransaction tx = null;
		try {
			tx = entityManager.getTransaction();
			tx.begin();
			
			Session session = entityManager.unwrap(Session.class);
			SQLQuery query = session.createSQLQuery(DELETE_SP);
			query.setParameter("PV_USER_ID", userCn);
			query.setParameter("PV_FNAME", userFirstName);
			query.setParameter("PV_LNAME", userLastName);
			query.setParameter("PV_GROUP_NAME", userGroups);
			query.executeUpdate();
			
			entityManager.merge(e);
			tx.commit();
		} catch (Exception ex) {
			if ((tx != null) && tx.isActive()) {
				tx.rollback();
			}
			StringBuilder msg = new StringBuilder();
			msg.append("Failed to update entity of type '").append(getEntityClass().getName()).append("'");
			LOG.error(msg.toString(), ex);
			throw new CqtServiceException(ex);
		} finally {
			this.cqtEntityManagerFactory.closeEntityManager(entityManager);
		}
		return e;
	}
	
	@Override
	public void update(List<E> listOfE, String userCn, String userFirstName, String userLastName, String userGroups) throws CqtServiceException {
		EntityManager entityManager = this.cqtEntityManagerFactory.getEntityManager();
		EntityTransaction tx = null;
		try {
			tx = entityManager.getTransaction();
			tx.begin();
			
			Session session = entityManager.unwrap(Session.class);
			SQLQuery query = session.createSQLQuery(DELETE_SP);
			query.setParameter("PV_USER_ID", userCn);
			query.setParameter("PV_FNAME", userFirstName);
			query.setParameter("PV_LNAME", userLastName);
			query.setParameter("PV_GROUP_NAME", userGroups);
			query.executeUpdate();
			
			for (E e : listOfE) {
				//setModifyTimestampAndUserInfo(e);
				entityManager.merge(e);
			}
			tx.commit();
		} catch (Exception ex) {
			if ((tx != null) && tx.isActive()) {
				tx.rollback();
			}
			StringBuilder msg = new StringBuilder();
			msg.append("Failed to update entity of type '").append(getEntityClass().getName()).append("'");
			LOG.error(msg.toString(), ex);
			throw new CqtServiceException(ex);
		} finally {
			this.cqtEntityManagerFactory.closeEntityManager(entityManager);
		}
	}

	@Override
	public void remove(Long id, String userCn, String userFirstName, String userLastName, String userGroups) throws CqtServiceException {
		EntityManager entityManager = this.cqtEntityManagerFactory.getEntityManager();
		final Class<? extends E> entityClass = getEntityClass();
		EntityTransaction tx = null;
		try {
			tx = entityManager.getTransaction();
			tx.begin();
			Session session = entityManager.unwrap(Session.class);
			SQLQuery query = session.createSQLQuery(DELETE_SP);
			query.setParameter("PV_USER_ID", userCn);
			query.setParameter("PV_FNAME", userFirstName);
			query.setParameter("PV_LNAME", userLastName);
			query.setParameter("PV_GROUP_NAME", userGroups);
			query.executeUpdate();
			
			E e = entityManager.find(entityClass, id);
			if (e != null) {
				entityManager.remove(e);
			}
			tx.commit();
		} catch (Exception ex) {
			if ((tx != null) && tx.isActive()) {
				tx.rollback();
			}
			StringBuilder msg = new StringBuilder();
			msg.append("Failed to remove entity of type '").append(getEntityClass().getName()).append("'");
			LOG.error(msg.toString(), ex);
			throw new CqtServiceException(ex);
		} finally {
			this.cqtEntityManagerFactory.closeEntityManager(entityManager);
		}
	}

	@Override
	public void remove(E e, String userCn, String userFirstName, String userLastName, String userGroups) throws CqtServiceException {
		if (null == e) {
			throw new CqtServiceException("Entity remove stopped. Null entity received for removal.");
		} else if (e.getId() != null) {
			throw new CqtServiceException("Entity remove stopped. Null primary key found for the entity.");
		} else {
			remove(e.getId(), userCn, userFirstName, userLastName, userGroups);
		}
	}

	@Override
	public void remove(Set<Long> ids, String userCn, String userFirstName, String userLastName, String userGroups) throws CqtServiceException {
		final Class<? extends E> entityClass = getEntityClass();
		String deleteSp = "begin CPQ_DML_PKG.SET_MODIFIED_BY(:PV_USER_ID, :PV_FNAME, :PV_LNAME, :PV_GROUP_NAME); end; ";
		
		StringBuilder queryString = new StringBuilder("delete from ");
		queryString.append(entityClass.getName()).append(" where id in (:ids) ");

		EntityManager entityManager = this.cqtEntityManagerFactory.getEntityManager();
		EntityTransaction tx = null;
		try {
			tx = entityManager.getTransaction();
			tx.begin();
			Session session = entityManager.unwrap(Session.class);
			SQLQuery deleteSpQuery = session.createSQLQuery(deleteSp);
			deleteSpQuery.setParameter("PV_USER_ID", userCn);
			deleteSpQuery.setParameter("PV_FNAME", userFirstName);
			deleteSpQuery.setParameter("PV_LNAME", userLastName);
			deleteSpQuery.setParameter("PV_GROUP_NAME", userGroups);
			deleteSpQuery.executeUpdate();
			
			Query query = entityManager.createQuery(queryString.toString());
			query.setParameter("ids", ids);
			query.executeUpdate();
			tx.commit();
		} catch (Exception ex) {
			if ((tx != null) && tx.isActive()) {
				tx.rollback();
			}
			StringBuilder msg = new StringBuilder();
			msg
					.append("Failed to update remove on ids of type '")
					.append(getEntityClass().getName())
					.append("' Ids provided were->")
					.append(ids.toString());
			LOG.error(msg.toString(), ex);
			throw new CqtServiceException(ex);
		} finally {
			this.cqtEntityManagerFactory.closeEntityManager(entityManager);
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<E> list() {
		List<E> retVal = null;
		final Class<? extends E> entityClass = getEntityClass();
		EntityManager entityManager = this.cqtEntityManagerFactory.getEntityManager();
		try {
			retVal = entityManager.createQuery("from " + entityClass.getName() + " a ").getResultList();
		} finally {
			this.cqtEntityManagerFactory.closeEntityManager(entityManager);
		}
		return retVal;
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<E> list(String orderByEntityField, OrderBy orderBy) {
		List<E> retVal = null;
		final Class<? extends E> entityClass = getEntityClass();
		EntityManager entityManager = this.cqtEntityManagerFactory.getEntityManager();
		try {
			retVal = entityManager
					.createQuery("from " + entityClass.getName() + " a order by a." + orderByEntityField + " "
							+ orderBy.name())
					.getResultList();
		} finally {
			this.cqtEntityManagerFactory.closeEntityManager(entityManager);
		}
		return retVal;
	}

	@Override
	public Class<E> getEntityClass() {
		return this.entityClass;
	}
	
}
