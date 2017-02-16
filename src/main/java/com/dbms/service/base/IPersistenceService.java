package com.dbms.service.base;

import java.util.List;
import java.util.Set;

import com.dbms.entity.IEntity;

/**
 * @author Jay G.(jayshanchn@hotmail.com)
 * @date Feb 9, 2017 7:24:25 PM
 **/
public interface IPersistenceService<E extends IEntity> {
	public E findById(Long id);
	public E findById(Long id, List<String> fetchFields);
	public void create(E e) throws Exception;
	public E update(E e) throws Exception;
	public void remove(Long id) throws Exception;
	public void remove(E e) throws Exception;
	public void remove(Set<Integer> ids) throws Exception;
	public Class<E> getEntityClass();
	public List<E> list();
}
