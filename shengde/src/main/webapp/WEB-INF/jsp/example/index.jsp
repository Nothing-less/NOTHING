<%@ page contentType="text/html; charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<!DOCTYPE html>
<html lang="zh-CN">

<body>
    <!-- 背景粒子 -->
    <div class="particles" id="particles"></div>

    <div class="card">
        <!-- 动态图标 -->
        <div class="icon-wrapper">
            <span class="icon">🐽</span>
        </div>

        <h1>See you~</h1>
        <p class="subtitle">👉已退出👈</p>

        <!-- 时间显示 -->
        <div class="time-box">
            <%-- <div class="time-label" ></div> --%>
            <div id="serverTime">--:--:--</div>
        </div>

        <!-- 操作按钮 -->
        <div class="actions">
            <a href="${pageContext.request.contextPath}/" class="btn btn-primary">
                返回首页
            </a>
            <%-- <a href="${pageContext.request.contextPath}/" class="btn btn-secondary">
                返回首页
            </a> --%>
        </div>

        <div class="footer">
            <span id="statusText">正在同步时间...</span>
        </div>
    </div>

    <script>
        // 生成背景粒子
        (function createParticles() {
            const container = document.getElementById('particles');
            const particleCount = 20;
            
            for (let i = 0; i < particleCount; i++) {
                const particle = document.createElement('div');
                particle.className = 'particle';
                particle.style.left = Math.random() * 100 + '%';
                particle.style.animationDelay = Math.random() * 15 + 's';
                particle.style.animationDuration = (15 + Math.random() * 10) + 's';
                container.appendChild(particle);
            }
        })();

        // 时间同步逻辑
        (function() {
            const el = document.getElementById('serverTime');
            const statusText = document.getElementById('statusText');
            let offset = 0;
            let isSynced = false;

            // 立即显示本地时间
            showLocalTime();

            function formatTime(date) {
                return date.toLocaleString('zh-CN', {
                    year: 'numeric',
                    month: '2-digit',
                    day: '2-digit',
                    hour: '2-digit',
                    minute: '2-digit',
                    second: '2-digit',
                    hour12: false
                }).replace(/\//g, '-');
            }

            function showLocalTime() {
                if (!isSynced) {
                    el.textContent = formatTime(new Date());
                    el.className = 'offline';
                    statusText.textContent = '(本地)';
                }
            }

            // 每秒更新
            setInterval(() => {
                if (isSynced) {
                    const now = new Date(Date.now() + offset);
                    el.textContent = formatTime(now);
                } else {
                    showLocalTime();
                }
            }, 1000);

            // 同步服务器时间
            function syncTime() {
                fetch('<%= request.getContextPath() %>/api/time')
                    .then(r => {
                        if (!r.ok) throw new Error('HTTP ' + r.status);
                        return r.json();
                    })
                    .then(data => {
                        offset = data.timestamp - Date.now();
                        isSynced = true;
                        el.textContent = data.datetime;
                        el.className = 'synced';
                        statusText.textContent = '';
                    })
                    .catch(err => {
                        console.log('时间同步失败:', err);
                        // 保持离线模式，30秒后重试
                        setTimeout(syncTime, 30000);
                    });
            }

            // 立即同步，然后每30秒同步一次
            syncTime();
            setInterval(syncTime, 30000);
        })();
    </script>
</body>

<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Goodbye~</title>
    <style>
        * {
            margin: 0;
            padding: 0;
            box-sizing: border-box;
        }

        body {
            font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', 'PingFang SC', 'Hiragino Sans GB', 'Microsoft YaHei', sans-serif;
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
            min-height: 100vh;
            display: flex;
            justify-content: center;
            align-items: center;
            overflow: hidden;
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
        }

        .particle {
            position: absolute;
            width: 10px;
            height: 10px;
            background: rgba(255, 255, 255, 0.1);
            border-radius: 50%;
            animation: float 15s infinite;
        }

        @keyframes float {
            0%, 100% {
                transform: translateY(100vh) rotate(0deg);
                opacity: 0;
            }
            10% {
                opacity: 1;
            }
            90% {
                opacity: 1;
            }
            100% {
                transform: translateY(-100vh) rotate(720deg);
                opacity: 0;
            }
        }

        /* 主卡片 */
        .card {
            background: rgba(255, 255, 255, 0.95);
            backdrop-filter: blur(10px);
            border-radius: 24px;
            padding: 60px 50px;
            text-align: center;
            box-shadow: 0 25px 50px -12px rgba(0, 0, 0, 0.25);
            max-width: 420px;
            width: 90%;
            position: relative;
            z-index: 10;
            animation: slideUp 0.6s ease-out;
        }

        @keyframes slideUp {
            from {
                opacity: 0;
                transform: translateY(30px);
            }
            to {
                opacity: 1;
                transform: translateY(0);
            }
        }

        /* 图标动画 */
        .icon-wrapper {
            width: 80px;
            height: 80px;
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
            border-radius: 50%;
            margin: 0 auto 24px;
            display: flex;
            align-items: center;
            justify-content: center;
            animation: pulse 2s infinite;
        }

        @keyframes pulse {
            0%, 100% {
                box-shadow: 0 0 0 0 rgba(102, 126, 234, 0.4);
            }
            50% {
                box-shadow: 0 0 0 20px rgba(102, 126, 234, 0);
            }
        }

        .icon {
            font-size: 40px;
            color: white;
        }

        /* 文字样式 */
        h1 {
            color: #1a1a2e;
            font-size: 28px;
            font-weight: 600;
            margin-bottom: 12px;
            letter-spacing: -0.5px;
        }

        .subtitle {
            color: #64748b;
            font-size: 16px;
            margin-bottom: 32px;
            line-height: 1.5;
        }

        /* 时间显示 */
        .time-box {
            background: linear-gradient(135deg, #f8fafc 0%, #e2e8f0 100%);
            border-radius: 16px;
            padding: 20px;
            margin-bottom: 32px;
            border: 1px solid #e2e8f0;
        }

        .time-label {
            font-size: 12px;
            color: #94a3b8;
            text-transform: uppercase;
            letter-spacing: 1px;
            margin-bottom: 8px;
        }

        #serverTime {
            font-size: 24px;
            font-weight: 600;
            color: #334155;
            font-variant-numeric: tabular-nums;
            letter-spacing: 1px;
        }

        #serverTime.synced {
            color: #667eea;
        }

        #serverTime.offline {
            color: #94a3b8;
            font-size: 20px;
        }

        /* 按钮组 */
        .actions {
            display: flex;
            gap: 12px;
            flex-direction: column;
        }

        .btn {
            padding: 14px 28px;
            border-radius: 12px;
            font-size: 15px;
            font-weight: 500;
            cursor: pointer;
            transition: all 0.3s ease;
            text-decoration: none;
            display: inline-block;
            border: none;
        }

        .btn-primary {
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
            color: white;
            box-shadow: 0 4px 14px 0 rgba(102, 126, 234, 0.39);
        }

        .btn-primary:hover {
            transform: translateY(-2px);
            box-shadow: 0 6px 20px 0 rgba(102, 126, 234, 0.5);
        }

        .btn-secondary {
            background: transparent;
            color: #64748b;
            border: 1px solid #e2e8f0;
        }

        .btn-secondary:hover {
            background: #f8fafc;
            color: #334155;
        }

        /* 底部信息 */
        .footer {
            margin-top: 24px;
            font-size: 13px;
            color: #94a3b8;
        }

        /* 响应式 */
        @media (max-width: 480px) {
            .card {
                padding: 40px 30px;
            }
            
            h1 {
                font-size: 24px;
            }
        }
    </style>
</head>
</html>