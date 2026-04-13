<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<link rel="stylesheet" href="<c:url value='/static/css/pages.css' />">
<!-- 订单管理 -->
<div class="card" style="grid-column: 1 / -1;">
    <div class="card-header">
        <span class="card-title">订单列表</span>
        <button class="btn-primary">+ 新建订单</button>
    </div>
    <div class="filter-bar">
        <input type="text" placeholder="搜索订单号..." class="search-input">
        <select class="filter-select">
            <option>全部状态</option>
            <option>待付款</option>
            <option>已付款</option>
            <option>已发货</option>
            <option>已完成</option>
        </select>
    </div>
    <table class="data-table">
        <thead>
            <tr>
                <th>订单号</th>
                <th>客户</th>
                <th>金额</th>
                <th>状态</th>
                <th>创建时间</th>
                <th>操作</th>
            </tr>
        </thead>
        <tbody>
            <tr>
                <td>ORD-2024-001</td>
                <td>张三</td>
                <td>¥1,299</td>
                <td><span class="badge-success">已完成</span></td>
                <td class="text-muted">2024-01-15 14:30</td>
                <td>
                    <a href="#" class="action-edit">查看</a>
                </td>
            </tr>
        </tbody>
    </table>
</div>
