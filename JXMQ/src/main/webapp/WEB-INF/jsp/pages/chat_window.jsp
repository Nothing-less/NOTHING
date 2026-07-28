<!-- chat_window.jsp - 聊天窗口 -->
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<link rel="stylesheet" href="<c:url value='/static/css/chat_file_revoke.css' />">
<script src="<c:url value='/static/js/chat_file_revoke.js' />"></script>

<link rel="stylesheet" href="<c:url value='/static/css/pages.css' />">
<div class="chat-container" id="chatContainer" style="display: flex; height: 100vh; flex-direction: column;">
    <div class="chat-messages" id="messageArea" style="flex: 1; overflow-y: auto; padding: 10px;">
        <!-- 消息记录 -->
    </div>
    <div class="chat-input" style="display: flex; padding: 10px; border-top: 1px solid rgba(99,102,241,0.2);">
        <textarea id="msgInput" placeholder="输入消息..." style="flex: 1; resize: none; padding: 8px; border-radius: 6px; border: 1px solid rgba(99,102,241,0.3); background: #1e1b4b; color: #fff;" 
            onkeydown="if(event.key==='Enter' && !event.shiftKey){event.preventDefault();sendMessage();}"></textarea>
        <button onclick="sendMessage()" style="margin-left: 10px; padding: 8px 20px; background: linear-gradient(135deg, #6366f1, #4f46e5); color: white; border: none; border-radius: 6px; cursor: pointer;">发送</button>
    </div>
</div>

<script>
    var currentFriendId = String('${friendId}');
    var currentFriendNickname = String('${nickname}');
    var lastMsgId = null;
    var pendingMessages = {};

    if (currentFriendId && currentFriendId !== 'null' && currentFriendId !== 'undefined') {
        loadHistory();
        markAsRead();
    } else {
        console.error('[ChatWindow] friendId is empty or invalid!');
    }

    var chatClient = window.parent.chatClient || window.top.chatClient;

    if (chatClient) {
        chatClient.on('revoke', function (msg) {
            // console.log('[ChatWindow] 收到撤回通知', msg);

            var msgId = String(msg.msgId);
            var friendId = String(msg.friendId);

            if (friendId === String(currentFriendId)) {
                var area = document.getElementById('messageArea');
                area.innerHTML = '';
                lastMsgId = null;
                loadHistory();
            }
        });

        chatClient.on('message', function (msg) {
            var realMsg = msg.message || msg;
            if (realMsg && String(realMsg.senderId) === String(currentFriendId)) {
                appendMessage(realMsg);
                markAsRead();
            }
        });
    }


    function loadHistory() {
        if (!currentFriendId || currentFriendId === '0') return;
        var url = '${pageContext.request.contextPath}/message/history?friendId=' + currentFriendId;
        if (lastMsgId) {
            url += '&lastMsgId=' + lastMsgId;
        }
        
        fetch(url)
            .then(function(r) { return r.json(); })
            .then(function(res) {
                if (res.code === 200 && res.data && res.data.length > 0) {
                    var area = document.getElementById('messageArea');
                    var messages = res.data.reverse();
                    messages.forEach(function(msg) {
                        appendMessage(msg);
                        if (!lastMsgId || msg.msgId < lastMsgId) lastMsgId = msg.msgId;
                    });
                    area.scrollTop = area.scrollHeight;
                }
            })
            .catch(function(err) { console.error('Load history failed:', err); });
    }

    function appendMessage(msg) {
        var area = document.getElementById('messageArea');

        // ★★★ 文件消息统一由 ChatFileRevoke 处理 ★★★
        if (window.ChatFileRevoke && ChatFileRevoke.isFileMessage(msg)) {
            ChatFileRevoke.renderFileMessage(msg, area);
            return; // 不走后面的文本渲染
        }
        // ===== 原来的文本消息渲染逻辑 =====
        var currentUserId = '${sessionScope.CURRENT_USER_ID}';
        var isSelf = String(msg.senderId) === String(currentUserId);
        
        var div = document.createElement('div');
        div.className = 'msg-item ' + (isSelf ? 'self' : 'other');
        div.style.margin = '8px 0';
        div.style.display = 'flex';
        div.style.flexDirection = 'column';
        div.style.alignItems = isSelf ? 'flex-end' : 'flex-start';
        
        var msgId = msg.msgId || msg.tempId || '';
        var status = msg.status || (isSelf ? 'sent' : '');
        var content = escapeHtml(msg.contents || msg.content || '');
        var timeStr = formatTime(msg.sendTime);
        
        var html = '<div class="msg-bubble" style="max-width: 70%; padding: 10px 14px; border-radius: 12px; ' +
                'background: ' + (isSelf ? 'linear-gradient(135deg, #6366f1, #4f46e5)' : 'rgba(255,255,255,0.1)') +
                '; color: #fff; word-break: break-word;">' +
                content +
                '</div>' +
                '<div class="msg-meta" style="font-size: 11px; color: #888; margin-top: 4px; display: flex; gap: 6px; align-items: center;">' +
                '<span>' + timeStr + '</span>';
        
        if (isSelf) {
            html += '<span class="msg-status" data-msg-id="' + msgId + '">' +
                    (status === 'sending' ? '发送中...' : '已发送') +
                    '</span>';
        }
        
        html += '</div>';
        div.innerHTML = html;
        
        if (msg.tempId) {
            var oldMsg = document.querySelector('[data-temp-id="' + msg.tempId + '"]');
            if (oldMsg) oldMsg.remove();
            div.dataset.tempId = msg.tempId;
        }
        if (msgId) div.dataset.msgId = msgId;
        
        area.appendChild(div);
        area.scrollTop = area.scrollHeight;
    }

    // 撤回成功后，刷新当前聊天记录
    document.addEventListener('file:revoked', function(e) {
        const shareId = e.detail.shareId;
        loadMessages(currentFriendId);
    });

    function escapeHtml(text) {
        if (!text) return '';
        var div = document.createElement('div');
        div.textContent = text;
        return div.innerHTML;
    }

    function sendMessage() {
        
        var input = document.getElementById('msgInput');
        var content = input.value.trim();
        
        if (!content) {
            console.log('[ChatWindow] Content empty, abort');
            return;
        }
        if (!currentFriendId || currentFriendId === '0' || currentFriendId === 'null') {
            alert('错误: 好友ID为空, 请重新打开聊天窗口');
            return;
        }
        
        var tempId = 'temp_' + Date.now();
        
        var optimisticMsg = {
            tempId: tempId,
            senderId: '${sessionScope.CURRENT_USER_ID}',
            receiverId: currentFriendId,
            contents: content,
            sendTime: new Date().toISOString(),
            status: 'sending'
        };
        appendMessage(optimisticMsg);
        input.value = '';
        
        // console.log('[ChatWindow] window.parent:', window.parent);
        // console.log('[ChatWindow] window.parent.chatClient:', window.parent.chatClient);
        
        var parentChat = null;
        
        if (window.parent && window.parent.chatClient) {
            parentChat = window.parent.chatClient;
            // console.log('[ChatWindow] Got chatClient from window.parent');
        } else if (window.top && window.top.chatClient) {
            parentChat = window.top.chatClient;
            // console.log('[ChatWindow] Got chatClient from window.top');
        }
        
        if (parentChat) {
            // console.log('[ChatWindow] WebSocket state:', parentChat.ws ? parentChat.ws.readyState : 'no ws');
            
            var wsOpen = parentChat.ws && parentChat.ws.readyState === WebSocket.OPEN;
            
            if (wsOpen) {
                // console.log('[ChatWindow] Sending via WebSocket');
                var sent = parentChat.sendChat(currentFriendId, content);
                // console.log('[ChatWindow] sendChat returned:', sent);
                pendingMessages[tempId] = { content: content, time: Date.now() };
            } else {
                console.log('[ChatWindow] WebSocket not open, falling back to HTTP');
                sendByHttp(content, tempId);
            }
        } else {
            console.log('[ChatWindow] No chatClient found, falling back to HTTP');
            sendByHttp(content, tempId);
        }
    }
    
    function sendByHttp(content, tempId) {
        // console.log('[ChatWindow] Sending via HTTP');
        var params = new URLSearchParams();
        params.append('receiverId', currentFriendId);
        params.append('content', content);
        params.append('msgType', 1);
        
        fetch('${pageContext.request.contextPath}/message/send', {
            method: 'POST',
            body: params
        })
        .then(function(r) { return r.json(); })
        .then(function(res) {
            // console.log('[ChatWindow] HTTP response:', res);
            if (res.code === 200 && res.data) {
                var oldMsg = document.querySelector('[data-temp-id="' + tempId + '"]');
                if (oldMsg) {
                    oldMsg.dataset.msgId = res.data.msgId;
                    var statusEl = oldMsg.querySelector('.msg-status');
                    if (statusEl) statusEl.textContent = '已发送';
                }
            } else {
                updateMessageStatus(tempId, 'failed');
            }
        })
        .catch(function(err) {
            // console.error('[ChatWindow] HTTP send failed:', err);
            updateMessageStatus(tempId, 'failed');
        });
    }

    function updateMessageStatus(tempId, status) {
        var msg = document.querySelector('[data-temp-id="' + tempId + '"]');
        if (msg) {
            var statusEl = msg.querySelector('.msg-status');
            if (statusEl) {
                statusEl.textContent = status === 'failed' ? '发送失败' : '已发送';
                statusEl.style.color = status === 'failed' ? '#ef4444' : '#4ade80';
            }
        }
        delete pendingMessages[tempId];
    }

    function markAsRead() {
        window.parent.postMessage({
            type: 'MARK_READ',
            friendId: currentFriendId
        }, '*');
    }

    window.addEventListener('message', function(event) {
        var data = event.data;
        if (!data) return;

        var msg = data.message;
        var realMsg = msg && msg.message ? msg.message : msg;

        switch (data.type) {
            case 'CHAT_MESSAGE':
                if (realMsg && String(realMsg.senderId) === String(currentFriendId)) {
                    appendMessage(realMsg);
                    markAsRead();
                }
                break;

            case 'FILE_SHARE':  // 文件消息
                if (realMsg && String(realMsg.senderId) === String(currentFriendId)) {
                    appendMessage(realMsg);
                    markAsRead();
                }
                break;
            case 'SENT_ACK':
                updateMessageStatusByMsgId(data.messageId, 'sent');
                break;

            case 'READ_RECEIPT':
                updateMessageStatusByMsgId(data.messageId, 'read');
                break;
        }
    });

    function formatTime(time) {
        if (!time) return '';
        var d = new Date(time);
        if (isNaN(d.getTime())) return time;
        return d.toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' });
    }

    function closeChatWindow() {
        if (window.parent && window.parent.closeGlobalChat) {
            window.parent.closeGlobalChat(currentFriendId);
        }
    }
</script>