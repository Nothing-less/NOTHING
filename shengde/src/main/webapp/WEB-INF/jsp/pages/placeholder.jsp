<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ page import="java.net.URLDecoder" %>
<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>PlaceHolder</title>
    <link rel="stylesheet" href="<c:url value='/static/css/pages.css' />">
</head>
<body>
    <%
        String menu = request.getParameter("menu");
        String menuName = menu != null ? URLDecoder.decode(menu, "UTF-8") : "未知";
    %>
    <div class="card" style="grid-column: 1 / -1; text-align: center; padding: 60px;">
        <div style="font-size: 48px; margin-bottom: 20px;">🚧</div>
        <h3 style="color: var(--text-light); margin-bottom: 10px;">功能开发中</h3>
        <p style="color: var(--text-muted);"><%= menuName %> 模块即将上线</p>
    </div>
</body>
</html>