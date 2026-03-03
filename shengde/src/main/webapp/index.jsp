<%@ page contentType="text/html; charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<!DOCTYPE html>
<html lang="en">
<body>
    <div class="decoration decoration-1"></div>
    <div class="decoration decoration-2"></div>
    
    <div class="login-wrap">
        <div class="login-header">
            <h1>Welcome !</h1>
            <p>Please sign in to continue</p>
        </div>

        <c:if test="${not empty requestScope.error}">
            <div class="error">
                <svg width="20" height="20" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 8v4m0 4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z"></path>
                </svg>
                ${fn:escapeXml(requestScope.error)}
            </div>
        </c:if>
        <c:if test="${not empty param.error}">
            <div class="error">
                <svg width="20" height="20" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 8v4m0 4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z"></path>
                </svg>
                ${fn:escapeXml(param.error)}
            </div>
        </c:if>

        <form action="${pageContext.request.contextPath}/login" method="post" autocomplete="off">
            <c:if test="${not empty _csrf}">
                <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}"/>
            </c:if>

            <div class="form-row">
                <label for="username">Username</label>
                <div class="input-wrapper">
                    <input id="username" name="username" type="text" value="${fn:escapeXml(param.username)}" required autofocus placeholder="Enter your username"/>
                    <svg class="input-icon" width="20" height="20" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M16 7a4 4 0 11-8 0 4 4 0 018 0zM12 14a7 7 0 00-7 7h14a7 7 0 00-7-7z"></path>
                    </svg>
                </div>
            </div>

            <div class="form-row">
                <label for="password">Password</label>
                <div class="input-wrapper">
                    <input id="password" name="password" type="password" required placeholder="Enter your password"/>
                </div>
            </div>

            <div class="actions">
                <button type="submit" class="btn">Sign In</button>
            </div>

            <c:if test="${not empty param.redirect}">
                <input type="hidden" name="redirect" value="${fn:escapeXml(param.redirect)}"/>
            </c:if>
        </form>
        
        <div class="hint">
            Forgot your password? <a href="#">Contact administrator</a>
        </div>
    </div>
</body>
<head>
    <meta charset="UTF-8"/>
    <title>Login</title>
    <meta name="viewport" content="width=device-width,initial-scale=1"/>
    <script src="${pageContext.request.contextPath}/static/js/SHA256Util.js"></script>
<style>
    * {
        margin: 0;
        padding: 0;
        box-sizing: border-box;
    }

    body {
        font-family: 'Segoe UI', system-ui, -apple-system, sans-serif;
        min-height: 100vh;
        display: flex;
        align-items: center;
        justify-content: center;
        background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
        position: relative;
        overflow: hidden;
        /* 创建独立合成层，避免背景影响主文档 */
        isolation: isolate;
    }

    /* 动态背景动画 - 优化：使用will-change，限制动画属性 */
    body::before {
        content: '';
        position: absolute;
        width: 200%;
        height: 200%;
        background: 
            radial-gradient(circle at 20% 80%, rgba(120, 119, 198, 0.3) 0%, transparent 50%),
            radial-gradient(circle at 80% 20%, rgba(255, 119, 198, 0.3) 0%, transparent 50%),
            radial-gradient(circle at 40% 40%, rgba(99, 102, 241, 0.2) 0%, transparent 40%);
        animation: float 20s ease-in-out infinite;
        /* GPU加速提示 */
        will-change: transform;
        /* 强制GPU层 */
        transform: translateZ(0);
        /*  containment减少重绘区域 */
        contain: strict;
    }

    @keyframes float {
        0%, 100% { transform: translate3d(0, 0, 0) rotate(0deg); }
        33% { transform: translate3d(-30px, -30px, 0) rotate(120deg); }
        66% { transform: translate3d(30px, -20px, 0) rotate(240deg); }
    }

    .login-wrap {
        width: 400px;
        padding: 40px;
        background: rgba(255, 255, 255, 0.95);
        backdrop-filter: blur(20px);
        border-radius: 24px;
        border: 1px solid rgba(255, 255, 255, 0.3);
        box-shadow: 
            0 25px 50px -12px rgba(0, 0, 0, 0.25),
            0 0 0 1px rgba(255, 255, 255, 0.1) inset;
        position: relative;
        z-index: 10;
        /* 使用translate3d替代translateY，强制GPU层 */
        transform: translate3d(0, 0, 0);
        transition: transform 0.3s ease, box-shadow 0.3s ease;
        /* 创建独立合成层 */
        will-change: transform;
        /*  containment优化 */
        contain: layout style paint;
    }

    .login-wrap:hover {
        transform: translate3d(0, -5px, 0);
        box-shadow: 
            0 30px 60px -12px rgba(0, 0, 0, 0.3),
            0 0 0 1px rgba(255, 255, 255, 0.2) inset;
    }

    .login-header {
        text-align: center;
        margin-bottom: 32px;
    }

    .login-header h1 {
        font-size: 32px;
        font-weight: 700;
        background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
        -webkit-background-clip: text;
        -webkit-text-fill-color: transparent;
        background-clip: text;
        margin-bottom: 8px;
        letter-spacing: -0.5px;
        /* 防止渐变文字导致的合成层问题 */
        transform: translateZ(0);
    }

    .login-header p {
        color: #6b7280;
        font-size: 14px;
        font-weight: 400;
    }

    .form-row {
        margin-bottom: 20px;
        position: relative;
        /*  containment减少重排 */
        contain: layout;
    }

    label {
        display: block;
        margin-bottom: 8px;
        font-size: 14px;
        font-weight: 500;
        color: #374151;
        /* 缩短transition时间，减少计算 */
        transition: color 0.15s ease;
    }

    .form-row:focus-within label {
        color: #667eea;
    }

    .input-wrapper {
        position: relative;
        display: flex;
        align-items: center;
    }

    input[type="text"], input[type="password"] {
        width: 100%;
        padding: 14px 16px;
        padding-right: 45px;
        font-size: 15px;
        border: 2px solid #e5e7eb;
        border-radius: 12px;
        background: #fafafa;
        color: #1f2937;
        /* 优化：使用更简单的cubic-bezier，减少计算量 */
        transition: border-color 0.2s ease, background-color 0.2s ease, box-shadow 0.2s ease, transform 0.2s ease;
        outline: none;
        /* 防止输入时的重排 */
        will-change: transform;
    }

    input[type="text"]:hover, input[type="password"]:hover {
        border-color: #d1d5db;
        background: #f9fafb;
    }

    input[type="text"]:focus, input[type="password"]:focus {
        border-color: #667eea;
        background: #ffffff;
        box-shadow: 0 0 0 4px rgba(102, 126, 234, 0.1);
        /* 使用translate3d */
        transform: translate3d(0, -1px, 0);
    }

    /* 输入框图标 */
    .input-icon {
        position: absolute;
        right: 16px;
        color: #9ca3af;
        pointer-events: none;
        transition: color 0.15s ease;
        /* 避免图标动画影响布局 */
        will-change: color;
    }

    .form-row:focus-within .input-icon {
        color: #667eea;
    }

    /* 密码可见性切换按钮 */
    .toggle-password {
        position: absolute;
        right: 12px;
        background: none;
        border: none;
        color: #6b7280;
        cursor: pointer;
        padding: 4px 8px;
        font-size: 13px;
        font-weight: 500;
        border-radius: 6px;
        transition: color 0.15s ease, background-color 0.15s ease;
        z-index: 2;
        /* 提升点击响应，不触发布局变化 */
        will-change: color, background-color;
    }

    .toggle-password:hover {
        color: #667eea;
        background: rgba(102, 126, 234, 0.1);
    }

    .actions {
        margin-top: 24px;
    }

    .btn {
        width: 100%;
        padding: 14px 24px;
        font-size: 16px;
        font-weight: 600;
        border: none;
        background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
        color: #fff;
        border-radius: 12px;
        cursor: pointer;
        position: relative;
        overflow: hidden;
        transition: transform 0.3s ease, box-shadow 0.3s ease;
        box-shadow: 0 4px 6px -1px rgba(102, 126, 234, 0.2), 0 2px 4px -1px rgba(102, 126, 234, 0.1);
        /* GPU加速 */
        will-change: transform;
        transform: translate3d(0, 0, 0);
        /*  containment */
        contain: layout style;
    }

    .btn::before {
        content: '';
        position: absolute;
        top: 0;
        left: -100%;
        width: 100%;
        height: 100%;
        background: linear-gradient(90deg, transparent, rgba(255,255,255,0.3), transparent);
        transition: left 0.5s ease;
        /* 独立层 */
        will-change: left;
    }

    .btn:hover {
        transform: translate3d(0, -2px, 0);
        box-shadow: 0 10px 20px -5px rgba(102, 126, 234, 0.4);
    }

    .btn:hover::before {
        left: 100%;
    }

    .btn:active {
        transform: translate3d(0, 0, 0);
    }

    .btn:disabled {
        opacity: 0.6;
        cursor: not-allowed;
        transform: translate3d(0, 0, 0);
    }

    /* 加载动画 - 优化：使用transform替代margin，独立合成层 */
    .btn.loading {
        color: transparent;
    }

    .btn.loading::after {
        content: '';
        position: absolute;
        width: 20px;
        height: 20px;
        top: 50%;
        left: 50%;
        /* 使用transform替代margin，避免布局计算 */
        transform: translate3d(-50%, -50%, 0);
        border: 2px solid #ffffff;
        border-radius: 50%;
        border-top-color: transparent;
        animation: spinner 0.8s linear infinite;
        /* GPU加速 */
        will-change: transform;
    }

    @keyframes spinner {
        to { transform: translate3d(-50%, -50%, 0) rotate(360deg); }
    }

    .error {
        background: linear-gradient(135deg, #fee2e2 0%, #fecaca 100%);
        color: #991b1b;
        padding: 12px 16px;
        border-radius: 10px;
        margin-bottom: 20px;
        font-size: 14px;
        font-weight: 500;
        border-left: 4px solid #ef4444;
        display: flex;
        align-items: center;
        gap: 8px;
        animation: shake 0.5s ease-in-out;
        /* 动画结束后移除will-change */
        will-change: transform;
    }

    @keyframes shake {
        0%, 100% { transform: translate3d(0, 0, 0); }
        25% { transform: translate3d(-5px, 0, 0); }
        75% { transform: translate3d(5px, 0, 0); }
    }

    .hint {
        text-align: center;
        margin-top: 24px;
        font-size: 13px;
        color: #6b7280;
    }

    .hint a {
        color: #667eea;
        text-decoration: none;
        font-weight: 500;
        transition: color 0.15s ease;
    }

    .hint a:hover {
        color: #764ba2;
        text-decoration: underline;
    }

    /* 装饰性元素 - 优化：强制GPU层，使用transform */
    .decoration {
        position: absolute;
        width: 100px;
        height: 100px;
        border-radius: 50%;
        filter: blur(40px);
        opacity: 0.5;
        z-index: 1;
        /* 避免模糊滤镜影响性能，提升为独立层 */
        will-change: transform;
        transform: translate3d(0, 0, 0);
        /*  containment */
        contain: strict;
    }

    .decoration-1 {
        top: -50px;
        right: -50px;
        background: #667eea;
    }

    .decoration-2 {
        bottom: -50px;
        left: -50px;
        background: #764ba2;
    }

    /* 响应式设计 */
    @media (max-width: 480px) {
        .login-wrap {
            width: 90%;
            padding: 32px 24px;
            margin: 20px;
        }
    }

    /* Noscript 警告样式 */
    noscript .error {
        position: fixed;
        top: 20px;
        left: 50%;
        transform: translate3d(-50%, 0, 0);
        z-index: 1000;
        box-shadow: 0 10px 25px rgba(0,0,0,0.2);
    }
</style>
</head>

</html>
<noscript>
    <div class="error">JavaScript is disabled — some features may not work.</div>
</noscript>

<script>
(function(){
    var form = document.querySelector('form');
    if(!form) return;
    var username = document.getElementById('username');
    var password = document.getElementById('password');
    var submit = form.querySelector('button[type="submit"]');
    
    // 密码加密和提交处理
    [username, password].forEach(function(input) {
        if(input) {
            input.addEventListener('keypress', function(e) {
                if (e.key === 'Enter' || e.keyCode === 13) {
                    e.preventDefault();
                    if(password.value) {
                        password.value = SHA256Util.encrypt(password.value);
                    }
                    submitForm();
                }
            });
        }
    });

    function submitForm() {
        if(submit) {
            submit.disabled = true;
            submit.classList.add('loading');
        }
        form.submit();
    }

    form.addEventListener('submit', function(e){
        e.preventDefault();
        if(password.value && !password.value.match(/^[a-f0-9]{64}$/i)) {
            password.value = SHA256Util.encrypt(password.value);
        }
        submitForm();
    });

    // 密码可见性切换
    if(password){
        var container = password.parentNode;
        
        var toggle = document.createElement('button');
        toggle.type = 'button';
        toggle.className = 'toggle-password';
        toggle.setAttribute('aria-label','Toggle password visibility');
        toggle.innerHTML = `
            <svg width="20" height="20" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 12a3 3 0 11-6 0 3 3 0 016 0z"></path>
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M2.458 12C3.732 7.943 7.523 5 12 5c4.478 0 8.268 2.943 9.542 7-1.274 4.057-5.064 7-9.542 7-4.477 0-8.268-2.943-9.542-7z"></path>
            </svg>
        `;

        toggle.addEventListener('click', function(){
            if(password.type === 'password'){
                password.type = 'text';
                toggle.innerHTML = `
                    <svg width="20" height="20" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M13.875 18.825A10.05 10.05 0 0112 19c-4.478 0-8.268-2.943-9.543-7a9.97 9.97 0 011.563-3.029m5.858.908a3 3 0 114.243 4.243M9.878 9.878l4.242 4.242M9.88 9.88l-3.29-3.29m7.532 7.532l3.29 3.29M3 3l3.59 3.59m0 0A9.953 9.953 0 0112 5c4.478 0 8.268 2.943 9.543 7a10.025 10.025 0 01-4.132 5.411m0 0L21 21"></path>
                    </svg>
                `;
            } else {
                password.type = 'password';
                toggle.innerHTML = `
                    <svg width="20" height="20" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 12a3 3 0 11-6 0 3 3 0 016 0z"></path>
                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M2.458 12C3.732 7.943 7.523 5 12 5c4.478 0 8.268 2.943 9.542 7-1.274 4.057-5.064 7-9.542 7-4.477 0-8.268-2.943-9.542-7z"></path>
                    </svg>
                `;
            }
            password.focus();
        });

        container.appendChild(toggle);
    }

    // 输入框焦点自动清除错误状态（如果有）
    var errorDivs = document.querySelectorAll('.error');
    if(errorDivs.length > 0) {
        [username, password].forEach(function(input) {
            if(input) {
                input.addEventListener('input', function() {
                    errorDivs.forEach(function(err) {
                        err.style.opacity = '0';
                        err.style.transform = 'translateY(-10px)';
                        setTimeout(function() {
                            err.style.display = 'none';
                        }, 300);
                    });
                });
            });
        });
    }
})();
</script>