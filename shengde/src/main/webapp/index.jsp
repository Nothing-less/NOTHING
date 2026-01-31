<%@ page contentType="text/html; charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8"/>
    <title>Login</title>
    <meta name="viewport" content="width=device-width,initial-scale=1"/>
    <style>
        body { font-family: Arial, sans-serif; background:#f5f5f5; }
        .login-wrap { width: 320px; margin: 8% auto; padding: 24px; background: #fff; border-radius: 6px; box-shadow: 0 2px 8px rgba(0,0,0,.1); }
        .login-wrap h1 { margin: 0 0 16px; font-size: 20px; text-align:center; }
        .form-row { margin-bottom: 12px; }
        label { display:block; margin-bottom:6px; font-size:13px; }
        input[type="text"], input[type="password"] { width:100%; padding:8px 10px; box-sizing:border-box; border:1px solid #ccc; border-radius:4px; }
        .actions { text-align:right; }
        .btn { padding:8px 14px; border:none; background:#007bff; color:#fff; border-radius:4px; cursor:pointer; }
        .btn:active { transform: translateY(1px); }
        .error { color:#b00020; margin-bottom:12px; font-size:13px; }
        .hint { font-size:12px; color:#666; margin-top:8px; }
    </style>
</head>
<body>
<div class="login-wrap">
    <h1>Sign in</h1>

    <c:if test="${not empty requestScope.error}">
        <div class="error">${fn:escapeXml(requestScope.error)}</div>
    </c:if>
    <c:if test="${not empty param.error}">
        <div class="error">${fn:escapeXml(param.error)}</div>
    </c:if>

    <form action="${pageContext.request.contextPath}/login" method="post" autocomplete="off">
        <c:if test="${not empty _csrf}">
            <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}"/>
        </c:if>

        <div class="form-row">
            <label for="username">Username</label>
            <input id="username" name="username" type="text" value="${fn:escapeXml(param.username)}" required autofocus/>
        </div>

        <div class="form-row">
            <label for="password">Password</label>
            <input id="password" name="password" type="password" required/>
        </div>

        <div class="actions">
            <button type="submit" class="btn">Log in</button>
        </div>

        <c:if test="${not empty param.redirect}">
            <input type="hidden" name="redirect" value="${fn:escapeXml(param.redirect)}"/>
        </c:if>
    </form>
    <%-- <div class="hint">If you forgot your password, contact your administrator.</div> --%>
</div>
</body>
</html>
<noscript><div class="error">JavaScript is disabled — some features may not work.</div></noscript>

<script>
(function(){
    var form = document.querySelector('form');
    if(!form) return;
    var username = document.getElementById('username');
    var password = document.getElementById('password');
    var submit = form.querySelector('button[type="submit"]');
    
    [username, password].forEach(function(input) {
        if(input) {
            input.addEventListener('keypress', function(e) {
                if (e.key === 'Enter' || e.keyCode === 13) {
                    e.preventDefault(); // 防止重复提交
                    form.submit();      // 触发表单提交
                }
            });
        }
    });

    form.addEventListener('submit', function(){
        if(submit) submit.disabled = true;
    });

    if(password){
        // create a simple show/hide toggle appended to the password field container
        var container = password.parentNode;
        container.style.position = 'relative';

        var toggle = document.createElement('button');
        toggle.type = 'button';
        toggle.setAttribute('aria-label','Toggle password visibility');
        toggle.textContent = 'Show';
        toggle.style.position = 'absolute';
        toggle.style.right = '10px';
        toggle.style.top = '50%';
        toggle.style.transform = 'translateY(-50%)';
        toggle.style.padding = '6px';
        toggle.style.border = 'none';
        toggle.style.background = 'transparent';
        toggle.style.cursor = 'pointer';
        toggle.style.color = '#007bff';

        toggle.addEventListener('click', function(){
            if(password.type === 'password'){
                password.type = 'text';
                toggle.textContent = 'Hide';
            } else {
                password.type = 'password';
                toggle.textContent = 'Show';
            }
            password.focus();
        });

        container.appendChild(toggle);
    }
})();
</script>