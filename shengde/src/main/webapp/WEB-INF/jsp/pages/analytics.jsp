<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Analytics</title>
    <link rel="stylesheet" href="<c:url value='/static/css/pages.css' />">
</head>
<body>
    <!-- 数据分析 -->
    <div class="card" style="grid-column: 1 / -1;">
        <div class="card-header">
            <span class="card-title">数据图表</span>
        </div>
        <div class="chart-container">
            <div class="chart-placeholder">
                <div style="font-size: 48px; margin-bottom: 20px;">📊</div>
                <h3>图表区域</h3>
                <p class="text-muted">这里将展示各种数据图表</p>
            </div>
        </div>
    </div>
</body>
</html>