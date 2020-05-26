package com.dbms.service;

import java.util.List;
import java.util.Map;

import com.dbms.entity.cqt.SmqBase190;
import com.dbms.entity.cqt.SmqRelation190;
import com.dbms.entity.cqt.dtos.SMQReverseHierarchySearchDto;
import com.dbms.util.SmqAndPtCodeHolder;

public interface ISmqBaseService {

	List<SmqBase190> findByLevelAndTerm(Integer level, String searchTerm);

	Long findSmqRelationsCountForSmqCode(Long smqCode);
	List<Map<String, Object>> findSmqRelationsCountForSmqCodes(List<Long> smqCodes);

	List<SmqRelation190> findSmqRelationsForSmqCode(Long smqCode);
	List<SmqRelation190> findSmqRelationsForSmqCodes(List<Long> smqCodes);

	List<SmqBase190> findChildSmqByParentSmqCodes(List<Long> smqCodes);
	List<SmqBase190> findChildSmqByParentSmqCode(Long smqCode);

	SmqBase190 findByCode(Long smqCode);
    List<SmqBase190> findByCodes(List<Long> smqCodes);

	Long findChildSmqCountByParentSmqCode(Long smqCode);
    List<Map<String, Object>> findChildSmqCountByParentSmqCodes(List<Long> smqCodes);

	SmqRelation190 findSmqRelationBySmqAndPtCode(Long smqCode, Integer ptCode);
	List<SmqRelation190> findSmqRelationBySmqAndPtCode(List<SmqAndPtCodeHolder> smqAndPtCodeHolders);

	List<SmqRelation190> findSmqRelationsForSmqCodeAndScope(Long smqCode, String scope);

	List<SMQReverseHierarchySearchDto> findFullReverseByLevelAndTerm(String level, String myFilterTermName);

	List<SMQReverseHierarchySearchDto> findReverseByLevelAndTerm(Integer level, String searchTerm);

	List<Map<String, Object>> findParentCountSmqCountByChildSmqCodes(List<Long> smqCodes);

	List<SMQReverseHierarchySearchDto> findReverseParentByChildCode(Long smqCode);

	SmqRelation190 findSmqRelationsByPtCode(Long ptCode);

	List<Map<String, Object>> findSmqChildRelationsCountForSmqCodes(List<Long> smqCodes);

	Long findSmqChildRelationsCountForSmqCode(Long smqCode);
	
	Long findChildSmqCountByParentSmqCode(Long smqCode, String dictionaryVersion);

	Long findSmqRelationsCountForSmqCode(Long smqCode, String dictionaryVersion);

	List<SmqBase190> findChildSmqByParentSmqCode(Long smqCode, String dictionaryVersion);

	List<Map<String, Object>> findSmqRelationsCountForSmqCodes(List<Long> smqCodes, String dictionaryVersion);

	List<SmqRelation190> findSmqRelationsForSmqCodeAndScope(Long smqCode, String scope, String dictionaryVersion);

	List<SmqRelation190> findSmqRelationsForSmqCode(Long smqCode, String dictionaryVersion);

	List<Map<String, Object>> findSmqChildRelationsCountForSmqCodes(List<Long> smqCodes, String dictionaryVersion);

	SmqRelation190 findSmqRelationBySmqAndPtCode(Long smqCode, Integer ptCode, String dictionaryVersion);

	SmqBase190 findByCode(Long smqCode, String dictionaryVersion);
	
	List<SmqRelation190> findSmqRelationsForSmqCodeAndImapctType(Long smqCode,List<String> impactTypes);
	List<SmqRelation190> findSmqRelationsForSmqCodeAndScopeAndImpactType(Long smqCode, String scope,List<String> impactTypes);

}