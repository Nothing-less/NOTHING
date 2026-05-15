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
    let currentFriendId = '${friendId}';
    let currentFriendNickname = '${nickname}';
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
        div.innerHTML = '<div class="msg-content">' + escapeHtml(msg.content) + '</div><div class="msg-time">' + formatTime(msg.sendTime) + '</div>';
        area.appendChild(div);
    }

    function escapeHtml(text) {
        const div = document.createElement('div');
        div.textContent = text;
        return div.innerHTML;
    }

    function sendMessage() {
        const content = document.getElementById('msgInput').value.trim();
        if (!content || !currentFriendId) return;
        
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
                document.getElementById('messageArea').scrollTop = document.getElementById('messageArea').scrollHeight;
            }
        });
    }

    function startMessagePolling() {
        function poll() {
            fetch('${pageContext.request.contextPath}/chat/poll')
                .then(r => r.json())
                .then(res => {
                    if (res.code === 200 && res.data.length > 0) {
                        res.data.forEach(msg => {
                            if (String(msg.senderId) === String(currentFriendId)) {
                                appendMessage(msg);
                                fetch('${pageContext.request.contextPath}/message/read?friendId=' + msg.senderId, {method: 'POST'});
                            }
                        });
                    }
                })
                .finally(() => {
                    setTimeout(poll, 3000);
                });
        }
        poll();
    }

    function formatTime(time) {
        return new Date(time).toLocaleTimeString();
    }

    // 关闭时通知父窗口
    function closeChatWindow() {
        if (window.parent && window.parent.closeGlobalChat) {
            window.parent.closeGlobalChat(currentFriendId);
        }
    }

    startMessagePolling();
</script>
