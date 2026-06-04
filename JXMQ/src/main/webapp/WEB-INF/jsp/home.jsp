<%@ page contentType="text/html; charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ page import="icu.nothingless.commons.RespEntity" %>
<%@ page import="icu.nothingless.pojo.dto.User" %>
<%@ page import="icu.nothingless.tools.ViewUtil" %>
<%@ page import="icu.nothingless.tools.RedirectUtil" %>
<%@ page import="java.util.Map" %>
<%@ page import="java.time.LocalDateTime" %>
<%@ page import="java.time.format.DateTimeFormatter" %>
<%
    String serverDateTime = java.time.LocalDateTime.now()
        .format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    long serverTimestamp = System.currentTimeMillis();
/***********************************************************************************************/
    User currentUser = (User) session.getAttribute("CURRENT_USER");
    if (currentUser == null) {
        request.setAttribute("respEntity", RespEntity.error("错误！系统出现异常！"));
        ViewUtil.render(request, response, "error_page");
        return;
    }
    Object currentUser_ID = currentUser.userId();
    session.setAttribute("CURRENT_USER_ID", currentUser_ID);
    String contextPath = request.getContextPath();
%>
<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>主页</title>
    <link rel="preconnect" href="<%= contextPath %>">
    <link rel="stylesheet" href="<c:url value='/static/css/pages.css' />">
    <link rel="stylesheet" href="<c:url value='/static/css/tables.css' />">
    <script src="<c:url value='/static/js/home.js'       />" defer></script>
    <script src="<c:url value='/static/js/ChatClient.js' />" defer></script>

    <style>
        /* 聊天窗口容器 - 必须 display:flex 才能显示 */
        .chat-window {
            position: fixed !important;
            display: none;
            flex-direction: column;
            background: #1e1b4b;
            border-radius: 12px;
            border: 1px solid rgba(99, 102, 241, 0.3);
            box-shadow: 0 8px 32px rgba(0,0,0,0.4), 0 0 0 1px rgba(99,102,241,0.1);
            overflow: hidden;
            z-index: 1000;
            min-width: 320px;
            min-height: 240px;
            /* 初始居中定位 */
            left: 50%;
            top: 50%;
            transform: translate(-50%, -50%);
            width: 520px;
            height: 600px;
            color: #fff;
        }

        /* active 状态显示窗口 - 这是关键！ */
        .chat-window.active {
            display: flex !important;
        }

        /* 拖动/调整大小时的激活样式 */
        .chat-window.dragging,
        .chat-window.resizing {
            box-shadow: 0 12px 48px rgba(0,0,0,0.5), 0 0 0 2px rgba(99,102,241,0.4);
            transition: none;
            opacity: 0.95;
        }

        /* 标题栏 */
        .chat-window-header {
            display: flex;
            align-items: center;
            justify-content: space-between;
            padding: 0 14px;
            height: 44px;
            background: linear-gradient(135deg, #6366f1 0%, #4f46e5 100%);
            color: white;
            cursor: move;
            user-select: none;
            flex-shrink: 0;
            border-bottom: 1px solid rgba(255,255,255,0.1);
        }

        .chat-window-title {
            font-size: 14px;
            font-weight: 500;
            overflow: hidden;
            text-overflow: ellipsis;
            white-space: nowrap;
            flex: 1;
            padding-right: 10px;
        }

        .chat-window-controls {
            display: flex;
            gap: 6px;
        }

        .chat-window-btn {
            width: 28px;
            height: 28px;
            border: none;
            background: rgba(255,255,255,0.15);
            color: white;
            border-radius: 6px;
            cursor: pointer;
            font-size: 16px;
            line-height: 1;
            display: flex;
            align-items: center;
            justify-content: center;
            transition: all 0.2s;
        }

        .chat-window-btn:hover {
            background: rgba(255,255,255,0.3);
            transform: scale(1.1);
        }

        .chat-window-btn.close:hover {
            background: #ef4444;
        }

        /* iframe 容器 */
        .chat-window-body {
            flex: 1;
            overflow: hidden;
            position: relative;
            background: #0f0e2e;
        }

        .chat-window-body iframe {
            width: 100%;
            height: 100%;
            border: none;
            display: block;
        }

        /* 调整大小手柄 - 8个方向 */
        .resize-handle {
            position: absolute;
            z-index: 20;
        }
        .resize-handle.n  { top: 0; left: 8px; right: 8px; height: 6px; cursor: n-resize; }
        .resize-handle.s  { bottom: 0; left: 8px; right: 8px; height: 6px; cursor: s-resize; }
        .resize-handle.w  { left: 0; top: 8px; bottom: 8px; width: 6px; cursor: w-resize; }
        .resize-handle.e  { right: 0; top: 8px; bottom: 8px; width: 6px; cursor: e-resize; }
        .resize-handle.nw { top: 0; left: 0; width: 14px; height: 14px; cursor: nw-resize; }
        .resize-handle.ne { top: 0; right: 0; width: 14px; height: 14px; cursor: ne-resize; }
        .resize-handle.sw { bottom: 0; left: 0; width: 14px; height: 14px; cursor: sw-resize; }
        .resize-handle.se { bottom: 0; right: 0; width: 14px; height: 14px; cursor: se-resize; }

        /* 手柄悬停高亮 */
        .resize-handle:hover {
            background: rgba(99, 102, 241, 0.3);
        }

        /* 拖动遮罩 - 防止iframe捕获鼠标 */
        .chat-window-drag-mask {
            position: absolute;
            top: 0; left: 0; right: 0; bottom: 0;
            z-index: 9999;
            display: none;
            background: transparent;
        }

        .chat-window-drag-mask.active {
            display: block;
        }

        /* 多窗口层级 */
        .chat-window.focused {
            box-shadow: 0 8px 32px rgba(99,102,241,0.3), 0 0 0 1px rgba(99,102,241,0.2);
        }
    </style>
</head>
<body data-api-base="<%= contextPath %>">

    <div class="particles" id="particles"></div>

    <div class="sidebar-container" id="sidebar">
        <div class="sidebar">
            <div class="user-profile" id="userProfile">
                <div class="avatar" id="userAvatar">?</div>
                <div class="username" id="userName">加载中...</div>
                <div class="user-role" id="userRole">-</div>
            </div>

            <nav class="menu" id="dynamicMenu">
                <div class="menu-loading">
                    <div class="loading"></div>
                    <div style="margin-top: 10px;">加载菜单中...</div>
                </div>
            </nav>

            <div class="sidebar-footer">
                <a href="<%= contextPath %>/logout" id="logoutLink" class="logout-btn">
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
            <div class="server-time" id="serverTime" data-timestamp="<%= serverTimestamp %>">
                <%= serverDateTime %>
            </div>
        </div>

        <div class="content-wrapper" id="contentWrapper">
            <div class="iframe-loading" id="iframeLoading">
                <div class="loading"></div>
            </div>
            <iframe class="content-iframe" id="contentFrame" name="contentFrame"></iframe>
        </div>
    </main>

    <!-- 聊天窗口容器 -->
    <div id="chatWindowsContainer"></div>

    <button class="fab" onclick="App.toggleSidebar()" title="切换侧边栏 (Alt+S)">☰</button>

    <script>
        // ========== 调试模式 ==========
        const CHAT_DEBUG = false;
        function chatLog(...args) {
            if (CHAT_DEBUG) console.log('[ChatWindow]', ...args);
        }

        document.addEventListener('DOMContentLoaded', function() {
            chatLog('DOM loaded, initializing chat system...');

            var contextPath = '<%= contextPath %>';
            
            // ========== 【关键】将 chat 实例挂载到 window，供 iframe 访问 ==========
            window.chatClient = new ChatClient('<%= currentUser.userId() %>');
            const chat = window.chatClient;
            // =====================================================================
            
            // home.jsp 中
            chat.on('sent', function(msg) {
                // 找到对应的聊天窗口，通知消息发送成功
                const win = ChatWindowManager.windows[msg.toUserId];
                if (win) {
                    const iframe = win.querySelector('iframe');
                    if (iframe && iframe.contentWindow) {
                        iframe.contentWindow.postMessage({
                            type: 'SENT_ACK',
                            messageId: msg.messageId
                        }, '*');
                    }
                }
            });
            
            chat.on('disconnected', function(e) {
                console.log('WebSocket disconnected');
                if (e.permanent) {
                    showError('连接已断开，请刷新页面重试');
                }
            });

            // ========== 【关键】收到消息后转发给对应的聊天窗口 ==========
            chat.on('message', function(msg) {
                chatLog('Received message via WebSocket:', msg);
                
                // msg 的结构取决于后端发送格式
                // ChatWebSocketServer 发送的是: {type: "CHAT", message: Message对象}
                const actualMsg = msg.message || msg;
                const senderId = actualMsg.senderId;
                
                // 找到对应的聊天窗口并转发
                const win = ChatWindowManager.windows[senderId];
                if (win) {
                    const iframe = win.querySelector('iframe');
                    if (iframe && iframe.contentWindow) {
                        iframe.contentWindow.postMessage({
                            type: 'CHAT_MESSAGE',
                            message: actualMsg
                        }, '*');
                    }
                    // 窗口闪烁或提示
                    if (!win.classList.contains('focused')) {
                        win.querySelector('.chat-window-header').style.background = 'linear-gradient(135deg, #ef4444 0%, #dc2626 100%)';
                    }
                } else {
                    // 没有打开聊天窗口，更新好友列表未读数
                    // 可以在这里调用 loadFriends() 或局部更新
                    chatLog('Message from unopened chat, friendId:', senderId);
                }
            });
            // =========================================================

            // 监听来自 iframe 的消息（打开聊天窗口）
            window.addEventListener('message', function(event) {
                chatLog('Received message from iframe:', event.data);
                if (event.origin !== window.location.origin) {
                    // 开发环境可注释
                    // return;
                }
                const data = event.data;
                if (data && data.type === 'OPEN_CHAT') {
                    chatLog('Opening chat window for:', data.friendId, data.nickname);
                    openChatWindow(data.friendId, data.nickname);
                }
            });

            chatLog('Message listener registered');
        });

        window.CURRENT_USER = {
            userId: '<%= currentUser.userId() %>',
            nickname: '<%= currentUser.nickname() %>'
        };

        // ========== 聊天窗口管理器 ==========
        const ChatWindowManager = {
            windows: {},
            zIndexBase: 1000,

            createWindow(friendId, nickname) {
                chatLog('createWindow called:', friendId, nickname);

                // 如果已存在，置顶并返回
                if (this.windows[friendId]) {
                    chatLog('Window already exists, bringing to front');
                    this.restore(friendId);
                    this.bringToFront(friendId);
                    return this.windows[friendId];
                }

                const container = document.getElementById('chatWindowsContainer');
                const windowId = 'chat-window-' + friendId;

                chatLog('Creating DOM element:', windowId);

                const win = document.createElement('div');
                win.id = windowId;
                win.className = 'chat-window active';  // 直接加 active
                win.dataset.friendId = friendId;
                win.style.zIndex = ++this.zIndexBase;

                // 使用模板字符串构建HTML（更清晰）
                const safeNickname = escapeHtml(nickname);

                win.innerHTML = `
                    <div class="chat-window-drag-mask" id="${windowId}-mask"></div>
                    <div class="chat-window-header" id="${windowId}-header">
                        <span class="chat-window-title">💬 ${safeNickname}</span>
                        <div class="chat-window-controls">
                            <button class="chat-window-btn minimize" title="最小化">−</button>
                            <button class="chat-window-btn close" title="关闭">×</button>
                        </div>
                    </div>
                    <div class="chat-window-body">
                        <iframe src="about:blank" id="${windowId}-frame"></iframe>
                    </div>
                    <div class="resize-handle n" data-dir="n"></div>
                    <div class="resize-handle s" data-dir="s"></div>
                    <div class="resize-handle w" data-dir="w"></div>
                    <div class="resize-handle e" data-dir="e"></div>
                    <div class="resize-handle nw" data-dir="nw"></div>
                    <div class="resize-handle ne" data-dir="ne"></div>
                    <div class="resize-handle sw" data-dir="sw"></div>
                    <div class="resize-handle se" data-dir="se"></div>
                `;

                container.appendChild(win);
                chatLog('Window appended to container');

                // 绑定按钮事件（避免内联onclick）
                const minimizeBtn = win.querySelector('.chat-window-btn.minimize');
                const closeBtn = win.querySelector('.chat-window-btn.close');
                minimizeBtn.addEventListener('click', () => this.minimize(friendId));
                closeBtn.addEventListener('click', () => this.closeWindow(friendId));

                // 加载聊天页面
                const chatUrl = '<%= contextPath %>/chat/page?friendId=' + encodeURIComponent(friendId) + '&nickname=' + encodeURIComponent(nickname);
                chatLog('Loading iframe URL:', chatUrl);
                win.querySelector('iframe').src = chatUrl;

                // 初始化功能
                this.initDrag(win);
                this.initResize(win);
                this.initFocus(win);

                this.windows[friendId] = win;

                // 验证窗口是否正确显示
                setTimeout(() => {
                    const rect = win.getBoundingClientRect();
                    chatLog('Window dimensions after creation:', rect.width, 'x', rect.height);
                    chatLog('Window display style:', getComputedStyle(win).display);
                    if (rect.width === 0 || rect.height === 0) {
                        console.error('[ChatWindow] ERROR: Window has zero size! Check CSS.');
                    }
                }, 100);

                return win;
            },

            // 初始化拖动
            initDrag(win) {
                const header = win.querySelector('.chat-window-header');
                const mask = win.querySelector('.chat-window-drag-mask');
                let isDragging = false;
                let startX, startY, startLeft, startTop;

                header.addEventListener('mousedown', (e) => {
                    if (e.target.closest('.chat-window-btn')) return;

                    isDragging = true;
                    startX = e.clientX;
                    startY = e.clientY;

                    const rect = win.getBoundingClientRect();
                    startLeft = rect.left;
                    startTop = rect.top;

                    // 清除居中transform，转为固定像素定位
                    win.style.transform = 'none';
                    win.style.left = startLeft + 'px';
                    win.style.top = startTop + 'px';

                    win.classList.add('dragging');
                    mask.classList.add('active');

                    e.preventDefault();
                });

                const onMouseMove = (e) => {
                    if (!isDragging) return;

                    const dx = e.clientX - startX;
                    const dy = e.clientY - startY;

                    let newLeft = startLeft + dx;
                    let newTop = startTop + dy;

                    // 边界限制
                    const maxLeft = window.innerWidth - 100;
                    const maxTop = window.innerHeight - 40;
                    newLeft = Math.max(-win.offsetWidth + 100, Math.min(newLeft, maxLeft));
                    newTop = Math.max(0, Math.min(newTop, maxTop));

                    win.style.left = newLeft + 'px';
                    win.style.top = newTop + 'px';
                };

                const onMouseUp = () => {
                    if (isDragging) {
                        isDragging = false;
                        win.classList.remove('dragging');
                        mask.classList.remove('active');
                    }
                };

                document.addEventListener('mousemove', onMouseMove);
                document.addEventListener('mouseup', onMouseUp);
            },

            // 初始化调整大小
            initResize(win) {
                const handles = win.querySelectorAll('.resize-handle');
                const mask = win.querySelector('.chat-window-drag-mask');
                let isResizing = false;
                let currentDir, startX, startY, startWidth, startHeight, startLeft, startTop;

                const minWidth = 320;
                const minHeight = 240;

                handles.forEach(handle => {
                    handle.addEventListener('mousedown', (e) => {
                        isResizing = true;
                        currentDir = handle.dataset.dir;
                        startX = e.clientX;
                        startY = e.clientY;

                        const rect = win.getBoundingClientRect();
                        startWidth = rect.width;
                        startHeight = rect.height;
                        startLeft = rect.left;
                        startTop = rect.top;

                        win.classList.add('resizing');
                        mask.classList.add('active');

                        e.preventDefault();
                        e.stopPropagation();
                    });
                });

                const onMouseMove = (e) => {
                    if (!isResizing) return;

                    const dx = e.clientX - startX;
                    const dy = e.clientY - startY;

                    let newWidth = startWidth;
                    let newHeight = startHeight;
                    let newLeft = startLeft;
                    let newTop = startTop;

                    if (currentDir.includes('e')) {
                        newWidth = Math.max(minWidth, startWidth + dx);
                    }
                    if (currentDir.includes('w')) {
                        newWidth = Math.max(minWidth, startWidth - dx);
                        newLeft = startLeft + (startWidth - newWidth);
                    }
                    if (currentDir.includes('s')) {
                        newHeight = Math.max(minHeight, startHeight + dy);
                    }
                    if (currentDir.includes('n')) {
                        newHeight = Math.max(minHeight, startHeight - dy);
                        newTop = startTop + (startHeight - newHeight);
                    }

                    win.style.width = newWidth + 'px';
                    win.style.height = newHeight + 'px';
                    win.style.left = newLeft + 'px';
                    win.style.top = newTop + 'px';
                };

                const onMouseUp = () => {
                    if (isResizing) {
                        isResizing = false;
                        win.classList.remove('resizing');
                        mask.classList.remove('active');
                    }
                };

                document.addEventListener('mousemove', onMouseMove);
                document.addEventListener('mouseup', onMouseUp);
            },

            initFocus(win) {
                win.addEventListener('mousedown', () => {
                    this.bringToFront(win.dataset.friendId);
                    // 标记为聚焦状态
                    Object.values(this.windows).forEach(w => w.classList.remove('focused'));
                    win.classList.add('focused');
                });
            },

            bringToFront(friendId) {
                const win = this.windows[friendId];
                if (!win) return;
                win.style.zIndex = ++this.zIndexBase;
            },

            minimize(friendId) {
                const win = this.windows[friendId];
                if (win) {
                    win.style.display = 'none';
                    win.classList.remove('active');
                }
            },

            restore(friendId) {
                const win = this.windows[friendId];
                if (win) {
                    win.style.display = 'flex';
                    win.classList.add('active');
                    this.bringToFront(friendId);
                }
            },

            closeWindow(friendId) {
                chatLog('Closing window:', friendId);
                const win = this.windows[friendId];
                if (win) {
                    win.remove();
                    delete this.windows[friendId];
                }
            },

            closeAll() {
                Object.keys(this.windows).forEach(id => this.closeWindow(id));
            }
        };

        // 全局函数
        function openChatWindow(friendId, nickname) {
            chatLog('openChatWindow called:', friendId, nickname);
            if (!friendId) {
                console.error('friendId is required');
                return;
            }
            ChatWindowManager.createWindow(friendId, nickname);
        }

        function closeGlobalChat(friendId) {
            if (friendId) {
                ChatWindowManager.closeWindow(friendId);
            }
        }

        function escapeHtml(text) {
            if (!text) return '';
            const div = document.createElement('div');
            div.textContent = text;
            return div.innerHTML;
        }
    </script>
</body>
</html>