package test;

import java.util.Hashtable;
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

/**
 * Example code for retrieving a Users Primary Group
 * from Microsoft Active Directory via. its LDAP API
 * 
 * @author Adam Retter <adam.retter@googlemail.com>
 */

/*
PFB, PXED LDAP service account details in Dev.

Account                         ----  cn=cqt_admpx,ou=People,dc=pxed,dc=pfizer,dc=com
Server                            ----   pxedv2dev.pfizer.com
SSL Port                        ----   6360
Search base (Base DN)   ----   dc=pxed,dc=pfizer,dc=com
Ldap password               ----   H7IMC85R#@4$

*/

public class LDAPTest {

	public static final String MEMBER_OF = "uniquemember";
	
	/**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws NamingException {
        
        final String ldapAdServer = "ldaps://pxedv2dev.pfizer.com:6360";
        //final String ldapAdServer = "ldap://iamv2dev.pfizer.com:6360";
        
        
        final String ldapSearchBase = "dc=pxed,dc=pfizer,dc=com";
        
        final String ldapUsername = "cn=cqt_admpx,ou=People,dc=pxed,dc=pfizer,dc=com";
        final String ldapPassword = "H7IMC85R#@4$";
        
        final String ldapAccountToLookup = "cougha02";
        
        
        Hashtable<String, Object> env = new Hashtable<String, Object>();
        env.put(Context.SECURITY_AUTHENTICATION, "simple");
        if(ldapUsername != null) {
            env.put(Context.SECURITY_PRINCIPAL, ldapUsername);
        }
        if(ldapPassword != null) {
            env.put(Context.SECURITY_CREDENTIALS, ldapPassword);
        }
        env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
        env.put(Context.PROVIDER_URL, ldapAdServer);

        //ensures that objectSID attribute values
        //will be returned as a byte[] instead of a String
        env.put("java.naming.ldap.attributes.binary", "objectSID");
        
        // the following is helpful in debugging errors
        //env.put("com.sun.jndi.ldap.trace.ber", System.err);
        
        // Create the initial context
        LdapContext ctx = new InitialLdapContext(env, null);
        
        LDAPTest ldap = new LDAPTest();
        
        //1) lookup the ldap account
        SearchResult srLdapGroups = ldap.findAllGroups(ctx, ldapSearchBase, ldapAccountToLookup);
//        SearchResult srLdapUser = ldap.findAccountByAccountName(ctx, ldapSearchBase, ldapAccountToLookup);
        
        //2) get the SID of the users primary group
        //String primaryGroupSID = ldap.getPrimaryGroupSID(srLdapUser);
        
        //3) get the users Primary Group
        //String primaryGroupName = ldap.findGroupBySID(ctx, ldapSearchBase, primaryGroupSID);
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
		    System.out.println("group name: " + userGroup);
		    
		    //Attribute members = attrs.get("uniquemember");
		    
            // Look for and process memberOf
            Attribute memberOf = attrs.get(MEMBER_OF);
            if (memberOf != null) {
                for ( NamingEnumeration<?> e1 = memberOf.getAll() ; e1.hasMoreElements() ; ) {
                    String unprocessedGroupDN = e1.nextElement().toString();
                    String unprocessedGroupCN = getCN(unprocessedGroupDN);
                    System.out.println("-- member: \t" + unprocessedGroupCN);
        		    
        		    findAccountByAccountName(ctx, ldapSearchBase, unprocessedGroupCN);
                }
            }
            
		    System.out.println();
            
/*            
	        for (NamingEnumeration<? extends Attribute> vals = attrs.getAll(); vals.hasMoreElements();) {
	            System.out.println("---\t" + vals.nextElement());
	        }
*/		    
	    }
        return searchResult;
	    
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
    public static String decodeSID(byte[] sid) {
        
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
    public static String getCN(String cnName) {
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

    public static void printSearchEnumeration(NamingEnumeration<SearchResult> retEnum) {
    	
    	final String[] IDs = {
    			"pfizerpreferredname",
    			"displayname",
    			"sn",
    			"givenname"
    	};
    	
	    try {
	        while (retEnum.hasMore()) {
		        SearchResult sr = retEnum.next();
		        System.out.println("\t" + ">>" + sr.getNameInNamespace());
		        
			    Attributes attrs = sr.getAttributes();
			    
			    for (String ID : IDs ) {
			    	System.out.println("\t" + ID + " = " + getAttr(attrs, ID));
			    }
	        }
	    } catch (NamingException e) {
	        e.printStackTrace();
	    }
    }    

    public static String getAttr(Attributes attrs, String key) {
	    //Attributes attrs = sr.getAttributes();
	    String keyStr = attrs.get(key).toString();
	    String [] keyVal = keyStr.split(":");
	    String ret = keyVal[1];
	    return ret;
    }

}
