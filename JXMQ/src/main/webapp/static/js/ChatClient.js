class ChatClient {
    constructor(userId) {
        this.userId = String(userId);
        this.ws = null;
        this.heartbeatTimer = null;
        this.reconnectAttempts = 0;
        this.maxReconnectAttempts = 5;
        this.listeners = new Map();
        
        this.connect();
    }
    
    connect() {
        var path = '';
        
        if (typeof window !== 'undefined' && window.contextPath) {
            path = window.contextPath;
        } else if (document.body && document.body.dataset && document.body.dataset.apiBase) {
            path = document.body.dataset.apiBase;
        }
        
        if (path) {
            path = path.replace(/\/+$/, '');
            if (!path.startsWith('/')) {
                path = '/' + path;
            }
        }
        
        var protocol = location.protocol === 'https:' ? 'wss:' : 'ws:';
        var wsUrl = protocol + '//' + location.host + path + '/ws/chat/' + this.userId;
        
        // console.log('[ChatClient] Connecting to:', wsUrl);
        this.ws = new WebSocket(wsUrl);
        
        var self = this;
        
        this.ws.onopen = function() {
            // console.log('[ChatClient] Connected');
            self.reconnectAttempts = 0;
            self.startHeartbeat();
            self.emit('connected', { userId: self.userId });
        };
        
        this.ws.onmessage = function(event) {
            try {
                var msg = JSON.parse(event.data);
                self.handleMessage(msg);
            } catch (e) {
                console.error('[ChatClient] Failed to parse message:', e);
            }
        };
        
        this.ws.onclose = function(event) {
            console.log('[ChatClient] WebSocket closed:', event.code, event.reason);
            self.stopHeartbeat();
            if (!event.wasClean) {
                self.attemptReconnect();
            }
        };
        
        this.ws.onerror = function(error) {
            console.error('[ChatClient] WebSocket error:', error);
            self.emit('error', { type: 'websocket_error', error: error });
        };
    }
    
    startHeartbeat() {
        var self = this;
        this.heartbeatTimer = setInterval(function() {
            if (self.ws && self.ws.readyState === WebSocket.OPEN) {
                self.send({
                    type: 'HEARTBEAT',
                    timestamp: Date.now()
                });
            }
        }, 25000);
    }
    
    stopHeartbeat() {
        if (this.heartbeatTimer) {
            clearInterval(this.heartbeatTimer);
            this.heartbeatTimer = null;
        }
    }
    
    attemptReconnect() {
        if (this.reconnectAttempts >= this.maxReconnectAttempts) {
            console.error('[ChatClient] Max reconnect attempts reached');
            this.emit('disconnected', { permanent: true, reason: 'max_reconnect' });
            return;
        }
        
        var delay = Math.min(1000 * Math.pow(2, this.reconnectAttempts), 30000);
        this.reconnectAttempts++;
        
        console.log('[ChatClient] Reconnecting in ' + delay + 'ms (attempt ' + this.reconnectAttempts + ')');
        this.emit('reconnecting', { attempt: this.reconnectAttempts, delay: delay });
        
        var self = this;
        setTimeout(function() { self.connect(); }, delay);
    }
    
    send(message) {
        if (this.ws && this.ws.readyState === WebSocket.OPEN) {
            this.ws.send(JSON.stringify(message));
            return true;
        } else {
            console.warn('[ChatClient] WebSocket not open, message queued:', message);
            this.emit('pending', message);
            return false;
        }
    }
    
    sendChat(toUserId, content) {
        return this.send({
            type: 'CHAT',
            toUserId: String(toUserId),
            content: content,
            timestamp: Date.now()
        });
    }
    
    sendReadReceipt(messageId, fromUserId) {
        this.send({
            type: 'READ_ACK',
            messageId: String(messageId),
            fromUserId: String(fromUserId)
        });
    }
    
    sendFriendApply(toUserId, applyMsg) {
        this.send({
            type: 'FRIEND_APPLY',
            toUserId: String(toUserId),
            applyMsg: applyMsg,
            timestamp: Date.now()
        });
    }
    
    handleMessage(msg) {
        switch (msg.type) {
            case 'CONNECTED':
                // 连接成功确认，不需要特殊处理
                console.log('[ChatClient] Server confirmed connection');
                break;
                
            case 'HEARTBEAT_ACK':
                this.emit('heartbeat', msg);
                break;
                
            case 'CHAT':
                this.emit('message', msg);
                if (msg.message && msg.message.msgId && msg.message.senderId) {
                    this.sendReadReceipt(msg.message.msgId, msg.message.senderId);
                }
                break;
                
            case 'FRIEND_APPLY':
                this.emit('friendApply', msg);
                break;
                
            case 'SENT_ACK':
                this.emit('sent', msg);
                break;
                
            case 'READ_RECEIPT':
                this.emit('read', msg);
                break;
                
            case 'ERROR':
                console.error('[ChatClient] Server error:', msg.message);
                this.emit('error', msg);
                break;
                
            default:
                console.log('[ChatClient] Unknown message type:', msg.type, msg);
        }
    }
    
    on(event, callback) {
        if (!this.listeners.has(event)) {
            this.listeners.set(event, []);
        }
        this.listeners.get(event).push(callback);
        
        var self = this;
        return function() {
            var callbacks = self.listeners.get(event);
            if (callbacks) {
                var index = callbacks.indexOf(callback);
                if (index > -1) callbacks.splice(index, 1);
            }
        };
    }
    
    emit(event, data) {
        var callbacks = this.listeners.get(event);
        if (callbacks) {
            callbacks.forEach(function(cb) {
                try {
                    cb(data);
                } catch (e) {
                    console.error('[ChatClient] Error in listener:', e);
                }
            });
        }
    }
    
    close() {
        this.stopHeartbeat();
        if (this.ws) {
            this.ws.close(1000, 'Client closed');
        }
    }
}