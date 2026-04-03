<!-- friend_list.jsp - 好友列表面板 -->
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<div class="friend-panel">
    <div class="friend-header">
        <div class="search-box">
            <input type="text" id="friendSearchInput" placeholder="搜索好友..." onkeyup="filterFriends()">
        </div>
        <button class="btn-add" onclick="showSearchModal()">+</button>
    </div>
    
    <div class="group-tabs" id="groupTabs">
        <!-- 分组标签动态生成 -->
    </div>
    
    <div class="friend-list" id="friendList">
        <!-- 好友列表动态加载 -->
    </div>
</div>

<script>
// 加载好友列表
function loadFriends(group = '', keyword = '') {
    fetch('${pageContext.request.contextPath}/friend/list?group=' + group + '&keyword=' + keyword)
        .then(r => r.json())
        .then(res => {
            if (res.code === 0) {
                renderFriendList(res.data);
            }
        });
}

function renderFriendList(friends) {
    const container = document.getElementById('friendList');
    container.innerHTML = friends.map(f => `
        <div class="friend-item" onclick="openChat(${f.friendInfo.userId}, '${f.friendInfo.nickname}')" data-group="${f.groupName}">
            <img src="${f.friendInfo.avatar || 'default-avatar.png'}" class="avatar">
            <div class="friend-info">
                <div class="nickname">${f.remark || f.friendInfo.nickname}</div>
                <div class="status ${f.friendInfo.status === 1 ? 'online' : 'offline'}">
                    ${f.friendInfo.status === 1 ? '在线' : '离线'}
                </div>
            </div>
            ${f.unreadCount > 0 ? `<span class="badge">${f.unreadCount}</span>` : ''}
        </div>
    `).join('');
}

// 筛选好友
function filterFriends() {
    const keyword = document.getElementById('friendSearchInput').value;
    loadFriends('', keyword);
}

// 加载分组
function loadGroups() {
    fetch('${pageContext.request.contextPath}/friend/groups')
        .then(r => r.json())
        .then(res => {
            if (res.code === 0) {
                const tabs = document.getElementById('groupTabs');
                tabs.innerHTML = '<span class="active" onclick="switchGroup(\'\')">全部</span>' +
                    res.data.map(g => `<span onclick="switchGroup('${g}')">${g}</span>`).join('');
            }
        });
}

function switchGroup(group) {
    document.querySelectorAll('.group-tabs span').forEach(s => s.classList.remove('active'));
    event.target.classList.add('active');
    loadFriends(group);
}

// 初始化
loadGroups();
loadFriends();

// 启动消息轮询
startMessagePolling();
</script>