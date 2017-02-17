package com.dbms.service;

import javax.faces.bean.ApplicationScoped;
import javax.faces.bean.ManagedBean;
import javax.persistence.EntityManager;

import com.dbms.entity.cqt.User;
import com.dbms.util.CqtEntityManagerFactory;

/**
 * @date Feb 7, 2017 2:09:15 AM
 **/
@ManagedBean(name = "userService")
@ApplicationScoped
public class UserService {

	public User findByName(String name) {
		/*EntityManager entityManager = HibernateUtil.openEntityManager();
		User user = null;
		try {
			user = (User) entityManager.createQuery("from User g where g.name=:name").setParameter("name", name).getSingleResult();
		} catch (Exception e) {
		}
		entityManager.close();
		return user;*/
		return null;
	}

	public User findById(Long id) {
		/*EntityManager entityManager = HibernateUtil.openEntityManager();
		User user = null;
		try {
			user = (User) entityManager.find(User.class, id);
		} catch (Exception e) {
		}
		entityManager.close();
		return user;*/
		return null;
	}

	public void save(User user) {
		/*EntityManager entityManager = HibernateUtil.openEntityManager();
		try {
			entityManager.getTransaction().begin();
			entityManager.persist(user);
			entityManager.getTransaction().commit();
		} catch (Exception e) {
			e.printStackTrace();
		}
		entityManager.close();*/
	}
}
