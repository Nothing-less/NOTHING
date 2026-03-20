<%@ page contentType="text/html; charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ page import="icu.nothingless.commons.RespEntity" %>
<%@ page import="icu.nothingless.dto.UserDTO" %>
<%@ page import="icu.nothingless.tools.ViewUtil" %>
<%@ page import="icu.nothingless.tools.RedirectUtil" %>
<%@ page import="java.util.Map" %>
<%
    UserDTO currentUser = (UserDTO) request.getSession(false).getAttribute("CURRENT_USER");
    if (currentUser == null) {
        request.setAttribute("respEntity", RespEntity.error("错误！系统出现异常！"));
        ViewUtil.render(request, response, "error_page");
        return;
    }
    String contextPath = request.getContextPath();
%>
<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>主页</title>
    <link rel="stylesheet" href="<c:url value='/static/css/pages.css' />">
    <link rel="stylesheet" href="<c:url value='/static/css/tables.css' />">
</head>
<body data-api-base="<%= contextPath %>">
    
    <div class="particles" id="particles"></div>
    
    <div class="sidebar-container" id="sidebar">
        <div class="sidebar">
            <div class="user-profile" id="userProfile">
                <div class="avatar" id="userAvatar">?</div>
                <div class="username" id="userName">加载中...</div>
                <div class="user-role" id="userRole">-</div>
            </div>
            
            <nav class="menu" id="dynamicMenu">
                <div class="menu-loading">
                    <div class="loading"></div>
                    <div style="margin-top: 10px;">加载菜单中...</div>
                </div>
            </nav>
            
            <div class="sidebar-footer">
                <a href="<%= contextPath %>/logout" id="logoutLink" class="logout-btn">
                    <span class="menu-icon">🚪</span>
                    <span class="menu-text">退出登录</span>
                </a>
            </div>
        </div>
    </div>
    
    <main class="main-content">
        <div class="top-bar">
            <div>
                <h1 class="page-title" id="pageTitle">加载中...</h1>
                <div class="breadcrumb">
                    <a href="javascript:void(0)" onclick="App.loadFirstPage()">首页</a>
                    <span>/</span>
                    <span id="breadcrumbCurrent">加载中...</span>
                </div>
            </div>
            <div class="server-time" id="serverTime">--:--:--</div>
        </div>
        
        <div class="content-wrapper" id="contentWrapper">
            <div class="iframe-loading" id="iframeLoading">
                <div class="loading"></div>
            </div>
            <iframe class="content-iframe" id="contentFrame" name="contentFrame"></iframe>
        </div>
    </main>
    
    <button class="fab" onclick="App.toggleSidebar()" title="切换侧边栏 (Alt+S)">☰</button>
    
    <script src="<c:url value='/static/js/home.js' />" /> </script>
</body>
</html>