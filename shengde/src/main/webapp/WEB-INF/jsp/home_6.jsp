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
    String safeUserAccount = org.apache.commons.text.StringEscapeUtils.escapeHtml4(currentUser.getUserAccount());
%>
<html>
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>主页 - <%= safeUserAccount %></title>
    <link rel="stylesheet" href="<c:url value='/static/css/pages.css' />">
    <style>
        /* 菜单图标样式 */
        .menu-icon {
            display: inline-flex;
            align-items: center;
            justify-content: center;
            width: 24px;
            height: 24px;
            margin-right: 12px;
            font-size: 18px;
        }

        /* 动态菜单加载状态 */
        .menu-loading {
            padding: 20px;
            text-align: center;
            color: var(--text-muted);
        }
        
        /* 错误提示 */
        .menu-error {
            padding: 20px;
            text-align: center;
            color: #ff6b6b;
            font-size: 14px;
        }
        
        /* 空菜单提示 */
        .menu-empty {
            padding: 20px;
            text-align: center;
            color: var(--text-muted);
            font-size: 14px;
        }
        /* 确保主内容区域占满剩余空间 */
        .main-content {
            flex: 1;
            display: flex;
            flex-direction: column;
            min-height: 100vh; /* 确保至少占满视口高度 */
            margin-left: 250px; /* 根据你的侧边栏宽度调整 */
            transition: margin-left 0.3s ease;
        }
        /* 内容包装器 - 关键修复 */
        .content-wrapper {
            flex: 1; /* 占满剩余空间 */
            position: relative;
            overflow: hidden; /* 防止滚动条问题 */
            background: rgba(255, 255, 255, 0.02);
        }
        /* iframe 样式 - 关键修复 */
        .content-iframe {
            width: 100%;
            height: 100%; /* 占满父容器 */
            border: none;
            display: block;
            background: transparent;
            scrollbar: none;
        }

        html, body {
            height: 100%;
            margin: 0;
            overflow: hidden; /* 防止双滚动条 */
        }
        
    </style>
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
                    <%= safeUserAccount.substring(0, 1).toUpperCase() %>
                </div>
                <div class="username"><%= safeUserAccount %></div>
                <div class="user-role"><%= currentUser.getRoleId() %></div>
            </div>

            <!-- 动态菜单容器 -->
            <nav class="menu" id="dynamicMenu">
                <div class="menu-loading">
                    <div class="loading"></div>
                    <div style="margin-top: 10px; font-size: 12px;">加载菜单中...</div>
                </div>
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
                <h1 class="page-title" id="pageTitle">加载中...</h1>
                <div class="breadcrumb">
                    <a href="javascript:void(0)" onclick="loadFirstPage()">首页</a>
                    <span>/</span>
                    <span id="breadcrumbCurrent">加载中...</span>
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
    // console.log("currentUser is","<%= org.apache.commons.text.StringEscapeUtils.escapeEcmaScript(currentUser.toString()) %>");
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

        // 存储菜单数据
        let menuList = [];
        let currentMenu = '';
        let firstMenu = '';

        // 默认图标映射
        const iconMap = {
            'orders': '📦',
            'users': '👥',
            'products': '🛍️',
            'dashboard': '📊',
            'analytics': '📈',
            'settings': '⚙️'
        };

        // 获取图标
        function getIcon(name) {
            return iconMap[name] || '📄';
        }

        // 加载菜单数据
        function loadMenuData() {
            const menuContainer = document.getElementById('dynamicMenu');
            
            fetch('<%= request.getContextPath() %>/api/menu')
                .then(response => {
                    if (!response.ok) {
                        throw new Error('HTTP error! status: ' + response.status);
                    }
                    return response.json();
                })
                .then(result => {
                    if (!result || result.code !== 200) {
                        const errorMsg = result?.message && result.code !== 200 
                            ? result.message 
                            : '加载菜单失败';
                        throw new Error(errorMsg);
                    }
                    
                    const menuData = result.data || [];
                    if (menuData.length === 0) {
                        menuContainer.innerHTML = '<div class="menu-empty">暂无可用菜单</div>';
                        return;
                    }

                    menuList = menuData;
                    firstMenu = menuList[0].name;
                    
                    // 生成菜单 HTML
                    let html = '';
                    menuList.forEach((item, index) => {
                        const menuName = item.name;
                        const displayText = item.displayText;
                        const icon = getIcon(menuName);
                        
                        html += '<a href="javascript:void(0)" ' +
                                'onclick="loadPage(\'' + menuName + '\', \'' + displayText + '\')" ' +
                                'class="menu-item" ' +
                                'id="menu-' + menuName + '">' +
                                '<span class="menu-icon">' + icon + '</span>' +
                                '<span class="menu-text">' + displayText + '</span>' +
                                '</a>';
                    });
                    
                    menuContainer.innerHTML = html;
                    
                    // 加载默认页面
                    const urlParams = new URLSearchParams(window.location.search);
                    const targetMenu = urlParams.get('menu');
                    
                    if (targetMenu) {
                        const targetItem = menuList.find(item => item.name === targetMenu);
                        if (targetItem) {
                            loadPage(targetMenu, targetItem.displayText);
                        } else {
                            loadPage(firstMenu, menuList[0].displayText);
                        }
                    } else {
                        loadPage(firstMenu, menuList[0].displayText);
                    }
                })
                .catch(error => {
                    console.error('加载菜单失败:', error);
                    menuContainer.innerHTML = '<div class="menu-error">菜单加载失败，请刷新页面重试<br><small>' + 
                                            (error.message) + '</small></div>';
                });
        }

        // 加载页面到 iframe
        function loadPage(menuName, displayText) {
            // 显示加载动画
            document.getElementById('iframeLoading').classList.add('active');
            
            // 更新当前菜单
            currentMenu = menuName;
            
            // 更新菜单激活状态
            document.querySelectorAll('.menu-item').forEach(item => {
                item.classList.remove('active');
            });
            const activeMenu = document.getElementById('menu-' + menuName);
            if (activeMenu) {
                activeMenu.classList.add('active');
            }
            
            // 更新标题
            document.getElementById('pageTitle').textContent = displayText;
            document.getElementById('breadcrumbCurrent').textContent = displayText;
            document.title = displayText + ' - <%= safeUserAccount %>';
            
            const iframe = document.getElementById('contentFrame');
            const pageUrl = '<%= request.getContextPath() %>/page/' + menuName;
            
            // 使用iframe的load事件隐藏loading
            iframe.onload = function() {
                setInterval(resizeIframe, 1000);
                document.getElementById('iframeLoading').classList.remove('active');
                
                try {
                    const iframeDoc = iframe.contentDocument || iframe.contentWindow.document;
                    const iframeBody = iframeDoc.body;
                    const height = iframeBody.scrollHeight;
                    iframeBody.style.overflowY = 'auto';
                    
                } catch (e) {
                    // 跨域情况下会报错，忽略即可
                }
            };
            
            if (iframe.src !== pageUrl) {
                iframe.src = pageUrl;
            } else {
                // 如果URL相同，手动触发隐藏loading（因为onload不会触发）
                setTimeout(() => {
                    document.getElementById('iframeLoading').classList.remove('active');
                }, 300);
            }
        }
        function resizeIframe() {
            const iframe = document.querySelector('.content-iframe');
            if (!iframe) return;
            
            const iframeDoc = iframe.contentDocument || iframe.contentWindow.document;
            if (!iframeDoc) return;
            try{
                // 设置 iframe 高度为内容高度
                iframe.style.height = iframeDoc.documentElement.scrollHeight + 'px';
                // 允许 iframe 内容区域滚动
                iframeDoc.body.style.overflowY = 'auto';
                iframeDoc.documentElement.style.overflowY = 'auto';
            }catch (e) {
            }
            
            
        }

        // 在 iframe 加载完成后调用
        const iframe = document.querySelector('.content-iframe');
        iframe.onload = resizeIframe;
        resizeIframe();

        // 如果 iframe 内容会动态变化，可以定时检测
        // setInterval(resizeIframe, 1000);

        // 加载第一个页面
        function loadFirstPage() {
            if (firstMenu && menuList.length > 0) {
                loadPage(firstMenu, menuList[0].displayText);
            }
        }

        // 切换侧边栏
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
            if (e.altKey && e.key >= '1' && e.key <= '9') {
                const index = parseInt(e.key) - 1;
                if (menuList[index]) {
                    loadPage(menuList[index].name, menuList[index].displayText);
                }
            }
        });


        // 页面加载完成后初始化菜单
        document.addEventListener('DOMContentLoaded', () => {
            document.body.style.opacity = '0';
            setTimeout(() => {
                document.body.style.transition = 'opacity 0.5s';
                document.body.style.opacity = '1';
            }, 100);
            
            // 加载动态菜单
            loadMenuData();
        });
    </script>
</body>
</html>