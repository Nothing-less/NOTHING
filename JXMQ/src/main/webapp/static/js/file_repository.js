/* =========================================================
   FileRepo - 用户文件仓库（原生 JS，零 EL 冲突）
   ========================================================= */
const FileRepo = (() => {

    const ctx = window.APP.contextPath;
    const list = document.getElementById('fileList');

    let selectedFileId = null;   // 准备发送的文件 ID
    let uploadTimer   = null;    // 搜索防抖

    /* ================= 1. 上传 ================= */

    function upload(input) {
        const file = input.files[0];
        if (!file) return;

        // 显示进度条
        showProgress(0);

        const xhr = new XMLHttpRequest();
        const fd  = new FormData();
        fd.append('file', file);

        const userId = window.APP?.currentUser?.userId;
        console.log("userId value:", userId, "type:", typeof userId);
        fd.append('userId',userId);

        xhr.upload.addEventListener('progress', e => {
            if (e.lengthComputable) {
                const pct = Math.round(e.loaded / e.total * 100);
                showProgress(pct);
            }
        });

        xhr.addEventListener('load', () => {
            hideProgress();
            input.value = ''; // 重置，允许重复选同一文件

            try {
                const res = JSON.parse(xhr.responseText);
                if (res.code === 200) {
                    alert('✅ 上传成功');
                    loadList();
                } else {
                    alert('❌ ' + (res.message || '上传失败'));
                }
            } catch (e) {
                alert('❌ 服务器响应异常');
            }
        });

        xhr.addEventListener('error', () => {
            hideProgress();
            alert('❌ 网络错误');
        });

        xhr.open('POST', ctx + '/file/upload');
        xhr.send(fd);
    }

    function showProgress(pct) {
        const bar  = document.getElementById('uploadProgress');
        const fill = document.getElementById('uploadProgressBar');
        const text = document.getElementById('uploadProgressText');
        bar.style.display  = 'flex';
        fill.style.width   = pct + '%';
        text.textContent    = pct + '%';
    }

    function hideProgress() {
        setTimeout(() => {
            document.getElementById('uploadProgress').style.display = 'none';
            document.getElementById('uploadProgressBar').style.width = '0%';
        }, 500);
    }

    /* ================= 2. 列表 ================= */

    function loadList() {
        list.innerHTML = '';

        fetch(ctx + '/file/list', {credentials: 'include'})
            .then(r => r.json())
            .then(res => {
                if (res.code === 200) {
                    renderList(res.data || []);
                } else {
                    list.innerHTML = '<div class="empty">加载失败</div>';
                }
            })
            .catch(() => {
                list.innerHTML = '<div class="empty">网络错误</div>';
            });
    }

    function renderList(files) {
        if (!files.length) {
            list.innerHTML = '<div class="empty">📁 暂无文件，点击上方按钮上传</div>';
            return;
        }

        list.innerHTML = '';

        files.forEach(f => {
            const item = document.createElement('div');
            item.className = 'file-item';

            // 图标
            const icon = document.createElement('div');
            icon.className = 'file-icon';
            icon.textContent = getFileEmoji(f.fileName);

            // 信息
            const info = document.createElement('div');
            info.className = 'file-info';

            const name = document.createElement('div');
            name.className = 'file-name';
            name.textContent = f.fileName;

            const meta = document.createElement('div');
            meta.className = 'file-meta';
            meta.textContent = formatSize(f.fileSize) + ' · ' + (f.uploadTime || '');

            info.appendChild(name);
            info.appendChild(meta);

            // 操作按钮
            const actions = document.createElement('div');
            actions.className = 'file-actions';

            actions.appendChild(makeBtn('⬇️ 下载', 'btn-download', () => download(f.id)));
            actions.appendChild(makeBtn('📤 发送', 'btn-send',    () => openSendModal(f.id)));
            actions.appendChild(makeBtn('🗑️ 删除', 'btn-delete',  () => deleteFile(f.id)));

            item.appendChild(icon);
            item.appendChild(info);
            item.appendChild(actions);

            list.appendChild(item);
        });
    }

    /* ================= 3. 搜索 ================= */

    function search() {
        clearTimeout(uploadTimer);
        const kw = document.getElementById('fileSearchInput').value.trim();

        if (kw.length < 1) { loadList(); return; }

        uploadTimer = setTimeout(() => {
            fetch(ctx + '/file/search?keyword=' + encodeURIComponent(kw))
                .then(r => r.json())
                .then(res => {
                    if (res.code === 200) renderList(res.data || []);
                });
        }, 300);
    }

    /* ================= 4. 下载 ================= */

    function download(fileId) {
        window.location.href = ctx + '/file/download?fileId=' + fileId;
    }

    /* ================= 5. 删除 ================= */

    function deleteFile(fileId) {
        if (!confirm('确定删除该文件？此操作不可恢复。')) return;

        const params = new URLSearchParams({ fileId });

        fetch(ctx + '/file/delete', { method: 'POST', body: params })
            .then(r => r.json())
            .then(res => {
                alert(res.code === 200 ? '✅ 已删除' : '❌ ' + res.message);
                if (res.code === 200) loadList();
            })
            .catch(() => alert('❌ 网络错误'));
    }

    /* ================= 6. 发送 → 选好友弹窗 ================= */

    function openSendModal(fileId) {
        selectedFileId = fileId;
        document.getElementById('sendFileModal').classList.add('active');
        loadFriendsForPick();
    }

    function closeSendModal() {
        document.getElementById('sendFileModal').classList.remove('active');
        selectedFileId = null;
    }

    function loadFriendsForPick() {
        const container = document.getElementById('friendPickList');
        container.innerHTML = '<div class="search-tip">加载好友列表中...</div>';

        fetch(ctx + '/friend/list')
            .then(r => r.json())
            .then(res => {
                if (res.code !== 200) {
                    container.innerHTML = '<div class="search-tip">加载失败</div>';
                    return;
                }

                const friends = res.data || [];
                if (!friends.length) {
                    container.innerHTML = '<div class="search-tip">暂无好友</div>';
                    return;
                }

                container.innerHTML = '';
                friends.forEach(f => {
                    const u = f.friendInfo || f;
                    const item = document.createElement('div');
                    item.className = 'friend-pick-item';
                    item.dataset.uid = u.userId;

                    const avatar = document.createElement('img');
                    avatar.className = 'friend-pick-avatar';
                    avatar.src = (u.userKey2 || '').trim() ||
                                ctx + '/static/images/default-avatar.png';
                    avatar.onerror = function() {
                        this.onerror = null;
                        this.src = ctx + '/static/images/default-avatar.png';
                    };

                    const text = document.createElement('div');
                    text.className = 'friend-pick-text';
                    text.textContent = u.nickname || '未知用户';

                    item.appendChild(avatar);
                    item.appendChild(text);

                    item.addEventListener('click', () => {
                        sendTo(u.userId, u.nickname || '该好友');
                    });

                    container.appendChild(item);
                });
            });
    }

    function sendTo(friendId, friendName) {
        if (!selectedFileId) return;

        if (!confirm('确定发送该文件给「' + friendName + '」？')) return;

        const params = new URLSearchParams({
            fileId:   selectedFileId,
            friendId: friendId
        });

        fetch(ctx + '/file/send', { method: 'POST', body: params })
            .then(r => r.json())
            .then(res => {
                alert(res.code === 200 ? '✅ 发送成功' : '❌ ' + res.message);
                if (res.code === 200) closeSendModal();
            })
            .catch(() => alert('❌ 网络错误'));
    }

    /* ================= 7. 已发送文件（撤回管理） ================= */

    function openSentModal() {
        document.getElementById('sentFilesModal').classList.add('active');
        loadSentFiles();
    }

    function closeSentModal() {
        document.getElementById('sentFilesModal').classList.remove('active');
    }

    function loadSentFiles() {
        const container = document.getElementById('sentFilesList');
        container.innerHTML = '';

        fetch(ctx + '/file/sent')
            .then(r => r.json())
            .then(res => {
                if (res.code !== 200) {
                    container.innerHTML = '<div class="search-tip">加载失败</div>';
                    return;
                }
                renderSentList(res.data || []);
            });
    }

    function renderSentList(shares) {
        const container = document.getElementById('sentFilesList');

        if (!shares.length) {
            container.innerHTML = '<div class="search-tip">暂无已发送文件</div>';
            return;
        }

        container.innerHTML = '';

        shares.forEach(s => {
            const within24h = isWithin24h(s.sendTime);
            const revoked   = s.isRevoked === 1;

            const item = document.createElement('div');
            item.className = 'sent-file-item' + (revoked ? ' revoked' : '');

            const info = document.createElement('div');
            info.className = 'sent-file-info';

            const name = document.createElement('div');
            name.className = 'sent-file-name';
            name.textContent = s.fileName || '未知文件';

            const detail = document.createElement('div');
            detail.className = 'sent-file-detail';
            detail.textContent =
                '发送给：' + (s.receiverName || '未知') +
                ' · ' + (s.sendTime || '') +
                (revoked ? ' · [已撤回]' : '');

            info.appendChild(name);
            info.appendChild(detail);

            item.appendChild(info);

            // 撤回按钮（24h 内 + 未撤回 才显示）
            if (!revoked && within24h) {
                const btn = makeBtn('↩️ 撤回', 'btn-revoke', () => revoke(s.id, s.fileName));
                item.appendChild(btn);
            }

            container.appendChild(item);
        });
    }

    /* ================= 8. 撤回 ================= */

    function revoke(shareId, fileName) {
        if (!confirm('确定撤回文件「' + fileName + '」？\n（仅 24 小时内可撤回）')) return;

        const params = new URLSearchParams({ shareId });

        fetch(ctx + '/file/revoke', { method: 'POST', body: params })
            .then(r => r.json())
            .then(res => {
                alert(res.code === 200 ? '✅ 已撤回' : '❌ ' + res.message);
                if (res.code === 200) {
                    // 刷新列表
                    loadSentFiles();
                    // 通知聊天窗口
                    document.dispatchEvent(new CustomEvent('file:revoked', {
                        detail: { shareId: shareId }
                    }));
                }
            })
            .catch(() => alert('❌ 网络错误'));
    }

    /* ================= 9. 接收到的文件 ================= */

    function openReceivedModal() {
        document.getElementById('receivedFilesModal').classList.add('active');
        loadReceivedFiles();
    }

    function closeReceivedModal() {
        document.getElementById('receivedFilesModal').classList.remove('active');
    }

    function loadReceivedFiles() {
        const container = document.getElementById('receivedFilesList');
        container.innerHTML = '';

        fetch(ctx + '/file/received')
            .then(r => r.json())
            .then(res => {
                if (res.code !== 200) {
                    container.innerHTML = '<div class="search-tip">加载失败</div>';
                    return;
                }

                const list2 = res.data || [];
                if (!list2.length) {
                    container.innerHTML = '<div class="search-tip">暂无收到的文件</div>';
                    return;
                }

                container.innerHTML = '';
                list2.forEach(s => {
                    const item = document.createElement('div');
                    item.className = 'sent-file-item';

                    const info = document.createElement('div');
                    info.className = 'sent-file-info';

                    const name = document.createElement('div');
                    name.className = 'sent-file-name';
                    name.textContent = s.fileName || '未知文件';

                    const detail = document.createElement('div');
                    detail.className = 'sent-file-detail';
                    detail.textContent =
                        '来自：' + (s.senderName || '未知') +
                        ' · ' + (s.sendTime || '');

                    info.appendChild(name);
                    info.appendChild(detail);

                    const dlBtn = makeBtn('⬇️ 下载', 'btn-download', () => {
                        // 下载原始文件（通过 shareId 定位）
                        window.location.href = ctx + '/file/download?shareId=' + s.id;
                    });

                    item.appendChild(info);
                    item.appendChild(dlBtn);
                    container.appendChild(item);
                });
            });
    }

    /* ================= 10. 工具函数 ================= */

    function makeBtn(text, className, onClick) {
        const btn = document.createElement('button');
        btn.type = 'button';
        btn.className = className;
        btn.textContent = text;
        btn.addEventListener('click', onClick);
        return btn;
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
            zip:'📦',rar:'📦','7z':'📦',tar:'📦',gz:'📦',
            txt:'📝',json:'📋',xml:'📋',html:'📋',
            java:'☕',js:'📜',py:'🐍',c:'📜',cpp:'📜'
        };
        return map[ext] || '📄';
    }

    function isWithin24h(timeStr) {
        if (!timeStr) return false;
        try {
            const t = new Date(timeStr.replace(' ', 'T') + 'Z');
            return (Date.now() - t.getTime()) < 24 * 3600 * 1000;
        } catch(e) { return false; }
    }

    function escapeHtml(str) {
        if (!str) return '';
        return String(str)
            .replace(/&/g, '&amp;').replace(/</g, '&lt;')
            .replace(/>/g, '&gt;').replace(/"/g, '&quot;')
            .replace(/'/g, '&#39;');
    }

    /* ================= 初始化 ================= */

    loadList();

    /* ================= 对外暴露 ================= */

    return {
        upload,
        search,
        download,
        deleteFile,
        openSendModal,
        closeSendModal,
        openSentModal,
        closeSentModal,
        openReceivedModal,
        closeReceivedModal,
        revoke
    };

})();
