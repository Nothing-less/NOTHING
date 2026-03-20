<%@ page contentType="text/html; charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ page import="icu.nothingless.commons.RespEntity" %>
<%@ page import="icu.nothingless.dto.UserDTO" %>
<%@ page import="icu.nothingless.tools.ViewUtil" %>
<%@ page import="icu.nothingless.tools.RedirectUtil" %>
<%@ page import="java.util.Map" %>
<%
    // ==================== 初始化验证 ====================
    UserDTO currentUser = (UserDTO) request.getSession(false).getAttribute("CURRENT_USER");
    if (currentUser == null) {
        request.setAttribute("respEntity", RespEntity.error("错误！系统出现异常！"));
        ViewUtil.render(request, response, "error_page");
        return;
    }
    
    String currentMenu = (String) request.getSession(false).getAttribute("MENU");
    System.err.println("Current Menu is : "+currentMenu);
    if (currentMenu == null) currentMenu = "dashboard";

    String safeUserAccount = org.apache.commons.text.StringEscapeUtils.escapeHtml4(currentUser.getUserAccount());
    String userInitial = safeUserAccount.substring(0, 1).toUpperCase();
    String userRole = currentUser.getRoleId();
    String contextPath = request.getContextPath();
%>
<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>主页 - <%= safeUserAccount %></title>
    <link rel="stylesheet" href="<c:url value='/static/css/pages.css' />">
    <link rel="stylesheet" href="<c:url value='/static/css/tables.css' />">
</head>
<body>
    <!-- ==================== 背景粒子效果 ==================== -->
    <div class="particles" id="particles"></div>
    
    <!-- ==================== 侧边栏 ==================== -->
    <div class="sidebar-container" id="sidebar">
        <div class="sidebar">
            <!-- 用户信息 -->
            <div class="user-profile">
                <div class="avatar"><%= userInitial %></div>
                <div class="username"><%= safeUserAccount %></div>
                <div class="user-role"><%= userRole %></div>
            </div>
            
            <!-- 动态菜单 -->
            <nav class="menu" id="dynamicMenu">
                <div class="menu-loading">
                    <div class="loading"></div>
                    <div style="margin-top: 10px;">加载菜单中...</div>
                </div>
            </nav>
            
            <!-- 侧边栏底部 -->
            <div class="sidebar-footer">
                <a href="<%= contextPath %>/logout" class="logout-btn">
                    <span class="menu-icon">🚪</span>
                    <span class="menu-text">退出登录</span>
                </a>
            </div>
        </div>
    </div>
    
    <!-- ==================== 主内容区 ==================== -->
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
    
    <!-- ==================== 悬浮按钮 ==================== -->
    <button class="fab" onclick="App.toggleSidebar()" title="切换侧边栏 (Alt+S)">☰</button>
    
    <!-- ==================== JavaScript 应用逻辑 ==================== -->
    <script>
        var App = (function() {
            // ==================== 配置常量 ====================
            const CONFIG = {
                API: {
                    MENU: '<%= contextPath %>/api/menu',
                    TIME: '<%= contextPath %>/api/time'
                },
                PATH: {
                    PAGE: '<%= contextPath %>/page/',
                    CONTEXT: '<%= contextPath %>'
                },
                INTERVAL: {
                    CLOCK_UPDATE: 1000,
                    TIME_SYNC: 30000
                },
                 // 添加这行：从后端传递当前菜单
                CURRENT_MENU: '<%= currentMenu %>'
            };
            
            // ==================== 图标映射 ====================
            var ICON_MAP = {
                orders: '📦',
                users: '👥',
                products: '🛍️',
                dashboard: '📊',
                analytics: '📈',
                settings: '⚙️',
                default: '📄'
            };
            
            // ==================== 状态管理 ====================
            var state = {
                menuList: [],
                currentMenu: '',
                firstMenu: '',
                serverTimeOffset: 0,
                isLoading: false
            };
            
            // ==================== DOM 元素缓存 ====================
            var elements = {
                dynamicMenu: document.getElementById('dynamicMenu'),
                pageTitle: document.getElementById('pageTitle'),
                breadcrumbCurrent: document.getElementById('breadcrumbCurrent'),
                contentFrame: document.getElementById('contentFrame'),
                iframeLoading: document.getElementById('iframeLoading'),
                serverTime: document.getElementById('serverTime'),
                sidebar: document.getElementById('sidebar'),
                particles: document.getElementById('particles')
            };
            
            // ==================== 工具函数模块 ====================
            var utils = {
                getIcon: function(name) {
                    return ICON_MAP[name] || ICON_MAP.default;
                },
                
                escapeHtml: function(text) {
                    if (!text) return '';
                    var div = document.createElement('div');
                    div.textContent = text;
                    return div.innerHTML;
                },
                
                formatDateTime: function(date) {
                    var pad = function(n) {
                        return n < 10 ? '0' + n : n.toString();
                    };
                    var y = date.getFullYear();
                    var m = pad(date.getMonth() + 1);
                    var d = pad(date.getDate());
                    var h = pad(date.getHours());
                    var min = pad(date.getMinutes());
                    var s = pad(date.getSeconds());
                    return y + '-' + m + '-' + d + ' ' + h + ':' + min + ':' + s;
                }
            };
            
            // ==================== 菜单管理模块 ====================
            var menuManager = {
                loadMenuData: function() {
                    var self = this;
                    fetch(CONFIG.API.MENU).then(function(response) {
                        if (!response.ok) {
                            throw new Error('HTTP ' + response.status + ': ' + response.statusText);
                        }
                        return response.json();
                    }).then(function(result) {
                        if (!result || result.code !== 200) {
                            throw new Error(result && result.message ? result.message : '加载菜单失败');
                        }
                        
                        var menuData = result.data || [];
                        if (menuData.length === 0) {
                            self.renderEmptyMenu();
                            return;
                        }
                        
                        state.menuList = menuData;
                        state.firstMenu = menuData[0].name;
                        self.renderMenu(menuData);
                        self.loadDefaultPage();
                    }).catch(function(error) {
                        console.error('加载菜单失败:', error);
                        self.renderError(error.message);
                    });
                },
                
                renderMenu: function(menuData) {
                    elements.dynamicMenu.innerHTML = '';
                    
                    for (var i = 0; i < menuData.length; i++) {
                        var item = menuData[i];
                        var menuItem = document.createElement('a');
                        menuItem.href = 'javascript:void(0)';
                        menuItem.className = 'menu-item';
                        menuItem.id = 'menu-' + utils.escapeHtml(item.name);
                        
                        // 使用闭包保存当前项
                        (function(currentItem) {
                            menuItem.onclick = function() {
                                pageLoader.loadPage(currentItem.name, currentItem.displayText);
                            };
                        })(item);
                        
                        // 添加图标
                        var iconSpan = document.createElement('span');
                        iconSpan.className = 'menu-icon';
                        iconSpan.textContent = utils.getIcon(item.name);
                        menuItem.appendChild(iconSpan);
                        
                        // 添加文字
                        var textSpan = document.createElement('span');
                        textSpan.className = 'menu-text';
                        textSpan.textContent = item.displayText;
                        menuItem.appendChild(textSpan);
                        
                        // 添加键盘快捷键提示
                        if (i < 9) {
                            menuItem.title = 'Alt+' + (i + 1);
                        }
                        
                        elements.dynamicMenu.appendChild(menuItem);
                    }
                },
                
                renderEmptyMenu: function() {
                    elements.dynamicMenu.innerHTML = '';
                    var emptyDiv = document.createElement('div');
                    emptyDiv.className = 'menu-empty';
                    emptyDiv.textContent = '暂无可用菜单';
                    elements.dynamicMenu.appendChild(emptyDiv);
                },
                
                renderError: function(errorMessage) {
                    elements.dynamicMenu.innerHTML = '';
                    var errorDiv = document.createElement('div');
                    errorDiv.className = 'menu-error';
                    
                    var text1 = document.createTextNode('菜单加载失败，请刷新页面重试');
                    errorDiv.appendChild(text1);
                    
                    var br = document.createElement('br');
                    errorDiv.appendChild(br);
                    
                    var small = document.createElement('small');
                    small.textContent = utils.escapeHtml(errorMessage);
                    errorDiv.appendChild(small);
                    
                    elements.dynamicMenu.appendChild(errorDiv);
                },
                
                loadDefaultPage: function() {
                    var urlParams = new URLSearchParams(window.location.search);
                    var targetMenu = urlParams.get('menu');
                    var targetItem = null;

                    // 优先使用URL参数，其次使用后端传递的菜单，最后使用第一个菜单
                    var menuToLoad = targetMenu || CONFIG.CURRENT_MENU || state.firstMenu;
                    
                    for (var i = 0; i < state.menuList.length; i++) {
                        if (state.menuList[i].name === menuToLoad) {
                            targetItem = state.menuList[i];
                            break;
                        }
                    }
                    
                    if (targetItem) {
                        pageLoader.loadPage(menuToLoad, targetItem.displayText);
                    } else if (state.firstMenu) {
                        pageLoader.loadPage(state.firstMenu, state.menuList[0].displayText);
                    }
                },
                
                setActiveMenu: function(menuName) {
                    var items = document.querySelectorAll('.menu-item');
                    for (var i = 0; i < items.length; i++) {
                        items[i].classList.remove('active');
                    }
                    
                    var activeMenu = document.getElementById('menu-' + menuName);
                    if (activeMenu) {
                        activeMenu.classList.add('active');
                    }
                },
                
                getMenuByIndex: function(index) {
                    return state.menuList[index] || null;
                }
            };
            
            // ==================== 页面加载模块 ====================
            var pageLoader = {
                loadPage: function(menuName, displayText) {
                    if (!menuName || state.isLoading) return;
                    
                    var self = this;
                    state.isLoading = true;
                    elements.iframeLoading.classList.add('active');
                    
                    state.currentMenu = menuName;
                    menuManager.setActiveMenu(menuName);
                    this.updatePageInfo(displayText);
                    
                    var pageUrl = CONFIG.PATH.PAGE + encodeURIComponent(menuName);
                    
                    this.loadIframePage(pageUrl).then(function() {
                        state.isLoading = false;
                    }).catch(function(error) {
                        console.error('页面加载失败:', error);
                        state.isLoading = false;
                    });
                },
                
                updatePageInfo: function(displayText) {
                    elements.pageTitle.textContent = displayText;
                    elements.breadcrumbCurrent.textContent = displayText;
                    document.title = displayText + ' - <%= safeUserAccount %>';
                },
                
                loadIframePage: function(url) {
                    var self = this;
                    return new Promise(function(resolve, reject) {
                        var iframe = elements.contentFrame;
                        var timeoutId = null;
                        
                        var cleanup = function() {
                            if (timeoutId) {
                                clearTimeout(timeoutId);
                                timeoutId = null;
                            }
                            iframe.removeEventListener('load', onLoad);
                            iframe.removeEventListener('error', onError);
                        };
                        
                        var onLoad = function() {
                            cleanup();
                            elements.iframeLoading.classList.remove('active');
                            self.resizeIframe();
                            resolve();
                        };
                        
                        var onError = function() {
                            cleanup();
                            reject(new Error('Iframe 加载失败'));
                        };
                        
                        iframe.addEventListener('load', onLoad);
                        iframe.addEventListener('error', onError);
                        
                        timeoutId = setTimeout(function() {
                            cleanup();
                            elements.iframeLoading.classList.remove('active');
                            reject(new Error('页面加载超时'));
                        }, 10000);
                        
                        if (iframe.src !== url) {
                            iframe.src = url;
                        } else {
                            setTimeout(onLoad, 100);
                        }
                    });
                },
                
                resizeIframe: function() {
                    var iframe = elements.contentFrame;
                    if (!iframe) return;
                    
                    try {
                        var iframeDoc = iframe.contentDocument || (iframe.contentWindow && iframe.contentWindow.document);
                        if (!iframeDoc) return;
                        
                        var height = iframeDoc.documentElement && iframeDoc.documentElement.scrollHeight ? 
                                    iframeDoc.documentElement.scrollHeight : 
                                    (iframeDoc.body && iframeDoc.body.scrollHeight ? iframeDoc.body.scrollHeight : 600);
                        
                        iframe.style.height = height + 'px';
                        
                        // 隐藏滚动条但保留滚动功能
                        if (iframeDoc.body) {
                            iframeDoc.body.style.overflowY = 'auto';
                            iframeDoc.body.style.scrollbarWidth = 'none';           // Firefox
                            iframeDoc.body.style.msOverflowStyle = 'none';            // IE 10+
                        }
                        if (iframeDoc.documentElement) {
                            iframeDoc.documentElement.style.overflowY = 'auto';
                            iframeDoc.documentElement.style.scrollbarWidth = 'none';  // Firefox
                            iframeDoc.documentElement.style.msOverflowStyle = 'none';   // IE 10+
                        }
                        
                        // 注入 CSS 隐藏 Webkit 滚动条（Chrome/Safari/Edge）
                        var style = iframeDoc.getElementById('hide-scrollbar-style');
                        if (!style) {
                            style = iframeDoc.createElement('style');
                            style.id = 'hide-scrollbar-style';
                            style.textContent = '::-webkit-scrollbar { display: none !important; width: 0 !important; height: 0 !important; }';
                            iframeDoc.head.appendChild(style);
                        }
                        
                    } catch (error) {
                        iframe.style.height = '600px';
                    }
                },
                
                loadFirstPage: function() {
                    if (state.firstMenu && state.menuList.length > 0) {
                        this.loadPage(state.firstMenu, state.menuList[0].displayText);
                    }
                }
            };
            
            // ==================== 时间管理模块 ====================
            var timeManager = {
                clockInterval: null,
                syncInterval: null,
                
                init: function() {
                    var self = this;
                    this.fetchServerTime().then(function() {
                        self.startClock();
                    });
                },
                
                fetchServerTime: function() {
                    var self = this;
                    return new Promise(function(resolve) {
                        fetch(CONFIG.API.TIME).then(function(response) {
                            if (!response.ok) throw new Error('时间同步失败');
                            return response.json();
                        }).then(function(data) {
                            if (data.timestamp) {
                                state.serverTimeOffset = data.timestamp - Date.now();
                                elements.serverTime.textContent = data.datetime || 
                                    utils.formatDateTime(new Date(data.timestamp));
                            }
                            resolve();
                        }).catch(function(error) {
                            console.warn('使用本地时间:', error);
                            self.useLocalTime();
                            resolve();
                        });
                    });
                },
                
                useLocalTime: function() {
                    elements.serverTime.textContent = utils.formatDateTime(new Date());
                },
                
                startClock: function() {
                    var self = this;
                    
                    if (this.clockInterval) clearInterval(this.clockInterval);
                    if (this.syncInterval) clearInterval(this.syncInterval);
                    
                    this.clockInterval = setInterval(function() {
                        self.updateDisplay();
                    }, CONFIG.INTERVAL.CLOCK_UPDATE);
                    
                    this.syncInterval = setInterval(function() {
                        self.fetchServerTime();
                    }, CONFIG.INTERVAL.TIME_SYNC);
                },
                
                updateDisplay: function() {
                    var now = new Date(Date.now() + state.serverTimeOffset);
                    elements.serverTime.textContent = utils.formatDateTime(now);
                },
                
                destroy: function() {
                    if (this.clockInterval) clearInterval(this.clockInterval);
                    if (this.syncInterval) clearInterval(this.syncInterval);
                }
            };
            
            // ==================== UI 效果模块 ====================
            var uiEffects = {
                createParticles: function() {
                    var container = elements.particles;
                    if (!container) return;
                    
                    for (var i = 0; i < 50; i++) {
                        var particle = document.createElement('div');
                        particle.className = 'particle';
                        particle.style.left = (Math.random() * 100) + '%';
                        particle.style.animationDelay = (Math.random() * 20) + 's';
                        particle.style.animationDuration = (15 + Math.random() * 10) + 's';
                        container.appendChild(particle);
                    }
                },
                
                initFadeIn: function() {
                    document.body.style.opacity = '0';
                    setTimeout(function() {
                        document.body.style.transition = 'opacity 0.5s ease';
                        document.body.style.opacity = '1';
                    }, 100);
                },
                
                toggleSidebar: function() {
                    if (elements.sidebar) {
                        elements.sidebar.classList.toggle('active');
                    }
                }
            };
            
            // ==================== 键盘快捷键模块 ====================
            var keyboardShortcuts = {
                init: function() {
                    var self = this;
                    document.addEventListener('keydown', function(e) {
                        self.handleKeyDown(e);
                    });
                },
                
                handleKeyDown: function(e) {
                    // Alt + S: 切换侧边栏
                    if (e.altKey && (e.key === 's' || e.key === 'S')) {
                        e.preventDefault();
                        uiEffects.toggleSidebar();
                    }
                    
                    // Alt + 1-9: 快速切换菜单
                    if (e.altKey && e.key >= '1' && e.key <= '9') {
                        e.preventDefault();
                        var index = parseInt(e.key) - 1;
                        var menuItem = menuManager.getMenuByIndex(index);
                        if (menuItem) {
                            pageLoader.loadPage(menuItem.name, menuItem.displayText);
                        }
                    }
                    
                    // Alt + Home: 返回首页
                    if (e.altKey && e.key === 'Home') {
                        e.preventDefault();
                        pageLoader.loadFirstPage();
                    }
                }
            };
            
            // ==================== 初始化 ====================
            var init = function() {
                uiEffects.createParticles();
                uiEffects.initFadeIn();
                timeManager.init();
                keyboardShortcuts.init();
                menuManager.loadMenuData();
                
                // 监听窗口大小变化
                window.addEventListener('resize', function() {
                    pageLoader.resizeIframe();
                });
            };
            
            // ==================== 公共 API ====================
            return {
                loadPage: function(menuName, displayText) {
                    return pageLoader.loadPage(menuName, displayText);
                },
                loadFirstPage: function() {
                    return pageLoader.loadFirstPage();
                },
                toggleSidebar: function() {
                    return uiEffects.toggleSidebar();
                },
                init: init
            };
        })();
        
        // 启动应用
        document.addEventListener('DOMContentLoaded', App.init);
    </script>
</body>
</html>