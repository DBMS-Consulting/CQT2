package com.dbms.service.base;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.TypeVariable;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Set;

import javax.faces.bean.ManagedProperty;
import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.persistence.Query;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dbms.entity.IEntity;
import com.dbms.entity.cqt.CmqBase190;
import com.dbms.entity.cqt.CmqRelation190;
import com.dbms.entity.cqt.SmqBase190;
import com.dbms.util.ICqtEntityManagerFactory;
import com.dbms.util.OrderBy;
import com.dbms.util.exceptions.CqtServiceException;

/**
 * @author Jay G.(jayshanchn@hotmail.com)
 * @date Feb 9, 2017 7:21:44 PM
 **/
public abstract class CqtPersistenceService<E extends IEntity> implements ICqtPersistenceService<E> {

	private static final Logger LOG = LoggerFactory.getLogger(CqtPersistenceService.class);

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

	public void create(E e) throws CqtServiceException {
		EntityManager entityManager = this.cqtEntityManagerFactory.getEntityManager();
		EntityTransaction tx = null;
		try {
			tx = entityManager.getTransaction();
			tx.begin();
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
	
	/**
	 * set the last modified date/user info
	 * @param e
	 */
	private void setModifyTimestampAndUserInfo(E e) {
		if(e instanceof CmqBase190) {
			((CmqBase190) e).setLastModifiedDate(new Date());
			((CmqBase190) e).setLastModifiedBy("test-user");
		} else if(e instanceof CmqRelation190) {
			((CmqRelation190) e).setLastModifiedDate(new Date());
			((CmqRelation190) e).setLastModifiedBy("test-user");
		}
	}

	@Override
	public E update(E e) throws CqtServiceException {
		EntityManager entityManager = this.cqtEntityManagerFactory.getEntityManager();
		EntityTransaction tx = null;
		try {
			tx = entityManager.getTransaction();
			tx.begin();
			
			// set the last modified date/user info
			setModifyTimestampAndUserInfo(e);
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
	public void update(List<E> listOfE) throws CqtServiceException {
		EntityManager entityManager = this.cqtEntityManagerFactory.getEntityManager();
		EntityTransaction tx = null;
		try {
			tx = entityManager.getTransaction();
			tx.begin();
			for (E e : listOfE) {
				setModifyTimestampAndUserInfo(e);
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
	public void remove(Long id) throws CqtServiceException {
		EntityManager entityManager = this.cqtEntityManagerFactory.getEntityManager();
		final Class<? extends E> entityClass = getEntityClass();
		EntityTransaction tx = null;
		try {
			tx = entityManager.getTransaction();
			tx.begin();
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
			msg.append("Failed to update entity of type '").append(getEntityClass().getName()).append("'");
			LOG.error(msg.toString(), ex);
			throw new CqtServiceException(ex);
		} finally {
			this.cqtEntityManagerFactory.closeEntityManager(entityManager);
		}
	}

	@Override
	public void remove(E e) throws CqtServiceException {
		if (null == e) {
			throw new CqtServiceException("Entity remove stopped. Null entity received for removal.");
		} else if (e.getId() != null) {
			throw new CqtServiceException("Entity remove stopped. Null primary key found for the entity.");
		} else {
			remove(e.getId());
		}
	}

	@Override
	public void remove(Set<Long> ids) throws CqtServiceException {
		final Class<? extends E> entityClass = getEntityClass();
		StringBuilder queryString = new StringBuilder("delete from ");
		queryString.append(entityClass.getName()).append(" where id in (:ids) ");

		EntityManager entityManager = this.cqtEntityManagerFactory.getEntityManager();
		EntityTransaction tx = null;
		try {
			tx = entityManager.getTransaction();
			tx.begin();
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
