<%@ page contentType="text/html; charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ page import="icu.nothingless.commons.RespEntity" %>
<%@ page import="icu.nothingless.pojo.dto.User" %>
<%@ page import="icu.nothingless.tools.ViewUtil" %>
<%@ page import="java.util.Map" %>
<%@ page import="java.time.LocalDateTime" %>
<%@ page import="java.time.format.DateTimeFormatter" %>
<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>主页</title>
    <link rel="stylesheet" href="<c:url value='/static/css/pages.css' />">
    <link rel="stylesheet" href="<c:url value='/static/css/tables.css' />">
    <script src="<c:url value='/static/js/home.js'       />" defer></script>
    <script src="<c:url value='/static/js/ChatClient.js' />" defer></script>

    <style>
        /* 聊天窗口样式（保持不变） */
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
            left: 50%;
            top: 50%;
            transform: translate(-50%, -50%);
            width: 520px;
            height: 600px;
            color: #fff;
        }
        .chat-window.active { display: flex !important; }
        .chat-window.dragging, .chat-window.resizing {
            box-shadow: 0 12px 48px rgba(0,0,0,0.5), 0 0 0 2px rgba(99,102,241,0.4);
            transition: none; opacity: 0.95;
        }
        .chat-window-header {
            display: flex; align-items: center; justify-content: space-between;
            padding: 0 14px; height: 44px;
            background: linear-gradient(135deg, #6366f1 0%, #4f46e5 100%);
            color: white; cursor: move; user-select: none; flex-shrink: 0;
            border-bottom: 1px solid rgba(255,255,255,0.1);
        }
        .chat-window-title { font-size: 14px; font-weight: 500; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; flex: 1; padding-right: 10px; }
        .chat-window-controls { display: flex; gap: 6px; }
        .chat-window-btn { width: 28px; height: 28px; border: none; background: rgba(255,255,255,0.15); color: white; border-radius: 6px; cursor: pointer; font-size: 16px; line-height: 1; display: flex; align-items: center; justify-content: center; transition: all 0.2s; }
        .chat-window-btn:hover { background: rgba(255,255,255,0.3); transform: scale(1.1); }
        .chat-window-btn.close:hover { background: #ef4444; }
        .chat-window-body { flex: 1; overflow: hidden; position: relative; background: #0f0e2e; }
        .chat-window-body iframe { width: 100%; height: 100%; border: none; display: block; }
        .resize-handle { position: absolute; z-index: 20; }
        .resize-handle.n { top: 0; left: 8px; right: 8px; height: 6px; cursor: n-resize; }
        .resize-handle.s { bottom: 0; left: 8px; right: 8px; height: 6px; cursor: s-resize; }
        .resize-handle.w { left: 0; top: 8px; bottom: 8px; width: 6px; cursor: w-resize; }
        .resize-handle.e { right: 0; top: 8px; bottom: 8px; width: 6px; cursor: e-resize; }
        .resize-handle.nw { top: 0; left: 0; width: 14px; height: 14px; cursor: nw-resize; }
        .resize-handle.ne { top: 0; right: 0; width: 14px; height: 14px; cursor: ne-resize; }
        .resize-handle.sw { bottom: 0; left: 0; width: 14px; height: 14px; cursor: sw-resize; }
        .resize-handle.se { bottom: 0; right: 0; width: 14px; height: 14px; cursor: se-resize; }
        .resize-handle:hover { background: rgba(99, 102, 241, 0.3); }
        .chat-window-drag-mask { position: absolute; top: 0; left: 0; right: 0; bottom: 0; z-index: 9999; display: none; background: transparent; }
        .chat-window-drag-mask.active { display: block; }
        .chat-window.focused { box-shadow: 0 8px 32px rgba(99,102,241,0.3), 0 0 0 1px rgba(99,102,241,0.2); }
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
            <div class="server-time" id="serverTime">···· ·· ·· ··:··:··</div>
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
        // 【修复】确保 contextPath 在全局作用域，供 ChatClient 使用
        window.contextPath = '<%= contextPath %>';
        
        const CHAT_DEBUG = false;
        function chatLog(...args) {
            if (CHAT_DEBUG) console.log('[ChatWindow]', ...args);
        }

        document.addEventListener('DOMContentLoaded', function() {
            chatLog('DOM loaded, initializing chat system...');

        // ========== 服务器时间自动更新 ==========
        /*
        function initServerTime() {
            const timeEl = document.getElementById('serverTime');
            if (!timeEl) return;
            
            // 获取服务器初始时间戳
            const serverTimestamp = parseInt(timeEl.dataset.timestamp, 10);
            if (isNaN(serverTimestamp)) return;
            
            // 记录本地启动时间，用于计算偏移
            const localStart = Date.now();
            
            function updateTime() {
                // 计算当前服务器时间 = 初始时间 + (本地经过的时间)
                const elapsed = Date.now() - localStart;
                const currentServerTime = serverTimestamp + elapsed;
                
                const date = new Date(currentServerTime);
                const formatted = date.toLocaleString('zh-CN', {
                    year: 'numeric',
                    month: '2-digit',
                    day: '2-digit',
                    hour: '2-digit',
                    minute: '2-digit',
                    second: '2-digit',
                    hour12: false
                }).replace(/\//g, '-');
                
                timeEl.textContent = formatted;
            }
            updateTime();
            
            // 每秒更新
            setInterval(updateTime, 1000);
        }
        */

            // 将 chat 实例挂载到 window，供 iframe 访问
            window.chatClient = new ChatClient('<%= currentUser.userId() %>');
            const chat = window.chatClient;
            
            // ========== 连接状态监听 ==========
            chat.on('connected', function(e) {
                // console.log('[Home] WebSocket connected, userId:', e.userId);
            });
            
            chat.on('disconnected', function(e) {
                console.log('[Home] WebSocket disconnected');
                if (e.permanent) {
                    showError('连接已断开，请刷新页面重试');
                }
            });
            
            chat.on('reconnecting', function(e) {
                console.log('[Home] Reconnecting... attempt:', e.attempt);
            });

            // ========== 发送成功确认 ==========
            chat.on('sent', function(msg) {
                chatLog('Message sent ack:', msg);
                // 转发给对应的聊天窗口
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

            // ========== 收到消息，转发给对应聊天窗口 ==========
            chat.on('message', function(msg) {
                chatLog('Received message via WebSocket:', msg);
                
                // msg 结构: {type: "CHAT", message: Message对象}
                const actualMsg = msg.message || msg;
                const senderId = String(actualMsg.senderId);
                
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
                    // 窗口未聚焦时高亮标题栏
                    if (!win.classList.contains('focused')) {
                        win.querySelector('.chat-window-header').style.background = 'linear-gradient(135deg, #ef4444 0%, #dc2626 100%)';
                    }
                } else {
                    // 没有打开聊天窗口，可以在这里更新好友列表未读数
                    chatLog('Message from unopened chat, friendId:', senderId);
                    // 触发未读消息提示（可以调用 friend_list 中的函数）
                    notifyUnreadMessage(senderId, actualMsg);
                }
            });
            
            // 已读回执处理
            chat.on('read', function(msg) {
                chatLog('Read receipt:', msg);
                const win = ChatWindowManager.windows[msg.readBy];
                if (win) {
                    const iframe = win.querySelector('iframe');
                    if (iframe && iframe.contentWindow) {
                        iframe.contentWindow.postMessage({
                            type: 'READ_RECEIPT',
                            messageId: msg.messageId
                        }, '*');
                    }
                }
            });

            // ========== 监听来自 iframe 的消息 ==========
            window.addEventListener('message', function(event) {

                const data = event.data;
                if (!data) return;
                
                chatLog('Received message from iframe:', data);
                
                switch (data.type) {
                    case 'OPEN_CHAT':
                        chatLog('Opening chat window for:', data.friendId, data.nickname);
                        openChatWindow(data.friendId, data.nickname);
                        break;
                        
                     case 'REFRESH_FRIEND_LIST':
                        // 通知 contentFrame 刷新好友列表
                        const contentFrame = document.getElementById('contentFrame');
                        if (contentFrame && contentFrame.contentWindow && contentFrame.contentWindow.loadFriends) {
                            contentFrame.contentWindow.loadFriends();
                        }
                        break;
                    case 'SEND_MESSAGE':
                        // chat_window 请求发送消息
                        if (data.toUserId && data.content) {
                            chat.sendChat(data.toUserId, data.content);
                        }
                        break;
                        
                    case 'MARK_READ':
                        // 标记已读
                        if (data.friendId) {
                            fetch('<%= contextPath %>/message/read?friendId=' + data.friendId, {method: 'POST'});
                        }
                        break;
                }
            });

            chatLog('Chat system initialized');
        });

        window.CURRENT_USER = {
            userId: '<%= currentUser.userId() %>',
            nickname: '<%= currentUser.nickname() %>'
        };

        // 未读消息提示（供外部调用）
        function notifyUnreadMessage(friendId, message) {
            // 可以在这里更新侧边栏或标题栏的未读计数
            // 例如：向 friend_list iframe 发送消息
            const contentFrame = document.getElementById('contentFrame');
            if (contentFrame && contentFrame.contentWindow) {
                contentFrame.contentWindow.postMessage({
                    type: 'NEW_MESSAGE',
                    friendId: friendId,
                    message: message
                }, '*');
            }
        }

        // ========== 聊天窗口管理器 ==========
        const ChatWindowManager = {
            windows: {},
            zIndexBase: 1000,

            createWindow(friendId, nickname) {
                chatLog('createWindow called:', friendId, nickname);
                
                if (this.windows[friendId]) {
                    chatLog('Window already exists, bringing to front');
                    this.restore(friendId);
                    this.bringToFront(friendId);
                    return this.windows[friendId];
                }

                const container = document.getElementById('chatWindowsContainer');
                const windowId = 'chat-window-' + friendId;

                const win = document.createElement('div');
                win.id = windowId;
                win.className = 'chat-window active';
                win.dataset.friendId = friendId;
                win.style.zIndex = ++this.zIndexBase;

                const safeNickname = escapeHtml(nickname);

                // 【修复】先创建结构，不设置标题文本
                win.innerHTML = `
                    <div class="chat-window-drag-mask" id="${windowId}-mask"></div>
                    <div class="chat-window-header" id="${windowId}-header">
                        <span class="chat-window-title"></span>
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

                // 【修复】用 textContent 设置标题，避免模板字符串问题
                const titleSpan = win.querySelector('.chat-window-title');
                titleSpan.textContent = '💬 ' + safeNickname;

                container.appendChild(win);

                // 绑定按钮事件
                win.querySelector('.chat-window-btn.minimize').addEventListener('click', () => this.minimize(friendId));
                win.querySelector('.chat-window-btn.close').addEventListener('click', () => this.closeWindow(friendId));

                // 加载聊天页面
                const chatUrl = '<%= contextPath %>/chat/page?friendId=' + encodeURIComponent(friendId) + '&nickname=' + encodeURIComponent(nickname);
                chatLog('Loading iframe URL:', chatUrl);
                win.querySelector('iframe').src = chatUrl;

                // 初始化功能
                this.initDrag(win);
                this.initResize(win);
                this.initFocus(win);

                this.windows[friendId] = win;
                return win;
            },

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
                    let newLeft = Math.max(-win.offsetWidth + 100, Math.min(startLeft + dx, window.innerWidth - 100));
                    let newTop = Math.max(0, Math.min(startTop + dy, window.innerHeight - 40));
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

            initResize(win) {
                const handles = win.querySelectorAll('.resize-handle');
                const mask = win.querySelector('.chat-window-drag-mask');
                let isResizing = false;
                let currentDir, startX, startY, startWidth, startHeight, startLeft, startTop;
                const minWidth = 320, minHeight = 240;

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
                    let newWidth = startWidth, newHeight = startHeight;
                    let newLeft = startLeft, newTop = startTop;

                    if (currentDir.includes('e')) newWidth = Math.max(minWidth, startWidth + dx);
                    if (currentDir.includes('w')) {
                        newWidth = Math.max(minWidth, startWidth - dx);
                        newLeft = startLeft + (startWidth - newWidth);
                    }
                    if (currentDir.includes('s')) newHeight = Math.max(minHeight, startHeight + dy);
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
                    Object.values(this.windows).forEach(w => w.classList.remove('focused'));
                    win.classList.add('focused');
                    // 恢复标题栏颜色
                    win.querySelector('.chat-window-header').style.background = '';
                });
            },

            bringToFront(friendId) {
                const win = this.windows[friendId];
                if (win) win.style.zIndex = ++this.zIndexBase;
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

        function openChatWindow(friendId, nickname) {
            chatLog('openChatWindow called:', friendId, nickname);
            if (!friendId) {
                console.error('friendId is required');
                return;
            }
            ChatWindowManager.createWindow(friendId, nickname);
        }

        function closeGlobalChat(friendId) {
            if (friendId) ChatWindowManager.closeWindow(friendId);
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