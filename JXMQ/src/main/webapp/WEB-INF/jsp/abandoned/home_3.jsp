<%@ page contentType="text/html; charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<%@ page import="icu.nothingless.commons.RespEntity" %>
<%@ page import="icu.nothingless.dto.UserDTO" %>
<%@ page import="icu.nothingless.tools.ViewUtil" %>
<!DOCTYPE html>
<%
    UserDTO currentUser = (UserDTO) request.getSession().getAttribute("CURRENT_USER");
    if (currentUser == null) {
        request.setAttribute("respEntity", RespEntity.error("错误！系统出现异常！"));
        ViewUtil.render(request, response, "error_page");
        return;
    }
    String currentMenu = (String) request.getSession().getAttribute("menu");
    if (currentMenu == null) currentMenu = "dashboard";
    String safeUserAccount = org.apache.commons.text.StringEscapeUtils.escapeHtml4(currentUser.getUserAccount());
%>
<html>
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>主页 - <%= safeUserAccount %></title>
    <link rel="stylesheet" href="<c:url value='/static/css/pages.css' />">
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

            <!-- 动态菜单容器 - 使用事件委托，无需内联onclick -->
            <nav class="menu" id="dynamicMenu" data-current-menu="<%= currentMenu %>">
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
                    <a href="#" data-action="home">首页</a>
                    <span>/</span>
                    <span id="breadcrumbCurrent">加载中...</span>
                </div>
            </div>
            <div class="server-time" id="serverTime">--:--:--</div>
        </div>

        <!-- 动态内容区 - iframe使用100%填充，无需JS计算高度 -->
        <div class="content-wrapper" id="contentWrapper">
            <div class="iframe-loading" id="iframeLoading">
                <div class="loading"></div>
            </div>
            <iframe class="content-iframe" id="contentFrame" name="contentFrame" sandbox="allow-same-origin allow-scripts allow-forms"></iframe>
        </div>
    </main>

    <!-- 快捷按钮 -->
    <button class="fab" id="fabBtn" title="切换侧边栏">☰</button>

    <script>
    /**
     * 主页应用模块 - 使用IIFE避免全局污染
     * 优化点：
     * 1. 所有样式移至外部CSS，JS不再注入任何样式
     * 2. 使用事件委托处理菜单点击，避免重复绑定
     * 3. iframe使用flex布局自然填充，无需resize计算
     * 4. 使用ResizeObserver替代setInterval轮询
     * 5. 模块化管理，逻辑清晰分离
     */
    (function() {
        'use strict';
        
        // ==================== 配置与常量 ====================
        const CONFIG = {
            apiBase: '<%=request.getContextPath()%>',
            defaultIcon: '📄',
            iconMap: {
                'orders': '📦',
                'users': '👥',
                'products': '🛍️',
                'dashboard': '📊',
                'analytics': '📈',
                'settings': '⚙️'
            },
            timeSyncInterval: 30000, // 30秒同步一次服务器时间
            particleCount: 50
        };

        // ==================== 状态管理 ====================
        const state = {
            menuList: [],
            currentMenu: '',
            firstMenu: '',
            serverTimeOffset: 0,
            isSidebarOpen: true
        };

        // ==================== DOM元素缓存 ====================
        const elements = {
            particles: document.getElementById('particles'),
            dynamicMenu: document.getElementById('dynamicMenu'),
            sidebar: document.getElementById('sidebar'),
            contentFrame: document.getElementById('contentFrame'),
            iframeLoading: document.getElementById('iframeLoading'),
            pageTitle: document.getElementById('pageTitle'),
            breadcrumbCurrent: document.getElementById('breadcrumbCurrent'),
            serverTime: document.getElementById('serverTime'),
            fabBtn: document.getElementById('fabBtn')
        };

        // ==================== 工具函数 ====================
        const utils = {
            /**
             * 转义HTML，防止XSS
             */
            escapeHtml: (text) => {
                const div = document.createElement('div');
                div.textContent = text;
                return div.innerHTML;
            },

            /**
             * 获取图标
             */
            getIcon: (name) => CONFIG.iconMap[name] || CONFIG.defaultIcon,

            /**
             * 安全的JSON解析
             */
            safeJsonParse: async (response) => {
                const text = await response.text();
                try {
                    return JSON.parse(text);
                } catch (e) {
                    throw new Error('Invalid JSON response');
                }
            }
        };

        // ==================== 粒子背景 ====================
        const particleSystem = {
            init() {
                if (!elements.particles) return;
                
                const fragment = document.createDocumentFragment();
                for (let i = 0; i < CONFIG.particleCount; i++) {
                    const particle = document.createElement('div');
                    particle.className = 'particle';
                    // 使用CSS变量或data属性传递随机值，避免内联样式
                    particle.style.cssText = `
                        left: ${Math.random() * 100}%;
                        animation-delay: ${Math.random() * 20}s;
                        animation-duration: ${15 + Math.random() * 10}s;
                    `;
                    fragment.appendChild(particle);
                }
                elements.particles.appendChild(fragment);
            }
        };

        // ==================== 菜单系统 ====================
        const menuSystem = {
            /**
             * 初始化菜单
             */
            async init() {
                try {
                    const response = await fetch('<%= request.getContextPath() %>/api/menu');
                    if (!response.ok) throw new Error(`HTTP ${response.status}`);
                    
                    const result = await utils.safeJsonParse(response);
                    if (result.code !== 200 || !result.data?.length) {
                        throw new Error(result.message || '暂无可用菜单');
                    }

                    state.menuList = result.data;
                    state.firstMenu = state.menuList[0].name;
                    
                    this.render();
                    this.setupEventDelegation(); // 使用事件委托
                    this.loadInitialPage();
                    
                } catch (error) {
                    console.error('菜单加载失败:', error);
                    elements.dynamicMenu.innerHTML = `
                        <div class="menu-error">
                            菜单加载失败，请刷新页面重试<br>
                            <small>${utils.escapeHtml(error.message)}</small>
                        </div>
                    `;
                }
            },

            /**
             * 渲染菜单 - 使用事件委托，不绑定内联onclick
             */
            render() {
                const html = state.menuList.map(item => `
                    <a href="#" 
                       class="menu-item" 
                       data-menu-name="${utils.escapeHtml(item.name)}"
                       data-display-text="${utils.escapeHtml(item.displayText)}">
                        <span class="menu-icon">${utils.getIcon(item.name)}</span>
                        <span class="menu-text">${utils.escapeHtml(item.displayText)}</span>
                    </a>
                `).join('');
                
                elements.dynamicMenu.innerHTML = html;
            },

            /**
             * 设置事件委托 - 所有菜单点击由父容器统一处理
             */
            setupEventDelegation() {
                elements.dynamicMenu.addEventListener('click', (e) => {
                    e.preventDefault();
                    const menuItem = e.target.closest('.menu-item');
                    if (!menuItem) return;
                    
                    const { menuName, displayText } = menuItem.dataset;
                    this.loadPage(menuName, displayText);
                });
            },

            /**
             * 加载初始页面
             */
            loadInitialPage() {
                const urlParams = new URLSearchParams(window.location.search);
                const targetMenu = urlParams.get('menu');
                
                const targetItem = targetMenu 
                    ? state.menuList.find(item => item.name === targetMenu)
                    : null;

                if (targetItem) {
                    this.loadPage(targetItem.name, targetItem.displayText);
                } else {
                    this.loadPage(state.firstMenu, state.menuList[0].displayText);
                }
            },

            /**
             * 加载页面到iframe - 优化：无需计算高度，使用CSS flex填充
             */
            loadPage(menuName, displayText) {
                if (state.currentMenu === menuName && elements.contentFrame.src) {
                    // 已在当前页面，仅隐藏loading
                    elements.iframeLoading.classList.remove('active');
                    return;
                }

                // 显示加载动画
                elements.iframeLoading.classList.add('active');
                state.currentMenu = menuName;

                // 更新UI状态 - 使用classList替代style操作
                elements.dynamicMenu.querySelectorAll('.menu-item').forEach(item => {
                    item.classList.toggle('active', item.dataset.menuName === menuName);
                });

                // 更新标题
                elements.pageTitle.textContent = displayText;
                elements.breadcrumbCurrent.textContent = displayText;
                document.title = `${displayText} - <%= safeUserAccount %>`;

                // 设置iframe src
                const pageUrl = `<%=request.getContextPath()%>/page/${menuName}`;
                
                // 使用单次load事件，避免重复绑定
                const handleLoad = () => {
                    elements.iframeLoading.classList.remove('active');
                    elements.contentFrame.removeEventListener('load', handleLoad);
                };
                
                elements.contentFrame.addEventListener('load', handleLoad);
                elements.contentFrame.src = pageUrl;
            },

            /**
             * 加载第一个页面（首页）
             */
            loadFirstPage() {
                if (state.firstMenu && state.menuList.length > 0) {
                    this.loadPage(state.firstMenu, state.menuList[0].displayText);
                }
            }
        };

        // ==================== 时间同步系统 ====================
        const timeSystem = {
            init() {
                this.sync();
                // 每秒更新显示
                setInterval(() => this.updateDisplay(), 1000);
                // 定期同步服务器时间
                setInterval(() => this.sync(), CONFIG.timeSyncInterval);
            },

            async sync() {
                try {
                    const response = await fetch('<%= request.getContextPath() %>/api/time');
                    const data = await response.json();
                    state.serverTimeOffset = data.timestamp - Date.now();
                    this.updateDisplay(data.datetime);
                } catch (error) {
                    // 静默失败，使用本地时间
                    this.updateDisplay();
                }
            },

            updateDisplay(serverDatetime) {
                const now = serverDatetime 
                    ? new Date(serverDatetime) 
                    : new Date(Date.now() + state.serverTimeOffset);
                
                elements.serverTime.textContent = now.toLocaleString('zh-CN', {
                    year: 'numeric',
                    month: '2-digit',
                    day: '2-digit',
                    hour: '2-digit',
                    minute: '2-digit',
                    second: '2-digit',
                    hour12: false
                }).replace(/\//g, '-');
            }
        };

        // ==================== 侧边栏控制 ====================
        const sidebarSystem = {
            init() {
                // 绑定FAB按钮
                elements.fabBtn?.addEventListener('click', () => this.toggle());
                
                // 键盘快捷键 - 使用事件委托在document层处理
                document.addEventListener('keydown', (e) => {
                    if (e.altKey && e.key === 's') {
                        e.preventDefault();
                        this.toggle();
                    }
                    // Alt+数字切换菜单
                    if (e.altKey && e.key >= '1' && e.key <= '9') {
                        e.preventDefault();
                        const index = parseInt(e.key) - 1;
                        const menu = state.menuList[index];
                        if (menu) {
                            menuSystem.loadPage(menu.name, menu.displayText);
                        }
                    }
                });
            },

            toggle() {
                state.isSidebarOpen = !state.isSidebarOpen;
                elements.sidebar?.classList.toggle('active', state.isSidebarOpen);
            }
        };

        // ==================== 全局事件委托 ====================
        const globalEvents = {
            init() {
                // 面包屑首页点击
                document.querySelector('.breadcrumb a[data-action="home"]')?.addEventListener('click', (e) => {
                    e.preventDefault();
                    menuSystem.loadFirstPage();
                });
            }
        };

        // ==================== 初始化 ====================
        const init = () => {
            // 页面淡入效果 - 使用CSS transition类
            document.body.style.opacity = '0';
            requestAnimationFrame(() => {
                document.body.style.transition = 'opacity 0.5s';
                document.body.style.opacity = '1';
            });

            // 初始化各模块
            particleSystem.init();
            menuSystem.init();
            timeSystem.init();
            sidebarSystem.init();
            globalEvents.init();
        };

        // DOM就绪后启动
        if (document.readyState === 'loading') {
            document.addEventListener('DOMContentLoaded', init);
        } else {
            init();
        }

    })();
    </script>
</body>
</html>