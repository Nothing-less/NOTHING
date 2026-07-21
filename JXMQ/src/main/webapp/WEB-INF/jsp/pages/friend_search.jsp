<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<link rel="stylesheet" href="<c:url value='/static/css/pages.css' />">

<!-- 搜索弹窗 -->
<div class="modal" id="searchModal">
    <div class="modal-content">
        <div class="modal-header">
            <h3>添加好友</h3>
            <button class="btn-modal-close" onclick="closeSearchModal()">关闭</button>
        </div>

        <div class="search-box">
            <input type="text"
                   id="searchInput"
                   placeholder="输入账号或昵称"
                   onkeyup="doSearch()">
        </div>

        <div class="search-results" id="searchResults">
            <div class="search-tip">请输入账号或昵称</div>
        </div>
    </div>
</div>

<script src="<c:url value='/static/js/ChatClient.js' />"></script>
<script>
    function showSearchModal() {
        document.getElementById('searchModal').classList.add('active');
        setTimeout(() => {
            document.getElementById('searchInput').focus();
        }, 100);
    }

    function closeSearchModal() {
        document.getElementById('searchModal').classList.remove('active');
    }

    let searchTimer;
    function doSearch() {
        clearTimeout(searchTimer);
        const keyword = document.getElementById('searchInput').value.trim();
        if (keyword.length < 2) {
            document.getElementById('searchResults').innerHTML =
                '<div class="search-tip">请输入至少 2 个字符开始搜索</div>';
            return;
        }

        searchTimer = setTimeout(() => {
            fetch('${pageContext.request.contextPath}/friend/search?keyword=' + encodeURIComponent(keyword))
                .then(r => r.json())
                .then(res => {
                    if (res.code === 200) {
                        renderSearchResults(res.data);
                    } else {
                        document.getElementById('searchResults').innerHTML =
                            '<div class="search-tip">搜索失败：' + (res.message || '未知错误') + '</div>';
                    }
                })
                .catch(() => {
                    document.getElementById('searchResults').innerHTML =
                        '<div class="search-tip">网络错误，请重试</div>';
                });
        }, 300);
    }

    function renderSearchResults(users) {
        const container = document.getElementById('searchResults');

        if (!users || users.length === 0) {
            container.innerHTML = '<div class="search-tip">未找到用户</div>';
            return;
        }

        container.innerHTML = users.map(function(u) {
            const statusClass = (u.userKey1 === 'ONLINE') ? 'online' : 'offline';
            const statusText  = (u.userKey1 === 'ONLINE') ? '在线' : '离线';

            const defaultAvatar = '${pageContext.request.contextPath}/static/images/default-avatar.png';
            const avatarUrl = (u.userKey2 || '').trim() ? u.userKey2 : defaultAvatar;

            return '<div class="user-item" data-uid="' + u.userId + '" data-nick="' + escapeHtml(u.nickname) + '">' +
                '<img src="' + avatarUrl + '" class="user-avatar" ' +
                     'onerror="this.src=\'' + defaultAvatar + '\'">' +
                '<div class="user-info">' +
                    '<div class="user-name">' + escapeHtml(u.nickname) + '</div>' +
                    '<div class="user-account">' + escapeHtml(u.userAccount) + '</div>' +
                    '<div class="status ' + statusClass + '">' + statusText + '</div>' +
                '</div>' +
                '<button class="btn-add-friend" onclick="applyFriendFromButton(this)">添加</button>' +
            '</div>';
        }).join('');
    }

    function applyFriendFromButton(btn) {
        const item = btn.closest('.user-item');
        const friendId = item.getAttribute('data-uid');
        const nickname = item.getAttribute('data-nick');
        applyFriend(friendId, nickname);
    }

    function escapeHtml(str) {
        if (!str) return '';
        return String(str)
            .replace(/&/g, '&amp;')
            .replace(/</g, '&lt;')
            .replace(/>/g, '&gt;')
            .replace(/"/g, '&quot;')
            .replace(/'/g, '&#39;');
    }

    function applyFriend(friendId, nickname) {
        const applyMsg = prompt('发送验证消息:', '我是 ' + (window.parent.CURRENT_USER && window.parent.CURRENT_USER.nickname || ''));
        if (applyMsg === null) return;

        const params = new URLSearchParams();
        params.append('friendId', friendId);
        params.append('applyMsg', applyMsg);

        fetch('${pageContext.request.contextPath}/friend/apply', {
            method: 'POST',
            body: params
        })
        .then(r => r.json())
        .then(res => {
            alert(res.code === 200 ? '✅ 申请已发送' : (res.message || '申请失败'));
        })
        .catch(() => {
            alert('网络错误，请重试');
        });
    }
</script>