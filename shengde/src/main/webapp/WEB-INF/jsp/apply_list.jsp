<!-- apply_list.jsp - 好友申请列表 -->
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<div class="apply-panel" id="applyPanel" style="display:none;">
    <h3>好友申请</h3>
    <div id="applyList"></div>
</div>

<script>
function loadApplyList() {
    fetch('${pageContext.request.contextPath}/friend/requests')
        .then(r => r.json())
        .then(res => {
            if (res.code === 0 && res.data.length > 0) {
                document.getElementById('applyPanel').style.display = 'block';
                document.getElementById('applyList').innerHTML = res.data.map(a => `
                    <div class="apply-item">
                        <img src="${a.friendInfo.avatar || 'default-avatar.png'}" class="avatar">
                        <div class="apply-info">
                            <div>${a.friendInfo.nickname} (${a.friendInfo.account})</div>
                            <div class="apply-msg">${a.applyMsg}</div>
                        </div>
                        <div class="apply-actions">
                            <button onclick="handleApply(${a.friendInfo.userId}, true)">同意</button>
                            <button onclick="handleApply(${a.friendInfo.userId}, false)">拒绝</button>
                        </div>
                        <div class="remark-input" id="remark_${a.friendInfo.userId}" style="display:none;">
                            <input type="text" placeholder="备注名" id="input_remark_${a.friendInfo.userId}">
                            <select id="input_group_${a.friendInfo.userId}">
                                <option value="我的好友">我的好友</option>
                            </select>
                            <button onclick="confirmAgree(${a.friendInfo.userId})">确认</button>
                        </div>
                    </div>
                `).join('');
            }
        });
}

function handleApply(friendId, isAgree) {
    if (isAgree) {
        document.getElementById('remark_' + friendId).style.display = 'block';
    } else {
        if (!confirm('确定拒绝该好友申请？')) return;
        fetch('${pageContext.request.contextPath}/friend/reject', {
            method: 'POST',
            body: new URLSearchParams({friendId: friendId})
        }).then(() => loadApplyList());
    }
}

function confirmAgree(friendId) {
    const remark = document.getElementById('input_remark_' + friendId).value;
    const groupName = document.getElementById('input_group_' + friendId).value;
    
    const params = new URLSearchParams();
    params.append('friendId', friendId);
    params.append('remark', remark);
    params.append('groupName', groupName);
    
    fetch('${pageContext.request.contextPath}/friend/agree', {
        method: 'POST',
        body: params
    }).then(() => {
        loadApplyList();
        loadFriends(); // 刷新好友列表
    });
}

// 定期检查新申请
setInterval(loadApplyList, 30000);
loadApplyList();
</script>