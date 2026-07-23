/* =========================================================
   ChatFileRevoke - 聊天窗口文件撤回集成
   =========================================================
   使用方式：
   1. 在聊天窗口 JSP 中引入本文件
   2. 在渲染消息时，对文件类型消息调用：
      ChatFileRevoke.renderFileMessage(msg, container)
   3. 监听撤回事件刷新消息列表：
      document.addEventListener('file:revoked', () => { ...刷新消息... });
   ========================================================= */

const ChatFileRevoke = (() => {

    const ctx = window.APP?.contextPath || '';

    /* ================= 判断消息是否为文件类型 ================= */

    function isFileMessage(msg) {
        return msg.type === 'FILE' || msg.msgType === 'FILE'
            || msg.fileId != null || msg.shareId != null;
    }

    /* ================= 渲染文件消息气泡 ================= */
    /*
     * msg 字段约定（和后端 FileShare / Message 对齐）：
     *   - id / msgId       消息ID
     *   - shareId          文件分享记录ID（用于撤回）
     *   - senderId         发送者ID
     *   - fileId           文件ID
     *   - fileName         文件名
     *   - fileSize         文件大小（字节）
     *   - sendTime         发送时间
     *   - revoked          是否已撤回（0/1 或 true/false）
     *   - isMine           是否是我发的（可前端自己算）
     */
    function renderFileMessage(msg, container) {
        const isMine = msg.isMine !== undefined
            ? msg.isMine
            : (msg.senderId === (window.APP?.currentUser?.userId || 0));

        const revoked = msg.revoked === 1 || msg.revoked === true;

        // 已撤回 → 显示撤回标记
        if (revoked) {
            container.appendChild(buildRevokedTag(msg, isMine));
            return;
        }

        const within24h = checkWithin24h(msg.sendTime);
        const shareId    = msg.shareId || msg.id;
        const fileName   = msg.fileName || '未知文件';
        const fileSize   = formatSize(msg.fileSize || 0);

        // 气泡容器
        const bubble = document.createElement('div');
        bubble.className = 'msg-bubble msg-file ' + (isMine ? 'mine' : 'theirs');

        // 文件图标 + 名称 + 大小
        const fileRow = document.createElement('div');
        fileRow.className = 'file-msg-row';

        const emoji = getFileEmoji(fileName);
        const iconSpan = document.createElement('span');
        iconSpan.className = 'file-msg-icon';
        iconSpan.textContent = emoji;

        const textWrap = document.createElement('div');
        textWrap.className = 'file-msg-text';

        const nameDiv = document.createElement('div');
        nameDiv.className = 'file-msg-name';
        nameDiv.textContent = fileName;

        const sizeDiv = document.createElement('div');
        sizeDiv.className = 'file-msg-size';
        sizeDiv.textContent = fileSize;

        textWrap.appendChild(nameDiv);
        textWrap.appendChild(sizeDiv);
        fileRow.appendChild(iconSpan);
        fileRow.appendChild(textWrap);

        bubble.appendChild(fileRow);

        // 下载按钮
        const dlBtn = document.createElement('button');
        dlBtn.type = 'button';
        dlBtn.className = 'btn-msg-download';
        dlBtn.textContent = '⬇️ 下载';
        dlBtn.addEventListener('click', () => {
            window.location.href = ctx + '/file/download?shareId=' + shareId;
        });
        bubble.appendChild(dlBtn);

        // ★ 撤回按钮（仅自己发的 + 24h 内 + 未撤回）
        if (isMine && within24h) {
            const revokeBtn = document.createElement('button');
            revokeBtn.type = 'button';
            revokeBtn.className = 'btn-msg-revoke';
            revokeBtn.textContent = '↩️ 撤回';
            revokeBtn.addEventListener('click', () => {
                doRevoke(shareId, fileName, bubble);
            });
            bubble.appendChild(revokeBtn);
        }

        container.appendChild(bubble);
    }

    /* ================= 执行撤回 ================= */

    function doRevoke(shareId, fileName, bubbleNode) {
        if (!confirm('确定撤回文件「' + fileName + '」？\n（仅 24 小时内可撤回）')) return;

        const params = new URLSearchParams({ shareId: shareId });

        fetch(ctx + '/file/revoke', { method: 'POST', body: params })
            .then(r => r.json())
            .then(res => {
                if (res.code === 200) {
                    // 气泡就地变成"已撤回"
                    if (bubbleNode && bubbleNode.parentNode) {
                        const replacement = buildRevokedTag({
                            isMine: true
                        }, true);
                        bubbleNode.parentNode.replaceChild(replacement, bubbleNode);
                    }
                    // 通知外部刷新
                    document.dispatchEvent(new CustomEvent('file:revoked', {
                        detail: { shareId: shareId }
                    }));
                } else {
                    alert('❌ ' + (res.message || '撤回失败'));
                }
            })
            .catch(() => alert('❌ 网络错误'));
    }

    /* ================= 构建"已撤回"标记 ================= */

    function buildRevokedTag(msg, isMine) {
        const tag = document.createElement('div');
        tag.className = 'msg-bubble msg-revoked ' + (isMine ? 'mine' : 'theirs');

        const icon = document.createElement('span');
        icon.textContent = '↩️';

        const text = document.createElement('span');
        text.textContent = isMine ? '你撤回了一个文件' : '对方撤回了一个文件';

        tag.appendChild(icon);
        tag.appendChild(text);
        return tag;
    }

    /* ================= 工具函数 ================= */

    function checkWithin24h(timeStr) {
        if (!timeStr) return false;
        try {
            const t = new Date(timeStr.replace(' ', 'T') + 'Z');
            return (Date.now() - t.getTime()) < 24 * 3600 * 1000;
        } catch (e) { return false; }
    }

    function formatSize(bytes) {
        if (!bytes) return '0 B';
        if (bytes < 1024) return bytes + ' B';
        if (bytes < 1048576) return (bytes / 1024).toFixed(1) + ' KB';
        if (bytes < 1073741824) return (bytes / 1048576).toFixed(1) + ' MB';
        return (bytes / 1073741824).toFixed(2) + ' GB';
    }

    function getFileEmoji(name) {
        if (!name) return '📄';
        const ext = name.split('.').pop().toLowerCase();
        const map = {
            jpg:'🖼️',jpeg:'🖼️',png:'🖼️',gif:'🖼️',bmp:'🖼️',webp:'🖼️',
            mp4:'🎬',avi:'🎬',mov:'🎬',mkv:'🎬',wmv:'🎬',
            mp3:'🎵',wav:'🎵',flac:'🎵',aac:'🎵',
            pdf:'📕',doc:'📘',docx:'📘',xls:'📗',xlsx:'📗',ppt:'📙',pptx:'📙',
            zip:'📦',rar:'📦',7z:'📦',tar:'📦',gz:'📦',
            txt:'📝',json:'📋',xml:'📋',html:'📋',
            java:'☕',js:'📜',py:'🐍',c:'📜',cpp:'📜'
        };
        return map[ext] || '📄';
    }

    /* ================= 对外暴露 ================= */

    return {
        isFileMessage,
        renderFileMessage,
        doRevoke
    };

})();
