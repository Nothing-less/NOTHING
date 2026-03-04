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
        request.getRequestDispatcher("/WEB-INF/jsp/error_page.jsp").forward(request, response);
        return;
    }
    
    // 获取当前菜单（从参数或默认）
    String currentMenu = request.getParameter("menu");
    if (currentMenu == null) currentMenu = "dashboard";
%>
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
                <a href="?menu=dashboard" class="menu-item <%= "dashboard".equals(currentMenu) ? "active" : "" %>">
                    <span class="menu-icon">📊</span>
                    <span class="menu-text">数据概览</span>
                </a>
                <a href="?menu=users" class="menu-item <%= "users".equals(currentMenu) ? "active" : "" %>">
                    <span class="menu-icon">👥</span>
                    <span class="menu-text">用户管理</span>
                </a>
                <a href="?menu=orders" class="menu-item <%= "orders".equals(currentMenu) ? "active" : "" %>">
                    <span class="menu-icon">📦</span>
                    <span class="menu-text">订单管理</span>
                </a>
                <a href="?menu=products" class="menu-item <%= "products".equals(currentMenu) ? "active" : "" %>">
                    <span class="menu-icon">🛍️</span>
                    <span class="menu-text">商品管理</span>
                </a>
                <a href="?menu=analytics" class="menu-item <%= "analytics".equals(currentMenu) ? "active" : "" %>">
                    <span class="menu-icon">📈</span>
                    <span class="menu-text">数据分析</span>
                </a>
                <a href="?menu=settings" class="menu-item <%= "settings".equals(currentMenu) ? "active" : "" %>">
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
                    <a href="?menu=dashboard">首页</a>
                    <span>/</span>
                    <span id="breadcrumbCurrent">数据概览</span>
                </div>
            </div>
            <%-- <div style="color: var(--text-muted); font-size: 14px;">
                <%= new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm").format(new java.util.Date()) %>
            </div> --%>
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

        <!-- 动态内容区 -->
        <div class="content-grid" id="contentArea">
            <!-- 根据菜单加载不同内容 -->
            <% if ("dashboard".equals(currentMenu)) { %>
                <!-- 仪表盘 -->
                <div class="card">
                    <div class="card-header">
                        <span class="card-title">总用户数</span>
                        <div class="card-icon">👤</div>
                    </div>
                    <div class="card-value">1,284</div>
                    <div class="card-desc">较上月增长 +12.5%</div>
                </div>
                <div class="card">
                    <div class="card-header">
                        <span class="card-title">今日订单</span>
                        <div class="card-icon">📋</div>
                    </div>
                    <div class="card-value">86</div>
                    <div class="card-desc">较昨日增长 +5.2%</div>
                </div>
                <div class="card">
                    <div class="card-header">
                        <span class="card-title">销售额</span>
                        <div class="card-icon">💰</div>
                    </div>
                    <div class="card-value">¥12,580</div>
                    <div class="card-desc">今日实时统计</div>
                </div>
                <div class="card">
                    <div class="card-header">
                        <span class="card-title">系统状态</span>
                        <div class="card-icon">✅</div>
                    </div>
                    <div class="card-value" style="color: #10b981;">正常</div>
                    <div class="card-desc">运行时间 15天 3小时</div>
                </div>

            <% } else if ("users".equals(currentMenu)) { %>
                <!-- 用户管理 -->
                <div class="card" style="grid-column: 1 / -1;">
                    <div class="card-header">
                        <span class="card-title">用户列表</span>
                        <button class="fab" style="position: static; width: auto; height: auto; padding: 10px 20px; border-radius: 8px; font-size: 14px;">+ 新增用户</button>
                    </div>
                    <table style="width: 100%; border-collapse: collapse; margin-top: 20px;">
                        <thead>
                            <tr style="color: var(--text-muted); font-size: 14px; text-align: left;">
                                <th style="padding: 15px; border-bottom: 1px solid var(--glass-border);">用户账号</th>
                                <th style="padding: 15px; border-bottom: 1px solid var(--glass-border);">最后登录</th>
                                <th style="padding: 15px; border-bottom: 1px solid var(--glass-border);">状态</th>
                                <th style="padding: 15px; border-bottom: 1px solid var(--glass-border);">操作</th>
                            </tr>
                        </thead>
                        <tbody>
                            <tr>
                                <td style="padding: 15px; border-bottom: 1px solid var(--glass-border);"><%= currentUser.getUserAccount() %></td>
                                <td style="padding: 15px; border-bottom: 1px solid var(--glass-border); color: var(--text-muted);"><%= currentUser.getLastLoginTime() %></td>
                                <td style="padding: 15px; border-bottom: 1px solid var(--glass-border);"><span style="color: #10b981;">● 在线</span></td>
                                <td style="padding: 15px; border-bottom: 1px solid var(--glass-border);">
                                    <a href="#" style="color: var(--primary-color); text-decoration: none; margin-right: 15px;">编辑</a>
                                    <a href="#" style="color: #ef4444; text-decoration: none;">禁用</a>
                                </td>
                            </tr>
                        </tbody>
                    </table>
                </div>

            <% } else { %>
                <!-- 其他菜单占位 -->
                <div class="card" style="grid-column: 1 / -1; text-align: center; padding: 60px;">
                    <div style="font-size: 48px; margin-bottom: 20px;">🚧</div>
                    <h3 style="color: var(--text-light); margin-bottom: 10px;">功能开发中</h3>
                    <p style="color: var(--text-muted);"><%= currentMenu %> 模块即将上线</p>
                </div>
            <% } %>
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

        // 自动更新标题（页面加载时）
        const urlParams = new URLSearchParams(window.location.search);
        const currentMenu = urlParams.get('menu') || 'dashboard';
        updateTitle(currentMenu);

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
                    window.location.href = '?menu=' + menus[index];
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
        });
    </script>
</body>

<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>主页 - <%= currentUser.getUserAccount() %></title>
    <style>
        * {
            margin: 0;
            padding: 0;
            box-sizing: border-box;
        }

        :root {
            --sidebar-width: 260px;
            --sidebar-collapsed: 60px;
            --primary-color: #6366f1;
            --primary-dark: #4f46e5;
            --bg-dark: #1e1b4b;
            --bg-darker: #0f0e2e;
            --text-light: #e0e7ff;
            --text-muted: #818cf8;
            --glass-bg: rgba(30, 27, 75, 0.85);
            --glass-border: rgba(99, 102, 241, 0.2);
        }

        body {
            font-family: 'Segoe UI', system-ui, -apple-system, sans-serif;
            background: linear-gradient(135deg, #0f0e2e 0%, #1e1b4b 50%, #312e81 100%);
            min-height: 100vh;
            overflow: hidden;
            color: #fff;
        }

        /* 背景动画粒子 */
        .particles {
            position: fixed;
            top: 0;
            left: 0;
            width: 100%;
            height: 100%;
            pointer-events: none;
            overflow: hidden;
            z-index: 0;
        }

        .particle {
            position: absolute;
            width: 4px;
            height: 4px;
            background: rgba(99, 102, 241, 0.5);
            border-radius: 50%;
            animation: float 20s infinite linear;
        }

        @keyframes float {
            0% {
                transform: translateY(100vh) translateX(0);
                opacity: 0;
            }
            10% { opacity: 1; }
            90% { opacity: 1; }
            100% {
                transform: translateY(-100vh) translateX(100px);
                opacity: 0;
            }
        }

        /* 侧边栏容器 */
        .sidebar-container {
            position: fixed;
            left: 0;
            top: 0;
            height: 100vh;
            z-index: 1000;
            transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
        }

        /* 触发区域（鼠标感应区） */
        .sidebar-trigger {
            position: absolute;
            left: 0;
            top: 0;
            width: 20px;
            height: 100%;
            z-index: 999;
        }

        /* 侧边栏主体 */
        .sidebar {
            width: var(--sidebar-width);
            height: 100%;
            background: var(--glass-bg);
            backdrop-filter: blur(20px);
            border-right: 1px solid var(--glass-border);
            padding: 20px;
            transform: translateX(calc(-100% + var(--sidebar-collapsed)));
            transition: transform 0.4s cubic-bezier(0.4, 0, 0.2, 1);
            display: flex;
            flex-direction: column;
            box-shadow: 10px 0 40px rgba(0,0,0,0.3);
            overflow: hidden;
        }

        /* 展开状态 */
        .sidebar-container:hover .sidebar,
        .sidebar-container.active .sidebar {
            transform: translateX(0);
        }

        /* 收缩时的指示条 */
        .sidebar-collapsed-indicator {
            position: absolute;
            right: 0;
            top: 50%;
            transform: translateY(-50%);
            width: 4px;
            height: 60px;
            background: linear-gradient(180deg, transparent, var(--primary-color), transparent);
            border-radius: 2px;
            opacity: 0;
            transition: opacity 0s;
        }

        .sidebar-container:hover .sidebar-collapsed-indicator {
            opacity: 0;
        }

        /* 用户头像区 */
        .user-profile {
            text-align: center;
            padding: 20px 0;
            border-bottom: 1px solid var(--glass-border);
            margin-bottom: 20px;
        }

        .avatar {
            width: 70px;
            height: 70px;
            border-radius: 50%;
            background: linear-gradient(135deg, var(--primary-color), var(--primary-dark));
            display: flex;
            align-items: center;
            justify-content: center;
            margin: 0 auto 15px;
            font-size: 28px;
            font-weight: bold;
            box-shadow: 0 10px 30px rgba(99, 102, 241, 0.4);
            animation: pulse 2s infinite;
        }

        @keyframes pulse {
            0%, 100% { transform: scale(1); }
            50% { transform: scale(1.05); }
        }

        .username {
            font-size: 16px;
            font-weight: 600;
            color: var(--text-light);
        }

        .user-role {
            font-size: 12px;
            color: var(--text-muted);
            margin-top: 5px;
        }

        /* 菜单样式 */
        .menu {
            flex: 1;
            overflow-y: auto;
            overflow-x: hidden;
            scrollbar-width: none;
            -ms-overflow-style: none;
        }

        .menu-item {
            display: flex;
            align-items: center;
            padding: 14px 18px;
            margin: 8px 0;
            border-radius: 12px;
            cursor: pointer;
            transition: all 0.3s ease;
            color: var(--text-muted);
            text-decoration: none;
            position: relative;
            overflow: hidden;
        }

        .menu-item::before {
            content: '';
            position: absolute;
            left: 0;
            top: 0;
            width: 0;
            height: 100%;
            background: linear-gradient(90deg, var(--primary-color), transparent);
            transition: width 0.3s ease;
        }

        .menu-item:hover::before,
        .menu-item.active::before {
            width: 100%;
        }

        .menu-item:hover,
        .menu-item.active {
            background: rgba(99, 102, 241, 0.15);
            color: var(--text-light);
            transform: translateX(5px);
        }

        .menu-icon {
            width: 24px;
            height: 24px;
            margin-right: 15px;
            display: flex;
            align-items: center;
            justify-content: center;
            font-size: 18px;
            z-index: 1;
        }

        .menu-text {
            font-size: 14px;
            font-weight: 500;
            z-index: 1;
            white-space: nowrap;
        }

        /* 收缩时只显示图标 */
        .sidebar-container:not(:hover):not(.active) .menu-text,
        .sidebar-container:not(:hover):not(.active) .username,
        .sidebar-container:not(:hover):not(.active) .user-role {
            opacity: 0;
            transition: opacity 0.2s;
        }
        .menu::-webkit-scrollbar {
            display: none;
        }

        /* 底部操作区 */
        .sidebar-footer {
            border-top: 1px solid var(--glass-border);
            padding-top: 20px;
            margin-top: auto;
        }

        .logout-btn {
            display: flex;
            align-items: center;
            padding: 12px 18px;
            border-radius: 10px;
            color: #ef4444;
            cursor: pointer;
            transition: all 0.3s;
            text-decoration: none;
        }

        .logout-btn:hover {
            background: rgba(239, 68, 68, 0.15);
        }

        /* 主内容区 */
        .main-content {
            margin-left: var(--sidebar-collapsed);
            min-height: 100vh;
            padding: 30px;
            transition: margin-left 0.4s ease;
            position: relative;
            z-index: 10;
        }

        .sidebar-container:hover ~ .main-content,
        .sidebar-container.active ~ .main-content {
            margin-left: var(--sidebar-width);
        }

        /* 顶部栏 */
        .top-bar {
            display: flex;
            justify-content: space-between;
            align-items: center;
            margin-bottom: 30px;
            padding: 20px 30px;
            background: var(--glass-bg);
            backdrop-filter: blur(20px);
            border-radius: 16px;
            border: 1px solid var(--glass-border);
            animation: slideDown 0.6s ease;
        }

        @keyframes slideDown {
            from {
                opacity: 0;
                transform: translateY(-20px);
            }
            to {
                opacity: 1;
                transform: translateY(0);
            }
        }

        .page-title {
            font-size: 28px;
            font-weight: 700;
            background: linear-gradient(135deg, #fff, #818cf8);
            -webkit-background-clip: text;
            -webkit-text-fill-color: transparent;
        }

        .breadcrumb {
            display: flex;
            align-items: center;
            gap: 10px;
            color: var(--text-muted);
            font-size: 14px;
        }

        .breadcrumb a {
            color: var(--text-muted);
            text-decoration: none;
            transition: color 0.3s;
        }

        .breadcrumb a:hover {
            color: var(--primary-color);
        }

        /* 内容卡片 */
        .content-grid {
            display: grid;
            grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
            gap: 25px;
            animation: fadeIn 0.8s ease;
        }

        @keyframes fadeIn {
            from { opacity: 0; transform: translateY(20px); }
            to { opacity: 1; transform: translateY(0); }
        }

        .card {
            background: var(--glass-bg);
            backdrop-filter: blur(20px);
            border: 1px solid var(--glass-border);
            border-radius: 20px;
            padding: 25px;
            transition: all 0.4s ease;
            position: relative;
            overflow: hidden;
        }

        .card::before {
            content: '';
            position: absolute;
            top: 0;
            left: 0;
            width: 100%;
            height: 3px;
            background: linear-gradient(90deg, var(--primary-color), var(--primary-dark));
            transform: scaleX(0);
            transition: transform 0.4s ease;
        }

        .card:hover {
            transform: translateY(-5px);
            box-shadow: 0 20px 40px rgba(0,0,0,0.3);
        }

        .card:hover::before {
            transform: scaleX(1);
        }

        .card-header {
            display: flex;
            align-items: center;
            justify-content: space-between;
            margin-bottom: 20px;
        }

        .card-title {
            font-size: 18px;
            font-weight: 600;
            color: var(--text-light);
        }

        .card-icon {
            width: 48px;
            height: 48px;
            border-radius: 12px;
            background: linear-gradient(135deg, var(--primary-color), var(--primary-dark));
            display: flex;
            align-items: center;
            justify-content: center;
            font-size: 24px;
        }

        .card-value {
            font-size: 36px;
            font-weight: 700;
            color: #fff;
            margin: 10px 0;
        }

        .card-desc {
            font-size: 14px;
            color: var(--text-muted);
        }

        /* 快捷操作按钮 */
        .fab {
            position: fixed;
            bottom: 30px;
            right: 30px;
            width: 60px;
            height: 60px;
            border-radius: 50%;
            background: linear-gradient(0deg, var(--primary-color), var(--primary-dark));
            border: none;
            color: white;
            font-size: 24px;
            cursor: pointer;
            box-shadow: 0 10px 30px rgba(99, 102, 241, 0.4);
            transition: all 0.3s ease;
            z-index: 100;
        }

        .fab:hover {
            transform: scale(1.1) rotate(0deg);
            box-shadow: 0 15px 40px rgba(99, 102, 241, 0.6);
        }

        /* 响应式 */
        @media (max-width: 768px) {
            .sidebar {
                transform: translateX(-100%);
            }
            .main-content {
                margin-left: 0;
            }
        }

        /* 加载动画 */
        .loading {
            display: inline-block;
            width: 20px;
            height: 20px;
            border: 3px solid rgba(255,255,255,0.3);
            border-radius: 50%;
            border-top-color: var(--primary-color);
            animation: spin 1s ease-in-out infinite;
        }

        @keyframes spin {
            to { transform: rotate(360deg); }
        }
    </style>
</head>
</html>