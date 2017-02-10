
<%
	String contextPath = request.getContextPath();
	String themeName = "default";
	String imgPath = contextPath + "/theme/" + themeName +"/images";
	String cssPath = contextPath + "/theme/" + themeName + "/css";
	session.setAttribute("imgPath",imgPath);
%>
