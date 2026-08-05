<!-- profile.jsp - 个人资料更新页面 -->
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ page import="icu.nothingless.pojo.dto.User" %>
<%@ page import="icu.nothingless.tools.RedirectUtil" %>

<%
    User currentUser = (User) icu.nothingless.tools.RedirectUtil.getFlash(request, "CURRENT_USER");
    String avatarUrl = currentUser.userKey2();
    if (avatarUrl == null || avatarUrl.trim().isEmpty()) {
        avatarUrl = request.getContextPath() + "/static/images/default-avatar.png";
    }
%>

<link rel="stylesheet" href="<c:url value='/static/css/pages.css' />">

<div class="profile-container" style="padding: 20px; max-width: 500px; margin: 0 auto;">
    <div class="panel-header" style="margin-bottom: 24px; text-align: center;">
        <h2 style="color: #fff; font-size: 20px; margin: 0;">📝 个人信息</h2>
    </div>

    <!-- 头像区域 -->
    <div class="avatar-section" style="text-align: center; margin-bottom: 24px;">
        <div class="avatar-wrapper" style="position: relative; display: inline-block;">
            <img id="avatarPreview" src="<%= avatarUrl %>" 
                style="width: 280px; height: 280px; border-radius: 50%; object-fit: cover; 
                       border: 3px solid rgba(99,102,241,0.5); cursor: pointer;"
                onclick="document.getElementById('avatarInput').click()"
                title="点击更换头像">
            <div class="avatar-overlay" 
                style="position: absolute; bottom: 0; right: 0; width: 32px; height: 32px; 
                       background: linear-gradient(135deg, #6366f1, #4f46e5); border-radius: 50%; 
                       display: flex; align-items: center; justify-content: center; 
                       cursor: pointer; border: 2px solid #1e1b4b;"
                onclick="document.getElementById('avatarInput').click()">
                <span style="color: #fff; font-size: 48px;">📷</span>
            </div>
        </div>
        <input type="file" id="avatarInput" accept="image/*" style="display: none;" onchange="uploadAvatar(this)">
        <p style="color: #888; font-size: 12px; margin-top: 8px;">点击头像更换</p>
    </div>

    <form id="profileForm" style="display: flex; flex-direction: column; gap: 16px;">
        <!-- 昵称 -->
        <div class="form-group">
            <label style="display: block; color: #a0a0b8; font-size: 13px; margin-bottom: 6px;">昵称</label>
            <input type="text" id="nickname" name="nickname" 
                value="<%= currentUser.nickname() != null ? currentUser.nickname() : "" %>" 
                placeholder="请输入昵称"
                style="width: 100%; padding: 10px 14px; border-radius: 8px; border: 1px solid rgba(99,102,241,0.3); 
                       background: #1e1b4b; color: #fff; font-size: 14px; box-sizing: border-box;">
        </div>

        <!-- 个人简介 -->
        <div class="form-group">
            <label style="display: block; color: #a0a0b8; font-size: 13px; margin-bottom: 6px;">个人简介</label>
            <textarea id="userInfos" name="userInfos" rows="4" placeholder="介绍一下自己..."
                style="width: 100%; padding: 10px 14px; border-radius: 8px; border: 1px solid rgba(99,102,241,0.3); 
                       background: #1e1b4b; color: #fff; font-size: 14px; box-sizing: border-box; resize: vertical;"><%= currentUser.userInfos() != null ? currentUser.userInfos() : "" %></textarea>
        </div>

        <div class="form-actions" style="margin-top: 8px; display: flex; gap: 12px;">
            <button type="button" onclick="submitForm()" 
                style="flex: 1; padding: 12px; border-radius: 8px; border: none; 
                       background: linear-gradient(135deg, #6366f1, #4f46e5); color: #fff; 
                       font-size: 14px; font-weight: 500; cursor: pointer; transition: all 0.2s;">
                保存修改
            </button>
            <button type="button" onclick="resetForm()" 
                style="flex: 1; padding: 12px; border-radius: 8px; border: 1px solid rgba(99,102,241,0.3); 
                       background: transparent; color: #a0a0b8; 
                       font-size: 14px; font-weight: 500; cursor: pointer; transition: all 0.2s;">
                重置
            </button>
        </div>
    </form>

    <div id="resultMessage" style="margin-top: 16px; padding: 12px; border-radius: 8px; display: none; font-size: 13px;"></div>
</div>

<script>
    // 保存原始值用于重置
    var originalNickname = document.getElementById('nickname').value;
    var originalUserInfos = document.getElementById('userInfos').value;

    function resetForm() {
        document.getElementById('nickname').value = originalNickname;
        document.getElementById('userInfos').value = originalUserInfos;
        document.getElementById('resultMessage').style.display = 'none';
    }

    function uploadAvatar(input) {
        var file = input.files[0];
        if (!file) return;

        // 校验文件类型
        if (!file.type.startsWith('image/')) {
            showMessage('请选择图片文件', 'error');
            return;
        }

        // 校验文件大小（最大 2MB）
        if (file.size > 2 * 1024 * 1024) {
            showMessage('图片大小不能超过 2MB', 'error');
            return;
        }

        var formData = new FormData();
        formData.append('avatar', file);

        fetch('${pageContext.request.contextPath}/upload/avatar', {
            method: 'POST',
            body: formData
        })
        .then(function(r) { return r.json(); })
        .then(function(res) {
            if (res.code >= 200 && res.code < 300) {
                // 更新头像预览
                document.getElementById('avatarPreview').src = res.data + '?t=' + Date.now();
                showMessage('✓ 头像上传成功', 'success');
                // 同步更新侧边栏头像
                var sidebarAvatar = document.getElementById('userAvatar');
                if (sidebarAvatar && sidebarAvatar.tagName === 'IMG') {
                    sidebarAvatar.src = res.data + '?t=' + Date.now();
                }
                setTimeout(() => {
                    location.reload();
                }, 900);
            } else {
                showMessage('✗ ' + (res.message || '上传失败'), 'error');
            }
        })
        .catch(function(err) {
            console.error('Upload avatar failed:', err);
            showMessage('✗ 网络错误，请重试', 'error');
        });
    }

    function submitForm() {
        var nickname = document.getElementById('nickname').value.trim();
        var userInfos = document.getElementById('userInfos').value.trim();

        if (!nickname) {
            showMessage('昵称不能为空', 'error');
            return;
        }
        if (nickname.length > 50) {
            showMessage('昵称长度不能超过50个字符', 'error');
            return;
        }
        if (userInfos.length > 500) {
            showMessage('个人简介长度不能超过500个字符', 'error');
            return;
        }

        var params = new URLSearchParams();
        params.append('nickname', nickname);
        params.append('userInfos', userInfos);

        fetch('${pageContext.request.contextPath}/user/update', {
            method: 'POST',
            body: params
        })
        .then(function(r) { return r.json(); })
        .then(function(res) {
            if (res.code >= 200 && res.code < 300) {
                showMessage('✓ 个人信息更新成功', 'success');
                originalNickname = nickname;
                originalUserInfos = userInfos;
                // 更新侧边栏昵称
                var sidebarName = document.getElementById('userName');
                if (sidebarName) sidebarName.textContent = nickname;
            } else {
                showMessage('✗ ' + (res.message || '更新失败'), 'error');
            }
        })
        .catch(function(err) {
            console.error('Update profile failed:', err);
            showMessage('✗ 网络错误，请重试', 'error');
        });
    }

    function showMessage(text, type) {
        var msgEl = document.getElementById('resultMessage');
        msgEl.style.display = 'block';
        msgEl.textContent = text;
        
        if (type === 'success') {
            msgEl.style.background = 'rgba(52, 211, 153, 0.15)';
            msgEl.style.color = '#34d399';
            msgEl.style.border = '1px solid rgba(52, 211, 153, 0.3)';
        } else {
            msgEl.style.background = 'rgba(248, 113, 113, 0.15)';
            msgEl.style.color = '#f87171';
            msgEl.style.border = '1px solid rgba(248, 113, 113, 0.3)';
        }
    }
</script>