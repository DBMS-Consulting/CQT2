package com.dbms.service;

import java.util.List;

import com.dbms.entity.cqt.CmqRelation190;
import com.dbms.service.base.ICqtPersistenceService;
import com.dbms.util.exceptions.CqtServiceException;

public interface ICmqRelation190Service extends ICqtPersistenceService<CmqRelation190> {

	public void create(List<CmqRelation190> cmqRelations) throws CqtServiceException;
}