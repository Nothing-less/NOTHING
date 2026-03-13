<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<link rel="stylesheet" href="<c:url value='/static/css/pages.css' />">
<!-- 商品管理 -->
<div class="card" style="grid-column: 1 / -1;">
    <div class="card-header">
        <span class="card-title">商品列表</span>
        <button class="btn-primary">+ 新增商品</button>
    </div>
    <div class="product-grid">
        <div class="product-item">
            <div class="product-image">🛍️</div>
            <div class="product-info">
                <h4>示例商品</h4>
                <p class="price">¥99.00</p>
                <p class="stock">库存: 100</p>
            </div>
        </div>
    </div>
</div>
