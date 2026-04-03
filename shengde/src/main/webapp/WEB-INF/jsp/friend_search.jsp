<!-- friend_search.jsp - 搜索添加好友 -->
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<div class="modal" id="searchModal" style="display:none;">
    <div class="modal-content">
        <h3>添加好友</h3>
        <input type="text" id="searchInput" placeholder="输入账号或昵称" onkeyup="doSearch()">
        <div class="search-results" id="searchResults"></div>
        <button onclick="closeSearchModal()">关闭</button>
    </div>
</div>

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
                if (res.code === 0) {
                    renderSearchResults(res.data);
                }
            });
    }, 300);
}

function renderSearchResults(users) {
    const container = document.getElementById('searchResults');
    container.innerHTML = users.map(u => `
        <div class="user-item">
            <img src="${u.avatar || 'default-avatar.png'}" class="avatar">
            <div class="user-info">
                <div>${u.nickname} (${u.account})</div>
                <div class="status ${u.status === 1 ? 'online' : 'offline'}">
                    ${u.status === 1 ? '在线' : '离线'}
                </div>
            </div>
            <button onclick="applyFriend(${u.userId}, '${u.nickname}')">添加</button>
        </div>
    `).join('');
}

function applyFriend(friendId, nickname) {
    const applyMsg = prompt('发送验证消息:', '我是' + '${sessionScope.user.nickname}');
    if (applyMsg === null) return;
    
    const params = new URLSearchParams();
    params.append('friendId', friendId);
    params.append('applyMsg', applyMsg);
    
    fetch('${pageContext.request.contextPath}/friend/apply', {
        method: 'POST',
        body: params
    }).then(r => r.json()).then(res => {
        alert(res.code === 0 ? '申请已发送' : res.msg);
    });
}
</script>