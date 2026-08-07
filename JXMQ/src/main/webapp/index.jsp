<%@ page contentType="text/html; charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<!DOCTYPE html>
<html lang="en">

<body>
    <!-- 马卡龙色系动态渐变背景 -->
    <div class="gradient-bg"></div>
    
    <!-- 柔和光球 -->
    <div class="orb orb-1"></div>
    <div class="orb orb-2"></div>
    <div class="orb orb-3"></div>
    <div class="orb orb-4"></div>

    <div class="login-wrapper">
        <!-- 使用 liquidGlassLarge - 需要添加 glassLightMode 适配浅色背景 -->
        <div class="liquidGlassLarge glassLightMode">
            <div class="login-content">
                <div class="login-header">
                    <h1>Welcome</h1>
                    <p>Sign in</p>
                </div>

                <c:if test="${not empty requestScope.error}">
                    <div class="error-glass">
                        <svg width="20" height="20" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 8v4m0 4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z"></path>
                        </svg>
                        ${fn:escapeXml(requestScope.error)}
                    </div>
                </c:if>
                <c:if test="${not empty param.error}">
                    <div class="error-glass">
                        <svg width="20" height="20" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 8v4m0 4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z"></path>
                        </svg>
                        ${fn:escapeXml(param.error)}
                    </div>
                </c:if>

                <form action="${pageContext.request.contextPath}/login" method="post" autocomplete="true">
                    <c:if test="${not empty _csrf}">
                        <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}"/>
                    </c:if>

                    <div class="form-row">
                        <label for="username">Username</label>
                        <div class="input-wrapper">
                            <input id="username" name="username" type="text" 
                                    value="${fn:escapeXml(param.username)}" 
                                    required autofocus 
                                    autocomplete="username"
                                    pattern="[a-zA-Z0-9]+"
                                    title="Only letters and numbers are allowed"
                                    placeholder="your username"/>
                            <svg class="input-icon" width="20" height="20" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" 
                                      d="M16 7a4 4 0 11-8 0 4 4 0 018 0zM12 14a7 7 0 00-7 7h14a7 7 0 00-7-7z"></path>
                            </svg>
                        </div>
                    </div>

                    <div class="form-row">
                        <label for="password">Password</label>
                        <div class="input-wrapper">
                            <input id="password" name="password" type="password" 
                                    autocomplete="current-password"
                                    required  
                                    placeholder="your password"/>
                            <input type="hidden" id="pwd_entrypted" name="pwd_entrypted" type="pwd_entrypted" />
                        </div>
                    </div>

                    <div class="actions">
                        <button type="submit" class="btn-login">
                            Sign In
                        </button>
                    </div>

                    <c:if test="${not empty param.redirect}">
                        <input type="hidden" name="redirect" value="${fn:escapeXml(param.redirect)}"/>
                    </c:if>
                </form>
                
                <div class="hint">
                    Forgot your password? <a href="#">Contact administrator</a>
                </div>
            </div>
        </div>
    </div>

    <noscript>
        <div style="position:fixed;top:20px;left:50%;transform:translateX(-50%);z-index:1000;background:rgba(255,150,150,0.9);color:#fff;padding:16px 24px;border-radius:12px;font-weight:500;">
            JavaScript is disabled — some features may not work.
        </div>
    </noscript>

    <script>
    (function(){
        var form = document.querySelector('form');
        if(!form) return;
        var username = document.getElementById('username');
        var password = document.getElementById('password');
        var pwd_entrypted = document.getElementById('pwd_entrypted');
        var submit = form.querySelector('button[type="submit"]');
        
        if(username) {
            username.addEventListener('input', function(e) {
                // 实时过滤：把非字母数字的字符替换掉
                var original = this.value;
                var filtered = original.replace(/[^a-zA-Z0-9]/g, '');
                if(original !== filtered) {
                    this.value = filtered;
                }
            });
            username.addEventListener('paste', function(e) {
                e.preventDefault();
                var paste = (e.clipboardData || window.clipboardData).getData('text');
                var filtered = paste.replace(/[^a-zA-Z0-9]/g, '');
                // 在光标位置插入
                var start = this.selectionStart;
                var end = this.selectionEnd;
                this.value = this.value.substring(0, start) + filtered + this.value.substring(end);
                this.selectionStart = this.selectionEnd = start + filtered.length;
            });
        };

        // 密码加密和提交处理
        [username, password].forEach(function(input) {
            if(input) {
                input.addEventListener('keypress', function(e) {
                    if (e.key === 'Enter' || e.keyCode === 13) {
                        e.preventDefault();
                        if(password.value) {
                            e.preventDefault();
                            submitForm();
                        }
                    }
                });
            }
        });

        function submitForm() {
            if(username && username.value) {
                username.value = username.value.toLowerCase().trim();
            }
            
            if(password.value && !password.value.match(/^[a-f0-9]{64}$/i)) {
                pwd_entrypted.value = SHA256Util.encrypt(password.value.trim());
            }

            if(submit) {
                submit.disabled = true;
                submit.classList.add('loading');
            }
            password.removeAttribute('name');
            form.submit();
        };

        form.addEventListener('submit', function(e){
            e.preventDefault();
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
                <svg width="18" height="18" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 12a3 3 0 11-6 0 3 3 0 016 0z"></path>
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M2.458 12C3.732 7.943 7.523 5 12 5c4.478 0 8.268 2.943 9.542 7-1.274 4.057-5.064 7-9.542 7-4.477 0-8.268-2.943-9.542-7z"></path>
                </svg>
            `;

            toggle.addEventListener('click', function(){
                if(password.type === 'password'){
                    password.type = 'text';
                    toggle.innerHTML = `
                        <svg width="18" height="18" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M13.875 18.825A10.05 10.05 0 0112 19c-4.478 0-8.268-2.943-9.543-7a9.97 9.97 0 011.563-3.029m5.858.908a3 3 0 114.243 4.243M9.878 9.878l4.242 4.242M9.88 9.88l-3.29-3.29m7.532 7.532l3.29 3.29M3 3l3.59 3.59m0 0A9.953 9.953 0 0112 5c4.478 0 8.268 2.943 9.543 7a10.025 10.025 0 01-4.132 5.411m0 0L21 21"></path>
                        </svg>
                    `;
                } else {
                    password.type = 'password';
                    toggle.innerHTML = `
                        <svg width="18" height="18" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 12a3 3 0 11-6 0 3 3 0 016 0z"></path>
                            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M2.458 12C3.732 7.943 7.523 5 12 5c4.478 0 8.268 2.943 9.542 7-1.274 4.057-5.064 7-9.542 7-4.477 0-8.268-2.943-9.542-7z"></path>
                        </svg>
                    `;
                }
                password.focus();
            });

            container.appendChild(toggle);
        }

        // 输入时清除错误提示
        var errorDivs = document.querySelectorAll('.error-glass');
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
                }
            });
        }
    })();
    </script>
</body>

<head>
    <meta charset="UTF-8"/>
    <title>Hello</title>
    <meta name="viewport" content="width=device-width,initial-scale=1"/>
    <script src="<c:url value='/static/js/SHA256Util.js' />" /> </script>
    <link rel="stylesheet" href="<c:url value='/static/css/liquidGlass.css' />">
    <script src="<c:url value='/static/js/liquidGlass.js' />"></script>
    
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
            position: relative;
            overflow: hidden;
            background: #fef9f6; /* 温暖的米白后备色 */
        }

        /* 马卡龙色系动态渐变背景
        /* ============================================================
        颜色插值的动态背景（@property 方案）
        ============================================================ */

        /* 注册颜色变量 */
        @property --c1 { syntax: '<color>'; initial-value: #ffd1dc; inherits: false; }
        @property --c2 { syntax: '<color>'; initial-value: #e0b0ff; inherits: false; }
        @property --c3 { syntax: '<color>'; initial-value: #c9e9f6; inherits: false; }
        @property --c4 { syntax: '<color>'; initial-value: #a8d8ea; inherits: false; }
        @property --c5 { syntax: '<color>'; initial-value: #b5e7a0; inherits: false; }
        @property --c6 { syntax: '<color>'; initial-value: #fffacd; inherits: false; }
        @property --c7 { syntax: '<color>'; initial-value: #ffdab9; inherits: false; }
        @property --c8 { syntax: '<color>'; initial-value: #ffb7c5; inherits: false; }

        /* 背景层 */
        .gradient-bg {
            position: fixed;
            inset: 0;
            z-index: 0;
            color-interpolation: in oklch;
            
            /* 用变量构建渐变 */
            background: linear-gradient(
                -45deg,
                var(--c1),  /* 樱花粉 */
                var(--c2),  /* 淡紫罗兰 */
                var(--c3),  /* baby蓝 */
                var(--c4),  /* 薄荷蓝 */
                var(--c5),  /* 薄荷绿 */
                var(--c6),  /* 柠檬绸 */
                var(--c7),  /* 杏桃 */
                var(--c8),  /* 深樱花粉 */
                var(--c1)   /* 闭环回到樱花粉 */
            );
            background-size: 400% 400%;
            
            animation: macaronRing  30s ease infinite;
            
            /* 保留性能优化 */
            will-change: background;
            transform: translateZ(0);
        }

        /* 噪点纹理 */
        .gradient-bg::after {
        content: '';
        position: absolute;
        inset: 0;
        opacity: 0.02;
        background-image: url("data:image/svg+xml,%3Csvg viewBox='0 0 200 200' xmlns='http://www.w3.org/2000/svg'%3E%3Cfilter id='noiseFilter'%3E%3CfeTurbulence type='fractalNoise' baseFrequency='0.9' numOctaves='3' stitchTiles='stitch'/%3E%3C/filter%3E%3Crect width='100%25' height='100%25' filter='url(%23noiseFilter)'/%3E%3C/svg%3E");
        pointer-events: none;
        }

        /* 关键帧 */
        @keyframes macaronRing {
            0.0% { --c1: #ffd1dc; --c2: #e0b0ff; --c3: #c9e9f6; --c4: #a8d8ea; --c5: #b5e7a0; --c6: #fffacd; --c7: #ffdab9; --c8: #ffb7c5; }
            12.5% { --c1: #e0b0ff; --c2: #c9e9f6; --c3: #a8d8ea; --c4: #b5e7a0; --c5: #fffacd; --c6: #ffdab9; --c7: #ffb7c5; --c8: #ffd1dc; }
            25.0% { --c1: #c9e9f6; --c2: #a8d8ea; --c3: #b5e7a0; --c4: #fffacd; --c5: #ffdab9; --c6: #ffb7c5; --c7: #ffd1dc; --c8: #e0b0ff; }
            37.5% { --c1: #a8d8ea; --c2: #b5e7a0; --c3: #fffacd; --c4: #ffdab9; --c5: #ffb7c5; --c6: #ffd1dc; --c7: #e0b0ff; --c8: #c9e9f6; }
            50.0% { --c1: #b5e7a0; --c2: #fffacd; --c3: #ffdab9; --c4: #ffb7c5; --c5: #ffd1dc; --c6: #e0b0ff; --c7: #c9e9f6; --c8: #a8d8ea; }
            62.5% { --c1: #fffacd; --c2: #ffdab9; --c3: #ffb7c5; --c4: #ffd1dc; --c5: #e0b0ff; --c6: #c9e9f6; --c7: #a8d8ea; --c8: #b5e7a0; }
            75.0% { --c1: #ffdab9; --c2: #ffb7c5; --c3: #ffd1dc; --c4: #e0b0ff; --c5: #c9e9f6; --c6: #a8d8ea; --c7: #b5e7a0; --c8: #fffacd; }
            87.5% { --c1: #ffb7c5; --c2: #ffd1dc; --c3: #e0b0ff; --c4: #c9e9f6; --c5: #a8d8ea; --c6: #b5e7a0; --c7: #fffacd; --c8: #ffdab9; }
            100.0% { --c1: #ffd1dc; --c2: #e0b0ff; --c3: #c9e9f6; --c4: #a8d8ea; --c5: #b5e7a0; --c6: #fffacd; --c7: #ffdab9; --c8: #ffb7c5; }
        }

        /* 马卡龙色浮动光球 - 超慢速漂浮 */
        .orb {
            position: absolute;
            border-radius: 50%;
            filter: blur(100px);
            z-index: 1;
            mix-blend-mode: multiply;
            opacity: 0.6;
            animation: orbFloat 30s ease-in-out infinite;
        }
        
        .orb-1 {
            width: 500px;
            height: 500px;
            background: radial-gradient(circle, rgba(255, 209, 220, 0.45) 0%, transparent 70%);
            top: -15%;
            left: -10%;
            animation-delay: 0s;
        }

        .orb-2 {
            width: 450px;
            height: 450px;
            background: radial-gradient(circle, rgba(201, 233, 246, 0.40) 0%, transparent 70%);
            bottom: -10%;
            right: -5%;
            animation-delay: -10s;
            animation-duration: 50s;
        }

        .orb-3 {
            width: 400px;
            height: 400px;
            background: radial-gradient(circle, rgba(181, 231, 160, 0.40) 0%, transparent 70%)
            top: 50%;
            left: 60%;
            animation-delay: -20s;
            animation-duration: 45s;
        }

        .orb-4 {
            width: 350px;
            height: 350px;
            background: radial-gradient(circle, rgba(255, 244, 230, 0.45) 0%, transparent 70%);
            top: 60%;
            left: 15%;
            animation-delay: -30s;
            animation-duration: 55s;
        }
       
        /*
        .orb-1 {
            background: radial-gradient(circle, rgba(255, 209, 220, 0.5) 0%, transparent 70%);
        }
        .orb-2 {
            background: radial-gradient(circle, rgba(181, 231, 160, 0.4) 0%, transparent 70%);
        }
        .orb-3 {
            background: radial-gradient(circle, rgba(201, 233, 246, 0.45) 0%, transparent 70%);
        }
        .orb-4 {
            background: radial-gradient(circle, rgba(255, 244, 230, 0.5) 0%, transparent 70%);
        }
        */
        @keyframes orbFloat {
            0%, 100% { 
                transform: translate(0, 0) scale(1); 
            }
            25% { 
                transform: translate(30px, -20px) scale(1.05); 
            }
            50% { 
                transform: translate(-20px, 30px) scale(0.95); 
            }
            75% { 
                transform: translate(25px, 25px) scale(1.02); 
            }
        }

        /* 登录容器 */
        .login-wrapper {
            position: relative;
            z-index: 10;
            width: 420px;
            padding: 0px;
        }

        .login-content {
            padding: 40px;
        }

        .login-header {
            text-align: center;
            margin-bottom: 32px;
        }

        .login-header h1 {
            font-size: 32px;
            font-weight: 700;
            margin-bottom: 8px;
            color: #000000; /* 柔和深灰，适配浅色背景 */
            letter-spacing: -0.5px;
        }

        .login-header p {
            font-size: 14px;
            color: #888;
            font-weight: 400;
        }

        /* 表单样式 - 适配浅色背景 */
        .form-row {
            margin-bottom: 20px;
        }

        label {
            display: block;
            margin-bottom: 8px;
            font-size: 12px;
            font-weight: 600;
            color: #666;
            text-transform: uppercase;
            letter-spacing: 1px;
        }

        .input-wrapper {
            position: relative;
            display: flex;
            align-items: center;
        }

        /* 玻璃态输入框 - 浅色模式 */
        input[type="text"], input[type="password"] {
            width: 100%;
            padding: 16px 18px;
            padding-right: 50px;
            font-size: 15px;
            border: 1px solid rgba(255, 255, 255, 0.6);
            border-radius: 14px;
            background: rgba(255, 255, 255, 0.25);
            color: #555;
            backdrop-filter: blur(10px);
            -webkit-backdrop-filter: blur(10px);
            transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
            outline: none;
            box-shadow: 0 2px 8px rgba(0,0,0,0.05);
        }

        input[type="text"]::placeholder, input[type="password"]::placeholder {
            color: #999;
        }

        input[type="text"]:hover, input[type="password"]:hover {
            background: rgba(255, 255, 255, 0.35);
            border-color: rgba(255, 255, 255, 0.8);
            transform: translateY(-1px);
            box-shadow: 0 4px 12px rgba(0,0,0,0.08);
        }

        input[type="text"]:focus, input[type="password"]:focus {
            background: rgba(255, 255, 255, 0.45);
            border-color: rgba(200, 200, 255, 0.9);
            box-shadow: 0 0 0 4px rgba(201, 233, 246, 0.3);
            transform: translateY(-2px);
        }

        .input-icon {
            position: absolute;
            right: 18px;
            color: #aaa;
            pointer-events: none;
            transition: color 0.3s ease;
        }

        .form-row:focus-within .input-icon {
            color: #999;
        }

        /* 密码可见性切换 - 浅色适配 */
        .toggle-password {
            position: absolute;
            right: 14px;
            background: rgba(255, 255, 255, 0.3);
            border: none;
            color: #888;
            cursor: pointer;
            padding: 6px;
            border-radius: 8px;
            transition: all 0.2s ease;
            z-index: 15;
            display: flex;
            align-items: center;
            justify-content: center;
        }

        .toggle-password:hover {
            color: #666;
            background: rgba(255, 255, 255, 0.5);
            transform: scale(1.1);
        }

        /* 按钮样式 - 马卡龙色系 */
        .btn-login {
            width: 100%;
            margin-top: 12px;
            padding: 16px 24px;
            font-size: 16px;
            font-weight: 700;
            letter-spacing: 0.5px;
            text-transform: uppercase;
            cursor: pointer;
            position: relative;
            overflow: hidden;
            transition: all 0.3s ease;
            background: rgba(255, 209, 220, 0.6); /* 樱花粉玻璃 */
            border: 1px solid rgba(255, 255, 255, 0.8);
            color: #8b6b7b;
            backdrop-filter: blur(10px);
            border-radius: 12px;
            text-shadow: none;
            box-shadow: 0 4px 15px rgba(255, 209, 220, 0.4);
        }

        .btn-login::before {
            content: '';
            position: absolute;
            top: 0;
            left: -100%;
            width: 100%;
            height: 100%;
            background: linear-gradient(90deg, transparent, rgba(255,255,255,0.4), transparent);
            transition: left 0.6s ease;
        }

        .btn-login:hover::before {
            left: 100%;
        }

        .btn-login:hover {
            transform: translateY(-2px);
            background: rgba(255, 209, 220, 0.8);
            box-shadow: 0 8px 25px rgba(255, 209, 220, 0.5);
            color: #7a5a6a;
        }

        .btn-login:active {
            transform: translateY(0);
        }

        .btn-login:disabled {
            opacity: 0.6;
            cursor: not-allowed;
            transform: none;
        }

        /* 加载动画 */
        .btn-login.loading {
            color: transparent !important;
        }

        .btn-login.loading::after {
            content: '';
            position: absolute;
            width: 24px;
            height: 24px;
            top: 50%;
            left: 50%;
            transform: translate(-50%, -50%);
            border: 3px solid rgba(139, 107, 123, 0.3);
            border-radius: 50%;
            border-top-color: #8b6b7b;
            animation: spinner 0.8s linear infinite;
        }

        @keyframes spinner {
            to { transform: translate(-50%, -50%) rotate(360deg); }
        }

        /* 玻璃态错误提示 - 浅色适配 */
        .error-glass {
            background: rgba(255, 200, 200, 0.25);
            border: 1px solid rgba(255, 150, 150, 0.4);
            color: #c45c5c;
            padding: 14px 18px;
            border-radius: 14px;
            margin-bottom: 24px;
            font-size: 14px;
            font-weight: 500;
            display: flex;
            align-items: center;
            gap: 10px;
            backdrop-filter: blur(20px);
            -webkit-backdrop-filter: blur(20px);
            animation: shake 0.5s ease-in-out, fadeIn 0.3s ease;
        }

        @keyframes shake {
            0%, 100% { transform: translateX(0); }
            25% { transform: translateX(-6px); }
            75% { transform: translateX(6px); }
        }

        @keyframes fadeIn {
            from { opacity: 0; transform: translateY(-10px); }
            to { opacity: 1; transform: translateY(0); }
        }

        /* 底部提示 - 浅色适配 */
        .hint {
            text-align: center;
            margin-top: 28px;
            font-size: 13px;
            color: #888;
            font-weight: 400;
        }

        .hint a {
            color: #b5a0c9; /* 淡薰衣草紫 */
            text-decoration: none;
            font-weight: 600;
            position: relative;
            padding-bottom: 2px;
            transition: color 0.3s ease;
        }

        .hint a:hover {
            color: #9a85b0;
        }

        .hint a::after {
            content: '';
            position: absolute;
            bottom: 0;
            left: 0;
            width: 0;
            height: 1px;
            background: #b5a0c9;
            transition: width 0.3s ease;
        }

        .hint a:hover::after {
            width: 100%;
        }

        /* 响应式 */
        @media (max-width: 480px) {
            .login-wrapper {
                width: 92%;
                margin: 0px;
            }
            .login-content {
                padding: 32px 24px;
            }
            .login-header h1 {
                font-size: 28px;
            }
        }

        /* 性能优化 */
        .gradient-bg, .orb {
            will-change: transform, background-position;
            transform: translateZ(0);
        }
    </style>
</head>
</html>