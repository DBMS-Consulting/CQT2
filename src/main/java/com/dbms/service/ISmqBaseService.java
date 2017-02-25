package com.dbms.service;

import java.util.List;

import com.dbms.entity.cqt.SmqBase190;
import com.dbms.entity.cqt.SmqRelation190;

public interface ISmqBaseService {

	List<SmqBase190> findByLevelAndTerm(Integer level, String searchTerm);

	Long findSmqRelationsCountForSmqCode(Long smqCode);

	List<SmqRelation190> findSmqRelationsForSmqCode(Long smqCode);
	
	SmqBase190 findByCode(Long smqCode);
}