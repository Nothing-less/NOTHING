<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Settings</title>
    <link rel="stylesheet" href="<c:url value='/static/css/pages.css' />">
</head>
<body>
    <!-- 系统设置 -->
    <div class="card" style="grid-column: 1 / -1;">
        <div class="card-header">
            <span class="card-title">基本设置</span>
        </div>
        <form class="settings-form">
            <div class="form-group">
                <label>网站名称</label>
                <input type="text" value="管理系统" class="form-input">
            </div>
            <div class="form-group">
                <label>系统邮箱</label>
                <input type="email" value="admin@example.com" class="form-input">
            </div>
            <div class="form-group">
                <label>每页显示条数</label>
                <select class="form-input">
                    <option>10</option>
                    <option selected>20</option>
                    <option>50</option>
                </select>
            </div>
            <button type="submit" class="btn-primary">保存设置</button>
        </form>
    </div>
</body>
</html>