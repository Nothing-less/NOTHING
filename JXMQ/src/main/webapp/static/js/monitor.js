(function () {
    'use strict';

    // 配置常量
    const CONFIG = {
        ENDPOINT: '/api/log/collect',
        SESSION_ID: `MONITOR_${Date.now()}_${Math.random().toString(36).substr(2, 9)}`,
        DEBOUNCE_MS: 500,
        MAX_INPUT_LENGTH: 100
    };

    // 工具函数库
    const Utils = {
        /**
         * 获取元素选择器路径（优化版）
         */
        getSelectorPath(element) {
            if (!element || element === document.body) return 'body';
            if (!(element instanceof Element)) return '';

            const path = [];
            let current = element;

            while (current && current.nodeType === Node.ELEMENT_NODE) {
                let selector = current.nodeName.toLowerCase();

                if (current.id) {
                    selector += `#${current.id}`;
                    path.unshift(selector);
                    break;
                } else {
                    // 使用更稳定的 nth-child 计算
                    const siblings = Array.from(current.parentNode?.children || []);
                    const sameTagSiblings = siblings.filter(s => s.nodeName.toLowerCase() === selector);
                    if (sameTagSiblings.length > 1) {
                        const index = siblings.indexOf(current) + 1;
                        selector += `:nth-child(${index})`;
                    }
                }

                // 添加类名提示（如果有）
                if (current.className && typeof current.className === 'string') {
                    const classes = current.className.split(' ').filter(c => c).slice(0, 2);
                    if (classes.length) selector += `.${classes.join('.')}`;
                }

                path.unshift(selector);
                current = current.parentNode;
            }

            return path.join(' > ');
        },

        /**
         * 判断是否为敏感输入类型（对应 Java 的 isSensitiveInput）
         */
        isSensitiveInput(inputType) {
            if (!inputType) return false;
            const sensitiveTypes = ['password', 'credit-card', 'cvv', 'ssn', 'secret'];
            return sensitiveTypes.some(type => inputType.toLowerCase().includes(type)) ||
                /pwd|pass|secret|token|key/i.test(inputType);
        },

        /**
         * 模拟加密（对应 Java 的 InputSnapshot.mask）
         */
        mockEncrypt(value) {
            if (!value) return '';
            // 简单的哈希模拟，实际项目应调用加密服务
            let hash = 0;
            for (let i = 0; i < value.length; i++) {
                const char = value.charCodeAt(i);
                hash = ((hash << 5) - hash) + char;
                hash = hash & hash;
            }
            return `ENC:${Math.abs(hash).toString(16)}`;
        },

        /**
         * 创建 InputSnapshot（对应 Java 工厂方法）
         */
        createInputSnapshot(value, inputType) {
            const isSensitive = this.isSensitiveInput(inputType);

            if (!value) {
                return {
                    displayValue: '',
                    encryptedValue: null,
                    isMasked: false
                };
            }

            // 长度截断
            const truncated = value.length > CONFIG.MAX_INPUT_LENGTH
                ? value.substr(0, CONFIG.MAX_INPUT_LENGTH) + '...'
                : value;

            if (isSensitive) {
                return {
                    displayValue: '*'.repeat(Math.min(truncated.length, 6)),
                    encryptedValue: this.mockEncrypt(value), // 加密原始值用于审计
                    isMasked: true
                };
            }

            return {
                displayValue: truncated,
                encryptedValue: null,
                isMasked: false
            };
        },

        /**
         * 获取性能指标
         */
        getPerformanceMetrics() {
            const nav = performance.getEntriesByType('navigation')[0];
            return {
                stayTimeMillis: nav ? Math.round(nav.loadEventEnd - nav.startTime) : null,
                responseTimeMillis: nav ? Math.round(nav.responseEnd - nav.requestStart) : null
            };
        },

        /**
         * 获取当前时间戳（ISO-8601，对应 Java Instant）
         */
        now() {
            return new Date().toISOString();
        }
    };

    /**
     * ActionBuilder - 对应 Java 的 Builder 模式
     * 构建符合 UserAction Record 结构的对象
     */
    class ActionBuilder {
        constructor() {
            // 必填字段默认值
            this.audit = {
                sessionId: CONFIG.SESSION_ID,
                timestamp: Utils.now()
            };
            this.type = null;

            // 可选字段初始化为 null（对应 Java Optional.empty）
            this.location = null;
            this.target = null;
            this.input = null;
            this.client = null;
            this.principal = null;
            this.metrics = null;
        }

        // 链式调用方法
        type(actionType) {
            this.type = actionType;
            return this;
        }

        location(url = window.location.href, path = window.location.pathname) {
            this.location = { url, path };
            return this;
        }

        target(element) {
            if (!element) return this;

            const tag = element.tagName || 'UNKNOWN';
            const inputType = element.type || '';

            this.target = {
                tag: tag,
                id: element.id || null,
                name: element.name || null,
                cssClass: element.className || null,
                inputType: inputType || null,
                cssSelector: Utils.getSelectorPath(element)
            };

            // 如果是输入元素，自动创建 InputSnapshot
            if (['INPUT', 'TEXTAREA', 'SELECT'].includes(tag)) {
                this.input = Utils.createInputSnapshot(element.value, inputType);
            }

            return this;
        }

        // 专门用于输入事件的构建（因为 input 事件时 value 在事件对象中）
        inputValue(value, inputType) {
            this.input = Utils.createInputSnapshot(value, inputType);
            return this;
        }

        client() {
            this.client = {
                userAgent: navigator.userAgent,
                ipAddress: null, // 后端从请求头获取
                screenResolution: `${window.screen.width}x${window.screen.height}`,
                referrer: document.referrer || null
            };
            return this;
        }

        principal(userId, username) {
            if (userId != null) {
                this.principal = { id: userId, username: username || null };
            }
            return this;
        }

        metrics(stayTime, responseTime) {
            this.metrics = {
                stayTimeMillis: stayTime || null,
                responseTimeMillis: responseTime || null
            };
            return this;
        }

        /**
         * 构建最终对象，过滤掉所有 null 值（模拟 Optional.empty 的序列化）
         */
        build() {
            if (!this.type) throw new Error('ActionType is required');

            const action = {
                audit: this.audit,
                type: this.type,
                location: this.location || {
                    url: window.location.href,
                    path: window.location.pathname
                },
                client: this.client || {
                    userAgent: navigator.userAgent,
                    ipAddress: null,
                    screenResolution: `${window.screen.width}x${window.screen.height}`,
                    referrer: document.referrer || null
                }
            };

            // 仅添加非 null 的可选字段（与 Java Optional 语义一致）
            if (this.target) action.target = this.target;
            if (this.input) action.input = this.input;
            if (this.principal) action.principal = this.principal;
            if (this.metrics) action.metrics = this.metrics;

            return action;
        }
    }

    // 发送逻辑（保持不变，但增加格式验证）
    function sendLog(actionBuilder) {
        try {
            const data = actionBuilder instanceof ActionBuilder ? actionBuilder.build() : actionBuilder;

            // 开发环境可取消注释查看发送的数据结构
            console.debug('[Monitor] Sending:', JSON.stringify(data, null, 2));

            const payload = JSON.stringify(data);

            if (navigator.sendBeacon) {
                navigator.sendBeacon(CONFIG.ENDPOINT, new Blob([payload], { type: 'application/json' }));
            } else {
                fetch(CONFIG.ENDPOINT, {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/json' },
                    body: payload,
                    keepalive: true
                }).catch(() => { });
            }
        } catch (e) {
            console.error('[Monitor] Failed to send log:', e);
        }
    }

    // ==================== 事件监听 ====================

    // 1. 点击事件
    document.addEventListener('click', (e) => {
        const builder = new ActionBuilder()
            .type('CLICK')
            .target(e.target)
            .client();

        // 尝试获取用户信息（假设存储在 window.__USER__ 或类似位置）
        if (window.__USER__) {
            builder.principal(window.__USER__.id, window.__USER__.name);
        }

        sendLog(builder);
    }, true);

    // 2. 输入事件（防抖）
    let inputTimer = null;
    document.addEventListener('input', (e) => {
        const target = e.target;
        if (!['INPUT', 'TEXTAREA', 'SELECT'].includes(target.tagName)) return;

        clearTimeout(inputTimer);
        inputTimer = setTimeout(() => {
            const builder = new ActionBuilder()
                .type('INPUT')
                .target(target) // 这会设置 target，但 input 需要重新设置
                .client();

            // 重新设置 input（因为 target() 用的是 element.value，而 input 事件需要最新值）
            builder.inputValue(target.value, target.type);

            sendLog(builder);
        }, CONFIG.DEBOUNCE_MS);
    }, true);

    // 3. 滚动事件（节流）
    let scrollTimer = null;
    let scrollCount = 0;
    window.addEventListener('scroll', () => {
        if (scrollTimer) return;
        scrollTimer = setTimeout(() => {
            scrollCount++;
            // 每 10 次滚动或首次滚动上报一次，避免过多数据
            if (scrollCount % 10 === 1) {
                sendLog(
                    new ActionBuilder()
                        .type('SCROLL')
                        .client()
                        .metrics(null, null) // 滚动通常不需要性能指标
                );
            }
            scrollTimer = null;
        }, 1000);
    }, { passive: true });

    // 4. 表单提交
    document.addEventListener('submit', (e) => {
        const form = e.target;
        sendLog(
            new ActionBuilder()
                .type('SUBMIT')
                .target(form)
                .client()
        );
    }, true);

    // 5. 页面进入（使用更精确的 Performance API）
    window.addEventListener('load', () => {
        setTimeout(() => {
            const perf = Utils.getPerformanceMetrics();
            sendLog(
                new ActionBuilder()
                    .type('PAGE_ENTER')
                    .client()
                    .metrics(perf.stayTimeMillis, perf.responseTimeMillis)
                    .principal(window.__USER__?.id, window.__USER__?.name)
            );
        }, 0); // 确保在 load 事件完成后计算性能指标
    });

    // 6. 页面离开（计算停留时间）
    let pageEnterTime = Date.now();
    window.addEventListener('beforeunload', () => {
        const stayTime = Date.now() - pageEnterTime;
        sendLog(
            new ActionBuilder()
                .type('PAGE_LEAVE')
                .client()
                .metrics(stayTime, null)
        );
    });

    // ==================== 公共 API ====================
    // 暴露全局接口供业务代码手动上报（如 API 调用）
    window.Monitor = {
        /**
         * 手动上报 API 调用等行为
         * @param {string} actionType - 行为类型
         * @param {Object} metadata - 额外元数据（会合并到 target 中）
         */
        track(actionType, metadata = {}) {
            const builder = new ActionBuilder()
                .type(actionType)
                .client();

            if (metadata.target) {
                builder.target(metadata.target);
            }
            if (metadata.input) {
                builder.inputValue(metadata.input.value, metadata.input.type);
            }

            sendLog(builder);
        },

        /**
         * 设置用户信息（用于未登录时更新身份）
         */
        setUser(id, username) {
            window.__USER__ = { id, name: username };
        }
    };

})();