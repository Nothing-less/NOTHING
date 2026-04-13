var App = (function() {
    // 配置管理
    var configManager = {
        config: null,
        userInfo: null,
        
        init: function() {
            var self = this;
            var apiBase = document.body.dataset.apiBase || '';
            
            return Promise.all([
                this.fetchConfig(apiBase),
                this.fetchCurrentUser(apiBase)
            ]).then(function(results) {
                self.config = results[0];
                self.userInfo = results[1];
                return self.config;
            });
        },
        
        fetchConfig: function(apiBase) {
            return fetch(apiBase + '/api/config').then(function(response) {
                if (!response.ok) throw new Error('获取配置失败');
                return response.json();
            }).then(function(result) {
                if (result.code !== 200) throw new Error(result.message || '配置加载失败');
                return result.data;
            }).catch(function(error) {
                console.warn('使用默认配置:', error);
                return {
                    contextPath: apiBase,
                    currentMenu: 'dashboard',
                    intervals: { clock: 1000, sync: 30000 }
                };
            });
        },
        
        fetchCurrentUser: function(apiBase) {
            return fetch(apiBase + '/api/user').then(function(response) {
                if (!response.ok) throw new Error('获取用户信息失败');
                return response.json();
            }).then(function(result) {
                if (result.code !== 200) throw new Error(result.message || '用户信息加载失败');
                return result.data;
            });
        },
        
        getConfig: function() { return this.config; },
        getUserInfo: function() { return this.userInfo; }
    };
    
    // 图标映射
    var ICON_MAP = {
        orders: '📦', users: '👥', products: '🛍️',
        dashboard: '📊', analytics: '📈', settings: '⚙️',
        default: '📄'
    };
    
    // 状态
    var state = {
        menuList: [], currentMenu: '', firstMenu: '',
        serverTimeOffset: 0, isLoading: false
    };
    
    // DOM 元素
    var elements = {
        dynamicMenu: document.getElementById('dynamicMenu'),
        pageTitle: document.getElementById('pageTitle'),
        breadcrumbCurrent: document.getElementById('breadcrumbCurrent'),
        contentFrame: document.getElementById('contentFrame'),
        iframeLoading: document.getElementById('iframeLoading'),
        serverTime: document.getElementById('serverTime'),
        sidebar: document.getElementById('sidebar'),
        particles: document.getElementById('particles'),
        userAvatar: document.getElementById('userAvatar'),
        userName: document.getElementById('userName'),
        userRole: document.getElementById('userRole')
    };
    
    // 工具函数
    var utils = {
        getIcon: function(name) { return ICON_MAP[name] || ICON_MAP.default; },
        escapeHtml: function(text) {
            if (!text) return '';
            var div = document.createElement('div');
            div.textContent = text;
            return div.innerHTML;
        },
        formatDateTime: function(date) {
            var pad = function(n) { return n < 10 ? '0' + n : n; };
            return date.getFullYear() + '-' + pad(date.getMonth() + 1) + '-' + pad(date.getDate()) + ' ' +
                   pad(date.getHours()) + ':' + pad(date.getMinutes()) + ':' + pad(date.getSeconds());
        },
        getInitial: function(name) { return name ? name.substring(0, 1).toUpperCase() : '?'; }
    };
    
    // 用户渲染
    var userRenderer = {
        render: function(userInfo) {
            if (!userInfo) return;
            var safeAccount = utils.escapeHtml(userInfo.nickname);
            elements.userAvatar.textContent = utils.getInitial(safeAccount);
            elements.userName.textContent = safeAccount;
            elements.userRole.textContent = utils.escapeHtml(userInfo.roleId || '-');
            document.title = '主页 - ' + safeAccount;
        }
    };
    
    // 菜单管理
    var menuManager = {
        apiUrl: '',
        init: function(config) { this.apiUrl = config.contextPath + '/api/menu'; },
        
        loadMenuData: function() {
            var self = this;
            fetch(this.apiUrl).then(function(response) {
                if (!response.ok) throw new Error('HTTP ' + response.status);
                return response.json();
            }).then(function(result) {
                if (!result || result.code !== 200) throw new Error(result?.message || '加载菜单失败');
                var menuData = result.data || [];
                if (menuData.length === 0) { self.renderEmptyMenu(); return; }
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
                (function(currentItem) {
                    menuItem.onclick = function() { pageLoader.loadPage(currentItem.name, currentItem.displayText); };
                })(item);
                menuItem.innerHTML = '<span class="menu-icon">' + utils.getIcon(item.name) + '</span>' +
                                    '<span class="menu-text">' + item.displayText + '</span>';
                if (i < 9) menuItem.title = 'Alt+' + (i + 1);
                elements.dynamicMenu.appendChild(menuItem);
            }
        },
        
        renderEmptyMenu: function() {
            elements.dynamicMenu.innerHTML = '<div class="menu-empty">暂无可用菜单</div>';
        },
        
        renderError: function(msg) {
            elements.dynamicMenu.innerHTML = '<div class="menu-error">菜单加载失败<br><small>' + utils.escapeHtml(msg) + '</small></div>';
        },
        
        loadDefaultPage: function() {
            var urlParams = new URLSearchParams(window.location.search);
            var targetMenu = urlParams.get('menu');
            var config = configManager.getConfig();
            var menuToLoad = targetMenu || config?.currentMenu;
            
            for (var i = 0; i < state.menuList.length; i++) {
                if (state.menuList[i].name === menuToLoad) {
                    pageLoader.loadPage(menuToLoad, state.menuList[i].displayText);
                    return;
                }
            }
            if (state.firstMenu) {
                pageLoader.loadPage(state.firstMenu, state.menuList[0].displayText);
            }
        },
        
        setActiveMenu: function(menuName) {
            var items = document.querySelectorAll('.menu-item');
            for (var i = 0; i < items.length; i++) items[i].classList.remove('active');
            var active = document.getElementById('menu-' + menuName);
            if (active) active.classList.add('active');
        },
        
        getMenuByIndex: function(index) { return state.menuList[index] || null; }
    };
    
    // 页面加载
    var pageLoader = {
        config: null,
        init: function(config) { this.config = config; },
        
        loadPage: function(menuName, displayText) {
            if (!menuName || state.isLoading) return;
            state.isLoading = true;
            elements.iframeLoading.classList.add('active');
            state.currentMenu = menuName;
            menuManager.setActiveMenu(menuName);
            this.updatePageInfo(displayText);
            var pageUrl = this.config.contextPath + '/page/' + encodeURIComponent(menuName);
            this.loadIframePage(pageUrl);
        },
        
        updatePageInfo: function(displayText) {
            var userInfo = configManager.getUserInfo();
            elements.pageTitle.textContent = displayText;
            elements.breadcrumbCurrent.textContent = displayText;
            document.title = displayText + ' - ' + (userInfo?.nickname || '');
        },
        
        loadIframePage: function(url) {
            var self = this, iframe = elements.contentFrame, timeoutId;
            var cleanup = function() {
                clearTimeout(timeoutId);
                iframe.removeEventListener('load', onLoad);
                iframe.removeEventListener('error', onError);
            };
            var onLoad = function() {
                cleanup();
                elements.iframeLoading.classList.remove('active');
                self.resizeIframe();
                state.isLoading = false;
            };
            var onError = function() { cleanup(); state.isLoading = false; };
            iframe.addEventListener('load', onLoad);
            iframe.addEventListener('error', onError);
            timeoutId = setTimeout(function() { cleanup(); state.isLoading = false; }, 10000);
            iframe.src = url;
        },
        
        resizeIframe: function() {
            var iframe = elements.contentFrame;
            if (!iframe) return;
            try {
                var doc = iframe.contentDocument || iframe.contentWindow?.document;
                if (!doc) return;
                var height = doc.documentElement?.scrollHeight || doc.body?.scrollHeight || 600;
                iframe.style.height = height + 'px';
                var style = doc.getElementById('hide-scrollbar-style');
                if (!style) {
                    style = doc.createElement('style');
                    style.id = 'hide-scrollbar-style';
                    style.textContent = '::-webkit-scrollbar { display: none !important; }';
                    doc.head.appendChild(style);
                }
                if (doc.body) { doc.body.style.overflowY = 'auto'; doc.body.style.scrollbarWidth = 'none'; }
            } catch (e) { iframe.style.height = '600px'; }
        },
        
        loadFirstPage: function() {
            if (state.firstMenu && state.menuList.length > 0) {
                this.loadPage(state.firstMenu, state.menuList[0].displayText);
            }
        }
    };
    
    // 时间管理
    var timeManager = {
        intervals: [],
        apiUrl: '', syncTime: 30000,
        init: function(config) {
            this.apiUrl = config.contextPath + '/api/time';
            this.syncTime = config.intervals?.sync || 30000;
        },
        start: function() {
            var self = this;
            this.fetchServerTime().then(function() { self.startClock(); });
        },
        fetchServerTime: function() {
            var self = this;
            return fetch(this.apiUrl).then(function(r) { return r.json(); })
                .then(function(data) {
                    if (data.timestamp) {
                        state.serverTimeOffset = data.timestamp - Date.now();
                        elements.serverTime.textContent = data.datetime || utils.formatDateTime(new Date(data.timestamp));
                    }
                }).catch(function(e) {
                    console.warn('使用本地时间:', e);
                    elements.serverTime.textContent = utils.formatDateTime(new Date());
                });
        },
        startClock: function() {
            var self = this;
            this.intervals.push(setInterval(function() {
                var now = new Date(Date.now() + state.serverTimeOffset);
                elements.serverTime.textContent = utils.formatDateTime(now);
            }, 1000));
            this.intervals.push(setInterval(function() { self.fetchServerTime(); }, this.syncTime));
        }
    };
    
    // UI 效果
    var uiEffects = {
        createParticles: function() {
            var container = elements.particles;
            if (!container) return;
            for (var i = 0; i < 50; i++) {
                var p = document.createElement('div');
                p.className = 'particle';
                p.style.left = Math.random() * 100 + '%';
                p.style.animationDelay = Math.random() * 20 + 's';
                p.style.animationDuration = (15 + Math.random() * 10) + 's';
                container.appendChild(p);
            }
        },
        initFadeIn: function() {
            document.body.style.opacity = '0';
            setTimeout(function() {
                document.body.style.transition = 'opacity 0.5s ease';
                document.body.style.opacity = '1';
            }, 100);
        },
        toggleSidebar: function() { elements.sidebar?.classList.toggle('active'); }
    };
    
    // 键盘快捷键
    var keyboardShortcuts = {
        init: function() {
            document.addEventListener('keydown', function(e) {
                if (e.altKey && (e.key === 's' || e.key === 'S')) {
                    e.preventDefault(); uiEffects.toggleSidebar();
                }
                if (e.altKey && e.key >= '1' && e.key <= '9') {
                    e.preventDefault();
                    var item = menuManager.getMenuByIndex(parseInt(e.key) - 1);
                    if (item) pageLoader.loadPage(item.name, item.displayText);
                }
                if (e.altKey && e.key === 'Home') {
                    e.preventDefault(); pageLoader.loadFirstPage();
                }
            });
        }
    };
    
    // 初始化
    var init = function() {
        configManager.init().then(function(config) {
            userRenderer.render(configManager.getUserInfo());
            menuManager.init(config);
            pageLoader.init(config);
            timeManager.init(config);
            uiEffects.createParticles();
            uiEffects.initFadeIn();
            timeManager.start();
            keyboardShortcuts.init();
            menuManager.loadMenuData();
            window.addEventListener('resize', function() { pageLoader.resizeIframe(); });
        }).catch(function(error) {
            console.error('初始化失败:', error);
            elements.dynamicMenu.innerHTML = '<div class="menu-error">系统初始化失败</div>';
        });
    };
    
    return {
        loadPage: function(n, d) { pageLoader.loadPage(n, d); },
        loadFirstPage: function() { pageLoader.loadFirstPage(); },
        toggleSidebar: function() { uiEffects.toggleSidebar(); },
        init: init
    };
})();

document.addEventListener('DOMContentLoaded', App.init);