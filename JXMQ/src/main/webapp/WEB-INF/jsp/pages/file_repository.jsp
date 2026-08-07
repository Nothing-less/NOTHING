<!-- file_repository.jsp - 个人文件仓库页面 -->
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ page import="icu.nothingless.commons.RespEntity" %>
<%@ page import="icu.nothingless.pojo.dto.User" %>
<%@ page import="icu.nothingless.tools.RedirectUtil" %>
<%@ page import="icu.nothingless.config.GlobalParams" %>

<link rel="stylesheet" href="<c:url value='/static/css/file_repository.css' />">

<%
    User currentUser = (User) RedirectUtil.getFlash(request, GlobalParams.CURRENT_USER);
    String contextPath = request.getContextPath();
%>

<!-- 全局配置，必须在所有 JS 之前 -->
<script>
    window.APP = {
        contextPath: '${pageContext.request.contextPath}',
        currentUser: {
            userId: '${sessionScope.CURRENT_USER_ID}',
            nickname: '${sessionScope.CURRENT_USER_NICKNAME}'
        }
    };
</script>

<div class="file-panel">

    <!-- ===== 顶部操作栏 ===== -->
    <div class="file-header">
        <div class="file-header-left">

        </div>
        <div class="file-header-right">
        
            <button class="btn-upload" onclick="document.getElementById('fileInput').click()">
                📤 上传文件
            </button>
            <input type="file" id="fileInput" style="display:none"
                onchange="FileRepo.upload(this)">

            <button class="btn-upload" style="background:#6c757d;" 
                    onclick="FileRepo.openSentModal()">
                ↩️ 已发送
            </button>
            
            <button class="btn-upload" style="background:#28a745;" 
                    onclick="FileRepo.openReceivedModal()">
                📥 收到的文件
            </button>

            <input type="text" id="fileSearchInput"
                placeholder="🔍 搜索文件名..."
                onkeyup="FileRepo.search()">
        </div>
    </div>

    <!-- ===== 文件列表 ===== -->
    <div class="file-list" id="fileList">
        <%-- <div class="loading"></div> --%>
    </div>

    <!-- ===== 上传进度条（隐藏） ===== -->
    <div class="upload-progress" id="uploadProgress" style="display:none;">
        <div class="upload-progress-bar" id="uploadProgressBar"></div>
        <span class="upload-progress-text" id="uploadProgressText">0%</span>
    </div>
</div>

<!-- ===== 发送给好友弹窗 ===== -->
<div class="modal" id="sendFileModal">
    <div class="modal-content">
        <div class="modal-header">
            <h3>📤 发送给好友</h3>
            <button class="btn-modal-close" type="button"
                    onclick="FileRepo.closeSendModal()">关闭</button>
        </div>
        <div class="friend-pick-list" id="friendPickList">
            <div class="search-tip">加载好友列表中...</div>
        </div>
    </div>
</div>

<!-- ===== 已发送文件（撤回管理）弹窗 ===== -->
<div class="modal" id="sentFilesModal">
    <div class="modal-content">
        <div class="modal-header">
            <h3>↩️ 已发送文件</h3>
            <button class="btn-modal-close" type="button"
                    onclick="FileRepo.closeSentModal()">关闭</button>
        </div>
        <div class="sent-files-list" id="sentFilesList">
            <div class="search-tip">加载中...</div>
        </div>
    </div>
</div>

<!-- ===== 接收到的文件弹窗 ===== -->
<div class="modal" id="receivedFilesModal">
    <div class="modal-content">
        <div class="modal-header">
            <h3>📥 收件列表</h3>
            <button class="btn-modal-close" type="button"
                    onclick="FileRepo.closeReceivedModal()">关闭</button>
        </div>
        <div class="sent-files-list" id="receivedFilesList">
            <div class="search-tip">加载中...</div>
        </div>
    </div>
</div>

<!-- 依赖 -->
<script src="<c:url value='/static/js/ChatClient.js' />"></script>
<!-- 业务 JS -->
<script src="<c:url value='/static/js/file_repository.js' />"></script>
