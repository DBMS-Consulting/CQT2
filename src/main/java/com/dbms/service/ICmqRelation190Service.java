package com.dbms.service;

import java.util.List;

import com.dbms.entity.cqt.CmqRelation190;
import com.dbms.service.base.ICqtPersistenceService;

public interface ICmqRelation190Service extends ICqtPersistenceService<CmqRelation190> {

	List<CmqRelation190> findBaseWithRootRelations();

	CmqRelation190 findByTermName(String termName);

}