class ChatClient {
    constructor(userId) {
        this.userId = userId;
        this.ws = null;
        this.heartbeatTimer = null;
        this.reconnectAttempts = 0;
        this.maxReconnectAttempts = 5;
        this.listeners = new Map();
        
        this.connect();
    }
    
    connect() {
        const wsUrl = `ws://${location.host}/ws/chat/${this.userId}`;
        this.ws = new WebSocket(wsUrl);
        
        this.ws.onopen = () => {
            console.log('WebSocket 连接成功');
            this.reconnectAttempts = 0;
            this.startHeartbeat();
            this.emit('connected', { userId: this.userId });
        };
        
        this.ws.onmessage = (event) => {
            const msg = JSON.parse(event.data);
            this.handleMessage(msg);
        };
        
        this.ws.onclose = (event) => {
            console.log('WebSocket 关闭:', event.code, event.reason);
            this.stopHeartbeat();
            this.attemptReconnect();
        };
        
        this.ws.onerror = (error) => {
            console.error('WebSocket 错误:', error);
        };
    }
    
    // 启动心跳（每 25 秒发送一次）
    startHeartbeat() {
        this.heartbeatTimer = setInterval(() => {
            this.send({
                type: 'HEARTBEAT',
                timestamp: Date.now()
            });
        }, 25000);
    }
    
    stopHeartbeat() {
        if (this.heartbeatTimer) {
            clearInterval(this.heartbeatTimer);
            this.heartbeatTimer = null;
        }
    }
    
    // 自动重连（指数退避）
    attemptReconnect() {
        if (this.reconnectAttempts >= this.maxReconnectAttempts) {
            console.error('重连次数耗尽');
            this.emit('disconnected', { permanent: true });
            return;
        }
        
        const delay = Math.min(1000 * Math.pow(2, this.reconnectAttempts), 30000);
        this.reconnectAttempts++;
        
        console.log(`${delay}ms 后尝试第 ${this.reconnectAttempts} 次重连`);
        
        setTimeout(() => this.connect(), delay);
    }
    
    // 发送消息
    send(message) {
        if (this.ws && this.ws.readyState === WebSocket.OPEN) {
            this.ws.send(JSON.stringify(message));
        } else {
            console.warn('WebSocket 未连接，消息待发送:', message);
            // 可加入消息队列，重连后发送
        }
    }
    
    // 发送聊天消息
    sendChat(toUserId, content) {
        this.send({
            type: 'CHAT',
            toUserId: toUserId,
            content: content,
            timestamp: Date.now()
        });
    }
    
    // 发送已读回执
    sendReadReceipt(messageId, fromUserId) {
        this.send({
            type: 'READ_ACK',
            messageId: messageId,
            fromUserId: fromUserId
        });
    }
    
    // 处理收到的消息
    handleMessage(msg) {
        switch (msg.type) {
            case 'HEARTBEAT_ACK':
                // 服务器心跳响应
                break;
                
            case 'CHAT':
                // 收到聊天消息
                this.emit('message', msg.message);
                // 自动发送已读回执
                this.sendReadReceipt(msg.message.id, msg.message.fromUserId);
                break;
                
            case 'FRIEND_APPLY':
                // 收到好友申请
                this.emit('friendApply', msg);
                break;
                
            case 'SENT_ACK':
                // 发送成功回执
                this.emit('sent', msg);
                break;
                
            case 'READ_RECEIPT':
                // 对方已读
                this.emit('read', msg);
                break;
                
            case 'ERROR':
                console.error('服务器错误:', msg.message);
                this.emit('error', msg);
                break;
        }
    }
    
    // 事件监听
    on(event, callback) {
        this.listeners.set(event, callback);
    }
    
    emit(event, data) {
        const callback = this.listeners.get(event);
        if (callback) callback(data);
    }
    
    // 关闭连接
    close() {
        this.stopHeartbeat();
        this.ws.close();
    }
}

// 使用示例
const chat = new ChatClient(currentUserId);

chat.on('message', (msg) => {
    console.log('收到消息:', msg);
    displayMessage(msg);
});

chat.on('friendApply', (apply) => {
    showNotification(`收到来自 ${apply.fromUserId} 的好友申请`);
});

chat.on('connected', () => {
    updateOnlineStatus(true);
});

chat.on('disconnected', (e) => {
    updateOnlineStatus(false);
    if (e.permanent) {
        showError('连接已断开，请刷新页面重试');
    }
});