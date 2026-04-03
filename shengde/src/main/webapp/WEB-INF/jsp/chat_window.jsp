<!-- chat_window.jsp - 聊天窗口 -->
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<div class="chat-container" id="chatContainer" style="display:none;">
    <div class="chat-header">
        <span id="chatTitle">好友昵称</span>
        <button onclick="closeChat()">×</button>
    </div>
    <div class="chat-messages" id="messageArea">
        <!-- 消息记录 -->
    </div>
    <div class="chat-input">
        <textarea id="msgInput" placeholder="输入消息..."></textarea>
        <button onclick="sendMessage()">发送</button>
    </div>
</div>

<script>
let currentFriendId = null;
let lastMsgId = null;

function openChat(friendId, nickname) {
    currentFriendId = friendId;
    document.getElementById('chatContainer').style.display = 'flex';
    document.getElementById('chatTitle').textContent = nickname;
    document.getElementById('messageArea').innerHTML = '';
    lastMsgId = null;
    
    loadHistory();
    // 标记已读
    fetch('${pageContext.request.contextPath}/message/read?friendId=' + friendId, {method: 'POST'});
}

function loadHistory() {
    fetch('${pageContext.request.contextPath}/message/history?friendId=' + currentFriendId + '&lastMsgId=' + (lastMsgId || ''))
        .then(r => r.json())
        .then(res => {
            if (res.code === 0 && res.data.length > 0) {
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
    const isSelf = msg.senderId === ${sessionScope.userId};
    const div = document.createElement('div');
    div.className = 'msg-item ' + (isSelf ? 'self' : 'other');
    div.innerHTML = `
        <div class="msg-content">${msg.content}</div>
        <div class="msg-time">${formatTime(msg.sendTime)}</div>
    `;
    area.appendChild(div);
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
        if (res.code === 0) {
            appendMessage(res.data);
            document.getElementById('msgInput').value = '';
            document.getElementById('messageArea').scrollTop = document.getElementById('messageArea').scrollHeight;
        }
    });
}

// 消息轮询
function startMessagePolling() {
    function poll() {
        fetch('${pageContext.request.contextPath}/chat/poll')
            .then(r => r.json())
            .then(res => {
                if (res.code === 0 && res.data.length > 0) {
                    res.data.forEach(msg => {
                        if (msg.senderId === currentFriendId) {
                            appendMessage(msg);
                            // 标记已读
                            fetch('${pageContext.request.contextPath}/message/read?friendId=' + msg.senderId, {method: 'POST'});
                        } else {
                            // 显示未读提醒
                            showNotification(msg);
                            // 刷新好友列表显示未读数
                            loadFriends();
                        }
                    });
                }
            })
            .finally(() => {
                setTimeout(poll, 1000); // 继续轮询
            });
    }
    poll();
}

function formatTime(time) {
    return new Date(time).toLocaleTimeString();
}

function closeChat() {
    document.getElementById('chatContainer').style.display = 'none';
    currentFriendId = null;
}
</script>