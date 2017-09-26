package com.dbms.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.SessionScoped;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.mail.internet.MimeUtility;
import javax.mail.internet.ParseException;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dbms.entity.cqt.RefConfigCodeList;
import com.dbms.entity.cqt.SessionTrack;

@ManagedBean(name = "AuthenticationService", eager = true)
@SessionScoped
public class AuthenticationService {

	private static final Logger LOG = LoggerFactory.getLogger(AuthenticationService.class);

	private static final String PXED_DUMMY_GROUP_MEMBERSHIP = "CN=OPENCQT_MQM,OU=CQT_OU,OU=Applications,OU=Delegated,OU=Groups,DC=pxed,DC=pfizer,DC=com"
																+ ":CN=CQT_Users,OU=CQT_OU,OU=Applications,OU=Delegated,OU=Groups,DC=pxed,DC=pfizer,DC=com"
																+ ":CN=GBL-BTNONColleagues,OU=GBLGroups,OU=Applications,OU=Delegated,OU=Groups,DC=pxed,DC=pfizer,DC=com";
	
	private static final String IAMPFIZERUSERCN_HEADER = "IAMPFIZERUSERCN";
	private static final String IAMPFIZERUSERGIVENNAME_HEADER = "IAMPFIZERUSERGIVENNAME";
	private static final String IAMPFIZERUSERSURNAME_HEADER = "IAMPFIZERUSERSURNAME";
	private static final String IAMPFIZERUSERGROUPMEMBERSHIP_HEADER = "IAMPFIZERUSERGROUPMEMBERSHIP";
	private static final String IAMPFIZERUSERINTERNETEMAILADDRESS_HEADER = "IAMPFIZERUSERINTERNETEMAILADDRESS";

	public static final String MANAGER_GROUP = "MANAGER";
	public static final String ADMIN_GROUP = "ADMIN";
	public static final String REQUESTER_GROUP = "REQUESTOR";
	public static final String USER_GROUP = "USER";
	public static final String MQM_GROUP = "MQM";
	
	public static final String ENTERPRISE_AD_PXED = "PXED";
	public static final String ENTERPRISE_AD_PXED_DUMMY = "PXED-DUMMY";
	public static final String ENTERPRISE_AD_DBMS = "AD";
	public static final String ENTERPRISE_AD_NONE = "NONE";
	public static final String DEFAULT_ENTERPRISE_AD_CODE_LIST_VALUE = ENTERPRISE_AD_NONE;
	
	@ManagedProperty("#{RefCodeListService}")
	private IRefCodeListService refCodeListService;

	@ManagedProperty("#{SessionTrackService}")
	private ISessionTrackService sessionTrackService;
	
	private List<UrlGroups> urlGroupsList;

	private String userCn;
	private String userGivenName;
	private String userSurName;
	private String userEmail;
	private String groupMembershipHeader;
	private List<String> groupMemberships;
	private List<String> cmqMappedGroupMemberships;
	private List<String> combinedMappedGroupMemberships;
	private String enterpriseAdCodeListValue;

	private Long sessionTrackId;
	
	@PostConstruct
	public void init() {
		this.parseUrlPermissionMappings();
	}

	public void logout(HttpServletRequest request) {
		ExternalContext ec = FacesContext.getCurrentInstance().getExternalContext();
	    ec.invalidateSession();
	    try {
			ec.redirect("index.html");
		} catch (IOException e) {
			e.printStackTrace();
		}
	    
		FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "Logout success!", "");
		FacesContext.getCurrentInstance().addMessage(null, msg);
		
		
	}
	
	public void authenticate(HttpServletRequest request) throws IOException {
		this.validateUser(request);
		
		//add session track data now if enterpriseAdCodeListValue is PXED, PXED-DUMMY or AD
		if(ENTERPRISE_AD_PXED.equalsIgnoreCase(this.enterpriseAdCodeListValue)
				|| ENTERPRISE_AD_PXED_DUMMY.equalsIgnoreCase(this.enterpriseAdCodeListValue)) {
			ExternalContext ec = FacesContext.getCurrentInstance().getExternalContext();
			SessionTrack sessionTrack = this.sessionTrackService.addLoginSessionTrack(this.userCn, this.userGivenName, this.userSurName
															, this.userEmail, this.groupMembershipHeader, ec.getSessionId(false));
			if(null != sessionTrack) {
				this.sessionTrackId = sessionTrack.getId();
				LOG.info("Added SessionTrack for user " + this.userCn + " with sessionTrackId: " + this.sessionTrackId);
			}
		}
		
		FacesContext.getCurrentInstance().getExternalContext()
				.redirect(request.getContextPath() + "/index.xhtml?faces-redirect=true");
	}

	public void validateUser(HttpServletRequest request) {
		RefConfigCodeList enterpriseAdCodeList = refCodeListService.findEnterpriseAdType();
		if (null != enterpriseAdCodeList) {
			this.enterpriseAdCodeListValue = enterpriseAdCodeList.getValue();
			if (StringUtils.isBlank(this.enterpriseAdCodeListValue)) {
				this.enterpriseAdCodeListValue = DEFAULT_ENTERPRISE_AD_CODE_LIST_VALUE;
			}
			if (ENTERPRISE_AD_PXED.equalsIgnoreCase(this.enterpriseAdCodeListValue)) {
				this.parsePxedHeaderData(request);
			} else if (ENTERPRISE_AD_PXED_DUMMY.equalsIgnoreCase(this.enterpriseAdCodeListValue)) {
				this.setPxedDummyData();
			} else if (ENTERPRISE_AD_DBMS.equalsIgnoreCase(this.enterpriseAdCodeListValue)) {
				// this is DBMS internal AD
			} else {
				this.setNoneEnterpriseAdType();
			}
		}
	}

	public static String decode(String value) {
		try {
			return MimeUtility.decodeWord(value);
		} catch (ParseException | UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			//e.printStackTrace();
		}
		return value;
	}
	
	private void parsePxedHeaderData(HttpServletRequest request) {
		this.userCn = decode(request.getHeader(IAMPFIZERUSERCN_HEADER));
		this.userGivenName = decode(request.getHeader(IAMPFIZERUSERGIVENNAME_HEADER));
		this.userSurName = decode(request.getHeader(IAMPFIZERUSERSURNAME_HEADER));
		this.userEmail = decode(request.getHeader(IAMPFIZERUSERINTERNETEMAILADDRESS_HEADER));
		this.groupMembershipHeader = decode(request.getHeader(IAMPFIZERUSERGROUPMEMBERSHIP_HEADER));
		
		LOG.info("Group membership header for " + this.userCn + " is : " + this.groupMembershipHeader);
		this.parseAndSetGroupMemberships();
	}

	private void setPxedDummyData() {
		this.userCn = "ZUTSHM-Dummy";
		this.userGivenName = "MEENAKSHI";
		this.userSurName = "ZUTSHI";
		this.userEmail = "meenakshi.zutshi@pfizer.com";
		if(this.groupMembershipHeader == null) {
			//if its not filled from the dummy ldap form
			this.groupMembershipHeader = PXED_DUMMY_GROUP_MEMBERSHIP;
		}
		this.parseAndSetGroupMemberships();
	}

	private void setNoneEnterpriseAdType() {
		this.userCn = "Test";
		this.userGivenName = "Test";
		this.userSurName = "Test";
		this.userEmail = "test@test.com";
		this.cmqMappedGroupMemberships = new ArrayList<>();
		this.cmqMappedGroupMemberships.add(MANAGER_GROUP);
		this.cmqMappedGroupMemberships.add(ADMIN_GROUP);
	}

	private void parseAndSetGroupMemberships() {
		// cter Dn example:
		// "CN=OPENCQT_MQM,OU=CQT_OU,OU=Applications,OU=Delegated,OU=Groups,DC=pxed,DC=pfizer,DC=com:CN=CQT_Users,OU=CQT_OU,OU=Applications,OU=Delegated,OU=Groups,DC=pxed,DC=pfizer,DC=com:CN=GBL-BTNONColleagues,OU=GBLGroups,OU=Applications,OU=Delegated,OU=Groups,DC=pxed,DC=pfizer,DC=com";
		this.groupMemberships = new ArrayList<String>();
		if (this.groupMembershipHeader != null) {
			String[] dnStrings = this.groupMembershipHeader.split(":");
			for (String dn : dnStrings) {
				String[] pieces = dn.split(",");
				for (String piece : pieces) {
					String[] pieceParts = piece.split("=");
					if (pieceParts.length == 2) {
						if (StringUtils.isNotEmpty(pieceParts[0]) && (null != pieceParts[1])
								&& "CN".equalsIgnoreCase(pieceParts[0].trim())) {
							this.groupMemberships.add(pieceParts[1].trim());
						}
					}
				}
			}
		}
		LOG.info("User [" + this.userCn + "] belongs to PXED groups " + this.groupMemberships);

		// map the pfixer groups to cmq groups and add them here
		List<RefConfigCodeList> cmqUserGroups = this.refCodeListService.findUserGroups();
		this.cmqMappedGroupMemberships = new ArrayList<>();
		this.combinedMappedGroupMemberships = new ArrayList<>();
		if (null != cmqUserGroups) {
			for (RefConfigCodeList cmqUserGroup : cmqUserGroups) {
				String pxedGroupName = cmqUserGroup.getValue();
				if (this.groupMemberships.contains(pxedGroupName)) {
					this.cmqMappedGroupMemberships.add(cmqUserGroup.getCodelistInternalValue());
					int index = this.groupMemberships.indexOf(pxedGroupName);
					String groupName = this.groupMemberships.get(index);
					String combinedMapping = cmqUserGroup.getCodelistInternalValue() + "[" + groupName + "]";
					this.combinedMappedGroupMemberships.add(combinedMapping);
				}
			}
			if (this.cmqMappedGroupMemberships.isEmpty()) {
				this.cmqMappedGroupMemberships.add(USER_GROUP);// default group
				this.combinedMappedGroupMemberships.add(USER_GROUP + "["+ USER_GROUP +"]");
			}
		} else {
			this.cmqMappedGroupMemberships.add(USER_GROUP);// default group
			this.combinedMappedGroupMemberships.add(USER_GROUP + "["+ USER_GROUP +"]");
		}
	}

	public String getGroupMembershipsAsString() {
		String groupsString = "[";
		int i = 0;
		if (("PXED".equalsIgnoreCase(this.enterpriseAdCodeListValue))
				|| ("PXED-DUMMY".equalsIgnoreCase(this.enterpriseAdCodeListValue))) {
			for (String group : groupMemberships) {
				groupsString += group;
				if (i++ < (groupMemberships.size() - 1)) {
					groupsString += ", ";
				}
			}
		} else if(this.cmqMappedGroupMemberships != null) {
			for (String group : this.cmqMappedGroupMemberships) {
				groupsString += group;
				if (i++ < (cmqMappedGroupMemberships.size() - 1)) {
					groupsString += ", ";
				}
			}
		}
		groupsString += "]";
		return groupsString;
	}
    
    public String getGroupName() {
        String gg = getGroupMembershipsAsString();

     	
    	String[] list = gg.replace("[", "").replace("]", "").split(",");
    	
    	for (String s: list) {    		
    		if (s.startsWith("OPENCQT_") || s.startsWith(" OPENCQT_")) {
    			String finalGrp  = s.replace("OPENCQT_", "").replace(" OPENCQT_", "").trim().replace(" ", "");
     			    			
    			if (gg.contains("ADMIN"))
    				return "ADMIN";
    			if (finalGrp.contains("MQM"))
    				return "MQM";
    			if (gg.contains("REQUESTOR"))
    				return "REQUESTOR";
    		}
    	}
    	
//        String g = getGroupMembershipsAsString();
//        
//        String[] gp = g.replaceAll("[\\[\\]]", "").split(",");
//        if(gp.length>= 1 && gp[0].length() > 0)
//            return gp[0].replace("OPENCQT_", "");
        return "NO-GROUP";
    }

	/**
	 * This method can be used on selective page parts. "e.g. don't show approve
	 * button if user is not a Manager"
	 * 
	 * @param groupNames
	 * @return
	 */
	public boolean hasGroup(String[] groupNames) {
		if (ENTERPRISE_AD_NONE.equals(this.enterpriseAdCodeListValue)) {
			return true;
		} else {
			boolean retval = false;
            if(this.cmqMappedGroupMemberships != null ) {
                for (String groupName : groupNames) {
                    if (this.cmqMappedGroupMemberships.contains(groupName.trim())) {
                        retval = true;
                        break;
                    }
                }
            }
			return retval;
		}
	}
    public boolean hasGroup(String groupName) {
        return hasGroup(new String[] { groupName });
    }
	
	public boolean hasAccess(HttpServletRequest request) {
		if(ENTERPRISE_AD_NONE.equals(this.enterpriseAdCodeListValue)) {
			return true;
		} else {
			return this.getPermissonByUri(request.getRequestURI());
		}
	}

	private List<String> parseUrlPermissionMappings() {
		List<String> filenames = new ArrayList<>();
		this.urlGroupsList = new ArrayList<>();
		try {
			InputStream in = getResourceAsStream("url-group-mappings.txt");
			BufferedReader br = new BufferedReader(new InputStreamReader(in));
			String line = null;
			while ((line = br.readLine()) != null) {
				if ((line.trim().length() != 0) && !(line.startsWith("#"))) {
					String[] lineParts = line.split("=");
					if ((null != lineParts) && (lineParts.length == 2)) {
						UrlGroups urlGroups = new UrlGroups();
						urlGroups.setUrl(lineParts[0].trim());

						String[] groupsArray = lineParts[1].split(",");
						String[] cleanedGroupsArray = new String[groupsArray.length];
						for (int i = 0; i < groupsArray.length; i++) {
							cleanedGroupsArray[i] = groupsArray[i].trim();
						}
						urlGroups.setGroups(cleanedGroupsArray);
						this.urlGroupsList.add(urlGroups);
					}
				}
			}
		} catch (Exception e) {
			LOG.error("Failed to read url-group-mappings.txt file", e);
		}

		return filenames;
	}

	public boolean getPermissonByUri(String uri) {
		boolean retval = false;
		boolean uriFound = false;
		for (UrlGroups urlGroups : urlGroupsList) {
			if (uri.contains(urlGroups.getUrl())) {
				uriFound = true;
				retval = this.hasGroup(urlGroups.getGroups());
				if (retval) {// match found
					break;
				}
			}
		}
		// if uri is not found in mappings then its a lowest level uri
		// accessible to all
		if (!uriFound) {
			return true;
		} else {
			return retval;
		}
	}
	
	public String getLastModifiedByUserAsString() {
		StringBuilder sb = new StringBuilder();
		sb.append(this.userCn).append(" , ").append(this.userSurName)
			.append(" , ").append(this.userGivenName);
		return sb.toString();
	}
	
	public String getLastModifiedByString() {
		StringBuilder sb = new StringBuilder();
		sb.append("|#|").append(this.userCn).append("|#|").append(this.userGivenName)
			.append("|#|").append(this.userSurName)
			.append("|#|");
		if(CollectionUtils.isNotEmpty(combinedMappedGroupMemberships)) {
			int i = 0;
			for (String combinedMappedGroup : this.combinedMappedGroupMemberships) {
				sb.append(combinedMappedGroup);
				if(++i < this.combinedMappedGroupMemberships.size()) {
					sb.append(",");
				}
			}
		}
		sb.append("|#|");
		return sb.toString();
	}
	
	public String getCombinedMappedGroupMembershipAsString() {
		StringBuilder sb = new StringBuilder();
		if(CollectionUtils.isNotEmpty(combinedMappedGroupMemberships)) {
			int i = 0;
			for (String combinedMappedGroup : this.combinedMappedGroupMemberships) {
				sb.append(combinedMappedGroup);
				if(++i < this.combinedMappedGroupMemberships.size()) {
					sb.append(",");
				}
			}
		}
		return sb.toString();
	}
	
	private InputStream getResourceAsStream(String resource) {
		final InputStream in = getContextClassLoader().getResourceAsStream(resource);
		return in == null ? getClass().getResourceAsStream(resource) : in;
	}

	private ClassLoader getContextClassLoader() {
		return Thread.currentThread().getContextClassLoader();
	}

	public void updatePxedDummyGroupMembershipHeader() {
		this.parseAndSetGroupMemberships();
	}
	
	@PreDestroy
	public void destroy() {
		LOG.info("AuthenticationService pre destroy called for user: " + this.userCn);
		SessionTrack sessionTrack = this.sessionTrackService.updateLogoutInSessionTrack(this.sessionTrackId);
		if (null != sessionTrack) {
			LOG.info("SessionTrack with sessionTrackId:" + this.sessionTrackId + " logged out successfully.");
		}
	}

	private class UrlGroups {
		private String url;
		private String[] groups;

		public String getUrl() {
			return url;
		}

		public void setUrl(String url) {
			this.url = url;
		}

		public String[] getGroups() {
			return groups;
		}

		public void setGroups(String[] groups) {
			this.groups = groups;
		}

	}

	public String getUserCn() {
		return userCn;
	}

	public void setUserCn(String userCn) {
		this.userCn = userCn;
	}

	public String getUserGivenName() {
		return userGivenName;
	}

	public void setUserGivenName(String userGivenName) {
		this.userGivenName = userGivenName;
	}

	public String getUserSurName() {
		return userSurName;
	}

	public void setUserSurName(String userSurName) {
		this.userSurName = userSurName;
	}

	public String getUserEmail() {
		return userEmail;
	}

	public void setUserEmail(String userEmail) {
		this.userEmail = userEmail;
	}

	public List<String> getGroupMemberships() {
		return groupMemberships;
	}

	public void setGroupMemberships(List<String> groupMemberships) {
		this.groupMemberships = groupMemberships;
	}

	public IRefCodeListService getRefCodeListService() {
		return refCodeListService;
	}

	public void setRefCodeListService(IRefCodeListService refCodeListService) {
		this.refCodeListService = refCodeListService;
	}

	public String getGroupMembershipHeader() {
		return groupMembershipHeader;
	}

	public void setGroupMembershipHeader(String groupMembershipHeader) {
		this.groupMembershipHeader = groupMembershipHeader;
	}

	public List<String> getCmqMappedGroupMemberships() {
		return cmqMappedGroupMemberships;
	}

	public void setCmqMappedGroupMemberships(List<String> cmqMappedGroupMemberships) {
		this.cmqMappedGroupMemberships = cmqMappedGroupMemberships;
	}

	public String getEnterpriseAdCodeListValue() {
		return enterpriseAdCodeListValue;
	}

	public void setEnterpriseAdCodeListValue(String enterpriseAdCodeListValue) {
		this.enterpriseAdCodeListValue = enterpriseAdCodeListValue;
	}

	public ISessionTrackService getSessionTrackService() {
		return sessionTrackService;
	}

	public void setSessionTrackService(ISessionTrackService sessionTrackService) {
		this.sessionTrackService = sessionTrackService;
	}
}
