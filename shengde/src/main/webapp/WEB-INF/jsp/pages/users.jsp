<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Users</title>
    <link rel="stylesheet" href="<c:url value='/static/css/pages.css' />">
</head>
<body>
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
                    <td><%= currentUser != null ? currentUser.getUserAccount() : "admin" %></td>
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
</body>
</html>