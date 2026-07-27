# 聊天窗口集成文件撤回 — 接入指南

## 你需要做的 3 步

### 第 1 步：在聊天窗口 JSP 中引入文件

```jsp
<!-- 在聊天窗口 JSP 的 <head> 或底部引入 -->
<link rel="stylesheet" href="<c:url value='/static/css/chat_file_revoke.css' />">
<script src="<c:url value='/static/js/pages/chat_file_revoke.js' />"></script>
```

### 第 2 步：在消息渲染函数中接入

找到你聊天窗口 JS 中**渲染单条消息的函数**（比如 `renderMessage`、`appendMsg`、`addMessage` 等），加入判断：

```js
function renderMessage(msg, container) {
    // ★ 新增：如果是文件类型，用 ChatFileRevoke 渲染
    if (ChatFileRevoke.isFileMessage(msg)) {
        ChatFileRevoke.renderFileMessage(msg, container);
        return;  // 不走普通文本渲染
    }

    // 原来的文本/图片消息渲染逻辑...
    if (msg.type === 'TEXT') {
        // ...原有代码
    }
}
```

### 第 3 步：监听撤回事件，刷新消息列表

```js
// 撤回成功后，刷新当前聊天记录
document.addEventListener('file:revoked', function(e) {
    const shareId = e.detail.shareId;
    console.log('文件已撤回，刷新消息列表', shareId);
    // 调用你现有的刷新方法
    loadMessages(currentFriendId);
});
```

---

## msg 对象字段约定

`ChatFileRevoke.renderFileMessage(msg, container)` 需要的字段：

| 字段 | 类型 | 说明 |
|------|------|------|
| `shareId` | number | 文件分享记录 ID（**撤回用这个**） |
| `fileId` | number | 文件 ID |
| `fileName` | string | 文件名（用于显示 + 图标） |
| `fileSize` | number | 文件大小（字节） |
| `senderId` | number | 发送者 ID |
| `sendTime` | string | 发送时间（用于 24h 判断） |
| `revoked` | 0/1 或 bool | 是否已撤回 |
| `isMine` | bool | 是否是我发的（可选，不传则自动判断） |

---

## 效果

- 📎 文件消息显示为带图标 + 文件名 + 大小的气泡
- ⬇️ 下载按钮直接下载文件
- ↩️ **撤回按钮仅在我发的 + 24h 内的文件消息上显示**
- ↩️ 点击撤回 → 气泡就地变成 "你撤回了一个文件"
- ↩️ 超过 24h 自动不显示撤回按钮

---

## 后端配合

确保你聊天消息接口返回的 JSON 中包含 `shareId` 字段：

```json
{
    "type": "FILE",
    "shareId": 42,
    "fileId": 15,
    "fileName": "report.pdf",
    "fileSize": 204800,
    "senderId": 9,
    "sendTime": "2026-07-21 15:30:00",
    "revoked": 0
}
```

如果后端还没返回 `shareId`，需要在消息查询 SQL/DAO 中 JOIN `file_share` 表补上。
