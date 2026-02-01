<%@ page contentType="text/html; charset=UTF-8" language="java" %>
<%
    String username = request.getParameter("username");
    String password = request.getParameter("password");
    String token = (String) request.getAttribute("token");
    out.println("用户名：" + username + "<br>");
    out.println("密码：" + password + "<br>");
    out.println("请求方法：" + request.getMethod()+ "<br>");
    out.println("Token: " + token + "<br>");
%>