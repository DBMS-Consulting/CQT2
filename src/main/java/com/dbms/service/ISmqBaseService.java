package com.dbms.service;

import java.util.List;
import java.util.Map;

import com.dbms.entity.cqt.SmqBase190;
import com.dbms.entity.cqt.SmqRelation190;

public interface ISmqBaseService {

	List<SmqBase190> findByLevelAndTerm(Integer level, String searchTerm);

	Long findSmqRelationsCountForSmqCode(Long smqCode);

	List<Map<String, Object>> findSmqRelationsCountForSmqCodes(List<Long> smqCodes);

	List<SmqRelation190> findSmqRelationsForSmqCode(Long smqCode);

	List<SmqRelation190> findSmqRelationsForSmqCodes(List<Long> smqCodes);

	List<SmqBase190> findChildSmqByParentSmqCodes(List<Long> smqCodes);

	List<SmqBase190> findChildSmqByParentSmqCode(Long smqCode);

	SmqBase190 findByCode(Long smqCode);

	Long findChildSmqCountByParentSmqCode(Long smqCode);

	SmqRelation190 findSmqRelationBySmqAndPtCode(Long smqCode, Integer ptCode);

}