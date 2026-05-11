<!-- friend_list.jsp - 好友列表面板 -->
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<link rel="stylesheet" href="<c:url value='/static/css/pages.css' />">
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
<script src="<c:url value='/static/js/ChatClient.js' />"></script>
<script>
    // 加载好友列表
    function loadFriends(group = '', keyword = '') {
        fetch('${pageContext.request.contextPath}/friend/list?group=' + group + '&keyword=' + keyword)
            .then(r => r.json())
            .then(res => {
                if (res.code === 200) {
                    renderFriendList(res.data);
                } else {
                    console.error('接口错误:', res.message);
                    document.getElementById('friendList').innerHTML = 
                        '<div class="empty">加载失败：' + (res.message || '未知错误') + '</div>';
                }
            })
            .catch(err => {
                console.error('网络错误:', err);
                document.getElementById('friendList').innerHTML = 
                    '<div class="empty">网络错误，请刷新重试</div>';
            });
    }

    function renderFriendList(friends) {
        const container = document.getElementById('friendList');
        
        if (!friends || !Array.isArray(friends)) {
            container.innerHTML = '<div class="empty">暂无好友</div>';
            return;
        }
        
        if (friends.length === 0) {
            container.innerHTML = '<div class="empty">暂无好友</div>';
            return;
        }

        container.innerHTML = friends.map(function(f) {
            const friendInfo = f.friendInfo || {};
            const nickname = friendInfo.nickname || '未知用户';
            const userId = friendInfo.userId;
            const status = friendInfo.userKey1 || 'offline';
            const remark = f.remark || nickname;
            const groupName = f.groupName || '默认分组';
            const unreadCount = f.unreadCount || f.unreadMsgCount || 0;

            return '<div class="friend-item" onclick="openChat(' + userId + ', \'' + nickname + '\')" data-group="' + groupName + '">' +
                '<div class="friend-info">' +
                    '<div class="nickname">' + remark + '</div>' +
                    '<div class="status ' + status + '">' + status + '</div>' +
                '</div>' +
                (unreadCount > 0 ? '<span class="badge">' + unreadCount + '</span>' : '') +
            '</div>';
        }).join('');
    }

    // 筛选好友
    function filterFriends() {
        const keyword = document.getElementById('friendSearchInput').value;
        loadFriends('', keyword);
    }

    // 加载分组
    function loadGroups() {
        fetch('${pageContext.request.contextPath}/friend/groups')
            .then(function(r) { return r.json(); })
            .then(function(res) {
                if (res.code === 200) {
                    const tabs = document.getElementById('groupTabs');
                    tabs.innerHTML = '<span class="active" onclick="switchGroup(\'\')">全部</span>' +
                        res.data.map(function(g) {
                            return '<span onclick="switchGroup(\'' + g + '\')">' + g + '</span>';
                        }).join('');
                } else {
                    console.error('获取分组失败:', res.message);
                }
            })
            .catch(function(err) {
                console.error('网络错误:', err);
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

</script>