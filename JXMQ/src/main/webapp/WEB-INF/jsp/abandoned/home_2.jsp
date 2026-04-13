<%@ page contentType="text/html; charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<%@ page import="icu.nothingless.commons.RespEntity" %>
<%@ page import="icu.nothingless.dto.UserDTO" %>
<%@ page import="icu.nothingless.tools.ViewUtil" %>
<!DOCTYPE html>
<%
    // 获取登录用户信息（从 Session 或 request）
    UserDTO currentUser = (UserDTO) request.getSession().getAttribute("CURRENT_USER");
    if (currentUser == null) {
        request.setAttribute("respEntity",RespEntity.error("错误！系统出现异常！"));
        ViewUtil.render(request,response,"error_page");
        return;
    }
    // 获取当前菜单（从参数或默认）
    String currentMenu = (String)request.getSession().getAttribute("menu");
    if (currentMenu == null) currentMenu = "dashboard";
%>
<html>
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>主页 - <%= currentUser.getUserAccount() %></title>
    <link rel="stylesheet" href="<c:url value='/static/css/home.css' />">
</head>
<body>
    <!-- 背景粒子 -->
    <div class="particles" id="particles"></div>

    <!-- 侧边栏 -->
    <div class="sidebar-container" id="sidebar">
        <div class="sidebar-trigger"></div>
        <div class="sidebar-collapsed-indicator"></div>
        
        <div class="sidebar">
            <!-- 用户信息 -->
            <div class="user-profile">
                <div class="avatar">
                    <%= currentUser.getUserAccount().substring(0, 1).toUpperCase() %>
                </div>
                <div class="username"><%= currentUser.getUserAccount() %></div>
                <div class="user-role"><%= currentUser.getRoleId() %></div>
            </div>

            <!-- 菜单 -->
            <nav class="menu">
                <a href="javascript:void(0)" onclick="loadPage('dashboard')" class="menu-item" id="menu-dashboard">
                    <span class="menu-icon">📊</span>
                    <span class="menu-text">数据概览</span>
                </a>
                <a href="javascript:void(0)" onclick="loadPage('users')" class="menu-item" id="menu-users">
                    <span class="menu-icon">👥</span>
                    <span class="menu-text">用户管理</span>
                </a>
                <a href="javascript:void(0)" onclick="loadPage('orders')" class="menu-item" id="menu-orders">
                    <span class="menu-icon">📦</span>
                    <span class="menu-text">订单管理</span>
                </a>
                <a href="javascript:void(0)" onclick="loadPage('products')" class="menu-item" id="menu-products">
                    <span class="menu-icon">🛍️</span>
                    <span class="menu-text">商品管理</span>
                </a>
                <a href="javascript:void(0)" onclick="loadPage('analytics')" class="menu-item" id="menu-analytics">
                    <span class="menu-icon">📈</span>
                    <span class="menu-text">数据分析</span>
                </a>
                <a href="javascript:void(0)" onclick="loadPage('settings')" class="menu-item" id="menu-settings">
                    <span class="menu-icon">⚙️</span>
                    <span class="menu-text">系统设置</span>
                </a>
            </nav>

            <!-- 底部 -->
            <div class="sidebar-footer">
                <a href="<%= request.getContextPath() %>/logout" class="logout-btn">
                    <span class="menu-icon">🚪</span>
                    <span class="menu-text">退出登录</span>
                </a>
            </div>
        </div>
    </div>

    <!-- 主内容 -->
    <main class="main-content">
        <!-- 顶部栏 -->
        <div class="top-bar">
            <div>
                <h1 class="page-title" id="pageTitle">数据概览</h1>
                <div class="breadcrumb">
                    <a href="javascript:void(0)" onclick="loadPage('dashboard')">首页</a>
                    <span>/</span>
                    <span id="breadcrumbCurrent">数据概览</span>
                </div>
            </div>
            <div style="color: var(--text-muted); font-size: 14px;" id="serverTime">
                --:--:--
            </div>

            <script>
            (function() {
                const el = document.getElementById('serverTime');
                let offset = 0;
                
                function fetchTime() {
                    fetch('<%= request.getContextPath() %>/api/time')
                        .then(r => r.json())
                        .then(data => {
                            offset = data.timestamp - Date.now();
                            el.textContent = data.datetime;
                        })
                        .catch(() => {
                            el.textContent = new Date().toLocaleString('zh-CN', {
                                year: 'numeric',
                                month: '2-digit',
                                day: '2-digit',
                                hour: '2-digit',
                                minute: '2-digit',
                                second: '2-digit',
                                hour12: false
                            }).replace(/\//g, '-');
                        });
                }
                
                function update() {
                    const now = new Date(Date.now() + offset);
                    const datetime = now.toLocaleString('zh-CN', {
                        year: 'numeric',
                        month: '2-digit',
                        day: '2-digit',
                        hour: '2-digit',
                        minute: '2-digit',
                        second: '2-digit',
                        hour12: false
                    }).replace(/\//g, '-');
                    el.textContent = datetime;
                }
                
                fetchTime();
                setInterval(update, 1000);
                setInterval(fetchTime, 30000);
            })();
            </script>
        </div>

        <!-- 动态内容区 - 使用 iframe -->
        <div class="content-wrapper" id="contentWrapper">
            <div class="iframe-loading" id="iframeLoading">
                <div class="loading"></div>
            </div>
            <iframe class="content-iframe" id="contentFrame" name="contentFrame"></iframe>
        </div>
    </main>

    <!-- 快捷按钮 -->
    <button class="fab" onclick="toggleSidebar()" title="切换侧边栏">☰</button>

    <script>
        // 生成背景粒子
        function createParticles() {
            const container = document.getElementById('particles');
            for (let i = 0; i < 50; i++) {
                const particle = document.createElement('div');
                particle.className = 'particle';
                particle.style.left = Math.random() * 100 + '%';
                particle.style.animationDelay = Math.random() * 20 + 's';
                particle.style.animationDuration = (15 + Math.random() * 10) + 's';
                container.appendChild(particle);
            }
        }
        createParticles();

        // 菜单标题映射
        const menuTitles = {
            'dashboard': '数据概览',
            'users': '用户管理',
            'orders': '订单管理',
            'products': '商品管理',
            'analytics': '数据分析',
            'settings': '系统设置'
        };

        // 页面 URL 映射（根据您的实际路径调整）
        const pageUrls = {
            'dashboard': '<%= request.getContextPath() %>/home/dashboard',
            'users': '<%= request.getContextPath() %>/home/users',
            'orders': '<%= request.getContextPath() %>/home/orders',
            'products': '<%= request.getContextPath() %>/home/products',
            'analytics': '<%= request.getContextPath() %>/home/analytics',
            'settings': '<%= request.getContextPath() %>/home/settings'
        };

        // 当前激活的菜单
        let currentMenu = 'dashboard';

        // 加载页面到 iframe（关键：使用 replaceState 避免历史记录堆积）
        function loadPage(menu) {
            if (!pageUrls[menu]) return;
            
            // 显示加载动画
            document.getElementById('iframeLoading').classList.add('active');
            
            // 更新当前菜单状态
            currentMenu = menu;
            
            // 更新菜单激活状态
            document.querySelectorAll('.menu-item').forEach(item => {
                item.classList.remove('active');
            });
            document.getElementById('menu-' + menu).classList.add('active');
            
            // 更新标题
            updateTitle(menu);
            
            // 加载 iframe（使用 replace 方式，不增加历史记录）
            const iframe = document.getElementById('contentFrame');
            
            // 如果 iframe 已经加载过该页面，直接显示，否则重新加载
            if (iframe.src !== pageUrls[menu]) {
                iframe.src = pageUrls[menu];
            }
            
            // 隐藏加载动画（模拟延迟）
            setTimeout(() => {
                document.getElementById('iframeLoading').classList.remove('active');
            }, 300);
            
            // 关键：使用 replaceState 替换当前历史记录，而不是 pushState
            // 这样多次点击菜单后，浏览器返回按钮会直接返回上一级页面
            // const newUrl = '<%= request.getContextPath() %>/home?menu=' + menu;
            // history.replaceState({menu: menu}, '', newUrl);
        }

        // 更新页面标题
        function updateTitle(menu) {
            const title = menuTitles[menu] || '主页';
            document.getElementById('pageTitle').textContent = title;
            document.getElementById('breadcrumbCurrent').textContent = title;
            document.title = title + ' - <%= currentUser.getUserAccount() %>';
        }

        // 切换侧边栏（移动端或手动）
        function toggleSidebar() {
            document.getElementById('sidebar').classList.toggle('active');
        }

        // 键盘快捷键
        document.addEventListener('keydown', (e) => {
            // Alt + S 切换侧边栏
            if (e.altKey && e.key === 's') {
                toggleSidebar();
            }
            // Alt + 数字键切换菜单
            if (e.altKey && e.key >= '1' && e.key <= '6') {
                const menus = ['dashboard', 'users', 'orders', 'products', 'analytics', 'settings'];
                const index = parseInt(e.key) - 1;
                if (menus[index]) {
                    loadPage(menus[index]);
                }
            }
        });

        // 页面加载动画
        document.addEventListener('DOMContentLoaded', () => {
            document.body.style.opacity = '0';
            setTimeout(() => {
                document.body.style.transition = 'opacity 0.5s';
                document.body.style.opacity = '1';
            }, 100);
            
            // 初始化加载默认页面
            const urlParams = new URLSearchParams(window.location.search);
            const initialMenu = urlParams.get('menu') || 'dashboard';
            loadPage(initialMenu);
        });
        ;
    </script>
</body>

</html>