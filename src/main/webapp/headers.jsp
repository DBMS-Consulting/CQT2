<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
<%@page import="java.util.*"%>
<%@page import="java.io.*"%>
<%@page import="javax.mail.*"%>
<%@page import="javax.mail.internet.MimeUtility"%>
<%@page import="javax.xml.bind.DatatypeConverter"%>

<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<body>
<%! 
	public static String toHexadecimal(String text) throws UnsupportedEncodingException
	{
	    byte[] myBytes = text.getBytes("UTF-8");
	
	    return DatatypeConverter.printHexBinary(myBytes);
	}

	private Map<String, String> getHeadersInfo(HttpServletRequest request) {
	
	    Map<String, String> map = new HashMap<String, String>();
	
	    Enumeration headerNames = request.getHeaderNames();
	    while (headerNames.hasMoreElements()) {
	        String key = (String) headerNames.nextElement();
	        String value = request.getHeader(key);
	        map.put(key, value);
	    }
	
	    return map;
	}

	private static final String ENCODED_TOKEN_MARKER = "=?";
    private static final String ENCODED_TOKEN_FINISHER = "?=";	
%>
<%
String headerName;
String headerValue;
Enumeration e = request.getHeaderNames();
while(e.hasMoreElements()) {
    headerName = (String)e.nextElement();
    headerValue = request.getHeader(headerName);
	if (headerValue.startsWith(ENCODED_TOKEN_MARKER) && headerValue.endsWith(ENCODED_TOKEN_FINISHER))
    	out.println(headerName +"-----"+ MimeUtility.decodeWord(headerValue) + " [" + toHexadecimal(headerValue) + "]");
	else
    	out.println(headerName +"-----"+ headerValue + " [" + toHexadecimal(headerValue) + "]");
	
	
	out.println("<br>");
}

%>
</body>

<!-- 
IAMPFIZERUSERGROUPMEMBERSHIP
IAMPFIZERUSERCN
IAMPFIZERUSERSURNAME
IAMPFIZERUSERGIVENNAME
IAMPFIZERUSERINTERNETEMAILADDRESS
 -->

</html>
