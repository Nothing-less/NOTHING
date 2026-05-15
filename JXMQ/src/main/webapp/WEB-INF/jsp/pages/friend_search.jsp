<!-- friend_search.jsp - 搜索添加好友 -->
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<link rel="stylesheet" href="<c:url value='/static/css/pages.css' />">
<div class="modal" id="searchModal" >
    <div class="modal-content">
        <h3>添加好友</h3>
        <input type="text" id="searchInput" placeholder="输入账号或昵称" onkeyup="doSearch()">
        <div class="search-results" id="searchResults"></div>
        <button onclick="closeSearchModal()">关闭</button>
    </div>
</div>
<script src="<c:url value='/static/js/ChatClient.js' />" ></script>
    <script>
    function showSearchModal() {
        document.getElementById('searchModal').style.display = 'flex';
        document.getElementById('searchInput').focus();
    }

    function closeSearchModal() {
        document.getElementById('searchModal').style.display = 'none';
    }

    let searchTimer;
    function doSearch() {
        clearTimeout(searchTimer);
        const keyword = document.getElementById('searchInput').value;
        if (keyword.length < 2) return;
        
        searchTimer = setTimeout(() => {
            fetch('${pageContext.request.contextPath}/friend/search?keyword=' + keyword)
                .then(r => r.json())
                .then(res => {
                    if (res.code === 200) {
                        renderSearchResults(res.data);
                    }
                });
        }, 300);
    }

    function renderSearchResults(users) {
        const container = document.getElementById('searchResults');
        
        if (!users || users.length === 0) {
            container.innerHTML = '<div class="no-result">未找到用户</div>';
            return;
        }
        
        // 生成 HTML，使用 data-* 属性存储数据
        container.innerHTML = users.map(function(u) {
            var statusClass = (u.userKey1 === 'ONLINE') ? 'online' : 'offline';
            var avatar = u.avatar || 'default-avatar.png';
            
            return '<div class="user-item" data-uid="' + u.userId + '" data-nick="' + escapeHtml(u.nickname) + '">' +
                '<img src="' + avatar + '" class="avatar">' +
                '<div class="user-info">' +
                    '<div>' + escapeHtml(u.nickname) + ' (' + escapeHtml(u.userAccount) + ')</div>' +
                    '<div class="status ' + statusClass + '">' + u.userKey1 + '</div>' +
                '</div>' +
                '<button class="add-friend-btn">添加</button>' +
            '</div>';
        }).join('');
        
        // 事件委托绑定点击事件
        container.querySelectorAll('.add-friend-btn').forEach(function(btn) {
            btn.onclick = function() {
                var item = this.closest('.user-item');
                var friendId = item.getAttribute('data-uid');
                var nickname = item.getAttribute('data-nick');
                applyFriend(friendId, nickname);
            };
        });
    }

    // HTML 转义函数，防止特殊字符破坏属性
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
        const applyMsg = prompt('发送验证消息:', '我是' + window.parent.CURRENT_USER.nickname);
        if (applyMsg === null) return;
        
        const params = new URLSearchParams();
        params.append('friendId', friendId);
        params.append('applyMsg', applyMsg);
        
        fetch('${pageContext.request.contextPath}/friend/apply', {
            method: 'POST',
            body: params
        }).then(r => r.json()).then(res => {
            alert(res.code === 200 ? '申请已发送' : res.msg);
        });
    }
</script>