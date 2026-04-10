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
    <link rel="stylesheet" href="<c:url value='/static/css/pages2.css' />">
    <style>
        /* 确保主内容区域占满剩余空间 */
        .main-content {
            flex: 1;
            display: flex;
            flex-direction: column;
            min-height: 100vh;
            margin-left: 250px; /* 与CSS变量同步 */
            transition: margin-left 0.3s ease;
            background: transparent; /* 添加透明背景 */
        }

        html, body {
            height: 100%;
            margin: 0;
            padding: 0; /* 添加padding重置 */
            overflow: hidden;
            background: var(--bg-primary); /* 使用CSS变量 */
        }

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

            <%-- <script>
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
            </script> --%>
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
    // 配置常量
    const CONFIG = {
        ICON_MAP: {
            'orders': '📦',
            'users': '👥',
            'products': '🛍️',
            'dashboard': '📊',
            'analytics': '📈',
            'settings': '⚙️'
        },
        APP_CONTEXT: '<%= request.getContextPath() %>',
        USER_ACCOUNT: '<%= safeUserAccount %>'
    };

    // 全局状态
    const State = {
        menuList: [],
        currentMenu: '',
        firstMenu: '',
        timeOffset: 0
    };

    // DOM元素缓存
    const DOM = {
        particles: document.getElementById('particles'),
        dynamicMenu: document.getElementById('dynamicMenu'),
        pageTitle: document.getElementById('pageTitle'),
        breadcrumbCurrent: document.getElementById('breadcrumbCurrent'),
        iframeLoading: document.getElementById('iframeLoading'),
        contentFrame: document.getElementById('contentFrame'),
        serverTime: document.getElementById('serverTime'),
        sidebar: document.getElementById('sidebar')
    };

    // 工具函数
    const Utils = {
        escapeHtml: (text) => {
            const div = document.createElement('div');
            div.textContent = text;
            return div.innerHTML;
        },
        
        getIcon: (name) => {
            return CONFIG.ICON_MAP[name] || '📄';
        },
        
        showLoading: (show = true) => {
            if (show) {
                DOM.iframeLoading.classList.add('active');
            } else {
                DOM.iframeLoading.classList.remove('active');
            }
        },
        
        updateMenuActive: (menuName) => {
            document.querySelectorAll('.menu-item').forEach(item => {
                item.classList.remove('active');
            });
            const activeMenu = document.getElementById(`menu-${menuName}`);
            if (activeMenu) activeMenu.classList.add('active');
        },
        
        updatePageTitle: (displayText) => {
            DOM.pageTitle.textContent = displayText;
            DOM.breadcrumbCurrent.textContent = displayText;
            document.title = `${displayText} - CONFIG.USER_ACCOUNT`;
        }
    };

    // 粒子背景
    function createParticles() {
        if (!DOM.particles) return;
        
        for (let i = 0; i < 50; i++) {
            const particle = document.createElement('div');
            particle.className = 'particle';
            particle.style.cssText = `
                left: ${Math.random() * 100}%;
                animation-delay: ${Math.random() * 20}s;
                animation-duration: ${15 + Math.random() * 10}s;
            `;
            DOM.particles.appendChild(particle);
        }
    }

    // 服务器时间同步
    function initServerTime() {
        async function fetchServerTime() {
            try {
                const response = await fetch(CONFIG.APP_CONTEXT + '/api/time');
                const data = await response.json();
                State.timeOffset = data.timestamp - Date.now();
                updateTimeDisplay();
            } catch (error) {
                console.warn('服务器时间同步失败，使用本地时间');
                updateTimeDisplay();
            }
        }
        
        function updateTimeDisplay() {
            if (!DOM.serverTime) return;
            
            const now = new Date(Date.now() + State.timeOffset);
            const datetime = now.toLocaleString('zh-CN', {
                year: 'numeric',
                month: '2-digit',
                day: '2-digit',
                hour: '2-digit',
                minute: '2-digit',
                second: '2-digit',
                hour12: false
            }).replace(/\//g, '-');
            
            DOM.serverTime.textContent = datetime;
        }
        
        fetchServerTime();
        setInterval(updateTimeDisplay, 1000);
        setInterval(fetchServerTime, 30000);
    }

    // 加载菜单
    async function loadMenuData() {
        try {
            const response = await fetch(CONFIG.APP_CONTEXT + '/api/menu');
            if (!response.ok) throw new Error(`HTTP ${response.status}`);
            
            const result = await response.json();
            if (!result || result.code !== 200) {
                throw new Error(result?.message || '加载菜单失败');
            }
            
            const menuData = result.data || [];
            if (menuData.length === 0) {
                DOM.dynamicMenu.innerHTML = '<div class="menu-empty">暂无可用菜单</div>';
                return;
            }
            
            State.menuList = menuData;
            State.firstMenu = menuData[0].name;
            
            renderMenu(menuData);
            loadDefaultPage();
            
        } catch (error) {
            console.error('加载菜单失败:', error);
            DOM.dynamicMenu.innerHTML = `
                <div class="menu-error">
                    菜单加载失败，请刷新页面重试<br>
                    <small>${Utils.escapeHtml(error.message)}</small>
                </div>
            `;
        }
    }

    function renderMenu(menuData) {
        const menuHtml = menuData.map((item, index) => {
            const icon = Utils.getIcon(item.name);
            return `
                <a href="javascript:void(0)"
                onclick="loadPage('${item.name}', '${Utils.escapeHtml(item.displayText)}')"
                class="menu-item"
                id="menu-${item.name}">
                    <span class="menu-icon">${icon}</span>
                    <span class="menu-text">${item.displayText}</span>
                </a>
            `;
        }).join('');
        
        DOM.dynamicMenu.innerHTML = menuHtml;
    }

    function loadDefaultPage() {
        const urlParams = new URLSearchParams(window.location.search);
        const targetMenu = urlParams.get('menu');
        
        if (targetMenu) {
            const targetItem = State.menuList.find(item => item.name === targetMenu);
            if (targetItem) {
                loadPage(targetMenu, targetItem.displayText);
                return;
            }
        }
        
        if (State.firstMenu && State.menuList[0]) {
            loadPage(State.firstMenu, State.menuList[0].displayText);
        }
    }

    // 加载页面到iframe
    async function loadPage(menuName, displayText) {
        if (State.currentMenu === menuName) return;
        
        Utils.showLoading(true);
        Utils.updateMenuActive(menuName);
        Utils.updatePageTitle(displayText);
        State.currentMenu = menuName;
        
        const iframe = DOM.contentFrame;
        const pageUrl = CONFIG.APP_CONTEXT + '/page/' + menuName;
        
        // 清理旧的load事件监听器
        iframe.onload = null;
        
        // 设置新的load事件监听器
        iframe.onload = function() {
            Utils.showLoading(false);
            injectIframeCSS();
            setupIframeResize();
        };
        
        // 加载页面
        if (iframe.src !== pageUrl) {
            iframe.src = pageUrl;
        } else {
            iframe.contentWindow.location.reload();
        }
    }

    // 关键修复：向iframe注入CSS
    function injectIframeCSS() {
        const iframe = DOM.contentFrame;
        if (!iframe) return;
        
        try {
            const iframeDoc = iframe.contentDocument || iframe.contentWindow.document;
            if (!iframeDoc) return;
            
            // 检查是否已注入CSS
            if (iframeDoc.getElementById('injected-styles')) return;
            
            // 创建link标签引入外部CSS
            const link = document.createElement('link');
            link.id = 'injected-styles';
            link.rel = 'stylesheet';
            link.href = `<c:url value='/static/css/pages.css' />`;
            
            // 添加基础样式
            const style = document.createElement('style');
            style.id = 'injected-base-styles';
            style.textContent = `
                body, html {
                    margin: 0;
                    padding: 0;
                    font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
                    background: transparent;
                }
                * {
                    box-sizing: border-box;
                }
            `;
            
            iframeDoc.head.appendChild(link);
            iframeDoc.head.appendChild(style);
            
        } catch (error) {
            console.warn('CSS注入失败（可能是跨域限制）:', error);
        }
    }

    // iframe自适应高度
    function setupIframeResize() {
        const iframe = DOM.contentFrame;
        if (!iframe) return;
        
        function resizeIframe() {
            try {
                const iframeDoc = iframe.contentDocument || iframe.contentWindow.document;
                if (!iframeDoc || !iframeDoc.documentElement) return;
                
                const height = Math.max(
                    iframeDoc.documentElement.scrollHeight,
                    iframeDoc.body.scrollHeight
                );
                
                iframe.style.height = `${height}px`;
                
                // 确保iframe内部可滚动
                iframeDoc.body.style.overflowY = 'auto';
                iframeDoc.documentElement.style.overflowY = 'auto';
                
            } catch (error) {
                // 跨域情况忽略错误
            }
        }
        
        // 初始调整
        setTimeout(resizeIframe, 100);
        
        // 监听iframe内部变化
        try {
            const iframeDoc = iframe.contentDocument || iframe.contentWindow.document;
            if (iframeDoc) {
                const observer = new MutationObserver(() => {
                    setTimeout(resizeIframe, 50);
                });
                
                observer.observe(iframeDoc.body, {
                    childList: true,
                    subtree: true,
                    attributes: true,
                    characterData: true
                });
                
                // 监听iframe内部窗口大小变化
                iframe.contentWindow.addEventListener('resize', resizeIframe);
            }
        } catch (error) {
            // 跨域情况使用轮询
            setInterval(resizeIframe, 1000);
        }
    }

    // 页面初始化
    function initPage() {
        // 淡入效果
        document.body.style.opacity = '0';
        setTimeout(() => {
            document.body.style.transition = 'opacity 0.5s';
            document.body.style.opacity = '1';
        }, 100);
        
        // 初始化各模块
        createParticles();
        initServerTime();
        loadMenuData();
        setupKeyboardShortcuts();
    }

    // 键盘快捷键
    function setupKeyboardShortcuts() {
        document.addEventListener('keydown', (e) => {
            // Alt + S 切换侧边栏
            if (e.altKey && e.key === 's') {
                toggleSidebar();
            }
            
            // Alt + 数字键切换菜单
            if (e.altKey && e.key >= '1' && e.key <= '9') {
                const index = parseInt(e.key) - 1;
                if (State.menuList[index]) {
                    loadPage(State.menuList[index].name, State.menuList[index].displayText);
                }
            }
        });
    }

    // 切换侧边栏
    function toggleSidebar() {
        DOM.sidebar.classList.toggle('active');
    }

    // 加载第一个页面
    function loadFirstPage() {
        if (State.firstMenu && State.menuList[0]) {
            loadPage(State.firstMenu, State.menuList[0].displayText);
        }
    }

    // DOM加载完成后初始化
    if (document.readyState === 'loading') {
        document.addEventListener('DOMContentLoaded', initPage);
    } else {
        initPage();
    }
    </script>
</body>
</html>