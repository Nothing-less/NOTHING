<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="friendId" value="${requestScope.friendId}" />
<c:set var="nickname" value="${requestScope.nickname}" />

<link rel="stylesheet" href="<c:url value='/static/css/pages.css' />">
<div class="chat-container" id="chatContainer" style="display: flex; height: 100vh; flex-direction: column;">
    <div class="chat-messages" id="messageArea" style="flex: 1; overflow-y: auto;">
        <!-- 消息记录 -->
    </div>
    <div class="chat-input">
        <textarea id="msgInput" placeholder="输入消息..." onkeydown="if(event.key==='Enter' && !event.shiftKey){event.preventDefault();sendMessage();}"></textarea>
        <button onclick="sendMessage()">发送</button>
    </div>
</div>

<script>
    let currentFriendId = String('${friendId}');
    let currentFriendNickname = String('${nickname}');
    let lastMsgId = null;

    if (currentFriendId) {
        loadHistory();
        fetch('${pageContext.request.contextPath}/message/read?friendId=' + currentFriendId, {method: 'POST'});
    }

    function loadHistory() {
        if (!currentFriendId) return;
        fetch('${pageContext.request.contextPath}/message/history?friendId=' + currentFriendId + '&lastMsgId=' + (lastMsgId || ''))
            .then(r => r.json())
            .then(res => {
                if (res.code === 200 && res.data.length > 0) {
                    const area = document.getElementById('messageArea');
                    res.data.reverse().forEach(msg => {
                        appendMessage(msg);
                        if (!lastMsgId || msg.msgId < lastMsgId) lastMsgId = msg.msgId;
                    });
                    area.scrollTop = area.scrollHeight;
                }
            });
    }

    function appendMessage(msg) {
        const area = document.getElementById('messageArea');
        const currentUserId = '${sessionScope.CURRENT_USER_ID}';
        const isSelf = String(msg.senderId) === String(currentUserId);
        const div = document.createElement('div');
        div.className = 'msg-item ' + (isSelf ? 'self' : 'other');
        div.innerHTML = '<div class="msg-content">' + escapeHtml(msg.contents) + '</div><div class="msg-time">' + formatTime(msg.sendTime) + '</div>';
        area.appendChild(div);
        area.scrollTop = area.scrollHeight;
    }

    function escapeHtml(text) {
        const div = document.createElement('div');
        div.textContent = text;
        return div.innerHTML;
    }

    // ========== 【关键修改】通过 WebSocket 发送 ==========
    function sendMessage() {
        const content = document.getElementById('msgInput').value.trim();
        if (!content || !currentFriendId) return;
        
        // 获取父窗口的 ChatClient 实例
        const parentChat = window.parent.chatClient;
        
        if (parentChat && parentChat.ws && parentChat.ws.readyState === WebSocket.OPEN) {
            // 通过 WebSocket 发送
            parentChat.sendChat(currentFriendId, content);
            
            // 本地乐观显示
            const optimisticMsg = {
                senderId: '${sessionScope.CURRENT_USER_ID}',
                receiverId: currentFriendId,
                contents: content,
                sendTime: new Date().toISOString(),
                isSelf: true
            };
            appendMessage(optimisticMsg);
            document.getElementById('msgInput').value = '';
        } else {
            // WebSocket 未连接，降级为 HTTP
            sendByHttp(content);
        }
    }
    
    function sendByHttp(content) {
        const params = new URLSearchParams();
        params.append('receiverId', currentFriendId);
        params.append('content', content);
        params.append('msgType', 1);
        
        fetch('${pageContext.request.contextPath}/message/send', {
            method: 'POST',
            body: params
        }).then(r => r.json()).then(res => {
            if (res.code === 200) {
                appendMessage(res.data);
                document.getElementById('msgInput').value = '';
            }
        });
    }
    // ===================================================

    // ========== 【关键修改】接收消息改为监听 postMessage ==========
    window.addEventListener('message', function(event) {
        // 安全：检查来源（生产环境建议加 origin 校验）
        // if (event.origin !== window.location.origin) return;
        
        const data = event.data;
        if (data && data.type === 'CHAT_MESSAGE') {
            const msg = data.message;
            // 只处理当前聊天好友的消息
            if (String(msg.senderId) === String(currentFriendId)) {
                appendMessage(msg);
                // 标记已读
                fetch('${pageContext.request.contextPath}/message/read?friendId=' + currentFriendId, {method: 'POST'});
            }
        }
        if (data && data.type === 'SENT_ACK') {
        // 消息发送成功确认，可以移除"发送中"状态
        console.log('Message sent successfully:', data.messageId);
    }
    });
    // ===================================================

    function formatTime(time) {
        if (!time) return '';
        const d = new Date(time);
        if (isNaN(d.getTime())) return time;  // 如果解析失败，原样返回
        return d.toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' });
    }

    function closeChatWindow() {
        if (window.parent && window.parent.closeGlobalChat) {
            window.parent.closeGlobalChat(currentFriendId);
        }
    }
</script>