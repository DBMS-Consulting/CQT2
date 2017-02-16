package com.dbms.service;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.TypeVariable;
import java.util.List;
import java.util.Set;

import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dbms.entity.IEntity;
import com.dbms.service.base.IPersistenceService;
import com.dbms.util.HibernateUtil;

/**
 * @author Jay G.(jayshanchn@hotmail.com)
 * @date Feb 9, 2017 7:21:44 PM
 **/
public class HibernatePersistenceService<E extends IEntity> implements IPersistenceService<E> {

	protected final Logger log = LoggerFactory.getLogger(getClass());
	protected Class<E> entityClass;

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public HibernatePersistenceService() {
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

	@Override
	public E findById(Long id) {
		log.debug("start of find {} by id (id={}) ..", getEntityClass().getSimpleName(), id);
		Session session = HibernateUtil.currentSession();
		final Class<? extends E> productClass = getEntityClass();
		E e = session.find(productClass, id);
		HibernateUtil.closeSession();
		log.trace("end of find {} by id (id={}). Result found={}.", getEntityClass().getSimpleName(), id, e != null);
		return e;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public E findById(Long id, List<String> fetchFields) {
		log.debug("start of find {} by id (id={}) ..", getEntityClass().getSimpleName(), id);
		Session session = HibernateUtil.currentSession();
		E e = null;
		try {
			final Class<? extends E> productClass = getEntityClass();
			StringBuilder queryString = new StringBuilder("from " + productClass.getName() + " a");
			if (fetchFields != null && !fetchFields.isEmpty()) {
				for (String fetchField : fetchFields) {
					queryString.append(" left join fetch a." + fetchField);
				}
			}
			queryString.append(" where a.id = :id");
			Query query = session.createQuery(queryString.toString());
			query.setParameter("id", id);

			List<E> results = query.getResultList();
			if (!results.isEmpty()) {
				e = (E) results.get(0);
			}
		} catch (Exception ex) {
		} finally {
			HibernateUtil.closeSession();
		}
		log.trace("end of find {} by id (id={}). Result found={}.", getEntityClass().getSimpleName(), id, e != null);
		return e;
	}

	public void create(E e) throws Exception {
		log.debug("start of create {} entity id={}", e.getClass().getSimpleName(), e.getId());
		Session session = HibernateUtil.currentSession();
		Transaction transaction = session.getTransaction();
		try {
			transaction.begin();
			session.save(e);
			transaction.commit();
		} catch (Exception ex) {
			transaction.rollback();
			throw new Exception(ex);
		} finally {
			HibernateUtil.closeSession();
		}
		log.trace("end of create {}. entity id={}.", e.getClass().getSimpleName(), e.getId());
	}

	@Override
	public E update(E e) throws Exception {
		log.debug("start of update {} entity (id={}) ..", e.getClass().getSimpleName(), e.getId());
		Session session = HibernateUtil.currentSession();
		Transaction transaction = session.getTransaction();
		try {
			transaction.begin();
			session.merge(e);
			transaction.commit();
		} catch (Exception ex) {
			transaction.rollback();
			throw new Exception(ex);
		} finally {
			HibernateUtil.closeSession();
		}
		log.trace("end of update {} entity (id={}).", e.getClass().getSimpleName(), e.getId());
		return e;
	}

	@Override
	public void remove(Long id) throws Exception {
		log.debug("start of remove {} entity (id={}) ..", getEntityClass().getSimpleName(), id);
		Session session = HibernateUtil.currentSession();
		Transaction transaction = session.getTransaction();
		try {
			transaction.begin();
			E e = session.find(getEntityClass(), id);
			if (e != null) {
				session.remove(e);
			}
			transaction.commit();
		} catch (Exception ex) {
			transaction.rollback();
			throw new Exception(ex);
		} finally {
			HibernateUtil.closeSession();
		}
		log.trace("end of remove {} entity (id={}).", getEntityClass().getSimpleName(), id);
	}

	@Override
	public void remove(E e) throws Exception {
		remove(e.getId());
	}

	@SuppressWarnings("rawtypes")
	@Override
	public void remove(Set<Integer> ids) throws Exception {
		Session session = HibernateUtil.currentSession();
		Transaction transaction = session.getTransaction();
		try {
			transaction.begin();
			Query query = session.createQuery("delete from " + getEntityClass().getName() + " where id in (:ids)");
			query.setParameter("ids", ids);
			query.executeUpdate();
			transaction.commit();
		} catch (Exception ex) {
			transaction.rollback();
			throw new Exception(ex);
		} finally {
			HibernateUtil.closeSession();
		}
	}

	@Override
	public Class<E> getEntityClass() {
		return this.entityClass;
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<E> list() {
		final Class<? extends E> entityClass = getEntityClass();
		Session session = HibernateUtil.currentSession();
		List<E> result = session.createQuery("from " + entityClass + " a ").getResultList();
		HibernateUtil.closeSession();
		return result;
	}

}
