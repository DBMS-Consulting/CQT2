package com.dbms.service.base;

import java.util.List;
import java.util.Set;

import com.dbms.entity.IEntity;
import com.dbms.util.OrderBy;
import com.dbms.util.exceptions.CqtServiceException;

/**
 * @author Jay G.(jayshanchn@hotmail.com)
 * @date Feb 9, 2017 7:24:25 PM
 **/
public interface ICqtPersistenceService<E extends IEntity> {
	public E findById(Long id);

	public E findById(Long id, List<String> fetchFields);

	public void create(E e) throws CqtServiceException;

	public E update(E e) throws CqtServiceException;

	public void remove(Long id) throws CqtServiceException;

	public void remove(E e) throws CqtServiceException;

	public void remove(Set<Long> ids) throws CqtServiceException;

	public Class<E> getEntityClass();

	public List<E> list();

	public List<E> list(String orderByEntityField, OrderBy orderBy);

	public void update(List<E> e) throws CqtServiceException;
}
