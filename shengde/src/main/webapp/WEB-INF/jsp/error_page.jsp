<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="icu.nothingless.pojo.commons.RespEntity" %>
<%@ page import="java.util.Map" %>

<%
    RespEntity<Object> respEntity = (RespEntity<Object>)request.getAttribute("resp");
    String backUrl = "";
%>

<!DOCTYPE html>
<html lang="zh-CN">
<body>
    <div class="card">
        <div class="failed-icon"> X </div>
        <h1><%= respEntity.getMessage() %></h1>
        <div class="message">
            出现问题: <%= respEntity.getData() != null ? "，相关数据如下" : "" %>
        </div>
        
        <% if (respEntity.getData() != null) { %>
        <div class="data-preview">
            <%= respEntity.getData().toString() %>
        </div>
        <% } %>
        
        <a href="<%= backUrl != null ? backUrl : request.getContextPath() + "/" %>" class="btn">
            ← 返回
        </a>
    </div>
</body>
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Error Occurred!</title>
    <style>
        * { margin: 0; padding: 0; box-sizing: border-box; }
        body {
            font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, sans-serif;
            background: linear-gradient(135deg, #10b98115 0%, #05966905 100%);
            min-height: 100vh;
            display: flex;
            align-items: center;
            justify-content: center;
            padding: 20px;
        }
        .card {
            background: white;
            border-radius: 24px;
            box-shadow: 0 20px 60px rgba(16, 185, 129, 0.15);
            max-width: 500px;
            width: 100%;
            padding: 50px;
            text-align: center;
            animation: scaleIn 0.5s ease;
        }
        @keyframes scaleIn {
            from { opacity: 0; transform: scale(0.9); }
            to { opacity: 1; transform: scale(1); }
        }
        .failed-icon {
            width: 80px;
            height: 80px;
            background: linear-gradient(135deg, #10b981 0%, #059669 100%);
            border-radius: 50%;
            display: flex;
            align-items: center;
            justify-content: center;
            margin: 0 auto 25px;
            font-size: 40px;
            color: white;
            animation: checkmark 0.6s ease;
        }
        @keyframes checkmark {
            0% { transform: scale(0) rotate(-45deg); }
            50% { transform: scale(1.2) rotate(10deg); }
            100% { transform: scale(1) rotate(0); }
        }
        h1 { color: #059669; font-size: 28px; margin-bottom: 15px; }
        .message { color: #6b7280; font-size: 16px; margin-bottom: 30px; line-height: 1.6; }
        .data-preview {
            background: #f0fdf4;
            border-radius: 12px;
            padding: 20px;
            margin-bottom: 30px;
            text-align: left;
            font-family: monospace;
            font-size: 13px;
            color: #166534;
            max-height: 200px;
            overflow: auto;
        }
        .btn {
            display: inline-flex;
            align-items: center;
            gap: 8px;
            padding: 14px 32px;
            background: linear-gradient(135deg, #10b981 0%, #059669 100%);
            color: white;
            text-decoration: none;
            border-radius: 10px;
            font-weight: 600;
            transition: transform 0.2s, box-shadow 0.2s;
        }
        .btn:hover {
            transform: translateY(-2px);
            box-shadow: 0 10px 30px rgba(16, 185, 129, 0.3);
        }
    </style>
</head>

</html>