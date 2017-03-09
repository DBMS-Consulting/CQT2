package com.dbms.service;

import java.util.List;

import javax.faces.bean.ApplicationScoped;
import javax.faces.bean.ManagedBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dbms.entity.cqt.CreateEntity;

/**
 * @date Feb 6, 2017 9:53:07 AM
 **/
@ManagedBean(name="createEntityService")
@ApplicationScoped
public class CreateEntityService {

	private static final Logger log = LoggerFactory.getLogger(CreateEntityService.class);

	public void save(CreateEntity entity) {
		/*EntityManager entityManager = HibernateUtil.openEntityManager();
		entityManager.getTransaction().begin();
		if (entity.isTransient()) {
			entityManager.persist(entity);
		} else {
			entityManager.merge(entity);
		}
		entityManager.getTransaction().commit();
		entityManager.close();*/
	}

	public CreateEntity findByCode(Integer code) {
		/*EntityManager entityManager = HibernateUtil.openEntityManager();
		CreateEntity entity = entityManager.find(CreateEntity.class, code);
		entityManager.close();
		return entity;*/
		return null;
	}

	public List<CreateEntity> findAll() {
		/*EntityManager entityManager = HibernateUtil.openEntityManager();
		List<CreateEntity> entities = entityManager.createQuery("from CreateEntity c").getResultList();
		entityManager.close();
		return entities;*/
		return null;
	}

	private StringBuilder appendClause(StringBuilder sb, boolean first) {
		if (first) {
			sb.append(" where");
		} else {
			sb.append(" and");
		}
		return sb;
	}

	public List<CreateEntity> findByCriterias(String extension, String drugProgram, String protocol) {
		/*StringBuilder sb = new StringBuilder("from CreateEntity c");
		boolean first = true;
		if (StringUtils.isNotEmpty(extension)) {
			sb.append(" where c.extension=:extension");
			first = false;
		}
		if (StringUtils.isNoneEmpty(drugProgram)) {
			sb = appendClause(sb, first);
			sb.append(" c.drugProgram like :drugProgram");
			first = false;
		}
		if (StringUtils.isNotEmpty(protocol)) {
			sb = appendClause(sb, first);
			sb.append(" c.protocol like :protocol");
			first = false;
		}
		EntityManager entityManager = HibernateUtil.openEntityManager();
		Query query = entityManager.createQuery(sb.toString());
		if (StringUtils.isNotEmpty(extension)) {
			query.setParameter("extension", extension);
		}
		if (StringUtils.isNotEmpty(drugProgram)) {
			query.setParameter("drugProgram", "%" + drugProgram + "%");
		}
		if (StringUtils.isNotEmpty(protocol)) {
			query.setParameter("protocol", "%" + protocol + "%");
		}

		List<CreateEntity> entities = query.getResultList();
		entityManager.close();
		return entities;*/
		return null;
	}
}
