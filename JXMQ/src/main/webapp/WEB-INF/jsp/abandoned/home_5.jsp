<%@ page contentType="text/html; charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ page import="icu.nothingless.commons.RespEntity" %>
<%@ page import="icu.nothingless.dto.UserDTO" %>
<%@ page import="icu.nothingless.tools.ViewUtil" %>
<%
    // 初始化用户和菜单
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
<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>主页 - <%= safeUserAccount %></title>
    <link rel="stylesheet" href="<c:url value='/static/css/pages.css' />">
</head>
<body>
    <div class="particles" id="particles"></div>
    
    <div class="sidebar-container" id="sidebar">
        <div class="sidebar">
            <div class="user-profile">
                <div class="avatar"><%= safeUserAccount.substring(0, 1).toUpperCase() %></div>
                <div class="username"><%= safeUserAccount %></div>
                <div class="user-role"><%= currentUser.getRoleId() %></div>
            </div>
            
            <nav class="menu" id="dynamicMenu">
                <div class="menu-loading">
                    <div class="loading"></div>
                    <div style="margin-top: 10px;">加载菜单中...</div>
                </div>
            </nav>
            
            <div class="sidebar-footer">
                <a href="<%= request.getContextPath() %>/logout" class="logout-btn">
                    <span class="menu-icon">🚪</span>
                    <span class="menu-text">退出登录</span>
                </a>
            </div>
        </div>
    </div>
    
    <main class="main-content">
        <div class="top-bar">
            <div>
                <h1 class="page-title" id="pageTitle">加载中...</h1>
                <div class="breadcrumb">
                    <a href="javascript:void(0)" onclick="App.loadFirstPage()">首页</a>
                    <span>/</span>
                    <span id="breadcrumbCurrent">加载中...</span>
                </div>
            </div>
            <div class="server-time" id="serverTime">--:--:--</div>
        </div>
        
        <div class="content-wrapper" id="contentWrapper">
            <div class="iframe-loading" id="iframeLoading">
                <div class="loading"></div>
            </div>
            <iframe class="content-iframe" id="contentFrame" name="contentFrame"></iframe>
        </div>
    </main>
    
    <button class="fab" onclick="App.toggleSidebar()" title="切换侧边栏 (Alt+S)">☰</button>
    
    <script>
        const App = (() => {
            // 配置常量
            const CONFIG = {
                MENU_API: '<%= request.getContextPath() %>/api/menu',
                TIME_API: '<%= request.getContextPath() %>/api/time',
                PAGE_PATH: '<%= request.getContextPath() %>/page/',
                CONTEXT_PATH: '<%= request.getContextPath() %>',
            };
            
            // 图标映射
            const ICON_MAP = {
                orders: '📦',
                users: '👥',
                products: '🛍️',
                dashboard: '📊',
                analytics: '📈',
                settings: '⚙️',
                default: '📄'
            };
            
            // 状态管理
            const state = {
                menuList: [],
                currentMenu: '',
                firstMenu: '',
                serverTimeOffset: 0
            };
            
            // DOM 元素缓存
            const elements = {
                dynamicMenu: document.getElementById('dynamicMenu'),
                pageTitle: document.getElementById('pageTitle'),
                breadcrumbCurrent: document.getElementById('breadcrumbCurrent'),
                contentFrame: document.getElementById('contentFrame'),
                iframeLoading: document.getElementById('iframeLoading'),
                serverTime: document.getElementById('serverTime'),
                sidebar: document.getElementById('sidebar')
            };
            
            // 工具函数
            const utils = {
                getIcon(name) {
                    return ICON_MAP[name] || ICON_MAP.default;
                },
                
                escapeHtml(text) {
                    return text
                        .replace(/&/g, '&amp;')
                        .replace(/</g, '&lt;')
                        .replace(/>/g, '&gt;')
                        .replace(/"/g, '&quot;')
                        .replace(/'/g, '&#x27;');
                },
                
                formatDateTime(date) {
                    return date.toLocaleString('zh-CN', {
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
            
            // 菜单管理
            const menuManager = {
                async loadMenuData() {
                    try {
                        const response = await fetch(CONFIG.MENU_API);
                        if (!response.ok) throw new Error(`HTTP`+response.status);
                        
                        const result = await response.json();
                        if (!result || result.code !== 200) {
                            throw new Error(result?.message || '加载菜单失败');
                        }
                        
                        const menuData = result.data || [];
                        if (menuData.length === 0) {
                            this.renderEmptyMenu();
                            return;
                        }
                        
                        state.menuList = menuData;
                        state.firstMenu = menuData[0].name;
                        this.renderMenu(menuData);
                        this.loadDefaultPage();
                    } catch (error) {
                        console.error('加载菜单失败:', error);
                        this.renderError(error.message);
                    }
                },
                
                renderMenu(menuData) {
                    const menuItems = menuData.map((item, index) => {
                        const icon = utils.getIcon(item.name);
                        const escapedName = utils.escapeHtml(item.name);
                        const escapedText = utils.escapeHtml(item.displayText);
                        
                         return '<a href="javascript:void(0)" ' +
                                'onclick="App.loadPage(\'' + escapedName + '\', \'' + escapedText + '\')" ' +
                                'class="menu-item" ' +
                                'id="menu-' + escapedName + '">' +
                                '<span class="menu-icon">' + icon + '</span>' +
                                '<span class="menu-text">' + escapedText + '</span>' +
                                '</a>';
                    }).join('');
                    
                    elements.dynamicMenu.innerHTML = menuItems;
                },
                
                renderEmptyMenu() {
                    elements.dynamicMenu.innerHTML = '<div class="menu-empty">暂无可用菜单</div>';
                },
                
                renderError(errorMessage) {
                    elements.dynamicMenu.innerHTML = 
                        '<div class="menu-error">' +
                            '菜单加载失败，请刷新页面重试<br>' +
                            '<small>' + utils.escapeHtml(errorMessage) + '</small>' +
                        '</div>';
                },
                
                loadDefaultPage() {
                    const urlParams = new URLSearchParams(window.location.search);
                    const targetMenu = urlParams.get('menu');
                    const targetItem = state.menuList.find(item => item.name === targetMenu);
                    
                    if (targetItem) {
                        pageLoader.loadPage(targetMenu, targetItem.displayText);
                    } else if (state.firstMenu) {
                        pageLoader.loadPage(state.firstMenu, state.menuList[0].displayText);
                    }
                },
                
                setActiveMenu(menuName) {
                    document.querySelectorAll('.menu-item').forEach(item => {
                        item.classList.remove('active');
                    });
                    
                    const activeMenu = document.getElementById("menu-"+menuName);
                    if (activeMenu) {
                        activeMenu.classList.add('active');
                    }
                }
            };
            
            // 页面加载
            const pageLoader = {
                async loadPage(menuName, displayText) {
                    if (!menuName) return;
                    
                    // 显示加载动画
                    elements.iframeLoading.classList.add('active');
                    
                    // 更新状态
                    state.currentMenu = menuName;
                    menuManager.setActiveMenu(menuName);
                    
                    // 更新界面
                    this.updatePageInfo(displayText);
                    
                    // 加载页面
                    const pageUrl = CONFIG.PAGE_PATH + window.encodeURIComponent(menuName);
                    await this.loadIframePage(pageUrl);
                },
                
                updatePageInfo(displayText) {
                    elements.pageTitle.textContent = displayText;
                    elements.breadcrumbCurrent.textContent = displayText;
                    document.title = displayText + ` - <%= safeUserAccount %>`;
                },
                
                async loadIframePage(url) {
                    return new Promise((resolve) => {
                        const iframe = elements.contentFrame;
                        
                        const onLoadComplete = () => {
                            elements.iframeLoading.classList.remove('active');
                            this.resizeIframe();
                            iframe.removeEventListener('load', onLoadComplete);
                            resolve();
                        };
                        
                        iframe.addEventListener('load', onLoadComplete);
                        
                        if (iframe.src !== url) {
                            iframe.src = url;
                        } else {
                            // 相同URL，手动触发完成
                            setTimeout(onLoadComplete, 100);
                        }
                    });
                },
                
                resizeIframe() {
                    const iframe = elements.contentFrame;
                    if (!iframe) return;
                    
                    try {
                        const iframeDoc = iframe.contentDocument || iframe.contentWindow.document;
                        if (!iframeDoc) return;
                        
                        iframe.style.height = iframeDoc.documentElement.scrollHeight + 'px';
                        iframeDoc.body.style.overflowY = 'auto';
                        iframeDoc.documentElement.style.overflowY = 'auto';
                    } catch (error) {
                        // 跨域情况，忽略
                    }
                },
                
                loadFirstPage() {
                    if (state.firstMenu && state.menuList.length > 0) {
                        pageLoader.loadPage(state.firstMenu, state.menuList[0].displayText);
                    }
                }
            };
            
            // 时间管理
            const timeManager = {
                async init() {
                    await this.fetchServerTime();
                    this.startClock();
                },
                
                async fetchServerTime() {
                    try {
                        const response = await fetch(CONFIG.TIME_API);
                        const data = await response.json();
                        state.serverTimeOffset = data.timestamp - Date.now();
                        elements.serverTime.textContent = data.datetime;
                    } catch (error) {
                        this.useLocalTime();
                    }
                },
                
                useLocalTime() {
                    elements.serverTime.textContent = utils.formatDateTime(new Date());
                },
                
                startClock() {
                    setInterval(() => this.updateDisplay(), 1000);
                    setInterval(() => this.fetchServerTime(), 30000);
                },
                
                updateDisplay() {
                    const now = new Date(Date.now() + state.serverTimeOffset);
                    elements.serverTime.textContent = utils.formatDateTime(now);
                }
            };
            
            // UI 效果
            const uiEffects = {
                createParticles() {
                    const container = document.getElementById('particles');
                    for (let i = 0; i < 50; i++) {
                        const particle = document.createElement('div');
                        particle.className = 'particle';
                        particle.style.left = `${Math.random() * 100}%`;
                        particle.style.animationDelay = `${Math.random() * 20}s`;
                        particle.style.animationDuration = `${15 + Math.random() * 10}s`;
                        container.appendChild(particle);
                    }
                },
                
                initFadeIn() {
                    document.body.style.opacity = '0';
                    setTimeout(() => {
                        document.body.style.transition = 'opacity 0.5s';
                        document.body.style.opacity = '1';
                    }, 100);
                },
                
                toggleSidebar() {
                    elements.sidebar.classList.toggle('active');
                }
            };
            
            // 键盘快捷键
            const keyboardShortcuts = {
                init() {
                    document.addEventListener('keydown', (e) => {
                        // Alt + S 切换侧边栏
                        if (e.altKey && e.key === 's') {
                            uiEffects.toggleSidebar();
                        }
                        
                        // Alt + 数字键切换菜单
                        if (e.altKey && e.key >= '1' && e.key <= '9') {
                            const index = parseInt(e.key) - 1;
                            if (state.menuList[index]) {
                                const item = state.menuList[index];
                                pageLoader.loadPage(item.name, item.displayText);
                            }
                        }
                    });
                }
            };
            
            // 初始化应用
            const init = () => {
                uiEffects.createParticles();
                uiEffects.initFadeIn();
                timeManager.init();
                keyboardShortcuts.init();
                menuManager.loadMenuData();
                
                // 监听 iframe 内容变化
                if (MutationObserver) {
                    const observer = new MutationObserver(() => pageLoader.resizeIframe());
                    observer.observe(elements.contentFrame, { 
                        attributes: true, 
                        attributeFilter: ['height', 'style'] 
                    });
                }
            };
            
            // 公共 API
            return {
                loadPage: pageLoader.loadPage.bind(pageLoader),
                loadFirstPage: pageLoader.loadFirstPage.bind(pageLoader),
                toggleSidebar: uiEffects.toggleSidebar,
                init
            };
        })();
        
        // 启动应用
        document.addEventListener('DOMContentLoaded', App.init);
    </script>
</body>
</html>