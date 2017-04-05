<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
<%@page import="java.util.*"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<body>
<%
String headerName;
String headerValue;
Enumeration e = request.getHeaderNames();
while(e.hasMoreElements()) {
    headerName = (String)e.nextElement();
    headerValue = request.getHeader(headerName);
out.println(headerName +"-----"+ headerValue);
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
