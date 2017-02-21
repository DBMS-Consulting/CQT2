package com.dbms.service;

import java.util.List;

import com.dbms.entity.cqt.SmqBase190;

public interface ISmqBaseService {

	List<SmqBase190> findByLevelAndTerm(Integer level, String searchTerm);

}