<%@ page contentType="text/html; charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<link rel="stylesheet" href="<c:url value='/static/css/pages.css' />">
<link rel="stylesheet" href="<c:url value='/static/css/tables.css' />">
<div class="table-container">
    <table class="liquid-table striped">
        <thead>
            <tr>
                <th>
                    <label class="checkbox-wrapper">
                        <input type="checkbox" id="selectAll">
                        <span class="checkbox-custom"></span>
                    </label>
                </th>
                <th>订单号 <span class="sort-icon">▼</span></th>
                <th>客户名称</th>
                <th>金额</th>
                <th>状态</th>
                <th>操作</th>
            </tr>
        </thead>
        <tbody>
            <tr>
                <td>
                    <label class="checkbox-wrapper">
                        <input type="checkbox">
                        <span class="checkbox-custom"></span>
                    </label>
                </td>
                <td>#2024001</td>
                <td>张三科技有限公司</td>
                <td>¥12,580.00</td>
                <td><span class="tag tag-success">已完成</span></td>
                <td>
                    <button class="action-btn">查看</button>
                    <button class="action-btn">编辑</button>
                </td>
            </tr>
            <tr>
                <td>
                    <label class="checkbox-wrapper">
                        <input type="checkbox">
                        <span class="checkbox-custom"></span>
                    </label>
                </td>
                <td>#2024002</td>
                <td>李四贸易集团</td>
                <td>¥8,960.00</td>
                <td><span class="tag tag-warning">处理中</span></td>
                <td>
                    <button class="action-btn">查看</button>
                    <button class="action-btn">编辑</button>
                </td>
            </tr>
        </tbody>
    </table>
    
    <!-- 分页 -->
    <div class="table-pagination">
        <button disabled>上一页</button>
        <button class="active">1</button>
        <button>2</button>
        <button>3</button>
        <button>...</button>
        <button>10</button>
        <button>下一页</button>
        <span class="page-info">共 100 条记录</span>
    </div>
</div>