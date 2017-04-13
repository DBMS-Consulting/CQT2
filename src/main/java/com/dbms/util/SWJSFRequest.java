package com.dbms.util;

import java.util.ArrayList;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.faces.bean.ManagedBean;
import javax.faces.context.FacesContext;
import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;
import javax.naming.ldap.InitialLdapContext;
import javax.naming.ldap.LdapContext;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;


@ManagedBean(name="appSWJSFRequest")
@ApplicationScoped
public class SWJSFRequest 
{
    public static final String MEMBER_OF = "uniquemember";
    public static final String LDAP_AD_SERVER = "ldaps://pxedv2dev.pfizer.com:6360"; //"ldap://iamv2dev.pfizer.com:6360";
    public static final String LDAP_SEARCH_BASE = "dc=pxed,dc=pfizer,dc=com";
    public static final String LDAP_USERNAME = "cn=cqt_admpx,ou=People,dc=pxed,dc=pfizer,dc=com";
    public static final String LDAP_PASSWORD = "H7IMC85R#@4$";
    public static final String LDAP_ACCOUNT_TO_LOOKUP = "cougha02";
    
    
    public LdapContext initLdapContext() throws NamingException {
        Hashtable<String, Object> env = new Hashtable<String, Object>();
        env.put(Context.SECURITY_AUTHENTICATION, "simple");
        if(LDAP_USERNAME != null) {
            env.put(Context.SECURITY_PRINCIPAL, LDAP_USERNAME);
        }
        if(LDAP_PASSWORD != null) {
            env.put(Context.SECURITY_CREDENTIALS, LDAP_PASSWORD);
        }
        env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
        env.put(Context.PROVIDER_URL, LDAP_AD_SERVER);

        //ensures that objectSID attribute values
        //will be returned as a byte[] instead of a String
        env.put("java.naming.ldap.attributes.binary", "objectSID");
        
        // the following is helpful in debugging errors
        //env.put("com.sun.jndi.ldap.trace.ber", System.err);
        
        // Create the initial context
        LdapContext ctx = new InitialLdapContext(env, null);
        return ctx;
    }
        
	public HttpServletRequest getRequest()
	{
		HttpServletRequest request = 
			(HttpServletRequest)FacesContext
				.getCurrentInstance()
					.getExternalContext()
						.getRequest();
		if (request == null)
		{
			throw new RuntimeException("Sorry. Got a null request from faces context");
		}
		return request;
	}
	
	public void setAttribute(String name, Object value)
	{
		HttpServletRequest request = getRequest();
		request.setAttribute(name,value);
	}
	
	public Object getAttribute(String name)
	{
		HttpServletRequest request = getRequest();
		return request.getAttribute(name);
		
	}

	
	public Object def(Object value, Object defVal) {
		return value != null ? value : defVal;
	}
	
	
	public List<String> getGroupList(String name, String key)
	{
		HttpServletRequest request = getRequest();
		String value = (String) request.getAttribute(name);
		return getGroupListValue(key, value);
	}

	public List<String> getGroupListValue(String key, String value)
	{
		//value = "CN=OPENCQT_ADMIN,OU=CQT_OU,OU=Applications,OU=Delegated,OU=Groups,DC=pxed,DC=pfizer,DC=com:CN=CQT_Users,OU=CQT_OU,OU=Applications,OU=Delegated,OU=Groups,DC=pxed,DC=pfizer,DC=com:CN=GBL-BTNONColleagues,OU=GBLGroups,OU=Applications,OU=Delegated,OU=Groups,DC=pxed,DC=pfizer,DC=com";
		//System.out.println("name: " + name);
		System.out.println("key: " + key);
		System.out.println("value: " + value);

		List<String> ret = new ArrayList<String>();
		
		if (value != null) {		
			String[] arr = value.split("[\\s,:]+");
			for (String s : arr) {
				if (s.startsWith(key + "=")) {
					String[] v1 = s.split("=");
					ret.add(v1[1]);
				}
			}
		}
		
		return ret;		
	}
	
	public String getGroupListValueAsString(String key, String value) {
		List<String> groupList = getGroupListValue(key, value);
		if (groupList.size() > 0)
			return StringUtils.join(", ", groupList);
		else 
			return null;
	}
	
	public String getGroupListAsString(String name, String key) {
		List<String> groupList = getGroupList(name, key);
		if (groupList.size() > 0)
			return StringUtils.join(", ", groupList);
		else 
			return null;
	}

	
	
//	IAMPFIZERUSERGROUPMEMBERSHIP-----CN=OPENCQT_ADMIN,OU=CQT_OU,OU=Applications,OU=Delegated,OU=Groups,DC=pxed,DC=pfizer,DC=com:CN=CQT_Users,OU=CQT_OU,OU=Applications,OU=Delegated,OU=Groups,DC=pxed,DC=pfizer,DC=com:CN=GBL-BTNONColleagues,OU=GBLGroups,OU=Applications,OU=Delegated,OU=Groups,DC=pxed,DC=pfizer,DC=com 
			
	
/*	
	public Object locateObject(String name)
	{
		System.out.println("Trying to locate object:" + name);
		HttpServletRequest request = getRequest();
		Object o = request.getAttribute(name);
		if(o != null) return o;
		
		//HttpSession session = SWJSFSession.getSession();
		//o = session.getAttribute(name);
		//if(o != null) return o;

		FacesContext facescontext = FacesContext.getCurrentInstance();
		String address = "#{requestScope." + name + "}";
		System.out.println("Trying to locate object:" + address);
		
		o = facescontext.getApplication().createValueBinding(address).getValue(facescontext);
		if(o != null) return o;
		
		return null;
	}
*/	
	public void dumpAttributes()
	{
		HttpServletRequest request = getRequest();
		Enumeration e = request.getAttributeNames();
		while(e.hasMoreElements())
		{
			String name = (String)e.nextElement();
			System.out.println(name);
		}
	}
    
    public SearchResult findAllGroups() throws NamingException {
        LdapContext ctx = initLdapContext();
        //1) lookup the ldap account
        return findAllGroups(ctx, LDAP_SEARCH_BASE, LDAP_ACCOUNT_TO_LOOKUP);
    }
    
    public SearchResult findAllGroups(DirContext ctx, String ldapSearchBase, String accountName) throws NamingException {
    	SearchResult searchResult = null;
    	SearchControls ctls = new SearchControls();
	    //String[] attrIDs = { "cn", "memberOf" };	    
	    //ctls.setReturningAttributes(attrIDs);
	    //ctls.setSearchScope(SearchControls.ONELEVEL_SCOPE);
	    ctls.setSearchScope(SearchControls.SUBTREE_SCOPE);
        
        //String searchFilter = "(objectclass=pfizerGroup)";
        String searchFilter = "(&(objectclass=pfizerGroup)(cn=opencqt*))";
        //String searchFilter = "(&(objectCategory=group)(cn=opencqt*))";
        
	    
	    NamingEnumeration<SearchResult> answer = ctx.search(ldapSearchBase, searchFilter, ctls);
	    while (answer.hasMore()) {
		    SearchResult rslt = answer.next();
		    searchResult = rslt;		    
		    Attributes attrs = rslt.getAttributes();
		    String groups = attrs.get("cn").toString();
		    String [] groupname = groups.split(":");
		    String userGroup = groupname[1];
		    System.out.println(attrs.get("cn"));
		    
		    //Attribute members = attrs.get("uniquemember");
		    
            // Look for and process memberOf
            Attribute memberOf = attrs.get(MEMBER_OF);
            if (memberOf != null) {
                for ( NamingEnumeration<?> e1 = memberOf.getAll() ; e1.hasMoreElements() ; ) {
                    String unprocessedGroupDN = e1.nextElement().toString();
                    String unprocessedGroupCN = getCN(unprocessedGroupDN);
        		    System.out.println("--\t" + unprocessedGroupCN);
                }
            }
            
/*            
	        for (NamingEnumeration<? extends Attribute> vals = attrs.getAll(); vals.hasMoreElements();) {
	            System.out.println("---\t" + vals.nextElement());
	        }
*/		    
	    }
        return searchResult;
	    
    }    

    public SearchResult findAccountByAccountName() throws NamingException {
        LdapContext ctx = initLdapContext();
        return findAccountByAccountName(ctx, LDAP_SEARCH_BASE, LDAP_ACCOUNT_TO_LOOKUP);
    }
    
        
    public SearchResult findAccountByAccountName(DirContext ctx, String ldapSearchBase, String accountName) throws NamingException {

//        String searchFilter = "(&(objectClass=user)(sAMAccountName=" + accountName + "))";

        String searchFilter = "(&(objectClass=pfizerperson)(cn=" + accountName + "))";

    	
        SearchControls searchControls = new SearchControls();
        searchControls.setSearchScope(SearchControls.SUBTREE_SCOPE);

        NamingEnumeration<SearchResult> results = ctx.search(ldapSearchBase, searchFilter, searchControls);
        // Print the answer
        printSearchEnumeration(results);
        
        SearchResult searchResult = null;
        if(results.hasMoreElements()) {
             searchResult = (SearchResult) results.nextElement();

            //make sure there is not another item available, there should be only 1 match
            if(results.hasMoreElements()) {
                System.err.println("Matched multiple users for the accountName: " + accountName);
                return null;
            }
        }
        
        return searchResult;
    }
    
    
    public String findGroupBySID(DirContext ctx, String ldapSearchBase, String sid) throws NamingException {
        
        String searchFilter = "(&(objectClass=pfizergroup)(objectSid=" + sid + "))";

        SearchControls searchControls = new SearchControls();
        searchControls.setSearchScope(SearchControls.SUBTREE_SCOPE);
        
        NamingEnumeration<SearchResult> results = ctx.search(ldapSearchBase, searchFilter, searchControls);

        if(results.hasMoreElements()) {
            SearchResult searchResult = (SearchResult) results.nextElement();

            //make sure there is not another item available, there should be only 1 match
            if(results.hasMoreElements()) {
                System.err.println("Matched multiple groups for the group with SID: " + sid);
                return null;
            } else {
                return (String)searchResult.getAttributes().get("sAMAccountName").get();
            }
        }
        return null;
    }
    
    public String getPrimaryGroupSID(SearchResult srLdapUser) throws NamingException {
        byte[] objectSID = (byte[])srLdapUser.getAttributes().get("pfizerntdomainname").get();
        String strPrimaryGroupID = (String)srLdapUser.getAttributes().get("primaryGroupID").get();
        
        String strObjectSid = decodeSID(objectSID);
        
        return strObjectSid.substring(0, strObjectSid.lastIndexOf('-') + 1) + strPrimaryGroupID;
    }
    
    /**
     * The binary data is in the form:
     * byte[0] - revision level
     * byte[1] - count of sub-authorities
     * byte[2-7] - 48 bit authority (big-endian)
     * and then count x 32 bit sub authorities (little-endian)
     * 
     * The String value is: S-Revision-Authority-SubAuthority[n]...
     * 
     * Based on code from here - http://forums.oracle.com/forums/thread.jspa?threadID=1155740&tstart=0
     */
    public String decodeSID(byte[] sid) {
        
        final StringBuilder strSid = new StringBuilder("S-");

        // get version
        final int revision = sid[0];
        strSid.append(Integer.toString(revision));
        
        //next byte is the count of sub-authorities
        final int countSubAuths = sid[1] & 0xFF;
        
        //get the authority
        long authority = 0;
        //String rid = "";
        for(int i = 2; i <= 7; i++) {
           authority |= ((long)sid[i]) << (8 * (5 - (i - 2)));
        }
        strSid.append("-");
        strSid.append(Long.toHexString(authority));
        
        //iterate all the sub-auths
        int offset = 8;
        int size = 4; //4 bytes for each sub auth
        for(int j = 0; j < countSubAuths; j++) {
            long subAuthority = 0;
            for(int k = 0; k < size; k++) {
                subAuthority |= (long)(sid[offset + k] & 0xFF) << (8 * k);
            }
            
            strSid.append("-");
            strSid.append(subAuthority);
            
            offset += size;
        }
        
        return strSid.toString();    
    }
    
    
    /*
     * Prepares and returns CN that can be used for AD query
     * e.g. Converts "CN=**Dev - Test Group" to "**Dev - Test Group"
     * Converts CN=**Dev - Test Group,OU=Distribution Lists,DC=DOMAIN,DC=com to "**Dev - Test Group"
     */
    public String getCN(String cnName) {
        if (cnName != null && cnName.toUpperCase().startsWith("CN=")) {
            cnName = cnName.substring(3);
        }
        int position = cnName.indexOf(',');
        if (position == -1) {
            return cnName;
        } else {
            return cnName.substring(0, position);
        }
    }

    public void printSearchEnumeration(NamingEnumeration<SearchResult> retEnum) {
	    try {
	        while (retEnum.hasMore()) {
	        SearchResult sr = retEnum.next();
	        System.out.println(">>" + sr.getNameInNamespace());
	        }
	    } catch (NamingException e) {
	        e.printStackTrace();
	    }
    }
    
    public String extractGroupName(String groupMembership, String defaultGroup) {
        String g;
        try {
            g = def(getGroupListValueAsString("CN", groupMembership), null).toString();
        } catch (Exception e) {
            g = null;
        }
        if(g == null)
            return defaultGroup;
        String[] gp = g.replaceAll("[\\[\\]]", "").split(",");
        if(gp.length>= 2)
            return gp[1].replace("OPENCQT_", "");
        return defaultGroup;
    }
    
}
