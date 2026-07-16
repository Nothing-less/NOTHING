<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<link rel="stylesheet" href="<c:url value='/static/css/pages.css' />">
<script src="<c:url value='/static/js/SHA256Util.js' />" /> </script>

<div class="user-manage-container" style="padding: 20px; max-width: 500px; margin: 0 auto;">
    <div class="panel-header" style="margin-bottom: 24px;">
        <h2 style="color: #fff; font-size: 20px; margin: 0;">👤 新增用户账号</h2>
    </div>

    <form id="addUserForm" style="display: flex; flex-direction: column; gap: 16px;">
        <div class="form-group">
            <label style="display: block; color: #a0a0b8; font-size: 13px; margin-bottom: 6px;">账号</label>
            <input type="text" id="account" name="account" placeholder="请输入账号" 
                style="width: 100%; padding: 10px 14px; border-radius: 8px; border: 1px solid rgba(99,102,241,0.3); 
                       background: #1e1b4b; color: #fff; font-size: 14px; box-sizing: border-box;"
                onblur="validateAccount()">
            <span id="accountError" style="color: #ef4444; font-size: 12px; display: none;"></span>
        </div>

        <div class="form-group">
            <label style="display: block; color: #a0a0b8; font-size: 13px; margin-bottom: 6px;">昵称</label>
            <input type="text" id="nickname" name="nickname" placeholder="请输入昵称" 
                style="width: 100%; padding: 10px 14px; border-radius: 8px; border: 1px solid rgba(99,102,241,0.3); 
                       background: #1e1b4b; color: #fff; font-size: 14px; box-sizing: border-box;"
                onblur="validateNickname()">
            <span id="nicknameError" style="color: #ef4444; font-size: 12px; display: none;"></span>
        </div>

        <div class="form-group">
            <label style="display: block; color: #a0a0b8; font-size: 13px; margin-bottom: 6px;">密码</label>
            <input type="password" id="password" name="password" placeholder="请输入密码" 
                style="width: 100%; padding: 10px 14px; border-radius: 8px; border: 1px solid rgba(99,102,241,0.3); 
                       background: #1e1b4b; color: #fff; font-size: 14px; box-sizing: border-box;"
                onblur="validatePassword()">
            <span id="passwordError" style="color: #ef4444; font-size: 12px; display: none;"></span>
        </div>

        <div class="form-group">
            <label style="display: block; color: #a0a0b8; font-size: 13px; margin-bottom: 6px;">确认密码</label>
            <input type="password" id="confirmPassword" name="confirmPassword" placeholder="请再次输入密码" 
                style="width: 100%; padding: 10px 14px; border-radius: 8px; border: 1px solid rgba(99,102,241,0.3); 
                       background: #1e1b4b; color: #fff; font-size: 14px; box-sizing: border-box;"
                onblur="validateConfirmPassword()">
            <span id="confirmError" style="color: #ef4444; font-size: 12px; display: none;"></span>
        </div>

        <div class="form-group" style="display:none">
            <label style="display: block; color: #a0a0b8; font-size: 13px; margin-bottom: 6px;">权限</label>
            <input type="text" value="普通用户 (role_id = 0)" disabled
                style="width: 100%; padding: 10px 14px; border-radius: 8px; border: 1px solid rgba(99,102,241,0.2); 
                       background: rgba(99,102,241,0.1); color: #888; font-size: 14px; box-sizing: border-box; cursor: not-allowed;">
            <input type="hidden" id="roleId" name="roleId" value="0">
        </div>

        <div class="form-actions" style="margin-top: 8px;">
            <button type="button" onclick="submitForm()" 
                style="width: 100%; padding: 12px; border-radius: 8px; border: none; 
                       background: linear-gradient(135deg, #6366f1, #4f46e5); color: #fff; 
                       font-size: 14px; font-weight: 500; cursor: pointer; transition: all 0.2s;">
                创建用户
            </button>
        </div>
    </form>

    <div id="resultMessage" style="margin-top: 16px; padding: 12px; border-radius: 8px; display: none; font-size: 13px;"></div>
</div>

<script>
    function validateAccount() {
        var account = document.getElementById('account').value.trim();
        var errorEl = document.getElementById('accountError');
        
        if (!account) {
            errorEl.textContent = '账号不能为空';
            errorEl.style.display = 'block';
            return false;
        }
        if (account.length < 3 || account.length > 20) {
            errorEl.textContent = '账号长度需在 3-20 位之间';
            errorEl.style.display = 'block';
            return false;
        }
        if (!/^[a-zA-Z0-9_]+$/.test(account)) {
            errorEl.textContent = '账号只能包含字母、数字和下划线';
            errorEl.style.display = 'block';
            return false;
        }
        
        errorEl.style.display = 'none';
        return true;
    }

    function validateNickname() {
        var nickname = document.getElementById('nickname').value.trim();
        var errorEl = document.getElementById('nicknameError');
        
        if (!nickname) {
            errorEl.textContent = '昵称不能为空';
            errorEl.style.display = 'block';
            return false;
        }
        if (nickname.length < 2 || nickname.length > 20) {
            errorEl.textContent = '昵称长度需在 2-20 位之间';
            errorEl.style.display = 'block';
            return false;
        }
        
        errorEl.style.display = 'none';
        return true;
    }

    function validatePassword() {
        var password = document.getElementById('password').value;
        var errorEl = document.getElementById('passwordError');
        
        if (!password) {
            errorEl.textContent = '密码不能为空';
            errorEl.style.display = 'block';
            return false;
        }
        if (password.length < 6 || password.length > 32) {
            errorEl.textContent = '密码长度需在 6-32 位之间';
            errorEl.style.display = 'block';
            return false;
        }
        
        errorEl.style.display = 'none';
        return true;
    }

    function validateConfirmPassword() {
        var password = document.getElementById('password').value;
        var confirm = document.getElementById('confirmPassword').value;
        var errorEl = document.getElementById('confirmError');
        
        if (!confirm) {
            errorEl.textContent = '请确认密码';
            errorEl.style.display = 'block';
            return false;
        }
        if (password !== confirm) {
            errorEl.textContent = '两次输入的密码不一致';
            errorEl.style.display = 'block';
            return false;
        }
        
        errorEl.style.display = 'none';
        return true;
    }

    function submitForm() {
        if (!validateAccount() || !validatePassword() || !validateConfirmPassword() || !validateNickname()) {
            return;
        }

        var nickname = document.getElementById('nickname').value.trim();
        var account = document.getElementById('account').value.trim();
        var password = document.getElementById('password').value;
        var pwd_entrypted = SHA256Util.encrypt(password);
        var roleId = document.getElementById('roleId').value;

        var params = new URLSearchParams();
        params.append('nickname', nickname);
        params.append('account', account);
        params.append('password', pwd_entrypted);
        params.append('roleId', roleId);

        fetch('${pageContext.request.contextPath}/user/add', {
            method: 'POST',
            body: params
        })
        .then(function(r) { return r.json(); })
        .then(function(res) {
            var msgEl = document.getElementById('resultMessage');
            msgEl.style.display = 'block';
            
            if (res.code === 200) {
                msgEl.style.background = 'rgba(52, 211, 153, 0.15)';
                msgEl.style.color = '#34d399';
                msgEl.style.border = '1px solid rgba(52, 211, 153, 0.3)';
                msgEl.textContent = '✓ 用户创建成功';
                document.getElementById('addUserForm').reset();
            } else {
                msgEl.style.background = 'rgba(248, 113, 113, 0.15)';
                msgEl.style.color = '#f87171';
                msgEl.style.border = '1px solid rgba(248, 113, 113, 0.3)';
                msgEl.textContent = '✗ ' + (res.message || '创建失败');
            }
        })
        .catch(function(err) {
            console.error('Create user failed:', err);
            var msgEl = document.getElementById('resultMessage');
            msgEl.style.display = 'block';
            msgEl.style.background = 'rgba(248, 113, 113, 0.15)';
            msgEl.style.color = '#f87171';
            msgEl.style.border = '1px solid rgba(248, 113, 113, 0.3)';
            msgEl.textContent = '✗ 网络错误，请重试';
        });
    }
</script>