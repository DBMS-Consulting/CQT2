package com.dbms.service;

import com.dbms.entity.cqt.SessionTrack;

public interface ISessionTrackService {

	SessionTrack addLoginSessionTrack(String adCn, String adFirstName, String adLastName, String adEmail,
			String adGroupList, String javaSessionGuid);

	SessionTrack updateLogoutInSessionTrack(Long sessionTrackId);

}