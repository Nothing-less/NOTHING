<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="icu.nothingless.dto.UserDTO" %>
<%@ page import="icu.nothingless.commons.RespEntity" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ page import="icu.nothingless.tools.ViewUtil" %>
<!DOCTYPE html>
<%
    // 获取登录用户信息（从 Session 或 request）
    UserDTO currentUser = (UserDTO) request.getSession(false).getAttribute("CURRENT_USER");
    if (currentUser == null) {
        request.setAttribute("respEntity",RespEntity.error("错误！系统出现异常！"));
        ViewUtil.render(request,response,"error_page");
        return;
    }
    // 获取当前菜单（从参数或默认）
    String currentMenu = (String)request.getSession(false).getAttribute("menu");
    if (currentMenu == null) currentMenu = "dashboard";
    String safeUserAccount = org.apache.commons.text.StringEscapeUtils.escapeHtml4(currentUser.getUserAccount());
%>

<link rel="stylesheet" href="<c:url value='/static/css/pages.css' />">
<!-- 用户管理 -->
<div class="card" style="grid-column: 1 / -1;">
    <div class="card-header">
        <span class="card-title">用户列表</span>
        <button class="btn-primary" onclick="">+ 新增用户</button>
    </div>
    <table class="data-table">
        <thead>
            <tr>
                <th>用户账号</th>
                <th>最后登录</th>
                <th>状态</th>
                <th>操作</th>
            </tr>
        </thead>
        <tbody>
            <tr>
                <td><%= currentUser != null ? currentUser.getUserAccount() : "Wrong User Account!" %></td>
                <td class="text-muted"><%= currentUser != null ? currentUser.getLastLoginTime() : "2024-01-01" %></td>
                <td><span class="status-online">● 在线</span></td>
                <td>
                    <a href="#" class="action-edit">编辑</a>
                    <a href="#" class="action-delete">禁用</a>
                </td>
            </tr>
        </tbody>
    </table>
</div>
