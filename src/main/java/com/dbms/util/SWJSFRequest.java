package com.dbms.util;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.faces.bean.ManagedBean;
import javax.faces.context.FacesContext;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;


@ManagedBean
@ApplicationScoped
public class SWJSFRequest 
{
	public static HttpServletRequest getRequest()
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
	
	public static void setAttribute(String name, Object value)
	{
		HttpServletRequest request = getRequest();
		request.setAttribute(name,value);
	}
	
	public static Object getAttribute(String name)
	{
		HttpServletRequest request = getRequest();
		return request.getAttribute(name);
		
	}

	
	public static Object def(Object value, Object defVal) {
		return value != null ? value : defVal;
	}
	
	
	public static List<String> getGroupList(String name, String key)
	{
		HttpServletRequest request = getRequest();
		String value = (String) request.getAttribute(name);
		return getGroupListValue(key, value);
	}

	public static List<String> getGroupListValue(String key, String value)
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
	
	public static String getGroupListValueAsString(String key, String value) {
		List<String> groupList = getGroupListValue(key, value);
		if (groupList.size() > 0)
			return StringUtils.join(", ", groupList);
		else 
			return null;
	}
	
	public static String getGroupListAsString(String name, String key) {
		List<String> groupList = getGroupList(name, key);
		if (groupList.size() > 0)
			return StringUtils.join(", ", groupList);
		else 
			return null;
	}

	
	
//	IAMPFIZERUSERGROUPMEMBERSHIP-----CN=OPENCQT_ADMIN,OU=CQT_OU,OU=Applications,OU=Delegated,OU=Groups,DC=pxed,DC=pfizer,DC=com:CN=CQT_Users,OU=CQT_OU,OU=Applications,OU=Delegated,OU=Groups,DC=pxed,DC=pfizer,DC=com:CN=GBL-BTNONColleagues,OU=GBLGroups,OU=Applications,OU=Delegated,OU=Groups,DC=pxed,DC=pfizer,DC=com 
			
	
/*	
	public static Object locateObject(String name)
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
	public static void dumpAttributes()
	{
		HttpServletRequest request = getRequest();
		Enumeration e = request.getAttributeNames();
		while(e.hasMoreElements())
		{
			String name = (String)e.nextElement();
			System.out.println(name);
		}
	}
}
